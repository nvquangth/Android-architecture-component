package com.sournary.architecturecomponent.ui.filterimage

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FilterImageViewModelFactory(
    private val application: Application,
    private val initImagePath: String
) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FilterImageViewModel(application, initImagePath) as T
    }

}
