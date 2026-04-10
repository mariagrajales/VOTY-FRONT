package com.jmvoty.votacion.core.hardware.di

import android.content.Context
import com.jmvoty.votacion.core.data.preferences.HardwarePreferencesDataSource
import com.jmvoty.votacion.core.data.preferences.HardwarePreferencesDataSourceImpl
import com.jmvoty.votacion.core.hardware.ShakeDetector
import com.jmvoty.votacion.core.hardware.ShakeDetectorImpl
import com.jmvoty.votacion.core.hardware.VibratorManager
import com.jmvoty.votacion.core.hardware.VibratorManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HardwareModule {

    @Provides
    @Singleton
    fun provideHardwarePreferencesDataSource(
        @ApplicationContext context: Context
    ): HardwarePreferencesDataSource =
        HardwarePreferencesDataSourceImpl(context)

    @Provides
    @Singleton
    fun provideVibratorManager(
        @ApplicationContext context: Context,
        preferencesDataSource: HardwarePreferencesDataSource
    ): VibratorManager = VibratorManagerImpl(context, preferencesDataSource)

    @Provides
    @Singleton
    fun provideShakeDetector(
        @ApplicationContext context: Context
    ): ShakeDetector = ShakeDetectorImpl(context)
}
