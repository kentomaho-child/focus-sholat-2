package com.focussholat.util

import java.security.MessageDigest

object SecurityUtil {

    fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, storedHash: String): Boolean {
        return hashPin(pin) == storedHash
    }
}
