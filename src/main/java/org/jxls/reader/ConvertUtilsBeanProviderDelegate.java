package org.jxls.reader;

import org.apache.commons.beanutils.ConvertUtilsBean;

public class ConvertUtilsBeanProviderDelegate implements ConvertUtilsBeanProvider{

	private ConvertUtilsBeanProvider delegate;
	
	public ConvertUtilsBeanProviderDelegate(){
		
	}
	
	public ConvertUtilsBeanProviderDelegate(final ConvertUtilsBean convertUtilsBean2) {
		this.delegate = new ConvertUtilsBeanProvider() {
			@Override
			public ConvertUtilsBean getConvertUtilsBean() {
				return convertUtilsBean2;
			}
		};
	}


	public ConvertUtilsBean getConvertUtilsBean(){
		ConvertUtilsBean result;
		if( delegate != null ){
			result = delegate.getConvertUtilsBean();
		}else{
			result = ReaderConfig.getInstance().getConvertUtilsBean();
		}
		return result;
	}
	
	public void setDelegate( ConvertUtilsBeanProvider convertUtilsBean2){
		delegate = convertUtilsBean2;
	}
}
