/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.pr.reviewer.fixture.internal;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.File;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(service = FixtureEntryExporter.class)
public class FixtureEntryExporter {

	/**
	 * Writes the entries to the file in the format its extension names. The
	 * {@code csv} and {@code json} extensions are supported.
	 */
	public void export(File file, List<FixtureEntry> fixtureEntries) {
		Path path = file.toPath();

		String extension = StringUtil.extractLast(
			file.getName(), CharPool.PERIOD);

		String content = null;

		switch (extension) {
			case "csv":
				content = toCSV(fixtureEntries);

				break;
			case "json":
				content = _toJSON(fixtureEntries);

				break;
			default:
				throw new IllegalArgumentException(
					"The file extension must be csv or json");
		}

		_persistExport(path, content);
	}

	public void export(List<FixtureEntry> fixtureEntries) {
		export(new File("data/exports/", "entries.csv"), fixtureEntries);
	}

	public String toSummaryJSON(FixtureEntry fixtureEntry) {
		JSONObject jsonObject = JSONUtil.put(
			"name", fixtureEntry.getName()
		).put(
			"status", fixtureEntry.getStatus()
		);

		return jsonObject.toString();
	}

	String toCSV(List<FixtureEntry> fixtureEntries) {
		List<String> rows = new ArrayList<>();

		for (FixtureEntry fixtureEntry : fixtureEntries) {
			rows.add(_toCsvRow(fixtureEntry));
		}

		return StringUtil.merge(rows, StringPool.NEW_LINE);
	}

	private void _persistExport(Path path, String content) {
		try {
			Files.write(path, content.getBytes(StandardCharsets.UTF_8));
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	private String _toCsvRow(FixtureEntry fixtureEntry) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(fixtureEntry.getName());
		stringBuilder.append(CharPool.COMMA);
		stringBuilder.append(fixtureEntry.getCount());
		stringBuilder.append(CharPool.COMMA);
		stringBuilder.append(fixtureEntry.getStatus());

		return stringBuilder.toString();
	}

	/**
	 * Returns the entries as a JSON array, such as
	 * {@code [{"count":3,"name":"alpha","status":"active"}]}.
	 */
	private String _toJSON(List<FixtureEntry> fixtureEntries) {
		JSONArray jsonArray = _jsonFactory.createJSONArray();

		for (FixtureEntry fixtureEntry : fixtureEntries) {
			JSONObject jsonObject = _jsonFactory.createJSONObject();

			jsonObject.put("count", fixtureEntry.getCount());
			jsonObject.put("name", fixtureEntry.getName());
			jsonObject.put("status", fixtureEntry.getStatus());

			jsonArray.put(jsonObject);
		}

		return jsonArray.toString();
	}

	@Reference
	private JSONFactory _jsonFactory;

}