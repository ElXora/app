package com.example.ui.game

import kotlin.random.Random
import kotlin.math.abs

// Data class representing an individual tile's obstacle
enum class ObstacleType {
    NONE,
    CRATE,        // Needs adjacent match to break
    ICE,          // Covers candy, 3 layers of durability cracks
    CHAIN,        // Candy locked, cannot be swapped until cleared
    CHOCOLATE,    // Spreads if not matched, clears when matched
    BARREL,       // Needs adjacent match, 4 layers of durability
    CRYSTAL_CAGE, // Blocks swap, 3 layers of durability
    HONEY         // Sticky coating, 3 layers of durability
}

// Model representing a single candy block on our board
data class CandyItem(
    val id: Long, // Unique stable ID for Compose transition animation
    val type: CandyType,
    val special: CandySpecial = CandySpecial.NONE,
    var isNew: Boolean = false,
    var isExploding: Boolean = false
)

// Active matching goal
data class LevelGoal(
    val type: CandyType?, // null if it's general score or a blocker
    val isBlocker: Boolean = false,
    val blockerType: ObstacleType = ObstacleType.NONE,
    val targetCount: Int,
    var currentCount: Int = 0
) {
    val isCompleted: Boolean get() = currentCount >= targetCount
}

// Holds reproducible configurations for all 10,000+ levels procedurally
data class LevelConfig(
    val levelNumber: Int,
    val worldName: String,
    val movesLimit: Int,
    val targetScore: Int,
    val goals: List<LevelGoal>,
    val allowedCandyTypes: List<CandyType>,
    val defaultObstacles: Map<Pair<Int, Int>, ObstacleType>,
    val isBossBattle: Boolean = false,
    val bossId: String = ""
)

object LevelGenerator {
    fun generate(level: Int): LevelConfig {
        val rand = Random(level.toLong() * 9531)
        
        // Map worlds
        val worldName = when {
            level <= 10 -> "Candy Meadows"
            level <= 20 -> "Chocolate Mountains"
            level <= 30 -> "Jelly Jungle"
            level <= 40 -> "Cookie Desert"
            level <= 50 -> "Marshmallow Valley"
            level <= 60 -> "Ice Cream Glacier"
            level <= 70 -> "Lollipop City"
            level <= 80 -> "Rainbow Kingdom"
            level <= 90 -> "Sugar Volcano"
            else -> "Shadow Candy Castle"
        }

        // Moves
        val moves = 20 + rand.nextInt(16) // 20 - 35 moves

        // Target Score
        val targetScore = 5000 + (level * 1200)

        // Boss Battle every 100 levels
        val isBossBattle = level % 100 == 0
        val bossId = if (isBossBattle) "boss_${level}" else ""

        // Candy count selection
        val colorCount = when {
            level <= 5 -> 4
            level <= 15 -> 5
            level <= 30 -> 6
            else -> 7
        }
        val allTypes = listOf(
            CandyType.RED_JELLYBEAN,
            CandyType.BLUE_GEM,
            CandyType.YELLOW_STAR,
            CandyType.PURPLE_BERRY,
            CandyType.GREEN_CUBE,
            CandyType.ORANGE_STRIPED,
            CandyType.PINK_SWIRL
        )
        val selectedCandies = allTypes.take(colorCount)

        // Goals selection
        val goals = mutableListOf<LevelGoal>()
        if (isBossBattle) {
            // High boss hp represented as target matching score/powerups
            goals.add(LevelGoal(type = null, isBlocker = false, targetCount = targetScore))
        } else {
            // Target specific candies
            val primaryCandy = selectedCandies[rand.nextInt(selectedCandies.size)]
            goals.add(LevelGoal(type = primaryCandy, targetCount = 15 + level * 2))
            
            // Maybe secondary goals
            if (level > 3) {
                val secondaryCandy = selectedCandies.filter { it != primaryCandy }.random(rand)
                goals.add(LevelGoal(type = secondaryCandy, targetCount = 10 + level))
            }
            // Add progressive block clearing goals
            if (level > 8) {
                val bType = when {
                    level < 15 -> listOf(ObstacleType.CRATE, ObstacleType.ICE).random(rand)
                    level < 25 -> listOf(ObstacleType.CRATE, ObstacleType.ICE, ObstacleType.BARREL).random(rand)
                    level < 35 -> listOf(ObstacleType.CRATE, ObstacleType.ICE, ObstacleType.BARREL, ObstacleType.HONEY).random(rand)
                    else -> listOf(ObstacleType.ICE, ObstacleType.BARREL, ObstacleType.CRYSTAL_CAGE, ObstacleType.HONEY).random(rand)
                }
                goals.add(LevelGoal(type = null, isBlocker = true, blockerType = bType, targetCount = (3 + level / 5).coerceAtMost(16)))
            }
        }

        // Procedural obstacles grid placements (Difficulty progression!)
        val obstacles = mutableMapOf<Pair<Int, Int>, ObstacleType>()
        if (level > 5 && !isBossBattle) {
            val count = (3 + rand.nextInt((level / 3 + 4).coerceAtMost(18))).coerceAtMost(22)
            for (i in 0 until count) {
                val r = rand.nextInt(8)
                val c = rand.nextInt(8)
                if (r > 1) { // Avoid top rows
                    val oType = when {
                        level < 10 -> ObstacleType.CRATE
                        level < 20 -> listOf(ObstacleType.CRATE, ObstacleType.ICE).random(rand)
                        level < 30 -> listOf(ObstacleType.CRATE, ObstacleType.ICE, ObstacleType.BARREL).random(rand)
                        level < 40 -> listOf(ObstacleType.CRATE, ObstacleType.ICE, ObstacleType.BARREL, ObstacleType.HONEY).random(rand)
                        level < 50 -> listOf(ObstacleType.CRATE, ObstacleType.ICE, ObstacleType.BARREL, ObstacleType.HONEY, ObstacleType.CHAIN).random(rand)
                        level < 65 -> listOf(ObstacleType.ICE, ObstacleType.BARREL, ObstacleType.HONEY, ObstacleType.CHAIN, ObstacleType.CRYSTAL_CAGE).random(rand)
                        else -> listOf(ObstacleType.ICE, ObstacleType.BARREL, ObstacleType.HONEY, ObstacleType.CHAIN, ObstacleType.CRYSTAL_CAGE, ObstacleType.CHOCOLATE).random(rand)
                    }
                    obstacles[Pair(r, c)] = oType
                }
            }
        } else if (isBossBattle) {
            // Boss battles have intense themed obstacle patterns
            val bossTypeNum = (level / 100) % 10
            // Place 10 obstacles matching the boss layout
            for (row in 4..7) {
                for (col in 1..6) {
                    if ((row + col) % 3 == 0) {
                        val oType = when (bossTypeNum) {
                            1 -> ObstacleType.CHOCOLATE // Chocolate Titan
                            2 -> ObstacleType.ICE       // Frostbite Queen
                            3 -> ObstacleType.CRATE     // Lollipop Dragon
                            4 -> ObstacleType.CHAIN     // Cookie Golem
                            else -> ObstacleType.CRATE
                        }
                        obstacles[Pair(row, col)] = oType
                    }
                }
            }
        }

        return LevelConfig(
            levelNumber = level,
            worldName = worldName,
            movesLimit = moves,
            targetScore = targetScore,
            goals = goals,
            allowedCandyTypes = selectedCandies,
            defaultObstacles = obstacles,
            isBossBattle = isBossBattle,
            bossId = bossId
        )
    }
}

// Matches and explosive cascade results container
data class MatchResult(
    val matchesFound: List<Pair<Int, Int>>,
    val specialCreated: Map<Pair<Int, Int>, Pair<CandyType, CandySpecial>>,
    val clearedObstacles: List<Pair<Int, Int>>,
    val pointsScored: Long,
    val damageToBoss: Long
)

class Match3Engine(private val config: LevelConfig) {
    private var nextId = 1L
    val rows = 8
    val cols = 8

    // We store candies in a flat mutable 2D array coordinates list to simplify state emissions
    var board = Array(rows) { Array<CandyItem?>(cols) { null } }
        private set

    var obstacles = mutableMapOf<Pair<Int, Int>, ObstacleType>()
        private set

    var obstacleDurability = mutableMapOf<Pair<Int, Int>, Int>()
        private set

    init {
        // Initialize obstacles from config and set multi-layer durability counts
        obstacles.putAll(config.defaultObstacles)
        for ((cell, type) in config.defaultObstacles) {
            obstacleDurability[cell] = getInitialDurability(type)
        }
        refillBoardClean()
    }

    fun getInitialDurability(type: ObstacleType): Int {
        return when (type) {
            ObstacleType.BARREL -> 4
            ObstacleType.ICE -> 3
            ObstacleType.CRYSTAL_CAGE -> 3
            ObstacleType.HONEY -> 3
            else -> 1
        }
    }

    // Refills board ensuring no matches exist initially
    private fun refillBoardClean() {
        val rand = Random.Default
        do {
            nextId = 1L
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    board[r][c] = generateRandomCandy(r, c, rand)
                }
            }
        } while (hasMatchesOnBoard())
    }

    private fun generateRandomCandy(row: Int, col: Int, rand: Random): CandyItem {
        // Pick type that doesn't create immediate 3-match with neighbors
        val invalidTypes = mutableSetOf<CandyType>()
        if (col >= 2) {
            val left1 = board[row][col - 1]?.type
            val left2 = board[row][col - 2]?.type
            if (left1 != null && left1 == left2) invalidTypes.add(left1)
        }
        if (row >= 2) {
            val up1 = board[row - 1][col]?.type
            val up2 = board[row - 2][col]?.type
            if (up1 != null && up1 == up2) invalidTypes.add(up1)
        }

        val pool = config.allowedCandyTypes.filter { it !in invalidTypes }
        val type = if (pool.isNotEmpty()) pool.random(rand) else config.allowedCandyTypes.random(rand)

        return CandyItem(
            id = nextId++,
            type = type,
            isNew = true
        )
    }

    private fun hasMatchesOnBoard(): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (c <= cols - 3) {
                    val type = board[r][c]?.type
                    if (type != null && type == board[r][c + 1]?.type && type == board[r][c + 2]?.type) {
                        return true
                    }
                }
                if (r <= rows - 3) {
                    val type = board[r][c]?.type
                    if (type != null && type == board[r + 1][c]?.type && type == board[r + 2][c]?.type) {
                        return true
                    }
                }
            }
        }
        return false
    }

    // Swaps two adjacent cells
    fun swapCandies(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        // Check adjacency
        if (abs(r1 - r2) + abs(c1 - c2) != 1) return false

        // Check if locked in chains
        if (obstacles[Pair(r1, c1)] == ObstacleType.CHAIN || obstacles[Pair(r2, c2)] == ObstacleType.CHAIN) {
            return false // Locked!
        }

        val temp = board[r1][c1]
        board[r1][c1] = board[r2][c2]
        board[r2][c2] = temp
        return true
    }

    // Reverts swap
    fun revertSwap(r1: Int, c1: Int, r2: Int, c2: Int) {
        val temp = board[r1][c1]
        board[r1][c1] = board[r2][c2]
        board[r2][c2] = temp
    }

    // Scans board, returns matches, updates obstacles, refills, returns cascade list
    fun processMatchesAndCollapse(): MatchResult {
        val matchedCells = mutableSetOf<Pair<Int, Int>>()
        val specialCandidates = mutableMapOf<Pair<Int, Int>, Pair<CandyType, CandySpecial>>()
        val clearedBlocks = mutableListOf<Pair<Int, Int>>()
        
        var pointsGained = 0L
        var bossDamageDealt = 0L

        // Scan Horizontal Matches
        for (r in 0 until rows) {
            var c = 0
            while (c < cols) {
                val currentType = board[r][c]?.type
                if (currentType != null && currentType != CandyType.COLOR_BOMB) {
                    var matchLength = 1
                    while (c + matchLength < cols && board[r][c + matchLength]?.type == currentType) {
                        matchLength++
                    }
                    if (matchLength >= 3) {
                        // Mark match cells
                        val lineCells = (c until c + matchLength).map { Pair(r, it) }
                        matchedCells.addAll(lineCells)

                        // Special candy creation logic!
                        if (matchLength == 4) {
                            // Striped candy
                            val targetIndex = lineCells[1]
                            specialCandidates[targetIndex] = Pair(currentType, CandySpecial.STRIPED_HORIZONTAL)
                            pointsGained += 500
                        } else if (matchLength >= 5) {
                            // Color Bomb
                            val targetIndex = lineCells[2]
                            specialCandidates[targetIndex] = Pair(CandyType.COLOR_BOMB, CandySpecial.NONE)
                            pointsGained += 1000
                        }
                        
                        pointsGained += matchLength * 100L
                        bossDamageDealt += when (matchLength) {
                            3 -> 10
                            4 -> 35
                            else -> 100
                        }
                        c += matchLength - 1
                    }
                }
                c++
            }
        }

        // Scan Vertical Matches
        for (c in 0 until cols) {
            var r = 0
            while (r < rows) {
                val currentType = board[r][c]?.type
                if (currentType != null && currentType != CandyType.COLOR_BOMB) {
                    var matchLength = 1
                    while (r + matchLength < rows && board[r + matchLength][c]?.type == currentType) {
                        matchLength++
                    }
                    if (matchLength >= 3) {
                        val lineCells = (r until r + matchLength).map { Pair(it, c) }
                        matchedCells.addAll(lineCells)

                        if (matchLength == 4) {
                            val targetIndex = lineCells[1]
                            specialCandidates[targetIndex] = Pair(currentType, CandySpecial.STRIPED_VERTICAL)
                            pointsGained += 500
                        } else if (matchLength >= 5) {
                            // Color Bomb
                            val targetIndex = lineCells[2]
                            specialCandidates[targetIndex] = Pair(CandyType.COLOR_BOMB, CandySpecial.NONE)
                            pointsGained += 1000
                        }
                        
                        pointsGained += matchLength * 100L
                        bossDamageDealt += when (matchLength) {
                            3 -> 10
                            4 -> 35
                            else -> 100
                        }
                        r += matchLength - 1
                    }
                }
                r++
            }
        }

        // Detect T-shape or L-Shape matches for WRAPPED candy creation
        // A cell is a candidate if it belongs to both vertical and horizontal match lines
        val intersections = matchedCells.filter { cell ->
            val hasHorizMatchedNeighbors = matchedCells.contains(Pair(cell.first, cell.second - 1)) &&
                    matchedCells.contains(Pair(cell.first, cell.second + 1))
            val hasVertMatchedNeighbors = matchedCells.contains(Pair(cell.first - 1, cell.second)) &&
                    matchedCells.contains(Pair(cell.first + 1, cell.second))
            hasHorizMatchedNeighbors && hasVertMatchedNeighbors
        }
        if (intersections.isNotEmpty()) {
            val target = intersections[0]
            val type = board[target.first][target.second]?.type ?: config.allowedCandyTypes.random()
            specialCandidates[target] = Pair(type, CandySpecial.WRAPPED)
            pointsGained += 800
            bossDamageDealt += 80
        }

        // Apply explosion logic of nested specials!
        val cellsToExplode = matchedCells.toMutableList()
        val scannedSpecials = mutableSetOf<Pair<Int, Int>>()
        
        var idx = 0
        while (idx < cellsToExplode.size) {
            val cell = cellsToExplode[idx]
            val candy = board[cell.first][cell.second]
            if (candy != null && cell !in scannedSpecials) {
                scannedSpecials.add(cell)
                
                // Explode Striped (clears row/col)
                if (candy.special == CandySpecial.STRIPED_HORIZONTAL) {
                    bossDamageDealt += 45
                    for (colIdx in 0 until cols) {
                        val otherCell = Pair(cell.first, colIdx)
                        if (otherCell !in cellsToExplode) cellsToExplode.add(otherCell)
                    }
                } else if (candy.special == CandySpecial.STRIPED_VERTICAL) {
                    bossDamageDealt += 45
                    for (rowIdx in 0 until rows) {
                        val otherCell = Pair(rowIdx, cell.second)
                        if (otherCell !in cellsToExplode) cellsToExplode.add(otherCell)
                    }
                }
                
                // Explode Wrapped (double 3x3 blast)
                if (candy.special == CandySpecial.WRAPPED) {
                    bossDamageDealt += 60
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val targetR = cell.first + dr
                            val targetC = cell.second + dc
                            if (targetR in 0 until rows && targetC in 0 until cols) {
                                val otherCell = Pair(targetR, targetC)
                                if (otherCell !in cellsToExplode) cellsToExplode.add(otherCell)
                            }
                        }
                    }
                }
            }
            idx++
        }

        // Damage adjacent blockers/obstacles (with layered durability)
        for (cell in cellsToExplode) {
            // Check adjacent cells for adjacent-breakable obstacles (CRATE, ICE, BARREL, HONEY)
            for (dr in -1..1) {
                for (dc in -1..1) {
                    if (abs(dr) + abs(dc) == 1) { // Orthogonal adjacency
                        val adjR = cell.first + dr
                        val adjC = cell.second + dc
                        if (adjR in 0 until rows && adjC in 0 until cols) {
                            val adjCell = Pair(adjR, adjC)
                            val obsType = obstacles[adjCell] ?: ObstacleType.NONE
                            if (obsType == ObstacleType.CRATE || obsType == ObstacleType.ICE || 
                                obsType == ObstacleType.BARREL || obsType == ObstacleType.HONEY) {
                                damageObstacleAt(adjCell, clearedBlocks)
                                pointsGained += 150
                            }
                        }
                    }
                }
            }
            
            // Core blocker breaking (CHAIN, CRYSTAL_CAGE)
            val directObs = obstacles[cell]
            if (directObs != null) {
                if (directObs == ObstacleType.CHAIN || directObs == ObstacleType.CRYSTAL_CAGE) {
                    damageObstacleAt(cell, clearedBlocks)
                    pointsGained += 200
                }
            }
        }

        // Detonate matching candies and remove them
        for (cell in cellsToExplode) {
            board[cell.first][cell.second] = null
        }

        // Place special candies created back into board
        for ((cell, candyPair) in specialCandidates) {
            val (type, special) = candyPair
            board[cell.first][cell.second] = CandyItem(
                id = nextId++,
                type = type,
                special = special,
                isNew = true
            )
        }

        return MatchResult(
            matchesFound = cellsToExplode,
            specialCreated = specialCandidates,
            clearedObstacles = clearedBlocks,
            pointsScored = pointsGained,
            damageToBoss = bossDamageDealt
        )
    }

    // Performs sliding fall collapse
    fun collapseBoard(): Boolean {
        var shifted = false
        val rand = Random.Default

        // Core gravity loop column by column
        for (c in 0 until cols) {
            // Sliding candies down
            for (r in rows - 1 downTo 0) {
                if (board[r][c] == null) {
                    // Search upwards for first non-null candy to pull down
                    var searchR = r - 1
                    while (searchR >= 0) {
                        if (board[searchR][c] != null) {
                            board[r][c] = board[searchR][c]
                            board[searchR][c] = null
                            shifted = true
                            break
                        }
                        searchR--
                    }
                }
            }

            // Fill empty cells at top of column
            for (r in 0 until rows) {
                if (board[r][c] == null) {
                    board[r][c] = generateRandomCandy(r, c, rand)
                    shifted = true
                }
            }
        }

        return shifted
    }

    // Handles Color Bomb detonation on all matches of specific color
    fun triggerColorBombDetonation(color: CandyType): List<Pair<Int, Int>> {
        val exploded = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c]?.type == color) {
                    exploded.add(Pair(r, c))
                    board[r][c] = null
                }
            }
        }
        return exploded
    }

    // Trigger Hammer booster
    fun useHammerBooster(r: Int, c: Int): Boolean {
        if (r !in 0 until rows || c !in 0 until cols) return false
        board[r][c] = null
        obstacles.remove(Pair(r, c))
        return true
    }

    // Chocolate expansion spread logic at end of turn if NO matches were made
    fun spreadChocolateObstacle(): List<Pair<Int, Int>> {
        val chocoCells = obstacles.filter { it.value == ObstacleType.CHOCOLATE }.keys.toList()
        if (chocoCells.isEmpty()) return emptyList()

        val rand = Random.Default
        val spreadCandidates = mutableListOf<Pair<Int, Int>>()

        for (cell in chocoCells) {
            val directions = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
            for (dir in directions) {
                val nextR = cell.first + dir.first
                val nextC = cell.second + dir.second
                if (nextR in 0 until rows && nextC in 0 until cols) {
                    val target = Pair(nextR, nextC)
                    if (obstacles[target] == null && board[nextR][nextC] != null) {
                        spreadCandidates.add(target)
                    }
                }
            }
        }

        if (spreadCandidates.isNotEmpty()) {
            val chosen = spreadCandidates.random(rand)
            obstacles[chosen] = ObstacleType.CHOCOLATE
            obstacleDurability[chosen] = 1
            return listOf(chosen)
        }
        return emptyList()
    }

    private fun damageObstacleAt(cell: Pair<Int, Int>, clearedBlocks: MutableList<Pair<Int, Int>>) {
        val currentType = obstacles[cell] ?: return
        val currentDur = obstacleDurability[cell] ?: 1
        val nextDur = currentDur - 1
        
        if (nextDur <= 0) {
            obstacles.remove(cell)
            obstacleDurability.remove(cell)
            clearedBlocks.add(cell)
            
            // Satisfying synthesized breaking sound effects
            when (currentType) {
                ObstacleType.ICE -> SoundManager.playSweepTone(800.0, 1100.0, 140, 0.45f)
                ObstacleType.BARREL -> SoundManager.playSweepTone(300.0, 90.0, 220, 0.55f)
                ObstacleType.CRYSTAL_CAGE -> SoundManager.playSweepTone(950.0, 1900.0, 280, 0.5f)
                ObstacleType.HONEY -> SoundManager.playSweepTone(450.0, 180.0, 190, 0.45f)
                else -> SoundManager.playMatch3Pop()
            }
        } else {
            obstacleDurability[cell] = nextDur
            // Progressive denting sound click!
            SoundManager.playTone(380.0 + (nextDur * 100.0), 90, 0.38f)
        }
    }

    // Boost: Dupe Bomb Convert 8 random non-special cells to the most common candy type on the board
    fun runDupeBombBoost() {
        val typeGroups = board.flatten().filterNotNull().filter { it.type != CandyType.COLOR_BOMB }.groupBy { it.type }
        if (typeGroups.isEmpty()) return
        val targetType = typeGroups.maxByOrNull { it.value.size }?.key ?: return

        var converted = 0
        val cells = (0 until rows).flatMap { r -> (0 until cols).map { c -> Pair(r, c) } }.shuffled()
        for (cell in cells) {
            val candy = board[cell.first][cell.second]
            if (candy != null && candy.type != targetType && candy.type != CandyType.COLOR_BOMB && candy.special == CandySpecial.NONE) {
                board[cell.first][cell.second] = candy.copy(type = targetType, isNew = true)
                converted++
                if (converted >= 8) break
            }
        }
        SoundManager.playSweepTone(400.0, 1600.0, 300, 0.6f)
    }

    // Boost: TNT Bomb Explodes a 5x5 grid centered around a random blocker or central board area
    fun runTntBombBoost(): List<Pair<Int, Int>> {
        val targets = mutableListOf<Pair<Int, Int>>()
        // Choose center cell
        val centerR = 4
        val centerC = 4
        for (r in (centerR - 2)..(centerR + 2)) {
            for (c in (centerC - 2)..(centerC + 2)) {
                if (r in 0 until rows && c in 0 until cols) {
                    targets.add(Pair(r, c))
                    board[r][c] = null
                    obstacles.remove(Pair(r, c))
                    obstacleDurability.remove(Pair(r, c))
                }
            }
        }
        SoundManager.playTntExplosion()
        return targets
    }

    // Boost: Spinner Clears 3 random remaining blockers / obstacles on the board
    fun runSpinnerBoost(): List<Pair<Int, Int>> {
        val remainingBlockers = obstacles.keys.toList().shuffled()
        val cleared = remainingBlockers.take(3)
        for (cell in cleared) {
            obstacles.remove(cell)
            obstacleDurability.remove(cell)
        }
        SoundManager.playSweepTone(800.0, 300.0, 250, 0.55f)
        return cleared
    }
}
