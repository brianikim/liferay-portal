#!/usr/bin/env python3

import json
import os
import re
import subprocess
import sys

REPO = subprocess.run(['git', 'rev-parse', '--show-toplevel'], capture_output=True, text=True).stdout.strip()
os.chdir(REPO)
STATE = os.path.join(REPO, '.git', 'replace-grouped')


def fix_files(files, mapping):
	for f in files:
		if not os.path.isfile(f):
			continue
		try:
			text = open(f).read()
		except UnicodeDecodeError:
			continue
		new = replace_text(text, mapping)
		if new != text:
			open(f, 'w').write(new)
			git('add', f)


def git(*args, check=True):
	return subprocess.run(['git', *args], capture_output=True, text=True, check=check).stdout


def load_mapping():
	return {int(k): v for k, v in json.load(open(os.path.join(STATE, 'mapping.json'))).items()}


def main():
	if sys.argv[1] == '--step':
		return step()
	os.makedirs(STATE, exist_ok=True)
	mapping = {str(k): v for k, v in json.load(open(sys.argv[1])).items()}
	json.dump(mapping, open(os.path.join(STATE, 'mapping.json'), 'w'))
	base = sys.argv[2] if len(sys.argv) > 2 else git('merge-base', 'HEAD', 'upstream/master').strip()
	todo = []
	for sha in git('rev-list', '--reverse', f'{base}..HEAD').split():
		todo.append(f'pick {sha}')
		todo.append(f'exec {sys.executable} {os.path.abspath(__file__)} --step')
	open(os.path.join(STATE, 'todo.txt'), 'w').write('\n'.join(todo) + '\n')
	env = dict(os.environ, GIT_SEQUENCE_EDITOR='cp ' + os.path.join(STATE, 'todo.txt'), GIT_EDITOR='true')
	subprocess.run(['git', 'rebase', '-q', '-i', base], env=env, capture_output=True)
	for _ in range(1000):
		if not os.path.isdir(os.path.join(REPO, '.git', 'rebase-merge')):
			break
		if git('diff', '--name-only', '--diff-filter=U').strip():
			resolve()
		subprocess.run(['git', 'rebase', '--continue'], env=env, capture_output=True)
	left = git('grep', '-c', 'LPD-106244-Grouped-', check=False).strip()
	titles = [t for t in git('log', '--format=%s', f'{base}..HEAD').splitlines() if 'Grouped-' in t]
	print('placeholders left in files:', left or 'none')
	print('titles still using a placeholder:', len(titles))


def replace_text(text, mapping):
	def tag(match):
		n = int(match.group(1))
		return mapping[n] if n in mapping else match.group(0)
	return re.sub(r'LPD-106244-Grouped-(\d+)', tag, text)


def resolve():
	mapping = load_mapping()
	sha = open(os.path.join(REPO, '.git', 'rebase-merge', 'stopped-sha')).read().strip()
	for f in git('diff', '--name-only', '--diff-filter=U').split():
		content = git('show', f'{sha}:{f}', check=False)
		open(f, 'w').write(replace_text(content, mapping))
		git('add', f)


def step():
	mapping = load_mapping()
	files = git('diff-tree', '--no-commit-id', '--name-only', '-r', 'HEAD').split()
	fix_files(files, mapping)
	message = git('log', '-1', '--format=%B')
	new_message = replace_text(message, mapping)
	staged = git('diff', '--cached', '--name-only').strip()
	if staged or new_message != message:
		open(os.path.join(STATE, 'msg.txt'), 'w').write(new_message)
		git('commit', '-q', '--amend', '--no-verify', '-F', os.path.join(STATE, 'msg.txt'))


if __name__ == '__main__':
	main()