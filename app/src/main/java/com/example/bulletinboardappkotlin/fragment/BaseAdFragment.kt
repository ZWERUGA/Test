package com.example.bulletinboardappkotlin.fragment

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.bulletinboardappkotlin.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

open class BaseAdFragment : Fragment(), IntersticialAdClose {

    lateinit var adView: AdView
    var interAd: InterstitialAd? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAd()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadInterstitialAd()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onPause() {
        super.onPause()
        adView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        adView.destroy()
    }

    private fun initAd() {
        MobileAds.initialize(activity as Activity)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context as Activity, getString(R.string.intersticial_ad_id), adRequest,
                object: InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interAd = ad
                    }
            })
    }

    fun showInterstitialAd() {
        if (interAd != null) {
            interAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onClose()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    onClose()
                }
            }
            interAd?.show(activity as Activity)
        } else {
            onClose()
        }
    }

    override fun onClose() {}
}