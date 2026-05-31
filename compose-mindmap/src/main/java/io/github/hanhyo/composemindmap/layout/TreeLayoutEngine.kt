package io.github.hanhyo.composemindmap.layout

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import io.github.hanhyo.composemindmap.model.MindMapNode
import io.github.hanhyo.composemindmap.model.MindMapStyle
import io.github.hanhyo.composemindmap.model.defaultNodeSize

data class MindMapLayoutInput(
    val nodes: List<MindMapNode>,
    val style: MindMapStyle,
    val density: Density,
    val nodeSize: (MindMapNode) -> DpSize = { style.defaultNodeSize },
)

data class MindMapLayoutNode(
    val node: MindMapNode,
    val offset: Offset,
    val size: Size,
)

data class MindMapLayoutEdge(
    val parentId: String,
    val childId: String,
    val start: Offset,
    val end: Offset,
    val direction: MindMapEdgeDirection = if (kotlin.math.abs(end.y - start.y) >= kotlin.math.abs(end.x - start.x)) {
        MindMapEdgeDirection.VERTICAL
    } else {
        MindMapEdgeDirection.HORIZONTAL
    },
)

enum class MindMapEdgeDirection {
    VERTICAL,
    HORIZONTAL,
}

data class MindMapLayoutResult(
    val nodes: List<MindMapLayoutNode> = emptyList(),
    val edges: List<MindMapLayoutEdge> = emptyList(),
)

enum class MindMapRootAlignment {
    TOP_CENTER,
    CENTER_START,
    CENTER,
}

interface MindMapLayoutEngine {
    val rootAlignment: MindMapRootAlignment

    fun layout(input: MindMapLayoutInput): MindMapLayoutResult
}

object TopDownTreeLayoutEngine : MindMapLayoutEngine {
    override val rootAlignment = MindMapRootAlignment.TOP_CENTER

    override fun layout(input: MindMapLayoutInput): MindMapLayoutResult {
        if (input.nodes.isEmpty()) return MindMapLayoutResult()
        return with(input.density) {
            val nodes = input.nodes
            val style = input.style
            val hGap = style.horizontalGap.toPx()
            val vGap = style.verticalGap.toPx()
            val sizes = nodes.associate { node ->
                val size = input.nodeSize(node)
                node.id to Size(size.width.toPx(), size.height.toPx())
            }

            val childrenMap: Map<String?, List<MindMapNode>> = nodes.groupBy { it.parentId }
            val root = childrenMap[null]?.firstOrNull() ?: return@with MindMapLayoutResult()
            val result = mutableListOf<MindMapLayoutNode>()
            val subtreeWidths = mutableMapOf<String, Float>()

            fun subtreeWidth(nodeId: String): Float {
                subtreeWidths[nodeId]?.let { return it }
                val nodeWidth = sizes.getValue(nodeId).width
                val children = childrenMap[nodeId]
                if (children.isNullOrEmpty()) return nodeWidth.also { subtreeWidths[nodeId] = it }
                val childrenTotal = children.sumOf { subtreeWidth(it.id).toDouble() }.toFloat()
                return maxOf(nodeWidth, childrenTotal + hGap * (children.size - 1))
                    .also { subtreeWidths[nodeId] = it }
            }

            fun place(node: MindMapNode, startX: Float, y: Float) {
                val size = sizes.getValue(node.id)
                val allocated = subtreeWidth(node.id)
                val nodeX = startX + (allocated - size.width) / 2f
                result += MindMapLayoutNode(node, Offset(nodeX, y), size)

                val children = childrenMap[node.id] ?: return
                val childY = y + size.height + vGap
                var childX = startX
                for (child in children) {
                    place(child, childX, childY)
                    childX += subtreeWidth(child.id) + hGap
                }
            }

            place(root, 0f, 0f)
            MindMapLayoutResult(
                nodes = result,
                edges = result.edges(MindMapEdgeDirection.VERTICAL) { parent, child ->
                    Offset(parent.offset.x + parent.size.width / 2f, parent.offset.y + parent.size.height) to
                        Offset(child.offset.x + child.size.width / 2f, child.offset.y)
                },
            )
        }
    }
}

object LeftToRightTreeLayoutEngine : MindMapLayoutEngine {
    override val rootAlignment = MindMapRootAlignment.CENTER_START

    override fun layout(input: MindMapLayoutInput): MindMapLayoutResult {
        if (input.nodes.isEmpty()) return MindMapLayoutResult()
        return with(input.density) {
            val nodes = input.nodes
            val style = input.style
            val hGap = style.horizontalGap.toPx()
            val vGap = style.verticalGap.toPx()
            val sizes = nodes.associate { node ->
                val size = input.nodeSize(node)
                node.id to Size(size.width.toPx(), size.height.toPx())
            }

            val childrenMap: Map<String?, List<MindMapNode>> = nodes.groupBy { it.parentId }
            val root = childrenMap[null]?.firstOrNull() ?: return@with MindMapLayoutResult()
            val result = mutableListOf<MindMapLayoutNode>()
            val subtreeHeights = mutableMapOf<String, Float>()

            fun subtreeHeight(nodeId: String): Float {
                subtreeHeights[nodeId]?.let { return it }
                val nodeHeight = sizes.getValue(nodeId).height
                val children = childrenMap[nodeId]
                if (children.isNullOrEmpty()) return nodeHeight.also { subtreeHeights[nodeId] = it }
                val childrenTotal = children.sumOf { subtreeHeight(it.id).toDouble() }.toFloat()
                return maxOf(nodeHeight, childrenTotal + vGap * (children.size - 1))
                    .also { subtreeHeights[nodeId] = it }
            }

            fun place(node: MindMapNode, x: Float, startY: Float) {
                val size = sizes.getValue(node.id)
                val allocated = subtreeHeight(node.id)
                val nodeY = startY + (allocated - size.height) / 2f
                result += MindMapLayoutNode(node, Offset(x, nodeY), size)

                val children = childrenMap[node.id] ?: return
                val childX = x + size.width + hGap
                var childY = startY
                for (child in children) {
                    place(child, childX, childY)
                    childY += subtreeHeight(child.id) + vGap
                }
            }

            place(root, 0f, 0f)
            MindMapLayoutResult(
                nodes = result,
                edges = result.edges(MindMapEdgeDirection.HORIZONTAL) { parent, child ->
                    Offset(parent.offset.x + parent.size.width, parent.offset.y + parent.size.height / 2f) to
                        Offset(child.offset.x, child.offset.y + child.size.height / 2f)
                },
            )
        }
    }
}

private fun List<MindMapLayoutNode>.edges(
    direction: MindMapEdgeDirection,
    anchors: (parent: MindMapLayoutNode, child: MindMapLayoutNode) -> Pair<Offset, Offset>,
): List<MindMapLayoutEdge> {
    val nodeMap = associateBy { it.node.id }
    return mapNotNull { child ->
        val parentId = child.node.parentId ?: return@mapNotNull null
        val parent = nodeMap[parentId] ?: return@mapNotNull null
        val (start, end) = anchors(parent, child)
        MindMapLayoutEdge(
            parentId = parentId,
            childId = child.node.id,
            start = start,
            end = end,
            direction = direction,
        )
    }
}
