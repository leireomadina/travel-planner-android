package com.leireomadina.travelplanner.di

import com.leireomadina.travelplanner.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
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
        // In debug builds, log every request and error to Logcat (tag "Supabase"), since
        // the screens only show a generic message
        if (BuildConfig.DEBUG) defaultLogLevel = LogLevel.DEBUG
        install(Auth) {
            // The confirmation email's link opens travelplanner://confirm, which Android
            // routes to MainActivity (see the intent-filter in AndroidManifest.xml).
            // With these set, signUpWith asks Supabase to redirect there instead of the
            // Site URL. Must match an entry in Supabase → Authentication → URL
            // Configuration → Redirect URLs.
            scheme = "travelplanner"
            host = "confirm"
        }
    }
}
