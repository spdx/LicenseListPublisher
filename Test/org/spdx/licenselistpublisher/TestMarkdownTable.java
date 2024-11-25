package org.spdx.licenselistpublisher;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.core.DefaultModelStore;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.SpdxModelFactory;
import org.spdx.library.model.v2.license.LicenseException;
import org.spdx.library.model.v2.license.ListedLicenseException;
import org.spdx.library.model.v2.license.SpdxListedLicense;
import org.spdx.storage.simple.InMemSpdxStore;

public class TestMarkdownTable {

	private static final String DOC_URI = "https://mydoc.uri";

	@Before
	public void setUp() throws Exception {
		SpdxModelFactory.init();
		DefaultModelStore.initialize(new InMemSpdxStore(), DOC_URI, new ModelCopyManager());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		String version = "v1.1";
		MarkdownTable md = new MarkdownTable(version);
		String name1 = "name1";
		String id1 = "id1";
		boolean osiApproved1 = true;
		SpdxListedLicense lic1 = new SpdxListedLicense(name1, id1, "text", Arrays.asList(new String[0]),
				"", "", "", osiApproved1, false, "", false, "");
		md.addLicense(lic1, false);
		String name2 = "name2";
		String id2 = "id2";
		boolean osiApproved2 = false;
		SpdxListedLicense lic2 = new SpdxListedLicense(name2, id2, "text", Arrays.asList(new String[0]),
				"", "", "", osiApproved2, false, "", false, "");
		md.addLicense(lic2, false);
		String name3 = "name3";
		String id3 = "id3";
		boolean osiApproved3 = false;
		SpdxListedLicense depLicense = new SpdxListedLicense(name3, id3, "text", Arrays.asList(new String[0]),
				"", "", "", osiApproved3, false, "", false, "");
		md.addLicense(depLicense, false);
		String name4 = "name4";
		String id4 = "id4";
		String text4 = "text4";
		LicenseException ex1 = new ListedLicenseException(id4, name4, text4);
		md.addException(ex1, false);
		String name5 = "name5";
		String id5 = "id5";
		String text5 = "text5";
		LicenseException ex2 = new ListedLicenseException(id5, name5, text5);
		md.addException(ex2, false);
		StringWriter result = new StringWriter();
		md.writeTOC(result);
		String sresult = result.toString();
		assertTrue(sresult.contains(name1));
		assertTrue(sresult.contains(name2));
		//TODO: add remaining checks after implementation
	}

}
