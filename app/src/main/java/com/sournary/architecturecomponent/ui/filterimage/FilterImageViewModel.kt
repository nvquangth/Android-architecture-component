package com.sournary.architecturecomponent.ui.filterimage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkManager
import com.sournary.architecturecomponent.util.Constant
import com.sournary.architecturecomponent.worker.ImageOperation

class FilterImageViewModel(application: Application, initImagePath: String) :
    AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    val filterStatus = workManager.getWorkInfosByTagLiveData(Constant.SAVE_IMAGE_TAG)

    private val _imagePath = MutableLiveData(initImagePath)
    val imagePath: LiveData<String> = _imagePath

    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean> = _showProgress

    private val _showSaveToGallery = MutableLiveData(true)
    val showSaveToGallery: LiveData<Boolean> = _showSaveToGallery

    private val _showCancelWork = MutableLiveData<Boolean>()
    val showCancelWork: LiveData<Boolean> = _showCancelWork

    fun filterImage(imageOperation: ImageOperation) {
        imageOperation.workContinuation.enqueue()
    }

    fun cancelFilter() {
        workManager.cancelUniqueWork(Constant.IMAGE_FILTER_WORK_NAME)
    }

    fun setProgressVisibility(show: Boolean) {
        _showProgress.value = show
    }

    fun setSaveToGalleryVisibility(show: Boolean) {
        _showSaveToGallery.value = show
    }

    fun setCancelWorkVisibility(show: Boolean) {
        _showCancelWork.value = show
    }

}
