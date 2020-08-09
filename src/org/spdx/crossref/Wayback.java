package org.spdx.crossref;

import java.util.concurrent.Callable;

import org.apache.commons.validator.UrlValidator;

public class Wayback implements Callable<Boolean> {
	String url;

    public Wayback(String url) {
        this.url = url;
    }
    
    public static boolean isWayBackUrl(String url){
		// Check if url if from wayback machine
		for(int i = 0; i < UrlConstants.WAYBACK_URLS.length; i++) {
			if(url.contains(UrlConstants.WAYBACK_URLS[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Boolean call() throws Exception {
		// TODO Auto-generated method stub
		return isWayBackUrl(url);
	}

}
