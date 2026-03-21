package com.example.hotel_app.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hotel_app.databinding.LayoutItemServiceBinding
import com.example.hotel_app.domain.model.HotelService
import com.example.hotel_app.domain.model.getIcon

class ServicesAdapter(
    private val onServiceClick: (HotelService) -> Unit
) : ListAdapter<HotelService, ServicesAdapter.ServiceViewHolder>(DiffCallback) {

    class ServiceViewHolder(val binding: LayoutItemServiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = LayoutItemServiceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = getItem(position)
        with(holder.binding) {
            tvServiceIcon.text = service.category.getIcon()
            tvServiceTitle.text = service.title
            tvServicePrice.text = "${service.price}₽"
            tvServiceDescription.text = service.description
            
            root.setOnClickListener {
                onServiceClick(service)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<HotelService>() {
        override fun areItemsTheSame(oldItem: HotelService, newItem: HotelService) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: HotelService, newItem: HotelService) = oldItem == newItem
    }
}
