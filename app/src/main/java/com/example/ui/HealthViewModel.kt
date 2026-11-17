package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DailyHealthRecord
import com.example.data.HealthRepository
import com.example.data.MedicationSlot
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DateInfo(val dateKey: String, val displayDate: String)

sealed interface HealthUiState {
  data class Loading(val displayDate: String) : HealthUiState

  data class Success(val displayDate: String, val record: DailyHealthRecord) : HealthUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class HealthViewModel(
  private val repository: HealthRepository,
  private val currentTimeMillis: () -> Long = { System.currentTimeMillis() },
  enableMidnightTicker: Boolean = true,
) : ViewModel() {

  private val dateInfoFlow = MutableStateFlow(buildDateInfo(currentTimeMillis()))

  val uiState: StateFlow<HealthUiState> =
    dateInfoFlow
      .flatMapLatest { dateInfo ->
        repository.observeRecordForDate(dateInfo.dateKey).combine(dateInfoFlow) { record, latestDate
          ->
          HealthUiState.Success(displayDate = latestDate.displayDate, record = record)
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = HealthUiState.Loading(displayDate = dateInfoFlow.value.displayDate),
      )

  init {
    viewModelScope.launch { repository.resetPreviousDaysRecords(dateInfoFlow.value.dateKey) }
    if (enableMidnightTicker) {
      startMidnightTicker()
    }
  }

  private fun startMidnightTicker() {
    viewModelScope.launch {
      while (isActive) {
        val delayMs = millisUntilNextMidnight(currentTimeMillis())
        delay(delayMs)
        refreshDateIfNeeded()
      }
    }
  }

  fun refreshDateIfNeeded() {
    val latestInfo = buildDateInfo(currentTimeMillis())
    if (latestInfo.dateKey != dateInfoFlow.value.dateKey) {
      dateInfoFlow.value = latestInfo
      viewModelScope.launch { repository.resetPreviousDaysRecords(latestInfo.dateKey) }
    }
  }

  fun toggleMedication(slot: MedicationSlot) {
    refreshDateIfNeeded()
    val dateKey = dateInfoFlow.value.dateKey
    val formattedTime = HealthRepository.currentKoreanTime(Date(currentTimeMillis()))
    viewModelScope.launch { repository.toggleMedication(dateKey, slot, formattedTime) }
  }

  fun addWaterCup() {
    refreshDateIfNeeded()
    val dateKey = dateInfoFlow.value.dateKey
    viewModelScope.launch { repository.incrementWater(dateKey) }
  }

  fun removeWaterCup() {
    refreshDateIfNeeded()
    val dateKey = dateInfoFlow.value.dateKey
    viewModelScope.launch { repository.decrementWater(dateKey) }
  }

  private fun buildDateInfo(timeMillis: Long): DateInfo {
    val now = Date(timeMillis)
    return DateInfo(
      dateKey = HealthRepository.currentDateKey(now),
      displayDate = HealthRepository.currentDisplayDate(now),
    )
  }

  companion object {
    internal fun millisUntilNextMidnight(nowMillis: Long): Long {
      val calendar =
        Calendar.getInstance().apply {
          timeInMillis = nowMillis
          add(Calendar.DAY_OF_YEAR, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 50)
        }
      return (calendar.timeInMillis - nowMillis).coerceAtLeast(1_000L)
    }
  }

  class Factory(private val repository: HealthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(HealthViewModel::class.java)) {
        return HealthViewModel(repository) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
