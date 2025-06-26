package com.groupproject.spendwise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TipAdapter(private val tips: List<Pair<String, String>>) : RecyclerView.Adapter<TipAdapter.TipViewHolder>() {
    class TipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tipTitle)
        val content: TextView = itemView.findViewById(R.id.tipContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tip, parent, false)
        return TipViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val (title, content) = tips[position]
        holder.title.text = title
        holder.content.text = content
    }

    override fun getItemCount(): Int = tips.size
} 