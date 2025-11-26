package com.example.jetcheckers.di

import com.shu.design.models.ConversationList
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object AppModule {

    @Provides
    @ActivityRetainedScoped
    fun provideNavigator() : Navigator = Navigator(startDestination = ConversationList)

 @Provides
    @ActivityRetainedScoped
    fun provideShared() : Navigator = Navigator(startDestination = ConversationList)
}
