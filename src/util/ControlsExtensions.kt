package util

import controlP5.Controller
import controlP5.Slider
import controlP5.Slider2D
import controls.panels.ControlStyle
import coordinate.BoundRect

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
  return this
}
