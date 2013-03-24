package org.sevensource.wro4spring;

import org.springframework.web.filter.DelegatingFilterProxy;

public class DelegatingWroFilterProxy extends DelegatingFilterProxy {

	public DelegatingWroFilterProxy() {
		super();
		setTargetFilterLifecycle(true);
	}
}
