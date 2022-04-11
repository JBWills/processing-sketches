package util

import controlP5.ControlGroup
import controlP5.Controller
import controlP5.ControllerInterface
import controlP5.Slider
import controlP5.Slider2D
import controlP5.Tab
import controls.panels.ControlStyle
import controls.panels.LabelAlign.Companion.alignCaptionAndLabel
import controls.panels.TabStyle
import coordinate.BoundRect
import coordinate.Point
import processing.core.PFont
import util.base.DoubleRange
import java.awt.Color

val ControllerInterface<*>.topLeft get(): Point = Point(position[0], position[1])

val ControllerInterface<*>.bounds get(): BoundRect = BoundRect(topLeft, width, height)

fun Slider.range(r: DoubleRange): Slider = setRange(r.start.toFloat(), r.endInclusive.toFloat())
fun Slider2D.range(x: DoubleRange, y: DoubleRange): Slider2D =
  setMinMax(
    x.start.toFloat(),
    y.start.toFloat(),
    x.endInclusive.toFloat(),
    y.endInclusive.toFloat(),
  )

fun <T> ControllerInterface<T>.positionAndSize(bound: BoundRect): ControllerInterface<T> {
  bound.topLeft.let { setPosition(it.xf, it.yf) }
  bound.size.toPixelPoint().also {
    if (this is ControlGroup) {
      setSize(it.x, it.y)
    } else if (this is Controller) {
      setSize(it.x, it.y)
    }
  }
  return this
}

fun <T> ControllerInterface<T>.style(controlStyle: ControlStyle): ControllerInterface<T> {
  setColorBackground(controlStyle.backgroundColor.rgb)
  setColorActive(controlStyle.onHoverColor.rgb)
  setColorForeground(controlStyle.color.rgb)
  setColorLabel(controlStyle.textColor.rgb)
  setFont(PFont(controlStyle.font.toFont(), true))

  if (this is ControlGroup) {
    alignCaptionAndLabel(
      valueAlign = controlStyle.labelAlign,
      captionAlign = controlStyle.captionAlign,
    )
  } else if (this is Controller) {
    alignCaptionAndLabel(
      valueAlign = controlStyle.labelAlign,
      captionAlign = controlStyle.captionAlign,
    )
  }

  return this
}

fun Tab.style(tabStyle: TabStyle): Tab {
  fun Color.applyStyle(applyFunction: (Int) -> Unit) = applyFunction(this.rgb)

  tabStyle.tabBackgroundColor?.applyStyle(this::setColorBackground)
  tabStyle.tabOnHoverColor?.applyStyle(this::setColorActive)
  tabStyle.tabColor?.applyStyle(this::setColorForeground)
  tabStyle.tabTextColor?.applyStyle(this::setColorLabel)
  return this
}
