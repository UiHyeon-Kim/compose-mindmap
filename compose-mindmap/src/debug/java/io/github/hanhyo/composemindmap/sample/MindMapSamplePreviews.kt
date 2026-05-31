package io.github.hanhyo.composemindmap.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.canvas.OrthogonalMindMapEdgeRenderer
import io.github.hanhyo.composemindmap.canvas.PayloadMindMapCanvas
import io.github.hanhyo.composemindmap.canvas.StraightMindMapEdgeRenderer
import io.github.hanhyo.composemindmap.layout.LeftToRightTreeLayoutEngine
import io.github.hanhyo.composemindmap.model.MindMapNode
import io.github.hanhyo.composemindmap.model.withPayload

@Preview(widthDp = 420, heightDp = 500, showBackground = true)
@Composable
private fun DefaultTopDownMindMapPreview() {
    Surface {
        MindMapCanvas(nodes = sampleNodes)
    }
}

@Preview(widthDp = 640, heightDp = 320, showBackground = true)
@Composable
private fun LeftToRightStraightMindMapPreview() {
    Surface {
        MindMapCanvas(
            nodes = sampleNodes,
            layoutEngine = LeftToRightTreeLayoutEngine,
            edgeRenderer = StraightMindMapEdgeRenderer,
        )
    }
}

@Preview(widthDp = 420, heightDp = 500, showBackground = true)
@Composable
private fun TopDownOrthogonalMindMapPreview() {
    Surface {
        MindMapCanvas(
            nodes = sampleNodes,
            edgeRenderer = OrthogonalMindMapEdgeRenderer,
        )
    }
}

@Preview(widthDp = 420, heightDp = 500, showBackground = true)
@Composable
private fun PayloadSlotMindMapPreview() {
    val nodes = sampleNodes.map { node ->
        node.withPayload(SamplePayload(badge = if (node.parentId == null) "ROOT" else "BOOK"))
    }
    Surface {
        PayloadMindMapCanvas(
            nodes = nodes,
            nodeContent = { item, _ ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF3F0E7), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Text(text = "${item.payload.badge}  ${item.node.title}")
                }
            },
        )
    }
}

private data class SamplePayload(val badge: String)

private val sampleNodes = listOf(
    MindMapNode(id = "root", title = "Reading"),
    MindMapNode(id = "fiction", title = "Fiction", parentId = "root"),
    MindMapNode(id = "essay", title = "Essay", parentId = "root"),
    MindMapNode(id = "classic", title = "Classic", parentId = "fiction"),
    MindMapNode(id = "science", title = "Science", parentId = "essay"),
)
