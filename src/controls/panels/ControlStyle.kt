package controls.panels

import coordinate.PaddingRect
import java.awt.Color

data class ControlStyle(
  val backgroundColorOverrides: Color? = null,
  val deselectedColorOverrides: Color? = null,
  val selectedColorOverrides: Color? = null,
  val textColorOverrides: Color? = null,
  val paddingOverrides: PaddingRect? = null,
  val childPaddingOverrides: PaddingRect? = null,
) {
  constructor(
    base: ControlStyle,
    backgroundColor: Color? = null,
    deselectedColor: Color? = null,
    selectedColor: Color? = null,
    textColor: Color? = null,
    padding: PaddingRect? = null,
    childPadding: PaddingRect? = null,
  ) : this(
    backgroundColorOverrides = backgroundColor ?: base.backgroundColorOverrides,
    deselectedColorOverrides = deselectedColor ?: base.deselectedColorOverrides,
    selectedColorOverrides = selectedColor ?: base.selectedColorOverrides,
    textColorOverrides = textColor ?: base.textColorOverrides,
    paddingOverrides = padding ?: base.paddingOverrides,
    childPaddingOverrides = childPadding ?: base.childPaddingOverrides,
  )

  val padding: PaddingRect = paddingOverrides ?: BASE.paddingOverrides!!
  val childPadding: PaddingRect = childPaddingOverrides ?: BASE.childPaddingOverrides!!
  val backgroundColor: Color = backgroundColorOverrides ?: BASE.backgroundColorOverrides!!
  val selectedColor: Color = selectedColorOverrides ?: BASE.selectedColorOverrides!!
  val textColor: Color = textColorOverrides ?: BASE.textColorOverrides!!
  val deselectedColor: Color = deselectedColorOverrides ?: BASE.deselectedColorOverrides!!

  fun withOverrides(overrides: ControlStyle) = ControlStyle(
    this,
    backgroundColor = overrides.backgroundColorOverrides,
    deselectedColor = overrides.deselectedColorOverrides,
    selectedColor = overrides.selectedColorOverrides,
    textColor = overrides.textColorOverrides,
    padding = overrides.paddingOverrides,
    childPadding = overrides.childPaddingOverrides,
  )

  fun withBackgroundColor(c: Color?) = withOverrides(ControlStyle(backgroundColorOverrides = c))
  fun withPadding(p: PaddingRect?) = withOverrides(ControlStyle(paddingOverrides = p))
  fun withChildPadding(p: PaddingRect?) = withOverrides(ControlStyle(childPaddingOverrides = p))

  companion object {
    val BASE = ControlStyle(
      backgroundColorOverrides = Color(50, 5, 131),
      selectedColorOverrides = Color(90, 0, 220),
      deselectedColorOverrides = Color(108, 0, 238),
      textColorOverrides = Color.WHITE,
      paddingOverrides = PaddingRect(vertical = 0, horizontal = 0),
      childPaddingOverrides = PaddingRect(vertical = 2.5, horizontal = 2.5),
    )
    val RED = ControlStyle(backgroundColorOverrides = Color.RED)
    val GREEN = ControlStyle(backgroundColorOverrides = Color.GREEN)
    val BLUE = ControlStyle(backgroundColorOverrides = Color.BLUE)
    val ORANGE = ControlStyle(backgroundColorOverrides = Color.ORANGE)
    val NO_OVERRIDES = ControlStyle()

    fun styleWithColor(c: Color) = ControlStyle(backgroundColorOverrides = c)
    fun styleWithPadding(p: PaddingRect) = ControlStyle(paddingOverrides = p)
  }
}
