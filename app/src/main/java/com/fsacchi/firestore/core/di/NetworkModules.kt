package com.fsacchi.firestore.core.di

import com.fsacchi.firestore.core.utils.FirebaseTokenProvider
import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}

val firebaseModule = module {
    single {
        FirebaseTokenProvider(
            serviceAccountJson = getProperty("FIREBASE_SERVICE_ACCOUNT")
        )
    }
}