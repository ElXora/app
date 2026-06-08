package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_state")
data class PlayerState(
    @PrimaryKey val username: String = "Guest",
    val password: String = "",
    val coins: Int = 1000,
    val gems: Int = 100,
    val currentLevel: Int = 1,
    val totalScore: Long = 0,
    val starsData: String = "", // Format: "level:stars,level:stars" (e.g. "1:3,2:2")
    val boostersData: String = "hammer:5,handswap:3,colorbomb:2,striped:3,wrapped:3,fish:3,dupe_bomb:5,tnt_bomb:5,spinner:5", // Format: "booster:count,booster:count"
    val defeatedBossesData: String = "", // Format: "bossId,bossId"
    val lives: Int = 5,
    val lastLifeRegenTime: Long = 0L,
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val avatarColorHex: String = "#FF1744",
    val avatarEmoji: String = "CR",
    val countryCode: String = "US",
    val avatarUri: String = "",
    val eventLevelsCompleted: Int = 0,
    val lastJoinedEventTime: Long = 0L
) {
    // Helper to get stars for level
    fun getStarsForLevel(level: Int): Int {
        if (starsData.isEmpty()) return 0
        return starsData.split(",")
            .mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) {
                    val lvl = parts[0].toIntOrNull()
                    val strs = parts[1].toIntOrNull()
                    if (lvl != null && strs != null) lvl to strs else null
                } else null
            }
            .toMap()[level] ?: 0
    }

    // Helper to set stars for level
    fun withLevelStars(level: Int, stars: Int): PlayerState {
        val currentMap = if (starsData.isEmpty()) emptyMap() else {
            starsData.split(",")
                .mapNotNull {
                    val parts = it.split(":")
                    if (parts.size == 2) {
                        val lvl = parts[0].toIntOrNull()
                        val strs = parts[1].toIntOrNull()
                        if (lvl != null && strs != null) lvl to strs else null
                    } else null
                }.toMap()
        }
        val mutable = currentMap.toMutableMap()
        val oldStars = mutable[level] ?: 0
        if (stars > oldStars) {
            mutable[level] = stars
        }
        val encoded = mutable.map { "${it.key}:${it.value}" }.joinToString(",")
        return this.copy(starsData = encoded)
    }

    // Helper to get booster count
    fun getBoosterCount(type: String): Int {
        if (boostersData.isEmpty()) return 0
        return boostersData.split(",")
            .mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) {
                    val bst = parts[0]
                    val cnt = parts[1].toIntOrNull()
                    if (cnt != null) bst to cnt else null
                } else null
            }
            .toMap()[type] ?: 0
    }

    // Helper to adjust booster
    fun withBoosterAdjusted(type: String, amount: Int): PlayerState {
        val currentMap = if (boostersData.isEmpty()) emptyMap() else {
            boostersData.split(",")
                .mapNotNull {
                    val parts = it.split(":")
                    if (parts.size == 2) {
                        val bst = parts[0]
                        val cnt = parts[1].toIntOrNull()
                        if (cnt != null) bst to cnt else null
                    } else null
                }.toMap()
        }
        val mutable = currentMap.toMutableMap()
        val currentCount = mutable[type] ?: 0
        mutable[type] = (currentCount + amount).coerceAtLeast(0)
        val encoded = mutable.map { "${it.key}:${it.value}" }.joinToString(",")
        return this.copy(boostersData = encoded)
    }

    // Helper to check if boss is defeated
    fun isBossDefeated(bossId: String): Boolean {
        if (defeatedBossesData.isEmpty()) return false
        return defeatedBossesData.split(",").contains(bossId)
    }

    // Helper to mark boss as defeated
    fun withBossDefeated(bossId: String): PlayerState {
        val currentList = if (defeatedBossesData.isEmpty()) emptyList() else defeatedBossesData.split(",")
        if (currentList.contains(bossId)) return this
        val newList = currentList + bossId
        return this.copy(defeatedBossesData = newList.joinToString(","))
    }
}
