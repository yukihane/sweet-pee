package com.github.yukihane.sweet_pee.di

import android.content.Context
import androidx.room.Room
import com.github.yukihane.sweet_pee.data.database.AppDatabase
import com.github.yukihane.sweet_pee.data.database.BloodGlucoseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * データベース関連のDIモジュール
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "sweet_pee_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBloodGlucoseDao(database: AppDatabase): BloodGlucoseDao {
        return database.bloodGlucoseDao()
    }
}
