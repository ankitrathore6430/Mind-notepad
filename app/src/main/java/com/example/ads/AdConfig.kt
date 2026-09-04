package com.example.ads

/**
 * AdMob Configuration & Ad Unit IDs.
 *
 * Yahan aap apni AdMob App ID aur Ad Unit IDs store kar sakte hain.
 * Future me jab bhi aapko new Ad Unit IDs lagani ho, sirf is file me values update karein:
 */
object AdConfig {
  /**
   * AdMob App ID (Google Test ID by default)
   * Real app ID AndroidManifest.xml me bhi update karni hoti hai.
   */
  const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"

  /**
   * Home Screen Banner Ad Unit ID
   * Default: ca-app-pub-3940256099942544/6300978111
   */
  var BANNER_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/6300978111"

  /**
   * Interstitial (Full Screen) Ad Unit ID
   * Default: ca-app-pub-3940256099942544/1033173712
   */
  var INTERSTITIAL_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/1033173712"
}
