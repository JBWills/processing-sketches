package fastnoise

import FastNoiseLite
import FastNoiseLite.NoiseType

fun createFastNoise(seed: Int, type: NoiseType): FastNoiseLite =
  FastNoiseLite(seed).also { it.SetNoiseType(type) }
