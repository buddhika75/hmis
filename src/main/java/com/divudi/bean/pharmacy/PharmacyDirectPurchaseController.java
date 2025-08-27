/*
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.common.ConfigOptionApplicationController;
import com.divudi.bean.common.SessionController;
import com.divudi.core.util.JsfUtil;
import com.divudi.core.data.BillType;
import com.divudi.core.data.BillTypeAtomic;
import com.divudi.core.data.PaymentMethod;
import com.divudi.core.data.dataStructure.BillListWithTotals;
import com.divudi.core.data.dataStructure.PaymentMethodData;
import com.divudi.core.data.dataStructure.PharmacyStockRow;
import com.divudi.ejb.BillEjb;
import com.divudi.ejb.BillNumberGenerator;
import com.divudi.ejb.CashTransactionBean;
import com.divudi.ejb.PharmacyBean;
import com.divudi.ejb.PharmacyCalculation;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillFee;
import com.divudi.core.entity.BillFinanceDetails;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.BillItemFinanceDetails;
import com.divudi.core.entity.BillNumber;
import com.divudi.core.entity.BilledBill;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.Institution;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.Payment;
import com.divudi.core.entity.pharmacy.Ampp;
import com.divudi.core.entity.pharmacy.ItemBatch;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.entity.pharmacy.Stock;
import com.divudi.core.facade.AmpFacade;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.BillFeeFacade;
import com.divudi.core.facade.BillItemFacade;
import com.divudi.core.facade.PaymentFacade;
import com.divudi.core.facade.PharmaceuticalBillItemFacade;
import com.divudi.core.util.CommonFunctions;
import com.divudi.service.PaymentService;
import com.divudi.service.pharmacy.PharmacyCostingService;
import com.divudi.core.util.BigDecimalUtil;
import com.divudi.bean.pharmacy.PharmacyController;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author Buddhika
 */
@Named
@SessionScoped
public class PharmacyDirectPurchaseController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    // </editor-fold>  
    // <editor-fold defaultstate="collapsed" desc="Controllers">
    // </editor-fold>  
    // <editor-fold defaultstate="collapsed" desc="Class Variables">
    private BilledBill bill;
    private List<BillItem> billItems;
    private BillItem currentBillItem;
    private boolean printPreview;
    private boolean showAllBillFormats = false;
    private BillItem currentExpense;
    private List<BillItem> billExpenses;
    private String warningMessage;

    // </editor-fold>  
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    // </editor-fold>  
    // <editor-fold defaultstate="collapsed" desc="Navigation Methods">
    public String navigateToStartNewDirectPurchaseBill() {
        prepareForNewDIrectPurchaseBill();
        return "/pharmacy/direct_purchase?faces-redirect=true";
    }

    // </editor-fold>  
    // <editor-fold defaultstate="collapsed" desc="Functions">
    public void prepareForNewDIrectPurchaseBill() {
        printPreview = false;
        currentBillItem = null;
        bill = null;
        billItems = null;
        billExpenses = null;
        currentExpense = null;
        warningMessage = null;
    }

    public void addItem() {
        Item item = getCurrentBillItem().getItem();
        BillItemFinanceDetails f = getCurrentBillItem().getBillItemFinanceDetails();
        PharmaceuticalBillItem pbi = getCurrentBillItem().getPharmaceuticalBillItem();

        if (item == null) {
            JsfUtil.addErrorMessage("Please select an item");
            return;
        }

        if (f == null || pbi == null) {
            JsfUtil.addErrorMessage("Invalid internal structure. Cannot proceed.");
            return;
        }

        if (BigDecimalUtil.isNullOrZero(f.getQuantity()) || BigDecimalUtil.isNegative(f.getQuantity())) {
            JsfUtil.addErrorMessage("Please enter quantity");
            return;
        }

        if (BigDecimalUtil.isNullOrZero(f.getLineGrossRate()) || BigDecimalUtil.isNegative(f.getLineGrossRate())) {
            JsfUtil.addErrorMessage("Please enter the purchase rate");
            return;
        }

        if (BigDecimalUtil.isNullOrZero(f.getRetailSaleRatePerUnit()) || BigDecimalUtil.isNegative(f.getRetailSaleRatePerUnit())) {
            JsfUtil.addErrorMessage("Please enter the sale rate");
            return;
        }

        if (pbi.getDoe() == null) {
            JsfUtil.addErrorMessage("Please set the date of expiry");
            return;
        }

        if (pbi.getDoe() != null) {
            if (pbi.getDoe().getTime() < Calendar.getInstance().getTimeInMillis()) {
                JsfUtil.addErrorMessage("Check Date of Expiry");
                return;
            }
        }
        if (getBill().getId() == null) {
            getBillFacade().create(getBill());
        }

        // Custom financial calculations instead of using service (to avoid override issues)
        // We'll calculate everything manually for proper AMP/AMPP handling
        
        BigDecimal qty = BigDecimalUtil.valueOrZero(f.getQuantity());
        BigDecimal freeQty = BigDecimalUtil.valueOrZero(f.getFreeQuantity());
        
        
        // Calculate basic financial fields first
        if (item instanceof Ampp) {
            // AMPP: User enters packs, but we need both pack and unit calculations
            BigDecimal unitsPerPack = Optional.ofNullable(f.getUnitsPerPack()).orElse(BigDecimal.ONE);
            BigDecimal totalQtyUnits = BigDecimalUtil.multiply(qty, unitsPerPack);
            BigDecimal totalFreeQtyUnits = BigDecimalUtil.multiply(freeQty, unitsPerPack);
            
            // Set quantity fields
            f.setQuantityByUnits(totalQtyUnits);
            f.setFreeQuantityByUnits(totalFreeQtyUnits);
            f.setTotalQuantityByUnits(BigDecimalUtil.add(totalQtyUnits, totalFreeQtyUnits));
            
            // Set rates - lineGrossRate is per pack (user input), grossRate should be per unit
            BigDecimal packRate = BigDecimalUtil.valueOrZero(f.getLineGrossRate());
            f.setGrossRate(packRate.divide(unitsPerPack, 4, BigDecimal.ROUND_HALF_UP));
            
            
            // Calculate line totals using pack-based rates
            f.setLineGrossTotal(BigDecimalUtil.multiply(qty, packRate));
            
            // Apply discount if any
            BigDecimal discountRate = BigDecimalUtil.valueOrZero(f.getLineDiscountRate());
            // For AMPP: discount is per pack, so multiply discountRate by quantity (packs)
            BigDecimal lineDiscount = BigDecimalUtil.multiply(discountRate, qty);
            f.setLineDiscount(lineDiscount);
            f.setLineNetTotal(BigDecimalUtil.subtract(f.getLineGrossTotal(), lineDiscount));
            f.setLineNetRate(f.getLineNetTotal().divide(qty, 4, BigDecimal.ROUND_HALF_UP));
            
            
            // Set retail rates - convert to pack rate if needed
            BigDecimal retailRatePerUnit = BigDecimalUtil.valueOrZero(f.getRetailSaleRatePerUnit());
            f.setRetailSaleRate(BigDecimalUtil.multiply(retailRatePerUnit, unitsPerPack));
            
            // Calculate cost values
            if (f.getLineCostRate() != null) {
                BigDecimal packCostRate = BigDecimalUtil.multiply(f.getLineCostRate(), unitsPerPack);
                f.setLineCost(BigDecimalUtil.multiply(qty, packCostRate));
                f.setValueAtCostRate(f.getLineCost());
                f.setTotalCostRate(packCostRate);  // Set per-pack cost rate for AMPP
            } else {
                // Set default cost rate to prevent NPE
                f.setTotalCostRate(f.getGrossRate());
            }
            
            // Calculate value fields
            f.setValueAtPurchaseRate(f.getLineGrossTotal());
            f.setValueAtRetailRate(BigDecimalUtil.multiply(qty, f.getRetailSaleRate()));
            
        } else {
            // AMP: User enters units, everything is unit-based
            f.setQuantityByUnits(qty);
            f.setFreeQuantityByUnits(freeQty);
            f.setTotalQuantityByUnits(BigDecimalUtil.add(qty, freeQty));
            
            // Set rates - both lineGrossRate and grossRate should be per unit for AMP
            f.setGrossRate(f.getLineGrossRate());
            
            
            // Calculate line totals
            f.setLineGrossTotal(BigDecimalUtil.multiply(qty, f.getLineGrossRate()));
            
            // Apply discount if any
            BigDecimal discountRate = BigDecimalUtil.valueOrZero(f.getLineDiscountRate());
            // For AMP: discount is per unit, so multiply discountRate by quantity (units)
            BigDecimal lineDiscount = BigDecimalUtil.multiply(discountRate, qty);
            f.setLineDiscount(lineDiscount);
            f.setLineNetTotal(BigDecimalUtil.subtract(f.getLineGrossTotal(), lineDiscount));
            f.setLineNetRate(f.getLineNetTotal().divide(qty, 4, BigDecimal.ROUND_HALF_UP));
            
            
            // Retail rate is already per unit
            f.setRetailSaleRate(f.getRetailSaleRatePerUnit());
            
            // Calculate cost values
            if (f.getLineCostRate() != null) {
                f.setLineCost(BigDecimalUtil.multiply(qty, f.getLineCostRate()));
                f.setValueAtCostRate(f.getLineCost());
                f.setTotalCostRate(f.getLineCostRate());  // Set per-unit cost rate for AMP
            } else {
                // Set default cost rate to prevent NPE
                f.setTotalCostRate(f.getGrossRate());
            }
            
            // Calculate value fields
            f.setValueAtPurchaseRate(f.getLineGrossTotal());
            f.setValueAtRetailRate(BigDecimalUtil.multiply(qty, f.getRetailSaleRatePerUnit()));
        }

        // Set PharmaceuticalBillItem values based on calculated finance details
        if (item instanceof Ampp) {
            // AMPP: Quantities in units, rates as calculated
            pbi.setQty(f.getQuantityByUnits().doubleValue());
            pbi.setQtyPacks(f.getQuantity().doubleValue());
            pbi.setFreeQty(f.getFreeQuantityByUnits().doubleValue());
            pbi.setFreeQtyPacks(f.getFreeQuantity().doubleValue());

            // Set rates - use per-unit rates for PharmaceuticalBillItem
            pbi.setPurchaseRate(f.getGrossRate().doubleValue());  // per unit rate
            pbi.setPurchaseRatePack(f.getLineGrossRate().doubleValue());  // per pack rate
            pbi.setRetailRate(f.getRetailSaleRatePerUnit().doubleValue());
            pbi.setRetailRatePack(f.getRetailSaleRate().doubleValue());
            pbi.setRetailRateInUnit(f.getRetailSaleRatePerUnit().doubleValue());

            // Calculate values using unit quantities and unit rates
            pbi.setPurchaseValue(f.getQuantityByUnits().doubleValue() * f.getGrossRate().doubleValue());
            pbi.setRetailValue(f.getQuantityByUnits().doubleValue() * f.getRetailSaleRatePerUnit().doubleValue());

        } else {
            // AMP: Everything is unit-based
            pbi.setQty(f.getQuantityByUnits().doubleValue());
            pbi.setQtyPacks(f.getQuantityByUnits().doubleValue());
            pbi.setFreeQty(f.getFreeQuantityByUnits().doubleValue());
            pbi.setFreeQtyPacks(f.getFreeQuantityByUnits().doubleValue());

            // Set rates - all per unit for AMP
            pbi.setPurchaseRate(f.getLineGrossRate().doubleValue());
            pbi.setPurchaseRatePack(f.getLineGrossRate().doubleValue());
            pbi.setRetailRate(f.getRetailSaleRatePerUnit().doubleValue());
            pbi.setRetailRatePack(f.getRetailSaleRatePerUnit().doubleValue());
            pbi.setRetailRateInUnit(f.getRetailSaleRatePerUnit().doubleValue());

            // Calculate values
            pbi.setPurchaseValue(f.getQuantityByUnits().doubleValue() * f.getLineGrossRate().doubleValue());
            pbi.setRetailValue(f.getQuantityByUnits().doubleValue() * f.getRetailSaleRatePerUnit().doubleValue());
        }

        // Set BillItem rate and value fields
        getCurrentBillItem().setRate(BigDecimalUtil.valueOrZero(f.getLineGrossRate()).doubleValue());
        getCurrentBillItem().setNetRate(BigDecimalUtil.valueOrZero(f.getLineNetRate()).doubleValue());
        getCurrentBillItem().setNetValue(getCurrentBillItem().getNetRate() * getCurrentBillItem().getQty());
        getCurrentBillItem().setGrossValue(BigDecimalUtil.valueOrZero(f.getLineGrossTotal()).doubleValue());
        

        getCurrentBillItem().setSearialNo(getBillItems().size());
        getBillItems().add(currentBillItem);

        currentBillItem = null;
        // REMOVED: pharmacyCostingService.distributeProportionalBillValuesToItems() - was overriding our custom calculations
        calculateBillTotalsFromItems();
        
        
        // Apply proportional bill-level adjustments to grossRate for all items
        distributeBillLevelAdjustmentsToGrossRates();
        
//        calulateTotalsWhenAddingItemsOldMethod();
    }
    
    /**
     * Distributes bill-level discount and tax proportionally to update grossRate for all bill items
     * lineGrossRate remains unchanged (user input), but grossRate includes bill-level adjustments
     */
    private void distributeBillLevelAdjustmentsToGrossRates() {
        if (getBillItems() == null || getBillItems().isEmpty()) {
            return;
        }
        
        // Get bill-level totals
        BigDecimal billDiscount = BigDecimalUtil.valueOrZero(BigDecimal.valueOf(getBill().getDiscount()));
        BigDecimal billTax = BigDecimalUtil.valueOrZero(BigDecimal.valueOf(getBill().getTax()));
        
        // Calculate total line gross value for all items (basis for proportion)
        BigDecimal totalLineGrossValue = BigDecimal.ZERO;
        for (BillItem item : getBillItems()) {
            if (item.getBillItemFinanceDetails() != null) {
                totalLineGrossValue = BigDecimalUtil.add(totalLineGrossValue, 
                    BigDecimalUtil.valueOrZero(item.getBillItemFinanceDetails().getLineGrossTotal()));
            }
        }
        
        if (totalLineGrossValue.compareTo(BigDecimal.ZERO) == 0) {
            return; // Avoid division by zero
        }
        
        // Apply proportional adjustments to each item
        for (BillItem item : getBillItems()) {
            BillItemFinanceDetails f = item.getBillItemFinanceDetails();
            if (f == null) continue;
            
            BigDecimal itemLineGrossTotal = BigDecimalUtil.valueOrZero(f.getLineGrossTotal());
            BigDecimal itemQuantity = BigDecimalUtil.valueOrZero(f.getQuantity());
            
            if (itemQuantity.compareTo(BigDecimal.ZERO) == 0) continue;
            
            // Calculate proportional share of bill discount and tax
            BigDecimal proportionOfBill = itemLineGrossTotal.divide(totalLineGrossValue, 6, BigDecimal.ROUND_HALF_UP);
            BigDecimal itemBillDiscount = BigDecimalUtil.multiply(billDiscount, proportionOfBill);
            BigDecimal itemBillTax = BigDecimalUtil.multiply(billTax, proportionOfBill);
            
            // Calculate per-unit adjustments
            BigDecimal discountPerUnit = itemBillDiscount.divide(itemQuantity, 4, BigDecimal.ROUND_HALF_UP);
            BigDecimal taxPerUnit = itemBillTax.divide(itemQuantity, 4, BigDecimal.ROUND_HALF_UP);
            
            // Update grossRate: lineGrossRate + tax per unit - discount per unit
            BigDecimal adjustedGrossRate = f.getLineGrossRate();
            adjustedGrossRate = BigDecimalUtil.add(adjustedGrossRate, taxPerUnit);
            adjustedGrossRate = BigDecimalUtil.subtract(adjustedGrossRate, discountPerUnit);
            
            f.setGrossRate(adjustedGrossRate);
        }
    }

// ChatGPT contributed - Recalculates line-level financial values before adding BillItem to bill
    public void onQuantityChange() {
        BillItem bi = currentBillItem;
        if (bi == null || bi.getBillItemFinanceDetails() == null) {
            return;
        }
        BillItemFinanceDetails f = bi.getBillItemFinanceDetails();
        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(f);
    }

    public void onFreeQuantityChange() {
        BillItem bi = currentBillItem;
        if (bi == null || bi.getBillItemFinanceDetails() == null) {
            return;
        }

        BillItemFinanceDetails f = bi.getBillItemFinanceDetails();

        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(f);
    }

    public void onLineGrossRateChange() {
        BillItem bi = currentBillItem;
        if (bi == null || bi.getBillItemFinanceDetails() == null) {
            return;
        }

        BillItemFinanceDetails f = bi.getBillItemFinanceDetails();
        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(f); // Rate drives discount value

    }

    public void onLineDiscountRateChange() {
        BillItem bi = currentBillItem;
        if (bi == null || bi.getBillItemFinanceDetails() == null) {
            return;
        }

        BillItemFinanceDetails f = bi.getBillItemFinanceDetails();
        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(f);
    }

    // ChatGPT contributed: Optimized for null-safety and readability
    public void onRetailSaleRateChange() {
        if (currentBillItem == null) {
            return;
        }

        BillItemFinanceDetails f = currentBillItem.getBillItemFinanceDetails();
        if (f == null || f.getRetailSaleRate() == null) {
            return;
        }

        Item item = currentBillItem.getItem();
        if (item instanceof Ampp) {
            double dblVal = item.getDblValue();
            BigDecimal unitsPerPack = dblVal > 0.0 ? BigDecimal.valueOf(dblVal) : BigDecimal.ONE;
            f.setRetailSaleRatePerUnit(f.getRetailSaleRate().divide(unitsPerPack, MathContext.DECIMAL64));
        } else {
            f.setRetailSaleRatePerUnit(f.getRetailSaleRate());
        }

        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(f);
    }

    // </editor-fold>  
    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    // </editor-fold>  
    // <editor-fold defaultstate="collapsed" desc="Inner Classes Static Converter">
    // </editor-fold>  
    // <editor-fold defaultstate="collapsed" desc="Inner Classes">
    // </editor-fold>  
    /**
     * EJBs
     */
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillNumberGenerator billNumberBean;
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private AmpFacade ampFacade;
    @EJB
    BillFeeFacade billFeeFacade;
    @EJB
    PaymentFacade paymentFacade;
    @EJB
    BillEjb billEjb;
    @EJB
    PaymentService paymentService;
    @EJB
    PharmacyCostingService pharmacyCostingService;

    /**
     * Controllers
     */
    @Inject
    private SessionController sessionController;
    @Inject
    PharmacyCalculation pharmacyBillBean;
    @Inject
    ConfigOptionApplicationController configOptionApplicationController;
    @Inject
    private PharmacyController pharmacyController;
    /**
     * Properties
     */

    double saleRate;
    double wsRate;
    AmpController ampController;

    Institution institution;
    Department department;
    Date fromDate;
    Date toDate;
    List<PharmacyStockRow> rows;

    BillListWithTotals billListWithTotals;
    private double billItemsTotalQty;

    private PaymentMethodData paymentMethodData;
    private Institution site;
    private Institution toInstitution;
    private PaymentMethod paymentMethod;

    public List<BillItem> getBillExpenses() {
        if (billExpenses == null) {
            billExpenses = new ArrayList<>();
        }
        return billExpenses;
    }

    public void setBillExpenses(List<BillItem> billExpenses) {
        this.billExpenses = billExpenses;
    }

    public BillItem getCurrentExpense() {
        if (currentExpense == null) {
            currentExpense = new BillItem();
        }
        return currentExpense;
    }

    public void setCurrentExpense(BillItem currentExpense) {
        this.currentExpense = currentExpense;
    }
// ChatGPT contributed: Null-safe and debug-augmented version

    public void onItemSelect(SelectEvent event) {
        BillItem current = getCurrentBillItem();
        if (current == null || current.getItem() == null) {
            return;
        }

        Item item = current.getItem();
        Department dept = getSessionController().getDepartment();
        if (dept == null) {
            return;
        }

        double pr = 0.0;
        double rr = 0.0;
        BigDecimal packRate = BigDecimal.ZERO;

        BillItem lastPurchasedBillItem = getPharmacyBean().getLastPurchaseItem(item, dept);
        if (lastPurchasedBillItem != null) {
            BillItemFinanceDetails lastDetails = lastPurchasedBillItem.getBillItemFinanceDetails();
            if (lastDetails != null) {
                BigDecimal lineGrossRate = lastDetails.getLineGrossRate();
                BigDecimal lastRetailRate = lastDetails.getRetailSaleRate();

                pr = (lineGrossRate != null) ? lineGrossRate.doubleValue() : 0.0;
                rr = (lastRetailRate != null) ? lastRetailRate.doubleValue() : 0.0;
                packRate = BigDecimalUtil.valueOrZero(lastRetailRate);

            }
        }

        // Fallback logic
        if (pr == 0.0 || rr == 0.0) {
            double fallbackPr = getPharmacyBean().getLastPurchaseRate(item, dept);
            double fallbackRr = getPharmacyBean().getLastRetailRateByBillItemFinanceDetails(item, dept);
            pr = fallbackPr > 0.0 ? fallbackPr : pr;
            rr = fallbackRr > 0.0 ? fallbackRr : rr;
            packRate = BigDecimal.valueOf(rr);
        }

        PharmaceuticalBillItem pbi = current.getPharmaceuticalBillItem();
        if (pbi != null) {
            pbi.setPurchaseRate(pr);
            pbi.setRetailRate(rr);
        }

        BillItemFinanceDetails f = current.getBillItemFinanceDetails();
        if (f == null) {
            return;
        }

        f.setLineGrossRate(BigDecimal.valueOf(pr));

        if (item instanceof Ampp) {
            double units = item.getDblValue();
            BigDecimal unitsPerPack = (units > 0.0) ? BigDecimal.valueOf(units) : BigDecimal.ONE;
            f.setUnitsPerPack(unitsPerPack);
            f.setRetailSaleRate(packRate);
            f.setRetailSaleRatePerUnit(packRate.divide(unitsPerPack, MathContext.DECIMAL64));
        } else {
            f.setUnitsPerPack(BigDecimal.ONE);
            f.setRetailSaleRate(packRate);
            f.setRetailSaleRatePerUnit(packRate);
        }

        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(f);
    }

    public void onExpenseItemSelect(SelectEvent event) {
        BillItem current = getCurrentExpense();
        if (current == null || current.getItem() == null) {
            return;
        }
        
        // Set the consideredForCosting based on the selected expense item's default setting
        current.setConsideredForCosting(current.getItem().isConsideredForCosting());
    }

    public void updateExpenseCosting(BillItem expense) {
        if (expense == null) {
            return;
        }
        
        // Use transactional EJB method to ensure atomicity
        billEjb.updateExpenseCosting(expense, getBill(), getBillItems(), pharmacyCostingService);
    }

    private void recalculateExpenseTotals() {
        Bill bill = getBill(); // Cache the bill reference
        if (bill == null) {
            return;
        }

        double billExpensesConsideredTotal = 0.0;
        double billExpensesNotConsideredTotal = 0.0;
        double billExpensesTotal = 0.0;

        
        // Calculate totals from bill-level expense BillItems (use Bill entity's list)
        if (bill.getBillExpenses() != null && !bill.getBillExpenses().isEmpty()) {
            for (BillItem expense : bill.getBillExpenses()) {
                billExpensesTotal += expense.getNetValue();
                if (expense.isConsideredForCosting()) {
                    billExpensesConsideredTotal += expense.getNetValue();
                } else {
                    billExpensesNotConsideredTotal += expense.getNetValue();
                }
            }
        }


        // Update the bill's expense totals
        bill.setExpenseTotal(billExpensesTotal);
        bill.setExpensesTotalConsideredForCosting(billExpensesConsideredTotal);
        bill.setExpensesTotalNotConsideredForCosting(billExpensesNotConsideredTotal);
        
        
        // Also update BillFinanceDetails if it exists
        if (bill.getBillFinanceDetails() != null) {
            bill.getBillFinanceDetails().setBillExpense(BigDecimal.valueOf(billExpensesTotal));
            bill.getBillFinanceDetails().setBillExpensesConsideredForCosting(BigDecimal.valueOf(billExpensesConsideredTotal));
            bill.getBillFinanceDetails().setBillExpensesNotConsideredForCosting(BigDecimal.valueOf(billExpensesNotConsideredTotal));
        }
        
    }

    public double getBillExpensesTotal() {
        if (getBill().getBillExpenses() == null || getBill().getBillExpenses().isEmpty()) {
            return 0.0;
        }
        
        double total = 0.0;
        for (BillItem expense : getBill().getBillExpenses()) {
            total += expense.getNetValue();
        }
        return total;
    }

// ChatGPT contributed - Calculates true profit margin (%) based on unit sale and cost rates
    // ChatGPT contributed - Calculates profit margin (%) correctly based on item type (Amp or Ampp)
    public double calculateProfitMargin(BillItem bi) {
        return pharmacyCostingService.calculateProfitMarginForPurchases(bi);
    }

    public boolean isProfitMarginExcessive(BillItem ph) {
        if (ph == null || ph.getItem() == null || ph.getItem().getCategory() == null) {
            return false;
        }
        double margin = calculateProfitMargin(ph);
        return ph.getItem().getCategory().getProfitMargin() > margin;
    }

    public void displayItemDetails(BillItem bi) {
        if (bi == null || bi.getItem() == null) {
            return;
        }
        pharmacyController.fillItemDetails(bi.getItem());
    }

    public List<PharmacyStockRow> getRows() {
        return rows;
    }

    public Institution getInstitution() {
        if (institution == null) {
            institution = getSessionController().getInstitution();
        }
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonFunctions.getStartOfMonth(new Date());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
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

    public PaymentMethod[] getPaymentMethods() {
        return PaymentMethod.values();

    }

    public PharmacyCalculation getPharmacyBillBean() {
        return pharmacyBillBean;
    }

    public void setPharmacyBillBean(PharmacyCalculation pharmacyBillBean) {
        this.pharmacyBillBean = pharmacyBillBean;
    }

    public PharmacyDirectPurchaseController() {
    }

    public void setBatch() {
        if (getCurrentBillItem() != null) {
            PharmaceuticalBillItem pharmaceuticalBillItem = getCurrentBillItem().getPharmaceuticalBillItem();

            if (pharmaceuticalBillItem != null) {
                String stringValue = pharmaceuticalBillItem.getStringValue();

                if (stringValue != null && stringValue.trim().isEmpty()) {
                    Date date = pharmaceuticalBillItem.getDoe();

                    if (date != null) {
                        DateFormat df = new SimpleDateFormat("ddMMyyyy");
                        String reportDate = df.format(date);
                        pharmaceuticalBillItem.setStringValue(reportDate);
                    }
                }
            }
        }
    }

    public String errorCheck() {
        String msg = "";

        if (getBill().getFromInstitution() == null) {
            msg = "Please select Dealor";
            return msg;
        }

        if (getBillItems().isEmpty()) {
            msg = "Empty Items";
            return msg;
        }

        return msg;
    }

    @EJB
    CashTransactionBean cashTransactionBean;

    public CashTransactionBean getCashTransactionBean() {
        return cashTransactionBean;
    }

    public void setCashTransactionBean(CashTransactionBean cashTransactionBean) {
        this.cashTransactionBean = cashTransactionBean;
    }

    public void billDiscountChangedByUser() {
        ensureBillDiscountAndTaxSynchronization();
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getBill());
    }
    
    /**
     * Ensures that both bill.discount/tax and bill.billFinanceDetails.billDiscount/billTaxValue are synchronized
     * The service method reads from billFinanceDetails, but UI may store in bill directly
     */
    private void ensureBillDiscountAndTaxSynchronization() {
        if (getBill() == null) {
            return;
        }
        
        // Ensure BillFinanceDetails exists
        if (getBill().getBillFinanceDetails() == null) {
            getBill().setBillFinanceDetails(new BillFinanceDetails(getBill()));
        }
        
        // Synchronize discount and tax from bill to billFinanceDetails
        getBill().getBillFinanceDetails().setBillDiscount(BigDecimal.valueOf(getBill().getDiscount()));
        getBill().getBillFinanceDetails().setBillTaxValue(BigDecimal.valueOf(getBill().getTax()));
    }

    public void addExpense() {
        if (getBill().getId() == null) {
            getBillFacade().create(getBill());
            if (getBill().getBillFinanceDetails() == null) {
                getBill().setBillFinanceDetails(new BillFinanceDetails(getBill()));
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

        getCurrentExpense().setSearialNo(getBillExpenses().size());
        getBillExpenses().add(currentExpense);
        
        // IMPORTANT: Also add to the Bill entity's expense list
        getBill().getBillExpenses().add(currentExpense);
        
        // Recalculate expense totals after adding new expense
        recalculateExpenseTotals();
        
        // Recalculate entire bill totals with updated expense categorization
        calculateBillTotalsFromItems();
        
        // Distribute proportional bill values (including expenses considered for costing) to line items
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getBill());
        
        // Persist the updated bill
        if (getBill().getId() != null) {
            getBillFacade().edit(getBill());
        }
        
        currentExpense = null;

    }

    public void removeExpense(BillItem expense) {
        if (expense == null) {
            return;
        }

        if (billExpenses != null) {
            billExpenses.remove(expense);
            int index = 0;
            for (BillItem be : billExpenses) {
                be.setSearialNo(index++);
            }
        }

        if (getBill().getBillExpenses() != null) {
            getBill().getBillExpenses().remove(expense);
        }

        recalculateExpenseTotals();
        calculateBillTotalsFromItems();
        pharmacyCostingService.distributeProportionalBillValuesToItems(getBillItems(), getBill());

        if (getBill().getId() != null) {
            billFacade.edit(getBill());
        }
    }

    public void settleDirectPurchaseBillFinally() {
        if (getBill().getPaymentMethod() == null) {
            JsfUtil.addErrorMessage("Select Payment Method");
            return;
        }
        if (getBill().getFromInstitution() == null) {
            JsfUtil.addErrorMessage("Select Dealer");
            return;
        }
        if (getBill().getReferenceInstitution() == null) {
            JsfUtil.addErrorMessage("Select Reference Institution");
        }
        if (getBill().getInvoiceNumber() == null || getBill().getInvoiceNumber().trim().isEmpty()) {
            boolean autogenerateInvoiceNumber = configOptionApplicationController.getBooleanValueByKey("Autogenerate Invoice Number for Pharmacy Direct Purchase", false);
            if (autogenerateInvoiceNumber) {
                BillNumber bn = billNumberBean.fetchLastBillNumberForYear(sessionController.getInstitution(), sessionController.getDepartment(), BillTypeAtomic.PHARMACY_DIRECT_PURCHASE);
                String invoiceNumber = configOptionApplicationController.getShortTextValueByKey("Invoice Number Prefix for Pharmacy Direct Purchase", "") + bn.getLastBillNumber();
                getBill().setInvoiceNumber(invoiceNumber);
            } else {
                JsfUtil.addErrorMessage("Please Enter Invoice Number");
                return;
            }
        }
        if (getBill().getInvoiceDate() == null) {
            boolean useCurrentDataIfInvoiceDataIsNotProvided = configOptionApplicationController.getBooleanValueByKey("If Invoice Number is not provided for Pharmacy Direct Purchase, use the current date", false);
            if (useCurrentDataIfInvoiceDataIsNotProvided) {
                getBill().setInvoiceDate(new Date());
            } else {
                JsfUtil.addErrorMessage("Please Fill Invoice Date");
                return;
            }
        }
        if (getBill().getPaymentMethod() == PaymentMethod.MultiplePaymentMethods) {
            JsfUtil.addErrorMessage("MultiplePayments Not Allowed.");
            return;
        }

        //Need to Add History
        String msg = errorCheck();
        if (!msg.isEmpty()) {
            JsfUtil.addErrorMessage(msg);
            return;
        }

        saveBill();
        //   saveBillComponent();

//        Payment p = createPayment(getBill());
        List<Payment> ps = paymentService.createPayment(getBill(), getPaymentMethodData());

        billItemsTotalQty = 0;

        for (BillItem i : getBillItems()) {
            double lastPurchaseRate = 0.0;
            lastPurchaseRate = getPharmacyBean().getLastPurchaseRate(i.getItem());

            if (i.getPharmaceuticalBillItem().getQty() + i.getPharmaceuticalBillItem().getFreeQty() == 0.0) {
                continue;
            }

            billItemsTotalQty = billItemsTotalQty + i.getPharmaceuticalBillItem().getQty() + i.getPharmaceuticalBillItem().getFreeQty();

            PharmaceuticalBillItem pbi = i.getPharmaceuticalBillItem();


            i.setPharmaceuticalBillItem(null);
            i.setCreatedAt(Calendar.getInstance().getTime());
            i.setCreater(getSessionController().getLoggedUser());
            i.setBill(getBill());

            if (i.getId() == null) {
                getBillItemFacade().create(i);
            } else {
                getBillItemFacade().edit(i);
            }

            if (pbi.getId() == null) {
                getPharmaceuticalBillItemFacade().create(pbi);
            } else {
                getPharmaceuticalBillItemFacade().edit(pbi);
            }

            i.setPharmaceuticalBillItem(pbi);
            getBillItemFacade().edit(i);

            saveBillFee(i);

            ItemBatch itemBatch = getPharmacyBillBean().saveItemBatchWithCosting(i);

            double addingQty = BigDecimalUtil.valueOrZero(i.getBillItemFinanceDetails().getTotalQuantityByUnits()).doubleValue();

            pbi.setItemBatch(itemBatch);

            Stock stock = getPharmacyBean().addToStockForCosting(i, Math.abs(addingQty), getSessionController().getDepartment());

            pbi.setLastPurchaseRate(lastPurchaseRate);
            pbi.setStock(stock);

            getPharmaceuticalBillItemFacade().edit(pbi);

            getBill().getBillItems().add(i);
        }

        //check and calculate expenses separately
        if (billExpenses != null && !billExpenses.isEmpty()) {
            getBill().setBillExpenses(billExpenses);

            double totalForExpenses = 0;
            for (BillItem expense : getBillExpenses()) {
                totalForExpenses += expense.getNetValue();
            }

            getBill().setExpenseTotal(-Math.abs(totalForExpenses));
            // Note: NetTotal is already correctly calculated by the service and includes expenses
            // Removed: getBill().setNetTotal(getBill().getNetTotal() + totalForExpenses);
        }

        getPharmacyBillBean().calculateRetailSaleValueAndFreeValueAtPurchaseRate(getBill());
        getBillFacade().edit(getBill());

        JsfUtil.addSuccessMessage("Direct Purchase Successfully Completed.");
        printPreview = true;
    }

    public void removeItem(BillItem bi) {
        getBillItems().remove(bi);

        int i = 0;
        for (BillItem it : getBillItems()) {
            it.setSearialNo(i++);
        }

        pharmacyCostingService.distributeProportionalBillValuesToItems(billItems, bill);
        calculateBillTotalsFromItems();
        currentBillItem = null;
    }

    public Payment createPayment(Bill bill) {
        Payment p = new Payment();
        p.setBill(bill);
        setPaymentMethodData(p, bill.getPaymentMethod());
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
            paymentFacade.create(p);
        }

    }

    public void saveBillFee(BillItem bi) {
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
//        createBillFeePaymentAndPayment(bf, p);
    }

    public void saveBill() {

        String deptId = billNumberBean.departmentBillNumberGeneratorYearly(getSessionController().getDepartment(), BillTypeAtomic.PHARMACY_DIRECT_PURCHASE);

        getBill().setDeptId(deptId);
        getBill().setInsId(deptId);
        getBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_DIRECT_PURCHASE);

        getBill().setInstitution(getSessionController().getInstitution());
        getBill().setDepartment(getSessionController().getDepartment());

        getBill().setCreatedAt(new Date());
        getBill().setCreater(getSessionController().getLoggedUser());

        if (getBill().getId() == null) {
            getBillFacade().create(getBill());
        } else {
            getBillFacade().edit(getBill());
        }

    }

    public double getNetTotal() {
        // If NetTotal has already been calculated by the service (includes expenses), return it as-is
        if (getBill().getNetTotal() != 0.0) {
            return getBill().getNetTotal(); // Return the calculated value (negative for purchases)
        }
        
        // Fallback calculation for cases where service hasn't calculated yet
        double tmp = getBill().getTotal() + getBill().getTax() - getBill().getDiscount();
        getBill().setNetTotal(0 - tmp);

        return 0 - tmp; // Return negative value for purchase transactions
    }

    public void calTotal() {
        double tot = 0.0;
        double saleValue = 0.0;
        int serialNo = 0;
        for (BillItem p : getBillItems()) {
            p.setQty(p.getPharmaceuticalBillItem().getQtyInUnit());
            p.setRate(p.getPharmaceuticalBillItem().getPurchaseRateInUnit());
            p.setSearialNo(serialNo++);
            double netValue = p.getQty() * p.getRate();
            p.setNetValue(0 - netValue);
            tot += p.getNetValue();
            saleValue += (p.getPharmaceuticalBillItem().getQtyInUnit() + p.getPharmaceuticalBillItem().getFreeQtyInUnit()) * p.getPharmaceuticalBillItem().getRetailRate();
        }
        getBill().setTotal(tot);
        getBill().setNetTotal(tot);
        getBill().setSaleValue(saleValue);
    }

    public void calculateBillTotalsFromItems() {
        pharmacyCostingService.calculateBillTotalsFromItemsForPurchases(getBill(), getBillItems());
        
    }

    public BilledBill getBill() {
        if (bill == null) {
            bill = new BilledBill();
            bill.setBillType(BillType.PharmacyPurchaseBill);
            bill.setBillTypeAtomic(BillTypeAtomic.PHARMACY_DIRECT_PURCHASE);
            bill.setReferenceInstitution(getSessionController().getInstitution());
            boolean consignmentEnabled = configOptionApplicationController.getBooleanValueByKey("Consignment Option is checked in new Pharmacy Purchasing Bills", true);
            bill.setConsignment(consignmentEnabled);
        }
        return bill;
    }

    public void setBill(BilledBill bill) {
        this.bill = bill;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public BillNumberGenerator getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberGenerator billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
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

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public BillItem getCurrentBillItem() {
        if (currentBillItem == null) {
            currentBillItem = new BillItem();
            PharmaceuticalBillItem cuPharmaceuticalBillItem = new PharmaceuticalBillItem();
            currentBillItem.setPharmaceuticalBillItem(cuPharmaceuticalBillItem);
            cuPharmaceuticalBillItem.setBillItem(currentBillItem);
            BillItemFinanceDetails fd = new BillItemFinanceDetails(currentBillItem);
            currentBillItem.setBillItemFinanceDetails(fd);
        }
        return currentBillItem;
    }

    public void setCurrentBillItem(BillItem currentBillItem) {
        this.currentBillItem = currentBillItem;
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

    public double getSaleRate() {
        return saleRate;
    }

    public void setSaleRate(double saleRate) {
        this.saleRate = saleRate;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public BillListWithTotals getBillListWithTotals() {
        return billListWithTotals;
    }

    public void setBillListWithTotals(BillListWithTotals billListWithTotals) {
        this.billListWithTotals = billListWithTotals;
    }

    public double getBillItemsTotalQty() {
        return billItemsTotalQty;
    }

    public void setBillItemsTotalQty(double billItemsTotalQty) {
        this.billItemsTotalQty = billItemsTotalQty;
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

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
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

    public boolean isShowAllBillFormats() {
        return showAllBillFormats;
    }

    public void setShowAllBillFormats(boolean showAllBillFormats) {
        this.showAllBillFormats = showAllBillFormats;
    }

    public String toggleShowAllBillFormats() {
        this.showAllBillFormats = !this.showAllBillFormats;
        return "";
    }

}
