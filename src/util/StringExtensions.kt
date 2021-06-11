package util

fun String.splitCamelCase() = replace(
  String.format(
    "%s|%s|%s",
    "(?<=[A-Z])(?=[A-Z][a-z])",
    "(?<=[^A-Z])(?=[A-Z])",
    "(?<=[A-Za-z])(?=[^A-Za-z])",
  ).toRegex(),
  " ",
)

fun String.lineLimit(limit: Int): String = lines().joinToString(separator = "\n", limit = limit)

fun String.isAllUniqueChars(): Boolean = uniqueChars() == this

fun String.uniqueChars(): String {
  val charsSeen = mutableSetOf<Char>()

  return buildString {
    this@uniqueChars.forEach { char ->
      if (!charsSeen.contains(char)) {
        charsSeen.add(char)
        append(char)
      }
    }
  }
}
