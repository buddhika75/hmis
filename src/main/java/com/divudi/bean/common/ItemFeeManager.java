/*
 * Open Hospital Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.bean.common;

import com.divudi.core.data.FeeType;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.ItemFee;
import com.divudi.core.entity.Staff;
import com.divudi.core.facade.DepartmentFacade;
import com.divudi.core.facade.ItemFacade;
import com.divudi.core.facade.ItemFeeFacade;
import com.divudi.core.facade.StaffFacade;
import com.divudi.core.facade.InstitutionFacade;
import com.divudi.core.util.JsfUtil;
import com.divudi.core.data.InstitutionType;
import com.divudi.core.data.ItemLight;
import com.divudi.core.data.dto.CollectingCentreItemFeeCountDTO;
import com.divudi.core.data.dto.CollectingCentreFeeAssignmentDTO;
import com.divudi.core.entity.Category;
import com.divudi.core.entity.Institution;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author pasan
 */
@Named
@SessionScoped
public class ItemFeeManager implements Serializable {

    /**
     * Creates a new instance of ItemFeeManager
     */
    public ItemFeeManager() {
    }

    Item item;
    ItemFee itemFee;
    ItemFee removingFee;
    private Institution collectingCentre;
    private Institution forSite;
    private Department forDepartment;
    private Category feeListType;

    List<ItemFee> itemFees;

    @Inject
    FeeValueController feeValueController;
    @EJB
    ItemFeeFacade itemFeeFacade;
    @EJB
    ItemFacade itemFacade;
    @EJB
    DepartmentFacade departmentFacade;
    @EJB
    StaffFacade staffFacade;
    @EJB
    InstitutionFacade institutionFacade;

    @Inject
    SessionController sessionController;
    @Inject
    ItemController itemController;

    List<Department> departments;
    List<Staff> staffs;
    private List<Item> selectedList;

    private Double totalItemFee;
    private Double totalItemFeeForForeigners;

    public String navigateToInstitutionItemFeeUpload() {
        return "/admin/pricing/institution_item_fee_upload?faces-redirect=true";
    }

    public String navigateItemFeeList() {
        return "/admin/pricing/item_fee_list?faces-redirect=true";
    }

    public String navigateItemFeeValueList() {
        return "/admin/pricing/item_fee_value_list?faces-redirect=true";
    }

    public String navigateToCollectingCentreItemFeeList() {
        return "/admin/pricing/item_fee_list_collecting_centre?faces-redirect=true";
    }

    public String navigateToCorrectItemFees() {
        return "/dataAdmin/bulk_update_itemsFees?faces-redirect=true";
    }

    public String navigateToUploadAndReplaceItemFees() {
        return "/admin/pricing/item_fee_upload_to_replace?faces-redirect=true";
    }

    public String navigateToDownloadBaseItemFees() {
        itemFees = new ArrayList<>();
        return "/admin/pricing/download_base_item_fees?faces-redirect=true";
    }

    public String navigateToUploadToReplaceSiteFeesByItemCode() {
        itemFees = new ArrayList<>();
        return "/admin/pricing/upload_to_replace_site_fees_by_item_code?faces-redirect=true";
    }

    public String navigateToUploadToAddSiteFeesByItemCode() {
        itemFees = new ArrayList<>();
        return "/admin/pricing/upload_to_add_site_fees_by_item_code?faces-redirect=true";
    }

    public String navigateToUploadToAddDepartmentFeesByItemCode() {
        itemFees = new ArrayList<>();
        return "/admin/pricing/upload_to_add_department_fees_by_item_code?faces-redirect=true";
    }

    public String navigateToUploadToAddCcFeesByItemCode() {
        itemFees = new ArrayList<>();
        return "/admin/pricing/upload_to_add_cc_fees_by_item_code?faces-redirect=true";
    }

    public String navigateToDownloadItemFeesForSites() {
        itemFees = new ArrayList<>();
        return "/admin/pricing/download_item_fees_for_sites?faces-redirect=true";
    }

    public String navigateToDownloadItemFeesForCollectingCentres() {
        itemFees = new ArrayList<>();
        return "/admin/pricing/download_item_fees_for_collecting_centres?faces-redirect=true";
    }

    public String navigateToUploadFeeListItemFees() {
        return "/admin/pricing/feelist_item_fees_upload?faces-redirect=true";
    }

    public String navigateToDownloadItemFeesForLists() {
        itemFees = new ArrayList<>();
        return "/admin/pricing/download_item_fees_for_lists?faces-redirect=true";
    }

    public String navigateToUploadChangedItemFees() {
        itemFees = new ArrayList<>();
        return "/admin/pricing/upload_changed_item_fees?faces-redirect=true";
    }

    public String navigateToUploadAndReplaceListFees() {
        return "/admin/pricing/feelist_type_upload_to_replace?faces-redirect=true";
    }

    public String navigateToUploadInstitutionItemFees() {

        return "/admin/pricing/feelist_item_fees_upload?faces-redirect=true";
    }

    public String navigateToUploadCollectingCentreFeeList() {
        return "/admin/pricing/collecting_centre_price_list_upload?faces-redirect=true";
    }

    public String navigateToUploadCollectingCentreItemFees() {
        return "/admin/pricing/item_fee_upload_collecting_centre?faces-redirect=true";
    }

    public String navigateItemViseFeeList() {
        return "/admin/pricing/manage_item_fees_bulk?faces-redirect=true";
    }

    public void fillBaseItemFees() {
        itemFees = fillFees(null, null, null);
    }

    public void fillFeeListItemFees() {
        itemFees = fillFees(null, null, feeListType);
    }

    public void fillSiteItemFees() {
        itemFees = fillFees(null, forSite, null);
    }

    public void downloadBaseItemFeesAsExcel() throws IOException {
        // Check if itemFees is null or empty
        if (itemFees == null || itemFees.isEmpty()) {
            JsfUtil.addErrorMessage("Please fill item fees first to download them.");
            return;
        }

        // Create a workbook and a sheet
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Item Fees");

        // Create the header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Collecting Centre Name", "Collecting Centre Code", "Item Code",
            "Item Name", "Fee Name", "Fee Type", "Discount Allowed", "Retired",
            "Institution", "Department", "Staff", "Value for Locals", "Value for Foreigners"};

        // Apply header formatting
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);

        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
        }

        // Fill the data into the sheet
        int rowNum = 1;
        for (ItemFee fee : itemFees) {
            if (fee == null) {
                continue; // Skip null entries
            }

            Row row = sheet.createRow(rowNum++);

            boolean unlock = (rowNum - 1) > 0; // Avoid unlocking headers

            // Populate the ID
            createLockedCell(row, 0, fee.getId(), unlock, workbook);

            // Get and populate Collecting Centre Name and Code
            Institution collectingCentre = fee.getForInstitution();
            String ccName = (collectingCentre != null) ? collectingCentre.getName() : "";
            String ccCode = (collectingCentre != null) ? collectingCentre.getCode() : "";
            createLockedCell(row, 1, ccName, unlock, workbook); // Collecting Centre Name
            createLockedCell(row, 2, ccCode, unlock, workbook); // Collecting Centre Code

            // Populate the rest of the fields
            if (fee.getItem() != null) {
                createLockedCell(row, 3, fee.getItem().getCode(), unlock, workbook);
                createLockedCell(row, 4, fee.getItem().getName(), unlock, workbook);
            } else {
                createLockedCell(row, 3, "", unlock, workbook); // Fallback for null item code
                createLockedCell(row, 4, "", unlock, workbook); // Fallback for null item name
            }

            createUnlockedCell(row, 5, fee.getName(), workbook); // Fee Name - Unlocked

            if (fee.getFeeType() != null) {
                createLockedCell(row, 6, fee.getFeeType().getLabel(), unlock, workbook);
            } else {
                createLockedCell(row, 6, "", unlock, workbook); // Fallback for null fee type
            }

            createUnlockedCell(row, 7, fee.isDiscountAllowed() ? "Yes" : "No", workbook); // Discount Allowed - Unlocked
            createUnlockedCell(row, 8, fee.isRetired() ? "Yes" : "No", workbook); // Retired - Unlocked

            if (fee.getInstitution() != null) {
                createLockedCell(row, 9, fee.getInstitution().getName(), unlock, workbook);
            } else {
                createLockedCell(row, 9, "", unlock, workbook); // Fallback for null institution
            }

            if (fee.getDepartment() != null) {
                createLockedCell(row, 10, fee.getDepartment().getName(), unlock, workbook);
            } else {
                createLockedCell(row, 10, "", unlock, workbook); // Fallback for null department
            }

            if (fee.getStaff() != null && fee.getStaff().getPerson() != null) {
                createLockedCell(row, 11, fee.getStaff().getPerson().getNameWithTitle(), unlock, workbook);
            } else {
                createLockedCell(row, 11, "", unlock, workbook); // Fallback for null staff
            }

            createUnlockedCell(row, 12, fee.getFee(), workbook); // Value for Locals - Unlocked
            createUnlockedCell(row, 13, fee.getFfee(), workbook); // Value for Foreigners - Unlocked
        }

        // Apply a table format, ensuring there are enough rows and columns for the table
        if (rowNum > 1) { // Ensure there are rows beyond the header
            AreaReference area = new AreaReference("A1:N" + rowNum, SpreadsheetVersion.EXCEL2007);
            XSSFTable table = sheet.createTable(area);
            table.setName("BaseItemFeesTable");
            table.setDisplayName("BaseItemFeesTable");
        }

        // Write the output to the response
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"base_item_fees.xlsx\"");
        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
        outputStream.close();

        facesContext.responseComplete();
    }

// Method to create locked cells
    private void createLockedCell(Row row, int column, Object value, boolean unlock, XSSFWorkbook workbook) {
        Cell cell = row.createCell(column);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        }
//        CellStyle style = workbook.createCellStyle();
//        style.setLocked(!unlock); // Lock unless specified to unlock
//        cell.setCellStyle(style);

    }

// Method to create unlocked cells
    private void createUnlockedCell(Row row, int column, Object value, XSSFWorkbook workbook) {
        createLockedCell(row, column, value, true, workbook); // Call with unlock true
    }

    public void uploadToReplaceItemFees() {

        itemFees = fillFees(null, null, null);
    }

    public void fillForCollectingCentreItemFees() {
        itemFees = fillCollectingCentreSpecificFees(collectingCentre);
    }

    public void fillForSiteItemFees() {
        itemFees = fillFees(null, forSite, null);
    }

    public void updateFeesForSiteItemFees() {
        for (ItemFee tif : itemFees) {
            updateSiteFeeValues(tif.getItem(), forSite);
        }
    }

    public void updateFeesForDepartmentItemFees() {
        for (ItemFee tif : itemFees) {
            updateDepartmentFeeValues(tif.getItem(), forDepartment);
        }
    }

    public void updateFeesForListFees() {
        for (ItemFee tif : itemFees) {
            updateListFeeValues(tif.getItem(), feeListType);
        }
    }

    public void updateFeesForCcFees() {
        for (ItemFee tif : itemFees) {
            updateCcFeeValues(tif.getItem(), collectingCentre);
        }
    }

    public void createItemFessForSelectedItems() {
        if (selectedList == null || selectedList.isEmpty()) {
            JsfUtil.addErrorMessage("Nothing is selected");
            return;
        }
        if (itemFee == null) {
            JsfUtil.addErrorMessage("No Item Fee");
            return;
        }
        ItemFee baseItemFee = itemFee;
        for (Item i : selectedList) {
            ItemFee itf = new ItemFee();
            itf.setName(baseItemFee.getName());
            itf.setItem(i);
            itf.setInstitution(baseItemFee.getInstitution());
            itf.setDepartment(baseItemFee.getDepartment());
            itf.setFeeType(baseItemFee.getFeeType());
            itf.setFee(baseItemFee.getFee());
            itf.setFfee(baseItemFee.getFfee());
            addNewFeeForItem(i, itf);
        }
        itemFee = baseItemFee;
    }

    public void fixIssueToReferralFees() {
        List<ItemFee> ifs = itemFeeFacade.findAll();
        for (ItemFee f : ifs) {
            if (f.getFeeType() == FeeType.Issue) {
                f.setFeeType(FeeType.Referral);
                f.setInstitution(f.getItem().getInstitution());
                f.setDepartment(f.getItem().getDepartment());
                itemFeeFacade.edit(f);
            }
        }
    }

    public ItemFee getRemovingFee() {
        return removingFee;
    }

    public void setRemovingFee(ItemFee removingFee) {
        this.removingFee = removingFee;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<Staff> getStaffs() {
        return staffs;
    }

    public void setStaffs(List<Staff> staffs) {
        this.staffs = staffs;
    }

    public void removeFee() {
        if (removingFee == null) {
            JsfUtil.addErrorMessage("Select a fee");
            return;
        }
        removingFee.setRetired(true);
        removingFee.setRetiredAt(new Date());
        removingFee.setRetirer(sessionController.getLoggedUser());
        itemFeeFacade.edit(removingFee);
        itemFees = null;
        updateTotal();
        JsfUtil.addSuccessMessage("Removed. Reload Items");
    }

    public void removeFeeForSiteFees() {
        if (removingFee == null) {
            JsfUtil.addErrorMessage("Select a fee");
            return;
        }
        removingFee.setRetired(true);
        removingFee.setRetiredAt(new Date());
        removingFee.setRetirer(sessionController.getLoggedUser());
        itemFeeFacade.edit(removingFee);
        itemFees = null;
        updateTotal();
        feeValueController.updateFeeValue(item, forSite, totalItemFee, totalItemFeeForForeigners);
        JsfUtil.addSuccessMessage("Removed. Reload Items");
    }

    public void removeFeeForDepartmentFees() {
        if (removingFee == null) {
            JsfUtil.addErrorMessage("Select a fee");
            return;
        }
        removingFee.setRetired(true);
        removingFee.setRetiredAt(new Date());
        removingFee.setRetirer(sessionController.getLoggedUser());
        itemFeeFacade.edit(removingFee);
        itemFees = null;
        updateItemAndDepartmentFees();
        feeValueController.updateFeeValue(item, forDepartment, totalItemFee, totalItemFeeForForeigners);
        JsfUtil.addSuccessMessage("Removed. Reload Items");
    }

    public void fillDepartments() {
        ////// // System.out.println("fill dept");
        String jpql;
        Map m = new HashMap();
        m.put("ins", getItemFee().getInstitution());
        jpql = "select d from Department d where d.retired=false and d.institution=:ins order by d.name";
        ////// // System.out.println("m = " + m);
        ////// // System.out.println("jpql = " + jpql);
        departments = departmentFacade.findByJpql(jpql, m);
    }

    public void fillStaff() {
        ////// // System.out.println("fill staff");
        String jpql;
        Map m = new HashMap();
        m.put("ins", getItemFee().getSpeciality());
        jpql = "select d from Staff d where d.retired=false and d.speciality=:ins order by d.person.name";
        ////// // System.out.println("m = " + m);
        ////// // System.out.println("jpql = " + jpql);
        staffs = staffFacade.findByJpql(jpql, m);
    }

    public List<Department> compelteDepartments(String qry) {
        String jpql;
        if (qry == null) {
            return new ArrayList<>();
        }
        Map m = new HashMap();
        m.put("ins", getItemFee().getInstitution());
        m.put("name", "%" + qry.toUpperCase() + "%");
        jpql = "select d from Department d where d.retired=false and d.institution=:ins and d.name like :name order by d.name";
        return departmentFacade.findByJpql(jpql, m);
    }

    public String navigateToItemFees() {
        item = null;
        itemFees = null;
        return "/admin/pricing/manage_item_fees?faces-redirect=true";
    }

    public String navigateToCollectingCentreItemFees() {
        collectingCentre = null;
        item = null;
        feeListType = null;
        fillForCollectingCentreFees();
        return "/admin/pricing/manage_collecting_centre_item_fees?faces-redirect=true";
    }

    public String navigateToForSiteItemFees() {
        collectingCentre = null;
        item = null;
        feeListType = null;
        fillForCollectingCentreFees();
        return "/admin/pricing/manage_for_site_item_fees?faces-redirect=true";
    }

    public String navigateToForDepartmentItemFees() {
        collectingCentre = null;
        item = null;
        feeListType = null;
        fillForCollectingCentreFees();
        return "/admin/pricing/manage_for_department_item_fees?faces-redirect=true";
    }

    public String navigateToFeeListFees() {
        collectingCentre = null;
        item = null;
        feeListType = null;
        fillForCollectingCentreFees();
        return "/admin/pricing/manage_fee_list_item_fees?faces-redirect=true";
    }

    public String navigateToItemFeesMultiple() {
        return "/admin/pricing/manage_item_fees_multiple?faces-redirect=true";
    }

    public void clearItemFees() {
        item = null;
        itemFees = null;
        removingFee = null;
        collectingCentre = null;
    }

    public void clearSelectedItems() {
        selectedList = new ArrayList<>();
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public ItemFee getItemFee() {
        if (itemFee == null) {
            itemFee = new ItemFee();
        }
        return itemFee;
    }

    public void setItemFee(ItemFee itemFee) {
        this.itemFee = itemFee;
    }

    public List<ItemFee> getItemFees() {
        return itemFees;
    }

    public void setItemFees(List<ItemFee> itemFees) {
        this.itemFees = itemFees;
    }

    public void fillFees() {
        itemFees = fillFees(item);
    }

    public void fillFeesForAuditing() {
        itemFees = fillFeesForAuditing(item);
    }

    public void updateItemAndCollectingCentreFees() {
        itemFees = new ArrayList<>();
        if (item == null) {
            return;
        }
        if (collectingCentre == null) {
            return;
        }
        itemFees = fillFees(item, collectingCentre);
        totalItemFee = itemFees.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFee)
                .sum();
        totalItemFeeForForeigners = itemFees.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFfee)
                .sum();
        feeValueController.updateFeeValue(item, collectingCentre, totalItemFee, totalItemFeeForForeigners);
    }

    public void updateSiteFeeValues(Item ti, Institution si) {
        List<ItemFee> tfs = fillFees(ti, si);
        double tlf = tfs.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFee)
                .sum();
        double tfff = tfs.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFfee)
                .sum();
        feeValueController.updateFeeValue(ti, si, tlf, tfff);
    }

    public void updateDepartmentFeeValues(Item ti, Department dept) {
        List<ItemFee> tfs = fetchForDepartmentFees(ti, dept);
        double tlf = tfs.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFee)
                .sum();
        double tfff = tfs.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFfee)
                .sum();
        feeValueController.updateFeeValue(ti, dept, tlf, tfff);
    }
    
    public void updateDepartmentFeeValues(Item item, Department dept, List<ItemFee> tfs) {
        double localFeeTotal = tfs.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFee)
                .sum();
        double foreignerFeeTotal = tfs.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFfee)
                .sum();
        feeValueController.updateFeeValue(item, dept, localFeeTotal, foreignerFeeTotal);
    }

    public void updateCcFeeValues(Item ti, Institution cc) {
        List<ItemFee> tfs = fillFees(ti, cc);
        double tlf = tfs.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFee)
                .sum();
        double tfff = tfs.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFfee)
                .sum();
        feeValueController.updateFeeValue(ti, cc, tlf, tfff);
    }

    public void updateListFeeValues(Item ti, Category fl) {
        List<ItemFee> tfs = fillFees(ti, fl);
        double tlf = tfs.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFee)
                .sum();
        double tfff = tfs.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFfee)
                .sum();
        feeValueController.updateFeeValue(ti, fl, tlf, tfff);
    }

    public void updateItemAndSiteFees() {
        itemFees = new ArrayList<>();
        if (item == null) {
            return;
        }
        if (forSite == null) {
            return;
        }
        itemFees = fillFees(item, forSite);
        calculateFeesForSitesByProvidingFees();
    }

    public void updateItemAndDepartmentFees() {
        itemFees = new ArrayList<>();
        if (item == null) {
            return;
        }
        if (forDepartment == null) {
            return;
        }
        itemFees = fetchForDepartmentFees(item, forDepartment);
        calculateFeesForDepartmentsByProvidingFees();
    }

    public void calculateFeesForSitesByProvidingFees() {
        totalItemFee = itemFees.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFee)
                .sum();
        totalItemFeeForForeigners = itemFees.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFfee)
                .sum();
        feeValueController.updateFeeValue(item, forSite, totalItemFee, totalItemFeeForForeigners);
    }

    public void calculateFeesForDepartmentsByProvidingFees() {
        totalItemFee = itemFees.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFee)
                .sum();
        totalItemFeeForForeigners = itemFees.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFfee)
                .sum();
        feeValueController.updateFeeValue(item, forDepartment, totalItemFee, totalItemFeeForForeigners);
    }

    public void updateItemAndFeeListees() {
        itemFees = new ArrayList<>();
        if (item == null) {
            return;
        }
        if (feeListType == null) {
            return;
        }
        itemFees = fillFees(item, feeListType);
        totalItemFee = itemFees.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFee)
                .sum();
        totalItemFeeForForeigners = itemFees.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemFee::getFfee)
                .sum();
        feeValueController.updateFeeValue(item, feeListType, totalItemFee, totalItemFeeForForeigners);
    }

    public void fillForCollectingCentreFees() {
        if (collectingCentre == null) {
            itemFees = null;
            totalItemFee = 0.0;
            totalItemFeeForForeigners = 0.0;
            return;
        }
        itemFees = fillFees(item, collectingCentre);
//        totalItemFee = itemFees.stream()
//                .filter(Objects::nonNull)
//                .mapToDouble(ItemFee::getFee)
//                .sum();
//        totalItemFeeForForeigners = itemFees.stream()
//                .filter(Objects::nonNull)
//                .mapToDouble(ItemFee::getFfee)
//                .sum();
//
//        feeValueController.updateFeeValue(item, collectingCentre, totalItemFee, totalItemFeeForForeigners);

    }

    public void fillForSiteFees() {
        if (forSite == null) {
            itemFees = null;
            totalItemFee = 0.0;
            totalItemFeeForForeigners = 0.0;
            return;
        }
        itemFees = fillFees(item, forSite);
//        totalItemFee = itemFees.stream()
//                .filter(Objects::nonNull)
//                .mapToDouble(ItemFee::getFee)
//                .sum();
//        totalItemFeeForForeigners = itemFees.stream()
//                .filter(Objects::nonNull)
//                .mapToDouble(ItemFee::getFfee)
//                .sum();
//        feeValueController.updateFeeValue(item, forSite, totalItemFee, totalItemFeeForForeigners);

    }

    public void fillForForCategoryFees() {
        if (feeListType == null) {
            itemFees = null;
            totalItemFee = 0.0;
            totalItemFeeForForeigners = 0.0;
            return;
        }
        itemFees = fillFees(item, feeListType);
//        totalItemFee = itemFees.stream()
//                .filter(Objects::nonNull)
//                .mapToDouble(ItemFee::getFee)
//                .sum();
//        totalItemFeeForForeigners = itemFees.stream()
//                .filter(Objects::nonNull)
//                .mapToDouble(ItemFee::getFfee)
//                .sum();
    }

    public String toManageItemFees() {
        if (item == null) {
            JsfUtil.addErrorMessage("Nothing Selected to Edit");
            return "";
        }
        fillFees();
        return "/common/manage_item_fees?faces-redirect=true";
    }

    public List<ItemFee> fillFees(Item i) {
        return fillFees(i, null, null);
    }

    public List<ItemFee> fillFeesForAuditing(Item i) {
        return fillFees(i, null, null, true);
    }

    public List<ItemFee> fillFees(Item i, Institution ins) {
        return fillFees(i, ins, null);
    }

    public void fillForDepartmentFeesForSelectedItem() {
        itemFees = new ArrayList<>();
        if (item == null) {
            return;
        }
        if (forDepartment == null) {
            return;
        }
        itemFees = fetchForDepartmentFees(item, forDepartment);
        updateDepartmentFeeValues(item, forDepartment, itemFees);
    }

    public void fillForDepartmentFees() {
        if (forDepartment == null) {
            itemFees = null;
            totalItemFee = 0.0;
            totalItemFeeForForeigners = 0.0;
            return;
        }
        itemFees = fetchForDepartmentFees(item, forDepartment);
    }

    public List<ItemFee> fetchForDepartmentFees(Item i, Department dept) {
        if (dept == null) {
            return null;
        }
        String jpql = "select f "
                + " from ItemFee f where 1=1";
        Map<String, Object> m = new HashMap<>();
        jpql += " and f.retired=:ret";
        m.put("ret", false);
        if (i != null) {
            jpql += " and f.item=:i";
            m.put("i", i);
        }
        jpql += " and f.forDepartment=:d";
        m.put("d", dept);
        jpql += " and f.forInstitution is null";
        jpql += " and f.forCategory is null";
        return itemFeeFacade.findByJpql(jpql, m);
    }

    public List<ItemFee> fillFees(Item i, Category cat) {
        return fillFees(i, null, cat);
    }

    // Original method preserved
    public List<ItemFee> fillFees(Item i, Institution forInstitution, Category forCategory) {
        return fillFees(i, forInstitution, forCategory, false);
    }

// Overloaded method that ignores 'retired' filtering if includeRetired is true
    public List<ItemFee> fillFees(Item i, Institution forInstitution, Category forCategory, boolean includeRetired) {
        String jpql = "select f from ItemFee f where 1=1";
        Map<String, Object> m = new HashMap<>();

        if (!includeRetired) {
            jpql += " and f.retired=:ret";
            m.put("ret", false);
        }

        if (i != null) {
            jpql += " and f.item=:i";
            m.put("i", i);
        }

        if (forInstitution != null) {
            jpql += " and f.forInstitution=:ins";
            m.put("ins", forInstitution);
        } else {
            jpql += " and f.forInstitution is null";
        }

        if (forCategory != null) {
            jpql += " and f.forCategory=:cat";
            m.put("cat", forCategory);
        } else {
            jpql += " and f.forCategory is null";
        }

        return itemFeeFacade.findByJpql(jpql, m);
    }

    public List<ItemFee> fillCollectingCentreSpecificFees(Institution cc) {
        String jpql = "select f "
                + " from ItemFee f "
                + " where f.retired=:ret ";
        Map<String, Object> m = new HashMap<>();
        m.put("ret", false);

        if (cc != null) {
            jpql += " and f.forInstitution=:ins";
            m.put("ins", cc);
        } else {
            jpql += " and (f.forInstitution is not null and f.forInstitution.institutionType=:ccType) ";
            m.put("ccType", InstitutionType.CollectingCentre);
        }
        jpql += " and f.forCategory is null";

        List<ItemFee> fs = itemFeeFacade.findByJpql(jpql, m);
        return fs;
    }

    public List<ItemLight> fillItemLightsForSite(Institution forInstitution) {
        String jpql = "SELECT new com.divudi.core.data.ItemLight("
                + "f.item.id, "
                + "f.item.department.name, "
                + "f.item.name, "
                + "f.item.code, "
                + " sum(f.fee), "
                + "f.item.department.id) "
                + " from ItemFee f "
                + " where f.retired=:ret ";
        Map<String, Object> m = new HashMap<>();
        m.put("ret", false);

        if (forInstitution != null) {
            jpql += " and f.forInstitution=:ins";
            m.put("ins", forInstitution);
        } else {
            jpql += " and f.forInstitution is null ";
        }

        jpql += " and f.forCategory is null ";
        jpql += " and f.fee > :tot ";
        jpql += " and f.item.retired=:ir ";
        m.put("tot", 0.0);
        m.put("ir", false);
        jpql += " GROUP BY f.item "
                + " ORDER BY f.item.name";

        List<ItemLight> fs = (List<ItemLight>) itemFacade.findLightsByJpql(jpql, m);
        return fs;
    }

    public List<ItemLight> fillItemLightsForCc(Institution cc) {
        String jpql = "SELECT new com.divudi.core.data.ItemLight("
                + "f.item.id, "
                + "f.item.department.name, "
                + "f.item.name, "
                + "f.item.code, "
                + "f.item.total, "
                + "f.item.department.id) "
                + " from ItemFee f "
                + " where f.retired=:ret ";
        Map<String, Object> m = new HashMap<>();
        m.put("ret", false);

        jpql += " and (f.forInstitution.id=:ins or f.forCategory.id=:fl) ";
        m.put("ins", cc.getId());
        m.put("fl", cc.getFeeListType().getId());

        jpql += " and f.fee > :tot ";
        jpql += " and f.item.retired=:ir ";
        m.put("tot", 0.0);
        m.put("ir", false);
        jpql += " GROUP BY f.item "
                + " ORDER BY f.item.name";

        List<ItemLight> fs = (List<ItemLight>) itemFacade.findLightsByJpql(jpql, m);
        return fs;
    }

    public void addNewFee() {
        if (item == null) {
            JsfUtil.addErrorMessage("Select Item ?");
            return;
        }
        if (itemFee == null) {
            JsfUtil.addErrorMessage("Select Item Fee");
            return;
        }
        if (itemFee.getName() == null || itemFee.getName().trim().equals("")) {
            JsfUtil.addErrorMessage("Please Fill Fee Name");
            return;
        }

        if (itemFee.getFeeType() == null) {
            JsfUtil.addErrorMessage("Please Fill Fee Type");
            return;
        }

        if (itemFee.getFeeType() == FeeType.OtherInstitution || itemFee.getFeeType() == FeeType.OwnInstitution || itemFee.getFeeType() == FeeType.Referral) {
            if (itemFee.getDepartment() == null) {
                JsfUtil.addErrorMessage("Please Select Department");
                return;
            }
        }

//        if (itemFee.getFeeType() == FeeType.Staff) {
//            if (itemFee.getStaff() == null || itemFee.getStaff().getPerson().getName().trim().equals("")) {
//                JsfUtil.addErrorMessage("Please Select Staff");
//                return;
//            }
//        }
        // Fees with a value of zero are allowed. Previous versions prevented
        // saving such fees, but this validation has been removed to support
        // zero-priced services.
        getItemFee().setCreatedAt(new Date());
        getItemFee().setCreater(sessionController.getLoggedUser());
        itemFeeFacade.create(itemFee);

        getItemFee().setItem(item);
        itemFeeFacade.edit(itemFee);

        itemFee = new ItemFee();
        itemFees = null;
        fillFees();
        updateTotal();
        JsfUtil.addSuccessMessage("New Fee Added");
    }

    public void addNewCollectingCentreFee() {
        if (collectingCentre == null) {
            JsfUtil.addErrorMessage("Select Collecting Centre ?");
            return;
        }
        if (item == null) {
            JsfUtil.addErrorMessage("Select Item ?");
            return;
        }
        if (itemFee == null) {
            JsfUtil.addErrorMessage("Select Item Fee");
            return;
        }
        if (itemFee.getName() == null || itemFee.getName().trim().equals("")) {
            JsfUtil.addErrorMessage("Please Fill Fee Name");
            return;
        }

        if (itemFee.getFeeType() == null) {
            JsfUtil.addErrorMessage("Please Fill Fee Type");
            return;
        }

        if (itemFee.getFeeType() == FeeType.OtherInstitution || itemFee.getFeeType() == FeeType.OwnInstitution || itemFee.getFeeType() == FeeType.Referral) {
            if (itemFee.getDepartment() == null) {
                JsfUtil.addErrorMessage("Please Select Department");
                return;
            }
        }
        // Zero value fees are valid for collecting centres as well. Earlier
        // the system rejected such entries. This check has been removed.
        getItemFee().setCreatedAt(new Date());
        getItemFee().setCreater(sessionController.getLoggedUser());
        getItemFee().setForInstitution(collectingCentre);
        itemFeeFacade.create(itemFee);
        getItemFee().setItem(item);
        itemFeeFacade.edit(itemFee);
        itemFee = new ItemFee();
        itemFees = null;
        updateItemAndCollectingCentreFees();;
        JsfUtil.addSuccessMessage("New Fee Added for Collecting Centre");
    }

    public void addNewForSiteFee() {
        if (forSite == null) {
            JsfUtil.addErrorMessage("Select a Site ?");
            return;
        }
        if (item == null) {
            JsfUtil.addErrorMessage("Select an Item ?");
            return;
        }
        if (itemFee == null) {
            JsfUtil.addErrorMessage("Select Item Fee");
            return;
        }
        if (itemFee.getName() == null || itemFee.getName().trim().equals("")) {
            JsfUtil.addErrorMessage("Please Fill Fee Name");
            return;
        }

        if (itemFee.getFeeType() == null) {
            JsfUtil.addErrorMessage("Please Fill Fee Type");
            return;
        }

        if (itemFee.getFeeType() == FeeType.OtherInstitution || itemFee.getFeeType() == FeeType.OwnInstitution || itemFee.getFeeType() == FeeType.Referral) {
            if (itemFee.getDepartment() == null) {
                JsfUtil.addErrorMessage("Please Select Department");
                return;
            }
        }
        // Validation no longer rejects zero value fees for site specific
        // settings.
        getItemFee().setCreatedAt(new Date());
        getItemFee().setCreater(sessionController.getLoggedUser());
        getItemFee().setForInstitution(forSite);
        itemFeeFacade.create(itemFee);
        getItemFee().setItem(item);
        itemFeeFacade.edit(itemFee);
        itemFee = new ItemFee();
        itemFees = null;

        updateItemAndSiteFees();
        JsfUtil.addSuccessMessage("New Fee Added for Site");
    }

    public void addNewForDepartmentFee() {
        if (forDepartment == null) {
            JsfUtil.addErrorMessage("Select a Department ?");
            return;
        }
        if (item == null) {
            JsfUtil.addErrorMessage("Select an Item ?");
            return;
        }
        if (itemFee == null) {
            JsfUtil.addErrorMessage("Select Item Fee");
            return;
        }
        if (itemFee.getName() == null || itemFee.getName().trim().equals("")) {
            JsfUtil.addErrorMessage("Please Fill Fee Name");
            return;
        }

        if (itemFee.getFeeType() == null) {
            JsfUtil.addErrorMessage("Please Fill Fee Type");
            return;
        }

        // Department level fees can also be zero. Previous checks preventing
        // saving when the value was 0 have been removed.
        getItemFee().setCreatedAt(new Date());
        getItemFee().setCreater(sessionController.getLoggedUser());
        getItemFee().setForDepartment(forDepartment);
        itemFeeFacade.create(itemFee);
        getItemFee().setItem(item);
        itemFeeFacade.edit(itemFee);
        itemFee = new ItemFee();
        itemFees = null;

        updateItemAndDepartmentFees();
        JsfUtil.addSuccessMessage("New Fee Added for Department");
    }

    public void addNewFeeForFeeListType() {
        if (feeListType == null) {
            JsfUtil.addErrorMessage("Select Collecting Centre ?");
            return;
        }
        if (item == null) {
            JsfUtil.addErrorMessage("Select Item ?");
            return;
        }
        if (itemFee == null) {
            JsfUtil.addErrorMessage("Select Item Fee");
            return;
        }
        if (itemFee.getName() == null || itemFee.getName().trim().equals("")) {
            JsfUtil.addErrorMessage("Please Fill Fee Name");
            return;
        }

        if (itemFee.getFeeType() == null) {
            JsfUtil.addErrorMessage("Please Fill Fee Type");
            return;
        }

        if (itemFee.getFeeType() == FeeType.OtherInstitution || itemFee.getFeeType() == FeeType.OwnInstitution || itemFee.getFeeType() == FeeType.Referral) {
            if (itemFee.getDepartment() == null) {
                JsfUtil.addErrorMessage("Please Select Department");
                return;
            }
        }
        // Fee list entries may legitimately be zero. Validation allowing such
        // values ensures the dialog can save fees without charges.
        getItemFee().setCreatedAt(new Date());
        getItemFee().setCreater(sessionController.getLoggedUser());
        getItemFee().setForInstitution(null);
        getItemFee().setForCategory(feeListType);
        itemFeeFacade.create(itemFee);

        getItemFee().setItem(item);
        itemFeeFacade.edit(itemFee);

        itemFee = new ItemFee();
        itemFees = null;
        updateItemAndFeeListees();
        JsfUtil.addSuccessMessage("New Fee Added for Fee List");
    }

    public void addNewFeeForItem(Item inputItem, ItemFee inputFee) {
        if (inputItem == null) {
            JsfUtil.addErrorMessage("Select Item ?");
            return;
        }
        inputFee.setCreatedAt(new Date());
        inputFee.setCreater(sessionController.getLoggedUser());
        itemFeeFacade.create(inputFee);

        inputFee.setItem(inputItem);
        itemFeeFacade.edit(inputFee);

        List<ItemFee> inputFees = fillFees(inputItem);
        updateTotal(inputItem, inputFees);

    }

    public void saveItemFee(ItemFee inputFee) {
        if (inputFee == null) {
            return;
        }
        if (inputFee.getId() == null) {
            inputFee.setCreatedAt(new Date());
            inputFee.setCreater(sessionController.getLoggedUser());
            itemFeeFacade.create(inputFee);
        } else {
            inputFee.setEditedAt(new Date());
            inputFee.setEditer(sessionController.getLoggedUser());
            itemFeeFacade.edit(inputFee);
        }

        List<ItemFee> inputFees = fillFees(inputFee.getItem());
        updateTotal(inputFee.getItem(), inputFees);

    }

    public void updateFee(ItemFee f) {
        itemFeeFacade.edit(f);
        updateTotal();
    }

    public void updateFeeForSites(ItemFee f) {
        itemFeeFacade.edit(f);
        calculateFeesForSitesByProvidingFees();
        feeValueController.updateFeeValue(item, forSite, totalItemFee, totalItemFeeForForeigners);
    }

    public void updateFeeForDepartments(ItemFee f) {
        itemFeeFacade.edit(f);
        calculateFeesForDepartmentsByProvidingFees();
        feeValueController.updateFeeValue(item, forDepartment, totalItemFee, totalItemFeeForForeigners);
    }

    public void updateFee() {
        if (item == null) {
            return;
        }
        double t = 0.0;
        double tf = 0.0;
        for (ItemFee f : itemFees) {
            t += f.getFee();
            tf += f.getFfee();
            itemFeeFacade.edit(f);
        }
        getItem().setTotal(t);
        getItem().setTotalForForeigner(tf);
        itemFacade.edit(getItem());
    }

    public void updateTotal(Item inputItem, List<ItemFee> inputItemFees) {
        if (inputItem == null) {
            return;
        }
        double t = 0.0;
        double tf = 0.0;
        for (ItemFee f : inputItemFees) {
            t += f.getFee();
            tf += f.getFfee();
        }
        inputItem.setTotal(t);
        inputItem.setTotalForForeigner(tf);
        itemFacade.edit(inputItem);
    }

    public void updateTotal() {
        if (item == null) {
            return;
        }
        double t = 0.0;
        double tf = 0.0;
        if (itemFees != null) {
            for (ItemFee f : itemFees) {
                t += f.getFee();
                tf += f.getFfee();
            }
        }
        getItem().setTotal(t);
        getItem().setTotalForForeigner(tf);
        itemFacade.edit(item);
    }

    public List<Item> getSelectedList() {
        if (selectedList == null) {
            selectedList = new ArrayList<>();
        }
        return selectedList;
    }

    public void setSelectedList(List<Item> selectedList) {
        this.selectedList = selectedList;
    }

    public Institution getCollectingCentre() {
        return collectingCentre;
    }

    public void setCollectingCentre(Institution collectingCentre) {
        this.collectingCentre = collectingCentre;
    }

    public Double getTotalItemFee() {
        return totalItemFee;
    }

    public void setTotalItemFee(Double totalItemFee) {
        this.totalItemFee = totalItemFee;
    }

    public Double getTotalItemFeeForForeigners() {
        return totalItemFeeForForeigners;
    }

    public void setTotalItemFeeForForeigners(Double totalItemFeeForForeigners) {
        this.totalItemFeeForForeigners = totalItemFeeForForeigners;
    }

    public Category getFeeListType() {
        return feeListType;
    }

    public void setFeeListType(Category feeListType) {
        this.feeListType = feeListType;
    }

    public Institution getForSite() {
        return forSite;
    }

    public void setForSite(Institution forSite) {
        this.forSite = forSite;
    }

    public Department getForDepartment() {
        return forDepartment;
    }

    public void setForDepartment(Department forDepartment) {
        this.forDepartment = forDepartment;
    }
    
    public void onFeeListChange() {
        fillFeeListItemFees();
    }

    public void retireAllCollectingCentreItemFees() {
        String jpql = "select f from ItemFee f where f.retired=:ret and f.forInstitution.institutionType=:ccType";
        Map<String, Object> m = new HashMap<>();
        m.put("ret", false);
        m.put("ccType", InstitutionType.CollectingCentre);
        
        List<ItemFee> ccItemFees = itemFeeFacade.findByJpql(jpql, m);
        
        if (ccItemFees == null || ccItemFees.isEmpty()) {
            JsfUtil.addErrorMessage("No active item fees found for collecting centres");
            return;
        }
        
        int retiredCount = 0;
        for (ItemFee fee : ccItemFees) {
            fee.setRetired(true);
            fee.setRetiredAt(new Date());
            fee.setRetirer(sessionController.getLoggedUser());
            fee.setRetireComments("Bulk retirement of all collecting centre item fees");
            itemFeeFacade.edit(fee);
            retiredCount++;
        }
        
        JsfUtil.addSuccessMessage("Successfully retired " + retiredCount + " collecting centre item fees");
        
        // Refresh the count after retirement
        loadCollectingCentreItemFeeCounts();
    }

    public String navigateToRetireAllCollectingCentreItemFees() {
        return "/admin/pricing/retire_all_collecting_centre_item_fees?faces-redirect=true";
    }

    private List<CollectingCentreItemFeeCountDTO> collectingCentreItemFeeCounts;
    private Long totalActiveCollectingCentreItemFeesCount;
    
    // Fee Assignment functionality
    private List<CollectingCentreFeeAssignmentDTO> collectingCentresForFeeAssignment;
    private List<CollectingCentreFeeAssignmentDTO> filteredCollectingCentres;
    private List<CollectingCentreFeeAssignmentDTO> selectedCollectingCentres;
    private String globalFilterValue;
    private Category selectedFeeListForAssignment;

    public void loadCollectingCentreItemFeeCounts() {
        String jpql = "SELECT new com.divudi.core.data.dto.CollectingCentreItemFeeCountDTO("
                + "f.forInstitution.id, "
                + "f.forInstitution.name, "
                + "f.forInstitution.code, "
                + "count(f)) "
                + "FROM ItemFee f "
                + "WHERE f.retired = :ret "
                + "AND f.forInstitution.institutionType = :ccType "
                + "GROUP BY f.forInstitution.id, f.forInstitution.name, f.forInstitution.code "
                + "ORDER BY f.forInstitution.name";
        
        Map<String, Object> m = new HashMap<>();
        m.put("ret", false);
        m.put("ccType", InstitutionType.CollectingCentre);
        
        collectingCentreItemFeeCounts =  (List<CollectingCentreItemFeeCountDTO>) itemFeeFacade.findLightsByJpql(jpql, m);
        
        // Calculate total count
        totalActiveCollectingCentreItemFeesCount = 0L;
        if (collectingCentreItemFeeCounts != null) {
            for (CollectingCentreItemFeeCountDTO dto : collectingCentreItemFeeCounts) {
                totalActiveCollectingCentreItemFeesCount += dto.getActiveItemFeesCount();
            }
        }
        
        if (totalActiveCollectingCentreItemFeesCount == 0) {
            JsfUtil.addSuccessMessage("No active item fees found for collecting centres");
        } else {
            JsfUtil.addSuccessMessage("Found " + totalActiveCollectingCentreItemFeesCount + " active item fees across " + collectingCentreItemFeeCounts.size() + " collecting centres");
        }
    }

    public List<CollectingCentreItemFeeCountDTO> getCollectingCentreItemFeeCounts() {
        return collectingCentreItemFeeCounts;
    }

    public void setCollectingCentreItemFeeCounts(List<CollectingCentreItemFeeCountDTO> collectingCentreItemFeeCounts) {
        this.collectingCentreItemFeeCounts = collectingCentreItemFeeCounts;
    }

    public Long getTotalActiveCollectingCentreItemFeesCount() {
        return totalActiveCollectingCentreItemFeesCount;
    }

    public void setTotalActiveCollectingCentreItemFeesCount(Long totalActiveCollectingCentreItemFeesCount) {
        this.totalActiveCollectingCentreItemFeesCount = totalActiveCollectingCentreItemFeesCount;
    }

    public String navigateToAssignFeeListsToCollectingCentres() {
        return "/admin/pricing/assign_fee_lists_to_collecting_centres?faces-redirect=true";
    }

    public void loadCollectingCentresForFeeAssignment() {
        String jpql = "SELECT new com.divudi.core.data.dto.CollectingCentreFeeAssignmentDTO("
                + "i.id, "
                + "i.name, "
                + "i.code, "
                + "i.feeListType) "
                + "FROM Institution i "
                + "WHERE i.retired = :ret "
                + "AND i.institutionType = :ccType "
                + "ORDER BY i.name";
        
        Map<String, Object> m = new HashMap<>();
        m.put("ret", false);
        m.put("ccType", InstitutionType.CollectingCentre);
        
        try {
            collectingCentresForFeeAssignment = (List<CollectingCentreFeeAssignmentDTO>) institutionFacade.findLightsByJpql(jpql, m);
            
            // Clear previous selections
            selectedCollectingCentres = new ArrayList<>();
            filteredCollectingCentres = null;
            globalFilterValue = null;
            selectedFeeListForAssignment = null;
            
            if (collectingCentresForFeeAssignment == null || collectingCentresForFeeAssignment.isEmpty()) {
                JsfUtil.addErrorMessage("No collecting centres found in the system");
            } else {
                int withoutFeeList = countCentresWithoutFeeList();
                JsfUtil.addSuccessMessage("Loaded " + collectingCentresForFeeAssignment.size() + " collecting centres. " + withoutFeeList + " centres without fee lists.");
            }
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Error loading collecting centres: " + e.getMessage());
            e.printStackTrace();
            collectingCentresForFeeAssignment = new ArrayList<>();
        }
    }

    public int countCentresWithoutFeeList() {
        if (collectingCentresForFeeAssignment == null) {
            return 0;
        }
        int count = 0;
        for (CollectingCentreFeeAssignmentDTO dto : collectingCentresForFeeAssignment) {
            if (dto.getCurrentFeeListType() == null) {
                count++;
            }
        }
        return count;
    }

    public void assignFeeListToSelectedCentres() {
        if (selectedFeeListForAssignment == null) {
            JsfUtil.addErrorMessage("Please select a fee list to assign");
            return;
        }
        
        if (selectedCollectingCentres == null || selectedCollectingCentres.isEmpty()) {
            JsfUtil.addErrorMessage("Please select at least one collecting centre");
            return;
        }
        
        int totalUpdatedCount = 0;
        
        // Use performant bulk update for each selected centre
        String jpql = "UPDATE Institution i SET i.feeListType = :feeList, i.editer = :user, i.editedAt = :date WHERE i.id = :id";
        
        for (CollectingCentreFeeAssignmentDTO dto : selectedCollectingCentres) {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("feeList", selectedFeeListForAssignment);
                params.put("user", sessionController.getLoggedUser());
                params.put("date", new Date());
                params.put("id", dto.getInstitutionId());
                
                // Use the new performant update method from AbstractFacade
                int updatedCount = institutionFacade.updateByJpql(jpql, params);
                
                if (updatedCount > 0) {
                    // Update the DTO to reflect the change
                    dto.setCurrentFeeListType(selectedFeeListForAssignment);
                    totalUpdatedCount += updatedCount;
                } else {
                    JsfUtil.addErrorMessage("Institution not found or could not be updated: " + dto.getInstitutionName());
                }
            } catch (Exception e) {
                JsfUtil.addErrorMessage("Failed to update " + dto.getInstitutionName() + ": " + e.getMessage());
            }
        }
        
        if (totalUpdatedCount > 0) {
            JsfUtil.addSuccessMessage("Successfully assigned fee list '" + selectedFeeListForAssignment.getName() + "' to " + totalUpdatedCount + " collecting centre(s)");
            // Clear selections
            selectedCollectingCentres = new ArrayList<>();
        } else {
            JsfUtil.addErrorMessage("No collecting centres were updated");
        }
    }

    // Getters and setters for fee assignment functionality
    
    public List<CollectingCentreFeeAssignmentDTO> getCollectingCentresForFeeAssignment() {
        return collectingCentresForFeeAssignment;
    }

    public void setCollectingCentresForFeeAssignment(List<CollectingCentreFeeAssignmentDTO> collectingCentresForFeeAssignment) {
        this.collectingCentresForFeeAssignment = collectingCentresForFeeAssignment;
    }

    public List<CollectingCentreFeeAssignmentDTO> getFilteredCollectingCentres() {
        return filteredCollectingCentres;
    }

    public void setFilteredCollectingCentres(List<CollectingCentreFeeAssignmentDTO> filteredCollectingCentres) {
        this.filteredCollectingCentres = filteredCollectingCentres;
    }

    public List<CollectingCentreFeeAssignmentDTO> getSelectedCollectingCentres() {
        return selectedCollectingCentres;
    }

    public void setSelectedCollectingCentres(List<CollectingCentreFeeAssignmentDTO> selectedCollectingCentres) {
        this.selectedCollectingCentres = selectedCollectingCentres;
    }

    public String getGlobalFilterValue() {
        return globalFilterValue;
    }

    public void setGlobalFilterValue(String globalFilterValue) {
        this.globalFilterValue = globalFilterValue;
    }

    public Category getSelectedFeeListForAssignment() {
        return selectedFeeListForAssignment;
    }

    public void setSelectedFeeListForAssignment(Category selectedFeeListForAssignment) {
        this.selectedFeeListForAssignment = selectedFeeListForAssignment;
    }

}
