---

argument-hint: <group number, or "overlaps" or "status">
description: Verify one group of the LPD-106244 Poshi migration branch. Checks for overlaps with upstream master and open liferay-commerce pull requests, reviews each migrated test against the Poshi test it replaces, runs the tests, fixes failures in the introducing commits and writes a group report. Use when working on the LPD-106244 branch or asked to verify an LPD-106244-Grouped-N group.
name: lpd-106244-verify

---

# LPD-106244 Group Verification

The LPD-106244 branch migrates commerce Poshi tests to Jest, JUnit, integration and Playwright tests in 32 groups. None of the new tests has been run against a bundle. This skill verifies one group at a time.

Read `HANDOVER.md` in this directory before the first run. It holds the full context: branch overview, setup, overlap policy, fix rules, how to send a group as a pull request and the report template. Everything below is the short procedure.

## Arguments

- `<N>`: verify group N.

- `overlaps`: run the overlap check for every group and summarize the results.

- `status`: print the Verification Status table from `HANDOVER.md`.

## Procedure for Group N

1. Confirm the working tree is clean and the current branch contains the LPD-106244 commits (`git log --oneline | grep -m1 "LPD-106244-Grouped-"`).

1. Ask the user to update to the latest liferay/liferay-portal master if they have not done so in this session (`git fetch upstream master` and `git rebase upstream/master`). Do not fetch or rebase without their confirmation, and do not create or repoint remotes yourself.

1. Run the overlap check and stop to report if it finds anything that needs a decision:

	```bash
	python3 .claude/skills/lpd-106244-verify/check_overlaps.py --group <N>
	```

	Also remind the user to skim https://github.com/liferay-commerce/liferay-portal/pulls for migrations of the same feature area under different file names.

1. Print the group summary with the Poshi sources, and read the group's section in `RISKS.md`:

	```bash
	python3 .claude/skills/lpd-106244-verify/show_group.py <N> --poshi
	```

1. Review every commit in the group with `git show <sha>` against the Poshi tests it removes. Check assertion coverage, setup equivalence, the absence of `miniumSetUp` and `classicCommerceSetUp`, the test layer, and, for each "Drop ... already covered" commit, that the named covering test genuinely covers the dropped test.

1. Run the tests listed in the summary. Integration, JUnit and Playwright tests need the bundle running; ask the user before starting, stopping or restarting a server. Run Playwright with `--grep '@LPD-106244-Grouped-<N>(?![0-9])' --workers=1` first, then with the default worker count.

1. For each new or extended test, temporarily change one key expected value, confirm the test fails, and restore it.

1. Fix failures with `git commit --fixup=<introducing-sha>` followed by `GIT_SEQUENCE_EDITOR=: git rebase -i --autosquash upstream/master`. Follow the Fix Rules in `HANDOVER.md`: title patterns, the deletion rule, no comments, tags, no Minium setup, `format-source` and `tsc`.

1. Write `reports/group-<N>.md` from the template in `HANDOVER.md`, update the group's row in the Verification Status table, and summarize the result for the user.

## Guardrails

- Never include the handover commit (`LPD-106244 Add verification handover guide ...`) in a pull request, and never replace the group placeholders unless the user provides the real tickets.

- Never push, open a pull request, or write to Jira without the user's explicit confirmation.

- If a migrated test exposes a product defect, do not weaken the assertion to make it pass. Report the defect, and restore the Poshi test in the introducing commit if the migration cannot stay faithful.