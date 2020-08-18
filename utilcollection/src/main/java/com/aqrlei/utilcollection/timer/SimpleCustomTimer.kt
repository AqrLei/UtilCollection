package com.aqrlei.utilcollection.timer

import android.os.Handler
import android.os.Looper

/**
 * created by AqrLei on 2020/7/21
 */
class SimpleCustomTimer(
    private val maxMillis: Long,
    private val onTimerListener: OnTimerListener,
    private var intervalTime: Long = DEFAULT_INTERVAL_TIME,
    private val isCountDown: Boolean = true) : Runnable {
    companion object {
        private const val DEFAULT_INTERVAL_TIME = 1000L
        fun getInstance(
            maxMillis: Long,
            onTimer: (Long) -> Unit,
            onStart: (() -> Unit)? = null,
            onComplete: (() -> Unit)? = null): SimpleCustomTimer {
            return SimpleCustomTimer(maxMillis, object : SimpleCustomTimer.OnTimerListener {
                override fun onStart(countDown: Boolean) {
                    onStart?.invoke()
                }

                override fun onNext(currentTime: Long, countDown: Boolean) {
                    onTimer(currentTime)
                }

                override fun onComplete(countDown: Boolean) {
                    onComplete?.invoke()
                }
            })
        }
    }

    private var isRunning: Boolean = false
    var currentTime: Long = if (isCountDown) maxMillis else 0
        private set
    private var handler: Handler? = Handler(Looper.getMainLooper())

    override fun run() {
        if (isCountDown) {
            countDown()
        } else {
            countUp()
        }
    }

    private fun countDown() {
        currentTime -= intervalTime
        onTimerListener.onNext(currentTime.takeIf { it >= 0 } ?: 0, isCountDown)
        when {
            currentTime <= 0 -> complete()

            currentTime < intervalTime -> handler?.postDelayed(this, currentTime)

            else -> handler?.postDelayed(this, intervalTime)
        }
    }

    private fun countUp() {
        currentTime += intervalTime
        val remainTime = maxMillis - currentTime
        when {
            currentTime >= maxMillis -> complete()
            remainTime < intervalTime -> handler?.postDelayed(this, remainTime)
            else -> handler?.postDelayed(this, intervalTime)
        }
        onTimerListener.onNext(currentTime.takeIf { it <= maxMillis } ?: maxMillis, isCountDown)
    }

    fun start() {
        onTimerListener.onStart(isCountDown)
        isRunning = true
        handler?.postDelayed(this, intervalTime)
    }

    fun reset() {
        cancel()
        currentTime = if (isCountDown) maxMillis else 0
    }

    fun pause() {
        cancel()
    }

    fun cancel() {
        isRunning = false
        handler?.removeCallbacks(this)
    }

    private fun complete() {
        reset()
        onTimerListener.onComplete(isCountDown)
    }

    interface OnTimerListener {
        fun onStart(countDown: Boolean)
        fun onNext(currentTime: Long, countDown: Boolean)
        fun onComplete(countDown: Boolean)
    }
}