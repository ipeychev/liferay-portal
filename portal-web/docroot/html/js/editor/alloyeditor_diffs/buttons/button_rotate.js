YUI.add('button-rotate', function(Y) {
    'use strict';

    var Lang = Y.Lang;

    var ROTATE_REGEX = /rotate\((\d+)deg\)/;

    /**
     *
     */
    var Rotate = Y.Base.create('rotate', Y.Plugin.Base, [Y.ButtonBase], {
        /**
         *
         * @method _onClick
         * @protected
         * @param {EventFacade} event Event that triggered when user clicked on the button.
         */
        _onClick: function(event) {
            var instance = this,
                btnInst,
                editor,
                element;

            btnInst = event.target;

            editor = instance.get('host').get('editor');

            element = editor.getSelection().getSelectedElement() || editor.getSelection().getStartElement();

            var transform = element.getStyle('transform');

            var angle = 90;

            var hasRotation = ROTATE_REGEX.test(transform);

            if (hasRotation) {
                angle += Number(ROTATE_REGEX.exec(transform)[1]);
            }

            element.setStyle('transform', 'rotate(' + angle + 'deg)');
        },

        TPL_CONTENT: '<i class="icon icon-rotate-right"></i>'
    }, {
        NAME: 'rotate',

        NS: 'rotate',

        ATTRS: {
            strings: {
                validator: Lang.isObject,
                value: {
                    label: 'Rotate'
                }
            }
        }
    });

    Y.ButtonRotate = Rotate;

}, '', {
    requires: ['button-base']
});