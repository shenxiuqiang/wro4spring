package org.sevensource.wro4spring.config;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.sevensource.wro4spring.CachingHeaders;
import org.sevensource.wro4spring.WroContextInitializer;
import org.sevensource.wro4spring.wro4j.EnhancedGroupExtractor;
import org.sevensource.wro4spring.wro4j.ModelResourceAlterationWatcher;
import org.sevensource.wro4spring.wro4j.WroContextSupport;
import org.sevensource.wro4spring.wro4j.development.GroupPerFileGroupExtractor;
import org.sevensource.wro4spring.wro4j.development.GroupPerFileModelTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.ConfigurableWroFilter;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;

public abstract class AbstractWro4SpringConfiguration {

	public final static String WRO_FILTER_BEAN_NAME = "wroFilter";
	
	private final static Logger logger = LoggerFactory.getLogger(AbstractWro4SpringConfiguration.class);
	
	protected final static String NO_CACHE_HEADERS = CachingHeaders.NO_CACHE.toWroHeader();
	protected final static String NEVER_EXPIRES_HEADERS = CachingHeaders.NEVER_EXPIRES.toWroHeader();


	/**
	 * Create the wro filter.
	 * 
	 * @return
	 */
	@Bean(name = WRO_FILTER_BEAN_NAME)
	public ConfigurableWroFilter wroFilter(WroManagerFactory wroManagerFactory, WroConfiguration wroConfiguration) {
		ConfigurableWroFilter filter = new ConfigurableWroFilter();
		filter.setConfiguration(wroConfiguration);
		filter.setWroManagerFactory(wroManagerFactory);
		return configureFilter(filter);
	}

	
	/**
	 * Save {@link WroManagerFactory} and {@link WroConfiguration} in the {@link ServletContext}
	 * @param wroManagerFactory
	 * @param wroConfiguration
	 * @return
	 */
	@Bean
	public WroContextInitializer wroContextInitializer(WroManagerFactory wroManagerFactory, WroConfiguration wroConfiguration) {
		WroContextInitializer initializer = new WroContextInitializer(wroManagerFactory, wroConfiguration);
		return initializer;
	}
	
	
	/**
	 * 
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@Bean
	public WroManagerFactory wroManagerFactory() throws InstantiationException, IllegalAccessException {
		BaseWroManagerFactory wroManagerFactory = wroManagerFactoryClass().newInstance();
		wroManagerFactory.setModelFactory(wroModelFactory());
		wroManagerFactory.setUriLocatorFactory(wroUriLocatorFactory());
		wroManagerFactory.setProcessorsFactory(createProcessorsFactory());
		wroManagerFactory.setGroupExtractor( groupExtractor() );
		
		if(isDevelopment()) {
			wroManagerFactory.addModelTransformer( new GroupPerFileModelTransformer() );
		}
		
		return configureManagerFactory(wroManagerFactory);
	}
	
	protected Class<? extends BaseWroManagerFactory> wroManagerFactoryClass() {
		return BaseWroManagerFactory.class;
	}
	
	
	@Bean
	public GroupExtractor groupExtractor() {
		if(isDevelopment()) {
			return new GroupPerFileGroupExtractor();
		} else {
			return new EnhancedGroupExtractor();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	@Bean
	public WroConfiguration wroConfiguration() {
		WroConfiguration configuration = new WroConfiguration();
		configuration.setDebug( isDevelopment() );
		
		configuration.setEncoding("UTF-8");
		configuration.setIgnoreMissingResources(false);
		configuration.setIgnoreFailingProcessor(false);
		
		if(isDevelopment()) {
			configuration.setResourceWatcherUpdatePeriod(1l);
			configuration.setCacheUpdatePeriod(3600);
			configuration.setHeader( NO_CACHE_HEADERS );
		} else {
			configuration.setHeader( NEVER_EXPIRES_HEADERS );
		}
		return configureConfiguration(configuration);
	}
	
	
	@Bean
	public WroContextSupport wroContextSupport() {
		return new WroContextSupport();
	}
	
	/**
	 * watch for file changes of the model and reload if necessary.
	 * 
	 * @param wroConfiguration
	 * @return
	 */
	@Bean
	public ModelResourceAlterationWatcher modelReloader(WroConfiguration wroConfiguration) {
		Resource wroFileResource = getFileAsResource( getWroFile() );
		ModelResourceAlterationWatcher modelReloader = new ModelResourceAlterationWatcher(wroFileResource, wroConfiguration);
		return modelReloader;
	}

	/**
	 * 
	 * @return
	 */
	protected WroModelFactory wroModelFactory() {
		XmlModelFactory modelFactory = new XmlModelFactory() {
			@Override
			protected InputStream getModelResourceAsStream() throws IOException {
				return getWroFileAsInputStream();
			}
		};
		return modelFactory;
	}

	/**
	 * 
	 * @return
	 */
	protected UriLocatorFactory wroUriLocatorFactory() {
		return new SimpleUriLocatorFactory()
			.addUriLocator(new ClasspathUriLocator())
			.addUriLocator(new ServletContextUriLocator());
	}

	
	private static Resource getFileAsResource(String filename) {
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource resource = loader.getResource(filename);
		return resource;
	}
	
	private InputStream getWroFileAsInputStream() {
		final String wroFile = getWroFile();

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Using wroFile [{}]", wroFile);
			}

			Resource wroResource = getFileAsResource(wroFile);
			return wroResource.getInputStream();
		} catch (IOException e) {
			logger.error("Error: IOException", e);
			throw new IllegalArgumentException(e);
		}
	}

	
	
	protected ConfigurableWroFilter configureFilter(ConfigurableWroFilter filter) {
		return filter;
	}
	
	protected WroManagerFactory configureManagerFactory(WroManagerFactory managerFactory) {
		return managerFactory;
	}
	
	protected WroConfiguration configureConfiguration(WroConfiguration configuration) {
		return configuration;
	}
	
	/**
	 * 
	 * @return wro4j's model file (ie. wro.xml). Can be returned in Spring's
	 *         {@link Resource} syntax
	 * @see DefaultResourceLoader
	 */
	protected abstract String getWroFile();
	protected abstract ProcessorsFactory createProcessorsFactory();
	protected abstract boolean isDevelopment();
}
