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

					_getDeviceDialog: function(device) {
						var instance = this;

						var width = device.width;
						var height = device.height;

						var autoHeight = false;
						var autoWidth = false;

						if (!Lang.isNumber(width)) {
							var widthNode = A.one(width);

							if (widthNode) {
								width = widthNode.val();
							} else {
								autoWidth = true;
							}
						}

						if (!Lang.isNumber(height)) {
							var heightNode = A.one(height);

							if (heightNode) {
								height = heightNode.val();
							} else {
								autoHeight = true;
							}
						}

						var dialog = Liferay.Util.getWindow("123abcdef456");

						if (!dialog) {

							Liferay.Util.openWindow(
								{
									dialog: {
										on: {
											visibleChange: function(event) {
												// WON'T WORK
											}
										},
										//align: { // Use centered instead
										//	node: instance._devicePreviewNode,
										//	points: [A.WidgetPositionAlign.CC, A.WidgetPositionAlign.CC]
										//},
										centered: true,
										constrain: instance._devicePreviewNode,
										draggable: false,
										height: height, // Needed to set the proper modal height
										modal: false,
										render: instance._devicePreviewNode,
										resizable: false
										,width: width // Needed to set the proper modal width
									},
									height: height, // Needed to avoid auto-height
									id: "123abcdef456",
									title: device.title,
									uri: window.location.href,
									width: width // Needed to avoid auto-width
								},
								function(dialogWindow) {
									dialogWindow.on('visibleChange', 
										function(event) {
											if (!event.newVal) {
												instance._devicePreviewNode.hide();
											}
										}
									);
								}
							);

						} else {					
							dialog.set('width', width);
							dialog.set('height', height);

							dialog.set('autoWidth', autoWidth);
							dialog.set('autoHeight', autoHeight);

							if (device.title) {
								dialog.titleNode.html(device.title);
							}

							dialog.align(
								instance._devicePreviewNode,
								[A.WidgetPositionAlign.CC, A.WidgetPositionAlign.CC]
							)

							dialog.show();
						}
					},

					_onDeviceClick: function(event) {
						var instance = this;

						var deviceList = instance.get('devices');

						var deviceId = event.currentTarget.getData('device');

						var selectedDevice = deviceList[deviceId];

						instance._heightRatio = null;
						instance._widthRatio = null;

						if (selectedDevice) {
							instance._getDeviceDialog(selectedDevice);

							instance._devicePreviewContainer.all('.selected').toggleClass('selected');
							event.currentTarget.addClass('selected');

							instance._devicePreviewNode.show();
						}
					},

					_onSizeInput: function(event) {
						var instance = this;

						var width = A.one('.device-width').val();
						var height = A.one('.device-height').val();

						var dialog = instance._getDeviceDialog({width: width, height: height});
					}
				}
			}
		);

		Dockbar.DevicePreview = DevicePreview;
	},
	'',
	{
		requires: ['aui-event-input', 'aui-modal', 'aui-dialog-iframe-deprecated', 'liferay-portlet-base', 'liferay-util', 'liferay-util-window']
	}
);