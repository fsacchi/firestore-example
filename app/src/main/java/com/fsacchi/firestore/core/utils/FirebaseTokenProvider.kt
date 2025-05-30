package com.fsacchi.firestore.core.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.Base64

class FirebaseTokenProvider(
    serviceAccountJson: String
) {

    private val json = JSONObject(serviceAccountJson)

    private val clientEmail = json.getString("client_email")
    private val privateKeyPem = json.getString("private_key")
    private val tokenUri = json.getString("token_uri")
    private val projectId = json.getString("project_id")

    private val httpClient = OkHttpClient()

    suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        val jwt = createJwt()

        val requestBody = FormBody.Builder()
            .add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
            .add("assertion", jwt)
            .build()

        val request = Request.Builder()
            .url(tokenUri)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to get access token: ${response.body?.string()}")
        }

        val body = JSONObject(response.body!!.string())
        body.getString("access_token")
    }

    private fun createJwt(): String {
        val now = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000

        return JWT.create()
            .withIssuer(clientEmail)
            .withSubject(clientEmail)
            .withAudience(tokenUri)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + oneHour))
            .withClaim(
                "scope",
                "https://www.googleapis.com/auth/datastore"
            )
            .sign(Algorithm.RSA256(null, getPrivateKey()))
    }

    private fun getPrivateKey(): RSAPrivateKey {
        val privateKeyPEM = privateKeyPem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")

        val decoded = Base64.getDecoder().decode(privateKeyPEM)
        val keySpec = PKCS8EncodedKeySpec(decoded)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }

    fun getProjectId(): String = projectId
}

