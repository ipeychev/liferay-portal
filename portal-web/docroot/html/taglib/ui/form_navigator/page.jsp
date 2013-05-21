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

<portlet:defineObjects />

<%
String backURL = (String)request.getAttribute("liferay-ui:form-navigator:backURL");
String[][] categorySections = (String[][])request.getAttribute("liferay-ui:form-navigator:categorySections");
String[] categoryNames = (String[])request.getAttribute("liferay-ui:form-navigator:categoryNames");
String formName = GetterUtil.getString((String)request.getAttribute("liferay-ui:form-navigator:formName"));
String htmlBottom = (String)request.getAttribute("liferay-ui:form-navigator:htmlBottom");
String htmlTop = (String)request.getAttribute("liferay-ui:form-navigator:htmlTop");
String jspPath = (String)request.getAttribute("liferay-ui:form-navigator:jspPath");
boolean showButtons = GetterUtil.getBoolean((String)request.getAttribute("liferay-ui:form-navigator:showButtons"));

if (Validator.isNull(backURL)) {
	String redirect = ParamUtil.getString(request, "redirect");

	backURL = redirect;
}

if (Validator.isNull(backURL)) {
	PortletURL portletURL = liferayPortletResponse.createRenderURL();

	backURL = portletURL.toString();
}

String[] allSections = new String[0];

for (String[] categorySection : categorySections) {
	allSections = ArrayUtil.append(allSections, categorySection);
}

String curSection = StringPool.BLANK;

if (categorySections[0].length > 0) {
	curSection = categorySections[0][0];
}

String historyKey = ParamUtil.getString(request, "historyKey");

if (Validator.isNotNull(historyKey)) {
	curSection = historyKey;
}
%>

<div class="taglib-form-navigator">
	<aui:input name="modifiedSections" type="hidden" />

	<div class="taglib-form-navigator row-fluid" id="<portlet:namespace />tabs">
		<div class="span8">

			<%
			for (String section : allSections) {
				String sectionId = namespace + _getSectionId(section);
				String sectionJsp = jspPath + _getSectionJsp(section) + ".jsp";
			%>

				<!-- Begin fragment <%= sectionId %> -->

				<div class="form-section <%= (curSection.equals(section) || curSection.equals(sectionId)) ? "active" : "hide" %>" id="<%= sectionId %>">
					<liferay-util:include page="<%= sectionJsp %>" portletId="<%= portletDisplay.getRootPortletId() %>" />
				</div>

				<!-- End fragment <%= sectionId %> -->

			<%
			}
			%>

		</div>

		<ul class="nav nav-list span4 well form-navigator">
			<%= Validator.isNotNull(htmlTop) ? htmlTop : StringPool.BLANK %>

			<%
			String[] modifiedSections = StringUtil.split(ParamUtil.getString(request, "modifiedSections"));

			String errorSection = (String)request.getAttribute("errorSection");

			if (Validator.isNull(errorSection)) {
				modifiedSections = null;
			}

			for (int i = 0; i < categoryNames.length; i++) {
				String category = categoryNames[i];
				String[] sections = categorySections[i];

				if (sections.length > 0) {
			%>

					<c:if test="<%= Validator.isNotNull(category) %>">
						<h1 class="nav-header"><liferay-ui:message key="<%= category %>" /></h1>
					</c:if>

					<%
					if (Validator.isNotNull(errorSection)) {
						curSection = StringPool.BLANK;
					}

					for (String section : sections) {
						String sectionId = namespace + _getSectionId(section);

						Boolean show = (Boolean)request.getAttribute(WebKeys.FORM_NAVIGATOR_SECTION_SHOW + sectionId);

						if ((show != null) && !show.booleanValue()) {
							continue;
						}

						String cssClass = StringPool.BLANK;

						if (StringUtil.endsWith(sectionId, errorSection)) {
							cssClass += "section-error";

							curSection = section;
						}

						if (curSection.equals(section) || curSection.equals(sectionId)) {
							cssClass += " active";
						}

						if (ArrayUtil.contains(modifiedSections, sectionId)) {
							cssClass += " section-modified";
						}
					%>

						<li class="<%= cssClass %>" data-sectionId="<%= sectionId %>" id="<%= sectionId %>Tab">
							<a href="#<%= sectionId %>" id="<%= sectionId %>Link">

							<liferay-ui:message key="<%= section %>" />

							<span class="modified-notice"> (<liferay-ui:message key="modified" />) </span>

							</a>
						</li>

					<%
					}
					%>

			<%
				}
			}
			%>

			<c:if test="<%= showButtons %>">
				<aui:button-row>
					<aui:button cssClass="btn-primary" type="submit" />

					<aui:button href="<%= backURL %>" type="cancel" />
				</aui:button-row>
			</c:if>

			<%= Validator.isNotNull(htmlBottom) ? htmlBottom : StringPool.BLANK %>
		</ul>
	</div>
</div>

<aui:script use="aui-event-input,aui-tabview,aui-url,io-form,liferay-history-manager">
	var formNode = A.one('#<portlet:namespace /><%= formName %>');

	var HistoryManager = Liferay.HistoryManager;

	var tabview = new A.TabView(
		{
			srcNode: '#<portlet:namespace />tabs',
			type: 'list'
		}
	).render();

	function selectTabBySectionId(sectionId) {
		var instance = this;

		var tab = A.Widget.getByNode('#' + sectionId + 'Tab');

		var tabIndex = tabview.indexOf(tab);

		if (tab && (tabIndex > -1)) {
			tabview.selectChild(tabIndex);

			Liferay.fire('formNavigator:reveal' + sectionId);
		}
	};

	function updateSectionStatus() {
		var tabNode = tabview.get('selection').get('boundingBox');

		var sectionId = tabNode.getData('sectionId');

		var modifiedSectionsNode = A.one('#<portlet:namespace/>modifiedSections');

		var modifiedSections = modifiedSectionsNode.val().split(',');

		modifiedSections.push(sectionId);
		modifiedSections = A.Array.dedupe(modifiedSections);
		modifiedSectionsNode.val(modifiedSections.join());

		tabNode.addClass('section-modified');

		tabNode.toggleClass(
			'section-error',
			A.one('#' + sectionId).one('.error-field')
		);
	}

	function updateRedirectForSectionId(sectionId) {
		var redirect = A.one('#<portlet:namespace />redirect');

		if (redirect) {
			var url = new A.Url(redirect.val() || location.href);

			url.setAnchor(null);
			url.setParameter('<portlet:namespace />historyKey', sectionId);

			redirect.val(url.toString());
		}
	}

	tabview.after(
		'selectionChange',
		function(event) {
			var tab = event.newVal;

			var boundingBox = tab.get('boundingBox');

			var sectionId = boundingBox.getData('sectionId');

			Liferay.fire('formNavigator:reveal' + sectionId);

			HistoryManager.add(
				{
					'<portlet:namespace />tab': sectionId
				}
			);

			updateRedirectForSectionId(sectionId);
		}
	);

	HistoryManager.on(
		'stateChange',
		function(event) {
			var newState = event.newVal;

			var state = newState['<portlet:namespace />tab'] || newState['<portlet:namespace />historyKey'];

			if (state) {
				selectTabBySectionId(state);
			}
			else {
				tabview.selectChild(0);
			}
		}
	);

	if (formNode) {
		formNode.all('.modify-link').on('click', updateSectionStatus);

		formNode.delegate('change', updateSectionStatus, 'input, select, textarea');
	}

	var locationSectionId = HistoryManager.get('<portlet:namespace />historyKey');

	if (!locationSectionId) {
		locationSectionId = HistoryManager.get('<portlet:namespace />tab');
	}

	selectTabBySectionId(locationSectionId);
</aui:script>

<%!
private String _getSectionId(String name) {
	return TextFormatter.format(name, TextFormatter.M);
}

private String _getSectionJsp(String name) {
	return TextFormatter.format(name, TextFormatter.N);
}
%>