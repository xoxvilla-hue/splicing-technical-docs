package com.phanu.myapps.data

data class AppGroup(
    val id: String,
    val name: String,
    val packageNames: List<String> = emptyList()
)
