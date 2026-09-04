package app.mushaf.core.common.di

import app.mushaf.core.common.DefaultDispatcher
import app.mushaf.core.common.IoDispatcher
import app.mushaf.core.common.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides @Singleton @IoDispatcher
    fun provideIo(): CoroutineDispatcher = Dispatchers.IO

    @Provides @Singleton @DefaultDispatcher
    fun provideDefault(): CoroutineDispatcher = Dispatchers.Default

    @Provides @Singleton @MainDispatcher
    fun provideMain(): CoroutineDispatcher = Dispatchers.Main.immediate
}
