package com.sournary.architecturecomponent.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.sournary.architecturecomponent.model.Movie
import com.sournary.architecturecomponent.api.MovieListResponse
import com.sournary.architecturecomponent.util.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The data source fetches movies from server by using Paging and Coroutine.
 *
 * @param block A suspending function fetches movies and return movie response.
 * @param scope A coroutine scope that the fetched job should run on.
 */
class MovieDataSource(
    private val block: suspend (Int) -> MovieListResponse,
    private val scope: CoroutineScope
) : PageKeyedDataSource<Int, Movie>() {

    private var retry: (() -> Any)? = null

    private val _networkState = MutableLiveData<NetworkState>()
    val networkState: LiveData<NetworkState>
        get() = _networkState

    private val _refreshState = MutableLiveData<NetworkState>()
    val refreshState: LiveData<NetworkState>
        get() = _refreshState

    fun retryWhenAllFailed() {
        scope.launch {
            val prevRetry = retry
            retry = null
            prevRetry?.invoke()
        }
    }

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Movie>
    ) {
        scope.launch {
            try {
                _networkState.postValue(NetworkState.LOADING)
                _refreshState.postValue(NetworkState.LOADING)
                val response = block.invoke(1)
                val movies = response.results ?: emptyList()
                retry = null
                _networkState.postValue(NetworkState.SUCCESS)
                _refreshState.postValue(NetworkState.SUCCESS)
                callback.onResult(movies, null, 2)
            } catch (e: Exception) {
                val error = NetworkState.error(e.message ?: Constant.DEF_ERROR)
                _networkState.postValue(error)
                _refreshState.postValue(error)
                retry = { loadInitial(params, callback) }
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Movie>) {
        scope.launch {
            try {
                _networkState.value = NetworkState.LOADING
                val response = block.invoke(params.key)
                val movies = response.results ?: emptyList()
                retry = null
                val nextKey = if (params.key == response.totalPage) null else params.key + 1
                _networkState.postValue(NetworkState.SUCCESS)
                callback.onResult(movies, nextKey)
            } catch (e: Exception) {
                _networkState.postValue(NetworkState.error(e.message ?: Constant.DEF_ERROR))
                retry = { loadAfter(params, callback) }
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Movie>) {
        // Ignored
    }

    class Factory(
        private val block: suspend (Int) -> MovieListResponse,
        private val scope: CoroutineScope
    ) : DataSource.Factory<Int, Movie>() {

        private val _sourceLiveData = MutableLiveData<MovieDataSource>()
        val sourceLiveData: LiveData<MovieDataSource>
            get() = _sourceLiveData

        override fun create(): DataSource<Int, Movie> {
            val source = MovieDataSource(block, scope)
            _sourceLiveData.postValue(source)
            return source
        }

    }

}
