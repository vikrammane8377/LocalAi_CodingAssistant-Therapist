package com.example.programmingailocal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import com.example.programmingailocal.model.QWEN_MODEL
import com.example.programmingailocal.util.downloadModelFile
import kotlinx.coroutines.launch
import android.content.Context

sealed interface HomeUiState {
    data object Idle : HomeUiState
    data class Downloading(val progress: Float) : HomeUiState
    data object Ready : HomeUiState
}

class HomeViewModel(private val context: Context) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun prepareModel(onReady: () -> Unit) {
        viewModelScope.launch {
            val model = QWEN_MODEL
            val modelPath = model.getPath(context)
            val modelFile = java.io.File(modelPath)
            if (!modelFile.exists()) {
                _uiState.value = HomeUiState.Downloading(0f)
                val ok = downloadModelFile(
                    context,
                    model.url,
                    "${model.normalizedName}/${model.version}",
                    model.downloadFileName,
                ) { received, total ->
                    if (total > 0) _uiState.update { HomeUiState.Downloading(received / total.toFloat()) }
                }
                if (!ok) {
                    _uiState.value = HomeUiState.Idle
                    return@launch
                }
            }
            _uiState.value = HomeUiState.Ready
            onReady()
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(context) as T
                }
            }
    }
}

@Composable
fun HomeScreen(navigateToChat: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(context))
    val state by vm.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (state) {
            is HomeUiState.Idle -> {
                Button(onClick = { vm.prepareModel(navigateToChat) }) { Text("Start chat") }
            }
            is HomeUiState.Downloading -> {
                val p = (state as HomeUiState.Downloading).progress
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Downloading model… ${(p * 100).toInt()}%")
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = p, modifier = Modifier.width(200.dp))
                }
            }
            HomeUiState.Ready -> {
                // Should navigate automatically, but keep fallback button
                Button(onClick = navigateToChat) { Text("Open chat") }
            }
        }
    }
} 