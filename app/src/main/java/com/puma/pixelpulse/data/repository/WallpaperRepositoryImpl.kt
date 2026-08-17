package com.puma.pixelpulse.data.repository

import com.puma.pixelpulse.data.local.WallpaperDao
import com.puma.pixelpulse.data.local.WallpaperEntity
import com.puma.pixelpulse.domain.model.ScaleMode
import com.puma.pixelpulse.domain.model.Wallpaper
import com.puma.pixelpulse.domain.model.WallpaperType
import com.puma.pixelpulse.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperRepositoryImpl @Inject constructor(
    private val dao: WallpaperDao
) : WallpaperRepository {

    override fun getAll(): Flow<List<Wallpaper>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getFavorites(): Flow<List<Wallpaper>> =
        dao.getFavorites().map { entities -> entities.map { it.toDomain() } }

    override fun getRecentWallpapers(): Flow<List<Wallpaper>> =
        dao.getRecentWallpapers().map { entities -> entities.map { it.toDomain() } }

    override fun getByType(type: String): Flow<List<Wallpaper>> =
        dao.getByType(type).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): Wallpaper? =
        dao.getById(id)?.toDomain()

    override suspend fun getByUri(uri: String): Wallpaper? =
        dao.getByUri(uri)?.toDomain()

    override suspend fun insert(wallpaper: Wallpaper): Long =
        dao.insert(wallpaper.toEntity())

    override suspend fun update(wallpaper: Wallpaper) =
        dao.update(wallpaper.toEntity())

    override suspend fun delete(wallpaper: Wallpaper) =
        dao.delete(wallpaper.toEntity())

    override suspend fun deleteById(id: Long) =
        dao.deleteById(id)

    override suspend fun setFavorite(id: Long, isFavorite: Boolean) =
        dao.setFavorite(id, isFavorite)

    override suspend fun markAsUsed(id: Long) =
        dao.markAsUsed(id)

    override suspend fun getCount(): Int =
        dao.getCount()
}

private fun WallpaperEntity.toDomain() = Wallpaper(
    id = id,
    uri = uri,
    name = name,
    type = WallpaperType.valueOf(type),
    thumbnailUri = thumbnailUri,
    duration = duration,
    width = width,
    height = height,
    sizeBytes = sizeBytes,
    dateAdded = dateAdded,
    dateModified = dateModified,
    isFavorite = isFavorite,
    playbackSpeed = playbackSpeed,
    scaleMode = ScaleMode.valueOf(scaleMode),
    muted = muted,
    lastUsedAt = lastUsedAt,
    trimStartMs = trimStartMs,
    trimEndMs = trimEndMs,
    backgroundColor = backgroundColor
)

private fun Wallpaper.toEntity() = WallpaperEntity(
    id = id,
    uri = uri,
    name = name,
    type = type.name,
    thumbnailUri = thumbnailUri,
    duration = duration,
    width = width,
    height = height,
    sizeBytes = sizeBytes,
    dateAdded = dateAdded,
    dateModified = dateModified,
    isFavorite = isFavorite,
    playbackSpeed = playbackSpeed,
    scaleMode = scaleMode.name,
    muted = muted,
    lastUsedAt = lastUsedAt,
    trimStartMs = trimStartMs,
    trimEndMs = trimEndMs,
    backgroundColor = backgroundColor
)
