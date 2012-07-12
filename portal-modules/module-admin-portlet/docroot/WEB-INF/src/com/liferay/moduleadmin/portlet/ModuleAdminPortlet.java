/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.moduleadmin.portlet;

import com.liferay.moduleadmin.internal.ModuleUtil;
import com.liferay.mvc.freemarker.FreeMarkerMVCPortlet;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.module.framework.ModuleFrameworkException;
import com.liferay.portal.module.framework.ModuleFrameworkUtil;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.WebKeys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

/**
 * @author Raymond Augé
 */
public class ModuleAdminPortlet extends FreeMarkerMVCPortlet {

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

		try {
			UploadPortletRequest uploadRequest =
				PortalUtil.getUploadPortletRequest(actionRequest);

			String cmd = ParamUtil.getString(uploadRequest, Constants.CMD);
			File file = uploadRequest.getFile("importBundle");
			String location = ParamUtil.getString(uploadRequest, "location");

			if (cmd.equals("install-from-upload")) {
				if (Validator.isNull(location)) {
					location = uploadRequest.getFullFileName("importBundle");
				}

				if ((file == null) || !file.exists()) {
					throw new ModuleFrameworkException("file-does-not-exist");
				}

				ModuleFrameworkUtil.addBundle(
					location, new FileInputStream(file));
			}
			else if (cmd.equals("install-from-remote-location")) {
				ModuleFrameworkUtil.addBundle(location);
			}
			else if (cmd.equals("update-from-upload")) {
				long bundleId = ParamUtil.getLong(uploadRequest, "bundleId");

				if ((file == null) || !file.exists()) {
					throw new ModuleFrameworkException("file-does-not-exist");
				}

				ModuleFrameworkUtil.updateBundle(
					bundleId, new FileInputStream(file));
			}
			else if (cmd.equals("update-from-remote-location")) {
				long bundleId = ParamUtil.getLong(uploadRequest, "bundleId");

				ModuleFrameworkUtil.updateBundle(bundleId);
			}
			else if (cmd.equals("uninstall")) {
				long bundleId = ParamUtil.getLong(uploadRequest, "bundleId");

				ModuleFrameworkUtil.uninstallBundle(bundleId);
			}

			sendRedirect(actionRequest, actionResponse);
		}
		catch (Exception e) {
			if ((e instanceof ModuleFrameworkException) ||
				(e instanceof PrincipalException)) {

				SessionErrors.add(actionRequest, e.getClass().getName());
			}
		}
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException, IOException {

		Map<String, Object> ftlVariables = new HashMap<String,Object>();

		ftlVariables.put("ModuleUtil", new ModuleUtil());

		renderRequest.setAttribute(WebKeys.FTL_VARIABLES, ftlVariables);

		super.render(renderRequest, renderResponse);
	}

	@Override
	public void serveResource(
		ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws IOException, PortletException {

		String cmd = ParamUtil.getString(resourceRequest, Constants.CMD);
		long bundleId = ParamUtil.getLong(resourceRequest, "bundleId");

		resourceResponse.setContentType(ContentTypes.APPLICATION_JSON);

		String json = null;

		try {
			if (cmd.equals("setBundleStartLevel")) {
				int startLevel = ParamUtil.getInteger(
					resourceRequest, "startLevel");

				ModuleFrameworkUtil.setBundleStartLevel(bundleId, startLevel);
			}
			else if (cmd.equals("startBundle")) {
				ModuleFrameworkUtil.startBundle(bundleId);
			}
			else if (cmd.equals("stopBundle")) {
				ModuleFrameworkUtil.stopBundle(bundleId);
			}
			else if (cmd.equals("uninstallBundle")) {
				ModuleFrameworkUtil.uninstallBundle(bundleId);
			}
			else if (cmd.equals("updateBundle")) {
				ModuleFrameworkUtil.updateBundle(bundleId);
			}

			String state = ModuleFrameworkUtil.getState(bundleId);

			JSONObject jsonResult = JSONFactoryUtil.createJSONObject();

			jsonResult.put("state", state);

			json = jsonResult.toString();
		}
		catch (PortalException pe) {
			json = JSONFactoryUtil.serializeException(pe);
		}

		PrintWriter writer = resourceResponse.getWriter();

		writer.print(json);
		writer.flush();
		writer.close();
	}

}