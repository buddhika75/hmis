package com.divudi.bean.lab;

import com.divudi.bean.common.EnumController;
import com.divudi.bean.common.SessionController;
import com.divudi.core.data.lab.TestHistoryType;
import com.divudi.core.entity.AppEmail;
import com.divudi.core.entity.Category;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.Sms;
import com.divudi.core.entity.Staff;
import com.divudi.core.entity.lab.LabTestHistory;
import com.divudi.core.entity.lab.PatientInvestigation;
import com.divudi.core.entity.lab.PatientReport;
import com.divudi.core.entity.lab.PatientSample;
import com.divudi.core.entity.lab.PatientSampleComponant;
import com.divudi.core.facade.lab.LabTestHistoryFacade;
import com.divudi.core.util.JsfUtil;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import javax.faces.convert.Converter;

/**
 *
 * @author Dr M H B Ariyaratne <buddhika.ari@gmail.com> and H.K. Damith Deshan
 * <hkddrajapaksha@gmail.com>
 *
 */
@Named(value = "labTestHistoryController")
@SessionScoped
public class LabTestHistoryController implements Serializable {

    public LabTestHistoryController() {
    }

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private LabTestHistoryFacade facade;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    private SessionController sessionController;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Variables">
    private static final long serialVersionUID = 1L;
    private LabTestHistory current;
    private List<LabTestHistory> items = null;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Navigation Method">
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Function">
    // <editor-fold defaultstate="collapsed" desc="Billing">
    public void addBillingHistory(PatientInvestigation patientInvestigation, Department toDepartment) {
        addNewHistory(TestHistoryType.ORDERED, null, toDepartment, patientInvestigation, null, null, null, null, null, null, null, null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sample History">
    public void addBarcodeGenerateHistory(PatientInvestigation patientInvestigation, PatientSample patientSample) {
        addNewHistory(TestHistoryType.BARCODE_GENERATED, null, null, patientInvestigation, null, patientSample, null, null, null, null, null, null);
    }

    public void addSampleReGenerateHistory(PatientInvestigation patientInvestigation, PatientSample patientSample) {
        addNewHistory(TestHistoryType.BARCODE_REGENERATED, null, null, patientInvestigation, null, patientSample, null, null, null, null, null, null);
    }

    public void addSampleCollectHistory(PatientInvestigation patientInvestigation, PatientSample patientSample) {
        addNewHistory(TestHistoryType.SAMPLE_COLLECTED, null, null, patientInvestigation, null, patientSample, null, null, null, null, null, null);
    }

    public void addSampleReCollectHistory(PatientInvestigation patientInvestigation, PatientSample patientSample) {
        addNewHistory(TestHistoryType.SAMPLE_RECOLLECTED, null, null, patientInvestigation, null, patientSample, null, null, null, null, null, null);
    }

    public void addSampleSentHistory(PatientInvestigation patientInvestigation, PatientSample patientSample, Staff sampleTransporter) {
        if (sampleTransporter == null) {
            addNewHistory(TestHistoryType.SAMPLE_SENT, null, null, patientInvestigation, null, patientSample, null, null, null, null, null, null);
        } else {
            addNewHistory(TestHistoryType.SAMPLE_SENT, null, null, patientInvestigation, null, patientSample, sampleTransporter, null, null, null, null, null);
        }
    }

    public void addSampleOutLabSentHistory(PatientInvestigation patientInvestigation, PatientSample patientSample, Staff sampleTransporter, Department fromDepartment, Department toDepartment) {
        addNewHistory(TestHistoryType.SAMPLE_SENT_OUT_LAB, fromDepartment, toDepartment, patientInvestigation, null, patientSample, sampleTransporter, null, null, null, null, null);
    }

    public void addSampleReceiveHistory(PatientInvestigation patientInvestigation, PatientSample patientSample) {
        addNewHistory(TestHistoryType.SAMPLE_RECEIVED, null, null, patientInvestigation, null, patientSample, null, null, null, null, null, null);
    }

    public void addSampleRejectHistory(PatientInvestigation patientInvestigation, PatientSample patientSample, String comment) {
        addNewHistory(TestHistoryType.SAMPLE_REJECTED, null, null, patientInvestigation, null, patientSample, null, null, null, null, null, comment);
    }

    public void addSampleReCollectRequestHistory(PatientInvestigation patientInvestigation, PatientSample patientSample) {
        addNewHistory(TestHistoryType.SAMPLE_RECOLLECT_REQUEST, null, null, patientInvestigation, null, patientSample, null, null, null, null, null, null);
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Report History">
    public void addCreateReportHistory(PatientInvestigation patientInvestigation, PatientReport patientReport) {
        addNewHistory(TestHistoryType.REPORT_CREATED, null, null, patientInvestigation, patientReport, null, null, null, null, null, null, null);
    }

    public void addDataEnterHistory(PatientInvestigation patientInvestigation, PatientReport patientReport) {
        addNewHistory(TestHistoryType.DATA_ENTERED, null, null, patientInvestigation, patientReport, null, null, null, null, null, null, null);
    }

    public void addCalculateHistory(PatientInvestigation patientInvestigation, PatientReport patientReport) {
        addNewHistory(TestHistoryType.REPORT_CALCULATED, null, null, patientInvestigation, patientReport, null, null, null, null, null, null, null);
    }

    public void addApprovalHistory(PatientInvestigation patientInvestigation, PatientReport patientReport) {
        addNewHistory(TestHistoryType.REPORT_APPROVED, null, null, patientInvestigation, patientReport, null, null, null, null, null, null, null);
    }

    public void addApprovalCancelHistory(PatientInvestigation patientInvestigation, PatientReport patientReport) {
        addNewHistory(TestHistoryType.REPORT_APPROVED_CANCEL, null, null, patientInvestigation, patientReport, null, null, null, null, null, null, null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Report Sent">
    public void addReportSentSMSHistory(PatientInvestigation patientInvestigation, PatientReport patientReport, Sms sms) {
        addNewHistory(TestHistoryType.SENT_SMS_MANUAL, null, null, patientInvestigation, patientReport, null, null, sms, null, null, null, null);
    }

    public void addReportSentEmailHistory(PatientInvestigation patientInvestigation, PatientReport patientReport, AppEmail email) {
        addNewHistory(TestHistoryType.SENT_EMAIL, null, null, patientInvestigation, patientReport, null, null, null, email, null, null, null);
    }

    // </editor-fold>
    public void addNewHistory(
            TestHistoryType testHistoryType,
            Department fromDepartment,
            Department toDepartment,
            PatientInvestigation patientInvestigation,
            PatientReport patientReport,
            PatientSample patientSample,
            Staff staff,
            Sms sms,
            AppEmail email,
            PatientSampleComponant sampleComponant,
            Category analyzer,
            String comment
    ) {
        current = new LabTestHistory();
        current.setTestHistoryType(testHistoryType);
        current.setFromDepartment(fromDepartment);
        current.setToDepartment(toDepartment);
        current.setPatientInvestigation(patientInvestigation);
        current.setPatientReport(patientReport);
        current.setPatientSample(patientSample);
        current.setStaff(staff);
        current.setSms(sms);
        current.setEmail(email);
        current.setSampleComponant(sampleComponant);
        current.setAnalyzer(analyzer);
        current.setComment(comment);

        save();

    }

    public void save() {
        if (current == null) {
            return;
        }
        try {
            if (current.getId() != null) {
                getFacade().edit(current);
                //JsfUtil.addSuccessMessage("Updated Successfully.");
            } else {
                current.setInstitution(sessionController.getInstitution());
                current.setDepartment(sessionController.getDepartment());
                current.setCreatedAt(new Date());
                current.setCreatedBy(getSessionController().getLoggedUser());
                getFacade().create(current);
                //JsfUtil.addSuccessMessage("Saved Successfully");
            }
            items = null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Error saving: " + e.getMessage());
        }
    }
    @Inject
    EnumController enumController;

    public List<LabTestHistoryLight> getCreatedLabTestHistoryByInvestigation(PatientInvestigation patientInvestigation) {
        if (patientInvestigation == null) {
            return null;
        }

        String jpql = "SELECT new com.divudi.bean.lab.LabTestHistoryLight(his.id, his.testHistoryType, his.createdAt, his.institution.name, his.department.name, his.staff, his.createdBy, his.comment) "
                + " FROM LabTestHistory his "
                + " WHERE his.retired=:retired "
                + " AND his.patientInvestigation =:patientInvestigation"
                + " AND his.testHistoryType =:type"
                + " order by his.createdAt asc";
        Map<String, Object> params = new HashMap<>();
        params.put("retired", false);
        params.put("patientInvestigation", patientInvestigation);
        params.put("type", TestHistoryType.ORDERED);
        List<LabTestHistoryLight> labHistory = (List<LabTestHistoryLight>) getFacade().findLightsByJpql(jpql, params);
        return labHistory;
    }

    public List<LabTestHistoryLight> getReportLabTestHistoryByInvestigation(PatientInvestigation patientInvestigation) {
        if (patientInvestigation == null) {
            return null;
        }

        String jpql = "SELECT new com.divudi.bean.lab.LabTestHistoryLight(his.id, his.testHistoryType, his.createdAt, his.institution.name, his.department.name, his.staff, his.createdBy, his.comment) "
                + " FROM LabTestHistory his "
                + " WHERE his.retired=:retired "
                + " AND his.patientInvestigation=:patientInvestigation"
                + " AND his.testHistoryType In :types"
                + " order by his.createdAt asc";
        Map<String, Object> params = new HashMap<>();
        
        List<TestHistoryType> reportedTypes = new ArrayList<>();
        reportedTypes.add(TestHistoryType.REPORT_CREATED);
        reportedTypes.add(TestHistoryType.DATA_ENTERED);
        reportedTypes.add(TestHistoryType.REPORT_UPLOADED);
        reportedTypes.add(TestHistoryType.REMOVED_UPLOADED_REPORT);
        reportedTypes.add(TestHistoryType.REPORT_CALCULATED);
        reportedTypes.add(TestHistoryType.REPORT_APPROVED);
        reportedTypes.add(TestHistoryType.REPORT_APPROVED_CANCEL);
        reportedTypes.add(TestHistoryType.REPORT_VIEWED);
        reportedTypes.add(TestHistoryType.REPORT_PRINTED);
        reportedTypes.add(TestHistoryType.SENT_SMS_MANUAL);
        reportedTypes.add(TestHistoryType.SENT_SMS_AUTO);
        reportedTypes.add(TestHistoryType.SENT_EMAIL);

        params.put("retired", false);
        params.put("patientInvestigation", patientInvestigation);
        params.put("types", reportedTypes);
        List<LabTestHistoryLight> labHistory = (List<LabTestHistoryLight>) getFacade().findLightsByJpql(jpql, params);
        return labHistory;
    }

    public List<LabTestHistoryLight> getLabTestHistoryByInvestigation(PatientInvestigation patientInvestigation, PatientSample patientSample) {

        if (patientInvestigation == null) {
            return null;
        }
        if (patientSample == null) {
            return null;
        }

        String jpql = "SELECT new com.divudi.bean.lab.LabTestHistoryLight(his.id, his.testHistoryType, his.createdAt, his.institution.name, his.department.name, his.staff, his.createdBy, his.comment) "
                + " FROM LabTestHistory his "
                + " WHERE his.retired=:retired "
                + " AND his.patientInvestigation=:patientInvestigation"
                + " AND his.patientSample =:ps"
                + " order by his.createdAt asc";
        Map<String, Object> params = new HashMap<>();
        params.put("retired", false);
        params.put("patientInvestigation", patientInvestigation);
        params.put("ps", patientSample);
        List<LabTestHistoryLight> labHistory = (List<LabTestHistoryLight>) getFacade().findLightsByJpql(jpql, params);
        return labHistory;
    }

    public Long getLabTestHistoryCountByInvestigation(PatientInvestigation patientInvestigation, String labTestHistory) {
        TestHistoryType historyType = enumController.getLabTestHistory(labTestHistory);

        if (historyType == null || patientInvestigation == null) {
            return 0L;
        }

        String jpql = "SELECT COUNT(his.id) "
                + "FROM LabTestHistory his "
                + "WHERE his.retired = :retired "
                + "AND his.testHistoryType = :type "
                + "AND his.patientInvestigation = :patientInvestigation";

        Map<String, Object> params = new HashMap<>();
        params.put("type", historyType);
        params.put("retired", false);
        params.put("patientInvestigation", patientInvestigation);

        return getFacade().findLongByJpql(jpql, params);
    }

    @FacesConverter(forClass = LabTestHistory.class)
    public static class LabTestHistoryConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            LabTestHistoryController controller = (LabTestHistoryController) fc.getApplication()
                    .getELResolver().getValue(fc.getELContext(), null, "labTestHistoryController");
            return controller.getFacade().find(Long.valueOf(value));
        }

        @Override
        public String getAsString(FacesContext fc, UIComponent uic, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof LabTestHistory) {
                LabTestHistory lth = (LabTestHistory) object;
                return lth.getId() == null ? "" : lth.getId().toString();
            } else {
                throw new IllegalArgumentException("Unexpected object type");
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getter & Setter">
    public LabTestHistory getCurrent() {
        if (current == null) {
            current = new LabTestHistory();
        }
        return current;
    }

    public void setCurrent(LabTestHistory current) {
        this.current = current;
    }

    public List<LabTestHistory> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    public LabTestHistoryFacade getFacade() {
        return facade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }
    // </editor-fold>

}
