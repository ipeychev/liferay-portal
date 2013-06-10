AUI.add(
	'liferay-dockbar-device-preview',
	function(A) {
		var Lang = A.Lang;

		var BODY = A.getBody();

		var Dockbar = Liferay.Dockbar;

		var CSS_CONTENT_ITEM = '.lfr-device-item';

		var CSS_DEVICE_PREVIEW_CONTENT = '.device-preview-content';

		var STR_CLICK = 'click';

		var STR_CUSTOM_DEVICE = 'custom-device';

		var TPL_DEVICE_PREVIEW = '<div class="lfr-device-preview" />';

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

						if (instance._devicePreviewNode) {
							instance._devicePreviewNode.hide();
						}

						Dockbar.loadDevicePreviewPanel();
					},

					_getDeviceDialog: function(width, height) {
						var instance = this;

						var dialog = instance._dialog;

						if (!dialog) {
							var devicePreviewNode = A.Node.create(Lang.sub(TPL_DEVICE_PREVIEW));
							BODY.append(devicePreviewNode);

							dialog = new A.Modal(
								{
									align: {
										node: devicePreviewNode,
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
												dialog.set('height', height + delta);
											});

											dialog.iframe.on('uriChange', function(event) {
												//dialog.set('headerContent', event.newVal);
											});
										}
									},
									constrain: devicePreviewNode,
									draggable: false,
									headerContent: window.location.pathname,
									resizable: false
								}
							);

							dialog.plug(
								A.Plugin.DialogIframe, 
								{
									uri: window.location.href
								}
							);

							dialog.render(devicePreviewNode);

							instance._devicePreviewNode = devicePreviewNode;
							instance._dialog = dialog;

						} else {
							dialog.set('width', width);
							dialog.set('height', height);

							dialog.align(
								instance._devicePreviewNode,
								[A.WidgetPositionAlign.CC, A.WidgetPositionAlign.CC]
							)
						}

						return instance._dialog;
					},

					_onDeviceClick: function(event) {
						var instance = this;

						var deviceList = instance.get('devices');

						var deviceId = event.currentTarget.getData('device');

						var device = deviceList[deviceId];

						if (device) {
							var dialog = instance._getDeviceDialog(device.width, device.height);

							instance._devicePreviewContainer.all('.selected').toggleClass('selected');
							event.currentTarget.addClass('selected');

							if (event.currentTarget.hasClass(STR_CUSTOM_DEVICE)) {
								var width = event.currentTarget.one('.device-width').val();
								var height = event.currentTarget.one('.device-height').val();

								instance._changeDialogSize(width, height);							
							}

							instance._devicePreviewNode.show();
							dialog.show();
						}
					},

					_onSizeInput: function(event) {
						var instance = this;

						var custom = A.one('.' + STR_CUSTOM_DEVICE);

						width = custom.one('.device-width').val();
						height = custom.one('.device-height').val();

						instance._changeDialogSize(width, height);
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