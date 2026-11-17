package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.WaterBlueFill
import com.example.ui.theme.WaterBlueFillHighlight
import com.example.ui.theme.WaterCupEmptyBg
import com.example.ui.theme.WaterCupOutline

@Composable
fun WaterCupsVisualizer(
  filledCups: Int,
  goalCups: Int = 8,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val singleRow = maxWidth >= 480.dp
    if (singleRow) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        for (index in 1..goalCups) {
          WaterCupIcon(cupNumber = index, isFilled = index <= filledCups)
        }
      }
    } else {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
          for (index in 1..4) {
            WaterCupIcon(cupNumber = index, isFilled = index <= filledCups)
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
          for (index in 5..goalCups) {
            WaterCupIcon(cupNumber = index, isFilled = index <= filledCups)
          }
        }
      }
    }
  }
}

@Composable
private fun WaterCupIcon(cupNumber: Int, isFilled: Boolean, modifier: Modifier = Modifier) {
  val fillProgress by
    animateFloatAsState(
      targetValue = if (isFilled) 1f else 0f,
      animationSpec =
        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
      label = "water_cup_fill_$cupNumber",
    )

  val description =
    if (isFilled) {
      stringResource(R.string.cd_water_cup_filled, cupNumber)
    } else {
      stringResource(R.string.cd_water_cup_empty, cupNumber)
    }

  Canvas(
    modifier =
      modifier
        .width(50.dp)
        .height(58.dp)
        .semantics { contentDescription = description }
  ) {
    val w = size.width
    val h = size.height
    val strokeWidth = 3.dp.toPx()

    val topLeftX = w * 0.12f
    val topRightX = w * 0.88f
    val bottomLeftX = w * 0.22f
    val bottomRightX = w * 0.78f
    val topY = h * 0.12f
    val bottomY = h * 0.90f

    val cupPath =
      Path().apply {
        moveTo(topLeftX, topY)
        lineTo(topRightX, topY)
        lineTo(bottomRightX, bottomY)
        lineTo(bottomLeftX, bottomY)
        close()
      }

    drawPath(path = cupPath, color = WaterCupEmptyBg)

    if (fillProgress > 0.01f) {
      val waterHeight = (bottomY - topY) * 0.78f * fillProgress
      val waterTopY = bottomY - waterHeight
      clipPath(cupPath) {
        drawRect(
          color = WaterBlueFill,
          topLeft = Offset(0f, waterTopY),
          size = androidx.compose.ui.geometry.Size(w, bottomY - waterTopY + 4f),
        )
        drawRect(
          color = WaterBlueFillHighlight,
          topLeft = Offset(0f, waterTopY),
          size = androidx.compose.ui.geometry.Size(w, (waterHeight * 0.18f).coerceAtLeast(4f)),
        )
      }
    }

    drawPath(
      path = cupPath,
      color = if (isFilled) WaterBlueFill else WaterCupOutline,
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    // Top rim highlight for clear cup shape
    drawLine(
      color = if (isFilled) WaterBlueFill else WaterCupOutline,
      start = Offset(w * 0.06f, topY),
      end = Offset(w * 0.94f, topY),
      strokeWidth = strokeWidth,
      cap = StrokeCap.Round,
    )
  }
}
