/*
 * Pixel Music (2026)
 * © Zlgskt6 — github.com/zlgskt6
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * Based on ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.shahdullah.nomatune.ui.screens

import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.shahdullah.nomatune.innertube.YouTube
import com.shahdullah.nomatune.innertube.models.BrowseEndpoint
import com.shahdullah.nomatune.LocalPlayerAwareWindowInsets
import com.shahdullah.nomatune.LocalPlayerConnection
import com.shahdullah.nomatune.R
import com.shahdullah.nomatune.extensions.toMediaItem
import com.shahdullah.nomatune.library.LibraryTopMixId
import com.shahdullah.nomatune.viewmodels.LibraryTopMixUiModel
import com.shahdullah.nomatune.playback.queues.ListQueue
import com.shahdullah.nomatune.ui.component.NavigationTitle
import com.shahdullah.nomatune.ui.component.shimmer.ShimmerHost
import com.shahdullah.nomatune.ui.component.shimmer.TextPlaceholder
import com.shahdullah.nomatune.viewmodels.LibraryMixViewModel
import com.shahdullah.nomatune.viewmodels.LibraryTopMixesUiState
import com.shahdullah.nomatune.viewmodels.MoodAndGenresViewModel
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoodAndGenresScreen(
    navController: NavController,
    viewModel: MoodAndGenresViewModel = hiltViewModel(),
    libraryMixViewModel: LibraryMixViewModel = hiltViewModel(),
) {
    val moodAndGenres by viewModel.moodAndGenres.collectAsState()
    val topMixesUiState by libraryMixViewModel.topMixesUiState.collectAsStateWithLifecycle()
    val playerConnection = LocalPlayerConnection.current
    val gridState = rememberLazyGridState()
    val density = LocalDensity.current
    val windowInsets = LocalPlayerAwareWindowInsets.current
    val topPadding = with(density) { windowInsets.getTop(this).toDp() }
    val bottomPadding = with(density) { windowInsets.getBottom(this).toDp() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop =
        backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            gridState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        state = gridState,
        contentPadding = PaddingValues(
            start = 6.dp,
            top = topPadding,
            end = 6.dp,
            bottom = bottomPadding,
        ),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "top_mixes") {
            TopMixesForYouSection(
                state = topMixesUiState,
                onPlayMix = { mix ->
                    playerConnection?.playQueue(
                        ListQueue(
                            items = mix.tracks.map { it.toMediaItem() },
                        ),
                    )
                },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            NavigationTitle(
                title = stringResource(R.string.what_are_you_feeling_like),
                modifier = Modifier.animateItem(),
            )
        }

        if (moodAndGenres == null) {
            items(
                count = 12,
                key = { index -> "mood_genres_shimmer_$index" },
                contentType = { "mood_genres_shimmer" },
            ) {
                ShimmerHost {
                    TextPlaceholder(
                        height = MoodAndGenresButtonHeight,
                        shape = MoodAndGenresButtonShape,
                        modifier = Modifier.padding(6.dp),
                    )
                }
            }
        } else {
            items(
                items = moodAndGenres.orEmpty(),
                key = { item -> "${item.title}:${item.endpoint.browseId}:${item.endpoint.params}" },
                contentType = { "mood_genres_item" },
            ) { item ->
                MoodAndGenresButton(
                    title = item.title,
                    stripeColor = item.stripeColor,
                    endpoint = item.endpoint,
                    onClick = {
                        navController.navigate("youtube_browse/${item.endpoint.browseId}?params=${item.endpoint.params}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .animateItem(),
                )
            }
        }
    }
}

@Composable
private fun TopMixesForYouSection(
    state: LibraryTopMixesUiState,
    onPlayMix: (LibraryTopMixUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        LibraryTopMixesUiState.Loading -> Unit
        LibraryTopMixesUiState.Empty -> TopMixesMessageSection(
            message = stringResource(R.string.build_your_mix_empty_library),
            modifier = modifier,
        )
        is LibraryTopMixesUiState.Error -> TopMixesMessageSection(
            message = state.message,
            modifier = modifier,
        )
        is LibraryTopMixesUiState.Success -> {
            if (state.mixes.isEmpty()) {
                TopMixesMessageSection(
                    message = stringResource(R.string.build_your_mix_empty_library),
                    modifier = modifier,
                )
            } else {
                Column(modifier = modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.top_mixes),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(
                            items = state.mixes,
                            key = { mix -> mix.id },
                            contentType = { "library_top_mix" },
                        ) { mix ->
                            LibraryTopMixCard(
                                mix = mix,
                                onPlay = { onPlayMix(mix) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopMixesMessageSection(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.top_mixes),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun LibraryTopMixCard(
    mix: LibraryTopMixUiModel,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "LibraryTopMixCardScale",
    )

    Box(
        modifier = modifier
            .width(180.dp)
            .height(130.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onPlay,
            )
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = stringResource(mix.id.titleRes()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(mix.id.descriptionRes()),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    mix.previewArtworkUrls.forEach { artworkUrl ->
                        AsyncImage(
                            model = artworkUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                        )
                    }
                }

                IconButton(
                    onClick = onPlay,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

private fun LibraryTopMixId.titleRes(): Int =
    when (this) {
        LibraryTopMixId.DAILY -> R.string.daily_mix_1
        LibraryTopMixId.CHILL -> R.string.chill_mix
        LibraryTopMixId.FOCUS -> R.string.focus_mix
    }

private fun LibraryTopMixId.descriptionRes(): Int =
    when (this) {
        LibraryTopMixId.DAILY -> R.string.daily_mix_1_desc
        LibraryTopMixId.CHILL -> R.string.chill_mix_desc
        LibraryTopMixId.FOCUS -> R.string.focus_mix_desc
    }

@Composable
fun MoodAndGenresButton(
    title: String,
    stripeColor: Long,
    endpoint: BrowseEndpoint? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val base = remember(stripeColor) { Color(stripeColor) }
    val artworkUrl = rememberMoodAndGenresArtworkUrl(endpoint)
    val artworkModel = rememberMoodAndGenresArtworkModel(endpoint = endpoint, artworkUrl = artworkUrl)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coverShadow = with(LocalDensity.current) { 18.dp.toPx() }
    val cardStart = remember(base, colorScheme.primaryContainer) {
        lerp(base, colorScheme.primaryContainer, 0.18f)
    }
    val cardEnd = remember(base, colorScheme.surfaceContainerHighest) {
        lerp(base, colorScheme.surfaceContainerHighest, 0.34f)
    }
    val topGlow = remember(base) {
        lerp(base, Color.White, 0.24f).copy(alpha = 0.26f)
    }
    val coverStart = remember(base) {
        lerp(base, Color.White, 0.36f)
    }
    val coverEnd = remember(base, colorScheme.scrim) {
        lerp(base, colorScheme.scrim, 0.2f)
    }
    val coverAccent = remember(base, colorScheme.tertiary) {
        lerp(base, colorScheme.tertiary, 0.16f).copy(alpha = 0.5f)
    }
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 560f),
        label = "MoodAndGenresCardScale",
    )
    val coverRotation by animateFloatAsState(
        targetValue = if (isPressed) 14f else 21f,
        animationSpec = spring(dampingRatio = 0.74f, stiffness = 420f),
        label = "MoodAndGenresCoverRotation",
    )
    Box(
        modifier = modifier
            .height(MoodAndGenresButtonHeight)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clip(MoodAndGenresButtonShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(cardStart, cardEnd),
                    start = Offset.Zero,
                    end = Offset(900f, 650f),
                ),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val glowBrush = Brush.radialGradient(
                        colors = listOf(topGlow, Color.Transparent),
                        center = Offset(size.width * 0.86f, size.height * 0.16f),
                        radius = size.minDimension * 0.95f,
                    )
                    val depthBrush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f)),
                        startY = size.height * 0.24f,
                        endY = size.height,
                    )
                    onDrawBehind {
                        drawRect(glowBrush)
                        drawRect(depthBrush)
                    }
                },
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 32.dp)
                .size(80.dp)
                .graphicsLayer {
                    alpha = 0.24f
                    rotationZ = 13f
                    shape = MoodAndGenresCoverShape
                    clip = true
                    transformOrigin = TransformOrigin(1f, 0f)
                }
                .background(
                    Brush.linearGradient(
                        colors = listOf(coverStart.copy(alpha = 0.8f), coverEnd.copy(alpha = 0.74f)),
                        start = Offset.Zero,
                        end = Offset(480f, 480f),
                    ),
                ),
        ) {
            if (artworkModel != null) {
                AsyncImage(
                    model = artworkModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 16.dp)
                .size(90.dp)
                .graphicsLayer {
                    rotationZ = coverRotation
                    shadowElevation = coverShadow
                    ambientShadowColor = base.copy(alpha = 0.28f)
                    spotShadowColor = base.copy(alpha = 0.42f)
                    shape = MoodAndGenresCoverShape
                    clip = true
                    transformOrigin = TransformOrigin(1f, 0f)
                }
                .background(
                    Brush.linearGradient(
                        colors = listOf(coverStart, coverEnd),
                        start = Offset.Zero,
                        end = Offset(560f, 560f),
                    ),
                ),
        ) {
            if (artworkModel != null) {
                AsyncImage(
                    model = artworkModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val sheenBrush = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height),
                        )
                        val accentBrush = Brush.radialGradient(
                            colors = listOf(coverAccent, Color.Transparent),
                            center = Offset(size.width * 0.78f, size.height * 0.22f),
                            radius = size.minDimension * 0.44f,
                        )
                        onDrawBehind {
                            drawRect(sheenBrush)
                            drawRect(accentBrush)
                        }
                    },
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.35f),
                    offset = Offset(0f, 1f),
                    blurRadius = 4f,
                ),
            ),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 92.dp, bottom = 16.dp),
        )
    }
}

@Composable
private fun rememberMoodAndGenresArtworkUrl(endpoint: BrowseEndpoint?): String? {
    endpoint ?: return null

    val cacheKey = buildMoodAndGenresArtworkCacheKey(endpoint)
    val cachedArtwork = moodAndGenresArtworkCache[cacheKey]
    val artworkUrl by produceState(initialValue = cachedArtwork, key1 = cacheKey) {
        if (!value.isNullOrBlank()) return@produceState

        val resolvedArtwork = withContext(Dispatchers.IO) {
            YouTube.browse(endpoint.browseId, endpoint.params).getOrNull()?.thumbnail
        }

        if (!resolvedArtwork.isNullOrBlank()) {
            moodAndGenresArtworkCache[cacheKey] = resolvedArtwork
            value = resolvedArtwork
        }
    }

    return artworkUrl
}

@Composable
private fun rememberMoodAndGenresArtworkModel(
    endpoint: BrowseEndpoint?,
    artworkUrl: String?,
): ImageRequest? {
    if (artworkUrl.isNullOrBlank()) return null

    val context = LocalContext.current
    val requestSizePx = with(LocalDensity.current) { MoodAndGenresArtworkRequestSize.roundToPx() }
    val cacheKey = remember(endpoint, artworkUrl) {
        endpoint?.let(::buildMoodAndGenresArtworkCacheKey) ?: artworkUrl
    }

    return remember(context, artworkUrl, cacheKey, requestSizePx) {
        ImageRequest.Builder(context)
            .data(artworkUrl)
            .memoryCacheKey("mood_and_genres:$cacheKey")
            .diskCacheKey("mood_and_genres:$cacheKey")
            .diskCachePolicy(CachePolicy.ENABLED)
            .size(requestSizePx)
            .build()
    }
}

private fun buildMoodAndGenresArtworkCacheKey(endpoint: BrowseEndpoint): String =
    "${endpoint.browseId}:${endpoint.params.orEmpty()}"

private val moodAndGenresArtworkCache = ConcurrentHashMap<String, String>()

private val MoodAndGenresButtonShape = RoundedCornerShape(24.dp)
private val MoodAndGenresCoverShape = RoundedCornerShape(18.dp)
private val MoodAndGenresArtworkRequestSize = 90.dp

val MoodAndGenresButtonHeight = 100.dp
