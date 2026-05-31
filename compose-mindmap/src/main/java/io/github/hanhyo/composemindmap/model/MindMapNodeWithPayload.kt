package io.github.hanhyo.composemindmap.model

import androidx.compose.runtime.Immutable

@Immutable
data class MindMapNodeWithPayload<T>(
    val node: MindMapNode,
    val payload: T,
)

fun <T> MindMapNode.withPayload(payload: T): MindMapNodeWithPayload<T> =
    MindMapNodeWithPayload(node = this, payload = payload)

fun <T> MindMapNodeWithPayload<T>.mapNode(
    transform: (MindMapNode) -> MindMapNode,
): MindMapNodeWithPayload<T> = copy(node = transform(node))
