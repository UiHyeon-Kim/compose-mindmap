package io.github.hanhyo.composemindmap.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
data class MindMapStyle(
    val nodeWidth: Dp = 160.dp,
    val nodeHeight: Dp = 56.dp,
    val horizontalGap: Dp = 24.dp,
    val verticalGap: Dp = 48.dp,
    val cornerRadius: Dp = 12.dp,
    val edgeStrokeWidth: Dp = 2.dp,
    val selectedStrokeWidth: Dp = 2.dp,
    val contentPaddingHorizontal: Dp = 8.dp,
    val contentPaddingVertical: Dp = 6.dp,
    val iconSpacing: Dp = 4.dp,
    val subtitleSpacing: Dp = 2.dp,
    val addButtonRadius: Dp = 10.dp,
    val addButtonSpacing: Dp = 4.dp,
    val addButtonTouchPadding: Dp = 4.dp,
    val dropTargetStrokeWidth: Dp = 2.dp,
    val dropTargetDashLength: Dp = 8.dp,
    val dropTargetDashGap: Dp = 4.dp,
    val draggedNodeAlpha: Float = 0.3f,
    val dragGhostAlpha: Float = 0.6f,
    val defaultNodeColor: Color = Color(0xFFEEEEEE),
    val selectedStrokeColor: Color = Color(0xFF96A46B),
    val edgeColor: Color = Color(0xFFDADBCD),
    val dropTargetColor: Color = Color(0xFF4CAF50),
    val addButtonColor: Color = selectedStrokeColor,
    val titleTextStyle: TextStyle = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF1A1A1A),
    ),
    val subtitleTextStyle: TextStyle = TextStyle(
        fontSize = 11.sp,
        color = Color(0xFF888888),
    ),
    val iconTextStyle: TextStyle = TextStyle(fontSize = 14.sp),
    val addButtonTextStyle: TextStyle = TextStyle(
        fontSize = 14.sp,
        color = Color.White,
    ),
)

val MindMapStyle.defaultNodeSize: DpSize
    get() = DpSize(nodeWidth, nodeHeight)
