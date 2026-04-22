package com.mckimquyen.barcodescanner.feature.common.dlg

import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.mckimquyen.barcodescanner.R

/**
 * BottomSheet dialog for editing a barcode name.
 *
 * KEY: The app base theme is Theme.AppCompat (not MaterialComponents).
 * TextInputLayout requires MaterialComponents theme → inflate with ContextThemeWrapper.
 *
 * KEYBOARD: SOFT_INPUT_ADJUST_RESIZE + match_parent layout ensures
 * the button row stays pinned above the keyboard at all times.
 */
class DialogFragmentEditBarcodeName : BottomSheetDialogFragment() {

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

    // View refs stored to allow cleanup in onDestroyView
    private var keyboardRunnable: Runnable? = null
    private var editText: TextInputEditText? = null

    override fun getTheme(): Int = R.style.BottomSheetM3

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.skipCollapsed = true
        // ADJUST_RESIZE: window shrinks when keyboard opens → buttons stay above keyboard
        dialog.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        return dialog
    }

    /**
     * Wrap the context with Theme.MaterialComponents before inflating,
     * so that TextInputLayout can resolve its required theme attributes.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val materialContext = ContextThemeWrapper(
            requireContext(),
            com.google.android.material.R.style.Theme_MaterialComponents_DayNight
        )
        val materialInflater = inflater.cloneInContext(materialContext)
        return materialInflater.inflate(R.layout.bs_edit_barcode_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listener = requireActivity() as? Listener
        val name = arguments?.getString(NAME_KEY).orEmpty()

        view.findViewById<TextView>(R.id.textViewTitle).text =
            getString(R.string.dialog_edit_barcode_name_title)

        editText = view.findViewById<TextInputEditText>(R.id.editTextBarcodeName).apply {
            setText(name)
            setSelection(name.length)
            requestFocus()
            // "Done" key on keyboard triggers Save — same as tapping buttonPositive
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val newName = text?.toString().orEmpty()
                    listener?.onNameConfirmed(newName)
                    dismiss()
                    true
                } else false
            }
        }

        // Show soft keyboard — store runnable so we can cancel it in onDestroyView
        keyboardRunnable = Runnable {
            val et = editText ?: return@Runnable
            if (!isAdded || context == null) return@Runnable
            val imm = requireContext().getSystemService(InputMethodManager::class.java)
            imm?.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
        }
        editText?.postDelayed(keyboardRunnable!!, 200)

        view.findViewById<Button>(R.id.buttonNegative).apply {
            text = getString(R.string.dialog_edit_barcode_name_negative_button)
            setOnClickListener { dismiss() }
        }
        view.findViewById<Button>(R.id.buttonPositive).apply {
            text = getString(R.string.dialog_edit_barcode_name_positive_button)
            setOnClickListener {
                val newName = editText?.text?.toString().orEmpty()
                listener?.onNameConfirmed(newName)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        // Cancel pending keyboard runnable and release view references to avoid leak
        keyboardRunnable?.let { editText?.removeCallbacks(it) }
        keyboardRunnable = null
        editText = null
        super.onDestroyView()
    }
}
