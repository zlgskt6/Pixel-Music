/*
 * Pixel Music (2026)
 * © Shahdullah — github.com/shahdullah
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * Based on ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.shahdullah.nomatune.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.shahdullah.nomatune.BuildConfig
import com.shahdullah.nomatune.R



@Composable
fun buildSettingsGroups(
    navController: NavController,
    isAndroid12OrLater: Boolean,
    hasUpdate: Boolean,
    context: Context,
): List<SettingsGroup> =
    buildList {
        add(
            SettingsGroup(
                title = stringResource(R.string.settings),
                items = listOf(
                    SettingsItem(
                        key = "account",
                        icon = painterResource(R.drawable.account),
                        title = stringResource(R.string.account),
                        subtitle = stringResource(R.string.settings_account_subtitle),
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate("settings/account") },
                    ),
                    SettingsItem(
                        key = "stats",
                        icon = painterResource(R.drawable.stats),
                        title = stringResource(R.string.settings_stats_title),
                        subtitle = stringResource(R.string.settings_stats_subtitle),
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate("stats") },
                    ),
                    SettingsItem(
                        key = "appearance",
                        icon = painterResource(R.drawable.palette),
                        title = stringResource(R.string.appearance),
                        subtitle = stringResource(R.string.settings_appearance_subtitle),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        onClick = { navController.navigate("settings/appearance") },
                    ),
                    SettingsItem(
                        key = "playback",
                        icon = painterResource(R.drawable.music_note),
                        title = stringResource(R.string.settings_playback_title),
                        subtitle = stringResource(R.string.settings_playback_subtitle),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        onClick = { navController.navigate("settings/player") },
                    ),


                    SettingsItem(
                        key = "backup_restore",
                        icon = painterResource(R.drawable.backup),
                        title = stringResource(R.string.backup_restore),
                        subtitle = stringResource(R.string.settings_backup_restore_subtitle),
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate("settings/backup_restore") },
                    ),
                ),
            ),
        )

        add(
            SettingsGroup(
                title = stringResource(R.string.settings_section_player_content),
                items = buildList {
                    add(
                        SettingsItem(
                            key = "content",
                            icon = painterResource(R.drawable.language),
                            title = stringResource(R.string.content),
                            subtitle = stringResource(R.string.settings_content_subtitle),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = { navController.navigate("settings/content") },
                        ),
                    )


                    add(
                        SettingsItem(
                            key = "storage",
                            icon = painterResource(R.drawable.storage),
                            title = stringResource(R.string.storage),
                            subtitle = stringResource(R.string.settings_storage_subtitle),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = { navController.navigate("settings/storage") },
                        ),
                    )

                    if (isAndroid12OrLater) {
                        add(
                            SettingsItem(
                                key = "default_links",
                                icon = painterResource(R.drawable.link),
                                title = stringResource(R.string.default_links),
                                subtitle = stringResource(R.string.open_supported_links),
                                accentColor = MaterialTheme.colorScheme.secondary,
                                onClick = {
                                    try {
                                        val intent = Intent(
                                            Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        when (e) {
                                            is ActivityNotFoundException,
                                            is SecurityException,
                                            -> {
                                                Toast.makeText(
                                                    context,
                                                    R.string.open_app_settings_error,
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                            else -> {
                                                Toast.makeText(
                                                    context,
                                                    R.string.open_app_settings_error,
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                        }
                                    }
                                },
                            ),
                        )
                    }
                    if (BuildConfig.UPDATER_AVAILABLE) {
                        add(
                            SettingsItem(
                                key = "updates",
                                icon = painterResource(R.drawable.update),
                                title = stringResource(R.string.updates),
                                subtitle = if (hasUpdate) {
                                    stringResource(R.string.new_version_available)
                                } else {
                                    stringResource(R.string.settings_updates_subtitle)
                                },
                                showUpdateIndicator = hasUpdate,
                                accentColor = if (hasUpdate) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                badge = if (hasUpdate) "v${BuildConfig.VERSION_NAME}" else BuildConfig.VERSION_NAME,
                                onClick = { navController.navigate("settings/update") },
                            ),
                        )
                    }

                },
            ),
        )
    }
