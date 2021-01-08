package com.timmytruong.materialintervaltimer.ui.timer

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.timmytruong.materialintervaltimer.base.BaseViewModel
import com.timmytruong.materialintervaltimer.data.TimerRepository
import com.timmytruong.materialintervaltimer.model.Interval
import com.timmytruong.materialintervaltimer.model.IntervalSound
import com.timmytruong.materialintervaltimer.model.Timer
import com.timmytruong.materialintervaltimer.utils.Event
import com.timmytruong.materialintervaltimer.utils.enums.TimerState
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class TimerViewModel @Inject constructor(
    private val timerRepository: TimerRepository
) : BaseViewModel() {
    private lateinit var timer: Timer

    private lateinit var sound: IntervalSound

    private lateinit var currentInterval: Interval

    private var currentState: TimerState = TimerState.STOPPED
        set(value) {
            _timerStateEvent.value = Event(content = value)
        }

    private var shouldPlaySound: Boolean = true

    private var shouldRepeat: Boolean = false

    private var countDownTimer: CountDownTimer? = null

    private val _timeRemaining = MutableLiveData<Int>()
    val timeRemaining: LiveData<Int> get() = _timeRemaining

    private val _currentIntervalTotalTimeEvent = MutableLiveData<Event<Int>>()
    val currentIntervalTotalTimeEvent: LiveData<Event<Int>> get() = _currentIntervalTotalTimeEvent

    private val _timerStateEvent = MutableLiveData<Event<TimerState>>()
    val timerStateEvent: LiveData<Event<TimerState>> get() = _timerStateEvent

    private val _soundEvent: MutableLiveData<Event<IntervalSound>> = MutableLiveData()
    val soundEvent: LiveData<Event<IntervalSound>> get() = _soundEvent

    private val _intervals: MutableLiveData<ArrayList<Interval>> = MutableLiveData()
    val intervals: LiveData<ArrayList<Interval>> get() = _intervals

    private fun resetTimer() {
        clearCountDownTimer()
        _intervals.value = timer.timer_intervals
        setNewInterval(interval = timer.timer_intervals[0])
        currentState = TimerState.STOPPED
    }

    private fun clearCountDownTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun setNewInterval(interval: Interval) {
        currentInterval = interval
        countDownTimer = buildIntervalTimer(milliseconds = interval.interval_time_ms)
        _timeRemaining.value = currentInterval.interval_time_ms
        _currentIntervalTotalTimeEvent.value = Event(content = currentInterval.interval_time_ms)
    }

    private fun startTimer() {
        countDownTimer?.start()
    }

    private fun onIntervalFinished() {
        val newList = popFinishedInterval()
        playSound()

        if (newList.isEmpty()) {
            resetTimer()
        } else {
            _intervals.value = ArrayList(newList)
            setNewInterval(interval = newList[0])
            currentState = TimerState.PAUSED
            startTimer()
            currentState = TimerState.RUNNING
        }
    }

    private fun popFinishedInterval(): List<Interval> {
        val temp = arrayListOf<Interval>()
        temp.addAll(intervals.value ?: arrayListOf())
        if (temp.size > 0) temp.removeFirst()
        return temp
    }

    private fun playSound() {
        if (shouldPlaySound) {
            _soundEvent.value = Event(content = timer.timer_interval_sound)
        }
    }

    private fun addRepeatIntervals() {
        val temp = intervals.value
        temp?.addAll(timer.timer_intervals)
        _intervals.value = temp
    }

    private fun buildIntervalTimer(milliseconds: Int): CountDownTimer =
        object : CountDownTimer(milliseconds.toLong(), 100) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = millisUntilFinished.toInt()

                if (shouldRepeat && intervals.value?.size ?: 0 <= timer.timer_intervals_count / 2) {
                    addRepeatIntervals()
                }
            }

            override fun onFinish() {
                onIntervalFinished()
            }
        }

    fun fetchTimerFromRoom(id: Int) {
        viewModelScope.launch {
            timer = timerRepository.getTimerById(id = id)
            sound = timer.timer_interval_sound
            shouldRepeat = timer.timer_repeat
            resetTimer()
        }
    }

    fun setTimerState(newState: TimerState) {
        // If the new state is RUNNING, we start the timer built in the PAUSED or STOPPED state
        // If the new state is PAUSED, we build a new countdown timer
        // If the new state is STOPPED, we reset the timer
        when (newState) {
            TimerState.RUNNING -> {
                currentState = newState
                startTimer()
            }
            TimerState.PAUSED -> {
                clearCountDownTimer()
                countDownTimer = buildIntervalTimer(milliseconds = _timeRemaining.value ?: 0)
                currentState = newState
            }
            TimerState.STOPPED -> {
                resetTimer()
            }
        }
    }

    fun setShouldPlaySound(playSound: Boolean) {
        shouldPlaySound = playSound
    }
}