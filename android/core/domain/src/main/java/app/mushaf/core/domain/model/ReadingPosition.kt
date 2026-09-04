package app.mushaf.core.domain.model

/**
 * The user's last-read position. Persisted whenever it changes and
 * restored on app start (charter — "immediately return to the Quran at
 * their last reading position").
 */
data class ReadingPosition(
    val pageNumber: Int,                    // 1..604
    val ayahGlobalIndex: Int? = null,       // optional finer-grained anchor
    val updatedAtEpochMillis: Long = 0L,
) {
    companion object {
        val DEFAULT = ReadingPosition(pageNumber = 1)
    }
}
