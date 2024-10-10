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

// Annotation that specifies this module will be installed in the SingletonComponent, meaning
// the provided dependencies will live for the entire lifecycle of the application.
@InstallIn(SingletonComponent::class)
@Module
class OnBoardingModule {

    // Annotates the function as a provider of a dependency.
    // This function will provide an instance of OnBoardingRepository.
    @Provides
    @Singleton // Ensures that only one instance of the provided object is created (Singleton pattern).
    fun provideOnBoardingRepository(
        database: FirebaseFirestore,  // FirebaseFirestore instance, injected automatically by Hilt.
        auth: FirebaseAuth            // FirebaseAuth instance, injected automatically by Hilt.
    ): OnBoardingRepository {
        // Returns a concrete implementation of OnBoardingRepository.
        return OnBoardingRepositoryImpl(database, auth)
    }
}
