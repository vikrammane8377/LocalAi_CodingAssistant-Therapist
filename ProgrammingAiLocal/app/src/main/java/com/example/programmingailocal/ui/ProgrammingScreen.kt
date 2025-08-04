package com.example.programmingailocal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log

private data class Question(val prompt: String, val expectedOutput: String)

private val QUESTIONS = listOf(
    Question("Print the numbers from 1 to 5 inclusive, each on a separate line.",
        "1\n2\n3\n4\n5"),
    Question("Write a function factorial(n) and print factorial(5).",
        "120"),
    Question("Given the list nums = [3,1,4,1,5], print it sorted ascending.",
        "[1, 1, 3, 4, 5]"),
    Question("Read a string s and print s reversed. For this demo just reverse 'hello'.",
        "olleh"),
    Question("Compute the sum of even numbers between 1 and 10 inclusive and print it.",
        "30")
)

class ProgrammingViewModel : ViewModel() {
    private val TAG = "ProgVM"
    var index by mutableStateOf(0); private set
    var code by mutableStateOf(""); private set
    var result by mutableStateOf(""); private set
    var passed by mutableStateOf<Boolean?>(null); private set

    fun run() {
        val q = QUESTIONS[index]
        try {
            Log.d(TAG, "Running user code for question $index")
            val py = com.chaquo.python.Python.getInstance()
            val runner = py.getModule("runner")
            val ret = runner.callAttr("run_user_code", code, q.expectedOutput)
                .toJava(java.util.HashMap::class.java) as java.util.HashMap<*, *>
            val okAny = ret["ok"]
            passed = okAny is Boolean && okAny
            val errorStr = ret["error"] as? String ?: ""
            val outputStr = ret["output"] as? String ?: ""
            result = if (errorStr.isNotEmpty()) errorStr else outputStr
        } catch (e: Exception) {
            Log.e(TAG, "Python exec error", e)
            passed = false
            result = "Runtime error: ${e.message}"
        }
    }

    fun next() {
        if (index < QUESTIONS.lastIndex) {
            index++
            code = ""
            result = ""
            passed = null
        }
    }

    fun updateCode(newCode: String) { code = newCode }
}

@Composable
fun ProgrammingScreen(viewModel: ProgrammingViewModel = viewModel()) {
    val q = QUESTIONS[viewModel.index]

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Question ${viewModel.index + 1}/5", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(q.prompt)

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.code,
            onValueChange = viewModel::updateCode,
            label = { Text("Your Python code") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        )

        Spacer(Modifier.height(8.dp))

        viewModel.passed?.let { ok ->
            if (ok) {
                Text("✅ Correct!", color = MaterialTheme.colorScheme.primary)
                if (viewModel.result.isNotBlank()) {
                    Text("Output:\n${viewModel.result}")
                }
            } else {
                Text("❌ Not correct. ${viewModel.result}")
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = viewModel::run) { Text("Run") }
            Spacer(Modifier.width(8.dp))
            if (viewModel.passed == true && viewModel.index < QUESTIONS.lastIndex) {
                Button(onClick = viewModel::next) { Text("Next question") }
            }
        }
    }
} 