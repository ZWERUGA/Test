package com.example.bulletinboardappkotlin.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboardappkotlin.MainActivity
import com.example.bulletinboardappkotlin.R
import com.example.bulletinboardappkotlin.activities.DescriptionActivity
import com.example.bulletinboardappkotlin.activities.EditAdsActivity
import com.example.bulletinboardappkotlin.model.Advertisement
import com.example.bulletinboardappkotlin.databinding.AdvertisementListItemBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdvertisementRecyclerViewAdapter(val activity: MainActivity) :
    RecyclerView.Adapter<AdvertisementRecyclerViewAdapter.AdvertisementHolder>() {

    val advertisementArray = ArrayList<Advertisement>()
    private var timeFormatter: SimpleDateFormat? = null

    init {
        timeFormatter = SimpleDateFormat("dd/MM/yyyy - hh:mm:ss", Locale.getDefault())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertisementHolder {
        val binding = AdvertisementListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return AdvertisementHolder(binding, activity, timeFormatter!!)
    }

    override fun onBindViewHolder(holder: AdvertisementHolder, position: Int) {
        holder.setData(advertisementArray[position])
    }

    override fun getItemCount(): Int {
        return advertisementArray.size
    }

    fun updateAdvertisementAdapter(newList: List<Advertisement>) {
        val tempArray = ArrayList<Advertisement>()
        tempArray.addAll(advertisementArray)
        tempArray.addAll(newList)
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(advertisementArray, tempArray))
        diffResult.dispatchUpdatesTo(this)
        advertisementArray.clear()
        advertisementArray.addAll(tempArray)
    }

    fun updateWithClearAdapter(newList: List<Advertisement>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(advertisementArray, newList))
        diffResult.dispatchUpdatesTo(this)
        advertisementArray.clear()
        advertisementArray.addAll(newList)
    }

    class AdvertisementHolder(
        val binding: AdvertisementListItemBinding,
        val activity: MainActivity,
        val formatter: SimpleDateFormat
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(advertisement: Advertisement) = with(binding) {
            tvDescription.text = advertisement.description
            tvPrice.text = advertisement.price
            tvTitle.text = advertisement.title
            tvViewCounter.text = advertisement.viewsCounter
            tvFavCounter.text = advertisement.favouritesCounter
            val publishTime = "Время публикации: ${getTimeFromMillis(advertisement.time)}"
            tvPublishTime.text = publishTime

            Picasso.get().load(advertisement.mainImage).into(ivMain)

            isFavourite(advertisement)
            showEditPanel(isOwner(advertisement))
            initButtonsClickListeners(advertisement)
        }

        private fun getTimeFromMillis(timeMillis: String): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeMillis.toLong()
            return formatter.format(calendar.time)
        }

        private fun initButtonsClickListeners(advertisement: Advertisement) = with(binding) {
            itemView.setOnClickListener {
                activity.onAdvertisementViewed(advertisement)
            }
            ibFavourite.setOnClickListener {
                if (activity.mAuth.currentUser?.isAnonymous == false) {
                    activity.onFavouriteClicked(
                        advertisement
                    )
                }
            }
            ibDeleteAdvertisement.setOnClickListener {
                activity.onDeleteItem(advertisement)
            }
            ibEditAdvertisement.setOnClickListener(onClickEditAdvertisement(advertisement))
        }

        private fun isFavourite(advertisement: Advertisement) = with(binding) {
            if (advertisement.isFavourite) {
                ibFavourite.setImageResource(R.drawable.ic_favorite_pressed)
            } else {
                ibFavourite.setImageResource(R.drawable.ic_favorite_normal)
            }
        }

        private fun onClickEditAdvertisement(advertisement: Advertisement): OnClickListener {
            return OnClickListener {
                val editIntent = Intent(activity, EditAdsActivity::class.java).apply {
                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ADS_DATA, advertisement)
                }
                activity.startActivity(editIntent)
            }
        }

        private fun isOwner(advertisement: Advertisement): Boolean {
            return advertisement.uid == activity.mAuth.uid
        }

        private fun showEditPanel(isOwner: Boolean) {
            if (isOwner) {
                binding.llEditPanel.visibility = View.VISIBLE
            } else {
                binding.llEditPanel.visibility = View.GONE
            }
        }

        interface Listener {
            fun onDeleteItem(advertisement: Advertisement)
            fun onAdvertisementViewed(advertisement: Advertisement)
            fun onFavouriteClicked(advertisement: Advertisement)
        }
    }
}