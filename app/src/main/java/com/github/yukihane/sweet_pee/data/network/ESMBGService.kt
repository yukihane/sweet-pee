package com.github.yukihane.sweet_pee.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Document
import javax.inject.Inject
import javax.inject.Singleton

/**
 * e-SMBG サイト専用のスクレイピングサービス
 */
@Singleton
class ESMBGService @Inject constructor(
    private val webScrapingService: WebScrapingService
) {
    companion object {
        private const val BASE_URL = "https://cloud.e-smbg.net/"
        private const val LOGIN_FORM_SELECTOR = "#form1"
    }

    /**
     * e-SMBGサイトにアクセスしてログインフォームを取得
     */
    suspend fun getLoginForm(): ESMBGResult = withContext(Dispatchers.IO) {
        try {
            when (val result = webScrapingService.getDocument(BASE_URL)) {
                is ScrapingResult.Success -> {
                    val formInfo = webScrapingService.parseForm(result.document, LOGIN_FORM_SELECTOR)
                    if (formInfo != null) {
                        ESMBGResult.LoginFormFound(
                            document = result.document,
                            formInfo = formInfo,
                            html = result.html
                        )
                    } else {
                        ESMBGResult.Error("ログインフォーム (#form1) が見つかりません")
                    }
                }
                is ScrapingResult.Error -> {
                    ESMBGResult.Error(result.message)
                }
            }
        } catch (e: Exception) {
            ESMBGResult.Error("予期しないエラー: ${e.message}")
        }
    }

    /**
     * ログイン試行を実行
     */
    suspend fun attemptLogin(
        username: String,
        password: String,
        formInfo: FormInfo
    ): ESMBGResult = withContext(Dispatchers.IO) {
        try {
            // ログインデータの準備
            val loginData = formInfo.createPostData(
                mapOf(
                    "TxtID" to username,
                    "TxtPass" to password
                )
            )

            // POSTリクエストのURL決定
            val postUrl = if (formInfo.action.startsWith("http")) {
                formInfo.action
            } else if (formInfo.action.startsWith("/")) {
                "https://cloud.e-smbg.net${formInfo.action}"
            } else {
                "${BASE_URL}${formInfo.action}"
            }

            // ログイン実行
            when (val result = webScrapingService.postDocument(postUrl, loginData)) {
                is ScrapingResult.Success -> {
                    ESMBGResult.LoginAttempted(
                        document = result.document,
                        html = result.html,
                        statusCode = result.statusCode,
                        postData = loginData,
                        postUrl = postUrl
                    )
                }
                is ScrapingResult.Error -> {
                    ESMBGResult.Error("ログインリクエスト失敗: ${result.message}")
                }
            }
        } catch (e: Exception) {
            ESMBGResult.Error("ログイン処理でエラー: ${e.message}")
        }
    }

    /**
     * フォーム情報を分析して詳細情報を取得
     */
    fun analyzeLoginForm(document: Document): LoginFormAnalysis {
        val form = document.selectFirst(LOGIN_FORM_SELECTOR)
        
        val analysis = LoginFormAnalysis(
            formFound = form != null,
            action = form?.attr("action") ?: "",
            method = form?.attr("method") ?: "",
            fields = mutableListOf()
        )

        form?.select("input")?.forEach { input ->
            val fieldAnalysis = FieldAnalysis(
                name = input.attr("name"),
                type = input.attr("type"),
                value = input.attr("value"),
                placeholder = input.attr("placeholder"),
                required = input.hasAttr("required"),
                id = input.attr("id")
            )
            analysis.fields.add(fieldAnalysis)
        }

        return analysis
    }
}

/**
 * e-SMBG操作の結果を表すsealed class
 */
sealed class ESMBGResult {
    data class LoginFormFound(
        val document: Document,
        val formInfo: FormInfo,
        val html: String
    ) : ESMBGResult()

    data class LoginAttempted(
        val document: Document,
        val html: String,
        val statusCode: Int,
        val postData: Map<String, String>,
        val postUrl: String
    ) : ESMBGResult()

    data class Error(val message: String) : ESMBGResult()
}

/**
 * ログインフォーム分析結果
 */
data class LoginFormAnalysis(
    val formFound: Boolean,
    val action: String,
    val method: String,
    val fields: MutableList<FieldAnalysis>
)

/**
 * フィールド分析結果
 */
data class FieldAnalysis(
    val name: String,
    val type: String,
    val value: String,
    val placeholder: String,
    val required: Boolean,
    val id: String
)
