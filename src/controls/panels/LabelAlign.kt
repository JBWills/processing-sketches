package controls.panels

import controlP5.ControlP5
import controlP5.Controller
import controlP5.Label
import controls.panels.LabelAlignHorizontal.Center
import kotlinx.serialization.Serializable

@Serializable
enum class LabelAlignHorizontal(val value: Int) {
  Left(ControlP5.LEFT),
  LeftOutside(ControlP5.LEFT_OUTSIDE),
  Center(ControlP5.CENTER),
  Right(ControlP5.RIGHT),
  RightOutside(ControlP5.RIGHT_OUTSIDE),
}

@Serializable
enum class LabelAlignVertical(val value: Int) {
  Top(ControlP5.TOP),
  TopOutside(ControlP5.TOP_OUTSIDE),
  Center(ControlP5.CENTER),
  Bottom(ControlP5.BOTTOM),
  BottomOutside(ControlP5.BOTTOM_OUTSIDE),
}

@Serializable
data class LabelAlign(val horizontal: LabelAlignHorizontal, val vertical: LabelAlignVertical) {
  fun applyTo(label: Label) {
    label.align(horizontal.value, vertical.value)
  }

  override fun toString(): String {
    return "LabelAlign(horizontal=$horizontal, vertical=$vertical)"
  }

  companion object {
    val Centered: LabelAlign = LabelAlign(Center, LabelAlignVertical.Center)
    fun Label.align(labelAlign: LabelAlign) = labelAlign.applyTo(this)
    fun Controller<*>.alignCaptionAndLabel(valueAlign: LabelAlign, captionAlign: LabelAlign) {
      valueLabel.align(valueAlign)
      captionLabel.align(captionAlign)
    }
  }
}
