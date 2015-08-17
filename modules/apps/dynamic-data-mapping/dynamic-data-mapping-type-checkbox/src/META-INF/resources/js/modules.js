;(function() {
	AUI().applyConfig(
		{
			groups: {
				'liferay-ddm-type-checkbox': {
					base: 'js',
					modules: {
						'liferay-ddm-form-field-checkbox': {
							path: 'checkbox_field.js',
							requires: [
								'liferay-ddm-form-field-checkbox-template',
								'liferay-ddm-form-renderer-field'
							]
						},
						'liferay-ddm-form-field-checkbox-template': {
							path: 'checkbox.soy.js',
							requires: [
								'soyutils'
							]
						}
					},
					root: Liferay.ThemeDisplay.getPathContext() + '/o/ddm-type-checkbox/'
				}
			}
		}
	);
}());