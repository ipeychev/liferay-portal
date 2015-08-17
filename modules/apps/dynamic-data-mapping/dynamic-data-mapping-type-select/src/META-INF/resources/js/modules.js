;(function() {
	AUI().applyConfig(
		{
			groups: {
 				'liferay-ddm-type-select': {
					base: 'js',
					modules: {
						'liferay-ddm-form-field-select': {
							path: 'select_field.js',
							requires: [
								'liferay-ddm-form-field-select-template',
								'liferay-ddm-form-renderer-field'
							]
						},
						'liferay-ddm-form-field-select-template': {
							path: 'select.soy.js',
							requires: [
								'soyutils'
							]
						}
					},
					root: Liferay.ThemeDisplay.getPathContext() + '/o/ddm-type-select/'
				}
			}
		}
	);
}());