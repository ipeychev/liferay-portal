<%--
/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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
String cssClass = GetterUtil.getString((String)request.getAttribute("liferay-ui:input-editor:cssClass"));
String initMethod = (String)request.getAttribute("liferay-ui:input-editor:initMethod");
String name = namespace + GetterUtil.getString((String)request.getAttribute("liferay-ui:input-editor:name"));
%>

<div class="lfr-textarea message-edit <%= HtmlUtil.escapeAttribute(cssClass) %>">
	<textarea id="<%= HtmlUtil.escapeAttribute(name) %>" name="<%= HtmlUtil.escapeAttribute(name) %>"></textarea>
</div>

<aui:script use="liferay-bbcode-editor">
	window['<%= HtmlUtil.escapeJS(name) %>'] = new Liferay.Editor.bbCode(
		{
			textarea: '#<%= HtmlUtil.escapeJS(name) %>'
		}
	);

	<c:if test="<%= Validator.isNotNull(initMethod) %>">
		window['<%= HtmlUtil.escapeJS(name) %>'].setHTML(<%= HtmlUtil.escapeJS(namespace + initMethod) %>());
	</c:if>
</aui:script>