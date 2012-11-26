AUI.add(
	'liferay-xml-beautifier',
	function(A) {
		var Lang = A.Lang;

		var AArray = A.Array;

		var REGEX_TOKEN_1 = /(>)\s*(<)(\/*)/g;

		var REGEX_TOKEN_2 = /^<\/\w/;

		var REGEX_TOKEN_3 = /^<\w[^>]*[^\/]>.*$/;

		var STR_CHAR_CRLF = '\r\n';

		var STR_BLANK = '';

		var XMLBeautifier = A.Component.create(
			{
				EXTENDS: A.Base,

				NAME: 'xmlbeautifier',

				ATTRS: {
					lineIndent: {
						validator: Lang.isString,
						value: '\r\n'
					},

					lineSeparator: {
						setter: '_setTagSeparator',
						validator: '_validateTagSeparator',
						value: /\r?\n/g
					},

					tagIndent: {
						validator: Lang.isString,
						value: '\t'
					}
				},

				prototype: {
					beautify: function(content) {
						var instance = this;

						var tagIndent = instance.get('tagIndent');

						var lineIndent = instance.get('lineIndent');

						var lineSeparator = instance.get('lineSeparator');

						var pad = 0;

						var formatted = STR_BLANK;

						var lines = content.replace(REGEX_TOKEN_1, '$1' + lineIndent + '$2$3').split(lineSeparator);

						AArray.each(
							lines,
							function(item, index, collection) {
								var indent = 0;

								if (item.match(REGEX_TOKEN_2)) {
									if (pad !== 0) {
										pad -= 1;
									}
								}
								else if (item.match(REGEX_TOKEN_3)) {
									indent = 1;
								}

								formatted += instance._repeat(tagIndent, pad) + item + lineIndent;

								pad += indent;
							}
						);

						return formatted.replace(/^\s+|\s+$/g, STR_BLANK);
					},

					uglify: function(content) {
						return content.replace(/>\s{0,}</g, '><');
					},

					_repeat: function(separator, length) {
						return new Array(length + 1).join(separator);
					},

					_setTagSeparator: function(value) {
						if (Lang.isString(value)) {
							value = new RegExp(value);
						}

						return value;
					},

					_validateTagSeparator: function(value) {
						return Lang.isString(value) || value instanceof RegExp;
					}
				}
			}
		);

		Liferay.XMLBeautifier = XMLBeautifier;
	},
	'',
	{
		requires: ['aui-base']
	}
);