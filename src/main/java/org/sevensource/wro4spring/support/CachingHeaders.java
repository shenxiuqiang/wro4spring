package org.sevensource.wro4spring.support;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import ro.isdc.wro.util.WroUtil;


/**
 * Header configuration for Development and Production
 * 
 * @author pgaschuetz
 *
 */
public enum CachingHeaders {
	NEVER_EXPIRES {
		@Override
		protected String[] getHeaders() {
			final int oneYear = 60*60*24*365;
			return new String[] {
					"Cache-Control: public, max-age=" + oneYear + ",post-check=" + oneYear + ",pre-check=" + oneYear,
					"Last-Modified: " + WroUtil.toDateAsString( new Date().getTime() )
			};
		}
	},
	NO_CACHE {
		@Override
		protected String[] getHeaders() {
			return new String[] {
					"Cache-Control: no-store, no-cache, must-revalidate, max-age=0",
					"Pragma: no-cache",
					"Expires: Tue, 03 Jul 2001 06:00:00 GMT"
			};
		}
	};
	
	public String toWroHeader() {
		final String[] headers = getHeaders();
		return StringUtils.join(headers, "|");
	}
	
	protected abstract String [] getHeaders();
}
