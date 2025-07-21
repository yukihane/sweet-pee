package com.github.yukihane.sweet_pee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.yukihane.sweet_pee.presentation.viewmodel.ScrapingState
import com.github.yukihane.sweet_pee.presentation.viewmodel.ScrapingViewModel
import com.github.yukihane.sweet_pee.ui.theme.SweetpeeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SweetpeeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScrapingScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapingScreen(
    modifier: Modifier = Modifier,
    viewModel: ScrapingViewModel = hiltViewModel()
) {
    val scrapingState by viewModel.scrapingState.collectAsState()
    val logs by viewModel.logs.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // タイトル
        Text(
            text = "e-SMBG スクレイピングテスト",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ボタンエリア
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.fetchLoginForm() },
                enabled = scrapingState !is ScrapingState.Loading
            ) {
                Text("1. フォーム取得")
            }

            Button(
                onClick = { viewModel.attemptLogin() },
                enabled = scrapingState is ScrapingState.FormLoaded
            ) {
                Text("2. ログイン試行")
            }

            Button(
                onClick = { viewModel.reset() }
            ) {
                Text("リセット")
            }

            Button(
                onClick = { viewModel.clearLogs() }
            ) {
                Text("ログクリア")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 状態表示
        StatusCard(scrapingState = scrapingState)

        Spacer(modifier = Modifier.height(16.dp))

        // ログ表示
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ログ (${logs.size}件)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(logs) { log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(scrapingState: ScrapingState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "現在の状態",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when (scrapingState) {
                is ScrapingState.Idle -> {
                    Text(
                        text = "待機中",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is ScrapingState.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = scrapingState.message,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                is ScrapingState.FormLoaded -> {
                    Text(
                        text = "✅ ログインフォーム取得完了",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "フィールド数: ${scrapingState.analysis.fields.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                is ScrapingState.LoginCompleted -> {
                    Text(
                        text = "✅ ログイン試行完了",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "ステータス: ${scrapingState.statusCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                is ScrapingState.Error -> {
                    Text(
                        text = "❌ エラー",
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = scrapingState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScrapingScreenPreview() {
    SweetpeeTheme {
        // プレビュー用の簡単な表示
        Text("スクレイピング画面プレビュー")
    }
}
