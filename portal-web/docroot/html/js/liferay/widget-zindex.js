AUI.add(
	'liferay-widget-zidex',
	function(A) {
		var Lang = A.Lang;

		var NAME = 'widgetzindex';

		var NS = 'zindex';

		var WidgedZIndex = A.Component.create(
			{
				ATTRS: {
				},

				EXTENDS: A.Plugin.Base,

				NAME: NAME,

				NS: NS,

				prototype: {
					initializer: function() {
						var instance = this;

						
					}
				}
			}
		);

		Liferay.WidgetZIndex = WidgetZIndex;
	},
	'',
	{
		requires: ['aui-modal', 'plugin']
	}
);