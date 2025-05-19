package com.example.userblinkitclone.activity

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.userblinkitclone.R
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.adapters.AdapterCartProduct
import com.example.userblinkitclone.databinding.ActivityOrderPlaceBinding
import com.example.userblinkitclone.databinding.AddressLayoutBinding
import com.example.userblinkitclone.models.Users
import com.example.userblinkitclone.roomDB.CartProducts
import com.example.userblinkitclone.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class OrderPlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderPlaceBinding

    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapterCartProduct: AdapterCartProduct

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPlaceBinding.inflate(layoutInflater)

        this.window.statusBarColor = Color.TRANSPARENT


        setContentView(binding.root)

        getAllCartProducts()
        onPlaceOrderClicked()
    }

    private fun onPlaceOrderClicked() {
        binding.btnNext.setOnClickListener {
            viewModel.getAddressStatus().observe(this){ status ->
                if(status){ // agr status == true hai means address exist in the db so start payment work

                    // payment work

                } else {
                    val addressLayoutBinding = AddressLayoutBinding.inflate(LayoutInflater.from(this))

                    val alertDialog = AlertDialog.Builder(this).setView(addressLayoutBinding.root).create()

                    alertDialog.show()

                    addressLayoutBinding.btnAdd.setOnClickListener{
                        saveAddress(alertDialog, addressLayoutBinding)
                    }
                }
            }
        }
    }

    private fun saveAddress(alertDialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
        Utils.showDialog(this, "Processing...")
        val userPinCode = addressLayoutBinding.etPinCode.text.toString()
        val userPhoneNumber = addressLayoutBinding.etPhoneNumber.text.toString()
        val userState = addressLayoutBinding.etState.text.toString()
        val userDistrict = addressLayoutBinding.etDistrict.text.toString()
        val userAddress = addressLayoutBinding.etDescriptiveAddress.text.toString()

        val address = "$userPinCode, $userDistrict($userState), $userAddress, $userPhoneNumber"


        lifecycleScope.launch {
            viewModel.saveUserAddress(address)
            viewModel.saveAddressStatus()
        }

        alertDialog.dismiss()
        Utils.hideDialog()
        Utils.showToast(this,"Address Saved")
    }

    private fun getAllCartProducts() {
        lifecycleScope.launch {
            viewModel.getAllCartProducts().observe(this@OrderPlaceActivity){ cartProducts -> // list of cart products
                adapterCartProduct = AdapterCartProduct()
                binding.rvProductsItems.adapter = adapterCartProduct
                adapterCartProduct.differ.submitList(cartProducts)

                var totalPrice = 0

                for (product in cartProducts) {
                    val priceString = product.productPrice!!.filter { it.isDigit() }  // Extracts only digits
                    val price = priceString.toIntOrNull() ?: 0

                    val itemCount = product.productCount!!.toInt()

                    totalPrice += price * itemCount
                }

                binding.tvSubTotal.text = "₹${totalPrice}"

                if(totalPrice < 200){
                    binding.tvDeliveryCharge.text = "₹20"
                    totalPrice += 20
                }

                binding.tvGrandTotal.text = "₹$totalPrice"

            }
        }
    }
}