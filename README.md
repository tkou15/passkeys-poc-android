# Passkeys検証アプリ

このアプリは、AndroidのPasskeys（パスキー）機能をテストするための検証用アプリです。

## 主な機能

1. **Passkeyの登録**
   - ユーザー名と表示名を入力してPasskeyを作成
   - デバイスの生体認証を使用して登録

2. **Passkeyでの認証**
   - 登録済みのPasskeyを使用して認証
   - 生体認証またはデバイスのロック解除方法で認証

3. **リアルタイムログ表示**
   - 登録・認証の処理状況をリアルタイムで確認

## 技術仕様

### 使用ライブラリ

- **androidx.credentials** - Credential Manager API
- **androidx.credentials:credentials-play-services-auth** - Google Play Services連携
- **kotlinx-coroutines** - 非同期処理
- **Gson** - JSON処理

### 対応Android バージョン

- **minSdk**: 28 (Android 9.0 Pie)
- **targetSdk**: 36

## セットアップ手順

1. Android Studio でプロジェクトを開く
2. Gradle Sync を実行
3. 実機またはエミュレータでビルド・実行

### 重要な注意事項

#### ドメイン設定

本番環境で使用する場合は、[`PasskeyManager.kt`](app/src/main/java/space/k1t/app/passkey/poc/PasskeyManager.kt:30) の以下の定数を実際のドメインに変更してください：

```kotlin
private const val RP_ID = "passkeys-test.example.com"
private const val RP_NAME = "Passkeys Test App"
```

#### Digital Asset Links の設定

Passkeysを使用するには、ドメインとアプリを紐付けるためにDigital Asset Linksの設定が必要です。

このプロジェクトには [`.well-known/assetlinks.json`](.well-known/assetlinks.json) が含まれています：

```json
[
  {
    "relation": [
      "delegate_permission/common.handle_all_urls",
      "delegate_permission/common.get_login_creds"
    ],
    "target": {
      "namespace": "android_app",
      "package_name": "space.k1t.app.passkey.poc",
      "sha256_cert_fingerprints": [
        "8C:2A:23:87:D3:86:58:60:7D:82:E4:86:33:59:F0:D2:3F:F0:2D:79:45:AC:93:88:2E:79:AC:9A:C6:01:98:AB"
      ]
    }
  }
]
```

**設定手順：**

1. ウェブサーバーに `.well-known/assetlinks.json` を配置
   - URL例: `https://your-domain.com/.well-known/assetlinks.json`
   - Content-Type: `application/json`
   
2. [`PasskeyManager.kt`](app/src/main/java/space/k1t/app/passkey/poc/PasskeyManager.kt:30) の `RP_ID` を実際のドメインに変更

3. 本番用の署名証明書を使う場合は、そのSHA-256フィンガープリントに更新
   ```bash
   keytool -list -v -keystore your-keystore.jks -alias your-alias
   ```

**デバッグ用フィンガープリント（現在設定済み）：**
```
8C:2A:23:87:D3:86:58:60:7D:82:E4:86:33:59:F0:D2:3F:F0:2D:79:45:AC:93:88:2E:79:AC:9A:C6:01:98:AB
```

## ファイル構成

### メインファイル

- [`MainActivity.kt`](app/src/main/java/space/k1t/app/passkey/poc/MainActivity.kt) - メインアクティビティ（UI制御）
- [`PasskeyManager.kt`](app/src/main/java/space/k1t/app/passkey/poc/PasskeyManager.kt) - Passkey登録・認証ロジック
- [`PasskeyData.kt`](app/src/main/java/space/k1t/app/passkey/poc/PasskeyData.kt) - データクラスとユーティリティ
- [`activity_main.xml`](app/src/main/res/layout/activity_main.xml) - UIレイアウト

### Gradle設定

- [`app/build.gradle.kts`](app/build.gradle.kts) - アプリモジュールのビルド設定
- [`gradle/libs.versions.toml`](gradle/libs.versions.toml) - バージョン管理

## 使い方

1. **登録**
   - ユーザー名（例：`user@example.com`）を入力
   - 表示名（例：`山田太郎`）を入力
   - 「Passkeyを登録」ボタンをタップ
   - 生体認証を実行して登録完了

2. **認証**
   - 「Passkeyで認証」ボタンをタップ
   - 登録済みのPasskeyを選択
   - 生体認証を実行して認証完了

3. **ログ確認**
   - 画面下部のログエリアで処理の詳細を確認
   - 「ログをクリア」ボタンでログをリセット

## トラブルシューティング

### 登録・認証が失敗する場合

1. **デバイスの設定を確認**
   - デバイスのロック画面が設定されているか
   - 生体認証（指紋・顔認証）が登録されているか

2. **エミュレータを使用する場合**
   - API Level 28以上のシステムイメージを使用
   - Google Play Servicesが含まれているイメージを選択
   - エミュレータの設定で生体認証を有効化

3. **ログを確認**
   - アプリ内のログエリアでエラーメッセージを確認
   - Logcatで詳細なエラー情報を確認（TAG: `PasskeyManager`）

## 参考リンク

- [Android Credential Manager API](https://developer.android.com/training/sign-in/passkeys)
- [WebAuthn Specification](https://www.w3.org/TR/webauthn-2/)
- [FIDO Alliance](https://fidoalliance.org/)
