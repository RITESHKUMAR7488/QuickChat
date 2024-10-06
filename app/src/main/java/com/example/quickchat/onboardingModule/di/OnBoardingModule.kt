package com.example.quickchat.onboardingModule.di


import com.example.quickchat.onboardingModule.repositories.OnBoardingRepository
import com.example.quickchat.onboardingModule.repositories.OnBoardingRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class OnBoardingModule {

    @Provides
    @Singleton
    fun provideOnBoardingRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth
    ): OnBoardingRepository {
        return OnBoardingRepositoryImpl(database,auth)
    }
}