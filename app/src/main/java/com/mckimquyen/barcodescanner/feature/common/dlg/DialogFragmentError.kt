package com.mckimquyen.barcodescanner.feature.common.dlg

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mckimquyen.barcodescanner.R

/**
 * Error BottomSheet — shows an error message with a single dismiss button.
 * No Listener interface needed: callers only show this to inform the user;
 * dismissing is the only action.
 */
class DialogFragmentError : BottomSheetDialogFragment() {

    companion object {
        private const val ERROR_MESSAGE_KEY = "ERROR_MESSAGE_KEY"

        fun newInstance(context: Context, error: Throwable?): DialogFragmentError {
            return DialogFragmentError().apply {
                arguments = Bundle().apply {
                    putString(ERROR_MESSAGE_KEY, getErrorMessage(context, error))
                }
                isCancelable = false
            }
        }

        private fun getErrorMessage(context: Context, error: Throwable?): String {
            return error?.message ?: context.getString(R.string.error_dialog_default_message)
        }
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
    ): View = inflater.inflate(R.layout.bs_alert, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val message = arguments?.getString(ERROR_MESSAGE_KEY).orEmpty()

        view.findViewById<TextView>(R.id.textViewTitle).text =
            getString(R.string.error_dialog_title)
        view.findViewById<TextView>(R.id.textViewMessage).text = message

        // OK button simply dismisses the dialog — no callback needed
        view.findViewById<Button>(R.id.buttonPositive).apply {
            text = getString(R.string.error_dialog_positive_button_text)
            setOnClickListener { dismiss() }
        }
    }
}
