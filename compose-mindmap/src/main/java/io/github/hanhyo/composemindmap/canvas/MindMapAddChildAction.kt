package io.github.hanhyo.composemindmap.canvas

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import io.github.hanhyo.composemindmap.layout.MindMapLayoutNode
import io.github.hanhyo.composemindmap.model.MindMapStyle

@Immutable
data class MindMapAddChildAction(
    val nodeId: String,
    val center: Offset,
    val visualRadius: Float,
    val touchRadius: Float,
)

fun interface MindMapAddChildActionLayout {
    fun layout(node: MindMapLayoutNode, style: MindMapStyle, density: Density): MindMapAddChildAction
}

object DefaultMindMapAddChildActionLayout : MindMapAddChildActionLayout {
    override fun layout(
        node: MindMapLayoutNode,
        style: MindMapStyle,
        density: Density,
    ): MindMapAddChildAction {
        val radius = style.addButtonRadius.value * density.density
        val cx = node.offset.x + node.size.width / 2f
        val cy = node.offset.y + node.size.height + radius + style.addButtonSpacing.value * density.density
        return MindMapAddChildAction(
            nodeId = node.node.id,
            center = Offset(cx, cy),
            visualRadius = radius,
            touchRadius = radius + style.addButtonTouchPadding.value * density.density,
        )
    }
}
