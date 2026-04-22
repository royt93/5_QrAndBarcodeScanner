package com.mckimquyen.barcodescanner.feature.tabs.setting.formats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.mckimquyen.barcodescanner.databinding.IBarcodeFormatBinding
import com.mckimquyen.barcodescanner.extension.toStringId

class FormatsAdapter(
    private val listener: Listener,
    private val formats: List<BarcodeFormat>,
    private val formatSelection: List<Boolean>,
) : RecyclerView.Adapter<FormatsAdapter.ViewHolder>() {

    interface Listener {
        fun onFormatChecked(format: BarcodeFormat, isChecked: Boolean)
    }

    override fun getItemCount(): Int {
        return formats.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = IBarcodeFormatBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.show(position)
    }

    inner class ViewHolder(private val binding: IBarcodeFormatBinding) : RecyclerView.ViewHolder(binding.root) {

        fun show(position: Int) {
            val format = formats[position]
            binding.textViewText.text = binding.root.context.resources.getString(format.toStringId())
            binding.delimiter.isInvisible = position == formats.lastIndex
            binding.checkBox.isChecked = formatSelection[position]

            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                listener.onFormatChecked(format, isChecked)
            }

            binding.root.setOnClickListener {
                binding.checkBox.toggle()
            }
        }
    }
}