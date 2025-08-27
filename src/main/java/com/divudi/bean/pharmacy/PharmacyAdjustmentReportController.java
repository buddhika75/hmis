package com.divudi.bean.pharmacy;

import com.divudi.core.data.StockCorrectionRow;
import com.divudi.core.data.BillType;
import com.divudi.core.data.DepartmentType;
import com.divudi.core.entity.Institution;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.pharmacy.StockHistory;
import com.divudi.core.facade.StockHistoryFacade;
import com.divudi.core.util.JsfUtil;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.TemporalType;
import javax.servlet.http.HttpServletResponse;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Named
@SessionScoped
public class PharmacyAdjustmentReportController implements Serializable {

    @EJB
    private StockHistoryFacade stockHistoryFacade;

    private Date fromDate;
    private Date toDate;
    private Institution institution;
    private Institution site;
    private Department department;
    private List<StockCorrectionRow> stockCorrectionRows;

    public void createAdjustmentReport() {
        stockCorrectionRows = new ArrayList<>();
        try {
            List<BillType> billTypes = Arrays.asList(
                    BillType.PharmacyAdjustmentSaleRate,
                    BillType.PharmacyAdjustmentPurchaseRate,
                    BillType.PharmacyAdjustmentWholeSaleRate,
                    BillType.PharmacyAdjustmentCostRate
            );

            StringBuilder jpql = new StringBuilder("SELECT sh FROM StockHistory sh "
                    + "WHERE sh.retired = false "
                    + "AND sh.createdAt BETWEEN :fd AND :td "
                    + "AND (sh.itemBatch.item.departmentType IS NULL OR sh.itemBatch.item.departmentType = :depty) "
                    + "AND sh.pbItem.billItem.bill.billType IN :doctype");

            Map<String, Object> params = new HashMap<>();
            params.put("fd", fromDate);
            params.put("td", toDate);
            params.put("depty", DepartmentType.Pharmacy);
            params.put("doctype", billTypes);

            if (institution != null) {
                jpql.append(" AND sh.institution = :ins");
                params.put("ins", institution);
            }
            if (site != null) {
                jpql.append(" AND sh.department.site = :sit");
                params.put("sit", site);
            }
            if (department != null) {
                jpql.append(" AND sh.department = :dep");
                params.put("dep", department);
            }

            List<StockHistory> histories = stockHistoryFacade.findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);
            if (histories != null) {
                for (StockHistory sh : histories) {
                    if (sh.getPbItem() == null || sh.getItemBatch() == null || sh.getItemBatch().getItem() == null) {
                        continue;
                    }
                    StockCorrectionRow row = new StockCorrectionRow();
                    row.setItemName(sh.getItemBatch().getItem().getName());
                    row.setQty(sh.getPbItem().getStock().getStock());
                    row.setOldRate(sh.getPbItem().getBeforeAdjustmentValue());
                    row.setOldValue(sh.getPbItem().getStock().getStock() * sh.getPbItem().getBeforeAdjustmentValue());
                    row.setNewRate(sh.getPbItem().getAfterAdjustmentValue());
                    row.setNewValue(sh.getPbItem().getStock().getStock() * sh.getPbItem().getAfterAdjustmentValue());
                    row.setVariance(row.getNewValue() - row.getOldValue());
                    stockCorrectionRows.add(row);
                }
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Error generating adjustment report");
        }
    }

    public void exportToExcel() {
        if (stockCorrectionRows == null) {
            JsfUtil.addErrorMessage("No data to export");
            return;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=stock_adjustment_variance.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("Adjustments");
            int rowIndex = 0;
            Row header = sheet.createRow(rowIndex++);
            String[] headers = {"Item", "Qty", "Old Rate", "Old Value", "New Rate", "New Value", "Variance"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            for (StockCorrectionRow r : stockCorrectionRows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(r.getItemName());
                row.createCell(1).setCellValue(r.getQty());
                row.createCell(2).setCellValue(r.getOldRate());
                row.createCell(3).setCellValue(r.getOldValue());
                row.createCell(4).setCellValue(r.getNewRate());
                row.createCell(5).setCellValue(r.getNewValue());
                row.createCell(6).setCellValue(r.getVariance());
            }
            workbook.write(out);
            context.responseComplete();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Error generating Excel report");
        }
    }

    public void exportToPdf() {
        if (stockCorrectionRows == null) {
            JsfUtil.addErrorMessage("No data to export");
            return;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=stock_adjustment_variance.pdf");
        try (OutputStream out = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            document.add(new Paragraph("Stock Adjustment Variance", bold));
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            String[] headers = {"Item", "Qty", "Old Rate", "Old Value", "New Rate", "New Value", "Variance"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, bold));
                table.addCell(cell);
            }
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            for (StockCorrectionRow r : stockCorrectionRows) {
                table.addCell(new Phrase(r.getItemName(), normal));
                table.addCell(new Phrase(String.valueOf(r.getQty()), normal));
                table.addCell(new Phrase(String.valueOf(r.getOldRate()), normal));
                table.addCell(new Phrase(String.valueOf(r.getOldValue()), normal));
                table.addCell(new Phrase(String.valueOf(r.getNewRate()), normal));
                table.addCell(new Phrase(String.valueOf(r.getNewValue()), normal));
                table.addCell(new Phrase(String.valueOf(r.getVariance()), normal));
            }
            document.add(table);
            document.close();
            context.responseComplete();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Error generating PDF report");
        }
    }

    // Getters and setters
    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Institution getSite() {
        return site;
    }

    public void setSite(Institution site) {
        this.site = site;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<StockCorrectionRow> getStockCorrectionRows() {
        return stockCorrectionRows;
    }

    public void setStockCorrectionRows(List<StockCorrectionRow> stockCorrectionRows) {
        this.stockCorrectionRows = stockCorrectionRows;
    }
}

