package com.example.data.repository

import com.example.data.dao.PlayerDao
import com.example.data.model.PlayerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val playerDao: PlayerDao) {

    fun getPlayerState(username: String): Flow<PlayerState> {
        return playerDao.getPlayerState(username).map { state ->
            state ?: PlayerState(username = username)
        }
    }

    suspend fun getPlayerStateDirect(username: String): PlayerState {
        return playerDao.getPlayerStateDirect(username) ?: PlayerState(username = username)
    }

    suspend fun savePlayerState(state: PlayerState) {
        playerDao.savePlayerState(state)
    }

    suspend fun userExists(username: String): Boolean {
        return playerDao.userExists(username)
    }

    suspend fun getAllPlayersDirect(): List<PlayerState> {
        return playerDao.getAllPlayersDirect()
    }

    suspend fun addCoins(username: String, amount: Int) {
        val current = getPlayerStateDirect(username)
        savePlayerState(current.copy(coins = current.coins + amount))
    }

    suspend fun consumeCoins(username: String, amount: Int): Boolean {
        val current = getPlayerStateDirect(username)
        if (current.coins >= amount) {
            savePlayerState(current.copy(coins = current.coins - amount))
            return true
        }
        return false
    }

    suspend fun addGems(username: String, amount: Int) {
        val current = getPlayerStateDirect(username)
        savePlayerState(current.copy(gems = current.gems + amount))
    }

    suspend fun consumeGems(username: String, amount: Int): Boolean {
        val current = getPlayerStateDirect(username)
        if (current.gems >= amount) {
            savePlayerState(current.copy(gems = current.gems - amount))
            return true
        }
        return false
    }

    suspend fun unlockNextLevel(username: String, completedLevelCount: Int) {
        val current = getPlayerStateDirect(username)
        val nextLevel = completedLevelCount + 1
        if (current.currentLevel < nextLevel) {
            savePlayerState(current.copy(currentLevel = nextLevel))
        }
    }

    suspend fun saveLevelStars(username: String, level: Int, stars: Int, score: Long) {
        val current = getPlayerStateDirect(username)
        val newState = current.withLevelStars(level, stars).copy(
            totalScore = current.totalScore + score
        )
        savePlayerState(newState)
    }

    suspend fun adjustBooster(username: String, type: String, amount: Int) {
        val current = getPlayerStateDirect(username)
        val newState = current.withBoosterAdjusted(type, amount)
        savePlayerState(newState)
    }

    suspend fun markBossDefeated(username: String, bossId: String) {
        val current = getPlayerStateDirect(username)
        val newState = current.withBossDefeated(bossId)
        savePlayerState(newState)
    }
}
