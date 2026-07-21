package com.example.worldcup2026.data.api

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class AuthManager(private val context: Context) {

    // Cambia el ID de cliente Web por el tuyo de Google Cloud Console (Firebase)
    private val webClientId = "1087567707485-cph5stfpb9fpolevdno6vvq8237kltfi.apps.googleusercontent.com"

    suspend fun signInWithGoogle(): String? {
        var activityContext: Context = context
        while (activityContext is android.content.ContextWrapper) {
            if (activityContext is android.app.Activity) break
            activityContext = activityContext.baseContext
        }

        val credentialManager = CredentialManager.create(activityContext)
        
        // Generate a nonce (a random number used once)
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )
            handleSignIn(result)
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            // Cancelado por el usuario o por el sistema
            Log.e("AuthManager", "Cancelado por el usuario", e)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(context, "Operación cancelada", android.widget.Toast.LENGTH_LONG).show()
            }
            return null
        } catch (e: Throwable) {
            Log.e("AuthManager", "Error getCredential", e)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(context, "Error CredentialManager: ${e.message ?: e.javaClass.simpleName}", android.widget.Toast.LENGTH_LONG).show()
            }
            return null
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): String? {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken
                
                // Conectar a Firebase
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
                
                // Retornar el token de Firebase
                val user = authResult.user
                val tokenResult = user?.getIdToken(true)?.await()
                return tokenResult?.token
            } catch (e: Exception) {
                Log.e("AuthManager", "Error al procesar el token de Google", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Error Firebase: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
                return null
            }
        } else {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(context, "Credential Type Mismatch: ${credential.javaClass.simpleName} | ${if (credential is CustomCredential) credential.type else "N/A"}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
        return null
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}
