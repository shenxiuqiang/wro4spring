package org.sevensource.wro4spring.wro4j.development;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.util.Transformer;

public class GroupPerFileModelTransformer implements Transformer<WroModel> {

	private final static Logger logger = LoggerFactory.getLogger(GroupPerFileModelTransformer.class);
	
	@Override
	public WroModel transform(WroModel input) throws Exception {
		
		WroModel developmentModel = new WroModel();
		WroModelInspector inspector = new WroModelInspector(developmentModel);
		
		for(Resource resource : new WroModelInspector(input).getAllUniqueResources()) {
			final String groupName = filenameToGroupname(resource.getUri());
			
			Group group;
			
			/**
			 * if there is already a group of the same name (ie. JS and CSS with the same name), add the resource
			 * to the existing group, but only if it does not contain a resource of the same type.
			 */
			if(inspector.hasGroup(groupName)) {
				group = inspector.getGroupByName(groupName);
				
				if(group.hasResourcesOfType(resource.getType())) {
					final String msg = String.format("Group [%s] already contains a resource of type [%s]", groupName, resource.getType().name());
					final IllegalArgumentException iae = new IllegalArgumentException(msg);
					logger.error("wro4j configuration error", iae);
					throw iae;
				}
			} else {
				group = new Group(groupName);
				developmentModel.addGroup(group);
			}

			group.addResource(resource);
		}
		
		if (logger.isDebugEnabled()) {
			for(Group devGroup : developmentModel.getGroups()) {
				logger.debug("Created wro4j group [{}]", devGroup.getName());
			}
		}
		
		developmentModel.merge(input);
		
		return developmentModel;
	}
	
	
    /**
     * Convert the given file's name to a wro group's name.
     *
     * @param filename The file's name.
     * @return The group's name.
     */
    public static String filenameToGroupname(final String filename) {
      String group = filename;
      
      if (group.startsWith("/")) {
        group = group.substring(1);
      }
      
      group = FilenameUtils.removeExtension(group);
      //return group.replace("/", "_");
      return group;
    }
}
