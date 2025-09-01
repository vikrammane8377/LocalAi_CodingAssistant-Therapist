package com.example.programmingailocal.data

import android.content.Context
import java.io.File

private val NORMALIZE_NAME_REGEX = Regex("[^a-zA-Z0-9]")

data class Model(
    val name: String,
    val version: String = "_",
    val downloadFileName: String,
    val url: String,
    val sizeInBytes: Long,
    val configs: List<Config> = emptyList(),
    val isZip: Boolean = false,
    val unzipDir: String = "",
    // runtime fields
    var normalizedName: String = "",
    var instance: Any? = null,
) {
    init {
        normalizedName = NORMALIZE_NAME_REGEX.replace(name, "_")
    }

    fun getPath(context: Context, fileName: String = downloadFileName): String {
        val baseDir = listOf(context.getExternalFilesDir(null)?.absolutePath ?: "", normalizedName, version)
            .joinToString(File.separator)
        return if (isZip && unzipDir.isNotEmpty()) {
            "$baseDir/$unzipDir"
        } else {
            "$baseDir/$fileName"
        }
    }

    // Stub helpers – we just return defaults for now
    fun getIntConfigValue(key: ConfigKey, defaultValue: Int = 0): Int = defaultValue
    fun getFloatConfigValue(key: ConfigKey, defaultValue: Float = 0f): Float = defaultValue
    fun getStringConfigValue(key: ConfigKey, defaultValue: String = ""): String = defaultValue
} 