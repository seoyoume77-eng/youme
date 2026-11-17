package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.MedicationSlot
import com.example.ui.theme.DinnerAccent
import com.example.ui.theme.DinnerBadgeBg
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.IvoryBorder
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.LunchAccent
import com.example.ui.theme.LunchBadgeBg
import com.example.ui.theme.MedicationDoneGreen
import com.example.ui.theme.MedicationDoneGreenBorder
import com.example.ui.theme.MorningAccent
import com.example.ui.theme.MorningBadgeBg
import com.example.ui.theme.PureWhite

@Composable
fun MedicationCardButton(
  slot: MedicationSlot,
  isTaken: Boolean,
  takenTime: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptics = LocalHapticFeedback.current

  val label =
    when (slot) {
      MedicationSlot.MORNING -> stringResource(R.string.medication_morning)
      MedicationSlot.LUNCH -> stringResource(R.string.medication_lunch)
      MedicationSlot.DINNER -> stringResource(R.string.medication_dinner)
    }

  val slotIcon: ImageVector =
    when (slot) {
      MedicationSlot.MORNING -> Icons.Rounded.WbTwilight
      MedicationSlot.LUNCH -> Icons.Rounded.WbSunny
      MedicationSlot.DINNER -> Icons.Rounded.NightsStay
    }

  val pendingBadgeBg: Color =
    when (slot) {
      MedicationSlot.MORNING -> MorningBadgeBg
      MedicationSlot.LUNCH -> LunchBadgeBg
      MedicationSlot.DINNER -> DinnerBadgeBg
    }

  val pendingIconTint: Color =
    when (slot) {
      MedicationSlot.MORNING -> MorningAccent
      MedicationSlot.LUNCH -> LunchAccent
      MedicationSlot.DINNER -> DinnerAccent
    }

  val buttonTag =
    when (slot) {
      MedicationSlot.MORNING -> "medication_button_morning"
      MedicationSlot.LUNCH -> "medication_button_lunch"
      MedicationSlot.DINNER -> "medication_button_dinner"
    }

  val containerColor by
    animateColorAsState(
      targetValue = if (isTaken) MedicationDoneGreen else IvorySurface,
      animationSpec = tween(durationMillis = 240),
      label = "med_container_color",
    )

  val borderColor by
    animateColorAsState(
      targetValue = if (isTaken) MedicationDoneGreenBorder else IvoryBorder,
      animationSpec = tween(durationMillis = 240),
      label = "med_border_color",
    )

  val titleColor by
    animateColorAsState(
      targetValue = if (isTaken) PureWhite else InkPrimary,
      animationSpec = tween(durationMillis = 200),
      label = "med_title_color",
    )

  val subtitleColor by
    animateColorAsState(
      targetValue = if (isTaken) PureWhite.copy(alpha = 0.94f) else InkSecondary,
      animationSpec = tween(durationMillis = 200),
      label = "med_subtitle_color",
    )

  val checkScale by
    animateFloatAsState(
      targetValue = if (isTaken) 1f else 0.92f,
      animationSpec =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
      label = "med_check_scale",
    )

  val takenLabel = stringResource(R.string.medication_taken)
  val pendingLabel = stringResource(R.string.medication_pending)
  val statusText =
    if (isTaken) {
      if (!takenTime.isNullOrBlank()) "$takenLabel ($takenTime)" else takenLabel
    } else {
      pendingLabel
    }

  Surface(
    onClick = {
      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
      onClick()
    },
    modifier =
      modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 104.dp)
        .minimumInteractiveComponentSize()
        .testTag(buttonTag)
        .semantics {
          stateDescription = if (isTaken) takenLabel else pendingLabel
        },
    shape = RoundedCornerShape(24.dp),
    color = containerColor,
    border = BorderStroke(width = 2.dp, color = borderColor),
    shadowElevation = if (isTaken) 4.dp else 2.dp,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier.size(60.dp)
              .clip(CircleShape)
              .background(if (isTaken) PureWhite.copy(alpha = 0.20f) else pendingBadgeBg),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = slotIcon,
            contentDescription = null,
            tint = if (isTaken) PureWhite else pendingIconTint,
            modifier = Modifier.size(34.dp),
          )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium,
            color = titleColor,
            fontWeight = FontWeight.Bold,
          )
          AnimatedContent(
            targetState = statusText,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
            label = "med_status_text",
          ) { currentStatus ->
            Text(
              text = currentStatus,
              style = MaterialTheme.typography.titleMedium,
              color = subtitleColor,
              fontWeight = if (isTaken) FontWeight.Bold else FontWeight.Medium,
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Box(
        modifier =
          Modifier.size(48.dp)
            .scale(checkScale)
            .clip(CircleShape)
            .background(if (isTaken) PureWhite else Color.Transparent)
            .border(
              width = if (isTaken) 0.dp else 2.5.dp,
              color = if (isTaken) Color.Transparent else IvoryBorder,
              shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
      ) {
        if (isTaken) {
          Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = stringResource(R.string.cd_checked_icon),
            tint = MedicationDoneGreen,
            modifier = Modifier.size(32.dp),
          )
        }
      }
    }
  }
}
