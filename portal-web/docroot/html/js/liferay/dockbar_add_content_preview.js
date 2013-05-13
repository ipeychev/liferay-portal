AUI.add(
	'liferay-dockbar-add-content-preview',
	function(A) {
		var Dockbar = Liferay.Dockbar;
		var Lang = A.Lang;

		var BODY_CONTENT = 'bodyContent';

		var CSS_OVER = 'over';

		var STR_ALIGN_NODE = 'align.node';

		var STR_BOUNDING_BOX = 'boundingBox';

		var STR_CURRENT_NODE = 'currentNode';

		var TPL_MESSAGE_ERROR = '<div class="portlet-msg-error">{0}</div>';

		var TPL_LOADING = '<div class="loading-animation" />';

		var AddContentPreview = function() {
		};

		AddContentPreview.prototype = {
			initializer: function(config) {
				var instance = this;

				instance._eventHandles = [];

				instance._loadPreviewTask = A.debounce('_loadPreviewFn', 200, instance);

				instance._bindUIACPreview();
			},

			destructor: function() {
				var instance = this;

				new A.EventHandle(instance._eventHandles).detach();
			},

			_afterPreviewFailure: function(event) {
				var instance = this;

				var errorMsg = Lang.sub(
					TPL_MESSAGE_ERROR,
					[
						Liferay.Language.get('unable-to-load-content')
					]
				);

				instance._tooltip.set(BODY_CONTENT, errorMsg);
			},

			_afterPreviewSuccess: function(event) {
				var instance = this;

				var tooltip = instance._tooltip;

				tooltip.set(BODY_CONTENT, event.currentTarget.get('responseData'));
				tooltip.align();

				instance._eventHandles.push(
					tooltip.get(STR_BOUNDING_BOX).one('.add-button-preview').on('click', instance._addApplication, instance)
				);
			},

			_bindUIACPreview: function() {
				var instance = this;

				Liferay.Dockbar.getPanelNode().delegate(
					'mouseenter',
					instance._showTooltip,
					'.has-preview',
					instance
				);
			},

			_getIOPreview: function() {
				var instance = this;

				var ioPreview = instance._ioPreview;

				if (!ioPreview) {
					ioPreview = A.io.request(
						instance._addContentForm.getAttribute('action'),
						{
							after: {
								failure: A.bind('_afterPreviewFailure', instance),
								success: A.bind('_afterPreviewSuccess', instance)
							},
							autoLoad: false,
							data: {
								viewEntries: false,
								viewPreview: true
							}
						}
					);

					instance._ioPreview = ioPreview;
				}

				return ioPreview;
			},

			_getTooltip: function() {
				var instance = this;

				var tooltip = instance._tooltip;

				if (!tooltip) {
					tooltip = new A.Popover(
						{
							align: {
								points: [A.WidgetPositionAlign.LC, A.WidgetPositionAlign.RC]
							},
							constrain: true,
							position: 'right',
							visible: false,
							zIndex: 500
						}
					);

					tooltip.get(STR_BOUNDING_BOX).addClass('lfr-add-content-preview');

					tooltip.render();

					instance._eventHandles.push(
						tooltip.get(STR_BOUNDING_BOX).on('clickoutside', tooltip.hide, tooltip)
					);

					instance._tooltip = tooltip;
				}

				return tooltip;
			},

			_loadPreviewFn: function(className, classPK) {
				var instance = this;

				var ioPreview = instance._getIOPreview();

				ioPreview.stop();

				ioPreview.set('data.classPK', classPK);
				ioPreview.set('data.className', className);

				ioPreview.start();
			},

			_showTooltip: function(event) {
				var instance = this;

				var currentNode = event.currentTarget;

				var tooltip = instance._getTooltip();

				tooltip.set(BODY_CONTENT, TPL_LOADING);
				tooltip.set(STR_ALIGN_NODE, currentNode);

				tooltip.show();

				instance._loadPreviewTask(currentNode.attr('data-class-name'), currentNode.attr('data-class-pk'));
			}
		};

		Dockbar.AddContentPreview = AddContentPreview;
	},
	'',
	{
		requires: ['aui-io-request', 'aui-popover', 'event-mouseenter']
	}
);