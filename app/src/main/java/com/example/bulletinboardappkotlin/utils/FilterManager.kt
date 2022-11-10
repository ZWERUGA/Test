package com.example.bulletinboardappkotlin.utils

import com.example.bulletinboardappkotlin.model.Advertisement
import com.example.bulletinboardappkotlin.model.AdvertisementFilter

object FilterManager {
    fun createFilter(ad: Advertisement): AdvertisementFilter {
        return AdvertisementFilter(
            ad.time,
            "${ad.category}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.city}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.city}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.city}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.city}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.withSend}_${ad.time}",
        )
    }

    fun getFilter(filter: String): String {
        val stringBuilderNode = StringBuilder()
        val stringBuilderFilter = StringBuilder()
        val tempArray = filter.split("_")
        if (tempArray[0] != "empty") {
            stringBuilderNode.append("country_")
            stringBuilderFilter.append("${tempArray[0]}_")
        }
        if (tempArray[1] != "empty") {
            stringBuilderNode.append("city_")
            stringBuilderFilter.append("${tempArray[1]}_")
        }
        if (tempArray[2] != "empty") {
            stringBuilderNode.append("index_")
            stringBuilderFilter.append("${tempArray[2]}_")
        }
        stringBuilderFilter.append(tempArray[3])
        stringBuilderNode.append("withSend_time")

        return "$stringBuilderNode|$stringBuilderFilter"
    }
}