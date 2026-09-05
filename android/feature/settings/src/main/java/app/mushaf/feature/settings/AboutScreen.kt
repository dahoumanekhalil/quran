package app.mushaf.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mushaf.core.database.QuranAsset
import app.mushaf.core.designsystem.MushafColors

/**
 * Static "About" content. Version is passed in from the caller (app module has
 * BuildConfig). Dataset info comes from `QuranAsset` — the same object that
 * gates first-launch integrity checking.
 */
@Composable
fun AboutRoute(
    appVersionName: String,
    appVersionCode: Int,
    onClose: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "About",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).padding(end = 48.dp),
            )
        }

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Section("Mushaf") {
                Detail("Version", "$appVersionName ($appVersionCode)")
                Detail("Package", "app.mushaf")
            }

            Section("Quran content") {
                Detail("Dataset version", QuranAsset.CONTENT_VERSION)
                Detail("SHA-256", QuranAsset.CONTENT_SHA256, monospace = true)
                Body(
                    "Text: Tanzil Uthmani v1.1 (Creative Commons Attribution 3.0).\n" +
                        "Pagination: quran-center/quran-meta (Madani 15-line 604-page). MIT.\n" +
                        "Cross-verified against Quran.com KFGQPC — 6236/6236 skeleton-equal.",
                )
            }

            Section("Fonts") {
                Body(
                    "KFGQPC Uthmanic Hafs v2.2 — the typeface of the printed Madani mushaf. " +
                        "Bundled per the King Fahd Quran Printing Complex EULA (free-distribution allowed).\n\n" +
                        "Amiri Quran v1.003 — SIL Open Font License 1.1. Bundled as an alternate.",
                )
            }

            Section("Privacy") {
                Body(
                    "This app is fully offline. It contains no advertisements, no analytics, " +
                        "no tracking, no telemetry, and no user accounts. Nothing you read, " +
                        "search for, or bookmark ever leaves your device.\n\n" +
                        "The only network permission requested is none.",
                )
            }

            Section("Source") {
                Body("https://github.com/dahoumanekhalil/quran")
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MushafColors.Muted,
        modifier = Modifier.padding(top = 8.dp),
    )
    HorizontalDivider(color = MushafColors.Muted.copy(alpha = 0.15f))
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        content()
    }
}

@Composable
private fun Detail(label: String, value: String, monospace: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MushafColors.Muted,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            value,
            style = if (monospace) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Body(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}
