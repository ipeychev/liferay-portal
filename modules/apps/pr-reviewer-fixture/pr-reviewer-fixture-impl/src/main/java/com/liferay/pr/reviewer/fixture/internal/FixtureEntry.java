/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.pr.reviewer.fixture.internal;

import com.liferay.portal.kernel.util.Validator;

/**
 * @author Alejandro Tardín
 */
public class FixtureEntry implements Comparable<FixtureEntry> {

	public FixtureEntry(FixtureEntry fixtureEntry) {
		_modifiedTime = fixtureEntry._modifiedTime;
		_name = fixtureEntry._name;
		_status = fixtureEntry._status;

		if (fixtureEntry._count < 0) {
			_count = 0;
		}
		else {
			_count = fixtureEntry._count;
		}
	}

	public FixtureEntry(String name, String status) {
		if (Validator.isNull(name)) {
			throw new IllegalArgumentException("The name is required.");
		}

		_name = name;
		_status = status;

		_modifiedTime = System.currentTimeMillis();
	}

	public int compareTo(FixtureEntry fixtureEntry) {
		return _name.compareTo(fixtureEntry._name);
	}

	public int getCount() {
		return _count;
	}

	public long getModifiedTime() {
		return _modifiedTime;
	}

	public String getName() {
		return _name;
	}

	public String getStatus() {
		return _status;
	}

	public void setCount(int count) {
		_count = count;
	}

	public void setModifiedTime(long modifiedTime) {
		_modifiedTime = modifiedTime;
	}

	public void setStatus(String status) {
		_status = status;
	}

	private int _count;
	private long _modifiedTime;
	private final String _name;
	private String _status;

}