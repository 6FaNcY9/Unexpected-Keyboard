# Grey Console keyboard: design

Date: 2026-11-18. Size: small (a theme and a font in a fork, plus CI and install).
This file moves into the fork as `docs/superpowers/specs/` once the fork exists.

## Intent
The owner's phone keyboard should match the shell rice (bandit-nix, `lib/repository.nix`
`shells`, palette `themes/material-sand-dark.yaml` "Grey Console"): the same 0xProto font
and the same colours as Termux and the starship prompt. Typing itself is perfect as it is
(Unexpected Keyboard: corner swipes, circle gesture, layouts); only the look changes.

## Goals
- A "Grey Console" theme in a fork of Unexpected Keyboard (Julow/Unexpected-Keyboard, GPL-3.0).
- Letters, digits and corner labels drawn in 0xProto.
- Installs next to the F-Droid build, carrying over its settings and dictionaries.

## Non-goals
- No change to typing behaviour, gestures, layouts or suggestions.
- No system-wide font change (OhMyFont / `/data/adb` modules stay untouched).
- No automatic upstream tracking for now; updating is a manual sync and rebuild.
- No upstream pull request for now (possible later if the theme turns out well).

## Anti-goals
- Anything that makes typing worse or less reliable than the F-Droid build.
- A keyboard signed with a public or throwaway key (an IME sees every keystroke, passwords included).

## Approaches considered
1. **Fork plus theme and font, built in GitHub Actions** (picked). One small patch on upstream, so it stays easy to update.
2. System font via an OhMyFont module: it changes the whole phone, the colours still don't match, and it touches `/data/adb`.
3. Re-patching the F-Droid APK (apktool): has to be redone by hand on every update, more fragile than a fork.
4. Upstream feature request: not in the owner's control, and slow.
5. A keyboard from scratch: it would re-implement gestures and layouts and end up worse.

## Design
- **Repo:** fork `6FaNcY9/Unexpected-Keyboard`, branch `grey-console`, based on upstream tag `2.2.1`
  (the phone runs F-Droid 2.1.0).
- **Identity:** applicationId `juloo.keyboard2.pong`, app name "Unexpected Keyboard pong", so it
  installs next to `juloo.keyboard2`.
- **Theme `greyconsole`** in `res/values/themes.xml`, `Config.java` and the theme list, from the palette:

  | Theme attribute | Colour | Same as |
  |---|---|---|
  | colorKeyboard | base00 `#0d0d0e` | Termux background |
  | colorKey, colorKeySpaceBar | base01 `#18181a` | |
  | colorKeyAction (Shift, Ctrl, Fn, Enter, ⌫, arrows) | base02 `#303034` | prompt frame |
  | colorKeyActivated | base02 `#303034` | |
  | colorLabel | base05 `#d0d0d0` | Termux text |
  | colorSubLabel (corner characters) | base0A `#fdb827` | git branch in the prompt |
  | colorLabelActivated (Shift/Ctrl on once) | base0E `#bc61bc` | the purple `pong` badge |
  | colorLabelLocked (Caps lock) | base0C `#00a99d` | |
  | key border | none, radius 5dp (BaseTheme) | |

- **Font:** 0xProto Regular (SIL OFL 1.1, license file shipped alongside) in `assets/`, used for
  `_label_paint`, `_sublabel_paint` and `indication_paint` in `Theme.java`, in every theme of the fork.
  Icons (arrows, Enter, ⌫) keep the app's own `special_font.ttf`.
- **Build:** GitHub Actions on push and tag (reuse upstream `make-apk.yml`); the release APK is
  signed through the env vars upstream already reads (`RELEASE_KEYSTORE`, `RELEASE_KEYSTORE_PASSWORD`, ...).
- **Signing key:** a new keystore only for this app, stored as GitHub secrets, with a copy in
  Bitwarden. `~/.android-pong.jks` is not reused.
- **Install** (`pm`, `ime` and `input` are dead from the shell on this phone):
  1. download the APK with `gh run download`;
  2. open the installer with `am start -a android.intent.action.VIEW`; the owner taps Install;
  3. before the first launch, root-copy `shared_prefs/` and `files/*.dict` from `juloo.keyboard2`
     to `juloo.keyboard2.pong`, then fix owner and SELinux context (`chown`, `restorecon`), and set `theme` to `greyconsole`;
  4. the owner enables and selects the keyboard in Android settings.
     The F-Droid keyboard stays installed as a fallback.
- **Permissions:** unchanged from upstream. INTERNET is used only to download dictionaries in the
  settings screen, and the F-Droid build has it too.

## Verification
- CI build is green; `apksigner verify --print-certs` shows the new key;
  `aapt dump permissions` matches upstream.
- On the phone: a screenshot of the keyboard in Termux; corner swipes, the circle gesture and both
  layouts work; de and en_GB suggestions still work.

## Left to the builder
- How exactly the theme gets into the theme list (array entries, translations: English only).
- Whether sublabels look better in base0A or base04; the table is the starting point.
