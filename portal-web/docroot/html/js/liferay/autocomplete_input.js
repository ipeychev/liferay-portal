AUI.add(
	'liferay-autocomplete-input',
	function(A) {
		var Lang = A.Lang;

		var AArray = A.Array;

		var ARROW_KEYS = [
			37,
			38,
			39,
			40
		];

		var KEY_ARROW_DOWN = 40;

		var KEY_ARROW_LEFT = 37;

		var KEY_ARROW_RIGHT = 39;

		var REGEX_NEW_LINE = /\r\n/g;

		var STR_INPUT_NODE = 'inputNode';

		var STR_REG_EXP = 'regExp';

		var STR_SOURCE = 'source';

		var STR_SPACE = ' ';

		var STR_TERM = 'term';

		var STR_TPL_RESULTS = 'tplResults';

		var STR_VISIBLE = 'visible';

		var AutoCompleteInput = A.Component.create(
			{
				ATTRS: {
					acConfig: {
						validator: Lang.isObject,
						value: {
							activateFirstItem: true,
							resultFilters: 'phraseMatch',
							resultHighlighter: 'phraseMatch'
						}
					},

					inputNode: {
						setter: function(value) {
							var res = A.one(value);

							return res;
						},
						writeOnce: true
					},

					regExp: {
						setter: '_setRegExp',
						value: '(?:\\sterm|^term)([^\\s]+)'
					},

					source: {
					},

					term: {
						validator: Lang.isString,
						value: '@'
					},

					tplResults: {
						validator: Lang.isString
					}
				},

				EXTENDS: A.Base,

				NAME: 'liferay-autocomplete-input',

				prototype: {
					initializer: function() {
						var instance = this;

						var ac = new A.AutoComplete(instance._getACConfig()).render();

						ac.on('query', instance._onACQuery, instance);

						ac._keys[KEY_ARROW_DOWN] = A.bind(instance._onACKeyDown, instance);

						ac._updateValue = A.bind(instance._acUpdateValue, instance);

						instance.ac = ac;

						instance._bindUI();
					},

					destructor: function() {
						var instance = this;

						instance.ac.destroy();
					},

					_acResultFormatter: function(query, results) {
						var instance = this;

						var tplResults = instance.get(STR_TPL_RESULTS);

						return AArray.map(
							results,
							function(result) {
								var userData = result.raw;

								return Lang.sub(tplResults, userData);
							}
						);
					},

					_acUpdateValue: function(text) {
						var instance = this;

						var cursorPos = instance._getCursorPos();

						if (cursorPos) {
							var inputNode = instance.get(STR_INPUT_NODE);

							var val = inputNode.val();

							if (val) {
								var lastTermIndex = instance._getPrevTermIndex(val, cursorPos.start);

								if (lastTermIndex >= 0) {
									var prefix = val.substring(0, lastTermIndex);

									val = val.substring(lastTermIndex);

									var regExp = instance.get(STR_REG_EXP);

									var res = regExp.exec(val);

									if (res) {
										var restText = val.substring(res[1].length + 1);

										if (restText.length === 0 || restText.charAt(0) !== STR_SPACE) {
											text += ' ';

											var spaceAdded = true;
										}

										var resultText = prefix + instance.get(STR_TERM) + text;

										var newVal = resultText + restText;

										var resultEndPos = resultText.length + (spaceAdded ? 0 : 1);

										inputNode.val(newVal);

										instance._setCursorPos(inputNode, resultEndPos);
									}
								}
							}
						}
					},

					_bindUI: function() {
						var instance = this;

						var inputNode = instance.get(STR_INPUT_NODE);

						inputNode.on('key', A.bind(instance._onKeyUp, instance), 'up:' + ARROW_KEYS.join());
					},

					_getACConfig: function() {
						var instance = this;

						var acConfig = instance.get('acConfig');

						var tplResults = instance.get(STR_TPL_RESULTS);

						if (tplResults) {
							acConfig.resultFormatter = A.bind(instance._acResultFormatter, instance);
						}

						var input = instance.get(STR_INPUT_NODE);

						if (input) {
							acConfig.inputNode = input;
						}

						var source = instance.get(STR_SOURCE);

						if (source) {
							acConfig.source = source;
						}

						return acConfig;
					},

					_getPrevTermIndex: function(content, position) {
						var instance = this;

						var result = -1;

						var term = instance.get(STR_TERM);

						for (var i = position; i >= 0; --i) {
							if (content.charAt(i) === term) {
								result = i;

								break;
							}
						}

						return result;
					},

					_getQuery: function(val) {
						var instance = this;

						var result = null;

						var cursorPos = instance._getCursorPos();

						if (cursorPos) {
							val = val.substring(0, cursorPos.start);

							var term = instance.get(STR_TERM);

							var lastTermIndex = val.lastIndexOf(term);

							if (lastTermIndex >= 0) {
								val = val.substring(lastTermIndex);

								var regExp = instance.get(STR_REG_EXP);

								var res = regExp.exec(val);

								if (res && (res.index + res[1].length + term.length === val.length)) {
									result = val;
								}
							}
						}

						return result;
					},

					_onACKeyDown: function() {
						var instance = this;

						if (instance.ac.get(STR_VISIBLE)) {
							instance.ac._activateNextItem();
						}
						else {
							return false;
						}
					},

					_onACQuery: function(event) {
						var instance = this;

						var input = instance._getQuery(event.query);

						if (input) {
							event.query = input.substring(1);
						}
						else {
							event.preventDefault();

							if (instance.ac.get(STR_VISIBLE)) {
								instance.ac.hide();
							}
						}
					},

					_onKeyUp: function(event) {
						var instance = this;

						var acVisible = instance.ac.get(STR_VISIBLE);

						if (!acVisible) {
							instance._processKeyUp(event);
						}
						else if(event.keyCode === KEY_ARROW_LEFT || event.keyCode === KEY_ARROW_RIGHT) {
							instance._processKeyUp(event);
						}
					},

					_processKeyUp: function(event) {
						var instance = this;

						var inputNode = instance.get(STR_INPUT_NODE);

						var input = instance._getQuery(inputNode.val());

						if (input) {
							input = input.substring(1);

							instance.ac.sendRequest(input);
						}
						else if (instance.ac.get(STR_VISIBLE)) {
							instance.ac.hide();
						}
					},

					_setRegExp: function(value) {
						var instance = this;

						var term = instance.get(STR_TERM);

						return new RegExp(value.replace(/term/g, term));
					}
				}
			}
		);

		Liferay.AutoCompleteInput = AutoCompleteInput;
	},
	'',
	{
		requires: ['aui-base', 'autocomplete', 'autocomplete-filters', 'autocomplete-highlighters']
	}
);
