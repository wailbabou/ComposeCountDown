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

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class TimerViewModel : ViewModel() {
    lateinit var job: Job
    private val _time = MutableLiveData("10")
    val time: LiveData<String> = _time

    private val _progress = MutableLiveData(0f)
    val progress: LiveData<Float> = _progress

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _timeTxt = MutableLiveData("10s")
    val timeTxt: LiveData<String> = _timeTxt

    private val counter = MutableLiveData(0)

    private val _editDisabled = MediatorLiveData<Boolean>()
    val editDisabled: LiveData<Boolean> = _editDisabled
    init {
        _editDisabled.addSource(counter) {
            _editDisabled.value = (it > 0)
            Log.i("livedata", "it $it : ${ it > 0}")
        }
    }
    fun onTimeChanged(newTime: String) {
        if (newTime.isNotEmpty()) {
            if (newTime.toFloat() < 3600f) {
                _time.value = newTime
                changeTextProgress(0)
            } else {
                _time.value = "3599"
                changeTextProgress(0)
            }
        } else {
            _time.value = "0"
            changeTextProgress(0)
        }
    }

    @ExperimentalCoroutinesApi
    fun startTimer() {
        _isRunning.value = true
        job = viewModelScope.launch(Dispatchers.Main) {
            tickFlow(1000L).collect {
                _progress.value = calculateProgress(counter.value!!)
                changeTextProgress(counter.value!!)
                Log.i("PROGRESS", "${calculateProgress(it)}")
                if (_progress.value!! < 1) {
                    counter.value = (counter.value!! + 1)
                } else {
                    counter.value = 0
                }
            }
        }
    }

    fun pauseTimer() {
        job.cancel()
        _isRunning.value = false
    }

    @ExperimentalCoroutinesApi
    fun restartTimer() {
        job.cancel()
        counter.value = 0
        changeTextProgress(0)
        startTimer()
    }

    @ExperimentalCoroutinesApi
    fun tickFlow(millis: Long) = callbackFlow<Int> {
        val timer = Timer()
        var time = 0
        timer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    try { offer(time) } catch (e: Exception) {}
                    time += 1
                }
            },
            0,
            millis
        )
        awaitClose {
            timer.cancel()
        }
    }

    private fun calculateProgress(tick: Int): Float {
        val total = time.value!!.toFloat()
        val calculatedProgress = tick.toFloat() / total
        if (calculatedProgress == 1f) {
            job.cancel()
            _isRunning.value = false
        }
        return calculatedProgress
    }

    private fun changeTextProgress(tick: Int) {
        val total = time.value!!.toFloat()
        val waiting = total - tick
        val seconds = (waiting % 60).toInt()
        val minutes = waiting.toInt() / 60
        val finalTxt = if (minutes > 0) {
            "${minutes}m ${seconds}s"
        } else {
            "${seconds}s"
        }
        _timeTxt.value = finalTxt
    }
}
