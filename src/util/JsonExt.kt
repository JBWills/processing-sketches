package util

import controls.props.PropData
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json.Default.encodeToJsonElement
import kotlinx.serialization.json.Json.Default.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

fun <T : PropData<T>> T.toJsonElement(): JsonElement =
  encodeToJsonElement(toSerializer(), this)

fun <T : PropData<T>> List<T>.toJsonArray(): JsonElement {
  val first = firstOrNull() ?: return JsonArray(content = listOf())

  return encodeToJsonElement(ListSerializer(first.toSerializer()), this)
}

fun jsonObjectOf(vararg pairs: Pair<String, JsonElement>): JsonObject =
  JsonObject(hashMapOf(*pairs))

fun JsonObject.serializedString() = encodeToString(JsonObject.serializer(), this)
