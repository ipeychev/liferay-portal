AUI.add(
	'liferay-source-editor',
	function(A) {
		var Lang = A.Lang;

		var CLASS_ACTIVE_CELL = 'ace_gutter-active-cell';

		var STR_BOUNDING_BOX = 'boundingBox';

		var STR_CHANGE_CURSOR = 'changeCursor';

		var STR_CHANGE_FOLD = 'changeFold';

		var STR_CODE = 'code';

		var STR_DATA_CURRENT_THEME = 'data-currenttheme';

		var STR_THEMES = 'themes';

		var STR_TOOLBAR = 'toolbar';

		var TPL_CODE_CONTAINER = '<div class="{cssClass}"></div>';

		var TPL_TOOLBAR = '<ul class="{cssClass}">{buttons}</ul>';

		var TPL_THEME_BUTTON = '<li data-action="{action}"><a href="javascript:;"><i class="{iconCssClass}"></i></a></li>';

		var LiferaySourceEditor = A.Component.create(
			{
				ATTRS: {
					height: {
						value: 'auto'
					},

					themes: {
						validator: Lang.isArray,
						value: [
							{
								cssClass: '',
								iconCssClass: 'icon-sun'
							},
							{
								cssClass: 'ace_dark',
								iconCssClass: 'icon-moon'
							}
						]
					},

					width: {
						value: '100%'
					}
				},

				AUGMENTS: [],

				CSS_PREFIX: 'lfr-source-editor',

				EXTENDS: A.AceEditor,

				NAME: 'liferaysourceeditor',

				NS: 'liferaysourceeditor',

				prototype: {
					initializer: function() {
						var instance = this;

						var aceEditor = instance.getEditor();

						aceEditor.setOptions(
							{
								fontSize: 13,
								maxLines: Math.floor(A.DOM.winHeight() / aceEditor.renderer.lineHeight) - 15,
								minLines: 10,
								showInvisibles: false,
								showPrintMargin: false
							}
						);

						instance._initializeThemes();
						instance._highlightActiveGutterLine(0);
					},

					bindUI: function() {
						var instance = this;

						var updateActiveLineFn = A.bind('_updateActiveLine', instance);

						var aceEditor = instance.getEditor();

						aceEditor.selection.on(STR_CHANGE_CURSOR, updateActiveLineFn);
						aceEditor.session.on(STR_CHANGE_FOLD, updateActiveLineFn);

						var toolbar = instance.get(STR_BOUNDING_BOX).one('.' + instance.getClassName(STR_TOOLBAR));

						instance._eventHandles = [
							toolbar.delegate('click', A.bind('_onToolbarClick', instance), 'li[data-action]')
						];
					},

					destructor: function() {
						var instance = this;

						var aceEditor = instance.getEditor();

						aceEditor.selection.removeAllListeners(STR_CHANGE_CURSOR);
						aceEditor.session.removeAllListeners(STR_CHANGE_FOLD);
					},

					getEditor: function() {
						var instance = this;

						if (!instance.editor) {
							var contentBox = instance.get('contentBox');

							var codeContainer = contentBox.one(instance.getClassName(STR_CODE));

							if (!codeContainer) {
								codeContainer = A.Node.create(
									Lang.sub(
										TPL_CODE_CONTAINER,
										{
											cssClass: instance.getClassName(STR_CODE)
										}
									)
								);

								contentBox.append(codeContainer);

								contentBox.append(
									Lang.sub(
										TPL_TOOLBAR,
										{
											buttons: instance._getToolbarButtons(),
											cssClass: instance.getClassName(STR_TOOLBAR)
										}
									)
								);
							}

							instance.editor = ace.edit(codeContainer.getDOM());
						}

						return instance.editor;
					},

					_getToolbarButtons: function() {
						var instance = this;

						var toolbarButtons = '';

						var themes = instance.get(STR_THEMES);

						if (themes.length > 1) {
							toolbarButtons += Lang.sub(
								TPL_THEME_BUTTON,
								{
									action: STR_THEMES,
									iconCssClass: themes[1].iconCssClass || 'icon-adjust'
								}
							);
						}

						return toolbarButtons;
					},

					_highlightActiveGutterLine: function(line) {
						var instance = this;

						var session = instance.getSession();

						if (instance._currentLine !== null) {
							session.removeGutterDecoration(instance._currentLine, CLASS_ACTIVE_CELL);
						}

						session.addGutterDecoration(line, CLASS_ACTIVE_CELL);

						instance._currentLine = line;
					},

					_initializeThemes: function() {
						var instance = this;

						var themes = instance.get(STR_THEMES);

						if (themes.length) {
							instance.get(STR_BOUNDING_BOX).addClass(themes[0].cssClass);
						}
					},

					_onToolbarClick: function(event) {
						var instance = this;

						var currentTarget = event.currentTarget;

						var action = currentTarget.attr('data-action');

						if (action === STR_THEMES) {
							instance._switchTheme(currentTarget);
						}
					},

					_switchTheme: function(themeSelector) {
						var instance = this;

						var themes = instance.get(STR_THEMES);

						var currentThemeIndex = Lang.toInt(themeSelector.attr(STR_DATA_CURRENT_THEME));

						var nextThemeIndex = (currentThemeIndex + 1) % themes.length;

						var currentTheme = themes[currentThemeIndex];

						var nextTheme = themes[nextThemeIndex];

						var nextThemeIcon = themes[(currentThemeIndex + 2) % themes.length].iconCssClass;

						themeSelector.attr(STR_DATA_CURRENT_THEME, nextThemeIndex);

						themeSelector.one('i').replaceClass(nextTheme.iconCssClass, nextThemeIcon);

						var boundingBox = instance.get(STR_BOUNDING_BOX);

						boundingBox.replaceClass(currentTheme.cssClass, nextTheme.cssClass);
					},

					_updateActiveLine: function() {
						var instance = this;

						var line = instance.getEditor().getCursorPosition().row;

						var session = instance.getSession();

						if (session.isRowFolded(line)) {
							line = session.getRowFoldStart(line);
						}

						instance._highlightActiveGutterLine(line);
					}
				}
			}
		);

		A.LiferaySourceEditor = LiferaySourceEditor;
	},
	'',
	{
		requires: ['aui-ace-editor']
	}
);