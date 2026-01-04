package passkeys.poc.app.space.k1t

import android.util.Base64
import java.security.SecureRandom

/**
 * Passkeyの登録・認証に必要なデータクラス
 */
data class PasskeyData(
    val userId: String,
    val userName: String,
    val displayName: String,
    val challenge: String
)

/**
 * ユーティリティ関数
 */
object PasskeyUtils {
    /**
     * ランダムなチャレンジを生成
     */
    fun generateChallenge(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * ランダムなユーザーIDを生成
     */
    fun generateUserId(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}

/**
 * Passkeyの登録結果
 */
data class RegistrationResult(
    val success: Boolean,
    val credentialId: String? = null,
    val error: String? = null
)

/**
 * Passkeyの認証結果
 */
data class AuthenticationResult(
    val success: Boolean,
    val credentialId: String? = null,
    val error: String? = null
)
