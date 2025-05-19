package com.example.userblinkitclone.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.userblinkitclone.CartListner
import com.example.userblinkitclone.R
import com.example.userblinkitclone.adapters.AdapterCartProduct
import com.example.userblinkitclone.databinding.ActivityUsersMainBinding
import com.example.userblinkitclone.databinding.BsCartProductsBinding
import com.example.userblinkitclone.roomDB.CartProducts
import com.example.userblinkitclone.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class UsersMainActivity : AppCompatActivity(), CartListner {

    private lateinit var binding: ActivityUsersMainBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var cartProductList: List<CartProducts>
    private lateinit var adapterCartProduct: AdapterCartProduct


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUsersMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAllCartProducts()
        getTotalItemCountInCart()
        onCartClicked() // we open a bottom sheet list of cartproducts
        onNextButtonClicked()
    }

    private fun onNextButtonClicked() {
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, OrderPlaceActivity::class.java))
        }
    }

    private fun getAllCartProducts() {

        lifecycleScope.launch {
            viewModel.getAllCartProducts().observe(this@UsersMainActivity){ cartProducts ->
                cartProductList = cartProducts
            }
        }
    }

    private fun onCartClicked() {
        binding.llItemCart.setOnClickListener{
            val bsCartProductBinding = BsCartProductsBinding.inflate(LayoutInflater.from(this))

            val bs = BottomSheetDialog(this)
            bs.setContentView(bsCartProductBinding.root)

            bsCartProductBinding.tvNumberOfProductCount.text = binding.tvNumberOfProductCount.text

            adapterCartProduct = AdapterCartProduct()
            bsCartProductBinding.rvProductsItems.adapter = adapterCartProduct
            adapterCartProduct.differ.submitList(cartProductList)

            bsCartProductBinding.btnNext.setOnClickListener{
                startActivity(Intent(this, OrderPlaceActivity::class.java))

            }

            bs.show()
        }
    }

    private fun getTotalItemCountInCart() {
        // getttin value from shared prefrences
        viewModel.fetchTotalItemCount().observe(this){ itemCount ->
            if(itemCount > 0){
                binding.llCart.visibility = View.VISIBLE
                binding.tvNumberOfProductCount.text = itemCount.toString()
            } else {
                binding.llCart.visibility = View.GONE
            }
        }
    }

    override fun showCartLayout(itemCount: Int) {
        val previousCount = binding.tvNumberOfProductCount.text.toString().toInt()
        val updatedCount = previousCount + itemCount

        if(updatedCount > 0){
            binding.llCart.visibility = View.VISIBLE
            binding.tvNumberOfProductCount.text = updatedCount.toString()
        } else {
            binding.llCart.visibility = View.GONE
            binding.tvNumberOfProductCount.text = "0"
        }
    }

    override fun savingCartItemCount(itemCount: Int) {
        viewModel.fetchTotalItemCount().observe(this){ prevItemC ->
            viewModel.savingCartItemCount(prevItemC + itemCount)

            val currCount = prevItemC + itemCount
            Log.d("SharedPrefs", "Item count in SharedPrefrs: $currCount" );
        }
    }


}