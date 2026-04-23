/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.cell.web.internal.display.context;

import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Mylena Monte
 */
public class ViewContentSitesDisplayContext {

	public ViewContentSitesDisplayContext(
		HttpServletRequest httpServletRequest) {

		_httpServletRequest = httpServletRequest;
	}

	public String getAPIURL() {
		return "/o/ai-hub-cell/content-site-generator";
	}

	public List<DropdownItem> getBulkActionDropdownItems() throws Exception {
		return List.of(
			new FDSActionDropdownItem(
				getAPIURL() + "/{id}", "trash", "delete",
				LanguageUtil.get(_httpServletRequest, "delete"), "delete",
				"delete", "async"));
	}

	
	public CreationMenu getCreationMenu() throws Exception {
		return CreationMenuBuilder.addDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref("#");
				dropdownItem.setIcon("stars");
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "new-generator"));
			}
		).build();
	}

	public Map<String, Object> getEmptyState() {
		return HashMapBuilder.<String, Object>put(
			"description",
			LanguageUtil.get(
				_httpServletRequest,
				"create-a-new-generator-to-get-started")
		).put(
			"title",
			LanguageUtil.get(_httpServletRequest, "no-content-sites-yet")
		).build();
	}

	public List<FDSActionDropdownItem> getFDSActionDropdownItems()
		throws Exception {

		return List.of(
			new FDSActionDropdownItem(
				getAPIURL() + "/{id}", "view", "view",
				LanguageUtil.get(_httpServletRequest, "view"), "get", null,
				null),
			new FDSActionDropdownItem(
				getAPIURL() + "/{id}", "trash", "delete",
				LanguageUtil.get(_httpServletRequest, "delete"), "delete",
				"delete", "async"));
	}

	private final HttpServletRequest _httpServletRequest;

}