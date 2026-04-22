package com.mckimquyen.barcodescanner.feature.common.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.LoIconButtonWithDelimiterBinding

class IconButtonWithDelimiter : FrameLayout {
    private val binding: LoIconButtonWithDelimiterBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr) {
        val inflater = LayoutInflater.from(context)
        binding = LoIconButtonWithDelimiterBinding.inflate(inflater, this, true)

        context.obtainStyledAttributes(attrs, R.styleable.IconButtonWithDelimiter).apply {
            showIcon(this)
            showIconBackgroundColor(this)
            showText(this)
            showArrow(this)
            showDelimiter(this)
            recycle()
        }
    }

    private fun showIcon(attributes: TypedArray) {
        val iconResId = attributes.getResourceId(R.styleable.IconButtonWithDelimiter_icon, -1)
        val icon = AppCompatResources.getDrawable(context, iconResId)
        binding.imageViewSchema.setImageDrawable(icon)
    }

    private fun showIconBackgroundColor(attributes: TypedArray) {
        val color = attributes.getColor(
            R.styleable.IconButtonWithDelimiter_iconBackground,
//            context.resources.getColor(R.color.green)
            ContextCompat.getColor(context, R.color.green)
        )
        (binding.layoutImage.background.mutate() as GradientDrawable).setColor(color)
    }

    private fun showText(attributes: TypedArray) {
        binding.textView.text = attributes.getString(R.styleable.IconButtonWithDelimiter_text).orEmpty()
    }

    private fun showArrow(attributes: TypedArray) {
        binding.imageViewArrow.isVisible =
            attributes.getBoolean(R.styleable.IconButtonWithDelimiter_isArrowVisible, false)
    }

    private fun showDelimiter(attributes: TypedArray) {
        binding.delimiter.isInvisible =
            attributes.getBoolean(R.styleable.IconButtonWithDelimiter_isDelimiterVisible, true).not()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.imageViewSchema.isEnabled = enabled
        binding.textView.isEnabled = enabled
    }
}
