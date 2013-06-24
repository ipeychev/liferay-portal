AUI.add(
	'liferay-key-filtered-toggler-delegate',
	function(A) {
		var AArray = A.Array;

		var CLICK = 'click';

		var ESC = 'esc';

		var LEFT = 'left';

		var NUM_MINUS = 'num_minus';

		var NUM_PLUS = 'num_plus';

		var RIGHT = 'right';

		var SPACE = 'space';

		var KeyFilteredTogglerDelegate = A.Component.create(
			{
				EXTENDS: A.TogglerDelegate,

				NAME: 'keyfilteredtogglerdelegate',

				ATTRS: {
					filter: {
						validator: A.isArray,
						value: [
							ESC,
							LEFT,
							NUM_MINUS,
							NUM_PLUS,
							RIGHT,
							SPACE
						]
					}
				},

				prototype: {
					headerEventHandler: function(event) {
						var instance = this;

						var isValidKey = AArray.some(
							instance.get('filter'),
							function(item, index, collection) {
								return event.isKey(item);
							}
						);

						if (isValidKey || event.type === CLICK) {
							KeyFilteredTogglerDelegate.superclass.headerEventHandler.call(instance, event);
						}
					}
				}
			}
		);

		Liferay.KeyFilteredTogglerDelegate = KeyFilteredTogglerDelegate;
	},
	'',
	{
		requires: ['aui-toggler-delegate']
	}
);