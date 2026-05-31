package io.github.hanhyo.composemindmap.layout

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.hanhyo.composemindmap.model.MindMapNode
import io.github.hanhyo.composemindmap.model.MindMapStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TreeLayoutEngineTest {

    @Test
    fun `layout supports per-node sizes`() {
        val nodes = listOf(
            MindMapNode(id = "root", title = "root"),
            MindMapNode(id = "child", title = "child", parentId = "root"),
        )
        val style = MindMapStyle(verticalGap = 20.dp)

        val layout = TopDownTreeLayoutEngine.layout(
            MindMapLayoutInput(nodes, style, Density(1f)) { node ->
                if (node.id == "root") DpSize(100.dp, 120.dp) else DpSize(80.dp, 40.dp)
            },
        ).nodes

        val root = layout.first { it.node.id == "root" }
        val child = layout.first { it.node.id == "child" }
        assertEquals(100f, root.size.width)
        assertEquals(120f, root.size.height)
        assertEquals(80f, child.size.width)
        assertEquals(140f, child.offset.y)
        assertTrue(child.offset.x >= root.offset.x)
    }

    @Test
    fun `children are placed without overlap`() {
        val nodes = listOf(
            MindMapNode(id = "root", title = "root"),
            MindMapNode(id = "left", title = "left", parentId = "root"),
            MindMapNode(id = "right", title = "right", parentId = "root"),
        )
        val style = MindMapStyle(horizontalGap = 24.dp)

        val layout = TopDownTreeLayoutEngine.layout(MindMapLayoutInput(nodes, style, Density(1f))).nodes

        val left = layout.first { it.node.id == "left" }
        val right = layout.first { it.node.id == "right" }
        assertTrue(left.offset.x + left.size.width + 24f <= right.offset.x)
    }

    @Test
    fun `top-down edges connect bottom center to top center`() {
        val result = TopDownTreeLayoutEngine.layout(
            MindMapLayoutInput(
                nodes = listOf(
                    MindMapNode(id = "root", title = "root"),
                    MindMapNode(id = "child", title = "child", parentId = "root"),
                ),
                style = MindMapStyle(),
                density = Density(1f),
            ),
        )

        val root = result.nodes.first { it.node.id == "root" }
        val child = result.nodes.first { it.node.id == "child" }
        val edge = result.edges.single()
        assertEquals(Offset(root.offset.x + root.size.width / 2f, root.offset.y + root.size.height), edge.start)
        assertEquals(Offset(child.offset.x + child.size.width / 2f, child.offset.y), edge.end)
    }

    @Test
    fun `left-to-right children are placed without overlap and edges use horizontal anchors`() {
        val style = MindMapStyle(verticalGap = 24.dp)
        val result = LeftToRightTreeLayoutEngine.layout(
            MindMapLayoutInput(
                nodes = listOf(
                    MindMapNode(id = "root", title = "root"),
                    MindMapNode(id = "top", title = "top", parentId = "root"),
                    MindMapNode(id = "bottom", title = "bottom", parentId = "root"),
                ),
                style = style,
                density = Density(1f),
            ),
        )

        val root = result.nodes.first { it.node.id == "root" }
        val top = result.nodes.first { it.node.id == "top" }
        val bottom = result.nodes.first { it.node.id == "bottom" }
        assertTrue(top.offset.y + top.size.height + 24f <= bottom.offset.y)

        val edge = result.edges.first { it.childId == "top" }
        assertEquals(Offset(root.offset.x + root.size.width, root.offset.y + root.size.height / 2f), edge.start)
        assertEquals(Offset(top.offset.x, top.offset.y + top.size.height / 2f), edge.end)
    }
}
