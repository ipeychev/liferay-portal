define("js-es6-demo@1.0.0/sequence-base.es", ["exports", "module"], function(exports, module) {
    "use strict";

    var _createClass = function() {
        function defineProperties(target, props) {
            for (var i = 0; i < props.length; i++) {
                var descriptor = props[i];
                descriptor.enumerable = descriptor.enumerable || false;
                descriptor.configurable = true;

                if ("value" in descriptor)
                    descriptor.writable = true;

                Object.defineProperty(target, descriptor.key, descriptor);
            }
        }

        return function(Constructor, protoProps, staticProps) {
            if (protoProps)
                defineProperties(Constructor.prototype, protoProps);

            if (staticProps)
                defineProperties(Constructor, staticProps);

            return Constructor;
        };
    }();

    function _classCallCheck(instance, Constructor) {
        if (!(instance instanceof Constructor)) {
            throw new TypeError("Cannot call a class as a function");
        }
    }

    var Sequence = function() {
        function Sequence(generatorFn) {
            var label = (arguments[1] === undefined ? "Base Sequence" : arguments[1]);
            _classCallCheck(this, Sequence);
            this.label = label;
            this._generator = new generatorFn();
        }

        _createClass(Sequence, [{
            key: "next",

            value: function next() {
                return this._generator.next();
            }
        }]);

        return Sequence;
    }();

    module.exports = Sequence;
});