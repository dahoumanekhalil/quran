package app.mushaf.feature.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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

            Section("Feedback") {
                FeedbackLink(appVersionName = appVersionName, appVersionCode = appVersionCode)
            }
        }
    }
}

/**
 * mailto: link for user feedback (TASK-289). Fires a chooser via ACTION_SENDTO
 * so any installed mail app can compose. No network permission required —
 * ACTION_SENDTO delegates to the OS + a third-party mail app; nothing is
 * transmitted from Mushaf itself.
 */
@Composable
private fun FeedbackLink(appVersionName: String, appVersionCode: Int) {
    val ctx = LocalContext.current
    val subject = "Mushaf $appVersionName ($appVersionCode) feedback"
    val body = buildString {
        append("App version: ").append(appVersionName).append(" (").append(appVersionCode).append(")\n")
        append("Dataset: ").append(QuranAsset.CONTENT_VERSION).append("\n\n")
        append("(Please describe your feedback below.)")
    }
    Body("Send feedback via email")
    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                val sendTo = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.fromParts("mailto", FEEDBACK_EMAIL, null)
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                // Wrap in chooser so the OS handles the "no mail app" case
                // gracefully with a picker. Even so, on a bare device with
                // zero mail apps the chooser itself throws — fall back to a
                // Toast that shows the address so the user isn't stuck.
                val chooser = Intent.createChooser(sendTo, "Send feedback")
                try {
                    ctx.startActivity(chooser)
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(
                        ctx,
                        "No email app installed. Email us at $FEEDBACK_EMAIL",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
            .padding(vertical = 8.dp),
    ) {
        Text(
            FEEDBACK_EMAIL,
            style = MaterialTheme.typography.bodyMedium,
            color = MushafColors.Accent,
        )
    }
}

private const val FEEDBACK_EMAIL = "khalildahoumane@gmail.com"

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
