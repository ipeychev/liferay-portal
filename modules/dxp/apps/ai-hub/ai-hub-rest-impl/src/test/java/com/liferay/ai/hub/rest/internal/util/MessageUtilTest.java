/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.rest.internal.util;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Iliyan Peychev
 */
public class MessageUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testAppendContext() {
		Assert.assertEquals(
			"Generate the content\n\n# Context\n" +
				"generationExternalReferenceCode: abc-123\ngenerationId: 42\n",
			MessageUtil.appendContext(
				"Generate the content",
				Map.of(
					"generationExternalReferenceCode", "abc-123",
					"generationId", 42)));
	}

	@Test
	public void testAppendContextWhenContextIsEmpty() {
		Assert.assertEquals(
			"Generate the content",
			MessageUtil.appendContext("Generate the content", Map.of()));
	}

	@Test
	public void testAppendContextWhenContextIsNull() {
		Assert.assertEquals(
			"Generate the content",
			MessageUtil.appendContext("Generate the content", null));
	}

	@Test
	public void testAppendContextWhenContextValueIsNull() {
		Assert.assertEquals(
			"Generate the content\n\n# Context\n",
			MessageUtil.appendContext(
				"Generate the content",
				Collections.singletonMap("nullValue", null)));
	}

	@Test
	public void testAppendContextWhenTextIsNull() {
		Assert.assertEquals(
			"\n\n# Context\ngenerationId: 42\n",
			MessageUtil.appendContext(null, Map.of("generationId", 42)));
	}

}