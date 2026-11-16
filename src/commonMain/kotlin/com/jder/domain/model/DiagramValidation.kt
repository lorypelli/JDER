package com.jder.domain.model

class DiagramValidationException(errors: List<String>) :
    IllegalArgumentException(errors.joinToString("; "))
