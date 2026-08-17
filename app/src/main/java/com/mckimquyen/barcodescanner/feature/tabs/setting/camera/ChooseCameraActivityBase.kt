package com.mckimquyen.barcodescanner.feature.tabs.setting.camera

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mckimquyen.barcodescanner.databinding.AChooseCameraBinding
import com.mckimquyen.barcodescanner.di.settings
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.feature.ActivityBase
import com.mckimquyen.barcodescanner.feature.startActivitySlideRight

class ChooseCameraActivityBase : ActivityBase() {
    private lateinit var binding: AChooseCameraBinding


    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ChooseCameraActivityBase::class.java)
            context.startActivitySlideRight(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AChooseCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        handleToolbarBackClicked()
    }

    override fun onResume() {
        super.onResume()
        showSelectedCamera()
        handleBackCameraButtonChecked()
        handleFrontCameraButtonChecked()
    }

    private fun showSelectedCamera() {
        val isBackCamera = settings.isBackCamera
        binding.buttonBackCamera.isChecked = isBackCamera
        binding.buttonFrontCamera.isChecked = isBackCamera.not()
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun handleToolbarBackClicked() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleBackCameraButtonChecked() {
        binding.buttonBackCamera.setCheckedChangedListener { isChecked ->
            if (isChecked) {
                binding.buttonFrontCamera.isChecked = false
            }
            settings.isBackCamera = isChecked
        }
    }

    private fun handleFrontCameraButtonChecked() {
        binding.buttonFrontCamera.setCheckedChangedListener { isChecked ->
            if (isChecked) {
                binding.buttonBackCamera.isChecked = false
            }
            settings.isBackCamera = isChecked.not()
        }
    }
}