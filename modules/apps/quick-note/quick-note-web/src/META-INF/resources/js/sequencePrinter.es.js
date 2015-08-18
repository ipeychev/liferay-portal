'use strict';

import {F as fibonacci, T as triangle, S as square} from 'js-es6-demo/sequences.es';

class SequencePrinter {
	constructor(container, limit = 10) {
		this.container = container;
		this.limit = limit;

		this._sequences = {
			fibonacci: new fibonacci(),
			square: new square(),
			triangle: new triangle()
		}
	}

	print() {
		let content = '<table class="table table-stripped"><thead><tr><th>Sequence</th><th>Length</th><th>Items</th></tr></thead><tbody>';

		let sequencesContent = Object.keys(this._sequences).map(sequenceName => this.getSequenceContent(this._sequences[sequenceName]))

		content += sequencesContent.join();
		content += '</tbody></table>';

		this.container.innerHTML = content;
	}

	getSequenceContent(sequence) {
		let sequenceContent = `<tr><th scope="row">${sequence.label}</th><td>${this.limit}</td><td>`

		for (let i = 0; i < this.limit; i++) {
			let item = sequence.next();

			sequenceContent += `<strong>${item.value}</strong>, `;
		}

		sequenceContent += '</td></tr>';

		return sequenceContent;
	}
}

export default SequencePrinter;