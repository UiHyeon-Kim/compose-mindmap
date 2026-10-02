package io.github.hanhyo.composemindmap.consumer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.model.MindMapNode

class ConsumerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val usesLightSystemBarIcons =
                    MaterialTheme.colorScheme.background.luminance() > 0.5f
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = usesLightSystemBarIcons
                        isAppearanceLightNavigationBars = usesLightSystemBarIcons
                    }
                }
                MyMindMap()
            }
        }
    }
}

@Composable
private fun MyMindMap() {
    val nodes = remember {
        listOf(
            MindMapNode(id = "idea", title = "My idea"),
            MindMapNode(id = "research", title = "Research", parentId = "idea"),
            MindMapNode(id = "build", title = "Build", parentId = "idea"),
        )
    }
    var showCustomCards by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        TextButton(onClick = { showCustomCards = !showCustomCards }) {
            Text(if (showCustomCards) "Use Quick Start" else "Use custom cards")
        }
        Box(Modifier.weight(1f)) {
            if (showCustomCards) {
                CustomCardMindMap(nodes)
            } else {
                MindMapCanvas(nodes = nodes, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun CustomCardMindMap(nodes: List<MindMapNode>) {
    MindMapCanvas(
        nodes = nodes,
        modifier = Modifier.fillMaxSize(),
        nodeSize = { node ->
            if (node.parentId == null) DpSize(196.dp, 88.dp) else DpSize(160.dp, 72.dp)
        },
        nodeContent = { node, visualState ->
            val colors = MaterialTheme.colorScheme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = if (visualState.isSelected) colors.primaryContainer else colors.surfaceVariant,
                tonalElevation = if (visualState.isSelected) 4.dp else 1.dp,
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(node.title, style = MaterialTheme.typography.titleSmall)
                    if (node.subtitle.isNotBlank()) {
                        Text(node.subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
    )
}
