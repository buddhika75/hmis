package com.divudi.bean.common;

import com.divudi.core.data.BillType;
import com.divudi.core.data.BillTypeAtomic;
import com.divudi.core.data.HistoryType;
import com.divudi.core.data.PaymentMethod;
import com.divudi.core.entity.AgentHistory;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.Institution;
import com.divudi.core.entity.Payment;
import com.divudi.core.facade.AgentHistoryFacade;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.BillItemFacade;
import com.divudi.core.facade.PaymentFacade;
import com.divudi.core.light.common.BillLight;
import com.divudi.core.util.CommonFunctions;
import com.divudi.core.util.JsfUtil;
import com.divudi.ejb.BillNumberGenerator;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TemporalType;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author H.K. Damith Deshan | hkddrajapaksha@gmail.com
 */
@Named
@SessionScoped
public class CollectingCentrePaymentController implements Serializable {

// <editor-fold defaultstate="collapsed" desc="Ejbs">
    @EJB
    BillFacade billFacade;
    @EJB
    private BillNumberGenerator billNumberGenerator;
    @EJB
    PaymentFacade paymentFacade;
    @EJB
    BillItemFacade billItemFacade;
    @EJB
    AgentHistoryFacade agentHistoryFacade;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    SessionController sessionController;
    @Inject
    AgentAndCcApplicationController agentAndCcApplicationController;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Variables">
    private boolean ccPaymentSettlingStarted = false;
    private Date fromDate;
    private Date toDate;

    private Institution currentCollectingCentre;

    private List<BillLight> pandingCCpaymentBills;
    private List<BillLight> selectedCCpaymentBills;

    private double totalCCAmount;
    private double totalHospitalAmount;

    private double totalCCReceiveAmount = 0.0;
    private double payingTotalCCAmount = 0.0;
    private PaymentMethod paymentMethod;

    private boolean printPriview;

    private double startingBalanseInCC = 0.0;
    private double finalEndingBalanseInCC = 0.0;

    private double payingBalanceAcodingToCCBalabce = 0.0;
    private Bill currentPaymentBill;

    private List<Bill> paymentBills;

    private String billNumber;
    private String comment;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Navigation Method">
    public String navigateToSearchCCPaymentBills() {
        makeNull();
        return "/collecting_centre/collecting_centre_repayment_bill_search?faces-redirect=true";
    }

    public String navigateToViewCCPaymentBill(Bill bill) {
        setCurrentPaymentBill(bill);
        return "/collecting_centre/cc_repayment_bill_reprint?faces-redirect=true";
    }

    public String navigateToCCPayment() {
        makeNull();
        return "/collecting_centre/sent_payment_to_collecting_centre?faces-redirect=true";
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Functions">
    public CollectingCentrePaymentController() {
    }

    public void makeNull() {
        totalHospitalAmount = 0.0;
        totalCCAmount = 0.0;
        fromDate = null;
        toDate = null;
        selectedCCpaymentBills = null;
        pandingCCpaymentBills = null;
        ccPaymentSettlingStarted = false;
        currentCollectingCentre = null;
        totalCCReceiveAmount = 0.0;
        payingTotalCCAmount = 0.0;
        paymentMethod = null;
        printPriview = false;
        startingBalanseInCC = 0.0;
        finalEndingBalanseInCC = 0.0;
        payingBalanceAcodingToCCBalabce = 0.0;
        currentPaymentBill = null;
        billNumber = null;
        paymentBills = null;
    }

    public void processCollectingCentrePayment() {
        if (currentCollectingCentre == null) {
            JsfUtil.addErrorMessage("Select Collecting Centre");
            return;
        }

        findPendingCCBills();

        AgentHistory startingHistory = findFirstAgentHistory(currentCollectingCentre);
        AgentHistory endingHistory = findLastAgentHistory(currentCollectingCentre);

        if (startingHistory != null) {
            startingBalanseInCC = startingHistory.getBalanceBeforeTransaction();
        }
        if (endingHistory != null) {
            finalEndingBalanseInCC = endingHistory.getBalanceAfterTransaction();
        }

        calculaPayingBalanceAcodingToCCBalabce(startingHistory, endingHistory);

        calculateTotalOfPaymentReceive();
    }

    public double calculaPayingBalanceAcodingToCCBalabce(AgentHistory startingHistory, AgentHistory endingHistory) {
        double payingBalance = 0.0;
        if (startingHistory != null && endingHistory != null) {
            payingBalance = endingHistory.getBalanceAfterTransaction() - startingHistory.getBalanceBeforeTransaction();
        }

        if (payingBalance > 0.0) {
            payingBalanceAcodingToCCBalabce = payingBalance;
        } else {
            payingBalanceAcodingToCCBalabce = 0.0;
        }
        return payingBalanceAcodingToCCBalabce;
    }

    public AgentHistory findFirstAgentHistory(Institution collectingCentre) {
        List<HistoryType> types = new ArrayList<>();
        types.add(HistoryType.CollectingCentreBilling);
        types.add(HistoryType.CollectingCentreBillingCancel);
        types.add(HistoryType.CollectingCentreBalanceUpdateBill);
        types.add(HistoryType.CollectingCentreDeposit);
        types.add(HistoryType.CollectingCentreDepositCancel);
        types.add(HistoryType.CollectingCentreCreditNote);
        types.add(HistoryType.RepaymentToCollectingCentre);
        types.add(HistoryType.RepaymentToCollectingCentreCancel);

        String jpql = "select ah "
                + " from AgentHistory ah "
                + " where ah.retired=:ret"
                + " and ah.agency =:cc "
                + " and ah.historyType in :types "
                + " and ah.bill.createdAt between :fromDate and :toDate "
                + " and ah.bill.retired = false "
                + " order by ah.bill.createdAt asc ";

        Map<String, Object> m = new HashMap<>();
        m.put("ret", false);
        m.put("cc", collectingCentre);
        m.put("types", types);
        m.put("fromDate", fromDate);
        m.put("toDate", toDate);

        AgentHistory h = agentHistoryFacade.findFirstByJpql(jpql, m, TemporalType.TIMESTAMP);
        return h;

    }

    public AgentHistory findLastAgentHistory(Institution collectingCentre) {
        List<HistoryType> types = new ArrayList<>();
        types.add(HistoryType.CollectingCentreBilling);
        types.add(HistoryType.CollectingCentreBillingCancel);
        types.add(HistoryType.CollectingCentreBalanceUpdateBill);
        types.add(HistoryType.CollectingCentreDeposit);
        types.add(HistoryType.CollectingCentreDepositCancel);
        types.add(HistoryType.CollectingCentreCreditNote);
        types.add(HistoryType.RepaymentToCollectingCentre);
        types.add(HistoryType.RepaymentToCollectingCentreCancel);

        String jpql = "select ah "
                + " from AgentHistory ah "
                + " where ah.retired=:ret"
                + " and ah.agency =:cc "
                + " and ah.historyType in :types "
                + " and ah.bill.createdAt between :fromDate and :toDate "
                + " and ah.bill.retired = false "
                + " order by ah.bill.createdAt desc ";

        Map<String, Object> m = new HashMap<>();
        m.put("ret", false);
        m.put("cc", collectingCentre);
        m.put("types", types);
        m.put("fromDate", fromDate);
        m.put("toDate", toDate);

        AgentHistory h = agentHistoryFacade.findFirstByJpql(jpql, m, TemporalType.TIMESTAMP);
        return h;

    }

    public void findPendingCCBills() {
        pandingCCpaymentBills = new ArrayList<>();
        String jpql;
        Map temMap = new HashMap();

        jpql = "select new com.divudi.core.light.common.BillLight(bill.id, bill.deptId, bill.referenceNumber, bill.createdAt, bill.patient.person.name,  bill.totalCenterFee, bill.totalHospitalFee ) "
                + " from Bill bill "
                + " where bill.collectingCentre=:cc "
                + " and bill.createdAt between :fromDate and :toDate "
                + " and bill.paid =:paid"
                + " and bill.retired=false ";

        jpql += " order by bill.createdAt asc ";
        temMap.put("cc", currentCollectingCentre);
        temMap.put("fromDate", fromDate);
        temMap.put("paid", false);
        temMap.put("toDate", toDate);

        pandingCCpaymentBills = billFacade.findLightsByJpql(jpql, temMap, TemporalType.TIMESTAMP);

        totalHospitalAmount = 0.0;
        totalCCAmount = 0.0;

        for (BillLight bl : pandingCCpaymentBills) {
            if (bl.getReferenceNumber() == null || bl.getReferenceNumber().isEmpty()) {

                Map map = new HashMap();
                String newJpql = "select new com.divudi.core.light.common.BillLight(bill.id, bill.deptId, bill.referenceNumber, bill.createdAt, bill.patient.person.name,  bill.totalCenterFee, bill.totalHospitalFee ) "
                        + " from Bill bill "
                        + " where bill.collectingCentre=:cc "
                        + " and bill.cancelledBill.id=:canBillId"
                        + " and bill.retired=false ";

                jpql += " order by bill.createdAt asc ";
                map.put("cc", currentCollectingCentre);
                map.put("canBillId", bl.getId());

                List<BillLight> bills = billFacade.findLightsByJpql(newJpql, map, TemporalType.TIMESTAMP);

                if (bills.isEmpty()) {
                    Bill bill = billFacade.find(bl.getId());
                    Bill originalBill = bill.getBilledBill();

                    if (originalBill != null) {

                        bl.setReferenceNumber(originalBill.getReferenceNumber());
                        bl.setCcTotal(-originalBill.getTotalCenterFee());
                        bl.setHospitalTotal(-originalBill.getTotalHospitalFee());
                    }

                } else {
                    BillLight bill = bills.get(0);
                    bl.setReferenceNumber(bill.getReferenceNumber());
                    bl.setCcTotal(-bill.getCcTotal());
                    bl.setHospitalTotal(-bill.getHospitalTotal());

                }

            }
            totalHospitalAmount += bl.getHospitalTotal();
            totalCCAmount += bl.getCcTotal();
        }
    }

    public void calculateTotalOfPaymentReceive() {

        String jpql;
        Map<String, Object> temMap = new HashMap<>();

        jpql = "SELECT SUM(b.netTotal) "
                + "FROM Bill b "
                + "WHERE b.billTypeAtomic = :atomic "
                + "AND b.fromInstitution = :cc "
                + "AND b.createdAt BETWEEN :fromDate AND :toDate "
                + "AND b.cancelled = FALSE "
                + "AND b.retired = FALSE";

        temMap.put("atomic", BillTypeAtomic.CC_PAYMENT_RECEIVED_BILL);
        temMap.put("cc", currentCollectingCentre);
        temMap.put("fromDate", fromDate);
        temMap.put("toDate", toDate);

        totalCCReceiveAmount = billFacade.findDoubleByJpql(jpql, temMap, TemporalType.TIMESTAMP);

    }

    public void performCalculations() {
        if (selectedCCpaymentBills == null) {
            payingTotalCCAmount = 0.0;
        } else {
            double totalCClAmount = 0.0;

            for (BillLight bl : selectedCCpaymentBills) {
                totalCClAmount += bl.getCcTotal();
            }
            payingTotalCCAmount = totalCClAmount;
        }
    }

    public void settlePaymentBill() {
        if (ccPaymentSettlingStarted) {
            JsfUtil.addErrorMessage("Already Started Process");
            return;
        }

        ccPaymentSettlingStarted = true;

        if (paymentMethod == null) {
            ccPaymentSettlingStarted = false;
            JsfUtil.addErrorMessage("Select PaymentMethod");
            return;
        }
        if (selectedCCpaymentBills == null || selectedCCpaymentBills.isEmpty()) {
            ccPaymentSettlingStarted = false;
            JsfUtil.addErrorMessage("No Selected Bills");
            return;
        }

        currentPaymentBill = saveBill();
        createPayment(currentPaymentBill, paymentMethod);

        for (BillLight b : selectedCCpaymentBills) {
            Bill bill = billFacade.find(b.getId());
            saveBillItemForPaymentBill(bill, currentPaymentBill);
        }

        // Update CC Balance
        agentAndCcApplicationController.updateCcBalance(
                currentCollectingCentre,
                0.0,
                currentPaymentBill.getNetTotal(),
                0.0,
                currentPaymentBill.getNetTotal(),
                HistoryType.RepaymentToCollectingCentre,
                currentPaymentBill);

        // Update Drower
        ccPaymentSettlingStarted = false;
        printPriview = true;

    }

    public Bill saveBill() {
        Bill ccAgentPaymentBill = new Bill();

        ccAgentPaymentBill.setCreater(sessionController.getLoggedUser());
        ccAgentPaymentBill.setCreatedAt(new Date());
        ccAgentPaymentBill.setInstitution(sessionController.getInstitution());
        ccAgentPaymentBill.setDepartment(sessionController.getDepartment());
        ccAgentPaymentBill.setToInstitution(currentCollectingCentre);
        ccAgentPaymentBill.setBillType(BillType.CollectingCentreAgentPayment);
        ccAgentPaymentBill.setBillDate(new Date());
        ccAgentPaymentBill.setBillTime(new Date());

        ccAgentPaymentBill.setBillTypeAtomic(BillTypeAtomic.CC_AGENT_PAYMENT);
        ccAgentPaymentBill.setNetTotal(payingBalanceAcodingToCCBalabce);
        ccAgentPaymentBill.setTotal(payingBalanceAcodingToCCBalabce);
        ccAgentPaymentBill.setPaidAmount(payingBalanceAcodingToCCBalabce);

        ccAgentPaymentBill.setPaymentMethod(paymentMethod);
        String billNumber = billNumberGenerator.departmentBillNumberGeneratorYearly(sessionController.getDepartment(), BillTypeAtomic.CC_AGENT_PAYMENT);

        ccAgentPaymentBill.setDeptId(billNumber);
        ccAgentPaymentBill.setInsId(billNumber);

        if (ccAgentPaymentBill.getId() == null) {
            billFacade.create(ccAgentPaymentBill);
        } else {
            billFacade.edit(ccAgentPaymentBill);
        }

        return ccAgentPaymentBill;
    }

    public Payment createPayment(Bill bill, PaymentMethod pm) {
        Payment p = new Payment();
        p.setBill(bill);
        setPaymentMethodData(p, pm);
        return p;
    }

    public void setPaymentMethodData(Payment p, PaymentMethod pm) {
        p.setInstitution(sessionController.getInstitution());
        p.setDepartment(sessionController.getDepartment());
        p.setCreatedAt(new Date());
        p.setCreater(sessionController.getLoggedUser());
        p.setPaymentMethod(pm);

        p.setPaidValue(p.getBill().getNetTotal());

        if (p.getId() == null) {
            paymentFacade.create(p);
        }

    }

    private void saveBillItemForPaymentBill(Bill ccBill, Bill originalBill) {
        BillItem paymentBillItem = new BillItem();
        paymentBillItem.setReferenceBill(ccBill);
        paymentBillItem.setBill(originalBill);
        paymentBillItem.setCreatedAt(new Date());
        paymentBillItem.setCreater(sessionController.getLoggedUser());
        paymentBillItem.setDiscount(0.0);
        paymentBillItem.setGrossValue(ccBill.getTotalCenterFee());
        paymentBillItem.setNetValue(ccBill.getTotalCenterFee());
        billItemFacade.create(paymentBillItem);

        ccBill.setPaid(true);
        ccBill.setPaidAmount(ccBill.getTotalCenterFee());
        ccBill.setPaidAt(new Date());
        ccBill.setPaidBill(originalBill);
        billFacade.edit(ccBill);

        originalBill.getBillItems().add(paymentBillItem);
    }

    public void findCollectingCentrePaymentBills() {
        paymentBills = new ArrayList<>();

        String jpql;
        Map temMap = new HashMap();

        jpql = "select b from Bill b "
                + " where b.billTypeAtomic =:atomic "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (getBillNumber() != null && !getBillNumber().trim().equals("")) {
            jpql += " and b.deptId like :billNo ";
            temMap.put("billNo", "%" + getBillNumber().trim().toUpperCase() + "%");
        }

        if (getCurrentCollectingCentre() != null) {
            jpql += " and  ((b.toInstitution) =:toIns )";
            temMap.put("toIns", getCurrentCollectingCentre());
        }

        jpql += " order by b.createdAt desc  ";

        temMap.put("atomic", BillTypeAtomic.CC_AGENT_PAYMENT);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        paymentBills = billFacade.findByJpql(jpql, temMap, TemporalType.TIMESTAMP);

    }

    public void exportSelectedBillsToExcel() throws IOException {
        if (selectedCCpaymentBills == null || selectedCCpaymentBills.isEmpty()) {
            JsfUtil.addErrorMessage("No Selected Bills");
            return;
        }

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=CC_Payment_Bills.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Selected Bills");
            int rowIndex = 0;

            // Create styles
            XSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");

            XSSFCellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);

            XSSFCellStyle amountStyle = workbook.createCellStyle();

            XSSFCellStyle mergedStyle = workbook.createCellStyle();
            mergedStyle.cloneStyleFrom(amountStyle);
            mergedStyle.setFont(boldFont);

            // Create header row
            Row headerRow = sheet.createRow(rowIndex++);
            String[] headers = {"Bill No", "Reference Number", "Bill At", "Patient", "Hos. Fee", "CC Fee"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(boldStyle);
            }

            // Add data rows
            for (BillLight bl : selectedCCpaymentBills) {
                Row row = sheet.createRow(rowIndex++);

                // String values
                row.createCell(0).setCellValue(defaultIfNullOrEmpty(bl.getBillNo(), ""));
                row.createCell(1).setCellValue(defaultIfNullOrEmpty(bl.getReferenceNumber(), ""));

                // Date value - handle potential null
                Cell dateCell = row.createCell(2);
                dateCell.setCellValue(sdf.format(bl.getBillDate()));

                row.createCell(3).setCellValue(defaultIfNullOrEmpty(bl.getPatientName(), ""));

                // Numeric values with formatting
                Cell hospitalCell = row.createCell(4);
                hospitalCell.setCellValue(bl.getHospitalTotal());
                hospitalCell.setCellStyle(amountStyle);

                Cell ccCell = row.createCell(5);
                ccCell.setCellValue(bl.getCcTotal());
                ccCell.setCellStyle(amountStyle);

            }

            // Create footer row with totals
            Row footerRow = sheet.createRow(rowIndex++);

            Cell totalLabelCell = footerRow.createCell(0);
            totalLabelCell.setCellValue("Total");
            totalLabelCell.setCellStyle(boldStyle);

            // Empty cells for spacing
            footerRow.createCell(1).setCellValue("");
            footerRow.createCell(2).setCellValue("");
            footerRow.createCell(3).setCellValue("");

            Cell totalHospitalCell = footerRow.createCell(4);
            totalHospitalCell.setCellValue(getTotalHospitalAmount());
            totalHospitalCell.setCellStyle(mergedStyle); // Bold + amount formatting

            Cell totalCCCell = footerRow.createCell(5);
            totalCCCell.setCellValue(getTotalCCAmount());
            totalCCCell.setCellStyle(mergedStyle); // Bold + amount formatting

            // Auto-size columns for better appearance
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the workbook to the output stream
            workbook.write(out);
            out.flush();

            // Complete the response
            context.responseComplete();

        } catch (Exception e) {
            System.err.println("Error exporting to Excel: " + e.getMessage());
        }
    }

    private String defaultIfNullOrEmpty(String value, String defaultValue) {
        return (value == null || value.trim().isEmpty()) ? defaultValue : value;
    }

    public void cancelPaymentBill() {
        if (ccPaymentSettlingStarted) {
            JsfUtil.addErrorMessage("Already Started Process");
            return;
        }

        ccPaymentSettlingStarted = true;

        if (currentPaymentBill == null) {
            ccPaymentSettlingStarted = false;
            JsfUtil.addErrorMessage("Payment Bill is Missing");
            return;
        }
        if (currentPaymentBill.isCancelled()) {
            ccPaymentSettlingStarted = false;
            JsfUtil.addErrorMessage("Bill already cancelled.");
            return;
        }

        if (comment == null || comment.isEmpty()) {
            ccPaymentSettlingStarted = false;
            JsfUtil.addErrorMessage("Comment is Missing");
            return;
        }
        Bill cancelBill = saveCancelBill();

        createPayment(cancelBill, cancelBill.getPaymentMethod());

        for (BillItem bi : currentPaymentBill.getBillItems()) {

            BillItem reversal = new BillItem();
            reversal.setReferenceBill(bi.getReferenceBill());
            reversal.setBill(cancelBill);
            reversal.setCreatedAt(new Date());
            reversal.setCreater(sessionController.getLoggedUser());
            reversal.setDiscount(0.0);
            reversal.setGrossValue(0 - bi.getGrossValue());
            reversal.setNetValue(0 - bi.getNetValue());
            billItemFacade.create(reversal);

            Bill ref = bi.getReferenceBill();
            ref.setPaid(false);
            ref.setPaidAmount(0.0);
            ref.setPaidAt(null);
            ref.setPaidBill(null);
            billFacade.edit(ref);
            
        }

//        // Update CC Balance
        agentAndCcApplicationController.updateCcBalance(
                cancelBill.getToInstitution(),
                0.0,
                cancelBill.getNetTotal(),
                0.0,
                cancelBill.getNetTotal(),
                HistoryType.RepaymentToCollectingCentreCancel,
                cancelBill);

        ccPaymentSettlingStarted = false;
        setCurrentPaymentBill(cancelBill);
        printPriview = true;

    }

    public Bill saveCancelBill() {
        Bill ccAgentPaymentCancelBill = new Bill();

        ccAgentPaymentCancelBill.copy(currentPaymentBill);
        ccAgentPaymentCancelBill.copyValue(currentPaymentBill);
        ccAgentPaymentCancelBill.setCreater(sessionController.getLoggedUser());
        ccAgentPaymentCancelBill.setCreatedAt(new Date());
        ccAgentPaymentCancelBill.setBillType(BillType.CollectingCentreAgentPaymentCancel);
        ccAgentPaymentCancelBill.setFromInstitution(currentPaymentBill.getToInstitution());
        ccAgentPaymentCancelBill.setBillDate(new Date());
        ccAgentPaymentCancelBill.setBillTime(new Date());
        ccAgentPaymentCancelBill.setComments(comment);

        ccAgentPaymentCancelBill.setBillTypeAtomic(BillTypeAtomic.CC_AGENT_PAYMENT_CANCELLATION);

        String billNumber = billNumberGenerator.departmentBillNumberGeneratorYearly(sessionController.getDepartment(), BillTypeAtomic.CC_AGENT_PAYMENT_CANCELLATION);

        ccAgentPaymentCancelBill.setDeptId(billNumber);
        ccAgentPaymentCancelBill.setInsId(billNumber);

        ccAgentPaymentCancelBill.invertValueOfThisBill();

        if (ccAgentPaymentCancelBill.getId() == null) {
            billFacade.create(ccAgentPaymentCancelBill);
        } else {
            billFacade.edit(ccAgentPaymentCancelBill);
        }

        currentPaymentBill.setCancelled(true);
        currentPaymentBill.setCancelledBill(ccAgentPaymentCancelBill);
        billFacade.edit(currentPaymentBill);

        return ccAgentPaymentCancelBill;
    }

    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Getter & Setter">
    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonFunctions.getStartOfMonth();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = CommonFunctions.getEndOfMonth();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public boolean isCcPaymentSettlingStarted() {
        return ccPaymentSettlingStarted;
    }

    public void setCcPaymentSettlingStarted(boolean ccPaymentSettlingStarted) {
        this.ccPaymentSettlingStarted = ccPaymentSettlingStarted;
    }

    public Institution getCurrentCollectingCentre() {
        return currentCollectingCentre;
    }

    public void setCurrentCollectingCentre(Institution currentCollectingCentre) {
        this.currentCollectingCentre = currentCollectingCentre;
    }

    public List<BillLight> getPandingCCpaymentBills() {
        return pandingCCpaymentBills;
    }

    public void setPandingCCpaymentBills(List<BillLight> pandingCCpaymentBills) {
        this.pandingCCpaymentBills = pandingCCpaymentBills;
    }

    public List<BillLight> getSelectedCCpaymentBills() {
        return selectedCCpaymentBills;
    }

    public void setSelectedCCpaymentBills(List<BillLight> selectedCCpaymentBills) {
        this.selectedCCpaymentBills = selectedCCpaymentBills;
    }

    public double getTotalCCAmount() {
        return totalCCAmount;
    }

    public void setTotalCCAmount(double totalCCAmount) {
        this.totalCCAmount = totalCCAmount;
    }

    public double getTotalHospitalAmount() {
        return totalHospitalAmount;
    }

    public void setTotalHospitalAmount(double totalHospitalAmount) {
        this.totalHospitalAmount = totalHospitalAmount;
    }

    public double getPayingTotalCCAmount() {
        return payingTotalCCAmount;
    }

    public void setPayingTotalCCAmount(double payingTotalCCAmount) {
        this.payingTotalCCAmount = payingTotalCCAmount;
    }

    public double getTotalCCReceiveAmount() {
        return totalCCReceiveAmount;
    }

    public void setTotalCCReceiveAmount(double totalCCReceiveAmount) {
        this.totalCCReceiveAmount = totalCCReceiveAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isPrintPriview() {
        return printPriview;
    }

    public void setPrintPriview(boolean printPriview) {
        this.printPriview = printPriview;
    }

    public double getStartingBalanseInCC() {
        return startingBalanseInCC;
    }

    public void setStartingBalanseInCC(double startingBalanseInCC) {
        this.startingBalanseInCC = startingBalanseInCC;
    }

    public double getFinalEndingBalanseInCC() {
        return finalEndingBalanseInCC;
    }

    public void setFinalEndingBalanseInCC(double finalEndingBalanseInCC) {
        this.finalEndingBalanseInCC = finalEndingBalanseInCC;
    }

    public double getPayingBalanceAcodingToCCBalabce() {
        return payingBalanceAcodingToCCBalabce;
    }

    public void setPayingBalanceAcodingToCCBalabce(double payingBalanceAcodingToCCBalabce) {
        this.payingBalanceAcodingToCCBalabce = payingBalanceAcodingToCCBalabce;
    }

    public Bill getCurrentPaymentBill() {
        return currentPaymentBill;
    }

    public void setCurrentPaymentBill(Bill currentPaymentBill) {
        this.currentPaymentBill = currentPaymentBill;
    }

    public List<Bill> getPaymentBills() {
        return paymentBills;
    }

    public void setPaymentBills(List<Bill> paymentBills) {
        this.paymentBills = paymentBills;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
// </editor-fold>
}
