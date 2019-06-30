/**
 * 
 */
package org.spdx.licenselistpublisher.licensegenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.spdx.rdfparser.license.SpdxListedLicense;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

/**
 * @author Gary O'Neall
 * 
 * Source file for all listed licenses
 *
 */
public class ListedLicensesJavaSource {
	
	static final String TEMPLATE_CLASS_PATH = "resources" + "/" + "javaTemplate";
	static final String TEMPLATE_ROOT_PATH = "resources" + File.separator + "javaTemplate";
	private static final String JAVA_SOURCE_TEMPLATE = "SpdxListedLicenses.java";
	private static final Pattern idPattern = Pattern.compile("^\\d.+");

	private String licenseListVersion;	
	List<String> licenseEnums = new ArrayList<>();
	Map<String, String> idToEnum = new HashMap<>();

	public ListedLicensesJavaSource(String licenseListVersion) {
		this.licenseListVersion = licenseListVersion;
	}
	
	protected static String idToEnumIdentifier(String id) {
		String retval = id.replaceAll("(\\.|-)", "_");
		retval = retval.replaceAll("\\+", "PLUS");
		if (idPattern.matcher(retval).matches()) {
			retval = "_" + retval;
		}
		return retval;
	}
	
	protected static String escapeJavaString(String str) {
		if (str == null) {
			return "null";
		} else {
			return "\""+StringEscapeUtils.escapeJava(str)+"\"";
		}
	}
	
	protected static String toEscapedStringArray(String[] strings) {
		if (strings == null) {
			return "null";
		}
		if (strings.length == 0) {
			return "new String[] {}";
		}
		StringBuilder sb = new StringBuilder("new String[] {");
		sb.append(escapeJavaString(strings[0]));
		for (int i = 1; i < strings.length; i++) {
			sb.append(", \n\t\t\t\t");
			sb.append(escapeJavaString(strings[i]));
		}
		sb.append("}");
		return sb.toString();
	}
	
	protected static String toBooleanObject(Boolean b) {
		if (b == null) {
			return null;
		}
		if (b) {
			return "true";
		} else {
			return "false";
		}
	}
	
	protected static String toBoolean(boolean b) {
		if (b) {
			return "true";
		} else {
			return "false";
		}
	}

	public void addLicense(SpdxListedLicense license) {
		if (licenseEnums.size() > 0) {
			licenseEnums.set(licenseEnums.size()-1,licenseEnums.get(licenseEnums.size()-1) + ", // comment\n");
		}
		StringBuilder sb = new StringBuilder("\t");
		String enumId = idToEnumIdentifier(license.getLicenseId());
		this.idToEnum.put(license.getLicenseId(), enumId);
		sb.append(enumId);
		sb.append("(");
		sb.append(escapeJavaString(license.getLicenseId()));
		sb.append(", // licenseId\n\t\t");
		sb.append(escapeJavaString(license.getName()));
		sb.append(", // name\n\t\t");
		sb.append(escapeJavaString(license.getLicenseText()));
		sb.append(", // licenseText\n\t\t");
		sb.append(escapeJavaString(license.getStandardLicenseTemplate()));
		sb.append(", // standardLicenseTemplate\n\t\t");
		sb.append(escapeJavaString(license.getStandardLicenseHeader()));
		sb.append(", // standardLicenseHeader\n\t\t");
		sb.append(escapeJavaString(license.getStandardLicenseHeaderTemplate()));
		sb.append(", // standardLicenseHeaderTemplate\n\t\t");
		sb.append(toEscapedStringArray(license.getSeeAlso()));
		sb.append(", // seeAlso\n\t\t");
		sb.append(toBooleanObject(license.getFsfLibre()));
		sb.append(", // fsfLibre\n\t\t");
		sb.append(toBoolean(license.isOsiApproved()));
		sb.append(", // osiApproved\n\t\t");
		sb.append(toBoolean(license.isDeprecated()));
		sb.append(", // deprecated\n\t\t");
		sb.append(escapeJavaString(license.getDeprecatedVersion()));
		sb.append(", // deprecatedVersion\n\t\t");
		sb.append(escapeJavaString(license.getComment()));
		sb.append(")");
		licenseEnums.add(sb.toString());
	}

	public void writeFile(File file) throws IOException {
		FileOutputStream stream = null;
		OutputStreamWriter writer = null;
		if (!file.exists()) {
			if (!file.createNewFile()) {
				throw(new IOException("Can not create new file "+file.getName()));
			}
		}
		String templateDirName = TEMPLATE_ROOT_PATH;
		File templateDirectoryRoot = new File(templateDirName);
		if (!(templateDirectoryRoot.exists() && templateDirectoryRoot.isDirectory())) {
			templateDirName = TEMPLATE_CLASS_PATH;
		}
		try {
			stream = new FileOutputStream(file);
			writer = new OutputStreamWriter(stream, "UTF-8");
			DefaultMustacheFactory builder = new DefaultMustacheFactory(templateDirName);
	        Map<String, Object> mustacheMap = buildMustachMap();
	        Mustache mustache = builder.compile(JAVA_SOURCE_TEMPLATE);
	        mustache.execute(writer, mustacheMap);
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (stream != null) {
				stream.close();
			}
		}
	}

	private Map<String, Object> buildMustachMap() {
		Map<String, Object> retval = new HashMap<>();
		retval.put("version", licenseListVersion);
		licenseEnums.set(licenseEnums.size()-1,licenseEnums.get(licenseEnums.size()-1) + "; // comment\n");
		retval.put("licenseEnums", this.licenseEnums);
		Iterator<Entry<String, String>> idIterator = this.idToEnum.entrySet().iterator();
		List<String> licensePuts = new ArrayList<>();
		while (idIterator.hasNext()) {
			Entry<String, String> entry = idIterator.next();
			StringBuilder sb = new StringBuilder("iDLicenseMap.put(\"");
			sb.append(entry.getKey());
			sb.append("\", ");
			sb.append(entry.getValue());
			if (idIterator.hasNext()) {
				sb.append(");\n\t\t");
			} else {
				sb.append(");");
			}
			licensePuts.add(sb.toString());
		}
		retval.put("licensePuts", licensePuts);
		return retval;
	}

}
