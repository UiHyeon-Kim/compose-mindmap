package io.github.hanhyo.composemindmap.canvas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.geometry.Offset
import io.github.hanhyo.composemindmap.model.MindMapNode
import io.github.hanhyo.composemindmap.model.MindMapValidationResult
import io.github.hanhyo.composemindmap.model.withPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class MindMapCanvasInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun customNodeContent_isRendered() {
        composeRule.setContent {
            MindMapCanvas(
                nodes = listOf(MindMapNode(id = "root", title = "Root")),
                nodeContent = { node, _ ->
                    Text(text = node.title, modifier = Modifier.testTag(node.id))
                },
            )
        }

        composeRule.onNodeWithTag("root", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun nodeLongClick_invokesCallbackOnce() {
        var longClickCount = 0
        composeRule.setContent {
            Box(Modifier.fillMaxSize()) {
                MindMapCanvas(
                    nodes = listOf(MindMapNode(id = "root", title = "Root")),
                    nodeContent = { node, _ ->
                        Text(text = node.title, modifier = Modifier.testTag(node.id))
                    },
                    onNodeLongClick = { longClickCount++ },
                )
            }
        }

        composeRule.onNodeWithTag("root", useUnmergedTree = true).performTouchInput { longClick() }

        composeRule.runOnIdle { assertEquals(1, longClickCount) }
    }

    @Test
    fun invalidTree_reportsErrorWithoutRenderingNodes() {
        var validationError: MindMapValidationResult.Invalid? = null
        composeRule.setContent {
            MindMapCanvas(
                nodes = listOf(
                    MindMapNode(id = "root-1", title = "Root 1"),
                    MindMapNode(id = "root-2", title = "Root 2"),
                ),
                nodeContent = { node, _ ->
                    Text(text = node.title, modifier = Modifier.testTag(node.id))
                },
                onValidationError = { validationError = it },
            )
        }

        composeRule.waitUntil { validationError != null }
        composeRule.runOnIdle { assertTrue(validationError!!.errors.isNotEmpty()) }
        composeRule.onNodeWithTag("root-1").assertDoesNotExist()
        composeRule.onNodeWithTag("root-2").assertDoesNotExist()
    }

    @Test
    fun `잘못된_트리는_커스텀_오류_콘텐츠를_그린다`() {
        composeRule.setContent {
            MindMapCanvas(
                nodes = listOf(MindMapNode("a", "A"), MindMapNode("b", "B")),
                errorContent = { Text("Invalid tree", modifier = Modifier.testTag("error")) },
            )
        }
        composeRule.onNodeWithTag("error").assertIsDisplayed()
    }

    @Test
    fun `두_손가락_제스처는_확대와_이동을_함께_적용한다`() {
        val viewport = MindMapCanvasState()
        composeRule.setContent {
            MindMapCanvas(
                nodes = listOf(MindMapNode("root", "Root")),
                state = viewport,
            )
        }
        composeRule.waitForIdle()
        val oldOffset = viewport.offset
        composeRule.onRoot().performTouchInput {
            val middle = Offset(center.x, center.y)
            pinch(
                start0 = middle + Offset(-80f, 0f),
                end0 = middle + Offset(-170f, 40f),
                start1 = middle + Offset(80f, 0f),
                end1 = middle + Offset(170f, 40f),
            )
        }
        composeRule.runOnIdle {
            assertTrue(viewport.scale > 1f)
            assertTrue(viewport.offset != oldOffset)
        }
    }

    @Test
    fun `커스텀_카드는_상태_기반_접근성_노드를_하나만_노출한다`() {
        composeRule.setContent {
            MindMapCanvas(
                nodes = listOf(MindMapNode("root", "Root")),
                selectedNodeId = "root",
                semanticLabelProvider = MindMapSemanticLabelProvider { node, visual ->
                    "${node.title}, selected: ${visual.isSelected}"
                },
                accessibilityActionLabels = MindMapAccessibilityActionLabels("Choose", "Details", "Add"),
                nodeContent = { node, _ -> Text(node.title) },
            )
        }
        assertEquals(1, composeRule.onAllNodesWithContentDescription("Root, selected: true").fetchSemanticsNodes().size)
    }

    @Test
    fun `접힌_브랜치에서는_하위_노드를_표시하지_않는다`() {
        var collapsed by mutableStateOf(emptySet<String>())
        composeRule.setContent {
            MindMapCanvas(
                nodes = listOf(
                    MindMapNode("root", "Root"),
                    MindMapNode("child", "Child", parentId = "root"),
                ),
                collapsedNodeIds = collapsed,
                nodeContent = { node, _ -> Text(node.title, Modifier.testTag(node.id)) },
            )
        }
        composeRule.onNodeWithTag("child", useUnmergedTree = true).assertIsDisplayed()
        composeRule.runOnIdle { collapsed = setOf("root") }
        composeRule.onNodeWithTag("child", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `저장된_뷰포트_복원_뒤에도_노드_오프셋을_유지한다`() {
        val restoration = StateRestorationTester(composeRule)
        var viewport: MindMapCanvasState? = null
        restoration.setContent {
            val saved = rememberSaveableMindMapCanvasState()
            viewport = saved
            MindMapCanvas(
                nodes = listOf(MindMapNode("root", "Root"), MindMapNode("child", "Child", parentId = "root")),
                state = saved,
            )
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { viewport!!.focusNode("child") }
        composeRule.waitForIdle()
        val before = viewport!!.offset
        restoration.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle { assertEquals(before, viewport!!.offset) }
    }

    @Test
    fun `후손_노드에_드롭하면_이동_콜백을_호출하지_않는다`() {
        var moves = 0
        composeRule.setContent {
            MindMapCanvas(
                nodes = listOf(
                    MindMapNode("root", "Root"),
                    MindMapNode("a", "A", parentId = "root"),
                    MindMapNode("b", "B", parentId = "a"),
                ),
                selectedNodeId = "a",
                editMode = true,
                onNodeMove = { _, _ -> moves++ },
            )
        }
        val source = composeRule.onNodeWithContentDescription("A")
        val sourceBounds = source.getUnclippedBoundsInRoot()
        val targetBounds = composeRule.onNodeWithContentDescription("B").getUnclippedBoundsInRoot()
        val targetInSource = Offset(
            ((targetBounds.left.value + targetBounds.right.value) / 2f - sourceBounds.left.value) * composeRule.density.density,
            ((targetBounds.top.value + targetBounds.bottom.value) / 2f - sourceBounds.top.value) * composeRule.density.density,
        )
        source.performTouchInput {
            down(center)
            moveTo(targetInSource)
            up()
        }
        composeRule.runOnIdle { assertEquals(0, moves) }
    }

    @Test
    fun payloadCanvas_passesTypedPayloadToSlot() {
        composeRule.setContent {
            PayloadMindMapCanvas(
                nodes = listOf(
                    MindMapNode(id = "root", title = "Root").withPayload(SamplePayload(label = "typed")),
                ),
                nodeContent = { wrapped, _ ->
                    Text(
                        text = wrapped.payload.label,
                        modifier = Modifier.testTag(wrapped.payload.label),
                    )
                },
            )
        }

        composeRule.onNodeWithTag("typed", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun customEdgeRenderer_isInvoked() {
        val drawCount = AtomicInteger()
        composeRule.setContent {
            MindMapCanvas(
                nodes = listOf(
                    MindMapNode(id = "root", title = "Root"),
                    MindMapNode(id = "child", title = "Child", parentId = "root"),
                ),
                edgeRenderer = MindMapEdgeRenderer { _, _ -> drawCount.incrementAndGet() },
            )
        }

        composeRule.waitUntil { drawCount.get() > 0 }
    }

    private data class SamplePayload(val label: String)
}
