<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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
--%>

<%@ include file="/init.jsp" %>

<%
Group group = null;
LayoutSet layoutSet = null;

if (layout != null) {
	group = layout.getGroup();
	layoutSet = layout.getLayoutSet();
}

boolean hasLayoutCustomizePermission = LayoutPermissionUtil.contains(permissionChecker, layout, ActionKeys.CUSTOMIZE);
boolean hasLayoutUpdatePermission = LayoutPermissionUtil.contains(permissionChecker, layout, ActionKeys.UPDATE);

String toggleControlsState = GetterUtil.getString(SessionClicks.get(request, "com.liferay.frontend.js.web_toggleControls", "visible"));

boolean userSetupComplete = false;

if (user.isSetupComplete() || themeDisplay.isImpersonated()) {
	userSetupComplete = true;
}
%>

<c:if test="<%= !layout.isTypeControlPanel() && !group.isControlPanel() && userSetupComplete %>">
	<div class="control-menu">
		<c:if test="<%= (user.isSetupComplete() || themeDisplay.isImpersonated()) && themeDisplay.isShowStagingIcon() %>">
			<div class="control-menu-level-2">
				<div class="container-fluid-1280">
					<ul class="control-menu-nav">

						<%
						String renderPortletBoundary = GetterUtil.getString(request.getAttribute(WebKeys.RENDER_PORTLET_BOUNDARY));

						request.setAttribute(WebKeys.RENDER_PORTLET_BOUNDARY, Boolean.FALSE.toString());
						%>

						<liferay-portlet:runtime portletName="<%= PortletKeys.STAGING_BAR %>" />

						<%
						request.setAttribute(WebKeys.RENDER_PORTLET_BOUNDARY, renderPortletBoundary);
						%>

					</ul>
				</div>
			</div>
		</c:if>

		<div class="control-menu-level-1">
			<div class="container-fluid-1280">
				<ul class="control-menu-nav" data-namespace="<portlet:namespace />" id="<portlet:namespace />controlMenu">

					<%
					String productMenuState = SessionClicks.get(request, "com.liferay.control.menu.web_productMenuState", "closed");
					%>

					<li class="<%= Validator.equals(productMenuState, "open") ? "active" : StringPool.BLANK %>">
						<a class="control-menu-icon sidenav-toggler" data-content="body" data-toggle="sidenav" data-type="fixed-push" data-type-mobile="fixed" href="#sidenavSliderId" id="sidenavToggleId">
							<span class="icon-align-justify icon-monospaced"></span>
						</a>
					</li>

					<%
					boolean hasLayoutAddPermission = false;

					if (layout.getParentLayoutId() == LayoutConstants.DEFAULT_PARENT_LAYOUT_ID) {
						hasLayoutAddPermission = GroupPermissionUtil.contains(permissionChecker, group, ActionKeys.ADD_LAYOUT);
					}
					else {
						hasLayoutAddPermission = LayoutPermissionUtil.contains(permissionChecker, layout, ActionKeys.ADD_LAYOUT);
					}
					%>

					<c:if test="<%= !group.isControlPanel() && userSetupComplete && (hasLayoutAddPermission || hasLayoutUpdatePermission || (layoutTypePortlet.isCustomizable() && layoutTypePortlet.isCustomizedView() && hasLayoutCustomizePermission)) %>">
						<portlet:renderURL var="addURL" windowState="<%= LiferayWindowState.EXCLUSIVE.toString() %>">
							<portlet:param name="mvcPath" value="/add_panel.jsp" />
							<portlet:param name="stateMaximized" value="<%= String.valueOf(themeDisplay.isStateMaximized()) %>" />
							<portlet:param name="viewEntries" value="<%= Boolean.TRUE.toString() %>" />
						</portlet:renderURL>

						<%
						Map<String, Object> data = new HashMap<String, Object>();

						data.put("panelURL", addURL);
						%>

						<li>
							<liferay-ui:icon
								data="<%= data %>"
								iconCssClass="icon-plus icon-monospaced"
								id="addPanel"
								label="add"
								linkCssClass="control-menu-icon"
								url="javascript:;"
							/>
						</li>
					</c:if>

					<c:if test="<%= !group.isControlPanel() && userSetupComplete && (themeDisplay.isShowLayoutTemplatesIcon() || themeDisplay.isShowPageSettingsIcon()) %>">

						<%
						String portletId = PortletProviderUtil.getPortletId(Layout.class.getName(), PortletProvider.Action.EDIT);

						PortletURL editPageURL = PortalUtil.getControlPanelPortletURL(request, portletId, 0, PortletRequest.RENDER_PHASE);

						editPageURL.setParameter("groupId", String.valueOf(groupDisplayContextHelper.getLiveGroupId()));
						editPageURL.setParameter("selPlid", String.valueOf(layout.getPlid()));
						editPageURL.setParameter("privateLayout", String.valueOf(layout.isPrivateLayout()));
						editPageURL.setParameter("treeId", "layoutsTree");
						editPageURL.setParameter("viewLayout", Boolean.TRUE.toString());
						%>

						<li>
							<liferay-ui:icon
								iconCssClass="icon-cog icon-monospaced"
								label="edit"
								linkCssClass="control-menu-icon"
								url="<%= editPageURL.toString() %>"
							/>
						</li>
					</c:if>

					<%
					boolean customizableLayout = !(group.isLayoutPrototype() || group.isLayoutSetPrototype() || group.isStagingGroup() || group.isUserGroup()) && layoutTypePortlet.isCustomizable() && LayoutPermissionUtil.containsWithoutViewableGroup(permissionChecker, layout, false, ActionKeys.CUSTOMIZE);
					boolean linkedLayout = (!SitesUtil.isLayoutUpdateable(layout) || (layout.isLayoutPrototypeLinkActive() && !group.hasStagingGroup())) && LayoutPermissionUtil.containsWithoutViewableGroup(themeDisplay.getPermissionChecker(), layout, false, ActionKeys.UPDATE);
					boolean modifiedLayout = (layoutSet != null) && layoutSet.isLayoutSetPrototypeLinkActive() && SitesUtil.isLayoutModifiedSinceLastMerge(layout) && hasLayoutUpdatePermission;
					boolean hasMessages = modifiedLayout || linkedLayout || customizableLayout;
					%>

					<c:if test="<%= (user.isSetupComplete() || themeDisplay.isImpersonated()) && hasMessages %>">
						<li>
							<liferay-ui:icon
								iconCssClass="icon-info icon-monospaced"
								id="infoButton"
								linkCssClass="control-menu-icon"
								url="javascript:;"
							/>

							<c:if test="<%= hasMessages %>">
								<liferay-util:buffer var="infoContainer">
									<%@ include file="/view_page_customization_bar.jspf" %>
								</liferay-util:buffer>

								<aui:script sandbox="<%= true %>">
									$('#<portlet:namespace />infoButton').popover(
										{
											content: '<%= HtmlUtil.escapeJS(infoContainer) %>',
											html: true,
											placement: 'top'
										}
									);
								</aui:script>
							</c:if>
						</li>
					</c:if>

					<c:if test="<%= !group.isControlPanel() && userSetupComplete && (!group.hasStagingGroup() || group.isStagingGroup()) && (hasLayoutUpdatePermission || (layoutTypePortlet.isCustomizable() && layoutTypePortlet.isCustomizedView() && hasLayoutCustomizePermission) || PortletPermissionUtil.hasConfigurationPermission(permissionChecker, themeDisplay.getSiteGroupId(), layout, ActionKeys.CONFIGURATION)) %>">
						<li id="<portlet:namespace />toggleControls">
							<liferay-ui:icon
								cssClass="toggle-controls"
								iconCssClass='<%= "controls-state-icon " + (toggleControlsState.equals("visible") ? "icon-eye-open icon-monospaced" : "icon-eye-close icon-monospaced") %>'
								label="edit-controls"
								linkCssClass="control-menu-icon"
								url="javascript:;"
							/>
						</li>
					</c:if>

					<c:if test="<%= !group.isControlPanel() && userSetupComplete && (hasLayoutUpdatePermission || GroupPermissionUtil.contains(permissionChecker, group, ActionKeys.PREVIEW_IN_DEVICE)) %>">
						<portlet:renderURL var="previewContentURL" windowState="<%= LiferayWindowState.EXCLUSIVE.toString() %>">
							<portlet:param name="mvcPath" value="/preview_panel.jsp" />
						</portlet:renderURL>

						<%
						Map<String, Object> data = new HashMap<String, Object>();

						data.put("panelURL", previewContentURL);
						%>

						<li>
							<liferay-ui:icon
								data="<%= data %>"
								iconCssClass="icon-desktop icon-monospaced"
								id="previewPanel"
								label="preview"
								linkCssClass="control-menu-icon"
								url="javascript:;"
							/>
						</li>
					</c:if>
				</ul>
			</div>
		</div>
	</div>
</c:if>

<aui:script position="inline" use="liferay-control-menu">
	Liferay.ControlMenu.init('#<portlet:namespace />controlMenu');
</aui:script>