package org.jxls.reader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jxls.reader.sample.Department;
import org.jxls.reader.sample.Employee;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class SequentialConversionTest extends TestCase{

	 	public static final String dataXLS = "/templates/departmentData.xls";
	    public static final String xmlConfig = "/xml/departments.xml";
	    public static final String xmlConfig2 = "/xml/departments_indexedsheet.xml";

	    public void testBuildFromXML() throws IOException, SAXException, InvalidFormatException {
	    	
	        
			XLSReader firstReader;
			{
				InputStream resourceAsStream = getClass().getResourceAsStream(xmlConfig);
				InputStream inputXML = new BufferedInputStream(resourceAsStream);
		        firstReader = ReaderBuilder.buildFromXML( inputXML );
		        ConvertUtilsBean firstReaderConvertUtilsBean = firstReader.getConvertUtilsBeanProvider().getConvertUtilsBean();
		        firstReaderConvertUtilsBean.register( new Converter(){@Override
		        	public Object convert(Class dateClazz, Object value ) {
		        		Date result;
						Date parsedValue = (Date)(new DateConverter()).convert(dateClazz, value);
						Calendar cal = Calendar.getInstance();
		        		cal.setTime( parsedValue );
		        		cal.set( Calendar.YEAR, 2017 );
		        		result = cal.getTime();
		        		
		        		return result ;
		        	}
		        } , Date.class );
			}
			
			XLSReader secondReader;
			{
				InputStream resourceAsStream = getClass().getResourceAsStream(xmlConfig);
				InputStream inputXML = new BufferedInputStream(resourceAsStream);
				secondReader = ReaderBuilder.buildFromXML( inputXML );
		        ConvertUtilsBean secondReaderConvertUtilsBean = secondReader.getConvertUtilsBeanProvider().getConvertUtilsBean();
		        secondReaderConvertUtilsBean.register( new Converter(){@Override
		        	public Object convert(Class dateClazz, Object value ) {
		        		Date result;
			        	Date parsedValue = (Date)(new DateConverter()).convert(dateClazz, value);
						Calendar cal = Calendar.getInstance();
		        		cal.setTime( parsedValue );
		        		cal.set( Calendar.YEAR, 2020 );
		        		result = cal.getTime();
		        		
		        		return result ;
		        	}
		        } , Date.class );
			}
			{
				Department firstDepartment = new Department();
		        Department firstHRDepartment = new Department();
		        List firstDepartments = new ArrayList();
		        Map firstBeans = new HashMap();
		        firstBeans.put("department", firstDepartment);
		        firstBeans.put("hrDepartment", firstHRDepartment);
		        firstBeans.put("departments", firstDepartments);
		        
		        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(dataXLS));
		        firstReader.read( inputXLS, firstBeans);
		        
		        Employee employee = (Employee) firstDepartment.getStaff().get(0);
		        assertTrue( employee.getBirthDate().getYear() == 117 );
			}
			
			{
				Department secondDepartment = new Department();
		        Department secondHRDepartment = new Department();
		        List secondDepartments = new ArrayList();
		        Map secondBeans = new HashMap();
		        secondBeans.put("department", secondDepartment);
		        secondBeans.put("hrDepartment", secondHRDepartment);
		        secondBeans.put("departments", secondDepartments);
		        
		        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(dataXLS));
		        secondReader.read( inputXLS, secondBeans);
		        Employee employee = (Employee) secondDepartment.getStaff().get(0);
		        assertTrue( employee.getBirthDate().getYear() == 120 );
			}
	        
	    }
}
