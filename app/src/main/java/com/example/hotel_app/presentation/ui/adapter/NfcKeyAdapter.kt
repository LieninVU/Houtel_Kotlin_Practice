package com.example.hotel_app.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hotel_app.databinding.LayoutItemNfcKeyBinding
import com.example.hotel_app.domain.model.NfcKey

class NfcKeyAdapter : ListAdapter<NfcKey, NfcKeyAdapter.KeyViewHolder>(DiffCallback) {

    class KeyViewHolder(val binding: LayoutItemNfcKeyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyViewHolder {
        val binding = LayoutItemNfcKeyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return KeyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeyViewHolder, position: Int) {
        val key = getItem(position)
        with(holder.binding) {
            tvRoomNumber.text = "ROOM ${key.roomNumber}"
            tvRoomType.text = key.roomType
            tvValidUntil.text = "Valid until: ${key.validUntil}"
            tvLastUsed.text = "Last used: ${key.lastUsed ?: "Never"}"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NfcKey>() {
        override fun areItemsTheSame(oldItem: NfcKey, newItem: NfcKey) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NfcKey, newItem: NfcKey) = oldItem == newItem
    }
}
