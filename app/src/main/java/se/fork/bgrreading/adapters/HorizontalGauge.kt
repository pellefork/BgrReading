package se.fork.bgrreading.adapters

import android.view.View
import se.fork.bgrreading.R
import timber.log.Timber
import kotlin.math.absoluteValue
import kotlin.math.sign

class HorizontalGauge(val root: View) {

    val leftPart = root.findViewById<View>(R.id.left_gauge)
    val rightPart = root.findViewById<View>(R.id.right_gauge)

    var range: Float = 100f
    private var currentValue: Float = 0f
    private var lastValue: Float = 0f

    fun setValue(value : Float) {
        currentValue = value
        val size = toPixels()
        Timber.d("setValue: Width in pixels: $size")
        if (currentValue < 0f) {
            setLeftSizeInPixels(size)
            if (isChangingSign())
                setRightSizeInPixels(0)
        } else {
            setRightSizeInPixels(size)
            if (isChangingSign())
                setLeftSizeInPixels(0)
        }
        lastValue = value
    }
    
    private fun toPixels() : Int {
        val screenWidthFloat = root.resources.displayMetrics.widthPixels.toFloat()
        val fraction = currentValue / range
        val size = fraction * screenWidthFloat / 2f
        return size.absoluteValue.toInt()
    }

    private fun setLeftSizeInPixels(sizeInPixels: Int) {
        val params = leftPart.layoutParams
        params.width = sizeInPixels
        leftPart.layoutParams = params
    }

    private fun setRightSizeInPixels(sizeInPixels: Int) {
        val params = rightPart.layoutParams
        params.width = sizeInPixels
        rightPart.layoutParams = params
    }

    private fun isChangingSign() : Boolean {
        return currentValue.sign.equals(lastValue.sign).not()
    }
}