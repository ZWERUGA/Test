package com.example.bulletinboardappkotlin.model

import java.io.Serializable

data class Advertisement(
    val country: String? = null,
    val city: String? = null,
    val telephone: String? = null,
    val index: String? = null,
    val withSend: String? = null,
    val category: String? = null,
    val title: String? = null,
    val price: String? = null,
    val description: String? = null,
    val email: String? = null,
    val mainImage: String = "empty",
    val secondImage: String = "empty",
    val thirdImage: String = "empty",
    val key: String? = null,
    val uid: String? = null,
    val time: String = "0",
    var viewsCounter: String = "0",
    var isFavourite: Boolean = false,
    var favouritesCounter: String = "0",
    var emailsCounter: String = "0",
    var callsCounter: String = "0",
): Serializable
