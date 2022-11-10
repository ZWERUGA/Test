package com.example.bulletinboardappkotlin.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.example.bulletinboardappkotlin.HelperFunctions
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.databinding.ActivityFilterBinding
import com.example.bulletinboardappkotlin.dialogspinnerhelper.DialogSpinnerHelper
import com.example.bulletinboardappkotlin.utils.CountryHelper
import java.util.logging.Filter

class FilterActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        actionbarSettings()
        initButtons()
        getFilter()
    }

    private fun initButtons() = with(binding) {
        btSelectCountry.setOnClickListener {
            val listCountries = CountryHelper.getAllCountries(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, listCountries, btSelectCountry)
            if (btSelectCity.text.toString() != getString(R.string.select_city)) {
                btSelectCity.text = getString(R.string.select_city)
            }
        }

        btSelectCity.setOnClickListener {
            val selectedCountry = btSelectCountry.text.toString()
            if (selectedCountry != getString(R.string.select_country)) {
                val listCities = CountryHelper.getAllCities(selectedCountry, this@FilterActivity)
                dialog.showSpinnerDialog(this@FilterActivity, listCities, btSelectCity)
            } else {
                HelperFunctions.toastMessage(this@FilterActivity, "Страна не выбрана!")
            }
        }

        btFilterDone.setOnClickListener {
            val intent = Intent().apply {
                putExtra(FILTER_KEY, createFilter())
            }
            setResult(RESULT_OK, intent)
            finish()
        }

        btFilterClear.setOnClickListener {
            btSelectCountry.text = getString(R.string.select_country)
            btSelectCity.text = getString(R.string.select_city)
            etIndex.setText("")
            cbWithSend.isChecked = false
            setResult(RESULT_CANCELED)
        }
    }

    private fun getFilter() = with(binding) {
        val filter = intent.getStringExtra(FILTER_KEY)
        if (filter != null && filter != "empty") {
            val filterArray = filter.split("_")
            if (filterArray[0] != "empty") {
                btSelectCountry.text = filterArray[0]
            }
            if (filterArray[1] != "empty") {
                btSelectCity.text = filterArray[1]
            }
            if (filterArray[2] != "empty") etIndex.setText(filterArray[2])
            cbWithSend.isChecked = filterArray[3].toBoolean()
        }
    }

    private fun createFilter(): String = with(binding) {
        val stringBuilder = StringBuilder()
        val arrayTempFilter = listOf(
            btSelectCountry.text,
            btSelectCity.text,
            etIndex.text,
            cbWithSend.isChecked.toString()
        )

        for ((index, str) in arrayTempFilter.withIndex()) {
            if (str != getString(R.string.select_country) &&
                str != getString(R.string.select_city) &&
                str.isNotEmpty()
            ) {
                stringBuilder.append(str)
                if (index != arrayTempFilter.size - 1) stringBuilder.append("_")
            } else {
                stringBuilder.append("empty")
                if (index != arrayTempFilter.size - 1) stringBuilder.append("_")
            }
        }

        return stringBuilder.toString()
    }

    fun actionbarSettings() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val FILTER_KEY = "FILTER_KEY"
    }
}