package com.example.worldcup2026.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

object AdBlockerDetector {

    /**
     * Revisa si hay un bloqueador de publicidad activo (AdBlock DNS, Pi-hole, AdGuard VPN/DNS).
     * Comprueba:
     * 1. Si los dominios conocidos de publicidad (Google AdMob / Unity Ads) son bloqueados o resueltos a 0.0.0.0 / 127.0.0.1.
     * 2. Si hay una VPN activa que filtra tráfico.
     */
    suspend fun isAdBlockerActive(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Verificar resolución DNS de hosts de anuncios
            val testHosts = listOf(
                "googleads.g.doubleclick.net",
                "pagead2.googlesyndication.com",
                "auction.unityads.unity3d.com"
            )

            for (host in testHosts) {
                try {
                    val address = InetAddress.getByName(host)
                    val ip = address.hostAddress ?: ""
                    // Si resuelve a localhost o 0.0.0.0 es un bloqueo de DNS/hosts
                    if (ip == "127.0.0.1" || ip == "0.0.0.0" || ip.startsWith("::")) {
                        return@withContext true
                    }
                } catch (e: Exception) {
                    // Si falla la resolución de DNS de publicidad mientras hay conexión normal
                    if (isInternetAvailable(context)) {
                        return@withContext true
                    }
                }
            }

            // 2. Comprobar si hay una VPN activa (muy común en Blokada / AdGuard)
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                val activeNetwork = cm.activeNetwork
                val caps = cm.getNetworkCapabilities(activeNetwork)
                if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    return@withContext true
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
