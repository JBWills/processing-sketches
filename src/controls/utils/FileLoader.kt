package controls.utils

import BaseSketch
import java.io.File

fun BaseSketch.selectFile(onSelectFile: (File?) -> Unit) = selectInput(
  "Select file",
  FileLoader::onFileSelected.name,
  File(""),
  FileLoader(onSelectFile),
)

class FileLoader(val onSelectFile: (File?) -> Unit) {
  fun onFileSelected(f: File?) {
    onSelectFile(f)
  }
}
