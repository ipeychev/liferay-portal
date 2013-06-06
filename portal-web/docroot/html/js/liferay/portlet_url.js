AUI.add(
	'liferay-portlet-url',
	function(A) {
		var Util = Liferay.Util;

		var PortletURL = function(lifecycle, params, baseURL) {
			var instance = this;

			instance.params = {};

			instance.reservedParams = {
				controlPanelCategory: null,
				doAsUserId: null,
				doAsUserLanguageId: null,
				doAsGroupId: null,
				p_auth: null,
				p_auth_secret: null,
				p_l_id: null,
				p_l_reset: null,
				p_p_auth: null,
				p_p_id: null,
				p_p_i_id: null,
				p_p_lifecycle: null,
				p_p_url_type: null,
				p_p_state: null,
				p_p_state_rcv: null, // LPS-14144
				p_p_mode: null,
				p_p_resource_id: null,
				p_p_cacheability: null,
				p_p_width: null,
				p_p_col_id: null,
				p_p_col_pos: null,
				p_p_col_count: null,
				p_p_static: null,
				p_p_isolated: null,
				p_t_lifecycle: null, // LPS-14383
				p_v_l_s_g_id: null, // LPS-23010
				p_f_id: null,
				p_j_a_id: null, // LPS-16418
				refererGroupId: null,
				refererPlid: null,
				saveLastPath: null,
				scroll: null
			};

			instance.options = {
				baseURL: baseURL,
				escapeXML: null,
				secure: null
			};

			if (!baseURL) {
				instance.options.baseURL = themeDisplay.getPathContext() + themeDisplay.getPathMain() + '/portal/layout?p_l_id=' + themeDisplay.getPlid();
			}

			A.each(
				params,
				function(value, key) {
					if (value) {
						if (instance._isReservedParam(key)) {
							instance.reservedParams[key] = value;
						}
						else {
							instance.params[key] = value;
						}
					}
				}
			);

			if (lifecycle) {
				instance.setLifecycle(lifecycle);
			}
		};

		PortletURL.prototype = {
			setCopyCurrentRenderParameters: function(copyCurrentRenderParameters) {
				var instance = this;

				/* Deprecate - we may not know current render parameters (e.g. set by the event phase)
				 * Only server side knows the parameters - must be already inside baseURL.
				 */

				// instance.options.copyCurrentRenderParameters = copyCurrentRenderParameters;

				return instance;
			},
			setDoAsGroupId: function(doAsGroupId) {
				var instance = this;

				instance.reservedParams.doAsGroupId = doAsGroupId;

				return instance;
			},

			setDoAsUserId: function(doAsUserId) {
				var instance = this;

				instance.reservedParams.doAsUserId = doAsUserId;

				return instance;
			},

			setEncrypt: function(encrypt) {
				var instance = this;

				/* Deprecate - removed from backend for a longer time */

				// instance.options.encrypt = encrypt;

				return instance;
			},

			setEscapeXML: function(escapeXML) {
				var instance = this;

				instance.options.escapeXML = escapeXML;

				return instance;
			},

			setLifecycle: function(lifecycle) {
				var instance = this;

				var reservedParams = instance.reservedParams;

				if (lifecycle === 'ACTION_PHASE') {
					reservedParams.p_p_lifecycle = '1';
				}
				else if (lifecycle === 'RENDER_PHASE') {
					reservedParams.p_p_lifecycle = '0';
				}
				else if (lifecycle === 'RESOURCE_PHASE') {
					reservedParams.p_p_lifecycle = '2';
					reservedParams.p_p_cacheability = 'cacheLevelPage';
				}

				return instance;
			},

			setName: function(name) {
				var instance = this;

				instance.setParameter('javax.portlet.action', name);

				return instance;
			},

			setParameter: function(key, value) {
				var instance = this;

				if (instance._isReservedParam(key)) {
					instance.reservedParams[key] = value;
				}
				else {
					instance.params[key] = value;
				}

				return instance;
			},

			setPlid: function(plid) {
				var instance = this;

				instance.reservedParams.p_l_id =  plid;

				return instance;
			},

			setPortletConfiguration: function(portletConfiguration) {
				var instance = this;

				/* Deprecate - it's not used for a longer time */

				// instance.options.portletConfiguration = portletConfiguration;

				return instance;
			},

			setPortletId: function(portletId) {
				var instance = this;

				instance.reservedParams.p_p_id = portletId;

				return instance;
			},

			setPortletMode: function(portletMode) {
				var instance = this;

				instance.reservedParams.p_p_mode = portletMode;

				return instance;
			},

			setResourceId: function(resourceId) {
				var instance = this;

				instance.reservedParams.p_p_resource_id = resourceId;

				return instance;
			},

			setSecure: function(secure) {
				var instance = this;

				instance.options.secure = secure;

				return instance;
			},

			setWindowState: function(windowState) {
				var instance = this;

				instance.reservedParams.p_p_state = windowState;

				return instance;
			},

			toString: function() {
				var instance = this;

				var options = instance.options;

				var reservedParams = instance.reservedParams;

				var resultURL = new A.Url(options.baseURL);

				var portletId = reservedParams.p_p_id;

				if (!portletId) {
					portletId = resultURL.getParameter('p_p_id');
				}

				var namespacePrefix = Util.getPortletNamespace(portletId);

				A.each(
					reservedParams,
					function(value, key) {
						if (value) {
							resultURL.setParameter(key, value);
						}
					}
				);

				A.each(
					instance.params,
					function(value, key) {
						if (value) {
							resultURL.setParameter(namespacePrefix + key, value);
						}
					}
				);

				if (options.secure) {
					resultURL.setProtocol('https');
				}

				if (options.escapeXML) {
					return Util.escapeHTML(resultURL.toString());
				}
				else {
					return resultURL.toString();
				}
			},

			_isReservedParam: function(paramName) {
				var instance = this;

				var result = false;

				A.each(
					instance.reservedParams,
					function(value, key) {
						if (key === paramName) {
							result = true;
						}
					}
				);

				return result;
			}
		};

		A.mix(
			PortletURL,
			{
				createActionURL: function() {
					return new PortletURL('ACTION_PHASE');
				},

				createPermissionURL: function(portletResource, modelResource, modelResourceDescription, resourcePrimKey) {
					var redirect = location.href;

					var portletURL = PortletURL.createRenderURL();

					portletURL.setDoAsGroupId(themeDisplay.getScopeGroupId());
					portletURL.setParameter('struts_action', '/portlet_configuration/edit_permissions');
					portletURL.setParameter('redirect', redirect);

					if (!themeDisplay.isStateMaximized()) {
						portletURL.setParameter('returnToFullPageURL', redirect);
					}

					portletURL.setParameter('portletResource', portletResource);
					portletURL.setParameter('modelResource', modelResource);
					portletURL.setParameter('modelResourceDescription', modelResourceDescription);
					portletURL.setParameter('resourcePrimKey', resourcePrimKey);
					portletURL.setPortletId(86);
					portletURL.setWindowState('MAXIMIZED');

					return portletURL;
				},

				createRenderURL: function() {
					return new PortletURL('RENDER_PHASE');
				},

				createResourceURL: function() {
					return new PortletURL('RESOURCE_PHASE');
				},

				createURL: function(baseURL, params) {
					return new PortletURL(null, params, baseURL);
				}
			}
		);

		Liferay.PortletURL = PortletURL;
	},
	'',
	{
		requires: ['aui-base', 'aui-io-request', 'aui-url', 'querystring-stringify-simple']
	}
);