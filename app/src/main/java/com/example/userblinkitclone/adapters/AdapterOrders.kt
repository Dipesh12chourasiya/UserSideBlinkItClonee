package com.example.userblinkitclone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.userblinkitclone.R
import com.example.userblinkitclone.databinding.ItemViewOrdersBinding
import com.example.userblinkitclone.models.OrderdItem

class AdapterOrders(val context: Context, val onOrderItemViewClicked: (OrderdItem) -> Unit) : RecyclerView.Adapter<AdapterOrders.OrdersViewHolder>() {
    class OrdersViewHolder(val binding: ItemViewOrdersBinding): ViewHolder(binding.root)

    val diffUtil = object : DiffUtil.ItemCallback<OrderdItem>(){
        override fun areItemsTheSame(oldItem: OrderdItem, newItem: OrderdItem): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: OrderdItem, newItem: OrderdItem): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        return OrdersViewHolder(ItemViewOrdersBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = differ.currentList[position]

        holder.binding.apply {
            tvOrderTitles.text = order.itemTitle
            tvOrderDate.text = order.itemDate
            tvOrderAmount.text = "₹ ${order.itemPrice.toString()}"

            when(order.itemStatus){
                0 -> {
                    tvOrderStatus.text = "Ordered"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.yellow)
                }
                1-> {
                    tvOrderStatus.text = "Recieved"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.blue)
                }
                2-> {
                    tvOrderStatus.text = "Dispatched"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.orange)
                }
                3-> {
                    tvOrderStatus.text = "Delivered"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green)
                }
            }
        }
        holder.itemView.setOnClickListener{
            onOrderItemViewClicked(order)
        }
    }
}