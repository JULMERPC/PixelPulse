package com.puma.pixelpulse.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {

    @Query("SELECT * FROM wallpapers ORDER BY dateAdded DESC")
    fun getAll(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavorites(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE type = :type ORDER BY dateAdded DESC")
    fun getByType(type: String): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE lastUsedAt > 0 ORDER BY lastUsedAt DESC LIMIT 20")
    fun getRecentWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE id = :id")
    suspend fun getById(id: Long): WallpaperEntity?

    @Query("SELECT * FROM wallpapers WHERE uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): WallpaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallpaper: WallpaperEntity): Long

    @Update
    suspend fun update(wallpaper: WallpaperEntity)

    @Delete
    suspend fun delete(wallpaper: WallpaperEntity)

    @Query("DELETE FROM wallpapers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE wallpapers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE wallpapers SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun markAsUsed(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM wallpapers")
    suspend fun getCount(): Int
}
