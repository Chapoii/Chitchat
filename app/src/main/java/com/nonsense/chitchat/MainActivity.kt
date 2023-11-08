package com.nonsense.chitchat

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.Scaffold
import com.nonsense.chitchat.ui.theme.ChitchatTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val TAG = "ChatChit"

class MainActivity : ComponentActivity() {

    val msgs = mutableStateListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var text by remember { mutableStateOf("") }
            val scrollState = rememberScrollState()
            ChitchatTheme {

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(text = "聊天小程序", color = Color.Gray)
                            },
                            actions = {
                                IconButton(
                                    onClick = { msgs.clear() },
                                ) {
                                    Icon(Icons.Filled.Refresh, "")
                                }
                            },
                            backgroundColor = Color.White,
                            elevation = 12.dp
                        )
                    },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight(0.92f)
                                .fillMaxWidth()
                                .verticalScroll(
                                    scrollState
                                ),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            MessageCard(Message("Robot", "你好呀，欢迎和我聊天哦。"))
                            for (msg in msgs) {
                                MessageCard(msg)
                                LaunchedEffect(Unit) {scrollState.animateScrollBy(Float.MAX_VALUE)}
                            }

                            //msgs.map { t -> MessageCard(Message("User",  t)) }
                        }
                        //Conversation(msgs.map { txt -> Message("User", txt) })
                    },
                    bottomBar = {
                        BottomAppBar(
                            // contentColor = Color.Black,
                            backgroundColor = Color(0xffffffff)
                        ) {
                            Row() {
                                Box(
                                    modifier = Modifier
                                        .width(280.dp)
                                        .focusTarget()
                                ) {
                                    OutlinedTextField(
                                        value = text,
                                        onValueChange = {
                                            text = it
                                        },
                                        label = null,
                                        placeholder = { Text(text = "请输入文本") },
                                        maxLines = Int.MAX_VALUE,
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            unfocusedBorderColor = Color.White,   // 边框颜色
                                            focusedBorderColor = Color.White,   // 边框颜色
                                            cursorColor = Color(
                                                46,
                                                139,
                                                87
                                            ),
                                            // focusedLabelColor = Color.Blue         // Label颜色
                                        ),

                                        )
                                }

                                Box(modifier = Modifier.width(5.dp))

                                Button(
                                    onClick = {
                                        if (text != "") {
                                            msgs.add(Message("User", text))
                                            val tmp = text
                                            text = ""

                                            try {

                                                Network.service.chat(tmp).enqueue(object :
                                                    Callback<String> {

                                                    override fun onResponse(
                                                        call: Call<String>,
                                                        response: Response<String>
                                                    ) {
                                                        response.body()?.let {
                                                            msgs.add(Message("Robot", it))
                                                        }
                                                    }

                                                    override fun onFailure(
                                                        call: Call<String>,
                                                        t: Throwable
                                                    ) {
                                                        Log.d(TAG, "request wrong!")
                                                        t.printStackTrace()
                                                        msgs.add(
                                                            Message(
                                                                "Robot",
                                                                "服务器出错啦，请稍后再尝试！"
                                                            )
                                                        )
                                                    }
                                                })
                                            } catch (e: Exception) {
                                                Log.d(TAG, "生成摘要失败")
                                                e.printStackTrace()
                                                msgs.add(
                                                    Message(
                                                        "Robot",
                                                        "连接不上服务器，请检查网络后重试！"
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color(
                                            46,
                                            139,
                                            87
                                        )
                                    ),// 颜色
                                ) {
                                    Text("发送", color = Color.White)
                                }
                            }

                        }
                    }
                )
            }
        }
    }
}


data class Message(val author: String, val body: String)

@Composable
fun Conversation(messages: List<Message>) {
    LazyColumn {
        //消息列表
        items(messages) { message ->
            //卡片式消息列表
            MessageCard(message)
        }
    }

}

@Composable
fun MessageCard(msg: Message) {
    if (msg.author == "Robot") {
        RobotMessageCard(msg = msg)
    } else {
        UserMessageCard(msg = msg)
    }
}

@Composable
fun RobotMessageCard(msg: Message) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        //设置图片
        Image(
            painter = painterResource(R.drawable.robot),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colors.secondaryVariant, CircleShape)
        )
        //设置间距
        Spacer(modifier = Modifier.width(8.dp))

        // We keep track if the message is expanded or not in this
        // variable 消息是否展开的动画
        var isExpanded by remember { mutableStateOf(false) }

        // We toggle the isExpanded variable when we click on this Column
        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            //设置文本属性
            Text(
                text = msg.author,
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.subtitle2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                elevation = 1.dp,
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    // If the message is expanded, we display all its content
                    // otherwise we only display the first line
                    //maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    maxLines = Int.MAX_VALUE,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}


@Composable
fun UserMessageCard(msg: Message) {
    Row(
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {

        // We keep track if the message is expanded or not in this
        // variable 消息是否展开的动画
        var isExpanded by remember { mutableStateOf(false) }

        // We toggle the isExpanded variable when we click on this Column
        Column(
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .wrapContentWidth(),
            horizontalAlignment = Alignment.End
        ) {
            //设置文本属性
            Text(
                text = msg.author,
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.subtitle2,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                elevation = 1.dp,
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    // If the message is expanded, we display all its content
                    // otherwise we only display the first line
                    //maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    maxLines = Int.MAX_VALUE,
                    style = MaterialTheme.typography.body2
                )
            }
        }

        //设置间距
        Spacer(modifier = Modifier.width(8.dp))

        //设置图片
        Image(
            painter = painterResource(R.drawable.user),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colors.secondaryVariant, CircleShape)
        )

    }
}

@Preview(name = "Light Mode")
@Preview(
    //ui适配的主题模式
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)

@Composable
fun PreviewConversation() {
    ChitchatTheme {
        Conversation(SampleData.conversationSample)
    }
}
