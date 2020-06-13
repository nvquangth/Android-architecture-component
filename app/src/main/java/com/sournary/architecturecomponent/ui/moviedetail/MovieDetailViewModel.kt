package com.sournary.architecturecomponent.ui.moviedetail

import androidx.lifecycle.*
import com.sournary.architecturecomponent.BuildConfig
import com.sournary.architecturecomponent.repository.MovieRepository

/**
 * The view model class contains all logic of movie detail screen.
 */
class MovieDetailViewModel(private val movieId: Int, movieRepository: MovieRepository) :
    ViewModel() {

    private val _movieId = MutableLiveData(movieId)
    private val movieRepoResult = _movieId.switchMap { movieRepository.getMovieDetail(movieId) }
    val movie = movieRepoResult.map { it.data }
    val movieNetworkState = movieRepoResult.map { it.networkState }

    private val relatedMoviesRepoResult = movieRepository.getRatedMovies(viewModelScope, movieId)
    val relatedMovies = relatedMoviesRepoResult.data
    val relatedMovieState = relatedMoviesRepoResult.networkState

    fun retryGetRelatedMovies() {
        relatedMoviesRepoResult.retry?.invoke()
    }

    fun retryGetMovie() {
        _movieId.value = movieId
        relatedMoviesRepoResult.retry?.invoke()
    }

    fun launchFilterImage(action: (String) -> Unit): Boolean {
        val posterPath = movie.value?.posterPath ?: return false
        val imagePath = BuildConfig.BASE_IMAGE_URL + posterPath
        action.invoke(imagePath)
        return true
    }

}
