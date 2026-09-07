# Publishing to F-Droid

This repo is prepared for F-Droid packaging. What lives where, and the exact
steps to submit.

## What's already in place

| Piece | Location |
|---|---|
| License (GPL-3.0) | `LICENSE` |
| Font license (Doto, SIL OFL 1.1) | `licenses/Doto-OFL.txt` + credit in `README.md` |
| Store listing text | `fastlane/metadata/android/en-US/` (title, summaries, changelog `1.txt`) |
| Store icon (512×512) | `fastlane/metadata/android/en-US/images/icon.png` |
| F-Droid build recipe | `fdroid/com.radiothing.app.yml` (copy into fdroiddata on submission) |
| Screenshots | **missing — take before submitting** (see below) |

F-Droid never receives an APK from you. Their servers clone the repo, build the
tagged commit, and sign with their own key. Requirements met by this repo:

- All dependencies from `google()`/`mavenCentral()` only, all FOSS.
- Release build produces an **unsigned** APK when `keystore.properties` is
  absent (exactly how F-Droid's builders see the repo).
- No proprietary assets: the original Ndot57 typeface was replaced with
  [Doto](https://fonts.google.com/specimen/Doto) (SIL OFL 1.1).

## Submission checklist

1. **Take screenshots** — 2–5 phone screenshots into
   `fastlane/metadata/android/en-US/images/phoneScreenshots/`.
2. **Tag the release.** The build file says `versionCode 1` / `versionName 1.0.0`,
   so: commit everything, `git tag v1.0.0 && git push origin main --tags`.
   Every future release: bump `versionCode`, add
   `fastlane/.../changelogs/<versionCode>.txt`, tag `v<versionName>`.
3. **Check for an existing request** at https://gitlab.com/fdroid/rfp/-/issues
   (search `radiothing` or `com.radiothing.app`). If none, open an RFP issue
   using the `rfp` template: repo URL, summary, license, one-line description.
4. **Submit the build recipe.** Fork https://gitlab.com/fdroid/fdroiddata,
   copy `fdroid/com.radiothing.app.yml` to `metadata/com.radiothing.app.yml`
   (replacing the header comment with fdroiddata's standard warning header if
   the tooling asks), and open a merge request.
5. **Verify the build locally** (optional but catches most review round-trips):

   ```bash
   pipx install fdroidserver
   fdroid build --verbose --latest com.radiothing.app
   ```

6. **Wait for review.** Volunteers check licensing (code + assets), build
   reproducibility, and metadata. Expect a few weeks for a new app.

After the first merge, the `AutoUpdateMode: Version v%v` line makes F-Droid's
bot open update MRs automatically whenever a new `v*` tag is pushed.

## Notes

- The F-Droid build is signed with a **different key** than the Play/release
  build, so the two installs cannot update each other. That's normal.
- IzzyOnDroid (https://izzyondroid/releases) is a faster alternative repo with
  the same FOSS requirements — it picks up APKs attached to GitHub releases.
- The donation link in the yml is a placeholder; remove it if you don't want one.
