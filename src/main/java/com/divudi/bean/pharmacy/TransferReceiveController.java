/*
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.common.SessionController;
import com.divudi.bean.common.ConfigOptionApplicationController;
import com.divudi.core.util.JsfUtil;
import com.divudi.core.data.BillClassType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.persistence.TemporalType;
import com.divudi.core.data.BillNumberSuffix;
import com.divudi.core.data.BillType;
import com.divudi.core.data.BillTypeAtomic;
import com.divudi.core.data.dataStructure.SearchKeyword;
import com.divudi.ejb.BillNumberGenerator;

import com.divudi.ejb.PharmacyBean;
import com.divudi.ejb.PharmacyCalculation;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.BillItemFinanceDetails;
import com.divudi.core.entity.BilledBill;
import com.divudi.core.entity.CancelledBill;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.entity.pharmacy.Stock;
import com.divudi.core.entity.pharmacy.ItemBatch;
import com.divudi.core.entity.pharmacy.Vmp;
import com.divudi.core.entity.pharmacy.Vmpp;
import com.divudi.core.entity.pharmacy.Ampp;
import com.divudi.core.entity.Item;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.BillItemFacade;
import com.divudi.core.facade.PharmaceuticalBillItemFacade;
import com.divudi.core.facade.StockFacade;
import com.divudi.core.util.CommonFunctions;
import com.divudi.service.BillService;
import com.divudi.service.pharmacy.PharmacyCostingService;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class TransferReceiveController implements Serializable {

    private Bill issuedBill;
    private Bill receivedBill;
    private boolean printPreview;
    private boolean showAllBillFormats = false;
    private Date fromDate;
    private Date toDate;
    ///////
    @Inject
    private SessionController sessionController;
    @Inject
    private PharmacyController pharmacyController;
    ////
    @EJB
    private BillFacade billFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private BillItemFacade billItemFacade;
    ////
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private BillNumberGenerator billNumberBean;
    @EJB
    private StockFacade stockFacade;
    @EJB
    BillService billService;

    @EJB
    private PharmacyCostingService pharmacyCostingService;

    @Inject
    private ConfigOptionApplicationController configOptionApplicationController;

    @Inject
    private PharmacyCalculation pharmacyCalculation;
    private List<Bill> bills;
    private SearchKeyword searchKeyword;
    private BillItem selectedBillItem;

    public static class ConfigOptionInfo {

        private final String key;
        private final String defaultValue;

        public ConfigOptionInfo(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public String getKey() {
            return key;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }

    public void onFocus(BillItem tmp) {
        getPharmacyController().setPharmacyItem(tmp.getItem());
    }

    public String navigateBackToRecieveList() {
        return "/pharmacy/pharmacy_transfer_issued_list?faces-redirect=true";
    }

    public String navigateToRecieveRequest() {
        if (issuedBill == null) {
            JsfUtil.addErrorMessage("Nothing to received");
            return null;
        }
        // Check if already fully received to prevent over-receiving
        if (isAlreadyReceived(issuedBill)) {
            JsfUtil.addErrorMessage("Already Received!");
            return null;
        }
        printPreview=false;
        generateBillComponent();
        return "/pharmacy/pharmacy_transfer_receive?faces-redirect=true";
    }

    public String navigateToEditRecieveIssue() {
        return "/pharmacy/pharmacy_transfer_receive_with_approval?faces-redirect=true";
    }

    public String navigateToAproveRecieveIssue() {
        return "/pharmacy/pharmacy_transfer_receive_approval?faces-redirect=true";
    }

    public String navigateToReprintRecieveIssue() {
        return "/pharmacy/pharmacy_reprint_transfer_receive?faces-redirect=true";
    }

    public void makeNull() {
        issuedBill = null;
        receivedBill = null;
        printPreview = false;
        fromDate = null;
        toDate = null;
        selectedBillItem = null;
    }

    public TransferReceiveController() {
    }

    public Bill getIssuedBill() {
        if (issuedBill == null) {
            issuedBill = new BilledBill();
        }
        return issuedBill;
    }

    public void setIssuedBill(Bill issuedBill) {
        this.issuedBill = issuedBill;
    }

//   public String navigateBackToRecieveList(){
//        return "/pharmacy/pharmacy_transfer_issued_list?faces-redirect=true";
//    }
    public String navigateToRecieveIssue() {
        return "/pharmacy/pharmacy_transfer_receive_with_approval?faces-redirect=true";
    }

    public void generateBillComponentOld() {

        for (PharmaceuticalBillItem i : getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(getIssuedBill())) {

            BillItem bItem = new BillItem();
            bItem.setReferanceBillItem(i.getBillItem());
            bItem.copy(i.getBillItem());
            bItem.setTmpQty(Math.abs(i.getQty()));

            //       bItem.setTmpSuggession(getPharmacyCalculation().getSuggessionOnly(i.getBillItem().getItem()));
            PharmaceuticalBillItem phItem = new PharmaceuticalBillItem();
            phItem.setBillItem(bItem);
            phItem.copy(i);
            phItem.invertValue(i);

            bItem.setPharmaceuticalBillItem(phItem);

            getIssuedBill().getBillItems().add(bItem);
            bItem.setSearialNo(getIssuedBill().getBillItems().size());

        }

    }

    public void generateBillComponent() {
        // Final safety check before creating any bill items
        if (isAlreadyReceived(issuedBill)) {
            JsfUtil.addErrorMessage("Already Received - Cannot generate receive items!");
            return;
        }
        
        receivedBill = new BilledBill();
        getReceivedBill();
        getReceivedBill().setReferenceBill(issuedBill);

        getReceivedBill().setFromInstitution(issuedBill.getFromInstitution());
        getReceivedBill().setFromDepartment(issuedBill.getFromDepartment());

        getReceivedBill().setToInstitution(issuedBill.getToInstitution());
        getReceivedBill().setToDepartment(issuedBill.getToDepartment());

        getReceivedBill().setStaff(issuedBill.getStaff());

        List<BillItem> issuedBillItems = billService.fetchBillItems(issuedBill);
        for (BillItem issuedBillItem : issuedBillItems) {
            double remainingQty = calculateRemainingQtyWithFreshData(issuedBillItem);
            if (remainingQty <= 0.001) { // Add tolerance for floating point precision
                continue;
            }

            BillItem newlyCreatedReceivedBillItem = new BillItem();
            newlyCreatedReceivedBillItem.copyWithPharmaceuticalAndFinancialData(issuedBillItem);

            // Invert to turn negative values from the issued bill into positives
            newlyCreatedReceivedBillItem.invertValue();
            newlyCreatedReceivedBillItem.getPharmaceuticalBillItem().invertValue();

            // Adjust quantity to remaining amount
            double unitsPerPack = 1.0;
            Item item = newlyCreatedReceivedBillItem.getItem();
            if (item instanceof Ampp || item instanceof Vmpp) {
                unitsPerPack = item.getDblValue() > 0 ? item.getDblValue() : 1.0;
            }

            double packs = remainingQty / unitsPerPack;
            newlyCreatedReceivedBillItem.setQty(packs);
            newlyCreatedReceivedBillItem.getPharmaceuticalBillItem().setQty(remainingQty);

            // Fix rates by retrieving correct values from ItemBatch
            PharmaceuticalBillItem pbi = newlyCreatedReceivedBillItem.getPharmaceuticalBillItem();
            if (pbi != null && pbi.getItemBatch() != null) {
                ItemBatch itemBatch = pbi.getItemBatch();
                
                
                // Set correct purchase rates from ItemBatch
                pbi.setPurchaseRate(itemBatch.getPurcahseRate());
                pbi.setPurchaseRatePack(itemBatch.getPurcahseRate() * unitsPerPack);
                pbi.setPurchaseValue(itemBatch.getPurcahseRate() * remainingQty);
                
                // Set correct retail rates from ItemBatch
                pbi.setRetailRate(itemBatch.getRetailsaleRate());
                pbi.setRetailRatePack(itemBatch.getRetailsaleRate() * unitsPerPack);
                pbi.setRetailValue(itemBatch.getRetailsaleRate() * remainingQty);
                
                // Set correct cost rates from ItemBatch
                pbi.setCostRate(itemBatch.getCostRate());
                pbi.setCostRatePack(itemBatch.getCostRate() * unitsPerPack);
                pbi.setCostValue(itemBatch.getCostRate() * remainingQty);
                
            } else {
                if (pbi != null) {
                }
            }

            // Ensure finance details reflect positive quantities and rates
            BillItemFinanceDetails fd = newlyCreatedReceivedBillItem.getBillItemFinanceDetails();
            if (fd != null) {
                fd.setQuantity(BigDecimal.valueOf(packs));
                if (fd.getLineGrossRate() != null) {
                    fd.setLineGrossRate(fd.getLineGrossRate().abs());
                }
                updateFinancialsForTransferReceiveFromReference(fd, issuedBillItem);
            }

            newlyCreatedReceivedBillItem.setReferanceBillItem(issuedBillItem);
            newlyCreatedReceivedBillItem.setBill(receivedBill);
            getReceivedBill().getBillItems().add(newlyCreatedReceivedBillItem);
            newlyCreatedReceivedBillItem.setSearialNo(getReceivedBill().getBillItems().size());
        }

        pharmacyCostingService.calculateBillTotalsFromItemsForTransferOuts(getReceivedBill(), getReceivedBill().getBillItems());
    }

    public String navigateToPharmacyReceiveForRequests() {
        if (issuedBill == null || issuedBill.getId() == null) {
            JsfUtil.addErrorMessage("No Bill Selected");
            return "";
        }

        if (isAlreadyReceived(issuedBill)) {
            JsfUtil.addErrorMessage("Already Received!");
            return "";
        }

        generateBillComponent();
        printPreview = false;
        return "/pharmacy/pharmacy_transfer_receive?faces-redirect=true";
    }

    public boolean isAlreadyReceived(Bill bill) {
        if (bill == null) {
            return false;
        }
        
        // Get fresh data from the database to avoid caching issues
        Bill freshIssueBill = billFacade.find(bill.getId());
        if (freshIssueBill == null) {
            return false;
        }
        
        List<BillItem> issueItems = billService.fetchBillItems(freshIssueBill);
        for (BillItem bi : issueItems) {
            double remainingQty = calculateRemainingQtyWithFreshData(bi);
            if (remainingQty > 0.001) { // Add small tolerance for floating point precision
                return false; // Still has items remaining to receive
            }
        }
        return true; // All items are fully received
    }
    
    private double calculateRemainingQtyWithFreshData(BillItem issuedItem) {
        double issuedQtyInUnits = 0.0;
        if (issuedItem != null && issuedItem.getPharmaceuticalBillItem() != null) {
            issuedQtyInUnits = Math.abs(issuedItem.getPharmaceuticalBillItem().getQty());
        }

        // Get fresh receive data from database - only saved bills
        String jpql = "SELECT b FROM Bill b WHERE b.backwardReferenceBill.id = :issueBillId AND b.billType = :receiveType AND b.id IS NOT NULL";
        Map<String, Object> params = new HashMap<>();
        params.put("issueBillId", issuedItem.getBill().getId());
        params.put("receiveType", BillType.PharmacyTransferReceive);
        
        List<Bill> receiveBills = billFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);
            
        double totalReceivedQty = 0.0;
        for (Bill receiveBill : receiveBills) {
            if (receiveBill.getBillClassType() == BillClassType.CancelledBill) {
                continue; // Skip cancelled receives
            }
            
            // Include all receive bills including the current one being processed
            // (We want to count all receives to determine if the issue is fully complete)
            
            List<BillItem> receiveItems = billService.fetchBillItems(receiveBill);
            for (BillItem receiveItem : receiveItems) {
                // Only count items that reference the same issued item
                if (receiveItem.getReferanceBillItem() != null && 
                    Objects.equals(receiveItem.getReferanceBillItem().getId(), issuedItem.getId()) &&
                    receiveItem.getPharmaceuticalBillItem() != null) {
                    totalReceivedQty += Math.abs(receiveItem.getPharmaceuticalBillItem().getQty());
                }
            }
        }

        return issuedQtyInUnits - totalReceivedQty;
    }
    
    private boolean wouldCauseOverReceiving() {
        if (getReceivedBill() == null || getIssuedBill() == null) {
            return false;
        }
        
        List<BillItem> receivingItems = getReceivedBill().getBillItems();
        
        if (receivingItems == null || receivingItems.isEmpty()) {
            return false;
        }
        
        // Check each receiving item against its reference issued item
        for (BillItem receivingItem : receivingItems) {
            if (receivingItem.getReferanceBillItem() == null) {
                continue; // Skip items without reference
            }
            
            BillItem issuedItem = receivingItem.getReferanceBillItem();
            
            // Get current remaining quantity (excluding this receive bill)
            double currentRemainingQty = calculateRemainingQtyWithFreshData(issuedItem);
            
            // Get the quantity being received for this specific item
            double quantityBeingReceived = 0.0;
            if (receivingItem.getPharmaceuticalBillItem() != null) {
                quantityBeingReceived = Math.abs(receivingItem.getPharmaceuticalBillItem().getQty());
            }
            
            // Check if this receive would cause over-receiving
            if (quantityBeingReceived > currentRemainingQty + 0.001) { // Add tolerance
                return true; // Would cause over-receiving
            }
        }
        
        return false; // All quantities are within limits
    }

    public void settle() {
        if (getReceivedBill().getBillItems() == null || getReceivedBill().getBillItems().isEmpty()) {
            JsfUtil.addErrorMessage("Nothing to Recive, Please check Recieved Quantity");
            return;
        }
        
        // Additional validation: Check if trying to over-receive
        if (isAlreadyReceived(getIssuedBill())) {
            JsfUtil.addErrorMessage("Cannot receive - already fully received!");
            return;
        }
        
        // Validate that current receive quantities don't exceed remaining quantities
        if (wouldCauseOverReceiving()) {
            JsfUtil.addErrorMessage("Cannot receive - quantities exceed remaining amounts!");
            return;
        }

        saveBill();
        for (BillItem i : getReceivedBill().getBillItems()) {
            if (i.getPharmaceuticalBillItem().getQty() == 0.0) {
                continue;
            }
            if (errorCheck(i)) {
                continue;
            }
            if (i.getId() == null) {
                i.setCreatedAt(Calendar.getInstance().getTime());
                i.setCreater(getSessionController().getLoggedUser());
                getBillItemFacade().create(i);
            } else {
                getBillItemFacade().edit(i);
            }
            double qty = Math.abs(i.getPharmaceuticalBillItem().getQty());

            boolean returnFlag = getPharmacyBean().deductFromStock(i.getPharmaceuticalBillItem(), Math.abs(qty), getIssuedBill().getToStaff());

            if (returnFlag) {
                Stock addedStock = getPharmacyBean().addToStock(i.getPharmaceuticalBillItem(), Math.abs(qty), getSessionController().getDepartment());
                i.getPharmaceuticalBillItem().setStock(addedStock);
            } else {
                i.getPharmaceuticalBillItem().setQty(0);
                getBillItemFacade().edit(i);
            }
        }

        getReceivedBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), BillType.PharmacyTransferReceive, BillClassType.BilledBill, BillNumberSuffix.PHTI));
        getReceivedBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), BillType.PharmacyTransferReceive, BillClassType.BilledBill, BillNumberSuffix.PHTI));

        getReceivedBill().setInstitution(getSessionController().getInstitution());
        getReceivedBill().setDepartment(getSessionController().getDepartment());

        getReceivedBill().setCreater(getSessionController().getLoggedUser());
        getReceivedBill().setCreatedAt(Calendar.getInstance().getTime());

        getReceivedBill().setBackwardReferenceBill(getIssuedBill());

        getBillFacade().edit(getReceivedBill());

        getIssuedBill().getForwardReferenceBills().add(getReceivedBill());
        fillData(getReceivedBill());
        getBillFacade().edit(getIssuedBill());
        getBillFacade().edit(getReceivedBill());
        
        // Check if Transfer Issue is fully received and update fullyIssued status
        if (getIssuedBill() != null && !getIssuedBill().isFullyIssued()) {
            if (isAlreadyReceived(getIssuedBill())) {
                getIssuedBill().setFullyIssued(true);
                getIssuedBill().setFullyIssuedAt(new Date());
                getIssuedBill().setFullyIssuedBy(getSessionController().getLoggedUser());
                getBillFacade().edit(getIssuedBill());
            }
        }
        
        printPreview = true;
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

        double netTotal = 0.0;


        for (BillItem bi : inputBill.getBillItems()) {

            BillItemFinanceDetails bifd = bi.getBillItemFinanceDetails();
            PharmaceuticalBillItem pbi = bi.getPharmaceuticalBillItem();

            if (bifd == null) {
                continue;
            }

            if (pbi.getStock() == null || pbi.getStock().getItemBatch() == null) {
                continue;
            }

            double unitsPerPackValue = bifd.getUnitsPerPack() != null ? bifd.getUnitsPerPack().doubleValue() : 1.0;
            
            // Use rates from the PharmaceuticalBillItem to preserve original purchase rates
            double purchaseRate = pbi.getPurchaseRatePack() > 0 ? pbi.getPurchaseRatePack() : pbi.getStock().getItemBatch().getPurcahseRate() * unitsPerPackValue;
            double retailRate = pbi.getRetailRatePack() > 0 ? pbi.getRetailRatePack() : pbi.getStock().getItemBatch().getRetailsaleRate() * unitsPerPackValue;
            double wholesaleRate = pbi.getWholesaleRatePack() > 0 ? pbi.getWholesaleRatePack() : pbi.getStock().getItemBatch().getWholesaleRate() * unitsPerPackValue;
            double costRate = pbi.getStock().getItemBatch().getCostRate() * unitsPerPackValue;
            


            billTotalAtCostRate += bifd.getTotalCost() != null ? bifd.getTotalCost().doubleValue() : 0.0;

            double paidQty = pbi.getQty() / unitsPerPackValue;
            double freeQty = pbi.getFreeQty() / unitsPerPackValue;


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

            // Fix: Set GROSSRATE to the actual gross rate from line gross rate
            bifd.setGrossRate(bifd.getLineGrossRate() != null ? bifd.getLineGrossRate() : BigDecimal.ZERO);

            BigDecimal biNetTotal = bifd.getNetTotal();
            netTotal += biNetTotal != null ? biNetTotal.doubleValue() : 0.0;

            if (bifd.getQuantityByUnits() == null || bifd.getQuantityByUnits().compareTo(BigDecimal.ZERO) == 0) {
                bifd.setQuantityByUnits(BigDecimal.valueOf(pbi.getQty()));
            }

            double itemCostFree = freeQty * costRate;
            double itemCostNonFree = paidQty * costRate;
            bifd.setTotalCost(BigDecimal.valueOf(itemCostFree + itemCostNonFree));

            // Fix: Set LINECOSTRATE to the actual cost rate from ItemBatch
            bifd.setLineCostRate(BigDecimal.valueOf(costRate));
            // Fix: Set LINECOST using cost rate × quantity
            bifd.setLineCost(BigDecimal.valueOf(costRate).multiply(bifd.getTotalQuantity() != null ? bifd.getTotalQuantity() : BigDecimal.ZERO));

            if (bifd.getQuantityByUnits() != null && bifd.getQuantityByUnits().compareTo(BigDecimal.ZERO) > 0) {
                bifd.setTotalCostRate(BigDecimal.valueOf(itemCostFree + itemCostNonFree)
                        .divide(bifd.getQuantityByUnits(), 6, RoundingMode.HALF_UP));
            } else {
                bifd.setTotalCostRate(BigDecimal.ZERO);
            }

            // Fix: Set value calculations with correct rates
            bifd.setValueAtPurchaseRate(BigDecimal.valueOf(purchaseRate).multiply(bifd.getTotalQuantity() != null ? bifd.getTotalQuantity() : BigDecimal.ZERO)); // VALUEATPURCHASERATE
            bifd.setValueAtRetailRate(BigDecimal.valueOf(retailRate).multiply(bifd.getTotalQuantity() != null ? bifd.getTotalQuantity() : BigDecimal.ZERO)); // VALUEATRETAILRATE  
            bifd.setValueAtCostRate(BigDecimal.valueOf(costRate).multiply(bifd.getTotalQuantity() != null ? bifd.getTotalQuantity() : BigDecimal.ZERO)); // VALUEATCOSTRATE
            bifd.setValueAtWholesaleRate(BigDecimal.valueOf(wholesaleRate).multiply(bifd.getTotalQuantity() != null ? bifd.getTotalQuantity() : BigDecimal.ZERO));

            bifd.setProfitMargin(BigDecimal.ZERO);

            bifd.setTotalDiscount(BigDecimal.ZERO);
            bifd.setTotalDiscountRate(BigDecimal.ZERO);

            bifd.setTotalExpense(BigDecimal.ZERO);
            bifd.setTotalExpenseRate(BigDecimal.ZERO);

            bifd.setTotalTax(BigDecimal.ZERO);
            bifd.setTotalTaxRate(BigDecimal.ZERO);

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
        inputBill.setNetTotal(netTotal);
        inputBill.setGrantTotal(netTotal);
        inputBill.setTotal(netTotal);


    }

    public void saveRequest() {

        getReceivedBill().setBillType(BillType.PharmacyTransferReceive);
        getReceivedBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_RECEIVE_PRE);
        getReceivedBill().setBackwardReferenceBill(getIssuedBill());
        getReceivedBill().setFromStaff(getIssuedBill().getToStaff());
        getReceivedBill().setFromInstitution(getIssuedBill().getInstitution());
        getReceivedBill().setFromDepartment(getIssuedBill().getDepartment());

        if (getReceivedBill().getId() == null) {
            getBillFacade().create(getReceivedBill());
        }

        getReceivedBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), BillType.PharmacyTransferReceive, BillClassType.BilledBill, BillNumberSuffix.PHTI));
        getReceivedBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), BillType.PharmacyTransferReceive, BillClassType.BilledBill, BillNumberSuffix.PHTI));

        getReceivedBill().setInstitution(getSessionController().getInstitution());
        getReceivedBill().setDepartment(getSessionController().getDepartment());

        getReceivedBill().setCreater(getSessionController().getLoggedUser());
        getReceivedBill().setCreatedAt(Calendar.getInstance().getTime());

        getReceivedBill().setBackwardReferenceBill(getIssuedBill());

        pharmacyCostingService.calculateBillTotalsFromItemsForTransferOuts(receivedBill, receivedBill.getBillItems());

        getBillFacade().edit(getReceivedBill());

        //Update Issue Bills Reference Bill
        getIssuedBill().getForwardReferenceBills().add(getReceivedBill());
        getBillFacade().edit(getIssuedBill());
        JsfUtil.addSuccessMessage("Request Saved Successfully");
    }

    public void finalizeRequest() {
        // Check if a bill has been selected
        if (getReceivedBill() == null) {
            JsfUtil.addErrorMessage("No Bill Selected");
            return;
        }

        // Check if the request has been saved before finalizing
        if (getReceivedBill().getId() == null || (getReceivedBill().getForwardReferenceBills() == null && getReceivedBill().getForwardReferenceBills().isEmpty())) {
            // Save the request
            getReceivedBill().setBillType(BillType.PharmacyTransferReceive);
            getReceivedBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_RECEIVE_PRE);
            getReceivedBill().setBackwardReferenceBill(getIssuedBill());
            getReceivedBill().setFromStaff(getIssuedBill().getToStaff());
            getReceivedBill().setFromInstitution(getIssuedBill().getInstitution());
            getReceivedBill().setFromDepartment(getIssuedBill().getDepartment());
            getReceivedBill().setToInstitution(getSessionController().getInstitution());
            getReceivedBill().setToDepartment(getSessionController().getDepartment());

            if (getReceivedBill().getId() == null) {
                getBillFacade().create(getReceivedBill());
            }

            String receiveId = getBillNumberBean().departmentBillNumberGeneratorYearlyByFromDepartmentAndToDepartment(getReceivedBill().getFromDepartment(), getReceivedBill().getToDepartment(), BillTypeAtomic.PHARMACY_RECEIVE);

            getReceivedBill().setDeptId(receiveId);
            getReceivedBill().setInsId(receiveId);

            getReceivedBill().setInstitution(getSessionController().getInstitution());
            getReceivedBill().setDepartment(getSessionController().getDepartment());

            getReceivedBill().setCreater(getSessionController().getLoggedUser());
            getReceivedBill().setCreatedAt(Calendar.getInstance().getTime());

            getReceivedBill().setBackwardReferenceBill(getIssuedBill());

            pharmacyCostingService.calculateBillTotalsFromItemsForTransferOuts(receivedBill, receivedBill.getBillItems());

            getReceivedBill().setEditor(sessionController.getLoggedUser());
            getReceivedBill().setEditedAt(new Date());
            getReceivedBill().setCheckeAt(new Date());
            getReceivedBill().setCheckedBy(sessionController.getLoggedUser());

            //Update Issue Bills Reference Bill
            getIssuedBill().getForwardReferenceBills().add(getReceivedBill());

            // Update the received bill in the database
            getBillFacade().edit(getReceivedBill());
            getBillFacade().edit(getIssuedBill());
            // Add success message
            JsfUtil.addSuccessMessage("Request Finalized Successfully");
        } else {
            // Update the existing received bill with current details
            getReceivedBill().setEditor(sessionController.getLoggedUser());
            getReceivedBill().setEditedAt(new Date());
            getReceivedBill().setCheckeAt(new Date());
            getReceivedBill().setCheckedBy(sessionController.getLoggedUser());

            // Update the received bill in the database
            getBillFacade().edit(getReceivedBill());

            // Add success message
            JsfUtil.addSuccessMessage("Request Finalized Successfully");
        }
    }

    public void settleApprove() {
        if (getReceivedBill().getId() == null) {
            JsfUtil.addErrorMessage("No Bill");
            return;
        }
        
        // Additional validation: Check if trying to over-receive
        if (isAlreadyReceived(getIssuedBill())) {
            JsfUtil.addErrorMessage("Cannot receive - already fully received!");
            return;
        }
        
        // Validate that current receive quantities don't exceed remaining quantities
        if (wouldCauseOverReceiving()) {
            JsfUtil.addErrorMessage("Cannot receive - quantities exceed remaining amounts!");
            return;
        }

        getReceivedBill().setApproveAt(new Date());
        getReceivedBill().setApproveUser(getSessionController().getLoggedUser());

        getReceivedBill().setBillType(BillType.PharmacyTransferReceive);
        getReceivedBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_RECEIVE);
        getReceivedBill().setBackwardReferenceBill(getIssuedBill());
        List<BillItem> itemsToAdd = new ArrayList<>();

        for (BillItem i : getReceivedBill().getBillItems()) {
            if (i.getPharmaceuticalBillItem().getQty() == 0.0 || i.getItem() instanceof Vmpp || i.getItem() instanceof Vmp) {
                continue;
            }

            if (errorCheck(i)) {
                continue;
            }

            i.setBill(getReceivedBill());
            i.setCreatedAt(Calendar.getInstance().getTime());
            i.setCreater(getSessionController().getLoggedUser());
            i.setPharmaceuticalBillItem(i.getPharmaceuticalBillItem());

            PharmaceuticalBillItem tmpPh = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);

            if (i.getId() == null) {
                getBillItemFacade().create(i);
            }

            if (tmpPh.getId() == null) {
                getPharmaceuticalBillItemFacade().create(tmpPh);
            }
            i.setPharmaceuticalBillItem(tmpPh);
            getBillItemFacade().edit(i);

            tmpPh.setItemBatch(tmpPh.getStaffStock().getItemBatch());

            double qty = Math.abs(i.getPharmaceuticalBillItem().getQtyInUnit());

            // Deduct Staff Stock
            boolean returnFlag = getPharmacyBean().deductFromStock(tmpPh, Math.abs(qty), getIssuedBill().getToStaff());

            if (returnFlag) {
                // Add Stock To Department
                Stock addedStock = getPharmacyBean().addToStock(tmpPh, Math.abs(qty), getSessionController().getDepartment());
                tmpPh.setStock(addedStock);
            } else {
                i.setTmpQty(0);
                getBillItemFacade().edit(i);
            }

            getPharmaceuticalBillItemFacade().edit(tmpPh);
            itemsToAdd.add(i);
        }

        if (itemsToAdd.isEmpty()) {
            JsfUtil.addErrorMessage("Nothing to Receive, Please check Received Quantity");
            return;
        }

        pharmacyCostingService.calculateBillTotalsFromItemsForTransferOuts(receivedBill, receivedBill.getBillItems());

        getIssuedBill().setReferenceBill(getReceivedBill());
        getReceivedBill().setReferenceBill(getIssuedBill());

        getBillFacade().edit(getReceivedBill());
        getBillFacade().edit(getIssuedBill());

        // Check if Transfer Issue is fully received and update fullyIssued status
        if (getIssuedBill() != null && !getIssuedBill().isFullyIssued()) {
            if (isAlreadyReceived(getIssuedBill())) {
                getIssuedBill().setFullyIssued(true);
                getIssuedBill().setFullyIssuedAt(new Date());
                getIssuedBill().setFullyIssuedBy(getSessionController().getLoggedUser());
                getBillFacade().edit(getIssuedBill());
            }
        }

        printPreview = true;
    }

    public void onEdit(RowEditEvent event) {
        BillItem tmp = (BillItem) event.getObject();
        if (tmp.getPharmaceuticalBillItem().getStaffStock().getStock() < tmp.getQty()) {
            tmp.setTmpQty(0.0);
            JsfUtil.addErrorMessage("You cant recieved over than Issued Qty setted Old Value");
        }

        //   getPharmacyController().setPharmacyItem(tmp.getItem());
    }

    public boolean errorCheck(BillItem billItem) {
        if (billItem.getPharmaceuticalBillItem().getStaffStock().getStock() < billItem.getQty()) {
            return true;
        }
        return false;
    }

    public void saveBill() {
        getReceivedBill().setBillType(BillType.PharmacyTransferReceive);
        getReceivedBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_RECEIVE);
        getReceivedBill().setBackwardReferenceBill(getIssuedBill());
        getReceivedBill().setFromStaff(getIssuedBill().getToStaff());
        if (getReceivedBill().getId() == null) {
            getReceivedBill().setCreatedAt(new Date());
            getReceivedBill().setCreater(sessionController.getLoggedUser());
            getBillFacade().create(getReceivedBill());
        } else {
            getBillFacade().edit(getReceivedBill());
        }
    }

    private BigDecimal determineTransferRate(ItemBatch itemBatch) {
        boolean pharmacyTransferIsByPurchaseRate = configOptionApplicationController.getBooleanValueByKey("Pharmacy Transfer is by Purchase Rate", false);
        boolean pharmacyTransferIsByCostRate = configOptionApplicationController.getBooleanValueByKey("Pharmacy Transfer is by Cost Rate", false);
        boolean pharmacyTransferIsByRetailRate = configOptionApplicationController.getBooleanValueByKey("Pharmacy Transfer is by Retail Rate", true);

        if (pharmacyTransferIsByPurchaseRate) {
            return BigDecimal.valueOf(itemBatch.getPurcahseRate());
        } else if (pharmacyTransferIsByCostRate) {
            return BigDecimal.valueOf(itemBatch.getCostRate());
        } else {
            return BigDecimal.valueOf(itemBatch.getRetailsaleRate());
        }
    }

    private void updateFinancialsForTransferReceiveFromReference(BillItemFinanceDetails fd, BillItem referenceBillItem) {
        if (fd == null || fd.getBillItem() == null || referenceBillItem == null) {
            return;
        }

        BillItem bi = fd.getBillItem();
        PharmaceuticalBillItem ph = bi.getPharmaceuticalBillItem();
        Item item = bi.getItem();

        BigDecimal qty = Optional.ofNullable(fd.getQuantity()).orElse(BigDecimal.ZERO);

        BigDecimal unitsPerPack = BigDecimal.ONE;
        if (item instanceof Ampp || item instanceof Vmpp) {
            unitsPerPack = item.getDblValue() > 0 ? BigDecimal.valueOf(item.getDblValue()) : BigDecimal.ONE;
        }

        fd.setUnitsPerPack(unitsPerPack);
        fd.setTotalQuantity(qty);
        fd.setQuantity(qty);

        // Get the lineGrossRate from the reference (issued) bill item's finance details
        BigDecimal grossRate = BigDecimal.ZERO;
        if (referenceBillItem.getBillItemFinanceDetails() != null) {
            grossRate = Optional.ofNullable(referenceBillItem.getBillItemFinanceDetails().getLineGrossRate())
                    .orElse(BigDecimal.ZERO).abs(); // Use abs() to ensure positive value
        }
        
        // Fallback: if no rate from reference, use configuration-based determination
        if (grossRate.compareTo(BigDecimal.ZERO) == 0 && ph != null && ph.getItemBatch() != null) {
            grossRate = determineTransferRate(ph.getItemBatch()).multiply(unitsPerPack);
        }
        
        fd.setLineGrossRate(grossRate);
        

        BigDecimal lineGrossTotal = grossRate.multiply(qty);
        fd.setLineGrossTotal(lineGrossTotal);
        fd.setGrossTotal(lineGrossTotal);

        fd.setLineNetRate(grossRate);
        fd.setLineNetTotal(lineGrossTotal);
        fd.setNetTotal(lineGrossTotal);

        BigDecimal qtyByUnits = qty.multiply(unitsPerPack);
        fd.setQuantityByUnits(qtyByUnits);
        fd.setTotalQuantityByUnits(qtyByUnits);

        // Set correct rates for different types from ItemBatch
        if (ph != null && ph.getItemBatch() != null) {
            ItemBatch itemBatch = ph.getItemBatch();
            
            
            // Set purchase rate details
            BigDecimal purchaseRate = BigDecimal.valueOf(itemBatch.getPurcahseRate());
            fd.setValueAtPurchaseRate(purchaseRate.multiply(qtyByUnits));
            
            // Set retail rate details  
            BigDecimal retailRate = BigDecimal.valueOf(itemBatch.getRetailsaleRate());
            fd.setValueAtRetailRate(retailRate.multiply(qtyByUnits));
            
            // Set cost rate details
            BigDecimal costRate = BigDecimal.valueOf(itemBatch.getCostRate());
            fd.setLineCostRate(costRate);
            fd.setLineCost(costRate.multiply(qtyByUnits));
            fd.setValueAtCostRate(costRate.multiply(qtyByUnits));
            fd.setTotalCost(costRate.multiply(qtyByUnits));
            
        } else {
            if (ph != null) {
            }
        }

        fd.setLineDiscount(BigDecimal.ZERO);
        fd.setLineExpense(BigDecimal.ZERO);
        fd.setLineTax(BigDecimal.ZERO);
        fd.setTotalDiscount(BigDecimal.ZERO);
        fd.setTotalExpense(BigDecimal.ZERO);
        fd.setTotalTax(BigDecimal.ZERO);
        fd.setFreeQuantity(BigDecimal.ZERO);
        fd.setFreeQuantityByUnits(BigDecimal.ZERO);

        // NOTE: NOT calling recalculateFinancialsBeforeAddingBillItem() as it overwrites our correctly set purchase rates
        // pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(fd);

        if (ph != null) {
            ph.setQty(qtyByUnits.doubleValue());
            ph.setQtyPacks(qty.doubleValue());
        }

        bi.setQty(qty.doubleValue());
        bi.setRate(grossRate.doubleValue());
        bi.setNetRate(grossRate.doubleValue());
        bi.setNetValue(lineGrossTotal.doubleValue());
    }

    private void updateFinancialsForTransferReceive(BillItemFinanceDetails fd) {
        if (fd == null || fd.getBillItem() == null) {
            return;
        }

        BillItem bi = fd.getBillItem();
        PharmaceuticalBillItem ph = bi.getPharmaceuticalBillItem();
        Item item = bi.getItem();

        BigDecimal qty = Optional.ofNullable(fd.getQuantity()).orElse(BigDecimal.ZERO);

        BigDecimal unitsPerPack = BigDecimal.ONE;
        if (item instanceof Ampp || item instanceof Vmpp) {
            unitsPerPack = item.getDblValue() > 0 ? BigDecimal.valueOf(item.getDblValue()) : BigDecimal.ONE;
        }

        fd.setUnitsPerPack(unitsPerPack);
        fd.setTotalQuantity(qty);
        fd.setQuantity(qty);

        BigDecimal grossRate = Optional.ofNullable(fd.getLineGrossRate()).orElse(determineTransferRate(ph.getItemBatch()).multiply(unitsPerPack));
        fd.setLineGrossRate(grossRate);

        BigDecimal lineGrossTotal = grossRate.multiply(qty);
        fd.setLineGrossTotal(lineGrossTotal);
        fd.setGrossTotal(lineGrossTotal);

        fd.setLineNetRate(grossRate);
        fd.setLineNetTotal(lineGrossTotal);
        fd.setNetTotal(lineGrossTotal);

        BigDecimal qtyByUnits = qty.multiply(unitsPerPack);
        fd.setQuantityByUnits(qtyByUnits);
        fd.setTotalQuantityByUnits(qtyByUnits);

        // Set correct rates for different types from ItemBatch
        if (ph != null && ph.getItemBatch() != null) {
            ItemBatch itemBatch = ph.getItemBatch();
            
            
            // Set purchase rate details
            BigDecimal purchaseRate = BigDecimal.valueOf(itemBatch.getPurcahseRate());
            fd.setValueAtPurchaseRate(purchaseRate.multiply(qtyByUnits));
            
            // Set retail rate details  
            BigDecimal retailRate = BigDecimal.valueOf(itemBatch.getRetailsaleRate());
            fd.setValueAtRetailRate(retailRate.multiply(qtyByUnits));
            
            // Set cost rate details
            BigDecimal costRate = BigDecimal.valueOf(itemBatch.getCostRate());
            fd.setLineCostRate(costRate);
            fd.setLineCost(costRate.multiply(qtyByUnits));
            fd.setValueAtCostRate(costRate.multiply(qtyByUnits));
            fd.setTotalCost(costRate.multiply(qtyByUnits));
            
        } else {
            if (ph != null) {
            }
        }

        fd.setLineDiscount(BigDecimal.ZERO);
        fd.setLineExpense(BigDecimal.ZERO);
        fd.setLineTax(BigDecimal.ZERO);
        fd.setTotalDiscount(BigDecimal.ZERO);
        fd.setTotalExpense(BigDecimal.ZERO);
        fd.setTotalTax(BigDecimal.ZERO);
        fd.setFreeQuantity(BigDecimal.ZERO);
        fd.setFreeQuantityByUnits(BigDecimal.ZERO);

        pharmacyCostingService.recalculateFinancialsBeforeAddingBillItem(fd);

        if (ph != null) {
            ph.setQty(qtyByUnits.doubleValue());
            ph.setQtyPacks(qty.doubleValue());
        }

        bi.setQty(qty.doubleValue());
        bi.setRate(grossRate.doubleValue());
        bi.setNetRate(grossRate.doubleValue());
        bi.setNetValue(lineGrossTotal.doubleValue());
    }

    private double calculateRemainingQty(BillItem issuedItem) {
        double issuedQtyInUnits = 0.0;
        double remainingQtyInUnits = 0.0;
        if (issuedItem != null && issuedItem.getPharmaceuticalBillItem() != null) {
            issuedQtyInUnits = Math.abs(issuedItem.getPharmaceuticalBillItem().getQty());
        }

        double receivedBilledQtyInUnits = Math.abs(pharmacyCalculation.getTotalQty(issuedItem, BillType.PharmacyTransferReceive, new BilledBill()));
        double receivedCancelledQtyInUnits = Math.abs(pharmacyCalculation.getTotalQty(issuedItem, BillType.PharmacyTransferReceive, new CancelledBill()));

        double receivedNet = receivedBilledQtyInUnits - receivedCancelledQtyInUnits;
        remainingQtyInUnits = issuedQtyInUnits - receivedNet;
        return remainingQtyInUnits;
    }

    public void onQuantityChangeForTransferReceive(BillItem bi) {
        if (bi == null) {
            return;
        }
        
        // For quantity changes, use the reference bill item if available
        if (bi.getReferanceBillItem() != null) {
            updateFinancialsForTransferReceiveFromReference(bi.getBillItemFinanceDetails(), bi.getReferanceBillItem());
        } else {
            updateFinancialsForTransferReceive(bi.getBillItemFinanceDetails());
        }
        pharmacyCostingService.calculateBillTotalsFromItemsForTransferOuts(getReceivedBill(), getReceivedBill().getBillItems());
    }

    public Bill getReceivedBill() {
        if (receivedBill == null) {
            receivedBill = new BilledBill();
        }
        return receivedBill;
    }

    public void setReceivedBill(Bill receivedBill) {
        this.receivedBill = receivedBill;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public BillNumberGenerator getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberGenerator billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonFunctions.getStartOfDay(new Date());
        }
        return fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = CommonFunctions.getEndOfDay(new Date());
        }
        return toDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public StockFacade getStockFacade() {
        return stockFacade;
    }

    public void setStockFacade(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    public PharmacyCalculation getPharmacyCalculation() {
        return pharmacyCalculation;
    }

    public void setPharmacyCalculation(PharmacyCalculation pharmacyCalculation) {
        this.pharmacyCalculation = pharmacyCalculation;
    }

    public PharmacyController getPharmacyController() {
        return pharmacyController;
    }

    public void setPharmacyController(PharmacyController pharmacyController) {
        this.pharmacyController = pharmacyController;
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

    public double getDisplayReceivedNetTotal() {
        if (receivedBill == null || receivedBill.getBillItems() == null) {
            return 0.0;
        }
        double tot = 0.0;
        for (BillItem b : receivedBill.getBillItems()) {
            tot += Math.abs(b.getNetValue());
        }
        return tot;
    }

    public List<ConfigOptionInfo> getConfigOptionsForDevelopers() {
        List<ConfigOptionInfo> list = new ArrayList<>();
        list.add(new ConfigOptionInfo("Pharmacy Transfer is by Purchase Rate", "false"));
        list.add(new ConfigOptionInfo("Pharmacy Transfer is by Cost Rate", "false"));
        list.add(new ConfigOptionInfo("Pharmacy Transfer is by Retail Rate", "true"));
        list.add(new ConfigOptionInfo("Report Font Size of Item List in Pharmacy Disbursement Reports", "10pt"));
        list.add(new ConfigOptionInfo("Pharmacy Disbursement Reports - Display Serial Number", "true"));
        list.add(new ConfigOptionInfo("Pharmacy Disbursement Reports - Display Date of Expiary", "true"));
        list.add(new ConfigOptionInfo("Pharmacy Disbursement Reports - Display Code", "true"));
        list.add(new ConfigOptionInfo("Pharmacy Disbursement Reports - Display Purchase Rate", "true"));
        list.add(new ConfigOptionInfo("Pharmacy Disbursement Reports - Display Purchase Value", "false"));
        list.add(new ConfigOptionInfo("Pharmacy Disbursement Reports - Display Retail Sale Rate", "false"));
        list.add(new ConfigOptionInfo("Pharmacy Disbursement Reports - Display Retail Sale Value", "false"));
        list.add(new ConfigOptionInfo("Pharmacy Transfer Receive Bill Footer CSS", ""));
        list.add(new ConfigOptionInfo("Pharmacy Transfer Receive Bill Footer Text", ""));
        return list;
    }

    public String fillHeaderDataOfTransferReceiveNote(String s, Bill b) {
        if (b != null) {
            String filledHeader;

            // Institution details
            String institutionName = b.getCreater().getDepartment().getPrintingName();
            String institutionAddress = b.getCreater().getDepartment().getAddress() != null ?
                    b.getCreater().getDepartment().getAddress() : "";

            // Phone numbers handling
            String phone1 = b.getCreater().getDepartment().getTelephone1() != null ?
                    b.getCreater().getDepartment().getTelephone1() : "";
            String phone2 = b.getCreater().getDepartment().getTelephone2() != null ?
                    b.getCreater().getDepartment().getTelephone2() : "";
            String institutionPhones = phone1;
            if (!phone2.isEmpty()) {
                institutionPhones += " / " + phone2;
            }

            // Fax and Email
            String institutionFax = "";
            if (b.getCreater().getDepartment().getFax() != null &&
                    !b.getCreater().getDepartment().getFax().trim().isEmpty()) {
                institutionFax = "Fax: " + b.getCreater().getDepartment().getFax();
            }

            String institutionEmail = "";
            if (b.getCreater().getDepartment().getEmail() != null &&
                    !b.getCreater().getDepartment().getEmail().trim().isEmpty()) {
                institutionEmail = "Email: " + b.getCreater().getDepartment().getEmail();
            }

            // Cancelled status
            String cancelledStatus = "";
            if (b.getBilledBill() != null && b.getBilledBill().isCancelled() == Boolean.TRUE) {
                cancelledStatus = " <span class=\"receipt-cancelled\">**Cancelled**</span>";
            }

            // Bill details
            String locationFrom = b.getFromDepartment() != null ? b.getFromDepartment().getName() : "";
            String locationTo = b.getDepartment() != null ? b.getDepartment().getName() : "";
            String receivedPerson = b.getCreater().getWebUserPerson().getName();
            String issuedPerson = b.getBackwardReferenceBill() != null &&
                    b.getBackwardReferenceBill().getCreater() != null ?
                    b.getBackwardReferenceBill().getCreater().getWebUserPerson().getName() : "";
            String receiveNo = b.getDeptId() != null ? b.getDeptId() : "";
            String issueNo = b.getBackwardReferenceBill() != null ?
                    b.getBackwardReferenceBill().getDeptId() : "";

            // Date formatting
            String receivedTime =  (b != null ? CommonFunctions.getDateFormat(b.getCreatedAt(), sessionController.getApplicationPreference().getLongDateTimeFormat()) : "");
            String issueTime = (b != null ? CommonFunctions.getDateFormat(b.getBackwardReferenceBill().getCreatedAt(), sessionController.getApplicationPreference().getLongDateTimeFormat()) : "");

            filledHeader = s.replace("{{institution_name}}", institutionName)
                    .replace("{{institution_address}}", institutionAddress)
                    .replace("{{institution_phones}}", institutionPhones)
                    .replace("{{institution_fax}}", institutionFax)
                    .replace("{{institution_email}}", institutionEmail)
                    .replace("{{cancelled_status}}", cancelledStatus)
                    .replace("{{location_from}}", locationFrom)
                    .replace("{{location_to}}", locationTo)
                    .replace("{{received_person}}", receivedPerson)
                    .replace("{{issued_person}}", issuedPerson)
                    .replace("{{receive_no}}", receiveNo)
                    .replace("{{issue_no}}", issueNo)
                    .replace("{{received_time}}", receivedTime)
                    .replace("{{issue_time}}", issueTime);

            return filledHeader;
        } else {
            return s;
        }
    }

    public void displayItemDetails(BillItem bi) {
        getPharmacyController().fillItemDetails(bi.getItem());
    }

    public void prepareBatchDetails(BillItem bi) {
        selectedBillItem = bi;
    }

    public BillItem getSelectedBillItem() {
        return selectedBillItem;
    }

    public void setSelectedBillItem(BillItem selectedBillItem) {
        this.selectedBillItem = selectedBillItem;
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
