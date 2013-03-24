package org.sevensource.wro4spring.wro4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;

/**
 * Alternative class to watch for wro4j model changes. Wro4j features a
 * modelUpdatePeriod, after which the model is discarded and recreated.<br>
 * In development mode, this is rather annoying, as the you want model changes
 * to be reflected instantly, but don't want to recreate the model on an ongoing
 * basis.<br>
 * <br>
 * This class watches for file changes of the given files and triggers a model
 * reload once a change has been detected
 * 
 * @author pgaschuetz
 * 
 */
public class ModelResourceAlterationWatcher {

	private final static Logger logger = LoggerFactory
			.getLogger(ModelResourceAlterationWatcher.class);

	private final static long DEFAULT_INTERVAL = 500;
	private long interval = DEFAULT_INTERVAL;
	private FileAlterationMonitor monitor;
	private List<Resource> watchResources;

	private WroConfiguration wroConfiguration;

	public ModelResourceAlterationWatcher(Resource watchResource,
			WroConfiguration wroConfiguration) {
		this.watchResources = new ArrayList<Resource>();
		this.watchResources.add(watchResource);

		this.wroConfiguration = wroConfiguration;
	}

	protected void onModelFileChange(String filename) {
		if (logger.isInfoEnabled()) {
			logger.info(
					"WroModel Resource [{}] has changed. Reloading WroModel.",
					filename);
		}
		try {
			wroConfiguration.reloadModel();
		} catch (WroRuntimeException wre) {
			if (logger.isInfoEnabled()) {
				logger.info("There has been an error reloading the model.");
			}
		}
	}

	/**
	 * create the {@link FileAlterationMonitor}, the
	 * {@link FileAlterationObserver}s and start the monitor once the bean has
	 * been created
	 */
	@PostConstruct
	public void postConstruct() {
		monitor = new FileAlterationMonitor(interval);

		for (Resource resource : watchResources) {
			String filename = resource.getFilename();
			if (filename == null) {
				throw new IllegalArgumentException("Filename is null");
			} else {

				if (logger.isInfoEnabled()) {
					logger.info("Watching wroModel file [{}] for changes",
							filename);
				}

				final IOFileFilter filter = FileFilterUtils.and(
						FileFilterUtils.fileFileFilter(),
						FileFilterUtils.nameFileFilter(filename));

				try {
					final File directory = resource.getFile().getParentFile();
					FileAlterationObserver observer = new FileAlterationObserver(
							directory, filter);

					observer.addListener(new FileAlterationListenerAdaptor() {
						@Override
						public void onFileChange(File file) {
							onModelFileChange(file.getName());
						}
					});

					monitor.addObserver(observer);
				} catch (IOException ioe) {
					logger.error("Error while creating FileAlterationObserver",
							ioe);
				}
			}
		}

		start();
	}

	@PreDestroy
	public void preDestroy() {
		stop();
	}

	public void start() {
		try {
			monitor.start();
		} catch (Exception e) {
			logger.error("Cannot start monitor", e);
		}
	}

	public void stop() {
		try {
			monitor.stop();
		} catch (Exception e) {
			logger.error("Cannot stop monitor", e);
		}
	}
}
