package test.coordinate

import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import coordinate.isInCircle
import coordinate.isOnCircle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import util.times
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CircTest {

  @Test
  fun testConstructor() {
    assertEquals(Point.Zero, Circ(Point.Zero, 1).origin)
    assertEquals(Point.One, Circ(Point.One, 2).origin)
    assertEquals(Point.One, Circ(Point.One, 0).origin)
    assertEquals(2f, Circ(Point.One, 2).radius)
    assertEquals(0f, Circ(Point.One, 0).radius)
  }

  @Test
  fun testConstructorWithoutOrigin() {
    assertEquals(Point.Zero, Circ(1).origin)
    assertEquals(1f, Circ(1).radius)
  }

  @Test
  fun testAngleAtPointWhenPointOnCircleAtOrigin() {
    assertEquals(Deg(0), Circ(1).angleAtPoint(Point(1, 0)))
    assertEquals(Deg(90), Circ(1).angleAtPoint(Point(0, 1)))
    assertEquals(Deg(50), Circ(1).angleAtPoint(Point(cos(Deg(50).rad), sin(Deg(50).rad))))
  }

  @Test
  fun testAngleAtPointWhenPointNotOnCircleAtOrigin() {
    assertEquals(Deg(0), Circ(1).angleAtPoint(Point(2, 0)))
    assertEquals(Deg(90), Circ(1).angleAtPoint(Point(0, 0.5)))
    assertEquals(Deg(50), Circ(1).angleAtPoint(Point(cos(Deg(50).rad), sin(Deg(50).rad)) * 0.5))
    assertEquals(Deg(50), Circ(1).angleAtPoint(Point(cos(Deg(50).rad), sin(Deg(50).rad)) * 1.5))
  }

  @Test
  fun testAngleAtPointWhenNotAtOrigin() {
    val circ = Circ(Point(3, -3), 1)
    // when on circle
    assertEquals(Deg(0), circ.angleAtPoint(circ.origin + Point(1, 0)))
    assertEquals(Deg(90), circ.angleAtPoint(circ.origin + Point(0, 1)))
    assertEquals(Deg(50), circ.angleAtPoint(circ.origin + Point(cos(Deg(50).rad), sin(Deg(50).rad))))

    // when not on circle
    assertEquals(Deg(0), circ.angleAtPoint(circ.origin + Point(2, 0)))
    assertEquals(Deg(90), circ.angleAtPoint(circ.origin + Point(0, 0.5)))
    assertEquals(Deg(50), circ.angleAtPoint(circ.origin + Point(cos(Deg(50).rad), sin(Deg(50).rad)) * 0.5))
    assertEquals(Deg(50), circ.angleAtPoint(circ.origin + Point(cos(Deg(50).rad), sin(Deg(50).rad)) * 1.5))
  }

  @Test
  fun testPointAtAngle() {
    val circ = Circ(Point(3, -3), 1)
    assertEquals(Point(4, -3), circ.pointAtAngle(Deg(0)))
    assertEquals(Point(3, -2), circ.pointAtAngle(Deg(90)))
    assertEquals(Point(2, -3), circ.pointAtAngle(Deg(180)))
    assertEquals(Point(cos(Deg(359).rad), sin(Deg(359).rad)) + Point(3, -3), circ.pointAtAngle(Deg(359)))
  }

  @Test
  fun testIsOnCircle() {
    val circ = Circ(Point(3, -3), 1)

    1000.times {
      val deg = Deg(it.toFloat() / (360f * 1000))
      val p = Point(cos(deg.rad), sin(deg.rad)) + Point(3, -3)
      assertTrue("circ.isOnCircle(p) with deg: $deg, circ: $circ, p: $p") { circ.isOnCircle(p) }
      assertTrue("p.isOnCircle(circ) with deg: $deg, circ: $circ, p: $p") { p.isOnCircle(circ) }
    }
  }

  @Test
  fun testIsOnCircleWhenCircleSizeZero() {
    val circ = Circ(Point(3, -3), 0)
    assertFalse { circ.isOnCircle(Point(3, -3)) }
  }

  @Test
  fun testIsOnCircleWithInsideCircleReturnsFalse() {
    val circ = Circ(Point(3, -3), 1)

    1000.times {
      val deg = Deg(it.toFloat() / (360f * 1000))
      val pNotOnCircle = (Point(cos(deg.rad), sin(deg.rad)) * 0.9) + Point(3, -3)
      assertFalse("circ.isOnCircle(pNotOnCircle) with deg: $deg, circ: $circ, pNotOnCircle: $pNotOnCircle") { circ.isOnCircle(pNotOnCircle) }
      assertFalse("pNotOnCircle.isOnCircle(circ) with deg: $deg, circ: $circ, pNotOnCircle: $pNotOnCircle") { pNotOnCircle.isOnCircle(circ) }
    }
  }

  @Test
  fun testIsOnCircleWithOutsideCircleReturnsFalse() {
    val circ = Circ(Point(3, -3), 1)

    1000.times {
      val deg = Deg(it.toFloat() / (360f * 1000))
      val pNotOnCircle = (Point(cos(deg.rad), sin(deg.rad)) * 1.1) + Point(3, -3)
      assertFalse("circ.isOnCircle(pNotOnCircle) with deg: $deg, circ: $circ, pNotOnCircle: $pNotOnCircle") { circ.isOnCircle(pNotOnCircle) }
      assertFalse("pNotOnCircle.isOnCircle(circ) with deg: $deg, circ: $circ, pNotOnCircle: $pNotOnCircle") { pNotOnCircle.isOnCircle(circ) }
    }
  }

  @Test
  fun testIsInCircleWhenOnCircumference() {
    val circ = Circ(Point(3, -3), 1)

    1000.times {
      val deg = Deg(it.toFloat() / (360f * 1000))
      val p = Point(cos(deg.rad), sin(deg.rad)) + Point(3, -3)
      assertTrue("circ.isInCircle(p) with deg: $deg, circ: $circ, p: $p") { circ.isInCircle(p) }
      assertTrue("p.isInCircle(circ) with deg: $deg, circ: $circ, p: $p") { p.isInCircle(circ) }
    }
  }

  @Test
  fun testIsInCircleWhenInside() {
    val circ = Circ(Point(3, -3), 1)

    1000.times {
      val deg = Deg(it.toFloat() / (360f * 1000))
      val p = Point(cos(deg.rad), sin(deg.rad)) + Point(3, -3) * 0.9
      assertTrue("circ.isInCircle(p) with deg: $deg, circ: $circ, p: $p") { circ.isInCircle(p) }
      assertTrue("p.isInCircle(circ) with deg: $deg, circ: $circ, p: $p") { p.isInCircle(circ) }
    }
  }

  @Test
  fun testIsInCircleWhenOutside() {
    val circ = Circ(Point(3, -3), 1)

    1000.times {
      val deg = Deg(it.toFloat() / (360f * 1000))
      val p = Point(cos(deg.rad), sin(deg.rad)) + Point(3, -3) * 1.1
      assertFalse("circ.isInCircle(p) with deg: $deg, circ: $circ, p: $p") { circ.isInCircle(p) }
      assertFalse("p.isInCircle(circ) with deg: $deg, circ: $circ, p: $p") { p.isInCircle(circ) }
    }
  }

  @Test
  fun testIsInCircleWhenAtOrigin() {
    val circ = Circ(Point(3, -3), 1)
    assertTrue { circ.isInCircle(Point(3, -3)) }
  }

  @Test
  fun testIsInCircleWhenCircleSizeZero() {
    val circ = Circ(Point(3, -3), 0)
    assertFalse { circ.isInCircle(Point(3, -3)) }
  }
}