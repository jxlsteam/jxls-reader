package org.jxls.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * @author Leonid Vysochyn
 */
public class XLSSheetReaderImpl implements XLSSheetReader {

    List blockReaders = new ArrayList();
    String sheetName;
    int sheetIdx = -1;

    XLSReadStatus readStatus = new XLSReadStatus();

    ConvertUtilsBeanProviderDelegate convertUtilsBeanProvider = new ConvertUtilsBeanProviderDelegate();

    public XLSReadStatus read(Sheet sheet, Map beans) {
        readStatus.clear();
        XLSRowCursor cursor = new XLSRowCursorImpl( sheetName, sheet );
        for (int i = 0; i < blockReaders.size(); i++) {
        	XLSBlockReader blockReader = (XLSBlockReader) blockReaders.get(i);
            final int startRow = blockReader.getStartRow();
            final boolean mvCursor = startRow > 0 && startRow < sheet.getLastRowNum();
			if( mvCursor ){
            	cursor.setCurrentRowNum(startRow);
            }
            readStatus.mergeReadStatus( blockReader.read( cursor, beans ) );
            if( !mvCursor ){
            	cursor.moveForward();
            }
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
        Iterator it = this.blockReaders.iterator();
        while( it.hasNext() ){
        	((XLSBlockReader)it.next()).setConvertUtilsBeanProvider( convertUtilsBeanProvider );
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
