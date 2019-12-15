package org.jxls.reader.demo;

import junit.framework.TestCase;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jxls.reader.ReaderBuilder;
import org.jxls.reader.XLSReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demo class to showcase usage of jxls-reader
 */
public class XlsReaderDemoTest extends TestCase {
    static Logger logger = LoggerFactory.getLogger(XlsReaderDemoTest.class);
    private static String dataFile = "/demo/department_data.xls";
    public static final String xmlConfig = "/demo/departments.xml";

    public void testRunXlsReader() throws IOException, SAXException, InvalidFormatException {
        logger.info("Reading xml config file and constructing XLSReader");
        try (InputStream xmlInputStream = new BufferedInputStream(getClass().getResourceAsStream(xmlConfig))) {
            final XLSReader reader = ReaderBuilder.buildFromXML(xmlInputStream);
            try (InputStream xlsInputStream = new BufferedInputStream(getClass().getResourceAsStream(dataFile))) {
                Department department = new Department();
                Department hrDepartment = new Department();
                List<Department> departments = new ArrayList<>();
                Map<String, Object> beans = new HashMap<>();

                beans.put("department", department);
                beans.put("hrDepartment", hrDepartment);
                beans.put("departments", departments);
                logger.info("Reading the data...");
                reader.read(xlsInputStream, beans);
                logger.info("Read " + departments.size() + " departments into `departments` list");
                logger.info("Read " + department.getName() + " department into `department` variable");
                logger.info("Read " + hrDepartment.getHeadcount() + " employees in `hrDepartment`");
                assertEquals(4, department.getStaff().size());
                assertEquals("Oleg", department.getStaff().get(0).getName());
            }
        }
    }
}