# 0.2.0 release gate

This is a checklist, not evidence that the tag is already published. Do not create the `0.2.0` tag until every required check below is complete on the final integrated source tree.

- [x] Unit tests, lint, release AAR, sample build, and Maven Local publication on the current feature branch
- [x] API 36.1 emulator: library and sample connected suites passed on 2026-10-03 (13 library tests + 4 sample activity tests), covering off-center focal zoom, accessibility child action and touch target, sample edit/undo/redo, collapse/expand, pinch/pan, and viewport restoration
- [x] Genuine API 36 sample screenshot and edit/zoom GIF captured in `docs/images/`
- [x] Standalone JitPack consumer project builds against published `0.1.1` as harness validation
- [ ] API 26 and API 36 GitHub Actions instrumentation matrix: workflow now runs both library and sample connected suites and uploads reports; checks are pending the PR run
- [x] Fetch `origin`, confirm `origin/main` is still the branch base (`627bae4`, exact same commit; no merge needed), and run the updated connected suites on the current tree
- [ ] Reviewer/new developer follows README alone to install, run, and apply a custom card
- [ ] Publish `0.2.0` tag only after those checks; verify JitPack `0.2.0` POM and AAR and run `./gradlew -p consumer-smoke :app:assembleDebug` without the version override, install, and launch it
- [x] Apache-2.0 `LICENSE` and README license links are present in `origin/main` (`627bae4`)
- [ ] Verify GitHub recognizes Apache-2.0 in repository metadata
- [ ] Update the README release status and JitPack link after the `0.2.0` artifact is publicly verified; README currently says the release is prepared but unpublished
- [ ] Upload `docs/images/social-preview.png` through repository Settings → Social preview → Edit; this upload has not been completed

The source sample must continue to use `implementation(project(":compose-mindmap"))`; the consumer fixture is intentionally separate from the source Gradle project
