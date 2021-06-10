package util.io.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import processing.core.PImage
import util.image.ImageFormat
import util.image.ImageFormat.Companion.getFormat

@Serializable
class SimplePImageData(
  val colCount: Int,
  val rowCount: Int,
  val data: IntArray,
  val format: ImageFormat,
) {

  fun toPImage(): PImage = PImage(colCount, rowCount).also {
    it.format = format.pImageFormat
    it.pixels = data
    it.loadPixels()
  }

  companion object {
    fun serialize(encoder: Encoder, value: SimplePImageData) =
      serializer().serialize(encoder, value)

    fun deserialize(decoder: Decoder) = serializer().deserialize(decoder)
  }
}

object PImageSerializer : KSerializer<PImage> {
  override fun deserialize(decoder: Decoder): PImage =
    SimplePImageData.deserialize(decoder).toPImage()


  override val descriptor: SerialDescriptor
    get() = SimpleMat.serializer().descriptor

  override fun serialize(encoder: Encoder, value: PImage) {
    value.loadPixels()
    SimplePImageData.serialize(
      encoder,
      SimplePImageData(
        colCount = value.height,
        rowCount = value.width,
        data = value.pixels,
        format = value.getFormat(),
      ),
    )
  }
}
