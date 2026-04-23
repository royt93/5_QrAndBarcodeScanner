package com.mckimquyen.barcodescanner.feature.tabs.history

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.barcodescanner.databinding.IBarcodeHistoryBinding
import com.mckimquyen.barcodescanner.extension.toColorId
import com.mckimquyen.barcodescanner.extension.toImageId
import com.mckimquyen.barcodescanner.extension.toStringId
import com.mckimquyen.barcodescanner.model.Barcode
import java.text.SimpleDateFormat
import java.util.Locale

class AdapterBarcodeHistory(private val listener: Listener) :
    PagingDataAdapter<Barcode, AdapterBarcodeHistory.ViewHolder>(DiffUtilCallback) {

    interface Listener {
        fun onBarcodeClicked(barcode: Barcode)
    }

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = IBarcodeHistoryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.also { barcode ->
            holder.show(
                barcode = barcode,
                isLastItem = position == itemCount.dec()
            )
        }
    }

    inner class ViewHolder(private val binding: IBarcodeHistoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun show(
            barcode: Barcode,
            isLastItem: Boolean,
        ) {
            showDate(barcode)
            showFormat(barcode)
            showText(barcode)
            showImage(barcode)
            showImageBackgroundColor(barcode)
            showIsFavorite(barcode)
            showOrHideDelimiter(isLastItem)
            setClickListener(barcode)
        }

        private fun showDate(barcode: Barcode) {
            binding.textViewDate.text = dateFormatter.format(barcode.date)
        }

        private fun showFormat(barcode: Barcode) {
            binding.textViewFormat.setText(barcode.format.toStringId())
        }

        private fun showText(barcode: Barcode) {
            binding.textViewText.text = barcode.name ?: barcode.formattedText
        }

        private fun showImage(barcode: Barcode) {
            val imageId = barcode.schema.toImageId() ?: barcode.format.toImageId()
            val image = AppCompatResources.getDrawable(binding.root.context, imageId)
            binding.imageViewSchema.setImageDrawable(image)
        }

        private fun showImageBackgroundColor(barcode: Barcode) {
            val colorId = barcode.format.toColorId()
//            val color = binding.root.context.resources.getColor(colorId)
            val color = ContextCompat.getColor(binding.root.context, colorId)
            (binding.layoutImage.background.mutate() as GradientDrawable).setColor(color)
        }

        private fun showIsFavorite(barcode: Barcode) {
            binding.imageViewFavorite.isVisible = barcode.isFavorite
        }

        private fun showOrHideDelimiter(isLastItem: Boolean) {
            binding.delimiter.isInvisible = isLastItem
        }

        private fun setClickListener(barcode: Barcode) {
            binding.root.setOnClickListener {
                listener.onBarcodeClicked(barcode)
            }
        }
    }

    private object DiffUtilCallback : DiffUtil.ItemCallback<Barcode>() {

        override fun areItemsTheSame(
            oldItem: Barcode,
            newItem: Barcode,
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Barcode,
            newItem: Barcode,
        ): Boolean {
            return oldItem == newItem
        }
    }
}
