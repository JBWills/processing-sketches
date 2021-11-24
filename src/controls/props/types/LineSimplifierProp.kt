package controls.props.types

import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.panels.TabsBuilder.Companion.singleTab
import controls.controlsealedclasses.Slider.Companion.slider
import controls.props.PropData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.locationtech.jts.geom.LineString
import util.io.geoJson.SimplifyType
import util.io.geoJson.SimplifyType.DouglasPeucker
import util.io.geoJson.asLineString
import util.io.geoJson.simplify
import util.io.geoJson.smooth

@Serializable
data class LineSimplifierProp(
  var smoothAmount: Double,
  var simplifyAmount: Double,
  var simplifyType: SimplifyType,
  var lengthMin: Double,
) : PropData<LineSimplifierProp> {
  constructor(
    smoothAmount: Number,
    simplifyAmount: Number,
    simplifyType: SimplifyType,
    lengthMin: Number,
  ) : this(
    smoothAmount.toDouble(),
    simplifyAmount.toDouble(),
    simplifyType,
    lengthMin.toDouble(),
  )

  constructor() : this(0, 0, DouglasPeucker, 0)

  override fun toSerializer(): KSerializer<LineSimplifierProp> = serializer()

  override fun clone(): LineSimplifierProp =
    LineSimplifierProp(smoothAmount, simplifyAmount, simplifyType, lengthMin)

  override fun bind() = singleTab(this::class.simpleName!!) {
    row {
      dropdown(::simplifyType)
      slider(::simplifyAmount, 0..3)
    }

    row {
      slider(::smoothAmount, 0..1)
      slider(::lengthMin, 0..200)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LineSimplifierProp

    if (smoothAmount != other.smoothAmount) return false
    if (simplifyAmount != other.simplifyAmount) return false
    if (simplifyType != other.simplifyType) return false
    if (lengthMin != other.lengthMin) return false

    return true
  }

  override fun hashCode(): Int {
    var result = smoothAmount.hashCode()
    result = 31 * result + simplifyAmount.hashCode()
    result = 31 * result + simplifyType.hashCode()
    result = 31 * result + lengthMin.hashCode()
    return result
  }

  companion object {
    fun Collection<LineString>.simplifyContours(simplifier: LineSimplifierProp): List<LineString> {
      return filter { line -> line.length > simplifier.lengthMin }
        .map { line ->
          line.simplify(simplifier.simplifyAmount, simplifier.simplifyType)
            .smooth(1 - simplifier.smoothAmount)
            .asLineString()
        }
    }
  }
}
