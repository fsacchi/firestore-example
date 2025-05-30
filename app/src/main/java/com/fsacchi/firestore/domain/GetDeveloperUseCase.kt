package com.fsacchi.firestore.domain

import com.fsacchi.firestore.core.utils.FirebaseTokenProvider
import com.fsacchi.firestore.data.model.DeveloperModel
import com.fsacchi.firestore.presentation.states.DeveloperUiState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.coroutines.resume

class GetDeveloperUseCase(
    private val httpClient: OkHttpClient,
    private val firebaseTokenProvider: FirebaseTokenProvider
) : UseCase.NoParam<DeveloperUiState>() {

    companion object {
        private const val QUANT_DEVELOPERS_FOR_REVIEW = 2
        private const val BASE_URL = "https://firestore.googleapis.com/v1"
    }

    override suspend fun execute(): Flow<DeveloperUiState> = flow {
        emit(DeveloperUiState(DeveloperUiState.ScreenType.Await))

        val result = selectDevelopersOrReset(
            firebaseTokenProvider.getProjectId(),
            firebaseTokenProvider.getAccessToken()
        )

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

    private suspend fun selectDevelopersOrReset(
        projectId: String,
        accessToken: String
    ): List<DeveloperModel>? {
        var developers = getAvailableDevelopers(projectId, accessToken)

        if (developers.size < QUANT_DEVELOPERS_FOR_REVIEW) {
            resetAllDevelopers(projectId, accessToken)
            developers = getAvailableDevelopers(projectId, accessToken)
        }

        return if (developers.size >= QUANT_DEVELOPERS_FOR_REVIEW) {
            val selected = developers.shuffled().take(QUANT_DEVELOPERS_FOR_REVIEW)
            markDevelopersAsUnavailable(projectId, accessToken, selected)
            selected
        } else {
            null
        }
    }

    private suspend fun getAvailableDevelopers(
        projectId: String,
        accessToken: String
    ): List<DeveloperModel> {
        return getDevelopers(projectId, accessToken).filter { it.availableForReview }
    }

    private suspend fun resetAllDevelopers(
        projectId: String,
        accessToken: String
    ) {
        val all = getDevelopers(projectId, accessToken)
        all.forEach { updateDeveloper(projectId, accessToken, it, availableForReview = true) }
    }

    private suspend fun markDevelopersAsUnavailable(
        projectId: String,
        accessToken: String,
        developers: List<DeveloperModel>
    ) {
        developers.forEach { updateDeveloper(projectId, accessToken, it, availableForReview = false) }
    }

    private suspend fun getDevelopers(
        projectId: String,
        accessToken: String
    ): List<DeveloperModel> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/projects/$projectId/databases/(default)/documents/developers"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        val response = httpClient.newCall(request).execute()

        if (!response.isSuccessful) return@withContext emptyList()

        val body = response.body?.string() ?: return@withContext emptyList()

        val json = JSONObject(body)
        val documents = json.optJSONArray("documents") ?: return@withContext emptyList()

        (0 until documents.length()).mapNotNull { index ->
            val doc = documents.getJSONObject(index)
            parseDeveloper(doc)
        }
    }

    private suspend fun updateDeveloper(
        projectId: String,
        accessToken: String,
        developer: DeveloperModel,
        availableForReview: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/projects/$projectId/databases/(default)/documents/developers/${developer.id}"

        val json = JSONObject()
        val fields = JSONObject()

        fields.put("name", jsonString(developer.name))
        fields.put("idGit", jsonString(developer.idGit))
        fields.put("idSlack", jsonString(developer.idSlack))
        fields.put("availableForReview", jsonBoolean(availableForReview))

        json.put("fields", fields)

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .patch(body)
            .build()

        val response = httpClient.newCall(request).execute()
        response.isSuccessful
    }

    private fun parseDeveloper(doc: JSONObject): DeveloperModel? {
        return try {
            val name = doc.getJSONObject("fields").getJSONObject("name").getString("stringValue")
            val idGit = doc.getJSONObject("fields").getJSONObject("idGit").getString("stringValue")
            val idSlack = doc.getJSONObject("fields").getJSONObject("idSlack").getString("stringValue")
            val available = doc.getJSONObject("fields")
                .getJSONObject("availableForReview")
                .optBoolean("booleanValue", true)

            val nameParts = doc.getString("name").split("/")
            val id = nameParts.last()

            DeveloperModel(
                id = id,
                name = name,
                idGit = idGit,
                idSlack = idSlack,
                availableForReview = available
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun jsonString(value: String) = JSONObject().put("stringValue", value)
    private fun jsonBoolean(value: Boolean) = JSONObject().put("booleanValue", value)
}


