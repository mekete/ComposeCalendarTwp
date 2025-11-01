package com.ethiopiancalendar.ui.more

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ethiopiancalendar.R

@Composable
fun MoreScreen(
    onNavigateToTheme: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.screen_title_more),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.menu_color_theme),
                    onClick = onNavigateToTheme
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Settings,
                    title = stringResource(R.string.menu_settings),
                    onClick = onNavigateToSettings
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Policy,
                    title = stringResource(R.string.menu_privacy_policy),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.freeprivacypolicy.com/blog/privacy-policy-url/"))
                        context.startActivity(intent)
                    }
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.menu_language),
                    onClick = {
                        // TODO: Implement language selection
                    }
                )
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.cd_navigate),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}