package com.fsacchi.firestore.core.di

import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module

val servicesModule = module {
    single { FirebaseFirestore.getInstance() }
}
