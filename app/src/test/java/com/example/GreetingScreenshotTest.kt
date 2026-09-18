package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleTrack = com.example.data.model.TrackEntity(
      id = "test_1",
      title = "Midnight Horizon",
      artist = "Neon Echo",
      album = "Synthwave",
      coverUrl = "",
      audioUrl = "",
      durationMs = 210000L,
      source = "SPOTIFY"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.components.TrackItem(
          track = sampleTrack,
          isPlaying = false,
          isCurrent = true,
          downloadProgress = null,
          onTrackClick = {},
          onToggleFavorite = {},
          onDownloadClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
