# Contributing

Issues and pull requests are welcome. Please describe the Android version, expected and actual behavior, and a minimal tree that reproduces the problem. For visual defects, attach a screenshot or short recording

Before opening a pull request:

```bash
./gradlew :compose-mindmap:testDebugUnitTest :compose-mindmap:lintDebug :compose-mindmap:assembleRelease :sample:assembleDebug
```

Run `./gradlew :compose-mindmap:connectedDebugAndroidTest` on an API 26+ emulator when changing gestures, Compose UI, accessibility, or state restoration. Keep the sample dependent on the source module. For public API changes, update KDoc, README examples, migration notes, and `CHANGELOG.md`

The project targets a single-root Android tree map. Radial/bidirectional graph layouts, cross-links, export, and Compose Multiplatform are welcome as design discussions first, not assumed parts of small bug fixes. Contributions are licensed under the repository's [Apache-2.0 license](LICENSE)
