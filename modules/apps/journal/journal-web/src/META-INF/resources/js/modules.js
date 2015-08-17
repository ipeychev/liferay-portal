;(function() {
	AUI().applyConfig(
		{
			groups: {
				journal: {
					base: 'js',
					modules: {
						'liferay-journal-content': {
							path: 'content.js',
							requires: [
								'aui-base',
								'liferay-portlet-base'
							]
						},
						'liferay-journal-navigation': {
							path: 'navigation.js',
							requires: [
								'liferay-app-view-move',
								'liferay-app-view-select',
								'liferay-portlet-base'
							]
						},
						'liferay-portlet-journal': {
							path: 'main.js',
							requires: [
								'aui-base',
								'aui-dialog-iframe-deprecated',
								'aui-tooltip',
								'liferay-portlet-base',
								'liferay-util-window'
							]
						}
					},
					root: Liferay.ThemeDisplay.getPathContext() + '/o/comliferayjournalweb/'
				}
			}
		}
	);
})();