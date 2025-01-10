package com.example.eilgoal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(private val itemList: List<DataClass>):
RecyclerView.Adapter<ItemAdapter.ItemViewHolder>()
{
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val equipe: TextView = view.findViewById(R.id.equipe)
        val equipe2: TextView = view.findViewById(R.id.equipe2)
        val equipeImage: ImageView=view.findViewById(R.id.equipeImage)
        val equipe2Image: ImageView=view.findViewById(R.id.equipe2Image)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rcmachprog,parent,false)
        return ItemViewHolder(view)
    }
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.equipe.text = item.equipe
        holder.equipe2.text = item.equipe2
        holder.equipeImage.setImageResource(item.equipeImage)
        holder.equipe2Image.setImageResource(item.equipe2Image)

    }
    override fun getItemCount(): Int = itemList.size

}