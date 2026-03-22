package com.example.hotel_app.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hotel_app.R
import com.example.hotel_app.databinding.LayoutItemRoomBinding
import com.example.hotel_app.domain.model.Room
import com.example.hotel_app.presentation.ui.utils.ImageLoadingUtils

class RoomAdapter(
    private val onRoomSelected: (Room) -> Unit
) : ListAdapter<Room, RoomAdapter.RoomViewHolder>(DiffCallback) {

    private var selectedRoomId: String? = null

    fun setSelectedRoom(roomId: String?) {
        val previousSelected = selectedRoomId
        selectedRoomId = roomId
        
        currentList.forEachIndexed { index, room ->
            if (room.id == previousSelected || room.id == roomId) {
                notifyItemChanged(index)
            }
        }
    }

    inner class RoomViewHolder(val binding: LayoutItemRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val room = getItem(position)
                    if (room.isAvailable) {
                        onRoomSelected(room)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = LayoutItemRoomBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = getItem(position)
        val context = holder.itemView.context
        val isSelected = room.id == selectedRoomId

        with(holder.binding) {
            tvRoomType.text = room.type
            tvRoomPrice.text = "$ ${room.price.toInt()} / night"
            tvRoomDescription.text = room.description

            // ✅ Загрузка изображения с правильными Dispatchers и обработкой ошибок
            ImageLoadingUtils.loadImage(
                imageView = ivRoom,
                imageUrl = room.imageUrl,
                placeholder = R.drawable.ic_launcher_background,
                error = R.drawable.ic_launcher_background
            )

            root.alpha = if (room.isAvailable) 1.0f else 0.5f
            root.isClickable = room.isAvailable

            val strokeColor = when {
                isSelected -> ContextCompat.getColor(context, android.R.color.holo_green_dark)
                !room.isAvailable -> ContextCompat.getColor(context, android.R.color.darker_gray)
                else -> ContextCompat.getColor(context, android.R.color.transparent)
            }
            root.strokeColor = strokeColor
            root.strokeWidth = if (isSelected) 4 else 0

            val statusResId = if (room.isAvailable) R.string.room_status_available_en else R.string.room_status_booked_en
            val statusText = context.getString(statusResId)
            tvRoomType.text = "${room.type} • $statusText"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Room>() {
        override fun areItemsTheSame(oldItem: Room, newItem: Room) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Room, newItem: Room) = oldItem == newItem
    }
}
