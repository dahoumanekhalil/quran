package app.mushaf.core.domain.repository

import app.mushaf.core.domain.model.ReadingPosition
import kotlinx.coroutines.flow.Flow

interface ReadingPositionRepository {
    fun observe(): Flow<ReadingPosition>
    suspend fun get(): ReadingPosition
    suspend fun save(position: ReadingPosition)
}
