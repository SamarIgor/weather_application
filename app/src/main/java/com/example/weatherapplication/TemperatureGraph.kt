package com.example.weatherapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapplication.data.Forecast
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TemperatureGraph(hourlyForecasts: List<Forecast.HourlyForecast>) {
    val temperatures = hourlyForecasts.map { it.main.temp }
    val maxTemperature = temperatures.maxOrNull() ?: 0.0
    val minTemperature = temperatures.minOrNull() ?: 0.0
    val temperatureRange = maxTemperature - minTemperature

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)

    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.Center)
        ) {
            val width = size.width
            val height = size.height
            val graphWidth = width - 50.dp.toPx()
            val graphHeight = height - 50.dp.toPx() // Adjusted graph height
            val xStep = graphWidth / (temperatures.size - 1)
            val yStep = graphHeight / temperatureRange

            // Draw the temperature values as circles on the graph line
            for (i in 0 until temperatures.size) {
                val x = i * xStep
                val y = graphHeight - (temperatures[i] - minTemperature) * yStep

                val lineColor = when {
                    temperatures[i] < 10 -> Color.Blue
                    temperatures[i] >= 10 && temperatures[i] < 20 -> Color.Green
                    temperatures[i] >= 20 && temperatures[i] < 30 -> Color.Yellow
                    temperatures[i] >= 30 -> Color.Red
                    else -> Color.Black
                }

                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y.toFloat())
                )
            }

            // Draw the temperature values as text above the circles
            for (i in 0 until temperatures.size) {
                val x = i * xStep
                val y = graphHeight - (temperatures[i] - minTemperature) * yStep - 20.dp.toPx() // Adjusted y value

                val text = "${kotlin.math.floor(temperatures[i]).toInt()}°C"
                drawIntoCanvas { canvas ->
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 14.sp.toPx()
                    }
                    canvas.nativeCanvas.drawText(text, x - textPaint.measureText(text) / 2,
                        y.toFloat(), textPaint)
                }
            }

            // Draw the line that connects the dots for each temperature at each time hour
            val path = Path().apply {
                moveTo(0f, (graphHeight - (temperatures[0] - minTemperature) * yStep).toFloat())
                for (i in 1 until temperatures.size) {
                    val lineColor = when {
                        temperatures[i] < 10 -> Color.Blue
                        temperatures[i] >= 10 && temperatures[i] < 20 -> Color.Green
                        temperatures[i] >= 20 && temperatures[i] < 30 -> Color.Yellow
                        temperatures[i] >= 30 -> Color.Red
                        else -> Color.Black
                    }
                    drawPath(
                        path = Path().apply {
                            moveTo((i - 1) * xStep,
                                (graphHeight - (temperatures[i - 1] - minTemperature) * yStep).toFloat()
                            )
                            lineTo(i * xStep,
                                (graphHeight - (temperatures[i] - minTemperature) * yStep).toFloat()
                            )
                        },
                        color = lineColor,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Draw the horizontal line at the bottom of the graph
            drawLine(
                color = Color.White,
                start = Offset(0f, graphHeight + 20.dp.toPx()), // Adjusted y value
                end = Offset(graphWidth, graphHeight + 20.dp.toPx()), // Adjusted y value
                strokeWidth = 2.dp.toPx()
            )

            // Draw the time values as text below the horizontal line
            for (i in 0 until temperatures.size) {
                val x = i * xStep
                val y = graphHeight + 36.dp.toPx() // Adjusted y value

                val time = hourlyForecasts[i].dt_txt
                val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(time))
                drawIntoCanvas { canvas ->
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 12.sp.toPx()
                    }
                    canvas.nativeCanvas.drawText(formattedTime, x - textPaint.measureText(formattedTime) / 2, y, textPaint)
                }
            }
        }
    }
}
