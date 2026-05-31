package io.github.hanhyo.composemindmap.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.canvas.rememberMindMapCanvasState
import io.github.hanhyo.composemindmap.model.MindMapNode

class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    val state = rememberMindMapCanvasState()
                    var selected by remember { mutableStateOf<String?>(null) }

                    MindMapCanvas(
                        nodes = sampleNodes,
                        state = state,
                        modifier = Modifier.fillMaxSize(),
                        selectedNodeId = selected,
                        onNodeClick = { selected = if (selected == it) null else it },
                        onCanvasClick = { selected = null },
                    )
                }
            }
        }
    }
}

private val sampleNodes = listOf(
    MindMapNode(id = "root", title = "Compose MindMap"),
    MindMapNode(id = "canvas", title = "Canvas", subtitle = "rendering", parentId = "root"),
    MindMapNode(id = "layout", title = "Layout", subtitle = "engine", parentId = "root"),
    MindMapNode(id = "model", title = "Model", subtitle = "data", parentId = "root"),
    MindMapNode(id = "topdown", title = "Top-Down", parentId = "layout"),
    MindMapNode(id = "ltr", title = "Left-to-Right", parentId = "layout"),
    MindMapNode(id = "payload", title = "Payload", subtitle = "typed", parentId = "canvas"),
    MindMapNode(id = "policy", title = "EditPolicy", parentId = "canvas"),
    MindMapNode(id = "viewport", title = "Viewport API", parentId = "canvas"),
    MindMapNode(id = "collapse", title = "Collapse", parentId = "canvas"),
)
