package com.example.explore

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

data class Ball(var position: Offset, val radius: Float)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val balls = remember {
                mutableStateListOf(
                    mutableStateOf(Ball(Offset(200f, 200f), 60f)),
                    mutableStateOf(Ball(Offset(600f, 200f), 60f)),
                    mutableStateOf(Ball(Offset(1000f, 200f), 60f))
                )
            }
            val distances = remember { mutableStateListOf(200f, 200f) } // Distâncias entre as bolas

            Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Handle drag start
                    },
                    onDragEnd = {
                        // Handle drag end
                    },
                    onDragCancel = {
                        // Handle drag cancel
                    },
                    onDrag = { change, dragAmount ->
                        change.consume() // Consome o evento para evitar interferências

                        // Detecta qual bola está sendo arrastada e atualiza sua posição
                        balls.forEachIndexed { index, ballState ->
                            val ball = ballState.value
                            if ((ball.position - change.position).getDistance() <= ball.radius) {
                                ballState.value = ball.copy(position = ball.position + dragAmount)
                                applyIK(balls.map { it.value }, distances, index)
                            }
                        }
                    }
                )
            }) {
                Log.d("CanvasRecompose", "Recomposing Canvas")
                val path = Path().apply {
                    moveTo(balls[0].value.position.x, balls[0].value.position.y)
                    quadraticBezierTo(
                        balls[1].value.position.x, balls[1].value.position.y,
                        balls[2].value.position.x, balls[2].value.position.y
                    )
                }
                drawPath(path, Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))

                balls.forEach { ballState ->
                    val ball = ballState.value
                    drawCircle(Color.Red, ball.radius, ball.position)
                }
            }
        }
    }

    private fun applyIK(balls: List<Ball>, distances: List<Float>, targetIndex: Int) {
        // Forward reaching
        for (i in targetIndex downTo 1) {
            val direction = (balls[i].position - balls[i - 1].position).normalize()
            balls[i - 1].position = balls[i].position - direction * distances[i - 1]
        }

        // Backward reaching
        for (i in 1 until balls.size) {
            val direction = (balls[i].position - balls[i - 1].position).normalize()
            balls[i].position = balls[i - 1].position + direction * distances[i - 1]
        }
    }

    private fun Offset.getDistance() = sqrt(x * x + y * y)

    private operator fun Offset.minus(other: Offset) = Offset(x - other.x, y - other.y)

    private operator fun Offset.plus(other: Offset) = Offset(x + other.x, y + other.y)

    private operator fun Offset.times(scalar: Float) = Offset(x * scalar, y * scalar)

    private fun Offset.normalize(): Offset {
        val length = getDistance()
        return if (length != 0f) Offset(x / length, y / length) else Offset.Zero
    }
}