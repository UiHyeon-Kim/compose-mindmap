package io.github.hanhyo.composemindmap.canvas

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.hanhyo.composemindmap.layout.MindMapLayoutNode
import io.github.hanhyo.composemindmap.model.MindMapStyle

@Immutable
/** Geometry for an add-child affordance; [touchRadius] may exceed [visualRadius]. */
data class MindMapAddChildAction(
    val nodeId: String,
    val center: Offset,
    val visualRadius: Float,
    val touchRadius: Float,
)

/** Positions the add-child affordance for a laid-out node. */
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
            touchRadius = maxOf(radius + style.addButtonTouchPadding.value * density.density, with(density) { 24.dp.toPx() }),
        )
    }
}
