/*
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.common.ConfigOptionApplicationController;
import com.divudi.bean.common.ItemController;
import com.divudi.bean.common.EnumController;
import com.divudi.bean.common.NotificationController;
import com.divudi.bean.common.SessionController;

import com.divudi.core.data.BillType;
import com.divudi.core.data.PaymentMethod;
import com.divudi.ejb.BillNumberGenerator;
import com.divudi.ejb.PharmacyBean;
import com.divudi.ejb.PharmacyCalculation;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.BillItemFinanceDetails;
import com.divudi.core.entity.BilledBill;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.entity.pharmacy.Ampp;
import com.divudi.core.entity.pharmacy.Amp;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.BillItemFacade;
import com.divudi.core.facade.ItemFacade;
import com.divudi.core.facade.ItemsDistributorsFacade;
import com.divudi.core.facade.PharmaceuticalBillItemFacade;
import com.divudi.core.facade.EmailFacade;
import com.divudi.core.util.JsfUtil;
import com.divudi.core.util.CommonFunctions;
import com.divudi.ejb.EmailManagerEjb;
import com.divudi.core.entity.AppEmail;
import com.divudi.core.data.MessageType;
import com.divudi.core.data.BillTypeAtomic;
import com.divudi.core.data.dataStructure.PaymentMethodData;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class PurchaseOrderRequestController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(PurchaseOrderRequestController.class.getName());

    @EJB
    private ItemFacade itemFacade;
    @EJB
    private BillNumberGenerator billNumberBean;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private ItemsDistributorsFacade itemsDistributorsFacade;
    @EJB
    private EmailFacade emailFacade;
    @EJB
    private EmailManagerEjb emailManagerEjb;

    @Inject
    private SessionController sessionController;
    @Inject
    ConfigOptionApplicationController configOptionApplicationController;
    @Inject
    EnumController enumController;

    private Bill currentBill;
    private BillItem currentBillItem;
    private List<BillItem> selectedBillItems;
    private List<BillItem> billItems;
    private boolean printPreview;
    private double totalBillItemsCount;
    //private List<PharmaceuticalBillItem> pharmaceuticalBillItems;
    @Inject
    PharmacyCalculation pharmacyBillBean;
    private PaymentMethodData paymentMethodData;

    @Inject
    NotificationController notificationController;

    private String emailRecipient;

    public void removeSelected() {
        if (selectedBillItems == null) {
            return;
        }
        for (BillItem b : selectedBillItems) {
            b.setRetired(true);
            b.setRetirer(sessionController.getLoggedUser());
            b.setRetiredAt(new Date());
            if (getCurrentBill().getId() != null) {
                if (b.getId() != null) {
                    billItemFacade.edit(b);
                }
            }
        }

        getBillItems().removeAll(selectedBillItems);
        calculateBillTotals();
        selectedBillItems = null;
    }

    public PharmacyCalculation getPharmacyBillBean() {
        return pharmacyBillBean;
    }

    public String navigateToCreateNewPurchaseOrder() {
        resetBillValues();
        getCurrentBill();
        return "/pharmacy/pharmacy_purhcase_order_request?faces-redirect=true";
    }

    public String navigateToUpdatePurchaseOrder() {
        if (currentBill == null) {
            JsfUtil.addErrorMessage("No Bill");
            return "";
        }
        Bill tmpBill = currentBill;
        resetBillValues();
        setCurrentBill(tmpBill);
        setBillItems(generateBillItems(currentBill));
//        for(BillItem bi: getBillItems()){
//            System.out.println("bi = " + bi.getPharmaceuticalBillItem());
//            if(bi.getPharmaceuticalBillItem()==null){
//                bi.setPharmaceuticalBillItem(generatePharmaceuticalBillItem(bi));
//            }
//        }
        calculateBillTotals();
        return "/pharmacy/pharmacy_purhcase_order_request?faces-redirect=true";
    }

    public List<BillItem> generateBillItems(Bill bill) {
        String jpql = "select bi "
                + " from BillItem bi "
                + " where bi.retired=:ret "
                + " and bi.bill=:bill";
        Map m = new HashMap();
        m.put("ret", false);
        m.put("bill", bill);
        return billItemFacade.findByJpql(jpql, m);
    }

    public PharmaceuticalBillItem generatePharmaceuticalBillItem(BillItem billItem) {
        String jpql = "select pbi "
                + " from PharmaceuticalBillItem pbi "
                + " where bi.billItem=:bi";
        Map m = new HashMap();
        m.put("bi", billItem);
        return pharmaceuticalBillItemFacade.findFirstByJpql(jpql, m);
    }

    public void setPharmacyBillBean(PharmacyCalculation pharmacyBillBean) {
        this.pharmacyBillBean = pharmacyBillBean;
    }

    public void resetBillValues() {
        currentBill = null;
        currentBillItem = null;
        billItems = null;
        printPreview = false;
    }

    public void addItem() {
        if (getCurrentBillItem().getItem() == null) {
            JsfUtil.addErrorMessage("Please select and item from the list");
            return;
        }

        getCurrentBillItem().setSearialNo(getBillItems().size());
        getCurrentBillItem().getPharmaceuticalBillItem().setPurchaseRate(getPharmacyBean().getLastPurchaseRate(getCurrentBillItem().getItem(), getSessionController().getDepartment()));
        getCurrentBillItem().getPharmaceuticalBillItem().setRetailRate(getPharmacyBean().getLastRetailRate(getCurrentBillItem().getItem(), getSessionController().getDepartment()));

        if (getCurrentBillItem().getItem() instanceof Ampp) {
            BigDecimal unitsPerPack = BigDecimal.valueOf(getCurrentBillItem().getItem().getDblValue());
            if (unitsPerPack == null || unitsPerPack.doubleValue() <= 0) {
                unitsPerPack = BigDecimal.ONE;
            }
            getCurrentBillItem().getBillItemFinanceDetails().setUnitsPerPack(unitsPerPack);
        } else {
            getCurrentBillItem().getBillItemFinanceDetails().setUnitsPerPack(BigDecimal.ONE);
        }

        getCurrentBillItem().getBillItemFinanceDetails().setLineGrossRate(BigDecimal.valueOf(getCurrentBillItem().getPharmaceuticalBillItem().getPurchaseRate()));
        getCurrentBillItem().getBillItemFinanceDetails().setLineNetRate(getCurrentBillItem().getBillItemFinanceDetails().getLineGrossRate());

        getBillItems().add(getCurrentBillItem());

        calculateBillTotals();

        currentBillItem = null;
    }

    public void removeItem(BillItem bi) {
        Bill currentBill = getCurrentBill();
        if (currentBill == null || bi == null) {
            return;
        }

        Date now = new Date();

        bi.setRetired(true);
        bi.setRetirer(sessionController.getLoggedUser());
        bi.setRetiredAt(now);

        PharmaceuticalBillItem tmpPbi = bi.getPharmaceuticalBillItem();
        if (tmpPbi != null) {
            tmpPbi.setRetired(true);
            tmpPbi.setRetirer(sessionController.getLoggedUser());
            tmpPbi.setRetiredAt(now);
        }

        if (bi.getId() != null) {
            billItemFacade.edit(bi);
        }

        if (tmpPbi != null && tmpPbi.getId() != null) {
            pharmaceuticalBillItemFacade.edit(tmpPbi);
        }

        getBillItems().remove(bi);

        calculateBillTotals();

        currentBillItem = null;
    }

    @Inject
    private PharmacyController pharmacyController;

    public void onEdit(BillItem bi) {
        calculateLineValues(bi);
        calculateBillTotals();
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
        if (bdUnitsPerPack == null || bdUnitsPerPack.doubleValue() <= 0) {
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

        // Calculate free quantity by units (for AMPP items)
        BigDecimal freeQuantityByUnits;
        if (lineBillItem.getItem() instanceof Ampp) {
            freeQuantityByUnits = bdFreeQty.multiply(bdUnitsPerPack);
        } else {
            freeQuantityByUnits = bdFreeQty;
        }
        lineBillItem.getBillItemFinanceDetails().setFreeQuantityByUnits(freeQuantityByUnits);

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

        // Update pharmaceuticalBillItem quantities and rates
        double quantity = bdQty.doubleValue();
        double freeQuantity = bdFreeQty.doubleValue();

        if (lineBillItem.getItem() instanceof Ampp) {
            // For AMPP items, billItemFinanceDetails stores packs, convert to units
            double unitsPerPack = bdUnitsPerPack.doubleValue();
            lineBillItem.getPharmaceuticalBillItem().setQty(quantity * unitsPerPack);
            lineBillItem.getPharmaceuticalBillItem().setFreeQty(freeQuantity * unitsPerPack);

            lineBillItem.getPharmaceuticalBillItem().setPurchaseRate(bdPurchaseRate.doubleValue() / unitsPerPack);
            lineBillItem.getPharmaceuticalBillItem().setPurchaseRatePack(bdPurchaseRate.doubleValue());
            lineBillItem.getPharmaceuticalBillItem().setPurchaseValue(bdPurchaseValue.doubleValue());

            lineBillItem.getPharmaceuticalBillItem().setRetailRate(bdRetailRate.doubleValue() / unitsPerPack);
            lineBillItem.getPharmaceuticalBillItem().setRetailRatePack(bdRetailRate.doubleValue());
            lineBillItem.getPharmaceuticalBillItem().setRetailRateInUnit(bdRetailRate.doubleValue() / unitsPerPack);
            lineBillItem.getPharmaceuticalBillItem().setRetailValue(bdRetailValue.doubleValue());

        } else {
            // For AMP items, quantities are already in units
            lineBillItem.getPharmaceuticalBillItem().setQty(quantity);
            lineBillItem.getPharmaceuticalBillItem().setFreeQty(freeQuantity);

            lineBillItem.getPharmaceuticalBillItem().setPurchaseRate(bdPurchaseRate.doubleValue());
            lineBillItem.getPharmaceuticalBillItem().setPurchaseRatePack(bdPurchaseRate.doubleValue());
            lineBillItem.getPharmaceuticalBillItem().setPurchaseValue(bdPurchaseValue.doubleValue());

            lineBillItem.getPharmaceuticalBillItem().setRetailRate(bdRetailRate.doubleValue());
            lineBillItem.getPharmaceuticalBillItem().setRetailRatePack(bdRetailRate.doubleValue());
            lineBillItem.getPharmaceuticalBillItem().setRetailRateInUnit(bdRetailRate.doubleValue());
            lineBillItem.getPharmaceuticalBillItem().setRetailValue(bdRetailValue.doubleValue());
        }
        
        
        lineBillItem.getPharmaceuticalBillItem().setCostRate(0.0); // Not relevant for purchase orders
        lineBillItem.getPharmaceuticalBillItem().setCostRatePack(0.0); // Not relevant for purchase orders  
        lineBillItem.getPharmaceuticalBillItem().setCostValue(0.0); // Not relevant for purchase orders
        
        // Set audit fields for PharmaceuticalBillItem
        if (lineBillItem.getPharmaceuticalBillItem().getId() == null) {
            lineBillItem.getPharmaceuticalBillItem().setCreatedAt(new Date());
            lineBillItem.getPharmaceuticalBillItem().setCreater(getSessionController().getLoggedUser());
        }

        // Update BillItem values - always record as user entered (no pack/unit conversion)
        lineBillItem.setQty(bdQty.doubleValue()); // User entered quantity (packs for AMPP, units for AMP)

        lineBillItem.setRate(bdPurchaseRate.doubleValue()); // User entered rate (rate per pack for AMPP, rate per unit for AMP)
        lineBillItem.setNetRate(bdPurchaseRate.doubleValue()); // Net rate = gross rate (no discount)

        lineBillItem.setNetValue(bdNetValue.doubleValue()); // User entered rate × user entered quantity
        lineBillItem.setGrossValue(bdGrossValue.doubleValue()); // Same as net value (no discount)
        
        // Set audit fields for BillItem
        if (lineBillItem.getId() == null) {
            lineBillItem.setCreatedAt(new Date());
            lineBillItem.setCreater(getSessionController().getLoggedUser());
        }

    }

    public void displayItemDetails(BillItem bi) {
        getPharmacyController().fillItemDetails(bi.getItem());
    }

    public void saveBill() {
        if (getCurrentBill().getDeptId()==null||getCurrentBill().getDeptId().trim().equals("")) {
            String deptId = billNumberBean.departmentBillNumberGeneratorYearly(getSessionController().getDepartment(), BillTypeAtomic.PHARMACY_ORDER_PRE);
            getCurrentBill().setDeptId(deptId);
            getCurrentBill().setInsId(deptId);
        }

        getCurrentBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getCurrentBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());
        getCurrentBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        getCurrentBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getCurrentBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_ORDER_PRE);

        if (getCurrentBill().getId() == null) {
            getCurrentBill().setCreater(getSessionController().getLoggedUser());
            getCurrentBill().setCreatedAt(Calendar.getInstance().getTime());
            getBillFacade().create(getCurrentBill());
        } else {
            getCurrentBill().setEditedAt(Calendar.getInstance().getTime());
            getCurrentBill().setEditor(getSessionController().getLoggedUser());
            getBillFacade().edit(getCurrentBill());
        }
    }

    public void finalizeBill() {
        getCurrentBill().setEditedAt(new Date());
        getCurrentBill().setEditor(sessionController.getLoggedUser());
        getCurrentBill().setCheckeAt(new Date());
        getCurrentBill().setCheckedBy(sessionController.getLoggedUser());
        getCurrentBill().setBillTypeAtomic(BillTypeAtomic.PHARMACY_ORDER);
        getBillFacade().edit(getCurrentBill());
        notificationController.createNotification(getCurrentBill());

    }

    public void generateBillComponentsForAllSupplierItems(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        if (getBillItems() == null) {
            setBillItems(new ArrayList<>());
        }

        int serialStart = getBillItems().size();

        for (Item i : items) {
            BillItem bi = new BillItem();
            bi.setItem(i);

            PharmaceuticalBillItem tmp = new PharmaceuticalBillItem();
            tmp.setBillItem(bi);
            bi.setPharmaceuticalBillItem(tmp);

            bi.setSearialNo(serialStart++);
            tmp.setPurchaseRate(getPharmacyBean().getLastPurchaseRate(i, getSessionController().getDepartment()));
            tmp.setRetailRate(getPharmacyBean().getLastRetailRate(i, getSessionController().getDepartment()));

            getBillItems().add(bi);
        }

        calculateBillTotals();
    }

    public void saveBillComponent() {
        for (BillItem b : getBillItems()) {
            b.setBill(getCurrentBill());
            if (b.getId() == null) {
                getBillItemFacade().create(b);
            } else {
                getBillItemFacade().edit(b);
            }
        }
    }

    public void finalizeBillComponent() {
        getBillItems().removeIf(BillItem::isRetired);
        for (BillItem b : getBillItems()) {
            b.setBill(getCurrentBill());
            BigDecimal qUnits = (b.getBillItemFinanceDetails() != null && b.getBillItemFinanceDetails().getQuantityByUnits() != null)
                    ? b.getBillItemFinanceDetails().getQuantityByUnits() : BigDecimal.ZERO;
            BigDecimal fqUnits = (b.getBillItemFinanceDetails() != null && b.getBillItemFinanceDetails().getFreeQuantityByUnits() != null)
                    ? b.getBillItemFinanceDetails().getFreeQuantityByUnits() : BigDecimal.ZERO;
            BigDecimal totalUnits = qUnits.add(fqUnits);
            if (totalUnits.compareTo(BigDecimal.ZERO) <= 0) {
                b.setRetired(true);
                b.setRetirer(sessionController.getLoggedUser());
                b.setRetiredAt(new Date());
                b.setRetireComments("Retired at Finalising PO");
            }
            totalBillItemsCount += totalUnits.doubleValue();
            if (b.getId() == null) {
                getBillItemFacade().create(b);
            } else {
                getBillItemFacade().edit(b);
            }
        }
    }

    public void addAllSupplierItems() {
        if (getCurrentBill().getToInstitution() == null) {
            JsfUtil.addErrorMessage("Please Select Dealor");
            return;
        }
        List<Item> allItems = getPharmacyBillBean().getItemsForDealor(getCurrentBill().getToInstitution());
        generateBillComponentsForAllSupplierItems(allItems);
    }

    public void addAllSupplierItemsBelowRol() {
        if (getCurrentBill().getToInstitution() == null) {
            JsfUtil.addErrorMessage("Please Select Dealer");
            return;
        }

        String jpql = "SELECT i FROM Item i WHERE i IN "
                + "(SELECT id.item FROM ItemsDistributors id WHERE id.institution = :supplier AND id.retired = false AND id.item.retired = false) "
                + "AND i IN "
                + "(SELECT s.itemBatch.item FROM Stock s JOIN Reorder r ON r.item = s.itemBatch.item AND r.department = s.department "
                + "WHERE s.department = :department AND s.stock < r.rol GROUP BY s.itemBatch.item) "
                + "ORDER BY i.name";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("supplier", getCurrentBill().getToInstitution());
        parameters.put("department", getSessionController().getDepartment());
        List<Item> itemsBelowReorderLevel = getItemFacade().findByJpql(jpql, parameters);

        if (itemsBelowReorderLevel == null || itemsBelowReorderLevel.isEmpty()) {
            JsfUtil.addErrorMessage("No items found below reorder level for the selected supplier and department.");
        } else {
            generateBillComponentsForAllSupplierItems(itemsBelowReorderLevel);
        }
    }

    public void saveRequest() {
        if (getCurrentBill().getPaymentMethod() == null) {
            JsfUtil.addErrorMessage("Please select a payment method");
            return;
        }
        if (getBillItems() == null || getBillItems().isEmpty()) {
            JsfUtil.addErrorMessage("Please add bill items");
            return;
        }
        saveBill();
        saveBillComponent();
        JsfUtil.addSuccessMessage("Request Saved");
    }

    public List<Item> getDealorItems() {
        List<Item> lst;
        String sql;
        HashMap hm = new HashMap();
        sql = "select c.item "
                + " from ItemsDistributors c"
                + " where c.retired=false "
                + " and c.item.retired=false "
                + " and c.institution=:ins "
                + " order by c.item.name";
        hm.put("ins", getCurrentBill().getToInstitution());
        lst = itemFacade.findByJpql(sql, hm, 200);
        return lst;
    }

    public void finalizeRequest() {
        if (currentBill == null) {
            JsfUtil.addErrorMessage("No Bill");
            return;
        }
        if (getCurrentBill().getPaymentMethod() == null) {
            JsfUtil.addErrorMessage("Please select a payment method.");
            return;
        }
        if (getBillItems() == null || getBillItems().isEmpty()) {
            JsfUtil.addErrorMessage("Please add bill items.");
            return;
        }
        if (!allBillItemsValid(billItems)) {
            JsfUtil.addErrorMessage("Please ensure each item has quantity and purchase price.");
            return;
        }
        if (currentBill.getId() == null) {
            saveRequest();
        }
        finalizeBill();
        totalBillItemsCount = 0;
        finalizeBillComponent();
        if (totalBillItemsCount == 0) {
            JsfUtil.addErrorMessage("Please enter item quantities for the bill.");
            return;
        }

        JsfUtil.addSuccessMessage("Request successfully finalized.");
        printPreview = true;
    }

    public void prepareEmailDialog() {
        if (currentBill == null) {
            JsfUtil.addErrorMessage("No Bill");
            return;
        }

        // Set default email if available
        if (currentBill.getToInstitution() != null && currentBill.getToInstitution().getEmail() != null) {
            emailRecipient = currentBill.getToInstitution().getEmail();
        } else {
            emailRecipient = "";
        }
    }

    public void sendPurchaseOrderEmail() {
        if (currentBill == null) {
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
        email.setMessageSubject("Purchase Order Request");
        email.setMessageBody(body);
        email.setDepartment(sessionController.getLoggedUser().getDepartment());
        email.setInstitution(sessionController.getLoggedUser().getInstitution());
        email.setBill(currentBill);
        email.setMessageType(MessageType.Marketing);
        email.setSentSuccessfully(false);
        email.setPending(true);
        emailFacade.create(email);

        try {
            boolean success = emailManagerEjb.sendEmail(
                    java.util.Collections.singletonList(recipient),
                    body,
                    "Purchase Order Request",
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
            if (currentBill == null) {
                LOGGER.log(Level.SEVERE, "Current bill is null when generating purchase order HTML");
                return null;
            }

            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>Purchase Order Request</title></head><body>");
            html.append("<div style='font-family: Arial, sans-serif; padding: 20px;'>");

            // Institution header
            if (currentBill.getCreater() != null && currentBill.getCreater().getInstitution() != null) {
                html.append("<div style='text-align: center; margin-bottom: 20px;'>");
                html.append("<h2>").append(currentBill.getCreater().getInstitution().getName() != null ? currentBill.getCreater().getInstitution().getName() : "").append("</h2>");
                if (currentBill.getCreater().getInstitution().getAddress() != null) {
                    html.append("<p>").append(currentBill.getCreater().getInstitution().getAddress()).append("</p>");
                }
                if (currentBill.getCreater().getInstitution().getPhone() != null) {
                    html.append("<p>Phone: ").append(currentBill.getCreater().getInstitution().getPhone()).append("</p>");
                }
                html.append("</div>");
            }

            html.append("<h3 style='text-align: center; text-decoration: underline;'>Purchase Order Request</h3>");

            // Order details
            html.append("<table style='width: 100%; margin-bottom: 20px;'>");
            html.append("<tr><td><strong>Order No:</strong></td><td>").append(currentBill.getDeptId() != null ? currentBill.getDeptId() : "").append("</td></tr>");
            if (currentBill.getDepartment() != null) {
                html.append("<tr><td><strong>Order Department:</strong></td><td>").append(currentBill.getDepartment().getName() != null ? currentBill.getDepartment().getName() : "").append("</td></tr>");
            }
            if (currentBill.getToInstitution() != null) {
                html.append("<tr><td><strong>Supplier:</strong></td><td>").append(currentBill.getToInstitution().getName() != null ? currentBill.getToInstitution().getName() : "").append("</td></tr>");
                html.append("<tr><td><strong>Supplier Code:</strong></td><td>").append(currentBill.getToInstitution().getCode() != null ? currentBill.getToInstitution().getCode() : "").append("</td></tr>");
                if (currentBill.getToInstitution().getPhone() != null) {
                    html.append("<tr><td><strong>Supplier Phone:</strong></td><td>").append(currentBill.getToInstitution().getPhone()).append("</td></tr>");
                }
                if (currentBill.getToInstitution().getAddress() != null) {
                    html.append("<tr><td><strong>Supplier Address:</strong></td><td>").append(currentBill.getToInstitution().getAddress()).append("</td></tr>");
                }
            }
            html.append("<tr><td><strong>Payment Method:</strong></td><td>").append(currentBill.getPaymentMethod() != null ? currentBill.getPaymentMethod().toString() : "").append("</td></tr>");
            html.append("<tr><td><strong>Consignment:</strong></td><td>").append(currentBill.isConsignment() ? "Yes" : "No").append("</td></tr>");
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
            html.append("<td style='padding: 8px; text-align: right;'>").append(String.format("%,.2f", currentBill.getNetTotal())).append("</td>");
            html.append("</tr></tfoot></table>");

            // Footer details
            html.append("<div style='margin-top: 20px;'>");
            if (currentBill.getCreater() != null && currentBill.getCreater().getWebUserPerson() != null) {
                html.append("<p><strong>Order Initiated By:</strong> ").append(currentBill.getCreater().getWebUserPerson().getName() != null ? currentBill.getCreater().getWebUserPerson().getName() : "").append("</p>");
            }
            if (currentBill.getCheckedBy() != null) {
                html.append("<p><strong>Order Finalized By:</strong> ").append(currentBill.getCheckedBy().getName() != null ? currentBill.getCheckedBy().getName() : "").append("</p>");
            }
            if (currentBill.getCheckeAt() != null) {
                html.append("<p><strong>Order Finalized At:</strong> ").append(CommonFunctions.formatDate(currentBill.getCheckeAt(), "dd/MM/yyyy HH:mm:ss")).append("</p>");
            }
            html.append("<p><strong>Generated At:</strong> ").append(CommonFunctions.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss")).append("</p>");
            html.append("<p><strong>Total:</strong> ").append(String.format("%,.2f", currentBill.getNetTotal())).append("</p>");
            html.append("</div>");

            html.append("</div></body></html>");
            return html.toString();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating purchase order HTML", e);
            return null;
        }
    }

    // Extracted magic number for clarity
    private static final double MIN_PURCHASE_RATE = 0.00001;

    private boolean allBillItemsValid(List<BillItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        for (BillItem bi : items) {
            BillItemFinanceDetails fd = bi.getBillItemFinanceDetails();
            if (fd == null) {
                return false;
            }
            BigDecimal qtyUnits = fd.getQuantityByUnits() != null
                ? fd.getQuantityByUnits() : BigDecimal.ZERO;
            BigDecimal freeUnits = fd.getFreeQuantityByUnits() != null
                ? fd.getFreeQuantityByUnits() : BigDecimal.ZERO;
            BigDecimal totalUnits = qtyUnits.add(freeUnits);
            // Require at least one atomic unit (e.g., tablet)
            if (totalUnits.compareTo(BigDecimal.ONE) < 0) {
                return false;
            }
            BigDecimal rate = fd.getLineGrossRate();
            if (rate == null || rate.compareTo(BigDecimal.valueOf(MIN_PURCHASE_RATE)) < 0) {
                return false;
            }
        }
        return true;
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

                // Calculate purchase values (free vs non-free)
                BigDecimal qty = handlingBillItem.getBillItemFinanceDetails().getQuantity();
                BigDecimal freeQty = handlingBillItem.getBillItemFinanceDetails().getFreeQuantity();
                BigDecimal purchaseRate = handlingBillItem.getBillItemFinanceDetails().getLineGrossRate();

                if (qty != null && purchaseRate != null) {
                    totalPurchaseValueNonFree = totalPurchaseValueNonFree.add(qty.multiply(purchaseRate));
                }
                if (freeQty != null && purchaseRate != null) {
                    totalPurchaseValueFree = totalPurchaseValueFree.add(freeQty.multiply(purchaseRate));
                }

                // Calculate retail values (free vs non-free)
                BigDecimal retailRate = handlingBillItem.getBillItemFinanceDetails().getRetailSaleRate();
                if (qty != null && retailRate != null) {
                    totalRetailSaleValueNonFree = totalRetailSaleValueNonFree.add(qty.multiply(retailRate));
                }
                if (freeQty != null && retailRate != null) {
                    totalRetailSaleValueFree = totalRetailSaleValueFree.add(freeQty.multiply(retailRate));
                }
            }
        }

        // Update bill level totals
        getCurrentBill().setTotal(billGrossTotal.doubleValue());
        getCurrentBill().setNetTotal(billNetTotal.doubleValue());

        // Update BillFinanceDetails
        getCurrentBill().getBillFinanceDetails().setNetTotal(billNetTotal);
        getCurrentBill().getBillFinanceDetails().setGrossTotal(billGrossTotal);

        getCurrentBill().getBillFinanceDetails().setTotalPurchaseValueFree(totalPurchaseValueFree);
        getCurrentBill().getBillFinanceDetails().setTotalPurchaseValueNonFree(totalPurchaseValueNonFree);
        getCurrentBill().getBillFinanceDetails().setTotalPurchaseValue(totalPurchaseValueNonFree.add(totalPurchaseValueFree));

        getCurrentBill().getBillFinanceDetails().setTotalRetailSaleValue(totalRetailSaleValueNonFree.add(totalRetailSaleValueFree));
        getCurrentBill().getBillFinanceDetails().setTotalRetailSaleValueFree(totalRetailSaleValueFree);
        getCurrentBill().getBillFinanceDetails().setTotalRetailSaleValueNonFree(totalRetailSaleValueNonFree);
    }

    public PurchaseOrderRequestController() {
    }

    @Inject
    private ItemController itemController;

    public void setInsListener() {
        getItemController().setInstitution(getCurrentBill().getToInstitution());
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

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public Bill getCurrentBill() {
        if (currentBill == null) {
            currentBill = new BilledBill();
            currentBill.setBillType(BillType.PharmacyOrder);
            currentBill.setBillTypeAtomic(BillTypeAtomic.PHARMACY_ORDER);

            String key = "Pharmacy Purchase Order Default Payment Method";
            String strEnumValue = configOptionApplicationController.getEnumValueByKey(key);
            PaymentMethod pm = enumController.getEnumValue(PaymentMethod.class, strEnumValue);

            currentBill.setPaymentMethod(pm);
            boolean consignmentEnabled = configOptionApplicationController.getBooleanValueByKey("Consignment Option is checked in new Pharmacy Purchasing Bills", false);
            currentBill.setConsignment(consignmentEnabled);
        }
        return currentBill;
    }

    public void setCurrentBill(Bill currentBill) {
        this.currentBill = currentBill;
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

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public ItemsDistributorsFacade getItemsDistributorsFacade() {
        return itemsDistributorsFacade;
    }

    public void setItemsDistributorsFacade(ItemsDistributorsFacade itemsDistributorsFacade) {
        this.itemsDistributorsFacade = itemsDistributorsFacade;
    }

    public PharmacyController getPharmacyController() {
        return pharmacyController;
    }

    public void setPharmacyController(PharmacyController pharmacyController) {
        this.pharmacyController = pharmacyController;
    }

    public List<BillItem> getSelectedBillItems() {
        return selectedBillItems;
    }

    public void setSelectedBillItems(List<BillItem> selectedBillItems) {
        this.selectedBillItems = selectedBillItems;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public void setItemController(ItemController itemController) {
        this.itemController = itemController;
    }

    public BillItem getCurrentBillItem() {
        if (currentBillItem == null) {
            currentBillItem = new BillItem();
            PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
            currentBillItem.setPharmaceuticalBillItem(ph);
            ph.setBillItem(currentBillItem);
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

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public PaymentMethodData getPaymentMethodData() {
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
