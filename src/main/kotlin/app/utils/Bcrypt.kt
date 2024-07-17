package app.utils

import org.mindrot.jbcrypt.BCrypt

fun hash(string: String): String {
    return BCrypt.hashpw(string, BCrypt.gensalt())
}

fun compare(string: String, hashed: String): Boolean {
    return BCrypt.checkpw(string, hashed)
}