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

<%@ include file="/html/portlet/dockbar/init.jsp" %>

<%
int delta = ParamUtil.getInteger(request, "delta", 10);

String keywords = ParamUtil.getString(request, "keywords");

String displayStyle = ParamUtil.getString(request, "displayStyle", "list");
%>

<liferay-ui:panel collapsible="<%= false %>" cssClass="lfr-component panel-page-category recent" extended="<%= true %>" id="panel-manage-recent" persistState="<%= true %>" title="recent">

	<%
	long[] groupIds = new long[]{scopeGroupId};

	long[] availableClassNameIds = AssetRendererFactoryRegistryUtil.getClassNameIds();

	for (long classNameId : availableClassNameIds) {
		AssetRendererFactory assetRendererFactory = AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(PortalUtil.getClassName(classNameId));

		if (!assetRendererFactory.isSelectable()) {
			availableClassNameIds = ArrayUtil.remove(availableClassNameIds, classNameId);
		}
	}

	AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

	assetEntryQuery.setClassNameIds(availableClassNameIds);

	assetEntryQuery.setKeywords(keywords);

	int total = AssetEntryServiceUtil.getEntriesCount(assetEntryQuery);

	assetEntryQuery.setOrderByCol1("modifiedDate");
	assetEntryQuery.setOrderByCol2("title");
	assetEntryQuery.setOrderByType1("DESC");
	assetEntryQuery.setOrderByType2("ASC");

	assetEntryQuery.setEnd(delta);
	assetEntryQuery.setStart(0);

	List<AssetEntry> results = AssetEntryServiceUtil.getEntries(assetEntryQuery);
	%>

	<ul>

		<%
		int j = 0;

		for (AssetEntry assetEntry : results) {
			String className = PortalUtil.getClassName(assetEntry.getClassNameId());
			long classPK = assetEntry.getClassPK();

			AssetRendererFactory assetRendererFactory = AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(className);

			if (assetRendererFactory == null) {
				continue;
			}

			AssetRenderer assetRenderer = null;

			try {
				assetRenderer = assetRendererFactory.getAssetRenderer(classPK);
			}
			catch (Exception e) {
			}

			if ((assetRenderer == null) || !assetRenderer.isDisplayable()) {
				continue;
			}

			Map<String, Object> data = new HashMap<String, Object>();

			data.put("class-name", assetEntry.getClassName());
			data.put("class-pk", assetEntry.getClassPK());
			data.put("portlet-id", PortletKeys.ASSET_PUBLISHER);
		%>

			<c:choose>
				<c:when test='<%= !displayStyle.equals("list") %>'>
					<liferay-ui:app-view-entry
						cssClass="content-shortcut"
						data="<%= data %>"
						description="<%= assetRenderer.getSummary(locale) %>"
						displayStyle="<%= displayStyle %>"
						showCheckbox="<%= false %>"
						thumbnailSrc="<%= assetRenderer.getThumbnailPath(liferayPortletRequest) %>"
						title="<%= HtmlUtil.escape(assetRenderer.getTitle(locale)) %>"
						url="javascript:;"
					/>
				</c:when>
				<c:otherwise>
					<li class="<%= (j == 0) ? "first" : "" %>">
						<a class="content-shortcut" <%= AUIUtil.buildData(data) %> href="javascript:;">
							<liferay-ui:icon
								label="<%= true %>"
								message="<%= HtmlUtil.escape(assetRenderer.getTitle(locale)) %>"
								src="<%= assetRenderer.getIconPath(liferayPortletRequest) %>"
							/>
						</a>
					</li>
				</c:otherwise>
			</c:choose>

		<%
			j++;
		}
		%>

	</ul>
</liferay-ui:panel>