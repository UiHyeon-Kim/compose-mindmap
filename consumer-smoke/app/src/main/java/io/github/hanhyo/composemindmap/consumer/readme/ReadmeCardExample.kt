package io.github.hanhyo.composemindmap.consumer.readme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.model.MindMapNode

// Copied from the compact custom-card example in README.md.
@Composable
fun CustomCardMindMap(nodes: List<MindMapNode>) {
    MindMapCanvas(
        nodes = nodes,
        modifier = Modifier.fillMaxSize(),
        nodeSize = { DpSize(160.dp, 72.dp) },
        nodeContent = { node, _ -> BasicText(node.title) },
    )
}
