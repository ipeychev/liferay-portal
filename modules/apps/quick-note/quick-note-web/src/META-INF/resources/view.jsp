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

<div id="<portlet:namespace />pad" style="background: <%= HtmlUtil.escapeAttribute(color) %>;">
	<c:if test="<%= portletDisplay.isShowConfigurationIcon() %>">
		<table width="100%">
		<tr>
			<td width="60%">
				<div class="portlet-title-default">&nbsp;</div>
			</td>
			<td>
				<c:if test="<%= portletDisplay.isShowCloseIcon() %>">
					<liferay-ui:icon
						cssClass="close-note"
						iconCssClass="icon-remove"
						message="close"
						url="<%= portletDisplay.getURLClose() %>"
					/>
				</c:if>

				<span class="note-color yellow"></span>
				<span class="green note-color"></span>
				<span class="blue note-color"></span>
				<span class="note-color red"></span>
			</td>
		</tr>
		</table>
	</c:if>

	<div class="note-content" id="<portlet:namespace />note"><%= StringUtil.replace(HtmlUtil.escape(data), "&lt;br /&gt;", "<br />") %></div>
</div>

<div>
	<h1>Small Length (3) Sequences</h1>
	<div id="<portlet:namespace />SmallSequences"></div>

	<h1>Default Length (10) Sequences</h1>
	<div id="<portlet:namespace />DefaultSequences"></div>

	<h1>Long Length (30) Sequences</h1>
	<div id="<portlet:namespace />LongSequences"></div>
</div>

<script src="/o/js-es6-demo-1.0.0/browser-polyfill.js" />
<aui:script require="quick-note/js/sequencePrinter.es">
	var smallSequencesContainer = $('#<portlet:namespace />SmallSequences')[0];
	new quickNoteJsSequencePrinterEs(smallSequencesContainer, 3).print();

	var defaultSequencesContainer = $('#<portlet:namespace />DefaultSequences')[0];
	new quickNoteJsSequencePrinterEs(defaultSequencesContainer).print();

	var longSequencesContainer = $('#<portlet:namespace />LongSequences')[0];
	new quickNoteJsSequencePrinterEs(longSequencesContainer, 30).print();
</aui:script>