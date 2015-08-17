;(function() {
	AUI().applyConfig(
		{
			groups: {
 				'liferay-ddm-type-text': {
					base: 'js',
					modules: {
						'liferay-ddm-form-field-text-template': {
							path: 'text.soy.js',
							requires: [
								'soyutils'
							]
						}
					},
					root: Liferay.ThemeDisplay.getPathContext() + '/o/ddm-type-text/'
				}
			}
		}
	);
}());