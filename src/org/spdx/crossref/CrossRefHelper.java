package org.spdx.crossref;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.spdx.rdfparser.SpdxRdfConstants;

public class CrossRefHelper implements Callable<String[]> {
	
	String[] crossRefUrls;

    public CrossRefHelper(String[] crossRefUrls) {
        this.crossRefUrls = crossRefUrls;
    }

	public static String[] buildUrlDetails(String[] crossRefUrls) {
		String[] urlDetails = new String[crossRefUrls.length];
		for(int i = 0; i < crossRefUrls.length; i++) {
			String url = crossRefUrls[i];
			
			ExecutorService executorService = Executors.newFixedThreadPool(20);

			Future<Boolean> isValid = executorService.submit(new Valid(url));
			Future<Boolean> isLive = executorService.submit(new Live(url));
			Future<Boolean> isWayback = executorService.submit(new Wayback(url));
			Future<String> timestamp = executorService.submit(new Timestamp(url));
			
			try {
				Boolean isValidUrl = isValid.get(3, TimeUnit.SECONDS);
		    	Boolean isLiveUrl = isLive.get(6, TimeUnit.SECONDS);
		    	Boolean isWaybackUrl = isWayback.get(3, TimeUnit.SECONDS);
		    	String currentDate = timestamp.get(3, TimeUnit.SECONDS);
		    	String mk1 = String.format("{%s: %b,%s: %b,%s: %b,%s: %s,%s: %b, %s: %s}",
						SpdxRdfConstants.PROP_CROSS_REF_IS_VALID, isValidUrl,
						SpdxRdfConstants.PROP_CROSS_REF_WAYBACK_LINK, isWaybackUrl,
						SpdxRdfConstants.PROP_CROSS_REF_MATCH, "true",
						SpdxRdfConstants.PROP_CROSS_REF_URL, url,
						SpdxRdfConstants.PROP_CROSS_REF_IS_LIVE, isLiveUrl,
						SpdxRdfConstants.PROP_CROSS_REF_TIMESTAMP, currentDate);
		    	urlDetails[i] = mk1;
		    } catch (Exception e) {
		        // interrupts if there is any possible error
		    	isValid.cancel(true);
		    	isLive.cancel(true);
		    	isWayback.cancel(true);
		    	timestamp.cancel(true);
		    }
		    executorService.shutdown();
		    try {
				executorService.awaitTermination(3, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return urlDetails;
	}
	
	@Override
	public String[] call() throws Exception {
		// TODO Auto-generated method stub
		return buildUrlDetails(crossRefUrls);
	}
}
