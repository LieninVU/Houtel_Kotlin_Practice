package com.example.hotel_app.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hotel_app.databinding.LayoutItemPaidServiceBinding
import com.example.hotel_app.domain.model.PaidService
import com.example.hotel_app.domain.model.getIcon

class PaidServicesAdapter :
    ListAdapter<PaidService, PaidServicesAdapter.PaidServiceViewHolder>(DiffCallback) {

    class PaidServiceViewHolder(val binding: LayoutItemPaidServiceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaidServiceViewHolder {
        val binding = LayoutItemPaidServiceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PaidServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaidServiceViewHolder, position: Int) {
        val service = getItem(position)
        with(holder.binding) {
            tvServiceIcon.text = service.category.getIcon()
            tvServiceTitle.text = service.title
            tvServicePrice.text = "$${service.price.toInt()}"
            tvPaidAt.text = service.paidAt
            tvPaidStatus.text = "Оплачено"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PaidService>() {
        override fun areItemsTheSame(oldItem: PaidService, newItem: PaidService) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PaidService, newItem: PaidService) =
            oldItem == newItem
    }
}
