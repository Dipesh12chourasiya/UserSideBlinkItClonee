package com.example.userblinkitclone.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.CartListner
import com.example.userblinkitclone.Constants
import com.example.userblinkitclone.R
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.adapters.AdapterBestseller
import com.example.userblinkitclone.adapters.AdapterCatagory
import com.example.userblinkitclone.adapters.AdapterProduct
import com.example.userblinkitclone.databinding.BsSeeAllBinding
import com.example.userblinkitclone.databinding.FragmentHomeBinding
import com.example.userblinkitclone.databinding.ItemViewProductBinding
import com.example.userblinkitclone.models.Bestseller
import com.example.userblinkitclone.models.Catagory
import com.example.userblinkitclone.models.Product
import com.example.userblinkitclone.roomDB.CartProducts
import com.example.userblinkitclone.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel : UserViewModel by viewModels()
    private lateinit var adapterBestseller: AdapterBestseller

     lateinit var adapterProduct: AdapterProduct

    private var cartListener: CartListner? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(layoutInflater)
        requireActivity().window.statusBarColor = Color.TRANSPARENT

        setAllCategories()
        navigateToSearchFragment()
        onProfileClick()

        fetchBestseller()
        return binding.root
    }

    fun onSeeAllButtonClicked(productType: Bestseller){
        val bsSeeAllBinding = BsSeeAllBinding.inflate(LayoutInflater.from(requireContext()))

        val bs = BottomSheetDialog(requireContext())
        bs.setContentView(bsSeeAllBinding.root)

        adapterProduct = AdapterProduct(::onAddButtonClicked, ::onIncrementButtonClicked, ::onDecrementButtonClicked)
        bsSeeAllBinding.rvProducts.adapter = adapterProduct
        adapterProduct.differ.submitList(productType.products)
        bs.show()
    }

    private fun fetchBestseller() {
        lifecycleScope.launch {
            viewModel.fetchProductTypes().collect{
                adapterBestseller = AdapterBestseller( ::onSeeAllButtonClicked)
                binding.rvBestsellers.adapter = adapterBestseller
                adapterBestseller.differ.submitList(it)
            }
        }
    }

    private fun onProfileClick() {
        binding.ivProfile.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    private fun onCategoryIconClicked(category: Catagory){
        val bundle = Bundle()
        bundle.putString("category", category.title)
        findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, bundle)
    }


    private fun navigateToSearchFragment() {
        binding.searchCv.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun setAllCategories() {
        val categoryList = ArrayList<Catagory>()

        for(i in 0 until Constants.allProductCategoryIcon.size){
            categoryList.add(Catagory(Constants.allProductsCategory[i], Constants.allProductCategoryIcon[i]))
        }

        binding.rvCategories.adapter = AdapterCatagory(categoryList, ::onCategoryIconClicked)
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