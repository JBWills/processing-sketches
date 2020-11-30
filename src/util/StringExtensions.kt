package util

fun String.splitCamelCase() = replace(
  String.format(
    "%s|%s|%s",
    "(?<=[A-Z])(?=[A-Z][a-z])",
    "(?<=[^A-Z])(?=[A-Z])",
    "(?<=[A-Za-z])(?=[^A-Za-z])"
  ).toRegex(),
  " "
)