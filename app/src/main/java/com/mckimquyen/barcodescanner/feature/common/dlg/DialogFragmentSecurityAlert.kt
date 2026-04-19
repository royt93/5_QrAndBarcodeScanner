package com.mckimquyen.barcodescanner.feature.common.dlg

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.mckimquyen.barcodescanner.R

class DialogFragmentSecurityAlert : DialogFragment() {

    interface Listener {
        fun onSecurityProceed(url: String)
        fun onSecurityCancel()
    }

    companion object {
        private const val URL_KEY = "URL_KEY"

        fun newInstance(url: String): DialogFragmentSecurityAlert {
            return DialogFragmentSecurityAlert().apply {
                arguments = Bundle().apply {
                    putString(URL_KEY, url)
                }
                isCancelable = false
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = activity as? Listener
        val url = arguments?.getString(URL_KEY) ?: ""

        val dialog = MaterialAlertDialogBuilder(requireActivity(), R.style.DialogTheme)
            .setTitle(R.string.security_alert_title)
            .setMessage(getString(R.string.security_alert_message) + "\n\n" + url)
            .setCancelable(false)
            .setPositiveButton(R.string.action_go_back_safe) { _, _ ->
                listener?.onSecurityCancel()
            }
            .setNegativeButton(R.string.action_proceed_anyway) { _, _ ->
                listener?.onSecurityProceed(url)
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        }

        return dialog
    }
}
