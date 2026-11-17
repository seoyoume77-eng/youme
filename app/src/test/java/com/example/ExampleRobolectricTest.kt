package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.HealthDatabase
import com.example.data.HealthRepository
import com.example.data.MedicationSlot
import com.example.ui.HealthHomeScreen
import com.example.ui.HealthViewModel
import com.example.ui.theme.SeniorHealthTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var context: Context
  private lateinit var database: HealthDatabase
  private lateinit var repository: HealthRepository

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    database =
      Room.inMemoryDatabaseBuilder(context, HealthDatabase::class.java)
        .allowMainThreadQueries()
        .setQueryExecutor(Runnable::run)
        .setTransactionExecutor(Runnable::run)
        .build()
    repository = HealthRepository(database.dailyHealthDao())
  }

  @After
  fun teardown() {
    database.close()
  }

  @Test
  fun `app name matches Korean title`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("오늘건강", appName)
  }

  @Test
  fun `medication and water persistence survives repository reload`() = runBlocking {
    val todayKey = HealthRepository.currentDateKey()
    repository.toggleMedication(todayKey, MedicationSlot.MORNING, "오전 8:30")
    repository.toggleMedication(todayKey, MedicationSlot.DINNER, "오후 7:10")
    repository.incrementWater(todayKey)
    repository.incrementWater(todayKey)
    repository.incrementWater(todayKey)

    // Re-create repository against the same database to verify persisted record
    val reloadedRepo = HealthRepository(database.dailyHealthDao())
    val record = reloadedRepo.observeRecordForDate(todayKey).first()

    assertTrue(record.morningTaken)
    assertEquals("오전 8:30", record.morningTakenTime)
    assertEquals(false, record.lunchTaken)
    assertTrue(record.dinnerTaken)
    assertEquals(3, record.waterCups)
  }

  @Test
  fun `ui toggles medication completion and increments water count`() {
    val viewModel = HealthViewModel(repository)

    composeTestRule.setContent {
      SeniorHealthTheme { HealthHomeScreen(viewModel = viewModel) }
    }
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
        .onAllNodes(androidx.compose.ui.test.hasTestTag("medication_button_morning"))
        .fetchSemanticsNodes()
        .isNotEmpty()
    }

    // Verify date and 3 vertical medication buttons are shown
    composeTestRule.onNodeWithTag("today_date_text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("medication_button_morning").assertIsDisplayed()
    composeTestRule.onNodeWithTag("medication_button_lunch").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("medication_button_dinner").performScrollTo().assertIsDisplayed()

    // Tap Morning medication button and verify "복용 완료" appears
    composeTestRule.onNodeWithTag("medication_button_morning").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
        .onAllNodes(androidx.compose.ui.test.hasText("복용 완료", substring = true))
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    composeTestRule.onNodeWithText("복용 완료", substring = true).assertIsDisplayed()

    // Scroll to and tap water intake button twice
    composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
        .onAllNodes(androidx.compose.ui.test.hasText("1잔", substring = true))
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("water_cup_count").performScrollTo().assertTextContains("1잔")

    composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
        .onAllNodes(androidx.compose.ui.test.hasText("2잔", substring = true))
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("water_cup_count").performScrollTo().assertTextContains("2잔")

    // Undo one cup
    composeTestRule.onNodeWithTag("undo_water_button").performScrollTo().performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
        .onAllNodes(androidx.compose.ui.test.hasText("1잔", substring = true))
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("water_cup_count").performScrollTo().assertTextContains("1잔")
  }

  @Test
  fun `midnight rollover resets medication checks and water count and clears yesterday record`() =
    runBlocking {
      val cal =
        java.util.Calendar.getInstance().apply {
          set(2026, java.util.Calendar.NOVEMBER, 16, 23, 59, 50)
          set(java.util.Calendar.MILLISECOND, 0)
        }
      var fakeNowMillis = cal.timeInMillis
      val viewModel =
        HealthViewModel(
          repository = repository,
          currentTimeMillis = { fakeNowMillis },
          enableMidnightTicker = false,
        )

      composeTestRule.setContent { SeniorHealthTheme { HealthHomeScreen(viewModel = viewModel) } }
      composeTestRule.waitUntil(timeoutMillis = 5_000) {
        composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("2026년 11월 16일", substring = true))
          .fetchSemanticsNodes()
          .isNotEmpty()
      }

      // Check morning medication and add 2 cups of water before midnight
      composeTestRule.onNodeWithTag("medication_button_morning").performScrollTo().performClick()
      composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()
      composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()

      composeTestRule.waitUntil(timeoutMillis = 5_000) {
        composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("복용 완료", substring = true))
          .fetchSemanticsNodes()
          .isNotEmpty() &&
          composeTestRule
            .onAllNodes(androidx.compose.ui.test.hasText("2잔", substring = true))
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Advance clock past midnight to 2026-11-17 00:00:02
      fakeNowMillis += 12_000L
      viewModel.refreshDateIfNeeded()

      // Verify date rolled over and both medication and water counts reset automatically
      composeTestRule.waitUntil(timeoutMillis = 5_000) {
        composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("2026년 11월 17일", substring = true))
          .fetchSemanticsNodes()
          .isNotEmpty() &&
          composeTestRule
            .onAllNodes(androidx.compose.ui.test.hasText("0잔", substring = true))
            .fetchSemanticsNodes()
            .isNotEmpty() &&
          composeTestRule
            .onAllNodes(androidx.compose.ui.test.hasText("복용 완료", substring = true))
            .fetchSemanticsNodes()
            .isEmpty()
      }

      composeTestRule.onNodeWithTag("today_date_text").assertTextContains("2026년 11월 17일", substring = true)
      composeTestRule.onNodeWithTag("water_cup_count").performScrollTo().assertTextContains("0잔")

      // Verify yesterday's record was deleted from Room
      val yesterdayRecord = database.dailyHealthDao().getRecordByDate("2026-11-16")
      assertEquals(null, yesterdayRecord)
    }

  @Test
  fun `millisUntilNextMidnight computes delay to just after midnight`() {
    val cal =
      java.util.Calendar.getInstance().apply {
        set(2026, java.util.Calendar.NOVEMBER, 16, 23, 59, 59)
        set(java.util.Calendar.MILLISECOND, 0)
      }
    val delayMs = HealthViewModel.millisUntilNextMidnight(cal.timeInMillis)
    assertEquals(1_050L, delayMs)
  }
}
