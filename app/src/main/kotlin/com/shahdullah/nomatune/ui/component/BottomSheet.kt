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

package com.shahdullah.nomatune.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import com.shahdullah.nomatune.LocalAnimationsDisabled
import com.shahdullah.nomatune.constants.BottomSheetAnimationSpec
import com.shahdullah.nomatune.constants.BottomSheetSoftAnimationSpec
import com.shahdullah.nomatune.utils.rememberPreference

/**
 * Bottom Sheet
 * Modified from [ViMusic](https://github.com/vfsfitvnm/ViMusic)
 */
@Composable
fun BottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onDismiss: (() -> Unit)? = null,
    collapsedContent: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        val sheetOffset = (state.expandedBound - state.value).coerceAtLeast(0.dp)

        if (state.halfExpandedBound != null && state.value > state.collapsedBound + 1.dp && sheetOffset > 0.dp) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetOffset)
                    .align(Alignment.TopCenter)
                    .bottomSheetDraggable(state, onDismiss)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = state::collapseSoft,
                    )
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .offset {
                        val y = sheetOffset.roundToPx().coerceAtLeast(0)
                        IntOffset(x = 0, y = y)
                    }.bottomSheetDraggable(state, onDismiss)
                    .clip(
                        RoundedCornerShape(
                            topStart = if (!state.isExpanded) 20.dp else 0.dp,
                            topEnd = if (!state.isExpanded) 20.dp else 0.dp,
                        ),
                    ).background(
                        if (backgroundColor != Color.Unspecified) {
                            backgroundColor.copy(
                                alpha = backgroundColor.alpha * state.progress.coerceIn(0f, 1f)
                            )
                        } else Color.Transparent,
                    ),
        ) {
            if (state.isExpandedOrExpanding) {
                BackHandler(
                    onBack = {
                        if (state.isExpanded && state.halfExpandedBound != null) {
                            state.halfExpandSoft()
                        } else {
                            state.collapseSoft()
                        }
                    }
                )
            }

            if (!state.isCollapsed) {
                BoxWithConstraints(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = if (state.halfExpandedBound != null) {
                                    if (state.isCollapsed) 0f else ((state.value - state.collapsedBound) / ((state.halfExpandedBound ?: state.expandedBound) - state.collapsedBound)).coerceIn(0f, 1f)
                                } else {
                                    ((state.progress - 0.25f) * 4).coerceIn(0f, 1f)
                                }
                            },
                    content = content,
                )
            }

            if (!state.isExpanded && (onDismiss == null || !state.isDismissed)) {
                Box(
                    modifier =
                        Modifier
                            .graphicsLayer {
                                alpha = 1f - (state.progress * 4).coerceAtMost(1f)
                            }.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = if (state.halfExpandedBound != null) state::halfExpandSoft else state::expandSoft,
                            ).fillMaxWidth()
                            .height(state.collapsedBound),
                    content = collapsedContent,
                )
            }
        }
    }
}

@Stable
class BottomSheetState(
    draggableState: DraggableState,
    private val coroutineScope: CoroutineScope,
    private val animatable: Animatable<Dp, AnimationVector1D>,
    private val onAnchorChanged: (Int) -> Unit,
    private val animationsDisabled: Boolean,
    val collapsedBound: Dp,
    val halfExpandedBound: Dp? = null,
    val density: Density = Density(1f),
    initialAnchor: Int = DISMISSED_ANCHOR,
) : DraggableState by draggableState {
    val dismissedBound: Dp
        get() = animatable.lowerBound!!

    val expandedBound: Dp
        get() = animatable.upperBound!!

    val value by animatable.asState()

    var targetAnchor by mutableIntStateOf(initialAnchor)
        private set

    val isDismissed by derivedStateOf {
        value == animatable.lowerBound!!
    }

    val isCollapsed by derivedStateOf {
        value == collapsedBound
    }

    val isHalfExpanded by derivedStateOf {
        halfExpandedBound != null && value == halfExpandedBound
    }

    val isExpanded by derivedStateOf {
        value == animatable.upperBound
    }

    val isExpandedOrExpanding: Boolean
        get() = targetAnchor == EXPANDED_ANCHOR || targetAnchor == HALF_EXPANDED_ANCHOR

    val isHalfExpandedOrExpanding: Boolean
        get() = targetAnchor == HALF_EXPANDED_ANCHOR

    val progress by derivedStateOf {
        1f - (animatable.upperBound!! - animatable.value) / (animatable.upperBound!! - collapsedBound)
    }

    private fun updateAnchor(anchor: Int) {
        targetAnchor = anchor
        onAnchorChanged(anchor)
    }

    fun collapse(animationSpec: AnimationSpec<Dp>) {
        updateAnchor(COLLAPSED_ANCHOR)
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            animatable.animateTo(collapsedBound, animationSpec)
        }
    }

    fun halfExpand(animationSpec: AnimationSpec<Dp>) {
        val target = halfExpandedBound ?: animatable.upperBound!!
        updateAnchor(if (halfExpandedBound != null) HALF_EXPANDED_ANCHOR else EXPANDED_ANCHOR)
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            animatable.animateTo(target, animationSpec)
        }
    }

    fun expand(animationSpec: AnimationSpec<Dp>) {
        updateAnchor(EXPANDED_ANCHOR)
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            animatable.animateTo(animatable.upperBound!!, animationSpec)
        }
    }

    private fun collapse() {
        collapse(if (animationsDisabled) snap() else BottomSheetAnimationSpec)
    }

    fun halfExpand() {
        halfExpand(if (animationsDisabled) snap() else BottomSheetAnimationSpec)
    }

    private fun expand() {
        expand(if (animationsDisabled) snap() else BottomSheetAnimationSpec)
    }

    fun collapseSoft() {
        collapse(if (animationsDisabled) snap() else BottomSheetSoftAnimationSpec)
    }

    fun halfExpandSoft() {
        halfExpand(if (animationsDisabled) snap() else BottomSheetSoftAnimationSpec)
    }

    fun expandSoft() {
        expand(if (animationsDisabled) snap() else BottomSheetSoftAnimationSpec)
    }

    fun dismiss() {
        updateAnchor(DISMISSED_ANCHOR)
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            animatable.animateTo(animatable.lowerBound!!, if (animationsDisabled) snap() else BottomSheetAnimationSpec)
        }
    }

    fun snapTo(value: Dp) {
        updateAnchor(
            when (value) {
                expandedBound -> EXPANDED_ANCHOR
                halfExpandedBound -> HALF_EXPANDED_ANCHOR
                collapsedBound -> COLLAPSED_ANCHOR
                dismissedBound -> DISMISSED_ANCHOR
                else -> COLLAPSED_ANCHOR
            },
        )
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            animatable.snapTo(value)
        }
    }

    fun performFling(
        velocity: Float,
        onDismiss: (() -> Unit)?,
    ) {
        val half = halfExpandedBound
        if (half != null) {
            if (velocity > 250) {
                if (value >= half - 30.dp) {
                    expand()
                } else {
                    halfExpand()
                }
            } else if (velocity < -250) {
                if (value > half + 30.dp) {
                    halfExpand()
                } else if (value < collapsedBound && onDismiss != null) {
                    dismiss()
                    onDismiss.invoke()
                } else {
                    collapse()
                }
            } else {
                val mid1 = (collapsedBound + half) / 2
                val mid2 = (half + expandedBound) / 2
                val midDismiss = (dismissedBound + collapsedBound) / 2

                when {
                    onDismiss != null && value < midDismiss -> {
                        dismiss()
                        onDismiss.invoke()
                    }
                    value < mid1 -> collapse()
                    value < mid2 -> halfExpand()
                    else -> expand()
                }
            }
        } else {
            if (velocity > 250) {
                expand()
            } else if (velocity < -250) {
                if (value < collapsedBound && onDismiss != null) {
                    dismiss()
                    onDismiss.invoke()
                } else {
                    collapse()
                }
            } else {
                val l0 = dismissedBound
                val l1 = (collapsedBound - dismissedBound) / 2
                val l2 = (expandedBound - collapsedBound) / 2
                val l3 = expandedBound

                when (value) {
                    in l0..l1 -> {
                        if (onDismiss != null) {
                            dismiss()
                            onDismiss.invoke()
                        } else {
                            collapse()
                        }
                    }

                    in l1..l2 -> {
                        collapse()
                    }

                    in l2..l3 -> {
                        expand()
                    }

                    else -> {
                        Unit
                    }
                }
            }
        }
    }

    val preUpPostDownNestedScrollConnection
        get() =
            object : NestedScrollConnection {
                var isTopReached = false

                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (halfExpandedBound != null && value < expandedBound && available.y < 0 && source == NestedScrollSource.UserInput) {
                        val prevValue = value
                        dispatchRawDelta(available.y)
                        val consumedY = with(density) { -(value - prevValue).toPx() }
                        return Offset(0f, consumedY)
                    }

                    if (isExpanded && available.y < 0) {
                        isTopReached = false
                    }

                    return if (isTopReached && available.y < 0 && source == NestedScrollSource.UserInput) {
                        dispatchRawDelta(available.y)
                        available
                    } else {
                        Offset.Zero
                    }
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (!isTopReached) {
                        isTopReached = consumed.y == 0f && available.y > 0
                    }

                    return if (isTopReached && source == NestedScrollSource.UserInput) {
                        dispatchRawDelta(available.y)
                        available
                    } else {
                        Offset.Zero
                    }
                }

                override suspend fun onPreFling(available: Velocity): Velocity =
                    if (halfExpandedBound != null && value < expandedBound && available.y < 0) {
                        val velocity = -available.y
                        performFling(velocity, null)
                        available
                    } else if (isTopReached) {
                        val velocity = -available.y
                        performFling(velocity, null)
                        available
                    } else {
                        Velocity.Zero
                    }

                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity,
                ): Velocity {
                    isTopReached = false
                    return Velocity.Zero
                }
            }
}

const val HALF_EXPANDED_ANCHOR = 3
const val EXPANDED_ANCHOR = 2
const val COLLAPSED_ANCHOR = 1
const val DISMISSED_ANCHOR = 0

@Composable
fun rememberBottomSheetState(
    dismissedBound: Dp,
    expandedBound: Dp,
    collapsedBound: Dp = dismissedBound,
    halfExpandedBound: Dp? = null,
    initialAnchor: Int = DISMISSED_ANCHOR,
): BottomSheetState {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val animationsDisabled = LocalAnimationsDisabled.current

    var previousAnchor by rememberSaveable {
        mutableIntStateOf(initialAnchor)
    }
    val animatable =
        remember {
            Animatable(0.dp, Dp.VectorConverter)
        }

    return remember(dismissedBound, expandedBound, collapsedBound, halfExpandedBound, coroutineScope, animationsDisabled) {
        val initialValue =
            when (previousAnchor) {
                EXPANDED_ANCHOR -> expandedBound
                HALF_EXPANDED_ANCHOR -> halfExpandedBound ?: expandedBound
                COLLAPSED_ANCHOR -> collapsedBound
                DISMISSED_ANCHOR -> dismissedBound
                else -> error("Unknown BottomSheet anchor")
            }

        animatable.updateBounds(dismissedBound.coerceAtMost(expandedBound), expandedBound)
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            animatable.animateTo(initialValue, if (animationsDisabled) snap() else BottomSheetAnimationSpec)
        }

        BottomSheetState(
            draggableState =
                DraggableState { delta ->
                    coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                        animatable.snapTo(animatable.value - with(density) { delta.toDp() })
                    }
                },
            onAnchorChanged = { previousAnchor = it },
            coroutineScope = coroutineScope,
            animatable = animatable,
            animationsDisabled = animationsDisabled,
            collapsedBound = collapsedBound,
            halfExpandedBound = halfExpandedBound,
            density = density,
            initialAnchor = previousAnchor,
        )
    }
}

@Composable
fun Modifier.bottomSheetDraggable(
    state: BottomSheetState,
    onDismiss: (() -> Unit)? = null,
): Modifier =
    this.pointerInput(state) {
        val velocityTracker = VelocityTracker()

        detectVerticalDragGestures(
            onVerticalDrag = { change, dragAmount ->
                velocityTracker.addPointerInputChange(change)
                state.dispatchRawDelta(dragAmount)
            },
            onDragCancel = {
                velocityTracker.resetTracking()
                state.snapTo(state.collapsedBound)
            },
            onDragEnd = {
                val velocity = -velocityTracker.calculateVelocity().y
                velocityTracker.resetTracking()
                state.performFling(velocity, onDismiss)
            },
        )
    }
