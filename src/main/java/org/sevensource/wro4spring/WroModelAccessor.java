package org.sevensource.wro4spring;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.sevensource.wro4spring.WroContextSupport.ContextTemplate;
import org.sevensource.wro4spring.wro4j.development.GroupPerFileModelTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private boolean isDevelopment = true;
	
	public WroModelAccessor() {
	}
	
	public WroModelAccessor(HttpServletRequest request, WroContextSupport wroContextSupport) {
		setRequest(request);
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
			ResourceType resourceType) {
		final List<String> resources =

		wroContextSupport.doInContext(request, null,
				new ContextTemplate<List<String>>() {

					@Override
					public List<String> execute() {
						return getVersionedResourceUrisByGroupName(groupname,
										ResourceType.CSS, isDevelopment);
					}
				});

		return resources;
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
			String uri = getWroManager().encodeVersionIntoGroupPath( renderGroupName, resourceType, isDevelopment );
			uris.add(uri);
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
}
