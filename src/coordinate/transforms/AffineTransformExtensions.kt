package coordinate.transforms

import coordinate.Deg
import coordinate.Point
import coordinate.transforms.TransformBuilder.Companion.buildTransform
import util.polylines.PolyLine
import util.polylines.toDoubleArray
import util.polylines.toEmptyDoubleArray
import util.polylines.toPolyLine
import java.awt.geom.AffineTransform

fun AffineTransform.translate(translate: Point): AffineTransform = apply {
  if (translate != Point.Zero) translate(translate.x, translate.y)
}

fun AffineTransform.rotate(amount: Deg, anchor: Point = Point.Zero): AffineTransform = apply {
  val hasTranslation = anchor != Point.Zero
  if (hasTranslation) translate(-anchor)
  rotate(amount.rad)
  if (hasTranslation) translate(anchor)
}


fun AffineTransform.scale(amount: Point, anchor: Point = Point.Zero): AffineTransform = apply {
  val hasTranslation = anchor != Point.Zero
  if (hasTranslation) translate(-anchor)
  scale(amount.x, amount.y)
  if (hasTranslation) translate(anchor)
}


fun rotationTransform(amount: Deg, anchor: Point = Point.Zero) =
  buildTransform { rotate(amount, anchor) }

fun translationTransform(translate: Point) =
  buildTransform { translate(translate) }

fun scaleTransform(scale: Point, anchor: Point = Point.Zero) =
  buildTransform { scale(scale, anchor) }

fun shearTransform(shear: Point) =
  AffineTransform.getShearInstance(shear.x, shear.y)

fun AffineTransform.then(vararg transforms: AffineTransform): AffineTransform {
  transforms.reversed().forEach(this::concatenate)

  return this
}

fun AffineTransform.applyTo(line: PolyLine): PolyLine =
  line.toEmptyDoubleArray()
    .also { resultArr ->
      transform(line.toDoubleArray(), 0, resultArr, 0, 0)
    }.toPolyLine()
