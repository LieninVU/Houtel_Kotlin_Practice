package com.hotel.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hotel.app.databinding.ItemBookedServiceBinding
import com.hotel.app.models.HotelService

class BookedServicesAdapter(
    private val onDeleteClick: (HotelService) -> Unit
) : ListAdapter<HotelService, BookedServicesAdapter.BookedServiceViewHolder>(
    BookedServiceDiffCallback()
) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookedServiceViewHolder {
        val binding = ItemBookedServiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookedServiceViewHolder(binding, onDeleteClick)
    }
    
    override fun onBindViewHolder(holder: BookedServiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class BookedServiceViewHolder(
        private val binding: ItemBookedServiceBinding,
        private val onDeleteClick: (HotelService) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(service: HotelService) {
            binding.textServiceName.text = service.name
            binding.textServicePrice.text = "%.2f ₽".format(service.price)
            binding.textServiceDuration.text = service.duration
            
            binding.buttonDelete.setOnClickListener {
                onDeleteClick(service)
            }
        }
    }
    
    class BookedServiceDiffCallback : DiffUtil.ItemCallback<HotelService>() {
        override fun areItemsTheSame(oldItem: HotelService, newItem: HotelService): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: HotelService, newItem: HotelService): Boolean {
            return oldItem == newItem
        }
    }
}