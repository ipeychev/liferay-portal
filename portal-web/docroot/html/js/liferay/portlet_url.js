AUI.add(
	'liferay-portlet-url',
	function(A) {
		var PortletURL = function(baseURL, params, reservedParams) {
			var instance = this;

			instance.params = {};

			// Please see PortalImpl and PortletURLImpl

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
				baseURI: null,
				escapeXML: null,
				portletConfiguration: false,
				secure: null,
				urlSemicolonPart: null,
				urlHashPart: null
			};

			// Overwite params, reservedParams and options from baseURL

			instance._parseBaseURL();

			// Overwite params, reservedParams and options from provided variables

			instance.params = A.mix(instance.params, params);
			instance.reservedParams = A.mix(instance.reservedParams, reservedParams);
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

				if (lifecycle == 'ACTION_PHASE') {
					instance.reservedParams.p_p_lifecycle = '1';
				}
				else if (lifecycle == 'RENDER_PHASE') {
					instance.reservedParams.p_p_lifecycle = '0';
				}
				else if (lifecycle == 'RESOURCE_PHASE') {
					instance.reservedParams.p_p_lifecycle = '2';
					instance.reservedParams.p_p_cacheability = 'cacheLevelPage';
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

				instance.params[key] = value;

				return instance;
			},

			setPlid: function(plid) {
				var instance = this;

				instance.reservedParams.p_l_id =  plid;

				return instance;
			},

			setPortletConfiguration: function(portletConfiguration) {
				var instance = this;

				instance.options.portletConfiguration = portletConfiguration;

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
				var result = '';

				var secureURI = instance._buildSecureURI();
				result += secureURI;

				var urlParams = instance._buildURLParams();
				result += '?' + urlParams;

				if (instance.options.urlSemicolonPart != null) {
					result += ';' + instance.options.urlSemicolonPart;
				}

				if (instance.options.urlHashPart != null) {
					result += '#' + instance.options.urlHashPart;
				}

				if (instance.options.escapeXML) {
					result = Liferay.Util.escapeHTML(result);
				}

				return result;
			},

			_buildSecureURI: function() {
				var instance = this;

				var baseURI = instance.options.baseURI;

				// only upgrade security, newer downgrade

				if (instance.options.secure && Liferay.Util.startsWith(baseURI, 'http://')) {
					baseURI = 'https://' + baseURI.substring(7);
				}

				return baseURI;
			},

			_buildURLParams: function() {
				var instance = this;

				var result = '';

				var portletId = instance.reservedParams.p_p_id;

				var namespacePrefix = Liferay.Util.getPortletNamespace(portletId);

				var urlParams = A.merge({}, instance.reservedParams, instance.params);

				A.each(
					urlParams,
					function(value, key) {
						if (value == null) {
							return;
						}

						if (!Liferay.Util.startsWith(key, namespacePrefix) && !instance._isReservedParam(key)) {
							key = namespacePrefix + key;
						}

						result += encodeURIComponent(key) + '=' + encodeURIComponent(value) + '&';
					}
				);

				if (Liferay.Util.endsWith(result, '&')) {
					result = result.substring(0, result.length - 1);
				}

				return result;
			},

			_isReservedParam: function(paramName) {
				var instance = this;
				var result = false;

				A.each(
					instance.reservedParams,
					function(value, key) {
						if (key == paramName) {
							result = true;
						}
					}
				);

				return result;
			},

			_parseBaseURL: function() {
				var instance = this;

				var baseURL = instance.options.baseURL;

				if (baseURL == null) {
					baseURL = themeDisplay.getPathContext() + themeDisplay.getPathMain() + '/portal/layout';
					instance.reservedParams.p_l_id = themeDisplay.getPlid();
				}

				var hashIdx = baseURL.indexOf('#');
				if (hashIdx !== -1) {
					instance.options.urlHashPart = baseURL.substring(hashIdx + 1);
					baseURL = baseURL.substring(0, hashIdx);
				}

				var semicolonIdx = baseURL.indexOf(';');
				if (semicolonIdx !== -1) {
					instance.options.urlSemicolonPart = baseURL.substring(semicolonIdx + 1);
					baseURL = baseURL.substring(0, semicolonIdx);
				}

				var questionIdx = baseURL.indexOf('?');
				if (questionIdx === -1) {
					instance.options.baseURI = baseURL;
					return;
				}

				instance.options.baseURI = baseURL.substring(0, questionIdx);

				var params = baseURL.substring(questionIdx + 1).split('&');

				var paramsObj = {};

				A.each(
					params,
					function(value) {
						var kvp = value.split('=');
						var paramName = decodeURIComponent(kvp[0]);
						var paramValue = decodeURIComponent(kvp[1]);

						if (instance._isReservedParam(paramName)) {
							instance.reservedParams[paramName] = paramValue;
						}
						else {
							instance.setParameter(paramName, paramValue);
						}
					}
				);
			}
		};

		A.mix(
			PortletURL,
			{
				createActionURL: function() {
					var portletURL = new PortletURL();
					portletURL.setLifecycle('ACTION_PHASE');
					return portletURL;
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
					var portletURL = new PortletURL();
					portletURL.setLifecycle('RENDER_PHASE');
					return portletURL;
				},

				createResourceURL: function() {
					var portletURL = new PortletURL();
					portletURL.setLifecycle('RESOURCE_PHASE');
					return portletURL;
				},

				createURL: function(baseURL, params, reservedParams) {
					return new PortletURL(baseURL, params, reservedParams);
				}
			}
		);

		Liferay.PortletURL = PortletURL;
	},
	'',
	{
		requires: ['aui-base', 'aui-io-request', 'querystring-stringify-simple']
	}
);