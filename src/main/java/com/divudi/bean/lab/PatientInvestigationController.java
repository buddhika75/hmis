package com.divudi.bean.lab;

import com.divudi.bean.common.*;

import com.divudi.bean.report.InstitutionLabSumeryController;
import com.divudi.core.data.InvestigationItemType;
import com.divudi.core.data.InvestigationReportType;
import com.divudi.core.data.ItemType;
import com.divudi.core.data.MessageType;
import com.divudi.core.data.lab.Dimension;
import com.divudi.core.data.lab.DimensionTestResult;
import com.divudi.core.data.lab.SampleRequestType;
import com.divudi.core.data.lab.SysMexOld;
import com.divudi.core.data.lab.SysMexAdf1;
import com.divudi.core.data.lab.SysMexAdf2;
import com.divudi.core.data.reports.LaboratoryReport;
import com.divudi.ejb.SmsManagerEjb;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillComponent;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.Patient;
import com.divudi.core.entity.Sms;
import com.divudi.core.entity.UserPreference;
import com.divudi.core.entity.lab.Investigation;
import com.divudi.core.entity.lab.InvestigationItem;
import com.divudi.core.entity.lab.PatientInvestigation;
import com.divudi.core.entity.lab.PatientReport;
import com.divudi.core.entity.lab.PatientReportItemValue;
import com.divudi.core.entity.lab.PatientSample;
import com.divudi.core.entity.lab.PatientSampleComponant;
import com.divudi.core.entity.lab.ReportItem;
import com.divudi.core.facade.BillFacade;
import com.divudi.core.facade.BillItemFacade;
import com.divudi.core.facade.InvestigationFacade;
import com.divudi.core.facade.InvestigationItemFacade;
import com.divudi.core.facade.ItemFacade;
import com.divudi.core.facade.PatientInvestigationFacade;
import com.divudi.core.facade.PatientReportFacade;
import com.divudi.core.facade.PatientReportItemValueFacade;
import com.divudi.core.facade.PatientSampleComponantFacade;
import com.divudi.core.facade.PatientSampleFacade;
import com.divudi.core.facade.ReportItemFacade;
import com.divudi.core.facade.SmsFacade;
import com.divudi.core.util.JsfUtil;
import com.divudi.core.data.BillTypeAtomic;
import com.divudi.core.data.InvestigationItemValueType;
import com.divudi.core.data.dto.SampleDTO;
import com.divudi.core.data.lab.BillBarcode;
import com.divudi.core.data.lab.ListingEntity;
import com.divudi.core.data.lab.PatientInvestigationStatus;
import com.divudi.core.data.lab.PatientInvestigationWrapper;
import com.divudi.core.data.lab.PatientSampleWrapper;
import com.divudi.core.data.lab.Priority;
import com.divudi.core.data.lab.SampleTubeLabel;
import com.divudi.core.data.lab.SearchDateType;
import com.divudi.core.entity.Institution;
import com.divudi.core.entity.Route;
import com.divudi.core.entity.Staff;
import com.divudi.core.entity.WebUser;
import com.divudi.core.entity.lab.InvestigationTube;
import com.divudi.core.entity.lab.Machine;
import com.divudi.core.entity.lab.Sample;
import com.divudi.core.util.CommonFunctions;
import com.divudi.ws.lims.Lims;
import com.divudi.ws.lims.LimsMiddlewareController;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, MSc, MD(Health Informatics) Acting
 * Consultant (Health Informatics)
 */
@Named
@SessionScoped
public class PatientInvestigationController implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * EJBs
     */
    @EJB
    private PatientInvestigationFacade ejbFacade;
    @EJB
    PatientReportFacade prFacade;
    @EJB
    InvestigationItemFacade investigationItemFacade;
    @EJB
    private InvestigationFacade investFacade;
    @EJB
    SmsFacade smsFacade;
    @EJB
    BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private PatientSampleFacade patientSampleFacade;
    @EJB
    private PatientSampleComponantFacade patientSampleComponantFacade;
    @EJB
    private ItemFacade itemFacade;
    @EJB
    private SmsManagerEjb smsManagerEjb;
    @EJB
    private PatientReportItemValueFacade patientReportItemValueFacade;
    @EJB
    PatientReportFacade patientReportFacade;
    @EJB
    private ReportTimerController reportTimerController;
    /*
     * Controllers
     */
    @Inject
    ItemController itemController;
    @Inject
    InvestigationTubeController investigationTubeController;
    @Inject
    private InstitutionLabSumeryController labReportSearchByInstitutionController;
    @Inject
    SessionController sessionController;
    @Inject
    private BillBeanController billBeanController;
    @Inject
    private InvestigationItemController investigationItemController;
    @Inject
    private BillController billController;
    @Inject
    private PatientReportController patientReportController;
    @Inject
    private ItemForItemController itemForItemController;
    @Inject
    private SmsController smsController;
    @Inject
    private Lims lims;
    @Inject
    private ConfigOptionApplicationController configOptionApplicationController;
    @Inject
    LaboratoryManagementController laboratoryManagementController;
    @Inject
    LabTestHistoryController labTestHistoryController;
    @Inject
    LimsMiddlewareController limsMiddlewareController;

    /**
     * Class Variables
     */
    List<PatientInvestigation> selectedItems;
    private PatientInvestigation current;
    Investigation currentInvestigation;

    List<InvestigationItem> currentInvestigationItems;
    private List<PatientInvestigation> items = null;
    private List<Bill> bills = null;
    private List<BillItem> billItems = null;
    private List<PatientReport> patientReports = null;
    private List<PatientReport> selectedPatientReports = null;
    private List<Bill> selectedBills = null;
    private List<PatientInvestigation> lstToSamle = null;
    private List<PatientInvestigation> lstToReceive = null;
    private List<PatientInvestigation> lstToEnterData = null;
    private List<PatientReport> lstToApprove = null;
    private List<PatientReport> lstToPrint = null;
    private List<PatientInvestigation> lstForSampleManagement = null;
    List<PatientSample> patientSamples;
    private List<PatientSample> selectedPatientSamples;
    private Staff sampleTransportedToLabByStaff;
    String selectText = "";
    private Institution orderedInstitution;
    private Department orderedDepartment;
    private Institution performingInstitution;
    private Department performingDepartment;
    private Institution collectionCenter;
    private Route route;
    private Priority priority;
    private Sample speciman;
    private Long sampleId;
    // Range filters for large queries
    private Long fromId;
    private Long toId;
    private String patientName;
    private Sample specimen;
    private String type;
    private String externalDoctor;
    private Machine equipment;
    private Staff referringDoctor;
    private Investigation investigation;
    private String investigationName;
    private String itemName;
    private Department department;
    private SearchDateType searchDateType;
    private PatientInvestigationStatus patientInvestigationStatus;
    @Temporal(TemporalType.TIME)
    private Date fromDate;
    Date toDate;
    private String sampleRejectionComment;

    private int activeIndexOfManageInvestigation;

    boolean showSamplingPagination;
    List<Investigation> investSummery;
    Date sampledOutsideDate;
    boolean sampledOutSide;
    boolean listIncludingReceived;
    boolean listIncludingEnteredData;
    private boolean listIncludingSampled;
    boolean listIncludingApproved;
    List<PatientInvestigation> selectedToReceive;

    Sms sms;
    private Set<PatientSample> patientSamplesSet;

    private String samplingRequestResponse;
    private PatientSample currentPatientSample;
    private String inputBillId;
    private String username;
    private String password;

    private String msg;
    private String machine;
    private String shift;
    private String shift1;
    private String shift2;

    private String apiResponse = "#{success=false|msg=No Requests}";

    private List<PatientReportItemValue> patientReportItemValues;

    private List<SampleTubeLabel> sampleTubeLabels;

    private List<BillBarcode> billBarcodes;
    private List<BillBarcode> selectedBillBarcodes;

    private Double hospitalFeeTotal;
    private Double ccFeeTotal;
    private Double staffFeeTotal;
    private Double grossFeeTotal;
    private Double discountTotal;
    private Double netTotal;

    private ListingEntity listingEntity;
    private Institution site;
    private Institution orderedSite;
    private Institution peformedSite;

    private List<Item> itemsForParentItem;
    private List<PatientSampleComponant> patientSampleComponentsByInvestigation;
    private PatientInvestigation currentPI;

    private boolean printIndividualBarcodes;
    private Bill currentBill;

    private int number;

    private Institution outLabInstitution;
    private Department outLabDepartment;

    private String sampleSearchStrategy;
    private boolean requestReCollected;

    private String testDetails;

    public int getNumber() {
        return number;
    }

    public void increment() {
        number++;
    }

    public String sampleComponentNames(PatientSample ps) {
        List<PatientSampleComponant> pscList = getPatientSampleComponentsByPatientSample(ps);
        String sampleComponentName = "";
        for (PatientSampleComponant psc : pscList) {
            sampleComponentName += psc.getNameTranscient();
            sampleComponentName += "  ";
        }
        return sampleComponentName;
    }

    public String navigateToPrintBarcodeFromMenu() {
        return "/lab/sample_barcode_printing?faces-redirect=true";
    }

    public String navigateToTestHistory() {
        return "/lab/lab_test_history?faces-redirect=true";
    }

    public String navigateToLabBillItemList() {
        clearFilters();
        return "/reports/lab/lab_bill_item_list?faces-redirect=true";
    }

    public String navigateToPatientSampelIndex() {
        return "/lab/sample_index?faces-redirect=true";
    }

    public void sendToOutLabSample() {
        listingEntity = ListingEntity.PATIENT_SAMPLES;

        if (outLabInstitution == null) {
            JsfUtil.addErrorMessage("No Added Out Lab Institution");
            return;
        }

        if (outLabDepartment == null) {
            JsfUtil.addErrorMessage("No Added Out Lab Department");
            return;
        }

        if (selectedPatientSamples == null || selectedPatientSamples.isEmpty()) {
            JsfUtil.addErrorMessage("No samples selected");
            return;
        }

        if (sampleTransportedToLabByStaff == null) {
            JsfUtil.addErrorMessage("Samples Transporter is Empty");
            return;
        }

        List<PatientSample> canSentOutLabSamples = new ArrayList<>();
        for (PatientSample ps : selectedPatientSamples) {
            if (ps.getBill().isCancelled()) {
                JsfUtil.addErrorMessage("This Bill is Already Cancel");
                return;
            }

            if (ps.getDepartment() != sessionController.getDepartment()) {
                JsfUtil.addErrorMessage("Sample (" + ps.getId() + ") belongs to " + ps.getDepartment().getName() + " department. You cannot process samples from other departments.");
                return;
            }
            if (ps.getStatus() == PatientInvestigationStatus.SAMPLE_SENT_TO_OUTLAB) {
                JsfUtil.addErrorMessage("This Sample (" + ps.getId() + ") is Already Sent OutLab");
                return;
            }
            if (ps.getStatus() == PatientInvestigationStatus.SAMPLE_COLLECTED) {
                canSentOutLabSamples.add(ps);
            }
        }

        if (canSentOutLabSamples.isEmpty()) {
            JsfUtil.addErrorMessage("There are no suitable samples to send from the selected samples.");
            return;
        }

        Map<Long, PatientInvestigation> samplePtixs = new HashMap<>();
        Map<Long, Bill> sampleBills = new HashMap<>();

        // Process each selected patient sample
        for (PatientSample ps : canSentOutLabSamples) {
            ps.setOutsourced(true);
            ps.setOutsourceSampleTransporter(sampleTransportedToLabByStaff);
            ps.setOutsourceInstitution(outLabInstitution);
            ps.setOutsourceDepartment(outLabDepartment);
            ps.setOutsourceSentUser(sessionController.getLoggedUser());
            ps.setOutsourcedAt(new Date());
            ps.setInstitution(outLabInstitution);
            ps.setDepartment(outLabDepartment);
            ps.setStatus(PatientInvestigationStatus.SAMPLE_SENT_TO_OUTLAB);
            patientSampleFacade.edit(ps);

            // Retrieve and store PatientInvestigations by unique ID to avoid duplicates
            for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                samplePtixs.putIfAbsent(pi.getId(), pi);
            }
        }

        // Update PatientInvestigations and store associated Bills by unique ID to avoid duplicates
        for (PatientInvestigation tptix : samplePtixs.values()) {
            tptix.setSampleSent(true);
            tptix.setSampleTransportedToLabByStaff(sampleTransportedToLabByStaff);
            tptix.setSampleSentAt(new Date());
            tptix.setSampleSentBy(sessionController.getLoggedUser());

            tptix.setOutsourced(true);
            tptix.setOutsourcedInstitution(outLabInstitution);
            tptix.setOutsourcedDepartment(outLabDepartment);
            tptix.setOutsourcedUser(sessionController.getLoggedUser());
            tptix.setOutsourcedAt(new Date());

            tptix.setStatus(PatientInvestigationStatus.SAMPLE_SENT_TO_OUTLAB);
            getFacade().edit(tptix);
            sampleBills.putIfAbsent(tptix.getBillItem().getBill().getId(), tptix.getBillItem().getBill());
        }

        // Update Bills
        for (Bill tb : sampleBills.values()) {
            tb.setStatus(PatientInvestigationStatus.SAMPLE_SENT_TO_OUTLAB);
            billFacade.edit(tb);
        }

        if (configOptionApplicationController.getBooleanValueByKey("Lab Test History Enabled", false)) {
            for (PatientSample ps : canSentOutLabSamples) {
                for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                    labTestHistoryController.addSampleOutLabSentHistory(pi, ps, sampleTransportedToLabByStaff, sessionController.getDepartment(), outLabDepartment);
                }
            }
        }

        JsfUtil.addSuccessMessage("Selected Samples Sent to OutLab");

        outLabInstitution = null;
        outLabDepartment = null;
        sampleTransportedToLabByStaff = null;
    }

    public String navigateToAlternativeReportSelector(PatientInvestigation patientInvestigation) {
        currentPI = patientInvestigation;
        itemsForParentItem = new ArrayList<>();
        patientSampleComponentsByInvestigation = new ArrayList<>();
        itemsForParentItem = itemForItemController.getItemsForParentItem(patientInvestigation.getInvestigation());
        patientSampleComponentsByInvestigation = getPatientSampleComponentsByInvestigation(patientInvestigation);
        return "/lab/alternative_report_selector?faces-redirect=true";
    }

    public String navigateToCreatePatientReportBypassingSampleProcess() {
        if (current == null || current.getBillItem() == null || current.getBillItem().getBill() == null) {
            JsfUtil.addErrorMessage("No patient investigation selected");
            return "";
        }

        Bill selectedBill = current.getBillItem().getBill();
        List<PatientReport> billReport = patientReportController.patientReports(current);

        if (billReport == null || billReport.isEmpty()) {
            generateBarcodesForSelectedBill(selectedBill);
            List<PatientSample> investigationSmples = getPatientSamplesByInvestigation(current);
            recordSampleSendingProcess(selectedBill, current, investigationSmples);
            patientReportController.createNewReport(current);
        } else {
            patientReportController.setCurrentPatientReport(billReport.get(0));
        }
        return "/lab/patient_report_without_sample_sending_process?faces-redirect=true";
    }

    public void recordSampleSendingProcess(Bill bill, PatientInvestigation patientInvestigation, List<PatientSample> processingSamples) {
        if (bill == null || patientInvestigation == null || processingSamples == null) {
            JsfUtil.addErrorMessage("Invalid parameters for recording sample sending process");
            return;
        }

        List<PatientSample> canProcessSamples = new ArrayList<>();
        for (PatientSample ps : processingSamples) {
            if (ps.getBill().isCancelled()) {
                JsfUtil.addErrorMessage("This Bill is Already Cancel");
                continue;
            }
            if (ps.getStatus() == PatientInvestigationStatus.SAMPLE_GENERATED) {
                canProcessSamples.add(ps);
            }
        }

        if (canProcessSamples.isEmpty()) {
            JsfUtil.addErrorMessage("No valid samples to process");
            return;
        }

        //Update Sample Data
        for (PatientSample ps : canProcessSamples) {

            ps.setSampleCollected(true);
            ps.setSampleCollectedAt(new Date());
            ps.setSampleCollectedDepartment(sessionController.getDepartment());
            ps.setSampleCollectedInstitution(sessionController.getInstitution());
            ps.setSampleCollecter(sessionController.getLoggedUser());

            ps.setSampleSent(true);
            ps.setSampleSentBy(sessionController.getLoggedUser());
            ps.setSampleSentAt(new Date());
            ps.setSampleSentToInstitution(sessionController.getInstitution());
            ps.setSampleSentToDepartment(sessionController.getDepartment());
            ps.setSampleTransportedToLabByStaff(sessionController.getLoggedUser().getStaff());

            ps.setSampleReceivedAtLab(true);
            ps.setSampleReceiverAtLab(sessionController.getLoggedUser());
            ps.setSampleReceivedAtLabDepartment(sessionController.getDepartment());
            ps.setSampleReceivedAtLabInstitution(sessionController.getInstitution());
            ps.setSampleReceivedAtLabAt(new Date());

            ps.setDepartment(sessionController.getDepartment());
            ps.setStatus(PatientInvestigationStatus.SAMPLE_ACCEPTED);
            patientSampleFacade.edit(ps);
        }

        //Update PatientInvestigation Data
        patientInvestigation.setSampleCollected(true);
        patientInvestigation.setSampleCollectedAt(new Date());
        patientInvestigation.setSampleCollectedBy(sessionController.getLoggedUser());

        patientInvestigation.setSampleSent(true);
        patientInvestigation.setSampleTransportedToLabByStaff(sessionController.getLoggedUser().getStaff());
        patientInvestigation.setSampleSentAt(new Date());
        patientInvestigation.setSampleSentBy(sessionController.getLoggedUser());

        patientInvestigation.setSampleAccepted(true);
        patientInvestigation.setSampleAcceptedAt(new Date());
        patientInvestigation.setSampleAcceptedBy(sessionController.getLoggedUser());
        patientInvestigation.setReceived(true);
        patientInvestigation.setReceivedAt(new Date());
        patientInvestigation.setReceiveDepartment(sessionController.getDepartment());
        patientInvestigation.setReceiveInstitution(sessionController.getInstitution());
        patientInvestigation.setStatus(PatientInvestigationStatus.SAMPLE_ACCEPTED);
        ejbFacade.edit(patientInvestigation);

        //Update Bill Data
        bill.setStatus(PatientInvestigationStatus.SAMPLE_ACCEPTED);
        billFacade.edit(bill);

    }

    public String navigateToSampleManagementFromOPDBatchBillView(Bill bill) {

        String jpql;
        Map<String, Object> params = new HashMap<>();
        jpql = "SELECT pi.billItem.bill "
                + " FROM PatientInvestigation pi"
                + " WHERE pi.billItem.bill.retired = :ret"
                + " and pi.billItem.bill =:b"
                + " GROUP BY pi.billItem.bill ";

        params.put("b", bill);
        params.put("ret", false);

        if (configOptionApplicationController.getBooleanValueByKey("The system uses the Laboratory Dashboard as its default interface", false)) {
            laboratoryManagementController.setListingEntity(ListingEntity.BILLS);
            laboratoryManagementController.setBills(billFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP));
            return "/lab/laboratory_management_dashboard?faces-redirect=true";
        } else {
            listingEntity = ListingEntity.BILLS;
            bills = billFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);
            return "/lab/generate_barcode_p?faces-redirect=true";
        }
    }

    public String navigateToPrintBarcodesFromSampellingPage(PatientInvestigation ptIx) {
        if (ptIx == null) {
            JsfUtil.addErrorMessage("Patient Investigation is NOT selected");
            return null;
        }
        if (ptIx.getBillItem() == null) {
            JsfUtil.addErrorMessage("No Bill Item");
            return null;
        }
        if (ptIx.getBillItem().getBill() == null) {
            JsfUtil.addErrorMessage("No Bill");
            return null;
        }
        if (ptIx.getBillItem().getBill().getIdStr() == null) {
            JsfUtil.addErrorMessage("No Bill id sTRING");
            return null;
        }

        inputBillId = ptIx.getBillItem().getBill().getIdStr();
        prepareSampleCollection();
        return "/lab/sample_barcode_printing";
    }

    public void fillPatientReportItemValues() {
        fillPatientReportItemValues(fromId, toId, fromDate, toDate);
    }

    public void fillPatientReportItemValues(Long fromId, Long toId, Date fromDate, Date toDate) {
        StringBuilder j = new StringBuilder("select v from PatientReportItemValue v "
                + "where v.patientReport.approved=:app "
                + " and (v.investigationItem.ixItemType=:vtv or v.investigationItem.ixItemType=:vtc)"
                + " and v.patientReport.patientInvestigation.investigation.reportType=:rt");

        Map<String, Object> m = new HashMap<>();
        m.put("app", true);
        m.put("vtv", InvestigationItemType.Value);
        m.put("vtc", InvestigationItemType.Calculation);
        m.put("rt", InvestigationReportType.General);

        if (fromId != null) {
            j.append(" and v.id >= :fromId");
            m.put("fromId", fromId);
        }
        if (toId != null) {
            j.append(" and v.id <= :toId");
            m.put("toId", toId);
        }
        if (fromDate != null) {
            j.append(" and v.patientReport.approveAt >= :fd");
            m.put("fd", fromDate);
        }
        if (toDate != null) {
            j.append(" and v.patientReport.approveAt <= :td");
            m.put("td", toDate);
        }

        j.append(" order by v.id");

        patientReportItemValues = getPatientReportItemValueFacade().findByJpql(j.toString(), m, TemporalType.TIMESTAMP);
    }

    public void sentRequestToAnalyzer() {
        if (currentPatientSample == null) {
            JsfUtil.addErrorMessage("Nothing to Add");
            return;
        }
        currentPatientSample.setReadyTosentToAnalyzer(true);
        currentPatientSample.setSentToAnalyzer(false);
        currentPatientSample.setSampleRequestType(SampleRequestType.A);
        getPatientSampleFacade().edit(currentPatientSample);
    }

    public void stopSendingRequestToAnalyzer() {
        if (currentPatientSample == null) {
            JsfUtil.addErrorMessage("Nothing to Add");
            return;
        }
        currentPatientSample.setReadyTosentToAnalyzer(false);
        currentPatientSample.setSentToAnalyzer(false);
        currentPatientSample.setSampleRequestType(SampleRequestType.A);
        getPatientSampleFacade().edit(currentPatientSample);
    }

    public void sentRequestToDeleteToAnalyzer() {
        if (currentPatientSample == null) {
            JsfUtil.addErrorMessage("Nothing to Delete");
            return;
        }
        currentPatientSample.setReadyTosentToAnalyzer(true);
        currentPatientSample.setSentToAnalyzer(false);
        currentPatientSample.setSampleRequestType(SampleRequestType.D);
        getPatientSampleFacade().edit(currentPatientSample);
    }

    public void msgFromMiddleware() {
        apiResponse = "";
        if (username == null || username.trim().isEmpty()) {
            apiResponse += "#{success=false|msg=No Username}";
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            apiResponse += "#{success=false|msg=No Password}";
            return;
        }
        if (machine == null || machine.trim().isEmpty()) {
            apiResponse += "#{success=false|msg=No Machine Specified}";
            return;
        }
        if (msg == null || msg.trim().isEmpty()) {
            apiResponse += "#{success=false|msg=No Request From Analyzer}";
            return;
        }
        if (!sessionController.loginForRequests(username, password)) {
            apiResponse += "#{success=false|msg=Wrong username/password}";
            return;
        }
        if (machine.trim().equals("SysMex")) {
            apiResponse = msgFromSysmex();
            return;
        } else if (machine.trim().equals("Dimension")) {
            apiResponse = msgFromDimension();
            return;
        }
    }

    private String msgFromDimension() {
        String temMsgs = "";
        Dimension dim = new Dimension();
        dim.setInputStringBytesSpaceSeperated(msg);
        dim.analyzeReceivedMessage();

        if (dim.getAnalyzerMessageType() == com.divudi.core.data.lab.MessageType.Poll) {
            PatientSample nps = nextPatientSampleToSendToDimension();
            dim.setLimsPatientSample(nps);
            if (nps == null) {
                dim.setLimsFoundPatientInvestigationToEnterResults(false);
            } else {
                if (nps.getSampleRequestType() == null || nps.getSampleRequestType() == SampleRequestType.D) {
                    dim.setToDeleteSampleRequest(true);
                } else {
                    dim.setToDeleteSampleRequest(false);
                }
                dim.setLimsFoundPatientInvestigationToEnterResults(true);
                dim.setLimsPatientSampleComponants(getPatientSampleComponents(nps));
            }
            dim.prepareResponseForPollMessages();
        } else if (dim.getAnalyzerMessageType() == com.divudi.core.data.lab.MessageType.QueryMessage) {
            PatientSample nps = patientSampleFromId(dim.getAnalyzerSampleId());

            dim.setLimsPatientSample(nps);
            if (nps == null) {
                dim.setLimsFoundPatientInvestigationToEnterResults(false);
            } else {
                dim.setToDeleteSampleRequest(false);
                dim.setLimsFoundPatientInvestigationToEnterResults(true);
                dim.setLimsPatientSampleComponants(getPatientSampleComponents(nps));
            }
            dim.prepareResponseForQueryMessages();
        } else if (dim.getAnalyzerMessageType() == com.divudi.core.data.lab.MessageType.ResultMessage) {
            boolean resultAdded = true;
            for (DimensionTestResult r : dim.getAnalyzerTestResults()) {
                boolean tb = addResultsToReportsForDimension(dim.getAnalyzerSampleId(), r.getTestName(), r.getTestResult(), r.getTestUnit(), r.getErrorCode());
                if (!tb) {
                    resultAdded = false;
                }
            }
            temMsgs = "Results Added " + resultAdded;
            dim.setLimsFoundPatientInvestigationToEnterResults(resultAdded);
            dim.prepareResponseForResultMessages();

        } else if (dim.getAnalyzerMessageType() == com.divudi.core.data.lab.MessageType.CaliberationResultMessage) {
            dim.prepareResponseForCaliberationResultMessages();
        } else if (dim.getAnalyzerMessageType() == com.divudi.core.data.lab.MessageType.RequestAcceptance) {
            temMsgs = "#{success=true|}";
            return temMsgs;
        } else {
            temMsgs = "#{success=true|}";
            return temMsgs;
        }

        dim.createResponseString();
        temMsgs = dim.getResponseString();
        temMsgs = "#{success=true|toAnalyzer=" + temMsgs + "}";
        return temMsgs;
    }

    public boolean addResultsToReportsForDimension(String sampleId, String testStr, String result, String unit, String error) {
        boolean temFlag = false;
        long sid;
        try {
            sid = Long.parseLong(sampleId);
        } catch (Exception e) {
            sid = 0L;
        }
        PatientSample ps = getPatientSampleFromId(sid);

        if (ps == null) {
            return temFlag;
        }

        List<PatientSampleComponant> pscs = getPatientSampleComponents(ps);

        if (pscs == null) {
            return temFlag;
        }
        List<PatientInvestigation> ptixs = getPatientInvestigations(pscs);
        if (ptixs == null || ptixs.isEmpty()) {
            return temFlag;
        }
        for (PatientInvestigation pi : ptixs) {
            List<PatientReport> prs = new ArrayList<>();
            if (pi.getInvestigation().getMachine() != null && pi.getInvestigation().getMachine().getName().toLowerCase().contains("dim")) {
                PatientReport tpr;
                tpr = patientReportController.getUnapprovedPatientReport(pi);
                if (tpr == null) {
                    tpr = patientReportController.createNewPatientReport(pi, pi.getInvestigation());
                }
                prs.add(tpr);
            }
            for (PatientReport rtpr : prs) {
                for (PatientReportItemValue priv : rtpr.getPatientReportItemValues()) {
                    if (priv.getInvestigationItem() != null && priv.getInvestigationItem().getTest() != null && priv.getInvestigationItem().getIxItemType() == InvestigationItemType.Value) {
                        String test;
                        test = priv.getInvestigationItem().getResultCode();

                        if (test == null || test.trim().isEmpty()) {
                            test = priv.getInvestigationItem().getTest().getCode().toUpperCase();
                        }

                        if (test.equalsIgnoreCase(testStr)) {
                            if (ps.getInvestigationComponant() == null || priv.getInvestigationItem().getSampleComponent() == null) {
                                priv.setStrValue(result);
                                double dbl = 0d;
                                try {
                                    dbl = Double.parseDouble(result);
                                } catch (Exception ignored) {
                                }
                                priv.setDoubleValue(dbl);
                                temFlag = true;
                            } else if (priv.getInvestigationItem().getSampleComponent().equals(ps.getInvestigationComponant())) {
                                priv.setStrValue(result);
                                double dbl = 0d;
                                try {
                                    dbl = Double.parseDouble(result);
                                } catch (Exception ignored) {
                                }
                                priv.setDoubleValue(dbl);
                                temFlag = true;
                            }
                        }
                    }
                }
                rtpr.setDataEntered(true);
                rtpr.setDataEntryAt(new Date());
                rtpr.setDataEntryComments("Initial Results were taken from Analyzer through Middleware");
                getPrFacade().edit(rtpr);
            }
        }

        return temFlag;
    }

    public PatientSample patientSampleFromId(String id) {
        long pid = 0L;
        try {
            pid = Long.parseLong(id);
        } catch (Exception ignored) {
        }
        return getPatientSampleFacade().find(pid);
    }

    public PatientSample nextPatientSampleToSendToDimension() {
        String j = "select ps from PatientSample ps "
                + "where ps.readyTosentToAnalyzer=true "
                + " and ps.sentToAnalyzer=false "
                + " and (ps.machine.name) like :ma ";

        Map m = new HashMap();
        m.put("ma", "%dimension%");
        PatientSample ps = getPatientSampleFacade().findFirstByJpql(j, m);
        if (ps != null) {
            ps.setReadyTosentToAnalyzer(false);
            ps.setSentToAnalyzer(true);
            getPatientSampleFacade().edit(ps);
        }
        return ps;
    }

    private String msgFromSysmex() {
        String temMsgs = "";
        SysMexOld sysMex = new SysMexOld();
        sysMex.setInputStringBytesSpaceSeperated(msg);

        if (sysMex.getBytes().size() > 189 && sysMex.getBytes().size() < 200) {
            SysMexAdf1 m1 = new SysMexAdf1();
            m1.setInputStringBytesSpaceSeperated(msg);
            if (m1.isCorrectReport()) {
                return "#{success=true|msg=Received Result Format 1 for sample ID " + m1.getSampleId() + "}";
            }
        } else if (sysMex.getBytes().size() > 253 && sysMex.getBytes().size() < 258) {
            SysMexAdf2 m2 = new SysMexAdf2();
            m2.setInputStringBytesSpaceSeperated(msg);
            if (m2.isCorrectReport()) {
                return "#{success=true|msg=Received Result Format 2 for sample ID " + m2.getSampleId() + "}";
            } else {
                return extractDataFromSysMexAdf2(m2);
            }
        }
        return "#{success=false|msg=Wrong Data Communication}";
    }

    public String extractDataFromSysMexAdf2(SysMexAdf2 adf2) {
        StringBuilder temMsgs = new StringBuilder();
        long sampleId = adf2.getSampleId();
        PatientSample ps = getPatientSampleFromId(adf2.getSampleId());
        if (ps == null) {
            return "#{success=false|msg=Wrong Sample ID. Please resent results " + sampleId + "}";
        }
        List<PatientSampleComponant> pscs = getPatientSampleComponents(ps);
        if (pscs == null) {
            return "#{success=false|msg=Wrong Sample Components. Please inform developers. Please resent results " + sampleId + "}";
        }
        List<PatientInvestigation> ptixs = getPatientInvestigations(pscs);
        if (ptixs == null || ptixs.isEmpty()) {
            return "#{success=false|msg=Wrong Patient Investigations. Please inform developers. Please resent results " + sampleId + "}";
        }
        for (PatientInvestigation pi : ptixs) {
            List<PatientReport> prs = new ArrayList<>();
            if (pi.getInvestigation().getMachine() != null && pi.getInvestigation().getMachine().getName().toLowerCase().contains("sysmex")) {
                PatientReport tpr = patientReportController.createNewPatientReport(pi, pi.getInvestigation());
                prs.add(tpr);
            }
            List<Item> temItems = itemForItemController.getItemsForParentItem(pi.getInvestigation());
            for (Item ti : temItems) {
                if (ti instanceof Investigation) {
                    Investigation tix = (Investigation) ti;
                    if (tix.getMachine() != null && tix.getMachine().getName().toLowerCase().contains("sysmex")) {
                        PatientReport tpr = patientReportController.createNewPatientReport(pi, tix);
                        prs.add(tpr);
                    }
                }
            }
            for (PatientReport tpr : prs) {

                for (PatientReportItemValue priv : tpr.getPatientReportItemValues()) {
                    if (priv.getInvestigationItem() != null && priv.getInvestigationItem().getTest() != null && priv.getInvestigationItem().getIxItemType() == InvestigationItemType.Value) {
                        String test = priv.getInvestigationItem().getTest().getCode().toUpperCase();
                        switch (test) {
                            case "WBC":
                                priv.setStrValue(adf2.getWbc());
                                break;
                            case "NEUT%":
                                priv.setStrValue(adf2.getNeutPercentage());
                                break;
                            case "LYMPH%":
                                priv.setStrValue(adf2.getLymphPercentage());
                                break;
                            case "BASO%":
                                priv.setStrValue(adf2.getBasoPercentage());
                                break;
                            case "MONO%":
                                priv.setStrValue(adf2.getMonoPercentage());
                                break;
                            case "EO%":
                                priv.setStrValue(adf2.getEoPercentage());
                                break;
                            case "RBC":
                                priv.setStrValue(adf2.getRbc());
                                break;
                            case "HGB":
                                priv.setStrValue(adf2.getHgb());
                                break;
                            case "HCT":
                                priv.setStrValue(adf2.getHct());
                                break;
                            case "MCV":
                                priv.setStrValue(adf2.getMcv());
                                break;
                            case "MCH":
                                priv.setStrValue(adf2.getMch());
                                break;
                            case "MCHC":
                                priv.setStrValue(adf2.getMchc());
                                break;
                            case "PLT":
                                priv.setStrValue(adf2.getPlt());
                                break;

                        }
                    }
                }
                tpr.setDataEntered(true);
                tpr.setDataEntryAt(new Date());
                tpr.setDataEntryComments("Initial Results were taken from Analyzer through Middleware");
                temMsgs.append("Patient = ").append(tpr.getPatientInvestigation().getBillItem().getBill().getPatient().getPerson().getNameWithTitle()).append("\n");
                temMsgs.append("Bill No = ").append(tpr.getPatientInvestigation().getBillItem().getBill().getInsId()).append("\n");
                temMsgs.append("Investigation = ").append(tpr.getPatientInvestigation().getInvestigation().getName()).append("\n");
                getPrFacade().edit(tpr);
            }
        }
        return "#{success=true|msg=Data Added to LIMS \n" + temMsgs + "}";
    }

    private PatientSample getPatientSampleFromId(Long id) {
        String j = "select ps from PatientSample ps where ps.id = :id";
        Map m = new HashMap();
        m.put("id", id);
        return getPatientSampleFacade().findFirstByJpql(j, m);
    }

    public List<PatientSampleComponant> getPatientSampleComponents(PatientSample ps) {
        String j = "select psc from PatientSampleComponant psc where psc.patientSample = :ps";
        Map m = new HashMap();
        m.put("ps", ps);
        return patientSampleComponantFacade.findByJpql(j, m);
    }

    public List<PatientSampleComponant> getPatientSampleComponentsUsingSampleId(Long sampleId) {
        if (sampleId == null || sampleId <= 0) {
            return Collections.emptyList();
        }

        String j = "select psc from PatientSampleComponant psc where psc.patientSample.id = :id";
        Map m = new HashMap();
        m.put("id", sampleId);
        return patientSampleComponantFacade.findByJpql(j, m);
    }

    public List<PatientInvestigation> getPatientInvestigationsFromSample(PatientSample ps) {
        String j = "select psc from PatientSampleComponant psc where psc.patientSample = :ps";

        Map m = new HashMap();
        m.put("ps", ps);
        return patientSampleComponantFacade.findByJpql(j, m);
    }

    private List<PatientInvestigation> getPatientInvestigations(List<PatientSampleComponant> pscs) {
        Set<PatientInvestigation> ptixhs = new HashSet<>();
        for (PatientSampleComponant psc : pscs) {
            ptixhs.add(psc.getPatientInvestigation());
        }
        return new ArrayList<>(ptixhs);
    }

    public void resetLists() {
        items = null;
        lstToSamle = null;
        lstToReceive = null;
        lstToEnterData = null;
        lstToApprove = null;
        lstToPrint = null;
        selectedToReceive = null;
        investSummery = null;
        lstToReceiveSearch = null;
        toReceive = null;
    }

    public PatientInvestigation getPatientInvestigationFromBillItem(BillItem bi) {
        String j;
        Map m = new HashMap();
        j = "select pi "
                + " from PatientInvestigation pi "
                + " where pi.billItem =:bi "
                + " and pi.retired=false "
                + " order by pi.id";
        m.put("bi", bi);
        PatientInvestigation pi = getFacade().findFirstByJpql(j, m);
//        pi.isRetired();
        return pi;
    }

    public boolean sampledForAnyItemInTheBill(Bill bill) {
        String jpql;
        jpql = "select pi from PatientInvestigation pi where pi.billItem.bill=:b";
        Map m = new HashMap();
        m.put("b", bill);
        List<PatientInvestigation> pis = getFacade().findByJpql(jpql, m);
        for (PatientInvestigation pi : pis) {
            if (pi.getCollected() || pi.getReceived() || pi.getDataEntered()) {
                return true;
            }
        }
        return false;
    }

    public boolean sampledForBillItem(BillItem billItem) {
        String jpql;
        jpql = "select pi from PatientInvestigation pi where pi.billItem=:b";
        Map m = new HashMap();
        m.put("b", billItem);
        List<PatientInvestigation> pis = getFacade().findByJpql(jpql, m);
        for (PatientInvestigation pi : pis) {
            if (pi.getReceived() || pi.getDataEntered()) {
                return true;
            }
        }
        return false;
    }

    public boolean isListIncludingApproved() {
        return listIncludingApproved;
    }

    public void setListIncludingApproved(boolean listIncludingApproved) {
        this.listIncludingApproved = listIncludingApproved;
        lstToApprove = null;
    }

    public boolean isListIncludingEnteredData() {
        return listIncludingEnteredData;
    }

    public void setListIncludingEnteredData(boolean listIncludingEnteredData) {
        this.listIncludingEnteredData = listIncludingEnteredData;
        lstToEnterData = null;
    }

    public boolean isListIncludingReceived() {
        return listIncludingReceived;
    }

    public void setListIncludingReceived(boolean listIncludingReceived) {
        this.listIncludingReceived = listIncludingReceived;
        lstToReceive = null;
    }

    public List<PatientInvestigation> getSelectedToReceive() {
        if (selectedToReceive != null) {
            for (PatientInvestigation pi : selectedToReceive) {
                for (ReportItem ri : pi.getInvestigation().getReportItems()) {
                }
            }
        } else {
            selectedToReceive = new ArrayList<>();
        }
        return selectedToReceive;
    }

    public void setSelectedToReceive(List<PatientInvestigation> selectedToReceive) {
        this.selectedToReceive = selectedToReceive;
    }

    public Date getSampledOutsideDate() {
        if (sampledOutsideDate == null) {
            sampledOutsideDate = Calendar.getInstance().getTime();
        }
        return sampledOutsideDate;
    }

    public void setSampledOutsideDate(Date sampledOutsideDate) {
        this.sampledOutsideDate = sampledOutsideDate;
    }

    public boolean isSampledOutSide() {
        return sampledOutSide;
    }

    public void setSampledOutSide(boolean sampledOutSide) {
        this.sampledOutSide = sampledOutSide;
    }

    public List<Investigation> getInvestSummery() {
        if (investSummery == null) {
            String temSql;
            Map temMap = new HashMap();
            temSql = "SELECT inv FROM Investigation inv WHERE inv.id in(SELECT i.investigation.id FROM PatientInvestigation i where i.retired=false and i.collected = false and i.billItem.bill.billDate between :fromDate and :toDate)";
            temMap.put("toDate", getToDate());
            temMap.put("fromDate", getFromDate());
            investSummery = getInvestFacade().findByJpql(temSql, temMap, TemporalType.TIMESTAMP);
        }
        if (investSummery == null) {
            investSummery = new ArrayList<>();
        }
        return investSummery;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonFunctions.getStartOfDay(new Date());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
        resetLists();
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = CommonFunctions.getEndOfDay(new Date());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
        resetLists();
    }

    public PatientReportFacade getPrFacade() {
        return prFacade;
    }

    public void setPrFacade(PatientReportFacade prFacade) {
        this.prFacade = prFacade;
    }

    public InvestigationItemFacade getInvestigationItemFacade() {
        return investigationItemFacade;
    }

    public void setInvestigationItemFacade(InvestigationItemFacade investigationItemFacade) {
        this.investigationItemFacade = investigationItemFacade;
    }

    public Investigation getCurrentInvestigation() {
        return currentInvestigation;
    }

    public void setCurrentInvestigation(Investigation currentInvestigation) {
        String sql;
        if (currentInvestigation != null) {

            sql = "select i from InvestigationItem i where i.retired = false and i.item.id = " + currentInvestigation.getId() + " and i.ixItemType = com.divudi.core.data.InvestigationItemType.Value order by i.cssTop, i.cssLeft";
            setCurrentInvestigationItems(getInvestigationItemFacade().findByJpql(sql));
        } else {
            setCurrentInvestigationItems(new ArrayList<InvestigationItem>());
        }
        this.currentInvestigation = currentInvestigation;
    }

    public List<InvestigationItem> getCurrentInvestigationItems() {
        return currentInvestigationItems;
    }

    public void setCurrentInvestigationItems(List<InvestigationItem> currentInvestigationItems) {
        this.currentInvestigationItems = currentInvestigationItems;
    }

    public List<PatientInvestigation> getSelectedItems() {
        return selectedItems;
    }

    public void prepareAdd() {
        current = new PatientInvestigation();
    }

    public void setSelectedItems(List<PatientInvestigation> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public String getSelectText() {
        return selectText;
    }

    private void recreateModel() {
        items = null;
        lstToApprove = null;
        lstToSamle = null;
        lstToPrint = null;
        lstToReceiveSearch = null;
    }

    public void saveSelected() {

        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage("Updated Successfully.");
        } else {
            current.setCreatedAt(new Date());
            current.setCreater(getSessionController().getLoggedUser());
            getFacade().create(current);
            JsfUtil.addSuccessMessage("Saved Successfully");
        }
        recreateModel();
        getItems();
    }

    public void setSelectText(String selectText) {
        recreateModel();
        this.selectText = selectText;
    }

    public PatientInvestigationFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(PatientInvestigationFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public PatientInvestigationController() {
    }

    public PatientInvestigation getCurrent() {
        if (current == null) {
            current = new PatientInvestigation();
        }
        return current;
    }

    List<ReportItem> currentReportItems;
    @EJB
    ReportItemFacade reportItemFacade;

    public ReportItemFacade getReportItemFacade() {
        return reportItemFacade;
    }

    public void setReportItemFacade(ReportItemFacade reportItemFacade) {
        this.reportItemFacade = reportItemFacade;
    }

    public List<ReportItem> getCurrentReportItems() {
        String sql;
        Map m = new HashMap();
        sql = "select i from ReportItem i where i.item=:ix order by i.cssTop";
        m.put("ix", getCurrent().getInvestigation());
        currentReportItems = getReportItemFacade().findByJpql(sql, m);
        return currentReportItems;
    }

    public void setCurrentReportItems(List<ReportItem> currentReportItems) {
        this.currentReportItems = currentReportItems;
    }

    public void setCurrent(PatientInvestigation current) {
        if (current != null) {
            setCurrentInvestigation(current.getInvestigation());
        } else {
            setCurrentInvestigation(null);
        }
        this.current = current;
    }

    private PatientInvestigationFacade getFacade() {
        return ejbFacade;
    }

    public List<PatientInvestigation> getItems() {
        return items;
    }

    public List<PatientInvestigation> getLstToSamle() {
        return lstToSamle;
    }

    public boolean isShowSamplingPagination() {
        if (getLstToSamle().size() > 20) {
            showSamplingPagination = true;
        } else {
            showSamplingPagination = false;
        }
        return showSamplingPagination;
    }

    public void setShowSamplingPagination(boolean showSamplingPagination) {
        this.showSamplingPagination = showSamplingPagination;
    }

    public void sendSms() {
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing to send sms");
            return;
        }
        Bill bill = current.getBillItem().getBill();
        if (bill == null || bill.getPatient() == null || bill.getPatient().getPerson() == null || bill.getPatient().getPerson().getSmsNumber() == null) {
            JsfUtil.addErrorMessage("System Error");
            return;
        }

        Sms s = new Sms();
        s.setPending(false);
        s.setBill(bill);
        s.setCreatedAt(new Date());
        s.setCreater(sessionController.getLoggedUser());
        s.setDepartment(sessionController.getLoggedUser().getDepartment());
        s.setInstitution(sessionController.getLoggedUser().getInstitution());
        s.setPatientInvestigation(current);

        s.setReceipientNumber(bill.getPatient().getPerson().getSmsNumber());

        String messageBody = "Dear Sir/Madam, "
                + "Reports bearing bill number " + bill.getInsId() + " is ready for collection at "
                + sessionController.getLoggedUser().getDepartment().getPrintingName() + ".";

        s.setSendingMessage(messageBody);
        s.setSentSuccessfully(true);
        s.setSmsType(MessageType.LabReport);
        getSmsFacade().create(s);

        UserPreference ap = sessionController.getApplicationPreference();

        boolean sent = smsManagerEjb.sendSms(s);

        if (sent) {
            getCurrent().getBillItem().getBill().setSmsed(true);
            getCurrent().getBillItem().getBill().setSmsedAt(new Date());
            getCurrent().getBillItem().getBill().setSmsedUser(getSessionController().getLoggedUser());
            getFacade().edit(current);
            getCurrent().getBillItem().getBill().getSentSmses().add(s);
            billFacade.edit(getCurrent().getBillItem().getBill());
            JsfUtil.addSuccessMessage("Sms send");
        } else {
            JsfUtil.addErrorMessage("Sending SMS Failed.");
        }
    }

    public void markAsSampled() {
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing to sample");
            return;
        }

        getCurrent().setSampleCollecter(getSessionController().getLoggedUser());
        if (current.getSampleOutside()) {
            getCurrent().setSampledAt(sampledOutsideDate);
        } else {
            getCurrent().setSampledAt(new Date());
            current.setSampleDepartment(getSessionController().getLoggedUser().getDepartment());
            current.setSampleInstitution(getSessionController().getLoggedUser().getInstitution());
        }
        if (getCurrent().getId() != null || getCurrent().getId() != 0) {
            getCurrent().setCollected(Boolean.TRUE);
            getCurrent().setSampleCollecter(getSessionController().getLoggedUser());
            getEjbFacade().edit(getCurrent());
            JsfUtil.addSuccessMessage("Marked as Sampled");
        } else {
            JsfUtil.addErrorMessage("Empty");
        }
        setSampledOutsideDate(Calendar.getInstance().getTime());

        getLabReportSearchByInstitutionController().createPatientInvestigaationList();
    }

    public void markAsSampledCollected(PatientInvestigation pi) {
        if (pi == null) {
            JsfUtil.addErrorMessage("Nothing to sample");
            return;
        }

        getCurrent().setSampleCollecter(getSessionController().getLoggedUser());
        if (current.getSampleOutside()) {
            getCurrent().setSampledAt(sampledOutsideDate);
        } else {
            getCurrent().setSampledAt(new Date());
            current.setSampleDepartment(getSessionController().getLoggedUser().getDepartment());
            current.setSampleInstitution(getSessionController().getLoggedUser().getInstitution());
        }
        if (getCurrent().getId() != null || getCurrent().getId() != 0) {
            getCurrent().setCollected(Boolean.TRUE);
            getCurrent().setSampleCollecter(getSessionController().getLoggedUser());
            getEjbFacade().edit(getCurrent());
            JsfUtil.addSuccessMessage("Marked as Sampled");
        } else {
            JsfUtil.addErrorMessage("Empty");
        }
        setSampledOutsideDate(Calendar.getInstance().getTime());

        getLabReportSearchByInstitutionController().createPatientInvestigaationList();
    }

    public void revertMarkedSample() {
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing to Revert");
            return;
        }
        getCurrent().setSampleCollecter(getSessionController().getLoggedUser());

        if (getCurrent().getId() != null || getCurrent().getId() != 0) {
            getCurrent().setCollected(Boolean.FALSE);
            getCurrent().setReceived(Boolean.FALSE);
            getCurrent().setDataEntered(Boolean.FALSE);
            getCurrent().setSampleCollecter(getSessionController().getLoggedUser());
            getEjbFacade().edit(getCurrent());
            JsfUtil.addSuccessMessage("Revert Sample Successfully");
        } else {
            JsfUtil.addErrorMessage("Empty");
        }
        setSampledOutsideDate(Calendar.getInstance().getTime());

        getLabReportSearchByInstitutionController().createPatientInvestigaationList();
    }

    public void setLstToSamle(List<PatientInvestigation> lstToSamle) {
        this.lstToSamle = lstToSamle;
    }

    public List<PatientInvestigation> getLstToReceive() {
        if (lstToReceive == null) {
            String temSql;
            getCurrent().getSampledAt();
            Map temMap = new HashMap();
            if (listIncludingReceived) {
                temSql = "SELECT i FROM PatientInvestigation i where i.retired=false and i.collected = true and i.sampledAt between :fromDate and :toDate and i.receiveDepartment.id = " + getSessionController().getDepartment().getId();
            } else {
                temSql = "SELECT i FROM PatientInvestigation i where i.retired=false and i.collected = true and i.received=false and i.sampledAt between :fromDate and :toDate and i.receiveDepartment.id = " + getSessionController().getDepartment().getId();
            }
            temMap.put("toDate", getToDate());
            temMap.put("fromDate", getFromDate());
            lstToReceive = getFacade().findByJpql(temSql, temMap, TemporalType.TIMESTAMP);
        }
        if (lstToReceive == null) {
            lstToReceive = new ArrayList<>();
        }
        return lstToReceive;
    }
    List<PatientInvestigation> lstToReceiveSearch;

    public List<PatientInvestigation> getLstToReceiveSearch() {
        if (lstToReceiveSearch == null) {
            String temSql;
            Map temMap = new HashMap();
            if (selectText == null || selectText.trim().isEmpty()) {
                temSql = "SELECT i FROM PatientInvestigation i where i.retired=false and i.collected = true and i.sampledAt between :fromDate and :toDate and i.receiveDepartment.id = " + getSessionController().getDepartment().getId();
            } else {
                temSql = "select pi from PatientInvestigation pi join pi.investigation i join pi.billItem.bill b join b.patient.person p   where ((p.name) like '%" + selectText.toUpperCase() + "%' or (b.insId) like '%" + selectText.toUpperCase() + "%' or p.phone like '%" + selectText + "%' or (i.name) like '%" + selectText.toUpperCase() + "%' )  and pi.retired=false and b.createdAt between :fromDate and :toDate and pi.receiveDepartment.id = " + getSessionController().getDepartment().getId();
            }

            temMap.put("toDate", getToDate());
            temMap.put("fromDate", getFromDate());
            lstToReceiveSearch = getFacade().findByJpql(temSql, temMap, TemporalType.TIMESTAMP);
        }

        if (lstToReceiveSearch == null) {
            lstToReceiveSearch = new ArrayList<>();
        }
        return lstToReceiveSearch;
    }

    @Deprecated
    public String navigateToSampleManagement() {
        prepareToSample();
        return "/lab/sample_management?faces-redirect=true";
    }

    public String navigateToToCollelct() {
//        prepareToSample();
        listPatientInvestigationAwaitingSamplling();
        return "/lab/patient_investigations_to_collect?faces-redirect=true";
    }

    public String navigateToGenerateBarcodes() {
        makeNull();
        boolean searchInvestigationsForLoggedInstitution = configOptionApplicationController.getBooleanValueByKey("For Lab Sample Barcode Generation, Search by Ordered Institution", false);
        if (searchInvestigationsForLoggedInstitution) {
            orderedInstitution = sessionController.getInstitution();
        }
        boolean searchInvestigationsForLoggedDepartment = configOptionApplicationController.getBooleanValueByKey("For Lab Sample Barcode Generation, Search by Ordered Department", false);
        if (searchInvestigationsForLoggedDepartment) {
            orderedDepartment = sessionController.getDepartment();
        }
        listBillsToGenerateBarcodes();

        return "/lab/generate_barcode_p?faces-redirect=true";
    }

    public String navigateToGenerateBarcodesFromCourier() {
        boolean searchInvestigationsForLoggedInstitution = configOptionApplicationController.getBooleanValueByKey("For Lab Sample Barcode Generation, Search by Ordered Institution", false);
        if (searchInvestigationsForLoggedInstitution) {
            orderedInstitution = sessionController.getInstitution();
        }
        boolean searchInvestigationsForLoggedDepartment = configOptionApplicationController.getBooleanValueByKey("For Lab Sample Barcode Generation, Search by Ordered Department", false);
        if (searchInvestigationsForLoggedDepartment) {
            orderedDepartment = sessionController.getDepartment();
        }
        listBillsToGenerateBarcodes();
        return "/collecting_centre/courier/generate_barcode_p_cup?faces-redirect=true";
    }

    public String navigateToSentToLab() {
        return "/lab/sent_to_lab?faces-redirect=true";
    }

    //  Navigation Lab sampling at lab for barcode generate page
    public String navigateToGenerateBarcodesForLab() {
        return "/lab/generate_barcodes_for_lab?faces-redirect=true";
    }

    public String navigateToCollectSamples() {
        listBarcodeGeneratedPatientSamplesYetToCollect();
        return "/lab/samples_to_collect?faces-redirect=true";
    }

    public String navigateToAcceptSamples() {
        return "/lab/accept_samples?faces-redirect=true";
    }

    public String navigateToRejectSamples() {
        return "/lab/reject_samples?faces-redirect=true";
    }

    public String navigateToRevertSamples() {
        return "/lab/revert_samples?faces-redirect=true";
    }

    public String navigateToDivideSamples() {
        return "/lab/divide_samples?faces-redirect=true";
    }

    public void prepareToSample() {
        String temSql;
        Map temMap = new HashMap();
        temSql = "SELECT i FROM PatientInvestigation i where i.retired=false  and i.collected = false and i.billItem.bill.billDate between :fromDate and :toDate";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        lstToSamle = getFacade().findByJpql(temSql, temMap, TemporalType.TIMESTAMP);
        checkRefundBillItems(lstToSamle);
    }

    @Deprecated
    public void generateBarcodesForSelectedBills() {
        selectedBillBarcodes = new ArrayList<>();
        billBarcodes = new ArrayList<>();
        if (selectedBills == null) {
            JsfUtil.addErrorMessage("No Bills Seelcted");
            return;
        }
        if (selectedBills.isEmpty()) {
            JsfUtil.addErrorMessage("No Bills Seelcted");
            return;
        }
        listingEntity = ListingEntity.BILL_BARCODES;
        for (Bill b : selectedBills) {
            BillBarcode bb = new BillBarcode(b);
            List<Bill> bs = billBeanController.findValidBillsForSampleCollection(b);
            List<PatientSampleWrapper> psws = new ArrayList<>();
            List<PatientSample> pss = prepareSampleCollectionByBillsForPhlebotomyRoom(bs, sessionController.getLoggedUser());
            if (pss != null) {
                for (PatientSample ps : pss) {
                    PatientSampleWrapper ptsw = new PatientSampleWrapper(ps);
                    psws.add(ptsw);
                }
            }
            for (PatientInvestigationWrapper piw : bb.getPatientInvestigationWrappers()) {
                piw.getPatientInvestigation().setBarcodeGenerated(true);
                piw.getPatientInvestigation().setBarcodeGeneratedAt(new Date());
                piw.getPatientInvestigation().setBarcodeGeneratedBy(sessionController.getLoggedUser());
                ejbFacade.edit(piw.getPatientInvestigation());
            }
            bb.setPatientSampleWrappers(psws);
            billBarcodes.add(bb);
        }
        selectedBillBarcodes = billBarcodes;
    }

    public void generateBarcodesForSelectedBill(Bill billForBarcode) {
        selectedBillBarcodes = new ArrayList<>();
        billBarcodes = new ArrayList<>();
        setCurrentBill(billForBarcode);
        if (billForBarcode == null) {
            JsfUtil.addErrorMessage("No Bills Seelcted");
            return;
        }

        if (billForBarcode.isCancelled()) {
            JsfUtil.addErrorMessage("This Bill is Already Cancel");
            return;
        }

        BillBarcode bb = new BillBarcode(billForBarcode);
        List<PatientSampleWrapper> psws = new ArrayList<>();
        List<PatientSample> pss = prepareSampleCollectionByBillsForPhlebotomyRoom(billForBarcode, sessionController.getLoggedUser());
        StringBuilder sampleIDs = new StringBuilder();
        if (pss != null) {
            for (PatientSample ps : pss) {
                PatientSampleWrapper ptsw = new PatientSampleWrapper(ps);
                psws.add(ptsw);
                if (!sampleIDs.toString().contains(ps.getIdStr())) {
                    sampleIDs.append(ps.getIdStr()).append(" ");  // Add space for separation
                }
            }
        }

        for (PatientInvestigationWrapper piw : bb.getPatientInvestigationWrappers()) {
            if (billForBarcode.getStatus() == PatientInvestigationStatus.ORDERED) {
                piw.getPatientInvestigation().setBarcodeGenerated(true);
                piw.getPatientInvestigation().setBarcodeGeneratedAt(new Date());

            }

            // Properly add unique sample IDs to PatientInvestigation
            String[] idsToAdd = sampleIDs.toString().trim().split("\\s+");
            String existingSampleIds = piw.getPatientInvestigation().getSampleIds();
            for (String id : idsToAdd) {
                if (!existingSampleIds.contains(id)) {
                    existingSampleIds += " " + id;
                }
            }
            if (billForBarcode.getStatus() == PatientInvestigationStatus.ORDERED) {
                piw.getPatientInvestigation().setBarcodeGeneratedBy(sessionController.getLoggedUser());
                piw.getPatientInvestigation().setStatus(PatientInvestigationStatus.SAMPLE_GENERATED);
            }
            piw.getPatientInvestigation().setSampleIds(existingSampleIds.trim());

            ejbFacade.edit(piw.getPatientInvestigation());
        }
        if (billForBarcode.getStatus() == PatientInvestigationStatus.ORDERED) {
            billForBarcode.setStatus(PatientInvestigationStatus.SAMPLE_GENERATED);
        }

        billFacade.edit(billForBarcode);
        bb.setPatientSampleWrappers(psws);

        billBarcodes.add(bb);
        selectedBillBarcodes = billBarcodes;
        listingEntity = ListingEntity.BILL_BARCODES;
    }

    public void generateBarcodesForSelectedPatientInvestigations() {
        if (selectedItems == null || selectedItems.isEmpty()) {
            JsfUtil.addErrorMessage("No Patient Investigations are Selected");
            return;
        }

        // Create a set to track unique bills
        Set<Bill> uniqueBills = new HashSet<>();
        Bill ptIxBill = null;

        Bill tb = null;

// Iterate through selected items and add bills to the set
        for (PatientInvestigation pi : selectedItems) {
            Bill b = pi.getBillItem().getBill();
            tb = b;
            ptIxBill = b;
            uniqueBills.add(b);
        }

        // Check if there is more than one distinct bill
        if (uniqueBills.size() > 1) {
            JsfUtil.addErrorMessage("Multiple bills detected. Barcodes cannot be generated.");
            return;
        }

        if (ptIxBill == null) {
            JsfUtil.addErrorMessage("No Bill Found");
            return;
        }

        if (tb != null) {
            if (tb.getStatus() == PatientInvestigationStatus.ORDERED) {
                tb.setStatus(PatientInvestigationStatus.SAMPLE_GENERATED);
                billFacade.edit(tb);
            }

        }
        // Since there is only one bill, create a single BillBarcode
        BillBarcode billBarcode = new BillBarcode();
        billBarcode.setBill(ptIxBill);

        // Wrap patient investigations and add them to the BillBarcode
        for (PatientInvestigation pi : selectedItems) {
            PatientInvestigationWrapper piw = new PatientInvestigationWrapper(pi);
            billBarcode.getPatientInvestigationWrappers().add(piw);
        }

        // Prepare sample collection and assign to the list
        prepareSampleCollectionByBillsForPhlebotomyRoom(billBarcode);

        // Assign the billBarcode to the lists
        billBarcodes = new ArrayList<>();
        billBarcodes.add(billBarcode);
        selectedBillBarcodes = billBarcodes;

        // Set the listing entity for the view
        listingEntity = ListingEntity.BILL_BARCODES;
        // Debugging output

    }

    public void collectSamples() {

        if (selectedPatientSamples == null || selectedPatientSamples.isEmpty()) {
            JsfUtil.addErrorMessage("No samples selected");
            return;
        }

        List<PatientSample> canCollectSamples = new ArrayList<>();
        for (PatientSample ps : selectedPatientSamples) {
            if (ps.getBill().isCancelled()) {
                JsfUtil.addErrorMessage("This Bill is Already Cancel");
                return;
            }
            if (ps.getStatus() == PatientInvestigationStatus.SAMPLE_COLLECTED || ps.getStatus() == PatientInvestigationStatus.SAMPLE_RECOLLECTED) {
                JsfUtil.addErrorMessage("This sample (" + ps.getId() + ") is already colleted.");
                return;
            }
            if (ps.getStatus() == PatientInvestigationStatus.SAMPLE_GENERATED || ps.getStatus() == PatientInvestigationStatus.SAMPLE_REGENERATED) {
                canCollectSamples.add(ps);
            }
        }

        if (canCollectSamples.isEmpty()) {
            JsfUtil.addErrorMessage("There are no suitable samples to collect from the selected samples.");
            return;
        }

        listingEntity = ListingEntity.PATIENT_SAMPLES;

        Map<Long, PatientInvestigation> collectedPtixs = new HashMap<>();
        Map<Long, Bill> collectedBills = new HashMap<>();

        // Update sample collection details and gather associated patient investigations
        for (PatientSample ps : canCollectSamples) {
            ps.setSampleCollected(true);
            ps.setSampleCollectedAt(new Date());
            ps.setInstitution(sessionController.getInstitution());
            ps.setDepartment(sessionController.getDepartment());
            ps.setSampleCollectedDepartment(sessionController.getDepartment());
            ps.setSampleCollectedInstitution(sessionController.getInstitution());
            ps.setSampleCollecter(sessionController.getLoggedUser());

            if (ps.getReferenceSample() != null && ps.getReferenceSample().getStatus() == PatientInvestigationStatus.SAMPLE_RECOLLECTION_PENDING) {
                ps.getReferenceSample().setStatus(PatientInvestigationStatus.SAMPLE_RECOLLECTION_COMPLETE);
                patientSampleFacade.edit(ps.getReferenceSample());
                ps.setStatus(PatientInvestigationStatus.SAMPLE_RECOLLECTED);
            } else {
                ps.setStatus(PatientInvestigationStatus.SAMPLE_COLLECTED);
            }

            patientSampleFacade.edit(ps);

            // Retrieve and store PatientInvestigations by unique ID to avoid duplicates
            for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                collectedPtixs.putIfAbsent(pi.getId(), pi);
            }
        }

        // Update patient investigations and collect associated bills
        for (PatientInvestigation tptix : collectedPtixs.values()) {
            tptix.setSampleCollected(true);
            tptix.setSampleCollectedAt(new Date());
            tptix.setSampleCollectedBy(sessionController.getLoggedUser());

            if (tptix.getStatus() == PatientInvestigationStatus.SAMPLE_RECOLLECTION_PENDING) {
                tptix.setStatus(PatientInvestigationStatus.SAMPLE_RECOLLECTED);
            } else {
                tptix.setStatus(PatientInvestigationStatus.SAMPLE_COLLECTED);
            }

            getFacade().edit(tptix);
            collectedBills.putIfAbsent(tptix.getBillItem().getBill().getId(), tptix.getBillItem().getBill());
        }

        // Update bills status
        for (Bill tb : collectedBills.values()) {
            if (tb.getStatus() == PatientInvestigationStatus.SAMPLE_RECOLLECTION_PENDING) {
                tb.setStatus(PatientInvestigationStatus.SAMPLE_RECOLLECTED);
            } else {
                tb.setStatus(PatientInvestigationStatus.SAMPLE_COLLECTED);
            }
            billFacade.edit(tb);
        }

        if (configOptionApplicationController.getBooleanValueByKey("Lab Test History Enabled", false)) {
            for (PatientSample ps : canCollectSamples) {
                if (ps.getReferenceSample() != null && ps.getReferenceSample().getStatus() == PatientInvestigationStatus.SAMPLE_RECOLLECTION_COMPLETE) {
                    for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                        labTestHistoryController.addSampleReCollectHistory(pi, ps.getReferenceSample());
                    }
                }

                for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                    labTestHistoryController.addSampleCollectHistory(pi, ps);
                }
            }
        }

        JsfUtil.addSuccessMessage("Selected Samples Collected");
    }

    public void sendSamplesToLab() {
        List<PatientSample> canSentSamples = new ArrayList<>();

        if (selectedPatientSamples == null || selectedPatientSamples.isEmpty()) {
            JsfUtil.addErrorMessage("No samples selected");
            return;
        }

        for (PatientSample ps : selectedPatientSamples) {
            if (ps.getBill().isCancelled()) {
                JsfUtil.addErrorMessage("This Bill is Already Cancel");
                return;
            }
            if (ps.getOutsourced()) {
                JsfUtil.addErrorMessage("This Sample (" + ps.getId() + ") is Already Sent Outsource Lab");
                return;
            }
            if (ps.getStatus() == PatientInvestigationStatus.SAMPLE_SENT) {
                JsfUtil.addErrorMessage("This sample (" + ps.getId() + ") is already Sent.");
                return;
            }
            if (ps.getStatus() == PatientInvestigationStatus.SAMPLE_COLLECTED || ps.getStatus() == PatientInvestigationStatus.SAMPLE_RECOLLECTED) {
                canSentSamples.add(ps);
            }
        }

        if (canSentSamples.isEmpty()) {
            JsfUtil.addErrorMessage("There are no suitable samples to send from the selected samples.");
            return;
        }

        listingEntity = ListingEntity.PATIENT_SAMPLES;

        Map<Long, PatientInvestigation> samplePtixs = new HashMap<>();
        Map<Long, Bill> sampleBills = new HashMap<>();

        // Process each selected patient sample
        for (PatientSample ps : canSentSamples) {
            ps.setSampleTransportedToLabByStaff(sampleTransportedToLabByStaff);
            ps.setSampleSent(true);
            ps.setSampleSentBy(sessionController.getLoggedUser());
            ps.setSampleSentAt(new Date());
            ps.setStatus(PatientInvestigationStatus.SAMPLE_SENT);
            patientSampleFacade.edit(ps);

            // Retrieve and store PatientInvestigations by unique ID to avoid duplicates
            for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                samplePtixs.putIfAbsent(pi.getId(), pi);
            }
        }

        // Update PatientInvestigations and store associated Bills by unique ID to avoid duplicates
        for (PatientInvestigation tptix : samplePtixs.values()) {
            tptix.setSampleSent(true);
            tptix.setSampleTransportedToLabByStaff(sampleTransportedToLabByStaff);
            tptix.setSampleSentAt(new Date());
            tptix.setSampleSentBy(sessionController.getLoggedUser());
            tptix.setStatus(PatientInvestigationStatus.SAMPLE_SENT);
            getFacade().edit(tptix);
            sampleBills.putIfAbsent(tptix.getBillItem().getBill().getId(), tptix.getBillItem().getBill());
        }

        if (configOptionApplicationController.getBooleanValueByKey("Lab Test History Enabled", false)) {
            for (PatientSample ps : canSentSamples) {
                for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                    labTestHistoryController.addSampleSentHistory(pi, ps, sampleTransportedToLabByStaff);
                }
            }
        }

        // Update Bills
        for (Bill tb : sampleBills.values()) {
            tb.setStatus(PatientInvestigationStatus.SAMPLE_SENT);
            billFacade.edit(tb);
        }

        sampleTransportedToLabByStaff = null;
        JsfUtil.addSuccessMessage("Selected Samples Sent to Lab");
    }

    public void collectAndReceiveSamplesAtLab() {
        collectSamples();
        sendSamplesToLab();
        receiveSamplesAtLab();
    }

    public void receiveSamplesAtLab() {

        if (selectedPatientSamples == null || selectedPatientSamples.isEmpty()) {
            JsfUtil.addErrorMessage("No samples selected");
            return;
        }
        List<PatientSample> canAcceptSamples = new ArrayList<>();
        for (PatientSample ps : selectedPatientSamples) {
            if (ps.getBill().isCancelled()) {
                JsfUtil.addErrorMessage("This Bill is Already Cancel");
                return;
            }
            if (ps.getOutsourced()) {
                JsfUtil.addErrorMessage("This Sample (" + ps.getId() + ") is Already Sent Outsource Lab");
                return;
            }
            if (ps.getStatus() == PatientInvestigationStatus.SAMPLE_ACCEPTED) {
                JsfUtil.addErrorMessage("This Sample (" + ps.getId() + ") is Already Accepted");
                return;
            }
            if (ps.getStatus() == PatientInvestigationStatus.SAMPLE_SENT) {
                canAcceptSamples.add(ps);
            }
        }

        if (canAcceptSamples.isEmpty()) {
            JsfUtil.addErrorMessage("There are no suitable samples to be Receive from the selected samples.");
            return;
        }

        listingEntity = ListingEntity.PATIENT_SAMPLES;

        Map<Long, PatientInvestigation> receivedPtixs = new HashMap<>();
        Map<Long, Bill> receivedBills = new HashMap<>();

        // Update sample details and collect associated patient investigations
        for (PatientSample ps : canAcceptSamples) {
            ps.setSampleReceivedAtLab(true);
            ps.setSampleReceiverAtLab(sessionController.getLoggedUser());
            ps.setSampleReceivedAtLabDepartment(sessionController.getDepartment());
            ps.setSampleReceivedAtLabInstitution(sessionController.getInstitution());
            ps.setInstitution(sessionController.getInstitution());
            ps.setDepartment(sessionController.getDepartment());
            ps.setSampleReceivedAtLabAt(new Date());
            ps.setStatus(PatientInvestigationStatus.SAMPLE_ACCEPTED);
            patientSampleFacade.edit(ps);

            // Retrieve and store PatientInvestigations by unique ID to avoid duplicates
            for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                receivedPtixs.putIfAbsent(pi.getId(), pi);
            }
        }

        // Update patient investigations and collect associated bills
        for (PatientInvestigation tptix : receivedPtixs.values()) {
            tptix.setSampleAccepted(true);
            tptix.setSampleAcceptedAt(new Date());
            tptix.setSampleAcceptedBy(sessionController.getLoggedUser());
            tptix.setReceived(true);
            tptix.setReceivedAt(new Date());
            tptix.setReceiveDepartment(sessionController.getDepartment());
            tptix.setReceiveInstitution(sessionController.getInstitution());
            tptix.setStatus(PatientInvestigationStatus.SAMPLE_ACCEPTED);
            getFacade().edit(tptix);
            receivedBills.putIfAbsent(tptix.getBillItem().getBill().getId(), tptix.getBillItem().getBill());
        }

        if (configOptionApplicationController.getBooleanValueByKey("Lab Test History Enabled", false)) {
            for (PatientSample ps : canAcceptSamples) {
                for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                    labTestHistoryController.addSampleReceiveHistory(pi, ps);
                }
            }
        }

        // Update bills status
        for (Bill tb : receivedBills.values()) {
            tb.setStatus(PatientInvestigationStatus.SAMPLE_ACCEPTED);
            billFacade.edit(tb);
        }

        JsfUtil.addSuccessMessage("Selected Samples Are Received at Lab");
    }

    public void rejectSamples() {
        if (selectedPatientSamples == null || selectedPatientSamples.isEmpty()) {
            JsfUtil.addErrorMessage("No samples selected");
            return;
        }

        if (sampleRejectionComment == null || sampleRejectionComment.equalsIgnoreCase("")) {
            JsfUtil.addErrorMessage("Samples reject reason is Missing..");
            return;
        }
        
        List<PatientSample> canRejectSamples = new ArrayList<>();
        for (PatientSample ps : selectedPatientSamples) {
            if (ps.getBill().isCancelled()) {
                JsfUtil.addErrorMessage("This Bill is Already Cancel");
                return;
            }
            if (ps.getStatus() == PatientInvestigationStatus.SAMPLE_REJECTED) {
                JsfUtil.addErrorMessage("This Sample (" + ps.getId() + ") is Already Rejected");
                return;
            }
            
            String jpql = "SELECT r "
                + " FROM PatientReport r "
                + " WHERE r.retired = :ret "
                + " AND r.patientInvestigation in ( select ps.patientInvestigation from PatientSampleComponant ps where ps.patientSample=:pts and ps.retired=false ) ";
            
            Map<String, Object> params = new HashMap<>();
            params.put("pts", ps);
            params.put("ret", false);
            
            PatientReport pr = patientReportFacade.findFirstByJpql(jpql, params, TemporalType.TIMESTAMP);
            
            if (pr != null) {
                JsfUtil.addErrorMessage("This Sample (" + ps.getId() + ") has Already Report Created");
                return;
            }
            
            canRejectSamples.add(ps);
            
        }

        if (canRejectSamples.isEmpty()) {
            JsfUtil.addErrorMessage("There are no suitable samples to be Receive from the selected samples.");
            return;
        }

        listingEntity = ListingEntity.PATIENT_SAMPLES;

        Map<Long, PatientInvestigation> rejectedPtixs = new HashMap<>();
        Map<Long, Bill> affectedBills = new HashMap<>();

        // Update sample rejection details and gather associated patient investigations
        for (PatientSample ps : canRejectSamples) {
            ps.setSampleReceivedAtLabComments(sampleRejectionComment);
            ps.setRequestReCollected(requestReCollected);
            ps.setSampleRejected(true);
            ps.setSampleRejectedAt(new Date());
            ps.setSampleRejectionComment(sampleRejectionComment);
            ps.setSampleRejectedBy(sessionController.getLoggedUser());
            ps.setStatus(PatientInvestigationStatus.SAMPLE_REJECTED);
            patientSampleFacade.edit(ps);

            // Retrieve and store PatientInvestigations by unique ID to avoid duplicates
            for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                rejectedPtixs.putIfAbsent(pi.getId(), pi);
            }
        }

        // Update patient investigations and gather associated bills
        for (PatientInvestigation tptix : rejectedPtixs.values()) {
            tptix.setSampleRejected(true);
            tptix.setSampleRejectedAt(new Date());
            tptix.setSampleRejectedBy(sessionController.getLoggedUser());
            tptix.setStatus(PatientInvestigationStatus.SAMPLE_REJECTED);
            getFacade().edit(tptix);
            affectedBills.putIfAbsent(tptix.getBillItem().getBill().getId(), tptix.getBillItem().getBill());
        }

        if (configOptionApplicationController.getBooleanValueByKey("Lab Test History Enabled", false)) {
            for (PatientSample ps : selectedPatientSamples) {
                for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                    labTestHistoryController.addSampleRejectHistory(pi, ps, sampleRejectionComment);

                    if (ps.getRequestReCollected()) {
                        labTestHistoryController.addSampleReCollectRequestHistory(pi, ps);
                    }
                }
            }
        }

        // Update bills status accordingly
        for (Bill tb : affectedBills.values()) {
            tb.setStatus(PatientInvestigationStatus.SAMPLE_REJECTED);
            billFacade.edit(tb);
        }
        sampleRejectionComment = null;
        requestReCollected = false;

        JsfUtil.addSuccessMessage("Selected Samples Are Rejected");
        
    }

    public void reGenerateSampleForRejectSamples() {

        if (selectedPatientSamples == null || selectedPatientSamples.isEmpty()) {
            JsfUtil.addErrorMessage("No samples selected");
            return;
        }

        List<PatientSample> canReGenarateSamples = new ArrayList<>();
        for (PatientSample ps : selectedPatientSamples) {
            if (ps.getBill().isCancelled()) {
                JsfUtil.addErrorMessage("This Bill is Already Cancel");
                return;
            }
            if (ps.getStatus() != PatientInvestigationStatus.SAMPLE_REJECTED) {
                JsfUtil.addErrorMessage("This sample (" + ps.getId() + ") is not Rejected.");
                return;
            }
            if (!ps.getRequestReCollected()) {
                JsfUtil.addErrorMessage("This sample (" + ps.getId() + ") is not not Requesr to Recollect.");
                return;
            }
            if (ps.getStatus() == PatientInvestigationStatus.SAMPLE_REJECTED && ps.getRequestReCollected()) {
                canReGenarateSamples.add(ps);
            }
        }

        if (canReGenarateSamples.isEmpty()) {
            JsfUtil.addErrorMessage("There are no suitable samples to Re-Genarate from the selected samples.");
            return;
        }

        listingEntity = ListingEntity.PATIENT_SAMPLES;

        Map<Long, PatientInvestigation> collectedPtixs = new HashMap<>();
        Map<Long, Bill> collectedBills = new HashMap<>();

        List<PatientSample> reGenarateSamples = new ArrayList<>();

        // Update sample collection details and gather associated patient investigations
        for (PatientSample ps : canReGenarateSamples) {
            PatientSample newlySample = createNewPatientSampleFromAnotherSample(ps);

            for (PatientInvestigation pi : getPatientInvestigationsBySample(newlySample)) {
                collectedPtixs.putIfAbsent(pi.getId(), pi);
            }
            reGenarateSamples.add(newlySample);
        }

        // Update patient investigations and collect associated bills
        for (PatientInvestigation tptix : collectedPtixs.values()) {
            tptix.setSampleCollected(true);
            tptix.setSampleCollectedAt(new Date());
            tptix.setSampleCollectedBy(sessionController.getLoggedUser());
            tptix.setStatus(PatientInvestigationStatus.SAMPLE_RECOLLECTION_PENDING);
            getFacade().edit(tptix);
            collectedBills.putIfAbsent(tptix.getBillItem().getBill().getId(), tptix.getBillItem().getBill());
        }

        // Update bills status
        for (Bill tb : collectedBills.values()) {
            tb.setStatus(PatientInvestigationStatus.SAMPLE_RECOLLECTION_PENDING);
            billFacade.edit(tb);
        }

        if (configOptionApplicationController.getBooleanValueByKey("Lab Test History Enabled", false)) {

            // Create newly created Sample Lab Test History
            for (PatientSample ps : reGenarateSamples) {
                for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                    labTestHistoryController.addBarcodeGenerateHistory(pi, ps);
                }
            }
            // Create Old Sample Lab Test History
            for (PatientSample ps : canReGenarateSamples) {
                for (PatientInvestigation pi : getPatientInvestigationsBySample(ps)) {
                    labTestHistoryController.addSampleReGenerateHistory(pi, ps);
                }
            }
        }

        JsfUtil.addSuccessMessage("Selected Samples Recreated");
    }

    public PatientSample createNewPatientSampleFromAnotherSample(PatientSample referringPatientSample) {
        if (referringPatientSample == null) {
            JsfUtil.addErrorMessage("Referring Sample is Empty");
            return null;
        }

        //reload Sample
        PatientSample pts = patientSampleFacade.find(referringPatientSample.getId());

        //create a new Sample
        PatientSample newlyGeneratedSample = new PatientSample();

        newlyGeneratedSample.setCreater(sessionController.getWebUser());
        newlyGeneratedSample.setCreatedAt(new Date());
        newlyGeneratedSample.setTube(pts.getTube());
        newlyGeneratedSample.setSample(pts.getSample());
        newlyGeneratedSample.setInvestigationComponant(pts.getInvestigationComponant());
        newlyGeneratedSample.setInstitution(sessionController.getInstitution());
        newlyGeneratedSample.setDepartment(sessionController.getDepartment());
        newlyGeneratedSample.setMachine(pts.getMachine());
        newlyGeneratedSample.setPatient(pts.getPatient());
        newlyGeneratedSample.setBill(pts.getBill());
        newlyGeneratedSample.setBarcodeGenerated(true);
        newlyGeneratedSample.setBarcodeGeneratedDepartment(sessionController.getDepartment());
        newlyGeneratedSample.setBarcodeGeneratedInstitution(sessionController.getInstitution());
        newlyGeneratedSample.setBarcodeGenerator(sessionController.getWebUser());
        newlyGeneratedSample.setBarcodeGeneratedAt(new Date());
        newlyGeneratedSample.setStatus(PatientInvestigationStatus.SAMPLE_REGENERATED);
        newlyGeneratedSample.setSampleCollected(false);
        newlyGeneratedSample.setSampleReceivedAtLab(false);
        newlyGeneratedSample.setReadyTosentToAnalyzer(false);
        newlyGeneratedSample.setSentToAnalyzer(false);
        newlyGeneratedSample.setReceivedFromAnalyzer(false);
        newlyGeneratedSample.setRetired(false);
        newlyGeneratedSample.setReferenceSample(pts);
        if (newlyGeneratedSample.getId() == null) {
            patientSampleFacade.create(newlyGeneratedSample);
        } else {
            patientSampleFacade.edit(newlyGeneratedSample);
        }

        pts.setStatus(PatientInvestigationStatus.SAMPLE_RECOLLECTION_PENDING);
        patientSampleFacade.edit(pts);

        List<PatientSampleComponant> oldSamplePatientSampleComponant;
        String jpql = "select ps from PatientSampleComponant ps where ps.patientSample=:pts ";
        Map params = new HashMap();
        params.put("pts", pts);

        oldSamplePatientSampleComponant = patientSampleComponantFacade.findByJpql(jpql, params);

        for (PatientSampleComponant ptsc : oldSamplePatientSampleComponant) {

            // Create new ampleComponant
            PatientSampleComponant newlyCreatedPatientSampleComponant = new PatientSampleComponant();

            newlyCreatedPatientSampleComponant.setPatientSample(newlyGeneratedSample);
            newlyCreatedPatientSampleComponant.setBill(newlyGeneratedSample.getBill());
            newlyCreatedPatientSampleComponant.setPatient(newlyGeneratedSample.getPatient());
            newlyCreatedPatientSampleComponant.setPatientInvestigation(ptsc.getPatientInvestigation());
            newlyCreatedPatientSampleComponant.setInvestigationComponant(ptsc.getInvestigationComponant());
            newlyCreatedPatientSampleComponant.setCreatedAt(new Date());
            newlyCreatedPatientSampleComponant.setCreater(sessionController.getWebUser());

            if (newlyCreatedPatientSampleComponant.getId() == null) {
                patientSampleComponantFacade.create(newlyCreatedPatientSampleComponant);
            } else {
                patientSampleComponantFacade.edit(newlyCreatedPatientSampleComponant);
            }
        }
        return newlyGeneratedSample;

    }

    public void generateSampleCodesSamples() {
        if (selectedPatientSamples == null || selectedPatientSamples.isEmpty()) {
            JsfUtil.addErrorMessage("No samples selected");
            return;
        }
        listingEntity = ListingEntity.PATIENT_SAMPLES;

        testDetails = "";

        // Update sample rejection details and gather associated patient investigations
        for (PatientSample ps : selectedPatientSamples) {
            testDetails += limsMiddlewareController.generateTestCodesForAnalyzer(ps.getIdStr());
        }

        JsfUtil.addSuccessMessage("Selected Samples Details created");
    }

    public void listPatientInvestigationAwaitingSamplling() {
        String temSql;
        Map temMap = new HashMap();
        temSql = "SELECT i FROM PatientInvestigation i where i.retired=false  and i.collected = false and i.billItem.bill.billDate between :fromDate and :toDate";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        lstForSampleManagement = getFacade().findByJpql(temSql, temMap, TemporalType.TIMESTAMP);
    }

    public void listBillsToGenerateBarcodes() {
        String jpql;
        Map params = new HashMap();
        jpql = "SELECT i "
                + " FROM PatientInvestigation i "
                + " where i.retired=:ret  "
                + " and i.barcodeGenerated=:bg "
                + " and i.billItem.bill.billDate between :fromDate and :toDate ";

        if (orderedInstitution != null) {
            jpql += " and i.billItem.bill.institution=:ins ";
            params.put("ins", getOrderedInstitution());
        }

        if (orderedInstitution != null) {
            jpql += " and i.billItem.bill.department=:dep ";
            params.put("dep", getOrderedDepartment());
        }

        jpql += " order by i.id desc";
        params.put("fromDate", getFromDate());
        params.put("toDate", getToDate());
        params.put("ret", false);
        params.put("bg", false);
        billBarcodes = new ArrayList<>();
        List<PatientInvestigation> pis = getFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);
        billBarcodes = createBilBarcodeObjects(pis);
    }

    public void clearFilters() {
        // Reset search filters
        this.searchDateType = null;
        this.fromDate = null;
        this.toDate = null;
        peformedSite = null;
        orderedDepartment = null;
        orderedInstitution = null;
        orderedSite = null;

        makeNull();
    }

    public void makeNull() {
        this.patientInvestigationStatus = null;
        this.referringDoctor = null;
        this.externalDoctor = null;
        this.equipment = null;
        this.investigation = null;
        this.type = null;
        this.sampleId = null;
        this.patientName = null;
        this.specimen = null;
        this.priority = null;
        this.route = null;
        this.collectionCenter = null;
        this.orderedInstitution = null;
        this.orderedDepartment = null;
        this.performingInstitution = null;
        this.performingDepartment = null;
        this.printIndividualBarcodes = false;
        this.listingEntity = null;
        this.investigationName = null;
        this.sampleSearchStrategy = null;
        clearReportData();
        clearAlternativeReportData();

    }

    public void clearReportData() {
        this.items = null;
        this.bills = null;
        this.selectedBillBarcodes = null;
        this.patientReports = null;
        this.patientSamples = null;
    }

    public void clearAlternativeReportData() {
        this.itemsForParentItem = null;
        this.patientSampleComponentsByInvestigation = null;
        this.currentPI = null;
    }

    public void searchBills() {
        if (sampleId != null && sampleId != 0) {
            searchBillsWithSampleId();
        } else {
            searchBillsWithoutSampleId();
        }
    }

    public void searchBillsWithoutSampleId() {
        listingEntity = ListingEntity.BILLS;
        String jpql;
        Map<String, Object> params = new HashMap<>();
        jpql = "SELECT pi.billItem.bill "
                + " FROM PatientInvestigation pi"
                + " WHERE pi.billItem.bill.retired = :ret";
        if (searchDateType == null) {
            searchDateType = SearchDateType.ORDERED_DATE;
        }
        switch (searchDateType) {
            case ORDERED_DATE:
                jpql += " AND pi.billItem.bill.createdAt BETWEEN :fd AND :td";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
        }

        if (orderedInstitution != null) {
            jpql += " AND pi.billItem.bill.institution = :orderedInstitution";
            params.put("orderedInstitution", getOrderedInstitution());
        }

        if (orderedDepartment != null) {
            jpql += " AND pi.billItem.bill.toDepartment = :orderedDepartment";
            params.put("orderedDepartment", getOrderedDepartment());
        }

        if (performingInstitution != null) {
            jpql += " AND pi.performDepartment.institution = :performingInstitution";
            params.put("performingInstitution", performingInstitution);
        }

        if (performingDepartment != null) {
            jpql += " AND pi.performDepartment = :performingDepartment";
            params.put("performingDepartment", performingDepartment);
        }

        if (collectionCenter != null) {
            jpql += " AND (pi.billItem.bill.collectingCentre = :collectionCenter OR pi.billItem.bill.fromInstitution = :collectionCenter)";
            params.put("collectionCenter", getCollectionCenter());
        }

        if (route != null) {
            jpql += " AND (pi.billItem.bill.collectingCentre.route = :route OR pi.billItem.bill.fromInstitution.route = :route)";
            params.put("route", getRoute());
        }

        if (priority != null) {
        }

        if (specimen != null) {
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql += " AND pi.billItem.bill.patient.person.name LIKE :patientName";
            params.put("patientName", "%" + getPatientName().trim() + "%");
        }

        if (type != null && !type.trim().isEmpty()) {
            jpql += " AND pi.billItem.bill.ipOpOrCc = :type";
            params.put("type", getType().trim());
        }

        if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
            jpql += " AND pi.billItem.bill.referredByName = :externalDoctor";
            params.put("externalDoctor", getExternalDoctor().trim());
        }

        if (equipment != null) {
            // Add logic if needed
        }

        if (referringDoctor != null) {
            jpql += " AND pi.billItem.bill.referredBy = :referringDoctor";
            params.put("referringDoctor", getReferringDoctor());
        }

        if (investigation != null) {
            // Add logic if needed
        }

        if (department != null) {
            jpql += " AND pi.billItem.bill.toDepartment = :department";
            params.put("department", getDepartment());
        }

        if (patientInvestigationStatus != null) {
            jpql += " AND pi.billItem.bill.status = :status";
            params.put("status", patientInvestigationStatus);
        }

        jpql += " GROUP BY pi.billItem.bill ";

        jpql += " ORDER BY pi.billItem.bill.id DESC";

        params.put("ret", false);

        bills = billFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    public void searchBillsForCourier() {
        listingEntity = ListingEntity.BILLS;
        String jpql;
        Map<String, Object> params = new HashMap<>();
        jpql = "SELECT pi.billItem.bill "
                + " FROM PatientInvestigation pi"
                + " WHERE pi.billItem.bill.retired = :ret";
        if (searchDateType == null) {
            searchDateType = SearchDateType.ORDERED_DATE;
        }
        switch (searchDateType) {
            case ORDERED_DATE:
                jpql += " AND pi.billItem.bill.createdAt BETWEEN :fd AND :td";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
        }

        if (orderedInstitution != null) {
            jpql += " AND pi.billItem.bill.institution = :orderedInstitution";
            params.put("orderedInstitution", getOrderedInstitution());
        }

        if (orderedDepartment != null) {
            jpql += " AND pi.billItem.bill.toDepartment = :orderedDepartment";
            params.put("orderedDepartment", getOrderedDepartment());
        }

        if (collectionCenter != null) {
            jpql += " AND (pi.billItem.bill.collectingCentre = :collectionCenter OR pi.billItem.bill.fromInstitution = :collectionCenter) ";
            params.put("collectionCenter", getCollectionCenter());
        } else {
            if (!sessionController.getLoggableCollectingCentres().isEmpty()) {
                jpql += " AND (pi.billItem.bill.collectingCentre IN :collectionCenters OR pi.billItem.bill.fromInstitution IN :collectionCenters) ";
                params.put("collectionCenters", sessionController.getLoggableCollectingCentres());
            }
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql += " AND pi.billItem.bill.patient.person.name LIKE :patientName";
            params.put("patientName", "%" + getPatientName().trim() + "%");
        }

        if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
            jpql += " AND pi.billItem.bill.referredByName = :externalDoctor";
            params.put("externalDoctor", getExternalDoctor().trim());
        }

        if (referringDoctor != null) {
            jpql += " AND pi.billItem.bill.referredBy = :referringDoctor";
            params.put("referringDoctor", getReferringDoctor());
        }

        if (department != null) {
            jpql += " AND pi.billItem.bill.toDepartment = :department";
            params.put("department", getDepartment());
        }

        if (patientInvestigationStatus != null) {
            jpql += " AND pi.billItem.bill.status = :status";
            params.put("status", patientInvestigationStatus);
        }

        jpql += " GROUP BY pi.billItem.bill ";

        jpql += " ORDER BY pi.billItem.bill.createdAt DESC";

        params.put("ret", false);

        bills = billFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    public void searchPatientReportsFormSelectedBillForCourier(Bill bill) {
        listingEntity = ListingEntity.PATIENT_INVESTIGATIONS;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        // Query PatientSampleComponent to get PatientInvestigations
        jpql = "SELECT i "
                + "FROM PatientInvestigation i "
                + " WHERE i.retired = :ret ";

        jpql += " AND i.billItem.bill =:bill ";
        params.put("bill", bill);

        jpql += " AND i.billItem.bill.cancelled=:cancel ";
        params.put("cancel", false);

        jpql += " ORDER BY i.billItem.bill.patient asc, i.billItem.bill.createdAt desc";

        params.put("ret", false);
        items = getFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    public void searchBillsWithSampleId() {
        listingEntity = ListingEntity.BILLS;
        String jpql;
        Map<String, Object> params = new HashMap<>();
        jpql = "SELECT ps.bill "
                + " FROM PatientSample ps"
                + " join ps.bill b "
                + " WHERE b.retired = :ret "
                + " and ps.id=:sid ";
        params.put("sid", sampleId);

        if (searchDateType == null) {
            searchDateType = SearchDateType.ORDERED_DATE;
        }
        switch (searchDateType) {
            case ORDERED_DATE:
                jpql += " AND b.createdAt BETWEEN :fd AND :td";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
        }

        if (orderedInstitution != null) {
            jpql += " AND b.fromInstitution = :orderedInstitution";
            params.put("orderedInstitution", getOrderedInstitution());
        }

        if (orderedDepartment != null) {
            jpql += " AND b.fromDepartment = :orderedDepartment";
            params.put("orderedDepartment", getOrderedDepartment());
        }

        if (performingInstitution != null) {
            jpql += " AND b.toInstitution = :performingInstitution";
            params.put("performingInstitution", performingInstitution);
        }

        if (performingDepartment != null) {
            jpql += " AND b.toDepartment = :performingDepartment";
            params.put("performingDepartment", performingDepartment);
        }

        if (collectionCenter != null) {
            jpql += " AND (b.collectingCentre = :collectionCenter OR b.fromInstitution = :collectionCenter)";
            params.put("collectionCenter", getCollectionCenter());
        }

        if (route != null) {
            jpql += " AND (b.collectingCentre.route = :route OR b.fromInstitution.route = :route)";
            params.put("route", getRoute());
        }

        if (priority != null) {
        }

        if (specimen != null) {
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql += " AND b.patient.person.name LIKE :patientName";
            params.put("patientName", "%" + getPatientName().trim() + "%");
        }

        if (type != null && !type.trim().isEmpty()) {
            jpql += " AND b.ipOpOrCc = :type";
            params.put("type", getType().trim());
        }

        if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
            jpql += " AND b.referredByName = :externalDoctor";
            params.put("externalDoctor", getExternalDoctor().trim());
        }

        if (equipment != null) {
            // Add logic if needed
        }

        if (referringDoctor != null) {
            jpql += " AND b.referredBy = :referringDoctor";
            params.put("referringDoctor", getReferringDoctor());
        }

        if (investigation != null) {
            // Add logic if needed
        }

        if (department != null) {
            jpql += " AND b.toDepartment = :department";
            params.put("department", getDepartment());
        }

        if (patientInvestigationStatus != null) {
            jpql += " AND b.status = :status";
            params.put("status", patientInvestigationStatus);
        }

        jpql += " GROUP BY b ";

        jpql += " ORDER BY b.id DESC";

        params.put("ret", false);

        bills = billFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    public void searchPatientReportsForCourier() {
        listingEntity = ListingEntity.PATIENT_REPORTS;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "SELECT r "
                + " FROM PatientReport r "
                + " WHERE r.retired = :ret "
                + " AND r.patientInvestigation.billItem.bill.cancelled=:cancel ";

        if (searchDateType == null) {
            searchDateType = SearchDateType.ORDERED_DATE;
        }

        switch (searchDateType) {
            case ORDERED_DATE:
                jpql += " AND r.patientInvestigation.billItem.bill.createdAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case REPORT_AUTHORIZED:
                jpql += " AND r.approveAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case REPORT_PRINTED:
                jpql += " AND r.printingAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            // Add other date types as needed for PatientReport
        }

        if (orderedInstitution != null) {
            jpql += " AND r.patientInvestigation.billItem.bill.institution = :orderedInstitution ";
            params.put("orderedInstitution", getOrderedInstitution());
        }

        if (orderedDepartment != null) {
            jpql += " AND r.patientInvestigation.billItem.bill.department = :orderedDepartment ";
            params.put("orderedDepartment", getOrderedDepartment());
        }

        if (performingInstitution != null) {
            jpql += " AND r.approveInstitution = :performingInstitution ";
            params.put("performingInstitution", getPerformingInstitution());
        }

        if (performingDepartment != null) {
            jpql += " AND r.approveDepartment = :performingDepartment ";
            params.put("performingDepartment", getPerformingDepartment());
        }

        if (collectionCenter != null) {
            jpql += " AND (r.patientInvestigation.billItem.bill.collectingCentre = :collectionCenter OR r.patientInvestigation.billItem.bill.fromInstitution = :collectionCenter) ";
            params.put("collectionCenter", getCollectionCenter());
        } else {
            if (!sessionController.getLoggableCollectingCentres().isEmpty()) {
                jpql += " AND (r.patientInvestigation.billItem.bill.collectingCentre IN :collectionCenters OR r.patientInvestigation.billItem.bill.fromInstitution IN :collectionCenters) ";
                params.put("collectionCenters", sessionController.getLoggableCollectingCentres());
            }
        }
//         jpql += " AND (r.patientInvestigation.billItem.bill.collectingCentre.route = :route OR r.patientInvestigation.billItem.bill.fromInstitution.route = :route) ";
//        params.put("route", sessionController.getDepartment());

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql += " AND r.patientInvestigation.billItem.bill.patient.person.name LIKE :patientName ";
            params.put("patientName", "%" + getPatientName().trim() + "%");
        }

        if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
            jpql += " AND r.patientInvestigation.billItem.bill.referredByName =:externalDoctor ";
            params.put("externalDoctor", getExternalDoctor().trim());
        }

        if (referringDoctor != null) {
            jpql += " AND r.patientInvestigation.billItem.bill.referringDoctor=:referringDoctor ";
            params.put("referringDoctor", getReferringDoctor());
        }

        if (investigation != null) {
            jpql += " AND r.patientInvestigation.investigation=:investigation ";
            params.put("investigation", getInvestigation());
        }

        if (department != null) {
            jpql += " AND r.patientInvestigation.billItem.bill.toDepartment=:department ";
            params.put("department", getDepartment());
        }

        if (patientInvestigationStatus != null) {
            jpql += " AND r.status=:patientReportStatus ";
            params.put("patientReportStatus", patientInvestigationStatus);
        }

        jpql += " ORDER BY r.patientInvestigation.billItem.bill.patient asc, r.patientInvestigation.billItem.bill.createdAt desc";
        params.put("ret", false);
        params.put("cancel", false);
        patientReports = patientReportFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    public void clearData() {
        patientReports = null;
        bills = null;
        patientSamples = null;
        patientSamples = null;

        clearFilters();

    }

    public void searchPatientReports() {
        listingEntity = ListingEntity.PATIENT_REPORTS;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "SELECT r "
                + " FROM PatientReport r "
                + " WHERE r.retired = :ret ";

        if (searchDateType == null) {
            searchDateType = SearchDateType.ORDERED_DATE;
        }

        switch (searchDateType) {
            case ORDERED_DATE:
                jpql += " AND r.patientInvestigation.billItem.bill.createdAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case REPORT_AUTHORIZED:
                jpql += " AND r.approveAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case REPORT_PRINTED:
                jpql += " AND r.printingAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            // Add other date types as needed for PatientReport
        }

        if (orderedInstitution != null) {
            jpql += " AND r.patientInvestigation.billItem.bill.institution = :orderedInstitution ";
            params.put("orderedInstitution", getOrderedInstitution());
        }

        if (orderedDepartment != null) {
            jpql += " AND r.patientInvestigation.billItem.bill.department = :orderedDepartment ";
            params.put("orderedDepartment", getOrderedDepartment());
        }

        if (performingInstitution != null) {
            jpql += " AND r.approveInstitution = :performingInstitution ";
            params.put("performingInstitution", getPerformingInstitution());
        }

        if (performingDepartment != null) {
            jpql += " AND r.approveDepartment = :performingDepartment ";
            params.put("performingDepartment", getPerformingDepartment());
        }

        if (collectionCenter != null) {
            jpql += " AND (r.patientInvestigation.billItem.bill.collectingCentre = :collectionCenter OR r.patientInvestigation.billItem.bill.fromInstitution = :collectionCenter) ";
            params.put("collectionCenter", getCollectionCenter());
        }

        if (route != null) {
            jpql += " AND (r.patientInvestigation.billItem.bill.collectingCentre.route = :route OR r.patientInvestigation.billItem.bill.fromInstitution.route = :route) ";
            params.put("route", getRoute());
        }

        if (priority != null) {
            jpql += " AND r.patientInvestigation.billItem.priority = :priority ";
            params.put("priority", getPriority());
        }

        if (specimen != null) {
            jpql += " AND r.patientInvestigation.investigation.sample = :specimen ";
            params.put("specimen", getSpecimen());
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql += " AND r.patientInvestigation.billItem.bill.patient.person.name LIKE :patientName ";
            params.put("patientName", "%" + getPatientName().trim() + "%");
        }

        if (type != null && !type.trim().isEmpty()) {
            jpql += " AND r.patientInvestigation.billItem.bill.ipOpOrCC = :tp ";
            params.put("tp", getType().trim());
        }

        if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
            jpql += " AND r.patientInvestigation.billItem.bill.referredByName = :externalDoctor ";
            params.put("externalDoctor", getExternalDoctor().trim());
        }

        if (equipment != null) {
            jpql += " AND r.automatedAnalyzer = :equipment ";
            params.put("equipment", getEquipment());
        }

        if (referringDoctor != null) {
            jpql += " AND r.patientInvestigation.billItem.bill.referringDoctor = :referringDoctor ";
            params.put("referringDoctor", getReferringDoctor());
        }

        if (investigationName != null && !investigationName.trim().isEmpty()) {
            jpql += " AND r.patientInvestigation.billItem.item.name like :investigation ";
            params.put("investigation", "%" + investigationName.trim() + "%");
        }

        if (department != null) {
            jpql += " AND r.patientInvestigation.billItem.bill.toDepartment = :department ";
            params.put("department", getDepartment());
        }

        if (patientInvestigationStatus != null) {
            jpql += " AND r.status = :patientReportStatus ";
            params.put("patientReportStatus", patientInvestigationStatus);
        }
        jpql += " ORDER BY r.id DESC";
        params.put("ret", false);
        patientReports = patientReportFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    @Deprecated
    public void searchBillsOld() {
        listingEntity = ListingEntity.BILLS;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "SELECT b FROM Bill b WHERE b.retired = :ret";

        if (searchDateType == null) {
            searchDateType = SearchDateType.ORDERED_DATE;
        }

        switch (searchDateType) {
            case ORDERED_DATE:
                jpql += " AND b.createdAt BETWEEN :fd AND :td";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            // Add other cases if necessary, with appropriate fields from the Bill entity
        }

        if (orderedInstitution != null) {
            jpql += " AND b.institution = :orderedInstitution";
            params.put("orderedInstitution", getOrderedInstitution());
        }

        if (orderedDepartment != null) {
            jpql += " AND b.toDepartment = :orderedDepartment";
            params.put("orderedDepartment", getOrderedDepartment());
        }

        if (performingInstitution != null) {
            jpql += " AND b.toInstitution = :performingInstitution";
            params.put("performingInstitution", performingInstitution);
        }

        if (performingDepartment != null) {
            jpql += " AND b.toDepartment = :performingDepartment";
            params.put("performingDepartment", performingDepartment);
        }

        if (collectionCenter != null) {
            jpql += " AND (b.collectingCentre = :collectionCenter OR b.fromInstitution = :collectionCenter)";
            params.put("collectionCenter", getCollectionCenter());
        }

        if (route != null) {
            jpql += " AND (b.collectingCentre.route = :route OR b.fromInstitution.route = :route)";
            params.put("route", getRoute());
        }

        if (priority != null) {
            // Add logic if needed
        }

        if (specimen != null) {
            // Add logic if needed
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql += " AND b.patient.person.name LIKE :patientName";
            params.put("patientName", "%" + getPatientName().trim() + "%");
        }

        if (type != null && !type.trim().isEmpty()) {
            jpql += " AND b.ipOpOrCc = :type";
            params.put("type", getType().trim());
        }

        if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
            jpql += " AND b.referredByName = :externalDoctor";
            params.put("externalDoctor", getExternalDoctor().trim());
        }

        if (equipment != null) {
            // Add logic if needed
        }

        if (referringDoctor != null) {
            jpql += " AND b.referredBy = :referringDoctor";
            params.put("referringDoctor", getReferringDoctor());
        }

        if (investigation != null) {
            // Add logic if needed
        }

        if (department != null) {
            jpql += " AND b.toDepartment = :department";
            params.put("department", getDepartment());
        }

        if (patientInvestigationStatus != null) {
            // Add logic if needed
        }

        jpql += " ORDER BY b.id DESC";

        params.put("ret", false);

        bills = billFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    @Deprecated
    public void searchBills1() {
        listingEntity = ListingEntity.PATIENT_INVESTIGATIONS;
        String jpql;
        Map params = new HashMap();
        jpql = "SELECT i "
                + " FROM PatientInvestigation i "
                + " where i.retired=:ret  "
                + " and i.billItem.bill.billDate between :fromDate and :toDate ";

        if (orderedInstitution != null) {
            jpql += " and i.billItem.bill.institution=:ins ";
            params.put("ins", getOrderedInstitution());
        }

        if (orderedInstitution != null) {
            jpql += " and i.billItem.bill.department=:dep ";
            params.put("dep", getOrderedDepartment());
        }

        jpql += " order by i.id desc";
        params.put("fromDate", getFromDate());
        params.put("toDate", getToDate());
        params.put("ret", false);
        params.put("bg", false);
        billBarcodes = new ArrayList<>();
        List<PatientInvestigation> pis = getFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);
        billBarcodes = createBilBarcodeObjects(pis);
    }

    public void searchPatientInvestigations() {
        if (sampleId != null && sampleId != 0l) {
            searchPatientInvestigationsWithSampleId();
        } else {
            searchPatientInvestigationsWithoutSampleId();
        }
    }

    public void searchPatientReportsInBillingDepartment() {

        StringBuilder jpql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        jpql.append("SELECT i ")
                .append(" FROM PatientInvestigation i ")
                .append(" WHERE i.retired = :ret ")
                .append(" AND i.billItem.bill.department = :od ");

        params.put("ret", false);
        params.put("od", sessionController.getDepartment());

        searchDateType = SearchDateType.ORDERED_DATE;

        switch (searchDateType) {
            case ORDERED_DATE:
                jpql.append(" AND i.billItem.bill.createdAt BETWEEN :fd AND :td ");
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
        }

        if (collectionCenter != null) {
            jpql.append(" AND (i.billItem.bill.collectingCentre = :cc OR i.billItem.bill.fromInstitution = :cc) ");
            params.put("cc", getCollectionCenter());
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql.append(" AND i.billItem.bill.patient.person.name LIKE :patientName ");
            params.put("patientName", "%" + patientName.trim() + "%");
        }

        if (type != null && !type.trim().isEmpty()) {
            jpql.append(" AND i.billItem.bill.ipOpOrCc=:tp ");
            params.put("tp", type.trim());
        }

        if (referringDoctor != null) {
            jpql.append(" AND i.billItem.bill.referredBy = :rf ");
            params.put("rf", getReferringDoctor());
        }

        if (itemName != null && !itemName.trim().isEmpty()) {
            jpql.append(" AND i.billItem.item.name LIKE :item "); // Ensure correct column name
            params.put("item", "%" + itemName.trim() + "%");
        }

        jpql.append("ORDER BY i.id DESC");

        items = getFacade().findByJpql(jpql.toString(), params, TemporalType.TIMESTAMP);
    }

    public void searchPatientInvestigationsForCourier() {
        String jpql;
        Map<String, Object> params = new HashMap<>();

        // Query PatientSampleComponent to get PatientInvestigations
        jpql = "SELECT i "
                + "FROM PatientInvestigation i "
                + " WHERE i.retired = :ret ";

        params.put("ret", false);

        jpql += " AND i.billItem.bill.cancelled=:cancel ";
        params.put("cancel", false);

        if (searchDateType == null) {
            searchDateType = SearchDateType.ORDERED_DATE;
        }

        switch (searchDateType) {
            case ORDERED_DATE:
                jpql += " AND i.billItem.bill.createdAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case REPORT_AUTHORIZED:
                jpql += " AND i.approveAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case REPORT_PRINTED:
                jpql += " AND i.printingAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_ACCEPTED_DATE:
                jpql += " AND i.sampleAcceptedAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_COLLECTED_DATE:
                jpql += " AND i.sampleCollectedAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_GENERATED_DATE:
                jpql += " AND i.sampleGeneratedAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_SENT_DATE:
                jpql += " AND i.sampleSentAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
        }

        if (orderedInstitution != null) {
            jpql += " AND i.billItem.bill.institution = :orderedInstitution ";
            params.put("orderedInstitution", getOrderedInstitution());
        }

        if (orderedDepartment != null) {
            jpql += " AND i.billItem.bill.department = :orderedDepartment ";
            params.put("orderedDepartment", getOrderedDepartment());
        }

        if (performingInstitution != null) {
            jpql += " AND i.performInstitution = :performingInstitution ";
            params.put("performingInstitution", getPerformingInstitution());
        }

        if (performingDepartment != null) {
            jpql += " AND i.performDepartment = :performingDepartment ";
            params.put("performingDepartment", getPerformingDepartment());
        }

        if (collectionCenter != null) {
            jpql += " AND (i.billItem.bill.collectingCentre =:collectionCenter OR i.billItem.bill.fromInstitution =:collectionCenter) ";
            params.put("collectionCenter", getCollectionCenter());
        } else {
            if (!sessionController.getLoggableCollectingCentres().isEmpty()) {
                jpql += " AND (i.billItem.bill.collectingCentre IN :collectionCenters OR i.billItem.bill.fromInstitution IN :collectionCenters) ";
                params.put("collectionCenters", sessionController.getLoggableCollectingCentres());
            }
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql += " AND i.billItem.bill.patient.person.name LIKE :patientName ";
            params.put("patientName", "%" + getPatientName().trim() + "%");
        }

        if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
            jpql += " AND i.billItem.bill.referredByName = :externalDoctor ";
            params.put("externalDoctor", getExternalDoctor().trim());
        }

        if (referringDoctor != null) {
            jpql += " AND i.billItem.bill.referringDoctor = :referringDoctor ";
            params.put("referringDoctor", getReferringDoctor());
        }

        if (investigation != null) {
            jpql += " AND i.investigation like :investigation ";
            params.put("investigation", " %" + getInvestigation() + "%");
        }

        if (department != null) {
            jpql += " AND i.billItem.bill.toDepartment = :department ";
            params.put("department", getDepartment());
        }

        if (patientInvestigationStatus != null) {
            jpql += " AND i.status = :patientInvestigationStatus ";
            params.put("patientInvestigationStatus", getPatientInvestigationStatus());
        }

        jpql += " ORDER BY i.billItem.bill.patient asc, i.billItem.bill.createdAt desc";

        params.put("ret", false);

        items = getFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    public void searchPatientInvestigationsWithoutSampleId() {
        listingEntity = ListingEntity.PATIENT_INVESTIGATIONS;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "SELECT i "
                + " FROM PatientInvestigation i "
                + " WHERE i.retired = :ret ";

        if (searchDateType == null) {
            searchDateType = SearchDateType.ORDERED_DATE;
        }

        switch (searchDateType) {
            case ORDERED_DATE:
                jpql += " AND i.billItem.bill.createdAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case REPORT_AUTHORIZED:
                jpql += " AND i.approveAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case REPORT_PRINTED:
                jpql += " AND i.printingAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_ACCEPTED_DATE:
                jpql += " AND i.sampleAcceptedAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_COLLECTED_DATE:
                jpql += " AND i.sampleCollectedAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_GENERATED_DATE:
                jpql += " AND i.sampleGeneratedAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_SENT_DATE:
                jpql += " AND i.sampleSentAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
        }

        if (orderedInstitution != null) {
            jpql += " AND i.billItem.bill.institution = :orderedInstitution ";
            params.put("orderedInstitution", getOrderedInstitution());
        }

        if (orderedDepartment != null) {
            jpql += " AND i.billItem.bill.department = :orderedDepartment ";
            params.put("orderedDepartment", getOrderedDepartment());
        }

        if (performingInstitution != null) {
            jpql += " AND i.performInstitution = :peformingInstitution ";
            params.put("peformingInstitution", getPerformingInstitution());
        }

        if (performingDepartment != null) {
            jpql += " AND i.performDepartment = :peformingDepartment ";
            params.put("peformingDepartment", getPerformingDepartment());
        }

        if (collectionCenter != null) {
            jpql += " AND (i.billItem.bill.collectingCentre = :collectionCenter OR i.billItem.bill.fromInstitution = :collectionCenter) ";
            params.put("collectionCenter", getCollectionCenter());
        }

        if (route != null) {
            jpql += " AND (i.billItem.bill.collectingCentre.route = :route OR i.billItem.bill.fromInstitution.route = :route) ";
            params.put("route", getRoute());
        }

        if (priority != null) {
            jpql += " AND i.billItem.priority = :priority ";
            params.put("priority", getPriority());
        }

        if (specimen != null) {
            jpql += " AND i.investigation.sample = :specimen ";
            params.put("specimen", getSpecimen());
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql += " AND i.billItem.bill.patient.person.name LIKE :patientName ";
            params.put("patientName", "%" + getPatientName().trim() + "%");
        }

        if (type != null && !type.trim().isEmpty()) {
            jpql += " AND i.billItem.bill.ipOpOrCC = :tp ";
            params.put("tp", getType().trim());
        }

        if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
            jpql += " AND i.billItem.bill.referredByName = :externalDoctor ";
            params.put("externalDoctor", getExternalDoctor().trim());
        }

        if (equipment != null) {
            jpql += " AND i.investigation.machine = :equipment ";
            params.put("equipment", getEquipment());
        }

        if (referringDoctor != null) {
            jpql += " AND i.billItem.bill.referringDoctor = :referringDoctor ";
            params.put("referringDoctor", getReferringDoctor());
        }

        if (investigationName != null && !investigationName.trim().isEmpty()) {
            jpql += " AND i.billItem.item.name like :investigation ";
            params.put("investigation", "%" + investigationName.trim() + "%");
        }

        if (department != null) {
            jpql += " AND i.billItem.bill.toDepartment = :department ";
            params.put("department", getDepartment());
        }

        if (patientInvestigationStatus != null) {
            jpql += " AND i.status = :patientInvestigationStatus ";
            params.put("patientInvestigationStatus", getPatientInvestigationStatus());
        }

        jpql += " ORDER BY i.id DESC";

        params.put("ret", false);

        items = getFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    public void listPatientInvestigationsForCcs() {
        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "SELECT i "
                + " FROM PatientInvestigation i "
                + " WHERE i.retired = :ret ";

        if (searchDateType == null) {
            searchDateType = SearchDateType.ORDERED_DATE;
        }

        switch (searchDateType) {
            case ORDERED_DATE:
                jpql += " AND i.billItem.bill.createdAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case REPORT_AUTHORIZED:
                jpql += " AND i.approveAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case REPORT_PRINTED:
                jpql += " AND i.printingAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_ACCEPTED_DATE:
                jpql += " AND i.sampleAcceptedAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_COLLECTED_DATE:
                jpql += " AND i.sampleCollectedAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_GENERATED_DATE:
                jpql += " AND i.sampleGeneratedAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_SENT_DATE:
                jpql += " AND i.sampleSentAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
        }

        if (orderedInstitution != null) {
            jpql += " AND i.billItem.bill.institution = :orderedInstitution ";
            params.put("orderedInstitution", getOrderedInstitution());
        }

        if (orderedDepartment != null) {
            jpql += " AND i.billItem.bill.department = :orderedDepartment ";
            params.put("orderedDepartment", getOrderedDepartment());
        }

        if (performingInstitution != null) {
            jpql += " AND i.performInstitution = :peformingInstitution ";
            params.put("peformingInstitution", getPerformingInstitution());
        }

        if (performingDepartment != null) {
            jpql += " AND i.performDepartment = :peformingDepartment ";
            params.put("peformingDepartment", getPerformingDepartment());
        }

        if (collectionCenter != null) {
            jpql += " AND (i.billItem.bill.collectingCentre = :collectionCenter OR i.billItem.bill.fromInstitution = :collectionCenter) ";
            params.put("collectionCenter", getCollectionCenter());
        } else {
            jpql += " AND i.billItem.bill.collectingCentre is not null ";
        }

        if (route != null) {
            jpql += " AND (i.billItem.bill.collectingCentre.route = :route OR i.billItem.bill.fromInstitution.route = :route) ";
            params.put("route", getRoute());
        }

        if (priority != null) {
            jpql += " AND i.billItem.priority = :priority ";
            params.put("priority", getPriority());
        }

        if (specimen != null) {
            jpql += " AND i.investigation.sample = :specimen ";
            params.put("specimen", getSpecimen());
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql += " AND i.billItem.bill.patient.person.name LIKE :patientName ";
            params.put("patientName", "%" + getPatientName().trim() + "%");
        }

        if (type != null && !type.trim().isEmpty()) {
            jpql += " AND i.billItem.bill.ipOpOrCC = :tp ";
            params.put("tp", getType().trim());
        }

        if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
            jpql += " AND i.billItem.bill.referredByName = :externalDoctor ";
            params.put("externalDoctor", getExternalDoctor().trim());
        }

        if (equipment != null) {
            jpql += " AND i.investigation.machine = :equipment ";
            params.put("equipment", getEquipment());
        }

        if (referringDoctor != null) {
            jpql += " AND i.billItem.bill.referringDoctor = :referringDoctor ";
            params.put("referringDoctor", getReferringDoctor());
        }

        if (investigation != null) {
            jpql += " AND i.investigation = :investigation ";
            params.put("investigation", getInvestigation());
        }

        if (department != null) {
            jpql += " AND i.billItem.bill.toDepartment = :department ";
            params.put("department", getDepartment());
        }

        jpql += " AND i.billItem.bill.cancelled<>:can ";
        params.put("can", true);

//        jpql += " AND i.billItem.refunded<>:ref ";
//        params.put("ref", true);
        if (patientInvestigationStatus != null) {
            jpql += " AND i.status = :patientInvestigationStatus ";
            params.put("patientInvestigationStatus", getPatientInvestigationStatus());
        }

        jpql += " ORDER BY i.id DESC";

        params.put("ret", false);

        //System.out.println("params = " + params);
        //System.out.println("jpql = " + jpql);
        items = getFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);

        //System.out.println("items = " + items);
        // Initialize totals
        hospitalFeeTotal = 0.0;
        ccFeeTotal = 0.0;
        staffFeeTotal = 0.0;
        grossFeeTotal = 0.0;
        discountTotal = 0.0;
        netTotal = 0.0;

        List<PatientInvestigation> newItems = new ArrayList<>();

        if (items != null) {
            for (PatientInvestigation pi : items) {
                if (pi.getBillItem() == null) {
                    continue;
                }
                if (pi.getBillItem().isRefunded()) {
                    continue;
                }
                newItems.add(pi);
                hospitalFeeTotal += pi.getBillItem().getHospitalFee();
                ccFeeTotal += pi.getBillItem().getCollectingCentreFee();
                staffFeeTotal += pi.getBillItem().getStaffFee();
                grossFeeTotal += pi.getBillItem().getGrossValue();
                discountTotal += pi.getBillItem().getDiscount();
                netTotal += pi.getBillItem().getNetValue();
            }
        }
        items = newItems;
    }

    public void listBillItemsForCcs() {
        reportTimerController.trackReportExecution(() -> {
            String jpql;
            Map<String, Object> params = new HashMap<>();
            List<BillTypeAtomic> btas = new ArrayList<>();
            btas.add(BillTypeAtomic.CC_BILL);
            btas.add(BillTypeAtomic.CC_BILL_CANCELLATION);
            btas.add(BillTypeAtomic.CC_BILL_REFUND);
            // Starting from BillItem and joining to PatientInvestigation if needed
            jpql = "SELECT b "
                    + " FROM BillItem b "
                    + " LEFT JOIN b.patientInvestigation i "
                    + " WHERE b.retired = :ret ";

            if (searchDateType == null) {
                searchDateType = SearchDateType.ORDERED_DATE;
            }

            switch (searchDateType) {
                case ORDERED_DATE:
                    jpql += " AND b.bill.createdAt BETWEEN :fd AND :td ";
                    break;
                case REPORT_AUTHORIZED:
                    jpql += " AND i.approveAt BETWEEN :fd AND :td ";
                    break;
                case REPORT_PRINTED:
                    jpql += " AND i.printingAt BETWEEN :fd AND :td ";
                    break;
                case SAMPLE_ACCEPTED_DATE:
                    jpql += " AND i.sampleAcceptedAt BETWEEN :fd AND :td ";
                    break;
                case SAMPLE_COLLECTED_DATE:
                    jpql += " AND i.sampleCollectedAt BETWEEN :fd AND :td ";
                    break;
                case SAMPLE_GENERATED_DATE:
                    jpql += " AND i.sampleGeneratedAt BETWEEN :fd AND :td ";
                    break;
                case SAMPLE_SENT_DATE:
                    jpql += " AND i.sampleSentAt BETWEEN :fd AND :td ";
                    break;
            }
            params.put("fd", getFromDate());
            params.put("td", getToDate());

            if (orderedInstitution != null) {
                jpql += " AND b.bill.institution = :orderedInstitution ";
                params.put("orderedInstitution", getOrderedInstitution());
            }

            if (orderedDepartment != null) {
                jpql += " AND b.bill.department = :orderedDepartment ";
                params.put("orderedDepartment", getOrderedDepartment());
            }

            if (performingInstitution != null) {
                jpql += " AND i.performInstitution = :peformingInstitution ";
                params.put("peformingInstitution", getPerformingInstitution());
            }

            if (performingDepartment != null) {
                jpql += " AND i.performDepartment = :peformingDepartment ";
                params.put("peformingDepartment", getPerformingDepartment());
            }

            if (collectionCenter != null) {
                jpql += " AND (b.bill.collectingCentre = :collectionCenter OR b.bill.fromInstitution = :collectionCenter) ";
                params.put("collectionCenter", getCollectionCenter());
            }

            if (route != null) {
                jpql += " AND (b.bill.collectingCentre.route = :route OR b.bill.fromInstitution.route = :route) ";
                params.put("route", getRoute());
            }

            if (priority != null) {
                jpql += " AND b.priority = :priority ";
                params.put("priority", getPriority());
            }

            if (specimen != null) {
                jpql += " AND i.investigation.sample = :specimen ";
                params.put("specimen", getSpecimen());
            }

            if (patientName != null && !patientName.trim().isEmpty()) {
                jpql += " AND b.bill.patient.person.name LIKE :patientName ";
                params.put("patientName", "%" + getPatientName().trim() + "%");
            }

            if (type != null && !type.trim().isEmpty()) {
                jpql += " AND b.bill.ipOpOrCC = :tp ";
                params.put("tp", getType().trim());
            }

            if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
                jpql += " AND b.bill.referredByName = :externalDoctor ";
                params.put("externalDoctor", getExternalDoctor().trim());
            }

            if (equipment != null) {
                jpql += " AND i.investigation.machine = :equipment ";
                params.put("equipment", getEquipment());
            }

            if (referringDoctor != null) {
                jpql += " AND b.bill.referringDoctor = :referringDoctor ";
                params.put("referringDoctor", getReferringDoctor());
            }

            if (investigation != null) {
                jpql += " AND i.investigation = :investigation ";
                params.put("investigation", getInvestigation());
            }

            if (department != null) {
                jpql += " AND b.bill.toDepartment = :department ";
                params.put("department", getDepartment());
            }

            jpql += " AND b.bill.billTypeAtomic in :bts ";
            params.put("bts", btas);

            if (patientInvestigationStatus != null) {
                jpql += " AND i.status = :patientInvestigationStatus ";
                params.put("patientInvestigationStatus", getPatientInvestigationStatus());
            }

            jpql += " ORDER BY b.id DESC";

            params.put("ret", false);

            billItems = billItemFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);

            // Initialize totals
            hospitalFeeTotal = 0.0;
            ccFeeTotal = 0.0;
            staffFeeTotal = 0.0;
            grossFeeTotal = 0.0;
            discountTotal = 0.0;
            netTotal = 0.0;

            if (billItems != null) {
                for (BillItem billItem : billItems) {
                    hospitalFeeTotal += billItem.getHospitalFee();
                    ccFeeTotal += billItem.getCollectingCentreFee();
                    staffFeeTotal += billItem.getStaffFee();
                    grossFeeTotal += billItem.getGrossValue();
                    discountTotal += billItem.getDiscount();
                    netTotal += billItem.getNetValue();
                }
            }
        }, LaboratoryReport.INVESTIGATION_MONTH_END_SUMMARY, sessionController.getLoggedUser());
    }

    public void listBillItemsForLabs() {
        String jpql;
        Map<String, Object> params = new HashMap<>();
        List<BillTypeAtomic> btas = new ArrayList<>();
        btas.add(BillTypeAtomic.OPD_BILL_WITH_PAYMENT);
        btas.add(BillTypeAtomic.OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER);
        btas.add(BillTypeAtomic.OPD_BILL_CANCELLATION);
        btas.add(BillTypeAtomic.OPD_BILL_REFUND);
        btas.add(BillTypeAtomic.OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION);

        btas.add(BillTypeAtomic.PACKAGE_OPD_BILL_WITH_PAYMENT);
        btas.add(BillTypeAtomic.PACKAGE_OPD_BILL_PAYMENT_COLLECTION_AT_CASHIER);
        btas.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION);
        btas.add(BillTypeAtomic.PACKAGE_OPD_BILL_CANCELLATION_DURING_BATCH_BILL_CANCELLATION);
        btas.add(BillTypeAtomic.PACKAGE_OPD_BILL_REFUND);

        // Starting from BillItem and joining to PatientInvestigation if needed
        jpql = "SELECT DISTINCT b "
                + " FROM BillItem b "
                + " LEFT JOIN b.patientInvestigation i "
                + " WHERE b.retired = :ret "
                + " and type(b.item) = :invType";

        if (searchDateType == null) {
            searchDateType = SearchDateType.ORDERED_DATE;
        }

        switch (searchDateType) {
            case ORDERED_DATE:
                jpql += " AND b.bill.createdAt BETWEEN :fd AND :td ";
                break;
            case REPORT_AUTHORIZED:
                jpql += " AND i.approveAt BETWEEN :fd AND :td ";
                break;
            case REPORT_PRINTED:
                jpql += " AND i.printingAt BETWEEN :fd AND :td ";
                break;
            case SAMPLE_ACCEPTED_DATE:
                jpql += " AND i.sampleAcceptedAt BETWEEN :fd AND :td ";
                break;
            case SAMPLE_COLLECTED_DATE:
                jpql += " AND i.sampleCollectedAt BETWEEN :fd AND :td ";
                break;
            case SAMPLE_GENERATED_DATE:
                jpql += " AND i.sampleGeneratedAt BETWEEN :fd AND :td ";
                break;
            case SAMPLE_SENT_DATE:
                jpql += " AND i.sampleSentAt BETWEEN :fd AND :td ";
                break;
        }
        params.put("fd", getFromDate());
        params.put("td", getToDate());
        params.put("invType", Investigation.class);

        if (orderedInstitution != null) {
            jpql += " AND b.bill.institution = :orderedInstitution ";
            params.put("orderedInstitution", getOrderedInstitution());
        }

        if (orderedDepartment != null) {
            jpql += " AND b.bill.department = :orderedDepartment ";
            params.put("orderedDepartment", getOrderedDepartment());
        }

        if (performingInstitution != null) {
            jpql += " AND i.performInstitution = :performInstitution ";
            params.put("performInstitution", getPerformingInstitution());
        }

        if (performingDepartment != null) {
            jpql += " AND i.performDepartment = :peformingDepartment ";
            params.put("peformingDepartment", getPerformingDepartment());
        }

        if (site != null) {
            jpql += " AND i.performDepartment.site = :site ";
            params.put("site", getSite());
        }

        if (peformedSite != null) {
            jpql += " AND i.performDepartment.site = :psite ";
            params.put("psite", getPeformedSite());
        }

        if (orderedSite != null) {
            jpql += " AND b.bill.department.site = :bsite ";
            params.put("bsite", getOrderedSite());
        }

        if (priority != null) {
            jpql += " AND b.priority = :priority ";
            params.put("priority", getPriority());
        }

        if (specimen != null) {
            jpql += " AND i.investigation.sample = :specimen ";
            params.put("specimen", getSpecimen());
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql += " AND b.bill.patient.person.name LIKE :patientName ";
            params.put("patientName", "%" + getPatientName().trim() + "%");
        }

        if (type != null && !type.trim().isEmpty()) {
            jpql += " AND b.bill.ipOpOrCC = :tp ";
            params.put("tp", getType().trim());
        }

        if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
            jpql += " AND b.bill.referredByName = :externalDoctor ";
            params.put("externalDoctor", getExternalDoctor().trim());
        }

        if (equipment != null) {
            jpql += " AND i.investigation.machine = :equipment ";
            params.put("equipment", getEquipment());
        }

        if (referringDoctor != null) {
            jpql += " AND b.bill.referringDoctor = :referringDoctor ";
            params.put("referringDoctor", getReferringDoctor());
        }

        if (investigation != null) {
            jpql += " AND i.investigation = :investigation ";
            params.put("investigation", getInvestigation());
        }

        if (department != null) {
            jpql += " AND b.bill.department = :department ";
            params.put("department", getDepartment());
        }

        jpql += " AND b.bill.billTypeAtomic in :bts ";
        params.put("bts", btas);

        if (patientInvestigationStatus != null) {
            jpql += " AND i.status = :patientInvestigationStatus ";
            params.put("patientInvestigationStatus", getPatientInvestigationStatus());
        }

        jpql += " ORDER BY b.id DESC";

        params.put("ret", false);

        //System.out.println("jpql = " + jpql);
        //System.out.println("params = " + params);
        billItems = billItemFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);

        //System.out.println("billItems = " + billItems);
        // Initialize totals
        hospitalFeeTotal = 0.0;
        ccFeeTotal = 0.0;
        staffFeeTotal = 0.0;
        grossFeeTotal = 0.0;
        discountTotal = 0.0;
        netTotal = 0.0;

        if (billItems != null) {
            for (BillItem billItem : billItems) {
                hospitalFeeTotal += billItem.getHospitalFee();
                ccFeeTotal += billItem.getCollectingCentreFee();
                staffFeeTotal += billItem.getStaffFee();
                grossFeeTotal += billItem.getGrossValue();
                discountTotal += billItem.getDiscount();
                netTotal += billItem.getNetValue();
            }
        }
    }

    public void searchPatientInvestigationsWithSampleId() {
//        System.out.println("searchPatientInvestigations");
        listingEntity = ListingEntity.PATIENT_INVESTIGATIONS;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        // Query PatientSampleComponent to get PatientInvestigations
        jpql = "SELECT i "
                + "FROM PatientSampleComponant psc "
                + " join psc.patientInvestigation i "
                + "WHERE psc.retired = :ret "
                + "AND psc.patientSample.id=:sampleId ";

        params.put("ret", false);
        params.put("sampleId", sampleId);

        // Build JPQL query for PatientInvestigations
        jpql += " and i.retired = :ret ";

        if (searchDateType == null) {
            searchDateType = SearchDateType.ORDERED_DATE;
        }

        switch (searchDateType) {
            case ORDERED_DATE:
                jpql += " AND i.billItem.bill.createdAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case REPORT_AUTHORIZED:
                jpql += " AND i.approveAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case REPORT_PRINTED:
                jpql += " AND i.printingAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_ACCEPTED_DATE:
                jpql += " AND i.sampleAcceptedAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_COLLECTED_DATE:
                jpql += " AND i.sampleCollectedAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_GENERATED_DATE:
                jpql += " AND i.sampleGeneratedAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_SENT_DATE:
                jpql += " AND i.sampleSentAt BETWEEN :fd AND :td ";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
        }

        if (orderedInstitution != null) {
            jpql += " AND i.billItem.bill.institution = :orderedInstitution ";
            params.put("orderedInstitution", getOrderedInstitution());
        }

        if (orderedDepartment != null) {
            jpql += " AND i.billItem.bill.department = :orderedDepartment ";
            params.put("orderedDepartment", getOrderedDepartment());
        }

        if (performingInstitution != null) {
            jpql += " AND i.performInstitution = :performingInstitution ";
            params.put("performingInstitution", getPerformingInstitution());
        }

        if (performingDepartment != null) {
            jpql += " AND i.performDepartment = :performingDepartment ";
            params.put("performingDepartment", getPerformingDepartment());
        }

        if (collectionCenter != null) {
            jpql += " AND (i.billItem.bill.collectingCentre = :collectionCenter OR i.billItem.bill.fromInstitution = :collectionCenter) ";
            params.put("collectionCenter", getCollectionCenter());
        }

        if (route != null) {
            jpql += " AND (i.billItem.bill.collectingCentre.route = :route OR i.billItem.bill.fromInstitution.route = :route) ";
            params.put("route", getRoute());
        }

        if (priority != null) {
            jpql += " AND i.billItem.priority = :priority ";
            params.put("priority", getPriority());
        }

        if (specimen != null) {
            jpql += " AND i.investigation.sample = :specimen ";
            params.put("specimen", getSpecimen());
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql += " AND i.billItem.bill.patient.person.name LIKE :patientName ";
            params.put("patientName", "%" + getPatientName().trim() + "%");
        }

        if (type != null && !type.trim().isEmpty()) {
            jpql += " AND i.billItem.bill.ipOpOrCC = :tp ";
            params.put("tp", getType().trim());
        }

        if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
            jpql += " AND i.billItem.bill.referredByName = :externalDoctor ";
            params.put("externalDoctor", getExternalDoctor().trim());
        }

        if (equipment != null) {
            jpql += " AND i.investigation.machine = :equipment ";
            params.put("equipment", getEquipment());
        }

        if (referringDoctor != null) {
            jpql += " AND i.billItem.bill.referringDoctor = :referringDoctor ";
            params.put("referringDoctor", getReferringDoctor());
        }

        if (investigationName != null && !investigationName.trim().isEmpty()) {
            jpql += " AND i.billItem.item.name like :investigation ";
            params.put("investigation", "%" + investigationName.trim() + "%");
        }

        if (department != null) {
            jpql += " AND i.billItem.bill.toDepartment = :department ";
            params.put("department", getDepartment());
        }

        if (patientInvestigationStatus != null) {
            jpql += " AND i.status = :patientInvestigationStatus ";
            params.put("patientInvestigationStatus", getPatientInvestigationStatus());
        }

        jpql += " ORDER BY i.id DESC";

        params.put("ret", false);

        items = getFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    public void searchPatientSamples() {
//        System.out.println("searchPatientInvestigations");
        listingEntity = ListingEntity.PATIENT_SAMPLES;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "SELECT ps FROM PatientSample ps JOIN ps.bill b WHERE ps.retired = :ret";

        if (searchDateType == null) {
            searchDateType = SearchDateType.ORDERED_DATE;
        }

        switch (searchDateType) {
            case ORDERED_DATE:
                jpql += " AND b.createdAt BETWEEN :fd AND :td";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_ACCEPTED_DATE:
                jpql += " AND ps.sampleReceivedAtLabAt BETWEEN :fd AND :td";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_COLLECTED_DATE:
                jpql += " AND ps.sampleCollectedAt BETWEEN :fd AND :td";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_GENERATED_DATE:
                jpql += " AND ps.createdAt BETWEEN :fd AND :td";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            case SAMPLE_SENT_DATE:
                jpql += " AND ps.sampleSentAt BETWEEN :fd AND :td";
                params.put("fd", getFromDate());
                params.put("td", getToDate());
                break;
            // Add other cases as needed
        }

        if (orderedInstitution != null) {
            jpql += " AND b.institution = :orderedInstitution";
            params.put("orderedInstitution", getOrderedInstitution());
        }

        if (orderedDepartment != null) {
            jpql += " AND b.department = :orderedDepartment";
            params.put("orderedDepartment", getOrderedDepartment());
        }

        if (performingInstitution != null) {
            jpql += " AND ps.institution = :performingInstitution";
            params.put("performingInstitution", getPerformingInstitution());
        }

        if (performingDepartment != null) {
            jpql += " AND ps.department = :performingDepartment";
            params.put("performingDepartment", getPerformingDepartment());
        }

        if (collectionCenter != null) {
            jpql += " AND (b.collectingCentre = :collectionCenter OR b.fromInstitution = :collectionCenter)";
            params.put("collectionCenter", getCollectionCenter());
        }

        if (route != null) {
            jpql += " AND (b.collectingCentre.route = :route OR b.fromInstitution.route = :route)";
            params.put("route", getRoute());
        }

        if (priority != null) {
            jpql += " AND ps.priority = :priority";
            params.put("priority", getPriority());
        }

        if (specimen != null) {
            jpql += " AND ps.sample = :specimen";
            params.put("specimen", getSpecimen());
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            jpql += " AND b.patient.person.name LIKE :patientName";
            params.put("patientName", "%" + getPatientName().trim() + "%");
        }

        if (type != null && !type.trim().isEmpty()) {
            jpql += " AND b.ipOpOrCc = :type";
            params.put("type", getType().trim());
        }

        if (externalDoctor != null && !externalDoctor.trim().isEmpty()) {
            jpql += " AND b.referredByName = :externalDoctor";
            params.put("externalDoctor", getExternalDoctor().trim());
        }

        if (equipment != null) {
            // Add logic if needed
        }

        if (referringDoctor != null) {
            jpql += " AND b.referringDoctor = :referringDoctor";
            params.put("referringDoctor", getReferringDoctor());
        }

        if (investigation != null) {
            // Add logic if needed
        }

        if (department != null) {
            jpql += " AND ps.department = :department";
            params.put("department", getDepartment());
        }

        if (patientInvestigationStatus != null) {
            jpql += " AND ps.status = :status";
            params.put("status", getPatientInvestigationStatus());
        }

        if (sampleSearchStrategy != null) {
            if ("INTERNAL_ONLY".equals(sampleSearchStrategy)) {
                jpql += " AND ps.outsourced = :strategy";
                params.put("strategy", false);
            } else if ("EXTERNAL_ONLY".equals(sampleSearchStrategy)) {
                jpql += " AND ps.outsourced = :strategy";
                params.put("strategy", true);
            }
        }

        if (sampleId != null) {
            jpql += " AND (ps.sampleId like :smpid or ps.id like :smpId) ";
            params.put("smpid", "%" + String.valueOf(sampleId) + "%");
            params.put("smpId", "%" + String.valueOf(sampleId) + "%");
        }

        jpql += " ORDER BY ps.id DESC";

        params.put("ret", false);

        patientSamples = patientSampleFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    public List<PatientSampleComponant> getPatientSampleComponentsByInvestigation(PatientInvestigation patientInvestigation) {
//        System.out.println("patientInvestigation = " + patientInvestigation);
        String jpql = "SELECT psc "
                + " FROM PatientSampleComponant psc "
                + " WHERE psc.retired=:retired "
                + " AND psc.patientInvestigation=:patientInvestigation";
        Map<String, Object> params = new HashMap<>();
        params.put("retired", false);  // Assuming you want only non-retired records
        params.put("patientInvestigation", patientInvestigation);
        List<PatientSampleComponant> pscs = patientSampleComponantFacade.findByJpql(jpql, params);
        return pscs;
    }

    public String getPatientSample() {
        List<PatientSampleComponant> pscs = getPatientSampleComponentsByInvestigation(current);

        if (pscs == null || pscs.isEmpty()) {
            return "Not generated yet.";
        }

        return pscs.stream()
                .map(psc -> psc.getPatientSample() != null ? psc.getPatientSample().getIdStr() : "N/A")
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" | "));
    }

    public List<PatientSample> getPatientSamplesByInvestigation(PatientInvestigation patientInvestigation) {
//        System.out.println("patientInvestigation = " + patientInvestigation);
        String jpql = "SELECT DISTINCT psc.patientSample "
                + "FROM PatientSampleComponant psc "
                + "WHERE psc.retired = :retired "
                + "AND psc.patientInvestigation = :patientInvestigation";
        Map<String, Object> params = new HashMap<>();
        params.put("retired", false);  // Assuming you want only non-retired records
        params.put("patientInvestigation", patientInvestigation);
        List<PatientSample> patientSamples = patientSampleFacade.findByJpql(jpql, params);
        return patientSamples;
    }

    public String getPatientSamplesByInvestigationAsString(PatientInvestigation patientInvestigation) {
        List<PatientSample> patientSamples = getPatientSamplesByInvestigation(patientInvestigation);

        if (patientSamples == null || patientSamples.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (PatientSample ps : patientSamples) {
            sb.append(ps.getSampleId()).append(" ");
        }

        return sb.toString();
    }

    public List<PatientInvestigation> getPatientInvestigationsBySample(PatientSample patientSample) {
//        System.out.println("patientSample = " + patientSample);
        String jpql = "SELECT psc.patientInvestigation "
                + "FROM PatientSampleComponant psc "
                + "WHERE psc.retired = :retired "
                + "AND psc.patientSample = :patientSample";
        Map<String, Object> params = new HashMap<>();
        params.put("retired", false);  // Assuming you want only non-retired records
        params.put("patientSample", patientSample);
//        System.out.println("params = " + params);
        List<PatientInvestigation> patientInvestigations = getFacade().findByJpql(jpql, params);
        return patientInvestigations;
    }

    public void listBillsWithGeneratedBarcodes() {
        String jpql;
        Map params = new HashMap();
        jpql = "SELECT i "
                + " FROM PatientInvestigation i "
                + " where i.retired=:ret  "
                + " and i.barcodeGenerated=:bg "
                + " and i.billItem.bill.billDate between :fromDate and :toDate ";

        if (orderedInstitution != null) {
            jpql += " and i.billItem.bill.institution=:ins ";
            params.put("ins", getOrderedInstitution());
        }

        if (orderedInstitution != null) {
            jpql += " and i.billItem.bill.department=:dep ";
            params.put("dep", getOrderedDepartment());
        }

        jpql += " order by i.id desc";
        params.put("fromDate", getFromDate());
        params.put("toDate", getToDate());
        params.put("ret", false);
        params.put("bg", true);
        billBarcodes = new ArrayList<>();
        List<PatientInvestigation> pis = getFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);
        billBarcodes = createBilBarcodeObjects(pis);
    }

    private List<BillBarcode> createBilBarcodeObjects(List<PatientInvestigation> ptis) {
        if (ptis == null) {
            JsfUtil.addErrorMessage("No Patient Investigations");
            return new ArrayList<>();
        }
        if (ptis.isEmpty()) {
            JsfUtil.addErrorMessage("No Patient Investigations");
            return new ArrayList<>();
        }
        Map<Bill, BillBarcode> billBarcodeMap = new HashMap<>();
        for (PatientInvestigation pi : ptis) {
            if (pi.getBillItem() == null) {
                continue;
            }
            if (pi.getBillItem().getBill() == null) {
                continue;
            }

            Bill b = pi.getBillItem().getBill();
            BillBarcode bb;

            pi.setStatus(PatientInvestigationStatus.SAMPLE_GENERATED);
            pi.setBarcodeGenerated(true);
            pi.setBarcodeGeneratedAt(new Date());
            pi.setBarcodeGeneratedBy(sessionController.getLoggedUser());
            ejbFacade.edit(pi);

            if (billBarcodeMap.containsKey(b)) {
                bb = billBarcodeMap.get(b);
            } else {
                bb = new BillBarcode(b);
                billBarcodeMap.put(b, bb);
            }

            PatientInvestigationWrapper piw = new PatientInvestigationWrapper(pi);
            bb.getPatientInvestigationWrappers().add(piw);
        }

        return new ArrayList<>(billBarcodeMap.values());
    }

    public void listPatientSamples() {
        String jpql = "select ps "
                + " from PatientSample ps"
                + " where ps.createdAt between :fd and :td "
                + " order by ps.id";
        Map m = new HashMap();
        m.put("fd", fromDate);
        m.put("td", toDate);
//        m.put("ins", sessionController.getLoggedUser().getInstitution());
        patientSamples = getPatientSampleFacade().findByJpql(jpql, m, TemporalType.TIMESTAMP);
    }

    private List<SampleDTO> sampleDTOList;

    public void listPatientSamplesDTO() {
        reportTimerController.trackReportExecution(() -> {
            String jpql = "select new com.divudi.core.data.dto.SampleDTO( "
                    + " s.id, s.sampleId, s.createdAt, s.bill.deptId, s.bill.patient.person.name, s.machine.name )"
                    + " from PatientSample s"
                    + " where s.createdAt between :fd and :td "
                    + " order by s.id";
            Map m = new HashMap();
            m.put("fd", getFromDate());
            m.put("td", getToDate());
            sampleDTOList = getPatientSampleFacade().findLightsByJpql(jpql, m, TemporalType.TIMESTAMP);

        }, LaboratoryReport.PATIENT_SAMPLE_REPORT, sessionController.getLoggedUser());

    }

    public String navigateToEditPatientSample() {
        if (currentPatientSample == null) {
            return null;
        }
        return "/lab/patient_sample?faces-redirect=true";
    }

    public void prepareSampleCollectionByRequest() {
        samplingRequestResponse = "#{";
        if (inputBillId == null || inputBillId.trim().equals("")) {
            samplingRequestResponse += "Login=0|Message=Bill Number not entered.}#";
            return;
        }
        if (!getSessionController().loginForRequests(username, password)) {
            samplingRequestResponse += "Login=0|Message=Username or password error.}#";
            return;
        }
        prepareSampleCollection();
        if (getPatientSamples() == null || getPatientSamples().isEmpty()) {
            samplingRequestResponse += "Login=0|";
            samplingRequestResponse += "Bill Not Found. Pease reenter}";
            return;
        }

        samplingRequestResponse += "Login=1";
        String zplTemplate = "^XA\r\n"
                + "^LH170,10\r\n" // was 150
                + "^FO30,20,^ADN,18,10^FD#{header}^FS\r\n" // was 10
                + "^LH170,30\r\n" // was 150
                + "^FO30,10,^BCN,100,Y,N,N^FD#{barcode}^FS\r\n" // was 10
                + "^LH170,155\r\n" // was 150
                + "^FO30,20,^ADN,18,10^FD#{footer}^FS\r\n" // was 10
                + "^XZ\r\n";

        String ptLabel = "";
        Bill tb;
        tb = patientSamples.get(0).getBill();
        String tbis = "";

        samplingRequestResponse += "|message=";

        if (patientSamplesSet.isEmpty()) {
            ptLabel = zplTemplate;
            ptLabel = ptLabel.replace("#{header}", CommonFunctions.shortDate(patientSamples.get(0).getBill().getBillDate())
                    + " "
                    + patientSamples.get(0).getBill().getPatient().getPerson().getName());
            ptLabel = ptLabel.replace("#{barcode}", "" + patientSamples.get(0).getBill().getIdStr());
            List<BillItem> tpiics = patientSamples.get(0).getBill().getBillItems();
            tbis = "";
            for (BillItem i : tpiics) {
                tbis += i.getItem().getName() + ", ";
            }
            tbis = tbis.substring(0, tbis.length() - 2);
            ptLabel = ptLabel.replace("#{footer}", tbis);
            samplingRequestResponse += ptLabel;
        } else {
            for (PatientSample ps : patientSamplesSet) {
                ptLabel = zplTemplate;
                ptLabel = ptLabel.replace("#{header}", CommonFunctions.shortDate(ps.getBill().getBillDate())
                        + " "
                        + ps.getPatient().getPerson().getName());
                ptLabel = ptLabel.replace("#{barcode}", "" + ps.getIdStr());
                List<Item> tpiics = testComponantsForPatientSample(ps);
                tbis = "";
                for (Item i : tpiics) {
                    tbis += i.getName() + ", ";
                }
                if (tbis.length() > 2) {
                    tbis = tbis.substring(0, tbis.length() - 2);
                    ptLabel = ptLabel.replace("#{footer}", tbis);
                    samplingRequestResponse += ptLabel;
                }
            }
        }
        samplingRequestResponse += "}#";
    }

    public void prepareSampleCollection() {
        try {
            String barcodeJson = lims.generateSamplesForInternalUse(inputBillId, sessionController.getLoggedUser());
            // Parse the string into a JSON object
            JSONObject root = new JSONObject(barcodeJson);

            // Get the "Barcodes" JSON array from the JSON object
            JSONArray barcodes = root.getJSONArray("Barcodes");
            String defaultTemplate = "<div style='width: 5cm; height:2.5cm; padding: 8px; border: 1px solid #000; font-family: Arial, sans-serif; font-size: 11px;'>"
                    + "<div style='margin: 2px 0;'>"
                    + " Name: " + "{name} ({age}) <br/>"
                    + " Gender: " + "{sex} <br/>"
                    + " Date: " + "{billDate} <br/>"
                    + "</div>"
                    + "<div style='font-family: \"Libre Barcode 128\", cursive; font-size: 24px; margin: 5px 0;'>" + "{barcode}" + "</div>"
                    + "<div style='margin: 2px 0;'>"
                    + "Tests: " + "{tests} <br/>"
                    + "Ins ID: " + "{insId} <br/>"
                    + "Dept ID: " + "{deptId}" + "</div>"
                    + "</div>";
            String templateAbove = configOptionApplicationController.getLongTextValueByKey("Template for Sample Tube Sticker Printing - Above Barcode", defaultTemplate);
            String templateBelow = configOptionApplicationController.getLongTextValueByKey("Template for Sample Tube Sticker Printing - Below Barcode", "");
            sampleTubeLabels = new ArrayList<>();
            for (int i = 0; i < barcodes.length(); i++) {
                // Get each object in the array and convert it to a string
                JSONObject singleBarcode = barcodes.getJSONObject(i);
                SampleTubeLabel stl = new SampleTubeLabel();
                stl.setTextAboveBarcode(createLabelsFromJsonAndTemplate(templateAbove, singleBarcode));
                stl.setTextBelowBarcode(createLabelsFromJsonAndTemplate(templateBelow, singleBarcode));
                stl.setBarcode(singleBarcode.optString("barcode", "N/A"));
                sampleTubeLabels.add(stl);
            }
        } catch (Exception e) {
            // Handle the exception appropriately

        }
    }

    public String createLabelsFromJsonAndTemplate(String template, JSONObject singleBarcode) {
        // Assuming template string contains placeholders like {name}, {barcode}, etc.
        // You need to replace these placeholders with actual values from the JSON object
        String label = template
                .replace("{name}", singleBarcode.optString("name", "N/A")) // Use 'optString' to handle missing keys
                .replace("{billDate}", singleBarcode.optString("billDate", "N/A"))
                .replace("{sex}", singleBarcode.optString("sex", "N/A"))
                .replace("{age}", singleBarcode.optString("age", "N/A"))
                .replace("{barcode}", singleBarcode.optString("barcode", "N/A"))
                .replace("{tests}", singleBarcode.optString("tests", "N/A"))
                .replace("{insId}", singleBarcode.optString("insid", "N/A")) // Assumed JSON key as 'insid'
                .replace("{deptId}", singleBarcode.optString("deptid", "N/A"));

        return label;
    }

    public Long stringToLong(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Deprecated
    public List<Bill> prepareSampleCollectionByBillId(Long bill) {
        Bill b = getBillFacade().find(bill);
        List<Bill> bs = billController.validBillsOfBatchBill(b.getBackwardReferenceBill());
        if (bs == null || bs.isEmpty()) {
            JsfUtil.addErrorMessage("Can not find the bill. Please recheck.");
            return null;
        }
        return bs;
    }

    @Deprecated
    public List<Bill> prepareSampleCollectionByBillNumber(String insId) {
        String j = "Select b from Bill b where b.insId=:id";
        Map m = new HashMap();
        m.put("id", insId);
        Bill b = getBillFacade().findFirstByJpql(j, m);
        List<Bill> bs = billController.validBillsOfBatchBill(b.getBackwardReferenceBill());
        if (bs == null || bs.isEmpty()) {
            JsfUtil.addErrorMessage("Can not find the bill. Please recheck.");
            return null;
        }
        return bs;
    }

    public void temUpdatePatientSamplesToMatchCurrentDepartmentAndInstitution() {
        List<PatientSample> pss = getPatientSampleFacade().findAll();
        for (PatientSample ps : pss) {
            ps.setSampleCollectedDepartment(sessionController.getLoggedUser().getDepartment());
            ps.setSampleCollectedInstitution(sessionController.getLoggedUser().getInstitution());
            ps.setSampleCollectedAt(ps.getCreatedAt());
            ps.setSampleCollecter(ps.getCreater());
            getPatientSampleFacade().edit(ps);
        }
    }

    @Deprecated
    public void prepareSampleCollectionByBills(List<Bill> bills) {
        String j = "";
        Map m;
        patientSamplesSet = new HashSet<>();
        patientSamples = null;
        for (Bill b : bills) {
            m = new HashMap();
            m.put("can", false);
            m.put("bill", b);
            j = "Select pi from PatientInvestigation pi "
                    + " where pi.cancelled=:can "
                    + " and pi.billItem.bill=:bill";
            List<PatientInvestigation> pis = getFacade().findByJpql(j, m);
            for (PatientInvestigation ptix : pis) {
                Investigation ix = ptix.getInvestigation();

                ptix.setCollected(true);
                ptix.setSampleCollecter(getSessionController().getLoggedUser());
                ptix.setSampleDepartment(getSessionController().getLoggedUser().getDepartment());
                ptix.setSampleInstitution(getSessionController().getLoggedUser().getInstitution());
                ptix.setSampledAt(new Date());
                getFacade().edit(ptix);

                List<InvestigationItem> ixis = investigationItemController.getItems(ix);
                for (InvestigationItem ixi : ixis) {
                    if (ixi.getIxItemType() == InvestigationItemType.Value || ixi.getIxItemType() == InvestigationItemType.Template) {
                        j = "select ps from PatientSample ps "
                                + " where ps.tube=:tube "
                                + " and ps.sample=:sample "
                                + " and ps.machine=:machine "
                                + " and ps.patient=:pt "
                                + " and ps.bill=:bill "
                                + " and ps.collected=:ca";
                        m = new HashMap();
                        m.put("tube", ixi.getTube());
                        m.put("sample", ixi.getSample());
                        m.put("machine", ixi.getMachine());
                        m.put("pt", b.getPatient());
                        m.put("bill", b);
                        m.put("ca", false);
                        if (ix.isHasMoreThanOneComponant()) {
                            j += " and ps.investigationComponant=:sc ";
                            m.put("sc", ixi.getSampleComponent());
                        }

                        PatientSample pts = getPatientSampleFacade().findFirstByJpql(j, m);
                        if (pts == null) {
                            pts = new PatientSample();
                            pts.setTube(ixi.getTube());
                            pts.setSample(ixi.getSample());
                            if (ix.isHasMoreThanOneComponant()) {
                                pts.setInvestigationComponant(ixi.getSampleComponent());
                            }
                            pts.setMachine(ixi.getMachine());
                            pts.setPatient(b.getPatient());
                            pts.setBill(b);
                            pts.setSampleCollectedDepartment(sessionController.getLoggedUser().getDepartment());
                            pts.setSampleCollectedInstitution(sessionController.getLoggedUser().getInstitution());
                            pts.setSampleCollecter(sessionController.getLoggedUser());
                            pts.setSampleCollectedAt(new Date());
                            pts.setCreatedAt(new Date());
                            pts.setCreater(sessionController.getLoggedUser());
                            pts.setSampleCollected(false);
                            pts.setReadyTosentToAnalyzer(false);
                            pts.setSentToAnalyzer(false);
                            getPatientSampleFacade().create(pts);
                        }
                        patientSamplesSet.add(pts);

                        PatientSampleComponant ptsc;
                        j = "select ps from PatientSampleComponant ps "
                                + " where ps.patientSample=:pts "
                                + " and ps.bill=:bill "
                                + " and ps.patient=:pt "
                                + " and ps.patientInvestigation=:ptix "
                                + " and ps.investigationComponant=:ixc";
                        m = new HashMap();
                        m.put("pts", pts);
                        m.put("bill", b);
                        m.put("pt", b.getPatient());
                        m.put("ptix", ptix);
                        m.put("ixc", ixi.getSampleComponent());
                        ptsc = getPatientSampleComponantFacade().findFirstByJpql(j, m);
                        if (ptsc == null) {
                            ptsc = new PatientSampleComponant();
                            ptsc.setPatientSample(pts);
                            ptsc.setBill(b);
                            ptsc.setPatient(b.getPatient());
                            ptsc.setPatientInvestigation(ptix);
                            ptsc.setInvestigationComponant(ixi.getSampleComponent());
                            ptsc.setCreatedAt(new Date());
                            ptsc.setCreater(sessionController.getLoggedUser());
                            getPatientSampleComponantFacade().create(ptsc);
                        }
                    }
                }
            }
        }
    }

    public List<Item> testComponantsForPatientSample(PatientSample ps) {
        if (ps == null) {
            return new ArrayList<>();
        }
        List<Item> ts = new ArrayList<>();
        Map m = new HashMap();
        String j = "select ps.investigationComponant from PatientSampleComponant ps "
                + " where ps.patientSample=:pts "
                + " group by ps.investigationComponant";
        m = new HashMap();
        m.put("pts", ps);

        ts = getItemFacade().findByJpql(j, m);
        return ts;
    }

    public List<Item> getCurrentReportComponants(Investigation ix) {
        if (ix == null) {
            JsfUtil.addErrorMessage("Select an investigation");
            return null;
        }
        String j = "select i from Item i where i.itemType=:t and i.parentItem=:m and i.retired=:r order by i.name";
        Map m = new HashMap();
        m.put("t", ItemType.SampleComponent);
        m.put("r", false);
        m.put("m", ix);
        return getItemFacade().findByJpql(j, m);
    }

    private PatientInvestigation patientInvestigationOfBillComponant(List<PatientInvestigation> bcs, BillComponent bc) {
        for (PatientInvestigation pi : bcs) {
            if (pi.getBillComponent() == bc) {
                return pi;
            }
        }
        return null;
    }

    private List<BillComponent> billItemComponents(BillItem bi, Set<BillComponent> bcs) {
        List<BillComponent> bs = new ArrayList<>();
        for (BillComponent b : bcs) {
            if (b.getBillItem() == bi) {
                bs.add(b);
            }
        }
        return bs;
    }

    private List<Bill> patientBills(Patient pt, Set<Bill> bills) {
        List<Bill> bs = new ArrayList<>();
        for (Bill b : bills) {
            if (b.getPatient() == pt) {
                bs.add(b);
            }
        }
        return bs;
    }

    public void prepareSampled() {
        String temSql;
        Map temMap = new HashMap();
        temSql = "SELECT i FROM PatientInvestigation i where i.retired=false  and i.collected = true and i.billItem.bill.billDate between :fromDate and :toDate";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        lstToSamle = getFacade().findByJpql(temSql, temMap, TemporalType.TIMESTAMP);
        checkRefundBillItems(lstToSamle);
    }

    public void listPatientInvestigationsWhereSamplingCompleting() {
        String temSql;
        Map temMap = new HashMap();
        temSql = "SELECT i FROM PatientInvestigation i where i.retired=false  and i.collected = true and i.billItem.bill.billDate between :fromDate and :toDate";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        lstForSampleManagement = getFacade().findByJpql(temSql, temMap, TemporalType.TIMESTAMP);
    }

    public void listBarcodeGeneratedPatientSamplesYetToCollect() {
        patientSamples = findGeneratedSamplesYetToCollect();
    }

    public void listBarcodeGeneratedPatientSamplesCollected() {
        patientSamples = findGeneratedSamplesCollected();
    }

    public List<PatientSample> findGeneratedSamplesYetToCollect() {
        return findGeneratedSamples(true, false, fromDate, toDate);
    }

    public List<PatientSample> findGeneratedSamplesCollected() {
        return findGeneratedSamples(true, true, fromDate, toDate);
    }

    public List<PatientSample> findGeneratedSamples(boolean barcodeGenerated, boolean sampleCollected, Date fromDate, Date toDate) {
        List<PatientSample> pss;
        Map params = new HashMap<>();
        String jpql = "SELECT ps FROM PatientSample ps WHERE ps.retired=:ret AND ps.barcodeGenerated=:bg AND ps.sampleCollected=:sc AND ps.barcodeGeneratedAt between :fromDate and :toDate";
        params.put("ret", false);
        params.put("bg", barcodeGenerated);
        params.put("sc", sampleCollected);
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);
//        System.out.println("jpql = " + jpql);
        pss = patientSampleFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);
        return pss;
    }

    public void listComplletedSamples() {
        String jpql;
        Map params = new HashMap();
        jpql = "SELECT i "
                + "FROM PatientSample i "
                + " where i.retired=false "
                + " and i.collected = true "
                + " and i.billItem.bill.billDate between :fromDate and :toDate";
        params.put("toDate", getToDate());
        params.put("fromDate", getFromDate());
        lstForSampleManagement = getFacade().findByJpql(jpql, params, TemporalType.TIMESTAMP);
    }

    public void checkRefundBillItems(List<PatientInvestigation> pis) {
        for (PatientInvestigation pi : pis) {
            markRefundBillItem(pi);
        }
    }

    public void markRefundBillItem(PatientInvestigation pi) {
        String sql;
        Map m = new HashMap();
        sql = "select bi from BillItem bi "
                + " where bi.referanceBillItem.id=:bi ";
        m.put("bi", pi.getBillItem().getId());
        List<BillItem> bis = billItemFacade.findByJpql(sql, m);
        if (bis.isEmpty()) {
            pi.getBillItem().setTransRefund(false);
        } else {
            pi.getBillItem().setTransRefund(true);
        }
    }

    List<PatientInvestigation> toReceive;

    public List<PatientInvestigation> getToReceive() {
        return toReceive;
    }

    public void toPrintWorksheets() {
        Date startTime = new Date();

        String temSql;
        Map temMap = new HashMap();
        temSql = "SELECT i FROM PatientInvestigation i where "
                + " i.retired=false and i.collected = true "
                + " and (i.received=false or i.received is null) and i.sampledAt between :fromDate "
                + " and :toDate and i.receiveDepartment =:d "
                + " order by i.id";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("d", getSessionController().getDepartment());
        toReceive = getFacade().findByJpql(temSql, temMap, TemporalType.TIMESTAMP);

    }

    public void markYetToReceiveOnes() {
        selectedToReceive = new ArrayList<>();
        for (PatientInvestigation pi : lstToReceiveSearch) {
            if (pi.getReceived() != true) {
                selectedToReceive.add(pi);
            }
        }
    }

    public void markAsReceived() {

        if (getCurrent().getId() != null || getCurrent().getId() != 0) {
            getCurrent().setReceived(Boolean.TRUE);
            getCurrent().setReceivedAt(new Date());
            getCurrent().setReceivedCollecter(getSessionController().getLoggedUser());
            getEjbFacade().edit(getCurrent());
        }

        getLabReportSearchByInstitutionController().createPatientInvestigaationList();
    }

    private List<PatientReportItemValue> column1AntibioticList;
    private List<PatientReportItemValue> column2AntibioticList;

    public List<PatientReportItemValue> findAntibioticForMicrobiologyReport(List<PatientReportItemValue> ptivList) {
        List<PatientReportItemValue> antibioticItems = new ArrayList<>();
        column1AntibioticList = new ArrayList<>();
        column2AntibioticList = new ArrayList<>();

        for (PatientReportItemValue ptiv : ptivList) {
            if (ptiv.getInvestigationItem().getIxItemType() == InvestigationItemType.Antibiotic && !ptiv.getInvestigationItem().isRetired()) {
                if (ptiv.getStrValue() != null) {
                    if (!"".equals(ptiv.getStrValue())) {
                        antibioticItems.add(ptiv);
                    }
                }
            }
        }

        for (int i = 0; i < antibioticItems.size(); i++) {
            if (i % 2 == 0) {
                column1AntibioticList.add(antibioticItems.get(i));
            } else {
                column2AntibioticList.add(antibioticItems.get(i));
            }
        }

//        System.out.println("Antibiotic = " + antibioticItems.size());
//        System.out.println("Column 1 = " + column1AntibioticList.size());
//        System.out.println("Column 2 = " + column2AntibioticList.size());
        return antibioticItems;
    }

    public void markSelectedAsReceived() {
        for (PatientInvestigation pi : getSelectedToReceive()) {
            pi.setReceived(Boolean.TRUE);
            pi.setReceivedAt(new Date());
            pi.setReceivedCollecter(getSessionController().getLoggedUser());
            getEjbFacade().edit(pi);
        }
        selectedToReceive = new ArrayList<>();
        listIncludingReceived = false;
        toReceive = null;
        toPrintWorksheets();
    }

    public void setLstToReceive(List<PatientInvestigation> lstToReceive) {
        this.lstToReceive = lstToReceive;
    }

    public List<PatientInvestigation> getLstToEnterData() {
        if (lstToEnterData == null) {
            String temSql;
            Map temMap = new HashMap();
            if (listIncludingEnteredData) {
                temSql = "SELECT i FROM PatientInvestigation i where i.retired=false and i.received=true and i.dataEntered between :fromDate and :toDate and i.performDepartment.id = " + getSessionController().getDepartment().getId();
            } else {
                temSql = "SELECT i FROM PatientInvestigation i where i.retired=false and i.received=true and i.dataEntered=false  and i.sampledAt between :fromDate and :toDate and i.performDepartment.id = " + getSessionController().getDepartment().getId();
            }
            temMap.put("toDate", getToDate());
            temMap.put("fromDate", getFromDate());
            lstToEnterData = getFacade().findByJpql(temSql, temMap, TemporalType.TIMESTAMP);
        }
        if (lstToEnterData == null) {
            lstToEnterData = new ArrayList<PatientInvestigation>();
        }
        return lstToEnterData;
    }

    public void markAsDataEntered() {
        if (getCurrent().getId() != null || getCurrent().getId() != 0) {
            getCurrent().setDataEntered(Boolean.TRUE);
            getCurrent().setDataEntryAt(new Date());
            getCurrent().setDataEntryUser(getSessionController().getLoggedUser());
            getEjbFacade().edit(getCurrent());
        }
    }

    public void setLstToEnterData(List<PatientInvestigation> lstToEnterData) {
        this.lstToEnterData = lstToEnterData;
    }

    public List<PatientReport> getLstToApprove() {
        if (lstToApprove == null) {
            String temSql;
            if (listIncludingApproved == true) {
                temSql = "SELECT i FROM PatientReport i where i.retired=false and i.dataEntered=true and i.dataEntryAt between :fromDate and :toDate";
            } else {
                temSql = "SELECT i FROM PatientReport i where i.retired=false and i.dataEntered=true and i.approved=false and i.dataEntryAt between :fromDate and :toDate";
            }
            Map temMap = new HashMap();
            temMap.put("toDate", getToDate());
            temMap.put("fromDate", getFromDate());
            lstToApprove = getPrFacade().findByJpql(temSql, temMap, TemporalType.TIMESTAMP);
        }
        if (lstToApprove == null) {
            lstToApprove = new ArrayList<PatientReport>();
        }
        return lstToApprove;
    }

    public void markAsApproved() {
        if (getCurrent().getId() != null || getCurrent().getId() != 0) {
            getCurrent().setApproved(Boolean.TRUE);
            getCurrent().setApproveUser(getSessionController().getLoggedUser());
            getCurrent().setApproveAt(new Date());
            getEjbFacade().edit(getCurrent());
        }
    }

    public void setLstToApprove(List<PatientReport> lstToApprove) {
        this.lstToApprove = lstToApprove;
    }

    public List<PatientReport> getLstToPrint() {

        String temSql;
        temSql = "SELECT i FROM PatientReport i";
        lstToPrint = getPrFacade().findByJpql(temSql);

        if (lstToPrint == null) {
            lstToPrint = new ArrayList<PatientReport>();
        }
        return lstToPrint;
    }

    public void markAsPrinted() {
        if (getCurrent().getId() != null || getCurrent().getId() != 0) {
            getCurrent().setPrinted(Boolean.TRUE);
            getCurrent().setPrintingUser(getSessionController().getLoggedUser());
            getCurrent().setPrintingAt(new Date());
            getEjbFacade().edit(getCurrent());
        }
    }

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(new Date());
            current.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage("Deleted Successfully");
        } else {
            JsfUtil.addSuccessMessage("Nothing to Delete");
        }
        recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    public void setLstToPrint(List<PatientReport> lstToPrint) {
        this.lstToPrint = lstToPrint;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public InvestigationFacade getInvestFacade() {
        return investFacade;
    }

    public void setInvestFacade(InvestigationFacade investFacade) {
        this.investFacade = investFacade;
    }

    public boolean isListIncludingSampled() {
        return listIncludingSampled;
    }

    public void setListIncludingSampled(boolean listIncludingSampled) {
        this.listIncludingSampled = listIncludingSampled;

    }

    public InstitutionLabSumeryController getLabReportSearchByInstitutionController() {
        return labReportSearchByInstitutionController;
    }

    public void setLabReportSearchByInstitutionController(InstitutionLabSumeryController labReportSearchByInstitutionController) {
        this.labReportSearchByInstitutionController = labReportSearchByInstitutionController;
    }

    public SmsFacade getSmsFacade() {
        return smsFacade;
    }

    public void setSmsFacade(SmsFacade smsFacade) {
        this.smsFacade = smsFacade;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public Sms getSms() {
        return sms;
    }

    public void setSms(Sms sms) {
        this.sms = sms;
    }

    public InvestigationItemController getInvestigationItemController() {
        return investigationItemController;
    }

    public PatientSampleFacade getPatientSampleFacade() {
        return patientSampleFacade;
    }

    public BillController getBillController() {
        return billController;
    }

    public void setBillController(BillController billController) {
        this.billController = billController;
    }

    public String getInputBillId() {
        return inputBillId;
    }

    public void setInputBillId(String inputBillId) {
        this.inputBillId = inputBillId;
    }

    public Set<PatientSample> getPatientSamplesSet() {
        return patientSamplesSet;
    }

    public void setPatientSamplesSet(Set<PatientSample> patientSamplesSet) {
        this.patientSamplesSet = patientSamplesSet;
    }

    public List<PatientSample> getPatientSamples() {
        if (patientSamples == null) {
            patientSamples = new ArrayList<>();
            if (patientSamplesSet != null && !patientSamplesSet.isEmpty()) {
                patientSamples.addAll(patientSamplesSet);
            }
        }
        return patientSamples;
    }

    public void setPatientSamples(List<PatientSample> patientSamples) {
        this.patientSamples = patientSamples;
    }

    public String getSamplingRequestResponse() {
        return samplingRequestResponse;
    }

    public void setSamplingRequestResponse(String samplingRequestResponse) {
        this.samplingRequestResponse = samplingRequestResponse;
    }

    public void setPatientSampleComponantFacade(PatientSampleComponantFacade patientSampleComponantFacade) {
        this.patientSampleComponantFacade = patientSampleComponantFacade;
    }

    public void setPatientSampleFacade(PatientSampleFacade patientSampleFacade) {
        this.patientSampleFacade = patientSampleFacade;
    }

    public void setInvestigationItemController(InvestigationItemController investigationItemController) {
        this.investigationItemController = investigationItemController;
    }

    public PatientSampleComponantFacade getPatientSampleComponantFacade() {
        return patientSampleComponantFacade;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMachine() {
        return machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public String getApiResponse() {
        return apiResponse;
    }

    public void setApiResponse(String apiResponse) {
        this.apiResponse = apiResponse;
    }

    public PatientReportController getPatientReportController() {
        return patientReportController;
    }

    public void setPatientReportController(PatientReportController patientReportController) {
        this.patientReportController = patientReportController;
    }

    public ItemForItemController getItemForItemController() {
        return itemForItemController;
    }

    public void setItemForItemController(ItemForItemController itemForItemController) {
        this.itemForItemController = itemForItemController;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getShift1() {
        return shift1;
    }

    public void setShift1(String shift1) {
        this.shift1 = shift1;
    }

    public String getShift2() {
        return shift2;
    }

    public void setShift2(String shift2) {
        this.shift2 = shift2;
    }

    public SmsManagerEjb getSmsManagerEjb() {
        return smsManagerEjb;
    }

    public void setSmsManagerEjb(SmsManagerEjb smsManagerEjb) {
        this.smsManagerEjb = smsManagerEjb;
    }

    public SmsController getSmsController() {
        return smsController;
    }

    public List<PatientReportItemValue> getPatientReportItemValues() {
        return patientReportItemValues;
    }

    public void setPatientReportItemValues(List<PatientReportItemValue> patientReportItemValues) {
        this.patientReportItemValues = patientReportItemValues;
    }

    public PatientReportItemValueFacade getPatientReportItemValueFacade() {
        return patientReportItemValueFacade;
    }

    public int getActiveIndexOfManageInvestigation() {
        return activeIndexOfManageInvestigation;
    }

    public void setActiveIndexOfManageInvestigation(int activeIndexOfManageInvestigation) {
        this.activeIndexOfManageInvestigation = activeIndexOfManageInvestigation;
    }

    public List<SampleTubeLabel> getSampleTubeLabels() {
        return sampleTubeLabels;
    }

    public void setSampleTubeLabels(List<SampleTubeLabel> sampleTubeLabels) {
        this.sampleTubeLabels = sampleTubeLabels;
    }

    public Lims getLims() {
        return lims;
    }

    public void setLims(Lims lims) {
        this.lims = lims;
    }

    public List<PatientInvestigation> getLstForSampleManagement() {
        return lstForSampleManagement;
    }

    public void setLstForSampleManagement(List<PatientInvestigation> lstForSampleManagement) {
        this.lstForSampleManagement = lstForSampleManagement;
    }

    public List< BillBarcode> getBillBarcodes() {
        return billBarcodes;
    }

    public void setBillBarcodes(List< BillBarcode> billBarcodes) {
        this.billBarcodes = billBarcodes;
    }

    public List<BillBarcode> getSelectedBillBarcodes() {
        return selectedBillBarcodes;
    }

    public void setSelectedBillBarcodes(List<BillBarcode> selectedBillBarcodes) {
        this.selectedBillBarcodes = selectedBillBarcodes;
    }

    public Institution getOrderedInstitution() {
        return orderedInstitution;
    }

    public void setOrderedInstitution(Institution orderedInstitution) {
        this.orderedInstitution = orderedInstitution;
    }

    public Department getOrderedDepartment() {
        return orderedDepartment;
    }

    public void setOrderedDepartment(Department orderedDepartment) {
        this.orderedDepartment = orderedDepartment;
    }

    public Institution getPerformingInstitution() {
        return performingInstitution;
    }

    public void setPerformingInstitution(Institution performingInstitution) {
        this.performingInstitution = performingInstitution;
    }

    public Department getPerformingDepartment() {
        return performingDepartment;
    }

    public void setPerformingDepartment(Department performingDepartment) {
        this.performingDepartment = performingDepartment;
    }

    public SearchDateType getSearchDateType() {
        return searchDateType;
    }

    public void setSearchDateType(SearchDateType searchDateType) {
        this.searchDateType = searchDateType;
    }

    public PatientInvestigationStatus getPatientInvestigationStatus() {
        return patientInvestigationStatus;
    }

    public void setPatientInvestigationStatus(PatientInvestigationStatus patientInvestigationStatus) {
        this.patientInvestigationStatus = patientInvestigationStatus;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public BillBeanController getBillBeanController() {
        return billBeanController;
    }

    public void setBillBeanController(BillBeanController billBeanController) {
        this.billBeanController = billBeanController;
    }

    public ConfigOptionApplicationController getConfigOptionApplicationController() {
        return configOptionApplicationController;
    }

    public void setConfigOptionApplicationController(ConfigOptionApplicationController configOptionApplicationController) {
        this.configOptionApplicationController = configOptionApplicationController;
    }

    public Institution getCollectionCenter() {
        return collectionCenter;
    }

    public void setCollectionCenter(Institution collectionCenter) {
        this.collectionCenter = collectionCenter;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Sample getSpeciman() {
        return speciman;
    }

    public void setSpeciman(Sample speciman) {
        this.speciman = speciman;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public Long getFromId() {
        return fromId;
    }

    public void setFromId(Long fromId) {
        this.fromId = fromId;
    }

    public Long getToId() {
        return toId;
    }

    public void setToId(Long toId) {
        this.toId = toId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Sample getSpecimen() {
        return specimen;
    }

    public void setSpecimen(Sample specimen) {
        this.specimen = specimen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExternalDoctor() {
        return externalDoctor;
    }

    public void setExternalDoctor(String externalDoctor) {
        this.externalDoctor = externalDoctor;
    }

    public Machine getEquipment() {
        return equipment;
    }

    public void setEquipment(Machine equipment) {
        this.equipment = equipment;
    }

    public Staff getReferringDoctor() {
        return referringDoctor;
    }

    public void setReferringDoctor(Staff referringDoctor) {
        this.referringDoctor = referringDoctor;
    }

    public Investigation getInvestigation() {
        return investigation;
    }

    public void setInvestigation(Investigation investigation) {
        this.investigation = investigation;
    }

    public ListingEntity getListingEntity() {
        if (listingEntity == null) {
            listingEntity = ListingEntity.PATIENT_INVESTIGATIONS;
        }
        return listingEntity;
    }

    public void setListingEntity(ListingEntity listingEntity) {
        this.listingEntity = listingEntity;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public List<Bill> getSelectedBills() {
        return selectedBills;
    }

    public void setSelectedBills(List<Bill> selectedBills) {
        this.selectedBills = selectedBills;
    }

    public List<PatientSample> getSelectedPatientSamples() {
        return selectedPatientSamples;
    }

    public void setSelectedPatientSamples(List<PatientSample> selectedPatientSamples) {
        this.selectedPatientSamples = selectedPatientSamples;
    }

    public List<PatientReport> getPatientReports() {
        return patientReports;
    }

    public void setPatientReports(List<PatientReport> patientReports) {
        this.patientReports = patientReports;
    }

    public List<PatientReport> getSelectedPatientReports() {
        return selectedPatientReports;
    }

    public void setSelectedPatientReports(List<PatientReport> selectedPatientReports) {
        this.selectedPatientReports = selectedPatientReports;
    }

    public Staff getSampleTransportedToLabByStaff() {
        return sampleTransportedToLabByStaff;
    }

    public void setSampleTransportedToLabByStaff(Staff sampleTransportedToLabByStaff) {
        this.sampleTransportedToLabByStaff = sampleTransportedToLabByStaff;
    }

    public String getSampleRejectionComment() {
        return sampleRejectionComment;
    }

    public void setSampleRejectionComment(String sampleRejectionComment) {
        this.sampleRejectionComment = sampleRejectionComment;
    }

    public String getTestDetails() {
        return testDetails;
    }

    public void setTestDetails(String testDetails) {
        this.testDetails = testDetails;
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public Double getHospitalFeeTotal() {
        return hospitalFeeTotal;
    }

    public void setHospitalFeeTotal(Double hospitalFeeTotal) {
        this.hospitalFeeTotal = hospitalFeeTotal;
    }

    public Double getCcFeeTotal() {
        return ccFeeTotal;
    }

    public void setCcFeeTotal(Double ccFeeTotal) {
        this.ccFeeTotal = ccFeeTotal;
    }

    public Double getStaffFeeTotal() {
        return staffFeeTotal;
    }

    public void setStaffFeeTotal(Double staffFeeTotal) {
        this.staffFeeTotal = staffFeeTotal;
    }

    public Double getGrossFeeTotal() {
        return grossFeeTotal;
    }

    public void setGrossFeeTotal(Double grossFeeTotal) {
        this.grossFeeTotal = grossFeeTotal;
    }

    public Double getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(Double discountTotal) {
        this.discountTotal = discountTotal;
    }

    public Double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(Double netTotal) {
        this.netTotal = netTotal;
    }

    public Institution getSite() {
        return site;
    }

    public void setSite(Institution site) {
        this.site = site;
    }

    public PatientSample getCurrentPatientSample() {
        return currentPatientSample;
    }

    public void setCurrentPatientSample(PatientSample currentPatientSample) {
        this.currentPatientSample = currentPatientSample;
    }

    public List<Item> getItemsForParentItem() {
        return itemsForParentItem;
    }

    public void setItemsForParentItem(List<Item> itemsForParentItem) {
        this.itemsForParentItem = itemsForParentItem;
    }

    public List<PatientSampleComponant> getPatientSampleComponentsByInvestigation() {
        return patientSampleComponentsByInvestigation;
    }

    public void setPatientSampleComponentsByInvestigation(List<PatientSampleComponant> patientSampleComponentsByInvestigation) {
        this.patientSampleComponentsByInvestigation = patientSampleComponentsByInvestigation;
    }

    public PatientInvestigation getCurrentPI() {
        return currentPI;
    }

    public void setCurrentPI(PatientInvestigation currentPI) {
        this.currentPI = currentPI;
    }

    public Institution getOrderedSite() {
        return orderedSite;
    }

    public void setOrderedSite(Institution orderedSite) {
        this.orderedSite = orderedSite;
    }

    public Institution getPeformedSite() {
        return peformedSite;
    }

    public void setPeformedSite(Institution peformedSite) {
        this.peformedSite = peformedSite;
    }

    public boolean isPrintIndividualBarcodes() {
        return printIndividualBarcodes;
    }

    public void setPrintIndividualBarcodes(boolean printIndividualBarcodes) {
        this.printIndividualBarcodes = printIndividualBarcodes;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getInvestigationName() {
        return investigationName;
    }

    public void setInvestigationName(String investigationName) {
        this.investigationName = investigationName;
    }

    public List<PatientReportItemValue> getColumn1AntibioticList() {
        return column1AntibioticList;
    }

    public void setColumn1AntibioticList(List<PatientReportItemValue> column1AntibioticList) {
        this.column1AntibioticList = column1AntibioticList;
    }

    public List<PatientReportItemValue> getColumn2AntibioticList() {
        return column2AntibioticList;
    }

    public void setColumn2AntibioticList(List<PatientReportItemValue> column2AntibioticList) {
        this.column2AntibioticList = column2AntibioticList;
    }

    public Bill getCurrentBill() {
        return currentBill;
    }

    public void setCurrentBill(Bill currentBill) {
        this.currentBill = currentBill;
    }

    public Institution getOutLabInstitution() {
        return outLabInstitution;
    }

    public void setOutLabInstitution(Institution outLabInstitution) {
        this.outLabInstitution = outLabInstitution;
    }

    public Department getOutLabDepartment() {
        return outLabDepartment;
    }

    public void setOutLabDepartment(Department outLabDepartment) {
        this.outLabDepartment = outLabDepartment;
    }

    public String getSampleSearchStrategy() {
        return sampleSearchStrategy;
    }

    public void setSampleSearchStrategy(String sampleSearchStrategy) {
        this.sampleSearchStrategy = sampleSearchStrategy;
    }

    public List<SampleDTO> getSampleDTOList() {
        return sampleDTOList;
    }

    public void setSampleDTOList(List<SampleDTO> sampleDTOList) {
        this.sampleDTOList = sampleDTOList;
    }

    public boolean isRequestReCollected() {
        return requestReCollected;
    }

    public void setRequestReCollected(boolean requestReCollected) {
        this.requestReCollected = requestReCollected;
    }

    /**
     *
     */
    @FacesConverter(forClass = PatientInvestigation.class)
    public static class PatientInvestigationControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PatientInvestigationController controller = (PatientInvestigationController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "patientInvestigationController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof PatientInvestigation) {
                PatientInvestigation o = (PatientInvestigation) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + PatientInvestigationController.class.getName());
            }
        }
    }

    public void navigateToInvestigationsFromSelectedBill(Bill bill) {
        //System.out.println("navigate To Investigations From Selected Bill");
        items = new ArrayList<>();
        listingEntity = ListingEntity.PATIENT_INVESTIGATIONS;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "SELECT i "
                + " FROM PatientInvestigation i "
                + " WHERE i.retired = :ret "
                + " and i.billItem.bill =:bill"
                + " ORDER BY i.id DESC";

        params.put("ret", false);
        params.put("bill", bill);

        items = getFacade().findByJpql(jpql, params);
    }

    public void navigateToSamplesFromSelectedBill(Bill bill) {
//        System.out.println("navigate To Samples From Selected Bill");
        patientSamples = new ArrayList<>();
        listingEntity = ListingEntity.PATIENT_SAMPLES;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "SELECT ps "
                + "FROM PatientSample ps "
                + "JOIN ps.bill b "
                + "WHERE ps.retired = :ret "
                + " and ps.bill =:bill ";

        jpql += " ORDER BY ps.id DESC";

        params.put("ret", false);
        params.put("bill", bill);

        patientSamples = patientSampleFacade.findByJpql(jpql, params, TemporalType.TIMESTAMP);

    }

    public void navigateToPatientReportsFromSelectedInvestigation(PatientInvestigation patientInvestigation) {
        patientReports = new ArrayList<>();
        listingEntity = ListingEntity.PATIENT_REPORTS;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "SELECT r "
                + " FROM PatientReport r "
                + " WHERE r.retired = :ret "
                + " and r.patientInvestigation=:pi "
                + " ORDER BY r.id DESC";

        params.put("ret", false);
        params.put("pi", patientInvestigation);
        patientReports = patientReportFacade.findByJpql(jpql, params);
    }

    public void navigateToPatientReportsFromSelectedBill(Bill bill) {
        patientReports = new ArrayList<>();
        listingEntity = ListingEntity.PATIENT_REPORTS;
        String jpql;
        Map<String, Object> params = new HashMap<>();

        jpql = "SELECT r "
                + " FROM PatientReport r "
                + " WHERE r.retired = :ret "
                + " and r.patientInvestigation.billItem.bill=:bill "
                + " ORDER BY r.id DESC";

        params.put("ret", false);
        params.put("bill", bill);
        patientReports = patientReportFacade.findByJpql(jpql, params);
    }

    @Deprecated
    public List<PatientSample> prepareSampleCollectionByBillsForPhlebotomyRoom(List<Bill> bills, WebUser wu) {
        String j = "";
        Map m;
        Map<Long, PatientSample> rPatientSamplesMap = new HashMap<>();

        if (bills == null) {
            return null;
        }

        for (Bill b : bills) {
            m = new HashMap();
            m.put("can", false);
            m.put("bill", b);
            j = "Select pi from PatientInvestigation pi "
                    + " where pi.cancelled=:can "
                    + " and pi.billItem.bill=:bill";
            List<PatientInvestigation> pis = ejbFacade.findByJpql(j, m);
            if (pis == null) {
                return null;
            }

            for (PatientInvestigation ptix : pis) {

                Investigation ix = ptix.getInvestigation();

                if (ix.getReportedAs() != null) {
                    if (ix.getReportedAs() instanceof Investigation) {
                        ix = (Investigation) ix.getReportedAs();
                    }
                }

                if (ix == null) {
                    continue;
                }

                ptix.setCollected(true);
                ptix.setSampleCollecter(wu);
                ptix.setSampleDepartment(wu.getDepartment());
                ptix.setSampleInstitution(wu.getInstitution());
                ptix.setSampledAt(new Date());
                ejbFacade.edit(ptix);

                List<InvestigationItem> ixis = getIvestigationItemsForInvestigation(ix);
                Item ixSampleComponant = itemController.addSampleComponent(ix);

                if (ixis == null || ixis.isEmpty()) {
                    InvestigationItem ixi = new InvestigationItem();
                    ixi.setRiTop(46);
                    ixi.setRiHeight(2);
                    ixi.setRiLeft(50);
                    ixi.setName(ix.getName() + " Value");
                    ixi.setRiWidth(30);
                    ixi.setHtmltext(ix.getName() + " Value");
                    ixi.setTube(ix.getInvestigationTube());
                    ixi.setTest(ix);
                    ixi.setSampleComponent(ixSampleComponant);
                    ixi.setIxItemType(InvestigationItemType.Value);
                    ixi.setIxItemValueType(InvestigationItemValueType.Varchar);
                    investigationItemFacade.create(ixi);
                    ixis = new ArrayList<>();
                    ixis.add(ixi);
                }

                for (InvestigationItem ixi : ixis) {

                    if (ixi.getIxItemType() == InvestigationItemType.Value) {
                        //System.out.println("ixi.getTube() = " + ixi.getTube());
                        if (ixi.getTube() == null) {
                            if (ixi.getItem() != null) {
                                if (ixi.getItem() instanceof Investigation) {
                                    Investigation tix = (Investigation) ixi.getItem();
                                    ixi.setTube(tix.getInvestigationTube());
                                }
                            }
                        }
                        if (ixi.getTube() == null) {
                            continue;
                        }
//                        if (ixi.getSample() == null) {
//                            continue;
//                        }
                        //System.out.println("ixi.getSample() = " + ixi.getSample());
//                        if (ixi.getSample() == null) {
//                            continue;
//                        }

                        j = "select ps from PatientSample ps "
                                + " where ps.tube=:tube "
                                //                                + " and ps.sample=:sample "
                                //                                + " and ps.machine=:machine "
                                + " and ps.patient=:pt "
                                + " and ps.bill=:bill ";
//                                + " and ps.collected=:ca
                        m = new HashMap();
                        m.put("tube", ixi.getTube());

//                        m.put("sample", ixi.getSample());
//                        m.put("machine", ixi.getMachine());
                        m.put("pt", b.getPatient());

                        m.put("bill", b);
//                        m.put("ca", false);
                        if (ix.isHasMoreThanOneComponant()) {
                            j += " and ps.investigationComponant=:sc ";
                            m.put("sc", ixi.getSampleComponent());
                        }
                        //System.out.println("j = " + j);
                        //System.out.println("m = " + m);
                        PatientSample pts = patientSampleFacade.findFirstByJpql(j, m);
                        //System.out.println("pts = " + pts);
                        if (pts == null) {
                            pts = new PatientSample();

                            pts.setTube(ixi.getTube());
                            pts.setSample(ixi.getSample());
                            pts.setInvestigationComponant(ixi.getSampleComponent());
                            pts.setMachine(ixi.getMachine());
                            pts.setPatient(b.getPatient());
                            pts.setBill(b);

                            pts.setBarcodeGenerated(true);
                            pts.setBarcodeGeneratedDepartment(wu.getDepartment());
                            pts.setBarcodeGeneratedInstitution(wu.getInstitution());
                            pts.setBarcodeGenerator(wu);
                            pts.setBarcodeGeneratedAt(new Date());

                            pts.setCreatedAt(new Date());
                            pts.setCreater(wu);

                            pts.setSampleCollected(false);
                            pts.setSampleReceivedAtLab(false);
                            pts.setReadyTosentToAnalyzer(false);
                            pts.setSentToAnalyzer(false);
                            pts.setReceivedFromAnalyzer(false);
                            pts.setRetired(false);
                            patientSampleFacade.create(pts);
                        }
                        rPatientSamplesMap.put(pts.getId(), pts);

                        PatientSampleComponant ptsc;
                        j = "select ps from PatientSampleComponant ps "
                                + " where ps.patientSample=:pts "
                                + " and ps.bill=:bill "
                                + " and ps.patient=:pt "
                                + " and ps.patientInvestigation=:ptix "
                                + " and ps.investigationComponant=:ixc";
                        m = new HashMap();
                        m.put("pts", pts);
                        m.put("bill", b);
                        m.put("pt", b.getPatient());
                        m.put("ptix", ptix);
                        m.put("ixc", ixi.getSampleComponent());
                        m.put("pts", pts);

                        m.put("bill", b);

                        m.put("pt", b.getPatient());

                        m.put("ptix", ptix);

                        m.put("ixc", ixi.getSampleComponent());

                        //System.out.println("j = " + j);
                        ptsc = patientSampleComponantFacade.findFirstByJpql(j, m);
                        if (ptsc == null) {
                            ptsc = new PatientSampleComponant();
                            ptsc.setPatientSample(pts);
                            ptsc.setBill(b);
                            ptsc.setPatient(b.getPatient());
                            ptsc.setPatientInvestigation(ptix);
                            ptsc.setInvestigationComponant(ixi.getSampleComponent());
                            ptsc.setCreatedAt(new Date());
                            ptsc.setCreater(wu);
                            patientSampleComponantFacade.create(ptsc);
                        }
                    }
                }
            }

        }

        List<PatientSample> rPatientSamples = new ArrayList<>(rPatientSamplesMap.values());
        return rPatientSamples;
    }

    public List<PatientSample> prepareSampleCollectionByBillsForPhlebotomyRoom(Bill barcodeBill, WebUser wu) {
        String j;
        Map m;
        Map<Long, PatientSample> rPatientSamplesMap = new HashMap<>();
        m = new HashMap();
        m.put("can", false);
        m.put("bill", barcodeBill);
        j = "Select pi "
                + " from PatientInvestigation pi "
                + " where pi.cancelled=:can "
                + " and pi.billItem.bill=:bill";
        List<PatientInvestigation> pis = ejbFacade.findByJpql(j, m);
        if (pis == null) {
            return null;
        }

        for (PatientInvestigation ptix : pis) {
            Investigation ix = ptix.getInvestigation();
            if (ix.getReportedAs() != null) {
                if (ix.getReportedAs() instanceof Investigation) {
                    ix = (Investigation) ix.getReportedAs();
                }
            }
            if (ix == null) {
                continue;
            }
            ptix.setSampleGenerated(true);
            ptix.setSampleGeneratedBy(wu);
            ptix.setSampleGeneratedAt(new Date());
            ptix.setSampleDepartment(wu.getDepartment());
            ptix.setSampleInstitution(wu.getInstitution());
            ptix.setSampledAt(new Date());
            if (ptix.getBillItem().getBill().getStatus() == PatientInvestigationStatus.ORDERED || ptix.getBillItem().getBill().getStatus() == PatientInvestigationStatus.SAMPLE_GENERATED) {
                ptix.setStatus(PatientInvestigationStatus.SAMPLE_GENERATED);
            }
            ejbFacade.edit(ptix);

            List<InvestigationItem> ixis = getIvestigationItemsForInvestigation(ix);

            Item ixSampleComponant = itemController.addSampleComponent(ix);
            if (ixis == null || ixis.isEmpty()) {
                InvestigationItem ixi = new InvestigationItem();
                ixi.setRiTop(46);
                ixi.setRiHeight(2);
                ixi.setRiLeft(50);
                ixi.setName(ix.getName() + " Value");
                ixi.setRiWidth(30);
                ixi.setHtmltext(ix.getName() + " Value");
                ixi.setTube(ix.getInvestigationTube());
                ixi.setTest(ix);
                ixi.setSampleComponent(ixSampleComponant);
                ixi.setIxItemType(InvestigationItemType.Value);
                ixi.setIxItemValueType(InvestigationItemValueType.Varchar);
                investigationItemFacade.create(ixi);
                ixis = new ArrayList<>();
                ixis.add(ixi);
            }

            for (InvestigationItem ixi : ixis) {
                if ((ixi.getIxItemType() == InvestigationItemType.Value) || (ixi.getIxItemType() == InvestigationItemType.Template)) {
                    if (ixi.getTube() == null) {
                        if (ixi.getItem() != null) {
                            if (ixi.getItem() instanceof Investigation) {
                                Investigation tix = (Investigation) ixi.getItem();
                                ixi.setTube(tix.getInvestigationTube());
                            }
                        }
                    }
                    if (ixi.getTube() == null) {
                        InvestigationTube it = investigationTubeController.findAndCreateInvestigationTubeByName("Plain Tube");
                        ixi.setTube(it);
                    }
                    if (ixi.getSampleComponent() == null) {
                        ixi.setSampleComponent(ixSampleComponant);
                    }
                    j = "select ps "
                            + " from PatientSample ps "
                            + " where ps.tube=:tube "
                            + " and ps.bill=:bill ";
                    m = new HashMap();
                    m.put("tube", ixi.getTube());
                    m.put("bill", barcodeBill);
                    if (ix.isHasMoreThanOneComponant()) {
                        j += " and ps.investigationComponant=:sc ";
                        m.put("sc", ixi.getSampleComponent());
                    }
                    PatientSample pts = patientSampleFacade.findFirstByJpql(j, m);

                    if (pts == null) {
                        pts = new PatientSample();
                        pts.setTube(ixi.getTube());
                        pts.setSample(ixi.getSample());
                        if (ix.isHasMoreThanOneComponant()) {
                            pts.setInvestigationComponant(ixi.getSampleComponent());
                        }
                        pts.setInstitution(sessionController.getInstitution());
                        pts.setDepartment(sessionController.getDepartment());
                        pts.setMachine(ixi.getMachine());
                        pts.setDepartment(sessionController.getDepartment());
                        pts.setPatient(barcodeBill.getPatient());
                        pts.setBill(barcodeBill);
                        pts.setInvestigationComponant(ixi.getSampleComponent());
                        pts.setBarcodeGenerated(true);
                        pts.setBarcodeGeneratedDepartment(wu.getDepartment());
                        pts.setBarcodeGeneratedInstitution(wu.getInstitution());
                        pts.setBarcodeGenerator(wu);
                        pts.setBarcodeGeneratedAt(new Date());
                        if (pts.getBill().getStatus() == PatientInvestigationStatus.ORDERED || pts.getBill().getStatus() == PatientInvestigationStatus.SAMPLE_GENERATED) {
                            pts.setStatus(PatientInvestigationStatus.SAMPLE_GENERATED);
                        }

                        pts.setCreatedAt(new Date());
                        pts.setCreater(wu);

                        pts.setSampleCollected(false);
                        pts.setSampleReceivedAtLab(false);
                        pts.setReadyTosentToAnalyzer(false);
                        pts.setSentToAnalyzer(false);
                        pts.setReceivedFromAnalyzer(false);
                        pts.setRetired(false);
                        patientSampleFacade.create(pts);
                    }
                    rPatientSamplesMap.put(pts.getId(), pts);

                    PatientSampleComponant ptsc;
                    j = "select ps from PatientSampleComponant ps "
                            + " where ps.patientSample=:pts "
                            + " and ps.bill=:bill "
                            + " and ps.patient=:pt "
                            + " and ps.patientInvestigation=:ptix "
                            + " and ps.investigationComponant=:ixc";
                    m = new HashMap();
                    m.put("pts", pts);
                    m.put("bill", barcodeBill);
                    m.put("pt", barcodeBill.getPatient());
                    m.put("ptix", ptix);
                    m.put("ixc", ixi.getSampleComponent());
                    m.put("pts", pts);

                    ptsc = patientSampleComponantFacade.findFirstByJpql(j, m);
                    if (ptsc == null) {
                        ptsc = new PatientSampleComponant();
                        ptsc.setPatientSample(pts);
                        ptsc.setBill(barcodeBill);
                        ptsc.setPatient(barcodeBill.getPatient());
                        ptsc.setPatientInvestigation(ptix);
                        ptsc.setInvestigationComponant(ixi.getSampleComponent());
                        ptsc.setCreatedAt(new Date());
                        ptsc.setCreater(wu);
                        patientSampleComponantFacade.create(ptsc);
                    }
                }
            }
        }

        if (barcodeBill.getStatus() == PatientInvestigationStatus.ORDERED) {
            barcodeBill.setStatus(PatientInvestigationStatus.SAMPLE_GENERATED);

            if (configOptionApplicationController.getBooleanValueByKey("Lab Test History Enabled", false)) {
                for (PatientInvestigation pi : pis) {

                    String jpql = "select ps from PatientSampleComponant ps "
                            + " where ps.patientInvestigation=:ptix "
                            + " and ps.retired=:ret";
                    Map params = new HashMap();
                    params.put("ret", false);
                    params.put("ptix", pi);

                    List<PatientSampleComponant> componants = patientSampleComponantFacade.findByJpql(jpql, params);

                    if (componants == null) {
                        continue;
                    }

                    System.out.println("componants = " + componants);

                    for (PatientSampleComponant psc : componants) {
                        labTestHistoryController.addBarcodeGenerateHistory(pi, psc.getPatientSample());
                    }

                }
            }
        }

        billFacade.edit(barcodeBill);

        List<PatientSample> rPatientSamples = new ArrayList<>(rPatientSamplesMap.values());
        return rPatientSamples;
    }

    public void prepareSampleCollectionByBillsForPhlebotomyRoom(BillBarcode b) {
        String j = "";
        Map m;
        Map<Long, PatientSample> rPatientSamplesMap = new HashMap<>();

        if (b == null) {
            return;
        }

        if (b.getPatientInvestigationWrappers() == null) {
            return;
        }

        for (PatientInvestigationWrapper ptixw : b.getPatientInvestigationWrappers()) {
            PatientInvestigation ptix = ptixw.getPatientInvestigation();
            //System.out.println("ptix = " + ptix);
            if (ptix == null) {
                continue;
            }

            Investigation ix = ptix.getInvestigation();

            if (ix.getReportedAs() != null) {
                if (ix.getReportedAs() instanceof Investigation) {
                    ix = (Investigation) ix.getReportedAs();
                }
            }

            List<InvestigationItem> ixis = getIvestigationItemsForInvestigation(ix);

            Item ixSampleComponant = itemController.addSampleComponent(ix);

            if (ixis == null || ixis.isEmpty()) {
                InvestigationItem ixi = new InvestigationItem();
                ixi.setRiTop(46);
                ixi.setRiHeight(2);
                ixi.setRiLeft(50);
                ixi.setName(ix.getName() + " Value");
                ixi.setRiWidth(30);
                ixi.setHtmltext(ix.getName() + " Value");
                ixi.setTube(ix.getInvestigationTube());
                ixi.setTest(ix);
                ixi.setSampleComponent(ixSampleComponant);
                ixi.setIxItemType(InvestigationItemType.Value);
                ixi.setIxItemValueType(InvestigationItemValueType.Varchar);
                investigationItemFacade.create(ixi);
                ixis = new ArrayList<>();
                ixis.add(ixi);
            }

            for (InvestigationItem ixi : ixis) {

                if (ixi.getIxItemType() == InvestigationItemType.Value) {
                    //System.out.println("ixi.getTube() = " + ixi.getTube());
                    if (ixi.getTube() == null) {
                        if (ixi.getItem() != null) {
                            if (ixi.getItem() instanceof Investigation) {
                                Investigation tix = (Investigation) ixi.getItem();
                                ixi.setTube(tix.getInvestigationTube());
                            }
                        }
                    }

                    if (ixi.getTube() == null) {
                        InvestigationTube it = investigationTubeController.findAndCreateInvestigationTubeByName("Plain Tube");
                        ixi.setTube(it);
                    }

                    if (ixi.getTube() == null) {
                        continue;
                    }

                    if (ixi.getSampleComponent() == null) {
                        ixi.setSampleComponent(ixSampleComponant);
                    }

                    //System.out.println("ixi.getSample() = " + ixi.getSample());
//                    if (ixi.getSample() == null) {
//                        continue;
//                    }
                    j = "select ps from PatientSample ps "
                            + " where ps.tube=:tube "
                            + " and ps.bill=:bill ";
//                                + " and ps.collected=:ca
                    m = new HashMap();
                    m.put("tube", ixi.getTube());
                    m.put("bill", b.getBill());
//                        m.put("ca", false);
                    if (ix.isHasMoreThanOneComponant()) {
                        j += " and ps.investigationComponant=:sc ";
                        m.put("sc", ixi.getSampleComponent());
                    }
                    if (ixi.getSampleComponent() == null) {
                        ixi.setSampleComponent(ixSampleComponant);
                    }

                    //System.out.println("j = " + j);
                    //System.out.println("m = " + m);
                    PatientSample pts = patientSampleFacade.findFirstByJpql(j, m);
                    //System.out.println("pts = " + pts);
                    if (pts == null) {
                        pts = new PatientSample();

                        pts.setTube(ixi.getTube());
                        pts.setSample(ixi.getSample());
                        if (ix.isHasMoreThanOneComponant()) {
                            pts.setInvestigationComponant(ixi.getSampleComponent());
                        }
                        pts.setMachine(ixi.getMachine());
                        pts.setPatient(b.getBill().getPatient());
                        pts.setBill(b.getBill());
                        pts.setInvestigationComponant(ixi.getSampleComponent());
                        pts.setBarcodeGenerated(true);

                        pts.setCreater(sessionController.getLoggedUser());
                        if (pts.getPatientInvestigation().getBillItem().getBill().getStatus() == patientInvestigationStatus.ORDERED) {
                            pts.setBarcodeGeneratedDepartment(sessionController.getDepartment());
                            pts.setBarcodeGeneratedInstitution(sessionController.getInstitution());
                            pts.setBarcodeGenerator(sessionController.getLoggedUser());
                            pts.setBarcodeGeneratedAt(new Date());
                            pts.setStatus(PatientInvestigationStatus.SAMPLE_GENERATED);
                            pts.setCreatedAt(new Date());
                        }

                        patientSampleFacade.create(pts);
                    }

                    if (pts.getIdStr() != null && !ptixw.getPatientInvestigation().getSampleIds().contains(pts.getIdStr())) {
                        ptixw.getPatientInvestigation().setSampleIds(ptixw.getPatientInvestigation().getSampleIds() + " " + pts.getIdStr());
                        ejbFacade.edit(ptixw.getPatientInvestigation());
                    }
                    rPatientSamplesMap.put(pts.getId(), pts);

                    PatientSampleComponant ptsc;
                    j = "select ps from PatientSampleComponant ps "
                            + " where ps.patientSample=:pts "
                            + " and ps.bill=:bill "
                            + " and ps.patient=:pt "
                            + " and ps.patientInvestigation=:ptix "
                            + " and ps.investigationComponant=:ixc";
                    m = new HashMap();
                    m.put("pts", pts);
                    m.put("bill", b.getBill());
                    m.put("pt", b.getBill().getPatient());
                    m.put("ptix", ptix);
                    m.put("ixc", ixi.getSampleComponent());

                    //System.out.println("j = " + j);
                    ptsc = patientSampleComponantFacade.findFirstByJpql(j, m);
                    if (ptsc == null) {
                        ptsc = new PatientSampleComponant();
                        ptsc.setPatientSample(pts);
                        ptsc.setBill(b.getBill());
                        ptsc.setPatient(b.getBill().getPatient());
                        ptsc.setPatientInvestigation(ptix);
                        ptsc.setInvestigationComponant(ixi.getSampleComponent());
                        ptsc.setCreatedAt(new Date());
                        ptsc.setCreater(sessionController.getLoggedUser());
                        patientSampleComponantFacade.create(ptsc);
                    }
                }

                if (ptix.getBillItem().getBill().getStatus() == patientInvestigationStatus.ORDERED) {
                    ptix.setSampleGenerated(true);
                    ptix.setSampleGeneratedAt(new Date());
                    ptix.setSampleGeneratedBy(sessionController.getLoggedUser());
                    ptix.setSampleDepartment(sessionController.getDepartment());
                    ptix.setSampleInstitution(sessionController.getInstitution());
                    ptix.setStatus(PatientInvestigationStatus.SAMPLE_GENERATED);
                }

                ejbFacade.edit(ptix);

            }

        }

        List<PatientSample> rPatientSamples = new ArrayList<>(rPatientSamplesMap.values());

        for (PatientSample pts : rPatientSamples) {
            PatientSampleWrapper psw = new PatientSampleWrapper(pts);
            b.getPatientSampleWrappers().add(psw);
        }

    }

    public List<PatientSampleComponant> getPatientSampleComponentsByPatientSample(PatientSample patientSample) {
        List<PatientSampleComponant> ptsc;
        String j;
        Map m = new HashMap();
        j = "select ps from PatientSampleComponant ps "
                + " where ps.patientSample=:pts "
                + " and ps.retired=false ";
        m = new HashMap();
        m.put("pts", patientSample);
        //System.out.println("j = " + j);
        ptsc = patientSampleComponantFacade.findByJpql(j, m);
        return ptsc;
    }

    public List<InvestigationItem> getIvestigationItemsForInvestigation(Investigation ix) {
        List<InvestigationItem> iis;
        if (ix == null) {
            return new ArrayList<>();
        }
        Investigation temIx = ix;
        if (ix.getReportedAs() != null) {
            if (ix.getReportedAs() instanceof Investigation) {
                temIx = (Investigation) ix.getReportedAs();
            }
        }

        if (ix.getId() != null) {
            String temSql;
            temSql = "SELECT i FROM InvestigationItem i where i.retired<>true and i.item=:item order by i.riTop, i.riLeft";
            Map m = new HashMap();
            m.put("item", temIx);

            iis = investigationItemFacade.findByJpql(temSql, m);
        } else {
            iis = new ArrayList<>();
        }
        return iis;
    }

//    public String getPatientInvestigationCurrentStatus(PatientInvestigation pi) {
//        String status = "";
//        List<PatientReport> reports = patientReportController.patientReports(pi);
//
//        for(PatientReport pr : reports){
//            if(status == "Approved"){
//                continue;
//            }
//
//            switch (pr.getPatientInvestigation().getStatus()) {
//                case ORDERED:
//                    status = "Ordered";
//                    break;
//                case SAMPLE_GENERATED:
//                    status = "Ordered";
//                    break;
//                case SAMPLE_COLLECTED:
//                    status = "Ordered";
//                    break;
//                case SAMPLE_SENT:
//                    status = "Ordered";
//                    break;
//                case SAMPLE_ACCEPTED:
//                    status = "Ordered";
//                    break;
//                case SAMPLE_REJECTED:
//                    status = "Rejected";
//                    break;
//                case SAMPLE_REVERTED:
//                    status = "Ordered";
//                    break;
//                case SAMPLE_RESENT:
//                    status = "Resent";
//                    break;
//                case SAMPLE_RECOLLECTED:
//                    status = "Recollected";
//                    break;
//                case SAMPLE_INTERFACED:
//                    status = "Interfaced";
//                    break;
//                case SAMPLE_APPROVED:
//                    status = "Ordered";
//                    break;
//                case SAMPLE_REPEATED:
//                    status = "Repeated";
//                    break;
//                case REPORT_PRINTED:
//                    status = "Printed";
//                    break;
//                case REPORT_DISTRIBUTED:
//                    status = "Distributed";
//                    break;
//                case REPORT_REACHED_COLLECTING_CENTRE:
//                    status = "Report Reached Collecting Centre";
//                    break;
//                case REPORT_HANDED_OVER:
//                    status = "Report Handed Over";
//                    break;
//                case REPORT_CREATED:
//                    status = "Created";
//                    break;
//                case REPORT_APPROVED:
//                    status = "Approved";
//                    break;
//                default:
//                    status = "Unknown";
//                    break;
//            }
//        }
//        return status;
//    }
}
