package com.example.worldcup2026.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"

    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading = false

    // Carga un anuncio intersticial de forma asíncrona para que esté listo cuando se requiera
    fun loadInterstitialAd(context: Context) {
        if (mInterstitialAd != null || isLoading) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_TEST_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    isLoading = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    isLoading = false
                }
            }
        )
    }

    // Muestra el anuncio intersticial si está cargado; si no, ejecuta directamente la acción de retorno
    fun showInterstitialAd(context: Context, onComplete: () -> Unit) {
        val activity = context as? Activity
        val ad = mInterstitialAd

        if (activity != null && ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    onComplete()
                    // Cargar el siguiente de manera preventiva
                    loadInterstitialAd(context)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mInterstitialAd = null
                    onComplete()
                }
            }
            ad.show(activity)
        } else {
            // Si no está listo, intentamos cargarlo y procedemos directamente para no bloquear la UX
            loadInterstitialAd(context)
            onComplete()
        }
    }
}

@Composable
fun AdmobBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
