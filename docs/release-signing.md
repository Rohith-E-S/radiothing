# Release Signing

Google Play requires every upload to be signed with a keystore. This project
reads signing credentials from `keystore.properties` in the repo root, which
is **gitignored** — each developer/CI machine keeps its own copy.

## Layout

```
keystore.properties        # gitignored — credentials (chmod 600)
app/upload-keystore.jks    # gitignored (*.jks) — the upload key
```

`keystore.properties` format:

```properties
storeFile=app/upload-keystore.jks
storePassword=...
keyAlias=radiothing-upload
keyPassword=...
```

When `keystore.properties` exists, `assembleRelease` produces a signed APK.
Without it, the build still succeeds but the APK is unsigned
(`app-release-unsigned.apk`) — fine for CI verification, not for upload.

## Creating a keystore from scratch

```bash
keytool -genkeypair -v \
  -keystore app/upload-keystore.jks \
  -alias radiothing-upload \
  -keyalg RSA -keysize 2048 -validity 10000
```

## Play App Signing (recommended)

Enroll in Play App Signing in the Play Console and upload this keystore as
the **upload key**; Google holds the actual app signing key.

> ⚠️ Back up `upload-keystore.jks` and the passwords somewhere safe. If the
> upload key is lost and you are not enrolled in Play App Signing with a
> reset option, you cannot update the app.
