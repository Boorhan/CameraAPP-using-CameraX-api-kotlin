package com.example.cameraappusingcameraxapikotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Camera
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraController
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cameraappusingcameraxapikotlin.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {

    // implementation "androidx.camera:camera-view:1.0.0-alpha27"
    private lateinit var binding: ActivityMainBinding

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

    private var camSelectorGlobal = CameraSelector.DEFAULT_BACK_CAMERA


    var camera:Camera?=null
    var preview:Preview?=null
    var imageCapture:ImageCapture?=null
    var cameraSelector:CameraSelector?=null
    var cameraController: CameraController?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Request camera permissions
        if (allPermissionsGranted()) {
            //binding.viewFinder.post { startCamera() }
            startCamera()
            //startRecording()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, 0)
        }

        binding.btnVideo.setOnClickListener {
            Log.i("custom", "Ekhon jabo")
            val intent = Intent(this@MainActivity, recordVideo::class.java)
            startActivity(intent)
        }

        binding.btnCapture.setOnClickListener{
            savePhoto()
        }

        binding.btnCameraFlip.setOnClickListener{
            toggleCamera()
        }

        // orientation handle
        val orientationEventListener = object :OrientationEventListener(this as Context) {
            override fun onOrientationChanged(orientation : Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation : Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture?.targetRotation = rotation
            }
        }
        orientationEventListener.enable()
    }


    private fun toggleCamera() {

        if (camSelectorGlobal == CameraSelector.DEFAULT_FRONT_CAMERA) {
            camSelectorGlobal = CameraSelector.DEFAULT_BACK_CAMERA
            Toast.makeText(this, "Switched to Back-Camera", Toast.LENGTH_SHORT).show()
        } else if (camSelectorGlobal == CameraSelector.DEFAULT_BACK_CAMERA) {
            camSelectorGlobal = CameraSelector.DEFAULT_FRONT_CAMERA
            Toast.makeText(this, "Switched to Front-Camera", Toast.LENGTH_SHORT).show()
        }
        startCamera()

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



    private fun savePhoto() {
        //save the photo

        //val photoFile = File(externalMediaDirs.firstOrNull(), "CameraApp-${System.currentTimeMillis()}.jpg")
        val photoFile = File(externalMediaDirs.firstOrNull(), "CameraApp-${System.currentTimeMillis()}.jpg")
        val outputFile=ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture?.takePicture(outputFile,ContextCompat.getMainExecutor(this), object:ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Toast.makeText(applicationContext, "Image Saved", Toast.LENGTH_LONG).show()
            }

            override fun onError(exception: ImageCaptureException) {
                TODO("Not yet implemented")
            }

        })

    }


    @SuppressLint("WrongConstant")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider=cameraProviderFuture.get()
            preview=Preview.Builder().build()
            preview?.setSurfaceProvider(binding.cameraView?.surfaceProvider)
            imageCapture=ImageCapture.Builder().build()
            cameraSelector = CameraSelector.Builder().build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, camSelectorGlobal,preview,imageCapture)
                .also {camera}
        },ContextCompat.getMainExecutor(this))

    }
}