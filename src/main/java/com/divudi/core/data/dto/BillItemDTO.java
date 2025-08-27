package com.divudi.core.data.dto;

import com.divudi.core.data.PaymentMethod;
import java.io.Serializable;
import java.util.Date;

public class BillItemDTO implements Serializable {

    private Date billCreatedAt;
    private String itemName;
    private String itemCode;
    private String billDeptId;
    private String batchNo;
    private Double qty;
    private Double costRate;
    private Double purchaseRate;
    private Double retailRate;
    private Double billNetTotal;
    private Long id;
    private Long billId;
    private Double discount;
    private PaymentMethod paymentMethod;

    public BillItemDTO() {
    }

    public BillItemDTO(Date billCreatedAt, String itemName, String itemCode,
            String billDeptId, String batchNo, Double qty,
            Double costRate, Double retailRate, Double billNetTotal) {
        this.billCreatedAt = billCreatedAt;
        this.itemName = itemName;
        this.itemCode = itemCode;
        this.billDeptId = billDeptId;
        this.batchNo = batchNo;
        this.qty = qty;
        this.costRate = costRate;
        this.retailRate = retailRate;
        this.billNetTotal = billNetTotal;
    }

    public BillItemDTO(Long id, Date billCreatedAt, Double discount, Double billNetTotal, PaymentMethod paymentMethod) {
        this.id = id;
        this.billCreatedAt = billCreatedAt;
        this.discount = discount;
        this.billNetTotal = billNetTotal;
        this.paymentMethod = paymentMethod;
    }

    //Cost of Good Sold report-sale report
    public BillItemDTO(Long id, Long billId, Date createdAt, String name, String code,
            String deptId, String batchNo, Double qty, Double costRate, Double purchaseRate,
            Double retailRate, Double netTotal) {
        this.id = id;
        this.billId = billId;
        this.billCreatedAt = createdAt;
        this.itemName = name;
        this.itemCode = code;
        this.billDeptId = deptId;
        this.batchNo = batchNo;
        this.qty = qty;
        this.costRate = costRate;
        this.purchaseRate = purchaseRate;
        this.retailRate = retailRate;
        this.billNetTotal = netTotal;
    }
        
    // Calculated fields (Cost Value and Sale Value are calculated in JSF)
    public Double getCostValue() {
        return (costRate != null && qty != null) ? costRate * qty : 0.0;
    }

    public Double getSaleValue() {
        return (retailRate != null && qty != null) ? retailRate * qty : 0.0;
    }

    // Getters and setters
    public Date getBillCreatedAt() {
        return billCreatedAt;
    }

    public void setBillCreatedAt(Date billCreatedAt) {
        this.billCreatedAt = billCreatedAt;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getBillDeptId() {
        return billDeptId;
    }

    public void setBillDeptId(String billDeptId) {
        this.billDeptId = billDeptId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public Double getCostRate() {
        return costRate;
    }

    public void setCostRate(Double costRate) {
        this.costRate = costRate;
    }

    public Double getRetailRate() {
        return retailRate;
    }

    public void setRetailRate(Double retailRate) {
        this.retailRate = retailRate;
    }

    public Double getBillNetTotal() {
        return billNetTotal;
    }

    public void setBillNetTotal(Double billNetTotal) {
        this.billNetTotal = billNetTotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Double getPurchaseRate() {
        return purchaseRate;
    }

    public void setPurchaseRate(Double purchaseRate) {
        this.purchaseRate = purchaseRate;
    }

}
