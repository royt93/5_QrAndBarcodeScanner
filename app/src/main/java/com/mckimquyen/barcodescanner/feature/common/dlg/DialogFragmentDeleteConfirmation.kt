package com.mckimquyen.barcodescanner.feature.common.dlg

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.extension.orZero

class DialogFragmentDeleteConfirmation : DialogFragment() {

    companion object {
        private const val MESSAGE_ID_KEY = "MESSAGE_ID_KEY"

        fun newInstance(messageId: Int): DialogFragmentDeleteConfirmation {
            return DialogFragmentDeleteConfirmation().apply {
                arguments = Bundle().apply {
                    putInt(MESSAGE_ID_KEY, messageId)
                }
                isCancelable = false
            }
        }
    }

    interface Listener {
        fun onDeleteConfirmed()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = requireActivity() as? Listener ?: parentFragment as? Listener
        val messageId = arguments?.getInt(MESSAGE_ID_KEY).orZero()

        val dialog = MaterialAlertDialogBuilder(requireActivity(), R.style.DialogTheme)
            .setMessage(messageId)
            .setPositiveButton(R.string.dialog_delete_positive_button) { _, _ -> listener?.onDeleteConfirmed() }
            .setNegativeButton(R.string.dialog_delete_negative_button, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
        }

        return dialog
    }
}
