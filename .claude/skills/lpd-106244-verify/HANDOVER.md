# LPD-106244 Verification Handover

This guide hands over the verification of the LPD-106244 branch. The branch migrates commerce Poshi tests to Jest, JUnit, integration and Playwright tests. The migrations were written and statically checked, but none of the new tests has been run against a bundle. The goal of the handover is to take each group, confirm that the migration is faithful to the Poshi test it replaces, confirm that the new test passes and is correct, and then prepare the group to be sent as a pull request without duplicating work that already exists upstream.

## Branch Overview

- **Branch:** `LPD-106244`, based on `d41da6abe7d93`, which is an ancestor of liferay/liferay-portal master.

- **Commits:** 227 migration commits in 32 contiguous groups, followed by this handover commit. The handover commit must never be part of a pull request.

- **Scope:** tests, Playwright page objects and API helpers, Java integration and JUnit tests, and Jest tests. There are no production code changes, so no module needs to be deployed to verify the branch.

- **Removed Poshi tests:** 347 tests, including 7 whole `.testcase` files, from `modules/apps/commerce/commerce-product-test/src/testFunctional/tests`. 220 Poshi tests remain; see `REMAINING.md`.

- **Group identifiers:** each commit title starts with a temporary `LPD-106244-Grouped-N` prefix, and each Playwright test a group added or extended carries the tag `@LPD-106244-Grouped-N`. The prefix will be replaced with real tickets later; see `GROUPED-IDENTIFIERS.md`.

## Files in This Directory

| File | Purpose |
| --- | --- |
| `check_overlaps.py` | Reports, per group, overlaps with the latest liferay/liferay-portal master and with open pull requests on liferay-commerce/liferay-portal. |
| `GROUPED-IDENTIFIERS.md` | Explains the `LPD-106244-Grouped-N` placeholders and how to replace them with real tickets. |
| `GROUPS.json` | Machine-readable manifest of every group: commits, removed Poshi tests, deleted `.testcase` files, tests to run and shared helpers. |
| `HANDOVER.md` | This guide. |
| `REMAINING.md` | The Poshi tests still in place and why. |
| `replace_grouped.py` | Replaces the placeholders with real tickets in commit titles and files in one rebase. |
| `RISKS.md` | Runtime assumptions recorded by the authors of each migration. Read the section for a group before running it. |
| `show_group.py` | Prints one group's commits, removed Poshi tests, test commands and helpers. With `--poshi`, it also prints each removed Poshi test from the original base. |
| `SKILL.md` | Entry point for Claude Code (`/lpd-106244-verify <group>`). |

## Setup

1. Check out the branch:

	```bash
	git fetch <remote-with-the-branch> LPD-106244
	git checkout LPD-106244
	```

1. Update to the latest liferay/liferay-portal master before verifying anything, so the tests run against current code. In this repository the liferay/liferay-portal remote is normally named `upstream`:

	```bash
	git fetch upstream master
	git rebase upstream/master
	```

	Resolve any conflicts by keeping upstream changes and reapplying the branch change on top. If a Poshi test that a commit removes no longer exists on master, another change already handled it; see "Overlap Checks" below.

1. Build a bundle from the rebased branch with `ant all`, configure its database and `portal-ext.properties` as usual, and start it. If `portal-kernel` compilation fails on missing `FinderPath` members, run `ant install-portal-snapshot` in `portal-kernel` first; this only updates the local Maven repository.

1. Prepare Playwright. Use Node 22 and the local Playwright binary from `modules/test/playwright`. The tests read the bundle URL and credentials from the Playwright environment configuration; the defaults are `http://localhost:8080` and `test@liferay.com` with password `test`.

1. Commerce Playwright specs have two local prerequisites. Some specs look up the "Sales Agent" role, which only exists after a commerce site initializer has run once in the company. Also, no manually created Minium site may remain in the instance, because its fixed product external reference codes collide with the Minium sites that some upstream tests create; delete both the site and its catalog.

## Overlap Checks

Other people are migrating the same Poshi suite at the same time. Before starting work on a group, and again immediately before sending a group as a pull request, always recheck both of these for overlaps so that no duplicate pull request is sent:

- **Open pull requests:** https://github.com/liferay-commerce/liferay-portal/pulls

- **Latest master:** liferay/liferay-portal master. Update to it first (`git fetch upstream master` and `git rebase upstream/master`), then check.

`check_overlaps.py` automates both checks. It compares the branch against your local `upstream/master` ref (pass `--master <ref>` if your remote has a different name) and uses the GitHub CLI (`gh`) to read the open pull requests on liferay-commerce/liferay-portal. It does not fetch anything, so update to the latest master before running it:

```bash
git fetch upstream master
python3 .claude/skills/lpd-106244-verify/check_overlaps.py --group <N>
```

Omit `--group` to check every group. For each group it prints **CLEAR** or **OVERLAP**, and for an overlap it lists:

- Poshi tests the group removes that are already gone from master.

- Files the group changes that master has also changed since the original base.

- Open pull requests that remove the same Poshi tests or change the same test files.

Resolve every overlap before sending the group:

- **Same Poshi test removed by master or an open pull request:** do not send a second migration. Compare the two migrations, and if the other one is merged or clearly further along, drop that part of our group (remove the commit or the test from it). If ours covers something the other misses, coordinate with that pull request's author instead of sending a competing change.

- **Same file changed:** usually harmless, but rebase and rerun the group's tests, because upstream changes to a shared spec or page object can break our tests.

The script is a helper, not the source of truth. Also skim the open pull request list by hand for migrations of the same feature area that use different file names.

## Verifying a Group

Work through the groups in order. Later groups build on helpers added by earlier groups, so group N may not compile without groups 1 to N-1.

1. Print the group summary and the removed Poshi sources:

	```bash
	python3 .claude/skills/lpd-106244-verify/show_group.py <N> --poshi
	```

1. Run the overlap check for the group and resolve anything it reports.

1. Read the group's section in `RISKS.md`.

1. Review each commit (`git show <sha>`) against the Poshi tests it removes:

	- Every assertion in the Poshi test is present in the new test, or its absence is justified (for example, the assertion is covered by an existing test the commit names, or it checked something that no longer exists).

	- The setup reaches the same state as the Poshi setup. The new test does not use `miniumSetUp` or `classicCommerceSetUp`, directly, through a helper or through a spec-level hook.

	- The test is in the right layer: pure logic in Jest or JUnit, services and persistence in integration tests, and only real UI flows in Playwright.

	- For a "Drop ... already covered" commit, the commit body names the covering test. Open it and confirm it genuinely covers the dropped Poshi test with dedicated assertions, not by incidental overlap.

1. Run every test the summary lists. Integration tests need the bundle running. For Playwright, filter by the group tag. The `(?![0-9])` suffix stops `Grouped-1` from also matching `Grouped-10` to `Grouped-19`:

	```bash
	cd modules/test/playwright
	npx playwright test --grep '@LPD-106244-Grouped-<N>(?![0-9])' --workers=1
	```

	Run with one worker first to separate test defects from parallel interference, then once with the default worker count.

1. Confirm that each new test can fail. For each new or extended test, temporarily change one key expected value (a price, a count, a label) and confirm the test fails, then restore it. A test that still passes is vacuous and must be fixed.

1. Fix what fails, following the rules below, and rerun until the group passes.

1. Record the result using the report template below.

## Fix Rules

- **Fold fixes into the commit that introduced the code.** Create a fixup commit and autosquash it, so each group stays self-contained:

	```bash
	git commit --fixup=<sha-of-the-introducing-commit>
	GIT_SEQUENCE_EDITOR=: git rebase -i --autosquash upstream/master
	```

	The handover commit stays on top after the rebase.

- **Commit titles** follow one of four patterns, with the group placeholder as the prefix: `Migrate <subject> test(s) to <Playwright|Jest|integration|JUnit>`, `Drop <subject> test(s) already covered` (with a "Covered by ..." body), `Add <thing> to <page object or helper>`, and `Extract <thing> helper in <test class>`. Measure the 72-character limit as if the prefix were `LPD-106244 `, because the `-Grouped-N` part is temporary.

- **Deletion rule:** a Poshi test, including an `@ignore` test, may only be removed when it has dedicated, verified coverage or when it is obsolete. If a migrated test cannot be made to pass faithfully and the behavior is a product defect, restore the Poshi test in that commit, report the defect, and leave the test for later.

- **No comments** in test code other than license headers and `@author` tags. In long Playwright tests, phase markers are allowed only when every phase of the test has one.

- **Tags:** keep the `@LPD-106244-Grouped-N` tag on every Playwright test the group added or extended, next to the Poshi source ticket when there is one, for example `{tag: ['@COMMERCE-9181', '@LPD-106244-Grouped-23']}`.

- **No miniumSetUp or classicCommerceSetUp** in any test we write or extend. Use `apiStorefrontSetUp` or API helpers instead. If an upstream spec-level hook uses Minium, add our test as a standalone test with its own API setup rather than rewriting the shared hook.

- **Format** every change with the `format-source` skill (or `format-source-complete` if you have it) before committing, and rerun `tsc` for Playwright changes:

	```bash
	cd modules/test/playwright
	node_modules/.bin/tsc --noEmit -p tsconfig.json
	```

## Sending a Group as a Pull Request

1. Update to the latest liferay/liferay-portal master and rerun the overlap check for the group. Recheck https://github.com/liferay-commerce/liferay-portal/pulls by hand as well.

1. Replace the group's placeholder with its real ticket, either with `replace_grouped.py` (a mapping that contains only this group) or by hand in the commit titles and the group's Playwright tags.

1. Build the pull request branch from latest master with only the verified commits. If the group depends on a helper added by an earlier group that is not yet merged, either send the earlier group first or include that helper commit and say so in the pull request description.

1. Never include the handover commit.

1. Run the repository's `pr-check` skill if it is available, then open the pull request against liferay-commerce/liferay-portal.

## Report Template

Write one report per group, for example in `.claude/skills/lpd-106244-verify/reports/group-<N>.md`, and share it with the branch owner.

```markdown
# Group <N>: <Name>

- **Verified on:** <date>, rebased onto upstream/master <sha>
- **Overlap check:** CLEAR, or the overlaps found and how each was resolved
- **Result:** PASS, PASS WITH FIXES, or BLOCKED

## Tests

| Test | Layer | Result | Notes |
| --- | --- | --- | --- |
| <spec or class and title> | Playwright | Pass | Fails as expected when the price is changed |

## Fixes

- <sha> <what changed and why>

## Faithfulness

- <Poshi test>: all assertions covered, or what differs and why that is acceptable

## Risks Confirmed or Disproved

- <item from RISKS.md>: confirmed or disproved

## Product Defects Found

- <description, reproduction, and whether the Poshi test was restored>
```

## Verification Status

Update this table as groups are verified.

| Group | Area | Status |
| --- | --- | --- |
| 1 | CommerceSmoke file removal | Not started |
| 2 | CommerceDiscountCheckout file removal | Not started |
| 3 | CPCommerceOptions file removal | Not started |
| 4 | CommerceTermsAndConditions file removal | Not started |
| 5 | CPCommerceTermsAndConditions file removal | Not started |
| 6 | Units of measure | Not started |
| 7 | Channel administration | Not started |
| 8 | Channel services | Not started |
| 9 | Account management | Not started |
| 10 | Account selector | Not started |
| 11 | Storefront orders | Not started |
| 12 | Product definition services | Not started |
| 13 | Product search | Not started |
| 14 | Product options and SKUs | Not started |
| 15 | Product replacements | Not started |
| 16 | Admin catalog | Not started |
| 17 | Product media | Not started |
| 18 | Virtual products | Not started |
| 19 | Quick add and mini cart | Not started |
| 20 | Product comparison and details | Not started |
| 21 | Storefront search and sort | Not started |
| 22 | Commerce service and unit tests | Not started |
| 23 | Product workflow and admin | Not started |
| 24 | SKUs, subscriptions and media | Not started |
| 25 | Price on application | Not started |
| 26 | Quick add, mini cart and compare | Not started |
| 27 | Channels | Not started |
| 28 | Search and facets | Not started |
| 29 | Checkout and accounts | Not started |
| 30 | Display pages and templates | Not started |
| 31 | Bundle rules, options and product details | Not started |
| 32 | Virtual products and SKU settings | Not started |

At the time of the handover, `check_overlaps.py` reported overlaps with open pull requests for 20 of the 32 groups, so expect to resolve overlaps for most groups before sending them.