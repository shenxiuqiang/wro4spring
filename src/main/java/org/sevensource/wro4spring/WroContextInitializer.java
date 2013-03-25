package org.sevensource.wro4spring;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.springframework.web.context.ServletContextAware;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.WroServletContextListener;
import ro.isdc.wro.http.support.ServletContextAttributeHelper;
import ro.isdc.wro.manager.factory.WroManagerFactory;

/**
 * An extended {@link WroServletContextListener}, which listens for Spring
 * Events instead of ServletContextEvents. {@link WroServletContextListener}
 * registers the effective {@link WroConfiguration} and the
 * {@link WroManagerFactory} with the {@link ServletContext}<br>
 * This class should be instantiated by Spring and not by the ServletContainer
 * (ie. web.xml).
 * 
 * @author pgaschuetz
 * 
 */
public class WroContextInitializer extends WroServletContextListener implements
		ServletContextAware {

	private ServletContext servletContext;

	private final WroManagerFactory wroManagerFactory;
	private final WroConfiguration wroConfiguration;

	public WroContextInitializer(final WroManagerFactory wroManagerFactory,
			final WroConfiguration wroConfiguration) {
		this.wroManagerFactory = wroManagerFactory;
		this.wroConfiguration = wroConfiguration;
	}

	@PostConstruct
	public void postConstruct() {
		setManagerFactory(wroManagerFactory);
		setConfiguration(wroConfiguration);

		ServletContextEvent servletContextEvent = new ServletContextEvent(
				servletContext);
		contextInitialized(servletContextEvent);
	}

	@PreDestroy
	public void preDestroy() {
		ServletContextEvent servletContextEvent = new ServletContextEvent(
				servletContext);
		contextDestroyed(servletContextEvent);
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
