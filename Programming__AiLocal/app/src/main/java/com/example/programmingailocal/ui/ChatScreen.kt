package com.example.programmingailocal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.programmingailocal.data.Model
import com.example.programmingailocal.llm.LlmChatModelHelper
import com.example.programmingailocal.model.QWEN_MODEL
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.material3.TextFieldDefaults

class ChatViewModel(private val context: Context, private val model: Model) : ViewModel() {
    private val _messages = mutableStateListOf<Pair<Boolean, String>>() // true = user
    val messages: List<Pair<Boolean, String>> get() = _messages

    private var inProgress = false

    fun send(text: String) {
        val TAG = "ChatVM"
        if (inProgress) return            // ignore if a generation is running
        inProgress = true

        _messages.add(true to text)
        _messages.add(false to "…") // placeholder for the assistant
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            if (model.instance == null) {
                Log.d(TAG, "Initializing model…")
                var initError: String? = null
                LlmChatModelHelper.initialize(context, model) { err -> initError = err }
                // Wait (simple loop) until either instance created or error set
                while (model.instance == null && initError == null) {
                    kotlinx.coroutines.delay(50)
                }
                initError?.let {
                    _messages[_messages.lastIndex] = false to "[init failed: $it]"
                    inProgress = false
                    return@launch
                }
            }

            LlmChatModelHelper.runInference(model, text) { partial, done ->
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    if (partial.isNotEmpty()) {
                        val existing = _messages[_messages.lastIndex].second
                        _messages[_messages.lastIndex] = false to (existing + partial)
                    }
                    if (done) {
                        inProgress = false
                        Log.d(TAG, "Response done")
                    }
                }
            }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ChatViewModel(context, QWEN_MODEL) as T
                }
            }
    }
}

@Composable
fun ChatScreen() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val vm: ChatViewModel = viewModel(factory = ChatViewModel.factory(ctx))
    var input by remember { mutableStateOf(TextFieldValue("")) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)) {

        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFEFEFEF))
        ) {
            items(vm.messages) { (isUser, text) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    MessageBubble(text = text, isUser = isUser)
                }
            }
        }

        // auto-scroll to bottom when list grows
        LaunchedEffect(vm.messages.size) {
            if (vm.messages.isNotEmpty()) {
                listState.animateScrollToItem(vm.messages.lastIndex)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f), textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black), colors = TextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                val t = input.text
                if (t.isNotBlank()) {
                    vm.send(t)
                    input = TextFieldValue("")
                }
            }) { Text("Send") }
        }
    }
}

@Composable
private fun MessageBubble(text: String, isUser: Boolean) {
    val bg = if (isUser) Color(0xFFCCE5FF) else Color(0xFFE0E0E0)
    val txtColor = Color.Black
    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.widthIn(max = 280.dp)
    ) {
        Text(text, color = txtColor, modifier = Modifier.padding(8.dp))
    }
} 