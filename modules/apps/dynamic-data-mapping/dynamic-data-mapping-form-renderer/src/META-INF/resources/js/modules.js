;(function() {
	AUI().applyConfig(
		{
			groups: {
 				'form': {
					base: 'js',
					modules: {
						'liferay-ddm-form-renderer': {
							path: 'form.js',
							requires: [
								'aui-component',
								'aui-tabview',
								'liferay-ddm-form-field-checkbox',
								'liferay-ddm-form-field-options',
								'liferay-ddm-form-field-radio',
								'liferay-ddm-form-field-select',
								'liferay-ddm-form-field-text-template',
								'liferay-ddm-form-renderer-definition',
								'liferay-ddm-form-renderer-field-types',
								'liferay-ddm-form-renderer-nested-fields',
								'liferay-ddm-form-renderer-util'
							]
						},
						'liferay-ddm-form-renderer-definition': {
							path: 'form_definition_support.js',
							requires: [
								'liferay-ddm-form-renderer-field-types',
								'liferay-ddm-form-renderer-util'
							]
						},
						'liferay-ddm-form-renderer-field': {
							path: 'field.js',
							requires: [
								'aui-datatype',
								'aui-node',
								'liferay-ddm-form-renderer',
								'liferay-ddm-form-renderer-field-types',
								'liferay-ddm-form-renderer-util'
							]
						},
						'liferay-ddm-form-renderer-field-repetition': {
							path: 'field_repetition_support.js',
							requires: [
								'aui-datatype',
								'liferay-ddm-form-renderer-field-types',
								'liferay-ddm-form-renderer-util'
							]
						},
						'liferay-ddm-form-renderer-field-types': {
							path: 'field_types.js',
							requires: [
								'array-extras',
								'aui-form-builder-field-type',
								'liferay-ddm-form-renderer-util'
							]
						},
						'liferay-ddm-form-renderer-nested-fields': {
							path: 'nested_fields_support.js',
							requires: [
								'liferay-ddm-form-renderer-field-types',
								'liferay-ddm-form-renderer-util'
							]
						},
						'liferay-ddm-form-renderer-util': {
							path: 'util.js',
							requires: [
								'liferay-ddm-form-renderer-field-types',
								'queue'
							]
						}
					},
					root: Liferay.ThemeDisplay.getPathContext() + '/o/ddm-form-renderer/'
				}
			}
		}
	);
})();