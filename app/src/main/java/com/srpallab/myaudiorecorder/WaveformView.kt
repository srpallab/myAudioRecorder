package com.srpallab.myaudiorecorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

@Suppress("UNCHECKED_CAST")
class WaveformView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var paint = Paint()
    private var  amplitudes = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()

    private var radius = 6f
    private var w = 9f
    private var d = 6f
    private var screenWidth = 0f
    private var screenHeight = 400f

    private var maxSpikes = 0

    init {
        paint.color = Color.rgb(244, 81, 30)
        screenWidth = resources.displayMetrics.widthPixels.toFloat()
        maxSpikes = (screenWidth / (w+d)).toInt()
    }

    fun addAmplitude(amp: Float){
        val norm = (amp.toInt() / 7).coerceAtMost(400).toFloat()
        amplitudes.add(norm)
        spikes.clear()
        val amps = amplitudes.takeLast(maxSpikes)
        for (i in amps.indices){
            val left = screenWidth - i * (w + d)
            val top = screenHeight / 2 - amps[i] / 2
            val right = left + w
            val bottom = top + amps[i]

            spikes.add(RectF(left, top, right, bottom))
        }
        invalidate()
    }

    fun clear() : ArrayList<Float>{
        val amps = amplitudes.clone() as ArrayList<Float>
        amplitudes.clear()
        spikes.clear()
        invalidate()

        return  amps
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        spikes.forEach{
            canvas?.drawRoundRect(
                it,
                radius,
                radius,
                paint
            )
        }

    }
}