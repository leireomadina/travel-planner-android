package com.leireomadina.travelplanner.di

import com.leireomadina.travelplanner.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    // One client for the whole app: it holds the session and is expensive to create.
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY,
    ) {
        // Logging stays at supabase-kt's default level on purpose: DEBUG would also print
        // login tokens to Logcat. Failures are logged by DefaultAuthRepository instead.
        install(Auth) {
            // The confirmation email's link opens travelplanner://confirm, which Android
            // routes to MainActivity (see the intent-filter in AndroidManifest.xml).
            // With these set, signUpWith asks Supabase to redirect there instead of the
            // Site URL. Must match an entry in Supabase → Authentication → URL
            // Configuration → Redirect URLs.
            scheme = "travelplanner"
            host = "confirm"
            // PKCE: the link carries a one-time code instead of the session itself. Only this
            // install can exchange it, with a secret (the code verifier) saved on the device at
            // sign-up. So another app can't log the user in with a forged link, and one that
            // intercepts the link gets nothing usable.
            flowType = FlowType.PKCE
        }
    }
}
