package com.srpallab.myaudiorecorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import android.provider.MediaStore.Audio
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_CODE = 200

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener {
    private lateinit var amplitudes: ArrayList<Float>
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false
    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var fileName = ""
    private var isRecording = false
    private var isPaused = false
    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var db : AppDatabase
    private var duration = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionGranted = ActivityCompat.checkSelfPermission(
            this, permissions[0]
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        } else{
            requestPermissions(permissions, REQUEST_CODE)
        }

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords"
        ).build()

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE){
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }

        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        btnRecord.setOnClickListener{
            println(isPaused)
            println(isRecording)
            when{
                isPaused -> resumeRecording()
                isRecording -> pauseRecording()
                else -> startRecording()
            }
            vibrator.vibrate(VibrationEffect.createOneShot(
                50, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }

        btnList.setOnClickListener{
            startActivity(Intent(this, GalleryActivity::class.java))
        }

        btnDone.setOnClickListener {
            stopRecording()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBG.visibility = View.VISIBLE
            fileNameInput.setText(fileName)
            Toast.makeText(this, "Record Saved", Toast.LENGTH_LONG).show()
        }

        btnCancel.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            dismiss()
        }

        bottomSheetBG.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            dismiss()
        }

        btnOkay.setOnClickListener {
            dismiss()
            save()
        }

        btnDelete.setOnClickListener {
            stopRecording()
            File("$dirPath$fileName.mp3").delete()
            Toast.makeText(this, "Record Deleted", Toast.LENGTH_LONG).show()
        }
        btnDelete.isClickable = false

    }

    private fun startRecording(){
        if(!permissionGranted){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            println(permissionGranted)
            return
        }
        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss", Locale.ENGLISH)
        val date = simpleDateFormat.format(Date())
        fileName = "audio_record_$date"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$fileName.mp3")

            try {
                prepare()
            } catch (e: IOException){
                println(e)
            }
            start()
        }

        btnRecord.setImageResource(R.drawable.ic_pause)
        isRecording = true
        isPaused = false
        timer.start()

        btnList.visibility = View.GONE
        btnDone.visibility = View.VISIBLE
    }

    private fun pauseRecording(){
        recorder.pause()
        isPaused = true
        btnRecord.setImageResource(R.drawable.ic_record)
        timer.pause()

    }

    private fun  resumeRecording(){
        recorder.resume()
        isRecording = true
        isPaused = false
        btnRecord.setImageResource(R.drawable.ic_pause)
        timer.start()
        btnDelete.isClickable = true
        btnDelete.setImageResource(R.drawable.ic_delete)

        btnList.visibility = View.GONE
        btnDone.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun stopRecording(){
        timer.stop()
        recorder.apply {
            stop()
            release()
        }
        isPaused = false
        isRecording = false

        btnList.visibility = View.VISIBLE
        btnDone.visibility = View.GONE

        btnDelete.isClickable = false
        btnDelete.setImageResource(R.drawable.ic_delete_disabled)
        btnRecord.setImageResource(R.drawable.ic_record)

        tvTimer.text = "00:00.00"
        amplitudes = waveformView.clear()
    }

    override fun onTimerTick(duration: String) {
        println(duration)
        tvTimer.text = duration
        this.duration = duration.dropLast(3)
        waveformView.addAmplitude(recorder.maxAmplitude.toFloat())
    }

    private fun  hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun dismiss(){
        bottomSheetBG.visibility = View.GONE
        hideKeyboard(fileNameInput)

        Handler(
            Looper.getMainLooper()).postDelayed({
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            },
            100)
    }

    @SuppressLint("SetTextI18n")
    private fun save(){
        val newFileName = fileNameInput.text.toString()
        if(fileName != newFileName) {
            val newFile = File("$dirPath$fileName.mp3")
            File("$dirPath$fileName.mp3").renameTo(newFile)
        }
        tvTimer.text = "00:00.00"

        var filePath = "$dirPath$fileName.mp3"
        val timestamp = Date().time
        val ampsPath = "$dirPath$fileName"

        try {
            var fileOutputStream = FileOutputStream(ampsPath)
            var out = ObjectOutputStream(fileOutputStream)
            out.writeObject(amplitudes)
            fileOutputStream.close()
            out.close()
        } catch (e: IOException){
            println(e)
        }

        var record = AudioRecord(newFileName, filePath, timestamp, duration, ampsPath)
        GlobalScope.launch {
            db.audioRecordDao().insert(record)
        }
    }
}