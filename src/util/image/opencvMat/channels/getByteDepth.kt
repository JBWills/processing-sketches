package util.image.opencvMat.channels

import org.opencv.core.Mat
import util.image.opencvMat.ChannelDepth.Companion.channelDepth

fun Mat.getByteDepth() = channelDepth().byteDepth
