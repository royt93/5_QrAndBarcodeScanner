package com.mckimquyen.barcodescanner.feature.vip

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.sqrt

/**
 * Diagonal shimmer (45°) vẽ trực tiếp qua Canvas.
 * - Sweep góc 45° top-left → bottom-right: canvas.rotate(-45°) + drawRect
 * - White shimmer (không phải gold) để nổi bật trên nền gold
 * - Opacity tối đa 25% — pearl/luster effect, không neon
 * - clipPath đảm bảo không lộ hard edges tại góc bo tròn
 */
internal class GoldShimmerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clipPath = Path()
    private val shaderMatrix = Matrix()

    private val shimmerWidthFraction = 0.55f

    var cornerRadiusPx: Float = 0f
        set(value) { field = value; rebuildClipPath() }

    var shimmerProgress: Float = 0f
        set(value) { field = value; invalidate() }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildClipPath()

        val sw = w * shimmerWidthFraction
        paint.shader = LinearGradient(
            0f, 0f, sw, 0f,
            intArrayOf(
                0x00FFFFFF,
                0x15FFFFFF,
                0x40FFFFFF,
                0x15FFFFFF,
                0x00FFFFFF
            ),
            floatArrayOf(0f, 0.2f, 0.5f, 0.8f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    private fun rebuildClipPath() {
        if (width > 0 && height > 0) {
            clipPath.reset()
            clipPath.addRoundRect(
                RectF(0f, 0f, width.toFloat(), height.toFloat()),
                cornerRadiusPx, cornerRadiusPx,
                Path.Direction.CW
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (width == 0 || paint.shader == null) return

        val cx = width / 2f
        val cy = height / 2f
        val sw = width * shimmerWidthFraction
        val diag = sqrt((width * width + height * height).toDouble()).toFloat()

        val x = -sw + shimmerProgress * (diag + sw * 2)

        shaderMatrix.setTranslate(x, 0f)
        paint.shader.setLocalMatrix(shaderMatrix)

        canvas.save()
        if (!clipPath.isEmpty) canvas.clipPath(clipPath)
        canvas.rotate(-45f, cx, cy)
        canvas.drawRect(x, -height.toFloat() * 2, x + sw, height.toFloat() * 3, paint)
        canvas.restore()
    }
}
