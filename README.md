# Travel Planner for Android

Native Android app (Kotlin + Jetpack Compose) for travel-planner. It shares the Supabase backend
with the Nuxt web app (`travel-planner`), not the code. So far it covers the account flows: register
with email confirmation, log in and log out. The UI is in Spanish by default, and in English on
phones set to English.

## Setup

1. Install [Android Studio](https://developer.android.com/studio) (it bundles the JDK, 21) and
   create an emulator in **Device Manager** (any recent Pixel image).
2. Clone the repo and open the folder in Android Studio. Let it finish the Gradle sync.
3. Add the Supabase values to `local.properties` in the project root (Android Studio creates the
   file; it's git-ignored, like the web app's `.env`):

   ```properties
   SUPABASE_URL=https://<project-ref>.supabase.co
   SUPABASE_KEY=<publishable key>
   ```

   They're the same values as the web app's `NUXT_PUBLIC_SUPABASE_URL` and
   `NUXT_PUBLIC_SUPABASE_KEY`, also in Supabase → **Project Settings → Data API / API Keys**.
   Copy the URL from there: a one-letter typo gives "Unable to resolve host" on every request.
4. Press **▶ Run**. The app opens on Login when logged out.

After pulling changes to `gradle/libs.versions.toml` or a `build.gradle.kts`, sync Gradle
(**File → Sync Project with Gradle Files**) before running, or new imports show as unresolved.

### Supabase project settings

The app relies on these, under Supabase → **Authentication**:

- **URL Configuration → Redirect URLs** must include `travelplanner://confirm`. The app asks
  Supabase to send the confirmation email's link there, so it opens the app and logs the user
  in. If it's missing, Supabase silently falls back to the **Site URL** (the web app's), and the
  link opens a web page instead. The web app's own URLs stay in the same list.
- **Email limit.** Supabase's built-in email service sends only 2 auth emails per hour for the
  whole project; the third sign-up in the hour fails with HTTP 429 and creates no user. For
  testing, use Gmail aliases (`you+test1@gmail.com`): each is a new user, all land in one inbox.
  A custom SMTP provider (e.g. Brevo, Resend) lifts the limit.
- Free-plan projects **pause** after about a week without activity; restore them from the
  dashboard.

To test the confirmation link, open the email **on the emulator** (Chrome → gmail.com):
`travelplanner://` links only work on a device with the app installed. The app uses Supabase's
PKCE flow, so the link carries a one-time code that only the install that signed up can exchange
for a session. Opened anywhere else, the email still gets confirmed, and the user logs in
normally.

## Commands

| Command | What it does |
|---|---|
| `./gradlew testDebugUnitTest` | All tests: ViewModel, screen and navigation tests (Robolectric, no emulator) |
| `./gradlew ktlintCheck` / `ktlintFormat` | Check / fix code style (`.editorconfig`) |
| `./gradlew lint` | Android Lint |
| `./gradlew assembleDebug` | Debug APK |
| `./gradlew assembleRelease` | Release APK, optimized with R8. Signed with the debug key for local testing; Google Play will need a real upload key |
| `./gradlew installDebug` | Build and install the debug app on the running emulator |

CI runs `./gradlew ktlintCheck lint testDebugUnitTest assembleDebug` on every pull request; a
pull request can only be merged into `main` when it passes.

Judge the app's speed on release builds: debug builds are much slower.

## How the app is organized

```
app/src/main/java/com/leireomadina/travelplanner/
├── MainActivity.kt        Entry point: splash screen, confirmation deep link, AppRoot
├── MainViewModel.kt       The session state (authState) for the whole app
├── TravelPlannerApp.kt    The Application class, which starts Hilt
├── data/auth/             AuthRepository (interface) and DefaultAuthRepository (supabase-kt)
├── di/                    Hilt modules: the Supabase client, AuthRepository binding
└── ui/
    ├── navigation/        NavKeys (one per screen), AppNavigation (back stack), AppRoot
    ├── login/  register/  home/   Each: Screen (Compose UI), UiState, ViewModel
    ├── components/        Shared UI (LoadingSpinner)
    └── theme/             Material 3 theme, with dynamic color
```

- **Screens get their data from a ViewModel**, which exposes a `UiState` and talks to
  `AuthRepository`. Only `DefaultAuthRepository` uses supabase-kt.
- **The session decides the screens.** `AppRoot` shows a navigation starting at Login when logged
  out, and a separate one starting at Home when logged in (like `redirectOptions` in the web's
  `nuxt.config.ts`). Logging in or out replaces the whole back stack, so back after logout can't
  reach Home. Screens never navigate on login or logout themselves.
- **Strings** live in `res/values/strings.xml` (Spanish, the default) and
  `res/values-en/strings.xml` (English), with the same keys as the web's `src/locales/*.json`
  where they overlap. Every new string goes in both.
- **testTags** match the web app's `data-cy` names (`login-submit`, `register-error`, …).

### Debugging

The screens only show generic error messages. In debug builds, `DefaultAuthRepository` logs the
real reason to Logcat under the **`AuthRepository`** tag, e.g.
`Sign-up failed: HTTP 429 over_email_send_rate_limit: …` (never the email or password). Filter
Logcat in Android Studio by that tag.

### Tests

- `src/test/` holds every test; they all run on the computer. ViewModel tests use
  `FakeAuthRepository` (in-memory; any password except `WRONG_PASSWORD` works).
- Screen tests (`*ScreenTest`) draw one screen with a given state. `AppNavigationTest` runs the
  whole app with Hilt swapping Supabase for `FakeAuthRepository`.
- `src/test/resources/robolectric.properties` pins the simulated Android to API 36, because
  Espresso doesn't support 37 (the targetSdk) yet. Bump it when it does.

## Dependency decisions

All versions live in `gradle/libs.versions.toml`, pinned to stable releases (no alpha, beta or
RC).

### Toolchain

- **Kotlin 2.4.20.** AGP 9 compiles Kotlin itself and ships with Kotlin 2.2.10, but supabase-kt
  3.8.0 is compiled with Kotlin 2.4, which 2.2 can't read. Declaring the
  `org.jetbrains.kotlin.android` plugin at the root with `apply false` raises the Kotlin version
  AGP uses; it isn't applied to any module.
- **compileSdk and targetSdk 37.** Navigation 3 1.2.0 and core-ktx 1.19.1 require compileSdk 37.
  targetSdk follows it so it stays current for Google Play.
- **minSdk 26.** Checked against the libraries: supabase-kt needs 23, and every AndroidX library
  used here supports 26.

### supabase-kt check (2026-09-27)

supabase-kt is community-maintained (`supabase-community/supabase-kt`), not built by the Supabase
team. Checked before adopting version 3.8.0:

| Check | Result |
|---|---|
| Sign-up / sign-in with email | `auth.signUpWith(Email)`, `auth.signInWith(Email)` |
| Email-confirmation deep link | `SupabaseClient.handleDeeplinks(intent)`, with configurable `scheme` / `host` |
| Sign-out | `auth.signOut()` |
| Session state | `auth.sessionStatus: StateFlow<SessionStatus>`: `Initializing`, `Authenticated`, `NotAuthenticated`, `RefreshFailure` (still refreshing an expired session) |
| Session persistence and refresh | `autoSaveToStorage`, `autoLoadFromStorage`, `alwaysAutoRefresh` |
| Compatibility | Built with Kotlin 2.4.0 (project uses 2.4.20), Ktor 3.6.0 OkHttp engine, library minSdk 23 |
| Maintenance | Stable releases roughly monthly (3.6.0 Apr, 3.7.0 Jul, 3.8.0 Aug 2026), last commit 2026-09-26, not archived |
| Risk | Mostly one maintainer. Contained: only `DefaultAuthRepository` will use it |

## Continuous integration

`.github/workflows/ci.yml` runs on every pull request and every push to `main`:

```
./gradlew ktlintCheck lint testDebugUnitTest assembleDebug
```

That's code style (ktlint, configured in `.editorconfig`), Android Lint, all unit and UI tests
(Robolectric, no emulator) and a debug build. Run the same command locally before pushing;
`./gradlew ktlintFormat` fixes most style findings automatically.

### Supabase values in CI

The build needs `SUPABASE_URL` and `SUPABASE_KEY`. Locally they come from `local.properties`,
which isn't in the repo. CI passes **placeholder values** as environment variables instead: no
test talks to Supabase (they use `FakeAuthRepository`), so a fake address is enough. The app CI
builds can't log in, though.

To have CI build an app that really connects (e.g. for releases), store the real values as
GitHub secrets:

1. On GitHub, open the repo → **Settings → Secrets and variables → Actions → New repository
   secret**.
2. Add `SUPABASE_URL` with the project URL (`https://<project-ref>.supabase.co`), and
   `SUPABASE_KEY` with the publishable key. They're in Supabase → **Project Settings → API
   Keys** / **Data API**, and in `local.properties`.
3. In `.github/workflows/ci.yml`, replace the placeholders:

   ```yaml
   env:
     SUPABASE_URL: ${{ secrets.SUPABASE_URL }}
     SUPABASE_KEY: ${{ secrets.SUPABASE_KEY }}
   ```

GitHub encrypts secrets, masks them in the logs, and never shows them again (they can only be
replaced). Pull requests from forks don't receive them.

