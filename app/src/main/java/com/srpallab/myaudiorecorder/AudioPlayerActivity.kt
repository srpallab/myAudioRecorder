package com.srpallab.myaudiorecorder

import android.media.MediaPlayer
import android.media.PlaybackParams
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_audio_player.*
import java.text.DecimalFormat
import java.text.NumberFormat

class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvTitleFileName : TextView

    private lateinit var tvTrackProgress : TextView
    private lateinit var tvTrackDuration : TextView

    private lateinit var btnPlay : ImageButton
    private lateinit var btnBackward : ImageButton
    private lateinit var btnForward : ImageButton
    private lateinit var seekChip : Chip
    private lateinit var seekBar : SeekBar

    private lateinit var runnable : Runnable
    private lateinit var handler: Handler
    private val delay = 1000L
    private val jumpDelay = 1000
    private var speedChipValue = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        val filePath = intent.getStringExtra("filepath")
        val fileName = intent.getStringExtra("filename")

        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(filePath)
            prepare()
        }

        toolbar = findViewById(R.id.toolBar)
        tvTitleFileName = findViewById(R.id.tvTitleFileName)
        tvTitleFileName.text = fileName
        btnPlay = findViewById(R.id.btnPlayPlayer)
        btnForward = findViewById(R.id.btnForwardPlayer)
        btnBackward = findViewById(R.id.btnBackwardPlayer)
        seekChip = findViewById(R.id.chipSpeed)
        seekBar = findViewById(R.id.seekBar)
        tvTrackProgress = findViewById(R.id.tvTrackProgress)
        tvTrackDuration = findViewById(R.id.tvTrackDuration)

        tvTrackDuration.text = dateFormat(mediaPlayer.duration)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        btnPlay.setOnClickListener {
            playPausePlayer()
        }

        btnForward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition + jumpDelay)
            seekBar.progress += jumpDelay
        }

        btnBackward.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition - jumpDelay)
            seekBar.progress -= jumpDelay
        }

        chipSpeed.setOnClickListener {
            if (speedChipValue != 2.0f){
                speedChipValue += 0.5f
            } else {
                speedChipValue -= 0.5f
            }

            mediaPlayer.playbackParams = PlaybackParams().setSpeed(speedChipValue)
            chipSpeed.text = "x $speedChipValue"
        }

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser){
                        mediaPlayer.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            }
        )

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable{
            seekBar.progress = mediaPlayer.currentPosition
            tvTrackProgress.text = dateFormat(mediaPlayer.currentPosition )
            handler.postDelayed(runnable, delay)
        }

        playPausePlayer()
        seekBar.max = mediaPlayer.duration
        mediaPlayer.setOnCompletionListener {
            btnPlay.background = ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_play_circle,
                theme
            )
            handler.removeCallbacks(runnable)
        }
    }

    private fun  playPausePlayer() {
        if(!mediaPlayer.isPlaying){
            mediaPlayer.start()
            btnPlay.background = ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_pause_circle,
                theme
            )
            handler.postDelayed(runnable, delay)
        } else {
            mediaPlayer.pause()
            btnPlay.background = ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_play_circle,
                theme
            )
            handler.removeCallbacks(runnable)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer.stop()
        mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }

    private fun dateFormat(duration: Int): String{
        val durationSeconds = duration/1000
        val seconds = durationSeconds % 60
        val minutes = (durationSeconds/60 % 60)
        val hour = ((durationSeconds - minutes * 60)/360).toInt()

        val f: NumberFormat = DecimalFormat("00")
        var str = "$minutes:${f.format(seconds)}"
        if(hour > 0){
            str = "$hour:$str"
        }
        return  str
    }
}