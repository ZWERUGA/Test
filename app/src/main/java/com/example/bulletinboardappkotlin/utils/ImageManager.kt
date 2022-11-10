package com.example.bulletinboardappkotlin.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.example.bulletinboardappkotlin.adapters.ImageAdapter
import com.example.bulletinboardappkotlin.model.Advertisement
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

object ImageManager {
    private const val MAX_IMAGE_SIZE = 1000
    const val WIDTH = 0
    const val HEIGHT = 1

    fun getImageSize(uri: Uri, activity: Activity): List<Int> {
        val inStream = activity.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply {
            // Берем только края изображения
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inStream, null, options)
        return listOf(options.outWidth, options.outHeight)
    }

    suspend fun imageResize(uris: ArrayList<Uri>, activity: Activity): List<Bitmap> =
        withContext(Dispatchers.IO) {
            val tempList = ArrayList<List<Int>>()
            val bitmapList = ArrayList<Bitmap>()
            for (index in uris.indices) {
                val size = getImageSize(uris[index], activity)
                val imageRatio = size[WIDTH].toFloat() / size[HEIGHT].toFloat()

                // Изображение горизонтальное (if), вертикальное (else)
                if (imageRatio > 1) {
                    if (size[WIDTH] > MAX_IMAGE_SIZE) {
                        tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))
                    } else {
                        tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                    }
                } else {
                    if (size[HEIGHT] > MAX_IMAGE_SIZE) {
                        tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                    } else {
                        tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                    }
                }
            }

            for (index in uris.indices) {
                val e = kotlin.runCatching {
                    bitmapList.add(
                        Picasso.get().load(uris[index])
                            .resize(tempList[index][WIDTH], tempList[index][HEIGHT]).get()
                    )
                }
            }

            return@withContext bitmapList
        }

    private suspend fun getBitmapFromUris(uris: List<String?>): List<Bitmap> =
        withContext(Dispatchers.IO) {
            val bitmapList = ArrayList<Bitmap>()

            for (index in uris.indices) {
                val e = kotlin.runCatching {
                    bitmapList.add(
                        Picasso.get().load(uris[index]).get()
                    )
                }
            }

            return@withContext bitmapList
        }

    fun fillImageArray(advertisement: Advertisement, adapter: ImageAdapter) {
        val listUris =
            listOf(advertisement.mainImage, advertisement.secondImage, advertisement.thirdImage)
        CoroutineScope(Dispatchers.Main).launch {
            val bitmapList = getBitmapFromUris(listUris)
            adapter.updateArray(bitmapList as ArrayList<Bitmap>)
        }
    }

    fun chooseScaleType(imageView: ImageView, bitmap: Bitmap) {
        if (bitmap.width > bitmap.height) {
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }
}