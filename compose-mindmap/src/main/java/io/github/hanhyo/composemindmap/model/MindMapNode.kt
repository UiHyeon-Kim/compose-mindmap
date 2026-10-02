package io.github.hanhyo.composemindmap.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
/** One tree node. Exactly one node has a null [parentId]; every [id] must be unique. */
data class MindMapNode(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val parentId: String? = null,
    val color: Color? = null,
    val icon: String? = null,
)
