/**
 * @version 1.0 28.07.2007
 * @author Leonid Vysochyn
 */
package org.jxls.reader;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.ByteConverter;
import org.apache.commons.beanutils.converters.CharacterConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.ShortConverter;

public class ReaderConfig {
    private static ReaderConfig ourInstance = new ReaderConfig();

    private boolean skipErrors = false;
    private boolean useDefaultValuesForPrimitiveTypes = true;

    /**
     * The default value for Character conversions.
     */
    private static Character defaultCharacter = new Character(' ');
    /**
     * The default value for Byte conversions.
     */
    private static Byte defaultByte = new Byte((byte) 0);
    /**
     * The default value for Boolean conversions.
     */
    private static Boolean defaultBoolean = Boolean.FALSE;
    /**
     * The default value for Double conversions.
     */
    private static Double defaultDouble = new Double((double) 0.0);
    /**
     * The default value for Float conversions.
     */
    private static Float defaultFloat = new Float((float) 0.0);
    /**
     * The default value for Integer conversions.
     */
    private static Integer defaultInteger = new Integer(0);
    /**
     * The default value for Long conversions.
     */
    private static Long defaultLong = new Long((long) 0);
    /**
     * The default value for Short conversions.
     */
    private static Short defaultShort = new Short((short) 0);
   
    public static ConvertUtilsBean createConvertUtilsBean(boolean useDefaultValuesForPrimitiveTypes ){
    	return initializeConverters( useDefaultValuesForPrimitiveTypes );
    }
    
    private ConvertUtilsBean convertUtilsBean = null;
    
    public ConvertUtilsBean getConvertUtilsBean(){
    	if( convertUtilsBean==null ){
    		convertUtilsBean = initializeConverters( this.useDefaultValuesForPrimitiveTypes );
    	}
    	return convertUtilsBean;
    }
    
    public static ReaderConfig getInstance() {
        return ourInstance;
    }

    private ReaderConfig() {
        setUseDefaultValuesForPrimitiveTypes( false );
    }

    public boolean isSkipErrors() {
        return skipErrors;
    }

    public void setSkipErrors(boolean skipErrors) {
        this.skipErrors = skipErrors;
    }


    public boolean isUseDefaultValuesForPrimitiveTypes() {
        return useDefaultValuesForPrimitiveTypes;
    }

    public void setUseDefaultValuesForPrimitiveTypes(boolean useDefaultValuesForPrimitiveTypes) {
        this.useDefaultValuesForPrimitiveTypes = useDefaultValuesForPrimitiveTypes;
    }

	private static ConvertUtilsBean initializeConverters(boolean useDefaultValuesForPrimitiveTypes ) {
		ConvertUtilsBean converterUtilsBean = new ConvertUtilsBean(); 
		Converter integerConverter;
        Converter doubleConverter;
        Converter longConverter;
        Converter shortConverter;
        Converter booleanConverter;
        Converter floatConverter;
        Converter characterConverter;
        Converter byteConverter;
        if( useDefaultValuesForPrimitiveTypes ){
            integerConverter = new IntegerConverter( defaultInteger );
            byteConverter = new ByteConverter( defaultByte );
            doubleConverter = new DoubleConverter( defaultDouble);
            longConverter = new LongConverter( defaultLong );
            shortConverter = new ShortConverter( defaultShort );
            booleanConverter = new BooleanConverter( defaultBoolean );
            floatConverter = new FloatConverter( defaultFloat );
            characterConverter = new CharacterConverter( defaultCharacter );
        }else{
            integerConverter = new IntegerConverter();
            byteConverter = new ByteConverter(  );
            doubleConverter = new DoubleConverter();
            longConverter = new LongConverter();
            shortConverter = new ShortConverter();
            booleanConverter = new BooleanConverter();
            floatConverter = new FloatConverter();
            characterConverter = new CharacterConverter();
        }
        converterUtilsBean.register( integerConverter, Integer.TYPE);
        converterUtilsBean.register( integerConverter, Integer.class);
        converterUtilsBean.register( byteConverter, Byte.TYPE);
        converterUtilsBean.register( byteConverter, Byte.class);
        converterUtilsBean.register( doubleConverter, Double.TYPE);
        converterUtilsBean.register( doubleConverter, Double.class);
        converterUtilsBean.register( longConverter, Long.TYPE);
        converterUtilsBean.register( longConverter, Long.class);
        converterUtilsBean.register( shortConverter, Short.TYPE);
        converterUtilsBean.register( shortConverter, Short.class);
        converterUtilsBean.register( booleanConverter, Boolean.TYPE);
        converterUtilsBean.register( booleanConverter, Boolean.class);
        converterUtilsBean.register( floatConverter, Float.TYPE);
        converterUtilsBean.register( floatConverter, Float.class);
        converterUtilsBean.register( characterConverter, Character.TYPE);
        converterUtilsBean.register( characterConverter, Character.class);
        converterUtilsBean.register( new DateConverter(), java.util.Date.class);
        return converterUtilsBean;
	}
}
