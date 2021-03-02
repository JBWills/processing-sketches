package util.io

import java.io.File

fun File.save(s: String) = printWriter().use {
  it.print(s)
}
