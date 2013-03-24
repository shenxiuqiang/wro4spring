package org.sevensource.wro4spring.wro4j;

import ro.isdc.wro.model.group.DefaultGroupExtractor;
import ro.isdc.wro.model.resource.ResourceType;

public class EnhancedGroupExtractor extends DefaultGroupExtractor {

	/**
	 * {@link DefaultGroupExtractor} adds not only ?minimize=false, but also ?minimize=true
	 */
	@Override
	public String encodeGroupUrl(String groupName, ResourceType resourceType, boolean minimize) {
		String url = String.format("%s.%s", groupName, resourceType.name().toLowerCase());
		
		if(!minimize) {
			url = url + "?" + PARAM_MINIMIZE + "=false";
		}
		
		return url;
	}
	
}
