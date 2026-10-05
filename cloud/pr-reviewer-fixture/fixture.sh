#!/usr/bin/env bash

function main {
	local export_dir="${1:-exports}"

	mkdir -p "${export_dir}"

	echo "Exporting the fixture entries to ${export_dir}"
}

main "${@}"