package com.example.quickchat.mainModule.di

import com.example.quickchat.constants.Constant
import com.example.quickchat.mainModule.inteface.ImageUploadApi
import com.example.quickchat.mainModule.repositories.MainRepositoryImp
import com.example.quickchat.mainModule.repositories.RepositoryMain
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {



    @Singleton
    @Provides
    fun provideRetroFit(): Retrofit {
        return Retrofit.Builder().baseUrl(Constant.BASE_URL_IMAGE_UPLOAD)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    @Singleton
    @Provides
    fun provideImageUploadApi(retrofit: Retrofit): ImageUploadApi {
        return retrofit.create(ImageUploadApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRepositoryMain(
        database: FirebaseFirestore,
        imageUploadApi: ImageUploadApi
    ): RepositoryMain {
        return MainRepositoryImp(database,imageUploadApi)
    }
}