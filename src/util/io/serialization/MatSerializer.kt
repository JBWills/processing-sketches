package util.io.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.opencv.core.Mat
import util.image.ImageFormat
import util.image.ImageFormat.Companion.getFormat
import util.image.toByteArray
import util.image.toMat

@Serializable
class SimpleMatData(
  val colCount: Int,
  val rowCount: Int,
  val data: ByteArray,
  val format: ImageFormat,
) {

  fun toMat(): Mat = data.toMat(colCount, rowCount, format)

  companion object {
    fun serialize(encoder: Encoder, value: SimpleMatData) =
      serializer().serialize(encoder, value)

    fun deserialize(decoder: Decoder) = serializer().deserialize(decoder)
  }
}

object MatSerializer : KSerializer<Mat> {
  override fun deserialize(decoder: Decoder): Mat =
    SimpleMatData.deserialize(decoder).toMat()


  override val descriptor: SerialDescriptor
    get() = SimpleMat.serializer().descriptor

  override fun serialize(encoder: Encoder, value: Mat) {
    SimpleMatData.serialize(
      encoder,
      SimpleMatData(
        colCount = value.cols(),
        rowCount = value.rows(),
        data = value.toByteArray(),
        format = value.getFormat(),
      ),
    )
  }
}
