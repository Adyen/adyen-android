package com.adyen.checkout.example.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.adyen.checkout.example.databinding.ItemComponentEntryBinding
import com.adyen.checkout.example.databinding.ItemComponentTitleBinding

internal class ComponentItemAdapter(
    private val onEntryClick: (ComponentItem.Entry) -> Unit,
) : ListAdapter<ComponentItem, ComponentItemAdapter.ComponentItemViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ComponentItem.Title -> VIEW_TYPE_TITLE
        is ComponentItem.Entry -> VIEW_TYPE_ENTRY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComponentItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_TITLE -> TitleViewHolder(ItemComponentTitleBinding.inflate(layoutInflater, parent, false))
            VIEW_TYPE_ENTRY ->
                EntryViewHolder(ItemComponentEntryBinding.inflate(layoutInflater, parent, false), onEntryClick)
            else -> throw NotImplementedError()
        }
    }

    override fun onBindViewHolder(holder: ComponentItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    abstract class ComponentItemViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: ComponentItem)
    }

    private class TitleViewHolder(private val binding: ItemComponentTitleBinding) : ComponentItemViewHolder(binding) {

        override fun bind(item: ComponentItem) {
            binding.title.setText(item.stringResource)
        }
    }

    private class EntryViewHolder(
        private val binding: ItemComponentEntryBinding,
        private val onEntryClick: (ComponentItem.Entry) -> Unit,
    ) : ComponentItemViewHolder(binding) {

        override fun bind(item: ComponentItem) {
            setTitle(item)
            binding.root.setOnClickListener { onEntryClick(item as ComponentItem.Entry) }
        }

        private fun setTitle(item: ComponentItem) {
            val arguments = item.arguments
            if (arguments == null) {
                binding.title.setText(item.stringResource)
            } else {
                @Suppress("SpreadOperator")
                val title = binding.root.resources.getString(item.stringResource, *arguments.toTypedArray())
                binding.title.text = title
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_TITLE = 0
        private const val VIEW_TYPE_ENTRY = 1
    }

    private object DiffCallback : DiffUtil.ItemCallback<ComponentItem>() {
        override fun areItemsTheSame(oldItem: ComponentItem, newItem: ComponentItem): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: ComponentItem, newItem: ComponentItem): Boolean =
            oldItem == newItem
    }
}
