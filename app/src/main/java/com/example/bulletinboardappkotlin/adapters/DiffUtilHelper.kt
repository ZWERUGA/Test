package com.example.bulletinboardappkotlin.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.bulletinboardappkotlin.model.Advertisement

class DiffUtilHelper(val oldList: List<Advertisement>, val newList: List<Advertisement>) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].key == newList[newItemPosition].key
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}