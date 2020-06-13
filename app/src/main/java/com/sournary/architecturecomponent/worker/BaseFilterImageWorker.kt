package com.sournary.architecturecomponent.worker

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sournary.architecturecomponent.ext.getFilterInputBitmap
import com.sournary.architecturecomponent.util.Constant
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * This class is the base class for all filter operator classes.
 */
@Suppress("BlockingMethodInNonBlockingContext")
abstract class BaseFilterImageWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val remoteData: Boolean
        val imagePath = if (inputData.getString(Constant.KEY_REMOTE_IMAGE) != null) {
            remoteData = true
            inputData.getString(Constant.KEY_REMOTE_IMAGE)
        } else {
            remoteData = false
            inputData.getString(Constant.KEY_LOCAL_IMAGE)
        }
        if (imagePath.isNullOrEmpty()) return Result.failure()
        return try {
            val input =
                imagePath.getFilterInputBitmap(applicationContext.contentResolver, remoteData)
            val output = applyFilter(input)
            val outputUri = writeBitmapToFile(output)
            Result.success(workDataOf(Constant.KEY_LOCAL_IMAGE to outputUri.toString()))
        } catch (e: Exception) {
            Result.failure()
        }
    }

    abstract fun applyFilter(input: Bitmap): Bitmap

    /**
     * This method writes a bitmap to the [Context.getFilesDir] + '/' + [Constant.FILTER_IMAGE_DIR]
     * directory. The saved file is formatted in the png image.
     * @param bitmap the [Bitmap] that we need to write.
     * @return a [Uri] to the [Bitmap].
     */
    private fun writeBitmapToFile(bitmap: Bitmap): Uri {
        val name = String.format("filter-output-%s.png", UUID.randomUUID().toString())
        val outputDir = File(applicationContext.filesDir, Constant.FILTER_IMAGE_DIR)
        if (!outputDir.exists()) outputDir.mkdir()
        val outputFile = File(outputDir, name)
        return FileOutputStream(outputFile).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Uri.fromFile(outputFile)
        }
    }

}
