package util.io.serialization

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.DoubleArraySerializer
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import util.io.encode

private fun <T> T.encode(serializer: SerializationStrategy<T>) = encode(serializer, this)

fun DoubleArray.toJsonArray(): JsonElement = encode(DoubleArraySerializer())
fun IntArray.toJsonArray(): JsonElement = encode(IntArraySerializer())
fun FloatArray.toJsonArray(): JsonElement = encode(FloatArraySerializer())
fun LongArray.toJsonArray(): JsonElement = encode(LongArraySerializer())

@JvmName("doubleListToJsonArray")
fun List<Double>.toJsonArray(): JsonElement = encode(ListSerializer(Double.serializer()))

@JvmName("intListToJsonArray")
fun List<Int>.toJsonArray(): JsonElement = encode(ListSerializer(Int.serializer()))

@JvmName("floatListToJsonArray")
fun List<Float>.toJsonArray(): JsonElement = encode(ListSerializer(Float.serializer()))

@JvmName("longListToJsonArray")
fun List<Long>.toJsonArray(): JsonElement = encode(ListSerializer(Long.serializer()))

@JvmName("stringListToJsonArray")
fun List<String>.toJsonArray(): JsonElement = encode(ListSerializer(String.serializer()))

@JvmName("doubleListToJsonArray")
fun Array<Double>.toJsonArray(): JsonElement = encode(ArraySerializer(Double.serializer()))

fun Array<Int>.toJsonArray(): JsonElement = encode(ArraySerializer(Int.serializer()))
fun Array<Float>.toJsonArray(): JsonElement = encode(ArraySerializer(Float.serializer()))
fun Array<Long>.toJsonArray(): JsonElement = encode(ArraySerializer(Long.serializer()))
fun Array<String>.toJsonArray(): JsonElement = encode(ArraySerializer(String.serializer()))
