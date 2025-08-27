package com.divudi.service.pharmacy;

import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillFinanceDetails;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.BillItemFinanceDetails;
import com.divudi.core.entity.pharmacy.Ampp;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.pharmacy.Amp;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.entity.pharmacy.Vmp;
import com.divudi.core.entity.pharmacy.Vmpp;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import javax.ejb.Stateless;
import com.divudi.core.util.BigDecimalUtil;

@Stateless
public class PharmacyCostingService {

    // ChatGPT contributed
    /**
     * Recalculate Pharmaceutical Bill Item quantities and values to be negative
     * regardless of current sign. Used at the end of bills where stocks are
     * going out.
     *
     * @param pbi PharmaceuticalBillItem to update
     */
    public void makeAllQuantityValuesNegative(PharmaceuticalBillItem pbi) {
        if (pbi == null) {
            return;
        }
        pbi.setQty(-Math.abs(pbi.getQty()));
        pbi.setQtyPacks(-Math.abs(pbi.getQtyPacks()));
        pbi.setFreeQty(-Math.abs(pbi.getFreeQty()));
        pbi.setFreeQtyPacks(-Math.abs(pbi.getFreeQtyPacks()));
        pbi.setPurchaseValue(-Math.abs(pbi.getPurchaseValue()));
        pbi.setPurchaseRatePackValue(-Math.abs(pbi.getPurchaseRatePackValue()));
        pbi.setRetailValue(-Math.abs(pbi.getRetailValue()));
        pbi.setRetailPackValue(-Math.abs(pbi.getRetailPackValue()));
    }

    // ChatGPT contributed
    /**
     * Recalculate Pharmaceutical Bill Item quantities and values to be positive
     * regardless of current sign. Used at the end of bills where stocks are
     * coming in.
     *
     * @param pbi PharmaceuticalBillItem to update
     */
    public void makeAllQuantityValuesPositive(PharmaceuticalBillItem pbi) {
        if (pbi == null) {
            return;
        }
        pbi.setQty(Math.abs(pbi.getQty()));
        pbi.setQtyPacks(Math.abs(pbi.getQtyPacks()));
        pbi.setFreeQty(Math.abs(pbi.getFreeQty()));
        pbi.setFreeQtyPacks(Math.abs(pbi.getFreeQtyPacks()));
        pbi.setPurchaseValue(Math.abs(pbi.getPurchaseValue()));
        pbi.setPurchaseRatePackValue(Math.abs(pbi.getPurchaseRatePackValue()));
        pbi.setRetailValue(Math.abs(pbi.getRetailValue()));
        pbi.setRetailPackValue(Math.abs(pbi.getRetailPackValue()));
    }

    /**
     * Recalculate line-level financial values before adding a BillItem to a
     * bill.
     *
     * @param billItemFinanceDetails
     */
    public void recalculateFinancialsBeforeAddingBillItem(BillItemFinanceDetails billItemFinanceDetails) {
        if (billItemFinanceDetails == null || billItemFinanceDetails.getBillItem() == null) {
            return;
        }
        BillItem billItem = billItemFinanceDetails.getBillItem();
        if (billItem.getPharmaceuticalBillItem() == null) {
            return;
        }
        PharmaceuticalBillItem pbi = billItem.getPharmaceuticalBillItem();

        Double prPerUnit;
        Double rrPerUnit;
        BigDecimal qty = BigDecimalUtil.valueOrZero(billItemFinanceDetails.getQuantity());
        BigDecimal freeQty = BigDecimalUtil.valueOrZero(billItemFinanceDetails.getFreeQuantity());
        BigDecimal lineGrossRate = BigDecimalUtil.valueOrZero(billItemFinanceDetails.getLineGrossRate());
        BigDecimal lineDiscountRate = BigDecimalUtil.valueOrZero(billItemFinanceDetails.getLineDiscountRate());
        BigDecimal retailRate = BigDecimalUtil.valueOrZero(billItemFinanceDetails.getRetailSaleRate());

        Item item = billItemFinanceDetails.getBillItem().getItem();
        BigDecimal totalQty = qty.add(freeQty);

        BigDecimal unitsPerPack;
        BigDecimal qtyInUnits;
        BigDecimal freeQtyInUnits;
        BigDecimal totalQtyInUnits;
        if (item instanceof Ampp) {
            double dblVal = item.getDblValue();
            unitsPerPack = dblVal > 0.0 ? BigDecimal.valueOf(dblVal) : BigDecimal.ONE;
            qtyInUnits = qty.multiply(unitsPerPack);
            freeQtyInUnits = freeQty.multiply(unitsPerPack);
            totalQtyInUnits = totalQty.multiply(unitsPerPack);
            prPerUnit = lineGrossRate.divide(unitsPerPack, 4, RoundingMode.HALF_UP).doubleValue();
            rrPerUnit = retailRate.divide(unitsPerPack, 4, RoundingMode.HALF_UP).doubleValue();
        } else {
            unitsPerPack = BigDecimal.ONE;
            qtyInUnits = qty;
            freeQtyInUnits = freeQty;
            totalQtyInUnits = totalQty;
            prPerUnit = lineGrossRate.doubleValue();
            rrPerUnit = retailRate.doubleValue();
        }

        billItemFinanceDetails.setUnitsPerPack(unitsPerPack);
        billItemFinanceDetails.setQuantityByUnits(qtyInUnits);
        billItemFinanceDetails.setFreeQuantityByUnits(freeQtyInUnits);
        billItemFinanceDetails.setTotalQuantityByUnits(totalQtyInUnits);

        BigDecimal lineGrossTotal = lineGrossRate.multiply(qty);
        // lineDiscountRate is amount per unit, not percentage
        BigDecimal lineDiscountValue = lineDiscountRate.multiply(qty);
        BigDecimal lineNetTotal = lineGrossTotal.subtract(lineDiscountValue);
        BigDecimal lineCostRate = BigDecimalUtil.isPositive(totalQtyInUnits)
                ? lineNetTotal.divide(totalQtyInUnits, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        BigDecimal retailValue = retailRate.multiply(totalQtyInUnits);

        billItemFinanceDetails.setLineGrossRate(lineGrossRate);
        billItemFinanceDetails.setLineNetRate(BigDecimalUtil.isPositive(qty)
                ? lineNetTotal.divide(qty, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        billItemFinanceDetails.setRetailSaleRatePerUnit(
                BigDecimalUtil.isPositive(unitsPerPack)
                ? retailRate.divide(unitsPerPack, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO
        );

        billItemFinanceDetails.setLineDiscount(lineDiscountValue);
        billItemFinanceDetails.setLineGrossTotal(lineGrossTotal);
        billItemFinanceDetails.setLineNetTotal(lineNetTotal);
        billItemFinanceDetails.setLineCost(lineNetTotal);
        billItemFinanceDetails.setLineCostRate(lineCostRate);
        billItemFinanceDetails.setTotalQuantity(totalQty);

        billItemFinanceDetails.setProfitMargin(calculateProfitMarginForPurchasesBigDecimal(billItemFinanceDetails.getBillItem()));

        pbi.setRetailRate(retailRate.doubleValue());
        pbi.setRetailRateInUnit(rrPerUnit);
        pbi.setRetailRatePack(retailRate.doubleValue());

        pbi.setRetailPackValue(retailValue.doubleValue());
        pbi.setRetailValue(retailValue.doubleValue());

        pbi.setPurchaseRate(prPerUnit);
        pbi.setPurchaseRatePack(lineGrossRate.doubleValue());

        pbi.setPurchaseRatePackValue(lineGrossTotal.doubleValue());
        pbi.setPurchaseValue(lineGrossTotal.doubleValue());
    }

    /**
     * Distribute bill-level values (discounts, expenses, taxes) proportionally
     * to items.
     *
     * @param billItems
     * @param bill
     */
    public void distributeProportionalBillValuesToItems(List<BillItem> billItems, Bill bill) {
        if (bill == null) {
            System.out.println("DEBUG: Bill is null, returning");
            return;
        }

        if (bill.getBillFinanceDetails() == null) {
            bill.setBillFinanceDetails(new BillFinanceDetails(bill));
        }
        
        // Reset and recalculate expense totals from actual bill expense items
        double expenseTotal = 0.0;
        double expensesTotalConsideredForCosting = 0.0;
        double expensesTotalNotConsideredForCosting = 0.0;
        
        if (bill.getBillExpenses() != null && !bill.getBillExpenses().isEmpty()) {
            for (com.divudi.core.entity.BillItem expense : bill.getBillExpenses()) {
                double expenseValue = expense.getNetValue();
                boolean isConsidered = expense.isConsideredForCosting();
                
                expenseTotal += expenseValue;
                if (isConsidered) {
                    expensesTotalConsideredForCosting += expenseValue;
                } else {
                    expensesTotalNotConsideredForCosting += expenseValue;
                }
            }
        }
        
        // Set the recalculated expense totals
        bill.setExpenseTotal(expenseTotal);
        bill.setExpensesTotalConsideredForCosting(expensesTotalConsideredForCosting);
        bill.setExpensesTotalNotConsideredForCosting(expensesTotalNotConsideredForCosting);
        
        bill.getBillFinanceDetails().setBillDiscount(BigDecimal.valueOf(bill.getDiscount()));
        bill.getBillFinanceDetails().setBillTaxValue(BigDecimal.valueOf(bill.getTax()));
        bill.getBillFinanceDetails().setBillExpense(BigDecimal.valueOf(expensesTotalConsideredForCosting));

        if (billItems == null || billItems.isEmpty()) {
            return;
        }

        // Note: Reset logic moved to calculateBillTotalsFromItemsForPurchases() method

        BigDecimal totalBasis = BigDecimal.ZERO;
        Map<BillItem, BigDecimal> itemBases = new HashMap<>();
        for (BillItem bi : billItems) {
            BillItemFinanceDetails f = bi.getBillItemFinanceDetails();
            if (f == null) {
                continue;
            }
            BigDecimal qty = BigDecimalUtil.valueOrZero(f.getQuantity());
            BigDecimal freeQty = BigDecimalUtil.valueOrZero(f.getFreeQuantity());
            BigDecimal lineGrossRate = BigDecimalUtil.valueOrZero(f.getLineGrossRate());
            // Use paid quantity only for proportional distribution basis
            BigDecimal basis = lineGrossRate.multiply(qty);
            
            itemBases.put(bi, basis);
            totalBasis = totalBasis.add(basis);
        }

        if (BigDecimalUtil.isNullOrZero(totalBasis)) {
            return;
        }

        BigDecimal billDiscountTotal = BigDecimalUtil.valueOrZero(bill.getBillFinanceDetails().getBillDiscount());
        BigDecimal billExpenseTotal = BigDecimalUtil.valueOrZero(bill.getBillFinanceDetails().getBillExpense());
        BigDecimal billTaxTotal = BigDecimalUtil.valueOrZero(bill.getBillFinanceDetails().getBillTaxValue());
        

        for (BillItem bi : billItems) {
            BillItemFinanceDetails f = bi.getBillItemFinanceDetails();
            if (f == null) {
                continue;
            }
            BigDecimal basis = itemBases.get(bi);
            BigDecimal ratio = basis.divide(totalBasis, 12, RoundingMode.HALF_UP);

            BigDecimal lineDiscount = BigDecimalUtil.valueOrZero(f.getLineDiscount());
            BigDecimal lineExpense = BigDecimalUtil.valueOrZero(f.getLineExpense());
            BigDecimal lineTax = BigDecimalUtil.valueOrZero(f.getLineTax());
            BigDecimal lineNetTotal = BigDecimalUtil.valueOrZero(f.getLineNetTotal());
            BigDecimal lineGrossTotal = BigDecimalUtil.valueOrZero(f.getLineGrossTotal());
            BigDecimal lineGrossRate = BigDecimalUtil.valueOrZero(f.getLineGrossRate());

            BigDecimal billDiscount = billDiscountTotal.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
            BigDecimal billExpense = billExpenseTotal.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
            BigDecimal billTax = billTaxTotal.multiply(ratio).setScale(2, RoundingMode.HALF_UP);

            System.out.println("DEBUG: Item: " + bi.getItem().getName() + 
                             ", Basis: " + basis +
                             ", Ratio: " + ratio + 
                             ", BillDiscount distributed: " + billDiscount +
                             ", BillExpense distributed: " + billExpense +
                             ", BillTax distributed: " + billTax);

            f.setBillDiscount(billDiscount);
            f.setBillExpense(billExpense);
            f.setBillTax(billTax);
            

            BigDecimal totalDiscount = lineDiscount.add(billDiscount);
            BigDecimal totalExpense = lineExpense.add(billExpense);
            BigDecimal totalTax = lineTax.add(billTax);

            f.setTotalDiscount(totalDiscount);
            f.setTotalExpense(totalExpense);
            f.setTotalTax(totalTax);
            
            System.out.println("DEBUG: Item: " + bi.getItem().getName() + 
                             ", TotalExpense: " + totalExpense);

            BigDecimal quantity = Optional.ofNullable(f.getQuantity()).orElse(BigDecimal.ZERO);
            BigDecimal freeQty = Optional.ofNullable(f.getFreeQuantity()).orElse(BigDecimal.ZERO);
            BigDecimal totalQty = quantity.add(freeQty);
            f.setTotalQuantity(totalQty);

            BigDecimal billDiscountRate = quantity.compareTo(BigDecimal.ZERO) > 0
                    ? billDiscount.divide(quantity, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            f.setBillDiscountRate(billDiscountRate);

            BigDecimal totalDiscountRate = quantity.compareTo(BigDecimal.ZERO) > 0
                    ? totalDiscount.divide(quantity, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            f.setTotalDiscountRate(totalDiscountRate);

            BigDecimal netTotal = lineGrossTotal.subtract(totalDiscount).add(totalTax).add(totalExpense);
            f.setNetTotal(netTotal);
            f.setTotalCost(netTotal);

            BigDecimal billCost = netTotal.subtract(lineNetTotal);
            f.setBillCost(billCost);

            BigDecimal qtyUnits = Optional.ofNullable(f.getTotalQuantityByUnits())
                    .orElse(totalQty);

            BigDecimal lineCostRate = qtyUnits.compareTo(BigDecimal.ZERO) > 0
                    ? lineNetTotal.divide(qtyUnits, 6, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal billCostRate = qtyUnits.compareTo(BigDecimal.ZERO) > 0
                    ? billCost.divide(qtyUnits, 6, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal totalCostRate = qtyUnits.compareTo(BigDecimal.ZERO) > 0
                    ? netTotal.divide(qtyUnits, 6, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            f.setLineCostRate(lineCostRate.setScale(4, RoundingMode.HALF_UP));
            f.setBillCostRate(billCostRate.setScale(4, RoundingMode.HALF_UP));
            f.setTotalCostRate(totalCostRate.setScale(4, RoundingMode.HALF_UP));

            f.setLineGrossRate(lineGrossRate);
            f.setBillGrossRate(BigDecimal.ZERO);
            f.setGrossRate(lineGrossRate);

            f.setLineGrossTotal(lineGrossTotal);
            f.setBillGrossTotal(BigDecimal.ZERO);
            f.setGrossTotal(lineGrossTotal);

            if (f.getLineNetRate() == null || f.getLineNetRate().compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal lineNetRate = quantity.compareTo(BigDecimal.ZERO) > 0
                        ? lineNetTotal.divide(quantity, 4, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                f.setLineNetRate(lineNetRate);
            }

            BigDecimal billNetRate = quantity.compareTo(BigDecimal.ZERO) > 0
                    ? billCost.divide(quantity, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            f.setBillNetRate(billNetRate);

            BigDecimal netRate = quantity.compareTo(BigDecimal.ZERO) > 0
                    ? netTotal.divide(quantity, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            f.setNetRate(netRate);
        }
        
        // After distribution, update bill-level totals by aggregating from distributed line items
        aggregateBillTotalsFromDistributedItems(bill, billItems);
        
        System.out.println("=== DEBUG: distributeProportionalBillValuesToItems() END ===");
    }
    
    /**
     * Aggregate bill totals from items after bill-level distribution has occurred.
     * This preserves line-level values while updating bill totals from distributed values.
     */
    private void aggregateBillTotalsFromDistributedItems(Bill bill, List<BillItem> billItems) {
        BigDecimal totalNetTotal = BigDecimal.ZERO;
        BigDecimal totalGrossTotal = BigDecimal.ZERO;
        
        for (BillItem bi : billItems) {
            BillItemFinanceDetails f = bi.getBillItemFinanceDetails();
            if (f != null) {
                totalNetTotal = totalNetTotal.add(Optional.ofNullable(f.getNetTotal()).orElse(BigDecimal.ZERO));
                totalGrossTotal = totalGrossTotal.add(Optional.ofNullable(f.getLineGrossTotal()).orElse(BigDecimal.ZERO));
            }
        }
        
        // Add bill expenses not considered for costing (to avoid double-counting)
        if (bill.getBillExpenses() != null) {
            for (BillItem expense : bill.getBillExpenses()) {
                if (!expense.isConsideredForCosting()) {
                    BigDecimal expenseNetValue = Optional.ofNullable(BigDecimal.valueOf(expense.getNetValue())).orElse(BigDecimal.ZERO);
                    totalNetTotal = totalNetTotal.add(expenseNetValue);
                }
            }
        }
        
        bill.setNetTotal(totalNetTotal.doubleValue());
        bill.setTotal(totalGrossTotal.doubleValue());
    }

    public void addPharmaceuticalBillItemQuantitiesFromBillItemFinanceDetailQuantities(PharmaceuticalBillItem pbi, BillItemFinanceDetails bifd) {
        if (pbi == null || bifd == null) {
            return;
        }

        BillItem pbiBillItem = pbi.getBillItem();
        BillItem bifdBillItem = bifd.getBillItem();
        if (pbiBillItem == null || bifdBillItem == null) {
            return;
        }

        BigDecimal qty = Optional.ofNullable(bifd.getQuantity()).orElse(BigDecimal.ZERO);
        BigDecimal freeQty = Optional.ofNullable(bifd.getFreeQuantity()).orElse(BigDecimal.ZERO);
        BigDecimal upp = Optional.ofNullable(bifd.getUnitsPerPack()).orElse(BigDecimal.ONE);
        if (upp.compareTo(BigDecimal.ZERO) == 0) {
            upp = BigDecimal.ONE;
        }

        pbi.setQty(qty.multiply(upp).doubleValue());
        pbi.setFreeQty(freeQty.multiply(upp).doubleValue());
        pbi.setQtyPacks(qty.doubleValue());
        pbi.setFreeQtyPacks(freeQty.doubleValue());

        BigDecimal totalQty = Optional.ofNullable(bifd.getTotalQuantity()).orElse(BigDecimal.ZERO);

        bifd.setQuantityByUnits(qty.multiply(upp));
        bifd.setFreeQuantityByUnits(freeQty.multiply(upp));
        bifd.setTotalQuantityByUnits(totalQty.multiply(upp));

    }

    public void calculateUnitsPerPack(BillItemFinanceDetails bifd) {
        if (bifd == null) {
            return;
        }
        if (bifd.getBillItem() == null) {
            return;
        }
        if (bifd.getBillItem().getPharmaceuticalBillItem() == null) {
            return;
        }
        if (bifd.getBillItem().getItem() == null) {
            return;
        }
        BillItem bi = bifd.getBillItem();
        if (bi.getItem() instanceof Ampp) {
            Ampp ampp = (Ampp) bi.getItem();
            bifd.setUnitsPerPack(BigDecimal.valueOf(ampp.getDblValue()));
        } else if (bi.getItem() instanceof Vmpp) {
            Vmpp vmpp = (Vmpp) bi.getItem();
            bifd.setUnitsPerPack(BigDecimal.valueOf(vmpp.getDblValue()));
        } else if (bi.getItem() instanceof Amp) {
            bifd.setUnitsPerPack(BigDecimal.ONE);
        } else if (bi.getItem() instanceof Vmp) {
            bifd.setUnitsPerPack(BigDecimal.ONE);
        } else {
            bifd.setUnitsPerPack(BigDecimal.ONE);
        }
    }

    public void addBillItemFinanceDetailQuantitiesFromPharmaceuticalBillItem(PharmaceuticalBillItem pbi, BillItemFinanceDetails bifd) {
        if (pbi == null || bifd == null) {
            return;
        }
        BigDecimal upp = Optional.ofNullable(bifd.getUnitsPerPack()).orElse(BigDecimal.ONE);
        if (upp.compareTo(BigDecimal.ZERO) == 0) {
            upp = BigDecimal.ONE;
        }
        Double qtyInUnits = Optional.ofNullable(pbi.getQty()).orElse(0.0);
        Double freeQtyInUnits = Optional.ofNullable(pbi.getFreeQty()).orElse(0.0);

        bifd.setQuantity(BigDecimal.valueOf(qtyInUnits).divide(upp));
        bifd.setFreeQuantity(BigDecimal.valueOf(freeQtyInUnits).divide(upp));
        bifd.setTotalQuantity(BigDecimal.valueOf(qtyInUnits + freeQtyInUnits).divide(upp));

        bifd.setQuantityByUnits(BigDecimal.valueOf(qtyInUnits));
        bifd.setFreeQuantityByUnits(BigDecimal.valueOf(freeQtyInUnits));
        bifd.setTotalQuantityByUnits(BigDecimal.valueOf(qtyInUnits + freeQtyInUnits));
        
    }

    public double calculateProfitMarginForPurchases(BillItem bi) {
        return calculateProfitMarginForPurchasesBigDecimal(bi).doubleValue();
    }

    public BigDecimal calculateProfitMarginForPurchasesBigDecimal(BillItem bi) {
        if (bi == null) {
            return BigDecimal.ZERO;
        }

        BillItemFinanceDetails f = bi.getBillItemFinanceDetails();
        if (f == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal purchaseRate = f.getLineNetRate();
        BigDecimal retailRate = f.getRetailSaleRate();
        BigDecimal qty = f.getQuantity();
        BigDecimal freeQty = f.getFreeQuantity();

        if (purchaseRate == null || retailRate == null || qty == null || freeQty == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalQty = qty.add(freeQty);
        BigDecimal purchaseValue = purchaseRate.multiply(qty);
        BigDecimal retailValue = retailRate.multiply(totalQty);

        if (purchaseValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return retailValue.subtract(purchaseValue)
                .divide(purchaseValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public void calculateBillTotalsFromItemsForPurchases(Bill bill, List<BillItem> billItems) {
        
        // Reset distributed bill-level values in all line items before calculating totals
        // Note: We only reset the DISTRIBUTED portions, not user-entered line-level values
        if (billItems != null && !billItems.isEmpty()) {
            for (BillItem bi : billItems) {
                BillItemFinanceDetails f = bi.getBillItemFinanceDetails();
                if (f != null) {
                    // Reset only the distributed bill-level values (not user input)
                    f.setBillExpense(BigDecimal.ZERO);
                    f.setBillDiscount(BigDecimal.ZERO);  // This will be recalculated from bill.getDiscount()
                    f.setBillTax(BigDecimal.ZERO);       // This will be recalculated from bill.getTax()
                    
                    // Reset totals to only line-level values (preserving user inputs)
                    f.setTotalExpense(BigDecimalUtil.valueOrZero(f.getLineExpense()));
                    f.setTotalDiscount(BigDecimalUtil.valueOrZero(f.getLineDiscount()));
                    f.setTotalTax(BigDecimalUtil.valueOrZero(f.getLineTax()));
                    
                    // Reset NetTotal to LineNetTotal (no bill-level distributions)
                    f.setNetTotal(BigDecimalUtil.valueOrZero(f.getLineNetTotal()));
                    f.setTotalCost(BigDecimalUtil.valueOrZero(f.getLineNetTotal()));
                }
            }
        }
        
        int serialNo = 0;

        // Only bill-level values provided by user
        BigDecimal billDiscount = BigDecimal.valueOf(bill.getDiscount());
        BigDecimal billExpense = BigDecimal.valueOf(bill.getExpensesTotalConsideredForCosting());
        BigDecimal billTax = BigDecimal.valueOf(bill.getTax());
        BigDecimal billCost = billDiscount.subtract(billExpense.add(billTax));
        

        // Initialize totals
        BigDecimal totalLineDiscounts = BigDecimal.ZERO;
        BigDecimal totalLineExpenses = BigDecimal.ZERO;
        BigDecimal totalLineCosts = BigDecimal.ZERO;
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

        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (BillItem bi : billItems) {
            PharmaceuticalBillItem pbi = bi.getPharmaceuticalBillItem();
            BillItemFinanceDetails f = bi.getBillItemFinanceDetails();

            // Don't override user-entered quantities during GRN costing
            // Only set quantities from PharmaceuticalBillItem if BillItem qty is not already set by user
            if (bi.getQty() == null || bi.getQty() == 0.0) {
                if (bi.getItem() instanceof Ampp) {
                    bi.setQty(pbi.getQtyPacks());
                    bi.setRate(pbi.getPurchaseRatePack());
                } else if (bi.getItem() instanceof Amp) {
                    bi.setQty(pbi.getQty());
                    bi.setRate(pbi.getPurchaseRate());
                }
            } else {
                // Preserve user-entered quantity but update rate if needed
                if (bi.getItem() instanceof Ampp) {
                    bi.setRate(pbi.getPurchaseRatePack());
                } else if (bi.getItem() instanceof Amp) {
                    bi.setRate(pbi.getPurchaseRate());
                }
            }

            bi.setSearialNo(serialNo++);
            double netValue = bi.getQty() * bi.getRate();
            bi.setNetValue(-netValue);

            if (f != null) {
                BigDecimal qty = Optional.ofNullable(f.getQuantity()).orElse(BigDecimal.ZERO);
                BigDecimal freeQty = Optional.ofNullable(f.getFreeQuantity()).orElse(BigDecimal.ZERO);
                BigDecimal qtyTotal = qty.add(freeQty);

                BigDecimal costRate = Optional.ofNullable(f.getLineCostRate()).orElse(BigDecimal.ZERO);
                BigDecimal retailRate = Optional.ofNullable(f.getRetailSaleRate()).orElse(BigDecimal.ZERO);
                BigDecimal wholesaleRate = Optional.ofNullable(f.getWholesaleRate()).orElse(BigDecimal.ZERO);

                BigDecimal retailValue = retailRate.multiply(qtyTotal);
                BigDecimal wholesaleValue = wholesaleRate.multiply(qtyTotal);
                BigDecimal freeItemValue = costRate.multiply(freeQty);

                totalLineDiscounts = totalLineDiscounts.add(Optional.ofNullable(f.getLineDiscount()).orElse(BigDecimal.ZERO));
                totalLineExpenses = totalLineExpenses.add(Optional.ofNullable(f.getLineExpense()).orElse(BigDecimal.ZERO));
                totalTaxLines = totalTaxLines.add(Optional.ofNullable(f.getLineTax()).orElse(BigDecimal.ZERO));
                totalLineCosts = totalLineCosts.add(Optional.ofNullable(f.getLineCost()).orElse(BigDecimal.ZERO));

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
                lineNetTotal = lineNetTotal.add(Optional.ofNullable(f.getLineNetTotal()).orElse(BigDecimal.ZERO));

                totalDiscount = totalDiscount.add(Optional.ofNullable(f.getTotalDiscount()).orElse(BigDecimal.ZERO));
                totalExpense = totalExpense.add(Optional.ofNullable(f.getTotalExpense()).orElse(BigDecimal.ZERO));
                totalCost = totalCost.add(Optional.ofNullable(f.getTotalCost()).orElse(BigDecimal.ZERO));
                totalTax = totalTax.add(Optional.ofNullable(f.getTotalTax()).orElse(BigDecimal.ZERO));
            }
        }

        // Set legacy totals on Bill
        System.out.println("DEBUG: SERVICE - netTotal from line items: " + netTotal);
        
        // Calculate current bill expenses total from bill expense items
        double currentBillExpensesTotal = 0.0;
        System.out.println("DEBUG: SERVICE - Bill expenses count: " + (bill.getBillExpenses() != null ? bill.getBillExpenses().size() : 0));
        if (bill.getBillExpenses() != null && !bill.getBillExpenses().isEmpty()) {
            for (com.divudi.core.entity.BillItem expense : bill.getBillExpenses()) {
                System.out.println("DEBUG: SERVICE - Adding expense: " + expense.getItem().getName() + ", value: " + expense.getNetValue());
                currentBillExpensesTotal += expense.getNetValue();
            }
        }
        System.out.println("DEBUG: SERVICE - currentBillExpensesTotal: " + currentBillExpensesTotal);
        
        // Use line-level aggregation only (no bill-level distribution)
        BigDecimal finalNetTotal = lineNetTotal.add(BigDecimal.valueOf(currentBillExpensesTotal));
        System.out.println("DEBUG: SERVICE - finalNetTotal (lineNetTotal + expenses): " + finalNetTotal);
        
        bill.setTotal(lineGrossTotal.doubleValue());
        bill.setNetTotal(finalNetTotal.doubleValue());
        bill.setSaleValue(totalRetail.doubleValue());
        
        System.out.println("DEBUG: SERVICE - Bill.netTotal set to: " + bill.getNetTotal());
        System.out.println("DEBUG: SERVICE - Bill.total set to: " + bill.getTotal());
        System.out.println("DEBUG: SERVICE - Bill.saleValue set to: " + bill.getSaleValue());

        // Ensure BillFinanceDetails is present
        BillFinanceDetails bfd = bill.getBillFinanceDetails();
        if (bfd == null) {
            bfd = new BillFinanceDetails(bill);
            bill.setBillFinanceDetails(bfd);
        }

        // Set calculated values
        bfd.setBillDiscount(billDiscount);
        bfd.setBillExpense(billExpense);
        bfd.setBillTaxValue(billTax);
        bfd.setBillCostValue(billCost);

        bfd.setLineDiscount(totalLineDiscounts);
        bfd.setLineExpense(totalLineExpenses);
        bfd.setItemTaxValue(totalTaxLines);
        bfd.setLineCostValue(totalLineCosts);

        bfd.setTotalDiscount(totalLineDiscounts);
        bfd.setTotalExpense(totalLineExpenses.add(billExpense));
        bfd.setTotalTaxValue(totalTaxLines);
        bfd.setTotalCostValue(totalLineCosts);

        bfd.setTotalOfFreeItemValues(totalFreeItemValue);
        bfd.setTotalPurchaseValue(totalPurchase);
        bfd.setTotalRetailSaleValue(totalRetail);
        bfd.setTotalWholesaleValue(totalWholesale);
        // DEBUG: Log the values being set in BillFinanceDetails
        // DEBUG: Log the values being set in BillFinanceDetails
        // DEBUG: Log the values being set in BillFinanceDetails
        // DEBUG: Log the values being set in BillFinanceDetails
        // DEBUG: Log the values being set in BillFinanceDetails
        // DEBUG: Log the values being set in BillFinanceDetails
        // DEBUG: Log the values being set in BillFinanceDetails
        // DEBUG: Log the values being set in BillFinanceDetails

        bfd.setTotalQuantity(totalQty);
        bfd.setTotalFreeQuantity(totalFreeQty);
        bfd.setTotalQuantityInAtomicUnitOfMeasurement(totalQtyAtomic);
        bfd.setTotalFreeQuantityInAtomicUnitOfMeasurement(totalFreeQtyAtomic);

        bfd.setGrossTotal(lineGrossTotal);
        bfd.setLineGrossTotal(lineGrossTotal);
        bfd.setNetTotal(finalNetTotal);
        bfd.setLineNetTotal(lineNetTotal);
        
    }

    public void calculateBillTotalsFromItemsForTransferOuts(Bill bill, List<BillItem> billItems) {
        int serialNo = 0;

        BigDecimal billDiscount = BigDecimal.valueOf(bill.getDiscount());
        BigDecimal billExpense = BigDecimal.valueOf(bill.getExpenseTotal());
        BigDecimal billTax = BigDecimal.valueOf(bill.getTax());
        BigDecimal billCost = billDiscount.subtract(billExpense.add(billTax));

        BigDecimal totalLineDiscounts = BigDecimal.ZERO;
        BigDecimal totalLineExpenses = BigDecimal.ZERO;
        BigDecimal totalLineCosts = BigDecimal.ZERO;
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
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (BillItem bi : billItems) {
            if (bi == null) {
                continue;
            }

            PharmaceuticalBillItem pbi = bi.getPharmaceuticalBillItem();
            BillItemFinanceDetails f = bi.getBillItemFinanceDetails();

            if (pbi == null || f == null) {
                continue;
            }

            bi.setSearialNo(serialNo++);
            double netValue = bi.getQty() * bi.getRate();
            bi.setNetValue(-netValue);

            BigDecimal qty = Optional.ofNullable(f.getQuantity()).orElse(BigDecimal.ZERO);
            BigDecimal grossRate = Optional.ofNullable(f.getLineGrossRate()).orElse(BigDecimal.ZERO);

            // Fallback: calculate grossTotal if missing or 0
            if (f.getGrossTotal() == null || f.getGrossTotal().compareTo(BigDecimal.ZERO) == 0) {
                f.setGrossTotal(grossRate.multiply(qty));
            }

            // Fallback net values
            if (f.getNetTotal() == null || f.getNetTotal().compareTo(BigDecimal.ZERO) == 0) {
                f.setNetTotal(f.getGrossTotal());
            }
            if (f.getLineNetTotal() == null) {
                f.setLineNetTotal(f.getLineGrossTotal());
            }

            BigDecimal freeQty = Optional.ofNullable(f.getFreeQuantity()).orElse(BigDecimal.ZERO);
            BigDecimal qtyTotal = qty.add(freeQty);

            BigDecimal costRate = Optional.ofNullable(f.getLineCostRate()).orElse(BigDecimal.ZERO);
            BigDecimal retailRate = Optional.ofNullable(f.getRetailSaleRate()).orElse(BigDecimal.ZERO);
            BigDecimal wholesaleRate = Optional.ofNullable(f.getWholesaleRate()).orElse(BigDecimal.ZERO);

            BigDecimal retailValue = retailRate.multiply(qtyTotal);
            BigDecimal wholesaleValue = wholesaleRate.multiply(qtyTotal);
            BigDecimal freeItemValue = costRate.multiply(freeQty);

            totalLineDiscounts = totalLineDiscounts.add(Optional.ofNullable(f.getLineDiscount()).orElse(BigDecimal.ZERO));
            totalLineExpenses = totalLineExpenses.add(Optional.ofNullable(f.getLineExpense()).orElse(BigDecimal.ZERO));
            totalTaxLines = totalTaxLines.add(Optional.ofNullable(f.getLineTax()).orElse(BigDecimal.ZERO));
            totalLineCosts = totalLineCosts.add(Optional.ofNullable(f.getLineCost()).orElse(BigDecimal.ZERO));
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
            lineNetTotal = lineNetTotal.add(Optional.ofNullable(f.getLineNetTotal()).orElse(BigDecimal.ZERO));
            totalDiscount = totalDiscount.add(Optional.ofNullable(f.getTotalDiscount()).orElse(BigDecimal.ZERO));
            totalExpense = totalExpense.add(Optional.ofNullable(f.getTotalExpense()).orElse(BigDecimal.ZERO));
            totalCost = totalCost.add(Optional.ofNullable(f.getTotalCost()).orElse(BigDecimal.ZERO));
            totalTax = totalTax.add(Optional.ofNullable(f.getTotalTax()).orElse(BigDecimal.ZERO));
        }

        bill.setTotal(grossTotal.doubleValue());
        bill.setNetTotal(netTotal.doubleValue());
        bill.setSaleValue(totalRetail.doubleValue());

        BillFinanceDetails bfd = bill.getBillFinanceDetails();
        if (bfd == null) {
            bfd = new BillFinanceDetails(bill);
            bill.setBillFinanceDetails(bfd);
        }

        bfd.setBillDiscount(billDiscount);
        bfd.setBillExpense(billExpense);
        bfd.setBillTaxValue(billTax);
        bfd.setBillCostValue(billCost);
        bfd.setLineDiscount(totalLineDiscounts);
        bfd.setLineExpense(totalLineExpenses);
        bfd.setItemTaxValue(totalTaxLines);
        bfd.setLineCostValue(totalLineCosts);
        bfd.setTotalDiscount(totalDiscount);
        bfd.setTotalExpense(totalExpense);
        bfd.setTotalTaxValue(totalTax);
        bfd.setTotalCostValue(totalCost);
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

    /**
     * Updates BillFinanceDetails for retail sales by calculating from existing bill items.
     * This method does not alter existing calculation logic but creates new specific calculation for retail sales.
     * @param bill The retail sale bill to update
     */
    public void updateBillFinanceDetailsForRetailSale(Bill bill) {
        
        if (bill == null || bill.getBillItems() == null || bill.getBillItems().isEmpty()) {
            return;
        }

        // Initialize totals
        BigDecimal totalRetailValue = BigDecimal.ZERO;
        BigDecimal totalPurchaseValue = BigDecimal.ZERO;
        BigDecimal totalCostValue = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalFreeQuantity = BigDecimal.ZERO;

        
        // Calculate totals from bill items
        for (BillItem billItem : bill.getBillItems()) {
            if (billItem.isRetired()) {
                continue;
            }
            
            PharmaceuticalBillItem pbi = billItem.getPharmaceuticalBillItem();
            if (pbi == null) {
                continue;
            }

            // Get quantities
            BigDecimal qty = BigDecimal.valueOf(billItem.getQty());
            BigDecimal freeQty = BigDecimal.valueOf(pbi.getFreeQty());
            BigDecimal totalQty = qty.add(freeQty);

            // Get rates
            BigDecimal retailRate = BigDecimal.valueOf(pbi.getRetailRate()) ;
            BigDecimal purchaseRate = BigDecimal.valueOf(pbi.getPurchaseRate()) ;

            // Calculate values
            BigDecimal itemRetailValue = retailRate.multiply(totalQty);
            BigDecimal itemPurchaseValue = purchaseRate.multiply(totalQty);
            BigDecimal itemCostValue = purchaseRate.multiply(totalQty); // Using purchase rate as cost for retail sales

            // Add to totals
            totalRetailValue = totalRetailValue.add(itemRetailValue);
            totalPurchaseValue = totalPurchaseValue.add(itemPurchaseValue);
            totalCostValue = totalCostValue.add(itemCostValue);
            totalQuantity = totalQuantity.add(qty);
            totalFreeQuantity = totalFreeQuantity.add(freeQty);

        }

        // Ensure BillFinanceDetails exists
        BillFinanceDetails bfd = bill.getBillFinanceDetails();
        if (bfd == null) {
            bfd = new BillFinanceDetails(bill);
            bill.setBillFinanceDetails(bfd);
        }

        // Update the key values needed for reports
        bfd.setTotalRetailSaleValue(totalRetailValue);
        bfd.setTotalPurchaseValue(totalPurchaseValue);
        bfd.setTotalCostValue(totalCostValue);
        bfd.setTotalQuantity(totalQuantity);
        bfd.setTotalFreeQuantity(totalFreeQuantity);

        // Set basic totals from bill
        BigDecimal netTotal = BigDecimal.valueOf(bill.getNetTotal());
        BigDecimal grossTotal = BigDecimal.valueOf(bill.getTotal());
        bfd.setNetTotal(netTotal);
        bfd.setGrossTotal(grossTotal);

    }

    public void calculateBillTotalsFromItemsForDisposalIssue(Bill bill, List<BillItem> billItems) {
        int serialNo = 0;

        BigDecimal billDiscount = BigDecimal.valueOf(bill.getDiscount());
        BigDecimal billExpense = BigDecimal.valueOf(bill.getExpenseTotal());
        BigDecimal billTax = BigDecimal.valueOf(bill.getTax());
        BigDecimal billCost = billDiscount.subtract(billExpense.add(billTax));

        BigDecimal totalLineDiscounts = BigDecimal.ZERO;
        BigDecimal totalLineExpenses = BigDecimal.ZERO;
        BigDecimal totalLineCosts = BigDecimal.ZERO;
        BigDecimal totalTaxLines = BigDecimal.ZERO;
        BigDecimal totalFreeItemValue = BigDecimal.ZERO;
        BigDecimal totalPurchase = BigDecimal.ZERO;
        BigDecimal totalRetail = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalWholesale = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalFreeQty = BigDecimal.ZERO;
        BigDecimal totalQtyAtomic = BigDecimal.ZERO;
        BigDecimal totalFreeQtyAtomic = BigDecimal.ZERO;
        BigDecimal grossTotal = BigDecimal.ZERO;
        BigDecimal lineGrossTotal = BigDecimal.ZERO;
        BigDecimal netTotal = BigDecimal.ZERO;
        BigDecimal lineNetTotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (BillItem bi : billItems) {
            if (bi == null) {
                continue;
            }

            PharmaceuticalBillItem pbi = bi.getPharmaceuticalBillItem();
            BillItemFinanceDetails f = bi.getBillItemFinanceDetails();

            if (pbi == null || f == null) {
                continue;
            }

            bi.setSearialNo(serialNo++);
            double netValue = bi.getQty() * bi.getRate();
            bi.setNetValue(-netValue);

            BigDecimal qty = Optional.ofNullable(f.getQuantity()).orElse(BigDecimal.ZERO);
            BigDecimal grossRate = Optional.ofNullable(f.getLineGrossRate()).orElse(BigDecimal.ZERO);

            // Fallback: calculate grossTotal if missing or 0
            if (f.getGrossTotal() == null || f.getGrossTotal().compareTo(BigDecimal.ZERO) == 0) {
                f.setGrossTotal(grossRate.multiply(qty));
            }

            // Fallback net values
            if (f.getNetTotal() == null || f.getNetTotal().compareTo(BigDecimal.ZERO) == 0) {
                f.setNetTotal(f.getGrossTotal());
            }
            if (f.getLineNetTotal() == null) {
                f.setLineNetTotal(f.getLineGrossTotal());
            }

            BigDecimal freeQty = Optional.ofNullable(f.getFreeQuantity()).orElse(BigDecimal.ZERO);
            BigDecimal qtyTotal = qty.add(freeQty);

            BigDecimal costRate = Optional.ofNullable(f.getLineCostRate()).orElse(BigDecimal.ZERO);
            if (costRate.compareTo(BigDecimal.ZERO) == 0 && f.getValueAtCostRate() != null && qty.compareTo(BigDecimal.ZERO) > 0) {
                costRate = f.getValueAtCostRate().divide(qty, 4, RoundingMode.HALF_UP);
                f.setLineCostRate(costRate);
            }
            
            BigDecimal retailRate = Optional.ofNullable(f.getRetailSaleRate()).orElse(BigDecimal.ZERO);
            BigDecimal wholesaleRate = Optional.ofNullable(f.getWholesaleRate()).orElse(BigDecimal.ZERO);

            BigDecimal retailValue = retailRate.multiply(qtyTotal);
            BigDecimal costValue = costRate.multiply(qtyTotal);
            BigDecimal wholesaleValue = wholesaleRate.multiply(qtyTotal);
            BigDecimal freeItemValue = costRate.multiply(freeQty);

            totalLineDiscounts = totalLineDiscounts.add(Optional.ofNullable(f.getLineDiscount()).orElse(BigDecimal.ZERO));
            totalLineExpenses = totalLineExpenses.add(Optional.ofNullable(f.getLineExpense()).orElse(BigDecimal.ZERO));
            totalTaxLines = totalTaxLines.add(Optional.ofNullable(f.getLineTax()).orElse(BigDecimal.ZERO));
            totalLineCosts = totalLineCosts.add(Optional.ofNullable(f.getLineCost()).orElse(BigDecimal.ZERO));
            totalFreeItemValue = totalFreeItemValue.add(freeItemValue);
            totalPurchase = totalPurchase.add(Optional.ofNullable(f.getValueAtPurchaseRate()).orElse(BigDecimal.ZERO));
            totalRetail = totalRetail.add(retailValue);
            totalCost = totalCost.add(costValue);
            totalWholesale = totalWholesale.add(wholesaleValue);
            totalQty = totalQty.add(qty);
            totalFreeQty = totalFreeQty.add(freeQty);
            totalQtyAtomic = totalQtyAtomic.add(Optional.ofNullable(f.getQuantityByUnits()).orElse(BigDecimal.ZERO));
            totalFreeQtyAtomic = totalFreeQtyAtomic.add(Optional.ofNullable(f.getFreeQuantityByUnits()).orElse(BigDecimal.ZERO));
            grossTotal = grossTotal.add(Optional.ofNullable(f.getLineNetTotal()).orElse(BigDecimal.ZERO));
            lineGrossTotal = lineGrossTotal.add(Optional.ofNullable(f.getLineGrossTotal()).orElse(BigDecimal.ZERO));
            netTotal = netTotal.add(Optional.ofNullable(f.getNetTotal()).orElse(BigDecimal.ZERO));
            lineNetTotal = lineNetTotal.add(Optional.ofNullable(f.getLineNetTotal()).orElse(BigDecimal.ZERO));
            totalDiscount = totalDiscount.add(Optional.ofNullable(f.getTotalDiscount()).orElse(BigDecimal.ZERO));
            totalExpense = totalExpense.add(Optional.ofNullable(f.getTotalExpense()).orElse(BigDecimal.ZERO));
            totalTax = totalTax.add(Optional.ofNullable(f.getTotalTax()).orElse(BigDecimal.ZERO));
        }

        bill.setTotal(grossTotal.doubleValue());
        bill.setNetTotal(netTotal.doubleValue());
        bill.setSaleValue(totalRetail.doubleValue());

        BillFinanceDetails bfd = bill.getBillFinanceDetails();
        if (bfd == null) {
            bfd = new BillFinanceDetails(bill);
            bill.setBillFinanceDetails(bfd);
        }

        bfd.setBillDiscount(billDiscount);
        bfd.setBillExpense(billExpense);
        bfd.setBillTaxValue(billTax);
        bfd.setBillCostValue(billCost);
        bfd.setLineDiscount(totalLineDiscounts);
        bfd.setLineExpense(totalLineExpenses);
        bfd.setItemTaxValue(totalTaxLines);
        bfd.setLineCostValue(totalLineCosts);
        bfd.setTotalDiscount(totalDiscount);
        bfd.setTotalExpense(totalExpense);
        bfd.setTotalTaxValue(totalTax);
        bfd.setTotalCostValue(totalCost);
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

}
