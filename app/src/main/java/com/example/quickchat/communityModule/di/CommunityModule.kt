package com.example.quickchat.communityModule.di

import com.example.quickchat.communityModule.repositories.CommunityRepository
import com.example.quickchat.communityModule.repositories.CommunityRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.contracts.Returns

@InstallIn(SingletonComponent::class)
@Module
class CommunityModule {

    @Provides
    @Singleton
    fun provideCommunityRepository(
        database: FirebaseFirestore,

        ): CommunityRepository {
        return CommunityRepositoryImpl(database)
    }
}