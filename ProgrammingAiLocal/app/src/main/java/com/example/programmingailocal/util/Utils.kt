package com.example.programmingailocal.util

fun cleanUpMediapipeTaskErrorMessage(message: String): String {
    val index = message.indexOf("=== Source Location Trace")
    return if (index >= 0) message.substring(0, index) else message
} 