package com.example.jetcheckers.di

import com.shu.conversation.ConversationDetailScreen
import com.shu.conversation.ConversationListScreen
import com.shu.design.models.ConversationDetail
import com.shu.design.models.ConversationList
import com.shu.models.Profile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

/*
@Module
@InstallIn(ActivityRetainedComponent::class)
object BottomModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller =
        {
            entry<ConversationList> {
                ConversationListScreen(
                    onConversationClicked = { conversationDetail ->
                        navigator.goTo(conversationDetail)
                    }
                )
            }
            entry<ConversationDetail> { key ->
                ConversationDetailScreen(key) { navigator.goTo(Profile) }
            }
        }
}*/
