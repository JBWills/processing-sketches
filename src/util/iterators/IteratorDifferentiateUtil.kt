package util.iterators

fun Iterable<Int>.differentiate(): List<Int> {
  var last = 0

  return map { value ->
    val diffValue = value - last
    last = value
    diffValue
  }
}

@JvmName("differentiateDouble")
fun Iterable<Double>.differentiate(): List<Double> {
  var last = 0.0

  return map { value ->
    val diffValue = value - last
    last = value
    diffValue
  }
}

@JvmName("differentiateLong")
fun Iterable<Long>.differentiate(): List<Long> {
  var last = 0L

  return map { value ->
    val diffValue = value - last
    last = value
    diffValue
  }
}

@JvmName("differentiateFloat")
fun Iterable<Float>.differentiate(): List<Float> {
  var last = 0F

  return map { value ->
    val diffValue = value - last
    last = value
    diffValue
  }
}
