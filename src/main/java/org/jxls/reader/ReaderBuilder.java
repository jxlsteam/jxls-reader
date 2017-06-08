package org.jxls.reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.digester3.Digester;
import org.xml.sax.SAXException;

/**
 * @author Leonid Vysochyn
 */
public class ReaderBuilder {

    XLSReader reader = new XLSReaderImpl();
    XLSSheetReader currentSheetReader;
    SimpleBlockReader currentSimpleBlockReader;
    XLSLoopBlockReader currentLoopBlockReader;
    boolean lastSheetReader = false;
    SectionCheck currentSectionCheck;
    OffsetRowCheck currentRowCheck;
    


    public static XLSReader buildFromXML(InputStream xmlStream) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating( false );
        digester.addObjectCreate("workbook", "org.jxls.reader.XLSReaderImpl");
        digester.addObjectCreate("workbook/worksheet", "org.jxls.reader.XLSSheetReaderImpl");
        digester.addSetProperties("workbook/worksheet", "name", "sheetName");
        digester.addSetProperties("workbook/worksheet", "idx", "sheetIdx");
//        digester.addSetProperty("workbook/worksheet", "sheetName", "name");
        digester.addSetNext("workbook/worksheet", "addSheetReader");
        digester.addObjectCreate("*/loop", "org.jxls.reader.XLSForEachBlockReaderImpl");
        digester.addSetProperties("*/loop");
        digester.addSetNext("*/loop", "addBlockReader");
        digester.addObjectCreate("*/section", "org.jxls.reader.SimpleBlockReaderImpl");
        digester.addSetProperties("*/section");
        digester.addSetNext("*/section", "addBlockReader");
        digester.addObjectCreate("*/mapping", "org.jxls.reader.BeanCellMapping");
        digester.addSetProperties("*/mapping");
        digester.addCallMethod("*/mapping", "setFullPropertyName", 1);
        digester.addCallParam("*/mapping", 0);
        digester.addSetNext("*/mapping", "addMapping");
        digester.addObjectCreate("*/loop/loopbreakcondition", "org.jxls.reader.SimpleSectionCheck");
        digester.addSetNext("*/loop/loopbreakcondition", "setLoopBreakCondition");
        digester.addObjectCreate("*/loopbreakcondition/rowcheck", "org.jxls.reader.OffsetRowCheckImpl");
        digester.addSetProperties("*/loopbreakcondition/rowcheck");
        digester.addSetNext("*/loopbreakcondition/rowcheck", "addRowCheck");
        digester.addObjectCreate("*/rowcheck/cellcheck", "org.jxls.reader.OffsetCellCheckImpl");
        digester.addSetProperties("*/rowcheck/cellcheck");
        digester.addCallMethod("*/rowcheck/cellcheck", "setValue", 1);
        digester.addCallParam("*/rowcheck/cellcheck", 0);
        digester.addSetNext("*/rowcheck/cellcheck", "addCellCheck");
        return (XLSReader) digester.parse( xmlStream );
    }

    public static XLSReader buildFromXML(File xmlFile) throws IOException, SAXException {
        InputStream xmlStream = new BufferedInputStream( new FileInputStream(xmlFile) );
        XLSReader reader = buildFromXML( xmlStream );
        xmlStream.close();
        return reader;
    }

    public ReaderBuilder addSheetReader(String sheetName) {
        XLSSheetReader sheetReader = new XLSSheetReaderImpl();
        reader.addSheetReader( sheetName, sheetReader );
        currentSheetReader = sheetReader;
        lastSheetReader = true;
        return this;
    }

    public XLSReader getReader() {
        return reader;
    }

    public ReaderBuilder addSimpleBlockReader(int startRow, int endRow) {
        SimpleBlockReader blockReader = new SimpleBlockReaderImpl( startRow, endRow );
        if( lastSheetReader ){
            currentSheetReader.addBlockReader( blockReader );
        }else{
            currentLoopBlockReader.addBlockReader( blockReader );
        }
        currentSimpleBlockReader = blockReader;
        return this;
    }

    public ReaderBuilder addMapping(String cellName, String propertyName) {
        BeanCellMapping mapping = new BeanCellMapping( cellName, propertyName );
        currentSimpleBlockReader.addMapping( mapping );
        return this;
    }

    public ReaderBuilder addLoopBlockReader(int startRow, int endRow, String items, String varName, Class varType) {
        XLSLoopBlockReader loopReader = new XLSForEachBlockReaderImpl(startRow, endRow, items, varName, varType);
        if( lastSheetReader ){
            currentSheetReader.addBlockReader( loopReader );
        }else{
            currentLoopBlockReader.addBlockReader( loopReader );
        }
        currentLoopBlockReader = loopReader;
        return this;
    }

    public ReaderBuilder addLoopBreakCondition(){
        SectionCheck condition = new SimpleSectionCheck();
        currentLoopBlockReader.setLoopBreakCondition( condition );
        currentSectionCheck = condition;
        return this;
    }

    public ReaderBuilder addOffsetRowCheck(int offset){
        OffsetRowCheck rowCheck = new OffsetRowCheckImpl( offset );
        currentSectionCheck.addRowCheck( rowCheck );
        currentRowCheck = rowCheck;
        return this;
    }

    public ReaderBuilder addOffsetCellCheck(short offset, String value){
        OffsetCellCheck cellCheck = new OffsetCellCheckImpl( offset, value );
        currentRowCheck.addCellCheck( cellCheck );
        return this;
    }
    // todo:
    public ReaderBuilder addSimpleBlockReaderToParent(){
        return this;
    }
    // todo:
    public ReaderBuilder addLoopBlockReaderToParent(){
        return this;
    }
}
