package com.example.bulletinboardappkotlin.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.adapters.ImageAdapter
import com.example.bulletinboardappkotlin.databinding.ActivityDescriptionBinding
import com.example.bulletinboardappkotlin.model.Advertisement
import com.example.bulletinboardappkotlin.utils.ImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter
    private var advertisement: Advertisement? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
        floatingActionButtons()
    }

    private fun init() {
        adapter = ImageAdapter()
        binding.apply {
            viewPager.adapter = adapter
        }
        getIntentFromMainActivity()
        imageChangeCounter()
    }

    private fun floatingActionButtons() = with(binding) {
        binding.fabTelephone.setOnClickListener { call() }
        binding.fabEmail.setOnClickListener { sendEmail() }
    }

    private fun call() {
        val callUri = "tel: ${advertisement?.telephone}"
        val intentCall = Intent(Intent.ACTION_DIAL)
        intentCall.data = callUri.toUri()
        startActivity(intentCall)
    }

    private fun sendEmail() {
        val intentSendEmail = Intent(Intent.ACTION_SEND)
        intentSendEmail.type = "message/rfs822"
        intentSendEmail.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(advertisement?.email))
            putExtra(Intent.EXTRA_SUBJECT, "Объявление")
            putExtra(Intent.EXTRA_TEXT, "Меня интересует ваше объявление!")
        }
        try {
            startActivity(Intent.createChooser(intentSendEmail, "Открыть с помощью ..."))
        } catch (e: ActivityNotFoundException) {
            toastMessage(this, "Нет приложения для отправки Email!")
        }
    }

    // region Заполнение ViewPager2
    private fun getIntentFromMainActivity() {
        advertisement = intent.getSerializableExtra(ADVERTISEMENT) as Advertisement
        if (advertisement != null) updateUI(advertisement!!)
    }
    // endregion

    // region Заполнение текстовой части объявления
    private fun fillTextViews(advertisement: Advertisement) = with(binding) {
        tvTitle.text = advertisement.title
        tvDescription.text = advertisement.description
        tvEmailValue.text = advertisement.email
        tvPriceValue.text = advertisement.price
        tvTelephoneValue.text = advertisement.telephone
        tvCountryValue.text = advertisement.country
        tvCityValue.text = advertisement.city
        tvIndexValue.text = advertisement.index
        tvWithSendValue.text = isWithSend(advertisement.withSend.toBoolean())
    }

    private fun isWithSend(withSend: Boolean): String {
        return if (withSend) {
            resources.getString(R.string.with_send_true)
        } else {
            resources.getString(R.string.with_send_false)
        }
    }

    private fun imageChangeCounter() {
        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position + 1}/${binding.viewPager.adapter?.itemCount}"
                binding.tvImageIndex.text = imageCounter
            }
        })
    }

    private fun updateUI(advertisement: Advertisement) {
        ImageManager.fillImageArray(advertisement, adapter)
        fillTextViews(advertisement)
    }
    // endregion

    companion object {
        const val ADVERTISEMENT = "ADVERTISEMENT"
    }
}