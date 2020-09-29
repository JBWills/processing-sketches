package test.coordinate

import coordinate.Deg
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DegTest {

  @Test
  fun testConstructor() {
    assertEquals(0, Deg(0).value)
    assertEquals(90, Deg(90).value)
    assertEquals(350, Deg(-10).value)
    assertEquals(180, Deg(-180).value)
    assertEquals(0, Deg(360).value)
    assertEquals(0, Deg(-360).value)
    assertEquals(359, Deg(-361).value)
    assertEquals(1, Deg(361).value)
    assertEquals(180, Deg(-900).value)
  }

  @Test
  fun testPlus() {
    assertEquals(0, (Deg(90) + Deg(-90)).value)
    assertEquals(0, (Deg(0) + Deg(0)).value)
    assertEquals(10, (Deg(10) + Deg(0)).value)
    assertEquals(10, (Deg(0) + Deg(10)).value)
    assertEquals(20, (Deg(10) + Deg(10)).value)
    assertEquals(180, (Deg(300) + Deg(600)).value)
  }

  @Test
  fun testMinus() {
    assertEquals(180, (Deg(90) - Deg(-90)).value)
    assertEquals(0, (Deg(0) - Deg(0)).value)
    assertEquals(10, (Deg(10) - Deg(0)).value)
    assertEquals(350, (Deg(0) - Deg(10)).value)
    assertEquals(0, (Deg(10) - Deg(10)).value)
    assertEquals(60, (Deg(300) - Deg(600)).value)
  }

  @Test
  fun testRotation() {
    assertEquals(10, Deg(90).rotation(Deg(80)))
    assertEquals(90, Deg(90).rotation(Deg(360)))
    assertEquals(0, Deg(0).rotation(Deg(0)))
    assertEquals(1, Deg(0).rotation(Deg(359)))
    assertEquals(45, Deg(315).rotation(Deg(270)))
    assertEquals(135, Deg(315).rotation(Deg(90)))
    assertEquals(45, Deg(270).rotation(Deg(315)))
    assertEquals(135, Deg(90).rotation(Deg(315)))
  }

  @Test
  fun testIsHorizontal() {
    assertEquals(false, Deg(90).isHorizontal())
    assertEquals(false, Deg(270).isHorizontal())
    assertEquals(false, Deg(45).isHorizontal())
    assertEquals(false, Deg(1).isHorizontal())
    assertEquals(true, Deg(0).isHorizontal())
    assertEquals(true, Deg(180).isHorizontal())
    assertEquals(true, Deg(360).isHorizontal())
  }

  @Test
  fun testIsVertical() {
    assertEquals(true, Deg(90).isVertical())
    assertEquals(true, Deg(270).isVertical())
    assertEquals(false, Deg(45).isVertical())
    assertEquals(false, Deg(1).isVertical())
    assertEquals(false, Deg(0).isVertical())
    assertEquals(false, Deg(180).isVertical())
    assertEquals(false, Deg(360).isVertical())
  }
}