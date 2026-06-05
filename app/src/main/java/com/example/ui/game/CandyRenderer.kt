package com.example.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class CandyType {
    PURPLE_BERRY,
    RED_JELLYBEAN,
    PINK_SWIRL,
    YELLOW_STAR,
    BLUE_GEM,
    CHOCO_BALL,
    ORANGE_STRIPED,
    GREEN_CUBE,
    COLOR_BOMB
}

enum class CandySpecial {
    NONE,
    STRIPED_HORIZONTAL,
    STRIPED_VERTICAL,
    WRAPPED,
    FISH
}

@Composable
fun CandyIcon(
    type: CandyType,
    special: CandySpecial = CandySpecial.NONE,
    modifier: Modifier = Modifier,
    isGlow: Boolean = false
) {
    // We can add subtle subtle continuous animations (like rotating spark lines, pulsing standard cores, etc.)
    val infiniteTransition = rememberInfiniteTransition(label = "candy_loop")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val baseScale = if (special == CandySpecial.WRAPPED) pulseScale * 1.05f else pulseScale
            val candyRadius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Draw wrapped blast shadow/glow if needed
            if (special == CandySpecial.WRAPPED || isGlow) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFEA79).copy(alpha = 0.6f),
                            Color(0xFFFF9800).copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = candyRadius * 1.4f
                    ),
                    radius = candyRadius * 1.4f,
                    center = center
                )
            }

            // Draw core candy based on type
            scale(baseScale, baseScale, center) {
                when (type) {
                    CandyType.PURPLE_BERRY -> drawPurpleBerry(center, candyRadius)
                    CandyType.RED_JELLYBEAN -> drawRedJellybean(center, candyRadius)
                    CandyType.PINK_SWIRL -> drawPinkSwirl(center, candyRadius, spinRotation)
                    CandyType.YELLOW_STAR -> drawYellowStar(center, candyRadius)
                    CandyType.BLUE_GEM -> drawBlueGem(center, candyRadius)
                    CandyType.CHOCO_BALL -> drawChocoBall(center, candyRadius)
                    CandyType.ORANGE_STRIPED -> drawOrangeStriped(center, candyRadius)
                    CandyType.GREEN_CUBE -> drawGreenCube(center, candyRadius)
                    CandyType.COLOR_BOMB -> drawColorBomb(center, candyRadius, spinRotation)
                }

                // Draw special additions
                when (special) {
                    CandySpecial.STRIPED_HORIZONTAL -> drawStripedOverlay(center, candyRadius, isHorizontal = true, spinRotation)
                    CandySpecial.STRIPED_VERTICAL -> drawStripedOverlay(center, candyRadius, isHorizontal = false, spinRotation)
                    CandySpecial.FISH -> drawFishDetails(center, candyRadius)
                    else -> {}
                }

                // Global 3D Glass sphere visual highlights (makes flat 2D look highly glossy 3D!)
                // Top-Left Specular Gleam
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(center.x - candyRadius * 0.35f, center.y - candyRadius * 0.35f),
                        radius = candyRadius * 0.65f
                    ),
                    radius = candyRadius * 0.65f,
                    center = Offset(center.x - candyRadius * 0.35f, center.y - candyRadius * 0.35f)
                )

                // Bottom-Right Volumetric Dark Depth
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                        center = center,
                        radius = candyRadius
                    ),
                    radius = candyRadius,
                    center = center
                )
            }
        }
    }
}


// 1. Purple Berry (Layered grape cluster)
private fun DrawScope.drawPurpleBerry(center: Offset, radius: Float) {
    val r = radius * 0.9f
    // Draw 7 small overlapping circles with rich glossy gradients
    val baseGrad = Brush.radialGradient(
        colors = listOf(Color(0xFFE040FB), Color(0xFF7B1FA2)),
        center = center,
        radius = r
    )

    // Draw little green leaf/stem on top
    val stemPath = Path().apply {
         moveTo(center.x, center.y - r)
         quadraticTo(center.x + r * 0.3f, center.y - r * 1.3f, center.x + r * 0.1f, center.y - r * 1.4f)
         quadraticTo(center.x - r * 0.2f, center.y - r * 1.1f, center.x, center.y - r)
         close()
    }
    drawPath(stemPath, Brush.linearGradient(listOf(Color(0xFF81C784), Color(0xFF2E7D32))))

    // Berry dots coords
    val berryRadius = r * 0.35f
    val offsets = listOf(
        // Top 3
        Offset(center.x - r * 0.35f, center.y - r * 0.3f),
        Offset(center.x, center.y - r * 0.45f),
        Offset(center.x + r * 0.35f, center.y - r * 0.3f),
        // Middle 3
        Offset(center.x - r * 0.32f, center.y + r * 0.1f),
        Offset(center.x, center.y),
        Offset(center.x + r * 0.32f, center.y + r * 0.1f),
        // Bottom 1
        Offset(center.x, center.y + r * 0.45f)
    )

    for (offset in offsets) {
        // Dot drop shadow
        drawCircle(
            color = Color(0x33000000),
            radius = berryRadius,
            center = Offset(offset.x + 2f, offset.y + 4f)
        )
        // Dot gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFEA80FC), Color(0xFF8E24AA), Color(0xFF4A148C)),
                center = Offset(offset.x - berryRadius * 0.15f, offset.y - berryRadius * 0.15f),
                radius = berryRadius * 1.2f
            ),
            radius = berryRadius,
            center = offset
        )
        // Dot gloss
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = berryRadius * 0.25f,
            center = Offset(offset.x - berryRadius * 0.35f, offset.y - berryRadius * 0.35f)
        )
    }
}

// 2. Red Jellybean (Glossy kidney/bean)
private fun DrawScope.drawRedJellybean(center: Offset, radius: Float) {
    val r = radius * 0.95f
    // Jellybean path (slightly angled kidney shape)
    val path = Path().apply {
        moveTo(center.x - r * 0.5f, center.y - r * 0.2f)
        quadraticTo(center.x - r * 0.1f, center.y - r * 0.7f, center.x + r * 0.5f, center.y - r * 0.3f)
        quadraticTo(center.x + r * 0.9f, center.y + r * 0.2f, center.x + r * 0.3f, center.y + r * 0.7f)
        quadraticTo(center.x - r * 0.1f, center.y + r * 0.4f, center.x - r * 0.5f, center.y + r * 0.6f)
        quadraticTo(center.x - r * 0.9f, center.y + r * 0.2f, center.x - r * 0.5f, center.y - r * 0.2f)
        close()
    }

    // Shadow
    drawContext.canvas.save()
    drawContext.transform.translate(3f, 6f)
    drawPath(path, Color(0x33000000))
    drawContext.canvas.restore()

    // Base Gradient
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFF8A80), Color(0xFFE53935), Color(0xFFB71C1C)),
            center = Offset(center.x - r * 0.2f, center.y - r * 0.2f),
            radius = r * 1.2f
        )
    )

    // Candy shine border
    drawPath(
        path = path,
        color = Color(0xFFFFCCBC).copy(alpha = 0.4f),
        style = Stroke(width = 3.dp.toPx())
    )

    // Gloss shine overlay (white crescent)
    val shinePath = Path().apply {
        moveTo(center.x - r * 0.4f, center.y - r * 0.2f)
        quadraticTo(center.x - r * 0.1f, center.y - r * 0.5f, center.x + r * 0.3f, center.y - r * 0.2f)
        quadraticTo(center.x - r * 0.1f, center.y - r * 0.35f, center.x - r * 0.4f, center.y - r * 0.2f)
        close()
    }
    drawPath(shinePath, Color.White.copy(alpha = 0.6f))
}

// 3. Pink/White Swirl (Lollipop swirl)
private fun DrawScope.drawPinkSwirl(center: Offset, radius: Float, rotation: Float) {
    val r = radius * 0.9f

    // Shadow
    drawCircle(
        color = Color(0x22000000),
        radius = r,
        center = Offset(center.x + 3f, center.y + 5f)
    )

    // Outer lollipop ring
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFCE4EC), Color(0xFFF06292), Color(0xFFD81B60)),
            center = center,
            radius = r
        ),
        radius = r,
        center = center
    )

    // Swirling spiral lines
    rotate(rotation, center) {
        val strokeWidth = r * 0.15f
        for (i in 0..3) {
            val angleStep = i * 90f
            val swirlPath = Path()
            for (angle in 0..180 step 10) {
                val rad = Math.toRadians((angle + angleStep).toDouble())
                val currentRadius = r * (angle / 180f)
                val x = center.x + currentRadius * cos(rad).toFloat()
                val y = center.y + currentRadius * sin(rad).toFloat()
                if (angle == 0) {
                    swirlPath.moveTo(x, y)
                } else {
                    swirlPath.lineTo(x, y)
                }
            }
            drawPath(
                path = swirlPath,
                color = Color.White.copy(alpha = 0.8f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }

    // Centered gloss highlight
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.7f), Color.Transparent),
            center = Offset(center.x - r * 0.3f, center.y - r * 0.3f),
            radius = r * 0.5f
        ),
        radius = r * 0.5f,
        center = Offset(center.x - r * 0.3f, center.y - r * 0.3f)
    )
}

// 4. Yellow Star Cookie/Candy
private fun DrawScope.drawYellowStar(center: Offset, radius: Float) {
    val r = radius * 1.0f
    val starPath = Path()
    val points = 5
    var first = true
    val innerRadius = r * 0.45f
    val outerRadius = r * 0.9f

    // Draw five pointed star with slightly rounded tip logic
    for (i in 0 until points * 2) {
        val angle = i * Math.PI / points - Math.PI / 2
        val currR = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + currR * cos(angle).toFloat()
        val y = center.y + currR * sin(angle).toFloat()
        if (first) {
            starPath.moveTo(x, y)
            first = false
        } else {
            starPath.lineTo(x, y)
        }
    }
    starPath.close()

    // Shadow
    drawContext.canvas.save()
    drawContext.transform.translate(3f, 5f)
    drawPath(starPath, Color(0x33000000))
    drawContext.canvas.restore()

    // Sunburst dynamic golden gradient
    drawPath(
        path = starPath,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFF57F17)),
            center = center,
            radius = outerRadius
        )
    )

    // Inner bevel star line
    drawPath(
        path = starPath,
        color = Color.White.copy(alpha = 0.5f),
        style = Stroke(width = 3.dp.toPx())
    )

    // Highlight dot on top left point
    drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = r * 0.15f,
        center = Offset(center.x - r * 0.25f, center.y - r * 0.25f)
    )
}

// 5. Blue Square Gem
private fun DrawScope.drawBlueGem(center: Offset, radius: Float) {
    val r = radius * 0.85f
    val rect = Rect(center.x - r, center.y - r, center.x + r, center.y + r)
    val roundRect = RoundRect(rect, CornerRadius(r * 0.3f))

    // Shadow
    drawContext.canvas.save()
    drawContext.transform.translate(3f, 5f)
    val shadowPath = Path().apply { addRoundRect(roundRect) }
    drawPath(shadowPath, Color(0x33000000))
    drawContext.canvas.restore()

    // Gem base gradient
    val baseGrad = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Color(0xFF00ACC1), Color(0xFF006064)),
        start = Offset(rect.left, rect.top),
        end = Offset(rect.right, rect.bottom)
    )
    val gemPath = Path().apply { addRoundRect(roundRect) }
    drawPath(gemPath, baseGrad)

    // Gem internal faceting diagonal lines
    val insetVal = r * 0.25f
    val innerRect = Rect(rect.left + insetVal, rect.top + insetVal, rect.right - insetVal, rect.bottom - insetVal)
    val innerPath = Path().apply { addRoundRect(RoundRect(innerRect, CornerRadius(r * 0.15f))) }

    drawPath(
        path = innerPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFE0F7FA), Color(0xFF26C6DA).copy(alpha = 0.4f)),
            start = Offset(innerRect.left, innerRect.top),
            end = Offset(innerRect.right, innerRect.bottom)
        )
    )

    // Shiny outline
    drawPath(
        path = gemPath,
        color = Color.White.copy(alpha = 0.5f),
        style = Stroke(width = 3.dp.toPx())
    )

    // Corner diagonal reflections
    drawLine(Color.White.copy(alpha = 0.6f), Offset(rect.left, rect.top), Offset(innerRect.left, innerRect.top), strokeWidth = 2.dp.toPx())
    drawLine(Color.White.copy(alpha = 0.6f), Offset(rect.right, rect.top), Offset(innerRect.right, innerRect.top), strokeWidth = 2.dp.toPx())
    drawLine(Color.White.copy(alpha = 0.6f), Offset(rect.left, rect.bottom), Offset(innerRect.left, innerRect.bottom), strokeWidth = 2.dp.toPx())
    drawLine(Color.White.copy(alpha = 0.6f), Offset(rect.right, rect.bottom), Offset(innerRect.right, innerRect.bottom), strokeWidth = 2.dp.toPx())

    // Glass gloss glow corner
    drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = r * 0.18f,
        center = Offset(rect.left + r * 0.4f, rect.top + r * 0.4f)
    )
}

// 6. Chocolate Ball with Candy Sprinkles
private fun DrawScope.drawChocoBall(center: Offset, radius: Float) {
    val r = radius * 0.9f

    // Shadow
    drawCircle(
        color = Color(0x44000000),
        radius = r,
        center = Offset(center.x + 3f, center.y + 5f)
    )

    // Rich dark chocolate round base-gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF8D6E63), Color(0xFF5D4037), Color(0xFF3E2723)),
            center = center,
            radius = r
        ),
        radius = r,
        center = center
    )

    // Gloss shine rim
    drawCircle(
        color = Color(0xFFFFCC80).copy(alpha = 0.2f),
        radius = r - 1.dp.toPx(),
        style = Stroke(width = 3.dp.toPx()),
        center = center
    )

    // Multiple tiny colorful capsule sprinkles randomly nested on top
    val sprinkleColors = listOf(
        Color(0xFFFF1744), // Red
        Color(0xFF00E676), // Green
        Color(0xFF2979FF), // Blue
        Color(0xFFFFEA00), // Yellow
        Color(0xFFE040FB), // Fuchsia
        Color(0xFF26A69A)  // Cyan
    )

    val sprinkleOffsetsAngle = listOf(
        Pair(Offset(-r * 0.35f, -r * 0.35f), 45f),
        Pair(Offset(r * 0.4f, -r * 0.3f), -35f),
        Pair(Offset(-r * 0.4f, r * 0.25f), 20f),
        Pair(Offset(r * 0.3f, r * 0.4f), 65f),
        Pair(Offset(-r * 0.12f, -r * 0.12f), -15f),
        Pair(Offset(r * 0.15f, r * 0.1f), 110f),
        Pair(Offset(r * 0.05f, -r * 0.45f), 10f),
        Pair(Offset(-r * 0.45f, -r * 0.05f), 85f),
        Pair(Offset(-r * 0.05f, r * 0.45f), -75f)
    )

    for (idx in sprinkleOffsetsAngle.indices) {
        val (offsetRel, angle) = sprinkleOffsetsAngle[idx]
        val col = sprinkleColors[idx % sprinkleColors.size]
        val sprCenter = Offset(center.x + offsetRel.x, center.y + offsetRel.y)

        rotate(angle, sprCenter) {
            val sprW = r * 0.3f
            val sprH = r * 0.1f
            val rect = Rect(sprCenter.x - sprW / 2, sprCenter.y - sprH / 2, sprCenter.x + sprW / 2, sprCenter.y + sprH / 2)
            val sprPath = Path().apply { addRoundRect(RoundRect(rect, CornerRadius(sprH / 2))) }

            // Small drop shadow on sprinkle
            drawContext.canvas.save()
            drawContext.transform.translate(1f, 2f)
            drawPath(sprPath, Color(0x33000000))
            drawContext.canvas.restore()

            // Main fill
            drawPath(sprPath, col)
            // Accent highlight line in sprinkle
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(rect.left + 2f, sprCenter.y),
                end = Offset(rect.right - 4f, sprCenter.y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }

    // Glowing big gloss reflection
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent),
            center = Offset(center.x - r * 0.35f, center.y - r * 0.35f),
            radius = r * 0.4f
        ),
        radius = r * 0.4f,
        center = Offset(center.x - r * 0.35f, center.y - r * 0.35f)
    )
}

// 7. Striped Orange Candy (Oval shape with yellow stripes)
private fun DrawScope.drawOrangeStriped(center: Offset, radius: Float) {
    val rx = radius * 0.95f
    val ry = radius * 0.75f

    // Shadow
    drawContext.canvas.save()
    drawContext.transform.translate(3f, 5f)
    val shadowPath = Path().apply { addOval(Rect(center.x - rx, center.y - ry, center.x + rx, center.y + ry)) }
    drawPath(shadowPath, Color(0x33000000))
    drawContext.canvas.restore()

    // Base Orange Gloss Gradient
    val orangeGrad = Brush.radialGradient(
        colors = listOf(Color(0xFFFFB74D), Color(0xFFF57C00), Color(0xFFE65100)),
        center = Offset(center.x - rx * 0.2f, center.y - ry * 0.2f),
        radius = rx * 1.2f
    )
    val bodyPath = Path().apply { addOval(Rect(center.x - rx, center.y - ry, center.x + rx, center.y + ry)) }
    drawPath(bodyPath, orangeGrad)

    // Drawn yellow/white cream diagonal stripes
    drawContext.canvas.save()
    // Clip stripes to oval candy shape
    drawContext.canvas.clipPath(bodyPath)
    
    val stripeBrush = Brush.linearGradient(listOf(Color(0xFFFFF59D).copy(alpha = 0.8f), Color(0xFFFFF176).copy(alpha = 0.9f)))
    for (i in -4..4) {
        val xShift = i * (rx * 0.4f)
        val path = Path().apply {
            moveTo(center.x + xShift - rx * 0.1f, center.y - ry * 1.2f)
            lineTo(center.x + xShift + rx * 0.2f, center.y - ry * 1.2f)
            lineTo(center.x + xShift - rx * 0.2f, center.y + ry * 1.2f)
            lineTo(center.x + xShift - rx * 0.5f, center.y + ry * 1.2f)
            close()
        }
        drawPath(path, stripeBrush)
    }
    drawContext.canvas.restore()

    // Outer shiny border
    drawPath(
        path = bodyPath,
        color = Color(0xFFFFE082).copy(alpha = 0.5f),
        style = Stroke(width = 3.dp.toPx())
    )

    // Dynamic gloss highlight on oval
    drawCircle(
        color = Color.White.copy(alpha = 0.7f),
        radius = rx * 0.3f,
        center = Offset(center.x - rx * 0.35f, center.y - ry * 0.35f)
    )
}

// 8. Green Jelly Cube (Translucent dice style)
private fun DrawScope.drawGreenCube(center: Offset, radius: Float) {
    val r = radius * 0.8f
    val rect = Rect(center.x - r, center.y - r, center.x + r, center.y + r)
    val roundRect = RoundRect(rect, CornerRadius(r * 0.15f))

    // Shadow
    drawContext.canvas.save()
    drawContext.transform.translate(3f, 5f)
    val shadowPath = Path().apply { addRoundRect(roundRect) }
    drawPath(shadowPath, Color(0x33000000))
    drawContext.canvas.restore()

    // Green Neon jelly gradient base
    val greenGrad = Brush.verticalGradient(
        colors = listOf(Color(0xFFB9F6CA), Color(0xFF00E676), Color(0xFF004D40))
    )
    val bodyPath = Path().apply { addRoundRect(roundRect) }
    drawPath(bodyPath, greenGrad)

    // Inner 3D glass beveled effect
    val inner = RoundRect(
        rect = Rect(rect.left + r * 0.2f, rect.top + r * 0.2f, rect.right - r * 0.2f, rect.bottom - r * 0.2f),
        cornerRadius = CornerRadius(r * 0.1f)
    )
    drawPath(
        path = Path().apply { addRoundRect(inner) },
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFCCFF90).copy(alpha = 0.5f), Color.Transparent),
            center = center,
            radius = r * 0.8f
        )
    )

    // Small green jelly bubbles inside
    val bubbles = listOf(
        Offset(-r * 0.4f, -r * 0.4f),
        Offset(r * 0.3f, -r * 0.3f),
        Offset(-r * 0.3f, r * 0.4f),
        Offset(r * 0.4f, r * 0.4f),
        Offset(0f, r * 0.1f)
    )
    for (bub in bubbles) {
        drawCircle(
            color = Color(0xFFE8F5E9).copy(alpha = 0.6f),
            radius = r * 0.08f,
            center = Offset(center.x + bub.x, center.y + bub.y)
        )
        // Bubble core
        drawCircle(
            color = Color(0xFF2E7D32).copy(alpha = 0.4f),
            radius = r * 0.04f,
            center = Offset(center.x + bub.x - 1f, center.y + bub.y - 1f)
        )
    }

    // Outer glow highlight border
    drawPath(
        path = bodyPath,
        color = Color.White.copy(alpha = 0.4f),
        style = Stroke(width = 3.dp.toPx())
    )

    // Sleek glass gleam
    drawRoundRect(
        color = Color.White.copy(alpha = 0.7f),
        topLeft = Offset(rect.left + r * 0.1f, rect.top + r * 0.1f),
        size = Size(r * 0.6f, r * 0.15f),
        cornerRadius = CornerRadius(r * 0.08f)
    )
}

// 9. Color Bomb (swirling rainbow magic core)
private fun DrawScope.drawColorBomb(center: Offset, radius: Float, rotation: Float) {
    val r = radius * 0.95f

    // Thick black shadow
    drawCircle(
        color = Color(0x66000000),
        radius = r,
        center = Offset(center.x + 3f, center.y + 5f)
    )

    // Cosmic black obsidian core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF37474F), Color(0xFF102027)),
            center = center,
            radius = r
        ),
        radius = r,
        center = center
    )

    // Swirling rainbow galaxy rings around the core
    rotate(rotation, center) {
        val rainbowColors = listOf(
            Color(0xFFFF1744),
            Color(0xFFFF9100),
            Color(0xFFFFEA00),
            Color(0xFF00E676),
            Color(0xFF00E5FF),
            Color(0xFFD500F9)
        )
        
        for (idx in rainbowColors.indices) {
            val c = rainbowColors[idx]
            val ringRadius = r * (0.4f + idx * 0.09f)
            drawCircle(
                color = c,
                radius = ringRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 25f), rotation))
            )
        }
    }

    // Rotating small glossy stars / circles representing sparkly sprinkle energy
    rotate(-rotation * 1.5f, center) {
        val sparkles = listOf(
            Offset(-r * 0.5f, -r * 0.5f) to Color(0xFFFF9100),
            Offset(r * 0.6f, -r * 0.2f) to Color(0xFF00E5FF),
            Offset(-r * 0.4f, r * 0.5f) to Color(0xFFD500F9),
            Offset(r * 0.4f, r * 0.4f) to Color(0xFFFFEA00)
        )
        for ((pos, col) in sparkles) {
            drawCircle(
                color = col,
                radius = r * 0.15f,
                center = Offset(center.x + pos.x, center.y + pos.y)
            )
            // inner sparkling highlight core
            drawCircle(
                color = Color.White,
                radius = r * 0.06f,
                center = Offset(center.x + pos.x - 2f, center.y + pos.y - 2f)
            )
        }
    }

    // Glowing white cosmic overlay reflection
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.8f), Color.Transparent),
            center = Offset(center.x - r * 0.3f, center.y - r * 0.3f),
            radius = r * 0.45f
        ),
        radius = r * 0.45f,
        center = Offset(center.x - r * 0.3f, center.y - r * 0.3f)
    )
}

// 10. Draw horizontal/vertical stripes (lasers!) on Striped Candies
private fun DrawScope.drawStripedOverlay(center: Offset, radius: Float, isHorizontal: Boolean, rotation: Float) {
    val r = radius * 0.95f
    val strokeW = r * 0.18f

    rotate(if (isHorizontal) 0f else 90f, center) {
        // High-energy white laser beam across candy
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White, Color.White, Color.Transparent)
            ),
            start = Offset(center.x - r * 1.3f, center.y),
            end = Offset(center.x + r * 1.3f, center.y),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        
        // Secondary energy pulse stripes
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(center.x - r * 0.8f, center.y - strokeW * 0.8f),
            end = Offset(center.x - r * 0.4f, center.y - strokeW * 0.8f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(center.x + r * 0.4f, center.y + strokeW * 0.8f),
            end = Offset(center.x + r * 0.8f, center.y + strokeW * 0.8f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

// 11. Custom Fish Addition (Cute matching details)
private fun DrawScope.drawFishDetails(center: Offset, radius: Float) {
    val r = radius * 0.85f

    // Cute fish tail on the right side
    val tailPath = Path().apply {
        moveTo(center.x + r * 0.2f, center.y)
        lineTo(center.x + r * 0.8f, center.y - r * 0.4f)
        lineTo(center.x + r * 0.6f, center.y)
        lineTo(center.x + r * 0.8f, center.y + r * 0.4f)
        close()
    }

    // Tail shadow
    drawContext.canvas.save()
    drawContext.transform.translate(2f, 3f)
    drawPath(tailPath, Color(0x33000000))
    drawContext.canvas.restore()

    drawPath(
        path = tailPath,
        brush = Brush.verticalGradient(listOf(Color(0xFF29B6F6), Color(0xFF0288D1)))
    )

    // cute side fins
    val finPath = Path().apply {
        moveTo(center.x - r * 0.1f, center.y + r * 0.2f)
        quadraticTo(center.x + r * 0.1f, center.y + r * 0.6f, center.x + r * 0.2f, center.y + r * 0.7f)
        quadraticTo(center.x, center.y + r * 0.4f, center.x - r * 0.1f, center.y + r * 0.2f)
        close()
    }
    drawPath(finPath, Color(0xFF81D4FA).copy(alpha = 0.9f))

    // Small cute fish eye
    drawCircle(
        color = Color.White,
        radius = r * 0.16f,
        center = Offset(center.x - r * 0.4f, center.y - r * 0.1f)
    )
    drawCircle(
        color = Color(0xFF01579B),
        radius = r * 0.08f,
        center = Offset(center.x - r * 0.44f, center.y - r * 0.12f)
    )
}

// Juicy physics-simulated particle explosion for a crushed candy, matching Candy Crush style
@Composable
fun CandyExplosionEffect(
    type: CandyType,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
    }

    val p = progress.value
    val color = getCandyColor(type)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f

        // 1. Expanding shockwave concentric rings
        drawCircle(
            color = color.copy(alpha = (1f - p) * 0.8f),
            radius = maxRadius * (0.1f + p * 1.6f),
            center = center,
            style = Stroke(width = (4.dp.toPx() * (1f - p)))
        )
        
        drawCircle(
            color = Color.White.copy(alpha = (1f - p) * 0.4f),
            radius = maxRadius * (0.05f + p * 1.2f),
            center = center,
            style = Stroke(width = (2.dp.toPx() * (1f - p)))
        )

        // 2. Flying candy Shards or Juice Drops (8 particles)
        val numParticles = 8
        for (i in 0 until numParticles) {
            val angleDeg = i * (360f / numParticles) + (p * 50f)
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val distance = maxRadius * (p * 1.9f)
            val px = center.x + (cos(angleRad) * distance).toFloat()
            val py = center.y + (sin(angleRad) * distance).toFloat()
            
            val pRadius = maxRadius * 0.25f * (1f - p)
            
            drawCircle(
                color = color,
                radius = pRadius,
                center = Offset(px, py)
            )
            
            // Bright white sheen on some particles
            if (i % 2 == 0) {
                drawCircle(
                    color = Color.White.copy(alpha = (1f - p)),
                    radius = pRadius * 0.4f,
                    center = Offset(px - pRadius * 0.2f, py - pRadius * 0.2f)
                )
            }
        }

        // 3. Center flash and remaining imploding core
        if (p < 0.6f) {
            val coreScale = (1f - p / 0.6f)
            drawCircle(
                color = Color.White.copy(alpha = (1f - p) * 0.9f),
                radius = maxRadius * 0.6f * coreScale,
                center = center
            )
        }
    }
}

fun getCandyColor(type: CandyType): Color {
    return when (type) {
        CandyType.PURPLE_BERRY -> Color(0xFFD81B60) // High quality candy color codes
        CandyType.RED_JELLYBEAN -> Color(0xFFE53935)
        CandyType.PINK_SWIRL -> Color(0xFFEC407A)
        CandyType.YELLOW_STAR -> Color(0xFFFFEB3B)
        CandyType.BLUE_GEM -> Color(0xFF1E88E5)
        CandyType.CHOCO_BALL -> Color(0xFF5D4037)
        CandyType.ORANGE_STRIPED -> Color(0xFFFB8C00)
        CandyType.GREEN_CUBE -> Color(0xFF43A047)
        CandyType.COLOR_BOMB -> Color(0xFFFFD54F)
    }
}
