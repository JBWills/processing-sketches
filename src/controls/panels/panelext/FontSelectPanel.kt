package controls.panels.panelext

import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.props.GenericProp
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
) = addNewPanel(style) {
  val fonts: Map<String, FontData> = getFonts().associateBy { it.displayString() }

  GenericProp(ref) {
    dropdown("Font Family", fonts.keys.toList(), ref.get().family) { fontName: String ->
      fonts[fontName]?.let { fontData ->
        ref.set(fontData)
        markDirty()
      }
    }
  }
}