package com.mckimquyen.barcodescanner.feature.common.dlg

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.extension.orZero

class DialogFragmentDeleteConfirmation : BottomSheetDialogFragment() {

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

    override fun getTheme(): Int = R.style.BottomSheetM3

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.isDraggable = false
        dialog.behavior.skipCollapsed = true
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.bs_confirm, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listener = requireActivity() as? Listener ?: parentFragment as? Listener
        val messageId = arguments?.getInt(MESSAGE_ID_KEY).orZero()

        view.findViewById<TextView>(R.id.textViewTitle).text =
            getString(R.string.dialog_delete_title)
        if (messageId != 0) view.findViewById<TextView>(R.id.textViewMessage).setText(messageId)

        view.findViewById<Button>(R.id.buttonNegative).apply {
            text = getString(R.string.dialog_delete_negative_button)
            setOnClickListener { dismiss() }
        }
        view.findViewById<Button>(R.id.buttonPositive).apply {
            text = getString(R.string.dialog_delete_positive_button)
            setOnClickListener {
                listener?.onDeleteConfirmed()
                dismiss()
            }
        }
    }
}
