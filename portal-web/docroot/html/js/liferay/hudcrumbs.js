AUI.add(
	'liferay-hudcrumbs',
	function(A) {
		var Lang = A.Lang;

		var NAME = 'hudcrumbs';

		var DOCKBAR_PINNED = 'dockbar:pinned';

		var STR_SCROLL = 'scroll';

		var SURFACE_START_NAVIGATE = 'surfaceStartNavigate';

		var WINDOW_RESIZE = 'windowresize';

		var getClassName = A.ClassNameManager.getClassName;

		var Hudcrumbs = A.Component.create(
			{
				ATTRS: {
					clone: {
						value: null
					},
					hostMidpoint: {
						validtor: Lang.isNumber,
						value: 0
					},
					scrollDelay: {
						validator: Lang.isNumber,
						value: 100
					},
					width: {
						validtor: Lang.isNumber,
						value: 0
					}
				},

				EXTENDS: A.Plugin.Base,

				NAME: NAME,

				NS: NAME,

				prototype: {
					initializer: function() {
						var instance = this;

						var breadcrumbs = instance.get('host');
						var hudcrumbs = breadcrumbs.clone();
						var region = breadcrumbs.get('region');

						hudcrumbs.resetId();

						var win = A.getWin();
						var body = A.getBody();

						instance._win = win;
						instance._body = body;
						instance._dockbar = Liferay.Dockbar && Liferay.Dockbar.dockBar;

						hudcrumbs.hide();

						hudcrumbs.addClass('lfr-hudcrumbs');

						instance.set('clone', hudcrumbs);

						instance._calculateDimensions();

						instance._debouncedScrollFn = A.debounce(instance._onScroll, instance.get('scrollDelay'), instance);

						win.on(STR_SCROLL, instance._debouncedScrollFn);
						win.on(WINDOW_RESIZE, instance._calculateDimensions, instance);

						body.append(hudcrumbs);

						Liferay.on(DOCKBAR_PINNED, instance._calculateDimensions, instance);

						Liferay.on(SURFACE_START_NAVIGATE, instance._onStartNavigate, instance);
					},

					destructor: function() {
						var instance = this;

						Liferay.detach(SURFACE_START_NAVIGATE, instance._onStartNavigate);

						Liferay.detach(DOCKBAR_PINNED, instance._calculateDimensions);

						var win = instance._win;

						win.detach(STR_SCROLL, instance._debouncedScrollFn);
						win.detach(WINDOW_RESIZE, instance._calculateDimensions);
					},

					_calculateDimensions: function(event) {
						var instance = this;

						var region = instance.get('host').get('region');

						instance.get('clone').setStyles(
							{
								left: region.left + 'px',
								width: region.width + 'px'
							}
						);

						instance.set('hostMidpoint', region.top + (region.height / 2));
					},

					_onScroll: function(event) {
						var instance = this;

						var scrollTop = event.currentTarget.get('scrollTop');
						var hudcrumbs = instance.get('clone');

						var action = 'hide';

						if (scrollTop >= instance.get('hostMidpoint')) {
							action = 'show';
						}

						if (instance.lastAction != action) {
							hudcrumbs[action]();
						}

						instance.lastAction = action;
					},

					_onStartNavigate: function(event) {
						var instance = this;

						instance.get('clone').hide();
					}
				}
			}
		);

		A.Hudcrumbs = Hudcrumbs;
	},
	'',
	{
		requires: ['aui-base', 'aui-debounce', 'event-resize']
	}
);