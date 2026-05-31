package io.github.hanhyo.composemindmap.canvas

import androidx.compose.ui.geometry.Offset
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
        val curve = curvedEdgePath(edge)
        val path = Path().apply {
            moveTo(edge.start.x, edge.start.y)
            cubicTo(
                curve.firstControl.x,
                curve.firstControl.y,
                curve.secondControl.x,
                curve.secondControl.y,
                edge.end.x,
                edge.end.y,
            )
        }
        drawPath(path, color = style.edgeColor, style = Stroke(width = style.edgeStrokeWidth.toPx()))
    }
}

object OrthogonalMindMapEdgeRenderer : MindMapEdgeRenderer {
    override fun DrawScope.draw(edge: MindMapLayoutEdge, style: MindMapStyle) {
        val orthogonal = orthogonalEdgePath(edge)
        val path = Path().apply {
            moveTo(edge.start.x, edge.start.y)
            lineTo(orthogonal.firstCorner.x, orthogonal.firstCorner.y)
            lineTo(orthogonal.secondCorner.x, orthogonal.secondCorner.y)
            lineTo(edge.end.x, edge.end.y)
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

internal data class CurvedEdgePath(
    val firstControl: Offset,
    val secondControl: Offset,
)

internal data class OrthogonalEdgePath(
    val firstCorner: Offset,
    val secondCorner: Offset,
)

internal fun curvedEdgePath(edge: MindMapLayoutEdge): CurvedEdgePath =
    if (edge.isVertical()) {
        val midY = (edge.start.y + edge.end.y) / 2f
        CurvedEdgePath(
            firstControl = Offset(edge.start.x, midY),
            secondControl = Offset(edge.end.x, midY),
        )
    } else {
        val midX = (edge.start.x + edge.end.x) / 2f
        CurvedEdgePath(
            firstControl = Offset(midX, edge.start.y),
            secondControl = Offset(midX, edge.end.y),
        )
    }

internal fun orthogonalEdgePath(edge: MindMapLayoutEdge): OrthogonalEdgePath =
    if (edge.isVertical()) {
        val midY = (edge.start.y + edge.end.y) / 2f
        OrthogonalEdgePath(
            firstCorner = Offset(edge.start.x, midY),
            secondCorner = Offset(edge.end.x, midY),
        )
    } else {
        val midX = (edge.start.x + edge.end.x) / 2f
        OrthogonalEdgePath(
            firstCorner = Offset(midX, edge.start.y),
            secondCorner = Offset(midX, edge.end.y),
        )
    }

private fun MindMapLayoutEdge.isVertical(): Boolean =
    abs(end.y - start.y) >= abs(end.x - start.x)
