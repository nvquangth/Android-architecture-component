package com.sournary.architecturecomponent.worker

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sournary.architecturecomponent.ext.getFilterInputBitmap
import com.sournary.architecturecomponent.util.Constant
import java.text.SimpleDateFormat
import java.util.*

class SaveImageToGalleryWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun doWork(): Result {
        return try {
            val resolver = applicationContext.contentResolver
            val isRemote: Boolean
            val imagePath = if (inputData.getString(Constant.KEY_LOCAL_IMAGE) != null) {
                isRemote = false
                inputData.getString(Constant.KEY_LOCAL_IMAGE)
            } else {
                isRemote = true
                inputData.getString(Constant.KEY_REMOTE_IMAGE)
            }
            if (imagePath.isNullOrEmpty()) return Result.failure()
            // Get output bitmap.
            val output = imagePath.getFilterInputBitmap(resolver, isRemote)
            val outputPath = saveImage(resolver, output)
            Result.success(workDataOf(Constant.KEY_OUTPUT_IMAGE to outputPath))
        } catch (e: Exception) {
            Result.failure()
        }
    }

    @Suppress("DEPRECATION")
    private fun saveImage(resolver: ContentResolver, bitmap: Bitmap): String? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.insertImage(
                resolver,
                bitmap,
                Constant.SAVED_GALLERY_IMAGE_NAME,
                getImageDescription()
            )
        } else {
            // On Android 10, the external storage enables scoped storage.
            val imageCollection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val imageValue = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, Constant.SAVED_GALLERY_IMAGE_NAME)
                put(MediaStore.Images.Media.DESCRIPTION, getImageDescription())
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val imageUri = resolver.insert(imageCollection, imageValue) ?: return null
            resolver.openOutputStream(imageUri).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            imageValue.clear()
            imageValue.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageUri, imageValue, null, null)
            imageUri.toString()
        }
    }

    private fun getImageDescription(): String {
        val dateFormatter = SimpleDateFormat(Constant.FORMAT_OUTPUT_TIME, Locale.getDefault())
        return dateFormatter.format(Date())
    }

}
