package com.mckimquyen.barcodescanner.feature.tabs.scan

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.fragment.app.DialogFragment
import com.mckimquyen.barcodescanner.R

class DialogFragmentScanHelper : DialogFragment() {

    var onDismissCallback: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.dialog_batch_scan_helper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // X close button
        view.findViewById<View>(R.id.buttonCloseBatchHelper)?.setOnClickListener {
            Log.d("roy93~", "DialogFragmentScanHelper: close button tapped")
            dismiss()
        }

        // Play premium slide-up + scale enter animation on the dialog card
        val enterAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_dialog_enter)
        view.startAnimation(enterAnim)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                // dim the background
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                attributes = attributes.also { it.dimAmount = 0.55f }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun dismiss() {
        // Play exit animation before dismissing
        view?.let { v ->
            val exitAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_dialog_exit)
            exitAnim.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(a: android.view.animation.Animation?) {}
                override fun onAnimationRepeat(a: android.view.animation.Animation?) {}
                override fun onAnimationEnd(a: android.view.animation.Animation?) {
                    super@DialogFragmentScanHelper.dismiss()
                }
            })
            v.startAnimation(exitAnim)
        } ?: super.dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d("roy93~", "DialogFragmentScanHelper: onDismiss triggered")
        onDismissCallback?.invoke()
    }
}
