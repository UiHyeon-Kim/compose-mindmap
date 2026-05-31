package io.github.hanhyo.composemindmap.model

sealed interface MindMapValidationResult {
    data object Valid : MindMapValidationResult

    data class Invalid(
        val errors: List<MindMapValidationError>,
    ) : MindMapValidationResult
}

sealed interface MindMapValidationError {
    data class InvalidRootCount(val actual: Int) : MindMapValidationError
    data class DuplicateNodeId(val nodeId: String) : MindMapValidationError
    data class MissingParent(val nodeId: String, val parentId: String) : MindMapValidationError
    data class CycleDetected(val nodeId: String) : MindMapValidationError
    data class UnreachableNode(val nodeId: String) : MindMapValidationError
}

fun validateMindMapNodes(nodes: List<MindMapNode>): MindMapValidationResult {
    if (nodes.isEmpty()) return MindMapValidationResult.Valid

    val errors = mutableListOf<MindMapValidationError>()
    val duplicates = nodes.groupingBy { it.id }.eachCount().filterValues { it > 1 }.keys
    duplicates.forEach { errors += MindMapValidationError.DuplicateNodeId(it) }

    val uniqueNodes = nodes.distinctBy { it.id }
    val nodeIds = uniqueNodes.mapTo(mutableSetOf()) { it.id }
    val roots = uniqueNodes.filter { it.parentId == null }
    if (roots.size != 1) {
        errors += MindMapValidationError.InvalidRootCount(roots.size)
    }

    uniqueNodes.forEach { node ->
        val parentId = node.parentId
        if (parentId != null && parentId !in nodeIds) {
            errors += MindMapValidationError.MissingParent(node.id, parentId)
        }
    }

    val parentMap = uniqueNodes.associate { it.id to it.parentId }
    uniqueNodes.forEach { node ->
        val visited = mutableSetOf<String>()
        var currentId: String? = node.id
        while (currentId != null && currentId in parentMap) {
            if (!visited.add(currentId)) {
                errors += MindMapValidationError.CycleDetected(node.id)
                break
            }
            currentId = parentMap[currentId]
        }
    }

    val root = roots.singleOrNull()
    if (root != null) {
        val childrenMap = uniqueNodes.groupBy { it.parentId }
        val reachable = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(root.id)
        while (queue.isNotEmpty()) {
            val nodeId = queue.removeFirst()
            if (!reachable.add(nodeId)) continue
            childrenMap[nodeId].orEmpty().forEach { queue.add(it.id) }
        }
        uniqueNodes.filter { it.id !in reachable }.forEach {
            errors += MindMapValidationError.UnreachableNode(it.id)
        }
    }

    return if (errors.isEmpty()) {
        MindMapValidationResult.Valid
    } else {
        MindMapValidationResult.Invalid(errors.distinct())
    }
}
