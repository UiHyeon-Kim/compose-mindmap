package io.github.hanhyo.composemindmap.canvas

import androidx.compose.ui.geometry.Offset
import io.github.hanhyo.composemindmap.layout.MindMapEdgeDirection
import io.github.hanhyo.composemindmap.layout.MindMapLayoutEdge
import org.junit.Assert.assertEquals
import org.junit.Test

class MindMapEdgeRendererTest {

    @Test
    fun `orthogonal top-down edge uses vertical horizontal vertical segments`() {
        val edge = edge(
            start = Offset(20f, 40f),
            end = Offset(180f, 140f),
            direction = MindMapEdgeDirection.VERTICAL,
        )

        val path = orthogonalEdgePath(edge)

        assertEquals(Offset(20f, 90f), path.firstCorner)
        assertEquals(Offset(180f, 90f), path.secondCorner)
    }

    @Test
    fun `orthogonal left-to-right edge uses horizontal vertical horizontal segments`() {
        val edge = edge(
            start = Offset(40f, 20f),
            end = Offset(140f, 180f),
            direction = MindMapEdgeDirection.HORIZONTAL,
        )

        val path = orthogonalEdgePath(edge)

        assertEquals(Offset(90f, 20f), path.firstCorner)
        assertEquals(Offset(90f, 180f), path.secondCorner)
    }

    @Test
    fun `curved top-down edge keeps straight vertical stems at node boundaries`() {
        val edge = edge(
            start = Offset(20f, 40f),
            end = Offset(180f, 140f),
            direction = MindMapEdgeDirection.VERTICAL,
        )

        val path = curvedEdgePath(edge)

        assertEquals(Offset(20f, 50f), path.firstStemEnd)
        assertEquals(Offset(20f, 90f), path.firstCurveFirstControl)
        assertEquals(Offset(92f, 90f), path.firstCurveSecondControl)
        assertEquals(Offset(92f, 90f), path.middleStart)
        assertEquals(Offset(108f, 90f), path.middleEnd)
        assertEquals(Offset(108f, 90f), path.secondCurveFirstControl)
        assertEquals(Offset(180f, 90f), path.secondCurveSecondControl)
        assertEquals(Offset(180f, 130f), path.secondStemStart)
    }

    @Test
    fun `curved left-to-right edge keeps straight horizontal stems at node boundaries`() {
        val edge = edge(
            start = Offset(40f, 20f),
            end = Offset(140f, 180f),
            direction = MindMapEdgeDirection.HORIZONTAL,
        )

        val path = curvedEdgePath(edge)

        assertEquals(Offset(50f, 20f), path.firstStemEnd)
        assertEquals(Offset(90f, 20f), path.firstCurveFirstControl)
        assertEquals(Offset(90f, 92f), path.firstCurveSecondControl)
        assertEquals(Offset(90f, 92f), path.middleStart)
        assertEquals(Offset(90f, 108f), path.middleEnd)
        assertEquals(Offset(90f, 108f), path.secondCurveFirstControl)
        assertEquals(Offset(90f, 180f), path.secondCurveSecondControl)
        assertEquals(Offset(130f, 180f), path.secondStemStart)
    }

    private fun edge(
        start: Offset,
        end: Offset,
        direction: MindMapEdgeDirection,
    ) = MindMapLayoutEdge(
        parentId = "parent",
        childId = "child",
        start = start,
        end = end,
        direction = direction,
    )
}
