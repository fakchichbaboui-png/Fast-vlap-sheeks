package com.example.player

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import com.example.data.model.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayerManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    init {
        instance = this
    }

    private var mediaPlayer: MediaPlayer? = null

    private val _currentTrack = MutableStateFlow<TrackEntity?>(null)
    val currentTrack: StateFlow<TrackEntity?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private val _playbackQueue = MutableStateFlow<List<TrackEntity>>(emptyList())
    val playbackQueue: StateFlow<List<TrackEntity>> = _playbackQueue.asStateFlow()

    private var progressTrackerJob: Job? = null

    // Callback when a track finishes or nears completion, enabling infinite similar autoplay
    var onTrackFinished: ((current: TrackEntity) -> Unit)? = null

    fun setQueue(tracks: List<TrackEntity>) {
        _playbackQueue.value = tracks
    }

    fun appendToQueue(tracks: List<TrackEntity>) {
        val currentQueue = _playbackQueue.value
        val newItems = tracks.filter { newT -> !currentQueue.any { it.id == newT.id } }
        if (newItems.isNotEmpty()) {
            _playbackQueue.value = currentQueue + newItems
        }
    }

    fun playTrack(track: TrackEntity, newQueue: List<TrackEntity>? = null) {
        if (newQueue != null) {
            _playbackQueue.value = newQueue
        } else if (!_playbackQueue.value.any { it.id == track.id }) {
            _playbackQueue.value = _playbackQueue.value + track
        }

        _currentTrack.value = track
        _isBuffering.value = true
        _isPlaying.value = false
        _currentPositionMs.value = 0L

        stopProgressTracker()

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            try {
                // Check if offline local file exists first
                val localFile = track.localAudioPath?.let { File(it) }
                if (localFile != null && localFile.exists() && localFile.length() > 0) {
                    setDataSource(localFile.absolutePath)
                } else {
                    setDataSource(track.audioUrl)
                }

                setOnPreparedListener { mp ->
                    _isBuffering.value = false
                    _durationMs.value = mp.duration.toLong().coerceAtLeast(track.durationMs)
                    mp.start()
                    _isPlaying.value = true
                    startProgressTracker()
                    startOrUpdateBackgroundService(track, true)
                }

                setOnCompletionListener {
                    handleTrackCompletion()
                }

                setOnErrorListener { _, what, extra ->
                    _isBuffering.value = false
                    _isPlaying.value = false
                    true
                }

                prepareAsync()
            } catch (e: Exception) {
                e.printStackTrace()
                _isBuffering.value = false
                _isPlaying.value = false
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopProgressTracker()
            _currentTrack.value?.let { startOrUpdateBackgroundService(it, false) }
        } else {
            player.start()
            _isPlaying.value = true
            startProgressTracker()
            _currentTrack.value?.let { startOrUpdateBackgroundService(it, true) }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs.toInt())
            _currentPositionMs.value = positionMs
        }
    }

    fun skipNext() {
        val queue = _playbackQueue.value
        val current = _currentTrack.value ?: return
        if (queue.isEmpty()) return

        if (_isShuffle.value) {
            val otherTracks = queue.filter { it.id != current.id }
            val next = if (otherTracks.isNotEmpty()) otherTracks.random() else current
            playTrack(next)
            return
        }

        val currentIndex = queue.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentIndex < queue.size - 1) {
            playTrack(queue[currentIndex + 1])
        } else {
            // Reached the end of queue: inform callback to discover similar tracks from Spotify
            onTrackFinished?.invoke(current)
            if (queue.isNotEmpty()) {
                playTrack(queue[0])
            }
        }
    }

    fun skipPrevious() {
        val player = mediaPlayer
        if (player != null && player.currentPosition > 3000) {
            player.seekTo(0)
            _currentPositionMs.value = 0L
            return
        }

        val queue = _playbackQueue.value
        val current = _currentTrack.value ?: return
        if (queue.isEmpty()) return

        val currentIndex = queue.indexOfFirst { it.id == current.id }
        if (currentIndex > 0) {
            playTrack(queue[currentIndex - 1])
        } else if (queue.isNotEmpty()) {
            playTrack(queue.last())
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
    }

    private fun handleTrackCompletion() {
        if (_isRepeat.value) {
            _currentTrack.value?.let { playTrack(it) }
        } else {
            skipNext()
        }
    }

    private fun startOrUpdateBackgroundService(track: TrackEntity, isPlaying: Boolean) {
        try {
            val intent = Intent(context, MusicPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPositionMs.value = player.currentPosition.toLong()
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = null
    }

    fun release() {
        stopProgressTracker()
        mediaPlayer?.release()
        mediaPlayer = null
        if (instance === this) {
            instance = null
        }
    }

    companion object {
        var instance: AudioPlayerManager? = null
            private set
    }
}
