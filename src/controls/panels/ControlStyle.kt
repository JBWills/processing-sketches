package controls.panels

import coordinate.PaddingRect
import java.awt.Color

data class ControlStyle(
  val backgroundColorOverrides: Color? = null,
  val colorOverrides: Color? = null,
  val onHoverColorOverrides: Color? = null,
  val frameBackgroundColorOverrides: Color? = null,
  val textColorOverrides: Color? = null,
  val paddingOverrides: PaddingRect? = null,
  val childPaddingOverrides: PaddingRect? = null,
) {
  constructor(
    base: ControlStyle,
    backgroundColor: Color? = null,
    color: Color? = null,
    onHoverColor: Color? = null,
    frameBackgroundColor: Color? = null,
    textColor: Color? = null,
    padding: PaddingRect? = null,
    childPadding: PaddingRect? = null,
  ) : this(
    backgroundColorOverrides = backgroundColor ?: base.backgroundColorOverrides,
    colorOverrides = color ?: base.colorOverrides,
    onHoverColorOverrides = onHoverColor ?: base.onHoverColorOverrides,
    frameBackgroundColorOverrides = frameBackgroundColor ?: base.frameBackgroundColorOverrides,
    textColorOverrides = textColor ?: base.textColorOverrides,
    paddingOverrides = padding ?: base.paddingOverrides,
    childPaddingOverrides = childPadding ?: base.childPaddingOverrides,
  )

  val padding: PaddingRect by lazy { paddingOverrides ?: Base.paddingOverrides!! }
  val childPadding: PaddingRect by lazy { childPaddingOverrides ?: Base.childPaddingOverrides!! }
  val backgroundColor: Color by lazy { backgroundColorOverrides ?: Base.backgroundColorOverrides!! }
  val frameBackgroundColor: Color by lazy {
    frameBackgroundColorOverrides ?: Base.frameBackgroundColorOverrides!!
  }
  val onHoverColor: Color by lazy { onHoverColorOverrides ?: Base.onHoverColorOverrides!! }
  val textColor: Color by lazy { textColorOverrides ?: Base.textColorOverrides!! }
  val color: Color by lazy { colorOverrides ?: Base.colorOverrides!! }

  fun withOverrides(overrides: ControlStyle) = ControlStyle(
    this,
    backgroundColor = overrides.backgroundColorOverrides,
    color = overrides.colorOverrides,
    onHoverColor = overrides.onHoverColorOverrides,
    frameBackgroundColor = overrides.frameBackgroundColorOverrides,
    textColor = overrides.textColorOverrides,
    padding = overrides.paddingOverrides,
    childPadding = overrides.childPaddingOverrides,
  )

  fun withBackgroundColor(c: Color?) = withOverrides(ControlStyle(backgroundColorOverrides = c))
  fun withColor(
    base: Color? = null,
    background: Color? = null,
    frameBackground: Color? = null,
    onHover: Color? = null,
    text: Color? = null
  ) = withOverrides(
    ControlStyle(
      this,
      backgroundColor = background,
      color = base,
      onHoverColor = onHover,
      frameBackgroundColor = frameBackground,
      textColor = text,
    ),
  )

  fun withPadding(p: PaddingRect?) = withOverrides(ControlStyle(paddingOverrides = p))
  fun withChildPadding(p: PaddingRect?) = withOverrides(ControlStyle(childPaddingOverrides = p))

  override fun toString(): String {
    return "ControlStyle(backgroundColor=$backgroundColorOverrides, color=$colorOverrides, onHoverColor=$onHoverColorOverrides, frameBackgroundColor=${frameBackgroundColorOverrides} textColor=$textColorOverrides, padding=$paddingOverrides, childPadding=$childPaddingOverrides, padding=$padding, childPadding=$childPadding, backgroundColor=$backgroundColor, selectedColor=$onHoverColor, textColor=$textColor, deselectedColor=$color)"
  }

  companion object {
    val Purple = ControlStyle(
      backgroundColorOverrides = Color(50, 5, 131),
      onHoverColorOverrides = Color(90, 0, 220),
      colorOverrides = Color(108, 0, 238),
    )

    val Red = ControlStyle(
      backgroundColorOverrides = Color(81, 5, 30),
      onHoverColorOverrides = Color(121, 15, 70),
      colorOverrides = Color(141, 5, 80),
    )

    val Green = ControlStyle(
      backgroundColorOverrides = Color(8, 51, 5),
      onHoverColorOverrides = Color(8, 81, 5),
      colorOverrides = Color(8, 101, 5),
    )

    val Blue = ControlStyle(
      backgroundColorOverrides = Color(8, 25, 101),
      onHoverColorOverrides = Color(8, 45, 131),
      colorOverrides = Color(8, 45, 161),
    )

    val Orange = ControlStyle(
      backgroundColorOverrides = Color(71, 37, 8),
      onHoverColorOverrides = Color(121, 47, 8),
      colorOverrides = Color(111, 42, 8),
    )

    val Yellow = ControlStyle(
      backgroundColorOverrides = Color(71, 65, 8),
      onHoverColorOverrides = Color(121, 100, 8),
      colorOverrides = Color(111, 90, 8),
    )

    val Gray = ControlStyle(
      backgroundColorOverrides = Color(67, 67, 87),
      onHoverColorOverrides = Color(97, 97, 107),
      colorOverrides = Color(87, 87, 97),
    )

    val EmptyStyle = ControlStyle()

    val Base = Purple.withOverrides(
      ControlStyle(
        textColorOverrides = Color.WHITE,
        paddingOverrides = PaddingRect(vertical = 0, horizontal = 0),
        childPaddingOverrides = PaddingRect(vertical = 2.5, horizontal = 2.5),
      ),
    )

    val x = run { println(Base) }

    fun styleWithPadding(p: PaddingRect) = ControlStyle(paddingOverrides = p)

    fun Panelable.withRedStyle() = withStyle(Red)
    fun Panelable.withBlueStyle() = withStyle(Blue)
    fun Panelable.withGreenStyle() = withStyle(Green)
    fun Panelable.withPurpleStyle() = withStyle(Purple)
    fun Panelable.withOrangeStyle() = withStyle(Orange)
    fun Panelable.withGrayStyle() = withStyle(Gray)
    fun Panelable.withYellowStyle() = withStyle(Yellow)
    fun Panelable.withEmptyStyle() = withStyle(EmptyStyle)
  }
}
