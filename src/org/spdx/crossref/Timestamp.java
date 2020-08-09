package org.spdx.crossref;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.Callable;

public class Timestamp implements Callable<String> {
	String url;

    public Timestamp(String url) {
        this.url = url;
    }
    
    public static String getTimestamp(){
		// Get current timestamp
    	DateTimeFormatter isoDateTime = DateTimeFormatter.ISO_DATE_TIME;
		DateTimeFormatter formatter = isoDateTime.ofLocalizedDateTime( FormatStyle.SHORT ).ofPattern("YYYY-MM-dd , HH:mm:ss");
		String timeStamp = ZonedDateTime.now( ZoneOffset.UTC ).format( formatter );
		return timeStamp.toString();
	}

	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub
		return getTimestamp();
	}

}
