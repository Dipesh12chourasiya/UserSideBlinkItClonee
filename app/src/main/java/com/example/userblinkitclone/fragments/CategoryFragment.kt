package com.example.userblinkitclone.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.CartListner
import com.example.userblinkitclone.R
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.adapters.AdapterProduct
import com.example.userblinkitclone.databinding.FragmentCategoryBinding
import com.example.userblinkitclone.databinding.ItemViewProductBinding
import com.example.userblinkitclone.models.Product
import com.example.userblinkitclone.roomDB.CartProducts
import com.example.userblinkitclone.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {
    private lateinit var binding: FragmentCategoryBinding
    private var categoryTitle: String? = null
    private lateinit var adapterProduct: AdapterProduct
    private val viewModel: UserViewModel by viewModels()

    private var cartListener: CartListner? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCategoryBinding.inflate(layoutInflater)
        requireActivity().window.statusBarColor = Color.TRANSPARENT

        getProductOfCategory()
        onNavBackIconClick()
        setToolBarTitle()
        get()
        onSearchMenuClick()
        fetchCategoryProduct()

        return binding.root
    }

    private fun get() {
        lifecycleScope.launch {
            viewModel.getAllCartProducts().observe(viewLifecycleOwner){
                for (i in it){
                    Log.d("vvv", i.productCount.toString())
                    Log.d("vvv", i.productTitle.toString());
                    Log.d("vvv", i.productPrice.toString());
                }
            }
        }
    }

    private fun onNavBackIconClick() {
        binding.tbSearchFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_homeFragment)
        }
    }

    private fun onSearchMenuClick() {
        binding.tbSearchFragment.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.searchMenu -> {
                    findNavController().navigate(R.id.action_categoryFragment_to_searchFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchCategoryProduct() { // this is same as getAllTheProducts in searchFragment

        binding.shimmerViewContainer.visibility = View.VISIBLE

        lifecycleScope.launch {
            viewModel.getCategoryProduct(categoryTitle!!).collect{products: List<Product> ->

                if (products.isEmpty()) {
                    binding.rvProducts.visibility = View.GONE
                    binding.tvText.visibility = View.VISIBLE
                } else {
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.tvText.visibility = View.GONE
                }

                adapterProduct = AdapterProduct(::onAddButtonClicked , ::onIncrementButtonClicked, ::onDecrementButtonClicked)
                binding.rvProducts.adapter = adapterProduct

                adapterProduct.differ.submitList(products)
                adapterProduct.originalList = products as ArrayList<Product>

                binding.shimmerViewContainer.visibility = View.GONE

            }

        }
    }

    private fun getProductOfCategory() {
        val bundle = arguments
        categoryTitle = bundle?.getString("category")
    }

    private fun setToolBarTitle() {
        binding.tbSearchFragment.title = categoryTitle
    }


    // add item to cart save in room dbs
    private fun onAddButtonClicked(product: Product, productBinding: ItemViewProductBinding){
        productBinding.tvAdd.visibility = View.GONE
        productBinding.llProductCount.visibility = View.VISIBLE

        // step 1
        var itemCount = productBinding.tvProductCount.text.toString().toInt()
        itemCount++;
        productBinding.tvProductCount.text = itemCount.toString()

        cartListener?.showCartLayout(1)

        // step 2
        product.itemCount = itemCount
        lifecycleScope.launch {
            cartListener?.savingCartItemCount(1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product , itemCount)
        }

    }

    private fun saveProductInRoomDb(product: Product) {
        val cartProduct = CartProducts(
            productId = product.productRandomId!!,
            productTitle = product.productTitle,
            productQuantity = "${product.productQuantity.toString()} ${product.productUnit.toString()}",
            productPrice = "${product.productPrice}"+ " RS.",
            productCount = product.itemCount,
            productStock = product.productStock,
            productImage = product.productImageUris?.get(0),
            productCategory = product.productCategory,
            adminUid = product.adminUid,
            productType = product.productType
            )

        lifecycleScope.launch {
            viewModel.insertCartProduct(cartProduct)
        }
    }

    fun onIncrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){

        var itemCountInc = productBinding.tvProductCount.text.toString().toInt()
        itemCountInc++;

        if(itemCountInc <= product.productStock!!){
            productBinding.tvProductCount.text = itemCountInc.toString()

            cartListener?.showCartLayout(1)

            // step 2
            product.itemCount = itemCountInc
            lifecycleScope.launch {
                cartListener?.savingCartItemCount(1)
                saveProductInRoomDb(product)
                viewModel.updateItemCount(product , itemCountInc)
            }
        } else {
            Utils.showToast(requireContext(), "Can not add more item, product stock reached")
        }

    }

    fun onDecrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){

        var itemCountDec = productBinding.tvProductCount.text.toString().toInt()
        itemCountDec--;

        // step 2
        product.itemCount = itemCountDec
        lifecycleScope.launch {
            cartListener?.savingCartItemCount(-1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product , itemCountDec)

        }

        if(itemCountDec > 0){
            productBinding.tvProductCount.text = itemCountDec.toString()
        } else{

            lifecycleScope.launch {
                viewModel.deleteCartProduct(product.productRandomId!!) // to delete product from  cart in ROOMM db
            }

            productBinding.tvAdd.visibility = View.VISIBLE
            productBinding.llProductCount.visibility = View.GONE
            productBinding.tvProductCount.text = "0"
        }

        cartListener?.showCartLayout(-1)



    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(context is CartListner){
            cartListener = context
        } else{
            throw ClassCastException("Please implement Cart Listner")
        }
    }

}