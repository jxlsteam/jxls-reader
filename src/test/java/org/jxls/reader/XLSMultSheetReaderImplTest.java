package org.jxls.reader;

import junit.framework.TestCase;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jxls.reader.dynamic.DynamicReaderBuilder;
import org.jxls.reader.sample.Department;
import org.jxls.reader.sample.Employee;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leonid Vysochyn
 */
public class XLSMultSheetReaderImplTest extends TestCase {
    public static final String idsXML = "/xml/mids.xml";
    public static final String idsXLS = "/templates/mids.xls";


    protected void setUp() throws Exception {
        super.setUp();
//        ReaderConfig.getInstance().setUseDefaultValuesForPrimitiveTypes( true );
    }

    public void testReadIdentifiers() throws IOException, SAXException, InvalidFormatException {
        InputStream inputXML = new BufferedInputStream(getClass().getResourceAsStream(idsXML));
        XLSReader reader = DynamicReaderBuilder.buildFromXML(inputXML);
        assertNotNull(reader);
        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(idsXLS));
        List employees = new ArrayList();
        Map<String, Object> beans = new HashMap<String, Object>();
        beans.put("employees", employees);
        reader.read( inputXLS, beans);
        assertNotNull( employees );
        assertEquals(18, employees.size());
        checkEmployeeId((Employee) employees.get(0), "Oleg", "a123b");
        checkEmployeeId((Employee) employees.get(1), "Yuriy", "a567");
        checkEmployeeId((Employee) employees.get(2), "Alex", "89x");
        checkEmployeeId((Employee) employees.get(3), "Vlad", "xyz");
        checkEmployeeId((Employee) employees.get(4), "Sergey", "123");
        checkEmployeeId((Employee) employees.get(5), "Slava", "5");
        inputXLS.close();

    }

    private void checkDepartmentInfo(Department department, String name, String chiefName, Integer chiefAge, Double chiefPayment, Double chiefBonus) {
        assertNotNull(department);
        assertEquals(name, department.getName());
        checkEmployee(department.getChief(), chiefName, chiefAge, chiefPayment, chiefBonus);
    }

    private void checkEmployee(Employee employee, String name, Integer age, Double payment, Double bonus) {
        assertNotNull(employee);
        assertEquals(name, employee.getName());
        assertEquals(age, employee.getAge());
        assertEquals(payment, employee.getPayment());
        assertEquals(bonus, employee.getBonus());
    }

    private void checkEmployeeId(Employee employee, String name, String id) {
        assertNotNull(employee);
        assertEquals(name, employee.getName());
        assertEquals(id, employee.getId());
    }

    private SectionCheck getLoopBreakCheck() {
        OffsetRowCheck rowCheck = new OffsetRowCheckImpl(0);
        rowCheck.addCellCheck(new OffsetCellCheckImpl((short) 0, "Employee Payment Totals:"));
        SectionCheck sectionCheck = new SimpleSectionCheck();
        sectionCheck.addRowCheck(rowCheck);
        return sectionCheck;
    }

}
