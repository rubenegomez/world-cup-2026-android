package com.example.worldcup2026.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MatchEntity::class, LeagueEntity::class], version = 9, exportSchema = false)
abstract class WorldCupDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun leagueDao(): LeagueDao

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

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `leagues` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `creatorId` TEXT NOT NULL, `code` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE matches ADD COLUMN tournamentId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE matches ADD COLUMN predictedHomePenalties INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE matches ADD COLUMN predictedAwayPenalties INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE leagues ADD COLUMN tournamentId INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): WorldCupDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorldCupDatabase::class.java,
                    "world_cup_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
