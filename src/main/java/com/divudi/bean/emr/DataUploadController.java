package com.divudi.bean.emr;

import com.divudi.bean.clinical.ClinicalEntityController;
import com.divudi.bean.clinical.PhotoCamBean;
import com.divudi.core.entity.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.primefaces.model.DefaultStreamedContent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import com.divudi.bean.clinical.DiagnosisController;
import com.divudi.bean.clinical.PatientEncounterController;
import com.divudi.bean.common.AgencyController;
import com.divudi.bean.common.AreaController;
import com.divudi.bean.common.CategoryController;
import com.divudi.bean.common.ConfigOptionApplicationController;
import com.divudi.bean.common.ConsultantController;
import com.divudi.bean.common.CreditCompanyController;
import com.divudi.bean.common.DepartmentController;
import com.divudi.bean.common.DoctorController;
import com.divudi.bean.common.DoctorSpecialityController;
import com.divudi.bean.common.EnumController;
import com.divudi.bean.common.FeeValueController;
import com.divudi.bean.common.InstitutionController;
import com.divudi.bean.common.ItemController;
import com.divudi.bean.common.ItemFeeController;
import com.divudi.bean.common.ItemFeeManager;
import com.divudi.bean.common.PatientController;
import com.divudi.bean.common.RelationController;
import com.divudi.bean.common.RouteController;
import com.divudi.bean.common.ServiceController;
import com.divudi.bean.common.SessionController;
import com.divudi.bean.common.SpecialityController;
import com.divudi.bean.hr.StaffController;
import com.divudi.bean.lab.CollectingCentreController;
import com.divudi.bean.lab.InvestigationCategoryController;
import com.divudi.bean.lab.InvestigationController;
import com.divudi.bean.lab.InvestigationTubeController;
import com.divudi.bean.lab.MachineController;
import com.divudi.bean.lab.SampleController;
import com.divudi.bean.pharmacy.AmpController;
import com.divudi.bean.pharmacy.AtmController;
import com.divudi.bean.pharmacy.MeasurementUnitController;
import com.divudi.bean.pharmacy.VmpController;
import com.divudi.bean.pharmacy.VtmController;
import com.divudi.core.data.CollectingCentrePaymentMethod;
import com.divudi.core.data.EncounterType;
import com.divudi.core.data.FeeType;
import com.divudi.core.data.InstitutionType;
import com.divudi.core.data.DepartmentType;
import com.divudi.core.data.Sex;
import com.divudi.core.data.Title;
import com.divudi.core.data.inward.InwardChargeType;
import com.divudi.core.entity.clinical.ClinicalEntity;
import com.divudi.core.entity.lab.Investigation;
import com.divudi.core.entity.lab.InvestigationTube;
import com.divudi.core.entity.lab.Machine;
import com.divudi.core.entity.lab.Sample;
import com.divudi.core.entity.pharmacy.Amp;
import com.divudi.core.entity.pharmacy.Atm;
import com.divudi.core.entity.pharmacy.MeasurementUnit;
import com.divudi.core.entity.pharmacy.Vmp;
import com.divudi.core.entity.pharmacy.Vtm;
import com.divudi.core.facade.CategoryFacade;
import com.divudi.core.facade.ItemFacade;
import com.divudi.core.facade.ItemFeeFacade;
import com.divudi.core.facade.PatientFacade;
import com.divudi.core.facade.PersonFacade;
import com.divudi.core.facade.VtmFacade;
import com.divudi.core.util.JsfUtil;
import com.divudi.bean.membership.MembershipSchemeController;
import com.divudi.bean.pharmacy.PharmacyPurchaseController;
import com.divudi.core.data.ItemLight;
import com.divudi.core.data.SymanticHyrachi;
import com.divudi.core.data.SymanticType;
import com.divudi.core.data.dataStructure.PharmacyImportCol;
import com.divudi.ejb.PharmacyBean;
import com.divudi.core.entity.inward.InwardService;
import com.divudi.core.entity.membership.MembershipScheme;
import com.divudi.core.entity.pharmacy.Ampp;
import com.divudi.core.entity.pharmacy.PharmaceuticalItemCategory;
import com.divudi.core.entity.pharmacy.PharmaceuticalItemType;
import com.divudi.core.entity.pharmacy.VirtualProductIngredient;
import com.divudi.core.entity.pharmacy.Vmpp;
import com.divudi.core.facade.AmpFacade;
import com.divudi.core.facade.AmppFacade;
import com.divudi.core.facade.AtmFacade;
import com.divudi.core.facade.FamilyFacade;
import com.divudi.core.facade.FamilyMemberFacade;
import com.divudi.core.facade.FeeFacade;
import com.divudi.core.facade.VmpFacade;
import com.divudi.core.facade.VmppFacade;
import com.divudi.core.util.CommonFunctions;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

@Named
@ViewScoped
public class DataUploadController implements Serializable {

    @Inject
    MembershipSchemeController membershipSchemeController;
    @Inject
    PatientController patientController;
    @Inject
    ItemController itemController;
    @Inject
    SessionController sessionController;
    @Inject
    VtmController vtmController;
    @Inject
    AtmController atmController;
    @Inject
    AmpController ampController;
    @Inject
    CategoryController categoryController;
    @Inject
    MeasurementUnitController measurementUnitController;
    @Inject
    private InstitutionController institutionController;
    @Inject
    VmpController vmpController;
    @Inject
    DiagnosisController diagnosisController;
    @Inject
    RelationController relationController;
    @Inject
    PatientEncounterController patientEncounterController;
    @Inject
    ServiceController serviceController;
    @Inject
    InvestigationController investigationController;
    @Inject
    DepartmentController departmentController;
    @Inject
    EnumController enumController;
    @Inject
    SampleController sampleController;
    @Inject
    InvestigationTubeController investigationTubeController;
    @Inject
    MachineController machineController;
    @Inject
    InvestigationCategoryController investigationCategoryController;
    @Inject
    ItemFeeManager itemFeeManager;
    @Inject
    ItemFeeController itemFeeController;
    @Inject
    CollectingCentreController collectingCentreController;
    @Inject
    AgencyController agencyController;
    @Inject
    RouteController routeController;
    @Inject
    DoctorSpecialityController doctorSpecialityController;
    @Inject
    ConsultantController consultantController;
    @Inject
    SpecialityController specialityController;
    @Inject
    StaffController staffController;
    @Inject
    AreaController areaController;
    @Inject
    CreditCompanyController creditCompanyController;
    @Inject
    DoctorController doctorController;
    @Inject
    ClinicalEntityController clinicalEntityController;
    @Inject
    FeeValueController feeValueController;
    @Inject
    private PharmacyPurchaseController pharmacyPurchaseController;
    @Inject
    ConfigOptionApplicationController configOptionApplicationController;

    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    PatientFacade patientFacade;
    @EJB
    FamilyMemberFacade familyMemberFacade;
    @EJB
    FamilyFacade familyFacade;
    @EJB
    PersonFacade personFacade;
    @EJB
    private VtmFacade vtmFacade;
    @EJB
    ItemFeeFacade itemFeeFacade;
    @EJB
    FeeFacade feeFacade;
    @EJB
    ItemFacade itemFacade;
    @EJB
    CategoryFacade categoryFacade;
    @EJB
    private AtmFacade atmFacade;
    @EJB
    private AmpFacade ampFacade;
    @EJB
    private VmpFacade vmpFacade;
    @EJB
    private AmppFacade amppFacade;
    @EJB
    private VmppFacade vmppFacade;

    private Institution institution;
    private Department department;

    private UploadedFile file;
    private String outputString;
    private List<Item> items;
    private List<ItemFee> itemFees;
    private Category selectedFeeList;
    private List<String> uploadErrors;
    private String uploadErrorDetails;
    private List<Institution> collectingCentres;
    private List<Institution> agencies;
    private List<Institution> suppliers;
    private List<Department> departments;
    private List<Area> areas;
    private List<Route> routes;
    private StreamedContent templateForItemWithFeeUpload;
    private StreamedContent templateForCollectingCentreUpload;
    private StreamedContent templateForsupplierUpload;
    private StreamedContent templateForInvestigationUpload;
    private StreamedContent templateForDiagnosisUpload;
    private StreamedContent templateForPatientUpload;
    private StreamedContent templateForVisitUpload;
    private StreamedContent templateForVtmUpload;
    private StreamedContent templateForAtmUpload;
    private StreamedContent templateForVmpUpload;
    private StreamedContent templateForAmpUpload;
    private StreamedContent templateForAmpMinimalUpload;
    private StreamedContent templateForCreditCompanyUpload;
    private StreamedContent templateForItemFeeUpload;
    private StreamedContent templateForDepartmentUpload;

    List<Item> itemsToSave;
    List<Item> itemsSaved;
    List<Item> itemsSkipped;
    private List<Item> itemsUpdated;
    List<Item> masterItemsToSave;
    List<ItemFee> itemFeesToSave;
    List<Category> categoriesSaved;
    List<Institution> institutionsSaved;
    List<Department> departmentsSaved;
    private List<Consultant> consultantsToSave;
    private List<Institution> creditCompanies;

    private List<Doctor> doctorsTosave;
    private List<Staff> staffToSave;
    private List<Patient> savedPatients;

    private boolean pollActive;
    private boolean uploadComplete;

    private List<ClinicalEntity> surgeries;
    private List<ClinicalEntity> surgeriesToSave;
    private List<ClinicalEntity> surgeriesToSkiped;

    private int number = 0;
    private int catCol = 1;
    private int ampCol = 2;
    private int codeCol = 3;
    private int barcodeCol = 4;
    private int vtmCol = 5;
    private int strengthOfIssueUnitCol = 6;
    private int strengthUnitCol = 7;
    private int issueUnitsPerPackCol = 8;
    private int issueUnitCol = 9;
    private int packUnitCol = 10;
    private int distributorCol = 11;
    private int manufacturerCol = 12;
    private int importerCol = 13;
    private int departmentTypeCol = 14;
    private int doeCol = 15;
    private int batchCol = 16;
    private int stockQtyCol = 17;
    private int pruchaseRateCol = 17;
    private int saleRateCol = 18;
    private List<PharmacyImportCol> itemNotPresent;
    private List<String> itemsWithDifferentGenericName;
    private List<String> itemsWithDifferentCode;

    private List<ItemFee> uploadeditemFees;
    private List<ItemLight> rejecteditemFees;

    private int startRow = 1;

    private boolean skipDepartmentTypeColumn;
    private DepartmentType defaultDepartmentType = DepartmentType.Pharmacy;

    public String navigateToRouteUpload() {
        uploadComplete = false;
        return "/admin/institutions/route_upload?faces-redirect=true";
    }

    public String navigateToCollectingCenterUpload() {
        uploadComplete = false;
        return "/admin/institutions/collecting_centre_upload?faces-redirect=true";
    }

    public String navigateToAgencyUpload() {
        uploadComplete = false;
        return "/admin/institutions/agency_upload?faces-redirect=true";
    }

    public String navigateToDepartmentUpload() {
        uploadComplete = false;
        return "/admin/institutions/department_upload?faces-redirect=true";
    }

    public String navigateToUploadMembers() {
        uploadComplete = false;
        return "/membership/upload_members?faces-redirect=true";
    }

    public String navigateToSupplierUpload() {
        uploadComplete = false;
        return "/admin/institutions/supplier_upload?faces-redirect=true";
    }

    public String navigateToSupplierUploadExtended() {
        uploadComplete = false;
        return "/admin/institutions/supplier_upload_extended?faces-redirect=true";
    }

    public String importToExcelWithStock() {
        if (file == null) {
            JsfUtil.addErrorMessage("No File");
            return "";
        }
        if (file.getFileName() == null) {
            JsfUtil.addErrorMessage("No File");
            return "";
        }
        catCol = 1;                   // B
        ampCol = 2;                   // C
        codeCol = 3;                  // D
        barcodeCol = 4;               // E
        vtmCol = 5;                   // F
        strengthOfIssueUnitCol = 6;  // G
        strengthUnitCol = 7;         // H
        issueUnitsPerPackCol = 8;    // I
        issueUnitCol = 9;            // J
        packUnitCol = 10;            // K
        distributorCol = 11;         // L
        manufacturerCol = 12;        // M
        importerCol = 13;            // N
        doeCol = 14;                 // O
        batchCol = 15;               // P
        stockQtyCol = 16;            // Q
        pruchaseRateCol = 17;        // R
        saleRateCol = 18;            // S

        startRow = 1; // If header is on row 0

        String strCat;
        String strAmp;
        String strCode;
        String strBarcode;
        String strGenericName;
        String strStrength;
        String strStrengthUnit;
        String strPackSize;
        String strIssueUnit;
        String strPackUnit;
        String strDistributor;
        String strManufacturer;
        String strImporter;

        PharmaceuticalItemCategory cat;
        PharmaceuticalItemType phType;
        Vtm vtm;
        Atm atm;
        Vmp vmp;
        Amp amp;
        Ampp ampp;
        Vmpp vmpp;
        VirtualProductIngredient vtmsvmps;
        MeasurementUnit issueUnit;
        MeasurementUnit strengthUnit;
        MeasurementUnit packUnit;
        double strengthUnitsPerIssueUnit;
        double issueUnitsPerPack;
        Institution distributor;
        Institution manufacturer;
        Institution importer;

        double stockQty;
        double pp;
        double sp;
        String batch;
        Date doe;
        StringBuilder warningMessages = new StringBuilder();

        int rowCount = 0;

        try (InputStream in = file.getInputStream(); Workbook workbook = new XSSFWorkbook(in)) {
            rowCount++;
            System.out.println("rowCount at Start of a row= " + rowCount);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            JsfUtil.addSuccessMessage(file.getFileName());

            pharmacyPurchaseController.makeNull();

            int rowIndex = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
//                System.out.println("row = " + row);
                if (rowIndex++ < startRow) {
                    continue; // Skip header or initial rows as per the startRow value
                }

                Map<String, Object> m = new HashMap<>();

                // Category
                Cell catCell = row.getCell(catCol);
                strCat = getStringCellValue(catCell);
                if (strCat == null || strCat.trim().isEmpty()) {
                    continue;
                }
                cat = getPharmacyBean().getPharmaceuticalCategoryByName(strCat);
                if (cat == null) {
                    continue;
                }
                phType = getPharmacyBean().getPharmaceuticalItemTypeByName(strCat);

                // Strength Unit
                Cell strengthUnitCell = row.getCell(strengthUnitCol);
                strStrengthUnit = getStringCellValue(strengthUnitCell);
                strengthUnit = getPharmacyBean().getUnitByName(strStrengthUnit);
                if (strengthUnit == null) {
                    continue;
                }

                // Pack Unit
                Cell packUnitCell = row.getCell(packUnitCol);
                strPackUnit = getStringCellValue(packUnitCell);
                packUnit = getPharmacyBean().getUnitByName(strPackUnit);
                if (packUnit == null) {
                    continue;
                }

                // Issue Unit
                Cell issueUnitCell = row.getCell(issueUnitCol);
                strIssueUnit = getStringCellValue(issueUnitCell);
                issueUnit = getPharmacyBean().getUnitByName(strIssueUnit);
                if (issueUnit == null) {
                    continue;
                }

                // Strength Of A Measurement Unit
                Cell strengthCell = row.getCell(strengthOfIssueUnitCol);
                strStrength = getCellValueAsString(strengthCell);
                strengthUnitsPerIssueUnit = parseDouble(strStrength);

                // Issue Units Per Pack
                Cell packSizeCell = row.getCell(issueUnitsPerPackCol);
                strPackSize = getCellValueAsString(packSizeCell);
                issueUnitsPerPack = parseDouble(strPackSize);

                // Vtm
                Cell vtmCell = row.getCell(vtmCol);
                strGenericName = getCellValueAsString(vtmCell);
                vtm = !strGenericName.isEmpty() ? getPharmacyBean().getVtmByName(strGenericName) : null;

                // Vmp
                vmp = getPharmacyBean().getVmp(vtm, strengthUnitsPerIssueUnit, strengthUnit, cat);
                if (vmp == null) {
                    continue;
                } else {
                    vmp.setCategory(phType);
                    getVmpFacade().edit(vmp);
                }

                // Code & Barcode
                strCode = getCellValueAsString(row.getCell(codeCol));
                strBarcode = getCellValueAsString(row.getCell(barcodeCol));

                // Distributor
                strDistributor = getCellValueAsString(row.getCell(distributorCol));

                // Amp
                Cell ampCell = row.getCell(ampCol);
                strAmp = getCellValueAsString(ampCell);
                m.put("v", vmp);
                m.put("n", strAmp.toUpperCase());
                amp = ampFacade.findFirstByJpql("SELECT c FROM Amp c Where c.retired=false and (c.name)=:n AND c.vmp=:v", m);
                if (amp == null) {
                    amp = new Amp();
                    amp.setName(strAmp);
                    amp.setCode(strCode);
                    amp.setBarcode(strBarcode);
                    amp.setMeasurementUnit(strengthUnit);
                    amp.setIssueUnit(issueUnit);
                    amp.setStrengthUnit(strengthUnit);
                    amp.setDblValue(strengthUnitsPerIssueUnit);
                    amp.setCategory(cat);
                    amp.setVmp(vmp);
                    getAmpFacade().create(amp);
                } else {
                    amp.setRetired(false);
                    getAmpFacade().edit(amp);
                }

                if (amp == null) {
                    continue;
                }

                // Ampp
                if (issueUnitsPerPack > 1.0) {
                    ampp = getPharmacyBean().getAmpp(amp, issueUnitsPerPack, packUnit);
                }
                // Set Code and Barcode
                amp.setCode(strCode);
                getAmpFacade().edit(amp);
                amp.setBarcode(strBarcode);
                getAmpFacade().edit(amp);

                // Manufacturer
                strManufacturer = getCellValueAsString(row.getCell(manufacturerCol));
                manufacturer = getInstitutionController().getInstitutionByName(strManufacturer, InstitutionType.Manufacturer);
                amp.setManufacturer(manufacturer);

                // Importer
                strImporter = getCellValueAsString(row.getCell(importerCol));
                importer = getInstitutionController().getInstitutionByName(strImporter, InstitutionType.Importer);
                amp.setManufacturer(importer);

                // Stock Quantity, Purchase Rate, Sale Rate, Batch, Date of Expiry
                stockQty = parseDouble(getCellValueAsString(row.getCell(stockQtyCol)));
                pp = parseDouble(getCellValueAsString(row.getCell(pruchaseRateCol)));
                sp = parseDouble(getCellValueAsString(row.getCell(saleRateCol)));
                batch = getCellValueAsString(row.getCell(batchCol));
                doe = parseDate(getCellValueAsString(row.getCell(doeCol)));

                System.out.println("amp = " + amp);
                System.out.println("stockQty = " + stockQty);
                getPharmacyPurchaseController().getCurrentBillItem().setItem(amp);
                getPharmacyPurchaseController().getCurrentBillItem().setTmpQty(stockQty);
                getPharmacyPurchaseController().getCurrentBillItem().getPharmaceuticalBillItem().setPurchaseRate(pp);
                getPharmacyPurchaseController().getCurrentBillItem().getPharmaceuticalBillItem().setRetailRate(sp);
                getPharmacyPurchaseController().getCurrentBillItem().getPharmaceuticalBillItem().setDoe(doe);
                if (batch == null || batch.trim().isEmpty()) {
                    getPharmacyPurchaseController().setBatch();
                } else {
                    getPharmacyPurchaseController().getCurrentBillItem().getPharmaceuticalBillItem().setStringValue(batch);
                }
                getPharmacyPurchaseController().addItem();
                System.out.println("rowCount at End of a row= " + rowCount);
            }
            if (warningMessages.length() > 0) {
                getPharmacyPurchaseController().setWarningMessage(warningMessages.toString());
            }
            JsfUtil.addSuccessMessage("Successful. All the data in Excel File Imported to the database");
            return "/pharmacy/pharmacy_purchase";
        } catch (IOException e) {
            JsfUtil.addErrorMessage(e.getMessage());
            return "";
        }
    }

    public String importFromExcelWithoutStock() {
        if (file == null) {
            JsfUtil.addErrorMessage("No File");
            return "";
        }
        if (file.getFileName() == null) {
            JsfUtil.addErrorMessage("No File");
            return "";
        }

        String strCat;
        String strAmp;
        String strCode;
        String strBarcode;
        String strGenericName;
        String strStrength;
        String strStrengthUnit;
        String strPackSize;
        String strIssueUnit;
        String strPackUnit;
        String strDistributor;
        String strManufacturer;
        String strImporter;

        PharmaceuticalItemCategory cat;
        PharmaceuticalItemType phType;
        Vtm vtm;
        Atm atm;
        Vmp vmp;
        Amp amp;
        Ampp ampp;
        Vmpp vmpp;
        VirtualProductIngredient vtmsvmps;
        MeasurementUnit issueUnit;
        MeasurementUnit strengthUnit;
        MeasurementUnit packUnit;
        double strengthUnitsPerIssueUnit;
        double issueUnitsPerPack;
        Institution distributor;
        Institution manufacturer;
        Institution importer;

        StringBuilder warningMessages = new StringBuilder();

        int rowCount = 0;

        try (InputStream in = file.getInputStream(); Workbook workbook = new XSSFWorkbook(in)) {
            rowCount++;
            System.out.println("rowCount at Start of a row= " + rowCount);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            JsfUtil.addSuccessMessage(file.getFileName());

            int rowIndex = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (rowIndex++ < startRow) {
                    continue;
                }

                Map<String, Object> m = new HashMap<>();

                Cell catCell = row.getCell(catCol);
                strCat = getStringCellValue(catCell);
                if (strCat == null || strCat.trim().isEmpty()) {
                    continue;
                }
                cat = getPharmacyBean().getPharmaceuticalCategoryByName(strCat);
                if (cat == null) {
                    continue;
                }
                phType = getPharmacyBean().getPharmaceuticalItemTypeByName(strCat);

                Cell strengthUnitCell = row.getCell(strengthUnitCol);
                strStrengthUnit = getStringCellValue(strengthUnitCell);
                strengthUnit = getPharmacyBean().getUnitByName(strStrengthUnit);
                if (strengthUnit == null) {
                    continue;
                }

                Cell packUnitCell = row.getCell(packUnitCol);
                strPackUnit = getStringCellValue(packUnitCell);
                packUnit = getPharmacyBean().getUnitByName(strPackUnit);
                if (packUnit == null) {
                    continue;
                }

                Cell issueUnitCell = row.getCell(issueUnitCol);
                strIssueUnit = getStringCellValue(issueUnitCell);
                issueUnit = getPharmacyBean().getUnitByName(strIssueUnit);
                if (issueUnit == null) {
                    continue;
                }

                Cell strengthCell = row.getCell(strengthOfIssueUnitCol);
                strStrength = getCellValueAsString(strengthCell);
                strengthUnitsPerIssueUnit = parseDouble(strStrength);

                Cell packSizeCell = row.getCell(issueUnitsPerPackCol);
                strPackSize = getCellValueAsString(packSizeCell);
                issueUnitsPerPack = parseDouble(strPackSize);

                Cell vtmCell = row.getCell(vtmCol);
                strGenericName = getCellValueAsString(vtmCell);
                vtm = !strGenericName.isEmpty() ? getPharmacyBean().getVtmByName(strGenericName) : null;

                vmp = getPharmacyBean().getVmp(vtm, strengthUnitsPerIssueUnit, strengthUnit, cat);
                if (vmp == null) {
                    continue;
                } else {
                    vmp.setCategory(phType);
                    getVmpFacade().edit(vmp);
                }

                strCode = getCellValueAsString(row.getCell(codeCol));
                strBarcode = getCellValueAsString(row.getCell(barcodeCol));

                strDistributor = getCellValueAsString(row.getCell(distributorCol));
                
                DepartmentType deptType = null;
                if (!skipDepartmentTypeColumn) {
                    String strDepartmentType = getCellValueAsString(row.getCell(departmentTypeCol));
                    deptType = departmentController.findDepartmentType(strDepartmentType);
                }
                if (deptType == null) {
                    deptType = defaultDepartmentType != null ? defaultDepartmentType : DepartmentType.Pharmacy;
                }

                Cell ampCell = row.getCell(ampCol);
                strAmp = getCellValueAsString(ampCell);
                m.put("v", vmp);
                m.put("n", strAmp.toUpperCase());
                amp = ampFacade.findFirstByJpql("SELECT c FROM Amp c Where c.retired=false and (c.name)=:n AND c.vmp=:v", m);
                if (amp == null) {
                    amp = new Amp();
                    amp.setName(strAmp);
                    amp.setCode(strCode);
                    amp.setBarcode(strBarcode);
                    amp.setMeasurementUnit(strengthUnit);
                    amp.setIssueUnit(issueUnit);
                    amp.setStrengthUnit(strengthUnit);
                    amp.setDblValue(strengthUnitsPerIssueUnit);
                    amp.setCategory(cat);
                    amp.setVmp(vmp);
                    amp.setDepartmentType(deptType);
                    getAmpFacade().create(amp);
                } else {
                    amp.setRetired(false);
                    getAmpFacade().edit(amp);
                }

                if (amp == null) {
                    continue;
                }

                if (issueUnitsPerPack > 1.0) {
                    ampp = getPharmacyBean().getAmpp(amp, issueUnitsPerPack, packUnit);
                }
                amp.setCode(strCode);
                getAmpFacade().edit(amp);
                amp.setBarcode(strBarcode);
                getAmpFacade().edit(amp);

                strManufacturer = getCellValueAsString(row.getCell(manufacturerCol));
                manufacturer = getInstitutionController().getInstitutionByName(strManufacturer, InstitutionType.Manufacturer);
                amp.setManufacturer(manufacturer);

                strImporter = getCellValueAsString(row.getCell(importerCol));
                importer = getInstitutionController().getInstitutionByName(strImporter, InstitutionType.Importer);
                amp.setImporter(importer);

                amp.setDepartmentType(deptType);

                System.out.println("amp = " + amp);
                System.out.println("rowCount at End of a row= " + rowCount);
            }
            if (warningMessages.length() > 0) {
                JsfUtil.addErrorMessage(warningMessages.toString());
            }
            JsfUtil.addSuccessMessage("Successful. All the data in Excel File Imported to the database");
            return "";
        } catch (IOException e) {
            JsfUtil.addErrorMessage(e.getMessage());
            return "";
        }
    }

    private String getStringCellValue(Cell cell) {
        return (cell != null && cell.getCellType() == CellType.STRING) ? cell.getStringCellValue() : "";
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        return new SimpleDateFormat("M/d/yyyy", Locale.ENGLISH).format(cell.getDateCellValue());
                    } catch (Exception e) {
                        return new SimpleDateFormat("d/M/yyyy", Locale.ENGLISH).format(cell.getDateCellValue());
                    }
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    public String importFromExcelWithoutStockLab() {
        skipDepartmentTypeColumn = true;
        defaultDepartmentType = DepartmentType.Lab;
        return importFromExcelWithoutStock();
    }

    public String importFromExcelWithoutStockStore() {
        skipDepartmentTypeColumn = true;
        defaultDepartmentType = DepartmentType.Store;
        return importFromExcelWithoutStock();
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Date parseDate(String value) {
        try {
            return new SimpleDateFormat("M/d/yyyy", Locale.ENGLISH).parse(value);
        } catch (Exception e) {
            try {
                return new SimpleDateFormat("d/M/yyyy", Locale.ENGLISH).parse(value);
            } catch (Exception ex) {
                return new Date();
            }
        }
    }

    public void uploadRoutes() {
        routes = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                System.out.println("inputStream = " + inputStream);
                routes = readRoutesFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                uploadComplete = false;
                JsfUtil.addErrorMessage("Error in Uploading. " + e.getMessage());
            }
        }
        uploadComplete = true;
        JsfUtil.addSuccessMessage("Successfully Uploaded");
    }

    private List<Route> readRoutesFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Route> routes = new ArrayList<>();
        Institution institution;

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Route route = null;

            String routeCode = null;
            String name = null;
            String institutionName = null;

            Cell routeCodeCell = row.getCell(0);
            if (routeCodeCell != null && routeCodeCell.getCellType() == CellType.STRING) {
                routeCode = routeCodeCell.getStringCellValue();
            }

            Cell nameCell = row.getCell(1);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();
            }

            Cell institutionCell = row.getCell(2);
            if (institutionCell != null && institutionCell.getCellType() == CellType.STRING) {
                institutionName = institutionCell.getStringCellValue();
            }
            if (institutionName == null || institutionName.trim().equals("")) {
                institution = sessionController.getInstitution();
            }
            institution = institutionController.findAndSaveInstitutionByName(institutionName);

            Route r = routeController.findRouteByName(name);
            if (r == null) {
                r = new Route();
                r.setName(name);
                r.setInstitution(institution);
                r.setCreatedAt(new Date());
                r.setCreater(sessionController.getLoggedUser());
                routeController.save(r);
            }
        }

        return routes;

    }

    public void uploadPatientAreas() {
        areas = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                areas = readAreasFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadInstitutionItemFees() {
        itemFees = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                itemFees = readInstitutionItemFeeFromXcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Area> readAreasFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Area> areas = new ArrayList<>();
        Area area;

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            area = null;
            String name = null;
            Cell nameCell = row.getCell(0);

            if (nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();
            }
            if (name == null || name.trim().equals("")) {
                continue;
            }

            area = areaController.findAreaByName(name);
            if (area == null) {
                area = new Area();
            }
            area.setName(name);
            areaController.save(area);

            areas.add(area);
        }
        areaController.recreateModel();
        return areas;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public String navigateToUploadCollectingCentreFees() {
        pollActive = false;
        file = null;
        return "/admin/items/item_and_fee_upload_for_collecting_Centres?faces-redirect=true";
    }

    public String navigateToUploadItemAndFees() {
        pollActive = false;
        file = null;
        return "/admin/items/item_and_fees_upload?faces-redirect=true";
    }

    public String navigateToUploadItemAndFeesWithForeginerFee() {
        pollActive = false;
        file = null;
        return "/admin/items/item_and_fees_upload_with_foreigner_fee?faces-redirect=true";
    }

    public String navigateToUploadOutSourceInvestigationFees() {
        pollActive = false;
        file = null;
        return "/admin/items/item_and_fee_upload_for_outsource_Investigation?faces-redirect=true";
    }

    public String navigateToUploadOpdItemsAndHospitalFees() {
        pollActive = false;
        return "/admin/items/opd_items_and_hospital_fee_upload?faces-redirect=true";
    }

    public String navigateToUploadToCorrectCode() {
        pollActive = false;
        return "/admin/items/opd_items_upload_to_correct_code?faces-redirect=true";
    }

    public String navigateToUploadOpdItemsAndDoctorFees() {
        pollActive = false;
        return "/admin/items/opd_items_and_doctor_fee_upload?faces-redirect=true";
    }

    public String navigateToCollectingCentreSpecialFeeUpload() {
        pollActive = false;
        return "/admin/items/collecting_centre_special_fee_upload?faces-redirect=true";
    }

    public String navigateToUploadAndAddProfessionalFees() {
        pollActive = false;
        return "/admin/items/upload_add_professional_fees?faces-redirect=true";
    }

    public String navigateToUploadDiagnoses() {
        return "/emr/admin/upload_diagnoses?faces-redirect=true";
    }

    public String navigateToUploadVisits() {
        return "/emr/admin/upload_visits?faces-redirect=true";
    }

    public String navigateToUploadConsultants() {
        return "/admin/staff/upload_consultants?faces-redirect=true";
    }

    public String navigateToUploadDoctors() {
        return "/admin/staff/upload_doctors?faces-redirect=true";
    }

    public String navigateToUploadStaff() {
        return "/admin/staff/upload_staff?faces-redirect=true";
    }

    public String toUploadPatients() {
        return "/emr/admin/upload_patients?faces-redirect=true";
    }

    public String toUploadVtms() {
        return "/pharmacy/admin/upload_vtms?faces-redirect=true";
    }

    public String toUploadAtms() {
        return "/pharmacy/admin/upload_atms?faces-redirect=true";
    }

    public String toUploadAmps() {
        return "/pharmacy/admin/upload_amps?faces-redirect=true";
    }

    public String toUploadAmpsMin() {
        return "/pharmacy/admin/upload_amps_minimal?faces-redirect=true";
    }

    public String toUploadVmps() {
        return "/pharmacy/admin/upload_vmps?faces-redirect=true";
    }

    public void uploadPatients() {
        List<Patient> patients;

        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                patients = readPatientDataFromExcel(inputStream);
                int i = 0;
                for (Patient p : patients) {
                    personFacade.create(p.getPerson());
                    patientFacade.create(p);
                    i++;
                }

                JsfUtil.addSuccessMessage("Uploaded Successfully");

                // Persist patients to the database or perform other operations
                // patientService.save(patients);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadVisits() {
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                readVisitDataFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadVtms() {
        List<Vtm> vtms;
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                vtms = readVtmsFromExcel(inputStream);
                for (Vtm v : vtms) {
                    vtmController.findAndSaveVtmByNameAndCode(v);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadAtms() {
        List<Atm> atms;
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                atms = readAtmsFromExcel(inputStream);
                for (Atm v : atms) {
                    atmController.findAndSaveAtmByNameAndCode(v, v.getVtm());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadAmps() {
        List<Amp> amps;
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                readAmpsFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadItemsAndHospitalFees() {
        items = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                items = readOpdItemsAndFeesFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadItemsAndDoctorFees() {
        items = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                items = readOpdItemsAndDoctorFeesFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadSurgeries() {
        surgeries = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                surgeries = readSurgeriesFromExcel(inputStream);
                System.out.println("surgeries = " + surgeries.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadAddProfessionalFees() {
        itemFees = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                itemFees = addProfessionalFeesFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<ItemFee> addProfessionalFeesFromExcel(InputStream inputStream) throws IOException {
        List<ItemFee> itemFeesToSave = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Item> itemsToSave = new ArrayList<>();

        // The first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String code = getCellValueAsString(row.getCell(0));
            String name = getCellValueAsString(row.getCell(1)); // Keeping name but not used for search
            String specialityName = getCellValueAsString(row.getCell(2));
            String staffName = getCellValueAsString(row.getCell(3));

            Double professionalFee = getCellValueAsDouble(row.getCell(4));
            Double foreignerFee = getCellValueAsDouble(row.getCell(5));

            if (code == null || code.trim().isEmpty()) {
                continue;
            }

            Item item = itemController.findItemByCode(code);
            if (item == null) {
                continue;
            }

            Speciality speciality = specialityName != null && !specialityName.trim().isEmpty()
                    ? specialityController.findSpeciality(specialityName, false)
                    : null;

            Staff staff = staffController.findStaffByName(staffName);

            ItemFee itf = new ItemFee();
            itf.setName("Professional Fee");
            itf.setItem(item);
            itf.setFeeType(FeeType.Staff);
            itf.setFee(professionalFee);
            itf.setFfee(foreignerFee); // Setting foreigner fee separately
            itf.setCreatedAt(new Date());
            itf.setCreater(sessionController.getLoggedUser());
            itf.setSpeciality(speciality);
            itf.setStaff(staff);

            itemFeeFacade.create(itf);
            itemFeesToSave.add(itf);

            Double total = item.getTotal() != null ? item.getTotal() : 0.0;
            Double totalForForeginer = item.getTotalForForeigner() != null ? item.getTotalForForeigner() : 0.0;
            item.setTotal(total + professionalFee);
            item.setTotalForForeigner(totalForForeginer + foreignerFee);
            item.setDblValue(total + professionalFee);

            itemFacade.edit(item);
            itemsToSave.add(item);
        }

        return itemFeesToSave;
    }

    // Helper method to safely retrieve numeric values from cells
    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                return CommonFunctions.stringToDouble(cell.getStringCellValue());
            case FORMULA:
                Workbook wb = cell.getSheet().getWorkbook();
                CreationHelper createHelper = wb.getCreationHelper();
                FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
                CellValue cellValue = evaluator.evaluate(cell);
                return cellValue.getCellType() == CellType.NUMERIC ? cellValue.getNumberValue() : 0.0;
            default:
                return 0.0;
        }
    }

    public void uploadAddReplaceFeesFromId() {
        itemFees = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                itemFees = replaceFeesFromExcelForCollectingCentreSpecificFees(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        itemFeeManager.updateFeesForListFees();
    }

    public void uploadCollectingCentreItemsAndFees() {
        pollActive = true;
        items = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                items = readCollectingCentreItemsAndFeesFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pollActive = false;
    }

    public void uploadItemsAndFees() {
        pollActive = true;
        items = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                items = readItemsAndFeesFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("items = " + items.size());
        pollActive = false;
    }

    public void uploadItemsAndFeesWithForeignerFees() {
        pollActive = true;
        items = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                items = readItemsAndFeesFromExcelWithForeginerFees(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("items = " + items.size());
        pollActive = false;
    }

    private List<Item> readItemsAndFeesFromExcelWithForeginerFees(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemsToSave = new ArrayList<>();
        masterItemsToSave = new ArrayList<>();
        itemFeesToSave = new ArrayList<>();
        categoriesSaved = new ArrayList<>();
        institutionsSaved = new ArrayList<>();
        departmentsSaved = new ArrayList<>();
        itemsSkipped = new ArrayList<>();

        Item item;
        Institution runningIns = null;
        Department runningDept = null;
        Category runningCategory = null;

        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Category category;
            Institution institution;
            Department department;
            InwardChargeType iwct = null;
            Sample smpl = null;
            InvestigationTube tube = null;

            String name = null;
            String comments = null;
            String printingName = null;
            String fullName = null;
            String code = null;
            String categoryName = null;
            String institutionName = null;
            String departmentName = null;
            String inwardName = null;
            String itemType = "Service";
            String specimen = null;
            String container = null;

            Double hospitalFee = 0.0;
            Double hospitalFeeForForeginer = 0.0;

            Cell insCell = row.getCell(5);
            if (insCell != null && insCell.getCellType() == CellType.STRING) {
                institutionName = insCell.getStringCellValue();
            }
            if (institutionName == null || institutionName.trim().equals("")) {
                institutionName = "Other";
            }
            if (runningIns == null) {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                institutionsSaved.add(institution);
                runningIns = institution;
            } else if (runningIns.getName().equals(institutionName)) {
                institution = runningIns;
            } else {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                institutionsSaved.add(institution);
                runningIns = institution;
            }
            Cell deptCell = row.getCell(6);
            if (deptCell != null && deptCell.getCellType() == CellType.STRING) {
                departmentName = deptCell.getStringCellValue();
            }
            if (departmentName == null || departmentName.trim().equals("")) {
                departmentName = institutionName;
            }
            if (runningDept == null) {
                department = departmentController.findAndSaveDepartmentByName(departmentName);
                runningDept = department;
                departmentsSaved.add(department);
            } else if (runningDept.getName().equals(departmentName)) {
                department = runningDept;
            } else {
                department = departmentController.getDefaultDepatrment(institution);
                runningDept = department;
                departmentsSaved.add(department);
            }

            Cell nameCell = row.getCell(0);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();
                if (name == null || name.trim().equals("")) {
                    continue;
                }
            }

            comments = name;
            name = CommonFunctions.sanitizeStringForDatabase(name);

            item = itemController.findItemByName(name, code, department);
            if (item != null) {
                itemsSkipped.add(item);
                continue;
            }

//            Item masterItem = itemController.findMasterItemByName(name);
            Cell printingNameCell = row.getCell(1);
            if (printingNameCell != null && printingNameCell.getCellType() == CellType.STRING) {
                printingName = printingNameCell.getStringCellValue();

            }
            if (printingName == null || printingName.trim().equals("")) {
                printingName = name;
            }

            Cell fullNameCell = row.getCell(2);
            if (fullNameCell != null && fullNameCell.getCellType() == CellType.STRING) {
                fullName = fullNameCell.getStringCellValue();
            }
            if (fullName == null || fullName.trim().equals("")) {
                fullName = name;
            }

            Cell codeCell = row.getCell(3);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }
            if (code == null || code.trim().equals("")) {
                code = serviceController.generateShortCode(name);
            }

            Cell categoryCell = row.getCell(4);
            if (categoryCell != null && categoryCell.getCellType() == CellType.STRING) {
                categoryName = categoryCell.getStringCellValue();
            }
            if (categoryName == null || categoryName.trim().equals("")) {
                itemsSkipped.add(item);
                continue;
            }

            if (runningCategory == null) {
                category = categoryController.findCategoryByName(categoryName);
                if (category == null) {
                    category = new Category();
                    category.setName(categoryName);
                    categoryFacade.create(category);
                    categoriesSaved.add(category);
                }
                runningCategory = category;
            } else if (runningCategory.getName() == null) {
                category = runningCategory;
            } else if (runningCategory.getName().equals(categoryName)) {
                category = runningCategory;
            } else {
                category = categoryController.findCategoryByName(categoryName);
                if (category == null) {
                    category = new Category();
                    category.setName(categoryName);
                    categoryFacade.create(category);
                    categoriesSaved.add(category);
                }
                runningCategory = category;
            }

            Cell inwardCcCell = row.getCell(7);
            if (inwardCcCell != null && inwardCcCell.getCellType() == CellType.STRING) {
                inwardName = inwardCcCell.getStringCellValue();
            }
            if (inwardName != null && !inwardName.trim().equals("")) {
                iwct = enumController.getInaChargeType(inwardName);
            }
            if (iwct == null) {
                iwct = InwardChargeType.OtherCharges;
            }

            Cell specimenCell = row.getCell(9);
            if (specimenCell != null && specimenCell.getCellType() == CellType.STRING) {
                specimen = specimenCell.getStringCellValue();
            }
            if (specimen != null && !specimen.trim().equals("")) {
                smpl = sampleController.getSpecimen(specimen);
            }
            if (smpl == null) {
                smpl = sampleController.findAndCreateSampleByName("Blood");
            }

            Cell tubeCell = row.getCell(10);
            if (tubeCell != null && tubeCell.getCellType() == CellType.STRING) {
                container = tubeCell.getStringCellValue();
            }
            if (container != null && !container.trim().equals("")) {
                tube = investigationTubeController.getTube(container);
            }
            if (tube == null) {
                tube = investigationTubeController.findAndCreateInvestigationTubeByName("Plain Tube");
            }

            Cell itemTypeCell = row.getCell(8);
            if (itemTypeCell != null && itemTypeCell.getCellType() == CellType.STRING) {
                itemType = itemTypeCell.getStringCellValue();
            }
            if (itemType == null || itemType.trim().equals("")) {
                itemType = "Investigation";
            }
            if (itemType.equals("Service")) {
                Service service = new Service();
                service.setName(name);
                service.setPrintName(printingName);
                service.setFullName(fullName);
                service.setCode(code);
                service.setCategory(category);
//                service.setMasterItemReference(masterItem);
                service.setInstitution(institution);
                service.setDepartment(department);
                service.setInwardChargeType(iwct);
                service.setCreater(sessionController.getLoggedUser());
                service.setCreatedAt(new Date());
                item = service;
            } else if (itemType.equals("Investigation")) {
                Investigation ix = new Investigation();
                ix.setName(name);
                ix.setPrintName(printingName);
                ix.setFullName(fullName);
                ix.setCode(code);
                ix.setCategory(category);
                ix.setInstitution(institution);
                ix.setDepartment(department);
                ix.setInwardChargeType(iwct);
                ix.setSample(smpl);
                ix.setInvestigationTube(tube);
//                ix.setMasterItemReference(masterItem);
                ix.setCreater(sessionController.getLoggedUser());
                ix.setCreatedAt(new Date());
                item = ix;
            }

            if (item == null) {
                itemsSkipped.add(item);
                continue;
            }
            System.out.println("---------------------------------------------");
            System.out.println("item = " + item.getName());

            Cell hospitalFeeTypeCell = row.getCell(11);
            if (hospitalFeeTypeCell != null) {
                if (hospitalFeeTypeCell.getCellType() == CellType.NUMERIC) {
                    // If it's a numeric value
                    hospitalFee = hospitalFeeTypeCell.getNumericCellValue();
                } else if (hospitalFeeTypeCell.getCellType() == CellType.FORMULA) {
                    // If it's a formula, evaluate it
                    Workbook wb = hospitalFeeTypeCell.getSheet().getWorkbook();
                    CreationHelper createHelper = wb.getCreationHelper();
                    FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(hospitalFeeTypeCell);

                    // Check the type of the evaluated value
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        hospitalFee = cellValue.getNumberValue();
                    } else {
                        // Handle other types if needed
                    }
                } else if (hospitalFeeTypeCell.getCellType() == CellType.STRING) {
                    // If it's a numeric value
                    String strhospitalFee = hospitalFeeTypeCell.getStringCellValue();
                    hospitalFee = CommonFunctions.stringToDouble(strhospitalFee);
                }

            }

            Cell hospitalFeeForForeignerCell = row.getCell(12);
            if (hospitalFeeForForeignerCell != null) {
                if (hospitalFeeForForeignerCell.getCellType() == CellType.NUMERIC) {
                    // If it's a numeric value
                    hospitalFeeForForeginer = hospitalFeeForForeignerCell.getNumericCellValue();
                } else if (hospitalFeeForForeignerCell.getCellType() == CellType.FORMULA) {
                    // If it's a formula, evaluate it
                    Workbook wb = hospitalFeeForForeignerCell.getSheet().getWorkbook();
                    CreationHelper createHelper = wb.getCreationHelper();
                    FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(hospitalFeeForForeignerCell);

                    // Check the type of the evaluated value
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        hospitalFeeForForeginer = cellValue.getNumberValue();
                    } else {
                        // Handle other types if needed
                    }
                }
                if (hospitalFeeForForeignerCell.getCellType() == CellType.STRING) {
                    // If it's a numeric value
                    String strcollectingCentreFee = hospitalFeeForForeignerCell.getStringCellValue();
                    hospitalFeeForForeginer = CommonFunctions.stringToDouble(strcollectingCentreFee);
                }

                ItemFee itf = new ItemFee();
                itf.setName("Hospital Fee");
                itf.setItem(item);
                itf.setInstitution(institution);
                itf.setDepartment(department);
                itf.setFeeType(FeeType.OwnInstitution);
                itf.setFee(hospitalFee);
                itf.setFfee(hospitalFee);
                itf.setCreatedAt(new Date());

                if (hospitalFeeForForeginer > 0.0) {
                    itf.setFfee(hospitalFeeForForeginer);
                } else {
                    hospitalFeeForForeginer = hospitalFee;
                    itf.setFfee(hospitalFeeForForeginer);
                }

                itf.setCreater(sessionController.getLoggedUser());
//                itemFeeFacade.create(itf);
                itemFeesToSave.add(itf);

            }

            item.setTotal(hospitalFee);
            item.setTotalForForeigner(hospitalFee + hospitalFeeForForeginer);
            item.setDblValue(hospitalFee);

            System.out.println("Total Fee = " + item.getTotal());
            System.out.println("Total Foreigner Fee = " + item.getTotalForForeigner());
            itemsToSave.add(item);
        }

        System.out.println("---------------------------------------------");

//        itemFacade.batchCreate(masterItemsToSave, 5000);
        itemFacade.batchCreate(itemsToSave, 5000);
        itemFeeFacade.batchCreate(itemFeesToSave, 10000);

        return itemsToSave;
    }

    private List<Item> readItemsAndFeesFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemsToSave = new ArrayList<>();
        masterItemsToSave = new ArrayList<>();
        itemFeesToSave = new ArrayList<>();
        categoriesSaved = new ArrayList<>();
        institutionsSaved = new ArrayList<>();
        departmentsSaved = new ArrayList<>();
        itemsSkipped = new ArrayList<>();

        Item item;
        Institution runningIns = null;
        Department runningDept = null;
        Category runningCategory = null;

        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Category category;
            Institution institution;
            Department department;
            InwardChargeType iwct = null;
            Sample smpl = null;
            InvestigationTube tube = null;

            String name = null;
            String comments = null;
            String printingName = null;
            String fullName = null;
            String code = null;
            String categoryName = null;
            String institutionName = null;
            String departmentName = null;
            String inwardName = null;
            String itemType = "Service";
            String specimen = null;
            String container = null;

            Double hospitalFee = 0.0;
            Double collectingCentreFee = 0.0;
            Double chemicalFee = 0.0;
            Double additionalFee = 0.0;

            Cell insCell = row.getCell(5);
            if (insCell != null && insCell.getCellType() == CellType.STRING) {
                institutionName = insCell.getStringCellValue();
            }
            if (institutionName == null || institutionName.trim().equals("")) {
                institutionName = "Other";
            }
            if (runningIns == null) {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                institutionsSaved.add(institution);
                runningIns = institution;
            } else if (runningIns.getName().equals(institutionName)) {
                institution = runningIns;
            } else {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                institutionsSaved.add(institution);
                runningIns = institution;
            }
            Cell deptCell = row.getCell(6);
            if (deptCell != null && deptCell.getCellType() == CellType.STRING) {
                departmentName = deptCell.getStringCellValue();
            }
            if (departmentName == null || departmentName.trim().equals("")) {
                departmentName = institutionName;
            }
            if (runningDept == null) {
                department = departmentController.findAndSaveDepartmentByName(departmentName);
                runningDept = department;
                departmentsSaved.add(department);
            } else if (runningDept.getName().equals(departmentName)) {
                department = runningDept;
            } else {
                department = departmentController.getDefaultDepatrment(institution);
                runningDept = department;
                departmentsSaved.add(department);
            }

            Cell nameCell = row.getCell(0);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();
                if (name == null || name.trim().equals("")) {
                    continue;
                }
            }

            comments = name;
            name = CommonFunctions.sanitizeStringForDatabase(name);

            item = itemController.findItemByName(name, code, department);
            if (item != null) {
                itemsSkipped.add(item);
                continue;
            }

//            Item masterItem = itemController.findMasterItemByName(name);
            Cell printingNameCell = row.getCell(1);
            if (printingNameCell != null && printingNameCell.getCellType() == CellType.STRING) {
                printingName = printingNameCell.getStringCellValue();

            }
            if (printingName == null || printingName.trim().equals("")) {
                printingName = name;
            }

            Cell fullNameCell = row.getCell(2);
            if (fullNameCell != null && fullNameCell.getCellType() == CellType.STRING) {
                fullName = fullNameCell.getStringCellValue();
            }
            if (fullName == null || fullName.trim().equals("")) {
                fullName = name;
            }

            Cell codeCell = row.getCell(3);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }
            if (code == null || code.trim().equals("")) {
                code = serviceController.generateShortCode(name);
            }

            Cell categoryCell = row.getCell(4);
            if (categoryCell != null && categoryCell.getCellType() == CellType.STRING) {
                categoryName = categoryCell.getStringCellValue();
            }
            if (categoryName == null || categoryName.trim().equals("")) {
                itemsSkipped.add(item);
                continue;
            }

            if (runningCategory == null) {
                category = categoryController.findCategoryByName(categoryName);
                if (category == null) {
                    category = new Category();
                    category.setName(categoryName);
                    categoryFacade.create(category);
                    categoriesSaved.add(category);
                }
                runningCategory = category;
            } else if (runningCategory.getName() == null) {
                category = runningCategory;
            } else if (runningCategory.getName().equals(categoryName)) {
                category = runningCategory;
            } else {
                category = categoryController.findCategoryByName(categoryName);
                if (category == null) {
                    category = new Category();
                    category.setName(categoryName);
                    categoryFacade.create(category);
                    categoriesSaved.add(category);
                }
                runningCategory = category;
            }

            Cell inwardCcCell = row.getCell(7);
            if (inwardCcCell != null && inwardCcCell.getCellType() == CellType.STRING) {
                inwardName = inwardCcCell.getStringCellValue();
            }
            if (inwardName != null && !inwardName.trim().equals("")) {
                iwct = enumController.getInaChargeType(inwardName);
            }
            if (iwct == null) {
                iwct = InwardChargeType.OtherCharges;
            }

            Cell specimenCell = row.getCell(9);
            if (specimenCell != null && specimenCell.getCellType() == CellType.STRING) {
                specimen = specimenCell.getStringCellValue();
            }
            if (specimen != null && !specimen.trim().equals("")) {
                smpl = sampleController.getSpecimen(specimen);
            }
            if (smpl == null) {
                smpl = sampleController.findAndCreateSampleByName("Blood");
            }

            Cell tubeCell = row.getCell(10);
            if (tubeCell != null && tubeCell.getCellType() == CellType.STRING) {
                container = tubeCell.getStringCellValue();
            }
            if (container != null && !container.trim().equals("")) {
                tube = investigationTubeController.getTube(container);
            }
            if (tube == null) {
                tube = investigationTubeController.findAndCreateInvestigationTubeByName("Plain Tube");
            }

            Cell itemTypeCell = row.getCell(8);
            if (itemTypeCell != null && itemTypeCell.getCellType() == CellType.STRING) {
                itemType = itemTypeCell.getStringCellValue();
            }
            if (itemType == null || itemType.trim().equals("")) {
                itemType = "Investigation";
            }
            if (itemType.equals("Service")) {
                Service service = new Service();
                service.setName(name);
                service.setPrintName(printingName);
                service.setFullName(fullName);
                service.setCode(code);
                service.setCategory(category);
//                service.setMasterItemReference(masterItem);
                service.setInstitution(institution);
                service.setDepartment(department);
                service.setInwardChargeType(iwct);
                service.setCreater(sessionController.getLoggedUser());
                service.setCreatedAt(new Date());
                item = service;
            } else if (itemType.equals("Investigation")) {
                Investigation ix = new Investigation();
                ix.setName(name);
                ix.setPrintName(printingName);
                ix.setFullName(fullName);
                ix.setCode(code);
                ix.setCategory(category);
                ix.setInstitution(institution);
                ix.setDepartment(department);
                ix.setInwardChargeType(iwct);
                ix.setSample(smpl);
                ix.setInvestigationTube(tube);
//                ix.setMasterItemReference(masterItem);
                ix.setCreater(sessionController.getLoggedUser());
                ix.setCreatedAt(new Date());
                item = ix;
            }

            if (item == null) {
                itemsSkipped.add(item);
                continue;
            }
            System.out.println("---------------------------------------------");
            System.out.println("item = " + item.getName());

            Cell hospitalFeeTypeCell = row.getCell(11);
            if (hospitalFeeTypeCell != null) {
                if (hospitalFeeTypeCell.getCellType() == CellType.NUMERIC) {
                    // If it's a numeric value
                    hospitalFee = hospitalFeeTypeCell.getNumericCellValue();
                } else if (hospitalFeeTypeCell.getCellType() == CellType.FORMULA) {
                    // If it's a formula, evaluate it
                    Workbook wb = hospitalFeeTypeCell.getSheet().getWorkbook();
                    CreationHelper createHelper = wb.getCreationHelper();
                    FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(hospitalFeeTypeCell);

                    // Check the type of the evaluated value
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        hospitalFee = cellValue.getNumberValue();
                    } else {
                        // Handle other types if needed
                    }
                } else if (hospitalFeeTypeCell.getCellType() == CellType.STRING) {
                    // If it's a numeric value
                    String strhospitalFee = hospitalFeeTypeCell.getStringCellValue();
                    hospitalFee = CommonFunctions.stringToDouble(strhospitalFee);
                }

                // Rest of your code remains the same
                ItemFee itf = new ItemFee();
                itf.setName("Hospital Fee");
                itf.setItem(item);
                itf.setInstitution(institution);
                itf.setDepartment(department);
                itf.setFeeType(FeeType.OwnInstitution);
                itf.setFee(hospitalFee);
                itf.setFfee(hospitalFee);
                itf.setCreatedAt(new Date());
                itf.setCreater(sessionController.getLoggedUser());
//                itemFeeFacade.create(itf);
                itemFeesToSave.add(itf);
            }

            Cell collectingCenterFeeTypeCell = row.getCell(12);
            if (collectingCenterFeeTypeCell != null) {
                if (collectingCenterFeeTypeCell.getCellType() == CellType.NUMERIC) {
                    // If it's a numeric value
                    collectingCentreFee = collectingCenterFeeTypeCell.getNumericCellValue();
                } else if (collectingCenterFeeTypeCell.getCellType() == CellType.FORMULA) {
                    // If it's a formula, evaluate it
                    Workbook wb = collectingCenterFeeTypeCell.getSheet().getWorkbook();
                    CreationHelper createHelper = wb.getCreationHelper();
                    FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(collectingCenterFeeTypeCell);

                    // Check the type of the evaluated value
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        collectingCentreFee = cellValue.getNumberValue();
                    } else {
                        // Handle other types if needed
                    }
                }
                if (collectingCenterFeeTypeCell.getCellType() == CellType.STRING) {
                    // If it's a numeric value
                    String strcollectingCentreFee = collectingCenterFeeTypeCell.getStringCellValue();
                    collectingCentreFee = CommonFunctions.stringToDouble(strcollectingCentreFee);
                }

                // Rest of your code remains the same
                if (collectingCentreFee > 0.0) {
                    ItemFee itf = new ItemFee();
                    itf.setName("CC Fee");
                    itf.setItem(item);
                    itf.setInstitution(institution);
                    itf.setDepartment(department);
                    itf.setFeeType(FeeType.CollectingCentre);
                    itf.setFee(collectingCentreFee);
                    itf.setFfee(collectingCentreFee);
                    itf.setCreatedAt(new Date());
                    itf.setCreater(sessionController.getLoggedUser());
                    itemFeesToSave.add(itf);
                }

            }

            //-----------------------------------------------------------------------------------------
            Cell chemicalFeeTypeCell = row.getCell(13);
            if (chemicalFeeTypeCell != null) {
                if (chemicalFeeTypeCell.getCellType() == CellType.NUMERIC) {
                    // If it's a numeric value
                    chemicalFee = chemicalFeeTypeCell.getNumericCellValue();
                } else if (chemicalFeeTypeCell.getCellType() == CellType.FORMULA) {
                    // If it's a formula, evaluate it
                    Workbook wb = chemicalFeeTypeCell.getSheet().getWorkbook();
                    CreationHelper createHelper = wb.getCreationHelper();
                    FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(chemicalFeeTypeCell);

                    // Check the type of the evaluated value
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        chemicalFee = cellValue.getNumberValue();
                    } else {
                        // Handle other types if needed
                    }
                }
                if (chemicalFeeTypeCell.getCellType() == CellType.STRING) {
                    // If it's a numeric value
                    String strChemicalFee = chemicalFeeTypeCell.getStringCellValue();
                    chemicalFee = CommonFunctions.stringToDouble(strChemicalFee);
                }

                // Rest of your code remains the same
                if (chemicalFee > 0.0) {
                    ItemFee itf = new ItemFee();
                    itf.setName("Regent Fee");
                    itf.setItem(item);
                    itf.setInstitution(institution);
                    itf.setDepartment(department);
                    itf.setFeeType(FeeType.Chemical);
                    itf.setFee(chemicalFee);
                    itf.setFfee(chemicalFee);
                    itf.setCreatedAt(new Date());
                    itf.setCreater(sessionController.getLoggedUser());
                    itemFeesToSave.add(itf);
                }

            }

            //-----------------------------------------------------------------------------------------
            Cell additionalFeeTypeCell = row.getCell(14);
            if (additionalFeeTypeCell != null) {
                if (additionalFeeTypeCell.getCellType() == CellType.NUMERIC) {
                    // If it's a numeric value
                    additionalFee = additionalFeeTypeCell.getNumericCellValue();
                } else if (additionalFeeTypeCell.getCellType() == CellType.FORMULA) {
                    // If it's a formula, evaluate it
                    Workbook wb = additionalFeeTypeCell.getSheet().getWorkbook();
                    CreationHelper createHelper = wb.getCreationHelper();
                    FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(additionalFeeTypeCell);

                    // Check the type of the evaluated value
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        additionalFee = cellValue.getNumberValue();
                    } else {
                        // Handle other types if needed
                    }
                }
                if (additionalFeeTypeCell.getCellType() == CellType.STRING) {
                    // If it's a numeric value
                    String strAdditionalFee = chemicalFeeTypeCell.getStringCellValue();
                    additionalFee = CommonFunctions.stringToDouble(strAdditionalFee);
                }

                // Rest of your code remains the same
                if (additionalFee > 0.0) {
                    ItemFee itf = new ItemFee();
                    itf.setName("Other Fee");
                    itf.setItem(item);
                    itf.setInstitution(institution);
                    itf.setDepartment(department);
                    itf.setFeeType(FeeType.Additional);
                    itf.setFee(additionalFee);
                    itf.setFfee(additionalFee);
                    itf.setCreatedAt(new Date());
                    itf.setCreater(sessionController.getLoggedUser());
                    itemFeesToSave.add(itf);
                }

            }

            item.setTotal(hospitalFee + collectingCentreFee + chemicalFee + additionalFee);
            item.setTotalForForeigner(hospitalFee + collectingCentreFee + chemicalFee + additionalFee);
            item.setDblValue(hospitalFee + collectingCentreFee + chemicalFee + additionalFee);

            System.out.println("Total Fee = " + item.getTotal());
            System.out.println("Total Foreigner Fee = " + item.getTotalForForeigner());
            itemsToSave.add(item);
        }

        System.out.println("---------------------------------------------");

        itemFacade.batchCreate(masterItemsToSave, 5000);
        itemFacade.batchCreate(itemsToSave, 5000);
        itemFeeFacade.batchCreate(itemFeesToSave, 10000);

        return itemsToSave;
    }

    public void uploadCollectingCentreSpecialFeeUpload() {
        pollActive = true;
        items = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                itemFees = readCollectingCentreSpecialFeeUploadFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pollActive = false;
    }

    public void uploadOutSourceDepartmentItemsAndFees() {
        pollActive = true;
        items = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                items = readOutSourceDepartmentItemsAndFeesFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pollActive = false;
        JsfUtil.addSuccessMessage("Uploaded Successfully");
    }

    public void uploadConsultants() {
        pollActive = true;
        items = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                consultantsToSave = readConsultantsFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pollActive = false;
    }

    public void uploadDoctors() {
        pollActive = true;
        items = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                doctorsTosave = readDoctorsFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pollActive = false;
    }

    public void uploadStaff() {
        pollActive = true;
        items = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                staffToSave = readStaffFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pollActive = false;
    }

    public void uploadMembers() {
        pollActive = true;
        items = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                savedPatients = readMembersFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pollActive = false;
    }

    public void uploadFeeListItemFees() {
        if (selectedFeeList == null) {
            JsfUtil.addErrorMessage("Please select a Fee List before uploading.");
            return;
        }
        
        itemFees = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                itemFees = readFeeListItemFeesFromExcel(inputStream, selectedFeeList);
                // Success/error messages are handled in readFeeListItemFeesFromExcel method
            } catch (IOException e) {
                JsfUtil.addErrorMessage("Error reading Excel file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            JsfUtil.addErrorMessage("Please select a file to upload.");
        }
    }

    public List<Category> readFeeListTypesFromExcel(InputStream inputStream) throws IOException {
        List<Category> feeListTypes = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        Category category = null;

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String feeListName = null;
            String description = null;

            Cell feeListNameCell = row.getCell(0);
            if (feeListNameCell != null && feeListNameCell.getCellType() == CellType.STRING) {
                feeListName = feeListNameCell.getStringCellValue();
            }

            if (feeListName != null || !feeListName.trim().equals("")) {
                category = categoryController.findCategoryByName(feeListName);
            }

            if (category == null) {
                category = new Category();
                category.setName(feeListName);
                category.setSymanticType(SymanticHyrachi.Fee_List_Type);
                category.setCreatedAt(new Date());
                category.setCreater(sessionController.getCurrent());
                categoryController.save(category);
                feeListTypes.add(category);
            }

        }
        JsfUtil.addSuccessMessage("FeeList Types Uploaded");
        return feeListTypes;
    }

    private List<ItemFee> readFeeListItemFeesFromExcel(InputStream inputStream, Category selectedFeeList) throws IOException {
        List<ItemFee> itemFees = new ArrayList<>();
        List<ItemFee> validatedItemFees = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        
        int totalRows = sheet.getLastRowNum();
        List<String> validationErrors = new ArrayList<>();
        StringBuilder errorDetailsBuilder = new StringBuilder();

        // Clear previous errors
        this.uploadErrors = null;
        this.uploadErrorDetails = null;

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        // PHASE 1: VALIDATION ONLY - No database saves
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String itemCode = null;
            String itemName = null;
            String discountAllowed = null;

            boolean disAllowd = false;
            double fee = 0.0;
            double ffee = 0.0;

            Item item;
            int rowNumber = row.getRowNum() + 1;

            // Extract Item Code
            Cell itemCodeCell = row.getCell(0);
            if (itemCodeCell != null) {
                if (itemCodeCell.getCellType() == CellType.STRING) {
                    itemCode = itemCodeCell.getStringCellValue();
                } else if (itemCodeCell.getCellType() == CellType.NUMERIC) {
                    itemCode = String.valueOf((long) itemCodeCell.getNumericCellValue());
                }
            }

            // Extract Item Name (for error reporting)
            Cell itemNameCell = row.getCell(1);
            if (itemNameCell != null && itemNameCell.getCellType() == CellType.STRING) {
                itemName = itemNameCell.getStringCellValue();
            }

            // Validate Item Code
            if (itemCode == null || itemCode.trim().isEmpty()) {
                validationErrors.add("Row " + rowNumber + ": Empty item code");
                continue;
            }

            // Validate Item Exists
            item = itemController.findItemByCode(itemCode);
            if (item == null) {
                String itemNameForError = (itemName != null && !itemName.trim().isEmpty()) ? itemName : "N/A";
                validationErrors.add("Row " + rowNumber + ": Item not found - Code: '" + itemCode + "', Name: '" + itemNameForError + "'");
                continue;
            }

            // Extract and Validate Fee Amount (Column C)
            Cell feeCell = row.getCell(2);
            if (feeCell != null) {
                if (feeCell.getCellType() == CellType.NUMERIC) {
                    fee = feeCell.getNumericCellValue();
                } else if (feeCell.getCellType() == CellType.STRING) {
                    String feeStr = feeCell.getStringCellValue().trim();
                    if (!feeStr.isEmpty()) {
                        try {
                            fee = Double.parseDouble(feeStr);
                        } catch (NumberFormatException e) {
                            validationErrors.add("Row " + rowNumber + ": Invalid fee format '" + feeStr + "' for item " + itemCode);
                            continue;
                        }
                    }
                } else if (feeCell.getCellType() == CellType.FORMULA) {
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(feeCell);
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        fee = cellValue.getNumberValue();
                    } else if (cellValue.getCellType() == CellType.STRING) {
                        String feeStr = cellValue.getStringValue().trim();
                        if (!feeStr.isEmpty()) {
                            try {
                                fee = Double.parseDouble(feeStr);
                            } catch (NumberFormatException e) {
                                validationErrors.add("Row " + rowNumber + ": Invalid fee formula result '" + feeStr + "' for item " + itemCode);
                                continue;
                            }
                        }
                    }
                }
            }

            // Extract and Validate Foreign Fee (Column D) 
            Cell ffeeCell = row.getCell(3);
            if (ffeeCell != null) {
                if (ffeeCell.getCellType() == CellType.NUMERIC) {
                    ffee = ffeeCell.getNumericCellValue();
                } else if (ffeeCell.getCellType() == CellType.STRING) {
                    String ffeeStr = ffeeCell.getStringCellValue().trim();
                    if (!ffeeStr.isEmpty()) {
                        try {
                            ffee = Double.parseDouble(ffeeStr);
                        } catch (NumberFormatException e) {
                            validationErrors.add("Row " + rowNumber + ": Invalid foreign fee format '" + ffeeStr + "' for item " + itemCode);
                            continue;
                        }
                    }
                } else if (ffeeCell.getCellType() == CellType.FORMULA) {
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(ffeeCell);
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        ffee = cellValue.getNumberValue();
                    } else if (cellValue.getCellType() == CellType.STRING) {
                        String ffeeStr = cellValue.getStringValue().trim();
                        if (!ffeeStr.isEmpty()) {
                            try {
                                ffee = Double.parseDouble(ffeeStr);
                            } catch (NumberFormatException e) {
                                validationErrors.add("Row " + rowNumber + ": Invalid foreign fee formula result '" + ffeeStr + "' for item " + itemCode);
                                continue;
                            }
                        }
                    }
                }
            }

            // Extract Discount Allowed (Column E)
            Cell discountAllowedCell = row.getCell(4);
            if (discountAllowedCell != null && discountAllowedCell.getCellType() == CellType.STRING) {
                discountAllowed = discountAllowedCell.getStringCellValue();
            }
            if (discountAllowed != null && !discountAllowed.trim().equals("") && 
                (discountAllowed.equalsIgnoreCase("yes") || discountAllowed.equalsIgnoreCase("true") || discountAllowed.equals("1"))) {
                disAllowd = true;
            }

            // If we reach here, validation passed - create ItemFee object but don't save yet
            ItemFee itemFee = new ItemFee();
            itemFee.setCreatedAt(new Date());
            itemFee.setName(selectedFeeList.getName());
            itemFee.setCreater(sessionController.getLoggedUser());
            itemFee.setForInstitution(null);
            itemFee.setForCategory(selectedFeeList);
            itemFee.setItem(item);
            itemFee.setFeeType(FeeType.OwnInstitution);
            itemFee.setInstitution(item.getInstitution());
            itemFee.setFee(fee);
            itemFee.setFfee(ffee);
            itemFee.setDiscountAllowed(disAllowd);
            validatedItemFees.add(itemFee);
        }

        // PHASE 2: HANDLE RESULTS
        if (!validationErrors.isEmpty()) {
            // Set errors for display
            this.uploadErrors = validationErrors;
            
            // Build detailed error message
            errorDetailsBuilder.append("=== UPLOAD VALIDATION FAILED ===\n");
            errorDetailsBuilder.append("Fee List: ").append(selectedFeeList.getName()).append("\n");
            errorDetailsBuilder.append("Total Rows: ").append(totalRows).append("\n");
            errorDetailsBuilder.append("Valid Items: ").append(validatedItemFees.size()).append("\n");
            errorDetailsBuilder.append("Errors Found: ").append(validationErrors.size()).append("\n\n");
            
            errorDetailsBuilder.append("=== ERROR DETAILS ===\n");
            for (int i = 0; i < validationErrors.size(); i++) {
                errorDetailsBuilder.append((i + 1)).append(". ").append(validationErrors.get(i)).append("\n");
            }
            
            errorDetailsBuilder.append("\n=== INSTRUCTIONS ===\n");
            errorDetailsBuilder.append("1. Fix the errors listed above in your Excel file\n");
            errorDetailsBuilder.append("2. Ensure all item codes exist in the system\n");
            errorDetailsBuilder.append("3. Check that fee amounts are valid numbers\n");
            errorDetailsBuilder.append("4. Re-upload the corrected file\n");
            
            this.uploadErrorDetails = errorDetailsBuilder.toString();
            
            JsfUtil.addErrorMessage(validationErrors.size() + " validation errors found. No data was saved. See error details below.");
            return new ArrayList<>(); // Return empty list - no data saved
        } else {
            // No errors - proceed with saving all validated items
            for (ItemFee itemFee : validatedItemFees) {
                itemFeeFacade.create(itemFee);
                itemFees.add(itemFee);
            }
            
            JsfUtil.addSuccessMessage("✓ SUCCESS: " + validatedItemFees.size() + " item fees uploaded to " + selectedFeeList.getName());
            return itemFees;
        }
    }

//    public void uploadFeeListItemFees() {
//        itemFees = new ArrayList<>();
//        if (file != null) {
//            try ( InputStream inputStream = file.getInputStream()) {
//                itemFees = readFeeListItemFeesFromExcel(inputStream);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    public List<Category> readFeeListTypesFromExcel(InputStream inputStream) throws IOException {
//        List<Category> feeListTypes = new ArrayList<>();
//        Workbook workbook = new XSSFWorkbook(inputStream);
//        Sheet sheet = workbook.getSheetAt(0);
//        Iterator<Row> rowIterator = sheet.rowIterator();
//
//        if (rowIterator.hasNext()) {
//            rowIterator.next();
//        }
//
//        Category category = null;
//
//        while (rowIterator.hasNext()) {
//            Row row = rowIterator.next();
//
//            String feeListName = null;
//            String description = null;
//
//            Cell feeListNameCell = row.getCell(0);
//            if (feeListNameCell != null && feeListNameCell.getCellType() == CellType.STRING) {
//                feeListName = feeListNameCell.getStringCellValue();
//            }
//
//            if (feeListName != null || !feeListName.trim().equals("")) {
//                category = categoryController.findCategoryByName(feeListName);
//            }
//
//            if (category == null) {
//                category = new Category();
//                category.setName(feeListName);
//                category.setSymanticType(SymanticHyrachi.Fee_List_Type);
//                category.setCreatedAt(new Date());
//                category.setCreater(sessionController.getCurrent());
//                categoryController.save(category);
//                feeListTypes.add(category);
//            }
//
//        }
//        JsfUtil.addSuccessMessage("FeeList Types Uploaded");
//        return feeListTypes;
//    }
//
//
//
//    private List<ItemFee> readFeeListItemFeesFromExcel(InputStream inputStream) throws IOException {
//        List<ItemFee> itemFees = new ArrayList<>();
//        Workbook workbook = new XSSFWorkbook(inputStream);
//        Sheet sheet = workbook.getSheetAt(0);
//        Iterator<Row> rowIterator = sheet.rowIterator();
//
//        // Assuming the first row contains headers, skip it
//        if (rowIterator.hasNext()) {
//            rowIterator.next();
//        }
//
//        while (rowIterator.hasNext()) {
//            Row row = rowIterator.next();
//            String itemCode = null;
//            String itemName = null;
//            String forCategoryName = null;
//            String institutionName = null;
//            String discountAllowed = null;
//            String ffeeValue = null;
//            String fffeeValue = null;
//
//            boolean disAllowd;
//            double fee = 0.0;
//            double ffee = 0.0;
//
//            Item item;
//            Category category;
//            Institution institution;
//
//            Cell itemCodeCell = row.getCell(0);
//            if (itemCodeCell != null && itemCodeCell.getCellType() == CellType.STRING) {
//                itemCode = itemCodeCell.getStringCellValue();
//            }
//
//            Cell itemNameCell = row.getCell(1);
//            if (itemNameCell != null && itemNameCell.getCellType() == CellType.STRING) {
//                itemName = itemNameCell.getStringCellValue();
//            }
//
//            Cell forCategoryCell = row.getCell(2);
//            if (forCategoryCell != null && forCategoryCell.getCellType() == CellType.STRING) {
//                forCategoryName = forCategoryCell.getStringCellValue();
//            }
//
//            Cell feeCell = row.getCell(3);
//            if (feeCell != null && feeCell.getCellType() == CellType.NUMERIC) {
//                fee = feeCell.getNumericCellValue();
//            }
//
//            Cell ffeeCell = row.getCell(4);
//            if (ffeeCell != null && ffeeCell.getCellType() == CellType.NUMERIC) {
//                ffee = ffeeCell.getNumericCellValue();
//            }
//
//            Cell discountAllowedCell = row.getCell(5);
//            if (discountAllowedCell != null && discountAllowedCell.getCellType() == CellType.STRING) {
//                discountAllowed = discountAllowedCell.getStringCellValue();
//            }
//            if (discountAllowed != null || !discountAllowed.trim().equals("")) {
//                disAllowd = true;
//            } else {
//                disAllowd = false;
//            }
//
//            if (itemName == null || itemCode == null) {
//                JsfUtil.addErrorMessage("Item Name and Item Code cannot be null.");
//                return itemFees;
//            }
//
//            if (forCategoryName == null || forCategoryName.trim().equals("")) {
//                JsfUtil.addErrorMessage("Fee List types cannot be null.");
//                return itemFees;
//            }
//
//            category = categoryController.findCategoryByName(forCategoryName);
//            if (category == null) {
//                JsfUtil.addErrorMessage("Fee List type Not found.");
//                return itemFees;
//            }
//
//            item = itemController.findItemByNameAndCode(itemName, itemCode);
//            if (item == null) {
//                JsfUtil.addErrorMessage("Item cannot be null.");
//                return itemFees;
//            }
//            ItemFee Itemfee = new ItemFee();
//            Itemfee.setCreatedAt(new Date());
//            Itemfee.setName(forCategoryName);
//            Itemfee.setCreater(sessionController.getLoggedUser());
//            Itemfee.setForInstitution(null);
//            Itemfee.setForCategory(category);
//            Itemfee.setItem(item);
//            Itemfee.setFeeType(FeeType.OwnInstitution);
//            Itemfee.setInstitution(item.getInstitution());
//            Itemfee.setFee(fee);
//            Itemfee.setFfee(ffee);
//            Itemfee.setDiscountAllowed(disAllowd);
//            itemFeeFacade.create(Itemfee);
//
//        }
//        JsfUtil.addSuccessMessage("Upload Success");
//        return itemFees;
//
//    }
    private List<Consultant> readConsultantsFromExcel(InputStream inputStream) throws IOException {
        List<Consultant> cons = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemsToSave = new ArrayList<>();
        masterItemsToSave = new ArrayList<>();
        itemFeesToSave = new ArrayList<>();
        categoriesSaved = new ArrayList<>();
        institutionsSaved = new ArrayList<>();
        departmentsSaved = new ArrayList<>();
        itemsSkipped = new ArrayList<>();

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            DoctorSpeciality speciality;
            Consultant consultant;
            Sex sex;
            Title title;

            String code = null;
            String name = null;
            String titleString = "";

            String registration = "";
            String description = "";
            String sexString = null;
            String mobileNumber = "";

            String specialityString = null;

            Cell codeCell = row.getCell(0);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }

            Cell titleCell = row.getCell(1);
            if (titleCell != null && titleCell.getCellType() == CellType.STRING) {
                titleString = titleCell.getStringCellValue();
            }

            Cell nameCell = row.getCell(2);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();

            }

            Cell registrationCell = row.getCell(3);
            if (registrationCell != null && registrationCell.getCellType() == CellType.STRING) {
                registration = registrationCell.getStringCellValue();
            }

            Cell descriptionCell = row.getCell(4);
            if (descriptionCell != null && descriptionCell.getCellType() == CellType.STRING) {
                description = descriptionCell.getStringCellValue();
            }

            Cell sexCell = row.getCell(5);
            if (sexCell != null) {
                sexString = sexCell.getStringCellValue();

            }

            Cell mobileCell = row.getCell(6);
            if (mobileCell != null && mobileCell.getCellType() == CellType.STRING) {
                mobileNumber = mobileCell.getStringCellValue();
            } else if (mobileCell != null && mobileCell.getCellType() == CellType.NUMERIC) {
                mobileNumber = "" + mobileCell.getNumericCellValue();
            }

            Cell specialityCell = row.getCell(7);
            if (specialityCell != null && specialityCell.getCellType() == CellType.STRING) {
                specialityString = specialityCell.getStringCellValue();
            }

            if (name == null || name.trim().equals("")) {
                continue;
            }

            if (specialityString == null || specialityString.trim().equals("")) {
                continue;
            }

            speciality = doctorSpecialityController.findDoctorSpeciality(specialityString, true);

            if (sexString != null && sexString.toLowerCase().contains("f")) {
                sex = Sex.Female;
            } else {
                sex = Sex.Male;
            }

            title = Title.getTitleEnum(titleString);

            consultant = consultantController.getConsultantByName(name);
            if (consultant == null) {
                consultant = new Consultant();
            }
            consultant.getPerson().setName(name);
            consultant.getPerson().setSex(sex);
            consultant.getPerson().setTitle(title);
            consultant.getPerson().setMobile(mobileNumber);
            consultant.setCode(code);
            consultant.setRegistration(registration);
            consultant.setDescription(description);
            consultant.setSpeciality(speciality);
            cons.add(consultant);

        }
        return cons;
    }

    private List<Doctor> readDoctorsFromExcel(InputStream inputStream) throws IOException {
        System.out.println("readDoctorsFromExcel");
        List<Doctor> docs = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemsToSave = new ArrayList<>();
        masterItemsToSave = new ArrayList<>();
        itemFeesToSave = new ArrayList<>();
        categoriesSaved = new ArrayList<>();
        institutionsSaved = new ArrayList<>();
        departmentsSaved = new ArrayList<>();
        itemsSkipped = new ArrayList<>();

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            DoctorSpeciality speciality;
            Doctor doctor;
            Sex sex;
            Title title;

            String code = null;
            String name = null;
            String titleString = "";

            String registration = "";
            String description = "";
            String sexString = null;
            String mobileNumber = "";

            String specialityString = null;

            Cell codeCell = row.getCell(0);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }

            Cell titleCell = row.getCell(1);
            if (titleCell != null && titleCell.getCellType() == CellType.STRING) {
                titleString = titleCell.getStringCellValue();
            }

            Cell nameCell = row.getCell(2);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();

            }

            Cell registrationCell = row.getCell(3);
            if (registrationCell != null && registrationCell.getCellType() == CellType.STRING) {
                registration = registrationCell.getStringCellValue();
            }

            Cell descriptionCell = row.getCell(4);
            if (descriptionCell != null && descriptionCell.getCellType() == CellType.STRING) {
                description = descriptionCell.getStringCellValue();
            }

            Cell sexCell = row.getCell(5);
            if (sexCell != null) {
                sexString = sexCell.getStringCellValue();

            }

            Cell mobileCell = row.getCell(6);
            if (mobileCell != null && mobileCell.getCellType() == CellType.STRING) {
                mobileNumber = mobileCell.getStringCellValue();
            } else if (mobileCell != null && mobileCell.getCellType() == CellType.NUMERIC) {
                mobileNumber = "" + mobileCell.getNumericCellValue();
            }

            Cell specialityCell = row.getCell(7);
            if (specialityCell != null && specialityCell.getCellType() == CellType.STRING) {
                specialityString = specialityCell.getStringCellValue();
            }

            System.out.println("name = " + name);
            if (name == null || name.trim().equals("")) {
                continue;
            }
            System.out.println("specialityString = " + specialityString);
            if (specialityString == null || specialityString.trim().equals("")) {
                continue;
            }

            speciality = doctorSpecialityController.findDoctorSpeciality(specialityString, true);

            System.out.println("sexString = " + sexString);
            if (sexString != null && sexString.toLowerCase().contains("f")) {
                sex = Sex.Female;
            } else {
                sex = Sex.Male;
            }

            title = Title.getTitleEnum(titleString);
            System.out.println("title = " + title);

            doctor = doctorController.getDoctorsByName(name);
            System.out.println("doctor = " + doctor);
            if (doctor == null) {
                doctor = new Doctor();
            }
            doctor.getPerson().setName(name);
            doctor.getPerson().setSex(sex);
            doctor.getPerson().setTitle(title);
            doctor.getPerson().setMobile(mobileNumber);
            doctor.setCode(code);
            doctor.setRegistration(registration);
            doctor.setDescription(description);
            doctor.setSpeciality(speciality);
            docs.add(doctor);

        }
        return docs;
    }

    public List<Staff> readStaffFromExcel(InputStream inputStream) throws IOException {
        List<Staff> stf = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        if (rowIterator.hasNext()) {
            rowIterator.next(); // Skip header row
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Staff staff;
            Title title;

            String code = null;
            String epfNo = null;
            String titleString = null;
            String name = null;
            String fullName = null;
            String nameWithInitials = null;
            String address = null;
            String sex = null;
            String nicNo = null;
            Date dob = null;
            Date retired = null;
            String departmentName = null;
            String branchName = null;
            String acNo = null;
            String bankName = null;

            Department department = null;
            Institution institution = null;
            Institution bank = null;
            Sex gender = null;

            Cell codeCell = row.getCell(0);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }

            Cell epfNoCell = row.getCell(1);
            if (epfNoCell != null) {
                if (epfNoCell.getCellType() == CellType.NUMERIC) {
                    epfNo = String.valueOf((int) epfNoCell.getNumericCellValue());
                } else if (epfNoCell.getCellType() == CellType.STRING) {
                    epfNo = epfNoCell.getStringCellValue();
                }
            }

            Cell titleCell = row.getCell(2);
            if (titleCell != null && titleCell.getCellType() == CellType.STRING) {
                titleString = titleCell.getStringCellValue();
            }

            Cell nameCell = row.getCell(3);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();
            }

            Cell fullNameCell = row.getCell(4);
            if (fullNameCell != null && fullNameCell.getCellType() == CellType.STRING) {
                fullName = fullNameCell.getStringCellValue();
            }

            Cell nameWithInitialsCell = row.getCell(5);
            if (nameWithInitialsCell != null && nameWithInitialsCell.getCellType() == CellType.STRING) {
                nameWithInitials = nameWithInitialsCell.getStringCellValue();
            }

            Cell addressCell = row.getCell(6);
            if (addressCell != null && addressCell.getCellType() == CellType.STRING) {
                address = addressCell.getStringCellValue();
            }

            Cell sexCell = row.getCell(7);
            if (sexCell != null && sexCell.getCellType() == CellType.STRING) {
                sex = sexCell.getStringCellValue();
                try {
                    gender = Sex.valueOf(sex);
                } catch (IllegalArgumentException e) {
                    gender = null;
                }
            }

            Cell nicNoCell = row.getCell(8);
            if (nicNoCell != null && nicNoCell.getCellType() == CellType.STRING) {
                nicNo = nicNoCell.getStringCellValue();
            }

            Cell dobCell = row.getCell(9);
            if (dobCell != null) {
                if (dobCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dobCell)) {
                    dob = dobCell.getDateCellValue();
                } else if (dobCell.getCellType() == CellType.STRING) {
                    String dobString = dobCell.getStringCellValue();
                    try {
                        String uploadDateFormat = configOptionApplicationController.getShortTextValueByKey("Date format for Uploads", "yyyy-MM-dd");
                        dob = new SimpleDateFormat(uploadDateFormat).parse(dobString);
                    } catch (ParseException ignored) {
                    }
                }
            }

            Cell retiredCell = row.getCell(10);
            if (retiredCell != null) {
                if (retiredCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(retiredCell)) {
                    retired = retiredCell.getDateCellValue();
                } else if (retiredCell.getCellType() == CellType.STRING) {
                    String retiredString = retiredCell.getStringCellValue();
                    try {
                        retired = new SimpleDateFormat("yyyy-MM-dd").parse(retiredString);
                    } catch (ParseException ignored) {
                    }
                }
            }

            Cell departmentCell = row.getCell(11);
            if (departmentCell != null && departmentCell.getCellType() == CellType.STRING) {
                departmentName = departmentCell.getStringCellValue();
            }
            if (departmentName != null) {
                department = departmentController.findAndSaveDepartmentByName(departmentName);
            }

            Cell branchCell = row.getCell(12);
            if (branchCell != null && branchCell.getCellType() == CellType.STRING) {
                branchName = branchCell.getStringCellValue();
            }
            if (branchName != null) {
                institution = institutionController.findAndSaveInstitutionByName(branchName);
            }

            Cell acNoCell = row.getCell(13);
            if (acNoCell != null && acNoCell.getCellType() == CellType.STRING) {
                acNo = acNoCell.getStringCellValue();
            }

            Cell bankCell = row.getCell(14);
            if (bankCell != null && bankCell.getCellType() == CellType.STRING) {
                bankName = bankCell.getStringCellValue();
            }
            if (bankName != null) {
                bank = institutionController.findAndSaveInstitutionByName(bankName);
                if (bank != null) {
                    bank.setInstitutionType(InstitutionType.Bank);
                    institutionController.save(bank);
                }
            }

            if (name == null || name.trim().isEmpty()) {
                continue;
            }
            if (nameWithInitials == null || nameWithInitials.trim().isEmpty()) {
                continue;
            }

            title = Title.getTitleEnum(titleString);

            staff = staffController.getstaffByName(name);
            if (staff == null) {
                staff = new Staff();
            }

            staff.setCode(code);
            staff.setEpfNo(epfNo);
            staff.getPerson().setName(name);
            staff.getPerson().setTitle(title);
            staff.getPerson().setFullName(fullName);
            staff.getPerson().setNameWithInitials(nameWithInitials);
            staff.getPerson().setAddress(address);
            staff.getPerson().setSex(gender);
            staff.getPerson().setNic(nicNo);
            staff.getPerson().setDob(dob);
            staff.setDateRetired(retired);
            staff.setDepartment(department);
            staff.setInstitution(institution);
            staff.setAccountNo(acNo);
            staff.setBankBranch(bank);

            stf.add(staff);
        }

        return stf;
    }

    public List<Patient> readMembersFromExcel(InputStream inputStream) throws IOException {
        List<Patient> patients = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        if (rowIterator.hasNext()) {
            rowIterator.next();  // Skip the header row
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String name = row.getCell(1).getStringCellValue();
            String titleString = row.getCell(2).getStringCellValue();
            String sexString = row.getCell(3).getStringCellValue();
            Long membershipNumberLong = getNumericCellAsLong(row.getCell(4));
            Long phoneNumberLong = getNumericCellAsLong(row.getCell(5));
            String nicNumber = getStringOrNumericCellValue(row.getCell(6));
            String address = row.getCell(7).getStringCellValue();
            String membershipName = row.getCell(8).getStringCellValue();
            String relationName = row.getCell(9).getStringCellValue();
//            Integer ageInt = row.getCell(10) != null ? (int) row.getCell(10).getNumericCellValue() : null;
            Date dateOfBirth = getDateFromCell(row.getCell(10));

            String phoneNumberString = phoneNumberLong != null ? phoneNumberLong.toString() : null;

            MembershipScheme ms = membershipSchemeController.fetchMembershipByName(membershipName);
            Family family = patientController.fetchFamilyFromMembershipNumber(membershipNumberLong, ms, phoneNumberString);
            Relation relation = relationController.fetchRelationByName(relationName);
            Title title = Title.getTitleEnum(titleString);
            Sex sex = Sex.getByLabelOrShortLabel(sexString);
//            Date dateOfBirth = CommonFunctions.fetchDateOfBirthFromAge(ageInt);

            Patient pt = new Patient();
            pt.getPerson().setName(name);
            pt.getPerson().setAddress(address);
            pt.getPerson().setPhone(phoneNumberString);
            pt.getPerson().setMobile(phoneNumberString);
            pt.getPerson().setTitle(title);
            pt.getPerson().setSex(sex);
            pt.getPerson().setDob(dateOfBirth);
            pt.getPerson().setMembershipScheme(ms);
            pt.getPerson().setNic(nicNumber);

            pt.setPatientMobileNumber(phoneNumberLong);
            pt.setPatientPhoneNumber(phoneNumberLong);
            patientFacade.create(pt);

            FamilyMember fm = new FamilyMember();
            fm.setFamily(family);
            fm.setPatient(pt);
            fm.setRelationToChh(relation);
            familyMemberFacade.create(fm);

            family.getFamilyMembers().add(fm);
            family.setChiefHouseHolder(pt);
            familyFacade.edit(family);

            patients.add(pt);
        }

        workbook.close();
        return patients;
    }

    private Date getDateFromCell(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.STRING) {
                String dateString = cell.getStringCellValue().trim();
                return parseDateString(dateString);
            }

            if (cell.getCellType() == CellType.NUMERIC) {
                DataFormatter formatter = new DataFormatter();
                String formattedValue = formatter.formatCellValue(cell);

                if (formattedValue.matches("\\d{1,2}/\\d{1,2}/\\d{2,4}")) {
                    return parseDateString(formattedValue);
                }

                return null;
            }
        } catch (Exception e) {
            Logger.getLogger(DataUploadController.class.getName()).log(
                    Level.SEVERE, null, "Error parsing date from cell: " + e.getMessage());
        }

        return null;
    }

    private Date parseDateString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        dateString = dateString.trim();

        String[] dateFormats = {
                "dd/MM/yyyy",  // Handles 02/05/2000 as May 2nd, 2000
                "d/M/yyyy",    // Handles 2/5/2000 as May 2nd, 2000
                "dd/MM/yy",    // Handles 02/05/00 as May 2nd, 2000
                "d/M/yy"       // Handles 2/5/00 as May 2nd, 2000
        };

        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setLenient(false); //  stricter parsing to avoid ambiguity

        for (String format : dateFormats) {
            try {
                sdf.applyPattern(format);
                Date date = sdf.parse(dateString);

                // Handle 2-digit year conversion for yy formats
                if (format.endsWith("/yy")) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);

                    // Adjust 2-digit year logic:
                    // Years 00-30 -> 2000-2030
                    // Years 31-99 -> 1931-1999
                    if (year <= 30) {
                        cal.set(Calendar.YEAR, 2000 + year);
                    } else if (year >= 31 && year <= 99) {
                        cal.set(Calendar.YEAR, 1900 + year);
                    }
                    date = cal.getTime();
                }

                return date;
            } catch (ParseException e) {
                continue;
            }
        }

        return null;
    }

    private static Long getNumericCellAsLong(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.STRING) {
                return Long.parseLong(cell.getStringCellValue().replaceAll("\\D+", ""));
            } else if (cell.getCellType() == CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Optionally handle date-to-long conversion or throw an exception
                    throw new IllegalArgumentException("Date cannot be converted to Long");
                }
                return (long) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.FORMULA) {
                switch (cell.getCachedFormulaResultType()) {
                    case NUMERIC:
                        return (long) cell.getNumericCellValue();
                    case STRING:
                        return Long.parseLong(cell.getStringCellValue().replaceAll("\\D+", ""));
                    default:
                        throw new IllegalArgumentException("Unsupported formula result type");
                }
            } else {
                return null;
            }
        } catch (NumberFormatException | IllegalStateException e) {
            return null;
        }
    }

    private static String getStringOrNumericCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // Convert date to a string format if needed
                        return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                    }
                    return String.valueOf((long) cell.getNumericCellValue());
                case FORMULA:
                    switch (cell.getCachedFormulaResultType()) {
                        case STRING:
                            return cell.getStringCellValue();
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                            }
                            return String.valueOf((long) cell.getNumericCellValue());
                        default:
                            throw new IllegalArgumentException("Unsupported formula result type");
                    }
                default:
                    return null;
            }
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public void saveConsultants() {
        for (Consultant con : consultantsToSave) {
            consultantController.save(con);
        }
        JsfUtil.addSuccessMessage("Saved");
        consultantsToSave = new ArrayList<>();
    }

    public void saveDoctors() {
        System.out.println("saveDoctors");
        for (Doctor doc : doctorsTosave) {
            doctorController.save(doc);
        }
        JsfUtil.addSuccessMessage("Saved");
        doctorsTosave = new ArrayList<>();
    }

    public void saveStaff() {
        for (Staff stf : staffToSave) {
            staffController.save(stf);
        }
        JsfUtil.addSuccessMessage("Saved");
        staffToSave = new ArrayList<>();
    }

    public void saveDepartments() {
        for (Department stf : departments) {
            departmentController.save(stf);
        }
        JsfUtil.addSuccessMessage("Saved");
        departments = new ArrayList<>();
    }

    public void uploadCorrectItemCodes() {
        itemsUpdated = new ArrayList<>();
        itemsSkipped = new ArrayList<>();

        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                processExcelFile(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processExcelFile(InputStream inputStream) throws IOException {
        System.out.println("Processing uploaded Excel file...");

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        // Skip header row
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String itemName = getCellValueAsString(row.getCell(0));
            System.out.println("itemName = " + itemName);
            String institutionName = getCellValueAsString(row.getCell(1));
            String correctCode = getCellValueAsString(row.getCell(2));

            if (itemName.isEmpty() || institutionName.isEmpty() || correctCode.isEmpty()) {
                System.out.println("Skipping row " + row.getRowNum() + " due to missing data.");
                continue;
            }

            // Find the item by exact match of Item Name and Institution Name
            Item item = itemController.findItemByNameAndInstitution(itemName, institutionName);
            if (item != null) {
                System.out.println("Updating item: " + item.getName() + " in " + institutionName + " with new code: " + correctCode);
                System.out.println("item.getCode() Before= " + item.getCode());
                item.setCode(correctCode);
                System.out.println("item = " + item);
                System.out.println("item.getId() = " + item.getId());
                itemFacade.edit(item);
                System.out.println("item.getCode() After= " + item.getCode());
                itemsUpdated.add(item);
            } else {
                System.out.println("Skipping item: " + itemName + " in " + institutionName + " (not found).");
//                itemsSkipped.add(new SkippedItem(itemName, institutionName));
            }
        }

        workbook.close();
        System.out.println("Excel file processing completed.");
    }

    private List<Item> readOpdItemsAndFeesFromExcel(InputStream inputStream) throws IOException {
        System.out.println("readOpdItemsAndFeesFromExcel");
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemsToSave = new ArrayList<>();
        itemFeesToSave = new ArrayList<>();
        categoriesSaved = new ArrayList<>();
        institutionsSaved = new ArrayList<>();
        departmentsSaved = new ArrayList<>();
        itemsSkipped = new ArrayList<>();

        Item item;

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        System.out.println("Reading rows from Excel...");

        while (rowIterator.hasNext()) {
            Category category = null;
            Category financialCategory = null;
            Category feeList = null;
            Row row = rowIterator.next();

            Institution institution;
            Department department;
            String institutionName = null;
            String departmentName = null;
            String name = null;
            String code = null;
            Double hospitalFee = 0.0;
            String feeName = "Hospital Fee";
            String siteName = null; // New site column
            String financialCategoryName = null;
            String categoryName = null;
            String feeListName = null;

            System.out.println("Reading data from row " + row.getRowNum());

            // Column 0: Name (Required)
            Cell nameCell = row.getCell(0);
            if (nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();
                System.out.println("Name: " + name);
                if (name == null || name.trim().equals("")) {
                    System.out.println("Skipping row due to missing name.");
                    continue;  // Skip row if name is missing
                }
            }

            // Column 4: Category (Required)
            Cell catCell = row.getCell(4);
            if (catCell != null && catCell.getCellType() == CellType.STRING) {
                categoryName = catCell.getStringCellValue();
                System.out.println("Category Name: " + categoryName);
                if (categoryName != null && !categoryName.trim().equals("")) {
                    category = categoryController.findCategoryByName(categoryName);
                    System.out.println("Category found: " + category);
                }
            }

            // Column 5: Financial Category (Optional)
            Cell fcatCell = row.getCell(5);
            if (fcatCell != null && fcatCell.getCellType() == CellType.STRING) {
                financialCategoryName = fcatCell.getStringCellValue();
                System.out.println("Financial Category Name: " + financialCategoryName);
                if (financialCategoryName != null && !financialCategoryName.trim().equals("")) {
                    financialCategory = categoryController.findCategoryByName(financialCategoryName);
                    System.out.println("Financial Category found: " + financialCategory);
                }
            }

            // Column 6: Institution (Optional)
            Cell insCell = row.getCell(6);
            if (insCell != null && insCell.getCellType() == CellType.STRING) {
                institutionName = insCell.getStringCellValue();
                System.out.println("Institution Name: " + institutionName);
            }
            if (institutionName == null || institutionName.trim().equals("")) {
                institutionName = sessionController.getInstitution().getName();
                System.out.println("Using logged institution: " + institutionName);
            }

            // Column 7: Department (Optional)
            Cell deptCell = row.getCell(7);
            if (deptCell != null && deptCell.getCellType() == CellType.STRING) {
                departmentName = deptCell.getStringCellValue();
                System.out.println("Department Name: " + departmentName);
            }
            if (departmentName == null || departmentName.trim().equals("")) {
                departmentName = sessionController.getDepartment().getName();
                System.out.println("Using logged department: " + departmentName);
            }

            // Column 10: Fee Name (Optional, default "Hospital Fee")
            Cell feeNameCell = row.getCell(10);
            if (feeNameCell != null && feeNameCell.getCellType() == CellType.STRING) {
                feeName = feeNameCell.getStringCellValue();
                System.out.println("Fee Name: " + feeName);
            }

            // Column 11: Hospital Fee (Optional, for logged institution and department)
            Cell hospitalFeeTypeCell = row.getCell(11);
            if (hospitalFeeTypeCell != null) {
                hospitalFee = extractHospitalFee(hospitalFeeTypeCell);
                System.out.println("Hospital Fee: " + hospitalFee);
            }

            // Column 12: Site or Collecting Centre (Optional)
            Cell siteCell = row.getCell(12);
            if (siteCell != null && siteCell.getCellType() == CellType.STRING) {
                siteName = siteCell.getStringCellValue();
                System.out.println("Site Name: " + siteName);
            }

            // Column 13: Fee List (Optional)
            Cell feeListCell = row.getCell(13);
            if (feeListCell != null && feeListCell.getCellType() == CellType.STRING) {
                feeListName = feeListCell.getStringCellValue();
                System.out.println("Fee List Name: " + feeListName);
                if (feeListName != null && !feeListName.trim().equals("")) {
                    feeList = categoryController.findCategoryByName(feeListName);
                    System.out.println("Fee List found: " + feeList);
                }
            }

            // Handle institution and department
            institution = institutionController.findAndSaveInstitutionByName(institutionName);
            System.out.println("Institution: " + institution);
            department = departmentController.findAndSaveDepartmentByName(departmentName, institution);
            System.out.println("Department: " + department);

            // Handle code and item lookup
            code = row.getCell(3) != null ? row.getCell(3).getStringCellValue() : serviceController.generateShortCode(name);
            System.out.println("Item Code: " + code);
            item = itemController.findItemByCode(code);
            if (item == null) {
                System.out.println("Creating new item for code: " + code);
                item = createItem(row, code, name, institution, department, category, financialCategory);
            } else {
                System.out.println("Item found for code: " + code);
            }

            // Save or update the item
            if (item.getId() == null) {
                itemFacade.create(item);
                System.out.println("Item created: " + item);
            } else {
                itemFacade.edit(item);
                System.out.println("Item updated: " + item);
            }

            // Create and save the ItemFee object
            ItemFee itf = new ItemFee();
            itf.setName(feeName);
            itf.setItem(item);
            itf.setInstitution(institution);
            itf.setDepartment(department);
            itf.setFeeType(FeeType.OwnInstitution);
            itf.setFee(hospitalFee);
            itf.setFfee(hospitalFee);
            itf.setCreatedAt(new Date());
            itf.setCreater(sessionController.getLoggedUser());
            itf.setForCategory(feeList);

            if (siteName != null && !siteName.trim().isEmpty()) {
                Institution siteInstitution = institutionController.findAndSaveInstitutionByName(siteName);
                itf.setForInstitution(siteInstitution);
                System.out.println("Set site institution for ItemFee: " + siteInstitution);
            }

            if (itf.getId() == null) {
                itemFeeFacade.create(itf);
                System.out.println("ItemFee created: " + itf);
            } else {
                itemFeeFacade.edit(itf);
                System.out.println("ItemFee updated: " + itf);
            }

            // Save the fee to the list
            itemFeesToSave.add(itf);
            itemsToSave.add(item);
            System.out.println("Item and ItemFee added to save list.");
        }

        workbook.close(); // Always close the workbook to prevent memory leaks
        System.out.println("Finished reading and processing Excel data.");
        return itemsToSave;
    }

    private Double extractHospitalFee(Cell hospitalFeeTypeCell) {
        Double hospitalFee = 0.0;
        if (hospitalFeeTypeCell.getCellType() == CellType.NUMERIC) {
            hospitalFee = hospitalFeeTypeCell.getNumericCellValue();
        } else if (hospitalFeeTypeCell.getCellType() == CellType.FORMULA) {
            FormulaEvaluator evaluator = hospitalFeeTypeCell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
            CellValue cellValue = evaluator.evaluate(hospitalFeeTypeCell);
            if (cellValue.getCellType() == CellType.NUMERIC) {
                hospitalFee = cellValue.getNumberValue();
            }
        } else if (hospitalFeeTypeCell.getCellType() == CellType.STRING) {
            hospitalFee = CommonFunctions.stringToDouble(hospitalFeeTypeCell.getStringCellValue());
        }
        return hospitalFee == null || hospitalFee < 0 ? 0.0 : hospitalFee;
    }

    private Item createItem(Row row, String code, String name, Institution institution, Department department) {
        System.out.println("createItem = ");
        String itemType = row.getCell(9) != null ? row.getCell(9).getStringCellValue() : "Investigation";
        Item item = null;
        System.out.println("itemType = " + itemType);
        if (itemType.equals("Service")) {
            item = new Service();
        } else if (itemType.equals("Investigation")) {
            item = new Investigation();
        } else if (itemType.equals("InwardService")) {
            item = new InwardService();
        } else if (itemType.equals("Surgery")) {
            item = new ClinicalEntity();
            ((ClinicalEntity) item).setSymanticType(SymanticType.Therapeutic_Procedure);
        }

        if (item != null) {
            item.setName(name);
            item.setCode(code);
            item.setInstitution(institution);
            item.setDepartment(department);
            item.setCreater(sessionController.getLoggedUser());
            item.setCreatedAt(new Date());
        }

        return item;
    }

    private Item createItem(Row row, String code, String name, Institution institution, Department department, Category category, Category financialCategory) {
        System.out.println("createItem = ");
        String itemType = row.getCell(9) != null ? row.getCell(9).getStringCellValue() : "Investigation";
        Item item = null;
        System.out.println("itemType = " + itemType);
        if (itemType.equals("Service")) {
            item = new Service();
        } else if (itemType.equals("Investigation")) {
            item = new Investigation();
        } else if (itemType.equals("InwardService")) {
            item = new InwardService();
        } else if (itemType.equals("Surgery")) {
            item = new ClinicalEntity();
            ((ClinicalEntity) item).setSymanticType(SymanticType.Therapeutic_Procedure);
        }

        if (item != null) {
            item.setName(name);
            item.setCode(code);
            item.setInstitution(institution);
            item.setDepartment(department);
            item.setCategory(category);
            item.setFinancialCategory(financialCategory);
            item.setCreater(sessionController.getLoggedUser());
            item.setCreatedAt(new Date());
        }

        return item;
    }

    private List<Item> readOpdItemsAndDoctorFeesFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemsToSave = new ArrayList<>();
//      masterItemsToSave = new ArrayList<>();
        itemFeesToSave = new ArrayList<>();
        categoriesSaved = new ArrayList<>();
        institutionsSaved = new ArrayList<>();
        departmentsSaved = new ArrayList<>();
        itemsSkipped = new ArrayList<>();
        itemsSaved = new ArrayList<>();

        Item item;
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {

            Institution runningIns = null;
            Department runningDept = null;
            Row row = rowIterator.next();

            Staff staff = null;

            String name = null;
            String code = null;
            String staffName = null;
            String institutionName = null;
            String departmentName = null;
            String feeName = "Doctor Fee";
            Double doctorFee = 0.0;

            Cell staffCell = row.getCell(2);
            if (staffCell != null && staffCell.getCellType() == CellType.STRING) {
                staffName = staffCell.getStringCellValue();
            }
//            System.out.println("staffName = " + staffName);
            if (staffName != null) {
                staff = staffController.findStaffByName(staffName);
                System.out.println("staff = " + staff);
            }

            if (staff == null) {
                System.out.println("SKipping. Staff Null");
                continue;
            }

            Cell insCell = row.getCell(4);
            if (insCell != null && insCell.getCellType() == CellType.STRING) {
                institutionName = insCell.getStringCellValue();
            }
            if (institutionName == null || institutionName.trim().equals("")) {
                institutionName = sessionController.getInstitution().getName();
            }
            if (runningIns == null) {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                institutionsSaved.add(institution);
                runningIns = institution;
            } else if (runningIns.getName().equals(institutionName)) {
                institution = runningIns;
            } else {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                institutionsSaved.add(institution);
                runningIns = institution;
            }
            System.out.println("institutionName = " + institutionName);

            Cell deptCell = row.getCell(5);
            if (deptCell != null && deptCell.getCellType() == CellType.STRING) {
                departmentName = deptCell.getStringCellValue();
            }
            if (departmentName == null || departmentName.trim().equals("")) {
                departmentName = sessionController.getDepartment().getName();
            }
            if (runningDept == null) {
                department = departmentController.findAndSaveDepartmentByName(departmentName, institution);
                runningDept = department;
                departmentsSaved.add(department);
            } else if (runningDept.getName().equals(departmentName)) {
                department = runningDept;
            } else {
                department = departmentController.getDefaultDepatrment(institution);
                runningDept = department;
                departmentsSaved.add(department);
            }
            System.out.println("departmentName = " + departmentName);

            Cell codeCell = row.getCell(0);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            } else if (codeCell != null && codeCell.getCellType() == CellType.NUMERIC) {
                code = codeCell.getNumericCellValue() + "";
            }
            if (code == null || code.trim().equals("")) {
                code = serviceController.generateShortCode(name);
            }

            Cell nameCell = row.getCell(1);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();
                if (name == null || name.trim().equals("")) {
                    continue;
                }
            }

            name = CommonFunctions.sanitizeStringForDatabase(name);
            item = itemController.findItemByCode(code);

            Cell feeNameCell = row.getCell(5);
            if (feeNameCell != null && feeNameCell.getCellType() == CellType.STRING) {
                feeName = feeNameCell.getStringCellValue();
            }
            System.out.println("feeName = " + feeName);

            Cell doctorFeeTypeCell = row.getCell(4);
            if (doctorFeeTypeCell != null) {
                if (doctorFeeTypeCell.getCellType() == CellType.NUMERIC) {
                    // If it's a numeric value
                    doctorFee = doctorFeeTypeCell.getNumericCellValue();
                } else if (doctorFeeTypeCell.getCellType() == CellType.FORMULA) {
                    // If it's a formula, evaluate it
                    Workbook wb = doctorFeeTypeCell.getSheet().getWorkbook();
                    CreationHelper createHelper = wb.getCreationHelper();
                    FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(doctorFeeTypeCell);

                    // Check the type of the evaluated value
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        doctorFee = cellValue.getNumberValue();
                    } else {
                        // Handle other types if needed
                    }
                } else if (doctorFeeTypeCell.getCellType() == CellType.STRING) {
                    // If it's a numeric value
                    String strhospitalFee = doctorFeeTypeCell.getStringCellValue();
                    doctorFee = CommonFunctions.stringToDouble(strhospitalFee);
                }

                if (doctorFee == null || doctorFee < 0) {
                    doctorFee = 0.0;
                }

                ItemFee itf = new ItemFee();
                itf.setName(feeName);
                itf.setItem(item);
                itf.setInstitution(institution);
                itf.setDepartment(department);
                itf.setFeeType(FeeType.Staff);
                itf.setFee(doctorFee);
                itf.setFfee(doctorFee);
                itf.setSpeciality(staff.getSpeciality());
                itf.setStaff(staff);
                itf.setCreatedAt(new Date());
                itf.setCreater(sessionController.getLoggedUser());
                itemFeeFacade.create(itf);

            }

            System.out.println("item = 2 " + item);
            if (item != null) {
                itemsSaved.add(item);
            }
        }
        return itemsSaved;
    }

    private List<ClinicalEntity> readSurgeriesFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        surgeriesToSave = new ArrayList<>();
        surgeriesToSkiped = new ArrayList<>();
        departmentsSaved = new ArrayList<>();
        itemsSkipped = new ArrayList<>();
        institutionsSaved = new ArrayList<>();
        itemsToSave = new ArrayList<>();
        ClinicalEntity item;
        // New running financial category

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Institution runningIns = null;
            Department runningDept = null;
            Row row = rowIterator.next();

            Institution institution;
            Department department;

            String name = null;
            String description = "";
            String printingName = null;
            String fullName = null;
            String code = null;
            String categoryName = null;
            String financialCategoryName = null;
            String institutionName = null;
            String departmentName = null;
            String inwardName = null;

            Cell insCell = row.getCell(6);
            if (insCell != null && insCell.getCellType() == CellType.STRING) {
                institutionName = insCell.getStringCellValue();
            }
            if (institutionName == null || institutionName.trim().equals("")) {
                institutionName = sessionController.getInstitution().getName();
            }

            if (runningIns == null) {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                institutionsSaved.add(institution);
                runningIns = institution;
            } else if (runningIns.getName().equals(institutionName)) {
                institution = runningIns;
            } else {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                institutionsSaved.add(institution);
                runningIns = institution;
            }

            Cell deptCell = row.getCell(5);
            if (deptCell != null && deptCell.getCellType() == CellType.STRING) {
                departmentName = deptCell.getStringCellValue();
            }
            if (departmentName == null || departmentName.trim().equals("")) {
                departmentName = sessionController.getDepartment().getName();
            }
            if (runningDept == null) {
                department = departmentController.findAndSaveDepartmentByName(departmentName, institution);
                runningDept = department;
                departmentsSaved.add(department);
            } else if (runningDept.getName().equals(departmentName)) {
                department = runningDept;
            } else {
                department = departmentController.getDefaultDepatrment(institution);
                runningDept = department;
                departmentsSaved.add(department);
            }

            Cell nameCell = row.getCell(0);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();
                if (name == null || name.trim().equals("")) {
                    continue;
                }
            }

            Cell printingNameCell = row.getCell(2);
            if (printingNameCell != null && printingNameCell.getCellType() == CellType.STRING) {
                printingName = printingNameCell.getStringCellValue();
            }
            if (printingName == null || printingName.trim().equals("")) {
                printingName = name;
            }

            Cell fullNameCell = row.getCell(1);
            if (fullNameCell != null && fullNameCell.getCellType() == CellType.STRING) {
                fullName = fullNameCell.getStringCellValue();
            }
            if (fullName == null || fullName.trim().equals("")) {
                fullName = name;
            }

            name = CommonFunctions.sanitizeStringForDatabase(name);
            item = clinicalEntityController.findItemByName(name, department);
            if (item != null) {
                itemsSkipped.add(item);
                continue;
            }

            Cell codeCell = row.getCell(3);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            } else if (codeCell != null && codeCell.getCellType() == CellType.NUMERIC) {
                code = codeCell.getNumericCellValue() + "";
            }
            if (code == null || code.trim().equals("")) {
                code = serviceController.generateShortCode(name);
            }

            Cell descriptionCell = row.getCell(1);
            if (descriptionCell != null && descriptionCell.getCellType() == CellType.STRING) {
                description = descriptionCell.getStringCellValue();
            }
            if (description == null || description.trim().equals("")) {
                description = name;
            }

            ClinicalEntity cli = new ClinicalEntity();
            cli.setName(name);
            cli.setPrintName(printingName);
            cli.setFullName(fullName);
            cli.setCode(code);
            cli.setInstitution(institution);
            cli.setDepartment(department);
            cli.setSymanticType(SymanticType.Therapeutic_Procedure);
            cli.setCreater(sessionController.getLoggedUser());
            cli.setCreatedAt(new Date());
            item = cli;
            itemController.saveSelected(item);
            if (item != null) {
                surgeriesToSave.add(item);
            }
        }
        return surgeriesToSave;
    }

    private List<ItemFee> replaceFeesFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemFeesToSave = new ArrayList<>();

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            ItemFee fee = null;  // Ensure fee is initialized correctly
            String feeIdString = null;
            String feeName = null;
            Long feeIdLong = null;
            Double feeValue = 0.0;
            Double foreignFeeValue = 0.0;
            boolean retired = false;
            boolean discountAllowed = true;

            // Column 0: Fee ID
            Cell idCell = row.getCell(0);
            if (idCell != null) {
                if (idCell.getCellType() == CellType.STRING) {
                    feeIdString = idCell.getStringCellValue();
                    fee = itemFeeController.findItemFeeFromItemFeeId(feeIdString);
                } else if (idCell.getCellType() == CellType.NUMERIC) {
                    feeIdLong = (long) idCell.getNumericCellValue();
                    fee = itemFeeController.findItemFeeFromItemFeeId(feeIdLong);
                }
            }

            if (fee == null) {
                continue; // Skip if fee not found
            }

            // Column 3: Fee Name
            Cell feeNameCell = row.getCell(3);
            if (feeNameCell != null && feeNameCell.getCellType() == CellType.STRING) {
                feeName = feeNameCell.getStringCellValue();
            }

            // Column 10: Fee Value for Locals
            Cell feeValueCell = row.getCell(10);
            if (feeValueCell != null) {
                if (feeValueCell.getCellType() == CellType.NUMERIC) {
                    feeValue = feeValueCell.getNumericCellValue();
                } else if (feeValueCell.getCellType() == CellType.FORMULA) {
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(feeValueCell);
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        feeValue = cellValue.getNumberValue();
                    }
                } else if (feeValueCell.getCellType() == CellType.STRING) {
                    feeValue = CommonFunctions.stringToDouble(feeValueCell.getStringCellValue());
                }
            }

            // Column 11: Fee Value for Foreigners
            Cell foreignFeeValueCell = row.getCell(11);
            if (foreignFeeValueCell != null) {
                if (foreignFeeValueCell.getCellType() == CellType.NUMERIC) {
                    foreignFeeValue = foreignFeeValueCell.getNumericCellValue();
                } else if (foreignFeeValueCell.getCellType() == CellType.FORMULA) {
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(foreignFeeValueCell);
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        foreignFeeValue = cellValue.getNumberValue();
                    }
                } else if (foreignFeeValueCell.getCellType() == CellType.STRING) {
                    foreignFeeValue = CommonFunctions.stringToDouble(foreignFeeValueCell.getStringCellValue());
                }
            }

            // Column 6: Retired (Yes/No)
            Cell retiredCell = row.getCell(6);
            if (retiredCell != null && retiredCell.getCellType() == CellType.STRING) {
                String retiredString = retiredCell.getStringCellValue();
                retired = retiredString.equalsIgnoreCase("Yes");
            }

            // Column 5: Discount Allowed (Yes/No)
            Cell discountAllowedCell = row.getCell(5);
            if (discountAllowedCell != null && discountAllowedCell.getCellType() == CellType.STRING) {
                String discountAllowedString = discountAllowedCell.getStringCellValue();
                discountAllowed = discountAllowedString.equalsIgnoreCase("Yes");
            }

            // Update fee details
            fee.setName(feeName);
            fee.setFee(feeValue);
            fee.setFfee(foreignFeeValue);
            fee.setRetired(retired);
            if (retired) {
                fee.setRetiredAt(new Date());
                fee.setRetirer(sessionController.getLoggedUser());
            }
            fee.setDiscountAllowed(discountAllowed);

            // Save the fee
            itemFeesToSave.add(fee);
            feeFacade.edit(fee);
        }

        workbook.close(); // Always close the workbook to prevent memory leaks
        return itemFeesToSave;
    }

    private List<ItemFee> replaceFeesFromExcelForCollectingCentreSpecificFees(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemFeesToSave = new ArrayList<>();

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            ItemFee fee = null;  // Ensure fee is initialized correctly
            String feeIdString = null;
            String feeName = null;
            Long feeIdLong = null;
            Double feeValue = 0.0;
            Double foreignFeeValue = 0.0;
            boolean retired = false;
            boolean discountAllowed = true;

            // Column 0: Fee ID
            Cell idCell = row.getCell(0);
            if (idCell != null) {
                if (idCell.getCellType() == CellType.STRING) {
                    feeIdString = idCell.getStringCellValue();
                    fee = itemFeeController.findItemFeeFromItemFeeId(feeIdString);
                } else if (idCell.getCellType() == CellType.NUMERIC) {
                    feeIdLong = (long) idCell.getNumericCellValue();
                    fee = itemFeeController.findItemFeeFromItemFeeId(feeIdLong);
                }
            }

            if (fee == null) {
                continue; // Skip if fee not found
            }

            // Column 1: Collecting Centre Name (Not used in this context but read to maintain correct indexing)
            Cell ccNameCell = row.getCell(1);
            if (ccNameCell != null && ccNameCell.getCellType() == CellType.STRING) {
                String ccName = ccNameCell.getStringCellValue();
                // You can use ccName if needed, but it seems you just want to update the fee
            }

            // Column 2: Collecting Centre Code (Not used in this context but read to maintain correct indexing)
            Cell ccCodeCell = row.getCell(2);
            if (ccCodeCell != null && ccCodeCell.getCellType() == CellType.STRING) {
                String ccCode = ccCodeCell.getStringCellValue();
                // You can use ccCode if needed, but it seems you just want to update the fee
            }

            // Column 3: Fee Name
            Cell feeNameCell = row.getCell(3);
            if (feeNameCell != null && feeNameCell.getCellType() == CellType.STRING) {
                feeName = feeNameCell.getStringCellValue();
            }

            // Column 10: Fee Value for Locals
            Cell feeValueCell = row.getCell(12);
            if (feeValueCell != null) {
                if (feeValueCell.getCellType() == CellType.NUMERIC) {
                    feeValue = feeValueCell.getNumericCellValue();
                } else if (feeValueCell.getCellType() == CellType.FORMULA) {
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(feeValueCell);
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        feeValue = cellValue.getNumberValue();
                    }
                } else if (feeValueCell.getCellType() == CellType.STRING) {
                    feeValue = CommonFunctions.stringToDouble(feeValueCell.getStringCellValue());
                }
            }

            // Column 11: Fee Value for Foreigners
            Cell foreignFeeValueCell = row.getCell(13);
            if (foreignFeeValueCell != null) {
                if (foreignFeeValueCell.getCellType() == CellType.NUMERIC) {
                    foreignFeeValue = foreignFeeValueCell.getNumericCellValue();
                } else if (foreignFeeValueCell.getCellType() == CellType.FORMULA) {
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(foreignFeeValueCell);
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        foreignFeeValue = cellValue.getNumberValue();
                    }
                } else if (foreignFeeValueCell.getCellType() == CellType.STRING) {
                    foreignFeeValue = CommonFunctions.stringToDouble(foreignFeeValueCell.getStringCellValue());
                }
            }

            // Column 6: Retired (Yes/No)
            Cell retiredCell = row.getCell(8);
            if (retiredCell != null && retiredCell.getCellType() == CellType.STRING) {
                String retiredString = retiredCell.getStringCellValue();
                retired = retiredString.equalsIgnoreCase("Yes");
            }

            // Column 5: Discount Allowed (Yes/No)
            Cell discountAllowedCell = row.getCell(7);
            if (discountAllowedCell != null && discountAllowedCell.getCellType() == CellType.STRING) {
                String discountAllowedString = discountAllowedCell.getStringCellValue();
                discountAllowed = discountAllowedString.equalsIgnoreCase("Yes");
            }

            // Update fee details
            fee.setName(feeName);
            fee.setFee(feeValue);
            fee.setFfee(foreignFeeValue);
            fee.setRetired(retired);
            if (retired) {
                fee.setRetiredAt(new Date());
                fee.setRetirer(sessionController.getLoggedUser());
            }
            fee.setDiscountAllowed(discountAllowed);

            // Save the fee
            itemFeesToSave.add(fee);
            feeFacade.edit(fee);
        }

        workbook.close(); // Always close the workbook to prevent memory leaks
        return itemFeesToSave;
    }

    private List<Item> readCollectingCentreItemsAndFeesFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemsToSave = new ArrayList<>();
        masterItemsToSave = new ArrayList<>();
        itemFeesToSave = new ArrayList<>();
        categoriesSaved = new ArrayList<>();
        institutionsSaved = new ArrayList<>();
        departmentsSaved = new ArrayList<>();
        itemsSkipped = new ArrayList<>();

        Item item;
        Institution runningIns = null;
        Department runningDept = null;
        Category runningCategory = null;

        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Category category;
            Institution institution;
            Department department;
            InwardChargeType iwct = null;

            String name = null;
            String comments = "";
            String printingName = null;
            String fullName = null;
            String code = null;
            String categoryName = null;
            String institutionName = null;
            String departmentName = null;
            String inwardName = null;

            String itemType = "Service";
            Double hospitalFee = 0.0;
            Double collectingCentreFee = 0.0;

            Cell insCell = row.getCell(5);
            if (insCell != null && insCell.getCellType() == CellType.STRING) {
                institutionName = insCell.getStringCellValue();
            }
            if (institutionName == null || institutionName.trim().equals("")) {
                institutionName = "Other";
            }
            if (runningIns == null) {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                institutionsSaved.add(institution);
                runningIns = institution;
            } else if (runningIns.getName().equals(institutionName)) {
                institution = runningIns;
            } else {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                institutionsSaved.add(institution);
                runningIns = institution;
            }
            Cell deptCell = row.getCell(6);
            if (deptCell != null && deptCell.getCellType() == CellType.STRING) {
                departmentName = deptCell.getStringCellValue();
            }
            if (departmentName == null || departmentName.trim().equals("")) {
                departmentName = institutionName;
            }
            if (runningDept == null) {
                department = departmentController.findAndSaveDepartmentByName(departmentName);
                runningDept = department;
                departmentsSaved.add(department);
            } else if (runningDept.getName().equals(departmentName)) {
                department = runningDept;
            } else {
                department = departmentController.getDefaultDepatrment(institution);
                runningDept = department;
                departmentsSaved.add(department);
            }

            Cell nameCell = row.getCell(0);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();
                if (name == null || name.trim().equals("")) {
                    continue;
                }
            }

            comments = name;
            name = CommonFunctions.sanitizeStringForDatabase(name);

            item = itemController.findItemByName(name, code, department);
            if (item != null) {
                itemsSkipped.add(item);
                continue;
            }

            Item masterItem = itemController.findMasterItemByName(name);

            Cell printingNameCell = row.getCell(1);
            if (printingNameCell != null && printingNameCell.getCellType() == CellType.STRING) {
                printingName = printingNameCell.getStringCellValue();

            }
            if (printingName == null || printingName.trim().equals("")) {
                printingName = name;
            }

            Cell fullNameCell = row.getCell(2);
            if (fullNameCell != null && fullNameCell.getCellType() == CellType.STRING) {
                fullName = fullNameCell.getStringCellValue();
            }
            if (fullName == null || fullName.trim().equals("")) {
                fullName = name;
            }

            Cell codeCell = row.getCell(3);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }
            if (code == null || code.trim().equals("")) {
                code = serviceController.generateShortCode(name);
            }

            Cell categoryCell = row.getCell(4);
            if (categoryCell != null && categoryCell.getCellType() == CellType.STRING) {
                categoryName = categoryCell.getStringCellValue();
            }
            if (categoryName == null || categoryName.trim().equals("")) {
                continue;
            }

            if (runningCategory == null) {
                category = categoryController.findCategoryByName(categoryName);
                if (category == null) {
                    category = new Category();
                    category.setName(categoryName);
                    categoryFacade.create(category);
                    categoriesSaved.add(category);
                }
                runningCategory = category;
            } else if (runningCategory.getName() == null) {
                category = runningCategory;
            } else if (runningCategory.getName().equals(categoryName)) {
                category = runningCategory;
            } else {
                category = categoryController.findCategoryByName(categoryName);
                if (category == null) {
                    category = new Category();
                    category.setName(categoryName);
                    categoryFacade.create(category);
                    categoriesSaved.add(category);
                }
                runningCategory = category;
            }

            Cell inwardCcCell = row.getCell(7);
            if (inwardCcCell != null && inwardCcCell.getCellType() == CellType.STRING) {
                inwardName = inwardCcCell.getStringCellValue();
            }
            if (inwardName != null && !inwardName.trim().equals("")) {
                iwct = enumController.getInaChargeType(inwardName);
            }
            if (iwct == null) {
                iwct = InwardChargeType.OtherCharges;
            }

            Cell itemTypeCell = row.getCell(8);
            if (itemTypeCell != null && itemTypeCell.getCellType() == CellType.STRING) {
                itemType = itemTypeCell.getStringCellValue();
            }
            if (itemType == null || itemType.trim().equals("")) {
                itemType = "Investigation";
            }
            if (itemType.equals("Service")) {
                if (masterItem == null) {
                    masterItem = new Service();
                    masterItem.setName(name);
                    masterItem.setPrintName(printingName);
                    masterItem.setFullName(fullName);
                    masterItem.setCode(code);
                    masterItem.setCategory(category);
                    masterItem.setIsMasterItem(true);
                    masterItem.setInwardChargeType(iwct);
                    masterItem.setCreater(sessionController.getLoggedUser());
                    masterItem.setCreatedAt(new Date());
                    masterItemsToSave.add(masterItem);
                }

                Service service = new Service();
                service.setName(name);
                service.setPrintName(printingName);
                service.setFullName(fullName);
                service.setCode(code);
                service.setCategory(category);
                service.setMasterItemReference(masterItem);
                service.setInstitution(institution);
                service.setDepartment(department);
                service.setInwardChargeType(iwct);
                service.setCreater(sessionController.getLoggedUser());
                service.setCreatedAt(new Date());
                item = service;
            } else if (itemType.equals("Investigation")) {

                if (masterItem == null) {
                    masterItem = new Investigation();
                    masterItem.setName(name);
                    masterItem.setPrintName(printingName);
                    masterItem.setFullName(fullName);
                    masterItem.setCode(code);
                    masterItem.setIsMasterItem(true);
                    masterItem.setCategory(category);
                    masterItem.setInwardChargeType(iwct);
                    masterItem.setCreater(sessionController.getLoggedUser());
                    masterItem.setCreatedAt(new Date());
                    masterItemsToSave.add(masterItem);
                }
                Investigation ix = new Investigation();
                ix.setName(name);
                ix.setPrintName(printingName);
                ix.setFullName(fullName);
                ix.setCode(code);
                ix.setCategory(category);
                ix.setInstitution(institution);
                ix.setDepartment(department);
                ix.setInwardChargeType(iwct);
                ix.setMasterItemReference(masterItem);
                ix.setCreater(sessionController.getLoggedUser());
                ix.setCreatedAt(new Date());
                item = ix;
            }

            if (item == null) {
                continue;
            }

            Cell hospitalFeeTypeCell = row.getCell(9);
            if (hospitalFeeTypeCell != null) {
                if (hospitalFeeTypeCell.getCellType() == CellType.NUMERIC) {
                    // If it's a numeric value
                    hospitalFee = hospitalFeeTypeCell.getNumericCellValue();
                } else if (hospitalFeeTypeCell.getCellType() == CellType.FORMULA) {
                    // If it's a formula, evaluate it
                    Workbook wb = hospitalFeeTypeCell.getSheet().getWorkbook();
                    CreationHelper createHelper = wb.getCreationHelper();
                    FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(hospitalFeeTypeCell);

                    // Check the type of the evaluated value
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        hospitalFee = cellValue.getNumberValue();
                    } else {
                        // Handle other types if needed
                    }
                } else if (hospitalFeeTypeCell.getCellType() == CellType.STRING) {
                    // If it's a numeric value
                    String strhospitalFee = hospitalFeeTypeCell.getStringCellValue();
                    hospitalFee = CommonFunctions.stringToDouble(strhospitalFee);
                }

                // Rest of your code remains the same
                ItemFee itf = new ItemFee();
                itf.setName("Hospital Fee");
                itf.setItem(item);
                itf.setInstitution(sessionController.getInstitution());
                itf.setDepartment(sessionController.getDepartment());
                itf.setFeeType(FeeType.OwnInstitution);
                itf.setFee(hospitalFee);
                itf.setFfee(hospitalFee);
                itf.setCreatedAt(new Date());
                itf.setCreater(sessionController.getLoggedUser());
//                itemFeeFacade.create(itf);
                itemFeesToSave.add(itf);
            }

            Cell collectingCenterFeeTypeCell = row.getCell(10);
            if (collectingCenterFeeTypeCell != null) {
                if (collectingCenterFeeTypeCell.getCellType() == CellType.NUMERIC) {
                    // If it's a numeric value
                    collectingCentreFee = collectingCenterFeeTypeCell.getNumericCellValue();
                } else if (collectingCenterFeeTypeCell.getCellType() == CellType.FORMULA) {
                    // If it's a formula, evaluate it
                    Workbook wb = collectingCenterFeeTypeCell.getSheet().getWorkbook();
                    CreationHelper createHelper = wb.getCreationHelper();
                    FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(collectingCenterFeeTypeCell);

                    // Check the type of the evaluated value
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        collectingCentreFee = cellValue.getNumberValue();
                    } else {
                        // Handle other types if needed
                    }
                }
                if (collectingCenterFeeTypeCell.getCellType() == CellType.STRING) {
                    // If it's a numeric value
                    String strcollectingCentreFee = collectingCenterFeeTypeCell.getStringCellValue();
                    collectingCentreFee = CommonFunctions.stringToDouble(strcollectingCentreFee);
                }

                // Rest of your code remains the same
                ItemFee itf = new ItemFee();
                itf.setName("Hospital Fee");
                itf.setItem(item);
                itf.setInstitution(institution);
                itf.setDepartment(department);
                itf.setFeeType(FeeType.CollectingCentre);
                itf.setFee(collectingCentreFee);
                itf.setFfee(collectingCentreFee);
                itf.setCreatedAt(new Date());
                itf.setCreater(sessionController.getLoggedUser());
                itemFeesToSave.add(itf);
            }

            item.setTotal(hospitalFee + collectingCentreFee);
            item.setTotalForForeigner((hospitalFee + collectingCentreFee) * 2);
            item.setDblValue(hospitalFee + collectingCentreFee);
            itemsToSave.add(item);
        }

        itemFacade.batchCreate(masterItemsToSave, 5000);
        itemFacade.batchCreate(itemsToSave, 5000);
        itemFeeFacade.batchCreate(itemFeesToSave, 10000);

        return itemsToSave;
    }

    private List<ItemFee> readCollectingCentreSpecialFeeUploadFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        outputString = "";
        itemsToSave = new ArrayList<>();
        masterItemsToSave = new ArrayList<>();
        itemFeesToSave = new ArrayList<>();
        categoriesSaved = new ArrayList<>();
        institutionsSaved = new ArrayList<>();
        departmentsSaved = new ArrayList<>();
        itemsSkipped = new ArrayList<>();

        Item item;
        int rowCount = 0;

        if (rowIterator.hasNext()) {
            rowIterator.next(); // Skip header row
        }

        while (rowIterator.hasNext()) {
            rowCount++;
            Row row = rowIterator.next();

            Institution ins;
            Department dept;
            Institution cc;
            FeeType feeType = null;
            Institution feeInstitution = null;
            Department feeDepartment = null;
            Speciality feeSpeciality = null;
            Staff feeStaff = null;

            String name = null;
            String code = null;

            String institutionName = null;
            String departmentName = null;
            String ccName = null;
            String feeName = null;
            String feeTypeName = null;
            String feeSpecialityName = null;
            String feeStaffName = null;
            String feeInstitutionName = null;
            String feeDepartmentName = null;
            Double fee = null;
            Double ffee = null;

            Cell nameCell = row.getCell(0);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();
                if (name == null || name.trim().isEmpty()) {
                    outputString += "Row " + rowCount + ". No Item name given. Skipping.\n";
                    continue;
                }
            }

            Cell codeCell = row.getCell(1);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }

            Cell insCell = row.getCell(2);
            if (insCell != null && insCell.getCellType() == CellType.STRING) {
                institutionName = insCell.getStringCellValue();
            }

            ins = institutionController.findExistingInstitutionByName(institutionName);
            if (ins == null) {
                outputString += "Row " + rowCount + ". No Institution named " + institutionName + ". Skipping.\n";
                continue;
            }

            Cell deptCell = row.getCell(3);
            if (deptCell != null && deptCell.getCellType() == CellType.STRING) {
                departmentName = deptCell.getStringCellValue();
            }

            dept = departmentController.findExistingDepartmentByName(departmentName, ins);
            if (dept == null) {
                outputString += "Row " + rowCount + ". No Department named " + departmentName + " under the institution " + institutionName + ". Skipping.\n";
                continue;
            }

            Cell feeNameCell = row.getCell(4);
            if (feeNameCell != null && feeNameCell.getCellType() == CellType.STRING) {
                feeName = feeNameCell.getStringCellValue();
            }

            Cell feeTypeCell = row.getCell(5);
            if (feeTypeCell != null && feeTypeCell.getCellType() == CellType.STRING) {
                feeTypeName = feeTypeCell.getStringCellValue();
                try {
                    feeType = FeeType.valueOf(feeTypeName);
                } catch (IllegalArgumentException e) {
                    outputString += "Row " + rowCount + ". Invalid Fee Type " + feeTypeName + ". Skipping.\n";
                    continue;
                }
            }

            Cell feeSpecialityCell = row.getCell(6);
            if (feeSpecialityCell != null && feeSpecialityCell.getCellType() == CellType.STRING) {
                feeSpecialityName = feeSpecialityCell.getStringCellValue();
                feeSpeciality = specialityController.findSpeciality(feeSpecialityName, false);
            }

            Cell feeStaffCell = row.getCell(7);
            if (feeStaffCell != null && feeStaffCell.getCellType() == CellType.STRING) {
                feeStaffName = feeStaffCell.getStringCellValue();
                feeStaff = staffController.findStaffByName(feeStaffName);
            }

            Cell feeInstitutionCell = row.getCell(8);
            if (feeInstitutionCell != null && feeInstitutionCell.getCellType() == CellType.STRING) {
                feeInstitutionName = feeInstitutionCell.getStringCellValue();
                feeInstitution = institutionController.findExistingInstitutionByName(feeInstitutionName);
            }

            Cell feeDepartmentCell = row.getCell(9);
            if (feeDepartmentCell != null && feeDepartmentCell.getCellType() == CellType.STRING) {
                feeDepartmentName = feeDepartmentCell.getStringCellValue();
                feeDepartment = departmentController.findExistingDepartmentByName(feeDepartmentName, feeInstitution);
            }

            Cell ccCell = row.getCell(10);
            if (ccCell != null && ccCell.getCellType() == CellType.STRING) {
                ccName = ccCell.getStringCellValue();
            }

            cc = institutionController.findExistingInstitutionByName(ccName);
            if (cc == null) {
                outputString += "Row " + rowCount + ". No Collecting Centre named " + ccName + ". Skipping.\n";
                continue;
            }

            Cell feeCell = row.getCell(11);
            if (feeCell != null) {
                switch (feeCell.getCellType()) {
                    case NUMERIC:
                        fee = feeCell.getNumericCellValue();
                        break;
                    case FORMULA:
                        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                        CellValue cellValue = evaluator.evaluate(feeCell);
                        if (cellValue.getCellType() == CellType.NUMERIC) {
                            fee = cellValue.getNumberValue();
                        }
                        break;
                    case STRING:
                        String strFee = feeCell.getStringCellValue();
                        fee = CommonFunctions.stringToDouble(strFee);
                        break;
                    default:
                        break;
                }
            }

            if (fee == null || fee < 1) {
                outputString += "Row " + rowCount + ". No Fee found or invalid fee. Skipping.\n";
                continue;
            }

            Cell ffeeCell = row.getCell(12);
            if (ffeeCell != null) {
                switch (ffeeCell.getCellType()) {
                    case NUMERIC:
                        ffee = ffeeCell.getNumericCellValue();
                        break;
                    case FORMULA:
                        FormulaEvaluator ffeeEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                        CellValue ffeeCellValue = ffeeEvaluator.evaluate(ffeeCell);
                        if (ffeeCellValue.getCellType() == CellType.NUMERIC) {
                            ffee = ffeeCellValue.getNumberValue();
                        }
                        break;
                    case STRING:
                        String strFfee = ffeeCell.getStringCellValue();
                        ffee = CommonFunctions.stringToDouble(strFfee);
                        break;
                    default:
                        break;
                }
            }

            if (ffee == null || ffee < 1) {
                outputString += "Row " + rowCount + ". No Fee for foreigner found or invalid fee. Skipping.\n";
                continue;
            }

            name = CommonFunctions.sanitizeStringForDatabase(name);
            feeName = CommonFunctions.sanitizeStringForDatabase(feeName);

            item = itemController.findItemByName(name, code, dept);
            if (item == null) {
                outputString += "Row " + rowCount + ". No Item found. Skipping.\n";
                continue;
            }

            ItemFee itf = new ItemFee();
            itf.setName(feeName);
            itf.setItem(item);
            itf.setInstitution(feeInstitution);
            itf.setDepartment(feeDepartment);
            itf.setFeeType(feeType);
            itf.setForCategory(null);
            itf.setForInstitution(cc);
            itf.setSpeciality(feeSpeciality);
            itf.setStaff(feeStaff);
            itf.setFee(fee);
            itf.setFfee(ffee);
            itf.setCreatedAt(new Date());
            itf.setCreater(sessionController.getLoggedUser());

            itemFeesToSave.add(itf);
        }

        itemFeeFacade.batchCreate(itemFeesToSave, 10000);
        return itemFeesToSave;
    }

    private List<Item> readOutSourceDepartmentItemsAndFeesFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemsToSave = new ArrayList<>();
        masterItemsToSave = new ArrayList<>();
        itemFeesToSave = new ArrayList<>();
        categoriesSaved = new ArrayList<>();
        institutionsSaved = new ArrayList<>();
        departmentsSaved = new ArrayList<>();
        itemsSkipped = new ArrayList<>();

        Item item;
        Institution runningIns = null;
        Department runningDept = null;
        Category runningCategory = null;

        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Category category;
            Institution institution;
            Department department;
            InwardChargeType iwct = null;
            Sample sample = null;
            InvestigationTube investigationTube = null;

            String name = null;
            String comment = null;
            String printingName = null;
            String fullName = null;
            String code = null;
            String categoryName = null;
            String institutionName = null;
            String departmentName = null;
            String inwardName = null;
            String samp = null;
            String container = null;

            String itemType = "Investigation";
            Double hospitalFee = 0.0;
            Double outSourceFee = 0.0;

            Cell insCell = row.getCell(5);
            if (insCell != null && insCell.getCellType() == CellType.STRING) {
                institutionName = insCell.getStringCellValue();
            }
            if (institutionName == null || institutionName.trim().equals("")) {
                institutionName = "Other";
            }
            if (runningIns == null) {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                institutionsSaved.add(institution);
                runningIns = institution;
            } else if (runningIns.getName().equals(institutionName)) {
                institution = runningIns;
            } else {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                institutionsSaved.add(institution);
                runningIns = institution;
            }
            Cell deptCell = row.getCell(6);
            if (deptCell != null && deptCell.getCellType() == CellType.STRING) {
                departmentName = deptCell.getStringCellValue();
            }
            if (departmentName == null || departmentName.trim().equals("")) {
                departmentName = institutionName;
            }
            if (runningDept == null) {
                department = departmentController.findAndSaveDepartmentByName(departmentName);
                runningDept = department;
                departmentsSaved.add(department);
            } else if (runningDept.getName().equals(departmentName)) {
                department = runningDept;
            } else {
                department = departmentController.getDefaultDepatrment(institution);
                runningDept = department;
                departmentsSaved.add(department);
            }

            Cell nameCell = row.getCell(0);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();
                if (name == null || name.trim().equals("")) {
                    continue;
                }
            }

            name = CommonFunctions.sanitizeStringForDatabase(name);

            item = itemController.findItemByName(name, code, department);
            if (item != null) {
                itemsSkipped.add(item);
                continue;
            }

            Item masterItem = itemController.findMasterItemByName(name);

            Cell printingNameCell = row.getCell(1);
            if (printingNameCell != null && printingNameCell.getCellType() == CellType.STRING) {
                printingName = printingNameCell.getStringCellValue();

            }
            if (printingName == null || printingName.trim().equals("")) {
                printingName = name;
            }

            Cell fullNameCell = row.getCell(2);
            if (fullNameCell != null && fullNameCell.getCellType() == CellType.STRING) {
                fullName = fullNameCell.getStringCellValue();
            }
            if (fullName == null || fullName.trim().equals("")) {
                fullName = name;
            }

            Cell codeCell = row.getCell(3);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }
            if (code == null || code.trim().equals("")) {
                code = serviceController.generateShortCode(name);
            }

            Cell categoryCell = row.getCell(4);
            if (categoryCell != null && categoryCell.getCellType() == CellType.STRING) {
                categoryName = categoryCell.getStringCellValue();
            }
            if (categoryName == null || categoryName.trim().equals("")) {
                continue;
            }

            if (runningCategory == null) {
                category = categoryController.findCategoryByName(categoryName);
                if (category == null) {
                    category = new Category();
                    category.setName(categoryName);
                    categoryFacade.create(category);
                    categoriesSaved.add(category);
                }
                runningCategory = category;
            } else if (runningCategory.getName() == null) {
                category = runningCategory;
            } else if (runningCategory.getName().equals(categoryName)) {
                category = runningCategory;
            } else {
                category = categoryController.findCategoryByName(categoryName);
                if (category == null) {
                    category = new Category();
                    category.setName(categoryName);
                    categoryFacade.create(category);
                    categoriesSaved.add(category);
                }
                runningCategory = category;
            }

            Cell inwardCcCell = row.getCell(7);
            if (inwardCcCell != null && inwardCcCell.getCellType() == CellType.STRING) {
                inwardName = inwardCcCell.getStringCellValue();
            }
            if (inwardName != null && !inwardName.trim().equals("")) {
                iwct = enumController.getInaChargeType(inwardName);
            }
            if (iwct == null) {
                iwct = InwardChargeType.OtherCharges;
            }

            Cell itemTypeCell = row.getCell(8);
            if (itemTypeCell != null && itemTypeCell.getCellType() == CellType.STRING) {
                itemType = itemTypeCell.getStringCellValue();
            }
            if (itemType == null || itemType.trim().equals("")) {
                itemType = "Investigation";
            }
            if (itemType.equals("Service")) {
                Service service = new Service();
                service.setName(name);
                service.setPrintName(printingName);
                service.setFullName(fullName);
                service.setCode(code);
                service.setCategory(category);
                service.setInstitution(institution);
                service.setDepartment(department);
                service.setInwardChargeType(iwct);
                service.setCreater(sessionController.getLoggedUser());
                service.setCreatedAt(new Date());
                item = service;
            } else if (itemType.equals("Investigation")) {
                Investigation ix = new Investigation();
                ix.setName(name);
                ix.setPrintName(printingName);
                ix.setFullName(fullName);
                ix.setCode(code);
                ix.setCategory(category);
                ix.setInstitution(institution);
                ix.setDepartment(department);
                ix.setInwardChargeType(iwct);
                ix.setCreater(sessionController.getLoggedUser());
                ix.setCreatedAt(new Date());
                item = ix;
            }

            if (item == null) {
                continue;
            }

            Cell sampleCell = row.getCell(9);
            if (sampleCell != null && sampleCell.getCellType() == CellType.STRING) {
                samp = sampleCell.getStringCellValue();
                sample = sampleController.findAndCreateSampleByName(samp);
            }

            if (samp == null || samp.trim().equals("")) {
                continue;
            }

            Cell containerCell = row.getCell(10);
            if (containerCell != null && containerCell.getCellType() == CellType.STRING) {
                container = containerCell.getStringCellValue();
                investigationTube = investigationTubeController.findAndCreateInvestigationTubeByName(container);
            }

            if (container == null || container.trim().equals("")) {
                continue;
            }

            Cell hospitalFeeTypeCell = row.getCell(11);
            if (hospitalFeeTypeCell != null) {
                if (hospitalFeeTypeCell.getCellType() == CellType.NUMERIC) {
                    // If it's a numeric value
                    hospitalFee = hospitalFeeTypeCell.getNumericCellValue();
                } else if (hospitalFeeTypeCell.getCellType() == CellType.FORMULA) {
                    // If it's a formula, evaluate it
                    Workbook wb = hospitalFeeTypeCell.getSheet().getWorkbook();
                    CreationHelper createHelper = wb.getCreationHelper();
                    FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(hospitalFeeTypeCell);

                    // Check the type of the evaluated value
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        hospitalFee = cellValue.getNumberValue();
                    } else {
                        // Handle other types if needed
                    }
                } else if (hospitalFeeTypeCell.getCellType() == CellType.STRING) {
                    // If it's a numeric value
                    String strhospitalFee = hospitalFeeTypeCell.getStringCellValue();
                    hospitalFee = CommonFunctions.stringToDouble(strhospitalFee);
                }

                // Rest of your code remains the same
                ItemFee itf = new ItemFee();
                itf.setName("Hospital Fee");
                itf.setItem(item);
                itf.setInstitution(sessionController.getInstitution());
                itf.setDepartment(sessionController.getDepartment());
                itf.setFeeType(FeeType.OwnInstitution);
                itf.setFee(hospitalFee);
                itf.setFfee(hospitalFee);
                itf.setCreatedAt(new Date());
                itf.setCreater(sessionController.getLoggedUser());
//                itemFeeFacade.create(itf);
                itemFeesToSave.add(itf);
            }

            Cell outsourceFeeTypeCell = row.getCell(12);
            if (outsourceFeeTypeCell != null) {
                if (outsourceFeeTypeCell.getCellType() == CellType.NUMERIC) {
                    // If it's a numeric value
                    outSourceFee = outsourceFeeTypeCell.getNumericCellValue();
                } else if (outsourceFeeTypeCell.getCellType() == CellType.FORMULA) {
                    // If it's a formula, evaluate it
                    Workbook wb = outsourceFeeTypeCell.getSheet().getWorkbook();
                    CreationHelper createHelper = wb.getCreationHelper();
                    FormulaEvaluator evaluator = createHelper.createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(outsourceFeeTypeCell);

                    // Check the type of the evaluated value
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        outSourceFee = cellValue.getNumberValue();
                    } else {
                        // Handle other types if needed
                    }
                }
                if (outsourceFeeTypeCell.getCellType() == CellType.STRING) {
                    // If it's a numeric value
                    String strcollectingCentreFee = outsourceFeeTypeCell.getStringCellValue();
                    outSourceFee = CommonFunctions.stringToDouble(strcollectingCentreFee);
                }

                // Rest of your code remains the same
                ItemFee itf = new ItemFee();
                itf.setName("Other Institution Fee");
                itf.setItem(item);
                itf.setInstitution(institution);
                itf.setDepartment(department);
                itf.setFeeType(FeeType.OtherInstitution);
                itf.setFee(outSourceFee);
                itf.setFfee(outSourceFee);
                itf.setCreatedAt(new Date());
                itf.setCreater(sessionController.getLoggedUser());
                itemFeesToSave.add(itf);
            }

            Cell commentCell = row.getCell(13);
            if (commentCell != null && commentCell.getCellType() == CellType.STRING) {
                comment = commentCell.getStringCellValue();

            }

            item.setTotal(hospitalFee + outSourceFee);
            item.setTotalForForeigner((hospitalFee + outSourceFee) * 2);
            item.setDblValue(hospitalFee + outSourceFee);
            itemsToSave.add(item);
        }

        itemFacade.batchCreate(masterItemsToSave, 5000);
        itemFacade.batchCreate(itemsToSave, 5000);
        itemFeeFacade.batchCreate(itemFeesToSave, 10000);

        return itemsToSave;
    }

    public void uploadCollectingCentres() {
        collectingCentres = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                collectingCentres = readCollectingCentresFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                uploadComplete = false;
                JsfUtil.addErrorMessage("Error in Uploading. " + e.getMessage());
            }
        }
        uploadComplete = true;
        JsfUtil.addSuccessMessage("Successfully Uploaded");
    }

    public void uploadAgencies() {
        agencies = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                agencies = readAgenciesFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                uploadComplete = false;
                JsfUtil.addErrorMessage("Error in Uploading. " + e.getMessage());
            }
        }
        uploadComplete = true;
        JsfUtil.addSuccessMessage("Successfully Uploaded");
    }

    public void uploadSuppliers() {
        suppliers = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                System.out.println("inputStream = " + inputStream);
                suppliers = readSuppliersFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                uploadComplete = false;
                JsfUtil.addErrorMessage("Error in Uploading. " + e.getMessage());
            }
        }
        uploadComplete = true;
        JsfUtil.addSuccessMessage("Successfully Uploaded");
    }

    public void uploadSuppliersExtended() {
        suppliers = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                suppliers = readSuppliersExtendedFromExcel(inputStream);
            } catch (IOException e) {
                Logger.getLogger(DataUploadController.class.getName()).log(Level.SEVERE, "Error uploading suppliers", e);
                uploadComplete = false;
                JsfUtil.addErrorMessage("Error in Uploading. " + e.getMessage());
            }
        }
        uploadComplete = true;
        JsfUtil.addSuccessMessage("Successfully Uploaded");
    }

    public void uploadDepartments() {
        departments = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                departments = readDepartmentFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                uploadComplete = false;
                JsfUtil.addErrorMessage("Error in Uploading. " + e.getMessage());
            }
        }
        uploadComplete = true;
        JsfUtil.addSuccessMessage("Uploaded. Please click the save button to save.");
    }

    public void uploadCreditCOmpanies() {
        creditCompanies = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                creditCompanies = readCreditCOmpanyFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Institution> readCollectingCentresFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Institution> collectingCentresList = new ArrayList<>();
        Institution collectingCentre;

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            collectingCentre = null;
            Route route = null;

            String code = null;
            String collectingCentreName = null;
            Boolean active = null;
            Boolean withCommissionStatus = null;
            String routeName = null;
            Double percentage = null;
            String collectingCentrePrintingName = null;
            Double standardCreditLimit = null;
            Double allowedCreditLimit = null;
            Double maxCreditLimit = null;
            String phone = null;
            String email = null;
            String ownerName = null;
            String address = null;

            Cell codeCell = row.getCell(0);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }

            if (code == null || code.trim().equals("")) {
                continue;
            }

            //    Item masterItem = itemController.findMasterItemByName(code);
            Cell agentNameCell = row.getCell(1);

            if (agentNameCell != null && agentNameCell.getCellType() == CellType.STRING) {
                collectingCentreName = agentNameCell.getStringCellValue();
            }
            if (collectingCentreName == null || collectingCentreName.trim().equals("")) {
                continue;
            }

            Cell agentPrintingNameCell = row.getCell(2);

            if (agentPrintingNameCell != null && agentPrintingNameCell.getCellType() == CellType.STRING) {
                collectingCentrePrintingName = agentPrintingNameCell.getStringCellValue();

            }
            if (collectingCentrePrintingName == null || collectingCentrePrintingName.trim().equals("")) {
                collectingCentrePrintingName = collectingCentreName;
            }

            Cell activeCell = row.getCell(3);

            if (activeCell != null && activeCell.getCellType() == CellType.BOOLEAN) {
                active = activeCell.getBooleanCellValue();

            }
            if (active == null) {
                active = false;
            }

            Cell withCommissionStatusCell = row.getCell(4);

            if (withCommissionStatusCell != null && withCommissionStatusCell.getCellType() == CellType.BOOLEAN) {
                withCommissionStatus = withCommissionStatusCell.getBooleanCellValue();

            }
            if (withCommissionStatus == null) {
                withCommissionStatus = false;
            }

            Cell routeNameCell = row.getCell(5);
            if (routeNameCell != null && routeNameCell.getCellType() == CellType.STRING) {
                routeName = routeNameCell.getStringCellValue();
                route = routeController.findAndCreateRouteByName(routeName);

            }
            if (routeName == null || routeName.trim().equals("")) {
                route = null;
            }

            Cell percentageCell = row.getCell(6);

            if (percentageCell != null) {
                if (percentageCell.getCellType() == CellType.NUMERIC) {
                    percentage = percentageCell.getNumericCellValue();

                } else if (percentageCell.getCellType() == CellType.STRING) {
                    percentage = Double.parseDouble(percentageCell.getStringCellValue());
                }
            }

            if (percentage == null) {
                percentage = 0.0;
            }

            Cell contactNumberCell = row.getCell(7);

            if (contactNumberCell != null) {
                if (contactNumberCell.getCellType() == CellType.NUMERIC) {
                    DecimalFormat decimalFormat = new DecimalFormat("#");
                    phone = decimalFormat.format(contactNumberCell.getNumericCellValue());

                } else if (contactNumberCell.getCellType() == CellType.STRING) {
                    phone = contactNumberCell.getStringCellValue();
                }
            }
            if (phone == null || phone.trim().equals("")) {
                phone = null;
            }

            Cell emailAddressCell = row.getCell(8);

            if (emailAddressCell != null && emailAddressCell.getCellType() == CellType.STRING) {
                email = emailAddressCell.getStringCellValue();

            }
            if (email == null || email.trim().equals("")) {
                email = null;
            }

            Cell ownerNameCell = row.getCell(9);

            if (ownerNameCell != null && ownerNameCell.getCellType() == CellType.STRING) {
                ownerName = ownerNameCell.getStringCellValue();
            }
            if (ownerName == null || ownerName.trim().equals("")) {
                ownerName = null;
            }

            Cell addressCell = row.getCell(10);

            if (addressCell != null && addressCell.getCellType() == CellType.STRING) {
                address = addressCell.getStringCellValue();
            }
            if (address == null || address.trim().equals("")) {
                address = null;
            }

            Cell standardCreditCell = row.getCell(11);
            if (standardCreditCell != null && standardCreditCell.getCellType() == CellType.NUMERIC) {
                standardCreditLimit = standardCreditCell.getNumericCellValue();

            }
            if (standardCreditLimit == null) {
                standardCreditLimit = 0.0;
            }

            Cell allowedCreditCell = row.getCell(12);
            if (allowedCreditCell != null && allowedCreditCell.getCellType() == CellType.NUMERIC) {
                allowedCreditLimit = allowedCreditCell.getNumericCellValue();

            }
            if (allowedCreditLimit == null) {
                allowedCreditLimit = 0.0;
            }

            Cell maxCreditCell = row.getCell(13);
            if (maxCreditCell != null && maxCreditCell.getCellType() == CellType.NUMERIC) {
                maxCreditLimit = maxCreditCell.getNumericCellValue();

            }
            if (maxCreditLimit == null) {
                maxCreditLimit = 0.0;
            }

            if (code.trim().equals("")) {
                continue;
            }

            if (collectingCentreName.trim().equals("")) {
                continue;
            }

            collectingCentre = collectingCentreController.findCollectingCentreByCode(code);
            if (collectingCentre == null) {
                collectingCentre = new Institution();
                collectingCentre.setInstitutionType(InstitutionType.CollectingCentre);
                collectingCentre.setCode(code);
                collectingCentre.setName(collectingCentreName);
            }
            if (withCommissionStatus) {
                collectingCentre.setCollectingCentrePaymentMethod(CollectingCentrePaymentMethod.FULL_PAYMENT_WITH_COMMISSION);
            } else {
                collectingCentre.setCollectingCentrePaymentMethod(CollectingCentrePaymentMethod.PAYMENT_WITHOUT_COMMISSION);
            }

            collectingCentre.setInactive(active);
            collectingCentre.setRoute(route);
            collectingCentre.setChequePrintingName(collectingCentrePrintingName);
            collectingCentre.setPercentage(percentage);
            collectingCentre.setPhone(phone);
            collectingCentre.setEmail(email);
            collectingCentre.setOwnerName(ownerName);
            collectingCentre.setAddress(address);
            collectingCentre.setAllowedCredit(standardCreditLimit);
            collectingCentre.setAllowedCreditLimit(allowedCreditLimit);
            collectingCentre.setMaxCreditLimit(maxCreditLimit);

            collectingCentreController.save(collectingCentre);

            collectingCentresList.add(collectingCentre);
        }

        return collectingCentresList;
    }

    private List<Institution> readAgenciesFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Institution> agencyList = new ArrayList<>();
        Institution agency;

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            agency = null;
            //ToDo: Delete unnecessary fields

            Double codeDbl = 0.0;
            String code = null;
            String agencyName = null;
            Double creditLimit = null;

            Cell codeCell = row.getCell(0);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            } else if (codeCell != null && codeCell.getCellType() == CellType.NUMERIC) {
                codeDbl = codeCell.getNumericCellValue();
                code = codeDbl.toString();
            } else {
                code = UUID.randomUUID().toString();
            }
            System.out.println("code = " + code);

            //    Item masterItem = itemController.findMasterItemByName(code);
            Cell agentNameCell = row.getCell(1);

            if (agentNameCell != null && agentNameCell.getCellType() == CellType.STRING) {
                agencyName = agentNameCell.getStringCellValue();
            }
            System.out.println("agencyName = " + agencyName);
            if (agencyName == null || agencyName.trim().equals("")) {
                continue;
            }

            Cell standardCreditCell = row.getCell(2);
            if (standardCreditCell != null && standardCreditCell.getCellType() == CellType.NUMERIC) {
                creditLimit = standardCreditCell.getNumericCellValue();
            }
            System.out.println("creditLimit = " + creditLimit);
            if (creditLimit == null) {
                creditLimit = 0.0;
            }

            if (code.trim().equals("")) {
                continue;
            }

            if (agencyName.trim().equals("")) {
                continue;
            }

            //Change code to name
//            agency = agencyController.findAgencyByName(code);
            agency = agencyController.findAgencyByName(agencyName);
            System.out.println("agency name " + agency);

            if (agency == null) {
                agency = new Institution();
                agency.setInstitutionType(InstitutionType.Agency);
                agency.setCode(code);
                agency.setName(agencyName);
                agency.setAllowedCreditLimit(creditLimit);
            }

            agencyController.save(agency);

            agencyList.add(agency);
        }

        return agencyList;
    }

    public boolean isRowEmpty(Row row) {
        if (row != null) {
            for (Cell cell : row) {
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<Institution> readSuppliersFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Institution> suppliersList = new ArrayList<>();
        Institution supplier;

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            if (isRowEmpty(row)) {
                continue;
            }

            supplier = null;
            String code = null;
            String supplierName = null;
            Boolean active = null;
            String supplierPrintingName = null;
            String phone = null;
            String email = null;
            String fax = null;
            String web = null;
            String mobilenumber = null;
            String ownerName = null;
            String address = null;

            Cell faxCell = row.getCell(5);
            if (faxCell != null && faxCell.getCellType() == CellType.STRING) {
                fax = faxCell.getStringCellValue();
            }

            Cell emailCell = row.getCell(6);
            if (emailCell != null && emailCell.getCellType() == CellType.STRING) {
                email = emailCell.getStringCellValue();
            }

            Cell webCell = row.getCell(7);
            if (webCell != null && webCell.getCellType() == CellType.STRING) {
                web = webCell.getStringCellValue();
            }

            Cell mobCell = row.getCell(8);
            if (mobCell != null && mobCell.getCellType() == CellType.STRING) {
                mobilenumber = mobCell.getStringCellValue();
            }

            Cell codeCell = row.getCell(0);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }

            //    Item masterItem = itemController.findMasterItemByName(code);
            Cell agentNameCell = row.getCell(1);

            if (agentNameCell != null && agentNameCell.getCellType() == CellType.STRING) {
                supplierName = agentNameCell.getStringCellValue();
            }

            Cell agentPrintingNameCell = row.getCell(2);

            if (agentPrintingNameCell != null && agentPrintingNameCell.getCellType() == CellType.STRING) {
                supplierPrintingName = agentPrintingNameCell.getStringCellValue();

            }
            if (supplierPrintingName == null || supplierPrintingName.trim().equals("")) {
                supplierPrintingName = supplierPrintingName;
            }
            Cell activeCell = row.getCell(3);

            if (activeCell != null && activeCell.getCellType() == CellType.BOOLEAN) {
                active = activeCell.getBooleanCellValue();

            }
            if (active == null) {
                active = false;
            }

            Cell contactNumberCell = row.getCell(4);

            if (contactNumberCell != null) {
                if (contactNumberCell.getCellType() == CellType.NUMERIC) {
                    DecimalFormat decimalFormat = new DecimalFormat("#");
                    phone = decimalFormat.format(contactNumberCell.getNumericCellValue());

                } else if (contactNumberCell.getCellType() == CellType.STRING) {
                    phone = contactNumberCell.getStringCellValue();
                }
            }
            if (phone == null || phone.trim().equals("")) {
                phone = null;
            }

            Cell ownerNameCell = row.getCell(9);

            if (ownerNameCell != null && ownerNameCell.getCellType() == CellType.STRING) {
                ownerName = ownerNameCell.getStringCellValue();
            }
            if (ownerName == null || ownerName.trim().equals("")) {
                ownerName = null;
            }
            Cell addressCell = row.getCell(10);

            if (addressCell != null && addressCell.getCellType() == CellType.STRING) {
                address = addressCell.getStringCellValue();
            }
            if (address == null || address.trim().equals("")) {
                address = null;
            }

            supplier = institutionController.findAndSaveInstitutionByCode(code);

            if (supplier == null) {
                supplier = new Institution();
            }
            supplier.setCode(code);
            supplier.setInstitutionCode(code);
            supplier.setName(supplierName);
            supplier.setInactive(active);
            supplier.setOwnerName(ownerName);
            supplier.setMobile(mobilenumber);
            supplier.setFax(fax);
            supplier.setChequePrintingName(supplierPrintingName);
            supplier.setPhone(phone);
            supplier.setEmail(email);
            supplier.setOwnerName(ownerName);
            supplier.setAddress(address);
            supplier.setInstitutionType(InstitutionType.Dealer);
            institutionController.save(supplier);
            suppliersList.add(supplier);
        }

        return suppliersList;
    }

    private List<Institution> readSuppliersExtendedFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Institution> suppliersList = new ArrayList<>();
        Institution supplier;

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            if (isRowEmpty(row)) {
                continue;
            }

            supplier = null;
            String code = null;
            String supplierName = null;
            String qbSupplierName = null;
            Boolean active = null;
            String contactPersonName = null;
            String address = null;
            String phone = null;
            String fax = null;
            String email = null;
            String web = null;
            String mobilenumber = null;
            String paymentCompanyName = null;
            String bankName = null;
            String branchName = null;
            String accountNo = null;
            String legalCompany = null;
//            String supplierPrintingName = null;
//            String ownerName = null;


            Cell faxCell = row.getCell(7);
            if (faxCell != null) {
                if (faxCell.getCellType() == CellType.NUMERIC) {
                    DecimalFormat decimalFormat = new DecimalFormat("#");
                    fax = decimalFormat.format(faxCell.getNumericCellValue());

                } else if (faxCell.getCellType() == CellType.STRING) {
                    fax = faxCell.getStringCellValue();
                }
            }
            if (fax == null || fax.trim().isEmpty()) {
                fax = null;
            }

            Cell emailCell = row.getCell(8);
            if (emailCell != null && emailCell.getCellType() == CellType.STRING) {
                email = emailCell.getStringCellValue();
            }

            Cell webCell = row.getCell(9);
            if (webCell != null && webCell.getCellType() == CellType.STRING) {
                web = webCell.getStringCellValue();
            }

            Cell mobCell = row.getCell(10);
            if (mobCell != null) {
                if (mobCell.getCellType() == CellType.NUMERIC) {
                    DecimalFormat decimalFormat = new DecimalFormat("#");
                    mobilenumber = decimalFormat.format(mobCell.getNumericCellValue());

                } else if (mobCell.getCellType() == CellType.STRING) {
                    mobilenumber = mobCell.getStringCellValue();
                }
            }
            if (mobilenumber == null || mobilenumber.trim().isEmpty()) {
                mobilenumber = null;
            }

            Cell codeCell = row.getCell(0);
            if (codeCell != null) {
                if (codeCell.getCellType() == CellType.NUMERIC) {
                    DecimalFormat decimalFormat = new DecimalFormat("#");
                    code = decimalFormat.format(codeCell.getNumericCellValue());

                } else if (codeCell.getCellType() == CellType.STRING) {
                    code = codeCell.getStringCellValue();
                }
            }
            if (code == null || code.trim().isEmpty()) {
                code = null;
            }

            //    Item masterItem = itemController.findMasterItemByName(code);
            Cell agentNameCell = row.getCell(1);
            if (agentNameCell != null && agentNameCell.getCellType() == CellType.STRING) {
                supplierName = agentNameCell.getStringCellValue();
            }

            Cell activeCell = row.getCell(3);
            if (activeCell != null && activeCell.getCellType() == CellType.STRING) {
                String cellValue = activeCell.getStringCellValue();
                active = cellValue.equalsIgnoreCase("Active");
            }
            if (active == null) {
                active = false;
            }

            Cell contactNumberCell = row.getCell(6);
            if (contactNumberCell != null) {
                if (contactNumberCell.getCellType() == CellType.NUMERIC) {
                    DecimalFormat decimalFormat = new DecimalFormat("#");
                    phone = decimalFormat.format(contactNumberCell.getNumericCellValue());

                } else if (contactNumberCell.getCellType() == CellType.STRING) {
                    phone = contactNumberCell.getStringCellValue();
                }
            }
            if (phone == null || phone.trim().isEmpty()) {
                phone = null;
            }

            Cell addressCell = row.getCell(5);
            if (addressCell != null && addressCell.getCellType() == CellType.STRING) {
                address = addressCell.getStringCellValue();
            }
            if (address == null || address.trim().isEmpty()) {
                address = null;
            }

            Cell contactPersonNameCell = row.getCell(4);
            if (contactPersonNameCell != null && contactPersonNameCell.getCellType() == CellType.STRING) {
                contactPersonName = contactPersonNameCell.getStringCellValue();
            }
            if (contactPersonName == null || contactPersonName.trim().isEmpty()) {
                contactPersonName = null;
            }

            Cell qbSupplierNameCell = row.getCell(2);
            if (qbSupplierNameCell != null && qbSupplierNameCell.getCellType() == CellType.STRING) {
                qbSupplierName = qbSupplierNameCell.getStringCellValue();
            }
            if (qbSupplierName == null || qbSupplierName.trim().isEmpty()) {
                qbSupplierName = null;
            }

            Cell paymentCompanyNameCell = row.getCell(11);
            if (paymentCompanyNameCell != null && paymentCompanyNameCell.getCellType() == CellType.STRING) {
                paymentCompanyName = paymentCompanyNameCell.getStringCellValue();
            }
            if (paymentCompanyName == null || paymentCompanyName.trim().isEmpty()) {
                paymentCompanyName = null;
            }

            Cell bankNameCell = row.getCell(12);
            if (bankNameCell != null && bankNameCell.getCellType() == CellType.STRING) {
                bankName = bankNameCell.getStringCellValue();
            }
            if (bankName == null || bankName.trim().isEmpty()) {
                bankName = null;
            }

            Cell branchNameCell = row.getCell(13);
            if (branchNameCell != null && branchNameCell.getCellType() == CellType.STRING) {
                branchName = branchNameCell.getStringCellValue();
            }
            if (branchName == null || branchName.trim().isEmpty()) {
                branchName = null;
            }

            Cell accountNoCell = row.getCell(14);
            if (accountNoCell != null && accountNoCell.getCellType() == CellType.STRING) {
                accountNo = accountNoCell.getStringCellValue();
            }
            if (accountNo == null || accountNo.trim().isEmpty()) {
                accountNo = null;
            }

            Cell legalCompanyCell = row.getCell(15);
            if (legalCompanyCell != null && legalCompanyCell.getCellType() == CellType.STRING) {
                legalCompany = legalCompanyCell.getStringCellValue();
            }
            if (legalCompany == null || legalCompany.trim().isEmpty()) {
                legalCompany = null;
            }

            supplier = institutionController.findAndSaveInstitutionByCode(code);

            if (supplier == null) {
                supplier = new Institution();
            }

            supplier.setCode(code);
            supplier.setInstitutionCode(code);
            supplier.setName(supplierName);
            supplier.setInactive(!active);
            supplier.setMobile(mobilenumber);
            supplier.setFax(fax);
            supplier.setPhone(phone);
            supplier.setEmail(email);
//            supplier.setChequePrintingName(supplierPrintingName);
//            supplier.setOwnerName(ownerName);
            supplier.setAddress(address);
            supplier.setWeb(web);
            supplier.setAccountNo(accountNo);
            supplier.setQbSupplierName(qbSupplierName);
            supplier.setContactPersonName(contactPersonName);
            supplier.setPaymentCompanyName(paymentCompanyName);
            supplier.setBankName(bankName);
            supplier.setBranchName(branchName);
            supplier.setLegalCompany(legalCompany);

            supplier.setInstitutionType(InstitutionType.Dealer);
            institutionController.save(supplier);
            suppliersList.add(supplier);
        }

        suppliers = new ArrayList<>(suppliersList);
        return suppliersList;
    }

    private List<ItemFee> readCollectingCentrePriceListFromXcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemFees = new ArrayList<>();

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }
        Institution runningIns = null;
        Department runningDept = null;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Institution institution;
            Department department;
            Item item = null;
            Category category = null;

            String departmentName = null;

            String institutionName = null;
            String itemCode = null;
            String itemName = null;
            String itemTypeName = null;
            String feeListType = null;
            Double price = 0.0;

            Cell insCell = row.getCell(5);
            if (insCell != null && insCell.getCellType() == CellType.STRING) {
                institutionName = insCell.getStringCellValue();
            }
            if (institutionName == null || institutionName.trim().equals("")) {
                institutionName = "Other";
            }
            if (runningIns == null) {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                runningIns = institution;
            } else if (runningIns.getName().equals(institutionName)) {
                institution = runningIns;
            } else {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                runningIns = institution;
            }

            Cell deptCell = row.getCell(6);
            if (deptCell != null && deptCell.getCellType() == CellType.STRING) {
                departmentName = deptCell.getStringCellValue();
            }
            if (departmentName == null || departmentName.trim().equals("")) {
                departmentName = institutionName;
            }
            if (runningDept == null) {
                department = departmentController.findAndSaveDepartmentByName(departmentName);
                runningDept = department;
            } else if (runningDept.getName().equals(departmentName)) {
                department = runningDept;
            } else {
                department = departmentController.getDefaultDepatrment(institution);
                runningDept = department;
            }

            // Column A: Department Code (Required)
            Cell itemCodeCell = row.getCell(0);
            if (itemCodeCell != null && itemCodeCell.getCellType() == CellType.STRING) {
                itemCode = itemCodeCell.getStringCellValue();
            }
            if (itemCode != null) {
            }
            if (itemCode == null || itemCode.trim().isEmpty()) {
                continue;
            }

            // Column B: Department Name (Required)
            Cell itemNameCell = row.getCell(1);
            if (itemNameCell != null && itemNameCell.getCellType() == CellType.STRING) {
                itemName = itemNameCell.getStringCellValue();
            }
            if (itemName == null || itemName.trim().isEmpty()) {
                continue;
            }

            Cell itemTypeCell = row.getCell(3);
            if (itemTypeCell != null && itemTypeCell.getCellType() == CellType.STRING) {
                itemTypeName = itemTypeCell.getStringCellValue();
            }

            if (itemTypeName != null) {
                switch (itemTypeName) {
                    case "Investigation":
                        item = itemController.findAndCreateInvestigationByNameAndCode(itemName, itemCode);
                        System.out.println("itemTypeCell = " + itemTypeName);
                        break;

                    case "Service":
                        item = itemController.findAndCreateServiceByNameAndCode(itemName, itemCode);
                        System.out.println("itemTypeCell = " + itemTypeName);
                        break;

                    default:
                        throw new AssertionError();
                }
            }

            // Column C: Bill Prefix (Optional)
            Cell priceCell = row.getCell(4);
            if (priceCell != null && priceCell.getCellType() == CellType.NUMERIC) {
                price = priceCell.getNumericCellValue();
            }

            Cell itemFeeListCell = row.getCell(2);
            if (itemFeeListCell != null && itemFeeListCell.getCellType() == CellType.STRING) {
                feeListType = itemFeeListCell.getStringCellValue();
            }
            if (feeListType != null) {
                category = categoryController.findAndCreateCategoryByName(feeListType);
                if (category.getSymanticType() == SymanticHyrachi.Fee_List_Type) {
                    category.setSymanticType(SymanticHyrachi.Fee_List_Type);
                }
                categoryFacade.edit(category);
            }

            ItemFee fee = new ItemFee();
            fee.setName(itemName);
            fee.setCreatedAt(new Date());
            fee.setCreater(sessionController.getLoggedUser());
            fee.setForInstitution(null);
            fee.setForCategory(category);
            fee.setItem(item);
            fee.setFee(price);
            fee.setInstitution(institution);
            fee.setDepartment(department);
            fee.setFeeType(FeeType.OwnInstitution);
            itemFeeFacade.create(fee);
            System.out.println("fee = " + fee.getId());

        }
        return itemFees;
    }

    private List<ItemFee> readInstitutionItemFeeFromXcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemFees = new ArrayList<>();

        uploadeditemFees = new ArrayList<>();
        rejecteditemFees = new ArrayList<>();

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }
        Institution runningIns = null;
        Department runningDept = null;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Item item = null;
            Institution forInstitution = null;

            String departmentName = null;

            String institutionName = null;
            String itemCode = null;
            String itemName = null;
            String itemTypeName = null;
            Double localFee = 0.0;
            Double foreignerFee = 0.0;
            String forInstitutionName = null;

            ItemLight currentUploadItem = new ItemLight();

            Cell insCell = row.getCell(6);
            if (insCell != null && insCell.getCellType() == CellType.STRING) {
                institutionName = insCell.getStringCellValue();
            }
            if (institutionName == null || institutionName.trim().equals("")) {
                institutionName = "Other";
            }
            if (runningIns == null) {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                runningIns = institution;
            } else if (runningIns.getName().equals(institutionName)) {
                institution = runningIns;
            } else {
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
                runningIns = institution;
            }

            Cell deptCell = row.getCell(7);
            if (deptCell != null && deptCell.getCellType() == CellType.STRING) {
                departmentName = deptCell.getStringCellValue();
            }
            if (departmentName == null || departmentName.trim().equals("")) {
                departmentName = institutionName;
            }
            if (runningDept == null) {
                department = departmentController.findAndSaveDepartmentByName(departmentName);
                runningDept = department;
            } else if (runningDept.getName().equals(departmentName)) {
                department = runningDept;
            } else {
                department = departmentController.getDefaultDepatrment(institution);
                runningDept = department;
            }

            // Column A: Item Code (Required)
            Cell itemCodeCell = row.getCell(0);
            if (itemCodeCell != null && itemCodeCell.getCellType() == CellType.STRING) {
                itemCode = itemCodeCell.getStringCellValue();
            }
            if (itemCode != null) {
            }

            // Column B: Item Name (Required)
            Cell itemNameCell = row.getCell(1);
            if (itemNameCell != null && itemNameCell.getCellType() == CellType.STRING) {
                itemName = itemNameCell.getStringCellValue();
            }

            if (itemCode == null || itemCode.trim().isEmpty()) {
                currentUploadItem.setCode(itemCode);
                currentUploadItem.setName(itemName);
                rejecteditemFees.add(currentUploadItem);
                continue;
            }
            if (itemName == null || itemName.trim().isEmpty()) {
                currentUploadItem.setCode(itemCode);
                currentUploadItem.setName(itemName);
                rejecteditemFees.add(currentUploadItem);
                continue;
            }

            // Column C: Fee Site 
            Cell forInstitutionCell = row.getCell(2);
            if (forInstitutionCell != null && forInstitutionCell.getCellType() == CellType.STRING) {
                forInstitutionName = forInstitutionCell.getStringCellValue();
            }
            if (forInstitutionName != null) {
                forInstitution = institutionController.findAndSaveInstitutionByName(forInstitutionName);
                System.out.println("forInstitution = " + forInstitution);
            }

            // Column D: Item Type 
            Cell itemTypeCell = row.getCell(3);
            if (itemTypeCell != null && itemTypeCell.getCellType() == CellType.STRING) {
                itemTypeName = itemTypeCell.getStringCellValue();
            }

            if (itemTypeName != null) {
                switch (itemTypeName) {
                    case "Investigation":
                        item = itemController.findAndCreateInvestigationByNameAndCode(itemName, itemCode);
                        break;

                    case "Service":
                        item = itemController.findAndCreateServiceByNameAndCode(itemName, itemCode);
                        break;

                    default:
                        item = itemController.findAndCreateInvestigationByNameAndCode(itemName, itemCode);
                }
            }

            // Column E: Local Fee Value
            Cell localFeeCell = row.getCell(4);
            if (localFeeCell != null && localFeeCell.getCellType() == CellType.NUMERIC) {
                localFee = localFeeCell.getNumericCellValue();
            } else {
                currentUploadItem.setCode(itemCode);
                currentUploadItem.setName(itemName);
                rejecteditemFees.add(currentUploadItem);
                continue;
            }

            // Column E: Foreigner Fee Value
            Cell foreignerFeeCell = row.getCell(5);
            if (foreignerFeeCell != null && foreignerFeeCell.getCellType() == CellType.NUMERIC) {
                foreignerFee = foreignerFeeCell.getNumericCellValue();
            } else {
                currentUploadItem.setCode(itemCode);
                currentUploadItem.setName(itemName);
                rejecteditemFees.add(currentUploadItem);
                continue;
            }

            ItemFee fee = new ItemFee();
            fee.setName("Hospital Fee");
            fee.setCreatedAt(new Date());
            fee.setCreater(sessionController.getLoggedUser());
            fee.setForInstitution(forInstitution);
            fee.setForCategory(null);
            fee.setItem(item);
            fee.setFee(localFee);
            fee.setFfee(foreignerFee);
            fee.setInstitution(institution);
            fee.setDepartment(department);
            fee.setFeeType(FeeType.OwnInstitution);
            itemFeeFacade.create(fee);
            System.out.println("Create Fee = " + fee.getId());

            uploadeditemFees.add(fee);

        }
        return itemFees;
    }

    private List<Department> readDepartmentFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Department> departmentList = new ArrayList<>();

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Department department;
            String departmentCode = null;
            String departmentName = null;
            String billPrefix = null;
            String departmentType = null;
            String phone = null;
            String email = null;
            String address = null;
            String institutionName = null;
            Boolean active = null;

            // Column A: Department Code (Required)
            Cell departmentCodeCell = row.getCell(0);
            if (departmentCodeCell != null && departmentCodeCell.getCellType() == CellType.STRING) {
                departmentCode = departmentCodeCell.getStringCellValue();
            }
            if (departmentCode == null || departmentCode.trim().isEmpty()) {
                continue;
            }

            // Column B: Department Name (Required)
            Cell departmentNameCell = row.getCell(1);
            if (departmentNameCell != null && departmentNameCell.getCellType() == CellType.STRING) {
                departmentName = departmentNameCell.getStringCellValue();
            }
            if (departmentName == null || departmentName.trim().isEmpty()) {
                continue;
            }

            // Column C: Bill Prefix (Optional)
            Cell billPrefixCell = row.getCell(2);
            if (billPrefixCell != null && billPrefixCell.getCellType() == CellType.STRING) {
                billPrefix = billPrefixCell.getStringCellValue();
            }

            // Column D: Department Type (Optional)
            Cell departmentTypeCell = row.getCell(3);
            if (departmentTypeCell != null && departmentTypeCell.getCellType() == CellType.STRING) {
                departmentType = departmentTypeCell.getStringCellValue();
            }
            if (departmentType == null || departmentType.trim().isEmpty()) {
                departmentType = "Other";
            }

            // Column E: Phone (Optional)
            Cell phoneCell = row.getCell(4);
            if (phoneCell != null) {
                if (phoneCell.getCellType() == CellType.NUMERIC) {
                    DecimalFormat decimalFormat = new DecimalFormat("#");
                    phone = decimalFormat.format(phoneCell.getNumericCellValue());
                } else if (phoneCell.getCellType() == CellType.STRING) {
                    phone = phoneCell.getStringCellValue();
                }
            }

            // Column F: Email (Optional)
            Cell emailCell = row.getCell(5);
            if (emailCell != null && emailCell.getCellType() == CellType.STRING) {
                email = emailCell.getStringCellValue();
            }

            // Column G: Address (Optional)
            Cell addressCell = row.getCell(6);
            if (addressCell != null && addressCell.getCellType() == CellType.STRING) {
                address = addressCell.getStringCellValue();
            }

            // Column H: Institution (Optional)
            Cell institutionCell = row.getCell(7);
            if (institutionCell != null && institutionCell.getCellType() == CellType.STRING) {
                institutionName = institutionCell.getStringCellValue();
            }
            Institution parentInstitution = institutionController.findExistingInstitutionByName(institutionName);
            if (parentInstitution == null) {
                parentInstitution = sessionController.getInstitution();
            }

            // Column I: Active (True/False)
            Cell activeCell = row.getCell(8);
            if (activeCell != null && activeCell.getCellType() == CellType.BOOLEAN) {
                active = activeCell.getBooleanCellValue();
            }
            if (active == null) {
                active = false;
            }

            department = departmentController.findExistingDepartmentByName(departmentName, parentInstitution);
            if (department == null) {
                department = new Department();
                department.setName(departmentName);
                department.setCode(departmentCode);
                department.setDepartmentCode(billPrefix);
                department.setDepartmentType(departmentController.findDepartmentType(departmentType));
                department.setTelephone1(phone);
                department.setTelephone2(phone);
                department.setEmail(email);
                department.setAddress(address);
                department.setInstitution(parentInstitution);
                department.setActive(active);
                department.setCreatedAt(new Date());
                department.setCreater(sessionController.getLoggedUser());
            }

            departmentList.add(department);
        }

        return departmentList;
    }

    private List<Institution> readCreditCOmpanyFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Institution> CreditCompanyList = new ArrayList<>();
        Institution creditCompany;

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            creditCompany = null;
            String creditCompanyName = null;
            String creditCompanyPrintingName = null;
            String creditCompanyPhone = null;
            String creditCompanyEmail = null;
            String creditCompanyaddress = null;

            //    Item masterItem = itemController.findMasterItemByName(code);
            Cell agentNameCell = row.getCell(1);

            if (agentNameCell != null && agentNameCell.getCellType() == CellType.STRING) {
                creditCompanyName = agentNameCell.getStringCellValue();
            }
            if (creditCompanyName == null || creditCompanyName.trim().equals("")) {
                continue;
            }

            Cell agentPrintingNameCell = row.getCell(2);

            if (agentPrintingNameCell != null && agentPrintingNameCell.getCellType() == CellType.STRING) {
                creditCompanyPrintingName = agentPrintingNameCell.getStringCellValue();

            }
            if (creditCompanyPrintingName == null || creditCompanyPrintingName.trim().equals("")) {
                creditCompanyPrintingName = creditCompanyPrintingName;
            }

            Cell contactNumberCell = row.getCell(3);

            if (contactNumberCell != null) {
                if (contactNumberCell.getCellType() == CellType.NUMERIC) {
                    DecimalFormat decimalFormat = new DecimalFormat("#");
                    creditCompanyPhone = decimalFormat.format(contactNumberCell.getNumericCellValue());

                } else if (contactNumberCell.getCellType() == CellType.STRING) {
                    creditCompanyPhone = contactNumberCell.getStringCellValue();
                }
            }
            if (creditCompanyPhone == null || creditCompanyPhone.trim().equals("")) {
                creditCompanyPhone = null;
            }

            Cell emailAddressCell = row.getCell(4);

            if (emailAddressCell != null && emailAddressCell.getCellType() == CellType.STRING) {
                creditCompanyEmail = emailAddressCell.getStringCellValue();

            }
            if (creditCompanyEmail == null || creditCompanyEmail.trim().equals("")) {
                creditCompanyEmail = null;
            }

            Cell addressCell = row.getCell(5);

            if (addressCell != null && addressCell.getCellType() == CellType.STRING) {
                creditCompanyaddress = addressCell.getStringCellValue();
            }
            if (creditCompanyaddress == null || creditCompanyaddress.trim().equals("")) {
                creditCompanyaddress = null;
            }

            if (creditCompanyName.trim().equals("")) {
                continue;
            }

            creditCompany = creditCompanyController.findCreditCompanyByName(creditCompanyName);

            if (creditCompany == null) {
                creditCompany = new Institution();
            }
//            collectingCentre = new Institution();
            creditCompany.setInstitutionType(InstitutionType.CollectingCentre);
            creditCompany.setName(creditCompanyName);
            creditCompany.setChequePrintingName(creditCompanyPrintingName);
            creditCompany.setPhone(creditCompanyPhone);
            creditCompany.setEmail(creditCompanyEmail);
            creditCompany.setAddress(creditCompanyaddress);
            creditCompany.setInstitutionType(InstitutionType.CreditCompany);
            creditCompanyController.save(creditCompany);
            CreditCompanyList.add(creditCompany);
        }

        return CreditCompanyList;
    }

    public void uploadItemFeesToUpdateFees() {
        itemFees = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                itemFees = replaceItemFeesFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public StreamedContent getTemplateForDepartmentUpload() {
        try {
            createTemplateForDepartmentUpload();
        } catch (IOException e) {
            JsfUtil.addErrorMessage("Error creating department upload template: " + e.getMessage());
            return null;
        }
        return templateForDepartmentUpload;
    }

    public void createTemplateForDepartmentUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Department Code", "Department Name", "Bill Prefix", "Department Type", "Phone", "Email", "Address", "Institution", "Active"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForDepartmentUpload = DefaultStreamedContent.builder()
                .name("template_for_Department_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public StreamedContent getTemplateForItemFeeUpload() {
        try {
            createTemplateForItemFeeUpload();
        } catch (IOException e) {
            JsfUtil.addErrorMessage("Error creating Item Fee template: " + e.getMessage());
            return null;
        }
        return templateForItemFeeUpload;
    }

    public void createTemplateForItemFeeUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Item Fee ID", "Item Name", "Institution", "Department", "Staff", "Fee Type", "Fee", "Fee for foreigner"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForItemFeeUpload = DefaultStreamedContent.builder()
                .name("template_for_Item_Fee_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void uploadCollectingCentrePriceList() {
        itemFees = new ArrayList<>();
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                itemFees = readCollectingCentrePriceListFromXcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadInvestigations() {
        List<Investigation> investigations;
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                readInvestigationsFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadToFixItemCategoriesByCode() {
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                fixItemCategoriesFromCode(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadDiagnoses() {
        if (file != null) {
            try {
                InputStream inputStream = file.getInputStream();
                readDiagnosesFromExcel(inputStream);

            } catch (IOException ex) {
                Logger.getLogger(DataUploadController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void uploadVmps() {
        outputString += "uploadVmps\n";
        List<Vmp> vmps;
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                vmps = readVmpsFromExcel(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<PatientEncounter> readVisitDataFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<PatientEncounter> visits = new ArrayList<>();

        List<String> datePatterns = Arrays.asList("dd MMMM yyyy", "dd/MMM/yyyy hh:mm", "M/d/yy", "MM/dd/yyyy", "d/M/yy", "dd/MM,yyyy", "dd/MM/yyyy", "yyyy-MM-dd");

        Long visitID = 0l;
        Long patientID = 0l;
        Date completedTime = null;
        String Comments = "";
        Double visitWeight = null;
        Long sbp = null;
        Long dbp = null;
        Double bmi = null;
        Long pr = null;

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        // Use a DataFormatter to read cell values, as date cells are typically formatted as strings
        DataFormatter dataFormatter = new DataFormatter();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

//...
            Cell visitIdCell = row.getCell(0);
            if (visitIdCell != null && visitIdCell.getCellType() == CellType.NUMERIC) {
                visitID = (long) Math.round(visitIdCell.getNumericCellValue());
            }

            Cell patientIdCell = row.getCell(1);
            if (patientIdCell != null && patientIdCell.getCellType() == CellType.NUMERIC) {
                patientID = (long) Math.round(patientIdCell.getNumericCellValue());
            }

            Cell completedTimeCell = row.getCell(2);
            if (completedTimeCell != null) {
                if (completedTimeCell.getCellType() == CellType.STRING) {
                    String dateOfBirthStr = dataFormatter.formatCellValue(completedTimeCell);
                    LocalDate localDateOfBirth = parseDate(dateOfBirthStr, datePatterns);
                    Instant instant = localDateOfBirth.atStartOfDay(ZoneId.systemDefault()).toInstant();
                    completedTime = Date.from(instant);
                } else if (completedTimeCell.getCellType() == CellType.NUMERIC) {
                    if (DateUtil.isCellDateFormatted(completedTimeCell)) {
                        completedTime = completedTimeCell.getDateCellValue();
                    } else {
                    }
                }
            }

            Cell commentsCell = row.getCell(3);
            if (commentsCell != null && commentsCell.getCellType() == CellType.STRING) {
                Comments = commentsCell.getStringCellValue();
            }

            Cell visitWeightCell = row.getCell(4);
            if (visitWeightCell != null && visitWeightCell.getCellType() == CellType.NUMERIC) {
                visitWeight = visitWeightCell.getNumericCellValue();
            }

            Cell sbpCell = row.getCell(5);
            if (sbpCell != null && sbpCell.getCellType() == CellType.NUMERIC) {
                sbp = (long) Math.round(sbpCell.getNumericCellValue());
            }

            Cell dbpCell = row.getCell(6);
            if (dbpCell != null && dbpCell.getCellType() == CellType.NUMERIC) {
                dbp = (long) Math.round(dbpCell.getNumericCellValue());
            }

            Cell bmiCell = row.getCell(7);
            if (bmiCell != null && bmiCell.getCellType() == CellType.NUMERIC) {
                bmi = bmiCell.getNumericCellValue();
            }

            Cell prCell = row.getCell(8);
            if (prCell != null && prCell.getCellType() == CellType.NUMERIC) {
                pr = (long) Math.round(prCell.getNumericCellValue());
            }

            PatientEncounter visit = new PatientEncounter();
            visit.setEncounterType(EncounterType.Opd);
            visit.setEncounterId(visitID);
            Patient patient = patientController.findPatientByPatientId(patientID);
            if (patient == null) {
                continue;
            }
            visit.setPatient(patient);
            visit.setCreatedAt(new Date());
            visit.setEncounterDate(completedTime);
            visit.setEncounterDateTime(completedTime);
            visit.setDepartment(sessionController.getDepartment());
            visit.setInstitution(sessionController.getInstitution());
            visit.setComments(Comments);
            visit.setSbp(sbp);
            visit.setDbp(dbp);
            visit.setBmi(bmi);
            visit.setPr(pr);
            visit.setVisitWeight(visitWeight);
            patientEncounterController.saveEncounter(visit);
            visits.add(visit);
        }
        return visits;
    }

    private List<Patient> readPatientDataFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Patient> patients = new ArrayList<>();

        List<String> datePatterns = Arrays.asList("dd MMMM yyyy", "M/d/yy", "MM/dd/yyyy", "d/M/yy", "dd/MM,yyyy", "dd/MM/yyyy", "yyyy-MM-dd");

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        // Use a DataFormatter to read cell values, as date cells are typically formatted as strings
        DataFormatter dataFormatter = new DataFormatter();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Patient patient = new Patient();

//            patient.setPatientId((long) row.getCell(0).getNumericCellValue());
//            patient.getPerson().setName(row.getCell(1).getStringCellValue());
            Cell idCell = row.getCell(0);
            if (idCell != null) {
                String idStr;
                Long id = null;
                if (idCell.getCellType() == CellType.STRING) {
                    idStr = idCell.getStringCellValue();

                    id = CommonFunctions.convertStringToLongOrNull(idStr);

                } else if (idCell.getCellType() == CellType.NUMERIC) {
                    Double tmp;
                    tmp = idCell.getNumericCellValue();
                    id = CommonFunctions.convertDoubleToLong(tmp);

                }
                if (id != null) {
                    patient.setId(id);
                }
            }

            String name = null;
            Cell nameCell = row.getCell(1);
            if (nameCell != null) {

                if (nameCell.getCellType() == CellType.STRING) {
                    name = nameCell.getStringCellValue();

                }
                if (name != null) {
                    patient.getPerson().setName(name);
                }
            }

            if (name == null || name.trim().isEmpty()) {
                continue;
            }

            Cell codeCell = row.getCell(2);
            if (codeCell != null) {
                String code = null;
                if (codeCell.getCellType() == CellType.STRING) {
                    code = codeCell.getStringCellValue();

                } else if (codeCell.getCellType() == CellType.NUMERIC) {
                    Double tmp;
                    tmp = codeCell.getNumericCellValue();
                    code = CommonFunctions.convertDoubleToString(tmp);
                }
                if (code != null) {
                    patient.setCode(code);
                }
            }

            Cell dateOfBirthCell = row.getCell(3);
            if (dateOfBirthCell != null) {
                Date dob = CommonFunctions.convertDateToDbType(dateOfBirthCell.getStringCellValue());
                patient.getPerson().setDob(dob);
            }

//            Cell dateOfBirthCell = row.getCell(3);
//            if (dateOfBirthCell != null) {
//                String dateOfBirthStr = dataFormatter.formatCellValue(dateOfBirthCell);
//                LocalDate localDateOfBirth = parseDate(dateOfBirthStr, datePatterns);
//                if (localDateOfBirth != null) {
//                    Instant instant = localDateOfBirth.atStartOfDay(ZoneId.systemDefault()).toInstant();
//                    Date dateOfBirth = Date.from(instant);
//                    patient.getPerson().setDob(dateOfBirth);
//                }
//            }
            Cell addressCell = row.getCell(4);
            if (addressCell != null) {
                patient.getPerson().setAddress(addressCell.getStringCellValue());
            }
            String phone = null;
            Long phoneLong = null;

            Cell phoneCell = row.getCell(5);

            if (phoneCell != null) {
                switch (phoneCell.getCellType()) {
                    case STRING:
                        phone = phoneCell.getStringCellValue();
                        phoneLong = CommonFunctions.convertStringToLongByRemoveSpecialChars(phone);
                        break;
                    case NUMERIC:
                        // Assuming the phone number is a whole number
                        Double tmpDblPhone = phoneCell.getNumericCellValue();
                        phoneLong = CommonFunctions.convertDoubleToLong(tmpDblPhone);
                        // Convert the numeric value to String and add leading '0'
                        phone = "0" + phoneLong.toString();
                        break;
                    default:
                        // Handle other cell types if needed
                        break;
                }
                patient.getPerson().setPhone(phone);
                patient.setPatientPhoneNumber(phoneLong);
            }

            Cell mobileCell = row.getCell(6);
            if (mobileCell != null) {
                String mobile = null;
                if (mobileCell.getCellType() == CellType.STRING) {
                    mobile = mobileCell.getStringCellValue();
                } else if (mobileCell.getCellType() == CellType.NUMERIC) {
                    Double mobileLong;
                    mobileLong = mobileCell.getNumericCellValue();
                    Long mobileNumber = CommonFunctions.convertDoubleToLong(mobileLong);
                    mobile = "0" + String.valueOf(mobileNumber);
                }
                patient.getPerson().setMobile(mobile);
            }

            Cell emailCell = row.getCell(7);
            if (emailCell != null) {
                patient.getPerson().setEmail(emailCell.getStringCellValue());
            }

            Title title;
            try {
                Cell titleCell = row.getCell(8);
                if (titleCell != null) {
                    title = Title.valueOf(titleCell.getStringCellValue());
                } else {
                    title = Title.Mr;
                }
            } catch (IllegalArgumentException e) {
                title = Title.Mr;
            }
            patient.getPerson().setTitle(title);

            Cell sexCell = row.getCell(9);
            if (sexCell != null) {
                String sex = sexCell.getStringCellValue();
                if (sex.toLowerCase().contains("f")) {
                    patient.getPerson().setSex(Sex.Female);
                } else {
                    patient.getPerson().setSex(Sex.Male);
                }
            }

            Cell civilStatusCell = row.getCell(10);
            if (civilStatusCell != null) {
                String strCivilStatus = civilStatusCell.getStringCellValue();
                Item civilStatus = itemController.findItemByName(strCivilStatus, "civil_statuses");
                patient.getPerson().setCivilStatus(civilStatus);
            }

            Cell raceCell = row.getCell(11);
            if (raceCell != null) {
                String strRace = raceCell.getStringCellValue();
                Item race = itemController.findItemByName(strRace, "races");
                patient.getPerson().setRace(race);
            }

            Cell bloodGroupCell = row.getCell(12);
            if (bloodGroupCell != null) {
                String strBloodGroup = bloodGroupCell.getStringCellValue();
                Item bloodGroup = itemController.findItemByName(strBloodGroup, "blood_groups");
                patient.getPerson().setBloodGroup(bloodGroup);
            }

            Cell commentsCell = row.getCell(13);
            if (commentsCell != null) {
                patient.setComments(commentsCell.getStringCellValue());
            }

            Cell fullNameCell = row.getCell(14);
            if (fullNameCell != null) {
                patient.getPerson().setFullName(fullNameCell.getStringCellValue());
            }

            Cell occupationCell = row.getCell(15);
            if (occupationCell != null) {
                String strOccupation = occupationCell.getStringCellValue();
                Item occupation = itemController.findItemByName(strOccupation, "occupations");
                patient.getPerson().setOccupation(occupation);
            }

            patient.setCreatedAt(new Date());
            patient.setCreater(sessionController.getLoggedUser());
            patient.setCreatedInstitution(sessionController.getInstitution());

            Cell AreaCell = row.getCell(16);
            if (AreaCell != null) {
                String strArea = AreaCell.getStringCellValue();
                Area area = areaController.findAreaByName(strArea);
                if (area == null) {
                    Area areanew = new Area();
                    areanew.setCreatedAt(new Date());
                    areanew.setCreater(sessionController.getLoggedUser());
                    areanew.setName(strArea);
                    areaController.save(areanew);
                    patient.getPerson().setArea(areanew);
                }
                patient.getPerson().setArea(area);
            }

            patient.setCreatedAt(new Date());
            patient.setCreater(sessionController.getLoggedUser());
            patient.setCreatedInstitution(sessionController.getInstitution());

            patients.add(patient);

        }

        return patients;
    }

    private List<Vtm> readVtmsFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Vtm> vtms = new ArrayList<>();

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Vtm vtm = new Vtm();

            Cell idCell = row.getCell(0);
            if (idCell != null) {
                vtm.setItemId((long) idCell.getNumericCellValue());
            }
            Cell nameCell = row.getCell(1);
            if (nameCell != null) {
                vtm.setName(nameCell.getStringCellValue());
                vtm.setCode(CommonFunctions.nameToCode("vtm_" + vtm.getName()));
            }
            vtm.setCreatedAt(new Date());
            vtm.setCreater(sessionController.getLoggedUser());
            vtms.add(vtm);

        }

        return vtms;
    }

    private List<Atm> readAtmsFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        outputString = "readAtmsFromExcel\n";
        List<Atm> atms = new ArrayList<>();

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Atm atm = new Atm();

            Cell idCell = row.getCell(0);
            if (idCell != null) {
                atm.setItemId((long) idCell.getNumericCellValue());
            }
            Cell nameCell = row.getCell(1);
            if (nameCell != null) {
                atm.setName(nameCell.getStringCellValue());
                atm.setCode(CommonFunctions.nameToCode("atm_" + atm.getName()));
            }
            Cell vtmCell = row.getCell(2);
            if (vtmCell != null) {
                String vtmName = vtmCell.getStringCellValue();
                outputString += vtmName + "\n";
                Vtm vtm = vtmController.findAndSaveVtmByName(vtmName);
                outputString += vtm + "\n";
                atm.setVtm(vtm);
            }
            atm.setCreatedAt(new Date());
            atms.add(atm);
            atmController.saveAtm(atm);
        }

        return atms;
    }

    private List<ClinicalEntity> readDiagnosesFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<ClinicalEntity> dxx = new ArrayList<>();

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            ClinicalEntity dx = new ClinicalEntity();

            /**
             * ItemID tblItem_Item ItemCode tradename_Item generic_Item Category
             * strength unit issue unit pack unit StrengthUnitsPerIssueUnit
             * IssueUnitsPerPack	ROL	ROQ	MinROQ	manu_Institution
             * importer_Institution	supplier_Institution	SalePrice	PurchasePrice
             * LastSalePrice	LastPurchasePrice	MinIQty	MaxIQty	DivQty
             */
            Long id;
            String dxName = null;

            Cell idCell = row.getCell(0);
            if (idCell != null && idCell.getCellType() == CellType.NUMERIC) {
                id = (long) idCell.getNumericCellValue();
                dx.setItemId(id);
            }

            Cell nameCell = row.getCell(1);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                dxName = nameCell.getStringCellValue();
                if (dxName == null || dxName.trim().equals("")) {
                    continue;
                }
                dx = diagnosisController.findAndSaveDiagnosis(dxName);
                if (dx != null) {
                    continue;
                }
            } else {
                continue;
            }

            dxx.add(dx);
        }

        return dxx;
    }

    private List<ItemFee> replaceItemFeesFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        itemFees = new ArrayList<>();
        ItemFee itemFee;

//        // Assuming the first row contains headers, skip it
//        if (rowIterator.hasNext()) {
//            rowIterator.next();
//        }
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            itemFee = null;
            Long itemFeeId = null;
            Double feeValue = null;
            Double foreignerFeeValue = null;

            Cell itemFeeIdCell = row.getCell(0);
            if (itemFeeIdCell != null && itemFeeIdCell.getCellType() == CellType.NUMERIC) {
                double numericValue = itemFeeIdCell.getNumericCellValue();
                if (numericValue % 1 == 0) {
                    itemFeeId = (long) numericValue;
                } else {
                    continue;
                }
            } else {
                continue;
            }

            itemFee = itemFeeController.findItemFeeFromItemFeeId(itemFeeId);

            if (itemFee == null) {
                continue;
            }

            Cell feeCell = row.getCell(6);
            if (feeCell != null && feeCell.getCellType() == CellType.NUMERIC) {
                feeValue = feeCell.getNumericCellValue();
            }

            if (feeValue == null) {
                continue;
            }

            Cell ffeeCell = row.getCell(7);
            if (ffeeCell != null && ffeeCell.getCellType() == CellType.NUMERIC) {
                foreignerFeeValue = ffeeCell.getNumericCellValue();
            }

            if (foreignerFeeValue == null) {
                foreignerFeeValue = feeValue;
            }

            itemFee.setFee(feeValue);
            itemFee.setFfee(foreignerFeeValue);

            itemFeeManager.saveItemFee(itemFee);

            itemFees.add(itemFee);
        }

        return itemFees;
    }

    private List<Item> fixItemCategoriesFromCode(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        List<Item> changedItems = new ArrayList<>();
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Category cat = null;
            String code = null;
            String categoryName = null;

            Cell codeCell = row.getCell(0);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }
            if (code == null || code.trim().equals("")) {
                continue;
            }
            Item item = itemController.findItemByCode(code);
            if (item == null) {
                continue;
            }
            Cell categoryCell = row.getCell(2);
            if (categoryCell != null && categoryCell.getCellType() == CellType.STRING) {
                categoryName = categoryCell.getStringCellValue();
            }
            if (categoryName == null || categoryName.trim().equals("")) {
                continue;
            }
            cat = categoryController.findAndCreateCategoryByName(categoryName);
            if (cat == null) {
                continue;
            }
            item.setCategory(cat);
            itemFacade.edit(item);
            changedItems.add(item);
        }

        return changedItems;
    }

    private List<Investigation> readInvestigationsFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Investigation> investigations = new ArrayList<>();

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Category cat = null;
            Institution institution = null;
            Department department = null;
            InwardChargeType iwct = null;
            Sample sample = null;
            InvestigationTube investigationTube = null;
            Machine analyser = null;

            String name = null;
            String printingName = null;
            String fullName = null;
            String code = null;
            String categoryName = null;
            String institutionName = null;
            String departmentName = null;
            String inwardName = null;
            String sampleName = null;
            String containerName = null;
            String analyserName = null;

            Cell nameCell = row.getCell(0);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                name = nameCell.getStringCellValue();
                if (name == null || name.trim().equals("")) {
                    continue;
                }

            }

            Cell printingNameCell = row.getCell(1);
            if (printingNameCell != null && printingNameCell.getCellType() == CellType.STRING) {
                printingName = printingNameCell.getStringCellValue();

            }
            if (printingName == null || printingName.trim().equals("")) {
                printingName = name;
            }

            Cell fullNameCell = row.getCell(2);
            if (fullNameCell != null && fullNameCell.getCellType() == CellType.STRING) {
                fullName = fullNameCell.getStringCellValue();
            }
            if (fullName == null || fullName.trim().equals("")) {
                fullName = name;
            }

            Cell codeCell = row.getCell(3);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }
            if (code == null || code.trim().equals("")) {
                continue;
            }
            Cell categoryCell = row.getCell(4);
            if (categoryCell != null && categoryCell.getCellType() == CellType.STRING) {
                categoryName = categoryCell.getStringCellValue();
                cat = investigationCategoryController.findAndCreateCategoryByName(categoryName);
            }
            if (categoryName == null || categoryName.trim().equals("")) {
                continue;
            }

            Cell insCell = row.getCell(5);
            if (insCell != null && insCell.getCellType() == CellType.STRING) {
                institutionName = insCell.getStringCellValue();
                institution = institutionController.findAndSaveInstitutionByName(institutionName);
            }

            Cell deptCell = row.getCell(6);
            if (deptCell != null && deptCell.getCellType() == CellType.STRING) {
                departmentName = deptCell.getStringCellValue();
            }
            if (departmentName != null && !departmentName.trim().equals("")) {
                department = departmentController.findAndSaveDepartmentByName(departmentName);
            }

            Cell inwardCcCell = row.getCell(7);
            if (inwardCcCell != null && inwardCcCell.getCellType() == CellType.STRING) {
                inwardName = inwardCcCell.getStringCellValue();
            }
            if (inwardName != null && !inwardName.trim().equals("")) {
                iwct = enumController.getInaChargeType(inwardName);
            }
            if (iwct == null) {
                iwct = InwardChargeType.OtherCharges;
            }

            Cell sampleCell = row.getCell(8);
            if (sampleCell != null && sampleCell.getCellType() == CellType.STRING) {
                sampleName = sampleCell.getStringCellValue();
                sample = sampleController.findAndCreateSampleByName(sampleName);
            }

            if (sampleName == null || sampleName.trim().equals("")) {
                continue;
            }

            Cell containerCell = row.getCell(9);
            if (containerCell != null && containerCell.getCellType() == CellType.STRING) {
                containerName = containerCell.getStringCellValue();
                investigationTube = investigationTubeController.findAndCreateInvestigationTubeByName(containerName);
            }

            if (containerName == null || containerName.trim().equals("")) {
                continue;
            }

            Cell analyserCell = row.getCell(10);
            if (analyserCell != null && analyserCell.getCellType() == CellType.STRING) {
                analyserName = analyserCell.getStringCellValue();
                analyser = machineController.findAndCreateAnalyserByName(analyserName);
            }

            if (analyserName == null || analyserName.trim().equals("")) {
                continue;
            }

            Investigation investigation = new Investigation();
            investigation.setName(name);
            investigation.setPrintName(printingName);
            investigation.setFullName(fullName);
            investigation.setCode(code);
            investigation.setCategory(cat);
            investigation.setInstitution(institution);
            investigation.setDepartment(department);
            investigation.setInwardChargeType(iwct);
            investigation.setSample(sample);
            investigation.setInvestigationTube(investigationTube);
            investigation.setMachine(analyser);

            investigation.setCreater(sessionController.getLoggedUser());
            investigation.setCreatedAt(new Date());
            investigationController.save(investigation);
            investigations.add(investigation);

        }

        return investigations;
    }

    private List<Amp> readAmpsFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Amp> amps = new ArrayList<>();

        // Assuming the first row contains headers, skip it
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Amp amp = new Amp();
            Atm atm = null;
            Vmp vmp = null;

            Long id;
            String ampName;
            String code = null;
            String barcode = null;
            String vmpName;
            String manufacturerName = null;
            String importerName = null;
            Institution importer;
            Institution manufacturer;

            Cell idCell = row.getCell(0);
            if (idCell != null && idCell.getCellType() == CellType.NUMERIC) {
                id = (long) idCell.getNumericCellValue();
                amp.setItemId(id);
            }

            Cell nameCell = row.getCell(1);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                ampName = nameCell.getStringCellValue();
                if (ampName == null || ampName.trim().equals("")) {
                    continue;
                }
                amp = ampController.findAmpByName(ampName);
                if (amp == null) {
                    amp = new Amp();
                    amp.setName(ampName);
                    amp.setCreatedAt(new Date());
                    amp.setCreater(sessionController.getLoggedUser());
                }
            } else {
                continue;
            }

            Cell codeCell = row.getCell(2);
            if (codeCell != null && codeCell.getCellType() == CellType.STRING) {
                code = codeCell.getStringCellValue();
            }

            Cell barcodeCell = row.getCell(3);
            if (barcodeCell != null && barcodeCell.getCellType() == CellType.STRING) {
                barcode = barcodeCell.getStringCellValue();
            }

            Cell vmpCell = row.getCell(4);
            if (vmpCell != null && vmpCell.getCellType() == CellType.STRING) {
                vmpName = vmpCell.getStringCellValue();
                vmp = vmpController.findVmpByName(vmpName);
                if (vmp == null) {
                    continue;
                }
            } else {
            }

            Cell manufacturerCell = row.getCell(5);
            if (manufacturerCell != null && manufacturerCell.getCellType() == CellType.STRING) {
                manufacturerName = manufacturerCell.getStringCellValue();
                manufacturer = institutionController.findAndSaveInstitutionByName(manufacturerName);
            }

            Cell importerCell = row.getCell(6);
            if (importerCell != null && importerCell.getCellType() == CellType.STRING) {
                importerName = importerCell.getStringCellValue();
                importer = institutionController.findAndSaveInstitutionByName(importerName);
            }

            manufacturer = institutionController.getInstitutionByName(manufacturerName, InstitutionType.Manufacturer);
            importer = institutionController.getInstitutionByName(importerName, InstitutionType.Importer);

//            amp = new Amp();
            amp.setName(ampName);
            amp.setCode("amp_" + CommonFunctions.nameToCode(ampName));

            if (vmp != null) {
                amp.setVmp(vmp);
                amp.setCategory(vmp.getCategory());
            }
            amp.setCode(code);
            amp.setBarcode(barcode);
            if (manufacturer != null) {
                amp.setManufacturer(manufacturer);
            }
            if (importer != null) {
                amp.setImporter(importer);
            }

            amp.setCreater(sessionController.getLoggedUser());

            ampController.saveAmp(amp);

            amps.add(amp);

        }

        return amps;
    }

    private List<Vmp> readVmpsFromExcel(InputStream inputStream) throws IOException {
        outputString += "readVmpsFromExcel";
        StringBuilder output = new StringBuilder();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        List<Vmp> vmps = new ArrayList<>();

        if (rowIterator.hasNext()) {
            rowIterator.next(); // Skip header row
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Vmp vmp = new Vmp();
            Vtm vtm = null;
            Category dosageForm = null;
            MeasurementUnit strengthUnit = null;
            MeasurementUnit issueUnit = null;
            Long id;
            String vmpName = "";
            String vtmName;
            String dosageFormName;
            String strengthUnitName;
            String issueUnitName;
            Double strengthUnitsPerIssueUnit = null;
            Double minimumIssueQuantity = null;
            Double issueMultipliesQuantity = null;

            // Read ID (Optional)
            Cell idCell = row.getCell(0);
            if (idCell != null && idCell.getCellType() == CellType.NUMERIC) {
                id = (long) idCell.getNumericCellValue();
                vmp.setItemId(id);
            }

            // Read VMP Name
            Cell nameCell = row.getCell(1);
            if (nameCell != null && nameCell.getCellType() == CellType.STRING) {
                vmpName = nameCell.getStringCellValue();
                if (vmpName != null && !vmpName.trim().isEmpty()) {
                    vmp = vmpController.findVmpByName(vmpName);
                    if (vmp != null) {
                        vmps.add(vmp);
                        continue;
                    }
                } else {
                    continue;
                }
            }

            // Read VTM Name
            Cell vtmCell = row.getCell(2);
            if (vtmCell != null && vtmCell.getCellType() == CellType.STRING) {
                vtmName = vtmCell.getStringCellValue();
                vtm = vtmController.findAndSaveVtmByName(vtmName);
            }

            // Read Dosage Form
            Cell dosageFormCell = row.getCell(3);
            if (dosageFormCell != null && dosageFormCell.getCellType() == CellType.STRING) {
                dosageFormName = dosageFormCell.getStringCellValue();
                dosageForm = categoryController.findAndCreateCategoryByName(dosageFormName);
            }

            // Read Strength Unit
            Cell strengthUnitCell = row.getCell(4);
            if (strengthUnitCell != null && strengthUnitCell.getCellType() == CellType.STRING) {
                strengthUnitName = strengthUnitCell.getStringCellValue();
                strengthUnit = measurementUnitController.findAndSaveMeasurementUnitByName(strengthUnitName);
            }

            // Read Issue Unit
            Cell issueUnitCell = row.getCell(5);
            if (issueUnitCell != null && issueUnitCell.getCellType() == CellType.STRING) {
                issueUnitName = issueUnitCell.getStringCellValue();
                issueUnit = measurementUnitController.findAndSaveMeasurementUnitByName(issueUnitName);
            }

            // Read Strength Units per Issue Unit
            Cell strengthUnitsPerIssueUnitCell = row.getCell(6);
            if (strengthUnitsPerIssueUnitCell != null && strengthUnitsPerIssueUnitCell.getCellType() == CellType.NUMERIC) {
                strengthUnitsPerIssueUnit = strengthUnitsPerIssueUnitCell.getNumericCellValue();
            }

            // Read Min Issue Quantity
            Cell minIQtyCell = row.getCell(7);
            if (minIQtyCell != null && minIQtyCell.getCellType() == CellType.NUMERIC) {
                minimumIssueQuantity = minIQtyCell.getNumericCellValue();
            }

            // Read Issue Multiplies Quantity
            Cell issueMultipliesQuentityCell = row.getCell(8);
            if (issueMultipliesQuentityCell != null && issueMultipliesQuentityCell.getCellType() == CellType.NUMERIC) {
                issueMultipliesQuantity = issueMultipliesQuentityCell.getNumericCellValue();
            }

            // Check for null values and create VMP
            if (vtm != null && dosageForm != null && strengthUnitsPerIssueUnit != null && strengthUnit != null
                    && minimumIssueQuantity != null && issueMultipliesQuantity != null) {
                vmp = vmpController.createVmp(vmpName,
                        vtm,
                        dosageForm,
                        strengthUnit,
                        issueUnit,
                        strengthUnitsPerIssueUnit,
                        minimumIssueQuantity, issueMultipliesQuantity);
                vmps.add(vmp);
            }
        }

        outputString = output.toString();
        return vmps;
    }

    private LocalDate parseDate(String dateString, List<String> patterns) {
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                // Ignore exception and try the next pattern
            }
        }
        throw new IllegalArgumentException("No matching date format found for: " + dateString);
    }

    public StreamedContent getTemplateForItemWithFeeUpload() {
        try {
            createTemplateForItemWithFeeUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForItemWithFeeUpload;
    }

    public void createTemplateForInvestigationUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Hiding the institution sheet
//        workbook.setSheetHidden(workbook.getSheetIndex("Institutions"), true);
        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Name", "Printing Name", "Full Name", "Item Code", "Category", "Institution", "Department", "Inward Charge Category", "Sample", "Container", "Analyser"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForInvestigationUpload = DefaultStreamedContent.builder()
                .name("template_for_Investigation_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void createTemplateForVisitUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Hiding the institution sheet
//        workbook.setSheetHidden(workbook.getSheetIndex("Institutions"), true);
        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Visit ID", "Patient ID", "Date/Time", "Comments", "Weight", "SBP", "DBP", "Email", "BMI", "PR"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForVisitUpload = DefaultStreamedContent.builder()
                .name("template_for_Visit_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void createTemplateForDiagnosisUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Hiding the institution sheet
//        workbook.setSheetHidden(workbook.getSheetIndex("Institutions"), true);
        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"ID", "Diagnosis"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForDiagnosisUpload = DefaultStreamedContent.builder()
                .name("template_for_Diagnosis_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void createTemplateForVtmUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Hiding the institution sheet
//        workbook.setSheetHidden(workbook.getSheetIndex("Institutions"), true);
        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"ID", "Vtm Name"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForVtmUpload = DefaultStreamedContent.builder()
                .name("template_for_Vtm_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void createTemplateForAtmUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Hiding the institution sheet
//        workbook.setSheetHidden(workbook.getSheetIndex("Institutions"), true);
        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"ID", "Atm Name", "Vtm Name"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForAtmUpload = DefaultStreamedContent.builder()
                .name("template_for_Atm_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void createTemplateForVmpUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Hiding the institution sheet
//        workbook.setSheetHidden(workbook.getSheetIndex("Institutions"), true);
        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"ID", "Vmp Name", "Vtm Name", "Dosage Form", "Dosage Unit", "Strngth Unit", "Issue Unit", "Strength Units per Issue Unit", "Min Issue Quantity", "Issue Multiplies Quantity"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForVmpUpload = DefaultStreamedContent.builder()
                .name("template_for_Vmp_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void createTemplateForAmpUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Hiding the institution sheet
//        workbook.setSheetHidden(workbook.getSheetIndex("Institutions"), true);
        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"ID", "Amp Name", "Code", "Bar code", "Vmp Name", "Manufacturer", "Importer"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForAmpUpload = DefaultStreamedContent.builder()
                .name("template_for_Amp_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void createTemplateForAmpMinimalUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Hiding the institution sheet
//        workbook.setSheetHidden(workbook.getSheetIndex("Institutions"), true);
        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Category", "Vmp Name", "Amp Name", "Code", "Bar code"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForAmpMinimalUpload = DefaultStreamedContent.builder()
                .name("template_for_Amp_Minimal_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void createTemplateForPatientUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Hiding the institution sheet
//        workbook.setSheetHidden(workbook.getSheetIndex("Institutions"), true);
        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Patient ID", "Patient Name", "Patient Code", "Date of Birth", "Address", "Telephone", "Mobile", "Email", "Title", "Sex", "Civil Status", "Race", "Blood Group", "Comments", "Full Name", "Occupation", "Area"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForPatientUpload = DefaultStreamedContent.builder()
                .name("template_for_Patient_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public StreamedContent getTemplateForInvestigationUpload() {
        try {
            createTemplateForInvestigationUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForInvestigationUpload;
    }

    public StreamedContent getTemplateForVisitUpload() {
        try {
            createTemplateForVisitUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForVisitUpload;
    }

    public StreamedContent getTemplateForVtmUpload() {
        try {
            createTemplateForVtmUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForVtmUpload;
    }

    public StreamedContent getTemplateForAmpUpload() {
        try {
            createTemplateForAmpUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForAmpUpload;
    }

    public StreamedContent getTemplateForAtmUpload() {
        try {
            createTemplateForAtmUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForAtmUpload;
    }

    public StreamedContent getTemplateForAmpMinimalUpload() {
        try {
            createTemplateForAmpMinimalUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForAmpMinimalUpload;
    }

    public StreamedContent getTemplateForVmpUpload() {
        try {
            createTemplateForVmpUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForVmpUpload;
    }

    public StreamedContent getTemplateForPatientUpload() {
        try {
            createTemplateForPatientUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForPatientUpload;
    }

    public StreamedContent getTemplateForDiagnosisUpload() {
        try {
            createTemplateForDiagnosisUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForDiagnosisUpload;
    }

    public void createTemplateForItemWithFeeUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Creating the second sheet for institution list
        XSSFSheet institutionSheet = workbook.createSheet("Institutions");

        // Fetching institutions and adding to the institution sheet
        List<Institution> institutions = institutionController.getItems();
        for (int i = 0; i < institutions.size(); i++) {
            Row row = institutionSheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(institutions.get(i).getName());
        }

        // Hiding the institution sheet
//        workbook.setSheetHidden(workbook.getSheetIndex("Institutions"), true);
        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Name", "Printing Name", "Full Name", "Code", "Category", "Institution", "Department", "Inward Charge Type", "Item Type", "Hospital Fee", "Collecting Centre Fee"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Setting data validation for institution column in data sheet
        XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(dataSheet);
        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint("Institutions!$A$1:$A$" + institutions.size());

        CellRangeAddressList addressList = new CellRangeAddressList(1, 65535, 5, 5); // Assuming column F is for institutions
        XSSFDataValidation dataValidation = (XSSFDataValidation) validationHelper.createValidation(constraint, addressList);

        dataValidation.setShowErrorBox(true);
        dataSheet.addValidationData(dataValidation);

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForItemWithFeeUpload = DefaultStreamedContent.builder()
                .name("template_for_item_fee_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public StreamedContent getTemplateForCollectingCentreItemWithFeeUpload() {
        try {
            createTemplateForCollectingCentreItemWithFeeUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForItemWithFeeUpload;
    }

    public StreamedContent getTemplateForItemFeeUploadToSite() {
        try {
            createTemplateForItemFeeUploadToSite();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForItemWithFeeUpload;
    }

    public void createTemplateForItemFeeUploadToSite() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Templae");

        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Item Code", "Item Name", "For Institution", "Item Type", "Local Fee", "Foreigner Fee", "Institution", "Department"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForItemWithFeeUpload = DefaultStreamedContent.builder()
                .name("template_for_item_fee_upload_to_site.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public StreamedContent getTemplateForItemAndFeeUpload() {
        try {
            createTemplateForItemandFeeUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForItemWithFeeUpload;
    }

    public void createTemplateForItemandFeeUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Templae");

        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Name", "Printing Name", "Full Name", "Code", "Category", "Institution", "Department", "Inward Charge Type", "Item Type", "Specimen", "Container", "Hospital Fee", "Collecting Centre Fee", "Chemical Fee", "Additional (Other) Fee"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForItemWithFeeUpload = DefaultStreamedContent.builder()
                .name("template_for_item_and_fee_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public StreamedContent getTemplateForCollectingCentreSpecialFeeUpload() {
        if (department == null) {
            JsfUtil.addErrorMessage("Need to select a Department");
            return null;
        }
        try {
            createTemplateForCollectingCentreSpecialFeeUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForItemWithFeeUpload;
    }

    public StreamedContent getTemplateForOutSourceItemWithFeeUpload() {
        try {
            createTemplateForOutSourceItemWithFeeUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForItemWithFeeUpload;
    }

    public void createTemplateForCollectingCentreItemWithFeeUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Creating the second sheet for institution list
        XSSFSheet institutionSheet = workbook.createSheet("Institutions");

        // Fetching institutions and adding to the institution sheet
        List<Institution> institutions = institutionController.getItems();
        for (int i = 0; i < institutions.size(); i++) {
            Row row = institutionSheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(institutions.get(i).getName());
        }

        // Hiding the institution sheet
//        workbook.setSheetHidden(workbook.getSheetIndex("Institutions"), true);
        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Name", "Printing Name", "Full Name", "Code", "Category", "Institution", "Department", "Inward Charge Type", "Item Type", "Hospital Fee", "Collecting Centre Fee"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Setting data validation for institution column in data sheet
        XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(dataSheet);
        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint("Institutions!$A$1:$A$" + institutions.size());

        CellRangeAddressList addressList = new CellRangeAddressList(1, 65535, 5, 5); // Assuming column F is for institutions
        XSSFDataValidation dataValidation = (XSSFDataValidation) validationHelper.createValidation(constraint, addressList);

        dataValidation.setShowErrorBox(true);
        dataSheet.addValidationData(dataValidation);

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForItemWithFeeUpload = DefaultStreamedContent.builder()
                .name("template_for_item_fee_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void createTemplateForCollectingCentreSpecialFeeUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Upload Collecting Centre Special Fees for " + department.getName());

        // Creating the second sheet for institution list
        XSSFSheet institutionSheet = workbook.createSheet("Institutions");

        // Fetching institutions and adding to the institution sheet
        List<Institution> institutions = institutionController.getItems();
        for (int i = 0; i < institutions.size(); i++) {
            Row row = institutionSheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(institutions.get(i).getName());
        }

        // Hiding the institution sheet
        // workbook.setSheetHidden(workbook.getSheetIndex("Institutions"), true);
        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {
            "Name", "Item Code", "Institution", "Department", "Fee Name", "Fee Type",
            "Speciality", "Staff", "Fee Institution", "Fee Department",
            "Collecting Centre", "Fee Value for Locals", "Fee Value for Foreigners"
        };
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        List<ItemFee> fees = itemFeeController.fillDepartmentItemFees(department);
        for (int i = 0; i < fees.size(); i++) {
            ItemFee fee = fees.get(i);
            Row row = dataSheet.createRow(i + 1); // Starting from row 1 since row 0 is the header
            row.createCell(0).setCellValue(fee.getItem().getName());
            row.createCell(1).setCellValue(fee.getItem().getCode());
            row.createCell(2).setCellValue(fee.getItem().getInstitution().getName());
            row.createCell(3).setCellValue(fee.getItem().getDepartment().getName());
            row.createCell(4).setCellValue(fee.getName());
            row.createCell(5).setCellValue(fee.getFeeType() != null ? fee.getFeeType().toString() : "");
            row.createCell(6).setCellValue(fee.getSpeciality() != null ? fee.getSpeciality().getName() : "");
            row.createCell(7).setCellValue(fee.getStaff() != null ? fee.getStaff().getName() : "");
            row.createCell(8).setCellValue(fee.getInstitution() != null ? fee.getInstitution().getName() : "");
            row.createCell(9).setCellValue(fee.getDepartment() != null ? fee.getDepartment().getName() : "");
            row.createCell(10).setCellValue("");
            row.createCell(11).setCellValue(fee.getFee());
            row.createCell(12).setCellValue(fee.getFfee());
        }

        // Setting data validation for institution column in data sheet
        XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(dataSheet);
        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint("Institutions!$A$1:$A$" + institutions.size());

        CellRangeAddressList addressList = new CellRangeAddressList(1, 65535, 2, 2); // Assuming column C is for institutions
        XSSFDataValidation dataValidation = (XSSFDataValidation) validationHelper.createValidation(constraint, addressList);

        dataValidation.setShowErrorBox(true);
        dataSheet.addValidationData(dataValidation);

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForItemWithFeeUpload = DefaultStreamedContent.builder()
                .name("template_for_item_fee_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void createTemplateForOutSourceItemWithFeeUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Data Entry");

        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Name", "Printing Name", "Full Name", "Code", "Category", "Institution", "Department", "Inward Charge Type", "Item Type", "Sample", "Container", "Hospital Fee", "Outside Fee", "Comments"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForItemWithFeeUpload = DefaultStreamedContent.builder()
                .name("template_for_Investigation_and_fee_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public StreamedContent getTemplateForCreditCompanyUpload() {
        try {
            createTemplateForCreditCOmpanyUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForCreditCompanyUpload;
    }

    public void createTemplateForCreditCOmpanyUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Collecting Centres");

        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Name", "Printing Name", "Contact No", "Email Address", "Agent Address"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForCreditCompanyUpload = DefaultStreamedContent.builder()
                .name("template_for_credit_company_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public StreamedContent getTemplateForCollectingCentreUpload() {
        try {
            createTemplateForCollectingCentreUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForCollectingCentreUpload;
    }

    public StreamedContent getTemplateForSupplierUpload() {
        try {
            createTemplateForSupplierUpload();
        } catch (IOException e) {
            // Handle IOException
        }
        return templateForsupplierUpload;
    }

    public StreamedContent getTemplateForSupplierExtendedUpload() {
        try {
            createTemplateForSupplierExtendedUpload();
        } catch (IOException e) {
            Logger.getLogger(DataUploadController.class.getName()).log(Level.SEVERE, "Error creating supplier template", e);
            JsfUtil.addErrorMessage("Error creating template: " + e.getMessage());
        }
        return templateForsupplierUpload;
    }

    public void createTemplateForCollectingCentreUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Collecting Centres");

        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Code", "Agent Name", "Agent Printing Name", "Active", "With Commission Status", "Route Name", "Percentage", "Agent Contact No", "Email Address", "Owner Name", "Agent Address", "Standard Credit Limit", "Allowed Credit Limit", "Max Credit Limit"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForCollectingCentreUpload = DefaultStreamedContent.builder()
                .name("template_for_collecting_centres_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void createTemplateForSupplierUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Collecting Centres");

        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Code", "Supplier Name", "Supplier Printing Name", "Active", "Supplier Contact No", "Fax", "Email Address", "web", "mobile no", "Owner Name", "Agent Address"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForsupplierUpload = DefaultStreamedContent.builder()
                .name("template_for_supplier_upload.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public void createTemplateForSupplierExtendedUpload() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Creating the first sheet for data entry
        XSSFSheet dataSheet = workbook.createSheet("Suppliers");

        // Create header row in data sheet
        Row headerRow = dataSheet.createRow(0);
        String[] columnHeaders = {"Code", "Supplier Name", "QB Supplier Name", "Active", "Contact Person Name", "Address",
                "Telephone", "Fax", "E Mail", "Web", "Mobile No.", "Payment Company Name", "Bank Name", "Branch Name", "Acc No", "Legal Company"};
        for (int i = 0; i < columnHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
        }

        // Auto-size columns for aesthetics
        for (int i = 0; i < columnHeaders.length; i++) {
            dataSheet.autoSizeColumn(i);
        }

        // Write the output to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Set the downloading file
        templateForsupplierUpload = DefaultStreamedContent.builder()
                .name("template_for_supplier_upload_extended.xlsx")
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .stream(() -> inputStream)
                .build();
    }

    public String getOutputString() {
        return outputString;
    }

    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<ItemFee> getItemFees() {
        return itemFees;
    }

    public void setItemFees(List<ItemFee> itemFees) {
        this.itemFees = itemFees;
    }

    public List<Institution> getCollectingCentres() {
        return collectingCentres;
    }

    public void setCollectingCentres(List<Institution> collectingCentres) {
        this.collectingCentres = collectingCentres;
    }

    public List<Item> getItemsToSave() {
        return itemsToSave;
    }

    public void setItemsToSave(List<Item> itemsToSave) {
        this.itemsToSave = itemsToSave;
    }

    public List<Item> getItemsSkipped() {
        return itemsSkipped;
    }

    public void setItemsSkipped(List<Item> itemsSkipped) {
        this.itemsSkipped = itemsSkipped;
    }

    public List<Category> getCategoriesSaved() {
        return categoriesSaved;
    }

    public void setCategoriesSaved(List<Category> categoriesSaved) {
        this.categoriesSaved = categoriesSaved;
    }

    public List<Institution> getInstitutionsSaved() {
        return institutionsSaved;
    }

    public void setInstitutionsSaved(List<Institution> institutionsSaved) {
        this.institutionsSaved = institutionsSaved;
    }

    public List<Department> getDepartmentsSaved() {
        return departmentsSaved;
    }

    public void setDepartmentsSaved(List<Department> departmentsSaved) {
        this.departmentsSaved = departmentsSaved;
    }

    public boolean isPollActive() {
        return pollActive;
    }

    public void setPollActive(boolean pollActive) {
        this.pollActive = pollActive;
    }

    public List<Consultant> getConsultantsToSave() {
        return consultantsToSave;
    }

    public void setConsultantsToSave(List<Consultant> consultantsToSave) {
        this.consultantsToSave = consultantsToSave;
    }

    public List<Staff> getStaffToSave() {
        return staffToSave;
    }

    public void setStaffToSave(List<Staff> staffToSave) {
        this.staffToSave = staffToSave;
    }

    public List<Institution> getCreditCompanies() {
        return creditCompanies;
    }

    public void setCreditCompanies(List<Institution> creditCompanies) {
        this.creditCompanies = creditCompanies;
    }

    public List<Doctor> getDoctorsTosave() {
        return doctorsTosave;
    }

    public void setDoctorsTosave(List<Doctor> doctorsTosave) {
        this.doctorsTosave = doctorsTosave;
    }

    public boolean isUploadComplete() {
        return uploadComplete;
    }

    public void setUploadComplete(boolean uploadComplete) {
        this.uploadComplete = uploadComplete;
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

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<ClinicalEntity> getSurgeries() {
        return surgeries;
    }

    public void setSurgeries(List<ClinicalEntity> surgeries) {
        this.surgeries = surgeries;
    }

    public List<ClinicalEntity> getSurgeriesToSave() {
        return surgeriesToSave;
    }

    public void setSurgeriesToSave(List<ClinicalEntity> surgeriesToSave) {
        this.surgeriesToSave = surgeriesToSave;
    }

    public List<ClinicalEntity> getSurgeriesToSkiped() {
        return surgeriesToSkiped;
    }

    public void setSurgeriesToSkiped(List<ClinicalEntity> surgeriesToSkiped) {
        this.surgeriesToSkiped = surgeriesToSkiped;
    }

    public List<Institution> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<Institution> suppliers) {
        this.suppliers = suppliers;
    }

    public StreamedContent getTemplateForsupplierUpload() {
        return templateForsupplierUpload;
    }

    public void setTemplateForsupplierCentreUpload(StreamedContent templateForsupplierCentreUpload) {
        this.templateForsupplierUpload = templateForsupplierCentreUpload;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public List<Institution> getAgencies() {
        return agencies;
    }

    public void setAgencies(List<Institution> agencies) {
        this.agencies = agencies;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public VtmFacade getVtmFacade() {
        return vtmFacade;
    }

    public AtmFacade getAtmFacade() {
        return atmFacade;
    }

    public AmpFacade getAmpFacade() {
        return ampFacade;
    }

    public VmpFacade getVmpFacade() {
        return vmpFacade;
    }

    public AmppFacade getAmppFacade() {
        return amppFacade;
    }

    public VmppFacade getVmppFacade() {
        return vmppFacade;
    }

    public PharmacyPurchaseController getPharmacyPurchaseController() {
        return pharmacyPurchaseController;
    }

    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public List<Patient> getSavedPatients() {
        return savedPatients;
    }

    public void setSavedPatients(List<Patient> savedPatients) {
        this.savedPatients = savedPatients;
    }

    public List<Item> getItemsUpdated() {
        return itemsUpdated;
    }

    public void setItemsUpdated(List<Item> itemsUpdated) {
        this.itemsUpdated = itemsUpdated;
    }

    public List<ItemFee> getUploadeditemFees() {
        return uploadeditemFees;
    }

    public void setUploadeditemFees(List<ItemFee> uploadeditemFees) {
        this.uploadeditemFees = uploadeditemFees;
    }

    public List<ItemLight> getRejecteditemFees() {
        return rejecteditemFees;
    }

    public void setRejecteditemFees(List<ItemLight> rejecteditemFees) {
        this.rejecteditemFees = rejecteditemFees;
    }

    public boolean isSkipDepartmentTypeColumn() {
        return skipDepartmentTypeColumn;
    }

    public void setSkipDepartmentTypeColumn(boolean skipDepartmentTypeColumn) {
        this.skipDepartmentTypeColumn = skipDepartmentTypeColumn;
    }

    public DepartmentType getDefaultDepartmentType() {
        return defaultDepartmentType;
    }

    public void setDefaultDepartmentType(DepartmentType defaultDepartmentType) {
        this.defaultDepartmentType = defaultDepartmentType;
    }

    public Category getSelectedFeeList() {
        return selectedFeeList;
    }

    public void setSelectedFeeList(Category selectedFeeList) {
        this.selectedFeeList = selectedFeeList;
    }

    public List<String> getUploadErrors() {
        return uploadErrors;
    }

    public void setUploadErrors(List<String> uploadErrors) {
        this.uploadErrors = uploadErrors;
    }

    public String getUploadErrorDetails() {
        return uploadErrorDetails;
    }

    public void setUploadErrorDetails(String uploadErrorDetails) {
        this.uploadErrorDetails = uploadErrorDetails;
    }

    public void clearUploadErrors() {
        this.uploadErrors = null;
        this.uploadErrorDetails = null;
    }

}
