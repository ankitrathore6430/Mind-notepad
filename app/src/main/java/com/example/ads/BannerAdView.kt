package com.example.ads

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdView(
  modifier: Modifier = Modifier,
  adUnitId: String = AdConfig.BANNER_AD_UNIT_ID
) {
  val context = LocalContext.current
  var isAdLoaded by remember { mutableStateOf(false) }

  // Remember AdView across recompositions and cleanup on dispose
  val adView = remember(adUnitId) {
    AdView(context).apply {
      setAdSize(AdSize.BANNER)
      this.adUnitId = adUnitId
      setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
      adListener = object : AdListener() {
        override fun onAdLoaded() {
          super.onAdLoaded()
          isAdLoaded = true
          Log.d("BannerAdView", "Banner ad loaded successfully")
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
          super.onAdFailedToLoad(error)
          isAdLoaded = false
          Log.w("BannerAdView", "Banner ad failed to load: ${error.message}")
        }
      }
      loadAd(AdRequest.Builder().build())
    }
  }

  DisposableEffect(adView) {
    onDispose {
      try {
        adView.destroy()
      } catch (e: Exception) {
        Log.e("BannerAdView", "Error destroying AdView: ${e.message}")
      }
    }
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("banner_ad_container"),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .padding(vertical = 1.dp),
      contentAlignment = Alignment.Center
    ) {
      AndroidView(
        factory = { adView },
        modifier = Modifier.testTag("admob_banner_view")
      )
    }
  }
}
