package com.example.hotel_app.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hotel_app.databinding.LayoutItemRoomBinding
import com.example.hotel_app.domain.model.Room

class RoomAdapter(
    private val onRoomClick: (Room) -> Unit
) : ListAdapter<Room, RoomAdapter.RoomViewHolder>(DiffCallback) {

    class RoomViewHolder(val binding: LayoutItemRoomBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = LayoutItemRoomBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = getItem(position)
        with(holder.binding) {
            tvRoomType.text = room.type
            tvRoomDescription.text = room.description
            tvRoomPrice.text = "${room.price}₽ / ночь"
            tvRoomAvailable.text = if (room.isAvailable) "Доступен" else "Занят"
            
            root.setOnClickListener {
                onRoomClick(room)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Room>() {
        override fun areItemsTheSame(oldItem: Room, newItem: Room) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Room, newItem: Room) = oldItem == newItem
    }
}
