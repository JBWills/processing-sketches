package util.iterators

fun IntArray.toDoubleArray(): DoubleArray = DoubleArray(size) { get(it).toDouble() }
fun ShortArray.toIntArray(): IntArray = IntArray(size) { get(it).toInt() }
fun ShortArray.toDoubleArray(): DoubleArray = DoubleArray(size) { get(it).toDouble() }
