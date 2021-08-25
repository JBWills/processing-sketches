package util.fonts

import kotlinx.serialization.Serializable
import util.io.getFileName
import util.io.getFontFilePath
import java.awt.Font
import java.io.File

@Serializable
data class FontData(
  val family: String,
  val weight: String,
  val type: String = "ttf"
) {
  val fullPath = getFontFilePath(family, weight, type)

  fun withWeight(newWeight: String) = FontData(family, newWeight, type)

  fun toFont(size: Double = 10.0): Font = loadFontMemo(fullPath).deriveFont(size.toFloat())

  fun displayString(): String = "$family - $weight"

  companion object {
    fun File.toFontData(): FontData {
      val fileName = getFileName()
      val (family, weight) = fileName.split('-').let { it.first() to it.last() }

      return FontData(family, weight, extension)
    }
  }
}
