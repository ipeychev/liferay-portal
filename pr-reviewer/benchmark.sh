#!/bin/bash

#
# Measure how well review.sh catches planted violations.
#
# A fixture plants one violation per rule and records each one in a manifest,
# a JSON array of {file, line, rule, note} objects. This script reviews the
# range that adds the fixture, matches every reported violation to the manifest
# by file, rule, and line, and prints the recall per planted violation, the
# false positives, and the time per run. With several runs it also shows how
# stable each catch is, since the model is not deterministic.
#
# Usage:
# benchmark.sh [--group <label>] [--manifest <file>]... [--runs <count>] [--staged] [--tolerance <lines>] [<range>]
#
# With --group only that rule group of review.sh runs, the same call pr-check
# makes for it, and only the planted violations of its rules are scored, so
# tuning one group does not pay for the others.
#
# The range defaults to the last commit that touched the first manifest, which is
# the commit that adds or updates the fixture, and --staged reviews the index
# instead, for a fixture that is not committed yet. The
# manifests default to every expected*.json under modules/apps/pr-reviewer-fixture.
#

set -o errexit
set -o nounset
set -o pipefail

function main {
	local diff_range=""
	local group_args=()
	local manifests=()
	local runs=1
	local tolerance=5

	while [ ${#} -gt 0 ]
	do
		case ${1} in
			--group)
				group_args=(--group ${2})

				shift
				;;
			--manifest)
				manifests+=(${2})

				shift
				;;
			--runs)
				runs=${2}

				shift
				;;
			--staged)
				diff_range=--staged
				;;
			--tolerance)
				tolerance=${2}

				shift
				;;
			--*)
				echo "Unknown option ${1}." >&2

				exit 2
				;;
			*)
				diff_range=${1}
				;;
		esac

		shift
	done

	local repo_root

	repo_root=$(git rev-parse --show-toplevel)

	cd ${repo_root}

	if [ ${#manifests[@]} -eq 0 ]
	then
		manifests=($(ls modules/apps/pr-reviewer-fixture/expected*.json 2> /dev/null || true))
	fi

	if [ ${#manifests[@]} -eq 0 ]
	then
		echo "No manifest found. Pass --manifest <file>." >&2

		exit 2
	fi

	if [[ -z ${diff_range} ]]
	then
		local fixture_commit

		fixture_commit=$(git log --format=%H --max-count=1 -- ${manifests[0]})

		diff_range="${fixture_commit}~1..${fixture_commit}"
	fi

	local work_dir

	work_dir=$(mktemp --directory)

	trap "rm --force --recursive ${work_dir}" EXIT

	jq --slurp '[.[][]] | sort_by([.rule, .file, .line])' "${manifests[@]}" > ${work_dir}/expected.json

	local expected_count

	expected_count=$(jq "length" ${work_dir}/expected.json)

	#
	# The manifests are the answer key, so they stay out of the reviewed diff, and
	# so does this script, which the commit that adds the fixture may also add.
	#

	local exclude_args=(--exclude pr-reviewer/benchmark.sh)
	local manifest

	for manifest in "${manifests[@]}"
	do
		exclude_args+=(--exclude ${manifest})
	done

	if [ ${#group_args[@]} -eq 0 ]
	then
		echo "Benchmarking ${runs} run(s) over ${diff_range} against ${expected_count} planted violation(s) from ${manifests[*]}."
	else
		echo "Benchmarking ${runs} run(s) of the ${group_args[1]} group over ${diff_range} against the violations planted for its rules in ${manifests[*]}."
	fi
	echo ""

	local run

	for ((run = 1; run <= runs; run++))
	do
		local seconds=$(date +%s)

		local status=0

		pr-reviewer/review.sh --json "${exclude_args[@]}" "${group_args[@]}" ${diff_range} > ${work_dir}/run-${run}.json 2> ${work_dir}/run-${run}.err || status=${?}

		if [ ${status} -eq 2 ] || ! jq --exit-status ".violations" ${work_dir}/run-${run}.json > /dev/null 2>&1
		then
			echo "Run ${run} produced no verdict: $(head --bytes=300 ${work_dir}/run-${run}.err)" >&2

			exit 1
		fi

		jq --argjson run ${run} --argjson seconds $(($(date +%s) - seconds)) '{cost: .cost, run: $run, seconds: $seconds, tokens: .tokens, violations: [.violations[] | .file |= sub("^[ab]/"; "")]}' ${work_dir}/run-${run}.json > ${work_dir}/scored-${run}.json

		echo "Run ${run}: $(jq --raw-output '"\(.violations | length) violation(s) in \(.seconds)s, \(.tokens.input + .tokens.cache_write + .tokens.cache_read) input tokens (\(.tokens.cache_read) cached, \(.tokens.cache_write) cache writes), \(.tokens.output) output tokens, $\(.cost * 100 | round / 100)"' ${work_dir}/scored-${run}.json)"
	done

	echo ""

	#
	# A run reports the rules it reviewed, so a run limited to one group is scored
	# only against the violations planted for that group.
	#

	jq --slurpfile run ${work_dir}/run-1.json '[.[] | select(.rule as $rule | $run[0].rules | index($rule))]' ${work_dir}/expected.json > ${work_dir}/expected-reviewed.json

	mv ${work_dir}/expected-reviewed.json ${work_dir}/expected.json

	_print_summary
}

function _print_summary {
	local summary_json

	summary_json=$(jq --slurp --argjson tolerance ${tolerance} --slurpfile expected ${work_dir}/expected.json '
		def matches($e; $r):
			($e.file == $r.file) and ($e.rule == $r.rule) and (($e.line - $r.line) | fabs) <= $tolerance;

		def rule_matches($e; $r):
			($e.file == $r.file) and ($e.rule == $r.rule);

		. as $runs
		| ($expected[0]) as $expected
		| ($runs | length) as $runs_count
		| {
			runs: [
				$runs[]
				| . as $run
				| {
					run: .run,
					seconds: .seconds,
					found: [$expected[] | . as $e | select(any($run.violations[]; matches($e; .)))] | length,
					false_positives: [$run.violations[] | . as $r | select(any($expected[]; rule_matches(.; $r)) | not)] | length
				}
			],
			expected: [
				$expected[]
				| . as $e
				| {
					rule: .rule,
					file: (.file | sub("^modules/apps/pr-reviewer-fixture/pr-reviewer-fixture-impl/"; "")),
					line: .line,
					found_runs: [$runs[] | select(any(.violations[]; matches($e; .)))] | length,
					rule_runs: [$runs[] | select(any(.violations[]; rule_matches($e; .)))] | length
				}
			],
			false_positives: (
				[$runs[] | .violations[] | . as $r | select(any($expected[]; rule_matches(.; $r)) | not)]
				| group_by(.rule)
				| map({rule: .[0].rule, count: length, sample: ((.[0].file | sub("^modules/apps/pr-reviewer-fixture/pr-reviewer-fixture-impl/"; "")) + ":" + (.[0].line | tostring) + " " + .[0].message)})
				| sort_by(-.count)
			),
			runs_count: $runs_count
		}
	' ${work_dir}/scored-*.json)

	echo "${summary_json}" | jq --raw-output '
		.runs_count as $runs_count
		| (.expected | length) as $expected_count
		| (.runs | map(.found) | add / length) as $mean_found
		| (.runs | map(.false_positives) | add / length) as $mean_false_positives
		| (.runs | map(.seconds) | add / length) as $mean_seconds
		| (.expected | map(.rule_runs) | add / $runs_count) as $mean_rule_found
		| (.expected | map(select(.found_runs == $runs_count)) | length) as $always
		| (.expected | map(select(.found_runs == 0)) | length) as $never
		| "Recall: \($mean_found * 100 / $expected_count | floor)% (\($mean_found) of \($expected_count) per run, \($always) always found, \($never) never found), \($mean_rule_found * 100 / $expected_count | floor)% ignoring the line, \($mean_false_positives * 10 | round / 10) false positive(s) per run, \($mean_seconds | floor)s per run",
		"",
		"Rule  Found  Line  File",
		(.expected[] | "\(.rule)   \(.found_runs)/\($runs_count)\(if .rule_runs > .found_runs then "*" else " " end)   \(.line | tostring | .[0:5] + " " * (5 - length))\(.file)"),
		"",
		"An asterisk marks a rule the reviewer reported on the right file and rule but outside the line tolerance in at least one run.",
		"",
		(if (.false_positives | length) > 0 then
			"False positives by rule (over all runs):",
			(.false_positives[] | "\(.rule)   \(.count)   \(.sample)")
		else
			"No false positives."
		end)
	'
}

main "${@}"