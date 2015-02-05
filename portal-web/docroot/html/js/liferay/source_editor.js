/* global ace */

AUI.add(
	'liferay-source-editor',
	function(A) {
		var Lang = A.Lang;

		var CSS_ACTIVE_CELL = 'ace_gutter-active-cell';

		var STR_BOUNDING_BOX = 'boundingBox';

		var STR_CHANGE_CURSOR = 'changeCursor';

		var STR_CHANGE_FOLD = 'changeFold';

		var STR_CODE = 'code';

		var STR_DATA_CURRENT_THEME = 'data-currenttheme';

		var STR_DOT = '.';

		var STR_VALUE = 'value';

		var STR_THEMES = 'themes';

		var STR_TOOLBAR = 'toolbar';

		var TPL_CODE_CONTAINER = '<div class="{cssClass}"></div>';

		var TPL_FULL_SCREEN = '<div class="lfr-source-editor-fullscreen">' +
		'<div class="content-html"> <div id="{sourceCodeId}"></div> </div>' +
		'<div class="splitter"></div>' +
		'<div class="content-preview"> {preview} </div>'+
		'</div>';

		var TPL_THEME_BUTTON = '<li data-action="{action}"><button type="button" class="btn btn-default btn-lg"><i class="{iconCssClass}"></i></button></li>';

		var TPL_TOOLBAR = '<ul class="{cssClass}">{buttons}</ul>';

		var LiferaySourceEditor = A.Component.create(
			{
				ATTRS: {
					aceOptions: {
						validator: Lang.isObject,
						valueFn: function() {
							var instance = this;

							var aceEditor = instance.getEditor();

							return {
								fontSize: 13,
								maxLines: Math.floor(A.DOM.winHeight() / aceEditor.renderer.lineHeight) - 15,
								minLines: 10,
								showInvisibles: false,
								showPrintMargin: false
							};
						}
					},

					fullScreenTitle: {
						validator: Lang.isString,
						value: ' '
					},

					height: {
						validator: function(value) {
							return Lang.isString(value) || Lang.isNumber(value);
						},
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
						validator: function(value) {
							return Lang.isString(value) || Lang.isNumber(value);
						},
						value: '100%'
					}
				},

				CSS_PREFIX: 'lfr-source-editor',

				EXTENDS: A.AceEditor,

				NAME: 'liferaysourceeditor',

				NS: 'liferaysourceeditor',

				prototype: {
					initializer: function() {
						var instance = this;

						var aceEditor = instance.getEditor();

						aceEditor.setOptions(instance.get('aceOptions'));

						instance._initializeThemes();
						instance._highlightActiveGutterLine(0);
					},

					bindUI: function() {
						var instance = this;

						var updateActiveLineFn = A.bind('_updateActiveLine', instance);

						var aceEditor = instance.getEditor();

						aceEditor.selection.on(STR_CHANGE_CURSOR, updateActiveLineFn);
						aceEditor.session.on(STR_CHANGE_FOLD, updateActiveLineFn);

						var toolbar = instance.get(STR_BOUNDING_BOX).one(STR_DOT + instance.getClassName(STR_TOOLBAR));

						instance._eventHandles = [
							toolbar.delegate('click', A.bind('_onToolbarClick', instance), 'li[data-action]')
						];
					},

					destructor: function() {
						var instance = this;

						var aceEditor = instance.getEditor();

						aceEditor.selection.removeAllListeners(STR_CHANGE_CURSOR);
						aceEditor.session.removeAllListeners(STR_CHANGE_FOLD);

						(new A.EventHandle(instance._eventHandles)).detach();
					},

					getEditor: function() {
						var instance = this;

						if (!instance.editor) {
							var boundingBox = instance.get(STR_BOUNDING_BOX);

							var codeContainer = boundingBox.one(STR_DOT + instance.getClassName(STR_CODE));

							if (!codeContainer) {
								codeContainer = A.Node.create(
									Lang.sub(
										TPL_CODE_CONTAINER,
										{
											cssClass: instance.getClassName(STR_CODE)
										}
									)
								);

								boundingBox.append(codeContainer);
							}

							var toolbarContainer = boundingBox.one(STR_DOT + instance.getClassName(STR_TOOLBAR));

							if (!toolbarContainer) {
								boundingBox.append(
									Lang.sub(
										TPL_TOOLBAR,
										{
											buttons: instance._getButtonsMarkup(),
											cssClass: instance.getClassName(STR_TOOLBAR)
										}
									)
								);
							}

							instance.editor = ace.edit(codeContainer.getDOM());
						}

						return instance.editor;
					},

					openFullScreen: function() {
						var instance = this;

						var sourceCodeId = A.guid();

						var templateContent = Lang.sub(
							TPL_FULL_SCREEN,
							{
								preview: instance.get(STR_VALUE),
								sourceCodeId: sourceCodeId
							}
						);

						var fullScreenDialog;

						Liferay.Util.openWindow(
							{
								dialog: {
									after: {
										destroy: function() {
											instance._editorFullScreen.destroy();
										}
									},
									bodyContent: templateContent,
									constrain: true,
									destroyOnHide: true,
									modal: true,
									toolbars: {
										footer: [
											{
												label: Liferay.Language.get('discard'),
												on: {
													click: function() {
														fullScreenDialog.hide();
													}
												}
											},
											{
												cssClass: 'btn-primary',
												label: Liferay.Language.get('done'),
												on: {
													click: function() {
														var currentValue = instance._editorFullScreen.getValue();

														instance.set(STR_VALUE, currentValue);

														fullScreenDialog.hide();
													}
												}
											}
										],
										header: [
											{
												cssClass: 'close',
												label: '\u00D7',
												on: {
													click: function(event) {
														fullScreenDialog.hide();

														event.domEvent.stopPropagation();
													}
												},
												render: true
											}
										]
									}
								},
								title: instance.get('fullScreenTitle')
							},
							function(dialog) {
								fullScreenDialog = dialog;

								var options = A.merge(
									instance.get('aceOptions'),
									{
										mode: instance.get('mode').$id
									}
								);

								var fullScreenEditor = ace.edit(A.one('#' + sourceCodeId).getDOM());

								fullScreenEditor.setOptions(options);

								fullScreenEditor.getSession().setValue(instance.get(STR_VALUE));

								fullScreenEditor.getSession().on('change', A.debounce(A.bind(instance._refreshPreviewEntry, instance), 100));

								instance._editorFullScreen = fullScreenEditor;
							}
						);
					},

					_getButtonsMarkup: function() {
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
							session.removeGutterDecoration(instance._currentLine, CSS_ACTIVE_CELL);
						}

						session.addGutterDecoration(line, CSS_ACTIVE_CELL);

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

					_refreshPreviewEntry: function() {
						var instance = this;

						var fullScreenEditor = instance._editorFullScreen;

						A.one('.content-preview').html(fullScreenEditor.getValue());
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