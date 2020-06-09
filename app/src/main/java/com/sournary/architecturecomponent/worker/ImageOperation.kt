package com.sournary.architecturecomponent.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.*
import com.sournary.architecturecomponent.util.Constant

class ImageOperation private constructor(val workContinuation: WorkContinuation) {

    class Builder(private val context: Context, private val imagePath: String) {

        private var hasBlur = false
        private var hasGrayScale = false
        private var hasWaterColor = false
        private var hasSavedImage = false

        fun applyBlur(blur: Boolean): Builder {
            hasBlur = blur
            return this
        }

        fun applyGrayScale(grayScale: Boolean): Builder {
            hasGrayScale = grayScale
            return this
        }

        fun applyWaterColor(waterColor: Boolean): Builder {
            hasWaterColor = waterColor
            return this
        }

        fun applySavedImage(savedImage: Boolean): Builder {
            hasSavedImage = savedImage
            return this
        }

        @SuppressLint("EnqueueWork")
        fun build(): ImageOperation {
            val data = workDataOf(Constant.KEY_REMOTE_IMAGE to imagePath)
            var hasInputData = false
            var workContinuation = WorkManager.getInstance(context.applicationContext)
                .beginUniqueWork(
                    Constant.IMAGE_FILTER_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequest.from(CleanupWorker::class.java)
                )
            if (hasBlur) {
                val blurWork = OneTimeWorkRequestBuilder<BlurFilterWorker>()
                    .setInputData(data)
                    .build()
                hasInputData = true
                workContinuation = workContinuation.then(blurWork)
            }
            if (hasGrayScale) {
                val grayScaleWorkBuilder = OneTimeWorkRequestBuilder<GrayScaleFilterWorker>()
                if (!hasInputData) {
                    grayScaleWorkBuilder.setInputData(data)
                    hasInputData = true
                }
                val grayScaleWork = grayScaleWorkBuilder.build()
                workContinuation = workContinuation.then(grayScaleWork)
            }
            if (hasWaterColor) {
                val waterColorWorkBuilder = OneTimeWorkRequestBuilder<WaterColorFilterWorker>()
                if (!hasInputData) {
                    waterColorWorkBuilder.setInputData(data)
                    hasInputData = true
                }
                val waterColorWork = waterColorWorkBuilder.build()
                workContinuation = workContinuation.then(waterColorWork)
            }
            if (hasSavedImage) {
                val saveImageWorkBuilder = OneTimeWorkRequestBuilder<SaveImageToGalleryWorker>()
                if (!hasInputData) {
                    saveImageWorkBuilder.setInputData(data)
                }
                val saveImageWork = saveImageWorkBuilder.addTag(Constant.SAVE_IMAGE_TAG).build()
                workContinuation = workContinuation.then(saveImageWork)
            }
            return ImageOperation(workContinuation)
        }

    }

}
