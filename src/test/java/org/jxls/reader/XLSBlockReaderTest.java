package org.jxls.reader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.jxls.reader.sample.Department;
import org.jxls.reader.sample.Employee;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @author Leonid Vysochyn
 */
public class XLSBlockReaderTest extends TestCase {
    public static final String dataXLS = "/templates/departmentData.xls";


    protected void setUp() throws Exception {
        super.setUp();
//        ConvertUtils.register( new SqlDateConverter(null), java.util.Date.class);
    }

    public void testRead() throws IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException, ParseException, InvalidFormatException {
        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(dataXLS));
        Workbook hssfInputWorkbook = WorkbookFactory.create(inputXLS);
        Sheet sheet = hssfInputWorkbook.getSheetAt( 0 );
        List<BeanCellMapping> mappings = new ArrayList<BeanCellMapping>();
        Department departmentBean = new Department();
        Employee chief = new Employee();
        Map<String, Object> beans = new HashMap<String, Object>();
        beans.put("department", departmentBean);
        beans.put("chief", chief);

        mappings.add( new BeanCellMapping(0, (short) 1, "department.name") );
        mappings.add( new BeanCellMapping("A4", "chief.name"));
        mappings.add( new BeanCellMapping("B4", "chief.age"));
        mappings.add( new BeanCellMapping("C4", "chief.birthDate"));
        mappings.add( new BeanCellMapping("D4", "chief.payment"));
        mappings.add( new BeanCellMapping("E4", "chief.bonus"));

        SimpleBlockReaderImpl reader = new SimpleBlockReaderImpl(0, 6, mappings);
        XLSRowCursor cursor = new XLSRowCursorImpl( sheet );
        cursor.setSheetName( hssfInputWorkbook.getSheetName(0));
        reader.read( cursor, beans );
        assertEquals( "IT", departmentBean.getName() );
        assertEquals( "Maxim", chief.getName() );
        assertEquals( new Integer(30), chief.getAge() );
        assertEquals(3000.0, chief.getPayment() );
        assertEquals(0.25, chief.getBonus() );

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Date date = format.parse("12/20/1976");
        assertEquals("Date value read error", date, chief.getBirthDate());

        mappings.clear();
        DynaBean dynaBean = new LazyDynaBean();
        beans.clear();
        beans.put("total", dynaBean);
        reader.setStartRow(8);
        reader.addMapping( new BeanCellMapping(9, (short) 3, "total", "totalPayment") );
        cursor.setCurrentRowNum( 12 );
        reader.read( cursor, beans );
        assertEquals(Integer.toString(10100), dynaBean.get( "totalPayment" ));
    }
}
