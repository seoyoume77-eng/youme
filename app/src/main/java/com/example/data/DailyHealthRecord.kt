package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MedicationSlot {
  MORNING,
  LUNCH,
  DINNER,
}

@Entity(tableName = "daily_health_records")
data class DailyHealthRecord(
  @PrimaryKey val dateKey: String,
  val morningTaken: Boolean = false,
  val morningTakenTime: String? = null,
  val lunchTaken: Boolean = false,
  val lunchTakenTime: String? = null,
  val dinnerTaken: Boolean = false,
  val dinnerTakenTime: String? = null,
  val waterCups: Int = 0,
  val updatedAtMillis: Long = System.currentTimeMillis(),
) {
  val completedMedicationCount: Int
    get() = listOf(morningTaken, lunchTaken, dinnerTaken).count { it }
}
