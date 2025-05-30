package com.fsacchi.firestore.core.di

val presentationModules = listOf(
    servicesModule,
    networkModule,
    firebaseModule
) + useCaseModules + viewModelModules
