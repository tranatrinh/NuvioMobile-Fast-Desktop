package com.nuvio.app.features.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Toc
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.AppIconResource
import com.nuvio.app.core.ui.appIconPainter
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.core.ui.nuvioTypeScale
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

internal object DesktopPlayerUiBridge {
    var volume by mutableFloatStateOf(1f)
    var isMuted by mutableStateOf(false)
    var isFullscreenProvider: (() -> Boolean)? = null
    var onToggleFullscreen: (() -> Unit)? = null
    var onVolumeChanged: ((Float, Boolean) -> Unit)? = null
}

@Composable
internal fun PlayerToolbar(
    isLocked: Boolean,
    onLockToggle: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onPartyClick: (() -> Unit)? = null,
    partyActive: Boolean = false,
    onSwitchEngineClick: (() -> Unit)? = null,
) {
    val isDesktopFs = DesktopPlayerUiBridge.isFullscreenProvider?.invoke() == true
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Spacer(Modifier.weight(1f))
        if (onSwitchEngineClick != null) {
            PlayerAction(
                description = stringResource(Res.string.cd_switch_player_engine),
                icon = Icons.Rounded.SwapHoriz,
                onClick = onSwitchEngineClick,
            )
        }
        if (onPartyClick != null) {
            PlayerAction(
                description = stringResource(Res.string.party_title),
                icon = Icons.Rounded.Groups,
                onClick = onPartyClick,
                tint = if (partyActive) MaterialTheme.colorScheme.primary else Color.White,
            )
        }
        PlayerAction(
            description = stringResource(
                if (isLocked) Res.string.compose_player_unlock_controls else Res.string.compose_player_lock_controls,
            ),
            icon = if (isLocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
            onClick = onLockToggle,
        )
        if (DesktopPlayerUiBridge.onToggleFullscreen != null) {
            PlayerAction(
                description = if (isDesktopFs) "Thu nhỏ" else "Toàn màn hình",
                icon = if (isDesktopFs) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                iconSize = 26.dp,
                onClick = { DesktopPlayerUiBridge.onToggleFullscreen?.invoke() },
            )
        }
        PlayerAction(
            description = stringResource(Res.string.compose_player_close),
            icon = Icons.Rounded.Close,
            iconSize = 26.dp,
            onClick = onBack,
        )
    }
}

@Composable
internal fun PlayerControlActions(
    playbackSnapshot: PlayerPlaybackSnapshot,
    displayedPositionMs: Long,
    showRemainingTime: Boolean,
    onRuntimeClick: () -> Unit,
    metrics: PlayerLayoutMetrics,
    resizeMode: PlayerResizeMode,
    onSubtitleClick: () -> Unit,
    onAudioClick: () -> Unit,
    onTtsClick: () -> Unit = {},
    isTtsEnabled: Boolean = false,
    onSourcesClick: (() -> Unit)?,
    onEpisodesClick: (() -> Unit)?,
    onLiveChannelsClick: (() -> Unit)? = null,
    onChaptersClick: (() -> Unit)?,
    onNextEpisodeClick: (() -> Unit)?,
    onSpeedClick: () -> Unit,
    onResizeModeClick: () -> Unit,
    isPortraitOrientation: Boolean = false,
    onToggleOrientationClick: (() -> Unit)? = null,
    onVideoSettingsClick: (() -> Unit)?,
    onOpenInExternalPlayer: (() -> Unit)?,
    onStreamInfoClick: () -> Unit,
    onSubmitIntroClick: (() -> Unit)?,
    onInteraction: () -> Unit,
    onTogglePlayback: (() -> Unit)? = null,
) {
    val actions = playerControlActions(
        playbackSnapshot = playbackSnapshot,
        resizeMode = resizeMode,
        onSubtitleClick = onSubtitleClick,
        onAudioClick = onAudioClick,
        onTtsClick = onTtsClick,
        isTtsEnabled = isTtsEnabled,
        onSourcesClick = onSourcesClick,
        onEpisodesClick = onEpisodesClick,
        onLiveChannelsClick = onLiveChannelsClick,
        onChaptersClick = onChaptersClick,
        onNextEpisodeClick = onNextEpisodeClick,
        onSpeedClick = onSpeedClick,
        onResizeModeClick = onResizeModeClick,
        isPortraitOrientation = isPortraitOrientation,
        onToggleOrientationClick = onToggleOrientationClick,
        onVideoSettingsClick = onVideoSettingsClick,
        onOpenInExternalPlayer = onOpenInExternalPlayer,
        onStreamInfoClick = onStreamInfoClick,
        onSubmitIntroClick = onSubmitIntroClick,
    )
    val scrollState = rememberScrollState()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = PlayerTimelineContentInset),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onTogglePlayback != null) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            onInteraction()
                            onTogglePlayback()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (playbackSnapshot.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playbackSnapshot.isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.weight(1f).horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions.forEach { action ->
                    PlayerAction(
                        description = action.description,
                        onClick = {
                            onInteraction()
                            action.onClick()
                        },
                        icon = action.icon,
                        painter = action.painter,
                        iconSize = action.iconSize,
                        tint = action.tint,
                    )
                }
            }

            if (DesktopPlayerUiBridge.onVolumeChanged != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            onInteraction()
                            DesktopPlayerUiBridge.isMuted = !DesktopPlayerUiBridge.isMuted
                            DesktopPlayerUiBridge.onVolumeChanged?.invoke(
                                DesktopPlayerUiBridge.volume,
                                DesktopPlayerUiBridge.isMuted
                            )
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (DesktopPlayerUiBridge.isMuted || DesktopPlayerUiBridge.volume <= 0.01f)
                                Icons.AutoMirrored.Rounded.VolumeOff
                            else
                                Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Volume",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    val currentVol = if (DesktopPlayerUiBridge.isMuted) 0f else DesktopPlayerUiBridge.volume.coerceIn(0f, 1f)
                    val trackWidth = 84.dp
                    val thumbSize = 12.dp
                    Box(
                        modifier = Modifier
                            .width(trackWidth)
                            .height(28.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val newVal = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    onInteraction()
                                    DesktopPlayerUiBridge.volume = newVal
                                    DesktopPlayerUiBridge.isMuted = newVal <= 0.01f
                                    DesktopPlayerUiBridge.onVolumeChanged?.invoke(newVal, DesktopPlayerUiBridge.isMuted)
                                }
                            }
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { change, _ ->
                                    change.consume()
                                    val newVal = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    onInteraction()
                                    DesktopPlayerUiBridge.volume = newVal
                                    DesktopPlayerUiBridge.isMuted = newVal <= 0.01f
                                    DesktopPlayerUiBridge.onVolumeChanged?.invoke(newVal, DesktopPlayerUiBridge.isMuted)
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Duong ranh nen mo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.28f))
                        )
                        // Phan ranh am luong mau trang
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(currentVol)
                                .height(3.5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White)
                        )
                        // Cham tron trang
                        Box(
                            modifier = Modifier
                                .offset(x = (trackWidth - thumbSize) * currentVol)
                                .size(thumbSize)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.height(48.dp).widthIn(min = 48.dp).clickable(
                    role = Role.Button,
                    onClickLabel = stringResource(
                        if (showRemainingTime) Res.string.compose_player_show_elapsed_time else Res.string.compose_player_show_remaining_time,
                    ),
                    onClick = {
                        onRuntimeClick()
                        onInteraction()
                    },
                ),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = formatPlaybackRuntime(displayedPositionMs, playbackSnapshot.durationMs, showRemainingTime),
                    style = MaterialTheme.nuvioTypeScale.bodyMd.copy(fontSize = (metrics.timeSize.value + 2).sp),
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun PlayerAction(
    description: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    painter: Painter? = null,
    iconSize: Dp = 24.dp,
    tint: Color = Color.White,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        if (painter != null) {
            Icon(painter, description, tint = tint, modifier = Modifier.size(iconSize))
        } else if (icon != null) {
            Icon(icon, description, tint = tint, modifier = Modifier.size(iconSize))
        }
    }
}

@Composable
internal fun playerControlActions(
    playbackSnapshot: PlayerPlaybackSnapshot,
    resizeMode: PlayerResizeMode,
    onSubtitleClick: () -> Unit,
    onAudioClick: () -> Unit,
    onTtsClick: () -> Unit = {},
    isTtsEnabled: Boolean = false,
    onSourcesClick: (() -> Unit)?,
    onEpisodesClick: (() -> Unit)?,
    onLiveChannelsClick: (() -> Unit)? = null,
    onChaptersClick: (() -> Unit)?,
    onNextEpisodeClick: (() -> Unit)?,
    onSpeedClick: () -> Unit,
    onResizeModeClick: () -> Unit,
    isPortraitOrientation: Boolean = false,
    onToggleOrientationClick: (() -> Unit)? = null,
    onVideoSettingsClick: (() -> Unit)?,
    onOpenInExternalPlayer: (() -> Unit)?,
    onStreamInfoClick: () -> Unit,
    onSubmitIntroClick: (() -> Unit)?,
): List<PlayerControlAction> =
    listOfNotNull(
        onNextEpisodeClick?.let {
            PlayerControlAction(
                stringResource(Res.string.player_next_episode), it,
                icon = Icons.Rounded.SkipNext, iconSize = 36.dp,
            )
        },
        PlayerControlAction(
            stringResource(Res.string.compose_player_subtitles), onSubtitleClick,
            painter = appIconPainter(AppIconResource.PlayerSubtitles),
        ),
        PlayerControlAction(
            stringResource(Res.string.compose_player_audio), onAudioClick,
            painter = appIconPainter(AppIconResource.PlayerAudioFilled),
        ),
        PlayerControlAction(
            "Thuyết minh phụ đề (TTS)", onTtsClick,
            icon = Icons.Rounded.RecordVoiceOver,
            tint = if (isTtsEnabled) MaterialTheme.nuvio.colors.accent else Color.White,
        ),
        onChaptersClick?.let {
            PlayerControlAction(
                stringResource(Res.string.compose_player_chapters), it,
                icon = Icons.AutoMirrored.Rounded.Toc,
            )
        },
        onSourcesClick?.let {
            PlayerControlAction(
                stringResource(Res.string.compose_player_sources), it,
                painter = appIconPainter(AppIconResource.PlayerSource),
            )
        },
        onEpisodesClick?.let {
            PlayerControlAction(
                stringResource(Res.string.compose_player_episodes), it,
                painter = appIconPainter(AppIconResource.PlayerEpisodes),
            )
        },
        onLiveChannelsClick?.let {
            PlayerControlAction(
                stringResource(Res.string.compose_player_channels), it,
                icon = Icons.Rounded.Tv,
            )
        },
        PlayerControlAction(
            "${stringResource(Res.string.compose_player_speed)} ${formatPlaybackSpeedLabel(playbackSnapshot.playbackSpeed)}",
            onSpeedClick, icon = Icons.Rounded.Speed,
        ),
        PlayerControlAction(
            stringResource(resizeMode.labelRes), onResizeModeClick,
            painter = appIconPainter(AppIconResource.PlayerAspectRatio),
        ),
        onToggleOrientationClick?.let {
            PlayerControlAction(
                stringResource(
                    if (isPortraitOrientation) Res.string.compose_player_rotate_landscape else Res.string.compose_player_rotate_portrait,
                ),
                it,
                icon = Icons.Rounded.ScreenRotation,
            )
        },
        onOpenInExternalPlayer?.let {
            PlayerControlAction(
                stringResource(Res.string.streams_open_external_player), it,
                icon = Icons.AutoMirrored.Rounded.OpenInNew,
            )
        },
        PlayerControlAction(
            stringResource(Res.string.cd_stream_info), onStreamInfoClick,
            icon = Icons.Rounded.Info,
        ),
        onVideoSettingsClick?.let {
            PlayerControlAction(
                stringResource(Res.string.player_action_video_settings), it,
                icon = Icons.Rounded.Build,
            )
        },
        onSubmitIntroClick?.let {
            PlayerControlAction(
                stringResource(Res.string.submit_intro_action), it,
                icon = Icons.Rounded.Flag,
            )
        },
    )

internal data class PlayerControlAction(
    val description: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val painter: Painter? = null,
    val iconSize: Dp = 24.dp,
    val tint: Color = Color.White,
)

