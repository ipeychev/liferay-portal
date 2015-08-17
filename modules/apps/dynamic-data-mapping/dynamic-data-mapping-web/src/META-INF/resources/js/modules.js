;(function() {
	AUI().applyConfig(
		{
			groups: {
 				ddm: {
					base: 'js',
					modules: {
						'liferay-portlet-dynamic-data-mapping': {
							path: 'main.js',
							requires: [
								'arraysort',
								'aui-form-builder-deprecated',
								'aui-form-validator',
								'aui-map', 'aui-text-unicode',
								'json',
								'liferay-menu',
								'liferay-translation-manager',
								'liferay-util-window',
								'text'
							]
						},
						'liferay-portlet-dynamic-data-mapping-custom-fields': {
							path: 'custom_fields.js',
							requires: [
								'liferay-portlet-dynamic-data-mapping'
							]
						}
					},
					root: Liferay.ThemeDisplay.getPathContext() + '/o/ddm-web/'
				}
			}
		}
	);
}());