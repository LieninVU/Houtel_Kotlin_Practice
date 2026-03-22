package com.example.hotel_app.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hotel_app.R
import com.example.hotel_app.databinding.LayoutItemNfcKeyBinding
import com.example.hotel_app.domain.model.NfcKey

class NfcKeyAdapter(
    private val onKeyClick: (NfcKey) -> Unit
) : ListAdapter<NfcKey, NfcKeyAdapter.KeyViewHolder>(DiffCallback) {

    class KeyViewHolder(val binding: LayoutItemNfcKeyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyViewHolder {
        val binding = LayoutItemNfcKeyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return KeyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeyViewHolder, position: Int) {
        val key = getItem(position)
        val context = holder.itemView.context
        with(holder.binding) {
            tvRoomNumber.text = context.getString(R.string.nfc_key_room_format, key.roomNumber)
            tvRoomType.text = key.roomType
            tvValidUntil.text = context.getString(R.string.nfc_key_valid_until_format, key.validUntil)
            val lastUsedText = key.lastUsed ?: context.getString(R.string.nfc_key_last_used_never)
            tvLastUsed.text = context.getString(R.string.nfc_key_last_used_format, lastUsedText)

            root.setOnClickListener { onKeyClick(key) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NfcKey>() {
        override fun areItemsTheSame(oldItem: NfcKey, newItem: NfcKey) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NfcKey, newItem: NfcKey) = oldItem == newItem
    }
}
