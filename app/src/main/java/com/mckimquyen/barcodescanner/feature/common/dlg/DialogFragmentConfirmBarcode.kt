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
import com.mckimquyen.barcodescanner.extension.toStringId
import com.mckimquyen.barcodescanner.model.Barcode

class DialogFragmentConfirmBarcode : BottomSheetDialogFragment() {

    interface Listener {
        fun onBarcodeConfirmed(barcode: Barcode)
        fun onBarcodeDeclined()
    }

    companion object {
        private const val BARCODE_KEY = "BARCODE_FORMAT_MESSAGE_ID_KEY"

        fun newInstance(barcode: Barcode): DialogFragmentConfirmBarcode {
            return DialogFragmentConfirmBarcode().apply {
                arguments = Bundle().apply {
                    putSerializable(BARCODE_KEY, barcode)
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
        val listener = parentFragment as? Listener
        val barcode = arguments?.getSerializable(BARCODE_KEY) as? Barcode
            ?: throw IllegalArgumentException("No barcode passed")

        // Use a scan icon tinted with accent color
        view.findViewById<ImageView>(R.id.iconView).apply {
            setImageResource(R.drawable.ic_scan)
            imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
        }

        view.findViewById<TextView>(R.id.textViewTitle).text =
            getString(R.string.dialog_confirm_barcode_title)
        view.findViewById<TextView>(R.id.textViewMessage).setText(barcode.format.toStringId())

        view.findViewById<Button>(R.id.buttonNegative).apply {
            text = getString(R.string.dialog_confirm_barcode_negative_button)
            setOnClickListener {
                listener?.onBarcodeDeclined()
                dismiss()
            }
        }
        view.findViewById<Button>(R.id.buttonPositive).apply {
            text = getString(R.string.dialog_confirm_barcode_positive_button)
            setOnClickListener {
                listener?.onBarcodeConfirmed(barcode)
                dismiss()
            }
        }
    }
}
