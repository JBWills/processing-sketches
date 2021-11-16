package util

import controlP5.Controller
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

val Controller<*>.topLeft get(): Point = Point(position[0], position[1])

val Controller<*>.bounds get(): BoundRect = BoundRect(topLeft, width, height)

fun Slider.range(r: DoubleRange): Slider = setRange(r.start.toFloat(), r.endInclusive.toFloat())
fun Slider2D.range(x: DoubleRange, y: DoubleRange): Slider2D =
  setMinMax(
    x.start.toFloat(),
    y.start.toFloat(),
    x.endInclusive.toFloat(),
    y.endInclusive.toFloat(),
  )

fun <T> Controller<T>.positionAndSize(bound: BoundRect): Controller<T> {
  bound.topLeft.let { setPosition(it.xf, it.yf) }
  bound.size.toPixelPoint().let { setSize(it.x, it.y) }
  return this
}

fun <T> Controller<T>.style(controlStyle: ControlStyle): Controller<T> {
  setColorBackground(controlStyle.backgroundColor.rgb)
  setColorActive(controlStyle.onHoverColor.rgb)
  setColorForeground(controlStyle.color.rgb)
  setColorLabel(controlStyle.textColor.rgb)
  setFont(PFont(controlStyle.font.toFont(), true))
  
  alignCaptionAndLabel(
    valueAlign = controlStyle.labelAlign,
    captionAlign = controlStyle.captionAlign,
  )

  return this
}

fun Tab.style(tabStyle: TabStyle): Tab {
  tabStyle.tabBackgroundColor?.let { setColorBackground(it.rgb) }
  tabStyle.tabOnHoverColor?.let { setColorActive(it.rgb) }
  tabStyle.tabColor?.let { setColorForeground(it.rgb) }
  return this
}
