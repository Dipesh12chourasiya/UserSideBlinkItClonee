package com.example.userblinkitclone

interface CartListner {
    fun showCartLayout(itemCount: Int)

    fun savingCartItemCount(itemCount: Int)

    fun hideCartLayout()
}