package com.example.cameraappusingcameraxapikotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cameraappusingcameraxapikotlin.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    // implementation "androidx.camera:camera-view:1.0.0-alpha27"
    private lateinit var binding: ActivityMainBinding

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

    var camera:Camera?=null
    var preview:Preview?=null
    var imageCapture:ImageCapture?=null
    var cameraSelector:CameraSelector?=null
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


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider=cameraProviderFuture.get()
            preview=Preview.Builder().build()
            preview?.setSurfaceProvider(binding.cameraView.surfaceProvider)
            imageCapture=ImageCapture.Builder().build()

            cameraSelector= CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector!!,preview,imageCapture)
                .also {camera}
        },ContextCompat.getMainExecutor(this))

    }
}