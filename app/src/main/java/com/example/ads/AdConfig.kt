package com.example.ads

/**
 * AdMob Configuration & Ad Unit IDs.
 *
 * Yahan aap apni AdMob App ID aur Ad Unit IDs store kar sakte hain.
 * Future me jab bhi aapko new Ad Unit IDs lagani ho, sirf is file me values update karein:
 */
object AdConfig {
  /**
   * AdMob App ID
   */
  const val ADMOB_APP_ID = "ca-app-pub-3286067390245856~7901388310"

  /**
   * Home Screen Banner Ad Unit ID
   */
  var BANNER_AD_UNIT_ID: String = "ca-app-pub-3286067390245856/7322803811"

  /**
   * Interstitial (Full Screen) Ad Unit ID
   */
  var INTERSTITIAL_AD_UNIT_ID: String = "ca-app-pub-3286067390245856/3365201331"
}
