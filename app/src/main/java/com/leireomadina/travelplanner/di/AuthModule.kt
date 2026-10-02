package com.leireomadina.travelplanner.di

import com.leireomadina.travelplanner.data.auth.AuthRepository
import com.leireomadina.travelplanner.data.auth.DefaultAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    // Whoever asks for an AuthRepository gets the Supabase-backed one.
    // Tests replace this binding with FakeAuthRepository.
    @Binds
    @Singleton
    abstract fun bindAuthRepository(repository: DefaultAuthRepository): AuthRepository
}
