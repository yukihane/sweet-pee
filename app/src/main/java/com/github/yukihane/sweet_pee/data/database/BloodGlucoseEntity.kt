package com.github.yukihane.sweet_pee.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.yukihane.sweet_pee.domain.model.BloodGlucoseReading
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * 血糖値測定データのデータベースエンティティ
 */
@Entity(tableName = "blood_glucose_readings")
data class BloodGlucoseEntity(
    @PrimaryKey val id: String,
    val timestamp: Long, // Unix timestamp in seconds
    val valueMgDl: Double,
    val valueMMolL: Double,
    val measurementTimeHour: Int?, // 時刻情報の時間部分
    val measurementTimeMinute: Int?, // 時刻情報の分部分
    val source: String,
    val syncedToHealthConnect: Boolean
) {
    /**
     * エンティティからドメインモデルへの変換
     */
    fun toDomain(): BloodGlucoseReading {
        val dateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)
        val measurementTime = if (measurementTimeHour != null && measurementTimeMinute != null) {
            LocalTime.of(measurementTimeHour, measurementTimeMinute)
        } else null
        
        return BloodGlucoseReading(
            id = id,
            timestamp = dateTime,
            valueMgDl = valueMgDl,
            valueMMolL = valueMMolL,
            measurementTime = measurementTime,
            source = source,
            syncedToHealthConnect = syncedToHealthConnect
        )
    }
    
    companion object {
        /**
         * ドメインモデルからエンティティへの変換
         */
        fun fromDomain(reading: BloodGlucoseReading): BloodGlucoseEntity {
            return BloodGlucoseEntity(
                id = reading.id,
                timestamp = reading.timestamp.toEpochSecond(ZoneOffset.UTC),
                valueMgDl = reading.valueMgDl,
                valueMMolL = reading.valueMMolL,
                measurementTimeHour = reading.measurementTime?.hour,
                measurementTimeMinute = reading.measurementTime?.minute,
                source = reading.source,
                syncedToHealthConnect = reading.syncedToHealthConnect
            )
        }
    }
}
