package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_app.R
import com.example.hotel_app.ResourceProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestaurantViewModel : ViewModel() {

    private val _menuItems = MutableStateFlow<List<MenuItem>>(getMenuItems())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _selectedItem = MutableStateFlow<MenuItem?>(null)
    val selectedItem: StateFlow<MenuItem?> = _selectedItem.asStateFlow()

    private val _orderStatus = MutableStateFlow<OrderStatus?>(null)
    val orderStatus: StateFlow<OrderStatus?> = _orderStatus.asStateFlow()

    fun selectItem(item: MenuItem) {
        _selectedItem.value = item
    }

    fun placeOrder(roomNumber: String, comment: String) {
        val item = _selectedItem.value ?: return

        viewModelScope.launch {
            _orderStatus.value = OrderStatus.Preparing
            delay(2000)
            _orderStatus.value = OrderStatus.Delivering(roomNumber, item.name)
            delay(3000)
            _orderStatus.value = OrderStatus.Delivered
        }
    }

    fun clearOrderStatus() {
        _orderStatus.value = null
    }

    private fun getMenuItems(): List<MenuItem> = listOf(
        MenuItem("1", ResourceProvider.getString(R.string.menu_caesar_chicken), ResourceProvider.getString(R.string.menu_description_caesar), 450.0, "🥗"),
        MenuItem("2", ResourceProvider.getString(R.string.menu_borscht_moscow), ResourceProvider.getString(R.string.menu_description_borscht), 350.0, "🍲"),
        MenuItem("3", ResourceProvider.getString(R.string.menu_steak_ribeye), ResourceProvider.getString(R.string.menu_description_steak), 1200.0, "🥩"),
        MenuItem("4", ResourceProvider.getString(R.string.menu_salmon_grill), ResourceProvider.getString(R.string.menu_description_salmon), 890.0, "🐟"),
        MenuItem("5", ResourceProvider.getString(R.string.menu_pasta_carbonara), ResourceProvider.getString(R.string.menu_description_pasta), 520.0, "🍝"),
        MenuItem("6", ResourceProvider.getString(R.string.menu_tiramisu), ResourceProvider.getString(R.string.menu_description_tiramisu), 380.0, "🍰"),
        MenuItem("7", ResourceProvider.getString(R.string.menu_cheesecake_ny), ResourceProvider.getString(R.string.menu_description_cheesecake), 420.0, "🧀"),
        MenuItem("8", ResourceProvider.getString(R.string.menu_coffee_espresso), ResourceProvider.getString(R.string.menu_description_coffee), 150.0, "☕"),
        MenuItem("9", ResourceProvider.getString(R.string.menu_fresh_juice), ResourceProvider.getString(R.string.menu_description_juice), 250.0, "🧃")
    )
}

data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val icon: String
)

sealed class OrderStatus {
    object Preparing : OrderStatus()
    data class Delivering(val roomNumber: String, val itemName: String) : OrderStatus()
    object Delivered : OrderStatus()
}
