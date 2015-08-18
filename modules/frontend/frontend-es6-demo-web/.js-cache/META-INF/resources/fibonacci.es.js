define("js-es6-demo@1.0.0/fibonacci.es", ["exports", "module", "./sequence-base.es"], function(exports, module, _sequenceBaseEs) {
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

    var marked0$0 = [fibonacciGenerator].map(regeneratorRuntime.mark);

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

    function fibonacciGenerator() {
        var val1, val2, swap;

        return regeneratorRuntime.wrap(function fibonacciGenerator$(context$1$0) {
            while (1) switch (context$1$0.prev = context$1$0.next) {
            case 0:
                val1 = 0, val2 = 1, swap = undefined;
                context$1$0.next = 3;
                return val1;
            case 3:
                context$1$0.next = 5;
                return val2;
            case 5:
                if (!true) {
                    context$1$0.next = 13;
                    break;
                }

                swap = val1 + val2;
                val1 = val2;
                val2 = swap;
                context$1$0.next = 11;
                return swap;
            case 11:
                context$1$0.next = 5;
                break;
            case 13:
            case "end":
                return context$1$0.stop();
            }
        }, marked0$0[0], this);
    }

    var FibonacciSequence = function(_Sequence) {
        function FibonacciSequence() {
            _classCallCheck(this, FibonacciSequence);
            _get(Object.getPrototypeOf(FibonacciSequence.prototype), "constructor", this).call(this, fibonacciGenerator, "Fibonacci");
        }

        _inherits(FibonacciSequence, _Sequence);
        return FibonacciSequence;
    }(_Sequence2["default"]);

    module.exports = FibonacciSequence;
});