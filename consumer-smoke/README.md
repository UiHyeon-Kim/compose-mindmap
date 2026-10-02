# Consumer smoke test

This standalone Gradle app is not a module in the repository's source build. Its activity toggles between the README Quick Start and the `docs/USAGE.md` custom `nodeContent` + `nodeSize` card. A compile-only fixture copies the compact `BasicText` card from the README; the complete app-owned editing/history and saveable-viewport examples from `docs/USAGE.md` are also compile-only fixtures. All five Compose examples compile as a consumer against a published AAR, not `project(":compose-mindmap")`.

## Pre-release Maven Local check

To compile-check the current source publication before there is a public `0.2.0` artifact, run these commands from the repository root:

```bash
./gradlew :compose-mindmap:publishReleasePublicationToMavenLocal
./gradlew -p consumer-smoke :app:assembleDebug -PmindMapRepository=mavenLocal -PmindMapVersion=0.2.0
```

This explicit local mode uses the library's Maven Local coordinates, `io.github.hanhyo:compose-mindmap:0.2.0`. It verifies the current publication metadata/AAR and that the Quick Start, compact README card, full custom card, editing/history, and viewport API examples compile. It does **not** verify the public JitPack coordinate or a public release.

To inspect the two choices against this local artifact, install with the same properties and launch the activity on an API 26+ device:

```bash
./gradlew -p consumer-smoke :app:installDebug -PmindMapRepository=mavenLocal -PmindMapVersion=0.2.0
adb shell am start -n io.github.hanhyo.composemindmap.consumer/.ConsumerActivity
```

## Public JitPack check after tagging

Only after the `0.2.0` tag is published should the default JitPack coordinate be checked:

```bash
./gradlew -p consumer-smoke :app:assembleDebug
./gradlew -p consumer-smoke :app:installDebug
adb shell am start -n io.github.hanhyo.composemindmap.consumer/.ConsumerActivity
```

A successful build, installation, and both selectable examples on an API 26+ device verifies the public JitPack coordinate, POM/AAR dependency graph, and consumer runtime. Do not infer this result from Maven Local.

The earlier Quick Start-only consumer fixture was built against `0.1.1`. That historical harness check does not cover or establish compatibility for the current five-example fixture.
