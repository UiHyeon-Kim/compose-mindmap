package io.github.hanhyo.composemindmap.controller

import io.github.hanhyo.composemindmap.model.MindMapNode

class MindMapEditController {

    private val undoStack = ArrayDeque<NodeEditCommand>()
    private val redoStack = ArrayDeque<NodeEditCommand>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun reset() {
        undoStack.clear()
        redoStack.clear()
    }

    fun addNode(current: List<MindMapNode>, node: MindMapNode): List<MindMapNode> {
        if (current.any { it.id == node.id }) return current
        if (node.parentId != null && current.none { it.id == node.parentId }) return current
        return execute(NodeEditCommand.AddNode(node), current)
    }

    fun updateNode(current: List<MindMapNode>, before: MindMapNode, after: MindMapNode): List<MindMapNode> {
        if (before == after || current.none { it.id == before.id } || before.id != after.id) return current
        return execute(NodeEditCommand.UpdateNode(before, after), current)
    }

    fun deleteNode(current: List<MindMapNode>, target: MindMapNode, subtree: List<MindMapNode>): List<MindMapNode> {
        if (current.none { it.id == target.id }) return current
        return execute(NodeEditCommand.DeleteNode(target, subtree), current)
    }

    fun moveNode(current: List<MindMapNode>, nodeId: String, newParentId: String): List<MindMapNode> {
        val target = current.firstOrNull { it.id == nodeId } ?: return current
        val oldParentId = target.parentId ?: return current
        if (newParentId == oldParentId || current.none { it.id == newParentId }) return current
        if (isCyclic(current, nodeId, newParentId)) return current
        return execute(NodeEditCommand.MoveNode(nodeId, newParentId, oldParentId), current)
    }

    fun undo(current: List<MindMapNode>): List<MindMapNode>? {
        val command = undoStack.removeLastOrNull() ?: return null
        redoStack.addLast(command)
        return applyReverse(command, current)
    }

    fun redo(current: List<MindMapNode>): List<MindMapNode>? {
        val command = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(command)
        return applyForward(command, current)
    }

    fun isCyclic(nodes: List<MindMapNode>, nodeId: String, targetParentId: String): Boolean {
        if (nodeId == targetParentId) return true
        val parentMap = nodes.associate { it.id to it.parentId }
        val visited = mutableSetOf<String>()
        var current: String? = targetParentId
        while (current != null) {
            if (current == nodeId) return true
            if (!visited.add(current)) return true
            current = parentMap[current]
        }
        return false
    }

    fun collectSubtree(nodes: List<MindMapNode>, rootId: String): List<MindMapNode> {
        val childrenMap = nodes.groupBy { it.parentId }
        val result = mutableListOf<MindMapNode>()
        val visited = mutableSetOf(rootId)
        fun collect(id: String) {
            for (child in childrenMap[id] ?: return) {
                if (!visited.add(child.id)) continue
                result += child
                collect(child.id)
            }
        }
        collect(rootId)
        return result
    }

    private fun execute(command: NodeEditCommand, current: List<MindMapNode>): List<MindMapNode> {
        undoStack.addLast(command)
        redoStack.clear()
        return applyForward(command, current)
    }

    private fun applyForward(command: NodeEditCommand, nodes: List<MindMapNode>): List<MindMapNode> =
        when (command) {
            is NodeEditCommand.AddNode -> nodes + command.node
            is NodeEditCommand.UpdateNode -> nodes.map { if (it.id == command.after.id) command.after else it }
            is NodeEditCommand.DeleteNode -> {
                val ids = (listOf(command.target) + command.subtree).map { it.id }.toSet()
                nodes.filter { it.id !in ids }
            }
            is NodeEditCommand.MoveNode -> nodes.map {
                if (it.id == command.nodeId) it.copy(parentId = command.newParentId) else it
            }
        }

    private fun applyReverse(command: NodeEditCommand, nodes: List<MindMapNode>): List<MindMapNode> =
        when (command) {
            is NodeEditCommand.AddNode -> nodes.filter { it.id != command.node.id }
            is NodeEditCommand.UpdateNode -> nodes.map { if (it.id == command.before.id) command.before else it }
            is NodeEditCommand.DeleteNode -> nodes + command.target + command.subtree
            is NodeEditCommand.MoveNode -> nodes.map {
                if (it.id == command.nodeId) it.copy(parentId = command.oldParentId) else it
            }
        }
}
