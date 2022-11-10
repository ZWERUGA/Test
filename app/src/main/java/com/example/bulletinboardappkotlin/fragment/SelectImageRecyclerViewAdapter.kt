package com.example.bulletinboardappkotlin.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.activities.EditAdsActivity
import com.example.bulletinboardappkotlin.databinding.SelectImageFragmentItemBinding
import com.example.bulletinboardappkotlin.utils.AdapterCallback
import com.example.bulletinboardappkotlin.utils.ImageManager
import com.example.bulletinboardappkotlin.utils.ImagePicker
import com.example.bulletinboardappkotlin.utils.ItemTouchMoveCallback

class SelectImageRecyclerViewAdapter(val adapterCallback: AdapterCallback) :
    RecyclerView.Adapter<SelectImageRecyclerViewAdapter.ImageHolder>(),
    ItemTouchMoveCallback.ItemTouchAdapter {
    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val viewBinding = SelectImageFragmentItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(viewBinding, parent.context, this)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onMove(startPosition: Int, targetPosition: Int) {
        val targetItem = mainArray[targetPosition]
        mainArray[targetPosition] = mainArray[startPosition]
        mainArray[startPosition] = targetItem
        notifyItemMoved(startPosition, targetPosition)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder(
        val viewBinding: SelectImageFragmentItemBinding,
        val context: Context,
        val adapter: SelectImageRecyclerViewAdapter
    ) : RecyclerView.ViewHolder(viewBinding.root) {

        fun setData(bitmap: Bitmap) {
            viewBinding.imbEditImage.setOnClickListener {
                ImagePicker.getSingleImage(context as EditAdsActivity)
                context.editImagePosition = adapterPosition
            }

            viewBinding.imbDeleteImage.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                for (n in 0 until adapter.mainArray.size) {
                    adapter.notifyItemChanged(n)
                }
                adapter.adapterCallback.onItemDelete()
            }

            viewBinding.tvFragmentItemTitle.text =
                context.resources.getStringArray(R.array.title_image_array)[adapterPosition]
            ImageManager.chooseScaleType(viewBinding.ivFragmentItemImage, bitmap)
            viewBinding.ivFragmentItemImage.setImageBitmap(bitmap)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean) {
        if (needClear) {
            mainArray.clear()
        }
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }


}