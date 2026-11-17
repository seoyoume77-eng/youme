package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DailyHealthRecord::class], version = 1, exportSchema = false)
abstract class HealthDatabase : RoomDatabase() {
  abstract fun dailyHealthDao(): DailyHealthDao

  companion object {
    @Volatile private var INSTANCE: HealthDatabase? = null

    fun getInstance(context: Context): HealthDatabase {
      return INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                HealthDatabase::class.java,
                "senior_health_db",
              )
              .build()
              .also { INSTANCE = it }
        }
    }
  }
}
