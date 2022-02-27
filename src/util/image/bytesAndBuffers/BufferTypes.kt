package util.image.bytesAndBuffers

enum class BufferTypes(val depth: Int) {
  ByteBuf(1),
  ShortBuf(2),
  IntBuf(4),
  LongBuf(8),
  DoubleBuf(8),
}
