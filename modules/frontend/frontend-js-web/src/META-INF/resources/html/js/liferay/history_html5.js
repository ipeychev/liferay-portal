AUI.add(
	'liferay-history-html5',
	function(A) {

		/**
		 * The HistoryHTML5 Component.
		 *
		 * @module liferay-history
		 * @submodule liferay-history-html5
		 */

		var AObject = A.Object;
		var History = Liferay.History;
		var Lang = A.Lang;
		var QueryString = A.QueryString;

		var isEmpty = AObject.isEmpty;
		var isValue = Lang.isValue;

		var WIN = A.config.win;

		var HISTORY = WIN.history;

		var LOCATION = WIN.location;

		A.mix(

			/**
			 * A base class for `A.HistoryHtml5`.
			 *
			 * @class A.History.Html5
			 * @param {Object} config Object literal specifying widget configuration
			 * properties.
			 * @constructor
			 */
			History.prototype,
			{
				PROTECTED_HASH_KEYS: [/^liferay$/, /^tab$/, /^_\d+_tab$/],

				/**
				 * Add 'state' and 'options' to the History. 
				 *
				 * @method add
				 * @param state
				 * @param options
				 * @return History.superclass.add.call(instance, state, options)
				 * @protected
				 */
				add: function(state, options) {
					var instance = this;

					options = options || {};

					options.url = options.url || instance._updateURI(state);

					state.liferay = true;

					return History.superclass.add.call(instance, state, options);
				},

				/**
				 * Construction lifecycle implementation executed during 
                                 * `HistoryHtml5` instantiation.
				 *
				 * @method _init
				 * @param config	
				 * @protected
				 */
				_init: function(config) {
					var instance = this;

					var hash = LOCATION.hash;

					var locationHashValid = hash.indexOf(History.VALUE_SEPARATOR) != -1;

					if (locationHashValid) {
						HISTORY.replaceState(null, null, instance._updateURI());
					}

					config = config || {};

					if (!config.hasOwnProperty('initialState')) {
						if (locationHashValid) {
							config.initialState = instance._parse(hash.substr(1));
						}

						History.superclass._init.call(instance, config);
					}
				},

				/**
				 * Update the URI data for the history.
				 *
				 * @method _updateURI
				 * @param state
				 */
				_updateURI: function(state) {
					var instance = this;

					var uriData = [
						LOCATION.search.substr(1),
						LOCATION.hash.substr(1)
					];

					var hash = uriData[1];
					var query = uriData[0];

					var queryMap = {};

					if (query) {
						queryMap = instance._parse(query);
					}

					if (!state && hash) {
						var hashMap = instance._parse(hash);

						if (!isEmpty(hashMap)) {
							var protectedHashMap = {};

							state = hashMap;

							A.each(
								state,
								function(value1, key1) {
									instance.PROTECTED_HASH_KEYS.forEach(
										function(value2, key2) {
											if (value2.test(key1)) {
												delete state[key1];
												protectedHashMap[key1] = value1;
											}
										}
									);
								}
							);

							uriData.pop();

							uriData.push('#', QueryString.stringify(protectedHashMap));
						}
					}

					A.mix(queryMap, state, true);

					AObject.each(
						queryMap,
						function(item, index) {
							if (!isValue(item)) {
								delete queryMap[index];
							}
						}
					);

					uriData[0] = QueryString.stringify(
						queryMap,
						{
							eq: History.VALUE_SEPARATOR,
							sep: History.PAIR_SEPARATOR
						}
					);

					uriData.unshift(LOCATION.protocol, '//', LOCATION.host, LOCATION.pathname, '?');

					return uriData.join('');
				}
			},
			true
		);
	},
	'',
	{
		requires: ['history-html5', 'liferay-history', 'querystring-stringify-simple']
	}
);