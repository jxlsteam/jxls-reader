package org.jxls.reader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.jxls.reader.sample.Department;
import org.jxls.reader.sample.Employee;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.xml.sax.SAXException;

/**
 * @author Leonid Vysochyn
 */
public class XLSForEachBlockReaderImplTest extends TestCase {
    public static final String departmentDataXLS = "/templates/departmentData.xls";
    public static final String employeeDataXLS = "/templates/employeesData.xls";
    public static final String xmlConfig = "/xml/emptyloopbreak.xml";
    public static final String idsXML = "/xml/ids.xml";
    public static final String idsXLS = "/templates/ids.xls";


    protected void setUp() throws Exception {
        super.setUp();
//        ReaderConfig.getInstance().setUseDefaultValuesForPrimitiveTypes( true );
    }

    public void testRead() throws IOException, InvalidFormatException {
        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(departmentDataXLS));
        Workbook hssfInputWorkbook = WorkbookFactory.create(inputXLS);
        Sheet sheet = hssfInputWorkbook.getSheetAt( 0 );
        List<BeanCellMapping> mappings = new ArrayList<BeanCellMapping>();
        Department department = new Department();
        Map<String, Object> beans = new HashMap<String, Object>();
        beans.put("department", department);
        mappings.add( new BeanCellMapping(7, (short) 0, "employee", "name"));
        mappings.add( new BeanCellMapping(7, (short) 1, "employee", "age"));
        mappings.add( new BeanCellMapping(7, (short) 3, "employee", "payment"));
        mappings.add( new BeanCellMapping(7, (short) 4, "employee", "bonus"));
        XLSBlockReader reader = new SimpleBlockReaderImpl(7, 7, mappings);
        XLSRowCursor cursor = new XLSRowCursorImpl( sheet );
        XLSLoopBlockReader forEachReader = new XLSForEachBlockReaderImpl(7, 7, "department.staff", "employee", Employee.class);
        forEachReader.addBlockReader( reader );
        SectionCheck loopBreakCheck = getLoopBreakCheck();
        forEachReader.setLoopBreakCondition( loopBreakCheck );
        cursor.setCurrentRowNum( 7 );

        forEachReader.read( cursor, beans );
        assertEquals( 4, department.getStaff().size() );
        Employee employee = (Employee) department.getStaff().get(0);
        checkEmployee( employee, "Oleg", 32, 2000.0, 0.20);
        employee = (Employee) department.getStaff().get(1);
        checkEmployee( employee, "Yuri", 29, 1800.0, 0.15);
        employee = (Employee) department.getStaff().get(2);
        checkEmployee( employee, "Leonid", 30, 1700.0, 0.20);
        employee = (Employee) department.getStaff().get(3);
        checkEmployee( employee, "Alex", 28, 1600.0, 0.20);
    }

    public void testRead2() throws IOException, InvalidFormatException {
        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(departmentDataXLS));
        Workbook hssfInputWorkbook = WorkbookFactory.create(inputXLS);
        Sheet sheet = hssfInputWorkbook.getSheetAt( 2 );
        Department department;
        Map beans = new HashMap();
        List departments = new ArrayList();
        beans.put("departments", departments);
        List<BeanCellMapping> chiefMappings = new ArrayList<BeanCellMapping>();
        chiefMappings.add( new BeanCellMapping(0, (short) 1, "department", "name") );
        chiefMappings.add( new BeanCellMapping(3, (short) 0, "department", "chief.name") );
        chiefMappings.add( new BeanCellMapping(3, (short) 1, "department.chief.age") );
        chiefMappings.add( new BeanCellMapping(3, (short) 2, "department.chief.payment") );
        chiefMappings.add( new BeanCellMapping(3, (short) 3, "department", "chief.bonus") );
        XLSBlockReader chiefReader = new SimpleBlockReaderImpl(0, 6, chiefMappings);

        List<BeanCellMapping> employeeMappings = new ArrayList<BeanCellMapping>();
        employeeMappings.add( new BeanCellMapping(7, (short) 0, "employee", "name") );
        employeeMappings.add( new BeanCellMapping(7, (short) 1, "employee", "age") );
        employeeMappings.add( new BeanCellMapping(7, (short) 2, "employee", "payment") );
        employeeMappings.add( new BeanCellMapping(7, (short) 3, "employee", "bonus") );
        XLSBlockReader employeeReader = new SimpleBlockReaderImpl(7, 7, employeeMappings);
        XLSLoopBlockReader employeesReader = new XLSForEachBlockReaderImpl(7, 7, "department.staff", "employee", Employee.class);
        employeesReader.addBlockReader( employeeReader );
        SectionCheck loopBreakCheck = getLoopBreakCheck();
        employeesReader.setLoopBreakCondition( loopBreakCheck );
        XLSLoopBlockReader departmentInfoReader = new XLSForEachBlockReaderImpl(0, 8, "departments", "department", Department.class);
        departmentInfoReader.addBlockReader( chiefReader );
        departmentInfoReader.addBlockReader( employeesReader );
        departmentInfoReader.addBlockReader( new SimpleBlockReaderImpl(8, 8, new ArrayList<BeanCellMapping>()));
        loopBreakCheck = new SimpleSectionCheck();
        loopBreakCheck.addRowCheck( new OffsetRowCheckImpl(0) );
        loopBreakCheck.addRowCheck( new OffsetRowCheckImpl(1) );
        departmentInfoReader.setLoopBreakCondition( loopBreakCheck );
        
        XLSRowCursor cursor = new XLSRowCursorImpl( sheet );
        cursor.setCurrentRowNum( 0 );

        departmentInfoReader.read( cursor, beans );

        assertEquals( 3, departments.size() );

        department = (Department) departments.get(0);
        checkDepartmentInfo( department, "IT", "Derek", 35, 3000.0, 0.30);
        assertEquals( 5, department.getStaff().size() );
        Employee employee = (Employee) department.getStaff().get(0);
        checkEmployee( employee, "Elsa", 28, 1500.0, 0.15);
        employee = (Employee) department.getStaff().get(1);
        checkEmployee( employee, "Oleg", 32, 2300.0, 0.25);
        employee = (Employee) department.getStaff().get(2);
        checkEmployee( employee, "Neil", 34, 2500.0, 0.00);
        employee = (Employee) department.getStaff().get(3);
        checkEmployee( employee, "Maria", 34, 1700.0, 0.15);
        employee = (Employee) department.getStaff().get(4);
        checkEmployee( employee, "John", 35, 2800.0, 0.20);

        department = (Department) departments.get(1);
        checkDepartmentInfo( department, "HR", "Betsy", 37, 2200.0, 0.30);
        assertEquals( 4, department.getStaff().size() );
        employee = (Employee) department.getStaff().get(0);
        checkEmployee( employee, "Olga", 26, 1400.0, 0.20);
        employee = (Employee) department.getStaff().get(1);
        checkEmployee( employee, "Helen", 30, 2100.0, 0.10);
        employee = (Employee) department.getStaff().get(2);
        checkEmployee( employee, "Keith", 24, 1800.0, 0.15);
        employee = (Employee) department.getStaff().get(3);
        checkEmployee( employee, "Cat", 34, 1900.0, 0.15);

        department = (Department) departments.get(2);
        checkDepartmentInfo( department, "BA", "Wendy", 35, 2900.0, 0.35);
        assertEquals( 4, department.getStaff().size() );
        employee = (Employee) department.getStaff().get(0);
        checkEmployee( employee, "Denise", 30, 2400.0, 0.20);
        employee = (Employee) department.getStaff().get(1);
        checkEmployee( employee, "LeAnn", 32, 2200.0, 0.15);
        employee = (Employee) department.getStaff().get(2);
        checkEmployee( employee, "Natali", 28, 2600.0, 0.10);
        employee = (Employee) department.getStaff().get(3);
        checkEmployee( employee, "Martha", 33, 2150.0, 0.25);
    }

    public void testEmptyLoopBreakCondition() throws IOException, SAXException, InvalidFormatException {
        InputStream inputXML = new BufferedInputStream(getClass().getResourceAsStream(xmlConfig));
        XLSReader reader = ReaderBuilder.buildFromXML( inputXML );
        assertNotNull( reader );
        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(employeeDataXLS));
        List employees = new ArrayList();
        Map<String, Object> beans = new HashMap<String, Object>();
        beans.put("employees", employees);
        reader.read( inputXLS, beans);
        assertNotNull( employees );
        assertEquals(4, employees.size());
        checkEmployee((Employee) employees.get(0), "Oleg", 34, 3000.0, null);
        checkEmployee((Employee) employees.get(1), "Yuriy", 29, 2500.0, null);
        checkEmployee((Employee) employees.get(2), "Alex", 30, 2300.0, null);
        checkEmployee((Employee) employees.get(3), "Vlad", 31, 2000.0, null);
        inputXLS.close();

    }

    public void testReadIdentifiers() throws IOException, SAXException, InvalidFormatException {
        InputStream inputXML = new BufferedInputStream(getClass().getResourceAsStream(idsXML));
        XLSReader reader = ReaderBuilder.buildFromXML( inputXML );
        assertNotNull( reader );
        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(idsXLS));
        List employees = new ArrayList();
        Map<String, Object> beans = new HashMap<String, Object>();
        beans.put("employees", employees);
        reader.read( inputXLS, beans);
        assertNotNull( employees );
        assertEquals(6, employees.size());
        checkEmployeeId((Employee) employees.get(0), "Oleg", "a123b");
        checkEmployeeId((Employee) employees.get(1), "Yuriy", "a567");
        checkEmployeeId((Employee) employees.get(2), "Alex", "89x");
        checkEmployeeId((Employee) employees.get(3), "Vlad", "xyz");
        checkEmployeeId((Employee) employees.get(4), "Sergey", "123");
        checkEmployeeId((Employee) employees.get(5), "Slava", "5");
        inputXLS.close();

    }

    private void checkDepartmentInfo(Department department, String name, String chiefName, Integer chiefAge, Double chiefPayment, Double chiefBonus){
        assertNotNull( department );
        assertEquals( name, department.getName() );
        checkEmployee( department.getChief(), chiefName, chiefAge, chiefPayment, chiefBonus );
    }

    private void checkEmployee(Employee employee, String name, Integer age, Double payment, Double bonus){
        assertNotNull( employee );
        assertEquals( name, employee.getName() );
        assertEquals( age, employee.getAge() );
        assertEquals( payment, employee.getPayment() );
        assertEquals( bonus, employee.getBonus() );
    }

    private void checkEmployeeId(Employee employee, String name, String id){
        assertNotNull( employee );
        assertEquals( name, employee.getName() );
        assertEquals( id, employee.getId() );
    }

    private SectionCheck getLoopBreakCheck() {
        OffsetRowCheck rowCheck = new OffsetRowCheckImpl( 0 );
        rowCheck.addCellCheck( new OffsetCellCheckImpl((short) 0, "Employee Payment Totals:") );
        SectionCheck sectionCheck = new SimpleSectionCheck();
        sectionCheck.addRowCheck( rowCheck );
        return sectionCheck;
    }

}
