package com.github.yukihane.sweet_pee.di

import com.github.yukihane.sweet_pee.data.network.ESMBGService
import com.github.yukihane.sweet_pee.data.network.WebScrapingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ネットワーク関連のDIモジュール
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWebScrapingService(): WebScrapingService {
        return WebScrapingService()
    }

    @Provides
    @Singleton
    fun provideESMBGService(webScrapingService: WebScrapingService): ESMBGService {
        return ESMBGService(webScrapingService)
    }
}
