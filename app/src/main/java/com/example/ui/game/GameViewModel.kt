package com.example.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.PlayerState
import com.example.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Screen states in Candy Kingdom Legends
sealed class GameScreen {
    object Splash : GameScreen()
    object Login : GameScreen()
    object Home : GameScreen()
    object Map : GameScreen()
    data class Gameplay(val level: Int) : GameScreen()
}

// Sparkle/Floating Combat text animations model
data class FloatingText(
    val id: Long,
    val text: String,
    val r: Int,
    val c: Int,
    val isDamage: Boolean = false,
    val colorHex: String = "#FFFFFF"
)

// Main ViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    
    // Core active account flow
    val activeUser = MutableStateFlow("Guest")
    val playerState: StateFlow<PlayerState>

    // Navigation and screen management State
    private val _currentScreen = MutableStateFlow<GameScreen>(GameScreen.Splash)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    // Board State
    private val _boardState = MutableStateFlow<Array<Array<CandyItem?>>>(emptyArray())
    val boardState: StateFlow<Array<Array<CandyItem?>>> = _boardState.asStateFlow()

    private val _obstacleState = MutableStateFlow<Map<Pair<Int, Int>, ObstacleType>>(emptyMap())
    val obstacleState: StateFlow<Map<Pair<Int, Int>, ObstacleType>> = _obstacleState.asStateFlow()

    // Match-3 session metrics State
    val currentLevel = MutableStateFlow(1)
    val movesLeft = MutableStateFlow(30)
    val currentScore = MutableStateFlow(0L)
    val goalsList = MutableStateFlow<List<LevelGoal>>(emptyList())
    val isBusy = MutableStateFlow(false) // Blocks user feedback during cascades
    val isLevelDone = MutableStateFlow(false)
    val isGameOver = MutableStateFlow(false)
    val starsAchieved = MutableStateFlow(0)

    // Boss Battle states
    val isBossBattle = MutableStateFlow(false)
    val bossName = MutableStateFlow("")
    val bossMaxHp = MutableStateFlow(1000L)
    val bossHp = MutableStateFlow(1000L)
    val bossPhase = MutableStateFlow(1) // Phase 1, 2, 3
    val bossAttackCountdown = MutableStateFlow(5) // Downward count in steps to spawn random hurdles

    // Floating UI numbers and explosive messages
    private val _floatingTexts = MutableStateFlow<List<FloatingText>>(emptyList())
    val floatingTexts: StateFlow<List<FloatingText>> = _floatingTexts.asStateFlow()

    private var currentEngine: Match3Engine? = null
    private var activeLevelConfig: LevelConfig? = null
    private var textIdCounter = 1L

    // Pre-level Booster selections
    val equippedDupe = MutableStateFlow(false)
    val equippedTnt = MutableStateFlow(false)
    val equippedSpinner = MutableStateFlow(false)

    // In-game session usage
    val sessionDupeCount = MutableStateFlow(0)
    val sessionTntCount = MutableStateFlow(0)
    val sessionSpinnerCount = MutableStateFlow(0)

    // Booster Shop and animations state
    val showBoosterShop = MutableStateFlow(false)

    // Live Event State management
    val liveEventActive = MutableStateFlow(false)
    val liveEventTimeRemaining = MutableStateFlow("")
    val liveEventTimeMs = MutableStateFlow(0L)
    val liveEventBannerVisible = MutableStateFlow(false)
    val liveEventRewardPending = MutableStateFlow(false)
    val liveEventFinalRank = MutableStateFlow(1)
    val showEventEndPodium = MutableStateFlow(false)
    val liveEventLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    
    // Tracks current event ID checking
    private val lastCheckedEventId = MutableStateFlow(0L)

    // Life systems & countdown
    val livesRefillCountdown = MutableStateFlow("")

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.playerDao())
        
        // Reactive combining of active account
        playerState = activeUser
            .flatMapLatest { user -> repository.getPlayerState(user) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = PlayerState()
            )

        // Dynamic sound loop & Life regeneration countdown ticker
        viewModelScope.launch {
            // Start the lovely synthesized pentatonic arpeggio music stream
            SoundManager.startMusicLoop()
            
            while (true) {
                delay(1000)
                val state = playerState.value
                
                // Keep SoundManager configuration synchronized with player settings
                SoundManager.musicEnabled = state.musicEnabled
                SoundManager.sfxEnabled = state.sfxEnabled
                
                if (state.lives < 5) {
                    val now = System.currentTimeMillis()
                    val diff = now - state.lastLifeRegenTime
                    val limit = 20L * 60L * 1000L // 20 minutes
                    val remaining = limit - diff
                    if (remaining <= 0) {
                        repository.savePlayerState(state.copy(lives = 5))
                        livesRefillCountdown.value = ""
                        SoundManager.playRewardCollection()
                    } else {
                        val min = (remaining / 1000) / 60
                        val sec = (remaining / 1000) % 60
                        livesRefillCountdown.value = String.format("%02d:%02d", min, sec)
                    }
                } else {
                    livesRefillCountdown.value = ""
                }
            }
        }

        // Global Live Event Schedule Ticker
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val now = System.currentTimeMillis()
                val cycleLen = 20 * 60 * 1000L // 20 minutes
                val activeLen = 15 * 60 * 1000L // 15 minutes
                val currentCycleMs = now % cycleLen
                val active = currentCycleMs < activeLen
                val eventId = now / cycleLen

                // Update activity state
                val previousActive = liveEventActive.value
                liveEventActive.value = active

                val activeUserState = playerState.value
                val userLevelWins = activeUserState.eventLevelsCompleted

                if (active) {
                    val remainingMs = activeLen - currentCycleMs
                    val min = (remainingMs / 1000) / 60
                    val sec = (remainingMs / 1000) % 60
                    liveEventTimeRemaining.value = String.format("%02d:%02d", min, sec)
                    liveEventTimeMs.value = remainingMs

                    // Check if event transitioned to a new one
                    if (lastCheckedEventId.value != eventId) {
                        lastCheckedEventId.value = eventId
                        // Clear previous score
                        if (activeUserState.lastJoinedEventTime != eventId && activeUser.value != "Guest") {
                            repository.savePlayerState(activeUserState.copy(
                                eventLevelsCompleted = 0,
                                lastJoinedEventTime = eventId
                            ))
                        }
                        
                        // Slide down event start announcement
                        liveEventBannerVisible.value = true
                        SoundManager.playLevelStart()
                        
                        // Auto-dismiss after 8 seconds
                        launch {
                            delay(8000)
                            liveEventBannerVisible.value = false
                        }
                    }

                    // Simulated live leaderboard entries
                    val competitors = listOf(
                        Pair("SugarRush", "🇫🇷"), Pair("CandyQueen", "🇧🇷"), Pair("ChocoLord", "🇩🇪"),
                        Pair("LollipopHero", "🇰🇷"), Pair("SweetCheeks", "🇨🇦"), Pair("GummyBear99", "🇯🇵"),
                        Pair("SodaPop", "🇺🇸"), Pair("CookieMonster", "🇬🇧")
                    )

                    // Seed base scores on eventId and make them tick up very slowly
                    val boardList = competitors.mapIndexed { idx, pair ->
                        val progressMinutes = (currentCycleMs / 1000) / 60
                        val baseWins = (idx * 2 + (eventId % 3).toInt()) + (progressMinutes / (idx + 1).coerceAtLeast(1).toFloat()).toInt()
                        LeaderboardEntry(
                            rank = 0,
                            username = pair.first,
                            avatarEmoji = getMockEmoji(idx),
                            avatarColor = getMockColor(idx),
                            value = baseWins.toLong(),
                            country = pair.second
                        )
                    }.toMutableList()

                    // Add player entry
                    boardList.add(
                        LeaderboardEntry(
                            rank = 0,
                            username = activeUserState.username,
                            avatarEmoji = activeUserState.avatarEmoji,
                            avatarColor = activeUserState.avatarColorHex,
                            value = userLevelWins.toLong(),
                            country = activeUserState.countryCode,
                            isCurrentUser = true
                        )
                    )

                    // Sort descending by value (wins completed during event)
                    val sorted = boardList.sortedByDescending { it.value }.mapIndexed { i, e -> e.copy(rank = i + 1) }
                    liveEventLeaderboard.value = sorted

                } else {
                    // It is cooldown mode
                    // If it just transitioned from active to inactive, reward is pending!
                    if (previousActive && activeUser.value != "Guest") {
                        val currentLeaderbd = liveEventLeaderboard.value
                        val playerRank = currentLeaderbd.firstOrNull { it.isCurrentUser }?.rank ?: 5
                        liveEventFinalRank.value = playerRank
                        liveEventRewardPending.value = true
                        showEventEndPodium.value = true
                        
                        // Grant rewards based on final rank
                        grantLiveEventRewards(playerRank)
                    }

                    val cooldownRemainingMs = cycleLen - currentCycleMs
                    val min = (cooldownRemainingMs / 1000) / 60
                    val sec = (cooldownRemainingMs / 1000) % 60
                    liveEventTimeRemaining.value = String.format("Next Event: %02d:%02d", min, sec)
                    liveEventTimeMs.value = 0L
                    liveEventLeaderboard.value = emptyList()
                }
            }
        }

        // Run splash timeout, then transition to our secure Login screen
        viewModelScope.launch {
            delay(1800)
            _currentScreen.value = GameScreen.Login
        }
    }

    private fun getMockEmoji(idx: Int): String {
        val list = listOf("🍬", "🍭", "🍩", "🍫", "🧁", "🍪", "🧸", "🍒")
        return list[idx % list.size]
    }
    private fun getMockColor(idx: Int): String {
        val list = listOf("#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#009688", "#4CAF50", "#FFC107")
        return list[idx % list.size]
    }

    private fun grantLiveEventRewards(rank: Int) {
        viewModelScope.launch {
            val user = activeUser.value
            if (user == "Guest") return@launch
            when (rank) {
                1 -> {
                    repository.adjustBooster(user, "dupe_bomb", 1)
                    repository.adjustBooster(user, "tnt_bomb", 1)
                    repository.adjustBooster(user, "spinner", 2)
                    SoundManager.playLevelStart()
                }
                2 -> {
                    repository.adjustBooster(user, "tnt_bomb", 1)
                    repository.adjustBooster(user, "spinner", 2)
                    SoundManager.playLevelStart()
                }
                3 -> {
                    repository.adjustBooster(user, "spinner", 2)
                    SoundManager.playLevelStart()
                }
            }
        }
    }

    fun loadLevelWithBoosters(level: Int, useDupe: Boolean, useTnt: Boolean, useSpinner: Boolean) {
        if (playerState.value.lives <= 0) {
            addFloatingMessage("No Lives Left! Please Refill.", 4, 3, "#FF1744")
            SoundManager.playTone(300.0, 150)
            return
        }

        viewModelScope.launch {
            val user = activeUser.value
            if (user != "Guest") {
                if (useDupe) { repository.adjustBooster(user, "dupe_bomb", -1) }
                if (useTnt) { repository.adjustBooster(user, "tnt_bomb", -1) }
                if (useSpinner) { repository.adjustBooster(user, "spinner", -1) }
            }

            sessionDupeCount.value = if (useDupe) 1 else 0
            sessionTntCount.value = if (useTnt) 1 else 0
            sessionSpinnerCount.value = if (useSpinner) 1 else 0

            loadLevel(level)
        }
    }

    fun activateDupeBombInGame() {
        val engine = currentEngine ?: return
        if (isBusy.value || isLevelDone.value || isGameOver.value) return
        if (sessionDupeCount.value <= 0) return

        viewModelScope.launch {
            isBusy.value = true
            sessionDupeCount.value -= 1
            SoundManager.playLevelStart()
            
            // Call engine booster
            engine.runDupeBombBoost()
            
            // Refresh
            _boardState.value = engine.board.map { it.clone() }.toTypedArray()
            addFloatingMessage("DUPLICATING! ⚡", 4, 3, "#FFF59D")
            delay(320)
            
            processMatchesAndRunCascade()
            checkGameEndConditions()
            isBusy.value = false
        }
    }

    fun activateTntBombInGame() {
        val engine = currentEngine ?: return
        if (isBusy.value || isLevelDone.value || isGameOver.value) return
        if (sessionTntCount.value <= 0) return

        viewModelScope.launch {
            isBusy.value = true
            sessionTntCount.value -= 1
            
            // Explode a 5x5 board area centering
            engine.runTntBombBoost()
            
            _boardState.value = engine.board.map { it.clone() }.toTypedArray()
            _obstacleState.value = engine.obstacles.toMap()
            addFloatingMessage("TNT BOOM! 💥", 4, 3, "#FF7043")
            delay(320)
            
            processMatchesAndRunCascade()
            checkGameEndConditions()
            isBusy.value = false
        }
    }

    fun activateSpinnerInGame() {
        val engine = currentEngine ?: return
        if (isBusy.value || isLevelDone.value || isGameOver.value) return
        if (sessionSpinnerCount.value <= 0) return

        viewModelScope.launch {
            isBusy.value = true
            sessionSpinnerCount.value -= 1
            
            // Clear three random barriers
            val cleared = engine.runSpinnerBoost()
            
            _boardState.value = engine.board.map { it.clone() }.toTypedArray()
            _obstacleState.value = engine.obstacles.toMap()
            if (cleared.isNotEmpty()) {
                addFloatingMessage("SPINNED TILES! 🌪️", 4, 3, "#26C6DA")
            } else {
                addFloatingMessage("No Blockers found!", 4, 3, "#B0BEC5")
            }
            delay(320)
            
            processMatchesAndRunCascade()
            checkGameEndConditions()
            isBusy.value = false
        }
    }

    fun navigateTo(screen: GameScreen) {
        _currentScreen.value = screen
    }

    // Handles map Level selection click
    fun loadLevel(level: Int) {
        if (playerState.value.lives <= 0) {
            addFloatingMessage("No Lives Left! Please Refill.", 4, 3, "#FF1744")
            SoundManager.playTone(300.0, 150)
            return
        }
        currentLevel.value = level
        val config = LevelGenerator.generate(level)
        activeLevelConfig = config

        // Initialize state
        movesLeft.value = config.movesLimit
        currentScore.value = 0L
        isLevelDone.value = false
        isGameOver.value = false
        starsAchieved.value = 0
        goalsList.value = config.goals.map { it.copy() }

        // Boss Settings
        isBossBattle.value = config.isBossBattle
        if (config.isBossBattle) {
            bossName.value = getBossNameForLevel(level)
            val computedHp = (level * 15L + 50L) * 10L
            bossMaxHp.value = computedHp
            bossHp.value = computedHp
            bossPhase.value = 1
            bossAttackCountdown.value = 4
        }

        // Initialize Engine
        val engine = Match3Engine(config)
        currentEngine = engine
        _boardState.value = engine.board.map { it.clone() }.toTypedArray()
        _obstacleState.value = engine.obstacles.toMap()

        _currentScreen.value = GameScreen.Gameplay(level)
    }

    private fun getBossNameForLevel(level: Int): String {
        return when (level) {
            100 -> "Chocolate Titan"
            200 -> "Frostbite Queen"
            300 -> "Lollipop Dragon"
            400 -> "Cookie Golem"
            500 -> "Rainbow Serpent"
            600 -> "Marshmallow Beast"
            700 -> "Honey Hive Queen"
            800 -> "Crystal Kraken"
            900 -> "Sugar Phantom"
            else -> "Shadow Sugar King"
        }
    }

    // Executes swipe action
    fun performSwipeAction(r1: Int, c1: Int, r2: Int, c2: Int) {
        val engine = currentEngine ?: return
        if (isBusy.value || isLevelDone.value || isGameOver.value) return

        viewModelScope.launch {
            isBusy.value = true

            // Swap visual change
            val success = engine.swapCandies(r1, c1, r2, c2)
            if (success) {
                _boardState.value = engine.board.map { it.clone() }.toTypedArray()
                delay(180) // swap duration

                // Evaluate matches
                val matchesMade = processMatchesAndRunCascade()
                if (matchesMade) {
                    // Turn finished successfully, decrease move count
                    movesLeft.value = (movesLeft.value - 1).coerceAtLeast(0)
                    evaluateBossAttackStep()
                    checkGameEndConditions()
                } else {
                    // Return swap if no match
                    engine.revertSwap(r1, c1, r2, c2)
                    _boardState.value = engine.board.map { it.clone() }.toTypedArray()
                    addFloatingMessage("Locked!", r1, c1, "#FF5252")
                }
            }
            
            isBusy.value = false
        }
    }

    // Direct purchase of 5 extra moves with gold coins to keep retention extremely rewarding
    fun buyExtraMoves() {
        viewModelScope.launch {
            val user = activeUser.value
            val cost = 250
            if (repository.consumeCoins(user, cost)) {
                movesLeft.value += 5
                isGameOver.value = false
                addFloatingMessage("+5 Moves! ✨ Play ON", 4, 3, "#FFD700")
                SoundManager.playRewardCollection()
            } else {
                addFloatingMessage("Need Coins!", 4, 3, "#FF5252")
                SoundManager.playTone(300.0, 100)
            }
        }
    }

    // Use Hammer Booster in cell (breaks candy and blockers instantly)
    fun useHammerBoosterSelection(r: Int, c: Int) {
        val engine = currentEngine ?: return
        if (isBusy.value || isLevelDone.value || isGameOver.value) return

        viewModelScope.launch {
            isBusy.value = true
            val user = activeUser.value
            val hasInventory = playerState.value.getBoosterCount("hammer") > 0
            if (hasInventory) {
                // Deduct booster
                repository.adjustBooster(user, "hammer", -1)
                
                // Break tile
                engine.useHammerBooster(r, c)
                addFloatingMessage("SMASH!", r, c, "#00E5FF")
                SoundManager.playTntExplosion()
                _boardState.value = engine.board.map { it.clone() }.toTypedArray()
                _obstacleState.value = engine.obstacles.toMap()
                
                delay(300)
                processMatchesAndRunCascade()
            } else {
                addFloatingMessage("Buy Hammer!", r, c, "#FF1744")
                SoundManager.playTone(300.0, 100)
            }
            isBusy.value = false
        }
    }

    // Infinite loop cascading tumbles
    private suspend fun processMatchesAndRunCascade(): Boolean {
        val engine = currentEngine ?: return false
        var anyMatchesInSequence = false
        var cascadeLoopCount = 1

        while (true) {
            val result = engine.processMatchesAndCollapse()
            if (result.matchesFound.isNotEmpty() || result.clearedObstacles.isNotEmpty()) {
                anyMatchesInSequence = true

                // Scoring calculation
                val comboMultiplier = if (cascadeLoopCount > 1) 1.5f else 1.0f
                val addedScore = (result.pointsScored * comboMultiplier).toLong()
                currentScore.value += addedScore

                // Update matches goal progress
                updateGoalItems(result.matchesFound, result.clearedObstacles)

                // Damage math
                if (isBossBattle.value) {
                    val damage = (result.damageToBoss * comboMultiplier).toLong()
                    bossHp.value = (bossHp.value - damage).coerceAtLeast(0)
                    
                    // Show Boss damage floating text near top
                    addFloatingMessage(
                        text = "-$damage",
                        r = 2,
                        c = 3,
                        colorHex = "#FF1744",
                        isDamage = true
                    )
                    
                    // Adjust Boss Phases
                    val currentPct = (bossHp.value.toFloat() / bossMaxHp.value.toFloat()) * 100
                    bossPhase.value = when {
                        currentPct <= 30f -> 3
                        currentPct <= 70f -> 2
                        else -> 1
                    }
                }

                // Add nice score splash pop texts where matches occurred
                if (result.matchesFound.isNotEmpty()) {
                    val anchor = result.matchesFound.first()
                    addFloatingMessage(
                        text = "+$addedScore" + (if (cascadeLoopCount > 1) " (x$cascadeLoopCount combo!)" else ""),
                        r = anchor.first,
                        c = anchor.second,
                        colorHex = "#00FF66"
                    )
                }

                // Push match changes
                _boardState.value = engine.board.map { it.clone() }.toTypedArray()
                _obstacleState.value = engine.obstacles.toMap()
                
                // Keep delay to play nice exploding visual feel
                delay(320)

                // Refill collapse slide
                engine.collapseBoard()
                _boardState.value = engine.board.map { it.clone() }.toTypedArray()
                _obstacleState.value = engine.obstacles.toMap()
                
                delay(280) // tumble delay
                cascadeLoopCount++
            } else {
                break // No matches on this pass! Cascade loop ends.
            }
        }

        // If no matches are made on user turn, spread chocolate obstacle
        if (!anyMatchesInSequence && cascadeLoopCount == 1) {
            val spread = engine.spreadChocolateObstacle()
            if (spread.isNotEmpty()) {
                _obstacleState.value = engine.obstacles.toMap()
                val target = spread.first()
                addFloatingMessage("Choco Spread!", target.first, target.second, "#7D5233")
            }
        }

        return anyMatchesInSequence
    }

    private fun updateGoalItems(exploded: List<Pair<Int, Int>>, brokenBlockers: List<Pair<Int, Int>>) {
        val currentGoals = goalsList.value.map { it.copy() }
        val engine = currentEngine ?: return

        for (goal in currentGoals) {
            // Candy type matching goal
            if (goal.type != null) {
                val matchedOfThisType = exploded.count { boardState.value[it.first][it.second]?.type == goal.type }
                goal.currentCount = (goal.currentCount + matchedOfThisType).coerceAtMost(goal.targetCount)
            }
            // Blocker goal matching
            if (goal.isBlocker) {
                val brokenCount = brokenBlockers.count { cell ->
                    val obstacleBefore = engine.obstacles[cell]
                    obstacleBefore == goal.blockerType
                }
                goal.currentCount = (goal.currentCount + brokenCount).coerceAtMost(goal.targetCount)
            }
            // Score type goal (for boss fights)
            if (goal.type == null && !goal.isBlocker) {
                goal.currentCount = currentScore.value.coerceAtMost(goal.targetCount.toLong()).toInt()
            }
        }

        goalsList.value = currentGoals
    }

    // Timer boss attack countdown logic
    private fun evaluateBossAttackStep() {
        if (!isBossBattle.value) return
        val current = bossAttackCountdown.value - 1
        if (current <= 0) {
            // Trigger Boss special attacks based on active Boss Phases!
            triggerBossSpecialStrike()
            bossAttackCountdown.value = when (bossPhase.value) {
                3 -> 3 // Fast attack rate during phase 3!
                2 -> 4
                else -> 5
            }
        } else {
            bossAttackCountdown.value = current
        }
    }

    private fun triggerBossSpecialStrike() {
        val engine = currentEngine ?: return
        viewModelScope.launch {
            addFloatingMessage("${bossName.value} ATTACKS!", 3, 3, "#FF1744")
            
            // Randomly select 3 cells which are clear of obstacles to spawn barriers based on phase
            val activeCells = mutableListOf<Pair<Int, Int>>()
            while (activeCells.size < bossPhase.value + 1) {
                val r = (3..7).random() // Attack lower boards
                val c = (0..7).random()
                val cell = Pair(r, c)
                if (engine.obstacles[cell] == null && boardState.value[r][c] != null && !activeCells.contains(cell)) {
                    activeCells.add(cell)
                }
                if (activeCells.size >= 10) break // fail-safe
            }

            // Put boss blockers
            val bType = when (bossPhase.value) {
                3 -> ObstacleType.ICE // Freezes targets
                2 -> ObstacleType.CHOCOLATE // sticky chocolate spread
                else -> ObstacleType.CRATE // basic blocker
            }

            for (cell in activeCells) {
                engine.obstacles[cell] = bType
                addFloatingMessage("Blocked!", cell.first, cell.second, "#CFD8DC")
            }

            _obstacleState.value = engine.obstacles.toMap()
            delay(100)
        }
    }

    private fun checkGameEndConditions() {
        // Evaluate stars rating threshold based on score
        val target = activeLevelConfig?.targetScore ?: 10000
        val ratio = currentScore.value.toFloat() / target.toFloat()
        starsAchieved.value = when {
            ratio >= 1.2f -> 3
            ratio >= 0.8f -> 2
            ratio >= 0.4f -> 1
            else -> 0
        }

        // Goals checked
        val goalsDone = goalsList.value.all { it.isCompleted }
        val bossDefeated = if (isBossBattle.value) bossHp.value <= 0L else true

        if (goalsDone && bossDefeated) {
            isLevelDone.value = true
            isBusy.value = false
            saveProgressToDatabase(completed = true)
            SoundManager.playLevelStart() // joyful chime
        } else if (movesLeft.value <= 0) {
            // Moves ended -> loss
            isGameOver.value = true
            isBusy.value = false
            handleLevelFailed()
        }
    }

    // Handles decrementing life upon level failure
    fun handleLevelFailed() {
        viewModelScope.launch {
            val state = playerState.value
            val nextLives = (state.lives - 1).coerceAtLeast(0)
            // If lives was previously full (5), record when the refill countdown starts!
            val startTime = if (state.lives == 5) System.currentTimeMillis() else state.lastLifeRegenTime
            
            repository.savePlayerState(state.copy(
                lives = nextLives,
                lastLifeRegenTime = startTime
            ))
            
            // Satifying visual feedback and failure tune
            SoundManager.playDefeatMelody()
            addFloatingMessage("Life Lost! ❤️ Strike 1.", 4, 3, "#FF1744")
        }
    }

    // Buy 5 full lives for 150 coins
    fun purchaseLifeRefill() {
        viewModelScope.launch {
            val state = playerState.value
            val cost = 150
            if (repository.consumeCoins(state.username, cost)) {
                repository.savePlayerState(state.copy(lives = 5))
                SoundManager.playRewardCollection()
                addFloatingMessage("Hearts Restored! ❤️ Glow ON", 4, 3, "#00FF66")
            } else {
                addFloatingMessage("Need 150 Coins!", 4, 3, "#FF1744")
            }
        }
    }

    // Watch simulated rewarded ad to restore 1 life
    fun watchRewardedAdForLife() {
        viewModelScope.launch {
            isBusy.value = true
            addFloatingMessage("Watching Sweet Ad...", 4, 3, "#FFD54F")
            delay(1500)
            val state = playerState.value
            val nextLives = (state.lives + 1).coerceAtMost(5)
            val nextTime = if (nextLives == 5) 0L else state.lastLifeRegenTime
            
            repository.savePlayerState(state.copy(
                lives = nextLives,
                lastLifeRegenTime = nextTime
            ))
            SoundManager.playRewardCollection()
            addFloatingMessage("+1 Life Restored!", 4, 3, "#00FF66")
            isBusy.value = false
        }
    }

    // Spend an item to completely refill hearts
    fun useLifeItem() {
        viewModelScope.launch {
            val state = playerState.value
            repository.savePlayerState(state.copy(lives = 5))
            SoundManager.playRewardCollection()
            addFloatingMessage("Heart Potion Used!", 4, 3, "#E040FB")
        }
    }

    // Account login / registration core methods
    fun registerNewAccount(uName: String, pWord: String, emoji: String, colorHex: String, country: String, picUri: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (uName.trim().length < 3) {
                onResult(false, "Username too short! (min 3 chars)")
                return@launch
            }
            if (repository.userExists(uName)) {
                onResult(false, "Account name already taken!")
                return@launch
            }
            
            val newState = PlayerState(
                username = uName,
                password = pWord,
                coins = 1000,
                gems = 100,
                avatarEmoji = emoji,
                avatarColorHex = colorHex,
                countryCode = country,
                avatarUri = picUri
            )
            repository.savePlayerState(newState)
            activeUser.value = uName
            onResult(true, "Successfully Registered, Welcome!")
            _currentScreen.value = GameScreen.Home
            SoundManager.playLevelStart()
        }
    }

    fun loginExistingAccount(uName: String, pWord: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val state = repository.getPlayerStateDirect(uName)
            if (state.password == pWord) {
                activeUser.value = uName
                onResult(true, "Successfully Logged In!")
                _currentScreen.value = GameScreen.Home
                SoundManager.playLevelStart()
            } else {
                onResult(false, "Invalid username or password!")
            }
        }
    }

    fun logoutCurrentUser() {
        activeUser.value = "Guest"
        _currentScreen.value = GameScreen.Login
        SoundManager.playDefeatMelody()
    }

    // Ranks and Global Leaderboards math engine
    fun getPlayerCoinRank(coinsCount: Int): Int {
        val baseCoins = listOf(6500, 5200, 4100, 3200, 2500, 1800, 1400, 900, 600, 300)
        return (baseCoins.count { it > coinsCount } + 1)
    }

    fun getPlayerDiamondRank(gemsCount: Int): Int {
        val baseGems = listOf(450, 380, 310, 240, 190, 150, 110, 80, 40, 20)
        return (baseGems.count { it > gemsCount } + 1)
    }

    fun getPlayerWinRank(winsCount: Int): Int {
        val baseWins = listOf(45, 38, 31, 25, 18, 12, 8, 5, 3, 1)
        return (baseWins.count { it > winsCount } + 1)
    }

    fun getPlayerGlobalRank(coinsCount: Int, gemsCount: Int, winsCount: Int): Int {
        val cr = getPlayerCoinRank(coinsCount)
        val dr = getPlayerDiamondRank(gemsCount)
        val wr = getPlayerWinRank(winsCount)
        return ((cr + dr + wr) / 3).coerceAtLeast(1)
    }

    private fun saveProgressToDatabase(completed: Boolean) {
        if (!completed) return
        viewModelScope.launch {
            val user = activeUser.value
            val stars = starsAchieved.value.coerceAtLeast(1)
            val level = currentLevel.value
            val gainedCoins = 100 + level * 10 + (stars * 15)
            val gainedGems = if (isBossBattle.value) 50 else 5

            // Increment event levels completed score if Live Event is active
            val activeStateNow = repository.getPlayerStateDirect(user)
            val nextEventLevelsVal = if (liveEventActive.value) {
                activeStateNow.eventLevelsCompleted + 1
            } else {
                activeStateNow.eventLevelsCompleted
            }

            // Save player update with event status
            repository.savePlayerState(activeStateNow.copy(eventLevelsCompleted = nextEventLevelsVal))

            // Persist with username parameters
            repository.saveLevelStars(user, level, stars, currentScore.value)
            repository.addCoins(user, gainedCoins)
            repository.addGems(user, gainedGems)
            repository.unlockNextLevel(user, level)

            if (isBossBattle.value) {
                repository.markBossDefeated(user, activeLevelConfig?.bossId ?: "")
            }
        }
    }

    // Triggers daily gift claims
    fun claimDailyGift() {
        viewModelScope.launch {
            val user = activeUser.value
            repository.addCoins(user, 350)
            repository.addGems(user, 20)
            repository.adjustBooster(user, "hammer", 1)
            addFloatingMessage("Claimed candy shards!", 4, 3, "#FFD700")
            SoundManager.playRewardCollection()
        }
    }

    // Buys boosters from store using coins
    fun purchaseBooster(type: String, coinsCost: Int) {
        viewModelScope.launch {
            val user = activeUser.value
            if (repository.consumeCoins(user, coinsCost)) {
                repository.adjustBooster(user, type, 1)
                addFloatingMessage("+1 Booster!", 4, 3, "#00E5FF")
                SoundManager.playRewardCollection()
            } else {
                addFloatingMessage("Need Coins!", 4, 3, "#FF5252")
                SoundManager.playTone(300.0, 100)
            }
        }
    }

    // Spend gems to buy coins
    fun purchaseCoinsWithGems(coinsAmount: Int, gemsCost: Int) {
        viewModelScope.launch {
            val user = activeUser.value
            if (repository.consumeGems(user, gemsCost)) {
                repository.addCoins(user, coinsAmount)
                addFloatingMessage("+$coinsAmount Coins!", 4, 3, "#00FF66")
                SoundManager.playRewardCollection()
            } else {
                addFloatingMessage("Need Gems! 💎", 4, 3, "#FF5252")
                SoundManager.playTone(300.0, 100)
            }
        }
    }

    // Resets database data (for debug clearing or easy restarts!)
    fun debugResetAll() {
        viewModelScope.launch {
            val database = AppDatabase.getDatabase(getApplication())
            database.playerDao().clear()
            database.playerDao().savePlayerState(PlayerState())
            _currentScreen.value = GameScreen.Home
        }
    }

    // Helper adds floating combo alerts
    private fun addFloatingMessage(text: String, r: Int, c: Int, colorHex: String, isDamage: Boolean = false) {
        val newText = FloatingText(
            id = textIdCounter++,
            text = text,
            r = r,
            c = c,
            colorHex = colorHex,
            isDamage = isDamage
        )
        _floatingTexts.value = _floatingTexts.value + newText
        
        // Remove text after short delay automatically
        viewModelScope.launch {
            delay(1200)
            _floatingTexts.value = _floatingTexts.value.filter { it.id != newText.id }
        }
    }
}
