AUI.add(
	'liferay-dockbar-device-preview',
	function(A) {
		var Lang = A.Lang;

		var BODY = A.getBody();

		var Dockbar = Liferay.Dockbar;

		var CSS_CONTENT_ITEM = '.lfr-device-item';

		var CSS_DEVICE_PREVIEW_CONTENT = '.device-preview-content';

		var STR_CLICK = 'click';

		var TPL_DEVICE_PREVIEW = '<div class="lfr-device-preview hide" />';

		var DevicePreview = A.Component.create(
			{
				AUGMENTS: [Liferay.PortletBase],

				EXTENDS: A.Base,

				NAME: 'devicepreview',

				ATTRS: {
					devices: {
						validator: Lang.isObject
					},
					inputHeight: {
						setter: A.one
					},
					inputWidth: {
						setter: A.one
					}
				},

				prototype: {
					initializer: function(config) {
						var instance = this;

						instance._devicePreviewContainer = instance.byId('devicePreviewContainer');
						instance._devicePreviewContent = instance._devicePreviewContainer.one(CSS_DEVICE_PREVIEW_CONTENT);
						instance._closePanelButton = instance._devicePreviewContainer.one('#closePanel');

						instance._devicePreviewNode = A.Node.create(Lang.sub(TPL_DEVICE_PREVIEW));
						BODY.append(instance._devicePreviewNode);

						instance._bindUI();
					},

					_bindUI: function() {
						var instance = this;

						instance._closePanelButton.on(STR_CLICK, instance._closePanel, instance);

						instance._devicePreviewContent.delegate('click', instance._onDeviceClick, CSS_CONTENT_ITEM, instance);
						instance._devicePreviewContent.delegate('input', instance._onSizeInput, 'input', instance);
					},

					_closePanel: function() {
						var instance = this;
						
						Dockbar.togglePreviewPanel();

						instance._devicePreviewNode.hide();
					},

					_getDeviceDialog: function(width, height) {
						var instance = this;

						var dialog = instance._dialog;

						if (width >= 0 && width <= 1) {
							instance._widthRatio = width;
							width = instance._devicePreviewNode.get('offsetWidth') * instance._widthRatio;
						}

						if (height >= 0 && height <= 1) {
							instance._heightRatio = height;
							height = instance._devicePreviewNode.get('offsetHeight') * instance._heightRatio;							
						}	

						if (!dialog) {
							dialog = new A.Modal(
								{
									align: {
										node: instance._devicePreviewNode,
										points: [A.WidgetPositionAlign.CC, A.WidgetPositionAlign.CC]
									},
									animate: true,
									after: {
										visibleChange: function(event) {
											instance._devicePreviewNode.hide();
										},
										render: function(event) {
											dialog.iframe.on('load', function(event) {
												var boundingBox = dialog.get('boundingBox');
												var body = boundingBox.one('.dialog-iframe-bd');

												var delta = A.Lang.toInt(body.getStyle('paddingLeft')) + A.Lang.toInt(body.getStyle('paddingRight'));
												dialog.set('width', width + delta);
											});
										}
									},
									constrain: instance._devicePreviewNode,
									draggable: false,
									headerContent: window.location.pathname,
									height: height,
									resizable: false,
									width: width
								}
							);

							dialog.plug(
								A.Plugin.DialogIframe, 
								{
									uri: window.location.href
								}
							);

							dialog.render(instance._devicePreviewNode);

							instance._dialog = dialog;

						} else {					
							dialog.set('width', width);
							dialog.set('height', height);

							dialog.align(
								instance._devicePreviewNode,
								[A.WidgetPositionAlign.CC, A.WidgetPositionAlign.CC]
							)
						}

						if (instance._widthRatio || instance._heightRatio) {
							instance._onresizeHandle = A.on('windowresize', function(event) 
								{
									var w = instance._devicePreviewNode.get('offsetWidth') * instance._widthRatio;
									var h = instance._devicePreviewNode.get('offsetHeight') * instance._heightRatio;
									dialog.set('width', w);
									dialog.set('height', h);
								}
							);
						} else {
							if (instance._onresizeHandle) {
								instance._onresizeHandle.detach();
							}
						}

						return instance._dialog;
					},

					_onDeviceClick: function(event) {
						var instance = this;

						var deviceList = instance.get('devices');

						var deviceId = event.currentTarget.getData('device');

						var selectedDevice = deviceList[deviceId];

						instance._heightRatio = null;
						instance._widthRatio = null;

						if (selectedDevice) {
							var width = selectedDevice.width;
							var height = selectedDevice.height;

							if (!Lang.isNumber(width)) {
								var widthNode = A.one(width);

								if (widthNode) {
									width = widthNode.val();
								}
							}

							if (!Lang.isNumber(height)) {
								var heightNode = A.one(height);

								if (heightNode) {
									height = heightNode.val();
								}								
							}

							var dialog = instance._getDeviceDialog(width, height);

							instance._devicePreviewContainer.all('.selected').toggleClass('selected');
							event.currentTarget.addClass('selected');

							instance._devicePreviewNode.show();
							dialog.show();
						}
					},

					_onSizeInput: function(event) {
						var instance = this;

						var width = A.one('.device-width').val();
						var height = A.one('.device-height').val();

						var dialog = instance._getDeviceDialog(width, height);
					}
				}
			}
		);

		Dockbar.DevicePreview = DevicePreview;
	},
	'',
	{
		requires: ['aui-event-input', 'aui-modal', 'aui-dialog-iframe-deprecated', 'liferay-portlet-base']
	}
);