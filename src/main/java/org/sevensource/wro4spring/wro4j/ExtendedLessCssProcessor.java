package org.sevensource.wro4spring.wro4j;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.support.less.LessCss;

public class ExtendedLessCssProcessor extends LessCssProcessor {

	//public final static String LESS_JS_PATH = "classpath:/META-INF/resorces/wro4spring/less-1.3.3.js";
	public final static String LESS_JS_PATH = "classpath:/META-INF/resorces/wro4spring/less-rhino-1.3.3.js";

	private final static Logger logger = LoggerFactory.getLogger(ExtendedLessCssProcessor.class);
	
	protected LessCss newLessCss() {
		return new LessCss() {
			protected InputStream getScriptAsStream() {				
				try {
					ClassPathResource lessJs = new ClassPathResource(LESS_JS_PATH);
					InputStream is = lessJs.getInputStream();
					return is;
				} catch (IOException e) {
					logger.error("Error while loading less.js", e);
					throw new RuntimeException(e);
				}
			}
		};
	}
}