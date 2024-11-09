package com.example.quickchat.mainModule.di

import com.example.quickchat.mainModule.repositories.MainRepositoryImp
import com.example.quickchat.mainModule.repositories.RepositoryMain
import com.example.quickchat.onboardingModule.repositories.OnBoardingRepository
import com.example.quickchat.onboardingModule.repositories.OnBoardingRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {

    @Provides
    @Singleton
    fun provideRepositoryMain(
        database: FirebaseFirestore,
    ): RepositoryMain {
        return MainRepositoryImp(database)
    }
}