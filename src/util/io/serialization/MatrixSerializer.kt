package util.io.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import mikera.matrixx.Matrix

@Serializable
class SimpleMat(
  val colCount: Int,
  val rowCount: Int,
  val data: DoubleArray
) {

  fun toMatrix(): Matrix = Matrix(rowCount, colCount).apply { setElements(*data) }

  companion object {
    fun serialize(encoder: Encoder, value: SimpleMat) =
      serializer().serialize(encoder, value)

    fun deserialize(decoder: Decoder) = serializer().deserialize(decoder)
  }
}

object MatrixSerializer : KSerializer<Matrix> {
  override fun deserialize(decoder: Decoder): Matrix =
    SimpleMat.deserialize(decoder).toMatrix()


  override val descriptor: SerialDescriptor
    get() = SimpleMat.serializer().descriptor

  override fun serialize(encoder: Encoder, value: Matrix) =
    SimpleMat.serialize(
      encoder,
      SimpleMat(
        colCount = value.columnCount(),
        rowCount = value.rowCount(),
        data = value.array,
      ),
    )
}
