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

/**
 * @author Feliphe Marinho
 */
public class SitePageTools {

	public SitePageTools(String accessToken, long companyId, String userToken) {
		_accessToken = accessToken;
		_companyId = companyId;
		_userToken = userToken;
	}

	@Tool("Retrieve a site page and its page specifications")
	public String getSitePage(
		@P("Site external reference code") String siteExternalReferenceCode,
		@P("Site page external reference code") String
			sitePageExternalReferenceCode) {

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(_companyId)) {

			return _getSitePage(
				siteExternalReferenceCode, sitePageExternalReferenceCode);
		}
		catch (Exception exception) {
			return ReflectionUtil.throwException(exception);
		}
	}

	@Tool(
		"Update a site page. The body must be the full ContentPage JSON " +
			"payload; pageSpecifications is replaced wholesale."
	)
	public String updateSitePage(
		@P("Site external reference code") String siteExternalReferenceCode,
		@P("Site page external reference code") String
			sitePageExternalReferenceCode,
		@P("Full ContentPage JSON payload to persist") String body) {

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(_companyId)) {

			return _updateSitePage(
				body, siteExternalReferenceCode, sitePageExternalReferenceCode);
		}
		catch (Exception exception) {
			return ReflectionUtil.throwException(exception);
		}
	}

	private String _getSitePage(
			String siteExternalReferenceCode,
			String sitePageExternalReferenceCode)
		throws Exception {

		String location = _getSitePageLocation(
			siteExternalReferenceCode, sitePageExternalReferenceCode);

		location = HttpComponentsUtil.addParameter(
			location, "nestedFields", "pageSpecifications");
		location = HttpComponentsUtil.addParameter(
			location, "privateLayout", true);

		Http.Options options = new Http.Options();

		options.addHeader(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
		options.addHeader("Liferay-AI-Hub-Cell-On-Behalf-Of", _userToken);
		options.setLocation(location);
		options.setMethod(Http.Method.GET);

		return HttpUtil.URLtoString(options);
	}

	private String _getSitePageLocation(
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
			URLCodec.encodeURL(siteExternalReferenceCode), "/site-pages/",
			URLCodec.encodeURL(sitePageExternalReferenceCode));
	}

	private String _updateSitePage(
			String body, String siteExternalReferenceCode,
			String sitePageExternalReferenceCode)
		throws Exception {

		String location = _getSitePageLocation(
			siteExternalReferenceCode, sitePageExternalReferenceCode);

		location = HttpComponentsUtil.addParameter(
			location, "nestedFields", "pageSpecifications");
		location = HttpComponentsUtil.addParameter(
			location, "privateLayout", false);

		Http.Options options = new Http.Options();

		options.addHeader(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
		options.addHeader("Liferay-AI-Hub-Cell-On-Behalf-Of", _userToken);
		options.setBody(body, ContentTypes.APPLICATION_JSON, "UTF-8");
		options.setLocation(location);
		options.setMethod(Http.Method.PATCH);

		String responseBody = HttpUtil.URLtoString(options);

		int responseCode = options.getResponse(
		).getResponseCode();

		if ((responseCode < 200) || (responseCode >= 300)) {
			return StringBundler.concat(
				"HTTP ", String.valueOf(responseCode),
				". The server rejected the request. Analyze the error ",
				"response below, correct the body, and call updateSitePage ",
				"again.\n\n", responseBody);
		}

		return responseBody;
	}

	private final String _accessToken;
	private final long _companyId;
	private final String _userToken;

}