package util.polylines.clipping

enum class ForceClosedOption(val forceClose: Boolean?) {
  Close(true),
  NoClose(false),
  Default(null),
  ;

  companion object {
    fun Boolean?.toForceClosedOption() = when (this) {
      true -> Close
      false -> NoClose
      null -> Default
    }
  }
}
