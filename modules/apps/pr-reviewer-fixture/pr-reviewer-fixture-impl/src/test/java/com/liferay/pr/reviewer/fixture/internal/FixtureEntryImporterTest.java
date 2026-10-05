/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.pr.reviewer.fixture.internal;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.File;

import java.nio.file.Files;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Alejandro Tardín
 */
public class FixtureEntryImporterTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() {
		_fixtureEntryImporter = new FixtureEntryImporter();
		_fixtureEntryService = new FixtureEntryService();

		ReflectionTestUtil.setFieldValue(
			_fixtureEntryImporter, "_fixtureEntryService",
			_fixtureEntryService);
	}

	@Test
	public void testImportFixtureEntries() throws Exception {
		File file = temporaryFolder.newFile();
		File rejectedFile = temporaryFolder.newFile();

		Files.write(
			file.toPath(),
			Arrays.asList("alpha=3", "beta = 5", "", "# comment"));

		Assert.assertEquals(
			2, _fixtureEntryImporter.importFixtureEntries(file, rejectedFile));

		Map<String, Integer> counts = _fixtureEntryService.getCounts();

		Assert.assertEquals(Integer.valueOf(3), counts.get("alpha"));
		Assert.assertEquals(Integer.valueOf(5), counts.get("beta"));
	}

	@Test
	public void testImportFixtureEntriesWithBlankName() throws Exception {
		_testImportFixtureEntriesWithRejectedLine("=3");
	}

	@Test
	public void testImportFixtureEntriesWithMissingCount() throws Exception {
		_testImportFixtureEntriesWithRejectedLine("alpha=");
	}

	@Test
	public void testImportFixtureEntriesWithMissingEquals() throws Exception {
		_testImportFixtureEntriesWithRejectedLine("alpha 3");
	}

	@Test
	public void testImportFixtureEntriesWithUppercaseName() throws Exception {
		_testImportFixtureEntriesWithRejectedLine("Alpha=3");
	}

	private void _testImportFixtureEntriesWithRejectedLine(String line)
		throws Exception {

		File rejectedFile = temporaryFolder.newFile();
		File file = temporaryFolder.newFile();

		Files.write(file.toPath(), Collections.singletonList(line));

		Assert.assertEquals(
			0, _fixtureEntryImporter.importFixtureEntries(file, rejectedFile));

		List<String> rejectedLines = Files.readAllLines(rejectedFile.toPath());

		Assert.assertEquals(
			rejectedLines.toString(), 1, rejectedLines.size());
		Assert.assertEquals(line, rejectedLines.get(0));
	}

	private FixtureEntryImporter _fixtureEntryImporter;
	private FixtureEntryService _fixtureEntryService;

}