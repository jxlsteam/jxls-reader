package org.jxls.reader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Leonid Vysochyn
 */
public class OffsetRowCheckImpl implements OffsetRowCheck {

    List<OffsetCellCheck> cellChecks = new ArrayList<OffsetCellCheck>();
    int offset;


    public OffsetRowCheckImpl() {
    }

    public OffsetRowCheckImpl(int offset) {
        this.offset = offset;
    }

    public OffsetRowCheckImpl(List<OffsetCellCheck> cellChecks) {
        this.cellChecks = cellChecks;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public List getCellChecks() {
        return cellChecks;
    }

    public void setCellChecks(List<OffsetCellCheck> cellChecks) {
        this.cellChecks = cellChecks;
    }

    public boolean isCheckSuccessful(Row row) {
        if( cellChecks.isEmpty() ){
            return isRowEmpty( row );
        }
        for (OffsetCellCheck offsetCellCheck : cellChecks) {
            if (!offsetCellCheck.isCheckSuccessful(row)) {
                return false;
            }
        }
        return true;
    }

    public boolean isCheckSuccessful(XLSRowCursor cursor) {
        if( !cursor.hasNext() ){
            return isCellChecksEmpty();
        }
        Row row = cursor.getSheet().getRow( offset + cursor.getCurrentRowNum() );
        if( row == null ){
            return isCellChecksEmpty();
        }
        return isCheckSuccessful( row );
    }

    private boolean isCellChecksEmpty() {
        if( cellChecks.isEmpty() ){
            return true;
        }
        for (OffsetCellCheck offsetCellCheck : cellChecks) {
            if (!isCellCheckEmpty(offsetCellCheck)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCellCheckEmpty(OffsetCellCheck cellCheck) {
        if( cellCheck.getValue() == null ){
            return true;
        }
        return cellCheck.getValue().toString().trim().equals("");
    }


    public void addCellCheck(OffsetCellCheck cellCheck) {
        cellChecks.add( cellCheck );
    }

    private boolean isRowEmpty(Row row) {
        if( row == null ){
            return true;
        }
        if (row.getLastCellNum() < 0) {
            return true;
        }
        for(short i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++){
            Cell cell = row.getCell( i );
            if( !isCellEmpty( cell ) ){
                return false;
            }
        }
        return true;
    }

    private boolean isCellEmpty(Cell cell) {
        if( cell == null ){
            return true;
        }
        switch( cell.getCellType() ){
            case BLANK:
                return true;
            case STRING:
                String cellValue = cell.getRichStringCellValue().getString();
                return cellValue == null || cellValue.length() == 0 || cellValue.trim().length() == 0;
            default:
                return false;
        }
    }
}
