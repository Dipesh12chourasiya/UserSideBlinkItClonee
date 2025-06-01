package com.example.userblinkitclone.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.R
import com.example.userblinkitclone.adapters.AdapterCartProduct
import com.example.userblinkitclone.databinding.FragmentOrderDetailsBinding
import com.example.userblinkitclone.roomDB.CartProducts
import com.example.userblinkitclone.viewmodels.UserViewModel
import kotlinx.coroutines.launch


class OrderDetailsFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailsBinding
    private var status = 0
    private var orderId = ""

    private val viewModel: UserViewModel by viewModels()

    private lateinit var adapterCartProducts: AdapterCartProduct

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOrderDetailsBinding.inflate(layoutInflater)

        onBackButtonClicked()
        getValues()

        settingStatus()

        getOrderedProducts()

      return binding.root
    }

    private fun onBackButtonClicked() {
        binding.tbOrderDetailFragment.setNavigationOnClickListener{
            findNavController().navigate(R.id.action_orderDetailsFragment_to_ordersFragment)
        }
    }

    private fun getOrderedProducts() {
        lifecycleScope.launch {
            viewModel.getOrderedProducts(orderId).collect{ cartList ->
                adapterCartProducts = AdapterCartProduct()
                binding.rvProductItems.adapter = adapterCartProducts

                adapterCartProducts.differ.submitList(cartList)
            }
        }
    }

    private fun settingStatus() {
        when(status){
            0 ->{
                binding.iv1.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
            }
            1 ->{
                binding.iv1.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.iv2.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.view1.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
            }
            2 ->{
                binding.iv1.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.iv2.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.view1.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)

                binding.iv3.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.view2.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
            }
            3 ->{
                binding.iv1.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.iv2.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.view1.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)

                binding.iv3.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.view2.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)

                binding.iv4.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.view3.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
            }
        }
    }

    private fun getValues() {
        val bundle = arguments
        status = bundle!!.getInt("status")
        orderId = bundle.getString("orderId").toString()
    }
}