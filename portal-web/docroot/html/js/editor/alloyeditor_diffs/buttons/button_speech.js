AUI.add(
	'button-speech',
	function(A) {
		var Lang = A.Lang;

		var STR_EDITOR = 'editor';

		var STR_HOST = 'host';

		var BtnSpeech = A.Base.create(
			'imageselector',
			A.Plugin.Base,
			[A.ButtonBase],
			{
				TPL_CONTENT: '<i class="icon icon-microphone"></i>',

				TPL_SPEECH: '<h1>{text}</h1>',

				initializer: function() {
					var instance = this;

					var speechRecognition = new webkitSpeechRecognition();

					speechRecognition.onresult = A.bind('_onSpeechResult', instance);

					instance._speechTPL = new CKEDITOR.template(instance.TPL_SPEECH);

					instance._speechRecognition = speechRecognition;
				},

				_onClick: function(event) {
					var instance = this;

					instance._speechRecognition.start();
				},

				_onSpeechResult: function(event) {
					var instance = this;

					var editor = instance.get(STR_HOST).get(STR_EDITOR);

					var speechElement = CKEDITOR.dom.element.createFromHtml(
						instance._speechTPL.output(
							{
								text: event.results[0][0].transcript
							}
						)
					);

					editor.focus();
					instance.fire('actionPerformed');

					A.soon(A.bind('insertElement', editor, speechElement));
				}
			},
			{
				NAME: 'speech',

				NS: 'speech'
			}
		);

		A.ButtonSpeech = BtnSpeech;
	},
	'',
	{
		requires: ['button-base', 'timers']
	}
);