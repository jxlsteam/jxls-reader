package org.jxls.reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Leonid Vysochyn
 */
public class SimpleBlockReaderImpl extends BaseBlockReader implements SimpleBlockReader {
    protected final Log log = LogFactory.getLog(getClass());

    List<BeanCellMapping> beanCellMappings = new ArrayList<BeanCellMapping>();
    SectionCheck sectionCheck;

    static {
        ReaderConfig.getInstance();
    }

    ConvertUtilsBeanProviderDelegate convertUtilsProvider = new ConvertUtilsBeanProviderDelegate();
    
    public SimpleBlockReaderImpl() {
    }

    public SimpleBlockReaderImpl(int startRow, int endRow, List<BeanCellMapping> beanCellMappings) {
        this.startRow = startRow;
        this.endRow = endRow;
        // Avoid change internal behaviour by changing external list.
        // this change has required changing tests
        this.beanCellMappings = new ArrayList<BeanCellMapping>( beanCellMappings );
        for (BeanCellMapping beanCellMapping : this.beanCellMappings) {
            beanCellMapping.setConvertUtilsProvider(convertUtilsProvider);
        }
    }

    public SimpleBlockReaderImpl(int startRow, int endRow) {
        this.startRow = startRow;
        this.endRow = endRow;
    }
    
    public void setConvertUtilsBeanProvider( ConvertUtilsBeanProvider provider ){
    	this.convertUtilsProvider.setDelegate( provider );
    }

    public XLSReadStatus read(XLSRowCursor cursor, Map beans) {
        readStatus.clear();
        final int currentRowNum = cursor.getCurrentRowNum();
        final int rowShift = currentRowNum - startRow;
        BeanCellMapping mapping;
        for (BeanCellMapping beanCellMapping : beanCellMappings) {
            mapping = beanCellMapping;
            try {
                String dataString = readCellString(cursor.getSheet(), mapping.getRow() + rowShift, mapping.getCol());
                mapping.populateBean(dataString, beans);
            } catch (Exception e) {
                String message = "Can't read cell " + getCellName(mapping, rowShift) + " on " + cursor.getSheetName() + " spreadsheet";
                readStatus.addMessage(new XLSReadMessage(message, e));
                if (ReaderConfig.getInstance().isSkipErrors()) {
                    if (log.isWarnEnabled()) {
                        log.warn(message);
                    }
                } else {
                    readStatus.setStatusOK(false);
                    throw new XLSDataReadException(getCellName(mapping, rowShift), "Can't read cell " + getCellName(mapping, rowShift) + " on " + cursor.getSheetName() + " spreadsheet", readStatus, e);
                }
            }
        }
        cursor.setCurrentRowNum(endRow + rowShift);
        return readStatus;
    }

    protected String readCellString(Sheet sheet, int rowNum, short cellNum) {
        Cell cell = getCell(sheet, rowNum, cellNum);
        return getCellString(cell);
    }

    protected String getCellString(Cell cell) {
        String dataString = null;
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    dataString = cell.getRichStringCellValue().getString();
                    break;
                case NUMERIC:
                    dataString = readNumericCell(cell);
                    break;
                case BOOLEAN:
                    dataString = Boolean.toString(cell.getBooleanCellValue());
                    break;
                case BLANK:
                    break;
                case ERROR:
                    break;
                case FORMULA:
                    // attempt to read formula cell as numeric cell
                    try{
                    dataString = readNumericCell(cell);
                    }catch(Exception e1){
                        log.info("Failed to read formula cell as numeric. Next to try as string. Cell=" + cell.toString());
                        try{
                            dataString = cell.getRichStringCellValue().getString();
                            log.info("Successfully read formula cell as string. Value=" + dataString);
                        }catch(Exception e2){
                            log.warn("Failed to read formula cell as numeric or string. Cell=" + cell.toString());
                        }
                    }

                    break;
                default:
                    break;
            }
        }
        return dataString;
    }

    protected String readNumericCell(Cell cell) {
        double value;
        String dataString;
        value = cell.getNumericCellValue();
        if (((long) value) == value) {
            dataString = Long.toString((long) value);
        } else {
            dataString = Double.toString(cell.getNumericCellValue());
        }
        return dataString;
    }

    private String getCellName(BeanCellMapping mapping, int rowShift) {
        CellReference currentCellRef = new CellReference(mapping.getRow() + rowShift, mapping.getCol(), false, false);
        return currentCellRef.formatAsString();
    }


    public SectionCheck getLoopBreakCondition() {
        return sectionCheck;
    }

    public void setLoopBreakCondition(SectionCheck sectionCheck) {
        this.sectionCheck = sectionCheck;
    }

    public void addMapping(BeanCellMapping mapping) {
        mapping.setConvertUtilsProvider( convertUtilsProvider );
        beanCellMappings.add(mapping);
    }

    public List getMappings() {
        return beanCellMappings;
    }

    private Cell getCell(Sheet sheet, int rowNum, int cellNum) {
        Row row = sheet.getRow(rowNum);
        if (row == null) {
            return null;
        }
        return row.getCell(cellNum);
    }

}
