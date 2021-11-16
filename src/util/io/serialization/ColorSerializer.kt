package util.io.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.awt.Color


@Serializable
class SimpleColor(val red: Int, val green: Int, val blue: Int, val alpha: Int) {
  constructor(color: Color) : this(color.red, color.green, color.blue, color.alpha)

  fun toColor() = Color(red, green, blue, alpha)
}

@Serializable

object ColorSerializer : KSerializer<Color> {
  override fun deserialize(decoder: Decoder): Color =
    SimpleColor.serializer().deserialize(decoder).toColor()

  override val descriptor: SerialDescriptor
    get() = SimpleColor.serializer().descriptor

  override fun serialize(encoder: Encoder, value: Color) =
    SimpleColor.serializer().serialize(encoder, SimpleColor(value))
}
