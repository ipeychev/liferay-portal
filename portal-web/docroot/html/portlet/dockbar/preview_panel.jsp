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

<%@ include file="/html/portlet/dockbar/init.jsp" %>

<div id="<portlet:namespace />devicePreviewContainer">
	<button class="close pull-right" id="closePanel" type="button">&#x00D7;</button>

	<h1>Preview</h1>

	<div class="device-preview-content">
		<aui:nav cssClass="nav-list">
			<aui:nav-item cssClass="lfr-device-item row-fluid" data-device="autosize">
				<div class="span4 full"></div>
				<div class="span8">
					<div>AutoSize</div>
					<div>100%</div>
				</div>
			</aui:nav-item>

			<aui:nav-item cssClass="lfr-device-item row-fluid" data-device="smartphone">
				<div class="span4 smartphone"></div>
				<div class="span8">
					<div>Smartphone</div>
					<div>768px</div>
				</div>
			</aui:nav-item>

			<aui:nav-item cssClass="lfr-device-item row-fluid" data-device="tablet">
				<div class="span4 tablet"></div>
				<div class="span8">
					<div>Tablet</div>
					<div>1024px</div>
				</div>
			</aui:nav-item>

			<aui:nav-item cssClass="lfr-device-item row-fluid" data-device="desktop">
				<div class="span4 desktop"></div>
				<div class="span8">
					<div>Desktop</div>
					<div>1280px</div>
				</div>
			</aui:nav-item>

			<aui:nav-item cssClass="lfr-device-item row-fluid" data-device="custom">
				<div>Custom (px)</div>
				<input class="input-mini device-width" name="width" value="200"/> x <input class="input-mini device-height" name="height" value="200" />
			</aui:nav-item>
		</aui:nav>
	</div>
</div>

<aui:script use="liferay-dockbar-device-preview">
	new Liferay.Dockbar.DevicePreview(
		{
			namespace: '<portlet:namespace />',
			devices: {
				'autosize': {
					title: 'autosize',
					height: 'auto',
					width: 'auto'
				},
				'smartphone':
				{
					title: 'smartphone',
					height: 640,
					width: 400
				},
				'tablet':
				{
					title: 'tablet',
					height: 900,
					width: 760
				},
				'desktop':
				{
					title: 'desktop',
					height: 1050,
					width: 1300
				},
				'custom':
				{
					title: 'custom',
					height: '.device-height',
					width: '.device-width'
				}
			},
			inputHeight: '.device-height',
			inputWidth: '.device-width'
		}
	);
</aui:script>