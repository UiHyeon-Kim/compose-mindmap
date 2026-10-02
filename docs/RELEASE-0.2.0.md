# 0.2.0 release gate

This is a checklist, not evidence that the `0.2.0` tag or public artifact exists. Complete the pre-tag checks on the final integrated tree; the JitPack checks are necessarily post-tag. The deletion API contract remains separately gated on explicit human approval and has not been changed here.

- [x] Integrate the merged release-readiness PR with latest `origin/main` (`d261342`) using a normal merge; local integration commit is `105bf61`
- [x] Unit tests, lint, release AAR, and sample build pass on that integrated tree with the same command used by CI: `./gradlew :compose-mindmap:testDebugUnitTest :compose-mindmap:lintDebug :compose-mindmap:assembleRelease :sample:assembleDebug`
- [x] Publish the integrated release AAR to Maven Local and compile the standalone consumer smoke app against `io.github.hanhyo:compose-mindmap:0.2.0`; this is a local-source publication check, not public JitPack verification
- [x] Consumer smoke app compiles five examples against the Maven Local AAR: README Quick Start, compact `BasicText` card, full Material 3 custom card, editing/history, and viewport controls
- [x] API 36.1 emulator: library and sample connected suites passed (13 library tests + 4 sample activity tests), covering focal zoom, accessibility child action and touch target, sample edit/undo/redo, collapse/expand, pinch/pan, and viewport restoration
- [x] API 26 and API 36 GitHub Actions matrix passed in [run 37053555747](https://github.com/UiHyeon-Kim/compose-mindmap/actions/runs/37053555747) at merged `main` commit `d261342`; each API job ran 13 library + 4 sample instrumentation tests and uploaded reports
- [x] API 36.1 consumer runtime screenshots captured against Maven Local: [Quick Start](images/consumer-quickstart.png) and [custom cards](images/consumer-custom.png); both show the toggle and all three nodes. This is not public JitPack evidence
- [x] Genuine API 36 sample screenshot and edit/zoom GIF captured in `docs/images/`
- [x] Historical harness evidence only: the former Quick Start-only consumer fixture built against JitPack `0.1.1`; that result does not validate the current five-example fixture
- [x] GitHub repository metadata recognizes the Apache-2.0 license and topics `android`, `android-library`, `jetpack-compose`, `kotlin`, and `mindmap`; the Apache-2.0 `LICENSE` and README license links are present in `main`
- [ ] Explicit human approval and resolution of the separately gated deletion API contract
- [ ] Reviewer/new developer follows README alone to install, run, and apply a custom card
- [ ] After the authorized `0.2.0` tag is published, verify the public JitPack POM/AAR and build, install, and launch `consumer-smoke` without Maven Local/version overrides; the local check above does not satisfy this gate
- [ ] Update the README release status and JitPack link only after the public `0.2.0` artifact is verified; README currently says the release is prepared but unpublished
- [ ] Finalize and upload the social-preview artwork through repository Settings → Social preview → Edit; this upload has not been completed

The source sample must continue to use `implementation(project(":compose-mindmap"))`; the consumer fixture is intentionally separate from the source Gradle project.
