/*
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.core.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Buddhika
 */
public enum BillType {

    //Dont Use in With BillType in Channel
    @Deprecated
    ChannelCashFlow(null),
    @Deprecated
    ChannelCreditFlow(null),
    OpdBathcBill,
    OpdBathcBillPre,
    OpdProfessionalFeePayment,
    SurgeryBill,
    LabBill,
    CollectingCentreBill,
    CollectingCentreAgentPayment,
    CollectingCentreAgentPaymentCancel,
    CollectingCentreBatchBill,
    PaymentBill,//To Pay Professional Payments
    OpdBill,
    OpdPreBill,
    InwardPaymentBill,
    InwardFinalBill,
    InwardOriginalFinalBill,
    InwardProvisionalBill,
    InwardFinalBillCCPayment,
    InwardAppointmentBill,
    InwardBill,
    InwardProfessional,
    InwardProfessionalEstimates,
    InwardIntrimBill,
    @Deprecated
    InwardAdditionalBill,
    InwardOutSideBill,
    AdmissionBill,
    CashRecieveBill,
    PettyCash,
    PettyCashReturn,
    PettyCashCancelApprove,
    IouIssue,
    IouSettle,
    AgentPaymentReceiveBill,
    AgentCreditNoteBill,
    AgentDebitNoteBill,
    PatientPaymentReceiveBill,
    PatientPaymentRefundBill,
    PatientPaymentCanceldBill,
    CollectingCentrePaymentReceiveBill,
    CollectingCentrePaymentMadeBill,
    CollectingCentreCreditNoteBill,
    CollectingCentreDebitNoteBill,
    @Deprecated
    PharmacyBill, //Cash In
    PharmacySale,
    PharmacySaleWithoutStock,
    PharmacyWholeSale,
    @Deprecated
    SandryGrn,
    PharmacyIssue,
    PharmacyDisposalIssue,
    @Deprecated
    PharmacyBhtIssue,
    PharmacyBhtPre,
    InwardPharmacyRequest,
    @Deprecated
    StoreBhtIssue,
    StoreBhtPre,
    StoreIssue,
    //    @Deprecated Piumi requested issue 60
    StoreTransferIssue,
    StoreTransferReceive,
    StoreTransferRequest,
    PharmacyPre,
    PharmacyWholesalePre,
    PharmacyOrder,
    PharmacyOrderApprove,
    PharmacyGrnBill,//Cash out
    PharmacyGrnBillImport,//Cash out
    PharmacyGrnReturn,
    PharmacyReturnWithoutTraising,
    GrnPayment,
    GrnPaymentPreparation,
    GrnPaymentPre,
    PharmacyPurchaseBill, //Cash out
    PurchaseReturn,// Purchase Return
    PharmacyDonationBill,
    PharmacyTransferRequest,
    PharmacyTransferIssue,
    PharmacyDirectIssue,
    PharmacyTransferReceive,
    PharmacyDirectReceive,
    PharmacyAdjustment,
    PharmacyAdjustmentDepartmentStock,
    PharmacyAdjustmentDepartmentSingleStock,
    PharmacyAdjustmentStaffStock,
    PharmacyAdjustmentSaleRate,
    PharmacyAdjustmentWholeSaleRate,
    PharmacyAdjustmentPurchaseRate,
    PharmacyAdjustmentCostRate,
    PharmacyAdjustmentExpiryDate,
    PharmacyAddtoStock,
    DrawerAdjustment,
    PharmacyMajorAdjustment,
    PharmacySnapshotBill,
    PharmacyPhysicalCountBill,
    PharmacyStockAdjustmentBill,
    ChannelCash(ChannelCashFlow),
    ChannelPaid(ChannelCashFlow),
    ChannelAgent(ChannelCashFlow),
    ChannelOnCall(ChannelCreditFlow),
    ChannelStaff(ChannelCreditFlow),
    ChannelResheduleWithOutPayment(ChannelCreditFlow),
    ChannelResheduleWithPayment(ChannelCashFlow),
    //    @Deprecated need to payment bills for separately
    ChannelProPayment,
    ChannelAgencyPayment,
    ChannelAgencyCommission,
    ChannelOnlineBookingAgentPaidToHospital,
    ChannelOnlineBookingAgentPaidToHospitalBillCancellation,
    gpBooking,
    gpSettling,
    Appointment,
    @Deprecated
    GrnPaymentBill,
    @Deprecated
    GrnPaymentReturn,
    @Deprecated
    GrnPaymentCancell,
    @Deprecated
    GrnPaymentCancellReturn,
    CashIn,
    CashOut,
    @Deprecated
    ChannelCredit,
    ClinicalOpdBooking,
    StorePurchase,
    StoreGrnBill,
    StoreAdjustment,
    StoreSale,
    StoreOrderApprove,
    StoreGrnReturn,
    StorePre,
    StoreOrder,
    StorePurchaseReturn,
    //Old Bills
    ChannelCashOld,
    ChannelPaidOld,
    ChannelAgentOld,
    ChannelOnCallOld,
    ChannelStaffOld,
    ChannelIncomeBill,
    ChannelExpenesBill,
    // channel Session sperate X-ray scan or channel
    Channel,
    XrayScan,
    // Cash Handling and Transfer Processes
    ShiftStartFundBill, // For handling initial funds, be it cash, cheque, or electronic funds, at the beginning of a cashier's shift
    ShiftEndFundBill, // For summarising and finalising all transaction types, balances, and notes at the end of a cashier's shift
    CashHandoverCreateBill,
    CashHandoverAcceptBill,
    RecordShiftEndCash,
    FundTransferBill, // For transferring the total balance from one shift to another
    FundTransferReceivedBill, // For receiving the transferred balance from one shift to another
    DepositFundBill, // For processing deposits of all payment types into the bank by the main or bulk cashier
    WithdrawalFundBill, // For handling withdrawal transactions from the bank for operational purposes
    ShiftShortage,
    ShiftExcess,
    PaymentTransfer,
    @Deprecated
    TransactionHandoverBill, // For handling the handover of all transaction types at the end of a cashier's shift
    @Deprecated
    TransactionVerificationBill, // For the incoming cashier to verify all transaction types
    @Deprecated
    FinancialReconciliationBill, // For reconciling all types of recorded transactions against actual bank statements and balances
    @Deprecated
    FinancialAuditingBill, // For broader auditing purposes, ensuring compliance with policies and regulatory requirements
    StaffCreditSettle,
    FUND_SHIFT_COMPONANT_HANDOVER_CREATE,
    FUND_SHIFT_COMPONANT_HANDOVER_ACCEPT,
    SUPPLEMENTARY_INCOME,
    SUPPLEMENTARY_INCOME_CANCELLED,
    OPERATIONAL_EXPENSES,
    OPERATIONAL_EXPENSES_CANCELLED,
    CHEQUE_PAID,
    CHEQUE_REALIZED;

    public String getLabel() {
        switch (this) {
            case OpdBill:
                return "OPD Bill";
            case PaymentBill:
                return "Professional Payment Bill";
            case PettyCash:
                return "Petty Cash Payment Bill";
            case CashRecieveBill:
                return "Credit Company Payment Receive Bill";
            case AgentPaymentReceiveBill:
                return "Agent Payment Receive Bill";
            case InwardPaymentBill:
                return "Inward Payment Receive Bill";
            case PharmacyOrder:
                return "Purchase Order Request";
            case PharmacyWholeSale:
                return "Pharmacy Wholesale";
            case PharmacyWholesalePre:
                return "Purchase Wholsesale (Pre)";
            case PharmacyOrderApprove:
                return "Purchase Order Aproval";
            case PharmacyGrnBill:
                return "Good Receive Note";
            case PharmacyGrnReturn:
                return "Good Receive Note Return";
            case PharmacyReturnWithoutTraising:
                return "Pharmacy Return Without Traising";
            case PharmacyPurchaseBill:
                return "Pharmacy Direct Purchase";
            case PurchaseReturn:
                return "Pharmacy Purchase Return";
            case PharmacyDonationBill:
                return "Pharmacy Donation Bill";
            case PharmacySale:
                return "Pharmacy Sale Bill";

            case PharmacyPre:
                return "Pharmacy Sale Bill for Cashier";
            case PharmacyAdjustment:
                return "Pharmacy Adjustment";
            case PharmacySnapshotBill:
                return "Snapshot Bill";
            case PharmacyPhysicalCountBill:
                return "Physical Count Bill";
            case PharmacyStockAdjustmentBill:
                return "Stock Adjustment Bill";
            case GrnPayment:
                return "Grn Payment";
            case GrnPaymentPre:
                return "Grn Payment";
            case PharmacyTransferRequest:
                return "Pharmacy Transfer Request";
            case PharmacyTransferIssue:
                return "Pharmacy Transfer Issue";
            case PharmacyTransferReceive:
                return "Pharmacy Transfer Receive";
            case CashIn:
                return "Cash In Transaction";
            case CashOut:
                return "Cash Out Transaction";
            case ChannelAgent:
                return "Channel Agent";
            case ChannelCash:
                return "Channel Cash";
            case ChannelOnCall:
                return "Channel On Call";
            case ChannelStaff:
                return "Channel Staff";
            case StoreOrder:
                return "Store Order Request";
            case ChannelPaid:
                return "Channel Settle";
            case StoreOrderApprove:
                return "Store Order Aproved";
            case StoreGrnBill:
                return "Store Good Receive Note";
            case StoreGrnReturn:
                return "Store Good Receive Note Return";
            case StorePurchase:
                return "Store Purchase";
            case StorePurchaseReturn:
                return "Store Purchase Return";
            case StoreSale:
                return "Store Sale Bill";
            case StorePre:
                return "Store Sale Bill (Pre)";
            case StoreAdjustment:
                return "Store Adjustment";
            case StoreTransferRequest:
                return "Store Transfer Request";
            case StoreTransferIssue:
                return "Store Transfer Issue";
            case PharmacyIssue:
                return "Pharmacy Transfer Issue";
            case PharmacyDisposalIssue:
                return "Pharmacy Disposal Issue";
            case PharmacyBhtPre:
                return "Pharmacy BHT Issue (Pre)";
            case OpdPreBill:
                return "OPD Bills To Pay";
            case OpdBathcBill:
                return "OPD Accepet Payment";
            case CollectingCentrePaymentReceiveBill:
                return "Collecting Centre Payment Receive";
            case ChannelProPayment:
                return "Channel Professional Payment Bill";
            case ChannelIncomeBill:
                return "Channel Income Bill";
            case ChannelExpenesBill:
                return "Channel Expenses Bill";
            case Channel:
                return "Channel";
            case XrayScan:
                return "X-Ray and Scan";
            case InwardFinalBill:
                return "Inward Final Bill";
            case ShiftStartFundBill:
                return "Initial Fund Bill";
            case FundTransferBill:
                return "Shift Balance Transfer Bill";
            case TransactionHandoverBill:
                return "Transaction Handover Bill";
            case TransactionVerificationBill:
                return "Transaction Verification Bill";
            case DepositFundBill:
                return "Deposit Processing Bill";
            case WithdrawalFundBill:
                return "Withdrawal Processing Bill";
            case FinancialReconciliationBill:
                return "Financial Reconciliation Bill";
            case FinancialAuditingBill:
                return "Financial Auditing Bill";
            case FundTransferReceivedBill:
                return "Fund Transfer Received Bill";
            case RecordShiftEndCash:
                return "Shift End Cash Record";
            default:
                return this.toString();
        }
    }

    public String getCode() {
        switch (this) {
            case OpdBathcBill:
                return "OBB";
            case CollectingCentreBill:
                return "CCB";
            default:
                String originalString = this.getLabel();
                String[] words = originalString.split(" ");

                for (int i = 0; i < words.length && i < 3; i++) {
                    words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
                }

                return String.join("", words);
        }
    }

    private BillType parent = null;

    public BillType getParent() {
        return parent;
    }

    public void setParent(BillType parent) {
        this.parent = parent;
    }

    BillType(BillType parent) {
        this.parent = parent;
        if (this.parent != null) {
            this.parent.addChild(this);
        }
    }

    BillType() {
    }

    private final List<BillType> children = new ArrayList<>();

    public BillType[] children() {
        return children.toArray(new BillType[children.size()]);
    }

    public BillType[] allChildren() {
        List<BillType> list = new ArrayList<>();
        addChildren(this, list);
        return list.toArray(new BillType[list.size()]);
    }

    private static void addChildren(BillType root, List<BillType> list) {
        list.addAll(root.children);
        for (BillType child : root.children) {
            addChildren(child, list);
        }
    }

    private void addChild(BillType child) {
        this.children.add(child);
    }

    public boolean is(BillType other) {
        if (other == null) {
            return false;
        }

        for (BillType t = this; t != null; t = t.parent) {
            if (other == t) {
                return true;
            }
        }
        return false;
    }
}
