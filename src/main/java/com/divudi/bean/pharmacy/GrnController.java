/*
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.common.SessionController;
import com.divudi.core.util.JsfUtil;
import com.divudi.core.data.BillClassType;
import com.divudi.core.data.BillNumberSuffix;
import com.divudi.core.data.BillType;
import com.divudi.core.data.BillTypeAtomic;
import com.divudi.core.data.PaymentMethod;
import com.divudi.core.data.dataStructure.SearchKeyword;
import com.divudi.ejb.BillNumberGenerator;

import com.divudi.ejb.PharmacyBean;
import com.divudi.ejb.PharmacyCalculation;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillFee;
import com.divudi.core.entity.BillFeePayment;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.BilledBill;
import com.divudi.core.entity.Institution;
import com.divudi.core.entity.Payment;
import com.divudi.core.entity.pharmacy.ItemBatch;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.entity.pharmacy.Stock;
import com.divudi.core.facade.AmpFacade;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.BillFeeFacade;
import com.divudi.core.facade.BillFeePaymentFacade;
import com.divudi.core.facade.BillItemFacade;
import com.divudi.core.facade.CategoryFacade;
import com.divudi.core.facade.ItemBatchFacade;
import com.divudi.core.facade.ItemFacade;
import com.divudi.core.facade.PaymentFacade;
import com.divudi.core.facade.PharmaceuticalBillItemFacade;
import com.divudi.core.util.CommonFunctions;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import com.divudi.bean.common.ConfigOptionApplicationController;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class GrnController implements Serializable {

    @Inject
    private SessionController sessionController;
    private BilledBill bill;
    @EJB
    private BillNumberGenerator billNumberBean;
    @EJB
    private BillFacade billFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private ItemFacade itemFacade;
    @EJB
    private CategoryFacade categoryFacade;
    @EJB
    private ItemBatchFacade itemBatchFacade;
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private AmpFacade ampFacade;

    @EJB
    BillFeePaymentFacade billFeePaymentFacade;
    @EJB
    BillFeeFacade billFeeFacade;
    @EJB
    PaymentFacade paymentFacade;
    @Inject
    private PharmacyCalculation pharmacyCalculation;
    @Inject
    ConfigOptionApplicationController configOptionApplicationController;
    /////////////////
    private Institution dealor;
    private Bill approveBill;
    private Bill grnBill;
    private Bill currentGrnBillPre;
    //   private Double cashPaid;
    private Date fromDate;
    private Date toDate;
    private boolean printPreview;
    //////////////
    //private List<PharmacyItemData> pharmacyItems;
    private List<Bill> pos;
    private List<Bill> grns;
    private List<Bill> filteredValue;
    private List<BillItem> billItems;
    private List<BillItem> selectedBillItems;
    private SearchKeyword searchKeyword;
    private List<Bill> bills;
    private double insTotal;
    private double difference;
    private Institution fromInstitution;
    private Institution referenceInstitution;
    private double total;
    private double netTotal;
    private double discount;
    private Date invoiceDate;
    private String invoiceNumber;
    private Bill closeBill;
    private BillItem currentBillItem;
    BillItem currentExpense;
    List<BillItem> billExpenses;

    public void closeSelectedPurchaseOrder() {
        if (closeBill == null) {
            JsfUtil.addErrorMessage("Bill is Not Valid !");
            return;
        }

        closeBill.setBillClosed(true);
        billFacade.edit(closeBill);

    }

    public void openSelectedPurchaseOrder() {
        if (closeBill == null) {
            JsfUtil.addErrorMessage("Bill is Not Valid !");
            return;
        }

        closeBill.setBillClosed(false);
        billFacade.edit(closeBill);

    }

    public double calDifference() {
        difference = Math.abs(insTotal) - Math.abs(getNetTotal());
        return difference;
    }

    public String navigateToResive() {
        clear();
        createGrn();
        getGrnBill().setPaymentMethod(getApproveBill().getPaymentMethod());
        getGrnBill().setCreditDuration(getApproveBill().getCreditDuration());
        return "/pharmacy/pharmacy_grn?faces-redirect=true";
    }

    public String navigateToResiveWithSaveApprove() {
        clear();
        createGrn();
        getCurrentGrnBillPre().setPaymentMethod(getApproveBill().getPaymentMethod());
        getCurrentGrnBillPre().setCreditDuration(getApproveBill().getCreditDuration());
        return "/pharmacy/pharmacy_grn_with_save_approve?faces-redirect=true";
    }

    public String navigateToResiveFromImportGrn(Bill importGrn) {
        clear();
        saveImportBill(importGrn);
        createGrn(importGrn);
        setFromInstitution(importGrn.getFromInstitution());

        getGrnBill().setPaymentMethod(importGrn.getPaymentMethod());
        return "/pharmacy/pharmacy_grn?faces-redirect=true";
    }

    public void clear() {
        billExpenses = null;
        grnBill = null;
        total = 0;
        netTotal = 0;
        discount = 0;
        invoiceDate = null;
        invoiceNumber = null;
        dealor = null;
        pos = null;
        printPreview = false;
        billItems = null;
        difference = 0;
        insTotal = 0;
    }

    public String navigateToRecieveGrnPreBill() {
        clear();
        currentGrnBillPre = null;
        for (Bill b : getApproveBill().getListOfBill()) {
            if (b.getForwardReferenceBill() == null) {
                JsfUtil.addErrorMessage("Please approve the grn bill");
                return "";
            }
        }
        createGrn();
        getCurrentGrnBillPre().setPaymentMethod(getApproveBill().getPaymentMethod());
        return "/pharmacy/pharmacy_grn_with_approval?faces-redirect=true";
    }

    public String navigateToApproveRecieveGrnPreBill() {
        clear();
        billItems = getCurrentGrnBillPre().getBillItems();
        invoiceDate = getCurrentGrnBillPre().getInvoiceDate();
        invoiceNumber = getCurrentGrnBillPre().getInvoiceNumber();
        setFromInstitution(getCurrentGrnBillPre().getFromInstitution());
        setReferenceInstitution(getSessionController().getLoggedUser().getInstitution());
        for (BillItem bi : billItems) {
            bi.setTmpQty(bi.getPharmaceuticalBillItem().getQtyInUnit());
            bi.setTmpFreeQty(bi.getPharmaceuticalBillItem().getFreeQtyInUnit());
        }
        calGrossTotal();
        return "/pharmacy/pharmacy_grn_approval_finalized?faces-redirect=true";
    }

    public String navigateToEditGrn() {
        clear();
        billItems = getCurrentGrnBillPre().getBillItems();
        invoiceDate = getCurrentGrnBillPre().getInvoiceDate();
        invoiceNumber = getCurrentGrnBillPre().getInvoiceNumber();
        setFromInstitution(getCurrentGrnBillPre().getFromInstitution());
        setReferenceInstitution(getSessionController().getLoggedUser().getInstitution());
        
        // Load bill items and calculate totals
        for (BillItem bi : billItems) {
            bi.setTmpQty(bi.getPharmaceuticalBillItem().getQtyInUnit());
            bi.setTmpFreeQty(bi.getPharmaceuticalBillItem().getFreeQtyInUnit());
        }
        
        // Calculate totals from loaded bill data
        calGrossTotal();
        
        // Load existing totals from the bill if available
        if (getCurrentGrnBillPre().getTotal() != 0) {
            total = getCurrentGrnBillPre().getTotal();
        }
        if (getCurrentGrnBillPre().getNetTotal() != 0) {
            netTotal = getCurrentGrnBillPre().getNetTotal();
        }
        if (getCurrentGrnBillPre().getDiscount() != 0) {
            discount = getCurrentGrnBillPre().getDiscount();
        }
        
        // Trigger difference calculation if invoice total exists
        calDifference();
        
        return "/pharmacy/pharmacy_grn_with_save_approve?faces-redirect=true";
    }

    @Deprecated // Please use navigateToResive
    public String navigateToResiveAll() {
        grnBill = null;
        dealor = null;
        pos = null;
        printPreview = false;
        billItems = null;
        createGrnWholesale();
        return "/pharmacy/pharmacy_grn?faces-redirect=true";
    }

    public String navigateToReceiveWholesale() {
        grnBill = null;
        dealor = null;
        pos = null;
        printPreview = false;
        billItems = null;
        difference = 0;
        insTotal = 0;
        createGrnWholesale();
        return "/pharmacy/pharmacy_grn_wh?faces-redirect=true";
    }

    public void removeItem(BillItem bi) {
        getBillItems().remove(bi.getSearialNo());
        calGrossTotal();
    }

    public List<BillItem> findAllBillItemsRefernceToOriginalItem(BillItem referenceBillItem) {
        List<BillItem> tmpBillItems = new ArrayList<>();
        for (BillItem i : getBillItems()) {
            if (i.getReferanceBillItem() == referenceBillItem) {
                tmpBillItems.add(i);
            }
        }
        return tmpBillItems;
    }

    public void duplicateItem(BillItem originalBillItemToDuplicate) {
        BillItem newBillItemCreatedByDuplication = new BillItem();
        double totalQuantityOfBillItemsRefernceToOriginalItem = 0.0;
        double totalFreeQuantityOfBillItemsRefernceToOriginalItem = 0.0;

        double remainFreeQty;
        double remainQty;
        if (originalBillItemToDuplicate != null) {
            PharmaceuticalBillItem newPharmaceuticalBillItemCreatedByDuplication = new PharmaceuticalBillItem();
            newPharmaceuticalBillItemCreatedByDuplication.copy(originalBillItemToDuplicate.getPharmaceuticalBillItem());
            newPharmaceuticalBillItemCreatedByDuplication.setBillItem(newBillItemCreatedByDuplication);
            newBillItemCreatedByDuplication.setItem(originalBillItemToDuplicate.getItem());
            newBillItemCreatedByDuplication.setReferanceBillItem(originalBillItemToDuplicate.getReferanceBillItem());
            newBillItemCreatedByDuplication.setPharmaceuticalBillItem(newPharmaceuticalBillItemCreatedByDuplication);

            List<BillItem> tmpBillItems = findAllBillItemsRefernceToOriginalItem(originalBillItemToDuplicate.getReferanceBillItem());

            for (BillItem bi : tmpBillItems) {
                totalQuantityOfBillItemsRefernceToOriginalItem += bi.getPharmaceuticalBillItem().getQtyInUnit();
                totalFreeQuantityOfBillItemsRefernceToOriginalItem += bi.getPharmaceuticalBillItem().getFreeQtyInUnit();
            }
            remainQty = originalBillItemToDuplicate.getPreviousRecieveQtyInUnit() - totalQuantityOfBillItemsRefernceToOriginalItem;
            remainFreeQty = originalBillItemToDuplicate.getPreviousRecieveFreeQtyInUnit() - totalFreeQuantityOfBillItemsRefernceToOriginalItem;

            newBillItemCreatedByDuplication.getPharmaceuticalBillItem().setQty(remainQty);
            newBillItemCreatedByDuplication.getPharmaceuticalBillItem().setQtyInUnit(remainQty);

            newBillItemCreatedByDuplication.getPharmaceuticalBillItem().setFreeQty(remainFreeQty);
            newBillItemCreatedByDuplication.getPharmaceuticalBillItem().setFreeQtyInUnit(remainFreeQty);

            newBillItemCreatedByDuplication.setTmpQty(remainQty);
            newBillItemCreatedByDuplication.setTmpFreeQty(remainFreeQty);

            newBillItemCreatedByDuplication.setPreviousRecieveQtyInUnit(originalBillItemToDuplicate.getPreviousRecieveQtyInUnit());
            newBillItemCreatedByDuplication.setPreviousRecieveFreeQtyInUnit(originalBillItemToDuplicate.getPreviousRecieveFreeQtyInUnit());
            getBillItems().add(newBillItemCreatedByDuplication);
        }
        calGrossTotal();
    }

    public void removeSelected() {
        //  //System.err.println("1");
        if (selectedBillItems == null) {
            //   //System.err.println("2");
            return;
        }

        //   //System.err.println("3");
        for (BillItem b : selectedBillItems) {
            //  //System.err.println("4");
            getBillItems().remove(b.getSearialNo());
            calGrossTotal();
        }

        selectedBillItems = null;
    }

    public void clearList() {
        //   pharmacyItems = null;
        pos = null;
        filteredValue = null;
        //  billItems = null;
        grns = null;
    }

    public void setBatch(BillItem pid) {
        if (pid.getPharmaceuticalBillItem().getDoe() == null) {
            return;
        }

        if (pid.getPharmaceuticalBillItem().getDoe() != null) {
            if (pid.getPharmaceuticalBillItem().getDoe().getTime() < Calendar.getInstance().getTimeInMillis()) {
                pid.getPharmaceuticalBillItem().setStringValue(null);
                return;
                //    return;
            }
        }

        if (pid.getPharmaceuticalBillItem().getStringValue().trim().isEmpty()) {
            Date date = pid.getPharmaceuticalBillItem().getDoe();
            DateFormat df = new SimpleDateFormat("ddMMyyyy");
            String reportDate = df.format(date);
// Print what date is today!
            //       //System.err.println("Report Date: " + reportDate);
            pid.getPharmaceuticalBillItem().setStringValue(reportDate);
        }
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = CommonFunctions.getEndOfDay(new Date());
        }
        return toDate;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonFunctions.getStartOfDay(new Date());
        }
        return fromDate;
    }

    public void request() {
//        if (Math.abs(difference) > 1) {
//            JsfUtil.addErrorMessage("The invoice does not match..! Check again");
//            return;
//        }
        if (getCurrentGrnBillPre().getFromInstitution() == null) {
            getCurrentGrnBillPre().setFromInstitution(getFromInstitution());
        }
        if (getCurrentGrnBillPre().getReferenceInstitution() == null) {
            getCurrentGrnBillPre().setReferenceInstitution(getReferenceInstitution());
        }
        getCurrentGrnBillPre().setInvoiceDate(invoiceDate);
        getCurrentGrnBillPre().setInvoiceNumber(invoiceNumber);
        getCurrentGrnBillPre().setPaymentMethod(getApproveBill().getPaymentMethod());

        if (getCurrentGrnBillPre().getInvoiceDate() == null) {
            getCurrentGrnBillPre().setInvoiceDate(getApproveBill().getCreatedAt());
        }

        String msg = pharmacyCalculation.errorCheck(getCurrentGrnBillPre(), billItems);
        if (!msg.isEmpty()) {
            JsfUtil.addErrorMessage(msg);
            return;
        }

        saveGrnPreBill();
        if (!getCurrentGrnBillPre().getBillItems().isEmpty()) {
            getCurrentGrnBillPre().setBillItems(null);
        }

//        Payment p = createPayment(getGrnBill(), getGrnBill().getPaymentMethod());
        for (BillItem i : getBillItems()) {
            if (i.getTmpQty() == 0.0 && i.getTmpFreeQty() == 0.0) {
                continue;
            }

            PharmaceuticalBillItem ph = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);

            i.setCreatedAt(new Date());
            i.setCreater(getSessionController().getLoggedUser());
            i.setBill(getCurrentGrnBillPre());
            if (i.getId() == null) {
                getBillItemFacade().create(i);
            } else {
                getBillItemFacade().edit(i);
            }

            if (ph.getId() == null) {
                getPharmaceuticalBillItemFacade().create(ph);
            } else {
                getPharmaceuticalBillItemFacade().edit(ph);
            }

            i.setPharmaceuticalBillItem(ph);
            getBillItemFacade().edit(i);

//                 updatePoItemQty(i);
//            System.err.println("1 " + i);
            ItemBatch itemBatch = getPharmacyCalculation().saveItemBatch(i);
            // getPharmacyBillBean().preCalForAddToStock(i, itemBatch, getSessionController().getDepartment());

            double addingQty = i.getPharmaceuticalBillItem().getQtyInUnit() + i.getPharmaceuticalBillItem().getFreeQtyInUnit();

            i.getPharmaceuticalBillItem().setItemBatch(itemBatch);
//
//            Stock stock = getPharmacyBean().addToStock(
//                    i.getPharmaceuticalBillItem(),
//                    Math.abs(addingQty),
//                    getSessionController().getDepartment());
//
//            i.getPharmaceuticalBillItem().setStock(stock);
            getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
            getPharmacyCalculation().editBillItem(i.getPharmaceuticalBillItem(), getSessionController().getLoggedUser());
//            saveBillFee(i, p);
            getCurrentGrnBillPre().getBillItems().add(i);
        }
        for (BillItem bi : getCurrentGrnBillPre().getBillItems()) {
        }

        calGrossTotal();

        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(getCurrentGrnBillPre());
//        updateBalanceForGrn(getCurrentGrnBillPre());

        getBillFacade().edit(getCurrentGrnBillPre());

        //  getPharmacyBillBean().editBill(, , getSessionController());
//        printPreview = true;
        JsfUtil.addSuccessMessage("Request Saved");

    }

    public void requestFinalize() {
        if (Math.abs(difference) > 1) {
            JsfUtil.addErrorMessage("The invoice does not match..! Check again");
            return;
        }
        if (getCurrentGrnBillPre().getFromInstitution() == null) {
            getCurrentGrnBillPre().setFromInstitution(getFromInstitution());
        }
        if (getCurrentGrnBillPre().getReferenceInstitution() == null) {
            getCurrentGrnBillPre().setReferenceInstitution(getReferenceInstitution());
        }
        getCurrentGrnBillPre().setInvoiceDate(invoiceDate);
        getCurrentGrnBillPre().setInvoiceNumber(invoiceNumber);
        getCurrentGrnBillPre().setPaymentMethod(getApproveBill().getPaymentMethod());

        String msg = pharmacyCalculation.errorCheck(getCurrentGrnBillPre(), billItems);
        if (!msg.isEmpty()) {
            JsfUtil.addErrorMessage(msg);
            return;
        }
        if (!getCurrentGrnBillPre().getBillItems().isEmpty()) {
            getCurrentGrnBillPre().setBillItems(null);
        }
        finalizeBill();
        if (!getCurrentGrnBillPre().getBillItems().isEmpty()) {
            getCurrentGrnBillPre().setBillItems(null);
        }

//        Payment p = createPayment(getGrnBill(), getGrnBill().getPaymentMethod());
        for (BillItem i : getBillItems()) {
            if (i.getTmpQty() == 0.0 && i.getTmpFreeQty() == 0.0) {
                continue;
            }

            PharmaceuticalBillItem ph = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);

            i.setCreatedAt(new Date());
            i.setCreater(getSessionController().getLoggedUser());
            i.setBill(getCurrentGrnBillPre());
            if (i.getId() == null) {
                getBillItemFacade().create(i);
            } else {
                getBillItemFacade().edit(i);
            }

            if (ph.getId() == null) {
                getPharmaceuticalBillItemFacade().create(ph);
            } else {
                getPharmaceuticalBillItemFacade().edit(ph);
            }

            i.setPharmaceuticalBillItem(ph);
            getBillItemFacade().edit(i);

//                 updatePoItemQty(i);
//            System.err.println("1 " + i);
            ItemBatch itemBatch = getPharmacyCalculation().saveItemBatch(i);
            // getPharmacyBillBean().preCalForAddToStock(i, itemBatch, getSessionController().getDepartment());

            double addingQty = i.getPharmaceuticalBillItem().getQtyInUnit() + i.getPharmaceuticalBillItem().getFreeQtyInUnit();

            i.getPharmaceuticalBillItem().setItemBatch(itemBatch);
//
//            Stock stock = getPharmacyBean().addToStock(
//                    i.getPharmaceuticalBillItem(),
//                    Math.abs(addingQty),
//                    getSessionController().getDepartment());
//
//            i.getPharmaceuticalBillItem().setStock(stock);
            getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
            getPharmacyCalculation().editBillItem(i.getPharmaceuticalBillItem(), getSessionController().getLoggedUser());
//            saveBillFee(i, p);
            getCurrentGrnBillPre().getBillItems().add(i);
        }
        getCurrentGrnBillPre().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), BillType.PharmacyGrnBill, BillClassType.BilledBill, BillNumberSuffix.GRN));
        getCurrentGrnBillPre().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), BillType.PharmacyGrnBill, BillClassType.BilledBill, BillNumberSuffix.GRN));

        getCurrentGrnBillPre().setToInstitution(getApproveBill().getFromInstitution());
        getCurrentGrnBillPre().setToDepartment(getApproveBill().getFromDepartment());

        getCurrentGrnBillPre().setInstitution(getSessionController().getInstitution());
        getCurrentGrnBillPre().setDepartment(getSessionController().getDepartment());

        getCurrentGrnBillPre().setCreater(getSessionController().getLoggedUser());
        getCurrentGrnBillPre().setCreatedAt(Calendar.getInstance().getTime());

        calGrossTotal();

//        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(getCurrentGrnBillPre());
//        updateBalanceForGrn(getCurrentGrnBillPre());
        getBillFacade().edit(getCurrentGrnBillPre());

        //  getPharmacyBillBean().editBill(, , getSessionController());
//        printPreview = true;
        JsfUtil.addSuccessMessage("Request Finalized");

    }

    public void settle() {
        if (Math.abs(difference) > 1) {
            JsfUtil.addErrorMessage("The invoice does not match..! Check again");
            return;
        }
        if (getGrnBill().getFromInstitution() == null) {
            getGrnBill().setFromInstitution(getFromInstitution());
        }
        if (getGrnBill().getReferenceInstitution() == null) {
            getGrnBill().setReferenceInstitution(getReferenceInstitution());
        }

        if (getGrnBill().getPaymentMethod() == null) {
            JsfUtil.addErrorMessage("Please select a payment method");
            return;
        }

//        if (currentGrnBillPre != null) {
//            getGrnBill().setPaymentMethod(getCurrentGrnBillPre().getPaymentMethod());
//        } else {
//            getGrnBill().setPaymentMethod(getApproveBill().getPaymentMethod());
//        }
        getGrnBill().setInvoiceDate(invoiceDate);
        getGrnBill().setInvoiceNumber(invoiceNumber);
        String msg = pharmacyCalculation.errorCheck(getGrnBill(), billItems);
        if (!msg.isEmpty()) {
            JsfUtil.addErrorMessage(msg);
            return;
        }
        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(getGrnBill());
        if (getGrnBill().getInvoiceDate() == null) {
            getGrnBill().setInvoiceDate(getApproveBill().getCreatedAt());
        }

        if (getGrnBill().getReferenceBill() != null) {
            if (getGrnBill().getReferenceBill().getId() == null) {

            }
        }

        saveBill();

        for (BillItem i : getBillItems()) {
            if (i.getTmpQty() == 0.0 && i.getTmpFreeQty() == 0.0) {
                continue;
            }

            if (i.getTmpQty() < 0.0 && i.getTmpFreeQty() < 0.0) {
                continue;
            }

            PharmaceuticalBillItem ph = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);

            i.setCreatedAt(new Date());
            i.setCreater(getSessionController().getLoggedUser());
            i.setBill(getGrnBill());
            if (i.getId() == null) {
                getBillItemFacade().create(i);
            }

            if (ph.getId() == null) {
                getPharmaceuticalBillItemFacade().create(ph);
            }

            i.setPharmaceuticalBillItem(ph);
            getBillItemFacade().edit(i);

            //     updatePoItemQty(i);
            //System.err.println("1 " + i);
            ItemBatch itemBatch = getPharmacyCalculation().saveItemBatch(i);
            // getPharmacyBillBean().preCalForAddToStock(i, itemBatch, getSessionController().getDepartment());

            double addingQty = i.getPharmaceuticalBillItem().getQtyInUnit() + i.getPharmaceuticalBillItem().getFreeQtyInUnit();

            i.getPharmaceuticalBillItem().setItemBatch(itemBatch);

            Stock stock = getPharmacyBean().addToStock(
                    i.getPharmaceuticalBillItem(),
                    Math.abs(addingQty),
                    getSessionController().getDepartment());

            i.getPharmaceuticalBillItem().setStock(stock);

            getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
            getPharmacyCalculation().editBillItem(i.getPharmaceuticalBillItem(), getSessionController().getLoggedUser());
            saveBillFee(i);
            getGrnBill().getBillItems().add(i);
        }

        String deptId = billNumberBean.departmentBillNumberGeneratorYearly(getSessionController().getDepartment(), BillTypeAtomic.PHARMACY_GRN);

        getGrnBill().setDeptId(deptId);
        getGrnBill().setInsId(deptId);

        getGrnBill().setToInstitution(getApproveBill().getFromInstitution());
        getGrnBill().setToDepartment(getApproveBill().getFromDepartment());

        getGrnBill().setInstitution(getSessionController().getInstitution());
        getGrnBill().setDepartment(getSessionController().getDepartment());

        getGrnBill().setCreater(getSessionController().getLoggedUser());
        getGrnBill().setCreatedAt(Calendar.getInstance().getTime());

        getGrnBill().setBillExpenses(billExpenses);
        getGrnBill().setExpenseTotal(calExpenses());
        calGrossTotal();
        getGrnBill().setNetTotal(getGrnBill().getNetTotal() - calExpenses());

        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(getGrnBill());
        updateBalanceForGrn(getGrnBill());

        getBillFacade().edit(getGrnBill());

        Payment p = createPayment(getGrnBill(), getGrnBill().getPaymentMethod());

        //  getPharmacyBillBean().editBill(, , getSessionController());
        
        // Check if Purchase Order is fully received and update fullyIssued status
        if (getApproveBill() != null && !getApproveBill().isFullyIssued()) {
            if (isPurchaseOrderFullyReceived(getApproveBill())) {
                getApproveBill().setFullyIssued(true);
                getApproveBill().setFullyIssuedAt(new Date());
                getApproveBill().setFullyIssuedBy(getSessionController().getLoggedUser());
                getBillFacade().edit(getApproveBill());
            }
        }
        
        printPreview = true;

    }

    private boolean isPurchaseOrderFullyReceived(Bill purchaseOrderBill) {
        if (purchaseOrderBill == null) {
            return false;
        }
        
        List<PharmaceuticalBillItem> orderItems = getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(purchaseOrderBill);
        
        if (orderItems == null || orderItems.isEmpty()) {
            return true;
        }
        
        for (PharmaceuticalBillItem orderItem : orderItems) {
            double calculatedReturns = getPharmacyCalculation().calculateRemainigQtyFromOrder(orderItem);
            double remainingQty = Math.abs(orderItem.getQtyInUnit()) - Math.abs(calculatedReturns);
            double remainingFreeQty = orderItem.getFreeQty() - getPharmacyCalculation().calculateRemainingFreeQtyFromOrder(orderItem);
            
            if (remainingQty > 0 || remainingFreeQty > 0) {
                return false;
            }
        }
        
        return true;
    }

    public void settleWholesale() {
        if (insTotal == 0 && difference != 0) {
            JsfUtil.addErrorMessage("Fill the invoice Total");
            return;
        }
        if (difference != 0) {
            JsfUtil.addErrorMessage("The invoice does not match..! Check again");
            return;
        }

        String msg = pharmacyCalculation.errorCheck(getGrnBill(), billItems);
        if (!msg.isEmpty()) {
            JsfUtil.addErrorMessage(msg);
            return;
        }
        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(getGrnBill());
        if (getGrnBill().getInvoiceDate() == null) {
            getGrnBill().setInvoiceDate(getApproveBill().getCreatedAt());
        }

        saveWholesaleBill();

        Payment p = createPayment(getGrnBill(), getGrnBill().getPaymentMethod());

        for (BillItem i : getBillItems()) {
            if (i.getTmpQty() == 0.0 && i.getTmpFreeQty() == 0.0) {
                continue;
            }

            PharmaceuticalBillItem ph = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);

            i.setCreatedAt(new Date());
            i.setCreater(getSessionController().getLoggedUser());
            i.setBill(getGrnBill());
            if (i.getId() == null) {
                getBillItemFacade().create(i);
            }

            if (ph.getId() == null) {
                getPharmaceuticalBillItemFacade().create(ph);
            }

            i.setPharmaceuticalBillItem(ph);
            getBillItemFacade().edit(i);

            //     updatePoItemQty(i);
            //System.err.println("1 " + i);
            ItemBatch itemBatch = getPharmacyCalculation().saveItemBatch(i);
            // getPharmacyBillBean().preCalForAddToStock(i, itemBatch, getSessionController().getDepartment());

            double addingQty = i.getPharmaceuticalBillItem().getQtyInUnit() + i.getPharmaceuticalBillItem().getFreeQtyInUnit();

            i.getPharmaceuticalBillItem().setItemBatch(itemBatch);

            Stock stock = getPharmacyBean().addToStock(
                    i.getPharmaceuticalBillItem(),
                    Math.abs(addingQty),
                    getSessionController().getDepartment());

            i.getPharmaceuticalBillItem().setStock(stock);

            getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
            getPharmacyCalculation().editBillItem(i.getPharmaceuticalBillItem(), getSessionController().getLoggedUser());
            saveBillFee(i, p);
            getGrnBill().getBillItems().add(i);
        }

        getGrnBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), BillType.PharmacyGrnBill, BillClassType.BilledBill, BillNumberSuffix.GRN));
        getGrnBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), BillType.PharmacyGrnBill, BillClassType.BilledBill, BillNumberSuffix.GRN));

        getGrnBill().setToInstitution(getApproveBill().getFromInstitution());
        getGrnBill().setToDepartment(getApproveBill().getFromDepartment());

        getGrnBill().setInstitution(getSessionController().getInstitution());
        getGrnBill().setDepartment(getSessionController().getDepartment());

        getGrnBill().setCreater(getSessionController().getLoggedUser());
        getGrnBill().setCreatedAt(Calendar.getInstance().getTime());

        calGrossTotal();

        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(getGrnBill());
        updateBalanceForGrn(getGrnBill());

        getBillFacade().edit(getGrnBill());
        //  getPharmacyBillBean().editBill(, , getSessionController());
        
        // Check if Purchase Order is fully received and update fullyIssued status
        if (getApproveBill() != null && !getApproveBill().isFullyIssued()) {
            if (isPurchaseOrderFullyReceived(getApproveBill())) {
                getApproveBill().setFullyIssued(true);
                getApproveBill().setFullyIssuedAt(new Date());
                getApproveBill().setFullyIssuedBy(getSessionController().getLoggedUser());
                getBillFacade().edit(getApproveBill());
            }
        }
        
        printPreview = true;

    }

    public double calFreeQuantityPurchaseValue(Bill b) {
        double freeTotal = 0.0;
        for (BillItem bi : b.getBillItems()) {
            freeTotal = freeTotal + (bi.getPharmaceuticalBillItem().getFreeQty() * bi.getPharmaceuticalBillItem().getPurchaseRate());
        }
        return freeTotal;
    }

    public double calFreeQuantitySaleValue(Bill b) {
        double freeTotal = 0.0;
        for (BillItem bi : b.getBillItems()) {
            freeTotal = freeTotal + (bi.getPharmaceuticalBillItem().getFreeQty() * bi.getPharmaceuticalBillItem().getRetailRate());
        }
        return freeTotal;
    }

    private void updateBalanceForGrn(Bill grn) {
        if (grn == null) {
            return;
        }
        switch (grn.getPaymentMethod()) {
            case Agent:
            case Card:
            case Cash:
            case Cheque:
            case MultiplePaymentMethods:
            case OnCall:
            case OnlineSettlement:
            case PatientDeposit:
            case Slip:
            case Staff:
            case YouOweMe:
            case ewallet:
                grn.setBalance(0.0);
                break;
            case Credit:
                grn.setBalance(Math.abs(grn.getNetTotal()));
            default:
        }
    }

    public void viewPoList() {
        clearList();

    }

    public GrnController() {
    }

    public Institution getDealor() {
        return dealor;
    }

    public void setDealor(Institution dealor) {
        this.dealor = dealor;
    }

    private String txtSearch;

    public void makeListNull() {

//        pharmacyItems = null;
        pos = null;
        grns = null;
        filteredValue = null;
        bills = null;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public Bill getApproveBill() {
        if (approveBill == null) {
            approveBill = new BilledBill();
        }
        return approveBill;
    }

    public void finalizeBill() {
        if (currentGrnBillPre == null) {
            JsfUtil.addErrorMessage("No Bill");
            return;
        }
        if (currentGrnBillPre.getId() == null) {
            request();
        }
        getCurrentGrnBillPre().setEditedAt(new Date());
        getCurrentGrnBillPre().setEditor(sessionController.getLoggedUser());
        getCurrentGrnBillPre().setCheckeAt(new Date());
        getCurrentGrnBillPre().setCheckedBy(sessionController.getLoggedUser());
        getCurrentGrnBillPre().setBillTypeAtomic(BillTypeAtomic.PHARMACY_GRN);
        getBillFacade().edit(getCurrentGrnBillPre());

    }

    public void saveGrnPreBill() {
        getCurrentGrnBillPre().setBillDate(new Date());
        getCurrentGrnBillPre().setBillTime(new Date());
        getCurrentGrnBillPre().setPaymentMethod(getApproveBill().getPaymentMethod());
        getCurrentGrnBillPre().setReferenceBill(getApproveBill());
        if (getCurrentGrnBillPre().getFromInstitution() == null) {
            getCurrentGrnBillPre().setFromInstitution(getFromInstitution());
        }
        getCurrentGrnBillPre().setTotal(total);
        getCurrentGrnBillPre().setDiscount(discount);
        getCurrentGrnBillPre().setNetTotal(netTotal);
        getCurrentGrnBillPre().setReferenceInstitution(getReferenceInstitution());
        getCurrentGrnBillPre().setDepartment(getSessionController().getDepartment());
        getCurrentGrnBillPre().setInstitution(getSessionController().getInstitution());

        getCurrentGrnBillPre().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), BillType.PharmacyGrnBill, BillClassType.BilledBill, BillNumberSuffix.GRN));
        getCurrentGrnBillPre().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), BillType.PharmacyGrnBill, BillClassType.BilledBill, BillNumberSuffix.GRN));

        getCurrentGrnBillPre().setToInstitution(getApproveBill().getFromInstitution());
        getCurrentGrnBillPre().setToDepartment(getApproveBill().getFromDepartment());

        getCurrentGrnBillPre().setInstitution(getSessionController().getInstitution());
        getCurrentGrnBillPre().setDepartment(getSessionController().getDepartment());

        getCurrentGrnBillPre().setCreater(getSessionController().getLoggedUser());
        getCurrentGrnBillPre().setCreatedAt(Calendar.getInstance().getTime());
        //   getGrnBill().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment(), BillType.PharmacyGrnBill, BillNumberSuffix.GRN));
        //   getGrnBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getGrnBill(), BillType.PharmacyGrnBill, BillNumberSuffix.GRN));
//        getGrnBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_GRN);
        if (getCurrentGrnBillPre().getId() == null) {
            getBillFacade().create(getCurrentGrnBillPre());
        } else {
            getBillFacade().edit(getCurrentGrnBillPre());
        }

        getApproveBill().setReferenceBill(getCurrentGrnBillPre());
        getBillFacade().edit(getApproveBill());
    }

    public void saveBill() {
        getGrnBill().setBillDate(new Date());
        getGrnBill().setBillTime(new Date());
//        getGrnBill().setPaymentMethod(getApproveBill().getPaymentMethod());
        getGrnBill().setReferenceBill(getApproveBill());
        getGrnBill().setTotal(total);
        getGrnBill().setDiscount(discount);
        getGrnBill().setNetTotal(netTotal);
        getGrnBill().setReferenceInstitution(getReferenceInstitution());
        getGrnBill().setDepartment(getSessionController().getDepartment());
        getGrnBill().setInstitution(getSessionController().getInstitution());
        getGrnBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_GRN);

        if (getGrnBill().getId() == null) {
            getBillFacade().create(getGrnBill());
        } else {
            getBillFacade().edit(getGrnBill());
        }

        if (getCurrentGrnBillPre() != null) {

            if (getCurrentGrnBillPre().getId() == null) {
                billFacade.create(getCurrentGrnBillPre());
            } else {
                billFacade.edit(getCurrentGrnBillPre());
            }
            getCurrentGrnBillPre().setForwardReferenceBill(getGrnBill());
            getBillFacade().edit(getCurrentGrnBillPre());
        }

    }

    public void saveWholesaleBill() {
        getGrnBill().setBillDate(new Date());
        getGrnBill().setBillTime(new Date());
        getGrnBill().setPaymentMethod(getApproveBill().getPaymentMethod());
        getGrnBill().setReferenceBill(getApproveBill());
        if (getGrnBill().getFromInstitution() == null) {
            getGrnBill().setFromInstitution(getApproveBill().getToInstitution());
        }
        getGrnBill().setDepartment(getSessionController().getDepartment());
        getGrnBill().setInstitution(getSessionController().getInstitution());
        //   getGrnBill().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment(), BillType.PharmacyGrnBill, BillNumberSuffix.GRN));
        //   getGrnBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getGrnBill(), BillType.PharmacyGrnBill, BillNumberSuffix.GRN));
        if (getGrnBill().getId() == null) {
            getBillFacade().create(getGrnBill());
        }
    }

//    public void generateBillComponent() {
//
//        for (PharmaceuticalBillItem i : getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(getApproveBill())) {
//            double remains = getPharmacyCalculation().calQtyInTwoSql(i);
//
//            if (i.getQtyInUnit() >= remains && (i.getQtyInUnit() - remains) != 0) {
//                BillItem bi = new BillItem();
//                bi.setSearialNo(getBillItems().size());
//                bi.setItem(i.getBillItem().getItem());
//                bi.setReferanceBillItem(i.getBillItem());
//                bi.setQty(i.getQtyInUnit() - remains);
//                bi.setTmpQty(i.getQtyInUnit() - remains);
//                //Set Suggession
////                bi.setTmpSuggession(getPharmacyCalculation().getSuggessionOnly(bi.getItem()));
//
//                PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
//                ph.setBillItem(bi);
//                double tmpQty = bi.getQty();
//                ph.setQtyInUnit((double) tmpQty);
//                ph.setPurchaseRate(i.getPurchaseRate());
//                ph.setRetailRate(i.getRetailRate());
//                ph.setLastPurchaseRate(getPharmacyBean().getLastPurchaseRate(bi.getItem(), getSessionController().getDepartment()));
//
//                bi.setPharmaceuticalBillItem(ph);
//
//                getBillItems().add(bi);
//                //  getBillItems().r
//            }
//
//        }
//    }
    public void generateBillComponent() {

        for (PharmaceuticalBillItem i : getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(getApproveBill())) {
            double calculatedReturns = getPharmacyCalculation().calculateRemainigQtyFromOrder(i);
            double remains = Math.abs(i.getQtyInUnit()) - Math.abs(calculatedReturns);
            double remainFreeQty = i.getFreeQty() - getPharmacyCalculation().calculateRemainingFreeQtyFromOrder(i);

            if (remains > 0 || remainFreeQty > 0) {
                BillItem bi = new BillItem();
                bi.setSearialNo(getBillItems().size());
                bi.setItem(i.getBillItem().getItem());
                bi.setReferanceBillItem(i.getBillItem());
                bi.setQty(remains);
//                bi.setFreeQty(remainFreeQty);
                bi.setTmpQty(remains);
                bi.setTmpFreeQty(remainFreeQty);
                //Set Suggession
//                bi.setTmpSuggession(getPharmacyCalculation().getSuggessionOnly(bi.getItem()));

                PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
                ph.setBillItem(bi);
                double tmpQty = bi.getQty();
                double tmpFreeQty = remainFreeQty;

                bi.setPreviousRecieveQtyInUnit(tmpQty);
                bi.setPreviousRecieveFreeQtyInUnit(tmpFreeQty);

                ph.setQty(tmpQty);
                ph.setQtyInUnit(tmpQty);

                ph.setFreeQtyInUnit(tmpFreeQty);
                ph.setFreeQty(tmpFreeQty);

                ph.setPurchaseRate(i.getPurchaseRate());
                ph.setRetailRate(i.getRetailRate());

                double wholesaleFactor = configOptionApplicationController.getDoubleValueByKey("Wholesale Rate Factor", 1.08);
                ph.setWholesaleRate((ph.getPurchaseRate() * wholesaleFactor) * ph.getQtyInUnit() / (ph.getFreeQtyInUnit() + ph.getQtyInUnit()));

                ph.setLastPurchaseRate(getPharmacyBean().getLastPurchaseRate(bi.getItem(), getSessionController().getDepartment()));

                bi.setPharmaceuticalBillItem(ph);

                getBillItems().add(bi);
                //  getBillItems().r
            }

        }
    }

    public void generateBillComponent(Bill importGrnBill) {

        for (BillItem importBi : importGrnBill.getBillItems()) {
            PharmaceuticalBillItem i = importBi.getPharmaceuticalBillItem();
            double remains = i.getQty() - getPharmacyCalculation().calculateRemainigQtyFromOrder(i);
            double remainFreeQty = i.getFreeQty() - getPharmacyCalculation().calculateRemainingFreeQtyFromOrder(i);

            if (remains > 0 || remainFreeQty > 0) {
                BillItem bi = new BillItem();
                bi.setSearialNo(getBillItems().size());
                bi.setItem(i.getBillItem().getItem());
                bi.setReferanceBillItem(i.getBillItem());
                bi.setQty(remains);
//                bi.setFreeQty(remainFreeQty);
                bi.setTmpQty(remains);
                bi.setTmpFreeQty(remainFreeQty);
                //Set Suggession
//                bi.setTmpSuggession(getPharmacyCalculation().getSuggessionOnly(bi.getItem()));

                PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
                ph.setBillItem(bi);
                double tmpQty = bi.getQty();
                double tmpFreeQty = remainFreeQty;

                bi.setPreviousRecieveQtyInUnit(tmpQty);
                bi.setPreviousRecieveFreeQtyInUnit(tmpFreeQty);

                ph.setQty(tmpQty);
                ph.setQtyInUnit(tmpQty);

                ph.setFreeQtyInUnit(tmpFreeQty);
                ph.setFreeQty(tmpFreeQty);

                ph.setPurchaseRate(i.getPurchaseRate());
                ph.setRetailRate(i.getRetailRate());

                ph.setDoe(i.getDoe());
                ph.setStringValue(i.getStringValue());

                double wholesaleFactor2 = configOptionApplicationController.getDoubleValueByKey("Wholesale Rate Factor", 1.08);
                ph.setWholesaleRate((ph.getPurchaseRate() * wholesaleFactor2) * ph.getQtyInUnit() / (ph.getFreeQtyInUnit() + ph.getQtyInUnit()));

                ph.setLastPurchaseRate(getPharmacyBean().getLastPurchaseRate(bi.getItem(), getSessionController().getDepartment()));

                bi.setPharmaceuticalBillItem(ph);

                getBillItems().add(bi);
                //  getBillItems().r
            }

        }
    }

    public void generateBillComponentAll() {

        for (PharmaceuticalBillItem i : getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(getApproveBill())) {
            double remains = i.getQtyInUnit() - getPharmacyCalculation().calculateRemainigQtyFromOrder(i);

            BillItem bi = new BillItem();
            bi.setSearialNo(getBillItems().size());
            bi.setItem(i.getBillItem().getItem());
            bi.setReferanceBillItem(i.getBillItem());
            bi.setQty(remains);
            bi.setTmpQty(remains);
            //Set Suggession
//                bi.setTmpSuggession(getPharmacyCalculation().getSuggessionOnly(bi.getItem()));

            PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
            ph.setBillItem(bi);
            double tmpQty = bi.getQty();

            ph.setQty(tmpQty);
            ph.setQtyInUnit(tmpQty);

            ph.setFreeQty(i.getFreeQty());
            ph.setFreeQtyInUnit(i.getFreeQty());

            ph.setPurchaseRate(i.getPurchaseRate());
            ph.setRetailRate(i.getRetailRate());

            double wholesaleFactor3 = configOptionApplicationController.getDoubleValueByKey("Wholesale Rate Factor", 1.08);
            ph.setWholesaleRate((ph.getPurchaseRate() * wholesaleFactor3) * ph.getQtyInUnit() / (ph.getFreeQtyInUnit() + ph.getQtyInUnit()));

            ph.setLastPurchaseRate(getPharmacyBean().getLastPurchaseRate(bi.getItem(), getSessionController().getDepartment()));
            ph.setFreeQty(i.getFreeQty());

            bi.setPharmaceuticalBillItem(ph);

            getBillItems().add(bi);
            //  getBillItems().r

        }
    }

    public void createGrn() {
        setFromInstitution(getApproveBill().getToInstitution());
        setReferenceInstitution(getSessionController().getLoggedUser().getInstitution());
        generateBillComponent();
        calGrossTotal();
    }

    public void createGrn(Bill importGrn) {
        setFromInstitution(importGrn.getToInstitution());
        setReferenceInstitution(importGrn.getDepartment().getInstitution());
        generateBillComponent(importGrn);
        calGrossTotal();
    }

    public void createGrnAll() {
        getGrnBill().setPaymentMethod(getApproveBill().getPaymentMethod());
        getGrnBill().setFromInstitution(getApproveBill().getToInstitution());
        getGrnBill().setReferenceInstitution(getSessionController().getLoggedUser().getInstitution());
        generateBillComponentAll();
        calGrossTotal();
    }

    public void createGrnWholesale() {
        getGrnBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_GRN_WHOLESALE);
        getGrnBill().setPaymentMethod(getApproveBill().getPaymentMethod());
        getGrnBill().setFromInstitution(getApproveBill().getToInstitution());
        getGrnBill().setReferenceInstitution(getSessionController().getLoggedUser().getInstitution());
        generateBillComponent();
        calGrossTotal();
    }

    private double getRetailPrice(BillItem billItem) {
        String sql = "select (p.retailRate) from PharmaceuticalBillItem p where p.billItem=:b";
        HashMap hm = new HashMap();
        hm.put("b", billItem.getReferanceBillItem());
        return getPharmaceuticalBillItemFacade().findDoubleByJpql(sql, hm);
    }

    public void onEdit(RowEditEvent event) {
        BillItem tmp = (BillItem) event.getObject();

        //    onEditPurchaseRate(tmp);
        setBatch(tmp);
        onEdit(tmp);
    }

    public void checkQty(BillItem bi) {

        if (bi.getTmpQty() < 0.0) {
            bi.setTmpQty(0.0);
        }

        if (bi.getTmpFreeQty() < 0.0) {
            bi.setTmpFreeQty(0.0);
        }

        onEdit(bi);
    }

    public void onEdit(BillItem tmp) {
        setBatch(tmp);
        double remains = getPharmacyCalculation().getRemainingQty(tmp.getPharmaceuticalBillItem());

        if (remains < tmp.getPharmaceuticalBillItem().getQtyInUnit()) {
            tmp.setTmpQty(remains);
            JsfUtil.addErrorMessage("You cant Change Qty than Remaining qty");
        }

        if (tmp.getPharmaceuticalBillItem().getPurchaseRate() > tmp.getPharmaceuticalBillItem().getRetailRate()) {
            tmp.getPharmaceuticalBillItem().setRetailRate(getRetailPrice(tmp.getPharmaceuticalBillItem().getBillItem()));
            JsfUtil.addErrorMessage("You cant set retail price below purchase rate");
        }

        if (tmp.getPharmaceuticalBillItem().getDoe() != null) {
            if (tmp.getPharmaceuticalBillItem().getDoe().getTime() < Calendar.getInstance().getTimeInMillis()) {
                tmp.getPharmaceuticalBillItem().setDoe(null);
                JsfUtil.addErrorMessage("Check Date of Expiry");
                //    return;
            }
        }

        calGrossTotal();
        calDifference();
    }

    public void onEditPurchaseRate(BillItem tmp) {
        double retail = tmp.getPharmaceuticalBillItem().getPurchaseRate() + (tmp.getPharmaceuticalBillItem().getPurchaseRate() * (getPharmacyBean().getMaximumRetailPriceChange() / 100));
        tmp.getPharmaceuticalBillItem().setRetailRate(retail);
    }

    public void calGrossTotal() {
        double tmp = 0.0;
        int serialNo = 0;
        for (BillItem p : getBillItems()) {
            tmp += p.getPharmaceuticalBillItem().getPurchaseRate() * p.getTmpQty();
            p.setSearialNo(serialNo++);
        }

        setTotal(0 - tmp);
        ChangeDiscountLitener();
    }

    public void calGrossTotalForGrnPreBill() {
        double tmp = 0.0;
        int serialNo = 0;
        for (BillItem p : getBillItems()) {
            tmp += p.getPharmaceuticalBillItem().getPurchaseRate() * p.getPharmaceuticalBillItem().getQty();
            p.setSearialNo(serialNo++);
        }

        getGrnBill().setTotal(0 - tmp);
        ChangeDiscountLitener();
    }

    public void ChangeDiscountLitener() {
        setNetTotal(getTotal() + getDiscount());

    }

    public void netDiscount() {
        //getGrnBill().setNetTotal(getGrnBill().getTotal() + getGrnBill().getDiscount());
        double grossTotal = 0.0;
        ChangeDiscountLitener();

        calDifference();
    }

    public void saveBillFee(BillItem bi) {
        saveBillFee(bi, null);
    }

    public void saveBillFee(BillItem bi, Payment p) {
        BillFee bf = new BillFee();
        bf.setCreatedAt(Calendar.getInstance().getTime());
        bf.setCreater(getSessionController().getLoggedUser());
        bf.setBillItem(bi);
        bf.setPatienEncounter(bi.getBill().getPatientEncounter());
        bf.setPatient(bi.getBill().getPatient());
        bf.setFeeValue(bi.getNetValue());
        bf.setFeeGrossValue(bi.getGrossValue());
        bf.setSettleValue(bi.getNetValue());
        bf.setCreatedAt(new Date());
        bf.setDepartment(getSessionController().getDepartment());
        bf.setInstitution(getSessionController().getInstitution());
        bf.setBill(bi.getBill());

        if (bf.getId() == null) {
            getBillFeeFacade().create(bf);
        }
        createBillFeePaymentAndPayment(bf, p);
    }

    public Payment createPayment(Bill bill, PaymentMethod pm) {
        Payment p = new Payment();
        p.setBill(bill);
        setPaymentMethodData(p, pm);
        return p;
    }

    public void setPaymentMethodData(Payment p, PaymentMethod pm) {

        p.setInstitution(getSessionController().getInstitution());
        p.setDepartment(getSessionController().getDepartment());
        p.setCreatedAt(new Date());
        p.setCreater(getSessionController().getLoggedUser());
        p.setPaymentMethod(pm);

        p.setPaidValue(p.getBill().getNetTotal());

        if (p.getId() == null) {
            getPaymentFacade().create(p);
        }

    }

    public void createBillFeePaymentAndPayment(BillFee bf, Payment p) {
        BillFeePayment bfp = new BillFeePayment();
        bfp.setBillFee(bf);
        bfp.setAmount(bf.getSettleValue());
        bfp.setInstitution(getSessionController().getInstitution());
        bfp.setDepartment(getSessionController().getDepartment());
        bfp.setCreater(getSessionController().getLoggedUser());
        bfp.setCreatedAt(new Date());
        bfp.setPayment(p);
        getBillFeePaymentFacade().create(bfp);
    }

    public BillItem getCurrentExpense() {
        if (currentExpense == null) {
            currentExpense = new BillItem();
            currentExpense.setQty(1.0);
        }
        return currentExpense;
    }

    public BilledBill getBill() {
        if (bill == null) {
            bill = new BilledBill();
            bill.setBillType(BillType.StorePurchase);
            bill.setReferenceInstitution(getSessionController().getInstitution());
        }
        return bill;
    }

    public void addExpense() {
        if (getBill().getId() == null) {
            getBillFacade().create(getBill());
        }
        if (getCurrentExpense().getItem() == null) {
            JsfUtil.addErrorMessage("Expense ?");
            return;
        }
        if (currentExpense.getQty() == null || currentExpense.getQty().equals(0.0)) {
            currentExpense.setQty(1.0);
        }
        if (currentExpense.getNetRate() == 0.0) {
            currentExpense.setNetRate(currentExpense.getRate());
        }

        currentExpense.setNetValue(currentExpense.getNetRate() * currentExpense.getQty());
        currentExpense.setGrossValue(currentExpense.getRate() * currentExpense.getQty());

        getCurrentExpense().setSearialNo(getBillExpenses().size());
        getBillExpenses().add(currentExpense);
        currentExpense = null;

    }

    public double calExpenses() {
        double tot = 0.0;
        for (BillItem be : billExpenses) {
            tot = tot + be.getNetValue();
        }
        return tot;
    }

    public void calTotal() {
        double tot = 0.0;
        double exp = 0.0;
        int serialNo = 0;
        for (BillItem p : getBillItems()) {
            p.setQty(p.getPharmaceuticalBillItem().getQtyInUnit());
            p.setRate(p.getPharmaceuticalBillItem().getPurchaseRateInUnit());
            serialNo++;
            p.setSearialNo(serialNo);
            if (p.getParentBillItem() == null) {
                double netValue = p.getQty() * p.getRate();
                p.setNetValue(0 - netValue);
                tot += p.getNetValue();
            }
        }

        for (BillItem e : getBillExpenses()) {
            double nv = e.getNetRate() * e.getQty();
            e.setNetValue(0 - nv);
            exp += e.getNetValue();
        }

        getBill().setExpenseTotal(exp);
        getBill().setTotal(tot);
        getBill().setNetTotal(tot + exp);

    }

//    public double getNetTotal() {
//
//        double tmp = getGrnBill().getTotal() + getGrnBill().getTax() - getGrnBill().getDiscount();
//        getGrnBill().setNetTotal(tmp);
//
//        return tmp;
//    }
    public void setApproveBill(Bill approveBill) {
        this.approveBill = approveBill;
//        grnBill = null;
//        dealor = null;
//        pos = null;
//        printPreview = false;
//        billItems = null;
//        createGrn();
    }

    public Bill getGrnBill() {
        if (grnBill == null) {
            grnBill = new BilledBill();
            grnBill.setBillType(BillType.PharmacyGrnBill);
            grnBill.setBillTypeAtomic(BillTypeAtomic.PHARMACY_GRN);
        }
        return grnBill;
    }

    public void setGrnBill(Bill grnBill) {
        this.grnBill = grnBill;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillNumberGenerator getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberGenerator billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public CategoryFacade getCategoryFacade() {
        return categoryFacade;
    }

    public void setCategoryFacade(CategoryFacade categoryFacade) {
        this.categoryFacade = categoryFacade;
    }

    public ItemBatchFacade getItemBatchFacade() {
        return itemBatchFacade;
    }

    public void setItemBatchFacade(ItemBatchFacade itemBatchFacade) {
        this.itemBatchFacade = itemBatchFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public AmpFacade getAmpFacade() {
        return ampFacade;
    }

    public void setAmpFacade(AmpFacade ampFacade) {
        this.ampFacade = ampFacade;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public List<Bill> getFilteredValue() {
        return filteredValue;
    }

    public void setFilteredValue(List<Bill> filteredValue) {
        this.filteredValue = filteredValue;
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public List<Bill> getPos() {
        return pos;
    }

    public void setPos(List<Bill> pos) {
        this.pos = pos;
    }

//    public List<BillItem> getBillItems() {
//        if (billItems == null) {
//            billItems = new ArrayList<>();
//        }
//        return billItems;
//    }
//
//    public void setBillItems(List<BillItem> billItems) {
//        this.billItems = billItems;
//    }
    public String getTxtSearch() {
        return txtSearch;
    }

    public void setTxtSearch(String txtSearch) {
        this.txtSearch = txtSearch;
    }

    public List<BillItem> getBillItems() {
        if (billItems == null) {
            billItems = new ArrayList<>();
            // serialNo = 0;
        }
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public List<BillItem> getSelectedBillItems() {
        return selectedBillItems;
    }

    public void setSelectedBillItems(List<BillItem> selectedBillItems) {
        this.selectedBillItems = selectedBillItems;
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

    public PharmacyCalculation getPharmacyCalculation() {
        return pharmacyCalculation;
    }

    public void setPharmacyCalculation(PharmacyCalculation pharmacyCalculation) {
        this.pharmacyCalculation = pharmacyCalculation;
    }

    public List<Bill> getGrns() {
        return grns;
    }

    public void setGrns(List<Bill> grns) {
        this.grns = grns;
    }

    public BillFeePaymentFacade getBillFeePaymentFacade() {
        return billFeePaymentFacade;
    }

    public void setBillFeePaymentFacade(BillFeePaymentFacade billFeePaymentFacade) {
        this.billFeePaymentFacade = billFeePaymentFacade;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public PaymentFacade getPaymentFacade() {
        return paymentFacade;
    }

    public void setPaymentFacade(PaymentFacade paymentFacade) {
        this.paymentFacade = paymentFacade;
    }

    public double getInsTotal() {
        return insTotal;
    }

    public void setInsTotal(double insTotal) {
        this.insTotal = insTotal;
    }

    public double getDifference() {
        return difference;
    }

    public void setDifference(double difference) {
        this.difference = difference;
    }

    public Bill getCurrentGrnBillPre() {
        if (currentGrnBillPre == null) {
            currentGrnBillPre = new BilledBill();
            currentGrnBillPre.setBillType(BillType.PharmacyGrnBill);
            currentGrnBillPre.setBillTypeAtomic(BillTypeAtomic.PHARMACY_GRN_PRE);
        }
        return currentGrnBillPre;
    }

    public void setCurrentGrnBillPre(Bill currentGrnBillPre) {
        this.currentGrnBillPre = currentGrnBillPre;
    }

    public Institution getFromInstitution() {
        return fromInstitution;
    }

    public void setFromInstitution(Institution fromInstitution) {
        this.fromInstitution = fromInstitution;
    }

    public Institution getReferenceInstitution() {
        return referenceInstitution;
    }

    public void setReferenceInstitution(Institution referenceInstitution) {
        this.referenceInstitution = referenceInstitution;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Bill getCloseBill() {
        return closeBill;
    }

    public void setCloseBill(Bill closeBill) {
        this.closeBill = closeBill;
    }

    public BillItem getCurrentBillItem() {
        return currentBillItem;
    }

    public void setCurrentBillItem(BillItem currentBillItem) {
        this.currentBillItem = currentBillItem;
    }

    public List<BillItem> getBillExpenses() {
        if (billExpenses == null) {
            billExpenses = new ArrayList<>();
        }
        return billExpenses;
    }

    public void setBillExpenses(List<BillItem> billExpenses) {
        this.billExpenses = billExpenses;
    }

    private void saveImportBill(Bill importGrn) {
        importGrn.setBillType(BillType.PharmacyGrnBillImport);
        importGrn.setBillTypeAtomic(BillTypeAtomic.PHARMACY_GRN_PRE);
        if (importGrn.getId() == null) {
            billFacade.create(importGrn);
        } else {
            billFacade.edit(importGrn);
        }
        for (BillItem bi : importGrn.getBillItems()) {
            if (bi.getId() == null) {
                billItemFacade.create(bi);
            } else {
                billItemFacade.edit(bi);
            }
        }
    }
    
    public void requestWithSaveApprove() {
        // Simple save method for save/approve workflow
        // Allow saving with incomplete data - no validation required
        
        // Set basic bill information
        getCurrentGrnBillPre().setBillDate(new Date());
        getCurrentGrnBillPre().setBillTime(new Date());
        getCurrentGrnBillPre().setReferenceBill(getApproveBill());
        
        // Set invoice date if provided
        if (invoiceDate != null) {
            getCurrentGrnBillPre().setInvoiceDate(invoiceDate);
        }
        // Note: Invoice number is bound directly to bill in UI, no need to set from controller
        
        if (getCurrentGrnBillPre().getFromInstitution() == null) {
            getCurrentGrnBillPre().setFromInstitution(getFromInstitution());
        }
        if (getCurrentGrnBillPre().getReferenceInstitution() == null) {
            getCurrentGrnBillPre().setReferenceInstitution(getReferenceInstitution());
        }
        
        getCurrentGrnBillPre().setTotal(total);
        getCurrentGrnBillPre().setDiscount(discount);
        getCurrentGrnBillPre().setNetTotal(netTotal);
        getCurrentGrnBillPre().setDepartment(getSessionController().getDepartment());
        getCurrentGrnBillPre().setInstitution(getSessionController().getInstitution());
        getCurrentGrnBillPre().setCreater(getSessionController().getLoggedUser());
        getCurrentGrnBillPre().setCreatedAt(Calendar.getInstance().getTime());

        // Replicate receiving context from approved bill, if missing
        if (getCurrentGrnBillPre().getToInstitution() == null) {
            getCurrentGrnBillPre().setToInstitution(getApproveBill().getFromInstitution());
        }
        if (getCurrentGrnBillPre().getToDepartment() == null) {
            getCurrentGrnBillPre().setToDepartment(getApproveBill().getFromDepartment());
        }

        // Create or update the main bill
        if (getCurrentGrnBillPre().getId() == null) {
            getBillFacade().create(getCurrentGrnBillPre());
        } else {
            getBillFacade().edit(getCurrentGrnBillPre());
        }

        // Ensure billItems collection is initialized to prevent NPE
        if (getCurrentGrnBillPre().getBillItems() == null) {
            getCurrentGrnBillPre().setBillItems(new ArrayList<>());
        }

        // Update existing bill items without clearing (to avoid foreign key constraint violations)
        for (BillItem i : getBillItems()) {
            if (i.getTmpQty() == 0.0 && i.getTmpFreeQty() == 0.0) {
                continue;
            }

            // Update quantities from tmp values
            i.getPharmaceuticalBillItem().setQty(i.getTmpQty());
            i.getPharmaceuticalBillItem().setQtyInUnit(i.getTmpQty());
            i.getPharmaceuticalBillItem().setFreeQty(i.getTmpFreeQty());
            i.getPharmaceuticalBillItem().setFreeQtyInUnit(i.getTmpFreeQty());

            i.setBill(getCurrentGrnBillPre());
            i.setCreatedAt(new Date());
            i.setCreater(getSessionController().getLoggedUser());

            if (i.getId() == null) {
                getBillItemFacade().create(i);
            } else {
                getBillItemFacade().edit(i);
            }

            if (i.getPharmaceuticalBillItem().getId() == null) {
                getPharmaceuticalBillItemFacade().create(i.getPharmaceuticalBillItem());
            } else {
                getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
            }

            // Add to bill items collection only if not already present
            if (!getCurrentGrnBillPre().getBillItems().contains(i)) {
                getCurrentGrnBillPre().getBillItems().add(i);
            }
        }

        // Update totals
        calGrossTotal();
        getCurrentGrnBillPre().setTotal(total);
        getCurrentGrnBillPre().setNetTotal(netTotal);
        getBillFacade().edit(getCurrentGrnBillPre());

        JsfUtil.addSuccessMessage("GRN Saved");
    }

    public void requestFinalizeWithSaveApprove() {
        // Always use bill's invoice number, ignore controller reference
        if (getCurrentGrnBillPre().getInvoiceNumber() == null || getCurrentGrnBillPre().getInvoiceNumber().trim().isEmpty()) {
            JsfUtil.addErrorMessage("Please fill invoice number");
            return;
        }
        
        // Set invoice date if provided
        if (invoiceDate != null) {
            getCurrentGrnBillPre().setInvoiceDate(invoiceDate);
        }
        
        // Validate required fields for finalization
        if (Math.abs(difference) > 1) {
            JsfUtil.addErrorMessage("The invoice does not match..! Check again");
            return;
        }
        
        if (getCurrentGrnBillPre().getPaymentMethod() == null) {
            JsfUtil.addErrorMessage("Please select a payment method");
            return;
        }

        // Validate batch details and sale prices for finalization
        String msg = pharmacyCalculation.errorCheck(getCurrentGrnBillPre(), billItems);
        if (!msg.isEmpty()) {
            JsfUtil.addErrorMessage(msg);
            return;
        }

        // First ensure the bill is saved
        if (getCurrentGrnBillPre().getId() == null) {
            requestWithSaveApprove(); // Save first if not already saved
        }

        // Process bill items for finalization with full stock management
        for (BillItem i : getBillItems()) {
            if (i.getTmpQty() == 0.0 && i.getTmpFreeQty() == 0.0) {
                continue;
            }

            // Update quantities
            i.getPharmaceuticalBillItem().setQty(i.getTmpQty());
            i.getPharmaceuticalBillItem().setQtyInUnit(i.getTmpQty());
            i.getPharmaceuticalBillItem().setFreeQty(i.getTmpFreeQty());
            i.getPharmaceuticalBillItem().setFreeQtyInUnit(i.getTmpFreeQty());

            // Create/update item batch for stock management
            ItemBatch itemBatch = getPharmacyCalculation().saveItemBatch(i);
            i.getPharmaceuticalBillItem().setItemBatch(itemBatch);

            // Create stock record - THIS WAS MISSING!
            double addingQty = i.getPharmaceuticalBillItem().getQtyInUnit() + i.getPharmaceuticalBillItem().getFreeQtyInUnit();
            Stock stock = getPharmacyBean().addToStock(
                    i.getPharmaceuticalBillItem(),
                    Math.abs(addingQty),
                    getSessionController().getDepartment());
            
            i.getPharmaceuticalBillItem().setStock(stock);

            // Update pharmaceutical bill item with stock calculations
            getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
            getPharmacyCalculation().editBillItem(i.getPharmaceuticalBillItem(), getSessionController().getLoggedUser());
        }

        // Generate final bill numbers if not already generated
        if (getCurrentGrnBillPre().getDeptId() == null || getCurrentGrnBillPre().getDeptId().isEmpty()) {
            getCurrentGrnBillPre().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), BillType.PharmacyGrnBill, BillClassType.BilledBill, BillNumberSuffix.GRN));
        }
        if (getCurrentGrnBillPre().getInsId() == null || getCurrentGrnBillPre().getInsId().isEmpty()) {
            getCurrentGrnBillPre().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), BillType.PharmacyGrnBill, BillClassType.BilledBill, BillNumberSuffix.GRN));
        }

        // Set finalization timestamps and user
        getCurrentGrnBillPre().setEditedAt(new Date());
        getCurrentGrnBillPre().setEditor(sessionController.getLoggedUser());
        getCurrentGrnBillPre().setCheckeAt(new Date());
        getCurrentGrnBillPre().setCheckedBy(sessionController.getLoggedUser());

        // Change bill type from PRE to final GRN
        getCurrentGrnBillPre().setBillTypeAtomic(BillTypeAtomic.PHARMACY_GRN);

        // Final calculations
        calGrossTotal();
        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(getCurrentGrnBillPre());
        getCurrentGrnBillPre().setTotal(total);
        getCurrentGrnBillPre().setNetTotal(netTotal);

        // Update financial balances - THIS WAS MISSING!
        updateBalanceForGrn(getCurrentGrnBillPre());

        getBillFacade().edit(getCurrentGrnBillPre());

        // Create payment record - THIS WAS MISSING!
        Payment p = createPayment(getCurrentGrnBillPre(), getCurrentGrnBillPre().getPaymentMethod());

        // Check if Purchase Order is fully received and update fullyIssued status
        if (getApproveBill() != null && !getApproveBill().isFullyIssued()) {
            if (isPurchaseOrderFullyReceived(getApproveBill())) {
                getApproveBill().setFullyIssued(true);
                getApproveBill().setFullyIssuedAt(new Date());
                getApproveBill().setFullyIssuedBy(getSessionController().getLoggedUser());
                getBillFacade().edit(getApproveBill());
            }
        }

        JsfUtil.addSuccessMessage("GRN Finalized");
        printPreview = true;
    }

    public double getProfitMargin(BillItem bi) {
        if (bi == null || bi.getPharmaceuticalBillItem() == null) {
            return 0.0;
        }
        double purchaseRate = bi.getPharmaceuticalBillItem().getPurchaseRate();
        double retailRate = bi.getPharmaceuticalBillItem().getRetailRate();
        if (purchaseRate == 0.0) {
            return 0.0;
        }
        return ((retailRate - purchaseRate) / purchaseRate * 100);
    }

    public boolean isProfitAboveThreshold(BillItem bi) {
        if (bi == null) {
            return false;
        }
        double profitMargin = getProfitMargin(bi);
        double threshold = 20.0; // Example threshold
        return profitMargin > threshold;
    }

}
