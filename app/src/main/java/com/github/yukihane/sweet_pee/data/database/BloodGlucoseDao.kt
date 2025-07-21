package com.github.yukihane.sweet_pee.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 血糖値データのDAO（Data Access Object）
 */
@Dao
interface BloodGlucoseDao {

    /**
     * 全ての血糖値データを取得（新しい順）
     */
    @Query("SELECT * FROM blood_glucose_readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<BloodGlucoseEntity>>

    /**
     * Health Connectに未同期のデータを取得
     */
    @Query("SELECT * FROM blood_glucose_readings WHERE syncedToHealthConnect = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedReadings(): List<BloodGlucoseEntity>

    /**
     * IDでデータを取得
     */
    @Query("SELECT * FROM blood_glucose_readings WHERE id = :id")
    suspend fun getReadingById(id: String): BloodGlucoseEntity?

    /**
     * 指定期間のデータを取得
     */
    @Query("SELECT * FROM blood_glucose_readings WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp ORDER BY timestamp DESC")
    suspend fun getReadingsBetween(startTimestamp: Long, endTimestamp: Long): List<BloodGlucoseEntity>

    /**
     * データを挿入（重複時は置換）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: BloodGlucoseEntity)

    /**
     * 複数のデータを一括挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<BloodGlucoseEntity>)

    /**
     * データを更新
     */
    @Update
    suspend fun updateReading(reading: BloodGlucoseEntity)

    /**
     * 複数のデータを一括更新
     */
    @Update
    suspend fun updateReadings(readings: List<BloodGlucoseEntity>)

    /**
     * 指定されたIDのデータをHealth Connectに同期済みとしてマーク
     */
    @Query("UPDATE blood_glucose_readings SET syncedToHealthConnect = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    /**
     * 指定されたIDのデータを削除
     */
    @Query("DELETE FROM blood_glucose_readings WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * 全てのデータを削除
     */
    @Query("DELETE FROM blood_glucose_readings")
    suspend fun deleteAll()

    /**
     * データの総数を取得
     */
    @Query("SELECT COUNT(*) FROM blood_glucose_readings")
    suspend fun getCount(): Int

    /**
     * 同期済みデータの数を取得
     */
    @Query("SELECT COUNT(*) FROM blood_glucose_readings WHERE syncedToHealthConnect = 1")
    suspend fun getSyncedCount(): Int
}
