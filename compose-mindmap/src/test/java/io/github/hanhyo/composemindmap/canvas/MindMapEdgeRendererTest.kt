package io.github.hanhyo.composemindmap.canvas

import androidx.compose.ui.geometry.Offset
import io.github.hanhyo.composemindmap.layout.MindMapLayoutEdge
import org.junit.Assert.assertEquals
import org.junit.Test

class MindMapEdgeRendererTest {

    @Test
    fun `orthogonal top-down edge uses vertical horizontal vertical segments`() {
        val edge = edge(start = Offset(20f, 40f), end = Offset(80f, 140f))

        val path = orthogonalEdgePath(edge)

        assertEquals(Offset(20f, 90f), path.firstCorner)
        assertEquals(Offset(80f, 90f), path.secondCorner)
    }

    @Test
    fun `orthogonal left-to-right edge uses horizontal vertical horizontal segments`() {
        val edge = edge(start = Offset(40f, 20f), end = Offset(140f, 80f))

        val path = orthogonalEdgePath(edge)

        assertEquals(Offset(90f, 20f), path.firstCorner)
        assertEquals(Offset(90f, 80f), path.secondCorner)
    }

    @Test
    fun `curved top-down edge starts and ends perpendicular to node boundaries`() {
        val edge = edge(start = Offset(20f, 40f), end = Offset(80f, 140f))

        val path = curvedEdgePath(edge)

        assertEquals(Offset(20f, 90f), path.firstControl)
        assertEquals(Offset(80f, 90f), path.secondControl)
    }

    @Test
    fun `curved left-to-right edge starts and ends perpendicular to node boundaries`() {
        val edge = edge(start = Offset(40f, 20f), end = Offset(140f, 80f))

        val path = curvedEdgePath(edge)

        assertEquals(Offset(90f, 20f), path.firstControl)
        assertEquals(Offset(90f, 80f), path.secondControl)
    }

    private fun edge(start: Offset, end: Offset) = MindMapLayoutEdge(
        parentId = "parent",
        childId = "child",
        start = start,
        end = end,
    )
}
