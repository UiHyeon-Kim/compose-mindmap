package io.github.hanhyo.composemindmap.canvas

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import io.github.hanhyo.composemindmap.model.MindMapNode
import io.github.hanhyo.composemindmap.model.MindMapStyle

@Immutable
data class MindMapNodeVisualState(
    val isSelected: Boolean = false,
    val isDragging: Boolean = false,
    val isDropTarget: Boolean = false,
    val isDragGhost: Boolean = false,
    val hasChildren: Boolean = false,
    val isCollapsed: Boolean = false,
)

fun interface MindMapCanvasNodeRenderer {
    fun DrawScope.draw(
        node: MindMapNode,
        topLeft: Offset,
        size: Size,
        visualState: MindMapNodeVisualState,
        style: MindMapStyle,
        textMeasurer: TextMeasurer,
    )
}

object DefaultMindMapCanvasNodeRenderer : MindMapCanvasNodeRenderer {
    override fun DrawScope.draw(
        node: MindMapNode,
        topLeft: Offset,
        size: Size,
        visualState: MindMapNodeVisualState,
        style: MindMapStyle,
        textMeasurer: TextMeasurer,
    ) {
        val alpha = when {
            visualState.isDragGhost -> style.dragGhostAlpha
            visualState.isDragging -> style.draggedNodeAlpha
            else -> 1f
        }
        val cornerPx = style.cornerRadius.toPx()
        val bgColor = (node.color ?: style.defaultNodeColor).copy(alpha = alpha)

        drawRoundRect(
            color = bgColor,
            topLeft = topLeft,
            size = size,
            cornerRadius = CornerRadius(cornerPx),
        )

        if (visualState.isSelected || visualState.isDragGhost) {
            drawRoundRect(
                color = style.selectedStrokeColor.copy(alpha = alpha),
                topLeft = topLeft,
                size = size,
                cornerRadius = CornerRadius(cornerPx),
                style = Stroke(width = style.selectedStrokeWidth.toPx()),
            )
        }

        val paddingH = style.contentPaddingHorizontal.toPx()
        val paddingV = style.contentPaddingVertical.toPx()
        val icon = node.icon
        var textStartX = topLeft.x + paddingH
        val subtitle = node.subtitle.takeIf { it.isNotBlank() }

        if (icon != null) {
            val iconResult = textMeasurer.measure(text = icon, style = style.iconTextStyle)
            val iconY = if (subtitle != null) {
                topLeft.y + paddingV
            } else {
                topLeft.y + (size.height - iconResult.size.height) / 2f
            }
            drawText(textLayoutResult = iconResult, topLeft = Offset(topLeft.x + paddingH, iconY))
            textStartX += iconResult.size.width + style.iconSpacing.toPx()
        }

        val availableWidth = (topLeft.x + size.width - paddingH - textStartX)
            .toInt()
            .coerceAtLeast(1)
        val titleResult = textMeasurer.measure(
            text = node.title,
            style = style.titleTextStyle,
            constraints = Constraints.fixedWidth(availableWidth),
            maxLines = if (subtitle != null) 1 else 2,
            overflow = TextOverflow.Ellipsis,
        )
        val titleY = if (subtitle != null) {
            topLeft.y + paddingV
        } else {
            topLeft.y + (size.height - titleResult.size.height) / 2f
        }
        drawText(textLayoutResult = titleResult, topLeft = Offset(textStartX, titleY))

        if (subtitle != null) {
            val subtitleResult = textMeasurer.measure(
                text = subtitle,
                style = style.subtitleTextStyle,
                constraints = Constraints.fixedWidth(availableWidth),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            drawText(
                textLayoutResult = subtitleResult,
                topLeft = Offset(textStartX, titleY + titleResult.size.height + style.subtitleSpacing.toPx()),
            )
        }
    }
}

