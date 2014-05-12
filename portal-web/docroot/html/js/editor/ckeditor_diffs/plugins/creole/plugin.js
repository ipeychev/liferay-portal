;(function() {
	CKEDITOR.plugins.add(
	'creole',
		{
			init: function(editor) {
				var instance = this;

				instance._loadDependencies(editor);
			},

			_initCreoleDataProcessor: function(editor) {
				var creoleDataProcessor = CKEDITOR.plugins.get('creole_data_processor');

				creoleDataProcessor.init(editor);
			},

			_loadDependencies: function(editor) {
				var instance = this;

				var path = instance.path;

				var dependencies = [
					CKEDITOR.getUrl(path + 'creole_data_processor.js'),
					CKEDITOR.getUrl(path + 'creole_parser.js')
				];

				AUI().use('get', function(A) {
					var dependenciesLoaded = true;

					for (var i = 0, length = dependencies.length; i < length; i++) {
						if (!A.one('script[src=' + dependencies[i] + ']')) {
							dependenciesLoaded = false;

							break;
						}
					}

					if (!dependenciesLoaded) {
						A.Get.js(
							dependencies,
							function(error) {
								if (!error) {
									instance._initCreoleDataProcessor(editor);
								}
							}
						);
					}
					else {
						instance._initCreoleDataProcessor(editor);
					}
				});
			}
		}
	);
})();