package com.sournary.architecturecomponent.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import com.sournary.architecturecomponent.BR
import com.sournary.architecturecomponent.databinding.ItemNetworkStateBinding
import com.sournary.architecturecomponent.ext.setSafeClick
import com.sournary.architecturecomponent.repository.NetworkState
import java.util.concurrent.Executors

/**
 * The base Recycler's adapter uses data list that uses Paging.
 * Adapter can contain network state (error, loading) layout.
 *
 * @param T the object type that its instances are bound to layout's item.
 * @param B the binding object of item's layout.
 */
abstract class BasePagingListAdapter<T, B : ViewDataBinding>(
    diffUtil: DiffUtil.ItemCallback<T>,
    private val retry: (() -> Unit)? = null
) : PagedListAdapter<T, DataViewHolder<ViewDataBinding>>(
    AsyncDifferConfig.Builder(diffUtil)
        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
        .build()
) {

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DataViewHolder<ViewDataBinding> = when (viewType) {
        NETWORK_STATE_TYPE -> {
            val binding =
                ItemNetworkStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.retryButton.setSafeClick(View.OnClickListener { retry?.invoke() })
            DataViewHolder(binding)
        }
        ITEM_TYPE -> {
            val binding = createBinding(parent)
            DataViewHolder(binding)
        }
        else -> throw IllegalStateException("Unknown view type: $viewType")
    }

    abstract fun createBinding(parent: ViewGroup): B

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: DataViewHolder<ViewDataBinding>, position: Int) {
        when (getItemViewType(position)) {
            NETWORK_STATE_TYPE -> with(holder.binding as ItemNetworkStateBinding) {
                networkState = this@BasePagingListAdapter.networkState
                executePendingBindings()
            }
            ITEM_TYPE -> with(holder.binding as B) {
                val item = getItem(position) ?: return@with
                setVariable(BR.item, item)
                bindData(item, this)
                executePendingBindings()
            }
        }
    }

    /**
     * We need bind other information to item's layout.
     * @param item the object instance is bound to item's layout.
     * @param binding the binding instance of item's layout.
     */
    open fun bindData(item: T, binding: B) {}

    override fun getItemViewType(position: Int): Int =
        if (hasExtraRow() && position == itemCount - 1) {
            NETWORK_STATE_TYPE
        } else {
            ITEM_TYPE
        }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val prevNetworkState = networkState
        val hadExtraRow = hasExtraRow()
        networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hasExtraRow) {
                notifyItemInserted(super.getItemCount())
            } else {
                notifyItemRemoved(super.getItemCount())
            }
        } else if (hasExtraRow && prevNetworkState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    override fun getItemCount(): Int = super.getItemCount() + if (hasExtraRow()) 1 else 0

    private fun hasExtraRow(): Boolean =
        networkState != null && networkState != NetworkState.SUCCESS

    companion object {

        private const val NETWORK_STATE_TYPE = 0
        private const val ITEM_TYPE = 1

    }

}
