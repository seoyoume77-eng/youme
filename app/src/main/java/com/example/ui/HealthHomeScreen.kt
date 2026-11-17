package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.DailyHealthRecord
import com.example.data.MedicationSlot
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvoryBorder
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.IvorySurfaceVariant
import com.example.ui.theme.MedicationDoneBadgeBg
import com.example.ui.theme.MedicationDoneGreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WaterBlueBorder
import com.example.ui.theme.WaterBlueLightSurface
import com.example.ui.theme.WaterBluePrimary

@Composable
fun HealthHomeScreen(viewModel: HealthViewModel, modifier: Modifier = Modifier) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = IvoryBackground,
    contentWindowInsets = WindowInsets.safeDrawing,
  ) { innerPadding ->
    Box(
      modifier =
        Modifier.fillMaxSize()
          .padding(innerPadding)
          .verticalScroll(rememberScrollState()),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier.widthIn(max = 600.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        when (val state = uiState) {
          is HealthUiState.Loading -> {
            TodayDateHeaderCard(displayDate = state.displayDate, completedMedCount = 0)
            Box(
              modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
              contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator(
                color = MedicationDoneGreen,
                modifier = Modifier.size(48.dp),
              )
            }
          }
          is HealthUiState.Success -> {
            TodayDateHeaderCard(
              displayDate = state.displayDate,
              completedMedCount = state.record.completedMedicationCount,
            )
            MedicationChecklistSection(
              record = state.record,
              onToggleSlot = { slot -> viewModel.toggleMedication(slot) },
            )
            WaterIntakeSection(
              waterCups = state.record.waterCups,
              onAddCup = { viewModel.addWaterCup() },
              onRemoveCup = { viewModel.removeWaterCup() },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TodayDateHeaderCard(displayDate: String, completedMedCount: Int) {
  val allDone = completedMedCount == 3
  val summaryText =
    if (allDone) {
      stringResource(R.string.medication_all_done)
    } else {
      stringResource(R.string.medication_summary_format, 3, completedMedCount)
    }

  Surface(
    modifier = Modifier.fillMaxWidth().testTag("today_date_card"),
    shape = RoundedCornerShape(24.dp),
    color = IvorySurfaceVariant,
    border = BorderStroke(width = 1.5.dp, color = IvoryBorder),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.CalendarToday,
          contentDescription = null,
          tint = InkSecondary,
          modifier = Modifier.size(26.dp),
        )
        Text(
          text = stringResource(R.string.today_label),
          style = MaterialTheme.typography.titleMedium,
          color = InkSecondary,
          fontWeight = FontWeight.SemiBold,
        )
      }

      Text(
        text = displayDate,
        style = MaterialTheme.typography.headlineLarge,
        color = InkPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.testTag("today_date_text"),
      )

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (allDone) MedicationDoneBadgeBg else IvorySurface,
        border =
          BorderStroke(
            width = 1.5.dp,
            color = if (allDone) MedicationDoneGreen else IvoryBorder,
          ),
      ) {
        Text(
          text = summaryText,
          style = MaterialTheme.typography.titleMedium,
          color = if (allDone) MedicationDoneGreen else InkPrimary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
      }
    }
  }
}

@Composable
private fun MedicationChecklistSection(
  record: DailyHealthRecord,
  onToggleSlot: (MedicationSlot) -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.padding(horizontal = 4.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.Medication,
        contentDescription = null,
        tint = MedicationDoneGreen,
        modifier = Modifier.size(30.dp),
      )
      Text(
        text = stringResource(R.string.medication_section_title),
        style = MaterialTheme.typography.titleLarge,
        color = InkPrimary,
        fontWeight = FontWeight.Bold,
      )
    }

    MedicationCardButton(
      slot = MedicationSlot.MORNING,
      isTaken = record.morningTaken,
      takenTime = record.morningTakenTime,
      onClick = { onToggleSlot(MedicationSlot.MORNING) },
    )

    MedicationCardButton(
      slot = MedicationSlot.LUNCH,
      isTaken = record.lunchTaken,
      takenTime = record.lunchTakenTime,
      onClick = { onToggleSlot(MedicationSlot.LUNCH) },
    )

    MedicationCardButton(
      slot = MedicationSlot.DINNER,
      isTaken = record.dinnerTaken,
      takenTime = record.dinnerTakenTime,
      onClick = { onToggleSlot(MedicationSlot.DINNER) },
    )
  }
}

@Composable
private fun WaterIntakeSection(
  waterCups: Int,
  onAddCup: () -> Unit,
  onRemoveCup: () -> Unit,
) {
  val haptics = LocalHapticFeedback.current
  val goalCups = 8
  val goalReached = waterCups >= goalCups

  Surface(
    modifier = Modifier.fillMaxWidth().testTag("water_section_card"),
    shape = RoundedCornerShape(26.dp),
    color = IvorySurface,
    border = BorderStroke(width = 2.dp, color = WaterBlueBorder),
    shadowElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(WaterBlueLightSurface),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.WaterDrop,
              contentDescription = stringResource(R.string.cd_water_drop),
              tint = WaterBluePrimary,
              modifier = Modifier.size(28.dp),
            )
          }
          Text(
            text = stringResource(R.string.water_section_title),
            style = MaterialTheme.typography.titleLarge,
            color = InkPrimary,
            fontWeight = FontWeight.Bold,
          )
        }

        Text(
          text = stringResource(R.string.water_cup_unit, waterCups),
          style = MaterialTheme.typography.displayLarge,
          color = WaterBluePrimary,
          fontWeight = FontWeight.ExtraBold,
          modifier = Modifier.testTag("water_cup_count"),
        )
      }

      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = WaterBlueLightSurface,
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          WaterCupsVisualizer(filledCups = waterCups, goalCups = goalCups)

          Text(
            text =
              if (goalReached) {
                stringResource(R.string.water_goal_reached)
              } else {
                stringResource(R.string.water_goal_progress, waterCups)
              },
            style = MaterialTheme.typography.titleMedium,
            color = if (goalReached) MedicationDoneGreen else InkPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
          )
        }
      }

      Button(
        onClick = {
          haptics.performHapticFeedback(HapticFeedbackType.LongPress)
          onAddCup()
        },
        modifier =
          Modifier.fillMaxWidth()
            .defaultMinSize(minHeight = 84.dp)
            .minimumInteractiveComponentSize()
            .testTag("add_water_button"),
        shape = RoundedCornerShape(24.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = WaterBluePrimary,
            contentColor = PureWhite,
          ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
      ) {
        Row(
          modifier = Modifier.padding(vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Rounded.LocalDrink,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = stringResource(R.string.water_add_button),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
          )
        }
      }

      if (waterCups > 0) {
        OutlinedButton(
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onRemoveCup()
          },
          modifier =
            Modifier.fillMaxWidth()
              .defaultMinSize(minHeight = 58.dp)
              .minimumInteractiveComponentSize()
              .testTag("undo_water_button"),
          shape = RoundedCornerShape(18.dp),
          border = BorderStroke(width = 1.5.dp, color = IvoryBorder),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = InkSecondary),
        ) {
          Icon(
            imageVector = Icons.Rounded.RemoveCircleOutline,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = stringResource(R.string.water_undo_button),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}
