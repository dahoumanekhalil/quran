package app.mushaf.core.data.di

import app.mushaf.core.data.QuranRepositoryImpl
import app.mushaf.core.data.ReadingPositionRepositoryImpl
import app.mushaf.core.data.SettingsRepositoryImpl
import app.mushaf.core.domain.repository.QuranRepository
import app.mushaf.core.domain.repository.ReadingPositionRepository
import app.mushaf.core.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds @Singleton
    abstract fun bindQuranRepository(impl: QuranRepositoryImpl): QuranRepository

    @Binds @Singleton
    abstract fun bindReadingPositionRepository(impl: ReadingPositionRepositoryImpl): ReadingPositionRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
