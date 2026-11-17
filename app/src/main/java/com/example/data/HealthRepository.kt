package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HealthRepository(private val dao: DailyHealthDao) {

  fun observeRecordForDate(dateKey: String): Flow<DailyHealthRecord> {
    return dao.observeRecordByDate(dateKey).map { record ->
      record ?: DailyHealthRecord(dateKey = dateKey)
    }
  }

  suspend fun toggleMedication(dateKey: String, slot: MedicationSlot, formattedTime: String) {
    val current = dao.getRecordByDate(dateKey) ?: DailyHealthRecord(dateKey = dateKey)
    val now = System.currentTimeMillis()
    val updated =
      when (slot) {
        MedicationSlot.MORNING -> {
          val nextTaken = !current.morningTaken
          current.copy(
            morningTaken = nextTaken,
            morningTakenTime = if (nextTaken) formattedTime else null,
            updatedAtMillis = now,
          )
        }
        MedicationSlot.LUNCH -> {
          val nextTaken = !current.lunchTaken
          current.copy(
            lunchTaken = nextTaken,
            lunchTakenTime = if (nextTaken) formattedTime else null,
            updatedAtMillis = now,
          )
        }
        MedicationSlot.DINNER -> {
          val nextTaken = !current.dinnerTaken
          current.copy(
            dinnerTaken = nextTaken,
            dinnerTakenTime = if (nextTaken) formattedTime else null,
            updatedAtMillis = now,
          )
        }
      }
    dao.upsertRecord(updated)
  }

  suspend fun incrementWater(dateKey: String) {
    val current = dao.getRecordByDate(dateKey) ?: DailyHealthRecord(dateKey = dateKey)
    val updated =
      current.copy(
        waterCups = (current.waterCups + 1).coerceAtMost(30),
        updatedAtMillis = System.currentTimeMillis(),
      )
    dao.upsertRecord(updated)
  }

  suspend fun decrementWater(dateKey: String) {
    val current = dao.getRecordByDate(dateKey) ?: DailyHealthRecord(dateKey = dateKey)
    val updated =
      current.copy(
        waterCups = (current.waterCups - 1).coerceAtLeast(0),
        updatedAtMillis = System.currentTimeMillis(),
      )
    dao.upsertRecord(updated)
  }

  suspend fun resetPreviousDaysRecords(currentDateKey: String) {
    dao.deleteRecordsNotForDate(currentDateKey)
  }

  companion object {
    fun currentDateKey(date: Date = Date()): String {
      return SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(date)
    }

    fun currentDisplayDate(date: Date = Date()): String {
      return SimpleDateFormat("yyyy년 M월 d일 (E)", Locale.KOREA).format(date)
    }

    fun currentKoreanTime(date: Date = Date()): String {
      return SimpleDateFormat("a h:mm", Locale.KOREA).format(date)
    }
  }
}
