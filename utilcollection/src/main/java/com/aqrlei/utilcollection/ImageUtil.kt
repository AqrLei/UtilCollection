package com.aqrlei.utilcollection

import android.content.res.Resources
import android.graphics.*
import android.util.TypedValue

/**
 * created by AqrLei on 2020/7/21
 */
object ImageUtil {
    private var bitmap: Bitmap? = null
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val displayMetrics = Resources.getSystem().displayMetrics

    fun getBitmapFromResource(
        resource: Resources,
        resId: Int,
        targetWidthDp: Float,
        targetHeightDp: Float
    ): Bitmap? {
        val targetWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, targetHeightDp, displayMetrics)
        val targetHeight =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, targetWidthDp, displayMetrics)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(resource, resId, options)
        options.inJustDecodeBounds = false
        options.inScaled = false
        options.inSampleSize = getAdaptedBitmapSampleSize(options, targetHeight, targetWidth)
        bitmap = BitmapFactory.decodeResource(resource, resId, options)
        return roundBitmap()
    }

    private fun getAdaptedBitmapSampleSize(
        options: BitmapFactory.Options,
        targetHeight: Float,
        targetWidth: Float
    ): Int {
        var inSampleSize = 1
        val realHeight = options.outHeight
        val realWidth = options.outWidth
        while ((realHeight / inSampleSize > targetHeight) && (realWidth / inSampleSize > targetWidth)) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    fun roundBitmap(): Bitmap? {
        return bitmap?.run { setBitmap(this) }
    }

    private fun setBitmap(bitmap: Bitmap): Bitmap {
        val height = bitmap.height
        val width = bitmap.width
        val path = Path()
        val roundedCornerRadius = Math.min(height / 2F, width / 2F)
        val paintingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(paintingBitmap)


        val rect = Rect(0, 0, width, height)
        val rectF = RectF(Rect(0, 0, width, height))
        canvas.drawRoundRect(rectF, roundedCornerRadius, roundedCornerRadius, paint)
        canvas.drawRect(RectF(0F, 0F, roundedCornerRadius, roundedCornerRadius), paint)
        canvas.drawRect(
            RectF(
                width - roundedCornerRadius,
                height - roundedCornerRadius,
                width.toFloat(),
                height.toFloat()
            ),
            paint
        )

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        canvas.drawBitmap(bitmap, null, rect, paint)
        paint.apply {
            color = Color.parseColor("#00FF00")
            style = Paint.Style.STROKE
            strokeWidth = 10F
        }

        /*  if (roundLeftTop) {
              path.moveTo(0f, roundedCornerRadius)
              rectF.left = 0f
              rectF.top = 0f
              rectF.right = roundedCornerRadius * 2
              rectF.bottom = roundedCornerRadius * 2
              path.arcTo(rectF, 180f, 90f)
          } else {*/
        path.moveTo(0f, 0f)
        //}

        path.lineTo(width - roundedCornerRadius, 0f)
        rectF.left = width - roundedCornerRadius * 2
        rectF.top = 0f
        rectF.right = width.toFloat()
        rectF.bottom = roundedCornerRadius * 2
        path.arcTo(rectF, 270f, 90f)
        /*else {
            path.lineTo(width.toFloat(), 0f)
        }*/

        /*  if (roundRightBottom) {
              path.lineTo(width.toFloat(), height - roundedCornerRadius)
              rectF.left = width - roundedCornerRadius * 2
              rectF.top = height - roundedCornerRadius * 2
              rectF.right = width.toFloat()
              rectF.bottom = height.toFloat()
              path.arcTo(rectF, 0f, 90f)
          } else {*/
        path.lineTo(width.toFloat(), height.toFloat())
        path.lineTo(roundedCornerRadius, height.toFloat())
        rectF.left = 0f
        rectF.top = height - roundedCornerRadius * 2
        rectF.right = roundedCornerRadius * 2
        rectF.bottom = height.toFloat()
        path.arcTo(rectF, 90f, 90f)
        /* else {
            path.lineTo(0f, height.toFloat())
        }*/

        path.close()
        canvas.drawPath(path, paint)
        return paintingBitmap
    }
}