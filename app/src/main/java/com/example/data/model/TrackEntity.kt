package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "Single",
    val coverUrl: String,
    val audioUrl: String,
    val durationMs: Long = 0L,
    val source: String = "CURATED", // "SPOTIFY", "YOUTUBE_STUDIO", "CURATED"
    val sourceUrl: String = "",
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val localAudioPath: String? = null,
    val fileSizeBytes: Long = 0L,
    val addedAt: Long = System.currentTimeMillis(),
    val playCount: Int = 0,
    val lastPlayedAt: Long = 0L,
    val audioQuality: String = "320 kbps HD" // HD high quality audio indicator
)
