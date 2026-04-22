package com.mckimquyen.barcodescanner.feature.common.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.mckimquyen.barcodescanner.R
import com.mckimquyen.barcodescanner.databinding.LoDateTimePickerButtonBinding
import com.mckimquyen.barcodescanner.extension.formatOrNull
import java.text.SimpleDateFormat
import java.util.Locale

class DateTimePickerButton : FrameLayout {
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
    private val binding: LoDateTimePickerButtonBinding

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
        binding = LoDateTimePickerButtonBinding.inflate(inflater, this, true)

        context.obtainStyledAttributes(attrs, R.styleable.DateTimePickerButton).apply {
            showHint(this)
            recycle()
        }

        setOnClickListener {
            showDateTimePickerDialog()
        }

        showDateTime()
    }

    var dateTime: Long = System.currentTimeMillis()
        set(value) {
            field = value
            showDateTime()
        }

    private fun showHint(attributes: TypedArray) {
        binding.textViewHint.text = attributes.getString(R.styleable.DateTimePickerButton_hint).orEmpty()
    }

    private fun showDateTimePickerDialog() {
        SingleDateAndTimePickerDialog.Builder(context)
//            .backgroundColor(context.resources.getColor(R.color.date_time_picker_dialog_background_color))
            .backgroundColor(ContextCompat.getColor(context, R.color.date_time_picker_dialog_background_color))
            .title(binding.textViewHint.text.toString())
//            .mainColor(context.resources.getColor(R.color.blue))
            .mainColor(ContextCompat.getColor(context, R.color.blue))
            .listener { newDateTime ->
                dateTime = newDateTime.time
                showDateTime()
            }
            .display()
    }

    private fun showDateTime() {
        binding.textViewDateTime.text = dateFormatter.formatOrNull(dateTime).orEmpty()
    }
}
