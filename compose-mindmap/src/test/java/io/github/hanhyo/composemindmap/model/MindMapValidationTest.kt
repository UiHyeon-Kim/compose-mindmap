package io.github.hanhyo.composemindmap.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MindMapValidationTest {

    @Test
    fun `empty nodes are valid`() {
        assertSame(MindMapValidationResult.Valid, validateMindMapNodes(emptyList()))
    }

    @Test
    fun `single rooted tree is valid`() {
        assertSame(MindMapValidationResult.Valid, validateMindMapNodes(validTree()))
    }

    @Test
    fun `multiple roots are rejected`() {
        val result = validateMindMapNodes(
            listOf(
                MindMapNode(id = "root-1", title = "root-1"),
                MindMapNode(id = "root-2", title = "root-2"),
            ),
        )

        assertHasError<MindMapValidationError.InvalidRootCount>(result)
    }

    @Test
    fun `duplicate ids are rejected`() {
        val result = validateMindMapNodes(
            listOf(
                MindMapNode(id = "root", title = "root"),
                MindMapNode(id = "child", title = "child", parentId = "root"),
                MindMapNode(id = "child", title = "duplicate", parentId = "root"),
            ),
        )

        assertHasError<MindMapValidationError.DuplicateNodeId>(result)
    }

    @Test
    fun `missing parent is rejected`() {
        val result = validateMindMapNodes(
            listOf(
                MindMapNode(id = "root", title = "root"),
                MindMapNode(id = "orphan", title = "orphan", parentId = "missing"),
            ),
        )

        assertHasError<MindMapValidationError.MissingParent>(result)
        assertHasError<MindMapValidationError.UnreachableNode>(result)
    }

    @Test
    fun `cycle is rejected`() {
        val result = validateMindMapNodes(
            listOf(
                MindMapNode(id = "root", title = "root"),
                MindMapNode(id = "a", title = "a", parentId = "b"),
                MindMapNode(id = "b", title = "b", parentId = "a"),
            ),
        )

        assertHasError<MindMapValidationError.CycleDetected>(result)
        val invalid = result as MindMapValidationResult.Invalid
        assertEquals(2, invalid.errors.filterIsInstance<MindMapValidationError.UnreachableNode>().size)
    }

    private inline fun <reified T : MindMapValidationError> assertHasError(result: MindMapValidationResult) {
        assertTrue(result is MindMapValidationResult.Invalid)
        assertTrue((result as MindMapValidationResult.Invalid).errors.any { it is T })
    }

    private fun validTree() = listOf(
        MindMapNode(id = "root", title = "root"),
        MindMapNode(id = "child", title = "child", parentId = "root"),
    )
}
