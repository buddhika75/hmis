/*
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.common.NotificationController;
import com.divudi.bean.common.SessionController;
import com.divudi.core.util.JsfUtil;
import com.divudi.core.data.BillType;
import com.divudi.core.data.BillTypeAtomic;
import com.divudi.core.data.dataStructure.PaymentMethodData;
import com.divudi.core.data.dataStructure.SearchKeyword;
import com.divudi.ejb.BillNumberGenerator;

import com.divudi.ejb.PharmacyBean;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.BillItemFinanceDetails;
import com.divudi.core.entity.BilledBill;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.BillItemFacade;
import com.divudi.core.facade.PharmaceuticalBillItemFacade;
import com.divudi.core.facade.EmailFacade;
import com.divudi.core.util.CommonFunctions;
import com.divudi.ejb.EmailManagerEjb;
import com.divudi.core.entity.AppEmail;
import com.divudi.core.data.MessageType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.LazyDataModel;
import com.divudi.core.entity.pharmacy.Ampp;
import com.divudi.core.entity.pharmacy.Amp;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class PurchaseOrderController implements Serializable {

    @Inject
    private SessionController sessionController;
    @EJB
    private BillFacade billFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private BillNumberGenerator billNumberBean;
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private EmailFacade emailFacade;
    @EJB
    private EmailManagerEjb emailManagerEjb;
    ///////////////
    private Bill requestedBill;
    private Bill aprovedBill;
    private Date fromDate;
    Date toDate;
    private boolean printPreview;
    private String txtSearch;
    /////////////
//    private List<PharmaceuticalBillItem> pharmaceuticalBillItems;
    private List<PharmaceuticalBillItem> filteredValue;
    private List<BillItem> billItems;
    private List<BillItem> selectedItems;
    private List<Bill> billsToApprove;
    private List<Bill> bills;
    private SearchKeyword searchKeyword;
    // private List<BillItem> billItems;
    // List<PharmaceuticalBillItem> pharmaceuticalBillItems;
    //////////

    private LazyDataModel<Bill> searchBills;

    private PaymentMethodData paymentMethodData;
    private double totalBillItemsCount;
    @Inject
    NotificationController notificationController;
    
    private String emailRecipient;

    public void removeSelected() {
        //  //System.err.println("1");
        if (selectedItems == null) {
            JsfUtil.addErrorMessage("Please select items");
            return;
        }

        //System.err.println("3");
        for (BillItem b : selectedItems) {
            //  //System.err.println("4");
            getBillItems().remove(b.getSearialNo());
            calTotal();
        }

        selectedItems = null;
    }

    public void removeItem(BillItem billItem) {
        getBillItems().remove(billItem.getSearialNo());
        calTotal();
    }

    private int maxResult = 50;

    public void clearList() {
        filteredValue = null;
        billsToApprove = null;
        printPreview = true;
        billItems = null;
        aprovedBill = null;
        requestedBill = null;
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

    public String navigateToPurchaseOrderApproval() {
        Bill temRequestedBill = requestedBill;
        clearList();
        requestedBill = temRequestedBill;
        getAprovedBill().setPaymentMethod(getRequestedBill().getPaymentMethod());
        getAprovedBill().setToInstitution(getRequestedBill().getToInstitution());
        getAprovedBill().setCreditDuration(getRequestedBill().getCreditDuration());
        generateBillComponent();
        printPreview = false;
        return "/pharmacy/pharmacy_purhcase_order_approving?faces-redirect=true";
    }

    public String approve() {
        if (getAprovedBill().getPaymentMethod() == null) {
            JsfUtil.addErrorMessage("Select Paymentmethod");
            return "";
        }

        if (billItems == null || billItems.isEmpty()) {
            JsfUtil.addErrorMessage("Please add bill items");
            return "";
        }

        for (BillItem bis : billItems) {
            PharmaceuticalBillItem pbi = bis.getPharmaceuticalBillItem();
            if (pbi == null) {
                JsfUtil.addErrorMessage("Missing pharmaceutical details for item: " + bis.getItem().getName());
                return "";
            }

            double totalQty = pbi.getQty() + pbi.getFreeQty();
            if (totalQty <= 0) {
                JsfUtil.addErrorMessage("Item '" + bis.getItem().getName() + "' has zero quantity and free quantity");
                return "";
            }

            if (pbi.getPurchaseRate() <= 0) {
                JsfUtil.addErrorMessage("Item '" + bis.getItem().getName() + "' has invalid purchase price");
                return "";
            }
        }

        calTotal();
        saveBill();
        saveBillComponent();

        String deptId = billNumberBean.departmentBillNumberGeneratorYearly(
                getSessionController().getDepartment(),
                BillTypeAtomic.PHARMACY_ORDER_APPROVAL
        );

        getAprovedBill().setDeptId(deptId);
        getAprovedBill().setInsId(deptId);
        getAprovedBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_ORDER_APPROVAL);

        billFacade.edit(getAprovedBill());
        notificationController.createNotification(getAprovedBill());

        getRequestedBill().setReferenceBill(getAprovedBill());
        getBillFacade().edit(getRequestedBill());

        printPreview = true;
        return "";
    }

    public String viewRequestedList() {
        clearList();
        return "/pharmacy_purhcase_order_list_to_approve?faces-redirect=true";
    }

    @Inject
    private PharmacyController pharmacyController;

    public void onEdit(BillItem bi) {
        // During approving, only recalculate if BillItemFinanceDetails is missing or incomplete
        // This prevents unnecessary recalculations when data is already correct from request phase
        if (bi.getBillItemFinanceDetails() == null || 
            bi.getBillItemFinanceDetails().getLineNetTotal() == null ||
            bi.getBillItemFinanceDetails().getQuantity() == null ||
            bi.getBillItemFinanceDetails().getLineGrossRate() == null) {
            calculateLineValues(bi);
        } else {
            // Just ensure the values are synchronized for user changes
            updateCalculatedValues(bi);
        }
        calculateBillTotals();
    }

    private void updateCalculatedValues(BillItem lineBillItem) {
        // Quick update without full recalculation - just synchronize user input
        BigDecimal bdQty = lineBillItem.getBillItemFinanceDetails().getQuantity();
        BigDecimal bdFreeQty = lineBillItem.getBillItemFinanceDetails().getFreeQuantity();
        BigDecimal bdPurchaseRate = lineBillItem.getBillItemFinanceDetails().getLineGrossRate();

        if (bdQty == null) bdQty = BigDecimal.ZERO;
        if (bdFreeQty == null) bdFreeQty = BigDecimal.ZERO;
        if (bdPurchaseRate == null) bdPurchaseRate = BigDecimal.ZERO;

        // Recalculate only the essential values that depend on user input
        BigDecimal bdGrossValue = bdPurchaseRate.multiply(bdQty);
        BigDecimal bdNetValue = bdGrossValue;
        
        lineBillItem.getBillItemFinanceDetails().setLineNetTotal(bdNetValue);
        lineBillItem.getBillItemFinanceDetails().setLineGrossTotal(bdGrossValue);
        lineBillItem.setNetValue(bdNetValue.doubleValue());

        // Update PharmaceuticalBillItem values
        double quantity = bdQty.doubleValue();
        double freeQuantity = bdFreeQty.doubleValue();

        if (lineBillItem.getItem() instanceof Ampp) {
            BigDecimal unitsPerPackDecimal = lineBillItem.getBillItemFinanceDetails().getUnitsPerPack();
            double unitsPerPack = (unitsPerPackDecimal == null || unitsPerPackDecimal.doubleValue() == 0) ? 1.0 : unitsPerPackDecimal.doubleValue();

            lineBillItem.getPharmaceuticalBillItem().setQty(quantity * unitsPerPack);
            lineBillItem.getPharmaceuticalBillItem().setFreeQty(freeQuantity * unitsPerPack);
            lineBillItem.getPharmaceuticalBillItem().setPurchaseRate(bdPurchaseRate.doubleValue() / unitsPerPack);
            lineBillItem.getPharmaceuticalBillItem().setPurchaseRatePack(bdPurchaseRate.doubleValue());
        } else {
            lineBillItem.getPharmaceuticalBillItem().setQty(quantity);
            lineBillItem.getPharmaceuticalBillItem().setFreeQty(freeQuantity);
            lineBillItem.getPharmaceuticalBillItem().setPurchaseRate(bdPurchaseRate.doubleValue());
            lineBillItem.getPharmaceuticalBillItem().setPurchaseRatePack(bdPurchaseRate.doubleValue());
        }
    }

    public void calculateLineValues(BillItem lineBillItem) {

        BigDecimal bdQty = lineBillItem.getBillItemFinanceDetails().getQuantity(); // User Input Captured 
        BigDecimal bdFreeQty = lineBillItem.getBillItemFinanceDetails().getFreeQuantity(); // User input captured
        BigDecimal bdPurchaseRate = lineBillItem.getBillItemFinanceDetails().getLineGrossRate(); // User input captured
        BigDecimal bdRetailRate = lineBillItem.getBillItemFinanceDetails().getRetailSaleRate(); // User input captured
        BigDecimal bdUnitsPerPack = lineBillItem.getBillItemFinanceDetails().getUnitsPerPack(); // Taken at Item Add

        // Null safety checks
        if (bdQty == null) {
            bdQty = BigDecimal.ZERO;
        }
        if (bdFreeQty == null) {
            bdFreeQty = BigDecimal.ZERO;
        }
        if (bdPurchaseRate == null) {
            bdPurchaseRate = BigDecimal.ZERO;
        }
        if (bdRetailRate == null) {
            bdRetailRate = BigDecimal.ZERO;
        }
        if (bdUnitsPerPack == null || bdUnitsPerPack.doubleValue() == 0) {
            bdUnitsPerPack = BigDecimal.ONE;
        }

        // Calculate values for BillItemFinanceDetails
        BigDecimal bdGrossValue = bdPurchaseRate.multiply(bdQty); // purchase rate * quantity
        BigDecimal bdNetValue = bdGrossValue; // assign gross value as no discount here
        BigDecimal bdRetailValue = bdRetailRate.multiply(bdQty.add(bdFreeQty)); // retail rate * (qty + free qty)
        BigDecimal bdPurchaseValue = bdPurchaseRate.multiply(bdQty.add(bdFreeQty)); // purchase rate * (qty + free qty)

        // Assign calculated values for BillItemFinanceDetails
        // Since discounts, tax, expenses are zero: net rate = gross rate, net value = gross value
        lineBillItem.getBillItemFinanceDetails().setLineNetRate(bdPurchaseRate);
        lineBillItem.getBillItemFinanceDetails().setLineGrossRate(bdPurchaseRate);
        lineBillItem.getBillItemFinanceDetails().setGrossRate(bdPurchaseRate);

        lineBillItem.getBillItemFinanceDetails().setLineNetTotal(bdNetValue);
        lineBillItem.getBillItemFinanceDetails().setLineGrossTotal(bdGrossValue);
        lineBillItem.getBillItemFinanceDetails().setGrossTotal(bdGrossValue);

        lineBillItem.getBillItemFinanceDetails().setQuantity(bdQty);
        lineBillItem.getBillItemFinanceDetails().setFreeQuantity(bdFreeQty);
        
        // Calculate quantity by units (for AMPP items)
        BigDecimal quantityByUnits;
        if (lineBillItem.getItem() instanceof Ampp) {
            quantityByUnits = bdQty.multiply(bdUnitsPerPack);
        } else {
            quantityByUnits = bdQty;
        }
        lineBillItem.getBillItemFinanceDetails().setQuantityByUnits(quantityByUnits);

        lineBillItem.getBillItemFinanceDetails().setValueAtPurchaseRate(bdPurchaseValue);
        lineBillItem.getBillItemFinanceDetails().setValueAtRetailRate(bdRetailValue);
        
        // Set costing values to zero (not relevant for purchase orders)
        lineBillItem.getBillItemFinanceDetails().setLineCost(BigDecimal.ZERO);
        lineBillItem.getBillItemFinanceDetails().setLineCostRate(BigDecimal.ZERO);
        lineBillItem.getBillItemFinanceDetails().setValueAtCostRate(BigDecimal.ZERO);
        
        // Set audit fields for BillItemFinanceDetails
        if (lineBillItem.getBillItemFinanceDetails().getId() == null) {
            lineBillItem.getBillItemFinanceDetails().setCreatedAt(new Date());
            // Note: BillItemFinanceDetails may not have setCreater method, skip if not available
        }

        // Set audit fields for PharmaceuticalBillItem (set if null, regardless of ID)
        if (lineBillItem.getPharmaceuticalBillItem().getCreatedAt() == null) {
            lineBillItem.getPharmaceuticalBillItem().setCreatedAt(new Date());
        }
        if (lineBillItem.getPharmaceuticalBillItem().getCreater() == null) {
            lineBillItem.getPharmaceuticalBillItem().setCreater(sessionController.getLoggedUser());
        }

        // Set audit fields for BillItem
        if (lineBillItem.getId() == null) {
            lineBillItem.setCreatedAt(new Date());
            lineBillItem.setCreater(sessionController.getLoggedUser());
        }

        // Update pharmaceuticalBillItem quantities and rates
        double quantity = bdQty.doubleValue();
        double freeQuantity = bdFreeQty.doubleValue();
        double unitsPerPack = bdUnitsPerPack.doubleValue();

        if (lineBillItem.getItem() instanceof Ampp) {
            // For AMPP items, billItemFinanceDetails stores packs, convert to units
            lineBillItem.getPharmaceuticalBillItem().setQty(quantity * unitsPerPack);
            lineBillItem.getPharmaceuticalBillItem().setFreeQty(freeQuantity * unitsPerPack);

            lineBillItem.getPharmaceuticalBillItem().setPurchaseRate(bdPurchaseRate.doubleValue() / unitsPerPack);
            lineBillItem.getPharmaceuticalBillItem().setPurchaseRatePack(bdPurchaseRate.doubleValue());
            lineBillItem.getPharmaceuticalBillItem().setPurchaseValue(bdPurchaseValue.doubleValue());
        } else {
            // For AMP items, billItemFinanceDetails and pharmaceuticalBillItem use same units
            lineBillItem.getPharmaceuticalBillItem().setQty(quantity);
            lineBillItem.getPharmaceuticalBillItem().setFreeQty(freeQuantity);

            lineBillItem.getPharmaceuticalBillItem().setPurchaseRate(bdPurchaseRate.doubleValue());
            lineBillItem.getPharmaceuticalBillItem().setPurchaseRatePack(bdPurchaseRate.doubleValue()); // For AMP, rate per pack = rate per unit
            lineBillItem.getPharmaceuticalBillItem().setPurchaseValue(bdPurchaseValue.doubleValue());
        }

        // Set netValue for legacy compatibility
        lineBillItem.setNetValue(bdNetValue.doubleValue());
    }

    public void onFocus(BillItem ph) {
        getPharmacyController().setPharmacyItem(ph.getItem());
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public Bill getRequestedBill() {
        if (requestedBill == null) {
            requestedBill = new BilledBill();
        }
        return requestedBill;
    }

    public void saveBill() {

        getAprovedBill().setCreditDuration(getAprovedBill().getCreditDuration());
//        getAprovedBill().setPaymentMethod(getRequestedBill().getPaymentMethod());
        getAprovedBill().setFromDepartment(getRequestedBill().getDepartment());
        getAprovedBill().setFromInstitution(getRequestedBill().getInstitution());
        getAprovedBill().setReferenceBill(getRequestedBill());
        getAprovedBill().setBackwardReferenceBill(getRequestedBill());

        getAprovedBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getAprovedBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getAprovedBill().setCreater(getSessionController().getLoggedUser());
        getAprovedBill().setCreatedAt(Calendar.getInstance().getTime());

        getAprovedBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_ORDER_APPROVAL);

        try {
            if (getAprovedBill().getId() == null) {
                getBillFacade().create(getAprovedBill());
            } else {
                getBillFacade().edit(getAprovedBill());
            }
        } catch (Exception e) {
        }

    }

    public void saveBillComponent() {
        for (BillItem i : getBillItems()) {
            i.setBill(getAprovedBill());
            i.setCreatedAt(Calendar.getInstance().getTime());
            i.setCreater(getSessionController().getLoggedUser());
            i.setNetValue(i.getPharmaceuticalBillItem().getQty() * i.getPharmaceuticalBillItem().getPurchaseRate());

            double qty;
            qty = i.getQty() + i.getPharmaceuticalBillItem().getFreeQty();
            if (qty <= 0.0) {
                i.setRetired(true);
                i.setRetirer(sessionController.getLoggedUser());
                i.setRetiredAt(new Date());
                i.setRetireComments("Retired at Approving PO");

            }
//            totalBillItemsCount = totalBillItemsCount + qty;
            PharmaceuticalBillItem phItem = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);
            try {
                if (i.getId() == null) {
                    getBillItemFacade().create(i);
                } else {
                    getBillItemFacade().edit(i);
                }
            } catch (Exception e) {
            }

            phItem.setBillItem(i);

            try {
                if (phItem.getId() == null) {
                    getPharmaceuticalBillItemFacade().create(phItem);
                } else {
                    getPharmaceuticalBillItemFacade().edit(phItem);
                }
            } catch (Exception e) {
            }

            i.setPharmaceuticalBillItem(phItem);
            try {
                getBillItemFacade().edit(i);
            } catch (Exception e) {

            }
            getAprovedBill().getBillItems().add(i);
        }

        getBillFacade().edit(getAprovedBill());
    }

    public void generateBillComponent() {

        for (PharmaceuticalBillItem i : getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(getRequestedBill())) {
            BillItem bi = new BillItem();
            bi.copy(i.getBillItem());

            PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
            ph.setBillItem(bi);
            
            // Set audit fields for the new PharmaceuticalBillItem
            ph.setCreatedAt(new Date());
            ph.setCreater(sessionController.getLoggedUser());

            ph.setFreeQty(i.getFreeQty());
            ph.setQty(i.getQty());
            ph.setPurchaseRate(i.getPurchaseRate());
            ph.setPurchaseRatePack(i.getPurchaseRatePack());
            ph.setRetailRate(i.getRetailRate());
            ph.setPurchaseValue(i.getPurchaseValue());
            bi.setPharmaceuticalBillItem(ph);

            // Create a new BillItemFinanceDetails instance and copy values from the original request
            if (i.getBillItem().getBillItemFinanceDetails() != null) {
                BillItemFinanceDetails originalBifd = i.getBillItem().getBillItemFinanceDetails();
                BillItemFinanceDetails newBifd = bi.getBillItemFinanceDetails();
                
                // Copy all values from original to new instance
                newBifd.setUnitsPerPack(originalBifd.getUnitsPerPack());
                newBifd.setLineGrossRate(originalBifd.getLineGrossRate());
                newBifd.setBillGrossRate(originalBifd.getBillGrossRate());
                newBifd.setGrossRate(originalBifd.getGrossRate());
                newBifd.setLineNetRate(originalBifd.getLineNetRate());
                newBifd.setBillNetRate(originalBifd.getBillNetRate());
                newBifd.setNetRate(originalBifd.getNetRate());
                newBifd.setLineDiscountRate(originalBifd.getLineDiscountRate());
                newBifd.setBillDiscountRate(originalBifd.getBillDiscountRate());
                newBifd.setTotalDiscountRate(originalBifd.getTotalDiscountRate());
                newBifd.setLineExpenseRate(originalBifd.getLineExpenseRate());
                newBifd.setBillExpenseRate(originalBifd.getBillExpenseRate());
                newBifd.setTotalExpenseRate(originalBifd.getTotalExpenseRate());
                newBifd.setBillTaxRate(originalBifd.getBillTaxRate());
                newBifd.setLineTaxRate(originalBifd.getLineTaxRate());
                newBifd.setTotalTaxRate(originalBifd.getTotalTaxRate());
                newBifd.setBillCostRate(originalBifd.getBillCostRate());
                newBifd.setLineCostRate(originalBifd.getLineCostRate());
                newBifd.setTotalCostRate(originalBifd.getTotalCostRate());
                newBifd.setLineGrossTotal(originalBifd.getLineGrossTotal());
                newBifd.setBillGrossTotal(originalBifd.getBillGrossTotal());
                newBifd.setGrossTotal(originalBifd.getGrossTotal());
                newBifd.setLineNetTotal(originalBifd.getLineNetTotal());
                newBifd.setBillNetTotal(originalBifd.getBillNetTotal());
                newBifd.setNetTotal(originalBifd.getNetTotal());
                newBifd.setQuantity(originalBifd.getQuantity());
                newBifd.setFreeQuantity(originalBifd.getFreeQuantity());
                newBifd.setQuantityByUnits(originalBifd.getQuantityByUnits());
                newBifd.setLineCost(originalBifd.getLineCost());
                newBifd.setLineCostRate(originalBifd.getLineCostRate());
                newBifd.setValueAtCostRate(originalBifd.getValueAtCostRate());
                newBifd.setValueAtPurchaseRate(originalBifd.getValueAtPurchaseRate());
                newBifd.setValueAtRetailRate(originalBifd.getValueAtRetailRate());
                newBifd.setRetailSaleRate(originalBifd.getRetailSaleRate());
                newBifd.setWholesaleRate(originalBifd.getWholesaleRate());
                
                // Set the new instance (this will automatically set the bidirectional relationship)
                bi.setBillItemFinanceDetails(newBifd);
            }

            // Set the netValue from BillItemFinanceDetails for display compatibility
            if (bi.getBillItemFinanceDetails() != null && bi.getBillItemFinanceDetails().getLineNetTotal() != null) {
                bi.setNetValue(bi.getBillItemFinanceDetails().getLineNetTotal().doubleValue());
            } else {
                // Fallback to legacy calculation if BillItemFinanceDetails is missing
                bi.setNetValue(ph.getQty() * ph.getPurchaseRate());
            }

            getBillItems().add(bi);
        }

        calculateBillTotals();

    }

    public void setRequestedBill(Bill requestedBill) {
        // The logic inside getter was taken to the navigator method
//        clearList();
        this.requestedBill = requestedBill;
//        getAprovedBill().setPaymentMethod(getRequestedBill().getPaymentMethod());
//        getAprovedBill().setToInstitution(getRequestedBill().getToInstitution());
//        getAprovedBill().setCreditDuration(getRequestedBill().getCreditDuration());
//        generateBillComponent();
    }

    public Bill getAprovedBill() {
        if (aprovedBill == null) {
            aprovedBill = new BilledBill();
            aprovedBill.setBillType(BillType.PharmacyOrderApprove);
            aprovedBill.setBillTypeAtomic(BillTypeAtomic.PHARMACY_ORDER_APPROVAL);
            aprovedBill.setConsignment(getRequestedBill() != null && getRequestedBill().isConsignment());
        }
        return aprovedBill;
    }

    public void setAprovedBill(Bill aprovedBill) {
        this.aprovedBill = aprovedBill;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
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

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public void calculateBillTotals() {
        BigDecimal billNetTotal = BigDecimal.ZERO;
        BigDecimal billGrossTotal = BigDecimal.ZERO;
        BigDecimal totalPurchaseValueFree = BigDecimal.ZERO;
        BigDecimal totalPurchaseValueNonFree = BigDecimal.ZERO;
        BigDecimal totalRetailSaleValueFree = BigDecimal.ZERO;
        BigDecimal totalRetailSaleValueNonFree = BigDecimal.ZERO;
        int serialNo = 0;

        for (BillItem handlingBillItem : getBillItems()) {
            if (handlingBillItem == null || handlingBillItem.isRetired()) {
                continue;
            }

            // Set serial number
            handlingBillItem.setSearialNo(serialNo++);

            // Collect totals from BillItemFinanceDetails
            if (handlingBillItem.getBillItemFinanceDetails() != null) {
                BigDecimal lineNetTotal = handlingBillItem.getBillItemFinanceDetails().getLineNetTotal();
                BigDecimal lineGrossTotal = handlingBillItem.getBillItemFinanceDetails().getLineGrossTotal();
                BigDecimal purchaseValue = handlingBillItem.getBillItemFinanceDetails().getValueAtPurchaseRate();
                BigDecimal retailValue = handlingBillItem.getBillItemFinanceDetails().getValueAtRetailRate();

                if (lineNetTotal != null) {
                    billNetTotal = billNetTotal.add(lineNetTotal);
                }
                if (lineGrossTotal != null) {
                    billGrossTotal = billGrossTotal.add(lineGrossTotal);
                }
                if (purchaseValue != null) {
                    totalPurchaseValueNonFree = totalPurchaseValueNonFree.add(purchaseValue);
                }
                if (retailValue != null) {
                    totalRetailSaleValueNonFree = totalRetailSaleValueNonFree.add(retailValue);
                }
            }

            // Set the netValue for display compatibility
            if (handlingBillItem.getBillItemFinanceDetails() != null && 
                handlingBillItem.getBillItemFinanceDetails().getLineNetTotal() != null) {
                handlingBillItem.setNetValue(handlingBillItem.getBillItemFinanceDetails().getLineNetTotal().doubleValue());
            }
        }

        // Update bill totals
        getAprovedBill().setTotal(billGrossTotal.doubleValue());
        getAprovedBill().setNetTotal(billNetTotal.doubleValue());
    }

    // Maintain backward compatibility
    public void calTotal() {
        calculateBillTotals();
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public List<PharmaceuticalBillItem> getFilteredValue() {
        return filteredValue;
    }

    public void setFilteredValue(List<PharmaceuticalBillItem> filteredValue) {
        this.filteredValue = filteredValue;
    }

    public List<Bill> getBillsToApprove() {
        return billsToApprove;
    }

    public void setBillsToApprove(List<Bill> billsToApprove) {
        this.billsToApprove = billsToApprove;
    }

    public PharmacyController getPharmacyController() {
        return pharmacyController;
    }

    public void setPharmacyController(PharmacyController pharmacyController) {
        this.pharmacyController = pharmacyController;
    }

    public boolean getPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public void makeListNull() {
//        pharmaceuticalBillItems = null;
        filteredValue = null;
        billsToApprove = null;
        searchBills = null;
        billItems = null;
        bills = null;
        maxResult = 50;

    }

    public LazyDataModel<Bill> getSearchBills() {
        return searchBills;
    }

    public void setSearchBills(LazyDataModel<Bill> searchBills) {
        this.searchBills = searchBills;
    }

    public String getTxtSearch() {
        return txtSearch;
    }

    public void setTxtSearch(String txtSearch) {
        this.txtSearch = txtSearch;
    }

    public List<BillItem> getBillItems() {
        if (billItems == null) {
            billItems = new ArrayList<>();
        }
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public List<BillItem> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<BillItem> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
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

    public int getMaxResult() {
        return maxResult;
    }

    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    public PaymentMethodData getPaymentMethodData() {
        if (paymentMethodData == null) {
            paymentMethodData = new PaymentMethodData();
        }
        return paymentMethodData;
    }

    public void setPaymentMethodData(PaymentMethodData paymentMethodData) {
        this.paymentMethodData = paymentMethodData;
    }

    public double getTotalBillItemsCount() {
        return totalBillItemsCount;
    }

    public void setTotalBillItemsCount(double totalBillItemsCount) {
        this.totalBillItemsCount = totalBillItemsCount;
    }

    public void prepareEmailDialog() {
        if (aprovedBill == null) {
            JsfUtil.addErrorMessage("No Bill");
            return;
        }
        
        // Set default email if available
        if (aprovedBill.getToInstitution() != null && aprovedBill.getToInstitution().getEmail() != null) {
            emailRecipient = aprovedBill.getToInstitution().getEmail();
        } else {
            emailRecipient = "";
        }
    }

    public void sendPurchaseOrderEmail() {
        if (aprovedBill == null) {
            JsfUtil.addErrorMessage("No Bill");
            return;
        }
        
        if (emailRecipient == null || emailRecipient.trim().isEmpty()) {
            JsfUtil.addErrorMessage("Please enter recipient email");
            return;
        }

        String recipient = emailRecipient.trim();
        if (!CommonFunctions.isValidEmail(recipient)) {
            JsfUtil.addErrorMessage("Please enter a valid email address");
            return;
        }

        String body = generatePurchaseOrderHtml();
        if (body == null) {
            JsfUtil.addErrorMessage("Could not generate email body");
            return;
        }

        AppEmail email = new AppEmail();
        email.setCreatedAt(new Date());
        email.setCreater(sessionController.getLoggedUser());
        email.setReceipientEmail(recipient);
        email.setMessageSubject("Purchase Order");
        email.setMessageBody(body);
        email.setDepartment(sessionController.getLoggedUser().getDepartment());
        email.setInstitution(sessionController.getLoggedUser().getInstitution());
        email.setBill(aprovedBill);
        email.setMessageType(MessageType.Marketing);
        email.setSentSuccessfully(false);
        email.setPending(true);
        emailFacade.create(email);

        try {
            boolean success = emailManagerEjb.sendEmail(
                    java.util.Collections.singletonList(recipient),
                    body,
                    "Purchase Order",
                    true
            );
            email.setSentSuccessfully(success);
            email.setPending(!success);
            if (success) {
                email.setSentAt(new Date());
                JsfUtil.addSuccessMessage("Email Sent Successfully");
            } else {
                JsfUtil.addErrorMessage("Sending Email Failed");
            }
            emailFacade.edit(email);
        } catch (Exception ex) {
            JsfUtil.addErrorMessage("Sending Email Failed");
        }
    }

    private String generatePurchaseOrderHtml() {
        try {
            if (aprovedBill == null) {
                return null;
            }
            
            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>Purchase Order</title></head><body>");
            html.append("<div style='font-family: Arial, sans-serif; padding: 20px;'>");
            
            // Institution header
            if (aprovedBill.getCreater() != null && aprovedBill.getCreater().getInstitution() != null) {
                html.append("<div style='text-align: center; margin-bottom: 20px;'>");
                html.append("<h2>").append(aprovedBill.getCreater().getInstitution().getName() != null ? aprovedBill.getCreater().getInstitution().getName() : "").append("</h2>");
                if (aprovedBill.getCreater().getInstitution().getAddress() != null) {
                    html.append("<p>").append(aprovedBill.getCreater().getInstitution().getAddress()).append("</p>");
                }
                if (aprovedBill.getCreater().getInstitution().getPhone() != null) {
                    html.append("<p>Phone: ").append(aprovedBill.getCreater().getInstitution().getPhone()).append("</p>");
                }
                html.append("</div>");
            }
            
            html.append("<h3 style='text-align: center; text-decoration: underline;'>Purchase Order</h3>");
            
            // Order details
            html.append("<table style='width: 100%; margin-bottom: 20px;'>");
            html.append("<tr><td><strong>Order No:</strong></td><td>").append(aprovedBill.getDeptId() != null ? aprovedBill.getDeptId() : "").append("</td></tr>");
            if (aprovedBill.getDepartment() != null) {
                html.append("<tr><td><strong>Order Department:</strong></td><td>").append(aprovedBill.getDepartment().getName() != null ? aprovedBill.getDepartment().getName() : "").append("</td></tr>");
            }
            if (aprovedBill.getToInstitution() != null) {
                html.append("<tr><td><strong>Supplier:</strong></td><td>").append(aprovedBill.getToInstitution().getName() != null ? aprovedBill.getToInstitution().getName() : "").append("</td></tr>");
                html.append("<tr><td><strong>Supplier Code:</strong></td><td>").append(aprovedBill.getToInstitution().getCode() != null ? aprovedBill.getToInstitution().getCode() : "").append("</td></tr>");
                if (aprovedBill.getToInstitution().getPhone() != null) {
                    html.append("<tr><td><strong>Supplier Phone:</strong></td><td>").append(aprovedBill.getToInstitution().getPhone()).append("</td></tr>");
                }
                if (aprovedBill.getToInstitution().getAddress() != null) {
                    html.append("<tr><td><strong>Supplier Address:</strong></td><td>").append(aprovedBill.getToInstitution().getAddress()).append("</td></tr>");
                }
            }
            html.append("<tr><td><strong>Payment Method:</strong></td><td>").append(aprovedBill.getPaymentMethod() != null ? aprovedBill.getPaymentMethod().toString() : "").append("</td></tr>");
            html.append("<tr><td><strong>Consignment:</strong></td><td>").append(aprovedBill.isConsignment() ? "Yes" : "No").append("</td></tr>");
            html.append("</table>");
            
            // Items table
            html.append("<table border='1' style='width: 100%; border-collapse: collapse; margin-bottom: 20px;'>");
            html.append("<thead style='background-color: #f0f0f0;'>");
            html.append("<tr>");
            html.append("<th style='padding: 8px;'>Item Code</th>");
            html.append("<th style='padding: 8px;'>Item Name</th>");
            html.append("<th style='padding: 8px;'>Qty</th>");
            html.append("<th style='padding: 8px;'>Free Qty</th>");
            html.append("<th style='padding: 8px;'>Purchase Rate</th>");
            html.append("<th style='padding: 8px;'>Purchase Value</th>");
            html.append("</tr></thead><tbody>");
            
            if (billItems != null) {
                for (BillItem bi : billItems) {
                    if (bi != null && !bi.isRetired() && bi.getItem() != null) {
                        html.append("<tr>");
                        html.append("<td style='padding: 8px;'>").append(bi.getItem().getCode() != null ? bi.getItem().getCode() : "").append("</td>");
                        html.append("<td style='padding: 8px;'>").append(bi.getItem().getName() != null ? bi.getItem().getName() : "").append("</td>");
                        html.append("<td style='padding: 8px; text-align: right;'>");
                        if (bi.getPharmaceuticalBillItem() != null) {
                            html.append(String.format("%,.0f", bi.getPharmaceuticalBillItem().getQty()));
                        }
                        html.append("</td>");
                        html.append("<td style='padding: 8px; text-align: right;'>");
                        if (bi.getPharmaceuticalBillItem() != null) {
                            html.append(String.format("%,.0f", bi.getPharmaceuticalBillItem().getFreeQty()));
                        }
                        html.append("</td>");
                        html.append("<td style='padding: 8px; text-align: right;'>");
                        if (bi.getPharmaceuticalBillItem() != null) {
                            html.append(String.format("%,.2f", bi.getPharmaceuticalBillItem().getPurchaseRate()));
                        }
                        html.append("</td>");
                        html.append("<td style='padding: 8px; text-align: right;'>").append(String.format("%,.2f", bi.getNetValue())).append("</td>");
                        html.append("</tr>");
                    }
                }
            }
            
            html.append("</tbody>");
            html.append("<tfoot style='font-weight: bold;'>");
            html.append("<tr>");
            html.append("<td colspan='5' style='padding: 8px; text-align: right;'>Net Total:</td>");
            html.append("<td style='padding: 8px; text-align: right;'>").append(String.format("%,.2f", aprovedBill.getNetTotal())).append("</td>");
            html.append("</tr></tfoot></table>");
            
            // Footer details
            html.append("<div style='margin-top: 20px;'>");
            if (aprovedBill.getCreater() != null && aprovedBill.getCreater().getWebUserPerson() != null) {
                html.append("<p><strong>Order Initiated By:</strong> ").append(aprovedBill.getCreater().getWebUserPerson().getName() != null ? aprovedBill.getCreater().getWebUserPerson().getName() : "").append("</p>");
            }
            if (aprovedBill.getCheckedBy() != null) {
                html.append("<p><strong>Order Finalized By:</strong> ").append(aprovedBill.getCheckedBy().getName() != null ? aprovedBill.getCheckedBy().getName() : "").append("</p>");
            }
            if (aprovedBill.getCheckeAt() != null) {
                html.append("<p><strong>Order Finalized At:</strong> ").append(CommonFunctions.formatDate(aprovedBill.getCheckeAt(), "dd/MM/yyyy HH:mm:ss")).append("</p>");
            }
            html.append("<p><strong>Generated At:</strong> ").append(CommonFunctions.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss")).append("</p>");
            html.append("<p><strong>Total:</strong> ").append(String.format("%,.2f", aprovedBill.getNetTotal())).append("</p>");
            html.append("</div>");
            
            html.append("</div></body></html>");
            return html.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public EmailFacade getEmailFacade() {
        return emailFacade;
    }

    public void setEmailFacade(EmailFacade emailFacade) {
        this.emailFacade = emailFacade;
    }

    public EmailManagerEjb getEmailManagerEjb() {
        return emailManagerEjb;
    }

    public void setEmailManagerEjb(EmailManagerEjb emailManagerEjb) {
        this.emailManagerEjb = emailManagerEjb;
    }

    public String getEmailRecipient() {
        return emailRecipient;
    }

    public void setEmailRecipient(String emailRecipient) {
        this.emailRecipient = emailRecipient;
    }

}
