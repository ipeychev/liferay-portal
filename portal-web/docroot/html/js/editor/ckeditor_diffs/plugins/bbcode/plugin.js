(function() {
	CKEDITOR.plugins.add(
	'bbcode',
		{
			init: function(editor) {
				var instance = this;

				instance._loadDependencies(editor);

				var preElement = new CKEDITOR.style(
					{
						element: 'pre'
					}
				);

				preElement._.enterMode = editor.config.enterMode;

				editor.ui.addButton(
					'Code',
					{
						click : function() {
							editor.focus();
							editor.fire('saveSnapshot');

							var elementPath = new CKEDITOR.dom.elementPath(editor.getSelection().getStartElement());

							var elementAction = 'apply';

							if (preElement.checkActive(elementPath)) {
								elementAction = 'remove';
							}

							preElement[elementAction](editor.document);

							setTimeout(
								function() {
									editor.fire('saveSnapshot');
								},
								0
							);
						},
						icon: editor.config.imagesPath + 'code.png',
						label: Liferay.Language.get('code')
					}
				);
			},

			_initBBCodeDataProcessor: function(editor) {
				var bbcodeDataProcessor = CKEDITOR.plugins.get('bbcode_data_processor');

				bbcodeDataProcessor.init(editor);
			},

			_loadDependencies: function(editor) {
				var instance = this;

				var path = instance.path;

				var dependencies = [
					CKEDITOR.getUrl(path + 'bbcode_data_processor.js'),
					CKEDITOR.getUrl(path + 'bbcode_parser.js')
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
									instance._initBBCodeDataProcessor(editor);
								}
							}
						);
					}
					else {
						instance._initBBCodeDataProcessor(editor);
					}
				});
			}
		}
	);
})();