#!/usr/bin/env python3

import argparse
import json
import os
import re
import subprocess

HERE = os.path.dirname(os.path.abspath(__file__))
TESTS_DIR = 'modules/apps/commerce/commerce-product-test/src/testFunctional/tests/'


def main():
	parser = argparse.ArgumentParser()
	parser.add_argument('group', type=int)
	parser.add_argument('--poshi', action='store_true')
	args = parser.parse_args()

	repo = run('git', 'rev-parse', '--show-toplevel').strip()
	manifest = json.load(open(os.path.join(HERE, 'GROUPS.json')))
	group = next(g for g in manifest['groups'] if g['group'] == args.group)
	tag = group['tag']
	base = manifest['base']

	print(f"# Group {group['group']}: {group['name']}\n")
	print(f'Tag: {tag}\nOriginal base: {base}\n')

	log = run('git', 'log', '--reverse', '--format=%h\t%s', f'{base}..HEAD')
	commits = [line.split('\t', 1) for line in log.splitlines() if line.split('\t', 1)[1].startswith(tag[1:] + ' ')]

	print('## Commits\n')

	for sha, title in commits:
		print(f'- {sha} {title}')

	for commit in group['commits']:
		if commit['body']:
			print(f"  - {commit['title'].split(' ', 1)[1]}: {commit['body']}")

	print(f"\n## Removed Poshi Tests ({len(group['poshiRemoved'])})\n")

	for entry in group['poshiRemoved']:
		print(f'- {entry}')

	if group['testcaseFilesDeleted']:
		print(f"\nDeleted .testcase files: {', '.join(group['testcaseFilesDeleted'])}")

	print('\n## Tests To Run\n')

	for kind, task in (('integration', 'testIntegration'), ('junit', 'test')):
		for module, classes in (group[kind] or {}).items():
			filters = []

			for name, methods in classes.items():
				filters += [f"--tests '*.{name}.{method}'" for method in methods] or [f"--tests '*.{name}'"]

			print(f"- {kind}: (cd {module} && {repo}/gradlew {task} {' '.join(filters)})")

	for path in group['jest']:
		module = path.split('/test/')[0]
		print(f"- jest: (cd {module} && USE_REACT_16=true npx --no-install node-scripts test {path.split(module + '/')[1]})")

	if group['playwrightTests']:
		print(f"- playwright: (cd modules/test/playwright && npx playwright test --grep '{tag}(?![0-9])')")

		for test in group['playwrightTests']:
			print(f"  - {test['spec'].split('/tests/', 1)[1]}: {test['title']}")

	if group['helpers']:
		print('\n## Shared Helpers Added or Changed\n')

		for path in group['helpers']:
			print(f'- {path}')

	if args.poshi:
		print('\n## Poshi Sources at the Original Base\n')

		for entry in group['poshiRemoved']:
			file, name = entry.split('::')
			body = poshi_test(run('git', 'show', f'{base}:{TESTS_DIR}{file}.testcase'), name)
			print(f'### {entry}\n\n```\n{body or "(not found at base)"}\n```\n')


def poshi_test(source, name):
	lines = source.split('\n')
	start = next((i for i, line in enumerate(lines) if re.match(r'\s*test ' + name + r' \{', line)), None)

	if start is None:
		return None

	first = start

	while first > 0 and lines[first - 1].strip().startswith('@'):
		first -= 1

	depth = 0

	for end in range(start, len(lines)):
		depth += lines[end].count('{') - lines[end].count('}')

		if depth == 0:
			break

	return '\n'.join(lines[first:end + 1])


def run(*args):
	return subprocess.run(args, capture_output=True, text=True).stdout


if __name__ == '__main__':
	main()