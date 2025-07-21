package com.github.yukihane.sweet_pee.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Webスクレイピング用サービス
 * HTTP通信とHTML解析の基本機能を提供
 */
@Singleton
class WebScrapingService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .cookieJar(CookieJar.NO_COOKIES) // 今後必要に応じてCookie対応
        .build()

    /**
     * GET リクエストを実行してHTMLドキュメントを取得
     */
    suspend fun getDocument(url: String): ScrapingResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext ScrapingResult.Error("HTTP Error: ${response.code}")
            }

            val html = response.body?.string() ?: ""
            val document = Jsoup.parse(html, url)
            
            ScrapingResult.Success(document, html, response.code)
        } catch (e: IOException) {
            ScrapingResult.Error("Network Error: ${e.message}")
        } catch (e: Exception) {
            ScrapingResult.Error("Parsing Error: ${e.message}")
        }
    }

    /**
     * POST リクエストを実行してHTMLドキュメントを取得
     */
    suspend fun postDocument(
        url: String,
        formData: Map<String, String>,
        headers: Map<String, String> = emptyMap()
    ): ScrapingResult = withContext(Dispatchers.IO) {
        try {
            val formBody = FormBody.Builder().apply {
                formData.forEach { (key, value) ->
                    add(key, value)
                }
            }.build()

            val requestBuilder = Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")

            headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }

            val request = requestBuilder.build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext ScrapingResult.Error("HTTP Error: ${response.code}")
            }

            val html = response.body?.string() ?: ""
            val document = Jsoup.parse(html, url)
            
            ScrapingResult.Success(document, html, response.code)
        } catch (e: IOException) {
            ScrapingResult.Error("Network Error: ${e.message}")
        } catch (e: Exception) {
            ScrapingResult.Error("Parsing Error: ${e.message}")
        }
    }

    /**
     * フォーム要素を解析してフィールド情報を取得
     */
    fun parseForm(document: Document, formSelector: String): FormInfo? {
        val form = document.selectFirst(formSelector) ?: return null
        
        val action = form.attr("action")
        val method = form.attr("method").lowercase()
        
        val fields = mutableMapOf<String, String>()
        val hiddenFields = mutableMapOf<String, String>()
        
        // input要素の解析
        form.select("input").forEach { input ->
            val name = input.attr("name")
            val value = input.attr("value")
            val type = input.attr("type").lowercase()
            
            if (name.isNotEmpty()) {
                if (type == "hidden") {
                    hiddenFields[name] = value
                } else {
                    fields[name] = value
                }
            }
        }
        
        // select要素の解析
        form.select("select").forEach { select ->
            val name = select.attr("name")
            if (name.isNotEmpty()) {
                val selectedOption = select.selectFirst("option[selected]")
                    ?: select.selectFirst("option")
                fields[name] = selectedOption?.attr("value") ?: ""
            }
        }
        
        // textarea要素の解析
        form.select("textarea").forEach { textarea ->
            val name = textarea.attr("name")
            if (name.isNotEmpty()) {
                fields[name] = textarea.text()
            }
        }
        
        return FormInfo(
            action = action,
            method = method,
            fields = fields,
            hiddenFields = hiddenFields
        )
    }
}

/**
 * スクレイピング結果を表すsealed class
 */
sealed class ScrapingResult {
    data class Success(
        val document: Document,
        val html: String,
        val statusCode: Int
    ) : ScrapingResult()
    
    data class Error(val message: String) : ScrapingResult()
}

/**
 * フォーム情報を表すデータクラス
 */
data class FormInfo(
    val action: String,
    val method: String,
    val fields: Map<String, String>,
    val hiddenFields: Map<String, String>
) {
    /**
     * 指定されたフィールド値でPOSTデータを作成
     */
    fun createPostData(fieldValues: Map<String, String>): Map<String, String> {
        return hiddenFields + fields + fieldValues
    }
}
