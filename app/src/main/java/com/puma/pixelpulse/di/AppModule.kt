package com.puma.pixelpulse.di

import android.content.Context
import androidx.room.Room
import com.puma.pixelpulse.data.local.PixelPulseDatabase
import com.puma.pixelpulse.data.local.WallpaperDao
import com.puma.pixelpulse.data.repository.WallpaperRepositoryImpl
import com.puma.pixelpulse.domain.repository.WallpaperRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PixelPulseDatabase =
        Room.databaseBuilder(
            context,
            PixelPulseDatabase::class.java,
            "pixelpulse.db"
        )
            .addMigrations(
                PixelPulseDatabase.MIGRATION_1_2,
                PixelPulseDatabase.MIGRATION_2_3,
                PixelPulseDatabase.MIGRATION_3_4
            )
            .build()

    @Provides
    fun provideWallpaperDao(database: PixelPulseDatabase): WallpaperDao =
        database.wallpaperDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWallpaperRepository(impl: WallpaperRepositoryImpl): WallpaperRepository
}
