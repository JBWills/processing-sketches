package util.fonts

import arrow.core.memoize
import java.awt.Font
import java.io.File

private fun loadFont(path: String): Font {
  val file = File(path)
  if (file.extension != "ttf") throw Exception("Sorry only truetype supported right now")
  if (!file.exists()) throw Exception("Trying to load invalid font path: $path")

  return Font.createFont(Font.TRUETYPE_FONT, File(path).inputStream())
}

val loadFontMemo = ::loadFont.memoize()
