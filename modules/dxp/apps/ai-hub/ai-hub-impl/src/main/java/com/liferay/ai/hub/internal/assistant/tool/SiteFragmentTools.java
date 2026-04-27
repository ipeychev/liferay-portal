/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.assistant.tool;

import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalServiceUtil;
import com.liferay.oauth2.provider.service.OAuth2AuthorizationLocalServiceUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.Validator;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.io.Serializable;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mahmoud Tayem
 */
public class SiteFragmentTools {

	public SiteFragmentTools(
		String accessToken, long companyId, String userToken,
		Map<String, Serializable> workflowContext) {

		_accessToken = accessToken;
		_companyId = companyId;
		_userToken = userToken;
		_workflowContext = workflowContext;
	}

	@Tool("List all custom fragments available on a site with their editable fields")
	public String getFragments(
		@P("Site external reference code") String siteExternalReferenceCode) {

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(_companyId)) {

			return _getFragments(siteExternalReferenceCode);
		}
		catch (Exception exception) {
			return ReflectionUtil.throwException(exception);
		}
	}

	private String _doGet(String location) throws Exception {
		Http.Options options = new Http.Options();

		options.addHeader(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
		options.addHeader("Liferay-AI-Hub-Cell-On-Behalf-Of", _userToken);
		options.setLocation(location);
		options.setMethod(Http.Method.GET);

		String responseBody = HttpUtil.URLtoString(options);

		int responseCode = options.getResponse().getResponseCode();

		if ((responseCode < 200) || (responseCode >= 300)) {
			throw new Exception(
				StringBundler.concat(
					"HTTP ", responseCode, " from ", location, ": ",
					responseBody));
		}

		return responseBody;
	}

	private JSONArray _extractEditables(String html) throws Exception {
		JSONArray editables = JSONFactoryUtil.createJSONArray();

		if (Validator.isNull(html)) {
			return editables;
		}

		Matcher matcher = _editablePattern.matcher(html);

		while (matcher.find()) {
			JSONObject editable = JSONFactoryUtil.createJSONObject();

			editable.put("id", matcher.group(1));
			editable.put("type", matcher.group(2));

			editables.put(editable);
		}

		return editables;
	}

	private JSONArray _buildMinifiedCatalog(JSONArray fullCatalog)
		throws Exception {

		JSONArray minifiedCatalog = JSONFactoryUtil.createJSONArray();

		for (int i = 0; i < fullCatalog.length(); i++) {
			JSONObject full = fullCatalog.getJSONObject(i);

			JSONObject minified = JSONFactoryUtil.createJSONObject();

			minified.put(
				"editables", full.getJSONArray("editables"));
			minified.put(
				"externalReferenceCode",
				full.getString("externalReferenceCode"));
			minified.put("html", _minifyHtml(full.getString("html")));
			minified.put("key", full.getString("key"));
			minified.put("name", full.getString("name"));

			minifiedCatalog.put(minified);
		}

		return minifiedCatalog;
	}

	private String _getBaseURL() throws Exception {
		if (Validator.isNull(_accessToken) ||
			!_accessToken.startsWith("Bearer ")) {

			throw new IllegalArgumentException("Invalid access token");
		}

		OAuth2Authorization oAuth2Authorization =
			OAuth2AuthorizationLocalServiceUtil.
				getOAuth2AuthorizationByAccessTokenContent(
					_accessToken.substring(7));

		OAuth2Application oAuth2Application =
			OAuth2ApplicationLocalServiceUtil.getOAuth2Application(
				oAuth2Authorization.getOAuth2ApplicationId());

		return oAuth2Application.getHomePageURL();
	}

	private String _getFragments(String siteExternalReferenceCode)
		throws Exception {

		String cachedFull = (String)_workflowContext.get(
			"fullFragmentsCatalog");

		if (Validator.isNotNull(cachedFull)) {
			return _buildMinifiedCatalog(
				JSONFactoryUtil.createJSONArray(cachedFull)).toString();
		}

		String baseURL = _getBaseURL();

		String encodedSiteERC = URLCodec.encodeURL(siteExternalReferenceCode);

		JSONArray fragmentSets = _getPagedItems(
			StringBundler.concat(
				baseURL, "/o/headless-admin-fragment/v1.0/sites/",
				encodedSiteERC, "/fragment-sets"));

		JSONArray fullCatalog = JSONFactoryUtil.createJSONArray();
		JSONArray minifiedCatalog = JSONFactoryUtil.createJSONArray();

		for (int i = 0; i < fragmentSets.length(); i++) {
			JSONObject fragmentSet = fragmentSets.getJSONObject(i);

			String setERC = fragmentSet.getString("externalReferenceCode");

			JSONArray fragments = _getPagedItems(
				StringBundler.concat(
					baseURL, "/o/headless-admin-fragment/v1.0/sites/",
					encodedSiteERC, "/fragment-sets/",
					URLCodec.encodeURL(setERC),
					"/fragments?type=Component"));

			for (int j = 0; j < fragments.length(); j++) {
				JSONObject fragment = fragments.getJSONObject(j);

				String erc = fragment.getString("externalReferenceCode");
				String key = fragment.getString("key");
				String name = fragment.getString("name");
				String fragmentSetName = fragmentSet.getString("name");

				String html = null;
				String css = null;
				String js = null;
				String configuration = null;

				JSONArray versions = fragment.getJSONArray("fragmentVersions");

				JSONObject selectedVersion = _selectVersion(versions);

				if (selectedVersion != null) {
					html = selectedVersion.getString("html");
					css = selectedVersion.getString("css");
					js = selectedVersion.getString("js");
					configuration = selectedVersion.getString("configuration");
				}

				JSONArray editables = _extractEditables(html);

				JSONObject fullEntry = JSONFactoryUtil.createJSONObject();

				fullEntry.put("configuration", configuration);
				fullEntry.put("css", css);
				fullEntry.put("editables", editables);
				fullEntry.put("externalReferenceCode", erc);
				fullEntry.put("fragmentSetName", fragmentSetName);
				fullEntry.put("html", html);
				fullEntry.put("js", js);
				fullEntry.put("key", key);
				fullEntry.put("name", name);

				fullCatalog.put(fullEntry);

				JSONObject minifiedEntry = JSONFactoryUtil.createJSONObject();

				minifiedEntry.put("editables", editables);
				minifiedEntry.put("externalReferenceCode", erc);
				minifiedEntry.put("html", _minifyHtml(html));
				minifiedEntry.put("key", key);
				minifiedEntry.put("name", name);

				minifiedCatalog.put(minifiedEntry);
			}
		}

		_workflowContext.put(
			"fullFragmentsCatalog", fullCatalog.toString());

		return minifiedCatalog.toString();
	}

	private JSONArray _getPagedItems(String baseLocation) throws Exception {
		JSONArray allItems = JSONFactoryUtil.createJSONArray();

		int page = 1;

		while (true) {
			String location = HttpComponentsUtil.addParameter(
				baseLocation, "page", String.valueOf(page));

			location = HttpComponentsUtil.addParameter(
				location, "pageSize", "100");

			String responseBody = _doGet(location);

			JSONObject responseJSON = JSONFactoryUtil.createJSONObject(
				responseBody);

			JSONArray items = responseJSON.getJSONArray("items");

			if ((items == null) || (items.length() == 0)) {
				break;
			}

			for (int i = 0; i < items.length(); i++) {
				allItems.put(items.getJSONObject(i));
			}

			int lastPage = responseJSON.getInt("lastPage", page);

			if (page >= lastPage) {
				break;
			}

			page++;
		}

		return allItems;
	}

	private String _minifyHtml(String html) {
		if (Validator.isNull(html)) {
			return "";
		}

		return html.replaceAll("\\s+", " ").trim();
	}

	private JSONObject _selectVersion(JSONArray versions) {
		if ((versions == null) || (versions.length() == 0)) {
			return null;
		}

		for (int i = 0; i < versions.length(); i++) {
			JSONObject version = versions.getJSONObject(i);

			if ("Approved".equals(version.getString("status"))) {
				return version;
			}
		}

		return versions.getJSONObject(0);
	}

	private static final Pattern _editablePattern = Pattern.compile(
		"data-lfr-editable-id=\"([^\"]+)\"\\s+data-lfr-editable-type=\"([^\"]+)\"");

	private static final Log _log = LogFactoryUtil.getLog(
		SiteFragmentTools.class);

	private final String _accessToken;
	private final long _companyId;
	private final String _userToken;
	private final Map<String, Serializable> _workflowContext;

}
