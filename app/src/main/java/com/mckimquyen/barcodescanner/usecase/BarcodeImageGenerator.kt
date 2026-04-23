package com.mckimquyen.barcodescanner.usecase

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.mckimquyen.barcodescanner.model.Barcode
import io.reactivex.rxjava3.core.Single

object BarcodeImageGenerator {
    private val encoder = BarcodeEncoder()
    private val writer = MultiFormatWriter()

    fun generateBitmapAsync(
        barcode: Barcode,
        width: Int,
        height: Int,
        margin: Int = 0,
        codeColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE,
    ): Single<Bitmap> {
        return Single.create { emitter ->
            try {
                emitter.onSuccess(
                    generateBitmap(
                        barcode = barcode,
                        width = width,
                        height = height,
                        margin = margin,
                        codeColor = codeColor,
                        backgroundColor = backgroundColor
                    )
                )
            } catch (ex: Exception) {
                Logger.log(ex)
                emitter.onError(ex)
            }
        }
    }

    fun generateBitmap(
        barcode: Barcode,
        width: Int,
        height: Int,
        margin: Int = 0,
        codeColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE,
    ): Bitmap {
        try {
            val matrix = encoder.encode(
                /* contents = */ barcode.text,
                /* format = */ barcode.format,
                /* width = */ width,
                /* height = */ height,
                /* hints = */ createHints(barcode.errorCorrectionLevel, margin)
            )
            return createBitmap(
                matrix = matrix,
                codeColor = codeColor,
                backgroundColor = backgroundColor
            )
        } catch (ex: Exception) {
            throw Exception("Unable to generate barcode image, ${barcode.format}, ${barcode.text}", ex)
        }
    }

    fun generateSvgAsync(
        barcode: Barcode,
        width: Int,
        height: Int,
        margin: Int = 0,
    ): Single<String> {
        return Single.create { emitter ->
            try {
                emitter.onSuccess(
                    generateSvg(
                        barcode = barcode,
                        width = width,
                        height = height,
                        margin = margin
                    )
                )
            } catch (ex: Exception) {
                Logger.log(ex)
                emitter.onError(ex)
            }
        }
    }

    private fun generateSvg(
        barcode: Barcode,
        width: Int,
        height: Int,
        margin: Int = 0,
    ): String {
        val matrix = writer.encode(
            /* contents = */ barcode.text,
            /* format = */ barcode.format,
            /* width = */ 0,
            /* height = */ 0,
            /* hints = */ createHints(barcode.errorCorrectionLevel, margin)
        )
        return createSvg(width, height, matrix)
    }

    private fun createHints(
        errorCorrectionLevel: String?,
        margin: Int,
    ): Map<EncodeHintType, Any> {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "utf-8",
            EncodeHintType.MARGIN to margin
        )

        if (errorCorrectionLevel != null) {
            hints.plus(EncodeHintType.ERROR_CORRECTION to errorCorrectionLevel)
        }

        return hints
    }

    private fun createSvg(
        width: Int,
        height: Int,
        matrix: BitMatrix,
    ): String {
        val result = StringBuilder()
            .append("<svg width=\"$width\" height=\"$height\"")
            .append(" viewBox=\"0 0 $width $height\"")
            .append(" xmlns=\"http://www.w3.org/2000/svg\">\n")

        val w = matrix.width
        val h = matrix.height
        val xf = width.toFloat() / w
        val yf = height.toFloat() / h

        for (y in 0 until h) {
            for (x in 0 until w) {
                if (matrix.get(x, y)) {
                    val ox = x * xf
                    val oy = y * yf
                    result.append("<rect x=\"$ox\" y=\"$oy\"")
                    result.append(" width=\"$xf\" height=\"$yf\"/>\n")
                }
            }
        }

        result.append("</svg>\n")

        return result.toString()
    }

    private fun createBitmap(
        matrix: BitMatrix,
        codeColor: Int,
        backgroundColor: Int,
    ): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix[x, y]) codeColor else backgroundColor
            }
        }

        var bitmapCode = Bitmap.createBitmap(
            /* width = */ width,
            /* height = */ height,
            /* config = */ Bitmap.Config.ARGB_8888
        ).apply {
            setPixels(
                /* pixels = */ pixels,
                /* offset = */ 0,
                /* stride = */ width,
                /* x = */ 0,
                /* y = */ 0,
                /* width = */ width,
                /* height = */ height
            )
        }

        return createCenteredBitmapWithWhiteBackground(bitmapCode)
    }

    private fun createCenteredBitmapWithWhiteBackground(bitmapA: Bitmap): Bitmap {
        val marginInPx = 16

        // Define the dimensions for the new bitmap (B) with added margins
        val width = bitmapA.width + 2 * marginInPx
        val height = bitmapA.height + 2 * marginInPx

        // Create a new bitmap (B) with a white background
        val bitmapB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapB)
        canvas.drawColor(Color.WHITE) // Fill with white background

        // Calculate the position to center bitmapA within bitmapB with margins
        val left = marginInPx.toFloat()
        val top = marginInPx.toFloat()

        // Draw bitmapA on the canvas, centered with margin
        canvas.drawBitmap(bitmapA, left, top, null)

        return bitmapB
    }
}
