package com.jder.domain.model

import kotlin.uuid.Uuid

fun newId(): String = Uuid.random().toString()
