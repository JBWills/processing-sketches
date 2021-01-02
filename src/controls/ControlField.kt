package controls

import BaseSketch
import coordinate.Point
import util.DoubleRange
import util.PointRange
import util.property2DSlider
import util.propertyEnumDropdown
import util.propertySlider
import util.propertySliderPair
import util.propertyToggle

sealed class ControlField<T>(val sketch: BaseSketch<*>, val name: String, startVal: T) : ControlGroupable {
  abstract fun getControls(): Array<Control>

  override fun toControlGroup() = ControlGroup(*getControls())

  fun toControlGroup(vararg otherControls: Control = arrayOf()) = ControlGroup(*getControls(), *otherControls)

  fun toControlGroups(vararg otherControls: Array<Control> = arrayOf()) = ControlGroup(*getControls())
  fun getControl(index: Int = 0) = getControls()[index]

  var backingField = startVal

  fun get() = backingField
  fun set(newVal: T) {
    backingField = newVal
  }

  companion object {
    fun toControlGroups(vararg controlFields: ControlField<*>) =
      controlFields.map { it.toControlGroup() }.toTypedArray()

    fun BaseSketch<*>.intField(
      name: String,
      startVal: Int = 0,
      range: IntRange = 0..10,
    ) = IntField(this, name, startVal, range)

    fun BaseSketch<*>.doubleField(
      name: String,
      startVal: Number = 0.0,
      range: DoubleRange = 0.0..1.0,
    ) = DoubleField(this, name, startVal.toDouble(), range)

    fun BaseSketch<*>.pointField(
      name: String,
      startVal: Point = (Point.One / 2.0),
      range: PointRange = Point.Zero..Point.One,
    ) = PointField(this, name, startVal, range)

    fun BaseSketch<*>.doublePairField(
      name: String,
      startVal: Point = Point.Zero,
      ranges: Pair<DoubleRange, DoubleRange> = Pair(0.0..1.0, 0.0..1.0),
    ) = DoublePair(this, name, startVal, ranges)

    fun BaseSketch<*>.booleanField(
      name: String,
      startVal: Boolean = false,
    ) = BooleanField(this, name, startVal)

    fun <E : Enum<E>> BaseSketch<*>.enumField(
      name: String,
      startVal: E,
      onChange: () -> Unit = {},
    ) = EnumField(this, name, startVal, onChange)
  }

  class IntField(
    sketch: BaseSketch<*>,
    name: String,
    startVal: Int = 0,
    val range: IntRange = 0..10,
  ) : ControlField<Int>(sketch, name, startVal) {
    override fun getControls(): Array<Control> = arrayOf(sketch.propertySlider(::backingField, range, name = name))
  }

  class DoubleField(
    sketch: BaseSketch<*>,
    name: String,
    startVal: Double = 0.0,
    val range: DoubleRange = 0.0..1.0,
  ) : ControlField<Double>(sketch, name, startVal) {
    override fun getControls(): Array<Control> = arrayOf(sketch.propertySlider(::backingField, range, name = name))
  }

  class DoublePair(
    sketch: BaseSketch<*>,
    name: String,
    startVal: Point = Point.Zero,
    val ranges: Pair<DoubleRange, DoubleRange> = Pair(0.0..1.0, 0.0..1.0),
  ) : ControlField<Point>(sketch, name, startVal) {
    override fun getControls(): Array<Control> = arrayOf(*sketch.propertySliderPair(::backingField, ranges.first, ranges.second, name))
  }

  class PointField(
    sketch: BaseSketch<*>,
    name: String,
    startVal: Point = (Point.One / 2.0),
    val range: PointRange = Point.Zero..Point.One,
  ) : ControlField<Point>(sketch, name, startVal) {
    override fun getControls(): Array<Control> = arrayOf(sketch.property2DSlider(::backingField, range, name = name))
  }

  class BooleanField(
    sketch: BaseSketch<*>,
    name: String,
    startVal: Boolean = false,
  ) : ControlField<Boolean>(sketch, name, startVal) {
    override fun getControls(): Array<Control> = arrayOf(sketch.propertyToggle(::backingField, name = name))
  }

  class EnumField<E : Enum<E>>(
    sketch: BaseSketch<*>,
    name: String,
    startVal: E,
    val onChange: () -> Unit = {},
  ) : ControlField<E>(sketch, name, startVal) {
    override fun getControls(): Array<Control> = arrayOf(sketch.propertyEnumDropdown(::backingField, name = name, onChange = onChange))
  }
}