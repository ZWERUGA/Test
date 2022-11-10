package com.example.bulletinboardappkotlin.model

import com.example.bulletinboardappkotlin.utils.FilterManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DatabaseManager {
    val db = Firebase.database.getReference(MAIN_NODE)
    val dbStorage = Firebase.storage.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun publishAdvertisement(advertisement: Advertisement, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null) {
            db.child(advertisement.key ?: "empty")
                .child(auth.uid!!)
                .child(ADVERTISEMENT_NODE)
                .setValue(advertisement).addOnCompleteListener {
                    val advertisementFilter = FilterManager.createFilter(advertisement)
                    db.child(advertisement.key ?: "empty")
                        .child(FILTER_NODE)
                        .setValue(advertisementFilter).addOnCompleteListener {
                            finishWorkListener.onFinish(it.isSuccessful)
                        }
                }
        }
    }

    fun deleteAdvertisement(advertisement: Advertisement, listener: FinishWorkListener) {
        if (advertisement.key == null || advertisement.uid == null) return
        val map = mapOf(
            "/${FILTER_NODE}" to null,
            "/${INFORMATION_NODE}" to null,
            "/${FAVOURITE_NODE}" to null,
            "/${advertisement.uid}" to null,
        )
        db.child(advertisement.key).updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) deleteImagesFromStorage(advertisement, 0, listener)
        }
    }

    private fun deleteImagesFromStorage(
        advertisement: Advertisement,
        index: Int,
        listener: FinishWorkListener
    ) {
        val imageList =
            listOf(advertisement.mainImage, advertisement.secondImage, advertisement.thirdImage)
        if (advertisement.mainImage == "empty") {
            listener.onFinish(true)
            return
        }
        dbStorage.storage.getReferenceFromUrl(imageList[index]).delete().addOnCompleteListener {
            if (it.isSuccessful) {
                if (imageList.size > index + 1) {
                    if (imageList[index + 1] != "empty") {
                        deleteImagesFromStorage(advertisement, index + 1, listener)
                    } else {
                        listener.onFinish(true)
                    }
                } else {
                    listener.onFinish(true)
                }
            }
        }
    }

    fun advertisementViewed(advertisement: Advertisement) {
        var counter = advertisement.viewsCounter.toInt()
        counter += 1
        if (auth.uid != null) {
            db.child(advertisement.key ?: "empty")
                .child(INFORMATION_NODE)
                .setValue(
                    ItemInformation(
                        counter.toString(),
                        advertisement.emailsCounter,
                        advertisement.callsCounter
                    )
                )
        }
    }

    private fun readDataFromDatabase(query: Query, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val advertisementArray = ArrayList<Advertisement>()
                for (item in snapshot.children) {
                    var advertisement: Advertisement? = null
                    item.children.forEach {
                        if (advertisement == null) {
                            advertisement =
                                it.child(ADVERTISEMENT_NODE).getValue(Advertisement::class.java)
                        }
                    }

                    val itemInfo =
                        item.child(INFORMATION_NODE).getValue(ItemInformation::class.java)
                    val favouritesCounter = item.child(FAVOURITE_NODE).childrenCount
                    advertisement?.viewsCounter = itemInfo?.viewsCounter ?: "0"
                    advertisement?.emailsCounter = itemInfo?.emailsCounter ?: "0"
                    advertisement?.callsCounter = itemInfo?.callsCounter ?: "0"
                    advertisement?.favouritesCounter = favouritesCounter.toString()
                    val isFavourite = auth.uid?.let {
                        item.child(FAVOURITE_NODE).child(it).getValue(String::class.java)
                    }
                    advertisement?.isFavourite = isFavourite != null

                    if (advertisement != null) {
                        advertisementArray.add(advertisement!!)
                    }
                }
                readDataCallback?.readData(advertisementArray)
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun readNextPageFromDatabase(
        query: Query,
        filter: String,
        orderBy: String,
        readDataCallback: ReadDataCallback?
    ) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val advertisementArray = ArrayList<Advertisement>()
                for (item in snapshot.children) {
                    var advertisement: Advertisement? = null
                    item.children.forEach {
                        if (advertisement == null) {
                            advertisement =
                                it.child(ADVERTISEMENT_NODE).getValue(Advertisement::class.java)
                        }
                    }

                    val itemInfo =
                        item.child(INFORMATION_NODE).getValue(ItemInformation::class.java)
                    val filterNodeValue = item.child(FILTER_NODE).child(orderBy).value.toString()
                    val favouritesCounter = item.child(FAVOURITE_NODE).childrenCount
                    advertisement?.viewsCounter = itemInfo?.viewsCounter ?: "0"
                    advertisement?.emailsCounter = itemInfo?.emailsCounter ?: "0"
                    advertisement?.callsCounter = itemInfo?.callsCounter ?: "0"
                    advertisement?.favouritesCounter = favouritesCounter.toString()
                    val isFavourite = auth.uid?.let {
                        item.child(FAVOURITE_NODE).child(it).getValue(String::class.java)
                    }
                    advertisement?.isFavourite = isFavourite != null

                    if (advertisement != null && filterNodeValue.startsWith(filter)) {
                        advertisementArray.add(advertisement!!)
                    }
                }
                readDataCallback?.readData(advertisementArray)
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    interface ReadDataCallback {
        fun readData(list: ArrayList<Advertisement>)
    }

    interface FinishWorkListener {
        fun onFinish(isDone: Boolean)
    }

    fun getMyAds(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild(auth.uid + "/advertisement/uid").equalTo(auth.uid)
        readDataFromDatabase(query, readDataCallback)
    }

    fun getAllAdsFirstPage(filter: String, readDataCallback: ReadDataCallback?) {
        val query = if (filter.isEmpty()) {
            db.orderByChild("/${FILTER_NODE}/time").limitToLast(ADVERTISEMENT_LIMIT)
        } else {
            getAllAdsByFilterFirstPage(filter)
        }
        readDataFromDatabase(query, readDataCallback)
    }

    fun getAllAdsByFilterFirstPage(filter: String): Query {
        val orderBy = filter.split("|")[0]
        val filterBy = filter.split("|")[1]
        return db.orderByChild("/${FILTER_NODE}/$orderBy").startAt(filterBy)
            .endAt(filterBy + "\uf8ff").limitToLast(ADVERTISEMENT_LIMIT)
    }

    fun getAllAdsNextPage(time: String, filter: String, readDataCallback: ReadDataCallback?) {
        if (filter.isEmpty()) {
            val query = db.orderByChild("/${FILTER_NODE}/time")
                .endBefore(time).limitToLast(ADVERTISEMENT_LIMIT)
            readDataFromDatabase(query, readDataCallback)
        } else {
            getAllAdsByFilterNextPage(filter, time, readDataCallback)
        }
    }

    private fun getAllAdsByFilterNextPage(
        filter: String,
        time: String,
        readDataCallback: ReadDataCallback?
    ) {
        val orderBy = filter.split("|")[0]
        val filterBy = filter.split("|")[1]
        val query = db.orderByChild("/${FILTER_NODE}/$orderBy")
            .endBefore(filterBy + "_$time").limitToLast(ADVERTISEMENT_LIMIT)
        readNextPageFromDatabase(query, filterBy, orderBy, readDataCallback)
    }

    fun getAllAdsFromCategoryFirstPage(
        category: String,
        filter: String,
        readDataCallback: ReadDataCallback?
    ) {
        val query = if (filter.isEmpty()) {
            db.orderByChild("/${FILTER_NODE}/category_time").startAt(category)
                .endAt(category + "_\uf8ff").limitToLast(ADVERTISEMENT_LIMIT)
        } else {
            getAllAdsFromCategoryByFilterFirstPage(category, filter)
        }
        readDataFromDatabase(query, readDataCallback)
    }

    fun getAllAdsFromCategoryByFilterFirstPage(category: String, filter: String): Query {
        val orderBy = "category_" + filter.split("|")[0]
        val filterBy = category + "_" + filter.split("|")[1]
        return db.orderByChild("/${FILTER_NODE}/$orderBy").startAt(filterBy)
            .endAt(filterBy + "\uf8ff").limitToLast(ADVERTISEMENT_LIMIT)
    }

    fun getAllAdsFromCategoryNextPage(
        category: String,
        time: String,
        filter: String,
        readDataCallback: ReadDataCallback?
    ) {
        if (filter.isEmpty()) {
            val query = db.orderByChild("/${FILTER_NODE}/category_time")
                .endBefore(category + "_" + time).limitToLast(ADVERTISEMENT_LIMIT)
            readDataFromDatabase(query, readDataCallback)
        } else {
            getAllAdsFromCategoryByFilterNextPage(category, time, filter, readDataCallback)
        }

    }

    private fun getAllAdsFromCategoryByFilterNextPage(
        category: String,
        time: String,
        filter: String,
        readDataCallback: ReadDataCallback?
    ) {
        val orderBy = "category_" + filter.split("|")[0]
        val filterBy = category + "_" + filter.split("|")[1]
        val query = db.orderByChild("/${FILTER_NODE}/$orderBy")
            .endBefore(filterBy + "_" + time).limitToLast(ADVERTISEMENT_LIMIT)
        readNextPageFromDatabase(query, filterBy, orderBy, readDataCallback)
    }

    fun onFavouriteClick(advertisement: Advertisement, finishWorkListener: FinishWorkListener) {
        if (advertisement.isFavourite) {
            removeFromFavourites(advertisement, finishWorkListener)
        } else {
            addToFavourites(advertisement, finishWorkListener)
        }
    }

    fun getMyFavourites(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild("/${FAVOURITE_NODE}/${auth.uid}").equalTo(auth.uid)
        readDataFromDatabase(query, readDataCallback)
    }

    private fun addToFavourites(advertisement: Advertisement, listener: FinishWorkListener) {
        advertisement.key?.let {
            auth.uid?.let { uid ->
                db.child(it).child(FAVOURITE_NODE).child(uid).setValue(uid).addOnCompleteListener {
                    if (it.isSuccessful) {
                        listener.onFinish(true)
                    }
                }
            }
        }
    }

    private fun removeFromFavourites(advertisement: Advertisement, listener: FinishWorkListener) {
        advertisement.key?.let {
            auth.uid?.let { uid ->
                db.child(it).child(FAVOURITE_NODE).child(uid).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        listener.onFinish(true)
                    }
                }
            }
        }
    }

    companion object {
        const val ADVERTISEMENT_NODE = "advertisement"
        const val FILTER_NODE = "advertisement_filter"
        const val INFORMATION_NODE = "information"
        const val FAVOURITE_NODE = "favourite"
        const val MAIN_NODE = "main"
        const val ADVERTISEMENT_LIMIT = 3
    }
}