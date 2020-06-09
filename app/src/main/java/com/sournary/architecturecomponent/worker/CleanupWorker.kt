package com.sournary.architecturecomponent.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sournary.architecturecomponent.util.Constant
import java.io.File

/**
 * This class cleans all file in filter image directory before we starts filtering image.
 */
class CleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val outputDir = File(applicationContext.filesDir, Constant.FILTER_IMAGE_DIR)
            if (!outputDir.exists()) return Result.success()
            val files = outputDir.listFiles()
            if (files.isNullOrEmpty()) return Result.success()
            // Before start filtering, delete all png images.
            files.forEach {
                if (it.name.endsWith(".png")) it.delete()
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}
