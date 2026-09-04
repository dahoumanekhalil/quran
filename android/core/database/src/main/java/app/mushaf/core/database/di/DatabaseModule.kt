package app.mushaf.core.database.di

import android.content.Context
import app.mushaf.core.database.QuranDb
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
    fun provideQuranDb(@ApplicationContext context: Context): QuranDb = QuranDb(context)
}
