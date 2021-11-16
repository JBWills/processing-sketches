@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package controls.panels

import controls.panels.LabelAlignHorizontal.Left
import controls.panels.LabelAlignVertical.Bottom
import controls.panels.LabelAlignVertical.Top
import coordinate.PaddingRect
import kotlinx.serialization.Serializable
import util.constants.Black0
import util.constants.Black1
import util.constants.Black2
import util.constants.Black3
import util.constants.Blue0
import util.constants.Blue1
import util.constants.Blue2
import util.constants.Blue3
import util.constants.Gray0
import util.constants.Gray1
import util.constants.Gray2
import util.constants.Gray3
import util.constants.Green0
import util.constants.Green1
import util.constants.Green2
import util.constants.Green3
import util.constants.Orange0
import util.constants.Orange1
import util.constants.Orange2
import util.constants.Orange3
import util.constants.Purple0
import util.constants.Purple1
import util.constants.Purple2
import util.constants.Purple3
import util.constants.Red0
import util.constants.Red1
import util.constants.Red2
import util.constants.Red3
import util.constants.Yellow0
import util.constants.Yellow1
import util.constants.Yellow2
import util.constants.Yellow3
import util.fonts.FontData
import util.io.serialization.ColorSerializer
import java.awt.Color

@Serializable
data class ControlStyle(
  @Serializable(with = ColorSerializer::class)
  val backgroundColorOverrides: Color? = null,
  @Serializable(with = ColorSerializer::class)
  val colorOverrides: Color? = null,
  @Serializable(with = ColorSerializer::class)
  val onHoverColorOverrides: Color? = null,
  @Serializable(with = ColorSerializer::class)
  val frameBackgroundColorOverrides: Color? = null,
  @Serializable(with = ColorSerializer::class)
  val textColorOverrides: Color? = null,
  val paddingOverrides: PaddingRect? = null,
  val childPaddingOverrides: PaddingRect? = null,
  val fontOverrides: FontData? = null,
  val labelAlignOverrides: LabelAlign? = null,
  val captionAlignOverrides: LabelAlign? = null,
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
    font: FontData? = null,
    labelAlign: LabelAlign? = null,
    captionAlign: LabelAlign? = null,
  ) : this(
    backgroundColorOverrides = backgroundColor ?: base.backgroundColorOverrides,
    colorOverrides = color ?: base.colorOverrides,
    onHoverColorOverrides = onHoverColor ?: base.onHoverColorOverrides,
    frameBackgroundColorOverrides = frameBackgroundColor ?: base.frameBackgroundColorOverrides,
    textColorOverrides = textColor ?: base.textColorOverrides,
    paddingOverrides = padding ?: base.paddingOverrides,
    childPaddingOverrides = childPadding ?: base.childPaddingOverrides,
    fontOverrides = font ?: base.fontOverrides,
    labelAlignOverrides = labelAlign ?: base.labelAlignOverrides,
    captionAlignOverrides = captionAlign ?: base.captionAlignOverrides,
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
  val font: FontData by lazy { fontOverrides ?: Base.fontOverrides!! }
  val labelAlign: LabelAlign by lazy { labelAlignOverrides ?: Base.labelAlignOverrides!! }
  val captionAlign: LabelAlign by lazy { captionAlignOverrides ?: Base.captionAlignOverrides!! }

  fun withOverrides(overrides: ControlStyle) = ControlStyle(
    this,
    backgroundColor = overrides.backgroundColorOverrides,
    color = overrides.colorOverrides,
    onHoverColor = overrides.onHoverColorOverrides,
    frameBackgroundColor = overrides.frameBackgroundColorOverrides,
    textColor = overrides.textColorOverrides,
    padding = overrides.paddingOverrides,
    childPadding = overrides.childPaddingOverrides,
    font = overrides.fontOverrides,
    labelAlign = overrides.labelAlignOverrides,
    captionAlign = overrides.captionAlignOverrides,
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

  fun withoutFrameBackground() = ControlStyle(
    backgroundColorOverrides = backgroundColorOverrides,
    colorOverrides = colorOverrides,
    onHoverColorOverrides = onHoverColorOverrides,
    frameBackgroundColorOverrides = null,
    textColorOverrides = textColorOverrides,
    paddingOverrides = paddingOverrides,
    childPaddingOverrides = childPaddingOverrides,
    fontOverrides = fontOverrides,
    labelAlignOverrides = labelAlignOverrides,
    captionAlignOverrides = captionAlignOverrides,
  )

  override fun toString(): String {
    return "ControlStyle(backgroundColor=$backgroundColorOverrides, color=$colorOverrides, onHoverColor=$onHoverColorOverrides, frameBackgroundColor=${frameBackgroundColorOverrides} textColor=$textColorOverrides, padding=$paddingOverrides, childPadding=$childPaddingOverrides, padding=$padding, childPadding=$childPadding, backgroundColor=$backgroundColor, selectedColor=$onHoverColor, textColor=$textColor, deselectedColor=$color)"
  }

  companion object {
    private fun s(background: Color, frame: Color, hover: Color, color: Color) = ControlStyle(
      backgroundColorOverrides = background,
      frameBackgroundColorOverrides = frame,
      onHoverColorOverrides = hover,
      colorOverrides = color,
    )

    val Purple = s(background = Purple1, frame = Purple0, hover = Purple2, color = Purple3)
    val Red = s(background = Red1, frame = Red0, hover = Red2, color = Red3)
    val Green = s(background = Green1, frame = Green0, hover = Green2, color = Green3)
    val Blue = s(background = Blue1, frame = Blue0, hover = Blue2, color = Blue3)
    val Orange = s(background = Orange1, frame = Orange0, hover = Orange2, color = Orange3)
    val Yellow = s(background = Yellow1, frame = Yellow0, hover = Yellow2, color = Yellow3)
    val Gray = s(background = Gray1, frame = Gray0, hover = Gray2, color = Gray3)
    val Black = s(background = Black1, frame = Black0, hover = Black2, color = Black3)

    val EmptyStyle = ControlStyle()

    val Base = Purple.withOverrides(
      ControlStyle(
        textColorOverrides = Color.WHITE,
        paddingOverrides = PaddingRect(vertical = 0, horizontal = 0),
        childPaddingOverrides = PaddingRect(vertical = 2.5, horizontal = 2.5),
        fontOverrides = FontData("Roboto", "Medium", baseSize = 15.0),
        labelAlignOverrides = LabelAlign(Left, Top),
        captionAlignOverrides = LabelAlign(Left, Bottom),
      ),
    )

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
