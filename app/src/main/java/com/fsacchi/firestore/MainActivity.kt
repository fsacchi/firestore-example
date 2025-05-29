package com.fsacchi.firestore

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fsacchi.firestore.data.model.DeveloperModel
import com.fsacchi.firestore.presentation.features.DevelopersViewModel
import com.fsacchi.firestore.presentation.states.DeveloperUiState
import com.fsacchi.firestore.ui.theme.FirestoreTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val developersViewModel: DevelopersViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirestoreTheme {
                val context = LocalContext.current
                val uiState = developersViewModel.uiState.developer.collectAsState(
                    initial = DeveloperUiState()
                )

                LaunchedEffect(Unit) {
                    developersViewModel.getDevelopersAvailable()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        Button(
                            onClick = {
                                developersViewModel.getDevelopersAvailable()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(text = "Sortear Desenvolvedores")
                        }
                    }
                ) { innerPadding ->

                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                    ) {
                        when (val state = uiState.value.screenType) {
                            is DeveloperUiState.ScreenType.Await -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            is DeveloperUiState.ScreenType.Loaded -> {
                                DeveloperListContent(
                                    developers = state.developers
                                )
                            }

                            is DeveloperUiState.ScreenType.Error -> {
                                LaunchedEffect(state) {
                                    Toast.makeText(
                                        context,
                                        state.errorMessage ?: "Erro desconhecido",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                Text(
                                    text = "Ocorreu um erro",
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeveloperListContent(developers: List<DeveloperModel>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(developers) { developer ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = developer.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Git: ${developer.idGit}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Slack: ${developer.idSlack}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

