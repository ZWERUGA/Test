package com.example.bulletinboardappkotlin.utils

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.activities.EditAdsActivity
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImagePicker {
    const val MAX_IMAGE_COUNT = 3

    private fun getOptions(imageCount: Int): Options {
        val options = Options().apply {
            count = imageCount
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options
    }

    fun getMultiImages(editActivity: EditAdsActivity, imageCount: Int) {
        editActivity.addPixToActivity(R.id.place_holder, getOptions(imageCount)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectedImages(editActivity, result.data)
                }
                else -> {}
            }
        }
    }

    fun addImages(editActivity: EditAdsActivity, imageCount: Int) {
        editActivity.addPixToActivity(R.id.place_holder, getOptions(imageCount)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFragment(editActivity)
                    editActivity.chooseImageFragment?.updateAdapter(
                        result.data as ArrayList<Uri>,
                        editActivity
                    )
                }
                else -> {}
            }
        }
    }

    fun getSingleImage(editActivity: EditAdsActivity) {
        editActivity.addPixToActivity(R.id.place_holder, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFragment(editActivity)
                    getSingleSelectedImage(editActivity, result.data[0])
                }
                else -> {}
            }
        }
    }

    private fun openChooseImageFragment(editActivity: EditAdsActivity) {
        editActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.place_holder, editActivity.chooseImageFragment!!)
            .commit()
    }

    private fun closePixFragment(editActivity: EditAdsActivity) {
        val fList = editActivity.supportFragmentManager.fragments
        fList.forEach {
            if (it.isVisible) editActivity.supportFragmentManager.beginTransaction()
                .remove(it).commit()
        }
    }

    fun getMultiSelectedImages(editActivity: EditAdsActivity, uris: List<Uri>) {
        if (uris.size > 1 && editActivity.chooseImageFragment == null) {
            editActivity.openChooseImageFragment(uris as ArrayList<Uri>)
        } else if (uris.size == 1 && editActivity.chooseImageFragment == null) {
            CoroutineScope(Dispatchers.Main).launch {
                editActivity.binding.pbLoadedImages.visibility = View.VISIBLE
                val bitmapArray =
                    ImageManager.imageResize(
                        uris as ArrayList<Uri>,
                        editActivity
                    ) as ArrayList<Bitmap>
                editActivity.binding.pbLoadedImages.visibility = View.GONE
                editActivity.imageAdapter.updateArray(bitmapArray)
                closePixFragment(editActivity)
            }
        }
    }

    private fun getSingleSelectedImage(editActivity: EditAdsActivity, uri: Uri) {
        editActivity.chooseImageFragment
            ?.setSingleImage(uri, editActivity.editImagePosition)
    }
}