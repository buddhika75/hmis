/*
 * Open Hospital Management Information System
 *
 * Dr M H B Ariyaratne
 * Acting Consultant (Health Informatics)
 * (94) 71 5812399
 * (94) 71 5812399
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.common.ConfigOptionApplicationController;
import com.divudi.bean.common.ItemController;
import com.divudi.bean.common.ReportTimerController;
import com.divudi.bean.common.SessionController;
import com.divudi.core.data.*;
import com.divudi.core.data.dataStructure.DepartmentSale;
import com.divudi.core.data.dataStructure.DepartmentStock;
import com.divudi.core.data.dataStructure.InstitutionSale;
import com.divudi.core.data.dataStructure.InstitutionStock;
import com.divudi.core.data.dataStructure.ItemQuantityAndValues;
import com.divudi.core.data.dataStructure.ItemTransactionSummeryRow;
import com.divudi.core.data.dataStructure.StockAverage;

import com.divudi.core.data.reports.InventoryReports;
import com.divudi.core.entity.*;
import com.divudi.core.entity.pharmacy.*;
import com.divudi.core.facade.*;
import com.divudi.core.util.JsfUtil;
import com.divudi.core.data.dataStructure.CategoryWithItem;
import com.divudi.core.data.dataStructure.PharmacySummery;
import com.divudi.core.data.dto.PharmacyGrnItemDTO;
import com.divudi.core.data.dto.PharmacyGrnReturnItemDTO;
import com.divudi.core.data.table.String1Value1;
import com.divudi.core.util.CommonFunctions;
import com.divudi.core.light.pharmacy.PharmaceuticalItemLight;
import com.divudi.ejb.PharmacyBean;
import com.divudi.ejb.PharmacyService;
import com.divudi.service.BillService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TemporalType;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Dr. M. H. B. Ariyaratne, MBBS, MSc, MD(Health Informatics) Acting
 * Consultant (Health Informatics)
 */
@Named
@SessionScoped
public class PharmacyController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    private SessionController sessionController;
    @Inject
    private AmpController ampController;
    @Inject
    VtmController vtmController;
    @Inject
    AtmController atmController;
    @Inject
    ManufacturerController manufaturerController;
    @Inject
    ImporterController importerController;
    @Inject
    DiscardCategoryController discardCategoryController;
    @Inject
    VmpController vmpController;
    @Inject
    ConfigOptionApplicationController configOptionApplicationController;
    @Inject
    PharmacyErrorChecking pharmacyErrorChecking;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    BillService billService;
    @EJB
    ItemFacade itemFacade;
    @EJB
    VtmFacade vtmFacade;
    @EJB
    AtmFacade atmFacade;
    @EJB
    VmpFacade vmpFacade;
    @EJB
    AmpFacade ampFacade;
    @EJB
    VmppFacade vmppFacade;
    @EJB
    AmppFacade amppFacade;
    @EJB
    PharmaceuticalItemFacade piFacade;
    @EJB
    private DepartmentFacade departmentFacade;
    @EJB
    private StockFacade stockFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private ItemBatchFacade itemBatchFacade;
    @EJB
    private ReportTimerController reportTimerController;

    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;

    @EJB
    PharmacyBean pharmacyBean;

    @EJB
    PharmacyService pharmacyService;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Class Variables">
    private static final long serialVersionUID = 1L;
    private int pharmacyAdminIndex;
    private int pharmacySummaryIndex;

    private Item pharmacyItem;
    boolean hasSale;
    boolean hasWholesale;
    boolean hasInward;
    boolean hasIssue;
    boolean hasTransferOut;
    boolean hasPurchase;
    boolean hasTransferIn;
    ////////
    //List<DepartmentStock> departmentStocks;
    private List<DepartmentSale> departmentSale;
    private List<BillItem> grns;
    private List<BillItem> pendingGrns;
    private List<com.divudi.core.data.dto.PharmacyGrnItemDTO> grnDtos;
    private List<com.divudi.core.data.dto.PharmacyGrnReturnItemDTO> grnReturnDtos;
    private List<BillItem> pos;
    private List<BillItem> directPurchase;
    private List<Bill> bills;
    List<ItemTransactionSummeryRow> itemTransactionSummeryRows;
    private int managePharamcyReportIndex = -1;
    double persentage;
    Category category;

    private PharmaceuticalItemLight selectedLight;
    private List<PharmaceuticalItemLight> selectedLights;
    private List<PharmaceuticalItemLight> allLights;

    private List<Atm> atms;
    private List<Vtm> vtms;
    private List<Vmp> vmps;
    private List<Amp> amps;
    private List<Vmpp> vmpps;
    private List<Ampp> ampps;

    private List<Atm> atmsSelected;
    private List<Vtm> vtmsSelected;
    private List<Vmp> vmpsSelected;
    private List<Amp> ampsSelected;
    private List<Vmpp> vmppsSelected;
    private List<Ampp> amppsSelected;

    private Atm atm;
    private Vtm vtm;
    private Vmp vmp;
    private Amp amp;
    private Vmpp vmpp;
    private Ampp ampp;

    private MeasurementUnit issueUnit;
    // </editor-fold>

    private Map<String, Map<String, Double[]>> departmentTotals = new HashMap<>();
    private Map<String, Double[]> pharmacyTotals = new HashMap<>();
    private Map<String, Map<String, List<DepartmentCategoryWiseItems>>> departmentCategoryMap = new HashMap<>();

    // <editor-fold defaultstate="collapsed" desc="Methods - Fill Data">
    private void fillVtms() {
        String j = "select i "
                + " from Vtm i "
                + " where i.retired != true "
                + " order by i.name";
        vtms = vtmFacade.findByJpql(j);
    }

    private void fillAtms() {
        String j = "select i "
                + " from Atm i "
                + " where i.retired != true "
                + " order by i.name";
        atms = atmFacade.findByJpql(j);
    }

    private void fillVmps() {
        String j = "select i "
                + " from Vmp i "
                + " where i.retired != true "
                + " order by i.name";
        vmps = vmpFacade.findByJpql(j);
    }

    private void fillAmps() {
        String j = "select i "
                + " from Amp i "
                + " where i.retired != true "
                + " order by i.name";
        amps = ampFacade.findByJpql(j);
    }

    private void fillVmpps() {
        String j = "select i "
                + " from Vmpp i "
                + " where i.retired != true "
                + " order by i.name";
        vmpps = vmppFacade.findByJpql(j);
    }

    private void fillAmpps() {
        String j = "select i "
                + " from Ampp i "
                + " where i.retired != true "
                + " order by i.name";
        ampps = amppFacade.findByJpql(j);
    }

    public void listPharmacyPurchaseBills() {
        try {
            StringBuilder jpql = new StringBuilder("select b from Bill b where b.retired=:ret");
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ret", false);

            List<BillType> billTypes = new ArrayList<>();
            billTypes.add(BillType.PharmacyGrnReturn);
            billTypes.add(BillType.PharmacyGrnBill);
            billTypes.add(BillType.PharmacyPurchaseBill);
            jpql.append(" and b.billType in :bts");
            parameters.put("bts", billTypes);

            if (fromDate != null && toDate != null) {
                jpql.append(" and b.createdAt between :fd and :td");
                parameters.put("fd", fromDate);
                parameters.put("td", toDate);
            }

            if (institution != null) {
                jpql.append(" and b.fromInstitution=:fi");
                parameters.put("fi", institution);
            }

            bills = billFacade.findByJpql(jpql.toString(), parameters, TemporalType.TIMESTAMP);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Methods - Navigation">
    public String navigateToBinCard() {
        if (pharmacyErrorChecking.getDepartment() == null) {
            pharmacyErrorChecking.setDepartment(getSessionController().getDepartment());
        }
        return "/pharmacy/bin_card_dto?faces-redirect=true";
    }

    public String navigateToItemsList() {
        return "/pharmacy/list_amps?faces-redirect=true";
    }

    public String navigateToMedicineList() {
        return "/pharmacy/list_medicines?faces-redirect=true";
    }

    public String navigateToItemsWithoutDistributor() {
        return "/pharmacy/pharmacy_report_list_amps_with_out_distributor?faces-redirect=true";
    }

    public String navigateToItemsWithSuppliersAndPrices() {
        return "/pharmacy/item_supplier_prices?faces-redirect=true";
    }

    public String navigateToItemsWithDistributor() {
        return "/pharmacy/pharmacy_report_list_distributor_with_distributor_items?faces-redirect=true";
    }

    public String navigateToItemsWithMultipleDistributorsItemsOnly() {
        return "/pharmacy/pharmacy_report_list_grater_than_one_distributor?faces-redirect=true";
    }

    public String navigateToItemWithMultipleDistributors() {
        return "/pharmacy/pharmacy_report_list_item_with_multiple_dealor?faces-redirect=true";
    }

    public String navigateToReorderAnalysis() {
        return "/pharmacy/ordering_data?faces-redirect=true";
    }

    public String navigateToReorderManagement() {
        return "/pharmacy/reorder_management?faces-redirect=true";
    }

    public String navigateToAllItemsTransactionSummary() {
        return "/pharmacy/raport_all_item_transaction_summery?faces-redirect=true";
    }

    public String navigateToItemTransactionDetails() {
        return "/pharmacy/pharmacy_item_transactions?faces-redirect=true";
    }

    public String navigateToStockTake() {
        return "/pharmacy/pharmacy_stock_take?faces-redirect=true";
    }

    public String navigateToListPharmaceuticals() {
        fillPharmaceuticalLights();
        return "/pharmacy/admin/items?faces-redirect=true";
    }

    public String navigateToPharmacyAnalytics() {
        return "/pharmacy/pharmacy_analytics?faces-redirect=true";
    }

    public String navigateToManagePharmaceuticals() {
        return "/pharmacy/admin/index?faces-redirect=true";
    }

    public String navigateToListAllPharmaceuticalItemsLight() {
        return "/pharmacy/admin/items?faces-redirect=true";
    }

    public String navigateToDosageForms() {
        return "/pharmacy/admin/dosage_forms?faces-redirect=true";
    }

    public String navigateToPharmaceuticalItemCategories() {
        return "/pharmacy/admin/pharmaceutical_item_category?faces-redirect=true";
    }

    public String navigateToAtc() {
        return "/pharmacy/admin/atc?faces-redirect=true";
    }

    public String navigateToAmp() {
        ampController.setItems(null);
        return "/pharmacy/admin/amp?faces-redirect=true";
    }

    public String navigateToAmpp() {
        return "/pharmacy/admin/ampp?faces-redirect=true";
    }

    public String navigateToAtm() {
        atmController.getItems();
        atmController.getCurrent();
        return "/pharmacy/admin/atm?faces-redirect=true";
    }

    public String navigateToManufacturers() {
        manufaturerController.getItems();
        manufaturerController.getCurrent();
        return "/pharmacy/pharmacy_manufacturer?faces-redirect=true";
    }

    public String navigateToDiscardCategory() {
        discardCategoryController.fillDiscardCategories();
        discardCategoryController.getItems();
        discardCategoryController.getCurrent();
        return "/pharmacy/pharmacy_discard_category?faces-redirect=true";
    }

    public String navigateToVariantCategoryStockByItem() {
        return "/pharmacy/pharmacy_variant_category_stock_by_item?faces-redirect=true";
    }

    public String navigateToVariantAdjustmentPreList() {
        return "/pharmacy/pharmacy_variant_ajustment_pre_list?faces-redirect=true";
    }

    public String navigateToImporters() {
        importerController.getItems();
        importerController.getCurrent();
        return "/pharmacy/pharmacy_importer?faces-redirect=true";
    }

    public String navigateToSuppliers() {
        return "/pharmacy/pharmacy_dealer?faces-redirect=true";
    }

    public String navigateToItemSuppliers() {
        return "/pharmacy/pharmacy_items_distributors?faces-redirect=true";
    }

    public String navigateToVmp() {
        return "/pharmacy/admin/vmp?faces-redirect=true";
    }

    public String navigateToVtm() {
        vtmController.fillItems();
        return "/pharmacy/admin/vtm?faces-redirect=true";
    }

    public String navigateToVmpp() {
        return "/pharmacy/admin/vmpp?faces-redirect=true";
    }

    public String navigateToDosageFormsMultiple() {
        return "/pharmacy/admin/dosage_forms_multiple?faces-redirect=true";
    }

    public String navigateToAtcMultiple() {
        return "/pharmacy/admin/atc_multiple?faces-redirect=true";
    }

    public String navigateToAmpMultiple() {
        fillAmps();
        return "/pharmacy/admin/amp_multiple?faces-redirect=true";
    }

    public String navigateToAmppMultiple() {
        fillAmpps();
        return "/pharmacy/admin/ampp_multiple?faces-redirect=true";
    }

    public String navigateToAtmMultiple() {
        fillAtms();
        return "/pharmacy/admin/atm_multiple?faces-redirect=true";
    }

    public String navigateToVmpMultiple() {
        fillVmps();
        return "/pharmacy/admin/vmp_multiple?faces-redirect=true";
    }

    public String navigateToVtmMultiple() {
        fillVtms();
        return "/pharmacy/admin/vtm_multiple?faces-redirect=true";
    }

    public String navigateToVmppMultiple() {
        fillVmpps();
        return "/pharmacy/admin/vmpp_multiple?faces-redirect=true";
    }

    public String navigateToItemMismatchReport() {
        return "/pharmacy/pharmacy_item_mismatch_report?faces-redirect=true";
    }

    public String navigateToItemImport() {
        return "/pharmacy/pharmacy_item_import?faces-redirect=true";
    }

    public String navigateToItemImportWithoutStock() {
        return "/pharmacy/pharmacy_item_import_without_stock?faces-redirect=true";
    }

    public String navigateToItemImportWithStock() {
        return "/pharmacy/pharmacy_item_import_with_stock?faces-redirect=true";
    }

    public String navigateToItemImportByBarcode() {
        return "/pharmacy/pharmacy_item_import_by_barcode?faces-redirect=true";
    }

    public String navigateToItemImportStocksByName() {
        return "/pharmacy/pharmacy_item_import_stocks_by_name?faces-redirect=true";
    }

    public String navigateToPurchaseOpeningStock() {
        return "/pharmacy/pharmacy_purchase_openning_stock?faces-redirect=true";
    }

    public String navigateToImporterCategory() {
        return "/pharmacy/pharmacy_importer_category?faces-redirect=true";
    }

    public String navigateToItemImportBarcode() {
        return "/pharmacy/pharmacy_item_import_barcode?faces-redirect=true";
    }

    public String navigateToErrorCorrection() {
        return "/pharmacy/pharmacy_error_correction?faces-redirect=true";
    }

    public String navigateToTransferRequestAll() {
        return "/pharmacy/pharmacy_transfer_request_all?faces-redirect=true";
    }

    public String navigateToItemDistributorImport() {
        return "/pharmacy/pharmacy_item_distributor_import?faces-redirect=true";
    }

    public String navigateToItemNameFromBarcode() {
        return "/pharmacy/pharmacy_item_name_from_barcode?faces-redirect=true";
    }

    public String navigateToItemNamesMissing() {
        return "/pharmacy/pharmacy_item_names_missing?faces-redirect=true";
    }

    public String navigateToItemNamesCorrectionBySnapshot() {
        return "/pharmacy/pharmacy_item_names_correction_by_snapshot?faces-redirect=true";
    }

    public String navigateToItemByDistributor() {
        return "/pharmacy/pharmacy_item_by_distributor?faces-redirect=true";
    }

    public String navigateToErrorChecking() {
        return "/pharmacy/pharmacy_error_checking?faces-redirect=true";
    }

    public String navigateToErrorCheckingByDate() {
        return "/pharmacy/pharmacy_error_checking_date?faces-redirect=true";
    }

    public String navigateToDepartmentErrorReport() {
        return "/pharmacy/pharmacy_report_department_error?faces-redirect=true";
    }

    public String navigateToDepartmentStockByBatchMinus() {
        return "/pharmacy/pharmacy_report_department_stock_by_batch_minus?faces-redirect=true";
    }

    public String navigateToStockAdjustmentVariance() {
        return "/pharmacy/reports/stock_adjustment_variance?faces-redirect=true";
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Methods - Data Maniulation">
    public void deleteSingleVtm() {
        if (vtm == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        vtm.setRetired(true);
        vtmFacade.edit(vtm);
        fillVtms();
    }

    public void deleteSingleAtm() {
        if (atm == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        atm.setRetired(true);
        atmFacade.edit(atm);
        fillAtms();
    }

    public void deleteSingleVmp() {
        if (vmp == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        vmp.setRetired(true);
        vmpFacade.edit(vmp);
        fillVmps();
    }

    public void deleteSingleAmp() {
        if (amp == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        amp.setRetired(Boolean.TRUE);
        ampFacade.edit(amp);
        fillAmps();
    }

    public void deleteSingleVmpp() {
        if (vmpp == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        vmpp.setRetired(true);
        vmppFacade.edit(vmpp);
        fillVmpps();
    }

    public void deleteSingleAmpp() {
        if (ampp == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        ampp.setRetired(true);
        amppFacade.edit(ampp);
        fillAmpps();
    }

    public void deleteMultipleVtms() {
        if (vtmsSelected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        if (vtmsSelected.isEmpty()) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        for (Vtm i : vtmsSelected) {
            i.setRetired(true);
            //TODO: Write to AuditEvent
        }
        vtmFacade.batchEdit(vtmsSelected);
        fillVtms();
    }

    public void deleteMultipleVmps() {
        if (vmpsSelected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        if (vmpsSelected.isEmpty()) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        for (Vmp i : vmpsSelected) {
            i.setRetired(true);
            //TODO: Write to AuditEvent
        }
        vmpFacade.batchEdit(vmpsSelected);
        fillVmps();
    }

    public void assignIssueUnitToMultipleVmps() {
        if (issueUnit == null) {
            JsfUtil.addErrorMessage("No Issue Unit Selected");
            return;
        }
        if (vmpsSelected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        if (vmpsSelected.isEmpty()) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        for (Vmp i : vmpsSelected) {
            i.setIssueUnit(issueUnit);
            //TODO: Write to AuditEvent
        }
        vmpFacade.batchEdit(vmpsSelected);
        fillVmps();
    }

    public void deleteMultipleVmpps() {
        if (vmppsSelected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        if (vmppsSelected.isEmpty()) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        for (Vmpp i : vmppsSelected) {
            i.setRetired(true);
            //TODO: Write to AuditEvent
        }
        vmppFacade.batchEdit(vmppsSelected);
        fillVmpps();
    }

    public void deleteMultipleAmps() {
        if (ampsSelected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        if (ampsSelected.isEmpty()) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        for (Amp i : ampsSelected) {
            i.setRetired(true);
            //TODO: Write to AuditEvent
        }
        ampFacade.batchEdit(ampsSelected);
        fillAmps();
    }

    public void deleteMultipleAtms() {
        if (atmsSelected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        if (atmsSelected.isEmpty()) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        for (Atm i : atmsSelected) {
            i.setRetired(true);
            //TODO: Write to AuditEvent
        }
        atmFacade.batchEdit(atmsSelected);
        fillAtms();
    }

    public void deleteMultipleAmpps() {
        if (amppsSelected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        if (amppsSelected.isEmpty()) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        for (Ampp i : amppsSelected) {
            i.setRetired(true);
            //TODO: Write to AuditEvent
        }
        amppFacade.batchEdit(amppsSelected);
        fillAmpps();
    }

    // </editor-fold>
    private Date fromDate;
    private Date toDate;
    private Department department;
    private Department dept;
    private Department fromDepartment;
    private Department toDepartment;
    private Institution site;
    private Institution toInstitution;
    private Institution fromInstitution;
    private PaymentMethod paymentMethod;
    private String reportType;
    private double totalCreditPurchaseValue;
    private double totalCashPurchaseValue;
    private double totalCashCostValue;
    private double totalCreditCostValue;
    private double totalPurchase;
    private List<String1Value1> data;
    private double totalSaleValue;
    private double totalCreditSaleValue;
    private double totalCashSaleValue;
    private double totalCostValue;
    private Item item;
    private List<BillItem> billItems;
    private List<PharmacyRow> pharmacyRows;
    private List<PharmacySummery> departmentSummaries;
    private List<CategoryWithItem> issueDepartmentCategoryWiseItems;
    private List<DepartmentCategoryWiseItems> resultsList;
    private Map<String, List<PharmacyRow>> departmentWiseRows;

    private String transferType;
    private Institution fromSite;
    private Institution toSite;

    private List<DepartmentWiseBill> departmentWiseBillList;
    private List<Stock> stockList;
    private double qty;

    public void clearItemHistory() {

        grantStock = 0.00;
        grantSaleQty = 0.00;
        grantSaleValue = 0.00;
        grantWholeSaleQty = 0.00;
        grantWholeSaleValue = 0.00;
        grantBhtIssueQty = 0.00;
        grantBhtValue = 0.00;
        grantTransferIssueQty = 0.00;
        grantTransferIssueValue = 0.00;
        grantTransferReceiveQty = 0.00;
        grantTransferReceiveValue = 0.00;
        grantIssueQty = 0.00;
        grantIssueValue = 0.00;

        fromDate = CommonFunctions.getStartOfMonth();
        toDate = CommonFunctions.getEndOfDay(new Date());

        pharmacyItem = null;
        institutionStocks = null;
        institutionSales = null;
        grns = null;
        pendingGrns = null;
        institutionWholeSales = null;
        institutionBhtIssue = null;
        institutionTransferIssue = null;
        institutionTransferReceive = null;
        institutionIssue = null;
        pos = null;
        directPurchase = null;
        ampps = null;

    }

    public String generateFileNameForReport(String reportName) {
        if (reportName == null || reportName.trim().isEmpty()) {
            return ""; // Handle null or empty reportName
        }
        if (reportType != null && !reportType.trim().isEmpty()) {
            return reportName.trim() + " " + reportType.trim();
        }
        return reportName.trim();
    }

    @Deprecated
    public void generateGRNReportTableByBillItem(List<BillType> bt) {
        bills = null;
        totalCreditPurchaseValue = 0.0;
        totalCashPurchaseValue = 0.0;
        totalPurchase = 0.0;

//        billItems = new ArrayList<>();
//
//        String sql = "SELECT b FROM BillItem b WHERE b.bill.retired = false"
//                + " and b.bill.billType In :btp"
//                + " and b.bill.createdAt between :fromDate and :toDate";
//
//        Map<String, Object> tmp = new HashMap<>();
//
//        tmp.put("btp", bt);
//        tmp.put("fromDate", getFromDate());
//        tmp.put("toDate", getToDate());
//
//        if (institution != null) {
//            sql += " and b.bill.institution = :fIns";
//            tmp.put("fIns", institution);
//        }
//
//        if (site != null) {
//            sql += " and b.bill.department.site = :site";
//            tmp.put("site", site);
//        }
//
//        if (dept != null) {
//            sql += " and b.bill.department = :dept";
//            tmp.put("dept", dept);
//        }
//
//        if (paymentMethod != null) {
//            sql += " and b.bill.paymentMethod = :pm";
//            tmp.put("pm", paymentMethod);
//        }
//
//        if (fromInstitution != null) {
//            sql += " AND b.bill.fromInstitution = :supplier";
//            tmp.put("supplier", fromInstitution);
//        }
//
//        sql += " order by b.bill.id desc";
//
//        try {
//            billItems = getBillItemFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP);
//
//        } catch (Exception e) {
//            JsfUtil.addErrorMessage(e, " Something Went Worng!");
//        }
//        calculateTotals(bills);
//        calculateTotalsForBillItems(billItems);
    }

    public void fetchBillItemDetailsToPrint() {
        if (bills == null) {
            JsfUtil.addErrorMessage("No Bills");
            return;
        }
        if (bills.isEmpty()) {
            JsfUtil.addErrorMessage("Bill List Empty");
            return;
        }
        for (Bill b : bills) {
            if (b.getBillItems() == null || b.getBillItems().isEmpty()) {
                b.setBillItems(billService.fetchBillItems(b));
            }
        }
    }

    public String navigateToPrinteGeneratedGrnDetailedRportTable() {
        fetchBillItemDetailsToPrint();
        return "/reports/inventoryReports/grn_report_detail_print?faces-redirect=true";
    }

    public String navigateToPrinteGeneratedGrnReturnReportTable() {
        fetchBillItemDetailsToPrint();
        return "/reports/inventoryReports/grn_report_return_print?faces-redirect=true";
    }

    public String navigateToPrinteGeneratedGrnCancelReportTable() {
        fetchBillItemDetailsToPrint();
        return "/reports/inventoryReports/grn_report_cancel_print?faces-redirect=true";
    }

    public String navigateBackToGeneratedGrnDetailedRportTable() {
        return "/reports/inventoryReports/grn_report?faces-redirect=true";
    }

    public void generateGrnReportTable() {
        reportTimerController.trackReportExecution(() -> {
            resetFields();

            List<BillType> bt = new ArrayList<>();
            List<BillTypeAtomic> bta = new ArrayList<>();
            if ("returnReport".equals(reportType)) {
                bta.add(BillTypeAtomic.PHARMACY_GRN_RETURN);
                bta.add(BillTypeAtomic.PHARMACY_DIRECT_PURCHASE_REFUND);
            } else if ("cancellationReport".equals(reportType)) {
                bta.add(BillTypeAtomic.PHARMACY_GRN_CANCELLED);
                bta.add(BillTypeAtomic.PHARMACY_DIRECT_PURCHASE_CANCELLED);
            } else if ("summeryReport".equals(reportType) || "detailReport".equals(reportType)) {
                bta.add(BillTypeAtomic.PHARMACY_GRN);
                bta.add(BillTypeAtomic.PHARMACY_GRN_RETURN);
                bta.add(BillTypeAtomic.PHARMACY_GRN_CANCELLED);

                bta.add(BillTypeAtomic.PHARMACY_DIRECT_PURCHASE);
                bta.add(BillTypeAtomic.PHARMACY_DIRECT_PURCHASE_REFUND);
                bta.add(BillTypeAtomic.PHARMACY_DIRECT_PURCHASE_CANCELLED);
            }

            bills = new ArrayList<>();

            String jpql = "SELECT b FROM Bill b "
                    + " WHERE b.retired = false"
                    + " and b.cancelled = false"
                    + " and b.billTypeAtomic In :btas"
                    + " and b.createdAt between :fromDate and :toDate";

            Map<String, Object> params = new HashMap<>();

            params.put("btas", bta);
            params.put("fromDate", getFromDate());
            params.put("toDate", getToDate());

            if (institution != null) {
                jpql += " and b.institution = :fIns";
                params.put("fIns", institution);
            }

            if (site != null) {
                jpql += " and b.department.site = :site";
                params.put("site", site);
            }

            if (dept != null) {
                jpql += " and b.department = :dept";
                params.put("dept", dept);
            }

            if (paymentMethod != null) {
                jpql += " and b.paymentMethod = :pm";
                params.put("pm", paymentMethod);
            }

            if (fromInstitution != null) {
                jpql += " AND (b.fromInstitution = :supplier OR b.toInstitution = :supplier)";
                params.put("supplier", fromInstitution);
            }

            jpql += " order by b.id desc";

            try {
                bills = getBillFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, " Something Went Worng!");
            }
            calculateTotals(bills);
        }, InventoryReports.GRN_REPORT, sessionController.getLoggedUser());
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }

        UserPreference userPreference = new UserPreference();
        String pattern = userPreference.getLongDateTimeFormat();

        if (!pattern.contains("a")) {
            pattern = pattern.replace("HH", "hh") + " a";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public void exportGrnDetailReportToPdf() {
        boolean hasCosting = configOptionApplicationController.getBooleanValueByKey("Manage Costing", true);

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=GRN_Detail_Report.pdf");

        try (OutputStream out = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);

            document.open();

            com.itextpdf.text.Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("GRN Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            PdfPTable mainTable = new PdfPTable(hasCosting ? 17 : 15);
            mainTable.setWidthPercentage(100);
            if (hasCosting) {
                mainTable.setWidths(new float[]{1.5f, 1.8f, 2f, 2f, 2.2f, 2.2f, 2f, 2f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f, 2f, 8f});
            } else {
                mainTable.setWidths(new float[]{1.5f, 1.8f, 2f, 2f, 2.2f, 2.2f, 2f, 2f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f, 2f, 8f});
            }

            String[] headers = {"GRN No", "Invoice No", "Created Date", "Approved Date", "Supplier Name", "Institution",
                    "Site", "Department", "Po No", "Purchase Cash", "Purchase Credit", "Sale Cash", "Sale Credit", "Remark", "Purchase Details"};

            if (hasCosting) {
                headers = new String[]{"GRN No", "Invoice No", "Created Date", "Approved Date", "Supplier Name", "Institution",
                        "Site", "Department", "Po No", "Purchase Cash", "Purchase Credit", "Cost Cash", "Cost Credit", "Sale Cash", "Sale Credit", "Remark", "Purchase Details"};
            }

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                mainTable.addCell(cell);
            }

            for (Bill b : getBills()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");

                mainTable.addCell(new Phrase(b.getDeptId(), normalFont));
                mainTable.addCell(new Phrase(b.getInvoiceNumber() != null ? b.getInvoiceNumber() : b.getReferenceBill().getInvoiceNumber(), normalFont));
                mainTable.addCell(new Phrase(sdf.format(b.getCreatedAt()), normalFont));
                mainTable.addCell(new Phrase(sdf.format(b.getReferenceBill() != null ? b.getReferenceBill().getCreatedAt() : b.getCreatedAt()), normalFont));
                mainTable.addCell(new Phrase(
                        b.getFromInstitution() != null ? b.getFromInstitution().getName() :
                                (b.getToInstitution() != null ? b.getToInstitution().getName() : ""), normalFont));
                mainTable.addCell(new Phrase(b.getInstitution() != null ? b.getInstitution().getName() : "", normalFont));
                mainTable.addCell(new Phrase(b.getDepartment() != null && b.getDepartment().getSite() != null ? b.getDepartment().getSite().getName() : "", normalFont));
                mainTable.addCell(new Phrase(b.getDepartment() != null ? b.getDepartment().getName() : "", normalFont));
                mainTable.addCell(new Phrase(b.getReferenceBill() != null ? b.getReferenceBill().getDeptId() : "", normalFont));

                double purchaseCash = (b.getPaymentMethod() == PaymentMethod.Cash ? b.getNetTotal() : 0);
                double purchaseCredit = (b.getPaymentMethod() == PaymentMethod.Credit ? b.getNetTotal() : 0);
                double saleCash = (b.getPaymentMethod() == PaymentMethod.Cash ? b.getSaleValue() : 0);
                double saleCredit = (b.getPaymentMethod() == PaymentMethod.Credit ? b.getSaleValue() : 0);
                double costCash = hasCosting ? (b.getPaymentMethod() == PaymentMethod.Cash ? b.getBillFinanceDetails().getTotalCostValue() != null ? b.getBillFinanceDetails().getTotalCostValue().doubleValue() : 0.0 : 0.0) : 0.0;
                double costCredit = hasCosting ? (b.getPaymentMethod() == PaymentMethod.Credit ? b.getBillFinanceDetails().getTotalCostValue() != null ? b.getBillFinanceDetails().getTotalCostValue().doubleValue() : 0.0 : 0.0) : 0.0;

                if (b.getBillTypeAtomic().toString().contains("RETURN") || b.getBillTypeAtomic().toString().contains("CANCELLED") || b.getBillTypeAtomic().toString().contains("REFUND")) {
                    if (b.getReferenceBill() != null) {
                        if (b.getReferenceBill().getPaymentMethod() == PaymentMethod.Cash) {
                            purchaseCash = -Math.abs(b.getBillFinanceDetails().getLineNetTotal() != null ? b.getBillFinanceDetails().getLineNetTotal().doubleValue(): 0.0);
                            saleCash = -Math.abs(b.getBillFinanceDetails().getTotalRetailSaleValue() != null ? b.getBillFinanceDetails().getTotalRetailSaleValue().doubleValue() : 0.0);
                            if (hasCosting) {
                                costCash = -Math.abs(b.getBillFinanceDetails().getTotalCostValue() != null ? b.getBillFinanceDetails().getTotalCostValue().doubleValue() : 0.0);
                            }
                        } else {
                            purchaseCash = 0;
                            saleCash = 0;
                            costCash = 0;
                        }
                        if (b.getReferenceBill().getPaymentMethod() == PaymentMethod.Credit) {
                            purchaseCredit = -Math.abs(b.getBillFinanceDetails().getLineNetTotal() != null ? b.getBillFinanceDetails().getLineNetTotal().doubleValue(): 0.0);
                            saleCredit = -Math.abs(b.getBillFinanceDetails().getTotalRetailSaleValue() != null ? b.getBillFinanceDetails().getTotalRetailSaleValue().doubleValue() : 0.0);
                            if (hasCosting) {
                                costCredit = -Math.abs(b.getBillFinanceDetails().getTotalCostValue() != null ? b.getBillFinanceDetails().getTotalCostValue().doubleValue() : 0.0);
                            }
                        } else {
                            purchaseCredit = 0;
                            saleCredit = 0;
                            costCredit = 0;
                        }
                    }
                }

                mainTable.addCell(new Phrase(String.format("%,.2f", purchaseCash), normalFont));
                mainTable.addCell(new Phrase(String.format("%,.2f", purchaseCredit), normalFont));
                if (hasCosting) {
                    mainTable.addCell(new Phrase(String.format("%,.2f", costCash), normalFont));
                    mainTable.addCell(new Phrase(String.format("%,.2f", costCredit), normalFont));
                }
                mainTable.addCell(new Phrase(String.format("%,.2f", saleCash), normalFont));
                mainTable.addCell(new Phrase(String.format("%,.2f", saleCredit), normalFont));
                mainTable.addCell(new Phrase(b.getComments() != null ? b.getComments() : "", normalFont));

                PdfPTable nested = new PdfPTable(hasCosting ? 8 : 7);
                nested.setWidthPercentage(100);
                String[] nestedHeaders = {"Item", "Qty", "Free Qty", "Purchase Rate", "Discount Rate", "MRP", "Batch"};
                if (hasCosting) {
                    nestedHeaders = new String[]{"Item", "Qty", "Free Qty", "Purchase Rate", "Cost Rate", "Discount Rate", "MRP", "Batch"};
                }
                for (String h : nestedHeaders) {
                    PdfPCell nh = new PdfPCell(new Phrase(h, boldFont));
                    nh.setHorizontalAlignment(Element.ALIGN_CENTER);
                    nested.addCell(nh);
                }

                for (BillItem bi : b.getBillItems()) {
                    nested.addCell(new Phrase(bi.getItem().getName(), normalFont));
                    nested.addCell(new Phrase(String.valueOf(bi.getPharmaceuticalBillItem().getQty()), normalFont));
                    nested.addCell(new Phrase(String.valueOf(bi.getPharmaceuticalBillItem().getFreeQty()), normalFont));
                    nested.addCell(new Phrase(String.format("%,.2f", bi.getPharmaceuticalBillItem().getPurchaseRate()), normalFont));
                    if (hasCosting) {
                        nested.addCell(new Phrase(String.format("%,.2f", bi.getBillItemFinanceDetails().getLineCostRate()), normalFont));
                    }

                    Double discountRate = bi.getBillItemFinanceDetails().getLineDiscountRate() != null ? bi.getBillItemFinanceDetails().getLineDiscountRate().doubleValue() : null;
                    if (discountRate == null) {
                        if (bi.getReferanceBillItem().getBillItemFinanceDetails().getLineDiscountRate() != null) {
                            discountRate = bi.getReferanceBillItem().getBillItemFinanceDetails().getLineDiscountRate().doubleValue();
                        } else {
                            discountRate = 0.0;
                        }
                    }
                    nested.addCell(new Phrase(String.format("%,.2f", discountRate), normalFont));
                    nested.addCell(new Phrase(String.format("%,.2f", bi.getPharmaceuticalBillItem().getRetailRate()), normalFont));
                    nested.addCell(new Phrase(bi.getPharmaceuticalBillItem().getItemBatch().getBatchNo(), normalFont));
                }

                PdfPCell nestedCell = new PdfPCell(nested);
                mainTable.addCell(nestedCell);
            }

            PdfPCell footerLabel = new PdfPCell(new Phrase("Total", boldFont));
            footerLabel.setColspan(9);
            footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            mainTable.addCell(footerLabel);

            mainTable.addCell(new Phrase(String.format("%,.2f", totalCashPurchaseValue), boldFont));
            mainTable.addCell(new Phrase(String.format("%,.2f", totalCreditPurchaseValue), boldFont));
            if (hasCosting) {
                mainTable.addCell(new Phrase(String.format("%,.2f", totalCashCostValue), boldFont));
                mainTable.addCell(new Phrase(String.format("%,.2f", totalCreditCostValue), boldFont));
            }
            mainTable.addCell(new Phrase(String.format("%,.2f", totalCashSaleValue), boldFont));
            mainTable.addCell(new Phrase(String.format("%,.2f", totalCreditSaleValue), boldFont));
            mainTable.addCell(new Phrase("", boldFont));
            mainTable.addCell(new Phrase("", boldFont));

            document.add(mainTable);
            document.close();
            context.responseComplete();
        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    public void exportGrnDetailReportToExcel() {
        boolean hasCosting = configOptionApplicationController.getBooleanValueByKey("Manage Costing", true);

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=GRN_Detail_Report.xlsx");

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");

        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("GRN Detail Report");
            int rowIndex = 0;

            XSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);
            CellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);

            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("GRN Report");
            titleCell.setCellStyle(boldStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, hasCosting?16:14));

            String[] headers = {"GRN No", "Invoice No", "Created Date", "Approved Date", "Supplier Name", "Institution",
                    "Site", "Department", "Po No", "Purchase Cash", "Purchase Credit", "Sale Cash", "Sale Credit", "Remark", "Purchase Details"};

            if (hasCosting) {
                headers = new String[]{"GRN No", "Invoice No", "Created Date", "Approved Date", "Supplier Name", "Institution",
                        "Site", "Department", "Po No", "Purchase Cash", "Purchase Credit", "Cost Cash", "Cost Credit",
                        "Sale Cash", "Sale Credit", "Remark", "Purchase Details"};
            }

            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(boldStyle);
            }

            double totalCashPurchase = 0;
            double totalCreditPurchase = 0;
            double totalCashSale = 0;
            double totalCreditSale = 0;
            double totalCashCost = 0;
            double totalCreditCost = 0;

            for (Bill b : getBills()) {
                Row row = sheet.createRow(rowIndex++);
                int col = 0;

                row.createCell(col++).setCellValue(b.getDeptId() != null ? b.getDeptId() : "");
                row.createCell(col++).setCellValue(b.getInvoiceNumber() != null ? b.getInvoiceNumber() : b.getReferenceBill() != null ? b.getReferenceBill().getInvoiceNumber() : "");
                row.createCell(col++).setCellValue(sdf.format(b.getCreatedAt()));
                row.createCell(col++).setCellValue(b.getReferenceBill() != null && b.getReferenceBill().getCreatedAt() != null ? sdf.format(b.getReferenceBill().getCreatedAt()) : "");
                row.createCell(col++).setCellValue(b.getFromInstitution() != null ? b.getFromInstitution().getName() : b.getToInstitution() != null ? b.getToInstitution().getName() : "");
                row.createCell(col++).setCellValue(b.getInstitution() != null ? b.getInstitution().getName() : "");
                row.createCell(col++).setCellValue(b.getDepartment() != null && b.getDepartment().getSite() != null ? b.getDepartment().getSite().getName() : "");
                row.createCell(col++).setCellValue(b.getDepartment() != null ? b.getDepartment().getName() : "");
                row.createCell(col++).setCellValue(b.getReferenceBill() != null ? b.getReferenceBill().getDeptId() : "");

                double purchaseCash = (b.getPaymentMethod() == PaymentMethod.Cash ? b.getNetTotal() : 0);
                double purchaseCredit = (b.getPaymentMethod() == PaymentMethod.Credit ? b.getNetTotal() : 0);
                double saleCash = (b.getPaymentMethod() == PaymentMethod.Cash ? b.getSaleValue() : 0);
                double saleCredit = (b.getPaymentMethod() == PaymentMethod.Credit ? b.getSaleValue() : 0);
                double costCash = hasCosting ? (b.getPaymentMethod() == PaymentMethod.Cash ? b.getBillFinanceDetails().getTotalCostValue().doubleValue() : 0) : 0;
                double costCredit = hasCosting ? (b.getPaymentMethod() == PaymentMethod.Credit ? b.getBillFinanceDetails().getTotalCostValue().doubleValue() : 0) : 0;

                if (b.getBillTypeAtomic().toString().contains("RETURN") || b.getBillTypeAtomic().toString().contains("CANCELLED") || b.getBillTypeAtomic().toString().contains("REFUND")) {
                    if (b.getReferenceBill() != null) {
                        if (b.getReferenceBill().getPaymentMethod() == PaymentMethod.Cash) {
                            purchaseCash = -Math.abs(b.getBillFinanceDetails().getLineNetTotal() != null ? b.getBillFinanceDetails().getLineNetTotal().doubleValue(): 0.0);
                            saleCash = -Math.abs(b.getBillFinanceDetails().getTotalRetailSaleValue() != null ? b.getBillFinanceDetails().getTotalRetailSaleValue().doubleValue() : 0.0);
                            if (hasCosting) {
                                costCash = -Math.abs(b.getBillFinanceDetails().getTotalCostValue() != null ? b.getBillFinanceDetails().getTotalCostValue().doubleValue() : 0.0);
                            }
                        } else {
                            purchaseCash = 0;
                            saleCash = 0;
                            if (hasCosting) {
                                costCash = 0;
                            }
                        }
                        if (b.getReferenceBill().getPaymentMethod() == PaymentMethod.Credit) {
                            purchaseCredit = -Math.abs(b.getBillFinanceDetails().getLineNetTotal() != null ? b.getBillFinanceDetails().getLineNetTotal().doubleValue(): 0.0);
                            saleCredit = -Math.abs(b.getBillFinanceDetails().getTotalRetailSaleValue() != null ? b.getBillFinanceDetails().getTotalRetailSaleValue().doubleValue() : 0.0);
                            if (hasCosting) {
                                costCredit = -Math.abs(b.getBillFinanceDetails().getTotalCostValue() != null ? b.getBillFinanceDetails().getTotalCostValue().doubleValue() : 0.0);
                            }
                        } else {
                            purchaseCredit = 0;
                            saleCredit = 0;
                            if (hasCosting) {
                                costCredit = 0;
                            }
                        }
                    }
                }

                totalCashPurchase += purchaseCash;
                totalCreditPurchase += purchaseCredit;
                totalCashSale += saleCash;
                totalCreditSale += saleCredit;
                if (hasCosting) {
                    totalCashCost += costCash;
                    totalCreditCost += costCredit;
                }

                row.createCell(col++).setCellValue(purchaseCash);
                row.createCell(col++).setCellValue(purchaseCredit);
                if (hasCosting) {
                    row.createCell(col++).setCellValue(costCash);
                    row.createCell(col++).setCellValue(costCredit);
                }
                row.createCell(col++).setCellValue(saleCash);
                row.createCell(col++).setCellValue(saleCredit);
                row.createCell(col++).setCellValue(b.getComments() != null ? b.getComments() : "");

                Row nestedHeader = sheet.createRow(rowIndex++);
                String[] nestedHeaders = {"Item Name", "Qty", "Free Qty", "Purchase Rate", "Discount Rate", "MRP", "Batch", "UOM"};
                if (hasCosting) {
                    nestedHeaders = new String[]{"Item Name", "Qty", "Free Qty", "Purchase Rate", "Cost Rate", "Discount Rate", "MRP", "Batch", "UOM"};
                }
                for (int i = 0; i < nestedHeaders.length; i++) {
                    Cell cell = hasCosting? nestedHeader.createCell(i + 16) : nestedHeader.createCell(i + 14);
                    cell.setCellValue(nestedHeaders[i]);
                    cell.setCellStyle(boldStyle);
                }

                for (BillItem bi : b.getBillItems()) {
                    Row itemRow = sheet.createRow(rowIndex++);
                    itemRow.createCell(hasCosting? 16 : 14).setCellValue(bi.getItem().getName());
                    itemRow.createCell(hasCosting? 17 :15).setCellValue(bi.getPharmaceuticalBillItem().getQty());
                    itemRow.createCell(hasCosting? 18 :16).setCellValue(bi.getPharmaceuticalBillItem().getFreeQty());
                    itemRow.createCell(hasCosting? 19 :17).setCellValue(bi.getPharmaceuticalBillItem().getPurchaseRate());
                    if (hasCosting) {
                        itemRow.createCell(20).setCellValue(bi.getBillItemFinanceDetails().getLineCostRate().doubleValue());
                    }

                    Double discountRate = bi.getBillItemFinanceDetails().getLineDiscountRate() != null ? bi.getBillItemFinanceDetails().getLineDiscountRate().doubleValue() : null;
                    if (discountRate == null) {
                        if (bi.getReferanceBillItem().getBillItemFinanceDetails().getLineDiscountRate() != null) {
                            discountRate = bi.getReferanceBillItem().getBillItemFinanceDetails().getLineDiscountRate().doubleValue();
                        } else {
                            discountRate = 0.0;
                        }
                    }

                    itemRow.createCell(hasCosting? 21 :18).setCellValue(discountRate);
                    itemRow.createCell(hasCosting? 22 :19).setCellValue(bi.getPharmaceuticalBillItem().getRetailRate());
                    itemRow.createCell(hasCosting? 23 :20).setCellValue(bi.getPharmaceuticalBillItem().getItemBatch().getBatchNo());
                    itemRow.createCell(hasCosting? 24 :21).setCellValue(bi.getItem().getMeasurementUnit() != null ? bi.getItem().getMeasurementUnit().getName() : "");
                }
            }

            Row footer = sheet.createRow(rowIndex++);
            Cell footerLabel = footer.createCell(0);
            footerLabel.setCellValue("Total");
            footerLabel.setCellStyle(boldStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 8));

            footer.createCell(9).setCellValue(totalCashPurchase);
            footer.createCell(10).setCellValue(totalCreditPurchase);
            if (hasCosting) {
                footer.createCell(11).setCellValue(totalCashCost);
                footer.createCell(12).setCellValue(totalCreditCost);
            }
            footer.createCell(hasCosting?13:11).setCellValue(totalCashSale);
            footer.createCell(hasCosting?14:12).setCellValue(totalCreditSale);

            workbook.write(out);
            context.responseComplete();
        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    public void exportGrnReturnReportToPdf() {
        boolean hasCosting = configOptionApplicationController.getBooleanValueByKey("Manage Costing", true);

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=GRN_Return_Report.pdf");

        try (OutputStream out = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            com.itextpdf.text.Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("GRN Return Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            PdfPTable mainTable = new PdfPTable(hasCosting ? 12 : 11);
            mainTable.setWidthPercentage(100);

            if (hasCosting) {
                mainTable.setWidths(new float[]{2f, 2f, 2f, 2f, 2.5f, 2f, 2f, 2f, 2f, 2f, 2f, 12f});
            } else {
                mainTable.setWidths(new float[]{2f, 2f, 2f, 2f, 2.5f, 2f, 2f, 2f, 2f, 2f, 12f});
            }

            String[] headers = {"Return No", "GRN No", "GRN Invoice No", "GRN Date", "Reference Institution", "Created At", "Approved At", "Supplier", "Purchase Value", "Sale Value", "Purchase Details"};
            if (hasCosting) {
                headers = new String[]{"Return No", "GRN No", "GRN Invoice No", "GRN Date", "Reference Institution", "Created At", "Approved At", "Supplier", "Purchase Value", "Cost Value", "Sale Value", "Purchase Details"};
            }

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                mainTable.addCell(cell);
            }

            double totalPurchase = 0;
            double totalSale = 0;
            double totalCost = 0;

            for (Bill r : getBills()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");

                mainTable.addCell(new Phrase(r.getDeptId(), normalFont));
                mainTable.addCell(new Phrase(r.getReferenceBill() != null ? r.getReferenceBill().getDeptId() : "", normalFont));
                mainTable.addCell(new Phrase(r.getReferenceBill() != null ? r.getReferenceBill().getInvoiceNumber() : "", normalFont));
                mainTable.addCell(new Phrase(r.getReferenceBill() != null && r.getReferenceBill().getCreatedAt() != null ? sdf.format(r.getReferenceBill().getCreatedAt()) : "", normalFont));
                mainTable.addCell(new Phrase(r.getReferenceBill() != null && r.getReferenceBill().getReferenceInstitution() != null ? r.getReferenceBill().getReferenceInstitution().getName() : "", normalFont));
                mainTable.addCell(new Phrase(r.getCreatedAt() != null ? sdf.format(r.getCreatedAt()) : "", normalFont));
                mainTable.addCell(new Phrase(r.getReferenceBill() != null && r.getReferenceBill().getCreatedAt() != null ? sdf.format(r.getReferenceBill().getCreatedAt()) : "", normalFont));
                mainTable.addCell(new Phrase(r.getToInstitution() != null ? r.getToInstitution().getName() : "", normalFont));

                double purchase = r.getBillFinanceDetails().getLineNetTotal() != null ? r.getBillFinanceDetails().getLineNetTotal().doubleValue(): 0.0;
                double sale = r.getBillFinanceDetails().getTotalRetailSaleValue() != null ? r.getBillFinanceDetails().getTotalRetailSaleValue().doubleValue() : 0.0;
                double cost = hasCosting ? (r.getBillFinanceDetails().getTotalCostValue() != null ? r.getBillFinanceDetails().getTotalCostValue().doubleValue() : 0.0) : 0.0;

                double adjustedPurchase = purchase > 0 ? -purchase : purchase;
                double adjustedSale = sale > 0 ? -sale : sale;
                double adjustedCost = hasCosting ? (cost > 0 ? -cost : cost) : 0.0;

                totalPurchase += adjustedPurchase;
                totalSale += adjustedSale;
                totalCost += adjustedCost;

                mainTable.addCell(new Phrase(String.format("%,.2f", adjustedPurchase), normalFont));
                if (hasCosting) {
                    mainTable.addCell(new Phrase(String.format("%,.2f", adjustedCost), normalFont));
                }
                mainTable.addCell(new Phrase(String.format("%,.2f", adjustedSale), normalFont));

                PdfPTable nested = new PdfPTable(hasCosting ? 9 : 8);
                nested.setWidthPercentage(100);
                String[] nestedHeaders = {"Item Name", "Qty", "Free Qty", "Purchase Rate", "Discount Rate", "MRP", "Batch", "UOM"};
                if (hasCosting) {
                    nestedHeaders = new String[]{"Item Name", "Qty", "Free Qty", "Purchase Rate", "Cost Rate", "Discount Rate", "MRP", "Batch", "UOM"};
                }
                for (String h : nestedHeaders) {
                    PdfPCell nh = new PdfPCell(new Phrase(h, boldFont));
                    nh.setHorizontalAlignment(Element.ALIGN_CENTER);
                    nested.addCell(nh);
                }

                for (BillItem bi : r.getBillItems()) {
                    nested.addCell(new Phrase(bi.getItem().getName(), normalFont));
                    nested.addCell(new Phrase(String.valueOf(bi.getPharmaceuticalBillItem().getQty()), normalFont));
                    nested.addCell(new Phrase(String.valueOf(bi.getPharmaceuticalBillItem().getFreeQty()), normalFont));
                    nested.addCell(new Phrase(String.format("%,.2f", bi.getPharmaceuticalBillItem().getPurchaseRate()), normalFont));
                    if (hasCosting) {
                        nested.addCell(new Phrase(String.format("%,.2f", bi.getBillItemFinanceDetails().getLineCostRate()), normalFont));
                    }
                    Double discountRate = bi.getBillItemFinanceDetails().getLineDiscountRate() != null ? bi.getBillItemFinanceDetails().getLineDiscountRate().doubleValue() : null;
                    if (discountRate == null) {
                        if (bi.getReferanceBillItem().getBillItemFinanceDetails().getLineDiscountRate() != null) {
                            discountRate = bi.getReferanceBillItem().getBillItemFinanceDetails().getLineDiscountRate().doubleValue();
                        } else {
                            discountRate = 0.0;
                        }
                    }
                    nested.addCell(new Phrase(String.format("%,.2f", discountRate), normalFont));
                    nested.addCell(new Phrase(String.format("%,.2f", bi.getPharmaceuticalBillItem().getRetailRate()), normalFont));
                    nested.addCell(new Phrase(bi.getPharmaceuticalBillItem().getItemBatch().getBatchNo(), normalFont));
                    nested.addCell(new Phrase(bi.getItem().getMeasurementUnit() != null ? bi.getItem().getMeasurementUnit().getName() : "", normalFont));
                }

                PdfPCell nestedCell = new PdfPCell(nested);
                mainTable.addCell(nestedCell);
            }

            PdfPCell footerLabel = new PdfPCell(new Phrase("Total", boldFont));
            footerLabel.setColspan(8);
            footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            mainTable.addCell(footerLabel);

            mainTable.addCell(new Phrase(String.format("%,.2f", totalPurchase), boldFont));
            if (hasCosting) {
                mainTable.addCell(new Phrase(String.format("%,.2f", totalCost), boldFont));
            }
            mainTable.addCell(new Phrase(String.format("%,.2f", totalSale), boldFont));
            mainTable.addCell(new Phrase("", boldFont));

            document.add(mainTable);
            document.close();
            context.responseComplete();
        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    public void exportGrnReturnReportToExcel() {
        boolean hasCosting = configOptionApplicationController.getBooleanValueByKey("Manage Costing", true);

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=GRN_Return_Report.xlsx");

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");

        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("GRN Return Report");
            int rowIndex = 0;

            XSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);

            CellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);

            Row mainHeader = sheet.createRow(rowIndex++);
            Cell titleCell = mainHeader.createCell(0);
            titleCell.setCellValue("GRN Return Report");
            titleCell.setCellStyle(boldStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, hasCosting?11:10));

            String[] headers = {"Return No", "GRN No", "GRN Invoice No", "GRN Date", "Reference Institution", "Created At", "Approved At", "Supplier", "Purchase Value", "Sale Value", "Purchase Details"};

            if (hasCosting) {
                headers = new String[]{"Return No", "GRN No", "GRN Invoice No", "GRN Date", "Reference Institution", "Created At", "Approved At", "Supplier", "Purchase Value", "Cost Value", "Sale Value", "Purchase Details"};
            }

            Row columnHeader = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = columnHeader.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(boldStyle);
            }

            double totalPurchase = 0;
            double totalSale = 0;
            double totalCost = 0;

            for (Bill r : getBills()) {
                Row row = sheet.createRow(rowIndex++);
                int col = 0;

                row.createCell(col++).setCellValue(r.getDeptId());
                row.createCell(col++).setCellValue(r.getReferenceBill() != null ? r.getReferenceBill().getDeptId() : "");
                row.createCell(col++).setCellValue(r.getReferenceBill() != null ? r.getReferenceBill().getInvoiceNumber() : "");
                row.createCell(col++).setCellValue(r.getReferenceBill() != null && r.getReferenceBill().getCreatedAt() != null ? sdf.format(r.getReferenceBill().getCreatedAt()) : "");
                row.createCell(col++).setCellValue(r.getReferenceBill() != null && r.getReferenceBill().getReferenceInstitution() != null ? r.getReferenceBill().getReferenceInstitution().getName() : "");
                row.createCell(col++).setCellValue(r.getCreatedAt() != null ? sdf.format(r.getCreatedAt()) : "");
                row.createCell(col++).setCellValue(r.getReferenceBill() != null && r.getReferenceBill().getCreatedAt() != null ? sdf.format(r.getReferenceBill().getCreatedAt()) : "");
                row.createCell(col++).setCellValue(r.getToInstitution() != null ? r.getToInstitution().getName() : "");

                double purchase = r.getBillFinanceDetails().getLineNetTotal() != null ? r.getBillFinanceDetails().getLineNetTotal().doubleValue(): 0.0;
                double sale = r.getBillFinanceDetails().getTotalRetailSaleValue() != null ? r.getBillFinanceDetails().getTotalRetailSaleValue().doubleValue() : 0.0;
                double cost = hasCosting ? (r.getBillFinanceDetails().getTotalCostValue() != null ? r.getBillFinanceDetails().getTotalCostValue().doubleValue() : 0.0) : 0.0;

                double adjustedPurchase = purchase > 0 ? -purchase : purchase;
                double adjustedSale = sale > 0 ? -sale : sale;
                double adjustedCost = hasCosting ? (cost > 0 ? -cost : cost) : 0.0;

                totalPurchase += adjustedPurchase;
                totalSale += adjustedSale;
                if (hasCosting) {
                    totalCost += adjustedCost;
                }

                row.createCell(col++).setCellValue(adjustedPurchase);
                if (hasCosting) {
                    row.createCell(col++).setCellValue(adjustedCost);
                }
                row.createCell(col++).setCellValue(adjustedSale);

                Row subHeader = sheet.createRow(rowIndex++);
                String[] nestedHeaders = {"Item Name", "Qty", "Free Qty", "Purchase Rate", "Discount Rate", "MRP", "Batch", "UOM"};
                if (hasCosting) {
                    nestedHeaders = new String[]{"Item Name", "Qty", "Free Qty", "Purchase Rate", "Cost Rate", "Discount Rate", "MRP", "Batch", "UOM"};
                }

                for (int i = 0; i < nestedHeaders.length; i++) {
                    Cell cell = hasCosting ? subHeader.createCell(i + 11) : subHeader.createCell(i + 10);
                    cell.setCellValue(nestedHeaders[i]);
                    cell.setCellStyle(boldStyle);
                }

                for (BillItem bi : r.getBillItems()) {
                    Row itemRow = sheet.createRow(rowIndex++);
                    itemRow.createCell(hasCosting ? 11 :10).setCellValue(bi.getItem().getName());
                    itemRow.createCell(hasCosting ? 12 :11).setCellValue(bi.getPharmaceuticalBillItem().getQty());
                    itemRow.createCell(hasCosting ? 13 :12).setCellValue(bi.getPharmaceuticalBillItem().getFreeQty());
                    itemRow.createCell(hasCosting ? 14 :13).setCellValue(bi.getPharmaceuticalBillItem().getPurchaseRate());
                    if (hasCosting) {
                        itemRow.createCell(15).setCellValue(bi.getBillItemFinanceDetails().getLineCostRate().doubleValue());
                    }

                    Double discountRate = bi.getBillItemFinanceDetails().getLineDiscountRate() != null ? bi.getBillItemFinanceDetails().getLineDiscountRate().doubleValue() : null;
                    if (discountRate == null) {
                        if (bi.getReferanceBillItem().getBillItemFinanceDetails().getLineDiscountRate() != null) {
                            discountRate = bi.getReferanceBillItem().getBillItemFinanceDetails().getLineDiscountRate().doubleValue();
                        } else {
                            discountRate = 0.0;
                        }
                    }

                    itemRow.createCell(hasCosting ? 16 :14).setCellValue(discountRate);
                    itemRow.createCell(hasCosting ? 17 :15).setCellValue(bi.getPharmaceuticalBillItem().getRetailRate());
                    itemRow.createCell(hasCosting ? 18 : 16).setCellValue(bi.getPharmaceuticalBillItem().getItemBatch().getBatchNo());
                    itemRow.createCell(hasCosting ? 19 : 17).setCellValue(bi.getItem().getMeasurementUnit() != null ? bi.getItem().getMeasurementUnit().getName() : "");
                }
            }

            Row footer = sheet.createRow(rowIndex++);
            Cell totalLabel = footer.createCell(0);
            totalLabel.setCellValue("Total");
            totalLabel.setCellStyle(boldStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 7));

            Cell purchaseTotalCell = footer.createCell(8);
            purchaseTotalCell.setCellValue(totalPurchase);
            purchaseTotalCell.setCellStyle(boldStyle);

            if (hasCosting) {
                Cell costTotalCell = footer.createCell(9);
                costTotalCell.setCellValue(totalCost);
                costTotalCell.setCellStyle(boldStyle);
            }

            Cell saleTotalCell = footer.createCell(hasCosting ? 10 : 9);
            saleTotalCell.setCellValue(totalSale);
            saleTotalCell.setCellStyle(boldStyle);

            workbook.write(out);
            context.responseComplete();
        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    public void exportGrnCancellationReportToPdf() {
        boolean hasCosting = configOptionApplicationController.getBooleanValueByKey("Manage Costing", true);

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=GRN_Cancellation_Report.pdf");

        try (OutputStream out = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            com.itextpdf.text.Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("GRN Cancellation Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            PdfPTable mainTable = new PdfPTable(hasCosting? 12 : 11);
            mainTable.setWidthPercentage(100);
            if (hasCosting) {
                mainTable.setWidths(new float[]{2f, 2f, 2f, 2f, 2.5f, 2f, 2f, 2f, 2f, 2f, 2f, 6f});
            } else {
                mainTable.setWidths(new float[]{2f, 2f, 2f, 2f, 2.5f, 2f, 2f, 2f, 2f, 2f, 6f});
            }

            String[] headers = {"Cancelled No", "GRN No", "GRN Invoice No", "GRN Date", "Reference Institution", "Created At", "Approved At", "Supplier", "Purchase Value", "Sale Value", "Purchase Details"};
            if (hasCosting) {
                headers = new String[]{"Cancelled No", "GRN No", "GRN Invoice No", "GRN Date", "Reference Institution", "Created At", "Approved At", "Supplier", "Purchase Value", "Cost Value", "Sale Value", "Purchase Details"};
            }
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                mainTable.addCell(cell);
            }

            double totalPurchase = 0;
            double totalSale = 0;
            double totalCost = 0;

            for (Bill cb : getBills()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");

                mainTable.addCell(new Phrase(cb.getDeptId() != null ? cb.getDeptId() : "", normalFont));
                mainTable.addCell(new Phrase(cb.getReferenceBill() != null && cb.getReferenceBill().getDeptId() != null ? cb.getReferenceBill().getDeptId() : "", normalFont));
                mainTable.addCell(new Phrase(cb.getInvoiceNumber() != null ? cb.getInvoiceNumber() : "", normalFont));
                mainTable.addCell(new Phrase(cb.getReferenceBill() != null && cb.getReferenceBill().getCreatedAt() != null ? sdf.format(cb.getReferenceBill().getCreatedAt()) : "", normalFont));
                mainTable.addCell(new Phrase(cb.getReferenceInstitution() != null && cb.getReferenceInstitution().getName() != null ? cb.getReferenceInstitution().getName() : "", normalFont));
                mainTable.addCell(new Phrase(cb.getCreatedAt() != null ? sdf.format(cb.getCreatedAt()) : "", normalFont));
                mainTable.addCell(new Phrase(cb.getReferenceBill() != null && cb.getReferenceBill().getCreatedAt() != null ? sdf.format(cb.getReferenceBill().getCreatedAt()) : "", normalFont));
                mainTable.addCell(new Phrase(cb.getFromInstitution() != null && cb.getFromInstitution().getName() != null ? cb.getFromInstitution().getName() : "", normalFont));

                Double purchaseObj = cb.getBillFinanceDetails() != null ? cb.getBillFinanceDetails().getLineNetTotal().doubleValue() : null;
                Double saleObj = cb.getBillFinanceDetails() != null ? cb.getBillFinanceDetails().getTotalRetailSaleValue().doubleValue() : null;
                Double costObj = hasCosting ? (cb.getBillFinanceDetails() != null ? cb.getBillFinanceDetails().getTotalCostValue().doubleValue() : null) : null;

                double purchase = purchaseObj != null ? purchaseObj : 0;
                double sale = saleObj != null ? saleObj : 0;
                double cost = hasCosting ? (costObj != null ? costObj : 0) : 0;

                double adjustedPurchase = purchase > 0 ? -purchase : purchase;
                double adjustedSale = sale > 0 ? -sale : sale;
                double adjustedCost = hasCosting ? (cost > 0 ? -cost : cost) : 0.0;

                totalPurchase += adjustedPurchase;
                totalSale += adjustedSale;
                if (hasCosting) {
                    totalCost += adjustedCost;
                }

                mainTable.addCell(new Phrase(String.format("%,.2f", adjustedPurchase), normalFont));
                if (hasCosting) {
                    mainTable.addCell(new Phrase(String.format("%,.2f", adjustedCost), normalFont));
                }
                mainTable.addCell(new Phrase(String.format("%,.2f", adjustedSale), normalFont));

                PdfPTable nested = new PdfPTable(hasCosting? 9:8);
                nested.setWidthPercentage(100);
                String[] nestedHeaders = {"Item Name", "Qty", "Free Qty", "Purchase Rate", "Discount Rate", "MRP", "Batch", "UOM"};
                if (hasCosting) {
                    nestedHeaders = new String[]{"Item Name", "Qty", "Free Qty", "Purchase Rate", "Cost Rate", "Discount Rate", "MRP", "Batch", "UOM"};
                }
                for (String h : nestedHeaders) {
                    PdfPCell nh = new PdfPCell(new Phrase(h, boldFont));
                    nh.setHorizontalAlignment(Element.ALIGN_CENTER);
                    nested.addCell(nh);
                }

                if (cb.getBillItems() != null) {
                    for (BillItem bi : cb.getBillItems()) {
                        nested.addCell(new Phrase(bi.getItem() != null && bi.getItem().getName() != null ? bi.getItem().getName() : "", normalFont));
                        nested.addCell(new Phrase(bi.getPharmaceuticalBillItem() != null ? String.valueOf(bi.getPharmaceuticalBillItem().getQty()) : "", normalFont));
                        nested.addCell(new Phrase(bi.getPharmaceuticalBillItem() != null ? String.valueOf(bi.getPharmaceuticalBillItem().getFreeQty()) : "", normalFont));
                        nested.addCell(new Phrase(bi.getPharmaceuticalBillItem() != null ? String.format("%,.2f", bi.getPharmaceuticalBillItem().getPurchaseRate()) : "", normalFont));
                        if (hasCosting) {
                            nested.addCell(new Phrase(bi.getBillItemFinanceDetails() != null ? String.format("%,.2f", bi.getBillItemFinanceDetails().getLineCostRate()) : "", normalFont));
                        }
                        Double discountRate = bi.getBillItemFinanceDetails().getLineDiscountRate() != null ? bi.getBillItemFinanceDetails().getLineDiscountRate().doubleValue() : null;
                        if (discountRate == null) {
                            if (bi.getReferanceBillItem().getBillItemFinanceDetails().getLineDiscountRate() != null) {
                                discountRate = bi.getReferanceBillItem().getBillItemFinanceDetails().getLineDiscountRate().doubleValue();
                            } else {
                                discountRate = 0.0;
                            }
                        }
                        nested.addCell(new Phrase(String.format("%,.2f", discountRate), normalFont));
                        nested.addCell(new Phrase(bi.getPharmaceuticalBillItem() != null ? String.format("%,.2f", bi.getPharmaceuticalBillItem().getRetailRate()) : "", normalFont));
                        nested.addCell(new Phrase(bi.getPharmaceuticalBillItem() != null && bi.getPharmaceuticalBillItem().getItemBatch() != null && bi.getPharmaceuticalBillItem().getItemBatch().getBatchNo() != null ? bi.getPharmaceuticalBillItem().getItemBatch().getBatchNo() : "", normalFont));
                        nested.addCell(new Phrase(bi.getItem() != null && bi.getItem().getMeasurementUnit() != null && bi.getItem().getMeasurementUnit().getName() != null ? bi.getItem().getMeasurementUnit().getName() : "", normalFont));
                    }
                }

                PdfPCell nestedCell = new PdfPCell(nested);
                mainTable.addCell(nestedCell);
            }

            PdfPCell footerLabel = new PdfPCell(new Phrase("Total", boldFont));
            footerLabel.setColspan(8);
            footerLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            mainTable.addCell(footerLabel);

            mainTable.addCell(new Phrase(String.format("%,.2f", totalPurchase), boldFont));
            if (hasCosting) {
                mainTable.addCell(new Phrase(String.format("%,.2f", totalCost), boldFont));
            }
            mainTable.addCell(new Phrase(String.format("%,.2f", totalSale), boldFont));
            mainTable.addCell(new Phrase("", boldFont));

            document.add(mainTable);
            document.close();
            context.responseComplete();
        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    public void exportGrnCancellationReportToExcel() {
        boolean hasCosting = configOptionApplicationController.getBooleanValueByKey("Manage Costing", true);

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=GRN_Cancellation_Report.xlsx");

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");

        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("GRN Cancellation Report");
            int rowIndex = 0;

            XSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);

            CellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);

            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("GRN Cancellation Report");
            titleCell.setCellStyle(boldStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, hasCosting?11:10));

            Row headerRow = sheet.createRow(rowIndex++);
            String[] headers = {"Cancelled No", "GRN No", "GRN Invoice No", "GRN Date", "Reference Institution", "Created At", "Approved At", "Supplier", "Purchase Value", "Sale Value", "Purchase Details"};
            if (hasCosting) {
                headers = new String[]{"Cancelled No", "GRN No", "GRN Invoice No", "GRN Date", "Reference Institution", "Created At", "Approved At", "Supplier", "Purchase Value", "Cost Value", "Sale Value", "Purchase Details"};
            }
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(boldStyle);
            }

            double totalPurchase = 0;
            double totalSale = 0;
            double totalCost = 0;

            for (Bill cb : getBills()) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(cb.getDeptId() != null ? cb.getDeptId() : "");
                row.createCell(1).setCellValue(cb.getReferenceBill() != null && cb.getReferenceBill().getDeptId() != null ? cb.getReferenceBill().getDeptId() : "");
                row.createCell(2).setCellValue(cb.getInvoiceNumber() != null ? cb.getInvoiceNumber() : "");
                row.createCell(3).setCellValue(cb.getReferenceBill() != null && cb.getReferenceBill().getCreatedAt() != null ? sdf.format(cb.getReferenceBill().getCreatedAt()) : "");
                row.createCell(4).setCellValue(cb.getReferenceInstitution() != null && cb.getReferenceInstitution().getName() != null ? cb.getReferenceInstitution().getName() : "");
                row.createCell(5).setCellValue(cb.getCreatedAt() != null ? sdf.format(cb.getCreatedAt()) : "");
                row.createCell(6).setCellValue(cb.getReferenceBill() != null && cb.getReferenceBill().getCreatedAt() != null ? sdf.format(cb.getReferenceBill().getCreatedAt()) : "");
                row.createCell(7).setCellValue(cb.getFromInstitution() != null && cb.getFromInstitution().getName() != null ? cb.getFromInstitution().getName() : "");

                Double purchaseObj = cb.getBillFinanceDetails() != null ? cb.getBillFinanceDetails().getLineNetTotal().doubleValue() : null;
                Double saleObj = cb.getBillFinanceDetails() != null ? cb.getBillFinanceDetails().getTotalRetailSaleValue().doubleValue() : null;
                Double costObj = hasCosting ? (cb.getBillFinanceDetails() != null ? cb.getBillFinanceDetails().getTotalCostValue().doubleValue() : null) : null;

                double purchase = purchaseObj != null ? purchaseObj : 0;
                double sale = saleObj != null ? saleObj : 0;
                double cost = hasCosting ? (costObj != null ? costObj : 0) : 0;

                double adjustedPurchase = purchase > 0 ? -purchase : purchase;
                double adjustedSale = sale > 0 ? -sale : sale;
                double adjustedCost = hasCosting ? (cost > 0 ? -cost : cost) : 0.0;

                totalPurchase += adjustedPurchase;
                totalSale += adjustedSale;
                if (hasCosting) {
                    totalCost += adjustedCost;
                }

                row.createCell(8).setCellValue(adjustedPurchase);
                if (hasCosting) {
                    row.createCell(9).setCellValue(adjustedCost);
                }
                row.createCell(hasCosting?10:9).setCellValue(adjustedSale);

                Row nestedHeader = sheet.createRow(rowIndex++);
                String[] nestedHeaders = {"Item Name", "Qty", "Free Qty", "Purchase Rate", "Discount Rate", "MRP", "Batch", "UOM"};
                if (hasCosting) {
                    nestedHeaders = new String[]{"Item Name", "Qty", "Free Qty", "Purchase Rate", "Cost Rate", "Discount Rate", "MRP", "Batch", "UOM"};
                }
                for (int i = 0; i < nestedHeaders.length; i++) {
                    Cell cell = hasCosting ? nestedHeader.createCell(i + 11) : nestedHeader.createCell(i + 10);
                    cell.setCellValue(nestedHeaders[i]);
                    cell.setCellStyle(boldStyle);
                }

                if (cb.getBillItems() != null) {
                    for (BillItem bi : cb.getBillItems()) {
                        Row itemRow = sheet.createRow(rowIndex++);
                        itemRow.createCell(hasCosting ? 11 : 10).setCellValue(bi.getItem() != null && bi.getItem().getName() != null ? bi.getItem().getName() : "");
                        itemRow.createCell(hasCosting ? 12 : 11).setCellValue(bi.getPharmaceuticalBillItem() != null ? bi.getPharmaceuticalBillItem().getQty() : 0);
                        itemRow.createCell(hasCosting ? 13 : 12).setCellValue(bi.getPharmaceuticalBillItem() != null ? bi.getPharmaceuticalBillItem().getFreeQty() : 0);
                        itemRow.createCell(hasCosting ? 14 : 13).setCellValue(bi.getPharmaceuticalBillItem() != null ? bi.getPharmaceuticalBillItem().getPurchaseRate() : 0);
                        if (hasCosting) {
                            itemRow.createCell(15).setCellValue(bi.getBillItemFinanceDetails() != null ? bi.getBillItemFinanceDetails().getLineCostRate().doubleValue() : 0);
                        }

                        Double discountRate = bi.getBillItemFinanceDetails().getLineDiscountRate() != null ? bi.getBillItemFinanceDetails().getLineDiscountRate().doubleValue() : null;
                        if (discountRate == null) {
                            if (bi.getReferanceBillItem().getBillItemFinanceDetails().getLineDiscountRate() != null) {
                                discountRate = bi.getReferanceBillItem().getBillItemFinanceDetails().getLineDiscountRate().doubleValue();
                            } else {
                                discountRate = 0.0;
                            }
                        }

                        itemRow.createCell(hasCosting ? 16 :14).setCellValue(discountRate);
                        itemRow.createCell(hasCosting ? 17 : 15).setCellValue(bi.getPharmaceuticalBillItem() != null ? bi.getPharmaceuticalBillItem().getRetailRate() : 0);
                        itemRow.createCell(hasCosting ? 18 : 16).setCellValue(bi.getPharmaceuticalBillItem() != null && bi.getPharmaceuticalBillItem().getItemBatch() != null && bi.getPharmaceuticalBillItem().getItemBatch().getBatchNo() != null ? bi.getPharmaceuticalBillItem().getItemBatch().getBatchNo() : "");
                        itemRow.createCell(hasCosting ? 19 : 17).setCellValue(bi.getItem() != null && bi.getItem().getMeasurementUnit() != null && bi.getItem().getMeasurementUnit().getName() != null ? bi.getItem().getMeasurementUnit().getName() : "");
                    }
                }
            }

            Row footerRow = sheet.createRow(rowIndex++);
            Cell footerLabel = footerRow.createCell(0);
            footerLabel.setCellValue("Total");
            footerLabel.setCellStyle(boldStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 7));

            Cell purchaseTotalCell = footerRow.createCell(8);
            purchaseTotalCell.setCellValue(totalPurchase);
            purchaseTotalCell.setCellStyle(boldStyle);

            if (hasCosting) {
                Cell costTotalCell = footerRow.createCell(9);
                costTotalCell.setCellValue(totalCost);
                costTotalCell.setCellStyle(boldStyle);
            }

            Cell saleTotalCell = footerRow.createCell(hasCosting ? 10 : 9);
            saleTotalCell.setCellValue(totalSale);
            saleTotalCell.setCellStyle(boldStyle);

            workbook.write(out);
            context.responseComplete();
        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    public void createConsumptionReportTable() {
        reportTimerController.trackReportExecution(() -> {
            resetFields();

            switch (reportType) {
                case "byBillItem":
                    generateConsumptionReportTableByBillItems(BillType.PharmacyDisposalIssue);
                    break;

                case "byBill":
                    generateConsumptionReportTableByBill(BillType.PharmacyDisposalIssue);
                    break;

                case "summeryReport":
                case "categoryWise":
                    generateConsumptionReportTableByDepartmentAndCategoryWise(BillType.PharmacyDisposalIssue);
                    break;

                default:
                    throw new IllegalArgumentException("Invalid report type: " + reportType);
            }
        }, InventoryReports.CONSUMPTION_REPORT, sessionController.getLoggedUser());
    }

    private void resetFields() {
        bills = null;
        billItems = null;
        departmentSummaries = null;
        issueDepartmentCategoryWiseItems = null;
        pharmacyRows = null;
        resultsList = null;
        totalCreditPurchaseValue = 0.0;
        totalCashPurchaseValue = 0.0;
        totalPurchase = 0.0;
        grantIssueQty = 0.0;
    }

    public void generateConsumptionReportTableByBill(BillType billType) {
        try {
            bills = new ArrayList<>();

            String sql = "SELECT b FROM Bill b WHERE b.retired = false"
                    + " and b.billType = :btp"
                    + " and b.createdAt between :fromDate and :toDate";

            Map<String, Object> tmp = new HashMap<>();

            tmp.put("btp", billType);
            tmp.put("fromDate", getFromDate());
            tmp.put("toDate", getToDate());

            if (institution != null) {
                sql += " and b.institution = :fIns";
                tmp.put("fIns", institution);
            }

            if (site != null) {
                sql += " and b.department.site = :site";
                tmp.put("site", site);
            }

            if (dept != null) {
                sql += " and b.department = :dept";
                tmp.put("dept", dept);
            }

            if (category != null) {
                sql += " AND EXISTS (SELECT bi FROM BillItem bi WHERE bi.bill = b AND bi.item.category = :category)";
                tmp.put("category", category);
            }

            if (item != null) {
                sql += " AND EXISTS (SELECT bi FROM BillItem bi WHERE bi.bill = b AND bi.item = :item)";
                tmp.put("item", item);
            }

            if (toDepartment != null) {
                sql += " AND b.toDepartment = :toDept";
                tmp.put("toDept", toDepartment);
            }

            sql += " order by b.createdAt asc";

            bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP);

            totalPurchase = 0.0;
            totalCostValue = 0.0;

            pharmacyRows = new ArrayList<>();
            Set<Item> allUniqueItems = new HashSet<>();

            for (Bill b : bills) {
                if (b != null && b.getBillItems() != null) {
                    for (BillItem bi : b.getBillItems()) {
                        if (bi.getItem() != null) {
                            allUniqueItems.add(bi.getItem());
                        }
                    }
                }
            }

            List<ItemBatch> allBatches = getItemBatchesByItems(new ArrayList<>(allUniqueItems));

            Map<Long, ItemBatch> latestBatchMap = allBatches.stream()
                    .filter(ib -> ib.getItem() != null && ib.getId() != null)
                    .collect(Collectors.toMap(
                            ib -> ib.getItem().getId(),
                            Function.identity(),
                            BinaryOperator.maxBy(Comparator.comparing(ItemBatch::getId))
                    ));

            for (Bill b : bills) {
                if (b != null && b.getBillItems() != null && !b.getBillItems().isEmpty()) {
                    PharmacyRow row = new PharmacyRow();
                    row.setBill(b);

                    double costRateTotal = 0.0;

                    for (BillItem bi : b.getBillItems()) {
                        Item item = bi.getItem();
                        if (item != null) {
                            ItemBatch latestBatch = latestBatchMap.get(item.getId());

                            if (latestBatch != null && latestBatch.getCostRate() != null) {
                                costRateTotal += latestBatch.getCostRate() * bi.getQty();
                            }
                        }
                    }

                    row.setCostValue(costRateTotal);
                    totalPurchase += b.getStockBill() != null ? b.getStockBill().getStockValueAtPurchaseRates() : 0.0;
                    totalCostValue += costRateTotal;

                    pharmacyRows.add(row);
                }
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, " Something Went Wrong!");
        }
    }

    public void generateConsumptionReportTableByBillItems(BillType billType) {
        try {
            billItems = new ArrayList<>();
            Map<String, Object> parameters = new HashMap<>();
            StringBuilder sql = new StringBuilder();

            sql.append("SELECT bi FROM BillItem bi WHERE bi.retired = false ");
            sql.append("AND bi.bill.retired = false ");
            sql.append("AND bi.bill.createdAt BETWEEN :fromDate AND :toDate ");
            sql.append("AND bi.bill.billType = :billType ");

            parameters.put("fromDate", fromDate);
            parameters.put("toDate", toDate);
            parameters.put("billType", billType);

            if (institution != null) {
                sql.append("AND bi.bill.institution = :institution ");
                parameters.put("institution", institution);
            }

            if (site != null) {
                sql.append("AND bi.bill.department.site = :site ");
                parameters.put("site", site);
            }

            if (dept != null) {
                sql.append("AND bi.bill.department = :department ");
                parameters.put("department", dept);
            }

            if (category != null) {
                sql.append("AND bi.item.category = :category ");
                parameters.put("category", category);
            }

            if (item != null) {
                sql.append("AND bi.item = :item ");
                parameters.put("item", item);
            }

            if (toDepartment != null) {
                sql.append("AND bi.bill.toDepartment = :toDepartment ");
                parameters.put("toDepartment", toDepartment);
            }

            sql.append("ORDER BY bi.bill.createdAt ASC");

            billItems = getBillItemFacade().findByJpql(sql.toString(), parameters, TemporalType.TIMESTAMP);
            Set<Item> uniqueItems = new HashSet<>();

            for (BillItem i : billItems) {
                if (i.getItem() != null) {
                    uniqueItems.add(i.getItem());
                }
            }

            if (billItems.isEmpty()) {
                pharmacyRows = new ArrayList<>();
                return;
            }

            List<Item> items = new ArrayList<>(uniqueItems);

            if (!items.isEmpty()) {
                List<ItemBatch> allBatches = getItemBatchesByItems(items);
                pharmacyRows = createPharmacyRowsByBillItemsAndItemBatch(billItems, allBatches);
            }

            totalPurchase = 0.0;
            totalCostValue = 0.0;

            for (PharmacyRow row : pharmacyRows) {
                totalPurchase += row.getBillItem().getPharmaceuticalBillItem().getPurchaseRate() * row.getBillItem().getQty();
                totalCostValue += row.getItemBatch().getCostRate() * row.getBillItem().getQty();
            }

        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, "Error generating consumption report by bill items", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to generate report. Please try again."));
        }
    }

    public void generateConsumptionReportTableAsSummary(BillType billType) {
        // Initialize bill types
        List<BillType> bt = new ArrayList<>();
        bt.add(BillType.PharmacyIssue);

        // Prepare the SQL query
        String sql = "SELECT b.department.name, SUM(b.netTotal) FROM Bill b "
                + "WHERE b.retired = false "
                + "AND b.billType = :btp "
                + "AND b.createdAt BETWEEN :fromDate AND :toDate";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("btp", billType);
        parameters.put("fromDate", getFromDate());
        parameters.put("toDate", getToDate());

        // Add optional filters
        if (institution != null) {
            sql += " AND b.institution = :fIns";
            parameters.put("fIns", institution);
        }
        if (site != null) {
            sql += " AND b.department.site = :site";
            parameters.put("site", site);
        }
        if (dept != null) {
            sql += " AND b.department = :dept";
            parameters.put("dept", dept);
        }
        if (toDepartment != null) {
            sql += " AND b.toDepartment = :toDept";
            parameters.put("toDept", toDepartment);
        }

        // Group by department name and order by net total in descending order
        sql += " GROUP BY b.department.name ORDER BY SUM(b.netTotal) DESC";

        try {
            // Execute the query and fetch summarized data
            List<Object[]> results = getBillFacade().findObjectsArrayByJpql(sql, parameters, TemporalType.TIMESTAMP);

            // Process results into a summary list
            departmentSummaries = new ArrayList<>();
            double grandTotal = 0;

            for (Object[] result : results) {
                String departmentName = (String) result[0]; // Extract department name
                Double netTotal = (Double) result[1];       // Extract net total
                grandTotal += netTotal;                     // Accumulate the grand total

                // Add summary data
                departmentSummaries.add(new PharmacySummery(departmentName, netTotal));
            }

            // Add grand total
            departmentSummaries.add(new PharmacySummery("Total", grandTotal));

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Something Went Wrong!");
        }
    }

    @Deprecated
    public void generateConsumptionReportTableByDepartmentAndCategoryWise() {
        totalPurchase = 0.0;
        grantIssueQty = 0.0;

        Map<String, Object> parameters = new HashMap<>();
        StringBuilder sql = new StringBuilder();

        // Base query
        sql.append("SELECT bi.bill.toDepartment, ");
        sql.append("bi.item.category, ");
        sql.append("bi.item, ");
        sql.append("bi.pharmaceuticalBillItem.purchaseRate, ");
        sql.append("SUM(bi.qty), ");
        sql.append("SUM(bi.qty * bi.pharmaceuticalBillItem.purchaseRate) ");  // Total amount calculation
        sql.append("FROM BillItem bi ");
        sql.append("WHERE bi.retired = false ");
        sql.append("AND bi.bill.retired = false ");
        sql.append("AND bi.bill.createdAt BETWEEN :fromDate AND :toDate ");
        sql.append("AND bi.bill.billType = :billType ");

        // Mandatory parameters
        parameters.put("fromDate", fromDate);
        parameters.put("toDate", toDate);
        parameters.put("billType", BillType.PharmacyTransferIssue);

        // Dynamic filters
        if (institution != null) {
            sql.append("AND bi.bill.institution = :institution ");
            parameters.put("institution", institution);
        }

        if (site != null) {
            sql.append("AND bi.bill.department.site = :site ");
            parameters.put("site", site);
        }

        if (dept != null) {
            sql.append("AND bi.bill.department = :department ");
            parameters.put("department", dept);
        }

        if (category != null) {
            sql.append("AND bi.item.category = :category ");
            parameters.put("category", category);
        }

        if (item != null) {
            sql.append("AND bi.item = :item ");
            parameters.put("item", item);
        }

        if (toDepartment != null) {
            sql.append("AND bi.bill.toDepartment = :toDepartment ");
            parameters.put("toDepartment", toDepartment);
        }

        // Group by department and category
        sql.append("GROUP BY bi.bill.toDepartment, bi.item.category, bi.item, bi.pharmaceuticalBillItem.purchaseRate ");
        sql.append("ORDER BY bi.bill.toDepartment, bi.item.category");

        issueDepartmentCategoryWiseItems = new ArrayList<>();

        try {
            List<Object[]> results = getBillItemFacade().findObjectsArrayByJpql(sql.toString(), parameters, TemporalType.TIMESTAMP);

            if (results.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "No Data", "No records found for the selected criteria."));
                return;
            }

            for (Object[] result : results) {
                CategoryWithItem report = new CategoryWithItem();

                if (result[0] instanceof Department) {
                    report.setDepartment((Department) result[0]);
                }
                if (result[1] instanceof Category) {
                    report.setCategory((Category) result[1]);
                }
                if (result[2] instanceof Item) {
                    report.setItm((Item) result[2]);
                }
                if (result[3] instanceof Double) {
                    report.setPurchaseRate((Double) result[3]);
                }
                if (result[4] instanceof Double) {
                    report.setQty((Double) result[4]);
                }
                if (result[5] instanceof Double) {
                    report.setTotal((Double) result[5]);
                }

                issueDepartmentCategoryWiseItems.add(report);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to generate report. Please try again."));
        }

        for (CategoryWithItem i : issueDepartmentCategoryWiseItems) {
            totalPurchase += i.getTotal();
            grantIssueQty += i.getQty();
        }
    }

    public List<DepartmentCategoryWiseItems> generateConsumptionReportTableByDepartmentAndCategoryWise(BillType billType) {
        totalSaleValue = 0.0;
        totalCostValue = 0.0;
        qty = 0.0;

        Map<String, Object> parameters = new HashMap<>();
        String jpql = "SELECT new com.divudi.core.data.DepartmentCategoryWiseItems("
                + "bi.bill.department, "
                + "bi.bill.toDepartment, "
                + "bi.item, "
                + "bi.item.category, "
                + "SUM(bi.qty * bi.pharmaceuticalBillItem.purchaseRate), "
                + "COALESCE(ib.costRate, 0.0), "
                + "bi.pharmaceuticalBillItem.purchaseRate, "
                + "SUM(bi.qty)) "
                + "FROM BillItem bi "
                + "JOIN ItemBatch ib ON ib.item = bi.item "
                + "WHERE bi.retired = false AND bi.bill.retired = false "
                + "AND ib.id = (SELECT MAX(ib2.id) FROM ItemBatch ib2 WHERE ib2.item = bi.item) "
                + "AND bi.bill.createdAt BETWEEN :fromDate AND :toDate "
                + "AND bi.bill.billType = :billType ";

        // Mandatory parameters
        parameters.put("fromDate", fromDate);
        parameters.put("toDate", toDate);
        parameters.put("billType", billType);

        // Dynamic filters
        if (institution != null) {
            jpql += "AND bi.bill.institution = :institution ";
            parameters.put("institution", institution);
        }

        if (site != null) {
            jpql += "AND bi.bill.department.site = :site ";
            parameters.put("site", site);
        }

        if (dept != null) {
            jpql += "AND bi.bill.department = :department ";
            parameters.put("department", dept);
        }

        if (category != null) {
            jpql += "AND bi.item.category = :category ";
            parameters.put("category", category);
        }

        if (item != null) {
            jpql += "AND bi.item = :item ";
            parameters.put("item", item);
        }

        if (toDepartment != null) {
            jpql += "AND bi.bill.toDepartment = :toDepartment ";
            parameters.put("toDepartment", toDepartment);
        }

        // Group by clause
        jpql += "GROUP BY bi.bill.department, bi.bill.toDepartment, bi.item, bi.item.category, COALESCE(ib.costRate, 0.0), bi.pharmaceuticalBillItem.purchaseRate "
                + "ORDER BY bi.bill.toDepartment, bi.item.category";

        try {
            resultsList = (List<DepartmentCategoryWiseItems>) getBillItemFacade().findLightsByJpql(jpql, parameters, TemporalType.TIMESTAMP);

            if (resultsList.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "No Data", "No records found for the selected criteria."));
                return Collections.emptyList();
            }

            totalPurchase = 0.0;
            grantIssueQty = 0.0;
            for (DepartmentCategoryWiseItems i : resultsList) {
                totalPurchase += i.getNetTotal();
                grantIssueQty += i.getQty();
            }
            if ("summeryReport".equals(reportType)) {
                generateConsumptionReportTableAsDepartmentSummary(resultsList);
            } else if ("categoryWise".equals(reportType)) {
                generateConsumptionReportTableAsCategoryWise(resultsList);
            }
            return resultsList;
        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, "Error generating consumption report by department and category", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to generate report. Please try again."));
            return Collections.emptyList();
        }
    }

    public void generateConsumptionReportTableAsCategoryWise(final List<DepartmentCategoryWiseItems> list) {
        totalSaleValue = 0.0;
        totalCostValue = 0.0;
        // Department Name -> Category Name -> Item List
        Map<String, Map<String, List<DepartmentCategoryWiseItems>>> departmentCategoryMap = new HashMap<>();
        // Category Name -> Item List
        Map<String, List<DepartmentCategoryWiseItems>> categorizedItems = new HashMap<>();
        // Department Name -> Category Name -> [Total Value, Total Cost Value]
        Map<String, Map<String, Double[]>> departmentTotals = new TreeMap<>();

        for (DepartmentCategoryWiseItems item : list) {
            String departmentName = item.getConsumptionDepartment().getName();
            String categoryName = item.getCategory().getName();

            if (item.getQty() == 0) {
                continue;
            }

            if (categoryName == null || categoryName.trim().isEmpty()) {
                continue;
            }

            departmentCategoryMap
                    .computeIfAbsent(departmentName, k -> new TreeMap<>())
                    .computeIfAbsent(categoryName, k -> new ArrayList<>())
                    .add(item);

            categorizedItems
                    .computeIfAbsent(categoryName, k -> new ArrayList<>())
                    .add(item);

            departmentTotals
                    .computeIfAbsent(departmentName, k -> new TreeMap<>())
                    .merge(categoryName,
                            new Double[]{item.getNetTotal(), (item.getCostRate() != null ? item.getCostRate() * item.getQty() : 0.0)},
                            (existing, newValues) -> new Double[]{
                                    existing[0] + newValues[0],  // Sum net totals
                                    existing[1] + newValues[1]   // Sum cost rates
                            });

            totalSaleValue += item.getNetTotal();
            totalCostValue += (item.getCostRate() != null ? item.getCostRate() * item.getQty() : 0.0);
        }

        setDepartmentCategoryMap(departmentCategoryMap);
        setDepartmentTotals(departmentTotals);
    }

    public String getCategoryTotalForConsumptionReport(final String departmentName, final String categoryName) {
        double total = departmentTotals
                .getOrDefault(departmentName, Collections.emptyMap())
                .getOrDefault(categoryName, new Double[]{0.0, 0.0})[0];

        return formatNumber(total);
    }

    public String getDepartmentTotalForConsumptionReport(final String departmentName) {
        double total = departmentTotals
                .getOrDefault(departmentName, Collections.emptyMap())
                .values()
                .stream()
                .mapToDouble(arr -> arr[0])
                .sum();

        return formatNumber(total);
    }

    public String getCategoryCostTotalForConsumptionReport(final String departmentName, final String categoryName) {
        double total = departmentTotals
                .getOrDefault(departmentName, Collections.emptyMap())
                .getOrDefault(categoryName, new Double[]{0.0, 0.0})[1];

        return formatNumber(total);
    }

    public String getDepartmentCostTotalForConsumptionReport(final String departmentName) {
        double total = departmentTotals
                .getOrDefault(departmentName, Collections.emptyMap())
                .values()
                .stream()
                .mapToDouble(arr -> arr[1])
                .sum();

        return formatNumber(total);
    }

    private String formatNumber(double value) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(value);
    }

    public void generateConsumptionReportTableAsDepartmentSummary(final List<DepartmentCategoryWiseItems> list) {
        departmentSummaries = new ArrayList<>();
        totalSaleValue = 0.0;
        totalCostValue = 0.0;

        // Main Department -> [Total Value, Total Cost Value]
        Map<String, Map<String, Double[]>> departmentTotals = new TreeMap<>();
        // Pharmacy Department -> [Total Value, Total Cost Value]
        Map<String, Double[]> pharmacyTotals = new TreeMap<>();

        for (DepartmentCategoryWiseItems item : list) {
            String mainDepartmentName = item.getMainDepartment().getName();
            String consumptionDepartmentName = item.getConsumptionDepartment().getName();
            double paidAmount = item.getNetTotal();
            double costAmount = item.getCostRate() != null ? item.getCostRate() * item.getQty() : 0.0;

            if (paidAmount == 0.0) {
                continue;
            }

            // Store net total at [0] and cost rate at [1] for pharmacy totals
            pharmacyTotals.merge(mainDepartmentName,
                    new Double[]{paidAmount, costAmount},
                    (existing, newValues) -> new Double[]{
                            existing[0] + newValues[0],  // Sum net totals
                            existing[1] + newValues[1]   // Sum cost rates
                    });

            // Store net total at [0] and cost rate at [1] for department totals
            departmentTotals
                    .computeIfAbsent(mainDepartmentName, k -> new TreeMap<>())
                    .merge(consumptionDepartmentName,
                            new Double[]{paidAmount, costAmount},
                            (existing, newValues) -> new Double[]{
                                    existing[0] + newValues[0],  // Sum net totals
                                    existing[1] + newValues[1]   // Sum cost rates
                            });

            totalSaleValue += paidAmount;
            totalCostValue += costAmount;
        }

        setPharmacyTotals(pharmacyTotals);
        setDepartmentTotals(departmentTotals);
    }

    public void exportConsumptionReportCategoryWiseToExcel() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Category_Wise_Consumption_Report.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Category Wise Report");
            int rowIndex = 0;

            XSSFCellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            XSSFCellStyle amountStyle = workbook.createCellStyle();
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Category Wise Issue Details by Department");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            for (Map.Entry<String, Map<String, List<DepartmentCategoryWiseItems>>> deptEntry : departmentCategoryMap.entrySet()) {
                String departmentName = deptEntry.getKey();

                Row deptRow = sheet.createRow(rowIndex++);
                Cell deptCell = deptRow.createCell(0);
                deptCell.setCellValue("Department: " + departmentName);
                deptCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 4));

                Row headerRow = sheet.createRow(rowIndex++);
                String[] headers = {"Category", "Rate", "Quantity", "Cost Value", "Value"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                for (Map.Entry<String, List<DepartmentCategoryWiseItems>> categoryEntry : deptEntry.getValue().entrySet()) {
                    String categoryName = categoryEntry.getKey();
                    List<DepartmentCategoryWiseItems> items = categoryEntry.getValue();

                    Row categoryRow = sheet.createRow(rowIndex++);
                    Cell categoryCell = categoryRow.createCell(0);
                    categoryCell.setCellValue(categoryName);
                    categoryCell.setCellStyle(headerStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, 4));

                    for (DepartmentCategoryWiseItems item : items) {
                        Row dataRow = sheet.createRow(rowIndex++);
                        dataRow.createCell(0).setCellValue(item.getItem().getName());
                        dataRow.createCell(1).setCellValue(item.getPurchaseRate());
                        dataRow.createCell(2).setCellValue(item.getQty());
                        dataRow.createCell(3).setCellValue(item.getCostRate() != null ? item.getCostRate() * item.getQty() : 0.0);
                        dataRow.getCell(3).setCellStyle(amountStyle);
                        Cell valueCell = dataRow.createCell(4);
                        valueCell.setCellValue(item.getNetTotal());
                        valueCell.setCellStyle(amountStyle);
                    }

                    Row categoryTotalRow = sheet.createRow(rowIndex++);
                    categoryTotalRow.createCell(1).setCellValue("");
                    categoryTotalRow.createCell(2).setCellValue("Category Total:");
                    categoryTotalRow.createCell(3).setCellValue(getCategoryCostTotalForConsumptionReport(departmentName, categoryName));
                    Cell categoryTotalCell = categoryTotalRow.createCell(4);
                    categoryTotalCell.setCellValue(getCategoryTotalForConsumptionReport(departmentName, categoryName));
                    categoryTotalCell.setCellStyle(amountStyle);
                }

                Row departmentTotalRow = sheet.createRow(rowIndex++);
                departmentTotalRow.createCell(1).setCellValue("");
                departmentTotalRow.createCell(2).setCellValue("Department Total:");
                departmentTotalRow.createCell(3).setCellValue(getDepartmentCostTotalForConsumptionReport(departmentName));
                departmentTotalRow.getCell(3).setCellStyle(amountStyle);
                Cell departmentTotalCell = departmentTotalRow.createCell(4);
                departmentTotalCell.setCellValue(getDepartmentTotalForConsumptionReport(departmentName));
                departmentTotalCell.setCellStyle(amountStyle);
            }

            Row finalTotalRow = sheet.createRow(rowIndex++);
            finalTotalRow.createCell(0).setCellValue("Total");
            finalTotalRow.createCell(3).setCellValue(String.format("%.2f", totalCostValue));
            finalTotalRow.createCell(4).setCellValue(String.format("%.2f", totalSaleValue));

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            context.responseComplete();
        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, e.getMessage(), e);

        }
    }

    public void exportConsumptionReportCategoryWiseToPdf() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Category_Wise_Consumption_Report.pdf");

        try (OutputStream out = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Category Wise Issue Details by Department", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            com.itextpdf.text.Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

            for (Map.Entry<String, Map<String, List<DepartmentCategoryWiseItems>>> deptEntry : departmentCategoryMap.entrySet()) {
                String departmentName = deptEntry.getKey();
                document.add(new Paragraph("Department: " + departmentName, boldFont));

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3, 2, 2, 2, 2});

                String[] headers = {"Category", "Rate", "Quantity", "Cost Value", "Value"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    table.addCell(cell);
                }

                for (Map.Entry<String, List<DepartmentCategoryWiseItems>> categoryEntry : deptEntry.getValue().entrySet()) {
                    String categoryName = categoryEntry.getKey();
                    List<DepartmentCategoryWiseItems> items = categoryEntry.getValue();

                    PdfPCell categoryCell = new PdfPCell(new Phrase(categoryName, boldFont));
                    categoryCell.setColspan(5);
                    categoryCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    table.addCell(categoryCell);

                    for (DepartmentCategoryWiseItems item : items) {
                        table.addCell(new PdfPCell(new Phrase(item.getItem().getName(), normalFont)));
                        table.addCell(new PdfPCell(new Phrase(decimalFormat.format(item.getPurchaseRate()), normalFont)));
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(item.getQty()), normalFont)));
                        table.addCell(new PdfPCell(new Phrase(decimalFormat.format(item.getCostRate() != null ? item.getCostRate() * item.getQty() : 0.0), normalFont)));
                        table.addCell(new PdfPCell(new Phrase(decimalFormat.format(item.getNetTotal()), normalFont)));
                    }

                    table.addCell(new PdfPCell(new Phrase("", normalFont)));
                    table.addCell(new PdfPCell(new Phrase("", normalFont)));
                    table.addCell(new PdfPCell(new Phrase("Category Total:", boldFont)));
                    table.addCell(new PdfPCell(new Phrase(getCategoryCostTotalForConsumptionReport(departmentName, categoryName), boldFont)));
                    table.addCell(new PdfPCell(new Phrase(getCategoryTotalForConsumptionReport(departmentName, categoryName), boldFont)));
                }

                table.addCell(new PdfPCell(new Phrase("", normalFont)));
                table.addCell(new PdfPCell(new Phrase("", normalFont)));
                table.addCell(new PdfPCell(new Phrase("Department Total:", boldFont)));
                table.addCell(new PdfPCell(new Phrase(getDepartmentCostTotalForConsumptionReport(departmentName), boldFont)));
                table.addCell(new PdfPCell(new Phrase(getDepartmentTotalForConsumptionReport(departmentName), boldFont)));

                document.add(table);
            }

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{7, 2, 2});
            table.addCell(new PdfPCell(new Phrase("Total", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f", totalCostValue), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f", totalSaleValue), normalFont)));

            document.add(table);

            document.close();
            context.responseComplete();
        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void exportConsumptionReportSummaryToExcel() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Consumption_Report.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Consumption Report");
            int rowIndex = 0;

            XSSFCellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            XSSFCellStyle amountStyle = workbook.createCellStyle();
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            Row headerRow = sheet.createRow(rowIndex++);
            Cell headerCell0 = headerRow.createCell(0);
            headerCell0.setCellValue("Department");
            headerCell0.setCellStyle(headerStyle);

            Cell headerCell1 = headerRow.createCell(1);
            headerCell1.setCellValue("Consumption Department");
            headerCell1.setCellStyle(headerStyle);

            Cell headerCell2 = headerRow.createCell(2);
            headerCell2.setCellValue("Cost Total");
            headerCell2.setCellStyle(headerStyle);

            Cell headerCell3 = headerRow.createCell(3);
            headerCell3.setCellValue("Net Total");
            headerCell3.setCellStyle(headerStyle);

            for (Map.Entry<String, Map<String, Double[]>> departmentEntry : getDepartmentTotals().entrySet()) {
                String departmentName = departmentEntry.getKey();
                Map<String, Double[]> consumptionMap = departmentEntry.getValue();

                Row departmentRow = sheet.createRow(rowIndex++);
                Cell departmentCell = departmentRow.createCell(0);
                departmentCell.setCellValue(departmentName);
                departmentCell.setCellStyle(headerStyle);

                for (Map.Entry<String, Double[]> consumptionEntry : consumptionMap.entrySet()) {
                    String consumptionDepartment = consumptionEntry.getKey();
                    Double netTotal = consumptionEntry.getValue()[0];
                    Double costTotal = consumptionEntry.getValue()[1];

                    Row dataRow = sheet.createRow(rowIndex++);
                    dataRow.createCell(0).setCellValue("");
                    dataRow.createCell(1).setCellValue(consumptionDepartment);
                    Cell costCell = dataRow.createCell(2);
                    costCell.setCellValue(costTotal);
                    costCell.setCellStyle(amountStyle);
                    Cell totalCell = dataRow.createCell(3);
                    totalCell.setCellValue(netTotal);
                    totalCell.setCellStyle(amountStyle);
                }

                Row totalRow = sheet.createRow(rowIndex++);
                totalRow.createCell(1).setCellValue("Total:");
                Cell totalCostCell = totalRow.createCell(2);
                totalCostCell.setCellValue(getPharmacyTotals().get(departmentName)[1]);
                totalCostCell.setCellStyle(amountStyle);
                Cell totalAmountCell = totalRow.createCell(3);
                totalAmountCell.setCellValue(getPharmacyTotals().get(departmentName)[0]);
                totalAmountCell.setCellStyle(amountStyle);
            }

            Row grandTotalRow = sheet.createRow(rowIndex++);
            grandTotalRow.createCell(1).setCellValue("Grand Total:");
            Cell grandTotalCostCell = grandTotalRow.createCell(2);
            grandTotalCostCell.setCellValue(getTotalCostValue());
            grandTotalCostCell.setCellStyle(amountStyle);
            Cell grandTotalCell = grandTotalRow.createCell(3);
            grandTotalCell.setCellValue(getTotalSaleValue());
            grandTotalCell.setCellStyle(amountStyle);

            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            context.responseComplete();

        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void exportConsumptionReportSummaryToPdf() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Consumption_Report.pdf");

        try (OutputStream out = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Consumption Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            com.itextpdf.text.Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 3.5f, 2.5f, 2.5f});

            String[] headers = {"Department", "Consumption Department", "Cost Total", "Net Total"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

            for (Map.Entry<String, Map<String, Double[]>> departmentEntry : getDepartmentTotals().entrySet()) {
                String departmentName = departmentEntry.getKey();
                Map<String, Double[]> consumptionMap = departmentEntry.getValue();

                PdfPCell departmentCell = new PdfPCell(new Phrase(departmentName, boldFont));
                departmentCell.setColspan(4);
                departmentCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(departmentCell);

                for (Map.Entry<String, Double[]> consumptionEntry : consumptionMap.entrySet()) {
                    String consumptionDepartment = consumptionEntry.getKey();
                    Double netTotal = consumptionEntry.getValue()[0];
                    Double costTotal = consumptionEntry.getValue()[1];

                    table.addCell(new PdfPCell(new Phrase("", normalFont)));
                    table.addCell(new PdfPCell(new Phrase(consumptionDepartment, normalFont)));
                    table.addCell(new PdfPCell(new Phrase(decimalFormat.format(costTotal), normalFont)));
                    table.addCell(new PdfPCell(new Phrase(decimalFormat.format(netTotal), normalFont)));
                }

                table.addCell(new PdfPCell(new Phrase("", normalFont)));
                table.addCell(new PdfPCell(new Phrase("Total:", boldFont)));
                table.addCell(new PdfPCell(new Phrase(decimalFormat.format(getPharmacyTotals().get(departmentName)[1]), boldFont)));
                table.addCell(new PdfPCell(new Phrase(decimalFormat.format(getPharmacyTotals().get(departmentName)[0]), boldFont)));
            }

            table.addCell(new PdfPCell(new Phrase("", normalFont)));
            table.addCell(new PdfPCell(new Phrase("Grand Total:", boldFont)));
            table.addCell(new PdfPCell(new Phrase(decimalFormat.format(getTotalCostValue()), boldFont)));
            table.addCell(new PdfPCell(new Phrase(decimalFormat.format(getTotalSaleValue()), boldFont)));

            document.add(table);
            document.close();
            context.responseComplete();
        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public List<String1Value1> calculateTotals(List<Bill> billList) {
        data = new ArrayList<>();
        totalPurchase = 0.0;
        totalSaleValue = 0.0;
        totalCostValue = 0.0;
        totalCreditPurchaseValue = 0.0;
        totalCreditSaleValue = 0.0;
        totalCashPurchaseValue = 0.0;
        totalCashSaleValue = 0.0;
        totalCashCostValue = 0.0;
        totalCreditCostValue = 0.0;

        double totalCardPurchaseValue = 0.0, totalCardSaleValue = 0.0, totalCardCostValue = 0.0;
        double totalChequePurchaseValue = 0.0, totalChequeSaleValue = 0.0, totalChequeCostValue = 0.0;
        double totalSlipPurchaseValue = 0.0, totalSlipSaleValue = 0.0, totalSlipCostValue = 0.0;
        double totalEwalletPurchaseValue = 0.0, totalEwalletSaleValue = 0.0, totalEwalletCostValue = 0.0;
        double totalNonePurchaseValue = 0.0, totalNoneSaleValue = 0.0, totalNoneCostValue = 0.0;
        double totalOtherPurchaseValue = 0.0, totalOtherSaleValue = 0.0, totalOtherCostValue = 0.0;

        for (Bill bill : billList) {
            boolean isCancelled = bill.getBillTypeAtomic().equals(BillTypeAtomic.PHARMACY_GRN_CANCELLED)
                    || bill.getBillTypeAtomic().equals(BillTypeAtomic.PHARMACY_DIRECT_PURCHASE_CANCELLED)
                    || bill.getBillTypeAtomic().equals(BillTypeAtomic.PHARMACY_GRN_RETURN)
                    || bill.getBillTypeAtomic().equals(BillTypeAtomic.PHARMACY_DIRECT_PURCHASE_REFUND);

            double netTotal = 0.0;
            double saleValue = 0.0;
            double costValue = 0.0;

            if (isCancelled) {
                if (bill.getBillFinanceDetails() != null && bill.getBillFinanceDetails().getLineNetTotal() != null) {
                    double lineNetTotalValue = bill.getBillFinanceDetails().getLineNetTotal().doubleValue();
                    netTotal = lineNetTotalValue > 0.0 ? -lineNetTotalValue : lineNetTotalValue;
                }

                if (bill.getBillFinanceDetails() != null && bill.getBillFinanceDetails().getTotalRetailSaleValue() != null) {
                    double retailSaleValue = bill.getBillFinanceDetails().getTotalRetailSaleValue().doubleValue();
                    saleValue = retailSaleValue > 0.0 ? -retailSaleValue : retailSaleValue;
                }

                if (bill.getBillFinanceDetails() != null && bill.getBillFinanceDetails().getTotalCostValue() != null) {
                    double totalCostValue = bill.getBillFinanceDetails().getTotalCostValue().doubleValue();
                    costValue = totalCostValue > 0.0 ? -totalCostValue : totalCostValue;
                }
            } else {
                netTotal = bill.getNetTotal();
                saleValue = bill.getSaleValue();
                costValue = bill.getBillFinanceDetails() != null ? (bill.getBillFinanceDetails().getTotalCostValue() != null ?
                        bill.getBillFinanceDetails().getTotalCostValue().doubleValue():0.0) : 0.0;
            }

            if (bill.getPaymentMethod() == null || (bill.getReferenceBill() != null && bill.getReferenceBill().getPaymentMethod() == null)) {
                Logger.getLogger(PharmacyController.class.getName()).log(Level.WARNING, "Bill {0} has no payment method", bill.getId());
                continue;
            }

            switch (bill.getPaymentMethod()) {
                case Credit:
                    totalCreditPurchaseValue += netTotal;
                    totalCreditSaleValue += saleValue;
                    totalCreditCostValue += costValue;
                    break;
                case Cash:
                    totalCashPurchaseValue += netTotal;
                    totalCashSaleValue += saleValue;
                    totalCashCostValue += costValue;
                    break;
                case Card:
                    totalCardPurchaseValue += netTotal;
                    totalCardSaleValue += saleValue;
                    totalCardCostValue += costValue;
                    break;
                case Cheque:
                    totalChequePurchaseValue += netTotal;
                    totalChequeSaleValue += saleValue;
                    totalChequeCostValue += costValue;
                    break;
                case Slip:
                    totalSlipPurchaseValue += netTotal;
                    totalSlipSaleValue += saleValue;
                    totalSlipCostValue += costValue;
                    break;
                case ewallet:
                    totalEwalletPurchaseValue += netTotal;
                    totalEwalletSaleValue += saleValue;
                    totalEwalletCostValue += costValue;
                    break;
                case None:
                    totalNonePurchaseValue += netTotal;
                    totalNoneSaleValue += saleValue;
                    totalNoneCostValue += costValue;
                    break;
                default:
                    totalOtherPurchaseValue += netTotal;
                    totalOtherSaleValue += saleValue;
                    totalOtherCostValue += costValue;
                    break;
            }

            totalPurchase += netTotal;
            totalSaleValue += saleValue;
            totalCostValue += costValue;
        }

        addIfNotZero(data, "Final Cash Total", totalCashPurchaseValue, totalCashSaleValue, totalCashCostValue);
        addIfNotZero(data, "Final Credit Total", totalCreditPurchaseValue, totalCreditSaleValue, totalCreditCostValue);
        addIfNotZero(data, "Final Card Total", totalCardPurchaseValue, totalCardSaleValue, totalCardCostValue);
        addIfNotZero(data, "Final Cheque Total", totalChequePurchaseValue, totalChequeSaleValue, totalChequeCostValue);
        addIfNotZero(data, "Final Slip Total", totalSlipPurchaseValue, totalSlipSaleValue, totalSlipCostValue);
        addIfNotZero(data, "Final eWallet Total", totalEwalletPurchaseValue, totalEwalletSaleValue, totalEwalletCostValue);
        addIfNotZero(data, "Final None Total", totalNonePurchaseValue, totalNoneSaleValue, totalNoneCostValue);
        addIfNotZero(data, "Final Other Total", totalOtherPurchaseValue, totalOtherSaleValue, totalOtherCostValue);
        addIfNotZero(data, "Final Net Total", totalPurchase, totalSaleValue, totalCostValue);

        return data;
    }

    private void addIfNotZero(List<String1Value1> data, String label, double value, double value2, double value3) {
        if (value != 0 || value2 != 0 || value3 != 0) {
            String1Value1 entry = new String1Value1();
            entry.setString(label);
            entry.setValue(value);
            entry.setValue2(value2);
            entry.setValue3(value3);
            data.add(entry);
        }
    }

    @Deprecated
    public List<String1Value1> calculateTotalsOld(List<Bill> billList) {
        data = new ArrayList<>();

        totalSaleValue = 0.0;
        totalCreditSaleValue = 0.0;
        totalCashSaleValue = 0.0;

        String1Value1 credit = new String1Value1();
        String1Value1 cash = new String1Value1();
        String1Value1 cashAndCredit = new String1Value1();

        cash.setString("Final Cash Total");
        credit.setString("Final Credit Total");
        cashAndCredit.setString("Final Cash and Credit Total");

        for (Bill bill : billList) {
            if (bill.getPaymentMethod() == PaymentMethod.Credit) {
                totalCreditPurchaseValue += bill.getNetTotal();
                totalCreditSaleValue += bill.getSaleValue();
            } else if (bill.getPaymentMethod() == PaymentMethod.Cash) {
                totalCashPurchaseValue += bill.getNetTotal();
                totalCashSaleValue += bill.getSaleValue();
            }
            totalPurchase += bill.getNetTotal();
            totalSaleValue += bill.getSaleValue();
        }
        credit.setValue(totalCreditPurchaseValue);
        credit.setValue2(totalCreditSaleValue);

        cash.setValue(totalCashPurchaseValue);
        cash.setValue2(totalCashSaleValue);

        cashAndCredit.setValue(totalPurchase);
        cashAndCredit.setValue2(totalSaleValue);

        data.add(cash);
        data.add(credit);
        data.add(cashAndCredit);

        return data;
    }

    public List<String1Value1> calculateTotalsForBillItems(List<BillItem> billItemList) {
        data = new ArrayList<>();

        totalSaleValue = 0.0;
        totalCreditSaleValue = 0.0;
        totalCashSaleValue = 0.0;

        String1Value1 credit = new String1Value1();
        String1Value1 cash = new String1Value1();
        String1Value1 cashAndCredit = new String1Value1();

        cash.setString("Final Cash Total");
        credit.setString("Final Credit Total");
        cashAndCredit.setString("Final Cash and Credit Total");

        // Use a set to keep track of processed bills
        Set<Long> processedBillIds = new HashSet<>();

        for (BillItem billItem : billItemList) {
            Bill bill = billItem.getBill(); // Access the associated Bill object

            // Process each bill only once
            if (!processedBillIds.contains(bill.getId())) {
                processedBillIds.add(bill.getId()); // Mark the bill as processed

                if (bill.getPaymentMethod() == PaymentMethod.Credit) {
                    totalCreditPurchaseValue += bill.getNetTotal();
                    totalCreditSaleValue += bill.getSaleValue();
                } else if (bill.getPaymentMethod() == PaymentMethod.Cash) {
                    totalCashPurchaseValue += bill.getNetTotal();
                    totalCashSaleValue += bill.getSaleValue();
                }
                totalPurchase += bill.getNetTotal();
                totalSaleValue += bill.getSaleValue();
            }
        }

        credit.setValue(totalCreditPurchaseValue);
        credit.setValue2(totalCreditSaleValue);

        cash.setValue(totalCashPurchaseValue);
        cash.setValue2(totalCashSaleValue);

        cashAndCredit.setValue(totalPurchase);
        cashAndCredit.setValue2(totalSaleValue);

        data.add(cash);
        data.add(credit);
        data.add(cashAndCredit);

        return data;
    }

    public boolean isInvalidFilter() {
        if (item != null && (reportType.equals("summeryReport") || reportType.equals("byBill"))) {
            return true;
        }
        return false;
    }

    public void createStockTransferReport() {
        reportTimerController.trackReportExecution(() -> {
            resetFields();
            BillType bt;

            if (isInvalidFilter()) {
                JsfUtil.addErrorMessage("Item filter cannot be applied for 'Summary' or 'Bill' report types. Please remove the item filter or choose a 'Detail' Report.");
                return;
            }

            if ("issue".equals(transferType)) {
                bt = BillType.PharmacyTransferIssue;
            } else {
                bt = BillType.PharmacyTransferReceive;
            }

            if ("summeryReport".equals(reportType)) {
                generateReportAsSummary(bt);

            } else if ("detailReport".equals(reportType)) {
                generateReportByBillItems(bt);

            } else if ("byBill".equals(reportType)) {
                generateReportByDepartmentWiseBill(bt);

            }
        }, InventoryReports.STOCK_TRANSFER_REPORT, sessionController.getLoggedUser());
    }

    public void exportStockTransferDetailReportToExcel() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Stock_Transfer_Detail_Report.xlsx");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy hh:mm:ss a");

        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Stock Transfer Detail Report");
            int rowIndex = 0;

            // Create header row
            Row headerRow = sheet.createRow(rowIndex++);
            headerRow.createCell(0).setCellValue("Department");
            headerRow.createCell(1).setCellValue("Bill No");
            headerRow.createCell(2).setCellValue("Approval Date");
            headerRow.createCell(3).setCellValue("Ref Bill No");
            headerRow.createCell(4).setCellValue("Ref Bill Approved Date and Time");
            headerRow.createCell(5).setCellValue("Item Description");
            headerRow.createCell(6).setCellValue("Item Code");
            headerRow.createCell(7).setCellValue("Quantity");
            headerRow.createCell(8).setCellValue("Unit");
            headerRow.createCell(9).setCellValue("Purchase Rate");
            headerRow.createCell(10).setCellValue("Purchase Value");

            boolean costingEnabled = configOptionApplicationController.getBooleanValueByKey("Manage Costing", true);
            if (costingEnabled) {
                headerRow.createCell(11).setCellValue("Cost Rate");
                headerRow.createCell(12).setCellValue("Cost Value");
                headerRow.createCell(13).setCellValue("Issue Approved User");
            } else {
                headerRow.createCell(11).setCellValue("Issue Approved User");
            }

            for (Map.Entry<String, List<PharmacyRow>> deptEntry : departmentWiseRows.entrySet()) {
                String departmentName = deptEntry.getKey();
                List<PharmacyRow> billItems = deptEntry.getValue();

                for (PharmacyRow i : billItems) {
                    Row dataRow = sheet.createRow(rowIndex++);
                    int colIndex = 0;

                    dataRow.createCell(colIndex++).setCellValue(departmentName);
                    dataRow.createCell(colIndex++).setCellValue(i.getBillItem().getBill().getDeptId() != null ? i.getBillItem().getBill().getDeptId() : "-");
                    dataRow.createCell(colIndex++).setCellValue(i.getBillItem().getBill().getCreatedAt() != null ? sdf.format(i.getBillItem().getBill().getCreatedAt()) : "-");
                    dataRow.createCell(colIndex++).setCellValue(i.getBillItem().getBill().getBackwardReferenceBill() != null ? i.getBillItem().getBill().getBackwardReferenceBill().getDeptId() : "-");
                    dataRow.createCell(colIndex++).setCellValue(i.getBillItem().getBill().getBackwardReferenceBill() != null && i.getBillItem().getBill().getBackwardReferenceBill().getCreatedAt() != null ? sdf.format(i.getBillItem().getBill().getBackwardReferenceBill().getCreatedAt()) : "-");
                    dataRow.createCell(colIndex++).setCellValue(i.getBillItem().getItem().getName() != null ? i.getBillItem().getItem().getName() : "-");
                    dataRow.createCell(colIndex++).setCellValue(i.getBillItem().getItem().getCode() != null ? i.getBillItem().getItem().getCode() : "-");
                    dataRow.createCell(colIndex++).setCellValue(i.getBillItem().getPharmaceuticalBillItem().getQty());
                    dataRow.createCell(colIndex++).setCellValue(i.getBillItem().getItem().getVmp() != null && i.getBillItem().getItem().getVmp().getStrengthUnit() != null ? i.getBillItem().getItem().getVmp().getStrengthUnit().getName() : "-");
                    dataRow.createCell(colIndex++).setCellValue(i.getBillItem().getPharmaceuticalBillItem().getPurchaseRate());
                    dataRow.createCell(colIndex++).setCellValue(i.getBillItem().getPharmaceuticalBillItem().getQty() * i.getBillItem().getPharmaceuticalBillItem().getPurchaseRate());

                    if (costingEnabled) {
                        dataRow.createCell(colIndex++).setCellValue(i.getItemBatch() != null && i.getItemBatch().getCostRate() != null ? i.getItemBatch().getCostRate() : 0);
                        dataRow.createCell(colIndex++).setCellValue(i.getItemBatch() != null && i.getItemBatch().getCostRate() != null ? (i.getBillItem().getPharmaceuticalBillItem().getQty() * i.getItemBatch().getCostRate()) : 0);
                    }

                    dataRow.createCell(colIndex++).setCellValue(i.getBillItem().getBill().getCreater() != null && i.getBillItem().getBill().getCreater().getWebUserPerson() != null ? i.getBillItem().getBill().getCreater().getWebUserPerson().getName() : "-");
                }
            }

            workbook.write(out);
            context.responseComplete();
        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, "Error exporting Stock Transfer Detail Report to Excel", e);
        }
    }

    public void exportStockTransferDetailReportToPDF() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Stock_Transfer_Detail_Report.pdf");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy hh:mm:ss a");

        try (OutputStream out = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Stock Transfer Detail Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph("Date: " + sdf.format(new Date()), FontFactory.getFont(FontFactory.HELVETICA, 12)));
            document.add(new Paragraph(" "));

            boolean costingEnabled = configOptionApplicationController.getBooleanValueByKey("Manage Costing", true);
            int columnCount = costingEnabled ? 14 : 12;

            PdfPTable table = new PdfPTable(columnCount);
            table.setWidthPercentage(100);

            float[] columnWidths;
            String[] headers;

            if (costingEnabled) {
                columnWidths = new float[]{4f, 3f, 4f, 3f, 4f, 4f, 3f, 3f, 3f, 3f, 3f, 3f, 3f, 4f};
                headers = new String[]{"Department", "Bill No", "Approval Date", "Ref Bill No", "Ref Bill Approved Date", "Item Description", "Item Code", "Quantity", "Unit", "Purchase Rate", "Purchase Value", "Cost Rate", "Cost Value", "Issue Approved User"};
            } else {
                columnWidths = new float[]{4f, 3f, 4f, 3f, 4f, 4f, 3f, 3f, 3f, 3f, 3f, 4f};
                headers = new String[]{"Department", "Bill No", "Approval Date", "Ref Bill No", "Ref Bill Approved Date", "Item Description", "Item Code", "Quantity", "Unit", "Purchase Rate", "Purchase Value", "Issue Approved User"};
            }

            table.setWidths(columnWidths);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            for (Map.Entry<String, List<PharmacyRow>> deptEntry : departmentWiseRows.entrySet()) {
                String departmentName = deptEntry.getKey();
                List<PharmacyRow> billItems = deptEntry.getValue();

                for (PharmacyRow i : billItems) {
                    table.addCell(new PdfPCell(new Phrase(departmentName, FontFactory.getFont(FontFactory.HELVETICA, 8))));
                    table.addCell(new PdfPCell(new Phrase(i.getBillItem().getBill().getDeptId() != null ? i.getBillItem().getBill().getDeptId() : "-", FontFactory.getFont(FontFactory.HELVETICA, 8))));
                    table.addCell(new PdfPCell(new Phrase(i.getBillItem().getBill().getCreatedAt() != null ? sdf.format(i.getBillItem().getBill().getCreatedAt()) : "-", FontFactory.getFont(FontFactory.HELVETICA, 8))));
                    table.addCell(new PdfPCell(new Phrase(i.getBillItem().getBill().getBackwardReferenceBill() != null ? i.getBillItem().getBill().getBackwardReferenceBill().getDeptId() : "-", FontFactory.getFont(FontFactory.HELVETICA, 8))));
                    table.addCell(new PdfPCell(new Phrase(i.getBillItem().getBill().getBackwardReferenceBill() != null && i.getBillItem().getBill().getBackwardReferenceBill().getCreatedAt() != null ? sdf.format(i.getBillItem().getBill().getBackwardReferenceBill().getCreatedAt()) : "-", FontFactory.getFont(FontFactory.HELVETICA, 8))));
                    table.addCell(new PdfPCell(new Phrase(i.getBillItem().getItem().getName() != null ? i.getBillItem().getItem().getName() : "-", FontFactory.getFont(FontFactory.HELVETICA, 8))));
                    table.addCell(new PdfPCell(new Phrase(i.getBillItem().getItem().getCode() != null ? i.getBillItem().getItem().getCode() : "-", FontFactory.getFont(FontFactory.HELVETICA, 8))));
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(i.getBillItem().getPharmaceuticalBillItem().getQty()), FontFactory.getFont(FontFactory.HELVETICA, 8))));
                    table.addCell(new PdfPCell(new Phrase(i.getBillItem().getItem().getVmp() != null && i.getBillItem().getItem().getVmp().getStrengthUnit() != null ? i.getBillItem().getItem().getVmp().getStrengthUnit().getName() : "-", FontFactory.getFont(FontFactory.HELVETICA, 8))));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", i.getBillItem().getPharmaceuticalBillItem().getPurchaseRate()), FontFactory.getFont(FontFactory.HELVETICA, 8))));
                    table.addCell(new PdfPCell(new Phrase(String.format("%.2f", (i.getBillItem().getPharmaceuticalBillItem().getQty() * i.getBillItem().getPharmaceuticalBillItem().getPurchaseRate())), FontFactory.getFont(FontFactory.HELVETICA, 8))));

                    if (costingEnabled) {
                        table.addCell(new PdfPCell(new Phrase(i.getItemBatch() != null && i.getItemBatch().getCostRate() != null ? String.format("%.2f", i.getItemBatch().getCostRate()) : "0.00", FontFactory.getFont(FontFactory.HELVETICA, 8))));
                        table.addCell(new PdfPCell(new Phrase( i.getItemBatch() != null && i.getItemBatch().getCostRate() != null ? String.format("%.2f", (i.getBillItem().getPharmaceuticalBillItem().getQty() * i.getItemBatch().getCostRate())) : "0.00", FontFactory.getFont(FontFactory.HELVETICA, 8))));
                    }

                    table.addCell(new PdfPCell(new Phrase(i.getBillItem().getBill().getCreater() != null && i.getBillItem().getBill().getCreater().getWebUserPerson() != null ? i.getBillItem().getBill().getCreater().getWebUserPerson().getName() : "-", FontFactory.getFont(FontFactory.HELVETICA, 8))));
                }
            }

            document.add(table);
            document.close();
            context.responseComplete();
        } catch (Exception e) {
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, "Error exporting Stock Transfer Detail Report to PDF", e);
        }
    }

    public void generateReportByDepartmentWiseBill(BillType billType) {
        Map<String, Object> parameters = new HashMap<>();
        StringBuilder sql = new StringBuilder(
                "SELECT b.toDepartment, "
                        + "b.department, "
                        + "b.deptId, "
                        + "b.createdAt, "
                        + "b.backwardReferenceBill, "
                        + "b.backwardReferenceBill.deptId, "
                        + "SUM(b.netTotal), "
                        + "b "
                        + "FROM Bill b "
                        + "WHERE b.retired = false "
                        + "AND b.createdAt BETWEEN :fromDate AND :toDate "
                        + "AND b.billType = :billType "
        );

        parameters.put("billType", billType);
        parameters.put("fromDate", fromDate);
        parameters.put("toDate", toDate);

        additionalCommonFilltersForBillEntity(sql, parameters);

        sql.append(" GROUP BY b.toDepartment, b.department, b.deptId, b.createdAt, b.backwardReferenceBill, b.backwardReferenceBill.deptId, b");
        sql.append(" ORDER BY b.id");

        Map<Department, List<DepartmentWiseBill>> departmentWiseBillMap = new HashMap<>();

        try {
            List<Object[]> results = (List<Object[]>) getBillFacade()
                    .findLightsByJpql(sql.toString(), parameters, TemporalType.TIMESTAMP);

            departmentWiseBillList = new ArrayList<>();
            totalPurchase = 0;
            totalCostValue = 0;

            for (Object[] result : results) {
                Department toDepartment = (Department) result[0];
                DepartmentWiseBill departmentWiseBill = new DepartmentWiseBill(
                        (Department) result[1],
                        (String) result[2],
                        (Date) result[3],
                        (Bill) result[4],
                        (String) result[5],
                        (Double) result[6],
                        (Bill) result[7]
                );

                departmentWiseBillMap
                        .computeIfAbsent(toDepartment, k -> new ArrayList<>())
                        .add(departmentWiseBill);

                departmentWiseBillList.add(departmentWiseBill);
                totalPurchase += departmentWiseBill.getBill().getNetTotal();

                double costValue = Math.abs(departmentWiseBill.getBill().getBillFinanceDetails() != null &&
                        departmentWiseBill.getBill().getBillFinanceDetails().getTotalCostValue() != null ?
                        departmentWiseBill.getBill().getBillFinanceDetails().getTotalCostValue().doubleValue() : 0.0);

                totalCostValue += departmentWiseBill.getBill().getNetTotal() < 0 ? -costValue : costValue;
            }

            departmentWiseBillList = departmentWiseBillMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(Department::getName)))
                    .flatMap(entry -> entry.getValue().stream())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Error occurred while generating the consumption report.");
        }
    }

    private void addFilter(StringBuilder sql, Map<String, Object> parameters, String sqlField, String paramKey, Object value) {
        if (value != null) {
            sql.append(" AND ").append(sqlField).append(" = :").append(paramKey).append(" ");
            parameters.put(paramKey, value);
        }
    }

    private void additionalCommonFilltersForBillEntity(StringBuilder sql, Map<String, Object> parameters) {
        addFilter(sql, parameters, "b.fromInstitution", "fIns", fromInstitution);
        addFilter(sql, parameters, "b.fromDepartment.site", "fSite", fromSite);
        addFilter(sql, parameters, "b.fromDepartment", "fDept", fromDepartment);
        addFilter(sql, parameters, "b.toInstitution", "tIns", toInstitution);
        addFilter(sql, parameters, "b.toDepartment.site", "tSite", toSite);
        addFilter(sql, parameters, "b.toDepartment", "tDept", toDepartment);
    }

    public void generateReportByBillItems(BillType billType) {
        try {
            departmentWiseRows = new HashMap<>();
            pharmacyRows = new ArrayList<>();
            billItems = new ArrayList<>();
            Map<String, Object> parameters = new HashMap<>();
            StringBuilder sql = new StringBuilder();

            sql.append("SELECT bi FROM BillItem bi WHERE bi.retired = false ")
                    .append("AND bi.bill.retired = false ")
                    .append("AND bi.bill.createdAt BETWEEN :fromDate AND :toDate ")
                    .append("AND bi.bill.billType = :billType ");

            parameters.put("fromDate", fromDate);
            parameters.put("toDate", toDate);
            parameters.put("billType", billType);

            addFilter(sql, parameters, "bi.bill.fromInstitution", "institution", fromInstitution);
            addFilter(sql, parameters, "bi.bill.fromDepartment.site", "fSite", fromSite);
            addFilter(sql, parameters, "bi.bill.fromDepartment", "fDept", fromDepartment);
            addFilter(sql, parameters, "bi.bill.toInstitution", "tIns", toInstitution);
            addFilter(sql, parameters, "bi.bill.toDepartment.site", "tSite", toSite);
            addFilter(sql, parameters, "bi.bill.toDepartment", "tDept", toDepartment);
            addFilter(sql, parameters, "bi.item", "item", item);

            sql.append(" ORDER BY bi.id DESC");

            billItems = getBillItemFacade().findByJpql(sql.toString(), parameters, TemporalType.TIMESTAMP);

            if (billItems.isEmpty()) {
                pharmacyRows = new ArrayList<>();
                return;
            }

            List<Item> items = billItems.stream()
                    .map(BillItem::getItem)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (!items.isEmpty()) {
                List<ItemBatch> allBatches = getItemBatchesByItems(items);
                pharmacyRows = createPharmacyRowsByBillItemsAndItemBatch(billItems, allBatches);

                totalPurchase = pharmacyRows.stream()
                        .filter(r -> r.getQuantity() != null && r.getPurchaseValue() != null)
                        .mapToDouble(r -> r.getQuantity() * r.getPurchaseValue())
                        .sum();
            }

            for (PharmacyRow row : pharmacyRows) {
                String departmentName = row.getBillItem().getBill().getToDepartment() != null
                        ? row.getBillItem().getBill().getToDepartment().getName()
                        : "Other";

                departmentWiseRows.computeIfAbsent(departmentName, k -> new ArrayList<>()).add(row);
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to generate report. Please try again."));
            Logger.getLogger(PharmacyController.class.getName()).log(Level.SEVERE, "Error generating report by bill items", e);
        }
    }

    private List<ItemBatch> getItemBatchesByItems(List<Item> items) {
        Map<String, Object> batchParams = new HashMap<>();
        batchParams.put("items", items);

        String batchQuery = "SELECT ib FROM ItemBatch ib WHERE ib.item IN :items";
        return itemBatchFacade.findByJpql(batchQuery, batchParams);
    }

    private List<PharmacyRow> createPharmacyRowsByBillItemsAndItemBatch(List<BillItem> billItems, List<ItemBatch> itemBatches) {
        Map<Long, ItemBatch> latestBatchMap = itemBatches.stream()
                .filter(ib -> ib.getItem() != null)
                .collect(Collectors.toMap(ib -> ib.getItem().getId(), Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(ItemBatch::getId))));

        return billItems.stream()
                .map(bi -> {
                    ItemBatch latestBatch = latestBatchMap.get(
                            bi.getItem() != null ? bi.getItem().getId() : null
                    );
                    return new PharmacyRow(bi, latestBatch);
                })
                .collect(Collectors.toList());
    }

    public void generateReportAsSummary(BillType billType) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.department.name, SUM(b.netTotal), SUM(b.billFinanceDetails.totalCostValue) ");
        if ("issue".equals(transferType)) {
            sql.append(", SUM(CASE WHEN b.forwardReferenceBill IS NULL AND SIZE(b.forwardReferenceBills) = 0 THEN b.netTotal ELSE 0 END) ");
        }
        sql.append("FROM Bill b ")
                .append("WHERE b.retired = false ")
                .append("AND b.billType = :billType ")
                .append("AND b.createdAt BETWEEN :fromDate AND :toDate ");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("billType", billType);
        parameters.put("fromDate", getFromDate());
        parameters.put("toDate", getToDate());

        additionalCommonFilltersForBillEntity(sql, parameters);

        sql.append(" GROUP BY b.department.name ORDER BY SUM(b.netTotal) DESC");

        try {
            List<Object[]> results = getBillFacade().findObjectsArrayByJpql(sql.toString(), parameters, TemporalType.TIMESTAMP);
            Map<String, PharmacySummery> departmentMap = new HashMap<>();
            departmentSummaries = new ArrayList<>();
            double costTotal = 0.0;
            double grandTotal = 0.0;
            double goodInTransistTotal = 0.0;

            for (Object[] result : results) {
                String departmentName = (String) result[0];
                double netTotal = (double) result[1];
                double costValue = 0.0;
                if (result[2] != null) {
                    costValue = netTotal < 0
                            ? -Math.abs(((BigDecimal) result[2]).doubleValue())
                            : Math.abs(((BigDecimal) result[2]).doubleValue());
                }
                grandTotal += netTotal;
                costTotal += costValue;
                if ("issue".equals(transferType)) {
                    double goodInTransist = (double) result[3];
                    goodInTransistTotal += goodInTransist;
                    departmentSummaries.add(new PharmacySummery(departmentName, netTotal, goodInTransist, costValue));
                } else {
                    PharmacySummery summary = new PharmacySummery(departmentName, netTotal);
                    summary.setTotalCost(costValue);
                    departmentSummaries.add(summary);
                    departmentMap.put(departmentName, summary);
                }
            }

            departmentSummaries.add(new PharmacySummery("Total", grandTotal, goodInTransistTotal, costTotal));

            if ("receive".equals(transferType)) {
                generateReportAsSummaryWithGiT(BillType.PharmacyTransferIssue, departmentMap);
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Something Went Wrong!");
        }
    }

    private List<PharmacySummery> summaries;

    public void generateReportAsSummaryWithGiT(BillType billType, Map<String, PharmacySummery> departmentMap) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.toDepartment.name,")
                .append("SUM(CASE WHEN b.forwardReferenceBill IS NULL AND SIZE(b.forwardReferenceBills) = 0 THEN b.netTotal ELSE 0 END) ")
                .append("FROM Bill b ")
                .append("WHERE b.retired = false ")
                .append("AND b.billType = :billType ")
                .append("AND b.createdAt BETWEEN :fromDate AND :toDate ");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("billType", billType);
        parameters.put("fromDate", getFromDate());
        parameters.put("toDate", getToDate());

        additionalCommonFilltersForBillEntity(sql, parameters);

        sql.append(" GROUP BY b.toDepartment.name ");

        try {
            List<Object[]> results = getBillFacade().findObjectsArrayByJpql(sql.toString(), parameters, TemporalType.TIMESTAMP);

            summaries = new ArrayList<>();

            double goodInTransitTotal = 0.0;
            for (Object[] result : results) {
                String toDepartmentName = (String) result[0];
                double goodInTransit = (double) result[1];
                goodInTransitTotal += goodInTransit;

                if (departmentMap.containsKey(toDepartmentName)) {
                    PharmacySummery existingSummary = departmentMap.get(toDepartmentName);
                    existingSummary.setGoodInTransistAmount(goodInTransit);
                    summaries.add(existingSummary);
                }
            }

            PharmacySummery lastRow = departmentSummaries.get(departmentSummaries.size() - 1);
            if ("Total".equals(lastRow.getDepartmentName())) {
                lastRow.setGoodInTransistAmount(goodInTransitTotal);
            }
            summaries.add(new PharmacySummery("Total", lastRow.getNetTotal(), lastRow.getGoodInTransistAmount()));

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Something Went Wrong!");
        }
    }

    public void createBeforeStockTakingReport() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT s ")
                .append("FROM Stock s ")
                .append("WHERE s.retired = false ");

        Map<String, Object> parameters = new HashMap<>();

        if (transferType != null && qty >= 0.0) {
            switch (transferType) {
                case "equal":
                    sql.append(" AND s.stock = :st ");
                    break;
                case "notEqual":
                    sql.append(" AND s.stock <> :st ");
                    break;
                case "gThan":
                    sql.append(" AND s.stock > :st ");
                    break;
                case "lThan":
                    sql.append(" AND s.stock < :st ");
                    break;
                case "gThanOrEqual":
                    sql.append(" AND s.stock >= :st ");
                    break;
                case "lThanOrEqual":
                    sql.append(" AND s.stock <= :st ");
                    break;
            }
            parameters.put("st", qty);
        }

        addFilter(sql, parameters, "s.department", "dept", dept);
        addFilter(sql, parameters, "s.itemBatch.item.category", "cat", category);

        sql.append(" ORDER BY s.itemBatch.item.name ");

        try {
            stockList = getStockFacade().findByJpql(sql.toString(), parameters);
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Something Went Wrong!");
        }
    }

    public void deleteSelectedPharmaceuticalLight() {
        if (selectedLights == null || selectedLights.isEmpty()) {
            JsfUtil.addErrorMessage("Nothing selected");
            return;
        }
        List<Item> itemsToUpdate = new ArrayList<>();
        for (PharmaceuticalItemLight l : selectedLights) {
            if (l.getId() == null) {
                continue;
            }
            Item i = itemFacade.find(l.getId());
            if (i == null) {
                continue;
            }
            i.setRetired(true);
            i.setRetirer(sessionController.getLoggedUser());
            i.setRetiredAt(new Date());
            itemsToUpdate.add(i);
        }
        itemFacade.batchEdit(itemsToUpdate);
        fillPharmaceuticalLights();
    }

    public void fillPharmaceuticalLights() {
//        String jpql = "select new com.divudi.bean.pharmacy.PharmaceuticalItemLight(p.id, p.name, p.clazz) "
//                + " from PharmaceuticalItem p "
//                + " where p.retired != true "
//                + " order by p.name";
        String jpql = "select new com.divudi.bean.pharmacy.PharmaceuticalItemLight(p.id, p.name, p.clazz) "
                + " from PharmaceuticalItem p ";
//        Map m = new HashMap();
//        m.put("ret", Boolean.TRUE);
//        allLights = (List<PharmaceuticalItemLight>) itemFacade.findLightsByJpql(jpql, m);
//        Map m = new HashMap();
//        m.put("ret", Boolean.TRUE);
//        allLights = (List<PharmaceuticalItemLight>) itemFacade.findLightsByJpql(jpql, m);
        allLights = (List<PharmaceuticalItemLight>) itemFacade.findLightsByJpql(jpql);

    }

    public void makeNull() {
        departmentSale = null;
//        departmentStocks = null;
        pos = null;
        grns = null;
        pendingGrns = null;
        institutionSales = null;
        institutionStocks = null;
        institutionTransferIssue = null;
        directPurchase = null;

    }

    public List<Stock> completeAllStocks(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        double d = 0.0;
        m.put("n", "%" + qry.toUpperCase() + "%");
        sql = "select i from Stock i where i.department=:d and "
                + " ((i.itemBatch.item.name) like :n  or "
                + " (i.itemBatch.item.code) like :n  or  "
                + " (i.itemBatch.item.barcode) like :n ) "
                + " order by i.stock desc";
        items = getStockFacade().findByJpql(sql, m, 30);

        return items;
    }

    public List<Item> completeAllStockItems(String qry) {
        List<Item> items;
        String sql;
        Map m = new HashMap();
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        double d = 0.0;
        m.put("n", "%" + qry.toUpperCase() + "%");
        sql = "select distinct(i.itemBatch.item) from Stock i where i.department=:d and "
                + " ((i.itemBatch.item.name) like :n  or "
                + " (i.itemBatch.item.code) like :n  or  "
                + " (i.itemBatch.item.barcode) like :n ) "
                + " and i.stock>0 ";
        if (getCategory() != null) {
            sql += " and i.itemBatch.item.category=:cat ";
            m.put("cat", getCategory());
        }
        sql += " order by i.itemBatch.item.name ";
        items = getItemFacade().findByJpql(sql, m);
        return items;
    }

    public List<Stock> completeStaffStocks(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        double d = 0.0;
        m.put("s", d);
        m.put("n", "%" + qry.toUpperCase() + "%");
        sql = "select i from Stock i where i.stock >:s and "
                + "((i.staff.code) like :n or "
                + "(i.staff.person.name) like :n or "
                + "(i.itemBatch.item.name) like :n ) "
                + "order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        items = getStockFacade().findByJpql(sql, m, 20);

        return items;
    }

    public List<Department> getInstitutionDepatrments(Institution ins) {
        List<Department> d;
        HashMap hm = new HashMap();
        String sql = "Select d From Department d where d.retired=false and d.institution=:ins";
        hm.put("ins", ins);
        d = getDepartmentFacade().findByJpql(sql, hm);

        return d;
    }

    public void createAllItemTransactionSummery() {
        Date startTime = new Date();

        hasInward = false;
        hasIssue = false;
        hasPurchase = false;
        hasTransferIn = false;
        hasSale = false;
        hasTransferOut = false;
        hasWholesale = false;
        String s;
        s = "select a "
                + " from BillItem bi join bi.item a "
                + " where type(a)=:t "
                + " and bi.bill.billType in :bts "
                + " and bi.bill.createdAt between :fd and :td "
                + " and bi.bill.department =:dep "
                + " group by a "
                + " order by a.name";
        BillType[] abts = new BillType[]{BillType.PharmacySale, BillType.PharmacyTransferReceive, BillType.PharmacyPurchaseBill, BillType.PharmacyGrnBill, BillType.PharmacyBhtPre, BillType.PharmacyTransferIssue, BillType.PharmacyIssue, BillType.PharmacyWholeSale};

        Map p = new HashMap();
        p
                .put("t", Amp.class
                );
        p.put("fd", fromDate);
        p.put("td", toDate);
        p.put("dep", department);
        p.put("bts", Arrays.asList(abts));

        List<Amp> allAmps = ampFacade.findByJpql(s, p);

        Map<Long, ItemTransactionSummeryRow> m = new HashMap();

        for (Amp a : allAmps) {
            ItemTransactionSummeryRow r = new ItemTransactionSummeryRow();
            r.setItem(a);
            m.put(a.getId(), r);
        }

        BillType[] bts = new BillType[]{BillType.PharmacySale};
        BillType[] rbts = new BillType[]{BillType.PharmacyPre};
        List<ItemQuantityAndValues> rs = findPharmacyTrnasactionQuantityAndValues(fromDate,
                toDate, null, department, null, bts, rbts);

        for (ItemQuantityAndValues v : rs) {
            ItemTransactionSummeryRow r = m.get(v.getItem().getId());
            if (r != null) {
                if (Math.abs(v.getQuantity()) > 0.0) {
                    hasSale = true;
                }

                r.setRetailSaleQty(Math.abs(v.getQuantity()));
                r.setRetailSaleVal(Math.abs(v.getValue()));
            }
        }

        bts = new BillType[]{BillType.PharmacyWholeSale};
        rbts = new BillType[]{BillType.PharmacyWholesalePre};
        rs = findPharmacyTrnasactionQuantityAndValues(fromDate,
                toDate, null, department, null, bts, rbts);
        for (ItemQuantityAndValues v : rs) {
            ItemTransactionSummeryRow r = m.get(v.getItem().getId());
            if (r != null) {
                if (Math.abs(v.getQuantity()) > 0.0) {
                    hasWholesale = true;
                }
                r.setWholeSaleQty(Math.abs(v.getQuantity()));
                r.setWholeSaleVal(Math.abs(v.getValue()));
            }
        }

        bts = new BillType[]{BillType.PharmacyIssue};
        rs = findPharmacyTrnasactionQuantityAndValues(fromDate,
                toDate, null, department, null, bts, rbts);
        for (ItemQuantityAndValues v : rs) {
            ItemTransactionSummeryRow r = m.get(v.getItem().getId());
            if (r != null) {
                r.setIssueQty(Math.abs(v.getQuantity()));
                if (Math.abs(v.getQuantity()) > 0.0) {
                    hasIssue = true;
                }
                r.setIssueVal(Math.abs(v.getValue()));
            }
        }

        bts = new BillType[]{BillType.PharmacyTransferIssue};
        rs = findPharmacyTrnasactionQuantityAndValues(fromDate,
                toDate, null, department, null, bts, rbts);
        for (ItemQuantityAndValues v : rs) {
            ItemTransactionSummeryRow r = m.get(v.getItem().getId());
            if (r != null) {
                if (Math.abs(v.getQuantity()) > 0.0) {
                    hasTransferOut = true;
                }
                r.setTransferOutQty(Math.abs(v.getQuantity()));
                r.setTransferOutVal(Math.abs(v.getValue()));
            }
        }

        bts = new BillType[]{BillType.PharmacyBhtPre};
        rs = findPharmacyTrnasactionQuantityAndValues(fromDate,
                toDate, null, department, null, bts, rbts);
        for (ItemQuantityAndValues v : rs) {
            ItemTransactionSummeryRow r = m.get(v.getItem().getId());
            if (r != null) {
                if (Math.abs(v.getQuantity()) > 0.0) {
                    hasInward = true;
                }
                r.setBhtSaleQty(Math.abs(v.getQuantity()));
                r.setBhtSaleVal(Math.abs(v.getValue()));
            }
        }

        bts = new BillType[]{BillType.PharmacyPurchaseBill, BillType.PharmacyGrnBill};
        rs = findPharmacyTrnasactionQuantityAndValues(fromDate,
                toDate, null, department, null, bts, null);
        for (ItemQuantityAndValues v : rs) {
            ItemTransactionSummeryRow r = m.get(v.getItem().getId());
            if (r != null) {
                if (Math.abs(v.getQuantity()) > 0.0) {
                    hasPurchase = true;
                }
                r.setPurchaseQty(Math.abs(v.getQuantity()));
                r.setPurchaseVal(Math.abs(v.getValue()));
            }
        }

        bts = new BillType[]{BillType.PharmacyTransferReceive};
        rs = findPharmacyTrnasactionQuantityAndValues(fromDate,
                toDate, null, department, null, bts, rbts);
        for (ItemQuantityAndValues v : rs) {
            ItemTransactionSummeryRow r = m.get(v.getItem().getId());
            if (r != null) {
                if (Math.abs(v.getQuantity()) > 0.0) {
                    hasTransferIn = true;
                }
                r.setTransferInQty(Math.abs(v.getQuantity()));
                r.setTransferInVal(Math.abs(v.getValue()));
            }
        }

//        //System.out.println("m = " + m);
        itemTransactionSummeryRows = new ArrayList<>(m.values());

        for (ItemTransactionSummeryRow r : itemTransactionSummeryRows) {
            if (r.getBhtSaleQty() == 0.0 && r.getIssueQty() == 0.0 && r.getPurchaseQty() == 0.0 && r.getRetailSaleQty() == 0.0
                    && r.getWholeSaleQty() == 0.0 && r.getTransferOutQty() == 0.0 && r.getTransferInQty() == 0.0) {
                itemTransactionSummeryRows.remove(r);
            }
        }

        Collections.sort(itemTransactionSummeryRows);

    }

    public List<ItemQuantityAndValues> findPharmacyTrnasactionQuantityAndValues(Date fromDate,
                                                                                Date toDate,
                                                                                Institution ins,
                                                                                Department department,
                                                                                Item item,
                                                                                BillType[] billTypes,
                                                                                BillType[] referenceBillTypes) {

//        if (false) {
//            BillItem bi = new BillItem();
//            bi.getNetValue();
//            bi.getPharmaceuticalBillItem().getQty();
//        }
        String sql;
        Map m = new HashMap();
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        sql = "select new com.divudi.core.data.dataStructure.ItemQuantityAndValues(i.item, "
                + "sum(i.pharmaceuticalBillItem.qty), "
                + "sum(i.netValue)) "
                + " from BillItem i "
                + " where i.bill.createdAt between :frm and :to  ";
        if (department != null) {
            m.put("dep", department);
            sql += " and i.bill.department=:dep";
        }
        if (item != null) {
            m.put("item", department);
            sql += " and i.item=:item ";
        }

        if (billTypes != null) {
            List<BillType> bts = Arrays.asList(billTypes);
            m.put("bts", bts);
            sql += " and i.bill.billType in :bts ";
        }

        if (referenceBillTypes != null) {
            List<BillType> rbts = Arrays.asList(referenceBillTypes);
            m.put("rbts", rbts);
            sql += " and i.bill.referenceBill.billType in :rbts ";
        }

        if (ins != null) {
            m.put("ins", ins);
            sql += " and (i.bill.institution=:ins or i.bill.department.institution=:ins) ";
        }
        sql += " group by i.item";
        sql += " order by i.item.name";
//        //System.out.println("m = " + m);
//        //System.out.println("sql = " + sql);
        List<ItemQuantityAndValues> lst = getBillItemFacade().findItemQuantityAndValuesList(sql, m, TemporalType.DATE);
        return lst;

    }

    public void averageByDatePercentage() {
        Calendar frm = Calendar.getInstance();
        frm.setTime(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTime(toDate);

        long lValue = to.getTimeInMillis() - frm.getTimeInMillis();
        double dayCount = 0;
        if (lValue != 0) {
            dayCount = (double) lValue / (1000 * 60 * 60 * 24);
        }

        //System.err.println("Day Count " + dayCount);
        createStockAverageByPer(dayCount);

    }

    public void createStockAverageByPer(double dayCount) {

        stockAverages = new ArrayList<>();
        List<Item> items = getItemController().getDealorItem();
        List<Institution> insList = getCompany();
        for (Item i : items) {
            double itemStockTotal = 0;
            double itemAverageTotal = 0;
            StockAverage stockAverage = new StockAverage();
            stockAverage.setItem(i);
            stockAverage.setInstitutionStocks(new ArrayList<InstitutionStock>());

            for (Institution ins : insList) {
                double insStockTotal = 0;
                double insAverageTotal = 0;
                double insStock = 0.0;
                InstitutionStock newTable = new InstitutionStock();
                newTable.setInstitution(ins);
                newTable.setDepatmentStocks(new ArrayList<DepartmentStock>());
                List<Object[]> objs = calDepartmentStock(ins, i);
                double calPerStock = 0.0;
                for (Object[] obj : objs) {
//                    //System.err.println("Inside ");
                    DepartmentStock r = new DepartmentStock();
                    r.setDepartment((Department) obj[0]);
                    r.setStock((Double) obj[1]);

                    double qty = calDepartmentSaleQtyByPer(r.getDepartment(), i);
                    qty = 0 - qty;
                    if (qty != 0 && dayCount != 0) {
                        double avg = qty / dayCount;
                        calPerStock = (avg * persentage) / 100;
                        insStock = r.getStock();
                        r.setAverage(avg);
                    }

//                    //////System.out.println("calPerStock = " + calPerStock);
//                    //////System.out.println("insStockTotal = " + insStockTotal);
//                    //////System.out.println("insAverageTotal = " + insAverageTotal);
                    if ((insStock < calPerStock) && r.getStock() != 0) {
                        //////System.out.println("*insStock = " + insStock);
                        //////System.out.println("*calPerStock = " + calPerStock);
                        insStockTotal += r.getStock();
                        insAverageTotal += r.getAverage();
                        newTable.getDepatmentStocks().add(r);
                    }

                }

//                //////System.out.println("calPerStock = " + calPerStock);
//                //////System.out.println("insStockTotal = " + insStockTotal);
//                //////System.out.println("insAverageTotal = " + insAverageTotal);
                newTable.setInstitutionTotal(insStockTotal);
                newTable.setInstitutionAverage(insAverageTotal);

                if ((insStockTotal != 0 || insAverageTotal != 0) && insStock < calPerStock) {
                    stockAverage.getInstitutionStocks().add(newTable);
                    itemStockTotal += insStockTotal;
                    itemAverageTotal += insAverageTotal;
                }
            }

            if (itemAverageTotal != 0 || itemStockTotal != 0) {
                stockAverage.setItemAverageTotal(itemAverageTotal);
                stockAverage.setItemStockTotal(itemStockTotal);
                stockAverages.add(stockAverage);
            }

        }

    }

    public void averageByMonthByPercentage() {
        Calendar frm = Calendar.getInstance();
        frm.setTime(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTime(toDate);

        long lValue = to.getTimeInMillis() - frm.getTimeInMillis();
        double monthCount = 0;
        if (lValue != 0) {
            monthCount = (double) lValue / (1000 * 60 * 60 * 24 * 30);
        }

        //System.err.println("Month Count " + monthCount);
        createStockAverageByPer(Math.abs(monthCount));

    }

    public double calDepartmentSaleQtyByPer(Department department, Item itm) {

        if (itm instanceof Ampp) {
            itm = ((Ampp) pharmacyItem).getAmp();
        }

        String sql;
        Map m = new HashMap();
        m.put("itm", itm);
        m.put("dep", department);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyPre);
        m.put("refType", BillType.PharmacySale);
        sql = "select sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i where i.bill.department=:dep"
                + " and i.bill.referenceBill.billType=:refType "
                + " and i.item=:itm and i.bill.billType=:btp and "
                + " i.createdAt between :frm and :to  ";

        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public void makeAllPharmaceuticalItemsToAllowDiscounts() {
        List<PharmaceuticalItem> pis = piFacade.findAll();
        for (PharmaceuticalItem pi : pis) {
            pi.setDiscountAllowed(true);
            piFacade.edit(pi);
        }
        JsfUtil.addSuccessMessage("All Pharmaceutical Items were made to allow discounts");

    }

    private double grantStock; // TODO: This is a typo. Will correct in later iterations

    public double getGrantStock() { // TODO: This is a typo. Will correct in later iterations
        return grantStock;
    }

    public double getTransferIssueValueByInstitution(Institution toIns, Item i) {
        //   List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("toIns", toIns);
        m.put("frmIns", getSessionController().getInstitution());
        m.put("i", i);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyTransferIssue);
        //   m.put("refType", BillType.PharmacySale);
        sql = "select sum(i.pharmaceuticalBillItem.stock.itemBatch.purcahseRate*i.pharmaceuticalBillItem.qty) "
                + "from BillItem i where i.bill.toInstitution=:toIns and i.bill.fromInstitution=:frmIns "
                + " and i.item=:i and i.bill.billType=:btp and i.createdAt between :frm and :to ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public double getTransferIssueValueByDepartmet(Department toDep, Item i) {
        //   List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("tDep", toDep);
        m.put("fDep", getSessionController().getDepartment());
        m.put("i", i);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyTransferIssue);
        //   m.put("refType", BillType.PharmacySale);
        sql = "select sum(i.pharmaceuticalBillItem.stock.itemBatch.purcahseRate*i.pharmaceuticalBillItem.qty) "
                + " from BillItem i where i.bill.toDepartment=:tDep and i.bill.fromDepartment=:fDep "
                + " and i.item=:i and i.bill.billType=:btp and i.createdAt between :frm and :to ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public double getTransferIssueQtyByDepartmet(Department tDep, Item i) {
        //   List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("tDep", tDep);
        m.put("fDep", getSessionController().getDepartment());
        m.put("i", i);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyTransferIssue);
        //   m.put("refType", BillType.PharmacySale);
        sql = "select sum(i.pharmaceuticalBillItem.qty) from BillItem i where i.bill.toDepartment=:tDep"
                + " and  i.bill.fromDepartment=:fDep"
                + " and i.item=:i and i.bill.billType=:btp and i.createdAt between :frm and :to ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public double getTransferIssueQtyByInstitution(Institution toIns, Item i) {
        //   List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("tIns", toIns);
        m.put("fIns", getSessionController().getInstitution());
        m.put("i", i);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyTransferIssue);
        //   m.put("refType", BillType.PharmacySale);
        sql = "select sum(i.pharmaceuticalBillItem.qty) from BillItem i where "
                + " i.bill.toInstitution=:tIns and i.bill.fromInstitution=:fIns "
                + " and i.item=:i and i.bill.billType=:btp and i.createdAt between :frm and :to ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private double grantSaleQty;
    private double grantBhtIssueQty;
    private double grantSaleValue;
    private double grantBhtValue;

    private double grantWholeSaleQty;
    private double grantWholeSaleValue;

    private double grantTransferIssueQty;
    private double grantIssueQty;
    private double grantTransferIssueValue;
    private double grantIssueValue;

    @EJB
    private InstitutionFacade institutionFacade;

    private List<Institution> getCompany() {
        String sql;
        HashMap hm = new HashMap();
        hm.put("type", InstitutionType.Company);
        sql = "select c from Institution c where c.retired=false and c.institutionType=:type order by c.name";

        return getInstitutionFacade().findByJpql(sql, hm);
    }

    private List<InstitutionStock> institutionStocks;

    public List<Object[]> calDepartmentStock(Institution institution) {
        //   //System.err.println("Cal Department Stock");
        Item item;

        if (pharmacyItem instanceof Ampp) {
            item = ((Ampp) pharmacyItem).getAmp();
        } else {
            item = pharmacyItem;
        }

        String sql;
        Map m = new HashMap();
        m.put("ins", institution);
        m.put("i", item);
        sql = "select i.department,sum(i.stock) from Stock i where "
                + " i.department.institution=:ins and i.itemBatch.item=:i"
                + " group by i.department"
                + " having sum(i.stock) > 0 ";

        return getBillItemFacade().findAggregates(sql, m, TemporalType.TIMESTAMP);

    }

    public List<Object[]> calDepartmentStock(Institution institution, Item itm) {
        //   //System.err.println("Cal Department Stock");

        if (itm instanceof Ampp) {
            itm = ((Ampp) itm).getAmp();
        }

        String sql;
        Map m = new HashMap();
        m.put("ins", institution);
        m.put("i", itm);
        sql = "select i.department,sum(i.stock) from Stock i where "
                + " i.department.institution=:ins and i.itemBatch.item=:i"
                + " group by i.department"
                + " having sum(i.stock) > 0 ";

        return getBillItemFacade().findAggregates(sql, m);

    }

    public List<Object[]> calDepartmentTransferIssue(Institution institution) {
        Item item;

        if (pharmacyItem instanceof Ampp) {
            item = ((Ampp) pharmacyItem).getAmp();
        } else {
            item = pharmacyItem;
        }

        String sql;
        Map m = new HashMap();
        m.put("curr", getSessionController().getDepartment());
        m.put("i", item);
        m.put("ins", institution);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyTransferIssue);
        //   m.put("refType", BillType.PharmacySale);
        sql = "select i.bill.toDepartment,"
                + " sum(i.pharmaceuticalBillItem.stock.itemBatch.purcahseRate*i.pharmaceuticalBillItem.qty),"
                + " sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i "
                + " where i.bill.toDepartment.institution=:ins "
                + " and i.bill.department=:curr "
                + " and i.item=:i"
                + " and i.bill.billType=:btp "
                + " and i.createdAt between :frm and :to"
                + " group by i.bill.toDepartment";

        return getBillItemFacade().findAggregates(sql, m, TemporalType.TIMESTAMP);

    }

    public List<Object[]> calDepartmentIssue(Institution institution) {
        Item item;

        if (pharmacyItem instanceof Ampp) {
            item = ((Ampp) pharmacyItem).getAmp();
        } else {
            item = pharmacyItem;
        }

        String sql;
        Map m = new HashMap();
//        m.put("curr", getSessionController().getDepartment());
        m.put("i", item);
        m.put("ins", institution);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyIssue);
        //   m.put("refType", BillType.PharmacySale);
//        sql = "select i.bill.toDepartment,"
//                + " sum(i.pharmaceuticalBillItem.stock.itemBatch.purcahseRate*i.pharmaceuticalBillItem.qty),"
//                + " sum(i.pharmaceuticalBillItem.qty) "
//                + " from BillItem i "
//                + " where i.bill.toDepartment.institution=:ins "
//                + " and i.bill.department=:curr "
//                + " and i.item=:i"
//                + " and i.bill.billType=:btp "
//                + " and i.createdAt between :frm and :to"
//                + " group by i.bill.toDepartment";

        sql = "select i.billItem.bill.toDepartment,"
                + " sum(i.stock.itemBatch.purcahseRate*i.qty),"
                + " sum(i.qty) "
                + " from PharmaceuticalBillItem i "
                + " where i.billItem.bill.toDepartment.institution=:ins "
                //                + " and i.billItem.bill.department=:curr "
                + " and i.billItem.item=:i"
                + " and i.billItem.bill.billType=:btp "
                + " and i.billItem.createdAt between :frm and :to"
                + " group by i.billItem.bill.toDepartment";

        return getBillItemFacade().findAggregates(sql, m, TemporalType.TIMESTAMP);

    }

    public List<Object[]> calDepartmentTransferReceive(Institution institution) {
        Item item;

        if (pharmacyItem instanceof Ampp) {
            item = ((Ampp) pharmacyItem).getAmp();
        } else {
            item = pharmacyItem;
        }

        String sql;
        Map m = new HashMap();
        m.put("dep", getSessionController().getDepartment());
        m.put("i", item);
        m.put("ins", institution);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyTransferReceive);
        //   m.put("refType", BillType.PharmacySale);
        sql = "select i.bill.fromDepartment,sum(i.pharmaceuticalBillItem.stock.itemBatch.purcahseRate*i.pharmaceuticalBillItem.qty),"
                + " sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i where i.bill.fromDepartment.institution=:ins and i.bill.department=:dep "
                + " and i.item=:i and i.bill.billType=:btp and i.createdAt between :frm and :to group by i.bill.fromDepartment";

        return getBillItemFacade().findAggregates(sql, m, TemporalType.TIMESTAMP);

    }

    public List<Object[]> calDepartmentBhtIssue(Institution institution, BillType billType) {
        Item item;

        if (pharmacyItem instanceof Ampp) {
            item = ((Ampp) pharmacyItem).getAmp();
        } else {
            item = pharmacyItem;
        }

        String sql;
        Map m = new HashMap();
        m.put("itm", item);
        m.put("ins", institution);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", billType);
        sql = "select i.bill.department,"
                + " sum(i.netValue),"
                + " sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i "
                + " where i.bill.department.institution=:ins"
                + " and i.item=:itm "
                + " and i.bill.billType=:btp "
                + " and i.createdAt between :frm and :to  "
                + " group by i.bill.department";

        return getBillItemFacade().findAggregates(sql, m, TemporalType.TIMESTAMP);

    }

    public double findAllOutTransactions(Item item) {
        if (item instanceof Amp) {
            return findAllOutTransactions(amp);
        } else if (item instanceof Vmp) {
            List<Amp> amps = vmpController.ampsOfVmp(item);
            return findAllOutTransactions(amps);
        } else {
            return 0.0;
        }
    }

    public double findAllOutTransactions(Amp item) {
        List<Amp> amps = new ArrayList<>();
        amps.add(item);
        return findAllOutTransactions(amps);
    }

    public double findAllOutTransactions(List<Amp> amps) {
        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.PharmacyBhtPre);
        bts.add(BillType.PharmacyPre);
        return findTransactionStocks(null, null, bts, amps, fromDate, toDate);
    }

    public double findTransactionStocks(Department dep, Institution ins, List<BillType> billTypes, List<Amp> amps, Date fd, Date td) {
        StringBuilder jpqlBuilder = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();

        jpqlBuilder.append("select sum(abs(i.pharmaceuticalBillItem.qty)) from BillItem i where i.retired = false");

        if (dep != null) {
            jpqlBuilder.append(" and i.bill.department = :dep");
            parameters.put("dep", dep);
        }
        if (ins != null) {
            jpqlBuilder.append(" and i.bill.department.institution = :ins");
            parameters.put("ins", ins);
        }
        if (billTypes != null && !billTypes.isEmpty()) {
            jpqlBuilder.append(" and i.bill.billType in :btp");
            parameters.put("btp", billTypes);
        }
        if (amps != null && !amps.isEmpty()) {
            jpqlBuilder.append(" and i.item in :itm");
            parameters.put("itm", amps);
        }
        if (fd != null && td != null) {
            jpqlBuilder.append(" and i.createdAt between :frm and :to");
            parameters.put("frm", fd);
            parameters.put("to", td);
        }

        jpqlBuilder.append(" group by i.bill.department");

        String jpql = jpqlBuilder.toString();
        double qty = getBillItemFacade().findDoubleByJpql(jpql, parameters, TemporalType.TIMESTAMP);
        return qty;
    }

    public double findTransactionQuentity(Item item) {
        //TO DO Senula
        List<BillType> billTypes = new ArrayList<>();
        billTypes.add(BillType.PharmacySale);
        billTypes.add(BillType.InwardPharmacyRequest);
        return 0.0;
    }

    public double findTransactionQuentity(Institution institution, Amp item, List<BillType> billTypes) {
        //TO DO Senula
        return 0.0;
    }

    public double findTransactionQuentity(Institution institution, Item item, List<BillType> billTypes, Date fromDate, Date toDate) {
        //TO DO Senula
        if (pharmacyItem instanceof Ampp) {
            item = ((Ampp) pharmacyItem).getAmp();
        } else {
            item = pharmacyItem;
        }
//        String sql;

//        sql = "select i "
//                + " from BillItem i "
//                + " where i.bill.department.institution=:ins"
//                + " and i.bill.referenceBill.billType=:refType "
//                + " and i.bill.referenceBill.cancelled=false "
//                + " and i.item=:itm "
//                + " and i.bill.billType=:btp "
//                + " and i.createdAt between :frm and :to  "
//                + " order by i.bill.department.name,i.bill.insId ";
        Map m = new HashMap();

        m.put("itm", item);
        m.put("ins", institution);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyPre);
        m.put("refType", BillType.PharmacySale);
//
//        List<BillItem> billItems=getBillItemFacade().findByJpql(sql, m, TemporalType.TIMESTAMP);
//        if (billItems!=null) {
//            grns.addAll(billItems);
//        }
//        //System.out.println("billItems = " + billItems);
//        //System.out.println("institution.getName() = " + institution.getName());

//        for (BillItem bi : billItems) {
//            //System.out.println("bi.getBill().getDepartment().getName() = " + bi.getBill().getDepartment().getName());
//            //System.out.println("bi.getInsId() = " + bi.getInsId());
//            //System.out.println("bi.getDeptId() = " + bi.getDeptId());
//            //System.out.println("bi.getPharmaceuticalBillItem().getQty() = " + bi.getPharmaceuticalBillItem().getQty());
//        }
//        sql = "select i.bill.department,"
//                + " sum(i.netValue),"
//                + " sum(i.pharmaceuticalBillItem.qty) "
//                + " from BillItem i "
//                + " where i.bill.department.institution=:ins"
//                + " and i.bill.referenceBill.billType=:refType "
//                + " and i.bill.referenceBill.cancelled=false "
//                + " and i.item=:itm "
//                + " and i.bill.billType=:btp "
//                + " and i.createdAt between :frm and :to  "
//                + " group by i.bill.department";
        return 0.0;

    }

    public List<Object[]> calDepartmentSale(Institution institution) {
        Item selectedItem;
        if (pharmacyItem instanceof Ampp) {
            selectedItem = ((Ampp) pharmacyItem).getAmp();
        } else {
            selectedItem = pharmacyItem;
        }
        String sql;
        Map m = new HashMap();
        m.put("itm", selectedItem);
        m.put("ins", institution);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyPre);
        m.put("refType", BillType.PharmacySale);
        sql = "select i.bill.department,"
                + " sum(i.netValue),"
                + " sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i "
                + " where i.bill.department.institution=:ins"
                + " and i.bill.referenceBill.billType=:refType "
                + " and i.bill.referenceBill.cancelled=false "
                + " and i.item=:itm "
                + " and i.bill.billType=:btp "
                + " and i.createdAt between :frm and :to  "
                + " group by i.bill.department";
        return getBillItemFacade().findAggregates(sql, m, TemporalType.TIMESTAMP);
    }

    public List<Object[]> calDepartmentSalesAllInstitutions(List<Institution> institutions) {
        Item selectedItem;
        if (pharmacyItem instanceof Ampp) {
            selectedItem = ((Ampp) pharmacyItem).getAmp();
        } else {
            selectedItem = pharmacyItem;
        }

        String sql = "select i.bill.department.institution, i.bill.department,"
                + " sum(i.netValue),"
                + " sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i "
                + " where i.bill.referenceBill.billType=:refType "
                + " and i.bill.referenceBill.cancelled=false "
                + " and i.item=:itm "
                + " and i.bill.billType=:btp "
                + " and i.createdAt between :frm and :to ";

        Map m = new HashMap();
        m.put("itm", selectedItem);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyPre);
        m.put("refType", BillType.PharmacySale);

        if (institutions != null && !institutions.isEmpty()) {
            sql += " and i.bill.department.institution in :ins";
            m.put("ins", institutions);
        }

        sql += " group by i.bill.department.institution, i.bill.department";

        return getBillItemFacade().findAggregates(sql, m, TemporalType.TIMESTAMP);
    }

    public List<Object[]> calDepartmentWholeSale(Institution institution) {
        Item item;

        if (pharmacyItem instanceof Ampp) {
            item = ((Ampp) pharmacyItem).getAmp();
        } else {
            item = pharmacyItem;
        }

        String sql;
        Map m = new HashMap();
        m.put("itm", item);
        m.put("ins", institution);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyWholesalePre);
        m.put("refType", BillType.PharmacyWholeSale);
        sql = "select i.bill.department,"
                + " sum(i.netValue),"
                + " sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i "
                + " where i.bill.department.institution=:ins"
                + " and i.bill.referenceBill.billType=:refType "
                + " and i.item=:itm "
                + " and i.bill.billType=:btp "
                + " and i.createdAt between :frm and :to  "
                + " group by i.bill.department";

        return getBillItemFacade().findAggregates(sql, m, TemporalType.TIMESTAMP);

    }

    public double calDepartmentSaleQty(Department department, Item itm) {

        if (itm instanceof Ampp) {
            itm = ((Ampp) pharmacyItem).getAmp();
        }

        String sql;
        Map m = new HashMap();
        m.put("itm", itm);
        m.put("dep", department);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyPre);
        m.put("refType", BillType.PharmacySale);
        sql = "select sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i where i.bill.department=:dep"
                + " and i.bill.referenceBill.billType=:refType "
                + " and i.item=:itm and i.bill.billType=:btp and "
                + " i.createdAt between :frm and :to  ";

        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private Institution institution;
    private List<StockAverage> stockAverages;

    @Inject
    private ItemController itemController;

    public void averageByDate() {
        Date startTime = new Date();

        Calendar frm = Calendar.getInstance();
        frm.setTime(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTime(toDate);

        long lValue = to.getTimeInMillis() - frm.getTimeInMillis();
        double dayCount = 0;
        if (lValue != 0) {
            dayCount = lValue / (1000 * 60 * 60 * 24);
        }

        createStockAverage(dayCount);

    }

    public double getGrantWholeSaleQty() {
        return grantWholeSaleQty;
    }

    public void setGrantWholeSaleQty(double grantWholeSaleQty) {
        this.grantWholeSaleQty = grantWholeSaleQty;
    }

    public double getGrantWholeSaleValue() {
        return grantWholeSaleValue;
    }

    public void setGrantWholeSaleValue(double grantWholeSaleValue) {
        this.grantWholeSaleValue = grantWholeSaleValue;
    }

    public void averageByMonth() {
        Date startTime = new Date();

        Calendar frm = Calendar.getInstance();
        frm.setTime(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTime(toDate);

        long lValue = to.getTimeInMillis() - frm.getTimeInMillis();
        double monthCount = 0;
        if (lValue != 0) {
            monthCount = (double) lValue / (1000 * 60 * 60 * 24 * 30);
        }

        createStockAverage(Math.abs(monthCount));

    }

    public void createStockAverage(double dayCount) {

        stockAverages = new ArrayList<>();
        List<Item> items = getItemController().getDealorItem();
        List<Institution> insList = getCompany();
        for (Item i : items) {
            double itemStockTotal = 0;
            double itemAverageTotal = 0;
            StockAverage stockAverage = new StockAverage();
            stockAverage.setItem(i);
            stockAverage.setInstitutionStocks(new ArrayList<InstitutionStock>());

            for (Institution ins : insList) {
                double insStockTotal = 0;
                double insAverageTotal = 0;
                InstitutionStock newTable = new InstitutionStock();
                newTable.setInstitution(ins);
                newTable.setDepatmentStocks(new ArrayList<DepartmentStock>());
                List<Object[]> objs = calDepartmentStock(ins, i);

                for (Object[] obj : objs) {
//                    System.err.println("Inside ");
                    DepartmentStock r = new DepartmentStock();
                    r.setDepartment((Department) obj[0]);
                    r.setStock((Double) obj[1]);

                    double qty = calDepartmentSaleQty(r.getDepartment(), i);
                    qty = 0 - qty;
                    if (qty != 0 && dayCount != 0) {
                        double avg = qty / dayCount;
                        r.setAverage(avg);
                    }

                    insStockTotal += r.getStock();
                    insAverageTotal += r.getAverage();
                    newTable.getDepatmentStocks().add(r);

                }

                newTable.setInstitutionTotal(insStockTotal);
                newTable.setInstitutionAverage(insAverageTotal);

                if (insStockTotal != 0 || insAverageTotal != 0) {
                    stockAverage.getInstitutionStocks().add(newTable);
                    itemStockTotal += insStockTotal;
                    itemAverageTotal += insAverageTotal;
                }
            }

            stockAverage.setItemAverageTotal(itemAverageTotal);
            stockAverage.setItemStockTotal(itemStockTotal);
            stockAverages.add(stockAverage);
        }

    }

    public void createInstitutionStock() {
        //   //System.err.println("Institution Stock");
        List<Institution> insList = getCompany();

        institutionStocks = new ArrayList<>();
        grantStock = 0;

        for (Institution ins : insList) {
            InstitutionStock newTable = new InstitutionStock();
            List<DepartmentStock> list = new ArrayList<>();
            double totalStock = 0;
            List<Object[]> objs = calDepartmentStock(ins);

            for (Object[] obj : objs) {
                DepartmentStock r = new DepartmentStock();
                r.setDepartment((Department) obj[0]);
                r.setStock((Double) obj[1]);
                list.add(r);

                //Total Institution Stock
                totalStock += r.getStock();
                grantStock += r.getStock();

            }

            if (totalStock != 0) {
                newTable.setDepatmentStocks(list);
                newTable.setInstitution(ins);
                newTable.setInstitutionTotal(totalStock);

                institutionStocks.add(newTable);
            }
        }

    }

    public void createInstitutionSale() {
        List<Institution> insList = getCompany();
        institutionSales = new ArrayList<>();
        grantSaleQty = 0;
        grantSaleValue = 0;

        Map<Institution, InstitutionSale> saleMap = new LinkedHashMap<>();
        for (Institution ins : insList) {
            InstitutionSale is = new InstitutionSale();
            is.setInstitution(ins);
            is.setDepartmentSales(new ArrayList<>());
            saleMap.put(ins, is);
        }

        List<Object[]> results = calDepartmentSalesAllInstitutions(insList);

        for (Object[] obj : results) {
            Institution ins = (Institution) obj[0];
            Department dep = (Department) obj[1];
            double val = (Double) obj[2];
            double qty = (Double) obj[3];

            InstitutionSale insSale = saleMap.get(ins);
            if (insSale == null) {
                insSale = new InstitutionSale();
                insSale.setInstitution(ins);
                insSale.setDepartmentSales(new ArrayList<>());
                saleMap.put(ins, insSale);
            }

            DepartmentSale dSale = new DepartmentSale();
            dSale.setDepartment(dep);
            dSale.setSaleValue(val);
            dSale.setSaleQty(qty);
            insSale.getDepartmentSales().add(dSale);

            insSale.setInstitutionValue(insSale.getInstitutionValue() + val);
            insSale.setInstitutionQty(insSale.getInstitutionQty() + qty);

            grantSaleValue += val;
            grantSaleQty += qty;
        }

        for (Institution ins : insList) {
            InstitutionSale is = saleMap.get(ins);
            if (is != null && (is.getInstitutionQty() != 0 || is.getInstitutionValue() != 0)) {
                institutionSales.add(is);
            }
        }
    }

    public void createInstitutionWholeSale() {
        List<Institution> insList = getCompany();

        institutionWholeSales = new ArrayList<>();
        grantWholeSaleQty = 0;
        grantWholeSaleValue = 0;

        for (Institution ins : insList) {
            InstitutionSale newTable = new InstitutionSale();
            List<DepartmentSale> list = new ArrayList<>();
            double totalValue = 0;
            double totalQty = 0;
            List<Object[]> objs = calDepartmentWholeSale(ins);

            for (Object[] obj : objs) {
                DepartmentSale r = new DepartmentSale();
                r.setDepartment((Department) obj[0]);
                r.setSaleValue((Double) obj[1]);
                r.setSaleQty((Double) obj[2]);
                list.add(r);
                //Total Institution Stock
                totalValue += r.getSaleValue();
                totalQty += r.getSaleQty();
                grantWholeSaleValue += r.getSaleValue();
                grantWholeSaleQty += r.getSaleQty();

            }

            if (totalQty != 0 || totalValue != 0) {
                newTable.setDepartmentSales(list);
                newTable.setInstitution(ins);
                newTable.setInstitutionQty(totalQty);
                newTable.setInstitutionValue(totalValue);

                institutionWholeSales.add(newTable);

            }
        }

    }

    public double getGrantBhtIssueQty() {
        return grantBhtIssueQty;
    }

    public void setGrantBhtIssueQty(double grantBhtIssueQty) {
        this.grantBhtIssueQty = grantBhtIssueQty;
    }

    public double getGrantBhtValue() {
        return grantBhtValue;
    }

    public void setGrantBhtValue(double grantBhtValue) {
        this.grantBhtValue = grantBhtValue;
    }

    public List<InstitutionSale> getInstitutionBhtIssue() {
        return institutionBhtIssue;
    }

    public void setInstitutionBhtIssue(List<InstitutionSale> institutionBhtIssue) {
        this.institutionBhtIssue = institutionBhtIssue;
    }

    public void createInstitutionBhtIssue() {
        List<Institution> insList = getCompany();

        institutionBhtIssue = new ArrayList<>();
        grantBhtIssueQty = 0;
        grantBhtValue = 0;

        for (Institution ins : insList) {
            InstitutionSale newTable = new InstitutionSale();
            List<DepartmentSale> list = new ArrayList<>();
            double totalValue = 0;
            double totalQty = 0;
            List<Object[]> objs = calDepartmentBhtIssue(ins, BillType.PharmacyBhtPre);

            for (Object[] obj : objs) {
                DepartmentSale r = new DepartmentSale();
                r.setDepartment((Department) obj[0]);
                r.setSaleValue((Double) obj[1]);
                r.setSaleQty((Double) obj[2]);
                list.add(r);
                //Total Institution Stock
                totalValue += r.getSaleValue();
                totalQty += r.getSaleQty();
                grantBhtValue += r.getSaleValue();
                grantBhtIssueQty += r.getSaleQty();

            }

            if (totalQty != 0 || totalValue != 0) {
                newTable.setDepartmentSales(list);
                newTable.setInstitution(ins);
                newTable.setInstitutionQty(totalQty);
                newTable.setInstitutionValue(totalValue);

                institutionBhtIssue.add(newTable);

            }
        }

    }

    public List<InstitutionStock> getInstitutionStock() {
        return institutionStocks;
    }

    private List<InstitutionSale> institutionSales;
    private List<InstitutionSale> institutionWholeSales;
    private List<InstitutionSale> institutionBhtIssue;

    private List<InstitutionSale> institutionTransferIssue;
    private List<InstitutionSale> institutionIssue;

    public List<InstitutionSale> getInstitutionIssue() {
        return institutionIssue;
    }

    public void setInstitutionIssue(List<InstitutionSale> institutionIssue) {
        this.institutionIssue = institutionIssue;
    }

    public List<InstitutionSale> getInstitutionWholeSales() {
        return institutionWholeSales;
    }

    public void setInstitutionWholeSales(List<InstitutionSale> institutionWholeSales) {
        this.institutionWholeSales = institutionWholeSales;
    }

    private List<InstitutionSale> institutionTransferReceive;
    private double grantTransferReceiveQty = 0;
    private double grantTransferReceiveValue = 0;

    public void createInstitutionTransferIssue() {
        List<Institution> insList = getCompany();

        institutionTransferIssue = new ArrayList<>();
        grantTransferIssueQty = 0;
        grantTransferIssueValue = 0;

        for (Institution ins : insList) {
            InstitutionSale newTable = new InstitutionSale();
            List<DepartmentSale> list = new ArrayList<>();
            double totalValue = 0;
            double totalQty = 0;
            List<Object[]> objs = calDepartmentTransferIssue(ins);

            for (Object[] obj : objs) {
                DepartmentSale r = new DepartmentSale();
                r.setDepartment((Department) obj[0]);
                r.setSaleValue((Double) obj[1]);
                r.setSaleQty((Double) obj[2]);
                list.add(r);
                //Total Institution Stock
                totalValue += r.getSaleValue();
                totalQty += r.getSaleQty();
                grantTransferIssueValue += r.getSaleValue();
                grantTransferIssueQty += r.getSaleQty();

            }

            if (totalQty != 0 || totalValue != 0) {
                newTable.setDepartmentSales(list);
                newTable.setInstitution(ins);
                newTable.setInstitutionQty(totalQty);
                newTable.setInstitutionValue(totalValue);

                institutionTransferIssue.add(newTable);

            }
        }

    }

    public double getGrantIssueQty() {
        return grantIssueQty;
    }

    public void setGrantIssueQty(double grantIssueQty) {
        this.grantIssueQty = grantIssueQty;
    }

    public double getGrantIssueValue() {
        return grantIssueValue;
    }

    public void setGrantIssueValue(double grantIssueValue) {
        this.grantIssueValue = grantIssueValue;
    }

    public void createInstitutionIssue() {
        List<Institution> insList = getCompany();

        institutionIssue = new ArrayList<>();
        grantIssueQty = 0;
        grantIssueValue = 0;

        for (Institution ins : insList) {
            InstitutionSale newTable = new InstitutionSale();
            List<DepartmentSale> list = new ArrayList<>();
            double totalValue = 0;
            double totalQty = 0;
            List<Object[]> objs = calDepartmentIssue(ins);

            for (Object[] obj : objs) {
                DepartmentSale r = new DepartmentSale();
                r.setDepartment((Department) obj[0]);
                r.setSaleValue((Double) obj[1]);
                r.setSaleQty((Double) obj[2]);
                list.add(r);
                //Total Institution Stock
                totalValue += r.getSaleValue();
                totalQty += r.getSaleQty();
                grantIssueValue += r.getSaleValue();
                grantIssueQty += r.getSaleQty();

            }

            if (totalQty != 0 || totalValue != 0) {
                newTable.setDepartmentSales(list);
                newTable.setInstitution(ins);
                newTable.setInstitutionQty(totalQty);
                newTable.setInstitutionValue(totalValue);

                institutionIssue.add(newTable);

            }
        }

    }

    public void createInstitutionTransferReceive() {
        List<Institution> insList = getCompany();

        institutionTransferReceive = new ArrayList<>();
        grantTransferReceiveQty = 0;
        grantTransferReceiveValue = 0;

        for (Institution ins : insList) {
            InstitutionSale newTable = new InstitutionSale();
            List<DepartmentSale> list = new ArrayList<>();
            double totalValue = 0;
            double totalQty = 0;
            List<Object[]> objs = calDepartmentTransferReceive(ins);

            for (Object[] obj : objs) {
                DepartmentSale r = new DepartmentSale();
                r.setDepartment((Department) obj[0]);
                r.setSaleValue((Double) obj[1]);
                r.setSaleQty((Double) obj[2]);
                list.add(r);
                //Total Institution Stock
                totalValue += r.getSaleValue();
                totalQty += r.getSaleQty();
                grantTransferReceiveValue += r.getSaleValue();
                grantTransferReceiveQty += r.getSaleQty();

            }

            if (totalQty != 0 || totalValue != 0) {
                newTable.setDepartmentSales(list);
                newTable.setInstitution(ins);
                newTable.setInstitutionQty(totalQty);
                newTable.setInstitutionValue(totalValue);

                institutionTransferReceive.add(newTable);

            }
        }

    }

    public List<BillItem> getGrns() {
        return grns;
    }

    public List<BillItem> getPendingGrns() {
        return pendingGrns;
    }

    public List<com.divudi.core.data.dto.PharmacyGrnItemDTO> getGrnDtos() {
        return grnDtos;
    }

    public void setGrnDtos(List<com.divudi.core.data.dto.PharmacyGrnItemDTO> grnDtos) {
        this.grnDtos = grnDtos;
    }

    public List<com.divudi.core.data.dto.PharmacyGrnReturnItemDTO> getGrnReturnDtos() {
        return grnReturnDtos;
    }

    public void setGrnReturnDtos(List<com.divudi.core.data.dto.PharmacyGrnReturnItemDTO> grnReturnDtos) {
        this.grnReturnDtos = grnReturnDtos;
    }

    public void fillDetails() {
        createInstitutionSale();
        createInstitutionWholeSale();
        createInstitutionBhtIssue();
        createInstitutionStock();
        createInstitutionTransferIssue();
        createInstitutionTransferReceive();
        createPendingGrnTable();
        createGrnTable();
        createGrnReturnTable();
        createPoTable();
        createDirectPurchaseTable();
        createInstitutionIssue();
    }

    @Deprecated // Use fillDetails
    public void createTable() {
        createInstitutionSale();
        createInstitutionBhtIssue();
        createInstitutionStock();
        createInstitutionTransferIssue();
        createInstitutionIssue();
        createInstitutionTransferReceive();
        createGrnTable();
        createPoTable();
        createDirectPurchaseTable();
        createInstitutionIssue();
    }

    public void createGrnTable() {
        List<Item> relatedAmpAndAmpps = pharmacyService.findRelatedItems(pharmacyItem);
        String jpql = "SELECT new com.divudi.core.data.dto.PharmacyGrnItemDTO("
                + "b.bill.deptId, "
                + "b.bill.department.name, "
                + "b.bill.createdAt, "
                + "b.bill.referenceBill.deptId, "
                + "b.bill.fromInstitution.name, "
                + "b.item.name, "
                + "b.billItemFinanceDetails.quantity, "
                + "b.billItemFinanceDetails.freeQuantity, "
                + "b.pharmaceuticalBillItem.purchaseRate, "
                + "b.billItemFinanceDetails.totalCostRate, "
                + "b.billItemFinanceDetails.retailSaleRate, "
                + "b.billItemFinanceDetails.netTotal) "
                + "FROM BillItem b "
                + "WHERE type(b.bill)=:class "
                + "AND b.bill.creater is not null "
                + "AND b.bill.cancelled=false "
                + "AND b.retired=false "
                + "AND b.item IN :relatedAmpAndAmpps "
                + "AND b.bill.billTypeAtomic IN :btas "
                + "AND b.createdAt between :frm and :to "
                + "order by b.id desc";

        List<BillTypeAtomic> btas = new ArrayList<>();
        btas.add(BillTypeAtomic.PHARMACY_GRN);
        btas.add(BillTypeAtomic.PHARMACY_GRN_CANCELLED);
        btas.add(BillTypeAtomic.PHARMACY_GRN_WHOLESALE);
        btas.add(BillTypeAtomic.PHARMACY_WHOLESALE_GRN_BILL);
        btas.add(BillTypeAtomic.PHARMACY_WHOLESALE_GRN_BILL_CANCELLED);

        Map<String, Object> params = new HashMap<>();
        params.put("relatedAmpAndAmpps", relatedAmpAndAmpps);
        params.put("frm", getFromDate());
        params.put("to", getToDate());
        params.put("class", BilledBill.class);
        params.put("btas", btas);

        grnDtos = (List<PharmacyGrnItemDTO>) getBillItemFacade().findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    @Deprecated // Will remove later when all bill corrected to add bill item finance details
    public void debugGrnReturnData() {

        List<Item> relatedItems = pharmacyService.findRelatedItems(pharmacyItem);

        if (relatedItems == null || relatedItems.isEmpty()) {
            return;
        }

        List<BillTypeAtomic> btas = new ArrayList<>();
        btas.add(BillTypeAtomic.PHARMACY_GRN_RETURN);
        btas.add(BillTypeAtomic.PHARMACY_GRN_REFUND);
        btas.add(BillTypeAtomic.PHARMACY_GRN_RETURN_CANCELLATION);

        String jpql = "SELECT b, b.billItemFinanceDetails, b.pharmaceuticalBillItem "
                + "FROM BillItem b "
                + "WHERE type(b.bill)=:class "
                + "AND b.bill.creater IS NOT NULL "
                + "AND b.retired=false "
                + "AND b.item IN :relatedItems "
                + "AND b.bill.billTypeAtomic IN :btas "
                + "AND b.createdAt BETWEEN :frm AND :to "
                + "ORDER BY b.id DESC";

        Map<String, Object> params = new HashMap<>();
        params.put("relatedItems", relatedItems);
        params.put("frm", getFromDate());
        params.put("to", getToDate());
        params.put("class", BilledBill.class);
        params.put("btas", btas);

        List<Object[]> results = getBillItemFacade().findRawResultsByJpql(jpql, params, TemporalType.TIMESTAMP);

        if (results == null || results.isEmpty()) {
            return;
        }

        for (Object[] row : results) {
            BillItem bi = (BillItem) row[0];
            BillItemFinanceDetails fd = (BillItemFinanceDetails) row[1];
            PharmaceuticalBillItem pbi = (PharmaceuticalBillItem) row[2];

        }
    }

    public void createGrnReturnTable() {

        List<Item> relatedItems = pharmacyService.findRelatedItems(pharmacyItem);
        if (relatedItems == null || relatedItems.isEmpty()) {
            grnReturnDtos = new ArrayList<>();
            return;
        }

        // TODO: Once all old BillItems are updated with BillItemFinanceDetails,
        //       this method should be changed back to use JPQL DTO projection for better performance.
        String jpql = "SELECT b FROM BillItem b "
                + "WHERE type(b.bill)=:class "
                + "AND b.bill.creater IS NOT NULL "
                + "AND b.retired=false "
                + "AND b.item IN :relatedItems "
                + "AND b.bill.billTypeAtomic IN :btas "
                + "AND b.createdAt BETWEEN :frm AND :to "
                + "ORDER BY b.id DESC";

        List<BillTypeAtomic> btas = new ArrayList<>();
        btas.add(BillTypeAtomic.PHARMACY_GRN_RETURN);
        btas.add(BillTypeAtomic.PHARMACY_GRN_REFUND);
        btas.add(BillTypeAtomic.PHARMACY_GRN_RETURN_CANCELLATION);

        Map<String, Object> params = new HashMap<>();
        params.put("relatedItems", relatedItems);
        params.put("frm", getFromDate());
        params.put("to", getToDate());
        params.put("class", BilledBill.class);
        params.put("btas", btas);


        List<BillItem> billItems = (List<BillItem>) getBillItemFacade().findLightsByJpql(jpql, params, TemporalType.TIMESTAMP);
        grnReturnDtos = new ArrayList<>();

        for (BillItem b : billItems) {
            BillItemFinanceDetails fd = b.getBillItemFinanceDetails();
            PharmaceuticalBillItem pbi = b.getPharmaceuticalBillItem();

            PharmacyGrnReturnItemDTO dto = new PharmacyGrnReturnItemDTO();

            dto.setGrnReturnNo(b.getBill() != null ? b.getBill().getDeptId() : null);
            dto.setDepartmentName(b.getBill() != null && b.getBill().getDepartment() != null ? b.getBill().getDepartment().getName() : null);
            dto.setCreatedAt(b.getBill() != null ? b.getBill().getCreatedAt() : null);
            dto.setSupplierName(b.getBill() != null && b.getBill().getFromInstitution() != null ? b.getBill().getFromInstitution().getName() : null);
            dto.setItemName(b.getItem() != null ? b.getItem().getName() : null);

            // TODO: These values are missing for old bills without BillItemFinanceDetails
            dto.setQuantityReturned(fd != null ? fd.getQuantity() : BigDecimal.ZERO);
            dto.setFreeQuantityReturned(fd != null ? fd.getFreeQuantity() : BigDecimal.ZERO);
            dto.setPurchaseRate(pbi != null ? pbi.getPurchaseRate() : null);
            dto.setSaleRate(fd != null ? fd.getRetailSaleRate() : null);
            dto.setReturnedRate(fd != null ? fd.getNetRate() : null);
            dto.setReturnValue(fd != null ? fd.getNetTotal() : null);

            grnReturnDtos.add(dto);
        }

    }

    public void createDirectPurchaseTable() {

        // //System.err.println("Getting GRNS : ");
        String sql = "Select b From BillItem b where type(b.bill)=:class and b.bill.creater is not null "
                + " and b.bill.cancelled=false and b.retired=false and b.item=:i "
                + " and b.bill.billType=:btp and b.createdAt between :frm and :to order by b.id desc ";
        HashMap hm = new HashMap();
        hm.put("i", pharmacyItem);
        hm.put("frm", getFromDate());
        hm.put("to", getToDate());
        hm.put("btp", BillType.PharmacyPurchaseBill);
        hm
                .put("class", BilledBill.class
                );
        directPurchase = getBillItemFacade().findByJpql(sql, hm, TemporalType.TIMESTAMP);

    }

    public List<BillItem> getPos() {
        return pos;
    }

    public void createPoTable() {

        // //System.err.println("Getting POS : ");
        String sql = "Select b From BillItem b where type(b.bill)=:class and b.bill.creater is not null "
                + " and b.bill.cancelled=false and b.retired=false and b.item=:i "
                + " and b.bill.billTypeAtomic=:bta and b.createdAt between :frm and :to order by b.id desc";
        HashMap hm = new HashMap();
        hm.put("i", pharmacyItem);
        hm.put("bta", BillTypeAtomic.PHARMACY_ORDER_APPROVAL);
        hm.put("frm", getFromDate());
        hm.put("to", getToDate());
        hm
                .put("class", BilledBill.class
                );
        pos = getBillItemFacade().findByJpql(sql, hm, TemporalType.TIMESTAMP);

        for (BillItem t : pos) {
            //   t.setPharmaceuticalBillItem(getPoQty(t));
            t.setTotalGrnQty(getGrnQty(t));
        }

    }

    public void createPendingGrnTable() {
        List<Item> relatedItems = pharmacyService.findRelatedItems(pharmacyItem);
        if (relatedItems == null || relatedItems.isEmpty()) {
            pendingGrns = new ArrayList<>();
            return;
        }

        String jpql = "SELECT b FROM BillItem b "
                + "WHERE type(b.bill)=:class "
                + "AND b.bill.creater IS NOT NULL "
                + "AND b.bill.cancelled=false "
                + "AND b.retired=false "
                + "AND b.item IN :relatedItems "
                + "AND b.bill.billTypeAtomic=:bta "
                + "AND (b.bill.referenceBill IS NULL "
                + "     OR b.bill.referenceBill.billTypeAtomic NOT IN :refBtas) "
                + "AND b.createdAt BETWEEN :frm AND :to "
                + "ORDER BY b.id DESC";

        Map<String, Object> params = new HashMap<>();
        params.put("class", BilledBill.class);
        params.put("relatedItems", relatedItems);
        params.put("bta", BillTypeAtomic.PHARMACY_ORDER_APPROVAL);
        params.put("refBtas", Arrays.asList(BillTypeAtomic.PHARMACY_GRN_PRE, BillTypeAtomic.PHARMACY_GRN));
        params.put("frm", getFromDate());
        params.put("to", getToDate());

        pendingGrns = getBillItemFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    private double getGrnQty(BillItem b) {
        String sql = "Select sum(b.pharmaceuticalBillItem.qty) From BillItem b where b.retired=false and b.creater is not null"
                + " and b.bill.cancelled=false and b.bill.billType=:btp and "
                + " b.referanceBillItem=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyGrnBill);
        double value = getBillFacade().findDoubleByJpql(sql, hm);
        return value;
    }

    public void generateGrnReport() {
        resetFields();

        List<BillTypeAtomic> bta = new ArrayList<>();

        bta.add(BillTypeAtomic.PHARMACY_GRN);
        bta.add(BillTypeAtomic.PHARMACY_GRN_RETURN);
        bta.add(BillTypeAtomic.PHARMACY_GRN_CANCELLED);

        bills = new ArrayList<>();

        String sql = "SELECT b FROM Bill b "
                + " WHERE b.retired = false"
                + " and b.billTypeAtomic In :btas"
                + " and b.createdAt between :fromDate and :toDate";

        Map<String, Object> tmp = new HashMap<>();

        tmp.put("btas", bta);
        tmp.put("fromDate", getFromDate());
        tmp.put("toDate", getToDate());

        if (institution != null) {
            sql += " and b.institution = :fIns";
            tmp.put("fIns", institution);
        }

        if (site != null) {
            sql += " and b.department.site = :site";
            tmp.put("site", site);
        }

        if (dept != null) {
            sql += " and b.department = :dept";
            tmp.put("dept", dept);
        }

        if (paymentMethod != null) {
            sql += " and b.paymentMethod = :pm";
            tmp.put("pm", paymentMethod);
        }

        if (fromInstitution != null) {
            sql += " AND b.fromInstitution = :supplier";
            tmp.put("supplier", fromInstitution);
        }

        if (amp != null) {
            item = amp;
            sql += " AND EXISTS (SELECT bi FROM b.billItems bi WHERE bi.item = :itm)";
            tmp.put("itm", item);
        }

        if (category != null) {
            sql += " AND EXISTS (SELECT bi FROM b.billItems bi WHERE bi.item.category = :category)";
            tmp.put("category", category);
        }

        if (toDepartment != null) {
            sql += " AND b.toDepartment = :toDepartment";
            tmp.put("toDepartment", toDepartment);
        }

        sql += " order by b.id desc";

        try {
            bills = getBillFacade().findByJpql(sql, tmp, TemporalType.TIMESTAMP);
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, " Something Went Worng!");
        }
    }

    public Double calculateTotalGrnAmount() {
        double total = 0.0;

        for (Bill b : bills) {
            total += b.getNetTotal();
        }
        return total;
    }

    public Double calculateTotalPOAmount() {
        double total = 0.0;

        for (Bill b : bills) {
            if (b.getBillTypeAtomic().equals(BillTypeAtomic.PHARMACY_GRN_CANCELLED) || b.getBillTypeAtomic().equals(BillTypeAtomic.PHARMACY_GRN_RETURN)) {
                total -= b.getNetTotal();
            } else {
                total += b.getReferenceBill().getNetTotal();
            }
        }
        return total;
    }

    public void exportGRNDetailReportToExcel() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=GRN_Report.xlsx");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy hh:mm:ss a");

        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("GRN Report");
            int rowIndex = 0;

            Row headerRow = sheet.createRow(rowIndex++);
            headerRow.createCell(0).setCellValue("S. No");
            headerRow.createCell(1).setCellValue("GRN No");
            headerRow.createCell(2).setCellValue("PO No");
            headerRow.createCell(3).setCellValue("Invoice No");
            headerRow.createCell(4).setCellValue("Store Name");
            headerRow.createCell(5).setCellValue("Item Category");
            headerRow.createCell(6).setCellValue("Code");
            headerRow.createCell(7).setCellValue("Item");
            headerRow.createCell(8).setCellValue("Receiving Time");
            headerRow.createCell(9).setCellValue("Qty");
            headerRow.createCell(10).setCellValue("UOM");
            headerRow.createCell(11).setCellValue("Rate");
            headerRow.createCell(12).setCellValue("Batch");
            headerRow.createCell(13).setCellValue("Expiry Date");
            headerRow.createCell(14).setCellValue("Supplier");
            headerRow.createCell(15).setCellValue("MRP");
            headerRow.createCell(16).setCellValue("Discount");
            headerRow.createCell(17).setCellValue("Amount");
            headerRow.createCell(18).setCellValue("Total Amount");
            headerRow.createCell(19).setCellValue("PO Sub Total");
            headerRow.createCell(20).setCellValue("GRN Sub Total");

            int count = 0;

            for (Bill bill : bills) {
                Row emptyRow = sheet.createRow(rowIndex++);
                emptyRow.createCell(0).setCellValue("-");
                emptyRow.createCell(1).setCellValue(bill.getDeptId());
                emptyRow.createCell(2).setCellValue(bill.getReferenceBill().getDeptId());
                emptyRow.createCell(3).setCellValue(
                        bill.getInvoiceNumber() != null ? bill.getInvoiceNumber()
                                : (bill.getReferenceBill() != null && bill.getReferenceBill().getInvoiceNumber() != null
                                ? bill.getReferenceBill().getInvoiceNumber() : "-"));
                emptyRow.createCell(4).setCellValue("-");
                emptyRow.createCell(5).setCellValue("-");
                emptyRow.createCell(6).setCellValue("-");
                emptyRow.createCell(7).setCellValue("-");
                emptyRow.createCell(8).setCellValue(sdf.format(bill.getReferenceBill().getCreatedAt()));
                emptyRow.createCell(9).setCellValue("-");
                emptyRow.createCell(10).setCellValue("-");
                emptyRow.createCell(11).setCellValue("-");
                emptyRow.createCell(12).setCellValue("-");
                emptyRow.createCell(13).setCellValue("-");
                emptyRow.createCell(14).setCellValue(
                        bill.getBillTypeAtomic() != null && bill.getBillTypeAtomic().equals(BillTypeAtomic.PHARMACY_GRN_RETURN)
                                ? (bill.getToInstitution() != null && bill.getToInstitution().getName() != null
                                ? bill.getToInstitution().getName() : "-")
                                : (bill.getFromInstitution() != null && bill.getFromInstitution().getName() != null
                                ? bill.getFromInstitution().getName() : "-"));
                emptyRow.createCell(15).setCellValue("-");
                emptyRow.createCell(16).setCellValue("-");
                emptyRow.createCell(17).setCellValue("-");
                emptyRow.createCell(18).setCellValue("-");
                emptyRow.createCell(19).setCellValue(bill.getBillTypeAtomic().equals(BillTypeAtomic.PHARMACY_GRN_CANCELLED)
                        || bill.getBillTypeAtomic().equals(BillTypeAtomic.PHARMACY_GRN_RETURN)
                        ? -1 * bill.getReferenceBill().getNetTotal() : bill.getReferenceBill().getNetTotal());
                emptyRow.createCell(20).setCellValue(bill.getNetTotal());

                for (BillItem billItem : bill.getBillItems()) {
                    Row emptyInnerRow = sheet.createRow(rowIndex++);
                    count++;
                    emptyInnerRow.createCell(0).setCellValue(count);
                    emptyInnerRow.createCell(1).setCellValue("-");
                    emptyInnerRow.createCell(2).setCellValue("-");
                    emptyInnerRow.createCell(3).setCellValue("-");
                    emptyInnerRow.createCell(4).setCellValue(
                            (billItem.getBill() != null && billItem.getBill().getToDepartment() != null && billItem.getBill().getToDepartment().getName() != null)
                                    ? billItem.getBill().getToDepartment().getName()
                                    : (billItem.getBill() != null && billItem.getBill().getReferenceBill() != null
                                    && billItem.getBill().getReferenceBill().getToDepartment() != null
                                    && billItem.getBill().getReferenceBill().getToDepartment().getName() != null)
                                    ? billItem.getBill().getReferenceBill().getToDepartment().getName()
                                    : "-");
                    emptyInnerRow.createCell(5).setCellValue(billItem.getItem().getCategory().getName());
                    emptyInnerRow.createCell(6).setCellValue(billItem.getItem().getCode());
                    emptyInnerRow.createCell(7).setCellValue(billItem.getItem().getName());
                    emptyInnerRow.createCell(8).setCellValue("-");
                    emptyInnerRow.createCell(9).setCellValue(billItem.getQty());
                    emptyInnerRow.createCell(10).setCellValue(
                            (billItem.getItem() != null && billItem.getItem().getMeasurementUnit() != null
                                    && billItem.getItem().getMeasurementUnit().getName() != null)
                                    ? billItem.getItem().getMeasurementUnit().getName() : "-");
                    emptyInnerRow.createCell(11).setCellValue(billItem.getPharmaceuticalBillItem().getPurchaseRate());
                    emptyInnerRow.createCell(12).setCellValue(billItem.getPharmaceuticalBillItem().getItemBatch().getBatchNo());
                    emptyInnerRow.createCell(13).setCellValue(sdf.format(billItem.getPharmaceuticalBillItem().getItemBatch().getDateOfExpire()));
                    emptyInnerRow.createCell(14).setCellValue("-");
                    emptyInnerRow.createCell(15).setCellValue(billItem.getPharmaceuticalBillItem().getRetailRate());
                    emptyInnerRow.createCell(16).setCellValue(billItem.getDiscount());
                    emptyInnerRow.createCell(17).setCellValue(billItem.getNetValue());
                    emptyInnerRow.createCell(18).setCellValue(billItem.getBill().getNetTotal());
                    emptyInnerRow.createCell(19).setCellValue("-");
                    emptyInnerRow.createCell(20).setCellValue("-");
                }
            }

            Row footerRow = sheet.createRow(rowIndex++);
            footerRow.createCell(19).setCellValue(Math.round(calculateTotalPOAmount() * 100.0) / 100.0);
            footerRow.createCell(20).setCellValue(Math.round(calculateTotalGrnAmount() * 100.0) / 100.0);

            workbook.write(out);
            context.responseComplete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getLastPurchaseRate(Item item) {
        return pharmacyBean.getLastPurchaseRate(item, getSessionController().getDepartment());
    }

    public double getLastRetailRate(Item item) {
        return pharmacyBean.getLastRetailRate(item, getSessionController().getDepartment());
    }

    public void exportGRNDetailReportToPDF() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=GRN_Report.pdf");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy hh:mm:ss a");

        try (OutputStream out = response.getOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("GRN Detail Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph("Date: " + sdf.format(new Date()), FontFactory.getFont(FontFactory.HELVETICA, 12)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(21);
            table.setWidthPercentage(100);
            float[] columnWidths = {2f, 3f, 3f, 3f, 4f, 3f, 3f, 4f, 4f, 3f, 3f, 3f, 3f, 3f, 4f, 3f, 3f, 3f, 4f, 4f, 4f};
            table.setWidths(columnWidths);

            String[] headers = {"S. No", "GRN No", "PO No", "Invoice No", "Store Name", "Item Category", "Code", "Item", "Receiving Time", "Qty", "UOM", "Rate", "Batch", "Expiry Date", "Supplier", "MRP", "Discount", "Amount", "Total Amount", "PO Sub Total", "GRN Sub Total"};

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            int count = 0;
            for (Bill bill : bills) {
                table.addCell("-");
                table.addCell(bill.getDeptId());
                table.addCell(bill.getReferenceBill() != null ? bill.getReferenceBill().getDeptId() : "-");
                table.addCell(bill.getInvoiceNumber() != null ? bill.getInvoiceNumber() : (bill.getReferenceBill() != null ? bill.getReferenceBill().getInvoiceNumber() : "-"));
                table.addCell("-");
                table.addCell("-");
                table.addCell("-");
                table.addCell("-");
                table.addCell(sdf.format(bill.getReferenceBill().getCreatedAt()));
                table.addCell("-");
                table.addCell("-");
                table.addCell("-");
                table.addCell("-");
                table.addCell("-");
                table.addCell(bill.getBillTypeAtomic() != null && bill.getBillTypeAtomic().equals(BillTypeAtomic.PHARMACY_GRN_RETURN)
                        ? (bill.getToInstitution() != null ? bill.getToInstitution().getName() : "-")
                        : (bill.getFromInstitution() != null ? bill.getFromInstitution().getName() : "-"));
                table.addCell("-");
                table.addCell("-");
                table.addCell("-");
                table.addCell("-");
                table.addCell(String.format("%.2f", bill.getBillTypeAtomic().equals(BillTypeAtomic.PHARMACY_GRN_CANCELLED)
                        || bill.getBillTypeAtomic().equals(BillTypeAtomic.PHARMACY_GRN_RETURN)
                        ? -1 * bill.getReferenceBill().getNetTotal() : bill.getReferenceBill().getNetTotal()));
                table.addCell(String.valueOf(bill.getNetTotal()));

                for (BillItem billItem : bill.getBillItems()) {
                    count++;
                    table.addCell(String.valueOf(count));
                    table.addCell("-");
                    table.addCell("-");
                    table.addCell("-");
                    table.addCell(billItem.getBill() != null && billItem.getBill().getToDepartment() != null ? billItem.getBill().getToDepartment().getName() : "-");
                    table.addCell(billItem.getItem().getCategory().getName());
                    table.addCell(billItem.getItem().getCode());
                    table.addCell(billItem.getItem().getName());
                    table.addCell("-");
                    table.addCell(String.valueOf(billItem.getQty()));
                    table.addCell(billItem.getItem().getMeasurementUnit() != null ? billItem.getItem().getMeasurementUnit().getName() : "-");
                    table.addCell(String.valueOf(billItem.getPharmaceuticalBillItem().getPurchaseRate()));
                    table.addCell(billItem.getPharmaceuticalBillItem().getItemBatch().getBatchNo());
                    table.addCell(sdf.format(billItem.getPharmaceuticalBillItem().getItemBatch().getDateOfExpire()));
                    table.addCell("-");
                    table.addCell(String.valueOf(billItem.getPharmaceuticalBillItem().getRetailRate()));
                    table.addCell(String.format("%.2f", billItem.getDiscount()));
                    table.addCell(String.format("%.2f", billItem.getNetValue()));
                    table.addCell(String.format("%.2f", billItem.getBill().getNetTotal()));
                    table.addCell("-");
                    table.addCell("-");
                }
            }

            document.add(table);
            document.add(new Paragraph(" "));

            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{1f, 1f});
            footerTable.addCell(new PdfPCell(new Phrase("Total PO Amount: " + String.format("%.2f", calculateTotalPOAmount()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
            footerTable.addCell(new PdfPCell(new Phrase("Total GRN Amount: " + String.format("%.2f", calculateTotalGrnAmount()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
            document.add(footerTable);

            document.close();
            context.responseComplete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public DepartmentFacade getDepartmentFacade() {
        return departmentFacade;
    }

    public void setDepartmentFacade(DepartmentFacade departmentFacade) {
        this.departmentFacade = departmentFacade;
    }

    public Item getPharmacyItem() {
        return pharmacyItem;
    }

    public void setPharmacyItem(Item pharmacyItem) {
        makeNull();
        grns = new ArrayList<>();
        pendingGrns = new ArrayList<>();
        this.pharmacyItem = pharmacyItem;
        fillDetails();
    }

    public void fillItemDetails(Item pharmacyItem) {
        this.pharmacyItem = pharmacyItem;
        fillDetails();
    }

    public double findPharmacyMovement(Department department, Item itm, BillType[] bts, Date fd, Date td) {
        try {
            if (itm instanceof Ampp) {
                itm = ((Ampp) pharmacyItem).getAmp();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sql;
        Map m = new HashMap();
        m.put("itm", itm);
        m.put("dep", department);
        m.put("frm", fd);
        m.put("to", td);
        List<BillType> bts1 = Arrays.asList(bts);
        m.put("bts", bts1);
        sql = "select sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i "
                + " where i.bill.department=:dep"
                + " and i.item=:itm "
                + " and i.bill.billType in :bts "
                + " and i.createdAt between :frm and :to  ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);
    }

    public Date findFirstPharmacyMovementDate(Department department, Item itm, BillType[] bts, Date fd, Date td) {
        try {
            if (itm instanceof Ampp) {
                itm = ((Ampp) pharmacyItem).getAmp();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sql;
        Map m = new HashMap();
        m.put("itm", itm);
        m.put("dep", department);
        m.put("frm", fd);
        m.put("to", td);
        List<BillType> bts1 = Arrays.asList(bts);
        m.put("bts", bts1);
        sql = "select i "
                + " from BillItem i "
                + " where i.bill.department=:dep"
                + " and i.item=:itm "
                + " and i.bill.billType in :bts "
                + " and i.bill.createdAt between :frm and :to  "
                + " order by i.id";
        BillItem d = getBillItemFacade().findFirstByJpql(sql, m, TemporalType.TIMESTAMP);
        if (d == null) {
            return fd;
        } else if (d.getBill() != null && d.getBill().getCreatedAt() != null) {
            return d.getBill().getCreatedAt();
        } else if (d.getCreatedAt() != null) {
            return d.getCreatedAt();
        } else {
            return fd;
        }
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public StockFacade getStockFacade() {
        return stockFacade;
    }

    public void setStockFacade(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonFunctions.getStartOfMonth();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        makeNull();
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = CommonFunctions.getEndOfDay(new Date());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        makeNull();
        this.toDate = toDate;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public AmpFacade getAmpFacade() {
        return ampFacade;
    }

    public void setAmpFacade(AmpFacade ampFacade) {
        this.ampFacade = ampFacade;
    }

    public List<DepartmentSale> getDepartmentSale() {
        return departmentSale;
    }

    public void setDepartmentSale(List<DepartmentSale> departmentSale) {
        this.departmentSale = departmentSale;
    }

    public InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

    public void setInstitutionFacade(InstitutionFacade institutionFacade) {
        this.institutionFacade = institutionFacade;
    }

    public List<BillItem> getDirectPurchase() {
        return directPurchase;
    }

    public void setDirectPurchase(List<BillItem> directPurchase) {
        this.directPurchase = directPurchase;
    }

    public List<InstitutionStock> getInstitutionStocks() {
        return institutionStocks;
    }

    public void setInstitutionStocks(List<InstitutionStock> institutionStocks) {
        this.institutionStocks = institutionStocks;
    }

    public List<InstitutionSale> getInstitutionSales() {
        return institutionSales;
    }

    public void setInstitutionSales(List<InstitutionSale> institutionSales) {
        this.institutionSales = institutionSales;
    }

    public List<InstitutionSale> getInstitutionTransferIssue() {
        return institutionTransferIssue;
    }

    public void setInstitutionTransferIssue(List<InstitutionSale> institutionTransferIssue) {
        this.institutionTransferIssue = institutionTransferIssue;
    }

    public void setGrns(List<BillItem> grns) {
        this.grns = grns;
    }

    public void setPendingGrns(List<BillItem> pendingGrns) {
        this.pendingGrns = pendingGrns;
    }

    public void setPos(List<BillItem> pos) {
        this.pos = pos;
    }

    public void setGrantStock(double grantStock) {
        this.grantStock = grantStock;
    }

    public double getGrantSaleQty() {
        return grantSaleQty;
    }

    public void setGrantSaleQty(double grantSaleQty) {
        this.grantSaleQty = grantSaleQty;
    }

    public double getGrantSaleValue() {
        return grantSaleValue;
    }

    public void setGrantSaleValue(double grantSaleValue) {
        this.grantSaleValue = grantSaleValue;
    }

    public double getGrantTransferIssueQty() {
        return grantTransferIssueQty;
    }

    public void setGrantTransferIssueQty(double grantTransferIssueQty) {
        this.grantTransferIssueQty = grantTransferIssueQty;
    }

    public double getGrantTransferIssueValue() {
        return grantTransferIssueValue;
    }

    public void setGrantTransferIssueValue(double grantTransferIssueValue) {
        this.grantTransferIssueValue = grantTransferIssueValue;
    }

    public List<InstitutionSale> getInstitutionTransferReceive() {
        return institutionTransferReceive;
    }

    public void setInstitutionTransferReceive(List<InstitutionSale> institutionTransferReceive) {
        this.institutionTransferReceive = institutionTransferReceive;
    }

    public double getGrantTransferReceiveQty() {
        return grantTransferReceiveQty;
    }

    public void setGrantTransferReceiveQty(double grantTransferReceiveQty) {
        this.grantTransferReceiveQty = grantTransferReceiveQty;
    }

    public double getGrantTransferReceiveValue() {
        return grantTransferReceiveValue;
    }

    public void setGrantTransferReceiveValue(double grantTransferReceiveValue) {
        this.grantTransferReceiveValue = grantTransferReceiveValue;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public List<StockAverage> getStockAverages() {
        return stockAverages;
    }

    public void setStockAverages(List<StockAverage> stockAverages) {
        this.stockAverages = stockAverages;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public void setItemController(ItemController itemController) {
        this.itemController = itemController;
    }

    public double getPersentage() {
        return persentage;
    }

    public void setPersentage(double persentage) {
        this.persentage = persentage;
    }

    public Department getDepartment() {
        if (department == null) {
            department = getSessionController().getDepartment();
        }
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<ItemTransactionSummeryRow> getItemTransactionSummeryRows() {
        return itemTransactionSummeryRows;
    }

    public void setItemTransactionSummeryRows(List<ItemTransactionSummeryRow> itemTransactionSummeryRows) {
        this.itemTransactionSummeryRows = itemTransactionSummeryRows;
    }

    public boolean isHasSale() {
        return hasSale;
    }

    public void setHasSale(boolean hasSale) {
        this.hasSale = hasSale;
    }

    public boolean isHasWholesale() {
        return hasWholesale;
    }

    public void setHasWholesale(boolean hasWholesale) {
        this.hasWholesale = hasWholesale;
    }

    public boolean isHasInward() {
        return hasInward;
    }

    public void setHasInward(boolean hasInward) {
        this.hasInward = hasInward;
    }

    public boolean isHasIssue() {
        return hasIssue;
    }

    public void setHasIssue(boolean hasIssue) {
        this.hasIssue = hasIssue;
    }

    public boolean isHasTransferOut() {
        return hasTransferOut;
    }

    public void setHasTransferOut(boolean hasTransferOut) {
        this.hasTransferOut = hasTransferOut;
    }

    public boolean isHasPurchase() {
        return hasPurchase;
    }

    public void setHasPurchase(boolean hasPurchase) {
        this.hasPurchase = hasPurchase;
    }

    public boolean isHasTransferIn() {
        return hasTransferIn;
    }

    public void setHasTransferIn(boolean hasTransferIn) {
        this.hasTransferIn = hasTransferIn;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getPharmacyAdminIndex() {
        return pharmacyAdminIndex;
    }

    public void setPharmacyAdminIndex(int pharmacyAdminIndex) {
        this.pharmacyAdminIndex = pharmacyAdminIndex;
    }

    public int getPharmacySummaryIndex() {
        return pharmacySummaryIndex;
    }

    public void setPharmacySummaryIndex(int pharmacySummaryIndex) {
        this.pharmacySummaryIndex = pharmacySummaryIndex;
    }

    public int getManagePharamcyReportIndex() {
        return managePharamcyReportIndex;
    }

    public void setManagePharamcyReportIndex(int managePharamcyReportIndex) {
        this.managePharamcyReportIndex = managePharamcyReportIndex;
    }

    public PharmaceuticalItemLight getSelectedLight() {
        return selectedLight;
    }

    public void setSelectedLight(PharmaceuticalItemLight selectedLight) {
        this.selectedLight = selectedLight;
    }

    public List<PharmaceuticalItemLight> getSelectedLights() {
        return selectedLights;
    }

    public void setSelectedLights(List<PharmaceuticalItemLight> selectedLights) {
        this.selectedLights = selectedLights;
    }

    public List<PharmaceuticalItemLight> getAllLights() {
        return allLights;
    }

    public void setAllLights(List<PharmaceuticalItemLight> allLights) {
        this.allLights = allLights;
    }

    public AmpController getAmpController() {
        return ampController;
    }

    public void setAmpController(AmpController ampController) {
        this.ampController = ampController;
    }

    public List<Atm> getAtms() {
        return atms;
    }

    public void setAtms(List<Atm> atms) {
        this.atms = atms;
    }

    public List<Vtm> getVtms() {
        return vtms;
    }

    public void setVtms(List<Vtm> vtms) {
        this.vtms = vtms;
    }

    public List<Vmp> getVmps() {
        return vmps;
    }

    public void setVmps(List<Vmp> vmps) {
        this.vmps = vmps;
    }

    public List<Amp> getAmps() {
        return amps;
    }

    public void setAmps(List<Amp> amps) {
        this.amps = amps;
    }

    public List<Vmpp> getVmpps() {
        return vmpps;
    }

    public void setVmpps(List<Vmpp> vmpps) {
        this.vmpps = vmpps;
    }

    public List<Atm> getAtmsSelected() {
        return atmsSelected;
    }

    public void setAtmsSelected(List<Atm> atmsSelected) {
        this.atmsSelected = atmsSelected;
    }

    public List<Vtm> getVtmsSelected() {
        return vtmsSelected;
    }

    public void setVtmsSelected(List<Vtm> vtmsSelected) {
        this.vtmsSelected = vtmsSelected;
    }

    public List<Vmp> getVmpsSelected() {
        return vmpsSelected;
    }

    public void setVmpsSelected(List<Vmp> vmpsSelected) {
        this.vmpsSelected = vmpsSelected;
    }

    public List<Amp> getAmpsSelected() {
        return ampsSelected;
    }

    public void setAmpsSelected(List<Amp> ampsSelected) {
        this.ampsSelected = ampsSelected;
    }

    public List<Vmpp> getVmppsSelected() {
        return vmppsSelected;
    }

    public void setVmppsSelected(List<Vmpp> vmppsSelected) {
        this.vmppsSelected = vmppsSelected;
    }

    public List<Ampp> getAmppsSelected() {
        return amppsSelected;
    }

    public void setAmppsSelected(List<Ampp> amppsSelected) {
        this.amppsSelected = amppsSelected;
    }

    public Atm getAtm() {
        return atm;
    }

    public void setAtm(Atm atm) {
        this.atm = atm;
    }

    public Vtm getVtm() {
        return vtm;
    }

    public void setVtm(Vtm vtm) {
        this.vtm = vtm;
    }

    public Vmp getVmp() {
        return vmp;
    }

    public void setVmp(Vmp vmp) {
        this.vmp = vmp;
    }

    public Amp getAmp() {
        return amp;
    }

    public void setAmp(Amp amp) {
        this.amp = amp;
    }

    public Vmpp getVmpp() {
        return vmpp;
    }

    public void setVmpp(Vmpp vmpp) {
        this.vmpp = vmpp;
    }

    public Ampp getAmpp() {
        return ampp;
    }

    public void setAmpp(Ampp ampp) {
        this.ampp = ampp;
    }

    public List<Ampp> getAmpps() {
        return ampps;
    }

    public void setAmpps(List<Ampp> ampps) {
        this.ampps = ampps;
    }

    public MeasurementUnit getIssueUnit() {
        return issueUnit;
    }

    public void setIssueUnit(MeasurementUnit issueUnit) {
        this.issueUnit = issueUnit;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public Institution getSite() {
        return site;
    }

    public void setSite(Institution site) {
        this.site = site;
    }

    public Institution getToInstitution() {
        return toInstitution;
    }

    public void setToInstitution(Institution toInstitution) {
        this.toInstitution = toInstitution;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Department getDept() {
        return dept;
    }

    public void setDept(Department dept) {
        this.dept = dept;
    }

    public Institution getFromInstitution() {
        return fromInstitution;
    }

    public void setFromInstitution(Institution fromInstitution) {
        this.fromInstitution = fromInstitution;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public double getTotalCreditPurchaseValue() {
        return totalCreditPurchaseValue;
    }

    public void setTotalCreditPurchaseValue(double totalCreditPurchaseValue) {
        this.totalCreditPurchaseValue = totalCreditPurchaseValue;
    }

    public double getTotalCashPurchaseValue() {
        return totalCashPurchaseValue;
    }

    public void setTotalCashPurchaseValue(double totalCashPurchaseValue) {
        this.totalCashPurchaseValue = totalCashPurchaseValue;
    }

    public double getTotalCreditCostValue() {
        return totalCreditCostValue;
    }

    public void setTotalCreditCostValue(double totalCreditCostValue) {
        this.totalCreditCostValue = totalCreditCostValue;
    }

    public double getTotalCashCostValue() {
        return totalCashCostValue;
    }

    public void setTotalCashCostValue(double totalCashCostValue) {
        this.totalCashCostValue = totalCashCostValue;
    }

    public double getTotalPurchase() {
        return totalPurchase;
    }

    public void setTotalPurchase(double totalPurchase) {
        this.totalPurchase = totalPurchase;
    }

    public List<String1Value1> getData() {
        return data;
    }

    public void setData(List<String1Value1> data) {
        this.data = data;
    }

    public double getTotalSaleValue() {
        return totalSaleValue;
    }

    public void setTotalSaleValue(double totalSaleValue) {
        this.totalSaleValue = totalSaleValue;
    }

    public double getTotalCreditSaleValue() {
        return totalCreditSaleValue;
    }

    public void setTotalCreditSaleValue(double totalCreditSaleValue) {
        this.totalCreditSaleValue = totalCreditSaleValue;
    }

    public double getTotalCashSaleValue() {
        return totalCashSaleValue;
    }

    public void setTotalCashSaleValue(double totalCashSaleValue) {
        this.totalCashSaleValue = totalCashSaleValue;
    }

    public Department getFromDepartment() {
        return fromDepartment;
    }

    public void setFromDepartment(Department fromDepartment) {
        this.fromDepartment = fromDepartment;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Department getToDepartment() {
        return toDepartment;
    }

    public void setToDepartment(Department toDepartment) {
        this.toDepartment = toDepartment;
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public List<PharmacyRow> getPharmacyRows() {
        return pharmacyRows;
    }

    public void setPharmacyRows(List<PharmacyRow> pharmacyRows) {
        this.pharmacyRows = pharmacyRows;
    }

    public List<PharmacySummery> getDepartmentSummaries() {
        return departmentSummaries;
    }

    public void setDepartmentSummaries(List<PharmacySummery> departmentSummaries) {
        this.departmentSummaries = departmentSummaries;
    }

    public List<CategoryWithItem> getIssueDepartmentCategoryWiseItems() {
        return issueDepartmentCategoryWiseItems;
    }

    public void setIssueDepartmentCategoryWiseItems(List<CategoryWithItem> issueDepartmentCategoryWiseItems) {
        this.issueDepartmentCategoryWiseItems = issueDepartmentCategoryWiseItems;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public Institution getFromSite() {
        return fromSite;
    }

    public void setFromSite(Institution fromSite) {
        this.fromSite = fromSite;
    }

    public Institution getToSite() {
        return toSite;
    }

    public void setToSite(Institution toSite) {
        this.toSite = toSite;
    }

    public List<DepartmentCategoryWiseItems> getResultsList() {
        return resultsList;
    }

    public void setResultsList(List<DepartmentCategoryWiseItems> resultsList) {
        this.resultsList = resultsList;
    }

    public Map<String, List<PharmacyRow>> getDepartmentWiseRows() {
        return departmentWiseRows;
    }

    public void setDepartmentWiseRows(Map<String, List<PharmacyRow>> departmentWiseRows) {
        this.departmentWiseRows = departmentWiseRows;
    }

    public List<DepartmentWiseBill> getDepartmentWiseBillList() {
        return departmentWiseBillList;
    }

    public void setDepartmentWiseBillList(List<DepartmentWiseBill> departmentWiseBillList) {
        this.departmentWiseBillList = departmentWiseBillList;
    }

    public List<Stock> getStockList() {
        return stockList;
    }

    public void setStockList(List<Stock> stockList) {
        this.stockList = stockList;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public List<PharmacySummery> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<PharmacySummery> summaries) {
        this.summaries = summaries;
    }

    public Map<String, Map<String, Double[]>> getDepartmentTotals() {
        return departmentTotals;
    }

    public Map<String, Double[]> getPharmacyTotals() {
        return pharmacyTotals;
    }

    public void setDepartmentTotals(Map<String, Map<String, Double[]>> departmentTotals) {
        this.departmentTotals = departmentTotals;
    }

    public void setPharmacyTotals(Map<String, Double[]> pharmacyTotals) {
        this.pharmacyTotals = pharmacyTotals;
    }

    public Map<String, Map<String, List<DepartmentCategoryWiseItems>>> getDepartmentCategoryMap() {
        return departmentCategoryMap;
    }

    public void setDepartmentCategoryMap(Map<String, Map<String, List<DepartmentCategoryWiseItems>>> departmentCategoryMap) {
        this.departmentCategoryMap = departmentCategoryMap;
    }

    public double getTotalCostValue() {
        return totalCostValue;
    }

    public void setTotalCostValue(double totalCostValue) {
        this.totalCostValue = totalCostValue;
    }

    /**
     * Returns the appropriate PrimeFaces UI message CSS class based on item expiry date
     * @param dateOfExpire the expiry date of the item
     * @return PrimeFaces CSS class for styling
     */
    public String getExpiryStyleClass(Date dateOfExpire) {
        if (dateOfExpire == null) {
            return "ui-messages-success";
        }
        
        // Normalize dates to end-of-day for consistent comparison
        Date currentDateEndOfDay = CommonFunctions.getEndOfDay(new Date());
        Date dateOfExpireEndOfDay = CommonFunctions.getEndOfDay(dateOfExpire);
        Date threeMonthsFromNow = CommonFunctions.getDateAfterThreeMonthsCurrentDateTime();
        
        if (currentDateEndOfDay.after(dateOfExpireEndOfDay)) {
            return "ui-messages-fatal";
        } else if (threeMonthsFromNow.after(dateOfExpireEndOfDay)) {
            return "ui-messages-warn";
        }
        return "ui-messages-success";
    }

    /**
     * Returns the appropriate background style based on item expiry date
     * @param dateOfExpire the expiry date of the item
     * @return CSS background style
     */
    public String getExpiryBackgroundStyle(Date dateOfExpire) {
        if (dateOfExpire == null) {
            return "background-color: #dff0d8";
        }
        
        // Normalize dates to end-of-day for consistent comparison
        Date currentDateEndOfDay = CommonFunctions.getEndOfDay(new Date());
        Date dateOfExpireEndOfDay = CommonFunctions.getEndOfDay(dateOfExpire);
        Date threeMonthsFromNow = CommonFunctions.getDateAfterThreeMonthsCurrentDateTime();
        
        if (currentDateEndOfDay.after(dateOfExpireEndOfDay)) {
            return "background-color: #f2dede";  // Light red for expired
        } else if (threeMonthsFromNow.after(dateOfExpireEndOfDay)) {
            return "background-color: #fcf8e3";  // Light yellow for expiring soon
        }
        return "background-color: #dff0d8";  // Light green for good expiry
    }
    
    /**
     * Creates stock variance report from stock taking adjustments
     * @param fromDate start date for the report
     * @param toDate end date for the report
     * @param department specific department (optional)
     * @return list of stock variance data
     */
    public List<Map<String, Object>> createStockVarianceReport(Date fromDate, Date toDate, Department department) {
        List<Map<String, Object>> varianceData = new ArrayList<>();
        
        try {
            String jpql = "SELECT bi FROM BillItem bi "
                    + "WHERE bi.bill.billType = :billType "
                    + "AND bi.bill.createdAt BETWEEN :fromDate AND :toDate "
                    + "AND bi.bill.retired = false ";
            
            Map<String, Object> params = new HashMap<>();
            params.put("billType", BillType.PharmacyStockAdjustmentBill);
            params.put("fromDate", fromDate);
            params.put("toDate", toDate);
            
            if (department != null) {
                jpql += " AND bi.bill.department = :dept";
                params.put("dept", department);
            }
            
            List<BillItem> adjustmentItems = billItemFacade.findByJpql(jpql, params);
            
            for (BillItem bi : adjustmentItems) {
                if (bi.getPharmaceuticalBillItem() != null && bi.getPharmaceuticalBillItem().getItemBatch() != null) {
                    Map<String, Object> variance = new HashMap<>();
                    variance.put("itemName", bi.getItem().getName());
                    variance.put("batchNo", bi.getPharmaceuticalBillItem().getItemBatch().getBatchNo());
                    variance.put("expiryDate", bi.getPharmaceuticalBillItem().getItemBatch().getDateOfExpire());
                    variance.put("adjustmentQty", bi.getQty());
                    variance.put("adjustmentDate", bi.getBill().getCreatedAt());
                    variance.put("adjustmentBy", bi.getBill().getCreater() != null ? bi.getBill().getCreater().getName() : "");
                    variance.put("department", bi.getBill().getDepartment().getName());
                    varianceData.add(variance);
                }
            }
            
        } catch (Exception e) {
            // Handle exception appropriately
            System.err.println("Error creating stock variance report: " + e.getMessage());
        }
        
        return varianceData;
    }
    
    /**
     * Gets stock variance report data
     * @param fromDate start date
     * @param toDate end date
     * @param department department filter
     * @return variance report data
     */
    public List<Map<String, Object>> getStockVarianceReport(Date fromDate, Date toDate, Department department) {
        return createStockVarianceReport(fromDate, toDate, department);
    }
}
