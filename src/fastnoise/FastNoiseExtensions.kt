package fastnoise

import fastnoise.FastNoise.NoiseType

fun createFastNoise(seed: Int, type: NoiseType): FastNoise {
  val fn = FastNoise(seed)
  fn.SetNoiseType(type)
  return fn
}