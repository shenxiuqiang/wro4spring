package org.sevensource.wro4spring.wro4j;

import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ServletContextAware;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.WroContextFilter;

/**
 * Helper class to execute a method inside of a "Wro Transaction" and thereby
 * not using {@link WroContextFilter}
 * 
 * @author pgaschuetz
 * 
 */
public class WroContextSupport implements ServletContextAware {

	private ServletContext servletContext;
	private WroFilterConfig wroFilterConfig;

	public void doInContext(HttpServletRequest request,
			HttpServletResponse response, ContextTemplate template) {

		boolean removeContext = false;

		try {
			removeContext = setContext(request, response);

			template.execute();

		} finally {
			if (removeContext) {
				unsetContext();
			}
		}
	}

	/**
	 * Create & Set a wro4j WebContext
	 * 
	 * @param request
	 * @param response
	 * @return true if a context has been set (and needs to be removed later on)
	 * 
	 * @see Context
	 */
	public boolean setContext(HttpServletRequest request,
			HttpServletResponse response) {
		if (!Context.isContextSet()) {
			Context.set(Context.webContext(request, response, wroFilterConfig));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * unsets a previously set wro4j {@link Context}
	 * 
	 * @see WroContextSupport#setContext(HttpServletRequest,
	 *      HttpServletResponse)
	 */
	public void unsetContext() {
		if (Context.isContextSet()) {
			Context.unset();
		}
	}

	@PostConstruct
	public void postConstruct() {
		wroFilterConfig = new WroFilterConfig(servletContext);
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public interface ContextTemplate {
		public void execute();
	}

	
	/**
	 * an internal dummy {@link FilterConfig}
	 * @author pgaschuetz
	 *
	 */
	private static final class WroFilterConfig implements FilterConfig {

		private ServletContext servletContext;

		public WroFilterConfig(final ServletContext servletContext) {
			this.servletContext = servletContext;
		}

		@Override
		public String getFilterName() {
			return "wroFilter";
		}

		@Override
		public ServletContext getServletContext() {
			return servletContext;
		}

		@Override
		public String getInitParameter(final String name) {
			return null;
		}

		@Override
		public Enumeration<String> getInitParameterNames() {
			return null;
		}
	}

}
