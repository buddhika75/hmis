package com.divudi.bean.pharmacy;

import com.divudi.bean.common.SessionController;
import com.divudi.bean.common.WebUserController;
import com.divudi.core.data.Privileges;
import com.divudi.core.data.BillClassType;
import com.divudi.core.data.BillNumberSuffix;
import com.divudi.core.data.BillType;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.pharmacy.ItemBatch;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.entity.pharmacy.Stock;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.BillItemFacade;
import com.divudi.core.facade.PharmaceuticalBillItemFacade;
import com.divudi.core.facade.StockFacade;
import com.divudi.core.util.JsfUtil;
import com.divudi.ejb.BillNumberGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

/**
 * Controller for capturing pharmacy stock snapshots and exporting them to Excel.
 */
@Named
@SessionScoped
public class PharmacyStockTakeController implements Serializable {

    @Inject
    private SessionController sessionController;
    @Inject
    private WebUserController webUserController;

    @EJB
    private StockFacade stockFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private BillNumberGenerator billNumberBean;

    private Bill snapshotBill;
    private Bill physicalCountBill;
    private UploadedFile file;

    /**
     * Capture current department stock into a snapshot bill.
     */
    public void captureSnapshot() {
        Department dept = sessionController.getLoggedUser().getDepartment();
        if (dept == null) {
            JsfUtil.addErrorMessage("No department found");
            return;
        }
        String jpql = "select s from Stock s where s.department=:d and s.stock>0";
        HashMap<String, Object> params = new HashMap<>();
        params.put("d", dept);
        List<Stock> stocks = stockFacade.findByJpql(jpql, params);
        if (stocks == null || stocks.isEmpty()) {
            JsfUtil.addErrorMessage("No stock available");
            return;
        }
        snapshotBill = new Bill();
        snapshotBill.setBillType(BillType.PharmacySnapshotBill);
        snapshotBill.setBillClassType(BillClassType.BilledBill);
        snapshotBill.setDepartment(dept);
        snapshotBill.setInstitution(dept.getInstitution());
        snapshotBill.setCreatedAt(new Date());
        snapshotBill.setCreater(sessionController.getLoggedUser());
        String deptId = billNumberBean.departmentBillNumberGenerator(dept, BillType.PharmacySnapshotBill, BillClassType.BilledBill, BillNumberSuffix.NONE);
        snapshotBill.setInsId(deptId);
        snapshotBill.setDeptId(deptId);
        billFacade.create(snapshotBill);

        for (Stock s : stocks) {
            BillItem bi = new BillItem();
            bi.setBill(snapshotBill);
            bi.setItem(s.getItemBatch().getItem());
            bi.setQty(s.getStock());
            bi.setCreatedAt(new Date());
            bi.setCreater(sessionController.getLoggedUser());
            billItemFacade.create(bi);
            PharmaceuticalBillItem pbi = new PharmaceuticalBillItem();
            pbi.setBillItem(bi);
            pbi.setItemBatch(s.getItemBatch());
            pbi.setQty(s.getStock());
            pbi.setStock(s);
            pharmaceuticalBillItemFacade.create(pbi);
            bi.setPharmaceuticalBillItem(pbi);
            snapshotBill.getBillItems().add(bi);
        }
        billFacade.edit(snapshotBill);
        JsfUtil.addSuccessMessage("Snapshot created");
    }

    /**
     * Download guided sheet with current system quantities.
     */
    public StreamedContent downloadGuidedSheet() {
        return generateSheet(true, "pharmacy_stock_guided.xlsx");
    }

    /**
     * Download blind sheet without system quantities.
     */
    public StreamedContent downloadBlindSheet() {
        return generateSheet(false, "pharmacy_stock_blind.xlsx");
    }

    private StreamedContent generateSheet(boolean includeSystemQty, String fileName) {
        if (snapshotBill == null || snapshotBill.getBillItems().isEmpty()) {
            return null;
        }
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Stock");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Code");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Batch");
            header.createCell(3).setCellValue("Expiry Date");
            int col = 4;
            if (includeSystemQty) {
                header.createCell(col++).setCellValue("System Qty");
            }
            header.createCell(col).setCellValue("Counted Qty");
            int rowNum = 1;
            for (BillItem bi : snapshotBill.getBillItems()) {
                PharmaceuticalBillItem pbi = bi.getPharmaceuticalBillItem();
                ItemBatch ib = pbi.getItemBatch();
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(ib.getItem().getCode());
                row.createCell(1).setCellValue(ib.getItem().getName());
                row.createCell(2).setCellValue(ib.getBatchNo());
                row.createCell(3).setCellValue(ib.getDateOfExpire() != null ? ib.getDateOfExpire().toString() : "");
                int c = 4;
                if (includeSystemQty) {
                    row.createCell(c++).setCellValue(pbi.getQty());
                }
                // Counted quantity cell left blank for user input
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            return DefaultStreamedContent.builder()
                    .name(fileName)
                    .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .stream(() -> in)
                    .build();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Parse uploaded sheet and prepare physical count bill with variances.
     */
    public void parseUploadedSheet() {
        if (snapshotBill == null) {
            JsfUtil.addErrorMessage("No snapshot available");
            return;
        }
        if (file == null) {
            JsfUtil.addErrorMessage("No file uploaded");
            return;
        }
        try (InputStream in = file.getInputStream(); XSSFWorkbook wb = new XSSFWorkbook(in)) {
            XSSFSheet sheet = wb.getSheetAt(0);
            Department dept = sessionController.getLoggedUser().getDepartment();
            physicalCountBill = new Bill();
            physicalCountBill.setBillType(BillType.PharmacyPhysicalCountBill);
            physicalCountBill.setBillClassType(BillClassType.BilledBill);
            physicalCountBill.setDepartment(dept);
            physicalCountBill.setInstitution(dept.getInstitution());
            physicalCountBill.setCreatedAt(new Date());
            physicalCountBill.setCreater(sessionController.getLoggedUser());
            physicalCountBill.setReferenceBill(snapshotBill);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String code = getString(row, 0);
                String batch = getString(row, 2);
                // Skip expiry date column (column 3) and get physical count from last column
                double physical = getDouble(row, row.getLastCellNum() - 1);
                if (code == null || batch == null) {
                    continue;
                }
                BillItem snapItem = findSnapshotBillItem(code, batch);
                if (snapItem == null) {
                    continue;
                }
                BillItem bi = new BillItem();
                bi.setBill(physicalCountBill);
                bi.setItem(snapItem.getItem());
                bi.setQty(physical);
                bi.setCreatedAt(new Date());
                bi.setCreater(sessionController.getLoggedUser());
                bi.setReferanceBillItem(snapItem);
                bi.setAdjustedValue(physical - snapItem.getQty());
                PharmaceuticalBillItem pbi = new PharmaceuticalBillItem();
                pbi.setBillItem(bi);
                pbi.setItemBatch(snapItem.getPharmaceuticalBillItem().getItemBatch());
                pbi.setQty(physical);
                bi.setPharmaceuticalBillItem(pbi);
                physicalCountBill.getBillItems().add(bi);
            }
        } catch (IOException e) {
            JsfUtil.addErrorMessage(e, "Error processing file");
            physicalCountBill = null;
        }
    }

    private String getString(Row row, int col) {
        if (row.getCell(col) == null) {
            return null;
        }
        try {
            return row.getCell(col).getStringCellValue().trim();
        } catch (Exception e) {
            try {
                return String.valueOf((long) row.getCell(col).getNumericCellValue());
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private double getDouble(Row row, int col) {
        if (row.getCell(col) == null) {
            return 0.0;
        }
        try {
            return row.getCell(col).getNumericCellValue();
        } catch (Exception e) {
            try {
                return Double.parseDouble(row.getCell(col).getStringCellValue());
            } catch (Exception ex) {
                return 0.0;
            }
        }
    }

    private BillItem findSnapshotBillItem(String code, String batch) {
        if (snapshotBill == null) {
            return null;
        }
        for (BillItem bi : snapshotBill.getBillItems()) {
            PharmaceuticalBillItem pbi = bi.getPharmaceuticalBillItem();
            if (pbi == null || pbi.getItemBatch() == null) {
                continue;
            }
            if (code.equalsIgnoreCase(pbi.getItemBatch().getItem().getCode())
                    && batch.equalsIgnoreCase(pbi.getItemBatch().getBatchNo())) {
                return bi;
            }
        }
        return null;
    }

    /**
     * Persist prepared physical count bill.
     */
    public void savePhysicalCount() {
        if (physicalCountBill == null || physicalCountBill.getBillItems().isEmpty()) {
            JsfUtil.addErrorMessage("No physical counts to save");
            return;
        }
        Department dept = sessionController.getLoggedUser().getDepartment();
        String deptId = billNumberBean.departmentBillNumberGenerator(dept, BillType.PharmacyPhysicalCountBill, BillClassType.BilledBill, BillNumberSuffix.NONE);
        physicalCountBill.setInsId(deptId);
        physicalCountBill.setDeptId(deptId);
        physicalCountBill.setBackwardReferenceBill(snapshotBill);
        if (snapshotBill != null) {
            snapshotBill.setForwardReferenceBill(physicalCountBill);
            billFacade.edit(snapshotBill);
        }
        billFacade.create(physicalCountBill);
        for (BillItem bi : physicalCountBill.getBillItems()) {
            billItemFacade.create(bi);
            PharmaceuticalBillItem pbi = bi.getPharmaceuticalBillItem();
            if (pbi != null) {
                pharmaceuticalBillItemFacade.create(pbi);
            }
        }
        billFacade.edit(physicalCountBill);
        JsfUtil.addSuccessMessage("Physical count saved");
    }

    public void approvePhysicalCount() {
        if (physicalCountBill == null) {
            JsfUtil.addErrorMessage("No physical count available");
            return;
        }
        if (!webUserController.hasPrivilege(Privileges.PharmacyStockTakeApprove.toString())) {
            JsfUtil.addErrorMessage("Not authorized");
            return;
        }
        Department dept = sessionController.getLoggedUser().getDepartment();
        Bill adjustmentBill = new Bill();
        adjustmentBill.setBillType(BillType.PharmacyStockAdjustmentBill);
        adjustmentBill.setBillClassType(BillClassType.BilledBill);
        adjustmentBill.setDepartment(dept);
        adjustmentBill.setInstitution(dept.getInstitution());
        adjustmentBill.setCreatedAt(new Date());
        adjustmentBill.setCreater(sessionController.getLoggedUser());
        String deptId = billNumberBean.departmentBillNumberGenerator(dept, BillType.PharmacyStockAdjustmentBill, BillClassType.BilledBill, BillNumberSuffix.NONE);
        adjustmentBill.setInsId(deptId);
        adjustmentBill.setDeptId(deptId);
        adjustmentBill.setBackwardReferenceBill(physicalCountBill);
        physicalCountBill.setForwardReferenceBill(adjustmentBill);
        billFacade.create(adjustmentBill);
        for (BillItem bi : physicalCountBill.getBillItems()) {
            double variance = bi.getAdjustedValue();
            if (variance == 0) {
                continue;
            }
            BillItem abi = new BillItem();
            abi.setBill(adjustmentBill);
            abi.setItem(bi.getItem());
            abi.setQty(variance);
            abi.setCreatedAt(new Date());
            abi.setCreater(sessionController.getLoggedUser());
            billItemFacade.create(abi);
            PharmaceuticalBillItem apbi = new PharmaceuticalBillItem();
            apbi.setBillItem(abi);
            apbi.setItemBatch(bi.getPharmaceuticalBillItem().getItemBatch());
            Stock stock = bi.getReferanceBillItem().getPharmaceuticalBillItem().getStock();
            apbi.setStock(stock);
            apbi.setQty(variance);
            pharmaceuticalBillItemFacade.create(apbi);
            abi.setPharmaceuticalBillItem(apbi);
            adjustmentBill.getBillItems().add(abi);
            if (stock != null) {
                stock.setStock(stock.getStock() + variance);
                stockFacade.edit(stock);
            }
        }
        physicalCountBill.setApproveUser(sessionController.getLoggedUser());
        physicalCountBill.setApproveAt(new Date());
        billFacade.edit(physicalCountBill);
        billFacade.edit(adjustmentBill);
        JsfUtil.addSuccessMessage("Physical count approved");
    }

    public void rejectPhysicalCount() {
        if (physicalCountBill == null) {
            JsfUtil.addErrorMessage("No physical count available");
            return;
        }
        if (!webUserController.hasPrivilege(Privileges.PharmacyStockTakeApprove.toString())) {
            JsfUtil.addErrorMessage("Not authorized");
            return;
        }
        physicalCountBill.setApproveUser(sessionController.getLoggedUser());
        physicalCountBill.setApproveAt(new Date());
        physicalCountBill.setCancelled(true);
        billFacade.edit(physicalCountBill);
        JsfUtil.addSuccessMessage("Physical count rejected");
    }

    public Bill getPhysicalCountBill() {
        return physicalCountBill;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public Bill getSnapshotBill() {
        return snapshotBill;
    }

    public void setSnapshotBill(Bill snapshotBill) {
        this.snapshotBill = snapshotBill;
    }
    
    /**
     * Reset the stock taking session to start a new one
     */
    public void resetStockTakingSession() {
        this.snapshotBill = null;
        this.physicalCountBill = null;
        this.file = null;
        JsfUtil.addSuccessMessage("Stock taking session reset");
    }
}
