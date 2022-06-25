package tests.util.iterators

import org.junit.jupiter.api.Test
import util.iterators.uniqByInPlace
import kotlin.test.assertEquals

internal class UniqByTest {
  private fun <T : Comparable<T>> t(
    l: List<String>,
    by: (String) -> T,
    expected: List<String>,
    keepFirst: Boolean = true
  ) {
    val mL = l.toMutableList()
    mL.uniqByInPlace(keepFirst, by)
    assertEquals(expected, mL)
  }

  @Test
  fun testEmpty() {
    t(listOf(), { it }, listOf())
    t(listOf(), { it }, listOf(), keepFirst = false)
  }

  @Test
  fun testSingleElement() {
    t(listOf("a"), { true }, listOf("a"))
    t(listOf("a"), { true }, listOf("a"), keepFirst = false)
  }

  @Test
  fun testRemoveLast() {
    t(listOf("a", "b", "a"), { it }, listOf("a", "b"))
  }

  @Test
  fun testRemoveFirst() {
    t(listOf("a", "b", "a"), { it }, listOf("b", "a"), keepFirst = false)
  }

  @Test
  fun testRemoveMiddle() {
    t(listOf("a", "b", "c", "b", "d"), { it }, listOf("a", "b", "c", "d"))
    t(listOf("a", "b", "c", "b", "d"), { it }, listOf("a", "c", "b", "d"), keepFirst = false)
  }

  @Test
  fun testRemoveMulti() {
    t(listOf("b", "a", "b", "c", "b", "b", "b", "a"), { it }, listOf("b", "a", "c"))
    t(
      listOf("b", "a", "b", "c", "b", "b", "b", "a"),
      { it },
      listOf("c", "b", "a"),
      keepFirst = false,
    )
  }

  @Test
  fun testAllTheSame() {
    t(listOf("a", "a", "a", "a", "a", "a", "a", "a", "a", "a"), { it }, listOf("a"))
    t(
      listOf("a", "a", "a", "a", "a", "a", "a", "a", "a", "a"),
      { it },
      listOf("a"),
      keepFirst = false,
    )
  }

  @Test
  fun testByLength() {
    t(
      listOf(
        "abc",
        "apple",
        "acorn",
        "abcd",
      ),
      { it.length }, listOf("abc", "apple", "abcd"),
    )
    t(
      listOf(
        "abc",
        "apple",
        "acorn",
        "abcd",
      ),
      { it.length }, listOf("abc", "acorn", "abcd"),
      keepFirst = false,
    )
  }
}
