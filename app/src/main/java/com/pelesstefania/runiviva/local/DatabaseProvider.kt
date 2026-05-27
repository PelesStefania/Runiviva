package com.pelesstefania.runiviva.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    @Volatile
    private var INSTANCE: RunivivaDatabase? = null

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS calendar_day_statuses (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    date TEXT NOT NULL,
                    status TEXT NOT NULL,
                    isSynced INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    fun getDatabase(context: Context): RunivivaDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                RunivivaDatabase::class.java,
                "runiviva_db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()

            INSTANCE = instance
            instance
        }
    }
}