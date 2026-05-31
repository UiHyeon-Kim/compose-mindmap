package io.github.hanhyo.composemindmap.canvas

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import io.github.hanhyo.composemindmap.layout.MindMapLayoutEdge
import io.github.hanhyo.composemindmap.model.MindMapStyle
import kotlin.math.abs

fun interface MindMapEdgeRenderer {
    fun DrawScope.draw(edge: MindMapLayoutEdge, style: MindMapStyle)
}

object CurvedMindMapEdgeRenderer : MindMapEdgeRenderer {
    override fun DrawScope.draw(edge: MindMapLayoutEdge, style: MindMapStyle) {
        val path = Path().apply {
            moveTo(edge.start.x, edge.start.y)
            if (abs(edge.end.y - edge.start.y) >= abs(edge.end.x - edge.start.x)) {
                val midY = (edge.start.y + edge.end.y) / 2f
                cubicTo(edge.start.x, midY, edge.end.x, midY, edge.end.x, edge.end.y)
            } else {
                val midX = (edge.start.x + edge.end.x) / 2f
                cubicTo(midX, edge.start.y, midX, edge.end.y, edge.end.x, edge.end.y)
            }
        }
        drawPath(path, color = style.edgeColor, style = Stroke(width = style.edgeStrokeWidth.toPx()))
    }
}

object StraightMindMapEdgeRenderer : MindMapEdgeRenderer {
    override fun DrawScope.draw(edge: MindMapLayoutEdge, style: MindMapStyle) {
        drawLine(
            color = style.edgeColor,
            start = edge.start,
            end = edge.end,
            strokeWidth = style.edgeStrokeWidth.toPx(),
        )
    }
}
