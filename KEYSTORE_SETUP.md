# Release signing setup

Debug builds need nothing. To produce a **signed release APK** (for distribution),
set up a keystore once.

## 1. Create a keystore (local, one time)
```bash
keytool -genkeypair -v \
  -keystore release.keystore \
  -alias beatdrop \
  -keyalg RSA -keysize 2048 -validity 10000
```
Remember the **store password**, **key alias** (`beatdrop`), and **key password**.

> ⚠️ Never commit `release.keystore` or `keystore.properties`. Both are gitignored.

## 2a. Build signed releases locally
Create `keystore.properties` in the project root:
```properties
storeFile=release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=beatdrop
keyPassword=YOUR_KEY_PASSWORD
```
Then:
```bash
./gradlew assembleRelease
# → app/build/outputs/apk/release/app-release.apk
```

## 2b. Build signed releases in GitHub Actions
Encode the keystore and add repo secrets:
```bash
base64 -w0 release.keystore   # macOS: base64 -i release.keystore
```
In your repo: **Settings → Secrets and variables → Actions → New repository secret**, add:

| Secret | Value |
|--------|-------|
| `KEYSTORE_BASE64` | output of the base64 command above |
| `KEYSTORE_PASSWORD` | your store password |
| `KEY_ALIAS` | `beatdrop` |
| `KEY_PASSWORD` | your key password |

On the next push, the workflow auto-detects the secrets, builds, and uploads
**`BeatDrop-release-apk`**. Without the secrets it still builds the debug APK.

## How the Gradle config resolves signing
`app/build.gradle.kts` looks for, in order:
1. `KEYSTORE_FILE` env var (CI), else
2. `keystore.properties` `storeFile` (local).
If neither exists, the release build falls back to debug signing so it never
fails for contributors without the keystore.
