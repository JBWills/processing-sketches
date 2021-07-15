package coordinate

import interfaces.shape.Transformable
import util.iterators.copy

sealed class ShapeTransform {
  abstract fun <T : Transformable<T>> transform(t: T): T
  abstract fun inverted(): ShapeTransform
}

class ShapeTransformGroup() : ShapeTransform() {
  constructor(vararg transforms: ShapeTransform) : this() {
    add(*transforms)
  }

  constructor(transforms: List<ShapeTransform>) : this() {
    addAll(transforms)
  }

  private val transforms: MutableList<ShapeTransform> = mutableListOf()

  override fun <T : Transformable<T>> transform(t: T): T =
    transforms.fold(t) { transformable, transform -> transform.transform(transformable) }

  fun add(vararg transform: ShapeTransform): ShapeTransform =
    apply { transforms.addAll(transform) }

  fun addAll(transforms: List<ShapeTransform>) = add(*transforms.toTypedArray())

  fun copyAndAdd(vararg transform: ShapeTransform): ShapeTransform = ShapeTransformGroup(
    transforms.copy(),
  ).add(*transform)

  fun clearTransforms(): ShapeTransform = apply { transforms.clear() }

  override fun inverted(): ShapeTransformGroup =
    ShapeTransformGroup(transforms.reversed().map { it.inverted() })
}

class TranslateTransform(val translate: Point) : ShapeTransform() {
  override fun <T : Transformable<T>> transform(t: T): T = t.translated(translate)

  override fun inverted(): ShapeTransform = TranslateTransform(-translate)
}

class ScaleTransform(val scale: Point, val anchor: Point) : ShapeTransform() {
  override fun <T : Transformable<T>> transform(t: T): T = t.scaled(scale, anchor)

  override fun inverted(): ShapeTransform = ScaleTransform(1 / scale, anchor)
}
