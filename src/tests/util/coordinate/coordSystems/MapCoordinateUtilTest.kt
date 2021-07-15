package tests.util.coordinate.coordSystems

import coordinate.BoundRect
import coordinate.Point
import coordinate.coordSystems.getCoordinateMap
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MapCoordinateUtilTest {

  @Test
  fun testMapIdentity() {
    val b1 = BoundRect(Point(10, 10), 10, 10)
    val b2 = BoundRect(Point(10, 10), 10, 10)

    val map = getCoordinateMap(b1, b2)

    assertEquals(Point(11, 11), map.transform(Point(11, 11)))
    assertEquals(Point(10, 10), map.transform(Point(10, 10)))
    assertEquals(Point(1, 1), map.transform(Point(1, 1)))
    assertEquals(Point(20, 20), map.transform(Point(20, 20)))
  }

  @Test
  fun testMapTranslated() {
    val b1 = BoundRect(Point(10, 10), 10, 10)
    val b2 = BoundRect(Point(5, 5), 10, 10)

    val map = getCoordinateMap(b1, b2)

    assertEquals(Point(6, 6), map.transform(Point(11, 11)))
    assertEquals(Point(5, 5), map.transform(Point(10, 10)))
    assertEquals(Point(-4, -4), map.transform(Point(1, 1)))
    assertEquals(Point(15, 15), map.transform(Point(20, 20)))
  }

  @Test
  fun testMapScaledCentered() {
    val b1 = BoundRect(Point(10, 10), 10, 10)
    val b2 = BoundRect(Point(5, 5), 20, 20)

    val map = getCoordinateMap(b1, b2)

    assertEquals(Point(7, 7), map.transform(Point(11, 11)))
    assertEquals(Point(5, 5), map.transform(Point(10, 10)))
    assertEquals(Point(-13, -13), map.transform(Point(1, 1)))
    assertEquals(Point(25, 25), map.transform(Point(20, 20)))
  }
}
