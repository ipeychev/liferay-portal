# PR Reviewer Fixture

This module holds a small internal service that manages fixture entries, each with a name, a count, and a status. The reviewer benchmark runs against the module, so every source file plants a set of known violations and the manifest under `modules/apps/pr-reviewer-fixture/` records them.

## Entries

An entry is identified by its name.  The count records how many times the entry has been probed, and the status marks the entry as active or stale.

The service looks up an entry by its id when a probe reports back.

Stale entries are removed by a long-running cleanup task.

## Exports

The maintenance script under `scripts` lists the entries in an export and prunes the old exports in a directory. Run it from the module root:

```bash
scripts/fixture.sh list exports/fixture-entries-20260925.json
```

## Cleanup

Cleanup the export directory before you run the benchmark again, since a stale export skews the count.

The benchmark doesn't count a violation the manifest omits.

Run the benchmark from repository root.

## Manifest

One fragment file per agent, merged before scoring.

Each fragment is a JSON array with one object per planted violation. The
benchmark merges the fragments before it scores a run, and a line number
that drifted after the fragment was written counts a catch as a miss.
