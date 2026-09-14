package com.example.worldcup2026.data.api

import com.example.worldcup2026.data.model.Group
import com.example.worldcup2026.data.model.Match
import com.example.worldcup2026.data.model.Team
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import java.net.InetAddress
import java.net.Socket
import java.lang.reflect.Type
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonDeserializationContext

interface WorldCupApiService {
    @GET("api/teams")
    suspend fun getTeams(@Query("tournament_id") tournamentId: Int? = null): List<Team>

    @GET("api/groups")
    suspend fun getGroups(@Query("tournament_id") tournamentId: Int? = null): List<Group>

    @GET("api/matches")
    suspend fun getMatches(@Query("tournament_id") tournamentId: Int? = null): List<Match>

    @GET("api/matches/live")
    suspend fun getLiveMatches(@Query("tournament_id") tournamentId: Int? = null): List<LiveMatchDto>

    @GET("api/tournaments/{id}/annual-standings")
    suspend fun getAnnualStandings(@Path("id") tournamentId: Int): List<AnnualStandingDto>

    @GET("api/tournaments/{id}/descenso-standings")
    suspend fun getDescensoStandings(@Path("id") tournamentId: Int): List<DescensoStandingDto>

    @GET("api/tournaments/{id}/goleadores")
    suspend fun getGoleadores(@Path("id") tournamentId: Int): List<GoleadorDto>

    @retrofit2.http.POST("api/admin/match/update")
    suspend fun updateMatchAdmin(@retrofit2.http.Body req: AdminMatchUpdateRequest): retrofit2.Response<Unit>
}

data class AdminMatchUpdateRequest(
    val matchId: Int,
    val homeScore: Int?,
    val awayScore: Int?,
    val status: String
)

data class AnnualStandingDto(
    val pos: Int,
    val team_id: Int,
    val team_name: String,
    val logo_url: String,
    val pj: Int,
    val g: Int,
    val e: Int,
    val p: Int,
    val gf: Int,
    val gc: Int,
    val dg: Int,
    val pts: Int
)

data class DescensoStandingDto(
    val pos: Int,
    val team_id: Int,
    val team_name: String,
    val logo_url: String,
    val historical_pts: Int,
    val historical_pj: Int,
    val current_pts: Int,
    val current_pj: Int,
    val total_pts: Int,
    val total_pj: Int,
    val promedio: Double
)

data class GoleadorDto(
    val pos: Int,
    val player_name: String,
    val team_name: String,
    val logo_url: String,
    val goals: Int
)

data class LiveMatchDto(
    val matchId: Int,
    val tournament_id: Int? = null,
    val homeScore: Int?,
    val awayScore: Int?,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
    val status: String,
    val homePossession: Int?,
    val awayPossession: Int?,
    val homeShots: Int?,
    val awayShots: Int?,
    val homeFouls: Int?,
    val awayFouls: Int?,
    val homeCorners: Int?,
    val awayCorners: Int?,
    val homeSaves: Int?,
    val awaySaves: Int?,
    val homeYellowCards: Int?,
    val awayYellowCards: Int?,
    val homeRedCards: Int?,
    val awayRedCards: Int?,
    val homePasses: String?,
    val awayPasses: String?,
    val scorers: List<String>?,
    val events: List<String>?,
    val clock: String?
)

class TeamDeserializer : JsonDeserializer<Team> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Team {
        if (json == null || json.isJsonNull || !json.isJsonObject) {
            return Team(180, "Por definirse", "", "TBD", null, emptyList())
        }
        val obj = json.asJsonObject
        val id = if (obj.has("id") && !obj.get("id").isJsonNull) obj.get("id").asInt else 180
        val name = if (obj.has("name") && !obj.get("name").isJsonNull) obj.get("name").asString else "Por definirse"
        val flagUrl = if (obj.has("flagUrl") && !obj.get("flagUrl").isJsonNull) obj.get("flagUrl").asString else ""
        val group = if (obj.has("group") && !obj.get("group").isJsonNull) obj.get("group").asString else "TBD"
        val tournamentId = if (obj.has("tournament_id") && !obj.get("tournament_id").isJsonNull) obj.get("tournament_id").asInt else null
        return Team(id, name, flagUrl, group, tournamentId, emptyList())
    }
}

class Tls12SocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {
    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites
    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket =
        patch(delegate.createSocket(s, host, port, autoClose))
    override fun createSocket(host: String, port: Int): Socket =
        patch(delegate.createSocket(host, port))
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket =
        patch(delegate.createSocket(host, port, localHost, localPort))
    override fun createSocket(host: InetAddress, port: Int): Socket =
        patch(delegate.createSocket(host, port))
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket =
        patch(delegate.createSocket(address, port, localAddress, localPort))

    private fun patch(socket: Socket): Socket {
        if (socket is SSLSocket) {
            val supported = socket.supportedProtocols.toSet()
            val enabled = mutableListOf<String>()
            if (supported.contains("TLSv1.3")) enabled.add("TLSv1.3")
            if (supported.contains("TLSv1.2")) enabled.add("TLSv1.2")
            if (enabled.isNotEmpty()) {
                socket.enabledProtocols = enabled.toTypedArray()
            }
        }
        return socket
    }
}

object NetworkModule {
    const val BASE_URL = "https://ellocodelpedal.duckdns.org/"

    private val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    )

    private val sslContext = try {
        SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
    } catch (e: Exception) {
        null
    }

    private val customGson = com.google.gson.GsonBuilder()
        .registerTypeAdapter(Team::class.java, TeamDeserializer())
        .create()

    private val okHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(25, TimeUnit.SECONDS)
        readTimeout(25, TimeUnit.SECONDS)
        writeTimeout(25, TimeUnit.SECONDS)
        retryOnConnectionFailure(true)
        connectionSpecs(listOf(
            okhttp3.ConnectionSpec.MODERN_TLS,
            okhttp3.ConnectionSpec.COMPATIBLE_TLS,
            okhttp3.ConnectionSpec.CLEARTEXT
        ))
        if (sslContext != null) {
            sslSocketFactory(Tls12SocketFactory(sslContext.socketFactory), trustAllCerts[0] as X509TrustManager)
            hostnameVerifier { _, _ -> true }
        }
    }.build()

    val apiService: WorldCupApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(customGson))
            .build()
            .create(WorldCupApiService::class.java)
    }

    const val PRODE_BASE_URL = "https://ellocodelpedal.duckdns.org/"

    val prodeApiService: ProdeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(PRODE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(customGson))
            .build()
            .create(ProdeApiService::class.java)
    }
}

