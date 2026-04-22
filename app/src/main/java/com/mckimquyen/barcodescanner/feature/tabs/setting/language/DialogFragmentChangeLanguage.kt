package com.mckimquyen.barcodescanner.feature.tabs.setting.language

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mckimquyen.barcodescanner.R

/**
 * BottomSheet confirmation dialog for changing app language.
 * Rotation-safe: language code is stored in arguments; callback is
 * retrieved from the Activity via interface in onAttach().
 */
class DialogFragmentChangeLanguage : BottomSheetDialogFragment() {

    interface Listener {
        fun onLanguageChangeConfirmed(languageCode: String)
        fun onLanguageChangeCancelled()
    }

    companion object {
        private const val KEY_LANGUAGE_CODE = "language_code"
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_POSITIVE = "positive"
        private const val KEY_NEGATIVE = "negative"

        fun newInstance(
            languageCode: String,
            title: String,
            message: String,
            positive: String,
            negative: String,
        ): DialogFragmentChangeLanguage {
            return DialogFragmentChangeLanguage().apply {
                arguments = Bundle().apply {
                    putString(KEY_LANGUAGE_CODE, languageCode)
                    putString(KEY_TITLE, title)
                    putString(KEY_MESSAGE, message)
                    putString(KEY_POSITIVE, positive)
                    putString(KEY_NEGATIVE, negative)
                }
                isCancelable = false
            }
        }
    }

    private var listener: Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? Listener
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
        val args = requireArguments()
        val languageCode = args.getString(KEY_LANGUAGE_CODE) ?: return

        // Use settings icon instead of delete icon for language change
        view.findViewById<ImageView>(R.id.iconView).apply {
            setImageResource(R.drawable.ic_settings)
            imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
        }

        view.findViewById<TextView>(R.id.textViewTitle).text = args.getString(KEY_TITLE)
        view.findViewById<TextView>(R.id.textViewMessage).text = args.getString(KEY_MESSAGE)

        view.findViewById<Button>(R.id.buttonNegative).apply {
            text = args.getString(KEY_NEGATIVE)
            setOnClickListener {
                listener?.onLanguageChangeCancelled()
                dismiss()
            }
        }
        view.findViewById<Button>(R.id.buttonPositive).apply {
            text = args.getString(KEY_POSITIVE)
            setOnClickListener {
                listener?.onLanguageChangeConfirmed(languageCode)
                dismiss()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
