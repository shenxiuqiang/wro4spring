package org.sevensource.wro4spring.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.processor.PathPatternProcessorDecorator;
import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExtensionsAwareProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.ConformColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.ConsoleStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;

import com.google.javascript.jscomp.CompilationLevel;

public abstract class DefaultAbstractWro4SpringConfiguration extends AbstractWro4SpringConfiguration {
	
	
	private final static Logger logger = LoggerFactory.getLogger(DefaultAbstractWro4SpringConfiguration.class);
	
	/**
	 * 
	 * @return
	 */
	protected ProcessorsFactory createProcessorsFactory() {
		SimpleProcessorsFactory processorsFactory = new SimpleProcessorsFactory();

		//CSS
		processorsFactory.addPreProcessor(
				ExtensionsAwareProcessorDecorator.decorate(new LessCssProcessor()).addExtension("less").addExtension("lessCss")
				);

		processorsFactory.addPreProcessor(new CssImportPreProcessor());
		processorsFactory.addPreProcessor(new CssUrlRewritingProcessor());
		processorsFactory.addPreProcessor(new ConformColorsCssProcessor());
		
		if(isDevelopment()) {
			processorsFactory.addPreProcessor(new CssLintProcessor());
		} else {
			processorsFactory.addPostProcessor( new YUICssCompressorProcessor() );
		}
		
		
		//JS PreProcessors
		if(! isDevelopment()) {
			processorsFactory.addPreProcessor(new SemicolonAppenderPreProcessor());
			processorsFactory.addPreProcessor(
					PathPatternProcessorDecorator.exclude(new ConsoleStripperProcessor(), "**/*.min.js").getDecoratedObject()
					);
			
			processorsFactory.addPreProcessor(
					PathPatternProcessorDecorator.exclude(new GoogleClosureCompressorProcessor(CompilationLevel.SIMPLE_OPTIMIZATIONS), "**/*.min.js").getDecoratedObject()
					);
		}
		
		
		return processorsFactory;
	}
	
	@Override
	protected String getWroFile() {
		return "classpath:wro.xml";
	}
	
}
