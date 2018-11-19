package org.jxls.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * Interface to read and parse excel file
 * @author Leonid Vysochyn
 */
public interface XLSReader {
	 
    XLSReadStatus read(InputStream inputXLS, Map beans) throws IOException, InvalidFormatException;
    void setSheetReaders(Map sheetReaders);
    Map getSheetReaders();
    void addSheetReader(String sheetName, XLSSheetReader reader);
    void addSheetReader(XLSSheetReader reader);
    void addSheetReader(Integer idx, XLSSheetReader reader);
    ConvertUtilsBeanProvider getConvertUtilsBeanProvider();
}
 