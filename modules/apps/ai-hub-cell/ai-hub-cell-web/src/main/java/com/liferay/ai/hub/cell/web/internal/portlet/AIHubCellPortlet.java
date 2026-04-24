/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.cell.web.internal.portlet;

import com.liferay.ai.hub.cell.web.internal.constants.AIHubCellPortletKeys;
import com.liferay.ai.hub.cell.web.internal.display.context.ViewContentSitesDisplayContext;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.Portlet;
import jakarta.portlet.PortletException;
import jakarta.portlet.RenderRequest;
import jakarta.portlet.RenderResponse;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mylena Monte
 */
@Component(
	property = {
		"com.liferay.portlet.add-default-resource=true",
		"com.liferay.portlet.css-class-wrapper=portlet-content-site-generator",
		"com.liferay.portlet.display-category=category.hidden",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"com.liferay.portlet.preferences-unique-per-layout=false",
		"com.liferay.portlet.private-request-attributes=false",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.render-weight=50",
		"com.liferay.portlet.scopeable=false",
		"com.liferay.portlet.use-default-template=true",
		"jakarta.portlet.display-name=Content Site Generator",
		"jakarta.portlet.expiration-cache=0",
		"jakarta.portlet.init-param.portlet-title-based-navigation=true",
		"jakarta.portlet.init-param.template-path=/META-INF/resources/",
		"jakarta.portlet.init-param.view-template=/view.jsp",
		"jakarta.portlet.name=" + AIHubCellPortletKeys.CONTENT_SITE_GENERATOR,
		"jakarta.portlet.resource-bundle=content.Language",
		"jakarta.portlet.security-role-ref=administrator",
		"jakarta.portlet.version=4.0"
	},
	service = Portlet.class
)
public class AIHubCellPortlet extends MVCPortlet {

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		HttpServletRequest httpServletRequest = _portal.getHttpServletRequest(
			renderRequest);

		LiferayPortletResponse liferayPortletResponse =
			_portal.getLiferayPortletResponse(renderResponse);

		ViewContentSitesDisplayContext viewContentSitesDisplayContext =
			new ViewContentSitesDisplayContext(
				httpServletRequest, liferayPortletResponse);

		renderRequest.setAttribute(
			ViewContentSitesDisplayContext.class.getName(),
			viewContentSitesDisplayContext);

		if (_isSiteGeneratorMVCPath(renderRequest)) {
			ThemeDisplay themeDisplay =
				(ThemeDisplay)renderRequest.getAttribute(WebKeys.THEME_DISPLAY);

			PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

			portletDisplay.setShowBackIcon(true);
			portletDisplay.setURLBack(
				viewContentSitesDisplayContext.getBackURL());

			_portal.setPageTitle(
				LanguageUtil.get(httpServletRequest, "site-generator"),
				httpServletRequest);
		}

		super.render(renderRequest, renderResponse);
	}

	@Override
	protected String getTitle(RenderRequest renderRequest) {
		if (_isSiteGeneratorMVCPath(renderRequest)) {
			return LanguageUtil.get(
				_portal.getHttpServletRequest(renderRequest), "site-generator");
		}

		return super.getTitle(renderRequest);
	}

	private boolean _isSiteGeneratorMVCPath(RenderRequest renderRequest) {
		String mvcPath = renderRequest.getParameter("mvcPath");

		if (_REFINE_STEP_MVC_PATH.equals(mvcPath) ||
			_REVIEW_STEP_MVC_PATH.equals(mvcPath) ||
			_SITE_GENERATOR_MVC_PATH.equals(mvcPath)) {

			return true;
		}

		return false;
	}

	private static final String _REFINE_STEP_MVC_PATH = "/view_refine_step.jsp";

	private static final String _REVIEW_STEP_MVC_PATH = "/view_review_step.jsp";

	private static final String _SITE_GENERATOR_MVC_PATH =
		"/view_content_site_generator.jsp";

	@Reference
	private Portal _portal;

}