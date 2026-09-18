package com.example.data.repository

import com.example.data.local.PlaylistDao
import com.example.data.local.TrackDao
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistTrackCrossRef
import com.example.data.model.PlaylistWithTracks
import com.example.data.model.TrackEntity
import com.example.downloader.MusicDownloader
import com.example.grabber.MusicGrabberService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class MusicRepository(
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao,
    private val grabberService: MusicGrabberService,
    private val downloader: MusicDownloader
) {
    val allTracks: Flow<List<TrackEntity>> = trackDao.getAllTracks()
    val downloadedTracks: Flow<List<TrackEntity>> = trackDao.getDownloadedTracks()
    val favoriteTracks: Flow<List<TrackEntity>> = trackDao.getFavoriteTracks()
    val recentlyPlayedTracks: Flow<List<TrackEntity>> = trackDao.getRecentlyPlayedTracks()
    val mostPlayedTracks: Flow<List<TrackEntity>> = trackDao.getMostPlayedTracks()
    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val downloadProgress: StateFlow<Map<String, Int>> = downloader.downloadProgress

    suspend fun initializePreloadedTracksIfNeeded() {
        if (trackDao.getTrackCount() == 0) {
            val initialTracks = listOf(
                TrackEntity(
                    id = "sp_synth_01",
                    title = "Midnight Horizon",
                    artist = "Neon Echo",
                    album = "Synthwave Odyssey",
                    coverUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    durationMs = 372000L,
                    source = "SPOTIFY",
                    sourceUrl = "https://open.spotify.com/track/synthwave-midnight",
                    isFavorite = true,
                    playCount = 14,
                    lastPlayedAt = System.currentTimeMillis() - 3600000L * 2
                ),
                TrackEntity(
                    id = "yt_studio_02",
                    title = "Cyber Chill Hop",
                    artist = "Studio Velocity",
                    album = "Creator Audio Library",
                    coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    durationMs = 425000L,
                    source = "YOUTUBE_STUDIO",
                    sourceUrl = "https://studio.youtube.com/audio/cyber-chill",
                    isFavorite = false,
                    playCount = 28,
                    lastPlayedAt = System.currentTimeMillis() - 3600000L * 5
                ),
                TrackEntity(
                    id = "sp_lofi_03",
                    title = "Coffee & Rain Drops",
                    artist = "Lofi Café",
                    album = "Afternoon Study",
                    coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                    durationMs = 344000L,
                    source = "SPOTIFY",
                    sourceUrl = "https://open.spotify.com/track/lofi-coffee-rain",
                    isFavorite = true,
                    playCount = 42,
                    lastPlayedAt = System.currentTimeMillis() - 3600000L
                ),
                TrackEntity(
                    id = "yt_studio_04",
                    title = "Bass Dimension",
                    artist = "Pulse & Sub",
                    album = "Bassline Sessions",
                    coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                    durationMs = 302000L,
                    source = "YOUTUBE_STUDIO",
                    sourceUrl = "https://studio.youtube.com/audio/bass-dimension",
                    isFavorite = false,
                    playCount = 9,
                    lastPlayedAt = System.currentTimeMillis() - 3600000L * 24
                ),
                TrackEntity(
                    id = "sp_dance_05",
                    title = "Electric Sunrise",
                    artist = "Solar Shift",
                    album = "Club Anthems",
                    coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                    durationMs = 220000L,
                    source = "SPOTIFY",
                    sourceUrl = "https://open.spotify.com/track/electric-sunrise",
                    isFavorite = false,
                    playCount = 18,
                    lastPlayedAt = System.currentTimeMillis() - 3600000L * 12
                ),
                TrackEntity(
                    id = "yt_studio_06",
                    title = "City Lights Voyage",
                    artist = "Aura Studio",
                    album = "Urban Beats",
                    coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                    durationMs = 261000L,
                    source = "YOUTUBE_STUDIO",
                    sourceUrl = "https://studio.youtube.com/audio/city-lights",
                    isFavorite = false,
                    playCount = 5,
                    lastPlayedAt = System.currentTimeMillis() - 3600000L * 48
                ),
                TrackEntity(
                    id = "sp_chill_07",
                    title = "Deep Space Floating",
                    artist = "Cosmic Reverie",
                    album = "Starlight Ambient",
                    coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                    durationMs = 290000L,
                    source = "SPOTIFY",
                    sourceUrl = "https://open.spotify.com/track/cosmic-floating",
                    isFavorite = false,
                    playCount = 11,
                    lastPlayedAt = System.currentTimeMillis() - 3600000L * 18
                ),
                TrackEntity(
                    id = "yt_studio_08",
                    title = "Golden Hour Groove",
                    artist = "Sunset Collective",
                    album = "Acoustic & Beats",
                    coverUrl = "https://images.unsplash.com/photo-1520523839898-507127054976?w=600&q=80",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3",
                    durationMs = 258000L,
                    source = "YOUTUBE_STUDIO",
                    sourceUrl = "https://studio.youtube.com/audio/golden-hour",
                    isFavorite = true,
                    playCount = 31,
                    lastPlayedAt = System.currentTimeMillis() - 1800000L
                )
            )
            trackDao.insertTracks(initialTracks)

            // Seed default curated playlists
            val playlist1 = PlaylistEntity(
                id = "pl_synth_wave",
                name = "Late Night Synthwave",
                description = "Neon retro vibes from Spotify & Studio collections",
                coverUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=600&q=80",
                trackCount = 3
            )
            val playlist2 = PlaylistEntity(
                id = "pl_focus_chill",
                name = "Focus & Deep Coding",
                description = "High-definition background flows for maximum immersion",
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&q=80",
                trackCount = 3
            )
            playlistDao.insertPlaylist(playlist1)
            playlistDao.insertPlaylist(playlist2)

            playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlist1.id, "sp_synth_01"))
            playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlist1.id, "yt_studio_04"))
            playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlist1.id, "sp_dance_05"))

            playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlist2.id, "sp_lofi_03"))
            playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlist2.id, "yt_studio_02"))
            playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlist2.id, "yt_studio_08"))
        }
    }

    suspend fun grabMusic(input: String): Result<TrackEntity> {
        val result = grabberService.grabMusic(input)
        result.onSuccess { track ->
            trackDao.insertTrack(track)
        }
        return result
    }

    suspend fun downloadTrack(track: TrackEntity) {
        downloader.downloadTrack(track)
    }

    suspend fun removeDownload(track: TrackEntity) {
        downloader.removeDownload(track)
    }

    suspend fun toggleFavorite(track: TrackEntity) {
        trackDao.setFavorite(track.id, !track.isFavorite)
    }

    suspend fun recordTrackPlayed(trackId: String) {
        trackDao.recordTrackPlayed(trackId)
    }

    suspend fun deleteTrack(track: TrackEntity) {
        if (track.isDownloaded) {
            downloader.removeDownload(track)
        }
        trackDao.deleteTrack(track)
    }

    suspend fun searchOnlineTracks(query: String): List<TrackEntity> {
        val remoteResults = grabberService.searchOnlineTracks(query)
        // Check if any results match local downloaded/favorite flags
        return remoteResults.map { remoteTrack ->
            val localTrack = trackDao.getTrackByIdDirect(remoteTrack.id)
            if (localTrack != null) {
                remoteTrack.copy(
                    isDownloaded = localTrack.isDownloaded,
                    isFavorite = localTrack.isFavorite,
                    localAudioPath = localTrack.localAudioPath,
                    playCount = localTrack.playCount
                )
            } else {
                remoteTrack
            }
        }
    }

    suspend fun saveTrack(track: TrackEntity) {
        trackDao.insertTrack(track)
    }

    suspend fun getSimilarTracks(track: TrackEntity): List<TrackEntity> {
        val similar = grabberService.fetchSimilarTracks(track)
        return similar.map { remoteTrack ->
            val local = trackDao.getTrackByIdDirect(remoteTrack.id)
            if (local != null) {
                remoteTrack.copy(
                    isDownloaded = local.isDownloaded,
                    isFavorite = local.isFavorite,
                    localAudioPath = local.localAudioPath,
                    playCount = local.playCount
                )
            } else {
                remoteTrack
            }
        }
    }

    fun searchTracks(query: String): Flow<List<TrackEntity>> {
        return trackDao.searchTracks(query)
    }

    // Playlist Management
    suspend fun createPlaylist(name: String, description: String = "", coverUrl: String = ""): PlaylistEntity {
        val playlist = PlaylistEntity(
            id = "pl_" + UUID.randomUUID().toString().take(8),
            name = name,
            description = description,
            coverUrl = coverUrl.ifEmpty { "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&q=80" },
            trackCount = 0
        )
        playlistDao.insertPlaylist(playlist)
        return playlist
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String) {
        playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId, trackId))
        playlistDao.updatePlaylistTrackCount(playlistId)
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
        playlistDao.updatePlaylistTrackCount(playlistId)
    }

    suspend fun deletePlaylist(playlistId: String) {
        playlistDao.deletePlaylistById(playlistId)
    }

    fun getPlaylistWithTracks(playlistId: String): Flow<PlaylistWithTracks?> {
        return playlistDao.getPlaylistWithTracks(playlistId)
    }
}
