define("js-es6-demo@1.0.0/sequences.es", ["exports", "./fibonacci.es", "./triangle.es", "./square.es"], function(exports, _fibonacciEs, _triangleEs, _squareEs) {
    "use strict";

    Object.defineProperty(exports, "__esModule", {
        value: true
    });

    function _interopRequireDefault(obj) {
        return (obj && obj.__esModule ? obj : {
            "default": obj
        });
    }

    var _fibonacci = _interopRequireDefault(_fibonacciEs);
    var _triangle = _interopRequireDefault(_triangleEs);
    var _square = _interopRequireDefault(_squareEs);
    exports.F = _fibonacci["default"];
    exports.T = _triangle["default"];
    exports.S = _square["default"];
});