/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.pr.reviewer.fixture.internal.model;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.pr.reviewer.fixture.internal.FixtureEntry;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Alejandro Tardín
 */
public class FixtureEntryTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testCompareTo() {
		FixtureEntry alphaFixtureEntry = new FixtureEntry(
			"alpha", RandomTestUtil.randomString());
		FixtureEntry betaFixtureEntry = new FixtureEntry(
			"beta", RandomTestUtil.randomString());

		Assert.assertTrue(alphaFixtureEntry.compareTo(betaFixtureEntry) < 0);
		Assert.assertTrue(betaFixtureEntry.compareTo(alphaFixtureEntry) > 0);
	}

}