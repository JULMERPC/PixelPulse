package com.puma.pixelpulse.domain.repository

import com.puma.pixelpulse.domain.model.Wallpaper
import kotlinx.coroutines.flow.Flow

interface WallpaperRepository {

    fun getAll(): Flow<List<Wallpaper>>

    fun getFavorites(): Flow<List<Wallpaper>>

    fun getRecentWallpapers(): Flow<List<Wallpaper>>

    fun getByType(type: String): Flow<List<Wallpaper>>

    suspend fun getById(id: Long): Wallpaper?

    suspend fun getByUri(uri: String): Wallpaper?

    suspend fun insert(wallpaper: Wallpaper): Long

    suspend fun update(wallpaper: Wallpaper)

    suspend fun delete(wallpaper: Wallpaper)

    suspend fun deleteById(id: Long)

    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    suspend fun markAsUsed(id: Long)

    suspend fun getCount(): Int
}
