'use strict';

class Sequence {
	constructor(generatorFn, label = 'Base Sequence') {
		this.label = label;
		this._generator = new generatorFn();
	}

    next() {
        return this._generator.next();
    }
}

export default Sequence;