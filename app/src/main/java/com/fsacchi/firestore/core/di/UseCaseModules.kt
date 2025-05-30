package com.fsacchi.firestore.core.di

import com.fsacchi.firestore.domain.GetDeveloperUseCase
import org.koin.dsl.module

private val devUseCases = module {
    single { GetDeveloperUseCase(get(), get()) }
}

internal val useCaseModules = listOf(
    devUseCases
)
