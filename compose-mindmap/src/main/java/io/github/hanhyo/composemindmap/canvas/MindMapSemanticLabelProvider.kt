package io.github.hanhyo.composemindmap.canvas

import io.github.hanhyo.composemindmap.model.MindMapNode

fun interface MindMapSemanticLabelProvider {
    fun label(node: MindMapNode): String
}

object DefaultMindMapSemanticLabelProvider : MindMapSemanticLabelProvider {
    override fun label(node: MindMapNode): String = node.title
}
