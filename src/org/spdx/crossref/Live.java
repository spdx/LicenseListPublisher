package org.spdx.crossref;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class Live implements Callable<Boolean>  {
	String url;

    public Live(String url) {
        this.url = url;
    }
	
	public static boolean urlLinkExists(String URLName){
	    try {
	      HttpURLConnection.setFollowRedirects(false);
	      HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
	      con.setRequestMethod("HEAD");
	      con.setConnectTimeout(5000);
	      return (con.getResponseCode() == HttpURLConnection.HTTP_OK || con.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED);
	    }
	    catch (Exception e) {
	       return false;
	    }
	  }
	
	@Override
	public Boolean call() throws Exception {
		// TODO Auto-generated method stub
		return urlLinkExists(url);
	}

}
