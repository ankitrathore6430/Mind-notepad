package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {
  private const val TAG = "InterstitialAdManager"

  private var interstitialAd: InterstitialAd? = null
  private var isLoading = false
  private var isInitialized = false

  // Counter to show interstitial after every note edit/creation
  private var noteSaveCount = 0
  private const val AD_FREQUENCY_THRESHOLD = 2 // Show every 2 saves so user experience is smooth

  /**
   * Initializes MobileAds SDK and preloads the first interstitial ad.
   */
  fun init(context: Context) {
    if (isInitialized) return
    try {
      MobileAds.initialize(context) { status ->
        Log.d(TAG, "AdMob MobileAds initialized: $status")
        isInitialized = true
        loadAd(context)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to initialize AdMob: ${e.message}")
    }
  }

  /**
   * Pre-loads an Interstitial Ad using configured unit ID.
   */
  fun loadAd(context: Context) {
    if (isLoading || interstitialAd != null) return
    isLoading = true

    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
      context,
      AdConfig.INTERSTITIAL_AD_UNIT_ID,
      adRequest,
      object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(ad: InterstitialAd) {
          Log.d(TAG, "Interstitial ad loaded successfully")
          interstitialAd = ad
          isLoading = false

          ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
              Log.d(TAG, "Interstitial ad dismissed")
              interstitialAd = null
              loadAd(context)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
              Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
              interstitialAd = null
              loadAd(context)
            }

            override fun onAdShowedFullScreenContent() {
              Log.d(TAG, "Interstitial ad showed full screen")
            }
          }
        }

        override fun onAdFailedToLoad(loadError: LoadAdError) {
          Log.w(TAG, "Interstitial ad failed to load: ${loadError.message}")
          interstitialAd = null
          isLoading = false
        }
      }
    )
  }

  /**
   * Called when user finishes editing a note or saves.
   * Shows interstitial ad based on frequency or immediately if available.
   */
  fun onNoteSaved(activity: Activity, onComplete: () -> Unit = {}) {
    noteSaveCount++
    if (noteSaveCount % AD_FREQUENCY_THRESHOLD == 0) {
      showAdIfAvailable(activity, onComplete)
    } else {
      onComplete()
    }
  }

  /**
   * Shows the interstitial ad if ready, or immediately calls onDismissed.
   */
  fun showAdIfAvailable(activity: Activity, onDismissedOrSkipped: () -> Unit = {}) {
    val ad = interstitialAd
    if (ad != null) {
      ad.fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
          interstitialAd = null
          loadAd(activity.applicationContext)
          onDismissedOrSkipped()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
          Log.e(TAG, "Interstitial failed to show: ${adError.message}")
          interstitialAd = null
          loadAd(activity.applicationContext)
          onDismissedOrSkipped()
        }
      }
      ad.show(activity)
    } else {
      loadAd(activity.applicationContext)
      onDismissedOrSkipped()
    }
  }
}
