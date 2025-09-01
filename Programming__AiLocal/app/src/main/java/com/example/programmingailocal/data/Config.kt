package com.example.programmingailocal.data

// Only the minimal subset needed by the helper

enum class ConfigKey(val label: String) {
    MAX_TOKENS("Max tokens"),
    TOPK("TopK"),
    TOPP("TopP"),
    TEMPERATURE("Temperature"),
    ACCELERATOR("Accelerator")
}

enum class ValueType { INT, FLOAT, STRING, BOOLEAN }

open class Config(
    val key: ConfigKey,
    val defaultValue: Any,
    val valueType: ValueType
) 