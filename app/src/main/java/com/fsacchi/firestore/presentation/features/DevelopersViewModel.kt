package com.fsacchi.firestore.presentation.features

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fsacchi.firestore.domain.GetDeveloperUseCase
import com.fsacchi.firestore.presentation.states.DeveloperUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class DevelopersViewModel(
    private val getDeveloperUseCase: GetDeveloperUseCase
) : ViewModel(), LifecycleObserver {

    val uiState = UiState()

    fun getDevelopersAvailable() {
        viewModelScope.launch{
            getDeveloperUseCase().collect {
                uiState.developer.emit(it)
            }
        }
    }

    data class UiState(
        val developer: MutableSharedFlow<DeveloperUiState> = MutableSharedFlow()
    )
}