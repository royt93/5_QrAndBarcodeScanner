package com.mckimquyen.barcodescanner.feature.tabs.setting.permissions

import com.mckimquyen.barcodescanner.feature.startActivitySlideRight

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mckimquyen.barcodescanner.databinding.AAllPermissionsBinding
import com.mckimquyen.barcodescanner.extension.applySystemWindowInsets
import com.mckimquyen.barcodescanner.feature.ActivityBase

class AllPermissionsActivityBase : ActivityBase() {
    private lateinit var binding: AAllPermissionsBinding


    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AllPermissionsActivityBase::class.java)
            context.startActivitySlideRight(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AAllPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}