package com.example.jetcheckers.di

import com.shu.models.Profile
import com.shu.profile.ProfileScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet


// IMPLEMENTATION
@Module
@InstallIn(ActivityRetainedComponent::class)
object ProfileModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(): EntryProviderInstaller = {
        entry<Profile> {
            ProfileScreen()
        }
    }
}
