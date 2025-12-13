package com.example.jetcheckers.di

import com.shu.conversation.logic.CheckersScreen
import com.shu.design.models.Checkers

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

// API

// IMPL
@Module
@InstallIn(ActivityRetainedComponent::class)
object ConversationModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller =
        {

            entry<Checkers> {
                CheckersScreen()
            }
           /* entry<ConversationList> {
                ConversationListScreen(
                    onConversationClicked = { conversationDetail ->
                        navigator.goTo(conversationDetail)
                    }
                )
            }
            entry<ConversationDetail> { key ->
                ConversationDetailScreen(key) { navigator.goTo(Profile) }
            }*/
        }
}

