package org.spdx.crossref;

import java.util.concurrent.Callable;

import org.apache.commons.validator.UrlValidator;

public class Valid implements Callable<Boolean> {
	String url;

    public Valid(String url) {
        this.url = url;
    }
    
    public static boolean urlValidator(String url){
		// Get an UrlValidator using default schemes
		boolean containsInvalidUrl = false;
		for(int i = 0; i < UrlConstants.INVALID_URL_DOMAINS.length; i++) {
			if(url.contains(UrlConstants.INVALID_URL_DOMAINS[i])) {
				containsInvalidUrl = url.contains(UrlConstants.INVALID_URL_DOMAINS[i]);
			}
		}
		UrlValidator defaultValidator = new UrlValidator();
		return defaultValidator.isValid(url) && !containsInvalidUrl;
	}

	@Override
	public Boolean call() throws Exception {
		// TODO Auto-generated method stub
		return urlValidator(url);
	}

}