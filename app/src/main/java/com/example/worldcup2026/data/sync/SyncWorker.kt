package com.example.worldcup2026.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.worldcup2026.data.local.WorldCupDatabase
import com.example.worldcup2026.data.repository.WorldCupRepository

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = WorldCupDatabase.getDatabase(applicationContext)
            val repository = WorldCupRepository(database.matchDao())
            val success = repository.syncMatchesWithLiveJson(applicationContext)
            if (success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
