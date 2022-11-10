package com.example.bulletinboardappkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage
import com.example.bulletinboardappkotlin.accounthelper.AccountHelper
import com.example.bulletinboardappkotlin.activities.DescriptionActivity
import com.example.bulletinboardappkotlin.activities.EditAdsActivity
import com.example.bulletinboardappkotlin.activities.FilterActivity
import com.example.bulletinboardappkotlin.activities.showToast
import com.example.bulletinboardappkotlin.adapters.AdvertisementRecyclerViewAdapter
import com.example.bulletinboardappkotlin.databinding.ActivityMainBinding
import com.example.bulletinboardappkotlin.dialoghelper.DialogConsts
import com.example.bulletinboardappkotlin.dialoghelper.DialogHelper
import com.example.bulletinboardappkotlin.model.Advertisement
import com.example.bulletinboardappkotlin.utils.FilterManager
import com.example.bulletinboardappkotlin.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    AdvertisementRecyclerViewAdapter.AdvertisementHolder.Listener {
    private lateinit var tvAccountTitle: TextView
    private lateinit var ivAccountHeaderImage: ImageView

    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth
    val advertisementRecyclerViewAdapter = AdvertisementRecyclerViewAdapter(this)
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    lateinit var filterLauncher: ActivityResultLauncher<Intent>
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var clearUpdate: Boolean = true
    private var currentCategory: String? = null
    private var filter: String = "empty"
    private var filterDb: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initRecyclerView()
        initViewModel()
        bottomMenuOnClick()
        scrollListener()
        onActivityResultFilter()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_filter_button, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.filter) {
            val intent = Intent(this@MainActivity, FilterActivity::class.java).apply {
                putExtra(FilterActivity.FILTER_KEY, filter)
            }
            filterLauncher.launch(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        binding.headerActivityMain.bNavView.selectedItemId = R.id.id_home
    }

    override fun onStart() {
        super.onStart()
        updateUI(mAuth.currentUser)
    }

    private fun initViewModel() {
        firebaseViewModel.liveAdsData.observe(this) {
            val list = getAdsByCategories(it)
            if (!clearUpdate) {
                advertisementRecyclerViewAdapter.updateAdvertisementAdapter(list)
            } else {
                advertisementRecyclerViewAdapter.updateWithClearAdapter(list)
            }
            binding.headerActivityMain.tvEmpty.visibility =
                if (advertisementRecyclerViewAdapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun getAdsByCategories(list: ArrayList<Advertisement>): ArrayList<Advertisement> {
        val tempList = ArrayList<Advertisement>()
        tempList.addAll(list)
        if (currentCategory != getString(R.string.toolbar_all_ads)) {
            tempList.clear()
            list.forEach {
                if (currentCategory == it.category) tempList.add(it)
            }
        }
        tempList.reverse()
        return tempList
    }

    private fun init() {
        currentCategory = getString(R.string.toolbar_all_ads)
        setSupportActionBar(binding.headerActivityMain.tbActivityMain)
        onActivityResult()
        navigationViewSetting()
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout,
            binding.headerActivityMain.tbActivityMain, R.string.open, R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener(this)
        tvAccountTitle = binding.navigationView
            .getHeaderView(0)
            .findViewById(R.id.tvAccountTitle)
        ivAccountHeaderImage = binding.navigationView
            .getHeaderView(0)
            .findViewById(R.id.ivAccountHeaderImage)
    }

    private fun bottomMenuOnClick() = with(binding) {
        headerActivityMain.bNavView.setOnItemSelectedListener { item ->
            clearUpdate = true
            when (item.itemId) {
                R.id.id_home -> {
                    currentCategory = getString(R.string.toolbar_all_ads)
                    firebaseViewModel.loadAllAdsFirstPage(filterDb)
                    headerActivityMain.tbActivityMain.title = getString(R.string.toolbar_all_ads)
                }
                R.id.id_favs -> {
                    firebaseViewModel.loadMyFavourites()
                    headerActivityMain.tbActivityMain.title =
                        getString(R.string.toolbar_favourite_ads)
                }
                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()
                    headerActivityMain.tbActivityMain.title = getString(R.string.toolbar_mine_ads)
                }
                R.id.id_new_advertisement -> {
                    if (mAuth.currentUser != null) {
                        if (!mAuth.currentUser?.isAnonymous!!) {
                            val intent = Intent(
                                this@MainActivity,
                                EditAdsActivity::class.java
                            )
                            startActivity(intent)
                        } else {
                            showToast("Для публикации объявления необходимо зарегистрироваться!")
                        }
                    } else {
                        showToast("Ошибка регистрации!")
                    }

                }
            }
            true
        }
    }

    private fun initRecyclerView() {
        binding.apply {
            headerActivityMain.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            headerActivityMain.rcView.adapter = advertisementRecyclerViewAdapter
        }
    }

    private fun onActivityResult() {
        googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        dialogHelper.accountHelper.signInFirebaseWithGoogle(account.idToken!!)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Api error: ${e.message}")
                }
            }
    }

    private fun onActivityResultFilter() {
        filterLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                filter = it.data?.getStringExtra(FilterActivity.FILTER_KEY)!!
//                Log.d("MyLog", "Filter: $filter")
//                Log.d("MyLog", "GetFilter: ${FilterManager.getFilter(filter)}")
                filterDb = FilterManager.getFilter(filter)
            } else if (it.resultCode == RESULT_CANCELED) {
                filter = "empty"
                filterDb = ""
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate = true
        when (item.itemId) {
            R.id.ads_mine_title -> {

            }
            R.id.ads_cars_title -> {
                getAdsFromCategory(getString(R.string.ads_cars_title))
            }
            R.id.ads_computers_title -> {
                getAdsFromCategory(getString(R.string.ads_computers_title))
            }
            R.id.ads_smartphones_title -> {
                getAdsFromCategory(getString(R.string.ads_smartphones_title))
            }
            R.id.ads_appliances_title -> {
                getAdsFromCategory(getString(R.string.ads_appliances_title))
            }
            R.id.account_sign_up_title -> {
                dialogHelper.createSignDialog(DialogConsts.SIGN_UP_STATE)
            }
            R.id.account_sign_in_title -> {
                dialogHelper.createSignDialog(DialogConsts.SIGN_IN_STATE)
            }
            R.id.account_sign_out_title -> {
                if (mAuth.currentUser?.isAnonymous == true) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                updateUI(null)
                mAuth.signOut()
                dialogHelper.accountHelper.signOutGoogle()
                toastMessage(
                    this, resources.getString(
                        R.string.sign_out_done
                    )
                )
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAdsFromCategory(category: String) {
        currentCategory = category
        firebaseViewModel.loadAllAdsFromCategoryFirstPage(category, filterDb)
    }


    fun updateUI(user: FirebaseUser?) {
        if (user == null) {
            dialogHelper.accountHelper.signInAnonymously(object : AccountHelper.Listener {
                override fun onComplete() {
                    tvAccountTitle.text = getString(R.string.account_sign_in_anonymously)
                    ivAccountHeaderImage.setImageResource(R.drawable.ic_default_image_account)
                }
            })
        } else if (user.isAnonymous) {
            tvAccountTitle.text = getString(R.string.account_sign_in_anonymously)
            ivAccountHeaderImage.setImageResource(R.drawable.ic_default_image_account)
        } else if (!user.isAnonymous) {
            tvAccountTitle.text = user.email
            Picasso.get().load(user.photoUrl).into(ivAccountHeaderImage)
        }
    }

    override fun onDeleteItem(advertisement: Advertisement) {
        firebaseViewModel.deleteItem(advertisement)
    }

    override fun onAdvertisementViewed(advertisement: Advertisement) {
        firebaseViewModel.advertisementViewed(advertisement)
        val intent = Intent(this, DescriptionActivity::class.java)
        intent.putExtra(DescriptionActivity.ADVERTISEMENT, advertisement)
        startActivity(intent)
    }

    override fun onFavouriteClicked(advertisement: Advertisement) {
        firebaseViewModel.onFavouriteClick(advertisement)
    }

    private fun navigationViewSetting() = with(binding) {
        val menu = navigationView.menu
        val adsCategory = menu.findItem(R.id.ads_category)
        val spanAdsCategory = SpannableString(adsCategory.title)
        adsCategory.title?.let {
            spanAdsCategory.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(this@MainActivity, R.color.red_main)
                ),
                0, it.length, 0
            )
        }
        adsCategory.title = spanAdsCategory

        val accountCategory = menu.findItem(R.id.account_category)
        val spanAccountCategory = SpannableString(accountCategory.title)
        accountCategory.title?.let {
            spanAccountCategory.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(this@MainActivity, R.color.red_main)
                ),
                0, it.length, 0
            )
        }
        accountCategory.title = spanAccountCategory
    }

    private fun scrollListener() = with(binding.headerActivityMain) {
        rcView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(SCROLL_DOWN) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE
                ) {
                    clearUpdate = false
                    val adsList = firebaseViewModel.liveAdsData.value!!
                    if (adsList.isNotEmpty()) {
                        getAdsFromCategory(adsList)
                    }
                }
            }
        })
    }

    private fun getAdsFromCategory(adsList: ArrayList<Advertisement>) {
        adsList[0].let {
            if (currentCategory == getString(R.string.toolbar_all_ads)) {
                firebaseViewModel.loadAllAdsNextPage(it.time, filterDb)
            } else {
                firebaseViewModel.loadAllAdsFromCategoryNextPage(it.category!!, it.time, filterDb)
            }
        }
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1
    }
}