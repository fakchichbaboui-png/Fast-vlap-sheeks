package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PlaylistEntity
import com.example.data.model.TrackEntity
import com.example.ui.components.TrackItem
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.DownloadBadge
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun LibraryScreen(
    allTracks: List<TrackEntity>,
    downloadedTracks: List<TrackEntity>,
    favoriteTracks: List<TrackEntity>,
    playlists: List<PlaylistEntity>,
    currentTrack: TrackEntity?,
    isPlaying: Boolean,
    downloadProgress: Map<String, Int>,
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
    onTrackClick: (TrackEntity) -> Unit,
    onPlaylistClick: (PlaylistEntity) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onToggleFavorite: (TrackEntity) -> Unit,
    onDownloadClick: (TrackEntity) -> Unit,
    onAddToPlaylistClick: (TrackEntity) -> Unit,
    onNavigateToGrabber: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDownloadedBytes = downloadedTracks.sumOf { it.fileSizeBytes }
    val storageText = if (totalDownloadedBytes > 0) {
        String.format(Locale.getDefault(), "%.1f MB", totalDownloadedBytes / (1024f * 1024f))
    } else {
        "${downloadedTracks.size * 3.5} MB (est.)"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Title Row with Quick Action
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Library",
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                if (selectedTab == 1) {
                    // Quick new playlist button
                    Button(
                        onClick = onCreatePlaylistClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpotifyGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("lib_create_playlist_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "New Playlist", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Playlist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (downloadedTracks.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SpotifyGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Offline ready",
                                tint = DownloadBadge,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${downloadedTracks.size} Offline Ready",
                                color = SpotifyGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Tabs: Downloaded (0), Playlists (1), Favorites (2), All Grabbed (3)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = SpotifyGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SpotifyGreen,
                        height = 3.dp
                    )
                },
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DarkSurfaceHigh)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onTabSelect(0) },
                    text = {
                        Text(
                            text = "Downloaded (${downloadedTracks.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) SpotifyGreen else TextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_downloaded")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { onTabSelect(1) },
                    text = {
                        Text(
                            text = "Playlists (${playlists.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) SpotifyGreen else TextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_playlists")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { onTabSelect(2) },
                    text = {
                        Text(
                            text = "Favorites (${favoriteTracks.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) SpotifyGreen else TextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_favorites")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { onTabSelect(3) },
                    text = {
                        Text(
                            text = "All (${allTracks.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 3) SpotifyGreen else TextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_all")
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Tab 0: Downloaded Offline info banner
        if (selectedTab == 0 && downloadedTracks.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DownloadDone,
                                contentDescription = "Storage",
                                tint = SpotifyGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Offline Storage Active",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${downloadedTracks.size} songs saved on device • $storageText space used",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Tab Content
        when (selectedTab) {
            1 -> {
                // Playlists Tab
                if (playlists.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 50.dp, horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = "No Playlists",
                                    tint = TextMuted,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Create your first playlist",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Organize Spotify and YouTube Studio tracks into custom playlists.",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onCreatePlaylistClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SpotifyGreen,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text("Create Playlist", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(playlists, key = { it.id }) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlaylistClick(playlist) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("playlist_item_${playlist.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                if (playlist.coverUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = playlist.coverUrl,
                                        contentDescription = playlist.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = SpotifyGreen,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.name,
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Playlist • ${playlist.trackCount} songs",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                // Tracks Tab (0: Downloaded, 2: Favorites, 3: All)
                val tracks = when (selectedTab) {
                    0 -> downloadedTracks
                    2 -> favoriteTracks
                    else -> allTracks
                }

                if (tracks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 50.dp, horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = when (selectedTab) {
                                        0 -> Icons.Default.CloudDownload
                                        2 -> Icons.Default.Favorite
                                        else -> Icons.Default.QueueMusic
                                    },
                                    contentDescription = "Empty",
                                    tint = TextMuted,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = when (selectedTab) {
                                        0 -> "No downloaded songs yet"
                                        2 -> "No favorite songs yet"
                                        else -> "Your library is empty"
                                    },
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = when (selectedTab) {
                                        0 -> "Tap the download icon on any Spotify or YouTube Studio song to save it offline!"
                                        2 -> "Heart songs you love to access them instantly here."
                                        else -> "Grab music from Spotify or YouTube Studio to fill your library."
                                    },
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = onNavigateToGrabber,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SpotifyGreen,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text("Grab Music Now", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(tracks, key = { it.id }) { track ->
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
    }
}
