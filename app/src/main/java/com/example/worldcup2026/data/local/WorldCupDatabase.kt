package com.example.worldcup2026.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MatchEntity::class], version = 6, exportSchema = false)
abstract class WorldCupDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile
        private var INSTANCE: WorldCupDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Añadir nuevas columnas necesarias para las estadísticas VIP locales e incidencias en vivo
                db.execSQL("ALTER TABLE matches ADD COLUMN scorers TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE matches ADD COLUMN events TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE matches ADD COLUMN vipStats TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE matches ADD COLUMN clock TEXT DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): WorldCupDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorldCupDatabase::class.java,
                    "world_cup_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
