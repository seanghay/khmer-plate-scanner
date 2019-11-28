package com.seanghay.khmervehiclescanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_settings_theme.view.*

class SettingsAdapter(var items: List<SettingsItem> = emptyList()) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private infix fun ViewGroup.inflate(viewType: Int): View {
        val inflater = LayoutInflater.from(context)
        return inflater.inflate(viewType, this, false)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_settings_theme
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return (parent inflate viewType).themeSettingsViewHolder()
    }

    override fun getItemCount(): Int = 1
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    private fun View.themeSettingsViewHolder(): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(this) {

        }
    }
}