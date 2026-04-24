<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
ViewContentSitesDisplayContext viewContentSitesDisplayContext = (ViewContentSitesDisplayContext)request.getAttribute(ViewContentSitesDisplayContext.class.getName());
%>

<react:component
	module="{ReviewStep} from ai-hub-cell-web"
	props='<%=
		HashMapBuilder.<String, Object>put(
			"backURL", viewContentSitesDisplayContext.getRefineStepURL()
		).put(
			"cancelURL", viewContentSitesDisplayContext.getBackURL()
		).put(
			"runId", viewContentSitesDisplayContext.getRunId()
		).build()
	%>'
/>