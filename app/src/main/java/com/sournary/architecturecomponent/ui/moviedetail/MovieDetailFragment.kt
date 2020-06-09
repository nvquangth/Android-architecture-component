package com.sournary.architecturecomponent.ui.moviedetail

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.sournary.architecturecomponent.R
import com.sournary.architecturecomponent.databinding.FragmentMovieDetailBinding
import com.sournary.architecturecomponent.ext.autoCleared
import com.sournary.architecturecomponent.ui.common.BaseFragment
import com.sournary.architecturecomponent.ui.common.MainViewModel
import com.sournary.architecturecomponent.util.EdgeToEdge
import kotlinx.android.synthetic.main.fragment_movie_detail.*
import kotlinx.android.synthetic.main.layout_network_state.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * The class represent movie detail screen.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class MovieDetailFragment : BaseFragment<FragmentMovieDetailBinding, MovieDetailViewModel>() {

    private var movieId: Int = 0

    private var relatedMovieAdapter: RelatedMovieAdapter by autoCleared()

    private val mainViewModel: MainViewModel by activityViewModels()
    override val viewModel: MovieDetailViewModel by viewModels { MovieDetailViewModelFactory(movieId) }

    override val layoutId: Int = R.layout.fragment_movie_detail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val safeArgs: MovieDetailFragmentArgs by navArgs()
        movieId = safeArgs.movieId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnApplyWindowInsetsListener { _, insets ->
            EdgeToEdge.passWindowInsetsToChildrenRegularLayout(view as ViewGroup, insets)
            view.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )
            networkStateLayout.updatePadding(top = insets.systemWindowInsetTop)
            movieDetailScroll.updatePadding(bottom = insets.systemWindowInsetBottom)
            toolbar.updateLayoutParams<CollapsingToolbarLayout.LayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            statusBarScrim.updateLayoutParams<CollapsingToolbarLayout.LayoutParams> {
                height = insets.systemWindowInsetTop
            }
            toolbarScrim.updateLayoutParams<CollapsingToolbarLayout.LayoutParams> {
                topMargin = insets.systemWindowInsetTop
            }
            insets
        }
        NavigationUI.setupWithNavController(collapsingToolbar, toolbar, navController)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        setupRelatedMovieList()
        setupViewModel()
        setupEvents()
    }

    private fun setupRelatedMovieList() {
        relatedMovieAdapter = RelatedMovieAdapter { viewModel.retryGetRelatedMovies() }
        relatedMovieList.adapter = relatedMovieAdapter
    }

    private fun setupViewModel() {
        mainViewModel.setLockNavigation(true)
        viewModel.apply {
            relatedMovies.observe(viewLifecycleOwner) {
                relatedMovieAdapter.submitList(it)
            }
            relatedMovieState?.observe(viewLifecycleOwner) {
                relatedMovieAdapter.setNetworkState(it)
            }
        }
    }

    private fun setupEvents() {
        errorCloseImage.setOnClickListener {
            navController.popBackStack()
        }
        retryButton.setOnClickListener {
            viewModel.retryGetMovie()
        }
        visitSiteButton.setOnClickListener {
            viewModel.launchWebsite { title, url ->
                val directions = MovieDetailFragmentDirections.navigateToWebsite(title, url)
                navController.navigate(directions)
            }
        }
        toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.filter_image_dest ->  viewModel.launchFilterImage { url ->
                    val directions = MovieDetailFragmentDirections.navigateToFilterImage(url)
                    navController.navigate(directions)
                }
                else -> false
            }
        }
    }

}
