# Compose MindMap 사용 안내

Android Jetpack Compose에서 사용할 수 있는 **편집 가능한 트리형 마인드맵 라이브러리**입니다.

기본 `MindMapNode` 데이터에서 시작해 배치, 간선, 카드 UI, 편집 동작, 뷰포트를 앱에 맞게 변경할 수 있습니다.

[English README](../README.md)

![API 36 에뮬레이터에서 실행한 Compose MindMap 샘플](images/sample.png)

---

## 주요 기능

- **트리 배치** — 좌→우, 위→아래
- **간선 스타일** — 곡선, 직선, 직각
- **커스텀 노드** — 기본 Canvas 카드 또는 직접 만든 Compose UI
- **Typed Payload** — Tree 모델을 변경하지 않고 앱 데이터를 연결
- **편집** — 앱이 노드 상태를 소유하고 Drag & Drop과 `MindMapEditController` 사용
- **Undo / Redo** — 메모리 기반 편집 이력
- **접기 / 펼치기** — 앱에서 `collapsedNodeIds` 제어
- **뷰포트** — 이동, Pinch Zoom, `zoomBy`, `centerRoot`, `focusNode`, `fitContent`
- **뷰포트 상태 저장** — Activity 재생성 후 Scale과 Translation 복원
- **Tree Validation** — 배치 전에 잘못된 Root, ID, Parent, Cycle 검사
- **접근성** — 노드 Semantic Label과 지역화된 Accessibility Action 제공

```text
MindMapNode
    ↓
Validation
    ↓
Layout Engine
    ↓
Node / Edge Rendering
    ↓
Viewport & Interaction
```

---

## 주요 동작

### 트리 배치와 간선

![샘플에서 트리 배치와 간선 스타일을 전환하는 화면](images/layout-edge.gif)

`MindMapLayoutEngine`으로 좌→우 또는 위→아래 배치를 선택할 수 있습니다.

`MindMapEdgeRenderer`로 곡선, 직선, 직각 간선을 선택합니다.

샘플에서는 다음 위치에서 확인할 수 있습니다.

```text
Controls
└── Display
    ├── Layout: Left → right / Top → down
    └── Edge: Curve / Straight / Elbow
```

배치를 변경하면 샘플이 전체 Tree를 다시 화면에 맞추기 때문에 배치에 따라 확대 비율은 달라질 수 있습니다.

---

### 기본 Canvas 카드와 Typed Payload 카드

![왼쪽은 기본 Canvas 카드, 오른쪽은 Typed Payload로 그린 Compose 카드](images/default-vs-payload.png)

왼쪽 화면은 기본 Canvas Renderer를 사용합니다.

오른쪽 화면은:

- `PayloadMindMapCanvas`
- `withPayload`
- `nodeContent`
- `nodeSize`

를 이용해 앱의 데이터와 직접 만든 Compose 카드를 사용합니다.

샘플에서는 다음 메뉴에서 두 방식을 비교할 수 있습니다.

```text
Controls → Display → Card: Canvas / Payload
```

Tree의 배치와 Interaction은 라이브러리가 담당하고, 실제 카드 UI는 앱에서 자유롭게 구성할 수 있습니다.

---

### 자식 추가, Undo, Redo

![샘플에서 자식 노드를 추가하고 Undo한 뒤 Redo하는 화면](images/edit.gif)

현재 Node 목록은 앱이 소유하고, `MindMapEditController`는 편집 이력을 관리합니다.

샘플에서는:

```text
노드 선택
   ↓
Controls → Selected node → Add child
   ↓
Controls → History → Undo / Redo
```

순서로 확인할 수 있습니다.

이 GIF는 자식 추가, Undo, Redo만 보여줍니다.

라이브러리는 Node 이동과 앱에서 정의한 편집 정책을 적용할 수 있는 Callback도 제공합니다.

Undo / Redo 이력은 메모리에 유지되므로 장기 보존이 필요한 Node 데이터는 앱에서 별도로 저장해야 합니다.

---

### 확대와 전체 트리 맞춤

![샘플에서 확대했다가 전체 트리를 다시 맞추는 화면](images/zoom-fit.gif)

`MindMapCanvasState`로 뷰포트를 제어합니다.

```kotlin
state.zoomBy(1.4f)
state.centerRoot()
state.focusNode("research")
state.fitContent()
```

샘플에서는:

```text
Controls → Viewport → Zoom + / Fit all
```

에서 확인할 수 있습니다.

확대하면 일부 Node가 화면 밖으로 이동할 수 있으며, `Fit all`을 사용하면 전체 Tree가 다시 화면 안에 들어오도록 뷰포트를 조정합니다.

Activity 재생성 후에도 Scale과 Translation을 유지하려면 `rememberSaveableMindMapCanvasState()`를 사용합니다.

---

## 샘플 실행

Android Studio에서 `sample` 실행 구성을 선택하거나 API 26 이상 기기·에뮬레이터에 설치합니다.

```bash
./gradlew :sample:installDebug
```

샘플은 공개 Maven/JitPack Artifact가 아니라 저장소의 Library Module을 직접 사용하므로 `0.2.0` 공개 전에도 실행할 수 있습니다.

샘플에서는 다음 기능을 확인할 수 있습니다.

```text
Layout
Edge
Card
Editing
History
Collapse
Viewport
```

전체 구현은 [SampleActivity.kt](../sample/src/main/java/io/github/hanhyo/composemindmap/sample/SampleActivity.kt)에서 확인할 수 있습니다.

---

## 설치

> **0.2.0은 현재 Source Tree에 준비되어 있지만 아직 공개되지 않았습니다.**

아래 JitPack 좌표는 `0.2.0` Git Tag가 공개되고 JitPack Build가 성공한 뒤 사용할 수 있습니다.

현재는 위의 Source Sample을 실행해 주세요.

### 요구 사항

- Android Jetpack Compose
- minSdk **26**
- Java **17**

`settings.gradle.kts`에 JitPack Repository를 추가합니다.

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

`0.2.0` Tag 공개 후 앱 모듈의 `build.gradle.kts`에 다음 Dependency를 추가합니다.

```kotlin
dependencies {
    implementation("com.github.UiHyeon-Kim:compose-mindmap:0.2.0")
}
```

Tag가 공개되면 [JitPack Build Page](https://jitpack.io/#UiHyeon-Kim/compose-mindmap)와 [Android 설정 안내](https://docs.jitpack.io/android/)에서 상태를 확인할 수 있습니다.

---

## 빠른 시작

마인드맵은 평평한 `MindMapNode` 목록으로 시작합니다.

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.model.MindMapNode

@Composable
fun MyMindMap() {
    val nodes = listOf(
        MindMapNode(
            id = "idea",
            title = "내 아이디어",
        ),
        MindMapNode(
            id = "research",
            title = "조사",
            parentId = "idea",
        ),
        MindMapNode(
            id = "build",
            title = "만들기",
            parentId = "idea",
        ),
    )

    MindMapCanvas(
        nodes = nodes,
        modifier = Modifier.fillMaxSize(),
    )
}
```

```text
내 아이디어
├── 조사
└── 만들기
```

입력 Tree는 다음 조건을 만족해야 합니다.

- `parentId = null`인 Root가 정확히 하나
- 모든 Node에 비어 있지 않은 고유 ID 사용
- Root가 아닌 모든 Node의 `parentId`가 목록에 존재

---

## 커스텀 Compose 카드

기본 Renderer는 Canvas에 카드를 그리지만 `nodeContent`를 이용해 일반 Compose UI로 교체할 수 있습니다.

```kotlin
MindMapCanvas(
    nodes = nodes,
    nodeSize = { node ->
        // Layout Engine에서 사용할 카드 크기
    },
    nodeContent = { node, visualState ->
        // 앱에서 만든 Compose 카드
    },
)
```

커스텀 카드를 사용할 때는 `nodeSize`와 실제 Content 크기를 맞춰야 Layout 간격, Hit Test, Connector 위치가 올바르게 계산됩니다.

앱 고유 데이터를 Node와 함께 사용하려면 `PayloadMindMapCanvas`와 `withPayload`를 사용할 수 있습니다.

전체 예제는 [Usage Guide](USAGE.md)를 참고해 주세요.

---

## Validation

Tree는 **Layout 이전에 구조를 검증**합니다.

잘못된 데이터가 들어오면 `onValidationError`가 호출되고, `errorContent`를 지정하지 않은 경우 기본 지역화 오류 UI를 표시합니다.

다음과 같은 구조 오류를 확인합니다.

```text
Root 개수 오류
중복 Node ID
존재하지 않는 Parent
Cycle
Root에서 도달할 수 없는 Node
```

`collapsedNodeIds`로 현재 숨겨진 Branch도 포함해 전체 입력 Tree를 검증합니다.

잘못된 Graph 상태가 Layout이나 Rendering 단계까지 전달되지 않도록 하기 위한 경계입니다.

---

## 접근성

Compose MindMap은 각 Node에 대한 Semantics와 Interaction Action을 제공합니다.

Visual State를 이용해 접근성 Label을 변경할 수도 있습니다.

```kotlin
semanticLabelProvider = MindMapSemanticLabelProvider { node, visual ->
    buildString {
        append(node.title)

        if (visual.isSelected) {
            append(", 선택됨")
        }

        if (visual.isCollapsed) {
            append(", 접힘")
        }
    }
}
```

Accessibility Action 이름은 기본적으로 지역화되어 있으며 `accessibilityActionLabels`로 교체할 수 있습니다.

`nodeContent`로 커스텀 카드를 사용할 때는 의도한 경우가 아니라면 같은 카드에 별도의 Accessibility Node를 중복 생성하지 않는 것이 좋습니다.

---

## 설계 방식

Compose MindMap은 앱의 실제 데이터와 Layout / Rendering을 분리합니다.

```text
Your App
  │
  ├── MindMapNode 상태 소유
  │
  └── 데이터 저장 책임
          │
          ▼
   Compose MindMap
          │
          ├── Validation
          ├── Layout
          ├── Edge Rendering
          ├── Node Rendering
          ├── Gesture
          └── Viewport
```

편집도 같은 방식으로 동작합니다.

```text
사용자 Gesture
      ↓
Library Callback
      ↓
앱에서 Node 목록 변경
      ↓
변경된 목록을 다시 Canvas에 전달
```

라이브러리가 앱 데이터의 영구적인 Source of Truth가 되지 않고, 앱이 Node 상태와 Persistence를 직접 관리합니다.

---

## 시작 배경

Compose MindMap은 독서 기록 앱 [GureumPage](https://github.com/UiHyeon-Kim/GureumPage)의 마인드맵 기능을 개발하며 겪은 문제에서 시작했습니다.

앱 내부 구현을 그대로 Library로 옮기기보다, Tree UI를 구현하며 필요했던 Layout, Rendering, Editing, Viewport, Validation을 재사용 가능한 API로 다시 설계했습니다.

```text
GureumPage
    ↓
실제 Tree UI 문제 경험
    ↓
재사용 가능한 API 설계
    ↓
Compose MindMap
```

---

## 문서

- [영문 상세 Usage Guide](USAGE.md) — 커스텀 카드, `nodeSize`, 앱 소유 편집, Undo/Redo, 뷰포트
- [0.1.1 → 0.2.0 Migration Guide](MIGRATION-0.2.0.md)
- [English README](../README.md)
- [Changelog](../CHANGELOG.md)
- [Contributing](../CONTRIBUTING.md)
- [Apache-2.0 License](../LICENSE)

---

## 현재 지원 범위

현재 Compose MindMap은 다음을 지원합니다.

- 단일 Root Tree
- 두 가지 기본 Tree Layout
- 세 가지 Edge Renderer
- Controlled Collapse
- 앱 소유 Editing
- Viewport Control
- Validation
- Accessibility Semantics

현재 다음 기능은 제공하지 않습니다.

- Radial Layout
- 양방향 Layout
- 임의 Node 사이의 Cross-link
- Import / Export
- Compose Multiplatform

대형 Tree에 대한 성능 Benchmark는 아직 진행하지 않았으므로 특정 Node 수나 성능을 보장하지 않습니다.

---

## 라이선스

Compose MindMap은 [Apache License 2.0](../LICENSE)으로 배포됩니다.
