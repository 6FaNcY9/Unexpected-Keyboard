# Grey Console Keyboard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A fork of Unexpected Keyboard 2.2.1 with a "Grey Console" theme and 0xProto labels, installed beside the F-Droid build on pong with the owner's settings carried over.

**Architecture:** Fork `Julow/Unexpected-Keyboard` to `6FaNcY9/Unexpected-Keyboard`, branch `grey-console` from tag `2.2.1`. Keep the patch small so upstream merges stay easy: one new style, one theme-list entry, one font loader, plus the build identity. A separate workflow, `pong-release.yml`, builds a release APK signed with a dedicated key; upstream's `make-apk.yml` stays untouched. Then install by hand and migrate prefs as root.

**Tech Stack:** Android (Java, Gradle 8.14.4, JDK 17, AGP from upstream), JUnit 4 (upstream `test/`), GitHub Actions, root shell on pong (toybox, `am`, `stat`, `chcon`).

**Spec:** `docs/superpowers/specs/2026-11-18-grey-console-keyboard-design.md`. Until the fork exists it is at `/home/vino/docs/superpowers/specs/`; Task 1 moves the spec and this plan into the fork.

## Global Constraints

- The base is upstream tag `2.2.1`. The phone runs F-Droid `juloo.keyboard2` 2.1.0, which must stay installed and unchanged.
- applicationId is `juloo.keyboard2.pong`; the release app name is `Unexpected Keyboard pong`; versionNameSuffix is `-pong`.
- Typing behaviour, gestures, layouts, suggestions and permissions stay as upstream has them. Do not touch `make-apk.yml` or the manifest permissions.
- Palette values (from bandit-nix `themes/material-sand-dark.yaml`): base00 `#0d0d0e`, base01 `#18181a`, base02 `#303034`, base05 `#d0d0d0`, base0A `#fdb827`, base0C `#00a99d`, base0E `#bc61bc`.
- The signing key is new and only for this app. Never reuse `~/.android-pong.jks`. Never print passwords or the keystore; they go only into GitHub secrets and Bitwarden.
- Nothing is compiled on the phone; builds run in GitHub Actions only.
- On pong, `pm`, `ime`, `input` and `settings` fail with FAILED_TRANSACTION. Use `am`, `kill`, `dumpsys` and file ops. The owner taps Install and enables the keyboard.
- Phone commands handed to the owner must fit on one line of about 45 characters or less.

## Review Focus

1. **Prefs in device-protected storage.** The IME reads `/data/user_de/0/<pkg>/shared_prefs/<pkg>_preferences.xml`, not only `/data/data`. If only `/data/data` is copied, the keyboard starts with defaults. (Task 4, Step 4 check.)
2. **Owner and SELinux context of copied files.** A wrong uid or a context without the app's MLS categories makes the app silently ignore or reset prefs and dicts. Copied files must match the context of their parent dir. (Task 4, Step 4 check.)
3. **Glyph coverage.** The corner keys include ä ö ü ß € § ° ´ ` ^ ~ | \. 0xProto has all of them; the Unicode arrows are missing but are drawn by `special_font.ttf`. The test pins that the asset loads as a TTF. (Task 2, Step 1, and an on-device screenshot.)
4. **Coexistence with `juloo.keyboard2`.** No hard-coded `juloo.keyboard2.` permission or authority may remain in the merged release manifest, or the install fails with CONFLICTING_PROVIDER or DUPLICATE_PERMISSION. (Task 3, Step 4 CI check.)
5. **Key loss.** Without the keystore no update is possible except by uninstalling, which loses the settings. The keystore and passwords must be in Bitwarden before the first release is installed. (Task 3, Step 2 check.)

---

### Task 1: Fork and the Grey Console theme

**Files:**
- Create: `test/juloo.keyboard2/GreyConsoleThemeTest.java`
- Modify: `res/values/themes.xml` (new style after `GradientPurplePink`)
- Modify: `res/values/arrays.xml` (`pref_theme_entries`, `pref_theme_values`: append one item each)
- Modify: `res/values/untranslated_strings.xml` (`pref_theme_e_greyconsole` = `Grey Console`; untranslated, so `sync_translations.py` is not needed)
- Modify: `srcs/juloo.keyboard2/Config.java` (`getThemeId`: `case "greyconsole": return R.style.GreyConsole;`)
- Move in: the spec and this plan into `docs/superpowers/`

**Interfaces:**
- Produces: theme value `greyconsole` (pref key `theme`) and style `R.style.GreyConsole`. Task 4 writes `greyconsole` into the prefs.

- [ ] **Step 1: Fork and branch**

Run in the chroot: `cd ~/projects && gh repo fork Julow/Unexpected-Keyboard --clone --fork-name Unexpected-Keyboard`. Then, in the clone: `git fetch upstream --tags && git switch -c grey-console 2.2.1`. Copy the spec and plan into `docs/superpowers/specs/` and `docs/superpowers/plans/`.
Expected: `git describe --tags` prints `2.2.1`.

- [ ] **Step 2: Write the failing test**

`GreyConsoleThemeTest` parses `res/values/themes.xml` and `res/values/arrays.xml` with `javax.xml.parsers.DocumentBuilder`. The working directory of `gradle test` is the repo root.
- `greyConsoleColours()`: style `GreyConsole` has parent `BaseTheme` and these exact items:
  - `android:isLightTheme` = `false`
  - `colorKeyboard` = `#0d0d0e`
  - `colorKey` = `#18181a`, `colorKeySpaceBar` = `#18181a`
  - `colorKeyAction` = `#303034`, `colorKeyActivated` = `#303034`
  - `colorLabel` = `#d0d0d0`
  - `colorSubLabel` = `#fdb827`
  - `colorLabelActivated` = `#bc61bc`
  - `colorLabelLocked` = `#00a99d`
  - `keyBorderWidth` = `0dp`, `keyBorderWidthActivated` = `0dp`
  - `emoji_button_bg` = `#18181a`, `emoji_color` = `#d0d0d0`
- `greyConsoleListed()`: `pref_theme_values` contains `greyconsole`, and `pref_theme_entries` has the same length as `pref_theme_values`.

- [ ] **Step 3: Run it and see it fail**

This runs in CI only (no compiling on the phone). Push the branch with only the test commit and run `gh workflow run make-apk.yml --ref grey-console`. Because the fork's Actions are off by default, first enable them once: `gh api -X PUT repos/6FaNcY9/Unexpected-Keyboard/actions/permissions -F enabled=true`.
Expected: the job "Run tests" fails in `GreyConsoleThemeTest` with "style GreyConsole not found".

- [ ] **Step 4: Add the style, the list entries, the string and the `Config.getThemeId` case**

These carry the values from Step 2, in the same order and format as the `Dracula` style.

- [ ] **Step 5: Push and see the tests pass**

Run: `gh run watch` on the new run of `make-apk.yml` on `grey-console`.
Expected: "Run tests" passes, and "Check generated files" shows no diff.

- [ ] **Step 6: Commit**

`git commit -m "Add Grey Console theme (bandit-nix palette)"`, then push.

---

### Task 2: 0xProto label font

**Files:**
- Create: `assets/0xProto-Regular.ttf` (0xProto 2.502 Regular from the github.com/0xType/0xProto release; this is the same base as the Termux font)
- Create: `assets/0xProto-OFL.txt` (the release's license file)
- Create: `test/juloo.keyboard2/LabelFontAssetTest.java`
- Modify: `srcs/juloo.keyboard2/Theme.java`:
  - next to `_key_font`: `static Typeface _label_font = null;` and `static public Typeface getLabelFont(Context context)`, which loads `"0xProto-Regular.ttf"` with `Typeface.createFromAsset`, lazily like `getKeyFont`;
  - the constructor (line 50): also call `getLabelFont(context);`;
  - `init_label_paint(config, null)` becomes `init_label_paint(config, _label_font)` for `_label_paint`, `_sublabel_paint` and `indication_paint`. `_special_*` keep `_key_font`.

**Interfaces:**
- Produces: `Theme.getLabelFont(Context): Typeface`.

- [ ] **Step 1: Write the failing test**

`LabelFontAssetTest.labelFontIsTrueType()`: `assets/0xProto-Regular.ttf` exists, is over 50 000 bytes, and its first 4 bytes are `00 01 00 00`. `assets/0xProto-OFL.txt` contains `SIL OPEN FONT LICENSE`.

- [ ] **Step 2: Push, see it fail in CI**

Expected: FAIL, the file does not exist.

- [ ] **Step 3: Add the font, the license and the `Theme.java` changes**

Before committing, check the glyphs on the phone: `fc-query --format '%{charset}'` (via `nix shell nixpkgs#fontconfig`) must cover U+00E4 U+00F6 U+00FC U+00DF U+20AC U+00A7 U+00B0 U+00B4.

- [ ] **Step 4: Push, see the tests and the debug APK build pass**

Expected: `make-apk.yml` is green.

- [ ] **Step 5: Commit**

`git commit -m "Draw labels in 0xProto"`.

---

### Task 3: Own identity and a signed release build

**Files:**
- Modify: `build.gradle.kts`:
  - `defaultConfig.applicationId = "juloo.keyboard2.pong"`;
  - in buildType `release`: `resValue("string", "app_name", "Unexpected Keyboard pong")` replaces the `@string/app_name_release` line, and `versionNameSuffix = "-pong"`.
- Create: `.github/workflows/pong-release.yml`
- Local only, never committed: `/data/data/com.termux/files/home/.android/uk-pong/{release.jks,store.pass}`, mode 600.

**Interfaces:**
- Produces: the artifact `uk-pong-release`, containing `build/outputs/apk/release/*.apk` and `cert.txt` (the signer SHA-256). Task 4 downloads it.
- GitHub secrets: `RELEASE_KEYSTORE_B64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` (= `ukpong`), `RELEASE_KEY_PASSWORD` (same as the store password).

- [ ] **Step 1: Make the key** (chroot, `nix shell nixpkgs#jdk17_headless nixpkgs#openssl`)

- `openssl rand -base64 24 > store.pass`;
- `keytool -genkeypair -keystore release.jks -alias ukpong -keyalg RSA -keysize 4096 -validity 36500 -dname CN=ukpong`, with the passwords read from `store.pass` via `-storepass:file`/`-keypass:file`;
- `chmod 600` on both files.

Expected: `keytool -list -keystore release.jks -storepass:file store.pass` shows one `PrivateKeyEntry`. The password is never echoed.

- [ ] **Step 2: Secrets and backup**

- `base64 -w0 release.jks | gh secret set RELEASE_KEYSTORE_B64`;
- `gh secret set RELEASE_KEYSTORE_PASSWORD < store.pass`, and the same for `RELEASE_KEY_PASSWORD`;
- `gh secret set RELEASE_KEY_ALIAS -b ukpong`.

Then the owner stores `release.jks` and `store.pass` in Bitwarden, with `bw` unlocked by the owner.
Expected: `gh secret list` shows all four names. **Do not continue until the owner confirms the Bitwarden copy.**

- [ ] **Step 3: Write `pong-release.yml`**

- Triggers: `push` to `grey-console`, and `workflow_dispatch`.
- Steps: checkout with `submodules: true`; setup-java temurin 17; setup-gradle 8.14.4; decode `RELEASE_KEYSTORE_B64` to `$RUNNER_TEMP/release.jks`; run `gradle test assembleRelease` with env `RELEASE_KEYSTORE=$RUNNER_TEMP/release.jks` plus the three password and alias secrets.
- Then, with the newest `$ANDROID_HOME/build-tools/*`:
  - `apksigner verify --print-certs` → `cert.txt`;
  - `aapt2 dump permissions` → printed to the log;
  - `aapt2 dump xmltree --file AndroidManifest.xml` piped to `grep -c 'juloo\.keyboard2\.[A-Z]'` must be 0 (Review Focus 4);
  - `aapt2 dump badging` must show `package: name='juloo.keyboard2.pong'`.
- Upload artifact `uk-pong-release` (the APK and `cert.txt`).

- [ ] **Step 4: Push, see the release build pass**

Run: `gh run watch` for `pong-release.yml`.
Expected:
- green;
- the log shows exactly the upstream permissions: VIBRATE, RECEIVE_BOOT_COMPLETED, INTERNET, ACCESS_NETWORK_STATE, and `juloo.keyboard2.pong.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`;
- `cert.txt` shows `CN=ukpong`.

- [ ] **Step 5: Commit**

`git commit -m "pong: own applicationId and signed release workflow"`, then push.

---

### Task 4: Install on pong and carry the settings over

**Files:** none in the repo. The phone state changes: the new package `juloo.keyboard2.pong` and its data dirs.

**Interfaces:**
- Consumes: the artifact `uk-pong-release` (Task 3) and theme value `greyconsole` (Task 1).

- [ ] **Step 1: Download and check**

- `gh run download -n uk-pong-release -D ~/storage/downloads/uk-pong`.
- Compare the SHA-256 of the APK with the log of the run.
- `cert.txt` must say `CN=ukpong`.

- [ ] **Step 2: Install (owner)**

- Try `su -c 'am start -a android.intent.action.VIEW -t application/vnd.android.package-archive -d file:///sdcard/Download/uk-pong/<apk>'`.
- If the installer does not open, the owner opens the APK in MiXplorer and taps Install.

Expected: `su -c 'dumpsys package juloo.keyboard2.pong'` shows `versionName=2.2.1-pong`, and `juloo.keyboard2` is still installed.

- [ ] **Step 3: Copy prefs and dicts (root; the new app must not be running: `kill -9 $(pidof juloo.keyboard2.pong)` if a pid exists)**

| from (`juloo.keyboard2`) | to (`juloo.keyboard2.pong`) |
|---|---|
| `/data/user_de/0/juloo.keyboard2/shared_prefs/juloo.keyboard2_preferences.xml` | `/data/user_de/0/juloo.keyboard2.pong/shared_prefs/juloo.keyboard2.pong_preferences.xml` |
| `/data/user_de/0/juloo.keyboard2/shared_prefs/dictionaries.xml` | same name under `/data/user_de/0/juloo.keyboard2.pong/shared_prefs/` |
| `/data/data/juloo.keyboard2/shared_prefs/juloo.keyboard2_preferences.xml` | `/data/data/juloo.keyboard2.pong/shared_prefs/juloo.keyboard2.pong_preferences.xml` |
| `/data/data/juloo.keyboard2/shared_prefs/{pinned_clipboards,emoji_last_use}.xml` | same names under `/data/data/juloo.keyboard2.pong/shared_prefs/` |
| `/data/data/juloo.keyboard2/files/*.dict` | `/data/data/juloo.keyboard2.pong/files/` |

- In both copied `*_preferences.xml`, replace `<string name="theme">monetdark</string>` with `<string name="theme">greyconsole</string>`.
- Then, for each target dir (`.../shared_prefs`, `.../files`, created with `mkdir -p` if missing), set:
  - `chown -R <uid>:<uid>`, where uid = `stat -c %u` of the package dir;
  - `chmod 660` on files and `771` on dirs;
  - `chcon -R "$(stat -c %C <package dir>)"`.

- [ ] **Step 4: Check the copy (Review Focus 1 and 2)**

Run (root):
- `grep -c greyconsole /data/user_de/0/juloo.keyboard2.pong/shared_prefs/*_preferences.xml` → 1;
- `ls -lZ` of both `shared_prefs` dirs and `files`: the owner is the app uid and the context equals the package dir's context;
- the `*.dict` sizes equal the source.

- [ ] **Step 5: Enable and look (owner)**

The owner enables "Unexpected Keyboard pong" under Settings › System › Keyboards and selects it. Open Termux and take a screenshot.
Expected:
- the dark Grey Console keys with 0xProto letters and yellow corner characters;
- QWERTZ-DE and QWERTY-US are both present;
- the circle gesture gives a capital letter;
- suggestions appear for German.

If anything is off, switch back to `juloo.keyboard2`, which is untouched.

- [ ] **Step 6: Record**

Update the fork README with one line on how to rebuild and update. When basic-memory is reachable again, note the project in `projects/<name>`.
