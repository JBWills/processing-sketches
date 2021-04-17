package util.io

import controls.props.PropData
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

private val JsonFormat = Json {
  ignoreUnknownKeys = true
  prettyPrint = true
}

fun <T> decode(deserializer: DeserializationStrategy<T>, element: JsonElement): T =
  JsonFormat.decodeFromJsonElement(deserializer, element)

fun <T> encode(serializer: SerializationStrategy<T>, value: T): JsonElement =
  JsonFormat.encodeToJsonElement(serializer, value)

fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String =
  JsonFormat.encodeToString(serializer, value)

fun <T : PropData<T>> T.toJsonElement(): JsonElement =
  encode(toSerializer(), this)

fun <T : PropData<T>> List<T>.toJsonArray(): JsonElement {
  val first = firstOrNull() ?: return JsonArray(content = listOf())

  return encode(ListSerializer(first.toSerializer()), this)
}

fun jsonObjectOf(vararg pairs: Pair<String, JsonElement>): JsonObject =
  JsonObject(hashMapOf(*pairs))

fun jsonStringOf(vararg pairs: Pair<String, JsonElement>): String =
  jsonObjectOf(*pairs).serializedString()

fun JsonObject.serializedString(): String = encodeToString(JsonObject.serializer(), this)
