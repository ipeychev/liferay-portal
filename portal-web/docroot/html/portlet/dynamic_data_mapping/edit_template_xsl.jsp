<%--
/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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

<%@ include file="/html/portlet/dynamic_data_mapping/init.jsp" %>

<%
String langType = ParamUtil.getString(request, "langType");

String editorContentInputElement = "#" + renderResponse.getNamespace() + "scriptContent";
String editorContentOutputElement = "#" + renderResponse.getNamespace() + "scriptContent";

String editorType = ParamUtil.getString(request, "editorType");

if (Validator.isNotNull(editorType)) {
	portalPreferences.setValue(PortletKeys.DYNAMIC_DATA_MAPPING, "editor-type", editorType);
}
else {
	editorType = portalPreferences.getValue(PortletKeys.DYNAMIC_DATA_MAPPING, "editor-type", "plain");
}

boolean useRichEditor = editorType.equals("rich");

String editorMode = "php";

if (langType.equals("css")) {
	editorMode = "css";
}
else if (langType.equals("xml") || langType.equals("xsl") || langType.equals("xsd")) {
	editorMode = "xml";
}
%>

<aui:fieldset>
	<aui:select name="editorType">
		<aui:option label="plain" value="plain" />
		<aui:option label="rich" selected="<%= useRichEditor %>" value="rich" />
	</aui:select>

	<div class="lfr-plain-editor <%= useRichEditor ? "aui-helper-hidden" : StringPool.BLANK %>" id="<portlet:namespace />plainEditor">
		<aui:input cssClass="lfr-template-editor" inputCssClass="lfr-editor-textarea" label="" name="plainEditorField" onKeyDown="Liferay.Util.checkTab(this); Liferay.Util.disableEsc();" type="textarea" value="" wrap="off" />
	</div>

	<div class="lfr-rich-editor <%= !useRichEditor ? "aui-helper-hidden" : StringPool.BLANK %>" id="<portlet:namespace />richEditor"></div>
</aui:fieldset>

<aui:script use="aui-ace-editor,aui-dialog,aui-io-request">
	var editorType = '<%= HtmlUtil.escapeJS(editorType) %>';

	var editorContentInputElement = A.one('<%= HtmlUtil.escapeJS(editorContentInputElement) %>');
	var editorContentOutputElement = A.one('<%= HtmlUtil.escapeJS(editorContentOutputElement) %>');

	var plainEditorField = A.one('#<portlet:namespace />plainEditorField');

	var richEditor;

	var prevEditorContent;

	function getEditorContent(type) {
		var content = '';

		if (type == 'plain') {
			content = plainEditorField.val();
		}
		else {
			content = richEditor.getSession().getValue();
		}

		return content;
	}

	function setEditorContent(type, content) {
		if (type == 'plain') {
			plainEditorField.val(content);
		}
		else {
			richEditor.getSession().setValue(content);
		}

		prevEditorContent = content;
	}

	function updateEditorType(event) {
		var oldEditorType = editorType;

		var newEditorType = A.one('#<portlet:namespace />editorType').val();

		var oldEditorContent = getEditorContent(oldEditorType);

		setEditorContent(newEditorType, oldEditorContent);

		var richEditorType = (newEditorType != 'plain');

		A.one('#<portlet:namespace />plainEditor').toggle(!richEditorType);
		A.one('#<portlet:namespace />richEditor').toggle(richEditorType);

		if (richEditorType) {
			richEditor.editor.resize();
		}

		var uri = '<portlet:renderURL><portlet:param name="struts_action" value="/dynamic_data_mapping/edit_template_xsl" /></portlet:renderURL>&editorType=' + newEditorType;

		A.io.request(uri);

		editorType = newEditorType;
	}

	function updateTemplateXsl() {
		var content = getEditorContent(editorType);

		if (editorContentOutputElement) {
			var editorMode = '<%= editorMode %>';

			editorContentOutputElement.val(content);

			var dialog = Liferay.Util.getWindow();

			if (dialog) {
				dialog.close();

				if (content != prevEditorContent) {
					dialog.fire('update');
				}
			}
		}
	}

	A.on(
		'domready',
		function(event) {
			richEditor = new A.AceEditor(
				{
					boundingBox: '#<portlet:namespace />richEditor',
					width: '100%',
					height: '400',
					mode: '<%= editorMode %>'
				}
			).render();

			debugger;
			if (editorContentInputElement) {
				setEditorContent(editorType, editorContentInputElement.val());
			}

			window.<portlet:namespace />getEditorContent = getEditorContent;

			A.one('#<portlet:namespace />editorType').on('change', updateEditorType);
			A.one('#<portlet:namespace />update-button').on('click', updateTemplateXsl);
		},
		'#<portlet:namespace />richEditor'
	);
</aui:script>