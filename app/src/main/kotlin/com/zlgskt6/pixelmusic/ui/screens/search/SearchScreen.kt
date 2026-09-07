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

package com.zlgskt6.pixelmusic.ui.screens.search

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zlgskt6.pixelmusic.viewmodels.OnlineSearchSuggestionViewModel
import com.zlgskt6.pixelmusic.viewmodels.LocalSearchViewModel
import com.zlgskt6.pixelmusic.LocalDatabase
import com.zlgskt6.pixelmusic.LocalPlayerAwareWindowInsets
import com.zlgskt6.pixelmusic.R
import com.zlgskt6.pixelmusic.constants.AppBarHeight
import com.zlgskt6.pixelmusic.constants.PauseSearchHistoryKey
import com.zlgskt6.pixelmusic.constants.PureBlackKey
import com.zlgskt6.pixelmusic.constants.SearchSource
import com.zlgskt6.pixelmusic.constants.SearchSourceKey
import com.zlgskt6.pixelmusic.db.entities.SearchHistory
import com.zlgskt6.pixelmusic.utils.rememberEnumPreference
import com.zlgskt6.pixelmusic.utils.rememberPreference

@Composable
fun SearchScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val database = LocalDatabase.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val pureBlack by rememberPreference(PureBlackKey, defaultValue = false)
    val pauseSearchHistory by rememberPreference(PauseSearchHistoryKey, defaultValue = false)
    var searchSource by rememberEnumPreference(SearchSourceKey, defaultValue = SearchSource.ONLINE)

    val activity = LocalContext.current as? ComponentActivity
    val onlineSearchViewModel: OnlineSearchSuggestionViewModel =
        if (activity != null) hiltViewModel(activity) else hiltViewModel()
    val localSearchViewModel: LocalSearchViewModel =
        if (activity != null) hiltViewModel(activity) else hiltViewModel()

    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    val topPadding = WindowInsets.systemBars
        .only(WindowInsetsSides.Top)
        .asPaddingValues()
        .calculateTopPadding() + AppBarHeight

    val bottomPadding = LocalPlayerAwareWindowInsets.current
        .asPaddingValues()
        .calculateBottomPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (pureBlack) Color.Black else MaterialTheme.colorScheme.background)
            .padding(top = topPadding)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (pureBlack) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .height(48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = if (pureBlack) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            val trimmed = query.text.trim()
                            if (trimmed.isNotEmpty()) {
                                keyboardController?.hide()
                                if (searchSource == SearchSource.ONLINE) {
                                    navController.navigate(onlineSearchResultRoute(trimmed))
                                    if (!pauseSearchHistory) {
                                        database.query {
                                            insert(SearchHistory(query = trimmed))
                                        }
                                    }
                                }
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        if (query.text.isEmpty()) {
                            Text(
                                text = stringResource(
                                    when (searchSource) {
                                        SearchSource.LOCAL -> R.string.search_library
                                        SearchSource.ONLINE -> R.string.search_yt_music
                                    }
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        innerTextField()
                    }
                )
                if (query.text.isNotEmpty()) {
                    IconButton(onClick = { query = TextFieldValue("") }) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = {
                        searchSource =
                            if (searchSource == SearchSource.ONLINE) SearchSource.LOCAL else SearchSource.ONLINE
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            when (searchSource) {
                                SearchSource.LOCAL -> R.drawable.library_music
                                SearchSource.ONLINE -> R.drawable.language
                            }
                        ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = bottomPadding)
        ) {
            Crossfade(
                targetState = searchSource,
                label = "searchSourceCrossfade"
            ) { source ->
                when (source) {
                    SearchSource.ONLINE -> OnlineSearchScreen(
                        query = query.text,
                        onQueryChange = { query = it },
                        navController = navController,
                        onSearch = { targetQuery ->
                            keyboardController?.hide()
                            navController.navigate(onlineSearchResultRoute(targetQuery))
                            if (!pauseSearchHistory) {
                                database.query {
                                    insert(SearchHistory(query = targetQuery))
                                }
                            }
                        },
                        onDismiss = {},
                        pureBlack = pureBlack,
                        viewModel = onlineSearchViewModel,
                    )
                    SearchSource.LOCAL -> LocalSearchScreen(
                        query = query.text,
                        navController = navController,
                        onDismiss = {},
                        pureBlack = pureBlack,
                        viewModel = localSearchViewModel,
                    )
                }
            }
        }
    }
}
