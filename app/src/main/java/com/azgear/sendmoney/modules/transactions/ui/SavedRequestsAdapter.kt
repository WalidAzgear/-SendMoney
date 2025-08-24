package com.azgear.sendmoney.modules.transactions.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.azgear.sendmoney.R
import com.azgear.sendmoney.databinding.ItemSavedRequestBinding
import com.azgear.sendmoney.modules.transactions.data.SavedRequest

class SavedRequestsAdapter(
    private val onViewDetailsClick: (SavedRequest) -> Unit
) : ListAdapter<SavedRequest, SavedRequestsAdapter.SavedRequestViewHolder>(SavedRequestDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedRequestViewHolder {
        val binding: ItemSavedRequestBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_saved_request,
            parent,
            false
        )
        return SavedRequestViewHolder(binding, onViewDetailsClick)
    }
    
    override fun onBindViewHolder(holder: SavedRequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class SavedRequestViewHolder(
        private val binding: ItemSavedRequestBinding,
        private val onViewDetailsClick: (SavedRequest) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(request: SavedRequest) {
            binding.request = request
            binding.viewDetailsButton.setOnClickListener {
                onViewDetailsClick(request)
            }
            binding.executePendingBindings()
        }
    }
    
    private class SavedRequestDiffCallback : DiffUtil.ItemCallback<SavedRequest>() {
        override fun areItemsTheSame(oldItem: SavedRequest, newItem: SavedRequest): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: SavedRequest, newItem: SavedRequest): Boolean {
            return oldItem == newItem
        }
    }
} 