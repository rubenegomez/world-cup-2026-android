package com.example.worldcup2026.data.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class AuthManager(private val context: Context) {

    // Cambia el ID de cliente Web por el tuyo de Google Cloud Console (Firebase)
    private val webClientId = "1087567707485-cph5stfpb9fpolevdno6vvq8237kltfi.apps.googleusercontent.com"

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun handleSignInResult(data: Intent?): String? {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken
        } catch (e: ApiException) {
            e.printStackTrace()
            null
        }
    }
}
