package io.github.hanhyo.composemindmap.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class MindMapNodeWithPayloadTest {

    @Test
    fun `withPayload wraps node and payload`() {
        val node = MindMapNode(id = "root", title = "root")
        val payload = SamplePayload(value = 1)

        val wrapped = node.withPayload(payload)

        assertSame(node, wrapped.node)
        assertSame(payload, wrapped.payload)
    }

    @Test
    fun `mapNode changes node and preserves payload`() {
        val payload = SamplePayload(value = 1)
        val wrapped = MindMapNode(id = "root", title = "before").withPayload(payload)

        val mapped = wrapped.mapNode { it.copy(title = "after") }

        assertEquals("after", mapped.node.title)
        assertSame(payload, mapped.payload)
    }

    private data class SamplePayload(val value: Int)
}
