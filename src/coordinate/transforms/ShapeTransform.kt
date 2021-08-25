package coordinate.transforms

import coordinate.Point
import coordinate.div
import interfaces.shape.Transformable
import util.iterators.copy

sealed class ShapeTransform {
  abstract fun <T : Transformable<T>> transform(t: T): T
  abstract fun inverted(): ShapeTransform

  fun addTransform(t: ShapeTransform) = ShapeTransformGroup(this, t)
  fun addTransforms(vararg t: ShapeTransform) = ShapeTransformGroup(this, *t)
  fun addTransforms(t: List<ShapeTransform>) = ShapeTransformGroup(this, *t.toTypedArray())
}

class ShapeTransformGroup() : ShapeTransform() {
  constructor(vararg transforms: ShapeTransform) : this(transforms.toList())

  constructor(transforms: List<ShapeTransform>) : this() {
    val flattenedTransforms = transforms.flatMap {
      if (it is ShapeTransformGroup) it.transforms else listOf(it)
    }
    addAll(flattenedTransforms)
  }

  private val transforms: MutableList<ShapeTransform> = mutableListOf()

  fun getAsArray() = transforms.toTypedArray()

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

