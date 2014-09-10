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

<%@ include file="/html/taglib/init.jsp" %>

<%
String displayStyle = (String)request.getAttribute("liferay-ui:app-view-display-style:displayStyle");
String[] displayStyles = (String[])request.getAttribute("liferay-ui:app-view-display-style:displayStyles");
PortletURL displayStyleURL = (PortletURL)request.getAttribute("liferay-ui:app-view-display-style:displayStyleURL");
String eventName = (String)request.getAttribute("liferay-ui:app-view-display-style:eventName");
Map<String, String> requestParams = (Map<String, String>)request.getAttribute("liferay-ui:app-view-display-style:requestParams");
%>

<c:if test="<%= displayStyles.length > 1 %>">
	<span class="display-style-buttons-container" id="<portlet:namespace />displayStyleButtonsContainer">
		<div class="display-style-buttons" id="<portlet:namespace />displayStyleButtons">
			<aui:nav-item anchorCssClass="btn btn-default" dropdown="<%= true %>" iconCssClass='<%= "icon-" + _getIcon(displayStyle) %>'>

				<%
				for (String dataStyle : displayStyles) {
				%>

					<c:choose>
						<c:when test="<%= displayStyle != null %>">

							<%
							displayStyleURL.setParameter("displayStyle", dataStyle);
							%>

							<aui:nav-item
								href="<%= displayStyleURL.toString() %>"
								iconCssClass='<%= "icon-" + _getIcon(dataStyle) %>'
								label="<%= dataStyle %>"
							/>
						</c:when>
						<c:otherwise>

							<%
							Map<String, Object> data = new HashMap<String, Object>();

							data.put("displayStyle", dataStyle);
							%>

							<aui:nav-item
								anchorData="<%= data %>"
								href="javascript:;"
								iconCssClass='<%= "icon-" + _getIcon(dataStyle) %>'
								label="<%= dataStyle %>"
							/>
						</c:otherwise>
					</c:choose>

				<%
				}
				%>

			</aui:nav-item>
		</div>
	</span>

	<c:if test="<%= displayStyleURL == null %>">
		<aui:script use="aui-base">
			function changeDisplayStyle(displayStyle) {
				var config = {};

				<%
				if (requestParams != null) {
					Set<String> requestParamNames = requestParams.keySet();

					for (String requestParamName : requestParamNames) {
						String requestParamValue = requestParams.get(requestParamName);
				%>

						config['<portlet:namespace /><%= requestParamName %>'] = '<%= HtmlUtil.escapeJS(requestParamValue) %>';

				<%
					}
				}
				%>

				config['<portlet:namespace />displayStyle'] = displayStyle;

				Liferay.fire(
					'<portlet:namespace />dataRequest',
					{
						requestParams: config,
						src: Liferay.DL_ENTRIES_PAGINATOR
					}
				);
			}

			var displayStyleButtonsMenu = A.one('#<portlet:namespace />displayStyleButtons .dropdown-menu');

			if (displayStyleButtonsMenu) {
				displayStyleButtonsMenu.delegate(
					'click',
					function(event) {
						var displayStyle = event.currentTarget.attr('data-displayStyle');

						if (<%= requestParams != null %>) {
							changeDisplayStyle(displayStyle);
						}
						else if (<%= eventName != null %>) {
							Liferay.fire(
								'<%= eventName %>',
								{
									displayStyle: displayStyle
								}
							);
						}
					},
					'li > a'
				);
			}
		</aui:script>
	</c:if>
</c:if>

<%!
private String _getIcon(String displayStyle) {
	String displayStyleIcon = displayStyle;

	if (displayStyle.equals("descriptive")) {
		displayStyleIcon = "th-list";
	}
	else if (displayStyle.equals("icon")) {
		displayStyleIcon = "th-large";
	}
	else if (displayStyle.equals("list")) {
		displayStyleIcon = "align-justify";
	}

	return displayStyleIcon;
}
%>