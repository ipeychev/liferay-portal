;(function() {
	AUI().applyConfig(
		{
			groups: {
 				'liferay-ddm-type-radio': {
					base: 'js',
					modules: {
						'liferay-ddm-form-field-radio': {
							path: 'radio_field.js',
							requires: [
								'liferay-ddm-form-renderer-field',
								'liferay-ddm-form-field-radio-template'
							]
						},
						'liferay-ddm-form-field-radio-template': {
							path: 'radio.soy.js',
							requires: [
								'soyutils'
							]
						}
					},
					root: Liferay.ThemeDisplay.getPathContext() + '/o/ddm-type-radio/'
				}
			}
		}
	);
}());