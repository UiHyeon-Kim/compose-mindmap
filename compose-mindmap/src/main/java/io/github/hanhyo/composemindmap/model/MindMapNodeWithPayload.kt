package io.github.hanhyo.composemindmap.model

import androidx.compose.runtime.Immutable

@Immutable
/** Associates arbitrary typed app data with a renderable [node]. */
data class MindMapNodeWithPayload<T>(
    val node: MindMapNode,
    val payload: T,
)

/** Wraps this node with typed [payload] for [io.github.hanhyo.composemindmap.canvas.PayloadMindMapCanvas]. */
fun <T> MindMapNode.withPayload(payload: T): MindMapNodeWithPayload<T> =
    MindMapNodeWithPayload(node = this, payload = payload)

/** Copies the wrapper after changing only its node, preserving [payload]. */
fun <T> MindMapNodeWithPayload<T>.mapNode(
    transform: (MindMapNode) -> MindMapNode,
): MindMapNodeWithPayload<T> = copy(node = transform(node))
