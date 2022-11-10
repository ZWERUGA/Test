package com.example.bulletinboardappkotlin.model

data class AdvertisementFilter(
    val time: String? = null,
    val category_time: String? = null,

    // Filter with "Category"
    val category_country_withSend_time: String? = null,
    val category_country_city_withSend_time: String? = null,
    val category_country_city_index_withSend_time: String? = null,
    val category_index_withSend_time: String? = null,
    val category_withSend_time: String? = null,

    // Filter without "Category"
    val country_withSend_time: String? = null,
    val country_city_withSend_time: String? = null,
    val country_city_index_withSend_time: String? = null,
    val index_withSend_time: String? = null,
    val withSend_time: String? = null
)
