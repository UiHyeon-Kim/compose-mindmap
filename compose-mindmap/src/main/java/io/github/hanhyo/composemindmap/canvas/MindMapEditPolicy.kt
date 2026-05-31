package io.github.hanhyo.composemindmap.canvas

import io.github.hanhyo.composemindmap.model.MindMapNode

interface MindMapEditPolicy {
    fun canAddChild(node: MindMapNode): Boolean
    fun canDrag(node: MindMapNode): Boolean
    fun canDrop(dragged: MindMapNode, target: MindMapNode): Boolean
}

object DefaultMindMapEditPolicy : MindMapEditPolicy {
    override fun canAddChild(node: MindMapNode): Boolean = true
    override fun canDrag(node: MindMapNode): Boolean = node.parentId != null
    override fun canDrop(dragged: MindMapNode, target: MindMapNode): Boolean = true
}
