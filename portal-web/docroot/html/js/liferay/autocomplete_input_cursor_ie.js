AUI.add(
	'liferay-autocomplete-input-cursor-ie',
	function(A) {
		var STR_CHARACTER = 'character';

		var STR_END_TO_END = 'EndToEnd';

		var STR_INPUT_NODE = 'inputNode';

		var AutcompleteInputCursor = function(){};

		AutcompleteInputCursor.prototype = {
			_getCursorPos: function(node) {
				var instance = this;

				node = node || instance.get(STR_INPUT_NODE);

				var input = node.getDOMNode();

				var docNode = A.getDoc().getDOMNode();

				var start = 0;
				var end = 0;

				var normalizedValue, range, textInputRange, len, endRange;

				range = docNode.selection.createRange();

				if (range && range.parentElement() === input) {
					len = input.value.length;
					normalizedValue = input.value.replace(/\r\n/g, '\n');

					textInputRange = input.createTextRange();
					textInputRange.moveToBookmark(range.getBookmark());

					endRange = input.createTextRange();
					endRange.collapse(false);

					if (textInputRange.compareEndPoints('StartToEnd', endRange) > -1) {
						start = end = len;
					} else {
						start = -textInputRange.moveStart(STR_CHARACTER, -len);
						start += normalizedValue.slice(0, start).split('\n').length - 1;

						if (textInputRange.compareEndPoints(STR_END_TO_END, endRange) > -1) {
							end = len;
						} else {
							end = -textInputRange.moveEnd(STR_CHARACTER, -len);
							end += normalizedValue.slice(0, end).split('\n').length - 1;
						}
					}
				}

				return {
					start: start,
					end: end
				};
			},

			_setCursorPos: function(node, cursorPos) {
				var instance = this;

				node = node || instance.get(STR_INPUT_NODE);

				var input = node.getDOMNode();

				if (input.createTextRange) {
					var val = node.val().substring(0, cursorPos);

					var count = 0;

					var regExpNewLine = /\r\n/g;

					while (regExpNewLine.exec(val) !== null) {
						count++;
					}

					var range = input.createTextRange();

					range.move(STR_CHARACTER, cursorPos - count);

					range.select();
				}
			}
		};

		A.Base.mix(Liferay.AutoCompleteInput, [AutcompleteInputCursor]);
	},
	'',
	{
		requires: ['liferay-autocomplete-input']
	}
);