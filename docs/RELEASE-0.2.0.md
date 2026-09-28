# 0.2.0 release gate

This is a checklist, not evidence that the tag is already published. Do not create the `0.2.0` tag until every required check below is complete on the final integrated source tree

- [x] Unit tests, lint, release AAR, sample build, and Maven Local publication on the current feature branch
- [x] API 36 emulator: sample launch and 11 connected UI tests, including zoom/pan, collapse, descendant drop prevention, accessibility, and saved viewport restoration; rerun on 2026-09-27 passed 11/11
- [x] Genuine API 36 sample screenshot and edit/zoom GIF captured in `docs/images/`
- [x] Standalone JitPack consumer project builds against published `0.1.1` as harness validation
- [ ] API 26 emulator/device: run connected UI tests and sample core interactions
  - Not verified: the installed SDK has API 35/35-ext15, 36.1, and 37.x system images but no API 26 image or command-line `sdkmanager`/`avdmanager`; the only connected emulator reports API 36 (Android 16)
- [x] Fetch `origin`, confirm `origin/main` is still the feature branch base (`8cf52ea`, no merge needed), and rerun the build and 11 connected UI tests on the current tree
- [ ] Reviewer/new developer follows README alone to install, run, and apply a custom card
- [ ] Publish `0.2.0` tag only after those checks; verify JitPack `0.2.0` POM and AAR and run `./gradlew -p consumer-smoke :app:assembleDebug` without the version override, install, and launch it
- [ ] Verify GitHub recognizes Apache-2.0 after LICENSE is merged, and update the README release status text/link
- [ ] Upload `docs/images/social-preview.png` through repository Settings → Social preview → Edit; this upload has not been completed

The source sample must continue to use `implementation(project(":compose-mindmap"))`; the consumer fixture is intentionally separate from the source Gradle project
