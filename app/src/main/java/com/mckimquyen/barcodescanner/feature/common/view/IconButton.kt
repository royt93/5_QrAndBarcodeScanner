package com.mckimquyen.barcodescanner.feature.common.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.LoIconButtonBinding

class IconButton : FrameLayout {
    private val binding: LoIconButtonBinding

    var text: String
        get() = binding.textView.text.toString()
        set(value) {
            binding.textView.text = value
        }

    constructor(context: Context) : this(context, null)
    constructor(
        context: Context,
        attrs: AttributeSet?,
    ) : this(context, attrs, -1)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr) {
        val inflater = LayoutInflater.from(context)
        binding = LoIconButtonBinding.inflate(inflater, this, true)

        context.obtainStyledAttributes(attrs, R.styleable.IconButton).apply {
            showIcon(this)
            showIconBackgroundColor(this)
            showText(this)
            recycle()
        }
    }

    private fun showIcon(attributes: TypedArray) {
        val iconResId = attributes.getResourceId(R.styleable.IconButton_icon, -1)
        val icon = AppCompatResources.getDrawable(context, iconResId)
        binding.imageViewSchema.setImageDrawable(icon)
    }

    private fun showIconBackgroundColor(attributes: TypedArray) {
        val color = attributes.getColor(
            R.styleable.IconButton_iconBackground,
            ContextCompat.getColor(context, R.color.green)
        )
        (binding.layoutImage.background.mutate() as GradientDrawable).setColor(color)
    }

    private fun showText(attributes: TypedArray) {
        binding.textView.text = attributes.getString(R.styleable.IconButton_text).orEmpty()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.imageViewSchema.isEnabled = enabled
        binding.textView.isEnabled = enabled
    }
}
