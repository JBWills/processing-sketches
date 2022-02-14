package tests.util.image.opencvContouring

import coordinate.BoundRect
import coordinate.Point
import nu.pattern.OpenCV
import org.junit.jupiter.api.Test
import org.opencv.core.Mat
import util.image.ImageFormat.Gray
import util.image.opencvMat.copyTo
import util.image.opencvMat.submat
import util.image.opencvMat.toIntArray
import kotlin.test.assertEquals

internal class MatFiltersTest {

  init {
    OpenCV.loadLocally()
  }

  fun Array<IntArray>.arrStr() =
    "\n" + joinToString("\n") { row -> row.joinToString(" ") } + "\n"

  @Test
  fun testSubmat() {

    val smallMatOnes = Mat.ones(3, 3, Gray.openCVFormat!!)
    assertEquals(
      arrayOf(
        intArrayOf(1, 1, 1),
        intArrayOf(1, 1, 1),
        intArrayOf(1, 1, 1),
      ).arrStr(),
      smallMatOnes.submat(BoundRect(Point.Zero, Point(2))).toIntArray().arrStr(),
    )

    assertEquals(
      arrayOf(
        intArrayOf(1, 1),
        intArrayOf(1, 1),
      ).arrStr(),
      smallMatOnes.submat(BoundRect(Point.Zero, Point(1))).toIntArray().arrStr(),
    )

    assertEquals(
      arrayOf(
        intArrayOf(1, 1),
        intArrayOf(1, 1),
        intArrayOf(1, 1),
      ).arrStr(),
      smallMatOnes.submat(BoundRect(Point.Zero, Point(1, 2))).toIntArray().arrStr(),
    )

    assertEquals(
      arrayOf(
        intArrayOf(1, 1),
        intArrayOf(1, 1),
      ).arrStr(),
      smallMatOnes.submat(BoundRect(Point(0, 1), Point(1, 2))).toIntArray().arrStr(),
    )
  }

  @Test
  fun testCopyTo() {

    val smallMatOnes = Mat.ones(3, 3, Gray.openCVFormat!!)
    val largeMatZeros = Mat.zeros(5, 5, Gray.openCVFormat)

    val newMatTopLeft = smallMatOnes.copyTo(largeMatZeros, Point.Zero)
    assertEquals(
      arrayOf(
        intArrayOf(1, 1, 1, 0, 0),
        intArrayOf(1, 1, 1, 0, 0),
        intArrayOf(1, 1, 1, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
      ).arrStr(),
      newMatTopLeft.toIntArray().arrStr(),
    )

    val newMatBottomRight = smallMatOnes.copyTo(largeMatZeros, Point(2))
    assertEquals(
      arrayOf(
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 1, 1, 1),
        intArrayOf(0, 0, 1, 1, 1),
        intArrayOf(0, 0, 1, 1, 1),
      ).arrStr(),
      newMatBottomRight.toIntArray().arrStr(),
    )

    val newMatBottomLeft = smallMatOnes.copyTo(largeMatZeros, Point(0, 2))
    assertEquals(
      arrayOf(
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(1, 1, 1, 0, 0),
        intArrayOf(1, 1, 1, 0, 0),
        intArrayOf(1, 1, 1, 0, 0),
      ).arrStr(),
      newMatBottomLeft.toIntArray().arrStr(),
    )

    val newMatTopRight = smallMatOnes.copyTo(largeMatZeros, Point(2, 0))
    assertEquals(
      arrayOf(
        intArrayOf(0, 0, 1, 1, 1),
        intArrayOf(0, 0, 1, 1, 1),
        intArrayOf(0, 0, 1, 1, 1),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
      ).arrStr(),
      newMatTopRight.toIntArray().arrStr(),
    )

    val newMatCentered = smallMatOnes.copyTo(largeMatZeros, Point(1, 1))
    assertEquals(
      arrayOf(
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 1, 1, 1, 0),
        intArrayOf(0, 1, 1, 1, 0),
        intArrayOf(0, 1, 1, 1, 0),
        intArrayOf(0, 0, 0, 0, 0),
      ).arrStr(),
      newMatCentered.toIntArray().arrStr(),
    )

    val newMatClippedTopLeft = smallMatOnes.copyTo(largeMatZeros, Point(-1, -1))
    assertEquals(
      arrayOf(
        intArrayOf(1, 1, 0, 0, 0),
        intArrayOf(1, 1, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
      ).arrStr(),
      newMatClippedTopLeft.toIntArray().arrStr(),
    )

    val newMatClippedTopRight = smallMatOnes.copyTo(largeMatZeros, Point(3, -1))
    assertEquals(
      arrayOf(
        intArrayOf(0, 0, 0, 1, 1),
        intArrayOf(0, 0, 0, 1, 1),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
      ).arrStr(),
      newMatClippedTopRight.toIntArray().arrStr(),
    )

    val newMatClippedBottomLeft = smallMatOnes.copyTo(largeMatZeros, Point(-1, 3))
    assertEquals(
      arrayOf(
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(1, 1, 0, 0, 0),
        intArrayOf(1, 1, 0, 0, 0),
      ).arrStr(),
      newMatClippedBottomLeft.toIntArray().arrStr(),
    )

    val newMatClippedBottomRight = smallMatOnes.copyTo(largeMatZeros, Point(3, 3))
    assertEquals(
      arrayOf(
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 1, 1),
        intArrayOf(0, 0, 0, 1, 1),
      ).arrStr(),
      newMatClippedBottomRight.toIntArray().arrStr(),
    )

    val newMatOffScreen = smallMatOnes.copyTo(largeMatZeros, Point(5, 0))
    assertEquals(
      arrayOf(
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0),
      ).arrStr(),
      newMatOffScreen.toIntArray().arrStr(),
    )

    val largeMatOntoSmallMatTopLeft = largeMatZeros.copyTo(smallMatOnes, Point(0, 0))
    assertEquals(
      arrayOf(
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
      ).arrStr(),
      largeMatOntoSmallMatTopLeft.toIntArray().arrStr(),
    )

    val largeMatOntoSmallMatCentered = largeMatZeros.copyTo(smallMatOnes, Point(-1, -1))
    assertEquals(
      arrayOf(
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
        intArrayOf(0, 0, 0),
      ).arrStr(),
      largeMatOntoSmallMatCentered.toIntArray().arrStr(),
    )

    val largeMatOntoSmallClipped = largeMatZeros.copyTo(smallMatOnes, Point(1, 1))
    assertEquals(
      arrayOf(
        intArrayOf(1, 1, 1),
        intArrayOf(1, 0, 0),
        intArrayOf(1, 0, 0),
      ).arrStr(),
      largeMatOntoSmallClipped.toIntArray().arrStr(),
    )
  }
}
