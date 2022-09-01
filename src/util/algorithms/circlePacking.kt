package util.algorithms

import arrow.core.memoize
import coordinate.BoundRect
import coordinate.Circ
import org.locationtech.jts.geom.Envelope
import util.iterators.every
import util.quadTree.GQuadtree
import util.random.randPoint
import kotlin.random.Random

private fun Circ.toEnvelope() = Envelope(left, right, top, bottom)

fun Random.pack(bound: BoundRect, radius: Double, limit: Int, attempts: Int): List<Circ> {
  val tree = GQuadtree(Circ::toEnvelope)
  val actualBounds = bound.shrink(radius)

  for (i in 0 until limit) {
    var c: Circ? = null
    for (attempt in 0 until attempts) {
      val p = randPoint(actualBounds)
      val cAttempt = Circ(p, radius)
      if (tree.query(cAttempt.toEnvelope()).every { it.origin.dist(p) >= 2 * radius }) {
        c = cAttempt
        break
      }
    }
    c?.let { tree.insert(c) }
  }

  return tree.queryAll()
}

var lastRandom: Random? = null

private val packMemoHelper: (seed: Int, bound: BoundRect, radius: Double, limit: Int, attempts: Int) -> List<Circ> =
  { _: Int, bound: BoundRect, radius: Double, limit: Int, attempts: Int ->
    lastRandom?.pack(bound, radius, limit, attempts) ?: listOf()
  }.memoize()

fun packMemo(
  random: Random,
  randomSeed: Int,
  bound: BoundRect,
  radius: Double,
  limit: Int,
  attempts: Int
): List<Circ> {
  lastRandom = random
  return packMemoHelper(randomSeed, bound, radius, limit, attempts)
}
