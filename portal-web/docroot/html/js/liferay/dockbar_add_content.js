AUI.add(
	'liferay-dockbar-add-content',
	function(A) {
		var Lang = A.Lang;

		var Node = A.Node;

		var isString = Lang.isString;

		var ParseContent = A.Plugin.ParseContent;

		var AddContent = A.Component.create(
			{
				ATTRS: {
					namespace: {
						validator: isString
					}
				},

				AUGMENTS: [Liferay.PortletBase],

				EXTENDS: A.Base,

				NAME: 'addcontent',

				prototype: {
					initializer: function(config) {
						var instance = this;

						instance._addContentForm = instance.byId('addContentForm');

						instance._styleButtonsList = instance.byId('styleButtons');

						instance._styleButtons = instance._styleButtonsList.all('.button');

						instance._styleButtons.on('click', instance._onChangeDisplayStyle, instance);

						instance._numItems = instance.byId('numItems');

						instance._numItems.on('change', instance._refreshContentList, instance);

						instance._searchInput = instance.byId('searchInput');

						instance._createAddContentSearch();

						instance._entriesContainer = instance.byId('entriesContainer');

						instance._entriesContainer.delegate(
							'click',
							function(event) {
								event.halt();

								var item = event.currentTarget;

								var portletId = item.attr('data-portlet-id');

								var portletData = item.attr('data-class-pk') + "," + item.attr('data-class-name');

								Liferay.Portlet.add(
									{
										portletData: portletData,
										portletId: portletId
									}
								);
							},
							'.content-shortcut'
						);
					},

					destructor: function() {
						var instance = this;


					},

					_afterFailure: function(event) {

					},

					_afterSuccess: function(event) {
						var instance = this;

						var data = event.currentTarget.get('responseData');

						var content = Node.create(data);

						instance._entriesContainer.empty();

						instance._entriesContainer.plug(ParseContent);

						instance._entriesContainer.setContent(content);
					},

					_createAddContentSearch: function() {
						var instance = this;

						var addContentSearch = new AddContentSearch(
							{
								inputNode: instance._searchInput,
								minQueryLength: 0,
								queryDelay: 300
							}
						);

						addContentSearch.after(
							'query',
							function(event) {
								instance._restartSearch = true;

								instance._refreshContentList(event);
							}
						);

						instance._searchInput.on('keydown', instance._onSearchInputKeyDown, instance);

						instance._addContentSearch = addContentSearch;
					},

					_onChangeDisplayStyle: function(event) {
						var instance = this;

						instance._styleButtons.removeClass('selected');

						event.currentTarget.addClass('selected');

						instance._refreshContentList(event);
					},

					_onSearchInputKeyDown: function(event) {
						if (event.isKey('ENTER')) {
							event.halt();
						}
					},

					_refreshContentList: function(event) {
						var instance = this;

						var styleButton = instance._styleButtonsList.one('.selected');

						var displayStyle = styleButton.attr('data-style');

						var uri = instance._addContentForm.getAttribute('action');

						A.io.request(
							uri,
							{
								after: {
									failure: A.rbind(instance._afterFailure, instance),
									success: A.rbind(instance._afterSuccess, instance)
								},
								data: {
									delta: instance._numItems.val(),
									displayStyle: displayStyle,
									keywords: instance._searchInput.val()
								}
							}
						);
					}
				}
			}
		);

		var AddContentSearch = A.Component.create(
			{
				AUGMENTS: [A.AutoCompleteBase],
				EXTENDS: A.Base,
				NAME: 'addcontentsearch',
				prototype: {
					initializer: function() {
						this._bindUIACBase();
						this._syncUIACBase();
					}
				}
			}
		);

		Liferay.AddContent = AddContent;
	},
	'',
	{
		requires: ['aui-dialog', 'aui-io-request', 'autocomplete-base', 'liferay-portlet-base']
	}
);