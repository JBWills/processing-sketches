package util.io

import java.io.File

fun File.getFileName(withExtension: Boolean = false) =
  if (withExtension) name else nameWithoutExtension
