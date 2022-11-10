package com.example.bulletinboardappkotlin.dialogspinnerhelper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.activities.EditAdsActivity

class DialogRecyclerViewSpinnerAdapter(var btSelection: Button, var dialog: AlertDialog) :
    RecyclerView.Adapter<DialogRecyclerViewSpinnerAdapter.SpinnerViewHolder>() {
    private val mainList = ArrayList<String>()

    class SpinnerViewHolder(itemView: View, var btSelection: Button, var dialog: AlertDialog) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var itemText = ""

        fun setSpinnerItemData(text: String) {
            val tvSpinnerItem = itemView.findViewById<TextView>(R.id.tvSpinnerItem)
            itemView.setOnClickListener(this)
            tvSpinnerItem.text = text
            itemText = text
        }

        override fun onClick(v: View?) {
            btSelection.text = itemText
            dialog.dismiss()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpinnerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.spinner_list_item, parent, false)
        return SpinnerViewHolder(view, btSelection, dialog)
    }

    override fun onBindViewHolder(holder: SpinnerViewHolder, position: Int) {
        holder.setSpinnerItemData(mainList[position])
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: ArrayList<String>) {
        mainList.clear()
        mainList.addAll(list)
        notifyDataSetChanged()
    }
}