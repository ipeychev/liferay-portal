AUI.add(
	'liferay-autocomplete-input-cursor',
	function(A) {
		debugger;
		var AutcompleteInputCursor = function(){};

		AutcompleteInputCursor.prototype = {
			_getCursorPos: function(node) {
				var instance = this;

				node = node || instance.get('inputNode');

				var input = node.getDOMNode();

				return {
					start: input.selectionStart,
					end: input.selectionStart
				};
			},

			_setCursorPos: function(node, cursorPos) {
				var instance = this;

				node = node || instance.get(STR_INPUT_NODE);

				var input = node.getDOMNode();

				input.focus();

				input.setSelectionRange(cursorPos, cursorPos);
			}
		};

		A.Base.mix(Liferay.AutoCompleteInput, [AutcompleteInputCursor]);
	},
	'',
	{
		requires: ['liferay-autocomplete-input']
	}
);