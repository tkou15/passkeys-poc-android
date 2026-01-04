package space.k1t.app.passkey.poc

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import space.k1t.app.passkey.poc.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var passkeyManager: PasskeyManager
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // PasskeyManagerの初期化
        passkeyManager = PasskeyManager(this)

        setupViews()
        addLog("アプリを起動しました")
    }

    private fun setupViews() {
        // 登録ボタン
        binding.registerButton.setOnClickListener {
            handleRegisterPasskey()
        }

        // 認証ボタン
        binding.authenticateButton.setOnClickListener {
            handleAuthenticatePasskey()
        }

        // ログクリアボタン
        binding.clearLogButton.setOnClickListener {
            binding.logTextView.text = ""
            addLog("ログをクリアしました")
        }
    }

    /**
     * Passkeyの登録処理
     */
    private fun handleRegisterPasskey() {
        val userName = binding.userNameEditText.text.toString().trim()
        val displayName = binding.displayNameEditText.text.toString().trim()

        // 入力チェック
        if (userName.isEmpty()) {
            binding.userNameInputLayout.error = "ユーザー名を入力してください"
            return
        }
        if (displayName.isEmpty()) {
            binding.displayNameInputLayout.error = "表示名を入力してください"
            return
        }

        binding.userNameInputLayout.error = null
        binding.displayNameInputLayout.error = null

        // ユーザーIDを生成
        val userId = PasskeyUtils.generateUserId()

        addLog("Passkeyの登録を開始します...")
        addLog("ユーザー名: $userName")
        addLog("表示名: $displayName")
        addLog("ユーザーID: $userId")

        // ボタンを無効化
        binding.registerButton.isEnabled = false

        lifecycleScope.launch {
            val result = passkeyManager.registerPasskey(
                userId = userId,
                userName = userName,
                displayName = displayName
            )

            // ボタンを有効化
            binding.registerButton.isEnabled = true

            if (result.success) {
                addLog("✓ Passkeyの登録に成功しました")
                addLog("Credential ID: ${result.credentialId}")
                
                showSuccessDialog(
                    title = "登録成功",
                    message = "Passkeyの登録が完了しました。\n\n認証ボタンから認証をテストできます。"
                )
            } else {
                addLog("✗ Passkeyの登録に失敗しました")
                addLog("エラー: ${result.error}")
                
                showErrorDialog(
                    title = "登録失敗",
                    message = result.error ?: "不明なエラーが発生しました"
                )
            }
        }
    }

    /**
     * Passkeyでの認証処理
     */
    private fun handleAuthenticatePasskey() {
        addLog("Passkeyでの認証を開始します...")

        // ボタンを無効化
        binding.authenticateButton.isEnabled = false

        lifecycleScope.launch {
            val result = passkeyManager.authenticateWithPasskey()

            // ボタンを有効化
            binding.authenticateButton.isEnabled = true

            if (result.success) {
                addLog("✓ 認証に成功しました")
                addLog("Credential ID: ${result.credentialId}")
                
                showSuccessDialog(
                    title = "認証成功",
                    message = "Passkeyによる認証が完了しました。"
                )
            } else {
                addLog("✗ 認証に失敗しました")
                addLog("エラー: ${result.error}")
                
                showErrorDialog(
                    title = "認証失敗",
                    message = result.error ?: "不明なエラーが発生しました"
                )
            }
        }
    }

    /**
     * ログを追加
     */
    private fun addLog(message: String) {
        val timestamp = dateFormat.format(Date())
        val logMessage = "[$timestamp] $message\n"
        
        runOnUiThread {
            val currentLog = binding.logTextView.text.toString()
            if (currentLog == "操作を実行するとログが表示されます") {
                binding.logTextView.text = logMessage
            } else {
                binding.logTextView.append(logMessage)
            }
            
            // ログエリアを最下部にスクロール
            binding.logTextView.post {
                val scrollView = binding.logTextView.parent as? android.widget.ScrollView
                scrollView?.fullScroll(android.view.View.FOCUS_DOWN)
            }
        }
    }

    /**
     * 成功ダイアログを表示
     */
    private fun showSuccessDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * エラーダイアログを表示
     */
    private fun showErrorDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}

