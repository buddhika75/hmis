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
import com.divudi.core.entity.BillFinanceDetails;
import com.divudi.core.entity.BillItemFinanceDetails;
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
import com.divudi.service.pharmacy.PharmacyCostingService;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import com.divudi.bean.common.ConfigOptionApplicationController;
import com.divudi.core.entity.pharmacy.Amp;
import com.divudi.core.entity.pharmacy.Ampp;
import java.math.BigDecimal;
import java.util.Optional;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class GrnCostingController implements Serializable {

    private static final long serialVersionUID = 1L;

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
    @Deprecated
    BillFeePaymentFacade billFeePaymentFacade;
    @EJB
    BillFeeFacade billFeeFacade;
    @EJB
    PaymentFacade paymentFacade;
    @EJB
    PharmacyCostingService pharmacyCostingService;
    @Inject
    private PharmacyCalculation pharmacyCalculation;
    @Inject
    ConfigOptionApplicationController configOptionApplicationController;

    public boolean isShowProfitInGrnBill() {
        return configOptionApplicationController.getBooleanValueByKey("Show Profit Percentage in GRN", true);
    }

    /**
     * Wrapper for PharmacyCostingService.calculateProfitMarginForPurchases to
     * be used in JSF.
     *
     * @param bi
     * @return
     */
    public double calculateProfitMargin(BillItem bi) {
        return pharmacyCostingService.calculateProfitMarginForPurchases(bi);
    }

//    public double calculateProfitMargin(BillItem bi) {
//        return pharmacyCostingService.calculateProfitMarginForPurchases(bi);
//    }
    /////////////////
    private Bill approveBill;
    // private Bill grnBill; // Removed in favor of single accessor getGrnBill()
    private Bill currentGrnBillPre;
    private boolean printPreview;
    //////////////
    // Removed billItems field - using bill's collection directly
    private List<BillItem> selectedBillItems;
    private SearchKeyword searchKeyword;
    private double insTotal;
    private double difference;
    private Institution fromInstitution;
    private Institution referenceInstitution;
    private Date invoiceDate;
    private String invoiceNumber;
    BillItem currentExpense;

    public double calDifference() {
        difference = Math.abs(insTotal) - Math.abs(getGrnBill().getNetTotal());
        return difference;
    }

    public String navigateToResiveCosting() {
        clear();
        createGrn(); // This now includes discount copying and distribution
        getGrnBill().setPaymentMethod(getApproveBill().getPaymentMethod());
        getGrnBill().setCreditDuration(getApproveBill().getCreditDuration());
        return "/pharmacy/pharmacy_grn_costing?faces-redirect=true";
    }

    public void clear() {
        // grnBill = null; // Field removed
        currentGrnBillPre = null; // Clear both bills since they should be the same object
        invoiceDate = null;
        invoiceNumber = null;
        printPreview = false;
        // billItems removed - using bill's collection directly
        difference = 0;
        insTotal = 0;
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
        if (originalBillItemToDuplicate == null) {
            return;
        }
        BigDecimal totalQuantityOfBillItemsRefernceToOriginalItem = BigDecimal.ZERO;
        BigDecimal totalFreeQuantityOfBillItemsRefernceToOriginalItem = BigDecimal.ZERO;

        BigDecimal remainFreeQty = BigDecimal.ZERO;
        BigDecimal remainQty = BigDecimal.ZERO;

        BillItem newBillItemCreatedByDuplication = new BillItem();
        newBillItemCreatedByDuplication.copy(originalBillItemToDuplicate);
        newBillItemCreatedByDuplication.setId(null);

        BillItemFinanceDetails newBifd = originalBillItemToDuplicate.getBillItemFinanceDetails().clone();
        newBifd.setId(null);
        newBifd.setBillItem(newBillItemCreatedByDuplication);

        PharmaceuticalBillItem newPharmaceuticalBillItemCreatedByDuplication = new PharmaceuticalBillItem();
        newPharmaceuticalBillItemCreatedByDuplication.copy(originalBillItemToDuplicate.getPharmaceuticalBillItem());
        newPharmaceuticalBillItemCreatedByDuplication.setId(null);
        newPharmaceuticalBillItemCreatedByDuplication.setBillItem(newBillItemCreatedByDuplication);

        newBillItemCreatedByDuplication.setItem(originalBillItemToDuplicate.getItem());
        newBillItemCreatedByDuplication.setReferanceBillItem(originalBillItemToDuplicate.getReferanceBillItem());
        newBillItemCreatedByDuplication.setPharmaceuticalBillItem(newPharmaceuticalBillItemCreatedByDuplication);
        newBillItemCreatedByDuplication.setBillItemFinanceDetails(newBifd);

        List<BillItem> tmpBillItems = findAllBillItemsRefernceToOriginalItem(originalBillItemToDuplicate.getReferanceBillItem());

        for (BillItem bi : tmpBillItems) {
            totalQuantityOfBillItemsRefernceToOriginalItem = totalQuantityOfBillItemsRefernceToOriginalItem.add(bi.getBillItemFinanceDetails().getQuantity());
            totalFreeQuantityOfBillItemsRefernceToOriginalItem = totalFreeQuantityOfBillItemsRefernceToOriginalItem.add(bi.getBillItemFinanceDetails().getFreeQuantity());
        }
        remainQty = BigDecimal.valueOf(originalBillItemToDuplicate.getPreviousRecieveQtyInUnit()).subtract(totalQuantityOfBillItemsRefernceToOriginalItem);
        remainFreeQty = BigDecimal.valueOf(originalBillItemToDuplicate.getPreviousRecieveFreeQtyInUnit()).subtract(totalFreeQuantityOfBillItemsRefernceToOriginalItem);

        newBillItemCreatedByDuplication.getPharmaceuticalBillItem().setQty(remainQty.doubleValue());
        newBifd.setQuantity(remainQty);
        newBillItemCreatedByDuplication.getPharmaceuticalBillItem().setQtyInUnit(remainQty.doubleValue());
        newBifd.setFreeQuantity(remainFreeQty);

        newBillItemCreatedByDuplication.getPharmaceuticalBillItem().setFreeQty(remainFreeQty.doubleValue());
        newBillItemCreatedByDuplication.getPharmaceuticalBillItem().setFreeQtyInUnit(remainFreeQty.doubleValue());

        newBillItemCreatedByDuplication.setTmpQty(remainQty.doubleValue());
        newBillItemCreatedByDuplication.setTmpFreeQty(remainFreeQty.doubleValue());

        newBillItemCreatedByDuplication.setPreviousRecieveQtyInUnit(originalBillItemToDuplicate.getPreviousRecieveQtyInUnit());
        newBillItemCreatedByDuplication.setPreviousRecieveFreeQtyInUnit(originalBillItemToDuplicate.getPreviousRecieveFreeQtyInUnit());
        getBillItems().add(newBillItemCreatedByDuplication);
        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(newBillItemCreatedByDuplication.getBillItemFinanceDetails());
        calculateBillTotalsFromItems();
        calDifference();
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
        //  billItems = null;
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

        String msg = pharmacyCalculation.errorCheck(getCurrentGrnBillPre(), getBillItems());
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

            applyFinanceDetailsToPharmaceutical(i);

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

        String msg = pharmacyCalculation.errorCheck(getCurrentGrnBillPre(), getBillItems());
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

            applyFinanceDetailsToPharmaceutical(i);

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
        if (!validateInputs()) {
            return;
        }
        saveGrnBill();
        distributeValuesToItems();
        fillData(getGrnBill());
        processBillItems();
        finalizeSettle();
        createAndPersistPayment();
    }

    private void fillData(Bill inputBill) {
        double billTotalAtCostRate = 0.0;

        double purchaseFree = 0.0;
        double purchaseNonFree = 0.0;

        double retailFree = 0.0;
        double retailNonFree = 0.0;

        double wholesaleFree = 0.0;
        double wholesaleNonFree = 0.0;

        double costFree = 0.0;
        double costNonFree = 0.0;


        for (BillItem bi : getBillItems()) {

            BillItemFinanceDetails bifd = bi.getBillItemFinanceDetails();
            PharmaceuticalBillItem pbi = bi.getPharmaceuticalBillItem();

            if (bifd == null) {
                continue;
            }

            double purchaseRate = bifd.getLineNetRate() != null ? bifd.getLineNetRate().doubleValue() : 0.0;
            double retailRate = bifd.getRetailSaleRate() != null ? bifd.getRetailSaleRate().doubleValue() : 0.0;
            double wholesaleRate = bifd.getWholesaleRate() != null ? bifd.getWholesaleRate().doubleValue() : 0.0;
            double costRate = bifd.getTotalCostRate() != null ? bifd.getTotalCostRate().doubleValue() : 0.0;


            double totalCostValue = bifd.getTotalCost() != null ? bifd.getTotalCost().doubleValue() : 0.0;
            billTotalAtCostRate += totalCostValue;

            double freeQty = bifd.getFreeQuantityByUnits() != null ? bifd.getFreeQuantityByUnits().doubleValue() : 0.0;
            double paidQty = bifd.getQuantityByUnits() != null ? bifd.getQuantityByUnits().doubleValue() : 0.0;


            double tmp;

            tmp = freeQty * purchaseRate;
            purchaseFree += tmp;

            tmp = paidQty * purchaseRate;
            purchaseNonFree += tmp;

            tmp = freeQty * retailRate;
            retailFree += tmp;

            tmp = paidQty * retailRate;
            retailNonFree += tmp;

            tmp = freeQty * wholesaleRate;
            wholesaleFree += tmp;

            tmp = paidQty * wholesaleRate;
            wholesaleNonFree += tmp;

            tmp = freeQty * costRate;
            costFree += tmp;

            tmp = paidQty * costRate;
            costNonFree += tmp;

            BigDecimal grossTotal = bifd.getGrossTotal();
            if (grossTotal != null) {
                bifd.setGrossRate(grossTotal.abs().negate());
            } else {
                bifd.setGrossRate(BigDecimal.ZERO);
            }

            BigDecimal netTotal = bifd.getNetTotal();
            if (netTotal != null) {
                bifd.setNetRate(netTotal.abs().negate());
            } else {
                bifd.setNetRate(BigDecimal.ZERO);
            }
            
            // Safe calculation of value rates with null checks
            BigDecimal lineGrossRateBD = bifd.getLineGrossRate() != null ? bifd.getLineGrossRate() : BigDecimal.ZERO;
            BigDecimal retailSaleRateBD = bifd.getRetailSaleRate() != null ? bifd.getRetailSaleRate() : BigDecimal.ZERO;
            BigDecimal totalCostRateBD = bifd.getTotalCostRate() != null ? bifd.getTotalCostRate() : BigDecimal.ZERO;
            BigDecimal wholesaleRateBD = bifd.getWholesaleRate() != null ? bifd.getWholesaleRate() : BigDecimal.ZERO;
            BigDecimal totalQuantityBD = bifd.getTotalQuantity() != null ? bifd.getTotalQuantity() : BigDecimal.ZERO;
            BigDecimal unitsPerPackBD = bifd.getUnitsPerPack() != null ? bifd.getUnitsPerPack() : BigDecimal.ONE;
            
            bifd.setValueAtPurchaseRate(lineGrossRateBD.multiply(totalQuantityBD));
            bifd.setValueAtRetailRate(retailSaleRateBD.multiply(totalQuantityBD));
            bifd.setValueAtCostRate(totalCostRateBD.multiply(totalQuantityBD).multiply(unitsPerPackBD));
            bifd.setValueAtWholesaleRate(wholesaleRateBD.multiply(totalQuantityBD));
            
        }


        inputBill.getBillFinanceDetails().setTotalCostValue(BigDecimal.valueOf(costFree + costNonFree));
        inputBill.getBillFinanceDetails().setTotalCostValueFree(BigDecimal.valueOf(costFree));
        inputBill.getBillFinanceDetails().setTotalCostValueNonFree(BigDecimal.valueOf(costNonFree));

        inputBill.getBillFinanceDetails().setTotalPurchaseValue(BigDecimal.valueOf(purchaseFree + purchaseNonFree));
        inputBill.getBillFinanceDetails().setTotalPurchaseValueFree(BigDecimal.valueOf(purchaseFree));
        inputBill.getBillFinanceDetails().setTotalPurchaseValueNonFree(BigDecimal.valueOf(purchaseNonFree));

        inputBill.getBillFinanceDetails().setTotalRetailSaleValue(BigDecimal.valueOf(retailFree + retailNonFree));
        inputBill.getBillFinanceDetails().setTotalRetailSaleValueFree(BigDecimal.valueOf(retailFree));
        inputBill.getBillFinanceDetails().setTotalRetailSaleValueNonFree(BigDecimal.valueOf(retailNonFree));

        inputBill.getBillFinanceDetails().setTotalWholesaleValue(BigDecimal.valueOf(wholesaleFree + wholesaleNonFree));
        inputBill.getBillFinanceDetails().setTotalWholesaleValueFree(BigDecimal.valueOf(wholesaleFree));
        inputBill.getBillFinanceDetails().setTotalWholesaleValueNonFree(BigDecimal.valueOf(wholesaleNonFree));

        inputBill.setSaleValue(retailFree + retailNonFree);
        inputBill.setFreeValue(retailFree);

    }

    private boolean validateInputs() {
        if (Math.abs(difference) > 1) {
            JsfUtil.addErrorMessage("The invoice does not match..! Check again");
            return false;
        }
        if (getGrnBill().getFromInstitution() == null) {
            getGrnBill().setFromInstitution(getFromInstitution());
        }
        if (getGrnBill().getReferenceInstitution() == null) {
            getGrnBill().setReferenceInstitution(getReferenceInstitution());
        }
        if (getGrnBill().getPaymentMethod() == null) {
            JsfUtil.addErrorMessage("Please select a payment method");
            return false;
        }

        getGrnBill().setInvoiceDate(invoiceDate);
        getGrnBill().setInvoiceNumber(invoiceNumber);
        String msg = pharmacyCalculation.errorCheck(getGrnBill(), getBillItems());
        if (!msg.isEmpty()) {
            JsfUtil.addErrorMessage(msg);
            return false;
        }
        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(getGrnBill());
        if (getGrnBill().getInvoiceDate() == null) {
            getGrnBill().setInvoiceDate(getApproveBill().getCreatedAt());
        }

        if (getBillItems() == null || getBillItems().isEmpty()) {
            JsfUtil.addErrorMessage("No items found in the GRN.");
            return false;
        }

        for (BillItem bi : getBillItems()) {
            if (bi == null || bi.getBillItemFinanceDetails() == null || bi.getPharmaceuticalBillItem() == null) {
                JsfUtil.addErrorMessage("Invalid item data found.");
                return false;
            }

            BigDecimal qty = bi.getBillItemFinanceDetails().getQuantity();
            BigDecimal freeQty = bi.getBillItemFinanceDetails().getFreeQuantity();
            BigDecimal grossRate = bi.getBillItemFinanceDetails().getLineGrossRate();
            BigDecimal retailRate = bi.getBillItemFinanceDetails().getRetailSaleRate();
            Date doe = bi.getPharmaceuticalBillItem().getDoe();
            String batch = bi.getPharmaceuticalBillItem().getStringValue();

            if ((qty == null ? BigDecimal.ZERO : qty)
                    .add(freeQty == null ? BigDecimal.ZERO : freeQty)
                    .compareTo(BigDecimal.ZERO) <= 0) {
                JsfUtil.addErrorMessage("Item " + bi.getItem().getName() + " has zero or negative quantity.");
                return false;
            }

            if (grossRate == null || grossRate.compareTo(BigDecimal.ZERO) <= 0) {
                JsfUtil.addErrorMessage("Item " + bi.getItem().getName() + " has zero or missing purchase rate.");
                return false;
            }

            if (retailRate == null || retailRate.compareTo(BigDecimal.ZERO) <= 0) {
                JsfUtil.addErrorMessage("Item " + bi.getItem().getName() + " has zero or missing retail rate.");
                return false;
            }

            if (doe == null) {
                JsfUtil.addErrorMessage("Item " + bi.getItem().getName() + " is missing expiry date.");
                return false;
            }

            if (batch == null || batch.trim().isEmpty()) {
                JsfUtil.addErrorMessage("Item " + bi.getItem().getName() + " is missing batch number.");
                return false;
            }
        }

        return true;
    }

    private void saveGrnBill() {
        saveBill();
    }

    private void distributeValuesToItems() {
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());
    }

    private void processBillItems() {
        for (BillItem i : getBillItems()) {
            applyFinanceDetailsToPharmaceutical(i);
            PharmaceuticalBillItem pbi = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);
            i.setCreatedAt(new Date());
            i.setCreater(getSessionController().getLoggedUser());
            i.setBill(getGrnBill());
            if (i.getId() == null) {
                getBillItemFacade().create(i);
            }
            if (pbi.getId() == null) {
                getPharmaceuticalBillItemFacade().create(pbi);
            }
            i.setPharmaceuticalBillItem(pbi);
            getBillItemFacade().edit(i);
            updateStockAndBatches(i);
            getPharmacyCalculation().editBillItem(i.getPharmaceuticalBillItem(), getSessionController().getLoggedUser());
            saveBillFee(i);
            getGrnBill().getBillItems().add(i);
        }
    }

    private void updateStockAndBatches(BillItem i) {
        ItemBatch itemBatch;
        if (configOptionApplicationController.getBooleanValueByKey("Manage Costing", true)) {
            itemBatch = getPharmacyCalculation().saveItemBatchWithCosting(i);
        } else {
            itemBatch = getPharmacyCalculation().saveItemBatch(i);
        }
        double addingQty = i.getPharmaceuticalBillItem().getQtyInUnit()
                + i.getPharmaceuticalBillItem().getFreeQtyInUnit();
        i.getPharmaceuticalBillItem().setItemBatch(itemBatch);
        Stock stock = getPharmacyBean().addToStock(
                i.getPharmaceuticalBillItem(),
                Math.abs(addingQty),
                getSessionController().getDepartment());
        i.getPharmaceuticalBillItem().setStock(stock);
        getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
    }

    private void createAndPersistPayment() {
        createPayment(getGrnBill(), getGrnBill().getPaymentMethod());
    }

    private void finalizeSettle() {
        String deptId = billNumberBean.departmentBillNumberGeneratorYearly(
                getSessionController().getDepartment(), BillTypeAtomic.PHARMACY_GRN);
        getGrnBill().setDeptId(deptId);
        getGrnBill().setInsId(deptId);
        getGrnBill().setToInstitution(getApproveBill().getFromInstitution());
        getGrnBill().setToDepartment(getApproveBill().getFromDepartment());
        getGrnBill().setInstitution(getSessionController().getInstitution());
        getGrnBill().setDepartment(getSessionController().getDepartment());
        getGrnBill().setCreater(getSessionController().getLoggedUser());
        getGrnBill().setCreatedAt(Calendar.getInstance().getTime());
        
        // Recalculate expense totals using the new categorization method
        recalculateExpenseTotals();
        
        // Note: NetTotal is already correctly calculated by the service and includes expenses
        // Removed expense doubling line: getGrnBill().setNetTotal(getGrnBill().getNetTotal() - calExpenses());
        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(getGrnBill());
        updateBalanceForGrn(getGrnBill());
        getBillFacade().edit(getGrnBill());
        
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

        String msg = pharmacyCalculation.errorCheck(getGrnBill(), getBillItems());
        if (!msg.isEmpty()) {
            JsfUtil.addErrorMessage(msg);
            return;
        }
        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(getGrnBill());
        if (getGrnBill().getInvoiceDate() == null) {
            getGrnBill().setInvoiceDate(getApproveBill().getCreatedAt());
        }

        saveWholesaleBill();
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());

        Payment p = createPayment(getGrnBill(), getGrnBill().getPaymentMethod());

        for (BillItem i : getBillItems()) {
            if (i.getTmpQty() == 0.0 && i.getTmpFreeQty() == 0.0) {
                continue;
            }

            applyFinanceDetailsToPharmaceutical(i);

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
            ItemBatch itemBatch;
            if (configOptionApplicationController.getBooleanValueByKey("Manage Costing", true)) {
                itemBatch = getPharmacyCalculation().saveItemBatchWithCosting(i);
            } else {
                itemBatch = getPharmacyCalculation().saveItemBatch(i);
            }
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

    public GrnCostingController() {
    }

    private String txtSearch;

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
        getCurrentGrnBillPre().setBillTypeAtomic(BillTypeAtomic.PHARMACY_ORDER);
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
        getGrnBill().setReferenceInstitution(getReferenceInstitution());
        getGrnBill().setDepartment(getSessionController().getDepartment());
        getGrnBill().setInstitution(getSessionController().getInstitution());
        getGrnBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_GRN);
        getGrnBill().setBillType(BillType.PharmacyGrnBill);

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

    public void generateBillComponent() {

        for (PharmaceuticalBillItem pbiInApprovedOrder : getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(getApproveBill())) {

            if (pbiInApprovedOrder.getBillItem() == null) {
                continue;
            }

            double calculatedReturns = getPharmacyCalculation().calculateRemainigQtyFromOrder(pbiInApprovedOrder);
            double remains = Math.abs(pbiInApprovedOrder.getQty()) - Math.abs(calculatedReturns);
            double remainFreeQty = pbiInApprovedOrder.getFreeQty() - getPharmacyCalculation().calculateRemainingFreeQtyFromOrder(pbiInApprovedOrder);

            if (remains > 0 || remainFreeQty > 0) {
                BillItem newlyCreatedBillItemForGrn = new BillItem();
                newlyCreatedBillItemForGrn.setSearialNo(getBillItems().size());
                newlyCreatedBillItemForGrn.setItem(pbiInApprovedOrder.getBillItem().getItem());
                newlyCreatedBillItemForGrn.setReferanceBillItem(pbiInApprovedOrder.getBillItem());
                
                // Since pbis are always in units, we need to convert appropriately for BillItem
                if (pbiInApprovedOrder.getBillItem().getItem() instanceof com.divudi.core.entity.pharmacy.Ampp) {
                    // For Ampp items, BillItem quantity should be in packs, so convert units to packs
                    double unitsPerPack = pbiInApprovedOrder.getBillItem().getItem().getDblValue();
                    unitsPerPack = unitsPerPack > 0 ? unitsPerPack : 1.0;
                    newlyCreatedBillItemForGrn.setQty(remains / unitsPerPack); // Convert units to packs
                    newlyCreatedBillItemForGrn.setTmpQty(remains / unitsPerPack);
                    newlyCreatedBillItemForGrn.setTmpFreeQty(remainFreeQty / unitsPerPack);
                } else {
                    // For Amp items, BillItem quantity should also be in units
                    newlyCreatedBillItemForGrn.setQty(remains);
                    newlyCreatedBillItemForGrn.setTmpFreeQty(remainFreeQty);
                }

                PharmaceuticalBillItem newlyCreatedPbiForGrn = new PharmaceuticalBillItem();
                newlyCreatedPbiForGrn.setBillItem(newlyCreatedBillItemForGrn);
                double tmpQty = newlyCreatedBillItemForGrn.getQty();
                double tmpFreeQty = newlyCreatedBillItemForGrn.getTmpFreeQty();

                // Previous received quantities should always be stored in units (for tracking)
                newlyCreatedBillItemForGrn.setPreviousRecieveQtyInUnit(remains);
                newlyCreatedBillItemForGrn.setPreviousRecieveFreeQtyInUnit(remainFreeQty);

                // PharmaceuticalBillItem quantities are always in units
                newlyCreatedPbiForGrn.setQty(remains);
                newlyCreatedPbiForGrn.setFreeQty(remainFreeQty);

                double pr = pbiInApprovedOrder.getPurchaseRate(); // This is per unit rate
                double rr = pbiInApprovedOrder.getRetailRate(); // This is per unit rate

                if (pr == 0.0) {
                    double fallbackPr = getPharmacyBean().getLastPurchaseRate(newlyCreatedBillItemForGrn.getItem(), sessionController.getDepartment());
                    if (fallbackPr > 0.0) {
                        pr = fallbackPr;
                    }
                }

                if (rr == 0.0) {
                    double fallbackRr = getPharmacyBean().getLastRetailRateByBillItemFinanceDetails(newlyCreatedBillItemForGrn.getItem(), sessionController.getDepartment());
                    if (fallbackRr > 0.0) {
                        rr = fallbackRr;
                    }
                }

                // Set rates - for pbi they are always per unit rates
                newlyCreatedPbiForGrn.setPurchaseRate(pr);
                newlyCreatedPbiForGrn.setRetailRate(rr);
                
                // For BillItemFinanceDetails, we need the rate per BillItem unit
                double lineGrossRateForBillItem = pr;
                double retailRateForBillItem = rr;
                
                if (pbiInApprovedOrder.getBillItem().getItem() instanceof com.divudi.core.entity.pharmacy.Ampp) {
                    // For Ampp, BillItem is in packs, so we need pack rates
                    double unitsPerPack = pbiInApprovedOrder.getBillItem().getItem().getDblValue();
                    unitsPerPack = unitsPerPack > 0 ? unitsPerPack : 1.0;
                    lineGrossRateForBillItem = pr * unitsPerPack; // Convert unit rate to pack rate
                    retailRateForBillItem = rr * unitsPerPack;
                }

                newlyCreatedBillItemForGrn.setPharmaceuticalBillItem(newlyCreatedPbiForGrn);

                BillItemFinanceDetails fd = new BillItemFinanceDetails(newlyCreatedBillItemForGrn);
                
                // Set quantities in BillItemFinanceDetails to match the BillItem quantities
                fd.setQuantity(java.math.BigDecimal.valueOf(newlyCreatedBillItemForGrn.getQty()));
                fd.setFreeQuantity(java.math.BigDecimal.valueOf(newlyCreatedBillItemForGrn.getTmpFreeQty()));
                fd.setLineGrossRate(java.math.BigDecimal.valueOf(lineGrossRateForBillItem));
                fd.setLineDiscountRate(java.math.BigDecimal.ZERO);
                fd.setRetailSaleRate(java.math.BigDecimal.valueOf(retailRateForBillItem));
                fd.setLineNetRate(BigDecimal.valueOf(lineGrossRateForBillItem));

                newlyCreatedBillItemForGrn.setBillItemFinanceDetails(fd);
                pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(fd);

                getBillItems().add(newlyCreatedBillItemForGrn);
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
                
                // Since PBI quantities are always in units, convert appropriately for BillItem
                if (i.getBillItem().getItem() instanceof com.divudi.core.entity.pharmacy.Ampp) {
                    // For Ampp items, BillItem quantity should be in packs
                    double unitsPerPack = i.getBillItem().getItem().getDblValue();
                    unitsPerPack = unitsPerPack > 0 ? unitsPerPack : 1.0;
                    bi.setQty(remains / unitsPerPack);
                    bi.setTmpQty(remains / unitsPerPack);
                    bi.setTmpFreeQty(remainFreeQty / unitsPerPack);
                } else {
                    // For Amp items, quantities are already in units
                    bi.setQty(remains);
                    bi.setTmpQty(remains);
                    bi.setTmpFreeQty(remainFreeQty);
                }
                //Set Suggession
//                bi.setTmpSuggession(getPharmacyCalculation().getSuggessionOnly(bi.getItem()));

                PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
                ph.setBillItem(bi);
                double tmpQty = bi.getQty();
                double tmpFreeQty = bi.getTmpFreeQty();

                // Previous received quantities are always stored in units
                bi.setPreviousRecieveQtyInUnit(remains);
                bi.setPreviousRecieveFreeQtyInUnit(remainFreeQty);

                // PharmaceuticalBillItem quantities are always in units
                ph.setQty(remains);
                ph.setQtyInUnit(remains);

                ph.setFreeQtyInUnit(remainFreeQty);
                ph.setFreeQty(remainFreeQty);

                ph.setPurchaseRate(i.getPurchaseRate());
                ph.setRetailRate(i.getRetailRate());

                ph.setDoe(i.getDoe());
                ph.setStringValue(i.getStringValue());

                double wr = getWholesaleRate(
                        ph.getPurchaseRate(), ph.getQtyInUnit(), ph.getFreeQtyInUnit());
                ph.setWholesaleRate(wr);

                ph.setLastPurchaseRate(getPharmacyBean().getLastPurchaseRate(bi.getItem(), getSessionController().getDepartment()));

                bi.setPharmaceuticalBillItem(ph);

                BillItemFinanceDetails fd = new BillItemFinanceDetails(bi);
                // Set quantities to match BillItem quantities (not PBI quantities)
                fd.setQuantity(java.math.BigDecimal.valueOf(tmpQty));
                fd.setFreeQuantity(java.math.BigDecimal.valueOf(tmpFreeQty));
                
                // Set rates appropriate for BillItem unit type
                if (bi.getItem() instanceof com.divudi.core.entity.pharmacy.Ampp) {
                    // For Ampp, BillItem is in packs, so use pack rates
                    double unitsPerPack = bi.getItem().getDblValue();
                    unitsPerPack = unitsPerPack > 0 ? unitsPerPack : 1.0;
                    fd.setLineGrossRate(java.math.BigDecimal.valueOf(ph.getPurchaseRate() * unitsPerPack));
                    fd.setRetailSaleRate(java.math.BigDecimal.valueOf(ph.getRetailRate() * unitsPerPack));
                } else {
                    // For Amp, BillItem is in units, so use unit rates
                    fd.setLineGrossRate(java.math.BigDecimal.valueOf(ph.getPurchaseRate()));
                    fd.setRetailSaleRate(java.math.BigDecimal.valueOf(ph.getRetailRate()));
                }
                fd.setWholesaleRate(BigDecimal.valueOf(wr));
                bi.setBillItemFinanceDetails(fd);
                pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(fd);

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
            
            // Since PBI quantities are always in units, convert appropriately for BillItem
            if (i.getBillItem().getItem() instanceof com.divudi.core.entity.pharmacy.Ampp) {
                // For Ampp items, BillItem quantity should be in packs
                double unitsPerPack = i.getBillItem().getItem().getDblValue();
                unitsPerPack = unitsPerPack > 0 ? unitsPerPack : 1.0;
                bi.setQty(remains / unitsPerPack);
                bi.setTmpQty(remains / unitsPerPack);
            } else {
                // For Amp items, quantities are already in units
                bi.setQty(remains);
                bi.setTmpQty(remains);
            }
            //Set Suggession
//                bi.setTmpSuggession(getPharmacyCalculation().getSuggessionOnly(bi.getItem()));

            PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
            ph.setBillItem(bi);
            double tmpQty = bi.getQty();

            // PharmaceuticalBillItem quantities are always in units
            ph.setQty(remains);
            ph.setQtyInUnit(remains);

            ph.setFreeQty(i.getFreeQty());
            ph.setFreeQtyInUnit(i.getFreeQty());

            ph.setPurchaseRate(i.getPurchaseRate());
            ph.setRetailRate(i.getRetailRate());

            double wr = getWholesaleRate(
                    ph.getPurchaseRate(), ph.getQtyInUnit(), ph.getFreeQtyInUnit());
            ph.setWholesaleRate(wr);

            ph.setLastPurchaseRate(getPharmacyBean().getLastPurchaseRate(bi.getItem(), getSessionController().getDepartment()));
            ph.setFreeQty(i.getFreeQty());

            bi.setPharmaceuticalBillItem(ph);

            BillItemFinanceDetails fd = new BillItemFinanceDetails(bi);
            // Set quantities to match BillItem quantities (not PBI quantities)
            fd.setQuantity(java.math.BigDecimal.valueOf(tmpQty));
            fd.setFreeQuantity(java.math.BigDecimal.valueOf(i.getFreeQty()));
            
            // Set rates appropriate for BillItem unit type
            if (bi.getItem() instanceof com.divudi.core.entity.pharmacy.Ampp) {
                // For Ampp, BillItem is in packs, so use pack rates
                double unitsPerPack = bi.getItem().getDblValue();
                unitsPerPack = unitsPerPack > 0 ? unitsPerPack : 1.0;
                fd.setLineGrossRate(java.math.BigDecimal.valueOf(ph.getPurchaseRate() * unitsPerPack));
                fd.setRetailSaleRate(java.math.BigDecimal.valueOf(ph.getRetailRate() * unitsPerPack));
            } else {
                // For Amp, BillItem is in units, so use unit rates
                fd.setLineGrossRate(java.math.BigDecimal.valueOf(ph.getPurchaseRate()));
                fd.setRetailSaleRate(java.math.BigDecimal.valueOf(ph.getRetailRate()));
            }
            fd.setWholesaleRate(BigDecimal.valueOf(wr));
            bi.setBillItemFinanceDetails(fd);
            pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(fd);

            getBillItems().add(bi);
            //  getBillItems().r

        }
    }

    public void createGrn() {
        setFromInstitution(getApproveBill().getToInstitution());
        setReferenceInstitution(getSessionController().getLoggedUser().getInstitution());
        generateBillComponent();
        
        // Copy discount from the approved purchase order to GRN
        if (getApproveBill() != null) {
            getGrnBill().setDiscount(getApproveBill().getDiscount());
        }
        
        // Ensure bill discount synchronization before distribution
        ensureBillDiscountSynchronization();
        
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());
        calculateBillTotalsFromItems();
//        calGrossTotal();
    }

    public void calculateBillTotalsFromItems() {
        pharmacyCostingService.calculateBillTotalsFromItemsForPurchases(getGrnBill(), getBillItems());
    }
    
    // Keep the old method for backward compatibility if needed
    @Deprecated
    public void calculateBillTotalsFromItemsOld() {
        int serialNo = 0;

        // Bill-level inputs: do not calculate here
        BigDecimal billDiscount = BigDecimal.valueOf(getGrnBill().getDiscount());
        BigDecimal billExpense = BigDecimal.ZERO;
        BigDecimal billTax = BigDecimal.ZERO;
        BigDecimal billCost = BigDecimal.ZERO;

        // Totals from bill items
        BigDecimal totalLineDiscounts = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalLineExpenses = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalCostLines = BigDecimal.ZERO;

        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalTaxLines = BigDecimal.ZERO;

        BigDecimal totalFreeItemValue = BigDecimal.ZERO;
        BigDecimal totalPurchase = BigDecimal.ZERO;
        BigDecimal totalRetail = BigDecimal.ZERO;
        BigDecimal totalWholesale = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalFreeQty = BigDecimal.ZERO;
        BigDecimal totalQtyAtomic = BigDecimal.ZERO;
        BigDecimal totalFreeQtyAtomic = BigDecimal.ZERO;

        BigDecimal grossTotal = BigDecimal.ZERO;
        BigDecimal lineGrossTotal = BigDecimal.ZERO;
        BigDecimal netTotal = BigDecimal.ZERO;
        BigDecimal lineNetTotal = BigDecimal.ZERO;

        for (BillItem bi : getBillItems()) {
            PharmaceuticalBillItem pbi = bi.getPharmaceuticalBillItem();
            BillItemFinanceDetails f = bi.getBillItemFinanceDetails();

            if (bi.getItem() instanceof Ampp) {
                bi.setQty(pbi.getQtyPacks());
                bi.setRate(pbi.getPurchaseRatePack());
            } else if (bi.getItem() instanceof Amp) {
                bi.setQty(pbi.getQty());
                bi.setRate(pbi.getPurchaseRate());
            }

            bi.setSearialNo(serialNo++);
            double netValue = bi.getQty() * bi.getRate();
            bi.setNetValue(0 - netValue);

            if (f != null) {
                BigDecimal qty = Optional.ofNullable(f.getQuantity()).orElse(BigDecimal.ZERO);
                BigDecimal freeQty = Optional.ofNullable(f.getFreeQuantity()).orElse(BigDecimal.ZERO);
                BigDecimal costRate = Optional.ofNullable(f.getLineCostRate()).orElse(BigDecimal.ZERO);
                BigDecimal retailRate = Optional.ofNullable(f.getRetailSaleRate()).orElse(BigDecimal.ZERO);
                BigDecimal wholesaleRate = Optional.ofNullable(f.getWholesaleRate()).orElse(BigDecimal.ZERO);

                BigDecimal qtyTotal = qty.add(freeQty);
                BigDecimal retailValue = retailRate.multiply(qtyTotal);
                BigDecimal wholesaleValue = wholesaleRate.multiply(qtyTotal);
                BigDecimal freeItemValue = costRate.multiply(freeQty);

                totalLineDiscounts = totalLineDiscounts.add(Optional.ofNullable(f.getLineDiscount()).orElse(BigDecimal.ZERO));
                totalLineExpenses = totalLineExpenses.add(Optional.ofNullable(f.getLineExpense()).orElse(BigDecimal.ZERO));
                totalTaxLines = totalTaxLines.add(Optional.ofNullable(f.getLineTax()).orElse(BigDecimal.ZERO));
                totalCostLines = totalCostLines.add(Optional.ofNullable(f.getLineCost()).orElse(BigDecimal.ZERO));

                totalDiscount = totalDiscount.add(Optional.ofNullable(f.getTotalDiscount()).orElse(BigDecimal.ZERO));
                totalExpense = totalExpense.add(Optional.ofNullable(f.getTotalExpense()).orElse(BigDecimal.ZERO));
                totalCost = totalCost.add(Optional.ofNullable(f.getTotalCost()).orElse(BigDecimal.ZERO));
                totalTax = totalTax.add(Optional.ofNullable(f.getTotalTax()).orElse(BigDecimal.ZERO));

                totalFreeItemValue = totalFreeItemValue.add(freeItemValue);
                totalPurchase = totalPurchase.add(Optional.ofNullable(f.getGrossTotal()).orElse(BigDecimal.ZERO));
                totalRetail = totalRetail.add(retailValue);
                totalWholesale = totalWholesale.add(wholesaleValue);

                totalQty = totalQty.add(qty);
                totalFreeQty = totalFreeQty.add(freeQty);
                totalQtyAtomic = totalQtyAtomic.add(Optional.ofNullable(f.getQuantityByUnits()).orElse(BigDecimal.ZERO));
                totalFreeQtyAtomic = totalFreeQtyAtomic.add(Optional.ofNullable(f.getFreeQuantityByUnits()).orElse(BigDecimal.ZERO));

                grossTotal = grossTotal.add(Optional.ofNullable(f.getLineNetTotal()).orElse(BigDecimal.ZERO));
                lineGrossTotal = lineGrossTotal.add(Optional.ofNullable(f.getLineGrossTotal()).orElse(BigDecimal.ZERO));

                netTotal = netTotal.add(Optional.ofNullable(f.getNetTotal()).orElse(BigDecimal.ZERO));
                BigDecimal itemLineNetTotal = Optional.ofNullable(f.getLineNetTotal()).orElse(BigDecimal.ZERO);
                lineNetTotal = lineNetTotal.add(itemLineNetTotal);
            }

        }

        // Assign legacy totals
        getGrnBill().setTotal(grossTotal.doubleValue());
        getGrnBill().setNetTotal(netTotal.doubleValue());
        getGrnBill().setSaleValue(totalRetail.doubleValue());

        // Assign to BillFinanceDetails
        BillFinanceDetails bfd = getGrnBill().getBillFinanceDetails();
        if (bfd == null) {
            bfd = new BillFinanceDetails(getGrnBill());
            getGrnBill().setBillFinanceDetails(bfd);
        }

        // Inputs from user or UI – left unchanged if already set
        bfd.setBillDiscount(bfd.getBillDiscount() != null ? bfd.getBillDiscount() : billDiscount);
        bfd.setBillExpense(bfd.getBillExpense() != null ? bfd.getBillExpense() : billExpense);
        bfd.setBillTaxValue(bfd.getBillTaxValue() != null ? bfd.getBillTaxValue() : billTax);
        bfd.setBillCostValue(bfd.getBillCostValue() != null ? bfd.getBillCostValue() : billCost);

        // Assign calculated from items
        bfd.setLineDiscount(totalLineDiscounts);
        bfd.setLineExpense(totalLineExpenses);
        bfd.setItemTaxValue(totalTaxLines);
        bfd.setLineCostValue(totalCostLines);

        bfd.setTotalDiscount(totalDiscount);
        bfd.setTotalExpense(totalExpense);
        bfd.setTotalCostValue(totalCost);
        bfd.setTotalTaxValue(totalTax);

        bfd.setTotalOfFreeItemValues(totalFreeItemValue);
        bfd.setTotalPurchaseValue(totalPurchase);
        bfd.setTotalRetailSaleValue(totalRetail);
        bfd.setTotalWholesaleValue(totalWholesale);

        bfd.setTotalQuantity(totalQty);
        bfd.setTotalFreeQuantity(totalFreeQty);
        bfd.setTotalQuantityInAtomicUnitOfMeasurement(totalQtyAtomic);
        bfd.setTotalFreeQuantityInAtomicUnitOfMeasurement(totalFreeQtyAtomic);

        bfd.setGrossTotal(grossTotal);
        bfd.setLineGrossTotal(lineGrossTotal);
        bfd.setNetTotal(netTotal);
        bfd.setLineNetTotal(lineNetTotal);
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

    private double getWholesaleRate(double purchaseRate, double qtyInUnit, double freeQtyInUnit) {
        double wholesaleFactor
                = configOptionApplicationController.getDoubleValueByKey("Wholesale Rate Factor", 1.08);
        return (purchaseRate * wholesaleFactor) * qtyInUnit / (freeQtyInUnit + qtyInUnit);
    }

    private void applyFinanceDetailsToPharmaceutical(BillItem bi) {
        if (bi == null) {
            return;
        }

        BillItemFinanceDetails f = bi.getBillItemFinanceDetails();
        PharmaceuticalBillItem pbi = bi.getPharmaceuticalBillItem();

        if (f == null || pbi == null) {
            return;
        }

        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(f);

        if (bi.getItem() instanceof com.divudi.core.entity.pharmacy.Ampp) {
            BigDecimal unitsPerPack = Optional.ofNullable(f.getUnitsPerPack())
                    .orElse(BigDecimal.ONE);
            BigDecimal qtyPacks = Optional.ofNullable(f.getQuantity())
                    .orElse(BigDecimal.ZERO);
            BigDecimal qtyUnits = qtyPacks.multiply(unitsPerPack);
            BigDecimal freeQtyPacks = Optional.ofNullable(f.getFreeQuantity())
                    .orElse(BigDecimal.ZERO);
            BigDecimal freeQtyUnits = freeQtyPacks.multiply(unitsPerPack);

            pbi.setQty(qtyUnits.doubleValue());
            pbi.setQtyInUnit(pbi.getQty());
            pbi.setQtyPacks(qtyPacks.doubleValue());

            pbi.setFreeQty(freeQtyUnits.doubleValue());
            pbi.setFreeQtyInUnit(pbi.getFreeQty());
            pbi.setFreeQtyPacks(freeQtyPacks.doubleValue());

            pbi.setPurchaseRate(Optional.ofNullable(f.getNetRate()).orElse(BigDecimal.ZERO).doubleValue());
            pbi.setPurchaseRatePack(pbi.getPurchaseRate());

            pbi.setRetailRate(Optional.ofNullable(f.getRetailSaleRate()).orElse(BigDecimal.ZERO).doubleValue());
            pbi.setRetailRatePack(pbi.getRetailRate());
            pbi.setRetailRateInUnit(Optional.ofNullable(f.getRetailSaleRatePerUnit()).orElse(BigDecimal.ZERO).doubleValue());

            // Update BillItem quantity and rate in packs
            bi.setQty(qtyPacks.doubleValue());
            bi.setRate(pbi.getPurchaseRatePack());
        } else {
            BigDecimal qty = Optional.ofNullable(f.getQuantityByUnits()).orElse(BigDecimal.ZERO);
            BigDecimal freeQty = Optional.ofNullable(f.getFreeQuantityByUnits()).orElse(BigDecimal.ZERO);

            pbi.setQty(qty.doubleValue());
            pbi.setQtyInUnit(pbi.getQty());
            pbi.setQtyPacks(pbi.getQty());

            pbi.setFreeQty(freeQty.doubleValue());
            pbi.setFreeQtyInUnit(pbi.getFreeQty());
            pbi.setFreeQtyPacks(pbi.getFreeQty());

            pbi.setPurchaseRate(Optional.ofNullable(f.getNetRate()).orElse(BigDecimal.ZERO).doubleValue());
            pbi.setPurchaseRatePack(pbi.getPurchaseRate());

            double r = Optional.ofNullable(f.getRetailSaleRatePerUnit()).orElse(BigDecimal.ZERO).doubleValue();
            pbi.setRetailRate(r);
            pbi.setRetailRatePack(r);
            pbi.setRetailRateInUnit(r);

            // Update BillItem quantity and rate in units
            bi.setQty(qty.doubleValue());
            bi.setRate(pbi.getPurchaseRate());
        }
    }

    public void onEdit(RowEditEvent event) {
        BillItem editingBillItem = (BillItem) event.getObject();
        setBatch(editingBillItem);
        onEdit(editingBillItem);
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
        BillItemFinanceDetails f = tmp.getBillItemFinanceDetails();
        if (f == null) {
            return;
        }
        double remains = getPharmacyCalculation().getRemainingQty(tmp.getPharmaceuticalBillItem());
        if (remains < f.getQuantity().doubleValue()) {
            f.setQuantity(java.math.BigDecimal.valueOf(remains));
            tmp.setTmpQty(remains);
            JsfUtil.addErrorMessage("You cant Change Qty than Remaining qty");
        }

        if (f.getLineGrossRate().compareTo(f.getRetailSaleRatePerUnit()) > 0) {
            f.setRetailSaleRatePerUnit(f.getLineGrossRate());
            JsfUtil.addErrorMessage("You cant set retail price below purchase rate");
        }
        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(f);
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());
        calculateBillTotalsFromItems();
        calDifference();
    }

    public void lineDiscountRateChangedListner(BillItem tmp) {
        BillItemFinanceDetails f = tmp.getBillItemFinanceDetails();
        if (f == null) {
            return;
        }
        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(f);
        
        // Redistribute bill discount after line rate changes (even if discount is 0 to clear previous distributions)
        ensureBillDiscountSynchronization();
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());
        
        calculateBillTotalsFromItems();
        calDifference();
    }

    public void retailRateChangedListner(BillItem tmp) {
        BillItemFinanceDetails f = tmp.getBillItemFinanceDetails();
        if (f == null) {
            return;
        }
        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(f);
        
        // Redistribute bill discount after retail rate changes (even if discount is 0 to clear previous distributions)
        ensureBillDiscountSynchronization();
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());
        
        calculateBillTotalsFromItems();
        calDifference();
    }

    public void freeQtyChangedListner(BillItem tmp) {
        BillItemFinanceDetails f = tmp.getBillItemFinanceDetails();
        if (f == null) {
            return;
        }
        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(f);
        calculateBillTotalsFromItems();
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());
        calDifference();
    }

    public void qtyChangedListner(BillItem tmp) {
        BillItemFinanceDetails f = tmp.getBillItemFinanceDetails();
        if (f == null) {
            return;
        }
        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(f);
        
        // Redistribute bill discount after quantity changes (even if discount is 0 to clear previous distributions)
        ensureBillDiscountSynchronization();
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());
        
        calculateBillTotalsFromItems();
        calDifference();
    }

    public void onEditPurchaseRate(BillItem tmp) {
        double retail = tmp.getPharmaceuticalBillItem().getPurchaseRate() + (tmp.getPharmaceuticalBillItem().getPurchaseRate() * (getPharmacyBean().getMaximumRetailPriceChange() / 100));
        tmp.getPharmaceuticalBillItem().setRetailRate(retail);
    }

    @Deprecated
    public void calGrossTotal() {
        double tmp = 0.0;
        int serialNo = 0;
        for (BillItem p : getBillItems()) {
            BillItemFinanceDetails f = p.getBillItemFinanceDetails();
            if (f != null) {
                java.math.BigDecimal rate = java.util.Optional.ofNullable(f.getLineGrossRate()).orElse(java.math.BigDecimal.ZERO);
                java.math.BigDecimal qty = java.util.Optional.ofNullable(f.getQuantity()).orElse(java.math.BigDecimal.ZERO);
                tmp += rate.multiply(qty).doubleValue();
            }
            p.setSearialNo(serialNo++);
        }
        discountChangedLitener();
    }

    public void calGrossTotalForGrnPreBill() {
        double tmp = 0.0;
        int serialNo = 0;
        for (BillItem p : getBillItems()) {
            BillItemFinanceDetails f = p.getBillItemFinanceDetails();
            if (f != null) {
                java.math.BigDecimal rate = java.util.Optional.ofNullable(f.getLineGrossRate()).orElse(java.math.BigDecimal.ZERO);
                java.math.BigDecimal qty = java.util.Optional.ofNullable(f.getQuantity()).orElse(java.math.BigDecimal.ZERO);
                tmp += rate.multiply(qty).doubleValue();
            }
            p.setSearialNo(serialNo++);
        }

        getGrnBill().setTotal(0 - tmp);
        discountChangedLitener();
    }

    public void discountChangedLitener() {
        ensureBillDiscountSynchronization();
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());
        // Don't call calculateBillTotalsFromItems() after distribution as it resets the distributed values
        // The distribution method should handle final bill totals
        calDifference();
    }
    
    /**
     * Ensures that both bill.discount and bill.billFinanceDetails.billDiscount are synchronized
     * The service method reads from billFinanceDetails.billDiscount, but UI may store in bill.discount
     */
    private void ensureBillDiscountSynchronization() {
        if (getGrnBill() == null) {
            return;
        }
        
        // Ensure BillFinanceDetails exists
        if (getGrnBill().getBillFinanceDetails() == null) {
            getGrnBill().setBillFinanceDetails(new BillFinanceDetails(getGrnBill()));
        }
        
        // Synchronize discount and tax from bill to billFinanceDetails
        getGrnBill().getBillFinanceDetails().setBillDiscount(BigDecimal.valueOf(getGrnBill().getDiscount()));
        getGrnBill().getBillFinanceDetails().setBillTaxValue(BigDecimal.valueOf(getGrnBill().getTax()));
        
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
        // Bill Fee Payment Concept is no loger used. 
        // createBillFeePaymentAndPayment(bf, p);
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

    @Deprecated // THis is NO longer Needed
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
            // Explicitly set default value for consideredForCosting
            currentExpense.setConsideredForCosting(true);
        }
        return currentExpense;
    }
    
    // Method called when expense item is selected from autocomplete
    public void onExpenseItemSelect() {
        // This method is called when an expense item is selected from the autocomplete
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
        if (getGrnBill().getId() == null) {
            getBillFacade().create(getGrnBill());
            if (getGrnBill().getBillFinanceDetails() == null) {
                getGrnBill().setBillFinanceDetails(new BillFinanceDetails(getGrnBill()));
            }
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

        getCurrentExpense().setSearialNo(getGrnBill().getBillExpenses().size());
        
        // CRITICAL: Set the expenseBill reference for JPA relationship
        getCurrentExpense().setExpenseBill(getGrnBill());
        
        getGrnBill().getBillExpenses().add(currentExpense);
        
        // Recalculate expense totals after adding new expense
        recalculateExpenseTotals();
        
        // Recalculate entire bill totals with updated expense categorization
        calculateBillTotalsFromItems();
        
        // Distribute proportional bill values (including expenses considered for costing) to line items
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());
        
        // Persist the updated GRN bill
        if (getGrnBill().getId() != null) {
            getBillFacade().edit(getGrnBill());
        }
        
        currentExpense = null;
    }
    
    // Method to recalculate expense totals based on costing categorization
    public void recalculateExpenseTotals() {
        double totalExpenses = 0.0;
        double expensesForCosting = 0.0;
        double expensesNotForCosting = 0.0;
        
        if (getGrnBill().getBillExpenses() != null) {
            for (BillItem expense : getGrnBill().getBillExpenses()) {
                // Skip retired expenses
                if (expense.isRetired()) {
                    continue;
                }
                double expenseValue = expense.getNetValue();
                totalExpenses += expenseValue;
                
                if (expense.isConsideredForCosting()) {
                    expensesForCosting += expenseValue;
                } else {
                    expensesNotForCosting += expenseValue;
                }
            }
        }
        
        getGrnBill().setExpenseTotal(totalExpenses);
        getGrnBill().setExpensesTotalConsideredForCosting(expensesForCosting);
        getGrnBill().setExpensesTotalNotConsideredForCosting(expensesNotForCosting);
    }
    
    // Method to handle expense costing checkbox changes
    public void updateExpenseCosting(BillItem expense) {
        recalculateExpenseTotals();
        calculateBillTotalsFromItems();
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());
        
        if (getGrnBill().getId() != null) {
            billFacade.edit(getGrnBill());
        }
    }
    
    // Method to get total bill expenses for display
    public double getBillExpensesTotal() {
        if (getGrnBill() == null || getGrnBill().getBillExpenses() == null || getGrnBill().getBillExpenses().isEmpty()) {
            return 0.0;
        }
        
        double total = 0.0;
        for (BillItem expense : getGrnBill().getBillExpenses()) {
            // Skip retired expenses
            if (!expense.isRetired()) {
                total += expense.getNetValue();
            }
        }
        return total;
    }
    
    // Method to get expenses considered for costing total
    public double getExpensesTotalConsideredForCosting() {
        if (getGrnBill() == null || getGrnBill().getBillExpenses() == null || getGrnBill().getBillExpenses().isEmpty()) {
            return 0.0;
        }
        
        double total = 0.0;
        for (BillItem expense : getGrnBill().getBillExpenses()) {
            // Skip retired expenses
            if (!expense.isRetired() && expense.isConsideredForCosting()) {
                total += expense.getNetValue();
            }
        }
        return total;
    }
    
    // Method to get expenses not considered for costing total
    public double getExpensesTotalNotConsideredForCosting() {
        if (getGrnBill() == null || getGrnBill().getBillExpenses() == null || getGrnBill().getBillExpenses().isEmpty()) {
            return 0.0;
        }
        
        double total = 0.0;
        for (BillItem expense : getGrnBill().getBillExpenses()) {
            // Skip retired expenses
            if (!expense.isRetired() && !expense.isConsideredForCosting()) {
                total += expense.getNetValue();
            }
        }
        return total;
    }

    public void removeExpense(BillItem expense) {
        if (expense == null) {
            return;
        }

        // Mark expense as retired instead of deleting
        expense.setRetired(true);
        expense.setRetiredAt(new Date());
        expense.setRetirer(getSessionController().getLoggedUser());
        
        // If expense was already persisted, update it in database
        if (expense.getId() != null) {
            getBillItemFacade().edit(expense);
        }
        
        // Reload bill expenses to exclude retired ones
        if (getGrnBill().getId() != null) {
            reloadBillExpenses();
        }

        recalculateExpenseTotals();
        calculateBillTotalsFromItems();
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());

        if (getGrnBill().getId() != null) {
            billFacade.edit(getGrnBill());
        }
    }
    
    /**
     * Reload bill expenses from database excluding retired ones
     */
    private void reloadBillExpenses() {
        if (getGrnBill().getId() != null) {
            String jpql = "SELECT be FROM BillItem be WHERE be.expenseBill.id = :billId AND be.retired = false ORDER BY be.searialNo";
            Map<String, Object> params = new HashMap<>();
            params.put("billId", getGrnBill().getId());
            List<BillItem> activeExpenses = getBillItemFacade().findByJpql(jpql, params);
            getGrnBill().setBillExpenses(activeExpenses);
            
            // Reindex serial numbers for remaining expenses
            int index = 0;
            for (BillItem be : activeExpenses) {
                be.setSearialNo(index++);
            }
        }
    }

    public double calExpenses() {
        double tot = 0.0;
        if (getGrnBill().getBillExpenses() != null) {
            for (BillItem be : getGrnBill().getBillExpenses()) {
                // Skip retired expenses
                if (!be.isRetired()) {
                    tot = tot + be.getNetValue();
                }
            }
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

        for (BillItem e : getGrnBill().getBillExpenses()) {
            // Skip retired expenses
            if (!e.isRetired()) {
                double nv = e.getNetRate() * e.getQty();
                e.setNetValue(0 - nv);
                exp += e.getNetValue();
            }
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
        // Use the same object as currentGrnBillPre for one-bill approach
        Bill bill = getCurrentGrnBillPre();
        
        // Ensure it has BillFinanceDetails for calculations
        if (bill.getBillFinanceDetails() == null) {
            bill.setBillFinanceDetails(new BillFinanceDetails(bill));
        }
        
        return bill;
    }

    @Deprecated // Use getCurrentGrnBillPre() directly if needed for setting
    public void setGrnBill(Bill grnBill) {
        // this.grnBill = grnBill; // Field removed
        this.currentGrnBillPre = grnBill;
    }

    public SessionController getSessionController() {
        if (sessionController == null) {
            throw new IllegalStateException("SessionController is not properly injected");
        }
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

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
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
        // Always return the bill's collection directly - single source of truth
        if (getCurrentGrnBillPre().getBillItems() == null) {
            getCurrentGrnBillPre().setBillItems(new ArrayList<>());
        }
        return getCurrentGrnBillPre().getBillItems();
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

    public PharmacyCalculation getPharmacyCalculation() {
        return pharmacyCalculation;
    }

    public void setPharmacyCalculation(PharmacyCalculation pharmacyCalculation) {
        this.pharmacyCalculation = pharmacyCalculation;
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
            if (getApproveBill() != null) {
                currentGrnBillPre.setConsignment(getApproveBill().isConsignment());
            }
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

    public String navigateToResiveCostingWithSaveApprove() {
        clear();
        
        // Prepare bill and items without saving - like createGrn() but without persistence
        setFromInstitution(getApproveBill().getToInstitution());
        setReferenceInstitution(getSessionController().getLoggedUser().getInstitution());
        generateBillComponent(); // This creates bill items but doesn't save
        
        getCurrentGrnBillPre().setPaymentMethod(getApproveBill().getPaymentMethod());
        getCurrentGrnBillPre().setCreditDuration(getApproveBill().getCreditDuration());
        
        // Copy discount from the approved purchase order to GRN
        if (getApproveBill() != null) {
            getCurrentGrnBillPre().setDiscount(getApproveBill().getDiscount());
        }
        
        // Ensure calculations are done after setup
        if (getBillItems() != null && !getBillItems().isEmpty()) {
            // Ensure bill discount synchronization before distribution
            ensureBillDiscountSynchronization();
            
            pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());
            calculateBillTotalsFromItems();
            calDifference();
        }
        
        return "/pharmacy/pharmacy_grn_costing_with_save_approve?faces-redirect=true";
    }

    public String navigateToEditGrnCosting() {
        // Load the saved GRN for editing
        // Save the bill reference before clearing
        Bill savedBill = getCurrentGrnBillPre();
        clear();
        // Restore the bill reference after clearing
        setCurrentGrnBillPre(savedBill);
        
        // Explicitly fetch bill items from database to avoid lazy loading issues
        if (getCurrentGrnBillPre().getId() != null) {
            String jpql = "SELECT bi FROM BillItem bi WHERE bi.bill.id = :billId ORDER BY bi.searialNo";
            Map<String, Object> params = new HashMap<>();
            params.put("billId", getCurrentGrnBillPre().getId());
            List<BillItem> loadedItems = getBillItemFacade().findByJpql(jpql, params);
            
            // Set the loaded items to the bill's collection directly
            getCurrentGrnBillPre().setBillItems(loadedItems);
            
            // Also load bill expenses to preserve them when editing (exclude retired ones)
            String expenseJpql = "SELECT be FROM BillItem be WHERE be.expenseBill.id = :billId AND be.retired = false ORDER BY be.searialNo";
            List<BillItem> loadedExpenses = getBillItemFacade().findByJpql(expenseJpql, params);
            getCurrentGrnBillPre().setBillExpenses(loadedExpenses);
        }
        
        invoiceDate = getCurrentGrnBillPre().getInvoiceDate();
        invoiceNumber = getCurrentGrnBillPre().getInvoiceNumber();
        setFromInstitution(getCurrentGrnBillPre().getFromInstitution());
        
        // Recalculate totals when loading saved bill
        if (getBillItems() != null && !getBillItems().isEmpty()) {
            // Ensure each bill item's finance details are properly calculated
            for (BillItem bi : getBillItems()) {
                if (bi.getBillItemFinanceDetails() != null) {
                    pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(bi.getBillItemFinanceDetails());
                }
            }
            
            // Recalculate expense totals with loaded expenses
            recalculateExpenseTotals();
            
            // Ensure bill discount synchronization before distribution
            ensureBillDiscountSynchronization();
            
            pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());
            calculateBillTotalsFromItems();
            calDifference();
        }
        
        return "/pharmacy/pharmacy_grn_costing_with_save_approve?faces-redirect=true";
    }

    public void requestWithSaveApprove() {
        // Simple save method for costing save/approve workflow
        // Allow saving with incomplete data - no validation required
        
        // Set basic bill information
        getCurrentGrnBillPre().setBillDate(new Date());
        getCurrentGrnBillPre().setBillTime(new Date());
        
        // Only set reference bill if not already set (for new GRNs, not edited ones)
        if (getCurrentGrnBillPre().getReferenceBill() == null) {
            getCurrentGrnBillPre().setReferenceBill(getApproveBill());
        }
        
        // Set invoice date if provided
        if (invoiceDate != null) {
            getCurrentGrnBillPre().setInvoiceDate(invoiceDate);
        }
        
        if (getCurrentGrnBillPre().getFromInstitution() == null) {
            getCurrentGrnBillPre().setFromInstitution(getFromInstitution());
        }
        if (getCurrentGrnBillPre().getReferenceInstitution() == null) {
            getCurrentGrnBillPre().setReferenceInstitution(getReferenceInstitution());
        }
        
        getCurrentGrnBillPre().setDepartment(getSessionController().getDepartment());
        getCurrentGrnBillPre().setInstitution(getSessionController().getInstitution());
        getCurrentGrnBillPre().setCreater(getSessionController().getLoggedUser());
        getCurrentGrnBillPre().setCreatedAt(Calendar.getInstance().getTime());

        // Initialize bill items collection if null (getBillItems() handles this automatically)
        getBillItems(); // This will initialize if null

        // Create or update the main bill
        if (getCurrentGrnBillPre().getId() == null) {
            getBillFacade().create(getCurrentGrnBillPre());
        } else {
            getBillFacade().edit(getCurrentGrnBillPre());
        }

        // Save bill items - work directly with bill's collection
        for (BillItem i : getCurrentGrnBillPre().getBillItems()) {
            BillItemFinanceDetails f = i.getBillItemFinanceDetails();
            if (f == null || (
                (f.getQuantity() == null || f.getQuantity().compareTo(BigDecimal.ZERO) == 0) &&
                (f.getFreeQuantity() == null || f.getFreeQuantity().compareTo(BigDecimal.ZERO) == 0)
            )) {
                continue;
            }

            
            i.setBill(getCurrentGrnBillPre());
            i.setCreatedAt(new Date());
            i.setCreater(getSessionController().getLoggedUser());

            // Create or update BillItem
            if (i.getId() == null) {
                getBillItemFacade().create(i);
            } else {
                getBillItemFacade().edit(i);
            }

            // Handle BillItemFinanceDetails
            if (i.getBillItemFinanceDetails() != null) {
                i.getBillItemFinanceDetails().setBillItem(i);
            }

            // Handle PharmaceuticalBillItem
            if (i.getPharmaceuticalBillItem() != null) {
                if (i.getPharmaceuticalBillItem().getId() == null) {
                    getPharmaceuticalBillItemFacade().create(i.getPharmaceuticalBillItem());
                } else {
                    getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
                }
            }
        }
        
        // Save bill expenses - work directly with bill's expense collection
        if (getCurrentGrnBillPre().getBillExpenses() != null) {
            for (BillItem expense : getCurrentGrnBillPre().getBillExpenses()) {
                
                // Ensure expenseBill reference is set
                expense.setExpenseBill(getCurrentGrnBillPre());
                expense.setCreatedAt(new Date());
                expense.setCreater(getSessionController().getLoggedUser());
                
                // Create or update expense BillItem (including retired ones to persist retirement status)
                if (expense.getId() == null) {
                    getBillItemFacade().create(expense);
                } else {
                    getBillItemFacade().edit(expense);
                }
            }
        }
        
        // Final bill update to persist the collection
        getBillFacade().edit(getCurrentGrnBillPre());

        // Ensure bill discount distribution before saving (even if 0 to clear previous distributions)
        ensureBillDiscountSynchronization();
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());

        // Update totals - no copying needed since grnBill and currentGrnBillPre are the same object
        calculateBillTotalsFromItems();
        getBillFacade().edit(getCurrentGrnBillPre());

        JsfUtil.addSuccessMessage("GRN Saved");
    }

    public void finalizeGrnWithSaveApprove() {
        // Apply same validations as authorize button
        if (getCurrentGrnBillPre().getInvoiceNumber() == null || getCurrentGrnBillPre().getInvoiceNumber().trim().isEmpty()) {
            JsfUtil.addErrorMessage("Please fill invoice number");
            return;
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
        String msg = pharmacyCalculation.errorCheck(getCurrentGrnBillPre(), getBillItems());
        if (!msg.isEmpty()) {
            JsfUtil.addErrorMessage(msg);
            return;
        }
        
        // First perform the save operation
        requestWithSaveApprove();
        
        // Mark the bill as completed
        getCurrentGrnBillPre().setCompleted(true);
        getCurrentGrnBillPre().setCompletedBy(getSessionController().getLoggedUser());
        getCurrentGrnBillPre().setCompletedAt(new Date());
        
        // Save the completed state
        getBillFacade().edit(getCurrentGrnBillPre());
        
        JsfUtil.addSuccessMessage("GRN Finalized");
    }

    public void requestFinalizeWithSaveApprove() {
        // Always use bill's invoice number, ignore controller reference
        if (getCurrentGrnBillPre().getInvoiceNumber() == null || getCurrentGrnBillPre().getInvoiceNumber().trim().isEmpty()) {
            JsfUtil.addErrorMessage("Please fill invoice number");
            return;
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
        String msg = pharmacyCalculation.errorCheck(getCurrentGrnBillPre(), getBillItems());
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
            BillItemFinanceDetails f = i.getBillItemFinanceDetails();
            if (f == null || (
                (f.getQuantity() == null || f.getQuantity().compareTo(BigDecimal.ZERO) == 0) &&
                (f.getFreeQuantity() == null || f.getFreeQuantity().compareTo(BigDecimal.ZERO) == 0)
            )) {
                continue;
            }

            // Apply standardized finance details conversion
            applyFinanceDetailsToPharmaceutical(i);

            // Create/update item batch for stock management with costing respect
            ItemBatch itemBatch;
            if (configOptionApplicationController.getBooleanValueByKey("Manage Costing", true)) {
                itemBatch = getPharmacyCalculation().saveItemBatchWithCosting(i);
            } else {
                itemBatch = getPharmacyCalculation().saveItemBatch(i);
            }
            i.getPharmaceuticalBillItem().setItemBatch(itemBatch);

            // Create stock record
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

        // Ensure bill discount distribution before final calculations (even if 0 to clear previous distributions)
        ensureBillDiscountSynchronization();
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getGrnBill());

        // Final calculations with costing - no copying needed since grnBill and currentGrnBillPre are the same object
        calculateBillTotalsFromItems();
        pharmacyCalculation.calculateRetailSaleValueAndFreeValueAtPurchaseRate(getCurrentGrnBillPre());

        // Update financial balances
        updateBalanceForGrn(getCurrentGrnBillPre());

        getBillFacade().edit(getCurrentGrnBillPre());

        // Create payment record
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

}
