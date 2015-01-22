import {log as logMigue} from 'html/js/es6-out/migue';
import {log as logChema} from 'html/js/es6-out/chema';

function log(test) {
	logMigue(test);
}

export {logChema as log};