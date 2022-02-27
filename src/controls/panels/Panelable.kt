package controls.panels

fun interface Panelable {
  fun toControlPanel(): ControlPanel

  fun withHeight(height: Number): ControlPanel =
    toControlPanel().with(heightRatio = height.toDouble())

  fun withWidth(width: Number): ControlPanel =
    toControlPanel().with(widthRatio = width.toDouble())

  fun withStyle(style: ControlStyle?): ControlPanel =
    toControlPanel().with(style = style)

  fun applyStyleOverrides(style: ControlStyle?): Panelable =
    if (style != null) toControlPanel().overrideStyles(style) else this
}
