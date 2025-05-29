package com.fsacchi.firestore.core.di

import com.fsacchi.firestore.presentation.features.DevelopersViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private val devModules = module {
    viewModel { DevelopersViewModel(get()) }
}

internal val viewModelModules = listOf(
    devModules
)
