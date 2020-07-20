/**
 *
 */
package org.spdx.htmltemplates;

/**
 * @author Gary O'Neall
 *
 * Exceptions related to HTML license templates
 *
 */
public class InvalidLicenseHtmlTemplateException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public InvalidLicenseHtmlTemplateException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public InvalidLicenseHtmlTemplateException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public InvalidLicenseHtmlTemplateException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public InvalidLicenseHtmlTemplateException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public InvalidLicenseHtmlTemplateException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
