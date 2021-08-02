@file:Suppress("unused")

package util.print

import interfaces.NamedObject

sealed class StrokeWeight(override val name: String, val value: Number) : NamedObject {
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

sealed class StrokeWeightMM(name: String, val mm: MM) : StrokeWeight(name, mm) {
  override fun toPx(dpi: DPI) = dpi.toPixelsFromMm(mm)
}

sealed class StrokeWeightPx(name: String, val px: Px) : StrokeWeight(name, px) {
  override fun toPx(dpi: DPI) = px.toDouble()
}

class VeryThin : StrokeWeightMM("VeryThin", 0.1)
class Thin : StrokeWeightMM("Thin", 0.5)
class Thick : StrokeWeightMM("Thick", 1.0)
class VeryThick : StrokeWeightMM("VeryThick", 2.0)
class Brush : StrokeWeightMM("Brush", 3.0)
class CustomMM(mm: MM) : StrokeWeightPx("CustomMM", mm)
class CustomPx(px: Px) : StrokeWeightPx("CustomPx", px)

