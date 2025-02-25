package com.example.quickchat.mainModule.di

import com.example.quickchat.constants.Constant
import com.example.quickchat.mainModule.inteface.ImageUploadApi
import com.example.quickchat.mainModule.inteface.VideoGetApi
import com.example.quickchat.mainModule.repositories.MainRepositoryImp
import com.example.quickchat.mainModule.repositories.RepositoryMain
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {

    @Singleton
    @Provides
    @Named("ImageUploadRetrofit") // ✅ Naming the Retrofit instance for image upload
    fun provideRetroFit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constant.BASE_URL_IMAGE_UPLOAD)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideImageUploadApi(@Named("ImageUploadRetrofit") retrofit: Retrofit): ImageUploadApi {
        return retrofit.create(ImageUploadApi::class.java)
    }

    @Singleton
    @Provides
    @Named("VideoRetrofit") // ✅ Naming the Retrofit instance for video API
    fun provideVideoRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constant.BASE_URL_VIDEO_GET)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideVideoApi(@Named("VideoRetrofit") retrofit: Retrofit): VideoGetApi {
        return retrofit.create(VideoGetApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRepositoryMain(
        database: FirebaseFirestore,
        imageUploadApi: ImageUploadApi,
        videoGetApi: VideoGetApi
    ): RepositoryMain {
        return MainRepositoryImp(database, imageUploadApi, videoGetApi)
    }
}
