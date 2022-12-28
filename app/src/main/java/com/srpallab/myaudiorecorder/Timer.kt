package com.srpallab.myaudiorecorder

import android.os.Handler
import android.os.Looper

class Timer(listener: OnTimerTickListener) {
    interface  OnTimerTickListener {
        fun onTimerTick(duration: String)
    }

    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var duration = 0L
    private var delay = 100L

    init {
        runnable = Runnable{
            duration += delay
            handler.postDelayed(runnable, delay)
            listener.onTimerTick(formattedTime())
        }
    }

     fun start(){
        handler.postDelayed(runnable, delay)
    }

     fun pause(){
        handler.removeCallbacks(runnable)
    }

     fun stop(){
        handler.removeCallbacks(runnable)
        duration = 0L
    }

     private fun formattedTime(): String {
        val millis: Long = duration % 1000
        val seconds: Long = (duration / 1000) % 60
        val minutes: Long = (duration / (1000 * 60)) % 60
        val hours: Long = (duration / (1000 * 60 * 60))

        return if (hours > 0)
            "%02d:%02d:%02d.%02d".format(hours, minutes, seconds, millis/10)
        else
            "%02d:%02d.%02d".format(minutes, seconds, millis/10)
    }
}