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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Feliphe Marinho
 */
public class SitePageTools {

	public SitePageTools(String accessToken, long companyId, String userToken) {
		_accessToken = accessToken;
		_companyId = companyId;
		_userToken = userToken;
	}

	@Tool("Retrieve the draft content page specification")
	public String getPageSpecification(
		@P("Site external reference code") String siteExternalReferenceCode,
		@P("Site page external reference code") String
			sitePageExternalReferenceCode) {

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(_companyId)) {

			return _getPageSpecification(
				siteExternalReferenceCode, sitePageExternalReferenceCode);
		}
		catch (Exception exception) {
			return ReflectionUtil.throwException(exception);
		}
	}

	@Tool("Replace a draft content page specification with the provided one.")
	public String updatePageSpecification(
		@P("Site external reference code") String siteExternalReferenceCode,
		@P("Site page external reference code") String
			sitePageExternalReferenceCode,
		@P("Full ContentPageSpecification JSON payload to persist")
			String body) {

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(_companyId)) {

			return _updatePageSpecification(
				body, siteExternalReferenceCode, sitePageExternalReferenceCode);
		}
		catch (Exception exception) {
			return ReflectionUtil.throwException(exception);
		}
	}

	private void _alignExternalReferenceCodes(
		Object node, String oldERC, String newERC) {

		if (node instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject)node;

			for (String key : new HashSet<>(jsonObject.keySet())) {
				Object value = jsonObject.get(key);

				if (key.endsWith("ExternalReferenceCode") &&
					(value instanceof String)) {

					String stringValue = (String)value;

					if (stringValue.equals(oldERC)) {
						jsonObject.put(key, newERC);
					}
					else if (stringValue.startsWith(oldERC + "-")) {
						jsonObject.put(
							key,
							newERC +
								stringValue.substring(oldERC.length()));
					}
				}
				else {
					_alignExternalReferenceCodes(value, oldERC, newERC);
				}
			}
		}
		else if (node instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray)node;

			for (int i = 0; i < jsonArray.length(); i++) {
				_alignExternalReferenceCodes(
					jsonArray.get(i), oldERC, newERC);
			}
		}
	}

	private void _ensurePageExperienceERCs(
		JSONObject bodyJSONObject, String specERC) {

		JSONArray pageExperiences = bodyJSONObject.getJSONArray(
			"pageExperiences");

		if (pageExperiences == null) {
			return;
		}

		for (int i = 0; i < pageExperiences.length(); i++) {
			JSONObject experience = pageExperiences.getJSONObject(i);

			if (experience == null) {
				continue;
			}

			String erc = experience.getString("externalReferenceCode");

			if (Validator.isNull(erc)) {
				experience.put(
					"externalReferenceCode", specERC + "-default");
			}
		}
	}

	private String _getPageSpecification(
			String siteExternalReferenceCode,
			String sitePageExternalReferenceCode)
		throws Exception {

		String cached = _pageSpecCache.get(sitePageExternalReferenceCode);

		if (cached != null) {
			return cached;
		}

		String location = _getPageSpecificationLocation(
			siteExternalReferenceCode, sitePageExternalReferenceCode);

		location = HttpComponentsUtil.addParameter(
			location, "nestedFields", "pageExperiences,settings");

		Http.Options options = new Http.Options();

		options.addHeader("Authorization", _accessToken);
		options.addHeader(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
		options.addHeader("Liferay-AI-Hub-Cell-On-Behalf-Of", _userToken);
		options.setLocation(location);
		options.setMethod(Http.Method.GET);

		String responseBody = HttpUtil.URLtoString(options);

		int responseCode = options.getResponse(
		).getResponseCode();

		if ((responseCode < 200) || (responseCode >= 300)) {
			return responseBody;
		}

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(responseBody);

		_pruneReadOnlyFields(jsonObject);

		String result = jsonObject.toString();

		_pageSpecCache.put(sitePageExternalReferenceCode, result);

		return result;
	}

	private String _getPageSpecificationLocation(
			String siteExternalReferenceCode,
			String sitePageExternalReferenceCode)
		throws Exception {

		if (Validator.isNull(_accessToken) ||
			!_accessToken.startsWith("Bearer ")) {

			throw new IllegalArgumentException();
		}

		OAuth2Authorization oAuth2Authorization =
			OAuth2AuthorizationLocalServiceUtil.
				getOAuth2AuthorizationByAccessTokenContent(
					_accessToken.substring(7));

		OAuth2Application oAuth2Application =
			OAuth2ApplicationLocalServiceUtil.getOAuth2Application(
				oAuth2Authorization.getOAuth2ApplicationId());

		return StringBundler.concat(
			oAuth2Application.getHomePageURL(),
			"/o/headless-admin-site/v1.0/sites/",
			URLCodec.encodeURL(siteExternalReferenceCode),
			"/page-specifications/",
			URLCodec.encodeURL(sitePageExternalReferenceCode));
	}

	private void _pruneReadOnlyFields(Object value) {
		if (value instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject)value;

			for (String key : _readOnlyKeys) {
				jsonObject.remove(key);
			}

			for (String key : new HashSet<>(jsonObject.keySet())) {
				_pruneReadOnlyFields(jsonObject.get(key));
			}
		}
		else if (value instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray)value;

			for (int i = 0; i < jsonArray.length(); i++) {
				_pruneReadOnlyFields(jsonArray.get(i));
			}
		}
	}

	private String _stripMarkdownFences(String body) {
		if (body == null) {
			return body;
		}

		body = body.trim();

		if (body.startsWith("```")) {
			int firstNewline = body.indexOf('\n');

			if (firstNewline > 0) {
				body = body.substring(firstNewline + 1);
			}
			else {
				body = body.substring(3);
			}
		}

		if (body.endsWith("```")) {
			body = body.substring(0, body.length() - 3);
		}

		return body.trim();
	}

	private String _updatePageSpecification(
			String body, String siteExternalReferenceCode,
			String sitePageExternalReferenceCode)
		throws Exception {

		body = _stripMarkdownFences(body);

		JSONObject bodyJSONObject = JSONFactoryUtil.createJSONObject(body);

		String bodyERC = bodyJSONObject.getString("externalReferenceCode");

		if (Validator.isNull(bodyERC)) {
			bodyJSONObject.put(
				"externalReferenceCode", sitePageExternalReferenceCode);
		}
		else if (!bodyERC.equals(sitePageExternalReferenceCode)) {
			_alignExternalReferenceCodes(
				bodyJSONObject, bodyERC, sitePageExternalReferenceCode);
		}

		_ensurePageExperienceERCs(
			bodyJSONObject, sitePageExternalReferenceCode);

		body = bodyJSONObject.toString();

		String location = _getPageSpecificationLocation(
			siteExternalReferenceCode, sitePageExternalReferenceCode);

		Http.Options options = new Http.Options();

		options.addHeader("Authorization", _accessToken);
		options.addHeader(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
		options.addHeader("Liferay-AI-Hub-Cell-On-Behalf-Of", _userToken);
		options.setBody(body, ContentTypes.APPLICATION_JSON, "UTF-8");
		options.setLocation(location);
		options.setMethod(Http.Method.PUT);

		_log.error(
			StringBundler.concat(
				"updatePageSpecification PUT URL: ", location,
				"\nAuthorization: ", _accessToken,
				"\nRequest body:\n", body));

		String responseBody = HttpUtil.URLtoString(options);

		int responseCode = options.getResponse(
		).getResponseCode();

		if ((responseCode < 200) || (responseCode >= 300)) {
			_log.error(
				StringBundler.concat(
					"updatePageSpecification failed with HTTP ", responseCode,
					" for site ", siteExternalReferenceCode,
					" and page specification ", sitePageExternalReferenceCode,
					". Request body: ", body, ". Response body: ",
					responseBody));

			return StringBundler.concat(
				"HTTP ", responseCode, _RETRY_HINT_PREFIX, body,
				"\n\nError response:\n", responseBody);
		}

		JSONObject responseJSON = JSONFactoryUtil.createJSONObject(
			responseBody);

		_pruneReadOnlyFields(responseJSON);

		_pageSpecCache.put(
			sitePageExternalReferenceCode, responseJSON.toString());

		return responseBody;
	}

	private static final String _RETRY_HINT_PREFIX =
		". The server rejected the request. Compare the body you sent with " +
			"the error response below, correct the body, and call " +
				"updatePageSpecification again.\n\nRequest body sent:\n";

	private static final Log _log = LogFactoryUtil.getLog(SitePageTools.class);

	private static final Map<String, String> _pageSpecCache =
		new ConcurrentHashMap<>();

	private static final Set<String> _readOnlyKeys = Set.of(
		"configuration", "css", "customFields", "datePropagated",
		"draftFragmentInstanceExternalReferenceCode", "html", "indexed", "js",
		"namespace", "pageSpecificationExternalReferenceCode",
		"taxonomyCategoryBriefs", "uuid");

	private final String _accessToken;
	private final long _companyId;
	private final String _userToken;

}
