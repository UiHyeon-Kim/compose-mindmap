# Compose MindMap 사용 안내

Compose MindMap은 Android Jetpack Compose에서 편집 가능한 트리형 마인드맵을 그리는 라이브러리이며, 기본 `MindMapNode` 목록으로 시작해 배치·간선·카드·제스처·뷰포트를 바꿀 수 있습니다

![API 36 에뮬레이터에서 실행한 Compose MindMap 샘플](images/sample.png)

## 주요 동작

### 트리 배치와 간선

![샘플에서 트리 배치와 간선 스타일을 전환하는 화면](images/layout-edge.gif)

`MindMapLayoutEngine`으로 좌→우 또는 위→아래 배치를, `MindMapEdgeRenderer`로 곡선·직선·직각 간선을 선택할 수 있습니다

샘플 경로는 `Controls → Display → Layout: Left → right / Top → down`과 `Edge: Curve / Straight / Elbow`이며, 배치를 바꾸면 샘플이 전체 트리를 다시 맞추므로 캡처마다 확대율은 다릅니다

### 기본 Canvas 카드와 typed-payload 카드

![왼쪽은 기본 Canvas 카드, 오른쪽은 타입이 있는 payload로 그린 Compose 카드](images/default-vs-payload.png)

왼쪽은 기본 Canvas 렌더러이고 오른쪽은 `PayloadMindMapCanvas`, `withPayload`, `nodeContent`, `nodeSize`를 사용한 카드입니다

샘플의 `Controls → Display → Card: Canvas / Payload`에서 두 방식을 비교할 수 있습니다

### 자식 추가, 실행 취소와 다시 실행

![샘플에서 자식 노드를 추가하고 Undo한 뒤 Redo하는 화면](images/edit.gif)

앱이 노드 목록을 소유하고 `MindMapEditController`가 편집 이력을 관리합니다

노드를 선택한 다음 `Controls → Selected node → Add child`를 누르고 `Controls → History → Undo / Redo`로 되돌리거나 다시 적용할 수 있습니다

이 데모는 자식 추가·Undo·Redo만 보여 줍니다

### 확대와 전체 트리 맞춤

![샘플에서 확대했다가 전체 트리를 다시 맞추는 화면](images/zoom-fit.gif)

`MindMapCanvasState.zoomBy()`와 `fitContent()`로 뷰포트를 제어합니다

샘플의 `Controls → Viewport → Zoom + / Fit all`에서 확대와 전체 맞춤을 선택할 수 있습니다

확대하면 일부 노드가 화면 밖에 놓일 수 있지만 `Fit all`은 전체 트리를 다시 보여 줍니다

## 샘플 실행

Android Studio에서 `sample` 실행 구성을 선택하거나 API 26 이상 기기·에뮬레이터에 설치합니다

```bash
./gradlew :sample:installDebug
```

샘플은 공개 Maven/JitPack 아티팩트가 아니라 저장소의 모듈을 직접 사용하므로 릴리스 전에도 실행할 수 있습니다

전체 예제는 [샘플 액티비티](../sample/src/main/java/io/github/hanhyo/composemindmap/sample/SampleActivity.kt)에서 볼 수 있습니다

## 설치 상태

`0.2.0`은 아직 Git 태그와 JitPack 아티팩트로 공개되지 않았습니다

아래 좌표는 태그 공개와 JitPack 빌드 성공 뒤 사용할 수 있으니 지금은 위 샘플을 실행해 주세요

Android Jetpack Compose, `minSdk 26`, Java 17이 필요합니다

`settings.gradle.kts`에 다음 저장소 설정을 추가합니다

```kotlin
import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

태그 공개 뒤 앱 모듈의 `build.gradle.kts`에 다음 좌표를 추가합니다

```kotlin
dependencies {
    implementation("com.github.UiHyeon-Kim:compose-mindmap:0.2.0")
}
```

## 빠른 시작

다음 예제는 필요한 import를 포함합니다

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.model.MindMapNode

@Composable
fun MyMindMap() {
    val nodes = listOf(
        MindMapNode(id = "idea", title = "내 아이디어"),
        MindMapNode(id = "research", title = "조사", parentId = "idea"),
        MindMapNode(id = "build", title = "만들기", parentId = "idea"),
    )

    MindMapCanvas(nodes = nodes, modifier = Modifier.fillMaxSize())
}
```

루트는 하나여야 하며 루트의 `parentId`는 `null`입니다

ID는 고유해야 하고 나머지 노드의 부모 ID는 목록 안에 있어야 합니다

잘못된 트리는 `onValidationError`를 호출하고 기본 오류 UI를 보여 줍니다

## 다음 단계

- [영문 상세 사용 가이드](USAGE.md): 커스텀 카드와 `nodeSize`, 앱 소유 편집·Undo/Redo, 뷰포트 예제
- [0.1.1에서 마이그레이션](MIGRATION-0.2.0.md)
- [영문 README](../README.md)
- [변경 이력](../CHANGELOG.md) · [Apache-2.0 라이선스](../LICENSE)

현재 단일 루트 트리와 두 가지 기본 배치, 세 가지 간선 렌더러를 지원합니다

방사형·양방향 배치, 교차 간선, 가져오기·내보내기, Compose Multiplatform은 지원하지 않습니다

대형 트리 성능은 아직 벤치마크하지 않았습니다
