package com.sournary.architecturecomponent.ui.filterimage

import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.sournary.architecturecomponent.R
import com.sournary.architecturecomponent.databinding.FragmentFilterImageBinding
import com.sournary.architecturecomponent.ui.common.BaseFragment
import com.sournary.architecturecomponent.worker.ImageOperation
import kotlinx.android.synthetic.main.fragment_filter_image.*

class FilterImageFragment : BaseFragment<FragmentFilterImageBinding, FilterImageViewModel>() {

    private var imagePath = ""

    override val viewModel: FilterImageViewModel by viewModels {
        FilterImageViewModelFactory(activity!!.application, imagePath)
    }

    override val layoutId: Int = R.layout.fragment_filter_image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val safeArgs: FilterImageFragmentArgs by navArgs()
        imagePath = safeArgs.url
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val initButtonMargin = resources.getDimension(R.dimen.dp_16).toInt()
        view.setOnApplyWindowInsetsListener { _, insets ->
            view.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )
            saveToGalleryButton.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = initButtonMargin + insets.systemWindowInsetBottom
            }
            cancelWorkButton.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = initButtonMargin + insets.systemWindowInsetBottom
            }
            toolbar.updateLayoutParams<CollapsingToolbarLayout.LayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            insets
        }
        NavigationUI.setupWithNavController(collapsingToolbar, toolbar, navController)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        setupViewModel()
        setupEvents()
    }

    private fun setupViewModel() {
        viewModel.filterStatus.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) return@Observer
            // We only care about the one output status.
            // Every continuation has only one worker tagged SAVE_IMAGE_TAG
            val info = it[0]
            val finished = info.state.isFinished
            if (!finished){
                viewModel.setProgressVisibility(true)
                viewModel.setCancelWorkVisibility(true)
                viewModel.setSaveToGalleryVisibility(false)
            }else {
                viewModel.setProgressVisibility(false)
                viewModel.setCancelWorkVisibility(false)
                viewModel.setSaveToGalleryVisibility(true)
            }
        })
    }

    private fun setupEvents() {
        saveToGalleryButton.setOnClickListener {
            val imageOperation = ImageOperation.Builder(context!!, imagePath)
                .applyBlur(blurCheck.isChecked)
                .applyGrayScale(grayScaleCheck.isChecked)
                .applyWaterColor(waterColorCheck.isChecked)
                .applySavedImage(saveToGalleryRadio.isChecked)
                .build()
            viewModel.filterImage(imageOperation)
        }
    }

}
