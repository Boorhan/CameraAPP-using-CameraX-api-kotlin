package com.example.cameraappusingcameraxapikotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cameraappusingcameraxapikotlin.databinding.ActivityRecordVideoBinding
import java.io.File
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class recordVideo : AppCompatActivity(){

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    
    //var camera:Camera?=null
    var preview:Preview?=null
    //var videoCapture: VideoCapture?=null
    var cameraSelector:CameraSelector?=null

    var videoCapture:VideoCapture?=null
    //private val outputDirectory = getOutputDirectory()

    private lateinit var binding: ActivityRecordVideoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("custom","I am Under recordLanguage")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_video)
        binding = ActivityRecordVideoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //stop video and chronometer are invisible
        binding.stopVideo.visibility=View.INVISIBLE
        binding.viewTimer.visibility=View.INVISIBLE

        // Request camera permissions
        if (allPermissionsGranted()) {
            //binding.viewFinder.post { startCamera() }
            startCamera()
            //startRecording()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, 0)
        }
            // Request camera permissions
            if (allPermissionsGranted()) {
                //binding.viewFinder.post { startCamera() }
                startCamera()
                //startRecording()
            } else {
                ActivityCompat.requestPermissions(this@recordVideo, REQUIRED_PERMISSIONS, 0)
            }

        binding.startVideo.setOnClickListener{
            Toast.makeText(baseContext, "Video Recording Started", Toast.LENGTH_SHORT).show()
            //firstly starts the recordin
            startRecording()

            //set visibility
            binding.startVideo.visibility=View.INVISIBLE
            binding.stopVideo.visibility=View.VISIBLE
            binding.viewTimer.visibility=View.VISIBLE

            //choronometer start
            binding.viewTimer.base = SystemClock.elapsedRealtime()
            binding.viewTimer.start()
        }

        binding.stopVideo.setOnClickListener{

            Toast.makeText(baseContext, "Video Recording Finished", Toast.LENGTH_SHORT).show()
            stopRecording()

            binding.startVideo.visibility=View.VISIBLE
            binding.stopVideo.visibility=View.INVISIBLE
            binding.viewTimer.visibility=View.INVISIBLE

            binding.viewTimer.base = SystemClock.elapsedRealtime()
            binding.viewTimer.stop()
            //timer.cancel()
        }

        binding.clickCamera.setOnClickListener{
            val intent = Intent(this@recordVideo, MainActivity::class.java)
            startActivity(intent)
        }
    }


    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        Log.i("custom","I am Under StartCamera")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider=cameraProviderFuture.get()
            preview=Preview.Builder().build()
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            // imageCapture= ImageCapture.Builder().build()

            cameraSelector= CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            videoCapture = VideoCapture.Builder().apply {
                setTargetRotation(binding.viewFinder.display.rotation)
                setCameraSelector(cameraSelector!!)
            }.build()
            // videoCapture = VideoCapture(videoCaptureConfig)

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector!!, preview, videoCapture)
        },ContextCompat.getMainExecutor(this))

    }

    private fun allPermissionsGranted(): Boolean {
        for(permission in REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Request camera permissions
        if (allPermissionsGranted()) {
            //binding.viewFinder.post { startCamera() }
            startCamera()
            //startRecording()
        } else {
            Toast.makeText(this, "Please accept the permission", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun startRecording() {
        val videoFile = File(
            externalMediaDirs.firstOrNull(),
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US
            ).format(System.currentTimeMillis()) + ".mp4")
        val outputOptions = VideoCapture.OutputFileOptions.Builder(videoFile).build()

        videoCapture?.startRecording(outputOptions, ContextCompat.getMainExecutor(this), object: VideoCapture.OnVideoSavedCallback {
            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                Log.i("custom", "Video capture failed: $message")
            }

            override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(videoFile)
                val msg = "Video capture succeeded: $savedUri"
                //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                Log.i("custom", msg)
            }
        })
    }

    @SuppressLint("RestrictedApi")
    private fun stopRecording() {
        videoCapture?.stopRecording()
    }
}




