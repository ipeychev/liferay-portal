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
						value: '(?:\\sterm|^term)[^\\s]+$'
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
								inputNode: inputNode,
								maxResults: instance.get('maxResults'),
								on: {
									query: A.bind(instance._onACQuery, instance),
									select: A.bind(instance._onACSelect, instance)
								},
								source: instance.get('source')
							}
						).render();

						instance.ac = ac;

						console.log(instance.get('source'));
					},

					_getCursorPos: function(node) {
						var result = null;

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

					_onACSelect: function(event) {
						
					},

					_onACQuery: function(event) {
						var instance = this;

						var input = instance._processQuery(event.query);

						event.query = input;

						console.log(event.query);
					},

					_onKeyUp: function(event) {
						var instance = this;

						var inputNode = instance.get('input');

						instance._processQuery(inputNode.val());
					},

					_processQuery: function(val) {
						var instance = this;

						var result = null;

						var inputNode = instance.get('input');

						var cursorPos = instance._getCursorPos(inputNode);

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
		requires: ['aui-base', 'autocomplete', 'aui-event-input']
	}
);
