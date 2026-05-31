package io.github.hanhyo.composemindmap.canvas

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import io.github.hanhyo.composemindmap.layout.MindMapLayoutNode
import io.github.hanhyo.composemindmap.model.MindMapStyle

interface MindMapEditDecorationRenderer {
    fun DrawScope.drawAddChildAction(action: MindMapAddChildAction, style: MindMapStyle)
    fun DrawScope.drawDropTarget(node: MindMapLayoutNode, style: MindMapStyle)
}

class DefaultMindMapEditDecorationRenderer(
    private val textMeasurer: TextMeasurer,
) : MindMapEditDecorationRenderer {

    override fun DrawScope.drawAddChildAction(action: MindMapAddChildAction, style: MindMapStyle) {
        drawCircle(color = style.addButtonColor, radius = action.visualRadius, center = action.center)
        val plusResult = textMeasurer.measure(
            text = "+",
            style = style.addButtonTextStyle.copy(textAlign = TextAlign.Center),
        )
        drawText(
            textLayoutResult = plusResult,
            topLeft = Offset(
                action.center.x - plusResult.size.width / 2f,
                action.center.y - plusResult.size.height / 2f,
            ),
        )
    }

    override fun DrawScope.drawDropTarget(node: MindMapLayoutNode, style: MindMapStyle) {
        drawRoundRect(
            color = style.dropTargetColor,
            topLeft = node.offset,
            size = node.size,
            cornerRadius = CornerRadius(style.cornerRadius.toPx()),
            style = Stroke(
                width = style.dropTargetStrokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(style.dropTargetDashLength.toPx(), style.dropTargetDashGap.toPx()),
                ),
            ),
        )
    }
}
