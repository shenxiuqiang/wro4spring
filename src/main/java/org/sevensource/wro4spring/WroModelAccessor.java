package org.sevensource.wro4spring;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.sevensource.wro4spring.WroContextSupport.ContextTemplate;
import org.sevensource.wro4spring.wro4j.development.GroupPerFileModelTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.support.ServletContextAttributeHelper;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

public class WroModelAccessor implements IWroModelAccessor {
	
	
	private final static Logger logger = LoggerFactory.getLogger(WroModelAccessor.class);
	
	private HttpServletRequest request;
	private WroContextSupport wroContextSupport;
	private ServletContextAttributeHelper servletContextAttributeHelper;
	private WroDeliveryConfiguration wroDeliveryConfiguration;
	
	public WroModelAccessor() {
		if (logger.isDebugEnabled()) {
			logger.debug("New instance of ModelAccessor");
		}
	}
	
	public WroModelAccessor(HttpServletRequest request, WroDeliveryConfiguration wroDeliveryConfiguration, WroContextSupport wroContextSupport) {
		setRequest(request);
		setWroDeliveryConfiguration(wroDeliveryConfiguration);
		setWroContextSupport(wroContextSupport);
	}
	
	
	@Override
	public List<String> js(final String groupname) {
		return resources(groupname, ResourceType.JS);
	}
	
	
	@Override
	public List<String> css(final String groupname) {
		return resources(groupname, ResourceType.CSS);
	}
	
	
	public List<String> resources(final String groupname,
			final ResourceType resourceType) {
		final List<String> resources =

		wroContextSupport.doInContext(request, null,
				new ContextTemplate<List<String>>() {

					@Override
					public List<String> execute() {
						return getVersionedResourceUrisByGroupName(groupname,
										resourceType, wroDeliveryConfiguration.isDevelopment());
					}
				});

		return resources;
	}
	
	
	/**
	 * Builds a URL accoring to the {@link WroDeliveryConfiguration}
	 * 
	 * @param uri
	 *            a resources URI as returned from wro4j
	 * @return the resources full URL including domain, context path and prefix
	 * @see WroDeliveryConfiguration#getCdnDomain()
	 * @see WroDeliveryConfiguration#getContextPath()
	 * @see WroDeliveryConfiguration#getUriPrefix()
	 */
	
	protected String encodeDeliveryPathIntoUri(String uri) {
		return wroDeliveryConfiguration.encodeDeliveryInformationIntoUri(uri);
	}
	
	protected List<String> getVersionedResourceUrisByGroupName(String groupName, ResourceType resourceType, boolean isDevelopment) {
		
		final Group requestedGroup = getGroup(groupName);

		final List<String> groupNames = new ArrayList<String>();

		if (! isDevelopment) {
			groupNames.add(requestedGroup.getName());
		} else {
			// in development mode, add a group for each resource of the
			// requested group
			for (Resource resource : requestedGroup.getResources()) {
				if (resourceType.equals(resource.getType())) {
					String fileGroupname = GroupPerFileModelTransformer
							.filenameToGroupname(resource.getUri());
					groupNames.add(fileGroupname);
				}
			}
		}

		final List<String> uris = new ArrayList<String>(groupNames.size());
		
		for (String renderGroupName : groupNames) {
			
			/**
			 * WroManager#encodeVersionIntoGroupPath() uses a CacheKey to get the MD5 of the resource being encoded.
			 * This, in turn initializes, wro4j completly and - if configured - also rewrites URLs in CSS files. Since these
			 * calculations are relative to the URL being requested, we need to create a URL, which has the same folder depth
			 * as the final URL that will be served by wro4j. 
			 */
			
			final String directoryName = FilenameUtils.getFullPathNoEndSeparator(renderGroupName + "." + resourceType.toString()); 
			final String dummyUrl = wroDeliveryConfiguration.encodeLocalPathPrefixIntoUri("a-md5-hash-we-do-not-know-yet/" + directoryName , false);
			Context.get().setAggregatedFolderPath(dummyUrl);
			String uri = getWroManager().encodeVersionIntoGroupPath( renderGroupName, resourceType, !isDevelopment );
			Context.get().setAggregatedFolderPath(null);
			uris.add( encodeDeliveryPathIntoUri(uri) );
		}
		
		return uris;
		
	}
	
	/**
	 * Find a wro4j {@link Group} by its name
	 * 
	 * @param groupname
	 *            the requested {@link Group}
	 * @return
	 */
	private Group getGroup(String groupname) {
		WroModel model = getWroManager().getModelFactory().create();

		final WroModelInspector modelInspector = new WroModelInspector(model);
		final Group group = modelInspector.getGroupByName(groupname);

		if (group == null) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"Invalid groupname '" + groupname + "'");
			logger.error("Cannot find group", iae);
			throw iae;
		} else {
			return group;
		}
	}

	private WroManager getWroManager() {
		return servletContextAttributeHelper.getManagerFactory().create();
	}
	
	
	@Inject
	public void setRequest(HttpServletRequest request) {
		this.request = request;
		this.servletContextAttributeHelper = new ServletContextAttributeHelper(request.getServletContext());
	}
	
	@Inject
	public void setWroContextSupport(WroContextSupport wroContextSupport) {
		this.wroContextSupport = wroContextSupport;
	}
	
	@Inject
	public void setWroDeliveryConfiguration(WroDeliveryConfiguration wroDeliveryConfiguration) {
		this.wroDeliveryConfiguration = wroDeliveryConfiguration;
	}
}
