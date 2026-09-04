package app.mushaf.core.domain.repository

import app.mushaf.core.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observe(): Flow<AppSettings>
    suspend fun current(): AppSettings
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
