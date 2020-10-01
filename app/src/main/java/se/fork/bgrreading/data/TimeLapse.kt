package se.fork.bgrreading.data

data class TimeLapse (
    var frameRate : Int = 30,
    val movements : MutableList<MovementSnapshot>  = mutableListOf<MovementSnapshot>()
) {
    val interval = (1f / frameRate.toFloat()).times(1000).toInt()
}