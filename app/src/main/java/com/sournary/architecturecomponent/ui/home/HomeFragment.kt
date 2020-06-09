package com.sournary.architecturecomponent.ui.home

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.chip.Chip
import com.sournary.architecturecomponent.R
import com.sournary.architecturecomponent.databinding.FragmentHomeBinding
import com.sournary.architecturecomponent.ext.autoCleared
import com.sournary.architecturecomponent.ext.hideKeyboard
import com.sournary.architecturecomponent.model.Genre
import com.sournary.architecturecomponent.repository.NetworkState
import com.sournary.architecturecomponent.ui.common.BaseFragment
import com.sournary.architecturecomponent.ui.common.MainViewModel
import com.sournary.architecturecomponent.widget.MovieItemDecoration
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * The Fragment represents home screen.
 */
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    private var adapter: MovieListAdapter by autoCleared()

    private val mainViewModel: MainViewModel by activityViewModels()
    override val viewModel: HomeViewModel by viewModels { HomeViewModelFactory(this) }

    override val layoutId: Int = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnApplyWindowInsetsListener { _, insets ->
            view.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )
            view.updatePadding(top = insets.systemWindowInsetTop)
            movie_recycler.updatePadding(bottom = insets.systemWindowInsetBottom)
            insets
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupSearch()
        setupMovieList()
        setupViewModel()
    }

    private fun setupSearch() {
        search_text_input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                getMoviesFromSearch()
                true
            } else {
                false
            }
        }
        search_text_input.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                getMoviesFromSearch()
                true
            } else {
                false
            }
        }
        search_text_input.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP &&
                event.x <= search_text_input.compoundDrawables[0].bounds.width()
            ) {
                mainViewModel.openNavigation()
                true
            } else {
                false
            }
        }
        genre_group.setOnCheckedChangeListener { _, checkedId ->
            viewModel.checkId = checkedId
        }
    }

    private fun getMoviesFromSearch() {
        hideKeyboard()
        val searchText = search_text_input.text?.trim() ?: return
        viewModel.genres.value?.forEach { genre ->
            if (genre.name == searchText && viewModel.showMoviesOfCategory(genre)) {
                viewModel.shouldRefreshing = false
                movie_recycler.scrollToPosition(0)
            }
        }
    }

    private fun setupMovieList() {
        movie_swipe_refresh.setOnRefreshListener {
            viewModel.shouldRefreshing = true
            viewModel.refreshGetMovies()
        }
        adapter = MovieListAdapter(
            retry = { viewModel.retryGetMovies() },
            click = { movie ->
                val directions = HomeFragmentDirections.navigateToMovieDetail(movie.id)
                navController.navigate(directions)
            }
        )
        movie_recycler.adapter = adapter
        val divider =
            ContextCompat.getDrawable(context ?: return, R.drawable.shape_movie_divider) ?: return
        val horizontalSpacing = resources.getDimensionPixelOffset(R.dimen.dp_16)
        val itemDecoration = MovieItemDecoration(horizontalSpacing, divider)
        movie_recycler.addItemDecoration(itemDecoration)
    }

    private fun setupViewModel() {
        mainViewModel.setLockNavigation(false)
        viewModel.apply {
            movies.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
            networkState.observe(viewLifecycleOwner) {
                adapter.setNetworkState(it)
            }
            refreshState.observe(viewLifecycleOwner) {
                if (shouldRefreshing) {
                    movie_swipe_refresh.isRefreshing = it == NetworkState.LOADING
                }
            }
            genres.observe(viewLifecycleOwner) {
                addGenres(it)
            }
        }
    }

    private fun addGenres(genres: List<Genre>) {
        genre_group.removeAllViews()
        genres.forEachIndexed { index, genre ->
            val chip =
                layoutInflater.inflate(R.layout.layout_genre_item, genre_group, false) as Chip
            chip.text = genre.name
            chip.id = index
            chip.setOnClickListener {
                hideKeyboard()
                chip.requestFocus()
                search_text_input.setText(genre.name)
                if (viewModel.showMoviesOfCategory(genre)) {
                    viewModel.shouldRefreshing = false
                    movie_recycler.scrollToPosition(0)
                }
            }
            genre_group.addView(chip)
        }
        genre_group.check(viewModel.checkId)
    }

}
