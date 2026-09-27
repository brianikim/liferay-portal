#!/usr/bin/env python3

import argparse
import json
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = subprocess.run(['git', 'rev-parse', '--show-toplevel'], capture_output=True, text=True).stdout.strip()
UPSTREAM = 'liferay-commerce/liferay-portal'
TESTS_DIR = 'modules/apps/commerce/commerce-product-test/src/testFunctional/tests/'


def changed_files(diff):
	return set(re.findall(r'^\+\+\+ b/(\S+)', diff, re.M)) | set(re.findall(r'^--- a/(\S+)', diff, re.M))


def main():
	parser = argparse.ArgumentParser()
	parser.add_argument('--group', type=int, action='append')
	parser.add_argument('--master', default='upstream/master')
	args = parser.parse_args()

	manifest = json.load(open(os.path.join(HERE, 'GROUPS.json')))
	base = run('git', 'merge-base', 'HEAD', manifest['base']).strip() or manifest['base']
	master = args.master

	if not run('git', 'rev-parse', '--verify', '--quiet', master).strip():
		sys.exit(f'Could not resolve {master}. Fetch the latest liferay/liferay-portal master first, or pass --master <ref>.')

	master_tests = set()

	for name in run('git', 'ls-tree', '--name-only', master, TESTS_DIR).split():
		if name.endswith('.testcase'):
			master_tests |= set(re.findall(r'^\s*test (\w+) \{', run('git', 'show', f'{master}:{name}'), re.M))

	master_changed = set(run('git', 'diff', '--name-only', f'{base}...{master}').split())

	prs = json.loads(run('gh', 'pr', 'list', '-R', UPSTREAM, '--state', 'open', '--limit', '300', '--json', 'number,title,author') or '[]')
	pr_data = []

	for pr in prs:
		diff = run('gh', 'pr', 'diff', str(pr['number']), '-R', UPSTREAM)
		pr_data.append((pr, removed_tests(diff), changed_files(diff)))

	report = []
	blocked = 0

	for group in manifest['groups']:
		if args.group and group['group'] not in args.group:
			continue

		prefix = f"LPD-106244-Grouped-{group['group']} "
		files = set()

		for sha in run('git', 'log', '--format=%H', f'--grep=^{re.escape(prefix)}', f'{base}..HEAD').split():
			files |= set(run('git', 'show', '--name-only', '--format=', sha).split())

		tests = {entry.split('::')[1] for entry in group['poshiRemoved']}
		findings = []

		gone = sorted(tests - master_tests)

		if gone:
			findings.append(f"Upstream master no longer has these Poshi tests (someone else removed or migrated them): {', '.join(gone)}")

		touched = sorted(f for f in files & master_changed if not f.endswith('.testcase'))

		if touched:
			findings.append(f"Upstream master changed files this group also changes (rebase conflicts or duplicate work likely): {', '.join(touched)}")

		for pr, pr_tests, pr_files in pr_data:
			same_tests = sorted(tests & pr_tests)
			same_files = sorted(f for f in files & pr_files if not f.endswith('.testcase'))

			if same_tests:
				findings.append(f"PR #{pr['number']} ({pr['author']['login']}: {pr['title']}) also removes: {', '.join(same_tests)}")
			elif same_files:
				findings.append(f"PR #{pr['number']} ({pr['author']['login']}: {pr['title']}) also changes: {', '.join(same_files)}")

		status = 'OVERLAP' if findings else 'CLEAR'
		blocked += bool(findings)
		report.append(f"## Group {group['group']}: {group['name']} [{status}]\n")
		report.extend(f'- {finding}' for finding in findings)
		report.append('')

	print(f'Compared against {master} ({run("git", "log", "-1", "--format=%h %cs", master).strip()}) and {len(prs)} open PRs on {UPSTREAM}.\n')
	print('\n'.join(report))
	print(f'{blocked} group(s) with overlaps.')


def removed_tests(diff):
	return set(re.findall(r'^-\s*test (\w+) \{', diff, re.M))


def run(*args, check=False):
	return subprocess.run(args, capture_output=True, text=True, cwd=REPO, check=check).stdout


if __name__ == '__main__':
	main()