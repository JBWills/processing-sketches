package util.image.opencvMat

import org.opencv.core.Core
import org.opencv.core.Mat
import util.image.ImageFormat
import util.image.opencvMat.flags.ChannelDepth
import util.isAllUniqueChars
import util.tuple.Pair3
import util.tuple.Pair4


fun List<Mat>.merge(format: ImageFormat): Mat {
  if (format.numChannels != size) {
    throw Exception("Trying to merge with incorrect number of channels. Format: ${format.name}, number of channels expected: ${format.numChannels}, number of channels received: $size")
  }

  return Mat(this[0].rows(), this[0].cols(), format.openCVFormat).also { newMat ->
    Core.merge(this, newMat)
  }
}

fun Mat.splitRgb(): Pair3<Mat, Mat, Mat> {
  if (channels() != 3) {
    throw Exception("Trying to split with wrong number of channels! Expected 3 channels, got ${channels()}")
  }
  val channels = split()
  return Pair3(channels[0], channels[1], channels[2])
}

fun Mat.split(): List<Mat> = mutableListOf<Mat>().also { Core.split(this, it) }

fun Mat.split4(): Pair4<Mat, Mat, Mat, Mat>? {
  if (channels() != 4) {
    throw Exception("Trying to split with wrong number of channels! Expected 4 channels, got ${channels()}")
  }
  val channels = split()

  if (channels.isEmpty()) {
    return null
  }

  if (channels.size != 4) {
    throw Exception("For some reason the result of split() is returning a different number of mats than the number of channels, SplitChannels: ${channels.size}")
  }

  return Pair4(channels[0], channels[1], channels[2], channels[3])
}

fun Mat.splitArray(): Array<Mat> = split().toTypedArray()
fun Mat.setChannels(vararg channels: Mat) {
  Core.merge(channels.toList(), this)
}

fun List<Mat>.shuffle(currOrder: String, newOrder: String, newFormat: ImageFormat): Mat {
  if (!currOrder.isAllUniqueChars()) {
    throw Exception("currOrder mush include only unique chars. Received currOrder: $currOrder")
  } else if (currOrder.length != size) {
    throw Exception("Trying to shuffle the wrong number of channels. Expected length: ${size}, actual length: $currOrder")
  } else if (newOrder.length != newFormat.numChannels) {
    throw Exception("Output channels don't match between format and order string. newFormat channels: ${newFormat.numChannels}, new order channels: ${newOrder.length}")
  } else if (any { it.channels() != 1 }) {
    throw Exception("list provided needs to be all single channel mats.")
  } else if (!currOrder.toSet().containsAll(newOrder.toSet())) {
    throw Exception("unknown chars provided in newOrder. currOrder: $currOrder, newOrder: $newOrder")
  }

  val mapFromCharToChannel = currOrder
    .mapIndexed { index, c -> c to get(index) }
    .toMap()

  val newMatList: List<Mat> = newOrder.map { c -> mapFromCharToChannel[c]!! }

  return newMatList.merge(newFormat)
}

fun Mat.shuffle(currOrder: String, newOrder: String, newFormat: ImageFormat) =
  split().shuffle(currOrder, newOrder, newFormat)

fun Mat.convertTo(
  depth: ChannelDepth,
  dest: Mat = this,
  alpha: Double? = null,
  beta: Double? = null
): Mat {
  when {
    alpha != null && beta != null -> convertTo(dest, depth.type, alpha, beta)
    alpha != null -> convertTo(dest, depth.type, alpha)
    else -> convertTo(dest, depth.type)
  }

  return dest
}
