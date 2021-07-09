package util.io

import util.assertions.checkExists
import java.io.File
import java.io.InputStream

val IGNORED_FILE_NAMES = setOf(
  ".DS_Store",
)

fun File.save(s: String) = printWriter().use {
  it.print(s)
}

/**
 * @param path: absolute or relative path to file or directory
 * @param recursive: Look through subdirectories too
 * @param withExt: filter only for files with that extension, if null returns all files
 */
fun getAllFilesInPath(
  path: String,
  recursive: Boolean = false,
  withExt: String? = null
): List<File> {
  fun matchesExtension(file: File) = withExt == null || withExt == file.extension

  val f = File(path)
  if (!f.isDirectory) {
    return if (matchesExtension(f)) listOf(f) else listOf()
  }

  val thisFileOrDir = File(path)
  return thisFileOrDir
    .walk()
    .filter {
      when {
        it.absolutePath == thisFileOrDir.absolutePath ||
          it.name in IGNORED_FILE_NAMES ||
          it.isDirectory && !recursive ||
          !it.isDirectory && !matchesExtension(it) -> false
        else -> true
      }
    }
    .flatMap {
      when {
        it.isDirectory -> getAllFilesInPath(it.path, recursive, withExt)
        else -> listOf(it)
      }
    }
    .sortedBy { it.nameWithoutExtension }
    .toList()
}

fun File.streamFile(f: (InputStream) -> Unit): Unit {
  checkExists()
  val inputStream = inputStream()
  try {
    f(inputStream)
  } catch (e: Exception) {
    inputStream.close()
    throw e
  }
}
