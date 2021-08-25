package coordinate.transforms

import coordinate.Deg
import coordinate.Point
import java.awt.geom.AffineTransform

class TransformBuilder {
  val transforms: MutableList<(AffineTransform.() -> Unit)> = mutableListOf()

  private fun withAnchor(anchor: Point, block: () -> Unit) {
    val hasTranslation = anchor != Point.Zero
    if (hasTranslation) translate(-anchor)
    block()
    if (hasTranslation) translate(anchor)
  }

  fun translate(translate: Point) {
    if (translate != Point.Zero) transforms.add { translate(translate.x, translate.y) }
  }

  fun rotate(amount: Deg, anchor: Point = Point.Zero) = withAnchor(anchor) {
    transforms.add { rotate(amount.rad) }
  }


  fun scale(amount: Point, anchor: Point = Point.Zero) = withAnchor(anchor) {
    transforms.add { scale(amount.x, amount.y) }
  }

  fun scale(amount: Double, anchor: Point = Point.Zero) = scale(Point(amount), anchor)

  fun shear(amount: Point, anchor: Point = Point.Zero) = withAnchor(anchor) {
    transforms.add { shear(amount.x, amount.y) }
  }

  fun build() = AffineTransform().apply {
    transforms.reversed().forEach { it.invoke(this) }
  }

  companion object {
    fun buildTransform(buildFunc: TransformBuilder.() -> Unit): AffineTransform =
      TransformBuilder().apply { buildFunc() }.build()
  }
}
