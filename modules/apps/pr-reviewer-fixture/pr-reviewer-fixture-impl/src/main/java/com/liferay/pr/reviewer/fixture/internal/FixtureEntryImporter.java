/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.pr.reviewer.fixture.internal;

import com.liferay.petra.string.CharPool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.nio.file.Files;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(service = FixtureEntryImporter.class)
public class FixtureEntryImporter {

	/**
	 * Imports the entries from the file, one per line in the form
	 * {@code name=count}, where the name serves as the entry ID. Lines that
	 * are rejected are written to the rejected file.
	 */
	public int importFixtureEntries(File file, File rejectedFile)
		throws IOException {

		int entriesCount = 0;

		try (BufferedReader bufferedReader = Files.newBufferedReader(
				file.toPath());
			BufferedWriter bufferedWriter = Files.newBufferedWriter(
				rejectedFile.toPath());
			PrintWriter printWriter = new PrintWriter(bufferedWriter)) {

			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				if (!line.isEmpty() && !line.startsWith(_COMMENT_PREFIX)) {
					Pattern pattern = Pattern.compile(
						"\\s*[a-z]+\\s*=\\s*[0-9]+\\s*");

					Matcher matcher = pattern.matcher(line);

					if (!matcher.matches()) {
						printWriter.println(line);

						continue;
					}

					int equalsIndex = line.indexOf(CharPool.EQUAL);

					String name = line.substring(0, equalsIndex).trim();

					_fixtureEntryService.addFixtureEntry(name);

					String countString = line.substring(equalsIndex + 1);

					_fixtureEntryService.incrementFixtureEntryCount(
						name, Integer.parseInt(countString.trim()));

					entriesCount++;
				}
			}
		}

		return entriesCount;
	}

	private static final String _COMMENT_PREFIX = "#";

	@Reference
	private FixtureEntryService _fixtureEntryService;

}