package com.fsacchi.firestore.domain

import com.fsacchi.firestore.data.model.DeveloperModel
import com.fsacchi.firestore.presentation.states.DeveloperUiState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class GetDeveloperUseCase(
    private val db: FirebaseFirestore
) : UseCase.NoParam<DeveloperUiState>() {

    override suspend fun execute(): Flow<DeveloperUiState> = flow {
        emit(DeveloperUiState(DeveloperUiState.ScreenType.Await))

        val result = selectDevelopersOrReset()

        result?.let { developers ->
            emit(
                DeveloperUiState(
                    DeveloperUiState.ScreenType.Loaded(developers)
                )
            )
        } ?: emit(
            DeveloperUiState(
                DeveloperUiState.ScreenType.Error(
                    "Não foi possível obter a quantidade mínima de devs"
                )
            )
        )
    }

    private suspend fun selectDevelopersOrReset(): List<DeveloperModel>? {
        var developers = getAvailableDevelopers()

        if (developers.size < QUANT_DEVELOPERS_FOR_REVIEW) {
            resetAllDevelopers()
            developers = getAvailableDevelopers()
        }

        return if (developers.size >= QUANT_DEVELOPERS_FOR_REVIEW) {
            val selected = developers.shuffled().take(QUANT_DEVELOPERS_FOR_REVIEW)
            markDevelopersAsUnavailable(selected)
            selected
        } else {
            null
        }
    }

    private suspend fun getAvailableDevelopers(): List<DeveloperModel> {
        return getDevelopers().filter { it.availableForReview }
    }

    private suspend fun resetAllDevelopers() {
        val allDevelopers = getDevelopers()
        allDevelopers.forEach { updateDeveloper(it, availableForReview = true) }
    }

    private suspend fun markDevelopersAsUnavailable(developers: List<DeveloperModel>) {
        developers.forEach { updateDeveloper(it, availableForReview = false) }
    }

    private suspend fun getDevelopers(): List<DeveloperModel> {
        return suspendCancellableCoroutine { continuation ->
            db.collection("developers")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val developers = querySnapshot.documents.mapNotNull { doc ->
                        try {
                            DeveloperModel(
                                id = doc.id,
                                name = doc.getString("name").orEmpty(),
                                idGit = doc.getString("idGit").orEmpty(),
                                idSlack = doc.getString("idSlack").orEmpty(),
                                availableForReview = doc.getBoolean("availableForReview") ?: true
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }
                    continuation.resume(developers)
                }
                .addOnFailureListener {
                    continuation.resume(emptyList())
                }
        }
    }

    private suspend fun updateDeveloper(developerModel: DeveloperModel, availableForReview: Boolean): Boolean {
        val updatedDeveloper = developerModel.copy(availableForReview = availableForReview)
        return suspendCancellableCoroutine { continuation ->
            db.collection("developers")
                .document(updatedDeveloper.id)
                .set(updatedDeveloper)
                .addOnSuccessListener { continuation.resume(true) }
                .addOnFailureListener { continuation.resume(false) }
        }
    }

    companion object {
        private const val QUANT_DEVELOPERS_FOR_REVIEW = 2
    }
}

