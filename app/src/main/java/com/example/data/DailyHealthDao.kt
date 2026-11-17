package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHealthDao {
  @Query("SELECT * FROM daily_health_records WHERE dateKey = :dateKey LIMIT 1")
  fun observeRecordByDate(dateKey: String): Flow<DailyHealthRecord?>

  @Query("SELECT * FROM daily_health_records WHERE dateKey = :dateKey LIMIT 1")
  suspend fun getRecordByDate(dateKey: String): DailyHealthRecord?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertRecord(record: DailyHealthRecord)

  @Query("DELETE FROM daily_health_records WHERE dateKey != :currentDateKey")
  suspend fun deleteRecordsNotForDate(currentDateKey: String)
}
