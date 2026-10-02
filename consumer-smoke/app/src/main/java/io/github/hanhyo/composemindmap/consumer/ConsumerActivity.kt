package io.github.hanhyo.composemindmap.consumer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.model.MindMapNode

class ConsumerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MyMindMap() }
    }
}

@Composable
fun MyMindMap() {
    val nodes = listOf(
        MindMapNode(id = "root", title = "My idea"),
        MindMapNode(id = "research", title = "Research", parentId = "root"),
        MindMapNode(id = "build", title = "Build", parentId = "root"),
    )
    MindMapCanvas(nodes = nodes, modifier = Modifier.fillMaxSize())
}
