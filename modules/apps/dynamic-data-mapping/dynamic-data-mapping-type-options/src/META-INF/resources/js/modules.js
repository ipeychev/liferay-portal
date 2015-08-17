;(function() {
	AUI().applyConfig(
		{
			groups: {
 				'liferay-ddm-type-options': {
					base: 'js',
					modules: {
						'liferay-ddm-form-field-options': {
							path: 'options_field.js',
							requires: [
								'liferay-auto-fields',
								'liferay-ddm-form-field-options-template',
								'liferay-ddm-form-renderer-field'
							]
						},
						'liferay-ddm-form-field-options-template': {
							path: 'options.soy.js',
							requires: [
								'soyutils'
							]
						}
					},
					root: Liferay.ThemeDisplay.getPathContext() + '/o/ddm-type-options/'
				}
			}
		}
	);
}());