package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<DownloadHistoryEntity>>

    @Query("SELECT * FROM download_history WHERE id = :id LIMIT 1")
    suspend fun getHistoryById(id: String): DownloadHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(download: DownloadHistoryEntity)

    @Query("UPDATE download_history SET progress = :progress, status = :status WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Float, status: String)

    @Query("UPDATE download_history SET downloadPath = :path, status = :status WHERE id = :id")
    suspend fun updateCompletedPath(id: String, path: String, status: String)

    @Query("UPDATE download_history SET status = :status, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateError(id: String, status: String, errorMessage: String?)

    @Query("DELETE FROM download_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM download_history")
    suspend fun clearAll()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE url = :url)")
    fun isFavorite(url: String): Flow<Boolean>
}
