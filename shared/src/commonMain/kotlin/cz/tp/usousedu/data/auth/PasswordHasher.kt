package cz.tp.usousedu.data.auth

import org.kotlincrypto.hash.sha2.SHA256

// Only used by the local mock implementation; Firebase hashes passwords on the server.
// Plain SHA-256 is enough for a mock, a real backend needs a salted, slow hash.
internal object PasswordHasher {
    @OptIn(ExperimentalStdlibApi::class)
    fun hash(password: String): String = SHA256().digest(password.encodeToByteArray()).toHexString()
}
