define("js-es6-demo@1.0.0/square.es", ["exports", "module", "./sequence-base.es"], function(exports, module, _sequenceBaseEs) {
    "use strict";

    var _get = function get(_x, _x2, _x3) {
        var _again = true;

        _function:
        while (_again) {
            var object = _x, property = _x2, receiver = _x3;
            desc = parent = getter = undefined;
            _again = false;

            if (object === null)
                object = Function.prototype;

            var desc = Object.getOwnPropertyDescriptor(object, property);

            if (desc === undefined) {
                var parent = Object.getPrototypeOf(object);

                if (parent === null) {
                    return undefined;
                } else {
                    _x = parent;
                    _x2 = property;
                    _x3 = receiver;
                    _again = true;
                    continue _function;
                }
            } else if ("value" in desc) {
                return desc.value;
            } else {
                var getter = desc.get;

                if (getter === undefined) {
                    return undefined;
                }

                return getter.call(receiver);
            }
        }
    };

    function _interopRequireDefault(obj) {
        return (obj && obj.__esModule ? obj : {
            "default": obj
        });
    }

    var marked0$0 = [squareGenerator].map(regeneratorRuntime.mark);

    function _classCallCheck(instance, Constructor) {
        if (!(instance instanceof Constructor)) {
            throw new TypeError("Cannot call a class as a function");
        }
    }

    function _inherits(subClass, superClass) {
        if (typeof superClass !== "function" && superClass !== null) {
            throw new TypeError("Super expression must either be null or a function, not " + typeof superClass);
        }

        subClass.prototype = Object.create(superClass && superClass.prototype, {
            constructor: {
                value: subClass,
                enumerable: false,
                writable: true,
                configurable: true
            }
        });

        if (superClass)
            subClass.__proto__ = superClass;
    }

    var _Sequence2 = _interopRequireDefault(_sequenceBaseEs);

    function squareGenerator() {
        var currentVal, increase;

        return regeneratorRuntime.wrap(function squareGenerator$(context$1$0) {
            while (1) switch (context$1$0.prev = context$1$0.next) {
            case 0:
                currentVal = 0;
                increase = -1;
            case 2:
                if (!true) {
                    context$1$0.next = 9;
                    break;
                }

                increase += 2;
                currentVal += increase;
                context$1$0.next = 7;
                return currentVal;
            case 7:
                context$1$0.next = 2;
                break;
            case 9:
            case "end":
                return context$1$0.stop();
            }
        }, marked0$0[0], this);
    }

    var SquareSequence = function(_Sequence) {
        function SquareSequence() {
            _classCallCheck(this, SquareSequence);
            _get(Object.getPrototypeOf(SquareSequence.prototype), "constructor", this).call(this, squareGenerator, "Square");
        }

        _inherits(SquareSequence, _Sequence);
        return SquareSequence;
    }(_Sequence2["default"]);

    module.exports = SquareSequence;
});