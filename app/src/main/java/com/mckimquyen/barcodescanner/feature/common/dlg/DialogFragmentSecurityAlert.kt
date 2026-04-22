package com.mckimquyen.barcodescanner.feature.common.dlg

import android.app.Dialog
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

class DialogFragmentSecurityAlert : BottomSheetDialogFragment() {

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
        val listener = activity as? Listener
        val url = arguments?.getString(URL_KEY) ?: ""

        // Warning icon in red tint
        view.findViewById<ImageView>(R.id.iconView).apply {
            setImageResource(R.drawable.ic_link)
            imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.red)
        }

        view.findViewById<TextView>(R.id.textViewTitle).text =
            getString(R.string.security_alert_title)
        view.findViewById<TextView>(R.id.textViewMessage).text =
            "${getString(R.string.security_alert_message)}\n\n$url"

        view.findViewById<Button>(R.id.buttonNegative).apply {
            text = getString(R.string.action_go_back_safe)
            setOnClickListener {
                listener?.onSecurityCancel()
                dismiss()
            }
        }
        view.findViewById<Button>(R.id.buttonPositive).apply {
            text = getString(R.string.action_proceed_anyway)
            setOnClickListener {
                listener?.onSecurityProceed(url)
                dismiss()
            }
        }
    }
}
