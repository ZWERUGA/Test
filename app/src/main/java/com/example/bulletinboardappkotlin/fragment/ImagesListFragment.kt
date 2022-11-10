package com.example.bulletinboardappkotlin.fragment

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.activities.EditAdsActivity
import com.example.bulletinboardappkotlin.databinding.ListImagesFragmentBinding
import com.example.bulletinboardappkotlin.dialoghelper.ProgressDialog
import com.example.bulletinboardappkotlin.utils.AdapterCallback
import com.example.bulletinboardappkotlin.utils.ImageManager
import com.example.bulletinboardappkotlin.utils.ImagePicker
import com.example.bulletinboardappkotlin.utils.ItemTouchMoveCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImagesListFragment(private val fragmentCloseInterface: FragmentCloseInterface)
    : BaseAdFragment(), AdapterCallback {

    lateinit var binding: ListImagesFragmentBinding
    val adapter = SelectImageRecyclerViewAdapter(this)
    private val dragCallback = ItemTouchMoveCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    private var job: Job? = null
    private var addImageButton: MenuItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ListImagesFragmentBinding.inflate(layoutInflater, container, false)
        adView = binding.adView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        binding.apply {
            touchHelper.attachToRecyclerView(rcvSelectedImages)
            rcvSelectedImages.layoutManager = LinearLayoutManager(activity)
            rcvSelectedImages.adapter = adapter
        }
    }

    override fun onItemDelete() {
        addImageButton?.isVisible = true
    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>) {
        adapter.updateAdapter(bitmapList, true)
    }

    fun resizeSelectedImages(newList: ArrayList<Uri>, needClear: Boolean, activity: Activity) {
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity)
            val bitmapList = ImageManager.imageResize(newList, activity)
            dialog.dismiss()
            adapter.updateAdapter(bitmapList, needClear)
            if (adapter.mainArray.size > ImagePicker.MAX_IMAGE_COUNT - 1) {
                addImageButton?.isVisible = false
            }
        }
    }

    override fun onClose() {
        super.onClose()
        activity?.supportFragmentManager?.beginTransaction()
            ?.remove(this@ImagesListFragment)?.commit()
        fragmentCloseInterface.onFragmentClose(adapter.mainArray)
        job?.cancel()
    }

    private fun setUpToolbar() {
        binding.apply {
            tbFragment.inflateMenu(R.menu.toolbar_choose_image)
            addImageButton = tbFragment.menu.findItem(R.id.add_image)

            if (adapter.mainArray.size > ImagePicker.MAX_IMAGE_COUNT - 1) {
                addImageButton?.isVisible = false
            }

            val deleteImageButton = tbFragment.menu.findItem(R.id.delete_image)

            tbFragment.setNavigationOnClickListener {
                showInterstitialAd()
            }

            addImageButton?.setOnMenuItemClickListener {
                val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
                ImagePicker.addImages(activity as EditAdsActivity, imageCount)
                true
            }

            deleteImageButton.setOnMenuItemClickListener {
                adapter.updateAdapter(ArrayList(), true)
                toastMessage(this@ImagesListFragment, "Все изображения удалены!")
                addImageButton?.isVisible = true
                true
            }
        }
    }

    fun updateAdapter(newList: ArrayList<Uri>, activity: Activity) {
        resizeSelectedImages(newList, false, activity)
    }

    fun setSingleImage(uri: Uri, position: Int) {
        val pbEditImage = binding.rcvSelectedImages[position]
            .findViewById<ProgressBar>(R.id.pbEditImage)
        job = CoroutineScope(Dispatchers.Main).launch {
            pbEditImage.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResize(arrayListOf(uri), activity as Activity)
            pbEditImage.visibility = View.GONE
            adapter.mainArray[position] = bitmapList[0]
            adapter.notifyItemChanged(position)
        }

    }


}