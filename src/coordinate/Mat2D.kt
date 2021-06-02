package coordinate

import kotlinx.serialization.Serializable
import mikera.matrixx.Matrix
import mikera.matrixx.impl.ADenseArrayMatrix
import mikera.matrixx.impl.AStridedMatrix
import util.ceilInt
import util.io.serialization.MatrixSerializer
import util.iterators.mapArray

fun Matrix.bounds(offset: Point = Point.Zero) = BoundRect(offset, sizeX, sizeY)

fun getOverlap(
  matrix1Offset: Point,
  matrix1: Matrix,
  matrix2Offset: Point,
  matrix2: Matrix
): Pair<AStridedMatrix, AStridedMatrix>? {
  val bounds1 = matrix1.bounds(matrix1Offset)
  val bounds2 = matrix2.bounds(matrix2Offset)

  val intersection = bounds1.boundsIntersection(bounds2)

  return intersection?.let { matrix1.subMatrix(it - matrix1Offset) to matrix2.subMatrix(it - matrix2Offset) }
}

fun Matrix.croppedToSize(sizeX: Int, sizeY: Int): Matrix =
  Matrix(sizeX, sizeY).also { it.setElements(toDoubleArray(), 0) }

fun Matrix.subMatrix(rect: BoundRect): AStridedMatrix = subMatrix(
  rect.left.toInt(),
  rect.width.toInt(),
  rect.top.toInt(),
  rect.height.toInt(),
)

fun Matrix.subMatrix(offset: Point, size: Matrix) =
  subMatrix(size.bounds(offset))

fun Matrix.subMatrixCopy(rect: BoundRect): ADenseArrayMatrix = subMatrix(rect).clone()

val Matrix.sizeX get() = rowCount()
val Matrix.sizeY get() = columnCount()

fun Matrix.addi(offset: Point, other: Matrix): Matrix {
  getOverlap(Point.Zero, this, offset, other)
    ?.let { (thisSubMatrix, otherSubMatrix) ->
      thisSubMatrix.add(otherSubMatrix)
    }

  return this
}

fun Matrix.subtracti(offset: Point, other: Matrix): Matrix {
  getOverlap(Point.Zero, this, offset, other)
    ?.let { (thisSubMatrix, otherSubMatrix) ->
      thisSubMatrix.subtract(otherSubMatrix)
    }

  return this
}

fun Matrix.multiplyi(offset: Point, other: Matrix): Matrix {
  getOverlap(Point.Zero, this, offset, other)
    ?.let { (thisSubMatrix, otherSubMatrix) ->
      thisSubMatrix.multiply(otherSubMatrix)
    }

  return this
}

@Serializable
class Mat2D private constructor(@Serializable(with = MatrixSerializer::class) var backingArr: Matrix) {

  val width get() = backingArr.columnCount()
  val height get() = backingArr.rowCount()

  constructor(sizeX: Int, sizeY: Int) : this(Matrix(sizeX, sizeY))

  fun setNewSize(sizeX: Int, sizeY: Int) {
    backingArr = backingArr.croppedToSize(sizeX, sizeY)
  }

  fun get(p: Point) = backingArr.get(p.xl, p.yl)

  fun subMatrix(rect: BoundRect) = backingArr.subMatrix(rect)
  fun subMatrixCopy(rect: BoundRect) = backingArr.subMatrixCopy(rect)

  fun addCentered(centerPoint: Point, values: Mat2D) {
    val offset = backingArr.bounds().recentered(centerPoint).topLeft

    backingArr.addi(offset, values.backingArr)
  }

  fun subtractCentered(centerPoint: Point, values: Mat2D) {
    val offset = backingArr.bounds().recentered(centerPoint).topLeft
    backingArr.subtracti(offset, values.backingArr)
  }

  fun setAll(newValue: Double) {
    backingArr = Matrix(backingArr.rowCount(), backingArr.columnCount()).apply {
      setElements(DoubleArray(width * height) { newValue }, 0)
    }
  }

  fun toIntMatrix(scale: Int = 1): IntArray =
    backingArr.array.mapArray { (it * scale).toInt() }.toIntArray()

  fun clear() {
    backingArr = Matrix(backingArr.rowCount(), backingArr.columnCount())
  }

  companion object {
    fun createCircle(c: Circ, intensity: Double, feather: Double = 0.0): Mat2D {
      val diameter = c.diameter
      return Mat2D(
        Matrix.create(
          Array(diameter.ceilInt()) { xIndex ->
            DoubleArray(diameter.ceilInt()) { yIndex ->
              val p = Point(xIndex + c.origin.x, yIndex + c.origin.y)
              if (c.contains(p)) intensity else 0.0
            }
          },
        ),
      )
    }
  }
}
