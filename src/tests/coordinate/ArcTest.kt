package test.coordinate

import appletExtensions.clipCircInsideRect
import appletExtensions.clipCircOutsideRect
import appletExtensions.splitIntoArcsWhereIntersects
import coordinate.Arc
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ArcTest {

  private fun a(start: Number, l: Number) =
    Arc(Deg(start.toDouble()), l.toDouble(), Circ(Point.Zero, 1.0))

  @Test
  fun testPrimaryConstructor() {
    val a = Arc(Deg(90), 10.0, Circ(Point.Zero, 5.0))
    assertEquals(90.0, a.startDeg.value)
    assertEquals(10.0, a.lengthClockwise)
    assertEquals(Point.Zero, a.origin)
    assertEquals(5.0, a.radius)
  }

  @Test
  fun testCircleConstructor() {
    val a = Arc(Circ(Point.Zero, 5.0))
    assertEquals(Deg(0), a.startDeg)
    assertEquals(360.0, a.lengthClockwise)
  }

  @Test
  fun testDegreeConstructor() {
    var a = Arc(Deg(90), Deg(100), Circ(Point.Zero, 5.0))
    assertEquals(Deg(90), a.startDeg)
    assertEquals(10.0, a.lengthClockwise)


    a = Arc(Deg(90), Deg(50), Circ(Point.Zero, 5.0))
    assertEquals(Deg(90), a.startDeg)
    assertEquals(320.0, a.lengthClockwise)
  }

  @Test
  fun testStartAndEndpointConstructor() {
    var a = Arc(Point(0, -5), Point(0, 5), Circ(Point.Zero, 5.0))
    assertEquals(Deg(270), a.startDeg)
    assertEquals(180.0, a.lengthClockwise)


    a = Arc(Point(0, 5), Point(0, -5), Circ(Point.Zero, 5.0))
    assertEquals(Deg(90), a.startDeg)
    assertEquals(180.0, a.lengthClockwise)

    a = Arc(Point(0, 5), Point(0, 5), Circ(Point.Zero, 5.0))
    assertEquals(Deg(90), a.startDeg)
    assertEquals(0.0, a.lengthClockwise)
  }

  @Test
  fun testAngleBisector() {
    fun getBisector(start: Number, length: Number) = Arc(
      Deg(start.toDouble()),
      length.toDouble(),
      Circ(Point.Zero, 5.0)
    ).angleBisector.value

    assertEquals(180.0, getBisector(0, 360))
    assertEquals(90.0, getBisector(0, 180))
    assertEquals(45.0, getBisector(0, 90))
    assertEquals(0.0, getBisector(0, 0))

    assertEquals(0.0, getBisector(350, 20))
    assertEquals(355.0, getBisector(350, 10))
    assertEquals(352.5, getBisector(350, 5))
  }

  @Test
  fun testEngDeg() {
    fun getEndDeg(start: Number, length: Number) = Arc(
      Deg(start.toDouble()),
      length.toDouble(),
      Circ(Point.Zero, 5.0)
    ).endDeg.value

    assertEquals(0.0, getEndDeg(0, 360))
    assertEquals(180.0, getEndDeg(0, 180))
    assertEquals(90.0, getEndDeg(0, 90))
    assertEquals(0.0, getEndDeg(0, 0))

    assertEquals(10.0, getEndDeg(350, 20))
    assertEquals(0.0, getEndDeg(350, 10))
    assertEquals(355.0, getEndDeg(350, 5))
  }

  @Test
  fun testGetOverlapWithDifferentBaseCircles() {
    val circSmallAtOrigin = Circ(Point.Zero, 1.0)
    val circLargeAtOrigin = Circ(Point.Zero, 5.0)
    val circSmallAtOne = Circ(Point.One, 1.0)
    val circLargeAtOne = Circ(Point.One, 5.0)

    fun arcWithCirc(c: Circ) = Arc(Deg(5), 5.0, c)
    assertEquals(EMPTY, arcWithCirc(circSmallAtOrigin).getOverlap(arcWithCirc(circLargeAtOrigin)))
    assertEquals(EMPTY, arcWithCirc(circLargeAtOrigin).getOverlap(arcWithCirc(circSmallAtOrigin)))
    assertEquals(EMPTY, arcWithCirc(circSmallAtOrigin).getOverlap(arcWithCirc(circSmallAtOne)))
    assertEquals(EMPTY, arcWithCirc(circSmallAtOrigin).getOverlap(arcWithCirc(circLargeAtOne)))
    assertEquals(EMPTY, arcWithCirc(circSmallAtOne).getOverlap(arcWithCirc(circSmallAtOrigin)))
    assertEquals(EMPTY, arcWithCirc(circSmallAtOne).getOverlap(arcWithCirc(circLargeAtOne)))
    assertEquals(EMPTY, arcWithCirc(circLargeAtOne).getOverlap(arcWithCirc(circSmallAtOne)))
  }

  val EMPTY = listOf<Arc>()

  @Test
  fun testGetOverlapWhenSeparate() {
    // Totally separate
    assertEquals(EMPTY, a(5, 5).getOverlap(a(11, 5)))
    assertEquals(EMPTY, a(11, 5).getOverlap(a(5, 5)))

    // Touching but not overlapping
    assertEquals(EMPTY, a(5, 5).getOverlap(a(10, 5)))
    assertEquals(EMPTY, a(10, 5).getOverlap(a(5, 5)))
    assertEquals(EMPTY, a(10, 350).getOverlap(a(0, 10)))

    // crossing 0, not touching
    assertEquals(EMPTY, a(350, 20).getOverlap(a(20, 10)))
    assertEquals(EMPTY, a(20, 10).getOverlap(a(350, 20)))

    // crossing 0, touching
    assertEquals(EMPTY, a(350, 20).getOverlap(a(10, 10)))
    assertEquals(EMPTY, a(10, 10).getOverlap(a(350, 20)))
  }

  @Test
  fun testGetOverlapWhenExactlyOverlapping() {
    assertEquals(listOf(a(5, 5)), a(5, 5).getOverlap(a(5, 5)))
    assertEquals(listOf(a(5, 50)), a(5, 50).getOverlap(a(5, 50)))
    assertEquals(listOf(a(359, 50)), a(359, 50).getOverlap(a(359, 50)))
  }

  @Test
  fun testGetOverlapWhenOneHasSizeZero() {
    assertEquals(EMPTY, a(5, 0).getOverlap(a(5, 5)))
    assertEquals(EMPTY, a(5, 5).getOverlap(a(5, 0)))
    assertEquals(EMPTY, a(5, 0).getOverlap(a(5, 0)))
  }

  @Test
  fun testGetOverlapWhenOneCoversOther() {
    assertEquals(listOf(a(5, 5)), a(5, 5).getOverlap(a(4, 7)))
    assertEquals(listOf(a(5, 5)), a(4, 7).getOverlap(a(5, 5)))

    // larger crosses 0
    assertEquals(listOf(a(355, 3)), a(355, 3).getOverlap(a(350, 20)))
    assertEquals(listOf(a(355, 3)), a(350, 20).getOverlap(a(355, 3)))
    assertEquals(listOf(a(0, 3)), a(0, 3).getOverlap(a(350, 20)))
    assertEquals(listOf(a(0, 3)), a(350, 20).getOverlap(a(0, 3)))

    // both cross 0
    assertEquals(listOf(a(355, 7)), a(355, 7).getOverlap(a(350, 20)))
    assertEquals(listOf(a(355, 7)), a(350, 20).getOverlap(a(355, 7)))
  }

  @Test
  fun testGetOverlapWithPartialOverlap() {
    assertEquals(listOf(a(5, 3)), a(4, 4).getOverlap(a(5, 7)))
    assertEquals(listOf(a(5, 4)), a(5, 7).getOverlap(a(4, 5)))

    // first crosses 0
    assertEquals(listOf(a(355, 3)), a(355, 3).getOverlap(a(350, 20)))
    assertEquals(listOf(a(355, 3)), a(350, 20).getOverlap(a(355, 3)))
    assertEquals(listOf(a(0, 3)), a(0, 3).getOverlap(a(350, 20)))
    assertEquals(listOf(a(0, 3)), a(350, 20).getOverlap(a(0, 3)))
    assertEquals(listOf(a(0, 10)), a(350, 20).getOverlap(a(0, 13)))

    // both cross 0
    assertEquals(listOf(a(355, 7)), a(355, 7).getOverlap(a(350, 20)))
    assertEquals(listOf(a(355, 7)), a(350, 20).getOverlap(a(355, 7)))

    assertEquals(listOf(a(140, 6)), a(140, 260).getOverlap(a(115, 31)))
  }

  @Test
  fun testGetOverlapWithWrapAround() {
    assertEquals(listOf<Arc>(), a(0, 180).getOverlap(a(180, 180)))
    assertEquals(listOf(a(0, 10)), a(0, 180).getOverlap(a(180, 190)))
    assertEquals(listOf(a(0, 10), a(170, 10)), a(0, 180).getOverlap(a(170, 200)))
    assertEquals(listOf(a(0, 10), a(170, 10)), a(170, 200).getOverlap(a(0, 180)))
    assertEquals(
      listOf(a(170, 190)),
      a(170, 190).getOverlap(Arc(Deg(0), 360.0, Circ(Point.Zero, 1.0)))
    )

    assertEquals(listOf(a(4, 6), a(15, 9)), a(4, 20).getOverlap(a(15, 355)))
    assertEquals(listOf(a(4, 6), a(15, 9)), a(15, 355).getOverlap(a(4, 20)))
  }

  @Test
  fun testContains() {
    assertTrue(a(10, 10).contains(11))
    assertTrue(a(10, 10).contains(10))
    assertTrue(a(10, 10).contains(20))

    assertFalse(a(90, 100).contains(85))
    assertFalse(a(90, 100).contains(200))

    // circle should contain everything
    assertTrue(Arc(Circ(Point.Zero, 5.0)).contains(20))
    assertTrue(Arc(Circ(Point.Zero, 5.0)).contains(0))
    assertTrue(Arc(Circ(Point.Zero, 5.0)).contains(359))

    // size zero arc shouldn't contain anything
    assertFalse(a(10, 0).contains(20))
    assertFalse(a(10, 0).contains(10))

    // when crosses 0
    assertTrue(a(350, 20).contains(0))
    assertTrue(a(350, 20).contains(351))
    assertTrue(a(350, 20).contains(9))
    assertTrue(a(350, 20).contains(10))
    assertTrue(a(350, 20).contains(350))

    assertFalse(a(350, 20).contains(349))
    assertFalse(a(350, 20).contains(11))
  }

  @Test
  fun testContainsArc() {
    assertTrue(a(10, 10).contains(a(11, 1)))
    assertTrue(a(10, 10).contains(a(10, 1)))
    assertTrue(a(10, 10).contains(a(19, 1)))
    assertTrue(a(10, 10).contains(a(10, 10)))
    assertTrue(a(10, 10).contains(a(11, 8)))
    assertFalse(a(10, 10).contains(a(11, 10)))
    assertFalse(a(10, 10).contains(a(9, 10)))
    assertFalse(a(10, 10).contains(a(9, 1)))


    assertTrue(a(350, 350).contains(a(80, 120)))
    assertTrue(a(10, 355).contains(a(80, 120)))
    assertTrue(a(90, 355).contains(a(350, 20)))
    assertTrue(a(90, 355).contains(a(350, 5)))
    assertFalse(a(90, 355).contains(a(80, 120)))

    assertFalse(a(170, 190).contains(a(0, 360)))
    assertFalse(a(170, 190).contains(a(1, 360)))
  }

  @Test
  fun testSplitIntoArcsWhereIntersects() {
    assertEquals(
      2,
      Circ(5).splitIntoArcsWhereIntersects(BoundRect(Point(-100, -100), 103.0, 200.0)).size
    )
    assertEquals(1, Circ(5).splitIntoArcsWhereIntersects(BoundRect(Point(-5, -5), 10.0, 10.0)).size)
    assertEquals(8, Circ(6).splitIntoArcsWhereIntersects(BoundRect(Point(-5, -5), 10.0, 10.0)).size)
    assertEquals(
      listOf(Arc(Deg(0), Deg(90), Circ(1)), Arc(Deg(90), Deg(0), Circ(1))),
      Circ(1).splitIntoArcsWhereIntersects(BoundRect(Point.Zero, 10.0, 10.0))
    )
  }

  @Test
  fun testClipCircInsideRect() {
    assertEquals(1, Circ(1).clipCircInsideRect(BoundRect(Point.Zero, 10.0, 10.0)).size)
    assertEquals(
      listOf(Arc(Deg(0), Deg(90), Circ(1))),
      Circ(1).clipCircInsideRect(BoundRect(Point.Zero, 10.0, 10.0))
    )
  }

  @Test
  fun testClipCircOutsideRect() {
    assertEquals(1, Circ(1).clipCircInsideRect(BoundRect(Point.Zero, 10.0, 10.0)).size)
    assertEquals(
      listOf(Arc(Deg(90), Deg(0), Circ(1))),
      Circ(1).clipCircOutsideRect(BoundRect(Point.Zero, 10.0, 10.0))
    )
  }

  @Test
  fun testMinusWhenCompleteOverlap() {
    assertEquals(listOf<Arc>(), a(10, 10) - a(0, 40))
    assertEquals(listOf<Arc>(), a(10, 10) - a(10, 10))
    assertEquals(listOf<Arc>(), a(10, 10) - a(10, 20))
    assertEquals(listOf<Arc>(), a(10, 10) - a(0, 20))
    assertEquals(listOf<Arc>(), a(10, 0) - a(0, 20))
    assertEquals(listOf<Arc>(), a(20, 0) - a(0, 20))
    assertEquals(listOf<Arc>(), a(355, 10) - a(354, 12))
  }

  @Test
  fun testMinusWhenNoOverlap() {
    assertEquals(listOf(a(10, 10)), a(10, 10) - a(30, 10))
    assertEquals(listOf(a(10, 10)), a(10, 10) - a(30, 335))
    assertEquals(listOf(a(355, 10)), a(355, 10) - a(30, 10))
  }

  @Test
  fun testMinusWhenOverlapFromStart() {
    assertEquals(listOf(a(10, 10)), a(10, 20) - a(20, 10))
    assertEquals(listOf(a(10, 10)), a(10, 20) - a(20, 30))
  }

  @Test
  fun testMinusWhenOverlapFromEnd() {
    assertEquals(listOf(a(15, 15)), a(10, 20) - a(350, 25))
  }

  @Test
  fun testMinusWhenOverlapFromBothEnds() {
    assertEquals(listOf(a(10, 160)), a(0, 180) - a(170, 200))
  }

  @Test
  fun testMinusAll() {
    assertEquals(listOf(a(10, 10)), a(10, 20).minusAll(listOf(a(20, 10))))
    assertEquals(listOf(a(30, 350)), a(10, 360).minusAll(listOf(a(20, 10))))
    assertEquals(listOf(a(40, 340)), a(10, 360).minusAll(listOf(a(20, 10), a(30, 10))))
    assertEquals(listOf(a(30, 10), a(50, 330)), a(10, 360).minusAll(listOf(a(20, 10), a(40, 10))))
    assertEquals(
      listOf(
        a(30, 10),
        a(50, 30),
        a(180, 200)
      ),
      a(10, 360).minusAll(
        listOf(
          a(20, 10),
          a(40, 10),
          a(80, 100)
        )
      )
    )

  }

  @Test
  fun textExpand() {
    assertEquals(a(0, 10), a(0, 10).expandDeg(0.0))
    assertEquals(a(359, 12), a(0, 10).expandDeg(1.0))
    assertEquals(a(1, 8), a(0, 10).expandDeg(-1.0))
    assertEquals(a(5, 0), a(0, 10).expandDeg(-5.0))
    assertEquals(a(5, 0), a(0, 10).expandDeg(-8.0))
    assertEquals(a(90, 360), a(0, 90).expandDeg(270.0))
  }
}
