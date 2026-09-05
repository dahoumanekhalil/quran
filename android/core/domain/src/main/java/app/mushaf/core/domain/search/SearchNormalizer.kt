package app.mushaf.core.domain.search

/**
 * Loose consonantal-skeleton normalization for Arabic Quran search.
 *
 * MUST mirror the pipeline's `normalize_for_search()` in
 * `data/pipeline/pipeline.py` byte-for-byte. Any divergence silently degrades
 * search recall — the FTS5 `search_text` column was populated with the
 * pipeline's version; the client query has to hit the same skeleton.
 *
 * Strategy:
 *  - Strip harakat (fatha/kasra/damma/shadda/sukun/tanween — U+064B..U+0652).
 *  - Strip Quranic annotation marks (U+0610..U+061A and U+06D6..U+06ED).
 *  - Strip tatweel (U+0640, decorative kashida).
 *  - Strip ALL alef variants including bare alef (ا) and superscript alef (ٰ).
 *    Presence/absence of an inline alef must not affect matching.
 *  - Substitute alef maksura (ى) → ya (ي), ta marbuta (ة) → ha (ه).
 *  - Collapse runs of whitespace to a single space, then trim.
 *
 * Never apply this to text intended for display — it destroys diacritics.
 */
object SearchNormalizer {

    private val STRIP: Set<Int> = buildSet {
        // Harakat: fatha, kasra, damma, shadda, sukun, tanween forms.
        for (cp in 0x064B..0x0652) add(cp)
        // Quranic annotation marks (small high seen, small high madda, sajdah, ...).
        for (cp in 0x0610..0x061A) add(cp)
        for (cp in 0x06D6..0x06ED) add(cp)
        // Tatweel (kashida).
        add(0x0640)
        // Alef family — collapse every form so inline-alef presence doesn't matter.
        add(0x0627) // ا
        add(0x0622) // آ alef madda
        add(0x0623) // أ alef hamza above
        add(0x0625) // إ alef hamza below
        add(0x0671) // ٱ alef wasla
        add(0x0670) // ٰ superscript alef
    }

    private val SUB: Map<Int, Char> = mapOf(
        0x0649 to 'ي', // ى alef maksura → ي
        0x0629 to 'ه', // ة ta marbuta → ه
    )

    private val WHITESPACE = Regex("\\s+")

    fun normalize(input: String): String {
        if (input.isEmpty()) return ""
        val out = StringBuilder(input.length)
        for (ch in input) {
            val cp = ch.code
            if (cp in STRIP) continue
            val sub = SUB[cp]
            if (sub != null) out.append(sub) else out.append(ch)
        }
        return out.toString().replace(WHITESPACE, " ").trim()
    }
}
