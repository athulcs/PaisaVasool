package com.example.paisavasool.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChartSlice(
    val value: Float,
    val color: Color,
    val label: String = ""
)

@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    totalLabel: String,
    totalValue: String,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(slices) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val minSize = minOf(width, height)
            val strokeWidth = minSize * 0.15f
            val radius = (minSize - strokeWidth) / 2f

            var startAngle = -90f

            slices.forEach { slice ->
                val sweepAngle = if (total > 0) (slice.value / total) * 360f else 0f
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * animatedProgress.value,
                    useCenter = false,
                    topLeft = Offset(width / 2f - radius, height / 2f - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweepAngle
            }

            if (total == 0f) {
                drawArc(
                    color = Color.DarkGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(width / 2f - radius, height / 2f - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = totalLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = totalValue,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
