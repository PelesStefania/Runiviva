package com.pelesstefania.runiviva.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: RunivivaDatabase? = null

    fun getDatabase(context: Context): RunivivaDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                RunivivaDatabase::class.java,
                "runiviva_db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}