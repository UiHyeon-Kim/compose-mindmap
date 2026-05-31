package io.github.hanhyo.composemindmap.canvas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
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

        composeRule.onNodeWithTag("root").assertIsDisplayed()
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

        composeRule.onNodeWithTag("root").performTouchInput { longClick() }

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

        composeRule.onNodeWithTag("typed").assertIsDisplayed()
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
