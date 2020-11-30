package controls

class ControlGroup(vararg val controls: Control, val heightRatio: Number = 1) {
  val size get() = controls.size
}

fun List<ControlGroup>.totalRatio() = sumByDouble { it.heightRatio.toDouble() }

fun Control.toControlGroup() = ControlGroup(this)
fun List<Control>.toControlGroups() = map { it.toControlGroup() }