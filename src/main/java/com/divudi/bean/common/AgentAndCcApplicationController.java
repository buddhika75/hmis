package com.divudi.bean.common;

import com.divudi.core.data.HistoryType;
import static com.divudi.core.data.HistoryType.RepaymentToCollectingCentre;
import com.divudi.core.entity.AgentHistory;
import com.divudi.core.entity.AuditEvent;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.Institution;
import com.divudi.core.facade.AgentHistoryFacade;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.InstitutionFacade;
import com.divudi.core.util.CommonFunctions;
import com.google.gson.Gson;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.ejb.EJB;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author Buddhika
 */
@Named
@ApplicationScoped
public class AgentAndCcApplicationController {

    @EJB
    AgentHistoryFacade agentHistoryFacade;
    @EJB
    InstitutionFacade institutionFacade;
    @EJB
    BillFacade billFacade;

    @Inject
    AuditEventApplicationController auditEventApplicationController;

    // A map to store locks for each collecting centre based on its ID
    private final Map<Long, Lock> lockMap = new ConcurrentHashMap<>();

    public AgentAndCcApplicationController() {
    }

    public void updateCcBalance(Institution collectingCentre,
            double hospitalFee,
            double collectingCentreFee,
            double staffFee,
            double transactionValue,
            HistoryType historyType,
            Bill bill) {
        updateCcBalance(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, historyType, bill, "");
    }

    public void updateCcBalance(Institution collectingCentre,
            double hospitalFee,
            double collectingCentreFee,
            double staffFee,
            double transactionValue,
            HistoryType historyType,
            Bill bill,
            String comments) {
        switch (historyType) {
            case CollectingCentreBalanceUpdateBill:
                handleCcBalanceUpdateBill(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, bill, comments);
                break;
            case CollectingCentreBillingCancel:
                handleCcBillingCancel(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, bill);
                break;
            case CollectingCentreCreditNoteCancel:
                handleCcCreditNoteCancel(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, bill);
                break;
            case CollectingCentreDebitNote:
                handleCcDebitNote(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, bill);
                break;
            case CollectingCentreDeposit:
                handleCcDeposit(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, bill);
                break;
            case CollectingCentreDepositCancel:
                handleCcDepositCancel(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, bill, comments);
                break;
            case CollectingCentreBilling:
                handleCollectingCentreBilling(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, bill);
                break;
            case CollectingCentreBillingRefund:
                handleCcBillingRefund(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, bill);
                break;
            case CollectingentrePaymentMadeBill: // There is a typo, but can not correct it as the data is already in the database.
                handleCcCollectingCentrePaymentMade(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, bill);
                break;
            case CollectingCentreCreditNote:
                handleCcCollectingCentreCreditNote(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, bill);
                break;
            case CollectingCentreDebitNoteCancel:
                handleCcDebitNoteCancel(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, bill, comments);
                break;
            case RepaymentToCollectingCentre:
                handleCcRePayment(collectingCentre, hospitalFee, collectingCentreFee, staffFee, transactionValue, bill);
                break;
            case RepaymentToCollectingCentreCancel:
                handleCcRePaymentCancel(collectingCentre, transactionValue, bill);
                break;
            default:
                Map<String, Object> errorInfo = new LinkedHashMap<>();
                errorInfo.put("fileName", "AgentAndCcApplicationController.java");
                errorInfo.put("methodName", "updateCcBalance");
                errorInfo.put("historyType", historyType != null ? historyType.name() : null);
                errorInfo.put("billId", bill != null ? bill.getId() : null);
                errorInfo.put("institutionId", bill != null && bill.getInstitution() != null ? bill.getInstitution().getId() : null);
                errorInfo.put("departmentId", bill != null && bill.getDepartment() != null ? bill.getDepartment().getId() : null);
                errorInfo.put("collectingCentreId", collectingCentre != null ? collectingCentre.getId() : null);

                Gson gson = new Gson();
                String jsonWithError = gson.toJson(errorInfo);
                AuditEvent auditEvent = new AuditEvent();
                if (bill != null) {
                    if (bill.getDepartment() != null && bill.getDepartment().getId() != null) {
                        auditEvent.setDepartmentId(bill.getDepartment().getId());
                    }
                    if (bill.getWebUser() != null && bill.getWebUser().getId() != null) {
                        auditEvent.setWebUserId(bill.getWebUser().getId());
                    }
                }
                auditEvent.setEventDataTime(new Date());
                auditEvent.setEventStatus("ERROR");
                auditEvent.setAfterJson(jsonWithError);
                if (auditEventApplicationController != null) {
                    auditEventApplicationController.saveAuditEvent(auditEvent);
                }

                break;
        }
    }

    public Double ccBalanceBefore(Bill b) {
        String jpql;
        jpql = "select h.balanceBeforeTransaction "
                + " from AgentHistory h "
                + " where h.bill=:bill";
        Map params = new HashMap();
        params.put("bill", b);
        return agentHistoryFacade.findDoubleByJpql(jpql, params);
    }

    public Double ccBalanceAfter(Bill b) {
        String jpql;
        jpql = "select h.balanceAfterTransaction "
                + " from AgentHistory h "
                + " where h.bill=:bill";
        Map params = new HashMap();
        params.put("bill", b);
        return agentHistoryFacade.findDoubleByJpql(jpql, params);
    }

    private void handleCcBalanceUpdateBill(Institution collectingCentre, double hospitalFee, double collectingCentreFee, double staffFee, double transactionValue, Bill bill, String comments) {

        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {
            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.CollectingCentreBalanceUpdateBill);
            agentHistory.setCompanyTransactionValue(0);
            agentHistory.setAgentTransactionValue(0);
            agentHistory.setStaffTrasnactionValue(0);
            agentHistory.setTransactionValue(0);
            agentHistory.setComment(comments);

            double balanceBeforeTx = collectingCentre.getBallance();

            double balanceAfterTx = balanceBeforeTx;

            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.create(agentHistory);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);
        } finally {
            lock.unlock();
        }

    }

    private void handleCcBillingCancel(Institution collectingCentre, double hospitalFee, double collectingCentreFee, double staffFee, double transactionValue, Bill bill) {
        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {
            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.RepaymentToCollectingCentreCancel);
            agentHistory.setCompanyTransactionValue(0 - Math.abs(hospitalFee));
            agentHistory.setAgentTransactionValue(0 - Math.abs(collectingCentreFee));
            agentHistory.setStaffTrasnactionValue(0 - Math.abs(staffFee));
            //agentHistory.setTransactionValue(0 - Math.abs(transactionValue));
            agentHistory.setTransactionValue(hospitalFee);
            agentHistory.setPaidAmountByAgency(null);

            double balanceBeforeTx = collectingCentre.getBallance();
            double balanceAfterTx = balanceBeforeTx + Math.abs(hospitalFee);

            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.create(agentHistory);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);

        } finally {
            lock.unlock();
        }
    }

    private void handleCcBillingRefund(Institution collectingCentre, double hospitalFee, double collectingCentreFee, double staffFee, double transactionValue, Bill bill) {
        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {
            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.CollectingCentreBillingCancel);
            agentHistory.setCompanyTransactionValue(0 - Math.abs(hospitalFee));
            agentHistory.setAgentTransactionValue(0 - Math.abs(collectingCentreFee));
            agentHistory.setStaffTrasnactionValue(0 - Math.abs(staffFee));
            agentHistory.setTransactionValue(hospitalFee);
            agentHistory.setPaidAmountByAgency(null);

            double balanceBeforeTx = collectingCentre.getBallance();
            double balanceAfterTx = balanceBeforeTx + Math.abs(hospitalFee);


            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.create(agentHistory);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);


        } finally {
            lock.unlock();
        }
    }

    private void handleCcCollectingCentreCreditNote(Institution collectingCentre, double hospitalFee, double collectingCentreFee, double staffFee, double transactionValue, Bill bill) {
        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {
            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.CollectingCentreCreditNote);
            agentHistory.setCompanyTransactionValue(0);
            agentHistory.setAgentTransactionValue(0);
            agentHistory.setStaffTrasnactionValue(0);
            agentHistory.setTransactionValue(Math.abs(transactionValue));
            agentHistory.setAdjustmentToAgencyBalance(Math.abs(transactionValue));
            agentHistory.setPaidAmountByAgency(null);

            double balanceBeforeTx = collectingCentre.getBallance();
            double balanceAfterTx = balanceBeforeTx + Math.abs(transactionValue);

            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.create(agentHistory);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);

        } finally {
            lock.unlock();
        }
    }

    private void handleCcCollectingCentrePaymentMade(Institution collectingCentre, double hospitalFee, double collectingCentreFee, double staffFee, double transactionValue, Bill bill) {
        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {
            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.CollectingCentreCreditNote);
            agentHistory.setCompanyTransactionValue(0);
            agentHistory.setAgentTransactionValue(0);
            agentHistory.setStaffTrasnactionValue(0);
            agentHistory.setTransactionValue(Math.abs(transactionValue));
            agentHistory.setPaidAmountByAgency(null);

            double balanceBeforeTx = collectingCentre.getBallance();
            double balanceAfterTx = balanceBeforeTx + Math.abs(transactionValue);

            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.create(agentHistory);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);

        } finally {
            lock.unlock();
        }
    }

    private void handleCcCreditNoteCancel(Institution collectingCentre, double hospitalFee, double collectingCentreFee, double staffFee, double transactionValue, Bill bill) {
        // Implementation for CollectingCentreCreditNoteCancel
        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {
            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.CollectingCentreCreditNoteCancel);
            agentHistory.setCompanyTransactionValue(0);
            agentHistory.setAgentTransactionValue(0);
            agentHistory.setStaffTrasnactionValue(0);
            agentHistory.setTransactionValue(0 - Math.abs(transactionValue));
            agentHistory.setAdjustmentToAgencyBalance(0 - Math.abs(transactionValue));
            agentHistory.setPaidAmountByAgency(null);

            double balanceBeforeTx = collectingCentre.getBallance();
            double balanceAfterTx = balanceBeforeTx + transactionValue;

            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.create(agentHistory);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);

        } finally {
            lock.unlock();
        }
    }

    private void handleCcDebitNoteCancel(Institution collectingCentre, double hospitalFee, double collectingCentreFee, double staffFee, double transactionValue, Bill bill, String comment) {
        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {
            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.CollectingCentreDebitNoteCancel);
            agentHistory.setCompanyTransactionValue(0);
            agentHistory.setAgentTransactionValue(0);
            agentHistory.setStaffTrasnactionValue(0);
            agentHistory.setTransactionValue(Math.abs(transactionValue));
            agentHistory.setAdjustmentToAgencyBalance(Math.abs(transactionValue));
            agentHistory.setPaidAmountByAgency(Math.abs(transactionValue));
            agentHistory.setComment(comment);

            double balanceBeforeTx = collectingCentre.getBallance();
            double balanceAfterTx = balanceBeforeTx + Math.abs(transactionValue);

            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.create(agentHistory);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);

        } finally {
            lock.unlock();
        }
    }

    private void handleCcDebitNote(Institution collectingCentre, double hospitalFee, double collectingCentreFee, double staffFee, double transactionValue, Bill bill) {
        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {
            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.CollectingCentreDebitNote);
            agentHistory.setCompanyTransactionValue(0);
            agentHistory.setAgentTransactionValue(0);
            agentHistory.setStaffTrasnactionValue(0);
            agentHistory.setTransactionValue(0 - Math.abs(transactionValue));
            agentHistory.setAdjustmentToAgencyBalance(0 - Math.abs(transactionValue));
            agentHistory.setPaidAmountByAgency(null);

            double balanceBeforeTx = collectingCentre.getBallance();
            double balanceAfterTx = balanceBeforeTx - Math.abs(transactionValue);

            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.create(agentHistory);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);

        } finally {
            lock.unlock();
        }
    }
    
    private void handleCcDeposit(Institution collectingCentre, double hospitalFee, double collectingCentreFee, double staffFee, double transactionValue, Bill bill) {
        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {
            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.CollectingCentreDeposit);
            agentHistory.setCompanyTransactionValue(0);
            agentHistory.setAgentTransactionValue(transactionValue);
            agentHistory.setStaffTrasnactionValue(0);
            agentHistory.setTransactionValue(transactionValue);
            agentHistory.setPaidAmountByAgency(transactionValue);

            double balanceBeforeTx = collectingCentre.getBallance();

            double balanceAfterTx = balanceBeforeTx + collectingCentreFee;

            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.createAndFlush(agentHistory);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);

        } finally {
            lock.unlock();
        }
    }


    private void handleCcRePayment(Institution collectingCentre, double hospitalFee, double collectingCentreFee, double staffFee, double transactionValue, Bill bill) {
        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {
            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.RepaymentToCollectingCentre);
            agentHistory.setCompanyTransactionValue(0);
            agentHistory.setAgentTransactionValue(transactionValue);
            agentHistory.setStaffTrasnactionValue(0);
            agentHistory.setTransactionValue(-transactionValue);
            agentHistory.setPaidAmountToAgency(transactionValue);

            double balanceBeforeTx = collectingCentre.getBallance();

            double balanceAfterTx = balanceBeforeTx - collectingCentreFee;

            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.createAndFlush(agentHistory);
            
            bill.setAgentHistory(agentHistory);
            billFacade.edit(bill);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);

        } finally {
            lock.unlock();
        }
    }
    
    private void handleCcRePaymentCancel(Institution collectingCentre, double transactionValue, Bill bill) {
        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {

            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.RepaymentToCollectingCentreCancel);
            agentHistory.setCompanyTransactionValue(0);
            agentHistory.setAgentTransactionValue(Math.abs(transactionValue));
            agentHistory.setStaffTrasnactionValue(0);
            agentHistory.setTransactionValue(Math.abs(transactionValue));
            agentHistory.setPaidAmountByAgency(Math.abs(transactionValue));

            double balanceBeforeTx = collectingCentre.getBallance();

            double balanceAfterTx = balanceBeforeTx + Math.abs(transactionValue);

            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.createAndFlush(agentHistory);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);

        } finally {
            lock.unlock();
        }
    }

    private void handleCcDepositCancel(Institution collectingCentre, double hospitalFee, double collectingCentreFee, double staffFee, double transactionValue, Bill bill, String comments) {
        // Implementation for CollectingCentreDepositCancel
        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {
            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.CollectingCentreDepositCancel);
            agentHistory.setCompanyTransactionValue(0);
            agentHistory.setAgentTransactionValue(transactionValue);
            agentHistory.setStaffTrasnactionValue(0);
            agentHistory.setTransactionValue(0 - Math.abs(transactionValue));
            agentHistory.setPaidAmountByAgency(transactionValue);
            agentHistory.setComment(comments);

            double balanceBeforeTx = collectingCentre.getBallance();

            double balanceAfterTx = balanceBeforeTx + collectingCentreFee;

            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.create(agentHistory);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);

        } finally {
            lock.unlock();
        }
    }

    private void handleCollectingCentreBilling(Institution collectingCentre,
            double hospitalFee,
            double collectingCentreFee,
            double staffFee,
            double transactionValue,
            Bill bill) {
        Long collectingCentreId = collectingCentre.getId(); // Assuming each Institution has a unique ID
        Lock lock = lockMap.computeIfAbsent(collectingCentreId, id -> new ReentrantLock());
        lock.lock();
        try {
            collectingCentre = institutionFacade.findWithoutCache(collectingCentre.getId());
            AgentHistory agentHistory = new AgentHistory();
            agentHistory.setCreatedAt(new Date());
            agentHistory.setCreater(bill.getCreater());
            agentHistory.setBill(bill);
            agentHistory.setInstitution(bill.getInstitution());
            agentHistory.setDepartment(bill.getDepartment());
            agentHistory.setAgency(collectingCentre);
            agentHistory.setReferenceNumber(bill.getAgentRefNo());
            agentHistory.setHistoryType(HistoryType.CollectingCentreBilling);
            agentHistory.setCompanyTransactionValue(hospitalFee);
            agentHistory.setAgentTransactionValue(collectingCentreFee);
            agentHistory.setStaffTrasnactionValue(staffFee);
            //agentHistory.setTransactionValue(transactionValue);
            agentHistory.setTransactionValue(0 - Math.abs(hospitalFee));

            double balanceBeforeTx = collectingCentre.getBallance();

            double balanceAfterTx = balanceBeforeTx - hospitalFee;

            agentHistory.setBalanceBeforeTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceBeforeTx)
            );
            agentHistory.setBalanceAfterTransaction(
                    CommonFunctions.roundToTwoDecimalsBigDecimal(balanceAfterTx)
            );

            agentHistoryFacade.create(agentHistory);

            collectingCentre.setBallance(balanceAfterTx);
            institutionFacade.editAndCommit(collectingCentre);

        } finally {
            lock.unlock();
        }
    }

}
