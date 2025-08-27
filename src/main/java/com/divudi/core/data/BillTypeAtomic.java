package com.divudi.core.data;

import static com.divudi.core.data.BillClassType.Bill;
import static com.divudi.core.data.BillClassType.BilledBill;
import static com.divudi.core.data.BillClassType.CancelledBill;
import static com.divudi.core.data.BillClassType.OtherBill;
import static com.divudi.core.data.BillClassType.PreBill;
import static com.divudi.core.data.BillClassType.RefundBill;
import static com.divudi.core.data.BillType.PharmacyBhtPre;
import static com.divudi.core.data.BillType.PharmacyGrnBill;
import static com.divudi.core.data.BillType.PharmacyPre;
import static com.divudi.core.data.BillType.PharmacyPurchaseBill;
import static com.divudi.core.data.BillType.PharmacyWholeSale;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum BillTypeAtomic {
    @Deprecated //Use REQUEST_MEDICINE_INWARD
    INWARD_PHARMACY_REQUEST("Inward Request Medicines From Pharmacy", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.CREDIT_SPEND, BillType.InwardPharmacyRequest),
    INWARD_SERVICE_BATCH_BILL("Inward Service Batch Bill", BillCategory.BILL, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.CREDIT_SPEND, BillType.InwardProfessionalEstimates),
    INWARD_SERVICE_BATCH_BILL_CANCELLATION("Inward Service Batch Bill Cancellation", BillCategory.CANCELLATION, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.CREDIT_SPEND, BillType.InwardProfessionalEstimates),
    INWARD_SERVICE_BILL("Inward Service Bill", BillCategory.BILL, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.CREDIT_SPEND, BillType.InwardProfessional),
    INWARD_SERVICE_BILL_CANCELLATION("Inward Service Bill Cancellation", BillCategory.CANCELLATION, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.CREDIT_SPEND, BillType.InwardProfessional),
    INWARD_SERVICE_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION("Inward Service Bill Cancellation with Batch Bill", BillCategory.CANCELLATION, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.CREDIT_SPEND, BillType.InwardProfessional),
    INWARD_OUTSIDE_CHARGES_BILL("Inward Outside Bill", BillCategory.BILL, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.CREDIT_SPEND, BillType.InwardOutSideBill),
    INWARD_OUTSIDE_CHARGES_BILL_CANCELLATION("Inward Outside Bill Cancellation", BillCategory.CANCELLATION, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.CREDIT_SPEND, BillType.InwardOutSideBill),
    INWARD_PROFESSIONAL_FEE_BILL("Inward Professional Fee Bill", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardProfessional),
    INWARD_ESTIMATED_PROFESSIONAL_FEE_BILL("Inward Estimated Professional Fee Bill", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardProfessionalEstimates),
    INWARD_THEATRE_PROFESSIONAL_FEE_BILL("Inward Theatre Professional Fee Bill", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardProfessional),
    @Deprecated //Use INWARD_SERVICE_BILL_REFUND
    INWARD_SERVICE_BATCH_BILL_REFUND("Inward Service Bill Refund", BillCategory.REFUND, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.CREDIT_COLLECTION, BillType.InwardProfessional),
    INWARD_SERVICE_BILL_REFUND("Inward Service Bill Refund", BillCategory.REFUND, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.CREDIT_COLLECTION, BillType.InwardProfessional),
    @Deprecated // Use PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE
    INWARD_SERVICE_PROFESSIONAL_PAYMENT_BILL("Inward Service Professional Payment bill", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.CASH_OUT, CountedServiceType.INWARD, PaymentCategory.NON_CREDIT_SPEND, BillType.InwardProfessional),
    INWARD_DEPOSIT("Inward Deposit", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.CASH_IN, CountedServiceType.INWARD, PaymentCategory.CREDIT_SPEND, BillType.InwardPaymentBill),
    INWARD_DEPOSIT_CANCELLATION("Inward Deposit Cancellation", BillCategory.CANCELLATION, ServiceType.INWARD, BillFinanceType.CASH_OUT, CountedServiceType.INWARD, PaymentCategory.CREDIT_SPEND, BillType.InwardPaymentBill),
    INWARD_DEPOSIT_REFUND("Inward Deposit Refund", BillCategory.REFUND, ServiceType.INWARD, BillFinanceType.CASH_OUT, CountedServiceType.INWARD, PaymentCategory.CREDIT_SPEND, BillType.InwardPaymentBill),
    INWARD_DEPOSIT_REFUND_CANCELLATION("Inward Deposit Refund Cancellation", BillCategory.CANCELLATION, ServiceType.INWARD, BillFinanceType.CASH_IN, CountedServiceType.INWARD, PaymentCategory.CREDIT_SPEND, BillType.InwardPaymentBill),
    INWARD_FINAL_BILL("Inward Final Bill", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardFinalBill),
    INWARD_PROVISIONAL_BILL("Inward Provisional Final Bill", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardProvisionalBill),
    INWARD_PROVISIONAL_BILL_CANCELLATION("Inward Provisional Final Bill Cancellation", BillCategory.CANCELLATION, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardProvisionalBill),
    INWARD_ORIGINAL_FINAL_BILL("Inward Original Final Bill", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardFinalBill),
    INWARD_FINAL_BILL_PAYMENT_BY_CREDIT_COMPANY("Inward Final Bill Payments By Credit Company", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardFinalBill),
    INWARD_INTERIM_BILL("Inward Interim Refund", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardFinalBill),
    INWARD_ESTIMATE_BILL("Inward Estimate Bill", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardFinalBill),
    CANCELLED_INWARD_FINAL_BILL("Cancelled Inward Final Bill", BillCategory.CANCELLATION, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardFinalBill),
    CANCELLED_INWARD_INTERIM_BILL("Cancelled Inward Interim Bill", BillCategory.CANCELLATION, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardFinalBill),
    CANCELLED_INWARD_ESTIMATE_BILL("Cancelled Inward Estimate Bill", BillCategory.CANCELLATION, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardFinalBill),
    DIRECT_ISSUE_INWARD_MEDICINE("Direct Issue of Medicines to Inward Patients", BillCategory.BILL, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.PharmacyBhtPre),
    DIRECT_ISSUE_INWARD_MEDICINE_CANCELLATION("Direct Issue of Medicines to Inward Patients Cancellation", BillCategory.CANCELLATION, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.PharmacyBhtPre),
    DIRECT_ISSUE_INWARD_MEDICINE_RETURN("Direct Issue of Medicines to Inward Patients Return", BillCategory.REFUND, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.PharmacyBhtPre),
    DIRECT_ISSUE_THEATRE_MEDICINE("Direct Issue of Medicines to Theatre", BillCategory.BILL, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreBhtPre),
    DIRECT_ISSUE_THEATRE_MEDICINE_CANCELLATION("Direct Issue of Medicines to Theatre Cancellation", BillCategory.CANCELLATION, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreBhtPre),
    DIRECT_ISSUE_THEATRE_MEDICINE_RETURN("Direct Issue of Medicines to Theatre Return", BillCategory.REFUND, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreBhtPre),
    REQUEST_MEDICINE_INWARD("Request Medicines for Inward Patients", BillCategory.BILL, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardPharmacyRequest),
    REQUEST_MEDICINE_INWARD_CANCELLATION("Cancel Request Medicines for Inward Patients", BillCategory.CANCELLATION, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.InwardPharmacyRequest),
    REQUEST_MEDICINE_THEATRE("Request Medicines for Theatre", BillCategory.BILL, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreTransferRequest),
    REQUEST_MEDICINE_THEATRE_CANCELLATION("Cancel Request Medicines for Theatre", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreTransferRequest),
    ISSUE_MEDICINE_ON_REQUEST_INWARD("Issue Medicines on Request for Inpatients", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.PharmacyIssue),
    ISSUE_MEDICINE_ON_REQUEST_INWARD_CANCELLATION("Cancel Issue of Medicines for Request for Inpatients", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.PharmacyIssue),
    ISSUE_MEDICINE_ON_REQUEST_INWARD_RETURN("Returning Medicines on Request for Inpatients", BillCategory.REFUND, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.PharmacyIssue),
    ISSUE_MEDICINE_ON_REQUEST_THEATRE("Issue Medicines on Request to Theatre", BillCategory.BILL, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreIssue),
    ISSUE_MEDICINE_ON_REQUEST_THEATRE_CANCELLATION("Cancel Issue of Medicines on Request to Theatre", BillCategory.CANCELLATION, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreIssue),
    ACCEPT_ISSUED_MEDICINE_INWARD("Accept Issued Medicines Inward", BillCategory.BILL, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.PharmacyIssue),
    ACCEPT_ISSUED_MEDICINE_THEATRE("Accept Issued Medicines Theatre", BillCategory.BILL, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreTransferReceive),
    RETURN_MEDICINE_INWARD("Return Medicines Inward", BillCategory.REFUND, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.PharmacyIssue),
    ACCEPT_RETURN_MEDICINE_INWARD("Accept Returned Medicines Inward", BillCategory.BILL, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.PharmacyIssue),
    RETURN_MEDICINE_THEATRE("Return Medicines Theatre", BillCategory.REFUND, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreIssue),
    ACCEPT_RETURN_MEDICINE_THEATRE("Accept Returned Medicines Theatre", BillCategory.BILL, ServiceType.INWARD_SERVICE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreTransferReceive),
    STORE_ORDER("Store Order", BillCategory.BILL, ServiceType.STORE, BillFinanceType.CASH_IN, CountedServiceType.STORE, PaymentCategory.NON_CREDIT_SPEND, BillType.StoreOrder),
    STORE_ORDER_PRE("Store Order Pre", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.STORE, PaymentCategory.CREDIT_SPEND, BillType.StorePre),
    STORE_ORDER_CANCELLED("Store Order Cancelled", BillCategory.CANCELLATION, ServiceType.STORE, BillFinanceType.CASH_OUT, CountedServiceType.STORE, PaymentCategory.NON_CREDIT_SPEND, BillType.StoreOrder),
    STORE_ORDER_APPROVAL("Store Order Approval", BillCategory.BILL, ServiceType.STORE, BillFinanceType.CASH_IN, CountedServiceType.STORE, PaymentCategory.NON_CREDIT_SPEND, BillType.StoreOrderApprove),
    STORE_ORDER_APPROVAL_CANCELLED("Store Order Approval Cancelled", BillCategory.CANCELLATION, ServiceType.STORE, BillFinanceType.CASH_OUT, CountedServiceType.STORE, PaymentCategory.NON_CREDIT_SPEND, BillType.StoreOrderApprove),
    STORE_DIRECT_PURCHASE("Store Direct Purchase", BillCategory.BILL, ServiceType.STORE, BillFinanceType.CASH_IN, CountedServiceType.STORE, PaymentCategory.NON_CREDIT_SPEND, BillType.StorePurchase),
    STORE_DIRECT_PURCHASE_CANCELLED("Store Direct Purchase Cancelled", BillCategory.CANCELLATION, ServiceType.STORE, BillFinanceType.CASH_OUT, CountedServiceType.STORE, PaymentCategory.NON_CREDIT_SPEND, BillType.StorePurchase),
    STORE_DIRECT_PURCHASE_REFUND("Store Direct Purchase Refund", BillCategory.REFUND, ServiceType.STORE, BillFinanceType.CASH_OUT, CountedServiceType.STORE, PaymentCategory.NON_CREDIT_SPEND, BillType.StorePurchaseReturn),
    STORE_GRN("Store GRN", BillCategory.PAYMENTS, ServiceType.STORE, BillFinanceType.CASH_OUT, CountedServiceType.STORE, PaymentCategory.CREDIT_SPEND, BillType.StoreGrnBill),
    STORE_GRN_CANCELLED("Store GRN Cancelled", BillCategory.CANCELLATION, ServiceType.STORE, BillFinanceType.CASH_OUT, CountedServiceType.STORE, PaymentCategory.CREDIT_SPEND, BillType.StoreGrnBill),
    STORE_GRN_REFUND("Store GRN Refund", BillCategory.REFUND, ServiceType.STORE, BillFinanceType.CASH_OUT, CountedServiceType.STORE, PaymentCategory.CREDIT_SPEND, BillType.StoreGrnReturn),
    STORE_GRN_RETURN("Store GRN Return", BillCategory.REFUND, ServiceType.STORE, BillFinanceType.CASH_IN, CountedServiceType.STORE, PaymentCategory.NON_CREDIT_SPEND, BillType.StoreGrnReturn),
    STORE_DEPAERTMENT_STOCK_ADJUSTMENT("Store Depaertment Stock Adjustment", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.STORE, PaymentCategory.CREDIT_SPEND, BillType.StoreAdjustment),
    STORE_STAFF_STOCK_ADJUSTMENT("Store Staff Stock Adjustment", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.STORE, PaymentCategory.CREDIT_SPEND, BillType.StoreAdjustment),
    STORE_PURCHASE_RATE_ADJUSTMENT("Store Purchase Rate Adjustment", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.STORE, PaymentCategory.CREDIT_SPEND, BillType.StoreAdjustment),
    STORE_SALE_RATE_ADJUSTMENT("Store Sale Rate Adjustment", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.STORE, PaymentCategory.CREDIT_SPEND, BillType.StoreAdjustment),
    STORE_EXPIRY_DATE_ADJUSTMENT("Store Expiry Date Adjustment", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.STORE, PaymentCategory.CREDIT_SPEND, BillType.StoreAdjustment),
    DIRECT_ISSUE_STORE_INWARD("Direct Issue of Store Items to Inward Patients", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreBhtPre),
    DIRECT_ISSUE_STORE_INWARD_CANCELLATION("Direct Issue of Store Items to Inward Patients Cancellation", BillCategory.CANCELLATION, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreBhtPre),
    DIRECT_ISSUE_STORE_INWARD_RETURN("Direct Issue of Store Items to Inward Patients Return", BillCategory.REFUND, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreBhtPre),
    DIRECT_ISSUE_STORE_THEATRE("Direct Issue of Store Items to Theatre", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreBhtPre),
    DIRECT_ISSUE_STORE_THEATRE_CANCELLATION("Direct Issue of Store Items to Theatre Cancellation", BillCategory.CANCELLATION, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreBhtPre),
    DIRECT_ISSUE_STORE_THEATRE_RETURN("Direct Issue of Store Items to Theatre Return", BillCategory.REFUND, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreBhtPre),
    STORE_REQUEST_INWARD("Store Request for Inward", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreTransferRequest),
    STORE_REQUEST_INWARD_CANCELLATION("Cancel Store Request for Inward", BillCategory.CANCELLATION, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreTransferRequest),
    STORE_REQUEST_THEATRE("Store Request for Theatre", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreTransferRequest),
    STORE_REQUEST_THEATRE_CANCELLATION("Cancel Store Request for Theatre", BillCategory.CANCELLATION, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreTransferRequest),
    STORE_ISSUE_ON_REQUEST_INWARD("Issue Store Items on Request to Inward", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreIssue),
    STORE_ISSUE_ON_REQUEST_INWARD_CANCELLATION("Cancel Issue of Store Items on Request to Inward", BillCategory.CANCELLATION, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreIssue),
    STORE_ISSUE_ON_REQUEST_THEATRE("Issue Store Items on Request to Theatre", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreIssue),
    STORE_ISSUE_ON_REQUEST_THEATRE_CANCELLATION("Cancel Issue of Store Items on Request to Theatre", BillCategory.CANCELLATION, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreIssue),
    ACCEPT_ISSUED_STORE_INWARD("Accept Issued Store Items Inward", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreTransferReceive),
    ACCEPT_ISSUED_STORE_THEATRE("Accept Issued Store Items Theatre", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreTransferReceive),
    RETURN_STORE_INWARD("Return Store Items Inward", BillCategory.REFUND, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreIssue),
    ACCEPT_RETURN_STORE_INWARD("Accept Returned Store Items Inward", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreTransferReceive),
    RETURN_STORE_THEATRE("Return Store Items Theatre", BillCategory.REFUND, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreIssue),
    ACCEPT_RETURN_STORE_THEATRE("Accept Returned Store Items Theatre", BillCategory.BILL, ServiceType.STORE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NO_PAYMENT, BillType.StoreTransferReceive),
    // PHARMACY
    PHARMACY_RETAIL_SALE("Pharmacy Retail Sale", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PharmacySale),
    PHARMACY_RETAIL_SALE_WITHOUT_STOCKS("Pharmacy Retail Sale without Stocks", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PharmacySaleWithoutStock),
    PHARMACY_RETAIL_SALE_PRE("Pharmacy Retail Sale Pre", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyPre),
    PHARMACY_RETAIL_SALE_PRE_WITHOUT_STOCKS("Pharmacy Retail Sale Prebill Without Stocks", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacySaleWithoutStock),
    PHARMACY_RETAIL_SALE_PREBILL_SETTLED_AT_CASHIER("Pharmacy Retail Sale Pre Bill Settled At Cashier", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PharmacySale),
    PHARMACY_RETAIL_SALE_PRE_TO_SETTLE_AT_CASHIER("Pharmacy Retail Sale Pre Bill To Settle At Cashier", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyPre),
    PHARMACY_RETAIL_SALE_CANCELLED("Pharmacy Retail Sale Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PharmacySale),
    PHARMACY_RETAIL_SALE_CANCELLED_PRE("Pharmacy Retail Sale Cancelled - Pre Bill", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyPre),
    PHARMACY_RETAIL_SALE_REFUND("Pharmacy Retail Sale Refund", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PharmacySale),
    PHARMACY_RETAIL_SALE_RETURN_ITEMS_ONLY("Pharmacy Retail Sale Return Items Only", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacySale),
    PHARMACY_RETAIL_SALE_RETURN_ITEM_PAYMENTS("Pharmacy Retail Sale Return Item Payments", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PharmacySale),
    PHARMACY_RETAIL_SALE_RETURN_ITEMS_AND_PAYMENTS("Pharmacy Retail Sale Return Items And Payments", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PharmacySale),
    PHARMACY_RETAIL_SALE_RETURN_ITEMS_AND_PAYMENTS_PREBILL("Pharmacy Retail Sale Return Items And Payments - Prebill", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyPre),
    PHARMACY_SALE_WITHOUT_STOCK("Pharmacy Sale Without Stock", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PharmacySaleWithoutStock),
    PHARMACY_SALE_WITHOUT_STOCK_PRE("Pharmacy Sale Without Stock Pre", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacySaleWithoutStock),
    PHARMACY_SALE_WITHOUT_STOCK_CANCELLED("Pharmacy Sale Without Stock Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PharmacySaleWithoutStock),
    PHARMACY_SALE_WITHOUT_STOCK_REFUND("Pharmacy Sale Without Stock Refund", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacySaleWithoutStock),
    PHARMACY_RETAIL_SALE_PRE_ADD_TO_STOCK("Pharmacy Retail Sale Pre Bill Add to Stock", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyPre),
    PHARMACY_WHOLESALE("Pharmacy Wholesale", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PharmacySale),
    PHARMACY_WHOLESALE_PRE("Pharmacy Wholesale Pre", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacySale),
    PHARMACY_WHOLESALE_CANCELLED("Pharmacy Wholesale Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PharmacySale),
    PHARMACY_WHOLESALE_REFUND("Pharmacy Wholesale Refund", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PharmacySale),
    PHARMACY_ORDER("Pharmacy Order", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyOrder),
    PHARMACY_ORDER_PRE("Pharmacy Order Pre", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyOrder),
    PHARMACY_ORDER_CANCELLED("Pharmacy Order Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyOrder),
    PHARMACY_ORDER_APPROVAL("Pharmacy Order Approval", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyOrder),
    PHARMACY_ORDER_APPROVAL_CANCELLED("Pharmacy Order Approval Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyOrder),
    PHARMACY_DIRECT_PURCHASE("Pharmacy Direct Purchase", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyPurchaseBill),
    PHARMACY_DIRECT_PURCHASE_CANCELLED("Pharmacy Direct Purchase Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyPurchaseBill),
    PHARMACY_DIRECT_PURCHASE_REFUND("Pharmacy Direct Purchase Refund", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyPurchaseBill),
    PHARMACY_GRN("Pharmacy GRN", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyGrnBill),
    PHARMACY_GRN_PRE("Pharmacy GRN Pre", BillCategory.PREBILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyGrnBill),
    @Deprecated // Use PHARMACY_WHOLESALE_GRN_BILL. Can not Remove this as it is already in the databases.
    PHARMACY_GRN_WHOLESALE("Pharmacy Wholesale GRN", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyWholeSale),
    PHARMACY_GRN_CANCELLED("Pharmacy GRN Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyGrnBill),
    @Deprecated // Use PHARMACY_GRN_RETURN, Can not remove this as they are already in databases
    PHARMACY_GRN_REFUND("Pharmacy GRN Refund", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyGrnBill),
    PHARMACY_GRN_RETURN("Pharmacy GRN Return", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyGrnBill),
    PHARMACY_GRN_RETURN_CANCELLATION("Pharmacy GRN Return Cancellation", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyGrnReturn),
    PHARMACY_WHOLESALE_DIRECT_PURCHASE_BILL("Pharmacy Direct Purchase", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyPurchaseBill),
    PHARMACY_WHOLESALE_DIRECT_PURCHASE_BILL_CANCELLED("Pharmacy Direct Purchase - Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyPurchaseBill),
    PHARMACY_WHOLESALE_DIRECT_PURCHASE_BILL_REFUND("Pharmacy Direct Purchase - Refund", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyPurchaseBill),
    PHARMACY_DONATION_BILL("Pharmacy Donation Bill", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyDonationBill),
    PHARMACY_DONATION_BILL_CANCELLED("Pharmacy Donation Bill - Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyDonationBill),
    PHARMACY_DONATION_BILL_REFUND("Pharmacy Donation Bill - Refund", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyDonationBill),
    PHARMACY_WHOLESALE_GRN_BILL("Pharmacy GRN", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyGrnBill),
    PHARMACY_WHOLESALE_GRN_BILL_CANCELLED("Pharmacy GRN - Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyGrnBill),
    PHARMACY_WHOLESALE_GRN_BILL_REFUND("Pharmacy GRN - Refund", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacyGrnBill),
    PHARMACY_GRN_PAYMENT("GRN Payment", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.GrnPayment),
    PHARMACY_GRN_PAYMENT_CANCELLED("GRN Payment Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.GrnPayment),
    PHARMACY_ADJUSTMENT("Pharmacy Adjustment", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyAdjustment),
    PHARMACY_ADJUSTMENT_CANCELLED("Pharmacy Adjustment Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyAdjustment),
    PHARMACY_PURCHASE_RATE_ADJUSTMENT("Pharmacy Purchase Rate Adjustment", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyAdjustment),
    PHARMACY_RETAIL_RATE_ADJUSTMENT("Pharmacy Retail Rate Adjustment", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyAdjustment),
    PHARMACY_COST_RATE_ADJUSTMENT("Pharmacy Cost Rate Adjustment", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyAdjustment),
    PHARMACY_WHOLESALE_RATE_ADJUSTMENT("Pharmacy Wholesale Rate Adjustment", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyAdjustment),
    PHARMACY_STOCK_ADJUSTMENT("Pharmacy Stock Adjustment", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyAdjustment),
    PHARMACY_TRANSFER_REQUEST("Pharmacy Transfer Request", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyTransferRequest),
    PHARMACY_TRANSFER_REQUEST_PRE("Pharmacy Transfer Request Pre", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyTransferRequest),
    PHARMACY_TRANSFER_REQUEST_CANCELLED("Pharmacy Transfer Request Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyTransferRequest),
    PHARMACY_ISSUE("Pharmacy Transfer Issue", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyIssue),
    PHARMACY_ISSUE_CANCELLED("Pharmacy Transfer Issue Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyIssue),
    PHARMACY_ISSUE_RETURN("Pharmacy Transfer Issue returned", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyIssue),
    PHARMACY_DIRECT_ISSUE("Pharmacy Direct Issue", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyIssue),
    PHARMACY_DIRECT_ISSUE_CANCELLED("Pharmacy Direct Issue Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyIssue),
    PHARMACY_DISPOSAL_ISSUE("Pharmacy Disposal Issue", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyDisposalIssue),
    PHARMACY_DISPOSAL_ISSUE_CANCELLED("Pharmacy Disposal Issue Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyDisposalIssue),
    PHARMACY_DISPOSAL_ISSUE_RETURN("Pharmacy Disposal Issue returned", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyDisposalIssue),
    PHARMACY_RECEIVE("Pharmacy Receive", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyTransferReceive),
    PHARMACY_RECEIVE_PRE("Pharmacy Receive Request", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyTransferReceive),
    PHARMACY_RECEIVE_CANCELLED("Pharmacy Receive Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyTransferReceive),
    MULTIPLE_PHARMACY_ORDER_CANCELLED_BILL("Multiple Pharmacy Purchase Order Cancelled", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyOrder),
    PHARMACY_RETURN_ITEMS_AND_PAYMENTS_CANCELLATION("Pharmacy Return Items And Payments Cancellation", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_SPEND, BillType.PharmacySale),
    PHARMACY_STOCK_EXPIRY_DATE_AJUSTMENT("Pharmacy Medicine Expiry Date Ajustment", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyAdjustment),
    PHARMACY_SNAPSHOT_GENERATION("Pharmacy Snapshot Generation", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacySnapshotBill),
    PHARMACY_PHYSICAL_COUNT_ENTRY("Pharmacy Physical Count Entry", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacyPhysicalCountBill),
    PHARMACY_STOCK_ADJUSTMENT_BILL("Pharmacy Stock Adjustment Bill", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.CREDIT_SPEND, BillType.PharmacyStockAdjustmentBill),
    PHARMACY_RETURN_WITHOUT_TREASING("Pharmacy Return without a Receipt", BillCategory.REFUND, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NO_PAYMENT, BillType.PharmacySale),
    CHANNEL_BOOKING_WITH_PAYMENT("Channel Booking and Payment", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.CASH_IN, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelCash),
    CHANNEL_BOOKING_FOR_PAYMENT_ONLINE_PENDING_PAYMENT("Channel Booking For Online Payment - Pending Confirmation", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.CHANNELLING, PaymentCategory.NO_PAYMENT, BillType.ChannelOnCall),
    CHANNEL_BOOKING_FOR_PAYMENT_ONLINE_COMPLETED_PAYMENT("Channel Booking For Online Payment - Completed", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.ONLINE_PAYMENT_IN, CountedServiceType.CHANNELLING, PaymentCategory.CREDIT_COLLECTION, BillType.Channel),
    CHANNEL_RESHEDULE_WITH_PAYMENT("Channel Reschedule for paid Appointment", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.CHANNELLING, PaymentCategory.CREDIT_SPEND, BillType.ChannelResheduleWithPayment),
    CHANNEL_RESHEDULE_WITH_OUT_PAYMENT("Channel Reschedule For Non Paid Appointment", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.CHANNELLING, PaymentCategory.CREDIT_SPEND, BillType.ChannelResheduleWithOutPayment),
    @Deprecated
    CHANNEL_BOOKING_PAYMENT("Channel Booking For Online Payment - Pending Confirmation", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelCash),
    CHANNEL_BOOKING_WITH_PAYMENT_ONLINE("Channel Booking Online Payment", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.ONLINE_PAYMENT_IN, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_COLLECTION, BillType.ChannelCash),
    CHANNEL_BOOKING_WITH_PAYMENT_PENDING_ONLINE("Channel Booking Online Payment Pending", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.CHANNELLING, PaymentCategory.NO_PAYMENT, BillType.ChannelOnCall),
    CHANNEL_BOOKING_WITHOUT_PAYMENT("Channel Booking without Payment", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_COLLECTION, BillType.ChannelStaff),
    CHANNEL_PAYMENT_FOR_BOOKING_BILL("Channel Payment for Booking", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.CASH_IN, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelPaid),
    CHANNEL_CANCELLATION_WITH_PAYMENT("Channel Cancellation with Payment", BillCategory.CANCELLATION, ServiceType.CHANNELLING, BillFinanceType.CASH_OUT, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelPaid),
    CHANNEL_CANCELLATION_WITH_PAYMENT_FOR_CREDIT_SETTLED_BOOKINGS("Channel Cancellation with Payment for Bills Where Credit Payment was Settled", BillCategory.CANCELLATION, ServiceType.CHANNELLING, BillFinanceType.CASH_OUT, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelPaid),
    CHANNEL_REFUND_WITH_PAYMENT("Channel Refund with Payment", BillCategory.REFUND, ServiceType.CHANNELLING, BillFinanceType.CASH_OUT, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelPaid),
    CHANNEL_REFUND_WITH_PAYMENT_FOR_CREDIT_SETTLED_BOOKINGS("Channel Refund with Payment for Bills where Credit Payment was Settled", BillCategory.REFUND, ServiceType.CHANNELLING, BillFinanceType.CASH_OUT, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelPaid),
    CHANNEL_CANCELLATION_WITHOUT_PAYMENT("Channel Cancellation without Payment", BillCategory.CANCELLATION, ServiceType.CHANNELLING, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.CHANNELLING, PaymentCategory.CREDIT_SPEND, BillType.ChannelStaff),
    CHANNEL_CANCELLATION_WITH_PAYMENT_ONLINE_BOOKING("Channel Cancellation with online booking", BillCategory.CANCELLATION, ServiceType.CHANNELLING, BillFinanceType.CASH_OUT, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelCash),
    CHANNEL_REFUND("Channel Refund", BillCategory.REFUND, ServiceType.CHANNELLING, BillFinanceType.CASH_OUT, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelPaid),
    CHANNEL_AGENT_PAID_TO_HOSPITAL_FOR_ONLINE_BOOKINGS_BILL("OB Agent Paid To Hospital", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.CASH_IN, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelOnlineBookingAgentPaidToHospital),
    CHANNEL_AGENT_PAID_TO_HOSPITAL_FOR_ONLINE_BOOKINGS_BILL_CANCELLATION("OB Agent Paid To Hospital bill cancel", BillCategory.CANCELLATION, ServiceType.CHANNELLING, BillFinanceType.CASH_OUT, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelOnlineBookingAgentPaidToHospitalBillCancellation),
    CHANNEL_AGENT_PAID_TO_HOSPITAL_DIRECT_FUND_FOR_ONLINE_BOOKINGS_BILL("OB Agent Paid To Hospital direct fund", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.CASH_IN, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelOnlineBookingAgentPaidToHospital),
    CHANNEL_AGENT_PAID_TO_HOSPITAL_DIRECT_FUND_FOR_ONLINE_BOOKINGS_BILL_CANCELLATION("OB Agent Paid To Hospital direct fund bill cancel", BillCategory.CANCELLATION, ServiceType.CHANNELLING, BillFinanceType.CASH_OUT, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelOnlineBookingAgentPaidToHospitalBillCancellation),

    // OPD_IN
    OPD_BATCH_BILL_TO_COLLECT_PAYMENT_AT_CASHIER("Opd Batch Bill to Collect Payment at Cashier", BillCategory.BILL, ServiceType.OPD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.NONE, PaymentCategory.NO_PAYMENT, BillType.OpdBathcBillPre),
    OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER("Opd Batch Bill Payment Collection at Cashier", BillCategory.BILL, ServiceType.OPD, BillFinanceType.CASH_IN, CountedServiceType.NONE, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBathcBill),
    OPD_BATCH_BILL_WITH_PAYMENT("Opd Batch Bill with Payment Collection", BillCategory.BILL, ServiceType.OPD, BillFinanceType.CASH_IN, CountedServiceType.NONE, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBathcBill),
    OPD_BATCH_BILL_CANCELLATION("Opd Batch Bill Cancellation", BillCategory.CANCELLATION, ServiceType.OPD, BillFinanceType.CASH_OUT, CountedServiceType.OPD_OUT, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBathcBill),
    OPD_BILL_TO_COLLECT_PAYMENT_AT_CASHIER("Opd Bill to Collect Payment at Cashier", BillCategory.BILL, ServiceType.OPD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.NONE, PaymentCategory.CREDIT_SPEND, BillType.OpdPreBill),
    OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER("OPD Bill Payment Collection at Cashier", BillCategory.BILL, ServiceType.OPD, BillFinanceType.CASH_IN, CountedServiceType.OPD_IN, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBill),
    OPD_BILL_WITH_PAYMENT("OPD Bill with Payment", BillCategory.BILL, ServiceType.OPD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.OPD_IN, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBill),
    OPD_BILL_CANCELLATION("Opd Bill Cancellation", BillCategory.CANCELLATION, ServiceType.OPD, BillFinanceType.CASH_OUT, CountedServiceType.OPD_OUT, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBill),
    OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION("Opd Bill Cancellation with Batch Bill", BillCategory.CANCELLATION, ServiceType.OPD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.OPD_OUT, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBill),
    OPD_BILL_REFUND("Opd Bill Refund", BillCategory.REFUND, ServiceType.OPD, BillFinanceType.CASH_OUT, CountedServiceType.OPD_OUT, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBill),
    @Deprecated
    OPD_PROFESSIONAL_PAYMENT_BILL("OPD Professional Payment bill", BillCategory.PAYMENTS, ServiceType.PROFESSIONAL_PAYMENT, BillFinanceType.CASH_OUT, CountedServiceType.OPD_PROFESSIONAL_PAYMENT, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdProfessionalFeePayment),
    @Deprecated
    OPD_PROFESSIONAL_PAYMENT_BILL_RETURN("OPD Professional Payment bill Return", BillCategory.PAYMENTS, ServiceType.PROFESSIONAL_PAYMENT, BillFinanceType.CASH_IN, CountedServiceType.OPD_PROFESSIONAL_PAYMENT_RETURN, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdProfessionalFeePayment),
    PACKAGE_OPD_BATCH_BILL_TO_COLLECT_PAYMENT_AT_CASHIER("Package Opd Batch Bill to Collect Payment at Cashier", BillCategory.BILL, ServiceType.OPD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.OPD_IN, PaymentCategory.CREDIT_SPEND, BillType.OpdBathcBillPre),
    PACKAGE_OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER("Package Opd Batch Bill Payment Collection at Cashier", BillCategory.BILL, ServiceType.OPD, BillFinanceType.CASH_IN, CountedServiceType.OPD_IN, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBathcBill),
    PACKAGE_OPD_BATCH_BILL_WITH_PAYMENT("Package Opd Batch Bill with Payment Collection", BillCategory.BILL, ServiceType.OPD, BillFinanceType.CASH_IN, CountedServiceType.OPD_IN, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBathcBill),
    PACKAGE_OPD_BATCH_BILL_CANCELLATION("Package Opd Batch Bill Cancellation", BillCategory.CANCELLATION, ServiceType.OPD, BillFinanceType.CASH_OUT, CountedServiceType.OPD_IN, PaymentCategory.CREDIT_SPEND, BillType.OpdBathcBill),
    PACKAGE_OPD_BILL_TO_COLLECT_PAYMENT_AT_CASHIER("Package Opd Bill to Collect Payment at Cashier", BillCategory.BILL, ServiceType.OPD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.OPD_IN, PaymentCategory.CREDIT_SPEND, BillType.OpdPreBill),
    PACKAGE_OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER("Package OPD Bill Payment Collection at Cashier", BillCategory.BILL, ServiceType.OPD, BillFinanceType.CASH_IN, CountedServiceType.OPD_IN, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBill),
    PACKAGE_OPD_BILL_WITH_PAYMENT("Package OPD Bill with Payment", BillCategory.BILL, ServiceType.OPD, BillFinanceType.CASH_IN, CountedServiceType.OPD_IN, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBill),
    PACKAGE_OPD_BILL_CANCELLATION("Package Opd Bill Cancellation", BillCategory.CANCELLATION, ServiceType.OPD, BillFinanceType.CASH_OUT, CountedServiceType.OPD_IN, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBill),
    PACKAGE_OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION("Package Opd Bill Cancellation with Batch Bill", BillCategory.CANCELLATION, ServiceType.OPD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.OPD_IN, PaymentCategory.NO_PAYMENT, BillType.OpdBill),
    PACKAGE_OPD_BILL_REFUND("Package Opd Bill Refund", BillCategory.REFUND, ServiceType.OPD, BillFinanceType.CASH_OUT, CountedServiceType.OPD_IN, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdBill),
    // COLLECTING CENTRE
    CC_BILL("Collecting Centre Bill", BillCategory.BILL, ServiceType.COLLECTING_CENTRE, BillFinanceType.CASH_IN, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.NON_CREDIT_SPEND, BillType.CollectingCentreBill),
    CC_BILL_CANCELLATION("Collecting Centre Bill Cancellation", BillCategory.CANCELLATION, ServiceType.COLLECTING_CENTRE, BillFinanceType.CASH_OUT, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.NON_CREDIT_SPEND, BillType.CollectingCentreBill),
    CC_BILL_REFUND("Collecting Centre Bill Refund", BillCategory.REFUND, ServiceType.COLLECTING_CENTRE, BillFinanceType.CASH_OUT, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.NON_CREDIT_SPEND, BillType.CollectingCentreBill),
    CC_PAYMENT_RECEIVED_BILL("Collecting Centre Payment Received Bill", BillCategory.BILL, ServiceType.COLLECTING_CENTRE, BillFinanceType.CASH_IN, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.NON_CREDIT_SPEND, BillType.CollectingCentrePaymentReceiveBill),
    CC_PAYMENT_CANCELLATION_BILL("Collecting Centre Payment Cancellation Bill", BillCategory.CANCELLATION, ServiceType.COLLECTING_CENTRE, BillFinanceType.CASH_OUT, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.NON_CREDIT_SPEND, BillType.CollectingCentrePaymentReceiveBill),
    CC_CREDIT_NOTE("Collecting Centre Credit Note", BillCategory.BILL, ServiceType.COLLECTING_CENTRE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.CREDIT_SPEND, BillType.CollectingCentreCreditNoteBill),
    CC_DEBIT_NOTE("Collecting Centre Debit Note", BillCategory.BILL, ServiceType.COLLECTING_CENTRE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.CREDIT_SPEND, BillType.CollectingCentreDebitNoteBill),
    CC_CREDIT_NOTE_CANCELLATION("Collecting Centre Credit Note Cancellation", BillCategory.CANCELLATION, ServiceType.COLLECTING_CENTRE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.CREDIT_SPEND, BillType.CollectingCentreCreditNoteBill),
    CC_DEBIT_NOTE_CANCELLATION("Collecting Centre Debit Note Cancellation", BillCategory.CANCELLATION, ServiceType.COLLECTING_CENTRE, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.CREDIT_SPEND, BillType.CollectingCentreDebitNoteBill),
    CC_AGENT_PAYMENT("Collecting Centre Agent Payment", BillCategory.BILL, ServiceType.COLLECTING_CENTRE, BillFinanceType.CASH_OUT, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.NON_CREDIT_SPEND, BillType.CollectingCentreAgentPayment),
    CC_AGENT_PAYMENT_CANCELLATION("Collecting Centre Agent Payment Cancellation", BillCategory.CANCELLATION, ServiceType.COLLECTING_CENTRE, BillFinanceType.CASH_IN, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.NON_CREDIT_SPEND, BillType.CollectingCentreAgentPayment),
    @Deprecated
    CC_PAYMENT_MADE_BILL("Collecting Centre Payment Made Bill", BillCategory.BILL, ServiceType.COLLECTING_CENTRE, BillFinanceType.CASH_OUT, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.NON_CREDIT_SPEND, BillType.CollectingCentrePaymentMadeBill),
    @Deprecated
    CC_PAYMENT_MADE_CANCELLATION_BILL("Collecting Centre Payment Made Cancellation Bill", BillCategory.CANCELLATION, ServiceType.COLLECTING_CENTRE, BillFinanceType.CASH_OUT, CountedServiceType.COLLECTING_CENTRE, PaymentCategory.NON_CREDIT_SPEND, BillType.CollectingCentrePaymentMadeBill),
    FUND_SHIFT_START_BILL("Shift Start Fund Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.FLOAT_STARTING_BALANCE, CountedServiceType.OTHER, PaymentCategory.CREDIT_SPEND, BillType.ShiftStartFundBill),
    FUND_SHIFT_START_BILL_CANCELLED("Shift Start Fund Bill - Cancelled", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.FLOAT_STARTING_BALANCE, CountedServiceType.OTHER, PaymentCategory.CREDIT_SPEND, BillType.ShiftStartFundBill),
    FUND_SHIFT_END_BILL("Shift End Fund Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.FLOAT_CLOSING_BALANCE, CountedServiceType.OTHER, PaymentCategory.CREDIT_SPEND, BillType.ShiftEndFundBill),
    FUND_SHIFT_HANDOVER_CREATE("Shift Handover Create Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.FLOAT_DECREASE, CountedServiceType.OTHER, PaymentCategory.OTHER, BillType.CashHandoverCreateBill),
    FUND_SHIFT_COMPONANT_HANDOVER_CREATE("Shift Handover Componant Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.FLOAT_CHANGE, CountedServiceType.OTHER, PaymentCategory.OTHER, BillType.FUND_SHIFT_COMPONANT_HANDOVER_CREATE),
    FUND_SHIFT_DENOMINATION_HANDOVER_CREATE("Shift Handover Denomination Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.FLOAT_CHANGE, CountedServiceType.OTHER, PaymentCategory.OTHER, BillType.CashHandoverCreateBill),
    FUND_SHIFT_HANDOVER_ACCEPT("Shift Handover Accept", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.FLOAT_INCREASE, CountedServiceType.OTHER, PaymentCategory.OTHER, BillType.CashHandoverAcceptBill),
    FUND_SHIFT_COMPONANT_HANDOVER_ACCEPT("Shift Handover Accept Componant Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.FLOAT_CLOSING_BALANCE, CountedServiceType.OTHER, PaymentCategory.OTHER, BillType.FUND_SHIFT_COMPONANT_HANDOVER_ACCEPT),
    FUND_SHIFT_END_BILL_CANCELLED("Shift End Fund Bill - Cancelled", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.FLOAT_CLOSING_BALANCE, CountedServiceType.OTHER, PaymentCategory.CREDIT_SPEND, BillType.ShiftEndFundBill),
    FUND_SHIFT_END_CASH_RECORD("Record Shift End Cash", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.OTHER, PaymentCategory.OTHER, BillType.RecordShiftEndCash),
    FUND_TRANSFER_BILL("Fund Transfer Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.FLOAT_DECREASE, CountedServiceType.OTHER, PaymentCategory.CREDIT_SPEND, BillType.FundTransferBill),
    FUND_TRANSFER_BILL_CANCELLED("Fund Transfer Bill - Cancelled", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.FLOAT_INCREASE, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.FundTransferBill),
    FUND_TRANSFER_RECEIVED_BILL("Fund Transfer Received Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.FLOAT_INCREASE, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.FundTransferReceivedBill),
    FUND_TRANSFER_RECEIVED_BILL_CANCELLED("Fund Transfer Received Bill - Cancelled", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.FLOAT_DECREASE, CountedServiceType.OTHER, PaymentCategory.CREDIT_SPEND, BillType.FundTransferReceivedBill),
    FUND_SHIFT_SHORTAGE_BILL("Shift Shortage Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.FLOAT_INCREASE, CountedServiceType.OTHER, PaymentCategory.NO_PAYMENT, BillType.ShiftShortage),
    FUND_SHIFT_SHORTAGE_BILL_CANCELLED("Shift Shortage Bill - Cancelled", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.FLOAT_DECREASE, CountedServiceType.OTHER, PaymentCategory.NO_PAYMENT, BillType.ShiftShortage),
    FUND_SHIFT_EXCESS_BILL("Shift Excess Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.CASH_IN, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_COLLECTION, BillType.ShiftExcess),
    FUND_SHIFT_EXCESS_BILL_CANCELLED("Shift Excess Bill - Cancelled", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.CASH_OUT, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.ShiftExcess),
    TRANSFER_PAYMENT_METHOD_BILL("Transfer Payment Method Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.CASH_IN, CountedServiceType.OTHER, PaymentCategory.NO_PAYMENT, BillType.PaymentTransfer),
    TRANSFER_PAYMENT_METHOD_BILL_CANCELLED("Transfer Payment Method Bill - Cancelled", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.CASH_IN, CountedServiceType.OTHER, PaymentCategory.NO_PAYMENT, BillType.PaymentTransfer),
    // PROFESSIONAL PAYMENTS - CHANNELLING
    PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE("Channelling Payment for Staff", BillCategory.PAYMENTS, ServiceType.CHANNELLING, BillFinanceType.CASH_OUT, CountedServiceType.CHANNELLING_PROFESSIONAL_PAYMENT, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelProPayment),
    PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_SESSION("Channelling session Payment for Staff", BillCategory.PAYMENTS, ServiceType.CHANNELLING, BillFinanceType.CASH_OUT, CountedServiceType.CHANNELLING_PROFESSIONAL_PAYMENT, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelProPayment),
    PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES("Channelling Payment for Staff for agencies", BillCategory.PAYMENTS, ServiceType.OTHER, BillFinanceType.CASH_IN, CountedServiceType.OTHER_PROFESSIONAL_PAYMENT, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelAgencyPayment),
    PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_RETURN("Channelling Payment for Staff - Return and Cancellation", BillCategory.PAYMENTS, ServiceType.CHANNELLING, BillFinanceType.CASH_IN, CountedServiceType.CHANNELLING_PROFESSIONAL_PAYMENT_RETURN, PaymentCategory.NON_CREDIT_COLLECTION, BillType.ChannelProPayment),
    PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_CHANNELING_SERVICE_FOR_AGENCIES_RETURN("Channelling Payment for Staff for agencies - Return and Cancellation", BillCategory.PAYMENTS, ServiceType.OTHER, BillFinanceType.CASH_OUT, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.ChannelAgencyPayment),
    // PROFESSIONAL PAYMENTS - OPD
    PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES("OPD Professional Payment bill", BillCategory.PAYMENTS, ServiceType.PROFESSIONAL_PAYMENT, BillFinanceType.CASH_OUT, CountedServiceType.OPD_PROFESSIONAL_PAYMENT, PaymentCategory.NON_CREDIT_SPEND, BillType.OpdProfessionalFeePayment),
    PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_OPD_SERVICES_RETURN("OPD Professional Payment bill - Return and Cancellation", BillCategory.PAYMENTS, ServiceType.PROFESSIONAL_PAYMENT, BillFinanceType.CASH_IN, CountedServiceType.OPD_PROFESSIONAL_PAYMENT_RETURN, PaymentCategory.NON_CREDIT_COLLECTION, BillType.OpdProfessionalFeePayment),
    // PROFESSIONAL PAYMENTS - INWARD
    PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE("Inward Payment for Staff", BillCategory.PAYMENTS, ServiceType.INWARD, BillFinanceType.CASH_IN, CountedServiceType.INWARD_PROFESSIONAL_PAYMENT, PaymentCategory.NON_CREDIT_SPEND, BillType.InwardProfessional),
    PROFESSIONAL_PAYMENT_FOR_STAFF_FOR_INWARD_SERVICE_RETURN("Inward Payment for Staff - Return and Cancellation", BillCategory.PAYMENTS, ServiceType.INWARD, BillFinanceType.CASH_OUT, CountedServiceType.INWARD_PROFESSIONAL_PAYMENT_RETURN, PaymentCategory.NON_CREDIT_COLLECTION, BillType.InwardProfessional),
    PETTY_CASH_ISSUE("Petty Cash Issue", BillCategory.PAYMENTS, ServiceType.OTHER, BillFinanceType.CASH_OUT, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.PettyCash),
    PETTY_CASH_RETURN("Petty Cash Return", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.CASH_IN, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.PettyCashReturn),
    PETTY_CASH_BILL_CANCELLATION("Petty Cash Bill Cancellation", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.CASH_IN, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.PettyCashCancelApprove),
    IOU_CASH_ISSUE("Iou Cash Issue", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.FLOAT_CHANGE, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.IouIssue),
    IOU_SETTLE("Iou Cash Settle", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.FLOAT_CHANGE, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_COLLECTION, BillType.IouSettle),
    STAFF_CREDIT_SETTLE("Staff Credit Settle", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.CASH_IN, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.StaffCreditSettle),
    PATIENT_DEPOSIT("Patient Deposit Settle", BillCategory.BILL, ServiceType.PATIENT_DEPOSIT, BillFinanceType.CASH_IN, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.DepositFundBill),
    PATIENT_DEPOSIT_REFUND("Patient Deposit - Refund", BillCategory.REFUND, ServiceType.PATIENT_DEPOSIT, BillFinanceType.CASH_OUT, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.WithdrawalFundBill),
    PATIENT_DEPOSIT_CANCELLED("Patient Deposit - Cancelled", BillCategory.CANCELLATION, ServiceType.PATIENT_DEPOSIT, BillFinanceType.CASH_OUT, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.WithdrawalFundBill),
    // Agency Payments
    AGENCY_PAYMENT_RECEIVED("Agency Payment Received", BillCategory.BILL, ServiceType.AGENCY, BillFinanceType.CASH_IN, CountedServiceType.AGENCY, PaymentCategory.NON_CREDIT_SPEND, BillType.AgentPaymentReceiveBill),
    AGENCY_PAYMENT_CANCELLATION("Agency Payment Cancellation", BillCategory.CANCELLATION, ServiceType.AGENCY, BillFinanceType.CASH_OUT, CountedServiceType.AGENCY, PaymentCategory.NON_CREDIT_SPEND, BillType.AgentPaymentReceiveBill),
    AGENCY_CREDIT_NOTE("Agency Credit Note", BillCategory.BILL, ServiceType.AGENCY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.AGENCY, PaymentCategory.CREDIT_SPEND, BillType.AgentCreditNoteBill),
    AGENCY_CREDIT_NOTE_CANCELLATION("Agency Credit Note Cancellation", BillCategory.CANCELLATION, ServiceType.AGENCY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.AGENCY, PaymentCategory.CREDIT_SPEND, BillType.AgentCreditNoteBill),
    AGENCY_DEBIT_NOTE("Agency Debit Note", BillCategory.BILL, ServiceType.AGENCY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.AGENCY, PaymentCategory.CREDIT_SPEND, BillType.AgentDebitNoteBill),
    AGENCY_DEBIT_NOTE_CANCELLATION("Agency Debit Note Cancellation", BillCategory.CANCELLATION, ServiceType.AGENCY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.AGENCY, PaymentCategory.CREDIT_SPEND, BillType.AgentDebitNoteBill),
    // OPD Payments from Credit Companies
    OPD_CREDIT_COMPANY_PAYMENT_RECEIVED("OPD Payment Received from Credit Company", BillCategory.BILL, ServiceType.COMPANY_CREDIT, BillFinanceType.CASH_IN, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    OPD_CREDIT_COMPANY_PAYMENT_CANCELLATION("OPD Payment Cancellation from Credit Company", BillCategory.CANCELLATION, ServiceType.COMPANY_CREDIT, BillFinanceType.CASH_OUT, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    OPD_CREDIT_COMPANY_CREDIT_NOTE("OPD Credit Note from Credit Company", BillCategory.BILL, ServiceType.COMPANY_CREDIT, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.AGENCY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentRefundBill),
    OPD_CREDIT_COMPANY_DEBIT_NOTE("OPD Debit Note from Credit Company", BillCategory.BILL, ServiceType.COMPANY_CREDIT, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.AGENCY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentRefundBill),
    // Pharmacy Payments from Credit Companies
    PHARMACY_CREDIT_COMPANY_PAYMENT_RECEIVED("Pharmacy Payment Received from Credit Company", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.CASH_IN, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    PHARMACY_CREDIT_COMPANY_PAYMENT_CANCELLATION("Pharmacy Payment Cancellation from Credit Company", BillCategory.CANCELLATION, ServiceType.PHARMACY, BillFinanceType.CASH_OUT, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    PHARMACY_CREDIT_COMPANY_CREDIT_NOTE("Pharmacy Credit Note from Credit Company", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentRefundBill),
    PHARMACY_CREDIT_COMPANY_DEBIT_NOTE("Pharmacy Debit Note from Credit Company", BillCategory.BILL, ServiceType.PHARMACY, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.PHARMACY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentRefundBill),
    // Inpatient Payments from Credit Companies
    INPATIENT_CREDIT_COMPANY_PAYMENT_RECEIVED("Inpatient Payment Received from Credit Company", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.CASH_IN, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    INPATIENT_CREDIT_COMPANY_PAYMENT_CANCELLATION("Inpatient Payment Cancellation from Credit Company", BillCategory.CANCELLATION, ServiceType.INWARD, BillFinanceType.CASH_OUT, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    INPATIENT_CREDIT_COMPANY_CREDIT_NOTE("Inpatient Credit Note from Credit Company", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentRefundBill),
    INPATIENT_CREDIT_COMPANY_DEBIT_NOTE("Inpatient Debit Note from Credit Company", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.INWARD, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentRefundBill),
    // Channelling Payments from Credit Companies
    CHANNELLING_CREDIT_COMPANY_PAYMENT_RECEIVED("Channelling Payment Received from Credit Company", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.CASH_IN, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    CHANNELLING_CREDIT_COMPANY_PAYMENT_CANCELLATION("Channelling Payment Cancellation from Credit Company", BillCategory.CANCELLATION, ServiceType.CHANNELLING, BillFinanceType.CASH_OUT, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    CHANNELLING_CREDIT_COMPANY_CREDIT_NOTE("Channelling Credit Note from Credit Company", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentRefundBill),
    CHANNELLING_CREDIT_COMPANY_DEBIT_NOTE("Channelling Debit Note from Credit Company", BillCategory.BILL, ServiceType.CHANNELLING, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.CHANNELLING, PaymentCategory.NON_CREDIT_COLLECTION, BillType.PatientPaymentRefundBill),
    // BANKING TRANSACTIONS
    FUND_DEPOSIT_BILL("Deposit Fund Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.BANK_OUT, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.DepositFundBill),
    FUND_DEPOSIT_BILL_CANCELLED("Deposit Fund Bill - Cancelled", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.BANK_IN, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.DepositFundBill),
    FUND_WITHDRAWAL_BILL("Withdrawal Fund Bill", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.BANK_IN, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.WithdrawalFundBill),
    FUND_WITHDRAWAL_BILL_CANCELLED("Withdrawal Fund Bill - Cancelled", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.BANK_OUT, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.WithdrawalFundBill),
    // Settling Credit Bills by Companies
    CREDIT_COMPANY_OPD_PATIENT_PAYMENT("Credit Settlement by Company for OPD Services", BillCategory.BILL, ServiceType.OPD, BillFinanceType.CASH_IN, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    CREDIT_COMPANY_OPD_PATIENT_PAYMENT_CANCELLATION("Credit Settlement by Company for OPD Services - Cancellation", BillCategory.CANCELLATION, ServiceType.OPD, BillFinanceType.CASH_OUT, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    CREDIT_COMPANY_OPD_PATIENT_PAYMENT_REFUND("Credit Settlement by Company for OPD Services - Refund", BillCategory.REFUND, ServiceType.OPD, BillFinanceType.CASH_OUT, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.CREDIT_COLLECTION, BillType.PatientPaymentRefundBill),
    CREDIT_COMPANY_INPATIENT_PAYMENT("Credit Settlement by Company for Inpatient Services", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.CASH_IN, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    CREDIT_COMPANY_INPATIENT_PAYMENT_CANCELLATION("Credit Settlement by Company for Inpatient Services - Cancellation", BillCategory.CANCELLATION, ServiceType.INWARD, BillFinanceType.CASH_OUT, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    CREDIT_COMPANY_INPATIENT_PAYMENT_REFUND("Credit Settlement by Company for Inpatient Services - Refund", BillCategory.REFUND, ServiceType.INWARD, BillFinanceType.CASH_OUT, CountedServiceType.CREDIT_SETTLE_BY_COMPANY, PaymentCategory.CREDIT_COLLECTION, BillType.PatientPaymentRefundBill),
    // Settling Credit Bills by Patients
    CREDIT_SETTLE_FOR_OPD_SERVICES_BY_PATIENT("Credit Settlement by Patient for OPD Services", BillCategory.BILL, ServiceType.OPD, BillFinanceType.CASH_IN, CountedServiceType.CREDIT_SETTLE_BY_PATIENT, PaymentCategory.CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    CREDIT_SETTLE_FOR_OPD_SERVICES_BY_PATIENT_CANCELLATION("Credit Settlement by Patient for OPD Services - Cancellation", BillCategory.CANCELLATION, ServiceType.OPD, BillFinanceType.CASH_OUT, CountedServiceType.CREDIT_SETTLE_BY_PATIENT, PaymentCategory.CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    CREDIT_SETTLE_FOR_OPD_SERVICES_BY_PATIENT_REFUND("Credit Settlement by Patient for OPD Services - Refund", BillCategory.REFUND, ServiceType.OPD, BillFinanceType.CASH_OUT, CountedServiceType.CREDIT_SETTLE_BY_PATIENT, PaymentCategory.CREDIT_COLLECTION, BillType.PatientPaymentRefundBill),
    CREDIT_SETTLE_FOR_INPATIENT_SERVICES_BY_PATIENT("Credit Settlement by Patient for Inpatient Services", BillCategory.BILL, ServiceType.INWARD, BillFinanceType.CASH_IN, CountedServiceType.CREDIT_SETTLE_BY_PATIENT, PaymentCategory.CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    CREDIT_SETTLE_FOR_INPATIENT_SERVICES_BY_PATIENT_CANCELLATION("Credit Settlement by Patient for Inpatient Services - Cancellation", BillCategory.CANCELLATION, ServiceType.INWARD, BillFinanceType.CASH_OUT, CountedServiceType.CREDIT_SETTLE_BY_PATIENT, PaymentCategory.CREDIT_COLLECTION, BillType.PatientPaymentReceiveBill),
    CREDIT_SETTLE_FOR_INPATIENT_SERVICES_BY_PATIENT_REFUND("Credit Settlement by Patient for Inpatient Services - Refund", BillCategory.REFUND, ServiceType.INWARD, BillFinanceType.CASH_OUT, CountedServiceType.CREDIT_SETTLE_BY_PATIENT, PaymentCategory.CREDIT_COLLECTION, BillType.PatientPaymentRefundBill),
    // Additional Income
    SUPPLEMENTARY_INCOME("Supplementary Income", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.CASH_IN, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_COLLECTION, BillType.SUPPLEMENTARY_INCOME),
    SUPPLEMENTARY_INCOME_CANCELLED("Supplementary Income - Cancelled", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.CASH_OUT, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_COLLECTION, BillType.SUPPLEMENTARY_INCOME_CANCELLED),
    CHEQUE_REALIZED("Cheque Realized", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.NONE, PaymentCategory.NO_PAYMENT, BillType.CHEQUE_REALIZED),
    CHEQUE_REALIZED_CANCELLATION("Cheque Realized Cancellation", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.NONE, PaymentCategory.NO_PAYMENT, BillType.CHEQUE_REALIZED),
    CHEQUE_PAID("Cheque Paid", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.NONE, PaymentCategory.NO_PAYMENT, BillType.CHEQUE_PAID),
    CHEQUE_PAID_CANCELLATION("Cheque Paid Cancellation", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.NONE, PaymentCategory.NO_PAYMENT, BillType.CHEQUE_PAID),
    // Operational Expenses
    OPERATIONAL_EXPENSES("Operational Expenses", BillCategory.BILL, ServiceType.OTHER, BillFinanceType.CASH_OUT, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.OPERATIONAL_EXPENSES),
    OPERATIONAL_EXPENSES_CANCELLED("Operational Expenses - Cancelled", BillCategory.CANCELLATION, ServiceType.OTHER, BillFinanceType.CASH_IN, CountedServiceType.OTHER, PaymentCategory.NON_CREDIT_SPEND, BillType.OPERATIONAL_EXPENSES_CANCELLED),
    SUPPLIER_PAYMENT("Supplier Payment", BillCategory.BILL, ServiceType.SETTLEMENT, BillFinanceType.CASH_OUT, CountedServiceType.SUPPLIER_PAYMENT, PaymentCategory.NON_CREDIT_SPEND, BillType.GrnPayment),
    SUPPLIER_PAYMENT_PREPERATION("Supplier Payment Preperation", BillCategory.PREBILL, ServiceType.SETTLEMENT, BillFinanceType.NO_FINANCE_TRANSACTIONS, CountedServiceType.SUPPLIER_PAYMENT, PaymentCategory.NO_PAYMENT, BillType.GrnPaymentPreparation),
    SUPPLIER_PAYMENT_CANCELLED("GRN Payment Cancelled", BillCategory.CANCELLATION, ServiceType.SETTLEMENT, BillFinanceType.CASH_IN, CountedServiceType.SUPPLIER_PAYMENT, PaymentCategory.NON_CREDIT_COLLECTION, BillType.GrnPayment),
    SUPPLIER_PAYMENT_RETURNED("Supplier Payment Returned", BillCategory.REFUND, ServiceType.SETTLEMENT, BillFinanceType.CASH_IN, CountedServiceType.SUPPLIER_PAYMENT, PaymentCategory.NON_CREDIT_COLLECTION, BillType.GrnPayment),;

    public static List<BillTypeAtomic> findOpdAndInpatientServiceAndInvestigationIndividualBillTypes() {
        List<BillTypeAtomic> btas = new ArrayList<>();
        // OPD Bill Types
        btas.add(OPD_BILL_WITH_PAYMENT);
        // Inpatient Service Bill Types
        btas.add(INWARD_SERVICE_BILL);
        return btas;
    }

    public static List<BillTypeAtomic> findOpdAndInpatientServiceAndInvestigationBatchBillTypes() {
        List<BillTypeAtomic> btas = new ArrayList<>();
        // OPD Bill Types
        btas.add(OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER);
        btas.add(OPD_BATCH_BILL_WITH_PAYMENT);
        btas.add(PACKAGE_OPD_BATCH_BILL_WITH_PAYMENT);
        btas.add(PACKAGE_OPD_BATCH_BILL_PAYMENT_COLLECTION_AT_CASHIER);
        btas.add(INWARD_SERVICE_BATCH_BILL);
        return btas;
    }

    private final String label;
    private final BillCategory billCategory;
    private final ServiceType serviceType;
    private final BillFinanceType billFinanceType;
    private final CountedServiceType countedServiceType;
    private final PaymentCategory paymentCategory;
    private final BillType billType;

    public BillType getBillType() {
        return billType;
    }

    public static BillTypeAtomic getBillTypeAtomic(BillType billType, BillClassType billClassType) {
        System.out.println("getBillTypeAtomic");
        System.out.println("billClassType = " + billClassType);
        System.out.println("billType = " + billType);
        switch (billClassType) {
            case Bill:
                switch (billType) {
                    case PharmacyWholeSale:
                        return BillTypeAtomic.PHARMACY_WHOLESALE_GRN_BILL;
                    case PharmacyPurchaseBill:
                        return BillTypeAtomic.PHARMACY_DIRECT_PURCHASE;
                    case PharmacySnapshotBill:
                        return BillTypeAtomic.PHARMACY_SNAPSHOT_GENERATION;
                    case PharmacyPhysicalCountBill:
                        return BillTypeAtomic.PHARMACY_PHYSICAL_COUNT_ENTRY;
                    case PharmacyStockAdjustmentBill:
                        return BillTypeAtomic.PHARMACY_STOCK_ADJUSTMENT_BILL;
                }
                break;
            case BilledBill:
                switch (billType) {
                    case PharmacyGrnBill:
                        return BillTypeAtomic.PHARMACY_GRN;
                    case PharmacyWholeSale:
                        return BillTypeAtomic.PHARMACY_WHOLESALE_GRN_BILL;
                    case PharmacyPurchaseBill:
                        return BillTypeAtomic.PHARMACY_DIRECT_PURCHASE;
                    case PharmacySnapshotBill:
                        return BillTypeAtomic.PHARMACY_SNAPSHOT_GENERATION;
                    case PharmacyPhysicalCountBill:
                        return BillTypeAtomic.PHARMACY_PHYSICAL_COUNT_ENTRY;
                    case PharmacyStockAdjustmentBill:
                        return BillTypeAtomic.PHARMACY_STOCK_ADJUSTMENT_BILL;
                }
            case CancelledBill:
                switch (billType) {
                    case PharmacyGrnBill:
                        return BillTypeAtomic.PHARMACY_GRN_CANCELLED;
                    case PharmacyWholeSale:
                        return BillTypeAtomic.PHARMACY_WHOLESALE_GRN_BILL_CANCELLED;
                    case PharmacyPurchaseBill:
                        return BillTypeAtomic.PHARMACY_DIRECT_PURCHASE_CANCELLED;
                }
            case OtherBill:
                return null;
            case PreBill:
                switch (billType) {
                    case PharmacyBhtPre:
                        return BillTypeAtomic.DIRECT_ISSUE_INWARD_MEDICINE_CANCELLATION;
                    case PharmacyWholeSale:
                        return BillTypeAtomic.PHARMACY_WHOLESALE_GRN_BILL_CANCELLED;
                    case PharmacyPurchaseBill:
                        return BillTypeAtomic.PHARMACY_DIRECT_PURCHASE_CANCELLED;
                    case PharmacyPre:
                        return BillTypeAtomic.PHARMACY_RETAIL_SALE_CANCELLED_PRE;
                }
                return null;
            case RefundBill:
                switch (billType) {
                    case PharmacyGrnBill:
                        return BillTypeAtomic.PHARMACY_GRN_REFUND;
                    case PharmacyWholeSale:
                        return BillTypeAtomic.PHARMACY_WHOLESALE_GRN_BILL_REFUND;
                    case PharmacyPurchaseBill:
                        return BillTypeAtomic.PHARMACY_DIRECT_PURCHASE_CANCELLED;
                }

            default:
                return null;
        }
        return null;
    }

    BillTypeAtomic(String label, BillCategory billCategory, ServiceType serviceType, BillFinanceType billFinanceType, CountedServiceType countedServiceType, PaymentCategory paymentCategory, BillType billType) {
        this.label = label;
        this.billCategory = billCategory;
        this.serviceType = serviceType;
        this.billFinanceType = billFinanceType;
        this.countedServiceType = countedServiceType;
        this.paymentCategory = paymentCategory;
        this.billType = billType;
    }

    BillTypeAtomic(String label, BillCategory billCategory, ServiceType serviceType, BillFinanceType billFinanceType, CountedServiceType countedServiceType, PaymentCategory paymentCategory) {
        this(label, billCategory, serviceType, billFinanceType, countedServiceType, paymentCategory, null);
    }

//    BillTypeAtomic(String label, BillCategory billCategory, ServiceType serviceType, BillFinanceType billFinanceType, CountedServiceType countedServiceType, PaymentCategory paymentCategory) {
//        this.label = label;
//        this.billCategory = billCategory;
//        this.serviceType = serviceType;
//        this.billFinanceType = billFinanceType;
//        this.countedServiceType = countedServiceType;
//        this.paymentCategory = paymentCategory;
//    }
    public static List<BillTypeAtomic> findByServiceTypeAndPaymentCategory(ServiceType serviceType, PaymentCategory paymentCategory) {
        return Arrays.stream(BillTypeAtomic.values())
                .filter(billType -> billType.getServiceType() == serviceType && billType.getPaymentCategory() == paymentCategory)
                .collect(Collectors.toList());
    }

    // Method to find BillTypeAtomic by BillFinanceType
    public static List<BillTypeAtomic> findByFinanceType(BillFinanceType financeType) {
        return Arrays.stream(BillTypeAtomic.values())
                .filter(e -> e.getBillFinanceType() == financeType)
                .collect(Collectors.toList());
    }

    public PaymentCategory getPaymentCategory() {
        return paymentCategory;
    }

    // Method to find BillTypeAtomic by BillCategory
    public static List<BillTypeAtomic> findByCategory(BillCategory category) {
        return Arrays.stream(BillTypeAtomic.values())
                .filter(e -> e.getBillCategory() == category)
                .collect(Collectors.toList());
    }

    public static List<BillTypeAtomic> findAll() {
        return Arrays.stream(BillTypeAtomic.values())
                .collect(Collectors.toList());
    }

    public String getLabel() {
        return label;
    }

    public BillCategory getBillCategory() {
        return billCategory;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public BillFinanceType getBillFinanceType() {
        return billFinanceType;
    }

    public CountedServiceType getCountedServiceType() {
        return countedServiceType;
    }

// Method to find BillTypeAtomic by ServiceType
    public static List<BillTypeAtomic> findByServiceType(ServiceType serviceType) {
        return Arrays.stream(BillTypeAtomic.values())
                .filter(billType -> billType.getServiceType() == serviceType)
                .collect(Collectors.toList());
    }

    // Method to find BillTypeAtomic by CountedServiceType
    public static List<BillTypeAtomic> findByCountedServiceType(CountedServiceType countedServiceType) {
        return Arrays.stream(BillTypeAtomic.values())
                .filter(billType -> billType.getCountedServiceType() == countedServiceType)
                .collect(Collectors.toList());
    }

    // Method to find BillTypeAtomic by ServiceType and BillFinanceType
    public static List<BillTypeAtomic> findByServiceTypeAndFinanceType(ServiceType serviceType, BillFinanceType financeType) {
        return Arrays.stream(BillTypeAtomic.values())
                .filter(billType -> billType.getServiceType() == serviceType && billType.getBillFinanceType() == financeType)
                .collect(Collectors.toList());
    }

    // Method to find BillTypeAtomic by ServiceType and BillCategory
    public static List<BillTypeAtomic> findBillTypeAtomic(ServiceType serviceType, BillCategory billCategory) {
        return Arrays.stream(BillTypeAtomic.values())
                .filter(billType -> billType.getServiceType() == serviceType && billType.getBillCategory() == billCategory)
                .collect(Collectors.toList());
    }

}
