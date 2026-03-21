package com.example.hotel_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            kotlinx.coroutines.delay(2000)
            _orderStatus.value = OrderStatus.Delivering(roomNumber, item.name)
            kotlinx.coroutines.delay(3000)
            _orderStatus.value = OrderStatus.Delivered
        }
    }

    fun clearOrderStatus() {
        _orderStatus.value = null
    }

    private fun getMenuItems(): List<MenuItem> = listOf(
        MenuItem("1", "Цезарь с курицей", "Салат с курицей, пармезаном и сухариками", 450.0, "🥗"),
        MenuItem("2", "Борщ московский", "Традиционный русский суп со сметаной", 350.0, "🍲"),
        MenuItem("3", "Стейк рибай", "Сочный стейк из мраморной говядины", 1200.0, "🥩"),
        MenuItem("4", "Лосось на гриле", "Свежий лосось с овощами", 890.0, "🐟"),
        MenuItem("5", "Паста Карбонара", "Классическая итальянская паста", 520.0, "🍝"),
        MenuItem("6", "Тирамису", "Итальянский десерт с маскарпоне", 380.0, "🍰"),
        MenuItem("7", "Чизкейк Нью-Йорк", "Классический чизкейк", 420.0, "🧀"),
        MenuItem("8", "Кофе эспрессо", "Арабика 100%", 150.0, "☕"),
        MenuItem("9", "Свежевыжатый сок", "Апельсиновый или яблочный", 250.0, "🧃")
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
