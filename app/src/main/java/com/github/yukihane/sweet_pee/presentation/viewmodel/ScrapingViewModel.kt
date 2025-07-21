package com.github.yukihane.sweet_pee.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.yukihane.sweet_pee.data.network.ESMBGResult
import com.github.yukihane.sweet_pee.data.network.ESMBGService
import com.github.yukihane.sweet_pee.data.network.FormInfo
import com.github.yukihane.sweet_pee.data.network.LoginFormAnalysis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * スクレイピング機能のViewModel
 */
@HiltViewModel
class ScrapingViewModel @Inject constructor(
    private val esmgService: ESMBGService
) : ViewModel() {

    private val _scrapingState = MutableStateFlow<ScrapingState>(ScrapingState.Idle)
    val scrapingState: StateFlow<ScrapingState> = _scrapingState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private var currentFormInfo: FormInfo? = null

    /**
     * ログを追加
     */
    private fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        _logs.value = _logs.value + "[$timestamp] $message"
    }

    /**
     * ログをクリア
     */
    fun clearLogs() {
        _logs.value = emptyList()
    }

    /**
     * e-SMBGサイトからログインフォームを取得
     */
    fun fetchLoginForm() {
        viewModelScope.launch {
            try {
                _scrapingState.value = ScrapingState.Loading("ログインフォームを取得中...")
                addLog("e-SMBGサイトへアクセス開始")
                
                when (val result = esmgService.getLoginForm()) {
                    is ESMBGResult.LoginFormFound -> {
                        currentFormInfo = result.formInfo
                        val analysis = esmgService.analyzeLoginForm(result.document)
                        
                        addLog("ログインフォーム取得成功")
                        addLog("フォームアクション: ${result.formInfo.action}")
                        addLog("フォームメソッド: ${result.formInfo.method}")
                        addLog("フィールド数: ${analysis.fields.size}")
                        
                        analysis.fields.forEach { field ->
                            addLog("- ${field.name} (${field.type}): ${field.value}")
                        }

                        _scrapingState.value = ScrapingState.FormLoaded(
                            analysis = analysis,
                            htmlPreview = result.html.take(500) + "..."
                        )
                    }
                    is ESMBGResult.Error -> {
                        addLog("エラー: ${result.message}")
                        _scrapingState.value = ScrapingState.Error(result.message)
                    }
                    else -> {
                        addLog("予期しない結果タイプ")
                        _scrapingState.value = ScrapingState.Error("予期しない結果")
                    }
                }
            } catch (e: Exception) {
                addLog("例外発生: ${e.message}")
                _scrapingState.value = ScrapingState.Error("予期しないエラー: ${e.message}")
            }
        }
    }

    /**
     * ログイン試行を実行
     */
    fun attemptLogin(username: String = "test_user", password: String = "test_pass") {
        viewModelScope.launch {
            try {
                val formInfo = currentFormInfo
                if (formInfo == null) {
                    addLog("エラー: フォーム情報が取得されていません")
                    _scrapingState.value = ScrapingState.Error("先にログインフォームを取得してください")
                    return@launch
                }

                _scrapingState.value = ScrapingState.Loading("ログイン試行中...")
                addLog("ログイン試行開始")
                addLog("ユーザー名: $username")
                addLog("パスワード: ${password.map { '*' }.joinToString("")}")

                when (val result = esmgService.attemptLogin(username, password, formInfo)) {
                    is ESMBGResult.LoginAttempted -> {
                        addLog("ログインPOST送信完了")
                        addLog("ステータスコード: ${result.statusCode}")
                        addLog("POST URL: ${result.postUrl}")
                        addLog("POST データ:")
                        result.postData.forEach { (key, value) ->
                            val displayValue = if (key.contains("pass", ignoreCase = true)) {
                                "*".repeat(value.length)
                            } else {
                                value
                            }
                            addLog("  $key: $displayValue")
                        }

                        _scrapingState.value = ScrapingState.LoginCompleted(
                            statusCode = result.statusCode,
                            htmlPreview = result.html.take(1000) + "...",
                            fullHtml = result.html
                        )
                    }
                    is ESMBGResult.Error -> {
                        addLog("ログインエラー: ${result.message}")
                        _scrapingState.value = ScrapingState.Error(result.message)
                    }
                    else -> {
                        addLog("予期しない結果タイプ")
                        _scrapingState.value = ScrapingState.Error("予期しない結果")
                    }
                }
            } catch (e: Exception) {
                addLog("例外発生: ${e.message}")
                _scrapingState.value = ScrapingState.Error("予期しないエラー: ${e.message}")
            }
        }
    }

    /**
     * 状態をリセット
     */
    fun reset() {
        _scrapingState.value = ScrapingState.Idle
        currentFormInfo = null
        clearLogs()
        addLog("状態をリセットしました")
    }
}

/**
 * スクレイピングの状態を表すsealed class
 */
sealed class ScrapingState {
    object Idle : ScrapingState()
    
    data class Loading(val message: String) : ScrapingState()
    
    data class FormLoaded(
        val analysis: LoginFormAnalysis,
        val htmlPreview: String
    ) : ScrapingState()
    
    data class LoginCompleted(
        val statusCode: Int,
        val htmlPreview: String,
        val fullHtml: String
    ) : ScrapingState()
    
    data class Error(val message: String) : ScrapingState()
}
