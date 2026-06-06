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
import androidx.compose.ui.graphics.drawscope.clipPath
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
    SPINNER,
    TNT
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
            val baseScale = if (special == CandySpecial.TNT) pulseScale * 1.05f else pulseScale
            val candyRadius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Draw TNT blast shadow/glow if needed
            if (special == CandySpecial.TNT || isGlow) {
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

            // Draw core candy based on type or standalone special
            scale(baseScale, baseScale, center) {
                if (special == CandySpecial.TNT) {
                    drawTNT(center, candyRadius)
                } else if (special == CandySpecial.SPINNER) {
                    drawSpinner(center, candyRadius, spinRotation)
                } else {
                    when (type) {
                        CandyType.PURPLE_BERRY -> drawPurpleBerry(center, candyRadius)
                        CandyType.RED_JELLYBEAN -> drawRedJellybean(center, candyRadius)
                        CandyType.PINK_SWIRL -> drawPinkSwirl(center, candyRadius, spinRotation)
                        CandyType.YELLOW_STAR -> drawYellowStar(center, candyRadius)
                        CandyType.BLUE_GEM -> drawBlueGem(center, candyRadius)
                        CandyType.CHOCO_BALL -> drawChocoBall(center, candyRadius)
                        CandyType.ORANGE_STRIPED -> drawOrangeStriped(center, candyRadius)
                        CandyType.GREEN_CUBE -> drawGreenCube(center, candyRadius)
                        CandyType.COLOR_BOMB -> drawDiscoBall(center, candyRadius, spinRotation)
                    }

                    if (type != CandyType.COLOR_BOMB) {
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

// 9. Disco Ball (retro mirror tiles with swirling rainbow magic and bright electric lines)
private fun DrawScope.drawDiscoBall(center: Offset, radius: Float, rotation: Float) {
    val r = radius * 0.95f
    
    // Ambient back shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.4f),
        radius = r,
        center = Offset(center.x + 3f, center.y + 5f)
    )
    
    // Metallic base gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFECEFF1), Color(0xFF455A64)),
            center = center,
            radius = r
        ),
        radius = r
    )
    
    // Draw grid of disco tiles
    val gridCount = 6
    val sizeW = (r * 2f) / gridCount
    
    clipPath(Path().apply { addOval(Rect(center.x - r, center.y - r, center.x + r, center.y + r)) }) {
        rotate(rotation, center) {
            for (i in 0..gridCount) {
                for (j in 0..gridCount) {
                    val tileX = center.x - r + i * sizeW
                    val tileY = center.y - r + j * sizeW
                    
                    // Rainbow reflective color sequence based on tile position & rotation
                    val tileColor = when ((i + j + (rotation / 30).toInt()) % 6) {
                        0 -> Color(0xFFFF1744) // Red
                        1 -> Color(0xFFFF9100) // Orange
                        2 -> Color(0xFFFFEA00) // Yellow
                        3 -> Color(0xFF00E676) // Green
                        4 -> Color(0xFF00E5FF) // Blue
                        else -> Color(0xFFD500F9) // Purple
                    }
                    
                    drawRect(
                        color = tileColor,
                        topLeft = Offset(tileX + 1.5f, tileY + 1.5f),
                        size = Size(sizeW - 3f, sizeW - 3f)
                    )
                    
                    // Shine core on each tile
                    drawRect(
                        color = Color.White.copy(alpha = 0.6f),
                        topLeft = Offset(tileX + 3f, tileY + 3f),
                        size = Size((sizeW - 3f)/3f, (sizeW - 3f)/3f)
                    )
                }
            }
        }
    }
    
    // Beautiful metal outline
    drawCircle(
        color = Color(0xFFCFD8DC),
        radius = r,
        style = Stroke(1.5f.dp.toPx())
    )
    
    // Sparkles and rays
    val numRays = 8
    rotate(-rotation * 0.5f, center) {
        for (i in 0 until numRays) {
            val angle = i * (360f / numRays)
            val rad = Math.toRadians(angle.toDouble())
            val startRadius = r * 0.85f
            val endRadius = r * (1.1f + 0.15f * sin(rotation * 0.1f + i).toFloat())
            val startX = center.x + (cos(rad) * startRadius).toFloat()
            val startY = center.y + (sin(rad) * startRadius).toFloat()
            val endX = center.x + (cos(rad) * endRadius).toFloat()
            val endY = center.y + (sin(rad) * endRadius).toFloat()
            
            // Draw colorful neon beam style
            val neonColor = when (i % 4) {
                0 -> Color(0xFFFF1744)
                1 -> Color(0xFF00E5FF)
                2 -> Color(0xFFFFEA00)
                else -> Color(0xFFD500F9)
            }
            drawLine(
                color = neonColor.copy(alpha = 0.7f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.5f.dp.toPx()
            )
            drawLine(
                color = Color.White,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 0.8f.dp.toPx()
            )
        }
    }
}

// 10. Spinner (aerodynamic rotating propeller blade wings)
private fun DrawScope.drawSpinner(center: Offset, radius: Float, rotation: Float, unfoldProgress: Float = 1.0f) {
    val r = radius * 0.85f
    
    // Outer circular motion blur path
    drawCircle(
        color = Color(0x2200E5FF),
        radius = r * 1.1f,
        center = center,
        style = Stroke(2.5f.dp.toPx())
    )
    
    rotate(rotation, center) {
        // Draw 3 propeller wings (turbines)
        val numBlades = 3
        for (i in 0 until numBlades) {
            val angle = i * (360f / numBlades)
            val rad = Math.toRadians(angle.toDouble())
            
            val bladeLength = r * unfoldProgress
            val bladeWidth = r * 0.35f
            
            val bladePath = Path().apply {
                moveTo(center.x, center.y)
                val lx = center.x + (cos(rad) * bladeLength * 0.4f).toFloat() - (sin(rad) * bladeWidth * 0.5f).toFloat()
                val ly = center.y + (sin(rad) * bladeLength * 0.4f).toFloat() + (cos(rad) * bladeWidth * 0.5f).toFloat()
                val tx = center.x + (cos(rad) * bladeLength).toFloat()
                val ty = center.y + (sin(rad) * bladeLength).toFloat()
                val rx = center.x + (cos(rad) * bladeLength * 0.7f).toFloat() + (sin(rad) * bladeWidth * 0.3f).toFloat()
                val ry = center.y + (sin(rad) * bladeLength * 0.7f).toFloat() - (cos(rad) * bladeWidth * 0.3f).toFloat()
                
                lineTo(lx, ly)
                quadraticTo(tx - (sin(rad)*5f).toFloat(), ty + (cos(rad)*5f).toFloat(), tx, ty)
                quadraticTo(rx, ry, center.x, center.y)
                close()
            }
            
            val bladeColor = when (i) {
                0 -> Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF2979FF)))
                1 -> Brush.linearGradient(listOf(Color(0xFFFFEA00), Color(0xFFFF9100)))
                else -> Brush.linearGradient(listOf(Color(0xFFE040FB), Color(0xFF9013FE)))
            }
            
            drawPath(bladePath, bladeColor)
            
            // Shininess highlighted core line on blade
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = center,
                end = Offset(center.x + (cos(rad)*bladeLength*0.8f).toFloat(), center.y + (sin(rad)*bladeLength*0.8f).toFloat()),
                strokeWidth = 1.5f.dp.toPx()
            )
        }
    }
    
    // Polished golden central cap
    drawCircle(
        color = Color(0x44000000),
        radius = r * 0.28f,
        center = Offset(center.x + 1f, center.y + 2f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFD54F), Color(0xFFF57F17)),
            center = center,
            radius = r * 0.25f
        ),
        radius = r * 0.25f
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = r * 0.08f,
        center = Offset(center.x - r * 0.07f, center.y - r * 0.07f)
    )
}

// 11. TNT (triple dynamite bundled together with brown fuse & sparking element)
private fun DrawScope.drawTNT(center: Offset, radius: Float) {
    val r = radius * 0.85f
    val w = r * 0.35f
    val h = r * 1.3f
    
    // Shadow base
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.4f),
        topLeft = Offset(center.x - w * 1.45f + 2f, center.y - h / 2f + 4f),
        size = Size(w * 2.9f, h),
        cornerRadius = CornerRadius(4.dp.toPx())
    )
    
    val leftOffset = -w * 0.88f
    val rightOffset = w * 0.88f
    
    // Three dynamite sticks
    drawDynamiteStick(Offset(center.x + leftOffset, center.y), w, h)
    drawDynamiteStick(Offset(center.x + rightOffset, center.y), w, h)
    drawDynamiteStick(Offset(center.x, center.y), w, h)
    
    // Binding black straps
    drawRect(
        color = Color(0xFF212121),
        topLeft = Offset(center.x - w * 1.35f, center.y - h * 0.25f),
        size = Size(w * 2.7f, h * 0.12f)
    )
    drawRect(
        color = Color(0xFF212121),
        topLeft = Offset(center.x - w * 1.35f, center.y + h * 0.15f),
        size = Size(w * 2.7f, h * 0.12f)
    )
    
    // Fuse
    val fuseStart = Offset(center.x, center.y - h / 2f)
    val fuseCtrl = Offset(center.x + r * 0.3f, center.y - h * 0.7f)
    val fuseEnd = Offset(center.x + r * 0.5f, center.y - h * 0.8f)
    
    val fusePath = Path().apply {
        moveTo(fuseStart.x, fuseStart.y)
        quadraticTo(fuseCtrl.x, fuseCtrl.y, fuseEnd.x, fuseEnd.y)
    }
    drawPath(
        path = fusePath,
        color = Color(0xFF5D4037),
        style = Stroke(2.5f.dp.toPx(), cap = StrokeCap.Round)
    )
    
    // Burning fuse spark fire circle
    val sparkRadius = 5.dp.toPx()
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFFFFD54F), Color(0xFFFF3D00), Color.Transparent),
            center = fuseEnd,
            radius = sparkRadius * 2.5f
        ),
        radius = sparkRadius * 2.5f,
        center = fuseEnd
    )
    // Star crosses
    drawLine(Color.White, Offset(fuseEnd.x - sparkRadius, fuseEnd.y), Offset(fuseEnd.x + sparkRadius, fuseEnd.y), strokeWidth = 1.5f.dp.toPx())
    drawLine(Color.White, Offset(fuseEnd.x, fuseEnd.y - sparkRadius), Offset(fuseEnd.x, fuseEnd.y + sparkRadius), strokeWidth = 1.5f.dp.toPx())
}

private fun DrawScope.drawDynamiteStick(center: Offset, width: Float, height: Float) {
    val left = center.x - width / 2
    val top = center.y - height / 2
    
    // Cylinder body
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFF1744), Color(0xFF880E4F)),
            start = Offset(left, top),
            end = Offset(left + width, top)
        ),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(2.5f.dp.toPx())
    )
    
    val capHeight = height * 0.08f
    drawRoundRect(
        color = Color(0xFF263238),
        topLeft = Offset(left, top),
        size = Size(width, capHeight),
        cornerRadius = CornerRadius(1.5f.dp.toPx())
    )
    
    // Shiny specular side line
    drawLine(
        color = Color.White.copy(alpha = 0.35f),
        start = Offset(left + width * 0.25f, top + capHeight),
        end = Offset(left + width * 0.25f, top + height - capHeight),
        strokeWidth = 1f.dp.toPx()
    )
}

// 10. Draw horizontal/vertical stripes (with bright glossy candy stripe highlights!)
private fun DrawScope.drawStripedOverlay(center: Offset, radius: Float, isHorizontal: Boolean, rotation: Float) {
    val r = radius * 0.9f
    val numStripes = 4
    val stripeWidth = r * 0.16f
    val spacing = r * 0.45f

    // Iconic 3D high-energy candy stripes
    rotate(if (isHorizontal) -30f else 60f, center) {
        for (i in -2..2) {
            val offset = i * spacing
            // Draw a soft glowing halo behind each candy stripe
            drawLine(
                color = Color.White.copy(alpha = 0.25f),
                start = Offset(center.x - r * 1.1f, center.y + offset),
                end = Offset(center.x + r * 1.1f, center.y + offset),
                strokeWidth = stripeWidth * 1.8f,
                cap = StrokeCap.Round
            )
            // Main solid white glossy stripe
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White,
                        Color.White.copy(alpha = 0.3f)
                    )
                ),
                start = Offset(center.x - r * 1.1f, center.y + offset),
                end = Offset(center.x + r * 1.1f, center.y + offset),
                strokeWidth = stripeWidth,
                cap = StrokeCap.Round
            )
        }
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
            animationSpec = tween(durationMillis = 750, easing = LinearEasing)
        )
    }

    val p = progress.value
    val color = getCandyColor(type)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f

        // 1. Expanding shockwave concentric rings (happens quickly in first 50% of progress)
        val shockwaveP = (p / 0.5f).coerceAtMost(1f)
        if (shockwaveP < 1f) {
            drawCircle(
                color = color.copy(alpha = (1f - shockwaveP) * 0.8f),
                radius = maxRadius * (0.1f + shockwaveP * 1.6f),
                center = center,
                style = Stroke(width = (4.dp.toPx() * (1f - shockwaveP)))
            )
            
            drawCircle(
                color = Color.White.copy(alpha = (1f - shockwaveP) * 0.4f),
                radius = maxRadius * (0.05f + shockwaveP * 1.2f),
                center = center,
                style = Stroke(width = (2.dp.toPx() * (1f - shockwaveP)))
            )
        }

        // 2. Flying candy Shards or Juice Drops radiating outwards (first 60% of progress)
        val radialP = (p / 0.6f).coerceAtMost(1f)
        if (radialP < 1f) {
            val numParticles = 8
            for (i in 0 until numParticles) {
                val angleDeg = i * (360f / numParticles) + (radialP * 50f)
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val distance = maxRadius * (radialP * 1.9f)
                val px = center.x + (cos(angleRad) * distance).toFloat()
                val py = center.y + (sin(angleRad) * distance).toFloat()
                
                val pRadius = maxRadius * 0.25f * (1f - radialP)
                
                drawCircle(
                    color = color.copy(alpha = (1f - radialP)),
                    radius = pRadius,
                    center = Offset(px, py)
                )
                
                if (i % 2 == 0) {
                    drawCircle(
                        color = Color.White.copy(alpha = (1f - radialP) * 0.8f),
                        radius = pRadius * 0.4f,
                        center = Offset(px - pRadius * 0.2f, py - pRadius * 0.2f)
                    )
                }
            }
        }

        // 3. Center flash and remaining imploding core
        if (p < 0.4f) {
            val coreScale = (1f - p / 0.4f)
            drawCircle(
                color = Color.White.copy(alpha = (1f - p * 2.5f).coerceAtLeast(0f)),
                radius = maxRadius * 0.6f * coreScale,
                center = center
            )
        }

        // 4. Detailed candy pieces popping up and falling down with simulated gravity and shadows!
        val shardRadius = maxRadius * 0.38f
        for (j in 0..2) {
            // Horizontal and vertical velocity setup (pops left-ish, center-up, right-ish)
            val vx = when (j) {
                0 -> -maxRadius * 2.2f
                1 -> maxRadius * 0.4f
                else -> maxRadius * 1.8f
            }
            val vy = when (j) {
                0 -> -maxRadius * 4.2f
                1 -> -maxRadius * 5.8f
                else -> -maxRadius * 3.5f
            }
            // Gravitational pull accelerating downwards over progresses
            val gravityY = maxRadius * 15f
            
            val dx = vx * p
            val dy = vy * p + 0.5f * gravityY * p * p
            
            val shardCenter = Offset(center.x + dx, center.y + dy)
            val shadowCenter = Offset(center.x + dx + 6.dp.toPx(), center.y + dy + 10.dp.toPx())
            val tumbleRotation = j * 120f + p * 360f

            // A. DRAW SEPARATE DEPTH SHADOW FIRST (Rendered behind the colored pieces)
            drawContext.canvas.save()
            rotate(tumbleRotation, shadowCenter) {
                val shadowColor = Color.Black.copy(alpha = 0.38f * (1f - p))
                when (type) {
                    CandyType.YELLOW_STAR -> {
                        val shadowStar = Path().apply {
                            val innerR = shardRadius * 0.45f
                            val outerR = shardRadius
                            for (i in 0 until 10) {
                                val angle = i * Math.PI / 5 - Math.PI / 2
                                val rCurr = if (i % 2 == 0) outerR else innerR
                                val x = shadowCenter.x + rCurr * cos(angle).toFloat()
                                val y = shadowCenter.y + rCurr * sin(angle).toFloat()
                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                            }
                            close()
                        }
                        drawPath(shadowStar, shadowColor)
                    }
                    CandyType.BLUE_GEM -> {
                        val shadowGem = Path().apply {
                            moveTo(shadowCenter.x, shadowCenter.y - shardRadius)
                            lineTo(shadowCenter.x + shardRadius * 0.82f, shadowCenter.y)
                            lineTo(shadowCenter.x, shadowCenter.y + shardRadius)
                            lineTo(shadowCenter.x - shardRadius * 0.82f, shadowCenter.y)
                            close()
                        }
                        drawPath(shadowGem, shadowColor)
                    }
                    CandyType.GREEN_CUBE -> {
                        drawRect(
                            color = shadowColor,
                            topLeft = Offset(shadowCenter.x - shardRadius * 0.8f, shadowCenter.y - shardRadius * 0.8f),
                            size = Size(shardRadius * 1.6f, shardRadius * 1.6f)
                        )
                    }
                    else -> {
                        drawCircle(
                            color = shadowColor,
                            radius = shardRadius,
                            center = shadowCenter
                        )
                    }
                }
            }
            drawContext.canvas.restore()

            // B. DRAW THE MAIN COLORED SHARD SHAPE ON TOP
            drawContext.canvas.save()
            rotate(tumbleRotation, shardCenter) {
                val shardAlpha = (1f - p * 0.25f)
                val shardColor = color.copy(alpha = shardAlpha)
                
                when (type) {
                    CandyType.YELLOW_STAR -> {
                        val starPath = Path().apply {
                            val innerR = shardRadius * 0.45f
                            val outerR = shardRadius
                            for (i in 0 until 10) {
                                val angle = i * Math.PI / 5 - Math.PI / 2
                                val rCurr = if (i % 2 == 0) outerR else innerR
                                val x = shardCenter.x + rCurr * cos(angle).toFloat()
                                val y = shardCenter.y + rCurr * sin(angle).toFloat()
                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                            }
                            close()
                        }
                        drawPath(
                            path = starPath,
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFF9C4).copy(alpha = shardAlpha), shardColor),
                                center = shardCenter,
                                radius = shardRadius
                            )
                        )
                        drawPath(starPath, Color.White.copy(alpha = 0.5f * shardAlpha), style = Stroke(1.5.dp.toPx()))
                    }
                    CandyType.BLUE_GEM -> {
                        val gemPath = Path().apply {
                            moveTo(shardCenter.x, shardCenter.y - shardRadius)
                            lineTo(shardCenter.x + shardRadius * 0.8f, shardCenter.y)
                            lineTo(shardCenter.x, shardCenter.y + shardRadius)
                            lineTo(shardCenter.x - shardRadius * 0.8f, shardCenter.y)
                            close()
                        }
                        drawPath(
                            path = gemPath,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFE0F7FA).copy(alpha = shardAlpha), shardColor, Color(0xFF006064).copy(alpha = shardAlpha)),
                                start = Offset(shardCenter.x - shardRadius, shardCenter.y - shardRadius),
                                end = Offset(shardCenter.x + shardRadius, shardCenter.y + shardRadius)
                            )
                        )
                        drawPath(gemPath, Color.White.copy(alpha = 0.4f * shardAlpha), style = Stroke(1.5.dp.toPx()))
                    }
                    CandyType.GREEN_CUBE -> {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFB9F6CA).copy(alpha = shardAlpha), shardColor, Color(0xFF004D40).copy(alpha = shardAlpha))
                            ),
                            topLeft = Offset(shardCenter.x - shardRadius * 0.8f, shardCenter.y - shardRadius * 0.8f),
                            size = Size(shardRadius * 1.6f, shardRadius * 1.6f)
                        )
                        drawRect(
                            color = Color.White.copy(alpha = 0.4f * shardAlpha),
                            topLeft = Offset(shardCenter.x - shardRadius * 0.8f, shardCenter.y - shardRadius * 0.8f),
                            size = Size(shardRadius * 1.6f, shardRadius * 1.6f),
                            style = Stroke(1.5.dp.toPx())
                        )
                    }
                    CandyType.CHOCO_BALL -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF8D6E63).copy(alpha = shardAlpha), shardColor, Color(0xFF3E2723).copy(alpha = shardAlpha)),
                                center = shardCenter,
                                radius = shardRadius
                            ),
                            radius = shardRadius,
                            center = shardCenter
                        )
                        drawCircle(Color(0xFF00E676).copy(alpha = shardAlpha), radius = shardRadius * 0.2f, center = Offset(shardCenter.x - shardRadius * 0.3f, shardCenter.y - shardRadius * 0.3f))
                        drawCircle(Color(0xFFFFEA00).copy(alpha = shardAlpha), radius = shardRadius * 0.2f, center = Offset(shardCenter.x + shardRadius * 0.3f, shardCenter.y + shardRadius * 0.2f))
                    }
                    CandyType.PURPLE_BERRY -> {
                        val offsets = listOf(
                            Offset(shardCenter.x - shardRadius * 0.3f, shardCenter.y - shardRadius * 0.2f),
                            Offset(shardCenter.x + shardRadius * 0.3f, shardCenter.y - shardRadius * 0.2f),
                            Offset(shardCenter.x, shardCenter.y + shardRadius * 0.3f)
                        )
                        for (off in offsets) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFEA80FC).copy(alpha = shardAlpha), shardColor),
                                    center = off,
                                    radius = shardRadius * 0.5f
                                ),
                                radius = shardRadius * 0.5f,
                                center = off
                            )
                        }
                    }
                    else -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.5f * shardAlpha), shardColor),
                                center = shardCenter,
                                radius = shardRadius
                            ),
                            radius = shardRadius,
                            center = shardCenter
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.6f * shardAlpha),
                            radius = shardRadius * 0.25f,
                            center = Offset(shardCenter.x - shardRadius * 0.35f, shardCenter.y - shardRadius * 0.35f)
                        )
                    }
                }
            }
            drawContext.canvas.restore()
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
