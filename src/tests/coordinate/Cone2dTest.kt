package tests.coordinate

import coordinate.Cone2D
import coordinate.Deg
import coordinate.Point
import coordinate.Ray
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class Cone2dTest {

  @Test
  fun testContainsSimple() {
    // cone containing 90 degree slice from southEast to northEast
    val cone = Cone2D(Point.Zero, Deg(0), 90.0)

    val degreesThatContainPoint = (45 downTo -45 step 5).toList()
    val degreesThatDontContainPoint = (46..314 step 5).toList() + 314

    degreesThatContainPoint.forEach {
      assertTrue(cone.contains(Deg(it).unitVector), "Should contain point with degree: $it")
    }
    degreesThatDontContainPoint.forEach {
      assertFalse(cone.contains(Deg(it).unitVector), "Should NOT contain point with degree: $it")
    }
  }

  @Test
  fun testContainsReflex() {
    // cone containing everything but 90 degree slice from southEast to northEast
    val cone = Cone2D(Point.Zero, Deg(180), 360.0 - 90.0)

    val degreesThatDontContainPoint = (44 downTo -44 step 5).toList()
    val degreesThatContainPoint = (45..315 step 5).toList() + 314

    degreesThatContainPoint.forEach {
      assertTrue(cone.contains(Deg(it).unitVector), "Should contain point with degree: $it")
    }
    degreesThatDontContainPoint.forEach {
      assertFalse(cone.contains(Deg(it).unitVector), "Should NOT contain point with degree: $it")
    }
  }

  @Test
  fun testCreateFromRays() {
    // cone containing 90 degree slice from southEast to northEast
    val cone = Cone2D.fromRays(
      Ray(Point.Zero, Deg(45)),
      Ray(Point.Zero, Deg(-45)),
      isGreaterThan180 = false,
    )

    var degreesThatContainPoint = (45 downTo -45 step 5).toList()
    var degreesThatDontContainPoint = (46..314 step 5).toList() + 314

    degreesThatContainPoint.forEach {
      assertTrue(cone.contains(Deg(it).unitVector), "Should contain point with degree: $it")
    }
    degreesThatDontContainPoint.forEach {
      assertFalse(cone.contains(Deg(it).unitVector), "Should NOT contain point with degree: $it")
    }

    // cone containing everything but 90 degree slice from southEast to northEast
    val cone2 = Cone2D.fromRays(
      Ray(Point.Zero, Deg(45)),
      Ray(Point.Zero, Deg(-45)),
      isGreaterThan180 = true,
    )

    degreesThatDontContainPoint = (44 downTo -44 step 5).toList()
    degreesThatContainPoint = (45..315 step 5).toList() + 314

    degreesThatContainPoint.forEach {
      assertTrue(cone2.contains(Deg(it).unitVector), "Should contain point with degree: $it")
    }
    degreesThatDontContainPoint.forEach {
      assertFalse(cone2.contains(Deg(it).unitVector), "Should NOT contain point with degree: $it")
    }
  }
}
