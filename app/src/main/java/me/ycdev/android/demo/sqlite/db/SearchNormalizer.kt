package me.ycdev.android.demo.sqlite.db

import java.lang.StringBuilder

object SearchNormalizer {
    fun normalize(query: String): String {
        val builder = StringBuilder(query.length + 10)
        var termFound = false
        for (ch in query) {
            if (isValidTermCharacter(ch)) {
                termFound = true
                builder.append(ch)
            } else if (termFound) {
                builder.append("* ")
                termFound = false
            }
        }
        if (termFound) {
            builder.append("* ")
        }
        return builder.toString()
    }

    private fun isValidTermCharacter(ch: Char): Boolean {
        if (ch.toInt() in 0..127) {
            return ch == '_' || ch == '-' || ch in '0'..'9' || ch in 'a'..'z' || ch in 'A'..'Z'
        }
        return true
    }
}
