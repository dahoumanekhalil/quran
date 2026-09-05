package app.mushaf.core.domain.search

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SearchNormalizerTest {

    @Test
    fun emptyReturnsEmpty() {
        assertThat(SearchNormalizer.normalize("")).isEqualTo("")
    }

    @Test
    fun blankReturnsEmpty() {
        assertThat(SearchNormalizer.normalize("   \n \t  ")).isEqualTo("")
    }

    @Test
    fun stripsHarakat() {
        // بِسْمِ (with kasra, sukun, kasra) → بسم
        assertThat(SearchNormalizer.normalize("بِسْمِ")).isEqualTo("بسم")
    }

    @Test
    fun stripsShaddaAndFatha() {
        // ٱللَّهِ (alef wasla + lam + lam + fatha + shadda + ha + kasra) → لله
        assertThat(SearchNormalizer.normalize("ٱللَّهِ")).isEqualTo("لله")
    }

    @Test
    fun stripsAllAlefForms() {
        // Every alef variant vanishes so inline-alef presence doesn't affect matching.
        assertThat(SearchNormalizer.normalize("اآأإٱ")).isEqualTo("")
        assertThat(SearchNormalizer.normalize("اب")).isEqualTo("ب")
        assertThat(SearchNormalizer.normalize("ب")).isEqualTo("ب")
    }

    @Test
    fun stripsSuperscriptAlef() {
        // ٰ (U+0670) removed. Combined with alef maksura → ya substitution.
        assertThat(SearchNormalizer.normalize("علىٰ")).isEqualTo("علي")
    }

    @Test
    fun stripsTatweel() {
        // بـم → بم (tatweel between letters vanishes; no space inserted).
        assertThat(SearchNormalizer.normalize("بـم")).isEqualTo("بم")
    }

    @Test
    fun stripsQuranicMarks() {
        // ۩ (U+06E9 sajdah symbol) is in the U+06D6..U+06ED strip range.
        assertThat(SearchNormalizer.normalize("ب۩م")).isEqualTo("بم")
    }

    @Test
    fun substitutesAlefMaksuraToYa() {
        assertThat(SearchNormalizer.normalize("ى")).isEqualTo("ي")
    }

    @Test
    fun substitutesTaMarbutaToHa() {
        assertThat(SearchNormalizer.normalize("رحمة")).isEqualTo("رحمه")
    }

    @Test
    fun collapsesInternalWhitespace() {
        assertThat(SearchNormalizer.normalize("ب   م")).isEqualTo("ب م")
    }

    @Test
    fun trimsSurroundingWhitespace() {
        assertThat(SearchNormalizer.normalize("  بم  ")).isEqualTo("بم")
    }

    @Test
    fun realVerseSkeletonExample() {
        // بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ → "بسم لله لرحمن لرحيم"
        // All alef forms stripped, harakat + shadda + tatweel + superscript alef stripped.
        val input = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ"
        assertThat(SearchNormalizer.normalize(input)).isEqualTo("بسم لله لرحمن لرحيم")
    }

    @Test
    fun samplePrefixMatchesInlineAlefVariant() {
        // Users typing with or without inline alef must produce the same skeleton.
        val withAlef = SearchNormalizer.normalize("الرحمن")
        val withoutAlef = SearchNormalizer.normalize("لرحمن")
        assertThat(withAlef).isEqualTo(withoutAlef)
        assertThat(withAlef).isEqualTo("لرحمن")
    }

    @ParameterizedTest
    @CsvSource(
        // (input, expected)
        "'ا', ''",
        "'اا', ''",
        "'ب', 'ب'",
        "'ةه', 'هه'",
        "'ىى', 'يي'",
        "'ـ', ''",
    )
    fun paramTable(input: String, expected: String) {
        assertThat(SearchNormalizer.normalize(input)).isEqualTo(expected)
    }
}
