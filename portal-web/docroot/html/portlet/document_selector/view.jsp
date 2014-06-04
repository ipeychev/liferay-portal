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

<%@ include file="/html/portlet/document_selector/init.jsp" %>

<%
long groupId = ParamUtil.getLong(request, "groupId");

String eventName = ParamUtil.getString(request, "eventName");

boolean showGroupsSelector = ParamUtil.getBoolean(request, "showGroupsSelector");
%>

<c:if test="<%= showGroupsSelector %>">

	<%
	Group selectedGroup = GroupLocalServiceUtil.fetchGroup(groupId);
	%>

	<liferay-ui:icon-menu direction="down" extended="<%= false %>" icon="<%= StringPool.BLANK %>" message="<%= HtmlUtil.escape(selectedGroup.getDescriptiveName()) %>" showWhenSingleIcon="<%= true %>" triggerCssClass="btn">

		<%
		String refererPortletName = ParamUtil.getString(request, "refererPortletName");

		PortletURL selectGroupURL = renderResponse.createRenderURL();

		selectGroupURL.setParameter("struts_action", "/document_selector/view");
		selectGroupURL.setParameter("eventName", eventName);
		selectGroupURL.setParameter("showGroupsSelector", String.valueOf(showGroupsSelector));

		for (Group group : PortalUtil.getBrowsableScopeGroups(themeDisplay.getUserId(), themeDisplay.getCompanyId(), themeDisplay.getScopeGroupId(), refererPortletName)) {
			selectGroupURL.setParameter("groupId", String.valueOf(group.getGroupId()));
		%>

			<liferay-ui:icon
				iconCssClass="<%= group.getIconCssClass() %>"
				message="<%= HtmlUtil.escape(group.getDescriptiveName(locale)) %>"
				url="<%= selectGroupURL.toString() %>"
			/>

		<%
		}
		%>

	</liferay-ui:icon-menu>
</c:if>

<%
String tabsString = ParamUtil.getString(renderRequest, "tabs", "documents,pages");

String[] tabs = StringUtil.split(tabsString);
%>

<c:choose>
	<c:when test="<%= !ArrayUtil.isEmpty(tabs) && (tabs.length > 1) %>">
		<liferay-ui:tabs names="<%= tabsString %>" refresh="<%= false %>" type="pills">

			<%
			for (String tab : tabs) {
			%>

				<liferay-ui:section>
					<div>
						<liferay-util:include page='<%= "/html/portlet/document_selector/" + tab + ".jsp" %>'/>
					</div>
				</liferay-ui:section>

			<%
			}
			%>

		</liferay-ui:tabs>
	</c:when>
	<c:otherwise>
		<liferay-util:include page='<%= "/html/portlet/document_selector/" + tabs[0] + ".jsp" %>'/>
	</c:otherwise>
</c:choose>