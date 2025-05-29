package com.fsacchi.firestore.data.model

data class DeveloperModel(
    val id: String = "",
    var name: String = "",
    var idSlack: String = "",
    var idGit: String = "",
    var availableForReview: Boolean = false
)