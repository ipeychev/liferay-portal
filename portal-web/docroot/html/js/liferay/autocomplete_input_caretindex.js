AUI.add(
	'liferay-autocomplete-input-caretindex',
	function(A) {
		var STR_INPUT_NODE = 'inputNode';

		var AutcompleteInputCaretIndex = function(){};

		AutcompleteInputCaretIndex.prototype = {
			_getCaretIndex: function(node) {
				var instance = this;

				node = node || instance.get(STR_INPUT_NODE);

				var input = node.getDOMNode();

				return {
					start: input.selectionStart,
					end: input.selectionStart
				};
			},

			_setCaretIndex: function(node, caretIndex) {
				var instance = this;

				node = node || instance.get(STR_INPUT_NODE);

				var input = node.getDOMNode();

				input.focus();

				input.setSelectionRange(caretIndex, caretIndex);
			}
		};

		A.Base.mix(Liferay.AutoCompleteInput, [AutcompleteInputCaretIndex]);
	},
	'',
	{
		requires: ['liferay-autocomplete-input']
	}
);