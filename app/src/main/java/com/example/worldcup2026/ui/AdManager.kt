package com.example.worldcup2026.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    const val INTERSTITIAL_REAL_ID = "ca-app-pub-7768012635304880/8383774129"
    const val BANNER_REAL_ID = "ca-app-pub-7768012635304880/7721429148"
    const val REWARDED_TEST_ID = "ca-app-pub-3940256099942544/5224354917"
    const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"

    val BANNER_AD_UNIT_ID: String
        get() = if (com.example.worldcup2026.BuildConfig.DEBUG) BANNER_TEST_ID else BANNER_REAL_ID

    val INTERSTITIAL_AD_UNIT_ID: String
        get() = if (com.example.worldcup2026.BuildConfig.DEBUG) INTERSTITIAL_TEST_ID else INTERSTITIAL_REAL_ID

    const val ADSTERRA_SMARTLINK_URL = "https://www.profitableratecpmnetwork.com/ugedck5w?key=8537e3c00b02fb7cb23b3137c7867de3"

    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading = false

    private var mRewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    fun loadRewardedAd(context: Context) {
        UnityAdsManager.loadRewardedAd()
        if (mRewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_TEST_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mRewardedAd = null
                    isRewardedLoading = false
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    mRewardedAd = rewardedAd
                    isRewardedLoading = false
                }
            }
        )
    }

    // House Ads de Videos e Imágenes propios alojados en el Servidor / App
    data class HouseVideoAd(
        val appName: String,
        val appTagline: String,
        val videoUrl: String,
        val targetUrl: String,
        val accentColor: Long = 0xFF00E676,
        val imageDrawableRes: Int? = null
    )

    val houseVideoAds = listOf(
        HouseVideoAd(
            appName = "Arena Prode",
            appTagline = "¡El Prode oficial con resultados en vivo y estadísticas VIP!",
            videoUrl = "https://ellocodelpedal.duckdns.org/videos/ads/arena_ad.mp4",
            targetUrl = "https://ellocodelpedal.duckdns.org/arena.html",
            accentColor = 0xFFFFD700,
            imageDrawableRes = com.example.worldcup2026.R.drawable.banner_arena_ad
        ),
        HouseVideoAd(
            appName = "Bondi Maps",
            appTagline = "¡Gestioná tus tarjetas de colectivo, saldos y viajes de forma fácil!",
            videoUrl = "https://ellocodelpedal.duckdns.org/videos/ads/bondi_ad.mp4",
            targetUrl = "https://ellocodelpedal.duckdns.org/bondi.html",
            accentColor = 0xFF00E676,
            imageDrawableRes = com.example.worldcup2026.R.drawable.banner_bondi_ad
        ),
        HouseVideoAd(
            appName = "TimeTracker Pro",
            appTagline = "¡Gestioná tus horas de trabajo, guardias y cobros con precisión!",
            videoUrl = "https://ellocodelpedal.duckdns.org/videos/ads/timetracker_ad.mp4",
            targetUrl = "https://ellocodelpedal.duckdns.org/timetracker.html",
            accentColor = 0xFFFFC107,
            imageDrawableRes = com.example.worldcup2026.R.drawable.banner_timetracker_ad
        ),
        HouseVideoAd(
            appName = "Los Fondos del Loco",
            appTagline = "¡Wallpapers Ultra HD y calidad AMOLED para tu celular!",
            videoUrl = "https://ellocodelpedal.duckdns.org/videos/ads/fondos_ad.mp4",
            targetUrl = "https://ellocodelpedal.duckdns.org/fondos.html",
            accentColor = 0xFFE040FB,
            imageDrawableRes = com.example.worldcup2026.R.drawable.banner_fondos_ad
        )
    )

    private var houseAdIndex = 0
    private var houseAdShowImage = false
    val currentHouseVideoAd = androidx.compose.runtime.mutableStateOf<HouseVideoAd?>(null)
    val currentHouseAdIsImage = androidx.compose.runtime.mutableStateOf(false)
    private var onHouseAdFinishedCallback: (() -> Unit)? = null

    fun showHouseVideoAd(onFinished: () -> Unit) {
        val ad = houseVideoAds[houseAdIndex % houseVideoAds.size]
        houseAdIndex++
        val showImageThisTime = houseAdShowImage && (ad.imageDrawableRes != null)
        houseAdShowImage = !houseAdShowImage // Alternar: una vez video, una vez imagen

        onHouseAdFinishedCallback = onFinished
        currentHouseAdIsImage.value = showImageThisTime
        currentHouseVideoAd.value = ad
    }

    fun dismissHouseVideoAd() {
        currentHouseVideoAd.value = null
        currentHouseAdIsImage.value = false
        onHouseAdFinishedCallback?.invoke()
        onHouseAdFinishedCallback = null
    }

    private var rewardedCycleIndex = 0
    private var interstitialCycleIndex = 0

    // Función auxiliar para mostrar video de AdMob
    private fun showAdmobRewarded(activity: Activity, onRewardGranted: () -> Unit, onFallback: () -> Unit) {
        val ad = mRewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mRewardedAd = null
                    loadRewardedAd(activity)
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mRewardedAd = null
                    onFallback()
                }
            }
            ad.show(activity) { _ ->
                onRewardGranted()
                Toast.makeText(activity, "🎉 ¡2 horas sin publicidad activadas!", Toast.LENGTH_SHORT).show()
            }
        } else {
            onFallback()
        }
    }

    // Diálogo Intersticial / Oferta Patrocinada de Adsterra
    val showAdsterraInterstitialDialog = androidx.compose.runtime.mutableStateOf(false)
    private var onAdsterraInterstitialCompletedCallback: (() -> Unit)? = null

    fun showAdsterraInterstitial(onComplete: () -> Unit) {
        onAdsterraInterstitialCompletedCallback = onComplete
        showAdsterraInterstitialDialog.value = true
    }

    fun dismissAdsterraInterstitial() {
        showAdsterraInterstitialDialog.value = false
        onAdsterraInterstitialCompletedCallback?.invoke()
        onAdsterraInterstitialCompletedCallback = null
    }

    // Rotación de Recompensas: 0 -> Adsterra (Smartlink/Patrocinio), 1 -> Google AdMob, 2 -> Unity Ads, 3 -> Videos Propios
    fun showRewardedAd(context: Context, onRewardGranted: () -> Unit) {
        val activity = context as? Activity ?: run {
            onRewardGranted()
            return
        }

        val turn = rewardedCycleIndex % 4
        rewardedCycleIndex++

        when (turn) {
            0 -> {
                // Turno Adsterra Smartlink (Recompensa inmediata con oferta patrocinada)
                showAdsterraInterstitial(onComplete = {
                    onRewardGranted()
                    Toast.makeText(activity, "🎉 ¡2 horas sin publicidad activadas!", Toast.LENGTH_SHORT).show()
                })
            }
            1 -> {
                // Turno Google AdMob
                showAdmobRewarded(activity, onRewardGranted, onFallback = {
                    UnityAdsManager.showRewardedAd(activity, onRewardGranted, onFallback = {
                        showHouseVideoAd(onRewardGranted)
                    })
                })
            }
            2 -> {
                // Turno Unity Ads
                UnityAdsManager.showRewardedAd(activity, onRewardGranted, onFallback = {
                    showAdmobRewarded(activity, onRewardGranted, onFallback = {
                        showHouseVideoAd(onRewardGranted)
                    })
                })
            }
            else -> {
                // Turno Nosotros (Arena Prode / Bondi / TimeTracker / Fondos)
                showHouseVideoAd(onRewardGranted)
            }
        }
    }

    fun loadInterstitialAd(context: Context) {
        UnityAdsManager.loadInterstitialAd()
        if (mInterstitialAd != null || isLoading) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
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

    private fun showAdmobInterstitial(activity: Activity, onComplete: () -> Unit, onFallback: () -> Unit) {
        val ad = mInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    onComplete()
                    loadInterstitialAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mInterstitialAd = null
                    onFallback()
                }
            }
            ad.show(activity)
        } else {
            onFallback()
        }
    }

    // Rotación para Intersticiales / Estadísticas VIP:
    // 0 -> Adsterra (Smartlink/Patrocinio), 1 -> Google AdMob, 2 -> Unity Ads, 3 -> Nosotros
    fun showInterstitialAd(context: Context, onComplete: () -> Unit) {
        val activity = context as? Activity ?: run {
            onComplete()
            return
        }

        val turn = interstitialCycleIndex % 4
        interstitialCycleIndex++

        when (turn) {
            0 -> {
                // Turno Adsterra Intersticial
                showAdsterraInterstitial(onComplete = onComplete)
            }
            1 -> {
                // Turno Google AdMob
                showAdmobInterstitial(activity, onComplete, onFallback = {
                    UnityAdsManager.showInterstitialAd(context, onComplete, onFallback = {
                        showHouseVideoAd(onComplete)
                    })
                })
            }
            2 -> {
                // Turno Unity Ads
                UnityAdsManager.showInterstitialAd(context, onComplete, onFallback = {
                    showAdmobInterstitial(activity, onComplete, onFallback = {
                        showHouseVideoAd(onComplete)
                    })
                })
            }
            else -> {
                // Turno Nosotros
                showHouseVideoAd(onComplete)
            }
        }
    }
}

@Composable
fun AdsterraBannerView(
    modifier: Modifier = Modifier,
    onBannerFailed: () -> Unit = {}
) {
    val htmlContent = remember {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body { margin: 0; padding: 0; background-color: transparent; display: flex; justify-content: center; align-items: center; overflow: hidden; }
            </style>
        </head>
        <body>
            <script type="text/javascript">
                atOptions = {
                    'key' : '0911397d317b072b7071544159b4d692',
                    'format' : 'iframe',
                    'height' : 50,
                    'width' : 320,
                    'params' : {}
                };
            </script>
            <script type="text/javascript" src="https://www.highrevenueformat.com/0911397d317b072b7071544159b4d692/invoke.js"></script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { ctx ->
            android.webkit.WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                webViewClient = object : android.webkit.WebViewClient() {
                    override fun onReceivedError(
                        view: android.webkit.WebView?,
                        request: android.webkit.WebResourceRequest?,
                        error: android.webkit.WebResourceError?
                    ) {
                        onBannerFailed()
                    }

                    override fun shouldOverrideUrlLoading(
                        view: android.webkit.WebView?,
                        request: android.webkit.WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString() ?: return false
                        return try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            ctx.startActivity(intent)
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }
                }
                loadDataWithBaseURL("https://www.highrevenueformat.com", htmlContent, "text/html", "UTF-8", null)
            }
        }
    )
}

@Composable
fun AdmobBanner(modifier: Modifier = Modifier) {
    // Rotación con prioridad a Adsterra:
    // 0: Adsterra (3 min = 180s), 1: AdMob (1 min = 60s), 2: Unity (1 min = 60s), 3: House Ads (1 min = 60s)
    var currentProvider by remember { mutableIntStateOf(0) }
    var providerFailed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            // Bloque 1: Adsterra Banner Webview (Prioridad: 3 minutos)
            currentProvider = 0
            providerFailed = false
            kotlinx.coroutines.delay(180000L)

            // Bloque 2: AdMob (1 minuto)
            currentProvider = 1
            providerFailed = false
            kotlinx.coroutines.delay(60000L)

            // Bloque 3: Unity Ads (1 minuto)
            currentProvider = 2
            providerFailed = false
            kotlinx.coroutines.delay(60000L)

            // Bloque 4: House Ads propios (1 minuto)
            currentProvider = 3
            providerFailed = false
            kotlinx.coroutines.delay(60000L)
        }
    }

    if (currentProvider == 3 || providerFailed) {
        HouseBannerFallback(modifier = modifier)
    } else if (currentProvider == 0) {
        AdsterraBannerView(
            modifier = modifier,
            onBannerFailed = {
                providerFailed = true
            }
        )
    } else if (currentProvider == 2) {
        UnityBannerView(
            modifier = modifier,
            onBannerFailed = {
                providerFailed = true
            }
        )
    } else {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdManager.BANNER_AD_UNIT_ID
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                            providerFailed = true
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

data class HouseBannerItem(
    val title: String,
    val drawableRes: Int,
    val targetUrl: String
)

@Composable
fun HouseBannerFallback(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val houseBanners = remember {
        listOf(
            HouseBannerItem("Arena Prode y Torneos", com.example.worldcup2026.R.drawable.banner_arena_ad, "https://ellocodelpedal.duckdns.org/arena.html"),
            HouseBannerItem("Bondi Horarios y SUBE", com.example.worldcup2026.R.drawable.banner_bondi_ad, "https://ellocodelpedal.duckdns.org/bondi.html"),
            HouseBannerItem("TimeTracker Pro", com.example.worldcup2026.R.drawable.banner_timetracker_ad, "https://ellocodelpedal.duckdns.org/timetracker.html"),
            HouseBannerItem("Fondos del Loco", com.example.worldcup2026.R.drawable.banner_fondos_ad, "https://ellocodelpedal.duckdns.org/fondos.html")
        )
    }
    var bannerIndex by remember { mutableIntStateOf(0) }

    // Rotar banners propios cada 15 segundos
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(15000L)
            bannerIndex = (bannerIndex + 1) % houseBanners.size
        }
    }

    val currentBanner = houseBanners[bannerIndex]

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(currentBanner.targetUrl)).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
        color = Color(0xFF0A0F1D)
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = currentBanner.drawableRes),
            contentDescription = currentBanner.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds
        )
    }
}

@Composable
fun HouseVideoAdPlayerOverlay(
    houseAd: AdManager.HouseVideoAd,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var countdown by remember { mutableIntStateOf(5) }
    var canSkip by remember { mutableStateOf(false) }
    var videoError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            kotlinx.coroutines.delay(1000L)
            countdown--
        }
        canSkip = true
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { if (canSkip) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = canSkip,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0E14))
        ) {
            val isImageMode = AdManager.currentHouseAdIsImage.value && houseAd.imageDrawableRes != null

            if (isImageMode) {
                // Modo Imagen completa: Mostrar banner/diseño visual de alta resolución
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = houseAd.imageDrawableRes!!),
                        contentDescription = houseAd.appName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                    )
                }
            } else if (!videoError) {
                // Reproductor nativo VideoView con contenedor y bandas con texto de la app
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Banda superior explicativa
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⭐ ${houseAd.appName.uppercase()} — ${houseAd.appTagline}",
                            color = Color(houseAd.accentColor),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxWidth(),
                            factory = { ctx ->
                                android.widget.VideoView(ctx).apply {
                                    setVideoURI(android.net.Uri.parse(houseAd.videoUrl))
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        start()
                                    }
                                    setOnErrorListener { _, _, _ ->
                                        videoError = true
                                        true
                                    }
                                }
                            }
                        )
                    }

                    // Banda inferior
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Toque el botón inferior para instalar gratis",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            } else {
                // Vista de respaldo rica en caso de que el códec de video falle en el dispositivo
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(houseAd.accentColor).copy(alpha = 0.15f),
                        modifier = Modifier.size(100.dp),
                        border = BorderStroke(2.dp, Color(houseAd.accentColor))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⭐", fontSize = 48.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = houseAd.appName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(houseAd.accentColor)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = houseAd.appTagline,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Header con Cuenta Regresiva / Botón de Cerrar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ANUNCIO DESTACADO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(houseAd.accentColor),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    color = if (canSkip) Color(0xFFE53935) else Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable {
                        if (canSkip) onDismiss()
                    }
                ) {
                    Text(
                        text = if (canSkip) "✕ Omitir Anuncio" else "Omitir en ${countdown}s",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }

            // Barra inferior con botón de Descargar / Instalar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                color = Color(0xFF131F2E).copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(houseAd.accentColor).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = houseAd.appName,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(houseAd.accentColor)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = houseAd.appTagline,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    androidx.compose.material3.Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(houseAd.targetUrl)).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(houseAd.accentColor)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "INSTALAR",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdsterraInterstitialOverlay(
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var countdown by remember { mutableIntStateOf(5) }
    var canSkip by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            kotlinx.coroutines.delay(1000L)
            countdown--
        }
        canSkip = true
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { if (canSkip) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = canSkip,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            // Contenido central de patrocinio Adsterra Smartlink
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF5722).copy(alpha = 0.2f),
                    modifier = Modifier.size(90.dp),
                    border = BorderStroke(2.dp, Color(0xFFFF5722))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎁", fontSize = 42.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "OFERTA PATROCINADA",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF7043)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Descubrí promociones exclusivas y apps destacadas de nuestros patrocinadores para apoyar la app.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                androidx.compose.material3.Button(
                    onClick = {
                        try {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(AdManager.ADSTERRA_SMARTLINK_URL)
                            ).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        onDismiss()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5722)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        text = "🚀 VER OFERTA Y CONTINUAR",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            // Barra superior con botón para omitir tras 5 segundos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "PUBLICIDAD PATROCINADA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFAB91),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    color = if (canSkip) Color(0xFFE53935) else Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable {
                        if (canSkip) onDismiss()
                    }
                ) {
                    Text(
                        text = if (canSkip) "✕ Continuar" else "Esperar ${countdown}s",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}
