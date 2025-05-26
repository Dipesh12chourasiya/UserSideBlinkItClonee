package com.example.userblinkitclone.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.userblinkitclone.Constants
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.api.ApiUtilities
import com.example.userblinkitclone.models.Order
import com.example.userblinkitclone.models.OrderdItem
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow





class UserViewModel(application: Application): AndroidViewModel(application) {

    // Intitializations
    val sharedPrefrences : SharedPreferences = application.getSharedPreferences("My_Pref", MODE_PRIVATE)
    val cartProductDao: CartProductsDao = CartProductsDatabse.getDatabaseInstance(application).cartProductsDao() // making obj of Room database

//    private val _paymentStatus = MutableStateFlow<Boolean>(false)
//    val paymentStatus = _paymentStatus

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

    suspend fun deleteAllCartProducts(){
        return cartProductDao.deleteAllCartProduts()
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

    fun getAllOrders() : Flow<List<Order>> = callbackFlow{
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").orderByChild("orderStatus")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = ArrayList<Order>()
                for (orders in snapshot.children){
                    val order = orders.getValue(Order::class.java)

                    if(order!!.orderingUserId == Utils.getCurrentUserId()){
                        orderList.add(order!!)
                    }
                }

                trySend(orderList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }

        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener)}
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

    fun saveProductsAfterOrder(stock:Int, product: CartProducts){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productId}").child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productId}").child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productId}").child("itemCount").setValue(0)

        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productId}").child("productStock").setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productId}").child("productStock").setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productId}").child("productStock").setValue(stock)
    }

    fun saveUserAddress(address: String){
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserId()).child("userAddress").setValue(address)
    }

    fun getUserAddress(callback: (String?) -> Unit){

        val db = FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserId()).child("userAddress")

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val address = snapshot.getValue(String::class.java)
                    callback(address)
                } else {
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    fun saveOrderedProduct(order: Order){
        FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(order.orderId!!).setValue(order)
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


    // retrofit
//    suspend fun checkPaymentStatus(headers: Map<String, String>){
//        val res = ApiUtilities.statusAPI.checkStatus(headers, Constants.MERCHANTID, Constants.merchantTransactionId)
//        if(res.body() != null && res.body()!!.success){
//            _paymentStatus.value  = true
//        } else{
//            _paymentStatus.value = false
//        }
//    }


    // for simulating
    private val _paymentStatus = MutableStateFlow(false)
    val paymentStatus = _paymentStatus.asStateFlow()

    fun setPaymentStatus(status: Boolean) {
        _paymentStatus.value = status
    }


}