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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.roundToInt

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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // High-end generated game logo
            Image(
                painter = painterResource(id = R.drawable.img_app_icon),
                contentDescription = "Candy Kingdom Legends Logo",
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulse)
                    .shadow(16.dp, RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "CANDY KINGDOM",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFD54F),
                letterSpacing = 2.sp,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = "L E G E N D S",
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
                        Text(text = "🪙", fontSize = 18.sp)
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
                        Text(text = if (state.lives > 0) "❤️" else "🖤", fontSize = 18.sp)
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
                        Text(text = "💎", fontSize = 18.sp)
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

            // Large Hero banner with game logo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon),
                        contentDescription = "Candy Kingdom Hero Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Glass filter overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Candy Kingdom",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Welcome back, ${state.username}! 🍬 Let's crush sweet goals.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.82f)
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

            // Current Adventure Button
            Button(
                onClick = { viewModel.navigateTo(GameScreen.Map) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("play_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E676)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PLAY ADVENTURE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
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
                        text = "🍭 BOOSTER SWEET STORE",
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

        LiveEventAnnouncementBanner(viewModel)
    }
}

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
                Text(text = "🪙", fontSize = 12.sp)
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
                    Text(text = "🪙", fontSize = 15.sp)
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
                                            viewModel.loadLevel(lvl)
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
    }
}

// 4. MAIN GAMEPLAY / PUZZLE SCREEN
@Composable
fun GameplayScreen(viewModel: GameViewModel, level: Int) {
    val board by viewModel.boardState.collectAsState()
    val obstacles by viewModel.obstacleState.collectAsState()
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

    // Popups
    val isDone by viewModel.isLevelDone.collectAsState()
    val isOver by viewModel.isGameOver.collectAsState()

    // State matching selection variables for swiping
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isBoss) {
                        listOf(Color(0xFF3E1F47), Color(0xFF1F122B), Color(0xFF0E0715))
                    } else {
                        listOf(Color(0xFF145374), Color(0xFF003049), Color(0xFF000814))
                    }
                )
            )
    ) {
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
                    Text(text = "🪙", fontSize = 16.sp)
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
                BossArenaPanel(bName, bHp, bMax, bPhase, bAttackCountdown)
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
                Card(
                    modifier = Modifier.aspectRatio(1f),
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
                                selectedCell = selectedCell,
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
                                }
                            )
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
        when (type) {
            ObstacleType.CRATE -> drawRect(Color(0xFF8D6E63), size = size)
            ObstacleType.ICE -> drawCircle(Color(0xFF80DEEA), radius = r, center = center)
            ObstacleType.CHAIN -> drawCircle(Color.Gray, radius = r * 0.8f, center = center, style = Stroke(4f))
            ObstacleType.CHOCOLATE -> drawRect(Color(0xFF3E2723), size = size)
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
    countdown: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1033)),
        border = BorderStroke(2.dp, Color(0xFFFF1744).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name & phase
                Column {
                    Text(text = "👑 $name (" + when (phase) {
                        3 -> "Rage Phase 3"
                        2 -> "Agile Phase 2"
                        else -> "Standard Phase 1"
                    } + ")", fontSize = 14.sp, color = Color(0xFFFF1744), fontWeight = FontWeight.Black)
                    Text(text = "Mini-blocks after $countdown moves", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                }

                // hp display
                Text(text = "$hp / $maxHp HP", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // health bar progress
            val hpRatio = (hp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)
            val hpColor = when {
                hpRatio <= 0.3f -> Color.Red
                hpRatio <= 0.7f -> Color(0xFFFF9100)
                else -> Color(0xFF4CAF50)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(7.dp))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(hpRatio)
                        .background(
                            Brush.linearGradient(listOf(hpColor.copy(alpha = 0.7f), hpColor)),
                            RoundedCornerShape(5.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun GridComposablesBoard(
    board: Array<Array<CandyItem?>>,
    obstacles: Map<Pair<Int, Int>, ObstacleType>,
    selectedCell: Pair<Int, Int>?,
    onCellSelect: (Int, Int) -> Unit,
    onSwipe: (Int, Int, String) -> Unit
) {
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
                    val obstacle = obstacles[Pair(r, c)] ?: ObstacleType.NONE

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(2.dp)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
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
                        }

                        // Overlay Obstacle renders
                        if (obstacle != ObstacleType.NONE) {
                            RenderObstacleGraphic(obstacle)
                        }
                    }
                }
            }
        }
    }
}

// Draws obstacles overlays
@Composable
fun RenderObstacleGraphic(type: ObstacleType) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        when (type) {
            ObstacleType.CRATE -> {
                // Breakable woody wooden crates
                drawRect(
                    color = Color(0xFFA1887F),
                    size = size
                )
                drawRect(
                    color = Color(0xFF5D4037),
                    size = size,
                    style = Stroke(3.dp.toPx())
                )
                // Draw diagonal brace
                drawLine(
                    color = Color(0xFF5D4037),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            ObstacleType.ICE -> {
                // Shiny freezing frost crystals
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xE6E0F7FA), Color(0x994DD0E1)),
                        center = center,
                        radius = size.width * 0.7f
                    ),
                    size = size
                )
                drawRect(
                    color = Color.White,
                    size = size,
                    style = Stroke(2.dp.toPx())
                )
            }
            ObstacleType.CHAIN -> {
                // Steel chains matching overlay
                drawCircle(
                    color = Color(0xB3B0BEC5),
                    radius = r * 0.7f,
                    style = Stroke(4.dp.toPx()),
                    center = center
                )
                drawCircle(
                    color = Color.Black,
                    radius = r * 0.7f,
                    style = Stroke(1.dp.toPx()),
                    center = center
                )
            }
            ObstacleType.CHOCOLATE -> {
                // Sticky dark brown slime spread
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6D4C41), Color(0xFF3E2723)),
                        center = center,
                        radius = size.width * 0.8f
                    ),
                    size = size
                )
            }
            else -> {}
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val active = i <= stars
                        val tint = if (active) Color(0xFFFFD54F) else Color.Gray
                        val scale = if (active) 1.2f else 0.9f
                        
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star",
                            tint = tint,
                            modifier = Modifier
                                .size(48.dp)
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
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CONTINUE", color = Color(0xFF3F2B00), fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}

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
                    Text(text = "🪙", fontSize = 16.sp)
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
    var selectEmoji by remember { mutableStateOf("🍬") }
    var selectColor by remember { mutableStateOf("#E91E63") }
    var selectCountry by remember { mutableStateOf("US") }
    var customPicUri by remember { mutableStateOf("") }

    val emojis = listOf("🍬", "🍭", "🍩", "🍫", "🧁", "🍪", "🧸")
    val colors = listOf("#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#009688", "#4CAF50", "#FFC107", "#FF9800", "#FF5722")
    val countries = listOf(
        Pair("US", "🇺🇸"), Pair("DE", "🇩🇪"), Pair("BR", "🇧🇷"), Pair("KR", "🇰🇷"),
        Pair("FR", "🇫🇷"), Pair("CA", "🇨🇦"), Pair("JP", "🇯🇵"), Pair("GB", "🇬🇧")
    )

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
                                    Text(selectEmoji, fontSize = 28.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (customPicUri.isNotEmpty()) "Custom Photo Loaded" else "Using Preset Emoji",
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
                                Text(em, fontSize = 20.sp)
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
                        if (isSignUp) "CREATE LEGEND" else "ENTER KINGDOM",
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
        Triple("SugarRush", "🇫🇷", "#FF3D00" to "🍭"),
        Triple("CandyQueen", "🇧🇷", "#EC407A" to "🍩"),
        Triple("ChocoLord", "🇩🇪", "#7E57C2" to "🍫"),
        Triple("LollipopHero", "🇰🇷", "#26A69A" to "🧁"),
        Triple("SweetCheeks", "🇨🇦", "#42A5F5" to "🧸"),
        Triple("GummyBear99", "🇯🇵", "#9CCC65" to "🍬"),
        Triple("SodaPop", "🇺🇸", "#FFCA28" to "🧸"),
        Triple("CookieMonster", "🇬🇧", "#AB47BC" to "🍪"),
        Triple("TuttiFrutti", "🇧🇷", "#26C6DA" to "🍭"),
        Triple("Marshmallow", "🇩🇪", "#FF7043" to "🧁")
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
                    LeaderboardTabButton("🪙 Coins", activeTab == 0) { activeTab = 0; SoundManager.playSoftClick() }
                    LeaderboardTabButton("💎 Gems", activeTab == 1) { activeTab = 1; SoundManager.playSoftClick() }
                    LeaderboardTabButton("👑 Wins", activeTab == 2) { activeTab = 2; SoundManager.playSoftClick() }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weekly tag
                Text(
                    text = "⏳ S1 REWARDS LOCK IN 2D 14H",
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

                                Text(
                                    text = when (activeTab) {
                                        0 -> "🪙 ${item.value}"
                                        1 -> "💎 ${item.value}"
                                        else -> "👑 ${item.value} Wins"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
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
                    Text("🪙 BUY FULL REFILL (150 Coins)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
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
                    1 -> "🥇 REWARDS: 1 Dupe Bomb, 1 TNT Bomb, 2 Spinners!"
                    2 -> "🥈 REWARDS: 1 TNT Bomb, 2 Spinners!"
                    3 -> "🥉 REWARDS: 2 Spinners!"
                    else -> "🍫 REWARDS: 100 participation coins!"
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
                    Text("CLAIM REWARDS! 🍬", fontWeight = FontWeight.Black, color = Color.White)
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
                            "🍭 BOOSTER SWEET STORE",
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
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("🪙 Coins: ${state.coins}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("💎 Gems: ${state.gems}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        ShopPurchaseRow(
                            title = "Dupe Bomb ⚡",
                            desc = "Rainbow blast! Duplicates most prominent candies.",
                            price = 50000,
                            owned = state.getBoosterCount("dupe_bomb"),
                            icon = "⚡"
                        ) {
                            viewModel.purchaseBooster("dupe_bomb", 50000)
                        }
                    }

                    item {
                        ShopPurchaseRow(
                            title = "TNT Bomb 💥",
                            desc = "Giant explosive fuse clearing a 5x5 grid.",
                            price = 25000,
                            owned = state.getBoosterCount("tnt_bomb"),
                            icon = "💥"
                        ) {
                            viewModel.purchaseBooster("tnt_bomb", 25000)
                        }
                    }

                    item {
                        ShopPurchaseRow(
                            title = "Spinner 🌪️",
                            desc = "Launches spirals to crush 3 random goal blockers.",
                            price = 10000,
                            owned = state.getBoosterCount("spinner"),
                            icon = "🌪️"
                        ) {
                            viewModel.purchaseBooster("spinner", 10000)
                        }
                    }

                    item {
                        ShopPurchaseRow(
                            title = "Coin Vault Bundle 🪙",
                            desc = "Instantly buy 15,000 Coins via Gem transfer.",
                            price = 20,
                            isGem = true,
                            owned = 0,
                            icon = "🪙"
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
                Text(icon, fontSize = 24.sp)
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
                    Text(if (isGem) "💎" else "🪙", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${price}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}
