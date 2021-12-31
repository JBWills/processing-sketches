@file:Suppress("unused")

package util.print

import interfaces.NamedObject
import kotlinx.serialization.Serializable

@Serializable
sealed class StrokeWeight(override val name: String, val value: Double) : NamedObject {
  abstract fun toPx(dpi: DPI): Double

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as StrokeWeight

    if (name != other.name) return false
    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + value.hashCode()
    return result
  }

  companion object {
    fun values() = listOf(
      VeryThin(),
      Thin(),
      Thick(),
      VeryThick(),
      Micron003(),
      Micron005(),
      Micron01(),
      Micron02(),
      Micron03(),
      Micron04(),
      Micron05(),
      Micron08(),
      Micron1(),
      Micron2(),
      Micron3(),
      MicronBR(),
      Gelly08(),
      Gelly06(),
      Gelly1(),
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
class Micron003 : StrokeWeightMM("Micron003", 0.15)

@Serializable
class Micron005 : StrokeWeightMM("Micron005", 0.2)

@Serializable
class Micron01 : StrokeWeightMM("Micron01", 0.25)

@Serializable
class Micron02 : StrokeWeightMM("Micron02", 0.3)

@Serializable
class Micron03 : StrokeWeightMM("Micron03", 0.35)

@Serializable
class Micron04 : StrokeWeightMM("Micron04", 0.4)

@Serializable
class Micron05 : StrokeWeightMM("Micron05", 0.45)

@Serializable
class Micron08 : StrokeWeightMM("Micron08", 0.5)

@Serializable
class Micron1 : StrokeWeightMM("Micron1", 1.0)

@Serializable
class Micron2 : StrokeWeightMM("Micron2", 2.0)

@Serializable
class Micron3 : StrokeWeightMM("Micron3", 3.0)

@Serializable
class MicronBR : StrokeWeightMM("MicronBR", 4.0)

@Serializable
class Gelly08 : StrokeWeightMM("Gelly08", 0.8)

@Serializable
class Gelly06 : StrokeWeightMM("Gelly06", 0.6)

@Serializable
class Gelly1 : StrokeWeightMM("Gelly1", 1.0)

@Serializable
class Brush : StrokeWeightMM("Brush", 3.0)

@Serializable
class CustomMM(private val customMM: MM) : StrokeWeightPx("CustomMM", customMM)

@Serializable
class CustomPx(private val customPx: Px) : StrokeWeightPx("CustomPx", customPx) {
  constructor(px: Number) : this(px.toDouble())
}

