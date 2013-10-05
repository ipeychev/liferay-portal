AUI.add(
	'liferay-dockbar-keyboard-interaction',
	function(A) {
		var ACTIVE_DESCENDANT = 'activeDescendant';

		var CSS_DROPDOWN = 'dropdown';

		var EVENT_KEY = 'key';

		var NAME = 'liferaydockbarkeyboardinteraction';

		var OPEN = 'open';

		var KEY_PRESS_DOWN_ARROW = 'down:40';

		var KEY_PRESS_LEFT_ARROW = 'down:37';

		var KEY_PRESS_RIGHT_ARROW = 'down:39';

		var KEY_PRESS_TAB = 'down:9';

		var KEY_PRESS_UP_ARROW = 'down:38';

		var SELECTOR_A = 'a';

		var SELECTOR_LI_PARENT_MENU_ITEM = 'li.lfr-parent-menu-item';

		var SELECTOR_LFR_PARENT_MENU_ITEM = '.lfr-parent-menu-item';

		var DockbarKeyboardInteraction = A.Component.create(
			{
				EXTENDS: A.Plugin.Base,

				NAME: NAME,

				NS: NAME,

				prototype: {
					initializer: function() {
						var instance = this;

						instance._host = instance.get('host');

						instance._initHostFocusManager();

						instance._initParentMenuItemHandlers();
					},

					_handleDownKeyPress: function(event) {
						var instance = this;

						var currentTarget = event.currentTarget;

						var host = instance._host;

						var liParentMenuItem = currentTarget.ancestor(SELECTOR_LI_PARENT_MENU_ITEM);

						event.preventDefault();

						host.all(SELECTOR_LI_PARENT_MENU_ITEM).removeClass(OPEN);

						if (liParentMenuItem.hasClass(CSS_DROPDOWN)) {
							liParentMenuItem.addClass(OPEN);
						}
					},

					_handleLeftKeyPress: function(event) {
						var instance = this;

						var currentTarget = event.currentTarget;

						var currentParentMenuItem = currentTarget.ancestor(SELECTOR_LFR_PARENT_MENU_ITEM);

						var host = instance._host;

						var hostFocusManager = instance.hostFocusManager;

						var parentMenuItems = host.all(SELECTOR_LFR_PARENT_MENU_ITEM);

						var parentMenuItemsPosition = parentMenuItems.indexOf(currentParentMenuItem);

						var parentMenuItemsSize = parentMenuItems.size()-1;

						host.all(SELECTOR_LI_PARENT_MENU_ITEM).removeClass(OPEN);

						if (parentMenuItemsPosition <= 0) {
							hostFocusManager.focus(parentMenuItems.item(parentMenuItemsSize).one(SELECTOR_A));
						}
						else {
							hostFocusManager.focus(parentMenuItems.item(parentMenuItemsPosition - 1).one(SELECTOR_A));
						}
					},

					_handleRightKeyPress: function(event) {
						var instance = this;

						var currentTarget = event.currentTarget;

						var currentParentMenuItem = currentTarget.ancestor(SELECTOR_LFR_PARENT_MENU_ITEM);

						var host = instance._host;

						var hostFocusManager = instance.hostFocusManager;

						var parentMenuItems = host.all(SELECTOR_LFR_PARENT_MENU_ITEM);

						var parentMenuItemsPosition = parentMenuItems.indexOf(currentParentMenuItem);

						var parentMenuItemsSize = parentMenuItems.size()-1;

						host.all(SELECTOR_LI_PARENT_MENU_ITEM).removeClass(OPEN);

						if (parentMenuItemsPosition >= parentMenuItemsSize) {
							hostFocusManager.focus(parentMenuItems.item(0).one(SELECTOR_A));
						}
						else {
							hostFocusManager.focus(parentMenuItems.item(parentMenuItemsPosition + 1).one(SELECTOR_A));
						}
					},

					_handleTabKeyPress: function(event) {
						var currentTarget = event.currentTarget;

						currentTarget.all(SELECTOR_LI_PARENT_MENU_ITEM).removeClass(OPEN);
					},

					_handleUpKeyPress: function(event) {
						var instance = this;

						var host = instance._host;

						var hostFocusManager = instance.hostFocusManager;

						var descendantsMap = hostFocusManager._descendantsMap;

						var focusedCurrent = hostFocusManager.get(ACTIVE_DESCENDANT) - 1;

						event.preventDefault();

						host.all(SELECTOR_LI_PARENT_MENU_ITEM).removeClass(OPEN);

						if (focusedCurrent < 0) {
							focusedCurrent = hostFocusManager._lastNodeIndex;
						}

						for (var descendant in descendantsMap) {
							if (descendantsMap[descendant] === focusedCurrent) {
								var liParentMenuItem = A.one('#' + descendant).ancestor(SELECTOR_LFR_PARENT_MENU_ITEM);

								if (liParentMenuItem.hasClass(CSS_DROPDOWN)) {
									A.one('#' + descendant).ancestor(SELECTOR_LFR_PARENT_MENU_ITEM).addClass(OPEN);
								}
							}
						}
					},

					_initHostFocusManager: function() {
						var instance = this;

						var host = instance._host;

						host.plug(
							A.Plugin.NodeFocusManager,
							{
								descendants: 'li a',
								keys: {
									next: KEY_PRESS_DOWN_ARROW,
									previous: KEY_PRESS_UP_ARROW
								}
							}
						);

						host.focusManager.after(
							'focusedChange',
							function (event) {
								if (!event.newVal) {
									this.set(ACTIVE_DESCENDANT, 0);
								}
							}
						);

						instance.hostFocusManager = host.focusManager;
					},

					_initParentMenuItemHandlers: function() {
						var instance = this;

						var host = instance._host;

						host.delegate(EVENT_KEY, instance._handleDownKeyPress, KEY_PRESS_DOWN_ARROW, 'li.lfr-parent-menu-item > a', instance);

						host.delegate(EVENT_KEY, instance._handleLeftKeyPress, KEY_PRESS_LEFT_ARROW, 'li a', instance);

						host.delegate(EVENT_KEY, instance._handleRightKeyPress, KEY_PRESS_RIGHT_ARROW, 'li a', instance);

						host.delegate(EVENT_KEY, instance._handleTabKeyPress, KEY_PRESS_TAB);

						host.delegate(EVENT_KEY, instance._handleUpKeyPress, KEY_PRESS_UP_ARROW, 'li.lfr-parent-menu-item > a', instance);
					}
				}
			}
		);

		Liferay.DockbarKeyboardInteraction = DockbarKeyboardInteraction;
	},
	'',
	{
		requires: ['node-focusmanager', 'plugin']
	}
);