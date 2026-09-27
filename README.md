# Travel Planner for Android

Native Android app (Kotlin + Jetpack Compose) for travel-planner. It shares the Supabase backend
with the Nuxt web app, not the code. Setup and run instructions will be added in build step 12.

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
