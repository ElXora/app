package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.PlayerDao
import com.example.data.model.PlayerState

@Database(entities = [PlayerState::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val appCtx = context.applicationContext
            val finalContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                appCtx.createAttributionContext("default")
            } else {
                appCtx
            }
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    finalContext,
                    AppDatabase::class.java,
                    "candy_kingdom_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
