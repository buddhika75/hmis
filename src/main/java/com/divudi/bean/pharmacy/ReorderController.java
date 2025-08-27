package com.divudi.bean.pharmacy;

import com.divudi.bean.common.DepartmentController;
import com.divudi.bean.common.ItemController;
import com.divudi.bean.common.SessionController;

import com.divudi.core.data.BillType;
import com.divudi.core.data.DepartmentListMethod;
import static com.divudi.core.data.DepartmentListMethod.AllDepartmentsOfLoggedInstitution;
import com.divudi.core.data.DepartmentType;
import com.divudi.core.data.dataStructure.ItemReorders;
import com.divudi.core.data.dataStructure.ItemTransactionSummeryRow;

import com.divudi.ejb.PharmacyBean;
import com.divudi.core.entity.Bill;
import com.divudi.core.entity.BillItem;
import com.divudi.core.entity.Department;
import com.divudi.core.entity.Institution;
import com.divudi.core.entity.Item;
import com.divudi.core.entity.Person;
import com.divudi.core.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.core.entity.pharmacy.Reorder;
import com.divudi.core.entity.pharmacy.StockHistory;
import com.divudi.core.facade.ReorderFacade;
import com.divudi.core.util.JsfUtil;
import com.divudi.core.entity.pharmacy.Amp;
import com.divudi.core.util.CommonFunctions;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TemporalType;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.primefaces.event.RowEditEvent;

@Named
@SessionScoped
public class ReorderController implements Serializable {

//    Controllers
    @Inject
    AmpController ampController;
    @Inject
    StockController stockController;
    @Inject
    TransferRequestController transferRequestController;
    @Inject
    PharmacyController pharmacyController;
    @Inject
    SessionController sessionController;
    @Inject
    DepartmentController departmentController;
    @Inject
    ItemController itemController;
    @Inject
    PurchaseOrderRequestController purchaseOrderRequestController;

//    EJBs
    @EJB
    ReorderFacade ejbFacade;
    @EJB
    ReorderFacade reorderFacade;
    @EJB
    PharmacyBean pharmacyBean;

//    Attributes
    private Reorder current;
    private List<Reorder> items = null;
    List<Reorder> departmentReorders;
    Department department;
    Department fromDepartment;
    Department toDepartment;
    Institution institution;
    Person person;
    Date fromDate;
    Date toDate;
    Date orderingDate;
    Date expectedDeliveryDate;
    Date nextDeliveryDate;
    List<Reorder> reorders;
    List<ItemReorders> itemReorders;
    List<Reorder> userSelectedReorders;
    List<Item> selectableItems;
    List<Item> userSelectedItems;
    AutoOrderMethod autoOrderMethod;
    boolean readOnly = true;
    BillType[] billTypes;
    DepartmentListMethod departmentListMethod;
    List<Reorder> reordersAvailableForSelection;

    Item item;
    Department historyDept;

//    private CartesianChartModel dateModel;

    public String navigateReorderManagement() {
        return "/pharmacy/reorder_management?faces-redirect=true";
    }

    public void updateReorder(Reorder ro) {
        if (ro == null) {
            return;
        }
        save(ro);
    }

    public List<Reorder> fillReordersBySelectedDepartment() {
        reorders = null;
        Map m = new HashMap();
        String sql = "select r from Reorder r where r.department=:dep";
        m.put("dep", department);
        reorders = reorderFacade.findByJpql(sql, m);
        return reorders;

    }

    public List<Reorder> fillReordersBySelectedInstitution() {
        reorders = null;
        Map m = new HashMap();
        String sql = "select r from Reorder r where r.institution=:ins";
        m.put("ins", institution);
        reorders = reorderFacade.findByJpql(sql, m);
        return reorders;
    }

    public boolean isAmpHaveReorder(Amp amp, Department dept, Institution ins) {
        List<Reorder> r;
        Map m = new HashMap();
        String sql = "select r from Reorder r where r.item=:amp";
        m.put("amp", amp);

        if (dept != null) {
            sql += " and r.department=:dep";
            m.put("dep", dept);
        }

        if (ins != null) {
            sql += " and r.institution=:ins";
            m.put("ins", ins);
        }

        r = reorderFacade.findByJpql(sql, m);
        return !r.isEmpty();
    }

    public void removeReOrder(Reorder ro) {
        if (ro != null) {
            reorderFacade.remove(ro);
        }

    }

    public void createReOrdersByDepartment() {
        List<Amp> amps;
        amps = getAmpController().findItems();
        for (Amp amp : amps) {
            if (isAmpHaveReorder(amp, department, null) == false) {
                Reorder ro = new Reorder();
                ro.setDepartment(department);
                ro.setItem(amp);
                reorderFacade.create(ro);
            }
        }
    }

    public void createReOrdersByInstituion() {
        List<Amp> amps = getAmpController().findItems();
        for (Amp amp : amps) {
            if (isAmpHaveReorder(amp, null, institution) == false) {
                Reorder ro = new Reorder();
                ro.setInstitution(institution);
                ro.setItem(amp);
                reorderFacade.create(ro);
            }
        }
    }

//    public CartesianChartModel getDateModel() {
//        return dateModel;
//    }

    public void createDailyItemSummery() {
        createDailyItemSummery(item, historyDept, fromDate, toDate);
    }

    public void createDailyItemSummery(Item item, Department dept) {
        this.item = item;
        this.historyDept = dept;
        createDailyItemSummery(item, dept, fromDate, toDate);
    }

    public void createDailyItemSummery(Item item, Department dept, Date fromDate, Date toDate) {
//        dateModel = new CartesianChartModel();
//        List<ItemTransactionSummeryRow> rows;
//
//        LineChartSeries series1 = new LineChartSeries();
//        series1.setLabel("Stock Average");
//        rows = findDailyStockAverage(item, dept, fromDate, toDate);
//        for (ItemTransactionSummeryRow r : rows) {
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//            series1.set(df.format(r.getDate()), r.getQuantity());
//        }
//        dateModel.addSeries(series1);
//
//        LineChartSeries series2 = new LineChartSeries();
//        series2.setLabel("Sales");
//        rows = findDailySale(item, dept, fromDate, toDate);
//        for (ItemTransactionSummeryRow r : rows) {
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//            series2.set(df.format(r.getDate()), r.getQuantity());
//        }
//        dateModel.addSeries(series2);
//
//        LineChartSeries series3 = new LineChartSeries();
//        series3.setLabel("Purchase/Good Receive");
//        rows = findDailyPurchase(item, dept, fromDate, toDate);
//        for (ItemTransactionSummeryRow r : rows) {
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//            series3.set(df.format(r.getDate()), r.getQuantity());
//        }
//        dateModel.addSeries(series3);
//
//        LineChartSeries series4 = new LineChartSeries();
//        series4.setLabel("Transfer Issue");
//        rows = findDailyTransferOut(item, dept, fromDate, toDate);
//        for (ItemTransactionSummeryRow r : rows) {
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//            series4.set(df.format(r.getDate()), r.getQuantity());
//        }
//        dateModel.addSeries(series4);
//
//        LineChartSeries series5 = new LineChartSeries();
//        series5.setLabel("Transfer Receive");
//        rows = findDailyTransferIn(item, dept, fromDate, toDate);
//        for (ItemTransactionSummeryRow r : rows) {
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//            series5.set(df.format(r.getDate()), r.getQuantity());
//        }
//        dateModel.addSeries(series5);
//
////        dateModel.setTitle("Item Transactions");
////        dateModel.setZoom(true);
////        dateModel.setLegendPlacement(LegendPlacement.INSIDE);
////        dateModel.setLegendPosition("ne");
////        dateModel.getAxis(AxisType.Y).setLabel("Stock");
////        DateAxis axis = new DateAxis("Dates");
////        axis.setTickAngle(-50);
//////        axis.setMax("2014-02-01");
////        axis.setTickFormat("%b %#d, %y");
////        dateModel.getAxes().put(AxisType.X, axis);
    }

    public DepartmentListMethod getDepartmentListMethod() {
        if (departmentListMethod == null) {
            departmentListMethod = AllDepartmentsOfLoggedInstitution;
        }
        return departmentListMethod;
    }

    public void setDepartmentListMethod(DepartmentListMethod departmentListMethod) {
        this.departmentListMethod = departmentListMethod;
    }

    public Department getToDepartment() {
        return toDepartment;
    }

    public void setToDepartment(Department toDepartment) {
        this.toDepartment = toDepartment;
    }

    public Department getFromDepartment() {
        if (fromDepartment == null) {
            fromDepartment = sessionController.getDepartment();
        }
        return fromDepartment;
    }

    public void setFromDepartment(Department fromDepartment) {
        this.fromDepartment = fromDepartment;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Date getNextDeliveryDate() {
        if (nextDeliveryDate == null) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 17);
            nextDeliveryDate = c.getTime();
        }
        return nextDeliveryDate;
    }

    public void setNextDeliveryDate(Date nextDeliveryDate) {
        this.nextDeliveryDate = nextDeliveryDate;
    }

    public Date getOrderingDate() {
        if (orderingDate == null) {
            orderingDate = new Date();
        }
        return orderingDate;
    }

    public void setOrderingDate(Date orderingDate) {
        this.orderingDate = orderingDate;
    }

    public Date getExpectedDeliveryDate() {
        if (expectedDeliveryDate == null) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 3);
            expectedDeliveryDate = c.getTime();
        }
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(Date expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public void findDailyStockAverage() {
        findDailyStockAverage(item, historyDept, fromDate, toDate);
    }

    public void findDailyStockAverage(Item item, Department dept) {
        this.item = item;
        this.historyDept = dept;
        findDailyStockAverage(item, dept, fromDate, toDate);
    }

    public List<ItemTransactionSummeryRow> findDailyStockAverage(Item item, Department dept, Date fd, Date td) {
        String jpql;
        List<ItemTransactionSummeryRow> rows;
        jpql = "SELECT new com.divudi.core.data.dataStructure.ItemTransactionSummeryRow"
                + "(s.item, avg(s.stockQty), FUNC('DATE',s.createdAt)) "
                + " FROM StockHistory s "
                + " WHERE s.createdAt between :fd and :td "
                + " and s.item=:item ";
        Map temMap = new HashMap();
        temMap.put("fd", fd);
        temMap.put("td", td);
        temMap.put("item", item);
        if (dept != null) {
            jpql += " and s.department=:d ";
            temMap.put("d", dept);
        }
        StockHistory sh = new StockHistory();
        sh.getItem();
        sh.getStockQty();
        sh.getDepartment();
        sh.getCreatedAt();

        jpql += " group by FUNC('DATE',s.createdAt) "
                + "order by FUNC('DATE',s.createdAt)  ";

        List<Object[]> dsso = ejbFacade.findAggregates(jpql, temMap, TemporalType.DATE);

        if (dsso == null) {
            dsso = new ArrayList<>();
            //    //// // System.out.println("new list as null");
        }
        rows = new ArrayList<>();
        for (Object b : dsso) {
            ItemTransactionSummeryRow dsr = (ItemTransactionSummeryRow) b;
            rows.add(dsr);
        }
        return rows;
    }

    public List<ItemTransactionSummeryRow> findDailySale(Item item, Department dept, Date fd, Date td) {
        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.PharmacySale);
        return findDailyTransactions(item, dept, fd, td, bts);
    }

    public List<ItemTransactionSummeryRow> findDailyPurchase(Item item, Department dept, Date fd, Date td) {
        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.PharmacyPurchaseBill);
        bts.add(BillType.PharmacyGrnBill);
        return findDailyTransactions(item, dept, fd, td, bts);
    }

    public List<ItemTransactionSummeryRow> findDailyTransferIn(Item item, Department dept, Date fd, Date td) {
        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.PharmacyTransferReceive);
        return findDailyTransactions(item, dept, fd, td, bts);
    }

    public List<ItemTransactionSummeryRow> findDailyTransferOut(Item item, Department dept, Date fd, Date td) {
        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.PharmacyTransferIssue);
        return findDailyTransactions(item, dept, fd, td, bts);
    }

    public List<ItemTransactionSummeryRow> findDailyTransactions(Item item, Department dept, Date fd, Date td, List<BillType> billTypes) {
        String jpql;
        List<ItemTransactionSummeryRow> rows;
//        if (false) {
//            BillItem bi = new BillItem();
//            bi.getQty();
//            bi.getItem();
//        }
        jpql = "SELECT new com.divudi.core.data.dataStructure.ItemTransactionSummeryRow(s.item, sum(s.qty), FUNC('DATE',s.bill.createdAt)) "
                + " FROM BillItem s "
                + " WHERE s.bill.createdAt between :fd and :td "
                + " and s.item=:item ";
        Map temMap = new HashMap();
        temMap.put("fd", fd);
        temMap.put("td", td);
        temMap.put("item", item);
        if (dept != null) {
            jpql += " and s.bill.department=:d ";
            temMap.put("d", dept);
        }
        jpql += " and s.bill.billType in :bts ";
        temMap.put("bts", billTypes);
        StockHistory sh = new StockHistory();
        sh.getItem();
        sh.getStockQty();
        sh.getDepartment();
        sh.getCreatedAt();

        jpql += " group by FUNC('DATE',s.bill.createdAt) "
                + "order by FUNC('DATE',s.bill.createdAt)  ";

        List<Object[]> dsso = ejbFacade.findAggregates(jpql, temMap, TemporalType.DATE);

        if (dsso == null) {
            dsso = new ArrayList<>();
            //    //// // System.out.println("new list as null");
        }
        rows = new ArrayList<>();
        for (Object b : dsso) {
            ItemTransactionSummeryRow dsr = (ItemTransactionSummeryRow) b;
            dsr.setQuantity(Math.abs(dsr.getQuantity()));
            rows.add(dsr);
        }
        return rows;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.YEAR, -1);
            fromDate = c.getTime();
//            fromDate = new Date();//request by Mr.Mahinda
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = new Date();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public BillType[] getBillTypes() {
        if (billTypes == null) {
            billTypes = new BillType[]{BillType.PharmacySale};
        }
        return billTypes;
    }

    public void setBillTypes(BillType[] billTypes) {
        this.billTypes = billTypes;
    }

    public void fillReorders() {
        Date startTime = new Date();

        generateReorders(false);



    }

    public void fillReordersForRequiredItems() {
        Date startTime = new Date();

        generateReorders(false, true);


    }

    public List<Reorder> getReorders() {
        return reorders;
    }

    public void setReorders(List<Reorder> reorders) {
        this.reorders = reorders;
    }

    public List<ItemReorders> getItemReorders() {
        if (itemReorders == null) {
            itemReorders = new ArrayList<>();
        }
        return itemReorders;
    }

    public void setItemReorders(List<ItemReorders> itemReorders) {
        this.itemReorders = itemReorders;
    }

    public void listAllItems() {
        String j;
        Map m = new HashMap();
//        if (false) {
//            Reorder r = new Reorder();
//            r.getDepartment();
//            r.getRol();
//        }
        j = "select r from Reorder r where r.department=:dept ";
        m.put("dept", getDepartment());
        reordersAvailableForSelection = new ArrayList<>();
        reordersAvailableForSelection = ejbFacade.findByJpql(j, m);
        userSelectedItems = new ArrayList<>();
        selectableItems = new ArrayList<>();
        for (Reorder r : reordersAvailableForSelection) {
            double s = pharmacyBean.getStockQty(r.getItem(), department);
            r.setTransientStock(s);
            selectableItems.add(r.getItem());
        }
    }

    public void listItemsBelowRol() {
        String j;
        Map m = new HashMap();
//        if (false) {
//            Reorder r = new Reorder();
//            r.getDepartment();
//            r.getRol();
//        }
        j = "select r from Reorder r where r.department=:dept ";
        m.put("dept", getDepartment());
        reordersAvailableForSelection = new ArrayList<>();
        List<Reorder> lst = ejbFacade.findByJpql(j, m);
        userSelectedItems = new ArrayList<>();
        selectableItems = new ArrayList<>();
        for (Reorder r : lst) {

            double s = pharmacyBean.getStockQty(r.getItem(), department);
            if (r.getRol() >= s) {
                r.setTransientStock(s);
                reordersAvailableForSelection.add(r);
                selectableItems.add(r.getItem());
            }
        }
    }

    public void generateReorders() {
        Date startTime = new Date();

        generateReorders(true, false, departmentListMethod);



    }

    public void generateReorders(boolean overWrite) {
        generateReorders(overWrite, false, departmentListMethod);
    }

    public void generateReorders(boolean overWrite, boolean requiredItemOnly) {
        generateReorders(overWrite, requiredItemOnly, departmentListMethod);
    }

    public List<Department> getActiveDepartments(Institution ins, boolean includeAllInstitutionDepartmentsIfInstitutionIsNull) {
        Map<Long, Department> ds;
        ds = new HashMap<>();
        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.PharmacyAdjustment);
        bts.add(BillType.PharmacyBhtPre);
        bts.add(BillType.PharmacyGrnBill);
        bts.add(BillType.PharmacyGrnReturn);
        bts.add(BillType.PharmacyIssue);
        bts.add(BillType.PharmacyPre);
        bts.add(BillType.PharmacyPurchaseBill);
        bts.add(BillType.PharmacyReturnWithoutTraising);
        bts.add(BillType.PharmacyTransferIssue);
        bts.add(BillType.PharmacySale);
        bts.add(BillType.PharmacyTransferIssue);
        bts.add(BillType.PharmacyTransferReceive);
        //// // System.out.println("bts = " + bts);
        String sql = "Select b.department from "
                + " Bill b "
                + " where b.retired=false "
                + " and b.billType in :bts "
                + " and b.createdAt between :fd and :td ";
        Map m = new HashMap();
        if (ins == null) {
            if (!includeAllInstitutionDepartmentsIfInstitutionIsNull) {
                return new ArrayList<>();
            }
        } else {
            sql += " and (b.institution=:ins or b.department.institution=:ins) ";
            m.put("ins", ins);
        }
        sql += " group by b.department";

        m.put("bts", bts);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        List<Department> depst = departmentController.getDepartments(sql, m);

        //// // System.out.println("m = " + m);
//        if (false) {
//            Stock s = new Stock();
//            s.getDepartment();
//            s.getItemBatch().getItem().setDepartmentType(DepartmentType.Pharmacy);
//        }
        sql = "select s.department from Stock s "
                + " where s.stock > 0 "
                + " and (s.itemBatch.item.departmentType=:dt or s.itemBatch.item.departmentType is null) ";
        m = new HashMap();
        if (ins == null) {
            if (!includeAllInstitutionDepartmentsIfInstitutionIsNull) {
                return new ArrayList<>();
            }
        } else {
            sql += " and s.department.institution=:ins ";
            m.put("ins", ins);
        }
        sql += " group by s.department";

        m.put("dt", DepartmentType.Pharmacy);
        List<Department> depss = departmentController.getDepartments(sql, m);

        for (Department d : depst) {
            ds.put(d.getId(), d);
        }
        for (Department d : depss) {
            ds.put(d.getId(), d);
        }

        List<Department> deps = new ArrayList<>(ds.values());

        return deps;
    }

    private void save(Reorder ro) {
        if (ro == null) {
            return;
        }
        if(ro.getId()==null) {
            reorderFacade.create(ro);
        }else{
            reorderFacade.edit(ro);
        }
    }

    enum AutoOrderMethod {

        ByDistributor,
        ByRol,
        ByAll,
        ByGeneric,
    }

    public String autoOrderByDistributor() {
        autoOrderMethod = AutoOrderMethod.ByDistributor;
        return "/pharmacy/auto_ordering_by_distributor";
    }

    public String autoOrderByRol() {
        autoOrderMethod = AutoOrderMethod.ByDistributor;
        return "/pharmacy/auto_ordering_by_items_below_rol";
    }

    public String autoOrderByAllItems() {
        autoOrderMethod = AutoOrderMethod.ByAll;
        return "/pharmacy/auto_ordering_by_all_items";
    }

    public String autoOrderByGenerics() {
        autoOrderMethod = AutoOrderMethod.ByGeneric;
        return "/pharmacy/auto_ordering_by_items_by_generic";
    }

    List<Item> listedItems;

    public List<Item> getListedItems() {
        return listedItems;
    }

    public void setListedItems(List<Item> listedItems) {
        this.listedItems = listedItems;
    }

    public List<Item> getUserSelectedItems() {
        return userSelectedItems;
    }

    public void setUserSelectedItems(List<Item> userSelectedItems) {
        this.userSelectedItems = userSelectedItems;
    }

    public void generateUserSelectedItemsFromUserSelectedReorders() {
        userSelectedItems = new ArrayList<>();
        for (Reorder r : userSelectedReorders) {
            userSelectedItems.add(r.getItem());
        }
    }

    private void generateReorders(boolean overWrite, boolean requiredItemsOnly, DepartmentListMethod departmentListMethod) {
        List<Item> iss;

        if (autoOrderMethod == AutoOrderMethod.ByDistributor) {
            itemController.setInstitution(institution);
            iss = itemController.getDealorItem();
        } else if (autoOrderMethod == AutoOrderMethod.ByRol) {
            itemController.setInstitution(institution);
//            generateUserSelectedItemsFromUserSelectedReorders();
            iss = userSelectedItems;
        } else {
            itemController.setInstitution(institution);
//            generateUserSelectedItemsFromUserSelectedReorders();
            iss = userSelectedItems;
        }
        itemReorders = new ArrayList<>();
        int days = ((Long) ((toDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24))).intValue();
        List<Department> deps = null;
        switch (departmentListMethod) {
            case AllPharmaciesOfLoggedInstitution:
                deps = departmentController.getInstitutionDepatrments(sessionController.getInstitution(), DepartmentType.Pharmacy);
                break;
            case AllPharmaciesOfAllInstitutions:
                deps = departmentController.getInstitutionDepatrments(DepartmentType.Pharmacy);
                break;
            case ActiveDepartmentsOfAllInstitutions:
                deps = getActiveDepartments(null, true);
                break;
            case ActiveDepartmentsOfLoggedInstitution:
                deps = getActiveDepartments(institution, true);
                break;
            case AllDepartmentsOfAllInstitutions:
                deps = departmentController.getItems();
                break;
            case AllDepartmentsOfLoggedInstitution:
                deps = departmentController.getInstitutionDepatrments(sessionController.getInstitution());
                break;
            case LoggedDepartmentOnly:
                deps = new ArrayList<>();
                deps.add(sessionController.getDepartment());
                break;
            default:
                deps = new ArrayList<>();
        }

        for (Item i : iss) {
            ItemReorders ir = new ItemReorders();
            ir.setItem(i);
            int temNo = 0;
            for (Department dept : deps) {
                Reorder r = findReorder(dept, i);
                double useQty = pharmacyController.findPharmacyMovement(dept, i, billTypes, fromDate, toDate);
                Date firstDate = pharmacyController.findFirstPharmacyMovementDate(dept, i, billTypes, fromDate, toDate);

                if (firstDate.getTime() > fromDate.getTime()) {
                    int tdays = ((Long) ((toDate.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24))).intValue();
                    r.setDemandInUnitsPerDay(0 - (useQty / tdays));
                } else {
                    r.setDemandInUnitsPerDay(0 - (useQty / days));
                }

                r.setDemandInUnitsPerDay(CommonFunctions.roundToTwoDecimalPlaces(r.getDemandInUnitsPerDay()));
                r.setTransientStock(stockController.departmentItemStock(dept, i));

                if (r.getPurchaseCycleDurationInDays() == 0 || overWrite) {
                    r.setPurchaseCycleDurationInDays(((Long) ((nextDeliveryDate.getTime() - expectedDeliveryDate.getTime()) / (1000 * 60 * 60 * 24))).intValue());
                }

                if (temNo == 1) {
                    //    //// // System.out.println("nextDeliveryDate = " + nextDeliveryDate);
                    //    //// // System.out.println("nextDeliveryDate.getTime() = " + nextDeliveryDate.getTime());
                    //    //// // System.out.println("expectedDeliveryDate = " + expectedDeliveryDate);
                    //    //// // System.out.println("expectedDeliveryDate.getTime() = " + expectedDeliveryDate.getTime());
                    //    //// // System.out.println("nextDeliveryDate.getTime() - expectedDeliveryDate.getTime() = " + (nextDeliveryDate.getTime() - expectedDeliveryDate.getTime()));
                    //    //// // System.out.println("(nextDeliveryDate.getTime() - expectedDeliveryDate.getTime()) / (1000 * 60 * 60 * 24) = " + (nextDeliveryDate.getTime() - expectedDeliveryDate.getTime()) / (1000 * 60 * 60 * 24));
                    //    //// // System.out.println("((int) (nextDeliveryDate.getTime() - expectedDeliveryDate.getTime()) / (1000 * 60 * 60 * 24)) = " + ((int) (nextDeliveryDate.getTime() - expectedDeliveryDate.getTime()) / (1000 * 60 * 60 * 24)));
                    //    //// // System.out.println("((int) ((nextDeliveryDate.getTime() - expectedDeliveryDate.getTime()) / (1000 * 60 * 60 * 24))) = " + ((int) ((nextDeliveryDate.getTime() - expectedDeliveryDate.getTime()) / (1000 * 60 * 60 * 24))));
                    //    //// // System.out.println("((Long) ((nextDeliveryDate.getTime() - expectedDeliveryDate.getTime()) / (1000 * 60 * 60 * 24))).intValue() = " + ((Long) ((nextDeliveryDate.getTime() - expectedDeliveryDate.getTime()) / (1000 * 60 * 60 * 24))).intValue());
                }
                temNo++;

                if (r.getLeadTimeInDays() == 0 || overWrite) {
                    r.setLeadTimeInDays(((Long) ((expectedDeliveryDate.getTime() - orderingDate.getTime()) / (1000 * 60 * 60 * 24))).intValue());
                }
                if (r.getBufferStocks() == 0 || overWrite) {
                    r.setBufferStocks(CommonFunctions.roundToTwoDecimalPlaces(r.getDemandInUnitsPerDay() * r.getPurchaseCycleDurationInDays(), 0));
                }

                if (r.getRol() == 0 || overWrite) {
                    r.setRol(CommonFunctions.roundToTwoDecimalPlaces(r.getBufferStocks() + (r.getDemandInUnitsPerDay() * r.getLeadTimeInDays()), 0));
                }

                if (r.getRoq() == 0 || overWrite) {
                    r.setRoq(CommonFunctions.roundToTwoDecimalPlaces(r.getDemandInUnitsPerDay() * r.getPurchaseCycleDurationInDays(), 0));
                }

                r.setTransientOrderingQty(CommonFunctions.roundToTwoDecimalPlaces((r.getBufferStocks() - r.getTransientStock() + (r.getDemandInUnitsPerDay() * (r.getLeadTimeInDays() + r.getPurchaseCycleDurationInDays()))), 0));

//                if (r.getTransientOrderingQty() < 0) {
//                    r.setTransientOrderingQty(0.0);
//                }
                if (requiredItemsOnly) {
                    if (r.getTransientOrderingQty() > 0) {
                        ir.getReorders().add(r);
                    }
                } else {
                    ir.getReorders().add(r);
                }
            }
            itemReorders.add(ir);
        }
    }

    public Reorder findReorder(Department dept, Item item) {
        String sql;
        Map m = new HashMap();
        m.put("dept", dept);
        m.put("item", item);
        sql = "select r from Reorder r where r.department=:dept and r.item=:item";
        Reorder r = reorderFacade.findFirstByJpql(sql, m);
        if (r == null) {
            r = new Reorder();
            r.setDepartment(dept);
            r.setItem(item);
            reorderFacade.create(r);
        }
        return r;
    }

    public void saveReorders() {
        Date startTime = new Date();
        Date fromDate = null;
        Date toDate = null;

        for (ItemReorders ir : itemReorders) {
            for (Reorder r : ir.getReorders()) {
                reorderFacade.edit(r);
            }
        }
        JsfUtil.addSuccessMessage("Saved.");



    }

    public void onEdit(RowEditEvent event) {

        Reorder tmp = (Reorder) event.getObject();
        getEjbFacade().edit(tmp);
        JsfUtil.addSuccessMessage("Reorder Level Updted");
    }

    public List<Item> getSelectableItems() {
        return selectableItems;
    }

    public void setSelectableItems(List<Item> selectableItems) {
        this.selectableItems = selectableItems;
    }

    public String createPharmacyOrderRequest() {
        if (department == null) {
            JsfUtil.addErrorMessage("Please select a department");
            return "";
        }
        if (!department.equals(sessionController.getDepartment())) {
            JsfUtil.addErrorMessage("Please re-login to that department to create an order");
            sessionController.setLogged(false);
            sessionController.setDepartment(department);
            return "";
        }
        purchaseOrderRequestController.resetBillValues();
        purchaseOrderRequestController.getCurrentBill().setToInstitution(itemController.getInstitution());
        pharmacyController.setFromDate(fromDate);
        pharmacyController.setToDate(toDate);
        generatePharmacyOrderBillComponents();
        return "/pharmacy/pharmacy_purhcase_order_request";
    }

    public String createPharmacyTransferRequest() {
        if (fromDepartment == null) {
            JsfUtil.addErrorMessage("Please select a department");
            return "";
        }
        if (!fromDepartment.equals(sessionController.getDepartment())) {
            JsfUtil.addErrorMessage("Please re-login to that department to create an order");
            sessionController.setLogged(false);
            sessionController.setDepartment(fromDepartment);
            return "";
        }
        transferRequestController.recreate();
        transferRequestController.getBill().setToDepartment(toDepartment);
        transferRequestController.getBill().setFromDepartment(fromDepartment);
        pharmacyController.setFromDate(fromDate);
        pharmacyController.setToDate(toDate);
        generatePharmacyTransferRequestBillComponents();
        return "/pharmacy_transfer_request";
    }

    private void generatePharmacyTransferRequestBillComponents() {
        purchaseOrderRequestController.setBillItems(new ArrayList<BillItem>());
        //    //// // System.out.println("fromDepartment = " + fromDepartment);
        //    //// // System.out.println("toDepartment = " + toDepartment);
        for (ItemReorders i : itemReorders) {
            //    //// // System.out.println("i.getItem().getName() = " + i.getItem().getName());
            Reorder fromReorder = new Reorder();
            Reorder toReorder = new Reorder();

            for (Reorder r : i.getReorders()) {
                if (r.getDepartment().equals(fromDepartment)) {
                    //    //// // System.out.println("from");
                    //    //// // System.out.println("r.getTransientOrderingQty() = " + r.getTransientOrderingQty());
                    fromReorder = r;
                }
                if (r.getDepartment().equals(toDepartment)) {
                    //    //// // System.out.println("to");
                    //    //// // System.out.println("r.getTransientStock() = " + r.getTransientStock());
                    toReorder = r;
                }
            }
            //    //// // System.out.println("toReorder.getTransientOrderingQty() = " + toReorder.getTransientOrderingQty());

            if (fromReorder.getTransientOrderingQty() <= 0) {
                continue;
            }

            double availableToRequest;
            double requestingQty;

            availableToRequest = toReorder.getTransientStock() - (toReorder.getTransientOrderingQty() + toReorder.getBufferStocks());
            //    //// // System.out.println("availableToRequest = " + availableToRequest);
            if (availableToRequest > fromReorder.getTransientOrderingQty()) {
                requestingQty = fromReorder.getTransientOrderingQty();
            } else {
                requestingQty = availableToRequest;
            }
            //    //// // System.out.println("requestingQty = " + requestingQty);
            transferRequestController.getCurrentBillItem().setItem(i.getItem());
            transferRequestController.getCurrentBillItem().setTmpQty(requestingQty);
            transferRequestController.addItem();
        }
    }

    private void generatePharmacyOrderBillComponents() {
        purchaseOrderRequestController.setBillItems(new ArrayList<BillItem>());
        for (ItemReorders i : itemReorders) {
            BillItem bi = new BillItem();
            for (Reorder r : i.getReorders()) {
                if (r.getDepartment().equals(department)) {
                    if (r.getTransientOrderingQty() > 0) {
                        bi.setItem(i.getItem());
                        PharmaceuticalBillItem tmp = new PharmaceuticalBillItem();
                        tmp.setBillItem(bi);
                        tmp.setQty(CommonFunctions.roundToTwoDecimalPlaces(r.getTransientOrderingQty(), 0));
                        tmp.setPurchaseRateInUnit(getPharmacyBean().getLastPurchaseRate(bi.getItem(), getSessionController().getDepartment()));
                        tmp.setRetailRateInUnit(getPharmacyBean().getLastRetailRate(bi.getItem(), getSessionController().getDepartment()));
                        bi.setTmpQty(CommonFunctions.roundToTwoDecimalPlaces(r.getTransientOrderingQty(), 0));
                        bi.setPharmaceuticalBillItem(tmp);
                        purchaseOrderRequestController.getBillItems().add(bi);
                    }
                }
            }
        }
        purchaseOrderRequestController.calculateBillTotals();
    }

    public double calculateRoq(Reorder reorder) {
        int numberOfDaysToOrder = Math.max(reorder.getPurchaseCycleDurationInDays(), reorder.getLeadTimeInDays());
        return numberOfDaysToOrder * reorder.getDemandInUnitsPerDay();
    }

    public double calculateRol(Reorder reorder) {
        int numberOfDaysToOrder = Math.max(reorder.getPurchaseCycleDurationInDays(), reorder.getLeadTimeInDays());
        return numberOfDaysToOrder * reorder.getDemandInUnitsPerDay();
    }

    public int calculateLeadTime(Reorder reorder) {
        String jpql;
        Map m = new HashMap();
        DateTime dt = new DateTime();
        DateTime tfd = dt.minusMonths(reorder.getMonthsConsideredForShortTermAnalysis());
        Date fd = tfd.toDate();
        Date td = new Date();

        BillItem bi = new BillItem();
        bi.getReferanceBillItem();

        jpql = "Select b, rb "
                + " from BillItem bi "
                + " join bi.bill b "
                + " join bi.referanceBillItem rbi "
                + " join rbi.bill rb "
                + " where b.billType in :bts "
                + " and rb.billType in :rbts "
                + " and bi.item=:amp "
                + " and b.createdAt between :fd and :td "
                + " ";

        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.PharmacyOrderApprove);

        List<BillType> rbts = new ArrayList<>();
        bts.add(BillType.PharmacyGrnBill);

        m.put("bts", bts);
        m.put("rbts", rbts);
        m.put("amp", reorder.getItem());
        m.put("fd", fd);
        m.put("td", td);
        List<Object[]> obj = ejbFacade.findAggregates(jpql, m);

        if (obj == null) {
            return 7;
        }

        int count = 0;
        long differenceInMs = 0L;
        for (Object[] objc : obj) {
            Bill b = (Bill) objc[0];
            //    //// // System.out.println("b = " + b);
            Bill rf = (Bill) objc[1];
            //    //// // System.out.println("rf = " + rf);
            count++;
            //    //// // System.out.println("count = " + count);
            differenceInMs = differenceInMs + (rf.getCreatedAt().getTime() - b.getCreatedAt().getTime());
            //    //// // System.out.println("differenceInMs = " + differenceInMs);
        }

        int avgLeadTimeInDays;

        try {
            long avgLeadTimeInMs = differenceInMs / count;
            avgLeadTimeInDays = ((Long) (avgLeadTimeInMs / (1000 * 60 * 60 * 24))).intValue();
        } catch (Exception e) {
            avgLeadTimeInDays = 7;
        }
        return avgLeadTimeInDays;
    }

    public double calculateDailyDemandInUnits(Reorder reorder) {
        //    //// // System.out.println("Calculate daily demand in Units - reorder = " + reorder);
        String jpql;
        Map m = new HashMap();
        DateTime dt = new DateTime();
        DateTime tfd = dt.minusMonths(reorder.getMonthsConsideredForShortTermAnalysis());
        Date fd = tfd.toDate();
        Date td = new Date();

        List<BillType> bts = new ArrayList<>();

        jpql = "Select max(bi.bill.createdAt), min(bi.bill.createdAt), sum(bi.qty) "
                + " from BillItem bi "
                + " where bi.bill.billType in :bts "
                + " and bi.item=:amp "
                + " and bi.bill.createdAt between :fd and :td ";

        bts.add(BillType.PharmacyAdjustment);
        bts.add(BillType.PharmacyPre);
        bts.add(BillType.PharmacyBhtPre);
        bts.add(BillType.PharmacyIssue);
        bts.add(BillType.PharmacyTransferIssue);
        m.put("bts", bts);
        m.put("amp", reorder.getItem());
        m.put("fd", fd);
        m.put("td", td);
        //    //// // System.out.println("m = " + m);
        //    //// // System.out.println("jpql = " + jpql);
        Object[] obj = ejbFacade.findSingleAggregate(jpql, m);
        //    //// // System.out.println("obj = " + obj);
        if (obj == null) {
            return 14;
        }
        Date minDate;
        Date maxDate;
        double totalQty;

        try {
            //    //// // System.out.println(" obj[0] = " + obj[0]);
            minDate = (Date) obj[0];
        } catch (Exception e) {
            minDate = new Date();
        }
        try {
            //    //// // System.out.println(" obj[1] = " + obj[1]);
            maxDate = (Date) obj[1];
        } catch (Exception e) {
            maxDate = new Date();
        }
        try {
            //    //// // System.out.println(" obj[2] = " + obj[2]);
            totalQty = Math.abs((Double) obj[2]);
        } catch (Exception e) {
            totalQty = 0.0;
        }

        DateTime mind = new DateTime(minDate);
        DateTime maxd = new DateTime(maxDate);
        Days daysDiff = Days.daysBetween(mind, maxd);

        int ds = daysDiff.getDays();
        //    //// // System.out.println("ds = " + ds);
        //    //// // System.out.println("totalQty = " + totalQty);

        double dailyDemand;
        if (ds == 0) {
            ds = 1;
        }
        try {
            dailyDemand = totalQty / ds;
        } catch (Exception e) {
            dailyDemand = 1.0;
        }
        if (dailyDemand == 0.0) {
            dailyDemand = 1.0;
        }
        //    //// // System.out.println("dailyDemand = " + dailyDemand);
        return dailyDemand;
    }

    public int calculateOrderingCycleDurationInDays(Reorder reorder) {
        //    //// // System.out.println("calculating ordering cycle duration");
        String jpql;
        Map m = new HashMap();

        DateTime dt = new DateTime();
        DateTime tfd = dt.minusMonths(reorder.getMonthsConsideredForShortTermAnalysis());

        Date fd = tfd.toDate();
        Date td = new Date();

        jpql = "Select max(bi.bill.createdAt),min(bi.bill.createdAt),count(bi) "
                + " from BillItem bi "
                + " where bi.bill.billType in :bts"
                + " and bi.item=:amp "
                + " and bi.bill.createdAt between :fd and :td "
                + " group by bi.bill";

        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.PharmacyPurchaseBill);
        bts.add(BillType.PharmacyGrnBill);

        m.put("bts", bts);
        m.put("amp", reorder.getItem());
        m.put("fd", fd);
        m.put("td", td);

        //    //// // System.out.println("jpql = " + jpql);
        //    //// // System.out.println("m = " + m);
        Object[] obj = ejbFacade.findSingleAggregate(jpql, m);

        //    //// // System.out.println("obj = " + obj);
        if (obj == null) {
            return 14;
        }
        Date minDate;
        Date maxDate;
        int count;

        try {
            //    //// // System.out.println(" obj[0] = " + obj[0]);
            minDate = (Date) obj[0];
        } catch (Exception e) {
            minDate = new Date();
        }
        try {
            //    //// // System.out.println(" obj[1] = " + obj[1]);
            maxDate = (Date) obj[1];
        } catch (Exception e) {
            maxDate = new Date();
        }
        try {
            //    //// // System.out.println(" obj[2] = " + obj[2]);
            count = (int) obj[2];
        } catch (Exception e) {
            count = 1;
        }

        if (count == 0) {
            count = 1;
        }
        DateTime mind = new DateTime(minDate);
        DateTime maxd = new DateTime(maxDate);

        Days daysDiff = Days.daysBetween(maxd, mind);

        int ds = daysDiff.getDays();
        //    //// // System.out.println("ds = " + ds);
        //    //// // System.out.println("count = " + count);
        return ds / count;

    }

    public AmpController getAmpController() {
        return ampController;
    }

    public void setAmpController(AmpController ampController) {
        this.ampController = ampController;
    }

    public List<Reorder> getDepartmentReorders() {
        return departmentReorders;
    }

    public void setDepartmentReorders(List<Reorder> departmentReorders) {
        this.departmentReorders = departmentReorders;
    }

    public Reorder getCurrent() {
        return current;
    }

    public void setCurrent(Reorder current) {
        this.current = current;
    }

    public List<Reorder> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(List<Reorder> items) {
        this.items = items;
    }

    public ReorderFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(ReorderFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public Department getDepartment() {
        if (department == null) {
            department = sessionController.getDepartment();
        }
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public ReorderController() {
    }

    public Reorder getReorder(java.lang.Long id) {
        return ejbFacade.find(id);
    }

    public ItemController getItemController() {
        return itemController;
    }

    public PurchaseOrderRequestController getPurchaseOrderRequestController() {
        return purchaseOrderRequestController;
    }

    public PharmacyController getPharmacyController() {
        return pharmacyController;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public DepartmentController getDepartmentController() {
        return departmentController;
    }

    public StockController getStockController() {
        return stockController;
    }

    public ReorderFacade getReorderFacade() {
        return reorderFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Department getHistoryDept() {
        return historyDept;
    }

    public void setHistoryDept(Department historyDept) {
        this.historyDept = historyDept;
    }

    public List<Reorder> getReordersAvailableForSelection() {
        return reordersAvailableForSelection;
    }

    public void setReordersAvailableForSelection(List<Reorder> reordersAvailableForSelection) {
        this.reordersAvailableForSelection = reordersAvailableForSelection;
    }

    public List<Reorder> getUserSelectedReorders() {
        return userSelectedReorders;
    }

    public void setUserSelectedReorders(List<Reorder> userSelectedReorders) {
        this.userSelectedReorders = userSelectedReorders;
    }

    @FacesConverter(forClass = Reorder.class)
    public static class ReorderControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            ReorderController controller = (ReorderController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "reorderController");
            return controller.getReorder(getKey(value));
        }

        java.lang.Long getKey(String value) {
            long key;
            key = Long.parseLong(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            return String.valueOf(value);
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Reorder) {
                Reorder o = (Reorder) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Reorder.class.getName());
            }
        }

    }

}
