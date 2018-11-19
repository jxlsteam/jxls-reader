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

/**
 * @author Leonid Vysochyn
 */
public class XLSReaderTest extends TestCase {
    public static final String dataXLS = "/templates/departmentData.xls";

    public void testRead() throws IOException, InvalidFormatException {
        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(dataXLS));

        Department itDepartment = new Department();
        Department hrDepartment = new Department();
        Map<String, Object> beans = new HashMap<String, Object>();
        beans.put("itDepartment", itDepartment);
        beans.put("hrDepartment", hrDepartment);
        // Create Sheet1 Reader
        List<BeanCellMapping> chiefMappings = new ArrayList<BeanCellMapping>();
        chiefMappings.add( new BeanCellMapping(0, (short) 1, "itDepartment", "name") );
        chiefMappings.add( new BeanCellMapping(3, (short) 0, "itDepartment", "chief.name") );
        chiefMappings.add( new BeanCellMapping(3, (short) 1, "itDepartment", "chief.age") );
        chiefMappings.add( new BeanCellMapping(3, (short) 3, "itDepartment", "chief.payment") );
        chiefMappings.add( new BeanCellMapping("E4", "itDepartment", "chief.bonus") );
        XLSBlockReader chiefReader = new SimpleBlockReaderImpl(0, 6, chiefMappings);
        List<BeanCellMapping> employeeMappings = new ArrayList<BeanCellMapping>();
        employeeMappings.add( new BeanCellMapping(7, (short) 0, "employee", "name") );
        employeeMappings.add( new BeanCellMapping(7, (short) 1, "employee", "age") );
        employeeMappings.add( new BeanCellMapping(7, (short) 3, "employee", "payment") );
        employeeMappings.add( new BeanCellMapping(7, (short) 4, "employee", "bonus") );
        XLSBlockReader employeeReader = new SimpleBlockReaderImpl(7, 7, employeeMappings);
        XLSLoopBlockReader employeesReader = new XLSForEachBlockReaderImpl(7, 7, "itDepartment.staff", "employee", Employee.class);
        employeesReader.addBlockReader( employeeReader );
        SectionCheck loopBreakCheck = getLoopBreakCheck();
        employeesReader.setLoopBreakCondition( loopBreakCheck );
        XLSSheetReader sheet1Reader = new XLSSheetReaderImpl();
        sheet1Reader.addBlockReader( chiefReader );
        sheet1Reader.addBlockReader( employeesReader );
        // Create Sheet2 Reader
        XLSSheetReader sheet2Reader = new XLSSheetReaderImpl();
        employeeMappings = new ArrayList<BeanCellMapping>();
        employeeMappings.add( new BeanCellMapping(2, (short) 0, "employee", "name") );
        employeeMappings.add( new BeanCellMapping(2, (short) 1, "employee", "age") );
        employeeMappings.add( new BeanCellMapping(2, (short) 2, "employee", "payment") );
        employeeMappings.add( new BeanCellMapping(2, (short) 3, "employee", "bonus") );
        XLSBlockReader sheet2EmployeeReader = new SimpleBlockReaderImpl(2, 2, employeeMappings);
        XLSLoopBlockReader sheet2EmployeesReader = new XLSForEachBlockReaderImpl(2, 2, "hrDepartment.staff", "employee", Employee.class);
        sheet2EmployeesReader.addBlockReader( sheet2EmployeeReader );
        sheet2EmployeesReader.setLoopBreakCondition( getLoopBreakCheck() );
        chiefMappings = new ArrayList<BeanCellMapping>();
        chiefMappings.add( new BeanCellMapping(7, (short)0, "hrDepartment", "chief.name"));
        chiefMappings.add( new BeanCellMapping(7, (short)1, "hrDepartment", "chief.age"));
        chiefMappings.add( new BeanCellMapping(7, (short)2, "hrDepartment", "chief.payment"));
        chiefMappings.add( new BeanCellMapping(7, (short)3, "hrDepartment", "chief.bonus"));
        XLSBlockReader hrChiefReader = new SimpleBlockReaderImpl(3, 7, chiefMappings);
        sheet2Reader.addBlockReader( new SimpleBlockReaderImpl(0, 1, new ArrayList<BeanCellMapping>()));
        sheet2Reader.addBlockReader( sheet2EmployeesReader );
        sheet2Reader.addBlockReader( hrChiefReader );
        // create main reader
        XLSReader mainReader = new XLSReaderImpl();
        mainReader.addSheetReader("Sheet1", sheet1Reader);
        mainReader.addSheetReader("Sheet2", sheet2Reader);
        mainReader.read( inputXLS, beans);
        inputXLS.close();
        // check sheet1 data
        assertEquals( "IT", itDepartment.getName() );
        assertEquals( "Maxim", itDepartment.getChief().getName() );
        assertEquals( new Integer(30), itDepartment.getChief().getAge() );
        assertEquals(3000.0, itDepartment.getChief().getPayment() );
        assertEquals(0.25, itDepartment.getChief().getBonus() );
        assertEquals( 4, itDepartment.getStaff().size() );
        Employee employee = (Employee) itDepartment.getStaff().get(0);
        checkEmployee( employee, "Oleg", 32, 2000.0, 0.20);
        employee = (Employee) itDepartment.getStaff().get(1);
        checkEmployee( employee, "Yuri", 29, 1800.0, 0.15);
        employee = (Employee) itDepartment.getStaff().get(2);
        checkEmployee( employee, "Leonid", 30, 1700.0, 0.20);
        employee = (Employee) itDepartment.getStaff().get(3);
        checkEmployee( employee, "Alex", 28, 1600.0, 0.20);
        // check sheet2 data
        checkEmployee( hrDepartment.getChief(), "Betsy", 37, 2200.0, 0.3);
        assertEquals(4, hrDepartment.getStaff().size() );
        employee = (Employee) hrDepartment.getStaff().get(0);
        checkEmployee( employee, "Olga", 26, 1400.0, 0.20);
        employee = (Employee) hrDepartment.getStaff().get(1);
        checkEmployee( employee, "Helen", 30, 2100.0, 0.10);
        employee = (Employee) hrDepartment.getStaff().get(2);
        checkEmployee( employee, "Keith", 24, 1800.0, 0.15);
        employee = (Employee) hrDepartment.getStaff().get(3);
        checkEmployee( employee, "Cat", 34, 1900.0, 0.15);
    }

    public void testReadSheetsByIndex() throws IOException, InvalidFormatException {
        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(dataXLS));

        Department itDepartment = new Department();
        Department hrDepartment = new Department();
        Map<String, Object> beans = new HashMap<String, Object>();
        beans.put("itDepartment", itDepartment);
        beans.put("hrDepartment", hrDepartment);
        // Create Sheet1 Reader
        List<BeanCellMapping> chiefMappings = new ArrayList<BeanCellMapping>();
        chiefMappings.add( new BeanCellMapping(0, (short) 1, "itDepartment", "name") );
        chiefMappings.add( new BeanCellMapping(3, (short) 0, "itDepartment", "chief.name") );
        chiefMappings.add( new BeanCellMapping(3, (short) 1, "itDepartment", "chief.age") );
        chiefMappings.add( new BeanCellMapping(3, (short) 3, "itDepartment", "chief.payment") );
        chiefMappings.add( new BeanCellMapping("E4", "itDepartment", "chief.bonus") );
        XLSBlockReader chiefReader = new SimpleBlockReaderImpl(0, 6, chiefMappings);
        List<BeanCellMapping> employeeMappings = new ArrayList<BeanCellMapping>();
        employeeMappings.add( new BeanCellMapping(7, (short) 0, "employee", "name") );
        employeeMappings.add( new BeanCellMapping(7, (short) 1, "employee", "age") );
        employeeMappings.add( new BeanCellMapping(7, (short) 3, "employee", "payment") );
        employeeMappings.add( new BeanCellMapping(7, (short) 4, "employee", "bonus") );
        XLSBlockReader employeeReader = new SimpleBlockReaderImpl(7, 7, employeeMappings);
        XLSLoopBlockReader employeesReader = new XLSForEachBlockReaderImpl(7, 7, "itDepartment.staff", "employee", Employee.class);
        employeesReader.addBlockReader( employeeReader );
        SectionCheck loopBreakCheck = getLoopBreakCheck();
        employeesReader.setLoopBreakCondition( loopBreakCheck );
        XLSSheetReader sheet1Reader = new XLSSheetReaderImpl();
        sheet1Reader.addBlockReader( chiefReader );
        sheet1Reader.addBlockReader( employeesReader );
        // Create Sheet2 Reader
        XLSSheetReader sheet2Reader = new XLSSheetReaderImpl();
        employeeMappings = new ArrayList<BeanCellMapping>();
        employeeMappings.add( new BeanCellMapping(2, (short) 0, "employee", "name") );
        employeeMappings.add( new BeanCellMapping(2, (short) 1, "employee", "age") );
        employeeMappings.add( new BeanCellMapping(2, (short) 2, "employee", "payment") );
        employeeMappings.add( new BeanCellMapping(2, (short) 3, "employee", "bonus") );
        XLSBlockReader sheet2EmployeeReader = new SimpleBlockReaderImpl(2, 2, employeeMappings);
        XLSLoopBlockReader sheet2EmployeesReader = new XLSForEachBlockReaderImpl(2, 2, "hrDepartment.staff", "employee", Employee.class);
        sheet2EmployeesReader.addBlockReader( sheet2EmployeeReader );
        sheet2EmployeesReader.setLoopBreakCondition( getLoopBreakCheck() );
        chiefMappings = new ArrayList<BeanCellMapping>();
        chiefMappings.add( new BeanCellMapping(7, (short)0, "hrDepartment", "chief.name"));
        chiefMappings.add( new BeanCellMapping(7, (short)1, "hrDepartment", "chief.age"));
        chiefMappings.add( new BeanCellMapping(7, (short)2, "hrDepartment", "chief.payment"));
        chiefMappings.add( new BeanCellMapping(7, (short)3, "hrDepartment", "chief.bonus"));
        XLSBlockReader hrChiefReader = new SimpleBlockReaderImpl(3, 7, chiefMappings);
        sheet2Reader.addBlockReader( new SimpleBlockReaderImpl(0, 1, new ArrayList<BeanCellMapping>()));
        sheet2Reader.addBlockReader( sheet2EmployeesReader );
        sheet2Reader.addBlockReader( hrChiefReader );
        // create main reader
        XLSReader mainReader = new XLSReaderImpl();
        mainReader.addSheetReader(0, sheet1Reader);
        mainReader.addSheetReader(1, sheet2Reader);
        mainReader.read( inputXLS, beans);
        inputXLS.close();
        // check sheet1 data
        assertEquals( "IT", itDepartment.getName() );
        assertEquals( "Maxim", itDepartment.getChief().getName() );
        assertEquals( new Integer(30), itDepartment.getChief().getAge() );
        assertEquals(3000.0, itDepartment.getChief().getPayment() );
        assertEquals(0.25, itDepartment.getChief().getBonus() );
        assertEquals( 4, itDepartment.getStaff().size() );
        Employee employee = (Employee) itDepartment.getStaff().get(0);
        checkEmployee( employee, "Oleg", 32, 2000.0, 0.20);
        employee = (Employee) itDepartment.getStaff().get(1);
        checkEmployee( employee, "Yuri", 29, 1800.0, 0.15);
        employee = (Employee) itDepartment.getStaff().get(2);
        checkEmployee( employee, "Leonid", 30, 1700.0, 0.20);
        employee = (Employee) itDepartment.getStaff().get(3);
        checkEmployee( employee, "Alex", 28, 1600.0, 0.20);
        // check sheet2 data
        checkEmployee( hrDepartment.getChief(), "Betsy", 37, 2200.0, 0.3);
        assertEquals(4, hrDepartment.getStaff().size() );
        employee = (Employee) hrDepartment.getStaff().get(0);
        checkEmployee( employee, "Olga", 26, 1400.0, 0.20);
        employee = (Employee) hrDepartment.getStaff().get(1);
        checkEmployee( employee, "Helen", 30, 2100.0, 0.10);
        employee = (Employee) hrDepartment.getStaff().get(2);
        checkEmployee( employee, "Keith", 24, 1800.0, 0.15);
        employee = (Employee) hrDepartment.getStaff().get(3);
        checkEmployee( employee, "Cat", 34, 1900.0, 0.15);
    }



    private void checkEmployee(Employee employee, String name, Integer age, Double payment, Double bonus){
        assertNotNull( employee );
        assertEquals( name, employee.getName() );
        assertEquals( age, employee.getAge() );
        assertEquals( payment, employee.getPayment() );
        assertEquals( bonus, employee.getBonus() );
    }


    private SectionCheck getLoopBreakCheck() {
        OffsetRowCheck rowCheck = new OffsetRowCheckImpl( 0 );
        rowCheck.addCellCheck( new OffsetCellCheckImpl((short) 0, "Employee Payment Totals:") );
        SectionCheck sectionCheck = new SimpleSectionCheck();
        sectionCheck.addRowCheck( rowCheck );
        return sectionCheck;
    }


}
