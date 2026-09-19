package dev.dag0n.mirra.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dag0n.mirra.R
import dev.dag0n.mirra.data.AppSettings
import dev.dag0n.mirra.ui.theme.Cyan
import dev.dag0n.mirra.ui.theme.Muted
import dev.dag0n.mirra.ui.theme.Navy
import dev.dag0n.mirra.ui.theme.NavyRaised
import dev.dag0n.mirra.ui.theme.Ok

@Composable
fun HomeScreen(
    settings: AppSettings,
    running: Boolean,
    overlayVisible: Boolean,
    status: String,
    overlayGranted: Boolean,
    a11yEnabled: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onToggle: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenA11ySettings: () -> Unit,
    onRequestNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHelp: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 36.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(NavyRaised),
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.tagline),
                    color = Muted,
                    fontSize = 14.sp,
                )
            }
            IconButton(onClick = onOpenHelp) {
                Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = stringResource(R.string.help))
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.home_intro), color = Muted, lineHeight = 20.sp)
        Spacer(Modifier.height(20.dp))

        PermissionCard(
            title = stringResource(R.string.perm_overlay),
            body = stringResource(R.string.perm_overlay_desc),
            ready = overlayGranted,
            icon = Icons.Outlined.Layers,
            action = stringResource(R.string.grant),
            onAction = onOpenOverlaySettings,
        )
        Spacer(Modifier.height(10.dp))
        PermissionCard(
            title = stringResource(R.string.perm_notifications),
            body = stringResource(R.string.perm_notifications_desc),
            ready = true,
            optional = true,
            icon = Icons.Outlined.Notifications,
            action = stringResource(R.string.grant),
            onAction = onRequestNotifications,
        )
        Spacer(Modifier.height(10.dp))
        PermissionCard(
            title = stringResource(R.string.perm_a11y),
            body = stringResource(R.string.perm_a11y_desc),
            ready = a11yEnabled,
            icon = Icons.Outlined.AccessibilityNew,
            action = stringResource(R.string.open),
            onAction = onOpenA11ySettings,
        )
        if (!a11yEnabled) {
            Text(
                text = stringResource(R.string.a11y_recommended),
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
            )
        }

        Spacer(Modifier.height(24.dp))
        if (running) {
            Text(
                text = if (overlayVisible) stringResource(R.string.status_running_on)
                else stringResource(R.string.status_running_off),
                color = Ok,
                fontWeight = FontWeight.SemiBold,
            )
            if (status.isNotBlank()) {
                Text(status, color = Muted, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan),
            ) {
                Text(
                    if (overlayVisible) stringResource(R.string.action_hide_overlay)
                    else stringResource(R.string.action_show_overlay)
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onStop, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(stringResource(R.string.action_stop))
            }
        } else {
            Button(
                onClick = onStart,
                enabled = overlayGranted,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan),
            ) {
                Text(stringResource(R.string.start_mirror), fontWeight = FontWeight.Bold)
            }
            Text(
                text = stringResource(R.string.start_hint),
                color = Muted,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(
                R.string.active_mode,
                if (settings.remapTouches) stringResource(R.string.mode_remap)
                else if (settings.passThroughTouches) stringResource(R.string.mode_passthrough)
                else stringResource(R.string.mode_block),
            ),
            color = Muted,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun PermissionCard(
    title: String,
    body: String,
    ready: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    action: String,
    onAction: () -> Unit,
    optional: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NavyRaised)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = if (ready) Ok else Cyan)
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body, color = Muted, fontSize = 12.sp)
            if (optional) {
                Text(stringResource(R.string.optional), color = Muted, fontSize = 11.sp)
            }
        }
        if (!ready) {
            TextButton(onClick = onAction) { Text(action) }
        } else {
            Text(stringResource(R.string.ready), color = Ok, fontSize = 12.sp)
        }
    }
}
