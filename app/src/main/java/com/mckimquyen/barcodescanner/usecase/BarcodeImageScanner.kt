package com.mckimquyen.barcodescanner.usecase

import android.graphics.Bitmap
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.mckimquyen.barcodescanner.extension.orZero
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.schedulers.Schedulers

object BarcodeImageScanner {
    private var bitmapBuffer: IntArray? = null

    fun parse(image: Bitmap): Single<Result> {
        return Single
            .create { emitter ->
                parse(image, emitter)
            }
            .subscribeOn(Schedulers.newThread())
    }

    private fun parse(
        image: Bitmap,
        emitter: SingleEmitter<Result>,
    ) {
        try {
            emitter.onSuccess(tryParse(image))
        } catch (ex: Exception) {
            Logger.log(ex)
            emitter.onError(ex)
        }
    }

    private fun tryParse(image: Bitmap): Result {
        val width = image.width
        val height = image.height
        val size = width * height

        if (size > bitmapBuffer?.size.orZero()) {
            bitmapBuffer = IntArray(size)
        }

        bitmapBuffer?.let {
            image.getPixels(
                /* pixels = */ it,
                /* offset = */ 0,
                /* stride = */ width,
                /* x = */ 0,
                /* y = */ 0,
                /* width = */ width,
                /* height = */ height
            )
        }

        val source = RGBLuminanceSource(width, height, bitmapBuffer)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        val reader = MultiFormatReader()
        return reader.decode(bitmap)
    }
}
