package com.mckimquyen.barcodescanner.feature.tabs.scan

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mckimquyen.barcodescanner.R

/**
 * Material You Bottom Sheet dialog for Batch Scan onboarding.
 * Uses BottomSheetDialogFragment for the native M3 slide-up animation,
 * drag-to-dismiss gesture, scrim dimming, and 28dp top rounded corners.
 */
class DialogFragmentScanHelper : BottomSheetDialogFragment() {

    var onDismissCallback: (() -> Unit)? = null

    override fun getTheme(): Int = R.style.BottomSheetM3

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        // Expand fully on open — don't stop at half-expanded state
        dialog.behavior.apply {
            skipCollapsed = true
            isDraggable = true
        }
        Log.d("roy93~", "DialogFragmentScanHelper: BottomSheetDialog created")
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.dialog_batch_scan_helper, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // X close button
        view.findViewById<View>(R.id.buttonCloseBatchHelper)?.setOnClickListener {
            Log.d("roy93~", "DialogFragmentScanHelper: X button tapped → dismiss")
            dismiss()
        }

        // "Got It" primary button — same as closing
        view.findViewById<View>(R.id.buttonGotIt)?.setOnClickListener {
            Log.d("roy93~", "DialogFragmentScanHelper: Got It tapped → dismiss")
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d("roy93~", "DialogFragmentScanHelper: onDismiss triggered → invoking callback")
        onDismissCallback?.invoke()
        onDismissCallback = null
    }
}
