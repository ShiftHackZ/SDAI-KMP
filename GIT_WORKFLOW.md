# Git Workflow

This repository uses a release branch plus integration branch model.

- `master` is the released App Store / Google Play line.
- `develop` is the integration and experiment line.
- Feature branches are created from `develop`.
- Release tags are created from `master`.
- Hotfixes may start from `master`, but must be brought back into `develop`.

## Branch Roles

### master

`master` must represent the latest released or review-submitted state.

Use it for:

- App Store / Google Play review builds.
- Release tags.
- Compliance hotfixes.
- GitHub Pages published content.

Do not land experimental features directly into `master`.

### develop

`develop` is the normal integration branch.

Use it for:

- Feature PR targets.
- Experimental providers and UI work.
- Integration testing before a release train moves to `master`.

Do not configure GitHub Pages to deploy from `develop`.

### feature branches

Create feature branches from `develop`:

```bash
git fetch origin
git switch develop
git pull --ff-only
git switch -c feature/my-feature
```

Feature PRs target `develop`.

Feature PRs may be squash-merged into `develop`. This keeps the integration branch readable and makes generated documentation conflicts easier to replace with one final generated output.

Use `--force-with-lease`, not plain `--force`, when refreshing a feature branch after a rebase:

```bash
git push --force-with-lease origin feature/my-feature
```

## Release Flow: develop to master

Before moving `develop` to `master`, let Update Dokka refresh `develop`, or run
the workflow manually for `develop` to check immediately.

Expected release preparation:

1. Merge the planned feature PRs into `develop`.
2. Wait for the automated Dokka commit, or run Update Dokka manually for `develop`.
3. Run release validation from `develop`.
4. Fast-forward `master` from `develop`.
5. Tag the release from `master`.

The generated input marker moves with the release, so unchanged inputs on
`master` do not cause another generation job.

The release merge from `develop` to `master` must preserve commit object IDs. Do not use GitHub's PR merge buttons for `develop -> master`.

GitHub's merge UI can create new server-side commits or rewrite commits depending on the selected merge strategy:

- Squash merge creates a new commit.
- Rebase merge creates new commits with new object IDs.
- Merge commit preserves the commits being merged, but creates a GitHub-generated merge commit and makes the branch tips differ.

For this project, the release goal is stricter: the commits that are in `develop` should appear in `master` with the same object IDs. Use a local fast-forward release.

```bash
git fetch origin
git switch master
git pull --ff-only origin master
git merge --ff-only origin/develop
git push origin master
```

After the push, tag the release from `master`:

```bash
git tag vX.Y.Z
git push origin vX.Y.Z
```

If `git merge --ff-only origin/develop` fails, `master` and `develop` have diverged. Do not use GitHub UI to solve it. First sync the `master`-only commits back into `develop`.

```bash
git switch develop
git pull --ff-only origin develop
git merge --no-ff origin/master
git push origin develop
```

Then retry the fast-forward release from `develop` to `master`.

## Hotfix Flow

Hotfixes start from `master` when the release or review-submitted app needs a compliance, store, or production fix.

```bash
git fetch origin
git switch master
git pull --ff-only origin master
git switch -c hotfix/compliance-fix
```

After review and validation, land the hotfix into `master` with a local fast-forward whenever possible:

```bash
git switch master
git pull --ff-only origin master
git merge --ff-only hotfix/compliance-fix
git push origin master
```

Then sync the hotfix back into `develop` without rewriting it:

```bash
git switch develop
git pull --ff-only origin develop
git merge --no-ff origin/master
git push origin develop
```

This preserves the hotfix commit object ID and makes the next release fast-forwardable.

## GitHub Pages

GitHub Pages must never deploy from `develop`.

The allowed Pages source is the released line:

- GitHub Pages source: `master` / `docs`.
- If a Pages workflow is added later, restrict it to `master` or release tags only.
- Do not add `develop` to any Pages deployment trigger.

Allowed workflow trigger shape:

```yaml
on:
  push:
    branches: [master]
```

If manual deployment is added, the job must still guard against `develop`:

```yaml
if: github.ref == 'refs/heads/master'
```

Update Dokka commits generated output on `master` and `develop`. A manual run
can also commit it to a selected feature branch. Publishing is restricted to
`master`.

## Generated Dokka Documentation

Generated Dokka output lives under `docs/docs`.

Ordinary feature work should leave `docs/docs` generation to the automated
workflow after merge. Manual Update Dokka runs on feature branches are available
when a committed preview is needed.

Preferred feature shape:

1. Implementation commits.
2. Manual docs or README updates when they are part of the feature.
3. No generated Dokka commit.

Feature PR CI may run Dokka as a validation step, but the generated output should stay as a CI artifact or temporary local output, not as committed source.

After merges, Update Dokka checks both integration branches every 15 minutes
and waits for a 10-minute quiet period. It generates only when source or build
inputs differ from the last successful documentation build, then commits to the
same branch. Manual runs on any branch check immediately. See
[Documentation Workflow](DOCUMENTATION.md#automatic-and-manual-updates) for
credentials, controls and local generation.

If an old feature branch already contains a generated Dokka commit, drop or skip that commit while rebasing onto `develop`:

```bash
git fetch origin
git switch feature/old-feature
git rebase origin/develop
```

If the rebase conflicts only in the stale Dokka commit:

```bash
git rebase --skip
git push --force-with-lease origin feature/old-feature
```

If the rebase has source conflicts, resolve source code first. Do not hand-edit generated Dokka HTML to resolve semantic API conflicts.

## Root Markdown Documents

Root-level Markdown files are part of the public project documentation surface.

`README.md` must link to every other root-level `.md` document so users and contributors can discover repository policies and manuals from one place.

When adding, removing, or renaming a root-level `.md` file:

1. Update the root documentation section in `README.md`.
2. Keep the link text human-readable.
3. Do not include generated documentation output in this rule; it applies only to root-level Markdown files committed by maintainers.

## Current Migration Note

At the time this workflow was introduced, two feature branches were already based on `master`:

- `feature/new-functionality-A`
- `feature/new-functionality-B`

Both were created before the release-only Dokka rule. If either branch contains generated Dokka updates under `docs/docs`, drop or replace those generated docs during rebase and let the release Dokka PR regenerate them once after feature integration.

Recommended order:

1. Rebase both branches onto `develop`.
2. Remove generated Dokka changes from the feature branches.
3. Open both PRs against `develop`.
4. Merge both PRs into `develop`.
5. Let Update Dokka refresh the final `develop`, or trigger it manually.
6. Run the normal smoke tests.
7. Fast-forward `master` from `develop` for the next release.
