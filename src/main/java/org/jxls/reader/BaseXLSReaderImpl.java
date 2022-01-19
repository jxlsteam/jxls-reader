package org.jxls.reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Basic implementation of {@link XLSReader} interface
 *
 * @author Leonid Vysochyn
 */
public class BaseXLSReaderImpl extends XLSReaderImpl implements XLSReader {
    protected final Log log = LogFactory.getLog(getClass());

    @Override
    public final XLSReadStatus read(InputStream inputXLS, Map beans) throws IOException, InvalidFormatException {
        readStatus.clear();
        Workbook workbook = WorkbookFactory.create(inputXLS);
        for (int sheetNo = 0; sheetNo < workbook.getNumberOfSheets(); sheetNo++) {
            readStatus.mergeReadStatus(readSheet(workbook, sheetNo, beans));
        }
        workbook.close();
        return readStatus;
    }

    private XLSReadStatus readSheet(Workbook workbook, int sheetNo, Map beans) {
        Sheet sheet = workbook.getSheetAt(sheetNo);
        String sheetName = workbook.getSheetName(sheetNo);
        if (log.isInfoEnabled()) {
            log.info("Processing sheet " + sheetName);
        }
        XLSSheetReader sheetReader = findSheetReader(sheetName, sheetNo);
        if (sheetReader != null) {
            sheetReader.setSheetName(sheetName);
            return sheetReader.read(sheet, beans);
        }
        return null;
    }

    protected XLSSheetReader findSheetReader(String sheetName, int sheetNo) {
        if (sheetReaders.containsKey(sheetName)) {
            return sheetReaders.get(sheetName);
        } else if (sheetReadersByIdx.containsKey(sheetNo)) {
            return sheetReadersByIdx.get(sheetNo);
        } else {
            return null;
        }
    }
}
