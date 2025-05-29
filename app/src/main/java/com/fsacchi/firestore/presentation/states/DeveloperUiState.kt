package com.fsacchi.firestore.presentation.states

import androidx.compose.runtime.Stable
import com.fsacchi.firestore.data.model.DeveloperModel

@Stable
data class DeveloperUiState(
    val screenType: ScreenType = ScreenType.Await
) {
    sealed interface ScreenType {
        data object Await: ScreenType

        data class Loaded(
            val developers: List<DeveloperModel>
        ): ScreenType

        data class Error(
            val errorMessage: String?
        ): ScreenType
    }
}
