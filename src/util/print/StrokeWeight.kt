@file:Suppress("unused")

package util.print

import interfaces.NamedObject
import kotlinx.serialization.Serializable

@Serializable
sealed class StrokeWeight(override val name: String, val value: Double) : NamedObject {
  abstract fun toPx(dpi: DPI): Double

  companion object {
    fun values() = listOf(
      VeryThin(),
      Thin(),
      Thick(),
      VeryThick(),
      Brush(),
    )
  }
}

@Serializable
sealed class StrokeWeightMM(private val serialName: String, private val mm: MM) :
  StrokeWeight(serialName, mm) {
  override fun toPx(dpi: DPI) = dpi.toPixelsFromMm(mm)
}

@Serializable
sealed class StrokeWeightPx(private val serialName: String, private val px: Px) : StrokeWeight(
  serialName,
  px,
) {
  override fun toPx(dpi: DPI) = px
}

@Serializable
class VeryThin : StrokeWeightMM("VeryThin", 0.1)

@Serializable
class Thin : StrokeWeightMM("Thin", 0.5)

@Serializable
class Thick : StrokeWeightMM("Thick", 1.0)

@Serializable
class VeryThick : StrokeWeightMM("VeryThick", 2.0)

@Serializable
class Brush : StrokeWeightMM("Brush", 3.0)

@Serializable
class CustomMM(private val customMM: MM) : StrokeWeightPx("CustomMM", customMM)

@Serializable
class CustomPx(private val customPx: Px) : StrokeWeightPx("CustomPx", customPx) {
  constructor(px: Number) : this(px.toDouble())
}

