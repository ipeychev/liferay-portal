/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.pr.reviewer.fixture.internal;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Alejandro Tardín
 */
public class FixtureEntryServiceTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_fixtureEntryService = new FixtureEntryService();
	}

	@Test
	public void testActivateFixtureEntry() {
		String name = RandomTestUtil.randomString();

		_fixtureEntryService.addFixtureEntry(name);
		_fixtureEntryService.deactivateFixtureEntry(name);
		_fixtureEntryService.activateFixtureEntry(name);

		Assert.assertFalse(
			"The entry should not be archived",
			_fixtureEntryService.isArchived(name));
	}

	@Test
	public void testAddFixtureEntries() {
		String prefix = RandomTestUtil.randomString();

		_fixtureEntryService.addFixtureEntries(3, prefix);

		Map<String, Integer> counts = _fixtureEntryService.getCounts();

		Assert.assertEquals(counts.toString(), 3, counts.size());
	}

	@Test
	public void testAddFixtureEntry() {
		String name = RandomTestUtil.randomString();

		FixtureEntry fixtureEntry = _fixtureEntryService.addFixtureEntry(name);

		Assert.assertNotNull(fixtureEntry);
		Assert.assertEquals(name, fixtureEntry.getName());

		Map<String, Integer> counts = _fixtureEntryService.getCounts();

		Integer count = counts.get(name);

		Assert.assertEquals(0, count.intValue());
	}

	@Test
	public void testAddFixtureEntryIllegalArgumentException() {
		try {
			_fixtureEntryService.addFixtureEntry(StringPool.BLANK);

			Assert.fail();
		}
		catch (IllegalArgumentException illegalArgumentException) {
		}
	}

	@Test
	public void testArchived() {
		String name = RandomTestUtil.randomString();

		_fixtureEntryService.addFixtureEntry(name);

		Assert.assertFalse(_fixtureEntryService.isArchived(name));

		_fixtureEntryService.deactivateFixtureEntry(name);

		Assert.assertTrue(_fixtureEntryService.isArchived(name));
	}

	@Test
	public void testContainsFixtureEntry() {
		String name = RandomTestUtil.randomString();

		Assert.assertFalse(_fixtureEntryService.containsFixtureEntry(name));

		_fixtureEntryService.addFixtureEntry(name);

		Assert.assertTrue(_fixtureEntryService.containsFixtureEntry(name));
	}

	@Test
	public void testDeactivateFixtureEntry() {
		_fixtureEntryService.addFixtureEntry("alpha");
		_fixtureEntryService.deactivateFixtureEntry("alpha");

		FixtureEntry fixtureEntry = _fixtureEntryService.getFixtureEntry(
			"alpha");

		Assert.assertEquals("archived", fixtureEntry.getStatus());
	}

	@Test
	public void testDeleteFixtureEntries() {
		String name1 = RandomTestUtil.randomString();
		String name2 = RandomTestUtil.randomString();
		String name3 = RandomTestUtil.randomString();

		_fixtureEntryService.addFixtureEntry(name1);
		_fixtureEntryService.addFixtureEntry(name2);
		_fixtureEntryService.addFixtureEntry(name3);

		_fixtureEntryService.deleteFixtureEntries(new String[] {name1, name2});

		Assert.assertFalse(_fixtureEntryService.containsFixtureEntry(name1));
		Assert.assertFalse(_fixtureEntryService.containsFixtureEntry(name2));
		Assert.assertTrue(_fixtureEntryService.containsFixtureEntry(name3));
	}

	@Test
	public void testDeleteFixtureEntry() {
		String name = RandomTestUtil.randomString();

		_fixtureEntryService.addFixtureEntry(name);
		_fixtureEntryService.deleteFixtureEntry(name);

		Assert.assertFalse(_fixtureEntryService.containsFixtureEntry(name));
	}

	@Test
	public void testDeleteStaleFixtureEntries() {
		String name = RandomTestUtil.randomString();

		FixtureEntry fixtureEntry = _fixtureEntryService.addFixtureEntry(name);

		fixtureEntry.setModifiedTime(0);

		Assert.assertEquals(
			1, _fixtureEntryService.deleteStaleFixtureEntries());
		Assert.assertFalse(_fixtureEntryService.containsFixtureEntry(name));
	}

	@Test
	public void testGetCounts() {
		_addFixtureEntry("gamma", 3);
		_addFixtureEntry("alpha", 1);
		_addFixtureEntry("beta", 2);

		Map<String, Integer> counts = _fixtureEntryService.getCounts();

		Assert.assertEquals(Integer.valueOf(3), counts.get("gamma"));
		Assert.assertEquals(Integer.valueOf(1), counts.get("alpha"));
		Assert.assertEquals(Integer.valueOf(2), counts.get("beta"));
	}

	@Test
	public void testGetFixtureEntriesByStatus() {
		String name1 = RandomTestUtil.randomString();
		String name2 = RandomTestUtil.randomString();

		_fixtureEntryService.addFixtureEntry(name1);
		_fixtureEntryService.addFixtureEntry(name2);

		_fixtureEntryService.deactivateFixtureEntry(name2);

		List<FixtureEntry> fixtureEntries =
			_fixtureEntryService.getFixtureEntriesByStatus("archived");

		Assert.assertEquals(
			fixtureEntries.toString(), 1, fixtureEntries.size());

		FixtureEntry fixtureEntry = fixtureEntries.get(0);

		Assert.assertEquals(name2, fixtureEntry.getName());
	}

	@Test
	public void testGetFixtureEntriesCount() {

		// Archived

		String name = RandomTestUtil.randomString();

		_fixtureEntryService.addFixtureEntry(name);
		_fixtureEntryService.deactivateFixtureEntry(name);

		Assert.assertEquals(
			1, _fixtureEntryService.getFixtureEntriesCount("archived"));

		// Active

		name = RandomTestUtil.randomString();

		_fixtureEntryService.addFixtureEntry(name);

		Assert.assertEquals(
			1, _fixtureEntryService.getFixtureEntriesCount("active"));
	}

	@Test
	public void testGetFixtureEntry() {
		String name = RandomTestUtil.randomString();

		_fixtureEntryService.addFixtureEntry(name);

		FixtureEntry fixtureEntry = _fixtureEntryService.getFixtureEntry(name);

		Assert.assertEquals(name, fixtureEntry.getName());
	}

	@Test
	public void testGetFixtureEntryInventory() {
		_fixtureEntryService.addFixtureEntry("alpha");
		_fixtureEntryService.addFixtureEntry("beta");

		List<FixtureEntry> fixtureEntries =
			_fixtureEntryService.getFixtureEntryInventory();

		FixtureEntry alphaFixtureEntry = fixtureEntries.get(0);

		FixtureEntry betaFixtureEntry = fixtureEntries.get(1);

		Assert.assertEquals("alpha", alphaFixtureEntry.getName());
		Assert.assertEquals("beta", betaFixtureEntry.getName());
	}

	@Test
	public void testGetFixtureEntryNoSuchElementException() {
		try {
			_fixtureEntryService.getFixtureEntry(RandomTestUtil.randomString());

			Assert.fail();
		}
		catch (NoSuchElementException noSuchElementException) {
		}
	}

	@Test
	public void testIncrementFixtureEntryCount() {
		String name = RandomTestUtil.randomString();

		_fixtureEntryService.addFixtureEntry(name);

		Assert.assertEquals(
			3, _fixtureEntryService.incrementFixtureEntryCount(name, 3));
		Assert.assertEquals(
			5, _fixtureEntryService.incrementFixtureEntryCount(name, 2));
	}

	@Test
	public void testIncrementFixtureEntryCountIllegalStateDenied() {
		String name = RandomTestUtil.randomString();

		_fixtureEntryService.addFixtureEntry(name);
		_fixtureEntryService.deactivateFixtureEntry(name);

		try {
			_fixtureEntryService.incrementFixtureEntryCount(
				name, RandomTestUtil.randomInt());

			Assert.fail();
		}
		catch (IllegalStateException illegalStateException) {
		}
	}

	private void _addFixtureEntry(String name, int count) {
		_fixtureEntryService.addFixtureEntry(name);
		_fixtureEntryService.incrementFixtureEntryCount(name, count);
	}

	private FixtureEntryService _fixtureEntryService;

}