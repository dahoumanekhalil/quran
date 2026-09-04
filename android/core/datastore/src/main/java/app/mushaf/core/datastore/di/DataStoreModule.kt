package app.mushaf.core.datastore.di

import android.content.Context
import app.mushaf.core.datastore.UserPreferencesStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides @Singleton
    fun provideUserPrefsStore(@ApplicationContext context: Context): UserPreferencesStore =
        UserPreferencesStore(context)
}
