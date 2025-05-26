package com.example.userblinkitclone.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.R
import com.example.userblinkitclone.adapters.AdapterOrders
import com.example.userblinkitclone.databinding.FragmentOrdersBinding
import com.example.userblinkitclone.models.OrderdItem
import com.example.userblinkitclone.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class OrdersFragment : Fragment() {

    private lateinit var binding: FragmentOrdersBinding
    private  lateinit var adapterOrders: AdapterOrders

    private val viewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOrdersBinding.inflate(layoutInflater)
        requireActivity().window.statusBarColor = Color.TRANSPARENT

        onBackButtonClicked()
        getAllOrders()
        return binding.root
    }

    private fun getAllOrders() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.getAllOrders().collect{ ordersList -> // List of Orders

                if(ordersList.isNotEmpty()){
                    val  orderedList = ArrayList<OrderdItem>() // to pass to recycler view

                    for(order in ordersList){ // each order in ListOf Orders,
                        val title = StringBuilder()
                        var totalPrice = 0

                        for (product in order.orderList!!) { // it is List of Cart Products
                            val priceString = product.productPrice!!.filter { it.isDigit() }  // Extracts only digits
                            val price = priceString.toIntOrNull() ?: 0

                            val itemCount = product.productCount!!.toInt()

                            totalPrice += price * itemCount

                            title.append("${product.productCategory}, ")
                        }

                        val orderedItem = OrderdItem(order.orderId, order.orderDate, order.orderStatus, title.toString(), totalPrice)
                        orderedList.add(orderedItem)
                    }

                    adapterOrders = AdapterOrders(requireContext(), ::onOrderItemViewClicked)
                    binding.rvOrders.adapter = adapterOrders
                    adapterOrders.differ.submitList(orderedList)

                    binding.shimmerViewContainer.visibility = View.GONE

                }
            }
        }
    }

    private fun onBackButtonClicked() {
        binding.tbOrdersFragment.setNavigationOnClickListener{
            findNavController().navigate(R.id.action_ordersFragment_to_profileFragment)
        }
    }

    fun onOrderItemViewClicked(orderedItem: OrderdItem){

    }

}