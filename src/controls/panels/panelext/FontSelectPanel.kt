package controls.panels.panelext

import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.Panelable
import util.fonts.FontData
import util.fonts.FontData.Companion.toFontData
import util.io.fontDir
import util.io.getAllFilesInPath
import kotlin.reflect.KMutableProperty0

fun getFonts(): List<FontData> =
  getAllFilesInPath(fontDir, true, "ttf")
    .map { it.toFontData() }
    .sortedWith(compareBy({ it.family }, { it.weight }))

fun PanelBuilder.fontSelect(
  ref: KMutableProperty0<FontData>,
  style: ControlStyle? = null,
): Panelable {
  val fonts: Map<String, FontData> = getFonts().associateBy { it.displayString() }

  return dropdown(ref, ref.name, fonts.values.toList(), getName = { it.family }, style)
}
