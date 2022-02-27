package util.image.bytesAndBuffers

import util.image.bytesAndBuffers.BufferTypes.ByteBuf
import util.image.bytesAndBuffers.BufferTypes.DoubleBuf
import util.image.bytesAndBuffers.BufferTypes.IntBuf
import util.image.bytesAndBuffers.BufferTypes.LongBuf
import util.image.bytesAndBuffers.BufferTypes.ShortBuf
import util.image.opencvMat.toBuffer
import util.iterators.toDoubleArray
import util.iterators.toIntArray

fun ByteArray.checkDepth(expected: Int) {
  if (size % expected != 0) {
    throw Exception("Trying to convert bytearray that's not divisible by size $expected to int array! Byte size: $size")
  }
}

fun ByteArray.toShortArray(): ShortArray {
  checkDepth(ShortBuf.depth)
  return ShortArray(size / ShortBuf.depth).also {
    toBuffer().asShortBuffer().get(it)
  }
}

fun ByteArray.toIntArray(byteDepth: Int): IntArray = when (byteDepth) {
  1 -> toInt8Array()
  2 -> toInt16Array()
  4 -> toInt32Array()
  else -> throw Exception("Invalid byte depth, trying to convert ByteArray to IntArray with depth $byteDepth, Only valid depth values are 1,2,4")
}

fun ByteArray.toDoubleArray(byteDepth: Int): DoubleArray = when (byteDepth) {
  1 -> toInt8Array().toDoubleArray()
  2 -> toInt16Array().toDoubleArray()
  4 -> toInt32Array().toDoubleArray()
  8 -> toDoubleArray()
  else -> throw Exception("Invalid byte depth, trying to convert ByteArray to DoubleArray with depth $byteDepth, Only valid depth values are 1,2,4,8")
}

fun ByteArray.toInt8Array(): IntArray {
  val depth = ByteBuf.depth
  checkDepth(depth)

  return map { it.toInt() + 128 }.toIntArray()
}

fun ByteArray.toInt16Array(): IntArray =
  toShortArray().toIntArray()


fun ByteArray.toInt32Array(): IntArray {
  val depth = IntBuf.depth
  checkDepth(depth)
  return IntArray(size / depth).also {
    toBuffer().asIntBuffer().get(it)
  }
}

fun ByteArray.toLongArray(): LongArray {
  val depth = LongBuf.depth
  checkDepth(depth)
  return LongArray(size / depth).also {
    toBuffer().asLongBuffer().get(it)
  }
}

fun ByteArray.toDoubleArray(): DoubleArray {
  val depth = DoubleBuf.depth
  checkDepth(depth)
  return DoubleArray(size / depth).also {
    toBuffer().asDoubleBuffer().get(it)
  }
}
