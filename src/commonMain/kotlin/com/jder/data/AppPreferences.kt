package com.jder.data

expect fun readPreference(key: String): String?

expect fun savePreference(key: String, value: String, onFailure: (Throwable) -> Unit)
