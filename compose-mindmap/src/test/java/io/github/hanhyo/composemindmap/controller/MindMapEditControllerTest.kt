package io.github.hanhyo.composemindmap.controller

import io.github.hanhyo.composemindmap.model.MindMapNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MindMapEditControllerTest {

    private val controller = MindMapEditController()

    @Test
    fun `add update and delete subtree can be undone and redone`() {
        val child = MindMapNode(id = "child", title = "child", parentId = "root")
        val grandchild = MindMapNode(id = "grandchild", title = "grandchild", parentId = "child")
        val added = controller.addNode(listOf(root), child)
        val withGrandchild = controller.addNode(added, grandchild)
        val updatedChild = child.copy(title = "updated")
        val updated = controller.updateNode(withGrandchild, child, updatedChild)
        val deleted = controller.deleteNode(updated, updatedChild, controller.collectSubtree(updated, child.id))

        assertEquals(listOf(root), deleted)
        assertEquals(updated, controller.undo(deleted))
        assertEquals(withGrandchild, controller.undo(updated))
        assertEquals(added, controller.undo(withGrandchild))
        assertEquals(listOf(root), controller.undo(added))
        assertEquals(added, controller.redo(listOf(root)))
    }

    @Test
    fun `new command clears redo history`() {
        val child = MindMapNode(id = "child", title = "child", parentId = "root")
        val added = controller.addNode(listOf(root), child)
        assertEquals(listOf(root), controller.undo(added))
        assertTrue(controller.canRedo)

        controller.addNode(listOf(root), MindMapNode(id = "other", title = "other", parentId = "root"))

        assertFalse(controller.canRedo)
    }

    @Test
    fun `valid move changes parent and can be undone`() {
        val nodes = listOf(
            root,
            MindMapNode(id = "a", title = "a", parentId = "root"),
            MindMapNode(id = "b", title = "b", parentId = "root"),
        )

        val moved = controller.moveNode(nodes, nodeId = "a", newParentId = "b")

        assertEquals("b", moved.first { it.id == "a" }.parentId)
        assertEquals(nodes, controller.undo(moved))
    }

    @Test
    fun `invalid and no-op moves do not create history`() {
        val nodes = listOf(
            root,
            MindMapNode(id = "a", title = "a", parentId = "root"),
            MindMapNode(id = "b", title = "b", parentId = "a"),
        )

        assertEquals(nodes, controller.moveNode(nodes, nodeId = "root", newParentId = "a"))
        assertEquals(nodes, controller.moveNode(nodes, nodeId = "a", newParentId = "a"))
        assertEquals(nodes, controller.moveNode(nodes, nodeId = "a", newParentId = "b"))
        assertEquals(nodes, controller.moveNode(nodes, nodeId = "a", newParentId = "root"))
        assertEquals(nodes, controller.moveNode(nodes, nodeId = "missing", newParentId = "root"))
        assertEquals(nodes, controller.moveNode(nodes, nodeId = "a", newParentId = "missing"))
        assertFalse(controller.canUndo)
        assertNull(controller.undo(nodes))
    }

    @Test
    fun `collect subtree terminates when malformed nodes contain cycle`() {
        val nodes = listOf(
            MindMapNode(id = "a", title = "a", parentId = "b"),
            MindMapNode(id = "b", title = "b", parentId = "a"),
        )

        assertEquals(listOf("b"), controller.collectSubtree(nodes, "a").map { it.id })
    }

    private companion object {
        val root = MindMapNode(id = "root", title = "root")
    }
}
