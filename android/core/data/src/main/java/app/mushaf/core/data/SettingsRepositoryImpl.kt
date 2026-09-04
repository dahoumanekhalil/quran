package app.mushaf.core.data

import app.mushaf.core.datastore.UserPreferencesStore
import app.mushaf.core.domain.model.AppSettings
import app.mushaf.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val prefs: UserPreferencesStore,
) : SettingsRepository {
    override fun observe(): Flow<AppSettings> = prefs.settings
    override suspend fun current(): AppSettings = prefs.settingsOnce()
    override suspend fun update(transform: (AppSettings) -> AppSettings) =
        prefs.updateSettings(transform)
}
