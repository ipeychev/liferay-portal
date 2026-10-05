#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

# Manage the fixture entry exports. The list command prints the entries in one export, and the prune command removes the oldest exports in a directory beyond the retention count.

function _die {
	echo "${*}" >&2

	exit 1
}

function main {
	local command="${1:-}"

	case "${command}" in
		list)
			_list_entries "${@:2}"
			;;
		prune)
			_prune_exports "${@:2}"
			;;
		*)
			_die "Unknown command \"${command}\""
			;;
	esac
}

function _list_entries {
	local export="${1:-}"
	local status="${2:-active}"

	if [ -z "${export}" ]
	then
		_die "Pass the export file as the first argument"
	fi

	if ! jq --exit-status .entries "${export}" >/dev/null 2>&1
	then
		_die "The export ${export} has no entries member"
	fi

	jq --arg status "${status}" \
		--raw-output \
		'.entries[] | select(.status == $status) | "\(.name)\t\(.count)"' \
		"${export}"
}

function _prune_exports {
	local export_dir="${1:-}"
	local retain="${2:-5}"

	if [[ -z ${export_dir} ]]
	then
		_die "Pass the directory that holds the exports"
	fi

	if [[ ! -d ${export_dir} ]]
	then
		_die "The export directory ${export_dir} does not exist."
	fi

	shopt -s nullglob

	local exports=("${export_dir}"/fixture-entries-*.json)

	local excess=$((${#exports[@]} - retain))

	local pruned_count=0
	local kept_count=0

	local export

	for export in "${exports[@]}"
	do
		if [ ${pruned_count} -lt ${excess} ]
		then
			rm -f "${export}"

			((pruned_count++))
		else
			((++kept_count))
		fi
	done

	echo "Kept ${kept_count} and pruned ${pruned_count} export(s) in ${export_dir}"

	_publish_summary "${kept_count}" "${pruned_count}"
}

function _publish_summary {
	local kept_count="${1}"
	local pruned_count="${2}"

	if [[ -z ${FIXTURE_WEBHOOK_URL:-} ]]
	then
		return
	fi

	curl \
		--data "{\"event\":\"prune\",\"kept\":${kept_count},\"pruned\":${pruned_count}}" \
		--fail \
		-H "Content-Type: application/json" \
		--show-error \
		--silent \
		"${FIXTURE_WEBHOOK_URL}"
}

main "${@}"