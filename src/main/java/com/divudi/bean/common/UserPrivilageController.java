/*
 * Open Hospital Management Information System
 *
 * Dr M H B Ariyaratne
 * Consultant in Health Informatics
 * buddhika.ari [at] gmail.com
 * and
 * (94) 71 5812399
 */
package com.divudi.bean.common;

// <editor-fold defaultstate="collapsed" desc="Imports">
import com.divudi.core.data.Privileges;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.Institution;
import com.divudi.core.entity.WebUser;
import com.divudi.core.entity.WebUserPrivilege;
import com.divudi.core.facade.DepartmentFacade;
import com.divudi.core.facade.WebUserPrivilegeFacade;
import com.divudi.core.util.JsfUtil;
import com.divudi.core.entity.WebUserRole;
import com.divudi.core.entity.WebUserRolePrivilege;
import com.divudi.core.facade.WebUserRolePrivilegeFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
// </editor-fold>

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical Informatics)
 *
 */
@Named
@SessionScoped
public class UserPrivilageController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private WebUserPrivilegeFacade ejbFacade;
    @EJB
    private WebUserRolePrivilegeFacade facede;
    @EJB
    DepartmentFacade departmentFacade;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Class Variables">
    private static final long serialVersionUID = 1L;
    private List<WebUserPrivilege> selectedItems;
    private List<WebUserPrivilege> currentWebUserPrivileges;
    private List<WebUserRolePrivilege> selectedRoleItems;
    private List<WebUserRolePrivilege> currentWebUserRolePrivileges;
    private WebUser currentWebUser;
    private WebUserRole webUserRole;
    private TreeNode[] selectedNodes;
    private TreeNode<PrivilegeHolder> rootTreeNode;
    private Institution institution;
    private Department department;
    private List<Department> departments;
    private List<PrivilegeHolder> currentUserPrivilegeHolders;
    private boolean privilegesLoaded;
    private String searchText;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public UserPrivilageController() {
    }

    @PostConstruct
    public void init() {
        rootTreeNode = createPrivilegeHolderTreeNodes();
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Functional Methods">
    public List<Department> fillWebUserDepartments(WebUser wu) {
        Set<Department> departmentSet = new HashSet<>();
        String sql = "SELECT i.department "
                + " FROM WebUserDepartment i "
                + " WHERE i.retired = :ret "
                + " AND i.webUser = :wu "
                + " ORDER BY i.department.name";
        Map<String, Object> m = new HashMap<>();
        m.put("ret", false);
        m.put("wu", wu);
        List<Department> depts = departmentFacade.findByJpql(sql, m);
        departmentSet.addAll(depts);
        return new ArrayList<>(departmentSet);
    }

    private TreeNode<PrivilegeHolder> createPrivilegeHolderTreeNodes() {
        TreeNode root = new DefaultTreeNode(new PrivilegeHolder(null, "Root"), null);

        TreeNode allNode = new DefaultTreeNode(new PrivilegeHolder(null, "Privileges for All Sections"), root);

        // OPD Privileges
        TreeNode opdNode = new DefaultTreeNode(new PrivilegeHolder(null, "OPD"), allNode);

        TreeNode billingMenuNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.Opd, "Billing Menu"), opdNode);

        TreeNode billNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdBilling, "Bill"), opdNode);
        TreeNode billOrderingNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdOrdering, "Bill without Financial Details"), opdNode);
        TreeNode preBillingNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdPreBilling, "Pre Billing"), opdNode);
        TreeNode collectingCentreBillingNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdCollectingCentreBilling, "Collecting Centre Billing"), opdNode);
        TreeNode billSearchNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdBillSearch, "Bill Search"), opdNode);
        TreeNode billItemSearchNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdBillItemSearch, "Bill Item Search"), opdNode);
        TreeNode reprintNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdReprint, "Reprint"), opdNode);
        TreeNode cancelNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdCancel, "Cancel"), opdNode);
        TreeNode packageBillCancelNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdPackageBillCancel, "Package Bill Cancel"), opdNode);
        TreeNode returnNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdReturn, "Return"), opdNode);
        TreeNode reactivateNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdReactivate, "Reactivate"), opdNode);
        TreeNode OpdLabReportSearchNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdLabReportSearch, "Lab Report Search"), opdNode);
        TreeNode opdBillSearchEditNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdBillSearchEdit, "OPD Bill Search Edit (Patient Details)"), opdNode);
        TreeNode OpdReprintOriginalBillNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdReprintOriginalBill, "Reprint the Original Bill"), opdNode);
        TreeNode addCreditLimit = new DefaultTreeNode(new PrivilegeHolder(Privileges.AddCreditLimitInRegistration, "Add Credit Limit During Patient Registration"), opdNode);
        TreeNode addNewRefferalDoctor = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdAddNewRefferalDoctor, "Add New Referral Doctor"), opdNode);
        TreeNode addNewCollectingCentre = new DefaultTreeNode(new PrivilegeHolder(Privileges.OpdAddNewCollectingCentre, "Add New Referral Center"), opdNode);

        TreeNode cashierNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.Cashier, "Cashier Menu"), opdNode);
        TreeNode acceptPaymentForCashierBills = new DefaultTreeNode(new PrivilegeHolder(Privileges.AcceptPaymentForPharmacyBills, "Accept payment for sale for cashier bills"), cashierNode);
        TreeNode scanBills = new DefaultTreeNode(new PrivilegeHolder(Privileges.ScanBillsFromCashier, "Scan Bills From Cashier Menu"), cashierNode);
        TreeNode acceptPaymentForOpdBatchBills = new DefaultTreeNode(new PrivilegeHolder(Privileges.AcceptPaymentForOpdBatchBills, "Accept payment for OPD Bactch Bills From Cashier Menu"), cashierNode);
        TreeNode refundBillsAtCashier = new DefaultTreeNode(new PrivilegeHolder(Privileges.RefundFromCashier, "Refunds From Cashier"), cashierNode);
        TreeNode refundOpdBills = new DefaultTreeNode(new PrivilegeHolder(Privileges.RefundOpdBillsFromCashier, "Refund Opd Bills From Cashier Menu"), cashierNode);
        TreeNode refundPharmacyBills = new DefaultTreeNode(new PrivilegeHolder(Privileges.RefundPharmacyBillsFromCashier, "Refund Pharmacy Bills From Cashier"), cashierNode);

        // Inward Privileges
        TreeNode inwardNode = new DefaultTreeNode(new PrivilegeHolder(null, "Inward"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Inward, "Inward Menu"), inwardNode);
        TreeNode admissionsNode = new DefaultTreeNode(new PrivilegeHolder(null, "Admissions"), inwardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardAdmissions, "Admission Menu"), admissionsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardAdmissionsAdmission, "Admission"), admissionsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardAdmissionsEditAdmission, "Edit Admission Details"), admissionsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardAdmissionsInwardAppoinment, "Inward Appointment"), admissionsNode);

        TreeNode roomNode = new DefaultTreeNode(new PrivilegeHolder(null, "Room"), inwardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardRoom, "Room Menu"), roomNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardRoomRoomOccupency, "Room Occupancy"), roomNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardRoomRoomChange, "Room Change"), roomNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardRoomGurdianRoomChange, "Guardian Room Change"), roomNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardRoomDischarge, "Discharge Room"), roomNode);

        TreeNode servicesItemsNode = new DefaultTreeNode(new PrivilegeHolder(null, "Services & Items"), inwardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardServicesAndItems, "Services & Items Menu"), servicesItemsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardServicesAndItemsAddServices, "Add Services"), servicesItemsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardServicesAndItemsAddOutSideCharges, "Add Outside Charges"), servicesItemsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardServicesAndItemsAddProfessionalFee, "Add Professional Fee"), servicesItemsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardServicesAndItemsAddTimedServices, "Add Timed Services"), servicesItemsNode);

        TreeNode inwardBillingNode = new DefaultTreeNode(new PrivilegeHolder(null, "Billing"), inwardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardBilling, "Billing Menu"), inwardBillingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardBillingInterimBill, "Interim Bill"), inwardBillingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardBillingInterimBillSearch, "Interim Bill Search"), inwardBillingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardFinalBillReportEdit, "Edit Patient Name After Payment Finalized"), inwardBillingNode);

        TreeNode inwardPharmacyNode = new DefaultTreeNode(new PrivilegeHolder(null, "Pharmacy"), inwardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardPharmacyMenu, "Pharmacy Menu"), inwardPharmacyNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardPharmacyIssueRequest, "Pharmacy Issue Request"), inwardPharmacyNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardPharmacyIssueRequestSearch, "Pharmacy Issue Request Search"), inwardPharmacyNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardPharmacyIssueRequestCancel, "Pharmacy Issue Request Cancel"), inwardPharmacyNode);

        TreeNode searchNode = new DefaultTreeNode(new PrivilegeHolder(null, "Search"), inwardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardSearch, "Search Menu"), searchNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardSearchServiceBill, "Search Service Bill"), searchNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardSearchProfessionalBill, "Search Professional Bill"), searchNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardSearchFinalBill, "Search Final Bill"), searchNode);

        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardReport, "Inward Reports"), inwardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardAdministration, "Administration"), inwardNode);

        TreeNode additionalPrivilegesNode = new DefaultTreeNode(new PrivilegeHolder(null, "Additional Privileges"), inwardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardAdditionalPrivilages, "Additional Privilege Menu"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardBillSearch, "Search Bills"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardBillItemSearch, "Search Bill Items"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardBillReprint, "Reprint"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardCancel, "Cancel"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardReturn, "Return"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardReactivate, "Reactivate"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ShowInwardFee, "Show Inward Fee"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardCheck, "Inward Check"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardUnCheck, "Inward Uncheck"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardFinalBillCancel, "Inward Final Bill Cancel"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardAdmissionCancel, "Inward Admission Cancel"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardOutSideMarkAsUnPaid, "Inward Outside Mark As Unpaid"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardBillSettleWithoutCheck, "Inward Bill Settle Without Check"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardSearchServiceBillUnrestrictedAccess, "Inward Bill Search Without Restriction"), additionalPrivilegesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.InwardSettleFinalBillUnrestricted, "Inward Final Bill Settle Without Restriction"), additionalPrivilegesNode);

        // Theatre Privileges
        TreeNode theatreNode = new DefaultTreeNode(new PrivilegeHolder(null, "Theatre"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Theatre, "Theatre Menu"), theatreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheatreAddSurgery, "Add Surgery"), theatreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheatreBilling, "Theatre Billing"), theatreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterTransfer, "Theatre Transfer Menu Item"), theatreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterTransferRequest, "Theatre Transfer Request"), theatreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterTransferIssue, "Theatre Transfer Issue"), theatreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterTransferRecieve, "Theatre Transfer Receive"), theatreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterTransferReport, "Theatre Transfer Report"), theatreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterReports, "Theatre Show Reports Menu Item"), theatreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterSummeries, "Theatre Show Summary Menu Item"), theatreNode);

        TreeNode theatreBHTIssueNode = new DefaultTreeNode(new PrivilegeHolder(null, "Theatre BHT Issue"), theatreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterIssue, "Theatre BHT Issue"), theatreBHTIssueNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterIssuePharmacy, "Pharmacy BHT Issue"), theatreBHTIssueNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterIssueStore, "General BHT Issue"), theatreBHTIssueNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterIssueStoreBhtBilling, "Inward BHT Billing"), theatreBHTIssueNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterIssueStoreBhtSearchBill, "Search BHT Issue Bill"), theatreBHTIssueNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterIssueStoreBhtSearchBillItem, "Search BHT Issue Bill Items"), theatreBHTIssueNode);

        TreeNode opdIssueNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterIssueOpd, "Opd Issue"), theatreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterIssueOpdForCasheir, "Opd Issue For Cashier"), opdIssueNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterIssueOpdSearchPreBill, "Opd Issue Search Pre Bill"), opdIssueNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterIssueOpdSearchPreBillForReturnItemOnly, "Opd Issue Return Item Only"), opdIssueNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterIssueOpdSearchPreBillReturn, "Opd Issue Search Pre Bill Return"), opdIssueNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.TheaterIssueOpdSearchPreBillAddToStock, "Opd Issue Pre Bill Add To Stock"), opdIssueNode);

        // Lab Privileges
        TreeNode labNode = new DefaultTreeNode(new PrivilegeHolder(null, "Lab"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Lab, "Lab Menu"), labNode);
        TreeNode labBillingMenuNode = new DefaultTreeNode(new PrivilegeHolder(null, "Billing Menu"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabBilling, "Lab Bill"), labBillingMenuNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabBillSearch, "Lab Bill Search"), labBillingMenuNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabBillItemSearch, "Lab Bill Item Search"), labBillingMenuNode);
        
        TreeNode labDashBoardNode = new DefaultTreeNode(new PrivilegeHolder(null, "Laboratory DashBoard"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.DashBoardMenu, "DashBoard Menu"), labDashBoardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.DashBoardBillSearch, "Search Bill Bills"), labDashBoardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.DashBoardSampleSearch, "Search Sample"), labDashBoardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.DashBoardInvestigationSearch, "Search Investigation"), labDashBoardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.DashBoardReportSearch, "Report Search"), labDashBoardNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.DashBoardPatientReportSearch, "Patient Report Search"), labDashBoardNode);
        
        TreeNode labSampleNode = new DefaultTreeNode(new PrivilegeHolder(null, "Samples"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabSampleCollecting, "Sample Collection"), labSampleNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabSampleSending, "Sample Send"), labSampleNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.OutLabSampleSending, "Out Lab Sample Send"), labSampleNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabSampleReceiving, "Sample Receive"), labSampleNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabSampleRejecting, "Sample Reject"), labSampleNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.AccessLabTestHistory, "Access Investigation History"), labSampleNode);
        
        TreeNode labReportingNode = new DefaultTreeNode(new PrivilegeHolder(null, "Reporting"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabDataentry, "Data Entry"), labReportingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabAutherizing, "Authorize"), labReportingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabDeAutherizing, "De-Authorize"), labReportingNode);

        TreeNode labReportPrintNode = new DefaultTreeNode(new PrivilegeHolder(null, "Report Print"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabPrinting, "Report Print in Laboratory"), labReportPrintNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabReportPrint, "Report Printing"), labReportPrintNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabReportSearchByLoggedInstitution, "Search By Logged Institution"), labReportPrintNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabReportSearchByLoggedDepartment, "Search By Logged Department"), labReportPrintNode);
        
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabBillSearchCashier, "Lab Bill Search"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabBillSearch, "Search Bills"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabReportSearch, "Lab Report Search"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabEditPatient, "Patient Edit"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabBillReprint, "Lab Bill Reprint"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabBillReturning, "Lab Bill Return"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabBillCancelling, "Lab Bill Cancel"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CollectingCentreCancelling, "CC Bill Cancel"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabBillReactivating, "Reactivate"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabReprinting, "Report Reprint"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabReportFormatEditing, "Lab Report Formats Editing"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabReportEdit, "Report Edit After Authorized"), labNode);
        
        TreeNode labSummariesNode = new DefaultTreeNode(new PrivilegeHolder(null, "Lab Summaries"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabSummeries, "Lab Summaries Menu"), labSummariesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabSummeriesLevel1, "Lab Summaries Level 1"), labSummariesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabSummeriesLevel2, "Lab Summaries Level 2"), labSummariesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabSummeriesLevel3, "Lab Summaries Level 3"), labSummariesNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabInvestigationFee, "Lab Investigation Fees"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabBillCancelSpecial, "Lab Bill Cancel Special"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabBillRefundSpecial, "Lab Bill Refund Special"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabAddInwardServices, "Add Inward Services"), labNode);
        
        TreeNode labAdministrationNode = new DefaultTreeNode(new PrivilegeHolder(null, "Lab Administration"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabAdiministrator, "Lab Administration Menu"), labAdministrationNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabItems, "Manage Items Menu"), labAdministrationNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabItemFeeUpadate, "Manage Item Fee Update"), labAdministrationNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabItemFeeDelete, "Manage Item Fee Delete"), labAdministrationNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabReports, "Manage Reports Menu"), labAdministrationNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabLists, "Lists Menu"), labAdministrationNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabSetUp, "Setup Menu"), labAdministrationNode);
        TreeNode labInwardBillingMenuNode = new DefaultTreeNode(new PrivilegeHolder(null, "Lab Inward Billing Menu"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabInwardBilling, "Lab Inward Bill"), labInwardBillingMenuNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabInwardSearchServiceBill, "Lab Inward Bill Search"), labInwardBillingMenuNode);
        TreeNode labCollectingCenterBillingNode = new DefaultTreeNode(new PrivilegeHolder(null, "Lab Collecting Center Billing"), labNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabCollectingCentreBilling, "Lab Collecting Center Menu"), labCollectingCenterBillingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabCCBilling, "Lab Collecting Center Billing"), labCollectingCenterBillingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabCCBillingSearch, "Lab Collecting Center Bill Search"), labCollectingCenterBillingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.LabReporting, "Lab Reporting"), labNode);

        // Pharmacy Privileges
        TreeNode pharmacyNode = new DefaultTreeNode(new PrivilegeHolder(null, "Pharmacy"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Pharmacy, "Pharmacy Menu"), pharmacyNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyAdministration, "Pharmacy Administration"), pharmacyNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDonation, "Pharmacy Donation"), pharmacyNode);

        // Channelling Privileges
        TreeNode channellingNode = new DefaultTreeNode(new PrivilegeHolder(null, "Channelling"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Channelling, "Channelling Menu"), channellingNode);
        TreeNode channelBooking = new DefaultTreeNode(new PrivilegeHolder(null, "Channel Booking"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingChannelBooking, "Channel Booking"), channelBooking);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelSessionMultipleDeletion, "Channel Sessions Multiple Deletion"), channelBooking);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelSessionHolidayMark, "Channel Sessions Holiday Mark"), channelBooking);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelSessionManagement, "Channel Sessions Management"), channelBooking);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelSheduleManagement, "Channel Shedule Management"), channelBooking);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelBookingByMonth, "Channel Booking by Month"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelPatientPortal, "Channel Patient portal"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelDoctorCard, "Channel Doctor card"), channellingNode);
        
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingFutureChannelBooking, "Channel Future Booking"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingPastBooking, "Past Booking"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingBookedList, "Booked List"), channellingNode);
        TreeNode channelDoctorLeaveMenuNode = new DefaultTreeNode(new PrivilegeHolder(null, "Doctor Leave Menu"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingDoctorLeaveByDate, "Doctor Leave By Date"), channelDoctorLeaveMenuNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingDoctorLeaveByServiceSession, "Doctor Leave By Service Session"), channelDoctorLeaveMenuNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingChannelSheduling, "Channel Scheduling"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingSpecialChannelSheduling, "Special Channel Scheduling"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingChannelAgentFee, "Channel Agent Fee"), channellingNode);
        TreeNode channelBookingInterfaceNode = new DefaultTreeNode(new PrivilegeHolder(null, "Channel Booking Interface"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelBookingBokking, "Booking"), channelBookingInterfaceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelBookingReprint, "Reprint"), channelBookingInterfaceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelBookingCancel, "Cancel"), channelBookingInterfaceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelBookingRefund, "Refund"), channelBookingInterfaceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelBookingSettle, "Settle"), channelBookingInterfaceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelBookingChange, "Change"), channelBookingInterfaceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelBookingSearch, "Search"), channelBookingInterfaceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelBookingViews, "Views"), channelBookingInterfaceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelBookingDocPay, "Doctor Payment"), channelBookingInterfaceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelBookingRestric, "Restrict Channel Booking"), channelBookingInterfaceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingPrintInPastBooking, "Print Past Booking Receipt"), channellingNode);
        TreeNode channelPaymentNode = new DefaultTreeNode(new PrivilegeHolder(null, "Payment"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingPayment, "Payment Menu"), channelPaymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingPaymentPayDoctor, "Pay Doctor"), channelPaymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingPaymentDueSearch, "Payment Due Search"), channelPaymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingPaymentDoneSearch, "Payment Done Search"), channelPaymentNode);
        TreeNode channelCashierTransactionNode = new DefaultTreeNode(new PrivilegeHolder(null, "Cashier Transaction"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelCashierTransaction, "Cashier Transaction Menu"), channelCashierTransactionNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelCashierTransactionIncome, "Income"), channelCashierTransactionNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelCashierTransactionIncomeSearch, "Income Search"), channelCashierTransactionNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelCashierTransactionExpencess, "Expenses"), channelCashierTransactionNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelCashierTransactionExpencessSearch, "Expenses Search"), channelCashierTransactionNode);
        TreeNode channelAdministratorNode = new DefaultTreeNode(new PrivilegeHolder(null, "Administrator"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingApoinmentNumberCountEdit, "Edit Appointment Count"), channelAdministratorNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingEditSerialNo, "Edit Appointment Number"), channelAdministratorNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingEditPatientDetails, "Edit Patient Details"), channelAdministratorNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingChannelShedulRemove, "Delete Schedule"), channelAdministratorNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingChannelShedulName, "Edit Session Name"), channelAdministratorNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingChannelShedulStartingNo, "Edit Session Starting No"), channelAdministratorNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingChannelShedulRoomNo, "Edit Session Room No"), channelAdministratorNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingChannelShedulMaxRowNo, "Edit Session Max Row No"), channelAdministratorNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingEditCreditLimitUserLevel, "Edit Credit Limit User Level"), channelAdministratorNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingEditCreditLimitAdminLevel, "Edit Credit Limit Administrator Level"), channelAdministratorNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingReprintOriginalBill, "Channelling Reprint Original Bill"), channelAdministratorNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannellingPastBookingPatientAttend, "Channelling Attend Patients To Past Booking"), channelAdministratorNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelReports, "Channel Reports"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelSummery, "Channel Summary"), channellingNode);
        TreeNode channelManagementNode = new DefaultTreeNode(new PrivilegeHolder(null, "Channel Management"), channellingNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelManagement, "Channel Management Menu"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelAgencyAgencies, "Channel Agencies"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelAgencyCreditLimitUpdate, "Channel Agency Credit Limit Update"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelAgencyCreditLimitUpdateBulk, "Channel Agency Credit Limit Update (Bulk)"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelAddChannelBookToAgency, "Add Channel Book To Agency"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelManageSpecialities, "Channel Management Specialities"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelManageConsultants, "Channel Management Consultants"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelEditingAppoinmentCount, "Channel Editing Appointment Count"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelAddChannelingConsultantToInstitutions, "Add Channelling Consultants To Institution"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelFeeUpdate, "Channel Fee Update"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelCrdeitNote, "Channel Credit Note"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelCrdeitNoteSearch, "Channel Credit Note Search"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelDebitNote, "Channel Debit Note"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelDebitNoteSearch, "Channel Debit Note Search"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelCashCancelRestriction, "Channel Cash Cancel Restriction"), channelManagementNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChannelActiveVat, "Channel Active Vat"), channelManagementNode);

        TreeNode clinicsNode = new DefaultTreeNode(new PrivilegeHolder(null, "Clinics"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Clinic, "Clinics"), clinicsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicSession, "Clinic Session"), clinicsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicCalendar, "Clinic Calendar"), clinicsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicQueue, "Clinic Queue"), clinicsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicDisplay, "Clinic Display"), clinicsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicSchedule, "Clinic Schedule"), clinicsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicReports, "Clinic Reports"), clinicsNode);

        // Payment Privileges
        TreeNode paymentNode = new DefaultTreeNode(new PrivilegeHolder(null, "Payment"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Payment, "Payment Menu"), paymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.PaymentBilling, "Staff Payment Billing"), paymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.PaymentBillSearch, "Payment Search"), paymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.PaymentBillReprint, "Payment Reprints"), paymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.PaymentBillCancel, "Payment Cancel"), paymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.PaymentBillRefund, "Payment Refund"), paymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.PaymentBillReactivation, "Payment Reactivation"), paymentNode);

        // Reports Privileges
        TreeNode reportsNode = new DefaultTreeNode(new PrivilegeHolder(null, "Reports"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Reports, "Reports Menu"), reportsNode);
        TreeNode forOwnInstitutionNode = new DefaultTreeNode(new PrivilegeHolder(null, "For Own Institution"), reportsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ReportsSearchCashCardOwn, "Cash/Card Bill Reports"), forOwnInstitutionNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ReportsSearchCreditOwn, "Credit Bill Reports"), forOwnInstitutionNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ReportsItemOwn, "Item Reports"), forOwnInstitutionNode);
        TreeNode forAllInstitutionNode = new DefaultTreeNode(new PrivilegeHolder(null, "For All Institution"), reportsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ReportsSearchCashCardOther, "Cash/Card Bill Reports"), forAllInstitutionNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ReportSearchCreditOther, "Credit Bill Reports"), forAllInstitutionNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ReportsItemOther, "Item Reports"), forAllInstitutionNode);

        // Clinical Privileges
        TreeNode clinicalsNode = new DefaultTreeNode(new PrivilegeHolder(null, "Clinicals"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Clinical, "Clinical Data"), clinicalsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicalPatientSummery, "Patient Summary"), clinicalsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicalPatientDetails, "Patient Details"), clinicalsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicalPatientPhoto, "Patient Photo"), clinicalsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicalVisitDetail, "Visit Details"), clinicalsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicalVisitSummery, "Visit Summary"), clinicalsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicalHistory, "History"), clinicalsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicalAdministration, "Administration"), clinicalsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ClinicalPatientDelete, "Clinical Patient Delete"), clinicalsNode);

        // Administration Privileges
        TreeNode adminNode = new DefaultTreeNode(new PrivilegeHolder(null, "Administration"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Admin, "Admin Menu"), adminNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.AdminManagingUsers, "Manage Users"), adminNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.AdminInstitutions, "Manage Institutions"), adminNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.AdminStaff, "Manage Staff"), adminNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.AdminItems, "Manage Items/Services"), adminNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.AdminPrices, "Manage Fees/Prices/Packages"), adminNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ManageCreditCompany, "Manage Credit Companies"), adminNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.AdminFilterWithoutDepartment, "Filter Without Department"), adminNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.SearchAll, "Search All"), adminNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChangeProfessionalFee, "Change Professional Fee"), adminNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.SendBulkSMS, "Send Bulk SMS"), adminNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Developers, "Only For Developers"), adminNode);

        // Membership Privileges
        TreeNode membershipNode = new DefaultTreeNode(new PrivilegeHolder(null, "Membership"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShip, "Membership Menu"), membershipNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShipAdd, "Add Members"), membershipNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShipSearch, "Search Members"), membershipNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShipEdit, "Edit Members"), membershipNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MembershipReports, "Membership Reports"), membershipNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MembershipDiscountManagement, "Membership Discount Management"), membershipNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MembershipAdministration, "Membership Administration"), membershipNode);
        TreeNode otherNode = new DefaultTreeNode(new PrivilegeHolder(null, "Other"), membershipNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MembershipSchemes, "Membership Schemes"), otherNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShipInwardMemberShip, "Inward Membership Menu"), otherNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShipInwardMemberShipSchemesDicounts, "Schemes Discounts"), otherNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShipInwardMemberShipInwardMemberShipReport, "Inward Membership Report"), otherNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShipOpdMemberShipDis, "Opd Membership Dis Menu"), otherNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShipOpdMemberShipDisByDepartment, "Discount By Department"), otherNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShipOpdMemberShipDisByCategory, "Discount By Category"), otherNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShipOpdMemberShipDisOpdMemberShipReport, "Opd Membership Report"), otherNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShipMemberReActive, "Re-Activate Registered Patient"), otherNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MemberShipMemberDeActive, "De-Activate Registered Patient"), otherNode);

        // Human Resource Privileges
        TreeNode humanResourceNode = new DefaultTreeNode(new PrivilegeHolder(null, "Human Resource"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Hr, "HR Menu"), humanResourceNode);
        TreeNode workingTimeNode = new DefaultTreeNode(new PrivilegeHolder(null, "Working Time"), humanResourceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrWorkingTime, "Working Time Menu"), workingTimeNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrRosterTable, "Roster Table"), workingTimeNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrUploadAttendance, "Upload Attendance"), workingTimeNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrAnalyseAttendenceByRoster, "Analyse Attendance By Roster"), workingTimeNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrAnalyseAttendenceByStaff, "Analyse Attendance By Staff"), workingTimeNode);
        TreeNode formNode = new DefaultTreeNode(new PrivilegeHolder(null, "Form"), humanResourceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrForms, "Form Menu"), formNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrLeaveForms, "Leave Form"), formNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrAdditionalForms, "Additional Form"), formNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrAdvanceSalary, "HR Salary Advance"), humanResourceNode);
        TreeNode hrSalaryNode = new DefaultTreeNode(new PrivilegeHolder(null, "HR Salary"), humanResourceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrGenerateSalary, "HR Salary Generate"), hrSalaryNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrGenerateSalarySpecial, "HR Salary Generate Special"), hrSalaryNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrPrintSalary, "HR Salary Print"), humanResourceNode);
        TreeNode hrReportsNode = new DefaultTreeNode(new PrivilegeHolder(null, "HR Reports"), humanResourceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrReports, "HR Reports Menu"), hrReportsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrReportsLevel1, "HR Reports Level 1"), hrReportsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrReportsLevel2, "HR Reports Level 2"), hrReportsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrReportsLevel3, "HR Reports Level 3"), hrReportsNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrAdmin, "HR Administration Menu"), humanResourceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.hrDeleteLateLeave, "HR Delete Late Leave"), humanResourceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrEditRetiedDate, "HR Edit Retired Date"), humanResourceNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.HrRemoveResignDate, "HR Remove Resign Date"), humanResourceNode);

        // Store Privileges
        TreeNode storeNode = new DefaultTreeNode(new PrivilegeHolder(null, "Store"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Store, "Store Menu"), storeNode);
        TreeNode storeIssueNode = new DefaultTreeNode(new PrivilegeHolder(null, "Issue"), storeNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreIssue, "Issue Menu"), storeIssueNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreIssueInwardBilling, "Inward Billing"), storeIssueNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreIssueSearchBill, "Search Issue Bill"), storeIssueNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreIssueBillItems, "Search Issue Bill Items"), storeIssueNode);
        TreeNode storePurchaseNode = new DefaultTreeNode(new PrivilegeHolder(null, "Purchase"), storeNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StorePurchase, "Purchase Menu"), storePurchaseNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StorePurchaseOrder, "Purchase Order"), storePurchaseNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StorePurchaseOrderApprove, "PO Approve"), storePurchaseNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StorePurchaseGRNRecive, "GRN Receive"), storePurchaseNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StorePurchaseGRNReturn, "GRN Return"), storePurchaseNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StorePurchasePurchase, "Purchase"), storePurchaseNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StorePurchaseOrderApproveSearch, "PO Approve Search"), storePurchaseNode);
        TreeNode storeTransferNode = new DefaultTreeNode(new PrivilegeHolder(null, "Transfer"), storeNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreTransfer, "Transfer Menu"), storeTransferNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreTransferRequest, "Request"), storeTransferNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreTransferIssue, "Issue"), storeTransferNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreTransferRecive, "Receive"), storeTransferNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreTransferReport, "Report"), storeTransferNode);
        TreeNode storeAdjustmentNode = new DefaultTreeNode(new PrivilegeHolder(null, "Adjustment"), storeNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreAdjustment, "Adjustment Menu"), storeAdjustmentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreAdjustmentDepartmentStock, "Department Stock (Qty)"), storeAdjustmentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreAdjustmentStaffStock, "Staff Stock Adjustment"), storeAdjustmentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreAdjustmentPurchaseRate, "Purchase Rate"), storeAdjustmentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreAdjustmentSaleRate, "Sale Rate"), storeAdjustmentNode);
        TreeNode storeDealorPaymentNode = new DefaultTreeNode(new PrivilegeHolder(null, "Delor Payment"), storeNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreDealorPayment, "Delor Payment Menu"), storeDealorPaymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreDealorPaymentDueSearch, "Delor Due Search"), storeDealorPaymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreDealorPaymentDueByAge, "Delor Due By Age"), storeDealorPaymentNode);
        TreeNode storePaymentNode = new DefaultTreeNode(new PrivilegeHolder(null, "Payment"), storeDealorPaymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreDealorPaymentPayment, "Payment Menu"), storePaymentNode);       
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreDealorPaymentPaymentGRN, "GRN Payment"), storePaymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreDealorPaymentPaymentGRNSelect, "GRN Payment (Select)"), storePaymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreDealorPaymentGRNDoneSearch, "GRN Payment Due Search"), storeDealorPaymentNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreSearch, "Search Menu"), storeNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreReports, "Report Menu"), storeNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreSummery, "Summary Menu"), storeNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.StoreAdministration, "Administration Menu"), storeNode);

        TreeNode courierNode = new DefaultTreeNode(new PrivilegeHolder(null, "Courier"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Courier, "Courier Menu"), courierNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CourierCollectSamples, "Courier Collect Samples"), courierNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CourierHandoverSamplesToLab, "Courier Handover Samples to Lab"), courierNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CourierViewReports, "Courier View Reports"), courierNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CourierPrintReports, "Courier Print Reports"), courierNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CourierViewStatistics, "Courier View Statistics"), courierNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CourierViewBillReports, "Courier View Bill Reports"), courierNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CourierViewPaymentReports, "Courier View Payment Reports"), courierNode);

        TreeNode collectingCentreNode = new DefaultTreeNode(new PrivilegeHolder(null, "Collecting Centre"), allNode);
        
        TreeNode collectingCentreManageNode = new DefaultTreeNode(new PrivilegeHolder(null, "Collecting Centre Manage"), collectingCentreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CollectingCentreManageMenu, "Collecting Centre Manage Menu"), collectingCentreManageNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CollectingCentreBilling, "Collecting Centre Billing"), collectingCentreManageNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CCPaymentReceive, "Collecting Centre Payment Receive"), collectingCentreManageNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.SearchCCPaymentReceive, "Search Collecting Centre Payment Receive"), collectingCentreManageNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.IssueReferenceBook, "Collecting Centre Issue Reference Book"), collectingCentreManageNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.SearchIssuedReferenceBook, "Search Collecting Centre Reference Book"), collectingCentreManageNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChangeCreditLimitInCC, "Change Collecting Centre Credit Limit"), collectingCentreManageNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.PayCollectingCentre, "Pay Collecting Centre"), collectingCentreManageNode);
        
        TreeNode creditDebitNoteNode = new DefaultTreeNode(new PrivilegeHolder(null, "Credit/Debit Note"), collectingCentreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CollectingCentreCreditDebitNoteMenu, "Credit/Debit Note Menu"), creditDebitNoteNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CollectingCentreCreditNote, "Collecting Centre Credit Note"), creditDebitNoteNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CollectingCentreDebitNote, "Collecting Centre Debit Note"), creditDebitNoteNode);
        
        new DefaultTreeNode(new PrivilegeHolder(Privileges.CollectingCentreReports, "Collecting Centre Reports"), collectingCentreNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChangeCollectingCentre, "Change Collecting Centre"), collectingCentreNode);
 
        // User Menu
        TreeNode userNode = new DefaultTreeNode(new PrivilegeHolder(null, "User"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.UserMenu, "User Menu"), userNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChangeMyPassword, "Change User Password"), userNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChangeMyTheme, "Change User Theme"), userNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChangePreferece, "Change User Preferances"), userNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChangeMyApiKeys, "Change API Keys"), userNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.ChangeReceiptPrintingPaperTypes, "Change Receipt Printing Paper Types"), userNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.MyFinanacialTransactionManager, "User Financial Transaction Manager"), userNode);

        // Search Privileges
        TreeNode searchRootNode = new DefaultTreeNode(new PrivilegeHolder(null, "Search"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.Search, "Search Menu"), searchRootNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.SearchGrand, "Grand Search"), searchRootNode);

        // Cash Transaction Privileges
        TreeNode cashTransactionNode = new DefaultTreeNode(new PrivilegeHolder(null, "Cash Transaction"), allNode);
        TreeNode cashTransactionMenuNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.CashTransaction, "Cash Transaction Menu"), cashTransactionNode);
        TreeNode cashInNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.CashTransactionCashIn, "Cash In"), cashTransactionNode);
        TreeNode cashOutNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.CashTransactionCashOut, "Cash Out"), cashTransactionNode);

        TreeNode handoverAcceptAsCashier = new DefaultTreeNode(new PrivilegeHolder(Privileges.ShiftHandoverAcceptAsCashier, "Shift Handover Accept As A Cashier"), cashTransactionNode);
        TreeNode handoverAcceptAsMainCashier = new DefaultTreeNode(new PrivilegeHolder(Privileges.ShiftHandoverAcceptAsMainCashier, "Shift Handover Accept As Main Cashier"), cashTransactionNode);

        TreeNode listToCashReceiveNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.CashTransactionListToCashRecieve, "List To Cash Receive"), cashTransactionNode);

        TreeNode PettyCashBillApprove = new DefaultTreeNode(new PrivilegeHolder(Privileges.PettyCashBillApprove, "Petty Cash Bill Approval"), cashTransactionNode);
        TreeNode PettyCashBillCancellationApprove = new DefaultTreeNode(new PrivilegeHolder(Privileges.PettyCashBillCancellationApprove, "Petty Cash Bill Cancellation Approval"), cashTransactionNode);

        //Pharmacy
        TreeNode pharmacyTokenManagement = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyTokenManagement, "Pharmacy Token Management"), pharmacyNode);
        TreeNode retailTransaction = new DefaultTreeNode("Pharmacy Retail Transaction", pharmacyNode);
        TreeNode retailTransactionMenu = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyRetailTransactionMenue, "Pharmacy Retail Transaction Menu"), retailTransaction);
        TreeNode PharmacySale = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySale, "Pharmacy Sale"), retailTransaction);
        TreeNode PharmacySaleForCashier = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySaleForCashier, "Pharmacy Sale For Cashier"), retailTransaction);
        TreeNode PharmacySaleQuick = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySaleQuick, "Pharmacy Sale - Quick"), retailTransaction);
        TreeNode PharmacySaleForCashierQuick = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySaleForCashierQuick, "Pharmacy Sale For Cashier - Quick"), retailTransaction);
        TreeNode PharmacySaleWithOutStock = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySaleWithOutStock, "Pharmacy Sale With Out Stock"), retailTransaction);
        TreeNode PharmacySearchSaleBill = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySearchSaleBill, "Pharmacy Search Sale Bill"), retailTransaction);
        TreeNode PharmacySearchSalePreBill = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySearchSalePreBill, "Pharmacy Search Sale PreBill"), retailTransaction);
        TreeNode PharmacySearchSaleBillItems = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySearchSaleBillItems, "Pharmacy Search Sale BillItems"), retailTransaction);
        TreeNode PharmacyReturnItemsOnly = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyReturnItemsOnly, "Pharmacy Return Items Only"), retailTransaction);
        TreeNode PharmacyReturnItemsAndPayments = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyReturnItemsAndPayments, "Pharmacy Return Items And Payments"), retailTransaction);
        TreeNode PharmacySearchReturnBill = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySearchReturnBill, "Pharmacy Search ReturnBill"), retailTransaction);
        TreeNode PharmacySaleCancel = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySaleCancel, "Pharmacy Sale Bill Cancel"), retailTransaction);
        TreeNode PharmacyAddToStock = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyAddToStock, "Pharmacy Add To Stock"), retailTransaction);

        TreeNode PharmacyWholeSaleTransAction = new DefaultTreeNode("Pharmacy Wholesale Transaction", pharmacyNode);
        TreeNode PharmacyWholeSaleTransactionMenue = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyWholeSaleTransactionMenue, "Procurement Menu"), PharmacyWholeSaleTransAction);
        TreeNode PharmacyWholesaleSale = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyWholesaleSale, "Pharmacy Wholesale Sale"), PharmacyWholeSaleTransAction);
        TreeNode PharmacyWholesaleSaleForCashier = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyWholesaleSaleForCashier, "Pharmacy Wholesale Sale For Cashier"), PharmacyWholeSaleTransAction);
        TreeNode PharmacyWholesaleSearchSaleBill = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyWholesaleSearchSaleBill, "Pharmacy Wholesale Search Sale Bill"), PharmacyWholeSaleTransAction);
        TreeNode PharmacyWholesaleSearchSaleBillToPay = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyWholesaleSearchSaleBillToPay, "Pharmacy Wholesale Search Sale Bill To Pay"), PharmacyWholeSaleTransAction);
        TreeNode PharmacyWholesaleSearchSaleBillItems = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyWholesaleSearchSaleBillItems, "Pharmacy Wholesale Search Sale Bill Items"), PharmacyWholeSaleTransAction);
        TreeNode PharmacyWholesaleReturnItemsOnly = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyWholesaleReturnItemsOnly, "Pharmacy Wholesale Return Items Only"), PharmacyWholeSaleTransAction);
        TreeNode PharmacyWholesaleWholeSaleAddToStock = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyWholesaleWholeSaleAddToStock, "Pharmacy WholeSale Add To Stock"), PharmacyWholeSaleTransAction);
        TreeNode PharmacyWholeSalePurchase = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyWholeSalePurchase, "Pharmacy Whole Sale Purchase"), PharmacyWholeSaleTransAction);
        TreeNode PharmacySearchReturnBillItems = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySearchReturnBillItems, "Pharmacy Search Return Bill Items"), PharmacyWholeSaleTransAction);

        TreeNode disbursementNode = new DefaultTreeNode("Pharmacy Disbursement", pharmacyNode);
        TreeNode disbursementMenue = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisburesementMenu, "Pharmacy Disburesement Menu"), disbursementNode);
        TreeNode disbursementRequest = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisbursementRequest, "Request"), disbursementNode);
        TreeNode disbursementFinalizeRequest = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisbursementFinalizeRequest, "Finalize Transfer Request"), disbursementNode);
        TreeNode PharmacyDisbursementApproval = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisbursementRequestApproval, "Pharmacy Disbursement Request Approval"), disbursementNode);
        TreeNode disbursementIssueForRequest = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisbursementIssurForRequest, "Issue for Request"), disbursementNode);
        TreeNode disbursementDirectIssue = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisbursementDirectIssue, "Direct Issue"), disbursementNode);
        TreeNode disbursementRecieve = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisbursementRecieve, "Recieve"), disbursementNode);
        TreeNode TransferReciveApproval = new DefaultTreeNode(new PrivilegeHolder(Privileges.TransferReciveApproval, "Recieve Approval"), disbursementNode);
        TreeNode PharmacyDisbursementReports = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisbursementReports, "Pharmacy Disbursement Reports"), disbursementNode);
        TreeNode PharmacyTransferViewRates = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyTransferViewRates, "Pharmacy Transfer View Rates"), disbursementNode);
        TreeNode StockRequestViewRates = new DefaultTreeNode(new PrivilegeHolder(Privileges.StockRequestViewRates, "Stock Request View Rates"), disbursementNode);
        TreeNode ConsumptionViewRates = new DefaultTreeNode(new PrivilegeHolder(Privileges.ConsumptionViewRates, "Consumption View Rates"), disbursementNode);
        TreeNode StockTransactionViewRates = new DefaultTreeNode(new PrivilegeHolder(Privileges.StockTransactionViewRates, "Stock Transaction View Rates"), disbursementNode);
        TreeNode DiscardViewRates = new DefaultTreeNode(new PrivilegeHolder(Privileges.DiscardViewRates, "Discard View Rates"), disbursementNode);

        TreeNode InpatientMedicationManagementNode = new DefaultTreeNode("Inpatient medication Management", pharmacyNode);
        TreeNode InpatientMedicationManagementMenue = new DefaultTreeNode(new PrivilegeHolder(Privileges.InpatientMedicationManagementMenue, "Procurement Menu"), InpatientMedicationManagementNode);
        TreeNode PharmacyDirectIssueToBht = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDirectIssueToBht, "Pharmacy Direct Issue To Bht"), InpatientMedicationManagementNode);
        TreeNode PharmacyDirectIssueToTheaterCases = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDirectIssueToTheaterCases, "Pharmacy Direct Issue To Theater Cases"), InpatientMedicationManagementNode);
        TreeNode PharmacyBhtIssueRequest = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyBhtIssueRequest, "Pharmacy Bht Issue Request"), InpatientMedicationManagementNode);
        TreeNode PharmacySearchInpatientDirectIssuesbyBill = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySearchInpatientDirectIssuesbyBill, "Pharmacy Search Inpatient Direct Issues by Bill"), InpatientMedicationManagementNode);
        TreeNode PharmacySearchInpatientDirectIssuesbyItem = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySearchInpatientDirectIssuesbyItem, "Pharmacy Search Inpatient Direct Issues by Item"), InpatientMedicationManagementNode);
        TreeNode PharmacySearchInpatientDirectIssueReturnsbyBill = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySearchInpatientDirectIssueReturnsbyBill, "Pharmacy Search Inpatient Direct Issue Returns by Bill"), InpatientMedicationManagementNode);
        TreeNode PharmacysSearchInpatientDirectIssueReturnsbyItem = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacysSearchInpatientDirectIssueReturnsbyItem, "Pharmacy Search Inpatient Direct Issue Returns by Item"), InpatientMedicationManagementNode);
        TreeNode NursingIPBillingViewRates = new DefaultTreeNode(new PrivilegeHolder(Privileges.NursingIPBillingViewRates, "Nursing IP Billing View Rates"), InpatientMedicationManagementNode);
        TreeNode IPRequestViewRates = new DefaultTreeNode(new PrivilegeHolder(Privileges.IPRequestViewRates, "IP Request View Rates"), InpatientMedicationManagementNode);

        TreeNode ProcumentNode = new DefaultTreeNode("Pharmacy Procument", pharmacyNode);
        TreeNode pharmacyProcurementMenu = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyProcurementMenu, "Procurement Menu"), ProcumentNode);
        TreeNode pharmacyCreatePurchaseOrder = new DefaultTreeNode(new PrivilegeHolder(Privileges.CreatePurchaseOrder, "Create Purchase Order"), ProcumentNode);
        TreeNode pharmacyAutoOrderPModel = new DefaultTreeNode(new PrivilegeHolder(Privileges.AutoOrderPModel, "Auto Order (P Model)"), ProcumentNode);
        TreeNode pharmacyAutoOrderQModel = new DefaultTreeNode(new PrivilegeHolder(Privileges.AutoOrderQModal, "Auto Order (Q Model)"), ProcumentNode);
        TreeNode pharmacyDirectPurchase = new DefaultTreeNode(new PrivilegeHolder(Privileges.DirectPurchase, "Direct Purchase"), ProcumentNode);
        TreeNode pharmacyPurchaseOrderApprovel = new DefaultTreeNode(new PrivilegeHolder(Privileges.PurchaseOrdersApprovel, "Purchase Orders Approvel"), ProcumentNode);
        TreeNode pharmacyGoodRecipt = new DefaultTreeNode(new PrivilegeHolder(Privileges.GoodsRecipt, "Pharmacy Good Recipt"), ProcumentNode);
        TreeNode pharmacyReturnReceviedGoods = new DefaultTreeNode(new PrivilegeHolder(Privileges.ReturnReceviedGoods, "Pharmacy Return Recevied Goods"), ProcumentNode);
        TreeNode pharmacyReturnWithoutRecipt = new DefaultTreeNode(new PrivilegeHolder(Privileges.ReturnWithoutRecipt, "Pharmacy Return WIthout Recipt"), ProcumentNode);
        TreeNode pharmacyOrderCancellation = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyOrderCancellation, "Pharmacy Order Cancellation"), ProcumentNode);

        TreeNode DealerPayment = new DefaultTreeNode("Pharmacy Dealer Payment", pharmacyNode);
        TreeNode PharmacyDealerPaymentMenue = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDealerPaymentMenue, "Pharmacy Dealer Payment Menue"), DealerPayment);
        TreeNode PharmacyDealerDueSearch = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDealerDueSearch, "Pharmacy Dealer Due Search"), DealerPayment);
        TreeNode PharmacyDealerDueByAge = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDealerDueByAge, "Pharmacy Dealer Due By Age"), DealerPayment);
        TreeNode PharmacyPayment = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyPayment, "Pharmacy Payment"), DealerPayment);
        TreeNode PharmacyGRNPaymentApprove = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyGRNPaymentApprove, "Pharmacy GRN Payment Approve"), DealerPayment);
        TreeNode PharmacyGRNPaymentDoneSearch = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyGRNPaymentDoneSearch, "Pharmacy GRN Payment Done Search"), DealerPayment);
        TreeNode PharmacyCreditDueAndAccess = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyCreditDueAndAccess, "Pharmacy Credit Du eAnd Access"), DealerPayment);

        TreeNode PharmacyAdjustment = new DefaultTreeNode("Pharmacy Adjustment", pharmacyNode);
        TreeNode pharmacyPharmacyAdjustmentMenue = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyAdjustmentMenue, "Pharmacy Adjustment Menu"), PharmacyAdjustment);
        TreeNode PharmacyAdjustmentDepartmentStockQTY = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyAdjustmentDepartmentStockQTY, "Pharmacy Adjustment Department Stock QTY"), PharmacyAdjustment);
        TreeNode PharmacyAdjustmentDepartmentStockBySingleItemQTY = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyAdjustmentDepartmentStockBySingleItemQTY, "Pharmacy Adjustment Department Stock By Single Item QTY"), PharmacyAdjustment);
        TreeNode PharmacyAdjustmentStaffStockAdjustment = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyAdjustmentStaffStockAdjustment, "Pharmacy Adjustment Staff Stock Adjustment"), PharmacyAdjustment);
        TreeNode PharmacyAdjustmentPurchaseRate = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyAdjustmentPurchaseRate, "Pharmacy Adjustment Purchase Rate"), PharmacyAdjustment);
        TreeNode PharmacyAdjustmentSaleRate = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyAdjustmentSaleRate, "Pharmacy Adjustment Sale Rate"), PharmacyAdjustment);
        TreeNode PharmacyAdjustmentWholeSaleRate = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyAdjustmentWholeSaleRate, "Pharmacy Adjustment Wholesale Rate"), PharmacyAdjustment);
        TreeNode PharmacyAdjustmentExpiaryDate = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyAdjustmentExpiryDate, "Pharmacy Adjustment Expiary Date"), PharmacyAdjustment);
        TreeNode PharmacyAdjustmentReports = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyAdjustmentSearchAdjustmentBills, "Pharmacy Adjustment Search Adjustment Bills"), PharmacyAdjustment);
        TreeNode PharmacyPhysicalCountApprove = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyPhysicalCountApprove, "Pharmacy Physical Count Approve"), PharmacyAdjustment);

        TreeNode PharmacyDisposal = new DefaultTreeNode("Pharmacy Disposal", pharmacyNode);
        TreeNode pharmacyDisposalMenu = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisposalMenue, "Pharmacy Disposal Menue"), PharmacyDisposal);
        TreeNode PharmacyDisposalIssue = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisposalIssue, "Pharmacy Disposal Issue"), PharmacyDisposal);
        TreeNode PharmacyDisposalSearchIssueBill = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisposalSearchIssueBill, "Pharmacy Disposal Search Issue Bill"), PharmacyDisposal);
        TreeNode PharmacyDisposalSearchIssueBillItems = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisposalSearchIssueBillItems, "Pharmacy Disposal Search Issue Return Bill"), PharmacyDisposal);
        TreeNode PharmacyDisposalSearchIssueReturnBill = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisposalSearchIssueReturnBill, "Pharmacy Adjustment Purchase Rate"), PharmacyDisposal);
        TreeNode PharmacyDisposalUnitIssueMargin = new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyDisposalUnitIssueMargin, "Pharmacy Disposal Unit Issue Margin"), PharmacyDisposal);

        // Adding Optician node and subnodes
        TreeNode opticianNode = new DefaultTreeNode(new PrivilegeHolder(null, "Optician"), allNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.OpticianPatientManagement, "Patient Management"), opticianNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.OpticianAppointmentManagement, "Appointment Management"), opticianNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.OpticianEmr, "EMR"), opticianNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.OpticianStockManagement, "Stock Management"), opticianNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.OpticianProductCatalog, "Product Catalog"), opticianNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.OpticianRepairManagement, "Repair Management"), opticianNode);

        new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyItemSearch, "Item Search"), pharmacyNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacyGenarateReports, "Generate Reports"), pharmacyNode);
        new DefaultTreeNode(new PrivilegeHolder(Privileges.PharmacySummaryViews, "Summary Views"), pharmacyNode);

        TreeNode superAdminNode = new DefaultTreeNode(new PrivilegeHolder(Privileges.SuperAdmin, "Super Admin"), allNode);
        TreeNode editData = new DefaultTreeNode(new PrivilegeHolder(Privileges.EditData, "Edit Data"), superAdminNode);
        TreeNode reActivate = new DefaultTreeNode(new PrivilegeHolder(Privileges.Reactivate, "Reactivate"), superAdminNode);
        TreeNode deleteData = new DefaultTreeNode(new PrivilegeHolder(Privileges.DeleteData, "Delete Data"), superAdminNode);
        TreeNode billCancel = new DefaultTreeNode(new PrivilegeHolder(Privileges.BillCancel, "Bill Cancel "), superAdminNode);
        TreeNode billRefund = new DefaultTreeNode(new PrivilegeHolder(Privileges.BillRefund, "Bill Refund"), superAdminNode);

        return root;
    }

    public void savePrivileges() {
        if (currentWebUser == null) {
            JsfUtil.addErrorMessage("Please select a user");
            return;
        }
        if (department == null) {
            JsfUtil.addErrorMessage("Please select a department");
            return;
        }
        saveWebUserPrivileges();
    }

    public void saveUserRolePrivileges() {
        if (webUserRole == null) {
            JsfUtil.addErrorMessage("Please select a user");
            return;
        }
        saveWebUserRolePrivileges();
    }

    public List<PrivilegeHolder> createPrivilegeHolders(List<WebUserPrivilege> ps) {
        List<PrivilegeHolder> phs = new ArrayList<>();
        if (ps == null) {
            return phs;
        }

        for (WebUserPrivilege tmpWup : ps) {
            PrivilegeHolder ph = new PrivilegeHolder();
            ph.setPrivilege(tmpWup.getPrivilege());
            ph.setName(tmpWup.getPrivilege().getLabel());
            phs.add(ph);
        }
        return phs;
    }

    public List<PrivilegeHolder> createRolePrivilegeHolders(List<WebUserRolePrivilege> ps) {
        List<PrivilegeHolder> phs = new ArrayList<>();
        if (ps == null) {
            return phs;
        }

        for (WebUserRolePrivilege tmpWup : ps) {
            PrivilegeHolder ph = new PrivilegeHolder();
            ph.setPrivilege(tmpWup.getPrivilege());
            ph.setName(tmpWup.getPrivilege().getLabel());
            phs.add(ph);
        }
        return phs;
    }

    public void saveWebUserPrivileges(WebUser u, List<PrivilegeHolder> selected, Department dept) {
        currentWebUser = u;
        department = dept;
        List<PrivilegeHolder> selectedPrivileges = selected;
        for (WebUserPrivilege wup : getCurrentWebUserPrivileges()) {
            wup.setRetired(true);

        }
        getFacade().batchEdit(getCurrentWebUserPrivileges());
        if (selectedPrivileges == null) {
            return;
        }

        List<WebUserPrivilege> newWups = new ArrayList<>();
        List<WebUserPrivilege> oldWups = new ArrayList<>();

        for (PrivilegeHolder ph : selectedPrivileges) {
            if (ph.getPrivilege() == null) {
                continue;
            }
            String jpql = "select w"
                    + " from WebUserPrivilege w "
                    + " where w.department=:dep "
                    + " and w.webUser=:wu "
                    + " and w.privilege=:p";
            Map m = new HashMap();
            m.put("dep", department);
            m.put("wu", currentWebUser);
            m.put("p", ph.getPrivilege());
            WebUserPrivilege wup = getFacade().findFirstByJpql(jpql, m);
            if (wup == null) {
                wup = new WebUserPrivilege();
                wup.setDepartment(department);
                wup.setWebUser(currentWebUser);
                wup.setPrivilege(ph.getPrivilege());
                newWups.add(wup);
            } else {
                wup.setRetired(false);
                oldWups.add(wup);
            }
        }
        getFacade().batchCreate(newWups);
        getFacade().batchEdit(oldWups);

        fillUserPrivileges();
        JsfUtil.addSuccessMessage("Updated");
    }

    public void saveWebUserPrivileges() {
        List<PrivilegeHolder> selectedPrivileges = extractPrivileges(selectedNodes);

        // Retire all current web user privileges initially
        List<WebUserPrivilege> currentPrivileges = getCurrentWebUserPrivileges();
        for (WebUserPrivilege wup : currentPrivileges) {
            wup.setRetired(true);
        }

        if (selectedPrivileges == null) {
            getFacade().batchEdit(currentPrivileges);
            return;
        }

        List<WebUserPrivilege> newWups = new ArrayList<>();
        List<WebUserPrivilege> nonRetiredPrivileges = new ArrayList<>();

        for (PrivilegeHolder ph : selectedPrivileges) {
            if (ph.getPrivilege() == null) {
                continue;
            }

            boolean found = false;
            for (WebUserPrivilege wup : currentPrivileges) {
                if (wup.getPrivilege() == ph.getPrivilege()) {
                    wup.setRetired(false);
                    nonRetiredPrivileges.add(wup);
                    found = true;
                    break;
                }
            }

            if (!found) {
                WebUserPrivilege newWup = new WebUserPrivilege();
                newWup.setDepartment(department);
                newWup.setWebUser(currentWebUser);
                newWup.setPrivilege(ph.getPrivilege());
                newWups.add(newWup);
            }
        }

        getFacade().batchCreate(newWups);
        getFacade().batchEdit(currentPrivileges);

        // Combine non-retired current privileges and newly added privileges
        List<WebUserPrivilege> updatedPrivileges = new ArrayList<>(nonRetiredPrivileges);
        updatedPrivileges.addAll(newWups);

        // Set the combined list as current web user privileges
        setCurrentWebUserPrivileges(updatedPrivileges);
        // Log final state after saving

        for (WebUserPrivilege wup : updatedPrivileges) {
        }

        fillUserPrivileges();
        JsfUtil.addSuccessMessage("Updated");
    }

    private List<PrivilegeHolder> extractPrivileges(TreeNode[] selectedNodes) {
        List<PrivilegeHolder> privileges = new ArrayList<>();
        if (selectedNodes != null) {
            for (TreeNode node : selectedNodes) {
                Object data = node.getData();
                if (data instanceof PrivilegeHolder) {
                    PrivilegeHolder ph = (PrivilegeHolder) data;
                    privileges.add(ph);
                } else {
                    // Handle the case where the data is not of type PrivilegeHolder

                }
            }
        }
        return privileges;
    }

//    public static List<PrivilegeHolder> extractPrivileges(TreeNode[] selectedNodes) {
//        List<PrivilegeHolder> privileges = new ArrayList<>();
//        if (selectedNodes != null) {
//            for (TreeNode node : selectedNodes) {
//                Object data = node.getData();
//                if (data instanceof PrivilegeHolder) {
//                    privileges.add((PrivilegeHolder) data);
//                }
//            }
//        }
//        return privileges;
//    }
    public void saveWebUserRolePrivileges() {
        List<PrivilegeHolder> selectedPrivileges = extractPrivileges(selectedNodes);

        for (WebUserRolePrivilege wup : getCurrentWebUserRolePrivileges()) {
            wup.setRetired(true);
        }

        getRoleFacede().batchEdit(getCurrentWebUserRolePrivileges());
        if (selectedPrivileges == null) {
            return;
        }

        List<WebUserRolePrivilege> newWups = new ArrayList<>();
        List<WebUserRolePrivilege> oldWups = new ArrayList<>();

        for (PrivilegeHolder ph : selectedPrivileges) {
            if (ph.getPrivilege() == null) {
                continue;
            }
            String jpql = "select w"
                    + " from WebUserRolePrivilege w "
                    + " where w.webUserRole=:wu "
                    + " and w.privilege=:p";
            Map m = new HashMap();
            m.put("wu", webUserRole);
            m.put("p", ph.getPrivilege());
            WebUserRolePrivilege wup = getRoleFacede().findFirstByJpql(jpql, m);
            if (wup == null) {
                wup = new WebUserRolePrivilege();
                wup.setWebUserRole(webUserRole);
                wup.setPrivilege(ph.getPrivilege());
                newWups.add(wup);
            } else {
                wup.setRetired(false);
                oldWups.add(wup);
            }
        }
        getRoleFacede().batchCreate(newWups);
        getRoleFacede().batchEdit(oldWups);
        fillUserRolePrivileges();
        JsfUtil.addSuccessMessage("Updated");
    }

    private static void checkNodes(TreeNode root, List<PrivilegeHolder> privilegesToCheck) {
        if (root == null || privilegesToCheck == null || privilegesToCheck.isEmpty()) {
            return;
        }

        // Cast each child to TreeNode
        for (Object childObject : root.getChildren()) {
            if (childObject instanceof TreeNode) {
                TreeNode childNode = (TreeNode) childObject;
                checkNode(childNode, privilegesToCheck);
            }
        }
    }

    private static void checkNode(TreeNode node, List<PrivilegeHolder> privilegesToCheck) {
        if (node.getData() instanceof PrivilegeHolder) {
            PrivilegeHolder holder = (PrivilegeHolder) node.getData();
            if (privilegesToCheck.contains(holder)) {
                ((DefaultTreeNode) node).setSelected(true);
            }
        }

        // Cast each child to TreeNode
        for (Object childObject : node.getChildren()) {
            if (childObject instanceof TreeNode) {
                TreeNode childNode = (TreeNode) childObject;
                checkNode(childNode, privilegesToCheck);
            }
        }
    }

    private static void unselectTreeNodes(TreeNode root) {
        if (root == null) {
            return;
        }

        // Unselect the current node
        ((DefaultTreeNode) root).setSelected(false);

        // Recursively unselect child nodes
        for (Object childObject : root.getChildren()) {
            if (childObject instanceof TreeNode) {
                TreeNode childNode = (TreeNode) childObject;
                unselectTreeNodes(childNode);
            }
        }
    }

    public void filterPrivileges() {
        collapseAll(rootTreeNode);
        rootTreeNode.setExpanded(true);
        if (searchText == null || searchText.trim().isEmpty()) {
            return;
        }
        String st = searchText.trim().toLowerCase();
        expandMatches(rootTreeNode, st);
    }

    private void collapseAll(TreeNode node) {
        if (node == null) {
            return;
        }
        node.setExpanded(false);
        for (Object childObj : node.getChildren()) {
            if (childObj instanceof TreeNode) {
                collapseAll((TreeNode) childObj);
            }
        }
    }

    private boolean expandMatches(TreeNode node, String search) {
        boolean match = false;
        if (node.getData() instanceof PrivilegeHolder) {
            PrivilegeHolder ph = (PrivilegeHolder) node.getData();
            if (ph.getName() != null && ph.getName().toLowerCase().contains(search)) {
                match = true;
            }
        }
        for (Object childObj : node.getChildren()) {
            if (childObj instanceof TreeNode) {
                if (expandMatches((TreeNode) childObj, search)) {
                    match = true;
                }
            }
        }
        if (match) {
            node.setExpanded(true);
        }
        return match;
    }

    public void fillUserPrivileges() {
        List<WebUserPrivilege> wups;
        if (currentWebUser == null) {
            JsfUtil.addErrorMessage("User?");
        }
        String j = "SELECT i "
                + " FROM WebUserPrivilege i "
                + " where i.webUser=:wu "
                + " and i.retired<>:ret "
                + " and i.department=:dep";
        Map m = new HashMap();
        m.put("wu", currentWebUser);
        m.put("ret", true);
        m.put("dep", department);
        currentWebUserPrivileges = getEjbFacade().findByJpql(j, m);
        currentUserPrivilegeHolders = createPrivilegeHolders(currentWebUserPrivileges);
        unselectTreeNodes(rootTreeNode);
        checkNodes(rootTreeNode, currentUserPrivilegeHolders);
        privilegesLoaded = true;
    }

    public WebUserPrivilege addUserPrivilege(Privileges prv, WebUser wu, Department dept) {
        if (prv == null) {
            return null;
        }
        if (wu == null) {
            return null;
        }
        if (dept == null) {
            return null;
        }
        String j = "SELECT i "
                + " FROM WebUserPrivilege i "
                + " where i.webUser=:wu "
                + " and i.privilege=:p "
                + " and i.department=:dep";
        Map m = new HashMap();
        m.put("wu", wu);
        m.put("p", prv);
        m.put("dep", dept);
        WebUserPrivilege wup = getEjbFacade().findFirstByJpql(j, m);
        if (wup == null) {
            wup = new WebUserPrivilege();
            wup.setDepartment(dept);
            wup.setWebUser(wu);
            wup.setPrivilege(prv);
            getFacade().create(wup);
        } else {
            wup.setRetired(false);
            getFacade().edit(wup);
        }
        return wup;
    }

    public void makePrivilegesNeededToBeReloaded() {
        privilegesLoaded = false;
    }

    public void fillUserRolePrivileges(WebUserRole u) {
        webUserRole = u;
        fillUserRolePrivileges();
    }

    public void fillUserRolePrivileges() {
        List<WebUserRolePrivilege> wups;
        if (webUserRole == null) {
            JsfUtil.addErrorMessage("User Role?");
        }
        String j = "SELECT i "
                + " FROM WebUserRolePrivilege i "
                + " where i.webUserRole=:wu "
                + " and i.retired=:ret ";
        Map m = new HashMap();
        m.put("wu", webUserRole);
        m.put("ret", false);
        currentWebUserRolePrivileges = getRoleFacede().findByJpql(j, m);
        currentUserPrivilegeHolders = createRolePrivilegeHolders(currentWebUserRolePrivileges);
        unselectTreeNodes(rootTreeNode);
        checkNodes(rootTreeNode, currentUserPrivilegeHolders);
    }

    public List<WebUserRolePrivilege> fetchUserPrivileges(WebUserRole role) {
        List<WebUserRolePrivilege> wups;
        if (role == null) {
            return null;
        }
        String j = "SELECT i "
                + " FROM WebUserRolePrivilege i "
                + " where i.webUserRole=:wu "
                + " and i.retired=:ret ";
        Map m = new HashMap();
        m.put("wu", role);
        m.put("ret", false);
        wups = getRoleFacede().findByJpql(j, m);
        return wups;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Navigational Methods">
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public TreeNode[] getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(TreeNode[] selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    private WebUserPrivilegeFacade getEjbFacade() {
        return ejbFacade;
    }

    public WebUser getCurrentWebUser() {
        return currentWebUser;
    }

    public void setCurrentWebUser(WebUser currentWebUser) {
        this.currentWebUser = currentWebUser;
    }

    public List<WebUserPrivilege> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<WebUserPrivilege> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public List<WebUserRolePrivilege> getSelectedRoleItems() {
        return selectedRoleItems;
    }

    public void setSelectedRoleItems(List<WebUserRolePrivilege> selectedRoleItems) {
        this.selectedRoleItems = selectedRoleItems;
    }

    public TreeNode getRootTreeNode() {
        return rootTreeNode;
    }

    public void setRootTreeNode(TreeNode tmp) {
        this.rootTreeNode = tmp;
    }

    private WebUserPrivilegeFacade getFacade() {
        return ejbFacade;

    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<PrivilegeHolder> getCurrentUserPrivilegeHolders() {
        if (currentUserPrivilegeHolders == null) {
            currentUserPrivilegeHolders = new ArrayList<>();
        }
        return currentUserPrivilegeHolders;
    }

    public void setCurrentUserPrivilegeHolders(List<PrivilegeHolder> currentUserPrivilegeHolders) {
        this.currentUserPrivilegeHolders = currentUserPrivilegeHolders;
    }

    public List<WebUserPrivilege> getCurrentWebUserPrivileges() {
        if (currentWebUserPrivileges == null) {
            currentWebUserPrivileges = new ArrayList<>();
        }
        return currentWebUserPrivileges;
    }

    public void setCurrentWebUserPrivileges(List<WebUserPrivilege> currentWebUserPrivileges) {
        this.currentWebUserPrivileges = currentWebUserPrivileges;
    }

    public List<WebUserRolePrivilege> getCurrentWebUserRolePrivileges() {
        if (currentWebUserRolePrivileges == null) {
            currentWebUserRolePrivileges = new ArrayList<>();
        }
        return currentWebUserRolePrivileges;
    }

    public void setCurrentWebUserRolePrivileges(List<WebUserRolePrivilege> currentWebUserRolePrivileges) {
        this.currentWebUserRolePrivileges = currentWebUserRolePrivileges;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public WebUserRole getWebUserRole() {
        return webUserRole;
    }

    public void setWebUserRole(WebUserRole webUserRole) {
        this.webUserRole = webUserRole;
    }

    public WebUserRolePrivilegeFacade getRoleFacede() {
        return facede;
    }

    public void setRoleFacede(WebUserRolePrivilegeFacade facede) {
        this.facede = facede;
    }

    public boolean isPrivilegesLoaded() {
        return privilegesLoaded;
    }

    public void setPrivilegesLoaded(boolean privilegesLoaded) {
        this.privilegesLoaded = privilegesLoaded;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converters">
    /**
     *
     */
    @FacesConverter(forClass = WebUserPrivilege.class)
    public static class WebUserPrivilegeControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UserPrivilageController controller = (UserPrivilageController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "userPrivilegeController");
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
            if (object instanceof WebUserPrivilege) {
                WebUserPrivilege o = (WebUserPrivilege) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + UserPrivilageController.class.getName());
            }
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Innver Classes">
    public class PrivilegeHolder {

        private Privileges privilege;
        private String name;

        public PrivilegeHolder() {
        }

        public PrivilegeHolder(Privileges privilege, String name) {
            this.privilege = privilege;
            this.name = name;
        }

        public PrivilegeHolder(String name) {
            this.name = name;
        }

        public Privileges getPrivilege() {
            return privilege;
        }

        public void setPrivilege(Privileges privilege) {
            this.privilege = privilege;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 73 * hash + Objects.hashCode(this.privilege);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PrivilegeHolder other = (PrivilegeHolder) obj;
            return this.privilege == other.privilege;
        }

    }
    // </editor-fold>
}
