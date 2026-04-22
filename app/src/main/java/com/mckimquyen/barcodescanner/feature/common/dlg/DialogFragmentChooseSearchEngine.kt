package com.mckimquyen.barcodescanner.feature.common.dlg

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.model.SearchEngine

class DialogFragmentChooseSearchEngine : BottomSheetDialogFragment() {

    companion object {
        private val ITEMS = arrayOf(
            SearchEngine.BING,
            SearchEngine.DUCK_DUCK_GO,
            SearchEngine.GOOGLE,
            SearchEngine.QWANT,
            SearchEngine.STARTPAGE,
            SearchEngine.YAHOO,
            SearchEngine.YANDEX
        )
    }

    interface Listener {
        fun onSearchEngineSelected(searchEngine: SearchEngine)
    }

    override fun getTheme(): Int = R.style.BottomSheetM3

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.skipCollapsed = true
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.bs_search_engine, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listener = requireActivity() as? Listener

        val itemLabels = arrayOf(
            getString(R.string.activity_choose_search_engine_bing),
            getString(R.string.activity_choose_search_engine_duck_duck_go),
            getString(R.string.activity_choose_search_engine_google),
            getString(R.string.activity_choose_search_engine_qwant),
            getString(R.string.activity_choose_search_engine_startpage),
            getString(R.string.activity_choose_search_engine_yahoo),
            getString(R.string.activity_choose_search_engine_yandex)
        )

        val listContainer = view.findViewById<LinearLayout>(R.id.listContainer)
        itemLabels.forEachIndexed { index, label ->
            // Use a clean text-only row — NOT lo_settings_button which has a Switch
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.lo_bs_list_item, listContainer, false) as TextView
            itemView.text = label
            itemView.setOnClickListener {
                listener?.onSearchEngineSelected(ITEMS[index])
                dismiss()
            }
            listContainer.addView(itemView)
        }

        view.findViewById<Button>(R.id.buttonCancel).setOnClickListener { dismiss() }
    }
}
