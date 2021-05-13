package controls.props.types

import controls.panels.ControlStyle
import controls.panels.ControlTab.Companion.tab
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.Point
import kotlinx.serialization.Serializable
import util.tuple.and


@Serializable
data class PhotoFiltersProp(
  var photo: PhotoProp = PhotoProp(),
  var sampleRate: Point = Point(5, 5),
  var circleSizes: Point = Point(0, 30),
) : PropData<PhotoFiltersProp> {
  override fun bind() = listOf(
    tab("Photo") {
      panel(::photo)
    },
    tab("Filters") {
      style = ControlStyle.Blue
      sliderPair(::sampleRate, 2.0..50.0, withLockToggle = true)
      sliderPair(::circleSizes, 0.0..20.0 and 2.0..30.0)
    },
  )

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}
