package com.example.quickchat.di

import android.content.Context
import com.example.quickchat.MyApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {
    @Provides
    fun provideMyApp(@ApplicationContext context: Context): MyApplication {
        return context.applicationContext as MyApplication
    }
}