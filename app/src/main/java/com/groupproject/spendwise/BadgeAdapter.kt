package com.groupproject.spendwise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Data class for a badge/achievement
data class Badge(val iconRes: Int, val title: String, val achieved: Boolean)

class BadgeAdapter(private val badges: List<Badge>) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {
    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.badgeIcon)
        val title: TextView = itemView.findViewById(R.id.badgeTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        holder.icon.setImageResource(badge.iconRes)
        holder.title.text = badge.title
        if (!badge.achieved) {
            holder.icon.setColorFilter(android.graphics.Color.parseColor("#BDBDBD"), android.graphics.PorterDuff.Mode.SRC_IN)
            holder.icon.alpha = 0.5f
        } else {
            holder.icon.clearColorFilter()
            holder.icon.alpha = 1.0f
        }
    }

    override fun getItemCount(): Int = badges.size
} 