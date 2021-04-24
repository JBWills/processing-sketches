package controls.panels

import coordinate.PaddingRect
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
import java.awt.Color

data class TabStyle(
  val controlStyle: ControlStyle? = null,
  val frameBackgroundColor: Color? = null,
  val tabBackgroundColor: Color? = null,
  val tabColor: Color? = null,
  val tabOnHoverColor: Color? = null,
  val padding: PaddingRect? = null,
) {

  val nonNullControlStyle = controlStyle ?: ControlStyle.EmptyStyle

  val nonNullPadding by lazy { padding ?: BASE_TAB_PADDING }

  constructor(
    tabStyle: TabStyle,
    controlStyle: ControlStyle? = null,
    frameBackgroundColor: Color? = null,
    tabBackgroundColor: Color? = null,
    tabColor: Color? = null,
    tabOnHoverColor: Color? = null,
    padding: PaddingRect? = null,
  ) : this(
    controlStyle ?: tabStyle.controlStyle,
    frameBackgroundColor ?: tabStyle.frameBackgroundColor,
    tabBackgroundColor ?: tabStyle.tabBackgroundColor,
    tabColor ?: tabStyle.tabColor,
    tabOnHoverColor ?: tabStyle.tabOnHoverColor,
    padding ?: tabStyle.padding,
  )

  override fun toString(): String {
    return "TabStyle(controlStyle=$controlStyle, frameBackgroundColor=$frameBackgroundColor, tabBackgroundColor=$tabBackgroundColor, tabColor=$tabColor, tabOnHoverColor=$tabOnHoverColor, padding=$padding)"
  }

  companion object {
    private fun s(background: Color, frame: Color, hover: Color, color: Color) = TabStyle(
      tabBackgroundColor = background,
      frameBackgroundColor = frame,
      tabOnHoverColor = hover,
      tabColor = color,
    )

    val Purple = s(background = Purple1, frame = Purple0, hover = Purple2, color = Purple3)
    val Red = s(background = Red1, frame = Red0, hover = Red2, color = Red3)
    val Green = s(background = Green1, frame = Green0, hover = Green2, color = Green3)
    val Blue = s(background = Blue1, frame = Blue0, hover = Blue2, color = Blue3)
    val Orange = s(background = Orange1, frame = Orange0, hover = Orange2, color = Orange3)
    val Yellow = s(background = Yellow1, frame = Yellow0, hover = Yellow2, color = Yellow3)
    val Gray = s(background = Gray1, frame = Gray0, hover = Gray2, color = Gray3)
    val Black = s(background = Black1, frame = Black0, hover = Black2, color = Black3)

    val BASE_TAB_PADDING = PaddingRect(base = 20)
    val Base = TabStyle(
      Purple,
      controlStyle = ControlStyle.EmptyStyle,
      padding = BASE_TAB_PADDING,
    )
  }
}
