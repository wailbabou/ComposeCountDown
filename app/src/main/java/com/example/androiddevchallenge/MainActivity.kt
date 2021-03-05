/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
class MainActivity : AppCompatActivity() {
    private val timerViewModel: TimerViewModel = TimerViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                Scaffold {
                    MyApp(timerViewModel)
                }
            }
        }
    }
}

// Start building your app here!
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@Composable
fun MyApp(timerViewModel: TimerViewModel) {

    Surface {
        Box {
            // background
            Image(
                painter = painterResource(id = R.drawable.ic_background),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                contentScale = ContentScale.FillBounds
            )
            // content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .wrapContentSize(align = Alignment.Center)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.app_title),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 48.sp)
                )
                Spacer(Modifier.size(30.dp))
                TimeInputView(timerViewModel = timerViewModel)
                Spacer(Modifier.size(30.dp))
                CircularTimerView(timerViewModel = timerViewModel)
                Spacer(Modifier.size(30.dp))
                ActionsView(
                    onclick = {
                        when (it) {
                            0 -> timerViewModel.startTimer()
                            1 -> timerViewModel.pauseTimer()
                            2 -> timerViewModel.restartTimer()
                        }
                    },
                    timerViewModel = timerViewModel
                )
            }
        }
    }
}
@Composable
fun TimeInputView(timerViewModel: TimerViewModel) {
    val time: String by timerViewModel.time.observeAsState("")

    val textFieldDisabled: Boolean by timerViewModel.editDisabled.observeAsState(false)
    val textFieldBackground by animateColorAsState(
        if (textFieldDisabled) {
            Color(0xFFD5D3D0)
        } else {
            Color(0xFFffcc69)
        }
    )

    Row(
        modifier = Modifier.wrapContentSize(align = Alignment.Center)
    ) {
        BasicTextField(
            value = time,
            onValueChange = {
                if (!textFieldDisabled) {
                    timerViewModel.onTimeChanged(it)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .alignByBaseline()
                .clip(RoundedCornerShape(10.dp))
                .background(textFieldBackground)
                .padding(10.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "S",
            style = MaterialTheme.typography.caption,
            modifier = Modifier.alignByBaseline()
        )
    }
}

@Composable
fun TimeLeftView(timerViewModel: TimerViewModel) {
    val timeTxt: String by timerViewModel.timeTxt.observeAsState("0s")

    Column {
        Column(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(Color(0xFFf7a93d))
                .wrapContentSize(align = Alignment.Center)
        ) {
            Text(
                text = timeTxt,
                style = TextStyle(fontWeight = FontWeight.Bold),
                fontSize = 25.sp
            )
        }
    }
}

@Composable
fun CircularTimerView(timerViewModel: TimerViewModel) {
    val progress: Float by timerViewModel.progress.observeAsState(0f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    Box(
        modifier = Modifier
            .wrapContentSize(align = Alignment.Center),
        contentAlignment = Alignment.Center
    ) {
        TimeLeftView(timerViewModel)
        CircularProgressIndicator(
            progress = 1f,
            strokeWidth = 15.dp,
            color = Color.Black,
            modifier = Modifier.size(250.dp)
        )
        CircularProgressIndicator(
            progress = animatedProgress,
            strokeWidth = 15.dp,
            color = Color(0xFFffcc69),
            modifier = Modifier.size(250.dp)
        )
    }
}

@ExperimentalComposeUiApi
@Composable
fun ActionsView(
    onclick: (action: Int) -> Unit,
    timerViewModel: TimerViewModel
) {
    val isRunning: Boolean by timerViewModel.isRunning.observeAsState(false)
    val iconPlay = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .clickable { onclick(2) }
                .padding(10.dp)
                .wrapContentSize(Alignment.Center)
        ) {
            Icon(
                Icons.Filled.Refresh,
                null,
                modifier = Modifier
                    .size(30.dp)
            )
        }
        Spacer(Modifier.size(20.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .clickable {
                    if (!isRunning) {
                        // play
                        onclick(0)
                    } else {
                        // pause
                        onclick(1)
                    }
                }
                .padding(10.dp)
                .wrapContentSize(Alignment.Center)

        ) {
            Icon(
                iconPlay,
                null,
                modifier = Modifier
                    .size(30.dp)
            )
        }
    }
}
// --- previews
@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp(TimerViewModel())
    }
}

/*@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}*/
