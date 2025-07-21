package com.github.yukihane.sweet_pee.domain.model

import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 血糖値測定データのドメインモデル
 */
data class BloodGlucoseReading(
    val id: String,
    val timestamp: LocalDateTime,
    val valueMgDl: Double,
    val valueMMolL: Double = valueMgDl * 0.0555, // mg/dL → mmol/L 変換
    val measurementTime: LocalTime? = null, // 詳細な測定時刻（e-SMBGから取得）
    val source: String = "e-smbg",
    val syncedToHealthConnect: Boolean = false
) {
    companion object {
        /**
         * mg/dL から mmol/L への変換係数
         */
        const val MG_DL_TO_MMOL_L_FACTOR = 0.0555
    }
    
    /**
     * Google Health Connect用の値を取得
     */
    fun getValueForHealthConnect(): Double = valueMMolL
    
    /**
     * 表示用の値を取得（mg/dL）
     */
    fun getDisplayValue(): Double = valueMgDl
}
