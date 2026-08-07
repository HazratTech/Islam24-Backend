package com.islam24.api.security

import java.security.MessageDigest


fun hashToken(token: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(token.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}