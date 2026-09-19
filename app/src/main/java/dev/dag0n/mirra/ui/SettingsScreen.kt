package dev.dag0n.mirra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dag0n.mirra.R
import dev.dag0n.mirra.data.AppSettings
import dev.dag0n.mirra.ui.theme.Muted
import dev.dag0n.mirra.ui.theme.Navy

@Composable
fun SettingsScreen(
    settings: AppSettings,
    a11yEnabled: Boolean,
    onBack: () -> Unit,
    onChange: ((AppSettings) -> AppSettings) -> Unit,
    onReset: () -> Unit,
    onOpenA11y: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onReset) { Text(stringResource(R.string.reset)) }
        }

        Section(stringResource(R.string.section_image))
        Toggle(stringResource(R.string.setting_flip_h), stringResource(R.string.setting_flip_h_desc), settings.flipHorizontal) { checked ->
            onChange { it.copy(flipHorizontal = checked) }
        }
        Toggle(stringResource(R.string.setting_flip_v), stringResource(R.string.setting_flip_v_desc), settings.flipVertical) { checked ->
            onChange { it.copy(flipVertical = checked) }
        }
        Toggle(stringResource(R.string.setting_rotate), stringResource(R.string.setting_rotate_desc), settings.rotate180) { checked ->
            onChange { it.copy(rotate180 = checked) }
        }
        Toggle(stringResource(R.string.setting_crop), stringResource(R.string.setting_crop_desc), settings.cropSystemBars) { checked ->
            onChange { it.copy(cropSystemBars = checked) }
        }

        Section(stringResource(R.string.section_touch))
        Toggle(stringResource(R.string.setting_passthrough), stringResource(R.string.setting_passthrough_desc), settings.passThroughTouches) { checked ->
            onChange { current ->
                current.copy(passThroughTouches = checked, remapTouches = if (checked) false else current.remapTouches)
            }
        }
        Toggle(
            stringResource(R.string.setting_remap),
            stringResource(R.string.setting_remap_desc),
            settings.remapTouches,
            enabled = a11yEnabled,
        ) { checked ->
            onChange { current ->
                current.copy(remapTouches = checked, passThroughTouches = if (checked) false else current.passThroughTouches)
            }
        }
        if (!a11yEnabled) {
            TextButton(onClick = onOpenA11y) { Text(stringResource(R.string.enable_a11y_for_remap)) }
        }

        Section(stringResource(R.string.section_overlay))
        Toggle(stringResource(R.string.setting_bubble), stringResource(R.string.setting_bubble_desc), settings.showBubble) { checked ->
            onChange { it.copy(showBubble = checked) }
        }
        Toggle(stringResource(R.string.setting_keep_on), stringResource(R.string.setting_keep_on_desc), settings.keepScreenOn) { checked ->
            onChange { it.copy(keepScreenOn = checked) }
        }

        Text(
            text = stringResource(R.string.setting_opacity, settings.overlayOpacityPercent),
            modifier = Modifier.padding(top = 12.dp, start = 8.dp),
        )
        Slider(
            value = settings.overlayOpacityPercent.toFloat(),
            onValueChange = { value -> onChange { it.copy(overlayOpacityPercent = value.toInt()) } },
            valueRange = 20f..100f,
            steps = 15,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )

        Text(
            text = stringResource(R.string.setting_delay, settings.startDelaySeconds),
            modifier = Modifier.padding(top = 8.dp, start = 8.dp),
        )
        Slider(
            value = settings.startDelaySeconds.toFloat(),
            onValueChange = { value -> onChange { it.copy(startDelaySeconds = value.toInt()) } },
            valueRange = 0f..15f,
            steps = 14,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Section(title: String) {
    Text(
        text = title,
        color = Muted,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 20.dp, start = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun Toggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onChecked, enabled = enabled)
    }
}
