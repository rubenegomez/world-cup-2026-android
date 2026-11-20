package com.example.worldcup2026.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

object AdBlockerDetector {

    /**
     * Revisa si hay un bloqueador de publicidad activo (AdBlock DNS, Pi-hole, AdGuard DNS).
     * Comprueba si los dominios conocidos de publicidad (Unity Ads / Adsterra) son bloqueados
     * o resueltos a 0.0.0.0 / 127.0.0.1 sin bloquear conexiones VPN legítimas.
     */
    suspend fun isAdBlockerActive(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val testHosts = listOf(
                "auction.unityads.unity3d.com",
                "www.highrevenueformat.com"
            )

            for (host in testHosts) {
                try {
                    val address = InetAddress.getByName(host)
                    val ip = address.hostAddress ?: ""
                    if (ip == "127.0.0.1" || ip == "0.0.0.0" || ip.startsWith("::")) {
                        return@withContext true
                    }
                } catch (e: Exception) {
                    if (isInternetAvailable(context)) {
                        return@withContext true
                    }
                }
            }

            false
        } catch (e: Exception) {
            false
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }
}
