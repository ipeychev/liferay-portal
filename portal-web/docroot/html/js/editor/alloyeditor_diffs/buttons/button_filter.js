YUI.add('button-filter', function(Y) {
    'use strict';

    var Lang = Y.Lang;

    var filters = [
        {
            name: 'X-PRO 2',
            filter: 'contrast(1.3) brightness(0.8) sepia(0.3) saturate(1.5) hue-rotate(-20deg)'
        },
        {
            name: 'Willow',
            filter: 'saturate(0.02) contrast(0.85) brightness(1.2) sepia(0.02)'
        },
        {
            name: 'Walden',
            filter: 'sepia(0.35) contrast(0.9) brightness(1.1) hue-rotate(-10deg) saturate(1.5)'
        },
        {
            name: 'Valencia',
            filter: 'sepia(0.15) saturate(1.5) contrast(0.9)'
        },
        {
            name: 'Toaster',
            filter: 'sepia(0.4) saturate(2.5) hue-rotate(-30deg) contrast(0.67)'
        },
        {
            name: 'Sutro',
            filter: 'brightness(0.75) contrast(1.3) sepia(0.5) hue-rotate(-25deg)'
        },
        {
            name: 'Sierra',
            filter: 'contrast(0.8) saturate(1.2) sepia(0.15)'
        },
        {
            name: 'Rise',
            filter: 'saturate(1.4) sepia(0.25) hue-rotate(-15deg) contrast(0.8) brightness(1.1)'
        },
        {
            name: 'Nashville',
            filter: 'sepia(0.4) saturate(1.5) contrast(0.9) brightness(1.1) hue-rotate(-15deg)'
        },
        {
            name: 'Kelvin',
            filter: 'sepia(0.4) saturate(2.4) brightness(1.3) contrast(1)'
        },
        {
            name: 'Inkwell',
            filter: 'grayscale(1) brightness(1.2) contrast(1.05)'
        },
        {
            name: 'Earlybird',
            filter: 'sepia(0.4) saturate(1.6) contrast(1.1) brightness(0.9) hue-rotate(-10deg)'
        },
        {
            name: 'Brannan',
            filter: 'sepia(0.5) contrast(1.4)'
        },
        {
            name: '1977',
            filter: 'sepia(0.5) hue-rotate(-30deg) saturate(1.2) contrast(0.8)'
        }
    ];

    /**
     *
     */
    var Filter = Y.Base.create('filter', Y.Plugin.Base, [Y.ButtonBase], {
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

            var currentFilter = element.getAttribute('data-filterid') || 0;

            element.setStyle('filter', filters[currentFilter].filter);
            element.setStyle('-webkit-filter', filters[currentFilter].filter);

            element.setAttribute('data-filterid', ((currentFilter + 1) % filters.length));
        },

        TPL_CONTENT: '<i class="icon icon-adjust"></i>'
    }, {
        NAME: 'filter',

        NS: 'filter',

        ATTRS: {
            strings: {
                validator: Lang.isObject,
                value: {
                    label: 'Filter'
                }
            }
        }
    });

    Y.ButtonFilter = Filter;

}, '', {
    requires: ['button-base']
});