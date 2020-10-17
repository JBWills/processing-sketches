package test.coordinate

import coordinate.Arc
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ArcTest {

  @Test
  fun testPrimaryConstructor() {
    val a = Arc(Deg(90), 10f, Circ(Point.Zero, 5f))
    assertEquals(90f, a.startDeg.value)
    assertEquals(10f, a.lengthClockwise)
    assertEquals(Circ(Point.Zero, 5f), a)
  }

  @Test
  fun testCircleConstructor() {
    val a = Arc(Circ(Point.Zero, 5f))
    assertEquals(Deg(0), a.startDeg)
    assertEquals(360f, a.lengthClockwise)
  }

  @Test
  fun testDegreeConstructor() {
    var a = Arc(Deg(90), Deg(100), Circ(Point.Zero, 5f))
    assertEquals(Deg(90), a.startDeg)
    assertEquals(10f, a.lengthClockwise)


    a = Arc(Deg(90), Deg(50), Circ(Point.Zero, 5f))
    assertEquals(Deg(90), a.startDeg)
    assertEquals(320f, a.lengthClockwise)
  }

  @Test
  fun testStartAndEndpointConstructor() {
    var a = Arc(Point(0, -5), Point(0, 5), Circ(Point.Zero, 5f))
    assertEquals(Deg(270), a.startDeg)
    assertEquals(180f, a.lengthClockwise)


    a = Arc(Point(0, 5), Point(0, -5), Circ(Point.Zero, 5f))
    assertEquals(Deg(90), a.startDeg)
    assertEquals(180f, a.lengthClockwise)

    a = Arc(Point(0, 5), Point(0, 5), Circ(Point.Zero, 5f))
    assertEquals(Deg(90), a.startDeg)
    assertEquals(0f, a.lengthClockwise)
  }

  @Test
  fun testAngleBisector() {
    fun getBisector(start: Number, length: Number) = Arc(
      Deg(start.toFloat()),
      length.toFloat(),
      Circ(Point.Zero, 5f)
    ).angleBisector.value

    assertEquals(180f, getBisector(0, 360))
    assertEquals(90f, getBisector(0, 180))
    assertEquals(45f, getBisector(0, 90))
    assertEquals(0f, getBisector(0, 0))

    assertEquals(0f, getBisector(350, 20))
    assertEquals(355f, getBisector(350, 10))
    assertEquals(352.5f, getBisector(350, 5))
  }

  @Test
  fun testEngDeg() {
    fun getEndDeg(start: Number, length: Number) = Arc(
      Deg(start.toFloat()),
      length.toFloat(),
      Circ(Point.Zero, 5f)
    ).endDeg.value

    assertEquals(0f, getEndDeg(0, 360))
    assertEquals(180f, getEndDeg(0, 180))
    assertEquals(90f, getEndDeg(0, 90))
    assertEquals(0f, getEndDeg(0, 0))

    assertEquals(10f, getEndDeg(350, 20))
    assertEquals(0f, getEndDeg(350, 10))
    assertEquals(355f, getEndDeg(350, 5))
  }
}