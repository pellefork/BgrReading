package se.fork.bgrreading.adapters

import android.view.View
import android.widget.TextView
import se.fork.bgrreading.R
import timber.log.Timber
import java.text.DecimalFormat
import kotlin.math.absoluteValue
import kotlin.math.sign

class HorizontalGauge(val root: View) {

    val leftPart = root.findViewById<View>(R.id.left_gauge)
    val rightPart = root.findViewById<View>(R.id.right_gauge)
    val legendText = root.findViewById<TextView>(R.id.legend)
    val valueText = root.findViewById<TextView>(R.id.value)
    val decimalFormat = DecimalFormat("#.###")

    var range: Float = 100f
    var legend: String
        get() {
            return legendText.text.toString()
        }
        set(value) {
        legendText.text = value
    }
    private var currentValue: Float = 0f
    private var lastValue: Float = 0f

    fun setValue(value : Float) {
        currentValue = value

        valueText.text = decimalFormat.format(value)

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
        val size = fraction * screenWidthFloat / 3f     // Leave some space on the sides
        return size.absoluteValue.toInt()
    }

    private fun setLeftSizeInPixels(sizeInPixels: Int) {
        if (sizeInPixels.equals(0)) {
            leftPart.visibility = View.GONE
        } else {
            leftPart.visibility = View.VISIBLE
            val params = leftPart.layoutParams
            params.width = sizeInPixels
            leftPart.layoutParams = params
        }
    }

    private fun setRightSizeInPixels(sizeInPixels: Int) {
        if (sizeInPixels.equals(0)) {
            rightPart.visibility = View.GONE
        } else {
            rightPart.visibility = View.VISIBLE
            val params = rightPart.layoutParams
            params.width = sizeInPixels
            rightPart.layoutParams = params
        }
    }

    private fun isChangingSign() : Boolean {
        return currentValue.sign.equals(lastValue.sign).not()
    }
}