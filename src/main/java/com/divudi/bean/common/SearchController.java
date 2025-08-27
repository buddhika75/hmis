package com.divudi.bean.common;

// <editor-fold defaultstate="collapsed" desc="Template">
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Imports">
import com.divudi.bean.cashTransaction.CashBookEntryController;
import com.divudi.bean.cashTransaction.DrawerController;
import com.divudi.bean.cashTransaction.DrawerEntryController;
import com.divudi.bean.channel.ChannelSearchController;
import com.divudi.bean.channel.analytics.ReportTemplateController;
import com.divudi.bean.pharmacy.PharmacyPreSettleController;
import com.divudi.bean.pharmacy.PharmacySaleBhtController;
import com.divudi.core.data.BillNumberSuffix;
import com.divudi.core.data.BillType;
import com.divudi.core.data.DepartmentType;
import com.divudi.core.data.FeeType;
import com.divudi.core.data.InstitutionType;
import com.divudi.core.data.PaymentMethod;
import com.divudi.core.data.dataStructure.SearchKeyword;
import com.divudi.core.data.hr.ReportKeyWord;

import com.divudi.core.data.reports.FinancialReport;
import com.divudi.core.data.reports.CashierReports;
import com.divudi.core.data.reports.ProfessionalPaymentReport;
import com.divudi.ejb.PharmacyBean;
import com.divudi.core.entity.AuditEvent;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillFee;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.BillSession;
import com.divudi.core.entity.BilledBill;
import com.divudi.core.entity.CancelledBill;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.Doctor;
import com.divudi.core.entity.Institution;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.Patient;
import com.divudi.core.entity.PatientEncounter;
import com.divudi.core.entity.PreBill;
import com.divudi.core.entity.RefundBill;
import com.divudi.core.entity.ServiceSession;
import com.divudi.core.entity.Speciality;
import com.divudi.core.entity.Staff;
import com.divudi.core.entity.Token;
import com.divudi.core.entity.inward.Admission;
import com.divudi.core.entity.lab.PatientInvestigation;
import com.divudi.core.entity.lab.PatientReport;
import com.divudi.core.entity.pharmacy.Stock;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.BillFeeFacade;
import com.divudi.core.facade.BillItemFacade;
import com.divudi.core.facade.BillSessionFacade;
import com.divudi.core.facade.PatientFacade;
import com.divudi.core.facade.PatientInvestigationFacade;
import com.divudi.core.facade.PatientReportFacade;
import com.divudi.core.facade.StockFacade;
import com.divudi.core.util.JsfUtil;
import com.divudi.bean.opd.OpdBillController;
import com.divudi.bean.pharmacy.PharmacyBillSearch;
import com.divudi.core.data.BillCategory;
import com.divudi.core.data.BillClassType;

import com.divudi.core.data.BillFinanceType;
import com.divudi.core.data.BillTypeAtomic;

import com.divudi.core.data.PaymentCategory;
import com.divudi.core.data.PaymentStatus;
import com.divudi.core.data.PaymentType;
import com.divudi.core.data.ReportTemplateRow;
import com.divudi.core.data.ReportTemplateRowBundle;
import com.divudi.core.data.ServiceType;
import com.divudi.core.data.TokenType;
import com.divudi.core.data.analytics.ReportTemplateType;
import com.divudi.core.data.dto.OpdSaleSummaryDTO;
import com.divudi.core.data.dto.PharmacyItemPurchaseDTO;
import com.divudi.core.data.dto.PharmacyTransferRequestIssueDTO;
import com.divudi.core.data.dto.PharmacyTransferRequestListDTO;
import com.divudi.core.entity.AgentHistory;
import com.divudi.core.entity.Category;
import com.divudi.core.entity.Payment;
import com.divudi.core.entity.WebUser;
import com.divudi.core.entity.cashTransaction.CashBookEntry;
import com.divudi.core.entity.cashTransaction.Drawer;
import com.divudi.core.entity.lab.Investigation;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.facade.AgentHistoryFacade;
import com.divudi.core.facade.DrawerFacade;
import com.divudi.core.facade.PaymentFacade;
import com.divudi.core.facade.PharmaceuticalBillItemFacade;
import com.divudi.core.facade.TokenFacade;
import com.divudi.core.util.CommonFunctions;
import com.divudi.core.light.common.BillLight;
import com.divudi.core.light.common.BillSummaryRow;
import com.divudi.service.BillService;
import com.divudi.service.ChannelService;
import com.divudi.service.PatientInvestigationService;

import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TemporalType;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.model.file.UploadedFile;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;

import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// </editor-fold>

/**
 * @author Dr M H B Ariyaratne
 */
@Named
@SessionScoped
public class SearchController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    
    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private BillFacade billFacade;
    @EJB
    private AgentHistoryFacade agentHistoryFacade;
    @EJB
    private PaymentFacade paymentFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private PatientInvestigationFacade patientInvestigationFacade;
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private BillSessionFacade billSessionFacade;
    @EJB
    private StockFacade stockFacade;
    @EJB
    private PatientReportFacade patientReportFacade;
    @EJB
    private PatientFacade patientFacade;
    @EJB
    private TokenFacade tokenFacade;
    @EJB
    private DrawerFacade drawerFacade;
    @EJB
    private BillService billService;
    @EJB
    private PatientInvestigationService patientInvestigationService;
    @EJB
    private ReportTimerController reportTimerController;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    private BillBeanController billBean;
    @Inject
    private BillController billController;
    @Inject
    private SessionController sessionController;
    @Inject
    private TransferController transferController;
    @Inject
    private PharmacySaleBhtController pharmacySaleBhtController;
    @Inject
    private AuditEventApplicationController auditEventApplicationController;
    @Inject
    private OpdPreSettleController opdPreSettleController;
    @Inject
    private PharmacyPreSettleController pharmacyPreSettleController;
    @Inject
    private DepartmentController departmentController;
    @Inject
    private BillSearch billSearch;
    @Inject
    private PharmacyBillSearch pharmacyBillSearch;
    @Inject
    private OpdBillController opdBillController;
    @Inject
    private ConfigOptionApplicationController configOptionApplicationController;
    @Inject
    private ChannelSearchController channelSearchController;
    @Inject
    private ReportTemplateController reportTemplateController;
    @Inject
    private CashBookEntryController cashBookEntryController;
    @Inject
    private ExcelController excelController;
    @Inject
    private PdfController pdfController;
    @Inject
    private DrawerEntryController drawerEntryController;
    @Inject
    private DrawerController drawerController;
    @Inject
    private EnumController enumController;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Class Variables">
    private String visitType;
    private String methodType;

    private boolean printPreview = false;

    private Category category;
    private ReportTemplateType reportTemplateType;
    private SearchKeyword searchKeyword;
    private Date fromDate;
    private Date toDate;
    private Long startBillId;
    private Long endBillId;
    private WebUser webUser;
    private String backLink;
    private int maxResult = 50;
    private BillType billType;
    private BillTypeAtomic billTypeAtomic;
    private PaymentMethod paymentMethod;
    private List<PaymentMethod> paymentMethods;
    private List<Bill> bills;
    private List<Bill> filteredBills;
    // DTO list for pharmacy transfer requests
    private List<PharmacyTransferRequestListDTO> transferRequestDtos;
    // DTO lists for disposal issue search results
    private List<PharmacyItemPurchaseDTO> disposalIssueBillDtos;
    private List<PharmacyItemPurchaseDTO> disposalIssueBillItemDtos;
    private List<Payment> payments;
    private List<BillLight> billLights;
    private List<BillSummaryRow> billSummaryRows;
    private List<Bill> selectedBills;
    private List<Bill> aceptPaymentBills;
    private List<BillFee> billFees;
    private List<BillFee> billFeesDone;
    private List<BillItem> billItems;
    private List<PatientInvestigation> patientInvestigations;
    private List<PharmaceuticalBillItem> pharmaceuticalBillItems;
    private List<PatientReport> patientReports;
    private List<PatientInvestigation> patientInvestigationsSigle;
    private BillClassType billClassType;
    private String selectedOpdPackageBillSelector;
    private List<String> OpdPackageBillSelector;
    private ReportTemplateRow selectedChannelBookingBillRow;
    // Selected request id used when working with DTO-based tables
    private Long selectedRequestId;
    private Bill selectedRequest;
    String settledBillType;

    public String getSettledBillType() {
        return settledBillType;
    }

    public void setSettledBillType(String settledBillType) {
        this.settledBillType = settledBillType;
    }

    public ReportTemplateRow getSelectedChannelBookingBillRow() {
        return selectedChannelBookingBillRow;
    }

    public void setSelectedChannelBookingBillRow(ReportTemplateRow selectedChannelBookingBillRow) {
        this.selectedChannelBookingBillRow = selectedChannelBookingBillRow;
    }

    public void handleChannelBillRowClick() {
        if (selectedChannelBookingBillRow == null || selectedChannelBookingBillRow.getBill() == null) {
            return;
        }

        if (selectedChannelBookingBillRow.getBill() instanceof CancelledBill) {
            for (ReportTemplateRow r : bundle.getReportTemplateRows()) {
                if (r.getBill() != null
                        && r.getBill().getCancelledBill() != null
                        && r.getBill().getCancelledBill().equals(selectedChannelBookingBillRow.getBill())) {
                    this.selectedChannelBookingBillRow = r;
                    break;
                }
            }
        } else if (selectedChannelBookingBillRow.getBill() instanceof RefundBill) {
            for (ReportTemplateRow r : bundle.getReportTemplateRows()) {
                if (r.getBill() != null && r.getBill().getRefundedBill() != null && r.getBill().getRefundedBill().equals(selectedChannelBookingBillRow.getBill())) {
                    this.selectedChannelBookingBillRow = r;
                    break;
                }
            }
        }
    }

    private StreamedContent downloadingExcel;

    private Bill cancellingIssueBill;
    private Bill bill;
    private Speciality speciality;
    private PatientEncounter patientEncounter;
    private Staff staff;
    private Item item;
    private double dueTotal;
    private double doneTotal;
    private double netTotal;
    private double totalBillCount;
    private double grossTotal;
    private double discount;
    private ServiceSession selectedServiceSession;
    private Staff currentStaff;
    private String mrnNo;
    private List<BillItem> billItem;
    private List<PatientInvestigation> userPatientInvestigations;

    private String menuBarSearchText;
    private String smsText;
    private String uniqueSmsText;
    private boolean channelingPanelVisible;
    private boolean pharmacyPanelVisible;
    private boolean opdPanelVisible;
    private boolean inwardPanelVisible;
    private boolean labPanelVisile;
    private boolean patientPanelVisible;
    private ReportKeyWord reportKeyWord;

    private List<Bill> channellingBills;
    private List<BillSession> billSessions;
    private List<Bill> opdBills;
    private List<Bill> pharmacyBills;
    private List<Admission> admissions;
    private List<PatientInvestigation> pis;
    private List<Patient> patients;
    private List<String> telephoneNumbers;
    private List<String> selectedTelephoneNumbers;
    private List<PharmacyAdjustmentRow> pharmacyAdjustmentRows;

    private BillSession selectedBillSession;
    private UploadedFile file;
    private Institution creditCompany;

    private Institution otherInstitution;

    private Institution institution;
    private Department department;
    private List<Bill> prescreptionBills;
    private Department fromDepartment;
    private Department toDepartment;
    private Institution fromInstitution;
    private Institution toInstitution;
    private int manageListIndex;
    private Patient patient;
    private Institution dealer;
    private List<Bill> grnBills;
    private Bill currentBill;
    private Long currentBillId;
    private Long barcodeIdLong;
    private Date maxDate;

    private double cashTotal;
    private double cardTotal;
    private double chequeTotal;
    private double slipTotal;
    private double totalOfOtherPayments;
    private double billCount;
    private Token token;
    private int managePaymentIndex = -1;

    private double hosTotal;
    private double staffTotal;
    private double discountTotal;
    private double amountTotal;

    private boolean duplicateBillView;

    private ReportTemplateRowBundle bundle;
    private ReportTemplateRowBundle bundleBillItems;
    private List<OpdSaleSummaryDTO> opdSaleSummaryDtos;

    private List<CashBookEntry> cashBookEntries;
    private Institution site;
    private Institution toSite;
    private List<Drawer> drawerList;
    private Drawer selectedDrawer;
    private int opdAnalyticsIndex;

    private String searchType;
    private String reportType;
    private boolean withProfessionalFee;
    private boolean showLoggedDepartmentOnly;

    private Drawer drawer;

    private Department serviceDepartment;
    private Department billedDepartment;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Navigators">
    public String navigateToBillCountList() {
        fromDate = CommonFunctions.getStartOfMonth(new Date());
        toDate = CommonFunctions.getEndOfMonth(new Date());
        return "/analytics/bill_count_list?faces-redirect=true";
    }

    public String navigateToReturnReceivedGoods() {
        searchKeyword = new SearchKeyword();
        bills = new ArrayList<>();
        return "/pharmacy/pharmacy_grn_list_for_return?faces-redirect=true";
    }

    public String navigateToMyDepartmentAllCashierSummary() {
        department = sessionController.getDepartment();
        institution = sessionController.getInstitution();
        site = sessionController.getDepartment().getSite();
        webUser = null;
        return "/cashier/my_department_all_cashier_summary?faces-redirect=true";
    }

    public String navigateToUserDrawerDashboard() {
        Drawer d = drawerController.getUsersDrawer(drawer.getDrawerUser());
        drawerController.setCurrent(d);
        return "/admin/users/drawer?faces-redirect=true";
    }

    public String navigateToDrawerHistory() {
        drawerEntryController.findAllUsersDrawerDetails();
        return "/reports/financialReports/all_users_drawer_history?faces-redirect=true";
    }

    public String navigateToPettyCashBillApprove() {
        createPettyCashToApproveTable();
        return "/petty_cash_bill_to_approve?faces-redirect=true";
    }

    public String navigateToPettyCashCancelBillApprove() {
        createPettyApproveTable();
        return "/petty_cash_cancel_bill_to_approve?faces-redirect=true";
    }

    public String navigateTobill(Bill bill) {
        String navigateTo = "";
        if (bill == null) {
            return "";
        }
        switch (bill.getBillTypeAtomic()) {
            //Opd Bill Navigation
            case OPD_BILL_WITH_PAYMENT:
                billSearch.setBill(bill);
                navigateTo = "/opd/bill_reprint.xhtml";
                break;
            case OPD_BILL_REFUND:
                billSearch.setBill(bill);
                navigateTo = "/opd/bill_reprint.xhtml";
                break;
            case OPD_BILL_CANCELLATION:
                billSearch.setBill(bill);
                navigateTo = "/opd/bill_reprint.xhtml";
                break;
            case OPD_BATCH_BILL_WITH_PAYMENT:
                opdBillController.setBatchBill(bill);
                navigateTo = "/opd/opd_batch_bill_print.xhtml";
                break;
            //Pharmacy Bill Navigation
            case PHARMACY_RETAIL_SALE:
                pharmacyBillSearch.setBill(bill);
                navigateTo = "/pharmacy/pharmacy_reprint_bill_sale.xhtml";
                break;
            case OPD_PROFESSIONAL_PAYMENT_BILL:
            case OPD_PROFESSIONAL_PAYMENT_BILL_RETURN:
            case PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN:
            case PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES:
                billSearch.setBill(bill);
                navigateTo = "/payment_bill_reprint.xhtml?faces-redirect=true";
                break;

            case PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES:
            case PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES_RETURN:
            case PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN:
            case PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION:
            case PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN:
                channelSearchController.setBill(bill);
                navigateTo = "/channel/channel_payment_bill_reprint.xhtml";
                break;
            default:
                return navigateTo;
        }
        return navigateTo;
    }

    public String navigateToAllFinancialTransactionSummary() {
        billSummaryRows = null;
        return "/analytics/all_financial_transaction_summary?faces-redirect=true";
    }

    public String navigateToUserFinancialTransactionSummaryByBill() {
        billSummaryRows = null;
        return "/analytics/user_financial_transaction_summary_by_bill?faces-redirect=true";
    }

    public String navigateToAllFinancialTransactionSummaryCashier() {
        billSummaryRows = null;
        return "/analytics/all_financial_transaction_summary_by_user?faces-redirect=true";
    }

    public String navigateToFinancialTransactionSummaryPaymentMethod() {
        institution = sessionController.getInstitution();
        department = null;
        billSummaryRows = null;
        return "/analytics/financial_transaction_summary_PaymentMethod?faces-redirect=true";
    }

    public String navigateToAddToStockBillList() {
        printPreview = false;
        bills = null;
        return "/pharmacy/pharmacy_add_to_stock_search_bill?faces-redirect=true";
    }

    public String navigateToFinancialTransactionSummaryByUsers() {
        institution = sessionController.getInstitution();
        department = null;
        billSummaryRows = null;
        return "/analytics/financial_transaction_summary_Users?faces-redirect=true";
    }

    public String navigateToFinancialTransactionSummaryByUserPayment() {
        departments = departmentController.getInstitutionDepatrments(sessionController.getInstitution());
        billSummaryRows = null;
        return "/analytics/financial_transaction_summary_Users_PaymentMethod?faces-redirect=true";
    }

    public String navigateToWhtReport() {
        reportType = "individualReceipts";
        return "/reports/financialReports/wht?faces-redirect=true";
    }

    public String navigateToFinancialTransactionSummaryByDepartment() {
        department = sessionController.getDepartment();
        billSummaryRows = null;
        departments = departmentController.getInstitutionDepatrments(sessionController.getInstitution());
        return "/analytics/financial_transaction_summary_Department?faces-redirect=true";
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Functions">

    public void clearBillList() {
        if (bills == null) {
            return;
        } else {
            bills = new ArrayList<>();
        }
    }

    public void findBillcount() {
        if (configOptionApplicationController.getBooleanValueByKey("Calculating bill count using the item in bills", true)) {
            findBillcountUsingBillItem();
        } else {
            findBillcountUsingBills();
        }
    }

    public void findBillcountUsingBills() {
        String sql;
        List<BillTypeAtomic> billTypeAtomic = new ArrayList<>();
        //OPD Types
        billTypeAtomic.add(BillTypeAtomic.OPD_BILL_WITH_PAYMENT);
//        billTypeAtomic.add(BillTypeAtomic.OPD_BILL_CANCELLATION);
//        billTypeAtomic.add(BillTypeAtomic.OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION);
        billTypeAtomic.add(BillTypeAtomic.OPD_BILL_REFUND);
        //Package Types
        billTypeAtomic.add(BillTypeAtomic.PACKAGE_OPD_BILL_WITH_PAYMENT);
//        billTypeAtomic.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION);
//        billTypeAtomic.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION);
        billTypeAtomic.add(BillTypeAtomic.PACKAGE_OPD_BILL_REFUND);
        //CC Types
        billTypeAtomic.add(BillTypeAtomic.CC_BILL);
//        billTypeAtomic.add(BillTypeAtomic.CC_BILL_CANCELLATION);
        billTypeAtomic.add(BillTypeAtomic.CC_BILL_REFUND);
        //Pharmacy Types
        billTypeAtomic.add(BillTypeAtomic.PHARMACY_RETAIL_SALE);
        billTypeAtomic.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_PREBILL_SETTLED_AT_CASHIER);
//        billTypeAtomic.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_CANCELLED);
        billTypeAtomic.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_RETURN_ITEMS_AND_PAYMENTS);
        billTypeAtomic.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_REFUND);

        billTypeAtomic.add(BillTypeAtomic.DIRECT_ISSUE_INWARD_MEDICINE);
//        billTypeAtomic.add(BillTypeAtomic.DIRECT_ISSUE_INWARD_MEDICINE_CANCELLATION);
        billTypeAtomic.add(BillTypeAtomic.DIRECT_ISSUE_INWARD_MEDICINE_RETURN);

        //Pharmacy Types
        billTypeAtomic.add(BillTypeAtomic.INWARD_FINAL_BILL);
        billTypeAtomic.add(BillTypeAtomic.INWARD_SERVICE_BILL);
//        billTypeAtomic.add(BillTypeAtomic.INWARD_SERVICE_BILL_CANCELLATION);
//        billTypeAtomic.add(BillTypeAtomic.INWARD_SERVICE_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION);
        billTypeAtomic.add(BillTypeAtomic.INWARD_SERVICE_BILL_REFUND);

        billTypeAtomic.add(BillTypeAtomic.ISSUE_MEDICINE_ON_REQUEST_INWARD);
        billTypeAtomic.add(BillTypeAtomic.ISSUE_MEDICINE_ON_REQUEST_INWARD_RETURN);

        bundle = new ReportTemplateRowBundle();

        Long mainRowCount = 1L;

        for (BillTypeAtomic atomic : billTypeAtomic) {
            Map temMap = new HashMap();
            sql = "select new com.divudi.core.light.common.BillLight( b.billTypeAtomic, count(b) )"
                    + " from Bill b where "
                    + " b.createdAt between :fromDate and :toDate "
                    + " and b.cancelled=false "
                    + " and b.billTypeAtomic =:billTypes ";

            if (institution != null) {
                sql += " and b.institution =:ins";
                temMap.put("ins", getInstitution());
            }
            if (site != null) {
                sql += " and b.department.site =:site";
                temMap.put("site", getSite());
            }
            if (department != null) {
                sql += " and b.department =:dept";
                temMap.put("dept", getDepartment());
            }
            if (toInstitution != null) {
                sql += " and b.toInstitution =:toIns";
                temMap.put("toIns", getToInstitution());
            }
            if (toSite != null) {
                sql += " and b.toDepartment.site =:toSite";
                temMap.put("toSite", getToSite());
            }
            if (toDepartment != null) {
                sql += " and b.toDepartment =:toDept";
                temMap.put("toDept", getToDepartment());
            }

            sql += " group by b.billTypeAtomic";
            temMap.put("toDate", getToDate());
            temMap.put("fromDate", getFromDate());
            temMap.put("billTypes", atomic);
            List<BillLight> mainBills = getBillFacade().findLightsByJpql(sql, temMap, TemporalType.TIMESTAMP);

            if (mainBills == null) {
                continue;
            }

            for (BillLight mainBill : mainBills) {
                ReportTemplateRow mainBillRow = new ReportTemplateRow();
                mainBillRow.setRowType("MainRow");
                mainBillRow.setCounter(mainRowCount);
                mainBillRow.setBillTypeAtomic(mainBill.getBillTypeAtomic());
                mainBillRow.setCategoryCount(mainBill.getCount());
                bundle.getReportTemplateRows().add(mainBillRow);
                mainRowCount++;
            }

            Map params = new HashMap();

            String jpql = "select new com.divudi.core.light.common.BillLight( b.toDepartment, count(b) )"
                    + " from Bill b where "
                    + " b.createdAt between :fromDate and :toDate "
                    + " and b.billTypeAtomic =:billTypes "
                    + " and b.cancelled=false "
                    + " and b.toDepartment is not null";
            if (institution != null) {
                jpql += " and b.institution =:ins";
                params.put("ins", getInstitution());
            }
            if (site != null) {
                jpql += " and b.department.site =:site";
                params.put("site", getSite());
            }
            if (department != null) {
                jpql += " and b.department =:dept";
                params.put("dept", getDepartment());
            }
            if (toInstitution != null) {
                jpql += " and b.toInstitution =:toIns";
                params.put("toIns", getToInstitution());
            }
            if (toSite != null) {
                jpql += " and b.toDepartment.site =:toSite";
                params.put("toSite", getToSite());
            }
            if (toDepartment != null) {
                jpql += " and b.toDepartment =:toDept";
                params.put("toDept", getToDepartment());
            }

            jpql += " group by b.toDepartment";
            params.put("toDate", getToDate());
            params.put("fromDate", getFromDate());
            params.put("billTypes", atomic);
            List<BillLight> subBills = getBillFacade().findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);

            if (subBills == null) {
                continue;
            }

            Long subRowCount = 1L;
            for (BillLight subBill : subBills) {
                ReportTemplateRow subBillsrow = new ReportTemplateRow();
                subBillsrow.setRowType("SubRow");
                subBillsrow.setCounter(subRowCount);
                subBillsrow.setToDepartment(subBill.getToDpartment());
                subBillsrow.setCategoryCount(subBill.getCount());
                bundle.getReportTemplateRows().add(subBillsrow);
                subRowCount++;
            }

        }
    }

    public void findBillcountUsingBillItem() {
        String sql;
        List<BillTypeAtomic> billTypeAtomic = new ArrayList<>();
        //OPD Types
        billTypeAtomic.add(BillTypeAtomic.OPD_BILL_WITH_PAYMENT);
//        billTypeAtomic.add(BillTypeAtomic.OPD_BILL_CANCELLATION);
//        billTypeAtomic.add(BillTypeAtomic.OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION);
        billTypeAtomic.add(BillTypeAtomic.OPD_BILL_REFUND);
        //Package Types
        billTypeAtomic.add(BillTypeAtomic.PACKAGE_OPD_BILL_WITH_PAYMENT);
//        billTypeAtomic.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION);
//        billTypeAtomic.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION);
        billTypeAtomic.add(BillTypeAtomic.PACKAGE_OPD_BILL_REFUND);
        //CC Types
        billTypeAtomic.add(BillTypeAtomic.CC_BILL);
//        billTypeAtomic.add(BillTypeAtomic.CC_BILL_CANCELLATION);
        billTypeAtomic.add(BillTypeAtomic.CC_BILL_REFUND);
        //Pharmacy Types
        billTypeAtomic.add(BillTypeAtomic.PHARMACY_RETAIL_SALE);
        billTypeAtomic.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_PREBILL_SETTLED_AT_CASHIER);
//        billTypeAtomic.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_CANCELLED);
        billTypeAtomic.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_RETURN_ITEMS_AND_PAYMENTS);
        billTypeAtomic.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_REFUND);

        billTypeAtomic.add(BillTypeAtomic.DIRECT_ISSUE_INWARD_MEDICINE);
//        billTypeAtomic.add(BillTypeAtomic.DIRECT_ISSUE_INWARD_MEDICINE_CANCELLATION);
        billTypeAtomic.add(BillTypeAtomic.DIRECT_ISSUE_INWARD_MEDICINE_RETURN);

        //Pharmacy Types
        billTypeAtomic.add(BillTypeAtomic.INWARD_FINAL_BILL);
        billTypeAtomic.add(BillTypeAtomic.INWARD_SERVICE_BILL);
//        billTypeAtomic.add(BillTypeAtomic.INWARD_SERVICE_BILL_CANCELLATION);
//        billTypeAtomic.add(BillTypeAtomic.INWARD_SERVICE_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION);
        billTypeAtomic.add(BillTypeAtomic.INWARD_SERVICE_BILL_REFUND);

        billTypeAtomic.add(BillTypeAtomic.ISSUE_MEDICINE_ON_REQUEST_INWARD);
        billTypeAtomic.add(BillTypeAtomic.ISSUE_MEDICINE_ON_REQUEST_INWARD_RETURN);

        bundle = new ReportTemplateRowBundle();

        Long mainRowCount = 1L;

        for (BillTypeAtomic atomic : billTypeAtomic) {
            Map temMap = new HashMap();
            sql = "select new com.divudi.core.data.ReportTemplateRow(bi.item.department, b.billTypeAtomic, count(b) )"
                    + " from BillItem bi "
                    + " Join bi.bill b"
                    + " where "
                    + " b.createdAt between :fromDate and :toDate "
                    + " and b.cancelled=false "
                    + " and b.billTypeAtomic =:billTypes ";

            if (institution != null) {
                sql += " and b.institution =:ins";
                temMap.put("ins", institution);
            }
            if (site != null) {
                sql += " and b.department.site =:site";
                temMap.put("site", site);
            }
            if (department != null) {
                sql += " and b.department =:dept";
                temMap.put("dept", department);
            }
            if (toInstitution != null) {
                sql += " and b.toInstitution =:toIns";
                temMap.put("toIns", toInstitution);
            }
            if (toSite != null) {
                sql += " and b.toDepartment.site =:toSite";
                temMap.put("toSite", toSite);
            }
            if (toDepartment != null) {
                sql += " and b.toDepartment =:toDept";
                temMap.put("toDept", toDepartment);
            }

            sql += " group by bi.item.department, b.billTypeAtomic";
            temMap.put("toDate", getToDate());
            temMap.put("fromDate", getFromDate());
            temMap.put("billTypes", atomic);
            List<ReportTemplateRow> mainBills = (List<ReportTemplateRow>) getBillFacade().findLightsByJpql(sql, temMap, TemporalType.TIMESTAMP);

            if (mainBills == null) {
                continue;
            }

            bundle.getReportTemplateRows().addAll(mainBills);
        }
    }

    public Bill searchBillFromTokenId(Long currentTokenId) {
        if (currentTokenId == null) {
            JsfUtil.addErrorMessage("Enter Correct Bill Number !");
            return null; // Return null if the token ID is null
        }
        String sql = "SELECT t.bill FROM Token t "
                + "WHERE t.retired = false "
                + "AND t.id = :tid";
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("tid", currentTokenId);
        return getBillFacade().findFirstByJpql(sql, hm);
    }

    public Token searchTokenFromTokenId(Long currentTokenId) {
        if (currentTokenId == null) {
            JsfUtil.addErrorMessage("Enter Correct Bill Number !");
            return null; // Return null if the token ID is null
        }
        String sql = "SELECT t FROM Token t "
                + "WHERE t.retired = false "
                + "AND t.id = :tid";
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("tid", currentTokenId);
        return tokenFacade.findFirstByJpql(sql, hm);
    }

    public Bill searchBillFromBillId(Long currentTokenId) {
        if (currentTokenId == null) {
            JsfUtil.addErrorMessage("Enter Correct Bill Number !");
            return null; // Return null if the token ID is null
        }
        String sql = " SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = false "
                + "AND b.id = :bid";
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("bid", currentTokenId);
        return getBillFacade().findFirstByJpql(sql, hm);
    }

    public String settleBillByBarcode() {
        currentBill = searchBillFromBillId(barcodeIdLong);
        if (currentBill == null) {
            currentBill = searchBillFromTokenId(barcodeIdLong);
            opdPreSettleController.setToken(searchTokenFromTokenId(barcodeIdLong));
        }
        String action;
        if (currentBill == null) {
            JsfUtil.addErrorMessage("No Bill Found");
            return "";
        }
        if (currentBill.isPaid()) {
            JsfUtil.addErrorMessage("Error : Bill is Already Paid");
            return " ";
        }
        action = toSettle(currentBill);
        return action;

    }

    public String toSettle(Bill preBill) {

        if (preBill == null) {
            JsfUtil.addErrorMessage("No Such Prebill");
            return "";
        }
        String sql = "Select b from "
                + " BilledBill b"
                + " where b.referenceBill=:bil"
                + " and b.retired=false "
                + " and b.cancelled=false ";
        HashMap hm = new HashMap();
        hm.put("bil", preBill);
        Bill b = getBillFacade().findFirstByJpql(sql, hm);

        if (b != null) {
            JsfUtil.addErrorMessage("Allready Paid");
            return "";
        }

        if (preBill.getBillTypeAtomic() != null) {
            BillTypeAtomic bta = preBill.getBillTypeAtomic();
            switch (bta) {
                case OPD_BATCH_BILL_TO_COLLECT_PAYMENT_AT_CASHIER:
                    return opdPreSettleController.toSettle(preBill);
                case PHARMACY_RETAIL_SALE_PRE:
                case PHARMACY_RETAIL_SALE_PRE_TO_SETTLE_AT_CASHIER:
                    setPreBillForPharmecy(preBill);
                    return "/pharmacy/pharmacy_bill_pre_settle?faces-redirect=true";
                default:
                    JsfUtil.addErrorMessage("Other Bill Type Error");
                    return null;
            }
        }
//        if (preBill.getBillType() != null) {
//            BillType btype = preBill.getBillType();
//            switch (btype) {
//                case OpdPreBill:
//                    setPreBillForOpd(preBill);
//
//                case PharmacyPre:
//                    setPreBillForPharmecy(preBill);
//                    return "/pharmacy/pharmacy_bill_pre_settle?faces-redirect=true";
//
////                case OpdBathcBillPre:
////                    opdPreBatchBillSettleController.setPreBill(preBill);
////                    return "/opd/opd_bill_pre_settle?faces-redirect=true";
//                default:
//                    throw new AssertionError();
//            }
//
//        }
        JsfUtil.addErrorMessage("No bill error");
        return null;
    }

    public void setPreBillForOpd(Bill preBill) {
        makeNull();
        opdPreSettleController.setPreBill(preBill);

        opdPreSettleController.toSettle(preBill);

        opdPreSettleController.setBillPreview(false);

    }

    public void setPreBillForPharmecy(Bill preBill) {
        makeNull();
        pharmacyPreSettleController.setPreBill(preBill);

        pharmacyPreSettleController.setBillPreview(false);

    }

    public void createGrnWithDealerTable() {
        Map m = new HashMap();
        String sql = "select b from Bill b where b.retired=false and "
                + " b.billType = :billType and b.institution = :del "
                + "and b.createdAt between :fromDate and :toDate ";

        m.put("billType", BillType.PharmacyGrnBill);
        m.put("del", getDealer());
        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());

        grnBills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);
    }

    public void fillSavedTranserRequestBills() {

        String sql = "Select bill from Bill bill where bill.retired =false "
                + " and bill.billType = :billType "
                + " and bill.institution = :institution "
                + " and bill.fromDepartment = :fromDepartment "
                + " and bill.createdAt between :fromDate and :toDate"
                + " order by bill.createdAt desc";

        Map parametersForSearching = new HashMap();
        parametersForSearching.put("billType", BillType.PharmacyTransferRequest);
        parametersForSearching.put("institution", sessionController.getInstitution());
        parametersForSearching.put("fromDepartment", sessionController.getDepartment());
        parametersForSearching.put("fromDate", getFromDate());
        parametersForSearching.put("toDate", getToDate());

        bills = getBillFacade().findByJpql(sql, parametersForSearching, TemporalType.TIMESTAMP);

    }

    public String navigateToPatientLabReports() {
        fillPatientLabReports(patient);
        return "/lab/patient_lab_reports";
    }

    public String navigateToPatientAcceptPayment() {
        fillPatientPreBills(null, patient, null, true);
        return "/opd/patient_accept_payment";
    }

    public String menuBarSearch() {
        JsfUtil.addSuccessMessage("Sarched From Menubar" + "\n" + menuBarSearchText);
        return "/index";
    }

    public String navigateToSmsList() {
        return "/analytics/sms_list?faces-redirect=true";
    }

    public String navigateToBillList() {
        resetAllFiltersExceptDateRange();
        return "/analytics/bills?faces-redirect=true";
    }

    public String navigateToCancellationBillList() {
        resetAllFiltersExceptDateRange();
        return "/analytics/cancellation_bills?faces-redirect=true";
    }

    public String navigateToReturnBillList() {
        resetAllFiltersExceptDateRange();
        return "/analytics/return_bills?faces-redirect=true";
    }

    public String navigateToPharmaceuticalBillItemList() {
        resetAllFiltersExceptDateRange();
        return "/analytics/pharmaceutical_bill_items?faces-redirect=true";
    }

    public String navigateToPatientInvestigationList() {
        resetAllFiltersExceptDateRange();
        return "/analytics/patient_investigations?faces-redirect=true";
    }

    public String navigateToBillListFromBill(Bill bill) {
        resetAllFilters();
        fromDate = CommonFunctions.getStartOfBeforeDay(bill.getCreatedAt());
        toDate = CommonFunctions.getEndOfDay(bill.getCreatedAt());
        webUser = bill.getCreater();
        institution = bill.getInstitution();
        department = bill.getDepartment();
        if (bill.getDepartment() != null) {
            site = bill.getDepartment().getSite();
        }
        billTypeAtomic = bill.getBillTypeAtomic();
        billType = bill.getBillType();
        listBills();
        return "/analytics/bills?faces-redirect=true";
    }

    public String navigateToBillTypeList() {
        resetAllFiltersExceptDateRange();
        return "/analytics/bill_types?faces-redirect=true";
    }

    public String navigateToUserBillTypeList(WebUser wu) {
        resetAllFiltersExceptDateRange();
        webUser = wu;
        listBillTypes();
        return "/analytics/bill_types?faces-redirect=true";
    }

    public String navigateToUserBillPaymentList(WebUser wu) {
        billSummaryRows = null;
        resetAllFiltersExceptDateRange();
        resetTotals();
        webUser = wu;
        listPayments();
        return "/analytics/payments?faces-redirect=true";
    }

    public String navigateToUserBillList(WebUser wu) {
        resetAllFiltersExceptDateRange();
        webUser = wu;
        listBills();
        return "/analytics/bills?faces-redirect=true";
    }

    public String navigateToBillItemList() {
        resetAllFiltersExceptDateRange();
        return "/analytics/bill_items?faces-redirect=true";
    }

    public String navigateToBillFeeList() {
        resetAllFiltersExceptDateRange();
        return "/analytics/bill_fees?faces-redirect=true";
    }

    public String navigateToBillPaymentList() {
        billSummaryRows = null;
        resetAllFiltersExceptDateRange();
        resetTotals();
        return "/analytics/payments?faces-redirect=true";
    }

    //    public String navigateToBillPaymentList() {
//        resetAllFiltersExceptDateRange();
//        return "/analytics/bill_fees?faces-redirect=true";
//    }
    public String navigateToStaffCreditBillList() {
        bills = null;
        return "/analytics/staff_credit_bill_list?faces-redirect=true";
    }

    public String navigateToGeneralCreditBillList() {
        bills = null;
        return "/analytics/general_credit_bill_list?faces-redirect=true";
    }

    public String navigateToFailedSmsList() {
        return "/analytics/sms_faild?faces-redirect=true";
    }

    public String navigateToOpdSaleReport() {
        return "/opd/analytics/sale_report?faces-redirect=true";
    }

    public String navigateToSendSms() {
        return "/analytics/sms_send?faces-redirect=true";
    }

    public String navigateToListOtherInstitutionBills() {
        bills = null;
        return "/analytics/other_institution_bills?faces-redirect=true";
    }

    public String navigateToAnalytics() {
        bills = null;
        return "/analytics/index?faces-redirect=true";
    }

    public String navigateToProfessionalFees() {
        bundle = new ReportTemplateRowBundle();
        recreateProPayementModel();
        return "/reports/professional_payment_reports/professional_fees_opd?faces-redirect=true";
    }

    public String navigateToProfessionalFeePayments() {
        bundle = new ReportTemplateRowBundle();
        return "/reports/professional_payment_reports/professional_fee_payments_opd?faces-redirect=true";
    }

    public String navigateToProfessionalPayments() {
        resetAllFilters();
        bundle = new ReportTemplateRowBundle();
        return "/reports/professional_payment_reports/professional_payments_opd?faces-redirect=true";
    }

    public String navigateToMyProfessionalPayments() {
        bundle = new ReportTemplateRowBundle();
        webUser = sessionController.getLoggedUser();
        return "/cashier/my_professional_payments?faces-redirect=true";
    }

    public String navigateToOpdBillList() {
        return "/analytics/opd_bill_list?faces-redirect=true";
    }

    public String navigateToOpdBatchBillList() {
        return "/opd/analytics/opd_batch_bill_search?faces-redirect=true";
    }

    public String navigateToOpdBillItemList() {
        return "/opd/analytics/opd_bill_item_list?faces-redirect=true";
    }

    public String navigateToOpdBillItemList(ReportTemplateRow row) {
        institution = row.getInstitution();
        department = row.getDepartment();
        site = row.getSite();
        category = row.getCategory();
        item = row.getItem();
        backLink = "/reports/financialReports/daily_return?faces-redirect=true";
        generateOpdServicesByBillItem();
        return navigateToOpdBillItemList();
    }

    public String navigateToOpdSummaryByItem() {
        bills = null;
        fromDate = null;
        toDate = null;
        institution = null;
        department = null;
        fromInstitution = null;
        fromDepartment = null;
        toInstitution = null;
        toDepartment = null;
        webUser = null;
        creditCompany = null;
        startBillId = null;
        endBillId = null;
        return "/analytics/opd_summary_by_item?faces-redirect=true";
    }

    public String navigateToChannellingPaymentBillList() {
        bills = null;
        return "/analytics/channelling_payment_list?faces-redirect=true";
    }

    public String navigateToChannellingPaymentBillFeeList() {
        billFees = null;
        return "/analytics/channelling_payment_fee_list?faces-redirect=true";
    }

    public String navigateToOpdPaymentBillFeeList() {
        billFees = null;
        return "/analytics/opd_payment_fee_list?faces-redirect=true";
    }

    public String navigateToOpdPaymentBillList() {
        bills = null;
        return "/analytics/opd_payment_list?faces-redirect=true";
    }

    public String navigateToProfessionalPaymentBillList() {
        bills = null;
        return "/analytics/professional_payment_list?faces-redirect=true";
    }

    //    public String navigateToWhtReport(){
//        return "/reports/financialReports/wht?faces-redirect=true";
//    }
    public String toSearchBills() {
        bills = null;
        return "/dataAdmin/search_bill?faces-redirect=true";
    }

    public String toListAllBills() {
        bills = null;
        return "/dataAdmin/list_bills?faces-redirect=true";
    }

    public String toListAllPayments() {
        bills = null;
        return "/dataAdmin/list_payments?faces-redirect=true";
    }

    private void recreateProPayementModel() {
        institution = null;
        site = null;
        department = null;
        category = null;
        item = null;
        mrnNo = null;
        speciality = null;
        staff = null;
    }

    public void listAllBills() {
        String sql;
        Map temMap = new HashMap();
        sql = "select b from Bill b where "
                + " b.createdAt between :fromDate and :toDate "
                + "order by b.createdAt desc ";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    public void retireAlllistedBills() {
        for (Bill b : bills) {
            b.setRetired(true);
            b.setRetiredAt(new Date());
            b.setRetirer(sessionController.getLoggedUser());
            getBillFacade().edit(b);
        }
    }

    public void listAllPayments() {
        String sql;
        Map temMap = new HashMap();
        sql = "select p from Payment"
                + " p where "
                + " p.createdAt between :fromDate and :toDate "
                + " order by p.createdAt desc ";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        payments = paymentFacade.findByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    public String toViewBillSummery() {
        if (bill == null) {
            return "";
        }
        return "/bill_summery?faces-redirect=true";
    }

    public void fillBillSessions() {
        selectedBillSession = null;
        BillType[] billTypes = {BillType.ChannelAgent, BillType.ChannelCash, BillType.ChannelOnCall, BillType.ChannelStaff};
        List<BillType> bts = Arrays.asList(billTypes);
        if (false) {
            BillSession bs = new BillSession();
            bs.getBill().getInsId();
            bs.getBill().getDeptId();
            bs.getBill().getReferenceNumber();
            bs.getReferenceBillSession().getBill().getInsId();
            bs.getReferenceBillSession().getBill().getDeptId();
            bs.getReferenceBillSession().getBill().getReferenceNumber();
            bs.getBill().getReferenceBill().getInsId();
            bs.getBill().getReferenceBill().getDeptId();
            bs.getBill().getReferenceBill().getReferenceNumber();
        }
        String sql = "Select bs From BillSession bs "
                + " where bs.retired=false"
                + " and bs.bill.billType in :bt"
                + " and type(bs.bill)=:class "
                + " and ("
                + "    (bs.bill.insId) like :txt "
                + " or (bs.bill.deptId) like :txt "
                + " or (bs.bill.referenceNumber) like :txt "
                + " or (bs.referenceBillSession.bill.insId) like :txt "
                + " or (bs.referenceBillSession.bill.deptId) like :txt "
                + " or (bs.referenceBillSession.bill.referenceNumber) like :txt "
                + " or (bs.bill.referenceBill.insId) like :txt "
                + " or (bs.bill.referenceBill.deptId) like :txt "
                + " or (bs.bill.referenceBill.referenceNumber) like :txt "
                + " )"
                + " order by bs.sessionDate, bs.serialNo ";
        HashMap hh = new HashMap();
        hh.put("bt", bts);
        hh.put("class", BilledBill.class);
        hh.put("txt", "%" + menuBarSearchText.toLowerCase().trim() + "%");
        billSessions = billSessionFacade.findByJpql(sql, hh, TemporalType.DATE);

    }

    public void makeListNull() {
        maxResult = configOptionApplicationController.getIntegerValueByKey("Maximum Number of records for default search", 50);
        bills = null;
        aceptPaymentBills = null;
        selectedBills = null;
        billFees = null;
        billItems = null;
        patientInvestigations = null;
        searchKeyword = null;
        printPreview = false;
        showLoggedDepartmentOnly = true;
        settledBillType = null;
    }

    public String navigateToSearchOpdBillsOfLoggedDepartment() {
        maxResult = 50;
        bills = null;
        aceptPaymentBills = null;
        selectedBills = null;
        billFees = null;
        billItems = null;
        patientInvestigations = null;
        searchKeyword = null;
        return "/opd/search_opd_billd_of_logged_department?faces-redirect=true";
    }

    public void makeListNull2() {
        billFeesDone = null;
        searchKeyword = null;
        speciality = null;
        staff = null;
        item = null;
        makeListNull();
    }

    public void createPatientInvestigationsTableAllTest() {
        Date startTime = new Date();

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p";

        Map temMap = new HashMap();

        patientInvestigations = getPatientInvestigationFacade().findByJpql(sql, 10);
        checkRefundBillItems(patientInvestigations);

    }

    public String navigatToopdSearchProfessionalPaymentDue1() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();

        String url = request.getRequestURL().toString();

        String ipAddress = request.getRemoteAddr();

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setEventStatus("Started");
        long duration;
        Date startTime = new Date();
        auditEvent.setEventDataTime(startTime);
        if (sessionController != null && sessionController.getDepartment() != null) {
            auditEvent.setDepartmentId(sessionController.getDepartment().getId());
        }

        if (sessionController != null && sessionController.getInstitution() != null) {
            auditEvent.setInstitutionId(sessionController.getInstitution().getId());
        }
        if (sessionController != null && sessionController.getLoggedUser() != null) {
            auditEvent.setWebUserId(sessionController.getLoggedUser().getId());
        }
        auditEvent.setUrl(url);
        auditEvent.setIpAddress(ipAddress);
        auditEvent.setEventTrigger("navigatToopdSearchProfessionalPaymentDue1()");
        auditEventApplicationController.logAuditEvent(auditEvent);

        Date endTime = new Date();
        duration = endTime.getTime() - startTime.getTime();
        auditEvent.setEventDuration(duration);
        auditEvent.setEventStatus("Completed");
        auditEventApplicationController.logAuditEvent(auditEvent);
        return "/opd_search_professional_payment_due_1.xhtml?faces-redirect=true";
    }

    public String navigatToTotalCashierSummary() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/cashier_reports/total_cashier_summary?faces-redirect=true";
    }

    public void resetAllFiltersExceptDateRange() {
        institution = null;
        department = null;
        webUser = null;
        site = null;
        department = null;
        departments = null;
        paymentMethod = null;
        searchKeyword = null;
    }

    public void resetAllFiltersExceptDateRangeInstitueDepartmentSite() {
        webUser = null;
        departments = null;
        paymentMethod = null;
        searchKeyword = null;
    }

    public void resetAllFilters() {
        institution = null;
        department = null;
        webUser = null;
        site = null;
        fromDate = null;
        toDate = null;
        paymentMethod = null;
        searchKeyword = null;
        institution = null;
        department = null;
        site = null;
        category = null;
        item = null;
        speciality = null;
        staff = null;
        webUser = null;
    }

    public String navigatToAllCashierSummary() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/cashier_reports/all_cashier_summary?faces-redirect=true";
    }

    public String navigatBackToAllCashierSummary() {
        if (configOptionApplicationController.getBooleanValueByKey("Restrict Filter Reset of Cashier Summaries", false)) {
            resetAllFiltersExceptDateRangeInstitueDepartmentSite();
        } else {
            resetAllFiltersExceptDateRange();
        }
        bundle = new ReportTemplateRowBundle();
        generateAllCashierSummary();
        return "/reports/cashier_reports/all_cashier_summary?faces-redirect=true";
    }

    public String navigatToBillListByBillType(BillType billType) {
        this.billType = billType;
        listBills();
        return "/analytics/bills?faces-redirect=true";
    }

    public String navigatToBillListByBillTypeAtomic(BillTypeAtomic billTypeAtomic) {
        this.billTypeAtomic = billTypeAtomic;
        listBills();
        return "/analytics/bills?faces-redirect=true";
    }

    public String navigatToDepartmentRevenueReport() {
        bundle = new ReportTemplateRowBundle();
        return "/reports/financialReports/department_revenue_report?faces-redirect=true";
    }

    public String navigatToShiftStartAndEnds() {
        bundle = new ReportTemplateRowBundle();
        return "/reports/cashier_reports/shift_start_and_ends?faces-redirect=true";
    }

    public String navigatToCashierSummary() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/cashier_reports/cashier_summary?faces-redirect=true";
    }

    public String navigatToStaffWelfareBills() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/financialReports/staff_welfare?faces-redirect=true";
    }

    public String navigateToPackageReport() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/financialReports/package_report?faces-redirect=true";
    }

    public String navigateToCreditInvoiceDispatch() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/financialReports/credit_invoice_dispatch_report?faces-redirect=true";
    }

    public String navigateToPaymentSettlement() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/financialReports/payment_settlement_report?faces-redirect=true";
    }

    public String navigateToDueSearch() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/financialReports/inward_due_search?faces-redirect=true";
    }

    public String navigateToDueSearchCreditCompany() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/financialReports/inward_due_search_credit_company?faces-redirect=true";
    }

    public String navigateToDueAge() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/financialReports/inward_due_age?faces-redirect=true";
    }

    public String navigateToDueAgeCreditCompany() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/financialReports/inward_due_age_credit_company?faces-redirect=true";
    }

    public String navigateToDueAgeDetail() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/financialReports/inward_due_age_credit_company_detail?faces-redirect=true";
    }

    public String navigatToShiftEndSummary() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/cashier_reports/shift_end_summary?faces-redirect=true";
    }

    public String navigatToCashierDetails() {
        resetAllFiltersExceptDateRange();
        bundle = new ReportTemplateRowBundle();
        return "/reports/cashier_reports/cashier_detailed?faces-redirect=true";
    }

    public String navigateToListAllDrawers() {
        return "/reports/cashier_reports/all_drawers?faces-redirect=true";
    }

    public String navigateToAllCashierHandovers() {
        return "/reports/cashier_reports/all_cashier_shifts?faces-redirect=true";
    }

    public String navigateToShiftEndCash() {
        return "/reports/cashier_reports/shift_end_cash_list?faces-redirect=true";
    }

    public String navigatToReportDoctorPaymentOpd() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();

        String url = request.getRequestURL().toString();

        String ipAddress = request.getRemoteAddr();

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setEventStatus("Started");
        long duration;
        Date startTime = new Date();
        auditEvent.setEventDataTime(startTime);
        if (sessionController != null && sessionController.getDepartment() != null) {
            auditEvent.setDepartmentId(sessionController.getDepartment().getId());
        }

        if (sessionController != null && sessionController.getInstitution() != null) {
            auditEvent.setInstitutionId(sessionController.getInstitution().getId());
        }
        if (sessionController != null && sessionController.getLoggedUser() != null) {
            auditEvent.setWebUserId(sessionController.getLoggedUser().getId());
        }
        auditEvent.setUrl(url);
        auditEvent.setIpAddress(ipAddress);
        auditEvent.setEventTrigger("navigatToReportDoctorPaymentOpd()");
        auditEventApplicationController.logAuditEvent(auditEvent);

        Date endTime = new Date();
        duration = endTime.getTime() - startTime.getTime();
        auditEvent.setEventDuration(duration);
        auditEvent.setEventStatus("Completed");
        auditEventApplicationController.logAuditEvent(auditEvent);
        return "/reportCashier/report_doctor_payment_opd.xhtml?faces-redirect=true";

    }

    public String navigatToReportDoctorPaymentOpdByBill() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();

        String url = request.getRequestURL().toString();

        String ipAddress = request.getRemoteAddr();

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setEventStatus("Started");
        long duration;
        Date startTime = new Date();
        auditEvent.setEventDataTime(startTime);
        if (sessionController != null && sessionController.getDepartment() != null) {
            auditEvent.setDepartmentId(sessionController.getDepartment().getId());
        }

        if (sessionController != null && sessionController.getInstitution() != null) {
            auditEvent.setInstitutionId(sessionController.getInstitution().getId());
        }
        if (sessionController != null && sessionController.getLoggedUser() != null) {
            auditEvent.setWebUserId(sessionController.getLoggedUser().getId());
        }
        auditEvent.setUrl(url);
        auditEvent.setIpAddress(ipAddress);
        auditEvent.setEventTrigger("navigatToReportDoctorPaymentOpdByBill()");
        auditEventApplicationController.logAuditEvent(auditEvent);

        Date endTime = new Date();
        duration = endTime.getTime() - startTime.getTime();
        auditEvent.setEventDuration(duration);
        auditEvent.setEventStatus("Completed");
        auditEventApplicationController.logAuditEvent(auditEvent);
        return "/reportCashier/report_doctor_payment_opd_by_bill.xhtml?faces-redirect=true";

    }

    public void fillToMyDepartmentPatientInvestigations() {
        Date startTime = new Date();

        String jpql = "select pi "
                + " from PatientInvestigation pi "
                + " join pi.investigation i "
                + " join pi.billItem.bill b "
                + " join b.patient.person p "
                + " where "
                + " b.createdAt between :fromDate and :toDate  "
                + " and b.toDepartment=:dep ";

        Map temMap = new HashMap();
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("dep", getSessionController().getLoggedUser().getDepartment());

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            jpql += " and  ((p.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            jpql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            jpql += " and  ((p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            jpql += " and  ((i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (patientEncounter != null) {
            jpql += "and pi.encounter=:en";
            temMap.put("en", patientEncounter);
        }

        jpql += " order by pi.id desc  ";
//

        patientInvestigations = getPatientInvestigationFacade().findByJpql(jpql, temMap, TemporalType.TIMESTAMP, 50);
        checkRefundBillItems(patientInvestigations);

    }

    public void fillToDepartmentPatientInvestigations() {
        Date startTime = new Date();

        String jpql = "select pi "
                + " from PatientInvestigation pi "
                + " join pi.investigation i "
                + " join pi.billItem.bill b "
                + " join b.patient.person p "
                + " where "
                + " b.createdAt between :fromDate and :toDate  "
                + " and b.toDepartment=:dep ";

        Map temMap = new HashMap();
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("dep", getReportKeyWord().getDepartment());

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            jpql += " and  ((p.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            jpql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            jpql += " and  ((p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            jpql += " and  ((i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (patientEncounter != null) {
            jpql += "and pi.encounter=:en";
            temMap.put("en", patientEncounter);
        }

        jpql += " order by pi.id desc  ";
//

        patientInvestigations = getPatientInvestigationFacade().findByJpql(jpql, temMap, TemporalType.TIMESTAMP, 50);
        checkRefundBillItems(patientInvestigations);

    }

    public void fillToSelectedDepartmentPatientInvestigations() {
        getSearchKeyword().setItemDepartment(getReportKeyWord().getDepartment());
        getSearchKeyword().setPatientEncounter(patientEncounter);
        patientInvestigations = patientInvestigationService.fetchPatientInvestigations(fromDate, toDate, getSearchKeyword());
    }

    public void fillPatientInvestigationsForIxAdminPastData(Investigation ix) {
        getSearchKeyword().setPatientEncounter(patientEncounter);
        getSearchKeyword().setInvestigation(ix);
        patientInvestigations = patientInvestigationService.fetchPatientInvestigations(fromDate, toDate, getSearchKeyword());
    }

    public void fillCollectingCentreCourierPatientInvestigations() {
        String jpql = "select pi "
                + " from PatientInvestigation pi "
                + " join pi.investigation i "
                + " join pi.billItem.bill b "
                + " join b.patient.person p "
                + " where "
                + " b.createdAt between :fromDate and :toDate  ";

        Map temMap = new HashMap();
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("dep", getReportKeyWord().getDepartment());

        if (institution == null) {
            jpql += " and b.collectingCentre in :ccs ";
            temMap.put("ccs", sessionController.getLoggableCollectingCentres());
        } else {
            jpql += " and b.collectingCentre=:cc ";
            temMap.put("cc", institution);
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            jpql += " and  ((p.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            jpql += " and  ((b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            jpql += " and  ((p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            jpql += " and  ((i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        jpql += " order by pi.id desc  ";
//

        patientInvestigations = getPatientInvestigationFacade().findByJpql(jpql, temMap, TemporalType.TIMESTAMP, 50);
        checkRefundBillItems(patientInvestigations);

    }

    public void createPreRefundTable() {

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from RefundBill b where b.billType = :billType "
                + " and b.institution=:ins and "
                + " (b.billedBill is null  or type(b.billedBill)=:billedClass ) "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false and b.deptId is not null ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billedClass", PreBill.class);
        temMap.put("billType", BillType.PharmacyPre);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpqlWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 50);

        Date startTime = new Date();

    }

    public void createPreRefundOpdTable() {

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from RefundBill b where "
                + " b.createdAt between :fromDate and :toDate"
                + " and b.retired=false and b.deptId is not null "
                + " and b.billType = :billType"
                + " and b.institution = :ins";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
//        temMap.put("billedClass", PreBill.class);
//        temMap.put("billType", BillType.OpdBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billType", BillType.OpdBill);
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpqlWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 50);

        Date startTime = new Date();

    }

    public void reportSettledOPDBills() {
        Date startTime = new Date();
        settledBills(billType.OpdBill);

    }

    public void reportSettledPharmacyBills() {
        Date startTime = new Date();
        settledBills(billType.PharmacyWholeSale);

    }

    public void settledBills(BillType bt) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where "
                + " b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.referenceBill.billType=:bt "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                //                + " and b.balance=0 "
                + "order by b.createdAt desc ";

//
        temMap.put("billType", BillType.CashRecieveBill);
        temMap.put("bt", bt);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    List<billsWithbill> withbills;

    public List<billsWithbill> getWithbills() {
        return withbills;
    }

    public void setWithbills(List<billsWithbill> withbills) {
        this.withbills = withbills;
    }

    public void createCreditBillsWithOPDBill() {
        Date startTime = new Date();
        createCreditBillsWithBill(billType.OpdBill);

    }

    public void createCreditBillsWithPharmacyBill() {
        Date startTime = new Date();

        createCreditBillsWithBill(billType.PharmacyWholeSale);

    }

    public void createCreditBillsWithBill(BillType refBillType) {
        bills = fetchBills(BillType.CashRecieveBill, refBillType);
        withbills = new ArrayList<>();

        for (Bill b : bills) {
            billsWithbill bWithbill = new billsWithbill();
            bWithbill.setB(b);
            bWithbill.setCaBills(fetchCreditBills(BillType.CashRecieveBill, b));
            withbills.add(bWithbill);
        }

    }

    public List<Bill> fetchBills(BillType bt, BillType rbt) {

        String sql;
        Map temMap = new HashMap();

        sql = "select DISTINCT(b.referenceBill) from Bill b where "
                + " b.billType =:billType "
                + " and b.referenceBill.billType =:billTypeRef "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        temMap.put("billType", bt);
        temMap.put("billTypeRef", rbt);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        return getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    public List<Bill> fetchCreditBills(BillType bt, Bill b) {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where "
                + " b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.referenceBill=:bid ";

        temMap.put("billType", bt);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bid", b);
        temMap.put("ins", getSessionController().getInstitution());

        return getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    @Deprecated
    private void checkLabReportsApproved(List<Bill> bills) {
        if (!bills.isEmpty()) {
            for (Bill b : bills) {
                String sql;
                Map m = new HashMap();
                sql = " select pr from PatientReport pr where "
                        + " pr.retired=false "
                        + " and pr.patientInvestigation.billItem.bill=:b "
                        + " and pr.approved=true ";

                m.put("b", b);
                List<PatientReport> list = getPatientReportFacade().findByJpql(sql, m);
                if (!list.isEmpty()) {
                    b.setApprovedAnyTest(true);
                }
            }
        }
    }

    private void checkLabReportsApprovedBillItem(List<BillItem> billItems) {
        for (BillItem bi : billItems) {
            String sql;
            Map m = new HashMap();
            sql = " select pr from PatientReport pr where "
                    + " pr.retired=false "
                    + " and pr.patientInvestigation.billItem=:bi "
                    + " and pr.approved=true ";

            m.put("bi", bi);
            List<PatientReport> list = getPatientReportFacade().findByJpql(sql, m);
            if (!list.isEmpty()) {
                bi.getBill().setApprovedAnyTest(true);
            }
        }
    }

    public PatientReportFacade getPatientReportFacade() {
        return patientReportFacade;
    }

    public void setPatientReportFacade(PatientReportFacade patientReportFacade) {
        this.patientReportFacade = patientReportFacade;
    }

    public Institution getCreditCompany() {
        return creditCompany;
    }

    public void setCreditCompany(Institution creditCompany) {
        this.creditCompany = creditCompany;
    }

    public Department getFromDepartment() {
        return fromDepartment;
    }

    public void setFromDepartment(Department fromDepartment) {
        this.fromDepartment = fromDepartment;
    }

    public Department getToDepartment() {
        return toDepartment;
    }

    public void setToDepartment(Department toDepartment) {
        this.toDepartment = toDepartment;
    }

    public PatientFacade getPatientFacade() {
        return patientFacade;
    }

    public List<PatientReport> getPatientReports() {
        return patientReports;
    }

    public void setPatientReports(List<PatientReport> patientReports) {
        this.patientReports = patientReports;
    }

    public Institution getOtherInstitution() {
        return otherInstitution;
    }

    public void setOtherInstitution(Institution otherInstitution) {
        this.otherInstitution = otherInstitution;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public int getManageListIndex() {
        return manageListIndex;
    }

    public void setManageListIndex(int manageListIndex) {
        this.manageListIndex = manageListIndex;
    }

    public List<BillLight> getBillLights() {
        return billLights;
    }

    public void setBillLights(List<BillLight> billLights) {
        this.billLights = billLights;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Institution getFromInstitution() {
        return fromInstitution;
    }

    public void setFromInstitution(Institution fromInstitution) {
        this.fromInstitution = fromInstitution;
    }

    public Institution getToInstitution() {
        return toInstitution;
    }

    public void setToInstitution(Institution toInstitution) {
        this.toInstitution = toInstitution;
    }

    public Institution getDealer() {
        return dealer;
    }

    public void setDealer(Institution dealer) {
        this.dealer = dealer;
    }

    public List<Bill> getGrnBills() {
        return grnBills;
    }

    public void setGrnBills(List<Bill> grnBills) {
        this.grnBills = grnBills;
    }

    public Long getCurrentBillId() {
        return currentBillId;
    }

    public void setCurrentBillId(Long currentBillId) {
        this.currentBillId = currentBillId;
    }

    public Long getBarcodeIdLong() {
        return barcodeIdLong;
    }

    public void setBarcodeIdLong(Long barcodeIdLong) {
        this.barcodeIdLong = barcodeIdLong;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public List<BillSummaryRow> getBillSummaryRows() {
        return billSummaryRows;
    }

    public void setBillSummaryRows(List<BillSummaryRow> billSummaryRows) {
        this.billSummaryRows = billSummaryRows;
    }

    public double getGrossTotal() {
        return grossTotal;
    }

    public void setGrossTotal(double grossTotal) {
        this.grossTotal = grossTotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public DepartmentController getDepartmentController() {
        return departmentController;
    }

    public void setDepartmentController(DepartmentController departmentController) {
        this.departmentController = departmentController;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public double getTotalBillCount() {
        return totalBillCount;
    }

    public void setTotalBillCount(double totalBillCount) {
        this.totalBillCount = totalBillCount;
    }

    public Long getStartBillId() {
        return startBillId;
    }

    public void setStartBillId(Long startBillId) {
        this.startBillId = startBillId;
    }

    public Long getEndBillId() {
        return endBillId;
    }

    public void setEndBillId(Long endBillId) {
        this.endBillId = endBillId;
    }

    public List<PaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public WebUser getWebUser() {
        return webUser;
    }

    public void setWebUser(WebUser webUser) {
        this.webUser = webUser;
    }

    public BillTypeAtomic getBillTypeAtomic() {
        return billTypeAtomic;
    }

    public void setBillTypeAtomic(BillTypeAtomic billTypeAtomic) {
        this.billTypeAtomic = billTypeAtomic;
    }

    public Date getMaxDate() {
        maxDate = CommonFunctions.getEndOfDay(new Date());
        return maxDate;
    }

    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }

    public double getCashTotal() {
        return cashTotal;
    }

    public void setCashTotal(double cashTotal) {
        this.cashTotal = cashTotal;
    }

    public double getCardTotal() {
        return cardTotal;
    }

    public void setCardTotal(double cardTotal) {
        this.cardTotal = cardTotal;
    }

    public double getChequeTotal() {
        return chequeTotal;
    }

    public void setChequeTotal(double chequeTotal) {
        this.chequeTotal = chequeTotal;
    }

    public double getSlipTotal() {
        return slipTotal;
    }

    public void setSlipTotal(double slipTotal) {
        this.slipTotal = slipTotal;
    }

    public double getTotalOfOtherPayments() {
        return totalOfOtherPayments;
    }

    public void setTotalOfOtherPayments(double totalOfOtherPayments) {
        this.totalOfOtherPayments = totalOfOtherPayments;
    }

    public double getBillCount() {
        return billCount;
    }

    public void setBillCount(double billCount) {
        this.billCount = billCount;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public BillController getBillController() {
        return billController;
    }

    public void setBillController(BillController billController) {
        this.billController = billController;
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public boolean isDuplicateBillView() {
        return duplicateBillView;
    }

    public void setDuplicateBillView(boolean duplicateBillView) {
        this.duplicateBillView = duplicateBillView;
    }

    public int getManagePaymentIndex() {
        return managePaymentIndex;
    }

    public void setManagePaymentIndex(int managePaymentIndex) {
        this.managePaymentIndex = managePaymentIndex;
    }

    public ReportTemplateType getReportTemplateType() {
        return reportTemplateType;
    }

    public void setReportTemplateType(ReportTemplateType reportTemplateType) {
        this.reportTemplateType = reportTemplateType;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<CashBookEntry> getCashBookEntries() {
        return cashBookEntries;
    }

    public void setCashBookEntries(List<CashBookEntry> cashBookEntries) {
        this.cashBookEntries = cashBookEntries;
    }

    public Institution getSite() {
        return site;
    }

    public void setSite(Institution site) {
        this.site = site;
    }

    public ReportTemplateRowBundle getBundle() {
        return bundle;
    }

    public void setBundle(ReportTemplateRowBundle bundle) {
        this.bundle = bundle;
    }

    public String getBackLink() {
        return backLink;
    }

    public void setBackLink(String backLink) {
        this.backLink = backLink;
    }

    public ReportTemplateRowBundle getBundleBillItems() {
        return bundleBillItems;
    }

    public void setBundleBillItems(ReportTemplateRowBundle bundleBillItems) {
        this.bundleBillItems = bundleBillItems;
    }

    public List<OpdSaleSummaryDTO> getOpdSaleSummaryDtos() {
        return opdSaleSummaryDtos;
    }

    public void setOpdSaleSummaryDtos(List<OpdSaleSummaryDTO> opdSaleSummaryDtos) {
        this.opdSaleSummaryDtos = opdSaleSummaryDtos;
    }

    public int getOpdAnalyticsIndex() {
        return opdAnalyticsIndex;
    }

    public void setOpdAnalyticsIndex(int opdAnalyticsIndex) {
        this.opdAnalyticsIndex = opdAnalyticsIndex;
    }

    public List<Drawer> getDrawerList() {
        return drawerList;
    }

    public void setDrawerList(List<Drawer> drawerList) {
        this.drawerList = drawerList;
    }

    public DrawerFacade getDrawerFacade() {
        return drawerFacade;
    }

    public void setDrawerFacade(DrawerFacade drawerFacade) {
        this.drawerFacade = drawerFacade;
    }

    public Drawer getSelectedDrawer() {
        return selectedDrawer;
    }

    public void setSelectedDrawer(Drawer selectedDrawer) {
        this.selectedDrawer = selectedDrawer;
    }

    public BillClassType getBillClassType() {
        return billClassType;
    }

    public void setBillClassType(BillClassType billClassType) {
        this.billClassType = billClassType;
    }

    public StreamedContent getDownloadingExcel() {
        return downloadingExcel;
    }

    public void setDownloadingExcel(StreamedContent downloadingExcel) {
        this.downloadingExcel = downloadingExcel;
    }

    public double getHosTotal() {
        return hosTotal;
    }

    public void setHosTotal(double hosTotal) {
        this.hosTotal = hosTotal;
    }

    public double getStaffTotal() {
        return staffTotal;
    }

    public void setStaffTotal(double staffTotal) {
        this.staffTotal = staffTotal;
    }

    public double getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(double discountTotal) {
        this.discountTotal = discountTotal;
    }

    public double getAmountTotal() {
        return amountTotal;
    }

    public void setAmountTotal(double amountTotal) {
        this.amountTotal = amountTotal;
    }

    public Institution getToSite() {
        return toSite;
    }

    public void setToSite(Institution toSite) {
        this.toSite = toSite;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getMrnNo() {
        return mrnNo;
    }

    public void setMrnNo(String mrnNo) {
        this.mrnNo = mrnNo;
    }

    public boolean isWithProfessionalFee() {
        return withProfessionalFee;
    }

    public void setWithProfessionalFee(boolean withProfessionalFee) {
        this.withProfessionalFee = withProfessionalFee;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public Drawer getDrawer() {
        return drawer;
    }

    public void setDrawer(Drawer drawer) {
        this.drawer = drawer;
    }

    public Department getServiceDepartment() {
        return serviceDepartment;
    }

    public void setServiceDepartment(Department serviceDepartment) {
        this.serviceDepartment = serviceDepartment;
    }

    public Department getBilledDepartment() {
        return billedDepartment;
    }

    public void setBilledDepartment(Department billedDepartment) {
        this.billedDepartment = billedDepartment;
    }

    public String getSelectedOpdPackageBillSelector() {
        return selectedOpdPackageBillSelector;
    }

    public void setSelectedOpdPackageBillSelector(String selectedOpdPackageBillSelector) {
        this.selectedOpdPackageBillSelector = selectedOpdPackageBillSelector;
    }

    public List<String> getOpdPackageBillSelector() {
        if (OpdPackageBillSelector == null) {
            OpdPackageBillSelector = new ArrayList<>();
            OpdPackageBillSelector.add("Single Package Bills");
            OpdPackageBillSelector.add("Paackage Batch Bills");
        }
        return OpdPackageBillSelector;
    }

    public void setOpdPackageBillSelector(List<String> OpdPackageBillSelector) {
        this.OpdPackageBillSelector = OpdPackageBillSelector;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public boolean isShowLoggedDepartmentOnly() {
        return showLoggedDepartmentOnly;
    }

    public void setShowLoggedDepartmentOnly(boolean showLoggedDepartmentOnly) {
        this.showLoggedDepartmentOnly = showLoggedDepartmentOnly;
    }

    public List<PharmaceuticalBillItem> getPharmaceuticalBillItems() {
        return pharmaceuticalBillItems;
    }

    public void setPharmaceuticalBillItems(List<PharmaceuticalBillItem> pharmaceuticalBillItems) {
        this.pharmaceuticalBillItems = pharmaceuticalBillItems;
    }

    public class billsWithbill {

        Bill b;
        List<Bill> caBills;

        public Bill getB() {
            return b;
        }

        public void setB(Bill b) {
            this.b = b;
        }

        public List<Bill> getCaBills() {
            if (caBills == null) {
                caBills = new ArrayList<>();
            }
            return caBills;
        }

        public void setCaBills(List<Bill> caBills) {
            this.caBills = caBills;
        }

    }

    public void createReturnSaleBills() {
        Date startTime = new Date();

        createReturnSaleBills(BillType.PharmacyPre, true);

    }

    public void createReturnSaleAllBills() {
        Date startTime = new Date();

        createReturnSaleBills(BillType.PharmacyPre, false);

    }

    public void createReturnWholeSaleBills() {
        createReturnSaleBills(BillType.PharmacyWholesalePre, true);
    }

    public void createReturnWholeSaleAllBills() {
        createReturnSaleBills(BillType.PharmacyWholesalePre, false);
    }

    public void createReturnSaleBills(BillType billType, boolean maxNum) {

        Map m = new HashMap();
        m.put("bt", billType);
        m.put("billedClass", PreBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        String sql;

        sql = "Select b from RefundBill b where  b.retired=false "
                + " and b.institution=:ins and "
                + " (b.billedBill is null  or type(b.billedBill)=:billedClass ) "
                + " and b.createdAt between :fd and :td"
                + " and b.billType=:bt ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  ((b.billedBill.deptId) like :rNo )";
            m.put("rNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
        if (maxNum == true) {
            bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 25);
        } else {
            bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);
        }

    }

    public void createTableByKeywordToPayBills() {
        Date startTime = new Date();
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where "
                + " b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.balance>0 ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (((b.deptId) like :billNo )or((b.insId) like :billNo ))";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", BillType.OpdBathcBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createTablePharmacyCreditToPayBills() {
        Date startTime = new Date();

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where "
                + " b.billType in :billTypes "
                + " and b.institution=:ins "
                + " and b.department=:dep "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and (b.netTotal-b.paidAmount)>0 "
                + " and b.paymentMethod=:pm ";

        if (getSearchKeyword().getInstitution() != null && !getSearchKeyword().getInstitution().trim().equals("")) {
            sql += " and  ((b.toInstitution.name) like :comp )";
            temMap.put("comp", "%" + getSearchKeyword().getInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.toStaff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (((b.deptId) like :billNo )or((b.insId) like :billNo ))";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billTypes", Arrays.asList(new BillType[]{BillType.PharmacyWholeSale, BillType.PharmacySale}));
        temMap.put("pm", PaymentMethod.Credit);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("dep", getSessionController().getDepartment());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createReturnBhtBills() {
        Date startTime = new Date();

        createReturnBhtBills(BillType.PharmacyBhtPre);

    }

    public void createReturnBhtBillsStore() {
        createReturnBhtBills(BillType.StoreBhtPre);
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    private void createReturnBhtBills(BillType billType) {

        Map m = new HashMap();
        m.put("bt", billType);
        m.put("billedClass", PreBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        String sql;

        sql = "Select b from RefundBill b where  b.retired=false "
                + " and b.institution=:ins and "
                + " (b.billedBill is null  or type(b.billedBill)=:billedClass ) "
                + " and b.createdAt between :fd and :td"
                + " and b.billType=:bt ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  ((b.billedBill.deptId) like :rNo )";
            m.put("rNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            m.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createVariantReportSearch() {
        Date startTime = new Date();

        String sql = "";
        HashMap tmp = new HashMap();

        sql = "Select b From PreBill b where b.cancelledBill is null  "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false and b.billType= :bTp ";

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :dep )";
            tmp.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCategory() != null && !getSearchKeyword().getCategory().trim().equals("")) {
            sql += " and  ((b.category.name) like :cat )";
            tmp.put("cat", "%" + getSearchKeyword().getCategory().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("bTp", BillType.PharmacyMajorAdjustment);
        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP);

    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<Bill> getPrescreptionBills() {
        return prescreptionBills;
    }

    public void setPrescreptionBills(List<Bill> prescreptionBills) {
        this.prescreptionBills = prescreptionBills;
    }

    public void createPharmacyPrescriptionBillTable() {
        Date startTime = new Date();

        Map m = new HashMap();
        m.put("bt", BillType.PharmacyPre);
        m.put("rBt", BillType.PharmacySale);
        m.put("class", PreBill.class);
        m.put("rClass", BilledBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        String sql;
        sql = "Select b from Bill b "
                + " where b.retired=false and b.createdAt between :fd and :td and b.billType=:bt "
                + " and b.referredBy is not null "
                + " and b.institution=:ins "
                + " and b.referenceBill.billType=:rBt "
                + " and type(b)=:class "
                + " and type(b.referenceBill)=:rClass ";

        if (department != null) {
            sql += " and b.department=:dept ";
            m.put("dept", department);
        }

        sql += " order by b.createdAt ";

        prescreptionBills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public void createPharmacyRetailBills() {
        Date startTime = new Date();

        List<BillType> billTypes = new ArrayList<>();
        billTypes.add(BillType.PharmacyPre);
        billTypes.add(BillType.PharmacySaleWithoutStock);

        createPharmacyRetailBills(billTypes, true);

    }

    public void createPharmacyAddToStockBills() {
        Date startTime = new Date();
        createPharmacyAddToBills(BillType.PharmacyAddtoStock, true);
    }

    public void createPharmacyAddToStockAllBills() {
        Date startTime = new Date();
        createPharmacyAddToBills(BillType.PharmacyAddtoStock, false);
    }

    public void createPharmacyAddToBills(BillType billtype, boolean maxNum) {

        String sql;
        Map m = new HashMap();

        sql = "Select b from Bill b where "
                + " b.createdAt between :fd and :td "
                + " and b.billType=:bt"
                + " and b.institution=:ins "
                + " and b.department=:ldep";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :dep )";
            m.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        m.put("bt", billtype);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        m.put("ldep", getSessionController().getLoggedUser().getDepartment());
//
        //

        if (maxNum == true) {
            bills = billFacade.findByJpql(sql, m, TemporalType.TIMESTAMP, 25);
        } else {
            bills = billFacade.findByJpql(sql, m, TemporalType.TIMESTAMP);
        }
        netTotal = 0.0;
        for (Bill b : bills) {
            netTotal += b.getNetTotal();
        }
    }

    public void createPharmacyWholesaleBills() {
        createPharmacyRetailBills(BillType.PharmacyWholesalePre, true);
    }

    public void createPharmacyWholeTableRe() {
        Date startTime = new Date();

        Map m = new HashMap();
        m.put("bt", BillType.PharmacyWholesalePre);
        //     m.put("class", PreBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ret", false);
        m.put("ins", getSessionController().getInstitution());
        String sql;

        sql = "Select b from Bill b where "
                + " b.createdAt between :fd and :td "
                + " and b.billType=:bt "
                + " and b.retired=:ret "
                + " and b.institution=:ins";
        //+ " and type(b)=:class ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :dep )";
            m.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        //     //////
        bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyRetailAllBills() {
        Date startTime = new Date();

        List<BillType> billTypes = new ArrayList<>();
        billTypes.add(BillType.PharmacyPre);
        billTypes.add(BillType.PharmacySaleWithoutStock);

        createPharmacyRetailBills(billTypes, false);

    }

    public void createPharmacyWholesaleAllBills() {
        createPharmacyRetailBills(BillType.PharmacyWholesalePre, false);
    }

    public void createPharmacyRetailBills(BillType billtype, boolean maxNum) {

        List<BillType> billTypes = new ArrayList<>();
        billTypes.add(billtype);

        createPharmacyRetailBills(billTypes, maxNum);

    }

    public void createPharmacyRetailBills(List<BillType> billtypes, boolean maxNum) {

        Map m = new HashMap();
        m.put("bts", billtypes);
        //   m.put("class", PreBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        m.put("ldep", getSessionController().getLoggedUser().getDepartment());
        m.put("bta", BillTypeAtomic.PHARMACY_RETAIL_SALE_PRE_TO_SETTLE_AT_CASHIER);
        String sql;

        sql = "Select b from PreBill b where "
                + " b.createdAt between :fd and :td "
                + " and b.billType in :bts"
                + " and b.billTypeAtomic <> :bta"
                + " and b.billedBill is null "
                + " and b.institution=:ins "
                + " and b.department=:ldep";
        //  + " and type(b)=:class ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :dep )";
            m.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }
        if (getPaymentMethod() != null) {
            sql += " and b.paymentMethod=:pay ";
            m.put("pay", paymentMethod);
        }
        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :phone )";
            m.put("phone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        //     //////

        if (maxNum == true) {
            bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 25);
        } else {
            bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);
        }
        netTotal = 0.0;
        for (Bill b : bills) {
            netTotal += b.getNetTotal();
        }
    }

    public String searchMyPharmacyBills() {
        BillType billtype = BillType.PharmacyPre;
        String sql;
        if (false) {
            Bill b = new Bill();
            b.getPatient().getPerson();
            sessionController.getLoggedUser().getWebUserPerson();
        }
        sql = "Select b from PreBill b where "
                + " b.billType=:bt"
                + " and b.billedBill is null "
                + " and b.patient.person=:person";
        sql += " order by b.createdAt desc  ";
        Map m = new HashMap();
        m.put("bt", billtype);
        m.put("person", sessionController.getLoggedUser().getWebUserPerson());

        boolean maxNum = true;
        if (maxNum == true) {
            bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 25);
        } else {
            bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);
        }
        netTotal = 0.0;
        for (Bill b : bills) {
            netTotal += b.getNetTotal();
        }

        return "/mobile/my_pharmacy_bills";
    }

    double netTotalValue;

    public void createPharmacyStaffBill() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();

        String url = request.getRequestURL().toString();

        String ipAddress = request.getRemoteAddr();

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setEventStatus("Started");
        long duration;
        Date startTime = new Date();
        auditEvent.setEventDataTime(startTime);
        if (sessionController != null && sessionController.getDepartment() != null) {
            auditEvent.setDepartmentId(sessionController.getDepartment().getId());
        }

        if (sessionController != null && sessionController.getInstitution() != null) {
            auditEvent.setInstitutionId(sessionController.getInstitution().getId());
        }
        if (sessionController != null && sessionController.getLoggedUser() != null) {
            auditEvent.setWebUserId(sessionController.getLoggedUser().getId());
        }
        auditEvent.setUrl(url);
        auditEvent.setIpAddress(ipAddress);
        auditEvent.setEventTrigger("createPharmacyStaffBill()");
        auditEventApplicationController.logAuditEvent(auditEvent);

        Map m = new HashMap();
        m.put("bt", BillType.PharmacyPre);
        //   m.put("class", PreBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        String sql;

        sql = "Select b from PreBill b where "
                + " b.createdAt between :fd and :td "
                + " and b.billType=:bt"
                + " and b.billedBill is null "
                + " and b.institution=:ins "
                + " and b.toStaff is not null "
                + " order by b.createdAt ";
//
        //     //////
        bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);

        netTotalValue = 0.0;
        for (Bill b : bills) {
            netTotalValue += b.getNetTotal();
        }
        Date endTime = new Date();
        duration = endTime.getTime() - startTime.getTime();
        auditEvent.setEventDuration(duration);
        auditEvent.setEventStatus("Completed");
        auditEventApplicationController.logAuditEvent(auditEvent);

    }

    public void createPharmacyTableRe() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billTypeAtomic = :billTypeAtomic "
                + " and b.institution=:ins "
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false "
                + " and b.deptId is not null "
                + " and b.cancelled=false";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :dep )";
            temMap.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        } else if ((getSearchKeyword().getDepartment() == null || getSearchKeyword().getDepartment().isEmpty()) && getSessionController().getDepartment() != null) {
            sql += " and  ((b.department.name) = :dep )";
            temMap.put("dep", getSessionController().getDepartment().getName());
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :phone )";
            temMap.put("phone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }
        sql += " order by b.createdAt desc  ";
//
        temMap.put("billTypeAtomic", BillTypeAtomic.PHARMACY_RETAIL_SALE_PRE_TO_SETTLE_AT_CASHIER);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpqlWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);

    }

    public void listPharmacyIssue() {
        Map<String, Object> m = new HashMap<>();

        List<BillTypeAtomic> bts = new ArrayList<>();
        bts.add(BillTypeAtomic.PHARMACY_DISPOSAL_ISSUE);
        bts.add(BillTypeAtomic.PHARMACY_DISPOSAL_ISSUE_CANCELLED);
        bts.add(BillTypeAtomic.PHARMACY_DISPOSAL_ISSUE_RETURN);

        StringBuilder jpql = new StringBuilder();
        jpql.append("select new com.divudi.core.data.dto.PharmacyItemPurchaseDTO(");
        jpql.append("b.id, b.deptId, b.createdAt, ");
        jpql.append("b.institution.name, b.department.name, b.toDepartment.name, ");
        jpql.append("b.billType, b.total, b.netTotal, b.discount) ");
        jpql.append(" from Bill b");
        jpql.append(" where b.billTypeAtomic in :bts");
        jpql.append(" and b.createdAt between :fd and :td");
        jpql.append(" and b.institution=:ins");

        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("bts", bts);
        m.put("ins", getSessionController().getInstitution());

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            jpql.append(" and b.deptId like :billNo");
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            jpql.append(" and b.department.name like :fromDep");
            m.put("fromDep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            jpql.append(" and b.toDepartment.name like :toDep");
            m.put("toDep", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            try {
                Double netTotal = Double.parseDouble(getSearchKeyword().getNetTotal().trim());
                jpql.append(" and b.netTotal = :netTotal");
                m.put("netTotal", netTotal);
            } catch (NumberFormatException e) {
                // Ignore invalid number format
            }
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            try {
                Double total = Double.parseDouble(getSearchKeyword().getTotal().trim());
                jpql.append(" and b.total = :total");
                m.put("total", total);
            } catch (NumberFormatException e) {
                // Ignore invalid number format
            }
        }

        jpql.append(" order by b.createdAt desc");

        disposalIssueBillDtos = (List<PharmacyItemPurchaseDTO>) getBillFacade().findLightsByJpql(jpql.toString(), m, TemporalType.TIMESTAMP);
    }

    public void listStoreIssue() {
        listPharmacyPreBills(BillType.StoreIssue);
    }

    public void listPharmacyCancelled() {
        listPharmacyCancelledBills(BillType.PharmacyIssue);
    }

    public void listPharmacyReturns() {
        Date startTime = new Date();

        listPharmacyStoreReturnedBills(BillType.PharmacyIssue);

    }

    public void listStoreReturns() {
        Date startTime = new Date();
        listPharmacyStoreReturnedBills(BillType.StoreIssue);

    }

    public void listPharmacyBilledBills(BillType bt) {
        listPharmacyBills(bt, BilledBill.class);
    }

    public void listPharmacyPreBills(BillType bt) {
        listPharmacyBills(bt, PreBill.class);
    }

    public void listPharmacyCancelledBills(BillType bt) {
        listPharmacyBills(bt, CancelledBill.class);
    }

    public void listPharmacyStoreReturnedBills(BillType bt) {
        listReturnBills(bt, RefundBill.class);
    }

    public ServiceSession getSelectedServiceSession() {
        return selectedServiceSession;
    }

    public void setSelectedServiceSession(ServiceSession selectedServiceSession) {
        this.selectedServiceSession = selectedServiceSession;
    }

    public Staff getCurrentStaff() {
        return currentStaff;
    }

    public void setCurrentStaff(Staff currentStaff) {
        this.currentStaff = currentStaff;
    }

    public void listPharmacyBills(BillType bt, Class bc) {

        Map m = new HashMap();
        m.put("bt", bt);
        m.put("class", bc);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        String sql;

        sql = "Select b from Bill b where "
                + " b.createdAt between :fd and :td "
                + " and b.billType=:bt "
                + " and b.institution=:ins "
                + " and type(b)=:class "
                + " and b.billedBill is null ";

        if (getSearchKeyword().getRequestNo() != null && !getSearchKeyword().getRequestNo().trim().equals("")) {
            sql += " and  ((b.invoiceNumber) like :requestNo )";
            m.put("requestNo", "%" + getSearchKeyword().getRequestNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromDepartment() != null && !getSearchKeyword().getFromDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :fromDep )";
            m.put("fromDep", "%" + getSearchKeyword().getFromDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.toDepartment.name) like :toDep )";
            m.put("toDep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        //     //////
        bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public void listReturnBills(BillType bt, Class bc) {

        Map m = new HashMap();
        m.put("bt", bt);
        m.put("class", bc);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        m.put("dept", getSessionController().getDepartment());
        String sql;

        sql = "Select b from Bill b where "
                + " b.createdAt between :fd and :td "
                + " and b.billType=:bt "
                + " and b.institution=:ins "
                + " and b.department=:dept "
                + " and type(b)=:class ";

        if (getSearchKeyword().getRequestNo() != null && !getSearchKeyword().getRequestNo().trim().equals("")) {
            sql += " and  ((b.invoiceNumber) like :requestNo )";
            m.put("requestNo", "%" + getSearchKeyword().getRequestNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :dep )";
            m.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        //     //////
        bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyTableBht() {
        Date startTime = new Date();

        createTableBht(BillType.PharmacyBhtPre);

    }

    public void createStoreTableIssue() {
        createTableBht(BillType.StoreIssue);
    }

    public void createStoreTableBht() {
        Date startTime = new Date();
        createTableBht(BillType.StoreBhtPre);

    }

    public void createTableBht(BillType btp) {

        Map m = new HashMap();
        m.put("bt", btp);
        m.put("class", PreBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        m.put("dep", getSessionController().getDepartment());
        String sql;

        sql = "Select b from Bill b "
                + " where b.retired=false "
                + " and b.billedBill is null "
                + " and b.createdAt between :fd and :td "
                + " and b.billType=:bt"
                + " and b.institution=:ins "
                + " and b.department=:dep "
                + " and type(b)=:class ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            m.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :searchDep )";
            m.put("searchDep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        //     //////
        bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createIssueTable() {
        String sql;
        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.PharmacyTransferIssue);
        sql = "Select b From BilledBill b where b.retired=false and "
                + " b.toDepartment=:dep and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.toStaff.person.name) like :stf )";
            tmp.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromDepartment() != null && !getSearchKeyword().getFromDepartment().trim().equals("")) {
            sql += " and  (upper(b.fromDepartment.name) like :fDep )";
            tmp.put("fDep", "%" + getSearchKeyword().getFromDepartment().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
        logger.debug("Executing query: {}", sql);
        logger.trace("Query parameters: {}", redactPiiFromParameters(tmp));
        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, 50);
        for (Bill b : bills) {
            b.setTmpRefBill(getRefBill(b));

        }

    }

    public void createIssueReport1() {
        String sql;
        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        //tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.PharmacyTransferIssue);
        sql = "Select b From BilledBill b "
                + " where b.retired=false "
                //+ " and b.toDepartment=:dep "
                + " and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.toStaff.person.name) like :stf )";
            tmp.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFrmDepartment() != null) {
            sql += " and b.department=:frmdep";
            tmp.put("frmdep", getSearchKeyword().getFrmDepartment());
        }

        if (getSearchKeyword().getToDepartment() != null) {
            sql += " and b.toDepartment=:tdep";
            tmp.put("tdep", getSearchKeyword().getToDepartment());
        }

        sql += " order by b.createdAt desc  ";

        List<Bill> list = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP);
        bills = new ArrayList<>();
        netTotalValue = 0.0;
        for (Bill b : list) {

            Bill refBill = getActiveRefBill(b);
            if (refBill == null) {
                netTotalValue += b.getNetTotal();
                bills.add(b);
            }
        }

    }

    public void createIssuePharmacyReport() {
        Date startTime = new Date();
//        fetchPharmacyBills(BillType.PharmacyTransferIssue, BillType.PharmacyTransferReceive);
        fetchPharmacyBillsNew(BillType.PharmacyTransferIssue, BillType.PharmacyTransferReceive);

    }

    public void createIssueStoreReport() {
        fetchPharmacyBills(BillType.StoreTransferIssue, BillType.StoreTransferReceive);
    }

    public void createPoNotPharmacyApproveReport() {
        Date startTime = new Date();

        fetchPharmacyBills(BillType.PharmacyOrder, BillType.PharmacyAdjustment);

    }

    public void createPoNotStoreApproveReport() {
        fetchPharmacyBills(BillType.StoreOrder, BillType.StoreOrderApprove);
    }

    public void fetchPharmacyBills(BillType billType, BillType billType2) {
        String sql;
        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        //tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", billType);
        sql = "Select b From BilledBill b "
                + " where b.retired=false "
                //+ " and b.toDepartment=:dep "
                + " and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.toStaff.person.name) like :stf )";
            tmp.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFrmDepartment() != null) {
            sql += " and b.department=:frmdep";
            tmp.put("frmdep", getSearchKeyword().getFrmDepartment());
        }

        if (getSearchKeyword().getToDepartment() != null) {
            sql += " and b.toDepartment=:tdep";
            tmp.put("tdep", getSearchKeyword().getToDepartment());
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and UPPER(b.toDepartment.name) LIKE :dep";
            tmp.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        List<Bill> list = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP);
        bills = new ArrayList<>();
        netTotalValue = 0.0;
        for (Bill b : list) {

            Bill refBill = getActiveRefBillnotApprove(b, billType2);
            if (refBill == null) {
                netTotalValue += b.getNetTotal();
                bills.add(b);
            }
        }

    }

    public void fetchPharmacyBillsNew(BillType billType, BillType billType2) {
        String sql;
        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        //tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", billType);
        sql = "Select b From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false "
                + " and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.toStaff.person.name) like :stf )";
            tmp.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFrmDepartment() != null) {
            sql += " and b.department=:frmdep";
            tmp.put("frmdep", getSearchKeyword().getFrmDepartment());
        }

        if (getSearchKeyword().getToDepartment() != null) {
            sql += " and b.toDepartment=:tdep";
            tmp.put("tdep", getSearchKeyword().getToDepartment());
        }

        sql += " order by b.createdAt desc  ";

        List<Bill> list = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP);
        bills = new ArrayList<>();
        netTotalValue = 0.0;
        for (Bill b : list) {

            Bill refBill = getActiveRefBillnotApprove(b, billType2);
            if (refBill == null) {
                netTotalValue += b.getNetTotal();
                bills.add(b);
            }
        }

    }

    public void createIssueTableStore() {
        Date startTime = new Date();

        String sql;
        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.StoreTransferIssue);
        sql = "Select b From BilledBill b where b.retired=false and "
                + " b.toDepartment=:dep and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.toStaff.person.name) like :stf )";
            tmp.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :dept )";
            tmp.put("dept", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setTmpRefBill(getRefBill(b));

        }

    }

    private Bill getRefBill(Bill b) {
        String sql = "Select b From Bill b where b.retired=false "
                + " and b.cancelled=false and b.billType=:btp and "
                + " b.referenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyTransferReceive);
        return getBillFacade().findFirstByJpql(sql, hm);
    }

    private Bill getActiveRefBill(Bill b) {
        String sql = "Select b From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false"
                + "  and b.billType=:btp"
                + " and b.backwardReferenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyTransferReceive);
        return getBillFacade().findFirstByJpql(sql, hm);
    }

    private Bill getActiveRefBillnotApprove(Bill b, BillType billType) {
        String sql = "Select b From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false"
                + "  and b.billType=:btp"
                + " and b.backwardReferenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", billType);
        return getBillFacade().findFirstByJpql(sql, hm);
    }

    public void makeNull() {
        fromDate = null;
        toDate = null;
        department = null;
        departments = null;
        institution = null;
        paymentMethod = null;
        searchKeyword = null;
        bills = null;
        billSummaryRows = null;
        netTotal = 0.0;
        discount = 0.0;
        grossTotal = 0.0;

        cashTotal = 0.0;
        cardTotal = 0.0;
        chequeTotal = 0.0;
        slipTotal = 0.0;
        totalOfOtherPayments = 0.0;
        billCount = 0.0;
        totalPaying = 0.0;
        serviceDepartment = null;
        billedDepartment = null;
        visitType = null;
        methodType = null;
    }

    public void resetTotals() {
        bills = null;
        billSummaryRows = null;
        netTotal = 0.0;
        discount = 0.0;
        grossTotal = 0.0;

        cashTotal = 0.0;
        cardTotal = 0.0;
        chequeTotal = 0.0;
        slipTotal = 0.0;
        totalOfOtherPayments = 0.0;
        billCount = 0.0;
        totalPaying = 0.0;
    }

    public void processPharmacyBillSearch() {
        if (billTypeAtomic == null) {
            JsfUtil.addErrorMessage("Please Select Bill Type");
            return;
        }
        String jpql;
        Map params = new HashMap();
        jpql = "select b from Bill b "
                + " where b.retired=false "
                + " and b.billTypeAtomic = :billTypeAtomic  "
                + " and b.department=:dep "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            jpql += " and  ((b.deptId) like :billNo )";
            params.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRequestNo() != null && !getSearchKeyword().getRequestNo().trim().equals("")) {
            jpql += " and  ((b.insId) like :requestNo )";
            params.put("requestNo", "%" + getSearchKeyword().getRequestNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            jpql += " and  ((b.netTotal) = :netTotal )";
            params.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            jpql += " and  ((b.fromInstitution.name) like :frmIns )";
            params.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromDepartment() != null && !getSearchKeyword().getFromDepartment().trim().equals("")) {
            jpql += " and  ((b.fromDepartment.name) like :frmDept )";
            params.put("frmDept", "%" + getSearchKeyword().getFromDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            jpql += " and  ((b.toInstitution.name) like :toIns )";
            params.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToDepartment() != null && !getSearchKeyword().getToDepartment().trim().equals("")) {
            jpql += " and  ((b.toDepartment.name) like :toDept )";
            params.put("toDept", "%" + getSearchKeyword().getToDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            jpql += " and  ((b.referenceBill.deptId) like :refId )";
            params.put("refId", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            jpql += " and  ((b.invoiceNumber) like :inv )";
            params.put("inv", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            jpql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " ((bItem.item.name) like :itm ))";
            params.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            jpql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " ((bItem.item.code) like :cde ))";
            params.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        jpql += " order by b.createdAt desc  ";

        params.put("billTypeAtomic", billTypeAtomic);
        params.put("dep", getSessionController().getDepartment());
        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP, maxResult);

    }

    public void createTableByBillType() {
        if (billType == null) {
            JsfUtil.addErrorMessage("Please Select Bill Type");
            return;
        }
        String jpql;
        Map params = new HashMap();
        jpql = "select b from Bill b where b.retired=false and "
                + " (type(b)=:class1 or type(b)=:class2) "
                + " and b.department=:dep and b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate ";
        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            jpql += " and  ((b.deptId) like :billNo )";
            params.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRequestNo() != null && !getSearchKeyword().getRequestNo().trim().equals("")) {
            jpql += " and  ((b.insId) like :requestNo )";
            params.put("requestNo", "%" + getSearchKeyword().getRequestNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            jpql += " and  ((b.netTotal) = :netTotal )";
            params.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            jpql += " and  (upper(b.fromInstitution.name) like :frmIns )";
            params.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromDepartment() != null && !getSearchKeyword().getFromDepartment().trim().equals("")) {
            jpql += " and  (upper(b.fromDepartment.name) like :frmDept )";
            params.put("frmDept", "%" + getSearchKeyword().getFromDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            jpql += " and  (upper(b.toInstitution.name) like :toIns )";
            params.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToDepartment() != null && !getSearchKeyword().getToDepartment().trim().equals("")) {
            jpql += " and  (upper(b.toDepartment.name) like :toDept )";
            params.put("toDept", "%" + getSearchKeyword().getToDepartment().trim().toUpperCase() + "%");
        }
        
        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            jpql += " and  (upper(b.department.name) like :dept )";
            params.put("dept", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            jpql += " and  ((b.referenceBill.deptId) like :refId )";
            params.put("refId", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            jpql += " and  ((b.invoiceNumber) like :inv )";
            params.put("inv", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            jpql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " ((bItem.item.name) like :itm ))";
            params.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            jpql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " ((bItem.item.code) like :cde ))";
            params.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        jpql += " order by b.createdAt desc  ";

        params.put("class1", BilledBill.class);
        params.put("class2", PreBill.class);
        params.put("billType", billType);
        params.put("dep", getSessionController().getDepartment());
        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        //temMap.put("dep", getSessionController().getDepartment());
        bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP, maxResult);

    }

    public void createGRNRegistory() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();

        String url = request.getRequestURL().toString();

        String ipAddress = request.getRemoteAddr();

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setEventStatus("Started");
        long duration;
        Date startTime = new Date();
        auditEvent.setEventDataTime(startTime);
        if (sessionController != null && sessionController.getDepartment() != null) {
            auditEvent.setDepartmentId(sessionController.getDepartment().getId());
        }

        if (sessionController != null && sessionController.getInstitution() != null) {
            auditEvent.setInstitutionId(sessionController.getInstitution().getId());
        }
        if (sessionController != null && sessionController.getLoggedUser() != null) {
            auditEvent.setWebUserId(sessionController.getLoggedUser().getId());
        }
        auditEvent.setUrl(url);
        auditEvent.setIpAddress(ipAddress);
        auditEvent.setEventTrigger("createGRNRegistory()");
        auditEventApplicationController.logAuditEvent(auditEvent);

        if (getReportKeyWord().getDepartment() == null) {
            JsfUtil.addErrorMessage("Select Departmrnt.");
            return;
        }
        String sql;
        Map m = new HashMap();

        sql = "select b from Bill b where b.retired=false and "
                + " (type(b)=:class1 or type(b)=:class2) "
                + " and b.department=:dep and b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getReportKeyWord().getInstitution() != null) {
            sql += " and  ((b.fromInstitution.name) like :frmIns )";
            m.put("frmIns", getReportKeyWord().getInstitution());
        }

        if (getReportKeyWord().getItem() != null) {
            sql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false "
                    + " and bItem.item.name=:itm ))";
            m.put("itm", getReportKeyWord().getItem());
        }

        sql += " order by b.createdAt desc  ";

        m.put("class1", BilledBill.class);
        m.put("class2", PreBill.class);
        m.put("billType", BillType.PharmacyGrnBill);
        m.put("dep", getReportKeyWord().getDepartment());
        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());
        //temMap.put("dep", getSessionController().getDepartment());
        bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);

        Date endTime = new Date();
        duration = endTime.getTime() - startTime.getTime();
        auditEvent.setEventDuration(duration);
        auditEvent.setEventStatus("Completed");
        auditEventApplicationController.logAuditEvent(auditEvent);

    }

    public void createTableByBillTypeAllDepartment() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where b.retired=false and "
                + " (type(b)=:class1 or type(b)=:class2) "
                + " and  b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRequestNo() != null && !getSearchKeyword().getRequestNo().trim().equals("")) {
            sql += " and ((b.insId) like :requestNo)";
            temMap.put("requestNo", "%" + getSearchKeyword().getRequestNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  ((b.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  ((b.toInstitution.name) like :toIns )";
            temMap.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  ((b.referenceBill.deptId) like :refId )";
            temMap.put("refId", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  ((b.invoiceNumber) like :inv )";
            temMap.put("inv", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " ((bItem.item.name) like :itm ))";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " ((bItem.item.code) like :cde ))";
            temMap.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("class1", BilledBill.class);
        temMap.put("class2", PreBill.class);
        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        //temMap.put("dep", getSessionController().getDepartment());
        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, maxResult);

    }

    /**
     * Legacy method that loads full Bill entities for pharmacy transfer
     * requests. Replaced by {@link #createRequestTableDto()} which uses DTOs
     * for better performance. Left for backward compatibility.
     */
    @Deprecated
    public void createRequestTable() {
        Date startTime = new Date();
        BillClassType[] billClassTypes = {BillClassType.CancelledBill, BillClassType.RefundBill};
        List<BillClassType> bct = Arrays.asList(billClassTypes);

        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("toDep", getSessionController().getDepartment());
        tmp.put("bct", bct);
        tmp.put("bTp", BillType.PharmacyTransferRequest);
        tmp.put("billTypeAtomic", billTypeAtomic.PHARMACY_TRANSFER_REQUEST);

        sql = "Select b From Bill b where "
                + " b.retired=false and  b.toDepartment=:toDep"
                + " and b.billClassType not in :bct "
                + " and b.billTypeAtomic = :billTypeAtomic"
                + " and b.billType= :bTp and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :dep )";
            tmp.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getIssudBills(b));
        }

    }

    /**
     * Populates {@link #transferRequestDtos} using DTO projections. Each
     * request DTO additionally loads its issued bill DTOs for display.
     */
    public void createRequestTableDto() {
        String jpql = "select new com.divudi.core.data.dto.PharmacyTransferRequestListDTO("
                + " b.id, "
                + " b.deptId, "
                + " b.createdAt, "
                + " b.department.name, "
                + " b.creater.webUserPerson.name, "
                + " b.completed, "
                + " b.fullyIssued)"
                + " from Bill b"
                + " where b.retired=false "
                + " and b.cancelled=false "
                + " and  b.toDepartment=:toDep"
                + " and b.billTypeAtomic = :billTypeAtomic"
                + " and b.createdAt between :fromDate and :toDate";
        HashMap<String, Object> params = new HashMap<>();
        params.put("fromDate", getFromDate());
        params.put("toDate", getToDate());
        params.put("toDep", getSessionController().getDepartment());
        params.put("billTypeAtomic", BillTypeAtomic.PHARMACY_TRANSFER_REQUEST);


        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            jpql += " and ((b.deptId) like :billNo)";
            params.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            jpql += " and ((b.department.name) like :dep)";
            params.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        jpql += " order by b.createdAt desc";
        

        System.out.println("=== DEBUG: createRequestTableDto ===");
        System.out.println("JPQL: " + jpql);
        System.out.println("Params: " + params);
        System.out.println("From Date: " + getFromDate());
        System.out.println("To Date: " + getToDate());
        System.out.println("Department: " + getSessionController().getDepartment());
        
        transferRequestDtos = (List<PharmacyTransferRequestListDTO>) billFacade.findLightsByJpql(jpql, params, TemporalType.TIMESTAMP, 50);
        
        
        if (transferRequestDtos != null) {
            for (PharmacyTransferRequestListDTO dto : transferRequestDtos) {
                dto.setIssuedBills(fetchIssuedBillDtos(dto.getBillId()));
                // fullyIssued field is now fetched directly from database in the JPQL query above
            }
        }
    }

    /**
     * Fetches issued bills for a given transfer request using DTO projections.
     *
     * @param requestId id of the transfer request bill
     * @return list of issue DTOs
     */
    public List<PharmacyTransferRequestIssueDTO> fetchIssuedBillDtos(Long requestId) {
        String jpql = "select new com.divudi.core.data.dto.PharmacyTransferRequestIssueDTO("
                + " b.id, "
                + " b.deptId, "
                + " b.createdAt, "
                + " b.creater.webUserPerson.name, "
                + " ts.person.name, "
                + " b.netTotal)"
                + " from Bill b "
                + " left join b.toStaff ts"
                + " where b.retired=false "
                + " and b.cancelled=false "
                + " and b.billTypeAtomic=:bta"
                + " and (b.referenceBill.id=:rid or b.backwardReferenceBill.id=:rid)";
        HashMap<String, Object> params = new HashMap<>();
        params.put("bta", BillTypeAtomic.PHARMACY_ISSUE);
        params.put("rid", requestId);
        
        
        List<PharmacyTransferRequestIssueDTO> result = (List<PharmacyTransferRequestIssueDTO>) billFacade.findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);
        
        
        return result;
    }
    

    public void createInwardBHTRequestTable() {
        Date startTime = new Date();
        BillClassType[] billClassTypes = {BillClassType.CancelledBill, BillClassType.RefundBill};
        List<BillClassType> bct = Arrays.asList(billClassTypes);

        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bct", bct);
        tmp.put("bTp", BillType.InwardPharmacyRequest);

        sql = "Select b From Bill b where "
                + " b.retired=false and  b.toDepartment=:dep "
                + " and b.billClassType not in :bct"
                + " and b.billType= :bTp and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and (((b.insId) like :billNo ) or ((b.deptId) like :billNo )) ";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            tmp.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getBHTIssudBills(b));
        }

    }

    public void createInwardBHTForIssueTableAll() {
        createInwardBHTForIssueTable(null);
    }

    public void createInwardBHTForNotIssueTable() {
        createInwardBHTForIssueTable(true);
    }

    public String navigateToIssueForBhtRequests() {
        bills = createInwardPharmacyRequests();
        return "/ward/issue_for_bht_request_list";
    }

    public List<Bill> createInwardPharmacyRequests() {
        String sql;
        HashMap tmp = new HashMap();
        tmp.put("admission", getPatientEncounter());
        tmp.put("bTp", BillType.InwardPharmacyRequest);
        sql = "Select b "
                + " From Bill b "
                + " where b.retired=false "
                + " and  b.toDepartment=:toDep"
                + " and b.billType=:bTp "
                + " and b.patientEncounter=:admission ";
        sql += " order by b.createdAt desc  ";
        return getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, 100);
    }

    public void createInwardBHTForIssueOnlyTable() {
        createInwardBHTForIssueTable(false);
    }

    public void createInwardBHTForIssueTable(Boolean bool) {
        Date startTime = new Date();
        BillClassType[] billClassTypes = {BillClassType.CancelledBill, BillClassType.RefundBill};
        List<BillClassType> bct = Arrays.asList(billClassTypes);

        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("toDep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.InwardPharmacyRequest);
        tmp.put("bct", bct);

        sql = "Select b From Bill b where "
                + " b.retired=false and  b.toDepartment=:toDep"
                + " and b.billClassType not in :bct"
                + " and b.billType= :bTp and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and (((b.insId) like :billNo ) or ((b.deptId) like :billNo )) ";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            tmp.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, 100);

        for (Bill b : bills) {
            b.setListOfBill(getBHTIssudBills(b));
        }

        if (bool != null) {
            List<Bill> bs = new ArrayList<>();
            for (Bill b : bills) {
                if (pharmacySaleBhtController.checkBillComponent(b)) {
                    bs.add(b);
                }
            }
            if (bool) {
                bills = bs;
            } else {
                bills.removeAll(bs);
            }
        }

    }

    public long createInwardBHTForIssueBillCount() {
        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("toDep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.InwardPharmacyRequest);

        sql = "Select COUNT(b) From Bill b "
                + " where b.retired=false "
                + " and b.toDepartment=:toDep "
                + " and b.cancelled=false "
                + " and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        long count = 0l;
        count = getBillFacade().countByJpql(sql, tmp, TemporalType.TIMESTAMP);

        return count;

    }

    public void createRequestTableStore() {
        Date startTime = new Date();

        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("toDep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.StoreTransferRequest);

        sql = "Select b From Bill b where "
                + " b.retired=false and  b.toDepartment=:toDep"
                + " and b.billType= :bTp and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :dep )";
            tmp.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP);

        for (Bill b : bills) {
            b.setListOfBill(getIssudBills(b));
        }

    }

    public void createListToCashRecieve() {
        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("toWeb", getSessionController().getLoggedUser());
        tmp.put("bTp", BillType.CashOut);

        sql = "Select b From Bill b where "
                + " b.retired=false "
                + " and  b.toWebUser=:toWeb"
                + " and b.billType= :bTp"
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) like :net )";
            tmp.put("net", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPersonName() != null && !getSearchKeyword().getPersonName().trim().equals("")) {
            sql += " and  ((b.fromWebUser.webUserPerson.name) like :dep )";
            tmp.put("dep", "%" + getSearchKeyword().getPersonName().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyBillItemTable() {
        Date startTime = new Date();

        createPharmacyBillItemTable(BillType.PharmacyPre, BillType.PharmacySale);

    }

    public void createPharmacyWholeBillItemTable() {
        createPharmacyBillItemTable(BillType.PharmacyWholesalePre, BillType.PharmacyWholeSale);
    }

    public void createPharmacyBillItemTable(BillType billType, BillType refBillType) {
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", billType);
        m.put("rBType", refBillType);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);
        m.put("rClass", BilledBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class and type(bi.bill.referenceBill)=:rClass"
                + " and bi.bill.institution=:ins"
                + " and bi.bill.billType=:bType and "
                + " bi.bill.referenceBill.billType=:rBType "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((bi.bill.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((bi.netValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  ((bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyIssueBillItemTable() {
        Map<String, Object> m = new HashMap<>();

        List<BillTypeAtomic> bts = new ArrayList<>();
        bts.add(BillTypeAtomic.PHARMACY_DISPOSAL_ISSUE);
        bts.add(BillTypeAtomic.PHARMACY_DISPOSAL_ISSUE_CANCELLED);
        bts.add(BillTypeAtomic.PHARMACY_DISPOSAL_ISSUE_RETURN);

        StringBuilder jpql = new StringBuilder();
        jpql.append("select new com.divudi.core.data.dto.PharmacyItemPurchaseDTO(");
        jpql.append("bi.bill.id, bi.bill.deptId, bi.bill.createdAt, ");
        jpql.append("bi.bill.institution.name, bi.bill.department.name, bi.bill.toDepartment.name, ");
        jpql.append("bi.bill.billType, bi.bill.total, bi.bill.netTotal, bi.bill.discount, ");
        jpql.append("bi.item.id, bi.item.name, bi.item.code, ");
        jpql.append("bi.qty, bi.pharmaceuticalBillItem.freeQty) ");
        jpql.append(" from BillItem bi");
        jpql.append(" where bi.bill.billTypeAtomic in :bts");
        jpql.append(" and bi.bill.createdAt between :fd and :td");
        jpql.append(" and bi.bill.institution=:ins");

        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("bts", bts);
        m.put("ins", getSessionController().getInstitution());

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            jpql.append(" and bi.bill.toDepartment.name like :deptName");
            m.put("deptName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            jpql.append(" and bi.bill.deptId like :billNo");
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            try {
                Double total = Double.parseDouble(getSearchKeyword().getTotal().trim());
                jpql.append(" and bi.netValue = :total");
                m.put("total", total);
            } catch (NumberFormatException e) {
                // Ignore invalid number format
            }
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            jpql.append(" and bi.item.name like :itm");
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            jpql.append(" and bi.item.code like :cde");
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        jpql.append(" order by bi.bill.createdAt desc");

        disposalIssueBillItemDtos = (List<PharmacyItemPurchaseDTO>) getBillFacade().findLightsByJpql(jpql.toString(), m, TemporalType.TIMESTAMP);
    }

    public void createStoreIssueBillItemTable() {
        Date startTime = new Date();
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.StoreIssue);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class "
                + " and bi.bill.institution=:ins"
                + " and bi.bill.billType=:bType "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getToDepartment() != null && !getSearchKeyword().getToDepartment().trim().equals("")) {
            sql += " and  ((bi.bill.toDepartment.name) like :todept )";
            m.put("todept", "%" + getSearchKeyword().getToDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromDepartment() != null && !getSearchKeyword().getFromDepartment().trim().equals("")) {
            sql += " and  ((bi.bill.fromDepartment.name) like :fromdept )";
            m.put("fromdept", "%" + getSearchKeyword().getFromDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((bi.netValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  ((bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createErronousStoreIssueBillItemTable() {

        Date startTime = new Date();
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.StoreIssue);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class "
                + " and bi.bill.institution=:ins "
                + " and bi.bill.billType=:bType "
                + " and bi.item.retired=true "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((bi.bill.toDepartment.name) like :deptName )";
            m.put("deptName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((bi.netValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  ((bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyAdjustmentBillItemTable() {
        Date startTime = new Date();
        String sql;
        Map<String, Object> m = new HashMap<>();

        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);

        // Set bill types individually
        m.put("bType1", BillType.PharmacyAdjustment);
        m.put("bType2", BillType.PharmacyAdjustmentDepartmentStock);
        m.put("bType3", BillType.PharmacyAdjustmentDepartmentSingleStock);
        m.put("bType4", BillType.PharmacyAdjustmentStaffStock);
        m.put("bType5", BillType.PharmacyAdjustmentSaleRate);
        m.put("bType6", BillType.PharmacyAdjustmentWholeSaleRate);
        m.put("bType7", BillType.PharmacyAdjustmentPurchaseRate);
        m.put("bType8", BillType.PharmacyAdjustmentExpiryDate);

        sql = "select bi from BillItem bi"
                + " where type(bi.bill) = :class "
                + " and bi.bill.institution = :ins"
                + " and (bi.bill.billType = :bType1"
                + " or bi.bill.billType = :bType2"
                + " or bi.bill.billType = :bType3"
                + " or bi.bill.billType = :bType4"
                + " or bi.bill.billType = :bType5"
                + " or bi.bill.billType = :bType6"
                + " or bi.bill.billType = :bType7"
                + " or bi.bill.billType = :bType8)"
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().isEmpty()) {
            sql += " and (bi.bill.deptId) like :billNo ";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().isEmpty()) {
            sql += " and (bi.item.name) like :itm ";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().isEmpty()) {
            sql += " and (bi.item.code) like :cde ";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc";

        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public void createPharmacyAdjustmentBillItemTableForStockTaking() {
        fetchAdjustmentBillItemTableForStockTaking(BillType.PharmacyAdjustment);
    }

    public void createStoreAdjustmentBillItemTableForStockTaking() {
        fetchAdjustmentBillItemTableForStockTaking(BillType.StoreAdjustment);
    }

    private void fetchAdjustmentBillItemTableForStockTaking(BillType billType) {
        pharmacyAdjustmentRows = new ArrayList<>();

        if (getReportKeyWord().getDepartment() == null) {
            JsfUtil.addErrorMessage("Please Select Department");
            return;
        }
        String sql;
        Map m = new HashMap();

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class "
                + " and bi.bill.institution=:ins "
                + " and bi.bill.department=:dep "
                + " and bi.bill.billType=:bType  "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getReportKeyWord().getCategory() != null) {
            sql += " and bi.item.category=:cat ";
            m.put("cat", getReportKeyWord().getCategory());
        }

        sql += " order by bi.item.name, bi.pharmaceuticalBillItem.stock.itemBatch.id ";

        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", billType);
        m.put("ins", getSessionController().getInstitution());
        m.put("dep", getReportKeyWord().getDepartment());
        m.put("class", PreBill.class);

        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);

        dueTotal = 0.0;
        doneTotal = 0.0;
        netTotal = 0.0;

        if (getReportKeyWord().isAdditionalDetails()) {
            createAdjustmentRow(null, null, billItems);
        } else {
            m = new HashMap();
            sql = "select s from Stock s "
                    + " where s.department=:d "
                    + " and s.stock>=0 "
                    + " and s.itemBatch.item.departmentType!=:depty ";

            if (getReportKeyWord().getCategory() != null) {
                sql += " and s.itemBatch.item.category=:cat ";
                m.put("cat", getReportKeyWord().getCategory());
            }

            sql += " order by s.itemBatch.item.name,s.itemBatch.id ";
            m.put("d", getReportKeyWord().getDepartment());
            m.put("depty", DepartmentType.Inventry);
            List<Stock> stocks = getStockFacade().findByJpql(sql, m);
            for (Stock s : stocks) {
                boolean flag = true;
                int i = 1;
                for (BillItem bi : billItems) {
                    if (s.equals(bi.getPharmaceuticalBillItem().getStock())) {
                        createAdjustmentRow(null, bi, null);
                        flag = false;
                        i++;
                        break;
                    }
                    i++;
                }
                if (flag) {
                    if (s.getStock() > 0) {
                        createAdjustmentRow(s, null, null);
                    } else {
                    }
                }
            }

        }

    }

    public void createAdjustmentRow(Stock s, BillItem bi, List<BillItem> billItems) {
        PharmacyAdjustmentRow row;
        if (s != null) {
            row = new PharmacyAdjustmentRow(s.getItemBatch().getItem(),
                    s.getItemBatch().getPurcahseRate(),
                    s.getItemBatch().getRetailsaleRate(),
                    s.getStock(),
                    s.getStock(),
                    0,
                    s.getItemBatch().getBatchNo(),
                    s.getItemBatch().getDateOfExpire());
            dueTotal += row.getBefoerVal();
            doneTotal += row.getAfterVal();
            netTotal += row.getAdjusetedVal();
            pharmacyAdjustmentRows.add(row);
        }
        if (bi != null) {
            row = new PharmacyAdjustmentRow(bi.getItem(),
                    bi.getPharmaceuticalBillItem().getStock().getItemBatch().getPurcahseRate(),
                    bi.getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate(),
                    bi.getPharmaceuticalBillItem().getStockHistory().getStockQty(),
                    bi.getQty(),
                    bi.getQty() - bi.getPharmaceuticalBillItem().getStockHistory().getStockQty(),
                    bi.getPharmaceuticalBillItem().getStock().getItemBatch().getBatchNo(),
                    bi.getPharmaceuticalBillItem().getStock().getItemBatch().getDateOfExpire());
            dueTotal += row.getBefoerVal();
            doneTotal += row.getAfterVal();
            netTotal += row.getAdjusetedVal();
            pharmacyAdjustmentRows.add(row);
        }
        if (billItems != null) {
            for (BillItem bii : billItems) {
                try {
                    row = new PharmacyAdjustmentRow(bii.getItem(),
                            bii.getPharmaceuticalBillItem().getStock().getItemBatch().getPurcahseRate(),
                            bii.getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate(),
                            bii.getPharmaceuticalBillItem().getStockHistory().getStockQty(),
                            bii.getQty(),
                            bii.getQty() - bii.getPharmaceuticalBillItem().getStockHistory().getStockQty(),
                            bii.getPharmaceuticalBillItem().getStock().getItemBatch().getBatchNo(),
                            bii.getPharmaceuticalBillItem().getStock().getItemBatch().getDateOfExpire());
                    dueTotal += row.getBefoerVal();
                    doneTotal += row.getAfterVal();
                    netTotal += row.getAdjusetedVal();
                    pharmacyAdjustmentRows.add(row);
                } catch (Exception e) {
                }
            }
        }
    }

    public void createStoreAdjustmentBillItemTable() {
        Date startTime = new Date();
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.StoreAdjustment);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class "
                + " and bi.bill.institution=:ins"
                + " and bi.bill.billType=:bType  "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  ((bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public void createPaymentHistoryTable() {
        bills = new ArrayList<>();  // Initialize to avoid null issues
        List<BillTypeAtomic> bta = new ArrayList<>();
        bta.add(BillTypeAtomic.SUPPLEMENTARY_INCOME);
        bta.add(BillTypeAtomic.SUPPLEMENTARY_INCOME_CANCELLED);
        bta.add(BillTypeAtomic.OPERATIONAL_EXPENSES);
        bta.add(BillTypeAtomic.OPERATIONAL_EXPENSES_CANCELLED);

        String sql = "select bi "
                + " from Bill bi "
                + " where bi.retired=:ret "
                + " and bi.billTypeAtomic IN :bTypeList"
                + " and bi.createdAt BETWEEN :fd and :td ";
        Map<String, Object> m = new HashMap<>();
        m.put("bTypeList", bta);
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);

        bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);
    }

    public void createDrawerAdjustmentTable() {
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.DrawerAdjustment);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", BilledBill.class);

        sql = "select bi from Bill bi"
                + " where  type(bi)=:class "
                + " and bi.institution=:ins"
                + " and bi.billType=:bType  "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((bi.insId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyBillItemTableIssue() {
        createBillItemTableBht(BillType.StoreIssue);
    }

    public void createPharmacyBillItemTableBht() {
        Date startTime = new Date();

        createBillItemTableBht(BillType.PharmacyBhtPre);

    }

    public void createStoreBillItemTableBht() {
        createBillItemTableBht(BillType.StoreBhtPre);
    }

    public List<BillItem> getBillItem() {
        return billItem;
    }

    public void setBillItem(List<BillItem> billItem) {
        this.billItem = billItem;
    }

    public void createBillItemTableBht(BillType btp) {
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", btp);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class "
                + " and bi.bill.institution=:ins"
                + " and bi.bill.billType=:bType and "
                + " bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getFrmDepartment() != null) {
            sql += " and bi.bill.department=:dep";
            m.put("dep", getSearchKeyword().getFrmDepartment());
        }

        if (getSearchKeyword().getFrmDepartment() != null) {
            sql += " and bi.bill.department=:dep";
            m.put("dep", getSearchKeyword().getFrmDepartment());
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((bi.bill.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((bi.netValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  ((bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((bi.bill.patientEncounter.bhtNo) like :bht )";
            m.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getAdmissionType() != null) {
            sql += " and bi.bill.patientEncounter.admissionType=:admissionType ";
            m.put("admissionType", getSearchKeyword().getAdmissionType());
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createPoRequestedAndApprovedPharmacy() {
        Date startTime = new Date();

        createPoRequestedAndApproved(InstitutionType.Dealer, BillType.PharmacyOrder);

    }

    public void createPoRequestedAndApprovedStore() {
        Date startTime = new Date();
        createPoRequestedAndApproved(InstitutionType.StoreDealor, BillType.StoreOrder);

    }

    public void createPoRequestedAndApproved(InstitutionType institutionType, BillType bt) {
        bills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.billType= :bTp  ";

        sql += createKeySql(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("insTp", institutionType);
        tmp.put("bTp", bt);
        bills = getBillFacade().findByJpqlWithoutCache(sql, tmp, TemporalType.TIMESTAMP, maxResult);

    }

    public void createShiftShortageBillsTable() {
        bills = null;
        bills = new ArrayList<>();
        String sql;
        Map m = new HashMap();

        sql = "Select b From Bill b where "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.billTypeAtomic=:bTA "
                + " and b.billType=:bT ";

        m.put("fromDate", fromDate);
        m.put("toDate", toDate);
        m.put("bTA", BillTypeAtomic.FUND_SHIFT_SHORTAGE_BILL);
        m.put("bT", BillType.ShiftShortage);
        bills = getBillFacade().findByJpql(sql, m);

        if (bills == null || bills.isEmpty()) {
        } else {
        }

    }

    public void createApprovedPharmacy() {
        Date startTime = new Date();

        createApproved(InstitutionType.Dealer, BillType.PharmacyOrder);

    }

    public void createApprovedStore() {
        Date startTime = new Date();
        createApproved(InstitutionType.StoreDealor, BillType.StoreOrder);

    }

    public void createApproved(InstitutionType institutionType, BillType bt) {
        bills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where"
                + " b.referenceBill.creater is not null "
                + " and b.referenceBill.cancelled=false "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.billType= :bTp  ";

        sql += createKeySql(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("insTp", institutionType);
        tmp.put("bTp", bt);
        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, maxResult);

    }

    public void createNotApprovedPharmacy() {
        Date startTime = new Date();
        createNotApproved(InstitutionType.Dealer, BillType.PharmacyOrder);

    }

    public void fillOnlySavedPharmacyPo() {
        bills = null;
        HashMap tmp = new HashMap();
        String sql;
        sql = "Select b From BilledBill b where "
                + " b.referenceBill is null "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.billType= :bTp  "
                + " and b.checkedBy is null";

        sql += createKeySqlSearchForPoCancel(tmp);

        sql += " order by b.createdAt desc  ";
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("insTp", InstitutionType.Dealer);
        tmp.put("bTp", BillType.PharmacyOrder);

        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, maxResult);

    }

    public void fillOnlyFinalizedPharmacyPo() {
        bills = null;
        HashMap tmp = new HashMap();
        String sql;
        sql = "Select b From BilledBill b where "
                + " b.referenceBill is null "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.billType= :bTp  "
                + " and b.checkedBy is not null";
        sql += createKeySqlSearchForPoCancel(tmp);

        sql += " order by b.createdAt desc  ";
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("insTp", InstitutionType.Dealer);
        tmp.put("bTp", BillType.PharmacyOrder);

        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, maxResult);

    }

    public void fillOnlySavedPharmacyTransferRequest() {
        bills = null;
        HashMap tmp = new HashMap();
        String sql;
        sql = "Select b From Bill b where "
                + " b.checkedBy is null "
                + " and b.institution = :ins "
                + " and b.fromDepartment = :fromDep "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.billType= :bTp";

        sql += " order by b.createdAt desc  ";
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("ins", sessionController.getInstitution());
        tmp.put("fromDep", sessionController.getDepartment());
        tmp.put("bTp", BillType.PharmacyTransferRequest);

        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, maxResult);

    }

    public void createNotApprovedStore() {
        Date startTime = new Date();

        createNotApproved(InstitutionType.StoreDealor, BillType.StoreOrder);

    }

    public void createNotApproved(InstitutionType institutionType, BillType bt) {
        bills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where"
                + "  b.referenceBill is null  "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false"
                + " and b.billType= :bTp ";

        sql += createKeySql(tmp);
        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("insTp", institutionType);
        tmp.put("bTp", bt);
        List<Bill> lst1 = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, maxResult);

        sql = "Select b From BilledBill b where "
                + " b.referenceBill.creater is null "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.billType= :bTp  ";

        sql += createKeySql(tmp);
        sql += " order by b.createdAt desc  ";

        List<Bill> lst2 = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, maxResult);

        sql = "Select b From BilledBill b where "
                + " b.referenceBill.creater is not null "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.referenceBill.cancelled=true "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false"
                + " and b.billType= :bTp ";

        sql += createKeySql(tmp);
        sql += " order by b.createdAt desc  ";

        List<Bill> lst3 = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, maxResult);

        lst1.addAll(lst2);
        lst1.addAll(lst3);

        bills = lst1;

    }

    private String createKeySqlSearchForPoCancel(HashMap tmp) {
        String sql = "";
        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  ((b.toInstitution.name) like :toIns )";
            tmp.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCreator() != null && !getSearchKeyword().getCreator().trim().equals("")) {
            sql += " and  ((b.creater.webUserPerson.name) like :crt )";
            tmp.put("crt", "%" + getSearchKeyword().getCreator().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :crt )";
            tmp.put("crt", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) like :reqTotal )";
            tmp.put("reqTotal", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.referenceBill.netTotal) like :appTotal )";
            tmp.put("appTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        return sql;

    }

    private String createKeySql(HashMap tmp) {
        String sql = "";

        if (getSearchKeyword().getRequestNo() != null && !getSearchKeyword().getRequestNo().trim().equals("")) {
            sql += " and  ((b.qutationNumber) like :qutNo )";
            tmp.put("qutNo", "%" + getSearchKeyword().getRequestNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  ((b.toInstitution.name) like :toIns )";
            tmp.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCreator() != null && !getSearchKeyword().getCreator().trim().equals("")) {
            sql += " and  ((b.creater.webUserPerson.name) like :crt )";
            tmp.put("crt", "%" + getSearchKeyword().getCreator().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  ((b.department.name) like :crt )";
            tmp.put("crt", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  ((b.referenceBill.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) like :reqTotal )";
            tmp.put("reqTotal", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.referenceBill.netTotal) like :appTotal )";
            tmp.put("appTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        return sql;

    }

    private String keysForGrnReturnByBillItem(HashMap tmp) {
        String sql = "";
        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((bi.bill.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  ((bi.bill.invoiceNumber) like :invoice )";
            tmp.put("invoice", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  ((bi.bill.referenceBill.deptId) like :refNo )";
            tmp.put("refNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  ((bi.bill.fromInstitution.name) like :frmIns )";
            tmp.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((bi.bill.referenceBill.netTotal) like :total )";
            tmp.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((bi.bill.netTotal) = :netTotal )";
            tmp.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        return sql;
    }

    private String keysForGrnReturn(HashMap tmp) {
        String sql = "";
        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  ((b.invoiceNumber) like :invoice )";
            tmp.put("invoice", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  ((b.referenceBill.deptId) like :refNo )";
            tmp.put("refNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  ((b.fromInstitution.name) like :frmIns )";
            tmp.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.referenceBill.netTotal) like :total )";
            tmp.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            tmp.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        return sql;
    }

    public void createGrnTableStore() {
        bills = null;
        String sql;
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp "
                + " and b.institution=:ins and"
                + " b.createdAt between :fromDate and :toDate ";

        sql += keysForGrnReturn(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", BillType.StoreGrnBill);
        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getReturnBill(b, BillType.StoreGrnReturn));
        }

    }

    public void createGrnTableAllIns() {
        bills = null;
        String jpql;
        HashMap params = new HashMap();

        if (getSearchKeyword().getItem() == null) {
            jpql = "Select b From BilledBill b "
                    + " where  b.retired=false "
                    + " and b.billTypeAtomic = :bTp "
                    + " and b.createdAt between :fromDate and :toDate ";
            jpql += keysForGrnReturn(params);
            jpql += " order by b.createdAt desc  ";
            params.put("toDate", getToDate());
            params.put("fromDate", getFromDate());
            params.put("bTp", BillTypeAtomic.PHARMACY_GRN);
            bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP, 50);
        } else {
            jpql = "Select DISTINCT(bi.bill) From BillItem bi"
                    + " where bi.retired=false and bi.bill.billTypeAtomic = :bTp "
                    + " and bi.createdAt between :fromDate and :toDate "
                    + " and bi.item=:item ";
            jpql += keysForGrnReturnByBillItem(params);
            jpql += " order by bi.createdAt desc  ";
            params.put("toDate", getToDate());
            params.put("fromDate", getFromDate());
            params.put("item", getSearchKeyword().getItem());
            params.put("bTp", BillTypeAtomic.PHARMACY_GRN);
            bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP, 50);
        }
        for (Bill b : bills) {
            List<BillTypeAtomic> types = new ArrayList<>();
            types.add(BillTypeAtomic.PHARMACY_GRN_RETURN);
            types.add(BillTypeAtomic.PHARMACY_GRN_REFUND);
            types.add(BillTypeAtomic.PHARMACY_GRN_RETURN_CANCELLATION);
            b.setListOfBill(getReturnBill(b, types));
        }

    }

    public void createGrnTable() {
        bills = null;
        String jpql;
        HashMap params = new HashMap();

        if (getSearchKeyword().getItem() == null) {
            jpql = "Select b From Bill b "
                    + " where  b.retired=false "
                    + " and b.billTypeAtomic = :bTp "
                    + " and b.institution=:ins "
                    + " and b.createdAt between :fromDate and :toDate ";
            jpql += keysForGrnReturn(params);
            jpql += " order by b.createdAt desc  ";
            params.put("toDate", getToDate());
            params.put("fromDate", getFromDate());
            params.put("ins", getSessionController().getInstitution());
            params.put("bTp", BillTypeAtomic.PHARMACY_GRN);
            bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP, 50);
        } else {
            jpql = "Select DISTINCT(bi.bill) From BillItem bi"
                    + " where bi.retired=false and bi.bill.billTypeAtomic = :bTp "
                    + " and bi.bill.institution=:ins "
                    + " and bi.createdAt between :fromDate and :toDate "
                    + " and bi.item=:item ";
            jpql += keysForGrnReturnByBillItem(params);
            jpql += " order by bi.createdAt desc  ";
            params.put("toDate", getToDate());
            params.put("fromDate", getFromDate());
            params.put("ins", getSessionController().getInstitution());
            params.put("item", getSearchKeyword().getItem());
            params.put("bTp", BillTypeAtomic.PHARMACY_GRN);
            bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP, 50);
        }
        for (Bill b : bills) {
            List<BillTypeAtomic> types = new ArrayList<>();
            types.add(BillTypeAtomic.PHARMACY_GRN_RETURN);
            types.add(BillTypeAtomic.PHARMACY_GRN_REFUND);
            types.add(BillTypeAtomic.PHARMACY_GRN_RETURN_CANCELLATION);
            b.setListOfBill(getReturnBill(b, types));
        }

    }

    public void createGrnTableAllInsStore() {
        String sql;
        HashMap tmp = new HashMap();
        sql = "Select b From Bill b where  b.retired=false and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";
        sql += keysForGrnReturn(tmp);
        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        // tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", BillType.StoreGrnBill);
        bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getReturnBill(b, BillType.StoreGrnReturn));
        }

    }

    public void createGrnTableToFinalize() {
        bills = null;
        String jpql = "Select b From Bill b "
                + " where b.retired=false "
                + " and b.billTypeAtomic = :bTp "
                + " and b.completed = :completed "
                + " and b.createdAt between :fromDate and :toDate "
                + " order by b.createdAt desc";
        
        HashMap params = new HashMap();
        params.put("bTp", BillTypeAtomic.PHARMACY_GRN_PRE);
        params.put("completed", false);
        params.put("fromDate", getFromDate());
        params.put("toDate", getToDate());
        
        bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP, 50);
    }

    public void createGrnTableToApprove() {
        bills = null;
        String jpql;
        HashMap params = new HashMap();
        
        SearchKeyword sk = getSearchKeyword();
        if (sk == null || sk.getItem() == null) {
            jpql = "Select b From Bill b "
                    + " where  b.retired=false "
                    + " and b.billTypeAtomic = :bTp "
                    + " and b.institution=:ins "
                    + " and b.completed = :completed "
                    + " and b.createdAt between :fromDate and :toDate ";
            jpql += keysForGrnReturn(params);
            jpql += " order by b.createdAt desc  ";
            params.put("toDate", getToDate());
            params.put("fromDate", getFromDate());
            params.put("ins", getSessionController().getInstitution());
            params.put("bTp", BillTypeAtomic.PHARMACY_GRN_PRE);
            params.put("completed", true);
            bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP, 50);
        } else {
            jpql = "Select DISTINCT(bi.bill) From BillItem bi"
                    + " where bi.retired=false and bi.bill.billTypeAtomic = :bTp "
                    + " and bi.bill.institution=:ins and bi.bill.completed = :completed "
                    + " and bi.bill.createdAt between :fromDate and :toDate "
                    + " and bi.item=:item ";
            jpql += " order by bi.bill.createdAt desc  ";
            params.put("toDate", getToDate());
            params.put("fromDate", getFromDate());
            params.put("ins", getSessionController().getInstitution());
            params.put("bTp", BillTypeAtomic.PHARMACY_GRN_PRE);
            params.put("completed", true);
            params.put("item", sk.getItem());
            bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP, 50);
        }
    }

    private List<Bill> getReturnBill(Bill b, BillType bt) {
        String sql = "Select b From BilledBill b where b.retired=false and b.creater is not null"
                + " and  b.billType=:btp and "
                + " b.referenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", bt);
        return getBillFacade().findByJpql(sql, hm);
    }

    private List<Bill> getReturnBill(Bill b, BillTypeAtomic bt) {
        String jpql = "Select b "
                + " From BilledBill b "
                + " where b.retired=false "
                + " and b.creater is not null"
                + " and  b.billTypeAtomic=:btp "
                + " and b.referenceBill=:ref";
        HashMap params = new HashMap();
        params.put("ref", b);
        params.put("btp", bt);
        return getBillFacade().findByJpql(jpql, params);
    }

    public List<Bill> getReturnBill(Bill b, List<BillTypeAtomic> billTypes) {
        if (billTypes == null || billTypes.isEmpty()) {
            return new ArrayList<>();
        }

        String jpql = "Select b "
                + " From BilledBill b "
                + " where b.retired=false "
                + " and b.creater is not null"
                + " and b.billTypeAtomic in :btps "
                + " and b.referenceBill=:ref";

        HashMap<String, Object> params = new HashMap<>();
        params.put("ref", b);
        params.put("btps", billTypes);

        return getBillFacade().findByJpql(jpql, params);
    }

    public void createPoTablePharmacy() {
        List<BillTypeAtomic> billTypesToList = new ArrayList<>();
        billTypesToList.add(BillTypeAtomic.PHARMACY_ORDER_APPROVAL);
        List<BillTypeAtomic> billTypesToAttachedToEachBillInTheList = new ArrayList<>();
        billTypesToAttachedToEachBillInTheList.add(BillTypeAtomic.PHARMACY_GRN);
        billTypesToAttachedToEachBillInTheList.add(BillTypeAtomic.PHARMACY_GRN_PRE); // Include saved GRNs
        billTypesToAttachedToEachBillInTheList.add(BillTypeAtomic.PHARMACY_GRN_CANCELLED);
//        billTypesToAttachedToEachBillInTheList.add(BillTypeAtomic.PHARMACY_GRN_RETURN);
//        billTypesToAttachedToEachBillInTheList.add(BillTypeAtomic.PHARMACY_GRN_REFUND);

        createPoTable(billTypesToList, billTypesToAttachedToEachBillInTheList);
    }

    public void createPoTableStore() {
        Date startTime = new Date();

        createPoTable(InstitutionType.StoreDealor, BillType.StoreOrderApprove, BillType.StoreGrnBill);

    }

    @Deprecated // Use the same overloaded method with billtypeatomic lists as parameters
    public void createPoTable(InstitutionType institutionType, BillType bt, BillType referenceBillType) {
        bills = null;
        String sql;
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b "
                + " where  b.retired=false"
                + " and b.billType= :bTp"
                + " and b.toInstitution.institutionType=:insTp "
                + " and  b.referenceBill.institution=:ins "
                + " and  b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  ((b.toInstitution.name) like :toIns )";
            tmp.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            tmp.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("insTp", institutionType);
        tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", bt);
        if (getReportKeyWord() != null) {
            if (getReportKeyWord().isAdditionalDetails()) {
                bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP);
            } else {
                bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, 50);
            }
        } else {
            bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP, 50);
        }
        for (Bill b : bills) {
            b.setListOfBill(getGrns(b, referenceBillType));
        }

    }

    public void createPoTable(List<BillTypeAtomic> billTypeAtomicToList, List<BillTypeAtomic> referenceBillTypes) {
        bills = null;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "Select b From Bill b "
                + " where b.retired = false"
                + " and b.billTypeAtomic in :btas"
                + " and b.referenceBill.institution = :ins "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword() != null) {

            if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().isEmpty()) {
                jpql += " and upper(b.deptId) like :billNo ";
                params.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
            }

            if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().isEmpty()) {
                jpql += " and upper(b.toInstitution.name) like :toIns ";
                params.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
            }

            if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().isEmpty()) {
                try {
                    BigDecimal netTotalValue = new BigDecimal(getSearchKeyword().getNetTotal().trim());
                    jpql += " and b.netTotal = :netTotal ";
                    params.put("netTotal", netTotalValue);
                } catch (NumberFormatException e) {
                }
            }
            if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
                jpql += " and b.id in (select bItem.bill.id  "
                        + " from BillItem bItem where bItem.retired=false and  "
                        + " ((bItem.item.name) like :itm ))";
                params.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
            }
        }

        jpql += " order by b.createdAt desc ";

        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        params.put("ins", getSessionController().getInstitution());
        params.put("btas", billTypeAtomicToList);

        if (getReportKeyWord() != null && getReportKeyWord().isAdditionalDetails()) {
            bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);
        } else {
            bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP, 50);
        }

        if (bills != null && !bills.isEmpty()) {
            for (Bill b : bills) {
                b.setListOfBill(getGrns(b, referenceBillTypes)); // N+1; refactor if needed
            }
        }
    }

    @Deprecated // Please use the overloaded method with Bill Type Atomics
    private List<Bill> getGrns(Bill b, BillType billType) {
        String sql = "Select b From BilledBill b "
                + " where b.retired=false "
                + " and b.creater is not null"
                + " and b.billType=:btp"
                + " and b.referenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", billType);
        return getBillFacade().findByJpql(sql, hm);
    }

    private List<Bill> getGrns(Bill purchaseOrderBill, List<BillTypeAtomic> grnBillTypesAtomicsToList) {
        List<Bill> result = new ArrayList<>();

        // First: b.referenceBill = :ref
        String jpql1 = "SELECT b FROM Bill b "
                + "WHERE b.retired = false "
                + "AND b.billTypeAtomic IN :btas "
                + "AND b.referenceBill = :ref";

        Map<String, Object> params1 = new HashMap<>();
        params1.put("ref", purchaseOrderBill);
        params1.put("btas", grnBillTypesAtomicsToList);

        List<Bill> list1 = getBillFacade().findByJpql(jpql1, params1);

        if (list1 != null) {
            result.addAll(list1);
        }

        // Second: b.billedBill.referenceBill = :ref (with null check)
        String jpql2 = "SELECT b FROM Bill b "
                + "WHERE b.retired = false "
                + "AND b.billTypeAtomic IN :btas "
                + "AND b.billedBill IS NOT NULL "
                + "AND b.billedBill.referenceBill = :ref";

        Map<String, Object> params2 = new HashMap<>();
        params2.put("ref", purchaseOrderBill);
        params2.put("btas", grnBillTypesAtomicsToList);

        List<Bill> list2 = getBillFacade().findByJpql(jpql2, params2);

        if (list2 != null) {
            for (Bill b : list2) {
                if (!result.contains(b)) {
                    result.add(b);
                }
            }
        }

        return result;
    }


    private List<Bill> getIssudBills(Bill b) {
        String sql = "Select b From Bill b where b.retired=false and b.creater is not null"
                + " and b.billType=:btp and "
                + " b.referenceBill=:ref or b.backwardReferenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyTransferIssue);
        return getBillFacade().findByJpql(sql, hm);
    }

    private List<Bill> getBHTIssudBills(Bill b) {
        String sql = "Select b From Bill b where b.retired=false "
                + " and b.billType=:btp "
                + " and b.referenceBill=:ref ";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyBhtPre);
        return getBillFacade().findByJpql(sql, hm);
    }

    private List<Bill> getIssuedBills(Bill b) {
        String sql = "Select b From Bill b where b.retired=false and b.creater is not null"
                + " and b.billType=:btp and "
                + " b.referenceBill=:ref and b.backwardReferenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyTransferIssue);
        return getBillFacade().findByJpql(sql, hm);
    }

    public void createDueFeeTable() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where b.retired=false and "
                + " (b.bill.billType=:btp or b.bill.billType=:btpc) "
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 and"
                + "  b.bill.createdAt between :fromDate"
                + " and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        sql += "  order by b.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.OpdBill);
        temMap.put("btpc", BillType.CollectingCentreBill);

        billFees = getBillFeeFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);
        List<BillFee> removeingBillFees = new ArrayList<>();
        if (configOptionApplicationController.getBooleanValueByKey("Remove Refunded Bill From OPD Staff Payment")) {
            for (BillFee bf : billFees) {
                sql = "SELECT bi FROM BillItem bi where bi.retired=false and bi.referanceBillItem.id=" + bf.getBillItem().getId();
                BillItem rbi = getBillItemFacade().findFirstByJpql(sql);

                if (rbi != null) {
                    removeingBillFees.add(bf);
                }

            }
            billFees.removeAll(removeingBillFees);
        }
        calTotal();

    }

    public void createDueFeeTableAll() {
        Date sartTime = new Date();

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where b.retired=false and "
                + " (b.bill.billType=:btp or b.bill.billType=:btpc) "
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 and"
                + "  b.bill.createdAt between :fromDate"
                + " and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.bill.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }
        sql += "  order by b.staff.id    ";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.OpdBill);
        temMap.put("btpc", BillType.CollectingCentreBill);
        billFees = getBillFeeFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
//        List<BillFee> removeingBillFees = new ArrayList<>();
//        for (BillFee bf : billFees) {
//            sql = "SELECT bi FROM BillItem bi where bi.retired=false and bi.referanceBillItem.id=" + bf.getBillItem().getId();
//            BillItem rbi = getBillItemFacade().findFirstByJpql(sql);
//
//            if (rbi != null) {
//                removeingBillFees.add(bf);
//            }
//
//        }
//        billFees.removeAll(removeingBillFees);
        calTotal();
    }

    public void createDueFeeTableAndPaidFeeTable() {
        Date startTime = new Date();

        dueTotal = 0.0;
        doneTotal = 0.0;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where b.retired=false and "
                + " b.bill.billType=:btp "
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 and"
                + " b.bill.createdAt between :fromDate"
                + " and :toDate ";

        if (speciality != null) {
            sql += " and b.staff.speciality=:special ";
            temMap.put("special", speciality);
        }

        if (staff != null) {
            sql += " and b.staff=:staff ";
            temMap.put("staff", staff);
        }

        if (item != null) {
            sql += " and b.billItem.item=:item ";
            temMap.put("item", item);
        }

        sql += "  order by b.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.OpdBill);

        billFees = getBillFeeFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

        for (BillFee bf : billFees) {
            dueTotal += bf.getFeeValue();
        }

        temMap.clear();
        sql = "select b.paidForBillFee from BillItem b where b.retired=false and "
                + " b.bill.billType=:btp "
                + " and b.referenceBill.billType=:refType "
                + " and b.paidForBillFee.bill.cancelled=false "
                //                + " and b.feeValue > 0 "
                + " and b.createdAt between :fromDate"
                + " and :toDate ";

        if (speciality != null) {
            sql += " and b.paidForBillFee.staff.speciality=:special ";
            temMap.put("special", speciality);
        }

        if (staff != null) {
            sql += " and b.paidForBillFee.staff=:staff ";
            temMap.put("staff", staff);
        }

        if (item != null) {
            sql += " and b.paidForBillFee.billItem.item=:item ";
            temMap.put("item", item);
        }

        sql += "  order by b.paidForBillFee.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.PaymentBill);
        temMap.put("refType", BillType.OpdBill);

        billFeesDone = getBillFeeFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

        for (BillFee bf2 : billFeesDone) {
            doneTotal += bf2.getFeeValue();
        }

    }

    double totalPaying;

    public void fillDocPayingBillFee() {
        Date startTime = new Date();

        String sql;
        Map m = new HashMap();

        sql = "select b from BillItem b where b.retired=false "
                + " and b.bill.billType=:btp "
                + " and b.referenceBill.billType=:refType "
                //                + " and b.paidForBillFee.bill.cancelled=false "
                + " and b.createdAt between :fromDate and :toDate ";

        if (speciality != null) {
            sql += " and b.paidForBillFee.staff.speciality=:s ";
            m.put("s", speciality);
        }

        if (currentStaff != null) {
            sql += " and b.paidForBillFee.staff=:cs";
            m.put("cs", currentStaff);
        }

        sql += " order by b.bill.deptId ";

        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());
        m.put("btp", BillType.PaymentBill);
        m.put("refType", BillType.OpdBill);

        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);

        totalPaying = 0.0;
        if (billItems == null) {
            return;
        }
        for (BillItem dFee : billItems) {
            totalPaying += dFee.getPaidForBillFee().getFeeValue();
        }

    }

    public void fillDocPayingBill() {

        String sql;
        Map m = new HashMap();

        sql = "select distinct(b.bill) from BillItem b where b.retired=false "
                + " and b.bill.billType=:btp "
                + " and b.referenceBill.billType=:refType "
                //                + " and b.paidForBillFee.bill.cancelled=false "
                + " and b.createdAt between :fromDate and :toDate ";

        if (speciality != null) {
            sql += " and b.paidForBillFee.staff.speciality=:s ";
            m.put("s", speciality);
        }

        if (currentStaff != null) {
            sql += " and b.paidForBillFee.staff=:cs";
            m.put("cs", currentStaff);
        }

        sql += " order by b.bill.deptId ";

        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());
        m.put("btp", BillType.PaymentBill);
        m.put("refType", BillType.OpdBill);

//        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);
        bills = getBillFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);

        totalPaying = 0.0;
        if (bills == null) {
            return;
        }
        for (Bill b : bills) {
            totalPaying += b.getNetTotal();
        }

    }

    public void createDueFeeTableInward() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where "
                + " b.retired=false "
                + " and (b.bill.billType=:btp or b.bill.billType=:btp2 )"
                + " and b.bill.cancelled=false "
                //Starting of newly added code
                //                + " and b.bill.refunded=false "
                //Ending of newly added code
                + " and (b.feeValue - b.paidValue) > 0"
                //                + " and  b.bill.billTime between :fromDate and :toDate ";
                + " and  b.bill.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += "  order by b.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.InwardBill);
        temMap.put("btp2", BillType.InwardProfessional);

        billFees = getBillFeeFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);
        List<BillFee> removeingBillFees = new ArrayList<>();
        for (BillFee bf : billFees) {
            temMap = new HashMap();
            temMap.put("btp", BillType.InwardBill);
            sql = "SELECT bi FROM BillItem bi where bi.retired=false "
                    + " and bi.bill.cancelled=false "
                    + " and bi.bill.billType=:btp "
                    //                    + " and bi.bill.toStaff=:stf "
                    + " and bi.referanceBillItem.id=" + bf.getBillItem().getId();
            BillItem rbi = getBillItemFacade().findFirstByJpql(sql, temMap);

            if (rbi != null) {
                removeingBillFees.add(bf);
            }
        }
        billFees.removeAll(removeingBillFees);
        calTotal();

    }

    public void createDueFeeTableInwardAll() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billClass", BilledBill.class);
        temMap.put("btp", BillType.InwardBill);
        temMap.put("btp2", BillType.InwardProfessional);

        sql = "select b from BillFee b where "
                + " b.retired=false "
                + " and (b.bill.billType=:btp or b.bill.billType=:btp2 )"
                + " and b.bill.cancelled=false "
                + " and type(b.bill)=:billClass "
                //Starting of newly added code
                //                + " and b.bill.refunded=false "
                //Ending of newly added code
                + " and (b.feeValue - b.paidValue) > 0"
                //                + " and  b.bill.billTime between :fromDate and :toDate ";
                + " and  b.bill.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += "  order by b.staff.id    ";

        billFees = getBillFeeFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
        List<BillFee> removeingBillFees = new ArrayList<>();
        for (BillFee bf : billFees) {
            temMap = new HashMap();
            temMap.put("btp", BillType.InwardBill);
            sql = "SELECT bi FROM BillItem bi where bi.retired=false "
                    + " and bi.bill.cancelled=false "
                    + " and bi.bill.billType=:btp "
                    //                    + " and bi.bill.toStaff=:stf "
                    + " and bi.referanceBillItem.id=" + bf.getBillItem().getId();
            BillItem rbi = getBillItemFacade().findFirstByJpql(sql, temMap);

            if (rbi != null) {
                removeingBillFees.add(bf);
            }
        }
        billFees.removeAll(removeingBillFees);
        calTotal();

    }

    public void createDueFeeTableInwardAllWithCancelled() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where "
                + " b.retired=false "
                + " and (b.bill.billType=:btp or b.bill.billType=:btp2 )"
                + " and (b.feeValue - b.paidValue) > 0"
                + " and  b.bill.billDate between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += "  order by b.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.InwardBill);
        temMap.put("btp2", BillType.InwardProfessional);

        billFees = getBillFeeFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
        calTotal();
    }

    double total;

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    private void calTotal() {
        total = 0;
        if (billFees == null) {
            return;
        }

        for (BillFee billFee : billFees) {
            total += billFee.getFeeValue();
        }
    }

    private void calTotalBillItem() {
        total = 0;
        if (billItems == null) {
            return;
        }

        for (BillItem billFee : billItems) {
            total += billFee.getNetValue();
        }
    }

    public void createDueFeeReportInward() {

        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where "
                + " b.retired=false "
                + " and (b.bill.billType=:btp or b.bill.billType=:btp2 )"
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0"
                + " and  b.bill.billDate between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientEncounter() != null) {
            sql += " and  b.bill.patientEncounter =:pe";
            temMap.put("pe", getSearchKeyword().getPatientEncounter());
        }

        if (getSearchKeyword().getAdmissionType() != null) {
            sql += " and  b.bill.patientEncounter.admissionType =:adty";
            temMap.put("adty", getSearchKeyword().getAdmissionType());
        }

        if (getSearchKeyword().getPaymentMethod() != null) {
            sql += " and  b.bill.patientEncounter.paymentMethod =:payme";
            temMap.put("payme", getSearchKeyword().getPaymentMethod());
        }

        if (getSearchKeyword().getIns() != null) {
            sql += " and  b.bill.patientEncounter.creditCompany=:is";
            temMap.put("is", getSearchKeyword().getIns());
        }

        sql += "  order by b.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.InwardBill);
        temMap.put("btp2", BillType.InwardProfessional);

        billFees = getBillFeeFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createPaymentTable() {
        Date startTime = new Date();

        billItems = null;
        HashMap temMap = new HashMap();
        String sql = "Select b FROM BillItem b "
                + " where b.retired=false "
                + " and b.bill.billType=:bType "
                + " and b.referenceBill.billType=:refType "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPaymentMethod() != null) {
            sql += " and  b.paidForBillFee.bill.paymentMethod=:pm ";
            temMap.put("pm", getSearchKeyword().getPaymentMethod());
        }

        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :insId )";
            temMap.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.billItem.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }
        if (getReportKeyWord().getInstitution() != null) {
            sql += " and  b.paidForBillFee.bill.creditCompany=:cc ";
            temMap.put("cc", getReportKeyWord().getInstitution());
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bType", BillType.PaymentBill);
        temMap.put("refType", BillType.OpdBill);

        billItems = getBillItemFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createPaymentTableAll() {
        billItems = null;
        HashMap temMap = new HashMap();
        String sql = "Select b FROM BillItem b "
                + " where b.retired=false "
                + " and b.bill.billType=:bType "
                + " and b.referenceBill.billType=:refType "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPaymentMethod() != null) {
            sql += " and  b.paidForBillFee.bill.paymentMethod=:pm ";
            temMap.put("pm", getSearchKeyword().getPaymentMethod());
        }

        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :insId )";
            temMap.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.billItem.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }
        if (getReportKeyWord().getInstitution() != null) {
            sql += " and  b.paidForBillFee.bill.creditCompany=:cc ";
            temMap.put("cc", getReportKeyWord().getInstitution());
        }
        if (getReportKeyWord().getWebUser() != null) {
            sql += " and  b.creater=:creater ";
            temMap.put("creater", getReportKeyWord().getWebUser());
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bType", BillType.PaymentBill);
        temMap.put("refType", BillType.OpdBill);

        billItems = getBillItemFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createProfessionalPaymentTableInward() {
        Date startTime = new Date();

        billItems = null;
        HashMap temMap = new HashMap();
        temMap.put("bclass", BilledBill.class);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bType", BillType.PaymentBill);
        temMap.put("refType", BillType.InwardBill);
        temMap.put("refType2", BillType.InwardProfessional);
        String sql = "Select b FROM BillItem b "
                + " where b.retired=false "
                //                + " and b.bill.cancelled=false "
                + " and type(b.bill)=:bclass"
                + " and b.bill.billType=:bType "
                + " and (b.referenceBill.billType=:refType "
                + " or b.referenceBill.billType=:refType2) "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }
        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :insId )";
            temMap.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.billItem.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bType", BillType.PaymentBill);
        temMap.put("refType", BillType.InwardBill);
        temMap.put("refType2", BillType.InwardProfessional);

        billItems = getBillItemFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createProfessionalPaymentTableInwardAll() {
        billItems = null;
        HashMap temMap = new HashMap();
//        temMap.put("bclass", BilledBill.class);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bType", BillType.PaymentBill);
        temMap.put("refType", BillType.InwardBill);
        temMap.put("refType2", BillType.InwardProfessional);
        String sql = "Select b FROM BillItem b "
                + " where b.retired=false "
                + " and b.bill.billType=:bType "
                //                + " and b.bill.cancelled=false "
                //                + " and type(b.bill)=:bclass"
                + " and (b.referenceBill.billType=:refType "
                + " or b.referenceBill.billType=:refType2) "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }
        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :insId )";
            temMap.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.billItem.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.paidForBillFee.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        billItems = getBillItemFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
        calTotalBillItem();
    }

    private StreamedContent fileForDownload;

    public void listBillsAndItemsWithFees() {
        Map<String, Object> parameters = new HashMap<>();
        String jpql = "SELECT new com.divudi.core.data.ReportTemplateRow(bill) "
                + " FROM Bill bill "
                + "WHERE 1=1 "
                + "AND bill.billTypeAtomic in :bts ";

        List<BillTypeAtomic> btas = new ArrayList<>();
        btas.add(BillTypeAtomic.OPD_BILL_WITH_PAYMENT);
        parameters.put("bts", btas);

        if (institution != null) {
            jpql += "AND bill.department.institution = :ins ";
            parameters.put("ins", institution);
        }
        if (department != null) {
            jpql += "AND bill.department = :dep ";
            parameters.put("dep", department);
        }
        if (site != null) {
            jpql += "AND bill.department.site = :site ";
            parameters.put("site", site);
        }
        if (webUser != null) {
            jpql += "AND bill.creater = :wu ";
            parameters.put("wu", webUser);
        }
        if (paymentMethod != null) {
            jpql += "AND bill.paymentMethod = :pm ";
            parameters.put("pm", paymentMethod);
        }

        jpql += "AND bill.createdAt BETWEEN :fd AND :td ";
        parameters.put("fd", fromDate);
        parameters.put("td", toDate);

        jpql += "GROUP BY bill";

        List<ReportTemplateRow> rs = (List<ReportTemplateRow>) billFacade.findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

        for (ReportTemplateRow r : rs) {
            if (r.getBill() != null) {
                r.setItemCount(billService.fetchBillItemCount(r.getBill()));
            }
        }

        bundle = new ReportTemplateRowBundle();
        bundle.setReportTemplateRows(rs);

    }

    public void listMissingBillNumberinBills() {
        Map<String, Object> parameters = new HashMap<>();
        String jpql = "SELECT bill "
                + " FROM Bill bill "
                + " WHERE bill.deptId =:dep or bill.deptId =:dept ";

        if (institution != null) {
            jpql += "AND bill.department.institution = :ins ";
            parameters.put("ins", institution);
        }
        if (department != null) {
            jpql += "AND bill.department = :dep ";
            parameters.put("dep", department);
        }
        if (site != null) {
            jpql += "AND bill.department.site = :site ";
            parameters.put("site", site);
        }

        jpql += "AND bill.createdAt BETWEEN :fd AND :td ";
        parameters.put("fd", fromDate);
        parameters.put("td", toDate);
        parameters.put("dept", "");
        parameters.put("dep", null);

        bills = billFacade.findByJpql(jpql, parameters, TemporalType.TIMESTAMP);

    }

    public StreamedContent getFileForDownload() {
        prepareDataForExcelExport();  // Prepare data and create the Excel file
        return fileForDownload;       // This should now contain the generated Excel file
    }

    public void createBillsBillItemsAndBillFeesTable(Set<Bill> bills, List<BillItem> billItems, Map<BillItem, List<BillFee>> billItemToBillFees) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Bills Details");

        int rowIdx = 0;
        Row headerRow = sheet.createRow(rowIdx++);
        // Expanding the headers to include all necessary fields
        String[] headers = {
            "Bill ID", "Insitution Name", "Department Name", "Patient Name", "Staff Name", "Bill Type", "Total", "Discount", "Net Total", "Payment Method",
            "Bill Item Name", "Bill Item Code", "Item Type", "Quantity", "Rate", "Gross Value", "Bill Item Discount", "Net Value",
            "Fee Name", "Fee Type", "Fee Value", "Institution Name", "Department Name", "Staff Name"
        };
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Fill the sheet with data
        for (Bill bill : bills) {
            for (BillItem bi : billItems) {
                if (!bi.getBill().equals(bill)) {
                    continue;
                }
                List<BillFee> fees = billItemToBillFees.get(bi);
                for (BillFee fee : fees) {
                    Row row = sheet.createRow(rowIdx++);
                    int colIdx = 0;
                    row.createCell(colIdx++).setCellValue(bill.getId());
                    row.createCell(colIdx++).setCellValue(bill.getInstitution() != null ? bill.getInstitution().getName() : "");
                    row.createCell(colIdx++).setCellValue(bill.getDepartment() != null ? bill.getDepartment().getName() : "");
                    row.createCell(colIdx++).setCellValue(bill.getPatient() != null && bill.getPatient().getPerson() != null ? bill.getPatient().getPerson().getName() : "");
                    row.createCell(colIdx++).setCellValue(bill.getStaff() != null && bill.getStaff().getPerson() != null ? bill.getStaff().getPerson().getNameWithTitle() : "");
                    row.createCell(colIdx++).setCellValue(bill.getBillType() != null ? bill.getBillType().getLabel() : "");
                    row.createCell(colIdx++).setCellValue(bill.getGrantTotal());
                    row.createCell(colIdx++).setCellValue(bill.getDiscount());
                    row.createCell(colIdx++).setCellValue(bill.getNetTotal());
                    row.createCell(colIdx++).setCellValue(bill.getPaymentMethod() != null ? bill.getPaymentMethod().getLabel() : "");

                    row.createCell(colIdx++).setCellValue(bi.getItem() != null ? bi.getItem().getName() : "");
                    row.createCell(colIdx++).setCellValue(bi.getItem() != null ? bi.getItem().getCode() : "");
                    row.createCell(colIdx++).setCellValue(bi.getItem() != null ? bi.getItem().getItemType().toString() : "");
                    row.createCell(colIdx++).setCellValue(bi.getQty());
                    row.createCell(colIdx++).setCellValue(bi.getNetRate());
                    row.createCell(colIdx++).setCellValue(bi.getGrossValue());
                    row.createCell(colIdx++).setCellValue(bi.getDiscount());
                    row.createCell(colIdx++).setCellValue(bi.getNetValue());

                    row.createCell(colIdx++).setCellValue(fee.getFee() != null ? fee.getFee().getName() : "");
                    row.createCell(colIdx++).setCellValue(fee.getFee() != null ? fee.getFee().getFeeType().toString() : "");
                    row.createCell(colIdx++).setCellValue(fee.getFeeValue());
                    row.createCell(colIdx++).setCellValue(fee.getInstitution() != null ? fee.getInstitution().getName() : "");
                    row.createCell(colIdx++).setCellValue(fee.getDepartment() != null ? fee.getDepartment().getName() : "");
                    row.createCell(colIdx++).setCellValue(fee.getStaff() != null && fee.getStaff().getPerson() != null ? fee.getStaff().getPerson().getNameWithTitle() : "");
                }

            }
        }

        // Autosize columns to fit content
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to output stream and create download content
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        // Set the StreamedContent object for download
        fileForDownload = DefaultStreamedContent.builder()
                .name("bills_details.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
                .build();
    }

    public void prepareDataForExcelExport() {
        // Fetch all bills and their associated bill items and fees
        String jpql = "SELECT b FROM Bill b JOIN FETCH b.billItems bi JOIN FETCH bi.billFees WHERE b.createdAt BETWEEN :fromDate AND :toDate";
        Map<String, Object> params = new HashMap<>();
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);

        List<Bill> bills = billFacade.findByJpql(jpql, params); // Assuming you have a facade to execute JPQL queries

        Set<Bill> uniqueBills = new HashSet<>(bills);
        List<BillItem> allBillItems = new ArrayList<>();
        Map<BillItem, List<BillFee>> billItemToFeesMap = new HashMap<>();

        // Extract bill items and fees
        for (Bill bill : uniqueBills) {
            for (BillItem billItem : bill.getBillItems()) {
                allBillItems.add(billItem);
                List<BillFee> fees = billItem.getBillFees();
                billItemToFeesMap.put(billItem, fees);
            }
        }

        try {
            createBillsBillItemsAndBillFeesTable(uniqueBills, allBillItems, billItemToFeesMap);
        } catch (IOException e) {
            e.printStackTrace(); // Handle exceptions properly
        }
    }

    public void createBillItemTableByKeyword() {

        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        billTypesAtomics.add(BillTypeAtomic.OPD_BILL_WITH_PAYMENT);
        billTypesAtomics.add(BillTypeAtomic.OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER);

        Date startTime = new Date();
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);

        sql = "select bi "
                + " from BillItem bi "
                + " where  bi.bill.billTypeAtomic in :billTypesAtomics  "
                + " and bi.createdAt between :fromDate and :toDate ";

        m.put("billTypesAtomics", billTypesAtomics);

        if (showLoggedDepartmentOnly) {
            Department dept = sessionController.getDepartment();
            if (dept != null) {
                sql += " and bi.bill.department=:dept ";
                m.put("dept", dept);
            }
        }

        if (searchKeyword.getPatientName() != null && !searchKeyword.getPatientName().trim().equals("")) {
            sql += " and  ((bi.bill.patient.person.name) like :patientName )";
            m.put("patientName", "%" + searchKeyword.getPatientName().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getPatientPhone() != null && !searchKeyword.getPatientPhone().trim().equals("")) {
            sql += " and  ((bi.bill.patient.person.phone) like :patientPhone )";
            m.put("patientPhone", "%" + searchKeyword.getPatientPhone().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getBillNo() != null && !searchKeyword.getBillNo().trim().equals("")) {
            sql += " and  ((bi.bill.insId) like :billNo )";
            m.put("billNo", "%" + searchKeyword.getBillNo().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getItemName() != null && !searchKeyword.getItemName().trim().equals("")) {
            sql += " and  ((bi.item.name) like :itemName )";
            m.put("itemName", "%" + searchKeyword.getItemName().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getToInstitution() != null && !searchKeyword.getToInstitution().trim().equals("")) {
            sql += " and  ((bi.bill.toInstitution.name) like :toIns )";
            m.put("toIns", "%" + searchKeyword.getToInstitution().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 50);

        checkLabReportsApprovedBillItem(billItems);

        //   searchBillItems = new LazyBillItem(tmp);
    }

    public String toCreateBillItemListForCreditCompany() {
        billItems = new ArrayList<>();
        return "/reportLab/credit_company_bill_item_list";
    }

    public void createBillItemListForCreditCompany() {
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.OpdBill);
        m.put("ins", getCreditCompany());
        sql = "select bi from BillItem bi where bi.bill.creditCompany=:ins "
                + " and bi.bill.billType=:bType "
                + " and bi.createdAt between :fromDate and :toDate ";
        sql += " order by bi.id ";
        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);
//        checkLabReportsApprovedBillItem(billItems);
//
    }

    public void createBillItemTableByKeywordAll() {
        Date startTime = new Date();

        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.OpdBill);
        m.put("ins", getSessionController().getInstitution());

        sql = "select bi from BillItem bi where bi.bill.institution=:ins "
                + " and bi.bill.billType=:bType "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (searchKeyword.getPatientName() != null && !searchKeyword.getPatientName().trim().equals("")) {
            sql += " and  ((bi.bill.patient.person.name) like :patientName )";
            m.put("patientName", "%" + searchKeyword.getPatientName().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getPatientPhone() != null && !searchKeyword.getPatientPhone().trim().equals("")) {
            sql += " and  ((bi.bill.patient.person.phone) like :patientPhone )";
            m.put("patientPhone", "%" + searchKeyword.getPatientPhone().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getBillNo() != null && !searchKeyword.getBillNo().trim().equals("")) {
            sql += " and  ((bi.bill.insId) like :billNo )";
            m.put("billNo", "%" + searchKeyword.getBillNo().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getItemName() != null && !searchKeyword.getItemName().trim().equals("")) {
            sql += " and  ((bi.item.name) like :itemName )";
            m.put("itemName", "%" + searchKeyword.getItemName().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getToInstitution() != null && !searchKeyword.getToInstitution().trim().equals("")) {
            sql += " and  ((bi.bill.toInstitution.name) like :toIns )";
            m.put("toIns", "%" + searchKeyword.getToInstitution().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);

        checkLabReportsApprovedBillItem(billItems);

        //   searchBillItems = new LazyBillItem(tmp);
    }

    public void createPatientInvestigationsTable() {
        Date startTime = new Date();

        String sql = "select pi "
                + " from PatientInvestigation pi "
                + " join pi.investigation  i "
                + " join pi.billItem.bill b "
                + " join b.patient.person p "
                + " where "
                + " b.createdAt between :fromDate and :toDate  ";

        Map temMap = new HashMap();

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (p.name like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (b.insId like :billNo or b.deptId like :billNo)";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (p.phone like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (i.name like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (patientEncounter != null) {
            sql += "and pi.encounter=:en";
            temMap.put("en", patientEncounter);
        }

        if (getReportKeyWord().getDepartment() != null) {
            sql += " and b.toDepartment=:dep ";
            temMap.put("dep", getReportKeyWord().getDepartment());
        }

        if (getReportKeyWord().getDepartmentFrom() != null) {
            sql += " and b.fromDepartment=:depFrom ";
            temMap.put("depFrom", getReportKeyWord().getDepartmentFrom());
        }

        sql += " order by pi.id desc  ";
//

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        patientInvestigations = getPatientInvestigationFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);
        checkRefundBillItems(patientInvestigations);

    }

    public void getPatientInvestigationsReportsTable() {
        Date startTime = new Date();

        String sql = "select pi "
                + " from PatientInvestigation pi "
                + " join pi.investigation  i "
                + " join pi.billItem.bill b "
                + " join b.patient.person p "
                + " where "
                + " b.createdAt between :fromDate and :toDate  ";

        Map temMap = new HashMap();

        if (institution == null) {
            sql += " and b.collectingCentre in :ccs ";
            temMap.put("ccs", sessionController.getLoggableCollectingCentres());
        } else {
            sql += " and b.collectingCentre=:cc ";
            temMap.put("cc", institution);
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (p.name like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (b.insId like :billNo or b.deptId like :billNo)";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (p.phone like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (i.name like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getReportKeyWord().getDepartment() != null) {
            sql += " and b.toDepartment=:dep ";
            temMap.put("dep", getReportKeyWord().getDepartment());
        }

        if (getReportKeyWord().getDepartmentFrom() != null) {
            sql += " and b.fromDepartment=:depFrom ";
            temMap.put("depFrom", getReportKeyWord().getDepartmentFrom());
        }

        sql += " order by pi.id desc  ";
//

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        patientInvestigations = getPatientInvestigationFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);
        checkRefundBillItems(patientInvestigations);

    }

    public void createPatientInvestigationsTableByLoggedInstitution() {

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  "
                + " and b.institution =:ins ";

        Map temMap = new HashMap();
        temMap.put("ins", getSessionController().getInstitution());

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((p.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by pi.id desc  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        patientInvestigations = getPatientInvestigationFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createPatientInvestigationsTableByLoggedDepartment() {

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  "
                + " and b.department =:dep ";

        Map temMap = new HashMap();
        temMap.put("dep", getSessionController().getDepartment());

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((p.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by pi.id desc  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        patientInvestigations = getPatientInvestigationFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void searchPatientInvestigations() {
        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  ";
        Map temMap = new HashMap();
        sql += " order by pi.id ";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        patientInvestigations = getPatientInvestigationFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public String fillUserPatientReportMobile() {
        return fillUserPatientReport(false);
    }

    public String fillUserPatientReportWeb() {
        return fillUserPatientReport(true);
    }

    public String toSearchReportsByBillNumber() {
        return "/report_search_by_bill_number";
    }

    private String fillUserPatientReport(boolean web) {
        String jpql;
        Map m = new HashMap();
        m.put("pn", getSessionController().getPhoneNo());
        m.put("bn", getSessionController().getBillNo());
        if (getSessionController().getPhoneNo() == null || getSessionController().getPhoneNo().equals("")) {
            getReportKeyWord().setString("Please Enter Phone Number");
            JsfUtil.addErrorMessage("Please Enter Phone Number");
            return "";
        }
        if (getSessionController().getBillNo() == null || getSessionController().getBillNo().equals("")) {
            getReportKeyWord().setString("Please Enter Bill Number");
            JsfUtil.addErrorMessage("Please Enter Bill Number");
            return "";
        }

        jpql = " select pr from PatientInvestigation pr where pr.retired=false and "
                + " (pr.billItem.bill.patient.person.phone)=:pn and "
                + " ((pr.billItem.bill.insId)=:bn or (pr.billItem.bill.deptId)=:bn) "
                + " order by pr.id desc ";
        if (web) {
            m.put("pn", getSessionController().getPhoneNo());
        } else {
            m.put("pn", getSessionController().getPhoneNo().substring(0, 3) + "-" + getSessionController().getPhoneNo().substring(3));
        }
        m.put("bn", getSessionController().getBillNo());

        userPatientInvestigations = patientInvestigationFacade.findByJpql(jpql, m, 20);

        if (!userPatientInvestigations.isEmpty()) {
            if (web) {
                return "/reports_list_new";
            } else {
                getReportKeyWord().setString("0");
                getSessionController().setPhoneNo("");
                getSessionController().setBillNo("");
                return "/reports_list_new_1";
            }
        } else {
            getReportKeyWord().setString("Please Enter Correct Phone Number and Bill Number");
            JsfUtil.addErrorMessage("Please Enter Correct Phone Number and Bill Number");
            return "";
        }
    }

    public void createPatientInvestigationsTableSingle() {
        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient p where "
                + " p=:pt ";
        Map temMap = new HashMap();
        sql += " order by pi.id desc  ";
        temMap.put("pt", getTransferController().getPatient());
        patientInvestigationsSigle = getPatientInvestigationFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);
    }

    public void createPatientInvestigationsTableAll() {
        Date startTime = new Date();

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  ";

        Map temMap = new HashMap();
        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((p.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (b.insId like :billNo or b.deptId like :billNo)";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (patientEncounter != null) {
            sql += "and pi.encounter=:en";
            temMap.put("en", patientEncounter);
        }

        if (getReportKeyWord().getDepartment() != null) {
            sql += " and b.toDepartment=:dep ";
            temMap.put("dep", getReportKeyWord().getDepartment());
        }

        if (getReportKeyWord().getDepartmentFrom() != null) {
            sql += " and b.fromDepartment=:depFrom ";
            temMap.put("depFrom", getReportKeyWord().getDepartmentFrom());
        }

        sql += " order by pi.id desc  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        patientInvestigations = getPatientInvestigationFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
        checkRefundBillItems(patientInvestigations);

    }

    public void searchApprovedReportsWithCancelledOrReturnedBills() {
        String sql = "select pr "
                + " from PatientReport pr"
                + " join pr.patientInvestigation pi "
                + " join pi.billItem.bill b "
                + " where "
                + " b.createdAt between :fromDate and :toDate "
                //                + " and b.cancelled=:c "
                //                + " and pr.approved=:a "
                + " order by b.id";

        Map temMap = new HashMap();
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("c", true);
        temMap.put("a", true);
        patientReports = getPatientReportFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    public void checkRefundBillItems(List<PatientInvestigation> pis) {
        for (PatientInvestigation pi : pis) {
            for (PatientReport pr : pi.getPatientReports()) {
                if (pr.getApproved()) {
                    pi.getBillItem().getBill().setApprovedAnyTest(true);
                    break;
                }
            }
            markRefundBillItem(pi);
        }
    }

    public void markRefundBillItem(PatientInvestigation pi) {
        if (pi == null) {
            return;
        }
        if (pi.getBillItem() == null) {
            return;
        }
        if (pi.getBillItem().getId() == null) {
            return;
        }
        String sql;
        Map m = new HashMap();
        sql = "select bi from BillItem bi "
                + " where bi.referanceBillItem.id=:bi ";
        m.put("bi", pi.getBillItem().getId());
        List<BillItem> bis = getBillItemFacade().findByJpql(sql, m);
        if (bis.isEmpty()) {
            pi.getBillItem().setTransRefund(false);
        } else {
            pi.getBillItem().setTransRefund(true);
        }
    }

    public void fillPatientLabReports(Patient pt) {

        String sql = "select pi "
                + "from PatientInvestigation pi "
                + "join pi.billItem.bill b "
                + "where b.patient=:cp "
                + "and b.createdAt between :fromDate and :toDate ";

        Map temMap = new HashMap();
        temMap.put("cp", pt);

        sql += " order by pi.approveAt desc  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        patientInvestigations = getPatientInvestigationFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void fillPatientLabreportTable() {
        fillPatientLabReports(patient);
    }

    public void createPatientInvestigationsTableAllByLoggedInstitution() {
        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  "
                + " and b.institution =:ins ";

        Map temMap = new HashMap();
        temMap.put("ins", getSessionController().getInstitution());

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((p.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by pi.approveAt desc  ";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        patientInvestigations = getPatientInvestigationFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createPreBillsForReturn() {
        Date startTime = new Date();

        createPreBillsForReturn(BillType.PharmacyPre, BillType.PharmacySale);

    }

    public void createWholePreBillsForReturn() {
        createPreBillsForReturn(BillType.PharmacyWholesalePre, BillType.PharmacyWholeSale);
    }

    public void createPreBillsForReturn(BillType billType, BillType refBillType) {
        bills = null;
        String sql;
        Map<String, Object> temMap = new HashMap<>();
        if (getSearchKeyword().getItem() == null) {
            sql = "select b from PreBill b where b.billType = :billType and "
                    + "b.institution = :ins and b.billedBill is null and "
                    + "b.department = :dept and b.referenceBill.billType = :refBillType and "
                    + "b.createdAt between :fromDate and :toDate and b.retired = false and "
                    + "b.referenceBill.cancelled = false ";

            if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
                sql += "and b.deptId like :billNo ";
                temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
            }

            if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
                sql += "and b.netTotal = :netTotal ";
                temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
            }

            sql += "order by b.createdAt desc ";

            temMap.put("billType", billType);
            temMap.put("refBillType", refBillType);
            temMap.put("toDate", toDate);
            temMap.put("fromDate", fromDate);
            temMap.put("ins", getSessionController().getInstitution());
            temMap.put("dept", getSessionController().getLoggedUser().getDepartment());

            bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

        } else {

            sql = "select DISTINCT(bi.bill) from BillItem bi where bi.bill.billType = :billType and "
                    + " bi.bill.institution=:ins and (bi.bill.billedBill is null) and "
                    + " bi.item=:item and "
                    + " bi.bill.department=:dept and "
                    + " bi.bill.referenceBill.billType=:refBillType "
                    + " and bi.createdAt between :fromDate and :toDate and bi.bill.retired=false "
                    // for remove cancel bills
                    + " and bi.bill.referenceBill.cancelled=false ";

            if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
                sql += " and  ((bi.bill.deptId) like :billNo )";
                temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
            }

            if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
                sql += " and  ((bi.bill.netTotal) = :netTotal )";
                temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
            }

            sql += " order by bi.createdAt desc  ";

            temMap.put("billType", billType);
            temMap.put("refBillType", refBillType);
            temMap.put("toDate", toDate);
            temMap.put("fromDate", fromDate);
            temMap.put("item", getSearchKeyword().getItem());
            temMap.put("ins", getSessionController().getInstitution());
            temMap.put("dept", getSessionController().getLoggedUser().getDepartment());

            bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

        }
    }

    public BillBeanController getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBeanController billBean) {
        this.billBean = billBean;
    }

    public void createPreBillsNotPaid() {
        bills = getBillBean().billsForTheDayNotPaid(BillType.PharmacyPre, getSessionController().getDepartment(), fromDate, toDate);
    }

    public void createWholePreBillsNotPaid() {

        bills = getBillBean().billsForTheDayNotPaid(BillType.PharmacyWholesalePre, getSessionController().getDepartment());

    }

    public String navigateToAddToStockBillPrint() {
        printPreview = true;
        duplicateBillView = true;

        return "/pharmacy/pharmacy_search_pre_bill_not_paid?faces-redirect=true";
    }

    public String navigateToPharmacyBillSearch() {
        printPreview = true;
        duplicateBillView = true;
        return "/pharmacy/pharmacy_search_by_bill_type_atomic?faces-redirect=true";
    }

    public String navigateToItemizedSaleSummary() {
        return "/opd/analytics/itemized_sale_summary?faces-redirect=true";
    }

    public String navigateToItemizedSaleReport() {
        return "/opd/analytics/itemized_sale_report?faces-redirect=true";
    }

    public String navigateToItemizedSaleSummaryOpd() {
        return "/opd/analytics/itemized_sale_summary?faces-redirect=true";
    }

    public String navigateToOptimizedItemizedSaleSummary() {
        return "/opd/analytics/itemized_sale_summary_dto.xhtml?faces-redirect=true";
    }

    public String navigateToLegacyItemizedSaleSummary() {
        return "/opd/analytics/itemized_sale_summary.xhtml?faces-redirect=true";
    }

    public String navigateToItemizedSaleReportOpd() {
        return "/opd/analytics/itemized_sale_report?faces-redirect=true";
    }

    public String navigateToIncomeBreakdownByCategoryOpd() {
        return "/opd/analytics/income_breakdown_by_category?faces-redirect=true";
    }

    public boolean isOptimizedItemizedSaleSummaryEnabled() {
        return configOptionApplicationController.getBooleanValueByKey("OPD Itemized Sale Summary - Optimized Method", false);
    }

    public boolean isLegacyItemizedSaleSummaryEnabled() {
        return configOptionApplicationController.getBooleanValueByKey("OPD Itemized Sale Summary - Legacy Method", true);
    }

    public List<Payment> getPaymentDetals(Bill bill) {
        List<Payment> billPayments = new ArrayList<>();
        String jpql;
        Map temMap = new HashMap();

        jpql = "select p from Payment p "
                + " where p.bill =:bill "
                + " and p.retired =:ret ";

        temMap.put("bill", bill);
        temMap.put("ret", false);
        billPayments = paymentFacade.findByJpql(jpql, temMap);

        return billPayments;
    }

    public void addToStock() {
        Date startTime = new Date();
        Date fromDate = null;
        Date toDate = null;

        bill = new Bill();

        bill.setBillDate(new Date());
        bill.setBillTime(new Date());
        bill.setCreatedAt(new Date());

        bill.setDeptId(billController.getBillNumberGenerator().departmentBillNumberGenerator(getSessionController().getDepartment(), getSessionController().getDepartment(), BillType.PharmacyAddtoStock, BillClassType.BilledBill));
        bill.setInsId(billController.getBillNumberGenerator().institutionBillNumberGenerator(getSessionController().getInstitution(), getSessionController().getDepartment(), BillType.PharmacyAddtoStock, BillClassType.BilledBill, BillNumberSuffix.NONE));

        bill.setBillClassType(BillClassType.Bill);
        bill.setBillType(BillType.PharmacyAddtoStock);
        bill.setBillTypeAtomic(BillTypeAtomic.PHARMACY_RETAIL_SALE_PRE_ADD_TO_STOCK);

        bill.setCreater(getSessionController().getLoggedUser());
        bill.setDepartment(getSessionController().getDepartment());
        bill.setInstitution(sessionController.getInstitution());

        getBillFacade().create(bill);

        List<BillItem> billItems = new ArrayList<>();

        double totalValue = 0.0;

        for (Bill b : getSelectedBills()) {
            b = getBillFacade().find(b.getId());
            if (b.getReferenceBill() != null) {
                JsfUtil.addErrorMessage("This Bill " + b.getDeptId() + " alrady Paid can't add to stock.");
                continue;
            }
            if (b.checkActiveCashPreBill()) {
                continue;
            }

            totalValue += b.getNetTotal();

            for (BillItem bi : b.getBillItems()) {
                BillItem nbi = new BillItem();
                nbi.copy(bi);
                nbi.invertValue();
                nbi.setBill(bill);

                billItemFacade.create(nbi);

                PharmaceuticalBillItem npi = new PharmaceuticalBillItem();
                npi.copy(bi.getPharmaceuticalBillItem());
                npi.invertValue(npi);
                npi.setBillItem(nbi);

                pharmaceuticalBillItemFacade.create(npi);

                nbi.setPharmaceuticalBillItem(npi);

                billItemFacade.edit(nbi);
                billItems.add(nbi);
            }

            Bill prebill = getPharmacyBean().reAddToStock(b, getSessionController().getLoggedUser(),
                    getSessionController().getDepartment(), BillNumberSuffix.PRECAN);
            prebill.setBillTypeAtomic(BillTypeAtomic.PHARMACY_RETAIL_SALE_PRE_ADD_TO_STOCK);
            getBillFacade().edit(prebill);

            if (prebill != null) {
                b.setCancelled(true);
                b.setCancelledBill(prebill);
                getBillFacade().edit(b);
            }
        }
        bill.setNetTotal(totalValue);
        bill.setBillItems(billItems);
        getBillFacade().edit(bill);

        createPreBillsNotPaid();
        printPreview = true;
        duplicateBillView = false;
    }

    public void cancelIssueToUnitBills() {
        if (cancellingIssueBill == null) {
            JsfUtil.addErrorMessage("Select a bill to cancel");
            return;
        }
        Bill b = cancellingIssueBill;

        if (b.isCancelled() || b.isRefunded()) {
            JsfUtil.addErrorMessage("Can not cancel already cancelled or returned bills");
            return;
        }
        if (b instanceof PreBill) {
            Bill prebill = getPharmacyBean().readdStockForIssueBills((PreBill) b, getSessionController().getLoggedUser(),
                    getSessionController().getDepartment(), BillNumberSuffix.DIC);
            b.setCancelled(true);
            b.setCancelledBill(prebill);
            getBillFacade().edit(b);
            JsfUtil.addSuccessMessage("Cancelled");
        } else {
            JsfUtil.addErrorMessage("Not an Issue Bill. Can not cancell");
        }

    }

    private String createPharmacyPayKeyword(Map temMap) {
        String sql = "";
        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTokenNumber() != null && !getSearchKeyword().getTokenNumber().trim().equals("")) {
            sql += " and exists (select t from Token t where t.bill=b and upper(t.tokenNumber) like :token)";
            temMap.put("token", "%" + getSearchKeyword().getTokenNumber().trim().toUpperCase() + "%");
        }

        if (getReportKeyWord().getDepartment() != null) {
            sql += " and b.department=:dep ";
            temMap.put("dep", getReportKeyWord().getDepartment());
        }

        return sql;

    }

    public void createOpdBathcBillPreTable() {
        Date startTime = new Date();
        aceptPaymentBills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins"
                //                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false ";
//                + " and b.deptId is not null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", BillType.OpdBathcBillPre);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        aceptPaymentBills = getBillFacade().findByJpqlWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);

    }

    public void createOpdBillSearch() {
        Date startTime = new Date();
        createTableByKeyword(BillType.OpdBill);
//        checkLabReportsApproved(bills);

    }

    public void createOpdBathcBillPreTablePaidOnly() {
        Date startTime = new Date();
        aceptPaymentBills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins"
                + " and b.referenceBill.balance=0 "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false ";
//                + " and b.deptId is not null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", BillType.OpdBathcBillPre);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        aceptPaymentBills = getBillFacade().findByJpqlWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);

    }

    public void createOpdBathcBillPreTableNotPaidOly() {
        Date startTime = new Date();
        aceptPaymentBills = new ArrayList<>();
        String sql;
        Map temMap = new HashMap();

        List<Bill> abs = new ArrayList<>();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins"
                //                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false ";
//                + " and b.deptId is not null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", BillType.OpdBathcBillPre);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        abs = getBillFacade().findByJpqlWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 50);

        List<Bill> pbs = new ArrayList<>();
        Map temMap2 = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins"
                + " and b.referenceBill.balance=0 "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false ";
//                + " and b.deptId is not null ";

        sql += createPharmacyPayKeyword(temMap2);
        sql += " order by b.createdAt desc  ";
//
        temMap2.put("billType", BillType.OpdBathcBillPre);
        temMap2.put("toDate", getToDate());
        temMap2.put("fromDate", getFromDate());
        temMap2.put("ins", getSessionController().getInstitution());

        pbs = getBillFacade().findByJpqlWithoutCache(sql, temMap2, TemporalType.TIMESTAMP, 50);

        abs.removeAll(pbs);
        aceptPaymentBills.addAll(abs);

    }

    public String searchOpdBatchBillsSettledAtCashier() {
        bills = null;
        String jpql;
        Map m = new HashMap();
        jpql = "select b "
                + " from Bill b "
                + " where b.retired=false "
                + " and b.institution=:ins"
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.deptId is not null "
                + " and b.cancelled=false"
                + " and b.referenceBill is not null ";
        jpql += " and b.billTypeAtomic = :billType ";
        m.put("billType", BillTypeAtomic.OPD_BATCH_BILL_TO_COLLECT_PAYMENT_AT_CASHIER);
        jpql += " order by b.createdAt desc  ";
        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());
        m.put("ins", getSessionController().getInstitution());
        bills = getBillFacade().findByJpqlWithoutCache(jpql, m, TemporalType.TIMESTAMP, 25);
        return "/opd/opd_search_pre_bill?faces-redirect=true";
    }

    public String searchOpdBatchBillsToSettleAtCashier() {
        bills = null;
        String jpql;
        Map m = new HashMap();
        jpql = "select b "
                + " from Bill b "
                + " where b.retired=false "
                + " and b.institution=:ins"
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.deptId is not null "
                + " and b.cancelled=false"
                + " and b.referenceBill is null ";
        jpql += " and b.billTypeAtomic = :billType ";
        m.put("billType", BillTypeAtomic.OPD_BATCH_BILL_TO_COLLECT_PAYMENT_AT_CASHIER);
        jpql += " order by b.createdAt desc  ";
        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());
        m.put("ins", getSessionController().getInstitution());
        bills = getBillFacade().findByJpqlWithoutCache(jpql, m, TemporalType.TIMESTAMP, 25);
        return "/opd/opd_search_pre_bill?faces-redirect=true";
    }

    public String searchOpdBatchBillsSettledOrNotSettled() {
        bills = null;
        String jpql;
        Map m = new HashMap();
        jpql = "select b "
                + " from Bill b "
                + " where b.retired=false "
                + " and b.institution=:ins"
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.deptId is not null ";
        jpql += " and b.billTypeAtomic = :billType ";
        m.put("billType", BillTypeAtomic.OPD_BATCH_BILL_TO_COLLECT_PAYMENT_AT_CASHIER);
        jpql += " order by b.createdAt desc  ";
        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());
        m.put("ins", getSessionController().getInstitution());
        bills = getBillFacade().findByJpqlWithoutCache(jpql, m, TemporalType.TIMESTAMP, 25);
        return "/opd/opd_search_pre_bill?faces-redirect=true";
    }

    public String createOpdPreTable() {
        createPreTable(BillType.OpdPreBill);
        return "/opd/opd_search_pre_bill?faces-redirect=true";
    }

    public void fillPatientBillsToPay() {
        if (patient == null) {
            JsfUtil.addErrorMessage("Please select a Patient");
            return;
        }
        fillPatientPreBills(BillType.OpdPreBill, patient, true, null);
    }

    public void fillPatientBillsPaid() {
        if (patient == null) {
            JsfUtil.addErrorMessage("Please select a Patient");
            return;
        }
        fillPatientPreBills(BillType.OpdPreBill, patient, null, false);
    }

    public void fillPatientBillsPaidAndToPay() {
        if (patient == null) {
            JsfUtil.addErrorMessage("Please select a Patient");
            return;
        }
        fillPatientPreBills(BillType.OpdPreBill, patient, null, null);
    }

    public String createOpdPreTableNotPaid() {
        createPreTableNotPaid(BillType.OpdPreBill);
        return "/opd/opd_search_pre_bill?faces-redirect=true";
    }

    public String createOpdPreTablePaid() {
        createPreTablePaid(BillType.OpdPreBill);
        return "/opd/opd_search_pre_bill?faces-redirect=true";
    }

    public void fillPharmacyPreBillsToAcceptAtCashierGetPaid() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billTypeAtomic = :billTypeAtomic "
                + " and b.institution=:ins "
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false "
                + " and b.deptId is not null "
                + " and b.cancelled=false"
                + " and b.referenceBill is not null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//
        temMap.put("billTypeAtomic", BillTypeAtomic.PHARMACY_RETAIL_SALE_PRE_TO_SETTLE_AT_CASHIER);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpqlWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);
    }

    public void fillPharmacyPreBillsToAcceptAtCashierNotPaid() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billTypeAtomic = :billTypeAtomic "
                + " and b.institution=:ins "
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false "
                + " and b.billTypeAtomic is not null "
                + " and b.deptId is not null "
                + " and b.cancelled=false"
                + " and b.referenceBill is null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//
        temMap.put("billTypeAtomic", BillTypeAtomic.PHARMACY_RETAIL_SALE_PRE_TO_SETTLE_AT_CASHIER);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpqlWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);
    }

    public void fillPharmacyPaidPreBillsToAcceptAtCashierInTokenSystem(boolean paidOnly) {
        bills = null;
        String sql;
        Map parameters = new HashMap();
//        Token t = new Token();

        sql = "select token from Token token "
                + " where token.tokenType = :type "
                + " and token.bill is not null "
                + " and token.bill.retired = false"
                + " and token.tokenAt between :fromDate and :toDate "
                + " and token.retired = false "
                + " and token.department = :dept "
                + " and token.institution = :ins ";

//
        parameters.put("type", TokenType.PHARMACY_TOKEN_SALE_FOR_CASHIER);
        parameters.put("fromDate", getFromDate());
        parameters.put("toDate", getToDate());
        parameters.put("dept", sessionController.getDepartment());
        parameters.put("ins", sessionController.getInstitution());

        if (paidOnly) {
            sql += " and token.bill.referenceBill is not null ";
        } else {
            sql += " and token.bill.referenceBill is null ";
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((token.bill.patient.person.name) like :patientName )";
            parameters.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((token.bill.deptId) like :billNo )";
            parameters.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((token.bill.netTotal) = :netTotal )";
            parameters.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((token.bill.total) like :total )";
            parameters.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTokenNumber() != null && !getSearchKeyword().getTokenNumber().trim().equals("")) {
            sql += " and  (upper(token.tokenNumber) like :token)";
            parameters.put("token", "%" + getSearchKeyword().getTokenNumber().trim().toUpperCase() + "%");
        }

        sql += " order by token.tokenAt desc";

        List<Token> tokenList = tokenFacade.findByJpqlWithoutCache(sql, parameters, TemporalType.TIMESTAMP, 25);
        bills = tokenList.stream().map(t -> t.getBill()).collect(Collectors.toList());

    }

    public void fillPharmacyPreBillsToAcceptAtCashierInTokenSystem() {
        bills = null;
        String sql;
        Map parameters = new HashMap();
//        Token t = new Token();

        sql = "select token from Token token "
                + " where token.tokenType = :type "
                + " and token.bill is not null "
                + " and token.bill.retired = false"
                + " and token.tokenAt between :fromDate and :toDate "
                + " and token.retired = false "
                + " and token.department = :dept "
                + " and token.institution = :ins ";

//
        parameters.put("type", TokenType.PHARMACY_TOKEN_SALE_FOR_CASHIER);
        parameters.put("fromDate", getFromDate());
        parameters.put("toDate", getToDate());
        parameters.put("dept", sessionController.getDepartment());
        parameters.put("ins", sessionController.getInstitution());

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((token.bill.patient.person.name) like :patientName )";
            parameters.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((token.bill.deptId) like :billNo )";
            parameters.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((token.bill.netTotal) = :netTotal )";
            parameters.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((token.bill.total) like :total )";
            parameters.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTokenNumber() != null && !getSearchKeyword().getTokenNumber().trim().equals("")) {
            sql += " and  (upper(token.tokenNumber) like :token)";
            parameters.put("token", "%" + getSearchKeyword().getTokenNumber().trim().toUpperCase() + "%");
        }

        sql += " order by token.tokenAt desc";

        List<Token> tokenList = tokenFacade.findByJpqlWithoutCache(sql, parameters, TemporalType.TIMESTAMP, 25);
        bills = tokenList.stream().map(t -> t.getBill()).collect(Collectors.toList());

    }

    /**
     * @param bill need to find paid bills
     * @return paid bill list associate with bill This method added due to avoid
     * cache and get fresh bill entity from db. Otherwise it is not updated even
     * paid bills are available
     */
    public List<Bill> getRefreshCashBills(Bill bill) {
        Bill fetchBill = getBillFacade().findWithoutCache(bill.getId());
        if (fetchBill == null) {
            return Collections.emptyList();
        }
        return fetchBill.getCashBillsPre();
    }

    @Inject
    private TokenController tokenController;

    public Token findBillFromToken(Bill bill) {
        return tokenController.findPharmacyTokens(bill);
    }

    public List<Bill> filterNomarlBillsOnly(List<Bill> billList) {
        List<Bill> normalBills = new ArrayList<>();
        for (Bill bill : billList) {
            if (findBillFromToken(bill) == null) {
                normalBills.add(bill);
            }
        }

        return normalBills;
    }

    public void fillPharmacyPreBillsToAcceptAtCashier() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billTypeAtomic = :billTypeAtomic "
                + " and b.institution=:ins "
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false "
                + " and b.deptId is not null "
                + " and b.cancelled=false";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//
        temMap.put("billTypeAtomic", BillTypeAtomic.PHARMACY_RETAIL_SALE_PRE_TO_SETTLE_AT_CASHIER);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        List<Bill> allBills = getBillFacade().findByJpqlWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);
        bills = filterNomarlBillsOnly(allBills);
    }

    public void createPharmacyPreTable() {
        Date startTime = new Date();
        createPreTable(BillType.PharmacyPre);

    }

    public void createPharmacyPreTableNotPaid() {
        Date startTime = new Date();
        createPreTableNotPaid(BillType.PharmacyPre);

    }

    public void createPharmacyPreTablePaid() {
        Date startTime = new Date();
        createPreTablePaid(BillType.PharmacyPre);

    }

    public void createPreTable(BillType bt) {
        createPreTable(bt, null);
    }

    public void createPreTable(BillType bt, Patient pt) {
        bills = null;
        String jpql;
        Map m = new HashMap();

        jpql = "select b "
                + " from PreBill b "
                + " where b.retired=false "
                + " and b.institution=:ins"
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.deptId is not null ";

        if (bt != null) {
            jpql += " and b.billType = :billType ";
            m.put("billType", bt);
        }

        if (pt != null) {
            jpql += " and b.patient=:pt ";
            m.put("pt", pt);
        }

        jpql += createPharmacyPayKeyword(m);
        jpql += " order by b.createdAt desc  ";
//

        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());
        m.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpqlWithoutCache(jpql, m, TemporalType.TIMESTAMP, 25);

    }

    public void fillPatientPreBills(BillType bt, Patient pt, Boolean paidOnly, Boolean toPayOnly) {
        bills = null;
        String jpql;
        Map m = new HashMap();

        jpql = "select b "
                + " from PreBill b "
                + " where b.retired=false "
                + " and b.institution=:ins"
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.deptId is not null ";

        if (bt != null) {
            jpql += " and b.billType = :billType ";
            m.put("billType", bt);
        }

        if (pt != null) {
            jpql += " and b.patient=:pt ";
            m.put("pt", pt);
        }

        if (paidOnly != null) {
            jpql += " and b.referenceBill is not null ";
        }

        if (toPayOnly != null) {
            jpql += " and b.referenceBill is null ";
        }

        jpql += " order by b.createdAt desc  ";

        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());
        m.put("ins", getSessionController().getInstitution());
        bills = getBillFacade().findByJpql(jpql, m, TemporalType.TIMESTAMP, 25);

    }

    public void createPreTableNotPaid(BillType bt) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false "
                + " and b.deptId is not null "
                + " and b.cancelled=false"
                + " and b.referenceBill is null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", bt);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpqlWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);

    }

    public void createPreTablePaid(BillType bt) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false "
                + " and b.deptId is not null "
                + " and b.cancelled=false"
                + " and b.referenceBill is not null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", bt);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpqlWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);

    }

    public void createGrnPaymentTable() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b"
                + " where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", BillType.GrnPayment);
        temMap.put("insTp", InstitutionType.Dealer);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createGrnPaymentTableStore() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b"
                + " where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", BillType.GrnPayment);
        temMap.put("insTp", InstitutionType.StoreDealor);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyPayment() {
        Date startTime = new Date();

        InstitutionType[] institutionTypes = {InstitutionType.Dealer};
        createGrnPaymentTable(Arrays.asList(institutionTypes), BillType.GrnPayment);

    }

    public void createStorePayment() {
        Date startTime = new Date();

        InstitutionType[] institutionTypes = {InstitutionType.StoreDealor};
        createGrnPaymentTable(Arrays.asList(institutionTypes), BillType.GrnPayment);

    }

    public void createStorePaharmacyPayment() {
        Date startTime = new Date();

        InstitutionType[] institutionTypes = {InstitutionType.Dealer, InstitutionType.StoreDealor};
        createGrnPaymentTable(Arrays.asList(institutionTypes), BillType.GrnPayment);

    }

    @Deprecated //Use supplierPaymentController
    public void createPharmacyPaymentPre() {
        InstitutionType[] institutionTypes = {InstitutionType.Dealer};
        createGrnPaymentTable(Arrays.asList(institutionTypes), BillType.GrnPaymentPre);
    }

    @Deprecated //Use supplierPaymentController
    public void createStorePaymentPre() {
        InstitutionType[] institutionTypes = {InstitutionType.StoreDealor};
        createGrnPaymentTable(Arrays.asList(institutionTypes), BillType.GrnPaymentPre);
    }

    @Deprecated //Use supplierPaymentController
    public void createStorePaharmacyPaymentPre() {
        InstitutionType[] institutionTypes = {InstitutionType.Dealer, InstitutionType.StoreDealor};
        createGrnPaymentTable(Arrays.asList(institutionTypes), BillType.GrnPaymentPre);
    }

    private void createGrnPaymentTable(List<InstitutionType> institutionTypes, BillType billType) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b "
                + " where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.toInstitution.institutionType in :insTp "
                + " and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", billType);
        temMap.put("insTp", institutionTypes);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        //  temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createGrnPaymentTableAllStore() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b "
                + " where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", BillType.GrnPayment);
        temMap.put("insTp", InstitutionType.StoreDealor);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        //  temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    @Deprecated
    public void searchDepartmentOpdBills() {
        Date startTime = new Date();

        if (fromDate != null && toDate != null) {

            long timeGapInMillis = toDate.getTime() - fromDate.getTime();

            long daysGap = timeGapInMillis / (1000 * 60 * 60 * 24);

            if (daysGap > 3) {
//                if (searchKeyword.getBillNo() == null && searchKeyword.getBillNo().trim().equals("")) {
//                    JsfUtil.addErrorMessage("Please select upto 3 days or Use filtering data option");
//                    return;
//                } else if (searchKeyword.getPatientName() == null && searchKeyword.getPatientName().trim().equals("")) {
//                    JsfUtil.addErrorMessage("Please select upto 3 days or Use filtering data option");
//                    return;
//                } else if (searchKeyword.getPatientPhone() == null && searchKeyword.getPatientPhone().trim().equals("")) {
//                    JsfUtil.addErrorMessage("Please select upto 3 days or Use filtering data option");
//                    return;
//                }
                JsfUtil.addErrorMessage("Please select upto 3 days");
                return;
            }
        }

        createTableByKeyword(BillType.OpdBill, null, sessionController.getDepartment());
        checkLabReportsApproved(bills);

    }

    public void searchDepartmentOpdBillLights() {
        Date startTime = new Date();
        fillBills(BillType.OpdBill, null, sessionController.getDepartment());

    }

    public void searchOpdBatchBills() {
        Date startTime = new Date();
        createTableByKeyword(BillType.OpdBathcBill, institution, department, fromInstitution, fromDepartment, toInstitution, toDepartment);
        checkLabReportsApproved(bills);

    }

    public void searchOpdBills() {
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        billTypesAtomics.add(BillTypeAtomic.OPD_BILL_WITH_PAYMENT);
        billTypesAtomics.add(BillTypeAtomic.OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER);
        createTableByKeyword(billTypesAtomics, institution, department, fromInstitution, fromDepartment, toInstitution, toDepartment);

    }

    public void searchOpdPackageBills() {
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        billTypesAtomics.add(BillTypeAtomic.PACKAGE_OPD_BATCH_BILL_WITH_PAYMENT);

        createTableByKeyword(billTypesAtomics, institution, department, fromInstitution, fromDepartment, toInstitution, toDepartment);

    }

    @Deprecated
    public void listPaymentsExtra() {
        String jpql = "select p "
                + " from Payment p "
                + " where p.retired=:ret "
                + " p.createdAt between :fd and :td ";
        Map params = new HashMap<>();
        params.put("ret", false);
        params.put("fd", fromDate);
        params.put("td", toDate);
        if (institution != null) {
            jpql = " and p.institution=:ins ";
            params.put("ins", institution);
        }
        if (department != null) {
            jpql = " and p.department=:dep ";
            params.put("dep", department);
        }
        payments = paymentFacade.findByJpql(jpql, params);

    }

    public void searchChannelBills() {
        Date startTime = new Date();
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        billTypesAtomics.add(BillTypeAtomic.CHANNEL_BOOKING_WITHOUT_PAYMENT);
        billTypesAtomics.add(BillTypeAtomic.CHANNEL_BOOKING_FOR_PAYMENT_ONLINE_COMPLETED_PAYMENT);
        billTypesAtomics.add(BillTypeAtomic.CHANNEL_CANCELLATION_WITH_PAYMENT_ONLINE_BOOKING);
        billTypesAtomics.add(BillTypeAtomic.CHANNEL_BOOKING_WITH_PAYMENT);
        billTypesAtomics.add(BillTypeAtomic.CHANNEL_BOOKING_WITH_PAYMENT_ONLINE);
        billTypesAtomics.add(BillTypeAtomic.CHANNEL_CANCELLATION_WITH_PAYMENT_FOR_CREDIT_SETTLED_BOOKINGS);
        billTypesAtomics.add(BillTypeAtomic.CHANNEL_PAYMENT_FOR_BOOKING_BILL);
        billTypesAtomics.add(BillTypeAtomic.CHANNEL_CANCELLATION_WITH_PAYMENT);
        billTypesAtomics.add(BillTypeAtomic.CHANNEL_CANCELLATION_WITHOUT_PAYMENT);
        billTypesAtomics.add(BillTypeAtomic.CHANNEL_REFUND);
        billTypesAtomics.add(BillTypeAtomic.CHANNEL_REFUND_WITH_PAYMENT);
        billTypesAtomics.add(BillTypeAtomic.CHANNEL_REFUND_WITH_PAYMENT_FOR_CREDIT_SETTLED_BOOKINGS);
        createTableByKeywordForChannelBills(billTypesAtomics, institution, department, fromInstitution, fromDepartment, toInstitution, toDepartment, staff, speciality);

    }

    public void fillOpdSummaryByItem() {
        Date startTime = new Date();
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        ReportTemplateType type = ReportTemplateType.ITEM_SUMMARY_BY_BILL;
        billTypesAtomics.add(BillTypeAtomic.OPD_BILL_WITH_PAYMENT);
        billTypesAtomics.add(BillTypeAtomic.PACKAGE_OPD_BILL_WITH_PAYMENT);
        billTypesAtomics.add(BillTypeAtomic.OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER);
        bundle = reportTemplateController.generateReport(type,
                billTypesAtomics,
                null,
                fromDate,
                toDate,
                institution,
                department,
                fromInstitution,
                fromDepartment,
                toInstitution,
                toDepartment,
                webUser,
                creditCompany,
                startBillId,
                endBillId);

    }

    public void searchChannelProfessionalPaymentBills() {
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES_RETURN);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION);
        createTableByKeyword(billTypesAtomics, institution, department, null, null, null, null);
        checkLabReportsApproved(bills);

    }

    public void searchOpdProfessionalPaymentBills() {
        reportTimerController.trackReportExecution(() -> {
            List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
            bundle = createBundleByKeywordForBills(billTypesAtomics, institution, department, null, null, null, null);
            bundle.calculateTotalByBills();
        }, ProfessionalPaymentReport.OPD_PROFESSIONAL_PAYMENTS_REPORT, sessionController.getLoggedUser());
    }

    public void searchMyProfessionalPaymentBills() {
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
        billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
        billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
        billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
        billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
        bundle = billService.createBundleByKeywordForBills(billTypesAtomics,
                institution,
                department,
                null,
                null,
                null,
                null,
                sessionController.getLoggedUser(),
                fromDate,
                toDate,
                searchKeyword);
        bundle.calculateTotalByBills();
    }

    public void processWhtReport() {
        reportTimerController.trackReportExecution(() -> {
            switch (reportType) {
                case "individualReceipts":
                    processWhtReceipts();
                    break;
                case "monthlySummary":
                    processWhtMonthlySymmary();
                    break;
                case "consultantSummary":
                    processWhtConsultantSymmary();
                    break;
            }
        }, FinancialReport.WITHHOLDING_TAX_REPORT, sessionController.getLoggedUser());
    }

    public void processWhtReceipts() {
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        if (searchType == null || searchType.isEmpty()) {
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION);
        } else if (searchType.equalsIgnoreCase("op")) {
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
        } else if (searchType.equalsIgnoreCase("ip")) {
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN);
        } else if (searchType.equalsIgnoreCase("ch")) {
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION);
        }
        bundle = createBundleForBills(billTypesAtomics, institution, department, site, null, null, null, null, speciality, staff);
        bundle.calculateTotalNetTotalTaxByBills();
    }

    public void processWhtMonthlySymmary() {
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        if (searchType == null || searchType.isEmpty()) {
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION);
        } else if (searchType.equalsIgnoreCase("op")) {
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
        } else if (searchType.equalsIgnoreCase("ip")) {
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN);
        } else if (searchType.equalsIgnoreCase("ch")) {
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION);
        }
        bundle = createBundleForBills(billTypesAtomics, institution, department, site, null, null, null, null, speciality, staff);
        bundle = bundle.createBundleByAggregatingMonthlyTotalsFromBills();
        bundle.calculateTotalByRowTotals();
    }

    public void processWhtConsultantSymmary() {
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        if (searchType == null || searchType.isEmpty()) {
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION);
        } else if (searchType.equalsIgnoreCase("op")) {
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
        } else if (searchType.equalsIgnoreCase("ip")) {
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN);
        } else if (searchType.equalsIgnoreCase("ch")) {
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION);
        }
        bundle = createBundleForBills(billTypesAtomics, institution, department, site, null, null, null, null, speciality, staff);
        bundle = bundle.createBundleByAggregatingConsultantTotalsFromBills();
        bundle.calculateTotalByRowTotals();
    }

    public void updateToStaffForChannelProfessionalPaymentBills() {

        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES_RETURN);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION);
        billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
        billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
        billTypesAtomics.add(BillTypeAtomic.INWARD_SERVICE_PROFESSIONAL_PAYMENT_BILL);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);

        createTableByKeywordForBillsWithoutToStaff(billTypesAtomics, institution, department, null, null, null, null);

        for (Bill b : bills) {
            Staff bs = null;
            if (b.getToStaff() == null) {

                List<BillFee> bfs;
                if (b.getBillFees() == null) {
                    bfs = billBean.getBillFee(b);
                } else {
                    for (BillFee tbf : b.getBillFees()) {
                    }
                    bfs = b.getBillFees();
                }

                if (bfs != null) {
                    boolean sameStaff = true;

                    for (BillFee bf : bfs) {

                        if (bf.getReferenceBillFee() != null) {
                            if (bf.getReferenceBillFee().getStaff() != null) {
                                if (bs == null) {
                                    bs = bf.getReferenceBillFee().getStaff();
                                } else {
                                    if (!bs.equals(bf.getReferenceBillFee().getStaff())) {
                                        sameStaff = false;
                                    }
                                }

                            } else {
                                if (bf.getReferenceBillFee().getBill().getStaff() != null) {
                                    if (bs == null) {
                                        bs = bf.getReferenceBillFee().getBill().getStaff();
                                    } else {
                                        if (!bs.equals(bf.getReferenceBillFee().getBill().getStaff())) {
                                            sameStaff = false;
                                        }
                                    }
                                }

                            }
                        }
                    }

                    if (sameStaff && bs != null) {
                        b.setToStaff(bs);
                        if (b.getComments() == null) {
                            b.setComments("To Staff Added " + bs + " at " + new Date());
                        }
                        billFacade.edit(b);
                        bs = null;
                    }
                }
            }
        }

    }

    public void searchProfessionalPaymentBills() {
        Date startTime = new Date();
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES_RETURN);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION);
        billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
        billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
        billTypesAtomics.add(BillTypeAtomic.INWARD_SERVICE_PROFESSIONAL_PAYMENT_BILL);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);

        createTableByKeyword(billTypesAtomics, institution, department, null, null, null, null);

    }

    public void searchChannelProfessionalPaymentBillFees() {
        Date startTime = new Date();
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES_RETURN);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION);
        createTableByKeywordForBillFees(billTypesAtomics, institution, department, null, null, null, null);

    }

    public void searchOpdProfessionalPaymentBillFees() {
        reportTimerController.trackReportExecution(() -> {
            List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
            billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
            billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
            bundle = createBundleByKeywordForBillFees(billTypesAtomics, institution, department, null, null, null, null, category);
            bundle.calculateTotalsForProfessionalFees();
        }, ProfessionalPaymentReport.OPD_PROFESSIONAL_FEE_PAYMENTS_REPORT, sessionController.getLoggedUser());
    }

    public String searchOpdProfessionalPayments() {
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
        billTypesAtomics.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
        billTypesAtomics.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
        createTableByKeywordForBillFee(billTypesAtomics, institution, department, null, null, null, null, staff);
        createTableByKeywords(billTypesAtomics, institution, department, null, null, null, null, staff);
        return "/opd/opd_doctor_payments_done";
    }

    public void listOpdBatcuBills() {
        Date startTime = new Date();
        createTableByKeyword(BillType.OpdBathcBill, institution, department);
        checkLabReportsApproved(bills);
    }

    public void listOpdBills() {
        Date startTime = new Date();
        createTableByKeyword(BillType.OpdBill);
        checkLabReportsApproved(bills);

    }

    @Deprecated
    public void createCollectingCentreBillSearch() {
        createCCBillTableByKeyword(BillType.CollectingCentreBill, null, null);
        //checkLabReportsApproved(bills);

    }

    public void collectingCentreBillSearch() {
        createCcBillList(BillType.CollectingCentreBill, null, null);
    }

    public void createStaffCreditBillList() {
        String jpql;
        Map m = new HashMap();
        List<BillTypeAtomic> btas = new ArrayList<>();
        jpql = "select b from Bill b "
                + "where b.toStaff is not null "
                + "and b.createdAt between :fromDate and :toDate "
                + "and b.retired = false "
                + "and b.cancelled = false "
                + "and b.refunded = false ";
        jpql += " and b.billTypeAtomic in :btas ";
        if (staff != null) {
            jpql += " and b.toStaff=:staff ";
            m.put("staff", staff);
        }

        btas.addAll(BillTypeAtomic.findByServiceType(ServiceType.OPD));
        btas.addAll(BillTypeAtomic.findByServiceType(ServiceType.CHANNELLING));
        btas.removeAll(BillTypeAtomic.findByCategory(BillCategory.PAYMENTS));

        jpql += " order by b.createdAt desc";

        m.put("fromDate", fromDate);
        m.put("toDate", toDate);
        m.put("btas", btas);

        bills = getBillFacade().findByJpql(jpql, m, TemporalType.TIMESTAMP);
    }

    public void createGeneralCreditBillList() {
        String jpql;
        Map m = new HashMap();
        List<BillTypeAtomic> btas = new ArrayList<>();
        jpql = "select b from Bill b "
                + "where b.creditCompany is not null "
                + "and b.createdAt between :fromDate and :toDate "
                + "and b.retired = false ";
        jpql += " and b.billTypeAtomic in :btas ";

        if (creditCompany != null) {
            jpql += " and b.creditCompany=:cc ";
            m.put("cc", creditCompany);
        }
        //TODO: Use a List of Bill Type Atomics instead of calling the findBy methods
        btas.addAll(BillTypeAtomic.findByServiceType(ServiceType.OPD));
        btas.addAll(BillTypeAtomic.findByServiceType(ServiceType.CHANNELLING));
        btas.removeAll(BillTypeAtomic.findByCategory(BillCategory.PAYMENTS));
        btas.removeAll(BillTypeAtomic.findByCategory(BillCategory.CANCELLATION));
        btas.removeAll(BillTypeAtomic.findByCategory(BillCategory.REFUND));

        jpql += " order by b.createdAt desc";

        m.put("fromDate", fromDate);
        m.put("toDate", toDate);
        m.put("btas", btas);

        bills = getBillFacade().findByJpql(jpql, m, TemporalType.TIMESTAMP);
    }

    public void createCCBillTableByKeyword(BillType billType, Institution ins, Department dep) {
        billLights = null;
        String sql;
        Map temMap = new HashMap();
        sql = "select new com.divudi.core.light.common.BillLight(bill.id, bill.deptId, bill.createdAt, bill.fromInstitution.name, bill.institution.name, bill.toDepartment.name, bill.creater.name, bill.patient.person.name, bill.patient.person.phone, bill.total, bill.discount, bill.netTotal, bill.patient.id) "
                + " from BilledBill bill "
                + " where bill.billType = :billType "
                + " and bill.createdAt between :fromDate and :toDate "
                + " and bill.retired=false ";

        if (ins != null) {
            sql += " and bill.institution=:ins ";
            temMap.put("ins", ins);
        }

        if (dep != null) {
            sql += " and bill.department=:dep ";
            temMap.put("dep", dep);
        }

        if (getInstitution() != null) {
            sql += " and  ((bill.fromInstitution) =:ccName )";
            temMap.put("ccName", getInstitution());
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  ((bill.fromInstitution.institutionCode) like :code )";
            temMap.put("code", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((bill.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (((bill.insId) like :billNo )or((bill.deptId) like :billNo ))";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((bill.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((bill.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }
        sql += " order by bill.createdAt desc  ";

        temMap.put("billType", billType);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        billLights = getBillFacade().findLightsByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    public void createCcBillList(BillType billType, Institution ins, Department dep) {
        bills = null;
        String sql;
        Map temMap = new HashMap();
        sql = "select bill "
                + " from BilledBill bill "
                + " where bill.billType = :billType "
                + " and bill.createdAt between :fromDate and :toDate "
                + " and bill.retired=false ";

        if (ins != null) {
            sql += " and bill.institution=:ins ";
            temMap.put("ins", ins);
        }

        if (dep != null) {
            sql += " and bill.department=:dep ";
            temMap.put("dep", dep);
        }

        if (getInstitution() != null) {
            sql += " and  ((bill.fromInstitution) =:ccName )";
            temMap.put("ccName", getInstitution());
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  ((bill.fromInstitution.institutionCode) like :code )";
            temMap.put("code", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((bill.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (((bill.insId) like :billNo )or((bill.deptId) like :billNo ))";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((bill.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((bill.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }
        sql += " order by bill.createdAt desc  ";

        temMap.put("billType", billType);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    public void listOpdBilledBills() {
        listBills(BillType.OpdBill, BilledBill.class, false, false, null, null, null, null, null, null);
    }

    public void listBills(BillType billType, Class billClass, Boolean onlyCancelledBills, Boolean onlyReturnedBills,
            Institution fromInstitution, Department fromDepartment,
            Institution toInstitution, Department toDepartment,
            Doctor referredDoctor, Institution referredInstitution) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (billClass != null) {
            sql += " and type(b.bill)=:class ";
            temMap.put("class", billClass);
        }

        if (onlyCancelledBills == true) {
            sql += " and b.cancelled=:cancelled ";
            temMap.put("cancelled", true);
        }
        if (onlyReturnedBills == true) {
            sql += " and b.refunded=:refunded ";
            temMap.put("refunded", true);
        }
        if (fromInstitution != null) {
            sql += " and b.fromInstitution=:fromIns ";
            temMap.put("fromIns", fromInstitution);
        }
        if (fromDepartment != null) {
            sql += " and b.fromDepartment=:fromDepartment ";
            temMap.put("fromDepartment", fromDepartment);
        }
        if (toInstitution != null) {
            sql += " and b.toInstitution=:toIns ";
            temMap.put("toIns", toInstitution);
        }
        if (toDepartment != null) {
            sql += " and b.toDepartment=:toDepartment ";
            temMap.put("toDepartment", toDepartment);
        }
        if (referredDoctor != null) {
            sql += " and b.referredBy=:referredDoctor ";
            temMap.put("fromIns", referredDoctor);
        }
        if (referredInstitution != null) {
            sql += " and b.referredByInstitution=:referredInstitution ";
            temMap.put("fromDepartment", referredInstitution);
        }

        /**
         *
         *
         *
         *
         * temp.setStaff(staff); temp.setToStaff(toStaff);
         * temp.setReferredBy(referredBy); temp.setReferenceNumber(referralId);
         * temp.setReferredByInstitution(referredByInstitution);
         * temp.setCreditCompany(creditCompany);
         * temp.setCollectingCentre(collectingCentre);
         *
         */
        sql += " order by b.createdAt desc  ";

        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void fillOpdClients() {
        fillClients(BillType.OpdBill);
    }

    public void fillClients(BillType billType) {
        patients = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select distinct(b.patient) from BilledBill b where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (fromDepartment != null) {
            sql += " and b.fromDepartment=:fdep ";
            temMap.put("fdep", fromDepartment);
        }
        if (toDepartment != null) {
            sql += " and b.toDepartment=:tdep ";
            temMap.put("tdep", toDepartment);
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        patients = getPatientFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createTableByKeyword(BillType billType) {
        fillBills(billType, null, null);
    }

    public void fillBills(BillType billType, Institution ins, Department dep) {
        bills = null;
        String sql;
        Map temMap = new HashMap();
        sql = "select new com.divudi.core.light.common.BillLight(bill.id, bill.deptId, bill.createdAt, bill.institution.name, bill.toDepartment.name, bill.creater.name, bill.patient.person.name, bill.patient.person.phone, bill.total, bill.discount, bill.netTotal, bill.patient.id) "
                + " from BilledBill bill "
                + " where bill.billType = :billType "
                + " and bill.createdAt between :fromDate and :toDate "
                + " and bill.retired=false ";
        if (ins != null) {
            sql += " and bill.institution=:ins ";
            temMap.put("ins", ins);
        }

        if (dep != null) {
            sql += " and bill.department=:dep ";
            temMap.put("dep", dep);
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((bill.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (((bill.insId) like :billNo )or((bill.deptId) like :billNo ))";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((bill.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((bill.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }
        sql += " order by bill.createdAt desc  ";

        temMap.put("billType", billType);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        billLights = getBillFacade().findLightsByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public List<Bill> fillBills(BillType billType, Institution ins, Department dep, Patient patient) {
        return fillBills(billType, ins, dep, patient, 10);
    }

    public List<Bill> fillBills(BillType billType, Institution ins, Department dep, Patient patient, Integer maxCount) {
        List<Bill> bs;
        String jpql;
        Map m = new HashMap();
        jpql = "select bill"
                + " from BilledBill bill "
                + " where bill.retired=:ret ";
        m.put("ret", false);
        if (billType != null) {
            jpql += " and bill.billType=:billType ";
            m.put("billType", billType);
        }

        if (ins != null) {
            jpql += " and bill.institution=:ins ";
            m.put("ins", ins);
        }

        if (dep != null) {
            jpql += " and bill.department=:dep ";
            m.put("dep", dep);
        }
        if (patient != null) {
            jpql += " and  bill.patient=:pt";
            m.put("pt", patient);
        }

        jpql += " order by bill.id desc  ";

        bs = getBillFacade().findByJpql(jpql, m, TemporalType.TIMESTAMP, maxCount);

        return bs;
    }

    public List<BillLight> listBillsLights(
            BillType billType,
            Institution ins,
            Department dep,
            SearchKeyword searchKw,
            Date fd,
            Date td) {
        String sql;
        Map temMap = new HashMap();
        sql = "select new com.divudi.core.light.common.BillLight(bill.id, bill.deptId, bill.createdAt, bill.institution.name, bill.toDepartment.name, bill.creater.name, bill.patient.person.name, bill.patient.person.phone, bill.total, bill.discount, bill.netTotal, bill.patient.id) "
                + " from BilledBill bill "
                + " where bill.billType = :billType "
                + " and bill.createdAt between :fromDate and :toDate "
                + " and bill.retired=false ";
        if (ins != null) {
            sql += " and bill.institution=:ins ";
            temMap.put("ins", ins);
        }
        if (dep != null) {
            sql += " and bill.department=:dep ";
            temMap.put("dep", dep);
        }
        if (searchKw != null && !searchKw.getPatientName().trim().equals("")) {
            sql += " and  ((bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + searchKw.getPatientName().trim().toUpperCase() + "%");
        }
        if (searchKw != null && searchKw.getPatientPhone() != null && !searchKw.getPatientPhone().trim().equals("")) {
            sql += " and  ((bill.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + searchKw.getPatientPhone().trim().toUpperCase() + "%");
        }
        if (searchKw != null && searchKw.getBillNo() != null && !searchKw.getBillNo().trim().equals("")) {
            sql += " and  (((bill.insId) like :billNo )or((bill.deptId) like :billNo ))";
            temMap.put("billNo", "%" + searchKw.getBillNo().trim().toUpperCase() + "%");
        }
        if (searchKw != null && searchKw.getNetTotal() != null && !searchKw.getNetTotal().trim().equals("")) {
            sql += " and  ((bill.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + searchKw.getNetTotal().trim().toUpperCase() + "%");
        }
        if (searchKw != null && searchKw.getTotal() != null && !searchKw.getTotal().trim().equals("")) {
            sql += " and  ((bill.total) like :total )";
            temMap.put("total", "%" + searchKw.getTotal().trim().toUpperCase() + "%");
        }
        sql += " order by bill.createdAt desc  ";
        temMap.put("billType", billType);
        temMap.put("toDate", td);
        temMap.put("fromDate", fd);
        return (List<BillLight>) getBillFacade().findLightsByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    @Deprecated
    public void createTableByKeyword(BillType billType, Institution ins, Department dep) {
        createTableByKeyword(billType, ins, dep, null, null, null, null);
    }

    public ReportTemplateRowBundle createBundleByKeywordForBills(List<BillTypeAtomic> billTypesAtomics,
            Institution ins, Department dep,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep) {
        return billService.createBundleByKeywordForBills(billTypesAtomics, ins, dep, fromIns, fromDep, toIns, toDep, null, fromDate, toDate, searchKeyword);
    }

    public ReportTemplateRowBundle createBundleForBills(List<BillTypeAtomic> billTypesAtomics,
            Institution ins,
            Department dep,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep) {
        return createBundleForBills(
                billTypesAtomics,
                ins,
                dep,
                null,
                fromIns,
                fromDep,
                toIns,
                toDep,
                null,
                null
        );
    }

    public ReportTemplateRowBundle createBundleForBills(List<BillTypeAtomic> billTypesAtomics,
            Institution ins,
            Department dep,
            Institution site,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep,
            Speciality paramSpeciality,
            Staff paramStaff) {
        ReportTemplateRowBundle outputBundle = new ReportTemplateRowBundle();
        List<ReportTemplateRow> outputRows;
        bills = null;
        String jpql;
        Map params = new HashMap();

        jpql = "select new com.divudi.core.data.ReportTemplateRow(b) "
                + " from Bill b "
                + " where b.billTypeAtomic in :billTypesAtomics "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (ins != null) {
            jpql += " and b.institution=:ins ";
            params.put("ins", ins);
        }

        if (dep != null) {
            jpql += " and b.department=:dep ";
            params.put("dep", dep);
        }

        if (site != null) {
            jpql += " and b.department.site=:site ";
            params.put("site", site);
        }

        if (toDep != null) {
            jpql += " and b.toDepartment=:todep ";
            params.put("todep", toDep);
        }

        if (fromDep != null) {
            jpql += " and b.fromDepartment=:fromdep ";
            params.put("fromdep", fromDep);
        }

        if (fromIns != null) {
            jpql += " and b.fromInstitution=:fromins ";
            params.put("fromins", fromIns);
        }

        if (toIns != null) {
            jpql += " and b.toInstitution=:toins ";
            params.put("toins", toIns);
        }

        if (paramStaff != null) {
            jpql += " and b.staff=:staff ";
            params.put("staff", paramStaff);
        }

        if (paramSpeciality != null) {
            jpql += " and b.staff.speciality=:speciality ";
            params.put("speciality", paramSpeciality);
        }

        jpql += " order by b.createdAt desc  ";

        params.put("billTypesAtomics", billTypesAtomics);
        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        outputRows = (List<ReportTemplateRow>) getBillFacade().findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);
        outputBundle.setReportTemplateRows(outputRows);
        return outputBundle;
    }

    public void createTableByKeyword(List<BillTypeAtomic> billTypesAtomics,
            Institution ins, Department dep,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b "
                + " from Bill b "
                + " where b.billTypeAtomic in :billTypesAtomics "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (showLoggedDepartmentOnly) {
            Department dept = sessionController.getDepartment();
            if (dept != null) {
                sql += " and b.department=:dept ";
                temMap.put("dept", dept);
            }
        }

        if (ins != null) {
            sql += " and b.institution=:ins ";
            temMap.put("ins", ins);
        }

        if (dep != null) {
            sql += " and b.department=:dep ";
            temMap.put("dep", dep);
        }

        if (toDep != null) {
            sql += " and b.toDepartment=:todep ";
            temMap.put("todep", toDep);
        }

        if (fromDep != null) {
            sql += " and b.fromDepartment=:fromdep ";
            temMap.put("fromdep", fromDep);
        }

        if (fromIns != null) {
            sql += " and b.fromInstitution=:fromins ";
            temMap.put("fromins", fromIns);
        }

        if (toIns != null) {
            sql += " and b.toInstitution=:toins ";
            temMap.put("toins", toIns);
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  b.deptId like :billNo";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  (b.patient.code like :code OR b.patient.phn like :phn ) ";
            temMap.put("code", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
            temMap.put("phn", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billTypesAtomics", billTypesAtomics);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createTableByKeywordForChannelBills(List<BillTypeAtomic> billTypesAtomics,
            Institution ins, Department dep,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep,
            Staff consultant,
            Speciality speciality
    ) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b "
                + " from Bill b "
                + " where b.billTypeAtomic in :billTypesAtomics "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (ins != null) {
            sql += " and b.institution=:ins ";
            temMap.put("ins", ins);
        }

        if (dep != null) {
            sql += " and b.department=:dep ";
            temMap.put("dep", dep);
        }

        if (toDep != null) {
            sql += " and b.toDepartment=:todep ";
            temMap.put("todep", toDep);
        }

        if (fromDep != null) {
            sql += " and b.fromDepartment=:fromdep ";
            temMap.put("fromdep", fromDep);
        }

        if (fromIns != null) {
            sql += " and b.fromInstitution=:fromins ";
            temMap.put("fromins", fromIns);
        }

        if (toIns != null) {
            sql += " and b.toInstitution=:toins ";
            temMap.put("toins", toIns);
        }

        if (consultant != null) {
            sql += " and b.singleBillSession.sessionInstance.originatingSession.staff=:toStaff ";
            temMap.put("toStaff", consultant);
        }

        if (speciality != null) {
            sql += " and b.singleBillSession.sessionInstance.originatingSession.staff.speciality=:sp ";
            temMap.put("sp", speciality);
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  b.deptId like :billNo";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billTypesAtomics", billTypesAtomics);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createTableByKeywords(List<BillTypeAtomic> billTypesAtomics,
            Institution ins, Department dep,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep,
            Staff stf) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b "
                + " from Bill b "
                + " where b.billTypeAtomic in :billTypesAtomics "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (ins != null) {
            sql += " and b.institution=:ins ";
            temMap.put("ins", ins);
        }

        if (dep != null) {
            sql += " and b.department=:dep ";
            temMap.put("dep", dep);
        }

        if (toDep != null) {
            sql += " and b.toDepartment=:todep ";
            temMap.put("todep", toDep);
        }

        if (fromDep != null) {
            sql += " and b.fromDepartment=:fromdep ";
            temMap.put("fromdep", fromDep);
        }

        if (fromIns != null) {
            sql += " and b.fromInstitution=:fromins ";
            temMap.put("fromins", fromIns);
        }

        if (toIns != null) {
            sql += " and b.toInstitution=:toins ";
            temMap.put("toins", toIns);
        }

        if (stf != null) {
            sql += " and b.toStaff=:staff ";
            temMap.put("staff", stf);
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billTypesAtomics", billTypesAtomics);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createTableByKeywordForBillFees(List<BillTypeAtomic> billTypesAtomics,
            Institution ins, Department dep,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep) {
        createTableByKeywordForBillFees(billTypesAtomics, ins, dep, fromIns, fromDep, toIns, toDep, null);
    }

    public ReportTemplateRowBundle createBundleByKeywordForBillFees(List<BillTypeAtomic> billTypesAtomics,
            Institution ins, Department dep,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep,
            Category cat) {
        ReportTemplateRowBundle outputBundle = new ReportTemplateRowBundle();
        List<ReportTemplateRow> outputRows;
        String sql;
        Map temMap = new HashMap();

        sql = "select new com.divudi.core.data.ReportTemplateRow(bf) "
                + " from BillFee bf "
                + " where bf.bill.billTypeAtomic in :billTypesAtomics "
                + " and bf.bill.createdAt between :fromDate and :toDate "
                + " and bf.bill.retired=false ";

        if (ins != null) {
            sql += " and bf.bill.institution=:ins ";
            temMap.put("ins", ins);
        }

        if (dep != null) {
            sql += " and bf.bill.department=:dep ";
            temMap.put("dep", dep);
        }

        if (toDep != null) {
            sql += " and bf.bill.toDepartment=:todep ";
            temMap.put("todep", toDep);
        }

        if (fromDep != null) {
            sql += " and bf.bill.fromDepartment=:fromdep ";
            temMap.put("fromdep", fromDep);
        }

        if (fromIns != null) {
            sql += " and bf.bill.fromInstitution=:fromins ";
            temMap.put("fromins", fromIns);
        }

        if (toIns != null) {
            sql += " and bf.bill.toInstitution=:toins ";
            temMap.put("toins", toIns);
        }

        if (cat != null) {
            sql += " and bf.referenceBillFee.billItem.bill.category=:rbfcc ";
            temMap.put("rbfcc", cat);
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((bf.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((bf.bill.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  bf.bill.deptId like :billNo";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((bf.bill.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((bf.bill.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (mrnNo != null && !mrnNo.isEmpty()) {
            sql += " and UPPER(bf.patient.phn) LIKE :phn ";
            temMap.put("phn", "%" + mrnNo.toUpperCase() + "%");
        }

        sql += " order by bf.bill.createdAt desc  ";

        temMap.put("billTypesAtomics", billTypesAtomics);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        outputRows = (List<ReportTemplateRow>) getBillFacade().findLightsByJpql(sql, temMap, TemporalType.TIMESTAMP);
        outputBundle.setReportTemplateRows(outputRows);
        return outputBundle;
    }

    public void createTableByKeywordForBillFees(List<BillTypeAtomic> billTypesAtomics,
            Institution ins, Department dep,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep,
            Category cat) {

        String sql;
        Map temMap = new HashMap();

        sql = "select bf "
                + " from BillFee bf "
                + " where bf.bill.billTypeAtomic in :billTypesAtomics "
                + " and bf.bill.createdAt between :fromDate and :toDate "
                + " and bf.bill.retired=false ";

        if (ins != null) {
            sql += " and bf.bill.institution=:ins ";
            temMap.put("ins", ins);
        }

        if (dep != null) {
            sql += " and bf.bill.department=:dep ";
            temMap.put("dep", dep);
        }

        if (toDep != null) {
            sql += " and bf.bill.toDepartment=:todep ";
            temMap.put("todep", toDep);
        }

        if (fromDep != null) {
            sql += " and bf.bill.fromDepartment=:fromdep ";
            temMap.put("fromdep", fromDep);
        }

        if (fromIns != null) {
            sql += " and bf.bill.fromInstitution=:fromins ";
            temMap.put("fromins", fromIns);
        }

        if (toIns != null) {
            sql += " and bf.bill.toInstitution=:toins ";
            temMap.put("toins", toIns);
        }

        if (cat != null) {
            sql += " and bf.referenceBillFee.billItem.bill.category=:rbfcc ";
            temMap.put("rbfcc", cat);
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((bf.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((bf.bill.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  bf.bill.deptId like :billNo";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((bf.bill.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((bf.bill.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by bf.bill.createdAt desc  ";

        temMap.put("billTypesAtomics", billTypesAtomics);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        billFees = getBillFeeFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    public void createTableByKeywordForBillFee(List<BillTypeAtomic> billTypesAtomics,
            Institution ins, Department dep,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep,
            Staff stf) {
        createTableByKeywordBillFee(billTypesAtomics, ins, dep, fromIns, fromDep, toIns, toDep, stf);
    }

    public void createTableByKeywordBillFee(List<BillTypeAtomic> billTypesAtomics,
            Institution ins, Department dep,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep,
            Staff stf) {

        String sql;
        Map temMap = new HashMap();

        sql = "select bf "
                + " from BillFee bf "
                + " where bf.bill.billTypeAtomic in :billTypesAtomics "
                + " and bf.bill.createdAt between :fromDate and :toDate "
                + " and bf.bill.retired=false ";

        if (ins != null) {
            sql += " and bf.bill.institution=:ins ";
            temMap.put("ins", ins);
        }

        if (dep != null) {
            sql += " and bf.bill.department=:dep ";
            temMap.put("dep", dep);
        }

        if (toDep != null) {
            sql += " and bf.bill.toDepartment=:todep ";
            temMap.put("todep", toDep);
        }

        if (fromDep != null) {
            sql += " and bf.bill.fromDepartment=:fromdep ";
            temMap.put("fromdep", fromDep);
        }

        if (fromIns != null) {
            sql += " and bf.bill.fromInstitution=:fromins ";
            temMap.put("fromins", fromIns);
        }

        if (toIns != null) {
            sql += " and bf.bill.toInstitution=:toins ";
            temMap.put("toins", toIns);
        }

        if (stf != null) {
            sql += " and bf.bill.toStaff=:staff ";
            temMap.put("staff", stf);
        }

        sql += " order by bf.bill.createdAt desc  ";

        temMap.put("billTypesAtomics", billTypesAtomics);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        billFees = getBillFeeFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    public void createTableByKeywordForBillsWithoutToStaff(List<BillTypeAtomic> billTypesAtomics,
            Institution ins, Department dep,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b "
                + " from Bill b "
                + " where b.billTypeAtomic in :billTypesAtomics "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.toStaff is null ";

        if (ins != null) {
            sql += " and b.institution=:ins ";
            temMap.put("ins", ins);
        }

        if (dep != null) {
            sql += " and b.department=:dep ";
            temMap.put("dep", dep);
        }

        if (toDep != null) {
            sql += " and b.toDepartment=:todep ";
            temMap.put("todep", toDep);
        }

        if (fromDep != null) {
            sql += " and b.fromDepartment=:fromdep ";
            temMap.put("fromdep", fromDep);
        }

        if (fromIns != null) {
            sql += " and b.fromInstitution=:fromins ";
            temMap.put("fromins", fromIns);
        }

        if (toIns != null) {
            sql += " and b.toInstitution=:toins ";
            temMap.put("toins", toIns);
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  b.deptId like :billNo";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billTypesAtomics", billTypesAtomics);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 100);

    }

    public void createTableByKeyword(BillType billType,
            Institution ins, Department dep,
            Institution fromIns,
            Department fromDep,
            Institution toIns,
            Department toDep) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b "
                + " from BilledBill b "
                + " where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (ins != null) {
            sql += " and b.institution=:ins ";
            temMap.put("ins", ins);
        }

        if (dep != null) {
            sql += " and b.department=:dep ";
            temMap.put("dep", dep);
        }

        if (toDep != null) {
            sql += " and b.toDepartment=:todep ";
            temMap.put("todep", toDep);
        }

        if (fromDep != null) {
            sql += " and b.fromDepartment=:fromdep ";
            temMap.put("fromdep", fromDep);
        }

        if (fromIns != null) {
            sql += " and b.fromInstitution=:fromins ";
            temMap.put("fromins", fromIns);
        }

        if (toIns != null) {
            sql += " and b.toInstitution=:toins ";
            temMap.put("toins", toIns);
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  b.deptId like :billNo";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void clearOpdBillSearchData() {
        getSearchKeyword().setPatientName(null);
        getSearchKeyword().setPatientPhone(null);
        getSearchKeyword().setTotal(null);
        getSearchKeyword().setNetTotal(null);
        getSearchKeyword().setBillNo(null);
        fromInstitution = null;
        fromDepartment = null;
        toInstitution = null;
        toDepartment = null;
        selectedOpdPackageBillSelector = null;

    }

    public void clearChannelBillSearchData() {
        getSearchKeyword().setPatientName(null);
        getSearchKeyword().setPatientPhone(null);
        getSearchKeyword().setTotal(null);
        getSearchKeyword().setNetTotal(null);
        getSearchKeyword().setBillNo(null);
        fromInstitution = null;
        fromDepartment = null;
        toInstitution = null;
        toDepartment = null;

    }

    public void processAllFinancialTransactionalSummary() {
        billSummaryRows = null;
        grossTotal = 0.0;
        discount = 0.0;
        netTotal = 0.0;
        String jpql;
        Map params = new HashMap();
        List<BillTypeAtomic> billTypesToFilter = new ArrayList<>();
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_IN));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_OUT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT_REVERSE));
        jpql = "select new com.divudi.core.light.common.BillSummaryRow("
                + "b.billTypeAtomic, "
                + "sum(b.total), "
                + "sum(b.discount), "
                + "sum(b.netTotal),"
                + "count(b)) "
                + " from Bill b "
                + " where b.retired=:ret"
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.billTypeAtomic in :abts ";

        Bill b = new Bill();
        b.getTotal();
        b.getDiscount();
        b.getNetTotal();
        b.getBillTypeAtomic();
        b.getCreater();
        if (institution == null && department == null) {
            jpql += " and b.institution=:ins";
            params.put("ins", sessionController.getInstitution());

        } else if (institution != null && department == null) {
            jpql += " and b.institution=:ins";
            params.put("ins", getInstitution());

            jpql += " and b.department=:dept";
            params.put("dept", sessionController.getDepartment());

        } else if (institution != null && department != null) {
            jpql += " and b.institution=:ins";
            params.put("ins", getInstitution());

            jpql += " and b.department=:dept";
            params.put("dept", getDepartment());
        }

        if (webUser != null) {
            jpql += " and b.creater=:wu";
            params.put("wu", webUser);
        }

        jpql += " group by b.billTypeAtomic order by b.billTypeAtomic";

        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        params.put("ret", false);
        params.put("abts", billTypesToFilter);

        billSummaryRows = getBillFacade().findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);

        for (BillSummaryRow bss : billSummaryRows) {
            grossTotal += bss.getGrossTotal();
            discount += bss.getDiscount();
            netTotal += bss.getNetTotal();
        }
    }

    public void processUserFinancialTransactionalSummaryByBillForBillTypeAtomic() {
        reportTemplateType = ReportTemplateType.BILL_TYPE_ATOMIC_SUMMARY_USING_BILLS;
        billSummaryRows = null;
        grossTotal = 0.0;
        discount = 0.0;
        netTotal = 0.0;
        String jpql;
        Map params = new HashMap();
        List<BillTypeAtomic> billTypesToFilter = new ArrayList<>();
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_IN));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_OUT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT_REVERSE));
        jpql = "select new com.divudi.core.light.common.BillSummaryRow("
                + "b.billTypeAtomic, "
                + "sum(b.total), "
                + "sum(b.discount), "
                + "sum(b.netTotal),"
                + "count(b)) "
                + " from Bill b "
                + " where b.retired=:ret"
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.billTypeAtomic in :abts "
                + " and b.creater=:user";

//        Bill b = new Bill();
//        b.getTotal();
//        b.getDiscount();
//        b.getNetTotal();
//        b.getBillTypeAtomic();
//        b.getCreater();
        jpql += " group by b.billTypeAtomic order by b.billTypeAtomic";

        params.put("user", getWebUser());
        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        params.put("ret", false);
        params.put("abts", billTypesToFilter);

        billSummaryRows = getBillFacade().findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);

        for (BillSummaryRow bss : billSummaryRows) {
            grossTotal += bss.getGrossTotal();
            discount += bss.getDiscount();
            netTotal += bss.getNetTotal();
        }
    }

    public void processUserFinancialTransactionalSummarybyBillForPaymentMethodAndBillTypeAtomic() {
        reportTemplateType = ReportTemplateType.BILLT_TYPE_AND_PAYMENT_METHOD_SUMMARY_PAYMENTS;
        if (institution == null) {
            setDepartments(null);
        }
        billSummaryRows = null;
        totalPaying = 0.0;
        cashTotal = 0.0;
        cardTotal = 0.0;
        chequeTotal = 0.0;
        slipTotal = 0.0;
        totalOfOtherPayments = 0.0;

        String jpql;
        Map params = new HashMap();
        List<BillTypeAtomic> billTypesToFilter = new ArrayList<>();
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_IN));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_OUT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT_REVERSE));

        jpql = "select new com.divudi.core.light.common.BillSummaryRow("
                + "p.bill.billTypeAtomic, "
                + "sum(p.paidValue), "
                + "count(p.bill), "
                + "p.paymentMethod ) "
                + " from Payment p "
                + " where p.bill.retired=:ret"
                + " and p.createdAt between :fromDate and :toDate "
                + " and p.bill.billTypeAtomic in :abts ";

        if (webUser != null) {
            jpql += " and p.bill.creater=:wu";
            params.put("wu", webUser);
        }

//        if (institution == null) {
//            jpql += " and p.bill.institution=:ins";
//            params.put("ins", sessionController.getInstitution());
//        } else {
//            jpql += " and p.bill.institution=:ins";
//            params.put("ins", institution);
//        }
//
//        if (department == null) {
////            jpql += " and p.bill.department=:dept";
////            params.put("dept", sessionController.getLoggedUser().getDepartment());
//        } else {
//            jpql += " and p.bill.department=:dept";
//            params.put("dept", department);
//        }
        jpql += " group by p.paymentMethod, p.bill.billTypeAtomic "
                + " order by p.bill.billTypeAtomic";

        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        params.put("ret", false);
        params.put("abts", billTypesToFilter);
        billSummaryRows = paymentFacade.findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);

        for (BillSummaryRow bss : billSummaryRows) {
            if (bss.getPaymentMethod() == PaymentMethod.Cash) {
                cashTotal += bss.getPaidValue();
            } else if (bss.getPaymentMethod() == PaymentMethod.Card) {
                cardTotal += bss.getPaidValue();
            } else if (bss.getPaymentMethod() == PaymentMethod.Cheque) {
                chequeTotal += bss.getPaidValue();
            } else if (bss.getPaymentMethod() == PaymentMethod.Slip) {
                slipTotal += bss.getPaidValue();
            } else {

                totalOfOtherPayments += bss.getPaidValue();
            }
            totalPaying += bss.getPaidValue();
        }

    }

    public void processUserFinancialTransactionalSummaryByPaymentMethod() {
        reportTemplateType = ReportTemplateType.PAYMENT_METHOD_SUMMARY_USING_BILLS;
        if (institution == null) {
            setDepartments(null);
        }
        billSummaryRows = null;
        totalPaying = 0.0;
        cashTotal = 0.0;
        cardTotal = 0.0;
        chequeTotal = 0.0;
        slipTotal = 0.0;
        totalOfOtherPayments = 0.0;

        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "select new com.divudi.core.light.common.BillSummaryRow("
                + "p.paymentMethod, "
                + "sum(p.paidValue), "
                + "count(p.bill)) "
                + "from Payment p "
                + "where p.bill.retired = :ret "
                + "and p.createdAt between :fromDate and :toDate ";

        if (webUser != null) {
            jpql += " and p.bill.creater = :wu";
            params.put("wu", webUser);
        }

        jpql += " group by p.paymentMethod "
                + "order by p.paymentMethod";

        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        params.put("ret", false);

        List<?> result = paymentFacade.findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);

        billSummaryRows = new ArrayList<>();
        for (Object obj : result) {
            if (obj instanceof BillSummaryRow) {
                billSummaryRows.add((BillSummaryRow) obj);
            } else {
            }
        }

        for (BillSummaryRow bss : billSummaryRows) {
            if (bss.getPaymentMethod() == PaymentMethod.Cash) {
                cashTotal += bss.getPaidValue();
            } else if (bss.getPaymentMethod() == PaymentMethod.Card) {
                cardTotal += bss.getPaidValue();
            } else if (bss.getPaymentMethod() == PaymentMethod.Cheque) {
                chequeTotal += bss.getPaidValue();
            } else if (bss.getPaymentMethod() == PaymentMethod.Slip) {
                slipTotal += bss.getPaidValue();
            } else {
                totalOfOtherPayments += bss.getPaidValue();
            }
            totalPaying += bss.getPaidValue();
        }
    }

    private List<Department> departments;

    public void fillInstitutionInDepartment(Institution ins) {
        if (ins == null) {
            setDepartments(null);
        } else {
            setDepartments(getDepartmentController().getInstitutionDepatrments(ins));
        }
    }

    public void processAllFinancialTransactionalSummarybyPaymentMethod() {
        if (institution == null) {
            setDepartments(null);
        }
        billSummaryRows = null;
        totalPaying = 0.0;
        cashTotal = 0.0;
        cardTotal = 0.0;
        chequeTotal = 0.0;
        slipTotal = 0.0;
        totalOfOtherPayments = 0.0;

        String jpql;
        Map params = new HashMap();
        List<BillTypeAtomic> billTypesToFilter = new ArrayList<>();
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_IN));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_OUT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT_REVERSE));

        jpql = "select new com.divudi.core.light.common.BillSummaryRow("
                + "p.bill.billTypeAtomic, "
                + "sum(p.paidValue), "
                + "count(p.bill), "
                + "p.paymentMethod ) "
                + " from Payment p "
                + " where p.bill.retired=:ret"
                + " and p.createdAt between :fromDate and :toDate "
                + " and p.bill.billTypeAtomic in :abts ";

        if (institution == null) {
            jpql += " and p.bill.institution=:ins";
            params.put("ins", sessionController.getInstitution());
        } else {
            jpql += " and p.bill.institution=:ins";
            params.put("ins", institution);
        }

        if (department == null) {
//            jpql += " and p.bill.department=:dept";
//            params.put("dept", sessionController.getLoggedUser().getDepartment());
        } else {
            jpql += " and p.bill.department=:dept";
            params.put("dept", department);
        }

        jpql += " group by p.paymentMethod, p.bill.billTypeAtomic "
                + " order by p.bill.billTypeAtomic";

        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        params.put("ret", false);
        params.put("abts", billTypesToFilter);
        billSummaryRows = paymentFacade.findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);

        for (BillSummaryRow bss : billSummaryRows) {
            if (bss.getPaymentMethod() == PaymentMethod.Cash) {
                cashTotal += bss.getPaidValue();
            } else if (bss.getPaymentMethod() == PaymentMethod.Card) {
                cardTotal += bss.getPaidValue();
            } else if (bss.getPaymentMethod() == PaymentMethod.Cheque) {
                chequeTotal += bss.getPaidValue();
            } else if (bss.getPaymentMethod() == PaymentMethod.Slip) {
                slipTotal += bss.getPaidValue();
            } else {

                totalOfOtherPayments += bss.getPaidValue();
            }
            totalPaying += bss.getPaidValue();
        }

    }

    public void processAllFinancialTransactionalSummarybyUsers() {
        if (institution == null) {
            setDepartments(null);
        }
        billSummaryRows = null;
        grossTotal = 0.0;
        discount = 0.0;
        netTotal = 0.0;
        totalBillCount = 0.0;
        String jpql;
        Map params = new HashMap();
        List<BillTypeAtomic> billTypesToFilter = new ArrayList<>();
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_IN));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_OUT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT_REVERSE));

        jpql = "select new com.divudi.core.light.common.BillSummaryRow("
                + "sum(b.total), "
                + "sum(b.discount), "
                + "sum(b.netTotal), "
                + "count(b), "
                + "b.creater.webUserPerson ) "
                + " from Bill b "
                + " where b.retired=:ret"
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.billTypeAtomic in :abts ";

        if (institution != null) {
            jpql += " and b.institution=:ins";
            params.put("ins", getInstitution());

            if (department != null) {
                jpql += " and b.department=:dept";
                params.put("dept", getDepartment());
            }
        }

        jpql += " group by b.creater "
                + " order by  b.creater";

        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        params.put("ret", false);
        params.put("abts", billTypesToFilter);
        billSummaryRows = getBillFacade().findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);

        for (BillSummaryRow bss : billSummaryRows) {
            grossTotal += bss.getGrossTotal();
            discount += bss.getDiscount();
            netTotal += bss.getNetTotal();
            totalBillCount += bss.getBillCount();
        }
    }

    public void processAllFinancialTransactionalSummarybyUserPayment() {
        billSummaryRows = null;
        grossTotal = 0.0;
        discount = 0.0;
        netTotal = 0.0;
        totalBillCount = 0.0;
        String jpql;
        Map params = new HashMap();
        List<BillTypeAtomic> billTypesToFilter = new ArrayList<>();
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_IN));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_OUT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT_REVERSE));

        jpql = "select new com.divudi.core.light.common.BillSummaryRow("
                + "sum(b.total), "
                + "sum(b.discount), "
                + "sum(b.netTotal), "
                + "count(b), "
                + "b.paymentMethod,"
                + "b.creater.webUserPerson ) "
                + " from Bill b "
                + " where b.retired=:ret"
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.billTypeAtomic in :abts ";

        if (department != null) {
            jpql += " and b.department=:dept";
            params.put("dept", getDepartment());
        }

        jpql += " group by b.paymentMethod, b.creater.webUserPerson"
                + " order by b.creater.webUserPerson";

        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        params.put("ret", false);
        params.put("abts", billTypesToFilter);

        billSummaryRows = getBillFacade().findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);

        for (BillSummaryRow bss : billSummaryRows) {
            grossTotal += bss.getGrossTotal();
            discount += bss.getDiscount();
            netTotal += bss.getNetTotal();
            totalBillCount += bss.getBillCount();
        }
    }

    public void processAllFinancialTransactionalBillListBySingleUserByIds() {
        if (startBillId == null && endBillId == null) {
            JsfUtil.addErrorMessage("Enter at leat on bill number");
            return;
        }
        if (webUser == null) {
            webUser = sessionController.getLoggedUser();
        }
        billSummaryRows = null;
        grossTotal = 0.0;
        discount = 0.0;
        netTotal = 0.0;
        totalBillCount = 0.0;
        String jpql;
        Map params = new HashMap();
        List<BillTypeAtomic> billTypesToFilter = new ArrayList<>();
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_IN));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_OUT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT_REVERSE));

        jpql = "select b "
                + " from Bill b "
                + " where b.retired=:ret "
                + " and b.creater=:wu ";

        if (billTypeAtomic == null) {
            jpql += "  and b.billTypeAtomic in :abts  ";
            params.put("abts", billTypesToFilter);
        } else {
            jpql += "  and b.billTypeAtomic=:abt  ";
            params.put("abt", billTypeAtomic);
        }

        if (startBillId != null) {
            jpql += " and b.id > :sid ";
            params.put("sid", startBillId);
        }
        if (endBillId != null) {
            jpql += " and b.id < :eid ";
            params.put("eid", endBillId);
        }

        if (paymentMethod != null) {
            jpql += " and b.paymentMethod=:pm ";
            params.put("pm", paymentMethod);
        }
        if (paymentMethods != null) {
            jpql += " and b.paymentMethod in :pms ";
            params.put("pms", paymentMethods);
        }
        jpql += " order by b.paymentMethod, b.billTypeAtomic ";

        params.put("wu", webUser);
        params.put("ret", false);

        bills = getBillFacade().findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);

        for (Bill bss : bills) {
            grossTotal += bss.getTotal();
            discount += bss.getDiscount();
            netTotal += bss.getNetTotal();
            totalBillCount++;
        }
    }

    public void processAllFinancialTransactionalSummarybySingleUserByIds() {
        if (startBillId == null && endBillId == null) {
            JsfUtil.addErrorMessage("Enter at leat on bill number");
            return;
        }
        if (webUser == null) {
            webUser = sessionController.getLoggedUser();
        }
        billSummaryRows = null;
        grossTotal = 0.0;
        discount = 0.0;
        netTotal = 0.0;
        totalBillCount = 0.0;
        String jpql;
        Map params = new HashMap();
        List<BillTypeAtomic> billTypesToFilter = new ArrayList<>();
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_IN));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_OUT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT_REVERSE));

        jpql = "select new com.divudi.core.light.common.BillSummaryRow("
                + "b.billTypeAtomic, "
                + "sum(b.total), "
                + "sum(b.discount), "
                + "sum(b.netTotal), "
                + "count(b),"
                + "b.paymentMethod "
                + ") "
                + " from Bill b "
                + " where b.retired=:ret "
                + " and b.creater=:wu "
                + " and b.billTypeAtomic in :abts ";

        if (startBillId != null) {
            jpql += " and b.id > :sid ";
            params.put("sid", startBillId);
        }
        if (endBillId != null) {
            jpql += " and b.id < :eid ";
            params.put("eid", endBillId);
        }

        if (paymentMethod != null) {
            jpql += " and b.paymentMethod=:pm ";
            params.put("pm", paymentMethod);
        }
        if (paymentMethods != null) {
            jpql += " and b.paymentMethod in :pms ";
            params.put("pms", paymentMethods);
        }
        jpql += " group by b.paymentMethod, b.billTypeAtomic "
                + " order by b.creater.webUserPerson";

        params.put("wu", webUser);
        params.put("ret", false);
        params.put("abts", billTypesToFilter);

        billSummaryRows = getBillFacade().findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);

        for (BillSummaryRow bss : billSummaryRows) {
            grossTotal += bss.getGrossTotal();
            discount += bss.getDiscount();
            netTotal += bss.getNetTotal();
            totalBillCount += bss.getBillCount();
        }
    }

    public void processAllFinancialTransactionalSummarybySingleUserByIdsAdmin() {
        if (startBillId == null && endBillId == null) {
            JsfUtil.addErrorMessage("Enter at leat on bill number");
            return;
        }
        if (webUser == null) {
            webUser = sessionController.getLoggedUser();
        }
        billSummaryRows = null;
        grossTotal = 0.0;
        discount = 0.0;
        netTotal = 0.0;
        totalBillCount = 0.0;
        String jpql;
        Map params = new HashMap();
        List<BillTypeAtomic> billTypesToFilter = new ArrayList<>();
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_IN));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_OUT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT_REVERSE));

        jpql = "select new com.divudi.core.light.common.BillSummaryRow("
                + "b.billTypeAtomic, "
                + "sum(b.total), "
                + "sum(b.discount), "
                + "sum(b.netTotal), "
                + "count(b),"
                + "b.paymentMethod "
                + ") "
                + " from Bill b "
                + " where b.retired=:ret "
                + " and b.creater=:wu "
                + " and b.billTypeAtomic in :abts ";

        if (startBillId != null) {
            jpql += " and b.id > :sid ";
            params.put("sid", startBillId);
        }
        if (endBillId != null) {
            jpql += " and b.id < :eid ";
            params.put("eid", endBillId);
        }

        if (paymentMethod != null) {
            jpql += " and b.paymentMethod=:pm ";
            params.put("pm", paymentMethod);
        }
        if (paymentMethods != null) {
            jpql += " and b.paymentMethod in :pms ";
            params.put("pms", paymentMethods);
        }
        jpql += " group by b.paymentMethod, b.billTypeAtomic "
                + " order by b.creater.webUserPerson";

        params.put("wu", webUser);
        params.put("ret", false);
        params.put("abts", billTypesToFilter);

        billSummaryRows = getBillFacade().findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);

        for (BillSummaryRow bss : billSummaryRows) {
            grossTotal += bss.getGrossTotal();
            discount += bss.getDiscount();
            netTotal += bss.getNetTotal();
            totalBillCount += bss.getBillCount();
        }
    }

    public void processAllFinancialTransactionalSummarybyDepartment() {
        billSummaryRows = null;
        grossTotal = 0.0;
        discount = 0.0;
        netTotal = 0.0;
        totalBillCount = 0.0;

        String jpql;
        Map params = new HashMap();
        List<BillTypeAtomic> billTypesToFilter = new ArrayList<>();
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_IN));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_OUT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT_REVERSE));

        jpql = "select new com.divudi.core.light.common.BillSummaryRow("
                + "b.billTypeAtomic, "
                + "sum(b.total), "
                + "sum(b.discount), "
                + "sum(b.netTotal),"
                + "count(b)) "
                + " from Bill b "
                + " where b.retired=:ret"
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.billTypeAtomic in :abts "
                + " and b.department=:dept"
                + " group by b.billTypeAtomic"
                + " order by b.billTypeAtomic ";
//        Bill b = new Bill();
//        b.getTotal();
//        b.getDiscount();
//        b.getNetTotal();
//        b.getBillTypeAtomic();
        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        params.put("ret", false);
        params.put("abts", billTypesToFilter);
        params.put("dept", getDepartment());
        billSummaryRows = getBillFacade().findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);

        for (BillSummaryRow bss : billSummaryRows) {
            grossTotal += bss.getGrossTotal();
            discount += bss.getDiscount();
            netTotal += bss.getNetTotal();
            totalBillCount += bss.getBillCount();
        }

    }

    public void listBills() {
        bills = null;
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select b from Bill b where 1=1 ");
        if (toDate != null && fromDate != null) {
            jpql.append(" and b.createdAt between :fromDate and :toDate ");
            params.put("toDate", toDate);
            params.put("fromDate", fromDate);
        }

        if (institution != null) {
            params.put("ins", institution);
            jpql.append(" and b.department.institution = :ins ");
        }

        if (department != null) {
            params.put("dept", department);
            jpql.append(" and b.department = :dept ");
        }

        if (site != null) {
            params.put("site", site);
            jpql.append(" and b.department.site = :site ");
        }

        if (webUser != null) {
            jpql.append(" and b.creater=:wu ");
            params.put("wu", webUser);
        }

        if (billClassType != null) {
            jpql.append(" and type(b)=:billClassType ");
            switch (billClassType) {
                case Bill:
                    params.put("billClassType", com.divudi.core.entity.Bill.class);
                    break;
                case BilledBill:
                    params.put("billClassType", com.divudi.core.entity.BilledBill.class);
                    break;
                case CancelledBill:
                    params.put("billClassType", com.divudi.core.entity.CancelledBill.class);
                    break;
                case OtherBill:
                    params.put("billClassType", com.divudi.core.entity.Bill.class);
                    break;
                case PreBill:
                    params.put("billClassType", com.divudi.core.entity.PreBill.class);
                    break;
                case RefundBill:
                    params.put("billClassType", com.divudi.core.entity.RefundBill.class);
                    break;

            }
        }

        if (billType != null) {
            jpql.append(" and b.billType=:billType ");
            params.put("billType", billType);
        }

        if (billTypeAtomic != null) {
            jpql.append(" and b.billTypeAtomic=:billTypeAtomic ");
            params.put("billTypeAtomic", billTypeAtomic);
        }

        // Order by bill ID
        jpql.append(" order by b.id ");

        // Execute the query
        bills = getBillFacade().findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);

        total = 0.0;
        netTotal = 0.0;
        discount = 0.0;
        if (bills != null) {
            for (Bill bill : bills) {
                if (bill != null) {
                    total += bill.getTotal();
                    netTotal += bill.getNetTotal();
                    discount += bill.getDiscount();
                }
            }
        }

    }

    public void listCancellationBills() {
        bills = null;
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select b from Bill b where 1=1 ");
        if (toDate != null && fromDate != null) {
            jpql.append(" and b.createdAt between :fromDate and :toDate ");
            params.put("toDate", toDate);
            params.put("fromDate", fromDate);
        }

        if (institution != null) {
            params.put("ins", institution);
            jpql.append(" and b.department.institution = :ins ");
        }

        if (department != null) {
            params.put("dept", department);
            jpql.append(" and b.department = :dept ");
        }

        if (site != null) {
            params.put("site", site);
            jpql.append(" and b.department.site = :site ");
        }

        if (webUser != null) {
            jpql.append(" and b.creater=:wu ");
            params.put("wu", webUser);
        }

        jpql.append(" and type(b)=:billClassType ");
        params.put("billClassType", com.divudi.core.entity.CancelledBill.class);

        if (billType != null) {
            jpql.append(" and b.billType=:billType ");
            params.put("billType", billType);
        }

        if (billTypeAtomic != null) {
            jpql.append(" and b.billTypeAtomic=:billTypeAtomic ");
            params.put("billTypeAtomic", billTypeAtomic);
        }

        // Order by bill ID
        jpql.append(" order by b.id ");

        // Execute the query
        bills = getBillFacade().findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);

        total = 0.0;
        netTotal = 0.0;
        discount = 0.0;
        if (bills != null) {
            for (Bill bill : bills) {
                if (bill != null) {
                    total += bill.getTotal();
                    netTotal += bill.getNetTotal();
                    discount += bill.getDiscount();
                }
            }
        }

    }

    public void listReturnBills() {
        bills = null;
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select b from Bill b where 1=1 ");
        if (toDate != null && fromDate != null) {
            jpql.append(" and b.createdAt between :fromDate and :toDate ");
            params.put("toDate", toDate);
            params.put("fromDate", fromDate);
        }

        if (institution != null) {
            params.put("ins", institution);
            jpql.append(" and b.department.institution = :ins ");
        }

        if (department != null) {
            params.put("dept", department);
            jpql.append(" and b.department = :dept ");
        }

        if (site != null) {
            params.put("site", site);
            jpql.append(" and b.department.site = :site ");
        }

        if (webUser != null) {
            jpql.append(" and b.creater=:wu ");
            params.put("wu", webUser);
        }

        jpql.append(" and type(b)=:billClassType ");
        params.put("billClassType", com.divudi.core.entity.RefundBill.class);

        if (billType != null) {
            jpql.append(" and b.billType=:billType ");
            params.put("billType", billType);
        }

        if (billTypeAtomic != null) {
            jpql.append(" and b.billTypeAtomic=:billTypeAtomic ");
            params.put("billTypeAtomic", billTypeAtomic);
        }

        // Order by bill ID
        jpql.append(" order by b.id ");

        // Execute the query
        bills = getBillFacade().findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);

        total = 0.0;
        netTotal = 0.0;
        discount = 0.0;
        if (bills != null) {
            for (Bill bill : bills) {
                if (bill != null) {
                    total += bill.getTotal();
                    netTotal += bill.getNetTotal();
                    discount += bill.getDiscount();
                }
            }
        }

    }

    public void listPharmaceuticalBillItems() {
        pharmaceuticalBillItems = null;
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select pbi from PharmaceuticalBillItem pbi "
                + " join pbi.billItem bi join bi.bill b "
                + " where 1=1 "
                + " ");
        if (toDate != null && fromDate != null) {
            jpql.append(" and b.createdAt between :fromDate and :toDate ");
            params.put("toDate", toDate);
            params.put("fromDate", fromDate);
        }

        if (institution != null) {
            params.put("ins", institution);
            jpql.append(" and b.department.institution = :ins ");
        }

        if (department != null) {
            params.put("dept", department);
            jpql.append(" and b.department = :dept ");
        }

        if (site != null) {
            params.put("site", site);
            jpql.append(" and b.department.site = :site ");
        }

        if (webUser != null) {
            jpql.append(" and b.creater=:wu ");
            params.put("wu", webUser);
        }

        if (billClassType != null) {
            jpql.append(" and type(b)=:billClassType ");
            switch (billClassType) {
                case Bill:
                    params.put("billClassType", com.divudi.core.entity.Bill.class);
                    break;
                case BilledBill:
                    params.put("billClassType", com.divudi.core.entity.BilledBill.class);
                    break;
                case CancelledBill:
                    params.put("billClassType", com.divudi.core.entity.CancelledBill.class);
                    break;
                case OtherBill:
                    params.put("billClassType", com.divudi.core.entity.Bill.class);
                    break;
                case PreBill:
                    params.put("billClassType", com.divudi.core.entity.PreBill.class);
                    break;
                case RefundBill:
                    params.put("billClassType", com.divudi.core.entity.RefundBill.class);
                    break;

            }
        }

        if (billType != null) {
            jpql.append(" and b.billType=:billType ");
            params.put("billType", billType);
        }

        if (billTypeAtomic != null) {
            jpql.append(" and b.billTypeAtomic=:billTypeAtomic ");
            params.put("billTypeAtomic", billTypeAtomic);
        }

        // Order by bill ID
        jpql.append(" order by pbi.id ");

        pharmaceuticalBillItems = pharmaceuticalBillItemFacade.findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);
    }

    public void listPatientInvestigations() {
        patientInvestigations = null;
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select pi from PatientInvestigation pi "
                + " join pi.billItem bi join bi.bill b "
                + " where 1=1 "
                + " ");
        if (toDate != null && fromDate != null) {
            jpql.append(" and b.createdAt between :fromDate and :toDate ");
            params.put("toDate", toDate);
            params.put("fromDate", fromDate);
        }

        if (institution != null) {
            params.put("ins", institution);
            jpql.append(" and b.department.institution = :ins ");
        }

        if (department != null) {
            params.put("dept", department);
            jpql.append(" and b.department = :dept ");
        }

        if (site != null) {
            params.put("site", site);
            jpql.append(" and b.department.site = :site ");
        }

        if (webUser != null) {
            jpql.append(" and b.creater=:wu ");
            params.put("wu", webUser);
        }

        if (billClassType != null) {
            jpql.append(" and type(b)=:billClassType ");
            switch (billClassType) {
                case Bill:
                    params.put("billClassType", com.divudi.core.entity.Bill.class);
                    break;
                case BilledBill:
                    params.put("billClassType", com.divudi.core.entity.BilledBill.class);
                    break;
                case CancelledBill:
                    params.put("billClassType", com.divudi.core.entity.CancelledBill.class);
                    break;
                case OtherBill:
                    params.put("billClassType", com.divudi.core.entity.Bill.class);
                    break;
                case PreBill:
                    params.put("billClassType", com.divudi.core.entity.PreBill.class);
                    break;
                case RefundBill:
                    params.put("billClassType", com.divudi.core.entity.RefundBill.class);
                    break;
            }
        }

        if (billType != null) {
            jpql.append(" and b.billType=:billType ");
            params.put("billType", billType);
        }

        if (billTypeAtomic != null) {
            jpql.append(" and b.billTypeAtomic=:billTypeAtomic ");
            params.put("billTypeAtomic", billTypeAtomic);
        }

        jpql.append(" order by pi.id ");

        patientInvestigations = patientInvestigationFacade.findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);
    }

    public void listBillsOpdCreditCompanySettle() {
        bills = null;
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select b from Bill b where 1=1 ");
        if (toDate != null && fromDate != null) {
            jpql.append(" and b.createdAt between :fromDate and :toDate ");
            params.put("toDate", toDate);
            params.put("fromDate", fromDate);
        }

        if (institution != null) {
            params.put("ins", institution);
            jpql.append(" and b.department.institution = :ins ");
        }

        if (department != null) {
            params.put("dep", department);
            jpql.append(" and b.department = :dept ");
        }

        if (site != null) {
            params.put("site", site);
            jpql.append(" and b.department.site = :site ");
        }

        if (webUser != null) {
            jpql.append(" and b.creater=:wu ");
            params.put("wu", webUser);
        }

        List<BillTypeAtomic> btas = new ArrayList<>();
        btas.add(BillTypeAtomic.OPD_CREDIT_COMPANY_PAYMENT_RECEIVED);
        btas.add(BillTypeAtomic.OPD_CREDIT_COMPANY_PAYMENT_CANCELLATION);

        jpql.append(" and b.billTypeAtomic in :btas ");
        params.put("btas", btas);

        // Order by bill ID
        jpql.append(" order by b.id ");

        // Execute the query
        bills = getBillFacade().findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);

        if (bills != null) {
            for (Bill bill : bills) {
                if (bill != null) {
                    total += bill.getTotal();
                    netTotal += bill.getNetTotal();
                    discount += bill.getDiscount();
                }
            }
        }

    }

    public void listBillTypes() {
        bundle = new ReportTemplateRowBundle();
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select new com.divudi.core.data.ReportTemplateRow("
                + "b.billType, b.billClassType, b.billTypeAtomic, count(b), sum(b.total), sum(b.discount), sum(b.netTotal))"
                + " from Bill b where b.retired=:ret ");
        params.put("ret", false);
        if (toDate != null && fromDate != null) {
            jpql.append(" and b.createdAt between :fromDate and :toDate ");
            params.put("toDate", toDate);
            params.put("fromDate", fromDate);
        }

        if (institution != null) {
            params.put("ins", institution);
            jpql.append(" and b.department.institution = :ins ");
        }

        if (department != null) {
            params.put("dept", department);
            jpql.append(" and b.department = :dept ");
        }

        if (site != null) {
            params.put("site", site);
            jpql.append(" and b.department.site = :site ");
        }

        if (webUser != null) {
            jpql.append(" and b.creater=:wu ");
            params.put("wu", webUser);
        }

        jpql.append(" group by b.billType, b.billClassType, b.billTypeAtomic ");

        List<ReportTemplateRow> rows = (List<ReportTemplateRow>) getBillFacade().findLightsByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);

        bundle.setReportTemplateRows(rows);
        bundle.calculateTotalByValues();

    }

    public String navigateToListBillsWithErrors() {
        bills = null;
        return "/dataAdmin/bills_with_errors?faces-redirect=true";
    }

    public void findAllBillsWithErrors() {
        bills = new ArrayList<>();
        findBillsWithErrors();
        findBillsFromAgentHistoriesWithBalanceErrors();
    }

    public void findBillsWithErrors() {
        List<Bill> allBills;
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select b from Bill b where 1=1 ");
        if (toDate != null && fromDate != null) {
            jpql.append(" and b.createdAt between :fromDate and :toDate ");
            params.put("toDate", toDate);
            params.put("fromDate", fromDate);
        }

        if (institution != null) {
            params.put("ins", institution);
            jpql.append(" and b.department.institution = :ins ");
        }

        if (department != null) {
            params.put("dept", department);
            jpql.append(" and b.department = :dept ");
        }

        if (site != null) {
            params.put("site", site);
            jpql.append(" and b.department.site = :site ");
        }

        if (webUser != null) {
            jpql.append(" and b.creater=:wu ");
            params.put("wu", webUser);
        }

        if (billClassType != null) {
            jpql.append(" and type(b)=:billClassType ");
            switch (billClassType) {
                case Bill:
                    params.put("billClassType", com.divudi.core.entity.Bill.class);
                    break;
                case BilledBill:
                    params.put("billClassType", com.divudi.core.entity.BilledBill.class);
                    break;
                case CancelledBill:
                    params.put("billClassType", com.divudi.core.entity.CancelledBill.class);
                    break;
                case OtherBill:
                    params.put("billClassType", com.divudi.core.entity.Bill.class);
                    break;
                case PreBill:
                    params.put("billClassType", com.divudi.core.entity.PreBill.class);
                    break;
                case RefundBill:
                    params.put("billClassType", com.divudi.core.entity.RefundBill.class);
                    break;

            }
        }

        if (billType != null) {
            jpql.append(" and b.billType=:billType ");
            params.put("billType", billType);
        }

        if (billTypeAtomic != null) {
            jpql.append(" and b.billTypeAtomic=:billTypeAtomic ");
            params.put("billTypeAtomic", billTypeAtomic);
        }

        // Order by bill ID
        jpql.append(" order by b.id ");

        // Execute the query
        allBills = getBillFacade().findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);

        if (allBills != null) {
            for (Bill tmpBill : allBills) {
                boolean billHasErrors;
                billService.reloadBill(tmpBill);
                billHasErrors = billService.checkBillForErrors(tmpBill);
                if (!billHasErrors) {
                    continue;
                }
                bills.add(tmpBill);
                total += tmpBill.getTotal();
                netTotal += tmpBill.getNetTotal();
                discount += tmpBill.getDiscount();
            }
        }

    }

    public void findBillsFromAgentHistoriesWithBalanceErrors() {
        List<AgentHistory> allHistories;
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select ah from AgentHistory ah where ah.retired = false");

        if (fromDate != null && toDate != null) {
            jpql.append(" and ah.createdAt between :fromDate and :toDate ");
            params.put("fromDate", fromDate);
            params.put("toDate", toDate);
        }

        if (institution != null) {
            jpql.append(" and ah.bill.institution = :ins ");
            params.put("ins", institution);
        }

//        if (collectingCentre != null) {
//            jpql.append(" and ah.agency = :cc ");
//            params.put("cc", collectingCentre);
//        }
        jpql.append(" order by ah.createdAt ");

        allHistories = agentHistoryFacade.findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);

        if (allHistories != null) {
            for (AgentHistory ah : allHistories) {
                double expectedBalanceAfter = CommonFunctions.roundToTwoDecimalsBigDecimal(
                        ah.getBalanceBeforeTransaction() + ah.getTransactionValue()
                );
                double actualBalanceAfter = ah.getBalanceAfterTransaction();

                double diff = Math.abs(expectedBalanceAfter - actualBalanceAfter);

                if (diff > 1.0) { // Significant error found
                    Bill b = ah.getBill();
                    if (b != null && !bills.contains(b)) { // Avoid duplicates
                        String msg = "Agent history balance mismatch.";
                        b.setTmpComments((b.getTmpComments() == null ? "" : b.getTmpComments()) + msg + " ");
                        bills.add(b);
                    }
                }
            }
        }
    }

    public void listBillItems() {
        billItems = null;
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select bi from BillItem bi where 1=1 ");
        if (toDate != null && fromDate != null) {
            jpql.append(" and bi.bill.createdAt between :fromDate and :toDate ");
            params.put("toDate", toDate);
            params.put("fromDate", fromDate);
        }

        if (institution != null) {
            params.put("ins", institution);
            jpql.append(" and bi.bill.department.institution = :ins ");
        }

        if (department != null) {
            params.put("dep", department);
            jpql.append(" and bi.bill.department = :dept ");
        }

        if (site != null) {
            params.put("site", site);
            jpql.append(" and bi.bill.department.site = :site ");
        }

        if (webUser != null) {
            jpql.append(" and bi.bill.creater=:wu ");
            params.put("wu", webUser);
        }

        // Order by bill ID
        jpql.append(" order by bi.id ");

        billItems = getBillItemFacade().findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);

    }

    public void listBillFees() {
        billFees = null;
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select bf from BillFee bf join bf.bill b where 1=1 ");
        if (toDate != null && fromDate != null) {
            jpql.append(" and b.createdAt between :fromDate and :toDate ");
            params.put("toDate", toDate);
            params.put("fromDate", fromDate);
        }

        if (institution != null) {
            params.put("ins", institution);
            jpql.append(" and b.department.institution = :ins ");
        }

        if (department != null) {
            params.put("dep", department);
            jpql.append(" and b.department = :dept ");
        }

        if (site != null) {
            params.put("site", site);
            jpql.append(" and b.department.site = :site ");
        }

        if (webUser != null) {
            jpql.append(" and b.creater=:wu ");
            params.put("wu", webUser);
        }

        jpql.append(" order by b.id ");

        billFees = getBillFeeFacade().findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);

    }

    public void listPayments() {
        payments = null;
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select p from Payment p where p.retired=:ret");
        params.put("ret", false);
        if (toDate != null && fromDate != null) {
            jpql.append(" and p.createdAt between :fromDate and :toDate ");
            params.put("toDate", toDate);
            params.put("fromDate", fromDate);
        }

        if (institution != null) {
            params.put("ins", institution);
            jpql.append(" and p.department.institution = :ins ");
        }

        if (department != null) {
            params.put("dep", department);
            jpql.append(" and p.department = :dept ");
        }

        jpql.append(" order by p.id ");

        payments = paymentFacade.findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);

    }

    public String fillAllBills(Date fromDate, Date toDate, Institution institution, Department department, PaymentMethod paymentMethod, BillTypeAtomic billtypeAtomic) {
        bills = null;
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("select b from Bill b where 1=1 ");

        // Handle date filtering
        if (toDate != null && fromDate != null) {
            jpql.append(" and b.createdAt between :fromDate and :toDate ");
            params.put("toDate", toDate);
            params.put("fromDate", fromDate);
        }

        // Include BillTypeAtomic filtering
        List<BillTypeAtomic> billTypesToFilter = new ArrayList<>();
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_IN));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_OUT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT));
        billTypesToFilter.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CREDIT_SETTLEMENT_REVERSE));
        if (!billTypesToFilter.isEmpty()) {
            jpql.append(" and b.billTypeAtomic in :abts ");
            params.put("abts", billTypesToFilter);
        }

        // Institution and department filtering
        if (institution != null) {
            jpql.append(" and b.institution = :ins ");
            params.put("ins", institution);
            if (department != null) {
                jpql.append(" and b.department = :dept ");
                params.put("dept", department);
            }
        }

        if (paymentMethod != null) {
            jpql.append(" and b.paymentMethod=:pm ");
            params.put("pm", paymentMethod);
        }

        if (billtypeAtomic != null) {
            jpql.append(" and b.billTypeAtomic=:ba ");
            params.put("ba", billtypeAtomic);
        }

        if (webUser != null) {
            jpql.append(" and b.creater=:wu ");
            params.put("wu", webUser);
        }

        // Order by bill ID
        jpql.append(" order by b.id ");

        bills = getBillFacade().findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);
        return "/analytics/all_bills";
    }

    public void fillAllBills() {
        bills = null;
        String jpql;
        Map params = new HashMap();
        jpql = "select b from "
                + " Bill b "
                + " where b.createdAt between :fromDate and :toDate "
                + " order by b.id ";
        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    public void fillOtherInstitutionBills() {
        bills = null;
        if (otherInstitution == null) {
            JsfUtil.addErrorMessage("Select other Institution");
            return;
        }
        String jpql;
        Map m = new HashMap();

        jpql = "select b from "
                + " Bill b "
                + " where b.createdAt between :fromDate and :toDate ";
        jpql += " and b.institution in :ins ";
        m.put("ins", sessionController.getLoggableInstitutions());

        jpql += " and (b.fromInstitution=:oi or b.toInstitution=:oi or b.bank=:oi or b.referenceInstitution=:oi or b.creditCompany=:oi and b.collectingCentre=:oi or b.paymentSchemeInstitution=:oi or b.referredByInstitution=:oi ) ";
        m.put("oi", otherInstitution);

        jpql += " order by b.id ";
        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());
        bills = getBillFacade().findByJpql(jpql, m, TemporalType.TIMESTAMP);
    }

    public String getInstitutionType(Bill bill) {
        if (bill == null || otherInstitution == null) {
            return "";
        }
        if (bill.getFromInstitution() != null && bill.getFromInstitution().equals(otherInstitution)) {
            return "From Institution";
        } else if (bill.getToInstitution() != null && bill.getToInstitution().equals(otherInstitution)) {
            return "To Institution";
        } else if (bill.getBank() != null && bill.getBank().equals(otherInstitution)) {
            return "Bank";
        } else if (bill.getReferenceInstitution() != null && bill.getReferenceInstitution().equals(otherInstitution)) {
            return "Reference Institution";
        } else if (bill.getCreditCompany() != null && bill.getCreditCompany().equals(otherInstitution)) {
            return "Credit Company";
        } else if (bill.getCollectingCentre() != null && bill.getCollectingCentre().equals(otherInstitution)) {
            return "Collecting Centre";
        } else if (bill.getPaymentSchemeInstitution() != null && bill.getPaymentSchemeInstitution().equals(otherInstitution)) {
            return "Payment Scheme Institution";
        } else if (bill.getReferredByInstitution() != null && bill.getReferredByInstitution().equals(otherInstitution)) {
            return "Referred By Institution";
        }
        return "";
    }

    public String viewOPD(Bill b) {
        if (b.getBillType() == BillType.OpdBill) {
            return "/opd/bill_reprint?faces-redirect=true";
        } else {
            JsfUtil.addErrorMessage("Please Search Again and View Bill");
            bills = new ArrayList<>();
            return "";
        }
    }

    public String viewOPDBillById(Long billId) {
        if (billId == null) {
            return null;
        }
        Bill tb = getBillFacade().find(billId);
        if (tb == null) {
            return null;
        }
        bill = tb;
        return "/opd/bill_reprint?faces-redirect=true";
    }

    public void createTableCashIn() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false"
                + " and b.creater=:w ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPersonName() != null && !getSearchKeyword().getPersonName().trim().equals("")) {
            sql += " and  ((b.fromWebUser.webUserPerson.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPersonName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", BillType.CashIn);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("w", getSessionController().getLoggedUser());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createTableCashOut() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false"
                + " and b.creater=:w ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPersonName() != null && !getSearchKeyword().getPersonName().trim().equals("")) {
            sql += " and  ((b.toWebUser.webUserPerson.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPersonName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", BillType.CashOut);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("w", getSessionController().getLoggedUser());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createSearchBill() {
        // Check if getSearchKeyword() itself is null to prevent NPE
        if (getSearchKeyword() == null) {
            JsfUtil.addErrorMessage("Search keyword is null");
            return;
        }

        // Check if all necessary fields in getSearchKeyword() are null
        if ((getSearchKeyword().getInsId() == null || getSearchKeyword().getInsId().isEmpty())
                && (getSearchKeyword().getDeptId() == null || getSearchKeyword().getDeptId().isEmpty())
                && (getSearchKeyword().getBhtNo() == null || getSearchKeyword().getBhtNo().trim().isEmpty())
                && getSearchKeyword().getRefBillNo() == null) {
            JsfUtil.addErrorMessage("Enter BHT No or Bill No");
            return;
        }

        bills = null;
        String sql;
        Map<String, Object> m = new HashMap<>();

        sql = "select b from Bill b where b.id is not null";

        // Check and handle InsId
        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().isEmpty()) {
            sql += " and (b.insId=:insId or b.deptId=:insId)  ";
            m.put("insId", getSearchKeyword().getInsId());
        }

        // Check and handle DeptId
        if (getSearchKeyword().getDeptId() != null && !getSearchKeyword().getDeptId().isEmpty()) {
            sql += " and (b.insId=:deptId or b.deptId=:deptId)  ";
            m.put("deptId", getSearchKeyword().getDeptId());
        }

        // Check and handle BhtNo
        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().isEmpty()) {
            sql += " and b.patientEncounter.bhtNo=:bht";
            m.put("bht", getSearchKeyword().getBhtNo().trim());
        }

        // Check and handle RefBillNo
        if (getSearchKeyword().getRefBillNo() != null) {
            try {
                long l = Long.parseLong(getSearchKeyword().getRefBillNo());
                sql += " and b.id=:id";
                m.put("id", l);
            } catch (NumberFormatException e) {
                // Handle the case where RefBillNo is not a valid number
                JsfUtil.addErrorMessage("Invalid Bill No");
            }
        }

        sql += " order by b.deptId";

        // Fetch bills using the query
        bills = getBillFacade().findByJpql(sql, m, 5000);
    }

    public void searchByInsId() {
        if (getSearchKeyword() == null) {
            JsfUtil.addErrorMessage("Search keyword is null");
            return;
        }

        if (getSearchKeyword().getInsId() == null) {
            JsfUtil.addErrorMessage("Enter Ins ID");
            return;
        }
        bills = null;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "select b from Bill b where b.id is not null";

        jpql += " and b.insId=:insId ";
        params.put("insId", getSearchKeyword().getInsId());

        jpql += " order by b.insId";

        bills = getBillFacade().findByJpql(jpql, params, 500);
    }

    public void searchByDeptId() {
        if (getSearchKeyword() == null) {
            JsfUtil.addErrorMessage("Search keyword is null");
            return;
        }
        if (getSearchKeyword().getDeptId() == null) {
            JsfUtil.addErrorMessage("Enter Ins ID");
            return;
        }
        bills = null;
        String jpql;
        Map<String, Object> params = new HashMap<>();
        jpql = "select b from Bill b where b.id is not null";
        jpql += " and b.deptId=:deptId ";
        params.put("deptId", getSearchKeyword().getDeptId());

        jpql += " order by b.deptId";

        bills = getBillFacade().findByJpql(jpql, params, 500);
    }

    public void searchById() {
        if (getSearchKeyword() == null) {
            JsfUtil.addErrorMessage("Search keyword is null");
            return;
        }
        if (getSearchKeyword().getId() == null) {
            JsfUtil.addErrorMessage("Enter Ins ID");
            return;
        }
        bills = null;
        String jpql;
        Map<String, Object> params = new HashMap<>();
        jpql = "select b from Bill b where b.id is not null";
        jpql += " and b.id=:id ";
        params.put("id", getSearchKeyword().getId());
        jpql += " order by b.id";
        bills = getBillFacade().findByJpql(jpql, params, 500);
    }

    public void searchByBhtNumber() {
        if (getSearchKeyword() == null) {
            JsfUtil.addErrorMessage("Search keyword is null");
            return;
        }
        if (getSearchKeyword().getBhtNo() == null) {
            JsfUtil.addErrorMessage("Enter Ins ID");
            return;
        }
        bills = null;
        String jpql;
        Map<String, Object> params = new HashMap<>();
        jpql = "select b from Bill b where b.id is not null";
        jpql += " and b.patientEncounter.bhtNo=:bhtNo ";
        params.put("bhtNo", getSearchKeyword().getBhtNo());
        jpql += " order by b.id";
        bills = getBillFacade().findByJpql(jpql, params, 500);
    }

    public void createSearchAll() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getInstitution() != null && !getSearchKeyword().getInstitution().trim().equals("")) {
            sql += " and  ((b.institution.name) like :ins )";
            temMap.put("ins", "%" + getSearchKeyword().getInstitution().trim().toUpperCase() + "%");
        }
        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  ((b.toInstitution.name) like :toIns )";
            temMap.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }
        if (getSearchKeyword().getToDepartment() != null && !getSearchKeyword().getToDepartment().trim().equals("")) {
            sql += " and  ((b.toDepartment.name) like :toDept )";
            temMap.put("toDept", "%" + getSearchKeyword().getToDepartment().trim().toUpperCase() + "%");
        }
        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  ((b.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }
        if (getSearchKeyword().getFromDepartment() != null && !getSearchKeyword().getFromDepartment().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getFromDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPaymentScheme() != null && !getSearchKeyword().getPaymentScheme().trim().equals("")) {
            sql += " and  ((b.paymentScheme.name) like :pScheme )";
            temMap.put("pScheme", "%" + getSearchKeyword().getPaymentScheme().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPaymentmethod() != null && !getSearchKeyword().getPaymentmethod().trim().equals("")) {
            sql += " and  ((b.paymentMethod) like :pm )";
            temMap.put("pm", "%" + getSearchKeyword().getPaymentmethod().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  ((b.insId) like :insId )";
            temMap.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDeptId() != null && !getSearchKeyword().getDeptId().trim().equals("")) {
            sql += " and  ((b.insId) like :deptId )";
            temMap.put("deptId", "%" + getSearchKeyword().getDeptId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createCollectingTable() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where "
                + " b.billType = :billType and "
                + " b.institution=:ins "
                + " and b.createdAt between :fromDate "
                + " and :toDate and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", BillType.LabBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createCollectingTableAll() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", BillType.LabBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createCollectingBillItemTable() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select bi from BillItem bi join bi.bill b where "
                + " b.billType = :billType and "
                + " b.institution=:ins "
                + " and b.createdAt between :fromDate "
                + " and :toDate and b.retired=false "
                + " and type(b)=:class ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((bi.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
        temMap.put("billType", BillType.LabBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap
                .put("class", BilledBill.class
                );

        billItems = getBillItemFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createCollectingBillItemTableAll() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select bi from BillItem bi join bi.bill b where "
                + " b.billType = :billType and "
                + " b.institution=:ins "
                + " and b.createdAt between :fromDate "
                + " and :toDate and b.retired=false "
                + " and type(b)=:class ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((bi.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("billType", BillType.LabBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap
                .put("class", BilledBill.class
                );

        billItems = getBillItemFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createCreditTable() {
        Date startTime = new Date();

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b "
                + " where b.billType = :billType"
                + "  and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  ((b.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBank() != null && !getSearchKeyword().getBank().trim().equals("")) {
            sql += " and  ((b.bank.name) like :bank )";
            temMap.put("bank", "%" + getSearchKeyword().getBank().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  ((b.chequeRefNo) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("billType", BillType.CashRecieveBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

        if (settledBillType != null) {
            bills = filterBills(bills, settledBillType);
        }

    }

    private List<Bill> filterBills(List<Bill> bills, String settledBillType) {
        List<Bill> filteredBills = new ArrayList<>();
        String billType;

        for (Bill b : bills) {
            if (b.getBillItems() == null || b.getBillItems().isEmpty()) {
                billType = "";
            } else if (b.getBillItems().get(0).getPatientEncounter() != null) {
                billType = "IP";
            } else {
                if (b.getBillItems().get(0).getReferenceBill() != null
                        && b.getBillItems().get(0).getReferenceBill().getBillTypeAtomic() != null) {
                    billType = checkBillTypeByBill(b.getBillItems().get(0).getReferenceBill().getBillTypeAtomic());
                } else {
                    billType = "";
                }
            }

            // Only add bills that match the status filter
            if (billType.equals(settledBillType)) {
                filteredBills.add(b);
            }
        }

        return filteredBills; // Return the filtered list of bills
    }

    public String checkBillTypeByBill(BillTypeAtomic bta) {
        switch (bta) {
            case OPD_BATCH_BILL_WITH_PAYMENT:
            case OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER:
                return "OP";
            case PACKAGE_OPD_BATCH_BILL_WITH_PAYMENT:
            case PACKAGE_OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER:
                return "PACKAGE";
            case INWARD_FINAL_BILL_PAYMENT_BY_CREDIT_COMPANY:
                return "IP";
            default:
                return "";
        }
    }

    public void createCreditTableBillItemAll() {
        Date startTime = new Date();

        createCreditTableBillItem(null, true);

    }

    public void createCreditTableBillItemOpd() {
        Date startTime = new Date();
        createCreditTableBillItem(BillType.OpdBill, false);

    }

    public void createCreditTableBillItemBht() {
        Date startTime = new Date();
        createCreditTableBillItem(null, false);

    }

    public void createCreditTableBillItem(BillType billType, boolean all) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillItem b "
                + " where b.bill.billType = :billType"
                + "  and b.bill.institution=:ins "
                + " and b.bill.createdAt between :fromDate and :toDate "
                + " and b.bill.retired=false ";

        if (!all) {
            if (billType != null) {
                sql += " and b.referenceBill.billType=:refBtp";
                temMap.put("refBtp", billType);
            } else {
                sql += " and b.patientEncounter is not null ";
            }

        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }
        if (billType != null) {
            if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
                sql += " and  ((b.referenceBill.insId) like :reBillNo )";
                temMap.put("reBillNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
            }
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.bill.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  ((b.bill.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBank() != null && !getSearchKeyword().getBank().trim().equals("")) {
            sql += " and  ((b.bill.bank.name) like :bank )";
            temMap.put("bank", "%" + getSearchKeyword().getBank().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  ((b.bill.chequeRefNo) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        sql += " order by b.bill.createdAt desc  ";
//
        temMap.put("billType", BillType.CashRecieveBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        billItems = getBillItemFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public List<Bill> getChannelPaymentBillsOld() {
        if (bills == null) {
            String sql;
            Map temMap = new HashMap();
            if (bills == null) {
                sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.id in"
                        + "(Select bt.bill.id From BillItem bt Where bt.referenceBill.billType=:btp"
                        + " or bt.referenceBill.billType=:btp2) and b.billType=:type and b.createdAt "
                        + "between :fromDate and :toDate order by b.id";

                temMap.put("toDate", getToDate());
                temMap.put("fromDate", getFromDate());
                temMap.put("type", BillType.PaymentBill);
                temMap.put("btp", BillType.ChannelPaid);
                temMap.put("btp2", BillType.ChannelCredit);
                bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 100);

                if (bills == null) {
                    bills = new ArrayList<>();
                }
            }
        }
        return bills;

    }

    public void channelPaymentBills() {
        Date startTime = new Date();

        String sql;
        Map m = new HashMap();

        BillType[] bt = {
            BillType.ChannelOnCall,
            BillType.ChannelCash,
            BillType.ChannelAgent,
            BillType.ChannelStaff,
            BillType.ChannelCredit,};

        List<BillType> bts = Arrays.asList(bt);

        sql = "SELECT bi FROM BillItem bi WHERE bi.retired = false "
                + " and bi.bill.billType=:bt "
                + " and type(bi.bill)=:class "
                + " and bi.paidForBillFee.bill.billType in :bts "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((bi.paidForBillFee.bill.patientEncounter.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((bi.paidForBillFee.bill.insId) like :billNo or (bi.paidForBillFee.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  ((bi.bill.insId) like :insId )";
            m.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((bi.paidForBillFee.staff.speciality.name) like :special )";
            m.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((bi.paidForBillFee.staff.person.name) like :staff )";
            m.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((bi.paidForBillFee.feeValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by bi.bill.createdAt desc  ";

        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("bt", BillType.PaymentBill);
        m.put("bts", bts);
        m
                .put("class", BilledBill.class
                );

        if (getReportKeyWord().isAdditionalDetails()) {
            billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);
        } else {
            billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 50);
        }

    }

    public void channelPaymentBillsNew() {
        Date startTime = new Date();

        String sql;
        Map m = new HashMap();

        BillType[] bt = {
            BillType.ChannelOnCall,
            BillType.ChannelCash,
            BillType.ChannelAgent,
            BillType.ChannelStaff,
            BillType.ChannelPaid,
            BillType.ChannelCredit,};

        List<BillType> bts = Arrays.asList(bt);

        sql = "SELECT bi FROM BillItem bi WHERE bi.retired = false "
                + " and bi.bill.billType=:bt "
                + " and type(bi.bill)=:class "
                + " and bi.paidForBillFee.bill.billType in :bts "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((bi.paidForBillFee.bill.patientEncounter.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((bi.paidForBillFee.bill.insId) like :billNo or (bi.paidForBillFee.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  ((bi.bill.insId) like :insId )";
            m.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((bi.paidForBillFee.staff.speciality.name) like :special )";
            m.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((bi.paidForBillFee.staff.person.name) like :staff )";
            m.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((bi.paidForBillFee.feeValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by bi.bill.createdAt desc  ";

        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("bt", BillType.ChannelProPayment);
        m.put("bts", bts);
        m
                .put("class", BilledBill.class
                );
        if (getReportKeyWord().isAdditionalDetails()) {
            billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);
        } else {
            billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP, 50);
        }

    }

    public void channelAgentPaymentBills() {
        Date startTime = new Date();

        String sql;
        Map m = new HashMap();

        sql = "SELECT bi FROM BillItem bi WHERE bi.retired = false "
                + " and bi.bill.billType=:bt"
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getIns() != null) {
            sql += " and bi.bill.toInstitution=:ins";
            m.put("ins", getSearchKeyword().getIns());
        }

        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("bt", BillType.ChannelAgencyPayment);
        billItems = getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public void createChannelDueBillFeeOld() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where b.retired=false and (b.bill.billType=:btp or b.bill.billType=:btp2) "
                + " and b.bill.id in(Select bs.bill.id From BillSession bs where bs.retired=false ) "
                + "and b.bill.cancelled=false and (b.feeValue - b.paidValue) > 0 and  "
                + "b.bill.createdAt between :fromDate and :toDate order by b.staff.id  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.ChannelPaid);
        temMap.put("btp2", BillType.ChannelCredit);

        billFees = getBillFeeFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createChannelDueBillFee() {
        Date startTime = new Date();

        selectedServiceSession = null;

        BillType[] billTypes = {BillType.ChannelAgent, BillType.ChannelCash, BillType.ChannelOnCall, BillType.ChannelStaff};
        List<BillType> bts = Arrays.asList(billTypes);
        String sql = " SELECT b FROM BillFee b "
                + "  where type(b.bill)=:class "
                + " and b.bill.retired=false "
                + " and b.bill.billTypeAtomic <> :bta"
                + " and b.bill.paidAmount!=0 "
                + " and b.bill.refunded=false "
                + " and b.fee.feeType=:ftp"
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 "
                + " and b.bill.billType in :bt ";

        HashMap hm = new HashMap();
        hm.put("bta", BillTypeAtomic.CHANNEL_BOOKING_FOR_PAYMENT_ONLINE_PENDING_PAYMENT);

        if (getFromDate() != null && getToDate() != null) {
            sql += " and b.bill.appointmentAt between :frm and  :to";
            hm.put("frm", getFromDate());
            hm.put("to", getToDate());
        }

        if (getSelectedServiceSession() != null) {
            sql += " and bs.serviceSession=:ss";
            hm.put("ss", getSelectedServiceSession());
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.bill.patient.person.name) like :patientName )";
            hm.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :billNo )";
            hm.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.feeValue) like :total )";
            hm.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  ((b.staff.speciality.name) like :special )";
            hm.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.staff.person.name) like :staff )";
            hm.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        sql += " order by b.speciality.name ";

        //hm.put("ins", sessionController.getInstitution());
        hm.put("bt", bts);
        hm.put("ftp", FeeType.Staff);
        hm.put("class", BilledBill.class);

        if (getReportKeyWord().isAdditionalDetails()) {
            billFees = billFeeFacade.findByJpql(sql, hm, TemporalType.TIMESTAMP);
        } else {
            billFees = billFeeFacade.findByJpql(sql, hm, TemporalType.TIMESTAMP, 50);
        }

    }

    public void createChannelDueBillFeeByAgent() {
        selectedServiceSession = null;

        //BillType[] billTypes = {BillType.ChannelAgent, BillType.ChannelCash, BillType.ChannelOnCall, BillType.ChannelStaff};
        // List<BillType> bts = Arrays.asList(billTypes);
        String sql = " SELECT b FROM BillFee b "
                + "  where type(b.bill)=:class "
                + " and b.bill.retired=false "
                + " and b.bill.paidAmount!=0 "
                + " and b.bill.refunded=false "
                + " and b.fee.feeType=:ftp"
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 "
                + " and b.bill.billType in :bt ";

        HashMap hm = new HashMap();
        if (getFromDate() != null && getToDate() != null) {
            sql += " and b.bill.appointmentAt between :frm and  :to";
            hm.put("frm", getFromDate());
            hm.put("to", getToDate());
        }

        if (getSelectedServiceSession() != null) {
            sql += " and bs.serviceSession=:ss";
            hm.put("ss", getSelectedServiceSession());
        }

//        if (getCurrentStaff() != null) {
//            sql += " and b.staff=:stf ";
//            hm.put("stf", getCurrentStaff());
//        }
        //hm.put("ins", sessionController.getInstitution());
        hm.put("bt", BillType.ChannelAgent);
        hm.put("ftp", FeeType.OtherInstitution);
        hm
                .put("class", BilledBill.class
                );
        billFees = billFeeFacade.findByJpql(sql, hm, TemporalType.TIMESTAMP);

    }

    public void createChannelAgencyPaymentTable() {
        Date startTime = new Date();

        createAgentPaymentTable(BillType.AgentPaymentReceiveBill);

    }

    @EJB
    private ChannelService channelService;

    public void creatAgentDirectFundPaymentTable() {

        bills = channelService.fetchAgentDirectFundBills(searchKeyword, fromDate, toDate, null);

    }

    public void createChannelAgencyCreditNoteTable() {

        createAgentPaymentTable(BillType.AgentCreditNoteBill);

    }

    public void createChannelAgencyDebitNoteTable() {

        createAgentPaymentTable(BillType.AgentDebitNoteBill);

    }

    public void createCollectingCenterCreditNoteTable() {

        createAgentPaymentTable(BillType.CollectingCentreCreditNoteBill);

    }

    public void createCollectingCenterDebitNoteTable() {

        createAgentPaymentTable(BillType.CollectingCentreDebitNoteBill);

    }

    public void createCollectingCentrePaymentTable() {
        Date startTime = new Date();
        createAgentPaymentTable(BillType.CollectingCentrePaymentReceiveBill);

    }

    public void createPatientDepositTable() {
        Date startTime = new Date();
        createPatientDepositTable(BillType.PatientPaymentReceiveBill);

    }

    public void createPatientDepositReturnTable() {
        createPatientDepositTable(BillType.PatientPaymentRefundBill);
    }

    public void createAgentPaymentTable(BillType billType) {
        bills = new ArrayList<>();
        String jpql;
        Map temMap = new HashMap();

        jpql = "select b from BilledBill b where b.billType = :billType "
                + " and b.institution=:ins and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            jpql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            jpql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            jpql += " and  ((b.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            jpql += " and  ((b.fromInstitution.institutionCode) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        jpql += " order by b.createdAt desc  ";

        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpql(jpql, temMap, TemporalType.TIMESTAMP);

    }

    public void createPatientDepositTable(BillType billType) {
        bills = new ArrayList<>();
        String jpql;
        Map temMap = new HashMap();

        jpql = "select b from Bill b where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            jpql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            jpql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            jpql += " and  ((b.patient.person.name) like :pn )";
            temMap.put("pn", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        jpql += " order by b.createdAt desc  ";

        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        bills = getBillFacade().findByJpql(jpql, temMap, TemporalType.TIMESTAMP);

    }

    public void createInwardServiceTable() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select (b.bill) from BillItem b where "
                + " b.bill.billType = :billType "
                + " and type(b.bill)=:class "
                + " and b.bill.createdAt between :fromDate and :toDate"
                + " and b.bill.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.bill.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by b.bill.deptId desc ";
        temMap.put("billType", BillType.InwardBill);
        temMap
                .put("class", BilledBill.class
                );
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createInwardServiceBills() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from Bill b where "
                + " b.billType = :billType "
                + " and type(b)=:class "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.deptId desc ";

        temMap.put("billType", BillType.InwardBill);
        temMap.put("class", BilledBill.class);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createInwardServiceTableForLab() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select (b.bill) from BillItem b where "
                + " b.bill.billType = :billType "
                + " and b.bill.createdAt between :fromDate and :toDate"
                + " and b.bill.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.bill.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by b.bill.deptId desc ";
        temMap.put("billType", BillType.InwardBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        List<Bill> billList = new ArrayList<>();

        billList = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

        Set<Bill> uniqueBills = new HashSet<>(billList);

        bills = new ArrayList<>(uniqueBills);
    }

    public void createInwardServiceTableDischarged() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.createdAt is not null "
                + " and b.patientEncounter.discharged=true and"
                + " b.billType = :billType and b.createdAt between :fromDate and :toDate "
                + "and b.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += "order by b.deptId desc";

        temMap.put("billType", BillType.InwardBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createInwardFinalBillsCheck() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where"
                + " b.billType = :billType and "
                + " b.patientEncounter.dateOfDischarge between :fromDate and :toDate "
                + "and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.deptId desc  ";

        temMap.put("billType", BillType.InwardFinalBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createInwardFinalBills() {
//        double d = commonController.dateDifferenceInMinutes(fromDate, toDate) / (60 * 24);
//        if (d > 32 && getReportKeyWord().isBool1()) {
//            JsfUtil.addErrorMessage("Date Range To Long");
//            return;
//        }
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where"
                + " b.billType = :billType and "
                + " b.createdAt between :fromDate and :toDate "
                + "and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.deptId desc  ";

        temMap.put("billType", BillType.InwardFinalBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

//        if (getReportKeyWord().isBool1()) {
        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
//        } else {
//            bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);
//        }

    }

    public void createInwardProvisionalBills() {
//        double d = commonController.dateDifferenceInMinutes(fromDate, toDate) / (60 * 24);
//        if (d > 32 && getReportKeyWord().isBool1()) {
//            JsfUtil.addErrorMessage("Date Range To Long");
//            return;
//        }
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where"
                + " b.billType = :billType and "
                + " b.createdAt between :fromDate and :toDate "
                + "and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            try {
                double netTotal = Double.parseDouble(getSearchKeyword().getNetTotal().trim());
                sql += " and  ((b.netTotal) = :netTotal )";
                temMap.put("netTotal", netTotal);
            } catch (NumberFormatException e) {
                JsfUtil.addErrorMessage("Invalid number format for Net Total");
            }
        }

        sql += " order by b.deptId desc  ";

        temMap.put("billType", BillType.InwardProvisionalBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

//        if (getReportKeyWord().isBool1()) {
        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
//        } else {
//            bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);
//        }

    }

    public void createCancelledInwardFinalBills() {
        Date startTime = new Date();
        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where"
                + " b.billType = :billType and "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.cancelled=true ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.deptId desc  ";

        temMap.put("billType", BillType.InwardFinalBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

//        if (getReportKeyWord().isBool1()) {
        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
//        } else {
//            bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);
//        }

    }

    public void createInwardIntrimBills() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where"
                + " b.billType = :billType and "
                + " b.createdAt between :fromDate and :toDate "
                + "and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.insId desc  ";

        temMap.put("billType", BillType.InwardIntrimBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createInwardPaymentBills() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where"
                + " b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.deptId desc  ";

        temMap.put("billType", BillType.InwardPaymentBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createInwardRefundBills() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from RefundBill b where "
                + " b.billType = :billType "
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.deptId desc  ";

        temMap.put("billType", BillType.InwardPaymentBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void searchSurgery() {
        Date startTime = new Date();

        if (searchKeyword.isActiveAdvanceOption() && searchKeyword.getItem() == null && searchKeyword.getItemName().equals("")) {
            JsfUtil.addErrorMessage("You Need To select Surgury to Search All");
            return;
        }

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where "
                + " b.billType = :billType and "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.retired=false  ";

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.staff.person.name) like :staffName )";
            temMap.put("staffName", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null
                && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.procedure.item.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItem() != null) {
            sql += " and b.procedure.item=:item ";
            temMap.put("item", getSearchKeyword().getItem());
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("billType", BillType.SurgeryBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        if (searchKeyword.isActiveAdvanceOption()) {
            bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
        } else {
            bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);
        }

    }

    public void createInwardSurgeryBills() {
        Date startTime = new Date();

        if (searchKeyword.isActiveAdvanceOption() && searchKeyword.getItem() == null && searchKeyword.getItemName().equals("")) {
            JsfUtil.addErrorMessage("You Need To select Surgury to Search All");
            return;
        }

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where "
                + " b.billType = :billType and "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.retired=false  ";

        if (getSearchKeyword().getStaffName() != null
                && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.staff.person.name) like :staffName )";
            temMap.put("staffName", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientName() != null
                && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null
                && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null
                && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null
                && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null
                && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.procedure.item.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItem() != null) {
            sql += " and b.procedure.item=:item ";
            temMap.put("item", getSearchKeyword().getItem());
        }

        if (getSearchKeyword().getTotal() != null
                && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.deptId desc  ";

        temMap.put("billType", BillType.SurgeryBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        if (searchKeyword.isActiveAdvanceOption()) {
            bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
        } else {
            bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);
        }

    }

    public void createInwardSurgeryBillsReport() {

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where "
                + " b.billType = :billType and "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.retired=false  ";

        if (getSearchKeyword().getStaffName() != null
                && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.staff.person.name) like :staffName )";
            temMap.put("staffName", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientName() != null
                && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null
                && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null
                && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null
                && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null
                && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  ((b.procedure.item.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null
                && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  ((b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.deptId desc  ";

        temMap.put("billType", BillType.SurgeryBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createInwardPaymentBillsDischarged() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where  "
                + " and b.patientEncounter.discharged=true"
                + " and b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += "order by b.deptId desc";

        temMap.put("billType", BillType.InwardPaymentBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createInwardProBills() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where "
                + " b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.insId desc";

        temMap.put("billType", BillType.InwardProfessional);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createInwardProBillsDischarged() {

        String sql;
        Map temMap = new HashMap();
//        sql = "select b from BilledBill b where b.createdAt is not null and b.billType = :billType and b.patientEncounter.discharged=true and "
//                + " b.id in(Select bf.bill.id From BillFee bf where bf.retired=false and bf.createdAt between :fromDate and :toDate and bf.billItem is null)"
//                + " and b.createdAt between :fromDate and :toDate and b.retired=false";

        sql = "select b from BilledBill b where b.createdAt is not null "
                + " and b.patientEncounter.discharged=true and"
                + " b.billType = :billType and b.createdAt between :fromDate and :toDate "
                + "and b.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  ((b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  ((b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += "order by b.deptId desc";

        temMap.put("billType", BillType.InwardBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createPettyTable() {
        List<BillType> billTypes = new ArrayList<>();
        billTypes.add(BillType.PettyCash);
        billTypes.add(BillType.IouIssue);

        Date startTime = new Date();

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType IN :billTypes and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getStaff() != null) {
            sql += " and  ((b.staff) =:stf )";
            temMap.put("stf", getStaff());
        }

        if (getSearchKeyword().getPersonName() != null && !getSearchKeyword().getPersonName().trim().equals("")) {
            sql += " and  ((b.person.name) like :per )";
            temMap.put("per", "%" + getSearchKeyword().getPersonName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  ((b.invoiceNumber) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//

        temMap.put("billTypes", billTypes);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createPettyApproveTable() {
        List<BillType> billTypes = new ArrayList<>();
        billTypes.add(BillType.PettyCashCancelApprove);

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where b.billType IN :billTypes and b.createdAt between :fromDate and :toDate and b.retired=false";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTypes", billTypes);
        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createPettyCashToApproveTable() {
        List<BillTypeAtomic> billTypes = new ArrayList<>();
        billTypes.add(BillTypeAtomic.PETTY_CASH_ISSUE);

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where b.billTypeAtomic IN :billTypes and b.createdAt between :fromDate and :toDate and b.retired=false order by b.id desc";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTypes", billTypes);
        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createCreditCompanyPaymentToApproveTable() {
        List<BillTypeAtomic> billTypes = new ArrayList<>();
        billTypes.add(BillTypeAtomic.OPD_CREDIT_COMPANY_PAYMENT_RECEIVED);

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where b.billTypeAtomic IN :billTypes and b.createdAt between :fromDate and :toDate and b.retired=false order by b.id desc";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTypes", billTypes);
        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createIncomeBillTable() {
        fetchBillTable(BillType.ChannelIncomeBill);
    }

    public void createExpensesBillTable() {
        fetchBillTable(BillType.ChannelExpenesBill);
    }

    public void fetchBillTable(BillType billType) {
        Date startTime = new Date();

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType =:billType "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  ((b.staff.person.name) like :stf )";
            temMap.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPersonName() != null && !getSearchKeyword().getPersonName().trim().equals("")) {
            sql += " and  ((b.person.name) like :per )";
            temMap.put("per", "%" + getSearchKeyword().getPersonName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  ((b.netTotal) = :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  ((b.invoiceNumber) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//
        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createAllBillContacts() {
        String sql;
        Map temMap = new HashMap();
        telephoneNumbers = new ArrayList<>();

        if (getReportKeyWord().getString1().equals("0")) {
            sql = "select b.patient.person.phone from Bill b where ";
        } else {
            sql = "select b from Bill b where ";
        }

        sql += " b.retired = false "
                + " and b.cancelled=false "
                + " and b.refunded=false "
                + " and (b.patient.person.phone is not null "
                + " or b.patient.person.phone!=:em) "
                + " and b.createdAt between :fd and :td  ";

        if (getReportKeyWord().getString().equals("0")) {
        }
        if (getReportKeyWord().getString().equals("1")) {
            BillType[] billTypes = {BillType.ChannelCash, BillType.ChannelPaid};
            sql += " and b.billType in :bts ";
            temMap.put("bts", Arrays.asList(billTypes));
        }
        if (getReportKeyWord().getString().equals("2")) {
            BillType[] billTypes = {BillType.OpdBill};
            sql += " and b.billType in :bts ";
            temMap.put("bts", Arrays.asList(billTypes));
        }
        if (getReportKeyWord().getString().equals("3")) {
            BillType[] billTypes = {BillType.PharmacySale};
            sql += " and b.billType in :bts ";
            temMap.put("bts", Arrays.asList(billTypes));
        }
        if (getReportKeyWord().getArea() != null) {
            sql += " and b.patient.person.area=:a ";
            temMap.put("a", getReportKeyWord().getArea());
        }

        if (getReportKeyWord().getString1().equals("0")) {
            sql += " group by b.patient.person.phone ";
        }
        sql += " order by b.patient.person.phone ";

        temMap.put("em", "");
        temMap.put("fd", fromDate);
        temMap.put("td", toDate);

        if (getReportKeyWord().getString1().equals("0")) {
            List<Object> objs = getBillFacade().findObjectByJpql(sql, temMap, TemporalType.TIMESTAMP);

            for (Object o : objs) {
                String s = (String) o;
                if (s != null && !"".equals(s)) {
                    String ss = s.substring(0, 3);
                    if (ss.equals("077") || ss.equals("076")
                            || ss.equals("071") || ss.equals("072")
                            || ss.equals("075") || ss.equals("078")) {
                        telephoneNumbers.add(s);
                    }

                }
            }
        } else {
            bills = getBillFacade().findByJpql(sql, temMap, TemporalType.TIMESTAMP);
            for (Bill b : bills) {
                if (b.getPatient().getPerson().getPhone() != null && !"".equals(b.getPatient().getPerson().getPhone())) {
                    String ss = b.getPatient().getPerson().getPhone().substring(0, 3);
                    if (getReportKeyWord().getString1().equals("1")) {
                        if (b.getPatient().getAgeYears() <= getReportKeyWord().getFrom()) {
                            if (ss.equals("077") || ss.equals("076")
                                    || ss.equals("071") || ss.equals("072")
                                    || ss.equals("075") || ss.equals("078")) {
                                telephoneNumbers.add(b.getPatient().getPerson().getPhone());
                            }
                        }
                    }
                    if (getReportKeyWord().getString1().equals("2")) {
                        if (b.getPatient().getAgeYears() >= getReportKeyWord().getTo()) {
                            if (b.getPatient().getAgeYears() <= getReportKeyWord().getFrom()) {
                                if (ss.equals("077") || ss.equals("076")
                                        || ss.equals("071") || ss.equals("072")
                                        || ss.equals("075") || ss.equals("078")) {
                                    telephoneNumbers.add(b.getPatient().getPerson().getPhone());
                                }
                            }
                        }
                    }
                    if (getReportKeyWord().getString1().equals("3")) {
                        if (b.getPatient().getAgeYears() >= getReportKeyWord().getFrom()
                                && b.getPatient().getAgeYears() <= getReportKeyWord().getTo()) {
                            if (b.getPatient().getAgeYears() <= getReportKeyWord().getFrom()) {
                                if (ss.equals("077") || ss.equals("076")
                                        || ss.equals("071") || ss.equals("072")
                                        || ss.equals("075") || ss.equals("078")) {
                                    telephoneNumbers.add(b.getPatient().getPerson().getPhone());
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    //    public void sendSms() {
//        smsController.sendSmsToNumberList(uniqueSmsText, getSessionController().getApplicationPreference().getApplicationInstitution(), smsText, null, MessageType.Marketing);
//    }
//    public void sendSmsAll() {
//        if (selectedTelephoneNumbers == null) {
//            JsfUtil.addErrorMessage("Please Select Numbers");
//            return;
//        }
//        if (selectedTelephoneNumbers.size() > 10000) {
//            JsfUtil.addErrorMessage("Please Contact System Development Team.You are trying to send more than 10,000 sms.");
//            return;
//        }
//        if (smsText.equals("") || smsText == null) {
//            JsfUtil.addErrorMessage("Enter Message");
//            return;
//        }
//        for (String stn : selectedTelephoneNumbers) {
//
//            smsController.sendSmsToNumberList(stn, getSessionController().getApplicationPreference().getApplicationInstitution(), smsText, null, MessageType.Marketing);
//            JsfUtil.addSuccessMessage("Done.");
//        }
//
//    }
    public String navigateToCancelPurchaseOrder() {
        makeNull();
        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.MONTH, c1.get(Calendar.MONTH) - 2);
        fromDate = c1.getTime();
        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.MONTH, c2.get(Calendar.MONTH) - 1);
        toDate = c2.getTime();
        return "/pharmacy/pharmacy_purhcase_order_list_to_cancel?faces-redirect=true";
    }

    public String navigateToPurchaseOrderFinalize() {
        makeNull();
        return "/pharmacy/pharmacy_purhcase_order_list_to_finalize?faces-redirect=true";
    }

    public String navigateToTransferRequestFinalize() {
        makeNull();
        return "/pharmacy/pharmacy_transfer_request_list_to_finalize?faces-redirect=true";
    }

    // ToDo: TO Be Linked to command buttons where the file name is used without calling a backend method
    /**
     * Samples of places to be updated as follows
     * <p:commandButton
     * ajax="false"
     * action="pharmacy_purchase_order_list_for_recieve"
     * rendered="#{!configOptionApplicationController.getBooleanValueByKey('Pharmacy Good Recipt With Approval')}"
     * class="ui-button-warning"
     * icon="fa fa-arrow-left"
     * actionListener="#{grnController.viewPoList()}"
     * value="Back to PO List">
     * </p:commandButton>
     */
    /**
     *
     * @return
     */
    public String navigateToListPurchaseOrdersToReceive() {
        return "/pharmacy/pharmacy_purchase_order_list_for_recieve?faces-redirect=true";
    }

    public String navigateToPurchaseOrderApprove() {
        makeListNull();
        return "/pharmacy/pharmacy_purhcase_order_list_to_approve?faces-redirect=true";
    }

    public void createDocPaymentDue() {
        if (getReportKeyWord().getString().equals("0")) {
            fetchDueFeeTable(new BillType[]{BillType.OpdBill, BillType.CollectingCentreBill}, false);
        } else {
            fetchDueFeeTable(new BillType[]{BillType.InwardBill, BillType.InwardProfessional}, true);
        }
    }

    private void fetchDueFeeTable(BillType[] billTypes, boolean isInward) {
        String sql;
        Map m = new HashMap();

        sql = "select b from BillFee b where"
                + " b.retired=false "
                + " and b.bill.billType in :bts "
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 "
                + " and b.bill.createdAt between :fromDate and :toDate ";

        sql += " order by b.staff.person.name ";

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(2013, 00, 01, 00, 00, 00);
        m.put("fromDate", cal.getTime());
        m.put("toDate", getToDate());
        m.put("bts", Arrays.asList(billTypes));

        billFees = getBillFeeFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);

        List<BillFee> afterPaid = new ArrayList<>();

        sql = "Select bi.paidForBillFee FROM BillItem bi "
                + " where bi.retired=false "
                + " and bi.bill.billType=:bType "
                + " and bi.referenceBill.billType in :refType "
                + " and bi.bill.createdAt > :toDate "
                + " and bi.paidForBillFee.bill.createdAt <= :toDate";

//        sql += " order by b.createdAt desc  ";
        m = new HashMap();
        m.put("toDate", getToDate());
        m.put("bType", BillType.PaymentBill);
        m.put("refType", Arrays.asList(billTypes));
//        temMap.put("refType", BillType.OpdBill);

        afterPaid = getBillFeeFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);
        billFees.addAll(afterPaid);
        List<BillFee> removeingBillFees = new ArrayList<>();

        for (BillFee bf : billFees) {
            sql = "SELECT bi FROM BillItem bi where bi.retired=false "
                    + " and bi.bill.cancelled=false "
                    + " and bi.referanceBillItem.id=" + bf.getBillItem().getId();

            BillItem rbi = null;
            if (isInward) {
                m = new HashMap();
                m.put("btp", BillType.InwardBill);
                sql = "SELECT bi FROM BillItem bi where bi.retired=false "
                        + " and bi.bill.cancelled=false "
                        + " and bi.bill.billType=:btp "
                        + " and bi.referanceBillItem.id=" + bf.getBillItem().getId();
            } else {
                m = new HashMap();
//                m.put("class", RefundBill.class);
                sql = "SELECT bi FROM BillItem bi where "
                        + " bi.retired=false"
                        + " and bi.bill.cancelled=false "
                        //                        + " and type(bi.bill)=:class "
                        + " and bi.referanceBillItem.id=" + bf.getBillItem().getId();
            }
            rbi = getBillItemFacade().findFirstByJpql(sql, m);

            if (rbi != null) {
                removeingBillFees.add(bf);
            }
        }
        billFees.removeAll(removeingBillFees);
        total = 0.0;
        for (BillFee bf : billFees) {
            total += bf.getFeeValue();
        }
    }

    public void listnerBillTypeChange() {
        reportKeyWord.setArea(null);
    }

    public String navigateToLabReportSearch() {
        patientInvestigations = new ArrayList<>();
        return "/lab/search_for_reporting_ondemand?faces-redirect=true";
    }

    public String navigateToListSingleUserBills() {
        processAllFinancialTransactionalBillListBySingleUserByIds();
        return "/cashier/shift_end_summary_bill_list";
    }

    public String navigateToListCashBookEntry() {
        cashBookEntries = new ArrayList<>();
        return "/cashier/cash_book_entry";
    }

    public String navigateToListCashBookEntrySiteSummary() {
        cashBookEntries = new ArrayList<>();
        return "/cashier/cash_book_summery_site";
    }

    public String navigateToPatientReportSearch() {
        patientInvestigations = new ArrayList<>();
        return "/lab/patient_reports_search?faces-redirect=true";
    }

    public String navigateToLabAnalytics() {
        patientInvestigations = new ArrayList<>();
        return "/reportLab/lab_summeries_index?faces-redirect=true";
    }

    public void genarateCashBookEntries() {
        cashBookEntries = cashBookEntryController.genarateCashBookEntries(fromDate, toDate, site, institution, department);
    }

    public void generateOpdServicesByBillItem() {
        bundleBillItems = generateOpdServiceByBillItems();
    }

    public void createItemizedSalesSummary() {
        bundle = generateItemizedSalesSummary();
    }

    public void createItemizedSalesSummaryDto() {
        opdSaleSummaryDtos = generateItemizedSalesSummaryDto();
    }

    public void createItemizedSalesReport() {
        bundle = generateItemizedSalesReport();
        if (withProfessionalFee) {
            bundle.setName("Itemized Sales Report - With Professional Fee");
            bundle.setBundleType("itemized_sales_report_with_professional_fee");
        } else {
            bundle.setName("Itemized Sales Report - Without Professional Fee");
            bundle.setBundleType("itemized_sales_report_without_professional_fee");
        }

    }

    public void createIncomeBreakdownByCategory() {
        bundle = generateIncomeBreakdownByCategory();
        if (withProfessionalFee) {
            bundle.setName("Income Breakdown By Category - With Professional Fee");
            bundle.setBundleType("income_breakdown_by_category_with_professional_fee");
        } else {
            bundle.setName("Income Breakdown By Category - Without Professional Fee");
            bundle.setBundleType("income_breakdown_by_category_with_out_professional_fee");
        }

    }

    public void generateStaffWelfareBillReport() {
        reportTimerController.trackReportExecution(() -> {
            bundle = new ReportTemplateRowBundle();

            if (reportType == null || reportType.trim().equals("")) {
                JsfUtil.addErrorMessage("Please select a report type");
                return;
            }

            List<PaymentMethod> staffPaymentMethods = new ArrayList<>();
            staffPaymentMethods.add(PaymentMethod.Staff_Welfare);

            List<BillTypeAtomic> opdBts = new ArrayList<>();

            if (reportType.trim().equalsIgnoreCase("bills")) {
                opdBts.add(BillTypeAtomic.OPD_BATCH_BILL_WITH_PAYMENT);
                opdBts.add(BillTypeAtomic.OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER);
                opdBts.add(BillTypeAtomic.OPD_BATCH_BILL_CANCELLATION);
                opdBts.add(BillTypeAtomic.OPD_BILL_CANCELLATION);
                opdBts.add(BillTypeAtomic.OPD_BILL_REFUND);
                opdBts.add(BillTypeAtomic.OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION);

                opdBts.add(BillTypeAtomic.PACKAGE_OPD_BATCH_BILL_WITH_PAYMENT);
                opdBts.add(BillTypeAtomic.PACKAGE_OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER);
                opdBts.add(BillTypeAtomic.PACKAGE_OPD_BATCH_BILL_CANCELLATION);
                opdBts.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION);
                opdBts.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION);
                opdBts.add(BillTypeAtomic.PACKAGE_OPD_BILL_REFUND);

                opdBts.add(BillTypeAtomic.PHARMACY_RETAIL_SALE);
                opdBts.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_CANCELLED);
                opdBts.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_PREBILL_SETTLED_AT_CASHIER);
                opdBts.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_RETURN_ITEMS_AND_PAYMENTS);

                bundle.setName("Staff Welfare Bills");
                bundle.setBundleType("billList");
                bundle = generatePaymentMethodColumnsByStaffWelfareBills(opdBts, staffPaymentMethods);
                bundle.calculateTotalByBills();

            } else if (reportType.trim().equalsIgnoreCase("billItems")) {
                opdBts.add(BillTypeAtomic.OPD_BILL_WITH_PAYMENT);
                opdBts.add(BillTypeAtomic.OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER);
                opdBts.add(BillTypeAtomic.OPD_BATCH_BILL_WITH_PAYMENT);
                opdBts.add(BillTypeAtomic.OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER);
                opdBts.add(BillTypeAtomic.PACKAGE_OPD_BATCH_BILL_WITH_PAYMENT);
                opdBts.add(BillTypeAtomic.PACKAGE_OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER);
                opdBts.add(BillTypeAtomic.OPD_BATCH_BILL_CANCELLATION);
                opdBts.add(BillTypeAtomic.OPD_BILL_CANCELLATION);
                opdBts.add(BillTypeAtomic.PACKAGE_OPD_BATCH_BILL_CANCELLATION);
                opdBts.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION);
                opdBts.add(BillTypeAtomic.OPD_BILL_REFUND);
                opdBts.add(BillTypeAtomic.OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION);
                opdBts.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION);
                opdBts.add(BillTypeAtomic.PACKAGE_OPD_BILL_REFUND);

                opdBts.add(BillTypeAtomic.PHARMACY_RETAIL_SALE);
                opdBts.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_CANCELLED);
                opdBts.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_PREBILL_SETTLED_AT_CASHIER);
                opdBts.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_RETURN_ITEMS_AND_PAYMENTS);

                bundle.setName("Staff Welfare Bill Items");
                bundle.setBundleType("billItemList");
                bundle = generateStaffWelfareBillItems(opdBts, staffPaymentMethods);
                bundle.calculateTotalByBillItems();
            }
        }, FinancialReport.STAFF_WELFARE_REPORT, sessionController.getLoggedUser());
    }

    public void generateDailyReturn() {
        reportTimerController.trackReportExecution(() -> {

            bundle = new ReportTemplateRowBundle();
            bundle.setName("Daily Return");
            bundle.setBundleType("dailyReturn");

            String institutionName = institution != null ? institution.getName() : "All Institutions";
            String siteName = site != null ? site.getName() : "All Sites";
            String departmentName = department != null ? department.getName() : "All Departments";

            String dateTimeFormat = sessionController.getApplicationPreference().getLongDateTimeFormat();

            String formattedFromDate = fromDate != null ? new SimpleDateFormat(dateTimeFormat).format(fromDate) : "Not availbale";
            String formattedToDate = toDate != null ? new SimpleDateFormat(dateTimeFormat).format(toDate) : "Not availbale";

            String description = String.format("Report for %s to %s covering %s, %s, %s",
                    formattedFromDate, formattedToDate,
                    institutionName, siteName, departmentName);

            bundle.setDescription(description);

            double collectionForTheDay = 0.0;
            double netCashCollection = 0.0;
            double netCollectionPlusCredits = 0.0;

            ReportTemplateRowBundle opdServiceCollection;
            if (isWithProfessionalFee()) {
                opdServiceCollection = generateOpdServiceCollection(PaymentType.NON_CREDIT);
            } else {
                opdServiceCollection = generateOpdServiceCollectionWithoutProfessionalFee(PaymentType.NON_CREDIT);
            }
            bundle.getBundles().add(opdServiceCollection);
            collectionForTheDay += getSafeTotal(opdServiceCollection);

            // Generate pharmacy collection and add to the main bundle
            ReportTemplateRowBundle pharmacyCollection = generatePharmacyCollection();
            bundle.getBundles().add(pharmacyCollection);
            collectionForTheDay += getSafeTotal(pharmacyCollection);

            // Generate collecting centre collection and add to the main bundle
            ReportTemplateRowBundle ccCollection = generateCcCollection();
            bundle.getBundles().add(ccCollection);
            collectionForTheDay += getSafeTotal(ccCollection);

            // Generate OPD Credit Company Payment Collection and add to the main bundle
            ReportTemplateRowBundle opdCreditCompanyCollection = generateCreditCompanyCollectionForOpd();
            bundle.getBundles().add(opdCreditCompanyCollection);
            collectionForTheDay += getSafeTotal(opdCreditCompanyCollection);

            // Generate Inward Credit Company Payment Collection and add to the main bundle
            ReportTemplateRowBundle inwardCreditCompanyCollection = generateCreditCompanyCollectionForInward();
            bundle.getBundles().add(inwardCreditCompanyCollection);
            collectionForTheDay += getSafeTotal(inwardCreditCompanyCollection);

            // Generate Pharmacy Credit Company Payment Collection and add to the main bundle
            ReportTemplateRowBundle pharmacyCreditCompanyCollection = generateCreditCompanyCollectionForPharmacy();
            bundle.getBundles().add(pharmacyCreditCompanyCollection);
            collectionForTheDay += getSafeTotal(pharmacyCreditCompanyCollection);

            // Generate Channelling Credit Company Payment Collection and add to the main bundle
            ReportTemplateRowBundle channellingCreditCompanyCollection = generateCreditCompanyCollectionForChannelling();
            bundle.getBundles().add(channellingCreditCompanyCollection);
            collectionForTheDay += getSafeTotal(channellingCreditCompanyCollection);

            ReportTemplateRowBundle patientDepositPayments = generatePatientDepositCollection();
            bundle.getBundles().add(patientDepositPayments);
            collectionForTheDay += getSafeTotal(patientDepositPayments);

            // Final collection for the day
            ReportTemplateRowBundle collectionForTheDayBundle = new ReportTemplateRowBundle();
            collectionForTheDayBundle.setName("Collection for the day");
            collectionForTheDayBundle.setBundleType("collectionForTheDay");
            collectionForTheDayBundle.setTotal(collectionForTheDay);
            bundle.getBundles().add(collectionForTheDayBundle);

            netCashCollection = collectionForTheDay;

            // Deduct various payments from net cash collection
            ReportTemplateRowBundle pettyCashPayments = generatePettyCashPayments();
            bundle.getBundles().add(pettyCashPayments);
            netCashCollection -= Math.abs(getSafeTotal(pettyCashPayments));

            ReportTemplateRowBundle creditBills;
            if (isWithProfessionalFee()) {
                creditBills = generateCreditBills();
            } else {
                creditBills = generateCreditBills();
            }
            bundle.getBundles().add(creditBills);
//        netCashCollection -= Math.abs(getSafeTotal(creditBills)); // NOT Deducted from Totals

            if (isWithProfessionalFee()) {
                // Generate OPD professional payments and add to the main bundle
                ReportTemplateRowBundle opdProfessionalPayments = generateOpdProfessionalPayments();
                bundle.getBundles().add(opdProfessionalPayments);
                netCashCollection -= Math.abs(getSafeTotal(opdProfessionalPayments));
            }

            // Generate channelling professional payments and add to the main bundle
            ReportTemplateRowBundle channellingProfessionalPayments = generateChannellingProfessionalPayments();
            bundle.getBundles().add(channellingProfessionalPayments);
            netCashCollection -= Math.abs(getSafeTotal(channellingProfessionalPayments));

            // Generate inward professional payments and add to the main bundle
            ReportTemplateRowBundle inwardProfessionalPayments = generateInwardProfessionalPayments();
            bundle.getBundles().add(inwardProfessionalPayments);
            netCashCollection -= Math.abs(getSafeTotal(inwardProfessionalPayments));

            ReportTemplateRowBundle cardPayments = generateCreditCardPayments();
            cardPayments.calculateTotalByPayments();
            bundle.getBundles().add(cardPayments);
            netCashCollection -= Math.abs(getSafeTotal(cardPayments));

            ReportTemplateRowBundle staffPayments = generateStaffPayments();
            bundle.getBundles().add(staffPayments);
            netCashCollection -= Math.abs(getSafeTotal(staffPayments));

            ReportTemplateRowBundle voucherPayments = generateVoucherPayments();
            bundle.getBundles().add(voucherPayments);
            netCashCollection -= Math.abs(getSafeTotal(voucherPayments));

            ReportTemplateRowBundle chequePayments = generateChequePayments();
            bundle.getBundles().add(chequePayments);
            netCashCollection -= Math.abs(getSafeTotal(chequePayments));

            ReportTemplateRowBundle ewalletPayments = generateEwalletPayments();
            bundle.getBundles().add(ewalletPayments);
            netCashCollection -= Math.abs(getSafeTotal(ewalletPayments));

            ReportTemplateRowBundle slipPayments = generateSlipPayments();
            bundle.getBundles().add(slipPayments);
            netCashCollection -= Math.abs(getSafeTotal(slipPayments));

            // Final net cash for the day
            ReportTemplateRowBundle netCashForTheDayBundle = new ReportTemplateRowBundle();
            netCashForTheDayBundle.setName("Net Cash");
            netCashForTheDayBundle.setBundleType("netCash");
            netCashForTheDayBundle.setTotal(netCashCollection);
            bundle.getBundles().add(netCashForTheDayBundle);

            ReportTemplateRowBundle opdServiceCollectionCredit;
            opdServiceCollectionCredit = generateCreditOpdServiceCollection();
            bundle.getBundles().add(opdServiceCollectionCredit);
            netCollectionPlusCredits = netCashCollection + Math.abs(getSafeTotal(opdServiceCollectionCredit)); // NOT Deducted from Totals

            // Final net cash for the day
            ReportTemplateRowBundle netCashForTheDayBundlePlusCredits = new ReportTemplateRowBundle();
            netCashForTheDayBundlePlusCredits.setName("Net Cash Plus Credits");
            netCashForTheDayBundlePlusCredits.setBundleType("netCashPlusCredit");
            netCashForTheDayBundlePlusCredits.setTotal(netCollectionPlusCredits);
            bundle.getBundles().add(netCashForTheDayBundlePlusCredits);

        }, FinancialReport.DAILY_RETURN, sessionController.getLoggedUser());
    }

    public ReportTemplateRowBundle generatePaymentColumnForCollections(List<BillTypeAtomic> bts, List<PaymentMethod> pms) {
        ReportTemplateRowBundle b = new ReportTemplateRowBundle();

        Map<String, Object> parameters = new HashMap<>();
        String jpql = "SELECT new com.divudi.core.data.ReportTemplateRow("
                + "bill.department, FUNCTION('date', p.createdAt), p.creater, "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cash THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Card THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.MultiplePaymentMethods THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Credit THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff_Welfare THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Voucher THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.IOU THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Agent THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cheque THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Slip THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.ewallet THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientDeposit THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientPoints THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.OnlineSettlement THEN p.paidValue ELSE 0 END)) "
                + "FROM Payment p "
                + "JOIN p.bill bill "
                + "WHERE p.retired <> :bfr "
                + "AND bill.retired <> :br "
                + "AND bill.billTypeAtomic in :bts "
                + "AND p.paymentMethod in :pms ";

        parameters.put("bfr", true);
        parameters.put("br", true);
        parameters.put("bts", bts);
        parameters.put("pms", pms);

        if (institution != null) {
            jpql += "AND bill.department.institution = :ins ";
            parameters.put("ins", institution);
        }
        if (department != null) {
            jpql += "AND bill.department = :dep ";
            parameters.put("dep", department);
        }
        if (site != null) {
            jpql += "AND bill.department.site = :site ";
            parameters.put("site", site);
        }
        if (webUser != null) {
            jpql += "AND bill.creater = :wu ";
            parameters.put("wu", webUser);
        }
        if (paymentMethod != null) {
            jpql += "AND p.paymentMethod = :pm ";
            parameters.put("pm", paymentMethod);
        }

        jpql += "AND p.createdAt BETWEEN :fd AND :td ";
        parameters.put("fd", fromDate);
        parameters.put("td", toDate);

        jpql += "GROUP BY bill.department, FUNCTION('date', p.createdAt), p.creater";

        List<ReportTemplateRow> rs = (List<ReportTemplateRow>) paymentFacade.findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

        b.setReportTemplateRows(rs);
        b.calculateTotalsWithCredit();
        return b;
    }

    public ReportTemplateRowBundle generateTotalPaymentColumnByDepartment(List<BillTypeAtomic> bts, List<PaymentMethod> pms) {
        ReportTemplateRowBundle b = new ReportTemplateRowBundle();

        Map<String, Object> parameters = new HashMap<>();
        String jpql = "SELECT new com.divudi.core.data.ReportTemplateRow("
                + "bill.department, sum(p.paidValue) ) "
                + "FROM Payment p "
                + "JOIN p.bill bill "
                + "WHERE p.retired <> :bfr "
                + "AND bill.retired <> :br "
                + "AND bill.billTypeAtomic in :bts "
                + "AND p.paymentMethod in :pms ";

        parameters.put("bfr", true);
        parameters.put("br", true);
        parameters.put("bts", bts);
        parameters.put("pms", pms);

        if (institution != null) {
            jpql += "AND bill.department.institution = :ins ";
            parameters.put("ins", institution);
        }
        if (department != null) {
            jpql += "AND bill.department = :dep ";
            parameters.put("dep", department);
        }
        if (site != null) {
            jpql += "AND bill.department.site = :site ";
            parameters.put("site", site);
        }
        if (webUser != null) {
            jpql += "AND bill.creater = :wu ";
            parameters.put("wu", webUser);
        }
        if (paymentMethod != null) {
            jpql += "AND p.paymentMethod = :pm ";
            parameters.put("pm", paymentMethod);
        }

        jpql += "AND p.createdAt BETWEEN :fd AND :td ";
        parameters.put("fd", fromDate);
        parameters.put("td", toDate);

        jpql += "GROUP BY bill.department";

        List<ReportTemplateRow> rs = (List<ReportTemplateRow>) paymentFacade.findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

        b.setReportTemplateRows(rs);
        b.calculateTotalsByAddingRowTotals();
        return b;
    }

    public void generateMyCashierSummary() {
        institution = null;
        department = null;
        site = null;
        webUser = sessionController.getLoggedUser();
        generateCashierSummary();
    }

    public String navigateToSelectedCashierSummary(WebUser wu) {
        bundle = new ReportTemplateRowBundle();
        if (!configOptionApplicationController.getBooleanValueByKey("Restrict Filter Reset of Cashier Summaries", false)) {
            institution = null;
            department = null;
            site = null;
        }
        webUser = wu;
        generateCashierSummary();
        return "/reports/cashier_reports/cashier_summary?faces-redirect=true";
    }

    public String navigateToSelectedCashierDetails(WebUser wu) {
        bundle = new ReportTemplateRowBundle();
        if (!configOptionApplicationController.getBooleanValueByKey("Restrict Filter Reset of Cashier Summaries", false)) {
            institution = null;
            department = null;
            site = null;
        }
        paymentMethod = null;
        webUser = wu;
        generateCashierDetailed();
        return "/reports/cashier_reports/cashier_detailed?faces-redirect=true";
    }

    public void generateCashierSummary() {
        reportTimerController.trackReportExecution(() -> {
            bundle = new ReportTemplateRowBundle();

            paymentMethod = null;

            double collectionForTheDay = 0.0;
            double netCashCollection = 0.0;

            List<PaymentMethod> creditPaymentMethods = PaymentMethod.getMethodsByType(PaymentType.CREDIT);
            List<PaymentMethod> nonCreditPaymentMethods = PaymentMethod.getMethodsByType(PaymentType.NON_CREDIT);

            List<BillTypeAtomic> opdBts = new ArrayList<>();
            opdBts.add(BillTypeAtomic.OPD_BATCH_BILL_WITH_PAYMENT);
            opdBts.add(BillTypeAtomic.OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER);
            opdBts.add(BillTypeAtomic.PACKAGE_OPD_BATCH_BILL_WITH_PAYMENT);
            opdBts.add(BillTypeAtomic.PACKAGE_OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER);

            List<BillTypeAtomic> opdCancellations = new ArrayList<>();
            opdCancellations.add(BillTypeAtomic.OPD_BATCH_BILL_CANCELLATION);
            opdCancellations.add(BillTypeAtomic.OPD_BILL_CANCELLATION);
            opdCancellations.add(BillTypeAtomic.PACKAGE_OPD_BATCH_BILL_CANCELLATION);
            opdCancellations.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION);

            List<BillTypeAtomic> opdRefunds = new ArrayList<>();
            opdRefunds.add(BillTypeAtomic.OPD_BILL_REFUND);

            // Generate OPD service collection and add to the main bundle
            ReportTemplateRowBundle opdServiceCollection = generatePaymentColumnForCollections(opdBts, nonCreditPaymentMethods);
            opdServiceCollection.setBundleType("cashierSummaryOpd");
            opdServiceCollection.setName("OPD Collection");
            bundle.getBundles().add(opdServiceCollection);
            collectionForTheDay += getSafeTotal(opdServiceCollection);

            // Generate OPD service collection and add to the main bundle
            ReportTemplateRowBundle opdServiceCancellations = generatePaymentMethodColumnsByBills(opdCancellations, nonCreditPaymentMethods);
            opdServiceCancellations.setBundleType("opdServiceCancellations");
            opdServiceCancellations.setName("OPD Service Cancellations");
            bundle.getBundles().add(opdServiceCancellations);
            collectionForTheDay += getSafeTotal(opdServiceCancellations);

            // Generate OPD service Refunds and add to the main bundle
            ReportTemplateRowBundle opdServiceRefunds = generatePaymentMethodColumnsByBills(opdRefunds, nonCreditPaymentMethods);
            opdServiceRefunds.setBundleType("opdServiceRefunds");
            opdServiceRefunds.setName("OPD Service Refunds");
            bundle.getBundles().add(opdServiceRefunds);
            collectionForTheDay += getSafeTotal(opdServiceRefunds);

            // Generate OPD service collection for credit and add to the main bundle
            ReportTemplateRowBundle opdServiceCollectionCredit = generatePaymentMethodColumnsByBills(opdBts, creditPaymentMethods);
            opdServiceCollectionCredit.setBundleType("cashierSummaryOpdCredit");
            opdServiceCollectionCredit.setName("OPD Bills - Credit");
            bundle.getBundles().add(opdServiceCollectionCredit);
            collectionForTheDay += getSafeTotal(opdServiceCollectionCredit);

            // Generate OPD service cancellations for credit and add to the main bundle
            ReportTemplateRowBundle opdServiceCancellationsCredit = generatePaymentMethodColumnsByBills(opdCancellations, creditPaymentMethods);
            opdServiceCancellationsCredit.setBundleType("opdServiceCancellationsCredit");
            opdServiceCancellationsCredit.setName("OPD Service Cancellations - Credit");
            bundle.getBundles().add(opdServiceCancellationsCredit);
            collectionForTheDay += getSafeTotal(opdServiceCancellationsCredit);

            // Generate OPD service refunds for credit and add to the main bundle
            ReportTemplateRowBundle opdServiceRefundsCredit = generatePaymentMethodColumnsByBills(opdRefunds, creditPaymentMethods);
            opdServiceRefundsCredit.setBundleType("opdServiceRefundsCredit");
            opdServiceRefundsCredit.setName("OPD Service Refunds - Credit");
            bundle.getBundles().add(opdServiceRefundsCredit);
            collectionForTheDay += getSafeTotal(opdServiceRefundsCredit);

            // Generate Pharmacy Collection and add to the main bundle
            List<BillTypeAtomic> pharmacyCollectionBillTypes = new ArrayList<>();
            pharmacyCollectionBillTypes.add(BillTypeAtomic.PHARMACY_RETAIL_SALE);
            pharmacyCollectionBillTypes.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_PREBILL_SETTLED_AT_CASHIER);
            pharmacyCollectionBillTypes.add(BillTypeAtomic.PHARMACY_WHOLESALE);
            pharmacyCollectionBillTypes.add(BillTypeAtomic.PHARMACY_SALE_WITHOUT_STOCK);
            ReportTemplateRowBundle pharmacyCollection = generateTotalPaymentColumnByDepartment(pharmacyCollectionBillTypes, nonCreditPaymentMethods);
            pharmacyCollection.setBundleType("pharmacyCollection");
            pharmacyCollection.setName("Pharmacy Collection");
            bundle.getBundles().add(pharmacyCollection);
            collectionForTheDay += getSafeTotal(pharmacyCollection);

// Generate Pharmacy service cancellations and add to the main bundle
            List<BillTypeAtomic> pharmacyCancellations = new ArrayList<>();
            pharmacyCancellations.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_CANCELLED);
            pharmacyCancellations.add(BillTypeAtomic.PHARMACY_WHOLESALE_CANCELLED);
            ReportTemplateRowBundle pharmacyServiceCancellations = generatePaymentMethodColumnsByBills(pharmacyCancellations, nonCreditPaymentMethods);
            pharmacyServiceCancellations.setBundleType("pharmacyServiceCancellations");
            pharmacyServiceCancellations.setName("Pharmacy Service Cancellations");
            bundle.getBundles().add(pharmacyServiceCancellations);
            collectionForTheDay += getSafeTotal(pharmacyServiceCancellations);

// Generate Pharmacy service refunds and add to the main bundle
            List<BillTypeAtomic> pharmacyRefunds = new ArrayList<>();
            pharmacyRefunds.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_REFUND);
            pharmacyRefunds.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_RETURN_ITEMS_AND_PAYMENTS);
            pharmacyRefunds.add(BillTypeAtomic.PHARMACY_WHOLESALE_REFUND);

            ReportTemplateRowBundle pharmacyServiceRefunds = generatePaymentMethodColumnsByBills(pharmacyRefunds, nonCreditPaymentMethods);
            pharmacyServiceRefunds.setBundleType("pharmacyServiceRefunds");
            pharmacyServiceRefunds.setName("Pharmacy Service Refunds");
            bundle.getBundles().add(pharmacyServiceRefunds);
            collectionForTheDay += getSafeTotal(pharmacyServiceRefunds);

// Generate Professional Payments OPD and add to the main bundle
            List<BillTypeAtomic> professionalPaymentsOpd = new ArrayList<>();
            professionalPaymentsOpd.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
            professionalPaymentsOpd.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
            ReportTemplateRowBundle professionalPaymentsOpdBundle = generatePaymentMethodColumnsByBills(professionalPaymentsOpd);
            professionalPaymentsOpdBundle.setBundleType("ProfessionalPaymentsOPD");
            professionalPaymentsOpdBundle.setName("Professional Payments OPD");
            bundle.getBundles().add(professionalPaymentsOpdBundle);
            collectionForTheDay += getSafeTotal(professionalPaymentsOpdBundle);

// Generate Professional Payments OPD - Cancel and add to the main bundle
            List<BillTypeAtomic> professionalPaymentsOpdCancel = new ArrayList<>();
            professionalPaymentsOpdCancel.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
            professionalPaymentsOpdCancel.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
            ReportTemplateRowBundle professionalPaymentsOpdCancelBundle = generatePaymentMethodColumnsByBills(professionalPaymentsOpdCancel);
            professionalPaymentsOpdCancelBundle.setBundleType("ProfessionalPaymentsOPDCancel");
            professionalPaymentsOpdCancelBundle.setName("Professional Payments OPD - Cancel");
            bundle.getBundles().add(professionalPaymentsOpdCancelBundle);
            collectionForTheDay += getSafeTotal(professionalPaymentsOpdCancelBundle);

// Generate Professional Payments Inward and add to the main bundle
            List<BillTypeAtomic> professionalPaymentsInward = new ArrayList<>();
            professionalPaymentsInward.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE);
            ReportTemplateRowBundle professionalPaymentsInwardBundle = generatePaymentMethodColumnsByBills(professionalPaymentsInward);
            professionalPaymentsInwardBundle.setBundleType("ProfessionalPaymentsInward");
            professionalPaymentsInwardBundle.setName("Professional Payments Inward");
            bundle.getBundles().add(professionalPaymentsInwardBundle);
            collectionForTheDay += getSafeTotal(professionalPaymentsInwardBundle);

// Generate Professional Payments Inward - Cancel and add to the main bundle
            List<BillTypeAtomic> professionalPaymentsInwardCancel = new ArrayList<>();
            professionalPaymentsInwardCancel.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN);
            ReportTemplateRowBundle professionalPaymentsInwardCancelBundle = generatePaymentMethodColumnsByBills(professionalPaymentsInwardCancel);
            professionalPaymentsInwardCancelBundle.setBundleType("ProfessionalPaymentsInwardCancel");
            professionalPaymentsInwardCancelBundle.setName("Professional Payments Inward - Cancel");
            bundle.getBundles().add(professionalPaymentsInwardCancelBundle);
            collectionForTheDay += getSafeTotal(professionalPaymentsInwardCancelBundle);

// Generate Petty Cash Payment and add to the main bundle
            List<BillTypeAtomic> pettyCashPayment = new ArrayList<>();
            pettyCashPayment.add(BillTypeAtomic.PETTY_CASH_ISSUE);
            ReportTemplateRowBundle pettyCashPaymentBundle = generatePaymentMethodColumnsByBills(pettyCashPayment);
            pettyCashPaymentBundle.setBundleType("PettyCashPayment");
            pettyCashPaymentBundle.setName("Petty Cash Payments");
            bundle.getBundles().add(pettyCashPaymentBundle);
            collectionForTheDay += getSafeTotal(pettyCashPaymentBundle);

// Generate Petty Cash Payment Cancel and add to the main bundle
            List<BillTypeAtomic> pettyCashPaymentCancel = new ArrayList<>();
            pettyCashPaymentCancel.add(BillTypeAtomic.PETTY_CASH_RETURN);
            pettyCashPaymentCancel.add(BillTypeAtomic.PETTY_CASH_BILL_CANCELLATION);
            ReportTemplateRowBundle pettyCashPaymentCancelBundle = generatePaymentMethodColumnsByBills(pettyCashPaymentCancel);
            pettyCashPaymentCancelBundle.setBundleType("PettyCashPaymentCancel");
            pettyCashPaymentCancelBundle.setName("Petty Cash Payment Cancellations");
            bundle.getBundles().add(pettyCashPaymentCancelBundle);
            collectionForTheDay += getSafeTotal(pettyCashPaymentCancelBundle);

// Generate Inward Payments and add to the main bundle
            List<BillTypeAtomic> inwardPayments = new ArrayList<>();
            inwardPayments.add(BillTypeAtomic.INWARD_DEPOSIT);
            ReportTemplateRowBundle inwardPaymentsBundle = generatePaymentMethodColumnsByBills(inwardPayments);
            inwardPaymentsBundle.setBundleType("InwardPayments");
            inwardPaymentsBundle.setName("Inward Payments");
            bundle.getBundles().add(inwardPaymentsBundle);
            collectionForTheDay += getSafeTotal(inwardPaymentsBundle);

// Generate Inward Payments Cancel and add to the main bundle
            List<BillTypeAtomic> inwardPaymentsCancel = new ArrayList<>();
            inwardPaymentsCancel.add(BillTypeAtomic.INWARD_DEPOSIT_CANCELLATION);
            ReportTemplateRowBundle inwardPaymentsCancelBundle = generatePaymentMethodColumnsByBills(inwardPaymentsCancel);
            inwardPaymentsCancelBundle.setBundleType("InwardPaymentsCancel");
            inwardPaymentsCancelBundle.setName("Inward Payment Cancellations");
            bundle.getBundles().add(inwardPaymentsCancelBundle);
            collectionForTheDay += getSafeTotal(inwardPaymentsCancelBundle);

// Generate Inward Payments Refund and add to the main bundle
            List<BillTypeAtomic> inwardPaymentsRefund = new ArrayList<>();
            inwardPaymentsRefund.add(BillTypeAtomic.INWARD_DEPOSIT_REFUND);
            ReportTemplateRowBundle inwardPaymentsRefundBundle = generatePaymentMethodColumnsByBills(inwardPaymentsRefund);
            inwardPaymentsRefundBundle.setBundleType("InwardPaymentsRefund");
            inwardPaymentsRefundBundle.setName("Inward Payment Refunds");
            bundle.getBundles().add(inwardPaymentsRefundBundle);
            collectionForTheDay += getSafeTotal(inwardPaymentsRefundBundle);

// Generate Credit Company Payment OP - Receive and add to the main bundle
            List<BillTypeAtomic> creditCompanyPaymentOpReceive = new ArrayList<>();
            creditCompanyPaymentOpReceive.add(BillTypeAtomic.CREDIT_COMPANY_OPD_PATIENT_PAYMENT);
            ReportTemplateRowBundle creditCompanyPaymentOpReceiveBundle = generatePaymentMethodColumnsByBills(creditCompanyPaymentOpReceive);
            creditCompanyPaymentOpReceiveBundle.setBundleType("CreditCompanyPaymentOPReceive");
            creditCompanyPaymentOpReceiveBundle.setName("Credit Company OP Payment Reception");
            bundle.getBundles().add(creditCompanyPaymentOpReceiveBundle);
            collectionForTheDay += getSafeTotal(creditCompanyPaymentOpReceiveBundle);

// Generate Credit Company Payment OP - Cancel and add to the main bundle
            List<BillTypeAtomic> creditCompanyPaymentOpCancel = new ArrayList<>();
            creditCompanyPaymentOpCancel.add(BillTypeAtomic.CREDIT_COMPANY_OPD_PATIENT_PAYMENT_CANCELLATION);
            creditCompanyPaymentOpCancel.add(BillTypeAtomic.CREDIT_COMPANY_OPD_PATIENT_PAYMENT_REFUND);
            ReportTemplateRowBundle creditCompanyPaymentOpCancelBundle = generatePaymentMethodColumnsByBills(creditCompanyPaymentOpCancel);
            creditCompanyPaymentOpCancelBundle.setBundleType("CreditCompanyPaymentOPCancel");
            creditCompanyPaymentOpCancelBundle.setName("Credit Company OP Payment Cancellations and Refunds");
            bundle.getBundles().add(creditCompanyPaymentOpCancelBundle);
            collectionForTheDay += getSafeTotal(creditCompanyPaymentOpCancelBundle);

// Generate Credit Company Payment IP - Receive and add to the main bundle
            List<BillTypeAtomic> creditCompanyPaymentIpReceive = new ArrayList<>();
            creditCompanyPaymentIpReceive.add(BillTypeAtomic.CREDIT_COMPANY_INPATIENT_PAYMENT);
            creditCompanyPaymentIpReceive.add(BillTypeAtomic.CREDIT_COMPANY_OPD_PATIENT_PAYMENT);
            ReportTemplateRowBundle creditCompanyPaymentIpReceiveBundle = generatePaymentMethodColumnsByBills(creditCompanyPaymentIpReceive);
            creditCompanyPaymentIpReceiveBundle.setBundleType("CreditCompanyPaymentIPReceive");
            creditCompanyPaymentIpReceiveBundle.setName("Credit Company IP Payment Reception");
            bundle.getBundles().add(creditCompanyPaymentIpReceiveBundle);
            collectionForTheDay += getSafeTotal(creditCompanyPaymentIpReceiveBundle);

// Generate Credit Company Payment IP - Cancellation and Refunds and add to the main bundle
            List<BillTypeAtomic> creditCompanyPaymentIpCancellation = new ArrayList<>();
            creditCompanyPaymentIpCancellation.add(BillTypeAtomic.CREDIT_COMPANY_INPATIENT_PAYMENT_CANCELLATION);
            creditCompanyPaymentIpCancellation.add(BillTypeAtomic.CREDIT_COMPANY_INPATIENT_PAYMENT_REFUND);
            ReportTemplateRowBundle creditCompanyPaymentIpCancellationBundle = generatePaymentMethodColumnsByBills(creditCompanyPaymentIpCancellation);
            creditCompanyPaymentIpCancellationBundle.setBundleType("CreditCompanyPaymentIPCancellation");
            creditCompanyPaymentIpCancellationBundle.setName("Credit Company IP Payment Cancellations and Refunds");
            bundle.getBundles().add(creditCompanyPaymentIpCancellationBundle);
            collectionForTheDay += getSafeTotal(creditCompanyPaymentIpCancellationBundle);

// Generate Patient Deposit and add to the main bundle
            List<BillTypeAtomic> patientDeposit = new ArrayList<>();
            patientDeposit.add(BillTypeAtomic.PATIENT_DEPOSIT);
            ReportTemplateRowBundle patientDepositBundle = generatePaymentMethodColumnsByBills(patientDeposit);
            patientDepositBundle.setBundleType("PatientDeposit");
            patientDepositBundle.setName("Patient Deposits");
            bundle.getBundles().add(patientDepositBundle);

            collectionForTheDay += getSafeTotal(patientDepositBundle);

// Generate Patient Deposit Cancellation and add to the main bundle
            List<BillTypeAtomic> patientDepositCancel = new ArrayList<>();
            patientDepositCancel.add(BillTypeAtomic.PATIENT_DEPOSIT_CANCELLED);
            ReportTemplateRowBundle patientDepositCancelBundle = generatePaymentMethodColumnsByBills(patientDepositCancel);
            patientDepositCancelBundle.setBundleType("PatientDepositCancel");
            patientDepositCancelBundle.setName("Patient Deposit Cancellations");
            bundle.getBundles().add(patientDepositCancelBundle);
            collectionForTheDay += getSafeTotal(patientDepositCancelBundle);

// Generate Patient Deposit Refund and add to the main bundle
            List<BillTypeAtomic> patientDepositRefund = new ArrayList<>();
            patientDepositRefund.add(BillTypeAtomic.PATIENT_DEPOSIT_REFUND);
            ReportTemplateRowBundle patientDepositRefundBundle = generatePaymentMethodColumnsByBills(patientDepositRefund);
            patientDepositRefundBundle.setBundleType("PatientDepositRefund");
            patientDepositRefundBundle.setName("Patient Deposit Refunds");
            bundle.getBundles().add(patientDepositRefundBundle);
            collectionForTheDay += getSafeTotal(patientDepositRefundBundle);

// Generate Collecting Centre Payment Receive and add to the main bundle
            List<BillTypeAtomic> collectingCentrePaymentReceive = new ArrayList<>();
            collectingCentrePaymentReceive.add(BillTypeAtomic.CC_PAYMENT_RECEIVED_BILL);
            ReportTemplateRowBundle collectingCentrePaymentReceiveBundle = generatePaymentMethodColumnsByBills(collectingCentrePaymentReceive);
            collectingCentrePaymentReceiveBundle.setBundleType("CollectingCentrePaymentReceive");
            collectingCentrePaymentReceiveBundle.setName("Collecting Centre Payment Receives");
            bundle.getBundles().add(collectingCentrePaymentReceiveBundle);
            collectionForTheDay += getSafeTotal(collectingCentrePaymentReceiveBundle);

// Generate Collecting Centre Payment Cancel and add to the main bundle
            List<BillTypeAtomic> collectingCentrePaymentCancel = new ArrayList<>();
            collectingCentrePaymentCancel.add(BillTypeAtomic.CC_PAYMENT_CANCELLATION_BILL);
            ReportTemplateRowBundle collectingCentrePaymentCancelBundle = generatePaymentMethodColumnsByBills(collectingCentrePaymentCancel);
            collectingCentrePaymentCancelBundle.setBundleType("CollectingCentrePaymentCancel");
            collectingCentrePaymentCancelBundle.setName("Collecting Centre Payment Cancellations");
            bundle.getBundles().add(collectingCentrePaymentCancelBundle);
            collectionForTheDay += getSafeTotal(collectingCentrePaymentCancelBundle);

// Generate OPD Credit, Cancellation, and Refund and add to the main bundle
            List<BillTypeAtomic> opdCredit = new ArrayList<>();
            opdCredit.add(BillTypeAtomic.OPD_CREDIT_COMPANY_PAYMENT_RECEIVED);
            ReportTemplateRowBundle opdCreditBundle = generatePaymentMethodColumnsByBills(opdCredit);
            opdCreditBundle.setBundleType("OpdCredit");
            opdCreditBundle.setName("OPD Credit Payments");
            bundle.getBundles().add(opdCreditBundle);
            collectionForTheDay += getSafeTotal(opdCreditBundle);

            List<BillTypeAtomic> opdCreditCancel = new ArrayList<>();
            opdCreditCancel.add(BillTypeAtomic.OPD_CREDIT_COMPANY_PAYMENT_CANCELLATION);
            ReportTemplateRowBundle opdCreditCancelBundle = generatePaymentMethodColumnsByBills(opdCreditCancel);
            opdCreditCancelBundle.setBundleType("OpdCreditCancelled");
            opdCreditCancelBundle.setName("OPD Credit Cancellations");
            bundle.getBundles().add(opdCreditCancelBundle);
            collectionForTheDay += getSafeTotal(opdCreditCancelBundle);

            List<BillTypeAtomic> opdCreditRefund = new ArrayList<>();
            opdCreditRefund.add(BillTypeAtomic.OPD_CREDIT_COMPANY_CREDIT_NOTE);
            ReportTemplateRowBundle opdCreditRefundBundle = generatePaymentMethodColumnsByBills(opdCreditRefund);
            opdCreditRefundBundle.setBundleType("OpdCreditRefund");
            opdCreditRefundBundle.setName("OPD Credit Refunds");
            bundle.getBundles().add(opdCreditRefundBundle);
            collectionForTheDay += getSafeTotal(opdCreditRefundBundle);

// Generate Pharmacy Credit Bills, Cancellation, and Refund and add to the main bundle
            List<BillTypeAtomic> pharmacyCreditBills = new ArrayList<>();
            pharmacyCreditBills.add(BillTypeAtomic.PHARMACY_CREDIT_COMPANY_PAYMENT_RECEIVED);
            ReportTemplateRowBundle pharmacyCreditBillsBundle = generatePaymentMethodColumnsByBills(pharmacyCreditBills);
            pharmacyCreditBillsBundle.setBundleType("PharmacyCreditBills");
            pharmacyCreditBillsBundle.setName("Pharmacy Credit Bills");
            bundle.getBundles().add(pharmacyCreditBillsBundle);
            collectionForTheDay += getSafeTotal(pharmacyCreditBillsBundle);

            List<BillTypeAtomic> pharmacyCreditCancel = new ArrayList<>();
            pharmacyCreditCancel.add(BillTypeAtomic.PHARMACY_CREDIT_COMPANY_PAYMENT_CANCELLATION);
            ReportTemplateRowBundle pharmacyCreditCancelBundle = generatePaymentMethodColumnsByBills(pharmacyCreditCancel);
            pharmacyCreditCancelBundle.setBundleType("PharmacyCreditCancel");
            pharmacyCreditCancelBundle.setName("Pharmacy Credit Cancellations");
            bundle.getBundles().add(pharmacyCreditCancelBundle);
            collectionForTheDay += getSafeTotal(pharmacyCreditCancelBundle);

            List<BillTypeAtomic> pharmacyCreditRefund = new ArrayList<>();
            pharmacyCreditRefund.add(BillTypeAtomic.PHARMACY_CREDIT_COMPANY_CREDIT_NOTE);
            ReportTemplateRowBundle pharmacyCreditRefundBundle = generatePaymentMethodColumnsByBills(pharmacyCreditRefund);
            pharmacyCreditRefundBundle.setBundleType("PharmacyCreditRefund");
            pharmacyCreditRefundBundle.setName("Pharmacy Credit Refunds");
            bundle.getBundles().add(pharmacyCreditRefundBundle);
            collectionForTheDay += getSafeTotal(pharmacyCreditRefundBundle);

            // Final net cash for the day
            ReportTemplateRowBundle netCashForTheDayBundle = new ReportTemplateRowBundle();
            netCashForTheDayBundle.setName("Net Cash");
            netCashForTheDayBundle.setBundleType("netCash");
            netCashForTheDayBundle.setTotal(netCashCollection);

            bundle.getBundles().add(netCashForTheDayBundle);
            bundle.calculateTotalsByAllChildBundles();
        }, CashierReports.CASHIER_SUMMARY, sessionController.getLoggedUser());
    }

    public void listAllDrawers() {
        String jpql;
        Map<String, Object> params = new HashMap<>();
        jpql = "select d from Drawer d "
                + " where d.retired=:ret"
                + " order by d.drawerUser.name ";
        params.put("ret", false);
        drawerList = drawerFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    public void generateMyCashierDetailed() {
        webUser = sessionController.getLoggedUser();
        institution = null;
        department = null;
        site = null;
        paymentMethod = null;

        generateCashierDetailed();
    }

    public void generateCashierDetailed() {
        reportTimerController.trackReportExecution(() -> {
            bundle = new ReportTemplateRowBundle();
            bundle.setName("Cashier Detailed");

            double collectionForTheDay = 0.0;
            double netCashCollection = 0.0;

            List<PaymentMethod> creditPaymentMethods = PaymentMethod.getMethodsByType(PaymentType.CREDIT);
            List<PaymentMethod> nonCreditPaymentMethods = PaymentMethod.getMethodsByType(PaymentType.NON_CREDIT);

            List<BillTypeAtomic> opdBts = new ArrayList<>();
            opdBts.add(BillTypeAtomic.OPD_BATCH_BILL_WITH_PAYMENT);
            opdBts.add(BillTypeAtomic.OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER);
            opdBts.add(BillTypeAtomic.PACKAGE_OPD_BATCH_BILL_WITH_PAYMENT);
            opdBts.add(BillTypeAtomic.PACKAGE_OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER);

            List<BillTypeAtomic> opdCancellations = new ArrayList<>();
            opdCancellations.add(BillTypeAtomic.OPD_BATCH_BILL_CANCELLATION);
            opdCancellations.add(BillTypeAtomic.OPD_BILL_CANCELLATION);
            opdCancellations.add(BillTypeAtomic.PACKAGE_OPD_BATCH_BILL_CANCELLATION);
            opdCancellations.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION);

            List<BillTypeAtomic> opdRefunds = new ArrayList<>();
            opdRefunds.add(BillTypeAtomic.OPD_BILL_REFUND);

            ReportTemplateRowBundle opdServiceBilled = generatePaymentMethodColumnsByBills(opdBts, nonCreditPaymentMethods);
            opdServiceBilled.setBundleType("opdServiceBilled");
            opdServiceBilled.setName("OPD Bills");
            bundle.getBundles().add(opdServiceBilled);
            collectionForTheDay += getSafeTotal(opdServiceBilled);

            // Generate OPD service collection and add to the main bundle
            ReportTemplateRowBundle opdServiceCancellations = generatePaymentMethodColumnsByBills(opdCancellations, nonCreditPaymentMethods);
            opdServiceCancellations.setBundleType("opdServiceCancellations");
            opdServiceCancellations.setName("OPD Service Cancellations");
            bundle.getBundles().add(opdServiceCancellations);
            collectionForTheDay += getSafeTotal(opdServiceCancellations);

            // Generate OPD service Refunds and add to the main bundle
            ReportTemplateRowBundle opdServiceRefunds = generatePaymentMethodColumnsByBills(opdRefunds, nonCreditPaymentMethods);
            opdServiceRefunds.setBundleType("opdServiceRefunds");
            opdServiceRefunds.setName("OPD Service Refunds");
            bundle.getBundles().add(opdServiceRefunds);
            collectionForTheDay += getSafeTotal(opdServiceRefunds);

            // Generate OPD service collection for credit and add to the main bundle
            ReportTemplateRowBundle opdServiceCollectionCredit = generatePaymentMethodColumnsByBills(opdBts, creditPaymentMethods);
            opdServiceCollectionCredit.setBundleType("cashierSummaryOpdCredit");
            opdServiceCollectionCredit.setName("OPD Bills - Credit");
            bundle.getBundles().add(opdServiceCollectionCredit);
            collectionForTheDay += getSafeTotal(opdServiceCollectionCredit);

            // Generate OPD service cancellations for credit and add to the main bundle
            ReportTemplateRowBundle opdServiceCancellationsCredit = generatePaymentMethodColumnsByBills(opdCancellations, creditPaymentMethods);
            opdServiceCancellationsCredit.setBundleType("opdServiceCancellationsCredit");
            opdServiceCancellationsCredit.setName("OPD Service Cancellations - Credit");
            bundle.getBundles().add(opdServiceCancellationsCredit);
            collectionForTheDay += getSafeTotal(opdServiceCancellationsCredit);

            // Generate OPD service refunds for credit and add to the main bundle
            ReportTemplateRowBundle opdServiceRefundsCredit = generatePaymentMethodColumnsByBills(opdRefunds, creditPaymentMethods);
            opdServiceRefundsCredit.setBundleType("opdServiceRefundsCredit");
            opdServiceRefundsCredit.setName("OPD Service Refunds - Credit");
            bundle.getBundles().add(opdServiceRefundsCredit);
            collectionForTheDay += getSafeTotal(opdServiceRefundsCredit);

            // Generate Pharmacy Bills and add to the main bundle
            List<BillTypeAtomic> pharmacyCollectionBillTypes = new ArrayList<>();
            pharmacyCollectionBillTypes.add(BillTypeAtomic.PHARMACY_RETAIL_SALE);
            pharmacyCollectionBillTypes.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_PREBILL_SETTLED_AT_CASHIER);
            pharmacyCollectionBillTypes.add(BillTypeAtomic.PHARMACY_WHOLESALE);
            ReportTemplateRowBundle pharmacyCollection = generatePaymentMethodColumnsByBills(pharmacyCollectionBillTypes, nonCreditPaymentMethods);
            pharmacyCollection.setBundleType("pharmacyNonCreditBills");
            pharmacyCollection.setName("Pharmacy Bills (Non Credit)");
            bundle.getBundles().add(pharmacyCollection);
            collectionForTheDay += getSafeTotal(pharmacyCollection);

// Generate Pharmacy service cancellations and add to the main bundle
            List<BillTypeAtomic> pharmacyCancellations = new ArrayList<>();
            pharmacyCancellations.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_CANCELLED);
            pharmacyCancellations.add(BillTypeAtomic.PHARMACY_WHOLESALE_CANCELLED);
            ReportTemplateRowBundle pharmacyServiceCancellations = generatePaymentMethodColumnsByBills(pharmacyCancellations);
            pharmacyServiceCancellations.setBundleType("pharmacyServiceCancellations");
            pharmacyServiceCancellations.setName("Pharmacy Service Cancellations");
            bundle.getBundles().add(pharmacyServiceCancellations);
            collectionForTheDay += getSafeTotal(pharmacyServiceCancellations);

// Generate Pharmacy service refunds and add to the main bundle
            List<BillTypeAtomic> pharmacyRefunds = new ArrayList<>();
            pharmacyRefunds.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_REFUND);
            pharmacyRefunds.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_RETURN_ITEMS_AND_PAYMENTS);
            pharmacyRefunds.add(BillTypeAtomic.PHARMACY_WHOLESALE_REFUND);
            ReportTemplateRowBundle pharmacyServiceRefunds = generatePaymentMethodColumnsByBills(pharmacyRefunds);
            pharmacyServiceRefunds.setBundleType("pharmacyServiceRefunds");
            pharmacyServiceRefunds.setName("Pharmacy Service Refunds");
            bundle.getBundles().add(pharmacyServiceRefunds);
            collectionForTheDay += getSafeTotal(pharmacyServiceRefunds);

// Generate Professional Payments OPD and add to the main bundle
            List<BillTypeAtomic> professionalPaymentsOpd = new ArrayList<>();
            professionalPaymentsOpd.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
            professionalPaymentsOpd.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL);
            ReportTemplateRowBundle professionalPaymentsOpdBundle = generatePaymentMethodColumnsByBills(professionalPaymentsOpd);
            professionalPaymentsOpdBundle.setBundleType("ProfessionalPaymentsOPD");
            professionalPaymentsOpdBundle.setName("Professional Payments OPD");
            bundle.getBundles().add(professionalPaymentsOpdBundle);
            collectionForTheDay += getSafeTotal(professionalPaymentsOpdBundle);

// Generate Professional Payments OPD - Cancel and add to the main bundle
            List<BillTypeAtomic> professionalPaymentsOpdCancel = new ArrayList<>();
            professionalPaymentsOpdCancel.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);
            professionalPaymentsOpdCancel.add(BillTypeAtomic.OPD_PROFESSIONAL_PAYMENT_BILL_RETURN);
            ReportTemplateRowBundle professionalPaymentsOpdCancelBundle = generatePaymentMethodColumnsByBills(professionalPaymentsOpdCancel);
            professionalPaymentsOpdCancelBundle.setBundleType("ProfessionalPaymentsOPDCancel");
            professionalPaymentsOpdCancelBundle.setName("Professional Payments OPD - Cancel");
            bundle.getBundles().add(professionalPaymentsOpdCancelBundle);
            collectionForTheDay += getSafeTotal(professionalPaymentsOpdCancelBundle);

// Generate Professional Payments Inward and add to the main bundle
            List<BillTypeAtomic> professionalPaymentsInward = new ArrayList<>();
            professionalPaymentsInward.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE);
            ReportTemplateRowBundle professionalPaymentsInwardBundle = generatePaymentMethodColumnsByBills(professionalPaymentsInward);
            professionalPaymentsInwardBundle.setBundleType("ProfessionalPaymentsInward");
            professionalPaymentsInwardBundle.setName("Professional Payments Inward");
            bundle.getBundles().add(professionalPaymentsInwardBundle);
            collectionForTheDay += getSafeTotal(professionalPaymentsInwardBundle);

// Generate Professional Payments Inward - Cancel and add to the main bundle
            List<BillTypeAtomic> professionalPaymentsInwardCancel = new ArrayList<>();
            professionalPaymentsInwardCancel.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN);
            ReportTemplateRowBundle professionalPaymentsInwardCancelBundle = generatePaymentMethodColumnsByBills(professionalPaymentsInwardCancel);
            professionalPaymentsInwardCancelBundle.setBundleType("ProfessionalPaymentsInwardCancel");
            professionalPaymentsInwardCancelBundle.setName("Professional Payments Inward - Cancel");
            bundle.getBundles().add(professionalPaymentsInwardCancelBundle);
            collectionForTheDay += getSafeTotal(professionalPaymentsInwardCancelBundle);

// Generate Petty Cash Payment and add to the main bundle
            List<BillTypeAtomic> pettyCashPayment = new ArrayList<>();
            pettyCashPayment.add(BillTypeAtomic.PETTY_CASH_ISSUE);
            ReportTemplateRowBundle pettyCashPaymentBundle = generatePaymentMethodColumnsByBills(pettyCashPayment);
            pettyCashPaymentBundle.setBundleType("PettyCashPayment");
            pettyCashPaymentBundle.setName("Petty Cash Payments");
            bundle.getBundles().add(pettyCashPaymentBundle);
            collectionForTheDay += getSafeTotal(pettyCashPaymentBundle);

// Generate Petty Cash Payment Cancel and add to the main bundle
            List<BillTypeAtomic> pettyCashPaymentCancel = new ArrayList<>();
            pettyCashPaymentCancel.add(BillTypeAtomic.PETTY_CASH_RETURN);
            pettyCashPaymentCancel.add(BillTypeAtomic.PETTY_CASH_BILL_CANCELLATION);
            ReportTemplateRowBundle pettyCashPaymentCancelBundle = generatePaymentMethodColumnsByBills(pettyCashPaymentCancel);
            pettyCashPaymentCancelBundle.setBundleType("PettyCashPaymentCancel");
            pettyCashPaymentCancelBundle.setName("Petty Cash Payment Cancellations");
            bundle.getBundles().add(pettyCashPaymentCancelBundle);
            collectionForTheDay += getSafeTotal(pettyCashPaymentCancelBundle);

// Generate Inward Payments and add to the main bundle
            List<BillTypeAtomic> inwardPayments = new ArrayList<>();
            inwardPayments.add(BillTypeAtomic.INWARD_DEPOSIT);
            ReportTemplateRowBundle inwardPaymentsBundle = generatePaymentMethodColumnsByBills(inwardPayments);
            inwardPaymentsBundle.setBundleType("InwardPayments");
            inwardPaymentsBundle.setName("Inward Payments");
            bundle.getBundles().add(inwardPaymentsBundle);
            collectionForTheDay += getSafeTotal(inwardPaymentsBundle);

// Generate Inward Payments Cancel and add to the main bundle
            List<BillTypeAtomic> inwardPaymentsCancel = new ArrayList<>();
            inwardPaymentsCancel.add(BillTypeAtomic.INWARD_DEPOSIT_CANCELLATION);
            ReportTemplateRowBundle inwardPaymentsCancelBundle = generatePaymentMethodColumnsByBills(inwardPaymentsCancel);
            inwardPaymentsCancelBundle.setBundleType("InwardPaymentsCancel");
            inwardPaymentsCancelBundle.setName("Inward Payment Cancellations");
            bundle.getBundles().add(inwardPaymentsCancelBundle);
            collectionForTheDay += getSafeTotal(inwardPaymentsCancelBundle);

// Generate Inward Payments Refund and add to the main bundle
            List<BillTypeAtomic> inwardPaymentsRefund = new ArrayList<>();
            inwardPaymentsRefund.add(BillTypeAtomic.INWARD_DEPOSIT_REFUND);
            ReportTemplateRowBundle inwardPaymentsRefundBundle = generatePaymentMethodColumnsByBills(inwardPaymentsRefund);
            inwardPaymentsRefundBundle.setBundleType("InwardPaymentsRefund");
            inwardPaymentsRefundBundle.setName("Inward Payment Refunds");
            bundle.getBundles().add(inwardPaymentsRefundBundle);
            collectionForTheDay += getSafeTotal(inwardPaymentsRefundBundle);

// Generate Credit Company Payment OP - Receive and add to the main bundle
            List<BillTypeAtomic> creditCompanyPaymentOpReceive = new ArrayList<>();
            creditCompanyPaymentOpReceive.add(BillTypeAtomic.CREDIT_COMPANY_OPD_PATIENT_PAYMENT);
            creditCompanyPaymentOpReceive.add(BillTypeAtomic.OPD_CREDIT_COMPANY_PAYMENT_RECEIVED);
            ReportTemplateRowBundle creditCompanyPaymentOpReceiveBundle = generatePaymentMethodColumnsByBills(creditCompanyPaymentOpReceive);
            creditCompanyPaymentOpReceiveBundle.setBundleType("CreditCompanyPaymentOPReceive");
            creditCompanyPaymentOpReceiveBundle.setName("Credit Company OP Payment Reception");
            bundle.getBundles().add(creditCompanyPaymentOpReceiveBundle);
            collectionForTheDay += getSafeTotal(creditCompanyPaymentOpReceiveBundle);

// Generate Credit Company Payment OP - Cancel and add to the main bundle
            List<BillTypeAtomic> creditCompanyPaymentOpCancel = new ArrayList<>();
            creditCompanyPaymentOpCancel.add(BillTypeAtomic.CREDIT_COMPANY_OPD_PATIENT_PAYMENT_CANCELLATION);
            creditCompanyPaymentOpCancel.add(BillTypeAtomic.CREDIT_COMPANY_OPD_PATIENT_PAYMENT_REFUND);
            creditCompanyPaymentOpCancel.add(BillTypeAtomic.OPD_CREDIT_COMPANY_PAYMENT_CANCELLATION);

            ReportTemplateRowBundle creditCompanyPaymentOpCancelBundle = generatePaymentMethodColumnsByBills(creditCompanyPaymentOpCancel);
            creditCompanyPaymentOpCancelBundle.setBundleType("CreditCompanyPaymentOPCancel");
            creditCompanyPaymentOpCancelBundle.setName("Credit Company OP Payment Cancellations and Refunds");
            bundle.getBundles().add(creditCompanyPaymentOpCancelBundle);
            collectionForTheDay += getSafeTotal(creditCompanyPaymentOpCancelBundle);

// Generate Credit Company Payment IP - Receive and add to the main bundle
            List<BillTypeAtomic> creditCompanyPaymentIpReceive = new ArrayList<>();
            creditCompanyPaymentIpReceive.add(BillTypeAtomic.CREDIT_COMPANY_INPATIENT_PAYMENT);
            creditCompanyPaymentIpReceive.add(BillTypeAtomic.CREDIT_COMPANY_OPD_PATIENT_PAYMENT);
            creditCompanyPaymentIpReceive.add(BillTypeAtomic.INPATIENT_CREDIT_COMPANY_PAYMENT_RECEIVED);
            ReportTemplateRowBundle creditCompanyPaymentIpReceiveBundle = generatePaymentMethodColumnsByBills(creditCompanyPaymentIpReceive);
            creditCompanyPaymentIpReceiveBundle.setBundleType("CreditCompanyPaymentIPReceive");
            creditCompanyPaymentIpReceiveBundle.setName("Credit Company IP Payment Reception");
            bundle.getBundles().add(creditCompanyPaymentIpReceiveBundle);
            collectionForTheDay += getSafeTotal(creditCompanyPaymentIpReceiveBundle);

// Generate Credit Company Payment IP - Cancellation and Refunds and add to the main bundle
            List<BillTypeAtomic> creditCompanyPaymentIpCancellation = new ArrayList<>();
            creditCompanyPaymentIpCancellation.add(BillTypeAtomic.CREDIT_COMPANY_INPATIENT_PAYMENT_CANCELLATION);
            creditCompanyPaymentIpCancellation.add(BillTypeAtomic.INPATIENT_CREDIT_COMPANY_PAYMENT_CANCELLATION);
            creditCompanyPaymentIpCancellation.add(BillTypeAtomic.CREDIT_COMPANY_INPATIENT_PAYMENT_REFUND);
            ReportTemplateRowBundle creditCompanyPaymentIpCancellationBundle = generatePaymentMethodColumnsByBills(creditCompanyPaymentIpCancellation);
            creditCompanyPaymentIpCancellationBundle.setBundleType("CreditCompanyPaymentIPCancellation");
            creditCompanyPaymentIpCancellationBundle.setName("Credit Company IP Payment Cancellations and Refunds");
            bundle.getBundles().add(creditCompanyPaymentIpCancellationBundle);
            collectionForTheDay += getSafeTotal(creditCompanyPaymentIpCancellationBundle);

// Generate Patient Deposit and add to the main bundle
            List<BillTypeAtomic> patientDeposit = new ArrayList<>();
            patientDeposit.add(BillTypeAtomic.PATIENT_DEPOSIT);
            ReportTemplateRowBundle patientDepositBundle = generatePaymentMethodColumnsByBills(patientDeposit);
            patientDepositBundle.setBundleType("PatientDeposit");
            patientDepositBundle.setName("Patient Deposits");
            bundle.getBundles().add(patientDepositBundle);
            collectionForTheDay += getSafeTotal(patientDepositBundle);

// Generate Patient Deposit Cancellation and add to the main bundle
            List<BillTypeAtomic> patientDepositCancel = new ArrayList<>();
            patientDepositCancel.add(BillTypeAtomic.PATIENT_DEPOSIT_CANCELLED);
            ReportTemplateRowBundle patientDepositCancelBundle = generatePaymentMethodColumnsByBills(patientDepositCancel);
            patientDepositCancelBundle.setBundleType("PatientDepositCancel");
            patientDepositCancelBundle.setName("Patient Deposit Cancellations");
            bundle.getBundles().add(patientDepositCancelBundle);
            collectionForTheDay += getSafeTotal(patientDepositCancelBundle);

// Generate Patient Deposit Refund and add to the main bundle
            List<BillTypeAtomic> patientDepositRefund = new ArrayList<>();
            patientDepositRefund.add(BillTypeAtomic.PATIENT_DEPOSIT_REFUND);
            ReportTemplateRowBundle patientDepositRefundBundle = generatePaymentMethodColumnsByBills(patientDepositRefund);
            patientDepositRefundBundle.setBundleType("PatientDepositRefund");
            patientDepositRefundBundle.setName("Patient Deposit Refunds");
            bundle.getBundles().add(patientDepositRefundBundle);
            collectionForTheDay += getSafeTotal(patientDepositRefundBundle);

// Generate Collecting Centre Payment Receive and add to the main bundle
            List<BillTypeAtomic> collectingCentrePaymentReceive = new ArrayList<>();
            collectingCentrePaymentReceive.add(BillTypeAtomic.CC_PAYMENT_RECEIVED_BILL);
            ReportTemplateRowBundle collectingCentrePaymentReceiveBundle = generatePaymentMethodColumnsByBills(collectingCentrePaymentReceive);
            collectingCentrePaymentReceiveBundle.setBundleType("CollectingCentrePaymentReceive");
            collectingCentrePaymentReceiveBundle.setName("Collecting Centre Payment Receives");
            bundle.getBundles().add(collectingCentrePaymentReceiveBundle);
            collectionForTheDay += getSafeTotal(collectingCentrePaymentReceiveBundle);

// Generate Collecting Centre Payment Cancel and add to the main bundle
            List<BillTypeAtomic> collectingCentrePaymentCancel = new ArrayList<>();
            collectingCentrePaymentCancel.add(BillTypeAtomic.CC_PAYMENT_CANCELLATION_BILL);
            ReportTemplateRowBundle collectingCentrePaymentCancelBundle = generatePaymentMethodColumnsByBills(collectingCentrePaymentCancel);
            collectingCentrePaymentCancelBundle.setBundleType("CollectingCentrePaymentCancel");
            collectingCentrePaymentCancelBundle.setName("Collecting Centre Payment Cancellations");
            bundle.getBundles().add(collectingCentrePaymentCancelBundle);
            collectionForTheDay += getSafeTotal(collectingCentrePaymentCancelBundle);

//// Generate OPD Credit, Cancellation, and Refund and add to the main bundle
//        List<BillTypeAtomic> opdCreditBills = new ArrayList<>();
//        opdCreditBills.add(BillTypeAtomic.OPD_BATCH_BILL_WITH_PAYMENT);
//        opdCreditBills.add(BillTypeAtomic.OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER);
//        opdCreditBills.add(BillTypeAtomic.PACKAGE_OPD_BATCH_BILL_WITH_PAYMENT);
//        opdCreditBills.add(BillTypeAtomic.PACKAGE_OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER);
//        ReportTemplateRowBundle opdCreditBillBundle = generatePaymentMethodColumnsByBills(opdCreditBills, creditPaymentMethods);
//        opdCreditBillBundle.setBundleType("OpdCredit");
//        opdCreditBillBundle.setName("OPD Credit Payments");
//        bundle.getBundles().add(opdCreditBillBundle);
//        collectionForTheDay += getSafeTotal(opdCreditBillBundle);
//
//        List<BillTypeAtomic> opdCreditBillCancel = new ArrayList<>();
//        opdCreditBillCancel.add(BillTypeAtomic.OPD_BILL_CANCELLATION);
//        opdCreditBillCancel.add(BillTypeAtomic.OPD_BATCH_BILL_CANCELLATION);
//        opdCreditBillCancel.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION);
//        opdCreditBillCancel.add(BillTypeAtomic.PACKAGE_OPD_BATCH_BILL_CANCELLATION);
//        ReportTemplateRowBundle opdCreditCancelBundle = generatePaymentMethodColumnsByBills(opdCreditBillCancel, creditPaymentMethods);
//        opdCreditCancelBundle.setBundleType("OpdCreditCancelled");
//        opdCreditCancelBundle.setName("OPD Credit Cancellations");
//        bundle.getBundles().add(opdCreditCancelBundle);
//        collectionForTheDay += getSafeTotal(opdCreditCancelBundle);
//
//        List<BillTypeAtomic> opdCreditRefund = new ArrayList<>();
//        opdCreditRefund.add(BillTypeAtomic.OPD_BILL_REFUND);
//        ReportTemplateRowBundle opdCreditRefundBundle = generatePaymentMethodColumnsByBills(opdCreditRefund, creditPaymentMethods);
//        opdCreditRefundBundle.setBundleType("OpdCreditRefund");
//        opdCreditRefundBundle.setName("OPD Credit Refunds");
//        bundle.getBundles().add(opdCreditRefundBundle);
//        collectionForTheDay += getSafeTotal(opdCreditRefundBundle);
// Generate Pharmacy Credit Bills, Cancellation, and Refund and add to the main bundle
            List<BillTypeAtomic> pharmacyCreditBills = new ArrayList<>();
            pharmacyCreditBills.add(BillTypeAtomic.PHARMACY_CREDIT_COMPANY_PAYMENT_RECEIVED);
            ReportTemplateRowBundle pharmacyCreditBillsBundle = generatePaymentMethodColumnsByBills(pharmacyCreditBills);
            pharmacyCreditBillsBundle.setBundleType("PharmacyCreditBills");
            pharmacyCreditBillsBundle.setName("Pharmacy Credit Bills");
            bundle.getBundles().add(pharmacyCreditBillsBundle);
            collectionForTheDay += getSafeTotal(pharmacyCreditBillsBundle);

            List<BillTypeAtomic> pharmacyCreditCancel = new ArrayList<>();
            pharmacyCreditCancel.add(BillTypeAtomic.PHARMACY_CREDIT_COMPANY_PAYMENT_CANCELLATION);
            ReportTemplateRowBundle pharmacyCreditCancelBundle = generatePaymentMethodColumnsByBills(pharmacyCreditCancel);
            pharmacyCreditCancelBundle.setBundleType("PharmacyCreditCancel");
            pharmacyCreditCancelBundle.setName("Pharmacy Credit Cancellations");
            bundle.getBundles().add(pharmacyCreditCancelBundle);
            collectionForTheDay += getSafeTotal(pharmacyCreditCancelBundle);

            List<BillTypeAtomic> pharmacyCreditRefund = new ArrayList<>();
            pharmacyCreditRefund.add(BillTypeAtomic.PHARMACY_CREDIT_COMPANY_CREDIT_NOTE);
            ReportTemplateRowBundle pharmacyCreditRefundBundle = generatePaymentMethodColumnsByBills(pharmacyCreditRefund);
            pharmacyCreditRefundBundle.setBundleType("PharmacyCreditRefund");
            pharmacyCreditRefundBundle.setName("Pharmacy Credit Refunds");
            bundle.getBundles().add(pharmacyCreditRefundBundle);
            collectionForTheDay += getSafeTotal(pharmacyCreditRefundBundle);

            //Genarate Agency accept
            List<BillTypeAtomic> agencyDeposit = new ArrayList<>();
            agencyDeposit.add(BillTypeAtomic.AGENCY_PAYMENT_RECEIVED);
            ReportTemplateRowBundle agencyPaymentBundle = generatePaymentMethodColumnsByBills(agencyDeposit);
            agencyPaymentBundle.setBundleType("AgencyDeposit");
            agencyPaymentBundle.setName("Agency Accept Payments");
            bundle.getBundles().add(agencyPaymentBundle);
            collectionForTheDay += getSafeTotal(agencyPaymentBundle);

            //Genarate Channel Appointment Paid
            List<BillTypeAtomic> channeling = new ArrayList<>();
            channeling.add(BillTypeAtomic.CHANNEL_BOOKING_WITH_PAYMENT);
            channeling.add(BillTypeAtomic.CHANNEL_PAYMENT_FOR_BOOKING_BILL);
            channeling.add(BillTypeAtomic.CHANNEL_BOOKING_FOR_PAYMENT_ONLINE_COMPLETED_PAYMENT);
            channeling.add(BillTypeAtomic.CHANNEL_BOOKING_WITH_PAYMENT_ONLINE);

            ReportTemplateRowBundle channelAppointmentBundle = generatePaymentMethodColumnsByBills(channeling);
            channelAppointmentBundle.setBundleType("ChannelBookingsWithPayment");
            channelAppointmentBundle.setName("Channel Paid Bookings");
            bundle.getBundles().add(channelAppointmentBundle);
            collectionForTheDay += getSafeTotal(channelAppointmentBundle);

            //Genarate Channel Cancellations
            List<BillTypeAtomic> channelCancellations = new ArrayList<>();
            channelCancellations.add(BillTypeAtomic.CHANNEL_CANCELLATION_WITH_PAYMENT);
            channelCancellations.add(BillTypeAtomic.CHANNEL_CANCELLATION_WITH_PAYMENT_FOR_CREDIT_SETTLED_BOOKINGS);
            channelCancellations.add(BillTypeAtomic.CHANNEL_CANCELLATION_WITH_PAYMENT_ONLINE_BOOKING);

            ReportTemplateRowBundle ChannelBookingsCancellationBundle = generatePaymentMethodColumnsByBills(channelCancellations);
            ChannelBookingsCancellationBundle.setBundleType("ChannelBookingsCancellation");
            ChannelBookingsCancellationBundle.setName("Channel Paid Bookings Cancellations");
            bundle.getBundles().add(ChannelBookingsCancellationBundle);
            collectionForTheDay -= getSafeTotal(ChannelBookingsCancellationBundle);

            //Genarate Channel Refund
            List<BillTypeAtomic> channelRefunds = new ArrayList<>();
            channelRefunds.add(BillTypeAtomic.CHANNEL_REFUND);
            channelRefunds.add(BillTypeAtomic.CHANNEL_REFUND_WITH_PAYMENT);
            channelRefunds.add(BillTypeAtomic.CHANNEL_REFUND_WITH_PAYMENT_FOR_CREDIT_SETTLED_BOOKINGS);

            ReportTemplateRowBundle ChannelBookingsRefundBundle = generatePaymentMethodColumnsByBills(channelRefunds);
            ChannelBookingsRefundBundle.setBundleType("ChannelBookingsRefunds");
            ChannelBookingsRefundBundle.setName("Channel Paid Bookings Refunds");
            bundle.getBundles().add(ChannelBookingsRefundBundle);
            collectionForTheDay -= getSafeTotal(ChannelBookingsRefundBundle);

            // Final net cash for the day
            ReportTemplateRowBundle netCashForTheDayBundle = new ReportTemplateRowBundle();
            netCashForTheDayBundle.setName("Net Cash");
            netCashForTheDayBundle.setBundleType("netCash");
            netCashForTheDayBundle.setTotal(netCashCollection);

            bundle.getBundles().add(netCashForTheDayBundle);
            bundle.calculateTotalsByAllChildBundles();
        }, CashierReports.CASHIER_DETAILED, sessionController.getLoggedUser());

    }

    public ReportTemplateRowBundle generatePaymentMethodColumnsByBills(List<BillTypeAtomic> bts) {
        return generatePaymentMethodColumnsByBills(bts, null);
    }

    public ReportTemplateRowBundle generatePaymentMethodColumnsByBills(List<BillTypeAtomic> bts, List<PaymentMethod> billPaymentMethods) {
        Map<String, Object> parameters = new HashMap<>();
        String jpql = "SELECT new com.divudi.core.data.ReportTemplateRow("
                + "bill, "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cash THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Card THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.MultiplePaymentMethods THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Credit THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff_Welfare THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Voucher THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.IOU THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Agent THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cheque THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Slip THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.ewallet THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientDeposit THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientPoints THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.OnlineSettlement THEN p.paidValue ELSE 0 END)) "
                + "FROM Payment p "
                + "JOIN p.bill bill "
                + "WHERE p.retired <> :bfr AND bill.retired <> :br ";

        parameters.put("bfr", true);
        parameters.put("br", true);

        jpql += "AND bill.billTypeAtomic in :bts ";
        parameters.put("bts", bts);

        if (institution != null) {
            jpql += "AND bill.department.institution = :ins ";
            parameters.put("ins", institution);
        }

        if (billPaymentMethods != null) {
            jpql += "AND bill.paymentMethod in :bpms ";
            parameters.put("bpms", billPaymentMethods);
        }

        if (department != null) {
            jpql += "AND bill.department = :dep ";
            parameters.put("dep", department);
        }
        if (site != null) {
            jpql += "AND bill.department.site = :site ";
            parameters.put("site", site);
        }
        if (webUser != null) {
            jpql += "AND p.creater = :wu ";
            parameters.put("wu", webUser);
        }
        if (paymentMethod != null) {
            jpql += "AND p.paymentMethod = :pm ";
            parameters.put("pm", paymentMethod);
        }

        jpql += "AND p.createdAt BETWEEN :fd AND :td ";
        parameters.put("fd", fromDate);
        parameters.put("td", toDate);

        jpql += "GROUP BY bill";

        List<ReportTemplateRow> rs = (List<ReportTemplateRow>) paymentFacade.findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

        ReportTemplateRowBundle b = new ReportTemplateRowBundle();
        b.setReportTemplateRows(rs);
        b.createRowValuesFromBill();
        b.calculateTotalsWithCredit();
        return b;
    }

    public ReportTemplateRowBundle generatePaymentMethodColumnsByStaffWelfareBills(List<BillTypeAtomic> bts, List<PaymentMethod> billPaymentMethods) {
        Map<String, Object> parameters = new HashMap<>();
        String jpql = "SELECT new com.divudi.core.data.ReportTemplateRow("
                + "bill, "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cash THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Card THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.MultiplePaymentMethods THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Credit THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff_Welfare THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Voucher THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.IOU THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Agent THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cheque THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Slip THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.ewallet THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientDeposit THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientPoints THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.OnlineSettlement THEN p.paidValue ELSE 0 END)) "
                + "FROM Payment p "
                + "JOIN p.bill bill "
                + "WHERE p.retired <> :bfr AND bill.retired <> :br "
                + "AND bill.toStaff IS NOT NULL ";

        parameters.put("bfr", true);
        parameters.put("br", true);

        jpql += "AND bill.billTypeAtomic in :bts ";
        parameters.put("bts", bts);

        if (institution != null) {
            jpql += "AND bill.department.institution = :ins ";
            parameters.put("ins", institution);
        }

        if (billPaymentMethods != null) {
            jpql += "AND bill.paymentMethod in :bpms ";
            parameters.put("bpms", billPaymentMethods);
        }

        if (department != null) {
            jpql += "AND bill.department = :dep ";
            parameters.put("dep", department);
        }
        if (site != null) {
            jpql += "AND bill.department.site = :site ";
            parameters.put("site", site);
        }
        if (webUser != null) {
            jpql += "AND p.creater = :wu ";
            parameters.put("wu", webUser);
        }
        if (paymentMethod != null) {
            jpql += "AND p.paymentMethod = :pm ";
            parameters.put("pm", paymentMethod);
        }

        if (staff != null) {
            jpql += "AND ((bill.toStaff.person.name) = :staffName ) ";
            parameters.put("staffName", staff.getPerson().getName());
        }

        jpql += "AND p.createdAt BETWEEN :fd AND :td ";
        parameters.put("fd", fromDate);
        parameters.put("td", toDate);

        jpql += "GROUP BY bill";

        List<ReportTemplateRow> rs = (List<ReportTemplateRow>) paymentFacade.findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

        ReportTemplateRowBundle b = new ReportTemplateRowBundle();
        b.setReportTemplateRows(rs);
        b.createRowValuesFromBill();
        b.calculateTotalsWithCredit();
        return b;
    }

    public ReportTemplateRowBundle generateBillItems(List<BillTypeAtomic> bts, List<PaymentMethod> billPaymentMethods) {
        Map<String, Object> parameters = new HashMap<>();
        String jpql = "SELECT new com.divudi.core.data.ReportTemplateRow(billItem) "
                + "FROM BillItem billItem "
                + "JOIN billItem.bill bill "
                + "WHERE billItem.retired <> :bfr AND bill.retired <> :br ";

        parameters.put("bfr", true);
        parameters.put("br", true);

        jpql += "AND bill.billTypeAtomic in :bts ";
        parameters.put("bts", bts);

        if (institution != null) {
            jpql += "AND bill.department.institution = :ins ";
            parameters.put("ins", institution);
        }

        if (billPaymentMethods != null) {
            jpql += "AND bill.paymentMethod in :bpms ";
            parameters.put("bpms", billPaymentMethods);
        }

        if (department != null) {
            jpql += "AND bill.department = :dep ";
            parameters.put("dep", department);
        }
        if (site != null) {
            jpql += "AND bill.department.site = :site ";
            parameters.put("site", site);
        }
        if (webUser != null) {
            jpql += "AND bill.creater = :wu ";
            parameters.put("wu", webUser);
        }

        jpql += "AND bill.createdAt BETWEEN :fd AND :td ";
        parameters.put("fd", fromDate);
        parameters.put("td", toDate);

        jpql += "GROUP BY billItem";

        List<ReportTemplateRow> rs = (List<ReportTemplateRow>) paymentFacade.findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

        ReportTemplateRowBundle b = new ReportTemplateRowBundle();
        b.setReportTemplateRows(rs);
        b.createRowValuesFromBillItems();
        b.calculateTotalsWithCredit();
        return b;
    }

    public ReportTemplateRowBundle generateStaffWelfareBillItems(List<BillTypeAtomic> bts, List<PaymentMethod> billPaymentMethods) {
        Map<String, Object> parameters = new HashMap<>();
        String jpql = "SELECT new com.divudi.core.data.ReportTemplateRow(billItem) "
                + "FROM BillItem billItem "
                + "JOIN billItem.bill bill "
                + "WHERE billItem.retired <> :bfr AND bill.retired <> :br "
                + "AND bill.toStaff IS NOT NULL ";

        parameters.put("bfr", true);
        parameters.put("br", true);

        jpql += "AND bill.billTypeAtomic in :bts ";
        parameters.put("bts", bts);

        if (institution != null) {
            jpql += "AND bill.department.institution = :ins ";
            parameters.put("ins", institution);
        }

        if (billPaymentMethods != null) {
            jpql += "AND bill.paymentMethod in :bpms ";
            parameters.put("bpms", billPaymentMethods);
        }

        if (department != null) {
            jpql += "AND bill.department = :dep ";
            parameters.put("dep", department);
        }
        if (site != null) {
            jpql += "AND bill.department.site = :site ";
            parameters.put("site", site);
        }
        if (webUser != null) {
            jpql += "AND bill.creater = :wu ";
            parameters.put("wu", webUser);
        }

        if (staff != null) {
            jpql += "AND ((bill.toStaff.person.name) = :toStaffName ) ";
            parameters.put("toStaffName", staff.getPerson().getName());
        }

        jpql += "AND bill.createdAt BETWEEN :fd AND :td ";
        parameters.put("fd", fromDate);
        parameters.put("td", toDate);

        jpql += "GROUP BY billItem";

        List<ReportTemplateRow> rs = (List<ReportTemplateRow>) paymentFacade.findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

        ReportTemplateRowBundle b = new ReportTemplateRowBundle();
        b.setReportTemplateRows(rs);
        b.createRowValuesFromBillItems();
        b.calculateTotalsWithCredit();
        return b;
    }

    private double getSafeTotal(ReportTemplateRowBundle bundle) {
        return bundle != null && bundle.getTotal() != null ? bundle.getTotal() : 0.0;
    }

    public ReportTemplateRowBundle generateOpdServiceByBillItems() {
        ReportTemplateRowBundle biBundle = new ReportTemplateRowBundle();
        String jpql = "select bi "
                + " from BillItem bi "
                + " where bi.bill.retired=:br "
                + " and bi.bill.createdAt between :fd and :td ";
        Map m = new HashMap();
        m.put("br", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<BillTypeAtomic> btas = BillTypeAtomic.findByServiceType(ServiceType.OPD);
        biBundle.setDescription("Bill Types Listed: " + btas);
        if (!btas.isEmpty()) {
            jpql += " and bi.bill.billTypeAtomic in :bts ";
            m.put("bts", btas);
        }
        if (category != null) {
            jpql += " and bi.item.category.id=:catId ";
            m.put("catId", category.getId());
        }
        if (item != null) {
            jpql += " and bi.item.id=:itemId ";
            m.put("itemId", item.getId());
        }

        if (department != null) {
            jpql += " and bi.bill.department=:dep ";
            m.put("dep", department);
        }
        if (institution != null) {
            jpql += " and bi.bill.department.institution=:ins ";
            m.put("ins", institution);
        }
        if (site != null) {
            jpql += " and bi.bill.department.site=:site ";
            m.put("site", site);
        }

        List<BillItem> bis = billItemFacade.findByJpql(jpql, m, TemporalType.TIMESTAMP);

        biBundle = billItemsToBundleForOpd(biBundle, bis);

        biBundle.setName("OPD Service Collection");
        biBundle.setBundleType("opdServiceCollection");
        return biBundle;
    }

    public ReportTemplateRowBundle generateItemizedSalesSummary() {
        ReportTemplateRowBundle oiBundle = new ReportTemplateRowBundle();
        String jpql = "select bi "
                + " from BillItem bi "
                + " where bi.bill.retired=:br "
                + " and bi.bill.createdAt between :fd and :td ";
        Map m = new HashMap();
        m.put("br", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<BillTypeAtomic> btas = BillTypeAtomic.findByServiceType(ServiceType.OPD);
        oiBundle.setDescription("Bill Types Listed: " + btas);
        if (!btas.isEmpty()) {
            jpql += " and bi.bill.billTypeAtomic in :bts ";
            m.put("bts", btas);
        }

        if (department != null) {
            jpql += " and bi.bill.department=:dep ";
            m.put("dep", department);
        }
        if (institution != null) {
            jpql += " and bi.bill.department.institution=:ins ";
            m.put("ins", institution);
        }
        if (site != null) {
            jpql += " and bi.bill.department.site=:site ";
            m.put("site", site);
        }
        if (category != null) {
            jpql += " and bi.item.category=:cat ";
            m.put("cat", category);
        }
        if (item != null) {
            jpql += " and bi.item=:item ";
            m.put("item", item);
        }

        List<BillItem> bis = billItemFacade.findByJpql(jpql, m, TemporalType.TIMESTAMP);
        billItemsToItamizedSaleSummary(oiBundle, bis);

        oiBundle.setName("Itemized Sales Summary");
        oiBundle.setBundleType("itemized_sales_summary");

        oiBundle.getReportTemplateRows().stream()
                .forEach(rtr -> {
                    rtr.setInstitution(institution);
                    rtr.setDepartment(department);
                    rtr.setSite(site);
                    rtr.setFromDate(fromDate);
                    rtr.setToDate(toDate);
                });

        return oiBundle;
    }

    public List<OpdSaleSummaryDTO> generateItemizedSalesSummaryDto() {
        return billService.fetchOpdSaleSummaryDTOs(fromDate, toDate, institution, site,
                department, category, item);
    }

    public void billItemsToItamizedSaleSummary(ReportTemplateRowBundle rtrb, List<BillItem> billItems) {
        Map<String, ReportTemplateRow> categoryMap = new HashMap<>();
        Map<String, ReportTemplateRow> itemMap = new HashMap<>();
        List<ReportTemplateRow> rowsToAdd = new ArrayList<>();
        double totalOpdServiceCollection = 0.0;
        for (BillItem bi : billItems) {

            if (bi.getBill() == null) {
                continue;
            }

            String categoryName = bi.getItem() != null && bi.getItem().getCategory() != null ? bi.getItem().getCategory().getName() : "No Category";
            String itemName = bi.getItem() != null ? bi.getItem().getName() : "No Item";
            String itemKey = categoryName + "->" + itemName;

            categoryMap.putIfAbsent(categoryName, new ReportTemplateRow());
            itemMap.putIfAbsent(itemKey, new ReportTemplateRow());

            ReportTemplateRow categoryRow = categoryMap.get(categoryName);
            ReportTemplateRow itemRow = itemMap.get(itemKey);

            if (bi.getItem() != null) {
                categoryRow.setCategory(bi.getItem().getCategory());
                itemRow.setItem(bi.getItem());
            }

            long countModifier = 1;
            double grossValue = bi.getGrossValue();
            double hospitalFee = bi.getHospitalFee();
            double discount = bi.getDiscount();
            double staffFee = bi.getStaffFee();
            double netValue = bi.getNetValue();

            switch (bi.getBill().getBillClassType()) {
                case CancelledBill:
                case RefundBill:
                    countModifier = -1;
                    // Apply abs to ensure all values are positive before negating
                    grossValue = -Math.abs(grossValue);
                    hospitalFee = -Math.abs(hospitalFee);
                    discount = -Math.abs(discount);
                    staffFee = -Math.abs(staffFee);
                    netValue = -Math.abs(netValue);
                    break;
                case BilledBill:
                case Bill:
                    // Positive adjustments, no need to change the sign or apply abs
                    break;
                default:
                    // Do nothing for other types of bills
                    continue;  // Skip processing for unrecognized or unhandled bill types
            }
            totalOpdServiceCollection += netValue;

            updateRow(categoryRow, countModifier, grossValue, hospitalFee, discount, staffFee, netValue);
            updateRow(itemRow, countModifier, grossValue, hospitalFee, discount, staffFee, netValue);
        }

        // Only add rows that are properly initialized and grouped
        categoryMap.forEach((categoryName, catRow) -> {

            rowsToAdd.add(catRow);
            itemMap.values().stream()
                    .filter(iRow -> iRow.getItem() != null && iRow.getItem().getCategory() != null && iRow.getItem().getCategory().getName().equals(categoryName))
                    .forEach(iRow -> {
                        rowsToAdd.add(iRow);
                    });
        });

        rtrb.getReportTemplateRows().addAll(rowsToAdd);
        rtrb.setTotal(totalOpdServiceCollection);
    }

    public ReportTemplateRowBundle generateIncomeBreakdownByCategory() {
        ReportTemplateRowBundle oiBundle = new ReportTemplateRowBundle();

        // Prepare parameters
        Map<String, Object> mOP = new HashMap<>();
        Map<String, Object> mIP = new HashMap<>();
        mOP.put("br", false);
        mOP.put("fd", fromDate);
        mOP.put("td", toDate);
        mIP.putAll(mOP);

        // Prepare BillTypeAtomic lists
        List<BillTypeAtomic> obtas = BillTypeAtomic.findByServiceType(ServiceType.OPD);
        List<BillTypeAtomic> ibtas = BillTypeAtomic.findByServiceType(ServiceType.INWARD_SERVICE);

        List<BillTypeAtomic> btasOP = new ArrayList<>();
        List<BillTypeAtomic> btasIP = new ArrayList<>();

        if (visitType != null) {
            switch (visitType) {
                case "Any":
                    btasOP.addAll(obtas);
                    btasIP.addAll(ibtas);
                    break;
                case "OP":
                    btasOP.addAll(obtas);
                    break;
                case "IP":
                    btasIP.addAll(ibtas);
                    break;
                default:
                    break;
            }
        }

        // Prepare payment methods
        List<PaymentMethod> creditPaymentMethods = enumController.getPaymentTypeOfPaymentMethods(PaymentType.CREDIT);
        List<PaymentMethod> nonCreditPaymentMethods = enumController.getPaymentTypeOfPaymentMethods(PaymentType.NON_CREDIT);

        // Initialize lists to hold BillItems
        List<BillItem> bisOP = new ArrayList<>();
        List<BillItem> bisIP = new ArrayList<>();

        // Fetch OP Bills if applicable
        if (!btasOP.isEmpty()) {
            String jpqlOP = "select bi "
                    + " from BillItem bi "
                    + " where bi.bill.retired=:br "
                    + " and bi.bill.createdAt between :fd and :td "
                    + " and bi.bill.patientEncounter is null "
                    + " and bi.bill.billTypeAtomic in :bts ";

            mOP.put("bts", btasOP);

            // Apply payment method filter for OP bills
            if (!"Any".equals(methodType)) {
                if ("Credit".equals(methodType)) {
                    jpqlOP += " and bi.bill.paymentMethod in :pm ";
                    mOP.put("pm", creditPaymentMethods);
                } else if ("NonCredit".equals(methodType)) {
                    jpqlOP += " and bi.bill.paymentMethod in :pm ";
                    mOP.put("pm", nonCreditPaymentMethods);
                }
            }

            // Additional filters
            if (department != null) {
                jpqlOP += " and bi.bill.department=:dep ";
                mOP.put("dep", department);
            }
            if (institution != null) {
                jpqlOP += " and bi.bill.department.institution=:ins ";
                mOP.put("ins", institution);
            }
            if (site != null) {
                jpqlOP += " and bi.bill.department.site=:site ";
                mOP.put("site", site);
            }
            if (category != null) {
                jpqlOP += " and bi.item.category=:cat ";
                mOP.put("cat", category);
            }

            bisOP = billItemFacade.findByJpql(jpqlOP, mOP, TemporalType.TIMESTAMP);
        }

        // Fetch IP Bills if applicable
        if (!btasIP.isEmpty()) {
            String jpqlIP = "select bi "
                    + " from BillItem bi "
                    + " where bi.bill.retired=:br "
                    + " and bi.bill.createdAt between :fd and :td "
                    + " and bi.bill.patientEncounter is not null "
                    + " and bi.bill.billTypeAtomic in :bts ";

            mIP.put("bts", btasIP);

            // Apply payment method filter for IP bills
            if (!"Any".equals(methodType)) {
                if ("Credit".equals(methodType)) {
                    jpqlIP += " and bi.bill.patientEncounter.paymentMethod in :pm ";
                    mIP.put("pm", creditPaymentMethods);
                } else if ("NonCredit".equals(methodType)) {
                    jpqlIP += " and bi.bill.patientEncounter.paymentMethod in :pm ";
                    mIP.put("pm", nonCreditPaymentMethods);
                }
            }

            // Additional filters
            if (department != null) {
                jpqlIP += " and bi.bill.department=:dep ";
                mIP.put("dep", department);
            }
            if (institution != null) {
                jpqlIP += " and bi.bill.department.institution=:ins ";
                mIP.put("ins", institution);
            }
            if (site != null) {
                jpqlIP += " and bi.bill.department.site=:site ";
                mIP.put("site", site);
            }
            if (category != null) {
                jpqlIP += " and bi.item.category=:cat ";
                mIP.put("cat", category);
            }

            bisIP = billItemFacade.findByJpql(jpqlIP, mIP, TemporalType.TIMESTAMP);
        }

        // Combine OP and IP BillItems
        List<BillItem> bis = new ArrayList<>();
        bis.addAll(bisOP);
        bis.addAll(bisIP);

        for (BillItem bi : bis) {

            if (bi.getBill().getPatientEncounter() != null) {

            } else {

            }
        }

        // Process the combined BillItems
        summarizeBillItemsToIncomeByCategory(oiBundle, bis);

        // Set report details
        oiBundle.setName("Income Breakdown By Category - With Professional Fee");
        oiBundle.setBundleType("income_breakdown_by_category_with_professional_fee");

        oiBundle.getReportTemplateRows().forEach(rtr -> {
            rtr.setInstitution(institution);
            rtr.setDepartment(department);
            rtr.setSite(site);
            rtr.setFromDate(fromDate);
            rtr.setToDate(toDate);
        });

        return oiBundle;
    }

    public ReportTemplateRowBundle generateOpdProfessionalFees(String paymentStatusStr) {
        PaymentStatus paymentStatus = PaymentStatus.ALL;
        if (paymentStatusStr != null) {
            try {
                paymentStatus = PaymentStatus.valueOf(paymentStatusStr.toUpperCase());
            } catch (IllegalArgumentException e) {

            }
        }

        bundle = new ReportTemplateRowBundle();
        String jpql = "select bf "
                + " from BillFee bf "
                + " join bf.billItem bi "
                + " where bf.retired=:bfr "
                + " and bi.bill.retired=:br "
                + " and bf.fee.feeType=:ft "
                + " and bi.bill.createdAt between :fd and :td ";

        Map<String, Object> m = new HashMap<>();
        m.put("br", false);
        m.put("bfr", false);
        m.put("fd", fromDate);
        m.put("ft", FeeType.Staff);
        m.put("td", toDate);

        // Add payment status condition
        if (paymentStatus == PaymentStatus.DUE) {
            jpql += " and (bf.paidValue IS NULL OR bf.paidValue = 0) "
                    + " and bi.bill.cancelled = :can "
                    + " and bi.refunded = :refu";
            m.put("can", false);
            m.put("refu", false);
        } else if (paymentStatus == PaymentStatus.DONE) {
            jpql += " and bf.paidValue > 0 ";
        }
        // If paymentStatus is ALL, no additional condition is added

        // Add other conditions based on your filters
        List<BillTypeAtomic> btas = visitType.equalsIgnoreCase("OP") ? BillTypeAtomic.findByServiceType(ServiceType.OPD) : BillTypeAtomic.findByServiceType(ServiceType.INWARD);
        bundle.setDescription("Bill Types Listed: " + btas);
        if (!btas.isEmpty()) {
            jpql += " and bi.bill.billTypeAtomic in :bts ";
            m.put("bts", btas);
        }

        if (department != null) {
            jpql += " and bi.bill.department=:dep ";
            m.put("dep", department);
        }
        if (institution != null) {
            jpql += " and bi.bill.department.institution=:ins ";
            m.put("ins", institution);
        }
        if (site != null) {
            jpql += " and bi.bill.department.site=:site ";
            m.put("site", site);
        }
        if (category != null) {
            jpql += " and bi.item.category=:cat ";
            m.put("cat", category);
        }
        if (item != null) {
            jpql += " and bi.item=:item ";
            m.put("item", item);
        }
        if (speciality != null) {
            jpql += " and bf.speciality=:speciality ";
            m.put("speciality", speciality);
        }
        if (staff != null) {
            jpql += " and bf.staff=:staff ";
            m.put("staff", staff);
        }
        if (mrnNo != null && !mrnNo.isEmpty()) {
            jpql += " and UPPER(bi.bill.patient.phn) LIKE :phn ";
            m.put("phn", "%" + mrnNo.toUpperCase() + "%");
        }

        List<BillFee> bifs = billFeeFacade.findByJpql(jpql, m, TemporalType.TIMESTAMP);

        if (bifs != null) {
            for (BillFee bf : bifs) {
                ReportTemplateRow r = new ReportTemplateRow(bf);
                bundle.getReportTemplateRows().add(r);
            }
        }
        bundle.setName("Professional Fees");
        bundle.setBundleType("professional_fees");

        bundle.getReportTemplateRows().stream()
                .forEach(rtr -> {
                    rtr.setInstitution(institution);
                    rtr.setDepartment(department);
                    rtr.setSite(site);
                    rtr.setFromDate(fromDate);
                    rtr.setToDate(toDate);
                    rtr.setStaff(staff);
                    rtr.setSpeciality(speciality);
                });

        if (visitType.equalsIgnoreCase("OP")) {
            bundle.calculateTotalsForProfessionalFees();
        } else {
            bundle.calculateTotalsForProfessionalFeesForInward();
        }

        return bundle;
    }

    public ReportTemplateRowBundle generateItemizedSalesReport() {
        ReportTemplateRowBundle oiBundle = new ReportTemplateRowBundle();

        // Prepare parameters
        Map<String, Object> mOP = new HashMap<>();
        Map<String, Object> mIP = new HashMap<>();
        mOP.put("br", false);
        mOP.put("fd", fromDate);
        mOP.put("td", toDate);
        mIP.putAll(mOP);

        // Prepare BillTypeAtomic lists
        List<BillTypeAtomic> obtas = BillTypeAtomic.findByServiceType(ServiceType.OPD);
        List<BillTypeAtomic> ibtas = BillTypeAtomic.findByServiceType(ServiceType.INWARD_SERVICE);

        List<BillTypeAtomic> btasOP = new ArrayList<>();
        List<BillTypeAtomic> btasIP = new ArrayList<>();

        if (visitType != null) {
            switch (visitType) {
                case "Any":
                    btasOP.addAll(obtas);
                    btasIP.addAll(ibtas);
                    break;
                case "OP":
                    btasOP.addAll(obtas);
                    break;
                case "IP":
                    btasIP.addAll(ibtas);
                    break;
                default:
                    break;
            }
        }

        // Prepare payment methods
        List<PaymentMethod> creditPaymentMethods = enumController.getPaymentTypeOfPaymentMethods(PaymentType.CREDIT);
        List<PaymentMethod> nonCreditPaymentMethods = enumController.getPaymentTypeOfPaymentMethods(PaymentType.NON_CREDIT);

        // Initialize lists to hold BillItems
        List<BillItem> bisOP = new ArrayList<>();
        List<BillItem> bisIP = new ArrayList<>();

        // Fetch OP Bills if applicable
        if (!btasOP.isEmpty()) {
            String jpqlOP = "select bi "
                    + " from BillItem bi "
                    + " where bi.bill.retired=:br "
                    + " and bi.bill.createdAt between :fd and :td "
                    + " and bi.bill.patientEncounter is null "
                    + " and bi.bill.billTypeAtomic in :bts ";

            mOP.put("bts", btasOP);

            // Apply payment method filter for OP bills
            if (!"Any".equals(methodType)) {
                if ("Credit".equals(methodType)) {
                    jpqlOP += " and bi.bill.paymentMethod in :pm ";
                    mOP.put("pm", creditPaymentMethods);
                } else if ("NonCredit".equals(methodType)) {
                    jpqlOP += " and bi.bill.paymentMethod in :pm ";
                    mOP.put("pm", nonCreditPaymentMethods);
                }
            }

            // Additional filters
            if (billedDepartment != null) {
                jpqlOP += " and bi.bill.department=:dept ";
                mOP.put("dept", billedDepartment);
            }

            if (serviceDepartment != null) {
                jpqlOP += " and bi.bill.toDepartment=:serDept ";
                mOP.put("serDept", serviceDepartment);
            }

            if (institution != null) {
                jpqlOP += " and bi.bill.department.institution=:ins ";
                mOP.put("ins", institution);
            }
            if (site != null) {
                jpqlOP += " and bi.bill.department.site=:site ";
                mOP.put("site", site);
            }
            if (category != null) {
                jpqlOP += " and bi.item.category=:cat ";
                mOP.put("cat", category);
            }
            if (item != null) {
                jpqlOP += " and bi.item=:item ";
                mOP.put("item", item);
            }

            // Fetch OP BillItems
            bisOP = billItemFacade.findByJpql(jpqlOP, mOP, TemporalType.TIMESTAMP);
        }

        // Fetch IP Bills if applicable
        if (!btasIP.isEmpty()) {
            String jpqlIP = "select bi "
                    + " from BillItem bi "
                    + " where bi.bill.retired=:br "
                    + " and bi.bill.createdAt between :fd and :td "
                    + " and bi.bill.patientEncounter is not null "
                    + " and bi.bill.billTypeAtomic in :bts ";

            mIP.put("bts", btasIP);

            // Apply payment method filter for IP bills
            if (!"Any".equals(methodType)) {
                if ("Credit".equals(methodType)) {
                    jpqlIP += " and bi.bill.patientEncounter.paymentMethod in :pm ";
                    mIP.put("pm", creditPaymentMethods);
                } else if ("NonCredit".equals(methodType)) {
                    jpqlIP += " and bi.bill.patientEncounter.paymentMethod in :pm ";
                    mIP.put("pm", nonCreditPaymentMethods);
                }
            }

            // Additional filters
            if (billedDepartment != null) {
                jpqlIP += " and bi.bill.department=:dept ";
                mIP.put("dept", billedDepartment);
            }

            if (serviceDepartment != null) {
                jpqlIP += " and bi.bill.toDepartment=:serDept ";
                mIP.put("serDept", serviceDepartment);
            }

            if (institution != null) {
                jpqlIP += " and bi.bill.department.institution=:ins ";
                mIP.put("ins", institution);
            }
            if (site != null) {
                jpqlIP += " and bi.bill.department.site=:site ";
                mIP.put("site", site);
            }
            if (category != null) {
                jpqlIP += " and bi.item.category=:cat ";
                mIP.put("cat", category);
            }
            if (item != null) {
                jpqlIP += " and bi.item=:item ";
                mIP.put("item", item);
            }

            // Fetch IP BillItems
            bisIP = billItemFacade.findByJpql(jpqlIP, mIP, TemporalType.TIMESTAMP);
        }

        // Combine OP and IP BillItems
        List<BillItem> bis = new ArrayList<>();
        bis.addAll(bisOP);
        bis.addAll(bisIP);

        billItemsToItamizedSaleReport(oiBundle, bis);

        oiBundle.getReportTemplateRows().stream()
                .forEach(rtr -> {
                    rtr.setInstitution(institution);
                    rtr.setDepartment(department);
                    rtr.setSite(site);
                    rtr.setFromDate(fromDate);
                    rtr.setToDate(toDate);
                });

        return oiBundle;
    }

    public ReportTemplateRowBundle generateBillsByItemCategory() {
        ReportTemplateRowBundle oiBundle = new ReportTemplateRowBundle();
        String jpql = "select bi "
                + " from BillItem bi "
                + " where bi.bill.retired=:br "
                + " and bi.bill.createdAt between :fd and :td ";
        Map m = new HashMap();
        m.put("br", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<BillTypeAtomic> btas = BillTypeAtomic.findByServiceType(ServiceType.OPD);
        oiBundle.setDescription("Bill Types Listed: " + btas);
        if (!btas.isEmpty()) {
            jpql += " and bi.bill.billTypeAtomic in :bts ";
            m.put("bts", btas);
        }

        if (department != null) {
            jpql += " and bi.bill.department=:dep ";
            m.put("dep", department);
        }
        if (institution != null) {
            jpql += " and bi.bill.department.institution=:ins ";
            m.put("ins", institution);
        }
        if (site != null) {
            jpql += " and bi.bill.department.site=:site ";
            m.put("site", site);
        }
        if (category != null) {
            jpql += " and bi.item.category=:cat ";
            m.put("cat", category);
        }
        if (item != null) {
            jpql += " and bi.item=:item ";
            m.put("item", item);
        }

        List<BillItem> bis = billItemFacade.findByJpql(jpql, m, TemporalType.TIMESTAMP);
        billItemsToItamizedSaleReport(oiBundle, bis);

        oiBundle.setName("Itemized Sales Report");
        oiBundle.setBundleType("itemized_sales_report");

        oiBundle.getReportTemplateRows().stream()
                .forEach(rtr -> {
                    rtr.setInstitution(institution);
                    rtr.setDepartment(department);
                    rtr.setSite(site);
                    rtr.setFromDate(fromDate);
                    rtr.setToDate(toDate);
                });

        return oiBundle;
    }

    public void createBillsByItemCategory() {
        bundle = generateBillsByItemCategory();
    }

    public ReportTemplateRowBundle generateOpdServiceCollectionCashier() {
        ReportTemplateRowBundle opdServiceCollection = new ReportTemplateRowBundle();
        String jpql = "select bi "
                + " from BillItem bi "
                + " where bi.bill.retired=:br "
                + " and bi.bill.createdAt between :fd and :td ";
        Map m = new HashMap();
        m.put("br", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<BillTypeAtomic> btas = BillTypeAtomic.findByServiceType(ServiceType.OPD);
        opdServiceCollection.setDescription("Bill Types Listed: " + btas);
        if (!btas.isEmpty()) {
            jpql += " and bi.bill.billTypeAtomic in :bts ";
            m.put("bts", btas);
        }

        if (department != null) {
            jpql += " and bi.bill.department=:dep ";
            m.put("dep", department);
        }
        if (institution != null) {
            jpql += " and bi.bill.department.institution=:ins ";
            m.put("ins", institution);
        }
        if (site != null) {
            jpql += " and bi.bill.department.site=:site ";
            m.put("site", site);
        }
        if (webUser != null) {
            jpql += " and bi.bill.creater=:wu ";
            m.put("wu", webUser);
        }

        List<BillItem> bis = billItemFacade.findByJpql(jpql, m, TemporalType.TIMESTAMP);
        billItemsToBundleForOpdUnderCategory(opdServiceCollection, bis, PaymentType.NON_CREDIT);

        opdServiceCollection.setName("OPD Service Collection");
        opdServiceCollection.setBundleType("opdServiceCollection");

        opdServiceCollection.getReportTemplateRows().stream()
                .forEach(rtr -> {
                    rtr.setInstitution(institution);
                    rtr.setDepartment(department);
                    rtr.setSite(site);
                    rtr.setFromDate(fromDate);
                    rtr.setToDate(toDate);
                });

        return opdServiceCollection;
    }

    public ReportTemplateRowBundle generateOpdServiceCollection(PaymentType paymentType) {
        ReportTemplateRowBundle opdServiceCollection = new ReportTemplateRowBundle();
        String jpql = "select bi "
                + " from BillItem bi "
                + " where bi.bill.retired=:br "
                + " and bi.retired=:bir "
                + " and bi.bill.createdAt between :fd and :td ";
        Map m = new HashMap();
        m.put("br", false);
        m.put("bir", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<BillTypeAtomic> btas = BillTypeAtomic.findByServiceType(ServiceType.OPD);
        opdServiceCollection.setDescription("Bill Types Listed: " + btas);
        if (!btas.isEmpty()) {
            jpql += " and bi.bill.billTypeAtomic in :bts ";
            m.put("bts", btas);
        }

        if (department != null) {
            jpql += " and bi.bill.department=:dep ";
            m.put("dep", department);
        }
        if (institution != null) {
            jpql += " and bi.bill.department.institution=:ins ";
            m.put("ins", institution);
        }
        if (site != null) {
            jpql += " and bi.bill.department.site=:site ";
            m.put("site", site);
        }

        List<BillItem> bis = billItemFacade.findByJpql(jpql, m, TemporalType.TIMESTAMP);
        billItemsToBundleForOpdUnderCategory(opdServiceCollection, bis, paymentType);

        if (paymentType == PaymentType.CREDIT) {
            opdServiceCollection.setName("OPD Service Collection - Credit");
            opdServiceCollection.setBundleType("opdServiceCollectionCredit");
        } else {
            opdServiceCollection.setName("OPD Service Collection");
            opdServiceCollection.setBundleType("opdServiceCollection");
        }

        opdServiceCollection.getReportTemplateRows().stream()
                .forEach(rtr -> {
                    rtr.setInstitution(institution);
                    rtr.setDepartment(department);
                    rtr.setSite(site);
                    rtr.setFromDate(fromDate);
                    rtr.setToDate(toDate);
                });

        return opdServiceCollection;
    }

    public ReportTemplateRowBundle generateCreditOpdServiceCollection() {
        ReportTemplateRowBundle opdServiceCollection = new ReportTemplateRowBundle();
        String jpql = "select bi "
                + " from BillItem bi "
                + " where bi.bill.retired=:br "
                + " and bi.retired=:bir "
                + " and bi.bill.createdAt between :fd and :td ";
        Map m = new HashMap();
        m.put("br", false);
        m.put("bir", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<BillTypeAtomic> btas = BillTypeAtomic.findByServiceType(ServiceType.OPD);
        opdServiceCollection.setDescription("Bill Types Listed: " + btas);
        if (!btas.isEmpty()) {
            jpql += " and bi.bill.billTypeAtomic in :bts ";
            m.put("bts", btas);
        }

        if (department != null) {
            jpql += " and bi.bill.department=:dep ";
            m.put("dep", department);
        }
        if (institution != null) {
            jpql += " and bi.bill.department.institution=:ins ";
            m.put("ins", institution);
        }
        if (site != null) {
            jpql += " and bi.bill.department.site=:site ";
            m.put("site", site);
        }

        List<BillItem> bis = billItemFacade.findByJpql(jpql, m, TemporalType.TIMESTAMP);
        billItemsToBundleForOpdCreditUnderCategory(opdServiceCollection, bis, PaymentType.CREDIT);

        opdServiceCollection.setName("OPD Service Collection - Credit");
        opdServiceCollection.setBundleType("opdServiceCollectionCredit");

        opdServiceCollection.getReportTemplateRows().stream()
                .forEach(rtr -> {
                    rtr.setInstitution(institution);
                    rtr.setDepartment(department);
                    rtr.setSite(site);
                    rtr.setFromDate(fromDate);
                    rtr.setToDate(toDate);
                });

        return opdServiceCollection;
    }

    public ReportTemplateRowBundle generateOpdServiceCollectionWithoutProfessionalFee(PaymentType paymentType) {
        ReportTemplateRowBundle opdServiceCollection = new ReportTemplateRowBundle();
        String jpql = "select bi "
                + " from BillItem bi "
                + " where bi.bill.retired=:br "
                + " and bi.retired=:bir "
                + " and bi.bill.createdAt between :fd and :td ";
        Map m = new HashMap();
        m.put("br", false);
        m.put("bir", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<BillTypeAtomic> btas = BillTypeAtomic.findByServiceType(ServiceType.OPD);
        opdServiceCollection.setDescription("Bill Types Listed: " + btas);
        if (!btas.isEmpty()) {
            jpql += " and bi.bill.billTypeAtomic in :bts ";
            m.put("bts", btas);
        }

        if (department != null) {
            jpql += " and bi.bill.department=:dep ";
            m.put("dep", department);
        }
        if (institution != null) {
            jpql += " and bi.bill.department.institution=:ins ";
            m.put("ins", institution);
        }
        if (site != null) {
            jpql += " and bi.bill.department.site=:site ";
            m.put("site", site);
        }

        List<BillItem> bis = billItemFacade.findByJpql(jpql, m, TemporalType.TIMESTAMP);
        billItemsToBundleForOpdUnderCategoryWithoutProfessionalFee(opdServiceCollection, bis, paymentType);

        if (paymentType == PaymentType.CREDIT) {
            opdServiceCollection.setName("OPD Service Collection - Credit");
            opdServiceCollection.setBundleType("opdServiceCollectionCredit");
        } else {
            opdServiceCollection.setName("OPD Service Collection");
            opdServiceCollection.setBundleType("opdServiceCollection");
        }

        opdServiceCollection.getReportTemplateRows().stream()
                .forEach(rtr -> {
                    rtr.setInstitution(institution);
                    rtr.setDepartment(department);
                    rtr.setSite(site);
                    rtr.setFromDate(fromDate);
                    rtr.setToDate(toDate);
                });

        return opdServiceCollection;
    }

    public ReportTemplateRowBundle generatePatientDepositCollection() {
        ReportTemplateRowBundle depositCollection = new ReportTemplateRowBundle();
        String jpql = "select b "
                + " from Bill b "
                + " where b.retired=:br "
                + " and b.createdAt between :fd and :td ";
        Map m = new HashMap();
        m.put("br", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<BillTypeAtomic> btas = BillTypeAtomic.findByServiceType(ServiceType.PATIENT_DEPOSIT);
        depositCollection.setDescription("Patient Deposits");
        if (!btas.isEmpty()) {
            jpql += " and b.billTypeAtomic in :bts ";
            m.put("bts", btas);
        }

        if (department != null) {
            jpql += " and b.department=:dep ";
            m.put("dep", department);
        }
        if (institution != null) {
            jpql += " and b.department.institution=:ins ";
            m.put("ins", institution);
        }
        if (site != null) {
            jpql += " and b.department.site=:site ";
            m.put("site", site);
        }
        List<Bill> bis = billFacade.findByJpql(jpql, m, TemporalType.TIMESTAMP);
        billToBundleForPatientDeposits(depositCollection, bis);
        depositCollection.setName("Patient Deposit Payments");
        depositCollection.setBundleType("patientDepositPayments");

        return depositCollection;
    }

    public ReportTemplateRowBundle generatePharmacyCollection() {
        ReportTemplateRowBundle pb;
        //TODO: Use a List of Bill Type Atomics instead of calling the findByServiceTypeAndPaymentCategory
        List<BillTypeAtomic> pharmacyBillTypesAtomics = BillTypeAtomic.findByServiceTypeAndPaymentCategory(ServiceType.PHARMACY,
                PaymentCategory.NON_CREDIT_COLLECTION);
        List<PaymentMethod> ppms = PaymentMethod.getMethodsByType(PaymentType.NON_CREDIT);

        pb = reportTemplateController.generateValueByDepartmentReport(
                pharmacyBillTypesAtomics,
                ppms,
                fromDate,
                toDate,
                institution,
                department,
                site);
        pb.setName("Pharmacy Sale");
        pb.setBundleType("pharmacyCollection");
        double pharmacyCollectionTotal = 0.0;
        for (ReportTemplateRow row : pb.getReportTemplateRows()) {
            pharmacyCollectionTotal += row.getRowValue();
        }
        pb.setTotal(pharmacyCollectionTotal);
        return pb;
    }

    public ReportTemplateRowBundle generateCcCollection() {
        ReportTemplateRowBundle pb;
        List<BillTypeAtomic> ccCollection = new ArrayList<>();
        ccCollection.add(BillTypeAtomic.CC_PAYMENT_CANCELLATION_BILL);
        ccCollection.add(BillTypeAtomic.CC_PAYMENT_RECEIVED_BILL);
        pb = reportTemplateController.generateBillReport(
                ccCollection,
                fromDate,
                toDate,
                institution,
                department,
                site, false, false);
        pb.setName("Collecting Centre Collection");
        pb.setBundleType("ccCollection");
        double ccCollectionTotal = 0.0;
        for (ReportTemplateRow row : pb.getReportTemplateRows()) {
            ccCollectionTotal += row.getBill().getNetTotal();
        }
        pb.setTotal(ccCollectionTotal);
        return pb;
    }

    public ReportTemplateRowBundle generateAgencyCollection() {
        ReportTemplateRowBundle ap;
        List<BillTypeAtomic> ccCollection = new ArrayList<>();
        ccCollection.add(BillTypeAtomic.AGENCY_PAYMENT_RECEIVED);
        ccCollection.add(BillTypeAtomic.AGENCY_PAYMENT_CANCELLATION);
        ap = reportTemplateController.generateBillReport(
                ccCollection,
                fromDate,
                toDate,
                institution,
                department,
                site, false, false);
        ap.setName("Agency Payment Collection");
        ap.setBundleType("PayentBillReport");
        return ap;
    }

    public ReportTemplateRowBundle generateAgencyCollectionForOpd() {
        ReportTemplateRowBundle ap;
        List<BillTypeAtomic> ccCollection = new ArrayList<>();
        ccCollection.add(BillTypeAtomic.AGENCY_PAYMENT_RECEIVED);
        ccCollection.add(BillTypeAtomic.AGENCY_PAYMENT_CANCELLATION);
        ap = reportTemplateController.generateBillReport(
                ccCollection,
                fromDate,
                toDate,
                institution,
                department,
                site, false, false);
        ap.setName("Agency Payment Collection");
        ap.setBundleType("PayentBillReport");
        return ap;
    }

    public ReportTemplateRowBundle generateCreditCompanyCollectionForOpd() {
        ReportTemplateRowBundle ap;
        List<BillTypeAtomic> ccCollection = new ArrayList<>();
        ccCollection.add(BillTypeAtomic.OPD_CREDIT_COMPANY_PAYMENT_RECEIVED);
        ccCollection.add(BillTypeAtomic.OPD_CREDIT_COMPANY_PAYMENT_CANCELLATION);
        ccCollection.add(BillTypeAtomic.OPD_CREDIT_COMPANY_CREDIT_NOTE);
        ccCollection.add(BillTypeAtomic.OPD_CREDIT_COMPANY_DEBIT_NOTE);

        ap = reportTemplateController.generateBillReport(
                ccCollection,
                fromDate,
                toDate,
                institution,
                department,
                site, false, false);
        ap.setName("OPD Credit Company Payment Collection");
        ap.setBundleType("companyPaymentBillOpd");
        return ap;
    }

    public ReportTemplateRowBundle generateCreditCompanyCollectionForInward() {
        ReportTemplateRowBundle ap;
        List<BillTypeAtomic> ccCollection = new ArrayList<>();
        ccCollection.add(BillTypeAtomic.INPATIENT_CREDIT_COMPANY_PAYMENT_RECEIVED);
        ccCollection.add(BillTypeAtomic.INPATIENT_CREDIT_COMPANY_PAYMENT_CANCELLATION);
        ccCollection.add(BillTypeAtomic.INPATIENT_CREDIT_COMPANY_CREDIT_NOTE);
        ccCollection.add(BillTypeAtomic.INPATIENT_CREDIT_COMPANY_DEBIT_NOTE);
        ap = reportTemplateController.generateBillReport(
                ccCollection,
                fromDate,
                toDate,
                institution,
                department,
                site, false, false);
        ap.setName("Inpatient Credit Company Payment Collection");
        ap.setBundleType("companyPaymentBillInward");
        return ap;
    }

    public ReportTemplateRowBundle generateCreditCompanyCollectionForPharmacy() {
        ReportTemplateRowBundle ap;
        List<BillTypeAtomic> ccCollection = new ArrayList<>();
        ccCollection.add(BillTypeAtomic.PHARMACY_CREDIT_COMPANY_PAYMENT_RECEIVED);
        ccCollection.add(BillTypeAtomic.PHARMACY_CREDIT_COMPANY_PAYMENT_CANCELLATION);
        ccCollection.add(BillTypeAtomic.PHARMACY_CREDIT_COMPANY_CREDIT_NOTE);
        ccCollection.add(BillTypeAtomic.PHARMACY_CREDIT_COMPANY_DEBIT_NOTE);
        ap = reportTemplateController.generateBillReport(
                ccCollection,
                fromDate,
                toDate,
                institution,
                department,
                site, false, false);
        ap.setName("Pharmacy Credit Company Payment Collection");
        ap.setBundleType("companyPaymentBillPharmacy");
        return ap;
    }

    public ReportTemplateRowBundle generateCreditCompanyCollectionForChannelling() {
        ReportTemplateRowBundle ap;
        List<BillTypeAtomic> ccCollection = new ArrayList<>();
        ccCollection.add(BillTypeAtomic.CHANNELLING_CREDIT_COMPANY_PAYMENT_RECEIVED);
        ccCollection.add(BillTypeAtomic.CHANNELLING_CREDIT_COMPANY_PAYMENT_CANCELLATION);
        ccCollection.add(BillTypeAtomic.CHANNELLING_CREDIT_COMPANY_CREDIT_NOTE);
        ccCollection.add(BillTypeAtomic.CHANNELLING_CREDIT_COMPANY_DEBIT_NOTE);
        ap = reportTemplateController.generateBillReport(
                ccCollection,
                fromDate,
                toDate,
                institution,
                department,
                site, false, false);
        ap.setName("Channelling Credit Company Payment Collection");
        ap.setBundleType("companyPaymentBillChannelling");
        return ap;
    }

    public ReportTemplateRowBundle generateInwardProfessionalPayments() {
        ReportTemplateRowBundle ap;
        List<BillTypeAtomic> ccCollection = new ArrayList<>();
        ccCollection.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE);
        ccCollection.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN);
        ap = reportTemplateController.generateBillReport(
                ccCollection,
                fromDate,
                toDate,
                institution,
                department,
                site, false, false);
        ap.setName("Inward Professional Payments");
        ap.setBundleType("ProfessionalPaymentBillReportInward");
        return ap;
    }

    public ReportTemplateRowBundle generateChannellingProfessionalPayments() {
        ReportTemplateRowBundle ap;
        List<BillTypeAtomic> ccCollection = new ArrayList<>();
        ccCollection.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE);
        ccCollection.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN);
        ccCollection.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES);
        ccCollection.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES_RETURN);
        ccCollection.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION);

        ap = reportTemplateController.generateBillReport(
                ccCollection,
                fromDate,
                toDate,
                institution,
                department,
                site, false, false);
        ap.setName("Channelling Professional Payments");
        ap.setBundleType("ProfessionalPaymentBillReportChannelling");
        return ap;
    }

    public ReportTemplateRowBundle generateOpdProfessionalPayments() {
        ReportTemplateRowBundle ap;
        List<BillTypeAtomic> opdCollection = new ArrayList<>();
        opdCollection.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES);
        opdCollection.add(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN);

        ap = reportTemplateController.generateBillReport(
                opdCollection,
                fromDate,
                toDate,
                institution,
                department,
                site, false, false);
        ap.setName("OPD Professional Payments");
        ap.setBundleType("ProfessionalPaymentBillReportOpd");
        return ap;
    }

    public ReportTemplateRowBundle generateCreditCardPayments() {
        ReportTemplateRowBundle ap;
        ap = reportTemplateController.generatePaymentReport(
                PaymentMethod.Card,
                fromDate,
                toDate,
                institution,
                department,
                site);
        ap.setName("Credit Card Payments");
        ap.setBundleType("paymentReportCards");
        return ap;
    }

    public ReportTemplateRowBundle retirePaymentsReceivedForIndividualOpdBills() {
        ReportTemplateRowBundle ap;
        ap = reportTemplateController.generatePaymentReport(
                null,
                fromDate,
                toDate,
                institution,
                department,
                site);
        ap.setName("Credit Card Payments");
        ap.setBundleType("paymentReportCards");
        List<ReportTemplateRow> rtrs = new ArrayList<>();
        for (ReportTemplateRow r : ap.getReportTemplateRows()) {
            if (r.getPayment() == null) {
                continue;
            }
            if (r.getPayment().getBill() == null) {
                continue;
            }
            if (r.getPayment().getBill().getBillTypeAtomic() == null) {
                continue;
            }
            if (r.getPayment().getBill().getBillTypeAtomic() == BillTypeAtomic.OPD_BILL_CANCELLATION
                    || r.getPayment().getBill().getBillTypeAtomic() == BillTypeAtomic.OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER
                    || r.getPayment().getBill().getBillTypeAtomic() == BillTypeAtomic.OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION
                    || r.getPayment().getBill().getBillTypeAtomic() == BillTypeAtomic.OPD_BILL_WITH_PAYMENT) {
                r.getPayment().setRetired(true);
                r.getPayment().setRetirer(sessionController.getLoggedUser());
                r.getPayment().setRetireComments("Reriting OPD Individual Payment Cancellations");
                paymentFacade.edit(r.getPayment());
            }
        }
        return ap;
    }

    public ReportTemplateRowBundle generateStaffPayments() {
        ReportTemplateRowBundle ap;
        ap = reportTemplateController.generatePaymentReport(
                PaymentMethod.Staff_Welfare,
                fromDate,
                toDate,
                institution,
                department,
                site);
        ap.setName("Staff Welfare Payments");
        ap.setBundleType("paymentReportStaffWelfare");
        return ap;
    }

    public ReportTemplateRowBundle generateVoucherPayments() {
        ReportTemplateRowBundle ap;
        ap = reportTemplateController.generatePaymentReport(
                PaymentMethod.Voucher,
                fromDate,
                toDate,
                institution,
                department,
                site);
        ap.setName("Voucher Payments");
        ap.setBundleType("paymentReportVoucher");
        return ap;
    }

    public ReportTemplateRowBundle generateChequePayments() {
        ReportTemplateRowBundle ap;
        ap = reportTemplateController.generatePaymentReport(
                PaymentMethod.Cheque,
                fromDate,
                toDate,
                institution,
                department,
                site);
        ap.setName("Cheque Payments");
        ap.setBundleType("paymentReportCheque");
        return ap;
    }

    public ReportTemplateRowBundle generateEwalletPayments() {
        ReportTemplateRowBundle ap;
        ap = reportTemplateController.generatePaymentReport(
                PaymentMethod.ewallet,
                fromDate,
                toDate,
                institution,
                department,
                site);
        ap.setName("e-Wallet Payments");
        ap.setBundleType("paymentReportEwallet");
        return ap;
    }

    public ReportTemplateRowBundle generatePettyCashPayments() {
        ReportTemplateRowBundle ap;
        List<BillTypeAtomic> btas = new ArrayList<>();
        btas.add(BillTypeAtomic.PETTY_CASH_BILL_CANCELLATION);
        btas.add(BillTypeAtomic.PETTY_CASH_ISSUE);
        btas.add(BillTypeAtomic.PETTY_CASH_RETURN);

        ap = reportTemplateController.generateBillReport(
                btas,
                fromDate,
                toDate,
                institution,
                department,
                site, false, false);
        ap.setName("Petty Cash Payments");
        ap.setBundleType("pettyCashPayments");
        return ap;
    }

    public ReportTemplateRowBundle generateCreditBills() {
        ReportTemplateRowBundle ap;
        List<BillTypeAtomic> btas = new ArrayList<>();
        btas.add(BillTypeAtomic.OPD_BATCH_BILL_WITH_PAYMENT);
        btas.add(BillTypeAtomic.OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER);
        btas.add(BillTypeAtomic.OPD_BATCH_BILL_CANCELLATION);
        btas.add(BillTypeAtomic.OPD_BILL_CANCELLATION);
        btas.add(BillTypeAtomic.OPD_BILL_REFUND);
        btas.add(BillTypeAtomic.PHARMACY_RETAIL_SALE);
        btas.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_PREBILL_SETTLED_AT_CASHIER);

        ap = reportTemplateController.generateBillReport(
                btas,
                fromDate,
                toDate,
                institution,
                department,
                site,
                false,
                true);
        ap.setName("Credit Bills");
        ap.setBundleType("creditBills");
        return ap;
    }

    public ReportTemplateRowBundle generateCreditBillsWithoutProfessionalFees() {
        ReportTemplateRowBundle ap;
        List<BillTypeAtomic> btas = new ArrayList<>();
        btas.add(BillTypeAtomic.OPD_BATCH_BILL_WITH_PAYMENT);
        btas.add(BillTypeAtomic.OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER);
        btas.add(BillTypeAtomic.OPD_BATCH_BILL_CANCELLATION);
        btas.add(BillTypeAtomic.OPD_BILL_CANCELLATION);
        btas.add(BillTypeAtomic.OPD_BILL_REFUND);
        btas.add(BillTypeAtomic.PHARMACY_RETAIL_SALE);
        btas.add(BillTypeAtomic.PHARMACY_RETAIL_SALE_PREBILL_SETTLED_AT_CASHIER);
        ap = reportTemplateController.generateBillReportWithoutProfessionalFees(
                btas,
                fromDate,
                toDate,
                institution,
                department,
                site);
        ap.setName("Credit Bills");
        ap.setBundleType("creditBills");
        return ap;
    }

    public ReportTemplateRowBundle generateSlipPayments() {
        ReportTemplateRowBundle ap;
        ap = reportTemplateController.generatePaymentReport(
                PaymentMethod.Slip,
                fromDate,
                toDate,
                institution,
                department,
                site);
        ap.setName("Slip Payments");
        ap.setBundleType("paymentReportSlip");
        return ap;
    }

    public void updateBillItemValues() {
        bundle = new ReportTemplateRowBundle();
        String jpql = "select bi "
                + " from BillItem bi "
                + " where bi.bill.retired=:br "
                + " and bi.bill.createdAt between :fd and :td ";
        Map m = new HashMap();
        m.put("br", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<BillTypeAtomic> btas = BillTypeAtomic.findByServiceType(ServiceType.OPD);
        if (!btas.isEmpty()) {
            jpql += " and bi.bill.billTypeAtomic in :bts ";
            m.put("bts", btas);
        } else {
            // Handle the case where no bill types are found, perhaps by logging or throwing an exception
        }

        if (department != null) {
            jpql += " and bi.bill.department=:dep ";
            m.put("dep", department);
        }
        if (institution != null) {
            jpql += " and bi.bill.department.institution=:ins ";
            m.put("ins", institution);
        }
        if (site != null) {
            jpql += " and bi.bill.department.site=:site ";
            m.put("site", site);
        }
        List<BillItem> bis = billItemFacade.findByJpql(jpql, m, TemporalType.TIMESTAMP);
        billService.createBillItemFeeBreakdownAsHospitalFeeItemDiscount(bis);
    }

    public void billItemsToBundleForOpdUnderCategory(ReportTemplateRowBundle rtrb, List<BillItem> billItems, PaymentType paymentType) {
        Map<String, ReportTemplateRow> categoryMap = new TreeMap<>();
        Map<String, ReportTemplateRow> itemMap = new TreeMap<>();
        List<ReportTemplateRow> rowsToAdd = new ArrayList<>();
        double totalOpdServiceCollection = 0.0;
        double totalGrossValue = 0.0;
        double totalHospitalFee = 0.0;
        double totalSiscount = 0.0;
        double totalStaffFee = 0.0;
        long totalQuantity = 0l;

        for (BillItem bi : billItems) {

            if (bi.getBill() == null || bi.getBill().getPaymentMethod() == null
                    || bi.getBill().getPaymentMethod().getPaymentType() == PaymentType.NONE) {
                continue;
            }

            if (paymentType == PaymentType.CREDIT) {
                if (bi.getBill().getPaymentMethod().getPaymentType() != PaymentType.CREDIT) {
                    continue;
                }
            } else if (paymentType == PaymentType.NON_CREDIT) {
                if (bi.getBill().getPaymentMethod().getPaymentType() == PaymentType.CREDIT) {
                    continue;
                }

            }

            String categoryName = bi.getItem() != null && bi.getItem().getCategory() != null
                    ? bi.getItem().getCategory().getName() : "No Category";
            String itemName = bi.getItem() != null ? bi.getItem().getName() : "No Item";
            String itemKey = categoryName + "->" + itemName;

            // Initialize the maps if keys are not present
            categoryMap.putIfAbsent(categoryName, new ReportTemplateRow());
            itemMap.putIfAbsent(itemKey, new ReportTemplateRow());

            ReportTemplateRow categoryRow = categoryMap.get(categoryName);
            ReportTemplateRow itemRow = itemMap.get(itemKey);

            // Set category and item details
            if (bi.getItem() != null) {
                categoryRow.setCategory(bi.getItem().getCategory());
                itemRow.setItem(bi.getItem());
            }

            // Initialize financial values
            double grossValue = bi.getGrossValue();
            double hospitalFee = bi.getHospitalFee();
            double discount = bi.getDiscount();
            double staffFee = bi.getStaffFee();
            double netValue = bi.getNetValue();

            // Determine quantity modifier based on bill class type
            long qtyModifier = (bi.getBill().getBillClassType() == BillClassType.CancelledBill
                    || bi.getBill().getBillClassType() == BillClassType.RefundBill) ? -1 : 1;

            // Adjust financial values for cancelled/refunded items
            if (qtyModifier == -1) {
                grossValue = -Math.abs(grossValue);
                hospitalFee = -Math.abs(hospitalFee);
                discount = -Math.abs(discount);
                staffFee = -Math.abs(staffFee);
                netValue = -Math.abs(netValue);
            }

            // Calculate the adjusted quantity
            long quantity = (long) (bi.getQtyAbsolute() * qtyModifier);

            // Accumulate the total collection
            totalOpdServiceCollection += netValue;

            totalGrossValue += grossValue;

            totalHospitalFee += hospitalFee;

            totalSiscount += discount;

            totalStaffFee += staffFee;

            totalQuantity += quantity;

            // Update the rows with the adjusted values
            updateRow(categoryRow, quantity, grossValue, hospitalFee, discount, staffFee, netValue);
            updateRow(itemRow, quantity, grossValue, hospitalFee, discount, staffFee, netValue);
        }

        // Add the rows to the report template bundle
        categoryMap.forEach((categoryName, catRow) -> {
            rowsToAdd.add(catRow);

            itemMap.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(categoryName + "->"))
                    .forEach(entry -> {
                        rowsToAdd.add(entry.getValue());
                    });
        });

        rtrb.getReportTemplateRows().addAll(rowsToAdd);

        rtrb.setTotal(totalOpdServiceCollection);
        rtrb.setGrossTotal(totalGrossValue);
        rtrb.setHospitalTotal(totalHospitalFee);
        rtrb.setStaffTotal(totalStaffFee);
        rtrb.setDiscount(totalSiscount);
        rtrb.setCount(totalQuantity);

    }

    public void billItemsToBundleForOpdCreditUnderCategory(ReportTemplateRowBundle rtrb, List<BillItem> billItems, PaymentType paymentType) {
        // Use TreeMap to maintain alphabetical order
        Map<String, ReportTemplateRow> categoryMap = new TreeMap<>();
        Map<String, ReportTemplateRow> itemMap = new TreeMap<>();
        List<ReportTemplateRow> rowsToAdd = new ArrayList<>();
        double totalOpdServiceCollection = 0.0;
        double totalGrossValue = 0.0;
        double totalHospitalFee = 0.0;
        double totalSiscount = 0.0;
        double totalStaffFee = 0.0;
        long totalQuantity = 0l;

        for (BillItem bi : billItems) {
            PaymentType thisBillItemsBillsPaymentType
                    = null;
            if (bi.getBill() == null || bi.getBill().getPaymentMethod() == null
                    || bi.getBill().getPaymentMethod().getPaymentType() == PaymentType.NONE) {
                continue;
            } else {
                thisBillItemsBillsPaymentType = bi.getBill().getPaymentMethod().getPaymentType();
            }

            if (paymentType == PaymentType.NON_CREDIT) {
                if (thisBillItemsBillsPaymentType == PaymentType.CREDIT) {
                    continue;
                }
            }

            if (paymentType == PaymentType.CREDIT) {
                if (thisBillItemsBillsPaymentType == PaymentType.NON_CREDIT) {
                    continue;
                }
            }

            String categoryName = bi.getItem() != null && bi.getItem().getCategory() != null
                    ? bi.getItem().getCategory().getName() : "No Category";
            String itemName = bi.getItem() != null ? bi.getItem().getName() : "No Item";
            String itemKey = categoryName + "->" + itemName;

            // Initialize the maps if keys are not present
            categoryMap.putIfAbsent(categoryName, new ReportTemplateRow());
            itemMap.putIfAbsent(itemKey, new ReportTemplateRow());

            ReportTemplateRow categoryRow = categoryMap.get(categoryName);
            ReportTemplateRow itemRow = itemMap.get(itemKey);

            // Set category and item details
            if (bi.getItem() != null) {
                categoryRow.setCategory(bi.getItem().getCategory());
                itemRow.setItem(bi.getItem());
            }

            // Initialize financial values
            double grossValue = bi.getGrossValue();
            double hospitalFee = bi.getHospitalFee();
            double discount = bi.getDiscount();
            double staffFee = bi.getStaffFee();
            double netValue = bi.getNetValue();

            // Determine quantity modifier based on bill class type
            long qtyModifier = (bi.getBill().getBillClassType() == BillClassType.CancelledBill
                    || bi.getBill().getBillClassType() == BillClassType.RefundBill) ? -1 : 1;

            // Adjust financial values for cancelled/refunded items
            if (qtyModifier == -1) {
                grossValue = -Math.abs(grossValue);
                hospitalFee = -Math.abs(hospitalFee);
                discount = -Math.abs(discount);
                staffFee = -Math.abs(staffFee);
                netValue = -Math.abs(netValue);
            }

            // Calculate the adjusted quantity
            long quantity = (long) (bi.getQtyAbsolute() * qtyModifier);

            // Accumulate the total collection
            totalOpdServiceCollection += netValue;

            totalGrossValue += grossValue;

            totalHospitalFee += hospitalFee;

            totalSiscount += discount;

            totalStaffFee += staffFee;

            totalQuantity += quantity;

            // Update the rows with the adjusted values
            updateRow(categoryRow, quantity, grossValue, hospitalFee, discount, staffFee, netValue);
            updateRow(itemRow, quantity, grossValue, hospitalFee, discount, staffFee, netValue);
        }

        // Add the rows to the report template bundle
        categoryMap.forEach((categoryName, catRow) -> {
            rowsToAdd.add(catRow);

            itemMap.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(categoryName + "->"))
                    .forEach(entry -> {
                        rowsToAdd.add(entry.getValue());
                    });
        });

        rtrb.getReportTemplateRows().addAll(rowsToAdd);

        rtrb.setTotal(totalOpdServiceCollection);
        rtrb.setGrossTotal(totalGrossValue);
        rtrb.setHospitalTotal(totalHospitalFee);
        rtrb.setStaffTotal(totalStaffFee);
        rtrb.setDiscount(totalSiscount);
        rtrb.setCount(totalQuantity);

    }

    public void billItemsToBundleForOpdUnderCategoryWithoutProfessionalFee(ReportTemplateRowBundle rtrb, List<BillItem> billItems, PaymentType paymentType) {
        // Use TreeMap to maintain alphabetical order
        Map<String, ReportTemplateRow> categoryMap = new TreeMap<String, ReportTemplateRow>();
        Map<String, ReportTemplateRow> itemMap = new TreeMap<String, ReportTemplateRow>();
        List<ReportTemplateRow> rowsToAdd = new ArrayList<>();
        double totalOpdServiceCollection = 0.0;
        double totalGrossValue = 0.0;
        double totalHospitalFee = 0.0;
        double totalDiscount = 0.0;
        double totalStaffFee = 0.0;
        long totalQuantity = 0L;

        for (BillItem bi : billItems) {
            if (bi.getBill() == null || bi.getBill().getPaymentMethod() == null
                    || bi.getBill().getPaymentMethod().getPaymentType() == PaymentType.NONE) {
                continue;
            }

            if (paymentType == PaymentType.CREDIT) {
                if (bi.getBill().getPaymentMethod().getPaymentType() != PaymentType.CREDIT) {
                    continue;
                }
            } else if (paymentType == PaymentType.NON_CREDIT) {
                if (bi.getBill().getPaymentMethod().getPaymentType() == PaymentType.CREDIT) {
                    continue;
                }
            }

            // Identify category and item with null checks
            String categoryName = (bi.getItem() != null && bi.getItem().getCategory() != null && bi.getItem().getCategory().getName() != null)
                    ? bi.getItem().getCategory().getName() : "No Category";
            String itemName = (bi.getItem() != null && bi.getItem().getName() != null) ? bi.getItem().getName() : "No Item";
            String itemKey = categoryName + "->" + itemName;

            // Initialize the maps if keys are not present
            categoryMap.putIfAbsent(categoryName, new ReportTemplateRow());
            itemMap.putIfAbsent(itemKey, new ReportTemplateRow());

            ReportTemplateRow categoryRow = categoryMap.get(categoryName);
            ReportTemplateRow itemRow = itemMap.get(itemKey);

            // Set category and item details
            if (bi.getItem() != null) {
                categoryRow.setCategory(bi.getItem().getCategory());
                itemRow.setItem(bi.getItem());
            }

            // Initialize financial values
            double grossValue = bi.getGrossValue();
            double hospitalFee = bi.getHospitalFee();
            double discount = bi.getDiscount();
            double staffFee = bi.getStaffFee();
            double netValue = bi.getNetValue();

            // Determine quantity modifier based on bill class type
            long qtyModifier = (bi.getBill().getBillClassType() == BillClassType.CancelledBill
                    || bi.getBill().getBillClassType() == BillClassType.RefundBill) ? -1 : 1;

            // Adjust financial values for cancelled/refunded items
            if (qtyModifier == -1) {
                grossValue = -Math.abs(grossValue);
                hospitalFee = -Math.abs(hospitalFee);
                discount = -Math.abs(discount);
                staffFee = -Math.abs(staffFee);
                netValue = -Math.abs(netValue);
            }

            // Calculate the adjusted quantity
            long quantity = (long) (bi.getQtyAbsolute() * qtyModifier);

            // Accumulate the total collection
            totalOpdServiceCollection += hospitalFee - discount;

            totalGrossValue += grossValue;

            totalHospitalFee += hospitalFee;

            totalDiscount += discount;

            totalStaffFee += staffFee;

            totalQuantity += quantity;

            // Update the rows with the adjusted values
            updateRow(categoryRow, quantity, grossValue, hospitalFee, discount, staffFee, netValue);
            updateRow(itemRow, quantity, grossValue, hospitalFee, discount, staffFee, netValue);
        }

        // Add the rows to the report template bundle
        categoryMap.forEach((categoryName, catRow) -> {
            rowsToAdd.add(catRow);

            itemMap.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(categoryName + "->"))
                    .forEach(entry -> {
                        rowsToAdd.add(entry.getValue());
                    });
        });

        rtrb.getReportTemplateRows().addAll(rowsToAdd);

        rtrb.setTotal(totalOpdServiceCollection);
        rtrb.setGrossTotal(totalGrossValue);
        rtrb.setHospitalTotal(totalHospitalFee);
        rtrb.setStaffTotal(totalStaffFee);
        rtrb.setDiscount(totalDiscount);
        rtrb.setCount(totalQuantity);
    }

    public void billToBundleForPatientDeposits(ReportTemplateRowBundle rtrb, List<Bill> bills) {
        double totalDepositCollection = 0.0;
        List<ReportTemplateRow> rows = new ArrayList<>();
        for (Bill b : bills) {
            ReportTemplateRow row = new ReportTemplateRow();
            row.setBill(b);
            totalDepositCollection += b.getNetTotal();
            rows.add(row);
        }
        rtrb.getReportTemplateRows().addAll(rows);
        rtrb.setTotal(totalDepositCollection);
    }

    public void summarizeBillItemsToIncomeByCategory(ReportTemplateRowBundle reportBundle, List<BillItem> billItems) {
        Map<String, ReportTemplateRow> categoryMap = new HashMap<>();
        List<ReportTemplateRow> rowsToAdd = new ArrayList<>();
        double totalNetIncome = 0.0;
        double totalIncome = 0.0;
        double totalDiscount = 0.0;
        double totalHospitalFees = 0.0;
        double totalStaffFees = 0.0;

        for (BillItem iteratingBillItem : billItems) {
            if (iteratingBillItem.getBill() == null) {
                continue;
            }

            String categoryName = iteratingBillItem.getItem() != null && iteratingBillItem.getItem().getCategory() != null ? iteratingBillItem.getItem().getCategory().getName() : "No Category";
            categoryMap.putIfAbsent(categoryName, new ReportTemplateRow());

            ReportTemplateRow categoryRow = categoryMap.get(categoryName);
            if (iteratingBillItem.getItem() != null) {
                categoryRow.setCategory(iteratingBillItem.getItem().getCategory());
            }

            long countModifier = (iteratingBillItem.getBill().getBillClassType() == BillClassType.CancelledBill || iteratingBillItem.getBill().getBillClassType() == BillClassType.RefundBill) ? -1 : 1;
            double grossValue = countModifier * Math.abs(iteratingBillItem.getGrossValue());
            double hospitalFee = countModifier * Math.abs(iteratingBillItem.getHospitalFee());
            double iteratingDiscount = countModifier * Math.abs(iteratingBillItem.getDiscount());
            double staffFee = countModifier * Math.abs(iteratingBillItem.getStaffFee());
            double netValue = 0.0;
            if (withProfessionalFee) {
                netValue = countModifier * Math.abs(iteratingBillItem.getNetValue());
            } else {
                netValue = countModifier * Math.abs(iteratingBillItem.getNetValue() - iteratingBillItem.getStaffFee());
            }

            totalIncome += grossValue;
            totalNetIncome += netValue;
            totalHospitalFees += hospitalFee;
            totalDiscount += iteratingDiscount;
            totalStaffFees += staffFee;
            updateCategoryRow(categoryRow, countModifier, grossValue, hospitalFee, iteratingDiscount, staffFee, netValue);
        }

        // Iterate over categoryMap to add each category's row to rowsToAdd
        categoryMap.forEach((categoryName, catRow) -> rowsToAdd.add(catRow));

        reportBundle.getReportTemplateRows().addAll(rowsToAdd);

        reportBundle.setTotal(totalNetIncome);
        reportBundle.setDiscount(totalDiscount);
        reportBundle.setGrossTotal(totalIncome);
        reportBundle.setHospitalTotal(totalHospitalFees);
        reportBundle.setStaffTotal(totalStaffFees);
    }

    @Deprecated
    public void summarizeBillItemsToIncomeByCategoryWithoutProfessionalFee(ReportTemplateRowBundle reportBundle, List<BillItem> billItems) {
        Map<String, ReportTemplateRow> categoryMap = new HashMap<>();
        List<ReportTemplateRow> rowsToAdd = new ArrayList<>();
        double totalNetIncome = 0.0;
        double totalIncome = 0.0;
        double totalDiscount = 0.0;
        double totalHospitalFees = 0.0;

        for (BillItem loopBillItem : billItems) {
            if (loopBillItem.getBill() == null || loopBillItem.getBill().getPaymentMethod() == null || loopBillItem.getBill().getPaymentMethod().getPaymentType() == PaymentType.NONE) {
                continue;
            }

            String categoryName = loopBillItem.getItem() != null && loopBillItem.getItem().getCategory() != null ? loopBillItem.getItem().getCategory().getName() : "No Category";
            categoryMap.putIfAbsent(categoryName, new ReportTemplateRow());

            ReportTemplateRow categoryRow = categoryMap.get(categoryName);
            if (loopBillItem.getItem() != null) {
                categoryRow.setCategory(loopBillItem.getItem().getCategory());
            }

            long countModifier = (loopBillItem.getBill().getBillClassType() == BillClassType.CancelledBill || loopBillItem.getBill().getBillClassType() == BillClassType.RefundBill) ? -1 : 1;
            double grossValue = countModifier * Math.abs(loopBillItem.getGrossValue());
            double hospitalFee = countModifier * Math.abs(loopBillItem.getHospitalFee());
            double discount = countModifier * Math.abs(loopBillItem.getDiscount());
            double netValue = countModifier * Math.abs(loopBillItem.getNetValue());

            totalIncome += grossValue;
            totalNetIncome += netValue;
            totalHospitalFees += hospitalFee;
            totalDiscount += discount;
            updateCategoryRow(categoryRow, countModifier, grossValue, hospitalFee, discount, netValue);
        }

        // Iterate over categoryMap to add each category's row to rowsToAdd
        categoryMap.forEach((categoryName, catRow) -> rowsToAdd.add(catRow));

        reportBundle.getReportTemplateRows().addAll(rowsToAdd);

        reportBundle.setTotal(totalNetIncome);
        reportBundle.setDiscount(totalDiscount);
        reportBundle.setGrossTotal(totalIncome);
        reportBundle.setHospitalTotal(totalHospitalFees);
    }

    private void updateCategoryRow(ReportTemplateRow row, long countModifier, double grossValue, double hospitalFee, double discount, double professionalFee, double netValue) {

        if (row.getItemCount() == null) {
            row.setItemCount(0L);
        }
        if (row.getItemTotal() == null) {
            row.setItemTotal(0.0);
        }
        if (row.getItemHospitalFee() == null) {
            row.setItemHospitalFee(0.0);
        }
        if (row.getItemDiscountAmount() == null) {
            row.setItemDiscountAmount(0.0);
        }
        if (row.getItemProfessionalFee() == null) {
            row.setItemProfessionalFee(0.0);
        }
        if (row.getItemNetTotal() == null) {
            row.setItemNetTotal(0.0);
        }

        row.setItemCount(row.getItemCount() + countModifier);
        row.setItemTotal(row.getItemTotal() + grossValue);
        row.setItemHospitalFee(row.getItemHospitalFee() + hospitalFee);
        row.setItemDiscountAmount(row.getItemDiscountAmount() + discount);
        row.setItemProfessionalFee(row.getItemProfessionalFee() + professionalFee);
        row.setItemNetTotal(row.getItemNetTotal() + netValue);

    }

    private void updateCategoryRow(ReportTemplateRow row, long countModifier, double grossValue, double hospitalFee, double discount, double netValue) {
        if (row.getItemCount() == null) {
            row.setItemCount(0L);
        }
        if (row.getItemTotal() == null) {
            row.setItemTotal(0.0);
        }
        if (row.getItemHospitalFee() == null) {
            row.setItemHospitalFee(0.0);
        }
        if (row.getItemDiscountAmount() == null) {
            row.setItemDiscountAmount(0.0);
        }

        if (row.getItemNetTotal() == null) {
            row.setItemNetTotal(0.0);
        }

        row.setItemCount(row.getItemCount() + countModifier);
        row.setItemTotal(row.getItemTotal() + grossValue);
        row.setItemHospitalFee(row.getItemHospitalFee() + hospitalFee);
        row.setItemDiscountAmount(row.getItemDiscountAmount() + discount);
        row.setItemNetTotal(row.getItemNetTotal() + netValue);

    }

    public void generateOpdSaleByBill() {
        Map<String, Object> parameters = new HashMap<>();
        String jpql = "SELECT new com.divudi.core.data.ReportTemplateRow("
                + "bill, "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cash THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Card THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.MultiplePaymentMethods THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Credit THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff_Welfare THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Voucher THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.IOU THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Agent THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cheque THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Slip THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.ewallet THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientDeposit THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientPoints THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.OnlineSettlement THEN p.paidValue ELSE 0 END)) "
                + "FROM Payment p "
                + "JOIN p.bill bill "
                + "WHERE p.retired <> :bfr AND bill.retired <> :br ";

        parameters.put("bfr", true);
        parameters.put("br", true);

        List<BillTypeAtomic> bts = BillTypeAtomic.findByServiceType(ServiceType.OPD);
        jpql += "AND bill.billTypeAtomic in :bts ";
        parameters.put("bts", bts);

        if (institution != null) {
            jpql += "AND bill.department.institution = :ins ";
            parameters.put("ins", institution);
        }
        if (department != null) {
            jpql += "AND bill.department = :dep ";
            parameters.put("dep", department);
        }
        if (site != null) {
            jpql += "AND bill.department.site = :site ";
            parameters.put("site", site);
        }
        if (webUser != null) {
            jpql += "AND p.creater = :wu ";
            parameters.put("wu", webUser);
        }
        if (paymentMethod != null) {
            jpql += "AND p.paymentMethod = :pm ";
            parameters.put("pm", paymentMethod);
        }

        jpql += "AND p.createdAt BETWEEN :fd AND :td ";
        parameters.put("fd", fromDate);
        parameters.put("td", toDate);

        jpql += "GROUP BY bill";

        List<ReportTemplateRow> rs = (List<ReportTemplateRow>) paymentFacade.findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

        bundle = new ReportTemplateRowBundle();
        bundle.setReportTemplateRows(rs);
        bundle.createRowValuesFromBill();
        bundle.calculateTotalsWithCredit();
    }

    public void listAgentChannelBookings() {
        String jpql = "SELECT b "
                + " FROM Bill b "
                + " WHERE b.retired = :ret "
                + " AND b.billTypeAtomic IN :bts ";

        Map<String, Object> m = new HashMap<>();
        m.put("ret", false);

        List<BillTypeAtomic> bts = new ArrayList<>();
        bts.add(BillTypeAtomic.CHANNEL_BOOKING_WITH_PAYMENT);
//        bts.add(BillTypeAtomic.CHANNEL_CANCELLATION_WITH_PAYMENT);
//        bts.add(BillTypeAtomic.CHANNEL_REFUND_WITH_PAYMENT);
        m.put("bts", bts);

        jpql += " AND b.paymentMethod = :pm ";
        m.put("pm", PaymentMethod.Agent);

        if (creditCompany != null) {
            jpql += " AND b.creditCompany = :creditcom ";
            m.put("creditcom", creditCompany);
        }

        if (institution != null) {
            jpql += " AND b.institution = :ins ";
            m.put("ins", institution);
        }

        if (site != null) {
            jpql += " AND b.department.site = :site ";
            m.put("site", site);
        }

        if (department != null) {
            jpql += " AND b.department = :dept ";
            m.put("dept", department);
        }

        if (toInstitution != null) {
            jpql += " AND b.toInstitution = :tins ";
            m.put("tins", toInstitution);
        }

        if (toSite != null) {
            jpql += " AND b.toDepartment.site = :tsite ";
            m.put("tsite", toSite);
        }

        if (toDepartment != null) {
            jpql += " AND b.toDepartment = :tdept ";
            m.put("tdept", toDepartment);
        }

        if (webUser != null) {
            jpql += " AND b.creater = :wu ";
            m.put("wu", webUser);
        }

        jpql += " AND b.createdAt BETWEEN :fromDate AND :toDate ";
        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());

        bills = billFacade.findByJpql(jpql, m, TemporalType.TIMESTAMP);

        hosTotal = 0.0;
        staffTotal = 0.0;
        grossTotal = 0.0;
        discountTotal = 0.0;
        amountTotal = 0.0;

        for (Bill b : bills) {
            if (!b.isCancelled() && !b.isRefunded()) {
                hosTotal += b.getHospitalFee();
                staffTotal += b.getStaffFee();
                grossTotal += (b.getHospitalFee() + b.getStaffFee());
                discountTotal += b.getDiscount();
                amountTotal += b.getNetTotal();
            }
        }
    }

    private String bookingType;

    public void generateChannelIncome() {
        Map<String, Object> parameters = new HashMap<>();
        String jpql = "SELECT new com.divudi.core.data.ReportTemplateRow("
                + "bill, "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cash THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Card THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.MultiplePaymentMethods THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Credit THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff_Welfare THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Voucher THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.IOU THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Agent THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cheque THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Slip THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.ewallet THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientDeposit THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientPoints THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.OnlineSettlement THEN p.paidValue ELSE 0 END)) "
                + "FROM Payment p "
                + "JOIN p.bill bill "
                + "WHERE p.retired <> :bfr AND bill.retired <> :br ";

        parameters.put("bfr", true);
        parameters.put("br", true);

        List<BillTypeAtomic> bts = BillTypeAtomic.findByServiceType(ServiceType.CHANNELLING);
        bts.remove(BillTypeAtomic.CHANNEL_BOOKING_WITH_PAYMENT_PENDING_ONLINE);

        if (bookingType != null) {
            switch (bookingType) {
                case "System Bookings":
                    bts.remove(BillTypeAtomic.CHANNEL_BOOKING_FOR_PAYMENT_ONLINE_COMPLETED_PAYMENT);
                    bts.remove(BillTypeAtomic.CHANNEL_CANCELLATION_WITH_PAYMENT_ONLINE_BOOKING);
                    bts.remove(BillTypeAtomic.CHANNEL_AGENT_PAID_TO_HOSPITAL_FOR_ONLINE_BOOKINGS_BILL);
                    bts.remove(BillTypeAtomic.CHANNEL_AGENT_PAID_TO_HOSPITAL_FOR_ONLINE_BOOKINGS_BILL_CANCELLATION);
                    bts.remove(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN);
                    bts.remove(BillTypeAtomic.PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION);
                    break;
                case "Online Bookings":
                    bts = new ArrayList<BillTypeAtomic>();
                    bts.add(BillTypeAtomic.CHANNEL_BOOKING_FOR_PAYMENT_ONLINE_COMPLETED_PAYMENT);
                    bts.add(BillTypeAtomic.CHANNEL_CANCELLATION_WITH_PAYMENT_ONLINE_BOOKING);
                    break;
                case "Agent Bookings":
                    jpql += " And bill.billType = :bt ";
                    parameters.put("bt", BillType.ChannelAgent);
                    bts = new ArrayList<BillTypeAtomic>();
                    bts.add(BillTypeAtomic.CHANNEL_BOOKING_WITH_PAYMENT);
                    bts.add(BillTypeAtomic.CHANNEL_CANCELLATION_WITH_PAYMENT);
                    break;
                default:
                    break;
            }
        }

        //List<BillTypeAtomic> bts = BillTypeAtomic.findByServiceType(ServiceType.CHANNELLING);
        jpql += "AND bill.billTypeAtomic IN :bts ";
        parameters.put("bts", bts);

        if (institution != null) {
            jpql += "AND bill.department.institution = :ins ";
            parameters.put("ins", institution);
        }
        if (department != null) {
            jpql += "AND bill.department = :dep ";
            parameters.put("dep", department);
        }

        if (speciality != null) {
            jpql += "AND bill.staff.speciality = :spec ";
            parameters.put("spec", speciality);
        }
        if (staff != null) {
            jpql += "AND bill.staff = :stf ";
            parameters.put("stf", staff);
        }
        if (webUser != null) {
            jpql += "AND p.creater = :wu ";
            parameters.put("wu", webUser);
        }
        if (paymentMethod != null) {
            jpql += "AND p.paymentMethod = :pm ";
            parameters.put("pm", paymentMethod);
        }

        jpql += "AND p.createdAt BETWEEN :fd AND :td ";
        parameters.put("fd", fromDate);
        parameters.put("td", toDate);

        // Ensure proper grouping
        jpql += "GROUP BY bill";

        List<ReportTemplateRow> rs = (List<ReportTemplateRow>) paymentFacade.findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

        bundle = new ReportTemplateRowBundle();
        bundle.setReportTemplateRows(rs);
        bundle.createRowValuesFromBill();
        bundle.calculateTotals();

    }

    public void billItemsToItamizedSaleReport(ReportTemplateRowBundle rtrb, List<BillItem> billItems) {
        Map<String, ReportTemplateRow> categoryMap = new HashMap<>();
        Map<String, ReportTemplateRow> itemSummaryMap = new HashMap<>();
        Map<String, List<ReportTemplateRow>> detailedBillItemRows = new HashMap<>();
        List<ReportTemplateRow> rowsToAdd = new ArrayList<>();
        double totalOpdServiceCollection = 0.0;

        for (BillItem bi : billItems) {

            if (bi.getBill() == null || bi.getBill().getPaymentMethod() == null || bi.getBill().getPaymentMethod().getPaymentType() == PaymentType.NONE) {
                if (bi.getBill().getPaymentMethod() == null) {
                    if (bi.getBill().getPatientEncounter() == null) {
                        continue;
                    }
                } else {
                    continue;
                }
            }

            String categoryName = bi.getItem() != null && bi.getItem().getCategory() != null
                    ? bi.getItem().getCategory().getName()
                    : "No Category";
            String itemName = bi.getItem() != null ? bi.getItem().getName() : "No Item";
            String itemKey = categoryName + "->" + itemName;

            categoryMap.putIfAbsent(categoryName, new ReportTemplateRow());
            itemSummaryMap.putIfAbsent(itemKey, new ReportTemplateRow());
            detailedBillItemRows.putIfAbsent(itemKey, new ArrayList<>());

            // Summary Row for item categories
            if (bi.getItem() != null) {
                categoryMap.get(categoryName).setCategory(bi.getItem().getCategory());
                itemSummaryMap.get(itemKey).setItem(bi.getItem());
            }

            // Create a detailed row for each BillItem without item details to avoid redundancy
            ReportTemplateRow detailedRow = new ReportTemplateRow();
            detailedRow.setBillItem(bi);  // Assuming a method to set other attributes from BillItem

            double total = bi.getGrossValue();
            double hospitalFee = bi.getHospitalFee();
            double discount = bi.getDiscount();
            double professionalFee = bi.getStaffFee();
            double netTotal = 0.0;
            if (withProfessionalFee) {
                netTotal = bi.getNetValue();
            } else {
                netTotal = bi.getNetValue() - bi.getStaffFee();
            }

            long countModifier = (bi.getBill().getBillClassType() == BillClassType.CancelledBill
                    || bi.getBill().getBillClassType() == BillClassType.RefundBill) ? -1 : 1;

            long count = (long) bi.getQtyAbsolute() * countModifier;

            if (countModifier == -1) {
                total = -Math.abs(total);
                hospitalFee = -Math.abs(hospitalFee);
                discount = -Math.abs(discount);
                professionalFee = -Math.abs(professionalFee);
                netTotal = -Math.abs(netTotal);
            }

            totalOpdServiceCollection += netTotal;

            // Update the detailed row
            updateRow(detailedRow, count, total, hospitalFee, discount, professionalFee, netTotal);

            // Add the detailed row to the list for this item
            detailedBillItemRows.get(itemKey).add(detailedRow);

            // Update category summary row
            updateRow(categoryMap.get(categoryName), count, total, hospitalFee, discount, professionalFee, netTotal);

            // Update item summary row
            updateRow(itemSummaryMap.get(itemKey), count, total, hospitalFee, discount, professionalFee, netTotal);
        }

        // Add category rows and item summary rows, then each individual detailed bill item row within each item
        categoryMap.forEach((categoryName, catRow) -> {
            rowsToAdd.add(catRow);
            itemSummaryMap.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(categoryName + "->"))
                    .forEach(entry -> {
                        rowsToAdd.add(entry.getValue());
                        List<ReportTemplateRow> billItemRows = detailedBillItemRows.get(entry.getKey());
                        billItemRows.forEach(rowsToAdd::add);
                    });
        });

        rtrb.getReportTemplateRows().addAll(rowsToAdd);
        rtrb.setTotal(totalOpdServiceCollection);
    }

    public ReportTemplateRowBundle billItemsToBundleForOpd(ReportTemplateRowBundle rtrb, List<BillItem> billItems) {
        List<ReportTemplateRow> rowsToAdd = new ArrayList<>();
        long count = 1;
        double grossTotal = 0.0;
        double hospitalTotal = 0.0;
        double discountTotal = 0.0;
        double staffTotal = 0.0;
        double netTotal = 0.0;

        for (BillItem bi : billItems) {

            ReportTemplateRow row = new ReportTemplateRow(bi);
            switch (bi.getBill().getBillClassType()) {
                case CancelledBill:
                case RefundBill:
                    count--;
                    grossTotal -= Math.abs(bi.getFeeValue());
                    hospitalTotal -= Math.abs(bi.getHospitalFee());
                    discountTotal -= Math.abs(bi.getDiscount());
                    staffTotal -= Math.abs(bi.getStaffFee());
                    netTotal -= Math.abs(bi.getNetValue());
                    break;
                case BilledBill:
                case Bill:
                    count++;
                    grossTotal += Math.abs(bi.getFeeValue());
                    hospitalTotal += Math.abs(bi.getHospitalFee());
                    discountTotal += Math.abs(bi.getDiscount());
                    staffTotal += Math.abs(bi.getStaffFee());
                    netTotal += Math.abs(bi.getNetValue());
                    break;
                default:
                    continue; // Skip processing for unrecognized or unhandled bill types
            }

            rowsToAdd.add(row);
        }

        ReportTemplateRowBundle biBundle = new ReportTemplateRowBundle(rowsToAdd);
        biBundle.setTotal(netTotal);
        biBundle.setGrossTotal(grossTotal);
        biBundle.setHospitalTotal(hospitalTotal);
        biBundle.setStaffTotal(staffTotal);
        biBundle.setDiscount(discountTotal);

        return biBundle;
    }

    private void updateRow(ReportTemplateRow row, long count, double total, double hospitalFee, double discount, double professionalFee, double netTotal) {

        if (row.getItemCount() == null) {
            row.setItemCount(0L);
        }
        if (row.getItemTotal() == null) {
            row.setItemTotal(0.0);
        }
        if (row.getItemHospitalFee() == null) {
            row.setItemHospitalFee(0.0);
        }
        if (row.getItemDiscountAmount() == null) {
            row.setItemDiscountAmount(0.0);
        }
        if (row.getItemProfessionalFee() == null) {
            row.setItemProfessionalFee(0.0);
        }
        if (row.getItemNetTotal() == null) {
            row.setItemNetTotal(0.0);
        }

        row.setItemCount(row.getItemCount() + count);
        row.setItemTotal(row.getItemTotal() + total);
        row.setItemHospitalFee(row.getItemHospitalFee() + hospitalFee);
        row.setItemDiscountAmount(row.getItemDiscountAmount() + discount);
        row.setItemProfessionalFee(row.getItemProfessionalFee() + professionalFee);
        row.setItemNetTotal(row.getItemNetTotal() + netTotal);

    }

    public void generateAllCashierSummary() {
        reportTimerController.trackReportExecution(() -> {
            Map<String, Object> parameters = new HashMap<>();
            String jpql = "SELECT new com.divudi.core.data.ReportTemplateRow("
                    + "bill.department, FUNCTION('date', p.createdAt), p.creater, "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cash THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Card THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.MultiplePaymentMethods THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Credit THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff_Welfare THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Voucher THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.IOU THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Agent THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cheque THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Slip THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.ewallet THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientDeposit THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientPoints THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.OnlineSettlement THEN p.paidValue ELSE 0 END)) "
                    + "FROM Payment p "
                    + "JOIN p.bill bill "
                    + "WHERE p.retired <> :bfr AND bill.retired <> :br ";

            parameters.put("bfr", true);
            parameters.put("br", true);
            //Start - Atomic Bill Type with Cash in and cash out added for 10088-all-cashier-summary-had-calculated-fund-bills
            List<BillTypeAtomic> btas = BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_IN);
            btas.addAll(BillTypeAtomic.findByFinanceType(BillFinanceType.CASH_OUT));
            jpql += "AND bill.billTypeAtomic in :btas ";
            parameters.put("btas", btas);
            // End - Atomic Bill Type with Cash in and cash out added FOR 10088-all-cashier-summary-had-calculated-fund-bills
            if (institution != null) {
                jpql += "AND bill.department.institution = :ins ";
                parameters.put("ins", institution);
            }
            if (department != null) {
                jpql += "AND bill.department = :dep ";
                parameters.put("dep", department);
            }
            if (site != null) {
                jpql += "AND bill.department.site = :site ";
                parameters.put("site", site);
            }
            if (webUser != null) {
                jpql += "AND p.creater = :wu ";
                parameters.put("wu", webUser);
            }
            if (paymentMethod != null) {
                jpql += "AND p.paymentMethod = :pm ";
                parameters.put("pm", paymentMethod);
            }

            jpql += "AND p.createdAt BETWEEN :fd AND :td ";
            parameters.put("fd", fromDate);
            parameters.put("td", toDate);

            jpql += "GROUP BY bill.department, FUNCTION('date', p.createdAt), p.creater";

            List<ReportTemplateRow> rs = (List<ReportTemplateRow>) paymentFacade.findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

            bundle = new ReportTemplateRowBundle();
            bundle.setReportTemplateRows(rs);
            bundle.calculateTotalsWithCredit();
        }, CashierReports.All_CASHIER_SUMMARY, sessionController.getLoggedUser());
    }

    public void generateDepartmentRevenueReport() {
        Map<String, Object> parameters = new HashMap<>();
        String jpql = "SELECT new com.divudi.core.data.ReportTemplateRow("
                + "bill.toDepartment, bill.billTypeAtomic, "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cash THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Card THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.MultiplePaymentMethods THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Credit THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff_Welfare THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Voucher THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.IOU THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Agent THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cheque THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Slip THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.ewallet THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientDeposit THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientPoints THEN p.paidValue ELSE 0 END), "
                + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.OnlineSettlement THEN p.paidValue ELSE 0 END)) "
                + "FROM Payment p "
                + "JOIN p.bill bill "
                + "WHERE p.retired <> :bfr AND bill.retired <> :br ";

        parameters.put("bfr", true);
        parameters.put("br", true);

        if (institution != null) {
            jpql += "AND bill.department.institution = :ins ";
            parameters.put("ins", institution);
        }
        if (department != null) {
            jpql += "AND bill.department = :dep ";
            parameters.put("dep", department);
        }
        if (site != null) {
            jpql += "AND bill.department.site = :site ";
            parameters.put("site", site);
        }

        if (toInstitution != null) {
            jpql += "AND bill.toDepartment.institution = :ins ";
            parameters.put("ins", institution);
        }
        if (toDepartment != null) {
            jpql += "AND bill.toDepartment = :dep ";
            parameters.put("dep", department);
        }
        if (toSite != null) {
            jpql += "AND bill.toDepartment.site = :site ";
            parameters.put("site", site);
        }

        jpql += "AND p.createdAt BETWEEN :fd AND :td ";
        parameters.put("fd", fromDate);
        parameters.put("td", toDate);

        jpql += "GROUP BY bill.toDepartment, bill.billTypeAtomic";

        List<ReportTemplateRow> rs = (List<ReportTemplateRow>) paymentFacade.findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

        bundle = new ReportTemplateRowBundle();
        bundle.setReportTemplateRows(rs);
        bundle.calculateTotalsWithCredit();
    }

    public void generateTotalCashierSummary() {
        reportTimerController.trackReportExecution(() -> {

            Map<String, Object> parameters = new HashMap<>();
            String jpql = "SELECT new com.divudi.core.data.ReportTemplateRow("
                    + "bill.department, FUNCTION('date', p.createdAt), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cash THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Card THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.MultiplePaymentMethods THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Credit THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Staff_Welfare THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Voucher THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.IOU THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Agent THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Cheque THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.Slip THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.ewallet THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientDeposit THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.PatientPoints THEN p.paidValue ELSE 0 END), "
                    + "SUM(CASE WHEN p.paymentMethod = com.divudi.core.data.PaymentMethod.OnlineSettlement THEN p.paidValue ELSE 0 END)) "
                    + "FROM Payment p "
                    + "JOIN p.bill bill "
                    + "WHERE p.retired <> :bfr AND bill.retired <> :br ";

            parameters.put("bfr", true);
            parameters.put("br", true);

            if (institution != null) {
                jpql += "AND bill.department.institution = :ins ";
                parameters.put("ins", institution);
            }
            if (department != null) {
                jpql += "AND bill.department = :dep ";
                parameters.put("dep", department);
            }
            if (site != null) {
                jpql += "AND bill.department.site = :site ";
                parameters.put("site", site);
            }
            if (webUser != null) {
                jpql += "AND p.creater = :wu ";
                parameters.put("wu", webUser);
            }
            if (paymentMethod != null) {
                jpql += "AND p.paymentMethod = :pm ";
                parameters.put("pm", paymentMethod);
            }

            jpql += "AND p.createdAt BETWEEN :fd AND :td ";
            parameters.put("fd", fromDate);
            parameters.put("td", toDate);

            jpql += "GROUP BY bill.department, FUNCTION('date', p.createdAt)";

            List<ReportTemplateRow> rs = (List<ReportTemplateRow>) paymentFacade.findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

            bundle = new ReportTemplateRowBundle();
            bundle.setReportTemplateRows(rs);
            bundle.calculateTotalsWithCredit();

        }, CashierReports.TOTAL_CASHIER_SUMMARY, sessionController.getLoggedUser());
    }

    public void generateShiftStartEndSummary() {
        reportTimerController.trackReportExecution(() -> {
            Map<String, Object> parameters = new HashMap<>();
            String jpql = "SELECT b"
                    + " FROM Bill b"
                    + " WHERE b.retired =:ret";

            parameters.put("ret", false);

            if (institution != null) {
                jpql += " AND b.department.institution=:ins";
                parameters.put("ins", institution);
            }
            if (department != null) {
                jpql += " AND b.department=:dep";
                parameters.put("dep", department);
            }
            if (site != null) {
                jpql += " AND b.department.site=:site";
                parameters.put("site", site);
            }
            if (webUser != null) {
                jpql += " AND b.creater =:wu";
                parameters.put("wu", webUser);
            }
            jpql += " AND b.billTypeAtomic=:bta ";
            parameters.put("bta", BillTypeAtomic.FUND_SHIFT_END_BILL);

            jpql += " AND b.createdAt BETWEEN :fd AND :td";
            parameters.put("fd", fromDate);
            parameters.put("td", toDate);

            bills = billFacade.findByJpql(jpql, parameters, TemporalType.TIMESTAMP);
        }, CashierReports.CASHIER_SHIFT_END_SUMMARY, sessionController.getLoggedUser());
    }

    public SearchController() {
    }

    public class PharmacyAdjustmentRow {

        Item itm;
        double purchaseRate;
        double saleRate;
        double befoerQty;
        double afterQty;
        double adjusetedQty;
        double befoerVal;
        double afterVal;
        double adjusetedVal;
        String batchNo;
        Date expiry;

        public PharmacyAdjustmentRow() {
        }

        public PharmacyAdjustmentRow(Item itm, double purchaseRate, double saleRate, double befoerQty, double afterQty, double adjusetedQty, String batchNo, Date expiry) {
            this.itm = itm;
            this.purchaseRate = purchaseRate;
            this.saleRate = saleRate;
            this.befoerQty = befoerQty;
            this.afterQty = afterQty;
            this.adjusetedQty = adjusetedQty;
            this.batchNo = batchNo;
            this.expiry = expiry;
            this.adjusetedVal = adjusetedQty * purchaseRate;
            this.befoerVal = befoerQty * purchaseRate;
            this.afterVal = afterQty * purchaseRate;
        }

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
        public Item getItm() {
            return itm;
        }

        public void setItm(Item itm) {
            this.itm = itm;
        }

        public double getPurchaseRate() {
            return purchaseRate;
        }

        public void setPurchaseRate(double purchaseRate) {
            this.purchaseRate = purchaseRate;
        }

        public double getSaleRate() {
            return saleRate;
        }

        public void setSaleRate(double saleRate) {
            this.saleRate = saleRate;
        }

        public double getBefoerQty() {
            return befoerQty;
        }

        public void setBefoerQty(double befoerQty) {
            this.befoerQty = befoerQty;
        }

        public double getAfterQty() {
            return afterQty;
        }

        public void setAfterQty(double afterQty) {
            this.afterQty = afterQty;
        }

        public double getAdjusetedQty() {
            return adjusetedQty;
        }

        public void setAdjusetedQty(double adjusetedQty) {
            this.adjusetedQty = adjusetedQty;
        }

        public String getBatchNo() {
            return batchNo;
        }

        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }

        public Date getExpiry() {
            return expiry;
        }

        public void setExpiry(Date expiry) {
            this.expiry = expiry;
        }

        public double getBefoerVal() {
            return befoerVal;
        }

        public void setBefoerVal(double befoerVal) {
            this.befoerVal = befoerVal;
        }

        public double getAfterVal() {
            return afterVal;
        }

        public void setAfterVal(double afterVal) {
            this.afterVal = afterVal;
        }

        public double getAdjusetedVal() {
            return adjusetedVal;
        }

        public void setAdjusetedVal(double adjusetedVal) {
            this.adjusetedVal = adjusetedVal;
        }

    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = CommonFunctions.getEndOfDay(new Date());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonFunctions.getStartOfDay(new Date());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public SearchKeyword getSearchKeyword() {
        if (searchKeyword == null) {
            searchKeyword = new SearchKeyword();
        }
        return searchKeyword;
    }

    public void setSearchKeyword(SearchKeyword searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public List<Bill> getFilteredBills() {
        return filteredBills;
    }

    public void setFilteredBills(List<Bill> filteredBills) {
        this.filteredBills = filteredBills;
    }

    public List<PharmacyTransferRequestListDTO> getTransferRequestDtos() {
        return transferRequestDtos;
    }

    public void setTransferRequestDtos(List<PharmacyTransferRequestListDTO> transferRequestDtos) {
        this.transferRequestDtos = transferRequestDtos;
    }

    public List<PharmacyItemPurchaseDTO> getDisposalIssueBillDtos() {
        return disposalIssueBillDtos;
    }

    public void setDisposalIssueBillDtos(List<PharmacyItemPurchaseDTO> disposalIssueBillDtos) {
        this.disposalIssueBillDtos = disposalIssueBillDtos;
    }

    public List<PharmacyItemPurchaseDTO> getDisposalIssueBillItemDtos() {
        return disposalIssueBillItemDtos;
    }

    public void setDisposalIssueBillItemDtos(List<PharmacyItemPurchaseDTO> disposalIssueBillItemDtos) {
        this.disposalIssueBillItemDtos = disposalIssueBillItemDtos;
    }

    public Long getSelectedRequestId() {
        return selectedRequestId;
    }

    /**
     * Setter used by views to pass only the bill id. The full Bill entity is
     * fetched here to keep compatibility with existing methods expecting the
     * entity instance.
     */
    public void setSelectedRequestId(Long selectedRequestId) {
        this.selectedRequestId = selectedRequestId;
        if (selectedRequestId != null) {
            this.selectedRequest = billFacade.find(selectedRequestId);
        } else {
            this.selectedRequest = null;
        }
    }

    public Bill getSelectedRequest() {
        return selectedRequest;
    }

    public void setSelectedRequest(Bill selectedRequest) {
        this.selectedRequest = selectedRequest;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public List<BillFee> getBillFees() {
        return billFees;
    }

    public void setBillFees(List<BillFee> billFees) {
        this.billFees = billFees;
    }

    public List<PatientInvestigation> getPatientInvestigations() {
        return patientInvestigations;
    }

    public void setPatientInvestigations(List<PatientInvestigation> patientInvestigations) {
        this.patientInvestigations = patientInvestigations;
    }

    public PatientInvestigationFacade getPatientInvestigationFacade() {
        return patientInvestigationFacade;
    }

    public void setPatientInvestigationFacade(PatientInvestigationFacade patientInvestigationFacade) {
        this.patientInvestigationFacade = patientInvestigationFacade;
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public int getMaxResult() {

        return maxResult;
    }

    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    public List<Bill> getSelectedBills() {
        if (selectedBills == null) {
            selectedBills = new ArrayList<>();
        }
        return selectedBills;
    }

    public void setSelectedBills(List<Bill> selectedBills) {
        this.selectedBills = selectedBills;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public BillType getBillType() {
        return billType;
    }

    public void setBillType(BillType billType) {
        this.billType = billType;
    }

    public TransferController getTransferController() {
        return transferController;
    }

    public void setTransferController(TransferController transferController) {
        this.transferController = transferController;
    }

    public List<PatientInvestigation> getPatientInvestigationsSigle() {
        if (patientInvestigationsSigle == null) {
            createPatientInvestigationsTableSingle();
        }
        return patientInvestigationsSigle;
    }

    public void setPatientInvestigationsSigle(List<PatientInvestigation> patientInvestigationsSigle) {
        this.patientInvestigationsSigle = patientInvestigationsSigle;
    }

    public Bill getCancellingIssueBill() {
        return cancellingIssueBill;
    }

    public void setCancellingIssueBill(Bill cancellingIssueBill) {
        this.cancellingIssueBill = cancellingIssueBill;
    }

    public double getNetTotalValue() {
        return netTotalValue;
    }

    public void setNetTotalValue(double netTotalValue) {
        this.netTotalValue = netTotalValue;
    }

    public List<BillFee> getBillFeesDone() {
        return billFeesDone;
    }

    public void setBillFeesDone(List<BillFee> billFeesDone) {
        this.billFeesDone = billFeesDone;
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

    public PatientEncounter getPatientEncounter() {
        return patientEncounter;
    }

    public void setPatientEncounter(PatientEncounter patientEncounter) {
        this.patientEncounter = patientEncounter;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public double getDueTotal() {
        return dueTotal;
    }

    public void setDueTotal(double dueTotal) {
        this.dueTotal = dueTotal;
    }

    public double getDoneTotal() {
        return doneTotal;
    }

    public void setDoneTotal(double doneTotal) {
        this.doneTotal = doneTotal;
    }

    public double getTotalPaying() {
        return totalPaying;
    }

    public void setTotalPaying(double totalPaying) {
        this.totalPaying = totalPaying;
    }

    public List<PatientInvestigation> getUserPatientInvestigations() {
        return userPatientInvestigations;
    }

    public void setUserPatientInvestigations(List<PatientInvestigation> userPatientInvestigations) {
        this.userPatientInvestigations = userPatientInvestigations;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public List<Bill> getAceptPaymentBills() {
        if (aceptPaymentBills == null) {
            aceptPaymentBills = new ArrayList<>();
        }
        return aceptPaymentBills;
    }

    public void setAceptPaymentBills(List<Bill> aceptPaymentBills) {
        this.aceptPaymentBills = aceptPaymentBills;
    }

    public String getMenuBarSearchText() {
        return menuBarSearchText;
    }

    public void setMenuBarSearchText(String menuBarSearchText) {
        this.menuBarSearchText = menuBarSearchText;
    }

    public boolean isChannelingPanelVisible() {
        return channelingPanelVisible;
    }

    public void setChannelingPanelVisible(boolean channelingPanelVisible) {
        this.channelingPanelVisible = channelingPanelVisible;
    }

    public boolean isPharmacyPanelVisible() {
        return pharmacyPanelVisible;
    }

    public void setPharmacyPanelVisible(boolean pharmacyPanelVisible) {
        this.pharmacyPanelVisible = pharmacyPanelVisible;
    }

    public boolean isOpdPanelVisible() {
        return opdPanelVisible;
    }

    public void setOpdPanelVisible(boolean opdPanelVisible) {
        this.opdPanelVisible = opdPanelVisible;
    }

    public boolean isInwardPanelVisible() {
        return inwardPanelVisible;
    }

    public void setInwardPanelVisible(boolean inwardPanelVisible) {
        this.inwardPanelVisible = inwardPanelVisible;
    }

    public boolean isLabPanelVisile() {
        return labPanelVisile;
    }

    public void setLabPanelVisile(boolean labPanelVisile) {
        this.labPanelVisile = labPanelVisile;
    }

    public boolean isPatientPanelVisible() {
        return patientPanelVisible;
    }

    public void setPatientPanelVisible(boolean patientPanelVisible) {
        this.patientPanelVisible = patientPanelVisible;
    }

    public List<Bill> getChannellingBills() {
        return channellingBills;
    }

    public void setChannellingBills(List<Bill> channellingBills) {
        this.channellingBills = channellingBills;
    }

    public List<Bill> getOpdBills() {
        return opdBills;
    }

    public void setOpdBills(List<Bill> opdBills) {
        this.opdBills = opdBills;
    }

    public List<Bill> getPharmacyBills() {
        return pharmacyBills;
    }

    public void setPharmacyBills(List<Bill> pharmacyBills) {
        this.pharmacyBills = pharmacyBills;
    }

    public List<Admission> getAdmissions() {
        return admissions;
    }

    public void setAdmissions(List<Admission> admissions) {
        this.admissions = admissions;
    }

    public List<PatientInvestigation> getPis() {
        return pis;
    }

    public void setPis(List<PatientInvestigation> pis) {
        this.pis = pis;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    public List<BillSession> getBillSessions() {
        return billSessions;
    }

    public void setBillSessions(List<BillSession> billSessions) {
        this.billSessions = billSessions;
    }

    public BillSession getSelectedBillSession() {
        return selectedBillSession;
    }

    public void setSelectedBillSession(BillSession selectedBillSession) {
        this.selectedBillSession = selectedBillSession;
    }

    public List<String> getTelephoneNumbers() {
        return telephoneNumbers;
    }

    public void setTelephoneNumbers(List<String> telephoneNumbers) {
        this.telephoneNumbers = telephoneNumbers;
    }

    public List<String> getSelectedTelephoneNumbers() {
        return selectedTelephoneNumbers;
    }

    public void setSelectedTelephoneNumbers(List<String> selectedTelephoneNumbers) {
        this.selectedTelephoneNumbers = selectedTelephoneNumbers;
    }

    boolean paginator = true;
    int rows = 20;

    public boolean isPaginator() {
        return paginator;
    }

    public void setPaginator(boolean paginator) {
        this.paginator = paginator;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getSmsText() {
        return smsText;
    }

    public void setSmsText(String smsText) {
        this.smsText = smsText;
    }

    public String getUniqueSmsText() {
        return uniqueSmsText;
    }

    public void setUniqueSmsText(String uniqueSmsText) {
        this.uniqueSmsText = uniqueSmsText;
    }

    public ReportKeyWord getReportKeyWord() {
        if (reportKeyWord == null) {
            reportKeyWord = new ReportKeyWord();
        }
        return reportKeyWord;
    }

    public void setReportKeyWord(ReportKeyWord reportKeyWord) {
        this.reportKeyWord = reportKeyWord;
    }

    public StockFacade getStockFacade() {
        return stockFacade;
    }

    public void setStockFacade(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    public List<PharmacyAdjustmentRow> getPharmacyAdjustmentRows() {
        return pharmacyAdjustmentRows;
    }

    public void setPharmacyAdjustmentRows(List<PharmacyAdjustmentRow> pharmacyAdjustmentRows) {
        this.pharmacyAdjustmentRows = pharmacyAdjustmentRows;
    }

    private StreamedContent fileBillsAndBillItemsForDownload;

    public StreamedContent getFileBillsAndBillItemsForDownload() {
        prepareDataBillsAndBillItemsDownload();  // Prepare data and create the Excel file
        return fileBillsAndBillItemsForDownload;
    }

    public String navigateBack() {
        return backLink;
    }

    public List<BillTypeAtomic> prepareDistinctBillTypeAtomic() {
        String jpql = "SELECT DISTINCT b.billTypeAtomic FROM Bill b JOIN b.payments p";
        List<?> results = billFacade.findLightsByJpql(jpql);
        List<BillTypeAtomic> billTypeAtomics = new ArrayList<>();

        for (Object result : results) {
            if (result instanceof BillTypeAtomic) {
                billTypeAtomics.add((BillTypeAtomic) result);
            }
        }

        return billTypeAtomics;
    }

    public StreamedContent getFileForBillsAndBillItemsForDownload() {
        prepareDataBillsAndBillItemsDownload();  // Prepare data and create the Excel file
        return fileBillsAndBillItemsForDownload;       // This should now contain the generated Excel file
    }

    public void createBillsBillItemsList(Set<Bill> bills, List<BillItem> billItems) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Bills Details");

        int rowIdx = 0;
        Row headerRow = sheet.createRow(rowIdx++);
        // Headers for both bills and bill items
        String[] headers = {
            "Bill ID", "Institution Name", "Department Name", "Patient Name", "Staff Name", "User", "Bill Type", "Total", "Discount", "Net Total", "Payment Method",
            "Bill Item Name", "Bill Item Code", "Item Type", "Quantity", "Rate", "Gross Value", "Bill Item Discount", "Net Value"
        };
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Fill the sheet with bill data first, then bill item data in subsequent rows
        for (Bill bill : bills) {
            Row billRow = sheet.createRow(rowIdx++);
            int colIdx = 0;
            billRow.createCell(colIdx++).setCellValue(bill.getId() != null ? bill.getId().toString() : "");
            billRow.createCell(colIdx++).setCellValue(bill.getInstitution() != null ? bill.getInstitution().getName() : "");
            billRow.createCell(colIdx++).setCellValue(bill.getDepartment() != null ? bill.getDepartment().getName() : "");
            billRow.createCell(colIdx++).setCellValue(bill.getPatient() != null && bill.getPatient().getPerson() != null ? bill.getPatient().getPerson().getName() : "");
            billRow.createCell(colIdx++).setCellValue(bill.getStaff() != null && bill.getStaff().getPerson() != null ? bill.getStaff().getPerson().getNameWithTitle() : "");
            billRow.createCell(colIdx++).setCellValue(bill.getCreater() != null && bill.getCreater().getWebUserPerson() != null ? bill.getCreater().getWebUserPerson().getName() : "");
            billRow.createCell(colIdx++).setCellValue(bill.getBillTypeAtomic() != null ? bill.getBillTypeAtomic().getLabel() : "");
            billRow.createCell(colIdx++).setCellValue(bill.getGrantTotal());
            billRow.createCell(colIdx++).setCellValue(bill.getDiscount());
            billRow.createCell(colIdx++).setCellValue(bill.getNetTotal());
            billRow.createCell(colIdx++).setCellValue(bill.getPaymentMethod() != null ? bill.getPaymentMethod().getLabel() : "");

            // Leave the bill item columns empty in the bill row
            for (int j = 10; j < headers.length; j++) {
                billRow.createCell(j);
            }

            // Fill in bill item data in subsequent rows
            for (BillItem bi : billItems) {
                if (bi.getBill().equals(bill)) {
                    Row itemRow = sheet.createRow(rowIdx++);
                    // Leave bill details columns empty
                    for (int j = 0; j < 10; j++) {
                        itemRow.createCell(j);
                    }

                    int itemColIdx = 10;
                    try {
                        itemRow.createCell(itemColIdx++).setCellValue(bi.getItem() != null ? bi.getItem().getName() : "");
                        itemRow.createCell(itemColIdx++).setCellValue(bi.getItem() != null ? bi.getItem().getCode() : "");
                        itemRow.createCell(itemColIdx++).setCellValue(bi.getItem() != null ? bi.getItem().getItemType() != null ? bi.getItem().getItemType().toString() : "" : "");
                        itemRow.createCell(itemColIdx++).setCellValue(bi.getQty() != null ? bi.getQty().toString() : "");
                        itemRow.createCell(itemColIdx++).setCellValue(bi.getRate());
                        itemRow.createCell(itemColIdx++).setCellValue(bi.getGrossValue());
                        itemRow.createCell(itemColIdx++).setCellValue(bi.getDiscount());
                        itemRow.createCell(itemColIdx++).setCellValue(bi.getNetValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Autosize columns to fit content
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to output stream and create download content
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        fileBillsAndBillItemsForDownload = DefaultStreamedContent.builder()
                .name("bills_and_bill_items.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> new ByteArrayInputStream(outputStream.toByteArray()))
                .build();
    }

    public void prepareDataBillsAndBillItemsDownload(boolean old) {
        // Fetch all bills and their associated bill items, excluding fees
        List<BillTypeAtomic> billTypeAtomics = prepareDistinctBillTypeAtomic();
        String jpql = "SELECT b FROM Bill b JOIN FETCH b.billItems WHERE b.createdAt and b.retired=:ret and b.billTypeAtomic in :btas BETWEEN :fromDate AND :toDate";
        Map<String, Object> params = new HashMap<>();
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);
        params.put("ret", false);
        params.put("btas", billTypeAtomics);

        List<Bill> bills = billFacade.findByJpql(jpql, params); // Assuming you have a facade to execute JPQL queries

        Set<Bill> uniqueBills = new HashSet<>(bills);
        List<BillItem> allBillItems = new ArrayList<>();

        bills = new ArrayList<>();
        // Only extract bill items, no fees
        for (Bill bill : uniqueBills) {
            allBillItems.addAll(bill.getBillItems());
            bills.add(bill);
        }

        try {
            createBillsBillItemsList(uniqueBills, allBillItems);
        } catch (IOException e) {
            e.printStackTrace(); // Handle exceptions properly
        }
    }

    public StreamedContent getBundleAsPdf() {
        StreamedContent pdfSc = null;
        try {
            pdfSc = pdfController.createPdfForBundle(bundle);
        } catch (IOException e) {
            // Handle IOException
        }
        return pdfSc;
    }

    public StreamedContent getBundleAsExcel() {
        try {
            downloadingExcel = excelController.createExcelForBundle(bundle);
        } catch (IOException e) {
            // Handle IOException
        }
        return downloadingExcel;
    }

    public void directPurchaseOrderSearch() {
        bills = null;
        Map<String, Object> m = new HashMap<>();

        String jpql = "SELECT b FROM Bill b WHERE b.retired = false AND b.billType = :bTp "
                + "AND b.createdAt BETWEEN :fromDate AND :toDate";

        m.put("bTp", BillType.StorePurchase);
        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());

        bills = billFacade.findByJpql(jpql, m);

    }

    public void prepareDataBillsAndBillItemsDownload() {
        // JPQL to fetch Bills and their BillItems through Payments
        String jpql = "SELECT DISTINCT b "
                + " FROM Payment p "
                + " JOIN p.bill b "
                + " JOIN FETCH b.billItems bi "
                + " WHERE p.createdAt BETWEEN :fromDate AND :toDate AND b.retired = :ret";
        Map<String, Object> params = new HashMap<>();
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);
        params.put("ret", false);

//        jpql = "SELECT b FROM Bill b JOIN FETCH b.billItems WHERE  b.retired=:ret and b.createdAt BETWEEN :fromDate AND :toDate";
        // Execute the query to get filtered bills
        List<Bill> bills = billFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP); // Assuming you have a facade to execute JPQL queries

        // Since bills are fetched with their items, simply collect all items if needed
        List<BillItem> allBillItems = new ArrayList<>();
        bills.forEach(bill -> allBillItems.addAll(bill.getBillItems()));

        try {
            createBillsBillItemsList(new HashSet<>(bills), allBillItems);
        } catch (IOException e) {
            e.printStackTrace(); // Handle exceptions properly
        }
    }

    public void searchOpdBillsWithIndications() {
        List<BillTypeAtomic> billTypesAtomics = new ArrayList<>();
        billTypesAtomics.add(BillTypeAtomic.OPD_BILL_WITH_PAYMENT);
        billTypesAtomics.add(BillTypeAtomic.OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER);

        createTableByKeyword(billTypesAtomics, institution, department, fromInstitution, fromDepartment, toInstitution, toDepartment);

        if (bills != null) {
            bills = bills.stream()
                    .filter(b -> b.getIndication() != null && !b.getIndication().trim().isEmpty())
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Redacts potentially sensitive PII from parameter map for safe logging
     * @param parameters The parameter map to redact
     * @return A new map with sensitive values masked
     */
    private Map<String, Object> redactPiiFromParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return null;
        }
        
        Map<String, Object> redacted = new HashMap<>();
        Set<String> sensitiveKeys = new HashSet<>(Arrays.asList(
            "patient", "person", "phone", "nic", "stf", "staffname", 
            "patientname", "personname", "name", "email", "address"
        ));
        
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (sensitiveKeys.stream().anyMatch(key::contains)) {
                redacted.put(entry.getKey(), "***");
            } else {
                redacted.put(entry.getKey(), entry.getValue());
            }
        }
        return redacted;
    }

    // </editor-fold>
}
