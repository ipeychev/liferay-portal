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
String portletId = portletDisplay.getRootPortletId();

String mainPath = themeDisplay.getPathMain();

String doAsUserId = themeDisplay.getDoAsUserId();

if (Validator.isNull(doAsUserId)) {
	doAsUserId = Encryptor.encrypt(company.getKeyObj(), String.valueOf(themeDisplay.getUserId()));
}

long doAsGroupId = themeDisplay.getDoAsGroupId();

String alloyEditorConfigFileName = ParamUtil.getString(request, "ckEditorConfigFileName");

if (!_alloyEditorConfigFileNames.contains(alloyEditorConfigFileName)) {
	alloyEditorConfigFileName = "alloyconfig.jsp";
}

Map<String, String> configParamsMap = (Map<String, String>)request.getAttribute("liferay-ui:input-editor:configParams");

String configParams = marshallParams(configParamsMap);

String contentsLanguageId = (String)request.getAttribute("liferay-ui:input-editor:contentsLanguageId");
String cssClasses = GetterUtil.getString((String)request.getAttribute("liferay-ui:input-editor:cssClasses"));
String name = namespace + GetterUtil.getString((String)request.getAttribute("liferay-ui:input-editor:name"));
boolean inlineEdit = GetterUtil.getBoolean((String)request.getAttribute("liferay-ui:input-editor:inlineEdit"));

boolean resizable = GetterUtil.getBoolean((String)request.getAttribute("liferay-ui:input-editor:resizable"));
%>

<link href="/html/js/editor/alloy/assets/alloy-editor.css" rel="stylesheet">

<script src="/html/js/editor/alloy/ckeditor/ckeditor.js"></script>

<script type="text/javascript">
	CKEDITOR.disableAutoInline = true;

	CKEDITOR.env.isCompatible = true;
</script>

<script src="/html/js/editor/alloy/all.js"></script>

<aui:script use="aui-base">
	window['<%= name %>'] = {
		destroy: function() {
		},

		focus: function() {
		},

		getHTML: function() {
		},

		initEditor: function() {
		},

		instanceReady: false,

		setHTML: function(value) {
		}
	};

	CKEDITOR.inline(
		'<%= name %>',
		{
			customConfig: '<%= PortalUtil.getPathContext() %>/html/js/editor/alloy/<%= HtmlUtil.escapeJS(alloyEditorConfigFileName) %>?p_p_id=<%= HttpUtil.encodeURL(portletId) %>&p_main_path=<%= HttpUtil.encodeURL(mainPath) %>&contentsLanguageId=<%= HttpUtil.encodeURL(contentsLanguageId) %>&colorSchemeCssClass=<%= HttpUtil.encodeURL(themeDisplay.getColorScheme().getCssClass()) %>&cssClasses=<%= HttpUtil.encodeURL(cssClasses) %>&cssPath=<%= HttpUtil.encodeURL(themeDisplay.getPathThemeCss()) %>&doAsGroupId=<%= HttpUtil.encodeURL(String.valueOf(doAsGroupId)) %>&doAsUserId=<%= HttpUtil.encodeURL(doAsUserId) %>&imagesPath=<%= HttpUtil.encodeURL(themeDisplay.getPathThemeImages()) %>&inlineEdit=<%= inlineEdit %><%= configParams %>&languageId=<%= HttpUtil.encodeURL(LocaleUtil.toLanguageId(locale)) %>&name=<%= name %>&resizable=<%= resizable %>',
		}
	);

	if (window['<%= name %>Config']) {
		window['<%= name %>Config']();
	}

	var destroyInstance = function(event) {
		if (event.portletId === '<%= portletId %>') {
			try {
				var ckeditorInstances = window.CKEDITOR.instances;

				A.Object.each(
					ckeditorInstances,
					function(value, key) {
						var inst = ckeditorInstances[key];

						delete ckeditorInstances[key];

						inst.destroy();
					}
				);
			}
			catch(error) {
			}

			Liferay.detach('destroyPortlet', destroyInstance);
		}
	};

	Liferay.on('destroyPortlet', destroyInstance);
</aui:script>

<%!
public String marshallParams(Map<String, String> params) {
	if (params == null) {
		return StringPool.BLANK;
	}

	StringBundler sb = new StringBundler(4 * params.size());

	for (Map.Entry<String, String> configParam : params.entrySet()) {
		sb.append(StringPool.AMPERSAND);
		sb.append(configParam.getKey());
		sb.append(StringPool.EQUAL);
		sb.append(HttpUtil.encodeURL(configParam.getValue()));
	}

	return sb.toString();
}

private static Set<String> _alloyEditorConfigFileNames = SetUtil.fromArray(new String[] {"alloyconfig.jsp"});
%>