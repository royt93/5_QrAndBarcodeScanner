package com.mckimquyen.barcodescanner.feature.tabs.create.qr

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.barcodescanner.databinding.IAppBinding

class AdapterApp(private val listener: Listener) : RecyclerView.Adapter<AdapterApp.ViewHolder>() {

    interface Listener {
        fun onAppClicked(packageName: String)
    }

    var apps: List<ResolveInfo> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return apps.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = IAppBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        val isLastPosition = position == apps.lastIndex
        holder.show(app, isLastPosition)
    }

    inner class ViewHolder(private val binding: IAppBinding) : RecyclerView.ViewHolder(binding.root) {
        private val packageManager: PackageManager
            get() = binding.root.context.applicationContext.packageManager

        fun show(
            app: ResolveInfo,
            isLastPosition: Boolean,
        ) {
            showName(app)
            showIcon(app)
            showDelimiter(isLastPosition)
            handleItemClicked(app)
        }

        private fun showName(app: ResolveInfo) {
            binding.textView.text = app.loadLabel(packageManager)
        }

        private fun showIcon(app: ResolveInfo) {
            binding.imageView.setImageDrawable(app.loadIcon(packageManager))
        }

        private fun showDelimiter(isLastPosition: Boolean) {
            binding.delimiter.isInvisible = isLastPosition
        }

        private fun handleItemClicked(app: ResolveInfo) {
            binding.root.setOnClickListener {
                listener.onAppClicked(app.activityInfo?.packageName.orEmpty())
            }
        }
    }
}
