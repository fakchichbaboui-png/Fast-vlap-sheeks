package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.MiniPlayer
import com.example.ui.components.NowPlayingModal
import com.example.ui.components.PlaylistDetailScreen
import com.example.ui.screens.GrabberScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MainMusicApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val downloadedTracks by viewModel.downloadedTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val recentlyPlayedTracks by viewModel.recentlyPlayedTracks.collectAsStateWithLifecycle()
    val mostPlayedTracks by viewModel.mostPlayedTracks.collectAsStateWithLifecycle()
    val allPlaylists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()

    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isBuffering by viewModel.isBuffering.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val isRepeat by viewModel.isRepeat.collectAsStateWithLifecycle()

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val libraryTab by viewModel.libraryTab.collectAsStateWithLifecycle()

    val grabInput by viewModel.grabInput.collectAsStateWithLifecycle()
    val isGrabbing by viewModel.isGrabbing.collectAsStateWithLifecycle()
    val grabSuccessTrack by viewModel.grabSuccessTrack.collectAsStateWithLifecycle()
    val grabErrorMessage by viewModel.grabErrorMessage.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val onlineSearchResults by viewModel.onlineSearchResults.collectAsStateWithLifecycle()
    val isLiveSearching by viewModel.isLiveSearching.collectAsStateWithLifecycle()

    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val selectedPlaylistTracks by viewModel.selectedPlaylistTracks.collectAsStateWithLifecycle()

    val trackToAddToPlaylist by viewModel.trackToAddToPlaylist.collectAsStateWithLifecycle()
    val similarTracks by viewModel.similarTracks.collectAsStateWithLifecycle()
    val isAutoplayEnabled by viewModel.isAutoplayEnabled.collectAsStateWithLifecycle()
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBackground,
            contentWindowInsets = WindowInsets.statusBars,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    DarkBackground.copy(alpha = 0.95f),
                                    DarkBackground
                                )
                            )
                        )
                ) {
                    // Persistent Mini Player above Bottom Bar
                    if (currentTrack != null && selectedPlaylist == null) {
                        MiniPlayer(
                            track = currentTrack,
                            isPlaying = isPlaying,
                            isBuffering = isBuffering,
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onSkipNext = { viewModel.skipNext() },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onExpand = { viewModel.setNowPlayingExpanded(true) }
                        )
                    }

                    // Navigation Bar (Home, Search, Grab Music, Library)
                    if (selectedPlaylist == null) {
                        NavigationBar(
                            containerColor = DarkSurface,
                            contentColor = TextPrimary,
                            windowInsets = WindowInsets.navigationBars,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == Screen.HOME,
                                onClick = { viewModel.navigateTo(Screen.HOME) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == Screen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                        contentDescription = "Home",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Home",
                                        fontSize = 11.sp,
                                        fontWeight = if (currentScreen == Screen.HOME) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SpotifyGreen,
                                    selectedTextColor = SpotifyGreen,
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_item_home")
                            )

                            NavigationBarItem(
                                selected = currentScreen == Screen.SEARCH,
                                onClick = { viewModel.navigateTo(Screen.SEARCH) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == Screen.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                                        contentDescription = "Search",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Search",
                                        fontSize = 11.sp,
                                        fontWeight = if (currentScreen == Screen.SEARCH) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SpotifyGreen,
                                    selectedTextColor = SpotifyGreen,
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_item_search")
                            )

                            NavigationBarItem(
                                selected = currentScreen == Screen.GRABBER,
                                onClick = { viewModel.navigateTo(Screen.GRABBER) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == Screen.GRABBER) Icons.Filled.AddLink else Icons.Outlined.AddLink,
                                        contentDescription = "Grab Music",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Grab Music",
                                        fontSize = 11.sp,
                                        fontWeight = if (currentScreen == Screen.GRABBER) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SpotifyGreen,
                                    selectedTextColor = SpotifyGreen,
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_item_grabber")
                            )

                            NavigationBarItem(
                                selected = currentScreen == Screen.LIBRARY,
                                onClick = { viewModel.navigateTo(Screen.LIBRARY) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == Screen.LIBRARY) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                                        contentDescription = "Your Library",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Your Library",
                                        fontSize = 11.sp,
                                        fontWeight = if (currentScreen == Screen.LIBRARY) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SpotifyGreen,
                                    selectedTextColor = SpotifyGreen,
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_item_library")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // If a playlist is opened, display the PlaylistDetailScreen
                if (selectedPlaylist != null) {
                    val activePlaylist = selectedPlaylist!!
                    PlaylistDetailScreen(
                        playlist = activePlaylist,
                        tracks = selectedPlaylistTracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        downloadProgress = downloadProgress,
                        onBackClick = { viewModel.closePlaylist() },
                        onTrackClick = { viewModel.playTrack(it, selectedPlaylistTracks) },
                        onPlayAll = {
                            selectedPlaylistTracks.firstOrNull()?.let {
                                viewModel.playTrack(it, selectedPlaylistTracks)
                            }
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDownloadClick = { viewModel.downloadTrack(it) },
                        onDeletePlaylist = { viewModel.deletePlaylist(activePlaylist.id) },
                        onRemoveTrack = { trackId -> viewModel.removeTrackFromPlaylist(activePlaylist.id, trackId) }
                    )
                } else {
                    when (currentScreen) {
                        Screen.HOME -> HomeScreen(
                            tracks = allTracks,
                            recentlyPlayedTracks = recentlyPlayedTracks,
                            mostPlayedTracks = mostPlayedTracks,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            downloadProgress = downloadProgress,
                            activeFilter = activeFilter,
                            onFilterSelect = { viewModel.setActiveFilter(it) },
                            onTrackClick = { viewModel.playTrack(it, allTracks) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onDownloadClick = { viewModel.downloadTrack(it) },
                            onAddToPlaylistClick = { viewModel.showAddToPlaylistDialog(it) },
                            onNavigateToSearch = { viewModel.navigateTo(Screen.SEARCH) },
                            onNavigateToGrabber = { viewModel.navigateTo(Screen.GRABBER) }
                        )

                        Screen.SEARCH -> SearchScreen(
                            searchQuery = searchQuery,
                            searchResults = searchResults,
                            onlineSearchResults = onlineSearchResults,
                            isLiveSearching = isLiveSearching,
                            recentlyPlayedTracks = recentlyPlayedTracks,
                            mostPlayedTracks = mostPlayedTracks,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            downloadProgress = downloadProgress,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            onTrackClick = { track -> 
                                viewModel.playTrack(track, listOf(track)) 
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onDownloadClick = { viewModel.downloadTrack(it) },
                            onAddToPlaylistClick = { viewModel.showAddToPlaylistDialog(it) }
                        )

                        Screen.GRABBER -> GrabberScreen(
                            grabInput = grabInput,
                            isGrabbing = isGrabbing,
                            grabSuccessTrack = grabSuccessTrack,
                            grabErrorMessage = grabErrorMessage,
                            downloadProgress = downloadProgress,
                            onInputChange = { viewModel.updateGrabInput(it) },
                            onGrabClick = { viewModel.grabMusic() },
                            onPlayTrack = { viewModel.playTrack(it, allTracks) },
                            onDownloadTrack = { viewModel.downloadTrack(it) }
                        )

                        Screen.LIBRARY -> LibraryScreen(
                            allTracks = allTracks,
                            downloadedTracks = downloadedTracks,
                            favoriteTracks = favoriteTracks,
                            playlists = allPlaylists,
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            downloadProgress = downloadProgress,
                            selectedTab = libraryTab,
                            onTabSelect = { viewModel.setLibraryTab(it) },
                            onTrackClick = {
                                val contextQueue = when (libraryTab) {
                                    0 -> downloadedTracks
                                    2 -> favoriteTracks
                                    else -> allTracks
                                }
                                viewModel.playTrack(it, contextQueue)
                            },
                            onPlaylistClick = { viewModel.openPlaylist(it) },
                            onCreatePlaylistClick = { showCreatePlaylistDialog = true },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onDownloadClick = { viewModel.downloadTrack(it) },
                            onAddToPlaylistClick = { viewModel.showAddToPlaylistDialog(it) },
                            onNavigateToGrabber = { viewModel.navigateTo(Screen.GRABBER) }
                        )
                    }
                }
            }
        }

        // Add to Playlist Dialog
        if (trackToAddToPlaylist != null) {
            AddToPlaylistDialog(
                track = trackToAddToPlaylist!!,
                playlists = allPlaylists,
                onDismiss = { viewModel.dismissAddToPlaylistDialog() },
                onAddToPlaylist = { playlistId, trackId ->
                    viewModel.addTrackToPlaylist(playlistId, trackId)
                },
                onCreateNewPlaylist = { name ->
                    viewModel.createPlaylist(name)
                }
            )
        }

        // Create Playlist Dialog
        if (showCreatePlaylistDialog) {
            var plName by remember { mutableStateOf("") }
            var plDesc by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { showCreatePlaylistDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "New Playlist",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = plName,
                            onValueChange = { plName = it },
                            placeholder = { Text("Playlist name", color = TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpotifyGreen,
                                unfocusedBorderColor = DarkSurfaceHigh,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurfaceCard,
                                unfocusedContainerColor = DarkSurfaceCard
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = plDesc,
                            onValueChange = { plDesc = it },
                            placeholder = { Text("Optional description", color = TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpotifyGreen,
                                unfocusedBorderColor = DarkSurfaceHigh,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurfaceCard,
                                unfocusedContainerColor = DarkSurfaceCard
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showCreatePlaylistDialog = false }) {
                                Text("Cancel", color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.size(8.dp))
                            Button(
                                onClick = {
                                    if (plName.isNotBlank()) {
                                        viewModel.createPlaylist(plName.trim(), plDesc.trim())
                                        showCreatePlaylistDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SpotifyGreen,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Create", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Full Screen Expandable Now Playing Modal
        NowPlayingModal(
            track = currentTrack,
            visible = isNowPlayingExpanded,
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            isShuffle = isShuffle,
            isRepeat = isRepeat,
            downloadProgress = currentTrack?.let { downloadProgress[it.id] },
            similarTracks = similarTracks,
            isAutoplayEnabled = isAutoplayEnabled,
            onToggleAutoplay = { viewModel.toggleAutoplay() },
            onTrackClick = { viewModel.playTrack(it) },
            onClose = { viewModel.setNowPlayingExpanded(false) },
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onSeekTo = { viewModel.seekTo(it) },
            onSkipNext = { viewModel.skipNext() },
            onSkipPrevious = { viewModel.skipPrevious() },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onToggleRepeat = { viewModel.toggleRepeat() },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onDownloadClick = { viewModel.downloadTrack(it) }
        )
    }
}
