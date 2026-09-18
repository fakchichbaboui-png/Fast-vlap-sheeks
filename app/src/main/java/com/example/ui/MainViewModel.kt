package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.PlaylistEntity
import com.example.data.model.TrackEntity
import com.example.data.repository.MusicRepository
import com.example.downloader.MusicDownloader
import com.example.grabber.MusicGrabberService
import com.example.player.AudioPlayerManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    SEARCH,
    GRABBER,
    LIBRARY
}

enum class FilterChip {
    ALL,
    DOWNLOADED,
    FAVORITES,
    SPOTIFY,
    YOUTUBE
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val trackDao = database.trackDao()
    private val playlistDao = database.playlistDao()
    private val grabberService = MusicGrabberService()
    private val downloader = MusicDownloader(application, trackDao)
    val repository = MusicRepository(trackDao, playlistDao, grabberService, downloader)
    val playerManager = AudioPlayerManager(application, viewModelScope)

    val allTracks: StateFlow<List<TrackEntity>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedTracks: StateFlow<List<TrackEntity>> = repository.downloadedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<TrackEntity>> = repository.favoriteTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedTracks: StateFlow<List<TrackEntity>> = repository.recentlyPlayedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayedTracks: StateFlow<List<TrackEntity>> = repository.mostPlayedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadProgress: StateFlow<Map<String, Int>> = repository.downloadProgress

    // Player States
    val currentTrack: StateFlow<TrackEntity?> = playerManager.currentTrack
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val isBuffering: StateFlow<Boolean> = playerManager.isBuffering
    val currentPositionMs: StateFlow<Long> = playerManager.currentPositionMs
    val durationMs: StateFlow<Long> = playerManager.durationMs
    val isShuffle: StateFlow<Boolean> = playerManager.isShuffle
    val isRepeat: StateFlow<Boolean> = playerManager.isRepeat

    // Navigation & UI States
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _activeFilter = MutableStateFlow(FilterChip.ALL)
    val activeFilter: StateFlow<FilterChip> = _activeFilter.asStateFlow()

    // Grabber UI State
    private val _grabInput = MutableStateFlow("")
    val grabInput: StateFlow<String> = _grabInput.asStateFlow()

    private val _isGrabbing = MutableStateFlow(false)
    val isGrabbing: StateFlow<Boolean> = _isGrabbing.asStateFlow()

    private val _grabSuccessTrack = MutableStateFlow<TrackEntity?>(null)
    val grabSuccessTrack: StateFlow<TrackEntity?> = _grabSuccessTrack.asStateFlow()

    private val _grabErrorMessage = MutableStateFlow<String?>(null)
    val grabErrorMessage: StateFlow<String?> = _grabErrorMessage.asStateFlow()

    // Search UI State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLiveSearching = MutableStateFlow(false)
    val isLiveSearching: StateFlow<Boolean> = _isLiveSearching.asStateFlow()

    private val _onlineSearchResults = MutableStateFlow<List<TrackEntity>>(emptyList())
    val onlineSearchResults: StateFlow<List<TrackEntity>> = _onlineSearchResults.asStateFlow()

    val searchResults: StateFlow<List<TrackEntity>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            _onlineSearchResults.value = emptyList()
            flowOf(emptyList())
        } else {
            repository.searchTracks(query.trim())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Library tab selection (0 = Downloaded, 1 = Playlists, 2 = Favorites, 3 = All Grabbed)
    private val _libraryTab = MutableStateFlow(0)
    val libraryTab: StateFlow<Int> = _libraryTab.asStateFlow()

    // Currently opened playlist in detail view (if any)
    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    val selectedPlaylistTracks: StateFlow<List<TrackEntity>> = _selectedPlaylist.flatMapLatest { pl ->
        if (pl == null) {
            flowOf(emptyList())
        } else {
            repository.getPlaylistWithTracks(pl.id).flatMapLatest { plWithTracks ->
                flowOf(plWithTracks?.tracks ?: emptyList())
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dialog state for adding a track to a playlist
    private val _trackToAddToPlaylist = MutableStateFlow<TrackEntity?>(null)
    val trackToAddToPlaylist: StateFlow<TrackEntity?> = _trackToAddToPlaylist.asStateFlow()

    // Similar / Recommended tracks for the currently playing song (Spotify Radio style)
    private val _similarTracks = MutableStateFlow<List<TrackEntity>>(emptyList())
    val similarTracks: StateFlow<List<TrackEntity>> = _similarTracks.asStateFlow()

    private val _isLoadingSimilar = MutableStateFlow(false)
    val isLoadingSimilar: StateFlow<Boolean> = _isLoadingSimilar.asStateFlow()

    // Autoplay toggle (defaults to true: continuous Spotify/YouTube Music style listening)
    private val _isAutoplayEnabled = MutableStateFlow(true)
    val isAutoplayEnabled: StateFlow<Boolean> = _isAutoplayEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializePreloadedTracksIfNeeded()
        }

        // Set up infinite autoplay callback when queue finishes
        playerManager.onTrackFinished = { finishedTrack ->
            if (_isAutoplayEnabled.value) {
                onQueueExhaustedAutoplay(finishedTrack)
            }
        }
    }

    fun toggleAutoplay() {
        _isAutoplayEnabled.value = !_isAutoplayEnabled.value
    }

    private fun onQueueExhaustedAutoplay(finishedTrack: TrackEntity) {
        viewModelScope.launch {
            val candidate = _similarTracks.value.firstOrNull { it.id != finishedTrack.id }
            if (candidate != null) {
                // Auto play the first similar recommendation and replenish
                playTrack(candidate)
            } else {
                // Fetch fresh recommendations from Spotify and play the top match
                val freshSimilar = repository.getSimilarTracks(finishedTrack)
                val nextToPlay = freshSimilar.firstOrNull { it.id != finishedTrack.id }
                if (nextToPlay != null) {
                    playerManager.appendToQueue(freshSimilar)
                    _similarTracks.value = freshSimilar.filter { it.id != nextToPlay.id }
                    playTrack(nextToPlay)
                }
            }
        }
    }

    fun loadSimilarTracksFor(track: TrackEntity) {
        viewModelScope.launch {
            _isLoadingSimilar.value = true
            try {
                val similar = repository.getSimilarTracks(track)
                _similarTracks.value = similar
                // If autoplay is enabled, also queue these tracks so the user never runs out of music
                if (_isAutoplayEnabled.value && similar.isNotEmpty()) {
                    playerManager.appendToQueue(similar)
                }
            } catch (_: Exception) {
                // Ignore transient network errors
            } finally {
                _isLoadingSimilar.value = false
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        _selectedPlaylist.value = null
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun setActiveFilter(filter: FilterChip) {
        _activeFilter.value = filter
    }

    fun setLibraryTab(tab: Int) {
        _libraryTab.value = tab
    }

    fun openPlaylist(playlist: PlaylistEntity) {
        _selectedPlaylist.value = playlist
    }

    fun closePlaylist() {
        _selectedPlaylist.value = null
    }

    fun showAddToPlaylistDialog(track: TrackEntity) {
        _trackToAddToPlaylist.value = track
    }

    fun dismissAddToPlaylistDialog() {
        _trackToAddToPlaylist.value = null
    }

    fun updateGrabInput(input: String) {
        _grabInput.value = input
        _grabErrorMessage.value = null
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        val trimmed = query.trim()
        if (trimmed.length < 2) {
            _onlineSearchResults.value = emptyList()
            _isLiveSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(400) // debounce typing
            _isLiveSearching.value = true
            try {
                val results = repository.searchOnlineTracks(trimmed)
                _onlineSearchResults.value = results
            } catch (_: Exception) {
                _onlineSearchResults.value = emptyList()
            } finally {
                _isLiveSearching.value = false
            }
        }
    }

    fun grabMusic() {
        val query = _grabInput.value.trim()
        if (query.isEmpty()) return

        _isGrabbing.value = true
        _grabErrorMessage.value = null
        _grabSuccessTrack.value = null

        viewModelScope.launch {
            val result = repository.grabMusic(query)
            _isGrabbing.value = false
            result.onSuccess { track ->
                _grabSuccessTrack.value = track
                playTrack(track, allTracks.value)
            }.onFailure { error ->
                _grabErrorMessage.value = error.message ?: "Could not grab track"
            }
        }
    }

    fun playTrack(track: TrackEntity, contextList: List<TrackEntity>? = null) {
        val queue = contextList ?: allTracks.value
        playerManager.playTrack(track, queue)
        loadSimilarTracksFor(track)
        viewModelScope.launch {
            repository.saveTrack(track)
            repository.recordTrackPlayed(track.id)
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun skipNext() {
        playerManager.skipNext()
        playerManager.currentTrack.value?.let { track ->
            viewModelScope.launch { repository.recordTrackPlayed(track.id) }
        }
    }

    fun skipPrevious() {
        playerManager.skipPrevious()
        playerManager.currentTrack.value?.let { track ->
            viewModelScope.launch { repository.recordTrackPlayed(track.id) }
        }
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun toggleRepeat() {
        playerManager.toggleRepeat()
    }

    fun toggleFavorite(track: TrackEntity) {
        viewModelScope.launch {
            repository.saveTrack(track)
            repository.toggleFavorite(track)
        }
    }

    fun downloadTrack(track: TrackEntity) {
        viewModelScope.launch {
            repository.saveTrack(track)
            repository.downloadTrack(track)
        }
    }

    fun removeDownload(track: TrackEntity) {
        viewModelScope.launch {
            repository.removeDownload(track)
        }
    }

    fun deleteTrack(track: TrackEntity) {
        viewModelScope.launch {
            repository.deleteTrack(track)
        }
    }

    // Playlist Actions
    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
