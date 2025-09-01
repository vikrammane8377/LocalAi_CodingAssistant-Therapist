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
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

private data class Question(
    val prompt: String,
    val expectedOutput: String,
    val testInput: String = ""
)

private val QUESTIONS = listOf(
    Question("Print the numbers from 1 to 5 inclusive, each on a separate line.",
        "1\n2\n3\n4\n5"),
    Question("Create two threads; each thread prints 'worker' five times, then main thread prints 'Done'. Ensure 'Done' appears last.",
        "Done"),
    Question("Write a program that reads two integers, adds them and prints the result. (For testing, inputs 1 and 3 should produce 4).",
        "4",
        "1\n3\n"),
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
    var offerHelp by mutableStateOf(false); private set
    var generating by mutableStateOf(false); private set
    var hintAvailable by mutableStateOf(true); private set
    var explainAvailable by mutableStateOf(false); private set
    var explaining by mutableStateOf(false); private set
    var showInputDialog by mutableStateOf(false); private set
    var stdinDraft by mutableStateOf("")

    private fun execute(stdin: String) {
        val q = QUESTIONS[index]
        try {
            val py = com.chaquo.python.Python.getInstance()
            val runner = py.getModule("runner")
            val ret = runner.callAttr("run_user_code", code, q.expectedOutput, stdin)
                .toJava(java.util.HashMap::class.java) as java.util.HashMap<*, *>
            val okAny = ret["ok"]
            passed = okAny is Boolean && okAny
            val errorStr = ret["error"] as? String ?: ""
            val outputStr = ret["output"] as? String ?: ""
            result = if (errorStr.isNotEmpty()) errorStr else outputStr
            offerHelp = (passed == false)
            explainAvailable = errorStr.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Python exec error", e)
            passed = false; result = "Runtime error: ${e.message}"; offerHelp = true
            explainAvailable = true
        }
    }

    fun run() {
        val q = QUESTIONS[index]
        val needsInput = code.contains("input(")
        if (needsInput && !showInputDialog) {
            // ask user
            stdinDraft = q.testInput
            showInputDialog = true
            return
        }
        execute(if (needsInput) stdinDraft else q.testInput)
        showInputDialog = false
    }

    fun requestAiFix(context: android.content.Context) {
        generating = true
        offerHelp = false
        result = "Generating fix..."

        val sb = StringBuilder()
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
            val model = com.example.programmingailocal.model.QWEN_MODEL
            com.example.programmingailocal.llm.LlmChatModelHelper.resetSession(model)
            // ensure model initialized (reuse helper)
            if (model.instance == null) {
                com.example.programmingailocal.llm.LlmChatModelHelper.initialize(context, model) {}
                while (model.instance == null) { kotlinx.coroutines.delay(100) }
            }
            val questionText = QUESTIONS[index].prompt
            val extra = if (questionText.contains("thread", ignoreCase = true)) {
                "\nRequirement: you MUST use Python's threading module. Create two distinct Thread objects, start them, join them before printing 'Done'."
            } else ""
            val errorText = result
            val prompt = """Fix the following Python task. Task: $questionText$extra\nCurrent code:\n$code\nError or wrong output:\n$errorText\n\nReturn ONLY the corrected solution as runnable Python, formatted with real line breaks and 4-space indentation. Do NOT include \n escape sequences, backticks, or any text besides the code itself. Avoid compressing multiple statements on one line."""
            com.example.programmingailocal.llm.LlmChatModelHelper.runInference(model, prompt) { partial, done ->
                if (partial.isNotEmpty()) sb.append(partial)
                if (done) {
                    val newCode = sb.toString().trim('\n')
                    val cleaned = cleanGeneratedCode(newCode)
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        if (cleaned.isBlank()) {
                            generating = false
                            result = "⚠️ AI did not return code. Please try again."
                            offerHelp = true
                        } else {
                            code = cleaned
                            generating = false
                            result = "AI code inserted. Tap Run to test."
                        }
                    }
                }
            }
        }
    }

    fun requestHint(context: android.content.Context) {
        if (!hintAvailable) return
        generating = true
        hintAvailable = false
        result = "Generating hint..."

        val sb = StringBuilder()
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
            val model = com.example.programmingailocal.model.QWEN_MODEL
            com.example.programmingailocal.llm.LlmChatModelHelper.resetSession(model)
            if (model.instance == null) {
                com.example.programmingailocal.llm.LlmChatModelHelper.initialize(context, model) {}
                while (model.instance == null) { kotlinx.coroutines.delay(100) }
            }
            val questionText = QUESTIONS[index].prompt
            val prompt = """Provide a concise step-by-step algorithm to solve the following Python task. Return the algorithm as Python comments, each step on its own line starting with "# ". Task: $questionText"""

            com.example.programmingailocal.llm.LlmChatModelHelper.runInference(model, prompt) { partial, done ->
                if (partial.isNotEmpty()) sb.append(partial)
                if (done) {
                    val algorithm = cleanGeneratedCode(sb.toString())
                    val newContent = """$algorithm

$code"""
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        if (algorithm.isBlank()) {
                            result = "⚠️ Hint generation failed. Please try again."
                        } else {
                            code = newContent
                            result = "Hint inserted at top of editor."
                        }
                        generating = false
                    }
                }
            }
        }
    }

    fun requestExplain(context: android.content.Context) {
        if (!explainAvailable || explaining) return
        explaining = true; explainAvailable = false; result = "Explaining error..."

        val sb = StringBuilder()
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
            val model = com.example.programmingailocal.model.QWEN_MODEL
            com.example.programmingailocal.llm.LlmChatModelHelper.resetSession(model)
            if (model.instance == null) {
                com.example.programmingailocal.llm.LlmChatModelHelper.initialize(context, model) {}
                while (model.instance == null) { kotlinx.coroutines.delay(100) }
            }
            val prompt = """You are a helpful tutor. Explain the following Python error message in simple terms and point out where the issue is in the code. Suggest how to fix it concisely.\n\nCode:\n$code\n\nError:\n$result"""

            com.example.programmingailocal.llm.LlmChatModelHelper.runInference(model, prompt) { partial, done ->
                if (partial.isNotEmpty()) sb.append(partial)
                if (done) {
                    val explanation = sb.toString().trim('\n')
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        result = explanation
                        explaining = false
                    }
                }
            }
        }
    }

    fun dismissHelp() { offerHelp = false }

    fun next() {
        if (index < QUESTIONS.lastIndex) {
            index++
            code = ""
            result = ""
            passed = null
            hintAvailable = true
        }
    }

    fun updateCode(newCode: String) { code = newCode }
    fun updateStdin(newVal: String) { stdinDraft = newVal }
    fun dismissInput() { showInputDialog = false }

    private fun cleanGeneratedCode(raw: String): String {
        var txt = raw.trim()

        // remove any backtick code fences regardless of position
        txt = txt.replace("```python", "", ignoreCase = true)
            .replace("```", "")

        // Convert escaped newlines to real newlines
        txt = txt.replace("\\r\\n", "\n").replace("\\n", "\n")

        // Handle stray backslash followed by real newline
        txt = txt.replace("\\\n", "\n")
        txt = txt.trim()
        return txt
    }
}

@Composable
fun ProgrammingScreen(viewModel: ProgrammingViewModel = viewModel()) {
    val q = QUESTIONS[viewModel.index]

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Question ${viewModel.index + 1}/${QUESTIONS.size}", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(q.prompt)

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.code,
            onValueChange = viewModel::updateCode,
            label = { Text("Your Python code") },
            textStyle = androidx.compose.ui.text.TextStyle(color = androidx.compose.ui.graphics.Color.Black),
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
            Button(onClick = viewModel::run, enabled = !viewModel.generating && !viewModel.explaining) { Text("Run") }
            Spacer(Modifier.width(8.dp))
            if (viewModel.passed == true && viewModel.index < QUESTIONS.lastIndex) {
                Button(onClick = viewModel::next) { Text("Next question") }
            }
        }

        if (viewModel.offerHelp && !viewModel.generating) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { viewModel.dismissHelp() }) { Text("Code myself") }
                Spacer(Modifier.width(8.dp))
                val ctx = androidx.compose.ui.platform.LocalContext.current
                TextButton(onClick = { viewModel.requestAiFix(ctx) }) { Text("Please help me") }
            }
        }

        if (viewModel.generating) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        if (viewModel.explainAvailable && !viewModel.explaining) {
            Spacer(Modifier.height(8.dp))
            val ctx = androidx.compose.ui.platform.LocalContext.current
            TextButton(onClick = { viewModel.requestExplain(ctx) }) { Text("Explain error") }
        }

        if (viewModel.explaining) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        // Hint button
        if (viewModel.hintAvailable && !viewModel.generating) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                val ctx = androidx.compose.ui.platform.LocalContext.current
                TextButton(onClick = { viewModel.requestHint(ctx) }) {
                    Text("Hint")
                }
            }
        }

        // stdin dialog
        if (viewModel.showInputDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissInput() },
                confirmButton = {
                    TextButton(onClick = { viewModel.run() }) { Text("Run") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissInput() }) { Text("Cancel") }
                },
                title = { Text("Provide stdin lines (each on new line)") },
                text = {
                    OutlinedTextField(
                        value = viewModel.stdinDraft,
                        onValueChange = viewModel::updateStdin,
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }
            )
        }
    }
} 