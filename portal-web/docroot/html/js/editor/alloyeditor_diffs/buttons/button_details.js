YUI.add('button-details', function(Y) {
    'use strict';

    var Lang = Y.Lang;

    /**
     *
     */
    var Details = Y.Base.create('details', Y.Plugin.Base, [Y.ButtonBase], {
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
                element,
                details,
                detailsElement,
                detailsElementId,
                i;

            btnInst = event.target;

            editor = instance.get('host').get('editor');

            element = editor.getSelection().getSelectedElement() || editor.getSelection().getStartElement();

            detailsElementId = element.getAttribute('data-detailsid');

            if (!detailsElementId) {
                detailsElementId = new Date().getTime();
                element.setAttribute('data-detailsid', detailsElementId);
            }

            detailsElement = document.getElementById(detailsElementId);

            if (!detailsElement) {
                details = '<table class="table">';
                for (i in element.$.dataset) {
                    details += '<tr><td>' + i +'</td><td>' + element.$.dataset[i] + '</td></tr>';
                }
                details += '</table>';

                detailsElement = document.createElement('div');
                detailsElement.setAttribute('id', detailsElementId);
                detailsElement.style.position = 'absolute';
                detailsElement.style.top = '0';
                detailsElement.style.left = '450px';
                detailsElement.innerHTML = details;

                element.$.parentNode.style.position = 'relative';
                element.$.parentNode.appendChild(detailsElement);
            }

            if (btnInst.get('pressed')) {
                detailsElement.style.display = 'block';
            } else {
                detailsElement.style.display = 'none';
            }
        },

        TPL_CONTENT: '<i class="icon icon-list"></i>'
    }, {
        NAME: 'details',

        NS: 'details',

        ATTRS: {
            strings: {
                validator: Lang.isObject,
                value: {
                    label: 'Details'
                }
            }
        }
    });

    Y.ButtonDetails = Details;

}, '', {
    requires: ['button-base']
});