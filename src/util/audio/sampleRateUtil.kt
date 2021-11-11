package util.audio

import java.io.File
import javax.sound.sampled.AudioSystem

val File.sampleRate get() = AudioSystem.getAudioFileFormat(this).format.sampleRate
