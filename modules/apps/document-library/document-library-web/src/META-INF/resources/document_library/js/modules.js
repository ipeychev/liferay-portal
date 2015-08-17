;(function() {
	AUI().applyConfig(
		{
			groups: {
				dl: {
					base: 'js',
					modules: {
						'document-library-upload': {
							path: 'upload.js',
							requires: [
								'aui-component',
								'aui-data-set-deprecated',
								'aui-overlay-manager-deprecated',
								'aui-overlay-mask-deprecated',
								'aui-parse-content',
								'aui-progressbar',
								'aui-template-deprecated',
								'aui-tooltip',
								'liferay-app-view-move',
								'liferay-app-view-select',
								'liferay-search-container',
								'liferay-storage-formatter',
								'querystring-parse-simple',
								'uploader'
							]
						},
						'liferay-document-library': {
							path: 'main.js',
							requires: [
								'document-library-upload',
								'liferay-app-view-move',
								'liferay-message',
								'liferay-portlet-base',
								'liferay-portlet-dynamic-data-mapping',
								'liferay-portlet-dynamic-data-mapping-custom-fields'
							]
						}
					},
					root: Liferay.ThemeDisplay.getPathContext() + '/o/document-library-web/'
				}
			}
		}
	);
})();