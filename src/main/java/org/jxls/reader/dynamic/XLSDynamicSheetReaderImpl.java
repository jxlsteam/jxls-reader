package org.jxls.reader.dynamic;

import org.jxls.reader.XLSSheetReader;
import org.jxls.reader.XLSSheetReaderImpl;

/**
 * 动态页处理器
 *
 * @author 张江平
 */
public class XLSDynamicSheetReaderImpl extends XLSSheetReaderImpl implements XLSSheetReader ,DynamicAble {
    private boolean dynamic = false;

    @Override
    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
}
