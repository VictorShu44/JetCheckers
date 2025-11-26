package com.example.jetcheckers.di

import com.example.jetcheckers.CatDetail
import com.example.jetcheckers.CatList
import com.shu.models.Profile
import com.shu.profile.ProfileScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet


// IMPLEMENTATION
/*@Module
@InstallIn(ActivityRetainedComponent::class)
object CatsModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller = {

        entry<CatList> {
            CatList(this@SharedTransitionLayout) { cat ->
                navigator.goTo (CatDetail(cat))
            }
        }
        entry<CatDetail> { args ->
            CatDetail(
                args.cat,
                this@SharedTransitionLayout
            ) {
                navigator.goBack()
            }
        }
    }
}*/
