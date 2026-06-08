package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.R
import com.example.data.AppDatabase
import com.example.data.model.PlayerState
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt

fun getSafeAvatarLabel(avatarString: String): String {
    return when (avatarString) {
        "🍬" -> "CR"
        "🍭" -> "SH"
        "🍩" -> "DK"
        "🍫" -> "KT"
        "🧁" -> "LD"
        "🍪" -> "QN"
        "🧸" -> "KG"
        "👑" -> "CR"
        "🛡️" -> "SH"
        "⚔️" -> "KT"
        "🏰" -> "LD"
        "💎" -> "DM"
        "🌟" -> "ST"
        "🦁" -> "KG"
        else -> if (avatarString.length > 2) avatarString.take(2).uppercase() else avatarString.uppercase()
    }
}

@Composable
fun GoldCoinIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2
        // Gold Outer Ring
        drawCircle(
            color = Color(0xFFFFD700),
            radius = r
        )
        // Shadow Core
        drawCircle(
            color = Color(0xFFB8860B),
            radius = r * 0.85f
        )
        // Bright Inner Gold
        drawCircle(
            color = Color(0xFFFFE135),
            radius = r * 0.7f
        )
    }
}

@Composable
fun ShinyGemIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w / 2, 0f)
            lineTo(w, h * 0.35f)
            lineTo(w * 0.8f, h)
            lineTo(w * 0.2f, h)
            lineTo(0f, h * 0.35f)
            close()
        }
        drawPath(
            path = path,
            color = Color(0xFF00E5FF)
        )
        val innerPath = Path().apply {
            moveTo(w / 2, h * 0.15f)
            lineTo(w * 0.85f, h * 0.38f)
            lineTo(w * 0.7f, h * 0.85f)
            lineTo(w * 0.3f, h * 0.85f)
            lineTo(w * 0.15f, h * 0.38f)
            close()
        }
        drawPath(
            path = innerPath,
            color = Color(0xFFE0F7FA).copy(alpha = 0.7f)
        )
    }
}

@Composable
fun HeartIcon(modifier: Modifier = Modifier, empty: Boolean = false) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w / 2f, h * 0.25f)
            cubicTo(w * 0.2f, h * -0.1f, w * -0.1f, h * 0.4f, w / 2f, h * 0.9f)
            cubicTo(w * 1.1f, h * 0.4f, w * 0.8f, h * -0.1f, w / 2f, h * 0.25f)
        }
        drawPath(
            path = path,
            color = if (empty) Color(0xFF555555) else Color(0xFFFF1744)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameMainNavigator(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val state by viewModel.playerState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1E102F) // Midnight deep plum candy background
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
            },
            label = "screen_navigation"
        ) { screen ->
            when (screen) {
                is GameScreen.Splash -> SplashScreen()
                is GameScreen.Login -> LoginRegisterScreen(viewModel)
                is GameScreen.Home -> HomeScreen(viewModel, state)
                is GameScreen.Map -> WorldMapScreen(viewModel, state)
                is GameScreen.Gameplay -> GameplayScreen(viewModel, screen.level)
            }
        }
    }
}

// 1. GORGEOUS INTRO SPLASH SCREEN
@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF311B92), Color(0xFF1E102F), Color(0xFF0D0415))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        DecorativeBackgroundPattern()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // High-end generated game logo
            Image(
                painter = painterResource(id = R.drawable.img_app_icon),
                contentDescription = "Royal Crush Logo",
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulse)
                    .shadow(16.dp, RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "ROYAL CRUSH",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFD54F),
                letterSpacing = 2.sp,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = "P U Z Z L E",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 6.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color(0xFFFFC107),
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Gathering Sweet Crystal Shards...",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontFamily = FontFamily.SansSerif
            )
        }

        // Studio branding footer on the middle down corner
        Text(
            text = "AwoVision Studios Presents",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD54F),
            letterSpacing = 4.sp,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .shadow(2.dp)
        )
    }
}

// 2. MAIN LOBBY HOME SCREEN
@Composable
fun HomeScreen(viewModel: GameViewModel, state: PlayerState) {
    var showSettings by remember { mutableStateOf(false) }
    var showLeaderboards by remember { mutableStateOf(false) }
    var showHearts by remember { mutableStateOf(false) }
    var showLiveEventLeaderboard by remember { mutableStateOf(false) }
    var showAdminTools by remember { mutableStateOf(false) }

    val liveEventActive by viewModel.liveEventActive.collectAsState()
    val liveEventTimeRemaining by viewModel.liveEventTimeRemaining.collectAsState()
    val showEventEndPodium by viewModel.showEventEndPodium.collectAsState()
    val showBoosterShop by viewModel.showBoosterShop.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF8E24AA), Color(0xFF311B92), Color(0xFF1E102F))
                )
            )
    ) {
        DecorativeBackgroundPattern()
        // Decorative background sparkles/castles
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Upper stats bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold Coins Pill
                Card(
                    modifier = Modifier.clickable { SoundManager.playSoftClick() },
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoldCoinIcon(Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${state.coins}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Hearts life pill (Clicking opens Hearts Dialog refills!)
                Card(
                    modifier = Modifier.clickable { showHearts = true; SoundManager.playMenuWhoosh() },
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeartIcon(Modifier.size(18.dp), empty = state.lives == 0)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${state.lives}/5", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Gems Pill
                Card(
                    modifier = Modifier.clickable { SoundManager.playSoftClick() },
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShinyGemIcon(Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${state.gems}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 👑 Live Event Countdown Widget on Home Screen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .border(
                        2.dp,
                        if (liveEventActive) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        if (liveEventActive) {
                            showLiveEventLeaderboard = true
                            SoundManager.playMenuWhoosh()
                        } else {
                            SoundManager.playSoftClick()
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (liveEventActive) Color(0xFFF50057).copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(if (liveEventActive) "👑" else "⏳", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (liveEventActive) "Live Event is Active!" else "Next Global Tourney Starts",
                                color = if (liveEventActive) Color(0xFFFFD54F) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (liveEventActive) {
                                Text(
                                    text = "Your score: ${state.eventLevelsCompleted} matches completed",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "👉 TAP TO VIEW LIVE LEADERBOARD",
                                    color = Color(0xFF00FF66),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Compete offline and wait for event refuels",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (liveEventActive) Color(0xFFF50057) else Color.DarkGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = liveEventTimeRemaining,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Royal Path Progression Dashboard
            RoyalProgressDashboard(viewModel = viewModel, state = state)

            if (state.username == "MrAwo") {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                        .clickable { showAdminTools = true; SoundManager.playMenuWhoosh() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1035)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "DEV ADMIN CONSOLE",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color(0xFFFFD54F)
                                )
                                Text(
                                    text = "Unlocked for MrAwo • Modify state",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "Open Admin Tools",
                            tint = Color(0xFFFFD54F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic layout with Settings and Rankings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 3D Settings button
                Button3D(
                    onClick = { showSettings = true; SoundManager.playMenuWhoosh() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    containerColor = Color(0xFFEC407A),
                    bottomColor = Color(0xFFC2185B)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SETTINGS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                }

                // 3D Rankings button
                Button3D(
                    onClick = { showLeaderboards = true; SoundManager.playMenuWhoosh() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    containerColor = Color(0xFFFFD54F),
                    bottomColor = Color(0xFFC7A500)
                ) {
                    Text("🏆 RANKINGS", fontWeight = FontWeight.Black, color = Color(0xFF4E342E), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Level Badge Button - Play current level and prompt initial boosters selection
            Button(
                onClick = { viewModel.selectedPreLevel.value = state.currentLevel },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .testTag("play_button")
                    .border(3.dp, Color(0xFFFFD54F), RoundedCornerShape(34.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E676)
                ),
                shape = RoundedCornerShape(34.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("👑", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "PLAY LEVEL ${state.currentLevel}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Booster Shop / Store
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BOOSTER SHOP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFE082)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BoosterPurchaseColumn("Hammer", "hammer", 150, state.getBoosterCount("hammer"), viewModel)
                        BoosterPurchaseColumn("Colors", "colorbomb", 200, state.getBoosterCount("colorbomb"), viewModel)
                        BoosterPurchaseColumn("Striped", "striped", 180, state.getBoosterCount("striped"), viewModel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Claims panel & Debug clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.claimDailyGift() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Daily Gift", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.debugResetAll() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA1887F)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Clear Progress", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dedicated Booster Shop Trigger Button
            Button(
                onClick = { viewModel.showBoosterShop.value = true; SoundManager.playMenuWhoosh() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, Color(0xFFFFD54F))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛍️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("OPEN BOOSTER & COIN SHOP", fontWeight = FontWeight.Black, color = Color.White, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Settings, Leaderboard & Heart Refill dialog triggers
        if (showSettings) {
            SettingsDialog(viewModel, state, onClose = { showSettings = false })
        }
        if (showLeaderboards) {
            LeaderboardsOverlay(viewModel, state, onClose = { showLeaderboards = false })
        }
        if (showHearts) {
            HeartsRefillOverlay(viewModel, state, onClose = { showHearts = false })
        }
        if (showLiveEventLeaderboard) {
            LiveEventLeaderboardOverlay(viewModel, onClose = { showLiveEventLeaderboard = false })
        }
        if (showEventEndPodium) {
            LiveEventEndPodiumOverlay(viewModel, onClose = { viewModel.showEventEndPodium.value = false })
        }
        if (showBoosterShop) {
            BoosterShopOverlay(viewModel, state, onClose = { viewModel.showBoosterShop.value = false })
        }

        if (showAdminTools) {
            AdminToolsOverlay(viewModel, state, onClose = { showAdminTools = false })
        }

        val selectPreLvl by viewModel.selectedPreLevel.collectAsState()
        if (selectPreLvl != null) {
            PreLevelBoosterPopup(viewModel, state, lvlChoice = selectPreLvl!!)
        }

        LiveEventAnnouncementBanner(viewModel)
}
}

@Composable
fun RoyalProgressDashboard(
    viewModel: GameViewModel,
    state: PlayerState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16092C)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ROYAL PATHWAY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFA726),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Royal Journey Progress",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                androidx.compose.material3.Surface(
                    color = Color(0xFFFFD54F),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "LEVEL ${state.currentLevel}",
                        color = Color(0xFF4E342E),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Progress bar and decorations
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.CenterStart
            ) {
                // Progress filling
                val percent = (state.currentLevel.toFloat() / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = percent)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFD54F), Color(0xFFFF9800), Color(0xFFFF2A6D))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Meadows (lvl 1)",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "Milestone: Level 100",
                    fontSize = 10.sp,
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button: View Map Pathway
            Button(
                onClick = { viewModel.navigateTo(GameScreen.Map); SoundManager.playMenuWhoosh() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF311B92)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFFD54F))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Map Icon",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VIEW ADVENTURE MAP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}



/*
    val buildings by viewModel.buildingLevels.collectAsState()
    val kingdomLvl by viewModel.kingdomLevel.collectAsState()
    
    // Dynamic timer for rotating windmills, spouting water and sparkling stars!
    var animationTime by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            animationTime += 0.05f
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16092C)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val localDensity = LocalDensity.current
            val widthPx = with(localDensity) { maxWidth.toPx() }
            val heightPx = with(localDensity) { maxHeight.toPx() }
            val cx = widthPx / 2f
            val cy = heightPx / 2f + with(localDensity) { 40.dp.toPx() }
            
            // Isometric project functions
            fun project(x: Float, y: Float, z: Float): Offset {
                // X axis = 30 deg, Y axis = 150 deg down-pointing
                val px = cx + (x - y) * 0.866f
                val py = cy + (x + y) * 0.5f - z
                return Offset(px, py)
            }
            
            fun drawIsoPrism(
                x: Float, y: Float, z: Float,
                dx: Float, dy: Float, dz: Float,
                topColor: Color, leftColor: Color, rightColor: Color,
                canvasDrawScope: androidx.compose.ui.graphics.drawscope.DrawScope
            ) {
                val p1 = project(x, y, z)
                val p2 = project(x + dx, y, z)
                val p3 = project(x + dx, y + dy, z)
                val p4 = project(x, y + dy, z)
                
                val p5 = project(x, y, z + dz)
                val p6 = project(x + dx, y, z + dz)
                val p7 = project(x + dx, y + dy, z + dz)
                val p8 = project(x, y + dy, z + dz)
                
                // Left
                val leftPath = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(p4.x, p4.y)
                    lineTo(p8.x, p8.y)
                    lineTo(p5.x, p5.y)
                    close()
                }
                canvasDrawScope.drawPath(leftPath, leftColor)
                
                // Right
                val rightPath = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p6.x, p6.y)
                    lineTo(p5.x, p5.y)
                    close()
                }
                canvasDrawScope.drawPath(rightPath, rightColor)
                
                // Top
                val topPath = Path().apply {
                    moveTo(p5.x, p5.y)
                    lineTo(p6.x, p6.y)
                    lineTo(p7.x, p7.y)
                    lineTo(p8.x, p8.y)
                    close()
                }
                canvasDrawScope.drawPath(topPath, topColor)
            }
            
            // Draw 2D custom Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Splash background moat
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00B0FF), Color(0xFF0D47A1)),
                        center = Offset(cx, cy),
                        radius = 210.dp.toPx()
                    ),
                    radius = 180.dp.toPx(),
                    center = Offset(cx, cy)
                )
                
                // Ripples
                for (i in 0 until 3) {
                    val waveRadius = ((100 + i * 40 + (animationTime * 15f) % 40) % 180).dp.toPx()
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = waveRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                
                // 2. Green meadow island
                val mPath = Path().apply {
                    val scale = 140.dp.toPx()
                    moveTo(cx, cy - scale * 0.5f)
                    lineTo(cx + scale * 0.866f, cy - scale * 0.25f)
                    lineTo(cx + scale * 0.866f, cy + scale * 0.25f)
                    lineTo(cx, cy + scale * 0.5f)
                    lineTo(cx - scale * 0.866f, cy + scale * 0.25f)
                    lineTo(cx - scale * 0.866f, cy - scale * 0.25f)
                    close()
                }
                val dPath = Path().apply {
                    val scale = 140.dp.toPx()
                    val th = 12.dp.toPx()
                    moveTo(cx - scale * 0.866f, cy + scale * 0.25f)
                    lineTo(cx, cy + scale * 0.5f)
                    lineTo(cx + scale * 0.866f, cy + scale * 0.25f)
                    lineTo(cx + scale * 0.866f, cy + scale * 0.25f + th)
                    lineTo(cx, cy + scale * 0.5f + th)
                    lineTo(cx - scale * 0.866f, cy + scale * 0.25f + th)
                    close()
                }
                drawPath(dPath, Color(0xFF5E35B1))
                drawPath(mPath, Color(0xFF4CAF50))
                
                // Paths crossing
                val path1 = Path().apply {
                    val po1 = project(-100f, 0f, 0f)
                    val po2 = project(100f, 0f, 0f)
                    moveTo(po1.x, po1.y)
                    lineTo(po2.x, po2.y)
                }
                drawPath(path1, Color(0xFFE0E0E0).copy(alpha = 0.35f), style = Stroke(width = 12f))
                
                // 3. Buildings
                // A. Castle Keep (Central deep)
                val castleLvl = buildings["castle"] ?: 0
                if (castleLvl > 0) {
                    val ch = 35f + castleLvl * 10f
                    drawIsoPrism(-30f, -30f, 0f, 50f, 50f, ch, 
                        if (castleLvl == 5) Color(0xFFFFD54F) else Color(0xFFB0BEC5), 
                        if (castleLvl == 5) Color(0xFFF57F17) else Color(0xFF78909C), 
                        if (castleLvl == 5) Color(0xFFFFB300) else Color(0xFF90A4AE), 
                        this
                    )
                    
                    // Left defender tower
                    drawIsoPrism(-42f, -10f, 0f, 15f, 15f, ch + 15f, Color(0xFFCFD8DC), Color(0xFF546E7A), Color(0xFF78909C), this)
                    // Right defender tower
                    drawIsoPrism(-10f, -42f, 0f, 15f, 15f, ch + 15f, Color(0xFFCFD8DC), Color(0xFF546E7A), Color(0xFF78909C), this)
                    
                    // Conical roof
                    val rPeakL = project(-34.5f, -2.5f, ch + 30f)
                    val rBaseL1 = project(-42f, -10f, ch + 15f)
                    val rBaseL2 = project(-27f, -10f, ch + 15f)
                    val roofPathL = Path().apply {
                        moveTo(rPeakL.x, rPeakL.y)
                        lineTo(rBaseL1.x, rBaseL1.y)
                        lineTo(rBaseL2.x, rBaseL2.y)
                        close()
                    }
                    drawPath(roofPathL, Color(0xFFEF5350))
                    
                    // Castle flag
                    val flagStaffBottom = project(-5f, -5f, ch)
                    val flagStaffTop = project(-5f, -5f, ch + 28f)
                    drawLine(color = Color.White, start = flagStaffBottom, end = flagStaffTop, strokeWidth = 4f)
                    
                    val fWave = sin(animationTime * 2.5f) * 10f
                    val flagPath = Path().apply {
                        moveTo(flagStaffTop.x, flagStaffTop.y)
                        lineTo(flagStaffTop.x + 22f, flagStaffTop.y + 6f + fWave)
                        lineTo(flagStaffTop.x + 18f, flagStaffTop.y + 15f + fWave)
                        lineTo(flagStaffTop.x, flagStaffTop.y + 10f)
                        close()
                    }
                    drawPath(flagPath, if (castleLvl == 5) Color(0xFFFFD54F) else Color(0xFFE91E63))
                }
                
                // B. Mill (Left)
                val millLvl = buildings["mill"] ?: 0
                if (millLvl > 0) {
                    val mh = 30f + millLvl * 8f
                    drawIsoPrism(-90f, 15f, 0f, 35f, 35f, mh, Color(0xFFA1887F), Color(0xFF6D4C41), Color(0xFF8D6E63), this)
                    
                    // Windmill roof peak pyramid
                    val roofPeak = project(-72.5f, 32.5f, mh + 20f)
                    val rb2 = project(-55f, 15f, mh)
                    val rb3 = project(-55f, 50f, mh)
                    val roofPath = Path().apply {
                        moveTo(roofPeak.x, roofPeak.y)
                        lineTo(rb2.x, rb2.y)
                        lineTo(rb3.x, rb3.y)
                        close()
                    }
                    drawPath(roofPath, Color(0xFF4E342E))
                    
                    // Spinning vane blades
                    val centerRot = project(-55f, 32.5f, mh - 10f)
                    val bladeL = 36f + millLvl * 4f
                    val rotateAngle = animationTime * 30f
                    for (bIdx in 0 until 4) {
                        val ang = rotateAngle + bIdx * 90f
                        val rad = Math.toRadians(ang.toDouble())
                        val endX = centerRot.x + (cos(rad) * bladeL).toFloat()
                        val endY = centerRot.y + (sin(rad) * bladeL).toFloat()
                        drawLine(color = Color(0xFFD7CCC8), start = centerRot, end = Offset(endX, endY), strokeWidth = 4f)
                        
                        val fPath = Path().apply {
                            val fAng = Math.toRadians((ang + 15f).toDouble())
                            val fx1 = centerRot.x + (cos(rad) * bladeL * 0.4f).toFloat()
                            val fy1 = centerRot.y + (sin(rad) * bladeL * 0.4f).toFloat()
                            val fx2 = endX
                            val fy2 = endY
                            val fx3 = endX + (cos(fAng) * 11f).toFloat()
                            val fy3 = endY + (sin(fAng) * 11f).toFloat()
                            val fx4 = fx1 + (cos(fAng) * 11f).toFloat()
                            val fy4 = fy1 + (sin(fAng) * 11f).toFloat()
                            moveTo(fx1, fy1)
                            lineTo(fx2, fy2)
                            lineTo(fx3, fy3)
                            lineTo(fx4, fy4)
                            close()
                        }
                        drawPath(fPath, Color.White.copy(alpha = 0.85f))
                    }
                    drawCircle(color = Color(0xFF3E2723), radius = 5f, center = centerRot)
                }
                
                // C. Gardens (Right)
                val gardenLvl = buildings["garden"] ?: 0
                if (gardenLvl > 0) {
                    val gColors = listOf(Color(0xFFE91E63), Color(0xFFFFEB3B), Color(0xFF00FF66))
                    for (i in 0 until gardenLvl) {
                        val gx = 25f + i * 16f
                        val gy = -80f + i * 11f
                        drawIsoPrism(gx, gy, 0f, 12f, 12f, 8f, Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF388E3C), this)
                        
                        val flowCent = project(gx + 6f, gy + 6f, 9f)
                        drawCircle(color = gColors[i % gColors.size], radius = 3.5f, center = flowCent)
                    }
                }
                
                // D. Fountain (Front deep)
                val fountainLvl = buildings["fountain"] ?: 0
                if (fountainLvl > 0) {
                    val fCent = project(40f, 40f, 0f)
                    drawCircle(color = Color(0xFFE0E0E0), radius = 20f, center = fCent)
                    drawCircle(color = Color(0xFFBDBDBD), radius = 20f, center = fCent, style = Stroke(width = 3.dp.toPx()))
                    drawCircle(color = Color(0xFF00E5FF).copy(alpha = 0.45f), radius = 17f, center = fCent)
                    
                    val sHeight = (10f + sin(animationTime * 4.5f) * 8f).coerceAtLeast(3f)
                    val spoutTop = project(40f, 40f, sHeight)
                    val spoutBottom = project(40f, 40f, 1f)
                    drawLine(color = Color(0xFFE0F7FA), start = spoutBottom, end = spoutTop, strokeWidth = 4f)
                }
                
                // E. Golden Sugar Statue (Left front side)
                val statueLvl = buildings["statue"] ?: 0
                if (statueLvl > 0) {
                    drawIsoPrism(-45f, 70f, 0f, 14f, 14f, 16f, Color(0xFFECEFF1), Color(0xFF90A4AE), Color(0xFFB0BEC5), this)
                    
                    val floatH = 24f + sin(animationTime * 1.8f) * 3f
                    val stCent = project(-38f, 77f, floatH)
                    drawCircle(color = Color(0xFFFFD54F), radius = 7f, center = stCent)
                    
                    for (s in 0 until 4) {
                        val spAng = s * 90f + animationTime * 15f
                        val rad = Math.toRadians(spAng.toDouble())
                        val spX = stCent.x + (cos(rad) * 11f).toFloat()
                        val spY = stCent.y + (sin(rad) * 11f).toFloat()
                        drawCircle(color = Color(0xFFFFEA00), radius = 1.8f, center = Offset(spX, spY))
                    }
                }
            }
            
            // Render floating upgrade buttons
            val project2D = { x: Float, y: Float, z: Float ->
                val px = cx + (x - y) * 0.866f
                val py = cy + (x + y) * 0.5f - z
                Offset(px, py)
            }
            
            val buildingConfigs = listOf(
                Triple("castle", "Castle Keep", project2D(-5f, -5f, 60f + (buildings["castle"] ?: 0) * 12f)),
                Triple("mill", "Glory Mill", project2D(-72.5f, 32.5f, 38f)),
                Triple("garden", "Gardens", project2D(35f, -70f, 15f)),
                Triple("fountain", "Fountain", project2D(40f, 40f, 18f)),
                Triple("statue", "Statue", project2D(-38f, 77f, 26f))
            )
            
            for (bc in buildingConfigs) {
                val bKey = bc.first
                val pOffset = bc.third
                
                val currentLvl = buildings[bKey] ?: 0
                val px = with(localDensity) { pOffset.x.toDp() }
                val py = with(localDensity) { pOffset.y.toDp() }
                
                val price = when (currentLvl) {
                    0 -> 300
                    1 -> 500
                    2 -> 800
                    3 -> 1200
                    else -> 2000
                }
                val isMax = currentLvl >= 5
                
                Box(
                    modifier = Modifier
                        .offset(x = px - 45.dp, y = py - 18.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .size(width = 90.dp, height = 34.dp)
                            .border(
                                width = 1.dp,
                                color = if (isMax) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(17.dp)
                            )
                            .clickable { viewModel.upgradeBuilding(bKey) },
                        shape = RoundedCornerShape(17.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMax) Color(0xFFC2185B) else if (state.coins >= price) Color(0xFF00E676) else Color(0xFF5D4037)
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isMax) {
                                Text(
                                    text = "🏆 MAX",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            } else {
                                Text(
                                    text = if (currentLvl == 0) "BUILD" else "UPGRADE",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    GoldCoinIcon(Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "$price",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
*/

@Composable
fun BoosterPurchaseColumn(
    label: String,
    type: String,
    cost: Int,
    owned: Int,
    viewModel: GameViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(8.dp)
            .width(80.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "(Owned: $owned)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = { viewModel.purchaseBooster(type, cost) },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                GoldCoinIcon(Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "$cost", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 3. STORY WORLD MAP SCREEN
@Composable
fun WorldMapScreen(viewModel: GameViewModel, state: PlayerState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF3e2723), Color(0xFF1A237E), Color(0xFF0D0415))
                )
            )
    ) {
        DecorativeBackgroundPattern()
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(GameScreen.Home) }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                Text(
                    text = "Adventure Map",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoldCoinIcon(Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${state.coins}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Scrollable Map Nodes Pathway representing levels
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                // Generate level listing node items
                // Every 10 levels we can have worlds, and bosses at 100
                val levelsList = (1..100).toList() // Represent 1 to 100 for easy visual map traversal

                items(levelsList.chunked(5).reversed()) { rowLevels ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (lvl in rowLevels) {
                            val isUnlocked = lvl <= state.currentLevel
                            val isBoss = lvl % 10 == 0 // Mini-boss at 10, real milestone bosses at 100
                            val stars = state.getStarsForLevel(lvl)
                            val worldColor = when {
                                lvl <= 10 -> Color(0xFF4CAF50) // Candy Meadows green
                                lvl <= 20 -> Color(0xFF795548) // Chocolate Mountains brown
                                lvl <= 30 -> Color(0xFFE040FB) // Jelly Jungle violet
                                lvl <= 40 -> Color(0xFFFF9800) // Cookie Desert Orange
                                else -> Color(0xFFE91E63) // Castle magenta
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.testTag("level_node_$lvl")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isUnlocked) {
                                                Brush.radialGradient(listOf(worldColor.copy(alpha = 0.5f), worldColor))
                                            } else {
                                                Brush.radialGradient(listOf(Color(0xFF555555), Color(0xFF333333)))
                                            }
                                        )
                                        .border(
                                            width = if (isBoss && isUnlocked) 4.dp else 2.dp,
                                            color = if (isBoss) Color(0xFFFF1744) else Color.White,
                                            shape = CircleShape
                                        )
                                        .clickable(enabled = isUnlocked) {
                                            viewModel.selectedPreLevel.value = lvl
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!isUnlocked) {
                                        Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            if (isBoss) {
                                                Icon(Icons.Filled.Warning, contentDescription = "Boss", tint = Color.White, modifier = Modifier.size(16.dp))
                                            } else {
                                                Text(
                                                    text = "$lvl",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Stars rated underneath
                                if (isUnlocked && stars > 0) {
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        for (i in 1..3) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = if (i <= stars) Color(0xFFFFD54F) else Color.Gray,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                } else if (isUnlocked && stars == 0 && isBoss) {
                                    Text("BOSS", color = Color(0xFFFF1744), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Render Pre-level booster select popup on click
        val selectPreLvl by viewModel.selectedPreLevel.collectAsState()
        if (selectPreLvl != null) {
            PreLevelBoosterPopup(viewModel, state, lvlChoice = selectPreLvl!!)
        }
    }
}

// 3.5 PRE-LEVEL BOOSTER POPUP SCREEN
@Composable
fun PreLevelBoosterPopup(viewModel: GameViewModel, state: com.example.data.model.PlayerState, lvlChoice: Int) {
    var dupeChecked by remember { mutableStateOf(false) }
    var tntChecked by remember { mutableStateOf(false) }
    var spinnerChecked by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { viewModel.selectedPreLevel.value = null }
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1035)),
            border = BorderStroke(2.dp, Color(0xFFFFD54F)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("pre_level_popup")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "LEVEL $lvlChoice",
                    color = Color(0xFFFFD54F),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Battle Preparation",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Objective hint card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF120822)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎯", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                        Column {
                            Text(
                                text = "Objective:",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (lvlChoice % 10 == 0) "Defeat the Giant Candy Boss!" else "Match candies and clear all barriers!",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Text(
                    text = "EQUIP INITIAL BOOSTERS",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )

                // Booster Item Row: Electro Ball
                val rawDupeCount = state.getBoosterCount("dupe_bomb")
                BoosterSelectionRow(
                    title = "Electro Ball ⚡",
                    description = "Starts with a Color Bomb!",
                    stock = rawDupeCount,
                    isSelected = dupeChecked,
                    onToggle = { dupeChecked = !dupeChecked },
                    testTag = "pre_equip_dupe"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Booster Item Row: TNT
                val rawTntCount = state.getBoosterCount("tnt_bomb")
                BoosterSelectionRow(
                    title = "TNT Explosive 💥",
                    description = "Starts with a TNT explosive!",
                    stock = rawTntCount,
                    isSelected = tntChecked,
                    onToggle = { tntChecked = !tntChecked },
                    testTag = "pre_equip_tnt"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Booster Item Row: Spinner
                val rawSpinnerCount = state.getBoosterCount("spinner")
                BoosterSelectionRow(
                    title = "Bomb Spinner 🌪️",
                    description = "Launches 3 obstacle-clearers!",
                    stock = rawSpinnerCount,
                    isSelected = spinnerChecked,
                    onToggle = { spinnerChecked = !spinnerChecked },
                    testTag = "pre_equip_spinner"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel
                    Button(
                        onClick = { viewModel.selectedPreLevel.value = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    // Start Game!
                    Button(
                        onClick = {
                            viewModel.selectedPreLevel.value = null
                            viewModel.loadLevelWithBoosters(
                                level = lvlChoice,
                                useDupe = dupeChecked,
                                useTnt = tntChecked,
                                useSpinner = spinnerChecked
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("pre_start_button")
                    ) {
                        Text("START 🏁", color = Color(0xFF001202), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BoosterSelectionRow(
    title: String,
    description: String,
    stock: Int,
    isSelected: Boolean,
    onToggle: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2D164F) else Color(0xFF16092C)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = description, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Owned: $stock",
                    color = if (stock > 0) Color(0xFF00FF66) else Color(0xFFFF1744),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFFFD54F),
                        uncheckedColor = Color.White.copy(alpha = 0.3f),
                        checkmarkColor = Color(0xFF1E1035)
                    ),
                    modifier = Modifier.testTag(testTag)
                )
            }
        }
    }
}

// 4. MAIN GAMEPLAY / PUZZLE SCREEN
@Composable
fun GameplayScreen(viewModel: GameViewModel, level: Int) {
    val board by viewModel.boardState.collectAsState()
    val obstacles by viewModel.obstacleState.collectAsState()
    val obstacleDurability by viewModel.obstacleDurabilityState.collectAsState()
    val explodingCandies by viewModel.explodingCandiesState.collectAsState()
    val obstacleAnimations by viewModel.obstacleAnimationsState.collectAsState()
    val moves by viewModel.movesLeft.collectAsState()
    val score by viewModel.currentScore.collectAsState()
    val goals by viewModel.goalsList.collectAsState()
    val floatingList by viewModel.floatingTexts.collectAsState()
    val state by viewModel.playerState.collectAsState()

    // Boss properties
    val isBoss by viewModel.isBossBattle.collectAsState()
    val bName by viewModel.bossName.collectAsState()
    val bHp by viewModel.bossHp.collectAsState()
    val bMax by viewModel.bossMaxHp.collectAsState()
    val bPhase by viewModel.bossPhase.collectAsState()
    val bAttackCountdown by viewModel.bossAttackCountdown.collectAsState()
    val cinematicState by viewModel.bossCinematicState.collectAsState()
    val bActivity by viewModel.bossActivityState.collectAsState()
    val bSpeech by viewModel.bossSpeechBubble.collectAsState()
    val hintCandies by viewModel.hintHighlightCandies.collectAsState()

    // Popups
    val isDone by viewModel.isLevelDone.collectAsState()
    val isOver by viewModel.isGameOver.collectAsState()

    // State matching selection variables for swiping
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_game_background),
            contentDescription = "Game Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isBoss) {
                            listOf(Color(0xFF3E1F47).copy(alpha = 0.5f), Color(0xFF0E0715).copy(alpha = 0.85f))
                        } else {
                            listOf(Color(0xFF145374).copy(alpha = 0.4f), Color(0xFF000814).copy(alpha = 0.82f))
                        }
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)
        ) {
            // Header Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(GameScreen.Map) }) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = "Exit to Map", tint = Color.White)
                }

                Text(
                    text = "Level $level",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoldCoinIcon(Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${state.coins}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Target Objectives Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Moves
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MOVES", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("$moves", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD54F))
                    }

                    // Divider
                    Box(modifier = Modifier.size(1.dp, 35.dp).background(Color.White.copy(alpha = 0.2f)))

                    // Score
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SCORE", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("$score", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Divider
                    Box(modifier = Modifier.size(1.dp, 35.dp).background(Color.White.copy(alpha = 0.2f)))

                    // Goals
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (goal in goals) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(32.dp)) {
                                    if (goal.type != null) {
                                        CandyIcon(type = goal.type, modifier = Modifier.fillMaxSize())
                                    } else if (goal.isBlocker) {
                                        drawBlockerShapeMini(goal.blockerType)
                                    } else {
                                        Icon(Icons.Filled.Favorite, contentDescription = "HP goal/Score", tint = Color.Red, modifier = Modifier.fillMaxSize())
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${goal.currentCount}/${goal.targetCount}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (goal.isCompleted) Color(0xFF00FF66) else Color.White
                                    )
                                    if (goal.isCompleted) {
                                        Icon(Icons.Filled.Check, contentDescription = "Completed", tint = Color(0xFF00FF66), modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Epically Interactive BOSS ENCOUNTER BAR (Milestones check)
            if (isBoss) {
                BossArenaPanel(
                    name = bName,
                    hp = bHp,
                    maxHp = bMax,
                    phase = bPhase,
                    countdown = bAttackCountdown,
                    cinematicState = cinematicState,
                    activity = bActivity,
                    speech = bSpeech
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // PRIMARY 8x8 MATCH-3 BOARD GRID CONTAINER
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background board decorative grid frame
                val darkness by viewModel.boardDarkness.collectAsState()
                val flashActive by viewModel.boardFlash.collectAsState()
                val shakeOffset by viewModel.screenShakeOffset.collectAsState()
                val spinList by viewModel.flyingSpinners.collectAsState()
                val arcList by viewModel.lightningArcs.collectAsState()
                val partList by viewModel.boardParticles.collectAsState()

                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .offset(x = shakeOffset.first.dp, y = shakeOffset.second.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(3.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        // Drawing grid checkered floor patterns underneath
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width / 8f
                            val h = size.height / 8f
                            for (r in 0 until 8) {
                                for (c in 0 until 8) {
                                    if ((r + c) % 2 == 0) {
                                        drawRect(
                                            color = Color.Black.copy(alpha = 0.12f),
                                            topLeft = Offset(c * w, r * h),
                                            size = Size(w, h)
                                        )
                                    }
                                }
                            }
                        }

                        // Playable Board items
                        if (board.isNotEmpty()) {
                            GridComposablesBoard(
                                board = board,
                                obstacles = obstacles,
                                obstacleDurability = obstacleDurability,
                                explodingCandies = explodingCandies,
                                selectedCell = selectedCell,
                                hintHighlightCandies = hintCandies,
                                onCellSelect = { r, c ->
                                    val sel = selectedCell
                                    if (sel == null) {
                                        selectedCell = Pair(r, c)
                                    } else {
                                        // Tap adjacent to swap back up
                                        viewModel.performSwipeAction(sel.first, sel.second, r, c)
                                        selectedCell = null
                                    }
                                },
                                onSwipe = { r, c, dir ->
                                    val targetR = when (dir) {
                                        "UP" -> r - 1
                                        "DOWN" -> r + 1
                                        else -> r
                                    }
                                    val targetC = when (dir) {
                                        "LEFT" -> c - 1
                                        "RIGHT" -> c + 1
                                        else -> c
                                    }
                                    if (targetR in 0..7 && targetC in 0..7) {
                                        viewModel.performSwipeAction(r, c, targetR, targetC)
                                    }
                                },
                                obstacleAnimations = obstacleAnimations
                            )
                        }

                        // Overlay Darkness & Flash Effect
                        if (darkness > 0f) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = darkness)))
                        }
                        if (flashActive) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.9f)))
                        }

                        // Drawing Custom Animations and Particles on a top canvas layer
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width / 8f
                            val h = size.height / 8f

                            // Draw Neon Lightning Electric Arcs
                            arcList.forEach { arc ->
                                val fromX = arc.fromC * w + w / 2f
                                val fromY = arc.fromR * h + h / 2f
                                val toX = arc.toC * w + w / 2f
                                val toY = arc.toR * h + h / 2f
                                
                                val path = Path().apply {
                                    moveTo(fromX, fromY)
                                    val segments = 4
                                    for (i in 1 until segments) {
                                        val faction = i.toFloat() / segments
                                        val midX = fromX + (toX - fromX) * faction
                                        val midY = fromY + (toY - fromY) * faction
                                        // Random orthogonal offset
                                        val dx = ((Math.random() - 0.5f) * 18f).toFloat()
                                        val dy = ((Math.random() - 0.5f) * 18f).toFloat()
                                        lineTo(midX + dx, midY + dy)
                                    }
                                    lineTo(toX, toY)
                                }
                                
                                // Glowing background blur line
                                drawPath(
                                    path = path,
                                    color = arc.color.copy(alpha = 0.4f * arc.alpha),
                                    style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                )
                                // Main bright foreground core line
                                drawPath(
                                    path = path,
                                    color = Color.White.copy(alpha = arc.alpha),
                                    style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                )
                            }

                            // Draw Particles (Circular Chips & Glowing Flames/Smokes)
                            partList.forEach { p ->
                                val px = p.c * w + w / 2f
                                val py = p.r * h + h / 2f
                                
                                when (p.shape) {
                                    ParticleShape.CHIP -> {
                                        // Dynamic fragments with beautiful shadow
                                        drawCircle(
                                            color = Color.Black.copy(alpha = p.alpha * 0.4f),
                                            radius = p.size,
                                            center = Offset(px + 2f, py + 3f)
                                        )
                                        drawCircle(
                                            color = p.color.copy(alpha = p.alpha),
                                            radius = p.size,
                                            center = Offset(px, py)
                                        )
                                        drawCircle(
                                            color = Color.White.copy(alpha = p.alpha * 0.7f),
                                            radius = p.size * 0.4f,
                                            center = Offset(px - p.size * 0.3f, py - p.size * 0.3f)
                                        )
                                    }
                                    ParticleShape.FLAME -> {
                                        // Thermal core
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(Color.White, p.color, Color.Transparent),
                                                center = Offset(px, py),
                                                radius = p.size * 1.5f
                                            ),
                                            radius = p.size * 1.5f,
                                            center = Offset(px, py)
                                        )
                                    }
                                    ParticleShape.SMOKE -> {
                                        drawCircle(
                                            color = p.color.copy(alpha = p.alpha * 0.45f),
                                            radius = p.size,
                                            center = Offset(px, py)
                                        )
                                    }
                                    else -> {
                                        // Sparkles/Confetti
                                        val sizeVal = p.size
                                        val crossPath = Path().apply {
                                            moveTo(px - sizeVal, py)
                                            lineTo(px + sizeVal, py)
                                            moveTo(px, py - sizeVal)
                                            lineTo(px, py + sizeVal)
                                        }
                                        drawPath(
                                            path = crossPath,
                                            color = p.color.copy(alpha = p.alpha),
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }
                                }
                            }

                            // Draw Procedural Flying Propeller Spinners
                            spinList.forEach { spin ->
                                val sx = (spin.fromC + (spin.toC - spin.fromC) * spin.progress) * w + w / 2f
                                val sy = (spin.fromR + (spin.toR - spin.fromR) * spin.progress) * h + h / 2f
                                
                                val altitude = sin(spin.progress * Math.PI).toFloat() // peak in center of flight
                                val radiusScale = 22.dp.toPx() * (1f + altitude * 0.4f)
                                val spinRot = spin.rotation
                                
                                // 1. Draw Altitude Shadow
                                val shadowOffset = Offset(12f + altitude * 14f, 15f + altitude * 16f)
                                val shadowX = sx + shadowOffset.x
                                val shadowY = sy + shadowOffset.y
                                
                                rotate(spinRot, Offset(shadowX, shadowY)) {
                                    for (i in 0 until 3) {
                                        val angle = i * 120f
                                        val rad = Math.toRadians(angle.toDouble())
                                        val wl = radiusScale * 0.9f
                                        val ww = radiusScale * 0.35f
                                        
                                        val wp = Path().apply {
                                            moveTo(shadowX, shadowY)
                                            val lx = shadowX + (cos(rad) * wl * 0.4f).toFloat() - (sin(rad) * ww * 0.5f).toFloat()
                                            val ly = shadowY + (sin(rad) * wl * 0.4f).toFloat() + (cos(rad) * ww * 0.5f).toFloat()
                                            val tx = shadowX + (cos(rad) * wl).toFloat()
                                            val ty = shadowY + (sin(rad) * wl).toFloat()
                                            val rx = shadowX + (cos(rad) * wl * 0.7f).toFloat() + (sin(rad) * ww * 0.3f).toFloat()
                                            val ry = shadowY + (sin(rad) * wl * 0.7f).toFloat() - (cos(rad) * ww * 0.3f).toFloat()
                                            
                                            lineTo(lx, ly)
                                            quadraticTo(tx - (sin(rad)*5f).toFloat(), ty + (cos(rad)*5f).toFloat(), tx, ty)
                                            quadraticTo(rx, ry, shadowX, shadowY)
                                            close()
                                        }
                                        drawPath(wp, Color.Black.copy(alpha = 0.22f - altitude * 0.08f))
                                    }
                                }
                                
                                // 2. Draw TNT Payload if it is a bomb spinner!
                                if (spin.isBombSpinner) {
                                    val bWidth = radiusScale * 0.7f
                                    val bHeight = radiusScale * 0.9f
                                    drawRect(
                                        color = Color(0xFFD50000), // Red
                                        topLeft = Offset(sx - bWidth / 2f, sy - bHeight / 2f),
                                        size = Size(bWidth, bHeight)
                                    )
                                    // Yellow band
                                    drawRect(
                                        color = Color(0xFFFFD54F),
                                        topLeft = Offset(sx - bWidth / 2f, sy - bHeight * 0.35f),
                                        size = Size(bWidth, bHeight * 0.1f)
                                    )
                                    drawRect(
                                        color = Color(0xFFFFD54F),
                                        topLeft = Offset(sx - bWidth / 2f, sy + bHeight * 0.25f),
                                        size = Size(bWidth, bHeight * 0.1f)
                                    )
                                    drawCircle(
                                        color = Color(0xFF1A1A1A),
                                        radius = bWidth * 0.12f,
                                        center = Offset(sx, sy)
                                    )
                                }
                                
                                // 3. Draw Actual propeller spinner wings
                                rotate(spinRot, Offset(sx, sy)) {
                                    for (i in 0 until 3) {
                                        val angle = i * 120f
                                        val rad = Math.toRadians(angle.toDouble())
                                        val wl = radiusScale * 0.9f
                                        val ww = radiusScale * 0.35f
                                        
                                        val wp = Path().apply {
                                            moveTo(sx, sy)
                                            val lx = sx + (cos(rad) * wl * 0.4f).toFloat() - (sin(rad) * ww * 0.5f).toFloat()
                                            val ly = sy + (sin(rad) * wl * 0.4f).toFloat() + (cos(rad) * ww * 0.5f).toFloat()
                                            val tx = sx + (cos(rad) * wl).toFloat()
                                            val ty = sy + (sin(rad) * wl).toFloat()
                                            val rx = sx + (cos(rad) * wl * 0.7f).toFloat() + (sin(rad) * ww * 0.3f).toFloat()
                                            val ry = sy + (sin(rad) * wl * 0.7f).toFloat() - (cos(rad) * ww * 0.3f).toFloat()
                                            
                                            lineTo(lx, ly)
                                            quadraticTo(tx - (sin(rad)*5f).toFloat(), ty + (cos(rad)*5f).toFloat(), tx, ty)
                                            quadraticTo(rx, ry, sx, sy)
                                            close()
                                        }
                                        
                                        val colBrush = when (i) {
                                            0 -> Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF2979FF)))
                                            1 -> Brush.linearGradient(listOf(Color(0xFFFFEA00), Color(0xFFFF9100)))
                                            else -> Brush.linearGradient(listOf(Color(0xFFE040FB), Color(0xFF9013FE)))
                                        }
                                        drawPath(wp, colBrush)
                                    }
                                }
                                
                                // Golden center cap
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    radius = radiusScale * 0.28f,
                                    center = Offset(sx + 1f, sy + 2f)
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFFFD54F), Color(0xFFF57F17)),
                                        center = Offset(sx, sy),
                                        radius = radiusScale * 0.25f
                                    ),
                                    radius = radiusScale * 0.25f,
                                    center = Offset(sx, sy)
                                )
                            }
                        }

                        // Dynamic Floating scoring/damage combat numbers overlaid on top!
                        FloatingTextOverlays(floatingList)
                    }
                }
            }

            // HAMMER BOOSTER QUICK ACTIVATOR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Boosters panel title
                Text(text = "BOOSTERS", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)

                Button(
                    onClick = {
                        val sel = selectedCell
                        if (sel != null) {
                            viewModel.useHammerBoosterSelection(sel.first, sel.second)
                            selectedCell = null
                        } else {
                            viewModel.purchaseBooster("hammer", 150)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Build, contentDescription = "Hammer", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Hammer Booster (${state.getBoosterCount("hammer")})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // LEVEL VICTORY SCREEN POPUP
        if (isDone) {
            VictoryDialog(viewModel, score)
        }

        // GAME OUT OF MOVES / RETRY POPUP
        if (isOver) {
            LossDialog(viewModel, goals)
        }
    }
}

// Draw mini shapes during goals checklist
@Composable
fun drawBlockerShapeMini(type: ObstacleType) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val w = size.width
        val h = size.height
        
        when (type) {
            ObstacleType.CRATE -> {
                drawRect(Color(0xFF8D6E63), size = size)
                drawRect(Color(0xFF4E342E), size = size, style = Stroke(1.5.dp.toPx()))
                drawLine(Color(0xFF4E342E), Offset(0f, 0f), Offset(w, h), strokeWidth = 1.5.dp.toPx())
                drawLine(Color(0xFF4E342E), Offset(w, 0f), Offset(0f, h), strokeWidth = 1.5.dp.toPx())
            }
            ObstacleType.STONE -> {
                drawRect(Color(0xFF546E7A), size = size)
                drawRect(Color(0xFF263238), size = size, style = Stroke(1.5.dp.toPx()))
                drawLine(Color(0xFF263238), Offset(w * 0.2f, h * 0.3f), Offset(w * 0.8f, h * 0.7f), strokeWidth = 1.dp.toPx())
            }
            ObstacleType.ICE -> {
                drawCircle(Color(0xFF80DEEA), radius = r * 0.95f, center = center)
                drawCircle(Color.White, radius = r * 0.95f, center = center, style = Stroke(1.5.dp.toPx()))
            }
            ObstacleType.CHAIN -> {
                drawCircle(Color(0xFFB0BEC5), radius = r * 0.72f, center = center, style = Stroke(3.dp.toPx()))
                drawLine(Color(0xFF37474F), Offset(0f, 0f), Offset(w, h), strokeWidth = 2.dp.toPx())
                drawLine(Color(0xFF37474F), Offset(w, 0f), Offset(0f, h), strokeWidth = 2.dp.toPx())
            }
            ObstacleType.MAGIC_BARRIER -> {
                drawCircle(Color(0xFFE040FB), radius = r * 0.9f, center = center, style = Stroke(2.5.dp.toPx()))
                drawCircle(Color(0xFF4A148C).copy(alpha = 0.6f), radius = r * 0.75f, center = center)
            }
            ObstacleType.CHOCOLATE -> {
                drawCircle(Color(0xFF3E2723), radius = r * 0.85f, center = center)
            }
            ObstacleType.APPLE -> {
                drawCircle(Color(0xFFD50000), radius = r * 0.82f, center = center)
                drawCircle(Color(0xFF4CAF50), radius = r * 0.22f, center = Offset(center.x + r * 0.35f, center.y - r * 0.65f))
            }
            ObstacleType.CROWN -> {
                val cPath = Path().apply {
                    moveTo(center.x - r * 0.8f, center.y + r * 0.5f)
                    lineTo(center.x + r * 0.8f, center.y + r * 0.5f)
                    lineTo(center.x + r * 0.9f, center.y - r * 0.2f)
                    lineTo(center.x + r * 0.4f, center.y + r * 0.15f)
                    lineTo(center.x, center.y - r * 0.62f)
                    lineTo(center.x - r * 0.4f, center.y + r * 0.15f)
                    lineTo(center.x - r * 0.9f, center.y - r * 0.2f)
                    close()
                }
                drawPath(cPath, Color(0xFFFFD54F))
                drawPath(cPath, Color(0xFFE65100), style = Stroke(1.dp.toPx()))
            }
            ObstacleType.SHIELD -> {
                val sPath = Path().apply {
                    moveTo(center.x - r * 0.75f, center.y - r * 0.6f)
                    quadraticTo(center.x, center.y - r * 0.65f, center.x + r * 0.75f, center.y - r * 0.6f)
                    lineTo(center.x + r * 0.75f, center.y + r * 0.1f)
                    quadraticTo(center.x, center.y + r * 0.95f, center.x, center.y + r * 0.95f)
                    quadraticTo(center.x, center.y + r * 0.95f, center.x - r * 0.75f, center.y + r * 0.1f)
                    close()
                }
                drawPath(sPath, Color(0xFF0288D1))
                drawPath(sPath, Color(0xFFECEFF1), style = Stroke(1.5.dp.toPx()))
            }
            ObstacleType.CRYSTAL -> {
                val cryPath = Path().apply {
                    moveTo(center.x, center.y - r * 0.9f)
                    lineTo(center.x + r * 0.7f, center.y)
                    lineTo(center.x, center.y + r * 0.9f)
                    lineTo(center.x - r * 0.7f, center.y)
                    close()
                }
                drawPath(cryPath, Color(0xFF00E5FF))
                drawPath(cryPath, Color.White, style = Stroke(1.5.dp.toPx()))
            }
            ObstacleType.EGGS -> {
                val rBound = Rect(center.x - r * 0.55f, center.y - r * 0.8f, center.x + r * 0.55f, center.y + r * 0.75f)
                val eggPath = Path().apply { addOval(rBound) }
                drawPath(eggPath, Color(0xFFFFFDE7))
                drawPath(eggPath, Color(0xFF8D6E63), style = Stroke(1.dp.toPx()))
                drawCircle(Color(0xFF81C784), radius = r * 0.15f, center = Offset(center.x - r * 0.1f, center.y))
            }
            ObstacleType.BEE_HIVE -> {
                val yStep = r * 0.45f
                for (i in 0..2) {
                    val scaleX = 1f - (i * 0.22f)
                    drawRoundRect(
                        color = Color(0xFFFFB300),
                        topLeft = Offset(center.x - r * scaleX, center.y - r * 0.7f + i * yStep),
                        size = Size(r * 2 * scaleX, r * 0.42f),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun BossArenaPanel(
    name: String,
    hp: Long,
    maxHp: Long,
    phase: Int,
    countdown: Int,
    cinematicState: BossCinematic? = null,
    activity: BossActivity = BossActivity.IDLE,
    speech: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1033)),
        border = BorderStroke(2.dp, Color(0xFFFF1744).copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Box enclosing the Boss character and speech bubble
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.size(width = 75.dp, height = 90.dp)
            ) {
                // Drawn animated Giant Boss character on the Left
                GiantCandyBossCharacter(
                    phase = phase,
                    hp = hp,
                    cinematicState = cinematicState,
                    activity = activity
                )
                
                // Speech Bubble!
                if (!speech.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-5).dp)
                            .background(Color(0xFF33083E), RoundedCornerShape(8.dp))
                            .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = speech,
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Health details on the Right
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Name & phase
                    Column {
                        Text(text = "👑 $name", fontSize = 14.sp, color = Color(0xFFFF1744), fontWeight = FontWeight.Black)
                        Text(
                            text = when (phase) {
                                3 -> "Rage Phase 3 😡"
                                2 -> "Agile Phase 2 ⚡"
                                else -> "Standard Phase 1 🛡️"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // hp progress numbers
                    Text(text = "$hp / $maxHp HP", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // health bar progress (smoothed with dynamic animateFloatAsState!)
                val hpRatio = (hp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)
                val animatedRatio by animateFloatAsState(
                    targetValue = hpRatio,
                    animationSpec = tween(500, easing = FastOutSlowInEasing),
                    label = "boss_hp_deplete"
                )
                val animatedDelayedRatio by animateFloatAsState(
                    targetValue = hpRatio,
                    animationSpec = tween(1200, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
                    label = "boss_hp_delayed"
                )
                val hpColor = when {
                    hpRatio <= 0.3f -> Color.Red
                    hpRatio <= 0.7f -> Color(0xFFFF9100)
                    else -> Color(0xFF00FF66)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedDelayedRatio)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFFFD54F).copy(alpha = 0.8f), Color(0xFFFF1744))),
                                RoundedCornerShape(6.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedRatio)
                            .background(
                                Brush.linearGradient(listOf(hpColor.copy(alpha = 0.75f), hpColor)),
                                RoundedCornerShape(6.dp)
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Spawner Attack in $countdown moves",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun GiantCandyBossCharacter(
    phase: Int,
    hp: Long,
    cinematicState: BossCinematic?,
    activity: BossActivity = BossActivity.IDLE
) {
    // Dynamic animations based on phase and cinematic states
    val infiniteTransition = rememberInfiniteTransition(label = "boss_movement")
    
    // Smooth idle bounces & breaths
    val bounceSpeed = if (activity == BossActivity.LAUGH) 600 else if (phase == 3) 800 else 1200
    val idleOffsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(bounceSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Cape movement swaying
    val capeSwayAngle by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cape_sway"
    )

    // Weapon swaying
    val maceSwayAngle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mace_sway"
    )

    // Auto eye-blink animation
    val blinkScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                1f at 0
                1f at 3300
                0.05f at 3400 // rapid eye shut
                1f at 3500
            }
        ),
        label = "blink_scale"
    )

    // Cinematic custom positions / scaling
    val currentScale = when (cinematicState) {
        BossCinematic.INTRO_START -> 0f
        BossCinematic.INTRO_JUMP -> 1.5f
        BossCinematic.INTRO_ROAR -> 1.3f
        BossCinematic.OUTRO_DEATH_ROAR -> 1.4f
        BossCinematic.OUTRO_COLLAPSE -> 0.4f
        BossCinematic.OUTRO_BREAKING -> 0.7f
        BossCinematic.OUTRO_DISAPPEAR -> 0f
        else -> 1f
    } * breatheScale

    val rotationAngle = when (cinematicState) {
        BossCinematic.INTRO_JUMP -> 360f
        BossCinematic.OUTRO_DEATH_ROAR -> 15f
        BossCinematic.OUTRO_COLLAPSE -> 90f
        BossCinematic.OUTRO_BREAKING -> -30f
        else -> {
            when (activity) {
                BossActivity.TAUNT -> if (idleOffsetY > 0) 6f else -6f
                BossActivity.WEAPON_SLAM -> -15f
                else -> 0f
            }
        }
    }

    val finalOffsetY = idleOffsetY + when (cinematicState) {
        BossCinematic.INTRO_JUMP -> -20f
        BossCinematic.INTRO_IMPACT -> 15f
        BossCinematic.OUTRO_COLLAPSE -> 30f
        else -> {
            when (activity) {
                BossActivity.STOMP -> if (idleOffsetY > 0) 12f else -8f
                BossActivity.WEAPON_SLAM -> 10f
                else -> 0f
            }
        }
    }

    Box(
        modifier = Modifier
            .size(70.dp)
            .graphicsLayer(
                scaleX = currentScale,
                scaleY = currentScale,
                rotationZ = rotationAngle,
                translationY = finalOffsetY
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.width * 0.35f

            // 1. Draw Cape behind the monster
            rotate(degrees = capeSwayAngle, pivot = center) {
                val capePath = Path().apply {
                    moveTo(center.x - baseRadius * 0.5f, center.y + baseRadius * 0.2f)
                    lineTo(center.x - baseRadius * 1.5f, center.y + baseRadius * 1.6f)
                    lineTo(center.x + baseRadius * 1.5f, center.y + baseRadius * 1.6f)
                    lineTo(center.x + baseRadius * 0.5f, center.y + baseRadius * 0.2f)
                    close()
                }
                drawPath(
                    path = capePath,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF880E4F), Color(0xFF31001A))
                    )
                )
            }

            // 2. Rage aura if phase == 3
            if (phase == 3) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Red.copy(alpha = 0.45f), Color.Transparent)
                    ),
                    radius = baseRadius * 1.6f,
                    center = center
                )
            }

            // 3. Draw jelly candy monster body
            val bodyColor = when (phase) {
                3 -> Color(0xFFD50000) // Fierce Red for Rage Phase 3
                2 -> Color(0xFFFF9100) // Orange/Amber for Agile Phase 2
                else -> Color(0xFFE040FB) // Delicious grape purple for Standard Phase 1
            }

            // Body drop shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = baseRadius,
                center = center + Offset(3f, 4f)
            )

            // Jelly Body gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(bodyColor.copy(alpha = 0.6f), bodyColor)
                ),
                radius = baseRadius,
                center = center
            )

            // Glisten Highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.25f),
                radius = baseRadius * 0.4f,
                center = center - Offset(baseRadius * 0.3f, baseRadius * 0.3f)
            )

            // 4. Giant horns
            val hornPath = Path().apply {
                moveTo(center.x - baseRadius * 0.6f, center.y - baseRadius * 0.6f)
                quadraticTo(
                    center.x - baseRadius * 1.1f, center.y - baseRadius * 1.4f,
                    center.x - baseRadius * 0.4f, center.y - baseRadius * 0.9f
                )
                close()
            }
            drawPath(hornPath, color = Color(0xFFFFEA00)) // Golden star sugar horns!

            val rightHornPath = Path().apply {
                moveTo(center.x + baseRadius * 0.6f, center.y - baseRadius * 0.6f)
                quadraticTo(
                    center.x + baseRadius * 1.1f, center.y - baseRadius * 1.4f,
                    center.x + baseRadius * 0.4f, center.y - baseRadius * 0.9f
                )
                close()
            }
            drawPath(rightHornPath, color = Color(0xFFFFEA00))

            // 5. Draw Royal Mace weapon to the side of the boss
            rotate(degrees = maceSwayAngle, pivot = center + Offset(baseRadius * 1.0f, baseRadius * 0.3f)) {
                // Mace handle
                drawLine(
                    color = Color(0xFF795548),
                    start = center + Offset(baseRadius * 0.9f, baseRadius * 1.2f),
                    end = center + Offset(baseRadius * 0.9f, -baseRadius * 0.2f),
                    strokeWidth = 3.dp.toPx()
                )
                // Spiked chocolate sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFB300), Color(0xFF3E2723))
                    ),
                    radius = baseRadius * 0.35f,
                    center = center + Offset(baseRadius * 0.9f, -baseRadius * 0.2f)
                )
            }

            // 6. Funny expressive cartoon eyes
            val leftEyeCenter = center - Offset(baseRadius * 0.35f, baseRadius * 0.1f)
            val rightEyeCenter = center + Offset(baseRadius * 0.35f, -baseRadius * 0.1f)

            // Sclera height scaled by blink state
            val scleraRadiusX = baseRadius * 0.22f
            val scleraRadiusY = baseRadius * 0.22f * blinkScale

            // Draw white sclera
            drawOval(
                color = Color.White,
                topLeft = Offset(leftEyeCenter.x - scleraRadiusX, leftEyeCenter.y - scleraRadiusY),
                size = Size(scleraRadiusX * 2, scleraRadiusY * 2)
            )
            drawOval(
                color = Color.White,
                topLeft = Offset(rightEyeCenter.x - scleraRadiusX, rightEyeCenter.y - scleraRadiusY),
                size = Size(scleraRadiusX * 2, scleraRadiusY * 2)
            )

            // Pupils
            val eyeLookOffset = when (cinematicState) {
                BossCinematic.INTRO_ROAR, BossCinematic.OUTRO_DEATH_ROAR -> Offset(0f, -3f)
                BossCinematic.OUTRO_COLLAPSE -> Offset(2f, 3f)
                else -> {
                    when (activity) {
                        BossActivity.ROAR -> Offset(0f, -4f)
                        BossActivity.LAUGH -> Offset(1f, 1f)
                        else -> Offset(0f, 0f)
                    }
                }
            }
            drawCircle(color = Color(0xFF120822), radius = baseRadius * 0.11f, center = leftEyeCenter + eyeLookOffset)
            drawCircle(color = Color(0xFF120822), radius = baseRadius * 0.11f, center = rightEyeCenter + eyeLookOffset)

            // Eye highlights
            drawCircle(color = Color.White, radius = baseRadius * 0.04f, center = leftEyeCenter + eyeLookOffset - Offset(2f, 2f))
            drawCircle(color = Color.White, radius = baseRadius * 0.04f, center = rightEyeCenter + eyeLookOffset - Offset(2f, 2f))

            // 7. Cheerful/Scary Mouth
            val chewingOffset = if (idleOffsetY > 0) baseRadius * 0.08f else 0f
            val mouthHeightScale = when (activity) {
                BossActivity.ROAR -> 0.85f
                BossActivity.LAUGH -> 0.72f
                else -> if (phase == 3) 0.6f else 0.48f
            }
            
            val mouthPath = Path().apply {
                moveTo(center.x - baseRadius * 0.4f, center.y + baseRadius * 0.2f)
                quadraticTo(
                    center.x, center.y + baseRadius * mouthHeightScale + chewingOffset,
                    center.x + baseRadius * 0.4f, center.y + baseRadius * 0.2f
                )
                close()
            }
            drawPath(mouthPath, color = Color(0xFF000000).copy(alpha = 0.85f))

            // Fangs
            val toothPath1 = Path().apply {
                moveTo(center.x - baseRadius * 0.25f, center.y + baseRadius * 0.21f)
                lineTo(center.x - baseRadius * 0.15f, center.y + baseRadius * 0.35f)
                lineTo(center.x - baseRadius * 0.05f, center.y + baseRadius * 0.21f)
                close()
            }
            drawPath(toothPath1, color = Color.White)

            val toothPath2 = Path().apply {
                moveTo(center.x + baseRadius * 0.05f, center.y + baseRadius * 0.21f)
                lineTo(center.x + baseRadius * 0.15f, center.y + baseRadius * 0.35f)
                lineTo(center.x + baseRadius * 0.25f, center.y + baseRadius * 0.21f)
                close()
            }
            drawPath(toothPath2, color = Color.White)
        }
    }
}

@Composable
fun GridComposablesBoard(
    board: Array<Array<CandyItem?>>,
    obstacles: Map<Pair<Int, Int>, ObstacleType>,
    obstacleDurability: Map<Pair<Int, Int>, Int>,
    explodingCandies: List<ExplodingCandy>,
    selectedCell: Pair<Int, Int>?,
    onCellSelect: (Int, Int) -> Unit,
    onSwipe: (Int, Int, String) -> Unit,
    obstacleAnimations: List<ObstacleAnimationEvent> = emptyList(),
    hintHighlightCandies: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null
) {
    // Pulse animation for hint cells
    val hintTransition = rememberInfiniteTransition(label = "hint_glow")
    val hintGlowAlpha by hintTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_alpha"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        for (r in 0 until 8) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (c in 0 until 8) {
                    val item = board[r][c]
                    val isSelected = selectedCell?.first == r && selectedCell?.second == c
                    val cellKey = Pair(r, c)
                    val isHintCell = hintHighlightCandies != null && (hintHighlightCandies.first == cellKey || hintHighlightCandies.second == cellKey)
                    val obstacle = obstacles[cellKey] ?: ObstacleType.NONE
                    val durability = obstacleDurability[cellKey] ?: 1

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(2.dp)
                            .border(
                                width = if (isSelected) 3.dp else if (isHintCell) 3.dp else 0.dp,
                                color = if (isSelected) Color.White else if (isHintCell) Color(0xFFFFD54F).copy(alpha = hintGlowAlpha) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .pointerInput(r, c) {
                                detectDragGestures(
                                    onDragStart = { },
                                    onDragEnd = { },
                                    onDragCancel = { },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val (dx, dy) = dragAmount
                                        if (kotlin.math.abs(dx) > 30f && kotlin.math.abs(dy) < 15f) {
                                            onSwipe(r, c, if (dx > 0) "RIGHT" else "LEFT")
                                        } else if (kotlin.math.abs(dy) > 30f && kotlin.math.abs(dx) < 15f) {
                                            onSwipe(r, c, if (dy > 0) "DOWN" else "UP")
                                        }
                                    }
                                )
                            }
                            .clickable(
                                onClick = { onCellSelect(r, c) },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    ) {
                        // Render Core Candy if present
                        if (item != null) {
                            CandyIcon(
                                type = item.type,
                                special = item.special,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Render explosion effect if cell is in active explodingCandies set
                            val explodingCandy = explodingCandies.firstOrNull { it.r == r && it.c == c }
                            if (explodingCandy != null) {
                                CandyExplosionEffect(
                                    type = explodingCandy.type,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Overlay Obstacle renders
                        if (obstacle != ObstacleType.NONE) {
                            RenderObstacleGraphic(obstacle, durability)
                        }

                        // Overlay active obstacle hit/break animations if current cell matches!
                        val cellAnims = obstacleAnimations.filter { it.r == r && it.c == c }
                        for (anim in cellAnims) {
                            ObstacleExplosionEffect(
                                type = anim.type,
                                isDestroy = anim.isDestroy,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

// Draws obstacles overlays
@Composable
fun RenderObstacleGraphic(type: ObstacleType, durability: Int = 1) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val w = size.width
        val h = size.height
        
        when (type) {
            ObstacleType.CRATE -> {
                // Brown wooden box background
                drawRect(Color(0xFF8D6E63), size = size)
                
                // Border frame
                drawRect(Color(0xFF4E342E), size = size, style = Stroke(3.dp.toPx()))
                
                // Metal brackets on corners!
                val bSize = size.minDimension * 0.25f
                // Top-Left
                drawRect(Color(0xFF78909C), topLeft = Offset(0f, 0f), size = Size(bSize, bSize))
                drawRect(Color(0xFF37474F), topLeft = Offset(0f, 0f), size = Size(bSize, bSize), style = Stroke(1.dp.toPx()))
                // Top-Right
                drawRect(Color(0xFF78909C), topLeft = Offset(w - bSize, 0f), size = Size(bSize, bSize))
                drawRect(Color(0xFF37474F), topLeft = Offset(w - bSize, 0f), size = Size(bSize, bSize), style = Stroke(1.dp.toPx()))
                // Bottom-Left
                drawRect(Color(0xFF78909C), topLeft = Offset(0f, h - bSize), size = Size(bSize, bSize))
                drawRect(Color(0xFF37474F), topLeft = Offset(0f, h - bSize), size = Size(bSize, bSize), style = Stroke(1.dp.toPx()))
                // Bottom-Right
                drawRect(Color(0xFF78909C), topLeft = Offset(w - bSize, h - bSize), size = Size(bSize, bSize))
                drawRect(Color(0xFF37474F), topLeft = Offset(w - bSize, h - bSize), size = Size(bSize, bSize), style = Stroke(1.dp.toPx()))

                // Wood grain brace (X brace)
                drawLine(Color(0xFF4E342E), Offset(0f, 0f), Offset(w, h), strokeWidth = 3.dp.toPx())
                if (durability >= 2) {
                    drawLine(Color(0xFF4E342E), Offset(w, 0f), Offset(0f, h), strokeWidth = 3.dp.toPx())
                }
                
                // Cracks depending on durability (starts with 3 durability. 2 shows small cracks, 1 shows increased crack texture)
                if (durability == 2) {
                    drawLine(Color(0xFF3E2723), Offset(w * 0.2f, h * 0.3f), Offset(w * 0.5f, h * 0.45f), strokeWidth = 2.dp.toPx())
                    drawLine(Color(0xFF3E2723), Offset(w * 0.5f, h * 0.45f), Offset(w * 0.4f, h * 0.7f), strokeWidth = 2.dp.toPx())
                }
                if (durability == 1) {
                    drawLine(Color(0xFF3E2723), Offset(w * 0.2f, h * 0.3f), Offset(w * 0.5f, h * 0.45f), strokeWidth = 2.5.dp.toPx())
                    drawLine(Color(0xFF3E2723), Offset(w * 0.5f, h * 0.45f), Offset(w * 0.4f, h * 0.7f), strokeWidth = 2.5.dp.toPx())
                    drawLine(Color(0xFF3E2723), Offset(w * 0.7f, h * 0.2f), Offset(w * 0.8f, h * 0.6f), strokeWidth = 2.dp.toPx())
                    drawLine(Color(0xFF3E2723), Offset(w * 0.15f, h * 0.8f), Offset(w * 0.5f, h * 0.85f), strokeWidth = 1.5.dp.toPx())
                }

                // OVERLAY CHAINS ON DURABILITY 3!
                if (durability == 3) {
                    drawCircle(Color(0xB3ECEFF1), radius = r * 0.62f, center = center, style = Stroke(4.5.dp.toPx()))
                    drawCircle(Color(0xFF37474F), radius = r * 0.62f, center = center, style = Stroke(1.5.dp.toPx()))
                    
                    drawLine(Color(0xB3ECEFF1), Offset(0f, 0f), Offset(w, h), strokeWidth = 4.5.dp.toPx())
                    drawLine(Color(0xFF37474F), Offset(0f, 0f), Offset(w, h), strokeWidth = 1.5.dp.toPx())
                    
                    drawLine(Color(0xB3ECEFF1), Offset(w, 0f), Offset(0f, h), strokeWidth = 4.5.dp.toPx())
                    drawLine(Color(0xFF37474F), Offset(w, 0f), Offset(0f, h), strokeWidth = 1.5.dp.toPx())
                }
            }
            ObstacleType.STONE -> {
                // Gray heavy rock
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFB0BEC5), Color(0xFF546E7A)),
                        center = center,
                        radius = r * 1.5f
                    ),
                    size = size
                )
                // Stone outer blocks frame
                drawRect(Color(0xFF37474F), size = size, style = Stroke(3.dp.toPx()))
                
                // Solid cracks lines based on durability
                if (durability <= 2) {
                    drawLine(Color(0xFF263238), Offset(w * 0.1f, h * 0.5f), Offset(w * 0.4f, h * 0.35f), strokeWidth = 2.dp.toPx())
                    drawLine(Color(0xFF263238), Offset(w * 0.4f, h * 0.35f), Offset(w * 0.9f, h * 0.6f), strokeWidth = 2.5.dp.toPx())
                }
                if (durability == 1) {
                    drawLine(Color(0xFF263238), Offset(w * 0.5f, h * 0.1f), Offset(w * 0.3f, h * 0.8f), strokeWidth = 2.dp.toPx())
                }
            }
            ObstacleType.ICE -> {
                // Shiny freezing frost crystals translucent cover
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xE6E0F7FA), Color(0xAA4DD0E1)),
                        center = center,
                        radius = size.width * 0.7f
                    ),
                    size = size
                )
                drawRect(Color.White.copy(alpha = 0.8f), size = size, style = Stroke(2.5.dp.toPx()))
                
                // Shine line
                drawLine(Color.White.copy(alpha = 0.6f), Offset(w * 0.15f, h * 0.15f), Offset(w * 0.7f, h * 0.7f), strokeWidth = 4.dp.toPx())
                
                if (durability == 1) {
                    // Ice spiderweb cracks
                    drawLine(Color.White, Offset(w * 0.3f, h * 0.3f), Offset(w * 0.5f, h * 0.5f), strokeWidth = 1.5.dp.toPx())
                    drawLine(Color.White, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.7f, h * 0.4f), strokeWidth = 1.5.dp.toPx())
                    drawLine(Color.White, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.4f, h * 0.8f), strokeWidth = 1.5.dp.toPx())
                }
            }
            ObstacleType.CHAIN -> {
                // Crossing metal chains
                drawCircle(Color(0xB3ECEFF1), radius = r * 0.62f, center = center, style = Stroke(4.5.dp.toPx()))
                drawCircle(Color(0xFF37474F), radius = r * 0.62f, center = center, style = Stroke(1.5.dp.toPx()))
                
                drawLine(Color(0xB3ECEFF1), Offset(0f, 0f), Offset(w, h), strokeWidth = 4.5.dp.toPx())
                drawLine(Color(0xFF37474F), Offset(0f, 0f), Offset(w, h), strokeWidth = 1.5.dp.toPx())
                
                drawLine(Color(0xB3ECEFF1), Offset(w, 0f), Offset(0f, h), strokeWidth = 4.5.dp.toPx())
                drawLine(Color(0xFF37474F), Offset(w, 0f), Offset(0f, h), strokeWidth = 1.5.dp.toPx())
            }
            ObstacleType.MAGIC_BARRIER -> {
                // Purple rune pulse barrier
                drawCircle(Color(0xFF4A148C).copy(alpha = 0.5f), radius = r * 0.95f, center = center)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xE1E040FB), Color(0xAA4A148C)),
                        center = center,
                        radius = r * 0.9f
                    ),
                    radius = r * 0.85f,
                    center = center
                )
                drawCircle(Color(0xFFE040FB), radius = r * 0.88f, center = center, style = Stroke(2.5.dp.toPx()))
                
                // Draw inner magic cross rune star
                drawLine(Color(0xFFE040FB).copy(alpha = 0.7f), Offset(center.x - r * 0.5f, center.y), Offset(center.x + r * 0.5f, center.y), strokeWidth = 2.dp.toPx())
                drawLine(Color(0xFFE040FB).copy(alpha = 0.7f), Offset(center.x, center.y - r * 0.5f), Offset(center.x, center.y + r * 0.5f), strokeWidth = 2.dp.toPx())
            }
            ObstacleType.CHOCOLATE -> {
                // Liquid brown cocoa sludge
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF8D6E63), Color(0xFF3E2723)),
                        center = center,
                        radius = r * 1.3f
                    ),
                    size = size
                )
                // Outer gooey drips outline
                drawCircle(Color(0xFF3E2723), radius = r * 0.78f, center = Offset(center.x - 4f, center.y - 4f))
            }
            ObstacleType.APPLE -> {
                // Red Apple with stem and glossy highlights
                drawCircle(Color(0xFFD50000), radius = r * 0.75f, center = center)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF8A80), Color(0xFFD50000)),
                        center = Offset(center.x - r * 0.15f, center.y - r * 0.15f),
                        radius = r * 0.6f
                    ),
                    radius = r * 0.7f,
                    center = center
                )
                // Brown Stem
                drawLine(Color(0xFF5D4037), Offset(center.x, center.y - r * 0.7f), Offset(center.x + r * 0.25f, center.y - r * 1.05f), strokeWidth = 3.dp.toPx())
                // Leaf
                val leafPath = Path().apply {
                    moveTo(center.x + r * 0.1f, center.y - r * 0.85f)
                    quadraticTo(center.x + r * 0.6f, center.y - r * 1.0f, center.x + r * 0.45f, center.y - r * 0.65f)
                    close()
                }
                drawPath(leafPath, Color(0xFF4CAF50))
            }
            ObstacleType.CROWN -> {
                // Regallian Gold Crown
                val cPath = Path().apply {
                    moveTo(center.x - r * 0.7f, center.y + r * 0.6f)
                    lineTo(center.x + r * 0.7f, center.y + r * 0.6f)
                    lineTo(center.x + r * 0.85f, center.y - r * 0.2f)
                    lineTo(center.x + r * 0.45f, center.y + r * 0.15f)
                    lineTo(center.x, center.y - r * 0.7f)
                    lineTo(center.x - r * 0.45f, center.y + r * 0.15f)
                    lineTo(center.x - r * 0.85f, center.y - r * 0.2f)
                    close()
                }
                drawPath(
                    cPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFEA00), Color(0xFFFF9100))
                    )
                )
                drawPath(cPath, Color(0xFFE65100), style = Stroke(2.dp.toPx()))
                // Tip pearls
                drawCircle(Color.White, radius = r * 0.12f, center = Offset(center.x, center.y - r * 0.7f))
                drawCircle(Color.White, radius = r * 0.1f, center = Offset(center.x - r * 0.85f, center.y - r * 0.2f))
                drawCircle(Color.White, radius = r * 0.1f, center = Offset(center.x + r * 0.85f, center.y - r * 0.2f))

                // Rubies
                drawCircle(Color.Red, radius = r * 0.08f, center = Offset(center.x, center.y + r * 0.35f))
            }
            ObstacleType.SHIELD -> {
                // Guardian Blue Shield
                val sPath = Path().apply {
                    moveTo(center.x - r * 0.7f, center.y - r * 0.6f)
                    quadraticTo(center.x, center.y - r * 0.65f, center.x + r * 0.7f, center.y - r * 0.6f)
                    lineTo(center.x + r * 0.7f, center.y + r * 0.1f)
                    quadraticTo(center.x + r * 0.6f, center.y + r * 0.75f, center.x, center.y + r * 0.95f)
                    quadraticTo(center.x - r * 0.6f, center.y + r * 0.75f, center.x - r * 0.7f, center.y + r * 0.1f)
                    close()
                }
                drawPath(
                    sPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF29B6F6), Color(0xFF0288D1))
                    )
                )
                drawPath(sPath, Color(0xFFECEFF1), style = Stroke(2.5.dp.toPx()))
                
                // Silver Lining
                drawLine(Color(0xFFECEFF1), Offset(center.x, center.y - r * 0.6f), Offset(center.x, center.y + r * 0.95f), strokeWidth = 2.dp.toPx())
            }
            ObstacleType.CRYSTAL -> {
                // Sparkling Cyan Crystal
                val cryPath = Path().apply {
                    moveTo(center.x, center.y - r * 0.8f)
                    lineTo(center.x + r * 0.65f, center.y - r * 0.2f)
                    lineTo(center.x + r * 0.45f, center.y + r * 0.75f)
                    lineTo(center.x - r * 0.45f, center.y + r * 0.75f)
                    lineTo(center.x - r * 0.65f, center.y - r * 0.2f)
                    close()
                }
                drawPath(
                    cryPath,
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF)),
                        center = center,
                        radius = r * 0.9f
                    )
                )
                drawPath(cryPath, Color.White, style = Stroke(2.dp.toPx()))
                // Facet highlights lines
                drawLine(Color.White.copy(alpha = 0.6f), center, Offset(center.x, center.y - r * 0.8f), strokeWidth = 1.dp.toPx())
                drawLine(Color.White.copy(alpha = 0.6f), center, Offset(center.x + r * 0.65f, center.y - r * 0.2f), strokeWidth = 1.dp.toPx())
                drawLine(Color.White.copy(alpha = 0.6f), center, Offset(center.x - r * 0.65f, center.y - r * 0.2f), strokeWidth = 1.dp.toPx())
            }
            ObstacleType.EGGS -> {
                // Ivory white egg
                val rBound = Rect(center.x - r * 0.65f, center.y - r * 0.88f, center.x + r * 0.65f, center.y + r * 0.8f)
                val eggPath = Path().apply {
                    addOval(rBound)
                }
                drawPath(eggPath, Color(0xFFFFFDE7))
                drawPath(eggPath, Color(0xFF8D6E63), style = Stroke(1.5.dp.toPx()))
                
                // Spots on egg
                drawCircle(Color(0xFF81C784), radius = r * 0.12f, center = Offset(center.x - r * 0.24f, center.y))
                drawCircle(Color(0xFF81C784), radius = r * 0.12f, center = Offset(center.x + r * 0.24f, center.y - r * 0.24f))
                
                // Cracks depending on durability
                if (durability == 1) {
                    drawLine(Color(0xFF4E342E), Offset(center.x - r * 0.1f, center.y - r * 0.4f), Offset(center.x + r * 0.15f, center.y), strokeWidth = 2.dp.toPx())
                    drawLine(Color(0xFF4E342E), Offset(center.x + r * 0.15f, center.y), Offset(center.x - r * 0.2f, center.y + r * 0.35f), strokeWidth = 2.dp.toPx())
                }
            }
            ObstacleType.BEE_HIVE -> {
                // Golden tiered bee hive
                val yStep = r * 0.38f
                for (i in 0..3) {
                    val scaleX = 1f - (i * 0.15f)
                    drawRoundRect(
                        color = Color(0xFFFFB300),
                        topLeft = Offset(center.x - r * scaleX, center.y - r * 0.8f + i * yStep),
                        size = Size(r * 2 * scaleX, r * 0.38f),
                        cornerRadius = CornerRadius(r * 0.15f, r * 0.15f)
                    )
                    drawRoundRect(
                        color = Color(0xFFE65100),
                        topLeft = Offset(center.x - r * scaleX, center.y - r * 0.8f + i * yStep),
                        size = Size(r * 2 * scaleX, r * 0.38f),
                        cornerRadius = CornerRadius(r * 0.15f, r * 0.15f),
                        style = Stroke(1.5.dp.toPx())
                    )
                }
                // Honey entry doorway
                drawCircle(Color(0xFF3E2723), radius = r * 0.25f, center = Offset(center.x, center.y + r * 0.1f))
            }
            else -> {}
        }
    }
}

// Juicy physics-simulated particle explosion for hit and broken obstacles/blockers
@Composable
fun ObstacleExplosionEffect(
    type: ObstacleType,
    isDestroy: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = LinearOutSlowInEasing)
        )
    }

    val p = progress.value
    if (p >= 1f) return

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f
        
        when (type) {
            ObstacleType.CRATE -> {
                // Splintering wood pieces & sawdust
                val numChips = if (isDestroy) 10 else 4
                for (i in 0 until numChips) {
                    val angle = (i * 360f / numChips) + (p * 90f)
                    val rad = Math.toRadians(angle.toDouble())
                    // Pop speed
                    val pushDist = maxRadius * (p * (if (isDestroy) 2.2f else 1.1f))
                    val px = center.x + (cos(rad) * pushDist).toFloat()
                    val py = center.y + (sin(rad) * pushDist).toFloat() + (p * p * maxRadius * 3.5f) // Gravity pull
                    
                    val pSize = maxRadius * 0.28f * (1f - p)
                    val shadowOffset = 3.dp.toPx() * (1f - p)
                    // Draw shadow
                    drawRect(
                        color = Color.Black.copy(alpha = 0.3f * (1f - p)),
                        topLeft = Offset(px - pSize/2 + shadowOffset, py - pSize/2 + shadowOffset),
                        size = Size(pSize, pSize)
                    )
                    // Draw wooden splinter
                    drawRect(
                        color = Color(0xFF5D4037),
                        topLeft = Offset(px - pSize/2, py - pSize/2),
                        size = Size(pSize, pSize)
                    )
                }
            }
            ObstacleType.STONE -> {
                // Stone chunks
                val numRocks = if (isDestroy) 8 else 3
                for (i in 0 until numRocks) {
                    val angle = (i * 360f / numRocks) - (p * 45f)
                    val rad = Math.toRadians(angle.toDouble())
                    val pushDist = maxRadius * (p * (if (isDestroy) 2.0f else 0.9f))
                    val px = center.x + (cos(rad) * pushDist).toFloat()
                    val py = center.y + (sin(rad) * pushDist).toFloat() + (p * p * maxRadius * 4.0f) // gravity
                    
                    val pRadius = maxRadius * 0.24f * (1f - p)
                    val shadowOffset = 4.dp.toPx() * (1f - p)
                    // Shadow
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.35f * (1f - p)),
                        radius = pRadius,
                        center = Offset(px + shadowOffset, py + shadowOffset)
                    )
                    // Gray chunk
                    drawCircle(
                        color = Color(0xFF78909C),
                        radius = pRadius,
                        center = Offset(px, py)
                    )
                }
            }
            ObstacleType.ICE -> {
                // Shimmering ice shards
                val numShards = if (isDestroy) 12 else 5
                for (i in 0 until numShards) {
                    val angle = i * (360f / numShards) + (p * 180f)
                    val rad = Math.toRadians(angle.toDouble())
                    val pushDist = maxRadius * (p * (if (isDestroy) 2.5f else 1.3f))
                    val px = center.x + (cos(rad) * pushDist).toFloat()
                    val py = center.y + (sin(rad) * pushDist).toFloat() + (p * p * maxRadius * 2.8f) // gravity
                    
                    val pRadius = maxRadius * 0.2f * (1f - p)
                    drawCircle(
                        color = Color(0xE0E0F7FA).copy(alpha = 1f - p),
                        radius = pRadius,
                        center = Offset(px, py)
                    )
                }
            }
            ObstacleType.CHAIN -> {
                // Snapping chain links flying left and right!
                val numLinks = if (isDestroy) 6 else 2
                for (i in 0 until numLinks) {
                    val direction = if (i % 2 == 0) -1f else 1f
                    val angle = i * 60f + p * 120f
                    val rad = Math.toRadians(angle.toDouble())
                    val vx = direction * maxRadius * 2.2f * p
                    val vy = -maxRadius * (1f - p * 0.5f) * p + (p * p * maxRadius * 3.5f)
                    
                    val px = center.x + vx
                    val py = center.y + vy
                    
                    val pRadius = maxRadius * 0.22f * (1f - p)
                    drawCircle(
                        color = Color(0xFFECEFF1).copy(alpha = 1f - p),
                        radius = pRadius,
                        center = Offset(px, py),
                        style = Stroke(2.dp.toPx())
                    )
                }
            }
            ObstacleType.MAGIC_BARRIER -> {
                // Purple rune explosion
                drawCircle(
                    color = Color(0xFFE040FB).copy(alpha = 0.8f * (1f - p)),
                    radius = maxRadius * (1f + p * 1.5f),
                    center = center,
                    style = Stroke(3.dp.toPx() * (1f - p))
                )
                // Spark particles
                val numSparks = 10
                for (i in 0 until numSparks) {
                    val angle = i * (360f / numSparks)
                    val rad = Math.toRadians(angle.toDouble())
                    val pushDist = maxRadius * 1.8f * p
                    val px = center.x + (cos(rad) * pushDist).toFloat()
                    val py = center.y + (sin(rad) * pushDist).toFloat()
                    drawCircle(Color(0xFFFF00FF), radius = 3.dp.toPx() * (1f - p), center = Offset(px, py))
                }
            }
            ObstacleType.APPLE, ObstacleType.CROWN, ObstacleType.SHIELD, ObstacleType.CRYSTAL -> {
                // Upward pop and rapid lifting off board
                val px = center.x
                val py = center.y - (p * maxRadius * 4f)
                val targetRadius = maxRadius * (1f + p * 0.5f)
                val alpha = (1f - p)
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = alpha * 0.7f), Color.Transparent),
                        center = Offset(px, py),
                        radius = targetRadius * 1.5f
                    ),
                    radius = targetRadius * 1.5f,
                    center = Offset(px, py)
                )
            }
            else -> {
                // Standard match pop bubble
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f * (1f - p)),
                    radius = maxRadius * (1f + p * 1.2f),
                    center = center,
                    style = Stroke(2.dp.toPx())
                )
            }
        }
    }
}

// Coordinates overlapping popup combat numbers
@Composable
fun FloatingTextOverlays(list: List<FloatingText>) {
    val configuration = LocalConfiguration.current
    val w = (configuration.screenWidthDp - 20).dp / 8f // approx width of cell
    
    for (f in list) {
        // Floating upward effect animation
        val animOffset = remember { Animatable(0f) }
        val alphaState = remember { Animatable(1f) }

        LaunchedEffect(key1 = f.id) {
            launch {
                animOffset.animateTo(
                    targetValue = -35f,
                    animationSpec = tween(1000, easing = LinearOutSlowInEasing)
                )
            }
            launch {
                alphaState.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(1000, easing = FastOutLinearInEasing)
                )
            }
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (f.c * w.value * 2.2f).roundToInt(),
                        y = (f.r * w.value * 2.2f + animOffset.value).roundToInt()
                    )
                }
                .graphicsLayer(alpha = alphaState.value)
        ) {
            Text(
                text = f.text,
                fontSize = if (f.isDamage) 22.sp else 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(android.graphics.Color.parseColor(f.colorHex)),
                style = MaterialTheme.typography.bodyLarge.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

// 5. SUCCESS VICTORY POPUP
@Composable
fun VictoryDialog(viewModel: GameViewModel, score: Long) {
    val stars by viewModel.starsAchieved.collectAsState()

    Dialog(
        onDismissRequest = { viewModel.navigateTo(GameScreen.Map) },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ConfettiShower()

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1033)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                border = BorderStroke(3.dp, Color(0xFFFFD54F))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LEVEL COMPLETE!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD54F)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stars rating animations
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            val active = i <= stars
                            val tint = if (active) Color(0xFFFFD54F) else Color(0xFF424242)
                            
                            var visibleState by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(i * 300L)
                                visibleState = true
                            }
                            
                            val scale by animateFloatAsState(
                                targetValue = if (visibleState && active) 1.35f else if (visibleState) 1f else 0f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "star_pop"
                            )
                            
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Star",
                                tint = tint,
                                modifier = Modifier
                                    .size(56.dp)
                                    .scale(scale)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Your Score: $score",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Bonus Rewards Unlocked!",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reward Chest layout
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = "🎁", fontSize = 32.sp)
                        Column {
                            Text("+150 Gold Coins", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("+5 Power Gems", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.navigateTo(GameScreen.Map) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CONTINUE", color = Color(0xFF3F2B00), fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ConfettiShower() {
    val state = remember {
        List(45) {
            ConfettiParticleState(
                x = kotlin.random.Random.nextFloat() * 400f,
                y = -(50f + kotlin.random.Random.nextFloat() * 300f),
                speedY = 5f + kotlin.random.Random.nextFloat() * 7f,
                rotation = kotlin.random.Random.nextFloat() * 360f,
                rotationSpeed = -5f + kotlin.random.Random.nextFloat() * 10f,
                color = listOf(
                    Color(0xFFFF1744), Color(0xFF00E676), Color(0xFFFFD54F),
                    Color(0xFF29B6F6), Color(0xFFE040FB), Color(0xFFFF9100)
                ).random(),
                shapeType = (0..2).random()
            )
        }
    }

    var triggerTick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16)
            for (p in state) {
                p.y += p.speedY
                p.rotation += p.rotationSpeed
                if (p.y > 800f) {
                    p.y = -50f
                    p.x = kotlin.random.Random.nextFloat() * 400f
                }
            }
            triggerTick++
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        triggerTick.let { 
            for (p in state) {
                rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                    when (p.shapeType) {
                        0 -> drawCircle(color = p.color, radius = 6.dp.toPx(), center = Offset(p.x, p.y))
                        1 -> drawRect(color = p.color, topLeft = Offset(p.x - 5.dp.toPx(), p.y - 5.dp.toPx()), size = Size(10.dp.toPx(), 10.dp.toPx()))
                        else -> {
                            val path = Path().apply {
                                moveTo(p.x, p.y - 6.dp.toPx())
                                lineTo(p.x - 6.dp.toPx(), p.y + 6.dp.toPx())
                                lineTo(p.x + 6.dp.toPx(), p.y + 6.dp.toPx())
                                close()
                            }
                            drawPath(path, color = p.color)
                        }
                    }
                }
            }
        }
    }
}

class ConfettiParticleState(
    var x: Float,
    var y: Float,
    val speedY: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val shapeType: Int
)

// 6. OUT OF MOVES LOSS POPUP
@Composable
fun LossDialog(viewModel: GameViewModel, goals: List<LevelGoal>) {
    Dialog(
        onDismissRequest = { viewModel.navigateTo(GameScreen.Map) },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF331010)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            border = BorderStroke(3.dp, Color(0xFFFF1744))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "OUT OF MOVES!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF1744)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "So close! Try using 250 coins to buy 5 more moves and keep your progress alive!",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.buyExtraMoves() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    GoldCoinIcon(Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("BUY 5 MOVES (250 Coins)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { viewModel.navigateTo(GameScreen.Map) }
                ) {
                    Text("Exit to Map", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ======================== BRAND NEW 3D & RETENTIVE UI SYSTEMS ========================

@Composable
fun Button3D(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFFFD54F),
    bottomColor: Color = Color(0xFFC7A500),
    content: @Composable RowScope.() -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val offset by animateDpAsState(targetValue = if (pressed) 4.dp else 0.dp, label = "btn_offset")

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            pressed = false
                            onClick()
                        }
                    }
                )
            }
            .background(bottomColor, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .offset(y = (-4).dp + offset)
                .fillMaxSize()
                .background(containerColor, RoundedCornerShape(16.dp))
                .border(1.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
fun LoginRegisterScreen(viewModel: GameViewModel) {
    var isSignUp by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var selectEmoji by remember { mutableStateOf("CR") }
    var selectColor by remember { mutableStateOf("#E91E63") }
    var selectCountry by remember { mutableStateOf("US") }
    var customPicUri by remember { mutableStateOf("") }

    val emojis = listOf("CR", "SH", "DK", "KT", "LD", "QN", "KG")
    val colors = listOf("#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#009688", "#4CAF50", "#FFC107", "#FF9800", "#FF5722")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF200F21), Color(0xFF14071D), Color(0xFF09000D))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFE91E63).copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0A21).copy(alpha = 0.85f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "SWEET ACCOUNT",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD54F)
                )
                Text(
                    text = "Save your levels & stats securely!",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Tab selectors (3D like clickable tabs)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (!isSignUp) Color(0xFFE91E63) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { isSignUp = false; errorMsg = ""; SoundManager.playSoftClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Log In", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (isSignUp) Color(0xFFE91E63) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { isSignUp = true; errorMsg = ""; SoundManager.playSoftClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sign Up", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Input fields
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFE91E63),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFE91E63),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isSignUp) {
                    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                    ) { uri: android.net.Uri? ->
                        if (uri != null) {
                            customPicUri = uri.toString()
                            SoundManager.playRewardCollection()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "CHOOSE YOUR AVATAR / UPLOAD PICTURE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Custom avatar picture picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        Color(android.graphics.Color.parseColor(selectColor)),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (customPicUri.isNotEmpty()) {
                                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().clip(CircleShape)) {
                                        coil.compose.AsyncImage(
                                            model = customPicUri,
                                            contentDescription = "Uploaded Avatar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    Text(getSafeAvatarLabel(selectEmoji), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (customPicUri.isNotEmpty()) "Custom Photo Loaded" else "Using Royal Avatar",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap to upload custom camera image",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Button(
                            onClick = { launcher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("UPLOAD 📸", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Emojis row
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(emojis) { em ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (selectEmoji == em && customPicUri.isEmpty()) Color(0xFFE91E63) else Color.Black.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                    .clickable { 
                                        selectEmoji = em
                                        customPicUri = "" // discard custom logic
                                        SoundManager.playSoftClick() 
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(getSafeAvatarLabel(em), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Colors Palette picker
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(colors) { col ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(android.graphics.Color.parseColor(col)), CircleShape)
                                    .border(
                                        if (selectColor == col) 2.dp else 0.dp,
                                        Color.White,
                                        CircleShape
                                    )
                                    .clickable { selectColor = col; SoundManager.playSoftClick() }
                             )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "SELECT COUNTRY REGION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Flags row with expanded set of countries
                    val expandedCountries = listOf(
                        Pair("US", "🇺🇸"), Pair("DE", "🇩🇪"), Pair("BR", "🇧🇷"), Pair("KR", "🇰🇷"),
                        Pair("FR", "🇫🇷"), Pair("CA", "🇨🇦"), Pair("JP", "🇯🇵"), Pair("GB", "🇬🇧"),
                        Pair("AU", "🇦🇺"), Pair("IN", "🇮🇳"), Pair("IT", "🇮🇹"), Pair("MX", "🇲🇽"),
                        Pair("ES", "🇪🇸"), Pair("NL", "🇳🇱"), Pair("CN", "🇨🇳"), Pair("CH", "🇨🇭"),
                        Pair("SE", "🇸🇪"), Pair("TR", "🇹🇷"), Pair("AE", "🇦🇪"), Pair("ZA", "🇿🇦")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(expandedCountries) { pair ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selectCountry == pair.first) Color(0xFFE91E63) else Color.Black.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .clickable { selectCountry = pair.first; SoundManager.playSoftClick() }
                            ) {
                                Text("${pair.second} ${pair.first}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(errorMsg, color = Color(0xFFFF1744), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3D Submission Button
                Button3D(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            errorMsg = "Fields cannot be blank!"
                            return@Button3D
                        }
                        if (isSignUp) {
                            viewModel.registerNewAccount(username, password, selectEmoji, selectColor, selectCountry, customPicUri) { ok, msg ->
                                if (!ok) errorMsg = msg
                            }
                        } else {
                            viewModel.loginExistingAccount(username, password) { ok, msg ->
                                if (!ok) errorMsg = msg
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    containerColor = Color(0xFFFFD54F),
                    bottomColor = Color(0xFFC7A500)
                ) {
                    Text(
                        if (isSignUp) "CREATE LEGEND" else "ENTER GAME",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color(0xFF3F2B00)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🔒 Secured with Candy-Shield® Encryptions",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val avatarEmoji: String,
    val avatarColor: String,
    val value: Long,
    val country: String,
    val isCurrentUser: Boolean = false
)

fun getFlagEmoji(country: String): String {
    return when (country) {
        "DE" -> "🇩🇪"
        "BR" -> "🇧🇷"
        "KR" -> "🇰🇷"
        "FR" -> "🇫🇷"
        "CA" -> "🇨🇦"
        "JP" -> "🇯🇵"
        "GB" -> "🇬🇧"
        else -> "🇺🇸"
    }
}

@Composable
fun RankPill(title: String, colorHex: String, rank: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(title, fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
        Text("#$rank", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(android.graphics.Color.parseColor(colorHex)))
    }
}

@Composable
fun RowScope.LeaderboardTabButton(title: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .background(
                if (selected) Color(0xFFFFD54F) else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color(0xFF362100) else Color.White
        )
    }
}

@Composable
fun LeaderboardsOverlay(viewModel: GameViewModel, state: PlayerState, onClose: () -> Unit) {
    var activeTab by remember { mutableStateOf(0) } // 0: Coins, 1: Gems, 2: Wins

    val myCoins = state.coins
    val myGems = state.gems
    val myWins = (state.currentLevel - 1).coerceAtLeast(0)

    val coinRank = viewModel.getPlayerCoinRank(myCoins)
    val gemRank = viewModel.getPlayerDiamondRank(myGems)
    val winRank = viewModel.getPlayerWinRank(myWins)
    val globalRank = viewModel.getPlayerGlobalRank(myCoins, myGems, myWins)

    // Base mock templates
    val mockTemplates = listOf(
        Triple("RoyalChamp", "FR", "#FF3D00" to "CR"),
        Triple("CrownQueen", "BR", "#EC407A" to "QN"),
        Triple("GoldLord", "DE", "#7E57C2" to "LD"),
        Triple("RoyalKnight", "KR", "#26A69A" to "KT"),
        Triple("ShieldHero", "CA", "#42A5F5" to "SH"),
        Triple("DukeOfCrush", "JP", "#9CCC65" to "DK"),
        Triple("Sovereign", "US", "#FFCA28" to "KG"),
        Triple("BaronGold", "GB", "#AB47BC" to "LD"),
        Triple("GrandDuchess", "BR", "#26C6DA" to "QN"),
        Triple("RoyalViscount", "DE", "#FF7043" to "DK")
    )

    // Coins Leaderboard
    val coinValues = listOf(6500, 5200, 4100, 3200, 2500, 1800, 1400, 900, 600, 300)
    val coinsRoster = mutableListOf<LeaderboardEntry>()
    for (i in mockTemplates.indices) {
        val t = mockTemplates[i]
        coinsRoster.add(
            LeaderboardEntry(i + 1, t.first, t.third.second, t.third.first, coinValues[i].toLong(), t.second)
        )
    }
    coinsRoster.add(
        LeaderboardEntry(0, state.username, state.avatarEmoji, state.avatarColorHex, myCoins.toLong(), getFlagEmoji(state.countryCode), isCurrentUser = true)
    )
    val sortedCoins = coinsRoster.sortedByDescending { it.value }.mapIndexed { index, item -> item.copy(rank = index + 1) }

    // Gems Leaderboard
    val gemValues = listOf(450, 380, 310, 240, 190, 150, 110, 80, 40, 20)
    val gemsRoster = mutableListOf<LeaderboardEntry>()
    for (i in mockTemplates.indices) {
        val t = mockTemplates[i]
        gemsRoster.add(
            LeaderboardEntry(i + 1, t.first, t.third.second, t.third.first, gemValues[i].toLong(), t.second)
        )
    }
    gemsRoster.add(
        LeaderboardEntry(0, state.username, state.avatarEmoji, state.avatarColorHex, myGems.toLong(), getFlagEmoji(state.countryCode), isCurrentUser = true)
    )
    val sortedGems = gemsRoster.sortedByDescending { it.value }.mapIndexed { index, item -> item.copy(rank = index + 1) }

    // Wins Leaderboard
    val winValues = listOf(45, 38, 31, 25, 18, 12, 8, 5, 3, 1)
    val winsRoster = mutableListOf<LeaderboardEntry>()
    for (i in mockTemplates.indices) {
        val t = mockTemplates[i]
        winsRoster.add(
            LeaderboardEntry(i + 1, t.first, t.third.second, t.third.first, winValues[i].toLong(), t.second)
        )
    }
    winsRoster.add(
        LeaderboardEntry(0, state.username, state.avatarEmoji, state.avatarColorHex, myWins.toLong(), getFlagEmoji(state.countryCode), isCurrentUser = true)
    )
    val sortedWins = winsRoster.sortedByDescending { it.value }.mapIndexed { index, item -> item.copy(rank = index + 1) }

    val activeList = when (activeTab) {
        0 -> sortedCoins
        1 -> sortedGems
        else -> sortedWins
    }

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E102F))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🏆 LEADERBOARDS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD54F)
                    )
                    IconButton(onClick = onClose) {
                        Text("❌", fontSize = 16.sp, color = Color.White)
                    }
                }

                // Show Personal ranks
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RankPill("Global", "#00E5FF", globalRank)
                        RankPill("Coins", "#FFD700", coinRank)
                        RankPill("Gems", "#E140FB", gemRank)
                        RankPill("Wins", "#00FF66", winRank)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    LeaderboardTabButton("COINS", activeTab == 0) { activeTab = 0; SoundManager.playSoftClick() }
                    LeaderboardTabButton("GEMS", activeTab == 1) { activeTab = 1; SoundManager.playSoftClick() }
                    LeaderboardTabButton("WINS", activeTab == 2) { activeTab = 2; SoundManager.playSoftClick() }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weekly tag
                Text(
                    text = "RESET IN 2D 14H",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFC107),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Leaders list
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(activeList) { item ->
                        val isSelf = item.isCurrentUser
                        val bg = if (isSelf) Color(0xFFE91E63).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)
                        val border = if (isSelf) BorderStroke(1.5.dp, Color(0xFFE91E63)) else null

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = bg),
                            border = border
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (item.rank) {
                                        1 -> Text("🥇", fontSize = 20.sp)
                                        2 -> Text("🥈", fontSize = 18.sp)
                                        3 -> Text("🥉", fontSize = 16.sp)
                                        else -> Text("${item.rank}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(android.graphics.Color.parseColor(item.avatarColor)), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item.avatarEmoji, fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.country, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            item.username,
                                            fontSize = 13.sp,
                                            color = if (isSelf) Color(0xFFFFD54F) else Color.White,
                                            fontWeight = if (isSelf) FontWeight.Black else FontWeight.Bold
                                        )
                                    }
                                    if (isSelf) {
                                        Text("YOU", fontSize = 9.sp, color = Color(0xFFE91E63), fontWeight = FontWeight.Black)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val scoreText = item.value.toString()
                                    when (activeTab) {
                                        0 -> {
                                            GoldCoinIcon(Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(scoreText, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                        1 -> {
                                            ShinyGemIcon(Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(scoreText, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                        else -> {
                                            Text("$scoreText Wins", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeartsRefillOverlay(viewModel: GameViewModel, state: PlayerState, onClose: () -> Unit) {
    val countdown by viewModel.livesRefillCountdown.collectAsState()

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFFF2F68), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E102F))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("❤️ HEARTS LOBBY", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF2F68))
                    IconButton(onClick = onClose) {
                        Text("❌", fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..5) {
                        val active = state.lives >= i
                        Text(
                            text = if (active) "❤️" else "🖤",
                            fontSize = 32.sp,
                            modifier = Modifier.scale(if (active) 1.1f else 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "LIVES: ${state.lives} / 5",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                if (state.lives < 5 && countdown.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Next Refill: $countdown (20m total)",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button3D(
                    onClick = { viewModel.purchaseLifeRefill() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    containerColor = Color(0xFFFF9800),
                    bottomColor = Color(0xFFC55A00)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        GoldCoinIcon(Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BUY FULL REFILL (150 Coins)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button3D(
                    onClick = { viewModel.watchRewardedAdForLife() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    containerColor = Color(0xFF4CAF50),
                    bottomColor = Color(0xFF2E7D32)
                ) {
                    Text("📺 RECOVER 1 HEART (Watch Ad)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button3D(
                    onClick = { viewModel.useLifeItem() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    containerColor = Color(0xFFAB47BC),
                    bottomColor = Color(0xFF7B1FA2)
                ) {
                    Text("🧪 USE HEALTH POTION (Special)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(viewModel: GameViewModel, state: PlayerState, onClose: () -> Unit) {
    val database = AppDatabase.getDatabase(viewModel.getApplication())
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFEC407A), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E102F))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙️ GAME SETTINGS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC407A))
                    IconButton(onClick = onClose) {
                        Text("❌", fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎵", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Background Music", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text("Chant synth melodies", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                    Switch(
                        checked = state.musicEnabled,
                        onCheckedChange = { chk ->
                            scope.launch {
                                database.playerDao().savePlayerState(state.copy(musicEnabled = chk))
                                SoundManager.musicEnabled = chk
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFEC407A))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔊", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Sound Effects (SFX)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text("Retro synthesized pips", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                    Switch(
                        checked = state.sfxEnabled,
                        onCheckedChange = { chk ->
                            scope.launch {
                                database.playerDao().savePlayerState(state.copy(sfxEnabled = chk))
                                SoundManager.sfxEnabled = chk
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFEC407A))
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onClose()
                        viewModel.logoutCurrentUser()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("LOG OUT ACCOUNT (${state.username})")
                }
            }
        }
    }
}

@Composable
fun LiveEventLeaderboardOverlay(viewModel: GameViewModel, onClose: () -> Unit) {
    val leaderboard by viewModel.liveEventLeaderboard.collectAsState()
    val timeRemaining by viewModel.liveEventTimeRemaining.collectAsState()

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E102F))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🎉 LIVE EVENT LEADERS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD54F)
                    )
                    IconButton(onClick = onClose) {
                        Text("❌", fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🏆 RESET AND REWARDS IN $timeRemaining",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(leaderboard) { item ->
                        val isSelf = item.isCurrentUser
                        val bg = if (isSelf) Color(0xFFE91E63).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)
                        val border = if (isSelf) BorderStroke(1.5.dp, Color(0xFFE91E63)) else null

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = bg),
                            border = border
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (item.rank) {
                                        1 -> Text("🥇", fontSize = 20.sp)
                                        2 -> Text("🥈", fontSize = 18.sp)
                                        3 -> Text("🥉", fontSize = 16.sp)
                                        else -> Text("${item.rank}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(android.graphics.Color.parseColor(item.avatarColor)), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item.avatarEmoji, fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.country, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            item.username,
                                            fontSize = 13.sp,
                                            color = if (isSelf) Color(0xFFFFD54F) else Color.White,
                                            fontWeight = if (isSelf) FontWeight.Black else FontWeight.Bold
                                        )
                                    }
                                    if (isSelf) {
                                        Text("YOU", fontSize = 9.sp, color = Color(0xFFE91E63), fontWeight = FontWeight.Black)
                                    }
                                }

                                Text(
                                    "${item.value} levels",
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveEventAnnouncementBanner(viewModel: GameViewModel) {
    val visible by viewModel.liveEventBannerVisible.collectAsState()

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF50057))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 LIVE EVENT STARTED! 🎉",
                    color = Color(0xFFFFD54F),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "COMPLETE THE MOST LEVELS IN 15 MINUTES TO WIN AMAZING REWARDS!",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("⏱️ Countdown:", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    val time by viewModel.liveEventTimeRemaining.collectAsState()
                    Text(time, color = Color(0xFFFFD54F), fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun LiveEventEndPodiumOverlay(viewModel: GameViewModel, onClose: () -> Unit) {
    val finalRank by viewModel.liveEventFinalRank.collectAsState()

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E102F))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🏆 EVENT COMPLETED!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD54F)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "The tourney has ended! Here are the podium results:",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Draw podium graphic
                Row(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 2nd Place
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("🥈", fontSize = 24.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Color(0xFFB0BEC5), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("2nd", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                        }
                    }
                    // 1st Place
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                        Text("👑 🥇", fontSize = 32.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Color(0xFFFFD54F), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("1st", fontWeight = FontWeight.Black, color = Color(0xFF5D4037), fontSize = 14.sp)
                        }
                    }
                    // 3rd Place
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("🥉", fontSize = 20.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(35.dp)
                                .background(Color(0xFFCA9D7C), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("3rd", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "YOUR STANDING: Rank #$finalRank",
                    color = Color(0xFF00FF66),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                val rewardsText = when (finalRank) {
                    1 -> "1st Place REWARDS: 1 Dupe Bomb, 1 TNT Bomb, 2 Spinners!"
                    2 -> "2nd Place REWARDS: 1 TNT Bomb, 2 Spinners!"
                    3 -> "3rd Place REWARDS: 2 Spinners!"
                    else -> "Participation REWARDS: 100 participation coins!"
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = rewardsText,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button3D(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    containerColor = Color(0xFF00E676),
                    bottomColor = Color(0xFF00B0FF)
                ) {
                    Text("CLAIM REWARDS!", fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun BoosterShopOverlay(viewModel: GameViewModel, state: PlayerState, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(2.dp, Color(0xFFE91E63), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF160A21))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "BOOSTER SHOP",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            "Upgrade your explosive abilities!",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = onClose) {
                        Text("❌", fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GoldCoinIcon(Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Coins: ${state.coins}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ShinyGemIcon(Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gems: ${state.gems}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        ShopPurchaseRow(
                            title = "Dupe Bomb",
                            desc = "Rainbow blast! Duplicates most prominent candies.",
                            price = 50000,
                            owned = state.getBoosterCount("dupe_bomb"),
                            icon = "DB"
                        ) {
                            viewModel.purchaseBooster("dupe_bomb", 50000)
                        }
                    }

                    item {
                        ShopPurchaseRow(
                            title = "TNT Bomb",
                            desc = "Giant explosive fuse clearing a 5x5 grid.",
                            price = 25000,
                            owned = state.getBoosterCount("tnt_bomb"),
                            icon = "TNT"
                        ) {
                            viewModel.purchaseBooster("tnt_bomb", 25000)
                        }
                    }

                    item {
                        ShopPurchaseRow(
                            title = "Spinner",
                            desc = "Launches spirals to crush 3 random goal blockers.",
                            price = 10000,
                            owned = state.getBoosterCount("spinner"),
                            icon = "SP"
                        ) {
                            viewModel.purchaseBooster("spinner", 10000)
                        }
                    }

                    item {
                        ShopPurchaseRow(
                            title = "Coin Vault Bundle",
                            desc = "Instantly buy 15,000 Coins via Gem transfer.",
                            price = 20,
                            isGem = true,
                            owned = 0,
                            icon = "COIN"
                        ) {
                            viewModel.purchaseCoinsWithGems(15000, 20)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopPurchaseRow(
    title: String,
    desc: String,
    price: Int,
    isGem: Boolean = false,
    owned: Int,
    icon: String,
    onBuy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (icon) {
                    "COIN" -> GoldCoinIcon(Modifier.size(24.dp))
                    "GEM" -> ShinyGemIcon(Modifier.size(24.dp))
                    "DB" -> Text("DB", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD54F))
                    "TNT" -> Text("TNT", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF1744))
                    "SP" -> Text("SP", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E5FF))
                    else -> Text(icon, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                Text(desc, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                if (owned > 0) {
                    Text("Owned count: $owned", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isGem) {
                        ShinyGemIcon(Modifier.size(12.dp))
                    } else {
                        GoldCoinIcon(Modifier.size(12.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$price", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun DecorativeBackgroundPattern() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgPulse"
    )
    val slowRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgRotate"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF140D24))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Base warm sky radiant gradient
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF4C2A75), Color(0xFF1E0E3B), Color(0xFF0F0520)),
                    center = Offset(size.width * 0.5f, size.height * 0.3f),
                    radius = size.maxDimension * 0.9f
                )
            )

            // Dynamic cosmic glowing clouds
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8E24AA).copy(alpha = 0.25f * pulseAlpha), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.4f),
                    radius = size.maxDimension * 0.5f
                ),
                radius = size.maxDimension * 0.5f,
                center = Offset(size.width * 0.2f, size.height * 0.4f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00B0FF).copy(alpha = 0.2f * (1.2f - pulseAlpha)), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.7f),
                    radius = size.maxDimension * 0.4f
                ),
                radius = size.maxDimension * 0.4f,
                center = Offset(size.width * 0.8f, size.height * 0.7f)
            )

            // Radiant energy lines rotating behind the board
            rotate(slowRotate) {
                val numBeams = 12
                val beamAngle = 360f / numBeams
                for (i in 0 until numBeams) {
                    val angle = i * beamAngle
                    val rad = Math.toRadians(angle.toDouble())
                    val endX = size.width / 2f + (cos(rad) * size.maxDimension).toFloat()
                    val endY = size.height / 2f + (sin(rad) * size.maxDimension).toFloat()
                    
                    val path = Path().apply {
                        moveTo(size.width / 2f, size.height / 2f)
                        val radLeft = Math.toRadians((angle - beamAngle * 0.25f).toDouble())
                        lineTo(
                            (size.width / 2f + cos(radLeft) * size.maxDimension).toFloat(),
                            (size.height / 2f + sin(radLeft) * size.maxDimension).toFloat()
                        )
                        lineTo(endX, endY)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFFFFD54F).copy(alpha = 0.03f)
                    )
                }
            }

            // Draw twinkling background stars
            val starSeeds = listOf(
                Offset(0.15f, 0.12f) to 4f,
                Offset(0.85f, 0.18f) to 5f,
                Offset(0.35f, 0.25f) to 3f,
                Offset(0.72f, 0.32f) to 6f,
                Offset(0.22f, 0.55f) to 4f,
                Offset(0.88f, 0.62f) to 5f,
                Offset(0.12f, 0.78f) to 4f,
                Offset(0.55f, 0.88f) to 6f
            )
            for ((p, maxS) in starSeeds) {
                val sx = p.x * size.width
                val sy = p.y * size.height
                val currentSize = maxS * (0.4f + 0.6f * sin((System.currentTimeMillis() * 0.002f + sx).toDouble()).toFloat())
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = currentSize,
                    center = Offset(sx, sy)
                )
            }
        }
    }
}

@Composable
fun AdminToolsOverlay(
    viewModel: GameViewModel,
    state: PlayerState,
    onClose: () -> Unit
) {
    var adminLvl by remember { mutableStateOf(state.currentLevel) }
    val liveEventActive by viewModel.liveEventActive.collectAsState()

    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1035)),
            border = BorderStroke(2.dp, Color(0xFFFFD54F)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "👑 MR AWO ADMIN TOOLS",
                    color = Color(0xFFFFD54F),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Developer Cheat Console",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Level Manipulation
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SET CURRENT LEVEL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFA726)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { adminLvl = (adminLvl - 5).coerceAtLeast(1) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) { Text("-5", color = Color.White, fontWeight = FontWeight.Bold) }

                            Button(
                                onClick = { adminLvl = (adminLvl - 1).coerceAtLeast(1) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE64A19)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) { Text("-1", color = Color.White, fontWeight = FontWeight.Bold) }

                            Text(
                                text = "Lvl $adminLvl",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { adminLvl = (adminLvl + 1).coerceIn(1, 100) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) { Text("+1", color = Color.White, fontWeight = FontWeight.Bold) }

                            Button(
                                onClick = { adminLvl = (adminLvl + 5).coerceIn(1, 100) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) { Text("+5", color = Color.White, fontWeight = FontWeight.Bold) }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.adminModifyPlayer(setLevel = adminLvl) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF311B92)),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("APPLY LEVEL SELECTION", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Add Currency & Boosters cheats
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "QUICK CHEATS & RESOURCES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFA726),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.adminModifyPlayer(addCoins = 10000) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("+10K Coins", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Button(
                                onClick = { viewModel.adminModifyPlayer(addGems = 1000) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF)),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("+1K Gems", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.adminModifyPlayer(refillLives = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC407A)),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("Refill 5 Lives", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Button(
                                onClick = { viewModel.adminModifyPlayer(addBoosters = 10) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("+10 Boosters", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Interactive Live Event Toggle
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LIVE TOURNAMENT EVENT STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFA726)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (liveEventActive) "Status: ACTIVE 🏆" else "Status: INACTIVE ⏳",
                                color = if (liveEventActive) Color(0xFF00FF66) else Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Button(
                                onClick = { viewModel.adminToggleLiveEvent(!liveEventActive) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (liveEventActive) Color(0xFFE53935) else Color(0xFF43A047)
                                ),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = if (liveEventActive) "DISABLE" else "ENABLE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Close Button
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close Panel", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
