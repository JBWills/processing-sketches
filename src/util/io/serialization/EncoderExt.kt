package util.io.serialization


import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement

fun Encoder.encodeElement(arr: JsonElement) = encodeString(arr.toString())

fun Encoder.encodeArray(arr: DoubleArray) = encodeElement(arr.toJsonArray())
fun Encoder.encodeArray(arr: FloatArray) = encodeElement(arr.toJsonArray())
fun Encoder.encodeArray(arr: IntArray) = encodeElement(arr.toJsonArray())
fun Encoder.encodeArray(arr: LongArray) = encodeElement(arr.toJsonArray())
fun Encoder.encodeArray(arr: Array<Double>) = encodeElement(arr.toJsonArray())
fun Encoder.encodeArray(arr: Array<Int>) = encodeElement(arr.toJsonArray())
fun Encoder.encodeArray(arr: Array<Float>) = encodeElement(arr.toJsonArray())
fun Encoder.encodeArray(arr: Array<Long>) = encodeElement(arr.toJsonArray())
fun Encoder.encodeArray(arr: Array<String>) = encodeElement(arr.toJsonArray())

@JvmName("encodeStringList")
fun Encoder.encodeList(arr: List<String>) = encodeElement(arr.toJsonArray())

@JvmName("encodeDoubleList")
fun Encoder.encodeList(arr: List<Double>) = encodeElement(arr.toJsonArray())

@JvmName("encodeIntList")
fun Encoder.encodeList(arr: List<Int>) = encodeElement(arr.toJsonArray())

@JvmName("encodeFloatList")
fun Encoder.encodeList(arr: List<Float>) = encodeElement(arr.toJsonArray())

@JvmName("encodeLongList")
fun Encoder.encodeList(arr: List<Long>) = encodeElement(arr.toJsonArray())


