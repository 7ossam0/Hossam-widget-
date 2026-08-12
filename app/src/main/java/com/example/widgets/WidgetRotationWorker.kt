package com.example.widgets

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.repository.WidgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WidgetRotationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val repository = WidgetRepository(context)
            val configs = repository.getWidgetConfigById(0) // Warm up
            WidgetManagerHelper.updateAllWidgets(context)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
