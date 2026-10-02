package io.github.hanhyo.composemindmap.sample

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SampleActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<SampleActivity>()

    @Test
    fun 샘플에서_노드를_선택해_추가하고_실행취소와_재실행한다() {
        composeRule.onNodeWithText("Compose MindMap").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Plan").performClick()
        composeRule.onNodeWithText("Controls").performClick()
        composeRule.onNodeWithText("Add child").performClick()
        composeRule.onNodeWithContentDescription("New topic").assertExists()
        composeRule.onNodeWithText("Mind map controls").assertDoesNotExist()

        composeRule.onNodeWithText("Controls").performClick()
        composeRule.onNodeWithText("Undo").performClick()
        composeRule.onNodeWithContentDescription("New topic").assertDoesNotExist()
        composeRule.onNodeWithText("Mind map controls").assertDoesNotExist()

        composeRule.onNodeWithText("Controls").performClick()
        composeRule.onNodeWithText("Redo").performClick()
        composeRule.onNodeWithContentDescription("New topic").assertExists()
        composeRule.onNodeWithText("Mind map controls").assertDoesNotExist()
    }

    @Test
    fun 샘플에서_선택한_브랜치를_접고_다시_펼친다() {
        composeRule.onNodeWithContentDescription("Plan").performClick()
        composeRule.onNodeWithText("Controls").performClick()
        composeRule.onNodeWithText("Collapse").performClick()
        composeRule.onNodeWithText("Mind map controls").assertDoesNotExist()

        composeRule.onNodeWithContentDescription("Layouts").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Gestures").assertDoesNotExist()

        composeRule.onNodeWithText("Controls").performClick()
        composeRule.onNodeWithText("Expand").performClick()
        composeRule.onNodeWithContentDescription("Layouts").assertExists()
        composeRule.onNodeWithContentDescription("Gestures").assertExists()
    }

    @Test
    fun 샘플의_커스텀_카드에서_핀치_줌과_두_손가락_이동이_적용된다() {
        composeRule.onNodeWithText("Controls").performClick()
        composeRule.onNodeWithText("Card: Canvas").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Mind map controls").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Build an idea, ROOT").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Plan, TOPIC").assertIsDisplayed()

        val before = composeRule.onNodeWithContentDescription("Build an idea, ROOT").getUnclippedBoundsInRoot()
        var pinchStart = Offset.Zero
        var pinchEnd = Offset.Zero
        composeRule.onNodeWithTag("mind-map-viewport").performTouchInput {
            val start = Offset(center.x, center.y)
            val end = start + Offset(28f, 24f)
            pinchStart = start
            pinchEnd = end
            pinch(
                start0 = start + Offset(-70f, 0f),
                end0 = end + Offset(-125f, 0f),
                start1 = start + Offset(70f, 0f),
                end1 = end + Offset(125f, 0f),
            )
        }
        composeRule.waitForIdle()

        val after = composeRule.onNodeWithContentDescription("Build an idea, ROOT").getUnclippedBoundsInRoot()
        val beforeWidth = before.right.value - before.left.value
        val afterWidth = after.right.value - after.left.value
        val beforeCenter = Offset((before.left.value + before.right.value) / 2f, (before.top.value + before.bottom.value) / 2f)
        val afterCenter = Offset((after.left.value + after.right.value) / 2f, (after.top.value + after.bottom.value) / 2f)
        assertTrue(
            "Pinching outward should enlarge the node: viewport pinch $pinchStart → $pinchEnd, width $beforeWidth → $afterWidth, center $beforeCenter → $afterCenter",
            afterWidth > beforeWidth,
        )
        assertNotEquals(
            "The moving pinch centroid should translate the node: viewport pinch $pinchStart → $pinchEnd, center $beforeCenter → $afterCenter",
            beforeCenter,
            afterCenter,
        )
    }

    @Test
    fun 액티비티를_재생성해도_사용자가_조정한_뷰포트를_복원한다() {
        val initialBounds = composeRule.onNodeWithContentDescription("Build an idea").getUnclippedBoundsInRoot()
        composeRule.onNodeWithText("Zoom +").performClick()
        composeRule.waitForIdle()
        val zoomedBounds = composeRule.onNodeWithContentDescription("Build an idea").getUnclippedBoundsInRoot()
        assertNotEquals(initialBounds, zoomedBounds)

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        val recreatedBounds = composeRule.onNodeWithContentDescription("Build an idea").getUnclippedBoundsInRoot()
        assertEquals(zoomedBounds, recreatedBounds)
    }
}
