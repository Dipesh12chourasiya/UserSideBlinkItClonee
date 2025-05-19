package com.example.userblinkitclone

import android.widget.Filter
import com.example.userblinkitclone.adapters.AdapterProduct
import com.example.userblinkitclone.models.Product
import java.util.Locale

class FilteringProduct(val adapter: AdapterProduct, val filter: ArrayList<Product> ) : Filter(){

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val result = FilterResults()

        if(!constraint.isNullOrEmpty()){
            val filteredList = ArrayList<Product>()
            val query = constraint.toString().trim().uppercase(Locale.getDefault()).split(" ")

            for (product in filter){
                if (query.any{
                        product.productTitle?.uppercase(Locale.getDefault())?.contains(it) == true ||
                                product.productCategory?.uppercase(Locale.getDefault())?.contains(it) == true  ||
                                product.productType?.uppercase(Locale.getDefault())?.contains(it) == true ||
                                product.productPrice?.toString()?.uppercase(Locale.getDefault())?.contains(it) == true
                    }){
                        filteredList.add(product)
                   }
            }

            result.values = filteredList
            result.count = filteredList.size

        } else{
            result.values = filter
            result.count = filter.size
        }

        return result
    }

    override fun publishResults(p0: CharSequence?, result: FilterResults?) {
//        adapter.differ.submitList(result?.values as ArrayList<Product>)
        adapter.differ.submitList(result?.values as? List<Product>)

    }

}