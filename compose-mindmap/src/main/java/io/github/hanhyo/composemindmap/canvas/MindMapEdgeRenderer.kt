package io.github.hanhyo.composemindmap.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import io.github.hanhyo.composemindmap.layout.MindMapEdgeDirection
import io.github.hanhyo.composemindmap.layout.MindMapLayoutEdge
import io.github.hanhyo.composemindmap.model.MindMapStyle

fun interface MindMapEdgeRenderer {
    fun DrawScope.draw(edge: MindMapLayoutEdge, style: MindMapStyle)
}

object CurvedMindMapEdgeRenderer : MindMapEdgeRenderer {
    override fun DrawScope.draw(edge: MindMapLayoutEdge, style: MindMapStyle) {
        val curve = curvedEdgePath(edge)
        val path = Path().apply {
            moveTo(edge.start.x, edge.start.y)
            lineTo(curve.firstStemEnd.x, curve.firstStemEnd.y)
            cubicTo(
                curve.firstCurveFirstControl.x,
                curve.firstCurveFirstControl.y,
                curve.firstCurveSecondControl.x,
                curve.firstCurveSecondControl.y,
                curve.middleStart.x,
                curve.middleStart.y,
            )
            lineTo(curve.middleEnd.x, curve.middleEnd.y)
            cubicTo(
                curve.secondCurveFirstControl.x,
                curve.secondCurveFirstControl.y,
                curve.secondCurveSecondControl.x,
                curve.secondCurveSecondControl.y,
                curve.secondStemStart.x,
                curve.secondStemStart.y,
            )
            lineTo(edge.end.x, edge.end.y)
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
    val firstStemEnd: Offset,
    val firstCurveFirstControl: Offset,
    val firstCurveSecondControl: Offset,
    val middleStart: Offset,
    val middleEnd: Offset,
    val secondCurveFirstControl: Offset,
    val secondCurveSecondControl: Offset,
    val secondStemStart: Offset,
)

internal data class OrthogonalEdgePath(
    val firstCorner: Offset,
    val secondCorner: Offset,
)

internal fun curvedEdgePath(edge: MindMapLayoutEdge): CurvedEdgePath =
    if (edge.direction == MindMapEdgeDirection.VERTICAL) {
        val stemLength = (edge.end.y - edge.start.y) * CURVED_EDGE_STEM_FRACTION
        val curveWidth = (edge.end.x - edge.start.x) * CURVED_EDGE_CURVE_FRACTION
        val firstStemEnd = Offset(edge.start.x, edge.start.y + stemLength)
        val secondStemStart = Offset(edge.end.x, edge.end.y - stemLength)
        val midY = (firstStemEnd.y + secondStemStart.y) / 2f
        val middleStart = Offset(edge.start.x + curveWidth, midY)
        val middleEnd = Offset(edge.end.x - curveWidth, midY)
        CurvedEdgePath(
            firstStemEnd = firstStemEnd,
            firstCurveFirstControl = Offset(firstStemEnd.x, midY),
            firstCurveSecondControl = middleStart,
            middleStart = middleStart,
            middleEnd = middleEnd,
            secondCurveFirstControl = middleEnd,
            secondCurveSecondControl = Offset(secondStemStart.x, midY),
            secondStemStart = secondStemStart,
        )
    } else {
        val stemLength = (edge.end.x - edge.start.x) * CURVED_EDGE_STEM_FRACTION
        val curveHeight = (edge.end.y - edge.start.y) * CURVED_EDGE_CURVE_FRACTION
        val firstStemEnd = Offset(edge.start.x + stemLength, edge.start.y)
        val secondStemStart = Offset(edge.end.x - stemLength, edge.end.y)
        val midX = (firstStemEnd.x + secondStemStart.x) / 2f
        val middleStart = Offset(midX, edge.start.y + curveHeight)
        val middleEnd = Offset(midX, edge.end.y - curveHeight)
        CurvedEdgePath(
            firstStemEnd = firstStemEnd,
            firstCurveFirstControl = Offset(midX, firstStemEnd.y),
            firstCurveSecondControl = middleStart,
            middleStart = middleStart,
            middleEnd = middleEnd,
            secondCurveFirstControl = middleEnd,
            secondCurveSecondControl = Offset(midX, secondStemStart.y),
            secondStemStart = secondStemStart,
        )
    }

internal fun orthogonalEdgePath(edge: MindMapLayoutEdge): OrthogonalEdgePath =
    if (edge.direction == MindMapEdgeDirection.VERTICAL) {
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

private const val CURVED_EDGE_STEM_FRACTION = 0.1f
private const val CURVED_EDGE_CURVE_FRACTION = 0.45f
