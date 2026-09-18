package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.TrackEntity
import com.example.ui.FilterChip as UiFilterChip
import com.example.ui.components.TrackItem
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import java.util.Calendar

@Composable
fun HomeScreen(
    tracks: List<TrackEntity>,
    recentlyPlayedTracks: List<TrackEntity>,
    mostPlayedTracks: List<TrackEntity>,
    currentTrack: TrackEntity?,
    isPlaying: Boolean,
    downloadProgress: Map<String, Int>,
    activeFilter: UiFilterChip,
    onFilterSelect: (UiFilterChip) -> Unit,
    onTrackClick: (TrackEntity) -> Unit,
    onToggleFavorite: (TrackEntity) -> Unit,
    onDownloadClick: (TrackEntity) -> Unit,
    onAddToPlaylistClick: (TrackEntity) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToGrabber: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = getGreeting()

    val filteredTracks = when (activeFilter) {
        UiFilterChip.ALL -> tracks
        UiFilterChip.DOWNLOADED -> tracks.filter { it.isDownloaded }
        UiFilterChip.FAVORITES -> tracks.filter { it.isFavorite }
        UiFilterChip.SPOTIFY -> tracks.filter { it.source == "SPOTIFY" }
        UiFilterChip.YOUTUBE -> tracks.filter { it.source == "YOUTUBE_STUDIO" }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Bar & Greeting
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Spotify & YouTube Studio Music Hub",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .testTag("home_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onNavigateToGrabber,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SpotifyGreen.copy(alpha = 0.15f))
                            .testTag("home_quick_grab_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddLink,
                            contentDescription = "Grab Music",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Filter Chips Row
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    MusicFilterChip(
                        label = "All",
                        selected = activeFilter == UiFilterChip.ALL,
                        onClick = { onFilterSelect(UiFilterChip.ALL) },
                        testTag = "filter_chip_all"
                    )
                }
                item {
                    MusicFilterChip(
                        label = "Downloaded",
                        selected = activeFilter == UiFilterChip.DOWNLOADED,
                        onClick = { onFilterSelect(UiFilterChip.DOWNLOADED) },
                        testTag = "filter_chip_downloaded"
                    )
                }
                item {
                    MusicFilterChip(
                        label = "Favorites",
                        selected = activeFilter == UiFilterChip.FAVORITES,
                        onClick = { onFilterSelect(UiFilterChip.FAVORITES) },
                        testTag = "filter_chip_favorites"
                    )
                }
                item {
                    MusicFilterChip(
                        label = "Spotify",
                        selected = activeFilter == UiFilterChip.SPOTIFY,
                        onClick = { onFilterSelect(UiFilterChip.SPOTIFY) },
                        testTag = "filter_chip_spotify"
                    )
                }
                item {
                    MusicFilterChip(
                        label = "YouTube Studio",
                        selected = activeFilter == UiFilterChip.YOUTUBE,
                        onClick = { onFilterSelect(UiFilterChip.YOUTUBE) },
                        testTag = "filter_chip_youtube"
                    )
                }
            }
        }

        // Hero Featured Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        tracks.firstOrNull()?.let { onTrackClick(it) }
                    }
                    .testTag("hero_featured_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_music_hero),
                        contentDescription = "Featured Music",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    // Hero Content
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FEATURED STUDIO MIX",
                                color = SpotifyGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Sonic Waves & High Fidelity",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${tracks.size} tracks • 320 kbps Studio Quality Audio",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Featured",
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Recently Played Horizontal Strip
        if (recentlyPlayedTracks.isNotEmpty() && activeFilter == UiFilterChip.ALL) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Recent",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Recently Played",
                            color = TextPrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "See all",
                        color = SpotifyGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToSearch() }
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentlyPlayedTracks.take(6)) { track ->
                        AlbumCard(
                            track = track,
                            onTrackClick = { onTrackClick(track) }
                        )
                    }
                }
            }
        }

        // Section: Most Played Hits (Horizontal Tiles)
        if (mostPlayedTracks.isNotEmpty() && activeFilter == UiFilterChip.ALL) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Hot",
                            tint = YouTubeRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Most Played Hits",
                            color = TextPrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Top streams",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mostPlayedTracks.take(6)) { track ->
                        AlbumCard(
                            track = track,
                            onTrackClick = { onTrackClick(track) }
                        )
                    }
                }
            }
        }

        // Section Title: Tracks List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (activeFilter) {
                        UiFilterChip.ALL -> "All Tracks"
                        UiFilterChip.DOWNLOADED -> "Offline Downloaded Tracks"
                        UiFilterChip.FAVORITES -> "Favorite Tracks"
                        UiFilterChip.SPOTIFY -> "Spotify Tracks"
                        UiFilterChip.YOUTUBE -> "YouTube Studio Tracks"
                    },
                    color = TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${filteredTracks.size} songs",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Track items
        if (filteredTracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "No tracks",
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (activeFilter == UiFilterChip.DOWNLOADED) "No downloaded music yet" else "No songs found",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (activeFilter == UiFilterChip.DOWNLOADED) {
                                "Tap the download icon on any song or grab music to listen offline!"
                            } else {
                                "Grab songs directly from Spotify or YouTube Studio links"
                            },
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(filteredTracks, key = { it.id }) { track ->
                TrackItem(
                    track = track,
                    isPlaying = isPlaying && currentTrack?.id == track.id,
                    isCurrent = currentTrack?.id == track.id,
                    downloadProgress = downloadProgress[track.id],
                    onTrackClick = { onTrackClick(track) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    onDownloadClick = { onDownloadClick(track) },
                    onAddToPlaylist = { onAddToPlaylistClick(track) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AlbumCard(
    track: TrackEntity,
    onTrackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(132.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onTrackClick() }
            .padding(4.dp)
            .testTag("album_card_${track.id}")
    ) {
        Box(
            modifier = Modifier
                .size(124.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = track.coverUrl,
                contentDescription = track.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = track.title,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = track.artist,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MusicFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SpotifyGreen,
            selectedLabelColor = Color.Black,
            containerColor = DarkSurfaceElevated,
            labelColor = TextPrimary
        ),
        border = null,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag(testTag)
    )
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
}
