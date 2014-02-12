AUI.add(
	'liferay-autocomplete-input',
	function(A) {
		var Lang = A.Lang;

		var AutoCompleteInput = A.Component.create(
			{
				ATTRS: {
					input: {
						setter: function(value) {
							var res = A.one(value);

							return res;
						},
						writeOnce: true
					},

					maxResults: {
						validator: Lang.isNumber,
						value: 5
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
					}
				},

				EXTENDS: A.Base,

				NAME: 'liferay-autocomplete-input',

				prototype: {
					initializer: function() {
						var instance = this;

						instance._bindUI();
					},

					destructor: function() {

					},

					_bindUI: function() {
						var instance = this;

						var inputNode = instance.get('input');

						inputNode.on('key', A.bind(instance._onKeyUp, instance), 'up:37,38,39,40');

						var ac = new A.AutoComplete(
							{
								activateFirstItem: true,
								inputNode: inputNode,
								maxResults: instance.get('maxResults'),
								on: {
									query: A.bind(instance._onACQuery, instance),
									select: A.bind(instance._onACSelect, instance)
								},
								resultFilters: 'phraseMatch',
    							resultHighlighter: 'phraseMatch',
								source: instance.get('source')
							}
						).render();

						instance.ac = ac;

						console.log(instance.get('source'));
					},

					_getCursorPos: function(node) {
						var instance = this;

						var result = null;

						node = node || instance.get('input');

						var input = node.getDOMNode();

						var docNode = A.getDoc().getDOMNode();

						if ('selectionStart' in input && docNode.activeElement === input) {
							result = {
								end: input.selectionEnd,
								start: input.selectionStart
							};
						}
						else if (input.createTextRange) {
							var sel = docNode.selection.createRange();

							if (sel.parentElement() === input) {
								var range = input.createTextRange();

								range.moveToBookmark(sel.getBookmark());

								for (var len = 0; range.compareEndPoints('EndToStart', range) > 0; range.moveEnd('character', -1)) {
									len++;
								}

								range.setEndPoint('StartToStart', input.createTextRange());

								var pos = {
									end: len,
									start: 0
								};

								for (; range.compareEndPoints('EndToStart', range) > 0; range.moveEnd('character', -1)) {
									pos.start++;

									pos.end++;
								}

								result = pos;
							}
						}

						return result;
					},

					_getPrevTermIndex: function(content, position) {
						var instance = this;

						var result = -1;

						var term = instance.get('term');

						for(var i = position; i >= 0; --i) {
							if (content.charAt(i) === term) {
								result = i;

								break;
							}
						}

						return result;
					},

					_onACSelect: function(event) {
						var instance = this;

						event.preventDefault();

						var rawResult = event.result.raw;

						var cursorPos = instance._getCursorPos();

						if (cursorPos) {
							var inputNode = instance.get('input');

							var val = inputNode.val();

							if (val) {
								//debugger;

								var lastTermIndex = instance._getPrevTermIndex(val, cursorPos.start);

								if (lastTermIndex >= 0) {
									var prefix = val.substring(0, lastTermIndex);

									val = val.substring(lastTermIndex);

									var regExp = instance.get('regExp');

									var res = regExp.exec(val);

									if (res) {
										var newVal = prefix + instance.get('term') + rawResult + val.substring(res[1].length + 1);

										inputNode.val(newVal);
									}
								}
							}
						}
					},

					_onACQuery: function(event) {
						var instance = this;

						var input = instance._getQuery(event.query);

						if (input) {
							event.query = input.substring(1);

							console.log('original query: ' + event.query);
						}
						else {
							console.log('original no query');

							event.preventDefault();

							if (instance.ac.get('visible')) {
								instance.ac.hide();
							}
						}
					},

					_onKeyUp: function(event) {
						var instance = this;

						var acVisible = instance.ac.get('visible');

						if (!acVisible) {
							instance._processKeyUp(event);
						}
						else if(event.keyCode === 37 || event.keyCode === 39) {
							instance._processKeyUp(event);
						}
					},

					_getQuery: function(val) {
						var instance = this;

						var result = null;

						var cursorPos = instance._getCursorPos();

						if (cursorPos) {
							val = val.substring(0, cursorPos.start);

							var term = instance.get('term');

							var lastTermIndex = val.lastIndexOf(term);

							if (lastTermIndex >= 0) {
								val = val.substring(lastTermIndex);

								var regExp = instance.get('regExp');

								if (regExp.test(val)) {
									console.log('valid: ' + val.substring(1));

									result = val;
								}
							}
						}

						return result;
					},

					_processKeyUp: function(event) {
						var instance = this;

						var inputNode = instance.get('input');

						var input = instance._getQuery(inputNode.val());

						if (input) {
							input = input.substring(1);

							console.log('key query: ' + input);

							instance.ac.sendRequest(input);
						}
						else if (instance.ac.get('visible')) {
							console.log('key no query');

							instance.ac.hide();
						}
					},

					_setRegExp: function(value) {
						var instance = this;

						var term = instance.get('term');

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
