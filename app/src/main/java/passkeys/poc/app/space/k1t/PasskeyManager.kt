package passkeys.poc.app.space.k1t

import android.content.Context
import android.util.Log
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Passkey（パスキー）の登録と認証を管理するクラス
 */
class PasskeyManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private val gson = Gson()

    companion object {
        private const val TAG = "PasskeyManager"
        private const val RP_ID = "complete-passkeys.vercel.app" // ドメイン名のみ（https://やパスは含めない）
        private const val RP_NAME = "Passkeys Test App"
    }

    /**
     * Passkeyを登録する
     */
    suspend fun registerPasskey(
        userId: String,
        userName: String,
        displayName: String
    ): RegistrationResult = withContext(Dispatchers.Main) {
        try {
            val challenge = PasskeyUtils.generateChallenge()
            
            // 登録リクエストのJSONを作成
            val requestJson = createRegistrationRequestJson(
                challenge = challenge,
                userId = userId,
                userName = userName,
                displayName = displayName
            )

            Log.d(TAG, "Registration request: $requestJson")

            val request = CreatePublicKeyCredentialRequest(
                requestJson = requestJson,
                preferImmediatelyAvailableCredentials = false
            )

            val result = credentialManager.createCredential(
                context = context,
                request = request
            )

            when (result) {
                is CreatePublicKeyCredentialResponse -> {
                    val responseJson = result.registrationResponseJson
                    Log.d(TAG, "Registration successful: $responseJson")
                    
                    // レスポンスからcredentialIdを抽出
                    val credentialId = extractCredentialId(responseJson)
                    
                    RegistrationResult(
                        success = true,
                        credentialId = credentialId
                    )
                }
                else -> {
                    RegistrationResult(
                        success = false,
                        error = "Unknown response type"
                    )
                }
            }
        } catch (e: CreateCredentialCancellationException) {
            Log.e(TAG, "Registration cancelled", e)
            RegistrationResult(
                success = false,
                error = "登録がキャンセルされました"
            )
        } catch (e: CreateCredentialException) {
            Log.e(TAG, "Registration failed", e)
            RegistrationResult(
                success = false,
                error = "登録に失敗しました: ${e.message}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during registration", e)
            RegistrationResult(
                success = false,
                error = "予期しないエラー: ${e.message}"
            )
        }
    }

    /**
     * Passkeyで認証する
     */
    suspend fun authenticateWithPasskey(): AuthenticationResult = withContext(Dispatchers.Main) {
        try {
            val challenge = PasskeyUtils.generateChallenge()
            
            // 認証リクエストのJSONを作成
            val requestJson = createAuthenticationRequestJson(challenge)

            Log.d(TAG, "Authentication request: $requestJson")

            val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(
                requestJson = requestJson
            )

            val getCredRequest = GetCredentialRequest(
                listOf(getPublicKeyCredentialOption)
            )

            val result = credentialManager.getCredential(
                context = context,
                request = getCredRequest
            )

            val credential = result.credential

            when (credential) {
                is PublicKeyCredential -> {
                    val responseJson = credential.authenticationResponseJson
                    Log.d(TAG, "Authentication successful: $responseJson")
                    
                    // レスポンスからcredentialIdを抽出
                    val credentialId = extractAuthCredentialId(responseJson)
                    
                    AuthenticationResult(
                        success = true,
                        credentialId = credentialId
                    )
                }
                else -> {
                    AuthenticationResult(
                        success = false,
                        error = "Unknown credential type"
                    )
                }
            }
        } catch (e: GetCredentialCancellationException) {
            Log.e(TAG, "Authentication cancelled", e)
            AuthenticationResult(
                success = false,
                error = "認証がキャンセルされました"
            )
        } catch (e: NoCredentialException) {
            Log.e(TAG, "No credential found", e)
            AuthenticationResult(
                success = false,
                error = "保存された認証情報が見つかりません"
            )
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Authentication failed", e)
            AuthenticationResult(
                success = false,
                error = "認証に失敗しました: ${e.message}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during authentication", e)
            AuthenticationResult(
                success = false,
                error = "予期しないエラー: ${e.message}"
            )
        }
    }

    /**
     * 登録リクエストのJSONを作成
     */
    private fun createRegistrationRequestJson(
        challenge: String,
        userId: String,
        userName: String,
        displayName: String
    ): String {
        val requestMap = mapOf(
            "challenge" to challenge,
            "rp" to mapOf(
                "name" to RP_NAME,
                "id" to RP_ID
            ),
            "user" to mapOf(
                "id" to userId,
                "name" to userName,
                "displayName" to displayName
            ),
            "pubKeyCredParams" to listOf(
                mapOf(
                    "type" to "public-key",
                    "alg" to -7  // ES256
                ),
                mapOf(
                    "type" to "public-key",
                    "alg" to -257  // RS256
                )
            ),
            "timeout" to 60000,
            "attestation" to "none",
            "authenticatorSelection" to mapOf(
                "residentKey" to "preferred",
                "userVerification" to "preferred"
            )
        )
        return gson.toJson(requestMap)
    }

    /**
     * 認証リクエストのJSONを作成
     */
    private fun createAuthenticationRequestJson(challenge: String): String {
        val requestMap = mapOf(
            "challenge" to challenge,
            "rpId" to RP_ID,
            "timeout" to 60000,
            "userVerification" to "required"
        )
        return gson.toJson(requestMap)
    }

    /**
     * 登録レスポンスからcredentialIdを抽出
     */
    private fun extractCredentialId(responseJson: String): String? {
        return try {
            val response = gson.fromJson(responseJson, Map::class.java)
            response["id"] as? String
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract credential ID", e)
            null
        }
    }

    /**
     * 認証レスポンスからcredentialIdを抽出
     */
    private fun extractAuthCredentialId(responseJson: String): String? {
        return try {
            val response = gson.fromJson(responseJson, Map::class.java)
            response["id"] as? String
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract credential ID", e)
            null
        }
    }
}
