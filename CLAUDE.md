# CLAUDE.md

Guidance for AI coding assistants working in this repo. The README has the full setup and
architecture; this file is the short version plus the non-obvious lessons.

## Project

Native Android app (Kotlin, Jetpack Compose, Material 3) for travel-planner. It shares a
Supabase backend with the Nuxt web app in the sibling repo `travel-planner`, not code. Current
scope: register (with email confirmation via deep link), log in, log out. The app was built in
12 planned steps; the README's "Dependency decisions" explain the stack.

The main developer comes from web development (Vue/Nuxt): explaining Android concepts with web
equivalents works well (ViewModel ≈ a Pinia store per screen, strings.xml ≈ locales/*.json,
R8 ≈ a bundler's minifier, Robolectric ≈ jsdom).

## Commands

```bash
./gradlew ktlintCheck lint testDebugUnitTest assembleDebug   # what CI runs; run before pushing
./gradlew ktlintFormat                                       # fix style
./gradlew testDebugUnitTest --tests '*LoginScreenTest'       # one test class
./gradlew installDebug                                       # install on the running emulator
```

`adb` is at `~/Library/Android/sdk/platform-tools/adb` (not on PATH).

## Architecture

- `data/auth/`: `AuthRepository` interface; `DefaultAuthRepository` is the only class that uses
  supabase-kt. `authState: Flow<AuthState>` (Loading / LoggedIn / LoggedOut) is the single
  source of truth for the session.
- `ui/<screen>/`: `XxxScreen.kt` (a stateless `XxxScreen` plus an `XxxRoute` that connects the
  `@HiltViewModel`), `XxxUiState.kt`, `XxxViewModel.kt`.
- `ui/navigation/`: Navigation 3. `AppRoot` renders a separate `AppNavigation` per auth state, so
  login/logout replaces the whole back stack. **Screens and ViewModels never navigate on login
  or logout**; they call the repository and `AppRoot` reacts to `authState`.
- `di/`: Hilt modules. Tests swap `AuthModule` for `FakeAuthRepository`.

## Conventions

- Every user-facing string goes in **both** `res/values/strings.xml` (Spanish, the default) and
  `res/values-en/strings.xml` (English). Keys match the web's `src/locales/*.json` where they
  overlap.
- `testTag`s use the web app's `data-cy` names (`login-submit`, `register-error`, …).
- New screens are wrapped in `Screen { }` in `AppNavigation` (a solid background, or the
  predictive back animation shows screens through each other).
- ViewModels: set loading state before launching, rethrow `CancellationException`, map errors to
  UiState. `LoginError` distinguishes `Network` (an `IOException`) from `WrongCredentials`.
- Never log emails, passwords or tokens. Auth failures are logged in debug builds by
  `DefaultAuthRepository` under the `AuthRepository` Logcat tag.
- Code style: ktlint with `android_studio` style and trailing commas (`.editorconfig`). Comments
  explain *why*, in plain sentences.
- Dependencies: versions in `gradle/libs.versions.toml`, stable releases only.

## Workflow

- One branch per change, named after what it does (no "step-N" prefixes); pull request into
  `main`. The developer merges pull requests themselves.
- `main` is protected: the CI check **Lint, test and build** must pass before merging.
- Commit messages: imperative subject, a body that explains what changed and why.

## Gotchas (each cost a debugging round)

- **Supabase email limit:** the built-in email service allows 2 auth emails/hour per project. A
  failed sign-up with HTTP 429 means "wait", not a bug. Use `+alias` Gmail addresses for test
  accounts. Check Logcat (`AuthRepository` tag) or Supabase → Logs → Auth before guessing.
- **Redirect URL:** `travelplanner://confirm` must be in Supabase's Redirect URLs, or the
  confirmation email links to the web's Site URL instead.
- Confirmation links are single-use: opening one anywhere (e.g. on the computer) uses it up.
- **supabase-kt `LogLevel.DEBUG` prints login tokens** to Logcat; it's kept at the default on
  purpose.
- **Robolectric:** `src/test/resources/robolectric.properties` pins `sdk=36` (Espresso doesn't
  support 37 yet), and `app/build.gradle.kts` passes `--add-exports
  java.base/jdk.internal.access=ALL-UNNAMED` to tests. Both are needed.
- Building with the CI placeholders (`SUPABASE_URL` env vars, no `local.properties`) bakes them
  into `BuildConfig`; rebuild with `local.properties` before running the app.
- Pushing changes under `.github/workflows/` needs the `workflow` scope on the GitHub CLI token
  (`gh auth refresh -h github.com -s workflow`).
- After changing Gradle files, Android Studio needs a Gradle sync, or imports look unresolved even
  though `./gradlew` builds.
