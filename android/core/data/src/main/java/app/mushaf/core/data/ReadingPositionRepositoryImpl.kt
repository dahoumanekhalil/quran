package app.mushaf.core.data

import app.mushaf.core.datastore.UserPreferencesStore
import app.mushaf.core.domain.model.ReadingPosition
import app.mushaf.core.domain.repository.ReadingPositionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingPositionRepositoryImpl @Inject constructor(
    private val prefs: UserPreferencesStore,
) : ReadingPositionRepository {
    override fun observe(): Flow<ReadingPosition> = prefs.readingPosition
    override suspend fun get(): ReadingPosition = prefs.readingPositionOnce()
    override suspend fun save(position: ReadingPosition) = prefs.saveReadingPosition(position)
}
