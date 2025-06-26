package com.groupproject.spendwise

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class IncomeAdapter(private var incomeList: ArrayList<IncomeModel>) :
    RecyclerView.Adapter<IncomeAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val income: TextView = itemView.findViewById(R.id.rc_income_amount)
        val date: TextView = itemView.findViewById(R.id.rc_income_date)
        val type: TextView = itemView.findViewById(R.id.rc_income_type)
        val icon: ImageView = itemView.findViewById(R.id.rc_income_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.income_recycleview_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = incomeList[position]
        holder.income.text = "$${currentItem.income}"
        holder.date.text = currentItem.date
        holder.type.text = currentItem.type
        // Try to load local image if photoPath exists and file is present
        val photoPath = currentItem.photoUrl // Actually now stores photoPath
        if (photoPath != null && photoPath.isNotEmpty()) {
            val file = File(photoPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(photoPath)
                holder.icon.setImageBitmap(bitmap)
            } else {
                // Show placeholder depending on type
                if (currentItem.type?.lowercase() == "expense") {
                    holder.icon.setImageResource(R.drawable.expense_pic)
                } else {
                    holder.icon.setImageResource(R.drawable.income_pic)
                }
            }
        } else {
            if (currentItem.type?.lowercase() == "expense") {
                holder.icon.setImageResource(R.drawable.expense_pic)
            } else {
                holder.icon.setImageResource(R.drawable.income_pic)
            }
        }
    }

    override fun getItemCount(): Int {
        return incomeList.size
    }

    fun updateData(newList: List<IncomeModel>) {
        incomeList.clear()
        incomeList.addAll(newList)
        notifyDataSetChanged()
    }
}