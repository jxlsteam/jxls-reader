package org.jxls.reader.dynamic;

import org.jxls.reader.BaseXLSReaderImpl;
import org.jxls.reader.XLSReader;
import org.jxls.reader.XLSReaderImpl;
import org.jxls.reader.XLSSheetReader;

import java.util.HashMap;
import java.util.Map;

/**
 * 动态模板支持
 *
 * @author 张江平
 */
public class XLSDynamicReaderImpl extends BaseXLSReaderImpl implements XLSReader {
    private Map<String, XLSSheetReader> dyncSheetReaders = new HashMap<>();

    @Override
    protected XLSSheetReader findSheetReader(String sheetName, int sheetNo) {
        XLSSheetReader sheetReader = super.findSheetReader(sheetName, sheetNo);
        if (sheetReader == null) {
            //动态匹配
            for (String vn : dyncSheetReaders.keySet()) {
                if (sheetName.matches(vn)) {
                    return dyncSheetReaders.get(vn);
                }
            }
        }
        return null;
    }

    @Override
    public void addSheetReader(String sheetName, XLSSheetReader reader) {
        if (reader instanceof DynamicAble) {
            if (((DynamicAble) reader).isDynamic()) {
                dyncSheetReaders.put(sheetName, reader);
                reader.setConvertUtilsBeanProvider(getConvertUtilsBeanProvider());
            }
        }
        super.addSheetReader(sheetName, reader);
    }
}
