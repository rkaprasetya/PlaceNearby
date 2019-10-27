package com.example.chanatest.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chanatest.R
import com.example.chanatest.model.LocationCompact
import kotlinx.android.synthetic.main.item_place.view.*

class MainAdapter(val list : List<LocationCompact?>?):RecyclerView.Adapter<MainAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount()= list!!.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(list!![position])
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bindItems(item : LocationCompact?){
            itemView.tv_main_name.text = item?.name
            itemView.tv_main_rating.text = item?.rating
            itemView.tv_main_opening.text = item?.status
            itemView.tv_main_distance.text = String.format("%.2f", item?.dist)+ " Km"
        }




    }


}