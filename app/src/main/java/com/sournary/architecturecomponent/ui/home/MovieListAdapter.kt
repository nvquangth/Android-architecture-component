package com.sournary.architecturecomponent.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sournary.architecturecomponent.databinding.ItemHomeMovieBinding
import com.sournary.architecturecomponent.ext.setSafeClick
import com.sournary.architecturecomponent.model.Movie
import com.sournary.architecturecomponent.ui.common.BasePagingListAdapter

/**
 * The adapter of movie recycler view.
 */
class MovieListAdapter(
    retry: () -> Unit,
    private val click: (Movie) -> Unit
) : BasePagingListAdapter<Movie, ItemHomeMovieBinding>(Movie.COMPARATOR, retry) {

    override fun createBinding(parent: ViewGroup): ItemHomeMovieBinding =
        ItemHomeMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun bindData(item: Movie, binding: ItemHomeMovieBinding) {
        binding.itemMovieRoot.setSafeClick(View.OnClickListener { click.invoke(item) })
    }

}
