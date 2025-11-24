package com.example.student_schedule

import androidx.compose.ui.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.student_schedule.ui.theme.Student_scheduleTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Student_scheduleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Showbackground(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Showbackground(name: String, modifier: Modifier = Modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 左侧图标栏
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.setting),
                        contentDescription = "设置"
                    )
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.edit),
                        contentDescription = "编辑"
                    )
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.document),
                        contentDescription = "文档"
                    )
                }
            }

            // 右侧可滑动列表
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                MemoList()
            }
        }
    }
}

@Composable
fun MemoList() {
    // 模拟备忘录数据
    val memoItems = remember {
        List(20) { index ->
            MemoItem(
                id = index,
                title = "备忘录 ${index + 1}",
                content = "这是第 ${index + 1} 个备忘录的内容...",
                time = "2024-01-${String.format("%02d", (index % 30) + 1)}"
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(memoItems) { memo ->
            MemoCard(memoItem = memo)
        }
    }
}

@Composable
fun MemoCard(memoItem: MemoItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = memoItem.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = memoItem.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = memoItem.time,
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
        }
    }
}

// 数据类
data class MemoItem(
    val id: Int,
    val title: String,
    val content: String,
    val time: String
)