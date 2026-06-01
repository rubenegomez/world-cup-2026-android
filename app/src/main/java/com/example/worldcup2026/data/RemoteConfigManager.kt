package com.example.worldcup2026.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import org.json.JSONObject

object RemoteConfigManager {
    private var fixtureMap: Map<Int, Int> = emptyMap()

    fun init() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // 1 hour
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        // Defaults in case Firebase fails or hasn't loaded
        remoteConfig.setDefaultsAsync(mapOf(
            "fixture_id_map" to "{}"
        ))

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                parseConfig()
            } else {
                Log.e("RemoteConfigManager", "Fetch failed")
            }
        }
        
        // Always try to parse whatever is currently cached just in case
        parseConfig()
    }

    private fun parseConfig() {
        try {
            val jsonString = Firebase.remoteConfig.getString("fixture_id_map")
            if (jsonString.isNotEmpty() && jsonString != "{}") {
                val jsonObject = JSONObject(jsonString)
                val newMap = mutableMapOf<Int, Int>()
                
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    newMap[key.toInt()] = jsonObject.getInt(key)
                }
                
                fixtureMap = newMap
                Log.d("RemoteConfigManager", "Parsed ${fixtureMap.size} fixture IDs")
            }
        } catch (e: Exception) {
            Log.e("RemoteConfigManager", "Error parsing config", e)
        }
    }

    fun getApiId(localMatchId: Int): Int {
        // Returns mapped ID from Firebase if it exists, otherwise use fallback demonstration match 863234
        return fixtureMap[localMatchId] ?: 863234
    }
}
