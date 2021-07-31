package controls.props.types

import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.slider
import controls.panels.panelext.slider2D
import controls.props.PropData
import coordinate.Point
import kotlinx.serialization.Serializable

@Serializable
data class VectorProp(
  var direction: Point,
  var magnitude: Double
) : PropData<VectorProp> {

  val vector: Point get() = direction.normalized * magnitude
  fun scaledVector(amount: Number): Point = vector * amount
  override fun toSerializer() = serializer()

  override fun clone() = VectorProp(direction, magnitude)

  override fun bind(): List<ControlTab> = singleTab(this::class.simpleName!!) {
    row {
      slider2D(::direction, -1..1)
      slider(::magnitude, 0..1)
    }
  }
}
