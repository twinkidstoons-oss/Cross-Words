package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages Google Mobile Ads using official Android TEST Ad Unit IDs only.
 * Gracefully falls back if offline or if ads fail to load.
 */
object AdManager {

    // Official Google AdMob Android TEST Ad Unit IDs
    const val INTERSTITIAL_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private const val MIN_INTERSTITIAL_INTERVAL_MS = 60_000L

    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false
    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false

    private var eligibleTriggerCount = 0
    private var lastInterstitialShownElapsedMs = -MIN_INTERSTITIAL_INTERVAL_MS
    private val mainHandler = Handler(Looper.getMainLooper())

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                MobileAds.initialize(appContext) {
                    mainHandler.post {
                        preloadInterstitial(appContext)
                        preloadRewarded(appContext)
                    }
                }
            }
        }
    }

    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || isLoadingInterstitial) return
        isLoadingInterstitial = true
        runCatching {
            val request = AdRequest.Builder().build()
            InterstitialAd.load(
                context.applicationContext,
                INTERSTITIAL_TEST_AD_UNIT_ID,
                request,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoadingInterstitial = false
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoadingInterstitial = false
                    }
                },
            )
        }.onFailure {
            isLoadingInterstitial = false
        }
    }

    fun preloadRewarded(context: Context) {
        if (rewardedAd != null || isLoadingRewarded) return
        isLoadingRewarded = true
        runCatching {
            val request = AdRequest.Builder().build()
            RewardedAd.load(
                context.applicationContext,
                REWARDED_TEST_AD_UNIT_ID,
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoadingRewarded = false
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        isLoadingRewarded = false
                    }
                },
            )
        }.onFailure {
            isLoadingRewarded = false
        }
    }

    /**
     * Shows an interstitial ad on eligible transitions at most every 2nd eligible trigger
     * and with at least 60 seconds between displays. Always invokes [onComplete] so navigation
     * never stalls when ads are unavailable or skipped.
     */
    fun maybeShowInterstitial(
        activity: Activity?,
        isWordSelectionActive: Boolean = false,
        onComplete: () -> Unit,
    ) {
        if (activity == null || isWordSelectionActive) {
            onComplete()
            return
        }

        eligibleTriggerCount++
        val now = SystemClock.elapsedRealtime()
        val enoughTimeElapsed = (now - lastInterstitialShownElapsedMs) >= MIN_INTERSTITIAL_INTERVAL_MS
        val isSecondEligibleTrigger = (eligibleTriggerCount % 2 == 0)

        val ad = interstitialAd
        if (!isSecondEligibleTrigger || !enoughTimeElapsed || ad == null) {
            if (ad == null) preloadInterstitial(activity)
            onComplete()
            return
        }

        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                lastInterstitialShownElapsedMs = SystemClock.elapsedRealtime()
            }

            override fun onAdDismissedFullScreenContent() {
                preloadInterstitial(activity)
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                preloadInterstitial(activity)
                onComplete()
            }
        }
        runCatching {
            ad.show(activity)
        }.onFailure {
            preloadInterstitial(activity)
            onComplete()
        }
    }

    /**
     * Shows a rewarded ad to earn bonus coins (+500). Grants reward ONLY inside [OnUserEarnedRewardListener].
     */
    fun showRewardedAdForCoins(
        activity: Activity?,
        onRewardEarned: () -> Unit,
        onUnavailable: () -> Unit,
    ) {
        showRewardedAd(
            activity = activity,
            onRewardEarned = onRewardEarned,
            onUnavailable = onUnavailable,
        )
    }

    /**
     * Shows a rewarded ad to earn a puzzle hint. Grants reward ONLY inside [OnUserEarnedRewardListener].
     */
    fun showRewardedAdForHint(
        activity: Activity?,
        onRewardEarned: () -> Unit,
        onUnavailable: () -> Unit,
    ) {
        showRewardedAd(
            activity = activity,
            onRewardEarned = onRewardEarned,
            onUnavailable = onUnavailable,
        )
    }

    private fun showRewardedAd(
        activity: Activity?,
        onRewardEarned: () -> Unit,
        onUnavailable: () -> Unit,
    ) {
        if (activity == null) {
            onUnavailable()
            return
        }
        val ad = rewardedAd
        if (ad == null) {
            preloadRewarded(activity)
            onUnavailable()
            return
        }

        rewardedAd = null
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadRewarded(activity)
                if (earned) {
                    onRewardEarned()
                }
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                preloadRewarded(activity)
                onUnavailable()
            }
        }
        runCatching {
            ad.show(activity) {
                earned = true
            }
        }.onFailure {
            preloadRewarded(activity)
            onUnavailable()
        }
    }
}
