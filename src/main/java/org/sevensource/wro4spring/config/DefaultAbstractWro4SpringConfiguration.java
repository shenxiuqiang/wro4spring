package org.sevensource.wro4spring.config;

import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;

public abstract class DefaultAbstractWro4SpringConfiguration extends AbstractWro4SpringConfiguration {

	/**
	 * 
	 * @return
	 */
	protected ProcessorsFactory createProcessorsFactory() {
		SimpleProcessorsFactory processorsFactory = new SimpleProcessorsFactory();
		processorsFactory.addPreProcessor(new CssImportPreProcessor());
		processorsFactory.addPreProcessor(new CssUrlRewritingProcessor());
		processorsFactory.addPreProcessor(new JawrCssMinifierProcessor());
		
		processorsFactory.addPreProcessor(new SemicolonAppenderPreProcessor());
		processorsFactory.addPreProcessor(new JSMinProcessor());
		
		processorsFactory.addPostProcessor(new CssVariablesProcessor());
		
		return processorsFactory;
	}
	
	@Override
	protected String getWroFile() {
		return "classpath:wro.xml";
	}
	
}
