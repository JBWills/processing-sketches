package controls

import BaseSketch
import coordinate.Point
import fastnoise.Noise
import util.DoubleRange
import util.PointRange
import util.RangeWithCurrent
import util.RangeWithCurrent.Companion.at
import util.and
import util.property2DSlider
import util.propertyEnumDropdown
import util.propertySlider
import util.propertySliderPair
import util.propertyToggle
import util.toDoubleRange
import util.toIntRange
import kotlin.reflect.KProperty0

sealed class ControlField<T>(
  val sketch: BaseSketch,
  val name: String,
  startVal: T,
) : ControlSectionable {

  var backingField = startVal

  fun get() = backingField
  fun set(newVal: T) {
    backingField = newVal
  }

  companion object {
    fun <T> BaseSketch.fieldFrom(
      prop: KProperty0<T>,
      range: IntRange = 0..10,
    ) = when (prop.get()) {
      is Int -> IntField(this, prop.name, prop.get() as Int, range)
      is Double -> DoubleField(this, prop.name, prop.get() as Double, range.toDoubleRange())
      else -> throw Exception(
        "Tried to call fieldFrom with IntRange and type ${prop.get()!!::class}")
    }

    fun BaseSketch.booleanField(
      prop: KProperty0<Boolean>,
    ) = booleanField(prop.name, prop.get())

    fun BaseSketch.intField(
      prop: KProperty0<Int>,
      range: IntRange = 0..10,
    ) = intField(prop.name, prop.get(), range)

    fun BaseSketch.doubleField(
      prop: KProperty0<Double>,
      range: DoubleRange = 0.0..1.0,
    ) = doubleField(prop.name, prop.get(), range)

    fun BaseSketch.noiseField(
      prop: KProperty0<Noise>,
    ) = NoiseField(this, prop.name, prop.get())

    fun BaseSketch.intField(
      name: String,
      startVal: Int = 0,
      range: IntRange = 0..10,
    ) = IntField(this, name, startVal, range)

    fun BaseSketch.intField(
      name: String,
      range: RangeWithCurrent<Int> = 0..10 at 0,
    ) = IntField(this, name, range.value, range.range.toIntRange())

    fun BaseSketch.doubleField(
      name: String,
      startVal: Number = 0.0,
      range: DoubleRange = 0.0..1.0,
    ) = DoubleField(this, name, startVal.toDouble(), range)

    fun BaseSketch.doubleField(
      name: String,
      range: RangeWithCurrent<Double> = 0.0..1.0 at 0.0,
    ) = DoubleField(this, name, range.value, range.range)

    fun BaseSketch.pointField(
      name: String,
      startVal: Point = (Point.One / 2.0),
      range: PointRange = Point.Zero..Point.One,
    ) = PointField(this, name, startVal, range)

    fun BaseSketch.doubleField(
      name: String,
      range: RangeWithCurrent<Point>,
    ) = PointField(this, name, range.value, range.range)

    fun BaseSketch.doublePairField(
      name: String,
      startVal: Point = Point.Zero,
      ranges: Pair<DoubleRange, DoubleRange> = 0.0..1.0 to 0.0..1.0,
    ) = DoublePair(this, name, startVal, ranges)

    fun BaseSketch.doublePairField(
      prop1: KProperty0<Double>,
      prop2: KProperty0<Double>,
      ranges: Pair<DoubleRange, DoubleRange> = (0.0..1.0) and (0.0..1.0),
    ) = DoublePair(
      this,
      prop1.name + prop2.name,
      Point(prop1.get(), prop2.get()), ranges.first to ranges.second
    )

    fun BaseSketch.doublePairField(
      name: String,
      ranges: Pair<RangeWithCurrent<Double>, RangeWithCurrent<Double>> = (0.0..1.0 at 0) and (0.0..1.0 at 0),
    ) = DoublePair(
      this,
      name,
      Point(ranges.first.value, ranges.second.value),
      ranges.first.range to ranges.second.range
    )

    fun BaseSketch.booleanField(
      name: String,
      startVal: Boolean = false,
    ) = BooleanField(this, name, startVal)

    fun <E : Enum<E>> BaseSketch.enumField(
      name: String,
      startVal: E,
      onChange: () -> Unit = {},
    ) = EnumField(this, name, startVal, onChange)
  }

  class NoiseField(
    sketch: BaseSketch,
    name: String,
    startVal: Noise,
  ) : ControlField<Noise>(sketch, name, startVal) {
    override fun toControlGroups(): Array<ControlGroupable> =
      sketch.noiseControls(::backingField)
  }

  class IntField(
    sketch: BaseSketch,
    name: String,
    startVal: Int = 0,
    val range: IntRange = 0..10,
  ) : ControlField<Int>(sketch, name, startVal) {
    override fun toControlGroups(): Array<ControlGroupable> =
      arrayOf(ControlGroup(sketch.propertySlider(::backingField, range, name = name)))
  }

  class DoubleField(
    sketch: BaseSketch,
    name: String,
    startVal: Double = 0.0,
    val range: DoubleRange = 0.0..1.0,
  ) : ControlField<Double>(sketch, name, startVal) {
    override fun toControlGroups(): Array<ControlGroupable> =
      arrayOf(ControlGroup(sketch.propertySlider(::backingField, range, name = name)))
  }

  class DoublePair(
    sketch: BaseSketch,
    name: String,
    startVal: Point = Point.Zero,
    val ranges: Pair<DoubleRange, DoubleRange> = Pair(0.0..1.0, 0.0..1.0),
  ) : ControlField<Point>(sketch, name, startVal) {
    override fun toControlGroups(): Array<ControlGroupable> =
      arrayOf(
        ControlGroup(*sketch.propertySliderPair(::backingField, ranges.first, ranges.second, name)))
  }

  class PointField(
    sketch: BaseSketch,
    name: String,
    startVal: Point = (Point.One / 2.0),
    val range: PointRange = Point.Zero..Point.One,
  ) : ControlField<Point>(sketch, name, startVal) {
    override fun toControlGroups(): Array<ControlGroupable> =
      arrayOf(ControlGroup(sketch.property2DSlider(::backingField, range, name = name)))
  }

  class BooleanField(
    sketch: BaseSketch,
    name: String,
    startVal: Boolean = false,
  ) : ControlField<Boolean>(sketch, name, startVal) {
    override fun toControlGroups(): Array<ControlGroupable> =
      arrayOf(ControlGroup(sketch.propertyToggle(::backingField, name = name)))
  }

  class EnumField<E : Enum<E>>(
    sketch: BaseSketch,
    name: String,
    startVal: E,
    val onChange: () -> Unit = {},
  ) : ControlField<E>(sketch, name, startVal) {
    override fun toControlGroups(): Array<ControlGroupable> =
      arrayOf(
        ControlGroup(sketch.propertyEnumDropdown(::backingField, name = name, onChange = onChange)))
  }
}
