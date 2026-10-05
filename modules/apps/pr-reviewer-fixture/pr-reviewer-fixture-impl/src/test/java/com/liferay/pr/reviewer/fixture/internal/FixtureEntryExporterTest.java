/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.pr.reviewer.fixture.internal;

import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.File;

import java.nio.file.Files;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Alejandro Tardín
 */
public class FixtureEntryExporterTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		_fixtureEntryExporter = new FixtureEntryExporter();
	}

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void testExport() throws Exception {
		File file = temporaryFolder.newFile(
			RandomTestUtil.randomString() + ".csv");

		_fixtureEntryExporter.export(
			file,
			Arrays.asList(
				_createFixtureEntry("alpha", 1),
				_createFixtureEntry("beta", 2)));

		Assert.assertEquals(
			Arrays.asList("alpha,1,active", "beta,2,active"),
			Files.readAllLines(file.toPath()));
	}

	@Ignore
	@Test
	public void testExportWithJSONFile() throws Exception {
		File file = temporaryFolder.newFile(
			RandomTestUtil.randomString() + ".json");

		_fixtureEntryExporter.export(
			file, Collections.singletonList(_createFixtureEntry("alpha", 1)));

		List<String> lines = Files.readAllLines(file.toPath());

		Assert.assertEquals(lines.toString(), 1, lines.size());

		String line = lines.get(0);

		Assert.assertTrue(line.contains("alpha"));
	}

	@Ignore
	@Test
	public void testExportWithJSONFileWhenFixtureEntriesAreEmpty()
		throws Exception {

		File file = temporaryFolder.newFile(
			RandomTestUtil.randomString() + ".json");

		_fixtureEntryExporter.export(file, Collections.emptyList());

		Assert.assertEquals(
			Collections.singletonList("[]"), Files.readAllLines(file.toPath()));
	}

	@Test
	public void testExportWithUnsupportedExtension() throws Exception {
		File file = temporaryFolder.newFile(
			RandomTestUtil.randomString() + ".txt");

		try {
			_fixtureEntryExporter.export(file, Collections.emptyList());

			Assert.fail();
		}
		catch (IllegalArgumentException illegalArgumentException) {
		}
	}

	private FixtureEntry _createFixtureEntry(String name, int count) {
		FixtureEntry fixtureEntry = new FixtureEntry(name, "active");

		fixtureEntry.setCount(count);

		return fixtureEntry;
	}

	private static FixtureEntryExporter _fixtureEntryExporter;

}