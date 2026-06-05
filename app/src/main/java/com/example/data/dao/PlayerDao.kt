package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PlayerState
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_state WHERE username = :username LIMIT 1")
    fun getPlayerState(username: String): Flow<PlayerState?>

    @Query("SELECT * FROM player_state WHERE username = :username LIMIT 1")
    suspend fun getPlayerStateDirect(username: String): PlayerState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerState(state: PlayerState)

    @Query("SELECT EXISTS(SELECT 1 FROM player_state WHERE username = :username)")
    suspend fun userExists(username: String): Boolean

    @Query("SELECT * FROM player_state")
    suspend fun getAllPlayersDirect(): List<PlayerState>

    @Query("DELETE FROM player_state")
    suspend fun clear()
}
