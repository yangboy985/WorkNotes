package com.study.camera

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.study.camera.databinding.ActivityMainBinding
import permissions.dispatcher.*

@RuntimePermissions
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun showCamera() {
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    fun showRationaleForCamera(request: PermissionRequest) {
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraDenied() {
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onCameraNeverAskAgain() {
    }
}