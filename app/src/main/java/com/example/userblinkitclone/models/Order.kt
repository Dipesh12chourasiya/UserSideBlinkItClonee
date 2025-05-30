package com.example.userblinkitclone.models

import com.example.userblinkitclone.roomDB.CartProducts

data class Order(
    val orderId: String? = null,
    val orderList: List<CartProducts>? = null,
    val userAddress: String? = null,
    val orderStatus: Int? = 0,
    val orderDate: String? = null,
    val orderingUserId: String? = null,
)
