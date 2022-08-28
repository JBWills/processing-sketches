package util.numbers

fun Double.roundedString(decimals: Int = 2) = "%.${decimals}f".format(this)

fun Double.asPercentString(decimals: Int = 1) = "${(this * 100).roundedString(decimals)}%"
