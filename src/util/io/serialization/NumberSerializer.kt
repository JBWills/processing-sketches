package util.io.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
class SimpleNumber(val value: Double)

@Serializable

object NumberSerializer : KSerializer<Number> {
  override fun deserialize(decoder: Decoder): Number =
    SimpleNumber.serializer().deserialize(decoder).value

  override val descriptor: SerialDescriptor
    get() = SimpleColor.serializer().descriptor

  override fun serialize(encoder: Encoder, value: Number) =
    SimpleNumber.serializer().serialize(encoder, SimpleNumber(value.toDouble()))
}
