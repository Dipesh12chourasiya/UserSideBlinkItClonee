package com.example.userblinkitclone.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CartProducts")
data class CartProducts(
    @PrimaryKey
    var productId : String = "random",

    var productTitle : String? = null,
    var productQuantity : String? = null,
    var productPrice: String? = null,
    var productCount: Int? = null,
    var productStock: Int? = null,
    var productImage: String? = null,
    var productCategory: String? = null,
    var productType: String? = null,
    var adminUid : String? = null,
)