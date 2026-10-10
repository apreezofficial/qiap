# Releasing Qiap

Everything here can be done from a phone browser plus the Play Console. Nothing secret lives in the repo.

## 1. Make an upload key (once)
On any computer with Java (or in a GitHub Codespace):
```bash
keytool -genkeypair -v -keystore upload.jks -alias qiap-upload -keyalg RSA -keysize 4096 -validity 10000
```
Keep `upload.jks` and its passwords somewhere safe (a password manager). If you lose it you can ask
Play to reset the upload key, but only if you enrolled in Play App Signing (the default).

## 2. Give the key to GitHub Actions (once)
Repo > Settings > Secrets and variables > Actions > New repository secret. Add four:

| Secret | Value |
|---|---|
| `QIAP_KEYSTORE_B64` | the file, base64 encoded: `base64 -w0 upload.jks` |
| `QIAP_KEYSTORE_PASSWORD` | the keystore password |
| `QIAP_KEY_ALIAS` | `qiap-upload` |
| `QIAP_KEY_PASSWORD` | the key password |

Without these the CI still builds an AAB, but it is debug-signed and the job summary says so.

## 3. Build
Push to `main` or any `phase-*` branch (or run the Android workflow by hand). Download the
`qiap-release-aab` artifact. Its version code is the workflow run number, so each build is higher
than the last.

## 4. Play Console
Use `docs/play-listing.md` for every declaration and answer. Upload the AAB to Internal testing first,
install it from the Play link on a real phone, run `docs/device-checklist.md`, then promote.

## Before the first production release
- [ ] Device checklist passed on at least two phone brands, with the numbers written down.
- [ ] Privacy policy live at `https://qiap.app/privacy` (the web workflow builds it; deploy `web/out`).
- [ ] `hello@qiap.app` is a real inbox (it is in the policy and the footer).
- [ ] Alarm reliability: 20 of 20 on the lock screen, after a reboot, and after swiping the app away
      (details.md section 17).
