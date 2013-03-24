package org.sevensource.wro4spring.wro4j.development;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.sevensource.wro4spring.wro4j.EnhancedGroupExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Inject;

public class GroupPerFileGroupExtractor extends EnhancedGroupExtractor {

	private final static Logger logger = LoggerFactory
			.getLogger(GroupPerFileGroupExtractor.class);

	@Inject
	private WroModelFactory modelFactory;

	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getGroupName(final HttpServletRequest request) {
				
		if (request == null) {
			throw new IllegalArgumentException("Request cannot be NULL!");
		}

		String uri = getUri(request);

		WroModel model = modelFactory.create();
		WroModelInspector modelInspector = new WroModelInspector(model);
		

		if (logger.isDebugEnabled()) {
			logger.debug("Finding GroupPerFile from uri [{}]", uri);
		}

		boolean process = true;
		String groupName = null;
		
		while (process) {

			String tempGroup = GroupPerFileModelTransformer.filenameToGroupname(uri);

			if (logger.isDebugEnabled()) {
				logger.debug("Checking whether group [{}] exists", tempGroup);
			}

			if (modelInspector.hasGroup(tempGroup)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found group [{}]", uri);
				}

				process = false;
				groupName = tempGroup;
				
			} else {
				final int i = uri.indexOf("/");

				if (i < 0) {
					process = false;
				} else {
					uri = uri.substring(i + 1);
				}
			}
		}
		
		return StringUtils.isEmpty(groupName) ? super.getGroupName(request) : groupName;
	}
	
	
	protected String getUri(final HttpServletRequest request) {
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();

		if (uri.startsWith(contextPath)) {
			// Strip contextPath if present
			uri = uri.substring(contextPath.length());
		}
		return uri;
	}
}
