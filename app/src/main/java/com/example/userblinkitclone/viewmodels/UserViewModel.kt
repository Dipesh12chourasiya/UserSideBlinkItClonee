package com.example.userblinkitclone.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.models.Product
import com.example.userblinkitclone.models.Users
import com.example.userblinkitclone.roomDB.CartProducts
import com.example.userblinkitclone.roomDB.CartProductsDao
import com.example.userblinkitclone.roomDB.CartProductsDatabse
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow





class UserViewModel(application: Application): AndroidViewModel(application) {

    // Intitializations
    val sharedPrefrences : SharedPreferences = application.getSharedPreferences("My_Pref", MODE_PRIVATE)
    val cartProductDao: CartProductsDao = CartProductsDatabse.getDatabaseInstance(application).cartProductsDao() // making obj of Room database

    // Room DB
    suspend fun insertCartProduct(products: CartProducts){
        cartProductDao.insertCartProduct(products)
    }

    suspend fun updateCartProduct(products: CartProducts){
        cartProductDao.updateCartProduct(products)
    }

    suspend fun deleteCartProduct(productId:String){
        cartProductDao.deleteCartProduct(productId)
    }

    suspend fun getAllCartProducts(): LiveData<List<CartProducts>> {
        return cartProductDao.getAllCartProducts()
    }

//    Firebase call
    fun fetchAllProducts(): Flow<List<Product>> = callbackFlow{
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")

        val eventListner = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()

                for(product in snapshot.children){
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)
                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }

        db.addValueEventListener(eventListner)

        awaitClose{db.removeEventListener(eventListner)}
    }
    fun getCategoryProduct(category: String): Flow<List<Product>> = callbackFlow{
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${category}/")
        
        val eventListner = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()

                for(product in snapshot.children){
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)
                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        db.addValueEventListener(eventListner)

        awaitClose{db.removeEventListener(eventListner)}
    }

    fun updateItemCount(product: Product, itemCount: Int){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").child("itemCount").setValue(itemCount)
    }

    fun saveUserAddress(address: String){
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserId()).child("userAddress").setValue(address)
    }





    // sharedPrefrences
    fun savingCartItemCount(itemCount: Int){
        sharedPrefrences.edit().putInt("itemCount", itemCount).apply()
    }

    fun fetchTotalItemCount(): MutableLiveData<Int>{
        val totalItemCount = MutableLiveData<Int>()
        totalItemCount.value = sharedPrefrences.getInt("itemCount",0)

        return totalItemCount
    }

    fun saveAddressStatus(){
        sharedPrefrences.edit().putBoolean("addressStatus", true).apply()
    }

    fun getAddressStatus() : MutableLiveData<Boolean>{
        val status = MutableLiveData<Boolean>()
        status.value = sharedPrefrences.getBoolean("addressStatus",false)
        return status
    }
}