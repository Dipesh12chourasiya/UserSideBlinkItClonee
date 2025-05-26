package com.example.userblinkitclone.roomDB

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CartProductsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartProduct(product: CartProducts)

    @Update
    fun updateCartProduct(product: CartProducts)

    @Query("SELECT * FROM CartProducts")
    fun getAllCartProducts() : LiveData<List<CartProducts>>
                                                       // parameter of dCP function
    @Query("DELETE FROM CartProducts WHERE productId = :productId")
    suspend fun deleteCartProduct(productId: String)

    @Query("DELETE FROM CartProducts")
    suspend fun deleteAllCartProduts()
}

