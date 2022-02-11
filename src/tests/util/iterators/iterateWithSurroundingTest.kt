package tests.util.iterators

import org.junit.jupiter.api.Test
import util.iterators.mapWithSurroundingIndexed
import util.tuple.Pair4
import util.tuple.and
import kotlin.test.assertEquals

typealias Result = Pair4<Int, Double?, Double, Double?>

internal class IterateWithSurroundingTest {
  private val returnAsPair: (Int, Double?, Double, Double?) -> Result =
    { index, prev, curr, next -> index and prev and curr and next }

  private val baseList = listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)

  @Test
  fun testWithEmptyList() {
    assertEquals(
      listOf(),
      baseList.take(0).mapWithSurroundingIndexed(returnAsPair),
    )
  }

  @Test
  fun testWithOneItem() {
    assertEquals<List<Result>>(
      listOf(Pair4(0, null, 1.0, null)),
      baseList.take(1).mapWithSurroundingIndexed(returnAsPair),
    )
  }

  @Test
  fun testWithTwoItems() {
    assertEquals<List<Result>>(
      listOf(Pair4(0, null, 1.0, 2.0), Pair4(1, 1.0, 2.0, null)),
      baseList.take(2).mapWithSurroundingIndexed(returnAsPair),
    )
  }

  @Test
  fun testWithThreeItems() {
    assertEquals<List<Result>>(
      listOf(
        Pair4(0, null, 1.0, 2.0),
        Pair4(1, 1.0, 2.0, 3.0),
        Pair4(2, 2.0, 3.0, null),
      ),
      baseList.take(3).mapWithSurroundingIndexed(returnAsPair),
    )
  }

  @Test
  fun testWithFourItems() {
    assertEquals<List<Result>>(
      listOf(
        Pair4(0, null, 1.0, 2.0),
        Pair4(1, 1.0, 2.0, 3.0),
        Pair4(2, 2.0, 3.0, 4.0),
        Pair4(3, 3.0, 4.0, null),
      ),
      baseList.take(4).mapWithSurroundingIndexed(returnAsPair),
    )
  }
}
