package org.sevensource.wro4spring;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.ServletContextAware;

import ro.isdc.wro.http.WroFilter;

/**
 * 
 * @author pgaschuetz
 *
 */
public class WroDeliveryConfiguration implements ServletContextAware {
	private String contextPath;
	private String uriPrefix;
	private List<String> cdnDomains;
	private boolean development = false;
	
	private volatile String deliveryPrefix = null;
	private volatile String localPathPrefix = null;
	
	public String getUriPrefix() {
		return uriPrefix;
	}
	
	/**
	 * A path that every resource should have prefixed (excl. context path).<br>
	 * ie.<br>
	 * given <code>uriPrefix=/static/bundles/</code><br>
	 * Resource <code>/style.css</code> becomes <code>/static/bundles/style.css</css><br>
	 * The uriPrefix should be a path, that {@link WroFilter} matches on. 
	 * @param uriPrefix
	 */
	public void setUriPrefix(String uriPrefix) {
		this.uriPrefix = uriPrefix;
	}
	
	/**
	 * a domain, that should be prepended to every resource.<br>
	 * only set the domain, not the protocol
	 * @param domain
	 */
	public void setCdnDomain(String domain) {
		cdnDomains = new ArrayList<String>();
		cdnDomains.add(domain);
	}
	
	public String getCdnDomain() {
		return cdnDomains == null ? null : cdnDomains.get(0);
	}
	
	public boolean isDevelopment() {
		return development;
	}
	
	public void setDevelopment(boolean development) {
		this.development = development;
	}
	
	public String getContextPath() {
		return contextPath;
	}
	
	public String encodeDeliveryInformationIntoUri(String uri) {
		return deliveryPrefix + uri;
	}
	
	public String encodeLocalPathPrefixIntoUri(String uri, boolean includeServletContextPath) {
		return includeServletContextPath ? localPathPrefix + uri : getUriPrefix() + uri;
	}
	
	@PostConstruct
	public void afterPropertiesSet() {
		StringBuilder encoded = new StringBuilder();
		if (!StringUtils.isEmpty(getCdnDomain())) {
			encoded.append("//").append(getCdnDomain());
		}

		if (!StringUtils.isEmpty(getContextPath())) {
			encoded.append(getContextPath());
		}

		if (!StringUtils.isEmpty(getUriPrefix())) {
			encoded.append(getUriPrefix());
		}
		
		deliveryPrefix = encoded.toString();
		
		localPathPrefix = getContextPath() + getUriPrefix();
	}
	
	/**
	 * {@link ServletContext} automatically set by Springs {@link ServletContextAware} functionality
	 */
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.contextPath = servletContext.getContextPath();
	}
}