package com.example.student_schedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.student_schedule.ui.theme.Student_scheduleTheme

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

// 定义屏幕状态
enum class Screen {
    MAIN, SETTINGS
}

@Composable
fun Showbackground(name: String, modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }
    
    when (currentScreen) {
        Screen.MAIN -> MainScreen(
            onSettingsClick = { currentScreen = Screen.SETTINGS }
        )
        Screen.SETTINGS -> SettingsScreen(
            onBackClick = { currentScreen = Screen.MAIN }
        )
    }
}

@Composable
fun MainScreen(onSettingsClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 左侧图标栏
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top
            ) {
                // 设置按钮
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(60.dp)
                        .padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.setting),
                        contentDescription = "设置"
                    )
                }

                // 编辑按钮
                IconButton(
                    onClick = { 
                        println("编辑按钮被点击")
                    },
                    modifier = Modifier
                        .size(60.dp)
                        .padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.edit),
                        contentDescription = "编辑"
                    )
                }

                // 文档按钮
                IconButton(
                    onClick = { 
                        println("文档按钮被点击")
                    },
                    modifier = Modifier
                        .size(60.dp)
                        .padding(8.dp)
                ) {
                    Icon(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // 导入数据按钮
            Button(
                onClick = { 
                    // TODO: 实现导入数据逻辑
                    println("导入数据按钮被点击")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("导入数据")
            }
            
            // 导出数据按钮
            Button(
                onClick = { 
                    // TODO: 实现导出数据逻辑
                    println("导出数据按钮被点击")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("导出数据")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "其他设置选项可以在这里添加...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
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
                content = "这是第 ${index + 1} 个备忘录的内容，可以在这里记录重要的学习安排和任务提醒...",
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

@Composable
fun GreetingPreview() {
    Student_scheduleTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Showbackground(name = "Android")
        }
    }
}