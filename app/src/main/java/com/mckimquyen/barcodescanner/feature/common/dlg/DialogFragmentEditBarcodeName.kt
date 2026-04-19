package com.mckimquyen.barcodescanner.feature.common.dlg

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.mckimquyen.barcodescanner.R
import kotlinx.android.synthetic.main.dlg_edit_barcode_name.view.*

class DialogFragmentEditBarcodeName : DialogFragment() {

    interface Listener {
        fun onNameConfirmed(name: String)
    }

    companion object {
        private const val NAME_KEY = "NAME_KEY"

        fun newInstance(name: String?): DialogFragmentEditBarcodeName {
            return DialogFragmentEditBarcodeName().apply {
                arguments = Bundle().apply {
                    putString(NAME_KEY, name)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = requireActivity() as? Listener
        val name = arguments?.getString(NAME_KEY).orEmpty()

        val view = LayoutInflater
            .from(requireContext())
            .inflate(R.layout.dlg_edit_barcode_name, null, false)

        val dialog = MaterialAlertDialogBuilder(requireActivity(), R.style.DialogTheme)
            .setTitle(R.string.dialog_edit_barcode_name_title)
            .setView(view)
            .setPositiveButton(R.string.dialog_edit_barcode_name_positive_button) { _, _ ->
                val newName = view.editTextBarcodeName.text.toString()
                listener?.onNameConfirmed(newName)
            }
            .setNegativeButton(R.string.dialog_edit_barcode_name_negative_button, null)
            .create()

        dialog.setOnShowListener {
            initNameEditText(view.editTextBarcodeName, name)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        }

        return dialog
    }

    private fun initNameEditText(editText: EditText, name: String) {
        editText.apply {
            setText(name)
            setSelection(name.length)
            requestFocus()
        }

        val manager = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        manager?.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}
