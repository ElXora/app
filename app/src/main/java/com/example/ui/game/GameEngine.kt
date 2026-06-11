package com.example.ui.game

import kotlin.random.Random
import kotlin.math.abs

// Data class representing an individual tile's obstacle
enum class ObstacleType {
    NONE,
    CRATE,          // WOODEN CRATE (HP 1-3)
    STONE,          // STONE BLOCK (HP 2-5)
    ICE,            // ICE BLOCK (HP 1-3)
    CHAIN,          // CHAIN (Locks Tile, HP 1)
    MAGIC_BARRIER,  // MAGIC BARRIER (Blocks swapping, HP 1-3)
    CHOCOLATE,      // CHOCOLATE SPREAD (HP 1)
    APPLE,          // GOAL: COLLECT APPLES (HP 1)
    CROWN,          // GOAL: COLLECT CROWNS (HP 1)
    SHIELD,         // GOAL: COLLECT SHIELDS (HP 1)
    CRYSTAL,        // GOAL: COLLECT GEMS (HP 2)
    EGGS,           // GOAL: EGGS (HP 2)
    BEE_HIVE        // GOAL: BEE HIVE (HP 3)
}

// Model representing a single candy block on our board
data class CandyItem(
    val id: Long, // Unique stable ID for Compose transition animation
    val type: CandyType,
    val special: CandySpecial = CandySpecial.NONE,
    var isNew: Boolean = false,
    var isExploding: Boolean = false
)

// Active matching goal - strictly blocker/obstacle targets
data class LevelGoal(
    val type: CandyType? = null, // unused but kept for compatibility
    val isBlocker: Boolean = true,
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
            level <= 110 -> "Shadow Candy Castle"
            level <= 130 -> "Sweet Fantasy Sky"
            else -> "Royal Dessert Palace"
        }

        // Moves
        val moves = (22 + rand.nextInt(10) - (level / 12)).coerceAtLeast(15)

        // Target Score
        val targetScore = 5000 + (level * 1200)

        // Boss Battle every 10 levels
        val isBossBattle = level % 10 == 0
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

        // NO candy goals! Goals are strictly blocker targets.
        val goals = mutableListOf<LevelGoal>()
        if (isBossBattle) {
            // Level 100 Boss Battle style but structured to be progressively easier!
            val barrierCount = when {
                level < 20 -> 4
                level < 40 -> 6
                level < 70 -> 8
                level < 100 -> 10
                else -> 12 // Level 100 uses 12, identical to original but with lighter HP tuning!
            }
            goals.add(LevelGoal(isBlocker = true, blockerType = ObstacleType.MAGIC_BARRIER, targetCount = barrierCount))
        } else {
            // Select 1 to 2 random obstacle types for goals based on level
            val availableObstacles = mutableListOf(ObstacleType.CRATE)
            if (level > 2) availableObstacles.add(ObstacleType.ICE)
            if (level > 4) availableObstacles.add(ObstacleType.CHAIN)
            if (level > 7) availableObstacles.add(ObstacleType.STONE)
            if (level > 10) availableObstacles.add(ObstacleType.MAGIC_BARRIER)
            if (level > 13) {
                availableObstacles.add(ObstacleType.APPLE)
                availableObstacles.add(ObstacleType.CROWN)
            }
            if (level > 16) {
                availableObstacles.add(ObstacleType.SHIELD)
                availableObstacles.add(ObstacleType.CRYSTAL)
            }
            if (level > 20) {
                availableObstacles.add(ObstacleType.EGGS)
                availableObstacles.add(ObstacleType.BEE_HIVE)
                availableObstacles.add(ObstacleType.CHOCOLATE)
            }

            availableObstacles.shuffle(rand)
            
            val goalCount = when {
                level < 5 -> 1
                level < 15 -> 2
                else -> 3
            }
            val selectedGoalsType = availableObstacles.take(goalCount)
            
            for (gType in selectedGoalsType) {
                val baseCount = when (gType) {
                    ObstacleType.CRATE -> 5
                    ObstacleType.STONE -> 4
                    ObstacleType.ICE -> 5
                    ObstacleType.CHAIN -> 4
                    ObstacleType.MAGIC_BARRIER -> 4
                    else -> 4
                }
                val counts = (baseCount + level / 4).coerceAtMost(16)
                goals.add(LevelGoal(isBlocker = true, blockerType = gType, targetCount = counts))
            }
        }

        // Procedural obstacles grid placements (SUMMON EXACTLY target goal counts from starting!)
        val obstacles = mutableMapOf<Pair<Int, Int>, ObstacleType>()
        
        // Find safe positions on rows 2 to 7 to place our goals so they are accessible and do not block top spawning
        val eligibleCells = mutableListOf<Pair<Int, Int>>()
        for (r in 2 until 8) {
            for (c in 0 until 8) {
                eligibleCells.add(Pair(r, c))
            }
        }
        eligibleCells.shuffle(rand)

        var placedIndex = 0
        for (goal in goals) {
            val countToPlace = goal.targetCount
            for (i in 0 until countToPlace) {
                if (placedIndex < eligibleCells.size) {
                    val cell = eligibleCells[placedIndex++]
                    obstacles[cell] = goal.blockerType
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
    val clearedObstacles: List<Pair<Pair<Int, Int>, ObstacleType>>,
    val pointsScored: Long,
    val damageToBoss: Long,
    val explodedCandies: List<CandyItem> = emptyList(),
    val damagedObstacles: List<Pair<Pair<Int, Int>, ObstacleType>> = emptyList(),
    val launchedSpinners: List<Pair<Int, Int>> = emptyList(),
    val detonatedTNTs: List<Pair<Int, Int>> = emptyList()
)

data class HammerResult(
    val candy: CandyItem?,
    val obstacle: ObstacleType?
)

class Match3Engine(private val config: LevelConfig) {
    private var nextId = 1L
    val rows = 8
    val cols = 8

    val forceExplodeCells = mutableSetOf<Pair<Int, Int>>()

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
            ObstacleType.CRATE -> 3       // Wooden Crate has 3 layers (1 chain overlay + 2 wooden boxes)
            ObstacleType.STONE -> 3       // Stone Block has 3 layers 
            ObstacleType.ICE -> 2         // Ice Block has 2 layers
            ObstacleType.CHAIN -> 1       // Chain needs 1 hit to break
            ObstacleType.MAGIC_BARRIER -> 3 // Magic Barrier has 3 layers
            ObstacleType.CHOCOLATE -> 1   // Chocolate has 1 layer
            ObstacleType.APPLE -> 1       // Apples are collectable with 1 adjacent hit!
            ObstacleType.CROWN -> 1       // Crowns collectable with 1 adjacent hit
            ObstacleType.SHIELD -> 1      // Shields collectable with 1 adjacent hit
            ObstacleType.CRYSTAL -> 2     // Crystals have 2 layers
            ObstacleType.EGGS -> 2        // Eggs have 2 layers
            ObstacleType.BEE_HIVE -> 3    // Beehive HP is 3
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
                    val obs = obstacles[Pair(r, c)] ?: ObstacleType.NONE
                    val isSolidBlocker = obs in listOf(
                        ObstacleType.CRATE,
                        ObstacleType.STONE,
                        ObstacleType.MAGIC_BARRIER,
                        ObstacleType.APPLE,
                        ObstacleType.CROWN,
                        ObstacleType.SHIELD,
                        ObstacleType.CRYSTAL,
                        ObstacleType.EGGS,
                        ObstacleType.BEE_HIVE
                    )
                    if (isSolidBlocker) {
                        board[r][c] = null
                    } else {
                        board[r][c] = generateRandomCandy(r, c, rand)
                    }
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

        // Check if locked in blockers of any type, except immune special bombs/spinners!
        val candy1 = board[r1][c1]
        val candy2 = board[r2][c2]
        val isBomb1 = candy1?.special == CandySpecial.TNT || candy1?.special == CandySpecial.SPINNER || candy1?.type == CandyType.COLOR_BOMB
        val isBomb2 = candy2?.special == CandySpecial.TNT || candy2?.special == CandySpecial.SPINNER || candy2?.type == CandyType.COLOR_BOMB

        if (!isBomb1 && !isBomb2) {
            if ((obstacles[Pair(r1, c1)] ?: ObstacleType.NONE) != ObstacleType.NONE ||
                (obstacles[Pair(r2, c2)] ?: ObstacleType.NONE) != ObstacleType.NONE) {
                return false // Swapping is locked for standard candies under any active blocker!
            }
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
        matchedCells.addAll(forceExplodeCells)
        forceExplodeCells.clear()

        val specialCandidates = mutableMapOf<Pair<Int, Int>, Pair<CandyType, CandySpecial>>()
        val clearedBlocks = mutableListOf<Pair<Pair<Int, Int>, ObstacleType>>()
        
        var pointsGained = 0L
        var bossDamageDealt = 0L

        // Scan 2x2 Square Matches
        for (r in 0 until rows - 1) {
            for (c in 0 until cols - 1) {
                val t = board[r][c]?.type
                if (t != null && t != CandyType.COLOR_BOMB) {
                    if (board[r+1][c]?.type == t && board[r][c+1]?.type == t && board[r+1][c+1]?.type == t) {
                        val b00 = obstacles[Pair(r, c)] ?: ObstacleType.NONE
                        val b10 = obstacles[Pair(r+1, c)] ?: ObstacleType.NONE
                        val b01 = obstacles[Pair(r, c+1)] ?: ObstacleType.NONE
                        val b11 = obstacles[Pair(r+1, c+1)] ?: ObstacleType.NONE
                        if (b00 == ObstacleType.NONE && b10 == ObstacleType.NONE && b01 == ObstacleType.NONE && b11 == ObstacleType.NONE) {
                            val sqCells = listOf(Pair(r, c), Pair(r+1, c), Pair(r, c+1), Pair(r+1, c+1))
                            matchedCells.addAll(sqCells)
                            specialCandidates[Pair(r, c)] = Pair(t, CandySpecial.SPINNER)
                            pointsGained += 400
                            bossDamageDealt += 120
                        }
                    }
                }
            }
        }

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
                        val lineCells = (c until c + matchLength).map { Pair(r, it) }
                        matchedCells.addAll(lineCells)

                        if (matchLength == 4) {
                            val targetIndex = lineCells[1]
                            specialCandidates[targetIndex] = Pair(currentType, CandySpecial.SPINNER)
                            pointsGained += 500
                        } else if (matchLength >= 5) {
                            val targetIndex = lineCells[2]
                            specialCandidates[targetIndex] = Pair(CandyType.COLOR_BOMB, CandySpecial.NONE)
                            pointsGained += 1000
                        }
                        
                        pointsGained += matchLength * 100L
                        bossDamageDealt += when (matchLength) {
                            3 -> 30
                            4 -> 100
                            else -> 250
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
                            specialCandidates[targetIndex] = Pair(currentType, CandySpecial.SPINNER)
                            pointsGained += 500
                        } else if (matchLength >= 5) {
                            val targetIndex = lineCells[2]
                            specialCandidates[targetIndex] = Pair(CandyType.COLOR_BOMB, CandySpecial.NONE)
                            pointsGained += 1000
                        }
                        
                        pointsGained += matchLength * 100L
                        bossDamageDealt += when (matchLength) {
                            3 -> 30
                            4 -> 100
                            else -> 250
                        }
                        r += matchLength - 1
                    }
                }
                r++
            }
        }

        // Detect T-shape or L-Shape matches for TNT candy creation
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
            specialCandidates[target] = Pair(type, CandySpecial.TNT)
            pointsGained += 800
            bossDamageDealt += 250
        }

        // Any TNT or Spinner adjacent to any matched cells is also detonated ("breaked by anything or by just sliding")
        if (matchedCells.isNotEmpty()) {
            val adjacentSpecials = mutableSetOf<Pair<Int, Int>>()
            for (matchedCell in matchedCells) {
                val r = matchedCell.first
                val c = matchedCell.second
                val neighbors = listOf(Pair(r - 1, c), Pair(r + 1, c), Pair(r, c - 1), Pair(r, c + 1))
                for (neighbor in neighbors) {
                    val nr = neighbor.first
                    val nc = neighbor.second
                    if (nr in 0 until rows && nc in 0 until cols) {
                        val neighborCandy = board[nr][nc]
                        if (neighborCandy != null && !matchedCells.contains(neighbor) &&
                            (neighborCandy.special == CandySpecial.TNT || neighborCandy.special == CandySpecial.SPINNER)) {
                            adjacentSpecials.add(neighbor)
                        }
                    }
                }
            }
            matchedCells.addAll(adjacentSpecials)
        }

        // Apply explosion logic of nested specials!
        val cellsToExplode = matchedCells.toMutableList()
        val scannedSpecials = mutableSetOf<Pair<Int, Int>>()
        val launchedSpinnersList = mutableListOf<Pair<Int, Int>>()
        val detonatedTNTsList = mutableListOf<Pair<Int, Int>>()
        
        var idx = 0
        while (idx < cellsToExplode.size) {
            val cell = cellsToExplode[idx]
            val candy = board[cell.first][cell.second]
            if (candy != null && cell !in scannedSpecials) {
                scannedSpecials.add(cell)
                
                // Explode Spinner
                if (candy.special == CandySpecial.SPINNER) {
                    bossDamageDealt += 150
                    launchedSpinnersList.add(cell)
                }
                
                // Explode TNT (clears 5x5 zone)
                if (candy.special == CandySpecial.TNT) {
                    bossDamageDealt += 350
                    detonatedTNTsList.add(cell)
                    for (dr in -2..2) {
                        for (dc in -2..2) {
                            val targetR = cell.first + dr
                            val targetC = cell.second + dc
                            if (targetR in 0 until rows && targetC in 0 until cols) {
                                val otherCell = Pair(targetR, targetC)
                                if (otherCell !in cellsToExplode) cellsToExplode.add(otherCell)
                            }
                        }
                    }
                }

                // Explode Color Bomb (triggers cascading color clearing)
                if (candy.type == CandyType.COLOR_BOMB) {
                    bossDamageDealt += 500
                    val availableColors = board.flatten().filterNotNull()
                        .map { it.type }
                        .filter { it != CandyType.COLOR_BOMB && it != CandyType.CHOCO_BALL }
                        .distinct()
                    if (availableColors.isNotEmpty()) {
                        val chosenColor = availableColors.random(Random.Default)
                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                if (board[r][c]?.type == chosenColor) {
                                    val otherCell = Pair(r, c)
                                    if (otherCell !in cellsToExplode) {
                                        cellsToExplode.add(otherCell)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            idx++
        }

        // Damage adjacent blockers/obstacles (with layered durability)
        val damagedBlocks = mutableListOf<Pair<Pair<Int, Int>, ObstacleType>>()
        for (cell in cellsToExplode) {
            for (dr in -1..1) {
                for (dc in -1..1) {
                    if (abs(dr) + abs(dc) == 1) { // Orthogonal adjacency
                        val adjR = cell.first + dr
                        val adjC = cell.second + dc
                        if (adjR in 0 until rows && adjC in 0 until cols) {
                            val adjCell = Pair(adjR, adjC)
                            val obsType = obstacles[adjCell] ?: ObstacleType.NONE
                            if (obsType != ObstacleType.NONE && obsType != ObstacleType.CHAIN) {
                                val isDestroyed = damageObstacleAt(adjCell, clearedBlocks)
                                if (!isDestroyed) {
                                    damagedBlocks.add(Pair(adjCell, obsType))
                                }
                                pointsGained += 150
                            }
                        }
                    }
                }
            }
            
            // Core blocker breaking directly on the matched cell (like CHAIN which locks the item itself)
            val directObs = obstacles[cell] ?: ObstacleType.NONE
            if (directObs == ObstacleType.CHAIN) {
                val isDestroyed = damageObstacleAt(cell, clearedBlocks)
                if (!isDestroyed) {
                    damagedBlocks.add(Pair(cell, directObs))
                }
                pointsGained += 200
            }
        }

        // Detonate matching candies and remove them
        val explodedCandiesList = cellsToExplode.mapNotNull { board[it.first][it.second] }

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

        // Incorporate cleared blockers & crowns into boss damage
        // Collect Crowns = 50 HP, Destroy Blockers = 25 HP
        for (tuple in clearedBlocks) {
            val obsType = tuple.second
            bossDamageDealt += if (obsType == ObstacleType.CROWN) 50 else 25
        }

        return MatchResult(
            matchesFound = cellsToExplode,
            specialCreated = specialCandidates,
            clearedObstacles = clearedBlocks,
            pointsScored = pointsGained,
            damageToBoss = bossDamageDealt,
            explodedCandies = explodedCandiesList,
            damagedObstacles = damagedBlocks,
            launchedSpinners = launchedSpinnersList,
            detonatedTNTs = detonatedTNTsList
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

    // Swapping Color Bomb with adjacent item detonations
    fun detonateColorBombSwap(
        r1: Int, c1: Int,
        r2: Int, c2: Int,
        candy1: CandyItem?,
        candy2: CandyItem?
    ): MatchResult {
        val explodedPoints = mutableListOf<Pair<Int, Int>>()
        val clearedBlocks = mutableListOf<Pair<Pair<Int, Int>, ObstacleType>>()
        val explodedCandies = mutableListOf<CandyItem>()
        var pointsGained = 0L

        // Find which is the Color Bomb and which is the target candy color
        val targetColor: CandyType? = if (candy1?.type == CandyType.COLOR_BOMB) {
            candy2?.type
        } else if (candy2?.type == CandyType.COLOR_BOMB) {
            candy1?.type
        } else {
            null
        }

        // Since swapCandies has already been executed on the board:
        // candy1 (which was at r1, c1) is now currently at r2, c2 on the board
        // candy2 (which was at r2, c2) is now currently at r1, c1 on the board
        
        val posOfCandy1 = Pair(r2, c2)
        val posOfCandy2 = Pair(r1, c1)

        // Explode the Color Bomb cells and the candies they were swapped with
        if (candy1?.type == CandyType.COLOR_BOMB) {
            explodedPoints.add(posOfCandy1) // where Color Bomb is now
            explodedCandies.add(candy1)
            board[posOfCandy1.first][posOfCandy1.second] = null

            explodedPoints.add(posOfCandy2) // where target item candy2 is now
            if (candy2 != null) {
                explodedCandies.add(candy2)
                board[posOfCandy2.first][posOfCandy2.second] = null
                pointsGained += 200
            }
        } else if (candy2?.type == CandyType.COLOR_BOMB) {
            explodedPoints.add(posOfCandy2) // where Color Bomb is now
            explodedCandies.add(candy2)
            board[posOfCandy2.first][posOfCandy2.second] = null

            explodedPoints.add(posOfCandy1) // where target item candy1 is now
            if (candy1 != null) {
                explodedCandies.add(candy1)
                board[posOfCandy1.first][posOfCandy1.second] = null
                pointsGained += 200
            }
        }

        // Clear target candies on the entire board by replacing them with special TNTs first and detonating them
        if (targetColor != null) {
            val targetsToReplace = mutableListOf<Pair<Int, Int>>()
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val tile = board[r][c]
                    if (tile?.type == targetColor) {
                        targetsToReplace.add(Pair(r, c))
                    }
                }
            }

            for (pos in targetsToReplace) {
                val r = pos.first
                val c = pos.second
                val tile = board[r][c] ?: continue

                // 1. Temporarily replace the target candy with a gorgeous TNT Special candy (Electro bomb effect)
                val upgradedCandy = tile.copy(special = CandySpecial.TNT)
                board[r][c] = upgradedCandy

                if (pos !in explodedPoints) {
                    explodedPoints.add(pos)
                    explodedCandies.add(upgradedCandy)
                }

                // 2. Explode adjacent area around each replaced candy, damaging blockers!
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        val tr = r + dr
                        val tc = c + dc
                        if (tr in 0 until rows && tc in 0 until cols) {
                            val neighbor = Pair(tr, tc)
                            val neighborCandy = board[tr][tc]
                            if (neighborCandy != null && neighbor !in explodedPoints) {
                                explodedPoints.add(neighbor)
                                explodedCandies.add(neighborCandy)
                                board[tr][tc] = null
                                pointsGained += 150
                            }
                            // Damage blockers/goals in adjacent zones
                            damageObstacleAt(neighbor, clearedBlocks)
                        }
                    }
                }

                board[r][c] = null
                pointsGained += 300
            }
        } else {
            // Both are color bombs! Trigger full-board clear of candies
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val tile = board[r][c]
                    if (tile != null) {
                        explodedPoints.add(Pair(r, c))
                        explodedCandies.add(tile)
                        board[r][c] = null
                        pointsGained += 150
                    }
                }
            }
        }

        return MatchResult(
            matchesFound = explodedPoints,
            specialCreated = emptyMap(),
            clearedObstacles = clearedBlocks,
            pointsScored = pointsGained,
            damageToBoss = pointsGained / 4,
            explodedCandies = explodedCandies
        )
    }

    // Trigger Hammer booster
    fun useHammerBooster(r: Int, c: Int): HammerResult {
        if (r !in 0 until rows || c !in 0 until cols) return HammerResult(null, null)
        val candy = board[r][c]
        val obstacle = obstacles[Pair(r, c)]
        
        board[r][c] = null
        obstacles.remove(Pair(r, c))
        obstacleDurability.remove(Pair(r, c))
        
        return HammerResult(candy, obstacle)
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

    fun damageCellDirect(r: Int, c: Int): MatchResult {
        val explodedPoints = mutableListOf<Pair<Int, Int>>()
        val clearedBlocks = mutableListOf<Pair<Pair<Int, Int>, ObstacleType>>()
        val explodedCandies = mutableListOf<CandyItem>()
        var pointsGained = 0L
        
        val cell = Pair(r, c)
        if (r in 0 until rows && c in 0 until cols) {
            val candy = board[r][c]
            if (candy != null) {
                board[r][c] = null
                explodedPoints.add(cell)
                explodedCandies.add(candy)
                pointsGained += 100L
            }
            
            if (obstacles.containsKey(cell)) {
                damageObstacleAt(cell, clearedBlocks)
                pointsGained += 200L
            }
        }
        
        return MatchResult(
            matchesFound = explodedPoints,
            specialCreated = emptyMap(),
            clearedObstacles = clearedBlocks,
            pointsScored = pointsGained,
            damageToBoss = pointsGained / 4,
            explodedCandies = explodedCandies
        )
    }

    private fun damageObstacleAt(cell: Pair<Int, Int>, clearedBlocks: MutableList<Pair<Pair<Int, Int>, ObstacleType>>): Boolean {
        val currentType = obstacles[cell] ?: return false
        val currentDur = obstacleDurability[cell] ?: 1
        val nextDur = currentDur - 1
        
        if (nextDur <= 0) {
            obstacles.remove(cell)
            obstacleDurability.remove(cell)
            clearedBlocks.add(Pair(cell, currentType))
            
            // Play physical destruction sound effects
            when (currentType) {
                ObstacleType.CRATE -> SoundManager.playWoodBreak()
                ObstacleType.STONE -> SoundManager.playStoneBreak()
                ObstacleType.ICE -> SoundManager.playIceBreak()
                ObstacleType.CHAIN -> SoundManager.playChainBreak()
                ObstacleType.MAGIC_BARRIER -> SoundManager.playMagicBreak()
                ObstacleType.APPLE, ObstacleType.CROWN, ObstacleType.SHIELD -> SoundManager.playGoalChime()
                else -> SoundManager.playMatch3Pop()
            }
            return true
        } else {
            obstacleDurability[cell] = nextDur
            // Play hit sounds based on obstacle type and current layers
            when (currentType) {
                ObstacleType.CRATE -> {
                    if (currentDur == 3) {
                        SoundManager.playChainBreak()
                    } else {
                        SoundManager.playWoodHit()
                    }
                }
                ObstacleType.STONE -> SoundManager.playStoneHit()
                ObstacleType.ICE -> SoundManager.playIceHit()
                ObstacleType.CHAIN -> SoundManager.playChainHit()
                ObstacleType.MAGIC_BARRIER -> SoundManager.playMagicHit()
                else -> SoundManager.playSoftClick()
            }
            return false
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

    // Direct zone clearing (e.g. for TNT spinner landing)
    fun damageZoneDirect(r: Int, c: Int, radius: Int = 1): MatchResult {
        val explodedPoints = mutableListOf<Pair<Int, Int>>()
        val clearedBlocks = mutableListOf<Pair<Pair<Int, Int>, ObstacleType>>()
        val explodedCandies = mutableListOf<CandyItem>()
        var pointsGained = 0L

        for (dr in -radius..radius) {
            for (dc in -radius..radius) {
                val targetR = r + dr
                val targetC = c + dc
                if (targetR in 0 until rows && targetC in 0 until cols) {
                    val cell = Pair(targetR, targetC)
                    val candy = board[targetR][targetC]
                    if (candy != null) {
                        board[targetR][targetC] = null
                        if (cell !in explodedPoints) {
                            explodedPoints.add(cell)
                            explodedCandies.add(candy)
                            pointsGained += 100L
                        }
                    }
                    if (obstacles.containsKey(cell)) {
                        damageObstacleAt(cell, clearedBlocks)
                        pointsGained += 200L
                    }
                }
            }
        }

        return MatchResult(
            matchesFound = explodedPoints,
            specialCreated = emptyMap(),
            clearedObstacles = clearedBlocks,
            pointsScored = pointsGained,
            damageToBoss = pointsGained / 4,
            explodedCandies = explodedCandies
        )
    }

    // Find any potential match-3 trigger swap for user hints
    fun findPossibleSwipeMatch(): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
        val directions = listOf(Pair(0, 1), Pair(1, 0)) // Right and Down swaps
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                for (dir in directions) {
                    val nr = r + dir.first
                    val nc = c + dir.second
                    if (nr in 0 until rows && nc in 0 until cols) {
                        val c1 = board[r][c]
                        val c2 = board[nr][nc]
                        if (c1 != null && c2 != null) {
                            // Run the trial swap
                            board[r][c] = c2
                            board[nr][nc] = c1
                            
                            val hasMatch = hasAnyMatchOnBoard()
                            
                            // Restore back immediately
                            board[r][c] = c1
                            board[nr][nc] = c2
                            
                            if (hasMatch) {
                                return Pair(Pair(r, c), Pair(nr, nc))
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun hasAnyMatchOnBoard(): Boolean {
        // Horizontal checker
        for (r in 0 until rows) {
            var matchLen = 1
            for (c in 1 until cols) {
                val current = board[r][c]
                val prev = board[r][c - 1]
                if (current != null && prev != null && current.type == prev.type && current.type != CandyType.COLOR_BOMB) {
                    matchLen++
                    if (matchLen >= 3) return true
                } else {
                    matchLen = 1
                }
            }
        }
        // Vertical checker
        for (c in 0 until cols) {
            var matchLen = 1
            for (r in 1 until rows) {
                val current = board[r][c]
                val prev = board[r - 1][c]
                if (current != null && prev != null && current.type == prev.type && current.type != CandyType.COLOR_BOMB) {
                    matchLen++
                    if (matchLen >= 3) return true
                } else {
                    matchLen = 1
                }
            }
        }
        return false
    }
}
