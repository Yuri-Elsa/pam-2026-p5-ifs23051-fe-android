package org.delcom.pam_p5_ifs23051.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.delcom.pam_p5_ifs23051.R

@Composable
fun RippleLoading(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF009FFF),
    imageSize: Dp = 76.dp,
    circleSize: Dp = 36.dp,
    maxSize: Dp = 150.dp,
    animationDuration: Int = 1500
) {
    val density = LocalDensity.current
    val circleSizePx = with(density) { circleSize.toPx() }
    val maxSizePx = with(density) { maxSize.toPx() }
//    val midSizePx = circleSizePx * 2.1875f // ~70px if circleSize is 32px

    Box(
        modifier = modifier.size(maxSize + 40.dp),
        contentAlignment = Alignment.Center
    ) {
        // Center logo
        Image(
            painter = painterResource(R.drawable.img_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(imageSize)
        )

        // Create 3 ripple circles with different delays
        repeat(2) { index ->
            val infiniteTransition = rememberInfiniteTransition()

            // Size animation
            val size by infiniteTransition.animateFloat(
                initialValue = circleSizePx,
                targetValue = maxSizePx,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDuration,
                        easing = LinearEasing
                    ),
                    initialStartOffset = StartOffset((animationDuration * 0.6 * index).toInt())
                )
            )

            // Alpha animation - starts after size reaches midSize
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = animationDuration
                        0f at 0
                        0f at ((animationDuration * 0.5).toInt() - 1)
                        1f at ((animationDuration * 0.5).toInt())
                        0f at animationDuration
                    },
                    initialStartOffset = StartOffset((animationDuration * 0.6 * index).toInt())
                )
            )

            // Only show the circle when it's larger than initial size
            if (size > circleSizePx * 1.1f) {
                Box(
                    modifier = Modifier
                        .size(size.toDp())
                        .border(
                            width = 2.dp,
                            color = color.copy(alpha = alpha),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

// Extension function to convert Float px to Dp
@Composable
private fun Float.toDp(): Dp = with(LocalDensity.current) { this@toDp.toDp() }

// Cara menggunakannya:
@Composable
fun LoadingUI() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        RippleLoading()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoadingUI() {
    LoadingUI()
}