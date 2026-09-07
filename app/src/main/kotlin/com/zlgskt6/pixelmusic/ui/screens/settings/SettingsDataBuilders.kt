/*
 * Pixel Music (2026)
 * © zlgskt6 — github.com/zlgskt6
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * Based on ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.zlgskt6.pixelmusic.ui.screens.settings

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.zlgskt6.pixelmusic.BuildConfig
import com.zlgskt6.pixelmusic.R
import com.zlgskt6.pixelmusic.utils.isRecapTimePeriod



@Composable
fun buildSettingsGroups(
    navController: NavController,
    isAndroid12OrLater: Boolean,
    hasUpdate: Boolean,
    context: Context,
): List<SettingsGroup> =
    buildList {
        val isRecap = isRecapTimePeriod()
        add(
            SettingsGroup(
                title = stringResource(R.string.settings),
                items = buildList {
                    add(
                        SettingsItem(
                            key = "account",
                            icon = painterResource(R.drawable.account),
                            title = stringResource(R.string.account),
                            subtitle = stringResource(R.string.settings_account_subtitle),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = { navController.navigate("settings/account") },
                        ),
                    )
                    if (isRecap) {
                        add(
                            SettingsItem(
                                key = "stats",
                                icon = painterResource(R.drawable.stats),
                                title = stringResource(R.string.settings_stats_title),
                                subtitle = stringResource(R.string.settings_stats_subtitle),
                                accentColor = MaterialTheme.colorScheme.primary,
                                onClick = { navController.navigate("stats") },
                            ),
                        )
                    }
                    add(
                        SettingsItem(
                            key = "appearance",
                            icon = painterResource(R.drawable.palette),
                            title = stringResource(R.string.appearance),
                            subtitle = stringResource(R.string.settings_appearance_subtitle),
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onClick = { navController.navigate("settings/appearance") },
                        ),
                    )
                    add(
                        SettingsItem(
                            key = "playback",
                            icon = painterResource(R.drawable.music_note),
                            title = stringResource(R.string.settings_playback_title),
                            subtitle = stringResource(R.string.settings_playback_subtitle),
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            onClick = { navController.navigate("settings/player") },
                        ),
                    )


                    add(
                        SettingsItem(
                            key = "backup_restore",
                            icon = painterResource(R.drawable.backup),
                            title = stringResource(R.string.backup_restore),
                            subtitle = stringResource(R.string.settings_backup_restore_subtitle),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = { navController.navigate("settings/backup_restore") },
                        ),
                    )
                },
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

                    add(
                        SettingsItem(
                            key = "about",
                            icon = painterResource(R.drawable.info),
                            title = stringResource(R.string.about),
                            subtitle = stringResource(R.string.settings_about_subtitle),
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = { navController.navigate("settings/about") },
                        ),
                    )

                },
            ),
        )
    }
