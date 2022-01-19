package org.jxls.reader.dynamic;

import org.apache.commons.digester3.Digester;
import org.jxls.reader.*;
import org.xml.sax.SAXException;

import java.io.*;

/**
 * @author 张江平
 */
public class DynamicReaderBuilder {
    XLSReader reader = new XLSReaderImpl();
    XLSSheetReader currentSheetReader;
    SimpleBlockReader currentSimpleBlockReader;
    XLSLoopBlockReader currentLoopBlockReader;
    boolean lastSheetReader = false;
    SectionCheck currentSectionCheck;
    OffsetRowCheck currentRowCheck;


    public static XLSReader buildFromXML(InputStream xmlStream) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addObjectCreate("workbook", XLSDynamicReaderImpl.class);
        digester.addObjectCreate("workbook/worksheet", XLSDynamicSheetReaderImpl.class);
        digester.addSetProperties("workbook/worksheet", "name", "sheetName");
        digester.addSetProperties("workbook/worksheet", "idx", "sheetIdx");
        digester.addSetProperties("workbook/worksheet", "dynamic", "dynamic");
//        digester.addSetProperty("workbook/worksheet", "sheetName", "name");
        digester.addSetNext("workbook/worksheet", "addSheetReader");
        digester.addObjectCreate("*/loop", XLSForEachBlockReaderImpl.class);
        digester.addSetProperties("*/loop");
        digester.addSetNext("*/loop", "addBlockReader");
        digester.addObjectCreate("*/section", SimpleBlockReaderImpl.class);
        digester.addSetProperties("*/section");
        digester.addSetNext("*/section", "addBlockReader");
        digester.addObjectCreate("*/mapping", BeanCellMapping.class);
        digester.addSetProperties("*/mapping");
        digester.addCallMethod("*/mapping", "setFullPropertyName", 1);
        digester.addCallParam("*/mapping", 0);
        digester.addSetNext("*/mapping", "addMapping");
        digester.addObjectCreate("*/loop/loopbreakcondition", SimpleSectionCheck.class);
        digester.addSetNext("*/loop/loopbreakcondition", "setLoopBreakCondition");
        digester.addObjectCreate("*/loopbreakcondition/rowcheck", OffsetRowCheckImpl.class);
        digester.addSetProperties("*/loopbreakcondition/rowcheck");
        digester.addSetNext("*/loopbreakcondition/rowcheck", "addRowCheck");
        digester.addObjectCreate("*/rowcheck/cellcheck", OffsetCellCheckImpl.class);
        digester.addSetProperties("*/rowcheck/cellcheck");
        digester.addCallMethod("*/rowcheck/cellcheck", "setValue", 1);
        digester.addCallParam("*/rowcheck/cellcheck", 0);
        digester.addSetNext("*/rowcheck/cellcheck", "addCellCheck");
        return (XLSReader) digester.parse(xmlStream);
    }

    public static XLSReader buildFromXML(File xmlFile) throws IOException, SAXException {
        InputStream xmlStream = new BufferedInputStream(new FileInputStream(xmlFile));
        XLSReader reader = buildFromXML(xmlStream);
        xmlStream.close();
        return reader;
    }

    public DynamicReaderBuilder addSheetReader(String sheetName) {
        XLSSheetReader sheetReader = new XLSSheetReaderImpl();
        reader.addSheetReader(sheetName, sheetReader);
        currentSheetReader = sheetReader;
        lastSheetReader = true;
        return this;
    }

    public XLSReader getReader() {
        return reader;
    }

    public DynamicReaderBuilder addSimpleBlockReader(int startRow, int endRow) {
        SimpleBlockReader blockReader = new SimpleBlockReaderImpl(startRow, endRow);
        if (lastSheetReader) {
            currentSheetReader.addBlockReader(blockReader);
        } else {
            currentLoopBlockReader.addBlockReader(blockReader);
        }
        currentSimpleBlockReader = blockReader;
        return this;
    }

    public DynamicReaderBuilder addMapping(String cellName, String propertyName) {
        BeanCellMapping mapping = new BeanCellMapping(cellName, propertyName);
        currentSimpleBlockReader.addMapping(mapping);
        return this;
    }

    public DynamicReaderBuilder addLoopBlockReader(int startRow, int endRow, String items, String varName, Class varType) {
        XLSLoopBlockReader loopReader = new XLSForEachBlockReaderImpl(startRow, endRow, items, varName, varType);
        if (lastSheetReader) {
            currentSheetReader.addBlockReader(loopReader);
        } else {
            currentLoopBlockReader.addBlockReader(loopReader);
        }
        currentLoopBlockReader = loopReader;
        return this;
    }

    public DynamicReaderBuilder addLoopBreakCondition() {
        SectionCheck condition = new SimpleSectionCheck();
        currentLoopBlockReader.setLoopBreakCondition(condition);
        currentSectionCheck = condition;
        return this;
    }

    public DynamicReaderBuilder addOffsetRowCheck(int offset) {
        OffsetRowCheck rowCheck = new OffsetRowCheckImpl(offset);
        currentSectionCheck.addRowCheck(rowCheck);
        currentRowCheck = rowCheck;
        return this;
    }

    public DynamicReaderBuilder addOffsetCellCheck(short offset, String value) {
        OffsetCellCheck cellCheck = new OffsetCellCheckImpl(offset, value);
        currentRowCheck.addCellCheck(cellCheck);
        return this;
    }

    // todo:
    public DynamicReaderBuilder addSimpleBlockReaderToParent() {
        return this;
    }

    // todo:
    public DynamicReaderBuilder addLoopBlockReaderToParent() {
        return this;
    }
}
