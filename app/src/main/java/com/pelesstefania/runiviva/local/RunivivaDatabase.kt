package com.pelesstefania.runiviva.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pelesstefania.runiviva.model.LocalRunSession

@Database(
    entities = [LocalRunSession::class],
    version = 1,
    exportSchema = false
)
abstract class RunivivaDatabase : RoomDatabase() {
    abstract fun runDao(): RunDao
}