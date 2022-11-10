package com.example.bulletinboardappkotlin.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage
import com.example.bulletinboardappkotlin.MainActivity
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.adapters.ImageAdapter
import com.example.bulletinboardappkotlin.model.Advertisement
import com.example.bulletinboardappkotlin.model.DatabaseManager
import com.example.bulletinboardappkotlin.databinding.ActivityEditAdsBinding
import com.example.bulletinboardappkotlin.dialogspinnerhelper.DialogSpinnerHelper
import com.example.bulletinboardappkotlin.fragment.FragmentCloseInterface
import com.example.bulletinboardappkotlin.fragment.ImagesListFragment
import com.example.bulletinboardappkotlin.utils.CountryHelper
import com.example.bulletinboardappkotlin.utils.ImageManager
import com.example.bulletinboardappkotlin.utils.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import java.io.ByteArrayOutputStream

class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {
    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    var chooseImageFragment: ImagesListFragment? = null

    var editImagePosition = 0
    private var isEditState = false
    private var imageIndex = 0
    private var advertisement: Advertisement? = null

    private lateinit var btSelectCountry: Button
    private lateinit var btSelectCity: Button
    private lateinit var btSelectCategory: Button
    private lateinit var btPublish: Button
    private lateinit var ibtPickImages: ImageButton

    private val dbManager = DatabaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
        imageChangeCounter()
    }

    private fun checkEditState() {
        if (isEditState()) {
            isEditState = true
            advertisement = intent.getSerializableExtra(MainActivity.ADS_DATA) as Advertisement
            if (advertisement != null) {
                fillViews(advertisement!!)
            }
        }
    }

    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(advertisement: Advertisement) = with(binding) {
        btSelectCountry.text = advertisement.country
        btSelectCity.text = advertisement.city
        etTelephone.setText(advertisement.telephone)
        etIndex.setText(advertisement.index)
        cbWithSend.isChecked = advertisement.withSend.toBoolean()
        btSelectCategory.text = advertisement.category
        etTitle.setText(advertisement.title)
        etPrice.setText(advertisement.price)
        etDescription.setText(advertisement.description)
        updateImageCounter(0)
        ImageManager.fillImageArray(advertisement, imageAdapter)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun init() {
        btSelectCountry = binding.btSelectCountry
        btSelectCity = binding.btSelectCity
        btSelectCategory = binding.btCategory
        btPublish = binding.btPublish
        ibtPickImages = binding.ibtPickImages

        btSelectCountry.setOnClickListener {
            val listCountries = CountryHelper.getAllCountries(this)
            dialog.showSpinnerDialog(this, listCountries, binding.btSelectCountry)
            if (binding.btSelectCity.text.toString() != getString(R.string.select_city)) {
                binding.btSelectCity.text = getString(R.string.select_city)
            }
        }

        btSelectCity.setOnClickListener {
            val selectedCountry = binding.btSelectCountry.text.toString()
            if (selectedCountry != getString(R.string.select_country)) {
                val listCities = CountryHelper.getAllCities(selectedCountry, this)
                dialog.showSpinnerDialog(this, listCities, binding.btSelectCity)
            } else {
                toastMessage(this, "Страна не выбрана!")
            }
        }

        btSelectCategory.setOnClickListener {
            val listCategories =
                resources.getStringArray(R.array.categories).toMutableList() as ArrayList
            dialog.showSpinnerDialog(this, listCategories, binding.btCategory)
        }

        btPublish.setOnClickListener {
            if (isFieldsEmpty()) {
                showToast("Поля со * должны быть заполнены!")
            } else {
                binding.llProgressBar.visibility = View.VISIBLE
                advertisement = fillAdvertisement()
                uploadImages()
            }
        }

        ibtPickImages.setOnClickListener {
            if (imageAdapter.mainArray.size == 0) {
                ImagePicker.getMultiImages(this, 3)
            } else {
                openChooseImageFragment(null)
                chooseImageFragment?.updateAdapterFromEdit(imageAdapter.mainArray)
            }

        }

        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter
    }

    private fun onPublishFinish(): DatabaseManager.FinishWorkListener {
        return object : DatabaseManager.FinishWorkListener {
            override fun onFinish(isDone: Boolean) {
                binding.llProgressBar.visibility = View.GONE
                if (isDone) finish()
            }
        }
    }

    private fun isFieldsEmpty(): Boolean = with(binding) {
        return btSelectCountry.text.toString() == getString(R.string.select_country)
                || btSelectCity.text.toString() == getString(R.string.select_city)
                || etTelephone.text.isEmpty()
                || etIndex.text.isEmpty()
                || btSelectCategory.text.toString() == getString(R.string.select_category)
                || etTitle.text.isEmpty()
                || etPrice.text.isEmpty()
                || etDescription.text.isEmpty()
    }

    private fun fillAdvertisement(): Advertisement {
        val adTemp: Advertisement
        binding.apply {
            adTemp = Advertisement(
                btSelectCountry.text.toString(),
                btSelectCity.text.toString(),
                etTelephone.text.toString(),
                etIndex.text.toString(),
                cbWithSend.isChecked.toString(),
                btCategory.text.toString(),
                etTitle.text.toString(),
                etPrice.text.toString(),
                etDescription.text.toString(),
                etEmail.text.toString(),
                advertisement?.mainImage ?: "empty",
                advertisement?.secondImage ?: "empty",
                advertisement?.thirdImage ?: "empty",
                advertisement?.key ?: dbManager.db.push().key,
                dbManager.auth.uid,
                advertisement?.time ?: System.currentTimeMillis().toString(),
                "0"
            )
        }
        return adTemp
    }

    override fun onFragmentClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.updateArray(list)
        chooseImageFragment = null
        updateImageCounter(binding.vpImages.currentItem)
    }

    // Функция - запускает фрагмент
    fun openChooseImageFragment(newList: ArrayList<Uri>?) {
        chooseImageFragment = ImagesListFragment(this)
        if (newList != null) chooseImageFragment?.resizeSelectedImages(newList, true, this)
        binding.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFragment!!)
        fm.commit()
    }

    private fun uploadImages() {
        if (imageIndex == ImagePicker.MAX_IMAGE_COUNT) {
            dbManager.publishAdvertisement(advertisement!!, onPublishFinish())
            return
        }
        val oldUrl = getUrlFromAdvertisement()
        if (imageAdapter.mainArray.size > imageIndex) {
            val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
            if (oldUrl.startsWith("http")) {
                updateImage(byteArray, oldUrl) {
                    nextImage(it.result.toString())
                }
            } else {
                uploadSingleImage(byteArray) {
//                    dbManager.publishAdvertisement(advertisement!!, onPublishFinish())
                    nextImage(it.result.toString())
                }
            }

        } else {
            if (oldUrl.startsWith("http")) {
                deleteImageByUrl(oldUrl) {
                    nextImage("empty")
                }
            } else {
                nextImage("empty")
            }
        }
    }

    private fun nextImage(uri: String) {
        setImageUriToAdvertisement(uri)
        imageIndex++
        uploadImages()
    }

    private fun setImageUriToAdvertisement(uri: String) {
        when (imageIndex) {
            0 -> advertisement = advertisement?.copy(mainImage = uri)
            1 -> advertisement = advertisement?.copy(secondImage = uri)
            2 -> advertisement = advertisement?.copy(thirdImage = uri)
        }
    }

    private fun getUrlFromAdvertisement(): String {
        return listOf(
            advertisement?.mainImage!!,
            advertisement?.secondImage!!,
            advertisement?.thirdImage!!
        )[imageIndex]
    }

    private fun prepareImageByteArray(bitmap: Bitmap): ByteArray {
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream)
        return outStream.toByteArray()
    }

    private fun uploadSingleImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>) {
        val imStorageReference = dbManager.dbStorage
            .child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")
        val upTask = imStorageReference.putBytes(byteArray)
        upTask.continueWithTask { task ->
            imStorageReference.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun updateImage(byteArray: ByteArray, url: String, listener: OnCompleteListener<Uri>) {
        val imStorageReference = dbManager.dbStorage.storage.getReferenceFromUrl(url)
        val upTask = imStorageReference.putBytes(byteArray)
        upTask.continueWithTask { task ->
            imStorageReference.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun deleteImageByUrl(oldUrl: String, listener: OnCompleteListener<Void>) {
        dbManager.dbStorage.storage
            .getReferenceFromUrl(oldUrl).delete().addOnCompleteListener(listener)
    }

    private fun imageChangeCounter() {
        binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageCounter(position)
            }
        })
    }

    private fun updateImageCounter(counter: Int) {
        var index = 1
        val itemCount = binding.vpImages.adapter?.itemCount
        if (itemCount == 0) index = 0
        val imageCounter = "${counter + index}/$itemCount"
        binding.tvImageIndex.text = imageCounter
    }
}