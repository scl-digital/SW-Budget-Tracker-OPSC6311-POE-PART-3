package com.groupproject.spendwise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IncomeAdapter(private val incomelist : ArrayList<IncomeModel>): RecyclerView.Adapter<IncomeAdapter.IncomeViweHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViweHolder {
        val itemViwe = LayoutInflater.from(parent.context).inflate(R.layout.income_recycleview_row,parent,false)
        return IncomeViweHolder(itemViwe)
    }

    override fun onBindViewHolder(holder: IncomeViweHolder, position: Int) {

        val currentitem = incomelist[position]
        holder.income.text = currentitem.income
        holder.date.text = currentitem.date
        holder.type.text = currentitem.type
        holder.icon.setImageResource(currentitem.icon!!)
    }

    override fun getItemCount(): Int {
        return incomelist.size
    }

    class IncomeViweHolder(itemViwe : View): RecyclerView.ViewHolder(itemViwe){

        val income: TextView = itemViwe.findViewById(R.id.rc_income_amount)
        val date: TextView = itemViwe.findViewById(R.id.rc_income_date)
        val type: TextView = itemViwe.findViewById(R.id.rc_income_type)
        val icon: ImageView = itemViwe.findViewById(R.id.rc_income_icon)

    }



}