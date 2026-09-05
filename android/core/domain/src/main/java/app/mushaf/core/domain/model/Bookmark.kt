package app.mushaf.core.domain.model

/**
 * A user-saved reading location per ADR-0025.
 *
 * MVP writes only page-scoped bookmarks (`ayahGlobalIndex == null`). The
 * nullable ayah field is reserved for a POST-MVP ayah-scoped variant so no
 * data-model migration is needed when that feature ships.
 *
 * Uniqueness key: `(pageNumber, ayahGlobalIndex)`. Default sort order in
 * the repository is `createdAtEpochMillis` descending.
 */
data class Bookmark(
    val pageNumber: Int,                        // 1..604
    val ayahGlobalIndex: Int? = null,           // POST-MVP; null in MVP
    val createdAtEpochMillis: Long,
)
