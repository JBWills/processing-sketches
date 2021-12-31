package tests.util.iterators

import coordinate.Point
import org.junit.jupiter.api.Test
import util.iterators.forEachPair
import util.iterators.forEachWithNextIndexed
import util.iterators.forEachWithSurrounding
import util.iterators.forEachWithSurroundingCyclical
import util.iterators.limit
import util.iterators.map2D
import util.iterators.mapEachPairNonNull
import util.iterators.plusIf
import util.iterators.prependIf
import util.iterators.replaceKey
import util.iterators.sumByDoubleIndexed
import util.iterators.sumByIndexed
import util.iterators.sumPointsIndexed
import util.iterators.zipWithNext
import util.iterators.zipWithSiblings
import util.tuple.Pair3
import util.tuple.Pair4
import kotlin.test.assertEquals

internal class IteratorExtensionsTest {

  @Test
  fun testReplaceKey() {
    val m = mutableMapOf(1 to "a")
    m.replaceKey(1, 1)
    assertEquals(mutableMapOf(1 to "a"), m)
    m.replaceKey(1, 2)
    assertEquals(mutableMapOf(2 to "a"), m)
  }

  @Test
  fun testSumPointsIndexed() {
    val p0 = listOf<Point>()
    assertEquals(Point.Zero, p0.sumPointsIndexed { _, p -> p })

    val p1 = listOf(Point.One)
    assertEquals(Point.One, p1.sumPointsIndexed { _, p -> p })

    val p2 = listOf(Point.One, Point(2))
    assertEquals(Point(3), p2.sumPointsIndexed { _, p -> p })

    val p3 = listOf(Point.One, Point(2))
    assertEquals(Point(6), p3.sumPointsIndexed { _, p -> p * 2 })
  }

  @Test
  fun testSumByDoubleIndexed() {
    assertEquals(3.0, listOf(Point.One, Point(2)).sumByDoubleIndexed { _, p -> p.x })
  }

  @Test
  fun testSumByIndexed() {
    assertEquals(3, listOf(Point.One, Point(2)).sumByIndexed { _, p -> p.xi })
  }

  @Test
  fun testForEachPair() {
    val a = listOf(1, 2, 3)
    val result = mutableListOf<Pair<Int, Int>>()

    a.forEachPair { _, _, item1, item2 -> result.add(item1 to item2) }
    assertEquals(listOf(1 to 2, 1 to 3, 2 to 3), result)
  }


  @Test
  fun testMapEachPairNonNull() {
    val a = listOf(1, 2, 3)
    val result = a.mapEachPairNonNull { _, _, item1, item2 -> item1 to item2 }
    assertEquals(listOf(1 to 2, 1 to 3, 2 to 3), result)


    val a2 = listOf(1, 2, 3)
    val result2 =
      a2.mapEachPairNonNull { _, _, item1, item2 -> if (item1 == 1) null else item1 to item2 }
    assertEquals(listOf(2 to 3), result2)
  }

  @Test
  fun testMap2d() {
    val a = listOf(
      listOf(1, 2, 3),
      listOf(4, 5, 6),
      listOf(7, 8, 9),
    )

    val result = a.map2D { _, _, item -> item * 2 }
    val expected = listOf(
      listOf(2, 4, 6),
      listOf(8, 10, 12),
      listOf(14, 16, 18),
    )

    assertEquals(expected, result)
  }

  @Test
  fun testLimit() {
    assertEquals(listOf("a", "b", "c", "d"), listOf("a", "b", "c", "d", "e").limit(4))
    assertEquals(listOf("a", "b", "c", "d"), listOf("a", "b", "c", "d").limit(4))
    assertEquals(listOf("a", "b", "c", "d"), listOf("a", "b", "c", "d").limit(5))
    assertEquals(listOf(), listOf<String>().limit(5))
  }

  @Test
  fun testForEachWithSurrounding() {
    val tuples = mutableListOf<Pair3<Int?, Int, Int?>>()
    listOf(5, 6, 7).forEachWithSurrounding { prev, curr, next ->
      tuples.add(Pair3(prev, curr, next))
    }

    assertEquals(
      listOf<Pair3<Int?, Int, Int?>>(
        Pair3(null, 5, 6),
        Pair3(5, 6, 7),
        Pair3(6, 7, null),
      ),
      tuples,
    )


    val tuplesWithIndex = mutableListOf<Pair4<Int?, Int, Int?, Int>>()

    listOf(5, 6, 7).forEachWithSurrounding { prev, curr, next, index ->
      tuplesWithIndex.add(Pair4(prev, curr, next, index))
    }

    assertEquals(
      listOf<Pair4<Int?, Int, Int?, Int>>(
        Pair4(null, 5, 6, 0),
        Pair4(5, 6, 7, 1),
        Pair4(6, 7, null, 2),
      ),
      tuplesWithIndex,
    )

    val tuplesWithNext = mutableListOf<Pair3<Int, Int?, Int>>()

    listOf(5, 6, 7).forEachWithNextIndexed { curr, next, index ->
      tuplesWithNext.add(Pair3(curr, next, index))
    }

    assertEquals(
      listOf<Pair3<Int, Int?, Int>>(
        Pair3(5, 6, 0),
        Pair3(6, 7, 1),
        Pair3(7, null, 2),
      ),
      tuplesWithNext,
    )

    val tuplesWithSurrounding = mutableListOf<Pair3<Int, Int, Int>>()

    listOf(5, 6, 7).forEachWithSurroundingCyclical { prev, curr, next ->
      tuplesWithSurrounding.add(Pair3(prev, curr, next))
    }

    assertEquals(
      listOf<Pair3<Int, Int, Int>>(
        Pair3(7, 5, 6),
        Pair3(5, 6, 7),
        Pair3(6, 7, 5),
      ),
      tuplesWithSurrounding,
    )
  }

  @Test
  fun testPrependIf() {
    val l = listOf(1, 2)
    assertEquals(l, l.prependIf(false) { 0 })
    assertEquals(listOf(0, 1, 2), l.prependIf(true) { 0 })
    assertEquals(l, l.prependIf(false, 0))
    assertEquals(listOf(0, 1, 2), l.prependIf(true, 0))
  }

  @Test
  fun testAddIf() {
    val l = listOf(1, 2)
    assertEquals(l, l.plusIf(false) { 0 })
    assertEquals(listOf(1, 2, 0), l.plusIf(true) { 0 })
    assertEquals(l, l.plusIf(false, 0))
    assertEquals(listOf(1, 2, 0), l.plusIf(true, 0))
  }

  @Test
  fun testZipWithSiblings() {
    val tuplesWithSurrounding = listOf(5, 6, 7).zipWithSiblings()

    assertEquals(
      listOf(
        Triple(null, 5, 6),
        Triple(5, 6, 7),
        Triple(6, 7, null),
      ),
      tuplesWithSurrounding,
    )
  }

  @Test
  fun testZipWithNext() {
    val tuplesWithSurrounding = listOf(5, 6, 7).zipWithNext()
    assertEquals(listOf(Pair(5, 6), Pair(6, 7)), tuplesWithSurrounding)
  }
}
