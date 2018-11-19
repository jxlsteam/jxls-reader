package org.jxls.reader;

import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Leonid Vysochyn
 */
public class XLSSheetReaderImpl implements XLSSheetReader {

    private List<XLSBlockReader> blockReaders = new ArrayList<XLSBlockReader>();
    private String sheetName;
    private int sheetIdx = -1;

    XLSReadStatus readStatus = new XLSReadStatus();

    ConvertUtilsBeanProviderDelegate convertUtilsBeanProvider = new ConvertUtilsBeanProviderDelegate();

    public XLSReadStatus read(Sheet sheet, Map beans) {
        readStatus.clear();
        XLSRowCursor cursor = new XLSRowCursorImpl( sheetName, sheet );
        for (XLSBlockReader blockReader1 : blockReaders) {
            readStatus.mergeReadStatus(blockReader1.read(cursor, beans));
            cursor.moveForward();
        }
        return readStatus;
    }

    public String getSheetNameBySheetIdx(Sheet sheet, int idx){
        Sheet sheetAtIdx = sheet.getWorkbook().getSheetAt(idx);
        return sheetAtIdx.getSheetName();
    }
    
    public void setConvertUtilsBeanProvider( ConvertUtilsBeanProvider provider ){
    	this.convertUtilsBeanProvider.setDelegate( provider ) ;
    }

    public List getBlockReaders() {
        return blockReaders;
    }

    public void setBlockReaders(List blockReaders) {
        this.blockReaders = blockReaders;
        for (XLSBlockReader blockReader : this.blockReaders) {
            blockReader.setConvertUtilsBeanProvider(convertUtilsBeanProvider);
        }
    }

    public void addBlockReader(XLSBlockReader blockReader) {
    	blockReader.setConvertUtilsBeanProvider( convertUtilsBeanProvider );
        blockReaders.add( blockReader );
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getSheetIdx(){
        return sheetIdx;
    }

    public void setSheetIdx(int sheetIdx){
        this.sheetIdx = sheetIdx;
    }
}
