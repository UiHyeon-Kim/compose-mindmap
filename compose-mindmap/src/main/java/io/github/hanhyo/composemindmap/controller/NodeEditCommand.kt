package io.github.hanhyo.composemindmap.controller

import io.github.hanhyo.composemindmap.model.MindMapNode

internal sealed interface NodeEditCommand {
    val description: String

    data class AddNode(
        val node: MindMapNode,
        override val description: String = "노드 추가",
    ) : NodeEditCommand

    data class UpdateNode(
        val before: MindMapNode,
        val after: MindMapNode,
        override val description: String = "노드 수정",
    ) : NodeEditCommand

    data class DeleteNode(
        val target: MindMapNode,
        val subtree: List<MindMapNode>,
        override val description: String = "노드 삭제",
    ) : NodeEditCommand

    data class MoveNode(
        val nodeId: String,
        val newParentId: String,
        val oldParentId: String,
        override val description: String = "노드 이동",
    ) : NodeEditCommand
}
