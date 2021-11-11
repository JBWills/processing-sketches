package util.audio

import arrow.core.memoize
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

private val toAudioByteArrayMemo: File.() -> Pair<AudioFormat, ByteArray> =
  { f: File -> AudioSystem.getAudioInputStream(f).let { it.format to it.readAllBytes() } }.memoize()

val toAudioDispatcherMemo: File.(Int, Int) -> AudioDispatcher =
  { file: File, sampleSize: Int, bufferOverlap: Int ->
    val (format, bytes) = file.toAudioByteArrayMemo()
    AudioDispatcherFactory.fromByteArray(bytes, format, sampleSize, bufferOverlap)
  }.memoize()

fun File.toAudioDispatcher(sampleSize: Int = DefaultSampleSize, bufferOverlap: Int = 0) =
  toAudioDispatcherMemo(this, sampleSize, bufferOverlap)
