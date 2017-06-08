package org.jxls.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * Interface to read and parse excel file
 * @author Leonid Vysochyn
 */
public interface XLSReader {
	 
    public XLSReadStatus read(InputStream inputXLS, Map beans) throws IOException, InvalidFormatException;
    public void setSheetReaders(Map sheetReaders);
    public Map getSheetReaders();
    public void addSheetReader( String sheetName, XLSSheetReader reader);
    public void addSheetReader(XLSSheetReader reader);
    public void addSheetReader(Integer idx, XLSSheetReader reader);
    public ConvertUtilsBeanProvider getConvertUtilsBeanProvider();
}
 