CKEDITOR.plugins.add( 'lfrpopup' );

AUI().use(
	'querystring-parse',
	function(A) {
		CKEDITOR.editor.prototype.popup = function( url, width, height, options ) {
			options = A.QueryString.parse(options);

			Liferay.Util.openWindow(
				{
					dialog: {
						zIndex: 12000
					},
					height: height,
					stack: false,
					title: options.title || '',
					uri: url,
					width: width
				}
			);
		}
	}
);