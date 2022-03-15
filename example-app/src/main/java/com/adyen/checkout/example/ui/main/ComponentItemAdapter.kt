package com.adyen.checkout.example.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.adyen.checkout.example.databinding.ItemComponentEntryBinding
import com.adyen.checkout.example.databinding.ItemComponentTitleBinding

internal class ComponentItemAdapter(
    private val items: List<ComponentItem>
) :
    RecyclerView.Adapter<ComponentItemAdapter.ComponentItemViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ComponentItem.Title -> VIEW_TYPE_TITLE
        is ComponentItem.Entry -> VIEW_TYPE_ENTRY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComponentItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_TITLE -> TitleViewHolder(ItemComponentTitleBinding.inflate(layoutInflater, parent, false))
            VIEW_TYPE_ENTRY -> EntryViewHolder(ItemComponentEntryBinding.inflate(layoutInflater, parent, false))
            else -> throw NotImplementedError()
        }
    }

    override fun onBindViewHolder(holder: ComponentItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    abstract class ComponentItemViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: ComponentItem)
    }

    private class TitleViewHolder(private val binding: ItemComponentTitleBinding) : ComponentItemViewHolder(binding) {

        override fun bind(item: ComponentItem) {
            binding.title.text = item.text
        }
    }

    private class EntryViewHolder(private val binding: ItemComponentEntryBinding) : ComponentItemViewHolder(binding) {

        override fun bind(item: ComponentItem) {
            binding.title.text = item.text
        }
    }

    companion object {
        private const val VIEW_TYPE_TITLE = 0
        private const val VIEW_TYPE_ENTRY = 1
    }
}
