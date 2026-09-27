# Published artifact smoke test

This is a standalone Gradle consumer, not a module in the repository's source build. It depends on the **public JitPack coordinate**, not `project(":compose-mindmap")`.

After the `0.2.0` tag is published and JitPack reports success, run from the repository root:

```bash
./gradlew -p consumer-smoke :app:assembleDebug
./gradlew -p consumer-smoke :app:installDebug
```

Launch `io.github.hanhyo.composemindmap.consumer/.ConsumerActivity` on an API 26+ device. A successful build, installation, and visible three-node map verifies the coordinate, POM/AAR dependency graph, and Quick Start API. This project intentionally cannot resolve `0.2.0` before JitPack publishes that tag

To check the harness before release, build against the existing artifact:

```bash
./gradlew -p consumer-smoke :app:assembleDebug -PmindMapVersion=0.1.1
```
