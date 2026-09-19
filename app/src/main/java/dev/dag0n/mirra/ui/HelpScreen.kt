package dev.dag0n.mirra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dag0n.mirra.R
import dev.dag0n.mirra.ui.theme.Muted
import dev.dag0n.mirra.ui.theme.Navy

@Composable
fun HelpScreen(onBack: () -> Unit) {
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
                text = stringResource(R.string.help),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        HelpBlock(stringResource(R.string.help_1_title), stringResource(R.string.help_1_body))
        HelpBlock(stringResource(R.string.help_2_title), stringResource(R.string.help_2_body))
        HelpBlock(stringResource(R.string.help_3_title), stringResource(R.string.help_3_body))
        HelpBlock(stringResource(R.string.help_4_title), stringResource(R.string.help_4_body))
        HelpBlock(stringResource(R.string.help_5_title), stringResource(R.string.help_5_body))
        HelpBlock(stringResource(R.string.help_6_title), stringResource(R.string.help_6_body))
    }
}

@Composable
private fun HelpBlock(title: String, body: String) {
    Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 18.dp, start = 8.dp))
    Text(body, color = Muted, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp))
}
