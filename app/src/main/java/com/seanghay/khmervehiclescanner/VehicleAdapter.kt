package com.seanghay.khmervehiclescanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_vehicle_info.view.*

class VehicleAdapter(var items: List<VehicleItem>) :
    RecyclerView.Adapter<VehicleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_vehicle_info
    }

    fun updateItems(newItems: List<VehicleItem>) {
        val callback = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].type == newItems[newItemPosition].type
            }

            override fun getOldListSize(): Int {
                return items.size
            }

            override fun getNewListSize(): Int {
                return newItems.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition] == newItems[newItemPosition]
            }
        }

        val result = DiffUtil.calculateDiff(callback)
        result.dispatchUpdatesTo(this)
        this.items = newItems
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: VehicleItem) {
            with(itemView) {
                textViewType.text = item.type
                textViewValue.text = item.value
            }
        }
    }
}