package tests.util.numbers

import org.junit.Test
import util.numbers.floorInt
import util.numbers.isEven
import kotlin.test.assertEquals

internal class NumberExtensionsTest {
  @Test
  fun testFloorInt() {
    assert(1.1.floorInt() == 1)
    assert(1.9.floorInt() == 1)
    assert((-1.4).floorInt() == -1)
  }

  @Test
  fun testIsEven() {
    assert(2.isEven())
    assert(8.isEven())
    assertEquals(false, 9.isEven())
    assertEquals(false, (-1).isEven())
    assertEquals(true, (-2).isEven())
    assertEquals(true, (-4).isEven())
  }
}
