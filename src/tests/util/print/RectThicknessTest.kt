package tests.util.print

import coordinate.BoundRect
import coordinate.Point
import org.junit.jupiter.api.Test
import util.print.RectThickness
import util.print.RectThickness.ExtraExtraThick
import util.print.RectThickness.ExtraThick
import util.print.RectThickness.Regular
import util.print.RectThickness.Thick
import kotlin.test.assertEquals

internal class RectThicknessTest {
  @Test
  fun testGetBoundRectsToDraw() {
    val baseRect = BoundRect(Point(0, 0), 20, 20)

    RectThickness.values().forEach {
      val (expected, actual) = when (it) {
        Regular -> listOf(baseRect) to it.getBoundRectsToDraw(baseRect)
        Thick ->
          listOf(
            baseRect.shrink(0.2),
            baseRect,
            baseRect.expand(0.2),
          ) to it.getBoundRectsToDraw(baseRect)
        ExtraThick ->
          listOf(
            baseRect.shrink(0.4),
            baseRect.shrink(0.2),
            baseRect,
            baseRect.expand(0.2),
            baseRect.expand(0.4),
          ) to it.getBoundRectsToDraw(baseRect)
        ExtraExtraThick ->
          listOf(
            baseRect.shrink(1.0),
            baseRect.shrink(0.8),
            baseRect.shrink(0.6),
            baseRect.shrink(0.4),
            baseRect.shrink(0.2),
            baseRect,
            baseRect.expand(0.2),
            baseRect.expand(0.4),
            baseRect.expand(0.6),
            baseRect.expand(0.8),
            baseRect.expand(1.0),
          ) to it.getBoundRectsToDraw(baseRect)

      }

      assertEquals(
        expected.map { rect: BoundRect -> rect.width },
        actual.map { rect: BoundRect -> rect.width },
      )
      assertEquals(expected, actual)
    }
  }
}
