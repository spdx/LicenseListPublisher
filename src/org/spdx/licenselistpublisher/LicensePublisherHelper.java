/**
 * 
 */
package org.spdx.licenselistpublisher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.io.Files;

/**
 * @author Gary O'Neall
 * Static utility class for the LicecenseListPublisher
 *
 */
public class LicensePublisherHelper {
	
	private LicensePublisherHelper() {
		// Static helper, should not be instantiated
	}
	
	/**
	 * Copy a file from the resources directory to a destination file
	 * @param resourceFileName filename of the file in the resources directory
	 * @param destination target file - warning, this will be overwritten
	 * @throws IOException 
	 */
	public static void copyResourceFile(String resourceFileName, File destination) throws IOException {
		File resourceFile = new File(resourceFileName);
		if (resourceFile.exists()) {
			Files.copy(resourceFile, destination);
		} else {
			InputStream is = LicenseRDFAGenerator.class.getClassLoader().getResourceAsStream(resourceFileName);
			InputStreamReader reader = new InputStreamReader(is);
			FileWriter writer = new FileWriter(destination);
			try {
				char[] buf = new char[2048];
				int len = reader.read(buf);
				while (len > 0) {
					writer.write(buf, 0, len);
					len = reader.read(buf);
				}
			} finally {
				if (writer != null) {
					writer.close();
				}
				reader.close();
			}
		}
	}
}
