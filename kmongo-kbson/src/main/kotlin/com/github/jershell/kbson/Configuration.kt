package com.github.jershell.kbson

data class Configuration(
    val encodeDefaults: Boolean = true,
    val classDiscriminator: String = "___type",
    val nonEncodeNull: Boolean = false
)