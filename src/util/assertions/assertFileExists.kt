package util.assertions

import java.io.File

fun File.checkExists() {
  if (!exists()) {
    throw Exception("Error: file does not exist: ${this.path}")
  }
}
