AUI.add(
	'liferay-app-view-folders-navigation',
	function(A) {
		var AObject = A.Object;
		var Lang = A.Lang;
		
		var History = Liferay.HistoryManager;
		var formatSelectorNS = A.Node.formatSelectorNS;

		var owns = AObject.owns;

		var CSS_SELECTED = 'selected';

		var DATA_DIRECTION_RIGHT = 'data-direction-right';

		var DATA_FOLDER_ID = 'data-folder-id';

		var DATA_VIEW_ENTRIES = 'data-view-entries';

		var DATA_VIEW_FOLDERS = 'data-view-folders';

		var EXPAND_FOLDER = 'expandFolder';

		var MESSAGE_TYPE_ERROR = 'error';

		var SEARCH_REPOSITORY_ID = 'searchRepositoryId';

		var SRC_HISTORY = 2;

		var SRC_RESTORE_STATE = 4;

		var SRC_SEARCH = 3;

		var STR_AJAX_REQUEST = 'ajax';

		var STR_CLICK = 'click';

		var STR_DATA = 'data';

		var STR_FOLDER_ID = 'folderId';

		var STRUTS_ACTION = 'struts_action';

		var TPL_MESSAGE_RESPONSE = '<div class="lfr-message-response" />';

		var VIEW_ENTRIES = 'viewEntries';

		var VIEW_FOLDERS = 'viewFolders';

		var AppViewFoldersNavigation = A.Component.create(
			{
				ATTRS: {
					listView: {
						validator: Lang.isObject
					},

					portletContainerId: {
						validator: Lang.isString
					}
				},

				AUGMENTS: [Liferay.PortletBase],

				EXTENDS: A.Base,

				NAME: 'liferay-app-view-folders-navigation',

				prototype: {
					initializer: function(config) {
						var instance = this;

						var portletContainer = instance.byId(instance.get('portletContainerId'));

						instance._config = config;

						instance._eventDataRetrieveSuccess = instance.ns('dataRetrieveSuccess');
						instance._eventDataRequest = instance.ns('dataRequest');
						instance._dataRetrieveFailure = instance.ns('dataRetrieveFailure');

						instance._listView = instance.get('listView');

						instance._pageNavigation = config.pageNavigation;

						instance._portletContainer = portletContainer;

						instance._displayStyle = instance.ns('displayStyle');
						instance._folderId = instance.ns(STR_FOLDER_ID);

						instance._displayStyleToolbar = instance.byId(config.displayStyleToolbarId);

						instance._portletMessageContainer = A.Node.create(TPL_MESSAGE_RESPONSE);

						instance._entriesContainer = instance.byId('entriesContainer');

						instance._eventPageLoaded = instance.ns('pageLoaded');

						instance._repositoriesData = {};

						var eventHandles = [
							Liferay.after(instance._eventDataRequest, instance._afterDataRequest, instance),
							Liferay.on(instance._dataRetrieveFailure, instance._onDataRetrieveFailure, instance),
							Liferay.on(instance._eventDataRequest, instance._onDataRequest, instance)
						];

						instance._eventHandles = eventHandles;

						portletContainer.delegate(
							STR_CLICK,
							A.bind(instance._onPortletContainerClick, instance),
							formatSelectorNS(instance.NS, '#entriesContainer a[data-folder=true], #breadcrumbContainer a')
						);

						portletContainer.plug(A.LoadingMask);

						instance._restoreState();
					},

					destructor: function() {
						var instance = this;

						A.Array.invoke(instance._eventHandles, 'detach');
					},

					_addHistoryState: function(data) {
						var instance = this;

						var historyState = A.clone(data);

						var currentHistoryState = History.get();

						var defaultParams = instance._config.defaultParams;

						AObject.each(
							currentHistoryState,
							function(index, item, collection) {
								if (!owns(historyState, item) && !owns(defaultParams, item)) {
									historyState[item] = null;
								}
							}
						);

						if (!AObject.isEmpty(historyState)) {
							History.add(historyState);
						}
					},

					_afterDataRequest: function(event) {
						var instance = this;

						var requestParams = event.requestParams;

						var config = instance._config;

						var data = {};

						var displayStyle = instance._displayStyle;

						data[instance._folderId] = config.defaultParentFolderId;

						data[displayStyle] = History.get(displayStyle) || config.displayStyle;

						data[instance.ns(VIEW_ENTRIES)] = true;

						data[instance.ns(VIEW_FOLDERS)] = true;

						A.mix(data, requestParams, true);

						var src = event.src;

						if (src !== SRC_HISTORY) {
							instance._addHistoryState(data);
						}

						if (src !== SRC_RESTORE_STATE) {
							data[STR_AJAX_REQUEST] = true;
						}

						var ioRequest = A.io.request(
							instance._config.mainUrl,
							{
								autoLoad: false,
								method: 'get'
							}
						);

						var sendIOResponse = A.bind(instance._sendIOResponse, instance, ioRequest);

						ioRequest.after(['failure', 'success'], sendIOResponse);

						ioRequest.set(STR_DATA, data);

						if (src === SRC_SEARCH) {
							var repositoryId = event.requestParams[instance.NS + SEARCH_REPOSITORY_ID];

							var repositoriesData = instance._repositoriesData;

							var repositoryData = repositoriesData[repositoryId];

							if (!repositoryData) {
								repositoryData = {};

								repositoriesData[repositoryId] = repositoryData;
							}

							repositoryData.dataRequest = data;
						}
						else {
							instance._portletContainer.loadingmask.show();
						}

						ioRequest.start();

						delete data[STR_AJAX_REQUEST];

						instance._lastDataRequest = data;

						Liferay.fire(
							'liferay-app-view-folders-navigation:afterDataRequest',
							{
								data: data
							}
						);

					},

					_afterListViewItemChange: function(event, data) {
						var instance = this;

						var selFolder = A.one('.folder.selected');

						if (selFolder) {
							selFolder.removeClass(CSS_SELECTED);
						}

						var item = event.newVal;

						item.ancestor('.folder').addClass(CSS_SELECTED);

						var dataExpandFolder = item.attr('data-expand-folder');
						var dataStructureId = item.attr(data.entryTypeId);
						var dataFolderId = item.attr(DATA_FOLDER_ID);
						var dataNavigation = item.attr('data-navigation');
						var dataViewEntries = item.attr(DATA_VIEW_ENTRIES);
						var dataViewFolders = item.attr(DATA_VIEW_FOLDERS);

						var direction = 'left';

						if (item.attr(DATA_DIRECTION_RIGHT)) {
							direction = 'right';
						}

						instance._listView.set('direction', direction);

						var config = instance._config;

						var requestParams = {};

						requestParams[instance.ns(STRUTS_ACTION)] = config.strutsAction;

						if (dataExpandFolder) {
							requestParams[instance.ns(EXPAND_FOLDER)] = dataExpandFolder;
						}

						if (dataFolderId) {
							requestParams[instance._folderId] = dataFolderId;
						}

						if (dataNavigation) {
							requestParams[instance.ns('navigation')] = dataNavigation;
						}

						if (dataViewEntries) {
							requestParams[instance.ns(VIEW_ENTRIES)] = dataViewEntries;
						}

						if (dataStructureId) {
							requestParams[instance.ns(data.requestParam)] = dataStructureId;
						}

						if (dataViewFolders) {
							requestParams[instance.ns(VIEW_FOLDERS)] = dataViewFolders;
						}

						Liferay.fire(
							instance._eventDataRequest,
							{
								requestParams: requestParams,
								resetPaginator: true
							}
						);
					},

					_getDefaultHistoryState: function() {
						var instance = this;

						var initialState = History.get();

						if (AObject.isEmpty(initialState)) {
							initialState = instance._pageNavigation._getDefaultParams();
						}

						return initialState;
					},

					_onDataRequest: function(event) {
						var instance = this;

						instance._processDefaultParams(event);

						Liferay.fire(
							'liferay-app-view-folders-navigation:dataRequest',
							{
								requestParams: event.requestParams,
								src: event.src
							}
						);
					},

					_onDataRetrieveFailure: function(event) {
						var instance = this;

						instance._portletContainer.loadingmask.hide();

						instance._sendMessage(MESSAGE_TYPE_ERROR, Liferay.Language.get('your-request-failed-to-complete'));
					},

					_onPortletContainerClick: function(event) {
						var instance = this;

						event.preventDefault();

						var config = instance._config;

						var requestParams = {};

						requestParams[instance.ns(STRUTS_ACTION)] = config.strutsAction;
						requestParams[instance.ns('action')] = 'browseFolder';
						requestParams[instance._folderId] = event.currentTarget.attr(DATA_FOLDER_ID);
						requestParams[instance.ns(EXPAND_FOLDER)] = false;

						var viewEntries = event.currentTarget.attr(DATA_VIEW_ENTRIES);

						if (viewEntries) {
							requestParams[instance.ns(VIEW_ENTRIES)] = viewEntries;
						}

						var viewFolders = event.currentTarget.attr(DATA_VIEW_FOLDERS);

						if (viewFolders) {
							requestParams[instance.ns(VIEW_FOLDERS)] = viewFolders;
						}

						var direction = 'left';

						if (event.currentTarget.attr(DATA_DIRECTION_RIGHT)) {
							direction = 'right';
						}

						instance._listView.set('direction', direction);

						Liferay.fire(
							instance._eventDataRequest,
							{
								requestParams: requestParams,
								resetPaginator: true
							}
						);
					},

					_processDefaultParams: function(event) {
						var instance = this;

						var requestParams = event.requestParams;

						AObject.each(
							instance._config.defaultParams,
							function(item, index, collection) {
								if (!Lang.isValue(History.get(index))) {
									requestParams[index] = item;
								}
							}
						);
					},

					_restoreState: function() {
						var instance = this;

						if (!History.HTML5) {
							var initialState = History.get();

							if (!AObject.isEmpty(initialState)) {
								var namespace = instance.NS;

								var requestParams = {};

								AObject.each(
									initialState,
									function(item, index, collection) {
										if (index.indexOf(namespace) === 0) {
											requestParams[index] = item;
										}
									}
								);

								Liferay.fire(
									instance._eventDataRequest,
									{
										requestParams: requestParams,
										src: SRC_RESTORE_STATE
									}
								);
							}
						}
					},

					_sendIOResponse: function(ioRequest, event) {
						var instance = this;

						var data = ioRequest.get(STR_DATA);
						var responseData = ioRequest.get('responseData');

						var eventType = instance._eventDataRetrieveSuccess;

						if (event.type.indexOf('success') == -1) {
							eventType = instance._dataRetrieveFailure;
						}

						Liferay.fire(
							eventType,
							{
								data: data,
								responseData: responseData
							}
						);
					},

					_sendMessage: function(type, message) {
						var instance = this;

						var output = instance._portletMessageContainer;

						output.removeClass('portlet-msg-error').removeClass('portlet-msg-success');

						output.addClass('portlet-msg-' + type);

						output.html(message);

						output.show();

						instance._entriesContainer.setContent(output);
					},

					_setBreadcrumb: function(content) {
						var instance = this;

						var breadcrumb = instance.one('#breadcrumb', content);

						if (breadcrumb) {
							var breadcrumbContainer;

							var journalBreadcrumb = breadcrumb.one('.portlet-breadcrumb ul');

							if (journalBreadcrumb) {
								breadcrumbContainer = instance.byId('breadcrumbContainer');

								breadcrumbContainer.setContent(journalBreadcrumb);
							}

							var portalBreadcrumb = breadcrumb.one('.portal-breadcrumb ul');

							if (portalBreadcrumb) {
								breadcrumbContainer = A.one('#breadcrumbs ul');

								if (breadcrumbContainer) {
									breadcrumbContainer.setContent(portalBreadcrumb.html());
								}
							}
						}
					},

					_setButtons: function(content) {
						var instance = this;

						var addButton = instance.one('#addButton', content);

						if (addButton) {
							var addButtonContainer = instance.byId('addButtonContainer');

							addButtonContainer.plug(A.Plugin.ParseContent);

							addButtonContainer.setContent(addButton);
						}

						var displayStyleButtons = instance.one('#displayStyleButtons', content);

						if (displayStyleButtons) {
							instance._displayStyleToolbar.empty();

							var displayStyleButtonsContainer = instance.byId('displayStyleButtonsContainer');

							displayStyleButtonsContainer.plug(A.Plugin.ParseContent);

							displayStyleButtonsContainer.setContent(displayStyleButtons);
						}

						var sortButton = instance.one('#sortButton', content);

						if (sortButton) {
							var sortButtonContainer = instance.byId('sortButtonContainer');

							sortButtonContainer.plug(A.Plugin.ParseContent);

							sortButtonContainer.setContent(sortButton);
						}
					},

					_setEntries: function(content) {
						var instance = this;

						var entries = instance.one('#entries', content);

						if (entries) {
							var entriesContainer = instance._entriesContainer;

							entriesContainer.empty();

							entriesContainer.plug(A.Plugin.ParseContent);

							entriesContainer.setContent(entries);

							Liferay.fire('liferay-app-view-folders-navigation:setEntries');
						}
					},

					_setFolders: function(content) {
						var instance = this;

						var folders = instance.one('#folderContainer', content);

						if (folders) {
							var listViewDataContainer = A.one('.lfr-list-view-data-container');

							listViewDataContainer.plug(A.Plugin.ParseContent);

							instance._listView.set(STR_DATA, folders.html());
						}
					},

					_setParentFolderTitle: function(content) {
						var instance = this;

						var parentFolderTitle = instance.one('#parentFolderTitle', content);

						if (parentFolderTitle) {
							var parentFolderTitleContainer = instance.byId('parentFolderTitleContainer');

							parentFolderTitleContainer.setContent(parentFolderTitle);
						}
					}
				}
			}
		);

		Liferay.AppViewFoldersNavigation = AppViewFoldersNavigation;
	},
	'',
	{
		requires: ['aui-base', 'aui-parse-content', 'liferay-app-view-dd-navigation', 'liferay-history-manager', 'liferay-portlet-base']
	}
);