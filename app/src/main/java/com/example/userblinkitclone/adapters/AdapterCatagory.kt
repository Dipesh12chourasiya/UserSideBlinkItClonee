package com.example.userblinkitclone.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.userblinkitclone.databinding.ItemViewProductCatagoryBinding
import com.example.userblinkitclone.models.Catagory

class AdapterCatagory(val categoryList: ArrayList<Catagory>, val onCategoryIconClicked: (Catagory) -> Unit) : RecyclerView.Adapter<AdapterCatagory.CategoryViewHolder>() {

    class CategoryViewHolder (val binding: ItemViewProductCatagoryBinding) : ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(ItemViewProductCatagoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.binding.apply {
            ivCategoryImage.setImageResource(category.image)
            tvCategoryTitle.text = category.title
        }

        holder.itemView.setOnClickListener {
            onCategoryIconClicked(category)
        }
    }
}





