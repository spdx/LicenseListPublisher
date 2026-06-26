/**
 * SPDX-FileCopyrightText: 2026-present Arthit Suriyawongkul
 * SPDX-FileType: SOURCE
 * SPDX-License-Identifier: Apache-2.0
 */
package org.spdx.storage.listedlicense;

import java.util.Comparator;

/**
 * Extends LicenseJson with the ability to sort the crossRef list by order,
 * producing stable JSON output across publisher runs.
 */
public class SortableLicenseJson extends LicenseJson {

	public SortableLicenseJson() {
		super();
	}

	public SortableLicenseJson(String id) {
		super(id);
	}

	public void sortCrossRef() {
		if (crossRef != null) {
			// If order is null, put it at the end of the list
			crossRef.sort(Comparator.comparingInt(c -> c.order != null ? c.order : Integer.MAX_VALUE));
		}
	}

}
