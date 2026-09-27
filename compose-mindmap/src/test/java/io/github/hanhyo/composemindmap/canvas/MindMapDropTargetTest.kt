package io.github.hanhyo.composemindmap.canvas

import io.github.hanhyo.composemindmap.model.MindMapNode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MindMapDropTargetTest {
    private val nodes = listOf(
        MindMapNode("root", "Root"),
        MindMapNode("a", "A", parentId = "root"),
        MindMapNode("b", "B", parentId = "a"),
        MindMapNode("peer", "Peer", parentId = "root"),
    )

    @Test fun descendantsCannotBeDropParents() {
        assertTrue(nodes.isDescendantOf("b", "a"))
        assertTrue(nodes.isDescendantOf("b", "root"))
        assertFalse(nodes.isDescendantOf("peer", "a"))
        assertFalse(nodes.isDescendantOf("a", "a"))
    }
}
