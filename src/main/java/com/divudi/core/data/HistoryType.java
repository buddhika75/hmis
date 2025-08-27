
/*
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.core.data;

/**
 * @author Buddhika
 */
public enum HistoryType {

    Sale,
    Issue,
    Order,
    GoodReceive,
    Stock,
    ChannelBooking,
    ChannelBalanceReset,
    @Deprecated
    ChannelBookingCancel,
    ChannelDeposit,
    ChannelDepositCancel,
    ChannelCreditNote,
    ChannelCreditNoteCancel,
    ChannelDebitNote,
    ChannelDebitNoteCancel,

    PatientDeposit,
    PatientDepositCancel,
    PatientDepositReturn,
    PatientDepositUtilization,
    PatientDepositUtilizationCancel,
    PatientDepositUtilizationReturn,
    PatientDepositTransfer,
    PatientDepositTransferCancel,

    AgentBalanceUpdateBill,
    CollectingCentreBalanceUpdateBill,
    CollectingCentreDeposit,
    CollectingCentreDepositCancel,
    CollectingCentreCreditNote,
    CollectingCentreCreditNoteCancel,
    CollectingCentreDebitNote,
    CollectingCentreDebitNoteCancel,
    CollectingCentreBilling,
    CollectingCentreBillingCancel,
    CollectingCentreBillingRefund,
    CollectingentrePaymentMadeBill,
    RepaymentToCollectingCentre,
    RepaymentToCollectingCentreCancel,
    CollectingentrePaymentMadeBillCancelled,
    MonthlyRecord,
}
