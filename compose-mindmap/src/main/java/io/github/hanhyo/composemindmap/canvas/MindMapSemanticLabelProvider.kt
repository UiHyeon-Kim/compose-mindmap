package io.github.hanhyo.composemindmap.canvas

import io.github.hanhyo.composemindmap.model.MindMapNode

/** Supplies a spoken label using both node data and selection/collapse state. */
fun interface MindMapSemanticLabelProvider {
    fun label(node: MindMapNode, visualState: MindMapNodeVisualState): String
}

object DefaultMindMapSemanticLabelProvider : MindMapSemanticLabelProvider {
    override fun label(node: MindMapNode, visualState: MindMapNodeVisualState): String = node.title
}
