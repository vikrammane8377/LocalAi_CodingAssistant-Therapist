package com.example.programmingailocal.model

import com.example.programmingailocal.data.Config
import com.example.programmingailocal.data.Model

// NOTE: Update url & sizeInBytes if you have the precise numbers. The placeholder URL points to
// the Litert-community checkpoint on Hugging Face.

const val QWEN_MODEL_FILE = "Qwen2.5-0.5B-Instruct_multi-prefill-seq_f32_ekv1280.task"

val QWEN_MODEL = Model(
    name = "Qwen2.5-0.5B-Instruct f32",
    downloadFileName = QWEN_MODEL_FILE,
    url = "https://huggingface.co/litert-community/Qwen2.5-0.5B-Instruct/resolve/main/$QWEN_MODEL_FILE",
    sizeInBytes = 1_200_000_000L, // ≈ 1.2 GB – adjust if you know exact size
    configs = emptyList(),
) 