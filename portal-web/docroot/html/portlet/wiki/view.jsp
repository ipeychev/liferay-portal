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

<%@ include file="/html/portlet/wiki/init.jsp" %>

<aui:form name="myForm">
	<portlet:renderURL var="navigateURL">
		<portlet:param name="struts_action" value="/wiki/view_all_pages" />
	</portlet:renderURL>

	<aui:button value="Navigate" type="button" primary="<%= true %>" href="<%= navigateURL %>" />
</aui:form>

<aui:script position="inline" use="*">
var handler = function() {
	//
};

console.log('attaching listener');
Liferay.after('form:registered', handler);
</aui:script>